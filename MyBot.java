import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class MyBot extends Bot
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
        new MyBot().readSystemInput();
    }
    
    public MyBot()
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
        checkTasks();
        
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
        
        // remove hill as possible destination
        for(Tile myHill : ants.getMyHills())
        {
        	orders.put(myHill, null);
        }
        
        findFood(ants);
        
        for(Tile antLoc : ants.getMyAnts())
        {
        	if(ants.getTimeRemaining() < timeoutOther)
        		break;
        	
        	if(!tasks.containsKey(antLoc))
        	{
        		if(attackHills(ants, antLoc))
        			continue;
        		if(attackAnts(ants, antLoc))
        			continue;
        		if(randomDir(ants, antLoc) != null)
        			continue;
        	}
        }
        
        doTasks();
        
        System.err.println(ants.getTimeRemaining());
    }
    
    private void checkTasks()
    {
    	Ants ants = getAnts();
    	TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
    	
    	HashSet<Entry<Tile, Task>> entrySet = new HashSet<Map.Entry<Tile,Task>>(tasks.entrySet());
    	for(Map.Entry<Tile, Task> entry : entrySet)
    	{
    		// Remove lost ants
    		if(!sortedAnts.contains(entry.getKey()))
    		{
    			tasks.remove(entry.getKey());
    		}
    		
    		// Check if food target is gone
    		else if(entry.getValue().type == Task.Type.GATHER
    				&& ants.getIlk(entry.getValue().route.getEnd()) != Ilk.FOOD)
    		{
    			tasks.remove(entry.getKey());
    		}
    		
    		// Check if hill target is gone
    		else if(entry.getValue().type == Task.Type.ATTACK_HILL
    				&& !enemyHills.contains(entry.getValue().route.getEnd()))
    		{
    			tasks.remove(entry.getKey());
    		}
    		
    		// check if route needs to be recomputed
    		else if(!entry.getValue().route.isValid())
    		{
    			Route route = tasks.get(entry.getKey()).route;
    			route.findBestPath(ants);
    			if(!route.isValid())
    				tasks.remove(entry.getKey());
    		}
    	}
    }
    
    private void doTasks()
    {
    	HashSet<Entry<Tile, Task>> entrySet = new HashSet<Map.Entry<Tile,Task>>(tasks.entrySet());
    	for(Map.Entry<Tile, Task> entry : entrySet)
    	{
    		if(getAnts().getTimeRemaining() < timeoutTask)
        		break;
    		if(!orders.containsValue(entry.getKey()))
    		{
    			Tile next = doMoveDirection(entry.getValue().route);
    			if(next != null)
    			{
	    			tasks.put(next, entry.getValue());
	    			tasks.remove(entry.getKey());
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
    private Tile doMoveDirection(Route route)
    {
    	if(!route.isValid())
    	{
    		return randomDir(getAnts(), route.getStart());
    	}
    	
    	Ants ants = getAnts();
    	Tile antLoc = route.getStart();
    	
    	//Track all moves and prevent collisions
    	List<Aim> directions = getAllDirections(ants.getDirections(antLoc, route.getNext()));
    	for(Aim direction : directions)
    	{
	    	if(doMoveDirection(antLoc, direction))
	    	{
	    		Tile next = ants.getTile(antLoc, direction);
	    		
	    		// Update the route
	    		if(route.getNext().equals(next))
	    		{
	    			route.moveNext();
	    		}
	    		else
	    		{
	    			if(tasks.containsKey(antLoc))
	    			{
	    				tasks.get(antLoc).route = new Route(next, route.getEnd(), ants);
	    			}
	    		}
	    		
	    		return next;
	    	}
    	}
    	
    	return null;
    }
    
    private void findFood(Ants ants)
    {
    	TreeSet<Tile> sortedFood = new TreeSet<Tile>(ants.getFoodTiles());
        TreeSet<Tile> sortedAnts = new TreeSet<Tile>(ants.getMyAnts());
        
        for(Map.Entry<Tile, Task> entry : tasks.entrySet())
        {
        	// Remove food which has already been tasked
        	if(entry.getValue().type == Task.Type.GATHER)
        	{
        		sortedFood.remove(entry.getValue().route.getEnd());
        	}
        }
        
        // Evaluate all food-ant combinations
        while(sortedFood.size() != 0)
        {
        	if(ants.getTimeRemaining() < timeoutFood)
        		return;
        	
        	Tile foodLoc = sortedFood.first();
        	
        	List<Route> foodRoutes = new ArrayList<Route>();
        	
        	for(Tile antLoc : sortedAnts)
        	{
        		Route route;
        		if(tasks.containsKey(antLoc)
        				&& tasks.get(antLoc).route.getEnd().equals(foodLoc))
        		{
        			route = tasks.get(antLoc).route;
        		}
        		else
        		{
        			route = new Route(antLoc, foodLoc, ants);
        		}
        		
        		if(route.isValid())
    			{
        			foodRoutes.add(route);
    			}
        	}
        	Collections.sort(foodRoutes);
        	
        	boolean assigned = false;
        	for (Route route : foodRoutes)
        	{
        		Tile antLoc = route.getStart();
        		
        		// Closest ant does not have a task and can get the food
        		if(!tasks.containsKey(antLoc))
        		{
        			sortedFood.remove(foodLoc);
        			tasks.put(antLoc, new Task(Task.Type.GATHER, route));
        			assigned = true;
        			break;
        		}
        		
        		// Closest ant is tasked with food which is farther
        		else if(tasks.containsKey(antLoc)
        				&& tasks.get(antLoc).route.getDistance() > route.getDistance())
				{
        			sortedFood.remove(foodLoc);
        			sortedFood.add(tasks.get(antLoc).route.getEnd());
        			tasks.get(antLoc).route = route;
        			assigned = true;
        			break;
				}
        	}
        	
        	// All ants are assigned to closer food
        	if(!assigned)
        	{
        		sortedFood.remove(foodLoc);
        	}
        }
    }
    
    private boolean attackHills(Ants ants, Tile antLoc)
    {
    	List<Route> hillRoutes = new ArrayList<Route>();
        for(Tile hillLoc : enemyHills)
        {
        	Route route;
    		if(tasks.containsKey(antLoc)
    				&& tasks.get(antLoc).route.getEnd().equals(hillLoc))
    		{
    			route = tasks.get(antLoc).route;
    		}
    		else
    		{
    			route = new Route(antLoc, hillLoc, ants);
    		}
    		
    		if(route.isValid())
			{
    			hillRoutes.add(route);
			}
        }
        Collections.sort(hillRoutes);
        
        for(Route route: hillRoutes)
        {
        	tasks.put(antLoc, new Task(Task.Type.ATTACK_HILL, route));
        	return true;
        }
        
        return false;
    }
    
    private boolean attackAnts(Ants ants, Tile antLoc)
    {
    	Tile closestEnemy = null;
    	int minDistance = Integer.MAX_VALUE;
    	
        for(Tile enemyLoc : ants.getEnemyAnts())
        {
    		int distance = ants.getDistance(antLoc, enemyLoc);
    		if(distance < minDistance)
			{
    			minDistance = distance;
    			closestEnemy = enemyLoc;
			}
        }
        
        if(closestEnemy == null)
        	return false;
        
        Route route = new Route(antLoc, closestEnemy, ants);
        return doMoveDirection(route) != null;
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
				return randomDir(ants, antLoc) != null;
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
	        
			return doMoveDirection(route) != null;
		}
		
        return false;
    }
    
    private Tile randomDir(Ants ants, Tile antLoc)
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
    		
    		
    		return ants.getTile(antLoc, direction);
		}
		
        return null;
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
    private Map<Tile, Task> tasks = new HashMap<Tile, Task>();
    private Set<Tile> enemyHills = new HashSet<Tile>();
    Random gen;
    private final int timeoutFood = 200;
    private final int timeoutOther = 150;
    private final int timeoutTask = 50;
   }
