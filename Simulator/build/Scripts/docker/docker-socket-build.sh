#!/bin/bash

# Builds a docker image for running Warteschlangensimulator
# in socket server mode on port 10000.

if [ -f "../../Simulator.jar" ]
then
	cd ..
fi

if [ -f "../Simulator.jar" ]
then
	cd ..
fi

if [ -f "./Simulator.jar" ]
then
	docker build -t qs:socket -f ./tools/docker/docker-socket.txt .
	echo
	echo Run:
	echo docker run -d -p 10000:10000 qs:socket
else
	echo Simulator.jar not found.
fi