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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import language.Language;
import mathtools.TimeTools;
import tools.DrawIO;

/**
 * Speichert ein von {@link LogAnalyzerGraphicExporter} erstelltes
 * abstraktes Grafikmodell einer Log-Datei in einer
 * <a href="https://drawio-app.com/">https://drawio-app.com/</a>-Dateien.
 * @see LogAnalyzerGraphicExporter
 */
public class LogAnalyzerGraphicExporterWriterDrawIO extends DrawIO {
	/**
	 * Zuordnung von Ereignisobjekten zu xml-Dokument-IDs
	 * (um die Kanten zwischen Event-Objekten Zeichenflächen-xml-Daten zuordnen zu können)
	 * @see #drawEvents(List)
	 * @see #drawEdges(List)
	 */
	private Map<LogAnalyzerGraphicExporterEvent,String> eventIDs;

	/**
	 * Konstruktor
	 * @param data	Auszugebendes abstrakte Logdatei-Grafik
	 * @param file	Zieldatei (im <a href="https://drawio-app.com/">https://drawio</a>-Format)
	 */
	public LogAnalyzerGraphicExporterWriterDrawIO(final LogAnalyzerGraphicExporter data, final String file) {
		this(data,new File(file));
	}

	/**
	 * Konstruktor
	 * @param data	Auszugebendes abstrakte Logdatei-Grafik
	 * @param file	Zieldatei (im <a href="https://drawio-app.com/">https://drawio</a>-Format)
	 */
	public LogAnalyzerGraphicExporterWriterDrawIO(final LogAnalyzerGraphicExporter data, final File file) {
		super(file);
		eventIDs=new HashMap<>();
		drawTimes(data.getTimes());
		drawClients(data.getClients());
		drawEvents(data.getEvents());
		drawEdges(data.getEdges());
		drawLine(data.getLines());
	}

	/**
	 * Erstellt ein neues kastenförmiges Element und trägt es in das Dokument ein.
	 * @param box	Log-Datei-Analyse-Box die diese xml-Box repräsentieren soll
	 * @return	Neues Element
	 */
	private Element addBox(final LogAnalyzerGraphicExporterBox box) {
		return addBox(box.position,box.size);
	}

	/**
	 * Gibt die Zeitpunkt-Boxen aus.
	 * @param times	Liste aller Zeitpunkt-Boxen
	 */
	private void drawTimes(final List<LogAnalyzerGraphicExporterTime> times) {
		for (var time: times) {
			final Element node=addBox(time);
			node.setAttribute("value","<b>"+encodeHTML(TimeTools.formatExactLongTime(time.time,3))+"</b>");
			node.setAttribute("style","html=1;fillColor=#f5f5f5");
		}
	}

	/**
	 * Gibt die Kundennummer-Boxen aus.
	 * @param clients Liste aller Kundennummer-Boxen
	 */
	private void drawClients(final List<LogAnalyzerGraphicExporterClient> clients) {
		for (var client: clients) {
			final Element node=addBox(client);
			final String fillColor=(client.nr<0)?"#dae8fc":"#d5e8d4";
			node.setAttribute("style","html=1;horizontal=0;whiteSpace=wrap;fillColor="+fillColor);
			final String clientName=(client.nr<0)?Language.tr("LogAnalyzer.Global"):(Language.tr("LogAnalyzer.Client")+" "+client.nr);
			node.setAttribute("value","<b>"+encodeHTML(clientName)+"</b>");
		}
	}

	/**
	 * Generiert den html-Text zu einem Ereignis.
	 * @param event	Ereignis, dessen Daten in einen html-Text überführt werden sollen.
	 * @return	html-Text zur Anzeige in der xml-Box für das Ereignis
	 * @see #drawEvents(List)
	 */
	private String getEventText(final LogAnalyzerGraphicExporterEvent event) {
		final StringBuilder text=new StringBuilder();
		text.append("<b>"+encodeHTML(event.name)+"</b>");
		if (event.stationID>=0 || event.clientNr>=0) {
			text.append("<br>");
			if (event.stationID>=0) {
				text.append(encodeHTML(Language.tr("LogAnalyzer.Station")+": "));
				if (event.stationName==null) text.append("id="+event.stationID); else text.append(encodeHTML(event.stationName)+" (id="+event.stationID+")");
			}
			if (event.stationID>=0 && event.clientNr>=0) text.append(", ");
			if (event.clientNr>=0) {
				text.append(encodeHTML(Language.tr("LogAnalyzer.ClientNumber")+"="+event.clientNr));
			}
		}
		text.append("<br>");
		text.append("<small>"+encodeHTML(event.description)+"</small>");
		return text.toString();
	}

	/**
	 * Gibt die Ereignis-Boxen aus.<br>
	 * (Muss vor {@link #drawEdges(List)} aufgerufen werden.)
	 * @param events Liste aller Ereignis-Boxen
	 */
	private void drawEvents(final List<LogAnalyzerGraphicExporterEvent> events) {
		for (var event: events) {
			final Element node=addBox(event);
			node.setAttribute("value",getEventText(event));
			final String fillColor=(event.clientNr<0)?"#dae8fc":"#d5e8d4";
			node.setAttribute("style","html=1;align=left;whiteSpace=wrap;fillColor="+fillColor);
			eventIDs.put(event,node.getAttribute("id"));
		}
	}

	/**
	 * Style für die Verbindungskanten
	 * @see #drawEdges(List)
	 */
	private static final Map<String,String> edgeStyle;

	static {
		edgeStyle=new HashMap<>();
		edgeStyle.put("rounded","0");
		edgeStyle.put("orthogonalLoop","1");
		edgeStyle.put("jettySize","auto");
		edgeStyle.put("html","1");
		edgeStyle.put("edgeStyle","orthogonalEdgeStyle");
		edgeStyle.put("curved","1");
		edgeStyle.put("exitX","1");
		edgeStyle.put("exitY","0.5");
		edgeStyle.put("entryX","0");
		edgeStyle.put("entryY","0.5");
	}

	/**
	 * Gibt die Verbindungskanten aus.<br>
	 * (Muss nach {@link #drawEvents(List)} aufgerufen werden.)
	 * @param edges	Liste aller Verbindungskanten
	 */
	private void drawEdges(final List<LogAnalyzerGraphicExporterEdge> edges) {
		for (var edge: edges) {
			addEdge(eventIDs.get(edge.event1),eventIDs.get(edge.event2),edgeStyle,null);
		}
	}

	/**
	 * Gibt die Linien zur optischen Gestaltung aus.
	 * @param lines	Liste aller Linien
	 */
	private void drawLine(final List<LogAnalyzerGraphicExporterLine> lines) {
		for (var line: lines) {
			final Element node=addCell();
			node.setAttribute("edge","1");
			node.setAttribute("style","endArrow=none;dashed=1;html=1;dashPattern=1 3;strokeWidth=2");
			final Element geometry=doc.createElement("mxGeometry");
			node.appendChild(geometry);
			geometry.setAttribute("relative","1");
			geometry.setAttribute("as","geometry");
			final Element p1=doc.createElement("mxPoint");
			geometry.appendChild(p1);
			p1.setAttribute("x",""+line.p1.x);
			p1.setAttribute("y",""+line.p1.y);
			p1.setAttribute("as","sourcePoint");
			final Element p2=doc.createElement("mxPoint");
			geometry.appendChild(p2);
			p2.setAttribute("x",""+line.p2.x);
			p2.setAttribute("y",""+line.p2.y);
			p2.setAttribute("as","targetPoint");
		}
	}
}
