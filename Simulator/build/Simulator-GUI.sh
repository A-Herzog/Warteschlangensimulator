#!/bin/bash

# Runs the simulator and passes the command line arguments to the jar.

if [ -z "$JAVA_HOME" ]
then
	JAVA_RUN="java"
else
	JAVA_RUN="${JAVA_HOME}/bin/java"
fi

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

if [ -f "${DIR}/Simulator.jar" ]
then
	${JAVA_RUN} -splash:libs/simulator.png -jar ${DIR}/Simulator.jar $1 $2 $3 $4 $5
else
	echo Cannot find Simulator.jar.
fi
