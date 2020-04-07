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
package ui.tutorial;

import ui.MainPanel;

/**
 * Dieses Interface stellt die Basis für alle in interaktiven Tutorial
 * anzuzeigenden Seiten dar. Zu jeder html-Seite muss es eine Implementierung
 * dieses Interfaces geben, die über <code>TutorialWindow.buildPagesList()</code>
 * registriert ist.
 * @author Alexander Herzog
 * @see TutorialWindow
 * @see TutorialWindow#buildPagesList()
 */
public interface TutorialPage {
	/**
	 * Liefert den Namen der zu diesem Objekt gehörigen html-Seite (ohne Pfad und ohne Extension)
	 * @return Zugehörige html-Seite (ohne Pfad und ohne Extension)
	 */
	String getPageName();

	/**
	 * Prüft, ob die Seite weiterhin angezeigt werden soll oder ob zu einer anderen
	 * Seite umgeschaltet werden soll.
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll.
	 * @return	Wird <code>null</code> zurück gegeben, so erfolgt keine Veränderung. Wird ein String zurückgegeben, so wird die genannte html-Seite angezeigt.
	 */
	String checkNextCondition(final MainPanel mainPanel);
}
