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
import java.io.Serializable;

import javax.swing.JComponent;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTankFlowByClient}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTankFlowByClient
 */
public class ModelElementTankFlowByClientDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6233496637014286459L;

	private ModelElementTankFlowDataPanel data;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTankFlowByClient}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTankFlowByClientDialog(final Component owner, final ModelElementTankFlowByClient element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TankFlowByClient.Dialog.Title"),element,"ModelElementTankFlowByClient",readOnly);
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
		return InfoPanel.stationTankFlowByClient;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelSurface surface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		data=new ModelElementTankFlowDataPanel(((ModelElementTankFlowByClient)element).getFlowData(),surface,readOnly,()->pack());
		return data;
	}

	@Override
	protected boolean checkData() {
		return data.checkData(true);
	}

	@Override
	public void storeData() {
		super.storeData();
		data.storeData();
	}
}
