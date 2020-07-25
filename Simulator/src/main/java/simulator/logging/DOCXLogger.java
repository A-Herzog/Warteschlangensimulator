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
import simcore.logging.SimLogging;

/**
 * Schreibt die Logging-Daten in eine DOCX-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 * @see SimData#getLogger
 */
public class DOCXLogger implements SimLogging {
	private final File logFile;
	private final boolean groupSameTimeEvents;
	private final boolean singleLineMode;
	private final boolean useColors;
	private final boolean formatedTime;
	private long lastEventTime=-1;

	private final XWPFDocument doc;
	private XWPFParagraph paragraph=null;

	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klasse <code>DOCXLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param formatedTime	Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public DOCXLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final boolean formatedTime, final String[] headings) {
		this.logFile=logFile;
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;
		this.formatedTime=formatedTime;

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
	public boolean log(long time, Color color, String event, String info) {
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
			XWPFRun r=paragraph.createRun();
			r.setFontSize(11);
			if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
			StringBuilder sb=new StringBuilder();
			if (!groupSameTimeEvents) sb.append(timeString+" ");
			if (event!=null && !event.isEmpty()) sb.append(event+" ");
			if (info!=null && !info.isEmpty()) sb.append(info+" ");
			r.setText(sb.toString());
			paragraph.createRun().addBreak();
		} else {
			paragraph=doc.createParagraph();
			if (!groupSameTimeEvents) {
				XWPFRun r=paragraph.createRun();
				r.setFontSize(11);
				if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
				r.setText(timeString);
				paragraph.createRun().addBreak();
			}
			if (event!=null && !event.isEmpty()) {
				XWPFRun r=paragraph.createRun();
				r.setFontSize(11);
				if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
				r.setBold(true);
				r.setText(event);
				paragraph.createRun().addBreak();
			}
			if (info!=null && !info.isEmpty()) {
				XWPFRun r=paragraph.createRun();
				r.setFontSize(11);
				if (useColors && color!=null && !color.equals(Color.BLACK)) r.setColor(String.format("%02x%02x%02x",color.getRed(),color.getGreen(),color.getBlue()));
				r.setText(info);
			}
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,info);

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
