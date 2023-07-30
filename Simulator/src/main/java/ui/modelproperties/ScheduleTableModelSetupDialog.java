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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSchedule;

/**
 * Dialog zur Konfiguration der Zeitslot-spezifischen
 * zusätzlichen Einstellungen für einen Zeitplans.
 * @author Alexander Herzog
 * @see SchedulesTableModel
 */
public class ScheduleTableModelSetupDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7856120681087030477L;

	/** Namen für die Auswahlmöglichkeiten für die Dauern der Zeitslots */
	private static String[] durationPerSlotTemplateStrings;
	/** Zeitdauern (in Sekunden) für die Auswahlmöglichkeiten für die Dauern der Zeitslots */
	private static final int[] durationPerSlotTemplateValues=new int[]{60,900,1800,3600,86400};
	/** Index in {@link #durationPerSlotTemplateValues} für die standardmäßige Dauer eines Zeitslots */
	private static final int durationPerSlotTemplateDefaultIndex=2;

	/** Höchster aktuell tatsächlich verwendeter y-Wert */
	private final int neededMaxY;

	/** Auswahlfeld "Dauer eines Intervalls" */
	private final JComboBox<String> durationPerSlotSelect;
	/** Eingabefeld "Maximalwert pro Intervall" */
	private final JTextField editorMaxYEdit;
	/** Option "Zeitplan wiederholen" (Am Ende des Zeitplans) */
	private final JRadioButton repeatModeRepeat;
	/** Option "Zeitplan wiederholen, aber vorher Tag mit 0 auffüllen" (Am Ende des Zeitplans) */
	private final JRadioButton repeatModeRepeatFillDays;
	/** Option "Auf letztem Wert verweilen" (Am Ende des Zeitplans) */
	private final JRadioButton repeatModeLastValue;
	/** Option "Mit 0 fortsetzen" (Am Ende des Zeitplans) */
	private final JRadioButton repeatModeZero;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param durationPerSlot	Zeitdauer pro Zeitslot
	 * @param editorMaxY	Als Maximum anzuzeigender y-Wert
	 * @param neededMaxY	Höchster aktuell tatsächlich verwendeter y-Wert
	 * @param repeatMode	Wie soll am Ende des Zeitplans verfahren werden?
	 * @see	ui.modeleditor.ModelSchedule.RepeatMode
	 */
	public ScheduleTableModelSetupDialog(final Component owner, final Runnable help, final int durationPerSlot, final int editorMaxY, final int neededMaxY, final ModelSchedule.RepeatMode repeatMode) {
		super(owner,Language.tr("Schedule.SettingsDialog.Title"));

		this.neededMaxY=neededMaxY;

		durationPerSlotTemplateStrings=new String[]{
				Language.tr("Schedule.SettingsDialog.1Minute"),
				Language.tr("Schedule.SettingsDialog.15Minutes"),
				Language.tr("Schedule.SettingsDialog.30Minutes"),
				Language.tr("Schedule.SettingsDialog.1Hour"),
				Language.tr("Schedule.SettingsDialog.1Day")
		};

		JPanel line, box2;
		JLabel label;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final Box box=Box.createVerticalBox();
		content.add(box,BorderLayout.CENTER);

		box.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Schedule.SettingsDialog.IntervalDuration")+":"));
		line.add(durationPerSlotSelect=new JComboBox<>(durationPerSlotTemplateStrings));
		durationPerSlotSelect.setSelectedIndex(durationPerSlotTemplateDefaultIndex);
		for (int i=0;i<durationPerSlotTemplateValues.length;i++) if (durationPerSlot==durationPerSlotTemplateValues[i]) {durationPerSlotSelect.setSelectedIndex(i); break;}
		label.setLabelFor(durationPerSlotSelect);

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Schedule.SettingsDialog.MaximumValuePerInterval")+":",""+editorMaxY,5);
		box.add((JPanel)data[0]);
		editorMaxYEdit=(JTextField)data[1];
		editorMaxYEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		final ButtonGroup buttonGroup=new ButtonGroup();
		box.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(box2=new JPanel());
		box2.setLayout(new BoxLayout(box2,BoxLayout.PAGE_AXIS));
		box2.add(repeatModeRepeat=new JRadioButton(Language.tr("Schedule.SettingsDialog.RepeatMode.Repeat")));
		box2.add(repeatModeRepeatFillDays=new JRadioButton(Language.tr("Schedule.SettingsDialog.RepeatMode.RepeatByDay")));
		box2.add(repeatModeLastValue=new JRadioButton(Language.tr("Schedule.SettingsDialog.RepeatMode.StayAtLastValue")));
		box2.add(repeatModeZero=new JRadioButton(Language.tr("Schedule.SettingsDialog.RepeatMode.ContinueWithZero")));
		box2.setBorder(BorderFactory.createTitledBorder(Language.tr("Schedule.SettingsDialog.RepeatMode")));

		buttonGroup.add(repeatModeRepeat);
		buttonGroup.add(repeatModeRepeatFillDays);
		buttonGroup.add(repeatModeLastValue);
		buttonGroup.add(repeatModeZero);

		repeatModeRepeat.setSelected(repeatMode==ModelSchedule.RepeatMode.REPEAT_MODE_REPEAT);
		repeatModeRepeatFillDays.setSelected(repeatMode==ModelSchedule.RepeatMode.REPEAT_MODE_REPEAT_FILL_DAY);
		repeatModeLastValue.setSelected(repeatMode==ModelSchedule.RepeatMode.REPEAT_MODE_STAY_AT_LAST_VALUE);
		repeatModeZero.setSelected(repeatMode==ModelSchedule.RepeatMode.REPEAT_MODE_ZERO);

		checkData(false);

		pack();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		final Integer I=NumberTools.getNotNegativeInteger(editorMaxYEdit,true);
		if (I==null || I<1) {
			if (showErrorMessage) MsgBox.error(this,Language.tr("Schedule.SettingsDialog.MaximumValuePerInterval.Error.Title"),String.format(Language.tr("Schedule.SettingsDialog.MaximumValuePerInterval.Error.Info"),editorMaxYEdit.getText()));
			editorMaxYEdit.setBackground(Color.RED);
			return false;
		} else {
			if (I<neededMaxY) {
				if (showErrorMessage) MsgBox.error(this,Language.tr("Schedule.SettingsDialog.MaximumValuePerInterval.Error.Title"),String.format(Language.tr("Schedule.SettingsDialog.MaximumValuePerInterval.Error.InfoLowerThanNeeded"),editorMaxYEdit.getText(),neededMaxY));
				editorMaxYEdit.setBackground(Color.RED);
				return false;
			} else {
				editorMaxYEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}
		return true;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert die eingestellte Zeitdauer pro Zeitslot.
	 * @return	Zeitdauer pro Zeitslot
	 */
	public int getDurationPerSlot() {
		return durationPerSlotTemplateValues[durationPerSlotSelect.getSelectedIndex()];
	}

	/**
	 * Liefert den eingestellten als Maximum anzuzeigenden y-Wert.
	 * @return	Als Maximum anzuzeigender y-Wert
	 */
	public int getEditorMaxY() {
		return NumberTools.getNotNegativeInteger(editorMaxYEdit,true);
	}

	/**
	 * Liefert die Einstellung, wie am Ende des Zeitplans verfahren werden soll
	 * @return	Verhalten am Ende des Zeitplans
	 */
	public ModelSchedule.RepeatMode getRepeatMode() {
		if (repeatModeRepeat.isSelected()) return ModelSchedule.RepeatMode.REPEAT_MODE_REPEAT;
		if (repeatModeRepeatFillDays.isSelected()) return ModelSchedule.RepeatMode.REPEAT_MODE_REPEAT_FILL_DAY;
		if (repeatModeLastValue.isSelected()) return ModelSchedule.RepeatMode.REPEAT_MODE_STAY_AT_LAST_VALUE;
		if (repeatModeZero.isSelected()) return ModelSchedule.RepeatMode.REPEAT_MODE_ZERO;
		return ModelSchedule.RepeatMode.REPEAT_MODE_REPEAT;
	}
}
