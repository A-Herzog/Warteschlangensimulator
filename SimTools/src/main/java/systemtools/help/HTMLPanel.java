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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Zeigt eine Internetseite in einem <code>JPanel</code> an.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
public abstract class HTMLPanel extends JPanel {
	private static final long serialVersionUID = -3360161989499967773L;

	/** Wird, wenn nicht <code>null</code>, aufgerufen, wenn der Nutzer auf die Schlie�en-Schaltfl�che klickt. */
	private final Runnable closeNotify;

	/** Toolbar des Panels */
	private final JToolBar toolBar;

	/** "Schlie�en"-Schaltfl�che */
	private final JButton buttonClose;

	/** "Start"-Schaltfl�che */
	private final JButton buttonHome;

	/** "Zur�ck"-Schaltfl�che */
	private final JButton buttonBack;

	/** "Weiter"-Schaltfl�che */
	private final JButton buttonNext;

	/** "Inhalt anzeigen"-Schaltfl�che */
	private final JButton buttonContent;

	/** Panel zur Anzeige des Hilfetextes */
	private HTMLBrowserPanel textPane;

	/** Popup zur Anzeige der Inhaltselemente /wird �ber die "Inhalt anzeigen"-Schaltfl�che aktiviert */
	private final JPopupMenu contentPopup;

	/** Gibt an, ob die Toolbar-Schaltfl�che, die eine Popup-Men� mit einer �bersicht der Zwischen�berschriften der Seite enth�lt, angezeigt werden soll. */
	private final boolean showContent;

	/** Liste mit den "Zur�ck"-URLs */
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
	 * Wird <b>kein</b> Locking w�hrend des Ladens ben�tigt?
	 * @see HTMLBrowserPanel#needsLoadLock()
	 * @see #waitPageLoadDone()
	 */
	private boolean noLockingViewer=true;

	/** Callback welches aufgerufen wird, wenn der Nutzer auf einen Link klickt, der keine URL enth�lt. */
	private Runnable processSpecialLink;

	/** Linkziel f�r einen angeklickten Link, der keine URL enth�lt. */
	private String specialLink="";

	/**
	 * Konstruktor der Klasse <code>HTMLPanel</code>
	 * @param showBackAndNext Zeigt die Vorw�rts- und R�ckw�rtsschaltfl�chen an
	 * @param showContent	Zeigt eine Toolbar-Schaltfl�che an, die eine Popup-Men� mit einer �bersicht der Zwischen�berschriften der Seite enth�lt
	 * @param closeNotify	Wird aufgerufen, wenn der Nutzer auf die Schlie�en-Schaltfl�che klickt. (Darf <code>null</code> sein.)
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
		buttonContent=addButton(HelpBase.buttonContent,HelpBase.buttonContentInfo,SimToolsImages.HELP_FIND.getIcon());
		buttonContent.setVisible(false);
		if (showBackAndNext) add(toolBar,BorderLayout.NORTH);

		textPane=getHTMLBrowser();

		noLockingViewer=!textPane.needsLoadLock();

		textPane.init(new LinkClickListener(),new PageLoadListener());

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
	 * W�hlt den konkreten HTML-Viewer aus.
	 * @return	Zu verwendender HTML-Viewer.
	 */
	protected abstract HTMLBrowserPanel getHTMLBrowser();

	/**
	 * Liefert das eigentliche Browser-Element als <code>JComponent</code>-Objekt zur�ck
	 * @return	Eigentliches Browser-Element
	 */
	public JComponent getBrowserJComponent() {
		return textPane.asInnerJComponent();
	}

	/**
	 * Konstruktor der Klasse <code>HTMLPanel</code>
	 * (Vorw�rts-, Zur�ck- und Inhalt-Schaltfl�chen werden angezeigt.)
	 * @param closeNotify	Wird aufgerufen, wenn der Nutzer auf die Schlie�en-Schaltfl�che klickt.
	 */
	public HTMLPanel(Runnable closeNotify) {
		this(true,true,closeNotify);
	}

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
	 * wenn der Nutzer auf einen Link klickt, der keine URL enth�lt.
	 * @param processSpecialLink {@link Runnable}-Objekt, welche �ber das Klicken auf den besonderen Link informiert wird.
	 * @see #getSpecialLink()
	 */
	public void setProcessSpecialLink(Runnable processSpecialLink) {
		this.processSpecialLink=processSpecialLink;
	}

	/**
	 * Klickt der Nutzer auf einen Link, der keine URL enth�lt, so wird hier das angegebene Link-Ziel zur�ckgegeben.
	 * @return	Link-Ziel bei besonderen Links
	 * @see #setProcessSpecialLink(Runnable)
	 */
	public String getSpecialLink() {
		return specialLink;
	}

	/**
	 * Stellt die Seite ein, die �ber die "Startseite"-Schaltfl�che erreichbar sein soll.
	 * @param file	Startseiten-Datei
	 * @return	Gibt <code>true</code> zur�ck, wenn die Datei erfolgreich geladen werden konnte.
	 */
	public boolean setHome(File file) {
		try {setHome(file.toURI().toURL());} catch (MalformedURLException e) {return false;}
		return true;
	}

	/**
	 * Stellt die Seite ein, die �ber die "Startseite"-Schaltfl�che erreichbar sein soll.
	 * @param url	URL zu der Seite, die als Startseite verwendet werden soll.
	 */
	public void setHome(URL url) {
		homeURL=url;
	}

	/**
	 * Ruft die Startseite auf (sofern zuvor per <code>setHome</code> eine gesetzt wurde).
	 * @return Gibt <code>true</code> zur�ck, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 * @see #setHome(File)
	 * @see #setHome(URL)
	 */
	public boolean goHome() {
		if (homeURL==null) return false;
		return loadPage(homeURL);
	}

	/**
	 * Zeigt die als Parameter �bergebene Seite an.
	 * @param file	Anzuzeigende Datei
	 * @return Gibt <code>true</code> zur�ck, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(File file) {
		try {return loadPage(file.toURI().toURL());} catch (MalformedURLException e) {return false;}
	}

	/**
	 * Zeigt die als Parameter �bergebene Seite an.
	 * @param res Ressourcen-String zu der anzuzeigenden Datei
	 * @return Gibt <code>true</code> zur�ck, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
	 */
	public boolean loadPage(String res) {
		return loadPage(HTMLPanel.class.getResource(res));
	}

	/**
	 * Zeigt die als Parameter �bergebene Seite an.
	 * @param url URL zu der anzuzeigenden Datei
	 * @return Gibt <code>true</code> zur�ck, wenn die Seite erfolgreich geladen und angezeigt werden konnte.
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
	 * Pr�ft, ob die angegebene Seite existiert.
	 * @param res	Ressourcen-String zu der zu pr�fenden Datei
	 * @return	Gibt <code>true</code> zur�ck, wenn die Datei existiert.
	 */
	public boolean pageExists(String res) {
		return HTMLPanel.class.getResource(res)!=null;
	}

	/**
	 * Liefert den Pfad zu der angegebenen Datei
	 * @param res Name zu der anzuzeigenden Datei
	 * @return Vollst�ndiger Pfad
	 */
	public abstract URL getPageURL(String res);

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

	private final class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==buttonClose) {
				if (closeNotify!=null) closeNotify.run();
				return;
			}

			if (e.getSource()==buttonBack) {
				if (currentURL!=null) listNext.add(currentURL);
				currentURL=listBack.get(listBack.size()-1);
				listBack.remove(listBack.size()-1);
				textPane.showPage(currentURL);
				return;
			}

			if (e.getSource()==buttonNext) {
				if (currentURL!=null) listBack.add(currentURL);
				currentURL=listNext.get(listNext.size()-1);
				listNext.remove(listNext.size()-1);
				textPane.showPage(currentURL);
				return;
			}

			if (e.getSource()==buttonHome) {
				loadPage(homeURL);
				return;
			}

			if (e.getSource()==buttonContent && !textPane.getPageContent().isEmpty()) {
				initContentPopup();
				contentPopup.show(buttonContent,0,buttonContent.getBounds().height);
				return;
			}

			if (e.getSource() instanceof JMenuItem) {
				int i=contentPopup.getComponentIndex((JMenuItem)e.getSource());
				if (i>=0) textPane.scrollToPageContent(i);
				return;
			}
		}
	}

	/**
	 * Lade-Lock
	 * @see HTMLPanel.PageLoadListener
	 * @see #waitPageLoadDone()
	 */
	private final Object lockObject=new Object();

	private final class PageLoadListener implements Runnable {
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

	private final class LinkClickListener implements Runnable {
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
						if (!MsgBox.confirmOpenURL(HTMLPanel.this,url)) return;
						try {Desktop.getDesktop().browse(url.toURI());} catch (IOException | URISyntaxException e1) {
							MsgBox.error(HTMLPanel.this,HelpBase.errorNoInternetTitle,String.format(HelpBase.errorNoInternetInfo,url.toString()));
						}
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
	 * Listener, der auf Escape-Tastendr�cke reagiert
	 */
	private final class EscapeListener extends AbstractAction {
		private static final long serialVersionUID = 3060385322767789283L;

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			specialLink="special:escape";
			if (processSpecialLink!=null) SwingUtilities.invokeLater(processSpecialLink);
		}
	}
}