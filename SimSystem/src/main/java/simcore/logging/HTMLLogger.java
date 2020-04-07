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

import simcore.SimData;

/**
 * Schreibt die Logging-Daten in eine HTML-Datei.
 * @author Alexander Herzog
 * @see SimLogging
 */
public class HTMLLogger extends AbstractTextLogger {
	private final boolean groupSameTimeEvents;
	private final boolean singleLineMode;
	private final boolean useColors;
	private final String[] headings;
	private long lastEventTime=-1;

	/**
	 * Konstruktor der Klasse <code>HTMLLogger</code>
	 * @param logFile	Dateiname der Logfile-Datei
	 * @param groupSameTimeEvents	Nach Einträgen mit demselben Zeitstempel eine Leerzeile einfügen
	 * @param singleLineMode	Ereignisse in einer Zeile oder in mehreren Zeilen ausgeben
	 * @param useColors	Bei den Log-Zeilen angegebene Farben berücksichtigen
	 * @param headings	Auszugebende Überschriftzeilen
	 */
	public HTMLLogger(final File logFile, final boolean groupSameTimeEvents, final boolean singleLineMode, final boolean useColors, final String[] headings) {
		this.groupSameTimeEvents=groupSameTimeEvents;
		this.singleLineMode=singleLineMode;
		this.useColors=useColors;
		if (headings==null || headings.length==0) this.headings=new String[]{"Simulationsergebnisse"}; else this.headings=headings;
		init(logFile);
	}

	@Override
	protected void writeHeader() {
		final StringBuilder html=new StringBuilder();

		html.append("<html>"+System.lineSeparator());
		html.append("<head>"+System.lineSeparator());
		html.append("  <meta charset=\"utf-8\">"+System.lineSeparator());
		html.append("  <title>"+headings[0]+"</title>"+System.lineSeparator());
		html.append("  <meta name=\"author\" content=\"Alexander Herzog\">"+System.lineSeparator());
		html.append("  <style type=\"text/css\">"+System.lineSeparator());
		html.append("    body {font-family: Verdana, Lucida, sans-serif;}"+System.lineSeparator());
		html.append("    table {border-collapse: collapse;}"+System.lineSeparator());
		html.append("    td {border: 1px solid black; padding: 2px 5px;}"+System.lineSeparator());
		html.append("  </style>"+System.lineSeparator());
		html.append("</head>"+System.lineSeparator());
		html.append("<body>"+System.lineSeparator());
		html.append("<h1>"+headings[0]+"</h1>"+System.lineSeparator());
		if (headings.length>1) {
			html.append("<h2>");
			for (int i=1;i<headings.length;i++) {
				if (i>1) html.append("<br>");
				html.append(headings[i]);
			}
			html.append("</h2>"+System.lineSeparator());
		}

		if (singleLineMode) html.append("<table>"+System.lineSeparator()); else html.append("<ul>"+System.lineSeparator());

		writeString(html.toString());
	}

	@Override
	protected void writeFooter() {
		final StringBuilder sb=new StringBuilder();

		if (singleLineMode) sb.append("</table>"+System.lineSeparator()); else sb.append("</ul>"+System.lineSeparator());

		sb.append("</body>"+System.lineSeparator());
		sb.append("</html>"+System.lineSeparator());

		writeString(sb.toString());
	}

	private String getHTMLColor(final Color color) {
		if (!useColors || color==null) return "";
		if (color.equals(Color.BLACK)) return "";
		return String.format(" style=\"background-color:#%02x%02x%02x; color:#%02x%02x%02x;\"",color.getRed(),color.getGreen(),color.getBlue(),255-color.getRed(),255-color.getGreen(),255-color.getBlue());
	}

	@Override
	public boolean log(long time, Color color, String event, String info) {
		final StringBuilder sb=new StringBuilder();

		/* Abschnitt beginnen / beenden */
		if (groupSameTimeEvents) {
			if(lastEventTime!=time) {
				if (singleLineMode) {
					sb.append("  <tr><td colspan=\"2\" style=\"border: 0;\"><h2 style=\"margin-top: 10px; margin-bottom: 0px;\">"+SimData.formatSimTime(time)+"</h2></td></tr>"+System.lineSeparator());
				} else {
					sb.append("</ul>"+System.lineSeparator());
					sb.append("<h2>"+SimData.formatSimTime(time)+"</h2>"+System.lineSeparator());
					sb.append("<ul>"+System.lineSeparator());
				}
				lastEventTime=time;
			}
		}

		/* Daten ausgeben */
		if (singleLineMode) {
			sb.append("  <tr"+getHTMLColor(color)+">"+System.lineSeparator());
			if (!groupSameTimeEvents) {
				sb.append("    <td>");
				sb.append(SimData.formatSimTime(time));
				sb.append("</td>"+System.lineSeparator());
			}
			if (event!=null && !event.isEmpty()) {
				sb.append("    <td>");
				sb.append(event.replace("\n","<br>"));
				sb.append("</td>"+System.lineSeparator());
			}
			if (info!=null && !info.isEmpty()) {
				sb.append("    <td>");
				sb.append(info.replace("\n","<br>"));
				sb.append("</td>"+System.lineSeparator());
			}
			sb.append("  </tr>");
		} else {
			sb.append("  <li"+getHTMLColor(color)+">"+System.lineSeparator());
			if (!groupSameTimeEvents) {
				sb.append("    "+SimData.formatSimTime(time));
			}
			if (event!=null && !event.isEmpty()) {
				if (!groupSameTimeEvents) sb.append("<br>"+System.lineSeparator());
				sb.append("    "+event.replace("\n","<br>"));
			}
			if (info!=null && !info.isEmpty()) {
				sb.append("<br>"+System.lineSeparator());
				sb.append("    "+info.replace("\n","<br>"));
			}
			sb.append(System.lineSeparator());
			sb.append("  </li>");
		}

		if (nextLogger!=null) nextLogger.log(time,color,event,info);

		return writeString(sb.toString());
	}
}