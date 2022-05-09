/**
 * Copyright 2021 Alexander Herzog
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
package simulator.events;

import language.Language;
import scripting.java.SystemInterface;
import scripting.js.JSCommandSystem;
import simcore.Event;
import simcore.SimData;
import simulator.coreelements.RunElement;
import simulator.elements.RunElementHoldJS;
import simulator.elements.RunElementSetJS;
import simulator.runmodel.SimulationData;

/**
 * Dieses Ereignis ermöglicht die durch Nutzercode
 * ausgelöste zeitverzögerte Abarbeitung des Skriptcodes
 * an einer entsprechenden Station.
 * @author Alexander Herzog
 * @see RunElementSetJS
 * @see RunElementHoldJS
 * @see SystemInterface#triggerScriptExecution(int, double)
 * @see JSCommandSystem#triggerScriptExecution(int, double)
 */
public class TriggerScriptExecutionEvent extends Event {
	/**
	 * Station an der die Skriptausführung ausgelöst werden soll.
	 */
	public RunElement station;

	/**
	 * Konstruktor der Klasse
	 */
	public TriggerScriptExecutionEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Logging */
		if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.UserTriggeredScriptExecution"),String.format(Language.tr("Simulation.Log.UserTriggeredScriptExecution.Info"),station.name));


		/* Skriptausführung anstoßen: Skript-Station */
		if (station instanceof RunElementSetJS) {
			final RunElementSetJS setJS=(RunElementSetJS)station;
			setJS.processScriptExecutionOnly(simData,null);
		}

		/* Skriptausführung anstoßen: Skript-Bedingung-Station */
		if (station instanceof RunElementHoldJS) {
			final RunElementHoldJS holdJS=(RunElementHoldJS)station;
			holdJS.systemStateChangeNotify(simData);
		}

		simData.runData.fireStateChangeNotify(simData);
	}
}
