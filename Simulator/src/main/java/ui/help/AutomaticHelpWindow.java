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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import language.Language;
import systemtools.help.HelpBase;
import ui.EditorPanel;
import ui.MainPanel;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Diese Klasse stellt neben dem Hauptfenster ein schmales vertikales Fenster dar
 * in dem die Hilfeseite der jeweils auf der Zeichenfläche gewählten Station
 * angezeigt wird.
 * @author Alexander Herzog
 * @see EditorPanel
 */
public class AutomaticHelpWindow extends JFrame {
	private static final long serialVersionUID = -7226053471343055556L;

	private final JFrame mainFrame;
	private Point saveMainFrameLocation;
	private Dimension saveMainFrameSize;

	private final EditorPanel editorPanel;
	private final transient ActionListener selectionListener;

	private String lastPage;
	private final transient AutomaticHelp help;

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

		/* Fläche für Hilfe-Panel vorbereiten */
		getContentPane().setLayout(new BorderLayout());
		help=new AutomaticHelp(getContentPane());

		/* Callback initiieren */
		this.editorPanel=editorPanel;
		editorPanel.addSelectionListener(selectionListener=new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {selectionChanged();}
		});

		/* Verarbeitung starten */
		selectionChanged();

		/* Fenster anzeigen */
		setVisible(true);
	}

	/**
	 * Schließt das Fenster wieder. Wird entweder durch das Klicken des
	 * Nutzers auf die Schließen-Schaltfläche ausgelöst oder von einer
	 * neuen Instanz, die zunächst das Fenster der alten Instanz schließt.
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
	 * Schließt ein möglicherweise offenes Tutorial-Fenster.
	 */
	public static void closeWindow() {
		if (currentWindow!=null) currentWindow.close();
	}

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

	private class AutomaticHelp extends HelpBase {
		private JPanel helpPanel;
		private final Container parent;

		public AutomaticHelp(final Container parent) {
			super(parent);
			this.parent=parent;
		}

		@Override
		protected URL getPageURL(String res) {
			return getClass().getResource("pages_"+Language.tr("Numbers.Language")+"/"+res);
		}

		public void showPage(final String page) {
			if (parent==null) return;
			if (helpPanel!=null) parent.remove(helpPanel);
			parent.add(helpPanel=getHTMLPanel(page),BorderLayout.CENTER);
		}
	}
}