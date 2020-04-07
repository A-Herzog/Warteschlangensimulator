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
package simulator.coreelements;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.elements.ModelElementEdge;

/**
 * Erweiterung der Laufzeit-Element-Basisklasse für
 * Stationen, die nur einen Ausgang besitzen und die Kunden
 * immer zu diesem weitergeleitet werden
 * @author Alexander Herzog
 */
public abstract class RunElementPassThrough extends RunElement {
	private int connectionId;
	private RunElement connection;

	/**
	 * Konstruktor der Klasse <code>RunElementPassThrough</code>
	 * @param element	Modell-Element aus dem ID und Farbe ausgelesen werden
	 * @param name	Name der Station
	 */
	public RunElementPassThrough(final ModelElementBox element, final String name) {
		super(element,name);
	}

	/**
	 * Trägt die angegebene Modell-Kante als auslaufende Kante in das Laufzeitelement ein
	 * @param edge	Modell-Kante zum nächsten Element
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected final String buildEdgeOut(final ModelElementEdge edge) {
		connectionId=findNextId(edge);
		if (connectionId<0) return String.format(Language.tr("Simulation.Creator.NoEdgeOut"),id);
		return null;
	}

	/**
	 * Trägt die angegebene Modell-Kante als auslaufende Kante in das Laufzeitelement ein
	 * @param element	Element von dem die Kante ausgeht
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected final String buildEdgeOut(final ModelElementEdgeOut element) {
		return buildEdgeOut(element.getEdgeOut());
	}

	/**
	 * Prüft eine Modell-Kante zur Verwendung als auslaufende Kante für dieses Laufzeitelement
	 * @param edge	Modell-Kante zum nächsten Element
	 * @param element	Modell-Element welches zu diesem Laufzeit-Element gehört
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected final RunModelCreatorStatus testEdgeOut(final ModelElementEdge edge, final ModelElementBox element) {
		if (findNextId(edge)<0) return RunModelCreatorStatus.noEdgeOut(element);
		return null;
	}

	/**
	 * Prüft eine Modell-Kante zur Verwendung als auslaufende Kante für dieses Laufzeitelement
	 * @param element	Modell-Element welches zu diesem Laufzeit-Element gehört
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected final RunModelCreatorStatus testEdgeOut(final ModelElementEdgeOut element) {
		if (findNextId(element.getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);
		return null;
	}

	@Override
	public void prepareRun(final RunModel runModel) {
		connection=runModel.elements.get(connectionId);
	}

	@Override
	public void processLeave(SimulationData simData, RunDataClient client) {
		StationLeaveEvent.unannounceClient(simData,client,connection);
		StationLeaveEvent.sendToStation(simData,client,this,connection,false); /* false = kein SystemChanged auslösen, da processLeave von StationLeaveEvent.run ausgelöst wird und dieses als nächstes ebenfalls SystemChanged auslöst. */
	}

	@Override
	public RunElement getNext() {
		return connection;
	}
}
