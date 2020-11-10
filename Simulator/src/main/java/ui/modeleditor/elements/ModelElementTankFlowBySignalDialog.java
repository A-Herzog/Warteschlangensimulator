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
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTankFlowBySignal}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTankFlowBySignal
 */
public class ModelElementTankFlowBySignalDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2994666788602531951L;

	/**
	 * Auswahlbox zur Auswahl des Signals das den Fluss auslösen soll
	 */
	private JComboBox<String> signalCombo;

	/**
	 * Panel zur Konfiguration eines Flusses
	 */
	private ModelElementTankFlowDataPanel data;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTankFlowBySignal}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTankFlowBySignalDialog(final Component owner, final ModelElementTankFlowBySignal element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TankFlowBySignal.Dialog.Title"),element,"ModelElementTankFlowBySignal",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationTankFlowBySignal;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final List<String> signalNames=element.getSurface().getAllSignalNames();
		int index=signalNames.indexOf(((ModelElementTankFlowBySignal)element).getSignalName());
		if (index<0 && signalNames.size()>0) index=0;

		final JPanel line;
		final JLabel label;
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TankFlowBySignal.Dialog.SignalName")+":"));
		line.add(signalCombo=new JComboBox<>(signalNames.toArray(new String[0])));
		label.setLabelFor(signalCombo);
		if (index>=0) signalCombo.setSelectedIndex(index);

		final ModelSurface surface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		content.add(data=new ModelElementTankFlowDataPanel(((ModelElementTankFlowBySignal)element).getFlowData(),surface,readOnly,()->pack()));

		return content;
	}

	@Override
	protected boolean checkData() {
		if (signalCombo.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("Surface.TankFlowBySignal.Dialog.SignalName.ErrorTitle"),Language.tr("Surface.TankFlowBySignal.Dialog.SignalName.ErrorInfo"));
			return false;
		}
		return data.checkData(true);
	}

	@Override
	public void storeData() {
		super.storeData();
		if (signalCombo.getSelectedIndex()>=0) ((ModelElementTankFlowBySignal)element).setSignalName((String)signalCombo.getSelectedItem());
		data.storeData();
	}
}