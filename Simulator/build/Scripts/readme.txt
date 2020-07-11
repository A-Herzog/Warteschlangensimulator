benchmark-upto:

Performs benchmarks runs using the 1,2,... up to the specified number of CPU cores.
Results are appended to benchmark-result.txt.

Führt einen Benchmark-Läufe unter Verwendung von 1,2,... bis zur angegebenen Anzahl an CPU-Kernen durch.
Die Ergebnisse werden an die Datei benchmark-result.txt angehängt.



benchmark-native-upto:

Uses Graal native image version of simulator (created by compile-native.sh).

Performs benchmarks runs using the 1,2,... up to the specified number of CPU cores.
Results are appended to benchmark-result.txt.

Führt einen Benchmark-Läufe unter Verwendung von 1,2,... bis zur angegebenen Anzahl an CPU-Kernen durch.
Die Ergebnisse werden an die Datei benchmark-result.txt angehängt.



bechmark-multi:

Performs multiple benchmarks runs each using the same specified number of CPU cores.
Results are appended to benchmark-result.txt.

Führt mehrere Benchmark-Läufe unter Verwendung von jeweils derselben angegebenen Anzahl an CPU-Kernen durch.
Die Ergebnisse werden an die Datei benchmark-result.txt angehängt.



variance_analysis:

Performs a variance analysis of the variance_analysis-model.xml model.
For this purpose, the number of client arrivals is successively increased
and for each number 100 simulations are performed. For each number of
client arrivals an output file variance_analysis-resultsX.xlsx is created,
where X stands for the number of arrivals (in millions).

Führt eine Varianzanalyse des Modells variance_analysis-model.xml durch.
Dafür wird die Anzahl an Kundenankünften sukzessive erhöht und für jede
Anzahl werden 100 Simulationen durchgeführt. Für jede Kundenankünfteanzahl
wird eine Ausgabedatei variance_analysis-resultsX.xlsx angelegt, wobei
X für die Anzahl an Ankünften (in Millionen) steht.



benchmark-result.txt:

Per simulation a line continaing the following tabulator separated values is added to the file:
- Used number of threads
- Runtime in seconds (*)
- Number of simulated events per second (*)
- Number of simulated events per thread and second (*)
- Runtime of the fastest thread in seconds (*)
- Average (net) runtime of the threads in seconds (*)
- Runtime of the slowest thread in seconds (*)
- Relative proportion by which the slowest is slower than the fastest (*)
Values marked with (*) use a decimal point or a decimal comma depending
on the country setting detected by Java.

Pro Simulation wird eine Zeile mit den folgenden Werten, die durch Tabulatoren
getrennt werden, ausgegeben:
- Verwendete Anzahl an Threads
- Laufzeit in Sekunden (*)
- Anzahl an simulierten Ereignissen pro Sekunde (*)
- Anzahl an simulierten Ereignissen pro Thread pro Sekunde (*)
- Laufzeit des schnellsten Threads in Sekunden (*)
- Mittlere (netto) Laufzeit der Threads in Sekunden (*)
- Laufzeit des langsamsten Threads in Sekunden (*)
- Relativer Anteil um den der langsamste Thread langsamer als der schnellste ist (*)
Bei den mit (*) markierten Werten wird abhängig von der von Java detektierten
Landeseinstellung ein Dezimalkomma oder ein Dezimalpunkt verwendet.



runCalcServer:

Example script how to use the simulator in simulation server mode.



runWebServer:

Example script how to use the simulator in web server mode.