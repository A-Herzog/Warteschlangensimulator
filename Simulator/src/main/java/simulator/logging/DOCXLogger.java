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
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import mathtools.NumberTools;
import simcore.SimData;
import simcore.logging.AbstractTextLogger;
import simcore.logging.SimLogging;

/**
 * Schreibt die Logging-Daten in eine DOCX-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 * @see SimData#getLogger
 */
public class DOCXLogger implements SimLogging {
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
	/** Klassennamen der Event-Objekte ausgeben? */
	private final boolean printClassNames;
	/** Zeitpunkt des letzten Ereignisses (zur Gruppierung von Ereignissen) */
	private long lastEventTime=-1;

	/** Ausgabe-Dokument */
	private final XWPFDocument doc;
	/** Aktueller Absatz */
	private XWPFParagraph paragraph=null;

	/** Nachgeschalteter zweiter Logger, an den alle Logging-Daten ebenfalls übergeben werden. */
	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klasse <code>DOCXLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param formatedTime	Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param printClassNames	Klassennamen der Event-Objekte ausgeben?
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public DOCXLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final boolean formatedTime, final boolean printIDs, final boolean printClassNames, final String[] headings) {
		this.logFile=logFile;
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;
		this.formatedTime=formatedTime;
		this.printIDs=printIDs;
		this.printClassNames=printClassNames;

		String[] h;
		if (headings==null || headings.length==0) h=new String[]{"Simulationsergebnisse"}; else h=headings;

		doc=new XWPFDocument();

		XWPFRun r;
		r=doc.createParagraph().createRun();
		r.setBold(true);
		r.setFontSize(18);
		r.setText(h[0]);
		for (int i=1;i<h.length;i++) {
			r=doc.createParagraph().createRun();
			r.setBold(true);
			r.setFontSize(15);
			r.setText(h[i]);
		}
	}


	@Override
	public boolean ready() {
		return true;
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		final String timeString=formatedTime?SimData.formatSimTime(time):NumberTools.formatNumber(time/1000.0);

		/* Abschnitt beginnen / beenden */
		if (groupSameTimeEvents) {
			if (lastEventTime!=time) {
				XWPFRun r=doc.createParagraph().createRun();
				r.setBold(true);
				r.setFontSize(15);
				r.setText(timeString);
				paragraph=null;
				lastEventTime=time;
			}
		}

		/* Daten ausgeben */
		if (singleLineMode) {
			if (paragraph==null) paragraph=doc.createParagraph();
			final XWPFRun r=paragraph.createRun();
			r.setFontSize(11);
			if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
			StringBuilder sb=new StringBuilder();
			if (!groupSameTimeEvents) sb.append(timeString+" ");
			if (event!=null && !event.isEmpty()) sb.append(event+" ");
			if (printIDs && id>=0) sb.append("id="+id+" ");
			if (info!=null && !info.isEmpty()) sb.append(info+" ");
			r.setText(sb.toString());
			paragraph.createRun().addBreak();
		} else {
			paragraph=doc.createParagraph();
			if (!groupSameTimeEvents) {
				final XWPFRun r=paragraph.createRun();
				r.setFontSize(11);
				if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
				r.setText(timeString);
				paragraph.createRun().addBreak();
			}
			if (printClassNames) {
				final String eventObject=AbstractTextLogger.getCallingEventObject();
				if (eventObject!=null && !eventObject.isEmpty()) {
					final XWPFRun r=paragraph.createRun();
					r.setFontSize(11);
					if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
					r.setBold(true);
					r.setText(eventObject);
					paragraph.createRun().addBreak();
				}
			}
			if (event!=null && !event.isEmpty()) {
				final XWPFRun r=paragraph.createRun();
				r.setFontSize(11);
				if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
				r.setBold(true);
				r.setText(event);
				paragraph.createRun().addBreak();
			}
			if (printIDs && id>=0) {
				final XWPFRun r=paragraph.createRun();
				r.setFontSize(11);
				if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
				r.setBold(true);
				r.setText("id="+id);
				paragraph.createRun().addBreak();
			}
			if (info!=null && !info.isEmpty()) {
				final XWPFRun r=paragraph.createRun();
				r.setFontSize(11);
				if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
				r.setText(info);
			}
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);

		return true;
	}

	@Override
	public boolean done() {
		if (nextLogger!=null) nextLogger.done();

		try {
			try (FileOutputStream out=new FileOutputStream(logFile)) {
				doc.write(out);
			}
		} catch (IOException e) {return false;}

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
