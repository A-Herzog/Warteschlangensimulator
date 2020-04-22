#!/bin/bash

# Performs benchmarks runs using the 1,2,... up to the specified number of CPU cores.
# Results are appended to benchmark-result.txt.

# Führt einen Benchmark-Läufe unter Verwendung von 1,2,... bis zur angegebenen Anzahl an CPU-Kernen durch.
# Die Ergebnisse werden an die Datei benchmark-result.txt angehängt.

if [ "$1" == "" ]
then
	echo English:
	echo The maximum number of cores to be used must be specified as a parameter.
	echo An integer number greater than or equal to 1 must be specified.
	echo Deutsch:
	echo Als Parameter muss die Maximalanzahl an zu verwendenden Kernen angegeben werden.
	echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
else
	for ((x=1;x<=$1;x++))
	do	
		./benchmark-native.sh $x
	done
fi