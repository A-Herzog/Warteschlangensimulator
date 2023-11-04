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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelDark;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.EditorPanel;
import ui.help.Help;
import ui.infopanel.InfoPanel;
import ui.tools.FlatLaFHelper;

/**
 * Zeigt einen Dialog an, in dem Einstellungen zur Erzeugung eines einfachen
 * Modells vorzunehmen.
 * @see ModelGeneratorPanelOpen
 * @author Alexander Herzog
 */
public class ModelGeneratorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -7913684726422421731L;

	/**
	 * Auswahl des Modelltyps
	 */
	private final JComboBox<String> setupTypeSelect;

	/**
	 * Bereich zur Anzeige der Einstellungen für das Modell
	 */
	private final JPanel setupArea;

	/**
	 * Layout-Objekt für {@link #setupArea}
	 */
	private final CardLayout setupAreaLayout;

	/**
	 * Einstellungenbereiche für die verschiedenen Typen
	 */
	private final List<ModelGeneratorPanelBase> setups;

	/**
	 * Vorschau auf das zu erstellende Modell
	 */
	private final EditorPanel viewer;

	/**
	 * Zeichenflächen-Zoomfaktor beim Aufruf des Dialogs
	 */
	private final double lastZoom;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ModelGeneratorDialog(final Component owner) {
		super(owner,Language.tr("ModelGenerator.Title"));

		lastZoom=SetupData.getSetup().lastZoom;

		/* GUI erstellen */
		final JPanel content=createGUI(()->Help.topicModal(this,"Generator"));
		content.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(content,InfoPanel.globalGenerator);
		final JPanel main=new JPanel(new BorderLayout());
		content.add(main,BorderLayout.CENTER);

		/* Einstellungenbereich */
		final JPanel left=new JPanel();
		left.setLayout(new BoxLayout(left,BoxLayout.PAGE_AXIS));
		main.add(left,BorderLayout.WEST);

		/* Modelltyp-Auswahl */
		JPanel line;
		left.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("ModelGenerator.ModelType")+"</b></body></html>"));
		left.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		setupTypeSelect=addComboBox(line,Language.tr("ModelGenerator.NetType")+":",new String[] {});

		/* Editorbereich */
		left.add(setupArea=new JPanel(setupAreaLayout=new CardLayout()),BorderLayout.NORTH);
		setups=new ArrayList<>();

		/* Modelltypen */
		addSetup(new ModelGeneratorPanelOpen());
		addSetup(new ModelGeneratorPanelClosed());
		addSetup(new ModelGeneratorPanelLoad());

		/* Modellansicht */
		main.add(viewer=new EditorPanel(this,getEditModel(),true,true,false,false));
		viewer.setSavedViewsButtonVisible(false);

		/* setup.getModel() liefert erst dann ein Modell ohne Überlappungen der Elemente, wenn setup ein sichtbares Panel ist. */
		viewer.setZoom(0.8);
		SwingUtilities.invokeLater(()->{
			viewer.setModel(getEditModel());
			viewer.setZoom(0.8);
		});

		/* Mit Auswahlbox verknüpfen */
		setupTypeSelect.addActionListener(e->{
			setupAreaLayout.show(setupArea,""+setupTypeSelect.getSelectedIndex());
			viewer.setModel(getEditModel());
		});
		setupAreaLayout.show(setupArea,"0");

		/* Dialog starten */
		setMinSizeRespectingScreensize(1440,800);
		pack();
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Fügt einen neuen Modelltyp zum Dialog hinzu.
	 * @param setup	Modelltyp-Einstellungen-Panel
	 */
	private void addSetup(final ModelGeneratorPanelBase setup) {
		setupTypeSelect.addItem(setup.getTypeName());
		setups.add(setup);
		setup.addModelChangeListener(()->{viewer.setModel(getEditModel());});

		final JPanel outer=new JPanel(new BorderLayout());
		outer.add(setup,BorderLayout.NORTH);
		setupArea.add(outer,""+(setups.size()-1));
	}

	/**
	 * Liefert das aktuelle Modell aus dem Generator-Panel
	 * @return	Modell aus dem Generator-Panel
	 */
	private EditModel getEditModel() {
		final int index=setupTypeSelect.getSelectedIndex();
		if (index<0) return null;
		final EditModel model=setups.get(index).getModel();
		if (FlatLaFHelper.isDark()) EditModelDark.processModel(model,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
		return model;
	}

	/**
	 * Liefert das neu erstellte Modell
	 * @return	Neu erstelltes Modell oder <code>null</code>, wenn kein Modell erzeugt werden konnte.
	 */
	public EditModel getModel() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		return viewer.getModel();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b==false) {
			final SetupData setup=SetupData.getSetup();
			setup.lastZoom=lastZoom;
			setup.saveSetup();
		}
	}
}

