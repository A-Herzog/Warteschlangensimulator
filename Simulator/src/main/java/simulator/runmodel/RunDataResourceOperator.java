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
package simulator.runmodel;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.distribution.tools.DistributionRandomNumber;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelSurface;

/**
 * Hält die Daten für einen einzelnen Minimal-Bediener vor
 * @author Alexander Herzog
 * @see RunDataResource
 */
public class RunDataResourceOperator {
	/** Name der Bedienergruppe */
	public final String name;
	/** Icon der Bedienergruppe */
	public final String icon;
	/** 0-basierender Index dieses Bedieners in der Bedienergruppe */
	public int index;

	private long workingStart;
	private int stationIDLast;
	private int stationID;

	private final AbstractRealDistribution moveDistribution;
	private final String moveExpression;
	private ExpressionCalc moveExpressionObj;
	private final ModelSurface.TimeBase moveTimeBase;

	/**
	 * Konstruktor der Klasse
	 * @param index	0-basierender Index dieses Bedieners in der Bedienergruppe
	 * @param name	Icon der Bedienergruppe
	 * @param icon	Name der Bedienergruppe
	 * @param moveDistribution	Verteilung mit Rüstzeiten beim Wechsel des Bedieners von einer Station an eine andere
	 * @param moveExpression	Rechenausdruck zur Bestimmung der Rüstzeiten beim Wechsel des Bedieners von einer Station an eine andere
	 * @param moveTimeBase	Zeitbasis für die Rüstzeiten beim Wechsel des Bedieners von einer Station an eine andere
	 */
	public RunDataResourceOperator(final int index, final String name, final String icon, final AbstractRealDistribution moveDistribution, final String moveExpression, final ModelSurface.TimeBase moveTimeBase) {
		this.index=index;
		this.name=name;
		this.icon=icon;
		this.moveDistribution=moveDistribution;
		this.moveExpression=moveExpression;
		this.moveTimeBase=moveTimeBase;
		stationID=-1;
		stationIDLast=-1;
	}

	/**
	 * Gibt an, ob der Bediener momentan an einer Station arbeitet.
	 * @return	Liefert <code>true</code>, wenn der Bediener momentan an einer Station arbeitet.
	 */
	public final boolean isWorking() {
		return stationID>=0;
	}

	/**
	 * Liefert die ID der Station, an der sich der Bediener befindet.<br>
	 * (-1, wenn der Bediener an keiner Station ist.)
	 * @return	ID der Station, an der sich der Bediener befindet
	 */
	public final int getStation() {
		return stationID;
	}

	/**
	 * Gibt an, ob der Bediener momentan an einer Station arbeitet oder verfügbar ist.
	 * @param resource	Bedienergruppe zu der dieser Bediener gehört
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn der Bediener momentan an einer Station arbeitet oder verfügbar ist.
	 */
	public boolean isAvailableOrWorking(final RunDataResource resource, final SimulationData simData) {
		return isWorking();
	}

	/**
	 * Liefert den Zeitpunkt, an dem der Bediener angefangen hat zu arbeiten.
	 * @return	Startzeitpunkt der Arbeit (in Millisekunden)
	 * @see #startWorking(SimulationData, int)
	 */
	public long getWorkingStartTime() {
		return workingStart;
	}

	/**
	 * Startet die Arbeit eines Bedieners an einer Station
	 * @param simData	Simulationsdatenobjekt
	 * @param station	Station an der der Bediener arbeiten soll
	 * @return	Evtl. zusätzlich notwendige Rüstzeit zum Stationswechsel (in Sekunden)
	 * @see #endWorking(RunDataResource, SimulationData)
	 */
	public final double startWorking(final SimulationData simData, final int station) {
		workingStart=simData.currentTime;
		stationID=station;

		/* Rüstzeit beim Stationswechsel bestimmen */
		double additionalTime=0;
		if (stationID!=stationIDLast && stationIDLast>=0) {
			if (moveDistribution!=null) {
				additionalTime=DistributionRandomNumber.randomNonNegative(moveDistribution);
			} else {
				if (moveExpression!=null) {
					if (moveExpressionObj==null) {
						moveExpressionObj=new ExpressionCalc(simData.runModel.variableNames);
						moveExpressionObj.parse(moveExpression);
					}
					simData.runData.setClientVariableValues(null);
					if (simData.runModel.stoppOnCalcError) {
						final Double D=moveExpressionObj.calc(simData.runData.variableValues,simData,null);
						if (D==null) simData.calculationErrorRessource(moveExpressionObj,name);
						additionalTime=(D==null)?0.0:D.doubleValue();
					} else {
						additionalTime=moveExpressionObj.calcOrDefault(simData.runData.variableValues,simData,null,0.0);
					}
				}
			}
		}
		additionalTime=additionalTime*moveTimeBase.multiply;

		stationIDLast=stationID;
		return additionalTime;
	}

	/**
	 * Beendet die Arbeit des Bedieners
	 * @param resource	Bedienergruppe zu der dieser Bediener gehört
	 * @param simData	Simulationsdatenobjekt
	 * @see #startWorking(SimulationData, int)
	 */
	public void endWorking(final RunDataResource resource, final SimulationData simData) {
		stationID=-1;
	}
}
