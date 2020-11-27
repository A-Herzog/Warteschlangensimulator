#!/bin/bash

# Compiles the example plugin classes

javac -source 1.8 -target 1.8 -cp . ./scripting/java/RuntimeInterface.java
javac -source 1.8 -target 1.8 -cp . ./scripting/java/SystemInterface.java
javac -source 1.8 -target 1.8 -cp . ./ExampleClass.java