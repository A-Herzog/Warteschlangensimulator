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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import language.Language;
import systemtools.BaseDialog;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dialog zur Konfiguration, die während einer Animation wartende Kunden
 * an einer Station dargestellt werden sollen (nur max. 10 oder alle).
 * @author Alexander Herzog
 * @see ModelElementBaseDialog
 * @see ModelElementBox#hasQueue()
 * @see ModelElementBox#isDrawQueueAll()
 */
public class ModelElementBaseQueueDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6102981661769136402L;

	/**
	 * Auswahloption "Immer alle zeichnen"
	 */
	private final JRadioButton optionAll;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param drawQueueAll	Initialer Wert für "Soll die Warteschlange während der Animation immer vollständig dargestellt werden?"
	 */
	public ModelElementBaseQueueDialog(final Component owner, final boolean drawQueueAll) {
		super(owner,Language.tr("Editor.DialogBase.DisplayQueue.Title"));

		/* GUI */
		final JPanel content=createGUI(600,400,null);
		content.setLayout(new BorderLayout());
		final JPanel setupArea=new JPanel();
		content.add(setupArea,BorderLayout.NORTH);
		setupArea.setLayout(new BoxLayout(setupArea,BoxLayout.PAGE_AXIS));

		JPanel line;

		/* Maximal 10 Kunden zeichnen */
		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JRadioButton optionLimited=new JRadioButton(Language.tr("Editor.DialogBase.DisplayQueue.Limited"),!drawQueueAll);
		line.add(optionLimited);

		/* Immer alle zeichnen */
		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		optionAll=new JRadioButton(Language.tr("Editor.DialogBase.DisplayQueue.Full"),drawQueueAll);
		line.add(optionAll);

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Soll die Warteschlange während der Animation immer vollständig dargestellt werden?
	 * @return	Soll die Warteschlange während der Animation immer vollständig dargestellt werden?
	 */
	public boolean getDrawQueueAll() {
		return optionAll.isSelected();
	}
}
