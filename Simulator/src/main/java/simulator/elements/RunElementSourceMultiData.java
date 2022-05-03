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
 * Laufzeitdaten eines <code>RunElementSourceMulti</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementSourceMulti
 * @see RunElementData
 */
public class RunElementSourceMultiData extends RunElementData {
	/**
	 * Dieser Wert wird in {@link RunElementSourceMulti#processArrivalEvent(SimulationData, boolean, int)}
	 * einmalig berechnet und hier gespeichert. Es gibt an, nach wie vielen Ankünften
	 * die Simulation abgebrochen wird (weil es offenbar einen Fehler gegeben haben
	 * muss, der ein reguläres Ende verhindert). Es handelt sich um den 1000-fachen
	 * Wert der eigentlich für den Thread geplanten Anzahl an Ankünften.
	 * @see RunElementSourceMulti#processArrivalEvent(SimulationData, boolean, int)
	 */
	public long systemMaxArrival=-1;

	/**
	 * Thread-lokale Ankunftsdatensätze
	 * @see RunElementSourceRecord
	 */
	public RunElementSourceRecordData[] recordData;

	/**
	 * Konstruktor der Klasse <code>RunElementSourceMultiData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param simData	Simulationsdatenobjekt
	 * @param record	Modellweit gültige (nicht Thread-lokale) Ankunftsdatensätze
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 */
	public RunElementSourceMultiData(final RunElement station, final SimulationData simData, final RunElementSourceRecord record[], final String[] variableNames) {
		super(station);

		recordData=new RunElementSourceRecordData[record.length];
		for (int i=0;i<record.length;i++) recordData[i]=new RunElementSourceRecordData(simData,record[i],variableNames);
	}
}