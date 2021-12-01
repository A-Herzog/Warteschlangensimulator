#!/usr/bin/python3

"""
This script demonstrates how to connect to socket-based server in Warteschlangensimulator
to submit models to be simulated and to receive results.\n\n
The simulator will be started and stopped automatically.
"""


from socket_connect import QS_full_connect as QS_starter
import os

# Connection settings
java_path = os.environ['JAVA_HOME'] # Setup Java path here
simulator_path = ".\\Simulator" # Setup simulator path here
port = 1000

# Load model to be simulated
with open("model.xml", mode="rb") as file:
    model = file.read()

# Start/stop simulator and submit task
with QS_starter(java_path, simulator_path, port) as socket:
    statistics = socket.run_task(model)

# Save results
with open("statistics.xml", mode="wb") as file:
    file.write(statistics)
