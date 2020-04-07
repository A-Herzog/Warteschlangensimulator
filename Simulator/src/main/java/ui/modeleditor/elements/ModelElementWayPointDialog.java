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
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import tools.JTableExt;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementWayPoint}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementWayPoint
 */
public class ModelElementWayPointDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = -8966929906311201419L;

	private DefaultComboBoxModel<JLabel> iconChooserList;
	private JComboBox<JLabel> iconChooser;
	private WayPointTableModel tableModel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementWayPoint}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementWayPointDialog(final Component owner, final ModelElementWayPoint element, final boolean readOnly) {
		super(owner,Language.tr("Surface.WayPoint.Dialog.Title"),element,"ModelElementWayPoint",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(750,650);
		pack();
		setResizable(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationWayPoint;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		final AnimationImageSource imageSource=new AnimationImageSource();

		JPanel sub;
		JLabel label;

		/* Icon-Combobox */
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.WayPoint.Dialog.Icon")+":"));
		sub.add(iconChooser=new JComboBox<JLabel>());
		iconChooserList=imageSource.getIconsComboBox(element.getModel().animationImages);
		iconChooser.setModel(iconChooserList);
		iconChooser.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		label.setLabelFor(iconChooser);

		/* Icon-Combobox mit Vorgabe belegen */
		int index=0;
		final String icon=((ModelElementWayPoint)element).getIcon();
		if (icon!=null) for (int i=0;i<iconChooserList.getSize();i++) {
			String name=iconChooserList.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(name,name);
			if (icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		iconChooser.setSelectedIndex(index);
		iconChooser.setEnabled(!readOnly);

		/* Datensätze */
		final JTableExt table=new JTableExt();
		table.setModel(tableModel=new WayPointTableModel(table,((ModelElementWayPoint)element).getRecords(),element.getModel().surface,readOnly));
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setIsPanelCellTable(3);
		table.getColumnModel().getColumn(2).setMaxWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(100);
		table.getColumnModel().getColumn(3).setMaxWidth(200);
		table.getColumnModel().getColumn(3).setMinWidth(200);
		table.setEnabled(!readOnly);
		content.add(new JScrollPane(table),BorderLayout.CENTER);

		return content;
	}

	@Override
	protected void storeData() {
		super.storeData();

		/* Icon */
		final String icon=iconChooserList.getElementAt(iconChooser.getSelectedIndex()).getText();
		((ModelElementWayPoint)element).setIcon(AnimationImageSource.ICONS.getOrDefault(icon,icon));

		/* Datensätze */
		tableModel.storeData();
	}
}