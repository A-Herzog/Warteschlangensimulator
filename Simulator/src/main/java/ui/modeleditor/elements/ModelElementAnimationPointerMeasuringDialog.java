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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationPointerMeasuring}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationPointerMeasuring
 */
public class ModelElementAnimationPointerMeasuringDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4565047267829598870L;

	/** Eingabefeld für den Rechenausdruck */
	private JTextField editExpression;
	/** Eingabefeld für den Minimalwert der Anzeigeakala */
	private JTextField editMinValue;
	/** Eingabefeld für den Maximalwert der Anzeigeakala */
	private JTextField editMaxValue;
	/** Auswahl der Farbe des Zeigers */
	private SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationPointerMeasuring}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationPointerMeasuringDialog(final Component owner, final ModelElementAnimationPointerMeasuring element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationPointerMeasuring.Dialog.Title"),element,"ModelElementAnimationPointerMeasuring",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationPointerMeasuring;
	}

	@Override
	protected JComponent getContentPanel() {
		JPanel line, cell;
		JLabel label;
		Object[] data;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		data=getInputPanel(Language.tr("Surface.AnimationPointerMeasuring.Dialog.Expression")+":","");
		content.add((JPanel)data[0]);
		editExpression=(JTextField)data[1];
		editExpression.setEditable(!readOnly);
		((JPanel)data[0]).add(getExpressionEditButton(this,editExpression,false,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		editExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Minimalwert */
		data=getInputPanel(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MinValue")+":","",10);
		content.add((JPanel)data[0]);
		editMinValue=(JTextField)data[1];
		editMinValue.setEditable(!readOnly);
		editMinValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Maximalwert */
		data=getInputPanel(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MaxValue")+":","",10);
		content.add((JPanel)data[0]);
		editMaxValue=(JTextField)data[1];
		editMaxValue.setEditable(!readOnly);
		editMaxValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationPointerMeasuring.Dialog.Color")+":"),BorderLayout.NORTH);
		cell.add(colorChooser=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooser.setEnabled(!readOnly);
		label.setLabelFor(colorChooser);

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationPointerMeasuring) {
			final ModelElementAnimationPointerMeasuring pointerMeasuring=(ModelElementAnimationPointerMeasuring)element;
			editExpression.setText(pointerMeasuring.getExpression());
			editMinValue.setText(""+pointerMeasuring.getMinValue());
			editMaxValue.setText(""+pointerMeasuring.getMaxValue());
			colorChooser.setColor(pointerMeasuring.getColor());
		}

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		/* Ausdruck */
		final String text=editExpression.getText().trim();
		if (text.isEmpty()) {
			ok=false;
			editExpression.setBackground(Color.red);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.Expression.Error.Title"),Language.tr("Surface.AnimationPointerMeasuring.Dialog.Expression.ErrorNoExpression.Info"));
				return false;
			}
		} else {
			int error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				ok=false;
				editExpression.setBackground(Color.red);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.AnimationPointerMeasuring.Dialog.Expression.ErrorInvalidExpression.Info"),text,error+1));
					return false;
				}
			} else {
				editExpression.setBackground(SystemColor.text);
			}
		}

		/* Minimalwert */
		final Long L1=NumberTools.getNotNegativeLong(editMinValue,true);
		if (L1==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.MinValue.Error.Title"),String.format(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MinValue.Error.Info"),editMinValue.getText()));
				return false;
			}
		}

		/* Maximalwert */
		final Long L2=NumberTools.getPositiveLong(editMaxValue,true);
		if (L2==null || (L1!=null && L2.longValue()<=L1.longValue())) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationPointerMeasuring.Dialog.MaxValue.Error.Title"),String.format(Language.tr("Surface.AnimationPointerMeasuring.Dialog.MaxValue.Error.Info"),editMaxValue.getText()));
				return false;
			}
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
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementAnimationPointerMeasuring) {
			final ModelElementAnimationPointerMeasuring pointerMeasuring=(ModelElementAnimationPointerMeasuring)element;
			pointerMeasuring.setExpression(editExpression.getText());
			pointerMeasuring.setMinValue(NumberTools.getNotNegativeLong(editMinValue,true).intValue());
			pointerMeasuring.setMaxValue(NumberTools.getNotNegativeLong(editMaxValue,true).intValue());
			pointerMeasuring.setColor(colorChooser.getColor());
		}
	}
}
