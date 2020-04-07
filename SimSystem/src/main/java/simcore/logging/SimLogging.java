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

import simcore.SimData;

/**
 * Dieses Interface stellt die Methoden bereit, die von <code>SimData</code> verwendet werden,
 * um Logging-Daten auszugeben. Auf diese Weise können verschiedene Logging-Backends verwendet werden.
 * @author Alexander Herzog
 * @see SimData
 */
public interface SimLogging {
	/**
	 * Gibt an, ob das Logging erfolgreich gestartet werden konnte.
	 * @return	Gibt <code>true</code> zurück, wenn das Logging in die angegebene Datei gestartet werden konnte.
	 */
	boolean ready();

	/**
	 * Speichert Statusausgaben eines Ereignisses.
	 * @param time	Zeitpunkt des Ereignisses
	 * @param color	Farbe in die die Log-Zeile eingefärbt werden soll (kann Logger-abhängig ignoriert werden)
	 * @param event	Gibt den Namen des Event, das die Logging-Aktion ausgelöst hat, an.
	 * @param info	Enthält eine Beschreibung, die zu dem Logeintrag gespeichert werden soll.
	 * @return	Gibt an, ob das Ereignis erfolgreich geloggt werden konnte.
	 */
	boolean log(final long time, final Color color, final String event, final String info);

	/**
	 * Beendet das Logging und schließt die Ausgabedatei
	 * @return	Gibt an, ob die Ausgabedatei erfolgreich geschlossen werden konnte.
	 */
	boolean done();

	/**
	 * Nachgeschalteter zweiter Logger, an den alle Logging-Daten ebenfalls übergeben werden.
	 * @param logger	Weiterer Logger in der Kette
	 */
	void setNextLogger(final SimLogging logger);

	/**
	 * Liefert einen optionalen zweiten, nachgeschalteten Logger.
	 * @return	Weiterer Logger in der Kette (oder <code>null</code>)
	 */
	SimLogging getNextLogger();
}
