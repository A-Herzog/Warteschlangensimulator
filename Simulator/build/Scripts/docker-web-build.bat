@echo off

rem Builds a docker image for running Warteschlangensimulator
rem in web server mode on port 81.

cd ..
docker build -t qs:web -f ./tools/docker-web.txt .

echo.
echo Run image:
echo docker run -d -p 81:81 qs:web