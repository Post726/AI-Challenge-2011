import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        
        for (Tile myAnt : ants.getMyAnts()) {
            for (Aim direction : Aim.values()) {
                if (doMoveDirection(myAnt, direction)) {
                    break;
                }
            }
        }
    }
    
    private boolean doMoveDirection(Tile antLoc, Aim direction)
    {
    	Ants ants = getAnts();
    	
    	//Track all moves and prevent collisions
    	Tile newLoc = ants.getTile(antLoc, direction);
    	if(ants.getIlk(newLoc).isUnoccupied() && !orders.containsKey(newLoc))
    	{
    		ants.issueOrder(antLoc, direction);
    		orders.put(newLoc, antLoc);
    		return true;
    	}
    	
    	return false;
    }
    
    private Map<Tile, Tile> orders = new HashMap<Tile, Tile>();
}
