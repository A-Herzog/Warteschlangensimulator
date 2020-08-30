@echo off

rem Performs benchmarks runs using the 1,2,... up to the specified number of CPU cores.
rem Results are appended to benchmark-results.txt.

rem Führt einen Benchmark-Läufe unter Verwendung von 1,2,... bis zur angegebenen Anzahl an CPU-Kernen durch.
rem Die Ergebnisse werden an die Datei benchmark-results.txt angehängt.

if exist ../Simulator.jar goto work1
echo English:
echo This file has to be run from its own directory to find the simulator.
echo Deutsch:
echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
echo um den Simulator finden zu können.
goto end

:work1
if not "%1"=="" goto work2
echo English:
echo The maximum number of cores to be used must be specified as a parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als Parameter muss die Maximalanzahl an zu verwendenden Kernen angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:work2
for /L %%G in (1,1,%1) do benchmark.bat %%G

:end