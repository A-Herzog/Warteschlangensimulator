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

import language.Language;
import loganalyzer.LogAnalyzer.Event;
import mathtools.Table;
import mathtools.TimeTools;

/**
 * Exportiert verarbeitete Simulations-Log-Daten als Text.
 * @see LogAnalyzer
 */
public class LogAnalyzerTextExporter implements LogAnalyzerExporter {
	/**
	 * Sammlung der auszugebenden Zeilen
	 */
	private final StringBuilder result;

	/**
	 * Konstruktor
	 */
	public LogAnalyzerTextExporter() {
		result=new StringBuilder();
	}

	@Override
	public void setTime(final double time) {
		result.append("\n"+TimeTools.formatExactLongTime(time,3)+":\n");
	}

	@Override
	public void setClient(final long clientNr) {
		if (clientNr<=0) result.append("\n"+Language.tr("LogAnalyzer.Global")+":\n"); else result.append("\n"+Language.tr("LogAnalyzer.Client")+" "+clientNr+":\n");
	}

	@Override
	public void exportEvent(final Event event) {
		result.append("  "+event.name+" "+Language.tr("LogAnalyzer.StationID")+"="+event.stationName+" (id="+event.stationID+") "+Language.tr("LogAnalyzer.ClientNumber")+"="+event.clientNr+" ["+event.description+"]\n");
	}

	/**
	 * Liefert die auszugebenden Zeilen.
	 * @return	Auszugebende Zeilen
	 */
	public String getText() {
		return result.toString();
	}

	/**
	 * Schreibt die Ergebniszeilen direkt in eine Datei.
	 * @param outputFile	Ausgabedatei
	 * @return	Liefert <code>true</code>, wenn die Ausgabe erfolgreich war.
	 */
	public boolean save(final File outputFile) {
		return Table.saveTextToFile(getText(),outputFile);
	}
}
