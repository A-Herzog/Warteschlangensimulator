#!/bin/bash

# Builds a docker image for running Warteschlangensimulator
# in web server mode on port 81.

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
	docker build -t qs:web -f ./tools/docker/docker-web.txt .
	echo
	echo Run:
	echo docker run -d -p 81:81 qs:web
else
	echo Simulator.jar not found.
fi