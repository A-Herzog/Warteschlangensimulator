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

import scripting.java.ClientImpl;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.InputValueImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.ModelElementInputJS;

/**
 * Laufzeitdaten eines <code>RunElementInputJS</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementInputJS
 * @see RunElementData
 */
public class RunElementInputJSData extends RunElementData {
	/**
	 * Aktuelle Position in der Liste der Eingabewerte
	 */
	public int position;

	/**
	 * Das auszuführende Skript.
	 */
	public final String script;

	/**
	 * JS-Skript-Ausführungsumgebung
	 */
	public final JSRunSimulationData jsRunner;

	/**
	 * Java-Ausführungsumgebung
	 */
	public final DynamicRunner javaRunner;

	/**
	 * Konstruktor der Klasse <code>RunElementInputData</code>
	 * @param station	Zu dem Datenobjekt zugehöriges <code>RunElementInputJS</code>-Element
	 * @param script	Auszuführendes Skript
	 * @param mode	Skriptsprache
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementInputJSData(final RunElement station, final String script, final ModelElementInputJS.ScriptMode mode, final SimulationData simData) {
		super(station);
		this.script=script;

		position=0;

		switch (mode) {
		case Java:
			jsRunner=null;
			javaRunner=DynamicFactory.getFactory().load(script);
			javaRunner.parameter.system=new SystemImpl(simData,station.id);
			javaRunner.parameter.client=new ClientImpl(simData);
			javaRunner.parameter.inputValue=new InputValueImpl();
			break;
		case Javascript:
			jsRunner=new JSRunSimulationData(false,false);
			jsRunner.compile(script);
			javaRunner=null;
			break;
		default:
			jsRunner=null;
			javaRunner=null;
		}
	}
}
