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

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Diese Klasse ermöglicht es, ein vertikales Panel am Rande des Fensters
 * mit einer Slide-Animation ein- oder auszublenden.
 * @author Alexander Herzog
 */
public class PanelSlider {
	/** Anzahl der Schritte beim Ein- und Ausblenden */
	private static final int STEPS=5;
	/** Zeitlicher Abstand (in ms) der Schritte */
	private static final int STEP_DELAY=5;

	/** Ein- oder auszublendendes Panel */
	private final JPanel panel;
	/** Breite des Panels im eingeblendeten Zustand */
	private final int width;
	/** Optionales Callback, das nach Abschluss der Operation aufgerufen wird (kann <code>null</code> sein) */
	private final Runnable whenDone;
	/** Timer-Objekt, welches die einzelnen Slide-Schritte auslöst */
	private Timer timer;
	/** Nummer des aktuellen Schritts */
	private int step;
	/** Ein- bzw. Ausblende-Richtung (1=Einblenden, -1=Ausblenden) */
	private int delta;

	/**
	 * Konstruktor der Klasse
	 * @param panel	Ein- oder auszublendendes Panel
	 * @param setVisible	Soll das Panel ein- oder ausgeblendet werden?
	 * @param fast	Soll das Ein- oder Ausblenden schnell (<code>true</code>) oder mit Animation (<code>false</code>) erfolgen?
	 */
	public PanelSlider(final JPanel panel, final boolean setVisible, final boolean fast) {
		this(panel,setVisible,fast,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param panel	Ein- oder auszublendendes Panel
	 * @param setVisible	Soll das Panel ein- oder ausgeblendet werden?
	 * @param fast	Soll das Ein- oder Ausblenden schnell (<code>true</code>) oder mit Animation (<code>false</code>) erfolgen?
	 * @param whenDone	Optionales Callback, das nach Abschluss der Operation aufgerufen wird (kann <code>null</code> sein)
	 */
	public PanelSlider(final JPanel panel, final boolean setVisible, final boolean fast, final Runnable whenDone) {
		this.panel=panel;
		width=panel.getWidth();
		this.whenDone=whenDone;

		/* System.out.println(panel.isVisible()+"->"+setVisible+" fast="+fast+" width="+width); */

		/* Passt die Einstellung schon? Wenn ja, nichts machen. */
		if (panel.isVisible()==setVisible) {
			if (whenDone!=null) whenDone.run();
			return;
		}

		/* Keine Animation, nur umschalten? */
		if (fast || width==0) {
			panel.setVisible(setVisible);
			stepFix();
			if (whenDone!=null) whenDone.run();
			return;
		}

		/* Animation starten */
		if (setVisible) {
			setWidth(0);
			panel.setVisible(true);
			startTimer(1);
		} else {
			startTimer(-1);
		}
	}

	/**
	 * Korrektur der Darstellung des Panels (bzw. des übergeordneten Elements)
	 * nach einem einzelnen Animationsschritt.
	 */
	private void stepFix() {
		final Container parent=panel.getParent();
		parent.setVisible(false);
		parent.setVisible(true);
	}

	/**
	 * Stellt die Breite des Panels ein.
	 * @param width	Neue Breite für das Panel
	 */
	private void setWidth(final int width) {
		final Dimension size=new Dimension(width,panel.getHeight());
		panel.setSize(size);
		panel.setPreferredSize(size);
		panel.setMinimumSize(size);
		panel.setMaximumSize(size);
		panel.repaint();
		stepFix();
	}

	/**
	 * Startet die zeitgesteuerte Verarbeitung
	 * @param delta	Einblenden (1) oder ausblenden (-1)?
	 */
	private void startTimer(final int delta) {
		this.delta=delta;
		step=0;
		timer=new Timer(STEP_DELAY,e->processStep());
		timer.start();
	}

	/**
	 * Führt im Rahmen von {@link #timer} einen
	 * einzelnen Verarbeitungsschritt aus.
	 * @see #timer
	 */
	private void processStep() {
		step++;

		int w=width*step/STEPS;
		if (delta<0) w=width-w;
		setWidth(w);

		if (step==STEPS) {
			timer.stop();
			if (delta<0) {
				panel.setVisible(false);
				setWidth(width);
			}
			if (whenDone!=null) whenDone.run();
		}
	}
}
