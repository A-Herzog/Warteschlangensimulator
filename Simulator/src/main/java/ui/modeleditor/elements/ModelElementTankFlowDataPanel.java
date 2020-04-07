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

import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Hält die Daten für ein {@link ModelElementTankFlowData}-Element vor
 * @author Alexander Herzog
 * @see ModelElementTankFlowData
 */
public class ModelElementTankFlowDataPanel extends JPanel {
	private static final long serialVersionUID = -4252147656372254664L;

	private final List<String> sourceNames;
	private final List<Integer> sourceIDs;
	private final List<Integer> sourceValveCount;
	private final List<String> destinationNames;
	private final List<Integer> destinationIDs;
	private final List<Integer> destinationValveCount;
	private final List<String> signalNames;

	private final JComboBox<String> sourceID;
	private final JComboBox<String> sourceValve;

	private final JComboBox<String> destinationID;
	private final JComboBox<String> destinationValve;

	private final JRadioButton stopTime;
	private final JTextField stopTimeEdit;
	private final JRadioButton stopQuantity;
	private final JTextField stopQuantityEdit;
	private final JRadioButton stopSignal;
	private final JComboBox<String> stopSignalCombo;

	private final ModelElementTankFlowData data;
	private final boolean readOnly;
	private final Runnable reflowDialog;

	/**
	 * Konstruktor der Klasse
	 * @param data	Datenelement das in diesem Panel bearbeitet werden soll
	 * @param mainSurface	Primäre Zeichenfläche
	 * @param readOnly	Nur-Lese-Status für das Modell
	 * @param reflowDialog	Wird hier ein Wert ungleich <code>null</code> übergeben, so wird dieses Runnable am Ende der Initialisierung des Panels ausgelöst, um so einem übergeordneten Dialog zu ermöglichen, sich neu auszurichten
	 * @see ModelElementTankFlowData
	 */
	public ModelElementTankFlowDataPanel(final ModelElementTankFlowData data, final ModelSurface mainSurface, final boolean readOnly, final Runnable reflowDialog) {
		super();
		this.data=data;
		this.readOnly=readOnly;
		this.reflowDialog=reflowDialog;
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;

		/* Daten vorbereiten */
		sourceNames=new ArrayList<>();
		sourceIDs=new ArrayList<>();
		sourceValveCount=new ArrayList<>();
		destinationNames=new ArrayList<>();
		destinationIDs=new ArrayList<>();
		destinationValveCount=new ArrayList<>();
		signalNames=mainSurface.getAllSignalNames();
		initLists(mainSurface);

		/* Quelle */
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TankFlowPanel.Source")+":"));
		line.add(sourceID=new JComboBox<>(sourceNames.toArray(new String[0])));
		label.setLabelFor(sourceID);
		sourceID.setEnabled(!readOnly);
		sourceID.addActionListener(e->updateValveComboSource());
		line.add(sourceValve=new JComboBox<>());

		/* Ziel */
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TankFlowPanel.Destination")+":"));
		line.add(destinationID=new JComboBox<>(destinationNames.toArray(new String[0])));
		label.setLabelFor(destinationID);
		destinationID.setEnabled(!readOnly);
		destinationID.addActionListener(e->updateValveComboDestination());
		line.add(destinationValve=new JComboBox<>());

		/* Stoppbedingung */
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel("<html><body><b>"+Language.tr("Surface.TankFlowPanel.StopCondition")+"</b></body></html>"));

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(stopTime=new JRadioButton(Language.tr("Surface.TankFlowPanel.StopCondition.Time")+":"));
		stopTime.setEnabled(!readOnly);
		stopTime.addActionListener(e->checkData(false));
		line.add(stopTimeEdit=new JTextField("1",10));
		stopTimeEdit.setEditable(!readOnly);
		stopTimeEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {stopTime.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {stopTime.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {stopTime.setSelected(true); checkData(false);}
		});
		line.add(new JLabel(Language.tr("Surface.TankFlowPanel.StopCondition.Time.Seconds")));

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(stopQuantity=new JRadioButton(Language.tr("Surface.TankFlowPanel.StopCondition.Quantity")+":"));
		stopQuantity.setEnabled(!readOnly);
		stopQuantity.addActionListener(e->checkData(false));
		line.add(stopQuantityEdit=new JTextField("1",10));
		stopQuantityEdit.setEditable(!readOnly);
		stopQuantityEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {stopQuantity.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {stopQuantity.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {stopQuantity.setSelected(true); checkData(false);}
		});

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(stopSignal=new JRadioButton(Language.tr("Surface.TankFlowPanel.StopCondition.Signal")+":"));
		stopSignal.setEnabled(!readOnly && signalNames.size()>0);
		stopSignal.addActionListener(e->checkData(false));
		line.add(stopSignalCombo=new JComboBox<>(signalNames.toArray(new String[0])));
		stopSignalCombo.setEnabled(!readOnly && signalNames.size()>0);
		stopSignalCombo.addActionListener(e->{stopSignal.setSelected(true); checkData(false);});

		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(stopTime);
		buttonGroup.add(stopQuantity);
		buttonGroup.add(stopSignal);

		/* Daten in Dialog laden */
		int index;

		index=sourceIDs.indexOf(data.getSourceID());
		if (index<0) index=0;
		sourceID.setSelectedIndex(index);
		updateValveComboSource();
		index=data.getSourceValveNr();
		if (index<0 && sourceValve.getModel().getSize()>0) index=0;
		if (index>sourceValve.getModel().getSize() && sourceValve.getModel().getSize()>0) index=0;
		if (index>0 && sourceValve.getModel().getSize()>0) sourceValve.setSelectedIndex(index);

		index=destinationIDs.indexOf(data.getDestinationID());
		if (index<0) index=0;
		destinationID.setSelectedIndex(index);
		updateValveComboDestination();
		index=data.getDestinationValveNr();
		if (index<0 && destinationValve.getModel().getSize()>0) index=0;
		if (index>destinationValve.getModel().getSize() && destinationValve.getModel().getSize()>0) index=0;
		if (index>0 && destinationValve.getModel().getSize()>0) destinationValve.setSelectedIndex(index);

		switch (data.getStopCondition()) {
		case STOP_BY_TIME:
			stopTime.setSelected(true);
			stopTimeEdit.setText(NumberTools.formatNumberMax(data.getStopTime()));
			break;
		case STOP_BY_QUANTITY:
			stopQuantity.setSelected(true);
			stopQuantityEdit.setText(NumberTools.formatNumberMax(data.getStopQuantity()));
			break;
		case STOP_BY_SIGNAL:
			stopSignal.setSelected(true);
			index=signalNames.indexOf(data.getStopSignal());
			if (index<0 && signalNames.size()>0) index=0;
			if (index>=0) stopSignalCombo.setSelectedIndex(index);
			break;
		default:
			stopTime.setSelected(true);
			stopTimeEdit.setText("1");
			break;
		}
	}

	private void updateValveComboSource() {
		final int index=sourceID.getSelectedIndex();
		final int count=(index>=0)?sourceValveCount.get(index):0;
		if (count<=0) {
			sourceValve.setModel(new DefaultComboBoxModel<>());
			sourceValve.setEnabled(false);
			return;
		}

		final List<String> valves=new ArrayList<>();
		for (int i=0;i<count;i++) valves.add(String.format(Language.tr("Surface.TankFlowPanel.ValveNr"),i+1));
		sourceValve.setModel(new DefaultComboBoxModel<>(valves.toArray(new String[0])));
		sourceValve.setEnabled(!readOnly);

		if (reflowDialog!=null) reflowDialog.run();
	}

	private void updateValveComboDestination() {
		final int index=destinationID.getSelectedIndex();
		final int count=(index>=0)?destinationValveCount.get(index):0;
		if (count<=0) {
			destinationValve.setModel(new DefaultComboBoxModel<>());
			destinationValve.setEnabled(false);
			return;
		}

		final List<String> valves=new ArrayList<>();
		for (int i=0;i<count;i++) valves.add(String.format(Language.tr("Surface.TankFlowPanel.ValveNr"),i+1));
		destinationValve.setModel(new DefaultComboBoxModel<>(valves.toArray(new String[0])));
		destinationValve.setEnabled(!readOnly);

		if (reflowDialog!=null) reflowDialog.run();
	}

	private void checkAndAddToLists(final ModelElement element) {
		if (element instanceof ModelElementTank) {
			final ModelElementTank tank=(ModelElementTank)element;
			final String name=String.format("%s \"%s\" (id=%d)",Language.tr("Surface.Tank.Name"),tank.getName(),tank.getId());
			sourceNames.add(name);
			sourceIDs.add(tank.getId());
			sourceValveCount.add(tank.getValves().size());
			destinationNames.add(name);
			destinationIDs.add(tank.getId());
			destinationValveCount.add(tank.getValves().size());
		}
	}

	private void initLists(final ModelSurface mainSurface) {
		sourceNames.add(Language.tr("Surface.TankFlowPanel.Source.NotConnected"));
		sourceIDs.add(-1);
		sourceValveCount.add(0);

		destinationNames.add(Language.tr("Surface.TankFlowPanel.Destination.NotConnected"));
		destinationIDs.add(-1);
		destinationValveCount.add(0);

		for (ModelElement element1: mainSurface.getElements()) {
			checkAndAddToLists(element1);
			if (element1 instanceof ModelElementSub) for (ModelElement element2 : ((ModelElementSub)element1).getSubSurface().getElements()) {
				checkAndAddToLists(element2);
			}
		}
	}

	/**
	 * Prüft die Eingaben
	 * @param showErrorMessages	Fehlermeldung anzeigen ja/nein
	 * @return	Gibt <code>true</code> zurück, wenn alle Eingaben in Ordnung sind
	 */
	public boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		/* Quelle */

		if (sourceID.getSelectedIndex()>0 && sourceValve.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankFlowPanel.Source.ErrorNoValveTitle"),Language.tr("Surface.TankFlowPanel.Source.ErrorNoValveInfo"));
				return false;
			}
		}

		/* Ziel */

		if (destinationID.getSelectedIndex()>0 && destinationValve.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankFlowPanel.Destination.ErrorNoValveTitle"),Language.tr("Surface.TankFlowPanel.Destination.ErrorNoValveInfo"));
				return false;
			}
		}

		/* Quelle <-> Ziel */

		if (sourceID.getSelectedIndex()==0 && destinationID.getSelectedIndex()==0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankFlowPanel.SourceDestination.ErrorTitle"),Language.tr("Surface.TankFlowPanel.SourceDestination.ErrorInfo"));
				return false;
			}
		}

		if (sourceID.getSelectedIndex()==destinationID.getSelectedIndex()) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankFlowPanel.SourceDestination.ErrorSameTitle"),Language.tr("Surface.TankFlowPanel.SourceDestination.ErrorSameInfo"));
				return false;
			}
		}

		/* Stoppbedingung */

		if (stopTime.isSelected()) {
			final Double D=NumberTools.getPositiveDouble(stopTimeEdit,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.TankFlowPanel.StopCondition.Time.ErrorTitle"),String.format(Language.tr("Surface.TankFlowPanel.StopCondition.Time.ErrorInfo"),stopTimeEdit.getText()));
					return false;
				}
			}
		} else {
			stopTimeEdit.setBackground(SystemColor.text);
		}

		if (stopQuantity.isSelected()) {
			final Double D=NumberTools.getPositiveDouble(stopQuantityEdit,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.TankFlowPanel.StopCondition.Quantity.ErrorTitle"),String.format(Language.tr("Surface.TankFlowPanel.StopCondition.Quantity.ErrorInfo"),stopQuantityEdit.getText()));
					return false;
				}
			}
		} else {
			stopQuantityEdit.setBackground(SystemColor.text);
		}

		if (stopSignal.isSelected() && stopSignalCombo.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.TankFlowPanel.StopCondition.Signal.ErrorTitle"),Language.tr("Surface.TankFlowPanel.StopCondition.Signal.ErrorInfo"));
				return false;
			}
		}

		return ok;
	}

	/**
	 * Schreibt die Einstellungen in das im Konstruktor übergebene {@link ModelElementTankFlowData}-Element zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		int index;

		/* Quelle */
		index=sourceID.getSelectedIndex();
		data.setSourceID(sourceIDs.get(index));
		if (index>0) data.setSourceValveNr(sourceValve.getSelectedIndex());

		/* Ziel */
		index=destinationID.getSelectedIndex();
		data.setDestinationID(destinationIDs.get(index));
		if (index>0) data.setDestinationValveNr(destinationValve.getSelectedIndex());

		/* Stoppbedingung */
		if (stopTime.isSelected()) data.setStopByTime(NumberTools.getPositiveDouble(stopTimeEdit,true));
		if (stopQuantity.isSelected()) data.setStopByQuantity(NumberTools.getPositiveDouble(stopQuantityEdit,true));
		if (stopSignal.isSelected()) data.setStopBySignal((String)stopSignalCombo.getSelectedItem());
	}
}
