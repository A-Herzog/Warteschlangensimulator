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
package simcore.logging;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import mathtools.NumberTools;
import simcore.SimData;

/**
 * Schreibt die Logging-Daten in eine RTF-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 */
public class RTFLogger extends AbstractTextLogger {
	private final boolean groupSameTimeEvents;
	private final boolean singleLineMode;
	private final boolean useColors;
	private final boolean formatedTime;
	private final boolean printIDs;
	private final String[] headings;
	private long lastEventTime=-1;

	private final Map<Color,Integer> colorNumbers;
	private final StringBuilder buffer;


	/**
	 * Konstruktor der Klasse <code>RTFLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param formatedTime	Zeit als HH:MM:SS,s (<code>true</code>) oder als Sekunden-Zahlenwert (<code>false</code>) ausgeben
	 * @param printIDs	IDs mit ausgeben
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public RTFLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final boolean formatedTime, final boolean printIDs, final String[] headings) {
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;
		this.formatedTime=formatedTime;
		this.printIDs=printIDs;
		if (headings==null || headings.length==0) this.headings=new String[]{"Simulationsergebnisse"}; else this.headings=headings;
		init(logFile);
		if (ready()) {
			buffer=new StringBuilder();
			colorNumbers=new HashMap<>();
		} else {
			buffer=null;
			colorNumbers=null;
		}
	}

	@Override
	protected void writeHeader() {
		writeString("{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl\\f0\\fswiss Helvetica;}\\f0\n");
		writeString("\\fs34 "+convertLineToRTF(headings[0])+"\\fs22\\par\n");
		for (int i=1;i<headings.length;i++) writeString("\\fs28 "+convertLineToRTF(headings[i])+"\\fs22\\par\n");
	}

	@Override
	protected void writeFooter() {
		if (buffer!=null) {
			writeString("{\\colortbl ;");
			for (int i=1;i<=colorNumbers.size();i++) for (Map.Entry<Color,Integer> entry: colorNumbers.entrySet()) {
				if (entry.getValue()==i) writeString("\\red"+entry.getKey().getRed()+"\\green"+entry.getKey().getGreen()+"\\blue"+entry.getKey().getBlue()+";");
			}
			writeString("}\n");
			writeString(buffer.toString());
		}
		writeString("\n}\n");
	}

	private char hex(final int b) {
		if (b<10) return (char)(b+((short)'0')); else return (char)(b-10+((short)'a'));
	}

	private String convertLineToRTF(String s) {
		StringBuilder result=new StringBuilder();
		for (int i=0;i<s.length();i++) {
			char c=s.charAt(i);
			if ((short)c<=127) {result.append(c); continue;}
			short b=(short)c;

			result.append("\\'");
			result.append(hex(b/16));
			result.append(hex(b%16));

		}
		return result.toString();
	}

	private int getColorNumber(final Color color) {
		if (colorNumbers==null) return 0;
		Integer I=colorNumbers.get(color);
		if (I!=null) return I;
		int i=colorNumbers.size()+1;
		colorNumbers.put(color,i);
		return i;
	}

	@Override
	public boolean log(long time, Color color, String event, final int id, String info) {
		/* Abschnitt beginnen / beenden */
		if (groupSameTimeEvents) {
			if(lastEventTime!=time) {
				if (lastEventTime>=0) buffer.append("\\par\n");
				if (useColors) buffer.append("\\cf0\\highlight0");
				buffer.append("\\fs28 "+convertLineToRTF(formatedTime?SimData.formatSimTime(time):NumberTools.formatNumber(time/1000.0))+"\\fs22\\par\n");
				lastEventTime=time;
			}
		}

		/* Farbe einstellen */
		if (useColors && color!=null && !color.equals(Color.BLACK)) {
			buffer.append("\\highlight"+getColorNumber(color));
			buffer.append("\\cf"+getColorNumber(new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue())));
		}

		/* Daten ausgeben */
		if (singleLineMode) {
			if (!groupSameTimeEvents) buffer.append(convertLineToRTF(SimData.formatSimTime(time))+"\\tab\n");
			if (event!=null && !event.isEmpty()) buffer.append(convertLineToRTF(event)+"\\tab\n");
			if (printIDs) buffer.append(convertLineToRTF((id>=0)?(""+id):"")+"\\tab\n");
			if (info!=null && !info.isEmpty()) buffer.append(convertLineToRTF(info)+" ");
			buffer.append("\\line\n");
		} else {
			if (!groupSameTimeEvents) buffer.append(convertLineToRTF(SimData.formatSimTime(time))+"\\line\n");
			if (event!=null && !event.isEmpty()) buffer.append(convertLineToRTF(event)+"\\line\n");
			if (printIDs) buffer.append(convertLineToRTF((id>=0)?(""+id):"")+"\\line\n");
			if (info!=null && !info.isEmpty()) buffer.append(convertLineToRTF(info)+"\\line\n");
			buffer.append("\\par\n");
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,id,info);

		return true;
	}
}
