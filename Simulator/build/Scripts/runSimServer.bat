@echo off

rem Runs the simulator in compute server mode.
rem Other instances will be able to outsource
rem simulation tasks to this server.

java -jar ..\Simulator.jar server 1234

rem Parameters (all optional):
rem 1. Port number to listen on for requests.
rem 2. Password