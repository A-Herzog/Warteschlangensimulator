#!/bin/bash

# Performs multiple benchmarks runs each using the same specified number of CPU cores.
# Results are appended to benchmark-results.txt.

# Führt mehrere Benchmark-Läufe unter Verwendung von jeweils derselben angegebenen Anzahl an CPU-Kernen durch.
# Die Ergebnisse werden an die Datei benchmark-results.txt angehängt.

if [ -f "../Simulator.jar" ]
then
	if [ "$1" == "" ]
	then
		echo English:
		echo The number of cores to be used must be specified as first parameter.
		echo An integer number greater than or equal to 1 must be specified.
		echo Deutsch:
		echo Als erster Parameter muss die Anzahl an zu verwendenden Kernen angegeben werden.
		echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
	else
		if [ "$2" == "" ]
		then
			echo English:
			echo The number of repetitions must be specified as second parameter.
			echo An integer number greater than or equal to 1 must be specified.
			echo Deutsch:
			echo Als zweiter Parameter muss die Anzahl an Wiederholungen angegeben werden.
			echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
		else
			for ((x=1;x<=$2;x++))
			do	
				./benchmark.sh $1
			done
		fi
	fi
else
	echo English:
	echo This file has to be run from its own directory to find the simulator.
	echo Deutsch:
	echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
	echo um den Simulator finden zu können.
fi
