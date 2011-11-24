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
    	for(Tile antLoc : ants.getMyAnts())
    	{
    		if(!tasks.containsKey(antLoc))
    		{
    			double rand = gen.nextDouble();
    			
    			if(rand < .333)
    			{
    				tasks.put(antLoc, TaskType.ATTACK);
    			}
    			else if(rand < .667)
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
    	HashMap<Tile, Double> tmpGatherMap = new HashMap<Tile, Double>(diffusionMap_gather);
    	HashMap<Tile, Double> tmpGuardMap = new HashMap<Tile, Double>(diffusionMap_guard);
    	
    	setTiles(tmpAttackMap, ants.getEnemyHills(), VERY_POS_FORCE);
    	setTiles(tmpAttackMap, ants.getEnemyAnts(), POS_FORCE);
    	setTiles(tmpAttackMap, ants.getMyAnts(), NEG_FORCE);
    	updateMap(tmpAttackMap, diffusionMap_attack);
    	
    	setTiles(tmpGatherMap, ants.getEnemyAnts(), NEG_FORCE);
    	setTiles(tmpGatherMap, ants.getMyHills(), NEG_FORCE);
    	setTiles(tmpGatherMap, ants.getMyAnts(), NEG_FORCE);
    	setTiles(tmpGatherMap, ants.getFoodTiles(), VERY_POS_FORCE);
    	updateMap(tmpGatherMap, diffusionMap_gather);
    	
    	setTiles(tmpGuardMap, ants.getMyHills(), VERY_POS_FORCE);
    	setTiles(tmpGuardMap, ants.getMyAnts(), NEG_FORCE);
    	updateMap(tmpGuardMap, diffusionMap_guard);
    }
    
    private void setTiles(HashMap<Tile, Double> map, Set<Tile> set, double value)
    {
    	for(Tile tile : set)
    	{
    		map.put(tile, value);
    	}
    }
    
    private void updateMap(HashMap<Tile, Double> oldMap, HashMap<Tile, Double> newMap)
    {
    	Ants ants = getAnts();
    	
    	for(Map.Entry<Tile, Double> entry : newMap.entrySet())
    	{
    		Tile tile = entry.getKey();
    		
    		if(ants.getIlk(tile) == Ilk.WATER)
    		{
    			newMap.put(tile, 0.0);
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
