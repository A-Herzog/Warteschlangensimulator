/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package simulator.coreelements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import simcore.logging.SimLogging;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.logging.CallbackLoggerWithJS;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.statistics.Statistics;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementVertex;

/**
 * Äquivalent von <code>ModelElement</code> als Laufzeit-Element
 * @author Alexander Herzog
 * @see ModelElement
 */
public abstract class RunElement {
	/**
	 * ID des Laufzeit-Elements<br>
	 * (Wird vom Editor-Element übernommen)
	 */
	public final int id;

	/**
	 * Hat nur einen Wert ungleich -1, wenn das aktuelle Element in einem
	 * Untermodell enthalten ist. Dann steht hier die ID des Elements,
	 * welches das Untermodell kapselt.
	 */
	public final int parentId;

	/**
	 * Farbe für Logging-Meldungen<br>
	 * (Wird vom Editor-Element übernommen)
	 */
	public final Color logTextColor;

	/**
	 * Name des Laufzeit-Elements<br>
	 * (Wird vom Editor-Element übernommen)
	 */
	public final String name;

	/**
	 * Erfassung von Anzahl an Kunden an den Stationen (gesamt und wartend) und von Zwischen-Ankunfts- und -Abgangs-Zeiten an der Station
	 */
	public boolean stationStatisticsActive;

	/**
	 * Intervalllänge in Sekunden für die Erfassung des maximalen Durchsatzes an der Station<br>
	 * (Werte &le;0 zur Abschaltung der Erfassung)
	 */
	public int maxThroughputIntervalLengthSec;

	/**
	 * Konstruktor der Klasse <code>RunElement</code>
	 * @param element	Modell-Element aus dem ID und Farbe ausgelesen werden
	 * @param name	Name der Station
	 */
	public RunElement(final ModelElementPosition element, final String name) {
		this.id=(element==null)?-1:element.getId();
		this.parentId=getParentId(element);
		this.logTextColor=(!(element instanceof ModelElementBox))?Color.DARK_GRAY:((ModelElementBox)element).getDrawBackgroundColor();
		this.name=name;
		stationStatisticsActive=(element==null)?true:element.isStationStatisticsActive();
		if (element instanceof ModelElementBox) {
			maxThroughputIntervalLengthSec=((ModelElementBox)element).getMaxThroughputIntervalSeconds();
		}
	}

	/**
	 * Konstruktor der Klasse <code>RunElement</code>
	 * @param id	ID des Elements
	 * @param name	Name der Station
	 */
	public RunElement(final int id, final String name) {
		this.id=id;
		this.parentId=-1;
		this.logTextColor=Color.DARK_GRAY;
		this.name=name;
		stationStatisticsActive=true;
		maxThroughputIntervalLengthSec=-1;
	}

	/**
	 * Liefert die ID des Sub-Elements in dem sich dieses Element befindet
	 * sofern es in einem Sub-Element enthalten ist, sonst -1.
	 * @param element	Element bei dem das Eltern-Sub-Element ermittelt werden soll
	 * @return	ID des Sub-Elements oder -1, wenn sich das Element auf der Hauptebene befindet
	 */
	private int getParentId(final ModelElementPosition element) {
		if (element==null || element.getSurface()==null) return -1;
		final ModelSurface mainSurface=element.getSurface().getParentSurface();
		if (mainSurface==null) return -1;
		final int id=element.getId();

		for (ModelElement e: mainSurface.getElements()) if ((e instanceof ModelElementSub) && (((ModelElementSub)e).getSubSurface().getById(id)!=null)) return e.getId();

		return -1;
	}

	/**
	 * Erstellt den vollständigen Namen des Elements bestehend aus Typenname, Name und ID
	 * @param element	Editor-Element aus dem Name und ID ausgelesen werden sollen
	 * @param name	Typenname
	 * @return	vollständiger Name
	 */
	public static final String buildName(final ModelElementPosition element, final String name) {
		final StringBuilder sb=new StringBuilder();
		if (name!=null && !name.isBlank()) {
			sb.append(name);
			sb.append(" ");
		}
		if (element!=null) {
			final String s=element.getName();
			if (s!=null && !s.isBlank()) {
				sb.append("\"");
				sb.append(s);
				sb.append("\" ");
			}
		}
		sb.append("(id=");
		if (element==null) {
			sb.append("<");
			sb.append(Language.tr("Simulation.NoID"));
			sb.append(">");
		} else {
			sb.append(element.getId());
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Versucht zu einem <code>ModelElement</code>-Objekt ein <code>RunElement</code>-Objekt des aktuellen Typs zu erstellen
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param element	Editor-Modell-Element zu dem ein Laufzeit-Modell-Element erstellt werden soll
	 * @param parent	Optional übergeordnetes Untermodell-Element (oder <code>null</code>, wenn sich das Element auf der Hauptebene befindet)
	 * @param testOnly	Wird hier <code>true</code> übergeben, so werden externe Datenquellen nicht wirklich geladen
	 * @return	Liefert <code>null</code>, falls das aktuelle Laufzeit-Element nicht zu dem Editor-Element passt; ein <code>RunElement</code>-Objekt im Erfolgsfall und eine Zeichenkette im Fehlerfall
	 */
	public abstract Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly);

	/**
	 * Prüft, ob das Modell-Element in Ordnung ist
	 * @param element	Zu prüfendes Element
	 * @return	Gibt <code>null</code> zurück, wenn das Modell-Element nicht zu diesem Run-Element passt, sonst ein Objekt mit weiteren Informationen (dieses Objekt kann auch "Erfolg" darstellen)
	 */
	public abstract RunModelCreatorStatus test(final ModelElement element);

	/**
	 * Ermöglicht dem <code>RunElement</code>-Element Vorbereitungen vor dem Start der Simulation vorzunehmen.<br>
	 * (Z.B. kann hier die Verknüpfung von Stationen auf Referenzen-Basis erfolgen.)
	 * @param runModel	Laufzeit-Modell, welches zu diesem Zeitpunkt alle <code>RunElement</code>-Elemente enthält
	 */
	public void prepareRun(final RunModel runModel) {
	}

	/**
	 * Versucht basierend auf einer auslaufenden Kante das nächste Element vom Typ <code>ModelElementBox</code> zu finden
	 * @param connection	Aus dem Start-Element auslaufende Kante (<code>null</code> wird erkannt und als Fehler gemeldet)
	 * @return	Liefert die ID des nächsten <code>ModelElementBox</code>-Elements oder -1, wenn es kein Ziel gibt
	 */
	protected final int findNextId(final ModelElementEdge connection) {
		if (connection==null) return -1;
		ModelElement element=connection;
		while (element!=null) {
			if (!(element instanceof ModelElementEdge)) return -1;
			element=((ModelElementEdge)element).getConnectionEnd();
			if (element==null) return -1;
			if (element instanceof ModelElementVertex) {
				element=((ModelElementVertex)element).getEdgeOut();
				continue;
			}
			return element.getId();
		}
		return -1;
	}

	/**
	 * Versucht basierend auf einer einlaufenden Kante das vorherige Element vom Typ <code>ModelElementBox</code> zu finden
	 * @param connection	In das Start-Element einlaufende Kante (<code>null</code> wird erkannt und als Fehler gemeldet)
	 * @return	Liefert die IDs des vorherigen <code>ModelElementBox</code>-Elemente
	 */
	protected final List<Integer> findPreviousId(final ModelElementEdge connection) {
		if (connection==null) return new ArrayList<>();
		ModelElement element=connection.getConnectionStart();
		if (element==null) return new ArrayList<>();

		if (element instanceof ModelElementBox) return Arrays.asList(element.getId());

		if (element instanceof ModelElementVertex) {
			List<Integer> results=new ArrayList<>();
			for (ModelElementEdge edge: ((ModelElementVertex)element).getEdgesIn()) results.addAll(findPreviousId(edge));
			return results;
		}

		return null;
	}

	/**
	 * Bearbeitung eines Kunden an einer Station
	 * @param simData	Simulationsdaten
	 * @param client	Kunde
	 */
	public abstract void processArrival(final SimulationData simData, final RunDataClient client);

	/**
	 * Bearbeitung des Abgangs eines Kunden an einer Station
	 * @param simData	Simulationsdaten
	 * @param client	Kunde
	 */
	public abstract void processLeave(final SimulationData simData, final RunDataClient client);

	/**
	 * Liefert das zu dieser Station (=über alle Threads global) lokale Datenobjekt.
	 * Existiert ein solches Objekt noch nicht, so wird es angelegt.
	 * @param simData	Simulationsdaten
	 * @return	Objekt, welches die thread-lokalen Daten zu dieser Station vorhält
	 */
	public RunElementData getData(final SimulationData simData) {
		RunElementData data;
		data=(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementData(this,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Zählung der Kunden Ein- und Ausgänge aus dieser Station?<br>
	 * (Muss nur überschrieben werden, wenn für eine Station keine Zählung erfolgen soll.)
	 * @return	Gibt <code>true</code> zurück, wenn eine Zählung für diese Station stattfinden soll.
	 */
	public boolean isClientCountStation() {
		return true;
	}

	/**
	 * Erfassung der Zwischenankunftszeiten in Abhängigkeit von der Warteschlangenlänge?
	 * @return	Zwischenankunftszeiten in Abhängigkeit von der Warteschlangenlänge
	 * @param simData	Simulationsdatenobjekt
	 * @see Statistics#stationsInterarrivalTimeByState
	 */
	public boolean isInterarrivalByQueueStation(final SimulationData simData) {
		return false;
	}

	/**
	 * Gibt der Station die Möglichkeit, am Ende der Simulation Aufräumarbeiten durchzuführen.<br>
	 * (Bei einem Objekt, welches Daten in eine Datei schreibt kann dies z.B. das Schließen der Datei sein.)
	 * @param simData	Simulationsdatenobjekt
	 */
	public void finalCleanUp(final SimulationData simData) {
	}

	/**
	 * Erfasst ein Ereignis in der Logging-Aufzeichnung
	 * @param simData	Simulationsdatenobjekt
	 * @param event	Name des Ereignisses
	 * @param info	Zusätzliche Informationen
	 * @see SimulationData#loggingActive
	 * @see SimLogging#log(long, Color, String, int, String)
	 */
	public final void log(final SimulationData simData, final String event, final String info) {
		if (!simData.logInfoStation) return;
		if (simData.loggingIDs!=null && !simData.loggingIDs[id]) return;
		simData.logEventExecution(logTextColor,event,id,info);
	}

	/**
	 * Erfasst ein Ankunfts-Ereignis in der Logging-Aufzeichnung
	 * @param simData	Simulationsdatenobjekt
	 * @param event	Name des Ereignisses
	 * @param info	Zusätzliche Informationen
	 * @see SimulationData#loggingActive
	 * @see SimLogging#log(long, Color, String, int, String)
	 */
	public final void logArrive(final SimulationData simData, final String event, final String info) {
		if (!simData.logArrival) return;
		if (simData.loggingIDs!=null && !simData.loggingIDs[id]) return;
		simData.logEventExecution(logTextColor,event,id,info);
	}

	/**
	 * Erfasst ein Abgangs-Ereignis in der Logging-Aufzeichnung
	 * @param simData	Simulationsdatenobjekt
	 * @param event	Name des Ereignisses
	 * @param info	Zusätzliche Informationen
	 * @see SimulationData#loggingActive
	 * @see SimLogging#log(long, Color, String, int, String)
	 */
	public final void logLeave(final SimulationData simData, final String event, final String info) {
		if (!simData.logDeparture) return;
		if (simData.loggingIDs!=null && !simData.loggingIDs[id]) return;
		simData.logEventExecution(logTextColor,event,id,info);
	}

	/**
	 * Erfasst die Ausführung von Skript-Code in der Logging-Aufzeichnung
	 * @param simData	Simulationsdatenobjekt
	 * @param script	Skript das ausgeführt wurde
	 * @param result	Rückgabewert des Skriptes
	 * @see SimulationData#loggingActive
	 * @see CallbackLoggerWithJS#logJS(long, String, Color, String, String)
	 */
	public final void logJS(final SimulationData simData, final String script, final String result) {
		if (simData.logging instanceof CallbackLoggerWithJS) {
			((CallbackLoggerWithJS)simData.logging).logJS(simData.currentTime,name,logTextColor,script,result);
		}
	}

	/**
	 * Erfasst die Ausführung von Skript-Code in der Logging-Aufzeichnung
	 * @param simData	Simulationsdatenobjekt
	 * @param script	Skript das ausgeführt wurde
	 * @param result	Rückgabewert des Skriptes
	 * @see SimulationData#loggingActive
	 * @see CallbackLoggerWithJS#logJS(long, String, Color, String, String)
	 */
	public final void logJS(final SimulationData simData, final String script, final Object result) {
		if (simData.logging instanceof CallbackLoggerWithJS) {
			final String resultString=(result==null)?Language.tr("Animation.JSResults.NoReturnValue"):result.toString();
			((CallbackLoggerWithJS)simData.logging).logJS(simData.currentTime,name,logTextColor,script,resultString);
		}
	}

	/**
	 * Liefert optional die nächste Station (z.B. zum Auffinden von Pfaden)
	 * @return	nächste Station (kann <code>null</code> sein)
	 */
	public RunElement getNext() {
		return null;
	}

	/**
	 * Diese Methode wird von {@link SimulationData#endWarmUp()} aufgerufen, wenn die Einschwingphase beendet wird.
	 * @param simData	Simulationsdatenobjekt
	 */
	public void endWarmUpNotify(final SimulationData simData) {
	}
}