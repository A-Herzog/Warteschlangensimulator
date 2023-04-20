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
package ui.generator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import language.Language;
import systemtools.BaseDialog;

/**
 * Zeigt einen Dialog zur Auswahl der Parameter zur Erzeugung eines
 * großen Belastungsmodells an.
 * @author Alexander Herzog
 * @see ModelGeneratorPanel#getLargeModel(long, int, boolean)
 */
public class ModelGeneratorLargeDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=3987619475761744570L;

	/**
	 * Auswahlfeld: Anzahl an Kundenankünften
	 */
	private final SpinnerNumberModel clientCount;

	/**
	 * Auswahlfeld: Anzahl an Stationen
	 */
	private final SpinnerNumberModel stationCount;

	/**
	 * Option: Bedienstationen (statt Verzögerungsstationen) erzeugen
	 */
	private final JRadioButton optionProcess;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element zur Ausrichtung des Dialogs
	 */
	public ModelGeneratorLargeDialog(final Component owner) {
		super(owner,Language.tr("ModelGenerator.LargeModel.Title"));

		/* GUI */
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());
		final JPanel setupArea=new JPanel();
		content.add(setupArea,BorderLayout.NORTH);
		setupArea.setLayout(new BoxLayout(setupArea,BoxLayout.PAGE_AXIS));

		/* Anzahl an Kundenankünften */
		clientCount=addSpinner(setupArea,Language.tr("ModelGenerator.LargeModel.ArrivalCount"),1,1_000_000_000,100_000);

		/* Anzahl an Stationen */
		stationCount=addSpinner(setupArea,Language.tr("ModelGenerator.LargeModel.StationCount"),1,10_000,2_500);

		/* Stationstypen */
		JPanel line;

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JRadioButton optionDelay=new JRadioButton(Language.tr("ModelGenerator.LargeModel.StationType.Delay"));
		optionDelay.setSelected(true);
		line.add(optionDelay);

		setupArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		optionProcess=new JRadioButton(Language.tr("ModelGenerator.LargeModel.StationType.Process"));
		line.add(optionProcess);

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionDelay);
		buttonGroup.add(optionProcess);

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Auswahlfeld einfügen
	 * @param panel	Panel in das das Auswahlfeld eingefügt werden soll
	 * @param text	Beschriftung des Auswahlfeldes
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param value	Initialer Wert
	 * @return	Neues Auswahlfeld
	 */
	private SpinnerNumberModel addSpinner(final JPanel panel, final String text, final int min, final int max, final int value) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JLabel label=new JLabel(text+":");
		line.add(label);
		final JSpinner spinner=new JSpinner(new SpinnerNumberModel(value,min,max,1));
		line.add(spinner);
		label.setLabelFor(spinner);
		return (SpinnerNumberModel)spinner.getModel();
	}

	/**
	 * Liefert die Anzahl an Kundenankünften.
	 * @return	Anzahl an Kundenankünften
	 */
	public long getClientCount() {
		return clientCount.getNumber().longValue();
	}

	/**
	 * Liefert die Anzahl an Stationen.
	 * @return	Anzahl an Stationen
	 */
	public int getStationCount() {
		return stationCount.getNumber().intValue();
	}

	/**
	 * Soll Bedienstationen (statt Verzögerungsstationen) generiert werden?
	 * @return	Bedienstationen erzeugen?
	 */
	public boolean isUseProcessStations() {
		return optionProcess.isSelected();
	}
}
