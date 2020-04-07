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
 * Mögliche Statusmeldungen beim dynamischen Laden von Klassen
 * @author Alexander Herzog
 * @see DynamicClassBase
 */
public enum DynamicStatus {
	/**
	 * Falsches Betriebssystem, es kann nicht Just-in-time kompiliert werden.
	 */
	UNSUPPORTED_OS,

	/**
	 * Es wurde keine zu verwendende Eingabedatei bzw. kein Eingabetext angegeben.
	 */
	NO_INPUT_FILE_OR_DATA,

	/**
	 * Die Eingabedatei ist keine java- oder class-Datei.
	 */
	UNKNOWN_INPUT_FORMAT,

	/**
	 * Das Temp-Verzeichnis konnte nicht angelegt werden.
	 */
	NO_TEMP_FOLDER,

	/**
	 * Der Java-Kompiler "javac" konnte nicht gefunden werden.
	 */
	NO_COMPILER,

	/**
	 * Fehler während der Kompilierung.<br>
	 * Evtl. enthält {@link DynamicClassBase#getError()} eine detaillierte Fehlermeldung.
	 */
	COMPILE_ERROR,

	/**
	 * Fehler beim Laden der Klasse.
	 * Evtl. enthält {@link DynamicClassBase#getError()} eine detaillierte Fehlermeldung.
	 */
	LOAD_ERROR,

	/**
	 * Fehler bei der Ausführung.
	 * Evtl. enthält {@link DynamicClassBase#getError()} eine detaillierte Fehlermeldung.
	 */
	RUN_ERROR,

	/**
	 * Alles in Ordnung.
	 */
	OK
}
