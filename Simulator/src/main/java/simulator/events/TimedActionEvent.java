/**
 * Copyright 2022 Alexander Herzog
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

import simcore.Event;
import simcore.SimData;
import simulator.elements.RunElementAction;
import simulator.elements.RunElementActionRecord;
import simulator.runmodel.SimulationData;

/**
 * Löst eine zeitgesteuerte Aktion aus.
 * @author Alexander Herzog
 * @see RunElementAction#timeTrigger(SimulationData, int)
 * @see RunElementActionRecord#scheduleNextTimedAction(SimulationData, long, int)
 */
public class TimedActionEvent extends Event {
	/**
	 * Station an der die zeitgesteuerte Aktion ausgelöst werden soll
	 * @see RunElementAction#timeTrigger(SimulationData, int)
	 */
	public RunElementAction actionStation;

	/**
	 * Index der Aktion innerhalb des Aktion-Elements
	 * @see RunElementAction#timeTrigger(SimulationData, int)
	 */
	public int actionIndex;

	/**
	 * Konstruktor der Klasse
	 */
	public TimedActionEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		actionStation.timeTrigger((SimulationData)data,actionIndex);
	}
}
