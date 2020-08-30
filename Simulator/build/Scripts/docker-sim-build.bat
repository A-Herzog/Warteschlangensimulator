@echo off

rem Builds a docker image for running Warteschlangensimulator
rem in server mode on port 8183.

if exist ../Simulator.jar cd ..
if exist ./Simulator.jar goto work
echo Simulator.jar not found.
goto end

:work
docker build -t qs:sim -f ./tools/docker-sim.txt .

echo.
echo Run image:
echo docker run -d -p 8183:8183 qs:sim

:end