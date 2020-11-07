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

import language.Language;
import simcore.SimData;
import ui.modeleditor.ModelSurface;

/**
 * Hält die Daten für einen einzelnen Bediener vor
 * @author Alexander Herzog
 * @see RunDataResource
 */
public class RunDataResourceOperatorFull extends RunDataResourceOperator {
	/**
	 * Ausfalldatensätze für den Bediener
	 */
	private final RunDataResourceFailure[] failures;

	/**
	 * Wann startete die Verfügbarkeitszeit der Ressource (für Anwesenheitszeit-bedingte Ausfälle) (Millisekundenwert)
	 */
	long availableStartTime;

	/**
	 * Wenn sich der Bediener in Pausezeit befindet, gemäß welchem Pause-Datensatz
	 */
	int currentPauseIndex;

	/**
	 * Wann wird der Bediener wieder verfügbar sein?  (Millisekundenwert)
	 */
	long onlineAgainAt;

	/**
	 * Konstruktor der Klasse
	 * @param index	Index des Bedieners in seiner Gruppe
	 * @param name	Name der Bedienergruppe
	 * @param icon	Icon der Bedienergruppe
	 * @param moveDistribution	Verteilung mit Rüstzeiten beim Wechsel des Bedieners von einer Station an eine andere
	 * @param moveExpression	Rechenausdruck zur Bestimmung der Rüstzeiten beim Wechsel des Bedieners von einer Station an eine andere
	 * @param moveTimeBase	Zeitbasis für die Rüstzeiten beim Wechsel des Bedieners von einer Station an eine andere
	 * @param simData	Simulationsdatenobjekt
	 * @param failuresGlobal	Liste mit allen Pausenzeiten-Datensätzen
	 */
	public RunDataResourceOperatorFull(final int index, final String name, final String icon, final AbstractRealDistribution moveDistribution, final String moveExpression, final ModelSurface.TimeBase moveTimeBase, final SimulationData simData, final RunDataResourceFailure[] failuresGlobal) {
		super(index,name,icon,moveDistribution,moveExpression,moveTimeBase);

		failures=new RunDataResourceFailure[failuresGlobal.length];
		for (int i=0;i<failures.length;i++) {
			failures[i]=new RunDataResourceFailure(failuresGlobal[i],name,simData.runModel.variableNames);
			failures[i].scheduleDownTime(simData,simData.currentTime,name);
		}
	}

	/**
	 * Prüft, ob der aktuelle Bediener gemäß eines Pausenzeiten-Ausfalldatensatzes
	 * in eine Pause geschickt werden muss und bedingt diese ggf. auch gleich.
	 * @param resource	Gruppe zu der dieser Bediener gehört
	 * @param simData	Simulationsdatenobjekt
	 * @return	Liefert <code>true</code>, wenn der Bediener in eine Pausenzeit geschickt wurde
	 */
	public boolean testStartPause(final RunDataResource resource, final SimulationData simData) {
		for (int i=0;i<failures.length;i++) {
			final RunDataResourceFailure failure=failures[i];
			if (failure.testStartPause(simData,resource,this,i)) return true;
		}
		return false;
	}

	@Override
	public boolean isAvailableOrWorking(final RunDataResource resource, final SimulationData simData) {
		if (isWorking()) return true;

		/* Bediener in Pause */
		if (onlineAgainAt>0) {
			if (simData.currentTime<onlineAgainAt) return false;
			/* Pause zu Ende */
			resource.endDownTime(onlineAgainAt);
			if (simData.loggingActive && simData.logInfoSystem) simData.logEventExecution(Language.tr("Simulation.Log.ResourceFailure"),-1,String.format(Language.tr("Simulation.Log.ResourceFailure.EndOfDownTime"),name,index+1,SimData.formatSimTime(onlineAgainAt)));
			availableStartTime=onlineAgainAt;
			onlineAgainAt=0;
			failures[currentPauseIndex].scheduleDownTime(simData,availableStartTime,name);
		}

		/* Nächste Pause ? */
		if (failures.length==0) return true;
		return !testStartPause(resource,simData);
	}


	@Override
	public void endWorking(final RunDataResource resource, final SimulationData simData) {
		super.endWorking(resource,simData);

		final long delta=simData.currentTime-getWorkingStartTime();
		for (RunDataResourceFailure failure: failures) failure.countServed(delta);

		if (failures.length>0) testStartPause(resource,simData);
	}
}
