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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnalogValue}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnalogValue
 */
public class ModelElementAnalogValueDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4472904120653635356L;

	/**
	 * Umrechnungsfaktor um die Änderungsrate auf Sekunden zu normieren
	 * @see #changeRateUnit
	 * @see #analogNotifyUnit
	 */
	private static double[] MULTIPLY=new double[]{1,60,3600,86400};

	/** Eingabefeld für den initialen Wert */
	private JTextField initalValue;
	/** Option: Minimalwert verwenden? */
	private JCheckBox valueMinUse;
	/** Eingabefeld für den Minimalwert */
	private JTextField valueMin;
	/** Option: Maximalwert verwenden? */
	private JCheckBox valueMaxUse;
	/** Eingabefeld für den Maximalwert */
	private JTextField valueMax;
	/** Eingabefeld für die Änderungsrate */
	private JTextField changeRate;
	/** Anzusetzende Zeiteinheit für die Änderungsrate */
	private JComboBox<String> changeRateUnit;
	/** Eingabefeld für den Änderungsbenachrichtigungsabstand */
	private JTextField analogNotify;
	/** Anzusetzende Zeiteinheit für den Änderungsbenachrichtigungsabstand */
	private JComboBox<String> analogNotifyUnit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnalogValue}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnalogValueDialog(final Component owner, final ModelElementAnalogValue element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnalogValue.Dialog.Title"),element,"ModelElementAnalogValue",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	/**
	 * Lädt einen Wert in ein Eingabefeld und stellt die Zeiteinheiten-Auswahlbox passend ein.
	 * @param value	Zu verwendender Wert
	 * @param text	Eingabefeld in das der Wert (ggf. skaliert) eingetragen werden soll
	 * @param unit	Zeiteinheiten-Auswahlbox
	 * @param mul	Mit {@link #MULTIPLY} multiplizieren oder beim Umrechnen dadurch dividieren?
	 */
	private void loadValue(double value, final JTextField text, final JComboBox<String> unit, boolean mul) {
		text.setEditable(!readOnly);
		text.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		unit.setEnabled(!readOnly);
		unit.addActionListener(e->checkData(false));

		boolean minus=(value<0);
		value=Math.abs(value);
		int index=0;
		double scaled=value;
		if (mul) {
			while (index<MULTIPLY.length-1 && scaled<1) {
				index++;
				scaled=value*MULTIPLY[index];
			}
		} else {
			while (index<MULTIPLY.length-1 && value/MULTIPLY[index+1]>=1) {
				index++;
				scaled=value/MULTIPLY[index];
			}
		}

		double v=(minus?-1:1)*scaled;
		text.setText(NumberTools.formatNumberMax(NumberTools.reduceDigits(v,10)));
		unit.setSelectedIndex(index);
	}

	/**
	 * Erstellt eine Eingabeoption bestehend aus Checkbox und Eingabefeld
	 * @param label	Beschriftung
	 * @param check	Initialer Status der Checkbox
	 * @param text	Initialer Wert für das Eingabefeld
	 * @param size	Breite des Eingabefeldes
	 * @return	Liefert ein Array aus Zeile, Checkbox und Eingabefeld
	 */
	private Object[] getCheckInput(final String label, final boolean check, final String text, final int size) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));

		final JCheckBox checkbox=new JCheckBox(label,check);
		line.add(checkbox);

		final JTextField input=new JTextField(text,size);
		line.add(input);

		return new Object[]{line,checkbox,input};
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnalogValue;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementAnalogValue analogValue=(ModelElementAnalogValue)element;

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final String[] unitNames1=new String[]{
				Language.tr("Surface.AnalogValue.Dialog.Unit.PerSecond"),
				Language.tr("Surface.AnalogValue.Dialog.Unit.PerMinute"),
				Language.tr("Surface.AnalogValue.Dialog.Unit.PerHour"),
				Language.tr("Surface.AnalogValue.Dialog.Unit.PerDay")
		};
		final String[] unitNames2=new String[]{
				Language.tr("Surface.AnalogValue.Dialog.Unit.DeltaSeconds"),
				Language.tr("Surface.AnalogValue.Dialog.Unit.DeltaMinutes"),
				Language.tr("Surface.AnalogValue.Dialog.Unit.DeltaHours"),
				Language.tr("Surface.AnalogValue.Dialog.Unit.DeltaDays")
		};

		Object[] data;
		JPanel line;

		/* Initialwert */
		data=getInputPanel(Language.tr("Surface.AnalogValue.Dialog.InitialValue")+":",NumberTools.formatNumberMax(analogValue.getInitialValue()),10);
		content.add((JPanel)data[0]);
		initalValue=(JTextField)data[1];
		initalValue.setEditable(!readOnly);
		initalValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Minimalwert */
		data=getCheckInput(Language.tr("Surface.AnalogValue.Dialog.MinimumValue")+":",analogValue.isValueMinUse(),NumberTools.formatNumberMax(analogValue.getValueMin()),10);
		content.add((JPanel)data[0]);
		valueMinUse=(JCheckBox)data[1];
		valueMinUse.setEnabled(!readOnly);
		valueMinUse.addActionListener(e->checkData(false));
		valueMin=(JTextField)data[2];
		valueMin.setEditable(!readOnly);
		valueMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {valueMinUse.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {valueMinUse.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {valueMinUse.setSelected(true); checkData(false);}
		});

		data=getCheckInput(Language.tr("Surface.AnalogValue.Dialog.MaximumValue")+":",analogValue.isValueMaxUse(),NumberTools.formatNumberMax(analogValue.getValueMax()),10);
		content.add((JPanel)data[0]);
		valueMaxUse=(JCheckBox)data[1];
		valueMaxUse.setEnabled(!readOnly);
		valueMaxUse.addActionListener(e->checkData(false));
		valueMax=(JTextField)data[2];
		valueMax.setEditable(!readOnly);
		valueMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {valueMaxUse.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {valueMaxUse.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {valueMaxUse.setSelected(true); checkData(false);}
		});

		/* Änderungsrate */
		data=getInputPanel(Language.tr("Surface.AnalogValue.Dialog.ChangeRate")+":","",10);
		content.add(line=(JPanel)data[0]);
		changeRate=(JTextField)data[1];
		line.add(changeRateUnit=new JComboBox<>(unitNames1));
		loadValue(analogValue.getChangeRatePerSecond(),changeRate,changeRateUnit,true);

		/* Änderungsbenachrichtigung */
		data=getInputPanel(Language.tr("Surface.AnalogValue.Dialog.AnalogNotify")+":","",10);
		content.add(line=(JPanel)data[0]);
		analogNotify=(JTextField)data[1];
		line.add(analogNotifyUnit=new JComboBox<>(unitNames2));
		loadValue(analogValue.getAnalogNotify(),analogNotify,analogNotifyUnit,false);

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
		Double D;

		/* Initialwert */
		D=NumberTools.getDouble(initalValue,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnalogValue.Dialog.InitialValue.ErrorTitle"),String.format(Language.tr("Surface.AnalogValue.Dialog.InitialValue.ErrorInfo"),initalValue.getText()));
				return false;
			}
		}

		/* Minimalwert */
		if (valueMinUse.isSelected()) {
			D=NumberTools.getDouble(valueMin,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnalogValue.Dialog.MinimumValue.ErrorTitle"),String.format(Language.tr("Surface.AnalogValue.Dialog.MinimumValue.ErrorInfo"),valueMin.getText()));
					return false;
				}
			}
		} else {
			valueMin.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Maximalwert */
		if (valueMaxUse.isSelected()) {
			D=NumberTools.getDouble(valueMax,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnalogValue.Dialog.MaximumValue.ErrorTitle"),String.format(Language.tr("Surface.AnalogValue.Dialog.MaximumValue.ErrorInfo"),valueMax.getText()));
					return false;
				}
			}
		} else {
			valueMax.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Änderungsrate */
		D=NumberTools.getDouble(changeRate,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnalogValue.Dialog.ChangeRate.ErrorTitle"),String.format(Language.tr("Surface.AnalogValue.Dialog.ChangeRate.ErrorInfo"),changeRate.getText()));
				return false;
			}
		}

		/* Änderungsbenachrichtigung */
		D=NumberTools.getPositiveDouble(analogNotify,true);
		if (D==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnalogValue.Dialog.AnalogNotify.ErrorTitle"),String.format(Language.tr("Surface.AnalogValue.Dialog.AnalogNotify.ErrorInfo"),analogNotify.getText()));
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

		final ModelElementAnalogValue analogValue=(ModelElementAnalogValue)element;
		Double D;

		analogValue.setInitialValue(NumberTools.getDouble(initalValue,true));

		if (valueMinUse.isSelected()) {
			analogValue.setValueMinUse(true);
			analogValue.setValueMin(NumberTools.getDouble(valueMin,true));
		} else {
			analogValue.setValueMinUse(false);
		}

		if (valueMaxUse.isSelected()) {
			analogValue.setValueMaxUse(true);
			analogValue.setValueMax(NumberTools.getDouble(valueMax,true));
		} else {
			analogValue.setValueMaxUse(false);
		}

		D=NumberTools.getDouble(changeRate,true);
		analogValue.setChangeRatePerSecond(D.doubleValue()/MULTIPLY[changeRateUnit.getSelectedIndex()]);

		D=NumberTools.getPositiveDouble(analogNotify,true);
		analogValue.setAnalogNotify(D.doubleValue()*MULTIPLY[analogNotifyUnit.getSelectedIndex()]);
	}
}
