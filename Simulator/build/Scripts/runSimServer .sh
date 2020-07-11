#!/bin/bash

# Runs the simulator in compute server mode.
# Other instances will be able to outsource
# simulation tasks to this server.

java -jar ../Simulator.jar server 1234

# Parameters (all optional):
# 1. Port number to listen on for requests.
# 2. Password