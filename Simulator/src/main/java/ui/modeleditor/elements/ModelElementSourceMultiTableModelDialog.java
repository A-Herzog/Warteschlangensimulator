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
	 * @param showOkPrevious	Soll die Schaltfläche "Ok &amp; Vorheriger Kundentyp" angezeigt werden?
	 * @param showOkNext	Soll die Schaltfläche "Ok &amp; Nächster Kundentyp" angezeigt werden?
	 * @param setInitialTabIndex	0-basierter Index des initial anzuzeigenden Tabs (Werte &lt;0 bedeuten, dass keine explizite Vorauswahl erfolgen soll)
	 */
	public ModelElementSourceMultiTableModelDialog(final Component owner, final ModelElementSourceRecord record, final ModelElement element, final EditModel model, final ModelSurface surface, final ModelClientData clientData, final Runnable helpRunnable, final Function<Supplier<Boolean>,JButton> getSchedulesButton, final boolean hasActivation, final boolean showOkPrevious, final boolean showOkNext, final int setInitialTabIndex) {
		super(owner,Language.tr("Surface.MultiSourceTable.Dialog"));
		this.clientData=clientData;

		final JPanel content=createGUI(1024,768,showOkPrevious?Language.tr("Surface.MultiSourceTable.Dialog.Previous"):null,showOkNext?Language.tr("Surface.MultiSourceTable.Dialog.Next"):null,helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		content.add(recordPanel=new ModelElementSourceRecordPanel(false,model,surface,()->getSchedulesButton.apply(()->close(BaseDialog.CLOSED_BY_OK)),helpRunnable,record.hasOwnArrivals(),hasActivation),BorderLayout.CENTER);
		recordPanel.setData(record,element);
		if (setInitialTabIndex>=0) recordPanel.setActiveTabIndex(setInitialTabIndex);

		setMinSizeRespectingScreensize(700,625);
		pack();
		setMaxSizeRespectingScreensize(1024,768);
		setLocationRelativeTo(this.owner);
		setResizable(true);
	}

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
		this(owner,record,element,model,surface,clientData,helpRunnable,getSchedulesButton,hasActivation,false,false,-1);
	}

	@Override
	protected boolean checkData() {
		return recordPanel.checkData(true);
	}

	@Override
	protected void storeData() {
		recordPanel.getData(false,clientData);
	}

	/**
	 * Liefert den 0-basierten Index des in dem Dialog aktuell aktiven Tabs.
	 * @return	0-basierter Index des in dem Dialog aktuell aktiven Tabs
	 */
	public int getActiveTabIndex() {
		return recordPanel.getActiveTabIndex();
	}
}