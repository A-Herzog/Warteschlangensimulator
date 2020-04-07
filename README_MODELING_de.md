# Verfügbare Elementtypen im Warteschlangensimulator

![Übersicht über die verfügbaren Modellelemente](Images/Screenshot_de_stations.png "Übersicht über die verfügbaren Modellelemente")

### Kundenquellen

Jedes Warteschlangenmodell kann aus beliebig vielen Quellen bestehen, an denen die Kunden (bei denen es sich auch um Werkstücke usw. handeln kann) eintreffen.

* Die Kunden können einzelnen oder in Gruppen eintreffen (Batch-Ankünfte).
* Die Ankunftszeitpunkte können über Wahrscheinlichkeitsverteilungen oder Rechenausdrücke definiert werden oder aber auch aus Tabellen oder Datenbanken geladen werden.
* Ankünfte können jedoch auch auf Signale hin, die z.B. von Bedienstationen usw. ausgelöst werden, erfolgen.
* Die Quellen können Kunden verschiedener Typen generieren, die sich später im System auf verschiedenen Pfaden bewegen (z.B. verschidene Modellvarianten in einer Fertigungsstraße) und in der Statistik getrennt ausgewiesen werden.

### Bedienstationen

Bedienstationen sind die zentralen Elemente fast jedes Warteschlangenmodells. An ihnen werden die Kunden durch Bediener bedient. Die Bediener können dabei auch Maschinen oder Werkzeuge sein.

* Die Bedienzeiten können pro Kundentyp variieren und über Verteilungsfunktionen oder Rechenausdrücke definiert sein.
* Vor jeder Bedienstation existiert eine Warteschlange, deren Länge durch andere Stationen abgefragt werden kann, so dass Kunden ggf. erst freigegeben werden, wenn in der jeweiligen Warteschlange wieder Platz vorhanden ist.
* Für die Bedienung eines Kunden können eine oder mehrere Ressourcen (Bediener) notwendig sein. Die vorhanden Ressourcen werden global definiert und können evtl. zwischen verschiedenen Bedienstationen geteilt werden.
* Kunden können einzeln oder in Gruppen bedient werden (Batch-Bedienungen).
* Nach der Bedienung eines Kunden kann es notwendig sein, dass ein Bediener in eine Nachbearbeitungsphase geht. Außerdem können beim Wechsel des Kundentyps an einer Station Rüstzeiten entstehen.
* Kunden verschiedener Typen können verschiedene Prioritäten besitzen. Außerdem kann auch innerhalb eines Kundentyps eine Priorität auf Basis einer Formel definiert werden, so dass vom FIFO-Prinzip abweichende Bedienregeln möglich sind.
* Kunden können eine Ungedulg besitzen und die Station unbedient verlassen, wenn sie zu lange warten mussten.

### Verzweigungen

Eine Standardproblemstellung in vielen Produktionsprozessen, die sich jedoch mit Hilfe klassischer Warteschlangentheorie nur sehr schwer abbilden lässt, ist die Verzweigung von Kunden im Prozess bzw. die Abbildung von Produktionsnetzen.

Im Warteschlangensimulator können Kunden auf Basis verschiedenster Eigenschaften in verschiedene Richtungen verzweigt werden. Die beinhaltet u.a.:

* Verzweigung nach Kundentyp
* Verzweigung gemäß Systemeigenschaft (z.B. zu dem momentan am wenigsten ausgelasteten Teilsystem)
* Gleichmäßige Auslastung mehrerer Teilsysteme (d.h. zufällige Verzweigung gemäß vorgegebener Raten)

### Schranken

Häufig dürfen Kunden bestimmte Bereiche nur dann betreten, wenn bestimmte Bedingungen erfüllt sind. Ein eifnaches Beispiel stellt die Pull-Produktion dar: Erst wenn der Bestand an einer Folgestation einen Schwellenwert unterschritten hat, darf die Ausgangsstation den nächsten Kunden bzw. das nächste Werkstück liefern. Diese und ähnliche Regeln lassen sich mit Hilfe von Schranken, Signalen und Zählern auf flexible Weise im Warteschlangensimulator abbilden. 

### Batch-Verarbeitung

* Vielfach treffen die Kunden in Gruppen in einem System ein (z.B. in Form von Bauteilen, die per LKW angeliefert werden) oder aber Maschinen können Bauteile in Gruppen bedienen.
* Auch kann es notwendig sein, von einer Station zur nächsten Gruppen dynamisch zu bilden oder Gruppen wieder aufzulösen.
* In vielen Produktionsprozessen existieren Stationen, in denen bestimmte Fertigungsstränge zusammenlaufen (z.B. Motoren mit Karosserien zusammengeführt werden). Auch derartige Prozesse - und die damit einher gehenden Probleme - lassen sich im Warteschlangensimulator untersuchen.

### Transporte

Werden Werkstücke oder Kunden mit Hilfe von Transportressourcen von einer Station zu einer anderen transportiert, so ist dies nicht nur mit einem Zeitaufwand verbunden, sondern es werden Ressourcen für den Transport benötigt und die Transportzeiten können auch von der jeweiligen Strecke abhängen. Des Weiteren existieren häufig Restriktionen in Bezug auf die Transportkapazitäten usw. All diese Fälle lassen sich im Warteschlangensimulator abbilden.

### Zeitkontinuierliche Werte

In der klassischen ereignisorientierten Simulation treten keine Zeitkontinuierlichen Prozesse auf: Alle Änderungen am System treten sprunghaft jeweils zu bestimmten Zeitpunkten ein. (Erst ist kein Kunde im System, dann trifft ein Kunde ein und befindet sich im System. Einen fließenden Übergang zwischen 0 und einem Kunden existiert nicht.) Dennoch ist es für einige Fragestellungen von Bedeutung, Werte, die sich kontinuierlich über die Zeit verändern (z.B. Füllstände von Tanks), abbilden zu können. Der Warteschlangensimulator besitzt daher eine Reihe von Stationstypen, mit deren Hilfe genau solche Prozesse abgebildet werden können. 

### Transientes Verhalten

In der klassischen Warteschlangentheorie werden Prozesse im stationären Zustand, d.h. nach langer Laufzeit, betrachtet. Für diese Fälle werden die Zwischenankunftszeiten der Kunden über Wahrscheinlichkeitsverteilungen definiert. Häufig soll jedoch ein System zu einem bestimmten Zeitpunkt betrachtet werden und häufig sind Zeitspannen usw. über konkrete Werte (z.B. Ankunftszeitpunkte der Kunden) definiert oder aber die Rahmenparameter des Systems (z.B. Anzahl an jeweils verfügbaren Bedienern) ändern sich an bestimmten Zeitpunkten (z.B. bedingt durch einen Schichtplan).

Mit Mitteln der analytischen Warteschlangentheorie lassen sich solche Modelle nur sehr schwer oder gar nicht abbilden. Der Warteschlangensimulator bringt hingegen Umfangreiche Funktionen zur Modellierung der transienten Phase mit:

* Schichtpläne für Ressourcen
* Ausfälle von Ressourcen nach bestimmten Zeiten oder Anzahlen von bedienten Kunden (oder auch zufällig)
* Definition von Ankünften usw. auf Basis von Tabellen oder Datenbanken (mit historischen Daten)
* Erfassung der Veränderung von Werten im zeitlichen Verlauf in der Statistik

### Dokumentation

* [Übersicht über alle Stationstypen](Simulator/build/Help/Reference/de/Warteschlangensimulator-Reference-de.pdf) (pdf)