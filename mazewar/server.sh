#!/bin/bash
JAVA_HOME=/cad2/ece419s/java/jdk1.6.0/

# arguments to MazeServer
# $1 = port # where MazeServer is listening

${JAVA_HOME}/bin/java MazeServer $1
