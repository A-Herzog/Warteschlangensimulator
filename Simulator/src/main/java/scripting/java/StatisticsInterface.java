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
 * Implementierungsklasse f�r das Interface {@link StatisticsInterface}
 * @author Alexander Herzog
 * @see StatisticsInterface
 */
public interface StatisticsInterface {

	/**
	 * Stellt das Ausgabeformat f�r Zahlen ein
	 * @param format	Zeichenkette, �ber die das Format (z.B. Dezimalkomma sowie optional Prozentwert) f�r Zahlenausgaben festgelegt wird
	 */
	void setFormat(final String format);

	/**
	 * Stellt ein, welches Trennzeichen zwischen den Werten bei der Ausgabe von Verteilungen verwendet werden soll
	 * @param separator	Bezeichner f�r das Trennzeichen
	 */
	void setSeparator(final String separator);

	/**
	 * Liefert das Objekt, das �ber den als Parameter angegebenen XML-Pfad spezifiziert wird als Zeichenkette zur�ck
	 * @param path	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @return	Datenobjekt als Zeichenkette
	 */
	String xml(final String path);

	/**
	 * Liefert das Objekt, das �ber den als Parameter angegebenen XML-Pfad spezifiziert wird als Zahl zur�ck
	 * @param path	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @return	Datenobjekt als Double-Wert oder ein String mit einer Fehlermeldung
	 */
	Object xmlNumber(final String path);

	/**
	 * Gibt die Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist, als Array aus
	 * @param path	String, der den XML-Pfad zu der Verteilung enth�lt
	 * @return	Werte der Verteilung als Array
	 */
	Object xmlArray(final String path);

	/**
	 * Summiert die Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist auf und liefert das Ergebnis als Double-Wert
	 * @param path	String, der den XML-Pfad zu der Verteilung enth�lt
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	Object xmlSum(final String path);

	/**
	 * Bildet den Mittelwert der Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist und liefert das Ergebnis als Double-Wert
	 * @param path	String, der den XML-Pfad zu der Verteilung enth�lt
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	Object xmlMean(final String path);

	/**
	 * Bildet den Standardabweichung der Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist und liefert das Ergebnis als Double-Wert
	 * @param path	String, der den XML-Pfad zu der Verteilung enth�lt
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	Object xmlSD(final String path);

	/**
	 * Bildet den Variationskoeffizient der Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist und liefert das Ergebnis als Double-Wert
	 * @param path	String, der den XML-Pfad zu der Verteilung enth�lt
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	Object xmlCV(final String path);

	/**
	 * Speichert das XML-Objekt in einer Datei
	 * @param fileName	Dateiname
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	boolean save(final String fileName);

	/**
	 * Speichert das XML-Objekt unter dem n�chsten verf�gbaren Dateinamen
	 * in dem angegebenen Verzeichnis
	 * @param folderName	Verzeichnis, in dem die Dateien gespeichert werden sollen
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	boolean saveNext(final String folderName);

	/**
	 * Wendet das angegebene Javascript-Skript auf die Statistikdaten an und gibt das Ergebnis zur�ck.
	 * @param fileName	Skriptdateiname
	 * @return	R�ckgabewert des Skriptes
	 */
	String filter(final String fileName);

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden Dateiausgaben nicht mehr ausgef�hrt.)
	 */
	void cancel();

	/**
	 * �bersetzt die vorliegende Statistikdatei
	 * @param language	Neue Sprache ("de" oder "en")
	 * @return	Gibt an, ob das �bersetzen erfolgreich war
	 */
	boolean translate(final String language);

	/**
	 * Versucht basierend auf dem Namen einer Station die zugeh�rige ID zu ermitteln
	 * @param name	Name der Station
	 * @return	Zugeh�rige ID oder -1, wenn keine passende Station gefunden wurde
	 */
	int getStationID(final String name);

	/**
	 * Liefert den vollst�ndigen Pfad- und Dateinamen der Statistikdatei, aus der die Daten stammen.
	 * @return	Vollst�ndiger Pfad- und Dateinamen der Statistikdatei (kann leer, aber nicht <code>null</code> sein)
	 */
	String getStatisticsFile();

	/**
	 * Liefert den Dateinamen der Statistikdatei, aus der die Daten stammen.
	 * @return	Dateiname der Statistikdatei (kann leer, aber nicht <code>null</code> sein)
	 */
	String getStatisticsFileName();
}
