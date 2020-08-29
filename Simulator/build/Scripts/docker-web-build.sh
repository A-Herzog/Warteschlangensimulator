#!/bin/bash

# Builds a docker image for running Warteschlangensimulator
# in web server mode on port 81.

cd ..
docker build -t qs:web -f ./tools/docker-web.txt .

# Run:
# docker run -d -p 81:81 qs:web