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
package ui.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.OptionalColorChooserButton;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.parameterseries.ParameterCompareTools;

/**
 * Dialog um einen einzelnen {@link StatisticViewerDashboardRecord}-Datensatz
 * zu bearbeiten.
 * @author Alexander Herzog
 * @see StatisticViewerDashboardRecord
 */
public class StatisticViewerDashboardRecordDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=3251754066345608059L;

	/**
	 * Zu bearbeitender Datensatz
	 */
	private final StatisticViewerDashboardRecord record;

	/* Tab "Inhalt" */

	/**
	 * Eingabefeld für das XML-Element
	 */
	private final JTextField xmlTagEdit;

	/**
	 * Eingabefeld für die Überschrift
	 */
	private final JTextField headingEdit;

	/**
	 * Auswahlfeld zur Einstellung, ob die Überschrift wenn möglich automatisch ermittelt werden soll
	 */
	private final JCheckBox autoHeading;

	/**
	 * Eingabefeld für einen optionalen Text vor dem Wert
	 */
	private final JTextField preTextEdit;

	/**
	 * Eingabefeld für einen optionalen Text hinter dem Wert
	 */
	private final JTextField postTextEdit;

	/**
	 * Auswahlfeld für die Anzahl an anzuzeigenden Nachkommastellen
	 */
	private final SpinnerModel digits;

	/* Tab "Format" */

	/**
	 * Auswahlbox für das Format des auszugebenden Wertes
	 * @see StatisticViewerDashboardRecord.Format
	 */
	private final JComboBox<String> formatCombo;

	/* Tab "Hintergrundfarbe" */

	/**
	 * Auswahlfeld für die Hintergrundfarbe
	 */
	private final OptionalColorChooserButton backgroundColor;

	/**
	 * Auswahlfeld für die Gradientfarbe
	 */
	private final OptionalColorChooserButton gradientColor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param statistics	Statistikdatenobjekt (für den XML-Element-Auswahldialog)
	 * @param record	Zu bearbeitender Datensatz (wird beim Klicken auf "Ok" verändert)
	 */
	public StatisticViewerDashboardRecordDialog(final Component owner, final Statistics statistics, final StatisticViewerDashboardRecord record) {
		super(owner,Language.tr("Statistics.Dashboard.EditDialog.Title"));
		this.record=record;

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter, tab, line;
		JLabel label;
		Object[] data;

		/* Tab "Inhalt" */
		tabs.addTab(Language.tr("Statistics.Dashboard.EditDialog.Tab.Content"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new BorderLayout()));
		line.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		line.add(new JLabel(Language.tr("Statistics.Dashboard.EditDialog.XML")+": "),BorderLayout.WEST);
		line.add(xmlTagEdit=new JTextField(),BorderLayout.CENTER);
		ModelElementBaseDialog.addUndoFeature(xmlTagEdit);
		xmlTagEdit.setEditable(false);
		final JButton xmlTagButton=new JButton(Language.tr("Statistics.Dashboard.EditDialog.XML.Button"));
		line.add(xmlTagButton,BorderLayout.EAST);
		xmlTagButton.setToolTipText(Language.tr("Statistics.Dashboard.EditDialog.XML.Hint"));
		xmlTagButton.setIcon(Images.PARAMETERSERIES_SELECT_XML.getIcon());
		xmlTagButton.addActionListener(e->{
			final String xml=ParameterCompareTools.selectXML(this,statistics.saveToXMLDocument(),null);
			if (xml==null) return;
			xmlTagEdit.setText(xml);
		});
		xmlTagEdit.setText(record.getXMLData());

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Statistics.Dashboard.EditDialog.Heading")+":",record.getHeading());
		tab.add((JPanel)data[0]);
		headingEdit=(JTextField)data[1];

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(autoHeading=new JCheckBox(Language.tr("Statistics.Dashboard.EditDialog.Heading.Auto"),record.isAutoHeading()));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Statistics.Dashboard.EditDialog.PreText")+":",record.getPreText());
		tab.add((JPanel)data[0]);
		preTextEdit=(JTextField)data[1];

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Statistics.Dashboard.EditDialog.PostText")+":",record.getPostText());
		tab.add((JPanel)data[0]);
		postTextEdit=(JTextField)data[1];

		/* Tab "Format" */
		tabs.addTab(Language.tr("Statistics.Dashboard.EditDialog.Tab.Format"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Statistics.Dashboard.EditDialog.Format")+":"));
		line.add(formatCombo=new JComboBox<>(new String[] {
				Language.tr("Statistics.Dashboard.EditDialog.Format.Number"),
				Language.tr("Statistics.Dashboard.EditDialog.Format.Percent"),
				Language.tr("Statistics.Dashboard.EditDialog.Format.Time")
		}));
		formatCombo.setRenderer(new IconListCellRenderer(new Images[] {
				Images.GENERAL_NUMBERS,
				Images.GENERAL_PERCENT,
				Images.GENERAL_TIME,
		}));
		label.setLabelFor(formatCombo);
		switch (record.getFormat()) {
		case NUMBER: formatCombo.setSelectedIndex(0); break;
		case PERCENT: formatCombo.setSelectedIndex(1); break;
		case TIME: formatCombo.setSelectedIndex(2); break;
		default: formatCombo.setSelectedIndex(0); break;
		}

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Statistics.Dashboard.EditDialog.Digits")+":"));
		final JSpinner digitsSpinner=new JSpinner(digits=new SpinnerNumberModel(1,1,9,1));
		JSpinner.NumberEditor spinnerEditor=new JSpinner.NumberEditor(digitsSpinner);
		spinnerEditor.getFormat().setGroupingUsed(false);
		spinnerEditor.getTextField().setColumns(2);
		digitsSpinner.setEditor(spinnerEditor);
		line.add(digitsSpinner);
		label.setLabelFor(digitsSpinner);
		digits.setValue(Math.max(0,Math.min(9,record.getDigits())));

		/* Tab "Hintergrundfarbe" */
		tabs.addTab(Language.tr("Statistics.Dashboard.EditDialog.Tab.BackgroundColor"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(backgroundColor=new OptionalColorChooserButton(Language.tr("Statistics.Dashboard.EditDialog.UserDefinedBackgroundColor")+":",record.getBackgroundColor(),Color.LIGHT_GRAY));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(gradientColor=new OptionalColorChooserButton(Language.tr("Statistics.Dashboard.EditDialog.GradientColor")+":",record.getGradientColor(),Color.GRAY));

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.GENERAL_NUMBERS.getIcon());
		tabs.setIconAt(1,Images.GENERAL_FONT.getIcon());
		tabs.setIconAt(2,Images.EDIT_BACKGROUND_COLOR.getIcon());

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void storeData() {
		/* Tab "Inhalt" */
		record.setXMLData(xmlTagEdit.getText().trim());
		record.setHeading(headingEdit.getText().trim());
		record.setAutoHeading(autoHeading.isSelected());
		record.setPreText(preTextEdit.getText());
		record.setPostText(postTextEdit.getText());

		/* Tab "Format" */
		switch (formatCombo.getSelectedIndex()) {
		case 0: record.setFormat(StatisticViewerDashboardRecord.Format.NUMBER); break;
		case 1: record.setFormat(StatisticViewerDashboardRecord.Format.PERCENT); break;
		case 2: record.setFormat(StatisticViewerDashboardRecord.Format.TIME); break;
		}
		record.setDigits(((Integer)digits.getValue()).intValue());

		/* Tab "Hintergrundfarbe" */
		record.setBackgroundColor(backgroundColor.getColor());
		record.setGradientColor(gradientColor.getColor());
	}
}
