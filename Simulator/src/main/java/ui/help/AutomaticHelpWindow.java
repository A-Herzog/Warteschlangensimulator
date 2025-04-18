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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import language.Language;
import systemtools.help.HelpBase;
import ui.EditorPanel;
import ui.MainPanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.tools.FlatLaFHelper;

/**
 * Diese Klasse stellt neben dem Hauptfenster ein schmales vertikales Fenster dar
 * in dem die Hilfeseite der jeweils auf der Zeichenfl�che gew�hlten Station
 * angezeigt wird.
 * @author Alexander Herzog
 * @see EditorPanel
 */
public class AutomaticHelpWindow extends JFrame {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7226053471343055556L;

	/** Hauptfenster */
	private final JFrame mainFrame;
	/** Gespeicherte Position des Hauptfensters vor dem �ffnen dieses Hilfefensters (welche das Hauptfenster verschiebt und seine Gr��e �ndert) */
	private Point saveMainFrameLocation;
	/** Gespeicherte Gr��e des Hauptfensters vor dem �ffnen dieses Hilfefensters (welche das Hauptfenster verschiebt und seine Gr��e �ndert) */
	private Dimension saveMainFrameSize;

	/** {@link EditorPanel} mit dem dieses Hilfefenster zusammenarbeiten soll */
	private final EditorPanel editorPanel;
	/** Callback der von {@link #editorPanel} ausgel�st wird */
	private final transient ActionListener selectionListener;

	/** Aktuell angezeigte Hilfeseite */
	private String lastPage;
	/** Panel das die eigentliche Hilfe anzeigt */
	private final transient AutomaticHelp help;

	/** Da nur ein {@link AutomaticHelpWindow} ge�ffnet sein darf, wird hier das aktuelle Fenster global gespeichert. */
	private static AutomaticHelpWindow currentWindow=null;

	/**
	 * Konstruktor der Klasse
	 * @param mainPanel	Hauptpanel (wird genutzt um das Fensterobjekt zu erhalten um dann dieses anzupassen)
	 * @param editorPanel	{@link EditorPanel} mit dem dieses Hilfefenster zusammenarbeiten soll
	 */
	public AutomaticHelpWindow(final MainPanel mainPanel, final EditorPanel editorPanel) {
		super(Language.tr("AutomaticHelp.Title"));
		if (currentWindow!=null) currentWindow.close();
		currentWindow=this;

		/* Fenster zu Panel finden */
		Component c=mainPanel;
		while (c!=null) {
			if (c instanceof JFrame) break;
			c=c.getParent();
		}
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

		/* Beim Schlie�en Hauptfenster wiederherstellen */
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent event) {close();}
		});

		/* Icon */
		if (mainFrame!=null) {
			setIconImage(mainFrame.getIconImage());
		}

		/* Fl�che f�r Hilfe-Panel vorbereiten */
		getContentPane().setLayout(new BorderLayout());
		help=new AutomaticHelp(getContentPane());

		/* Callback initiieren */
		this.editorPanel=editorPanel;
		editorPanel.addSelectionListener(selectionListener=e->selectionChanged());

		/* Verarbeitung starten */
		selectionChanged();

		/* Fenster anzeigen */
		setVisible(true);
	}

	/**
	 * Schlie�t das Fenster wieder. Wird entweder durch das Klicken des
	 * Nutzers auf die Schlie�en-Schaltfl�che ausgel�st oder von einer
	 * neuen Instanz, die zun�chst das Fenster der alten Instanz schlie�t.
	 */
	private void close() {
		if (editorPanel!=null && selectionListener!=null) editorPanel.removeSelectionListener(selectionListener);
		if (mainFrame!=null) {
			mainFrame.setLocation(saveMainFrameLocation);
			mainFrame.setSize(saveMainFrameSize);
		}
		setVisible(false);
		currentWindow=null;
	}

	/**
	 * Schlie�t ein m�glicherweise offenes Tutorial-Fenster.
	 */
	public static void closeWindow() {
		if (currentWindow!=null) currentWindow.close();
	}

	/**
	 * Reagiert auf die ver�nderte Auswahl im {@link EditorPanel}
	 * und aktualisiert die Hilfeanzeige.
	 * @see EditorPanel#addSelectionListener(ActionListener)
	 * @see AutomaticHelp#showPage(String)
	 */
	private void selectionChanged() {
		if (editorPanel==null) return;

		String page=null;
		final ModelElement element=editorPanel.getSelectedElement();
		if (element!=null) page=element.getHelpPageName();
		if (page==null) page="MainEditor";

		if (lastPage!=null && page.equals(lastPage)) return;
		lastPage=page;

		help.showPage(page);
	}

	/**
	 * Hilfe-Viewer innerhalb des Fensters
	 */
	private class AutomaticHelp extends HelpBase {
		/**
		 * Hilfe-Viewer
		 * @see #showPage(String)
		 * @see HelpBase#getHTMLPanel(String, boolean)
		 */
		private JPanel helpPanel;
		/** �bergeordnetes Element */
		private final Container parent;

		/**
		 * Konstruktor der Klasse
		 * @param parent	�bergeordnetes Element
		 */
		public AutomaticHelp(final Container parent) {
			super(parent);
			this.parent=parent;
		}

		@Override
		protected URL getPageURL(String res) {
			return getClass().getResource("pages_"+Language.tr("Numbers.Language")+"/"+res);
		}

		/**
		 * Zeigt eine Hilfeseite an.
		 * @param page	Anzuzeigende Hilfeseite
		 */
		public void showPage(final String page) {
			if (parent==null) return;
			if (helpPanel!=null) parent.remove(helpPanel);
			parent.add(helpPanel=getHTMLPanel(page,false),BorderLayout.CENTER);
		}

		@Override
		protected void preprocessPage(final Element root) {
			final Document doc=root.getDocument();
			if (!(doc instanceof HTMLDocument)) return;
			final HTMLDocument html=(HTMLDocument)doc;
			final StyleSheet styleSheet=html.getStyleSheet();

			/* Hinweise auf Buchkapitel ausblenden, wenn Buch nicht verf�gbar ist. */
			if (!BookData.getInstance().isDataAvailable()) {
				styleSheet.removeStyle(".bookinfo");
				styleSheet.removeStyle(".bookinfosmall");
				styleSheet.addRule(".bookinfo {color: #F0F0FF; font-size: 1%;} .bookinfosmall {color: #F0F0FF; font-size: 1%;} .bookinfo a {color: #F0F0FF; font-size: 1%;} .bookinfosmall a {color: #F0F0FF; font-size: 1%;}");
			}

			/* Anpassungen f�r FlatLaF-Dark-Mode */
			if (FlatLaFHelper.isDark()) {
				styleSheet.addRule("body {color: #c0c0c0;} a {color: #8080FF;} a.box {background-color: #505050;} div.menu, div.model, div.plaininfo {background-color: #404040;} .bookinfo {background-color: #505050;} .bookinfosmall {background-color: #505050;}");
			}
		}
	}
}