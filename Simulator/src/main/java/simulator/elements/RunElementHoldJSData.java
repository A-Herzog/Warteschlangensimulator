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
package simulator.elements;

import java.util.ArrayList;
import java.util.List;

import scripting.java.ClientsImpl;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.ModelElementHoldJS;

/**
 * Laufzeitdaten eines <code>RunElementHoldJS</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementHoldJS
 * @see RunElementData
 */
public class RunElementHoldJSData extends RunElementData {
	/**
	 * Das auszuf�hrende Skript.
	 */
	public final String script;

	/**
	 * JS-Skript-Ausf�hrungsumgebung
	 */
	public final JSRunSimulationData jsRunner;

	/**
	 * Java-Ausf�hrungsumgebung
	 */
	public final DynamicRunner javaRunner;

	/**
	 * Liste der wartenden Kunden
	 */
	public List<RunDataClient> waitingClients;

	/**
	 * Speichert, wann zu letzten Mal ein Kunde die Station verlassen hat (um nicht zum selben Zeitschritt mehrfach die Bedingung zu pr�fen)
	 */
	public long lastRelease;

	/**
	 * Wenn die Warteschlange gerade bearbeitet wird, d�rfen per
	 * {@link RunElementHoldJS#getClient(simulator.runmodel.SimulationData)}
	 * keine Kunden entnommen werden. Dieser Status kann hier eingestellt werden.
	 * @see RunElementHoldJS#processArrival(simulator.runmodel.SimulationData, RunDataClient)
	 * @see RunElementHoldJS#systemStateChangeNotify(SimulationData)
	 */
	public boolean queueLockedForPickUp;

	/**
	 * Konstruktor der Klasse <code>RunElementHoldJSData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param script	Bei der Verz�gerung von Kunden auszuf�hrendes Skript
	 * @param mode	Skriptsprache
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementHoldJSData(final RunElement station, final String script, final ModelElementHoldJS.ScriptMode mode, final SimulationData simData) {
		super(station);
		this.script=script;
		queueLockedForPickUp=false;
		waitingClients=new ArrayList<>();
		lastRelease=-Long.MAX_VALUE;

		switch (mode) {
		case Java:
			jsRunner=null;
			javaRunner=DynamicFactory.getFactory().load(script);
			javaRunner.parameter.clients=new ClientsImpl(simData);
			javaRunner.parameter.system=new SystemImpl(simData);
			break;
		case Javascript:
			jsRunner=new JSRunSimulationData(false,true);
			jsRunner.compile((script==null)?"":script);
			javaRunner=null;
			break;
		default:
			jsRunner=null;
			javaRunner=null;
		}
	}
}
