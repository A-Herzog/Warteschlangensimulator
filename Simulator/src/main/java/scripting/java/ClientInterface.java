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
 * Teil-Interface, damit Nutzer-Java-Codes auf die Eigenschaft des jeweiligen Kunden
 * in dessen Kontext die Codeausf�hrung stattfindet, zugreifen k�nnen.
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public interface ClientInterface {
	/**
	 * Berechnet einen Ausdruck im Kontext der Simulation und des aktuellen Kunden.
	 * @param expression	Zu berechnender Ausdruck.
	 * @return	Liefert im Erfolgsfall ein {@link Double}-Objekt. Im Fehlerfall eine Fehlermeldung.
	 */
	Object calc(final String expression);

	/**
	 * Gibt an, ob der Kunde w�hrend der Einschwingphase generiert wurde.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Kunde w�hrend der Einschwingphase generiert wurde
	 */
	boolean isWarmUp();

	/**
	 * Gibt an, ob der Kunde in der Statistik erfasst werden soll.<br>
	 * Diese Einstellung ist unabh�ngig von der Einschwingphase. Ein Kunde wird nur erfasst, wenn er au�erhalb
	 * der Einschwingphase generiert wurde und hier nicht falsch zur�ckgeliefert wird.
	 * @return	Erfassung des Kunden in der Statistik
	 */
	boolean isInStatistics();

	/**
	 * Stellt ein, ob der Kunde in der Statistik erfasst werden soll.<br>
	 * Diese Einstellung ist unabh�ngig von der Einschwingphase. Ein Kunde wird nur erfasst, wenn er au�erhalb
	 * der Einschwingphase generiert wurde und hier nicht falsch eingestellt wurde.
	 * @param inStatistics	Erfassung des Kunden in der Statistik
	 */
	void setInStatistics(final boolean inStatistics);

	/**
	 * Liefert die bei 1 beginnende, fortlaufende Nummer des aktuellen Kunden.
	 * @return	Fortlaufende Nummer des Kunden
	 */
	long getNumber();

	/**
	 * Liefert den Namen des aktuellen Kunden
	 * @return	Name des Kunden
	 */
	String getTypeName();

	/**
	 * Liefert die bisherige Wartezeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Wartezeit des aktuellen Kunden
	 * @see ClientInterface#getWaitingTime()
	 */
	double getWaitingSeconds();

	/**
	 * Liefert die bisherige Wartezeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Wartezeit des aktuellen Kunden
	 * @see ClientInterface#getWaitingSeconds()
	 */
	String getWaitingTime();

	/**
	 * Stellt den Wert f�r die bisherige Wartezeit des aktuellen Kunden ein
	 * @param seconds	Wert f�r die bisherige Wartezeit des aktuellen Kunden in Sekunden
	 * @see ClientInterface#getWaitingSeconds()
	 */
	void setWaitingSeconds(final double seconds);

	/**
	 * Liefert die bisherige Transferzeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Transferzeit des aktuellen Kunden
	 * @see ClientInterface#getTransferTime()
	 */
	double getTransferSeconds();

	/**
	 * Liefert die bisherige Transferzeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Transferzeit des aktuellen Kunden
	 * @see ClientInterface#getTransferSeconds()
	 */
	String getTransferTime();

	/**
	 * Stellt den Wert f�r die bisherige Transferzeit des aktuellen Kunden ein
	 * @param seconds	Wert f�r die bisherige Transferzeit des aktuellen Kunden in Sekunden
	 * @see ClientInterface#getTransferSeconds()
	 */
	void setTransferSeconds(final double seconds);

	/**
	 * Liefert die bisherige Bedienzeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Bedienzeit des aktuellen Kunden
	 * @see ClientInterface#getProcessTime()
	 */
	double getProcessSeconds();

	/**
	 * Liefert die bisherige Bedienzeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Bedienzeit des aktuellen Kunden
	 * @see ClientInterface#getProcessSeconds()
	 */
	String getProcessTime();

	/**
	 * Stellt den Wert f�r die bisherige Bedienzeit des aktuellen Kunden ein
	 * @param seconds	Wert f�r die bisherige Bedienzeit des aktuellen Kunden in Sekunden
	 * @see ClientInterface#getProcessSeconds()
	 */
	void setProcessSeconds(final double seconds);

	/**
	 * Liefert die bisherige Verweilzeit des aktuellen Kunden in Sekunden als Zahlenwert
	 * @return Bisherige Verweilzeit des aktuellen Kunden
	 * @see ClientInterface#getResidenceTime()
	 */
	double getResidenceSeconds();

	/**
	 * Liefert die bisherige Verweilzeit des aktuellen Kunden in formatierter Form als Zeichenkette
	 * @return Bisherige Verweilzeit des aktuellen Kunden
	 * @see ClientInterface#getResidenceSeconds()
	 */
	String getResidenceTime();

	/**
	 * Stellt den Wert f�r die bisherige Verweilzeit des aktuellen Kunden ein
	 * @param seconds	Wert f�r die bisherige Verweilzeit des aktuellen Kunden in Sekunden
	 * @see ClientInterface#getResidenceSeconds()
	 */
	void setResidenceSeconds(final double seconds);

	/**
	 * Liefert einen zu dem Kunden gespeicherten Zahlenwert
	 * @param index	Index zu dem der Zahlenwert abgerufen werden soll
	 * @return	Zahlenwert zu dem Index f�r den Kunden. (Ist kein Wert f�r den Index gesetzt, so wird 0.0 zur�ckgeliefert.)
	 */
	double getValue(final int index);

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index f�r den Kunden
	 */
	void setValue(final int index, final int value);

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index f�r den Kunden
	 */
	void setValue(final int index, final double value);

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index f�r den Kunden
	 */
	void setValue(final int index, final String value);

	/**
	 * Liefert einen zu dem Kunden gespeicherten Textwert
	 * @param key	Schl�ssel zu dem der Textwert abgerufen werden soll
	 * @return	Textwert zu dem Schl�ssel f�r den Kunden. (Ist kein Wert f�r den Schl�ssel gesetzt, so wird ein leerer String zur�ckgeliefert.)
	 */
	String getText(final String key);

	/**
	 * Stellt einen Textwert f�r einen Kunden ein
	 * @param key	Schl�ssel zu dem der Textwert eingestellt werden soll
	 * @param value	Textwert der zu dem Schl�ssel hinterlegt werden soll
	 */
	void setText(final String key, final String value);
}
