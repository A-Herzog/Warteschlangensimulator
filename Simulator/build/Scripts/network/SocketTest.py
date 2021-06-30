#
# This script demonstrates how to connect to socket-based server in Warteschlangensimulator
# to submit models to be simulated and to receive results.
#

#
# Command to run Warteschlangensimulator as socket client:
# java -jar ./Simulator.jar serverSocket <portNumber>
#

import socket
import io

#  Connection settings
HOST="127.0.0.1"
PORT=1000

# Load model to be simulated
f=io.open("model.xml",mode="rb")
model=f.read()
f.close()



#
# Helper functions for sending data to and receiving data from simulator
#

# Send bytes to simulator
def sendBytes(bytes, socket):
    socket.sendall(len(bytes).to_bytes(4,"big"))
    socket.sendall(bytes)

# Send a string to simulator
def sendString(text, socket):
    sendBytes(bytearray(text,'utf-8'),socket)

# Receive bytes from simulator
def receiveBytes(socket):
    size=int.from_bytes(socket.recv(4),"big")
    chunks=[]
    read=0
    while (read<size):
        chunk=socket.recv(min(size-read,2048))
        if (len(chunk)==0):
            raise RuntimeError("Connection broken")
        chunks.append(chunk)
        read+=len(chunk)
    return b''.join(chunks)

# Receive a string from simulator
def receiveString(socket):
    return str(receiveBytes(socket),'utf-8')

# Send a simulation task to simulator
def sendTask(taskData, socket):
    sendString("Task",socket)

    if (isinstance(taskData, (bytes, bytearray))):
        sendBytes(taskData,socket)

    if (isinstance(taskData, (str))):
        sendString(taskData,socket)

# Request processing status from Simulator
def requestStatus(socket):
    sendString("Status",socket)

# Request results for a specific task from simulator
def getResults(taskId, socket):
    sendString("Result",socket)
    sendString(str(taskId),socket)

# Answer types
MSG_TYPE_ERROR="ERROR"
MSG_TYPE_MESSAGE="MESSAGE"
MSG_TYPE_TASKID="TASKID"
MSG_TYPE_RESULT="RESULT"

# Output anser from simulator
def processAnswer(socket, saveResults=None):
    answerType=receiveString(socket)

    if (answerType==MSG_TYPE_ERROR):
        print("An error occured while processing the request:")
        print(receiveString(socket))
        return

    if (answerType==MSG_TYPE_MESSAGE):
        print("Answer from Server:")
        print(receiveString(socket))
        return

    if (answerType==MSG_TYPE_TASKID):
        print("Server accepted the simulation task. ID of the new task:")
        print(receiveString(socket))
        return

    if (answerType==MSG_TYPE_RESULT):
        print("Simulation result data:")
        if (saveResults==None):
            print("")
            for line in receiveString(socket).splitlines():
                print(line)
        else:
            print("Saving result in ",saveResults)
            f=io.open(saveResults,mode="wb")
            f.write(receiveBytes(socket))
            f.close()
        return



#
# Communicate with Warteschlangensimulator via socket connection
#

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as socket:
    try:
        socket.connect((HOST,PORT))
    except ConnectionRefusedError as err:
        print("Cannot connect to server:")
        print(err)
        exit()

    try:
        # Only uncomment one of the following three options

        # Send a model for simulation to the simulator
        # sendTask(model,socket)

        # Request status from simulator
        requestStatus(socket)

        # Requests results of a simulation
        # getResults(1,socket)

        # For all three options: Show answer from simulator
        processAnswer(socket,"result.xml")

    finally:
        socket.close()
