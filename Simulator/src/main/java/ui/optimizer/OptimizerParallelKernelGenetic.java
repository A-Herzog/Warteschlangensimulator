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
import ui.ModelChanger;
import ui.optimizer.OptimizerSetup.ControlVariable;

/**
 * F�hrt eine Optimierung mit Hilfe von genetischen Algorithmen durch.<br>
 * Dies ist nur ein Kernel, die weitere Verarbeitung wird �ber {@link OptimizerParallelGenetic} durchgef�hrt.
 * @author Alexander Herzog
 * @see OptimizerParallelGenetic
 */
public class OptimizerParallelKernelGenetic extends OptimizerParallelKernelBase {
	/** Maximaler Ver�nderungsfaktor der Werte pro Runde */
	private final double[] maxDeltaFactor;
	/** Populationsgr��e */
	private final int populationSize;
	/** Wie viele Modelle sollen jeweils als Elterngeneration �bernommen werden? */
	private final int bestModelsCount;
	/** Werte der Kontrollvariablen f�r alle Modelle der aktuellen Runde */
	private double[][] controlValues;
	/** Optimierungsrunde (Z�hlung startet mit 1) */
	private int round;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Zu verwendende Optimierereinstellungen
	 * @param optimizer	Optimierer-Objekt, welches diesen Kernel verwendet
	 * @param startModel	Ausgangs-Editor-Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 */
	public OptimizerParallelKernelGenetic(final OptimizerSetup setup, final OptimizerBase optimizer, final EditModel startModel, final String editModelPath) {
		super(setup,optimizer,startModel,editModelPath);

		this.populationSize=Math.max(2,setup.geneticPopulationSize);
		double d=1-Math.min(0.95,Math.max(0.1,setup.geneticEvolutionPressure));
		bestModelsCount=Math.max(1,(int)Math.round(d*populationSize));

		maxDeltaFactor=new double[5];
		maxDeltaFactor[0]=Math.max(0.01,Math.min(1,setup.geneticChangeSpeed1));
		maxDeltaFactor[1]=Math.max(0.01,Math.min(maxDeltaFactor[0],setup.geneticChangeSpeed2));
		maxDeltaFactor[2]=Math.max(0.01,Math.min(maxDeltaFactor[1],setup.geneticChangeSpeed3));
		maxDeltaFactor[3]=Math.max(0.01,Math.min(maxDeltaFactor[2],setup.geneticChangeSpeed4));
		maxDeltaFactor[4]=Math.max(0.01,Math.min(maxDeltaFactor[3],setup.geneticChangeSpeed5));
	}

	/**
	 * Ver�ndert den Wert einer Kontrollvariable
	 * @param value	Bisheriger Wert
	 * @param index	Index der Kontrollvariable
	 * @return	Neuer Wert der Kontrollvariable
	 * @see #mutateControl(double[][])
	 */
	private double mutateIndex(double value, final int index) {
		final ControlVariable setup=this.setup.controlVariables.get(index);

		final double maxDelta=setup.rangeTo-setup.rangeFrom;
		final double delta=Math.max(0.6,maxDelta*maxDeltaFactor[Math.min(maxDeltaFactor.length-1,(round-1)/2)]);

		if (value==setup.rangeFrom) {
			/* nur nach oben */
			value+=delta*Math.random();

		} else {
			if (value==setup.rangeTo) {
				/* nur nach unten */
				value-=delta*Math.random();
			} else {
				/* beide Richtungen m�glich */
				if (Math.random()>=0.5) value+=delta*Math.random(); else value-=delta*Math.random();
			}
		}

		if (setup.integerValue || setup.mode==ModelChanger.Mode.MODE_RESOURCE) value=Math.round(value);
		if (value<setup.rangeFrom) value=setup.rangeFrom;
		if (value>setup.rangeTo) value=setup.rangeTo;

		return value;
	}

	/**
	 * Erstellt ein neues, ver�ndertes Set von Kontrollvariablen basierend auf den Kontrollvariablenwerten aller Modelle aus der Vorrunde
	 * @param initialControl	Kontrollvariablenwerte der Modelle der Vorrunde
	 * @return	Neues Kontrollvariablen-Set
	 */
	private double[] mutateControl(final double[][] initialControl) {
		final double[] control=new double[initialControl[0].length];

		for (int i=0;i<control.length;i++) {
			/* Elternwert w�hlen */
			final int index=(int)Math.round(Math.floor(Math.random()*initialControl.length));
			double d=initialControl[index][i];

			/* Ggf. zuf�llig mutieren */
			if (Math.random()>=1.0-1.0/control.length) d=mutateIndex(d,i);

			/* Wert speichern */
			control[i]=d;
		}

		return control;
	}

	/**
	 * Pr�ft, ob es sich bei einer Kontrollvariablenbelegung um ein neues Setup
	 * handelt oder ob diese Belegung in der Liste aller Kontrollvariablenbelegungen
	 * schon enthalten ist.
	 * @param control	Potentiell neue Kontrollvariablenbelegung
	 * @param controls	Liste aller schon vorhandenen Kontrollvariablenbelegungen
	 * @return	Liefert <code>true</code>, wenn es sich um eine neue Kontrollvariablenbelegung handelt
	 */
	private boolean controlIsNew(final double[] control, final List<List<Double>> controls) {
		for (List<Double> test: controls) {
			boolean isNew=false;
			for (int i=0;i<control.length;i++) if (control[i]!=test.get(i).doubleValue()) {isNew=true; break;}
			if (!isNew) return false;
		}
		return true;
	}

	/**
	 * �bertr�gt die Kontrollvariablenbelegungen aus {@link #setupModels(double[][])} nach {@link #controlValues}.
	 * @param controls	Neue Kontrollvariablenbelegungen
	 * @see #controlValues
	 * @see #setupModels(double[][])
	 */
	private void storeControlValues(List<List<Double>> controls) {
		controlValues=new double[controls.size()][];
		for (int i=0;i<controlValues.length;i++) {
			final List<Double> cList=controls.get(i);
			final double[] c=controlValues[i]=new double[cList.size()];
			for (int j=0;j<c.length;j++) c[j]=cList.get(j).doubleValue();
		}
	}

	/**
	 * Erstellt die Simulationsmodelle f�r die n�chste Runde
	 * @param initialControl	Kontrollvariablenwerte f�r die neuene Modelle
	 * @return	Neue Modelle
	 */
	private EditModel[] setupModels(final double[][] initialControl) {
		final List<EditModel> models=new ArrayList<>();
		final List<List<Double>> controls=new ArrayList<>();

		int rounds=0;

		while (models.size()<populationSize) {
			/* Variablen belegen */
			final double[] control=mutateControl(initialControl);

			/* Nebenbedingungen pr�fen */
			if (!controlValuesValide(control)) continue;

			/* Pr�fen, ob schon vorhanden */
			rounds++;
			if (!controlIsNew(control,controls)) {
				if (rounds>50) break; /* lange keine neuen Modelle mehr gefunden */
				continue;
			}
			rounds=0;

			/* Modell erstellen */
			final Object obj=generateModel(control);
			if (obj instanceof String) {
				addMessage((String)obj);
				return null;
			}
			models.add((EditModel)obj);
			final List<Double> controlList=new ArrayList<>();
			for (double c: control) controlList.add(c);
			controls.add(controlList);
		}

		storeControlValues(controls);
		return models.toArray(EditModel[]::new);
	}

	@Override
	protected EditModel[] setupInitialModels() {
		round=1;
		final double[][] initialControl=new double[][]{getInitialControlVariables()};
		return setupModels(initialControl);
	}

	@Override
	protected EditModel[] setupModels(final double[] lastTargetValues, final int[] bestModels) {
		/* Ergebnisse in Diagramm aufnehmen */
		final boolean[] usedForNextStep=new boolean[lastTargetValues.length];
		for (int i=0;i<usedForNextStep.length;i++) {
			usedForNextStep[i]=false;
			for (int j=0;j<Math.min(bestModels.length,bestModelsCount);j++) if (bestModels[j]==i) {usedForNextStep[i]=true; break;}
		}
		optimizer.addOptimizationRunResults(getControlVariables(),lastTargetValues,usedForNextStep);

		round++;
		final double[][] initialControl=new double[Math.min(bestModels.length,bestModelsCount)][];
		for (int i=0;i<initialControl.length;i++) initialControl[i]=Arrays.copyOf(controlValues[bestModels[i]],controlValues[bestModels[i]].length);

		final StringBuilder sb=new StringBuilder();
		for (int i=0;i<initialControl.length;i++) {
			if (sb.length()>0) sb.append(", ");
			sb.append(bestModels[i]+1);
		}
		addMessage(Language.tr("Optimizer.SelectedParentsForNextRound")+" "+sb.toString());

		return setupModels(initialControl);
	}

	@Override
	protected int getModelCount() {
		return controlValues.length;
	}

	@Override
	public double[] getControlVariablesForModel(int index) {
		return controlValues[index];
	}
}