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

import java.net.URL;
import java.util.List;

import javax.swing.JComponent;

/**
 * Interface, welches einen Webbrowser kapselt
 * @author Alexander Herzog
 */
public interface HTMLBrowserPanel {

	/**
	 * Initialisiert das Panel
	 * @param linkClickListener	Callback, das aufgerufen wird, wenn ein Link angeklickt wird
	 * @param pageLoadListener	Runnable, das aufgerufen wird, wenn eine Seite geladen wurde
	 */
	void init(final Runnable linkClickListener, final Runnable pageLoadListener);

	/**
	 * Gibt an, welche Seite angezeigt werden soll
	 * @param url	URL der anzuzeigenden Seite
	 * @return	Gibt <code>true</code> zur�ck, wenn die Seite erfolgreich geladen werden konnte
	 */
	boolean showPage(final URL url);

	/**
	 * Liefert die Webbrowser-Komponente als notwendigenfalls in ein <code>JScrollPane</code> eingebettetes <code>JComponent</code>-Objekt zur�ck
	 * @return	Webbrowser-Komponente
	 */
	JComponent asScrollableJComponent();

	/**
	 * Liefert die Webbrowser-Komponente als <code>JComponent</code>-Objekt zur�ck
	 * @return	Webbrowser-Komponente
	 */
	JComponent asInnerJComponent();

	/**
	 * Liefert die URL des Links, der zuletzt angeklickt wurde.
	 * Wurde noch kein Link angeklick oder konnte das Linkziel nicht als URL interpretiert werden, so liefert die Funktion <code>null</code> zur�ck.
	 * @return	URL des als letztes angeklickten Links
	 */
	URL getLastClickedURL();

	/**
	 * Lieft den Inhalt des Linkziels des zuletzt angeklickten Links als Text zur�ck.
	 * Wurde noch kein Link angeklickt, so liefert die Funktion einen leeren String zur�ck.
	 * @return	Linkziel des zuletzt angeklickten Links als Text
	 */
	String getLastClickedURLDescription();

	/**
	 * Liefert eine Liste mit allen �berschriften auf der Seite
	 * @return	Liste mit allen �berschriften auf der Seite
	 */
	List<String> getPageContent();

	/**
	 * Liefert eine Liste mit den Ebenen aller �berschriften auf der Seite
	 * @return	Liste mit den Ebenen aller �berschriften auf der Seite
	 */
	List<Integer> getPageContentLevel();

	/**
	 * Scrollt zu einer ausgew�hlten �berschrift
	 * @param index	Nummer der �berschrift, zu der gescrollt werden soll
	 * @return	Gibt <code>true</code> zur�ck, wenn der angegeben Index im g�ltigen Bereich lag.
	 */
	boolean scrollToPageContent(int index);

	/**
	 * Gibt an, ob nach dem Laden der Seite die R�ckkehr zum Aufrufer verz�gert werden soll, bis der Viewer meldet, dass die Seite vollst�ndig geladen wurde.
	 * @return	<code>true</code>, wenn die weitere Verarbeitung gestoppt werden muss, bis der in <code>init</code> �bergebene Page-Load-Listener ausgel�st wurde.
	 */
	boolean needsLoadLock();

	/**
	 * Setzt einen anderen Stylesheet.<br>
	 * Es muss in der html-Datei daf�r ein &lt;link ref="stylesheet"&gt;-Element vorhanden sein, in dem dann der die verlinkte Datei ersetzt wird.
	 * @param styleSheet	Dateiname des neuen Stylesheets.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Viewer das �ndern des Stylesheets unterst�tzt.
	 */
	boolean setUserDefinedStyleSheet(final String styleSheet);

	/**
	 * Gibt an, ob das Browser-Element einen Rahmen in dem Panel, in das es eingebettet werden soll, bekommen soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Element einen Rahmen, der in ein umgebendes Element gezeichnet werden soll, ben�tigt.
	 */
	boolean needsBorder();
}
