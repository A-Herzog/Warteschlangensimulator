#!/bin/bash

# Führt eine Varianzanalyse des Modells variance_analysis-model.xml durch.
# Dafür wird die Anzahl an Kundenankünften sukzessive erhöht und für jede
# Anzahl werden 100 Simulationen durchgeführt. Für jede Kundenankünfteanzahl
# wird eine Ausgabedatei variance_analysis-resultsX.xlsx angelegt, wobei
# X für die Anzahl an Ankünften (in Millionen) steht.

# Performs a variance analysis of the variance_analysis-model.xml model.
# For this purpose, the number of client arrivals is successively increased
# and for each number 100 simulations are performed. For each number of
# client arrivals an output file variance_analysis-resultsX.xlsx is created,
# where X stands for the number of arrivals (in millions).

if [ "$1" == "" ]
then
	echo English:
	echo The initial number of arrivals (in millions) must be specified as first parameter.
	echo An integer number greater than or equal to 1 must be specified.
	echo Deutsch:
	echo Als erster Parameter muss der Startwert für die Anzahl an Ankünften (in Millionen) angegeben werden.
	echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
else
	if [ "$2" == "" ]
	then
		echo English:
		echo The step wide for increasing the number of arrivals (in millions) must be specified as second parameter.
		echo An integer number greater than or equal to 1 must be specified.
		echo Deutsch:
		echo Als zweiter Parameter muss die Schrittweite für die Erhöhung der Anzahl an Ankünften (in Millionen) angegeben werden.
		echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
	else
		if [ "$2" == "" ]
		then
			echo English:
			echo The maximum number of arrivals (in millions) must be specified as third parameter.
			echo An integer number greater than or equal to 1 must be specified.
			echo Deutsch:
			echo Als dritter Parameter muss die Maximalanzahl an Ankünften (in Millionen) angegeben werden.
			echo Es muss eine Ganzzahl größer oder gleich 1 angegeben werden.
		else
			rm variance_analysis-series.xml
			rm variance_analysis-results.xml
			for ((x=5;x<=100;x+=5))
			do	
				java -jar ../Simulator.jar ParameterreiheVarianzanalyse variance_analysis-model.xml variance_analysis-series.xml 100 ${x}000000
				java -jar ../Simulator.jar Parameterreihe variance_analysis-series.xml variance_analysis-results.xml
				java -jar ../Simulator.jar ParameterreiheTabelle variance_analysis-results.xml variance_analysis-results${x}.xlsx
				rm variance_analysis-series.xml
				rm variance_analysis-results.xml
			done
		fi
	fi
fi