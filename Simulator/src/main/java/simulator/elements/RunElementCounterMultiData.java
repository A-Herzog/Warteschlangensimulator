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

import java.util.ArrayList;
import java.util.List;

import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.coreelements.RunElementDataWithMultiValues;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionMultiEval;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementCounterMulti</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementCounter
 * @see RunElementData
 */
public class RunElementCounterMultiData extends RunElementData implements RunElementDataWithMultiValues {
	/**
	 * Zugeh�riges Statistikobjekt
	 */
	private final StatisticsMultiPerformanceIndicator counterStatistic;

	/**
	 * Bedingungen f�r die Mehrfachz�hler
	 */
	public final ExpressionMultiEval[] conditions;

	/**
	 * Statistikobjekte der Mehrfachz�hler
	 */
	public final StatisticsSimpleCountPerformanceIndicator[] statistic;

	/**
	 * Statistikobjekt des Mehrfachz�hlers, der verwendet werden soll, wenn keine der Bedingungen zutrifft
	 */
	public final StatisticsSimpleCountPerformanceIndicator statisticElse;

	/**
	 * Name der Z�hlergruppe in ggf. f�r die Statistik angepassten Schreibweise
	 */
	private final String groupName;

	/**
	 * Ist der Gruppenname leer?<br>
	 * Dies beschleunigt {@link #getValue(int, boolean)}
	 */
	private final boolean groupNameIsEmpty;

	/**
	 * Name der Z�hlergruppe mit angeh�ngtem "-", so dass bei der Z�hlung
	 * alle passenden Z�hler in der Gruppe in der Statistik leichter
	 * bzw. speichersparsamer gefunden werden k�nnen
	 */
	private final String groupNameExt;


	/**
	 * Statistikobjekte f�r die Z�hler in der Gruppe<br>
	 * (Ist notwendig, um in {@link #getValue(int, boolean)} auch einen Anteil liefern zu k�nnen.)
	 */
	private StatisticsSimpleCountPerformanceIndicator[] indicators;

	/**
	 * Entspricht dieser Wert noch der Anzahl an Eintr�gen in {@link #counterStatistic},
	 * so kann {@link #indicators} in {@link #getValue(int, boolean)} direkt weiterverwendet
	 * werden, ansonsten muss es neu aufgebaut werden.
	 */
	private int indicatorsSize;


	/**
	 * Konstruktor der Klasse <code>RunElementCounterData</code>
	 * @param station	Station zu diesem Datenelement
	 * @param groupName	Name der Z�hlergruppe f�r die einzelnen Z�hler
	 * @param conditions	Bedingungen zur Aktivierung der jeweiligen Z�hler
	 * @param counterNames	Namen der Z�hler
	 * @param counterNameElse	Name des Z�hlers f�r den Fall, dass keine Bedingung erf�llt ist
	 * @param counterStatistic	Statistikobjekt welches alle Z�hler enth�lt
	 * @param variableNames	Liste der global verf�gbaren Variablennamen
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementCounterMultiData(final RunElement station, final String groupName, final String[] conditions, final String[] counterNames, final String counterNameElse, final StatisticsMultiPerformanceIndicator counterStatistic, final String[] variableNames, final SimulationData simData) {
		super(station,simData);

		this.counterStatistic=counterStatistic;

		this.groupName=groupName.replace('-','_');
		groupNameExt=this.groupName+"-";
		groupNameIsEmpty=this.groupName.isEmpty();

		this.conditions=new ExpressionMultiEval[conditions.length];
		statistic=new StatisticsSimpleCountPerformanceIndicator[conditions.length];

		for (int i=0;i<conditions.length;i++) {
			this.conditions[i]=new ExpressionMultiEval(variableNames,simData.runModel.modelUserFunctions);
			this.conditions[i].parse(conditions[i]);
			statistic[i]=(StatisticsSimpleCountPerformanceIndicator)counterStatistic.get(groupNameExt+counterNames[i]);
		}
		statisticElse=(StatisticsSimpleCountPerformanceIndicator)counterStatistic.get(groupNameExt+counterNameElse);
	}

	@Override
	public int getValueCount() {
		return statistic.length+1; /* "+1" f�r "statisticElse" */
	}

	@Override
	public double getValue(int index, boolean fullValue) {
		/* Ung�ltiger Index */
		if (index<0 || index>statistic.length) return 0;

		final StatisticsSimpleCountPerformanceIndicator stat=(index==statistic.length)?statisticElse:statistic[index];

		/* Aktueller Wert */
		if (fullValue) return stat.get();

		/* Anteil */
		if (groupNameIsEmpty) return 1.0; /* Kein Gruppenname, keine Summe */

		/* Liste der Statistikdaten f�r diese Gruppe */
		if (indicators==null || indicatorsSize!=counterStatistic.size()) {
			final List<StatisticsSimpleCountPerformanceIndicator> list=new ArrayList<>();
			for (String name: counterStatistic.getNames()) {
				if (name.startsWith(groupNameExt)) list.add((StatisticsSimpleCountPerformanceIndicator)counterStatistic.get(name));
			}
			indicators=list.toArray(StatisticsSimpleCountPerformanceIndicator[]::new);
			indicatorsSize=counterStatistic.size();
		}

		/* Summe berechnen */
		long sum=0;
		for (StatisticsSimpleCountPerformanceIndicator indicator: indicators) sum+=indicator.get();

		if (sum>0) {
			return ((double)stat.get())/sum;
		} else {
			return 0.0;
		}
	}
}
