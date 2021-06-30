@echo off

rem Activates NUMA mode in Warteschlangensimulator.

rem Aktiviert den NUMA-Modus im Warteschlangensimulator.

if exist ../../Simulator.jar goto work
echo English:
echo This file has to be run from its own directory to find the simulator.
echo Deutsch:
echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
echo um den Simulator finden zu können.
goto end

: work
java -jar ../../Simulator.jar SetNUMA 1

:end