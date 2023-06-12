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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zum Bearbeiten eines einzelnen Datensatzes in einer {@link BarrierSignalTableModel}-Tabelle
 * @author Alexander Herzog
 * @see BarrierSignalTableModel
 * @see ModelElementBarrier
 * @see ModelElementBarrierSignalOption
 */
public class BarrierSignalTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7619263878414705864L;

	/** Liste mit allen Signalnamen im System */
	private final String[] signals;
	/** Liste mit allen Kundentypnamen im System */
	private final String[] clientTypes;

	/** Auswahlbox zur Auswahl des Signals bei dem die Freigabe erfolgen soll */
	private final JComboBox<String> signal;
	/** Eingabefeld für die initiale Anzahl an Freigaben */
	private final JTextField initialClients;
	/** Option: Bei Signal bestimmte Anzahl an Kunden freigeben */
	private final JRadioButton optionNumber;
	/** Option: Bei Signal alle wartenden Kunden freigeben */
	private final JRadioButton optionAll;
	/** Eingabefeld für die Anzahl an Kunden die pro Signal freigegeben werden sollen */
	private final JTextField clientsPerSignal;
	/** Signale zwischenspeichern, wenn keine Kunden warten */
	private final JCheckBox storeSignals;
	/** Auswahl auf die Kunden welches Typs sich die Freigabe beziehen soll (inkl. der Möglichkeit "alle Kundentypen") */
	private JComboBox<String> clientType;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Callback
	 * @param option	Zu bearbeitender Datensatz
	 * @param signals	Liste mit allen Signalnamen im System
	 * @param clientTypes	Liste mit allen Kundentypnamen im System
	 */
	public BarrierSignalTableModelDialog(final Component owner, final Runnable help, ModelElementBarrierSignalOption option, final String[] signals, final String[] clientTypes) {
		super(owner,Language.tr("Surface.Barrier.Dialog.Edit"));

		if (option==null) option=new ModelElementBarrierSignalOption();
		this.signals=signals;
		this.clientTypes=clientTypes;

		final JPanel content=createGUI(help);

		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		JLabel label;

		/* Dialogfelder aufbauen */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Barrier.Dialog.ReleaseSignal")+":"));
		if (signals.length==0) {
			line.add(signal=new JComboBox<>(new String[]{"<"+Language.tr("Surface.Barrier.Dialog.ReleaseSignal.NoSignalsAvailable")+">"}));
		} else {
			line.add(signal=new JComboBox<>(signals));
		}
		label.setLabelFor(signal);

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Barrier.Dialog.ClientsToBeReleaseBeforeBarrierActivates")+":","",5);
		content.add((JPanel)data[0]);
		initialClients=(JTextField)data[1];
		initialClients.setEditable(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionNumber=new JRadioButton(Language.tr("Surface.Barrier.Dialog.ClientsPerRelease")+":"));
		optionNumber.setEnabled(!readOnly);
		line.add(clientsPerSignal=new JTextField(5));
		ModelElementBaseDialog.addUndoFeature(clientsPerSignal);
		clientsPerSignal.setEditable(!readOnly);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionAll=new JRadioButton(Language.tr("Surface.Barrier.Dialog.ClientsPerRelease.All")));
		optionAll.setEnabled(!readOnly);

		final ButtonGroup group=new ButtonGroup();
		group.add(optionNumber);
		group.add(optionAll);

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(storeSignals=new JCheckBox(Language.tr("Surface.Barrier.Dialog.StoreSignals")));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Barrier.Dialog.ClientType")+":"));
		List<String> types=new ArrayList<>();
		types.add(Language.tr("Surface.Barrier.Dialog.ClientType.All"));
		types.addAll(Arrays.asList(clientTypes));
		line.add(clientType=new JComboBox<>(types.toArray(new String[0])));

		label.setLabelFor(signal);

		/* Felder mit Werten initialisieren */
		if (signals.length==0) {
			signal.setSelectedIndex(0);
		} else {
			int index=0;
			final String s=option.getSignalName();
			for (int i=0;i<signals.length;i++) if (s.equalsIgnoreCase(signals[i])) {index=i; break;}
			signal.setSelectedIndex(index);
		}

		initialClients.setText(""+option.getInitialClients());

		int count=option.getClientsPerSignal();
		if (count>=1) {
			optionNumber.setSelected(true);
			clientsPerSignal.setText(""+count);
		} else {
			optionAll.setSelected(true);
			clientsPerSignal.setText("1");
		}
		storeSignals.setSelected(option.isStoreSignals());
		final String type=option.getClientType();
		clientType.setSelectedIndex(0);
		if (type!=null) for (int i=1;i<types.size();i++) if (type.equals(types.get(i))) {
			clientType.setSelectedIndex(i); break;
		}

		/* Check aktivieren */
		initialClients.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		clientsPerSignal.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {optionNumber.setSelected(true); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {optionNumber.setSelected(true); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {optionNumber.setSelected(true); checkData(false);}
		});

		pack();
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (readOnly) return false;

		boolean ok=true;

		if (NumberTools.getNotNegativeInteger(initialClients,true)==null) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Barrier.Dialog.ClientsToBeReleaseBeforeBarrierActivates.Error.Title"),String.format(Language.tr("Surface.Barrier.Dialog.ClientsToBeReleaseBeforeBarrierActivates.Error.Info"),initialClients.getText()));
				return false;
			}
			ok=false;
		}

		if (optionNumber.isSelected()) {
			if (NumberTools.getPositiveLong(clientsPerSignal,true)==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("Surface.Barrier.Dialog.ClientsPerRelease.Error.Title"),String.format(Language.tr("Surface.Barrier.Dialog.ClientsPerRelease.Error.Info"),clientsPerSignal.getText()));
					return false;
				}
				ok=false;
			}
		}

		if (this.signals.length==0) {
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Barrier.Dialog.ReleaseSignal.Error.Title"),Language.tr("Surface.Barrier.Dialog.ReleaseSignal.Error.Info"));
			}
			ok=false;
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
	 * Liefert, wenn der Dialog per "Ok" geschlossen wurde, den bearbeiteten Freigabedatensatz
	 * @return	NeuerFreigabedatensatz
	 * @see ModelElementBarrierSignalOption
	 */
	public ModelElementBarrierSignalOption getOption() {
		final ModelElementBarrierSignalOption option=new ModelElementBarrierSignalOption();

		if (signals.length==0) {
			option.setSignalName("");
		} else {
			option.setSignalName(signals[signal.getSelectedIndex()]);
		}

		option.setInitialClients(NumberTools.getNotNegativeInteger(initialClients,true));

		if (optionNumber.isSelected()) {
			option.setClientsPerSignal(NumberTools.getNotNegativeInteger(clientsPerSignal,true));
		} else {
			option.setClientsPerSignal(-1);
		}

		option.setStoreSignals(storeSignals.isSelected());

		if (clientType.getSelectedIndex()==0) {
			option.setClientType(null);
		} else {
			option.setClientType(clientTypes[clientType.getSelectedIndex()-1]);
		}

		return option;
	}
}