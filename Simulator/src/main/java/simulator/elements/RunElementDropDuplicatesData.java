/**
 * Copyright 2026 Alexander Herzog
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

import java.util.HashMap;
import java.util.Map;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;

/**
 * Laufzeitdaten eines {@link RunElementDropDuplicates}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDropDuplicates
 * @see RunElementData
 */
public class RunElementDropDuplicatesData extends RunElementData {
	/**
	 * Für wie lange sollen die IDs gespeichert werden? (in Simulationszeit)
	 */
	private final long storageTime;

	/**
	 * Speichert die Zuordnung von Kundendatenfeldwerten zu Zeitpunkten, wann diese das letzte Mal gesehen wurden
	 */
	private final Map<Double,Long> map;

	/**
	 * Konstruktor der Klasse <code>RunElementDelayData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param storageTimeSeconds	Für wie lange sollen die IDs gespeichert werden? (in Sekunden)
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementDropDuplicatesData(final RunElement station, final double storageTimeSeconds, final SimulationData simData) {
		super(station,simData);
		storageTime=(long)(storageTimeSeconds*simData.runModel.scaleToSimTime+0.5);
		map=new HashMap<>();
	}

	/**
	 * Prüfen, ob alte Einträge aus der Map entfernt werden können.
	 * @param currentTime	Aktuelle Simulationszeit
	 */
	private void cleanDB(final long currentTime) {
		final long maxAge=currentTime-storageTime;
		boolean done=false;
		while (!done) {
			done=true;
			Double delete=null;
			for (var entry: map.entrySet()) if (entry.getValue()<maxAge) {delete=entry.getKey(); break;}
			if (delete!=null) {
				map.remove(delete);
				done=false;
			}
		}
	}

	/**
	 * Ist bereits ein Eintrag mit dem entsprechenden Wert in der Map?
	 * @param clientDataValue	Kundendatenwert
	 * @param currentTime	Aktuelle Simulationszeit
	 * @return	Liefert <code>true</code>, wenn es sich um ein Duplikat handelt.
	 */
	public boolean isDuplicate(final double clientDataValue, final long currentTime) {
		/* Alte Einträge entfernen */
		cleanDB(currentTime);

		/* Ist der Datensatz bereits in der Map? */
		final boolean isDuplicate=map.containsKey(clientDataValue);

		/* In jedem Fall Map mit neuer Zeit aktualisieren. */
		map.put(clientDataValue,currentTime);

		return isDuplicate;
	}
}
