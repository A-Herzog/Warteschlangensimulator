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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSurface;
import ui.tools.FlatLaFHelper;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementProcess}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementProcess
 */
public class ModelElementProcessDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -86922871601132368L;

	/** Liste aller globalen Variablen in dem Modell (inkl. der kundenspezifischen Pseudovariablen) */
	private String[] variables;
	/** Liste aller globalen Variablen in dem Modell (ohne die kundenspezifischen Pseudovariablen) */
	private String[] variablesNoClient;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private ExpressionCalcModelUserFunctions userFunctions;

	/** Registerseiten des Dialogs */
	private JTabbedPane tabs;

	/** Auswahlboxen für die zu verwendende Zeiteinheit auf den verschiedenen Dialogseiten */
	private JComboBox<String>[] timeBase;

	/* Dialogseite "Bedienzeiten" */

	/** Auswahlbox zur Auswahl als was die Prozesszeit erfasst werden soll */
	private JComboBox<String> processTimeType;
	/** Panel zur Konfiguration der Prozesszeiten */
	private DistributionBySubTypeEditor distributionsWorking;

	/* Dialogseite "Rüstzeiten" */

	/** Panel zur Konfiguration der Rüstzeiten */
	private DistributionSetupTimesEditor editorSetupTimes;
	/** Kann ein Kunde das Warten auch noch während der Rüstzeit aufgeben? */
	private JCheckBox canCancelInSetupTime;

	/* Dialogseite "Nachbearbeitungszeiten" */

	/** Option: Nachbearbeitungszeiten verwenden? */
	private JCheckBox checkBoxPostProcessing;
	/** Panel zur Konfiguration der Nachbearbeitungszeiten */
	private DistributionBySubTypeEditor distributionsPostProcessing;

	/* Dialogseite "Wartezeittoleranzen" */

	/** Option: Kunden sind nur bereit, begrenzt lange zu warten */
	private JCheckBox checkBoxCancel;
	/** Panel zur Konfiguration der Wartezeittoleranzen */
	private DistributionBySubTypeEditor distributionsCancel;

	/* Dialogseite "Prioritäten und Batch-Größen" */

	/** Eingabefeld für die minimale Batch-Größe */
	private JTextField textBatchMin;
	/** Eingabefeld für die maximale Batch-Größe */
	private JTextField textBatchMax;
	/** Option: Kampagnen-Modus */
	private JCheckBox campaignMode;
	/** Tabelle zur Konfiguration der Prioritäten der Kundentypen */
	private PriorityTableModel tablePriorityModel;

	/* Dialogseite "Bediener" */

	/** Eingabefeld zur Konfiguration der Ressourcen-Zuweisungs-Priorität der Station */
	private JTextField textResourcePriority;
	/** Reihenfolge in der die Ressourcen-Alternativen auf Verfügbarkeit geprüft werden (in angegebener Reihenfolge oder zufällig) */
	private JComboBox<String> resourceCheckOrder;
	/** Panel in dem {@link #resourceAssistantUse} angeboten wird */
	private JPanel resourceAssistant;
	/** Schaltfläche "Neue Bedienergruppe anlegen" */
	private JButton resourceAssistantUse;

	/** Tabellendarstellung der notwendigen Bediener getrennt nach Alternativen */
	private MultiResourceTable resourceData;


	/* Dialogseite "Kosten" */

	/** Eingabefeld für die Kosten pro Bedienvorgang */
	private JTextField textCosts;
	/** Eingabefeld für die Kosten pro Bediensekunde */
	private JTextField textCostsPerProcessSecond;
	/** Eingabefeld für die Kosten pro Nachbearbeitungssekunde */
	private JTextField textCostsPerPostProcessSecond;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementProcess}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementProcessDialog(final Component owner, final ModelElementProcess element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Process.Dialog.Title"),element,"ModelElementProcess",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setSizeRespectingScreensize(1025,700);
		pack();
		setMaxSizeRespectingScreensize(1025,700);
	}

	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Liefert eine der gekoppelten Auswahlboxen zur Auswahl
	 * der Zeiteinheit für Verteilungen und Ausdrücke.
	 * @param index	Nummer (0..3) der Auswahlbox
	 * @return	Auswahlbox für die Zeiteinheit
	 */
	@SuppressWarnings("unchecked")
	private JPanel getTimeBasePanel(final int index) {
		if (timeBase==null) timeBase=new JComboBox[4];

		JPanel sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label;
		sub.add(label=new JLabel(Language.tr("Surface.Process.Dialog.TimeBase")+":"));
		sub.add(timeBase[index]=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase[index].setEnabled(!readOnly);
		timeBase[index].setSelectedIndex(((ModelElementProcess)element).getTimeBase().id);
		label.setLabelFor(timeBase[index]);

		timeBase[index].addActionListener(e-> {
			if (!(e.getSource() instanceof JComboBox<?>)) return;
			final int index1=((JComboBox<String>)e.getSource()).getSelectedIndex();
			for (int i=0;i<timeBase.length;i++) if (timeBase[i]!=null && timeBase[i].getSelectedIndex()!=index1) timeBase[i].setSelectedIndex(index1);
		});

		return sub;
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationProcess;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final ModelElementProcess process=(ModelElementProcess)element;

		variables=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true);
		variablesNoClient=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false);
		userFunctions=element.getModel().userFunctions;

		tabs=new JTabbedPane();
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		JPanel tab, area, sub, line;
		JLabel label;
		DistributionOrExpressionFromOtherStation loader;

		/* Tab "Bedienzeiten" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.ProcessingTimes"),tab=new JPanel(new BorderLayout()));
		sub=getTimeBasePanel(0);
		sub.add(label=new JLabel(Language.tr("Surface.Process.Dialog.TimeIs")));
		sub.add(processTimeType=new JComboBox<>(new String[]{
				Language.tr("Surface.Process.Dialog.TimeIs.WaitingTime"),
				Language.tr("Surface.Process.Dialog.TimeIs.TransferTime"),
				Language.tr("Surface.Process.Dialog.TimeIs.ProcessingTime"),
				Language.tr("Surface.Process.Dialog.TimeIs.Nothing")
		}));
		processTimeType.setEnabled(!readOnly);
		switch (process.getProcessTimeType()) {
		case PROCESS_TYPE_WAITING: processTimeType.setSelectedIndex(0); break;
		case PROCESS_TYPE_TRANSFER: processTimeType.setSelectedIndex(1); break;
		case PROCESS_TYPE_PROCESS: processTimeType.setSelectedIndex(2); break;
		case PROCESS_TYPE_NOTHING: processTimeType.setSelectedIndex(3); break;
		}
		label.setLabelFor(processTimeType);

		tab.add(sub,BorderLayout.NORTH);
		distributionsWorking=new DistributionBySubTypeEditor(element.getModel(),element.getSurface(),readOnly,Language.tr("Surface.Process.Dialog.DistributionOfProcessingTimes"),process.getWorking(),DistributionBySubTypeEditor.Mode.MODE_CLIENTS);
		tab.add(distributionsWorking,BorderLayout.CENTER);

		loader=new DistributionOrExpressionFromOtherStation(element.getModel());
		distributionsWorking.setupSpecialButton(Language.tr("Surface.LoadTimes.Button.Title"),Language.tr("Surface.LoadTimes.Button.Tooltip"),Images.MODEL_ADD_STATION.getIcon(),loader.getShowLoadMenu(record->{
			if (record.id==element.getId()) {
				if (record.type==null) {
					distributionsWorking.setDataForCurrentView(distributionsWorking.getCurrentData().get());
				} else {
					distributionsWorking.setDataForCurrentView(distributionsWorking.getCurrentData().get(record.type));
				}
			} else {
				distributionsWorking.setDataForCurrentView(record.data);
			}
		}));

		/* Tab "Rüstzeiten" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.SetupTimes"),tab=new JPanel(new BorderLayout()));
		tab.add(area=new JPanel(),BorderLayout.NORTH);
		area.setLayout(new BoxLayout(area,BoxLayout.PAGE_AXIS));
		area.add(getTimeBasePanel(1));

		if (DistributionSetupTimesEditor.showSetupTimesEditor(element.getSurface().getClientTypes().size())) {
			tab.add(editorSetupTimes=new DistributionSetupTimesEditor(element.getModel(),element.getSurface(),readOnly,process.getSetupTimes()),BorderLayout.CENTER);
			editorSetupTimes.addUserChangeListener(e->updateTabTitles());
		} else {
			editorSetupTimes=null;
			area.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			sub.add(new JLabel("<html><body style='color: red;'><b>"+Language.tr("Surface.Process.Dialog.Tab.SetupTimes.TooManyClientTypes")+"</b></body></html>"));
		}

		tab.add(area=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		area.add(canCancelInSetupTime=new JCheckBox(Language.tr("Surface.Process.Dialog.Tab.SetupTimes.CanCancelInSetupTime"),process.isCanCancelInSetupTime()));
		canCancelInSetupTime.setToolTipText(Language.tr("Surface.Process.Dialog.Tab.SetupTimes.CanCancelInSetupTime.Info"));

		loader=new DistributionOrExpressionFromOtherStation(element.getModel());
		editorSetupTimes.setupSpecialButton(Language.tr("Surface.LoadTimes.Button.Title"),Language.tr("Surface.LoadTimes.Button.Tooltip"),Images.MODEL_ADD_STATION.getIcon(),loader.getShowLoadMenu(record->{
			if (record.id==element.getId()) {
				editorSetupTimes.setDataForCurrentView(editorSetupTimes.getCurrentData().get(record.type,record.type2));
			} else {
				editorSetupTimes.setDataForCurrentView(record.data);
			}
		}));

		/* Tab "Nachbearbeitungszeiten" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.PostProcessingTimes"),tab=new JPanel(new BorderLayout()));
		tab.add(area=new JPanel(),BorderLayout.NORTH);
		area.setLayout(new BoxLayout(area,BoxLayout.PAGE_AXIS));
		area.add(getTimeBasePanel(2));
		area.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(checkBoxPostProcessing=new JCheckBox("<html><b>"+Language.tr("Surface.Process.Dialog.UsePostProcessingTimes")+"</b></html>"));
		checkBoxPostProcessing.setEnabled(!readOnly);
		checkBoxPostProcessing.setSelected(process.getPostProcessing().get()!=null);
		checkBoxPostProcessing.addActionListener(e->updateTabTitles());
		distributionsPostProcessing=new DistributionBySubTypeEditor(element.getModel(),element.getSurface(),readOnly,Language.tr("Surface.Process.Dialog.DistributionOfPostProcessingTimes"),process.getPostProcessing(),DistributionBySubTypeEditor.Mode.MODE_CLIENTS);
		distributionsPostProcessing.addUserChangeListener(e->{checkBoxPostProcessing.setSelected(true); updateTabTitles();});
		tab.add(distributionsPostProcessing,BorderLayout.CENTER);

		loader=new DistributionOrExpressionFromOtherStation(element.getModel());
		distributionsPostProcessing.setupSpecialButton(Language.tr("Surface.LoadTimes.Button.Title"),Language.tr("Surface.LoadTimes.Button.Tooltip"),Images.MODEL_ADD_STATION.getIcon(),loader.getShowLoadMenu(record->{
			if (record.id==element.getId()) {
				if (record.type==null) {
					distributionsPostProcessing.setDataForCurrentView(distributionsPostProcessing.getCurrentData().get());
				} else {
					distributionsPostProcessing.setDataForCurrentView(distributionsPostProcessing.getCurrentData().get(record.type));
				}
			} else {
				distributionsPostProcessing.setDataForCurrentView(record.data);
			}
		}));

		/* Tab "Wartezeittoleranzen" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.WaitingTimeTolerances"),tab=new JPanel(new BorderLayout()));
		tab.add(area=new JPanel(),BorderLayout.NORTH);
		area.setLayout(new BoxLayout(area,BoxLayout.PAGE_AXIS));
		area.add(getTimeBasePanel(3));
		area.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(checkBoxCancel=new JCheckBox("<html><b>"+Language.tr("Surface.Process.Dialog.UseWaitingTimeTolerances")+"</b></html>"));
		checkBoxCancel.setEnabled(!readOnly);
		checkBoxCancel.setSelected(process.getCancel().get()!=null);
		checkBoxCancel.addActionListener(e->updateTabTitles());
		distributionsCancel=new DistributionBySubTypeEditor(element.getModel(),element.getSurface(),readOnly,Language.tr("Surface.Process.Dialog.DistributionOfWaitingTimeTolerances"),process.getCancel(),DistributionBySubTypeEditor.Mode.MODE_CLIENTS);
		distributionsCancel.addUserChangeListener(e->{checkBoxCancel.setSelected(true); updateTabTitles();});
		tab.add(distributionsCancel,BorderLayout.CENTER);

		loader=new DistributionOrExpressionFromOtherStation(element.getModel());
		distributionsCancel.setupSpecialButton(Language.tr("Surface.LoadTimes.Button.Title"),Language.tr("Surface.LoadTimes.Button.Tooltip"),Images.MODEL_ADD_STATION.getIcon(),loader.getShowLoadMenu(record->{
			if (record.id==element.getId()) {
				if (record.type==null) {
					distributionsCancel.setDataForCurrentView(distributionsCancel.getCurrentData().get());
				} else {
					distributionsCancel.setDataForCurrentView(distributionsCancel.getCurrentData().get(record.type));
				}
			} else {
				distributionsCancel.setDataForCurrentView(record.data);
			}
		}));

		/* Tab "Prioritäten und Batch-Größen" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.PrioritiesAndBatchSizes"),tab=new JPanel(new BorderLayout()));
		tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Process.Dialog.MinimumBatchSize")+":"));
		sub.add(textBatchMin=new JTextField(4));
		ModelElementBaseDialog.addUndoFeature(textBatchMin);
		textBatchMin.setEnabled(!readOnly);
		textBatchMin.setText(""+process.getBatchMinimum());
		label.setLabelFor(textBatchMin);
		textBatchMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		sub.add(label=new JLabel(Language.tr("Surface.Process.Dialog.MaximumBatchSize")+":"));
		sub.add(textBatchMax=new JTextField(4));
		ModelElementBaseDialog.addUndoFeature(textBatchMax);
		textBatchMax.setEnabled(!readOnly);
		textBatchMax.setText(""+process.getBatchMaximum());
		label.setLabelFor(textBatchMax);
		textBatchMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		sub.add(Box.createHorizontalStrut(10));
		sub.add(campaignMode=new JCheckBox(Language.tr("Surface.Process.Dialog.CampaignMode"),process.isCampaignMode()));
		campaignMode.setToolTipText(Language.tr("Surface.Process.Dialog.CampaignMode.Info"));
		campaignMode.setEnabled(!readOnly);

		final JTableExt tablePriority;
		tab.add(new JScrollPane(tablePriority=new JTableExt()),BorderLayout.CENTER);
		tablePriority.setModel(tablePriorityModel=new PriorityTableModel(tablePriority,process,readOnly));
		tablePriority.setIsPanelCellTable(0);
		tablePriority.setIsPanelCellTable(2);
		tablePriority.getColumnModel().getColumn(0).setMaxWidth(200);
		tablePriority.getColumnModel().getColumn(0).setMinWidth(200);
		tablePriority.getColumnModel().getColumn(2).setMaxWidth(75);
		tablePriority.getColumnModel().getColumn(2).setMinWidth(75);
		tablePriority.setEnabled(!readOnly);
		tablePriority.putClientProperty("terminateEditOnFocusLost",true);
		tablePriority.getTableHeader().setReorderingAllowed(false);
		tablePriority.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					final int row=tablePriority.getSelectedRow();
					final int column=tablePriority.getSelectedColumn();
					if (column==1 && row>=0) {
						final JPopupMenu menu=new JPopupMenu();
						JMenuItem item;
						menu.add(item=new JMenuItem(Language.tr("Surface.Process.Dialog.Tab.PrioritiesAndBatchSizes.PrioritiesPopup.FIFO")));
						item.addActionListener(ev->tablePriority.setValueAt("w",row,1));
						menu.add(item=new JMenuItem(Language.tr("Surface.Process.Dialog.Tab.PrioritiesAndBatchSizes.PrioritiesPopup.Random")));
						item.addActionListener(ev->tablePriority.setValueAt("random()",row,1));
						menu.add(item=new JMenuItem(Language.tr("Surface.Process.Dialog.Tab.PrioritiesAndBatchSizes.PrioritiesPopup.LIFO")));
						item.addActionListener(ev->tablePriority.setValueAt("-w",row,1));
						menu.show(tablePriority,e.getX(),e.getY());
					}
				}
			}
		});

		/* Tab "Bediener" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.Operators"),tab=new JPanel(new BorderLayout()));

		tab.add(area=new JPanel(),BorderLayout.NORTH);
		area.setLayout(new BoxLayout(area,BoxLayout.PAGE_AXIS));

		Object[] data=getInputPanel(Language.tr("Surface.Process.Dialog.ResourcePriority")+":",process.getResourcePriority());
		textResourcePriority=(JTextField)data[1];
		area.add(sub=(JPanel)data[0]);
		sub.add(getExpressionEditButton(this,textResourcePriority,false,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		textResourcePriority.setEnabled(!readOnly);
		textResourcePriority.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});

		area.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Process.Dialog.ResourceCheckOrder")+":"));
		line.add(resourceCheckOrder=new JComboBox<>(new String[] {
				Language.tr("Surface.Process.Dialog.ResourceCheckOrder.InOrder"),
				Language.tr("Surface.Process.Dialog.ResourceCheckOrder.Random")
		}));
		label.setLabelFor(resourceCheckOrder);
		resourceCheckOrder.setSelectedIndex(process.isResourceCheckInRandomOrder()?1:0);
		resourceCheckOrder.setEnabled(!readOnly);
		resourceCheckOrder.setRenderer(new IconListCellRenderer(new Images[] {
				Images.ARROW_DOWN,
				Images.ARROW_SWITCH
		}));

		area.add(resourceAssistant=getAssistentPanel());

		tab.add(resourceData=new MultiResourceTable(process,helpRunnable,readOnly,()->updateTabTitles()),BorderLayout.CENTER);

		if (!readOnly) {
			final JButton resourceButton=getOpenModelOperatorsButton();
			if (resourceButton!=null) {
				tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
				sub.add(resourceButton);
			}
		}

		/* Tab "Kosten" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.Costs"),tab=new JPanel(new BorderLayout()));
		tab.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		data=getInputPanel(Language.tr("Surface.Process.Dialog.CostsPerClient")+":",process.getCosts());
		textCosts=(JTextField)data[1];
		sub.add((JPanel)data[0]);
		textCosts.setEnabled(!readOnly);
		textCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,textCosts,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Process.Dialog.CostsPerProcessingSecond")+":",process.getCostsPerProcessSecond());
		textCostsPerProcessSecond=(JTextField)data[1];
		sub.add((JPanel)data[0]);
		textCostsPerProcessSecond.setEnabled(!readOnly);
		textCostsPerProcessSecond.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,textCostsPerProcessSecond,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Process.Dialog.CostsPerPostProcessingSecond")+":",process.getCostsPerPostProcessSecond());
		textCostsPerPostProcessSecond=(JTextField)data[1];
		sub.add((JPanel)data[0]);
		textCostsPerPostProcessSecond.setEnabled(!readOnly);
		textCostsPerPostProcessSecond.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,textCostsPerPostProcessSecond,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		/* System vorbereiten */

		updateTabTitles();

		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_SERVICE.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_SETUP.getIcon());
		tabs.setIconAt(2,Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_POST_PROCESS.getIcon());
		tabs.setIconAt(3,Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_CANCEL.getIcon());
		tabs.setIconAt(4,Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_PRORITY.getIcon());
		tabs.setIconAt(5,Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_RESOURCES.getIcon());
		tabs.setIconAt(6,Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_COSTS.getIcon());

		return tabs;
	}

	/**
	 * Erstellt das Info-Panel zum Aufrufen des Dialogs zur
	 * automatischen Erstellung einer Bedienergruppe.
	 * @return	Panel zur Anzeige von {@link #resourceAssistantUse}
	 * @see #resourceAssistant
	 */
	private JPanel getAssistentPanel() {
		final JPanel result=new JPanel(new FlowLayout(FlowLayout.LEFT));

		if (FlatLaFHelper.isDark()) {
			result.setBorder(BorderFactory.createLineBorder(new Color(0,180,0)));
			result.setBackground(new Color(0,90,0));
		} else {
			result.setBorder(BorderFactory.createLineBorder(new Color(0,180,0)));
			result.setBackground(new Color(200,255,200));
		}

		JButton button;

		result.add(button=new JButton(Language.tr("Surface.Process.Dialog.Assistant.NewGroup")));
		button.setIcon(Images.MODELPROPERTIES_OPERATORS_ADD.getIcon());
		button.addActionListener(e->resourceData.addNewGroup());

		result.add(button=new JButton(Language.tr("Surface.Process.Dialog.Assistant.UseGroup")));
		button.setIcon(Images.MODELPROPERTIES_OPERATORS_ADD.getIcon());
		resourceAssistantUse=button;
		button.addActionListener(e->{
			final ModelResources resources=element.getModel().resources;
			final JPopupMenu popup=new JPopupMenu();
			for (int i=0;i<resources.size();i++) {
				final String name=resources.getName(i);
				final JMenuItem item=new JMenuItem(name);
				item.setIcon(Images.MODELPROPERTIES_OPERATORS.getIcon());
				item.addActionListener(e2->resourceData.addExistingGroup(name));
				popup.add(item);
			}
			popup.show(resourceAssistantUse,0,resourceAssistantUse.getHeight());
		});

		return result;
	}

	/**
	 * Prüft die Eingaben im Dialog.
	 * @param showErrorMessage	Soll im Fehlerfall eine Fehlermeldung ausgegeben werden?
	 * @return	Liefert <code>true</code>, wenn alle Eingaben in Ordnung sind
	 */
	private boolean checkInput(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;
		String text;

		final Long LbatchMin=NumberTools.getPositiveLong(textBatchMin,true);
		if (LbatchMin==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Process.Dialog.MinimumBatchSize.Error.Title"),Language.tr("Surface.Process.Dialog.MinimumBatchSize.Error.Info"));
				return false;
			}
			ok=false;
		}
		final Long LbatchMax=NumberTools.getPositiveLong(textBatchMax,true);
		if (LbatchMax==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Process.Dialog.MaximumBatchSize.Error.Title"),Language.tr("Surface.Process.Dialog.MaximumBatchSize.Error.Info"));
				return false;
			}
			ok=false;
		}

		if (ok && LbatchMin!=null && LbatchMax!=null) {
			long min=LbatchMin;
			long max=LbatchMax;
			if (max<min) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Process.Dialog.MaximumBatchSize.Error.RangeTitle"),Language.tr("Surface.Process.Dialog.MaximumBatchSize.Error.RangeInfo"));
					return false;
				}
				ok=false;
			}
		}

		text=textResourcePriority.getText();
		int error=ExpressionCalc.check(text,variablesNoClient,userFunctions);
		if (error>=0) {
			textResourcePriority.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Process.Dialog.ResourcePriority.Error.Title"),String.format(Language.tr("Surface.Process.Dialog.ResourcePriority.Error.Info"),text));
				return false;
			}
			ok=false;
		} else {
			textResourcePriority.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (!tablePriorityModel.checkInput(showErrorMessage)) {
			if (showErrorMessage) return false;
			ok=false;
		}

		text=textCosts.getText();
		if (!text.trim().isEmpty()) {
			error=ExpressionCalc.check(text,variables,userFunctions);
			if (error>=0) {
				textCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Process.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Process.Dialog.CostsPerClient.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				textCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			textCosts.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		text=textCostsPerProcessSecond.getText();
		if (!text.trim().isEmpty()) {
			error=ExpressionCalc.check(text,variables,userFunctions);
			if (error>=0) {
				textCostsPerProcessSecond.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Process.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Process.Dialog.CostsPerProcessingSecond.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				textCostsPerProcessSecond.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			textCostsPerProcessSecond.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		text=textCostsPerPostProcessSecond.getText();
		if (!text.trim().isEmpty()) {
			error=ExpressionCalc.check(text,variables,userFunctions);
			if (error>=0) {
				textCostsPerPostProcessSecond.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Process.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Process.Dialog.CostsPerPostProcessingSecond.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				textCostsPerPostProcessSecond.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		} else {
			textCostsPerPostProcessSecond.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkInput(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementProcess process=(ModelElementProcess)element;

		process.setTimeBase(ModelSurface.TimeBase.byId(timeBase[0].getSelectedIndex()));

		switch (processTimeType.getSelectedIndex()) {
		case 0: process.setProcessTimeType(ModelElementProcess.ProcessType.PROCESS_TYPE_WAITING); break;
		case 1: process.setProcessTimeType(ModelElementProcess.ProcessType.PROCESS_TYPE_TRANSFER); break;
		case 2: process.setProcessTimeType(ModelElementProcess.ProcessType.PROCESS_TYPE_PROCESS); break;
		case 3: process.setProcessTimeType(ModelElementProcess.ProcessType.PROCESS_TYPE_NOTHING); break;
		}

		distributionsWorking.storeData();
		if (editorSetupTimes!=null) editorSetupTimes.storeData();
		process.setCanCancelInSetupTime(canCancelInSetupTime.isSelected());
		if (checkBoxPostProcessing.isSelected()) distributionsPostProcessing.storeData(); else process.getPostProcessing().set(null);
		if (checkBoxCancel.isSelected()) distributionsCancel.storeData(); else process.getCancel().set(null);

		process.setBatchMinimum(NumberTools.getInteger(textBatchMin,true));
		process.setBatchMaximum(NumberTools.getInteger(textBatchMax,true));
		process.setCampaignMode(campaignMode.isSelected());
		tablePriorityModel.storeData();

		process.setResourcePriority(textResourcePriority.getText());
		process.setResourceCheckInRandomOrder(resourceCheckOrder.getSelectedIndex()==1);
		resourceData.store();

		process.setCosts(textCosts.getText());
		process.setCostsPerProcessSecond(textCostsPerProcessSecond.getText());
		process.setCostsPerPostProcessSecond(textCostsPerPostProcessSecond.getText());
	}

	/**
	 * Passt die Beschriftungen auf den Registerreitern an,
	 * wenn sich die Einstellungen im Dialog verändert haben.
	 */
	private void updateTabTitles() {
		final String html1="<html><body>";
		final String html2="</body></html>";

		final String on="<span style=\"color: green;\"><b>"+Language.tr("Surface.Process.Dialog.on")+"</b></span>";
		final String off=Language.tr("Surface.Process.Dialog.off");
		final String missing="<span style=\"color: red;\"><b>"+Language.tr("Surface.Process.Dialog.StillMissing").toUpperCase()+"</b></span>";

		if (editorSetupTimes!=null) {
			tabs.setTitleAt(1,html1+Language.tr("Surface.Process.Dialog.Tab.SetupTimes")+": "+((editorSetupTimes.isActive())?on:off)+html2);
		}
		tabs.setTitleAt(2,html1+Language.tr("Surface.Process.Dialog.Tab.PostProcessingTimes")+": "+((checkBoxPostProcessing.isSelected())?on:off)+html2);
		tabs.setTitleAt(3,html1+Language.tr("Surface.Process.Dialog.Tab.WaitingTimeTolerances")+": "+((checkBoxCancel.isSelected())?on:off)+html2);
		tabs.setTitleAt(5,html1+Language.tr("Surface.Process.Dialog.Tab.Operators")+((resourceData==null || resourceData.isResourceDefined())?"":": "+missing)+html2);

		resourceAssistant.setVisible(!readOnly && resourceData!=null && !resourceData.isResourceDefined());
		resourceAssistantUse.setVisible(element.getModel().resources.size()>0);
	}
}
