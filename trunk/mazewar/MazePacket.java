import java.io.Serializable;
 /**
 * BrokerPacket
 * ============
 * 
 * Packet format of the packets exchanged between the Broker and the Client
 * 
 */


/* inline class to describe host/port combo */
class BrokerLocation implements Serializable {
        public String  broker_host;
        public Integer broker_port;
        
        /* constructor */
        public BrokerLocation(String host, Integer port) {
                this.broker_host = host;
                this.broker_port = port;
        }
        
        /* printable output */
        public String toString() {
                return " HOST: " + broker_host + " PORT: " + broker_port; 
        }
        
}

public class MazePacket implements Serializable {

        /* 
         * Client packet requests
         */
        public static final int P_NULL 	= 0;
        public static final int C_INIT 	= 101;
        public static final int C_OPER	= 102;
        public static final int C_BYE 	= 199;
        
        /* 
         * Server packet replies
         */
        public static final int S_INIT	= 201;
        public static final int S_OPER 	= 202;
               
        /* error codes */
        public static final int ERROR_INVALID_OPER   = -101;
                
        /* message header */
        /* for part 1/2/3 */
        public int type = MazePacket.P_NULL;
        
        /* request quote */
        /* for part 1/2/3 */
        public String symbol;
        
        /* quote */
        /* for part 1/2/3 */
        public Long quote;
        
        /* report errors */
        /* for part 2/3 */
        public int error_code;
        
        /* exchange lookup */
        /* for part 3 */
        public String         exchange;
        public int            num_locations;
        public BrokerLocation locations[];
        
}
