#!/bin/bash

# Runs the simulator in web server mode.
# Simulation requests can be send to the
# simulator and statistics results can be
# downloaded or viewed by any web browser.

java -jar ../Simulator.jar serverWeb 80

# The command needs one parameter:
# The port number to listen on for requests.

# Alternative for using a fixed model:
# java -jar ./Simulator.jar serverWebFixed 80 model.xml