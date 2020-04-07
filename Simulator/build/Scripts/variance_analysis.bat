@echo off

rem Führt eine Varianzanalyse des Modells variance_analysis-model.xml durch.
rem Dafür wird die Anzahl an Kundenankünften sukzessive erhöht und für jede
rem Anzahl werden 100 Simulationen durchgeführt. Für jede Kundenankünfteanzahl
rem wird eine Ausgabedatei variance_analysis-resultsX.xlsx angelegt, wobei
rem X für die Anzahl an Ankünften (in Millionen) steht.

rem Performs a variance analysis of the variance_analysis-model.xml model.
rem For this purpose, the number of client arrivals is successively increased
rem and for each number 100 simulations are performed. For each number of
rem client arrivals an output file variance_analysis-resultsX.xlsx is created,
rem where X stands for the number of arrivals (in millions).

if "%1"=="" goto error1
if "%2"=="" goto error2
if "%3"=="" goto error3
goto work

:error1
echo English:
echo The initial number of arrivals (in millions) must be specified as first parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als erster Parameter muss der Startwert für die Anzahl an Ankünften (in Millionen) angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:error2
echo English:
echo The step wide for increasing the number of arrivals (in millions) must be specified as second parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als zweiter Parameter muss die Schrittweite für die Erhöhung der Anzahl an Ankünften (in Millionen) angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:error3
echo English:
echo The maximum number of arrivals (in millions) must be specified as third parameter.
echo An integer number greater than or equal to 1 must be specified.
echo Deutsch:
echo Als dritter Parameter muss die Maximalanzahl an Ankünften (in Millionen) angegeben werden.
echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
goto end

:work
del variance_analysis-series.xml
del variance_analysis-results.xml
for /L %%G in (%1,%2,%3) do (
	java -jar ../Simulator.jar ParameterreiheVarianzanalyse variance_analysis-model.xml variance_analysis-series.xml 100 %%G000000
	java -jar ../Simulator.jar Parameterreihe variance_analysis-series.xml variance_analysis-results.xml
	java -jar ../Simulator.jar ParameterreiheTabelle variance_analysis-results.xml variance_analysis-results%%G.xlsx
	del variance_analysis-series.xml
	del variance_analysis-results.xml
)

:end