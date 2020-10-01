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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementClientIcon}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementClientIcon
 */
public class ModelElementClientIconDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 3856457045122236178L;

	private DefaultComboBoxModel<JLabel> iconChooserList;
	private JComboBox<JLabel> iconChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementClientIcon}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementClientIconDialog(final Component owner, final ModelElementClientIcon element, final boolean readOnly) {
		super(owner,Language.tr("Surface.ClientIcon.Dialog.Title"),element,"ModelElementClientIcon",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationClientIcon;
	}

	@Override
	protected JComponent getContentPanel() {
		final AnimationImageSource imageSource=new AnimationImageSource();

		final JPanel content=new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label;

		content.add(label=new JLabel(Language.tr("Surface.ClientIcon.Dialog.IconForClient")+":"));
		content.add(iconChooser=new JComboBox<>());
		iconChooserList=imageSource.getIconsComboBox(element.getModel().animationImages);
		iconChooser.setModel(iconChooserList);
		iconChooser.setRenderer(new AnimationImageSource.IconComboBoxCellRenderer());
		iconChooser.setEnabled(!readOnly);
		label.setLabelFor(iconChooser);

		/* Icon-Combobox mit Vorgabe belegen */
		String icon=null;
		if (element instanceof ModelElementClientIcon) icon=((ModelElementClientIcon)element).getIcon();
		int index=0;
		if (icon!=null) for (int i=0;i<iconChooserList.getSize();i++) {
			String name=iconChooserList.getElementAt(i).getText();
			String value=AnimationImageSource.ICONS.getOrDefault(name,name);
			if (icon.equalsIgnoreCase(value)) {index=i; break;}
		}
		iconChooser.setSelectedIndex(index);

		return content;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementClientIcon) {
			String name=iconChooserList.getElementAt(iconChooser.getSelectedIndex()).getText();
			String s=AnimationImageSource.ICONS.getOrDefault(name,name);
			((ModelElementClientIcon)element).setIcon(s);
		}
	}
}