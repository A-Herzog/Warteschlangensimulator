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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog zum Bearbeiten einer Reihe für ein Amimationsdiagramm
 * (Ausdruck, Minimum, Maximum)
 * @author Alexander Herzog
 * @see ExpressionTableModelBar
 * @see ExpressionTableModelLine
 * @see ExpressionTableModelDialog2
 */
public class ExpressionTableModelDialog1 extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -9107725290348536791L;

	/**
	 * Ausdruck, der in {@link #editExpression} bearbeitet wird.
	 * @see #editExpression
	 * @see #getExpression()
	 */
	private final AnimationExpression expression;

	/** Eingabefeld für den Rechenausdruck */
	private final AnimationExpressionPanel editExpression;
	/** Eingabefeld für den Minimalwert */
	private final JTextField minValueEdit;
	/** Eingabefeld für den Maximalwert */
	private final JTextField maxValueEdit;

	/**
	 * Liefert den Titel für den Dialog.
	 * @param iconMode	Soll der Dialog zum Bearbeiten einer Balkendiagrammreihe oder eines Tortensegments verwendet werden?
	 * @return	Titel für den Dialog
	 */
	private static String getTitle(final ExpressionTableModelBar.IconMode iconMode) {
		switch (iconMode) {
		case BAR: return Language.tr("Surface.ExpressionTableModel.Dialog");
		case PIE: return Language.tr("Surface.ExpressionTableModel.Dialog.Pie");
		default: return "";
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param element	Modell-Element dessen Ausdrücke konfiguriert werden sollen
	 * @param expression	Bisheriger Ausdruck
	 * @param minValue	Bisheriger Minimalwert
	 * @param maxValue	Bisheriger Maximalwert
	 * @param helpRunnable	Hilfe-Runnable
	 * @param iconMode	Soll der Dialog zum Bearbeiten einer Balkendiagrammreihe oder eines Tortensegments verwendet werden?
	 */
	public ExpressionTableModelDialog1(final Component owner, final ModelElement element, final AnimationExpression expression, final double minValue, final double maxValue, final Runnable helpRunnable, final ExpressionTableModelBar.IconMode iconMode) {
		super(owner,getTitle(iconMode));

		Object[] data;

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		if (expression!=null) {
			this.expression=new AnimationExpression(expression);
			content.add(editExpression=new AnimationExpressionPanel(element,this.expression,readOnly,helpRunnable));
		} else {
			this.expression=null;
			editExpression=null;
		}

		/* Minimaler Wert */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.ExpressionTableModel.Dialog.MinimalValue")+":",NumberTools.formatNumber(minValue));
		content.add((JPanel)data[0]);
		minValueEdit=(JTextField)data[1];
		minValueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Maximaler Wert */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.ExpressionTableModel.Dialog.MaximalValue")+":",NumberTools.formatNumber(maxValue));
		content.add((JPanel)data[0]);
		maxValueEdit=(JTextField)data[1];
		maxValueEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Dialog starten */
		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param element	Modell-Element dessen Ausdrücke konfiguriert werden sollen
	 * @param expression	Bisheriger Ausdruck
	 * @param helpRunnable	Hilfe-Runnable
	 * @param iconMode	Soll der Dialog zum Bearbeiten einer Balkendiagrammreihe oder eines Tortensegments verwendet werden?
	 */
	public ExpressionTableModelDialog1(final Component owner, final ModelElement element, final AnimationExpression expression, final Runnable helpRunnable, final ExpressionTableModelBar.IconMode iconMode) {
		super(owner,getTitle(iconMode));

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		this.expression=new AnimationExpression(expression);
		content.add(editExpression=new AnimationExpressionPanel(element,this.expression,readOnly,helpRunnable));

		/* Kein minimaler oder maximaler Wert */
		minValueEdit=null;
		maxValueEdit=null;

		/* Dialog starten */
		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Ausdruck */
		if (editExpression!=null && !editExpression.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		if (minValueEdit!=null && maxValueEdit!=null) {
			/* Minimaler Wert */
			final Double Dmin=NumberTools.getDouble(minValueEdit,true);
			if (Dmin==null) {
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.ExpressionTableModel.Dialog.MinimalValue.Error.Title"),String.format(Language.tr("Surface.ExpressionTableModel.Dialog.MinimalValue.Error.Info"),minValueEdit.getText()));
					return false;
				}
				ok=true;
			}
			/* Maximaler Wert */
			final Double Dmax=NumberTools.getDouble(maxValueEdit,true);
			if (Dmax==null) {
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.ExpressionTableModel.Dialog.MaximalValue.Error.Title"),String.format(Language.tr("Surface.ExpressionTableModel.Dialog.MaximalValue.Error.Info"),maxValueEdit.getText()));
					return false;
				}
				ok=true;
			}
			if (Dmin!=null && Dmax!=null) {
				final double min=Dmin;
				final double max=Dmax;
				if (min>=max) {
					maxValueEdit.setBackground(Color.red);
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Surface.ExpressionTableModel.Dialog.Range.Error.Title"),String.format(Language.tr("Surface.ExpressionTableModel.Dialog.Range.Error.Info"),maxValueEdit.getText(),minValueEdit.getText()));
						return false;
					}
				}
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert den neuen Ausdruck.
	 * @return	Neuer Ausdruck
	 */
	public AnimationExpression getExpression() {
		if (editExpression==null) return null;
		editExpression.storeData();
		return expression;
	}

	/**
	 * Liefert den neuen Minimalwert.
	 * @return	Neuer Minimalwert
	 */
	public double getMinValue() {
		if (minValueEdit==null) return 0;
		return NumberTools.getDouble(minValueEdit.getText());
	}

	/**
	 * Liefert den neuen Maximalwert.
	 * @return	Neuer Maximalwert
	 */
	public double getMaxValue() {
		if (maxValueEdit==null) return 0;
		return NumberTools.getDouble(maxValueEdit.getText());
	}
}