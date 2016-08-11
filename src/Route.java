import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a route from one tile to another.
 */

public class Route implements Comparable<Route>
{
	private Tile start;
	private Tile end;
	private List<Tile> path;
	
	public Route(Tile start, Tile end, Ants ants)
	{
		this.start = start;
		this.end = end;
		
		this.path = new ArrayList<Tile>();
		
		if(start.equals(end))
			return;
		
		if(ants.getIlk(start).isPassable() && ants.getIlk(end).isPassable())
			findBestPath(ants);
		else
			System.err.println("Error: start/end of rout is not passable");
	}
	
	public boolean isValid()
	{
		return !path.isEmpty(); 
	}

	public Tile getStart()
	{
		return start;
	}
	
	public Tile getEnd()
	{
		return end;
	}
	
	public int getDistance()
	{
		if(path.isEmpty())
		{
			return -1;
		}
		
		return path.size();
	}
	
	public Tile getNext()
	{
		if(path.isEmpty())
		{
			return null;
		}
		
		return path.get(0);
	}
	
	public void moveNext()
	{
		if(!path.isEmpty())
			this.start = path.remove(0);
	}
	
	// A* search
	public void findBestPath(Ants ants)
	{
		Set<Tile> visited = new HashSet<Tile>();
		Set<Tile> available = new HashSet<Tile>();
		Map<Tile, Tile> predecessor = new HashMap<Tile, Tile>();
		
		Map<Tile, Double> f = new HashMap<Tile, Double>(); // node score
		Map<Tile, Double> g = new HashMap<Tile, Double>(); // node cost
		// heuristic is sqrt(ants.getDistance())
		
		// Initialize states for start 
		available.add(this.start);
		g.put(this.start, 0.0);
		f.put(this.start, Math.sqrt((double) ants.getDistance(this.start, this.end)));
		
		while(!available.isEmpty())
		{
			if(ants.getTimeRemaining() < timeoutRoute)
				return;
			
			// Find the best next move
			double lowestScore = Double.MAX_VALUE;
			Tile best = null;
			for(Tile loc : available)
			{
				if(ants.getTimeRemaining() < timeoutRoute)
					return;
				
				if(f.get(loc) < lowestScore)
				{
					lowestScore = f.get(loc);
					best = loc;
				}
			}
			
			available.remove(best);
			visited.add(best);
			
			// Found the end
			if(best.equals(this.end))
			{
				Tile loc = this.end;
				while(loc != this.start)
				{
					path.add(0, loc);
					loc = predecessor.get(loc);
				}
				return;
			}
			
			List<Tile> availableNeighbors = new ArrayList<Tile>();
			for(Aim direction : Aim.values())
			{
				Tile loc = ants.getTile(best, direction);
				if( ants.getIlk(loc).isPassable() 
						&& !ants.getMyHills().contains(loc)
						&& !visited.contains(loc))
				{
					availableNeighbors.add(loc);
				}
			}
			
			for(Tile next : availableNeighbors)
			{	
				double cost = g.get(best) + 1.0;
				if(!available.contains(next) || cost < g.get(next))
				{
					available.add(next); // add it if it is not already there
					
					g.put(next, cost);
					
					double h = Math.sqrt((double) ants.getDistance(next, this.end));
					f.put(next, cost + h);
					
					predecessor.put(next, best);
				}
			}
		}
		
		System.err.println("Error: No Path!");
	}

	@Override
	public int compareTo(Route route)
	{
		return getDistance() - route.getDistance();
	}
	
	@Override
	public int hashCode()
	{
		return start.hashCode() * Ants.MAX_MAP_SIZE * Ants.MAX_MAP_SIZE + end.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		boolean result = false;
		if(o instanceof Route)
		{
			Route route = (Route)o;
			result = start.equals(route.start) && end.equals(route.end);
		}
		
		return result;
	}
	
	private final int timeoutRoute = 175;
}
