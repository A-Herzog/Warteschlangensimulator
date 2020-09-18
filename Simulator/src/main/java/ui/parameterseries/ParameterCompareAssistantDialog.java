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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.JTableExt;

/**
 * Dialog zur Erstellung von Modellen zur Parametervariation
 * für die Parameter-Variationsstudien-Funktion
 * @author Alexander Herzog
 */
public class ParameterCompareAssistantDialog extends BaseDialog {
	private static final long serialVersionUID = 7801607609806408642L;

	/** Sollen die Parameterwerte in die Modellnamen mit aufgenommen werden? */
	private final JCheckBox includeParametersInName;
	/** Datenmodell der Vorgaben für die Erstellung der Modelle */
	private final ParameterCompareAssistantTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param setup	Parameter-Vergleichs-Einstellungen
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareAssistantDialog(final Component owner, final ParameterCompareSetup setup, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Table.AddModelByAssistant.Title"));

		/* GUI */

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		/* Tabelle */

		final JTableExt table=new JTableExt();
		content.add(new JScrollPane(table),BorderLayout.CENTER);
		table.setModel(tableModel=new ParameterCompareAssistantTableModel(table,setup));

		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(6);

		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(0).setMaxWidth(150);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(3).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMaxWidth(100);
		table.getColumnModel().getColumn(4).setMinWidth(100);
		table.getColumnModel().getColumn(4).setMaxWidth(100);
		table.getColumnModel().getColumn(5).setMinWidth(100);
		table.getColumnModel().getColumn(5).setMaxWidth(100);
		table.getColumnModel().getColumn(6).setMinWidth(125);
		table.getColumnModel().getColumn(6).setMaxWidth(125);

		/* Fußbereich */

		final JPanel footer=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(footer,BorderLayout.SOUTH);
		footer.add(includeParametersInName=new JCheckBox(Language.tr("ParameterCompare.Table.AddModelByAssistant.ParametersInName"),false));

		/* Dialog starten */

		setMinSizeRespectingScreensize(1000,500);
		setSizeRespectingScreensize(1000,500);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	@Override
	public boolean checkData() {
		final String error=tableModel.checkData();
		if (error!=null) MsgBox.error(this,Language.tr("ParameterCompare.Table.AddModelByAssistant.Error"),error);
		return error==null;
	}

	@Override
	public void storeData() {
		tableModel.storeData(includeParametersInName.isSelected());
	}
}