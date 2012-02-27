README File
Thierry Moreau 996368746
Danil Shevelev 995118858

Running the Distributed MazeWar:
	- Execute the make command.
	- You may have to overwrite the permissions to allow the scripts to run on your machine. Enter the following command to do so:
		$ chmod u+x *
	- The game server needs to go online first before the clients can be run. To run the server, type the command:
		$ ./server.sh <portno> <numclients>
		ex: ./server.sh 4444 3
	- Because this game does not support dynamic addition and removal of players, the clients need to wait until the number of clients specified in the server run command (numclients) have joined the server before the game can start.
	- The clients are now ready to join. To launch a new instance of Mazewar, type the following command:
		$ ./run.sh <hostname> <portno>
	- Each player will have to enter their name. This will lauch the registering process on the server. Once the specified number of clients (specified by numclients) are registered to the server, the game can start.


Brief Description of the New Classes:
	Server side:
	1) MazeServer:
		- Implements the MazeServer, that listens to commands from all clients, orders them in a queue, and broadcast the ordered commands to all clients.
		- It also takes care of the start sequence of the game, making sure that all clients have joined the game in order.
		- This Class runs a MazeServerHandlerThread thread for each client connected to the server to listen an enqueue commands from these clients, and runs a single MazeserverReplierThread thread that dequeues the commands from the command queue and broadcasts it to all of the clients. It finally runs a MazeServerUpdateProjectiles thread that issues a UpdateProjectile commands to all client that moves all projectiles forward in their local maps.
	2) MazePacket:
		Packet format of the packets exchanged between the MazeWar Client and the Server. Contains, client event, client name, and max client info.
	3) MazeServerHandlerThread:
		Handles each client, listens to their commands (client events), and enqueues the commands accordingly.
	4) MazeserverReplierThread: 
		Dequeues the commands from the command queue, and broadcasts it to all of the clients.
	5) MazeServerUpdateProjectilesThread:
		Issues a UpdateProjectile commands to all client every 200ms by enqueing the command in the ServerInQueue. This command type moves all projectiles forward in the client's local maps. This is essential to maintain consistensy between events ordering between all clients since the projectiles used to get updated based on the client's local clocks.

	Client side:
	1) MazeClientHandler:
		- This class implements a ClientListener. We use it to listen to events that are triggered by the GUIClient. 
		- The MazeClientHandler connects to the MazeServer to send client commands.
		- For the following actions: forward(), backward(), turnlelft(), turnright() and fire(), the MazeClientHandler will send a MazePacket to the MazeServer.
		- The MazeClientHandler also spawns all of the remote clients and stores them based on the start sequence initated by the MazeServer.
		- The MazeClientHandler listens to incoming messages from the MazeServer to make either the GUIClient or one of the RemoteClients perform an action on the Maze.

Errata:
	For now exiting the game on one of the clients is not supported