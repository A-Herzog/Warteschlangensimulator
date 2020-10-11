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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;

/**
 * Dieser Dialog zeigt die Lizenz der im Programm verwendeten Komponenten an.
 * @author Alexander Herzog
 */
public class LicenseViewer extends BaseDialog{
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6622197213633282594L;

	/**
	 * HTML-Kopf f�r die Ausgabe der html-formatierten Lizenztexte.
	 * @see #htmlFooter
	 */
	private static final String htmlHeader="<html><head><style>body {font-family: sans-serif; margin: 5px;}</style></head><body>";

	/**
	 * HTML-Fu�bereich f�r die Ausgabe der html-formatierten Lizenztexte.
	 * @see #htmlHeader
	 */
	private static final String htmlFooter="</body></html>";

	/**
	 * F�r welchen Programmbestandteil sollen die Lizenzen angezeigt werden?
	 * @author Alexander Herzog
	 */
	enum LicensePart {
		/** Lizenz f�r das Programm selber */
		MAIN(()->Language.tr("LicenseViewer.Part.Main"),"../LICENSE","docs/license.txt"),
		/** Lizenz der Komponenten, die SimSystem nutzt */
		SIMSYSTEM_COMPONENTS(()->Language.tr("LicenseViewer.Part.Components.SimSystem"),"../SimSystem/LICENSE_COMPONENTS.md","docs/license_components_simsystem.md"),
		/** Lizenz der Komponenten, die SimTools nutzt */
		SIMTOOLS_COMPONENTS(()->Language.tr("LicenseViewer.Part.Components.SimTools"),"../SimTools/LICENSE_COMPONENTS.md","docs/license_components_simtools.md"),
		/** Lizenz der Komponenten, die Simulator nutzt */
		SIMULATOR_COMPONENTS(()->Language.tr("LicenseViewer.Part.Components.Simulator"),"LICENSE_COMPONENTS.md","docs/license_components_simulator.md");

		/** �berschrift �ber dem zugeh�rigen Tab */
		private final Supplier<String> name;
		/** Anzuzeigende Dateien (die Varianten werden der Reihe nach durchprobiert) */
		private final String[] files;

		/**
		 * Konstruktor des Enum
		 * @param name	�berschrift �ber dem zugeh�rigen Tab
		 * @param files	Anzuzeigende Dateien (die Varianten werden der Reihe nach durchprobiert)
		 */
		LicensePart(final Supplier<String> name, final String... files) {
			this.name=name;
			this.files=files;
		}

		/**
		 * Liefert den Namen des Bereichs f�r den Titel des Tabs.
		 * @return	Namen des Bereichs f�r den Titel des Tabs
		 */
		public String getName() {
			return name.get();
		}

		/**
		 * Liefert die anzuzeigende Datei.
		 * @return	Anzuzeigende Datei oder <code>null</code>, wenn keine Datei gefunden werden konnte
		 */
		public File getFile() {
			if (files==null) return null;
			for (String file: files) {
				final File test=new File(SetupData.getProgramFolder(),file);
				if (test.isFile()) return test;
			}
			return null;
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Element
	 */
	public LicenseViewer(final Component owner) {
		super(owner,Language.tr("LicenseViewer.Title"));

		/* Allgemeines */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		/* Tabs anlegen */
		addViewer(tabs,LicensePart.MAIN);
		addViewer(tabs,LicensePart.SIMSYSTEM_COMPONENTS);
		addViewer(tabs,LicensePart.SIMTOOLS_COMPONENTS);
		addViewer(tabs,LicensePart.SIMULATOR_COMPONENTS);

		/* Starten */
		setMinSizeRespectingScreensize(600,500);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * F�gt ein Lizenz-Viewer-Tab zu dem Dialog hinzu.
	 * @param tabs	Tabs-Element zu dem der neue Tab hinzugef�gt werden soll
	 * @param licensePart	Welche Daten sollen in dem Tab angezeigt werden?
	 */
	private void addViewer(final JTabbedPane tabs, final LicensePart licensePart) {
		if (licensePart==null) return;
		final File file=licensePart.getFile();
		if (file==null) return;

		tabs.addTab(licensePart.getName(),getViewer(file));
	}

	/**
	 * L�dt eine Datei (Text oder MD) und liefert ein Viewer-Panel f�r diese.
	 * @param file	Zu ladende Datei
	 * @return	Neues Viewer-Panel
	 */
	private JPanel getViewer(final File file) {
		if (file==null) return getTextViewer(null);

		final String name=file.toString().toUpperCase();
		if (name.endsWith(".MD")) return getMDViewer(file);
		return getTextViewer(file);
	}

	/**
	 * L�dt eine einfache Textdatei und liefert ein Viewer-Panel f�r diese.
	 * @param file	Zu ladende Datei
	 * @return	Neues Viewer-Panel
	 */
	private JPanel getTextViewer(final File file) {
		final String content=getHTMLFromText(file);
		return getViewer(content);
	}

	/**
	 * L�dt eine Markdown-Datei und liefert ein Viewer-Panel f�r diese.
	 * @param file	Zu ladende Datei
	 * @return	Neues Viewer-Panel
	 */
	private JPanel getMDViewer(final File file) {
		final String content=getHTMLFromMD(file);
		return getViewer(content);
	}

	/**
	 * Erstellt ein Viewer-Panel und f�llt dieses mit einem HTML-Text.
	 * @param content	HTML-Text f�r den Viewer
	 * @return	Neues Viewer-Panel
	 */
	private JPanel getViewer(final String content) {
		final JTextPane viewer=new JTextPane();

		viewer.setEditable(false);
		viewer.addHyperlinkListener(e->linkProcessor(e));
		viewer.setEditorKit(new HTMLEditorKit());
		try {
			viewer.read(new ByteArrayInputStream(content.getBytes()),null);
		} catch (IOException e) {}

		final JPanel panel=new JPanel(new BorderLayout());
		panel.add(new JScrollPane(viewer),BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Liefert eine Fehlermeldung als HTML-Zeichenkette.
	 * @param file	Datei, die nicht geladen werden konnte
	 * @return	Fehlermeldung als HTML-Zeichenkette
	 */
	private String getHTMLError(final File file) {
		return htmlHeader+"<p>"+String.format(Language.tr("LicenseViewer.FileError"),file.toString())+"</p>"+htmlFooter;
	}

	/**
	 * L�dt eine einfache Textdatei und erstellt daraus eine HTML-Zeichenkette
	 * @param file	Zu ladende Datei
	 * @return	Liefert die HTML-Zeichenkette (falls die Datei nicht geladen werden konnte, eine HTML-Fehlermeldung)
	 */
	private String getHTMLFromText(final File file) {
		if (file==null) return getHTMLError(new File("nofile"));
		if (!file.isFile()) return getHTMLError(file);

		String text="";
		try {
			text=String.join("\n",Files.readAllLines(file.toPath()).toArray(new String[0]));
		} catch (IOException e) {
			return getHTMLError(file);
		}

		return htmlHeader+"<pre><code>"+text+"</code></pre>"+htmlFooter;
	}

	/**
	 * L�dt eine einfache Markdown-Datei und erstellt daraus eine HTML-Zeichenkette
	 * @param file	Zu ladende Datei
	 * @return	Liefert die HTML-Zeichenkette (falls die Datei nicht geladen werden konnte, eine HTML-Fehlermeldung)
	 */
	private String getHTMLFromMD(final File file) {
		if (file==null) return getHTMLError(new File("nofile"));
		if (!file.isFile()) return getHTMLError(file);

		String md="";
		try {
			md=String.join("\n",Files.readAllLines(file.toPath()).toArray(new String[0]));
		} catch (IOException e) {
			return getHTMLError(file);
		}

		final List<Extension> extensions=Arrays.asList(AutolinkExtension.create());
		final Parser parser=Parser.builder().extensions(extensions).build();
		final Node document=parser.parse(md);
		final HtmlRenderer renderer=HtmlRenderer.builder().extensions(extensions).build();
		return htmlHeader+renderer.render(document)+htmlFooter;
	}

	/**
	 * Reagiert auf Klicks und Mausbewegungen �ber die Links
	 * in den HTML-Viewern
	 * @param e	Hyperlink-Ereignis, auf das reagiert werden soll
	 * @see #getViewer(String)
	 */
	private void linkProcessor(final HyperlinkEvent e) {
		if (e.getEventType()==HyperlinkEvent.EventType.ENTERED) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			return;
		}

		if (e.getEventType()==HyperlinkEvent.EventType.EXITED) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return;
		}

		if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
			try {
				if (!MsgBox.confirmOpenURL(this,e.getURL())) return;
				Desktop.getDesktop().browse(e.getURL().toURI());
			} catch (IOException | URISyntaxException e1) {
				MsgBox.error(getOwner(),Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),e.getURL().toString()));
			}
		}
	}
}
