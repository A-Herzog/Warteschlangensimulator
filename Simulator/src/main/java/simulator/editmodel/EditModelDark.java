/**
 * Copyright 2021 Alexander Herzog
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
package simulator.editmodel;

import java.awt.Color;
import java.util.function.Consumer;

import simulator.examples.EditModelExamples;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnimationBar;
import ui.modeleditor.elements.ModelElementAnimationBarChart;
import ui.modeleditor.elements.ModelElementAnimationBarStack;
import ui.modeleditor.elements.ModelElementAnimationClock;
import ui.modeleditor.elements.ModelElementAnimationImage;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementAnimationPieChart;
import ui.modeleditor.elements.ModelElementAnimationRecord;
import ui.modeleditor.elements.ModelElementAnimationTextSelect;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementAnimationTextValueJS;
import ui.modeleditor.elements.ModelElementEllipse;
import ui.modeleditor.elements.ModelElementImage;
import ui.modeleditor.elements.ModelElementLine;
import ui.modeleditor.elements.ModelElementRectangle;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementText;
import ui.tools.FlatLaFHelper;

/**
 * Verändert ein Modell, damit Texte und Co. im dunklen Modus
 * farblich passend aussehen.
 * @author Alexander Herzog
 * @see FlatLaFHelper
 * @see EditModelExamples
 */
public class EditModelDark {
	/**
	 * Farbmodus
	 */
	public enum ColorMode {
		/**
		 * Farbmodus: hell
		 */
		LIGHT,

		/**
		 * Farbmodus: dunkel
		 */
		DARK
	}

	/**
	 * Zu verarbeitendes Modell
	 */
	private final EditModel model;

	/**
	 * Konstruktor der Klasse
	 * @param model	Zu verarbeitendes Modell
	 */
	public EditModelDark(final EditModel model) {
		this.model=model;
	}

	/**
	 * Verarbeitet ein Modell.
	 * @param model	Zu verarbeitendes Modell
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 */
	public static void processModel(final EditModel model, final ColorMode modeFrom, final ColorMode modeTo) {
		final EditModelDark processcor=new EditModelDark(model);
		processcor.process(modeFrom,modeTo);
	}

	/**
	 * Verarbeitet das Modell.
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 */
	public void process(final ColorMode modeFrom, final ColorMode modeTo) {
		if (model==null || modeFrom==null || modeTo==null || modeFrom==modeTo) return;
		processSurface(model.surface,modeFrom,modeTo);
	}

	/**
	 * Verarbeitet die Stationen auf einer Zeichenfläche.
	 * @param surface	Zeichenfläche
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 */
	private void processSurface(final ModelSurface surface, final ColorMode modeFrom, final ColorMode modeTo) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) {
				processSurface(((ModelElementSub)element).getSubSurface(),modeFrom,modeTo);
			} else {
				processElement(element,modeFrom,modeTo);
			}
		}
	}

	/**
	 * Verarbeitet die Farbdaten eines Elements.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 */
	private void processElement(final ModelElement element, final ColorMode modeFrom, final ColorMode modeTo) {
		/* Animation */
		if (element instanceof ModelElementAnimationTextValue) processAnimationTextValue((ModelElementAnimationTextValue)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationTextValueJS) processAnimationTextValueJS((ModelElementAnimationTextValueJS)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationTextSelect) processAnimationTextSelect((ModelElementAnimationTextSelect)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationBar) processAnimationBar((ModelElementAnimationBar)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationBarStack) processAnimationBarStack((ModelElementAnimationBarStack)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationLineDiagram) processAnimationLineDiagram((ModelElementAnimationLineDiagram)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationBarChart) processAnimationBarChart((ModelElementAnimationBarChart)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationPieChart) processAnimationPieChart((ModelElementAnimationPieChart)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationRecord) processAnimationAnimationRecord((ModelElementAnimationRecord)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationClock) processAnimationClock((ModelElementAnimationClock)element,modeFrom,modeTo);
		if (element instanceof ModelElementAnimationImage) processAnimationImage((ModelElementAnimationImage)element,modeFrom,modeTo);

		/* Optische Gestaltung */
		if (element instanceof ModelElementText) processText((ModelElementText)element,modeFrom,modeTo);
		if (element instanceof ModelElementLine) processLine((ModelElementLine)element,modeFrom,modeTo);
		if (element instanceof ModelElementRectangle) processRectangle((ModelElementRectangle)element,modeFrom,modeTo);
		if (element instanceof ModelElementEllipse) processEllipse((ModelElementEllipse)element,modeFrom,modeTo);
		if (element instanceof ModelElementImage) processImage((ModelElementImage)element,modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationTextValue}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationTextValue(final ModelElementAnimationTextValue element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationTextValueJS}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationTextValueJS(final ModelElementAnimationTextValueJS element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationTextSelect}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationTextSelect(final ModelElementAnimationTextSelect element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationBar}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationBar(final ModelElementAnimationBar element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getBorderColor(),c->element.setBorderColor(c),modeFrom,modeTo);
		processColor(element.getBackgroundColor(),c->element.setBackgroundColor(c),modeFrom,modeTo);
		processColor(element.getGradientFillColor(),c->element.setGradientFillColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationBarStack}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationBarStack(final ModelElementAnimationBarStack element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getBorderColor(),c->element.setBorderColor(c),modeFrom,modeTo);
		processColor(element.getBackgroundColor(),c->element.setBackgroundColor(c),modeFrom,modeTo);
		processColor(element.getGradientFillColor(),c->element.setGradientFillColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationLineDiagram}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationLineDiagram(final ModelElementAnimationLineDiagram element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getBorderColor(),c->element.setBorderColor(c),modeFrom,modeTo);
		processColor(element.getBackgroundColor(),c->element.setBackgroundColor(c),modeFrom,modeTo);
		processColor(element.getGradientFillColor(),c->element.setGradientFillColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationBarChart}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationBarChart(final ModelElementAnimationBarChart element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getBorderColor(),c->element.setBorderColor(c),modeFrom,modeTo);
		processColor(element.getBackgroundColor(),c->element.setBackgroundColor(c),modeFrom,modeTo);
		processColor(element.getGradientFillColor(),c->element.setGradientFillColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationPieChart}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationPieChart(final ModelElementAnimationPieChart element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getBorderColor(),c->element.setBorderColor(c),modeFrom,modeTo);
		processColor(element.getBackgroundColor(),c->element.setBackgroundColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationRecord}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationAnimationRecord(final ModelElementAnimationRecord element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getBorderColor(),c->element.setBorderColor(c),modeFrom,modeTo);
		processColor(element.getBackgroundColor(),c->element.setBackgroundColor(c),modeFrom,modeTo);
		processColor(element.getGradientFillColor(),c->element.setGradientFillColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationClock}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationClock(final ModelElementAnimationClock element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementAnimationImage}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processAnimationImage(final ModelElementAnimationImage element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getBorderColor(),c->element.setBorderColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementText}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processText(final ModelElementText element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementLine}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processLine(final ModelElementLine element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementRectangle}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processRectangle(final ModelElementRectangle element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
		processColor(element.getFillColor(),c->element.setFillColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementEllipse}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processEllipse(final ModelElementEllipse element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
		processColor(element.getFillColor(),c->element.setFillColor(c),modeFrom,modeTo);
	}

	/**
	 * Verarbeitet ein {@link ModelElementImage}-Element.
	 * @param element	Zu verarbeitendes Element
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @see #processElement(ModelElement, ColorMode, ColorMode)
	 */
	private void processImage(final ModelElementImage element, final ColorMode modeFrom, final ColorMode modeTo) {
		processColor(element.getColor(),c->element.setColor(c),modeFrom,modeTo);
	}

	/**
	 * Prüft, ob eine Farbe umgewandelt werden muss.
	 * @param color	Ausgangsfarbe
	 * @param setColor	Callback zum setzen einer neuen Farbe
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 */
	private void processColor(final Color color, final Consumer<Color> setColor, final ColorMode modeFrom, final ColorMode modeTo) {
		final Color newColor=processColor(color,modeFrom,modeTo);
		if (newColor!=null) setColor.accept(newColor);
	}

	/**
	 * Farbe: sehr helles Grau
	 * @see #processColor(Color, ColorMode, ColorMode)
	 */
	private static final Color VERY_LIGHT_GRAY=new Color(240,240,240);

	/**
	 * Untere Farbe für Diagramm-Hintergrund-Farbverläufe (im hellen Modus)
	 * @see #processColor(Color, ColorMode, ColorMode)
	 */
	private static final Color GRIADIENT_LOWER=Color.WHITE;

	/**
	 * Obere Farbe für Diagramm-Hintergrund-Farbverläufe (im hellen Modus)
	 * @see #processColor(Color, ColorMode, ColorMode)
	 */
	private static final Color GRIADIENT_UPPER=new Color(230,230,250);

	/**
	 * Prüft, ob eine Farbe umgewandelt werden muss.
	 * @param color	Ausgangsfarbe
	 * @param modeFrom	Ausgangs-Farbmodus
	 * @param modeTo	Ziel-Farbmodus
	 * @return	Neue Farbe oder <code>null</code>, wenn keine Umwandlung notwendig ist
	 */
	private Color processColor(final Color color, final ColorMode modeFrom, final ColorMode modeTo) {
		if (color==null) return null;

		if (modeFrom==ColorMode.LIGHT && modeTo==ColorMode.DARK) {
			if (color.equals(Color.BLUE)) return new Color(72,209,204);
			if (color.equals(Color.BLACK)) return new Color(195,195,195);
			if (color.equals(VERY_LIGHT_GRAY)) return new Color(135,135,135);

			if (color.equals(GRIADIENT_LOWER)) return Color.DARK_GRAY;
			if (color.equals(GRIADIENT_UPPER)) return Color.GRAY;
		}

		return null;
	}
}
