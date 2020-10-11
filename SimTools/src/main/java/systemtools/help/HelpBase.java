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

import java.awt.Container;
import java.awt.Window;
import java.io.Serializable;
import java.net.URL;

/**
 * Basisklasse für die html-basierte Online-Hilfe
 * @author Alexander Herzog
 * @version 1.2
 */
public abstract class HelpBase {
	/** Titel des Hilfefensters */
	public static String title="Hilfe";
	/** Bezeichnung für die Schaltfläche "Schließen" */
	public static String buttonClose="Schließen";
	/** Bezeichnung für den Tooltip für die Schaltfläche "Schließen" */
	public static String buttonCloseInfo="Schließt die Online-Hilfe";
	/** Bezeichnung für die Schaltfläche "Startseite" */
	public static String buttonStartPage="Startseite";
	/** Bezeichnung für den Tooltip für die Schaltfläche "Startseite" */
	public static String buttonStartPageInfo="Zeigt die Startseite der Hilfe an";
	/** Bezeichnung für die Schaltfläche "Zurück" */
	public static String buttonBack="Zurück";
	/** Bezeichnung für den Tooltip für die Schaltfläche "Zurück" */
	public static String buttonBackInfo="Zurück zur zuletzt angezeigten Hilfeseite";
	/** Bezeichnung für die Schaltfläche "Weiter" */
	public static String buttonNext="Weiter";
	/** Bezeichnung für den Tooltip für die Schaltfläche "Weiter" */
	public static String buttonNextInfo="Weiter zur nächsten Hilfeseite";
	/** Bezeichnung für die Schaltfläche "Inhalt dieser Seite" */
	public static String buttonContent="Inhalt dieser Seite";
	/** Bezeichnung für den Tooltip für die Schaltfläche "Inhalt dieser Seite" */
	public static String buttonContentInfo="Zeigt die Abschnittsüberschriften dieser Seite an";
	/** Titel der Fehlermeldung "Kein E-Mail-Programm festgelegt" */
	public static String errorNoEMailTitle="Kein E-Mail-Programm festgelegt";
	/** Inhalt Fehlermeldung "Kein E-Mail-Programm festgelegt" */
	public static String errorNoEMailInfo="Der angegeben E-Mail-Link\n%s\nkonnte nicht aufgerufen werden.";
	/** Titel der Fehlermeldung "Keine Internet-Verbindung möglich" */
	public static String errorNoInternetTitle="Keine Internet-Verbindung möglich";
	/** Inhalt der Fehlermeldung "Keine Internet-Verbindung möglich" */
	public static String errorNoInternetInfo="Die angegebene Adresse\n%s\nkonnte nicht aufgerufen werden.";

	/**
	 * Dateiname für die Startseite
	 */
	public static String CONTENT_PAGE="index.html";

	/**
	 * @see HelpBase#viewerMode
	 */
	public enum ViewerMode {
		/** Nutzt ein <code>JTextPane</code> zur Anzeige der Hilfeseiten. */
		HTML_VIEWER_SWING,

		/** Nutzt ein <code>JFXPanel</code> zur Anzeige der Hilfeseiten. */
		/* HTML_VIEWER_JAVAFX */
	}

	/**
	 * Welche HTML-Viewer soll verwendet werden?
	 * @see ViewerMode
	 */
	public static ViewerMode viewerMode=ViewerMode.HTML_VIEWER_SWING;

	/**
	 * Ersetzt optional den Standard-Style durch die hier angegebene Datei
	 */
	public static String customStyleSheet=null;

	/**
	 * Übergeordnetes Element
	 */
	private final Container parent;

	/** Modaler Hilfe-Dialog */
	private HTMLDialog helpDialog;
	/** Nicht modales Hilfe-Fenster */
	private static HTMLFrame helpFrame=null;

	/**
	 * Liefert das übergeordnete Fenster zu einer {@link Container}-Komponente
	 * @param parent	{@link Container}-Komponente für die das übergeordnete Fenster gesucht werden soll
	 * @return	Übergeordnetes Fenster oder <code>null</code>, wenn kein entsprechendes Fenster gefunden wurde
	 */
	private static final Window getOwnerWindow(final Container parent) {
		Container c=parent;
		while (c!=null) {
			if (c instanceof Window) return (Window)c;
			c=c.getParent();
		}
		return null;
	}

	/**
	 * Blendet das nicht-modale Hilfefenster aus.
	 */
	public static final void hideHelpFrame() {
		if (helpFrame!=null && helpFrame.isVisible()) helpFrame.setVisible(false);
	}

	/**
	 * Konstruktor der Klasse <code>HelpBase</code>.
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann leer sein, es wird dann die Startseite angezeigt.
	 * @param modal	Gibt an, ob die Hilfe als normales Fenster (<code>false</code>) oder als modaler Dialog (<code>true</code>) angezeigt werden soll.
	 */
	public HelpBase(final Container parent, final String topic, final boolean modal) {
		this.parent=parent;
		if (topic!=null) {
			if (modal) showHelpDialog(topic); else showHelpWindow(topic);
		}
	}

	/**
	 * Konstruktor der Klasse <code>HelpBase</code>.<br>
	 * Die Hilfe wird als (nicht modales) Fenster angezeigt.
	 * @param parent	Übergeordnetes Element
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann <code>null</code> oder leer sein, es wird dann die Startseite angezeigt.
	 */
	public HelpBase(final Container parent, final String topic) {
		this(parent,topic,false);
	}

	/**
	 * Konstruktor der Klasse <code>HelpBase</code>.<br>
	 * Zeigt die Startseite der Hilfe an.
	 * @param parent	Übergeordnetes Element
	 * @param modal	Gibt an, ob die Hilfe als normales Fenster (<code>false</code>) oder als modaler Dialog (<code>true</code>) angezeigt werden soll.
	 */
	public HelpBase(final Container parent, final boolean modal) {
		this(parent,null,modal);
	}

	/**
	 * Konstruktor der Klasse <code>HelpBase</code>.
	 * Zeigt die Startseite der Hilfe an.<br>
	 * Die Hilfe wird als (nicht modales) Fenster angezeigt.
	 * @param parent	Übergeordnetes Element
	 */
	public HelpBase(final Container parent) {
		this(parent,null,false);
	}

	/**
	 * Zeigt einen modalen Hilfe-Dialog an.
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann leer sein, es wird dann die Startseite angezeigt.
	 * @see #helpDialog
	 */
	private void showHelpDialog(final String topic) {
		helpDialog=new HTMLDialog(getOwnerWindow(parent),title,topic,()->processSpecialLink(getHRef())) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -7543988339882677342L;
			@Override
			protected URL getPageURL(String res) {
				return HelpBase.this.getPageURL(res);
			}
			@Override
			protected HTMLBrowserPanel getHTMLBrowser() {
				return HelpBase.this.getHTMLBrowser();
			}
		};
		helpDialog.setVisible(true);
	}

	/**
	 * Zeigt ein nicht modales Hilfe-Fenster an.
	 * @param topic	Anzuzeigendes Thema (Dateiname ohne ".html"-Endung). Kann leer sein, es wird dann die Startseite angezeigt.
	 * @see #helpFrame
	 */
	private void showHelpWindow(final String topic) {
		boolean newWindow=false;
		if (helpFrame==null || !helpFrame.isVisible()) {
			helpFrame=new HTMLFrame(getOwnerWindow(parent),title,()->processSpecialLink(getHRef())) {
				/**
				 * Serialisierungs-ID der Klasse
				 * @see Serializable
				 */
				private static final long serialVersionUID = 3469355239872231962L;
				@Override
				protected URL getPageURL(String res) {
					return HelpBase.this.getPageURL(res);
				}
				@Override
				protected HTMLBrowserPanel getHTMLBrowser() {
					return HelpBase.this.getHTMLBrowser();
				}
			};
			newWindow=true;
		}

		helpFrame.showPage(topic);
		if (!newWindow) {
			helpFrame.toFront();
			helpFrame.repaint();
		}
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
	protected HTMLBrowserPanel getHTMLBrowser() {
		final HTMLBrowserPanel viewer;

		switch (viewerMode) {
		case HTML_VIEWER_SWING:
			viewer=new HTMLBrowserTextPane();
			break;
			/*
		case HTML_VIEWER_JAVAFX:
			viewer=new HTMLBrowserFXPane();
			break;
			 */
		default:
			viewer=new HTMLBrowserTextPane();
			break;
		}

		if (customStyleSheet!=null && !customStyleSheet.trim().isEmpty()) viewer.setUserDefinedStyleSheet(customStyleSheet);

		return viewer;
	}

	/**
	 * Verarbeitet das Klicken auf einen Link, der nicht mit "http:", "https:" oder "mailto:" beginnt.
	 * @param href	href-Attribut des angeklickten Links
	 */
	protected void processSpecialLink(String href) {
		/*
		Beispiel:
		final String key="special:";
		if (!href.substring(0,Math.min(href.length(),key.length())).equalsIgnoreCase(key)) return;
		href=href.substring(key.length());
		 */
	}

	/**
	 * Ermittelt das Linkziel des angeklickten Links
	 * @return	Linkziel des angeklickten Links
	 * @see #processSpecialLink(String)
	 */
	private String getHRef() {
		return (helpDialog!=null && helpDialog.isVisible())?helpDialog.getSpecialLink():helpFrame.getSpecialLink();
	}

	/**
	 * Zuletzt in {@link #getHTMLPanel(String)} erzeugtes Hilfepanel.
	 */
	private HTMLPanel lastPanel;

	/**
	 * Erstellt ein <code>JPanel</code> und zeigt darin eine bestimmte Hilfeseite an.<br>
	 * Es existiert nur ein Panel dieser Art, d.h. es können nicht an verschiedenen
	 * Stellen gleichzeitig per {@link #getHTMLPanel(String)} erzeugte Panels verwendet werden.
	 * @param page	Anzuzeigende Seite
	 * @return	Objekt vom Typ <code>JPanel</code>
	 */
	public HTMLPanel getHTMLPanel(final String page) {
		if (lastPanel==null) {
			lastPanel=new HTMLPanel(false,false,null) {
				private static final long serialVersionUID = 1L;
				@Override
				protected HTMLBrowserPanel getHTMLBrowser() {
					return HelpBase.this.getHTMLBrowser();
				}
				@Override
				public URL getPageURL(String res) {
					return HelpBase.this.getPageURL(res);
				}
			};
			lastPanel.setProcessSpecialLink(()->processSpecialLink(lastPanel.getSpecialLink()));
		}
		lastPanel.loadPage(lastPanel.getPageURL(page+".html"));
		return lastPanel;
	}
}