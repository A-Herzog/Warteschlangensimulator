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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import language.Language;
import systemtools.help.HTMLBrowser;
import systemtools.help.HTMLBrowserPanel;
import systemtools.help.HelpBase;
import ui.MainPanel;
import ui.tutorial.pages.PageAddDispose;
import ui.tutorial.pages.PageAddProcess;
import ui.tutorial.pages.PageAddSource;
import ui.tutorial.pages.PageCloseConnect;
import ui.tutorial.pages.PageConnect1;
import ui.tutorial.pages.PageConnect2;
import ui.tutorial.pages.PageDone;
import ui.tutorial.pages.PageOpenConnect;
import ui.tutorial.pages.PageOpenTemplates;
import ui.tutorial.pages.PageSetupModel;
import ui.tutorial.pages.PageSetupProcess;
import ui.tutorial.pages.PageSetupSource;
import ui.tutorial.pages.PageStart;

/**
 * Dieses Fenster fügt sich selbst neben dem Hauptfenster ein und zeigt ein
 * interaktives Tutorial an. Es muss dafür mit dem <code>MainPanel</code>
 * verknüpft werden.
 * @author Alexander Herzog
 * @see MainPanel
 */
public class TutorialWindow extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2770321276617641597L;

	/** Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll. */
	private final MainPanel mainPanel;
	/** Fenster in dem sich das {@link #mainPanel} befindet */
	private final JFrame mainFrame;
	/** Position des Hauptfesnters vor dem Aufruf des Tutorial-Fensters */
	private Point saveMainFrameLocation;
	/** Größe des Hauptfesnters vor dem Aufruf des Tutorial-Fensters */
	private Dimension saveMainFrameSize;

	/** Aktuelle Sprache */
	private final String language;
	/** Aktuelle Tutorial-Seite */
	private TutorialPage page=null;
	/** Liste der Tutorial-Seiten */
	private final List<TutorialPage> pages;

	/** Regelmäßige Prüfung des Modells */
	private final Timer timer;
	/** Hilfe-Browser für die Tutorial-Seiten */
	private final HTMLBrowserPanel browser;

	/**
	 * Tutorial-Fensters<br>
	 * (Da es nur ein Tutorial-Fenster geben darf, schließt
	 * der Konstruktor ggf. vorherige Tutorial-Fenster.)
	 */
	private static TutorialWindow currentTutorialWindow=null;

	/**
	 * Konstruktor der Klasse <code>TutorialWindow</code>.<br>
	 * Der Konstruktor erstellt das Fenster und macht es auch sichtbar.
	 * War bereits eine Instanz dieser Klasse sichtbar, so wird
	 * diese zunächst ausgeblendet.
	 * @param mainPanel	Programm-<code>MainPanel</code> mit dem das interaktive Tutorial zusammenarbeiten soll.
	 */
	public TutorialWindow(final MainPanel mainPanel) {
		super(Language.tr("InteractiveTutorial.Title"));
		if (currentTutorialWindow!=null) currentTutorialWindow.close();
		currentTutorialWindow=this;
		this.mainPanel=mainPanel;

		/* Fenster zu Panel finden */
		Component c=mainPanel;
		while (c!=null) {if (c instanceof JFrame) break; c=c.getParent();}
		mainFrame=(JFrame)c;

		/* Position des Hauptfensters speichern, Hauptfenster und Tutorialfenster anordnen */
		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		final Insets insets=Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		final int w=screenSize.width-insets.left-insets.right;
		final int h=screenSize.height-insets.top-insets.bottom;
		if (mainFrame!=null) {
			saveMainFrameLocation=mainFrame.getLocation();
			saveMainFrameSize=mainFrame.getSize();
			mainFrame.setLocation(insets.left,insets.top);
			mainFrame.setSize(3*w/4,h);
		}
		setLocation(3*w/4+insets.left,insets.top);
		setSize(1*w/4,h);

		/* Beim Schließen Hauptfenster wiederherstellen */
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent event) {close();}
		});

		/* Icon */
		if (mainFrame!=null) {
			setIconImage(mainFrame.getIconImage());
		}

		/* Liste mit allen verfügbaren Seiten aufbauen */
		language=Language.tr("Numbers.Language");
		pages=new ArrayList<>();
		buildPagesList();

		/* GUI aufbauen */
		browser=HTMLBrowser.getBrowser(HelpBase.ViewerMode.HTML_VIEWER_SWING);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(browser.asScrollableJComponent(),BorderLayout.CENTER);
		loadPage(pages.get(0).getPageName());

		browser.init(()->loadPage(browser.getLastClickedURLDescription()),null);

		/* Prüfen, ob Bedingung für nächste Seite erfüllt ist */
		timer=new Timer("TutorialModelSupervisor");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (page==null) return;
				final String nextPage=page.checkNextCondition(TutorialWindow.this.mainPanel);
				if (nextPage!=null) {page=null; loadPage(nextPage);}
			}
		},250,250);

		/* Fenster anzeigen */
		setVisible(true);
	}

	/**
	 * Schließt das Fenster wieder. Wird entweder durch das Klicken des
	 * Nutzers auf die Schließen-Schaltfläche ausgelöst oder von einer
	 * neuen Instanz, die zunächst das Fenster der alten Instanz schließt.
	 */
	private void close() {
		if (mainFrame!=null) {
			mainFrame.setLocation(saveMainFrameLocation);
			mainFrame.setSize(saveMainFrameSize);
		}
		page=null;
		timer.cancel();
		setVisible(false);
		currentTutorialWindow=null;
	}

	/**
	 * Schließt ein möglicherweise offenes Tutorial-Fenster.
	 */
	public static void closeTutorialWindow() {
		if (currentTutorialWindow!=null) currentTutorialWindow.close();
	}

	/**
	 * In dieser Methode müssen alle Seiten, die im Rahmen des Tutorials
	 * angezeigt werden sollen, registriert werden.<br>
	 * Die erste hier registrierte Seite fungiert dabei automatisch als Startseite.
	 * @see TutorialPage
	 */
	private void buildPagesList() {
		pages.add(new PageStart());
		pages.add(new PageOpenTemplates());
		pages.add(new PageAddSource());
		pages.add(new PageAddProcess());
		pages.add(new PageAddDispose());
		pages.add(new PageOpenConnect());
		pages.add(new PageConnect1());
		pages.add(new PageConnect2());
		pages.add(new PageCloseConnect());
		pages.add(new PageSetupSource());
		pages.add(new PageSetupProcess());
		pages.add(new PageSetupModel());
		pages.add(new PageDone());
	}

	/**
	 * Zeigt eine bestimmte Seite an.<br>
	 * Es wird die hier angegebene html-Seite angezeigt und gleichzeitig das
	 * zugehörige <code>TutorialPage</code>-Element aktiviert.
	 * @param name	Name der anzuzeigenden Seite (ohne Pfad und ohne Extension)
	 * @see TutorialPage
	 * @see #buildPagesList()
	 */
	private void loadPage(final String name) {
		if (name.equals("CloseTutorial")) {
			close();
			return;
		}

		final URL url=getClass().getResource("pages_"+language+"/"+name+".html");
		if (url!=null) browser.showPage(url);
		TutorialPage current=null;
		for (TutorialPage page: pages) if (page.getPageName().equalsIgnoreCase(name)) {current=page; break;}
		if (current!=null) page=current;
	}
}