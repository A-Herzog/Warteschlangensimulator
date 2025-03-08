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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
	 * Diese Klasse kann nicht instanziert werden. Sie stellt nur statische Hilfsmethoden zur Verfügung.
	 */
	private GlassInfo() {
	}

	/**
	 * Anzahl an Sekunden, für die die Meldung angezeigt werden soll
	 */
	private static final int DISPLAY_SECONDS=4;

	/**
	 * Bereits angezeigte Meldung.
	 * @see #info(Component, String, int, boolean)
	 */
	private static Set<String> lastText=new HashSet<>();

	/**
	 * Zuordnung von Glas-Panes zu Timern (um diese ggf. vorzeitig abbrechen zu können)
	 * @see #info(Component, String, int, boolean)
	 */
	private static Map<Component,Timer> timers=new HashMap<>();

	/**
	 * Zeigt eine Meldung in Form eines halbtransparenten Overlays an.
	 * @param parent	Element in dessen Elternfenster das Overlay angezeigt werden soll.
	 * @param info	Auszugebender html-Text (ohne html-Start- und End-Tags)
	 * @param width	Maximale Breite des Overlays
	 */
	public static void info(final Component parent, final String info, final int width) {
		info(parent,info,width,false);
	}

	/**
	 * Zeigt eine Meldung in Form eines halbtransparenten Overlays an.
	 * @param parent	Element in dessen Elternfenster das Overlay angezeigt werden soll.
	 * @param info	Auszugebender html-Text (ohne html-Start- und End-Tags)
	 * @param width	Maximale Breite des Overlays
	 * @param force	Anzeige erzwingen, auch wenn noch nicht genug Zeit zwischen der letzten Anzeige und dieser erfolgt ist?
	 */
	public static void info(final Component parent, final String info, final int width, final boolean force) {
		if (info==null || info.isBlank()) return;
		if (parent==null) return;

		/* Setup: Anzeige von Infos aktiv? */
		if (!SetupData.getSetup().surfaceGlassInfos) return;

		/* Glass-Panel für aktuelles Fenster ermitteln */
		final Window window;
		if (parent instanceof Window) window=(Window)parent; else window=SwingUtilities.getWindowAncestor(parent);

		Component glass=null;
		if ((window instanceof JFrame)) glass=((JFrame)window).getGlassPane();
		if ((window instanceof JDialog)) glass=((JDialog)window).getGlassPane();
		if (!(glass instanceof JPanel)) return;
		final JPanel glassPane=(JPanel)glass;

		/* Wird bereits ein Informationstext angezeigt? */
		if (glassPane.isVisible() && !force) return;

		/* Text nicht zu häufig anzeigen */
		if (!force) {
			if (lastText.contains(info)) return;
			lastText.add(info);
		}

		/* Bisherige Anzeige entfernen */
		glassPane.setVisible(false);
		glassPane.removeAll();
		final Timer oldTimer=timers.get(glassPane);
		if (oldTimer!=null) oldTimer.cancel();
		timers.remove(glassPane);

		/* Neue Ausgabe erstellen */
		glassPane.setLayout(new BoxLayout(glassPane,BoxLayout.PAGE_AXIS));
		glassPane.add(Box.createVerticalGlue());
		glassPane.add(Box.createVerticalGlue());
		glassPane.add(Box.createVerticalGlue());
		glassPane.add(Box.createVerticalGlue());
		final JPanel infoPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
		infoPanel.add(new JLabel("<html><body style='color: white; font-size: x-large; font-weight: bold;'>"+info+"</body></html>"));
		infoPanel.setBackground(new Color(0,0,0,40));
		infoPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		infoPanel.setMaximumSize(new Dimension(width,1000));
		infoPanel.setOpaque(true);
		glassPane.add(infoPanel,BorderLayout.CENTER);
		glassPane.add(Box.createVerticalGlue());

		/* Aktivieren */
		glassPane.setVisible(true);
		window.repaint();

		/* Abschalt-Timer */
		final Timer timer=new Timer("HideGlassInfoPanel",true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					SwingUtilities.invokeAndWait(()->{
						glassPane.setVisible(false);
						timer.cancel();
						timers.remove(glassPane);
					});
				} catch (InvocationTargetException|InterruptedException e) {}
			}
		},1000*DISPLAY_SECONDS);
		timers.put(glassPane,timer);
	}
}
