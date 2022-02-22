@echo off

rem Builds a docker image for running Warteschlangensimulator
rem in socket server mode on port 10000.

if exist ../../Simulator.jar cd ..
if exist ../Simulator.jar cd ..
if exist ./Simulator.jar goto work
echo Simulator.jar not found.
goto end

:work
docker build -t qs:socket -f ./tools/docker/docker-socket.txt .

echo.
echo Run image:
echo docker run -d -p 10000:10000 qs:socket

:end