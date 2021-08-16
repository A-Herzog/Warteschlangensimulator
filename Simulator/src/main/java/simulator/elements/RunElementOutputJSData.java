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
import scripting.java.OutputImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.ModelElementOutputJS;

/**
 * Laufzeitdaten eines <code>RunElementOutputJS</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementOutputJS
 * @see RunElementData
 */
public class RunElementOutputJSData extends RunElementData {
	/**
	 * Das auszuf�hrende Skript.
	 */
	public final String script;

	/**
	 * Auszuf�hrendes Skript f�r die �berschrift (kann <code>null</code> sein, wenn keine dedizierte �berschrift ausgegeben werden soll).
	 */
	public final String scriptHeading;

	/**
	 * JS-Skript-Ausf�hrungsumgebung
	 */
	public final JSRunSimulationData jsRunner;

	/**
	 * Java-Ausf�hrungsumgebung
	 */
	public final DynamicRunner javaRunner;

	/**
	 * Java-Output-String
	 */
	public final StringBuilder output;

	/**
	 * JS-Skript-Ausf�hrungsumgebung (f�r �berschriften)
	 */
	public final JSRunSimulationData jsRunnerHeading;

	/**
	 * Java-Ausf�hrungsumgebung (f�r �berschriften)
	 */
	public final DynamicRunner javaRunnerHeading;

	/**
	 * Java-Output-String (f�r �berschriften)
	 */
	public final StringBuilder outputHeading;

	/**
	 * Konstruktor der Klasse <code>RunElementOutputJSData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param script	Auszuf�hrendes Skript
	 * @param mode	Skriptsprache
	 * @param scriptHeading	Auszuf�hrendes Skript f�r die �berschriftenausgabe (kann <code>null</code> sein)
	 * @param modeHeading	Skriptsprache f�r das �berschriften-Skript
	 * @param jRunner	Im Falle von Java als Sprache kann hier optional ein bereits vorbereiteter Runner, der dann kopiert wird, angegeben werden
	 * @param jRunnerHeading	Im Falle von Java als Sprache f�r das �berschriften-Skript (und wenn dieses nicht <code>null</code> ist) kann hier optional ein bereits vorbereiteter Runner, der dann kopiert wird, angegeben werden
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementOutputJSData(final RunElement station, final String script, final ModelElementOutputJS.ScriptMode mode, final String scriptHeading, final ModelElementOutputJS.ScriptMode modeHeading, final DynamicRunner jRunner, final DynamicRunner jRunnerHeading, final SimulationData simData) {
		super(station);

		this.script=script;

		switch (mode) {
		case Javascript:
			jsRunner=new JSRunSimulationData(true,false);
			jsRunner.compile(script);
			javaRunner=null;
			output=null;
			break;
		case Java:
			jsRunner=null;
			if (jRunner==null) {
				javaRunner=DynamicFactory.getFactory().load(script,simData.runModel.javaImports);
			} else {
				javaRunner=DynamicFactory.getFactory().load(jRunner,simData.runModel.javaImports);
			}
			javaRunner.parameter.system=new SystemImpl(simData,station.id);
			javaRunner.parameter.client=new ClientImpl(simData);
			output=new StringBuilder();
			javaRunner.parameter.output=new OutputImpl(s->output.append(s),false);
			break;
		default:
			jsRunner=null;
			javaRunner=null;
			output=null;
		}

		this.scriptHeading=scriptHeading;

		if (scriptHeading!=null) {
			switch (modeHeading) {
			case Javascript:
				jsRunnerHeading=new JSRunSimulationData(true,false);
				jsRunnerHeading.compile(scriptHeading);
				javaRunnerHeading=null;
				outputHeading=null;
				break;
			case Java:
				jsRunnerHeading=null;
				if (jRunnerHeading==null) {
					javaRunnerHeading=DynamicFactory.getFactory().load(scriptHeading,simData.runModel.javaImports);
				} else {
					javaRunnerHeading=DynamicFactory.getFactory().load(jRunnerHeading,simData.runModel.javaImports);
				}
				javaRunnerHeading.parameter.system=new SystemImpl(simData,station.id);
				javaRunnerHeading.parameter.client=new ClientImpl(simData);
				outputHeading=new StringBuilder();
				javaRunnerHeading.parameter.output=new OutputImpl(s->outputHeading.append(s),false);
				break;
			default:
				jsRunnerHeading=null;
				javaRunnerHeading=null;
				outputHeading=null;
			}
		} else {
			jsRunnerHeading=null;
			javaRunnerHeading=null;
			outputHeading=null;
		}
	}
}