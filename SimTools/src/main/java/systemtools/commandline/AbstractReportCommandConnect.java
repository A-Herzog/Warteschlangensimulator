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
package systemtools.commandline;

import java.io.File;

/**
 * Über die in diesem Interface definierten Methoden kann der Kommandozeilenbefehl zur
 * Reportgenerierung auf die entsprechende Funktionalität der Report-Generator-Funktion zugreifen.
 * @author Alexander Herzog
 */
public interface AbstractReportCommandConnect {

	/**
	 * Ruft den HTML-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param output	Dateiname, in der der HTML-Report gespeichert werden soll.
	 * @param inline	Gibt an, ob die Grafiken direkt in die HTML-Datei eingebettet werden sollen.
	 * @param exportAllItems	Wird dieser Parameter auf <code>true</code> gesetzt, so werden die Einstellungen, welche Daten exportiert werden sollen, ignoriert und es werden alle verfügbaren Daten in den Report geschrieben.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	boolean runReportGeneratorHTML(File output, boolean inline, boolean exportAllItems);

	/**
	 * Ruft den HTML-WebApp-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param output	Dateiname, in der der HTML-Report gespeichert werden soll.
	 * @param exportAllItems	Wird dieser Parameter auf <code>true</code> gesetzt, so werden die Einstellungen, welche Daten exportiert werden sollen, ignoriert und es werden alle verfügbaren Daten in den Report geschrieben.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	boolean runReportGeneratorHTMLApp(File output, boolean exportAllItems);

	/**
	 * Ruft den DOCX-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param output	Dateiname, in der der DOCX-Report gespeichert werden soll.
	 * @param exportAllItems	Wird dieser Parameter auf <code>true</code> gesetzt, so werden die Einstellungen, welche Daten exportiert werden sollen, ignoriert und es werden alle verfügbaren Daten in den Report geschrieben.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	boolean runReportGeneratorDOCX(File output, boolean exportAllItems);

	/**
	 * Ruft den PDF-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param output	Dateiname, in der der PDF-Report gespeichert werden soll.
	 * @param exportAllItems	Wird dieser Parameter auf <code>true</code> gesetzt, so werden die Einstellungen, welche Daten exportiert werden sollen, ignoriert und es werden alle verfügbaren Daten in den Report geschrieben.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	boolean runReportGeneratorPDF(File output, boolean exportAllItems);

	/**
	 * Ruft den LaTeX-Report-Generator auf und speichert den Report in der angegebenen Form in der angegebenen Datei.
	 * @param output	Dateiname, in der der LaTeX-Report gespeichert werden soll.
	 * @param exportAllItems	Wird dieser Parameter auf <code>true</code> gesetzt, so werden die Einstellungen, welche Daten exportiert werden sollen, ignoriert und es werden alle verfügbaren Daten in den Report geschrieben.
	 * @return	Gibt an, ob der Report erfolgreich erstellt werden konnte.
	 */
	boolean runReportGeneratorLaTeX(File output, boolean exportAllItems);

	/**
	 * Speichert eine Liste aller Einträge im Statistikbaum als Datei
	 * @param output	Dateiname, in der die Liste gespeichert werden soll.
	 * @return	Gibt an, ob die Liste erfolgreich gespeichert werden konnte.
	 */
	boolean getReportList(File output);

	/**
	 * Speichert ein einzelnes Dokument aus dem Statisitikbaum als Datei
	 * @param output	Dateiname, in dem das Dokument gespeichert werden soll.
	 * @param entry	Gibt den Namen des Dokuments im Statistikbaum an.
	 * @return	Gibt an, ob das Dokument erfolgreich gespeichert werden konnte.
	 */
	boolean getReportListEntry(File output, String entry);
}
