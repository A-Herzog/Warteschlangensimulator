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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;
import parser.MathCalcError;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.DistributionSystem;
import ui.modeleditor.elements.ModelElementTransportDestination;
import ui.modeleditor.elements.TransportTimeRecord;

/**
 * Diese Klasse hält die Laufzeitmodelldaten für die Transportzeiten vor.
 * Sie wird von {@link RunElementTransportSource} verwendet.
 * @author Alexander Herzog
 * @see RunElementTransportSource
 */
public class RunElementTransportSourceTime {
	private double timeBaseMultiply;
	private AbstractRealDistribution[] distribution;

	/**
	 * Liste mit den Ausdrücken zur Bestimmung der Transportzeiten.<br>
	 * Ist auf jeden Fall nicht <code>null</code>, kann aber <code>null</code>-Einträge besitzen
	 */
	public String[] expression;

	/**
	 * Gibt an, wie die Transportzeit in der Statistik erfasst werden soll
	 * @see TransportTimeRecord.DelayType
	 */
	public TransportTimeRecord.DelayType delayType;

	private final ModelElement element;

	/**
	 * Konstruktor der Klasse
	 * @param element	Zu dem Transport gehörendes Editor-Transport-Start-Element (zum Auslesen der ID für mögliche Fehlermeldungen)
	 */
	public RunElementTransportSourceTime(final ModelElement element) {
		this.element=element;
	}

	/**
	 * Lädt die Laufzeitdaten aus einem {@link TransportTimeRecord}-Objekt
	 * @param editModel	Editor-Modell
	 * @param runModel	Laufzeit-Modell
	 * @param record	{@link TransportTimeRecord}-Objekt aus dem die Daten geladen werden sollen
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	public String build(final EditModel editModel, final RunModel runModel, final TransportTimeRecord record) {
		timeBaseMultiply=record.getTimeBase().multiply;
		delayType=record.getDelayType();

		final DistributionSystem distributionSystem=record.getTransportTime();
		final int max=(editModel.surface.getParentSurface()!=null)?editModel.surface.getParentSurface().getMaxId():editModel.surface.getMaxId();

		expression=new String[max+1];
		distribution=new AbstractRealDistribution[max+1];

		for (int i=0;i<expression.length;i++) {
			final ModelElement e=editModel.surface.getByIdIncludingSubModels(i);
			if (e==null || !(e instanceof ModelElementTransportDestination)) continue;
			final Object delay=distributionSystem.getOrDefault(e.getName());
			if (delay instanceof AbstractRealDistribution) {
				distribution[i]=DistributionTools.cloneDistribution((AbstractRealDistribution)delay);
			} else {
				if (!(delay instanceof String)) return String.format(Language.tr("Simulation.Creator.TransportSourceInternalError"),e.getId(),element.getId());
				final String expr=(String)delay;
				final int error=ExpressionCalc.check(expr,runModel.variableNames);
				if (error>=0) return String.format(Language.tr("Simulation.Creator.DelayCondition"),expr,element.getId(),error+1);
				this.expression[i]=expr;
			}
		}

		return null;
	}

	private static final double toSec=1.0/1000.0;

	/**
	 * Ermittelt eine Transportzeit
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Kunde, der transportiert werden soll
	 * @param delayExpression	Liste mit den Verzögerungsausdrücken vom Typ {@link ExpressionCalc} (Einträge können <code>null</code> sein, Liste sollte aber die Länge wie {@link RunElementTransportSourceTime#expression} haben)
	 * @param stationName	Name der zugehörigen Station
	 * @return	Transportzeit in Sekunden
	 */
	public double getTransportTime(final SimulationData simData, final RunDataClient client, final ExpressionCalc[] delayExpression, final String stationName) {
		double value;
		if (distribution[client.stationInformationInt]!=null) {
			value=DistributionRandomNumber.randomNonNegative(distribution[client.stationInformationInt]);
		} else {
			final double additionalWaitingTime=(simData.currentTime-client.lastWaitingStart)*toSec;
			simData.runData.setClientVariableValues(client,additionalWaitingTime);
			try {
				value=delayExpression[client.stationInformationInt].calc(simData.runData.variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorStation(delayExpression[client.stationInformationInt],stationName);
				value=0;
			}
		}
		return FastMath.max(0,value)*timeBaseMultiply;
	}
}
