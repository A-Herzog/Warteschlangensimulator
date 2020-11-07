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
package net.dde;

import java.util.Timer;
import java.util.TimerTask;

import ui.AnimationPanel;
import ui.EditorPanel;
import ui.MainPanel;
import ui.statistics.StatisticsPanel;

/**
 * DDE-Server, der auf konkrete Anfragen an den Simulator reagiert.
 * @author Alexander Herzog
 */
public class SimulationDDEServer extends DDEServerSystem {
	/**
	 * Name des Services über den der DDE-Server angesprochen werden können soll
	 */
	private static final String SERVICE_NAME="QS";

	/**
	 * Singleton-Instanz dieser Klasse
	 * @see #getInstance(MainPanel)
	 */
	private static SimulationDDEServer instance;

	/**
	 * Hauptpanel für die diese Instanz gelten soll
	 */
	private final MainPanel mainPanel;

	/**
	 * Timer, der in regelmäßigen Abständen {@link #updateTest()} aufruft.
	 * @see #updateTest()
	 * @see #startTimer()
	 * @see #stopTimer()
	 */
	private Timer timer;

	/**
	 * Diese Singleton-Klasse kann nicht direkt instanziert werden.
	 * Es muss stattdessen die Methode {@link SimulationDDEServer#getInstance(MainPanel)}
	 * verwendet werden.
	 * @param mainPanel	Hauptpanel des Simulators
	 */
	private SimulationDDEServer(final MainPanel mainPanel) {
		super(SERVICE_NAME);
		this.mainPanel=mainPanel;

		topics.add(new DDETopicStatus(this,mainPanel));
		topics.add(new DDETopicCalc(this,mainPanel));
	}

	/**
	 * Liefert die Singleton-Instanz dieser Klasse
	 * @param mainPanel	Hauptpanel des Simulators
	 * @return	Instanz dieser Klasse
	 */
	public static synchronized SimulationDDEServer getInstance(final MainPanel mainPanel) {
		if (instance==null) instance=new SimulationDDEServer(mainPanel);
		return instance;
	}

	/**
	 * Startet den Timer zur Update-Prüfung.
	 * @see #start()
	 * @see #timer
	 */
	private void startTimer() {
		timer=new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override public void run() {updateTest();}
		},500,500);
	}

	/**
	 * Stoppt den Timer zur Update-Prüfung.
	 * @see #stop()
	 * @see #timer
	 */
	private void stopTimer() {
		if (timer!=null) timer.cancel();
		timer=null;
	}

	@Override
	public boolean start() {
		final boolean ok=super.start();
		if (ok) startTimer();
		return ok;
	}

	@Override
	public boolean stop() {
		final boolean ok=super.stop();
		stopTimer(); /* Timer auch anhalten, wenn es beim Stoppen des Servers ein Problem gab. */
		return ok;
	}

	/**
	 * Führt in Reaktion auf den DDE-"Step"-Befehl einen Animationsschritt aus.
	 * @see #processCommand(String)
	 */
	private void doAnimationStep() {
		if (mainPanel.currentPanel instanceof AnimationPanel) {
			((AnimationPanel)mainPanel.currentPanel).step(true);
			try {Thread.sleep(50);} catch (InterruptedException e) {}
			return;
		}

		if (!(mainPanel.currentPanel instanceof EditorPanel) && !(mainPanel.currentPanel instanceof StatisticsPanel)) return;

		final String error=mainPanel.startRemoteControlledAnimation();
		if (error!=null) return;

		try {Thread.sleep(50);} catch (InterruptedException e) {}
	}

	/**
	 * Bricht in Reaktion auf den DDE-"Stop"-Befehl die Animation ab.
	 * @see #processCommand(String)
	 */
	private void terminateAnimation() {
		if (mainPanel.currentPanel instanceof AnimationPanel) {
			((AnimationPanel)mainPanel.currentPanel).closeRequest();
		}
	}

	@Override
	protected boolean processCommand(final String command) {
		if (command.equalsIgnoreCase("step")) {doAnimationStep(); return true;}
		if (command.equalsIgnoreCase("stop")) {terminateAnimation(); return true;}
		return false;
	}

	/**
	 * Trägt ein neues Hauptpanel in die laufenden Server ein.
	 * @param mainPanel	Hauptpanel des Simulators (für den Zugriff durch den Animations-Web-Server)
	 */
	public static synchronized void updatePanel(final MainPanel mainPanel) {
		if (instance==null) return;
		final boolean running=instance.isRunning();
		if (running) instance.stop();
		instance=new SimulationDDEServer(mainPanel);
		if (running) instance.start();
	}
}