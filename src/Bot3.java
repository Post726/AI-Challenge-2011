import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class Bot3 extends Bot
{
    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException
    {
        new Bot3().readSystemInput();
    }
    
    public Bot3()
    {
    	gen = new Random();
    }
    
    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn()
    {
        Ants ants = getAnts();        
        orders.clear();
        init(ants);
        AssignTasks(ants);
        updateMap(ants);
        
        // remove hill as possible destination
        for(Tile myHill : ants.getMyHills())
        {
        	orders.put(myHill, null);
        }
        
        for(Tile antLoc : ants.getMyAnts())
        {
        	if(ants.getTimeRemaining() < 100)
        		break;
        	
        	climbHill(antLoc);
        }
    }
    
    private void init(Ants ants)
    {
    	if(diffusionMap_attack == null)
        {
        	int numRows = ants.getRows();
        	int numCols = ants.getCols();
        	diffusionMap_attack = new HashMap<Tile, Double>(numRows*numCols);
        	diffusionMap_gather = new HashMap<Tile, Double>(numRows*numCols);
        	diffusionMap_guard = new HashMap<Tile, Double>(numRows*numCols);
        	for(int i = 0; i < numRows; ++i)
        	{
        		for(int j = 0; j < numCols; ++j)
        		{
        			diffusionMap_attack.put(new Tile(i,j), 0.0);
        			diffusionMap_gather.put(new Tile(i,j), 0.0);
        			diffusionMap_guard.put(new Tile(i,j), 0.0);
        		}
        	}
        }
    }
    
    private void AssignTasks(Ants ants)
    {
    	int numAttackers = 0;
    	int numGatherers = 0;
    	
    	for(Map.Entry<Tile, TaskType> entry : tasks.entrySet())
    	{
    		if(entry.getValue() == TaskType.ATTACK)
    		{
    			++numAttackers;
    		}
    		else if(entry.getValue() == TaskType.GATHER)
    		{
    			++numGatherers;
    		}
    	}
    	
    	for(Tile antLoc : ants.getMyAnts())
    	{
    		if(!tasks.containsKey(antLoc))
    		{
    			int enemiesVisible = 0;
    			if(ants.getEnemyAnts().size() != 0)
    			{
    				enemiesVisible = 1;
    			}
    			
    			int moreAttackers = 0;
    			if(numAttackers > numGatherers)
    			{
    				moreAttackers = 1;
    			}
    			
    			double rand = gen.nextDouble();
    			int row = 2*enemiesVisible + moreAttackers;
    			if(rand < taskNode[row][0])
    			{
    				tasks.put(antLoc, TaskType.ATTACK);
    			}
    			else if(rand < taskNode[row][1] + taskNode[row][0])
    			{
    				tasks.put(antLoc, TaskType.GATHER);
    			}
    			else
    			{
    				tasks.put(antLoc, TaskType.GUARD);
    			}
    		}
    	}
    }
    
    private void updateMap(Ants ants)
    {
    	// Add any seen enemy hills
        for(Tile enemyHill : ants.getEnemyHills())
        {
        	if(!enemyHills.contains(enemyHill))
        	{
        		enemyHills.add(enemyHill);
        	}
        }
        
        // Remove dead hills
        Set<Tile> tmp = new HashSet<Tile>(enemyHills);
        for(Tile enemyHill : tmp)
        {
        	if(ants.isVisible(enemyHill)
        			&& !ants.getEnemyHills().contains(enemyHill))
        	{
        		enemyHills.remove(enemyHill);
        	}
        }
    	
    	HashMap<Tile, Double> tmpAttackMap = new HashMap<Tile, Double>(diffusionMap_attack);
    	HashSet<Tile> attackTilesSet = new HashSet<Tile>();
    	HashMap<Tile, Double> tmpGatherMap = new HashMap<Tile, Double>(diffusionMap_gather);
    	HashSet<Tile> gatherTilesSet = new HashSet<Tile>();
    	HashMap<Tile, Double> tmpGuardMap = new HashMap<Tile, Double>(diffusionMap_guard);
    	HashSet<Tile> guardTilesSet = new HashSet<Tile>();
    	
    	setTiles(tmpAttackMap, attackTilesSet, ants.getEnemyHills(), VERY_POS_FORCE);
    	setTiles(tmpAttackMap, attackTilesSet, ants.getEnemyAnts(), POS_FORCE);
    	setTiles(tmpAttackMap, attackTilesSet, ants.getMyAnts(), NEG_FORCE);
    	updateMap(tmpAttackMap, attackTilesSet, diffusionMap_attack);
    	
    	setTiles(tmpGatherMap, gatherTilesSet, ants.getEnemyAnts(), NEG_FORCE);
    	setTiles(tmpGatherMap, gatherTilesSet, ants.getMyHills(), NEG_FORCE);
    	setTiles(tmpGatherMap, gatherTilesSet, ants.getMyAnts(), NEG_FORCE);
    	setTiles(tmpGatherMap, gatherTilesSet, ants.getFoodTiles(), VERY_POS_FORCE);
    	updateMap(tmpGatherMap, attackTilesSet, diffusionMap_gather);
    	
    	setTiles(tmpGuardMap, guardTilesSet, ants.getMyHills(), VERY_POS_FORCE);
    	setTiles(tmpGuardMap, guardTilesSet, ants.getMyAnts(), NEG_FORCE);
    	updateMap(tmpGuardMap, guardTilesSet, diffusionMap_guard);
    }
    
    private void setTiles(HashMap<Tile, Double> map, Set<Tile> tilesSet, Set<Tile> set, double value)
    {
    	for(Tile tile : set)
    	{
    		if(tilesSet.contains(tile))
			{
    			continue;
			}
    		
    		map.put(tile, value);
    		tilesSet.add(tile);
    	}
    }
    
    private void updateMap(HashMap<Tile, Double> oldMap, Set<Tile> tilesSet, HashMap<Tile, Double> newMap)
    {
    	Ants ants = getAnts();
    	
    	for(Map.Entry<Tile, Double> entry : newMap.entrySet())
    	{
    		Tile tile = entry.getKey();
    		
    		if(ants.getIlk(tile) == Ilk.WATER)
    		{
    			newMap.put(tile, 0.0);
    			continue;
    		}
    		
    		if(tilesSet.contains(tile))
    		{
    			newMap.put(tile, oldMap.get(tile));
    			continue;
    		}
    		
    		double sum = 0.0;
    		int numNeighbors = 0;
    		for(Aim direction : Aim.values())
    		{
    			Tile neighbor = ants.getTile(entry.getKey(), direction);
    			
    			if(ants.getIlk(neighbor) != Ilk.WATER)
        		{
    				sum += oldMap.get(neighbor);
    				++numNeighbors;
        		}
    		}
    		
    		newMap.put(tile, sum/numNeighbors);
    	}
    }
    
    private boolean climbHill(Tile antLoc)
    {
    	Ants ants = getAnts();
    	
    	Map<Double, Aim> directions = new TreeMap<Double, Aim>(new LessThan());
    	for(Aim direction : Aim.values())
    	{
    		Tile tile = ants.getTile(antLoc, direction);
    		if(ants.getIlk(tile) != Ilk.WATER)
    		{
    			switch(tasks.get(antLoc))
    			{
    			case ATTACK:
    				directions.put(diffusionMap_attack.get(tile), direction);
    				break;
    			case GATHER:
    				directions.put(diffusionMap_gather.get(tile), direction);
    				break;
    			case GUARD:
    				directions.put(diffusionMap_guard.get(tile), direction);
    				break;
    			}
    			
    		}
    	}
    	
    	for(Map.Entry<Double, Aim> entry : directions.entrySet())
		{
			if(doMoveDirection(antLoc, entry.getValue()))
			{
				return true;
			}
		}
    	
    	return false;
    }
    
    private boolean doMoveDirection(Tile antLoc, Aim direction)
    {
    	Ants ants = getAnts();
    	
    	//Track all moves and prevent collisions
    	Tile newLoc = ants.getTile(antLoc, direction);
    	if(ants.getIlk(newLoc).isUnoccupied()
    			&& !orders.containsKey(newLoc))
    	{
    		ants.issueOrder(antLoc, direction);
    		orders.put(newLoc, antLoc);
    		tasks.put(newLoc, tasks.get(antLoc));
			tasks.remove(antLoc);
    		return true;
    	}
    	
    	return false;
    }
    
	class LessThan implements Comparator<Double>
	{
		public int compare(Double a, Double b)
		{
			return -a.compareTo(b);
		}
	}
	
	// (Enemy Visible) (More Attackers than Gatherers) 
    //              \   /
    //             (Task)
    
    // Conditional Probability Table
    private double[][] taskNode = {
    		// Prob Attacker, Prob Gatherer, Prob Guard
    		{		0.35,		0.45,			0.20}, // Enemy not Visible, Less Attackers than Gatherers (or equal)
    		{		0.25,		0.55,			0.20}, // Enemy not Visible, More Attackers than Gatherers
    		{		0.40,		0.30,			0.30}, // Enemy Visible, Less Attackers than Gatherers (or equal)
    		{		0.35,		0.35,			0.30}  // Enemy Visible, More Attackers than Gatherers 
    };

    private final double POS_FORCE = 500;
    private final double VERY_POS_FORCE = 1000;
    private final double NEG_FORCE = -500;
    private final double VERY_NEG_FORCE = -1000;
    
    private HashMap<Tile, Double> diffusionMap_attack;
    private HashMap<Tile, Double> diffusionMap_gather;
    private HashMap<Tile, Double> diffusionMap_guard;
    private HashMap<Tile, TaskType> tasks = new HashMap<Tile, TaskType>();
    private HashMap<Tile, Tile> orders = new HashMap<Tile, Tile>();
    private HashSet<Tile> enemyHills = new HashSet<Tile>();
    Random gen;
   }
