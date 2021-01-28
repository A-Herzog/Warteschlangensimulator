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
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.ModelElementSetJS;

/**
 * Laufzeitdaten eines <code>RunElementSetJS</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSetJS
 * @see RunElementData
 */
public class RunElementSetJSData extends RunElementData {
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
	 * Konstruktor der Klasse <code>RunElementSetJSData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param script	Auszuf�hrendes Skript
	 * @param mode	Skriptsprache
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementSetJSData(final RunElement station, final String script, final ModelElementSetJS.ScriptMode mode, final SimulationData simData) {
		super(station);
		this.script=script;

		switch (mode) {
		case Java:
			jsRunner=null;
			javaRunner=DynamicFactory.getFactory().load(script);
			javaRunner.parameter.client=new ClientImpl(simData);
			javaRunner.parameter.system=new SystemImpl(simData,station.id);
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