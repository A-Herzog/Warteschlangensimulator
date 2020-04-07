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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import systemtools.images.SimToolsImages;

/**
 * Zeigt eine Internetseite in einem Fenster an.
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 * @see HTMLPanel
 */
abstract class HTMLFrame extends JFrame {
	private static final long serialVersionUID = -8774433584734830953L;

	private final HTMLPanel panel;
	private final Runnable specialLinks;
	private String specialLink;

	/**
	 * Konstruktor der Klasse
	 * @param owner	�bergeordnetes Fenster
	 * @param title Titel des Fensters
	 * @param specialLinks Objekt vom Typ <code>Runnable</code>, welches (wenn ungleich <code>null</code>) aufgerufen wird, wenn ein Nicht-URL-Link angeklickt wird.
	 * @see #getSpecialLink()
	 */
	public HTMLFrame(Window owner, String title, Runnable specialLinks) {
		super();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(title);

		this.specialLinks=specialLinks;

		/* Fenster-Icon*/
		setIconImage(getToolkit().getImage(SimToolsImages.HELP.getURL()));

		/* HTMLPanel anlegen */
		Container p=getContentPane();
		p.setLayout(new BorderLayout());
		panel=new HTMLPanel(()->{setVisible(false); dispose();}){
			private static final long serialVersionUID = 3777239383887019425L;
			@Override
			protected HTMLBrowserPanel getHTMLBrowser() {
				return HTMLFrame.this.getHTMLBrowser();
			}
			@Override
			public URL getPageURL(String res) {
				return HTMLFrame.this.getPageURL(res);
			}
		};
		p.add(panel,BorderLayout.CENTER);

		panel.setHome(panel.getPageURL(HelpBase.CONTENT_PAGE));
		panel.setProcessSpecialLink(new SpecialLink());

		setSize(750,650);
		setMinimumSize(getSize());
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=new JRootPane();
		KeyStroke stroke=KeyStroke.getKeyStroke("ESCAPE");
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke,"ESCAPE");
		rootPane.getActionMap().put("ESCAPE",new CloseListener());
		return rootPane;
	}

	/**
	 * Liefert den Pfad zu der angegebenen Datei
	 * @param res Name zu der anzuzeigenden Datei
	 * @return Vollst�ndiger Pfad
	 */
	protected abstract URL getPageURL(String res);

	/**
	 * W�hlt den konkreten HTML-Viewer aus.
	 * @return	Zu verwendender HTML-Viewer.
	 */
	protected abstract HTMLBrowserPanel getHTMLBrowser();

	private class CloseListener extends AbstractAction {
		private static final long serialVersionUID = -485008309903554823L;
		@Override
		public void actionPerformed(ActionEvent actionEvent) {setVisible(false); dispose();}
	}

	/**
	 * Zeigt die im Parameter �bergebene Seite an.
	 * @param topic	Name der Seite ohne einleitendes "HTML/" und ohne abschlie�endes ".html"
	 */
	public void showPage(String topic) {
		if (topic==null || topic.isEmpty()) {
			panel.goHome();
		} else {
			if (!panel.loadPage(panel.getPageURL(topic+".html"))) panel.goHome();
		}
		panel.waitPageLoadDone();
	}

	/**
	 * Gibt zur�ck, welcher Nicht-URL-Link angeklickt wurde
	 * @return Name des Nicht-URL-Links
	 */
	public String getSpecialLink() {
		return specialLink;
	}

	private final class SpecialLink implements Runnable {
		@Override
		public void run() {
			if (panel.getSpecialLink().equalsIgnoreCase("special:escape")) {
				setVisible(false);
				return;
			}
			specialLink=panel.getSpecialLink();
			if (specialLinks!=null) SwingUtilities.invokeLater(specialLinks);
		}
	}
}
