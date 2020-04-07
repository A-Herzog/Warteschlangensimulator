cd ..
cd target
call javaw -XX:+FlightRecorder -XX:FlightRecorderOptions=stackdepth=256 -jar Simulator.jar