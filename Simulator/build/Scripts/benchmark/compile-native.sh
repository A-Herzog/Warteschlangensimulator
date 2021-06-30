#!/bin/bash

# Compiles simulator to Graal native image.
# This version can be used by benchmark-native-upto.sh.
# Compiling to a native image is only available unter Linux at the moment.

# Übersetzt den Simulator in ein Graal Native-Image.
# Diese Version kann dann von benchmark-native-upto.sh verwendet werden.
# Das Erstellen einer nativen Binärdatei ist momentan nur unter Linux möglich.

if [ -f "../../Simulator.jar" ]
then
	native-image -jar ../../Simulator.jar --initialize-at-build-time=org.mozilla.javascript,org.mariadb.jdbc
else
	echo English:
	echo This file has to be run from its own directory to find the simulator.
	echo Deutsch:
	echo Diese Datei muss von ihrem eigenen Verzeichnis aus gestartet werden,
	echo um den Simulator finden zu können.
fi
