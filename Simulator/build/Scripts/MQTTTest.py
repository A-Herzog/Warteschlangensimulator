#
# This script demonstrates how to connect to MQTT client in Warteschlangensimulator
# to submit models to be simulated and to receive results.
#

import paho.mqtt.client as mqtt
import paho.mqtt.packettypes as mqttpackettypes
import time
import io
import ssl

# Load model to be simulated via MQTT
f=io.open("model.json",mode="r",encoding="utf-8")
model=f.read()
f.close()

# Callback for connection errors
done=False
def on_connect(client, userdata, flags, rc, properties):
    if (rc!=0):
        print("Connected flags ",str(flags),"result code ",str(rc))
        global done
        done=True

# Callback for processing response
def on_message(client, userdata, message):
    f=io.open("statistics.xml",mode="wb") # Response file format defined via simulator configuration (can be xml, json, zip or tar.gz)
    f.write(message.payload)
    f.close()
    print("Result saved")

    if hasattr(message.properties,'UserProperty'):
        print("User properties submitted together with work request:")
        for property in message.properties.UserProperty:
            print(property)

    global done
    done=True

# Init MQTT client
client=mqtt.Client("Python test client",protocol=mqtt.MQTTv5)
# optional login data: client.username_pw_set("username","password")
client.on_message=on_message
client.on_connect=on_connect
# optional configure transport level security: client.tls_set(cert_reqs=mqtt.ssl.CERT_NONE,tls_version=mqtt.ssl.PROTOCOL_TLSv1_2)
client.connect("localhost",1883) # or 8883 when in transport level security mode
client.loop_start()
client.subscribe("Python/response",qos=2)

# Send model
properties=mqtt.Properties(mqttpackettypes.PacketTypes.PUBLISH)
properties.ResponseTopic="Python/response"
properties.UserProperty=("a","2") # User properties can be used to identify response (Warteschlangensimulator will echo them in response message)
properties.UserProperty=("c","3")
client.publish("Warteschlangensimulator/task",payload=model,qos=2,properties=properties)
print("Submitted work request together with response topic name and user properties")

# Wait
while not done:
    time.sleep(1)

# Disconnect
client.disconnect()
client.loop_stop()