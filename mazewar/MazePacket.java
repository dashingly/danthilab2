import java.io.Serializable;
 /**
 * BrokerPacket
 * ============
 * 
 * Packet format of the packets exchanged between the Broker and the Client
 * 
 */


/* inline class to describe host/port combo */
class CEPair implements Serializable {
	public String  		name;
    public ClientEvent 	event;
    
    public CEPair(String name, ClientEvent event) {
        this.name 	= name;
        this.event 	= event;
    }
        
    /* printable output */
    public String toString() {
            return " Player: " + this.name + " Event: " + this.event; 
    }
}

public class MazePacket implements Serializable {

        /* 
         * Client packet requests
         */
        public static final int P_NULL 	= 0;
        public static final int C_INIT 	= 101;
        public static final int C_EVENT	= 102;
        public static final int C_BYE 	= 199;
        
        /* 
         * Server packet replies
         */
        public static final int S_INIT	= 201;
        public static final int S_OPER 	= 202;
        public static final int S_BYE 	= 299;
               
        /* error codes */
        public static final int ERROR   = -101;
        
        public int 					type = MazePacket.P_NULL;  
        public static CEPair		pair;
        
        
        /* constructor */
        public MazePacket(int type, String name, ClientEvent event) {
        		this.type	= type;
                CEPair pair = new CEPair(name, event);
        }
        
        public MazePacket(int type, CEPair event) {
    		this.type	= type;
            this.pair = event;
        }
}
