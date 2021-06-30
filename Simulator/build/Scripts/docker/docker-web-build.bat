@echo off

rem Builds a docker image for running Warteschlangensimulator
rem in web server mode on port 81.

if exist ../../Simulator.jar cd ..
if exist ../Simulator.jar cd ..
if exist ./Simulator.jar goto work
echo Simulator.jar not found.
goto end

:work
docker build -t qs:web -f ./tools/docker/docker-web.txt .

echo.
echo Run image:
echo docker run -d -p 81:81 qs:web

:end