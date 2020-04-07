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
package ui.modeleditor.elements;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import simulator.editmodel.EditModel;
import simulator.elements.RunElementAnimationConnect;
import simulator.elements.RunModelAnimationViewer;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Wird dieses Dummy-Element zu einem Surface hinzugef�gt, so wird das zugeh�rige
 * <code>RunElementAnimationConnect</code>-Element zum Laufzeitmodell hinzugef�gt, welches
 * die Verbindung zur Animationsoberfl�che beinhaltet.
 * @author Alexander Herzog
 * @see RunElementAnimationConnect
 */
public class ModelElementAnimationConnect extends ModelElementBox {
	/**
	 * Dieses Feld muss auf ein {@link RunModelAnimationViewer}-Interface gesetzt
	 * werden, welches dann w�hrend der Animation des Modells
	 * �ber das zugeh�rige {@link RunElementAnimationConnect}-Element �ber
	 * Ver�nderungen am System benachrichtigt wird.
	 */
	public RunModelAnimationViewer animationViewer;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationConnect</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnimationConnect(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
	}

	@Override
	public void addDataToXML(final Document doc, final Element parent) {
	}

	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationConnect)) return false;
		return true;
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationConnect clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationConnect element=new ModelElementAnimationConnect(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
	}

	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		return false;
	}
}
