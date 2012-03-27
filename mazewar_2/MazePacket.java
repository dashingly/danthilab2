import java.io.Serializable;
 /**
 * MazePacket
 * Packet format of the packets exchanged between the MazeWar Client and the Server
 */


/* inline class to describe host/port combo */
class ClientIP implements Serializable {
	public String  client_name;
	public String  client_host;
	public Integer client_port;
	public Integer client_number;
	
	/* constructor */
	public ClientIP(String client_name, String host, Integer port, Integer number) {
		this.client_name = client_name;
		this.client_host = host;
		this.client_port = port;
		this.client_number = number;
	}
	
	/* printable output */
	public String toString() {
		return " HOST: " + client_host + " PORT: " + client_port + " NUMBER:" + client_number; 
	}
	
}

public class MazePacket implements Serializable {

	/* define constants */
	public static final int MAZE_NULL    = 0;
	public static final int ADD_CLIENT = 101;
	public static final int CLIENT_EVENT = 102;
	public static final int UPDATE_PROJECTILES = 103;
	
	/* for the naming service */
	public static final int NS_REGISTER = 201;
	public static final int NS_REPLY    = 202;
	public static final int NS_ADD      = 203;
	
	/* error codes */
	public static final int ERROR_INVALID_SYMBOL   = -101;
	public static final int GET_SEQs 	= 777;
	public static final int SEQs 		= 778;
	
	/* message header */
	public int type = MazePacket.MAZE_NULL;
	
	/* report errors */
	public int error_code = MazePacket.MAZE_NULL;
	
	/* client name */
	public String ClientName;
	
	/* number of registered clients */
	public int NumClients;
	
	/* client event */
	public int ce;
	
	/* client locations */
	public ClientIP locations[];
	
	/* Sequence number from ticketing service */
	public int seqs;
	
	
}
