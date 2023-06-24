/**
 * Copyright 2023 Alexander Herzog
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

import java.util.Arrays;
import java.util.List;

import language.Language;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import ui.modeleditor.elements.CounterCondition;

/**
 * Thread-lokales Laufzeitobjekt zu {@link CounterCondition}
 * @see CounterCondition
 * @author Alexander Herzog
 */
public class RunCounterConditionData {
	/**
	 * Zugehöriges Editor-Objekt
	 */
	private final CounterCondition counterCondition;

	/**
	 * Zu prüfende Bedingung (kann <code>null</code> sein)
	 */
	private ExpressionMultiEval condition;

	/**
	 * Welche Kundentypen sollen erfasst werden?
	 */
	private boolean[] clientType;

	/**
	 * Konstruktor der Klasse
	 * @param counterCondition	Zugehöriges Editor-Objekt
	 */
	public RunCounterConditionData(final CounterCondition counterCondition) {
		this.counterCondition=counterCondition;
	}

	/**
	 * Copy-Konstruktor der Klasse<br>
	 * ({@link #build(RunModel)} muss bei der Kopie immer aufgerufen werden.)
	 * @param copySource	Zu kopierendes Objekt
	 */
	public RunCounterConditionData(final RunCounterConditionData copySource) {
		counterCondition=copySource.counterCondition;
	}

	/**
	 * Prüft die angegebene Bedingung.
	 * @param variableNames	Namen der Variablen im Modell
	 * @param id	ID des Elements in dem diese Bedingung zum Einsatz kommt
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	public String test(final String[] variableNames, final int id) {
		/* Bedingung */
		final String condition=counterCondition.getCondition();
		if (!condition.trim().isEmpty()) {
			final int error=ExpressionMultiEval.check(condition,variableNames);
			if (error>=0) return String.format(Language.tr("Simulation.Creator.CounterCondition"),condition,id,error+1);
		}

		return null;
	}

	/**
	 * Initialisiert das thread-lokale Objekt.
	 * @param runModel	Laufzeitdaten
	 */
	public void build(final RunModel runModel) {
		/* Bedingung */
		final String condition=counterCondition.getCondition();
		if (condition.trim().isEmpty()) {
			this.condition=null;
		} else {
			this.condition=new ExpressionMultiEval(runModel.variableNames);
			this.condition.parse(condition);
		}

		/* Kundentypen */
		clientType=new boolean[runModel.clientTypes.length];
		final List<String> selectedClientTypes=counterCondition.getClientTypes();
		if (selectedClientTypes.size()==0 || selectedClientTypes.size()==runModel.clientTypes.length) {
			Arrays.fill(clientType,true);
		} else {
			Arrays.fill(clientType,false);
			for (String selectedClientType: selectedClientTypes) clientType[runModel.clientTypesMap.get(selectedClientType)]=true;
		}
	}

	/**
	 * Soll der aktuelle Kunde gezählt werden?
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Liefert <code>true</code>, wenn der Kunde gezählt werden soll
	 */
	public boolean isCountThisClient(final SimulationData simData, final RunDataClient client) {
		/* Bedingung */
		if (condition!=null) {
			simData.runData.setClientVariableValues(client);
			if (!condition.eval(simData.runData.variableValues,simData,client)) return false;
		}

		/* Kundentypen */
		return clientType[client.type];
	}
}
