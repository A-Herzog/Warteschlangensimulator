@echo off

rem Performs a single benchmark run using the specified number of CPU cores.
rem Results are appended to benchmark-result.txt.

rem Führt einen einzelnen Benchmark-Lauf unter Verwendung der angegebenen Anzahl an CPU-Kernen durch.
rem Die Ergebnisse werden an die Datei benchmark-result.txt angehängt.

if "%1"=="" goto error
goto work

:error
echo English:
echo The number of cores to be used must be specified as a parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als Parameter muss die Anzahl an zu verwendenden Kernen angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:work
del benchmark-statistics.xml
java -jar ../Simulator.jar SetMaxThreads %1
java -jar ../Simulator.jar Simulation benchmark-model.xml benchmark-statistics.xml
java -jar ../Simulator.jar Filter benchmark-statistics.xml benchmark-filter.js benchmark-result.txt
java -jar ../Simulator.jar SetMaxThreads 0
del benchmark-statistics.xml

:end