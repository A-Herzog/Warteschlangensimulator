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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import language.Language;
import systemtools.BaseDialog;
import systemtools.LabeledColorChooserButton;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dieser Dialog erlaubt das Bearbeiten eines Ausdrucks und einer Farbe für einen
 * Teildatensatz von {@link ModelElementAnimationBarStack}. Der Dialog wird
 * von {@link BarStackTableModel} aufgerufen.
 * @author Alexander Herzog
 * @see ModelElementAnimationBarStack
 * @see BarStackTableModel
 */
public class BarStackTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -110037828381567665L;

	/**
	 * Ausdruck, der in {@link #editExpression} bearbeitet wird.
	 * @see #editExpression
	 * @see #getExpression()
	 */
	private final AnimationExpression expression;

	/**
	 * Zu bearbeitender Rechenausdruck für das Balkensegment
	 * @see #getExpression()
	 */
	private final AnimationExpressionPanel editExpression;

	/**
	 * Auswahl der Farbe für das Balkensegment
	 * @see #getColor()
	 */
	private final LabeledColorChooserButton colorChooserBar;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param expression	Bisheriger Ausdruck
	 * @param color	Bisherige Farbe
	 * @param element	Modell-Element dessen Ausdrücke und Farben konfiguriert werden sollen
	 * @param helpRunnable	Hilfe-Callback
	 * @param usedExpressions	Liste aller momentan verwendeten Ausdrücke (darf den aktuellen Ausdruck enthalten, darf leer oder <code>null</code> sein) - um im Vorlagenpopup keine bereits verwendeten Ausdrücke anzubieten
	 */
	public BarStackTableModelDialog(final Component owner, final Runnable help, final AnimationExpression expression, final Color color, final ModelElement element, final Runnable helpRunnable, final List<AnimationExpression> usedExpressions) {
		super(owner,Language.tr("Surface.AnimationBarStack.Dialog.Edit"));

		JPanel line;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		this.expression=new AnimationExpression(expression);
		content.add(editExpression=new AnimationExpressionPanel(element,this.expression,readOnly,helpRunnable,AnimationExpressionPanel.extractExpressionStrings(usedExpressions)));

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooserBar=new LabeledColorChooserButton(Language.tr("Surface.AnimationBarStack.Dialog.BarColor")+":",(color==null)?Color.RED:color));
		colorChooserBar.setEnabled(!readOnly);

		setMinSizeRespectingScreensize(480,0);
		pack();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Ausdruck */
		if (!editExpression.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wird den neuen Ausdruck
	 * @return	Neuer Ausdruck
	 */
	public AnimationExpression getExpression() {
		editExpression.storeData();
		return expression;
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wird die neue Farbe
	 * @return	Neue Farbe
	 */
	public Color getColor() {
		return colorChooserBar.getColor();
	}
}
