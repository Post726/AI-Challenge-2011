import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
        
        findFood(ants);
        
        for(Tile antLoc : ants.getMyAnts())
        {
        	if(!orders.containsValue(antLoc))
        	{
        		if(attackHills(ants, antLoc))
        			continue;
        		if(attackAnts(ants, antLoc))
        			continue;
        		if(moveOut(ants, antLoc))
        			continue;
        		if(randomDir(ants, antLoc))
        			continue;
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
    	List<Aim> directions = getAllDirections(ants.getDirections(antLoc, destLoc));
    	for(Aim direction : directions)
    	{
	    	if(doMoveDirection(antLoc, direction))
	    	{
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
    // Move in the direction the route states
    private boolean doMoveDirection(Route route)
    {
    	Ants ants = getAnts();
    	
    	//Track all moves and prevent collisions
    	List<Aim> directions = getAllDirections(ants.getDirections(route.getStart(), route.getNext()));
    	for(Aim direction : directions)
    	{
	    	if(doMoveDirection(route.getStart(), direction))
	    	{
	    		return true;
	    	}
    	}
    	
    	return false;
    }
    
    private void findFood(Ants ants)
    {
    	Map<Tile, Tile> foodTargets = new HashMap<Tile, Tile>();
    	List<Route> foodRoutes = new ArrayList<Route>();
        TreeSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
        TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        
        // Evaluate all food-ant combinations
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
        
        // Send the closest ant to each food in sight
        for (Route route : foodRoutes) {
            if (!foodTargets.containsKey(route.getEnd())
            		&& !orders.containsValue(route.getStart())
            		&& doMoveDirection(route))
            {
                foodTargets.put(route.getEnd(), route.getStart());
            }
        }
    }
    
    private boolean attackHills(Ants ants, Tile antLoc)
    {
    	List<Route> hillRoutes = new ArrayList<Route>();
        for(Tile hillLoc : enemyHills)
        {
    		Route route = new Route(antLoc, hillLoc, ants);
    		if(route.getDistance() > 0)
			{
    			hillRoutes.add(route);
			}
        }
        Collections.sort(hillRoutes);
        
        for(Route route: hillRoutes)
        {
        	doMoveDirection(route);
        	return true;
        }
        
        return false;
    }
    
    private boolean attackAnts(Ants ants, Tile antLoc)
    {
    	List<Route> antRoutes = new ArrayList<Route>();
        for(Tile enemyLoc : ants.getEnemyAnts())
        {
    		Route route = new Route(antLoc, enemyLoc, ants);
    		if(route.getDistance() > 0)
			{
    			antRoutes.add(route);
			}
        }
        Collections.sort(antRoutes);
        
        for(Route route: antRoutes)
        {
        	doMoveDirection(route);
        	return true;
        }
        
        return false;
    }
    
    private boolean moveOut(Ants ants, Tile antLoc)
    {
    	Tile closestHill = null;
    	int minDistance = Integer.MAX_VALUE;
    	for(Tile hillLoc : ants.getMyHills())
        {
    		int distance = ants.getDistance(antLoc, hillLoc);
    		if(distance < minDistance)
			{
    			minDistance = distance;
    			closestHill = hillLoc;
			}
        }
		
		if(closestHill != null)
		{
			if(closestHill.equals(antLoc))
			{
				return randomDir(ants, antLoc);
			}
			
			double rowDelta = antLoc.getRow() - closestHill.getRow();
			double colDelta = antLoc.getCol() - closestHill.getCol();
			double curAngle = Math.atan(rowDelta/colDelta);
			
			// account for quadrant (atan math)
			if(colDelta < 0)
				curAngle += Math.PI;
			
			// account for wall crossing
			if((Math.abs(rowDelta) > ants.getRows() - Math.abs(rowDelta)) || (Math.abs(colDelta) > ants.getCols() - Math.abs(colDelta)))
				curAngle += Math.PI;
			
			double angle = curAngle + gen.nextDouble()*Math.PI/2 - Math.PI/4;
			
			double curRange = Math.sqrt(minDistance);
			double range = curRange + gen.nextDouble()*10;
			
			int row = ((int)Math.round(closestHill.getRow() + Math.sin(angle)*range)) % ants.getRows();
			int col = ((int)Math.round(closestHill.getCol() + Math.cos(angle)*range)) % ants.getCols();

			return doMoveDirection(antLoc, new Tile(row, col));
		}
		
        return false;
    }
    
    private boolean moveHome(Ants ants, Tile antLoc)
    {
    	Tile closestHill = null;
    	int minDistance = Integer.MAX_VALUE;
    	for(Tile hillLoc : ants.getMyHills())
        {
    		int distance = ants.getDistance(antLoc, hillLoc);
    		if(distance < minDistance)
			{
    			minDistance = distance;
    			closestHill = hillLoc;
			}
        }
		
		if(closestHill != null)
		{
			Route route = new Route(antLoc, closestHill, ants);
	        
			return doMoveDirection(route);
		}
		
        return false;
    }
    
    private boolean randomDir(Ants ants, Tile antLoc)
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
		
		if(!directions.isEmpty())
		{
    		Aim direction = directions.get(gen.nextInt(directions.size()));
    		doMoveDirection(antLoc, direction);
    		return true;
		}
		
        return false;
    }
    
    private List<Aim> getAllDirections(List<Aim> directions)
    {
    	if(directions.size() == 1)
    	{
    		switch(directions.get(0))
    		{
    		case NORTH:
    			directions.add(Aim.EAST);
    			directions.add(Aim.WEST);
    			directions.add(Aim.SOUTH);
    		case EAST:
    			directions.add(Aim.SOUTH);
    			directions.add(Aim.NORTH);
    			directions.add(Aim.WEST);
    		case SOUTH:
    			directions.add(Aim.WEST);
    			directions.add(Aim.EAST);
    			directions.add(Aim.NORTH);
    		case WEST:
    			directions.add(Aim.NORTH);
    			directions.add(Aim.SOUTH);
    			directions.add(Aim.EAST);
    		}
    	}
    	else
    	{
    		for(Aim direction : Aim.values())
    		{
    			if(!directions.contains(direction))
    			{
    				directions.add(direction);
    			}
    		}
    	}
    	
    	return directions;
    }
    
    private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
    private Set<Tile> enemyHills = new HashSet<Tile>();
    Random gen;
}
