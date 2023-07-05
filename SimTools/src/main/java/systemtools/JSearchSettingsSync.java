/**
 * Copyright 2023 Alexander Herzog
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
package systemtools;

/**
 * Stellt gemeinsame Konfigurationsoptionen f�r Suchdialoge
 * an verschiedenen Stellen im Programm bereit.
 */
public class JSearchSettingsSync {
	/**
	 * Wurde {@link #loadCallback} bereits aufgerufen?
	 * @see #loadCallback
	 */
	private static boolean loaded=false;

	/**
	 * L�uft {@link #loadCallback} gerade?
	 * @see #loadCallback
	 */
	private static boolean loading=false;

	/**
	 * Soll die Gro�- und Kleinschreibung bei der Suche ber�cksichtigt werden?
	 * @see #getCaseSensitive()
	 * @see #setCaseSensitive(boolean)
	 */
	private static boolean caseSensitive=false;

	/**
	 * Sollen nur vollst�ndige �bereinstimmungen ber�cksichtigt werden?
	 * @see #getFullMatchOnly()
	 * @see #setFullMatchOnly(boolean)
	 */
	private static boolean fullMatchOnly=false;

	/**
	 * Ist der Suchbegriff ein regul�rer Ausdruck?
	 * @see #getRegEx()
	 * @see #setRegEx(boolean)
	 */
	private static boolean regEx=false;

	/**
	 * Soll vorw�rts gesucht werden?
	 * @see #getForward()
	 * @see #setForward(boolean)
	 */
	private static boolean forward=true;

	/**
	 * Wird aufgerufen, wenn die Einstellungen in diesem statischen
	 * Objekt geladen werden sollen. (Kann <code>null</code> sein,
	 * dann wird nichts geladen.)
	 */
	public static Runnable loadCallback;

	/**
	 * Wird aufgerufen, wenn die Einstellungen in diesem statischen
	 * Objekt ver�ndert wurden und nun neu gespeichert werden sollen.
	 * (Kann <code>null</code> sein, dann wird nichts gespeichert.)
	 */
	public static Runnable saveCallback;

	/**
	 * Konstruktor der Klasse<br>
	 * Dieses statische Singleton kann nicht instanziert werden.
	 */
	private JSearchSettingsSync() {
	}

	/**
	 * Wird intern aufgerufen, wenn die Daten in dieses Objekt geladen werden sollen.
	 */
	private static void load() {
		if (loaded || loading) return;
		if (loadCallback!=null) {
			loading=true;
			try {
				loadCallback.run();
			} finally {
				loading=false;
			}
		}
		loaded=true;
	}

	/**
	 * Wird intern aufgerufen, wenn die Daten in diesem Objekt ver�ndert wurden
	 * und nun gespeichert werden sollen.
	 */
	private static void save() {
		if (loading) return;
		if (saveCallback!=null) saveCallback.run();
	}

	/**
	 * Soll die Gro�- und Kleinschreibung bei der Suche ber�cksichtigt werden?
	 * @return	Gro�- und Kleinschreibung bei der Suche ber�cksichtigen
	 */
	public static boolean getCaseSensitive() {
		load();
		return caseSensitive;
	}

	/**
	 * Sollen nur vollst�ndige �bereinstimmungen ber�cksichtigt werden?
	 * @return	Nur vollst�ndige �bereinstimmungen ber�cksichtigen
	 */
	public static boolean getFullMatchOnly() {
		load();
		return fullMatchOnly;
	}

	/**
	 * Ist der Suchbegriff ein regul�rer Ausdruck?
	 * @return	Suchbegriff ist ein regul�rer Ausdruck
	 */
	public static boolean getRegEx() {
		load();
		return regEx;
	}

	/**
	 * Soll vorw�rts gesucht werden?
	 * @return	Vorw�rts suchen
	 */
	public static boolean getForward() {
		load();
		return forward;
	}

	/**
	 * Soll die Gro�- und Kleinschreibung bei der Suche ber�cksichtigt werden?
	 * @param caseSensitive	Gro�- und Kleinschreibung bei der Suche ber�cksichtigen
	 */
	public static void setCaseSensitive(final boolean caseSensitive) {
		load();
		if (JSearchSettingsSync.caseSensitive==caseSensitive) return;
		JSearchSettingsSync.caseSensitive=caseSensitive;
		save();

	}

	/**
	 * Sollen nur vollst�ndige �bereinstimmungen ber�cksichtigt werden?
	 * @param fullMatchOnly	Nur vollst�ndige �bereinstimmungen ber�cksichtigen
	 */
	public static void setFullMatchOnly(final boolean fullMatchOnly) {
		load();
		if (JSearchSettingsSync.fullMatchOnly==fullMatchOnly) return;
		JSearchSettingsSync.fullMatchOnly=fullMatchOnly;
		save();
	}

	/**
	 * Ist der Suchbegriff ein regul�rer Ausdruck?
	 * @param regEx	Suchbegriff ist ein regul�rer Ausdruck
	 */
	public static void setRegEx(final boolean regEx) {
		load();
		if (JSearchSettingsSync.regEx==regEx) return;
		JSearchSettingsSync.regEx=regEx;
		save();
	}

	/**
	 * Soll vorw�rts gesucht werden?
	 * @param forward	Vorw�rts suchen
	 */
	public static void setForward(final boolean forward) {
		load();
		if (JSearchSettingsSync.forward==forward) return;
		JSearchSettingsSync.forward=forward;
		save();
	}
}