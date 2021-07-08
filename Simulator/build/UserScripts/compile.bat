@echo off

Rem Compiles the example plugin classes

Rem You do not need to use this script. You can also
Rem compile the java files by clicking "Compile" in the
Rem plugin folder configuation dialog.

Rem The release option was introduced in Java 9.
Rem If you use a Java 8 JDK, remove the '--release="8"' parameter.

javac --release="8" -cp . .\*.java