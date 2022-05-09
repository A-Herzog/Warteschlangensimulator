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
package simulator.events;

import simcore.Event;
import simcore.SimData;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.RunDataTransporterFailure;
import simulator.runmodel.SimulationData;

/**
 * Ereignis zum Ende einer Pause eines Transporters
 * @author Alexander Herzog
 * @see RunDataTransporter
 */
public class TransporterPauseEndEvent extends Event {
	/**
	 * Transporter der seine Pause beendet
	 */
	public RunDataTransporter transporter;

	/**
	 * Ausfall-Objekt, das diese Pause zu verantworten hatte
	 * (und nun ggf. die nächste Pause einplanen kann)
	 */
	public RunDataTransporterFailure failure;

	/**
	 * Konstruktor der Klasse
	 */
	public TransporterPauseEndEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		transporter.endDownTime((SimulationData)data);
		transporter.onlineAgainAt=0;
		transporter.availableStartTime=0;
		transporter.free((SimulationData)data);

		/* Wenn es ein Ausfall bedingt durch Verteilung oder Ausdruck war: Nächsten Ausfallzeitpunkt bestimmen */
		if (failure.pauseStartTime>=0) failure.scheduleDownTime((SimulationData)data,time,((SimulationData)data).runModel.transportersTemplate.type[transporter.type]);
	}
}
