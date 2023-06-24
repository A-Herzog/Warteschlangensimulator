/**
 * Copyright 2023 Alexander Herzog
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import systemtools.BaseDialog;
import ui.images.Images;

/**
 * Dialog zur Auswahl der Kundentypen für die eine Zählung erfolgen soll
 * @see CounterConditionPanel
 * @see CounterCondition
 * @author Alexander Herzog
 */
public class CounterConditionDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8270058319167994171L;

	/**
	 * Liste der Auswahlboxen für die verschiedenen Kundentypen
	 */
	private final List<JCheckBox> checkBoxes;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param allClientTypes	Liste aller Kundentypen
	 * @param selectedClientTypes	Liste der aktuell ausgewählten Kundentypen
	 */
	public CounterConditionDialog(final Component owner, final List<String> allClientTypes, final List<String> selectedClientTypes) {
		super(owner,Language.tr("Surface.CounterCondition.Panel.ClientTypes.Dialog"));

		/* GUI */
		addUserButton(Language.tr("Surface.CounterCondition.Panel.ClientTypes.SelectAll"),Images.EDIT_ADD.getIcon());
		addUserButton(Language.tr("Surface.CounterCondition.Panel.ClientTypes.SelectNone"),Images.EDIT_DELETE.getIcon());
		final JPanel content=createGUI(400,600,null);
		content.setLayout(new BorderLayout());

		/* Liste */
		final JPanel scrollPaneOuter=new JPanel(new BorderLayout());
		content.add(new JScrollPane(scrollPaneOuter));

		final JPanel scrollPaneInner=new JPanel();
		scrollPaneInner.setLayout(new BoxLayout(scrollPaneInner,BoxLayout.PAGE_AXIS));
		scrollPaneOuter.add(scrollPaneInner,BorderLayout.NORTH);

		checkBoxes=new ArrayList<>();
		final boolean allActive=selectedClientTypes.size()==0;
		for (String value: allClientTypes.stream().sorted().toArray(String[]::new)) {
			final boolean isActive=selectedClientTypes.contains(value) || allActive;
			final JCheckBox checkBox=new JCheckBox(value,isActive);
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			line.add(checkBox);
			scrollPaneInner.add(line);
			checkBoxes.add(checkBox);
		}

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(400,600);
		pack();
		setMaxSizeRespectingScreensize(400,600);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		checkBoxes.forEach(checkBox->checkBox.setSelected(nr==0));
	}

	/**
	 * Liefert die neue Liste der ausgewählten Kundentypen
	 * @return	Neue Liste der ausgewählten Kundentypen
	 */
	public List<String> getSelectedClientTypes() {
		boolean allChecked=true;
		for (JCheckBox checkBox: checkBoxes) if (!checkBox.isSelected()) {allChecked=false; break;}
		if (allChecked) return new ArrayList<>();

		return checkBoxes.stream().filter(checkBox->checkBox.isSelected()).map(checkBox->checkBox.getText()).collect(Collectors.toList());
	}
}
