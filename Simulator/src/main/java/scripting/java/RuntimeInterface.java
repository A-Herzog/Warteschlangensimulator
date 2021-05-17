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
 * Teil-Interface, damit Nutzer-Java-Codes auf allgemeine, simulationsunabh�ngige Daten zugreifen kann.
 * @author Alexander Herzog
 * @see SimulationInterface
 */
public interface RuntimeInterface {
	/**
	 * Berechnet einen Ausdruck.
	 * @param expression	Zu berechnender Ausdruck.
	 * @return	Liefert im Erfolgsfall ein {@link Double}-Objekt. Im Fehlerfall eine Fehlermeldung.
	 */
	Object calc(final String expression);

	/**
	 * Liefert die Computerzeit in Millisekunden f�r Laufzeitmessungen.
	 * @return	Computerzeit in Millisekunden
	 */
	long getTime();

	/**
	 * L�dt einen einzelnen Wert von einer Internetadresse
	 * @param url	Aufzurufende URL
	 * @param errorValue	Wert der im Fehlerfall zur�ckgeliefert werden soll
	 * @return	Gelesener Wert oder <code>errorValue</code> im Fehlerfall
	 */
	double getInput(final String url, final double errorValue);

	/**
	 * F�hrt einen externen Befehl aus und kehrt sofort zur�ck.
	 * @param commandLine	Auszuf�hrender Befehl
	 * @return	Liefert <code>true</code>, wenn der Befehl ausgef�hrt werden konnte
	 */
	boolean execute(final String commandLine);

	/**
	 * F�hrt einen externen Befehl aus und liefert die Ausgabe zur�ck.
	 * @param commandLine	Auszuf�hrender Befehl
	 * @return	Liefert im Erfolgsfall die Ausgabe zur�ck, sonst <code>null</code>
	 */
	String executeAndReturnOutput(final String commandLine);

	/**
	 * F�hrt einen externen Befehl aus und wartet auf den Abschluss.
	 * @param commandLine	Auszuf�hrender Befehl
	 * @return	Liefert im Erfolgsfall den R�ckgabecode, sonst -1.
	 */
	int executeAndWait(final String commandLine);

	/**
	 * Liefert das Stations-lokales Datenobjekt f�r Skript-Daten.
	 * @return	Stations-lokales Datenobjekt f�r Skript-Daten
	 */
	Map<String,Object> getMapLocal();

	/**
	 * Liefert das globale Datenobjekt f�r �ber alle Stationen hinweg gemeinsam genutzte Skript-Daten.
	 * @return	Globales Datenobjekt f�r �ber alle Stationen hinweg gemeinsam genutzte Skript-Daten
	 */
	Map<String,Object> getMapGlobal();
}
