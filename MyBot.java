import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            		&& !foodTargets.containsValue(route.getStart())
            		&& doMoveDirection(route.getStart(), route.getEnd()))
            {
                foodTargets.put(route.getEnd(), route.getStart());
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
}
