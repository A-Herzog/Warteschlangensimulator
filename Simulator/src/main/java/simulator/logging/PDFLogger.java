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

import mathtools.NumberTools;
import simcore.SimData;
import simcore.logging.SimLogging;
import systemtools.statistics.PDFWriter;

/**
 * Schreibt die Logging-Daten in eine PDF-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 * @see SimData#getLogger
 */
public class PDFLogger implements SimLogging {
	private final File logFile;
	private final boolean groupSameTimeEvents;
	private final boolean singleLineMode;
	private final boolean useColors;
	private final boolean formatedTime;
	private final boolean printIDs;
	private long lastEventTime=-1;

	private final PDFWriter pdf;

	private SimLogging nextLogger;

	/**
	 * Konstruktor der Klasse <code>PDFLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param formatedTime	Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public PDFLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final boolean formatedTime, final boolean printIDs, final String[] headings) {
		this.logFile=logFile;
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;
		this.formatedTime=formatedTime;
		this.printIDs=printIDs;

		String[] h;
		if (headings==null || headings.length==0) h=new String[]{"Simulationsergebnisse"}; else h=headings;

		pdf=new PDFWriter(null,15,10);

		if (pdf.systemOK) {
			pdf.writeText(h[0],15,true,0);
			for (int i=1;i<h.length;i++) pdf.writeText(h[i],12,true,0);
		}
	}


	@Override
	public boolean ready() {
		return pdf.systemOK;
	}

	@Override
	public boolean log(final long time, final Color color, final String event, final int id, final String info) {
		final String timeString=formatedTime?SimData.formatSimTime(time):NumberTools.formatNumber(time/1000.0);

		/* Abschnitt beginnen / beenden */
		if (groupSameTimeEvents) {
			if (lastEventTime!=time) {
				pdf.writeEmptySpace(10);
				pdf.writeText(timeString,12,true,0,(useColors?Color.BLACK:null));
				pdf.writeEmptySpace(5);
				lastEventTime=time;
			}
		}

		Color textColor=(useColors)?((color==null)?Color.BLACK:color):null;

		/* Daten ausgeben */
		if (singleLineMode) {
			final StringBuilder sb=new StringBuilder();
			if (!groupSameTimeEvents) sb.append(timeString+" ");
			if (event!=null && !event.isEmpty()) sb.append(event+" ");
			if (printIDs && id>=0) sb.append("id="+id+" ");
			if (info!=null && !info.isEmpty()) sb.append(info+" ");
			pdf.writeText(sb.toString(),11,false,0,textColor);
		} else {
			pdf.writeEmptySpace(5);
			if (!groupSameTimeEvents) pdf.writeText(timeString,11,false,0,textColor);
			if (event!=null && !event.isEmpty()) pdf.writeText(event,11,true,0,textColor);
			if (printIDs && id>=0) pdf.writeText("id="+id,11,true,0,textColor);
			if (info!=null && !info.isEmpty()) pdf.writeText(info,11,false,0,textColor);
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);

		return true;
	}

	@Override
	public boolean done() {
		pdf.save(logFile);
		if (nextLogger!=null) nextLogger.done();
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
