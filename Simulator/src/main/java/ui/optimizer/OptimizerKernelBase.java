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
import java.util.List;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionMultiEval;
import ui.ModelChanger;
import ui.optimizer.OptimizerSetup.ControlVariable;

/**
 * Diese Klasse stellt die Basis f�r alle (sowohl serielle als auch parallel) Optimierungskernel dar.
 * Sie stellt lediglich einige Basisfunktionen, die alle Kernel ben�tigen, zur Verf�gung.
 * @author Alexander Herzog
 */
public class OptimizerKernelBase {
	/**
	 * Liste mit allen Nachrichten, die �ber {@link #addMessage(String)} erzeugt wurden.
	 * @see #addMessage(String)
	 * @see #getMessages()
	 */
	private final List<String> messages;

	/**
	 * Zu verwendende Optimierereinstellungen
	 */
	protected final OptimizerSetup setup;

	/**
	 * Ausgangs-Editor-Modell
	 */
	protected final EditModel startModel;

	/**
	 * Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 */
	protected final String editModelPath;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Zu verwendende Optimierereinstellungen
	 * @param startModel	Ausgangs-Editor-Modell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 */
	public OptimizerKernelBase(final OptimizerSetup setup, final EditModel startModel, final String editModelPath) {
		this.setup=setup;
		this.startModel=startModel;
		this.editModelPath=editModelPath;
		messages=new ArrayList<>();
	}

	/**
	 * F�gt eine Zeile zu der Liste der Nachrichten, die �ber <code>getMessages</code>
	 * abgerufen werden k�nnen, hinzu.
	 * @param line	Neue Ausgabezeile
	 * @see #getMessages()
	 */
	protected final void addMessage(final String line) {
		messages.add(line);
	}

	/**
	 * L�scht die Liste der momentan gespeicherten Nachrichten,
	 * die �ber <code>getMessages</code> abgerufen werden k�nnen.
	 */
	protected final void clearMessages() {
		messages.clear();
	}

	/**
	 * Liefert eine Liste mit allen Nachrichten, die intern �ber {@link #addMessage(String)} erzeugt wurden.
	 * @return	Liste mit allen momentanen Nachrichten
	 * @see #addMessage(String)
	 */
	public final String[] getMessages() {
		return messages.toArray(new String[0]);
	}

	/**
	 * Liefert die initialen Werte f�r die Kontrollvariablen gem�� Optimierungs-Setup
	 * @return	Initiale Werte f�r die Kontrollvariablen
	 */
	protected final double[] getInitialControlVariables() {
		final double[] controlValues=new double[setup.controlVariables.size()];
		for (int i=0;i<setup.controlVariables.size();i++) {
			final ControlVariable controlVariable=setup.controlVariables.get(i);
			controlValues[i]=controlVariable.start;
			if (controlVariable.integerValue || controlVariable.mode==ModelChanger.Mode.MODE_RESOURCE) controlValues[i]=Math.round(controlValues[i]);
		}
		return controlValues;
	}

	/**
	 * Liefert ein vom Ausgangsmodell abgeleitetes Modell, bei dem alle Kontrollvariablen
	 * auf die vorgegebenen Werte eingestellt sind.
	 * @param controlValues	Werte, auf die die Kontrollvariablen eingestellt werden sollen
	 * @return	Angepasstes Modell oder ein String mit einer Fehlermeldung
	 */
	protected final Object generateModel(final double[] controlValues) {
		EditModel model=startModel;
		for (int i=0;i<setup.controlVariables.size();i++) {
			final ControlVariable controlVariable=setup.controlVariables.get(i);
			final Object obj=ModelChanger.changeModel(model,controlVariable.mode,controlVariable.tag,controlVariable.xmlMode,controlValues[i]);
			if (obj instanceof String) return obj;
			model=(EditModel)obj;
		}
		return model;
	}

	/**
	 * Liefert die Variablennamen f�r die Kontrollvariablen
	 * (zur Verwendung in den Nebenbedingungen-Ausdr�cken).
	 * @param count	Gesamtzahl an Kontrollvariablen
	 * @return	Variablennamen f�r die Kontrollvariablen
	 */
	private String[] getVariableNames(final int count) {
		final List<String> list=new ArrayList<>();
		for (int i=1;i<=count;i++) list.add(String.format("Var%d",i));
		return list.toArray(new String[0]);
	}

	/**
	 * Rechenausdr�cke f�r die Nebenbedingungen<br>
	 * (Kann <code>null</code> sein, wenn es keine Nebenbedingungen gibt.)
	 * @see #initControlValuesConditions()
	 * @see #controlValuesValide(double[])
	 */
	private ExpressionMultiEval[] controlValuesConditions;

	/**
	 * Pr�ft und initialisiert die Nebenbedingungen.
	 * @return	Gibt im Erfolgsfall <code>null</code> zur�ck, sonst eine Fehlermeldung.
	 */
	public String initControlValuesConditions() {
		if (setup.controlVariableConstrains.size()==0) {
			controlValuesConditions=null;
			return null;
		}

		final String[] varNames=getVariableNames(setup.controlVariables.size());

		controlValuesConditions=new ExpressionMultiEval[setup.controlVariableConstrains.size()];
		for (int i=0;i<setup.controlVariableConstrains.size();i++) {
			controlValuesConditions[i]=new ExpressionMultiEval(varNames,startModel.userFunctions);
			final String condition=setup.controlVariableConstrains.get(i);
			final int error=controlValuesConditions[i].parse(condition);
			if (error>=0) return String.format(Language.tr("Optimizer.Error.ControlValueCondition"),i+1,condition,error+1);
		}

		return null;
	}

	/**
	 * Pr�ft, ob ein Satz von Kontrollvariablenwerten die Nebenbedingungen erf�llt
	 * @param controlValues	Auf G�ltigkeit zu pr�fender Satz von Kontrollvariablenwerten
	 * @return	Gibt <code>true</code> zur�ck, wenn alle Nebenbedingungen erf�llt sind
	 */
	protected final boolean controlValuesValide(final double[] controlValues) {
		if (controlValuesConditions==null) return true;
		for (ExpressionMultiEval eval: controlValuesConditions) if (!eval.eval(controlValues,null,null)) return false;
		return true;
	}
}
