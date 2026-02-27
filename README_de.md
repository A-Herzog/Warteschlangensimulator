# Warteschlangensimulator

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/a-herzog/warteschlangensimulator)](https://github.com/A-Herzog/Warteschlangensimulator/releases/latest/)
![GitHub](https://img.shields.io/github/license/a-herzog/warteschlangensimulator)
[![GitHub All Releases](https://img.shields.io/github/downloads/a-herzog/warteschlangensimulator/total)](https://github.com/A-Herzog/Warteschlangensimulator/releases/latest/)
[![DOI](https://a-herzog.github.io/Warteschlangensimulator/zenodo.12606913.svg)](https://doi.org/10.5281/zenodo.12606913)

([English version of this readme.](README_en.md))

Der Warteschlangensimulator ist ein freier, plattformunabhängiger, [ereignisorientierter, stochastischer Simulator](https://en.wikipedia.org/wiki/Discrete-event_simulation "Wikipedia").

Im Simulator können Warteschlangenmodelle in Form von Fließbildern modelliert und anschließend simuliert werden. Während der Simulation kann optional eine Animation der Prozesse in dem Modell dargestellt werden. Im Anschluss an eine Simulation stehen eine Vielzahl an Kenngrößen für die statistische Auswertung zur Verfügung. Der Simulator kann sowohl interaktiv als auch im Kommandozeilenbetrieb verwendet werden. Auch stehen Funktionen zur automatisierten Durchführung von Parameterstudien zur Verfügung.

## Download

* **[Windows-Installer](https://github.com/A-Herzog/Warteschlangensimulator/releases/latest/download/SimulatorSetup.exe)**
* **[Zip-Datei](https://github.com/A-Herzog/Warteschlangensimulator/releases/latest/download/Simulator.zip)**
* **[Linux AppImage](https://github.com/A-Herzog/Warteschlangensimulator/releases/latest/download/Warteschlangensimulator-x86_64.AppImage)**
* [Quellcode](https://github.com/A-Herzog/Warteschlangensimulator/releases/latest/) ([Hinweise zum Build-Prozess](BUILD.md))

Die Windows-Version kann sowohl mit als auch ohne Admin-Rechte installiert werden. Die zip-Datei-Variante kann auf jeder Plattform ohne Installation verwendet werden.

* [Versionshinweise](https://github.com/A-Herzog/Warteschlangensimulator/wiki/Release-notes "Versionshinweise und Pläne für die nächsten Versionen im GitHub-Wiki")
* Systemvoraussetzung: [Java 11 oder höher](https://adoptium.net/ "Java von adoptopenjdk.net herunterladen") (Die neuste Java-Version wird vollständig unterstützt.)
* Lizenz: [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) (für den Warteschlangensimulator, die verwendeten Bibliotheken unterliegen zum Teil abweichenden Opensource-Lizenzen)

## Wesentliche Eigenschaften

🌏 Großes Modellierungsvermögen (>100 verschiedene Stationstypen)

🚀 Schnelle Simulation (>100.000 Ankünfte/Sekunde/Kern, mehrkernfähig)

🎬 Animation der Modelle möglich (inkl. optionaler Aufzeichnung als Video)

✅ Statistikerfassung (automatische Erfassung aller Kenngrößen)

💻 Flexibel einsetzbar (grafische Oberfläche oder Servermodus, plattformunabhängig)

❓ Unterstützung (Online-Hilfe zu allen Stationen und Erklärungen in der Statistik, interaktives Tutorial)

💬 Sprachen: Deutsch und Englisch verfügbar

⭐ Freie Software

![Screenshot](Images/Screenshot_de.png)

## Hilfe und Unterstützung

* Der Warteschlangensimulator verfügt über eine integrierte Online-Hilfe, diverse Beispielmodelle und ein interaktives Tutorial.
* [Kurzeinführung Warteschlangensimulator](https://a-herzog.github.io/Warteschlangensimulator/Warteschlangensimulator-de.pdf) (pdf)
* [Tutorial in Bildern](https://a-herzog.github.io/Warteschlangensimulator/Warteschlangensimulator-Tutorial-de.pdf) (pdf)																														  
* [Wiki zum Warteschlangensimulator](https://github.com/A-Herzog/Warteschlangensimulator/wiki) (Englisch)
* [**Präsentation** zur Vorstellung des Warteschlangensimulator](https://a-herzog.github.io/Warteschlangensimulator/IntroducingWarteschlangensimulator.pdf) (Englisch, pdf)
* [**Videos** zur Vorstellung des Warteschlangensimulator](https://github.com/A-Herzog/Warteschlangensimulator/wiki/Videos) (Englisch)
* [Feature-Übersicht **Präsentation**](https://a-herzog.github.io/Warteschlangensimulator/Warteschlangensimulator-Features.pdf) (pdf)
* [**Buch**: "Simulation mit dem Warteschlangensimulator"](https://www.springer.com/gp/book/9783658346676)
* Auf dem Laufenden bleiben: Sie können diesem Projekt auf GitHub folgen, um stets über neue Versionen usw. benachrichtigt zu werden.

---

## Anwendungsgebiete

In allen Produktions- und Logistikprozessen, in denen **Unsicherheiten** auftreten (unbekannte Ankunftszeiten der Kunden, variable Bedienzeiten, Ausfälle von Ressourcen ...) treten Wartezeiten auf. Je bessere diese Zusammenhänge verstanden sind, desto besser lassen sich unnötige Wartezeiten vermeiden. Wird nur mit Mittelwerten gerechnet, so lassen sich diese Probleme nicht erkennen. Bezogen auf die mittlere Bediendauer reicht die verfügbare Bedienleistung an den Supermarktkassen meist aus - und trotzdem entstehen häufig unerfreuliche Wartezeiten.

Besteht ein Bedienprozess nicht nur aus einer Station, sondern aus einem ganzen **Netz** inkl. Rückkopplungen und komplexen Verzweigungsregeln - was in vielen Produktionsprozessen den Normalfall darstellt - ist nur auf Basis von statischen Rechnungen überhaupt nicht mehr erkennbar, welche Stationen die eigentlichen Engpässe darstellen.
Mit Hilfe eines Simulationsmodells lassen sich die relevanten Eigenschaften eines realen Produktions- oder Logistikprozesses abbilden und am Computer simulieren. Dies beinhaltet sowohl stochastische Bedienzeiten als auch Verzweigungen und vernetzte Prozesse, komplexe Regeln zur Steuerung der Freigabe von Werkstücken, Ressourcen mit variabler Verfügbarkeit (Schichtpläne und auch unerwartete Ausfälle) und Abhängigkeiten zwischen verschiedenen Stationen.

Änderungen oder Erweiterungen an einem realen Produktionssystem vorzunehmen, ist meist mit hohen Risiken und Kosten verbunden. Mit Hilfe eines Simulationsmodells lassen sich die Auswirkungen von Änderungen an einem System gefahrlos und untersuchen und es lässt sich so eine optimale Steuerungsstrategie ermitteln.

**Zielgruppen**

* 🧬 **Forschung** (Erstellung komplexer Modelle, Erfassung aller in der Warteschlangentheorie üblichen Kenngrößen, Automatisierung von Parameterstudien, ...)
* 🏫 **Lehre** (Unterstützung bei der Erstellung einfacher Modelle, einfacher Vergleich von Simulations- und analytischen Ergebnissen, Hilfetexte zu allen Statistikausgaben)
* 🏭 **Unternehmenseinsatz** (Plattformunabhängigkeit, Verwendung von Standard-Dateiformaten, kommandozeilensteuerbar, Festlegung von Vorlagen für Reporting-Ausgaben)

---

## Features

Der Warteschlangensimulator ermöglicht die Modellierung, Simulation und Optimierung beliebiger Fragestellungen ausdem Bereich der Produktionsplanung. Insbesondere Probleme im Bereich von Produktions- und Logistiksystemen lassen sich mit Hilfe des Warteschlangensimulators sehr gut analysieren. 

#### Modellierung von Warteschlangensystemen in Form von Fließbildern
  
[<img src="Images/Screenshot_de_flowchart.png" alt="Warteschlangensimulator - Modellierung" style="float: right; max-width: 30%; padding-left: 5px;">](Images/Screenshot_de_flowchart.png "Anklicken für große Ansicht")  
Komplexe Produktions- und Logistikprozesse können im Warteschlangensimulator auf einfache Weise in Form von Fließbildern als Simulationsmodelle abgebildet werden.
<br clear="both">

#### Animation

[<img src="Images/Screenshot_de_animation.png" alt="Warteschlangensimulator - Animation" style="float: right; max-width: 30%; padding-left: 5px;">](Images/Screenshot_de_animation.png "Anklicken für große Ansicht")
Alle als Fließbild angelegten Warteschlangenmodelle können als Animation dargestellt werden. Während der Animation kann die Entwicklung von bestimmten Werten live verfolgt werden (sowohl in Form von Textwerten als auch in Form von laufend aktualisierten Diagrammen). Über bestimmte Stationstypen kann während einer laufenden Simulation direkt in die Steuerung eingegriffen werden.
<br clear="both">

#### Statistikaufzeichnung

[<img src="Images/Screenshot_de_statistics.png" alt="Warteschlangensimulator - Statistik" style="float: right; max-width: 30%; padding-left: 5px;">](Images/Screenshot_de_statistics.png "Anklicken für große Ansicht")  
Während der Simulation eines Modells werden automatisch alle relevanten Kenngrößen aufgezeichnet und stehen nach Abschluss der Simulation zur Auswertung zur Verfügung. Zur einfachen Weiterverarbeitung der Daten setzt der Warteschlangensimulator auf offene Dateiformate, kann aber z.B. Tabellen auch in Formaten für übliche Tabellenkalkulationen exportieren.
<br clear="both">

#### Automatisierung von Untersuchungen

[<img src="Images/Screenshot_de_parameterseries.png" alt="Warteschlangensimulator - Parameterstudien" style="float: right; max-width: 30%; padding-left: 5px;">](Images/Screenshot_de_parameterseries.png "Anklicken für große Ansicht")
Zur Untersuchungen der Auswirkungen von Veränderungen einzelner Parameter können diese automatisiert in einem bestimmten Bereich variiert werden und die Simulationsergebnisse in Tabellen- oder Grafikform dargestellt werden.
<br clear="both">

#### Netze und komplexe Steuerungsregeln

[<img src="Images/Screenshot_de_net.png" alt="Warteschlangensimulator - Warteschlangennetzwerke" style="float: right; max-width: 30%; padding-left: 5px;">](Images/Screenshot_de_net.png "Anklicken für große Ansicht")
Verzweigungen und Schranken können über komplexe Regeln, die weit über die Möglichkeiten analytischer Modelle hinausgehen, abgebildet werden. Es können dabei sogar nutzerdefinierte Skripte zur Vetzögerung oder Verzweigung von Kunden verwendet werden.
<br clear="both"> 
  
---
  
## Modellierungsmöglichkeiten

Im Warteschlangensimulator stehen 100 verschiedene Vorlagenelemente zur Erstellung der Warteschlangenmodelle zur Verfügung.

([Detailliertere Beschreibung der verfügbaren Elemente](README_MODELING_de.md))

![Übersicht über die verfügbaren Modellelemente](Images/Screenshot_de_stations.png "Übersicht über die verfügbaren Modellelemente")

---

## Automatisierung von Simulationen

In vielen Fällen interessiert man sich nicht nur für die Simulationsergebnisse zu einem Warteschlangenmodell, sondern möchte untersuchen, welche quantitativen Auswirkungen die Veränderung eines Parameters auf die Kenngrößen des Systems hat. Dies kann zwei mögliche Gründe haben:

* Es soll ein Verständnis für die Wirkzusammenhänge innerhalb des Systems gewonnen werden.
* Es soll eine Kenngröße (z.B. der Gesamtertrag des Systems) optimiert werden.

In beiden Fällen sind viele Simulationen eines jeweils leicht variierten Modells notwendig.

#### Parameterreihen

[<img src="Images/Screenshot_de_parameterseries.png" alt="Warteschlangensimulator - Parameterstudien" style="float: right; max-width: 30%; padding-left: 5px;">](Images/Screenshot_de_parameterseries.png "Anklicken für große Ansicht")

Sollen lediglich ein oder mehrere Parameter variiert werden und jeweils die Ergebnisse aufgezeichnet werden, so kann dies mit Hilfe der Parameterreihenfunktion automatisiert erfolgen: Es müssen jeweils nur die Eingabeparameter und ihre jeweiligen Variationsbereiche definiert werden und es muss festgelegt werden, welche Kenngrößen jeweils aufgezeichnet werden sollen. Den Rest führt der Warteschlangensimulator dann selbstständig durch. Als Ergebnis wird eine Tabelle, in der die Eingabeparameter den jeweiligen Kenngrößen gegenübergestellt werden, geliefert. Diese kann z.B. in einer Tabellenkalkulation weiterverarbeitet werden - aber kann auch direkt im Warteschlangensimulator als Diagramm visualisiert werden. Was-wäre-wenn-Fragestellungen lassen sich auf diese Weise sehr konfortabel, mit wenig Aufwand für den Anwender und vor allem auch sehr schnell beantworten.

#### Optimierung

Ist bereits bekannt, welche Kenngröße optimiert werden soll und durch die Variation welcher Parameter in welchem Bereich dies erfolgen soll, so kann der in den Warteschlangensimulator eingebaute Optimierer verwendet werden. Neben den klassischen Methoden zur Variation der Parameter stehen hier auch **genetische Algorithmen** zur Verfügung.

#### Client-Server-Betrieb

Der Warteschlangensimulator kann auf einem leistungsstarken System im Servermodus gestartet werden. Wird eine Client-Installation mit diesem Server **verknüpft**, so erfolgen alle Simulationen vollkommen transparent auf dem Server.

Alternativ können Parameterreihenkonfigurationen gespeichert und dann per **Kommandozeile** auf einem Rechenserver ohne jegliche grafische Oberfläche ausgeführt werden.

## Buch: "Simulation mit dem Warteschlangensimulator"

[<img src="Images/CoverHerzogSimulationMitDemWarteschlangensimulator_small.png" alt="Buch: Simulation mit dem Warteschlangensimulator" style="float: right; max-width: 30%; padding-left: 5px">](https://www.springer.com/gp/book/9783658346676)

[A. Herzog: "Simulation mit dem Warteschlangensimulator"](https://www.springer.com/gp/book/9783658346676)

"Simulation mit dem Warteschlangensimulator" mit BibTeX zitieren:

    @book{herzog2021simulation,
      title={Simulation mit dem Warteschlangensimulator},
      author={Herzog, Alexander},
      year={2021},
      publisher={Springer},
      location={Wiesbaden},
      doi={10.1007/978-3-658-34668-3}
    }
	
Die im Lehrbuch vorgestellten Beispiele sind auch als Warteschlangensimulator-Modelle direkt im Simulator und auf der 
[GitHub-Lehrbuchbeispiele](https://github.com/A-Herzog/Warteschlangensimulator/tree/master/Simulator/src/main/java/ui/help/bookexamples)-Seite verfügbar.

## Veröffentlichungen

Eine Liste mit allen Veröffentlichungen zum Warteschlangensimulator finden Sie auf der [Veröffentlichungen-GitHub-Wiki](https://github.com/A-Herzog/Warteschlangensimulator/wiki/Publication-list)-Seite.

## Referenznutzer

[<img src="https://a-herzog.github.io/Warteschlangensimulator/Logo_SWZ_de.svg" height="75" width="300" title="Simulationswissenschaftliches Zentrum Clausthal-Göttingen" alt="Simulationswissenschaftliches Zentrum Clausthal-Göttingen">](https://www.simzentrum.de)

[<img src="https://a-herzog.github.io/Warteschlangensimulator/Logo_TUClausthal.svg" height="75" width="403" title="Technische Universität Clausthal - Institut für Mathematik" alt="Technische Universität Clausthal - Institut für Mathematik">](https://www.mathematik.tu-clausthal.de)

[<img src="https://a-herzog.github.io/Warteschlangensimulator/Logo_HDa.svg" height="75" width="530" title="Hochschule Darmstadt - Fachbereich Mathematik und Naturwissenschaften" alt="Hochschule Darmstadt - Fachbereich Mathematik und Naturwissenschaften">](https://fbmn.h-da.de)
