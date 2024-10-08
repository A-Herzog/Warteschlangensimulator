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

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import loganalyzer.LogAnalyzer.Event;

/**
 * Übersetzt eine von {@link LogAnalyzer} erstellte Analyse in ein
 * allgemeines, ausgabeunabhängiges Grafikformat.
 * @see LogAnalyzer
 */
public class LogAnalyzerGraphicExporter implements LogAnalyzerExporter {
	/**
	 * Größe einer Ereignis-Box
	 */
	private final Dimension boxSize;

	/**
	 * Horizontale und vertikale Abstände zwischen den Boxen
	 */
	private final Dimension boxSpacing;

	/**
	 * Erfasste Zeitpunkte
	 */
	private final List<LogAnalyzerGraphicExporterTime> times;

	/**
	 * Erfasste Kundennummern
	 */
	private final Map<Long,LogAnalyzerGraphicExporterClient> clients;

	/**
	 * Verbindungskanten
	 */
	private final List<LogAnalyzerGraphicExporterEdge> edges;

	/**
	 * Linien zur optischen Gestaltung
	 */
	private final List<LogAnalyzerGraphicExporterLine> lines;

	/**
	 * Aktuelle Zeit
	 * @see #setTime(double)
	 * @see #exportEvent(Event)
	 */
	private LogAnalyzerGraphicExporterTime currentTime;

	/**
	 * Aktuelle Kunde
	 * @see #setClient(long)
	 * @see #exportEvent(Event)
	 */
	private LogAnalyzerGraphicExporterClient currentClient;

	/**
	 * Konstruktor
	 * @param boxSize	Größe einer Ereignis-Box
	 * @param boxSpacing	Horizontale und vertikale Abstände zwischen den Boxen
	 */
	public LogAnalyzerGraphicExporter(final Dimension boxSize, final Dimension boxSpacing) {
		times=new ArrayList<>();
		clients=new HashMap<>();
		edges=new ArrayList<>();
		lines=new ArrayList<>();
		this.boxSize=new Dimension(boxSize);
		this.boxSpacing=new Dimension(boxSpacing);
	}

	@Override
	public void setTime(final double time) {
		currentTime=new LogAnalyzerGraphicExporterTime(time);
		times.add(currentTime);
		currentClient=null;
	}

	@Override
	public void setClient(final long clientNr) {
		currentClient=clients.computeIfAbsent(clientNr,nr->new LogAnalyzerGraphicExporterClient(nr));
	}

	@Override
	public void exportEvent(final Event event) {
		final LogAnalyzerGraphicExporterEvent eventBox=new LogAnalyzerGraphicExporterEvent(event);
		currentTime.add(eventBox);
		if (currentClient==null) currentClient=clients.computeIfAbsent(Long.valueOf(-1),nr->new LogAnalyzerGraphicExporterClient(nr));
		currentClient.add(eventBox);
	}

	/**
	 * Erzeugt die Zeile mit den Zeitangabe-Boxen oben.
	 * @see #exportDone()
	 */
	private void buildTimeBoxes() {
		int x=boxSpacing.width+boxSize.height+boxSpacing.width;
		int y=boxSpacing.height;

		for (var time: times) {
			time.position.x=x;
			time.position.y=y;
			time.size.width=boxSize.width;
			time.size.height=boxSize.height;

			x+=boxSize.width+boxSpacing.width;
		}
	}

	/**
	 * Erzeugt die Boxen für die Ereignisse (noch nicht an den finalen y-Positionen).
	 * @see #exportDone()
	 */
	private void buildEventBoxes() {
		for (var time: times) {
			long lastClientNr=-2;
			int x=time.position.x;
			int y=time.position.y+time.size.height;

			for (var event: time.events) {
				if (event.clientNr!=lastClientNr) y+=boxSpacing.height;
				lastClientNr=event.clientNr;

				event.position.x=x;
				event.position.y=y;
				event.size.width=boxSize.width;
				event.size.height=boxSize.height;

				y+=boxSize.height;
			}
		}
	}

	/**
	 * Berechnete die minimale und die maximale y-Koordinate der Ereignisse eines Kunden
	 * @param clientNr	Kunde für den die minimale und die maximale y-Koordinate bestimmt werden soll
	 * @return 2-elementiges Array mit Minimum und Maximum (existieren keine Ereignisse für die angegebene Kundennummer, so sind beide Werte 0)
	 */
	private int[] getMinMaxYByClientNr(final long clientNr) {
		int minY=0;
		int maxY=0;
		for (var time: times) {
			int timeMinY=0;
			int timeMaxY=0;
			for (var event: time.events) {
				if (event.clientNr==clientNr || (timeMinY>0 && event.clientNr==-1)) {
					if (timeMinY==0) timeMinY=event.position.y;
					timeMaxY=Math.max(timeMaxY,event.position.y+boxSize.height);
				} else {
					if (timeMaxY>0) break;
					if (event.clientNr>clientNr) break;
				}
			}
			if (timeMinY>0) {
				if (minY>0) minY=Math.min(minY,timeMinY); else minY=timeMinY;
			}
			maxY=Math.max(maxY,timeMaxY);
		}
		return new int[] {minY, maxY};
	}

	/**
	 * Verschiebt die Ereignisse der Kunden ab einer bestimmte Nummer entlang der y-Koordinate
	 * @param currentClientNr	Aktuelle Kundennummer (alle Kunden mit einer höheren Nummer werden verschoben)
	 * @param currentClientMaxY	Maximale y-Koordinate der Ereignisboxen für den aktuellen Kunden
	 */
	private void moveElementsDown(final long currentClientNr, final int currentClientMaxY) {
		for (var time: times) {
			for (int i=0;i<time.events.size();i++) {
				if (time.events.get(i).clientNr>currentClientNr) {
					/* Ab hier verschieben */
					final int deltaY=currentClientMaxY+boxSpacing.height-time.events.get(i).position.y;
					for (int j=i;j<time.events.size();j++) {
						final LogAnalyzerGraphicExporterEvent event=time.events.get(j);
						event.position.y+=deltaY;
					}
					break;
				}
			}
		}
	}

	/**
	 * Fügt die Verbindungspfeile ein
	 * @param clientNrs	Sortierte Liste der vorhandenen Kundennummern
	 */
	private void buildEdges(final List<Long> clientNrs) {
		for (long clientNr: clientNrs) {
			if (clientNr<0) continue;
			LogAnalyzerGraphicExporterEvent lastEvent=null;
			for (var time: times) {
				final var events=time.events.stream().filter(event->event.clientNr==clientNr).collect(Collectors.toList());
				if (events.size()>0) {
					if (lastEvent!=null) edges.add(new LogAnalyzerGraphicExporterEdge(lastEvent,events.get(0)));
					lastEvent=events.get(events.size()-1);
				}
			}
		}
	}

	@Override
	public void exportDone() {
		/* Sortiere Liste der Kundennummern */
		final List<Long> clientNrs=clients.keySet().stream().sorted().collect(Collectors.toList());

		/* Zeitpunkte */
		buildTimeBoxes();

		/* Ereignisse (spaltenweise) */
		buildEventBoxes();

		/* Ereignisse vertikal ausrichten */
		for (var clientNr: clientNrs) {
			/* Maximalen y-Wert für einen Kundentyp ermitteln */
			final var yRange=getMinMaxYByClientNr(clientNr);
			if (yRange[1]==0) continue;

			/* Vertikale Box links einzeichnen */
			final var client=clients.get(clientNr);
			client.position.x=boxSpacing.width;
			client.position.y=yRange[0];
			client.size.width=boxSize.height;
			client.size.height=yRange[1]-yRange[0];

			/* Andere Boxen nach unten verschieben */
			moveElementsDown(clientNr,yRange[1]);
		}

		/* Trennlinien zwischen den Kundentypen einfügen */
		final int minX=boxSpacing.width;
		final int maxX=times.get(times.size()-1).position.x+boxSize.width;
		boolean first=true;
		for (var clientNr: clientNrs) {
			if (first) {first=false; continue;}
			final int y=clients.get(clientNr).position.y-boxSpacing.height/2;
			lines.add(new LogAnalyzerGraphicExporterLine(new Point(minX,y),new Point(maxX,y)));
		}

		/* Pfeile */
		buildEdges(clientNrs);
	}

	/**
	 * Liefert die Liste aller Zeitpunkt-Boxen (aufsteigend geordnet).
	 * @return	Liste aller Zeitpunkt-Boxen
	 */
	public List<LogAnalyzerGraphicExporterTime> getTimes() {
		return times;
	}

	/**
	 * Liefert die Liste aller Kundennummer-Boxen (aufsteigend geordnet).
	 * @return	Liste aller Kundennummer-Boxen
	 */
	public List<LogAnalyzerGraphicExporterClient> getClients() {
		final List<LogAnalyzerGraphicExporterClient> result=new ArrayList<>();
		for (long nr: clients.keySet().stream().sorted().collect(Collectors.toList())) result.add(clients.get(nr));
		return result;
	}

	/**
	 * Liefert die Liste aller Ereignis-Boxen.
	 * @return	Liste aller Ereignis-Boxen
	 */
	public List<LogAnalyzerGraphicExporterEvent> getEvents() {
		final List<LogAnalyzerGraphicExporterEvent> result=new ArrayList<>();
		for (var time: times) result.addAll(time.events);
		return result;
	}

	/**
	 * Liefert die Liste aller Verbindungskanten.
	 * @return	Liste aller Verbindungskanten
	 */
	public List<LogAnalyzerGraphicExporterEdge> getEdges() {
		return edges;
	}

	/**
	 * Liefert die Liste aller Linien (zur optischen Gestaltung).
	 * @return	Liste aller Linien
	 */
	public List<LogAnalyzerGraphicExporterLine> getLines() {
		return lines;
	}
}
