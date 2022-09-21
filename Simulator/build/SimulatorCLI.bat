@echo off

rem Simulator.exe starts the simulator without an attached console and returns immediately.
rem SimulatorCLI.bat will start the simulator with console output. 
rem SimulatorCLI.bat will not return until the simulator is terminated.

for /F "tokens=* USEBACKQ" %%F in (`"%~dp0tools\SimulatorCLIGetJava.exe"`) do (set JavaPath=%%F)
set SimulatorJar="%~dp0Simulator.jar"
"%JavaPath%" -jar %SimulatorJar% %1 %2 %3 %4 %5