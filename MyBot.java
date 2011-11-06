import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ListSelectionEvent;

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
    
    public MyBot()
    {
    	gen = new Random(983);
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
        		Route route = new Route(antLoc, foodLoc, ants);
        		if(route.getDistance() > 0)
    			{
        			foodRoutes.add(route);
    			}
        	}
        }
        Collections.sort(foodRoutes);
        
        for (Route route : foodRoutes) {
            if (!foodTargets.containsKey(route.getEnd())
            		&& !orders.containsValue(route.getStart())
            		&& doMoveDirection(route))
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
            		Route route = new Route(antLoc, hillLoc, ants);
            		if(route.getDistance() > 0)
        			{
            			hillRoutes.add(route);
        			}
        		}
        	}
        }
        Collections.sort(hillRoutes);
        
        for(Route route: hillRoutes)
        {
        	doMoveDirection(route);
        }
        
        // explore
        for(Tile antLoc : sortedAnts)
        {
        	if(!orders.containsValue(antLoc))
        	{
        		List<Aim> directions = new ArrayList<Aim>();
        		for(Aim direction : Aim.values())
        		{
        			Tile loc = ants.getTile(antLoc, direction);
        			if(ants.getIlk(loc).isUnoccupied()
        					&& !orders.containsKey(loc))
        			{
        				directions.add(direction);
        			}
        		}
        		
        		Aim direction = directions.get(gen.nextInt(directions.size()));
        		doMoveDirection(antLoc, direction);
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
    
    private boolean doMoveDirection(Route route)
    {
    	Ants ants = getAnts();
    	
    	//Track all moves and prevent collisions
    	List<Aim> directions = ants.getDirections(route.getStart(), route.getNext());
    	for(Aim direction : directions)
    	{
	    	if(doMoveDirection(route.getStart(), direction))
	    	{
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
    private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
    private Set<Tile> enemyHills = new HashSet<Tile>();
    Random gen;
}
