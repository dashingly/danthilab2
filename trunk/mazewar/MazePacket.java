import java.io.Serializable;
 /**
 * MazePacket
 * Packet format of the packets exchanged between the MazeWar Client and the Server
 */

public class MazePacket implements Serializable {

	/* define constants */
	public static final int MAZE_NULL    = 0;
	public static final int ADD_CLIENT = 101;
	public static final int CLIENT_EVENT = 102;
	
	/* error codes */
	public static final int ERROR_INVALID_SYMBOL   = -101;
	
	/* message header */
	public int type = MazePacket.MAZE_NULL;
	
	/* report errors */
	public int error_code = MazePacket.MAZE_NULL;
	
	/* client name */
	public String ClientName;
	
	/* server max player */
	public int MaxNumClient;
	
	/* client event */
	public int ce;
	
	/* max num client */
	
	
}
