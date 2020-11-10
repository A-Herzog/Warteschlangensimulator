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
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import ui.quickaccess.JPlaceholderTextField;

/**
 * Bietet ein Editor-Panel an, in dem die Namen der Kundentypen,
 * die beim Verlassen des Elements eingestellt werden sollen,
 * angegeben werden können.
 * @author Alexander Herzog
 * @see ModelElementDecideDialog
 * @see ModelElementDuplicateDialog
 */
public class NewClientTypesPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7672837912637436515L;

	/**
	 * Liste der Namens-Eingabefelder
	 */
	private final List<JTextField> nameEdit;

	/**
	 * Konstruktor der Klasse
	 * @param newClientTypeNames	Liste der bisherigen Kundentypennamen. Die Länge der Liste bestimmt gleichzeitig, wie viele Felder vorgesehen werden sollen.
	 * @param readOnly	Nur-Lese-Status
	 */
	public NewClientTypesPanel(final List<String> newClientTypeNames, final boolean readOnly) {
		super();
		setLayout(new BorderLayout());
		JPanel content=new JPanel(new GridLayout((newClientTypeNames==null)?1:newClientTypeNames.size(),2));
		add(content,BorderLayout.NORTH);

		nameEdit=new ArrayList<>();
		if (newClientTypeNames!=null) for (int i=0;i<newClientTypeNames.size();i++) {
			final String value=(newClientTypeNames.get(i)==null)?"":newClientTypeNames.get(i).trim();
			final JLabel label=new JLabel(String.format(Language.tr("NewClientTypeEdit.Edge")+":",i+1));
			content.add(label);
			final JPlaceholderTextField field=new JPlaceholderTextField(value);
			field.setEditable(!readOnly);
			label.setLabelFor(field);
			field.setPlaceholder(Language.tr("NewClientTypeEdit.InfoShort"));
			field.setToolTipText(Language.tr("NewClientTypeEdit.Info"));
			content.add(field);
			nameEdit.add(field);
		}
	}

	/**
	 * Liefert die Nutzereinstellungen zu den Kundentypnamen zurück
	 * @return	Neue Liste mit den neuen Kundentypnamen
	 */
	public List<String> getNewClientTypeNames() {
		return nameEdit.stream().map(edit->edit.getText().trim()).collect(Collectors.toList());
	}
}
