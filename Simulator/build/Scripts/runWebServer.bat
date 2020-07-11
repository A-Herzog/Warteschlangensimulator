@echo off

rem Runs the simulator in web server mode.
rem Simulation requests can be send to the
rem simulator and statistics results can be
rem downloaded or viewed by any web browser.

java -jar ..\Simulator.jar serverWeb 80

rem The command needs one parameter:
rem The port number to listen on for requests.

rem Alternative for using a fixed model:
rem java -jar .\Simulator.jar serverWebFixed 80 model.xml