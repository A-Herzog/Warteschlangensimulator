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
package ui.generator;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.EditorPanel;
import ui.help.Help;
import ui.infopanel.InfoPanel;

/**
 * Zeigt einen Dialog an, in dem Einstellungen zur Erzeugung eines einfachen
 * Modells vorzunehmen.
 * @see ModelGeneratorPanel
 * @author Alexander Herzog
 */
public class ModelGeneratorDialog extends BaseDialog {
	private static final long serialVersionUID = -7913684726422421731L;

	/** Einstellungenbereich für den Modellgenerator */
	private final ModelGeneratorPanel setup;
	/** Vorschau auf das zu erstellende Modell */
	private final EditorPanel viewer;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ModelGeneratorDialog(final Component owner) {
		super(owner,Language.tr("ModelGenerator.Title"));

		/* GUI erstellen */
		final JPanel content=createGUI(()->Help.topicModal(this,"Generator"));
		content.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(content,InfoPanel.globalGenerator);
		final JPanel main=new JPanel(new BorderLayout());
		content.add(main,BorderLayout.CENTER);
		final JPanel left=new JPanel();
		main.add(left,BorderLayout.WEST);
		left.add(setup=new ModelGeneratorPanel(),BorderLayout.NORTH);
		main.add(viewer=new EditorPanel(this,setup.getModel(),true,false,false));
		setup.addModelChangeListener(()->{viewer.setModel(setup.getModel());});

		/* setup.getModel() liefert erst dann ein Modell ohne Überlappungen der Elemente, wenn setup ein sichtbares Panel ist. */
		SwingUtilities.invokeLater(()->viewer.setModel(setup.getModel()));

		/* Dialog starten */
		setMinSizeRespectingScreensize(1200,700);
		pack();
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Liefert das neu erstellte Modell
	 * @return	Neu erstelltes Modell oder <code>null</code>, wenn kein Modell erzeugt werden konnte.
	 */
	public EditModel getModel() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		return viewer.getModel();
	}
}

