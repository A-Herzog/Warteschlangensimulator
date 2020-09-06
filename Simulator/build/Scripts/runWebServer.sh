#!/bin/bash

# Runs the simulator in web server mode.
# Simulation requests can be send to the
# simulator and statistics results can be
# downloaded or viewed by any web browser.

if [ -f "../Simulator.jar" ]
then
	java -jar ../Simulator.jar serverWeb 8000
else
	echo English:
	echo This file has to be run from its own directory to find the simulator.
	echo Deutsch:
	echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
	echo um den Simulator finden zu können.
fi

# The command needs one parameter:
# The port number to listen on for requests.

# Alternative for using a fixed model:
# java -jar ./Simulator.jar serverWebFixed 8000 model.xml