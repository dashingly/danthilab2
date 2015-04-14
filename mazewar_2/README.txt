README File

Running the Distributed MazeWar:
So here's how to launch a game:

>> namingService.sh 4444 # launch the naming service
>> run.sh localhost 4444 5555 0
This will launch local client 1
        // enter your name in the box (Danil)
>> run.sh localhost 4444 6666 0
This will launch local client 2
        // enter your name in the box (Thierry)

To add a robot client do:
>> run.sh localhost 4444 7777 1
The one at the end indicates that client is a robot. Make sure to add the robot once everyone has joined the game.
