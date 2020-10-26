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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import language.Language;
import systemtools.BaseDialog;

/**
 * Dialog zum Sortieren der Modelle
 * für die Parameter-Variationsstudien-Funktion
 * @author Alexander Herzog
 */
public class ParameterCompareSortDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6037938544402526106L;

	/** Parameter-Vergleichs-Einstellungen */
	private final ParameterCompareSetup setup;

	/** Optionen zur Auswahl wonach sortiert werden soll */
	private final List<JRadioButton> radioButtons;

	/** Sortierung aufsteigend? */
	private final JCheckBox sortAscending;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param setup	Parameter-Vergleichs-Einstellungen
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareSortDialog(final Component owner, final ParameterCompareSetup setup, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Table.SortModels.Title"));
		this.setup=setup;

		/* GUI */

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		final JPanel main=new JPanel();
		content.add(main,BorderLayout.NORTH);
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));

		JPanel line;
		radioButtons=new ArrayList<>();
		final ButtonGroup buttonGroup=new ButtonGroup();

		for (ParameterCompareSetupValueInput input: setup.getInput()) {
			final JRadioButton radioButton=new JRadioButton(String.format(Language.tr("ParameterCompare.Table.SortModels.SortBy"),input.getName()));
			radioButtons.add(radioButton);
			buttonGroup.add(radioButton);
			main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(radioButton);
		}
		radioButtons.get(0).setSelected(true);

		main.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(sortAscending=new JCheckBox(Language.tr("ParameterCompare.Table.SortModels.SortAscending"),true));

		/* Dialog starten */

		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	@Override
	protected void storeData() {
		int sortIndex=0;
		for (int i=0;i<radioButtons.size();i++) if (radioButtons.get(i).isSelected()) {sortIndex=i; break;}
		final String sortInputName=setup.getInput().get(sortIndex).getName();

		final boolean ascending=sortAscending.isSelected();

		List<ParameterCompareSetupModel> newList=setup.getModels().stream().sorted((m1,m2)->{
			final Double D1=m1.getInput().get(sortInputName);
			final Double D2=m2.getInput().get(sortInputName);
			final double value1=(D1==null)?0.0:D1.doubleValue();
			final double value2=(D2==null)?0.0:D2.doubleValue();
			int result=0;
			if (value1>value2) result=1; else {if (value2>value1) result=-1;}
			return ascending?result:-result;
		}).collect(Collectors.toList());

		setup.getModels().clear();
		setup.getModels().addAll(newList);
	}
}
