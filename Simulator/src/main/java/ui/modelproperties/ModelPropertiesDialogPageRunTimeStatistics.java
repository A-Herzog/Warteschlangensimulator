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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelLongRunStatisticsElement;

/**
 * Dialogseite "Laufzeitstatistik"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageRunTimeStatistics extends ModelPropertiesDialogPage {
	/** Eingabefeld "Zeitspanne pro Erfassungsintervall" */
	private JTextField stepWideEdit;
	/** Auswahlfeld für die Einheit für {@link #stepWideEdit} */
	private JComboBox<String> stepWideCombo;
	/** Checkbox zur Erfassung oder Verwerfung des letzten, unvollständigen Intervalls */
	private JCheckBox closeLastInterval;

	/** Datenmodell für die Liste der Laufzeitstatistikdatne */
	private AdditionalStatisticsTableModel statisticsData;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageRunTimeStatistics(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		content.setLayout(new BorderLayout());

		/* Schrittweite */

		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.NORTH);
		JLabel label=new JLabel(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.StepWide")+":");
		line.add(label);
		line.add(stepWideEdit=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(stepWideEdit);
		stepWideEdit.setEditable(!readOnly);
		addKeyListener(stepWideEdit,()->NumberTools.getPositiveDouble(stepWideEdit,true));
		line.add(stepWideCombo=new JComboBox<>(new String[] {
				Language.tr("Statistics.Seconds"),
				Language.tr("Statistics.Minutes"),
				Language.tr("Statistics.Hours"),
				Language.tr("Statistics.Days")
		}));
		stepWideCombo.setEnabled(!readOnly);

		double d=model.longRunStatistics.getStepWideSec();
		if (d<5*60) {
			/* Sekunden */
			stepWideEdit.setText(NumberTools.formatNumber(d));
			stepWideCombo.setSelectedIndex(0);
		} else {
			d=d/60;
			if (d<5*60) {
				/* Minuten */
				stepWideEdit.setText(NumberTools.formatNumber(d));
				stepWideCombo.setSelectedIndex(1);
			} else {
				d=d/60;
				if (d<5*24) {
					/* Stunden */
					stepWideEdit.setText(NumberTools.formatNumber(d));
					stepWideCombo.setSelectedIndex(2);
				} else {
					d=d/24;
					/* Tage */
					stepWideEdit.setText(NumberTools.formatNumber(d));
					stepWideCombo.setSelectedIndex(3);
				}
			}
		}

		line.add(closeLastInterval=new JCheckBox(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.LastInterval"),model.longRunStatistics.isCloseLastInterval()));
		closeLastInterval.setEnabled(!readOnly);

		/* Liste mit Ausdrücken */

		final JTableExt table=new JTableExt();
		statisticsData=new AdditionalStatisticsTableModel(model,table,readOnly,help);
		table.setModel(statisticsData);
		table.getColumnModel().getColumn(1).setMaxWidth(225);
		table.getColumnModel().getColumn(1).setMinWidth(225);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		content.add(new JScrollPane(table),BorderLayout.CENTER);

		/* Infopanel */

		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.SOUTH);
		line.add(new JLabel("<html><body>"+Language.tr("Editor.Dialog.Tab.RunTimeStatistics.Info").replace("\n","<br>\n")+"</body></html>"));
	}

	@Override
	public boolean checkData() {
		final Double D=NumberTools.getPositiveDouble(stepWideEdit,true);
		if (D==null) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.StepWide.Error"),stepWideEdit.getText()));
			return false;
		}

		return true;
	}

	@Override
	public void storeData() {
		final Double D=NumberTools.getPositiveDouble(stepWideEdit,true);
		if (D!=null) {
			double step=D;
			switch (stepWideCombo.getSelectedIndex()) {
			case 0: /* sind schon Sekunden */ break;
			case 1: step=step*60; break;
			case 2: step=step*60*60; break;
			case 3: step=step*60*60*24; break;
			}
			long l=Math.round(step);
			if (l<1) l=1;
			model.longRunStatistics.setStepWideSec(l);
			model.longRunStatistics.setCloseLastInterval(closeLastInterval.isSelected());
		}

		model.longRunStatistics.getData().clear();
		for (ModelLongRunStatisticsElement element: statisticsData.getData()) model.longRunStatistics.getData().add(element.clone());
	}
}
