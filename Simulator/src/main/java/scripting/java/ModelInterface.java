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
 * Teil-Interface, damit Nutzer-Java-Codes das aktuelle Modell verändern können.
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public interface ModelInterface {

	/**
	 * Stellt das Modell auf den Ausgangszustand zurück.
	 */
	void reset();

	/**
	 * Liefert den Wert eines XML-Objekts
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @return	Bisheriger Wert des XML-Objekts
	 */
	String xml(final String xmlName);

	/**
	 * Stellt den Wert eines XML-Objektes ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neuer Wert des XML-Objektes
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	boolean setString(final String xmlName, final String value);

	/**
	 * Stellt den Zahlenwert eines XML-Objektes ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neuer Zahlenwert des XML-Objektes
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	boolean setValue(final String xmlName, final double value);

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt deren Mittelwert ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neuer Mittelwert
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	boolean setMean(final String xmlName, final double value);

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt deren Standardabweichung ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neue Standardabweichung
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	boolean setSD(final String xmlName, final double value);

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt einen Verteilungsparameter ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param number	1-basierende Nummer des Verteilungsparameters (1-4)
	 * @param value	Neuer Wert für den Parameter
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	boolean setDistributionParameter(final String xmlName, final int number, final double value);

	/**
	 * Liefert die Anzahl an Bedienern in einer Ressource
	 * @param resourceName	Name der Ressource
	 * @return	Anzahl an Bedienern in einer Ressource oder -1, wenn die Ressource nicht existiert oder nicht über eine feste Anzahl an Bedienern definiert ist.
	 */
	int getResourceCount(final String resourceName);

	/**
	 * Stellt die Anzahl an Bedienern in einer Ressource ein
	 * @param resourceName	Name der Ressource
	 * @param count	Anzahl an Bedienern
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	boolean setResourceCount(final String resourceName, final int count);

	/**
	 * Liefert den initialen Ausdruck für eine globale Variable
	 * @param variableName	Name der globalen Variable
	 * @return	Initialer Ausdruck für die globale Variable oder eine leere Zeichenkette, wenn die Variable nicht existiert.
	 */
	String getGlobalVariableInitialValue(final String variableName);

	/**
	 * Stellt den initialen Ausdruck für eine globale Variable ein
	 * @param variableName	Name der globalen Variable
	 * @param expression Neuer initialer Wert für die globale Variable
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	boolean setGlobalVariableInitialValue(final String variableName, final String expression);

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden keine Simulationsläufe mehr ausgeführt.)
	 */
	void cancel();

	/**
	 * Simuliert das aktuelle Modell.
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich simuliert werden konnte
	 */
	boolean run();
}
