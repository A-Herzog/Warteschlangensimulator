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
import simulator.elements.TransporterPosition;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.SimulationData;

/**
 * Wird ausgelöst, wenn ein Transporter an einer Station angekommen ist
 * @author Alexander Herzog
 */
public class TransporterArrivalEvent extends Event {
	/**
	 * Station an der der Transporter eingetroffen ist.
	 */
	public TransporterPosition station;

	/**
	 * Transporter der eingetroffen ist.
	 */
	public RunDataTransporter transporter;

	/**
	 * Konstruktor der Klasse
	 */
	public TransporterArrivalEvent() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public void run(SimData data) {
		final SimulationData simData=(SimulationData)data;

		/* Transporter mitteilen, dass er angekommen ist */
		transporter.arrival(simData,data.currentTime);

		/* Station mitteilen, dass ein Transporter angekommen ist */
		if (station!=null) station.transporterArrival(transporter,simData);

		/* Transporter (nach optionalem Entladen durch die Station) mitteilen, dass er nun wieder für andere Stationen zur Verfügung steht */
		if (!transporter.inTransfer) transporter.free(simData);
	}
}
