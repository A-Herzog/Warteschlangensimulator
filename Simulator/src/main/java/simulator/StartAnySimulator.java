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
	/** Maximalanzahl an Rechenthreads (wird nur berücksichtigt, wenn ein lokaler Simulator gestartet wird) */
	private final int maxThreads;
	/** Editor-Modell */
	private final EditModel editModel;
	/** Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik */
	private final SimLogging logging;
	/** Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst) */
	private final int[] loggingIDs;
	/** Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen) */
	private final Set<Simulator.LogType> logType;

	/**
	 * Hält im Falle einer Client-Server-Simulation die Server-Verbinungs-Simulator-Instanz vor.
	 * @see #prepare()
	 * @see #start()
	 */
	private SimulationClient remoteSimulator;

	/**
	 * Hält im Falle einer lokalen Simulation die Simulator-Instanz vor.
	 * @see #prepare()
	 * @see #start()
	 */
	private Simulator localSimulator;

	/**
	 * Konstruktor der Klasse
	 * @param maxThreads	Maximalanzahl an Rechenthreads (wird nur berücksichtigt, wenn ein lokaler Simulator gestartet wird)
	 * @param editModel	Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public StartAnySimulator(final int maxThreads, final EditModel editModel, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		this.maxThreads=maxThreads;
		this.editModel=editModel;
		this.logging=logging;
		this.loggingIDs=loggingIDs;
		this.logType=logType;
	}

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 * @param loggingIDs	Liste der Stations-IDs deren Ereignisse beim Logging erfasst werden sollen (nur von Bedeutung, wenn das Logging als solches aktiv ist; kann <code>null</code> sein, dann werden die Ereignisse aller Stationen erfasst)
	 * @param logType	Welche Arten von Ereignissen sollen erfasst werden? (<code>null</code> bedeutet: alles erfassen)
	 */
	public StartAnySimulator(final EditModel editModel, final SimLogging logging, final int[] loggingIDs, final Set<Simulator.LogType> logType) {
		this(Integer.MAX_VALUE,editModel,logging,loggingIDs,logType);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es erfolgt kein Logging
	 * @param maxThreads	Maximalanzahl an Rechenthreads (wird nur berücksichtigt, wenn ein lokaler Simulator gestartet wird)
	 * @param editModel	Editor-Modell
	 */
	public StartAnySimulator(final int maxThreads, final EditModel editModel) {
		this(maxThreads,editModel,null,null,Simulator.logTypeFull);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es erfolgt kein Logging
	 * @param editModel	Editor-Modell
	 */
	public StartAnySimulator(final EditModel editModel) {
		this(Integer.MAX_VALUE,editModel,null,null,Simulator.logTypeFull);
	}

	/**
	 * Liefert die Servereinstellungen zum Zugriff auf einen entfernten Simulationsserver.
	 * @return	Liefert im Erfolgsfall ein 3-elementiges Array (Adresse, Port, Passwort) oder <code>null</code>, wenn keine serverseitige Simulation stattfinden soll.
	 */
	public static Object[] getServerSetup() {
		final SetupData setup=SetupData.getSetup();
		if (!setup.serverUse) return null;
		if (setup.serverData==null || setup.serverData.trim().isEmpty()) return null;

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
	 * Prüft das Modell und bereitet die Simulation vor.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public PrepareError prepare() {
		return prepare(true);
	}


	/**
	 * Prüft das Modell und bereitet die Simulation vor.
	 * @param allowLoadBalancer	Über diesen Parameter kann die Verwendung des Load-Balancers (für eine lokale Simulation, bei Remote-Simulationen wird dies ignoriert) generell unterbunden werden (<code>false</code>). Andernfalls (<code>true</code>) wird gemäß Setup und Modell entschieden.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public PrepareError prepare(final boolean allowLoadBalancer) {
		final PrepareError prepareError=testModel(editModel);
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

		localSimulator=new Simulator(maxThreadsReal,editModel,logging,loggingIDs,logType);
		return localSimulator.prepare(allowLoadBalancer);
	}

	/**
	 * Startet die Simulation.<br>
	 * Vorher muss {@link StartAnySimulator#prepare()} erfolgreich ausgeführt werden.
	 * @return	Liefert in Erfolgsfall ein Interface auf den gestarteten Simulator; im Fehlerfall <code>null</code>.
	 */
	public AnySimulator start() {
		if (remoteSimulator!=null) {
			final String error=remoteSimulator.start();
			if (error==null) {
				return remoteSimulator;
			} else {
				remoteSimulator=null;
				localSimulator=new Simulator(editModel,logging,loggingIDs,logType);
				if (localSimulator.prepare()!=null) localSimulator=null;
			}
		}

		if (localSimulator==null) return null;
		localSimulator.start();
		return localSimulator;
	}

	/**
	 * Prüft ein Modell.
	 * @param editModel	Zu prüfendes Modell
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public static PrepareError testModel(final EditModel editModel) {
		/*
		Evtl. langsam, weil Quelltabellen unnötiger Weise komplett geladen werden:
		Simulator simulator=new Simulator(editModel,null);
		return simulator.prepare();
		 */

		final Object obj=RunModel.getRunModel(editModel,true,SetupData.getSetup().useMultiCoreSimulation);
		if (obj instanceof StartAnySimulator.PrepareError) return (StartAnySimulator.PrepareError)obj;
		return null;
	}

	/**
	 * Prüft, ob ein Model in Bezug auf die Zeichenflächen-Elemente von einem externen Rechner simuliert werden kann.
	 * @param surface	Zu prüfende Zeichenfläche
	 * @return	Gibt <code>true</code> zurück, wenn das Modell in Bezug auf die Zeichenflächen-Elemente auf einem entfernten Rechner simuliert werden kann
	 */
	private static boolean isRemoveSimulateable(final ModelSurface surface) {
		for (ModelElement element1: surface.getElements()) {
			if (element1 instanceof ElementNoRemoteSimulation && (((ElementNoRemoteSimulation)element1).inputConnected())) return false;
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ElementNoRemoteSimulation && (((ElementNoRemoteSimulation)element2).inputConnected())) return false;
			}
		}
		return true;
	}

	/**
	 * Prüft, ob ein Model von einem externen Rechner simuliert werden kann.
	 * @param editModel	Zu prüfendes Modell
	 * @return	Gibt <code>true</code> zurück, wenn das Modell auf einem entfernten Rechner simuliert werden kann
	 */
	public static boolean isRemoveSimulateable(final EditModel editModel) {
		if (editModel.modelLoadData.willChangeModel()) return false;
		return isRemoveSimulateable(editModel.surface);
	}

	/**
	 * Weitere optionale Fehler-Flags für {@link StartAnySimulator.PrepareError#additional}
	 * @see StartAnySimulator.PrepareError#additional
	 */
	public enum AdditionalPrepareErrorInfo {
		/** Es steht kein Java-Kompiler zur Verfügunng. */
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
		 * Menge (die leer sein kann, aber nicht <code>null</code> ist) mit möglichen weiteren Fehler-Flags
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
		 * @param additional	Menge mit möglichen weiteren Fehler-Flags (darf <code>null</code> sein)
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