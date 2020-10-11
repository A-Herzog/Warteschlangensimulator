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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementDispose}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementDispose
 */
public class ModelElementDisposeDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -86922871601132368L;

	private JCheckBox stoppSimulationOnClientArrival;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementDispose}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementDisposeDialog(final Component owner, final ModelElementDispose element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Dispose.Dialog.Title"),element,"ModelElementDispose",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(550,0);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationDispose;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);
		line.add(stoppSimulationOnClientArrival=new JCheckBox(Language.tr("Surface.Dispose.Dialog.StoppSimulationOnClientArrival")));
		if (element instanceof ModelElementDispose) {
			stoppSimulationOnClientArrival.setSelected(((ModelElementDispose)element).isStoppSimulationOnClientArrival());
		}
		return content;
	}

	@Override
	protected void storeData() {
		super.storeData();
		if (element instanceof ModelElementDispose) {
			((ModelElementDispose)element).setStoppSimulationOnClientArrival(stoppSimulationOnClientArrival.isSelected());
		}
	}
}
