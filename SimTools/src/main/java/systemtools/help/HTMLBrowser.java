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
package systemtools.help;

/**
 * Ermöglicht die Instanzierung eines Objektes vom Typ {@link HTMLBrowserPanel}.
 * @author Alexander Herzog
 * @version 1.0
 */
public class HTMLBrowser {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt lediglich statische Methoden zur Verfügung.
	 */
	private HTMLBrowser() {}

	/**
	 * Liefert eine Instanz vom Typ {@link HTMLBrowserPanel}.
	 * @param viewerMode	Auswahl des Anzeige Backend
	 * @return	HTML-Browser
	 */
	public static HTMLBrowserPanel getBrowser(final HelpBase.ViewerMode viewerMode) {
		switch (viewerMode) {
		case HTML_VIEWER_SWING: return new HTMLBrowserTextPane();
		/* case HTML_VIEWER_JAVAFX: return new HTMLBrowserFXPane(); */
		default: return new HTMLBrowserTextPane();
		}
	}
}
