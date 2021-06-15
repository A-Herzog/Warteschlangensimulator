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
package ui.dialogs;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import language.Language;
import mathtools.NumberTools;
import net.calc.ServerStatus;
import scripting.js.JSEngineNames;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialogseite "Leistung" im Programmeinstellungen-Dialog
 * @author Alexander Herzog
 * @see SetupData
 */
public class SetupDialogPagePerformance extends SetupDialogPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3076630659811610984L;

	/* Bereich: Allgemein */

	/** Hintergrundverarbeitung (Modelle prüfen oder auch simulieren) */
	private final JComboBox<String> backgroundProcessing;

	/* Bereich: Programmoberfläche */

	/** Animationen in der grafischen Oberfläche verwenden */
	private final JCheckBox useGUIAnimations;

	/* Lokale Simulation */

	/** Mehrkernunterstützung für Simulationen verwenden? */
	private final JCheckBox useMultiCoreSimulation;
	/** Mehrkernunterstützung für Animationen verwenden? */
	private final JCheckBox useMultiCoreAnimation;
	/** Hohe Priorität für die Simulationsthreads verwenden? */
	private final JCheckBox highPriority;
	/** NUMA-Unterstützung für die Simulation aktivieren? */
	private final JCheckBox useNUMAMode;

	/* Bereich: Scripting  */

	/** Bevorzugte Javascript-Engine */
	private final JComboBox<String> jsEngine;
	/** Simulation bei Skriptfehler abbrechen? */
	private final JCheckBox cancelSimulationOnScriptError;

	/* Bereich: Netzwerksimulation */

	/** Server-basierte Simulation durchführen? */
	private final JCheckBox serverUse;
	/** Server-Adresse bzw. -Name für Server-basierte Simulation */
	private final JTextField serverName;
	/** Server-Port für Server-basierte Simulation */
	private final SpinnerModel serverPort;
	/** Evtl. notwendiges Passwort für Server-basierte Simulation */
	private final JTextField serverKey;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPagePerformance() {
		JPanel line;
		JLabel label;
		JButton button;
		Object[] data;

		/*
		 * Bereich:
		 * Allgemein
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Simulation.General"));

		/* Hintergrundverarbeitung (Modelle prüfen oder auch simulieren) */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.BackgroundProcessing")+":"));
		line.add(backgroundProcessing=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.BackgroundProcessing.Off"),
				Language.tr("SettingsDialog.BackgroundProcessing.CheckOnly"),
				Language.tr("SettingsDialog.BackgroundProcessing.Simulate"),
				Language.tr("SettingsDialog.BackgroundProcessing.SimulateAlways")
		}));
		backgroundProcessing.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.SIMULATION_CHECK,
				Images.SIMULATION,
				Images.SIMULATION
		}));
		label.setLabelFor(backgroundProcessing);

		/*
		 * Bereich:
		 * Programmoberfläche
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Performance.GUI"));

		/* Animationen in der grafischen Oberfläche verwenden */
		addLine().add(useGUIAnimations=new JCheckBox(Language.tr("SettingsDialog.Tabs.Performance.GUIAnimations")));

		/*
		 * Bereich:
		 * Lokale Simulation
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Simulation.Local"));

		/* Mehrkernunterstützung für Simulationen verwenden? */
		addLine().add(useMultiCoreSimulation=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseMultiCore")));

		/* Mehrkernunterstützung für Animationen verwenden? */
		addLine().add(useMultiCoreAnimation=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseMultiCoreAnimation")));

		/* Hohe Priorität für die Simulationsthreads verwenden? */
		addLine().add(highPriority=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseHighPriority")));

		/* NUMA-Unterstützung für die Simulation aktivieren? */
		line=addLine();
		line.add(useNUMAMode=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA")));
		line.add(Box.createHorizontalStrut(5));
		final JToolBar bar=new JToolBar();
		bar.setFloatable(false);
		line.add(bar);
		bar.add(button=new JButton());
		button.setIcon(Images.HELP.getIcon());
		button.setToolTipText(Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA.InfoShort"));
		button.addActionListener(e->MsgBox.info(this,Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA.InfoShort"),Language.tr("SettingsDialog.Tabs.Simulation.UseNUMA.InfoLong")));

		/*
		 * Bereich:
		 * Scripting
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Performance.Scripting"));

		/* Bevorzugte Javascript-Engine */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.JSEngine")+":"));
		line.add(jsEngine=new JComboBox<>(new String[] {
				Language.tr("SettingsDialog.JSEngine.Automatic"),
				Language.tr("SettingsDialog.JSEngine.Nashorn"),
				Language.tr("SettingsDialog.JSEngine.Rhino"),
				Language.tr("SettingsDialog.JSEngine.GraalJS"),
		}));
		jsEngine.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_ENGINE_AUTOMATIC,
				Images.SETUP_ENGINE_NASHORN,
				Images.SETUP_ENGINE_RHINO,
				Images.SETUP_ENGINE_GRAAL
		}));
		label.setLabelFor(jsEngine);

		/* Simulation bei Skriptfehler abbrechen? */
		addLine().add(cancelSimulationOnScriptError=new JCheckBox(Language.tr("SettingsDialog.Tabs.Performance.CancelSimulationOnScriptError")));

		/*
		 * Bereich:
		 * Netzwerksimulation
		 */
		addHeading(Language.tr("SettingsDialog.Tabs.Simulation.Remote"));

		/* Server-basierte Simulation durchführen? */
		addLine().add(serverUse=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.Server.Use")));

		/* Server-Adresse bzw. -Name für Server-basierte Simulation */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.Simulation.Server.Name")+":","");
		add((JPanel)data[0]);
		serverName=(JTextField)data[1];
		serverName.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {serverUse.setSelected(true);}
		});

		/* Server-Port für Server-basierte Simulation */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.Tabs.Simulation.Server.Port")+":"));
		final JSpinner serverPortSpinner=new JSpinner(serverPort=new SpinnerNumberModel(1,1,65535,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(serverPortSpinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(6);
		serverPortSpinner.setEditor(editor);
		line.add(serverPortSpinner);
		label.setLabelFor(serverPortSpinner);

		/* Evtl. notwendiges Passwort für Server-basierte Simulation */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.Simulation.Server.Key")+":","");
		add((JPanel)data[0]);
		serverKey=(JTextField)data[1];
		serverKey.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyReleased(KeyEvent e) {serverUse.setSelected(true);}
			@Override public void keyPressed(KeyEvent e) {serverUse.setSelected(true);}
		});

		/* Schaltfläche: Verbindung prüfen */
		line=addLine();
		button=new JButton(Language.tr("SettingsDialog.Tabs.Simulation.Server.CheckConnection"));
		button.setIcon(Images.EXTRAS_SERVER.getIcon());
		button.addActionListener(e->{
			final String key=(serverKey.getText().trim().isEmpty())?null:serverKey.getText().trim();
			final int port=((Integer)serverPort.getValue()).intValue();
			new ServerStatus(serverName.getText(),port,true,key).showMessage(this);
		});
		line.add(button);

		/*
		mainarea.add(Box.createVerticalStrut(15));
		mainarea.add(p=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		p.add(new JLabel("<html><body><b>"+Language.tr("SettingsDialog.Tabs.Compiler")+"</b></body></html>"));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("SettingsDialog.Tabs.Compiler.JDKPath")+":","");
		mainarea.add(p2=(JPanel)data[0]);
		javaJDKpath=(JTextField)data[1];
		final JButton selectFolder=new JButton();
		p2.add(selectFolder,BorderLayout.EAST);
		selectFolder.setToolTipText(Language.tr("SettingsDialog.Tabs.Compiler.SelectFolder"));
		selectFolder.setIcon(Images.SELECT_FOLDER.getIcon());
		selectFolder.addActionListener(e->{
			final String folder=selectFolder(Language.tr("SettingsDialog.Tabs.Compiler.SelectFolder"),javaJDKpath.getText());
			if (folder!=null) javaJDKpath.setText(folder);
		});
		 */
	}

	@Override
	public String getTitle() {
		return Language.tr("SettingsDialog.Tabs.Performance");
	}

	@Override
	public Icon getIcon() {
		return Images.SETUP_PAGE_PERFORMANCE.getIcon();
	}

	@Override
	public void loadData() {
		switch (setup.backgroundSimulation) {
		case BACKGROUND_NOTHING: backgroundProcessing.setSelectedIndex(0); break;
		case BACKGROUND_CHECK_ONLY: backgroundProcessing.setSelectedIndex(1); break;
		case BACKGROUND_SIMULATION: backgroundProcessing.setSelectedIndex(2); break;
		case BACKGROUND_SIMULATION_ALWAYS: backgroundProcessing.setSelectedIndex(3); break;
		}

		useGUIAnimations.setSelected(setup.useAnimations);

		useMultiCoreSimulation.setSelected(setup.useMultiCoreSimulation);
		useMultiCoreAnimation.setSelected(setup.useMultiCoreAnimation);
		highPriority.setSelected(setup.highPriority);
		useNUMAMode.setSelected(setup.useNUMAMode);
		JSEngineNames engine=JSEngineNames.fromName(setup.jsEngine);
		cancelSimulationOnScriptError.setSelected(setup.cancelSimulationOnScriptError);
		if (engine==null) engine=JSEngineNames.DEFAULT;
		switch (engine) {
		case DEFAULT: jsEngine.setSelectedIndex(0); break;
		case NASHORN: jsEngine.setSelectedIndex(1); break;
		case RHINO: jsEngine.setSelectedIndex(2); break;
		case GRAALJS:
		case GRAALJSNative: jsEngine.setSelectedIndex(3); break;
		}

		serverUse.setSelected(setup.serverUse);
		if (setup.serverData!=null) {
			final String[] parts=setup.serverData.split(":");
			if (parts.length>=2 && parts.length<=3) {
				serverName.setText(parts[0]);
				final Long L=NumberTools.getPositiveLong(parts[1]);
				if (L!=null) serverPort.setValue(L.intValue());
				serverPort.addChangeListener(e->serverUse.setSelected(true));
				if (parts.length==3) serverKey.setText(parts[2]);
			}
		}

		/*
		javaJDKpath.setText(setup.javaJDKPath);
		 */
	}

	@Override
	public void storeData() {
		switch (backgroundProcessing.getSelectedIndex()) {
		case 0: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_NOTHING; break;
		case 1: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_CHECK_ONLY; break;
		case 2: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION; break;
		case 3: setup.backgroundSimulation=SetupData.BackgroundProcessingMode.BACKGROUND_SIMULATION_ALWAYS; break;
		}

		setup.useAnimations=useGUIAnimations.isSelected();

		setup.useMultiCoreSimulation=useMultiCoreSimulation.isSelected();
		setup.useMultiCoreAnimation=useMultiCoreAnimation.isSelected();
		setup.highPriority=highPriority.isSelected();
		setup.useNUMAMode=useNUMAMode.isSelected();
		switch (jsEngine.getSelectedIndex()) {
		case 0: setup.jsEngine=""; break;
		case 1: setup.jsEngine=JSEngineNames.NASHORN.name; break;
		case 2: setup.jsEngine=JSEngineNames.RHINO.name; break;
		case 3: setup.jsEngine=JSEngineNames.GRAALJSNative.name; break;
		}
		setup.cancelSimulationOnScriptError=cancelSimulationOnScriptError.isSelected();

		setup.serverUse=serverUse.isSelected();
		final StringBuilder sb=new StringBuilder();
		sb.append(serverName.getText().trim());
		sb.append(":");
		sb.append(serverPort.getValue());
		final String key=serverKey.getText().trim();
		if (!key.isEmpty()) {
			sb.append(":");
			sb.append(key);
		}
		setup.serverData=sb.toString();

		/*
		setup.javaJDKPath=javaJDKpath.getText();
		 */
	}

	@Override
	public void resetSettings() {
		backgroundProcessing.setSelectedIndex(2);
		useGUIAnimations.setSelected(true);
		useMultiCoreSimulation.setSelected(true);
		useMultiCoreAnimation.setSelected(true);
		highPriority.setSelected(false);
		useNUMAMode.setSelected(false);
		jsEngine.setSelectedIndex(0);
		cancelSimulationOnScriptError.setSelected(true);
		serverPort.setValue(8183);
		serverUse.setSelected(false);
	}
}
