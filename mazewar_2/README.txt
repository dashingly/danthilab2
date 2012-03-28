README File
Thierry Moreau 996368746
Danil Shevelev 995118858

Running the Distributed MazeWar:
So here's how to launch a game:

>> namingService.sh 4444 # launch the naming service
>> run.sh localhost 4444 5555 # launch local client 1
        // enter your name in the box (Danil)
>> run.sh localhost 4444 6666 # launch local client 2
        // enter your name in the box (Thierry)

To add a robot client do:
>> run.sh localhost 4444 7777 1
One at the end indicates that cllient is a robot.
