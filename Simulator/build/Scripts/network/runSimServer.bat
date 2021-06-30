@echo off

rem Runs the simulator in compute server mode.
rem Other instances will be able to outsource
rem simulation tasks to this server.

if exist ..\..\Simulator.jar goto work
echo English:
echo This file has to be run from its own directory to find the simulator.
echo Deutsch:
echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
echo um den Simulator finden zu können.
goto end

:work
java -jar ..\..\Simulator.jar server 8183

rem Parameters (all optional):
rem 1. Port number to listen on for requests.
rem 2. Password

:end