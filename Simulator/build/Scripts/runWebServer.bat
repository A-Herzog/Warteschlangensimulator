@echo off

rem Runs the simulator in web server mode.
rem Simulation requests can be send to the
rem simulator and statistics results can be
rem downloaded or viewed by any web browser.

if exist ../Simulator.jar goto work
echo English:
echo This file has to be run from its own directory to find the simulator.
echo Deutsch:
echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
echo um den Simulator finden zu können.
goto end

:work
java -jar ..\Simulator.jar serverWeb 80

rem The command needs one parameter:
rem The port number to listen on for requests.

rem Alternative for using a fixed model:
rem java -jar .\Simulator.jar serverWebFixed 80 model.xml

:end