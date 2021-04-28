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
package scripting.java;

/**
 * Teil-Interface, damit Nutzer-Java-Codes Ausgaben vornehmen kann.
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public interface OutputInterface {

	/**
	 * Stellt das Ausgabeformat für Zahlen ein
	 * @param format	Zeichenkette, über die das Format (Dezimalkomma sowie optional Prozentwert) für Zahlenausgaben festgelegt wird
	 */
	void setFormat(final String format);

	/**
	 * Stellt ein, welches Trennzeichen zwischen den Werten bei der Ausgabe von Arrays verwendet werden soll
	 * @param separator	Bezeichner für das Trennzeichen
	 */
	void setSeparator(final String separator);

	/**
	 * Stellt ein, wie viele Nachkommastellen bei der Ausgabe von Zahlen lokaler Notation ausgegeben werden sollen.
	 * @param digits	Nachkommastellen bei der Ausgabe von Zahlen lokaler Notation
	 */
	void setDigits(final int digits);

	/**
	 * Gibt einen String oder eine Zahl aus (ohne folgenden Zeilenumbruch)
	 * @param obj	Auszugebendes Objekt
	 */
	void print(final Object obj);

	/**
	 * Gibt einen String oder eine Zahl mit folgendem Zeilenumbruch aus
	 * @param obj	Auszugebendes Objekt
	 */
	void println(final Object obj);

	/**
	 * Gibt einen Zeilenumbruch aus
	 */
	void newLine();

	/**
	 * Gibt einen Tabulator aus
	 */
	void tab();

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden Dateiausgaben nicht mehr ausgeführt.)
	 */
	void cancel();

	/**
	 * Stellt ein, in welche Datei die Ausgabe erfolgen soll
	 * @param file	Ausgabedatei
	 */
	void setFile(final Object file);

	/**
	 * Gibt einen String oder eine Zahl über eine DDE-Verbindung aus.
	 * @param workbook	Ziel-Excel-Arbeitsmappe
	 * @param table	Ziel-Excel-Tabelle in der Arbeitsmappe
	 * @param cell	Ziel-Excel-Zelle in der Tabelle
	 * @param obj	Auszugebendes Objekt
	 * @return	Gibt an, ob der Zellenwert an Excel übermittelt werden konnte
	 */
	boolean printlnDDE(final String workbook, final String table, final String cell, final Object obj);
}
