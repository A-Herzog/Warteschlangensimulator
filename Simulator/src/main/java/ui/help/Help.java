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
package ui.help;

import java.awt.Container;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import language.Language;
import systemtools.help.HelpBase;
import ui.tools.WindowSizeStorage;

/**
 * Ermöglicht die Anzeige von html-basierten Hilfeseiten.
 * @author Alexander Herzog
 */
public class Help extends HelpBase {
	/**
	 * Konstruktor der Klasse
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigende Hilfeseite
	 * @param modal	Modeler Dialog (<code>true</code>) oder normales Fenster (<code>false</code>)
	 */
	private Help(final Container parent, final String topic, final boolean modal) {
		super(parent,topic,modal);
	}

	@Override
	protected URL getPageURL(String res) {
		/* Die html-Dateien müssen in einem Unterordner namens "pages" des Ordners, in dem sich diese Datei befindet, liegen. */
		return getClass().getResource("pages_"+Language.tr("Numbers.Language")+"/"+res);
	}

	/**
	 * Zeigt eine Hilfeseite als nicht-modales Fenster an
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann leer sein, es wird dann die Startseite angezeigt.
	 */
	public static void topic(final Container parent, final String topic) {
		new Help(parent,topic,false);
	}

	/**
	 * Zeigt eine Hilfeseite als modalen Dialog an
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann leer sein, es wird dann die Startseite angezeigt.
	 */
	public static void topicModal(final Container parent, final String topic) {
		new Help(parent,topic,true);
	}

	/**
	 * Listener, der aufgerufen wird, wenn ein spezieller Link (beginnend mit "special:") angeklickt wird
	 * @see #infoPanel(String, Consumer)
	 */
	private Consumer<String> specialLinkListener;

	/**
	 * Erstellt ein Panel, in dem eine bestimmte Hilfe-Seite angezeigt wird
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung).
	 * @param listener	Listener, der aufgerufen wird, wenn ein spezieller Link (beginnend mit "special:") angeklickt wird
	 * @return	Panel, welches die HTML-Seite enthält
	 */
	public static JPanel infoPanel(final String topic, final Consumer<String> listener) {
		final Help help=new Help(null,null,true);
		help.specialLinkListener=listener;
		return help.getHTMLPanel(topic);
	}

	/**
	 * Verarbeitet einen Klick auf einen Buch-Aufruf-Link
	 * @param href	Buch-Aufruf-Link
	 * @see BookDataDialog
	 */
	private void processBookLink(final String href) {
		final BookData.BookSection match=BookData.getInstance().getSection(href);
		new BookDataDialog(parent,match);
	}

	/**
	 * Prefix für Links zur Programmfunktionen
	 * @see #specialLinkListener
	 */
	private static final String SPECIAL_KEY="special:";

	/**
	 * Prefix für Links zu Buchkapiteln
	 * @see #processBookLink(String)
	 * @see BookDataDialog
	 */
	private static final String BOOK_KEY="book:";

	@Override
	protected void processSpecialLink(final String href) {
		if (href==null) return;
		final String hrefLower=href.toLowerCase();

		if (hrefLower.startsWith(SPECIAL_KEY)) {
			if (specialLinkListener!=null) specialLinkListener.accept(href.substring(SPECIAL_KEY.length()));
		}

		if (hrefLower.startsWith(BOOK_KEY)) {
			processBookLink(href.substring(BOOK_KEY.length()));
		}
	}

	@Override
	protected void setupDialog(final JDialog dialog) {
		WindowSizeStorage.window(dialog,"HelpDialog");
	}

	@Override
	protected void setupWindow(final JFrame frame) {
		WindowSizeStorage.window(frame,"HelpWindow");
	}

	@Override
	protected void preprocessPage(final Element root) {
		if (BookData.getInstance().isDataAvailable()) return;
		final Document doc=root.getDocument();
		if (!(doc instanceof HTMLDocument)) return;
		final HTMLDocument html=(HTMLDocument)doc;

		html.getStyleSheet().removeStyle(".bookinfo");
		html.getStyleSheet().removeStyle(".bookinfosmall");

		try {
			html.insertAfterEnd(root.getElement(0),"<style>.bookinfo {color: #F0F0FF; font-size: 1%;}.bookinfosmall {color: #F0F0FF; font-size: 1%;}.bookinfo a {color: #F0F0FF; font-size: 1%;}.bookinfosmall a {color: #F0F0FF; font-size: 1%;}</style>");
		} catch (BadLocationException|IOException e) {}
	}
}
