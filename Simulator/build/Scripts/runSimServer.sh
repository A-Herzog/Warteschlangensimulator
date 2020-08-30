#!/bin/bash

# Runs the simulator in compute server mode.
# Other instances will be able to outsource
# simulation tasks to this server.

if [ -f "../Simulator.jar" ]
then
	java -jar ../Simulator.jar server 8183
else
	echo English:
	echo This file has to be run from its own directory to find the simulator.
	echo Deutsch:
	echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
	echo um den Simulator finden zu können.
fi

# Parameters (all optional):
# 1. Port number to listen on for requests.
# 2. Password