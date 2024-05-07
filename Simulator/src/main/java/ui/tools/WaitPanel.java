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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import net.calc.SimulationClient;
import simulator.AnySimulator;
import simulator.Simulator;
import simulator.StartAnySimulator;
import tools.Notifier;
import ui.images.Images;

/**
 * Diese Klasse zeigt ein "Bitte warten"-Panel während der Simulation an.
 * @author Alexander Herzog
 */
public class WaitPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3524929788005334671L;

	/** Übergeordnetes Fenster */
	private Window parentWindow;

	/**
	 * Wurde die Simulation erfolgreich beendet?
	 * @see #finalizeSimulation(boolean)
	 * @see #isSimulationSuccessful()
	 */
	private boolean simulationSuccessful;

	/**
	 * Soll die Simulation abgebrochen werden?
	 * @see #abortSimulation()
	 */
	private boolean abortRun;

	/** Zeile 1 Informationstext */
	private JLabel info1;
	/** Zeile 2 Informationstext */
	private JLabel info2;
	/** Statusleiste (linksbündig) mit weiteren Informationen zur laufenden Simulation */
	private JLabel statusbar;
	/** Statusleiste (rechtsbündig) mit weiteren Informationen zur laufenden Simulation */
	private JLabel statusbarRight;
	/** Fortschrittsbalken */
	private JProgressBar progress;
	/** Maximalwert für den Fortschrittsbalken */
	private int progressMax;
	/** Skalierungsfaktor der Kundenankünfte bei der Darstellung des Fortschrittsbalkens */
	private int clientScale;
	/** "Abbrechen"-Schaltfläche */
	private JButton cancel;

	/** Zählt die Aufrufe des TimerTasks */
	private long countTimerIntervals=0;

	/** Timer zur Aktualisierung der Fortschrittsanzeige */
	private Timer timer;
	/** Startzeit der Simulation (als {@link System#currentTimeMillis()}-Wert) */
	private long startTime;
	/** Geschätzte Restzeit in Sekunden */
	private int lastGesamt;

	/** Gestarteter Simulator, dessen Daten hier angezeigt werden sollen. */
	private AnySimulator simulator;

	/** Wird aufgerufen, wenn die Simulation beendet wurde (erfolgreich oder per Nutzerabbruch). Wird hier <code>null</code> übergeben, so erfolgt keine Rückmeldung. */
	private Runnable simulationDone;

	/** System zur Ermittung der CPU-Auslastung und der Arbeitsspeicherbelegung */
	private SystemInfoData sysInfo=new SystemInfoData();

	/**
	 * Konstruktor der Klasse {@link WaitPanel}
	 */
	public WaitPanel() {
		super(new BorderLayout());

		JPanel mainarea, p1x, p1a, p1b, p2;

		final JPanel statusbarOuter=new JPanel(new BorderLayout());
		add(statusbarOuter,BorderLayout.SOUTH);
		statusbarOuter.setBorder(BorderFactory.createEtchedBorder());
		statusbarOuter.add(statusbar=new JLabel(),BorderLayout.CENTER);
		statusbar.setPreferredSize(new Dimension(200,20));
		statusbar.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

		statusbarOuter.add(statusbarRight=new JLabel(),BorderLayout.EAST);
		statusbarRight.setBorder(BorderFactory.createEmptyBorder(0,0,0,5));
		statusbarRight.setAlignmentX(1);

		add(mainarea=new JPanel(),BorderLayout.CENTER);
		mainarea.setLayout(new BoxLayout(mainarea,BoxLayout.Y_AXIS));
		mainarea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainarea.add(Box.createVerticalGlue());
		mainarea.add(p1x=new JPanel()); p1x.setLayout(new BoxLayout(p1x,BoxLayout.X_AXIS));
		mainarea.add(p1a=new JPanel()); p1a.setLayout(new BoxLayout(p1a,BoxLayout.X_AXIS));
		mainarea.add(p1b=new JPanel()); p1b.setLayout(new BoxLayout(p1b,BoxLayout.X_AXIS));
		mainarea.add(Box.createVerticalStrut(10));
		mainarea.add(progress=new JProgressBar(0,100));
		mainarea.add(Box.createVerticalStrut(10));
		mainarea.add(p2=new JPanel()); p2.setLayout(new BoxLayout(p2,BoxLayout.X_AXIS));
		mainarea.add(Box.createVerticalGlue());
		mainarea.add(Box.createVerticalGlue());

		progress.setStringPainted(true);

		p1x.add(Box.createHorizontalGlue());
		p1x.add(new WaitPanelAnimation());
		p1x.add(Box.createHorizontalGlue());

		p1a.add(Box.createHorizontalGlue());
		p1a.add(info1=new JLabel(""));
		info1.setFont(info1.getFont().deriveFont(Font.BOLD));
		p1a.add(Box.createHorizontalGlue());

		p1b.add(Box.createHorizontalGlue());
		p1b.add(info2=new JLabel(""));
		p1b.add(Box.createHorizontalGlue());

		p2.add(Box.createHorizontalGlue());
		p2.add(cancel=new JButton(Language.tr("Dialog.Button.Cancel")));
		p2.add(Box.createHorizontalGlue());
		cancel.addActionListener(e->abortSimulation());
		cancel.setIcon(Images.GENERAL_CANCEL.getIcon());

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"ESCAPE");
		getActionMap().put("ESCAPE",new AbstractAction() {
			private static final long serialVersionUID = 190237083100271239L;
			@Override public void actionPerformed(ActionEvent e) {abortSimulation();}
		});
	}

	/**
	 * Versucht das übergeordnete Fenster zu ermitteln.
	 * @see #parentWindow
	 */
	private void findParentWindow() {
		Container c=getParent();
		while (c!=null) {
			if (c instanceof Window) {parentWindow=(Window)c; return;}
			c=c.getParent();
		}
	}

	/**
	 * Bricht die Simulation ab.
	 */
	public void abortSimulation() {
		cancel.setEnabled(false);
		abortRun=true;
	}

	/**
	 * Prüft, ob die Simulation erfolgreich beendet wurde.
	 * (Kann z.B. von dem Runnable, welches <code>setSimulator</code> übergeben wird aus aufgerufen werden.)
	 * @return	Gibt <code>true</code> zurück, wenn die Simulation erfolgreich beendet wurde. Andernfalls wurde sie entweder abgebrochen oder läuft noch.
	 */
	public boolean isSimulationSuccessful() {
		return simulationSuccessful;
	}

	/**
	 * Setzt das <code>Simulator</code>-Objekt, dessen Fortschritt in diesem Panel angezeigt werden soll.
	 * @param simulator	Gestarteter Simulator, dessen Daten hier angezeigt werden sollen.
	 * @param simulationDone	Wird aufgerufen, wenn die Simulation beendet wurde (erfolgreich oder per Nutzerabbruch). Wird hier <code>null</code> übergeben, so erfolgt keine Rückmeldung.
	 */
	public final void setSimulator(final AnySimulator simulator, final Runnable simulationDone) {
		abortRun=false;
		cancel.setEnabled(true);
		simulationSuccessful=false;
		this.simulator=simulator;
		this.simulationDone=simulationDone;

		statusbar.setText(Language.tr("Wait.Status.Start"));
		statusbarRight.setText("");
		if (simulator instanceof SimulationClient) {
			info1.setText(String.format(Language.tr("Wait.Info.StartRemote"),((SimulationClient)simulator).getHost()));
		} else {
			final Object[] server=StartAnySimulator.getServerSetup();
			if (server==null) {
				info1.setText(Language.tr("Wait.Info.Start"));
			} else {
				info1.setText(String.format(Language.tr("Wait.Info.StartNoRemoteAvailable"),(String)server[0]));
			}
		}
		info2.setText("");

		if (simulator.getCountClients()>=100_000) {
			clientScale=1000;
			progressMax=Math.max(1,(int)(simulator.getCountClients()/1000));
		} else {
			clientScale=1;
			progressMax=Math.max(1,(int)(simulator.getCountClients()));
		}

		progress.setMaximum(progressMax);
		progress.setValue(0);

		startTime=System.currentTimeMillis();

		lastGesamt=Integer.MAX_VALUE;

		if (!simulator.isRunning()) {
			/* Hintergrundsimulation ist schon fertig */
			finalizeSimulation(true);
		} else {
			countTimerIntervals=0;
			timer=new Timer("SimProgressBar",false);
			timer.schedule(new TimerTask() {
				@Override public void run() {try {runUpdate();} catch (Exception | OutOfMemoryError e) {abortRun=true;}}
			},50,50);
		}
	}

	/**
	 * Schließt die Simulation ab.
	 * @param successful	War die Simulation erfolgreich?
	 */
	private void finalizeSimulation(final boolean successful) {
		if (timer!=null) timer.cancel();
		simulationSuccessful=successful;
		simulator.finalizeRun();
		simulator=null;
		if (parentWindow==null) findParentWindow();
		Notifier.setSimulationProgress(parentWindow,-1);
		if (simulationDone!=null) SwingUtilities.invokeLater(simulationDone);
	}

	/**
	 * Aktualisiert die Info- und die Statuszeile im Falle eines über eine Zeitpunkt definierten Simulationsendes.
	 * @param progressPercent	Simulationsfortschritt
	 * @param arrivalCount	Anzahl an Kundenankünften bisher
	 * @param wip	Aktuelle Anzahl an Kunden im System
	 * @param delta	Zeit (in MS) seit dem Start der Simulation
	 */
	private void updateStatusBarTime(final int progressPercent, final long arrivalCount, final int wip, final long delta) {
		/* Informationen zur Restlaufzeit */
		if (delta>3000 && progressPercent>0) {
			double gesamt=delta*100.0/progressPercent;
			gesamt-=delta;
			if (gesamt/1000<lastGesamt) lastGesamt=(int)FastMath.round(gesamt/1000);
			if (lastGesamt<86400) {
				info2.setText(String.format(Language.tr("Wait.Info.LongRun"),NumberTools.formatLong(delta/1000),NumberTools.formatLong(Math.max(0,lastGesamt))));
			} else {
				info2.setText(String.format(Language.tr("Wait.Info.LongRunNoEstimation"),NumberTools.formatLong(delta/1000)));
			}
		} else {
			info2.setText(String.format(Language.tr("Wait.Info.LongRunNoEstimation"),NumberTools.formatLong(delta/1000)));
		}

		/* Statuszeile */
		final String currentClientsKString=NumberTools.formatLong(arrivalCount/1000);
		final long events=simulator.getEventCount();
		final String eventsPerSecondKString=NumberTools.formatLong(simulator.getEventsPerSecond()/1000);
		if (events<1_000_000) {
			final String totalEventsKString=NumberTools.formatLong(events/1000);
			if (wip==0) {
				statusbar.setText(String.format(Language.tr("Wait.Status.LongRunNoEstimationK.WIPZero"),currentClientsKString,totalEventsKString,eventsPerSecondKString));
			} else {
				if (wip==1) {
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRunNoEstimationK.WIPOne"),currentClientsKString,totalEventsKString,eventsPerSecondKString));
				} else {
					final String wipString=NumberTools.formatLong(wip);
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRunNoEstimationK"),currentClientsKString,wipString,totalEventsKString,eventsPerSecondKString));
				}
			}
		} else {
			final String totalEventsMString=NumberTools.formatLong(events/1_000_000);
			if (wip==0) {
				statusbar.setText(String.format(Language.tr("Wait.Status.LongRunNoEstimationM.WIPZero"),currentClientsKString,totalEventsMString,eventsPerSecondKString));
			} else {
				if (wip==1) {
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRunNoEstimationM.WIPOne"),currentClientsKString,totalEventsMString,eventsPerSecondKString));
				} else {
					final String wipString=NumberTools.formatLong(wip);
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRunNoEstimationM"),currentClientsKString,wipString,totalEventsMString,eventsPerSecondKString));
				}
			}
		}
	}

	/**
	 * Aktualisiert die Info- und die Statuszeile im Falle eines über eine Anzahl an Ankünften definierten Simulationsendes.
	 * @param arrivalCount	Anzahl an Kundenankünften bisher
	 * @param arrivalSum	Geplante Gesamtanzahl an Kundenankünften
	 * @param wip	Aktuelle Anzahl an Kunden im System
	 * @param delta	Zeit (in MS) seit dem Start der Simulation
	 */
	private void updateStatusBarCount(final long arrivalCount, final long arrivalSum, final int wip, final long delta) {
		/* Informationen zur Restlaufzeit */
		if (delta>3000) {
			double gesamt=delta/(((double)arrivalCount)/arrivalSum);
			gesamt-=delta;
			if (gesamt/1000<lastGesamt) lastGesamt=(int)FastMath.round(gesamt/1000);
			if (lastGesamt<86400) {
				info2.setText(String.format(Language.tr("Wait.Info.LongRun"),NumberTools.formatLong(delta/1000),NumberTools.formatLong(Math.max(0,lastGesamt))));
			} else {
				info2.setText(String.format(Language.tr("Wait.Info.LongRunNoEstimation"),NumberTools.formatLong(delta/1000)));
			}
		}

		/* Statuszeile */
		final long events=simulator.getEventCount();
		final String currentClientsKString=NumberTools.formatLong(arrivalCount/1000);
		final String totalClientsKString=NumberTools.formatLong(arrivalSum/1000);
		final String eventsPerSecondKString=NumberTools.formatLong(simulator.getEventsPerSecond()/1000);
		if (events<1_000_000) {
			final String totalEventsKString=NumberTools.formatLong(events/1000);
			if (wip==0) {
				statusbar.setText(String.format(Language.tr("Wait.Status.LongRunK.WIPZero"),currentClientsKString,totalClientsKString,totalEventsKString,eventsPerSecondKString));
			} else {
				if (wip==1) {
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRunK.WIPOne"),currentClientsKString,totalClientsKString,totalEventsKString,eventsPerSecondKString));
				} else {
					final String wipString=NumberTools.formatLong(wip);
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRunK"),currentClientsKString,totalClientsKString,wipString,totalEventsKString,eventsPerSecondKString));
				}
			}
		} else {
			final String totalEventsMString=NumberTools.formatLong(events/1_000_000);
			if (wip==0) {
				statusbar.setText(String.format(Language.tr("Wait.Status.LongRun.WIPZero"),currentClientsKString,totalClientsKString,totalEventsMString,eventsPerSecondKString));
			} else {
				if (wip==1) {
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRun.WIPOne"),currentClientsKString,totalClientsKString,totalEventsMString,eventsPerSecondKString));
				} else {
					final String wipString=NumberTools.formatLong(wip);
					statusbar.setText(String.format(Language.tr("Wait.Status.LongRun"),currentClientsKString,totalClientsKString,wipString,totalEventsMString,eventsPerSecondKString));
				}
			}
		}
	}

	/**
	 * Aktualisierung der Daten im Falle einer lang laufenden Simulation.
	 */
	private void runUpdate() {
		/* Auf Abbruch prüfen */

		if (abortRun) simulator.cancel();
		if (abortRun || !simulator.isRunning()) {finalizeSimulation(!abortRun); return;}
		countTimerIntervals++;

		/* Infozeile und Statuszeile */

		if (countTimerIntervals%20!=0) return;

		final long arrivalCount=simulator.getCurrentClients();
		final long arrivalSum=simulator.getCountClients();
		final int wip=simulator.getCurrentWIP();
		final long delta=System.currentTimeMillis()-startTime;

		int progressPercent;

		if (arrivalSum<0) {
			progressPercent=-1;
			if (simulator instanceof Simulator) {
				final Simulator localSimulator=(Simulator)simulator;
				if (localSimulator.threadCount==1) {
					final long terminationTime=localSimulator.getRunModel().terminationTime;
					if (terminationTime>0) {
						final double currentSeconds=localSimulator.getSingleThreadCurrentTime();
						if (currentSeconds>0) progressPercent=(int)(100*currentSeconds/terminationTime);
					}
				}
			}
			updateStatusBarTime(progressPercent,arrivalCount,wip,delta);
			progressMax=100;
		} else {
			updateStatusBarCount(arrivalCount,arrivalSum,wip,delta);
			progressPercent=(int)(arrivalCount/clientScale);
		}

		if (progressPercent>=0) {
			progress.setStringPainted(true);
			progress.setIndeterminate(false);
			progress.setMaximum(progressMax);
			progress.setValue(progressPercent);
			if (parentWindow==null) findParentWindow();
			Notifier.setSimulationProgress(parentWindow,100*progressPercent/progressMax);
		} else {
			progress.setStringPainted(false);
			progress.setIndeterminate(true);
		}

		/* Systemauslastung */

		if (countTimerIntervals%60!=0) return;
		final double load=sysInfo.getLoad();
		if (load>0.01) {
			statusbarRight.setText("CPU: "+NumberTools.formatPercent(load,0)+", RAM: "+NumberTools.formatLong(sysInfo.getRAMUsage()/1024/1024)+" MB");
		} else {
			statusbarRight.setText("RAM: "+NumberTools.formatLong(sysInfo.getRAMUsage()/1024/1024)+" MB");
		}
	}
}