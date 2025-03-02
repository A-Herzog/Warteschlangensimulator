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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResources;

/**
 * Ermöglicht die Auswahl oder auch die Erstellung einer Bedienergruppe, die notwendig
 * für die Bedienung von Kunden an einer bestimmten Station ist.
 * @author Alexander Herzog
 * @see ResourceTableModel
 */
public class ResourceTableModelDialog1 extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -31158625542021496L;

	/**
	 * Ressourcenliste, in der alle verfügbaren Bedienergruppen verzeichnet sind. (Die Liste wird möglicher Weise durch diesen Dialog direkt erweitert.)
	 */
	private final ModelResources resources;

	/** Option: Bestehende Bedienergruppe verwenden */
	private JRadioButton optionExisting;
	/** Option: Neue Bedienergruppe anlegen */
	private JRadioButton optionNew;
	/** Auswahlbox zur Wahl der zu verwendenden Bedienergruppe im Fall {@link #optionExisting} */
	private JComboBox<String> selectGroup;
	/** Info-Label mit Angaben zur Größe der in {@link #selectGroup} gewählten Gruppe */
	private JLabel selectGroupInfo;
	/** Eingabefeld für einen Namen für eine neue Bedienergruppe im Fall {@link #optionNew} */
	private final JTextField textGroupName;
	/** Eingabefeld für die Gruppengröße der neuen Bedienergruppe im Fall {@link #optionNew} */
	private final JTextField textGroupSize;

	/**
	 * Konstruktor der Klasse <code>ResourceTableModelDialog</code>
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param help	Wird aufgerufen, wenn der Nutzer auf "Hilfe" klickt
	 * @param map	Bisher verwendete Bedienergruppen
	 * @param index	Index der aktuellen Gruppe in der <code>map</code>-Struktur (-1, wenn es sich um einen neuen Eintrag handeln soll)
	 * @param resources	Ressourcenliste, in der alle verfügbaren Bedienergruppen verzeichnet sind. (Die Liste wird möglicher Weise durch diesen Dialog direkt erweitert.)
	 * @param model	Modell aus dem die Icons für das Bediener ausgelesen werden sollen
	 * @param allowSelectExisting	Nur neue Gruppen anlegen (<code>false</code>) oder auch Möglichkeit bestehende Gruppen auszuwählen (<code>true</code>)
	 */
	public ResourceTableModelDialog1(final Component owner, final Runnable help, final Map<String,Integer> map, final int index, final ModelResources resources, final EditModel model, final boolean allowSelectExisting) {
		super(owner,Language.tr("Surface.Resource.EditName.Dialog.Title"),false);
		this.resources=resources;

		JPanel panel;

		final JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		if (allowSelectExisting) {
			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(optionExisting=new JRadioButton(Language.tr("Surface.Resource.EditName.Dialog.UseExisting")));
			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(selectGroup=new JComboBox<>());
			selectGroup.addActionListener(e->updateGroupSizeInfo());
			panel.add(selectGroupInfo=new JLabel());
			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(optionNew=new JRadioButton(Language.tr("Surface.Resource.EditName.Dialog.AddNew")+":"));
		} else {
			content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			panel.add(new JLabel(Language.tr("Surface.Resource.EditName.Dialog.AddNew")+":"));
		}
		content.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		panel.add(new JLabel(Language.tr("Surface.Resource.EditName.Dialog.AddNew.Name")+":"));
		panel.add(textGroupName=new JTextField(getFreeResourceName(),30));
		ModelElementBaseDialog.addUndoFeature(textGroupName);
		textGroupName.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (optionNew!=null) optionNew.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {if (optionNew!=null) optionNew.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {if (optionNew!=null) optionNew.setSelected(true); checkData(false);}
		});
		panel.add(new JLabel(Language.tr("Surface.Resource.EditName.Dialog.AddNew.Size")+":"));
		panel.add(textGroupSize=new JTextField("1",5));
		ModelElementBaseDialog.addUndoFeature(textGroupSize);
		textGroupSize.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (optionNew!=null) optionNew.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {if (optionNew!=null) optionNew.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {if (optionNew!=null) optionNew.setSelected(true); checkData(false);}
		});

		if (allowSelectExisting) {
			optionExisting.addActionListener(e->checkData(false));
			optionNew.addActionListener(e->checkData(false));

			ButtonGroup buttonGroup=new ButtonGroup();
			buttonGroup.add(optionExisting);
			buttonGroup.add(optionNew);

			List<String> usedGroups=new ArrayList<>();
			for (Map.Entry<String,Integer> entry: map.entrySet()) usedGroups.add(entry.getKey());

			int availableIndex=-1;
			List<String> availableGroups=new ArrayList<>();
			for (String group: resources.list()) {
				int nr=-1;
				for (int i=0;i<usedGroups.size();i++) if (i!=index && usedGroups.get(i).equalsIgnoreCase(group)) {nr=i; break;}
				if (nr<0) {
					availableGroups.add(group);
					if (index>=0 && group.equalsIgnoreCase(usedGroups.get(index))) {availableIndex=availableGroups.size()-1;}
				}
			}

			if (availableGroups.size()==0) {
				optionExisting.setEnabled(false);
				optionNew.setSelected(true);
				selectGroup.setEnabled(false);
				if (index<0) {
					textGroupName.setText(resources.getNextAvailableResouceName());
				} else {
					textGroupName.setText(usedGroups.get(index));
				}
			} else {
				if (availableIndex>=0) {
					optionExisting.setSelected(true);
					optionNew.setSelected(false);
					selectGroup.setModel(new DefaultComboBoxModel<>(availableGroups.toArray(String[]::new)));
					selectGroup.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildResourceTypeIcons(availableGroups,model)));
					selectGroup.setSelectedIndex(availableIndex);
					textGroupName.setText(resources.getNextAvailableResouceName());
				} else {
					selectGroup.setModel(new DefaultComboBoxModel<>(availableGroups.toArray(String[]::new)));
					selectGroup.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildResourceTypeIcons(availableGroups,model)));
					if (index<0) {
						optionExisting.setSelected(true);
						optionNew.setSelected(false);
						textGroupName.setText(resources.getNextAvailableResouceName());
					} else {
						optionExisting.setSelected(false);
						optionNew.setSelected(true);
						textGroupName.setText(usedGroups.get(index));
					}
				}
			}

			updateGroupSizeInfo();
		}


		checkData(false);
		pack();
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Liefert den nächsten verfügbaren Gruppennamen.
	 * @return	Möglicher Name für eine neue Bedienergruppe
	 */
	private String getFreeResourceName() {
		final String baseName=Language.tr("Resources.Group.DefaultName");
		String name=baseName;
		int nr=0;
		while (resources.getNoAutoAdd(name)!=null) {
			nr++;
			name=baseName+" "+nr;
		}
		return name;
	}

	/**
	 * Aktualisiert nach einer Wahl in {@link #selectGroup}
	 * die zugehörige Anzeige der Gruppengröße in {@link #updateGroupSizeInfo()}
	 * @see #selectGroup
	 * @see #selectGroupInfo
	 */
	private void updateGroupSizeInfo() {
		int max=-1;

		if (selectGroup.isEnabled() && selectGroup.getSelectedItem() instanceof String) {
			final String name=(String)selectGroup.getSelectedItem();
			if (resources.get(name).getMode()==ModelResource.Mode.MODE_NUMBER) max=resources.get(name).getCount();
		}

		selectGroupInfo.setVisible(max>0);
		if (max==1) {
			selectGroupInfo.setText(Language.tr("Surface.Resource.EditName.Dialog.AddNew.InfoSingle"));
		} else {
			selectGroupInfo.setText(String.format(Language.tr("Surface.Resource.EditName.Dialog.AddNew.InfoPlural"),max));
		}
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		textGroupName.setBackground(NumberTools.getTextFieldDefaultBackground());
		textGroupSize.setBackground(NumberTools.getTextFieldDefaultBackground());
		if (optionNew==null || optionNew.isSelected()) {
			final String name=textGroupName.getText().trim();

			if (name.isEmpty()) {
				textGroupName.setBackground(Color.red);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Resource.EditName.Dialog.AddNew.ErrorEmpty.Title"),Language.tr("Surface.Resource.EditName.Dialog.AddNew.ErrorEmpty.Info"));
					return false;
				}
			}

			for (String resource: resources.list()) if (resource.equalsIgnoreCase(name)) {
				textGroupName.setBackground(Color.red);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Resource.EditName.Dialog.AddNew.Error.Title"),String.format(Language.tr("Surface.Resource.EditName.Dialog.AddNew.Error.Info"),name));
					return false;
				}
			}

			final Long L=NumberTools.getPositiveLong(textGroupSize,true);
			if (L==null) {
				textGroupSize.setBackground(Color.red);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Resource.EditName.Dialog.AddNew.ErrorSize.Title"),String.format(Language.tr("Surface.Resource.EditName.Dialog.AddNew.ErrorSize.Info"),textGroupSize.getText()));
					return false;
				}
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		if (optionNew==null || optionNew.isSelected()) {
			final Long L=NumberTools.getPositiveLong(textGroupSize,true);
			resources.add(new ModelResource(textGroupName.getText().trim(),L.intValue()));
		}
	}

	/**
	 * Liefert den gewählten Namen der Bedienergruppe.<br>
	 * (Sofern ein solcher Name noch nicht in der Ressourcenliste existierte, wird dieser automatisch hinzugefügt.)
	 */
	@Override
	public String getName() {
		if (optionNew==null || optionNew.isSelected()) return textGroupName.getText(); else return (String)selectGroup.getSelectedItem();
	}
}
