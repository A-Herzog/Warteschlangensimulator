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
package simulator.logging;

import java.awt.Color;
import java.io.File;

import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.text.Paragraph;

import mathtools.NumberTools;
import simcore.SimData;
import simcore.logging.SimLogging;

/**
 * Schreibt die Logging-Daten in eine ODT-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 * @see SimData#getLogger
 */
public class ODTLogger implements SimLogging {
	/** Dateiname der Logfile-Datei */
	private final File logFile;
	/** Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen */
	private final boolean groupSameTimeEvents;
	/** Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben */
	private final boolean singleLineMode;
	/** Bei den Log-Zeilen angegebene Farben berücksichtigen */
	private final boolean useColors;
	/** Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben */
	private final boolean formatedTime;
	/** IDs mit ausgeben */
	private final boolean printIDs;
	/** Zeitpunkt des letzten Ereignisses (zur Gruppierung von Ereignissen) */
	private long lastEventTime=-1;

	/** Ausgabe-Dokument */
	private final TextDocument odt;
	/** Aktueller Absatz */
	private Paragraph paragraph=null;

	/** Nachgeschalteter zweiter Logger, an den alle Logging-Daten ebenfalls übergeben werden. */
	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klasse <code>ODTLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param formatedTime	Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public ODTLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final boolean formatedTime, final boolean printIDs, final String[] headings) {
		this.logFile=logFile;
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;
		this.formatedTime=formatedTime;
		this.printIDs=printIDs;

		String[] h;
		if (headings==null || headings.length==0) h=new String[]{"Simulationsergebnisse"}; else h=headings;

		TextDocument odt=null;
		try {
			odt=TextDocument.newTextDocument();
		} catch (Exception e) {
			odt=null;
		}
		this.odt=odt;

		if (odt!=null) {
			final Paragraph p=odt.addParagraph(null);
			setFont(p,18,true);
			for (int i=0;i<h.length;i++) p.appendTextContent(h[i]);
		}
	}


	@Override
	public boolean ready() {
		return true;
	}

	/**
	 * Stellt Schriftgröße und Art für den Absatz ein
	 * @param p	Absatz für den die Schriftdaten eingestellt werden sollen
	 * @param size	Schriftgröße
	 * @param bold	Fette Darstellung?
	 */
	private static void setFont(final Paragraph p, final int size, final boolean bold) {
		final Font f=p.getFont();
		f.setSize(size);
		if (bold) f.setFontStyle(FontStyle.BOLD);
		p.setFont(f);
	}

	/**
	 * Stellt die Textfarbe für einen Absatz ein
	 * @param p	Absatz für den die Schriftfarbe eingestellt werden soll
	 * @param color	Farbe für die Schrift in dem Absatz
	 */
	private static void setColor(final Paragraph p, final Color color) {
		if (color==null || color.equals(Color.BLACK)) return;

		final Font f=p.getFont();
		f.setColor(new org.odftoolkit.odfdom.type.Color(color.getRed(),color.getGreen(),color.getBlue()));
		p.setFont(f);
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		if (odt==null) return false;

		final String timeString=formatedTime?SimData.formatSimTime(time):NumberTools.formatNumber(time/1000.0);

		/* Abschnitt beginnen / beenden */
		if (groupSameTimeEvents) {
			if (lastEventTime!=time) {
				final Paragraph p=odt.addParagraph(null);
				setFont(p,15,true);
				p.appendTextContent(timeString);
				paragraph=null;
				lastEventTime=time;
			}
		}

		/* Daten ausgeben */
		if (singleLineMode) {
			if (paragraph==null) paragraph=odt.addParagraph(null);
			setFont(paragraph,15,false);
			if (useColors) setColor(paragraph,color);
			StringBuilder sb=new StringBuilder();
			if (!groupSameTimeEvents) sb.append(timeString+" ");
			if (event!=null && !event.isEmpty()) sb.append(event+" ");
			if (printIDs && id>=0) sb.append("id="+id+" ");
			if (info!=null && !info.isEmpty()) sb.append(info+" ");
			paragraph.appendTextContent(sb.toString());
			paragraph.appendTextContent("\n");
		} else {
			paragraph=odt.addParagraph(null);
			setFont(paragraph,11,false);
			if (!groupSameTimeEvents) {
				if (useColors) setColor(paragraph,color);
				paragraph.appendTextContent(timeString);
				paragraph.appendTextContent("\n");
			}
			if (event!=null && !event.isEmpty()) {
				if (useColors) setColor(paragraph,color);
				paragraph.appendTextContent(event);
				paragraph.appendTextContent("\n");
			}
			if (printIDs && id>=0) {
				if (useColors) setColor(paragraph,color);
				paragraph.appendTextContent("id="+id);
				paragraph.appendTextContent("\n");
			}
			if (info!=null && !info.isEmpty()) {
				if (useColors) paragraph.getFont().setColor(new org.odftoolkit.odfdom.type.Color(color.getRed(),color.getGreen(),color.getBlue()));
				paragraph.appendTextContent(info);
			}
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);

		return true;
	}

	@Override
	public boolean done() {
		if (nextLogger!=null) nextLogger.done();

		if (odt!=null) try {
			odt.save(logFile);
			odt.close();
		} catch (Exception e) {return false;}

		return true;
	}

	@Override
	public void setNextLogger(final SimLogging logger) {
		nextLogger=logger;
	}

	@Override
	public SimLogging getNextLogger() {
		return nextLogger;
	}
}
