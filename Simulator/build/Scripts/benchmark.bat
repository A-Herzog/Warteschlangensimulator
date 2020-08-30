@echo off

rem Performs a single benchmark run using the specified number of CPU cores.
rem Results are appended to benchmark-results.txt.

rem Führt einen einzelnen Benchmark-Lauf unter Verwendung der angegebenen Anzahl an CPU-Kernen durch.
rem Die Ergebnisse werden an die Datei benchmark-results.txt angehängt.

if exist ../Simulator.jar goto work1
echo English:
echo This file has to be run from its own directory to find the simulator.
echo Deutsch:
echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
echo um den Simulator finden zu können.
goto end

: work1
if not "%1"=="" goto work2
echo English:
echo The number of cores to be used must be specified as a parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als Parameter muss die Anzahl an zu verwendenden Kernen angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:work2
if exist benchmark-statistics.xml del benchmark-statistics.xml
java -jar ../Simulator.jar SetMaxThreads %1
java -jar ../Simulator.jar Simulation benchmark-model.xml benchmark-statistics.xml
java -jar ../Simulator.jar Filter benchmark-statistics.xml benchmark-filter.js benchmark-results.txt
java -jar ../Simulator.jar SetMaxThreads 0
if exist benchmark-statistics.xml del benchmark-statistics.xml

:end