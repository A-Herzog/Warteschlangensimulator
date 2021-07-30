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

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.elements.AnimationExpression;

/**
 * Basisklasse f�r ein Animationselement, welches sich selber zeichnet
 * und daf�r einen Rechenausdruck verwendet.
 * @author Alexander Herzog
 */
public abstract class ModelElementAnimationCustomDrawExpression extends ModelElementAnimationCustomDraw {
	/** Ausdruck dessen Berechnungsergebnis angezeigt werden soll */
	protected final AnimationExpression expression=new AnimationExpression();

	/**
	 * L�uft gerade eine Animation (<code>true</code>) oder befinden wir und im
	 * Editor-Modus (<code>false</code>) in dem ggf. Dummy-Werte angezeigt werden sollen?
	 */
	protected boolean animationRunning=false;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param size	Gr��e der Box
	 */
	public ModelElementAnimationCustomDrawExpression(final EditModel model, final ModelSurface surface, final Dimension size) {
		super(model,surface,size);
		expression.setExpression("");
	}

	@Override
	public boolean isVisualOnly() {
		return true;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationCustomDrawExpression)) return false;
		final ModelElementAnimationCustomDrawExpression other=(ModelElementAnimationCustomDrawExpression)element;

		if (!expression.equalsAnimationExpression(other.expression)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationCustomDrawExpression) {
			final ModelElementAnimationCustomDrawExpression source=(ModelElementAnimationCustomDrawExpression)element;
			expression.copyFrom(source.expression);
		}
	}

	/**
	 * Liefert den Ausdruck dessen Berechnungsergebnis angezeigt werden soll.
	 * @return	Ausdruck dessen Berechnungsergebnis angezeigt werden soll
	 */
	public AnimationExpression getExpression() {
		return expression;
	}

	/**
	 * Liefert das Berechnungsergebnis des Rechenausdrucks
	 * @param simData	Simulationsdatenobjekt
	 * @return	Rechenergebnis
	 */
	protected final double getDrawExpression(final SimulationData simData) {
		return expression.getAnimationValue(this,simData);
	}

	@Override
	public void initAnimation(final SimulationData simData) {
		animationRunning=true;
		expression.initAnimation(this,simData);
	}

	@Override
	protected void updateDrawData(final SimulationData simData) {
		setAnimationDouble(NumberTools.fastBoxedValue(getDrawExpression(simData)));
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		if (expression.getMode()==AnimationExpression.ExpressionMode.Expression) {
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.Expression"),expression.getExpression(),newExpression->expression.setExpression(newExpression));
		}
	}
}
