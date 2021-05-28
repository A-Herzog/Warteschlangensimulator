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

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import scripting.java.ClientImpl;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElementData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.ModelElementDecideJS;

/**
 * Laufzeitdaten eines <code>RunElementDecideByScript</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDecideByScript
 * @see RunElementData
 */
public class RunElementDecideByScriptData extends RunElementData {
	/**
	 * Das auszuführende Skript.
	 */
	public final String script;

	/**
	 * JS-Skript-Ausführungsumgebung
	 */
	private final JSRunSimulationData jsRunner;

	/**
	 * Java-Ausführungsumgebung
	 */
	private final DynamicRunner javaRunner;

	/**
	 * Ausgabeobjekt das die Java-Ausgaben aufnimmt.
	 * (In der Javascript-Variante erfolgen Rückgaben direkt,
	 * so dass kein solches Objekt benötigt wird.)
	 * @see #getNextStationJava(SimulationData, RunDataClient)
	 */
	private final StringBuilder output;

	/**
	 * Konstruktor der Klasse <code>RunElementDecideByScriptData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param script	Bei der Verzweigung von Kunden auszuführendes Skript
	 * @param mode	Skriptsprache
	 * @param jRunner	Im Falle von Java als Sprache kann hier optional ein bereits vorbereiteter Runner, der dann kopiert wird, angegeben werden
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementDecideByScriptData(final RunElementDecideByScript station, final String script, final ModelElementDecideJS.ScriptMode mode, final DynamicRunner jRunner, final SimulationData simData) {
		super(station);
		this.script=script;

		switch (mode) {
		case Java:
			jsRunner=null;
			if (jRunner==null) {
				javaRunner=DynamicFactory.getFactory().load(script);
			} else {
				javaRunner=DynamicFactory.getFactory().load(jRunner);
			}
			javaRunner.parameter.client=new ClientImpl(simData);
			javaRunner.parameter.system=new SystemImpl(simData,station.id);
			output=new StringBuilder();
			javaRunner.parameter.output=new OutputImpl(s->output.append(s),false);
			break;
		case Javascript:
			jsRunner=new JSRunSimulationData(true,false);
			jsRunner.compile(script);
			javaRunner=null;
			output=null;
			break;
		default:
			jsRunner=null;
			javaRunner=null;
			output=null;
		}
	}

	/**
	 * Ermittelt auf Basis des hinterlegten Skripts welchen Ausgangspfad der aktuelle Kunde wählen soll.<br>
	 * (Skriptsprache: Javascript)
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde (auf seine Daten kann im Skript zugegriffen werden)
	 * @return	1-basierende Nummer des zu verwendenden Ausgangspfads
	 * @see #getNextStation(SimulationData, RunDataClient)
	 */
	private int getNextStationJS(final SimulationData simData, final RunDataClient client) {
		jsRunner.setSimulationData(simData,station.id,client);
		String result=jsRunner.runCompiled();
		if (simData.loggingActive) station.logJS(simData,script,result);
		if (!jsRunner.getLastSuccess()) {
			/* Logging */
			if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DecideByScript"),String.format(Language.tr("Simulation.Log.DecideByScript.ErrorScript"),station.name));
			/* Evtl. Abbruch der Simulation */
			if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DecideByScript.ErrorScript"),station.name)+" "+result);
			return 0;
		} else {
			if (jsRunner.isOutputDouble()) return (int)FastMath.round(jsRunner.getOutputDouble());
			result=result.trim();
			final Double D=NumberTools.getPlainDouble(result);
			if (D==null) {
				/* Logging */
				if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DecideByScript"),String.format(Language.tr("Simulation.Log.DecideByScript.ErrorNoNumber"),station.name,result));
				/* Evtl. Abbruch der Simulation */
				if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DecideByScript.ErrorNoNumber"),station.name,result));
				return 0;
			}
			simData.runData.updateMapValuesForStatistics(simData);
			return (int)FastMath.round(D);
		}
	}

	/**
	 * Ermittelt auf Basis des hinterlegten Skripts welchen Ausgangspfad der aktuelle Kunde wählen soll.<br>
	 * (Skriptsprache: Java)
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde (auf seine Daten kann im Skript zugegriffen werden)
	 * @return	1-basierende Nummer des zu verwendenden Ausgangspfads
	 * @see #getNextStation(SimulationData, RunDataClient)
	 */
	private int getNextStationJava(final SimulationData simData, final RunDataClient client) {
		javaRunner.parameter.client.setClient(client);
		javaRunner.run();
		if (javaRunner.getStatus()!=DynamicStatus.OK) {
			/* Logging */
			if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DecideByScript"),String.format(Language.tr("Simulation.Log.DecideByScript.ErrorScript"),station.name)+"\n"+DynamicFactory.getLongStatusText(javaRunner));
			/* Evtl. Abbruch der Simulation */
			if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DecideByScript.ErrorScript"),station.name)+"\n"+DynamicFactory.getLongStatusText(javaRunner));
			return 0;
		} else {
			output.setLength(0);
			if (javaRunner.parameter.output.isOutputDouble()) return (int)FastMath.round(javaRunner.parameter.output.getOutputDouble());
			final String result=output.toString().trim();
			final Double D=NumberTools.getPlainDouble(result);
			if (D==null) {
				/* Logging */
				if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DecideByScript"),String.format(Language.tr("Simulation.Log.DecideByScript.ErrorNoNumber"),station.name,result));
				/* Evtl. Abbruch der Simulation */
				if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DecideByScript.ErrorNoNumber"),station.name,result));
				return 0;
			}
			simData.runData.updateMapValuesForStatistics(simData);
			return (int)FastMath.round(D);
		}
	}

	/**
	 * Ermittelt auf Basis des hinterlegten Skripts welchen Ausgangspfad der aktuelle Kunde wählen soll
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde (auf seine Daten kann im Skript zugegriffen werden)
	 * @return	1-basierende Nummer des zu verwendenden Ausgangspfads
	 */
	public int getNextStation(final SimulationData simData, final RunDataClient client) {
		if (jsRunner!=null) return getNextStationJS(simData,client);
		if (javaRunner!=null) return getNextStationJava(simData,client);
		return 0;
	}
}
