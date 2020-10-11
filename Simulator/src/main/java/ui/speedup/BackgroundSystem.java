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
package ui.speedup;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import simcore.logging.SimLogging;
import simulator.AnySimulator;
import simulator.Simulator;
import simulator.StartAnySimulator;
import simulator.builder.RunModelCreator;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import tools.SetupData;
import ui.EditorPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ElementNoRemoteSimulation;
import ui.modeleditor.elements.ModelElementDecideJS;
import ui.modeleditor.elements.ModelElementHoldJS;
import ui.modeleditor.elements.ModelElementSetJS;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Diese Singleton-Klasse kapselt die Funktionalität zur Hintergrund-Verarbeitung
 * von Simulationsmodellen.
 * @author Alexander Herzog
 */
public class BackgroundSystem {
	/** Maximalwert für Kunden/Thread im nicht-agressiv Modus für die Hintergrundsimulation */
	private static final int MAX_CLIENTS_PER_THREAD=2_000_000;
	/** Maximalwert Modellelemente/Thread im nicht-agressiv Modus für die Hintergrundsimulation */
	private static final int MAX_ELEMENTS_PER_THREAD=10;
	/** Verzögerung vor dem Start der Hintergrundsimulation im Normalfall */
	private static final int DELAY_NORMAL=2_500;
	/** Verzögerung vor dem Start der Hintergrundsimulation im agressiven Modus */
	private static final int DELAY_FAST=1_500;

	private static Map<EditorPanel,BackgroundSystem> system;

	/** Referenz auf das Setup-Singleton */
	private static final SetupData setup=SetupData.getSetup();
	private SetupData.BackgroundProcessingMode lastBackgroundMode;
	private Timer timer;

	private EditModel lastModel;
	private StartAnySimulator lastStarter;
	private AnySimulator lastSimulator;
	private TimerTask lastTask;

	private long lastUsage;

	/**
	 * Konstruktor der Klasse.<br>
	 * Kann nicht direkt aufgerufen werden, stattdessen ist {@link BackgroundSystem#getBackgroundSystem(EditorPanel)} zu verwenden.
	 * @see BackgroundSystem#getBackgroundSystem(EditorPanel)
	 */
	private BackgroundSystem() {
		lastBackgroundMode=SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING;
		timer=null;
		lastModel=null;
		lastSimulator=null;
		lastStarter=null;
		lastUsage=System.currentTimeMillis();
	}

	private void initTimer() {
		if (timer==null) timer=new Timer("SimBackground",true);
	}

	private void doneTimer() {
		if (timer!=null) {
			timer.cancel();
			timer=null;
		}
	}

	/**
	 * Liefert die Instanz des Hintergrund-Simulations-Systems
	 * @param owner	Editor-Panel für das eine Instanz des Hintergrund-Simulations-Systems geliefert werden soll
	 * @return	Instanz des Hintergrund-Simulations-Systems
	 */
	public static synchronized BackgroundSystem getBackgroundSystem(final EditorPanel owner) {
		/* Hintergrund-System anlegen oder liefern */
		if (system==null) system=new HashMap<>();
		BackgroundSystem backgroundSystem=system.get(owner);
		if (backgroundSystem==null) system.put(owner,backgroundSystem=new BackgroundSystem());
		backgroundSystem.lastUsage=System.currentTimeMillis();

		/* Evtl. alte Systeme beenden */
		final long limit=System.currentTimeMillis()-60*1000;
		boolean done=false;
		while (!done) {
			done=true;
			for (Map.Entry<EditorPanel,BackgroundSystem> entry: system.entrySet()) {
				if (entry.getValue()==backgroundSystem) continue;
				if (entry.getValue().lastUsage<limit) {
					system.remove(entry.getKey());
					done=false;
					break;
				}
			}
		}

		return backgroundSystem;
	}

	/**
	 * Nimmt eine Grobprüfung eines einzelnen Elements vor
	 * @param element	Zu prüfendes Element
	 * @return	Gibt im Erfolgsfall (oder wenn keine Prüfung stattgefunden hat) <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public static String checkModelElement(final ModelElementPosition element) {
		if (setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING) return null;
		return RunModelCreator.testElement(element);
	}

	/**
	 * Prüft, ob die Modellprüfung auf die aktuelle Zeichenfläche angewandt werden kann
	 * @param surface	Zeichenfläche, von der ermittelt werden soll, ob sie geprüft werden kann und soll
	 * @return	Gibt <code>true</code> zurück, wenn eine Prüfung möglich und gewollt ist
	 */
	public boolean canCheck(final ModelSurface surface) {
		lastUsage=System.currentTimeMillis();
		if (setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING) return false;
		if (surface.getParentSurface()!=null) return false;
		if (surface.getElementCount()==0) return false;
		return true;
	}

	private boolean containsJSElements(final EditModel model) {
		for (ModelElement element1: model.surface.getElements()) {
			if (element1 instanceof ModelElementDecideJS) return true;
			if (element1 instanceof ModelElementHoldJS) return true;
			if (element1 instanceof ModelElementSetJS) return true;

			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementDecideJS) return true;
				if (element2 instanceof ModelElementHoldJS) return true;
				if (element2 instanceof ModelElementSetJS) return true;
			}
		}

		return false;
	}

	private boolean canBackgroundProcess(final EditModel model) {
		/* Keine Hintergrundverarbeitung, wenn Ausgabeelemente im Spiel sind. */
		if (!model.canRunInBackground()) return false;

		/* Generell keine Hintergrundsimulation gewünscht */
		if ((setup.backgroundSimulation!=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION && setup.backgroundSimulation!=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS) || !setup.useMultiCoreSimulation) return false;

		/* Hintergrundsimulation immer, wenn möglich */
		if (setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS) return true;

		/* Prüfen, ob dieses Modell im Hintergrund simuliert werden soll. */

		final int threadCount=Runtime.getRuntime().availableProcessors();

		if (containsJSElements(model)) return false; /* JS-Interpreter nicht im Hintergrund verwenden */
		if (model.resources.count()>threadCount) return false; /* Keine Modelle mit vielen Ressourcen (im Verhältnis zur CPU-Kern-Zahl) */
		if (model.transporters.count()>0) return false; /* Modelle mit Transportern nicht im Hintergrund verwenden */
		for (ModelElement element1: model.surface.getElements()) { /* Ausgaben und Co. dürfen nicht im Hintergrund laufen. */
			if (element1 instanceof ElementNoRemoteSimulation) return false;
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ElementNoRemoteSimulation) return false;
			}
		}

		boolean singleCore=(!model.getSingleCoreReason().isEmpty());
		if (singleCore && model.repeatCount==1) return true; /* Wir belasten nur einen Kern, damit harmlos. */

		long simClientCount=model.clientCount*model.repeatCount;
		if (simClientCount/threadCount>MAX_CLIENTS_PER_THREAD) return false;
		if (model.surface.getElementCount()/threadCount>MAX_ELEMENTS_PER_THREAD) return false;

		return true;
	}

	/**
	 * Übermittelt das aktuelle Modell an das Hintergrund-System.
	 * Dieses prüft das Modell evtl. und startet evtl. auch eine
	 * Hintergrund-Simulation.
	 * @param model	Aktuelles Modell
	 * @param startProcessing Modell nur prüfen (<code>false</code>) oder im Erfolgsfall auch Hintergrundsimulation starten (<code>true</code>)
	 * @return	Gibt <code>null</code> zurück, wenn das Modell fehlerfrei ist oder keine Prüfung stattgefunden hat, sonst eine Fehlermeldung.
	 * @see BackgroundSystem#getStartedSimulator(EditModel, SimLogging, int[], Set)
	 * @see BackgroundSystem#getLastBackgroundMode()
	 */
	public synchronized String process(final EditModel model, final boolean startProcessing) {
		lastUsage=System.currentTimeMillis();
		if (lastModel!=null && lastModel.equalsEditModel(model)) return null;

		stop(false);

		lastBackgroundMode=setup.backgroundSimulation;

		if (!canCheck(model.surface)) return null;

		/* Prüfen */

		final Object obj=RunModel.getRunModel(model,true);
		if (obj instanceof String) return (String)obj;

		/* Simulieren */

		if (!canBackgroundProcess(model)) return null;

		if (!startProcessing) return null;
		lastModel=model.clone();
		final String error=StartAnySimulator.testModel(lastModel);
		/* lastStarter=new StartAnySimulator(lastModel);
		final String error=lastStarter.prepare(); */
		if (error!=null) {
			lastModel=null;
			lastSimulator=null;
			lastStarter=null;
			stop(true);
			return error;
		}

		lastStarter=new StartAnySimulator(lastModel);

		long delay=DELAY_NORMAL;
		if (setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS) delay=DELAY_FAST;

		initTimer();
		timer.schedule(lastTask=new TimerTask() {
			@Override public void run() {
				startWaitingSimulator();
			}
		},delay);


		return null;
	}

	/**
	 * Liefert die Setup-Einstellung zur Hintergrundsimulation, die gültig war, als das letzte Mal ein Modell an das System übergeben wurde.
	 * @return	Setup-Einstellung zur Hintergrundsimulation während der letzten Modellübergabe per {@link BackgroundSystem#process(EditModel, boolean)}
	 * @see BackgroundSystem#process(EditModel, boolean)
	 */
	public SetupData.BackgroundProcessingMode getLastBackgroundMode() {
		lastUsage=System.currentTimeMillis();
		return lastBackgroundMode;
	}

	private synchronized void startWaitingSimulator() {
		doneTimer();
		if (lastStarter!=null && lastSimulator==null) {
			if (lastStarter.prepare()!=null) {
				lastModel=null;
				lastSimulator=null;
				lastStarter=null;
				stop(true);
				return;
			}
			if (lastStarter!=null) lastSimulator=lastStarter.start();
		}
	}

	/**
	 * Liefert einen (bereits gestarteten) Simulator für ein Editor-Modell
	 * <b>ohne dabei die Hintergrund-Funktionalität in irgendeiner Weise zu nutzen oder zu beeinflussen</b>
	 * @param editModel	Editor-Modell das simuliert werden soll
	 * @param logging	Optionales Logging-System (kann <code>null</code> sein)
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 * @return	Liefert im Erfolgsfall ein {@link AnySimulator}-Objekt; im Fehlerfall eine Fehlermeldung als Zeichenkette.
	 */
	public Object getNewStartedSimulator(final EditModel editModel, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		lastUsage=System.currentTimeMillis();
		final StartAnySimulator starter=new StartAnySimulator(editModel,logging,loggingIDs,logType);
		final String error=starter.prepare();
		if (error!=null) return error;
		return starter.start();
	}

	/**
	 * Liefert einen (bereits gestarteten) Simulator für ein Editor-Modell
	 * @param editModel	Editor-Modell das simuliert werden soll
	 * @param logging	Optionales Logging-System (kann <code>null</code> sein)
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @return	Liefert im Erfolgsfall ein {@link AnySimulator}-Objekt; im Fehlerfall eine Fehlermeldung als Zeichenkette.
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public Object getStartedSimulator(final EditModel editModel, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		lastUsage=System.currentTimeMillis();
		if (logging!=null || lastModel==null || lastSimulator==null) {
			stop();
			return getNewStartedSimulator(editModel,logging,loggingIDs,logType);
		}

		if (lastModel.equalsEditModel(editModel)) {
			final AnySimulator simulator=lastSimulator;
			if (lastTask!=null) {
				lastTask.cancel();
				lastTask=null;
				startWaitingSimulator();
			}
			lastModel=null;
			lastSimulator=null;
			lastStarter=null;
			return simulator;
		}

		return getNewStartedSimulator(editModel,null,null,Simulator.logTypeFull);
	}

	/**
	 * Stoppt alle möglichen Hintergrund-Simulationen.
	 */
	public void stop() {
		lastUsage=System.currentTimeMillis();
		stop(true);
	}

	private void stop(final boolean killTimer) {
		if (lastTask!=null) lastTask.cancel();
		if (lastSimulator!=null) lastSimulator.cancel();
		lastTask=null;
		lastModel=null;
		lastSimulator=null;
		lastStarter=null;
		if (killTimer) doneTimer();
	}
}
