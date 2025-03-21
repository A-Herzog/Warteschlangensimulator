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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import scripting.java.DynamicFactory;
import scripting.java.ImportSettingsBuilder;
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
import ui.modeleditor.elements.AnimationExpression;
import ui.modeleditor.elements.ElementNoRemoteSimulation;
import ui.modeleditor.elements.ElementWithAnimationScripts;
import ui.modeleditor.elements.ElementWithScript;
import ui.modeleditor.elements.ElementWithScript.ScriptMode;
import ui.modeleditor.elements.ModelElementMatch;
import ui.modeleditor.elements.ModelElementSplit;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Diese Singleton-Klasse kapselt die Funktionalit�t zur Hintergrund-Verarbeitung
 * von Simulationsmodellen.
 * @author Alexander Herzog
 */
public class BackgroundSystem {
	/** Maximalwert f�r Kunden/Thread im nicht-aggressiv Modus f�r die Hintergrundsimulation */
	private static final int MAX_CLIENTS_PER_THREAD=2_000_000;
	/** Maximalwert Modellelemente/Thread im nicht-aggressiv Modus f�r die Hintergrundsimulation */
	private static final int MAX_ELEMENTS_PER_THREAD=10;
	/** Verz�gerung vor dem Start der Hintergrundsimulation im Normalfall */
	private static final int DELAY_NORMAL=2_500;
	/** Verz�gerung vor dem Start der Hintergrundsimulation im aggressiven Modus */
	private static final int DELAY_FAST=1_500;

	/** Hintergrund-System in Abh�ngigkeit von den Editoren */
	private static Map<EditorPanel,BackgroundSystem> system;

	/** Referenz auf das Setup-Singleton */
	private static final SetupData setup=SetupData.getSetup();
	/** Wie soll die Hintergrundverarbeitung ablaufen? */
	private SetupData.BackgroundProcessingMode lastBackgroundMode;
	/** Timer f�r regelm��ige Modellpr�fungen */
	private Timer timer;

	/** Letztes �bergebenes Modell (bezogen auf {@link #lastModel}) */
	private EditModel lastModel;
	/** Letzter Simulations-Starter (bezogen auf {@link #lastModel}) */
	private StartAnySimulator lastStarter;
	/** Letzter Simulator (bezogen auf {@link #lastModel}) */
	private AnySimulator lastSimulator;
	/** Letzter Hintergrund-Thread zur �bersetzung von Java-Code in Skript-Elementen */
	private Thread lastCompileThread;
	/** Zuletzt verwendeter Task zum Starten einer simulation  (bezogen auf {@link #lastModel}) */
	private TimerTask lastTask;

	/**
	 * Letzter Zeitpunkt, an der auf das aktuelle Objekt zugegriffen
	 * wurde, um so ggf. Systeme f�r die die zugeh�rigen Fenster
	 * l�ngst geschlossen wurden, beenden zu k�nnen.
	 */
	private long lastUsage;

	/**
	 * �bergeordnetes Editor-Panel
	 */
	private final EditorPanel owner;

	/**
	 * Konstruktor der Klasse.<br>
	 * Kann nicht direkt aufgerufen werden, stattdessen ist {@link BackgroundSystem#getBackgroundSystem(EditorPanel)} zu verwenden.
	 * @param owner	�bergeordnetes Editor-Panel
	 * @see BackgroundSystem#getBackgroundSystem(EditorPanel)
	 */
	private BackgroundSystem(final EditorPanel owner) {
		lastBackgroundMode=SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING;
		timer=null;
		lastModel=null;
		lastSimulator=null;
		lastCompileThread=null;
		lastStarter=null;
		lastUsage=System.currentTimeMillis();
		this.owner=owner;
	}

	/**
	 * Timer f�r Pr�fungen aktivieren.
	 */
	private void initTimer() {
		if (timer==null) timer=new Timer("SimBackground",true);
	}

	/**
	 * Timer f�r Pr�fungen beenden.
	 */
	private void doneTimer() {
		if (timer!=null) {
			timer.cancel();
			timer=null;
		}
	}

	/**
	 * Liefert die Instanz des Hintergrund-Simulations-Systems
	 * @param owner	Editor-Panel f�r das eine Instanz des Hintergrund-Simulations-Systems geliefert werden soll
	 * @return	Instanz des Hintergrund-Simulations-Systems
	 */
	public static synchronized BackgroundSystem getBackgroundSystem(final EditorPanel owner) {
		/* Hintergrund-System anlegen oder liefern */
		if (system==null) system=new HashMap<>();
		BackgroundSystem backgroundSystem=system.get(owner);
		if (backgroundSystem==null) system.put(owner,backgroundSystem=new BackgroundSystem(owner));
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
	 * Nimmt eine Grobpr�fung eines einzelnen Elements vor
	 * @param element	Zu pr�fendes Element
	 * @return	Gibt im Erfolgsfall (oder wenn keine Pr�fung stattgefunden hat) <code>null</code> zur�ck, sonst eine Fehlermeldung.
	 */
	public static String checkModelElement(final ModelElementPosition element) {
		if (setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING) return null;
		return RunModelCreator.testElement(element);
	}

	/**
	 * Pr�ft, ob die Modellpr�fung auf die aktuelle Zeichenfl�che angewandt werden kann
	 * @param surface	Zeichenfl�che, von der ermittelt werden soll, ob sie gepr�ft werden kann und soll
	 * @return	Gibt <code>true</code> zur�ck, wenn eine Pr�fung m�glich und gewollt ist
	 */
	public boolean canCheck(final ModelSurface surface) {
		lastUsage=System.currentTimeMillis();
		if (setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING) return false;
		if (surface.getParentSurface()!=null) return false;
		if (surface.getElementCount()==0) return false;
		return true;
	}

	/**
	 * Pr�ft, ob das Modell im Hintergrund simuliert werden kann.
	 * @param model	Zu pr�fendes Modell
	 * @return	Liefert <code>true</code>, wenn das Modell im Hintergrund simuliert werden kann
	 * @see #process(EditModel, boolean)
	 */
	private boolean canBackgroundProcess(final EditModel model) {
		/* Keine Hintergrundverarbeitung, wenn Ausgabeelemente im Spiel sind. */
		if (!model.canRunInBackground()) return false;

		/* Generell keine Hintergrundsimulation gew�nscht */
		if ((setup.backgroundSimulation!=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION && setup.backgroundSimulation!=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS) || !setup.useMultiCoreSimulation) return false;

		/* Hintergrundsimulation immer, wenn m�glich */
		if (setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS) return true;

		/* Pr�fen, ob dieses Modell im Hintergrund simuliert werden soll. */

		final int threadCount=Runtime.getRuntime().availableProcessors();

		if (model.resources.count()>threadCount) return false; /* Keine Modelle mit vielen Ressourcen (im Verh�ltnis zur CPU-Kern-Zahl) */
		if (model.transporters.count()>0) return false; /* Modelle mit Transportern nicht im Hintergrund verwenden */
		for (ModelElement element1: model.surface.getElements()) {
			if (element1 instanceof ElementNoRemoteSimulation) {
				final var el=(ElementNoRemoteSimulation)element1;
				if (el.inputConnected() && el.isOutputActive()) return false;
			}
			if (element1 instanceof ElementWithScript) return false;
			if (element1 instanceof ModelElementMatch) return false; /* Zusammenf�hren f�hrt fast immer zu zu hohen Kundenanzahlen. Hintergrundsimulation lieber bleiben lassen. */
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ElementNoRemoteSimulation) {
					final var el=(ElementNoRemoteSimulation)element1;
					if (el.inputConnected() && el.isOutputActive()) return false;
				}
				if (element2 instanceof ElementWithScript) return false;
				if (element2 instanceof ModelElementMatch) return false; /* Zusammenf�hren f�hrt fast immer zu zu hohen Kundenanzahlen. Hintergrundsimulation lieber bleiben lassen. */
			}
		}

		boolean singleCore=(!model.getSingleCoreReason().isEmpty());
		if (singleCore && model.repeatCount==1) return true; /* Wir belasten nur einen Kern, damit harmlos. */

		if (model.repeatCount>threadCount) return false; /* Das scheint eine langwierige Simulation zu werden. */

		int split=1;
		for (ModelElement element1: model.surface.getElements()) {
			if (element1 instanceof ModelElementSplit) {
				split+=((ModelElementSplit)element1).getAverageArrivalSizesSum();
			}
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) if (element2 instanceof ModelElementSplit) {
				split+=((ModelElementSplit)element2).getAverageArrivalSizesSum();
			}
		}

		long simClientCount=model.clientCount*model.repeatCount*split;
		if (simClientCount/threadCount>MAX_CLIENTS_PER_THREAD) return false;
		if (model.surface.getElementCount()/threadCount>MAX_ELEMENTS_PER_THREAD) return false;

		return true;
	}

	/**
	 * �bersetzt die im Modell enthaltenen Java-basierten Skripte.
	 * @param model	Editormodell, in dem nach Skripten gesucht werden soll
	 * @see #lastCompileThread
	 */
	private void compileScripts(final EditModel model) {
		if (model==null) return;
		if (!DynamicFactory.isWindows() && !DynamicFactory.isInMemoryProcessing()) return;

		/* Alle Skripte sammeln */
		final List<String> scripts=new ArrayList<>();
		for (ModelElement element1: model.surface.getElements()) {
			if (element1 instanceof ElementWithScript) {
				if (((ElementWithScript)element1).getMode()==ScriptMode.Java) scripts.add(((ElementWithScript)element1).getScript());
				continue;
			}
			if (element1 instanceof ElementWithAnimationScripts) {
				for (AnimationExpression expression: ((ElementWithAnimationScripts)element1).getAnimationScripts()) {
					if (expression.getMode()==AnimationExpression.ExpressionMode.Java) scripts.add(expression.getScript());
				}
			}
			if (element1 instanceof ModelElementSub) {
				for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
					if (element2 instanceof ElementWithScript) {
						if (((ElementWithScript)element2).getMode()==ScriptMode.Java) scripts.add(((ElementWithScript)element2).getScript());
						continue;
					}
					if (element2 instanceof ElementWithAnimationScripts) {
						for (AnimationExpression expression: ((ElementWithAnimationScripts)element2).getAnimationScripts()) {
							if (expression.getMode()==AnimationExpression.ExpressionMode.Java) scripts.add(expression.getScript());
						}
					}
				}
				continue;
			}
		}
		if (scripts.isEmpty()) return;

		final DynamicFactory dynamicFactory=DynamicFactory.getFactory();
		final ImportSettingsBuilder scriptSettings=new ImportSettingsBuilder(model);

		/* �bersetzen */
		lastCompileThread=new Thread(()->{
			final Thread currentThread=Thread.currentThread();
			for (String script: scripts) if (script!=null && !script.isBlank()) { /* Um die Systembelastung zu begrenzen, immer nur ein Skript zur Zeit �bersetzen. */
				if (currentThread.isInterrupted()) return;
				dynamicFactory.test(script,scriptSettings);
			}
			if (!currentThread.isInterrupted()) lastCompileThread=null;
		},"Background script compilation");
		lastCompileThread.start();
	}

	/**
	 * �bermittelt das aktuelle Modell an das Hintergrund-System.
	 * Dieses pr�ft das Modell evtl. und startet evtl. auch eine
	 * Hintergrund-Simulation.
	 * @param model	Aktuelles Modell
	 * @param startProcessing Modell nur pr�fen (<code>false</code>) oder im Erfolgsfall auch Hintergrundsimulation starten (<code>true</code>)
	 * @return	Gibt <code>null</code> zur�ck, wenn das Modell fehlerfrei ist oder keine Pr�fung stattgefunden hat, sonst eine Fehlermeldung.
	 * @see BackgroundSystem#getStartedSimulator(EditModel, SimLogging, int[], Set)
	 * @see BackgroundSystem#getLastBackgroundMode()
	 */
	public synchronized String process(final EditModel model, final boolean startProcessing) {
		lastUsage=System.currentTimeMillis();
		if (lastModel!=null && lastModel.equalsEditModel(model)) return null;

		stop(false);

		lastBackgroundMode=setup.backgroundSimulation;

		if (!canCheck(model.surface)) return null;
		final boolean canBackgroundProcess=canBackgroundProcess(model);

		/* Pr�fen */

		final String editModelPath=(owner.getLastFile()==null)?null:owner.getLastFile().getParent();
		final Object obj=RunModel.getRunModel(model,editModelPath,!canBackgroundProcess,setup.useMultiCoreSimulation); /* !canBackgroundProcess == testOnly: Wenn wir sp�ter sowieso nicht simulieren k�nnen, dann hier auch keine Daten laden */
		if (obj instanceof StartAnySimulator.PrepareError) return ((StartAnySimulator.PrepareError)obj).error;

		/* Simulieren */

		if (!canBackgroundProcess) {
			lastModel=model.clone();
			if ((setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION || setup.backgroundSimulation==SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS) && setup.useMultiCoreSimulation) {
				compileScripts(lastModel);
			}
			return null;
		}

		if (!startProcessing) return null;
		lastModel=model.clone();
		final StartAnySimulator.PrepareError error=StartAnySimulator.testModel(lastModel,editModelPath);
		/* lastStarter=new StartAnySimulator(lastModel);
		final String error=lastStarter.prepare(); */
		if (error!=null) {
			lastModel=null;
			lastSimulator=null;
			lastStarter=null;
			stop(true);
			return error.error;
		}

		lastStarter=new StartAnySimulator(lastModel,editModelPath);

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
	 * Liefert die Setup-Einstellung zur Hintergrundsimulation, die g�ltig war, als das letzte Mal ein Modell an das System �bergeben wurde.
	 * @return	Setup-Einstellung zur Hintergrundsimulation w�hrend der letzten Modell�bergabe per {@link BackgroundSystem#process(EditModel, boolean)}
	 * @see BackgroundSystem#process(EditModel, boolean)
	 */
	public SetupData.BackgroundProcessingMode getLastBackgroundMode() {
		lastUsage=System.currentTimeMillis();
		return lastBackgroundMode;
	}

	/**
	 * Startet einen fertig vorbereiteten Simulator.
	 * @see #process(EditModel, boolean)
	 * @see #getStartedSimulator(EditModel, SimLogging, int[], Set)
	 */
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
	 * Liefert einen (bereits gestarteten) Simulator f�r ein Editor-Modell
	 * <b>ohne dabei die Hintergrund-Funktionalit�t in irgendeiner Weise zu nutzen oder zu beeinflussen</b>
	 * @param editModel	Editor-Modell das simuliert werden soll
	 * @param logging	Optionales Logging-System (kann <code>null</code> sein)
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 * @return	Liefert im Erfolgsfall ein {@link AnySimulator}-Objekt; im Fehlerfall eine Fehlermeldung als <code>PrepareError</code>-Objekt
	 */
	public Object getNewStartedSimulator(final EditModel editModel, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		lastUsage=System.currentTimeMillis();
		final String editModelPath=(owner.getLastFile()==null)?null:owner.getLastFile().getParent();
		final StartAnySimulator starter=new StartAnySimulator(editModel,editModelPath,logging,loggingIDs,logType);
		final StartAnySimulator.PrepareError error=starter.prepare();
		if (error!=null) return error;
		return starter.start();
	}

	/**
	 * Liefert einen (bereits gestarteten) Simulator f�r ein Editor-Modell
	 * @param editModel	Editor-Modell das simuliert werden soll
	 * @param logging	Optionales Logging-System (kann <code>null</code> sein)
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 * @return	Liefert im Erfolgsfall ein {@link AnySimulator}-Objekt; im Fehlerfall eine Fehlermeldung als <code>PrepareError</code>-Objekt.
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
	 * Stoppt alle m�glichen Hintergrund-Simulationen.
	 */
	public void stop() {
		lastUsage=System.currentTimeMillis();
		stop(true);
	}

	/**
	 * Stoppt alle m�glichen Hintergrund-Simulationen.
	 * @param killTimer	Soll auch der Timer f�r weitere Pr�fungen angehalten werden?
	 */
	private void stop(final boolean killTimer) {
		if (lastTask!=null) lastTask.cancel();
		if (lastSimulator!=null) lastSimulator.cancel();
		if (lastCompileThread!=null) lastCompileThread.interrupt();
		lastTask=null;
		lastModel=null;
		lastSimulator=null;
		lastCompileThread=null;
		lastStarter=null;
		if (killTimer) doneTimer();
	}
}
