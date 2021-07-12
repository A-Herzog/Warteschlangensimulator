#!/bin/bash

# Compiles the example plugin classes.

# You do not need to use this script. You can also
# compile the java files by clicking "Compile" in the
# plugin folder configuation dialog.

# The release option was introduced in Java 9.
# If you use a Java 8 JDK, remove the '--release="8"' parameter.

if [ -z "$JAVA_HOME" ]
then
	JAVAC_RUN="javac"
else
	JAVAC_RUN="${JAVA_HOME}/bin/javac"
fi

${JAVAC_RUN} --release="8" -cp . ./scripting/java/ClientsInterface.java
${JAVAC_RUN} --release="8" -cp . ./scripting/java/RuntimeInterface.java
${JAVAC_RUN} --release="8" -cp . ./scripting/java/SystemInterface.java
${JAVAC_RUN} --release="8" -cp . ./ExampleClass.java