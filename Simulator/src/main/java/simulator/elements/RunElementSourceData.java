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

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;

/**
 * Laufzeitdaten eines <code>RunElementSource</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSource
 * @see RunElementData
 */
public class RunElementSourceData extends RunElementData {
	/** Höchstanzahl an Kundenankünften überhaupt; eigentlich wird das Simulationsende an anderer Stelle ermittelt, aber hat allein diese Quelle das doppelte dieses Wertes an Kunden erzeugt, so stellt sie die Arbeit ein. */
	public long maxSystemArrival;

	/**
	 * Thread-lokaler Ankunftsdatensatz
	 * @see RunElementSourceRecord
	 */
	public RunElementSourceRecordData recordData;

	/**
	 * Konstruktor der Klasse <code>RunElementSourceData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param simData	Simulationsdatenobjekt
	 * @param record	Modellweit gültiger (nicht Thread-lokaler) Ankunftsdatensatz
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementSourceData(final RunElement station, final SimulationData simData, final RunElementSourceRecord record, final String[] variableNames) {
		super(station);
		maxSystemArrival=0;
		recordData=new RunElementSourceRecordData(simData,record,variableNames);
	}
}