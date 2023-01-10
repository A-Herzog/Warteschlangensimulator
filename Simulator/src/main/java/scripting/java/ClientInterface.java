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

import java.util.Map;

/**
 * Teil-Interface, damit Nutzer-Java-Codes auf die Eigenschaft des jeweiligen Kunden
 * in dessen Kontext die Codeausführung stattfindet, zugreifen können.
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
	 * Gibt an, ob der Kunde während der Einschwingphase generiert wurde.
	 * @return	Gibt <code>true</code> zurück, wenn der Kunde während der Einschwingphase generiert wurde
	 */
	boolean isWarmUp();

	/**
	 * Gibt an, ob der Kunde in der Statistik erfasst werden soll.<br>
	 * Diese Einstellung ist unabhängig von der Einschwingphase. Ein Kunde wird nur erfasst, wenn er außerhalb
	 * der Einschwingphase generiert wurde und hier nicht falsch zurückgeliefert wird.
	 * @return	Erfassung des Kunden in der Statistik
	 */
	boolean isInStatistics();

	/**
	 * Stellt ein, ob der Kunde in der Statistik erfasst werden soll.<br>
	 * Diese Einstellung ist unabhängig von der Einschwingphase. Ein Kunde wird nur erfasst, wenn er außerhalb
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
	 * Liefert die ID der Station, an der der aktuelle Kunde erzeugt wurde oder an der ihm sein aktueller Typ zugewiesen wurde.
	 * @return	ID der Station
	 */
	int getSourceStationID();

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
	 * Stellt den Wert für die bisherige Wartezeit des aktuellen Kunden ein
	 * @param seconds	Wert für die bisherige Wartezeit des aktuellen Kunden in Sekunden
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
	 * Stellt den Wert für die bisherige Transferzeit des aktuellen Kunden ein
	 * @param seconds	Wert für die bisherige Transferzeit des aktuellen Kunden in Sekunden
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
	 * Stellt den Wert für die bisherige Bedienzeit des aktuellen Kunden ein
	 * @param seconds	Wert für die bisherige Bedienzeit des aktuellen Kunden in Sekunden
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
	 * Stellt den Wert für die bisherige Verweilzeit des aktuellen Kunden ein
	 * @param seconds	Wert für die bisherige Verweilzeit des aktuellen Kunden in Sekunden
	 * @see ClientInterface#getResidenceSeconds()
	 */
	void setResidenceSeconds(final double seconds);

	/**
	 * Liefert einen zu dem Kunden gespeicherten Zahlenwert
	 * @param index	Index zu dem der Zahlenwert abgerufen werden soll
	 * @return	Zahlenwert zu dem Index für den Kunden. (Ist kein Wert für den Index gesetzt, so wird 0.0 zurückgeliefert.)
	 */
	double getValue(final int index);

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index für den Kunden
	 */
	void setValue(final int index, final int value);

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index für den Kunden
	 */
	void setValue(final int index, final double value);

	/**
	 * Stellt einen zu einem Kunden gespeicherten Zahlenwert ein
	 * @param index	Index zu dem der Zahlenwert eingestellt werden soll
	 * @param value	Zahlenwert zu dem Index für den Kunden
	 */
	void setValue(final int index, final String value);

	/**
	 * Liefert einen zu dem Kunden gespeicherten Textwert
	 * @param key	Schlüssel zu dem der Textwert abgerufen werden soll
	 * @return	Textwert zu dem Schlüssel für den Kunden. (Ist kein Wert für den Schlüssel gesetzt, so wird ein leerer String zurückgeliefert.)
	 */
	String getText(final String key);

	/**
	 * Stellt einen Textwert für einen Kunden ein
	 * @param key	Schlüssel zu dem der Textwert eingestellt werden soll
	 * @param value	Textwert der zu dem Schlüssel hinterlegt werden soll
	 */
	void setText(final String key, final String value);

	/**
	 * Liefert alle zu einem Kunden gespeicherten Zahlenwerte.
	 * @return	Alle zu einem Kunden gespeicherten Zahlenwerte
	 */
	double[] getAllValues();

	/**
	 * Liefert alle zu einem Kunden gespeicherten Textwerte.
	 * @return	Alle zu einem Kunden gespeicherten Textwerte
	 */
	Map<String,String> getAllTexts();

	/**
	 * Handelt es sich bei dem aktuellen Kunden um einen temporären Batch,
	 * so liefert diese Funktion die Anzahl der Kunden, die sich in dem Batch befinden.
	 * @return	Anzahl an Kunden im Batch oder 0, wenn es sich nicht um einen temporären Batch handelt.
	 */
	int batchSize();

	/**
	 * Liefert den Namen eines der Kunden in dem aktuellen Batch.
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return	Name des Kunden oder <code>null</code>, wenn der Index ungültig ist.
	 */
	String getBatchTypeName(final int batchIndex);

	/**
	 * Liefert die bisherige Wartezeit eines der Kunden in dem aktuellen Batch in Sekunden als Zahlenwert
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Wartezeit des Kunden
	 * @see ClientInterface#getBatchWaitingTime(int)
	 */
	double getBatchWaitingSeconds(final int batchIndex);

	/**
	 * Liefert die bisherige Wartezeit eines der Kunden in dem aktuellen Batch in formatierter Form als Zeichenkette
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Wartezeit des Kunden
	 * @see ClientInterface#getBatchWaitingSeconds(int)
	 */
	String getBatchWaitingTime(final int batchIndex);

	/**
	 * Liefert die bisherige Transferzeit eines der Kunden in dem aktuellen Batch in Sekunden als Zahlenwert
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Transferzeit des Kunden
	 * @see ClientInterface#getBatchTransferTime(int)
	 */
	double getBatchTransferSeconds(final int batchIndex);

	/**
	 * Liefert die bisherige Transferzeit eines der Kunden in dem aktuellen Batch in formatierter Form als Zeichenkette
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Transferzeit des Kunden
	 * @see ClientInterface#getBatchTransferSeconds(int)
	 */
	String getBatchTransferTime(final int batchIndex);

	/**
	 * Liefert die bisherige Bedienzeit eines der Kunden in dem aktuellen Batch in Sekunden als Zahlenwert
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Bedienzeit des Kunden
	 * @see ClientInterface#getBatchProcessTime(int)
	 */
	double getBatchProcessSeconds(final int batchIndex);

	/**
	 * Liefert die bisherige Bedienzeit eines der Kunden in dem aktuellen Batch in formatierter Form als Zeichenkette
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Bedienzeit des Kunden
	 * @see ClientInterface#getBatchProcessSeconds(int)
	 */
	String getBatchProcessTime(final int batchIndex);

	/**
	 * Liefert die bisherige Verweilzeit eines der Kunden in dem aktuellen Batch in Sekunden als Zahlenwert
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Verweilzeit des Kunden
	 * @see ClientInterface#getBatchResidenceTime(int)
	 */
	double getBatchResidenceSeconds(final int batchIndex);

	/**
	 * Liefert die bisherige Verweilzeit eines der Kunden in dem aktuellen Batch in formatierter Form als Zeichenkette
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @return Bisherige Verweilzeit des Kunden
	 * @see ClientInterface#getBatchResidenceSeconds(int)
	 */
	String getBatchResidenceTime(final int batchIndex);

	/**
	 * Liefert einen zu einem der Kunden in dem aktuellen Batch einen gespeicherten Zahlenwert
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @param index	Index zu dem der Zahlenwert abgerufen werden soll
	 * @return	Zahlenwert zu dem Index für den Kunden. (Ist kein Wert für den Index gesetzt, so wird 0.0 zurückgeliefert.)
	 */
	double getBatchValue(final int batchIndex, final int index);

	/**
	 * Liefert zu einem der Kunden in dem aktuellen Batch einen gespeicherten Textwert
	 * @param	batchIndex 0-basierter Index des Kunden in dem Batch (Werte von 0 bis {@link #batchSize()}-1)
	 * @param key	Schlüssel zu dem der Textwert abgerufen werden soll
	 * @return	Textwert zu dem Schlüssel für den Kunden. (Ist kein Wert für den Schlüssel gesetzt, so wird ein leerer String zurückgeliefert.)
	 */
	String getBatchText(final int batchIndex, final String key);
}
