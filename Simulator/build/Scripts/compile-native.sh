#!/bin/bash

# Compiles simulator to Graal native image.
# This version can be used by benchmark-native-upto.sh.
# Compiling to a native image is only available unter Linux at the moment.

# Übersetzt den Simulator in ein Graal Native-Image.
# Diese Version kann dann von benchmark-native-upto.sh verwendet werden.
# Das Erstellen einer nativen Binärdatei ist momentan nur unter Linux möglich.

../../Graal/bin/native-image -jar ../Simulator.jar --initialize-at-build-time=org.mozilla.javascript,org.mariadb.jdbc