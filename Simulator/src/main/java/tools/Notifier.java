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
package tools;

import java.awt.SystemTray;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.Window;
import java.util.Timer;
import java.util.TimerTask;

import language.Language;
import net.mqtt.MQTTBrokerURL;
import net.mqtt.MQTTSimClient;
import ui.MainFrame;

/**
 * Ermöglicht die Ausgabe einer Meldung über das System-Tray
 * @author Alexander Herzog
 */
public class Notifier {
	/**
	 * Über welches Ereignis soll benachrichtigt werden?
	 * @author Alexander Herzog
	 */
	public enum Message {
		/** Abschluss einer Simulation */
		SIMULATION_DONE,

		/** Abschluss einer Parameterreihen-Simulation */
		PARAMETER_SERIES_DONE,

		/** Abschluss einer Optimierung */
		OPTIMIZATION_DONE
	}

	/** Mindestzeitdauer (in Millisekunden) damit eine Operation als "lange dauernd" angesehen wird */
	private static final long MIN_NOTIFY_OPERATION_TIME=10_000;

	/** Programmeinstellungen-Singleton */
	private static final SetupData setup=SetupData.getSetup();

	/** Aktueller Tray-Area-Eintrag */
	private static TrayIcon lastIcon;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Methoden zur Verfügung und kann nicht instanziert werden.
	 */
	private Notifier() {

	}

	/**
	 * Entfernt die letzte Benachrichtigung.
	 * @return	Liefert <code>true</code>, wenn eine Benachrichtigung vorhanden war und entfernt werden konnte
	 */
	private static boolean removeLastNotify() {
		if (lastIcon==null) return false;
		try {
			final SystemTray tray=SystemTray.getSystemTray();
			if (tray==null) return false;
			tray.remove(lastIcon);
			lastIcon=null;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/*
	private static boolean playSound() {
		final Runnable runnable=(Runnable)Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.default");
		if (runnable==null) return false;
		runnable.run();
		return true;
	}
	 */

	/**
	 * Zeigt eine Meldung mit einem bestimmten Text im Tray-Area an.
	 * @param text	Anzuzeigende Meldung
	 * @return	Liefert <code>true</code>, wenn die Meldung im Tray-Area angezeigt werden konnte
	 */
	private static boolean showMessage(final String text) {
		try {
			final SystemTray tray=SystemTray.getSystemTray();
			if (tray==null) return false;

			final TrayIcon icon=new TrayIcon(Toolkit.getDefaultToolkit().getImage(MainFrame.ICON_URL),MainFrame.PROGRAM_NAME);
			icon.setImageAutoSize(true);
			tray.add(icon);

			icon.displayMessage(MainFrame.PROGRAM_NAME,text,MessageType.INFO);
			final Timer timer=new Timer("SystemNotifier");
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					tray.remove(icon);
					timer.cancel();
				}
			},15000);
			icon.addActionListener(e->tray.remove(icon));
			lastIcon=icon;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Sendet eine Meldung an den MQTT-Broker.
	 * @param text	Zu sendende Meldung
	 * @return	Liefert <code>true</code>, wenn die Meldung gesendet werden konnte
	 */
	private static boolean sendMQTTMessage(final String text) {
		/* Daten für Verbindung zu MQTT-Server vorbereiten */
		final MQTTSimClient serverMQTT=MQTTSimClient.getInstance();
		final MQTTBrokerURL broker=MQTTBrokerURL.parseString(setup.mqttBroker,setup.mqttVerifyCertificates);
		if (broker==null) return false;

		/* Läuft der Client bereits? Dann Nachricht direkt senden */
		if (serverMQTT.isRunning()) {
			return serverMQTT.sendText(setup.notifyMQTTTopic,text,0);
		}

		/* Client starten, Nachricht senden, Client beenden */
		if (serverMQTT.start(broker,null,null,null,setup.serverAuthName,setup.serverAuthPassword)!=null) return false;
		try {
			return serverMQTT.sendText(setup.notifyMQTTTopic,text,0);
		} finally {
			serverMQTT.stop();
		}
	}

	/**
	 * Liefert auf Basis des Nachrichtentyps die konkrete anzuzeigende Meldung
	 * @param message	Nachrichtentyp
	 * @return	Text der anzuzeigenden Meldung
	 * @see #run(Message, long)
	 */
	private static String getMessageString(final Message message) {
		switch (message) {
		case SIMULATION_DONE: return Language.tr("Notifier.Message.SimulationDone");
		case PARAMETER_SERIES_DONE: return Language.tr("Notifier.Message.ParameterSeriesDone");
		case OPTIMIZATION_DONE: return Language.tr("Notifier.Message.OptimizationDone");
		default: return "";
		}
	}

	/**
	 * Zeigt (wenn das Setup dies zulässt) eine Meldung an.
	 * @param message	Anzuzeigende Meldung
	 */
	public static void run(final Message message) {
		run(message,0);
	}

	/**
	 * Zeigt (wenn das Setup dies zulässt) eine Meldung an.
	 * @param message	Anzuzeigende Meldung
	 * @param operationStartTimeMS	Zeitpunkt des Beginns der jetzt abgeschlossenen Operation (falls Meldungen nur angezeigt werden sollen, wenn die jeweilige Operation länger gedauert hat)
	 */
	public static void run(final Message message, final long operationStartTimeMS) {
		final long operationTimeMS=System.currentTimeMillis()-operationStartTimeMS;
		final String messageString=getMessageString(message);

		removeLastNotify();
		if ((operationTimeMS>=MIN_NOTIFY_OPERATION_TIME && setup.notifyMode!=SetupData.NotifyMode.OFF) || setup.notifyMode==SetupData.NotifyMode.ALWAYS) {
			/* if (setup.useAcusticNotify) playSound(); - TrayIcon.displayMessage gibt bereits einen Ton aus */
			showMessage(messageString);
		}

		if (setup.notifyMQTT) {
			sendMQTTMessage(messageString);
		}
	}

	/**
	 * Stellt einen Wert zwischen 0% und 100% im Taskleisten-Icon ein.
	 * Werte außerhalb des Bereichs von 0 bis 100 führen zu einer Deaktivierung der Anzeige.
	 * @param window	Fenster dessen Taskleisten-Icon mit einem Wert versehen werden soll
	 * @param value	Anzueigender Wert
	 */
	public static void setSimulationProgress(final Window window, final int value) {
		if (window==null) return;

		try {
			Taskbar.getTaskbar().setWindowProgressValue(window,value);
		} catch (UnsupportedOperationException | SecurityException e) {
		}
	}
}
