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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;

import de.erichseifert.vectorgraphics2d.util.PageSize;
import language.Language;
import mathtools.TimeTools;
import systemtools.statistics.PDFFontMetrics;
import systemtools.statistics.PDFWriterBase;

/**
 * Speichert ein von {@link LogAnalyzerGraphicExporter} erstelltes
 * abstraktes Grafikmodell einer Log-Datei in einer pdf-Datei.
 * @see LogAnalyzerGraphicExporter
 */
public class LogAnalyzerGraphicExporterWriterPDF extends PDFWriterBase {
	/**
	 * Auszugebendes abstrakte Logdatei-Grafik
	 */
	private final LogAnalyzerGraphicExporter data;

	/**
	 * Zieldatei (im pdf-Format)
	 */
	private final File file;

	/**
	 * Hält die Schriftgrößen in pt vor
	 */
	private final PDFFontMetrics fontMetrics;

	/**
	 * Notwendige Seitengröße gemessen in der abstrakten Einheit aus {@link LogAnalyzerGraphicExporter}
	 * @see #getPageSize()
	 */
	private final Dimension pageSize;

	/**
	 * Skalierungsfaktor von {@link PageSize} zum pdf-Format
	 */
	private float pageScale;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element; das Element muss zum Zeitpunkt dem Aufruf dieser Methode sichtbar sein, da <code>getGraphics()</code> des <code>owner</code>-Elements aufgerufen wird.
	 * @param data	Auszugebendes abstrakte Logdatei-Grafik
	 * @param file	Zieldatei (im pdf-Format)
	 */
	public LogAnalyzerGraphicExporterWriterPDF(final Component owner, final LogAnalyzerGraphicExporter data, final String file) {
		this(owner,data,new File(file));
	}

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element; das Element muss zum Zeitpunkt dem Aufruf dieser Methode sichtbar sein, da <code>getGraphics()</code> des <code>owner</code>-Elements aufgerufen wird.
	 * @param data	Auszugebendes abstrakte Logdatei-Grafik
	 * @param file	Zieldatei (im pdf-Format)
	 */
	public LogAnalyzerGraphicExporterWriterPDF(final Component owner, final LogAnalyzerGraphicExporter data, final File file) {
		this.data=data;
		this.file=file;

		/* Schriftgrößen bestimmen */
		fontMetrics=new PDFFontMetrics(owner);

		/* Schriftarten ok? */
		systemOK=fontMetrics.systemOK;

		/* Seite anlegen */
		pageSize=getPageSize();
		pageScale=1;
		while (true) {
			pageSize.width=Math.max((int)Math.ceil(pageSize.width*pageScale),1);
			pageSize.height=Math.max((int)Math.ceil(pageSize.height*pageScale),1);
			if (pageSize.width<10_000 && pageSize.height<10_000) break;
			pageScale/=2;
		}
		if (systemOK) newPage(pageSize);
	}

	/**
	 * Ermittelt die notwendige Seitengröße gemessen in der
	 * abstrakten Einheit aus {@link LogAnalyzerGraphicExporter}.
	 * @return	Notwendige Seitengröße um alle Elemente darstellen zu können
	 * @see #pageSize
	 */
	private Dimension getPageSize() {
		final var events=data.getEvents();
		return new Dimension(
				events.stream().mapToInt(event->event.position.x+event.size.width).max().orElse(1),
				events.stream().mapToInt(event->event.position.y+event.size.height).max().orElse(1));
	}

	/**
	 * Fügt ein Rechteck in die pdf ein.
	 * @param position	Position der linken oberen Ecke (in der {@link LogAnalyzerGraphicExporter}-Skalierung)
	 * @param size	Größe des Rechtecks (in der {@link LogAnalyzerGraphicExporter}-Skalierung)
	 * @param color	Füllfarbe (Rahmenfarbe ist immer schwarz)
	 */
	private void drawBox(final Point position, final Dimension size, final Color color) {
		final float x1=position.x*pageScale;
		final float y1=pageSize.height-position.y*pageScale;
		final float x2=(position.x+size.width)*pageScale;
		final float y2=pageSize.height-(position.y+size.height)*pageScale;
		fillRectangle(x1,y1,x2,y2,pageScale,color);
		drawRectangle(x1,y1,x2,y2,pageScale,Color.BLACK);
	}

	/**
	 * Fügt ein Rechteck in die pdf ein.
	 * @param box	Zugehöriges Objekt aus {@link LogAnalyzerGraphicExporter}
	 * @param color	Füllfarbe (Rahmenfarbe ist immer schwarz)
	 */
	private void drawBox(final LogAnalyzerGraphicExporterBox box, final Color color) {
		drawBox(box.position,box.size,color);
	}

	/**
	 * Zeichnet eine Linie in die pdf ein.
	 * @param p1	Startpunkt (in der {@link LogAnalyzerGraphicExporter}-Skalierung)
	 * @param p2	Endpunkt (in der {@link LogAnalyzerGraphicExporter}-Skalierung)
	 * @param color	Linienfarbe
	 */
	private void drawLine(final Point p1, final Point p2, final Color color) {
		final float x1=p1.x*pageScale;
		final float y1=pageSize.height-p1.y*pageScale;
		final float x2=p2.x*pageScale;
		final float y2=pageSize.height-p2.y*pageScale;
		drawLine(x1,y1,x2,y2,pageScale,color);
	}

	/**
	 * Zeichnet eine gestrichelte Linie in die pdf ein.
	 * @param p1	Startpunkt (in der {@link LogAnalyzerGraphicExporter}-Skalierung)
	 * @param p2	Endpunkt (in der {@link LogAnalyzerGraphicExporter}-Skalierung)
	 * @param color	Linienfarbe
	 */
	private void drawDashedLine(final Point p1, final Point p2, final Color color) {
		final float x1=p1.x*pageScale;
		final float y1=pageSize.height-p1.y*pageScale;
		final float x2=p2.x*pageScale;
		final float y2=pageSize.height-p2.y*pageScale;
		drawDashedLine(x1,y1,x2,y2,pageScale,color,10);
	}

	/**
	 * Generiert den html-Text zu einem Ereignis.
	 * @param event	Ereignis, dessen Daten in Textzeilen überführt werden sollen.
	 * @return	3-elementiges Array mit den Textzeilen
	 */
	private String[] getEventText(final LogAnalyzerGraphicExporterEvent event) {
		final StringBuilder info=new StringBuilder();
		if (event.stationID>=0) {
			info.append(Language.tr("LogAnalyzer.Station")+": ");
			if (event.stationName==null) info.append("id="+event.stationID); else info.append(event.stationName+" (id="+event.stationID+")");
		}
		if (event.stationID>=0 && event.clientNr>=0) info.append(", ");
		if (event.clientNr>=0) {
			info.append(Language.tr("LogAnalyzer.ClientNumber")+"="+event.clientNr);
		}

		return new String[] {event.name,info.toString(),event.description};
	}

	/**
	 * Trägt die Elemente in die pdf ein.
	 * @see #save()
	 */
	private void draw() {
		final int fontSize=Math.round(16*pageScale);
		final int fontSizeSmall=Math.round(10*pageScale);
		final int textHeight=fontMetrics.getLineHeight(fontSize,false);
		final int textHeightBold=fontMetrics.getLineHeight(fontSize,true);
		final Color lightGreen=new Color(180,255,180);
		final Color lightBlue=new Color(180,180,255);

		/* Zeitpunkte-Boxen ausgeben */
		for (var time: data.getTimes()) {
			drawBox(time,Color.LIGHT_GRAY);
			final String text=TimeTools.formatExactLongTime(time.time,3);
			final int boxX=Math.round(time.position.x*pageScale);
			final int boxY=pageSize.height-Math.round(time.position.y*pageScale);
			final int boxWidth=Math.round(time.size.width*pageScale);
			final int boxHeight=Math.round(time.size.height*pageScale);
			final int textWidth=fontMetrics.getTextWidth(text,fontSize,true);
			setColor(Color.BLACK);
			writeTextObject(text,fontSize,true,boxX+(boxWidth-textWidth)/2,boxY-(boxHeight-textHeightBold)/2);
		}

		/* Ereignis-Boxen ausgeben */
		for (var event: data.getEvents()) {
			drawBox(event,(event.clientNr>=0)?lightGreen:lightBlue);
			final String[] text=getEventText(event);
			final int boxX=Math.round(event.position.x*pageScale);
			final int boxY=pageSize.height-Math.round(event.position.y*pageScale);
			writeTextObject(text[0],fontSize,true,boxX+1,boxY);
			writeTextObject(text[1],fontSize,false,boxX+1,boxY-textHeightBold);
			writeTextObject(text[2],fontSizeSmall,false,boxX+1,boxY-textHeightBold-textHeight);
		}

		/* Kunden-Boxen ausgeben */
		for (var client: data.getClients()) {
			drawBox(client,(client.nr>=0)?lightGreen:lightBlue);
			final String text=(client.nr>=0)?String.format(Language.tr("LogAnalyzer.Client")+" %d",client.nr):Language.tr("LogAnalyzer.Global");
			final int boxX=Math.round(client.position.x*pageScale);
			final int boxY=pageSize.height-Math.round(client.position.y*pageScale);
			final int boxWidth=Math.round(client.size.width*pageScale);
			final int boxHeight=Math.round(client.size.height*pageScale);
			final int textWidth=fontMetrics.getTextWidth(text,fontSize,false);
			writeTextObjectRotated(text,fontSize,false,boxX+(boxWidth-textHeight)/2,boxY-boxHeight+(boxHeight-textWidth)/2,Math.PI/2);
		}

		/* Verbindungspfeile */
		final int ARROW_LENGTH=50;
		for (var edge: data.getEdges()) {
			final Point p1=new Point(edge.event1.position.x+edge.event1.size.width,edge.event1.position.y+edge.event1.size.height/2);
			final Point p2=new Point(edge.event2.position.x,edge.event2.position.y+edge.event2.size.height/2);
			drawLine(p1,p2,Color.BLACK);
			final double alpha=Math.atan(((double)p2.y-p1.y)/((double)p2.x-p1.x));
			final Point delta1=new Point((int)Math.round(Math.cos(alpha+Math.PI/4)*ARROW_LENGTH*pageScale),(int)Math.round(Math.sin(alpha+Math.PI/4)*ARROW_LENGTH*pageScale));
			drawLine(p2,new Point(p2.x-delta1.x,p2.y-delta1.y),Color.BLACK);
			final Point delta2=new Point((int)Math.round(Math.cos(alpha-Math.PI/4)*ARROW_LENGTH*pageScale),(int)Math.round(Math.sin(alpha-Math.PI/4)*ARROW_LENGTH*pageScale));
			drawLine(p2,new Point(p2.x-delta2.x,p2.y-delta2.y),Color.BLACK);
		}

		/* Dekorationslinien */
		for (var line: data.getLines()) {
			drawDashedLine(line.p1,line.p2,Color.LIGHT_GRAY);
		}
	}

	/**
	 * Speichert die Darstellung in der im Konstruktor angegebenen Datei.
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public boolean save() {
		if (!systemOK) return false;
		draw();
		return save(file);
	}
}
