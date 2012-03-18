#!/bin/bash


# arguments to BrokerClient
# $1 = hostname of where NameService is located
# $2 = port # where NameService is listening
# $3 = port # where the client is listening

java Mazewar $1 $2 $3
