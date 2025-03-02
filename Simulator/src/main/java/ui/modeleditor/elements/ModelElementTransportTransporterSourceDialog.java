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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

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
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTransportTransporterSource}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTransportTransporterSource
 */
public class ModelElementTransportTransporterSourceDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -9026608309380773385L;

	/** Namen aller modellweit verfügbaren Variablennamen */
	private String[] variableNames;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private ExpressionCalcModelUserFunctions userFunctions;
	/** Liste mit den Namen aller modellweit verfügbaren Transporter */
	private String[] transporterNames;

	/** Auswahl des Transportertyps für diese Station */
	private JComboBox<String> transporterType;
	/** Eingabefeld für die Mindestanzahl an wartenden Kunden bevor ein Transporter angefordert wird */
	private JTextField requestMinWaiting;
	/** Eingabefeld für die Priorität zur Anforderung von Transportern (für einen unmittelbaren Transport) */
	private JTextField requestPriority;
	/** Eingabefeld für die Transporter-Wartekapazität an dieser Station */
	private JTextField waitingCapacity;
	/** Eingabefeld für die Priorität zur Anforderung von Transportern (zum Parken) */
	private JTextField waitingPriority;

	/**
	 * Objekt zur Konfiguration der Transport-Ziele
	 */
	private TransportTargetSystemPanel transportTargetSystemPanel;

	/**
	 * Tabelle zur Konfiguration der Kundenprioritäten
	 */
	private PriorityTableModel tablePriorityModel;

	/** Option: Bereich verlassen bei Transportbeginn */
	private JCheckBox useSectionStart;
	/** Auswahl des im Falle von {@link #useSectionStart} zu verlassenden Bereichs */
	private JComboBox<String> sectionStart;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTransportTransporterSource}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTransportTransporterSourceDialog(final Component owner, final ModelElementTransportTransporterSource element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TransportTransporterSource.Dialog.Title"),element,"ModelElementTransportTransporterSource",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(750,650);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationTransportTransporterSource;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementTransportTransporterSource source=(ModelElementTransportTransporterSource)element;

		variableNames=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false);
		userFunctions=element.getModel().userFunctions;
		transporterNames=element.getModel().transporters.getNames();

		final JTabbedPane tabs=new JTabbedPane();
		JPanel tab;

		/* Tab "Transporter" */
		tabs.addTab(Language.tr("Surface.TransportTransporterSource.Dialog.Tab.Transporter"),tab=new JPanel(new BorderLayout()));
		tab.add(getTransporterTab(),BorderLayout.NORTH);

		/* Tab "Routing-Ziele" */
		tabs.addTab(Language.tr("Surface.TransportTransporterSource.Dialog.Tab.RoutingTargets"),tab=new JPanel(new BorderLayout()));
		tab.add(transportTargetSystemPanel=new TransportTargetSystemPanel(source.getTransportTargetSystem(),readOnly,source.getModel(),source.getSurface(),helpRunnable));

		/* Tab "Prioritäten" */
		tabs.addTab(Language.tr("Surface.TransportTransporterSource.Dialog.Tab.Priorities"),tab=new JPanel(new BorderLayout()));
		tab.add(getPrioritiesTab(),BorderLayout.CENTER);

		/* Tab "Bereich" */
		tabs.addTab(Language.tr("Surface.TransportTransporterSource.Dialog.Tab.SectionEnd"),tab=new JPanel(new BorderLayout()));
		buildSectionsTab(tab);

		/* System vorbereiten */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_TRANSPORT_TRANSPORTER.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET.getIcon());
		tabs.setIconAt(2,Images.MODELEDITOR_ELEMENT_TRANSPORT_PRIORITIES.getIcon());
		tabs.setIconAt(3,Images.MODELEDITOR_ELEMENT_TRANSPORT_BATCH.getIcon());

		checkData(false);

		return tabs;
	}

	/**
	 * Erzeugt die Dialog-Registerseite "Transporter"
	 * @return	Dialog-Registerseite
	 */
	private JPanel getTransporterTab() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		Object[] data;

		/* Transportertyp */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TransportTransporterSource.Dialog.TransporterType")+":"));
		line.add(transporterType=new JComboBox<>(transporterNames));
		transporterType.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildTransporterTypeIcons(transporterNames,element.getModel())));
		transporterType.setEnabled(!readOnly);
		label.setLabelFor(transporterType);
		final String type=((ModelElementTransportTransporterSource)element).getTransporterType();
		int index=-1;
		for (int i=0;i<transporterNames.length;i++) if (type.equalsIgnoreCase(transporterNames[i])) {index=i; break;}
		if (index<0 && transporterNames.length>0) index=0;
		if (index>=0) transporterType.setSelectedIndex(index);

		/* Überschrift: "Anforderung von Transportern" */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.TransportTransporterSource.Dialog.Heading.Request")+"</b></body></html>"));

		/* Mindestanzahl für Anforderung */
		data=getInputPanel(Language.tr("Surface.TransportTransporterSource.Dialog.RequestMinWaiting")+":",""+((ModelElementTransportTransporterSource)element).getRequestMinWaiting(),5);
		content.add((JPanel)data[0]);
		requestMinWaiting=(JTextField)data[1];
		requestMinWaiting.setEnabled(!readOnly);
		requestMinWaiting.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		requestMinWaiting.addActionListener(e->checkData(false));

		/* Priorität bei Transporteranforderung */
		data=getInputPanel(Language.tr("Surface.TransportTransporterSource.Dialog.RequestPriority")+":",((ModelElementTransportTransporterSource)element).getRequestPriority());
		content.add((JPanel)data[0]);
		requestPriority=(JTextField)data[1];
		requestPriority.setEnabled(!readOnly);
		requestPriority.addActionListener(e->checkData(false));
		((JPanel)data[0]).add(getExpressionEditButton(this,requestPriority,false,false,element.getModel(),element.getModel().surface),BorderLayout.EAST);
		requestPriority.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Überschrift: "Parkplatz" */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.TransportTransporterSource.Dialog.Heading.Parking")+"</b></body></html>"));

		/* Parkplatz: Kapazität */
		data=getInputPanel(Language.tr("Surface.TransportTransporterSource.Dialog.WaitingCapacity")+":",""+((ModelElementTransportTransporterSource)element).getWaitingCapacity(),5);
		content.add((JPanel)data[0]);
		waitingCapacity=(JTextField)data[1];
		waitingCapacity.setEnabled(!readOnly);
		waitingCapacity.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		waitingCapacity.addActionListener(e->checkData(false));

		/* Parkplatz: Priorität */
		data=getInputPanel(Language.tr("Surface.TransportTransporterSource.Dialog.WaitingPriority")+":",((ModelElementTransportTransporterSource)element).getWaitingPriority());
		content.add((JPanel)data[0]);
		waitingPriority=(JTextField)data[1];
		waitingPriority.setEnabled(!readOnly);
		waitingPriority.addActionListener(e->checkData(false));
		((JPanel)data[0]).add(getExpressionEditButton(this,waitingPriority,false,false,element.getModel(),element.getModel().surface),BorderLayout.EAST);
		waitingPriority.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Button zum Öffnen der Transporter-Liste in den Modelleigenschaften */
		final JButton transportersButton=getOpenModelTransportersButton();
		if (transportersButton!=null) {
			final JPanel sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
			content.add(sub);
			sub.add(transportersButton);
		}

		return content;
	}

	/**
	 * Erzeugt die Dialog-Registerseite "Prioritäten"
	 * @return	Dialog-Registerseite
	 */
	private JPanel getPrioritiesTab() {
		final JPanel content=new JPanel(new BorderLayout());

		final JTableExt tablePriority;
		content.add(new JScrollPane(tablePriority=new JTableExt()),BorderLayout.CENTER);
		tablePriority.setModel(tablePriorityModel=new PriorityTableModel(tablePriority,element,readOnly));
		tablePriority.setIsPanelCellTable(0);
		tablePriority.setIsPanelCellTable(2);
		tablePriority.getColumnModel().getColumn(0).setMaxWidth(200);
		tablePriority.getColumnModel().getColumn(0).setMinWidth(200);
		tablePriority.setEnabled(!readOnly);
		tablePriority.putClientProperty("terminateEditOnFocusLost",true);
		tablePriority.getTableHeader().setReorderingAllowed(false);

		return content;
	}

	/**
	 * Erzeugt die Dialog-Registerseite "Bereich verlassen"
	 * @param tab	Dialog-Registerseite in die die Dialogelement eingefügt werden sollen
	 */
	private void buildSectionsTab(final JPanel tab) {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		tab.add(content,BorderLayout.NORTH);

		JPanel line;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useSectionStart=new JCheckBox(Language.tr("Surface.TransportTransporterSource.Dialog.SectionEnd.Use")));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel label=new JLabel(Language.tr("Surface.TransportTransporterSource.Dialog.SectionEnd.SectionStart")+":");
		line.add(label);

		final String[] stations=getSectionStartStations();
		line.add(sectionStart=new JComboBox<>(stations));
		label.setLabelFor(sectionStart);
		sectionStart.setEnabled(!readOnly);

		int index=-1;
		final String s=((ModelElementTransportTransporterSource)element).getSectionStartName();
		if (!s.isEmpty()) for (int i=0;i<stations.length;i++) if (stations[i].equalsIgnoreCase(s)) {index=i; break;}
		if (index<0) {
			useSectionStart.setSelected(false);
			if (stations.length>0) sectionStart.setSelectedIndex(0);
		} else {
			useSectionStart.setSelected(true);
			sectionStart.setSelectedIndex(index);
		}
		useSectionStart.setEnabled(!readOnly && stations.length>0);

		sectionStart.addActionListener(e->useSectionStart.setSelected(true));
	}

	/**
	 * Liefert die Namen aller Bereich-Betreten-Stationen
	 * (um diese als Option zum Verlassen beim Transport anzubieten).
	 * @return	Liste der Namen aller Bereich-Betreten-Stationen
	 * @see #useSectionStart
	 * @see #sectionStart
	 */
	private String[] getSectionStartStations() {
		final List<String> list=new ArrayList<>();

		final ModelSurface mainSurface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		for (ModelElement element: mainSurface.getElements()) {
			if (element instanceof ModelElementSectionStart && !element.getName().trim().isEmpty() && !list.contains(element.getName())) list.add(element.getName());
			if (element instanceof ModelElementSub) {
				for (ModelElement element2 : ((ModelElementSub)element).getSubSurface().getElements()) {
					if (element2 instanceof ModelElementSectionStart && !element2.getName().trim().isEmpty() && !list.contains(element2.getName())) list.add(element2.getName());
				}
			}
		}

		return list.toArray(String[]::new);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		Long L;
		int error;

		boolean ok=true;

		/* Tab "Transporter" */

		/* Mindestanzahl für Anforderung */
		L=NumberTools.getPositiveLong(requestMinWaiting,true);
		if (L==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TransportTransporterSource.Dialog.RequestMinWaiting.ErrorTitle"),String.format(Language.tr("Surface.TransportTransporterSource.Dialog.RequestMinWaiting.ErrorInfo"),requestMinWaiting.getText()));
				return false;
			}
		}

		/* Priorität bei Transporteranforderung */
		error=ExpressionCalc.check(requestPriority.getText(),variableNames,userFunctions);
		if (error>=0) {
			requestPriority.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TransportTransporterSource.Dialog.RequestPriority.ErrorTitle"),String.format(Language.tr("Surface.TransportTransporterSource.Dialog.RequestPriority.ErrorInfo"),requestPriority.getText(),error+1));
				return false;
			}
		} else {
			requestPriority.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Parkplatz: Kapazität */
		L=NumberTools.getPositiveLong(waitingCapacity,true);
		if (L==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TransportTransporterSource.Dialog.WaitingCapacity.ErrorTitle"),String.format(Language.tr("Surface.TransportTransporterSource.Dialog.WaitingCapacity.ErrorInfo"),waitingCapacity.getText()));
				return false;
			}
		}

		/* Parkplatz: Priorität */
		error=ExpressionCalc.check(waitingPriority.getText(),variableNames,userFunctions);
		if (error>=0) {
			waitingPriority.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TransportTransporterSource.Dialog.WaitingPriority.ErrorTitle"),String.format(Language.tr("Surface.TransportTransporterSource.Dialog.WaitingPriority.ErrorInfo"),waitingPriority.getText(),error+1));
				return false;
			}
		} else {
			waitingPriority.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Tab "Prioritäten" */
		if (!tablePriorityModel.checkInput(showErrorMessages)) {
			if (showErrorMessages) return false;
			ok=false;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		if (!transportTargetSystemPanel.checkData()) return false;
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		/* Tab "Routing-Ziele" */
		if (transporterType.getSelectedIndex()>=0) ((ModelElementTransportTransporterSource)element).setTransporterType(transporterNames[transporterType.getSelectedIndex()]);
		((ModelElementTransportTransporterSource)element).setRequestMinWaiting(NumberTools.getPositiveLong(requestMinWaiting,true).intValue());
		((ModelElementTransportTransporterSource)element).setRequestPriority(requestPriority.getText());
		((ModelElementTransportTransporterSource)element).setWaitingCapacity(NumberTools.getPositiveLong(waitingCapacity,true).intValue());
		((ModelElementTransportTransporterSource)element).setWaitingPriority(waitingPriority.getText());

		/* Tab "Routing-Ziele" */
		transportTargetSystemPanel.storeData();

		/* Tab "Prioritäten" */
		tablePriorityModel.storeData();

		/* Tab "Bereich" */
		if (!useSectionStart.isSelected()) {
			((ModelElementTransportTransporterSource)element).setSectionStartName("");
		} else {
			if (sectionStart.getSelectedIndex()<0) {
				((ModelElementTransportTransporterSource)element).setSectionStartName("");
			} else {
				((ModelElementTransportTransporterSource)element).setSectionStartName((String)sectionStart.getSelectedItem());
			}
		}
	}
}
