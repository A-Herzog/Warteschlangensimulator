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
	private final EditModel editModel;
	private final SimLogging logging;

	private SimulationClient remoteSimulator;
	private Simulator localSimulator;

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Editor-Modell
	 * @param logging	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird der Lauf durch den angegebenen Logger aufgezeichnet; ansonsten erfolgt nur die normale Aufzeichnung in der Statistik
	 */
	public StartAnySimulator(final EditModel editModel, final SimLogging logging) {
		this.editModel=editModel;
		this.logging=logging;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es erfolgt kein Logging
	 * @param editModel	Editor-Modell
	 */
	public StartAnySimulator(final EditModel editModel) {
		this(editModel,null);
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
	public String prepare() {
		final String prepareError=testModel(editModel);
		if (prepareError!=null) return prepareError;

		if (isRemoveSimulateable(editModel)) {
			final Object[] serverSettings=getServerSetup();
			if (serverSettings!=null) {
				remoteSimulator=new SimulationClient(editModel,(String)serverSettings[0],(Integer)serverSettings[1],(String)serverSettings[2]);
				if (remoteSimulator.prepare()==null) return null; else remoteSimulator=null;
			}
		}

		localSimulator=new Simulator(editModel,logging);
		return localSimulator.prepare();
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
				localSimulator=new Simulator(editModel,logging);
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
	public static String testModel(final EditModel editModel) {
		/*
		Evtl. langsam, weil Quelltabellen unnötiger Weise komplett geladen werden:
		Simulator simulator=new Simulator(editModel,null);
		return simulator.prepare();
		 */

		final Object obj=RunModel.getRunModel(editModel,true);
		if (obj instanceof String) return (String)obj;
		return null;

	}

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
}