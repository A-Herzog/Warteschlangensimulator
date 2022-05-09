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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;

import systemtools.images.SimToolsImages;

/**
 * Zeigt eine Internetseite in einem Dialog an.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 * @see HTMLPanel
 */
abstract class HTMLDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2605193561807240780L;

	/**
	 * Hilfe-Viewer innerhalb des Fensters
	 * @see HTMLPanel
	 */
	private final HTMLPanel panel;

	/**
	 * Objekt vom Typ <code>Runnable</code>, welches (wenn ungleich <code>null</code>) aufgerufen wird, wenn ein Nicht-URL-Link angeklickt wird.
	 */
	private final Runnable specialLinks;

	/**
	 * Gibt an, welcher Nicht-URL-Link angeklickt wurde.
	 * @see #getSpecialLink()
	 */
	private String specialLink;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param title Titel des Dialogs
	 * @param topic	Initial anzuzeigendes Hilfethema (kann <code>null</code> sein)
	 * @param specialLinks Objekt vom Typ <code>Runnable</code>, welches (wenn ungleich <code>null</code>) aufgerufen wird, wenn ein Nicht-URL-Link angeklickt wird.
	 * @see #getSpecialLink()
	 */
	public HTMLDialog(Window owner, String title, String topic, Runnable specialLinks) {
		super(owner,title,Dialog.ModalityType.DOCUMENT_MODAL);
		this.specialLinks=specialLinks;

		/* Fenster-Icon*/
		setIconImage(SimToolsImages.HELP.getImage());

		/* HTMLPanel anlegen */
		Container p=getContentPane();
		p.setLayout(new BorderLayout());
		panel=new HTMLPanel(()->{setVisible(false); dispose();}) {
			private static final long serialVersionUID = 1L;
			@Override
			protected HTMLBrowserPanel getHTMLBrowser() {
				return HTMLDialog.this.getHTMLBrowser();
			}
			@Override
			public URL getPageURL(String res) {
				return HTMLDialog.this.getPageURL(res);
			}
			@Override
			protected void preprocessPage(final Element root) {
				HTMLDialog.this.preprocessPage(root);
			}
		};
		p.add(panel,BorderLayout.CENTER);

		panel.setHome(panel.getPageURL(HelpBase.CONTENT_PAGE));
		if (topic!=null && !topic.isEmpty()) panel.loadPage(panel.getPageURL(topic+".html"));
		panel.setProcessSpecialLink(new SpecialLink());

		/* Allgemeine Fenster-Einstellungen */
		setSize(750,650);
		setMinimumSize(getSize());
		setLocationRelativeTo(owner);
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new SpecialKeyListener());

		return rootPane;
	}

	/**
	 * Liefert den Pfad zu der angegebenen Datei
	 * @param res Name zu der anzuzeigenden Datei
	 * @return Vollständiger Pfad
	 */
	protected abstract URL getPageURL(String res);

	/**
	 * Wählt den konkreten HTML-Viewer aus.
	 * @return	Zu verwendender HTML-Viewer.
	 */
	protected abstract HTMLBrowserPanel getHTMLBrowser();

	/**
	 * Reagiert auf Escape-Tastendrücke
	 * @see HTMLDialog#createRootPane()
	 */
	private final class SpecialKeyListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -485008309903554823L;

		/**
		 * Konstruktor der Klasse
		 */
		public SpecialKeyListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {setVisible(false); dispose();}
	}

	/**
	 * Zeigt die im Parameter übergebene Seite an.
	 * @param topic	Name der Seite ohne einleitendes "HTML/" und ohne abschließendes ".html"
	 */
	public void showPage(String topic) {
		if (topic==null || topic.isEmpty()) panel.goHome(); else panel.loadPage(panel.getPageURL(topic+".html"));
	}

	/**
	 * Gibt zurück, welcher Nicht-URL-Link angeklickt wurde
	 * @return Name des Nicht-URL-Links
	 */
	public String getSpecialLink() {
		return specialLink;
	}

	/**
	 * Reagiert auf Klicks auf besondere Links
	 * @see HTMLPanel#setProcessSpecialLink(Runnable)
	 */
	private final class SpecialLink implements Runnable {
		/**
		 * Konstruktor der Klasse
		 */
		public SpecialLink() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			specialLink=panel.getSpecialLink();
			if (specialLinks!=null) SwingUtilities.invokeLater(specialLinks);
		}
	}

	/**
	 * Optionale Vorverarbeitung der geladenen Seite
	 * @param root	Wurzelelement der Seite
	 */
	protected void preprocessPage(final Element root) {
	}
}
