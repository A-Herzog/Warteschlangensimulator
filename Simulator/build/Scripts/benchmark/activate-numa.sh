#!/bin/bash

# Activates NUMA mode in Warteschlangensimulator.

# Aktiviert den NUMA-Modus im Warteschlangensimulator.

if [ -z "$JAVA_HOME" ]
then
	JAVA_RUN="java"
else
	JAVA_RUN="${JAVA_HOME}/bin/java"
fi

if [ -f "../../Simulator.jar" ]
then
	${JAVA_RUN} -jar ../../Simulator.jar SetNUMA 1
else
	echo English:
	echo This file has to be run from its own directory to find the simulator.
	echo Deutsch:
	echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
	echo um den Simulator finden zu können.
fi
