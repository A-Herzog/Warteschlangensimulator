#!/bin/bash

# Performs a single benchmark run using the specified number of CPU cores.
# Results are appended to benchmark-results.txt.

# Führt einen einzelnen Benchmark-Lauf unter Verwendung der angegebenen Anzahl an CPU-Kernen durch.
# Die Ergebnisse werden an die Datei benchmark-results.txt angehängt.

if [ -f "../../Simulator.jar" ]
then
	if [ "$1" == "" ]
	then
		echo English:
		echo The number of cores to be used must be specified as a parameter.
		echo An integer number greater than or equal to 1 must be specified.
		echo Deutsch:
		echo Als Parameter muss die Anzahl an zu verwendenden Kernen angegeben werden.
		echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
	else
		if [ -f "benchmark-statistics.xml" ]
		then
			rm benchmark-statistics.xml
		fi
		../../Simulator SetMaxThreads $1
		../../Simulator Simulation benchmark-model.xml benchmark-statistics.xml
		../../Simulator Filter benchmark-statistics.xml benchmark-filter.js benchmark-results.txt
		../../Simulator SetMaxThreads 0
		if [ -f "benchmark-statistics.xml" ]
		then
			rm benchmark-statistics.xml
		fi
	fi
else
	echo English:
	echo This file has to be run from its own directory to find the simulator.
	echo Deutsch:
	echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
	echo um den Simulator finden zu können.
fi
