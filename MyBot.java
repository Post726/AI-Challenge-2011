import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MyBot extends Bot {
    /**
     * Main method executed by the game engine for starting the bot.
     * 
     * @param args command line arguments
     * 
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new MyBot().readSystemInput();
    }
    
    /**
     * For every ant check every direction in fixed order (N, E, S, W) and move it if the tile is
     * passable.
     */
    @Override
    public void doTurn() {
        Ants ants = getAnts();
        orders.clear();
        Map<Tile, Tile> foodTargets = new HashMap<Tile, Tile>();
        
        // initialize unseenTiles
        if(unseenTiles == null)
        {
        	unseenTiles = new HashSet<Tile>();
        	for(int row = 0; row < ants.getRows(); ++row)
        	{
        		for(int col = 0; col < ants.getCols(); ++col)
        		{
        			unseenTiles.add(new Tile(row, col));
        		}
        	}
        }
        
        // remove any new tiles seen
        for(Iterator<Tile> locIter = unseenTiles.iterator(); locIter.hasNext(); )
        {
        	Tile next = locIter.next();
        	if(ants.isVisible(next))
        	{
        		locIter.remove();
        	}
        }
        
        // add any seen enemy hills
        for(Tile enemyHill : ants.getEnemyHills())
        {
        	if(!enemyHills.contains(enemyHill))
        	{
        		enemyHills.add(enemyHill);
        	}
        }
        
        // remove hill as possible destination
        for(Tile myHill : ants.getMyHills())
        {
        	orders.put(myHill, null);
        }
        
        // Find close food
        List<Route> foodRoutes = new ArrayList<Route>();
        TreeSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
        TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        for(Tile foodLoc : sortedFood)
        {
        	for(Tile antLoc : sortedAnts)
        	{
        		int distance = ants.getDistance(antLoc, foodLoc);
        		Route route = new Route(antLoc, foodLoc, distance);
        		foodRoutes.add(route);
        	}
        }
        Collections.sort(foodRoutes);
        
        for (Route route : foodRoutes) {
            if (!foodTargets.containsKey(route.getEnd())
            		&& !orders.containsValue(route.getStart())
            		&& doMoveDirection(route.getStart(), route.getEnd()))
            {
                foodTargets.put(route.getEnd(), route.getStart());
            }
        }
        
        // attack hills
        List<Route> hillRoutes = new ArrayList<Route>();
        for(Tile hillLoc : enemyHills)
        {
        	for(Tile antLoc : sortedAnts)
        	{
        		if(!orders.containsValue(antLoc))
        		{
        			int distance = ants.getDistance(antLoc, hillLoc);
            		Route route = new Route(antLoc, hillLoc, distance);
            		hillRoutes.add(route);
        		}
        	}
        }
        Collections.sort(hillRoutes);
        
        for(Route route: hillRoutes)
        {
        	doMoveDirection(route.getStart(), route.getEnd());
        }
        
        // explore unseen areas (send each unassigned ant to the closest unseen tile)
        for(Tile antLoc : sortedAnts)
        {
        	if(!orders.containsValue(antLoc))
        	{
        		List<Route> unseenRoutes = new ArrayList<Route>();
        		for(Tile unseenLoc : unseenTiles)
        		{
        			int distance = ants.getDistance(antLoc, unseenLoc);
        			Route route = new Route(antLoc, unseenLoc, distance);
        			unseenRoutes.add(route);
        		}
        		Collections.sort(unseenRoutes);
        		
        		for(Route route : unseenRoutes)
        		{
        			if(doMoveDirection(route.getStart(), route.getEnd()))
        			{
        				break;
        			}
        		}
        	}
        }
        
        // unblock the hill
        for(Tile myHill : ants.getMyHills())
        {
        	if(ants.getMyAnts().contains(myHill) && !orders.containsValue(myHill))
        	{
        		for(Aim direction : Aim.values())
        		{
        			if(doMoveDirection(myHill, direction))
        			{
        				break;
        			}
        		}
        	}
        }
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
    		return true;
    	}
    	
    	return false;
    }
    
    private boolean doMoveDirection(Tile antLoc, Tile destLoc)
    {
    	Ants ants = getAnts();
    	
    	//Track all moves and prevent collisions
    	List<Aim> directions = ants.getDirections(antLoc, destLoc);
    	for(Aim direction : directions)
    	{
	    	if(doMoveDirection(antLoc, direction))
	    	{
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
    private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
    private Set<Tile> unseenTiles;
    private Set<Tile> enemyHills = new HashSet<Tile>();
}
