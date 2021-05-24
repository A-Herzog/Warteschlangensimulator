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

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Zerteilen-Element
 * @author Alexander Herzog
 */
public class ModelElementSplit extends ModelElementMultiInSingleOutBox implements ElementWithNewClientNames, ModelDataRenameListener {
	/**
	 * Kundenank�nfte-Datens�tze
	 * @see #getRecords()
	 */
	private final List<ModelElementSourceRecord> records;

	/**
	 * Sollen die Kundendatenfelder auf die neuen Ankunftsdatens�tze �bertragen werden?
	 */
	private boolean copyClientData;

	/**
	 * Konstruktor der Klasse <code>ModelElementSet</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementSplit(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ARROW_RIGHT_DOUBLE);
		records=new ArrayList<>();
		copyClientData=false;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SPLIT.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Split.Tooltip");
	}

	/**
	 * Liefert die Daten f�r die Kundenank�nfte
	 * @return	Kundenank�nfte-Datens�tze
	 * @see #addRecord(ModelElementSourceRecord)
	 */
	public List<ModelElementSourceRecord> getRecords() {
		return records;
	}

	/**
	 * F�gt einen Datensatz zur Liste der Datens�tze hinzu
	 * (und stellt dabei auch den Change-Listener des Datensatzes korrekt ein).
	 * @param record	Hinzuzuf�gender Datensatz
	 * @see #getRecords()
	 */
	public void addRecord(final ModelElementSourceRecord record) {
		record.addChangeListener(()->fireChanged());
		records.add(record);
	}


	/**
	 * Sollen die Kundendatenfelder auf die neuen Ankunftsdatens�tze �bertragen werden?
	 * @return	Kundendatenfelder auf die neuen Ankunftsdatens�tze �bertragen
	 */
	public boolean isCopyClientData() {
		return copyClientData;
	}

	/**
	 * Sollen die Kundendatenfelder auf die neuen Ankunftsdatens�tze �bertragen werden?
	 * @param copyClientData	Kundendatenfelder auf die neuen Ankunftsdatens�tze �bertragen
	 */
	public void setCopyClientData(boolean copyClientData) {
		this.copyClientData=copyClientData;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSplit)) return false;
		final ModelElementSplit split=(ModelElementSplit)element;

		if (split.records.size()!=records.size()) return false;
		for (int i=0;i<records.size();i++) if (!split.records.get(i).equalsRecord(records.get(i))) return false;

		if (split.copyClientData!=copyClientData) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSplit) {
			final ModelElementSplit copySource=(ModelElementSplit)element;
			records.clear();
			for (int i=0;i<copySource.records.size();i++) {
				final ModelElementSourceRecord record=copySource.records.get(i).clone();
				record.addChangeListener(()->fireChanged());
				records.add(record);
			}
			copyClientData=copySource.copyClientData;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSplit clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSplit element=new ModelElementSplit(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Split.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Split.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(180,255,180);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe f�r die Box
	 * @return	Vorgabe-Hintergrundfarbe f�r die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zur�ck, welches aufgerufen werden kann, wenn die Eigenschaften des Elements ver�ndert werden sollen.
	 * @param owner	�bergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspl�ne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementSplitDialog(owner,ModelElementSplit.this,readOnly,clientData);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsBatch(this,parentMenu,addNextStation);
	}

	/**
	 * F�gt optional weitere Eintr�ge zum Kontextmen� hinzu
	 * @param owner	�bergeordnetes Element
	 * @param popupMenu	Kontextmen� zu dem die Eintr�ge hinzugef�gt werden sollen
	 * @param surfacePanel	Zeichenfl�che
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so k�nnen �ber das Kontextmen� keine �nderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Split.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);
		Element sub;

		for (ModelElementSourceRecord record: records) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Split.XML.Source")));
			record.saveToXML(doc,sub);
		}

		if (copyClientData) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Split.XML.CopyClientData")));
			sub.setTextContent("1");
		}
	}

	/**
	 * L�dt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.Split.XML.Source",name)) {
			ModelElementSourceRecord record=new ModelElementSourceRecord(true,true,false);
			error=record.loadFromXML(node);
			if (error!=null) return error;
			record.addChangeListener(()->fireChanged());
			records.add(record);
			return null;
		}

		if (Language.trAll("Surface.Split.XML.CopyClientData",name)) {
			final String copyClientDataString=node.getTextContent();
			if (!copyClientDataString.isEmpty() && !copyClientDataString.equals("0")) copyClientData=true;
			return null;
		}

		return null;
	}

	@Override
	public String[] getNewClientTypes() {
		final List<String> list=new ArrayList<>();
		for (ModelElementSourceRecord record: records) {
			if (!record.isActive()) continue;
			final String name=record.getName();
			if (list.indexOf(name)<0) list.add(name);
		}
		return list.toArray(new String[0]);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSplit";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		for (ModelElementSourceRecord record: records) {
			record.buildDescriptionProperty(descriptionBuilder);
		}

		descriptionBuilder.addProperty(Language.tr("ModelDescription.Split.CopyClientData"),copyClientData?Language.tr("ModelDescription.Split.CopyClientData.Yes"):Language.tr("ModelDescription.Split.CopyClientData.No"),10000);
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_SIGNAL)) {
			for (ModelElementSourceRecord record: records) record.signalRenamed(oldName,newName);
		}
	}
}
