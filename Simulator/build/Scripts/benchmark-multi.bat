@echo off

rem Performs multiple benchmarks runs each using the same specified number of CPU cores.
rem Results are appended to benchmark-result.txt.

rem Führt mehrere Benchmark-Läufe unter Verwendung von jeweils derselben angegebenen Anzahl an CPU-Kernen durch.
rem Die Ergebnisse werden an die Datei benchmark-result.txt angehängt.

if "%1"=="" goto error1
if "%2"=="" goto error2
goto work

:error1
echo English:
echo The number of cores to be used must be specified as first parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als erster Parameter muss die Anzahl an zu verwendenden Kernen angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:error2
echo English:
echo The number of repetitions must be specified as second parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als zweiter Parameter muss die Anzahl an Wiederholungen angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.

:work
for /L %%G in (1,1,%2) do benchmark.bat %1

:end