/**
 * Copyright 2021 Alexander Herzog
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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import tools.SetupData;

/**
 * Blendet eine Informationsmeldung als halbtransparentes Overlay
 * in einem Fenster oder Dialog ein.
 * @author Alexander Herzog
 */
public class GlassInfo {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt nur statische Hilfsmethoden zur Verf�gung.
	 */
	private GlassInfo() {
	}

	/**
	 * Minimaler Zeitabstand (in Sekunden) nach der dieselbe Meldung erneut eingeblendet wird.
	 */
	private static final int MIN_REPEAT_SECONDS=15;

	/**
	 * Anzahl an Sekunden, f�r die die Meldung angezeigt werden soll
	 */
	private static final int DISPLAY_SECONDS=4;

	/**
	 * Zuletzt angezeigte Meldung.
	 * @see #info(Component, String, int)
	 */
	private static String lastText;

	/**
	 * Zeitpunkt an dem {@link #lastText} eingeblendet wurde.
	 * @see #info(Component, String, int)
	 */
	private static long lastTime;

	/**
	 * Zeigt eine Meldung in Form eines halbtransparenten Overlays an.
	 * @param parent	Element in dessen Elternfenster das Overlay angezeigt werden soll.
	 * @param info	Auszugebender html-Text (ohne html-Start- und End-Tags)
	 * @param width	Maximale Breite des Overlays
	 */
	public static void info(final Component parent, final String info, final int width) {
		if (info==null || info.trim().isEmpty()) return;
		if (parent==null) return;

		/* Setup: Anzeige von Infos aktiv? */
		if (!SetupData.getSetup().surfaceGlassInfos) return;

		/* Glass-Panel f�r aktuelles Fenster ermitteln */
		final Window window;
		if (parent instanceof Window) window=(Window)parent; else window=SwingUtilities.getWindowAncestor(parent);

		Component glass=null;
		if ((window instanceof JFrame)) glass=((JFrame)window).getGlassPane();
		if ((window instanceof JDialog)) glass=((JDialog)window).getGlassPane();
		if (!(glass instanceof JPanel)) return;
		final JPanel glassPane=(JPanel)glass;

		/* Wird bereits ein Informationstext angezeigt? */
		if (glassPane.isVisible()) return;

		/* Text nicht zu h�ufig anzeigen */
		if (info.equals(lastText) && lastTime+1000*(MIN_REPEAT_SECONDS+DISPLAY_SECONDS)>System.currentTimeMillis()) return;
		lastText=info;
		lastTime=System.currentTimeMillis();

		/* Bisherige Anzeige entfernen */
		glassPane.removeAll();

		/* Neue Ausgabe erstellen */
		glassPane.setLayout(new BoxLayout(glassPane,BoxLayout.PAGE_AXIS));
		glassPane.add(Box.createVerticalGlue());
		glassPane.add(Box.createVerticalGlue());
		glassPane.add(Box.createVerticalGlue());
		glassPane.add(Box.createVerticalGlue());
		final JPanel infoPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.add(new JLabel("<html><body style='color: white; font-size: large; font-weight: bold;'>"+info+"</body></html>"));
		infoPanel.setBackground(new Color(0,0,0,64));
		infoPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		infoPanel.setMaximumSize(new Dimension(width,1000));
		infoPanel.setOpaque(true);
		glassPane.add(infoPanel,BorderLayout.CENTER);
		glassPane.add(Box.createVerticalGlue());

		/* Aktivieren */
		glassPane.setVisible(true);

		/* Abschalt-Timer */
		final Timer timer=new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				glassPane.setVisible(false);
			}
		},1000*DISPLAY_SECONDS);
	}
}