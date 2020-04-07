@echo off

rem Performs benchmarks runs using the 1,2,... up to the specified number of CPU cores.
rem Results are appended to benchmark-result.txt.

rem Führt einen Benchmark-Läufe unter Verwendung von 1,2,... bis zur angegebenen Anzahl an CPU-Kernen durch.
rem Die Ergebnisse werden an die Datei benchmark-result.txt angehängt.

if "%1"=="" goto error
goto work

:error
echo English:
echo The maximum number of cores to be used must be specified as a parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als Parameter muss die Maximalanzahl an zu verwendenden Kernen angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:work
for /L %%G in (1,1,%1) do benchmark.bat %%G

:end