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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementInteractiveSlider}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementInteractiveSlider
 */
public class ModelElementInteractiveSliderDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7524897419484118673L;

	/** Eingabefeld für den Variablennamen */
	private JTextField editVariable;
	/** Eingabefeld für den Minimalwert */
	private JTextField editMinValue;
	/** Eingabefeld für den Maximalwert */
	private JTextField editMaxValue;
	/** Eingabefeld für die Schrittweite */
	private JTextField editStep;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementInteractiveSlider}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementInteractiveSliderDialog(final Component owner, final ModelElementInteractiveSlider element, final boolean readOnly) {
		super(owner,Language.tr("Surface.InteractiveSlider.Dialog.Title"),element,"ModelElementInteractiveSlider",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
		doLayout();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationInteractiveSlider;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BorderLayout());
		JPanel setup=new JPanel();
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));
		content.add(setup,BorderLayout.NORTH);

		final ModelElementInteractiveSlider slider=(ModelElementInteractiveSlider)element;
		Object[] data;

		data=getInputPanel(Language.tr("Surface.InteractiveSlider.Dialog.Variable")+":",slider.getVariable());
		setup.add((JPanel)data[0]);
		editVariable=(JTextField)data[1];
		editVariable.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.InteractiveSlider.Dialog.MinValue")+":",NumberTools.formatNumberMax(slider.getMinValue()));
		setup.add((JPanel)data[0]);
		editMinValue=(JTextField)data[1];
		editMinValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.InteractiveSlider.Dialog.MaxValue")+":",NumberTools.formatNumberMax(slider.getMaxValue()));
		setup.add((JPanel)data[0]);
		editMaxValue=(JTextField)data[1];
		editMaxValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		data=getInputPanel(Language.tr("Surface.InteractiveSlider.Dialog.StepWide")+":",NumberTools.formatNumberMax(slider.getStep()));
		setup.add((JPanel)data[0]);
		editStep=(JTextField)data[1];
		editStep.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (!ExpressionCalc.checkVariableName(editVariable.getText().trim())) {
			editVariable.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveSlider.Dialog.Variable.ErrorTitle"),Language.tr("Surface.InteractiveSlider.Dialog.Variable.ErrorInfo"));
				return false;
			}
		} else {
			editVariable.setBackground(SystemColor.text);
		}

		final Double min=NumberTools.getDouble(editMinValue,true);
		if (min==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveSlider.Dialog.MinValue.ErrorTitle"),String.format(Language.tr("Surface.InteractiveSlider.Dialog.MinValue.ErrorInfo"),editMinValue.getText()));
				return false;
			}
		}

		final Double max=NumberTools.getDouble(editMaxValue,true);
		if (max==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveSlider.Dialog.MaxValue.ErrorTitle"),String.format(Language.tr("Surface.InteractiveSlider.Dialog.MaxValue.ErrorInfo"),editMaxValue.getText()));
				return false;
			}
		}

		if (ok && min!=null && max!=null) {
			if (min.doubleValue()>=max.doubleValue()) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.InteractiveSlider.Dialog.MinMaxErrorTitle"),String.format(Language.tr("Surface.InteractiveSlider.Dialog.MinMaxErrorInfo"),editMinValue.getText(),editMaxValue.getText()));
					return false;
				}
			}
		}

		final Double step=NumberTools.getPositiveDouble(editStep,true);
		if (step==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.InteractiveSlider.Dialog.StepWide.ErrorTitle"),String.format(Language.tr("Surface.InteractiveSlider.Dialog.StepWide.ErrorInfo"),editStep.getText()));
				return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();
		final ModelElementInteractiveSlider slider=(ModelElementInteractiveSlider)element;
		slider.setVariable(editVariable.getText().trim());
		slider.setMinValue(NumberTools.getDouble(editMinValue,true));
		slider.setMaxValue(NumberTools.getDouble(editMaxValue,true));
		slider.setStep(NumberTools.getPositiveDouble(editStep,true));
	}
}