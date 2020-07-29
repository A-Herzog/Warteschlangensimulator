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
 * Teil-Interface, damit Nutzer-Java-Codes auf Listen wartender Kunden zugreifen kann und diese selektiv freigeben kann.
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public interface ClientsInterface {
	/**
	 * Gibt die Anzahl an Kunden in der Warteschlange an.
	 * @return	Anzahl an Kunden in der Warteschlange
	 */
	int count();

	/**
	 * Legt fest, dass ein bestimmter Kunde für den Weitertransport freigegeben werden soll.
	 * @param index	0-basierender Index des Kunden
	 */
	void release(final int index);

	/**
	 * Liefert den Namen eines Kunden
	 * @param index	0-basierender Index des Kunden
	 * @return	Name des Kunden
	 */
	String clientTypeName(final int index);

	/**
	 * Liefert ein Client-Daten-Element eines Kunden
	 * @param index	0-basierender Index des Kunden
	 * @param data	Index des Datenelements
	 * @return	Daten-Element des Kunden
	 */
	double clientData(final int index, final int data);

	/**
	 * Stellt ein Client-Daten-Element eines Kunden ein
	 * @param index	0-basierender Index des Kunden
	 * @param data	Index des Datenelements
	 * @param value	Neuer Wert
	 */
	void clientData(final int index, final int data, final double value);

	/**
	 * Liefert ein Client-Textdaten-Element eins Kunden
	 * @param index	0-basierender Index des Kunden
	 * @param key	Schlüssel des Datenelements
	 * @return	Daten-Element des Kunden
	 */
	String clientTextData(final int index, final String key);

	/**
	 * Stellt ein Client-Textdaten-Element eines Kunden ein
	 * @param index	0-basierender Index des Kunden
	 * @param key	Schlüssel des Datenelements
	 * @param value	Neuer Wert
	 */
	void clientTextData(final int index, final String key, final String value);

	/**
	 * Liefert die bisherige Wartezeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Wartezeit des Kunden
	 * @see ClientsInterface#clientWaitingTime(int)
	 */
	double clientWaitingSeconds(final int index);

	/**
	 * Liefert die bisherige Wartezeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Wartezeit des Kunden
	 * @see ClientsInterface#clientWaitingSeconds(int)
	 */
	String clientWaitingTime(final int index);

	/**
	 * Liefert die bisherige Transferzeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Transferzeit des Kunden
	 * @see ClientsInterface#clientTransferTime(int)
	 */
	double clientTransferSeconds(final int index);

	/**
	 * Liefert die bisherige Transferzeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Transferzeit des Kunden
	 * @see ClientsInterface#clientTransferSeconds(int)
	 */
	String clientTransferTime(final int index);

	/**
	 * Liefert die bisherige Bedienzeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Bedienzeit des Kunden
	 * @see ClientsInterface#clientProcessTime(int)
	 */
	double clientProcessSeconds(final int index);

	/**
	 * Liefert die bisherige Bedienzeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Bedienzeit des Kunden
	 * @see ClientsInterface#clientProcessSeconds(int)
	 */
	String clientProcessTime(final int index);

	/**
	 * Liefert die bisherige Verweilzeit eines Kunden in Sekunden als Zahlenwert
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Verweilzeit des Kunden
	 * @see ClientsInterface#clientResidenceTime(int)
	 */
	double clientResidenceSeconds(final int index);

	/**
	 * Liefert die bisherige Verweilzeit eines Kunden in formatierter Form als Zeichenkette
	 * @param index	0-basierender Index des Kunden
	 * @return Bisherige Verweilzeit des Kunden
	 * @see ClientsInterface#clientResidenceSeconds(int)
	 */
	String clientResidenceTime(final int index);
}