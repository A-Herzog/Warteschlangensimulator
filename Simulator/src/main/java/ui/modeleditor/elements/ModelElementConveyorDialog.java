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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
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
import systemtools.MsgBox;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementConveyor}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementConveyor
 */
public final class ModelElementConveyorDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8867788548959155049L;

	/** Liste aller globalen Variablen in dem Modell */
	private String[] variables;
	private List<String> clientTypes;

	private JTextField capacityAvailable;
	private JTextField capacityNeededGlobal;
	private List<JTextField> capacityNeeded;

	private JComboBox<String> timeBase;
	private JTextField transportTime;
	private JComboBox<String> transportTimeType;

	private JComboBox<String> moveDirection;
	private JTextField conveyorSize;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementConveyor}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementConveyorDialog(final Component owner, final ModelElementConveyor element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Conveyor.Dialog.Title"),element,"ModelElementConveyor",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(500,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationConveyor;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		variables=element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true);

		final ModelElementConveyor conveyor=(ModelElementConveyor)element;

		JPanel tabOuter, tab, sub;
		Object[] data;
		JLabel label;

		/* Kapazität */
		tabs.addTab(Language.tr("Surface.Conveyor.Dialog.Capacity"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));

		/* Kapazität - Verfügbar */
		data=getInputPanel(Language.tr("Surface.Conveyor.Dialog.CapacityAvailable")+":",NumberTools.formatNumber(conveyor.getCapacityAvailable()),10);
		tab.add((JPanel)data[0]);
		capacityAvailable=(JTextField)data[1];
		capacityAvailable.setEnabled(!readOnly);
		addListener(capacityAvailable);

		/* Kapazität - Benötigt global */
		data=getInputPanel(Language.tr("Surface.Conveyor.Dialog.CapacityNeededGlobal")+":",conveyor.getCapacityNeededGlobal());
		sub=(JPanel)data[0];
		tab.add(sub);
		capacityNeededGlobal=(JTextField)data[1];
		capacityNeededGlobal.setEnabled(!readOnly);
		sub.add(getExpressionEditButton(this,capacityNeededGlobal,false,true,element.getModel(),element.getModel().surface),BorderLayout.EAST);
		addListener(capacityNeededGlobal);

		/* Kapazität - Benötigt individuell */
		clientTypes=element.getSurface().getClientTypes();
		capacityNeeded=new ArrayList<>();
		if (clientTypes.size()>0) {
			tabOuter.add(new JScrollPane(tab=new JPanel()),BorderLayout.CENTER);
			tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));
			tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			sub.add(new JLabel(Language.tr("Surface.Conveyor.Dialog.CapacityNeeded.Info")));
			for (String clientType: clientTypes) {
				String value=conveyor.getCapacityNeeded(clientType);
				if (value==null) value="";
				data=getInputPanel(String.format(Language.tr("Surface.Conveyor.Dialog.CapacityNeeded"),clientType)+":",value);
				sub=(JPanel)data[0];
				tab.add(sub);
				final JTextField field=(JTextField)data[1];
				field.setEnabled(!readOnly);
				capacityNeeded.add(field);
				sub.add(getExpressionEditButton(this,field,false,true,element.getModel(),element.getModel().surface),BorderLayout.EAST);
				addListener(field);
			}
		}

		/* Zeit */
		tabs.addTab(Language.tr("Surface.Conveyor.Dialog.Time"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));

		/* Zeit - Zeitbasis */
		tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Conveyor.Dialog.TimeBase")+":"));
		sub.add(timeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase.setEnabled(!readOnly);
		timeBase.setSelectedIndex(conveyor.getTimeBase().id);
		label.setLabelFor(timeBase);

		/* Zeit - Transportzeit */
		data=getInputPanel(Language.tr("Surface.Conveyor.Dialog.TransportTime")+":",NumberTools.formatNumber(conveyor.getTransportTime()),10);
		tab.add((JPanel)data[0]);
		transportTime=(JTextField)data[1];
		transportTime.setEnabled(!readOnly);
		addListener(transportTime);

		/* Zeit - Erfassung als */
		tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Conveyor.Dialog.TransportTimeType")+":"));
		sub.add(transportTimeType=new JComboBox<>(new String[] {
				Language.tr("Surface.Conveyor.Dialog.TransportTimeType.WaitingTime"),
				Language.tr("Surface.Conveyor.Dialog.TransportTimeType.TransferTime"),
				Language.tr("Surface.Conveyor.Dialog.TransportTimeType.ProcessTime")
		}));
		transportTimeType.setEnabled(!readOnly);
		switch (conveyor.getTransportTimeType()) {
		case TRANSPORT_TYPE_WAITING: transportTimeType.setSelectedIndex(0); break;
		case TRANSPORT_TYPE_TRANSFER: transportTimeType.setSelectedIndex(1); break;
		case TRANSPORT_TYPE_PROCESS: transportTimeType.setSelectedIndex(2); break;
		default: transportTimeType.setSelectedIndex(1); break;
		}
		label.setLabelFor(transportTimeType);

		/* Animation */
		tabs.addTab(Language.tr("Surface.Conveyor.Dialog.Animation"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));

		/* Animation - Richtung */
		tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Conveyor.Dialog.AnimationDirection")+":"));
		sub.add(moveDirection=new JComboBox<>(new String[] {
				Language.tr("Surface.Conveyor.Dialog.AnimationDirection.LeftToRight"),
				Language.tr("Surface.Conveyor.Dialog.AnimationDirection.RightToLeft")
		}));
		moveDirection.setEnabled(!readOnly);
		switch (conveyor.getMoveDirection()) {
		case MOVE_LEFT_TO_RIGHT: moveDirection.setSelectedIndex(0); break;
		case MOVE_RIGHT_TO_LEFT: moveDirection.setSelectedIndex(1); break;
		default: moveDirection.setSelectedIndex(0); break;
		}
		label.setLabelFor(moveDirection);

		/* Animation - Fließbandlänge*/
		data=getInputPanel(Language.tr("Surface.Conveyor.Dialog.ConveyorSize")+":",NumberTools.formatNumber(conveyor.getWidth()/10),10);
		tab.add((JPanel)data[0]);
		conveyorSize=(JTextField)data[1];
		conveyorSize.setEnabled(!readOnly);
		addListener(conveyorSize);

		/* Icons */
		tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_CONVEYOR_PAGE_CAPACITY.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_CONVEYOR_PAGE_TIME.getIcon());
		tabs.setIconAt(2,Images.MODELEDITOR_ELEMENT_CONVEYOR_PAGE_ANIMATION.getIcon());

		/* Werte prüfen */
		checkData(false);

		return content;
	}

	private void addListener(final JTextField field) {
		field.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		Double D;
		int error;

		/* Kapazität - Verfügbar */
		D=NumberTools.getPositiveDouble(capacityAvailable,true);
		if (D==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Conveyor.Dialog.CapacityAvailable.ErrorTitle"),String.format(Language.tr("Surface.Conveyor.Dialog.CapacityAvailable.ErrorInfo"),capacityAvailable.getText()));
				return false;
			}
		}

		/* Kapazität - Benötigt global */
		error=ExpressionCalc.check(capacityNeededGlobal.getText(),variables);
		if (error>=0) {
			capacityNeededGlobal.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Conveyor.Dialog.CapacityNeededGlobal.ErrorTitle"),String.format(Language.tr("Surface.Conveyor.Dialog.CapacityNeededGlobal.ErrorInfo"),capacityNeededGlobal.getText(),error+1));
				return false;
			}
		} else {
			capacityNeededGlobal.setBackground(SystemColor.text);
		}

		/* Kapazität - Benötigt individuell */
		for (int i=0;i<capacityNeeded.size();i++) {
			final JTextField field=capacityNeeded.get(i);
			if (field.getText().trim().isEmpty()) {
				field.setBackground(SystemColor.text);
			} else {
				error=ExpressionCalc.check(field.getText(),variables);
				if (error>=0) {
					field.setBackground(Color.RED);
					ok=false;
					if (showErrorMessage) {
						MsgBox.error(this,Language.tr("Surface.Conveyor.Dialog.CapacityNeeded.ErrorTitle"),String.format(Language.tr("Surface.Conveyor.Dialog.CapacityNeeded.ErrorInfo"),clientTypes.get(i),field.getText(),error+1));
						return false;
					}
				} else {
					field.setBackground(SystemColor.text);
				}
			}
		}

		/* Zeit - Transportzeit */
		D=NumberTools.getNotNegativeDouble(transportTime,true);
		if (D==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Conveyor.Dialog.TransportTime.ErrorTitle"),String.format(Language.tr("Surface.Conveyor.Dialog.TransportTime.ErrorInfo"),transportTime.getText()));
				return false;
			}
		}

		/* Animation - Fließbandlänge*/
		final Long L=NumberTools.getPositiveLong(conveyorSize,true);
		if (L==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Conveyor.Dialog.ConveyorSize.ErrorTitle"),String.format(Language.tr("Surface.Conveyor.Dialog.ConveyorSize.ErrorInfo"),conveyorSize.getText()));
				return false;
			}
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

		final ModelElementConveyor conveyor=(ModelElementConveyor)element;

		/* Kapazität */
		conveyor.setCapacityAvailable(NumberTools.getPositiveDouble(capacityAvailable,true));
		conveyor.setCapacityNeededGlobal(capacityNeededGlobal.getText());
		for (int i=0;i<capacityNeeded.size();i++) {
			final String value=capacityNeeded.get(i).getText().trim();
			conveyor.setCapacityNeeded(clientTypes.get(i),value.isEmpty()?null:value);
		}

		/* Zeit */
		conveyor.setTimeBase(ModelSurface.TimeBase.byId(timeBase.getSelectedIndex()));
		conveyor.setTransportTime(NumberTools.getNotNegativeDouble(transportTime,true));
		switch (transportTimeType.getSelectedIndex()) {
		case 0: conveyor.setTransportTimeType(ModelElementConveyor.TransportTimeType.TRANSPORT_TYPE_WAITING); break;
		case 1: conveyor.setTransportTimeType(ModelElementConveyor.TransportTimeType.TRANSPORT_TYPE_TRANSFER); break;
		case 2: conveyor.setTransportTimeType(ModelElementConveyor.TransportTimeType.TRANSPORT_TYPE_PROCESS); break;
		}

		/* Animation */
		switch (moveDirection.getSelectedIndex()) {
		case 0: conveyor.setMoveDirection(ModelElementConveyor.MoveDirection.MOVE_LEFT_TO_RIGHT); break;
		case 1: conveyor.setMoveDirection(ModelElementConveyor.MoveDirection.MOVE_RIGHT_TO_LEFT); break;
		}
		conveyor.setWidth(NumberTools.getPositiveLong(conveyorSize,true).intValue()*10);
	}
}