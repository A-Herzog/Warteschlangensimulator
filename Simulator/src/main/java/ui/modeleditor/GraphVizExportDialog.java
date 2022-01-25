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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zeigt einen Dialog zur Auswahl der HTML- und GraphViz-Export-Optionen.
 * @author Alexander Herzog
 * @see GraphVizExport
 */
public class GraphVizExportDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2363308938846509858L;

	/**
	 * Modus "HTML-Canvas"
	 */
	private final JRadioButton modeHTML;

	/**
	 * Modus "D3 GraphViz"
	 */
	private final JRadioButton modeDOT;

	/**
	 * Option "Untermodelle einbeziehen"
	 */
	private final JCheckBox optionSubModel;

	/**
	 * Option "Statistikdaten mit ausgeben"
	 */
	private final JCheckBox optionStatistics;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param isHTML	Handelt es sich um einen Modell-Export im HTML-Format
	 * @param hasSubModel	Enthält das Modell Untermodelle?
	 * @param hasStatistics	Stehen Statistikdaten zur verfügung?
	 */
	public GraphVizExportDialog(final Component owner, final boolean isHTML, final boolean hasSubModel, final boolean hasStatistics) {
		super(owner,Language.tr("GraphVizExport.Options.Title"));

		/* GUI */
		final JPanel all=createGUI(null);
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		all.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;

		/* Modus "HTML-Canvas" */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(modeHTML=new JRadioButton("<html><body><b>"+Language.tr("GraphVizExport.Options.Mode.HTML")+"</b></body></html>"));
		if (isHTML) content.add(line);

		/* Modus "D3 GraphViz" */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		line.add(modeDOT=new JRadioButton("<html><body><b>"+Language.tr("GraphVizExport.Options.Mode.DOT")+"</b></body></html>"));
		if (isHTML) content.add(line);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(modeHTML);
		buttonGroup.add(modeDOT);
		modeHTML.setSelected(true);

		/* Option "Untermodelle einbeziehen" */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (isHTML) line.add(Box.createHorizontalStrut(20));
		line.add(optionSubModel=new JCheckBox(Language.tr("GraphVizExport.Options.Option.SubModels")));
		if (hasSubModel || hasStatistics) {
			content.add(line);
			optionSubModel.setEnabled(hasSubModel);
			optionSubModel.setSelected(hasSubModel);
			optionSubModel.addActionListener(e->modeDOT.setSelected(true));
		}

		/* Option "Statistikdaten mit ausgeben" */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (isHTML) line.add(Box.createHorizontalStrut(20));
		line.add(optionStatistics=new JCheckBox(Language.tr("GraphVizExport.Options.Option.Statistics")));
		if (hasSubModel || hasStatistics) {
			content.add(line);
			optionStatistics.setEnabled(hasStatistics);
			optionStatistics.setSelected(hasStatistics);
			optionStatistics.addActionListener(e->modeDOT.setSelected(true));
		}

		/* Dialog starten */
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/**
	 * Ist der GraphViz-HTML-Modus gewählt?
	 * @return	Ausgabe als in HTML eingebetteter GraphViz-Code
	 */
	public boolean isModeGraphViz() {
		return modeDOT.isSelected();
	}

	/**
	 * Ist die Option "Untermodelle einbeziehen" gewählt?
	 * @return	Liefert <code>true</code>, wenn die Option "Untermodelle einbeziehen" gewählt ist
	 */
	public boolean isIncludeSubModels() {
		return optionSubModel.isSelected();
	}

	/**
	 * Ist die Option "Statistikdaten mit ausgeben" gewählt?
	 * @return	Liefert <code>true</code>, wenn die Option "Statistikdaten mit ausgeben" gewählt ist
	 */
	public boolean isIncludeStatistics() {
		return optionStatistics.isSelected();
	}

	/**
	 * Prüft, ob ein Modell Untermodelle enthält.
	 * @param model	Zu prüfendes Modell
	 * @return	Liefert <code>true</code>, wenn das Modell Untermodelle enthält
	 */
	public static boolean hasSubModels(final EditModel model) {
		return model.surface.getElements().stream().filter(element->element instanceof ModelElementSub).findFirst().isPresent();
	}
}
