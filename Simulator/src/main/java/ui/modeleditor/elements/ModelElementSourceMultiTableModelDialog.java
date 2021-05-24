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
import java.awt.Component;
import java.io.Serializable;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dieser Dialog ermöglicht das Bearbeiten eines einzelnen Ankunftsdatensatzes
 * einer {@link ModelElementSourceMultiTableModel}-Tabelle in einem
 * {@link ModelElementSourceMultiDialog}- oder einem {@link ModelElementSplitDialog}-Element.
 * @author Alexander Herzog
 * @see ModelElementSourceMultiTableModel
 */
public class ModelElementSourceMultiTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5750673145282095397L;

	/**
	 * Panel zum Bearbeiten der Kunden-Quelle-Einstellungen
	 */
	private final ModelElementSourceRecordPanel recordPanel;

	/**
	 * Datenelement, welches die modellweite Kundentypenliste vorhält (falls sich durch die Veränderung des Datensatzes Kundentypnamen ändern)
	 */
	private final ModelClientData clientData;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param record	Ankunftsdatensatz
	 * @param element	Element, dessen Zuweisungen bearbeitet werden sollen (für den ExpressionBuilder und um die Variablenliste zusammenzustellen)
	 * @param model	Gesamtes Editor-Modell (für den ExpressionBuilder)
	 * @param surface	Haupt-Zeichenfläche (für den ExpressionBuilder)
	 * @param clientData	Datenelement, welches die modellweite Kundentypenliste vorhält (falls sich durch die Veränderung des Datensatzes Kundentypnamen ändern)
	 * @param helpRunnable	Hilfe-Callback
	 * @param getSchedulesButton	Callback zum Erstellen der Schaltfläche zum Aufrufen der Zeitpläne
	 * @param hasActivation	Kann der Datensatz deaktiviert werden?
	 */
	public ModelElementSourceMultiTableModelDialog(final Component owner, final ModelElementSourceRecord record, final ModelElement element, final EditModel model, final ModelSurface surface, final ModelClientData clientData, final Runnable helpRunnable, final Function<Supplier<Boolean>,JButton> getSchedulesButton, final boolean hasActivation) {
		super(owner,Language.tr("Surface.MultiSourceTable.Dialog"));
		this.clientData=clientData;

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		content.add(recordPanel=new ModelElementSourceRecordPanel(false,model,surface,()->getSchedulesButton.apply(()->close(BaseDialog.CLOSED_BY_OK)),helpRunnable,record.hasOwnArrivals(),hasActivation),BorderLayout.CENTER);
		recordPanel.setData(record,element);

		setMinSizeRespectingScreensize(700,625);
		pack();
		setLocationRelativeTo(this.owner);
		setResizable(true);
	}

	@Override
	protected boolean checkData() {
		return recordPanel.checkData(true);
	}

	@Override
	protected void storeData() {
		recordPanel.getData(false,clientData);
	}
}