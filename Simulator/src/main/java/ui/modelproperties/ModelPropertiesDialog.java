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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.tools.ThreadLocalRandomGenerator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionEval;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.JTableExt;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelLongRunStatisticsElement;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSchedules;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.ModelTransporters;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.elements.ModelElementText;
import ui.modeleditor.elements.VariablesTableModel;

/**
 * Dieser Dialog ermöglicht das Bearbeiten der zentralen Eigenschaften
 * eines Editor-Modells, die nicht direkt über die Zeichenfläche
 * konfiguriert werden können (wie Bedienergruppen, Einstellungen zur
 * Simulation als solches, Ausgabeanalyse usw.)
 * @author Alexander Herzog
 * @see EditModel
 */
public class ModelPropertiesDialog extends BaseDialog {
	private static final long serialVersionUID = 3736946126004680597L;

	/**
	 * Legt fegt, welche Dialogseite beim Aufruf des Dialogs initial angezeigt werden soll.
	 * @author Alexander Herzog
	 */
	public enum InitialPage {
		/** Dialogseite "Modellbeschreibung" */
		DESCRIPTION(()->Language.tr("Editor.Dialog.Tab.ModelDescription"),Images.MODELPROPERTIES_DESCRIPTION.getIcon()),

		/** Dialogseite "Simulation" */
		SIMULATION(()->Language.tr("Editor.Dialog.Tab.Simulation"),Images.MODELPROPERTIES_SIMULATION.getIcon()),

		/** Dialogseite "Kunden" */
		CLIENTS(()->Language.tr("Editor.Dialog.Tab.Clients"),Images.MODELPROPERTIES_CLIENTS.getIcon()),

		/** Dialogseite "Bediener" */
		OPERATORS(()->Language.tr("Editor.Dialog.Tab.Operators"),Images.MODELPROPERTIES_OPERATORS.getIcon()),

		/** Dialogseite "Transporter" */
		TRANSPORTERS(()->Language.tr("Editor.Dialog.Tab.Transporters"),Images.MODELPROPERTIES_TRANSPORTERS.getIcon()),

		/** Dialogseite "Zeitpläne" */
		SCHEDULES(()->Language.tr("Editor.Dialog.Tab.Schedule"),Images.MODELPROPERTIES_SCHEDULES.getIcon()),

		/** Dialogseite "Fertigungspläne" */
		SEQUENCES(()->Language.tr("Editor.Dialog.Tab.Sequences"),Images.MODELPROPERTIES_SEQUENCES.getIcon()),

		/** Dialogseite "Initiale Variablenwerte" */
		INITIAL_VALUES(()->Language.tr("Editor.Dialog.Tab.InitialVariableValues"),Images.MODELPROPERTIES_INITIAL_VALUES.getIcon()),

		/** Dialogseite "Laufzeitstatistik" */
		RUN_TIME_STATISTICS(()->Language.tr("Editor.Dialog.Tab.RunTimeStatistics"),Images.MODELPROPERTIES_RUNTIME_STATISTICS.getIcon()),

		/** Dialogseite "Ausgabeanalyse" */
		OUTPUT_ANALYSIS(()->Language.tr("Editor.Dialog.Tab.OutputAnalysis"),Images.MODELPROPERTIES_OUTPUT_ANALYSIS.getIcon()),

		/** Dialogseite "Ausgabeanalyse" */
		PATH_RECORDING(()->Language.tr("Editor.Dialog.Tab.PathRecording"),Images.MODELPROPERTIES_PATH_RECORDING.getIcon()),

		/** Dialogseite "Simulationssystem" */
		INFO(()->Language.tr("Editor.Dialog.Tab.SimulationSystem"),Images.MODELPROPERTIES_INFO.getIcon());

		private final Supplier<String> nameSupplier;
		private final Icon icon;

		/**
		 * Konstruktor der Enum-Klasse
		 * @param nameSupplier	Callback das den Namen der Seite in der passenden Sprache liefert
		 * @param icon	Zu verwendendes Icon
		 */
		InitialPage(final Supplier<String> nameSupplier, final Icon icon) {
			this.nameSupplier=nameSupplier;
			this.icon=icon;
		}

		/**
		 * Liefert den Namen der Dialogseite in der aktuellen Sprache.
		 * @return	Name der Dialogseite in der aktuellen Sprache
		 */
		public String getName() {
			return nameSupplier.get();
		}

		/**
		 * Liefert das Icon der Dialogseite.
		 * @return	Icon der Dialogseite
		 */
		public Icon getIcon() {
			return icon;
		}
	}

	/**
	 * Liefert nach dem Schließen des Dialogs eine Information darüber, welche Aktion als nächstes ausgeführt werden soll.
	 * @author Alexander Herzog
	 * @see ModelPropertiesDialog#getNextAction()
	 */
	public enum NextAction {
		/** Keine weitere geplante Aktion */
		NONE,
		/** Statistik-Batch-Größe bestimmen */
		FIND_BATCH_SIZE
	}

	private final EditModel model;
	private NextAction nextAction=NextAction.NONE;
	private final ModelSchedules localSchedules;
	private final ModelResources localResources;
	private final ModelTransporters localTransporters;
	private final AnimationImageSource imageSource;
	private final Runnable help;

	private JTextField name;
	private JTextField author;
	private JTextArea description;

	private JCheckBox terminationByClientClount;
	private JTextField clientCount;
	private JTextField warmUpTime;
	private JLabel warmUpTimeInfo;
	private JCheckBox terminationByCondition;
	private JTextField terminationCondition;
	private JCheckBox terminationByTime;
	private JTextField terminationTime;
	private JCheckBox useFixedSeed;
	private JTextField fixedSeed;
	private JTextField repeatCount;
	private JTextField distributionRecordHours;
	private JCheckBox stoppOnCalcError;
	private JCheckBox useTimedChecks;
	private JTextField editTimedChecks;
	private JCheckBox recordIncompleteClients;

	private DefaultListModel<JLabel> clientColorsListModel;

	private JList<JLabel> clientColorsList;

	private ResourceTableModel resourcesData;
	private JComboBox<String> secondaryResourcePriority;

	private TransporterTableModel transportersData;

	private SchedulesTableModel schedulesData;

	private SequencesEditPanel sequencesEdit;

	private VariablesTableModel variablesTableModel;

	private JTextField stepWideEdit;
	private JComboBox<String> stepWideCombo;
	private AdditionalStatisticsTableModel statisticsData;

	private JComboBox<String> correlationMode;
	private JTextField correlationRange;
	private JTextField batchMeansSize;
	private JCheckBox useFinishConfidence;
	private JTextField finishConfidenceHalfWidth;
	private JTextField finishConfidenceLevel;

	private JLabel correlationWarning;

	private JCheckBox pathRecordingStationTransitions;
	private JCheckBox pathRecordingClientPaths;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param initialPage	Beim Aufruf des Dialogs anzuzeigende Seite (darf <code>null</code> sein)
	 * @see ModelPropertiesDialog.InitialPage
	 */
	public ModelPropertiesDialog(final Component owner, final EditModel model, final boolean readOnly, final InitialPage initialPage) {
		super(owner,Language.tr("Editor.Dialog.Title"),readOnly);
		imageSource=new AnimationImageSource();
		this.model=model;
		localSchedules=model.schedules.clone();
		localResources=model.resources.clone();
		localTransporters=model.transporters.clone();

		help=()->Help.topicModal(ModelPropertiesDialog.this.owner,"EditorModelDialog");
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		final JTabbedPane tabs=new JTabbedPane();

		tabs.setTabPlacement(SwingConstants.LEFT);
		content.add(tabs,BorderLayout.CENTER);

		JPanel sub;

		tabs.addTab(InitialPage.DESCRIPTION.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelDescription);
		addGeneralTab(sub);

		tabs.addTab(InitialPage.SIMULATION.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelSimulation);
		addSimulationTab(sub);

		tabs.addTab(InitialPage.CLIENTS.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelClients);
		addClientsTab(sub);

		tabs.addTab(InitialPage.OPERATORS.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelOperators);
		addResourcesTab(sub);

		tabs.addTab(InitialPage.TRANSPORTERS.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelTransporters);
		addTransportersTab(sub);

		tabs.addTab(InitialPage.SCHEDULES.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelSchedule);
		addSchedulesTab(sub);

		tabs.addTab(InitialPage.SEQUENCES.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelSequences);
		addSequencesTab(sub);

		tabs.addTab(InitialPage.INITIAL_VALUES.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelInitialValues);
		addInitialVariableValuesTab(sub);

		tabs.addTab(InitialPage.RUN_TIME_STATISTICS.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelRunTimeStatistics);
		addRunTimeStatisticsTab(sub);

		tabs.addTab(InitialPage.OUTPUT_ANALYSIS.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelOutputAnalysis);
		addOutputAnalysisTab(sub);

		tabs.addTab(InitialPage.PATH_RECORDING.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelPathRecording);
		addPathRecordingTab(sub);

		tabs.addTab(InitialPage.INFO.getName(),sub=new JPanel(new BorderLayout()));
		sub=InfoPanel.addTopPanelAndGetNewContent(sub,InfoPanel.modelSimulationSystem);
		addStatusTab(sub);

		tabs.setIconAt(0,InitialPage.DESCRIPTION.getIcon());
		tabs.setIconAt(1,InitialPage.SIMULATION.getIcon());
		tabs.setIconAt(2,InitialPage.CLIENTS.getIcon());
		tabs.setIconAt(3, InitialPage.OPERATORS.getIcon());
		tabs.setIconAt(4,InitialPage.TRANSPORTERS.getIcon());
		tabs.setIconAt(5,InitialPage.SCHEDULES.getIcon());
		tabs.setIconAt(6,InitialPage.SEQUENCES.getIcon());
		tabs.setIconAt(7,InitialPage.INITIAL_VALUES.getIcon());
		tabs.setIconAt(8,InitialPage.RUN_TIME_STATISTICS.getIcon());
		tabs.setIconAt(9,InitialPage.OUTPUT_ANALYSIS.getIcon());
		tabs.setIconAt(10,InitialPage.PATH_RECORDING.getIcon());
		tabs.setIconAt(11,InitialPage.INFO.getIcon());

		/* Vergrößert die Tabs und setzt die Titel linksbündig */
		int maxWidth=0;
		for (int i=0;i<tabs.getTabCount();i++) {
			final JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT));
			final JLabel l=new JLabel(tabs.getTitleAt(i));
			l.setIcon(tabs.getIconAt(i));
			p.setOpaque(false);
			l.setOpaque(false);
			p.add(l);
			maxWidth=Math.max(maxWidth,p.getPreferredSize().width);
			tabs.setTabComponentAt(i,p);
		}
		for (int i=0;i<tabs.getTabCount();i++) {
			final JPanel p=(JPanel)tabs.getTabComponentAt(i);
			p.setPreferredSize(new Dimension(maxWidth,p.getPreferredSize().height));
		}

		setMinSizeRespectingScreensize(775,775);
		pack();
		if (getSize().width>900) setSize(900,getSize().height);
		setLocationRelativeTo(getOwner());
		setResizable(true);

		if (initialPage!=null) switch (initialPage) {
		case DESCRIPTION: tabs.setSelectedIndex(0); break;
		case SIMULATION: tabs.setSelectedIndex(1); break;
		case CLIENTS: tabs.setSelectedIndex(2); break;
		case OPERATORS: tabs.setSelectedIndex(3); break;
		case TRANSPORTERS: tabs.setSelectedIndex(4); break;
		case SCHEDULES: tabs.setSelectedIndex(5); break;
		case SEQUENCES: tabs.setSelectedIndex(6); break;
		case INITIAL_VALUES: tabs.setSelectedIndex(7); break;
		case RUN_TIME_STATISTICS: tabs.setSelectedIndex(8); break;
		case OUTPUT_ANALYSIS: tabs.setSelectedIndex(9); break;
		case PATH_RECORDING: tabs.setSelectedIndex(10); break;
		case INFO: tabs.setSelectedIndex(11); break;
		default: break;
		}
	}

	private JLabel addLabel(final JPanel parent, final String text, final String position) {
		JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (parent.getLayout() instanceof BorderLayout) {
			parent.add(panel,position);
		} else {
			parent.add(panel);
		}
		JLabel label=new JLabel(text);
		panel.add(label);
		return label;
	}

	private void addGeneralTab(final JPanel content) {
		JPanel sub;
		JPanel line;
		JLabel label;
		Object[] data;

		final JPanel top=new JPanel();
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));
		content.add(top,BorderLayout.NORTH);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.ModelDescription.NameOfTheModel")+":",model.name);
		top.add(sub=(JPanel)data[0]);
		name=(JTextField)data[1];
		name.setEditable(!readOnly);
		final JButton buttonAsNameToSurface;
		sub.add(buttonAsNameToSurface=new JButton(),BorderLayout.EAST);
		buttonAsNameToSurface.setIcon(Images.MODELPROPERTIES_DESCRIPTION_ADD_TO_SURFACE.getIcon());
		buttonAsNameToSurface.setToolTipText(Language.tr("Editor.Dialog.Tab.ModelDescription.NameOfTheModel.AddNameToModel"));
		buttonAsNameToSurface.setEnabled(!readOnly && !model.name.trim().isEmpty());
		buttonAsNameToSurface.addActionListener(e->addNameToModel(name.getText().trim()));
		name.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				buttonAsNameToSurface.setEnabled(!readOnly && !name.getText().trim().isEmpty());
			}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.ModelDescription.Author")+":",(model.author==null)?"":model.author);
		top.add((JPanel)data[0]);
		author=(JTextField)data[1];
		author.setEditable(!readOnly);
		if (!readOnly) {
			final JButton authorDefaultButton=new JButton();
			((JPanel)data[0]).add(authorDefaultButton,BorderLayout.EAST);
			authorDefaultButton.addActionListener(e->author.setText(EditModel.getDefaultAuthor()));
			authorDefaultButton.setIcon(Images.MODELPROPERTIES_DESCRIPTION_SET_AUTHOR.getIcon());
			authorDefaultButton.setToolTipText(Language.tr("Editor.Dialog.Tab.ModelDescription.Author.SetDefault"));
		}

		content.add(sub=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		label=addLabel(sub,Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription")+":",BorderLayout.NORTH);
		sub.add(new JScrollPane(description=new JTextArea(model.description)),BorderLayout.CENTER);
		description.setEditable(!readOnly);
		label.setLabelFor(description);
		ModelElementBaseDialog.addUndoFeature(description);

		content.add(sub=new JPanel(),BorderLayout.SOUTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JButton buttonAutoDescription;
		line.add(buttonAutoDescription=new JButton(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto")));
		buttonAutoDescription.setToolTipText(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.Hint"));
		final ModelDescriptionBuilderStyled builder=new ModelDescriptionBuilderStyled(model,null,true);
		builder.run();
		final String autoDescription=builder.getText();
		buttonAutoDescription.setIcon(Images.MODELPROPERTIES_DESCRIPTION_AUTO_CREATE.getIcon());
		buttonAutoDescription.setEnabled(!readOnly && !autoDescription.trim().isEmpty());
		buttonAutoDescription.addActionListener(e->{
			if (!description.getText().trim().isEmpty()) {
				if (!MsgBox.confirm(this,Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceTitle"),Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceInfo"),Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceYes"),Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceNo"))) return;
			}
			description.setText(autoDescription);
		});

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		if (model.surface.getElementCount()==1) {
			line.add(new JLabel(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Components.One")));
		} else {
			line.add(new JLabel(String.format(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Components.Number"),model.surface.getElementCount())));
		}
	}

	private void addSimulationTab(final JPanel content) {
		JPanel sub;
		Object[] data;

		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel lines;
		content.add(lines=new JPanel());

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		/* Anzahl an Kunden */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(terminationByClientClount=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.UseNumberOfArrivals")+"</b></html>"));
		terminationByClientClount.setEnabled(!readOnly);
		terminationByClientClount.setSelected(model.useClientCount);
		terminationByClientClount.addActionListener(e->testCorrelationWarning());

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.NumberOfArrivals")+":",""+model.clientCount,10);
		lines.add((JPanel)data[0]);
		clientCount=(JTextField)data[1];
		clientCount.setEditable(!readOnly);
		clientCount.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {terminationByClientClount.setSelected(true); NumberTools.getNotNegativeInteger(clientCount,true); testCorrelationWarning(); updateWarmUpTimeInfo();}
			@Override public void keyPressed(KeyEvent e) {terminationByClientClount.setSelected(true); NumberTools.getNotNegativeInteger(clientCount,true); testCorrelationWarning(); updateWarmUpTimeInfo();}
			@Override public void keyReleased(KeyEvent e) {terminationByClientClount.setSelected(true); NumberTools.getNotNegativeInteger(clientCount,true); testCorrelationWarning(); updateWarmUpTimeInfo();}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase")+":",NumberTools.formatPercent(model.warmUpTime,3),6);
		lines.add((JPanel)data[0]);
		warmUpTime=(JTextField)data[1];
		warmUpTime.setEditable(!readOnly);
		warmUpTime.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {NumberTools.getNotNegativeDouble(warmUpTime,true); updateWarmUpTimeInfo();}
			@Override public void keyPressed(KeyEvent e) {NumberTools.getNotNegativeDouble(warmUpTime,true); updateWarmUpTimeInfo();}
			@Override public void keyReleased(KeyEvent e) {NumberTools.getNotNegativeDouble(warmUpTime,true); updateWarmUpTimeInfo();}
		});
		((JPanel)data[0]).add(new JLabel("("+Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase.Info")+")"));
		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(warmUpTimeInfo=new JLabel());
		updateWarmUpTimeInfo();

		lines.add(Box.createVerticalStrut(25));

		/* Abbruchbedingung */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(terminationByCondition=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.UseCondition")+"</b></html>"));
		terminationByCondition.setEnabled(!readOnly);
		terminationByCondition.setSelected(model.useTerminationCondition);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.Condition")+":",model.terminationCondition);
		sub=(JPanel)data[0];
		lines.add(sub);
		terminationCondition=(JTextField)data[1];
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,terminationCondition,true,false,model,model.surface),BorderLayout.EAST);
		terminationCondition.setEditable(!readOnly);
		terminationCondition.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {terminationByCondition.setSelected(true); checkTerminationCondition();}
			@Override public void keyPressed(KeyEvent e) {terminationByCondition.setSelected(true); checkTerminationCondition();}
			@Override public void keyReleased(KeyEvent e) {terminationByCondition.setSelected(true); checkTerminationCondition();}
		});

		lines.add(Box.createVerticalStrut(25));

		/* Abbruchzeit */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(terminationByTime=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.UseTime")+"</b></html>"));
		terminationByTime.setEnabled(!readOnly);
		terminationByTime.setSelected(model.useFinishTime);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.Time")+":",TimeTools.formatLongTime(model.finishTime),15);
		sub=(JPanel)data[0];
		lines.add(sub);
		terminationTime=(JTextField)data[1];
		terminationTime.setEditable(!readOnly);
		terminationTime.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {terminationByTime.setSelected(true); checkTerminationTime();}
			@Override public void keyPressed(KeyEvent e) {terminationByTime.setSelected(true); checkTerminationTime();}
			@Override public void keyReleased(KeyEvent e) {terminationByTime.setSelected(true); checkTerminationTime();}
		});

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
		fixedSeed.setEditable(!readOnly);
		fixedSeed.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {useFixedSeed.setSelected(true); checkFixedSeed();}
			@Override public void keyPressed(KeyEvent e) {useFixedSeed.setSelected(true); checkFixedSeed();}
			@Override public void keyReleased(KeyEvent e) {useFixedSeed.setSelected(true); checkFixedSeed();}
		});
		if (!readOnly) {
			final JButton fixedSeedButton=new JButton(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.RandomButton"));
			fixedSeedButton.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.RandomButton.Hint"));
			fixedSeedButton.setIcon(Images.MODELPROPERTIES_SIMULATION_RANDOM_SEED.getIcon());
			sub.add(fixedSeedButton);
			fixedSeedButton.addActionListener(e->{
				fixedSeed.setText(""+new ThreadLocalRandomGenerator().nextLong());
				useFixedSeed.setSelected(true);
			});
		}

		lines.add(Box.createVerticalStrut(25));

		/* Weitere Simulationseinstellungen */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.Simulation.MoreSimulationSettings")+"</b></html>"));

		/* Wiederholungen */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.RepeatCount.Value")+":",""+model.repeatCount,5);
		sub=(JPanel)data[0];
		lines.add(sub);
		repeatCount=(JTextField)data[1];
		repeatCount.setEditable(!readOnly);
		repeatCount.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkRepeatCount();}
			@Override public void keyPressed(KeyEvent e) {checkRepeatCount();}
			@Override public void keyReleased(KeyEvent e) {checkRepeatCount();}
		});

		/* Anzahl Stunden in Verteilungen */

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Value")+":",""+model.distributionRecordHours,5);
		sub=(JPanel)data[0];
		lines.add(sub);
		distributionRecordHours=(JTextField)data[1];
		distributionRecordHours.setEditable(!readOnly);
		distributionRecordHours.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkDistributionRecordHours();}
			@Override public void keyPressed(KeyEvent e) {checkDistributionRecordHours();}
			@Override public void keyReleased(KeyEvent e) {checkDistributionRecordHours();}
		});
		sub.add(new JLabel(" ("+Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Info")+")"));

		/* Simulation abbrechen, wenn ein Rechenausdruck nicht ausgerechnet werden kann. */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		sub.add(stoppOnCalcError=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.StoppOnCalcError"),model.stoppOnCalcError));
		stoppOnCalcError.setEnabled(!readOnly);
		lines.add(sub);

		/* Zeitabhängige Bedingungsprüfungen */

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		lines.add(sub);
		sub.add(useTimedChecks=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks"),model.timedChecksDelta>0));
		useTimedChecks.setEnabled(!readOnly);
		useTimedChecks.addActionListener(e->checkTimedChecks());
		sub.add(editTimedChecks=new JTextField(NumberTools.formatNumber((model.timedChecksDelta>0)?(model.timedChecksDelta/1000.0):1.0),5));
		editTimedChecks.setEnabled(!readOnly);
		editTimedChecks.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {useTimedChecks.setSelected(true); checkTimedChecks();}
			@Override public void keyPressed(KeyEvent e) {useTimedChecks.setSelected(true); checkTimedChecks();}
			@Override public void keyReleased(KeyEvent e) {useTimedChecks.setSelected(true); checkTimedChecks();}
		});
		sub.add(new JLabel(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks.Seconds")));

		sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		lines.add(sub);
		sub.add(recordIncompleteClients=new JCheckBox(Language.tr("Editor.Dialog.Tab.Simulation.RecordIncompleteClients"),model.recordIncompleteClients));
		recordIncompleteClients.setToolTipText(Language.tr("Editor.Dialog.Tab.Simulation.RecordIncompleteClients.Hint"));
		recordIncompleteClients.setEnabled(!readOnly);
	}

	private void addClientsTab(final JPanel content) {
		clientColorsList=new JList<>(clientColorsListModel=new DefaultListModel<>());
		clientColorsList.setCellRenderer(new ElementListCellRenderer());
		content.add(new JScrollPane(clientColorsList));

		clientColorsList.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {editSelectedClientColor(); e.consume(); return;}}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});
		clientColorsList.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {}
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) {editSelectedClientColor(); e.consume(); return;}}
		});

		updateClientDataList();
	}

	private void editSelectedClientColor() {
		if (readOnly) return;

		final int selected=clientColorsList.getSelectedIndex();
		if (selected<0) return;
		final String clientType=model.surface.getClientTypes().get(selected);

		if (editClientData(this,help,model,clientType,false)) updateClientDataList();
	}

	private void updateWarmUpTimeInfo() {
		final Integer I=NumberTools.getNotNegativeInteger(clientCount,true);
		final Double D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (I==null || I==0 || D==null) {
			warmUpTimeInfo.setVisible(false);
		} else {
			final int additionalClients=(int)Math.round(I.intValue()*D.doubleValue());
			warmUpTimeInfo.setText("<html><body>"+String.format(Language.tr("Editor.Dialog.Tab.Simulation.WarmUpPhase.Info2"),NumberTools.formatLong(additionalClients)));
			warmUpTimeInfo.setVisible(true);
		}
	}

	/**
	 * Ruft den Dialog zum Bearbeiten der spezifischen Einstellungen für einen Kundentyp auf
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Runnable
	 * @param model	Modell, welches weitere Icondaten vorhält, aus dem die Kundendaten ausgelesen werden und in das die Kundendaten evtl. auch wieder zurückgeschrieben werden
	 * @param clientType	Name des zu bearbeitenden Kundentyps
	 * @param readOnly	Nur-Lese-Status für Dialog
	 * @return	Gibt <code>true</code> zurück, wenn der Dialog per Ok geschlossen wurde (und die Daten im Modell aktualisiert wurden)
	 */
	public static boolean editClientData(final Component owner, final Runnable help, final EditModel model, final String clientType, final boolean readOnly) {
		final Color color=model.clientData.getColor(clientType);
		String icon=model.clientData.getIcon(clientType);
		if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
		final double[] costs=model.clientData.getCosts(clientType);

		final ClientDataDialog dialog=new ClientDataDialog(owner,help,color,icon,costs,model.animationImages,readOnly);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return false;

		if (dialog.getUserColor()==null) model.clientData.delete(clientType); else model.clientData.setColor(clientType,dialog.getUserColor());
		model.clientData.setIcon(clientType,dialog.getIcon());
		if (dialog.getCosts()==null) model.clientData.delete(clientType); else model.clientData.setCosts(clientType,dialog.getCosts());

		return true;
	}

	private void updateClientDataList() {
		final int selected=clientColorsList.getSelectedIndex();
		clientColorsListModel.clear();
		for (String clientType: model.surface.getClientTypes()) clientColorsListModel.addElement(getLabel(clientType));
		clientColorsList.setSelectedIndex(selected);
	}

	private void drawColorToImage(final Graphics g, final Color color, final int x, final int y) {
		if (color==null) {
			g.setColor(Color.BLACK);
			g.drawLine(x+0,y+0,x+31,y+31);
			g.drawLine(x+31,y+0,x+0,y+31);
		} else {
			g.setColor(color);
			g.fillRect(x+0,y+0,31,31);
		}
		g.setColor(Color.BLACK);
		g.drawRect(x+0,y+0,31,31);
	}

	private JLabel getLabel(final String clientType) {
		final Color color=model.clientData.getColor(clientType);
		String iconName=model.clientData.getIcon(clientType);
		if (iconName==null || iconName.trim().isEmpty()) iconName=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
		final double[] costs=model.clientData.getCosts(clientType);

		/* Text aufbauen */
		final StringBuilder sb=new StringBuilder();
		sb.append("<b><span style=\"font-size: larger;\">"+clientType+"</span></b><br>");
		if (color==null) sb.append(Language.tr("Editor.Dialog.Tab.Clients.Color.Automatic")); else sb.append(Language.tr("Editor.Dialog.Tab.Clients.Color.UserDefined"));

		if (costs[0]!=0 || costs[1]!=0 || costs[2]!=0) {
			sb.append("<br>");
			sb.append(String.format(Language.tr("Editor.Dialog.Tab.Clients.Costs"),NumberTools.formatNumber(costs[0]),NumberTools.formatNumber(costs[1]),NumberTools.formatNumber(costs[2])));
		}

		/* Bild aufbauen */
		final BufferedImage image=new BufferedImage(64,32,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();

		BufferedImage symbol=imageSource.get(iconName,model.animationImages,32);
		g.drawImage(symbol,0,0,null);
		drawColorToImage(g,color,32,0);
		final Icon icon=new ImageIcon(image);

		/* Label erstellen */
		final JLabel label=new JLabel("<html><body>"+sb.toString()+"</body></html>");
		if (icon!=null) label.setIcon(icon);
		return label;
	}

	private class ElementListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 6913560392242517601L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((ElementListCellRenderer)renderer).setText(((JLabel)value).getText());
				((ElementListCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			return renderer;
		}
	}

	private void addResourcesTab(final JPanel content) {
		final JTableExt table=new JTableExt();
		resourcesData=new ResourceTableModel(model,model.surface,localResources,localSchedules,table,readOnly,help);
		table.setModel(resourcesData);
		table.getColumnModel().getColumn(1).setMaxWidth(175);
		table.getColumnModel().getColumn(1).setMinWidth(175);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		content.add(new JScrollPane(table),BorderLayout.CENTER);

		final JPanel setupArea=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(Language.tr("Resources.SecondaryPriority")+": ");
		setupArea.add(label);
		setupArea.add(secondaryResourcePriority=new JComboBox<>(new String[]{
				Language.tr("Resources.SecondaryPriority.Random"),
				Language.tr("Resources.SecondaryPriority.ClientPriority")
		}));
		secondaryResourcePriority.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELPROPERTIES_PRIORITIES_RANDOM,
				Images.MODELPROPERTIES_PRIORITIES_CLIENT
		}));

		label.setLabelFor(secondaryResourcePriority);
		secondaryResourcePriority.setEnabled(!readOnly);
		switch (localResources.secondaryResourcePriority) {
		case RANDOM:
			secondaryResourcePriority.setSelectedIndex(0);
			break;
		case CLIENT_PRIORITY:
			secondaryResourcePriority.setSelectedIndex(1);
			break;
		}
		content.add(setupArea,BorderLayout.SOUTH);
	}

	private void addTransportersTab(final JPanel content) {
		final JTableExt table=new JTableExt();
		transportersData=new TransporterTableModel(model,model.surface,localTransporters,table,readOnly,help);
		table.setModel(transportersData);
		table.getColumnModel().getColumn(1).setMaxWidth(75);
		table.getColumnModel().getColumn(1).setMinWidth(75);
		table.getColumnModel().getColumn(2).setMaxWidth(75);
		table.getColumnModel().getColumn(2).setMinWidth(75);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		table.setIsPanelCellTable(2);
		content.add(new JScrollPane(table));
	}

	private void addSchedulesTab(final JPanel content) {
		final JTableExt table=new JTableExt();
		schedulesData=new SchedulesTableModel(model.surface,localResources,localSchedules,table,readOnly,help);
		schedulesData.addUpdateResourcesListener(()->resourcesData.updateTable());
		table.setModel(schedulesData);
		table.getColumnModel().getColumn(0).setMaxWidth(225);
		table.getColumnModel().getColumn(0).setMinWidth(225);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		content.add(new JScrollPane(table));
	}

	private void addInitialVariableValuesTab(final JPanel content) {
		final Object[] data=VariablesTableModel.buildTable(model,readOnly,help);
		content.add((JScrollPane)data[0],BorderLayout.CENTER);
		variablesTableModel=(VariablesTableModel)data[1];
	}

	private void addRunTimeStatisticsTab(final JPanel content) {
		content.setLayout(new BorderLayout());

		/* Schrittweite */

		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT)); content.add(line,BorderLayout.NORTH);
		JLabel label=new JLabel(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.StepWide")+":");
		line.add(label);
		line.add(stepWideEdit=new JTextField(10));
		stepWideEdit.setEditable(!readOnly);
		stepWideEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {NumberTools.getPositiveDouble(stepWideEdit,true);}
			@Override public void keyPressed(KeyEvent e) {NumberTools.getPositiveDouble(stepWideEdit,true);}
			@Override public void keyReleased(KeyEvent e) {NumberTools.getPositiveDouble(stepWideEdit,true);}
		});
		line.add(stepWideCombo=new JComboBox<>(new String[] {
				Language.tr("Statistics.Seconds"),
				Language.tr("Statistics.Minutes"),
				Language.tr("Statistics.Hours"),
				Language.tr("Statistics.Days")
		}));
		stepWideCombo.setEnabled(!readOnly);

		double d=model.longRunStatistics.getStepWideSec();
		if (d<5*60) {
			/* Sekunden */
			stepWideEdit.setText(NumberTools.formatNumber(d));
			stepWideCombo.setSelectedIndex(0);
		} else {
			d=d/60;
			if (d<5*60) {
				/* Minuten */
				stepWideEdit.setText(NumberTools.formatNumber(d));
				stepWideCombo.setSelectedIndex(1);
			} else {
				d=d/60;
				if (d<5*24) {
					/* Stunden */
					stepWideEdit.setText(NumberTools.formatNumber(d));
					stepWideCombo.setSelectedIndex(2);
				} else {
					d=d/24;
					/* Tage */
					stepWideEdit.setText(NumberTools.formatNumber(d));
					stepWideCombo.setSelectedIndex(3);
				}
			}
		}

		/* Liste mit Ausdrücken */

		final JTableExt table=new JTableExt();
		statisticsData=new AdditionalStatisticsTableModel(model,table,readOnly,help);
		table.setModel(statisticsData);
		table.getColumnModel().getColumn(1).setMaxWidth(225);
		table.getColumnModel().getColumn(1).setMinWidth(225);
		table.setIsPanelCellTable(0);
		table.setIsPanelCellTable(1);
		content.add(new JScrollPane(table),BorderLayout.CENTER);
	}

	private void addOutputAnalysisTab(final JPanel content) {
		JPanel sub;
		Object[] data;
		JButton button;

		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel lines;
		content.add(lines=new JPanel());
		content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		/* Erfassung der Autokorrelation der Wartezeiten */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation")+"</b></html>"));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(correlationMode=new JComboBox<>(new String[]{
				Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Off"),
				Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Fast"),
				Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Full")
		}));
		correlationMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.GENERAL_OFF,
				Images.MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FAST,
				Images.MODELPROPERTIES_OUTPUT_ANALYSIS_AUTOCORRELATION_FULL
		}));
		correlationMode.setEnabled(!readOnly);
		switch (model.correlationMode) {
		case CORRELATION_MODE_OFF: correlationMode.setSelectedIndex(0); break;
		case CORRELATION_MODE_FAST: correlationMode.setSelectedIndex(1); break;
		case CORRELATION_MODE_FULL: correlationMode.setSelectedIndex(2); break;
		}
		int range=model.correlationRange;
		if (range<=0) range=1000;
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Range")+":",""+range,10);
		lines.add((JPanel)data[0]);
		correlationRange=(JTextField)data[1];
		correlationRange.setEditable(!readOnly);
		correlationRange.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {
				NumberTools.getPositiveLong(correlationRange,true);
				if (correlationMode.getSelectedIndex()==0) correlationMode.setSelectedIndex(1);
				testCorrelationWarning();
			}
			@Override public void keyPressed(KeyEvent e) {
				NumberTools.getPositiveLong(correlationRange,true);
				if (correlationMode.getSelectedIndex()==0) correlationMode.setSelectedIndex(1);
				testCorrelationWarning();
			}
			@Override public void keyReleased(KeyEvent e) {
				NumberTools.getPositiveLong(correlationRange,true);
				if (correlationMode.getSelectedIndex()==0) correlationMode.setSelectedIndex(1);
				testCorrelationWarning();
			}
		});

		lines.add(Box.createVerticalStrut(25));

		/* Batch-Means */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><b>"+Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans")+"</b></html>"));

		int size=model.batchMeansSize;
		if (size<=0) size=1;
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size")+":",""+size,10);
		lines.add(sub=(JPanel)data[0]);
		batchMeansSize=(JTextField)data[1];
		batchMeansSize.setEditable(!readOnly);
		batchMeansSize.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {NumberTools.getPositiveLong(batchMeansSize,true);}
			@Override public void keyPressed(KeyEvent e) {NumberTools.getPositiveLong(batchMeansSize,true);}
			@Override public void keyReleased(KeyEvent e) {NumberTools.getPositiveLong(batchMeansSize,true);}
		});
		sub.add(button=new JButton(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size.Auto"),Images.MSGBOX_OK.getIcon()));
		button.setToolTipText(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size.Auto.Hint"));
		button.setEnabled(!readOnly);
		button.addActionListener(e->{
			if (!checkData()) return;
			nextAction=NextAction.FIND_BATCH_SIZE;
			close(CLOSED_BY_OK);
		});

		lines.add(Box.createVerticalStrut(25));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(useFinishConfidence=new JCheckBox("<html><b>"+Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence")+"</b></html>",model.useFinishConfidence));
		useFinishConfidence.setEnabled(!readOnly);

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("("+Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.Hint")+")"));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.HalfWidth")+":",NumberTools.formatNumberMax(model.finishConfidenceHalfWidth),10);
		lines.add((JPanel)data[0]);
		finishConfidenceHalfWidth=(JTextField)data[1];
		finishConfidenceHalfWidth.setEditable(!readOnly);
		finishConfidenceHalfWidth.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {useFinishConfidence.setEnabled(true); NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);}
			@Override public void keyPressed(KeyEvent e) {useFinishConfidence.setEnabled(true); NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);}
			@Override public void keyReleased(KeyEvent e) {useFinishConfidence.setEnabled(true); NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);}
		});

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.Level")+":",NumberTools.formatPercent(model.finishConfidenceLevel),10);
		lines.add((JPanel)data[0]);
		finishConfidenceLevel=(JTextField)data[1];
		finishConfidenceLevel.setEditable(!readOnly);
		finishConfidenceLevel.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {useFinishConfidence.setEnabled(true); NumberTools.getPositiveDouble(finishConfidenceLevel,true);}
			@Override public void keyPressed(KeyEvent e) {useFinishConfidence.setEnabled(true); NumberTools.getPositiveDouble(finishConfidenceLevel,true);}
			@Override public void keyReleased(KeyEvent e) {useFinishConfidence.setEnabled(true); NumberTools.getPositiveDouble(finishConfidenceLevel,true);}
		});

		lines.add(Box.createVerticalStrut(25));

		/* Warnung bei zu langer Autokorrelationsaufzeichnung */

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(correlationWarning=new JLabel());
		correlationWarning.setIcon(Images.GENERAL_WARNING.getIcon());
		testCorrelationWarning();
	}

	private void testCorrelationWarning() {
		if (correlationMode.getSelectedIndex()==0) {correlationWarning.setVisible(false); return;}

		final Long L=NumberTools.getPositiveLong(correlationRange,true);
		if (L==null) {correlationWarning.setVisible(false); return;}
		final int correlationRange=L.intValue();

		if (!terminationByClientClount.isSelected()) {correlationWarning.setVisible(false); return;}

		final Integer I=NumberTools.getNotNegativeInteger(clientCount,true);
		if (I==null || I==0) {correlationWarning.setVisible(false); return;}
		final int clientCount=I.intValue();

		final SetupData setup=SetupData.getSetup();
		int threadCount=1;
		if (setup.useMultiCoreSimulation) threadCount=Math.min(setup.useMultiCoreSimulationMaxCount,Runtime.getRuntime().availableProcessors());

		final int clientsPerThread=clientCount/threadCount;
		if (clientsPerThread/2<correlationRange) {
			if (clientsPerThread>correlationRange) {
				correlationWarning.setText("<html><b>"+String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Warning.Near"),NumberTools.formatLong(correlationRange),NumberTools.formatLong(clientsPerThread))+"</b></html>");
			} else {
				correlationWarning.setText("<html><b>"+String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Warning.Over"),NumberTools.formatLong(correlationRange),NumberTools.formatLong(clientsPerThread))+"</b></html>");
			}
			correlationWarning.setVisible(true);
		} else {
			correlationWarning.setVisible(false);
		}
	}

	private void addPathRecordingTab(final JPanel content) {
		JPanel sub;

		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel lines;
		content.add(lines=new JPanel());
		content.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(pathRecordingStationTransitions=new JCheckBox(Language.tr("Editor.Dialog.Tab.PathRecording.StationTransitions"),model.recordStationTransitions));
		pathRecordingStationTransitions.setEnabled(!readOnly);

		lines.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(pathRecordingClientPaths=new JCheckBox(Language.tr("Editor.Dialog.Tab.PathRecording.ClientPaths"),model.recordClientPaths));
		pathRecordingClientPaths.setEnabled(!readOnly);
	}

	private void addSequencesTab(final JPanel content) {
		content.add(sequencesEdit=new SequencesEditPanel(model.sequences,model.surface,readOnly,help,model),BorderLayout.CENTER);
	}

	private void addStatusTab(final JPanel content) {
		final String error=StartAnySimulator.testModel(model);
		final StringBuilder sb=new StringBuilder();
		sb.append("<html><body style=\"margin: 10px;\">");
		if (error!=null) {
			sb.append("<p style=\"margin-bottom: 10px\">"+Language.tr("Editor.Dialog.Tab.SimulationSystem.Error")+"</p>");
			sb.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.ErrorInfo")+":<br><b>"+error+"</b></p>");
		} else {
			sb.append("<p style=\"margin-bottom: 10px\">"+Language.tr("Editor.Dialog.Tab.SimulationSystem.Ok")+"</p>");

			java.util.List<String> infoSingleCore=model.getSingleCoreReason();
			if (infoSingleCore==null || infoSingleCore.size()==0) {
				sb.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.MultiCoreOk")+"</p>");
			} else {
				sb.append("<p>"+Language.tr("Editor.Dialog.Tab.SimulationSystem.SingleCoreOnly")+":</p>");
				sb.append("<ul>");
				for (String line: infoSingleCore) sb.append("<li>"+line+"</li>");
				sb.append("</ul>");
			}
		}

		if (model.repeatCount>1) {
			final String infoNoRepeat=model.getNoRepeatReason();
			if (infoNoRepeat==null) {
				sb.append("<p>"+String.format(Language.tr("Editor.Dialog.Tab.SimulationSystem.RepeatOk"),model.repeatCount)+"</p>");
			} else {
				sb.append("<p>"+String.format(Language.tr("Editor.Dialog.Tab.SimulationSystem.RepeatNotOk"),model.repeatCount)+"<br>"+infoNoRepeat+"</p>");
			}
		}

		sb.append("</body></html>");

		content.setLayout(new BorderLayout(10,10));
		content.add(new JLabel(sb.toString()),BorderLayout.NORTH);
	}

	private int checkTerminationCondition() {
		if (terminationCondition.getText().trim().isEmpty()) {
			terminationCondition.setBackground(SystemColor.text);
			return -1;
		}

		final int error=ExpressionEval.check(terminationCondition.getText(),model.surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
		if (error>=0) terminationCondition.setBackground(Color.red); else terminationCondition.setBackground(SystemColor.text);
		return error;
	}

	private boolean checkTerminationTime() {
		return (TimeTools.getTime(terminationTime,true)!=null);
	}

	private boolean checkFixedSeed() {
		return (NumberTools.getLong(fixedSeed,true)!=null);
	}

	private boolean checkRepeatCount() {
		return (NumberTools.getPositiveLong(repeatCount,true)!=null);
	}

	private boolean checkDistributionRecordHours() {
		return (NumberTools.getNotNegativeLong(distributionRecordHours,true)!=null);
	}

	private boolean checkTimedChecks() {
		if (!useTimedChecks.isSelected()) {
			editTimedChecks.setBackground(SystemColor.text);
			return true;
		}
		return (NumberTools.getPositiveDouble(editTimedChecks,true)!=null);
	}

	private ModelElementText nameElement=null;

	private void addNameToModel(final String name) {
		int minY=Integer.MAX_VALUE;

		if (nameElement==null) {
			for (ModelElement element: model.surface.getElements()) {
				/* Element, das am weitesten oben ist, finden */
				if (element instanceof ModelElementPosition) {
					final int y=((ModelElementPosition)element).getPosition(true).y;
					if (y<minY) minY=y;
				}

				/* Text-Element ganz weit oben finden */
				if (element instanceof ModelElementText) {
					final ModelElementText text=(ModelElementText)element;
					if (nameElement==null) {nameElement=text; continue;}
					final Point p1=nameElement.getPosition(true);
					final Point p2=text.getPosition(true);
					if (p2.x*p2.x+p2.y*p2.y<p1.x*p1.x+p1.y*p1.y) nameElement=text;
				}
			}
			if (nameElement!=null && nameElement.getPosition(true).y>minY) nameElement=null;
		}

		/* Name einfügen */
		if (nameElement!=null) {
			/* Text-Element gefunden? -> Direkt ändern */
			nameElement.setText(name);
		} else {
			/* Alle bisherigen Elemente nach unten verschieben */
			final int delta=100-minY;
			if (delta>0) for (ModelElement element: model.surface.getElements()) if (element instanceof ModelElementPosition) {
				final ModelElementPosition posElement=(ModelElementPosition)element;
				final Point p=posElement.getPosition(false);
				p.y+=delta;
				posElement.setPosition(p);
			}

			/* Neues Element hinzufügen */
			model.surface.add(nameElement=new ModelElementText(model,model.surface));
			nameElement.setPosition(new Point(50,50));
			nameElement.setText(name);
			nameElement.setTextSize(18);
			nameElement.setTextBold(true);
		}
	}

	@Override
	protected boolean checkData() {

		/* Simulation */

		if (!terminationByClientClount.isSelected() && !terminationByCondition.isSelected() && !terminationByTime.isSelected()) {
			MsgBox.error(this,Language.tr("Editor.Dialog.Tab.Simulation.Criteria.Title"),Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorAtLeastOne"));
			return false;
		}

		Integer I=NumberTools.getNotNegativeInteger(clientCount,true);
		if (I==null) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorClients"),clientCount.getText()));
			return false;
		}
		Double D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (D==null) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorWarmUp"),warmUpTime.getText()));
			return false;
		}

		final int error=checkTerminationCondition();
		if (error>=0 && terminationByCondition.isSelected()) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorCondition"),terminationCondition.getText(),error+1));
			return false;
		}

		final boolean timeOk=checkTerminationTime();
		if (!timeOk && terminationByTime.isSelected()) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.Criteria.ErrorTime"),terminationTime.getText()));
			return false;
		}

		final boolean seedOk=checkFixedSeed();
		if (!seedOk && useFixedSeed.isSelected()) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.FixedSeed.Error"),fixedSeed.getText()));
			return false;
		}

		if (!checkRepeatCount()) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.RepeatCount.Error"),repeatCount.getText()));
			return false;
		}

		if (!checkDistributionRecordHours()) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.DistributionRecordHours.Error"),distributionRecordHours.getText()));
			return false;
		}

		if (!checkTimedChecks()) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.Simulation.TimedChecks.Error"),editTimedChecks.getText()));
			return false;
		}

		/* Laufzeitstatistik */

		D=NumberTools.getPositiveDouble(stepWideEdit,true);
		if (D==null) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.RunTimeStatistics.StepWide.Error"),stepWideEdit.getText()));
			return false;
		}

		/* Ausgabeanalyse */

		if (correlationMode.getSelectedIndex()>0) {
			Long L=NumberTools.getPositiveLong(correlationRange,true);
			if (L==null) {
				MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.RecordAutocorrelation.Range.Error"),correlationRange.getText()));
				return false;
			}
		}
		Long L=NumberTools.getPositiveLong(batchMeansSize,true);
		if (L==null) {
			MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.BatchMeans.Size.Error"),batchMeansSize.getText()));
			return false;
		}

		if (useFinishConfidence.isSelected()) {
			D=NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);
			if (D==null) {
				MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.HalfWidth.Error"),finishConfidenceHalfWidth.getText()));
				return false;
			}
			D=NumberTools.getProbability(finishConfidenceLevel,true);
			if (D==null) {
				MsgBox.error(this,Language.tr("Dialog.Title.Error"),String.format(Language.tr("Editor.Dialog.Tab.OutputAnalysis.FinishConfidence.Level.Error"),finishConfidenceLevel.getText()));
				return false;
			}
		}

		return true;
	}

	@Override
	protected void storeData() {

		/* Modellbeschreibung */

		model.name=name.getText();
		model.author=author.getText();
		model.description=description.getText();

		/* Simulation */

		model.useClientCount=terminationByClientClount.isSelected();
		Long L=NumberTools.getNotNegativeLong(clientCount,true);
		if (L!=null) model.clientCount=L;
		Double D=NumberTools.getNotNegativeDouble(warmUpTime,true);
		if (D!=null) model.warmUpTime=D;
		model.useTerminationCondition=terminationByCondition.isSelected();
		model.terminationCondition=terminationCondition.getText();
		model.useFinishTime=terminationByTime.isSelected();
		Integer I=TimeTools.getTime(terminationTime,true);
		if (I==null) model.finishTime=10*86400; else model.finishTime=I;
		model.useFixedSeed=useFixedSeed.isSelected();
		L=NumberTools.getLong(fixedSeed,true);
		if (L!=null) model.fixedSeed=L;
		L=NumberTools.getPositiveLong(repeatCount,true);
		if (L!=null) model.repeatCount=(int)L.longValue();
		L=NumberTools.getNotNegativeLong(distributionRecordHours,true);
		if (L!=null) model.distributionRecordHours=(int)L.longValue();
		model.stoppOnCalcError=stoppOnCalcError.isSelected();
		if (useTimedChecks.isSelected()) {
			D=NumberTools.getPositiveDouble(editTimedChecks,true);
			if (D!=null) model.timedChecksDelta=(int)Math.round(D.doubleValue()*1000);
		} else {
			model.timedChecksDelta=-1;
		}
		model.recordIncompleteClients=recordIncompleteClients.isSelected();

		/* Kunden */

		/* Kundendaten werden direkt im <code>model</code>-Element bearbeitet, da dieses eine Kopie ist und am Ende ggf. per <code>getModel</code> in den Editor geladen wird.

		/* Bediener */

		switch (secondaryResourcePriority.getSelectedIndex()) {
		case 0:
			resourcesData.getResources().secondaryResourcePriority=ModelResources.SecondaryResourcePriority.RANDOM;
			break;
		case 1:
			resourcesData.getResources().secondaryResourcePriority=ModelResources.SecondaryResourcePriority.CLIENT_PRIORITY;
			break;
		}
		model.resources.setDataFrom(resourcesData.getResources());

		/* Transporter */

		model.transporters.setDataFrom(transportersData.getTransporters());

		/* Zeitpläne */

		model.schedules.setDataFrom(schedulesData.getSchedules());

		/* Fertigungspläne */

		sequencesEdit.storeData();

		/* Initiale Variablen */

		variablesTableModel.storeData();

		/* Laufzeitstatistik */

		D=NumberTools.getPositiveDouble(stepWideEdit,true);
		if (D!=null) {
			double step=D;
			switch (stepWideCombo.getSelectedIndex()) {
			case 0: /* sind schon Sekunden */ break;
			case 1: step=step*60; break;
			case 2: step=step*60*60; break;
			case 3: step=step*60*60*24; break;
			}
			long l=Math.round(step);
			if (l<1) l=1;
			model.longRunStatistics.setStepWideSec(l);
		}

		model.longRunStatistics.getData().clear();
		for (ModelLongRunStatisticsElement element: statisticsData.getData()) model.longRunStatistics.getData().add(element.clone());

		/* Ausgabeanalyse */

		switch (correlationMode.getSelectedIndex()) {
		case 0: model.correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_OFF; break;
		case 1: model.correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_FAST; break;
		case 2: model.correlationMode=Statistics.CorrelationMode.CORRELATION_MODE_FULL; break;
		}
		if (model.correlationMode==Statistics.CorrelationMode.CORRELATION_MODE_OFF) {
			model.correlationRange=-1;
		} else {
			L=NumberTools.getPositiveLong(correlationRange,true);
			if (L!=null) model.correlationRange=L.intValue();
		}
		model.batchMeansSize=NumberTools.getPositiveLong(batchMeansSize,true).intValue();

		model.useFinishConfidence=useFinishConfidence.isSelected();
		final Double halfWidth=NumberTools.getPositiveDouble(finishConfidenceHalfWidth,true);
		final Double level=NumberTools.getProbability(finishConfidenceLevel,true);
		if (halfWidth!=null) model.finishConfidenceHalfWidth=halfWidth;
		if (level!=null) model.finishConfidenceLevel=level;

		/* Pfadaufzeichnung */

		model.recordStationTransitions=pathRecordingStationTransitions.isSelected();
		model.recordClientPaths=pathRecordingClientPaths.isSelected();
	}

	/**
	 * Liefert nach dem Schließen des Dialogs das bearbeitete Modell.
	 * @return	Bearbeitetes Modell
	 */
	public EditModel getModel() {
		return model;
	}

	/**
	 * Liefert nach dem Schließen des Dialogs eine Information darüber, welche Aktion als nächstes ausgeführt werden soll.
	 * @return	Nächste auszuführende Aktion
	 * @see ModelPropertiesDialog.NextAction
	 */
	public NextAction getNextAction() {
		return nextAction;
	}
}