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
import java.awt.Color;
import java.awt.Desktop;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;

import mathtools.Table;
import mathtools.distribution.swing.JOpenURL;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Zeigt eine Internetseite in einem <code>JPanel</code> an.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
public abstract class HTMLPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3360161989499967773L;

	/** Wird, wenn nicht <code>null</code>, aufgerufen, wenn der Nutzer auf die Schließen-Schaltfläche klickt. */
	private final Runnable closeNotify;

	/** Toolbar des Panels */
	private final JToolBar toolBar;

	/** "Schließen"-Schaltfläche */
	private final JButton buttonClose;

	/** "Start"-Schaltfläche */
	private final JButton buttonHome;

	/** "Zurück"-Schaltfläche */
	private final JButton buttonBack;

	/** "Weiter"-Schaltfläche */
	private final JButton buttonNext;

	/** "Inhalt anzeigen"-Schaltfläche */
	private final JButton buttonContent;

	/** "Suchen"-Schaltfläche */
	private final JButton buttonSearch;

	/** "Drucken"-Schaltfläche */
	private final JButton buttonPrint;

	/** Panel zur Anzeige des Hilfetextes */
	private HTMLBrowserPanel textPane;

	/** Popup zur Anzeige der Inhaltselemente /wird über die "Inhalt anzeigen"-Schaltfläche aktiviert */
	private final JPopupMenu contentPopup;

	/** Gibt an, ob die Toolbar-Schaltfläche, die eine Popup-Menü mit einer Übersicht der Zwischenüberschriften der Seite enthält, angezeigt werden soll. */
	private final boolean showContent;

	/** Liste mit den "Zurück"-URLs */
	private final List<URL> listBack;

	/** Liste mit den "Weiter"-URLs */
	private final List<URL> listNext;

	/** Aktuell angezeigte URL */
	private URL currentURL=null;

	/** Startseiten-URL */
	private URL homeURL=null;

	/**
	 * Findet gerade ein Ladevorgang statt?
	 * @see #loadPage(File)
	 * @see #waitPageLoadDone()
	 */
	private boolean loading=false;

	/**
	 * Wird <b>kein</b> Locking während des Ladens benötigt?
	 * @see HTMLBrowserPanel#needsLoadLock()
	 * @see #waitPageLoadDone()
	 */
	private boolean noLockingViewer=true;

	/** Callback welches aufgerufen wird, wenn der Nutzer auf einen Link klickt, der keine URL enthält. */
	private Runnable processSpecialLink;

	/** Linkziel für einen angeklickten Link, der keine URL enthält. */
	private String specialLink="";

	/**
	 * Konstruktor der Klasse <code>HTMLPanel</code>
	 * @param showBackAndNext Zeigt die Vorwärts- und Rückwärtsschaltflächen an
	 * @param showContent	Zeigt eine Toolbar-Schaltfläche an, die eine Popup-Menü mit einer Übersicht der Zwischenüberschriften der Seite enthält
	 * @param closeNotify	Wird aufgerufen, wenn der Nutzer auf die Schließen-Schaltfläche klickt. (Darf <code>null</code> sein.)
	 */
	public HTMLPanel(boolean showBackAndNext, boolean showContent, Runnable closeNotify) {
		setLayout(new BorderLayout());
		this.showContent=showContent;
		this.closeNotify=closeNotify;

		toolBar=new JToolBar();
		toolBar.setFloatable(false);
		buttonClose=addButton(HelpBase.buttonClose,HelpBase.buttonCloseInfo,SimToolsImages.EXIT.getIcon());
		buttonClose.setVisible(showBackAndNext && closeNotify!=null);
		buttonHome=addButton(HelpBase.buttonStartPage,HelpBase.buttonStartPageInfo,SimToolsImages.HELP_HOME.getIcon());
		buttonHome.setVisible(showBackAndNext);
		buttonBack=addButton(HelpBase.buttonBack,HelpBase.buttonBackInfo,SimToolsImages.HELP_BACK.getIcon());
		buttonBack.setVisible(showBackAndNext);
		buttonBack.setEnabled(false);
		buttonNext=addButton(HelpBase.buttonNext,HelpBase.buttonNextInfo,SimToolsImages.HELP_NEXT.getIcon());
		buttonNext.setVisible(showBackAndNext);
		buttonNext.setEnabled(false);
		buttonContent=addButton(HelpBase.buttonContent,HelpBase.buttonContentInfo,SimToolsImages.HELP_FIND_IN_PAGE.getIcon());
		buttonContent.setVisible(false);
		buttonSearch=addButton(HelpBase.buttonSearch,HelpBase.buttonSearchInfo,SimToolsImages.HELP_SEARCH.getIcon());
		buttonSearch.setVisible(IndexSystem.getInstance().isReady());
		buttonPrint=addButton(HelpBase.buttonPrint,HelpBase.buttonPrintInfo,SimToolsImages.HELP_PRINT.getIcon());

		if (showBackAndNext) add(toolBar,BorderLayout.NORTH);

		textPane=getHTMLBrowser();

		noLockingViewer=!textPane.needsLoadLock();

		textPane.init(new LinkClickListener(),root->preprocessPage(root), new PageLoadListener());

		if (textPane.needsBorder()) {
			final JPanel outer=new JPanel(new BorderLayout());
			outer.add(textPane.asScrollableJComponent(),BorderLayout.CENTER);
			outer.setBorder(BorderFactory.createLineBorder(new Color(SystemColor.controlShadow.getRGB())));
			add(outer,BorderLayout.CENTER);
		} else {
			add(textPane.asScrollableJComponent(),BorderLayout.CENTER);
		}

		contentPopup=new JPopupMenu();

		listBack=new ArrayList<>();
		listNext=new ArrayList<>();

		InputMap inputMap=getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		inputMap.put(stroke,"ESCAPE");
		getActionMap().put("ESCAPE",new EscapeListener());
	}

	/**
	 * Wählt den konkreten HTML-Viewer aus.
	 * @return	Zu verwendender HTML-Viewer.
	 */
	protected abstract HTMLBrowserPanel getHTMLBrowser();

	/**
	 * Liefert das eigentliche Browser-Element als <code>JComponent</code>-Objekt zurück
	 * @return	Eigentliches Browser-Element
	 */
	public JComponent getBrowserJComponent() {
		return textPane.asInnerJComponent();
	}

	/**
	 * Konstruktor der Klasse <code>HTMLPanel</code>
	 * (Vorwärts-, Zurück- und Inhalt-Schaltflächen werden angezeigt.)
	 * @param closeNotify	Wird aufgerufen, wenn der Nutzer auf die Schließen-Schaltfläche klickt.
	 */
	public HTMLPanel(Runnable closeNotify) {
		this(true,true,closeNotify);
	}

	/**
	 * Fügt eine neue Schaltfläche zur Symbolleiste {@link #toolBar} hinzu.
	 * @param title	Titel der Schaltfläche
	 * @param tip	Tooltip für die Schaltfläche (darf <code>null</code> sein)
	 * @param icon	Icon für die Schaltfläche (darf <code>null</code> sein)
	 * @return	Liefert die bereits eingefügte Schaltfläche.
	 * @see #toolBar
	 */
	private JButton addButton(final String title, final String tip, final Icon icon) {
		JButton button=new JButton(title);
		if (tip!=null && !tip.equals("")) button.setToolTipText(tip);
		if (icon!=null) button.setIcon(icon);
		toolBar.add(button);
		button.addActionListener(new ButtonListener());
		return button;
	}

	/**
	 * Registriert ein {@link Runnable}-Objekt, welches aufgerufen wird,
	 * wenn der Nutzer auf einen Link klickt, der keine URL enthält.
	 * @param processSpecialLink {@link Runnable}-Objekt, welche über das Klicken auf den besonderen Link informiert wird.
	 * @see #getSpecialLink()
	 */
	public void setProcessSpecialLink(Runnable processSpecialLink) {
		this.processSpecialLink=processSpecialLink;
	}

	/**
	 * Klickt der Nutzer auf einen Link, der keine URL enthält, so wird hier das angegebene Link-Ziel zurückgegeben.
	 * @return	Link-Ziel bei besonderen Links
	 * @see #setProcessSpecialLink(Runnable)
	 */
	public String getSpecialLink() {
		return specialLink;
	}

	/**
	 * Stellt die Seite ein, die über die "Startseite"-Schaltfläche erreichbar sein soll.
	 * @param file	Startseiten-Datei
	 * @return	Gibt <code>true</code> zurück, wenn die Datei erfolgreich geladen werden konnte.
	 */
	public boolean setHome(File file) {
		try {setHome(file.toURI().toURL());} catch (MalformedURLException e) {return false;}
		return true;
	}

	/**
	 * Stellt die Seite ein, die über die "Startseite"-Schaltfläche erreichbar sein soll.
	 * @param url	URL zu der Seite, die als Startseite verwendet werden soll.
	 */
	public void setHome(URL url) {
		homeURL=url;
	}

	/**
	 * Ruft die Startseite auf (sofern zuvor per <code>setHome</code> eine gesetzt wurde).
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 * @see #setHome(File)
	 * @see #setHome(URL)
	 */
	public boolean goHome() {
		if (homeURL==null) return false;
		return loadPage(homeURL);
	}

	/**
	 * Zeigt die als Parameter übergebene Seite an.
	 * @param file	Anzuzeigende Datei
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(File file) {
		try {return loadPage(file.toURI().toURL());} catch (MalformedURLException e) {return false;}
	}

	/**
	 * Zeigt die als Parameter übergebene Seite an.
	 * @param res Ressourcen-String zu der anzuzeigenden Datei
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(String res) {
		return loadPage(getPageURL(res));
	}

	/**
	 * Zeigt die als Parameter übergebene Seite an.
	 * @param url URL zu der anzuzeigenden Datei
	 * @return Gibt <code>true</code> zurück, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(URL url) {
		loading=true;
		if (currentURL!=null && url!=null && currentURL.sameFile(url)) {
			boolean ok=true;
			if (currentURL.getRef()==null && url.getRef()!=null) ok=false;
			if (currentURL.getRef()!=null && url.getRef()==null) ok=false;
			if (currentURL.getRef()!=null && url.getRef()!=null && !currentURL.getRef().equals(url.getRef())) ok=false;
			if (ok) return true;
		}

		if (!textPane.showPage(url)) return false;
		if (currentURL!=null) {listBack.add(currentURL); listNext.clear();}
		currentURL=url;
		return true;
	}

	/**
	 * Prüft, ob die angegebene Seite existiert.
	 * @param res	Ressourcen-String zu der zu prüfenden Datei
	 * @return	Gibt <code>true</code> zurück, wenn die Datei existiert.
	 */
	public boolean pageExists(String res) {
		return HTMLPanel.class.getResource(res)!=null;
	}

	/**
	 * Liefert den Pfad zu der angegebenen Datei
	 * @param res Name zu der anzuzeigenden Datei
	 * @return Vollständiger Pfad
	 */
	public abstract URL getPageURL(String res);

	/**
	 * Initialisiert die Einträge zur Auswahl bestimmter Elemente im {@link #contentPopup}.
	 * @see #contentPopup
	 * @see ButtonListener
	 */
	private void initContentPopup() {
		contentPopup.removeAll();
		List<String> content=textPane.getPageContent();
		List<Integer> level=textPane.getPageContentLevel();

		for (int i=0;i<content.size();i++) {
			String s="";
			if (level.get(i)>=4) s="  ";
			if (level.get(i)>=5) s+="   ";
			JMenuItem item=new JMenuItem(s+content.get(i));
			item.addActionListener(new ButtonListener());
			if (level.get(i)==1) item.setIcon(SimToolsImages.HELP_MARKER_LEVEL1.getIcon());
			if (level.get(i)==2) item.setIcon(SimToolsImages.HELP_MARKER_LEVEL2.getIcon());
			contentPopup.add(item);
		}
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen
	 * @see HTMLPanel#buttonClose
	 * @see HTMLPanel#buttonBack
	 * @see HTMLPanel#buttonNext
	 * @see HTMLPanel#buttonHome
	 * @see HTMLPanel#buttonContent
	 * @see HTMLPanel#buttonSearch
	 */
	private final class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source=e.getSource();

			if (source==buttonClose) {
				if (closeNotify!=null) closeNotify.run();
				return;
			}

			if (source==buttonBack) {
				if (currentURL!=null) listNext.add(currentURL);
				currentURL=listBack.get(listBack.size()-1);
				listBack.remove(listBack.size()-1);
				textPane.showPage(currentURL);
				return;
			}

			if (source==buttonNext) {
				if (currentURL!=null) listBack.add(currentURL);
				currentURL=listNext.get(listNext.size()-1);
				listNext.remove(listNext.size()-1);
				textPane.showPage(currentURL);
				return;
			}

			if (source==buttonHome) {
				loadPage(homeURL);
				return;
			}

			if (source==buttonContent && !textPane.getPageContent().isEmpty()) {
				initContentPopup();
				contentPopup.show(buttonContent,0,buttonContent.getBounds().height);
				return;
			}

			if (source==buttonSearch) {
				final HTMLPanelSearchDialog searchDialog=new HTMLPanelSearchDialog(HTMLPanel.this);
				if (searchDialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
					final Set<String> results=searchDialog.getResult();
					if (results!=null && results.size()>0) {
						final String[] pages=results.toArray(String[]::new);
						if (pages.length==1) {
							loadPage(pages[0]);
						} else {
							final HTMLPanelSelectDialog selectDialog=new HTMLPanelSelectDialog(HTMLPanel.this,results);
							if (selectDialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
								final String page=selectDialog.getSelectedPage();
								if (page!=null) loadPage(page);
							}
						}
					}
				}
			}

			if (source==buttonPrint) {
				if (!printPage()) {
					MsgBox.error(HTMLPanel.this,HelpBase.errorPrintTitle,HelpBase.errorPrintInfo);
				}
				return;
			}

			if (source instanceof JMenuItem) {
				int i=contentPopup.getComponentIndex((JMenuItem)source);
				if (i>=0) textPane.scrollToPageContent(i);
				return;
			}
		}
	}

	/**
	 * Lädt den Inhalt einer durch eine URL spezifizierten Textdatei.
	 * @param url	URL der zu ladenden Textdatei
	 * @return	Liefert im Erfolgsfall die Zeilen der Datei, sonst <code>null</code>
	 */
	private List<String> loadFile(final URL url) {
		final List<String> lines=new ArrayList<>();
		try(BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream(),StandardCharsets.UTF_8))) {
			String line=null;
			while ((line=br.readLine())!=null) lines.add(line);
		} catch (IOException e) {
			return null;
		}
		return lines;
	}

	/**
	 * Druckt die aktuelle Seite aus.
	 * @return	Liefert <code>true</code>, wenn die Daten zusammengestellt werden konnten und das Ergebnis ans Betriebssystem zum Ausdrucken übergeben werden konnte.
	 */
	private boolean printPage() {
		/* Seite Laden */
		final List<String> lines=loadFile(currentURL);
		if (lines==null) return false;

		/* CSS integrieren */
		for (int i=0;i<lines.size();i++) {
			if (lines.get(i).contains("link rel=\"stylesheet\"")) lines.set(i,processCSSLine(lines.get(i)));
		}

		/* Speichern */
		final File file;
		try {
			file=File.createTempFile("Print",".html");
		} catch (IOException e) {
			return false;
		}
		if (!Table.saveTextToFile(String.join("\n",lines),file)) return false;
		file.deleteOnExit();

		/* System-Drucken-Funktion aktivieren */
		try {
			Desktop.getDesktop().print(file);
		} catch (IOException e) {
			try {
				MsgBox.error(HTMLPanel.this,HelpBase.errorPrintTitle,HelpBase.errorPrintInfoNoHandler);
				Desktop.getDesktop().open(file);
			} catch (IOException e1) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Lädt die in einer HTML-Zeile angegebene CSS-Datei und liefert ihren Inhalt inkl. umschließender HTML-Tags zurück.
	 * @param cssLine	HTML-Zeile, in der ein "&lt;link ...&gt;"-Tag ausgewertet werden soll.
	 * @return	Im Erfolgsfall der Inhalt der CSS-Datei inkl. umschließender HTML-Tags; sonst einfach die im Parameter übergebene HTML-Zeile.
	 */
	private String processCSSLine(final String cssLine) {
		final int index1=cssLine.indexOf("href=");
		if (index1<0 || index1+7>=cssLine.length()) return cssLine;
		final char delimeter=cssLine.charAt(index1+5);
		final int index2=cssLine.indexOf(delimeter,index1+6);
		if (index2<0) return cssLine;

		final String cssFileName=cssLine.substring(index1+6,index2);
		final String htmlURL=currentURL.toString();
		final int index3=htmlURL.lastIndexOf("/");
		if (index3<0) return cssLine;
		final URL cssURL;
		try {
			cssURL=new URI(htmlURL.substring(0,index3+1)+cssFileName).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			return cssLine;
		}

		final List<String> css=loadFile(cssURL);
		if (css==null) return cssLine;

		css.add(0,"<style>");
		css.add("</style>");
		return String.join("\n",css);
	}

	/**
	 * Lade-Lock
	 * @see HTMLPanel.PageLoadListener
	 * @see #waitPageLoadDone()
	 */
	private final Object lockObject=new Object();

	/**
	 * Reagiert darauf, wenn das Laden einer Seite in {@link HTMLPanel#textPane}
	 * abgeschlossen ist (und stellt die Vor/Zurück-Schaltflächen usw. korrekt ein).
	 * @see HTMLPanel#textPane
	 */
	private final class PageLoadListener implements Runnable {
		/**
		 * Konstruktor der Klasse
		 */
		public PageLoadListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			buttonHome.setVisible(homeURL!=null);
			buttonHome.setEnabled(homeURL!=null	&& (currentURL==null || !homeURL.sameFile(currentURL)));
			buttonBack.setEnabled(!listBack.isEmpty());
			buttonNext.setEnabled(!listNext.isEmpty());
			buttonContent.setVisible(showContent && !textPane.getPageContent().isEmpty());

			synchronized(lockObject) {
				loading=false; lockObject.notify();
			}
		}
	}

	/**
	 * Wartet bis der Ladevorgang einer Seite abgeschlossen ist.
	 */
	public final void waitPageLoadDone() {
		if (noLockingViewer) return;
		if (!loading) return;
		synchronized(lockObject){
			try {if (loading) lockObject.wait(10_000);} catch (InterruptedException e) {}
		}
	}

	/**
	 * Reagiert auf das Anklicken von Links innerhalb der HTML-Anzeige.
	 * @see HTMLPanel#textPane
	 * @see HTMLPanel#processSpecialLink
	 */
	private final class LinkClickListener implements Runnable {
		/**
		 * Konstruktor der Klasse
		 */
		public LinkClickListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void run() {
			URL url=textPane.getLastClickedURL();

			if (url==null) {
				specialLink=textPane.getLastClickedURLDescription();
				if (processSpecialLink!=null) SwingUtilities.invokeLater(processSpecialLink);
			} else {
				String s=url.toString();
				if (s.toLowerCase().startsWith("mailto:")) {
					try {Desktop.getDesktop().mail(url.toURI());} catch (IOException | URISyntaxException e1) {
						MsgBox.error(HTMLPanel.this,HelpBase.errorNoEMailTitle,String.format(HelpBase.errorNoEMailInfo,url.toString()));
					}
				} else {
					if (s.toLowerCase().startsWith("http://") || s.toLowerCase().startsWith("https://")) {
						JOpenURL.open(HTMLPanel.this,url);
					} else {
						s=url.toString();
						s=s.substring(s.lastIndexOf('/')+1);
						loadPage(getPageURL(s));
					}
				}
			}
		}
	}

	/**
	 * Listener, der auf Escape-Tastendrücke reagiert
	 */
	private final class EscapeListener extends AbstractAction {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 3060385322767789283L;

		/**
		 * Konstruktor der Klasse
		 */
		public EscapeListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			specialLink="special:escape";
			if (processSpecialLink!=null) SwingUtilities.invokeLater(processSpecialLink);
		}
	}

	/**
	 * Optionale Vorverarbeitung der geladenen Seite
	 * @param root	Wurzelelement der Seite
	 */
	protected void preprocessPage(final Element root) {
	}
}