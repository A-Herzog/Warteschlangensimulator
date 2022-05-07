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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
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
	/** Eingabefeld "In Verteilung aufzuzeichnende Werte der Kundendatenfelder" */
	private JTextField distributionRecordClientDataValues;
	/** Varianzerfassung über Momente */
	private JRadioButton optionPrecisionMoments;
	/** Varianzerfassung über Welfords Algorithmus */
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

		/* Zwischenüberschrift: "Anzahl der Einträge der vektoriell erfassten Daten" */

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

		/* Kundendatenfelder */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordClientDataValues.Value")+":",""+model.distributionRecordClientDataValues,10);
		sub=(JPanel)data[0];
		lines.add(sub);
		distributionRecordClientDataValues=(JTextField)data[1];
		distributionRecordClientDataValues.setEditable(!readOnly);
		addKeyListener(distributionRecordClientDataValues,()->checkDistributionRecordClientDataValues());
		sub.add(new JLabel(" ("+Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Info")+")"));

		/* Zwischenüberschrift: "Präzision der Varianzerfassung" */

		lines.add(Box.createVerticalStrut(25));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.Precision")+"</b></html>"));

		/* Welford-Erfassung */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionPrecisionMoments=new JRadioButton("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.Precision.Moments")+"</b></html>",!model.useWelford));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(Box.createHorizontalStrut(25));
		sub.add(new JLabel(Language.tr("Editor.Dialog.Tab.Simulation.Precision.Moments.Info")));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(optionPrecisionWelford=new JRadioButton("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.Precision.Welford")+"</b></html>",model.useWelford));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(Box.createHorizontalStrut(25));
		sub.add(new JLabel(Language.tr("Editor.Dialog.Tab.Simulation.Precision.Welford.Info")));

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionPrecisionMoments);
		buttonGroup.add(optionPrecisionWelford);	}

	/**
	 * Prüft die angegebene Anzahl an Stunden für die Erfassung der Verteilungen in der Statistik.
	 * @return	Liefert <code>true</code>, wenn die Stundenanzahl gültig ist.
	 * @see #distributionRecordHours
	 * @see #checkData()
	 */
	private boolean checkDistributionRecordHours() {
		return (NumberTools.getNotNegativeLong(distributionRecordHours,true)!=null);
	}

	/**
	 * Prüft die angegebene Anzahl an Werten für die Erfassung der Verteilungen über die Kundendatenfelder in der Statistik.
	 * @return	Liefert <code>true</code>, wenn der Wert gültig ist.
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

		if (!checkDistributionRecordClientDataValues()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordClientDataValues.Error"),distributionRecordClientDataValues.getText()));
			return false;
		}

		return true;
	}

	@Override
	public void storeData() {
		Long L=NumberTools.getNotNegativeLong(distributionRecordHours,true);
		if (L!=null) model.distributionRecordHours=(int)L.longValue();
		L=NumberTools.getNotNegativeLong(distributionRecordClientDataValues,true);
		if (L!=null) model.distributionRecordClientDataValues=(int)L.longValue();
		model.useWelford=optionPrecisionWelford.isSelected();
	}
}
