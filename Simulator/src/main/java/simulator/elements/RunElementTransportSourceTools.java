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
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;

/**
 * Diese Klasse hält statische Methoden zur Unterstützung des
 * Transports von Kunden vor.<br>
 * Sie wird von {@link RunElementTransportSource} verwendet.
 * @author Alexander Herzog
 * @see RunElementTransportSource
 */
public class RunElementTransportSourceTools {
	private RunElementTransportSourceTools() {
	}

	/**
	 * Liefert die ID einer Transport-Ziel-Station basierend auf deren Namen
	 * @param element	Editor-Station für Transport-Start (aus der der globale Surface ausgelesen wird)
	 * @param destination	Name der Zielstation
	 * @return	ID der Zielstation oder -1, wenn keine Zielstation mit passendem Namen gefunden wurde
	 */
	public static int getDestinationID(final ModelElement element, final String destination) {
		ModelSurface surface=element.getSurface();
		if (surface.getParentSurface()!=null) surface=surface.getParentSurface();

		for (ModelElement e : surface.getElements()) {
			if (e instanceof ModelElementTransportDestination && e.getName().equals(destination)) return e.getId();
			if (e instanceof ModelElementSub) for (ModelElement e2: ((ModelElementSub)e).getSubSurface().getElements()) {
				if (e2 instanceof ModelElementTransportDestination && e2.getName().equals(destination)) return e2.getId();
			}
		}

		return -1;
	}

	/**
	 * Erfasst eine Transportzeit in der Statistik des Kunden und in der Statistik der Transport-Start-Station
	 * @param simData	Simulationsdatenobjekt
	 * @param element	Transport-Start-Station
	 * @param transportTime	Transportzeitenobjekt (aus dem ausgelesen wird, als was die Transportzeit erfasst werden soll)
	 * @param client	Kundenobjekt das transportiert werden soll
	 * @param waitingTimeMS	Warteteit des Kunden vor dem Transport in MS
	 * @param delayTimeMS	Transportzeitdauer in MS
	 */
	public static void logTransportTime(final SimulationData simData, final RunElement element, final RunElementTransportSourceTime transportTime, final RunDataClient client, final long waitingTimeMS, final long delayTimeMS) {
		/* Wartezeit in Kunden-Statistik */
		client.waitingTime+=waitingTimeMS;

		/* Bedienzeit in Kunden-Statistik */
		switch (transportTime.delayType) {
		case DELAY_TYPE_WAITING:
			client.waitingTime+=delayTimeMS;
			break;
		case DELAY_TYPE_TRANSFER:
			client.transferTime+=delayTimeMS;
			break;
		case DELAY_TYPE_PROCESS:
			client.processTime+=delayTimeMS;
			break;
		case DELAY_TYPE_NOTHING:
			/* nicht erfassen */
			break;
		}
		client.residenceTime+=delayTimeMS;

		/* Wartezeit + Verarbeitungszeit in der Statistik für die Station erfassen */
		final long residenceTimeMS=waitingTimeMS+delayTimeMS;
		switch (transportTime.delayType) {
		case DELAY_TYPE_WAITING:
			simData.runData.logStationProcess(simData,element,client,residenceTimeMS,0,0,residenceTimeMS);
			break;
		case DELAY_TYPE_TRANSFER:
			simData.runData.logStationProcess(simData,element,client,waitingTimeMS,delayTimeMS,0,residenceTimeMS);
			break;
		case DELAY_TYPE_PROCESS:
			simData.runData.logStationProcess(simData,element,client,waitingTimeMS,0,delayTimeMS,residenceTimeMS);
			break;
		case DELAY_TYPE_NOTHING:
			simData.runData.logStationProcess(simData,element,client,waitingTimeMS,0,0,residenceTimeMS); /* nicht erfassen */
			break;
		}
	}
}
