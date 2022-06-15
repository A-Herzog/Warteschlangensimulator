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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import simulator.editmodel.EditModel;
import tools.IconListCellRenderer;
import tools.JTableExt;
import ui.images.Images;
import ui.modeleditor.ModelResources;

/**
 * Dialogseite "Bediener"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageOperators extends ModelPropertiesDialogPage {
	/** Datenmodell für die Liste mit den Modell-Ressourcen */
	private ResourceTableModel resourcesData;
	/** Auswahlfeld "Ressourcenpriorität an einer Station bei Gleichstand" */
	private JComboBox<String> secondaryResourcePriority;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageOperators(ModelPropertiesDialog dialog, EditModel model, boolean readOnly, Runnable help) {
		super(dialog,model,readOnly,help);
	}

	@Override
	public void build(JPanel content) {
		final JTableExt table=new JTableExt();
		resourcesData=new ResourceTableModel(model,model.surface,dialog.localResources,dialog.localSchedules,table,readOnly,help);
		table.setModel(resourcesData);
		table.getColumnModel().getColumn(1).setMaxWidth(175);
		table.getColumnModel().getColumn(1).setMinWidth(175);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		content.add(new JScrollPane(table),BorderLayout.CENTER);

		final JPanel setupArea=new JPanel(new FlowLayout(FlowLayout.LEFT));

		final JLabel label=new JLabel(Language.tr("Resources.SecondaryPriority")+": ");
		setupArea.add(label);
		setupArea.add(secondaryResourcePriority=new JComboBox<>(new String[]{
				Language.tr("Resources.SecondaryPriority.Random"),
				Language.tr("Resources.SecondaryPriority.ClientPriority")
		}));
		secondaryResourcePriority.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELPROPERTIES_PRIORITIES_RANDOM,
				Images.MODELPROPERTIES_PRIORITIES_CLIENT
		}));
		label.setLabelFor(secondaryResourcePriority);
		secondaryResourcePriority.setEnabled(!readOnly);
		switch (dialog.localResources.secondaryResourcePriority) {
		case RANDOM:
			secondaryResourcePriority.setSelectedIndex(0);
			break;
		case CLIENT_PRIORITY:
			secondaryResourcePriority.setSelectedIndex(1);
			break;
		}

		final JButton infoButton=new JButton(Language.tr("Resources.Usage"),Images.MODEL_ADD_STATION.getIcon());
		infoButton.addActionListener(e->	new ModelPropertiesDialogPageOperatorsUsageDialog(dialog,model));
		setupArea.add(infoButton);

		content.add(setupArea,BorderLayout.SOUTH);
	}

	@Override
	public void storeData() {
		switch (secondaryResourcePriority.getSelectedIndex()) {
		case 0:
			resourcesData.getResources().secondaryResourcePriority=ModelResources.SecondaryResourcePriority.RANDOM;
			break;
		case 1:
			resourcesData.getResources().secondaryResourcePriority=ModelResources.SecondaryResourcePriority.CLIENT_PRIORITY;
			break;
		}
		model.resources.setDataFrom(resourcesData.getResources());
	}

	/**
	 * Aktualisiert die Darstellung der Bedienergruppen.
	 */
	public void updateResourceTable() {
		resourcesData.updateTable();
	}
}
