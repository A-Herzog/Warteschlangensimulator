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
import java.awt.Component;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import systemtools.BaseDialog;
import tools.JTableExt;
import ui.dialogs.ExpressionCalculatorDialogTableModel;
import ui.help.Help;
import ui.infopanel.InfoPanel;

/**
 * In diesem Dialog können die Startwerte für die globale Zuordnung bearbeitet werden.
 * Der Dialog kann über den Modelleigenschaften-Dialog aufgerufen werden.
 * @author Alexander Herzog
 * @see ModelPropertiesDialogPageInitialValues
 * @see ModelPropertiesDialog
 */
public class ModelPropertiesDialogPageInitialValuesMapDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=5620548661449936354L;

	/**
	 * Verweis auf das offizielle Zuordnungs-Objekt<br>
	 * (aus diesem werden die Daten zum Bearbeiten entnommen und sie werden erst beim
	 * Schließen mit "Ok" hierhin zurückgeschrieben.)
	 */
	private final Map<String,Object> mapOriginal;

	/**
	 * Aktueller Bearbeitungsstand der Zuordnung
	 */
	private final Map<String,Object> mapEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param map	Zu bearbeitende Zuordnung (Daten werden beim Schließen mit "Ok" in dieses Objekt zurückgeschrieben)
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelPropertiesDialogPageInitialValuesMapDialog(final Component owner, final Map<String,Object> map, final boolean readOnly) {
		super(owner,Language.tr("Editor.Dialog.Tab.InitialVariableValues.Map"),readOnly);

		/* Zuordnung speichern */
		mapOriginal=map;
		mapEdit=new HashMap<>(mapOriginal);

		/* GUI aufbauen */
		final JPanel all=createGUI(()->Help.topicModal(this,"EditorModelDialog"));
		all.setLayout(new BorderLayout());

		/* Infopanel */
		InfoPanel.addTopPanel(all,InfoPanel.modelInitialValuesMap);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Tabelle */
		final JTableExt mapTable=new JTableExt();
		final ExpressionCalculatorDialogTableModel mapTableModel=new ExpressionCalculatorDialogTableModel(mapTable,this.mapEdit,readOnly);
		mapTable.setModel(mapTableModel);
		mapTable.getColumnModel().getColumn(0).setMaxWidth(125);
		mapTable.getColumnModel().getColumn(0).setMinWidth(125);
		mapTable.getColumnModel().getColumn(2).setMinWidth(150);
		mapTable.getColumnModel().getColumn(2).setMaxWidth(150);
		mapTable.getColumnModel().getColumn(3).setMinWidth(100);
		mapTable.getColumnModel().getColumn(3).setMaxWidth(100);
		mapTable.setIsPanelCellTable(3);
		content.add(new JScrollPane(mapTable),BorderLayout.CENTER);

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,600);
		setMaxSizeRespectingScreensize(1280,1024);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	public void storeData() {
		mapOriginal.clear();
		mapOriginal.putAll(mapEdit);
	}
}
