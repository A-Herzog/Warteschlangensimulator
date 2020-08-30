#!/bin/bash

# Builds a docker image for running Warteschlangensimulator
# in server mode on port 8183.

if [ -f "../Simulator.jar" ]
then
	cd ..
fi

if [ -f "../Simulator.jar" ]
then
	docker build -t qs:sim -f ./tools/docker-sim.txt .
	echo
	echo Run:
	echo docker run -d -p 8183:8183 qs:sim
else
	echo Simulator.jar not found.
fi