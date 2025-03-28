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
package simulator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mathtools.NumberTools;
import net.calc.SimulationClient;
import simcore.logging.SimLogging;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import tools.SetupData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ElementNoRemoteSimulation;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Startet die Simulation des Modells lokal oder auf einem Server
 * und liefert ein Interface auf der Simulator-Objekt.
 * @author Alexander Herzog
 * @see AnySimulator
 */
public class StartAnySimulator {
	/** Maximalanzahl an Rechenthreads (wird nur ber�cksichtigt, wenn ein lokaler Simulator gestartet wird) */
	private final int maxThreads;
	/** Editor-Modell */
	private final EditModel editModel;
	/** Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen) */
	private final String editModelPath;
	/** Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik */
	private final SimLogging logging;
	/** Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst) */
	private final int[] loggingIDs;
	/** Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen) */
	private final Set<Simulator.LogType> logType;

	/**
	 * H�lt im Falle einer Client-Server-Simulation die Server-Verbinungs-Simulator-Instanz vor.
	 * @see #prepare()
	 * @see #start()
	 */
	private SimulationClient remoteSimulator;

	/**
	 * H�lt im Falle einer lokalen Simulation die Simulator-Instanz vor.
	 * @see #prepare()
	 * @see #start()
	 */
	private Simulator localSimulator;

	/**
	 * Konstruktor der Klasse
	 * @param maxThreads	Maximalanzahl an Rechenthreads (wird nur ber�cksichtigt, wenn ein lokaler Simulator gestartet wird)
	 * @param editModel	Editor-Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public StartAnySimulator(final int maxThreads, final EditModel editModel, final String editModelPath, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		this.maxThreads=maxThreads;
		this.editModel=editModel;
		this.editModelPath=editModelPath;
		this.logging=logging;
		this.loggingIDs=loggingIDs;
		this.logType=logType;
	}

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Editor-Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> �bergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public StartAnySimulator(final EditModel editModel, final String editModelPath, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		this(Integer.MAX_VALUE,editModel,editModelPath,logging,loggingIDs,logType);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es erfolgt kein Logging
	 * @param maxThreads	Maximalanzahl an Rechenthreads (wird nur ber�cksichtigt, wenn ein lokaler Simulator gestartet wird)
	 * @param editModel	Editor-Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 */
	public StartAnySimulator(final int maxThreads, final EditModel editModel, final String editModelPath) {
		this(maxThreads,editModel,editModelPath,null,null,Simulator.logTypeFull);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es erfolgt kein Logging
	 * @param editModel	Editor-Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 */
	public StartAnySimulator(final EditModel editModel, final String editModelPath) {
		this(Integer.MAX_VALUE,editModel,editModelPath,null,null,Simulator.logTypeFull);
	}

	/**
	 * Liefert die Servereinstellungen zum Zugriff auf einen entfernten Simulationsserver.
	 * @return	Liefert im Erfolgsfall ein 3-elementiges Array (Adresse, Port, Passwort) oder <code>null</code>, wenn keine serverseitige Simulation stattfinden soll.
	 */
	public static Object[] getServerSetup() {
		final SetupData setup=SetupData.getSetup();
		if (!setup.serverUse) return null;
		if (setup.serverData==null || setup.serverData.isBlank()) return null;

		final String[] parts=setup.serverData.split(":");
		if (parts.length<2 || parts.length>3) return null;
		final Long L=NumberTools.getPositiveLong(parts[1]);
		if (L==null) return null;

		if (parts.length==2) {
			return new Object[]{parts[0],Integer.valueOf(L.intValue()),null};
		} else {
			return new Object[]{parts[0],Integer.valueOf(L.intValue()),parts[2]};
		}
	}

	/**
	 * Pr�ft das Modell und bereitet die Simulation vor.
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung.
	 */
	public PrepareError prepare() {
		return prepare(true);
	}


	/**
	 * Pr�ft das Modell und bereitet die Simulation vor.
	 * @param allowLoadBalancer	�ber diesen Parameter kann die Verwendung des Load-Balancers (f�r eine lokale Simulation, bei Remote-Simulationen wird dies ignoriert) generell unterbunden werden (<code>false</code>). Andernfalls (<code>true</code>) wird gem�� Setup und Modell entschieden.
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung.
	 */
	public PrepareError prepare(final boolean allowLoadBalancer) {
		final PrepareError prepareError=testModel(editModel,editModelPath);
		if (prepareError!=null) return prepareError;

		if (isRemoveSimulateable(editModel)) {
			final Object[] serverSettings=getServerSetup();
			if (serverSettings!=null) {
				remoteSimulator=new SimulationClient(editModel,(String)serverSettings[0],(Integer)serverSettings[1],(String)serverSettings[2]);
				if (remoteSimulator.prepare()==null) return null; else remoteSimulator=null;
			}
		}

		int maxThreadsReal=maxThreads;
		final SetupData setup=SetupData.getSetup();
		if (setup.useMultiCoreSimulation) {
			maxThreadsReal=Math.min(maxThreadsReal,setup.useMultiCoreSimulationMaxCount);
		} else {
			maxThreadsReal=1;
		}
		maxThreadsReal=Math.max(maxThreadsReal,1);

		localSimulator=new Simulator(maxThreadsReal,editModel,editModelPath,logging,loggingIDs,logType);
		return localSimulator.prepare(allowLoadBalancer);
	}

	/**
	 * Startet die Simulation.<br>
	 * Vorher muss {@link StartAnySimulator#prepare()} erfolgreich ausgef�hrt werden.
	 * @return	Liefert in Erfolgsfall ein Interface auf den gestarteten Simulator; im Fehlerfall <code>null</code>.
	 */
	public AnySimulator start() {
		if (remoteSimulator!=null) {
			final String error=remoteSimulator.start();
			if (error==null) {
				return remoteSimulator;
			} else {
				remoteSimulator=null;
				localSimulator=new Simulator(editModel,editModelPath,logging,loggingIDs,logType);
				if (localSimulator.prepare()!=null) localSimulator=null;
			}
		}

		if (localSimulator==null) return null;
		localSimulator.start();
		return localSimulator;
	}

	/**
	 * Pr�ft ein Modell.
	 * @param editModel	Zu pr�fendes Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung.
	 */
	public static PrepareError testModel(final EditModel editModel, final String editModelPath) {
		/*
		Evtl. langsam, weil Quelltabellen unn�tiger Weise komplett geladen werden:
		Simulator simulator=new Simulator(editModel,null);
		return simulator.prepare();
		 */

		final Object obj=RunModel.getRunModel(editModel,editModelPath,true,SetupData.getSetup().useMultiCoreSimulation);
		if (obj instanceof StartAnySimulator.PrepareError) return (StartAnySimulator.PrepareError)obj;
		return null;
	}

	/**
	 * Pr�ft, ob ein Model in Bezug auf die Zeichenfl�chen-Elemente von einem externen Rechner simuliert werden kann.
	 * @param surface	Zu pr�fende Zeichenfl�che
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell in Bezug auf die Zeichenfl�chen-Elemente auf einem entfernten Rechner simuliert werden kann
	 */
	private static boolean isRemoveSimulateable(final ModelSurface surface) {
		for (ModelElement element1: surface.getElements()) {
			if (element1 instanceof ElementNoRemoteSimulation && ((ElementNoRemoteSimulation)element1).inputConnected() && ((ElementNoRemoteSimulation)element1).isOutputActive()) return false;
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ElementNoRemoteSimulation && ((ElementNoRemoteSimulation)element2).inputConnected() && ((ElementNoRemoteSimulation)element2).isOutputActive()) return false;
			}
		}
		return true;
	}

	/**
	 * Pr�ft, ob ein Model von einem externen Rechner simuliert werden kann.
	 * @param editModel	Zu pr�fendes Modell
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell auf einem entfernten Rechner simuliert werden kann
	 */
	public static boolean isRemoveSimulateable(final EditModel editModel) {
		if (editModel.modelLoadData.willChangeModel()) return false;
		return isRemoveSimulateable(editModel.surface);
	}

	/**
	 * Weitere optionale Fehler-Flags f�r {@link StartAnySimulator.PrepareError#additional}
	 * @see StartAnySimulator.PrepareError#additional
	 */
	public enum AdditionalPrepareErrorInfo {
		/** Es steht kein Java-Kompiler zur Verf�gunng. */
		NO_COMPILER
	}

	/**
	 * Diese Klasse beschreibt die Daten zu einem Fehler
	 * beim Erstellen eines Laufzeitmodells.
	 */
	public static class PrepareError {
		/**
		 * Fehlermeldung
		 */
		public final String error;

		/**
		 * ID der Station, an der der Fehler aufgetreten ist.<br>
		 * (Kann -1 sein, wenn der Fehler keiner Station zugeordnet werden kann.)
		 */
		public final int id;

		/**
		 * Menge (die leer sein kann, aber nicht <code>null</code> ist) mit m�glichen weiteren Fehler-Flags
		 * @see AdditionalPrepareErrorInfo
		 */
		public final Set<AdditionalPrepareErrorInfo> additional;

		/**
		 * Konstruktor der Klasse
		 * @param error	Fehlermeldung
		 * @param id	ID der Station, an der der Fehler aufgetreten ist. (Kann -1 sein, wenn der Fehler keiner Station zugeordnet werden kann.)
		 */
		public PrepareError(final String error, final int id) {
			this.error=error;
			this.id=id;
			additional=Collections.emptySet();
		}

		/**
		 * Konstruktor der Klasse
		 * @param error	Fehlermeldung
		 * @param id	ID der Station, an der der Fehler aufgetreten ist. (Kann -1 sein, wenn der Fehler keiner Station zugeordnet werden kann.)
		 * @param additional	Menge mit m�glichen weiteren Fehler-Flags (darf <code>null</code> sein)
		 */
		public PrepareError(final String error, final int id, final Set<AdditionalPrepareErrorInfo> additional) {
			this.error=error;
			this.id=id;
			if (additional==null) {
				this.additional=Collections.emptySet();
			} else {
				this.additional=new HashSet<>(additional);
			}
		}
	}
}