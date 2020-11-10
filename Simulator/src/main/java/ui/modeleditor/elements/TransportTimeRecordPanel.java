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
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelSurface;

/**
 * Diese Klasse kapselt die Dialogdaten für ein
 * {@link TransportTimeRecord}-Element.
 * @author Alexander Herzog
 * @see TransportTimeRecord
 * @see ModelElementTransportSourceDialog
 */
public class TransportTimeRecordPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4541268310614766813L;

	/**
	 * Daten, die in dem Panel bearbeitet werden sollen
	 */
	private final TransportTimeRecord data;

	/** Auswahlbox für die Zeiteinheit in {@link #distributionEditor} */
	private JComboBox<String> timeBase;
	/** Auswahlbox dafür, als was die Zeit in der Statistik erfasst werden soll */
	private JComboBox<String> transportTimeType;
	/** Konfiguration der Transportzeit (Verteilung oder Rechenausdruck) */
	private DistributionBySubTypeEditor distributionEditor;

	/**
	 * Konstruktor der Klasse
	 * @param data	Daten, die in dem Panel bearbeitet werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param model	Gesamtes Modell (zum Auslesen von Daten für den Expression-Builder)
	 * @param surface	Zeichenoberfläche (zum Auslesen von Daten für den Expression-Builder)
	 */
	public TransportTimeRecordPanel(final TransportTimeRecord data, final boolean readOnly, final EditModel model, final ModelSurface surface) {
		super();
		this.data=data;
		setLayout(new BorderLayout());

		JPanel line;
		JLabel label;

		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(label=new JLabel(Language.tr("Surface.TransportSource.Dialog.TimeBase")+":"));
		line.add(timeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase.setEnabled(!readOnly);
		timeBase.setSelectedIndex(data.getTimeBase().id);
		label.setLabelFor(timeBase);
		line.add(label=new JLabel(Language.tr("Surface.TransportSource.Dialog.DelayTimeIs")));
		line.add(transportTimeType=new JComboBox<>(new String[]{
				Language.tr("Surface.TransportSource.Dialog.DelayTimeIs.WaitingTime"),
				Language.tr("Surface.TransportSource.Dialog.DelayTimeIs.TransferTime"),
				Language.tr("Surface.TransportSource.Dialog.DelayTimeIs.ProcessTime"),
				Language.tr("Surface.TransportSource.Dialog.DelayTimeIs.Nothing")
		}));
		transportTimeType.setEnabled(!readOnly);
		switch (data.getDelayType()) {
		case DELAY_TYPE_WAITING: transportTimeType.setSelectedIndex(0); break;
		case DELAY_TYPE_TRANSFER: transportTimeType.setSelectedIndex(1); break;
		case DELAY_TYPE_PROCESS: transportTimeType.setSelectedIndex(2); break;
		case DELAY_TYPE_NOTHING: transportTimeType.setSelectedIndex(3); break;
		}

		label.setLabelFor(transportTimeType);

		add(distributionEditor=new DistributionBySubTypeEditor(model,surface,readOnly,Language.tr("Surface.TransportSource.Dialog.TransportTime"),data.getTransportTime(),DistributionBySubTypeEditor.Mode.MODE_TRANSPORT_DESTINATION),BorderLayout.CENTER);
	}

	/**
	 * Prüft, ob die Einstellungen in Ordnung sind und gibt im Fehlerfall eine Fehlermeldung aus.
	 * @return	Gibt <code>true</code> zurück, wenn die Einstellungen in Ordnung sind.
	 */
	public boolean checkData() {
		return true; /* Es gibt nichts zu prüfen. */
	}

	/**
	 * Schreibt die möglicherweise veränderten Daten in das
	 * im Konstruktor übergebene {@link TransportTimeRecord}-Objekt zurück.
	 */
	public void storeData() {
		data.setTimeBase(ModelSurface.TimeBase.byId(timeBase.getSelectedIndex()));
		switch (transportTimeType.getSelectedIndex()) {
		case 0: data.setDelayType(TransportTimeRecord.DelayType.DELAY_TYPE_WAITING); break;
		case 1: data.setDelayType(TransportTimeRecord.DelayType.DELAY_TYPE_TRANSFER); break;
		case 2: data.setDelayType(TransportTimeRecord.DelayType.DELAY_TYPE_PROCESS); break;
		case 3: data.setDelayType(TransportTimeRecord.DelayType.DELAY_TYPE_NOTHING); break;
		}
		distributionEditor.storeData();
	}
}
