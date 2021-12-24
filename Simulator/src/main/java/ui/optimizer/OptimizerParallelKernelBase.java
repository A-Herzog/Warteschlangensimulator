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
package ui.optimizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import simulator.editmodel.EditModel;

/**
 * Diese Klasse stellt die Basis für alle parallel arbeitenden Optimierungskernel dar.
 * Sie stellt Basisfunktionen, die alle parallelen Kernel benötigen, zur Verfügung.
 * @author Alexander Herzog
 */
public abstract class OptimizerParallelKernelBase extends OptimizerKernelBase {
	/**
	 * Ermöglicht den Zugriff auf das Optimierer-Objekt, welches diesen Kernel verwendet
	 */
	protected final OptimizerBase optimizer;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Zu verwendende Optimierereinstellungen
	 * @param optimizer	Optimierer-Objekt, welches diesen Kernel verwendet
	 * @param startModel	Ausgangs-Editor-Modell
	 */
	public OptimizerParallelKernelBase(final OptimizerSetup setup, final OptimizerBase optimizer, final EditModel startModel) {
		super(setup,startModel);
		this.optimizer=optimizer;
	}

	/**
	 * Liefert die Werte der Kontrollvariablen für eines der aktuellen Modelle
	 * @param index	Index des Modells, für dass die Werte der Kontrollvariablen geliefert werden sollen
	 * @return	Werte der Kontrollvariablen
	 */
	public abstract double[] getControlVariablesForModel(final int index);

	/**
	 * Liefert die Anzahl an Modellen pro Runde.
	 * @return	Anzahl an Modellen pro Runde
	 */
	protected abstract int getModelCount();

	/**
	 * Liefer die Belegung der Kontrollvariablen für alle aktuellen Modelle
	 * @return	Belegung der Kontrollvariablen für alle aktuellen Modelle (Kopie für den Aufrufer)
	 */
	public double[][] getControlVariables() {
		final int count=getModelCount();
		final double[][] result=new double[count][];
		for (int i=0;i<count;i++) {
			final double[] input=getControlVariablesForModel(i);
			result[i]=Arrays.copyOf(input,input.length);
		}
		return result;
	}

	/**
	 * Erstellt die initialen Modelle
	 * @return	Modelle für die erste Runde
	 */
	protected abstract EditModel[] setupInitialModels();

	/**
	 * Erstellt die Modelle für die nächste Runde
	 * @param lastTargetValues	Werte der Zielgröße im letzten Optimierungsschritt
	 * @param bestModels	Indices der besten Modelle aus der vorherigen Runde
	 * @return	Modelle für die erste Runde
	 */
	protected abstract EditModel[] setupModels(final double[] lastTargetValues, final int[] bestModels);

	/**
	 * Bestimmt die evolutionäre Fitness der Modelle aus der aktuellen Generation
	 * @param lastTargetValues	Zielwerte der jeweiligen Modelle
	 * @param simulationWasEmergencyStopped	Wurden bestimmte Simulationen abgebrochen?
	 * @return	Liefert die Fitness der aktuellen Modelle
	 * @see #getBestModels(double[])
	 * @see #setupNextStep(double[], boolean[])
	 */
	private double[] getFitness(final double[] lastTargetValues, final boolean[] simulationWasEmergencyStopped) {
		final double[] fitness=new double[lastTargetValues.length];

		for (int i=0;i<lastTargetValues.length;i++) {
			if (simulationWasEmergencyStopped[i]) {fitness[i]=-Double.MAX_VALUE; continue;}

			switch (setup.targetDirection) {
			case -1:
				/* kleiner = besser */
				fitness[i]=-lastTargetValues[i];
				break;
			case 0:
				/* Bereich */
				fitness[i]=0;
				if (lastTargetValues[i]<setup.targetRangeMin) fitness[i]=lastTargetValues[i]-setup.targetRangeMin;
				if (lastTargetValues[i]>setup.targetRangeMax) fitness[i]=setup.targetRangeMax-lastTargetValues[i];
				break;
			case 1:
				/* größer = besser */
				fitness[i]=lastTargetValues[i];
				break;
			}
		}

		return fitness;
	}

	/**
	 * Liefert die in Bezug auf die Fitness besten Modelle der aktuellen Generation
	 * @param fitness	Fitnesswerte für die Modelle
	 * @return	Indices der besten Modelle
	 * @see #getFitness(double[], boolean[])
	 * @see #setupNextStep(double[], boolean[])
	 */
	private int[] getBestModels(final double[] fitness) {
		final List<Integer> best=new ArrayList<>();

		boolean ok=true;
		while (ok) {
			ok=false;
			double currentBestValue=-Double.MAX_VALUE;
			int currentBestIndex=-1;
			for (int i=0;i<fitness.length;i++) if (fitness[i]>currentBestValue) {
				currentBestValue=fitness[i];
				currentBestIndex=i;
				ok=true;
			}
			if (currentBestIndex<0) break;
			best.add(currentBestIndex);
			fitness[currentBestIndex]=-Double.MAX_VALUE;
		}

		final int[] result=new int[best.size()];
		for (int i=0;i<best.size();i++) result[i]=best.get(i);
		return result;
	}

	/**
	 * Erstellt das Modell für den nächsten Optimierungsschritt.<br>
	 * @param lastTargetValues	Wert der Zielgröße im letzten Optimierungsschritt
	 * @param simulationWasEmergencyStopped	Gibt an, ob die Simulation im letzten Optimierungsschritt durch den Simulator selbst abgebrochen wurde
	 * @return	Nächstes zu simulierendes Modell oder <code>null</code> wenn die Optimierung abgebrochen werden soll.
	 */
	public final EditModel[] setupNextStep(final double[] lastTargetValues, final boolean[] simulationWasEmergencyStopped) {
		clearMessages();

		if (lastTargetValues==null || lastTargetValues.length==0) return setupInitialModels();

		final double[] fitness=getFitness(lastTargetValues,simulationWasEmergencyStopped);
		final int[] bestModels=getBestModels(fitness);
		if (bestModels==null || bestModels.length==0) {
			addMessage(Language.tr("Optimizer.AllModelsBad"));
			return null;
		}

		return setupModels(lastTargetValues,bestModels);
	}
}
