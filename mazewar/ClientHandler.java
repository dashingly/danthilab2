import java.net.*;
import java.io.*;
import java.util.*;

public class ClientHandler extends Thread{

        private Socket socket = null;
        //Store the sequence number of the command you last sent
        private int lastSent = 0;
       
        public ClientHandler(Socket socket, WarServer serve) {
                super("OnlineBrokerHandlerThread");
                this.socket = socket;
                System.out.println("Created new Thread to handle client");
        }
       
        public void run(WarServer serve) {

                boolean gotByePacket = false;
               
                /*
                 * What we can do is:
                 * 1. Check for command from the client.
                 * 2. If there is any -> add it to the queue.
                 * 3. Send queue operations from queue[lastsent] to queue[current] or to queue[greatest].
                 * 4. Increment "lastsent" accordingly. 
                 * 
                 * But actually the first thing we will have to do is send the seed of the map to the client.
                 */
               
                try {
                        /* stream to read from client */
                        ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                        MazePacket packetFromClient;
                       
                        /* stream to write back to client */
                        ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
                       
                       
                        while (( packetFromClient = (MazePacket) fromClient.readObject()) != null) {
                                /* create a packet to send reply back to client */
                                //MazePacket packetToClient = new MazePacket();
                                //TODO packetToClient.type = MazePacket.BROKER_QUOTE;
                               
                                /*
                                 * It seems like we do not have to worry about sending out seed. The map is created using "mazeSeed" constant.
                                 * Client wants to connect to the server and needs the seed for the map.
                                 *
                                if(packetFromClient.type == MazePacket.C_INIT) {
                                       
                                        //look up the quote from the cache
                                        packetToClient.symbol = packetFromClient.symbol;
                                        packetToClient.quote = (Long)cache.get(packetFromClient.symbol.toLowerCase());
                                        if( packetToClient.quote == null)
                                                packetToClient.quote = Long.parseLong("0");
                                       
                                        System.out.println("From Client: " + packetFromClient.symbol);
                               
                                        toClient.writeObject(packetToClient);
                                        continue;
                                }
                               
                               */
                                
                                /* 
                                 * If client wants to leave we have to broadcast it so that others can remove it locally.
                                 */
                                if (packetFromClient.type == MazePacket.P_NULL || packetFromClient.type == MazePacket.C_BYE) {
                                        gotByePacket = true;
                                        /*
                                         * TODO: need to write this up
                                         */
                                        //toClient.writeObject(packetToClient);
                                        //break;
                                }
                                
                                /* 
                                 * Here we process client events.
                                 */
                                if (packetFromClient.type == MazePacket.C_EVENT) {
                                        /*
                                         * TODO: Simply add event to the queue
                                         */
                                        serve.Add2Queue(packetFromClient.pair);
                                		//toClient.writeObject(packetToClient);
                                        //break;
                                }
                               
                                /*
                                 * Send the enqueued events out 
                                 */
                                {
                                	int lastQueued = serve.LastEvent();
                                	while (lastSent < lastQueued)
                                	{
                                		// Read in the pair
                                		lastSent++;
                                		MazePacket packetToClient = new MazePacket(MazePacket.S_OPER, (CEPair) serve.squeue.get(lastSent));
                                		// Send the packet out
                                		toClient.writeObject(packetToClient);
                                	}
                                }
                                
                                /* if code comes here, there is an error in the packet
                                System.err.println("ERROR: Unknown packet!!");
                                System.exit(-1);
                                */
                        }
                       
                        /* cleanup when client exits */
                        fromClient.close();
                        toClient.close();
                        socket.close();

                } catch (IOException e) {
                        if(!gotByePacket)
                                e.printStackTrace();
                } catch (ClassNotFoundException e) {
                        if(!gotByePacket)
                                e.printStackTrace();
                }
        }
       
        /*
        // build the cache out of the stock file
        private void buildCache() {
                try {
                        FileInputStream fstream = new FileInputStream("nasdaq");
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                        String line;

                        while ((line = br.readLine()) != null) {
                                parseLine(line);
                        }
                        in.close();
                } catch (Exception e) {
                        
                        e.printStackTrace();
                        System.exit(1);
                }
        }
       
        // helper function to parse each line of the file
        private void parseLine(String line) {
               
                // check if the line is blank
                if (line.trim().length() != 0) {
                        StringTokenizer st;
                        String symbol, inputQuote;
                        Long quote;

                        st = new StringTokenizer(line, " ");
                       
                        // remove leading and trailing whitespace from each field
                        symbol = st.nextToken().trim();
                        inputQuote = st.nextToken().trim();
                        // convert quote to Long object
                        quote = Long.parseLong(inputQuote);

                        // add each (symbol, quote) pair to the cache
                        //cache.put(symbol, quote);
                }
        }
        */

}