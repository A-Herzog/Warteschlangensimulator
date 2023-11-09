@echo off

Rem Compiles the example plugin classes.

Rem You do not need to use this script. You can also
Rem compile the java files by clicking "Compile" in the
Rem plugin folder configuation dialog.

javac --release="11" -cp . .\*.java