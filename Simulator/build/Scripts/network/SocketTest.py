#!/usr/bin/python3

"""
This script demonstrates how to connect to socket-based server in Warteschlangensimulator
to submit models to be simulated and to receive results.\n\n
Command to run Warteschlangensimulator as socket client:\n
java -jar ./Simulator.jar serverSocket <portNumber>
"""


from socket_connect import QS_socket_connect as QS
import io

#  Connection settings
host = "127.0.0.1"
port = 1000

# Load model to be simulated
with open("model.xml", mode="rb") as file:
    model = file.read()

# Communicate with Warteschlangensimulator via socket connection
socket=QS(host, port)
statistics = socket.run_task(model)

# Save results
with open("statistics.xml", mode="wb") as file:
    file.write(statistics)
