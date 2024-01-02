/**
 * Copyright 2022 Alexander Herzog
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

import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialogseite "Statistik"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageStatistics extends ModelPropertiesDialogPage {
	/** Eingabefeld "In Verteilung aufzuzeichnende Stunden" */
	private JTextField distributionRecordHours;

	/** Eingabefeld "H�chster per Histogramm zu erfassender Zustand" */
	private JTextField stateRecordSize;

	/** Option "Kunden, die am Simulationsende das System noch nicht verlassen haben, erfassen" */
	private JCheckBox recordIncompleteClients;

	/** Eingabefeld "In Verteilung aufzuzeichnende Werte der Kundendatenfelder" */
	private JTextField distributionRecordClientDataValues;

	/** Eingabefeld "Erfassung der Text-Kundendatenfelder in der Statistik" */
	private JCheckBox recordClientTextData;

	/** Varianzerfassung �ber Momente */
	private JRadioButton optionPrecisionMoments;
	/** Varianzerfassung �ber Welfords Algorithmus */
	private JRadioButton optionPrecisionWelford;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageStatistics(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		JPanel sub;
		Object[] data;

		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel lines;
		content.add(lines=new JPanel());

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		/* Zwischen�berschrift: "Anzahl der Eintr�ge der vektoriell erfassten Daten" */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.StatisticVectorLength")+"</b></html>"));

		/* Anzahl Stunden in Verteilungen */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Value")+":",""+model.distributionRecordHours,5);
		sub=(JPanel)data[0];
		lines.add(sub);
		distributionRecordHours=(JTextField)data[1];
		distributionRecordHours.setEditable(!readOnly);
		addKeyListener(distributionRecordHours,()->checkDistributionRecordHours());
		sub.add(new JLabel(" ("+Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Info")+")"));

		/* Welcher Zustand (z.B. Anzahl an Kunden im System) soll maximal erfasst werden? */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.StateRecordSize.Value")+":",""+model.stateRecordSize,8);
		sub=(JPanel)data[0];
		lines.add(sub);
		stateRecordSize=(JTextField)data[1];
		stateRecordSize.setEditable(!readOnly);
		addKeyListener(stateRecordSize,()->checkStateRecordSize());

		/* Zwischen�berschrift: "Kundendaten" */

		lines.add(Box.createVerticalStrut(25));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.ClientData")+"</b></html>"));

		/* Kunden, die am Simulationsende das System noch nicht verlassen haben, erfassen */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		lines.add(sub);
		sub.add(recordIncompleteClients=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.RecordIncompleteClients"),model.recordIncompleteClients));
		recordIncompleteClients.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.RecordIncompleteClients.Hint"));
		recordIncompleteClients.setEnabled(!readOnly);

		/* Kundendatenfelder */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordClientDataValues.Value")+":",""+model.distributionRecordClientDataValues,10);
		sub=(JPanel)data[0];
		lines.add(sub);
		distributionRecordClientDataValues=(JTextField)data[1];
		distributionRecordClientDataValues.setEditable(!readOnly);
		addKeyListener(distributionRecordClientDataValues,()->checkDistributionRecordClientDataValues());
		sub.add(new JLabel(" ("+Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Info")+")"));

		/* Text-Kundendatenfelder */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		lines.add(sub);
		sub.add(recordClientTextData=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.RecordClientTextData"),model.recordClientTextData));
		recordClientTextData.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.RecordClientTextData.Hint"));
		recordClientTextData.setEnabled(!readOnly);

		/* Zwischen�berschrift: "Pr�zision der Varianzerfassung" */

		lines.add(Box.createVerticalStrut(25));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.Precision")+"</b></html>"));

		/* Welford-Erfassung */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionPrecisionMoments=new JRadioButton("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.Precision.Moments")+"</b></html>",!model.useWelford));
		optionPrecisionMoments.setEnabled(!readOnly);

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(Box.createHorizontalStrut(25));
		sub.add(new JLabel(Language.tr("Editor.Dialog.Tab.Simulation.Precision.Moments.Info")));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionPrecisionWelford=new JRadioButton("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.Precision.Welford")+"</b></html>",model.useWelford));
		optionPrecisionWelford.setEnabled(!readOnly);

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(Box.createHorizontalStrut(25));
		sub.add(new JLabel(Language.tr("Editor.Dialog.Tab.Simulation.Precision.Welford.Info")));

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionPrecisionMoments);
		buttonGroup.add(optionPrecisionWelford);
	}

	/**
	 * Pr�ft die angegebene Anzahl an Stunden f�r die Erfassung der Verteilungen in der Statistik.
	 * @return	Liefert <code>true</code>, wenn die Stundenanzahl g�ltig ist.
	 * @see #distributionRecordHours
	 * @see #checkData()
	 */
	private boolean checkDistributionRecordHours() {
		return (NumberTools.getNotNegativeLong(distributionRecordHours,true)!=null);
	}

	/**
	 * Pr�ft die angegebene Maximalgr��e f�r die Erfassung der Zeitdauern pro Zustand in der Statistik.
	 * @return	Liefert <code>true</code>, wenn der Wert g�ltig ist.
	 * @see #stateRecordSize
	 * @see #checkData()
	 */
	private boolean checkStateRecordSize() {
		final Long L=NumberTools.getNotNegativeLong(stateRecordSize,true);
		return (L!=null && L.intValue()<=StatisticsTimePerformanceIndicator.MAX_MAX_STATE);
	}

	/**
	 * Pr�ft die angegebene Anzahl an Werten f�r die Erfassung der Verteilungen �ber die Kundendatenfelder in der Statistik.
	 * @return	Liefert <code>true</code>, wenn der Wert g�ltig ist.
	 * @see #distributionRecordClientDataValues
	 * @see #checkData()
	 */
	private boolean checkDistributionRecordClientDataValues() {
		return (NumberTools.getNotNegativeLong(distributionRecordClientDataValues,true)!=null);
	}

	@Override
	public boolean checkData() {
		if (!checkDistributionRecordHours()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Error"),distributionRecordHours.getText()));
			return false;
		}

		if (!checkStateRecordSize()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.StateRecordSize.Error"),stateRecordSize.getText()));
			return false;
		}

		if (!checkDistributionRecordClientDataValues()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordClientDataValues.Error"),distributionRecordClientDataValues.getText()));
			return false;
		}

		return true;
	}

	@Override
	public void storeData() {
		Long L;

		L=NumberTools.getNotNegativeLong(distributionRecordHours,true);
		if (L!=null) model.distributionRecordHours=L.intValue();

		L=NumberTools.getNotNegativeLong(stateRecordSize,true);
		if (L!=null) model.stateRecordSize=L.intValue();

		model.recordIncompleteClients=recordIncompleteClients.isSelected();

		L=NumberTools.getNotNegativeLong(distributionRecordClientDataValues,true);
		if (L!=null) model.distributionRecordClientDataValues=(int)L.longValue();

		model.recordClientTextData=recordClientTextData.isSelected();

		model.useWelford=optionPrecisionWelford.isSelected();
	}
}
