#!/bin/bash
JAVA_HOME=/cad2/ece419s/java/jdk1.6.0/

# arguments to BrokerClient
# $1 = hostname of where Mazewar is located
# $2 = port # where Mazewar is listening

${JAVA_HOME}/bin/java Mazewar $1 $2
