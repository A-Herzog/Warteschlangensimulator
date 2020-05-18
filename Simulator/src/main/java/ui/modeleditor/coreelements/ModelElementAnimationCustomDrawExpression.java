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
package ui.modeleditor.coreelements;

import java.awt.Dimension;
import java.util.Objects;

import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelSurface;

/**
 * Basisklasse für ein Animationselement, welches sich selber zeichnet
 * und dafür einen Rechenausdruck verwendet.
 * @author Alexander Herzog
 */
public abstract class ModelElementAnimationCustomDrawExpression extends ModelElementAnimationCustomDraw {
	private ExpressionCalc drawExpression;
	private String expression;

	/**
	 * Läuft gerade eine Animation (<code>true</code>) oder befinden wir und im
	 * Editor-Modus (<code>false</code>) in dem ggf. Dummy-Werte angezeigt werden sollen?
	 */
	protected boolean animationRunning=false;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param size	Größe der Box
	 */
	public ModelElementAnimationCustomDrawExpression(final EditModel model, final ModelSurface surface, final Dimension size) {
		super(model,surface,size);
		expression="";
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationCustomDrawExpression)) return false;
		final ModelElementAnimationCustomDrawExpression other=(ModelElementAnimationCustomDrawExpression)element;

		if (!Objects.equals(expression,other.expression)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationCustomDrawExpression) {
			final ModelElementAnimationCustomDrawExpression source=(ModelElementAnimationCustomDrawExpression)element;
			expression=source.expression;
		}
	}

	/**
	 * Liefert den Ausdruck dessen Berechnungsergebnis angezeigt werden soll.
	 * @return	Ausdruck dessen Berechnungsergebnis angezeigt werden soll
	 */
	public String getExpression() {
		return (expression==null)?"":expression;
	}

	/**
	 * Stellt den Ausdruck dessen Berechnungsergebnis angezeigt werden soll ein.
	 * @param expression	Ausdruck dessen Berechnungsergebnis angezeigt werden sollen
	 */
	public void setExpression(final String expression) {
		this.expression=(expression==null)?"":expression;
	}

	/**
	 * Liefert das Berechnungsergebnis des Rechenausdrucks
	 * @param simData	Simulationsdatenobjekt
	 * @return	Rechenergebnis
	 */
	protected final Double getDrawExpression(final SimulationData simData) {
		if (drawExpression==null) return null;
		simData.runData.setClientVariableValues(null);
		try {
			return NumberTools.fastBoxedValue(drawExpression.calc(simData.runData.variableValues,simData,null));
		} catch (MathCalcError e) {
			return null;
		}
	}

	@Override
	public void initAnimation(final SimulationData simData) {
		animationRunning=true;
		drawExpression=new ExpressionCalc(simData.runModel.variableNames);
		if (drawExpression.parse(expression)>=0) drawExpression=null;
	}

	@Override
	protected void updateDrawData(final SimulationData simData) {
		setAnimationDouble(getDrawExpression(simData));
	}
}
