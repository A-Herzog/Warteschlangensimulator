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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.RandomGeneratorMode;
import mathtools.distribution.tools.ThreadLocalRandomGenerator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialogseite "Simulationssystem"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageInfo extends ModelPropertiesDialogPage {
	/**
	 * Modus für Zufallszahlengenerator
	 */
	private JComboBox<String> randomMode;

	/**
	 * Option "Fester Startwert für Zufallszahlengenerator"
	 */
	private JCheckBox useFixedSeed;

	/**
	 * Eingabefeld "Startwert"
	 */
	private JTextField fixedSeed;

	/**
	 * Schaltfläche zum Zurücksetzen des Modus für Zufallszahlengenerator
	 */
	private JButton randomModeResetButton;

	/**
	 * Schaltfläche für Hinweis auf unpassenden Generator
	 */
	private JButton randomModeInfoButton;

	/**
	 * Eingabefeld für die Anzahl an Zeitschritten pro Sekunde
	 */
	private JTextField timeStepsPerSecond;

	/**
	 * Schaltfläche zum Zurücksetzen der Anzahl an Zeitschritten pro Sekunde auf den Vorgabewert
	 */
	private JButton timeStepsPerSecondResetButton;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageInfo(final ModelPropertiesDialog dialog, final EditModel model, final boolean readOnly, final Runnable help) {
		super(dialog,model,readOnly,help);
	}

	/**
	 * Liefert die Modellinformationen als html-Code
	 * @return Modellinformationen
	 */
	private String getInfo() {
		final StartAnySimulator.PrepareError error=StartAnySimulator.testModel(model,null);
		final StringBuilder result=new StringBuilder();
		result.append("<html><body style=\"margin-top: 5px;\">");
		if (error!=null) {
			result.append("<p style=\"margin-bottom: 10px\">"+Language.tr("Editor.Dialog.Tab.SimulationSystem.Error")+"</p>");
			result.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.ErrorInfo")+":<br><b>"+error.error+"</b></p>");
		} else {
			result.append("<p style=\"margin-bottom: 10px\">"+Language.tr("Editor.Dialog.Tab.SimulationSystem.Ok")+"</p>");

			java.util.List<String> infoSingleCore=model.getSingleCoreReason();
			if (infoSingleCore==null || infoSingleCore.size()==0) {
				result.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.MultiCoreOk")+"</p>");
			} else {
				result.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.SingleCoreOnly")+":</p>");
				result.append("<ul>");
				for (String line: infoSingleCore) result.append("<li>"+line+"</li>");
				result.append("</ul>");
			}
		}

		if (model.repeatCount>1) {
			final String infoNoRepeat=model.getNoRepeatReason();
			if (infoNoRepeat==null) {
				result.append("<p>"+String.format(Language.tr("Editor.Dialog.Tab.SimulationSystem.RepeatOk"),model.repeatCount)+"</p>");
			} else {
				result.append("<p>"+String.format(Language.tr("Editor.Dialog.Tab.SimulationSystem.RepeatNotOk"),model.repeatCount)+"<br>"+infoNoRepeat+"</p>");
			}
		}

		result.append("</body></html>");

		return result.toString();
	}

	@Override
	public void build(JPanel content) {
		JPanel sub;
		Object[] data;

		content.setLayout(new BorderLayout());
		JPanel lines;
		content.add(lines=new JPanel(),BorderLayout.NORTH);

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		/* Zufallszahlengenerator */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.RandomMode")+":</b></html>"));
		sub.add(Box.createHorizontalStrut(1));
		sub.add(randomMode=new JComboBox<>(RandomGeneratorMode.getAllNames()));
		randomMode.setRenderer(new IconListCellRenderer(Stream.of(RandomGeneratorMode.values()).map(mode->{
			if (mode==RandomGeneratorMode.defaultRandomGeneratorMode) return Images.GENERAL_ON;
			return mode.isGoodForSimulation?Images.MSGBOX_YES:Images.GENERAL_OFF;
		}).toArray(Images[]::new)));
		randomMode.setEnabled(!readOnly);
		randomMode.setSelectedIndex(RandomGeneratorMode.getIndex(model.randomMode));
		randomMode.addActionListener(e->checkRandomMode());

		randomModeResetButton=new JButton(Images.EDIT_UNDO.getIcon());
		randomModeResetButton.setToolTipText(Language.tr("Editor.Dialog.Tab.SimulationSystem.RandomMode.ResetToDefault"));
		randomModeResetButton.addActionListener(e->{
			randomMode.setSelectedIndex(0);
			checkRandomMode();
		});
		sub.add(randomModeResetButton);

		randomModeInfoButton=new JButton(Images.GENERAL_WARNING.getIcon());
		randomModeInfoButton.setToolTipText(Language.tr("Editor.Dialog.Tab.SimulationSystem.RandomMode.NotGood"));
		randomModeInfoButton.addActionListener(e->{
			MsgBox.info(dialog,Language.tr("Editor.Dialog.Tab.SimulationSystem.RandomMode.NotGood"),Language.tr("Editor.Dialog.Tab.SimulationSystem.RandomMode.NotGoodInfo"));
		});
		randomModeInfoButton.setVisible(false);
		sub.add(randomModeInfoButton);

		checkRandomMode();

		lines.add(Box.createVerticalStrut(25));

		/* Fester Startwert für Zufallszahlengenerator */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(useFixedSeed=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed")+"</b></html>"));
		useFixedSeed.setEnabled(!readOnly);
		useFixedSeed.setSelected(model.useFixedSeed);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.Value")+":",""+model.fixedSeed,20);
		sub=(JPanel)data[0];
		lines.add(sub);
		fixedSeed=(JTextField)data[1];
		fixedSeed.setEnabled(!readOnly);
		addKeyListener(fixedSeed,()->{
			useFixedSeed.setSelected(true);
			checkFixedSeed();
		});
		if (!readOnly) {
			final JButton fixedSeedButton=new JButton(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.RandomButton"));
			fixedSeedButton.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.RandomButton.Hint"));
			fixedSeedButton.setIcon(Images.MODELPROPERTIES_SIMULATION_RANDOM_SEED.getIcon());
			sub.add(fixedSeedButton);
			fixedSeedButton.addActionListener(e->{
				fixedSeed.setText(""+Math.abs(new ThreadLocalRandomGenerator().nextLong()));
				useFixedSeed.setSelected(true);
			});
		}

		lines.add(Box.createVerticalStrut(25));

		/* Zwischenüberschrift: "Anzahl an Zeitschritten pro Sekunde" */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.TimeStepsPerSecond.Title")+"</b></html>"));

		lines.add(sub=new JPanel(new BorderLayout()));
		sub.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		sub.add(new JLabel("<html>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.TimeStepsPerSecond.Info")+"</html>"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.SimulationSystem.TimeStepsPerSecond.Input")+":",""+model.timeStepsPerSecond,10);
		sub=(JPanel)data[0];
		lines.add(sub);
		timeStepsPerSecond=(JTextField)data[1];
		timeStepsPerSecond.setEnabled(!readOnly);
		addKeyListener(timeStepsPerSecond,()->checkTimeStepsPerSecond());

		timeStepsPerSecondResetButton=new JButton(Images.EDIT_UNDO.getIcon());
		timeStepsPerSecondResetButton.setToolTipText(Language.tr("Editor.Dialog.Tab.SimulationSystem.TimeStepsPerSecond.ResetToDefault"));
		timeStepsPerSecondResetButton.addActionListener(e->{
			timeStepsPerSecond.setText("1000");
			checkTimeStepsPerSecond();
		});
		sub.add(timeStepsPerSecondResetButton);

		checkTimeStepsPerSecond();

		lines.add(Box.createVerticalStrut(25));

		/* Zwischenüberschrift: "Informationen zum Modell" */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.Info")+"</b></html>"));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel(getInfo()));
	}

	/**
	 * Prüft die Einstellungen zum Zufallszahlengenerator
	 * (und aktiviert oder deaktiviert die Reset-Schaltfläche).
	 */
	private void checkRandomMode() {
		final int index=randomMode.getSelectedIndex();
		randomModeResetButton.setEnabled(index!=0);
		final boolean good=RandomGeneratorMode.getAllIsGoodForSimulation()[index];
		randomModeInfoButton.setVisible(!good);
	}

	/**
	 * Prüft den eingegebenen Startwert für den Zufallszahlengenerator.
	 * @return	Liefert <code>true</code>, wenn der eingegebene Startwert für den Zufallszahlengenerator gültig ist.
	 * @see #fixedSeed
	 * @see #checkData()
	 */
	private boolean checkFixedSeed() {
		return (NumberTools.getLong(fixedSeed,true)!=null);
	}

	/**
	 * Prüft die Eingaben im Anzahl an Zeitschritten pro Sekunde Eingabefeld
	 * @return	Liefert <code>true</code>, wenn der eingestellte Wert gültig ist
	 */
	private boolean checkTimeStepsPerSecond() {
		final Long L=NumberTools.getNotNegativeLong(timeStepsPerSecond,true);
		if (L==null) {
			timeStepsPerSecondResetButton.setEnabled(true);
			return false;
		}

		if (L<1000 || L>1_000_000_000) {
			timeStepsPerSecond.setBackground(Color.RED);
			timeStepsPerSecondResetButton.setEnabled(true);
			return false;
		}

		timeStepsPerSecondResetButton.setEnabled(L!=1000);
		return true;
	}

	@Override
	public boolean checkData() {
		final boolean seedOk=checkFixedSeed();
		if (!seedOk && useFixedSeed.isSelected()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.Error"),fixedSeed.getText()));
			return false;
		}

		if (!checkTimeStepsPerSecond()) {
			MsgBox.error(dialog,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.SimulationSystem.TimeStepsPerSecond.Error"),timeStepsPerSecond.getText()));
			return false;
		}

		return true;
	}

	@Override
	public void storeData() {
		model.randomMode=RandomGeneratorMode.fromIndex(randomMode.getSelectedIndex());

		model.useFixedSeed=useFixedSeed.isSelected();
		Long L=NumberTools.getLong(fixedSeed,true);
		if (L!=null) model.fixedSeed=L;

		L=NumberTools.getNotNegativeLong(timeStepsPerSecond,true);
		if (L!=null) model.timeStepsPerSecond=L.longValue();
	}
}
