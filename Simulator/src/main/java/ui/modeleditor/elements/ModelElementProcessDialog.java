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
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
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

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSurface;

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

	private String[] variables;

	private JTabbedPane tabs;

	private JComboBox<String>[] timeBase;
	private JComboBox<String> processTimeType;
	private DistributionBySubTypeEditor distributionsWorking;

	private DistributionSetupTimesEditor editorSetupTimes;

	private JCheckBox checkBoxPostProcessing;
	private DistributionBySubTypeEditor distributionsPostProcessing;

	private JCheckBox checkBoxCancel;
	private DistributionBySubTypeEditor distributionsCancel;

	private JTextField textBatchMin;
	private JTextField textBatchMax;
	private JTextField textResourcePriority;
	private JPanel resourceAssistant;
	private JButton resourceAssistantUse;
	private MultiResourceTable resourceData;
	private PriorityTableModel tablePriorityModel;

	private JTextField textCosts;
	private JTextField textCostsPerProcessSecond;
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
		setSizeRespectingScreensize(1025,600);
		pack();
	}

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

		timeBase[index].addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!(e.getSource() instanceof JComboBox<?>)) return;
				final int index=((JComboBox<String>)e.getSource()).getSelectedIndex();
				for (int i=0;i<timeBase.length;i++) if (timeBase[i]!=null && timeBase[i].getSelectedIndex()!=index) timeBase[i].setSelectedIndex(index);
			}
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

		tabs=new JTabbedPane();
		JPanel tab, area, sub;
		JLabel label;

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

		/* Tab "Rüstzeiten" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.SetupTimes"),tab=new JPanel(new BorderLayout()));
		tab.add(area=new JPanel(),BorderLayout.NORTH);
		area.setLayout(new BoxLayout(area,BoxLayout.PAGE_AXIS));
		area.add(getTimeBasePanel(2));
		tab.add(editorSetupTimes=new DistributionSetupTimesEditor(element.getModel(),element.getSurface(),readOnly,process.getSetupTimes()),BorderLayout.CENTER);
		editorSetupTimes.addUserChangeListener(e->updateTabTitles());

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

		/* Tab "Prioritäten und Batch-Größen" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.PrioritiesAndBatchSizes"),tab=new JPanel(new BorderLayout()));
		tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Process.Dialog.MinimumBatchSize")+":"));
		sub.add(textBatchMin=new JTextField(4));
		textBatchMin.setEditable(!readOnly);
		textBatchMin.setText(""+process.getBatchMinimum());
		label.setLabelFor(textBatchMin);
		textBatchMin.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		sub.add(label=new JLabel(Language.tr("Surface.Process.Dialog.MaximumBatchSize")+":"));
		sub.add(textBatchMax=new JTextField(4));
		textBatchMax.setEditable(!readOnly);
		textBatchMax.setText(""+process.getBatchMaximum());
		label.setLabelFor(textBatchMax);
		textBatchMax.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});

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

		/* Tab "Bediener" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.Operators"),tab=new JPanel(new BorderLayout()));

		tab.add(area=new JPanel(),BorderLayout.NORTH);
		area.setLayout(new BoxLayout(area,BoxLayout.PAGE_AXIS));
		Object[] data=getInputPanel(Language.tr("Surface.Process.Dialog.ResourcePriority")+":",process.getResourcePriority());
		textResourcePriority=(JTextField)data[1];
		area.add(sub=(JPanel)data[0]);
		sub.add(getExpressionEditButton(this,textResourcePriority,false,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		textResourcePriority.setEditable(!readOnly);
		textResourcePriority.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		area.add(resourceAssistant=getAssistentPanel());

		tab.add(resourceData=new MultiResourceTable(process,helpRunnable,readOnly,()->updateTabTitles()),BorderLayout.CENTER);

		final JButton resourceButton=getOpenModelOperatorsButton();
		if (resourceButton!=null) {
			tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
			sub.add(resourceButton);
		}

		/* Tab "Kosten" */
		tabs.addTab(Language.tr("Surface.Process.Dialog.Tab.Costs"),tab=new JPanel(new BorderLayout()));
		tab.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		data=getInputPanel(Language.tr("Surface.Process.Dialog.CostsPerClient")+":",process.getCosts());
		textCosts=(JTextField)data[1];
		sub.add((JPanel)data[0]);
		textCosts.setEditable(!readOnly);
		textCosts.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,textCosts,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Process.Dialog.CostsPerProcessingSecond")+":",process.getCostsPerProcessSecond());
		textCostsPerProcessSecond=(JTextField)data[1];
		sub.add((JPanel)data[0]);
		textCostsPerProcessSecond.setEditable(!readOnly);
		textCostsPerProcessSecond.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkInput(false);}
			@Override public void keyReleased(KeyEvent e) {checkInput(false);}
			@Override public void keyPressed(KeyEvent e) {checkInput(false);}
		});
		((JPanel)data[0]).add(getExpressionEditButton(this,textCostsPerProcessSecond,false,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

		data=getInputPanel(Language.tr("Surface.Process.Dialog.CostsPerPostProcessingSecond")+":",process.getCostsPerPostProcessSecond());
		textCostsPerPostProcessSecond=(JTextField)data[1];
		sub.add((JPanel)data[0]);
		textCostsPerPostProcessSecond.setEditable(!readOnly);
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

	private JPanel getAssistentPanel() {
		final JPanel result=new JPanel(new FlowLayout(FlowLayout.LEFT));

		result.setBorder(BorderFactory.createLineBorder(new Color(0,180,0)));
		result.setBackground(new Color(200,255,200));

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

	private boolean requestOpenResourceDialog=false;

	/**
	 * Liefert <code>true</code>, wenn der Dialog mit der Anweisung geschlossen wurde,
	 * den Modelleigenschaften-Dialog zur Bearbeitung der Ressourcen zu öffnen.
	 * Der Aufrufer dieses dialogs muss prüfen, ob diese Methode <code>true</code> liefert.
	 * @return	Soll unmittelbar nach dem Schließen dieses Dialogs der Modelleigenschaften-Dialog zur Bearbeitung der Ressourcen geöffnet werden?
	 */
	public boolean isRequestOpenResourceDialog() {
		return requestOpenResourceDialog;
	}

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

		int error=ExpressionCalc.check(textResourcePriority.getText(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
		if (error>=0) {
			textResourcePriority.setBackground(Color.red);
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Process.Dialog.ResourcePriority.Error.Title"),String.format(Language.tr("Surface.Process.Dialog.ResourcePriority.Error.Info"),textResourcePriority.getText()));
				return false;
			}
			ok=false;
		} else {
			textResourcePriority.setBackground(SystemColor.text);
		}

		if (!tablePriorityModel.checkInput(showErrorMessage)) {
			if (showErrorMessage) return false;
			ok=false;
		}

		text=textCosts.getText();
		if (!text.trim().isEmpty()) {
			error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				textCosts.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Process.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Process.Dialog.CostsPerClient.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				textCosts.setBackground(SystemColor.text);
			}
		} else {
			textCosts.setBackground(SystemColor.text);
		}

		text=textCostsPerProcessSecond.getText();
		if (!text.trim().isEmpty()) {
			error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				textCostsPerProcessSecond.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Process.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Process.Dialog.CostsPerProcessingSecond.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				textCostsPerProcessSecond.setBackground(SystemColor.text);
			}
		} else {
			textCostsPerProcessSecond.setBackground(SystemColor.text);
		}

		text=textCostsPerPostProcessSecond.getText();
		if (!text.trim().isEmpty()) {
			error=ExpressionCalc.check(text,variables);
			if (error>=0) {
				textCostsPerPostProcessSecond.setBackground(Color.red);
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Process.Dialog.CostsError.Title"),String.format(Language.tr("Surface.Process.Dialog.CostsPerPostProcessingSecond.ErrorInfo"),text,error+1));
					return false;
				}
				ok=false;
			} else {
				textCostsPerPostProcessSecond.setBackground(SystemColor.text);
			}
		} else {
			textCostsPerPostProcessSecond.setBackground(SystemColor.text);
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
		editorSetupTimes.storeData();
		if (checkBoxPostProcessing.isSelected()) distributionsPostProcessing.storeData(); else process.getPostProcessing().set(null);
		if (checkBoxCancel.isSelected()) distributionsCancel.storeData(); else process.getCancel().set(null);

		process.setBatchMinimum(NumberTools.getInteger(textBatchMin,true));
		process.setBatchMaximum(NumberTools.getInteger(textBatchMax,true));
		tablePriorityModel.storeData();

		process.setResourcePriority(textResourcePriority.getText());
		resourceData.store();

		process.setCosts(textCosts.getText());
		process.setCostsPerProcessSecond(textCostsPerProcessSecond.getText());
		process.setCostsPerPostProcessSecond(textCostsPerPostProcessSecond.getText());
	}

	private void updateTabTitles() {
		final String html1="<html><body>";
		final String html2="</body></html>";

		final String on="<span style=\"color: green;\"><b>"+Language.tr("Surface.Process.Dialog.on")+"</b></span>";
		final String off=Language.tr("Surface.Process.Dialog.off");
		final String missing="<span style=\"color: red;\"><b>"+Language.tr("Surface.Process.Dialog.StillMissing").toUpperCase()+"</b></span>";

		tabs.setTitleAt(1,html1+Language.tr("Surface.Process.Dialog.Tab.SetupTimes")+": "+((editorSetupTimes.isActive())?on:off)+html2);
		tabs.setTitleAt(2,html1+Language.tr("Surface.Process.Dialog.Tab.PostProcessingTimes")+": "+((checkBoxPostProcessing.isSelected())?on:off)+html2);
		tabs.setTitleAt(3,html1+Language.tr("Surface.Process.Dialog.Tab.WaitingTimeTolerances")+": "+((checkBoxCancel.isSelected())?on:off)+html2);
		tabs.setTitleAt(5,html1+Language.tr("Surface.Process.Dialog.Tab.Operators")+((resourceData==null || resourceData.isResourceDefined())?"":": "+missing)+html2);

		resourceAssistant.setVisible(!readOnly && resourceData!=null && !resourceData.isResourceDefined());
		resourceAssistantUse.setVisible(element.getModel().resources.size()>0);
	}
}
