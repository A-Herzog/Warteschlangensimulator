/**
 * Copyright 2024 Alexander Herzog
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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.modeleditor.ModelSurfaceArranger;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dialog zur Auswahl der Optionen und dann folgend zur Durchführung
 * der Neuanordnung der Elemente auf der Zeichenfläche.
 * @see ModelSurfaceArranger
 */
public class ModelSurfaceArrangerDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-485039729763912713L;

	/**
	 * System zur Neuanordnung der Elemente auf der Zeichenfläche
	 */
	private final ModelSurfaceArranger arranger;

	/**
	 * Option: Ausrichtung nur der ausgewählten Stationen (im Gegensatz zu: allen Stationen)
	 */
	private final JRadioButton optionSelected;

	/**
	 * Option: Vollständige Neuausrichtung (im Gegensatz zu: nur am Raster ausrichten)
	 */
	private final JRadioButton optionFull;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell dessen Hauptzeichenflächenstationen neu ausgerichtet werden sollen
	 * @param selectedIDs	Liste der IDs der im Editor momentan ausgewählten Stationen
	 */
	public ModelSurfaceArrangerDialog(final Component owner, final EditModel model, final Set<Integer> selectedIDs) {
		super(owner,Language.tr("ArrangeDialog.Title"));

		arranger=new ModelSurfaceArranger(model.surface,selectedIDs);

		final int selectedStationsCount=arranger.getSelectedStations().size();

		JPanel line;
		ButtonGroup buttonGroup;

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"EditorArrangeStations"));
		all.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		all.add(content,BorderLayout.CENTER);

		/* Stationen wählen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("ArrangeDialog.Elements")+"</b></body></html>"));

		/* Alle Stationen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JRadioButton optionAll=new JRadioButton(Language.tr("ArrangeDialog.Elements.All"),selectedStationsCount<2);
		line.add(optionAll);

		/* Nur gewählte Stationen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		optionSelected=new JRadioButton(Language.tr("ArrangeDialog.Elements.Selected"),selectedStationsCount>=2);
		optionSelected.setEnabled(selectedStationsCount>0);
		line.add(optionSelected);

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionAll);
		buttonGroup.add(optionSelected);

		/* Modus wählen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("ArrangeDialog.Mode")+"</b></body></html>"));

		/* Am Raster ausrichten */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JRadioButton optionGrid=new JRadioButton(Language.tr("ArrangeDialog.Mode.Grid"),true);
		line.add(optionGrid);

		/* Nur gewählte Stationen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		optionFull=new JRadioButton(Language.tr("ArrangeDialog.Mode.Full"));
		line.add(optionFull);

		buttonGroup=new ButtonGroup();
		buttonGroup.add(optionGrid);
		buttonGroup.add(optionFull);

		/* Hinweis auf Dekoelemente */
		if (arranger.hasDecorations()) {
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(new JLabel("<html><body><b>"+Language.tr("ArrangeDialog.NoteTitle")+"</b></body></html>"));
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(new JLabel("<html><body>"+Language.tr("ArrangeDialog.NoteContent")+"</body></html>"));
		}

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected boolean checkData() {
		final Set<ModelElementBox> stations=(optionSelected.isSelected())?arranger.getSelectedStations():arranger.getAllStations();
		final ModelSurfaceArranger.Mode mode=(optionFull.isSelected())?ModelSurfaceArranger.Mode.FULL_ALIGN:ModelSurfaceArranger.Mode.GRID_ALIGN;
		final boolean ok=arranger.arrange(stations,mode);
		if (!ok) MsgBox.error(this,Language.tr("ArrangeDialog.ErrorTitle"),Language.tr("ArrangeDialog.ErrorContent"));
		return ok;
	}
}
