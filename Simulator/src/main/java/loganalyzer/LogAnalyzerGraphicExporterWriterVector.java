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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.eps.EPSProcessor;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
import de.erichseifert.vectorgraphics2d.util.PageSize;
import language.Language;
import mathtools.TimeTools;
import ui.modeleditor.elements.FontCache;

/**
 * Speichert ein von {@link LogAnalyzerGraphicExporter} erstelltes
 * abstraktes Grafikmodell einer Log-Datei in einer eps- oder svg-Datei.
 * @see LogAnalyzerGraphicExporter
 */
public class LogAnalyzerGraphicExporterWriterVector {
	/**
	 * Ausgabegrafikformaz
	 */
	public enum VectorFormat {
		/** EPS-Datei */
		EPS,
		/** SVG-Datei */
		SVG
	}

	/**
	 * Auszugebendes abstrakte Logdatei-Grafik
	 */
	private final LogAnalyzerGraphicExporter data;

	/**
	 * Zieldatei (im svg-Format)
	 */
	private final File file;

	/**
	 * Zu verwendendes Ausgabegrafikformat
	 */
	private final VectorFormat format;

	/**
	 * Konstruktor
	 * @param data	Auszugebendes abstrakte Logdatei-Grafik
	 * @param file	Zieldatei (im eps- oder svg-Format)
	 * @param format	Zu verwendendes Ausgabegrafikformat
	 */
	public LogAnalyzerGraphicExporterWriterVector(final LogAnalyzerGraphicExporter data, final String file, final VectorFormat format) {
		this(data,new File(file),format);
	}

	/**
	 * Konstruktor
	 * @param data	Auszugebendes abstrakte Logdatei-Grafik
	 * @param file	Zieldatei (im eps- oder svg-Format)
	 * @param format	Zu verwendendes Ausgabegrafikformat
	 */
	public LogAnalyzerGraphicExporterWriterVector(final LogAnalyzerGraphicExporter data, final File file, final VectorFormat format) {
		this.data=data;
		this.file=file;
		this.format=format;
	}

	/**
	 * Ermittelt die notwendige Seitengröße gemessen in der
	 * abstrakten Einheit aus {@link LogAnalyzerGraphicExporter}.
	 * @return	Notwendige Seitengröße um alle Elemente darstellen zu können
	 */
	private Dimension getPageSize() {
		final var events=data.getEvents();
		return new Dimension(
				events.stream().mapToInt(event->event.position.x+event.size.width).max().orElse(1),
				events.stream().mapToInt(event->event.position.y+event.size.height).max().orElse(1));
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
	 * Gibt eine gefüllte, schwarz umrandete Box aus
	 * @param graphics	Zeichenfläche
	 * @param box	Zugehöriges Log-Analyse-Element aus dem die Maße der Box entnommen werden sollen
	 * @param fillColor	Füllfarbe (kann <code>null</code> sein, dann erfolgt keine Füllung)
	 */
	private void drawBox(final Graphics2D graphics, final LogAnalyzerGraphicExporterBox box, final Color fillColor) {
		if (fillColor!=null) {
			graphics.setColor(fillColor);
			graphics.fillRect(box.position.x,box.position.y,box.size.width,box.size.height);
		}
		graphics.setColor(Color.BLACK);
		graphics.drawRect(box.position.x,box.position.y,box.size.width,box.size.height);
	}

	/**
	 * Zeichnet die Elemente in ein {@link Graphics2D}-Objekt ein.
	 * @param graphics	Zeichenfläche
	 * @see #save()
	 */
	private void paintElements(final Graphics2D graphics) {
		final Color lightGreen=new Color(180,255,180);
		final Color lightBlue=new Color(180,180,255);
		final Font fontBold=FontCache.getFontCache().getFont(FontCache.defaultFamily,Font.BOLD,16);
		final Font fontNormal=FontCache.getFontCache().getFont(FontCache.defaultFamily,0,16);
		final Font fontSmall=FontCache.getFontCache().getFont(FontCache.defaultFamily,0,9);

		FontMetrics fontMetrics;

		/* Zeitpunkte-Boxen ausgeben */
		graphics.setFont(fontBold);
		fontMetrics=graphics.getFontMetrics();
		int lineHeight=fontMetrics.getAscent()+fontMetrics.getDescent();
		for (var time: data.getTimes()) {
			drawBox(graphics,time,Color.LIGHT_GRAY);
			final String text=TimeTools.formatExactLongTime(time.time,3);
			final int x=time.position.x+(time.size.width-fontMetrics.stringWidth(text))/2;
			final int y=time.position.y+(time.size.height-lineHeight)/2+fontMetrics.getAscent();
			graphics.drawString(text,x,y);
		}

		/* Ereignis-Boxen ausgeben */
		for (var event: data.getEvents()) {
			drawBox(graphics,event,(event.clientNr>=0)?lightGreen:lightBlue);
			final String[] text=getEventText(event);
			int y=event.position.y+1;
			graphics.setFont(fontBold);
			graphics.drawString(text[0],event.position.x+1,y+graphics.getFontMetrics().getAscent());
			y+=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
			graphics.setFont(fontNormal);
			graphics.drawString(text[1],event.position.x+1,y+graphics.getFontMetrics().getAscent());
			y+=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
			graphics.setFont(fontSmall);
			graphics.drawString(text[2],event.position.x+1,y+graphics.getFontMetrics().getAscent());
			y+=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
		}

		/* Kunden-Boxen ausgeben */
		final AffineTransform affineTransform=new AffineTransform();
		affineTransform.rotate(-Math.PI/2,0,0);
		final Font rotatedFont=fontNormal.deriveFont(affineTransform);
		graphics.setFont(rotatedFont);
		fontMetrics=graphics.getFontMetrics();
		lineHeight=graphics.getFontMetrics(fontNormal).getAscent()+graphics.getFontMetrics().getDescent();
		final int ascent=graphics.getFontMetrics(fontNormal).getAscent();
		for (var client: data.getClients()) {
			drawBox(graphics,client,(client.nr>=0)?lightGreen:lightBlue);
			final String text=(client.nr>=0)?String.format(Language.tr("LogAnalyzer.Client")+" %d",client.nr):Language.tr("LogAnalyzer.Global");
			final int textWidth=graphics.getFontMetrics(fontNormal).stringWidth(text);
			graphics.drawString(text,client.position.x+(client.size.width-lineHeight)/2+ascent,client.position.y+client.size.height-(client.size.height-textWidth)/2);
		}

		/* Verbindungspfeile */
		final int ARROW_LENGTH=25;
		graphics.setColor(Color.BLACK);
		for (var edge: data.getEdges()) {
			final Point p1=new Point(edge.event1.position.x+edge.event1.size.width,edge.event1.position.y+edge.event1.size.height/2);
			final Point p2=new Point(edge.event2.position.x,edge.event2.position.y+edge.event2.size.height/2);
			graphics.drawLine(p1.x,p1.y,p2.x,p2.y);
			final double alpha=Math.atan(((double)p2.y-p1.y)/((double)p2.x-p1.x));
			final Point delta1=new Point((int)Math.round(Math.cos(alpha+Math.PI/4)*ARROW_LENGTH),(int)Math.round(Math.sin(alpha+Math.PI/4)*ARROW_LENGTH));
			graphics.drawLine(p2.x,p2.y,p2.x-delta1.x,p2.y-delta1.y);
			final Point delta2=new Point((int)Math.round(Math.cos(alpha-Math.PI/4)*ARROW_LENGTH),(int)Math.round(Math.sin(alpha-Math.PI/4)*ARROW_LENGTH));
			graphics.drawLine(p2.x,p2.y,p2.x-delta2.x,p2.y-delta2.y);
		}

		/* Dekorationslinien */
		graphics.setColor(Color.LIGHT_GRAY);
		for (var line: data.getLines()) {
			graphics.setStroke(new BasicStroke(3,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{10,10},0));
			graphics.drawLine(line.p1.x,line.p1.y,line.p2.x,line.p2.y);
		}
	}

	/**
	 * Speichert die Darstellung in der im Konstruktor angegebenen Datei im svg-Format.
	 * @param pageSize	Seitengröße
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 * @see #save()
	 */
	private boolean saveSVG(final Dimension pageSize) {
		final DOMImplementation domImpl=GenericDOMImplementation.getDOMImplementation();
		final Document document=domImpl.createDocument("http://www.w3.org/2000/svg","svg",null);
		final SVGGraphics2D svgGenerator=new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize(pageSize);

		paintElements(svgGenerator);

		try (FileOutputStream fileWriter=new FileOutputStream(file)) {
			final Writer out=new OutputStreamWriter(fileWriter,StandardCharsets.UTF_8);
			svgGenerator.stream(out,true);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Speichert die Darstellung in der im Konstruktor angegebenen Datei im eps-Format.
	 * @param pageSize	Seitengröße
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 * @see #save()
	 */
	private boolean saveEPS(final Dimension pageSize) {
		final Graphics2D vg2d=new VectorGraphics2D();

		paintElements(vg2d);

		final CommandSequence commands=((VectorGraphics2D)vg2d).getCommands();
		final EPSProcessor epsProcessor=new EPSProcessor();
		final de.erichseifert.vectorgraphics2d.Document doc=epsProcessor.getDocument(commands,new PageSize(pageSize.width,pageSize.height));
		try (FileOutputStream stream=new FileOutputStream(file)) {
			doc.writeTo(stream);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Speichert die Darstellung in der im Konstruktor angegebenen Datei.
	 * @return	Gibt an, ob das Speichern erfolgreich war.
	 */
	public boolean save() {
		final Dimension pageSize=getPageSize();

		switch (format) {
		case SVG: return saveSVG(pageSize);
		case EPS: return saveEPS(pageSize);
		default: return false;
		}
	}
}
