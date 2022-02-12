#!/usr/bin/python3

"""
This script demonstrates how to connect to socket-based server in Warteschlangensimulator
to submit models to be simulated and to receive results.\n\n
The simulator will be started and stopped automatically.
"""


from socket_connect import QS_full_connect as QS_starter
import os

# Connection settings
simulator_path = ".\\Simulator"  # Setup simulator path here

# Load model to be simulated
with open("model.xml", mode="rb") as file:
    model = file.read()

# Start/stop simulator and submit task
with QS_starter(None, simulator_path) as socket:
    statistics = socket.run_task(model)

# Save results
with open("statistics.xml", mode="wb") as file:
    file.write(statistics)
