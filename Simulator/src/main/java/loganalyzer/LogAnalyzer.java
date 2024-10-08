/**
 * Copyright 2024 Alexander Herzog
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
package loganalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Lädt und analysiert eine Simulations-Log-Datei.
 */
public class LogAnalyzer {
	/**
	 * Liste aller Element im Modell.<br>
	 * Die Liste kann leer sein, ist aber nie <code>null</code>.
	 */
	private final List<ModelElement> elements;

	/**
	 * Zuordnung (zum beschleunigten Zugriff) von Elementen-ID
	 * zu Elementennamen.
	 * @see #getStationName(int)
	 */
	private final Map<Integer,String> elementNames;

	/**
	 * Liste der geladenen Ereignis-Datensätze.
	 * @see #load(File)
	 */
	private final List<Event> events;

	/**
	 * Konstruktor
	 * @param model	Modell (zum Auslesen der Elementennamen), darf <code>null</code> sein
	 */
	public LogAnalyzer(final EditModel model) {
		elements=(model==null)?List.of():model.surface.getElementsIncludingSubModels();
		elementNames=new HashMap<>();
		events=new ArrayList<>();
	}

	/**
	 * Konstruktor
	 * @param modelFile	Modell-Datei (zum Auslesen der Elementennamen), darf <code>null</code> sein
	 */
	public LogAnalyzer(final File modelFile) {
		this(loadModel(modelFile));
	}

	/**
	 * Konstruktor<br>
	 * (Es wird angenommen, dass keine Modelldaten geladen werden sollen.)
	 */
	public LogAnalyzer() {
		this((EditModel)null);
	}

	/**
	 * Konstruktor
	 * @param modelFile	Modell-Datei (zum Auslesen der Elementennamen), darf <code>null</code> sein
	 */
	public LogAnalyzer(final String modelFile) {
		this((modelFile==null)?null:new File(modelFile));
	}

	/**
	 * Versucht ein Modell aus einer Datei zu laden.
	 * @param file	Dateiname der Modelldatei (darf <code>null</code> sein)
	 * @return	Geladenes Modell (ist <code>null</code>, wenn entweder als Datei schon <code>null</code> übergeben wurde oder aber die Datei nicht geladen werden konnte)
	 */
	private static EditModel loadModel(final File file) {
		if (file==null) return null;
		final EditModel model=new EditModel();
		if (model.loadFromFile(file)==null) return model;
		return null;
	}

	/**
	 * Lädt eine Simulations-Log-Datei und verarbeitet diese.
	 * @param file	Zu verarbeitende Textdatei
	 * @return	Gibt an, ob die Datei geladen werden konnte
	 */
	public boolean load(final File file) {
		events.clear();
		try (var stream=Files.lines(file.toPath())) {
			events.addAll(stream.map(line->line.trim()).filter(line->!line.isBlank()).map(line->loadEventFromLine(line)).filter(event->event!=null).collect(Collectors.toList()));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Lädt eine Simulations-Log-Datei und verarbeitet diese.
	 * @param file	Zu verarbeitende Textdatei
	 * @return	Gibt an, ob die Datei geladen werden konnte
	 */
	public boolean load(final String file) {
		if (file==null) {
			events.clear();
			return false;
		}

		return load(new File(file));
	}

	/**
	 * Interpretiert eine Zahl oder eine Zeitangabe als Zeit-Zahlenwert
	 * @param time	Zeitangabe (Zahl oder Zeitangabe) als Zeichenkette
	 * @return	Zeitangabe als Fließkommazahl
	 */
	private Double getTime(final String time) {
		if (time.contains(":")) {
			return TimeTools.getExactTime(time);
		} else {
			return NumberTools.getNotNegativeDouble(time);
		}
	}

	/**
	 * Interpretiert eine Zeile aus einer Simulations-Log-Datei.
	 * @param line	Zu interpretierende Zeile
	 * @return	Liefert im Erfolgsfall ein {@link Event}-Objekt, sonst <code>null</code>.
	 */
	private Event loadEventFromLine(final String line) {
		final var parts=line.split("\\t");
		if (parts.length<3) return null;

		final Double time;
		final String name;
		final Long stationId;
		final String description;

		if (parts.length>=4) {
			/* Zeitpunkt - Name - StationsID - Beschreibung */
			time=getTime(parts[0]); /* Spalte 1: Zeitpunkt */
			name=parts[1]; /* Spalte 2: Name */
			stationId=NumberTools.getPositiveLong(parts[2]); /* Spalte 3: Stations ID */
			description=parts[3]; /* Spalte 4: Beschreibung */
		} else {
			/* Zeitpunkt - Name - Beschreibung */
			time=getTime(parts[0]); /* Spalte 1: Zeitpunkt */
			name=parts[1]; /* Spalte 2: Name */
			stationId=null; /* keine Stations ID */
			description=parts[2]; /* Spalte 3: Beschreibung */
		}

		if (time==null) return null;
		final long clientNr=getClientNr(description);
		return new Event(time,name,clientNr,(stationId==null)?-1:stationId,(stationId==null)?null:getStationName(stationId.intValue()),description);
	}

	/**
	 * Versucht die laufende Nummer eines Kunden aus einer Ereignisbeschreibung auszulesen.
	 * @param description	Ereignisbeschreibung
	 * @return	Fortlaufende Nummer des Kunden oder -1, wenn keine entsprechende Nummer ausgelesen werden konnte
	 */
	private long getClientNr(final String description) {
		final int len=description.length();
		int index=0;
		while (index<len) {
			index=description.indexOf("(id=",index);
			if (index<0) return -1;
			index+=4;
			while (index<len && description.charAt(index)>='0' && description.charAt(index)<='9') index++;
			if (index>=len-4) return -1;
			if (!description.substring(index,index+4).equals(",nr=")) continue;
			index+=4;
			if (index>=len) return -1;
			int index2=index;
			while (index2<len && description.charAt(index2)>='0' && description.charAt(index2)<='9') index2++;
			if (index2>index) {
				final Long L=NumberTools.getPositiveLong(description.substring(index,index2));
				if (L!=null) return L;
			}
		}
		return -1;
	}

	/**
	 * Versucht basierend auf dem Modell eine Stationsbeschreibung zu einer Stations-ID zu ermitteln
	 * @param id	Stations-ID (darf -1 sein; dann wird immer <code>null</code> geliefert)
	 * @return	Stationsbeschreibung oder <code>null</code>, wenn keine Beschreibung ermittelt werden konnte
	 */
	private String getStationName(final int id) {
		if (id<=0) return null;

		final String name=elementNames.get(id);
		if (name!=null) return name;

		ModelElement element=null;
		for (var e: elements) if (e.getId()==id) {element=e; break;}
		if (element==null) return null;

		final StringBuilder nameBuilder=new StringBuilder();
		nameBuilder.append(element.getContextMenuElementName());
		final String elementName=element.getName();
		if (!elementName.isBlank()) {
			nameBuilder.append(" \"");
			nameBuilder.append(elementName);
			nameBuilder.append("\"");
		}
		elementNames.put(id,nameBuilder.toString());
		return nameBuilder.toString();
	}

	/**
	 * Liefert die Anzahl an geladenen Ereignis-Datensätzen.
	 * @return	Anzahl an geladenen Ereignis-Datensätzen
	 */
	public int eventCount() {
		return events.size();
	}

	/**
	 * Schreibt die aufbereiteten Ereignis-Datensätze in ein Export-System.
	 * @param exporter	Export-System
	 * @see LogAnalyzerExporter
	 */
	public void export(final LogAnalyzerExporter exporter) {
		long lastClientNr=-2;
		double lastTime=-1;

		exporter.exportStart();

		for (var event: events) {
			/* Andere Zeit? */
			if (event.time!=lastTime) {
				exporter.setTime(event.time);
				lastTime=event.time;
				lastClientNr=-1;
			}

			/* Anderer Kunde? */
			if (lastClientNr!=event.clientNr) {
				if (event.clientNr==-1) {
					if (lastClientNr==-2) {
						exporter.setClient(-1);
						lastClientNr=-1;
					}
				} else {
					exporter.setClient(event.clientNr);
					lastClientNr=event.clientNr;
				}
			}

			/* Ausgabe */
			exporter.exportEvent(event);
		}

		exporter.exportDone();
	}

	/**
	 * Einzelner Ereignis-Datensatz (entspricht einer Zeile in der Log-Datei)
	 */
	public static class Event {
		/**
		 * Zeitpunkt der Ereignisausführung
		 */
		public final double time;

		/**
		 * Ereignisname
		 */
		public final String name;

		/**
		 * Fortlaufende Nummer des Kunden (ist -1, wenn das Ereignis keinem konkreten Kunden zugeordnet werden konnte)
		 */
		public final long clientNr;

		/**
		 * ID der Station, an der das Ereignis auftrat (ist -1, wenn das Ereignis keiner konkreten Station zugeordnet werden konnte)
		 */
		public final long stationID;

		/**
		 * Name der Station, an der das Ereignis auftrat (ist <code>null</code>, wenn das Ereignis keiner konkreten Station zugeordnet werden konnte oder aber wenn kein Modell zum Laden der Namen verfügbar war)
		 */
		public final String stationName;

		/**
		 * Beschreibung zum Ereignis
		 */
		public final String description;

		/**
		 * Konstruktor
		 * @param time	Zeitpunkt der Ereignisausführung
		 * @param name	Ereignisname
		 * @param clientNr	Fortlaufende Nummer des Kunden (ist -1, wenn das Ereignis keinem konkretem Kunden zugeordnet werden konnte)
		 * @param stationID	ID der Station, an der das Ereignis auftrat (ist -1, wenn das Ereignis keiner konkreten Station zugeordnet werden konnte)
		 * @param stationName	Name der Station, an der das Ereignis auftrat (ist <code>null</code>, wenn das Ereignis keiner konkreten Station zugeordnet werden konnte oder aber wenn kein Modell zum Laden der Namen verfügbar war)
		 * @param description	Beschreibung zum Ereignis
		 */
		private Event(final double time, final String name, final long clientNr, final long stationID, final String stationName, final String description) {
			this.time=time;
			this.name=name;
			this.clientNr=clientNr;
			this.stationID=stationID;
			this.stationName=stationName;
			this.description=description;
		}
	}
}
