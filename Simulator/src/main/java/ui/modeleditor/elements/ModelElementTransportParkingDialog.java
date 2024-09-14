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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTransportParking}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTransportParking
 */
public class ModelElementTransportParkingDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1937351216296016056L;

	/** Liste der Namen aller modellweit verfügbaren Variablennamen */
	private String[] variableNames;
	/** Modellspezifische nutzerdefinierte Funktionen */
	private ExpressionCalcModelUserFunctions userFunctions;
	/** Liste mit den Namen aller modellweit verfügbaren Transporter */
	private String[] transporterNames;

	/** Auswahl des Transportertyps für diese Station */
	private JComboBox<String> transporterType;
	/** Eingabefeld für die Transporter-Wartekapazität an dieser Station */
	private JTextField waitingCapacity;
	/** Eingabefeld für die Priorität zur Anforderung von Transportern */
	private JTextField waitingPriority;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTransportParking}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTransportParkingDialog(final Component owner, final ModelElementTransportParking element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TransportParking.Dialog.Title"),element,"ModelElementTransportParking",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationTransportParking;
	}

	@Override
	protected JComponent getContentPanel() {
		variableNames=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false);
		userFunctions=element.getModel().userFunctions;
		transporterNames=element.getModel().transporters.getNames();

		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;
		Object[] data;

		/* Transportertyp */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TransportParking.Dialog.TransporterType")+":"));
		line.add(transporterType=new JComboBox<>(transporterNames));
		transporterType.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildTransporterTypeIcons(transporterNames,element.getModel())));
		transporterType.setEnabled(!readOnly);
		label.setLabelFor(transporterType);
		final String type=((ModelElementTransportParking)element).getTransporterType();
		int index=-1;
		for (int i=0;i<transporterNames.length;i++) if (type.equalsIgnoreCase(transporterNames[i])) {index=i; break;}
		if (index<0 && transporterNames.length>0) index=0;
		if (index>=0) transporterType.setSelectedIndex(index);

		/* Parkplatz: Kapazität */
		data=getInputPanel(Language.tr("Surface.TransportParking.Dialog.WaitingCapacity")+":",""+((ModelElementTransportParking)element).getWaitingCapacity(),5);
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
		data=getInputPanel(Language.tr("Surface.TransportParking.Dialog.WaitingPriority")+":",((ModelElementTransportParking)element).getWaitingPriority());
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

		checkData(false);

		return content;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		setMaxSizeRespectingScreensize(600,1000);
		pack();
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
		setMaxSizeRespectingScreensize(600,1000);
		setSize(getWidth(),getHeight()+(int)Math.round(30*windowScaling));
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Parkplatz: Kapazität */
		final Long L=NumberTools.getPositiveLong(waitingCapacity,true);
		if (L==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TransportParking.Dialog.WaitingCapacity.ErrorTitle"),String.format(Language.tr("Surface.TransportParking.Dialog.WaitingCapacity.ErrorInfo"),waitingCapacity.getText()));
				return false;
			}
		}
		/* Parkplatz: Priorität */
		final int error=ExpressionCalc.check(waitingPriority.getText(),variableNames,userFunctions);
		if (error>=0) {
			waitingPriority.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TransportParking.Dialog.WaitingPriority.ErrorTitle"),String.format(Language.tr("Surface.TransportParking.Dialog.WaitingPriority.ErrorInfo"),waitingPriority.getText(),error+1));
				return false;
			}
		} else {
			waitingPriority.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		super.storeData();

		if (transporterType.getSelectedIndex()>=0) ((ModelElementTransportParking)element).setTransporterType(transporterNames[transporterType.getSelectedIndex()]);
		((ModelElementTransportParking)element).setWaitingCapacity(NumberTools.getPositiveLong(waitingCapacity,true).intValue());
		((ModelElementTransportParking)element).setWaitingPriority(waitingPriority.getText());
	}
}
