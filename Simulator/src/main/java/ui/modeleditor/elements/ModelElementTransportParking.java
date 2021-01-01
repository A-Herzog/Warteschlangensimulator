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
import java.util.List;

import javax.swing.Icon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementTransportParking;
import simulator.elements.RunElementTransportParkingData;
import simulator.runmodel.RunDataTransporters;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Parkplatz für Transporter
 * @author Alexander Herzog
 */
public class ModelElementTransportParking extends ModelElementBox implements ModelDataRenameListener {
	/**
	 * Transportertyp, der hier parken kann
	 * @see #getTransporterType()
	 * @see #setTransporterType(String)
	 */
	private String transporterType;

	/**
	 * Anzahl an Transportern, die hier parken können
	 * @see #getWaitingCapacity()
	 * @see #setWaitingCapacity(int)
	 */
	private int waitingCapacity;

	/**
	 * Priorität mit der verfügbare Transporter angezogen werden
	 * @see #getWaitingPriority()
	 * @see #setWaitingPriority(String)
	 */
	private String waitingPriority;

	/**
	 * Konstruktor der Klasse <code>ModelElementTransportParking</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementTransportParking(final EditModel model, ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		transporterType="";
		waitingCapacity=1;
		waitingPriority="1";
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_TRANSPORT_PARKING.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.TransportParking.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateIcon();
		super.fireChanged();
	}

	/**
	 * Aktualisiert die Darstellung des zusätzlichen Icons auf der Station.
	 * @see #fireChanged()
	 */
	private void updateIcon() {
		setAdditionalTransporterIconFromName(getTransporterType());
	}

	/**
	 * Liefert den Transportertyp, der hier parken kann
	 * @return	Transportertyp, der hier parken kann
	 */
	public String getTransporterType() {
		return transporterType;
	}

	/**
	 * Stellt den Transportertyp, der hier parken kann, ein.
	 * @param transporterType	Transportertyp, der hier parken kann
	 */
	public void setTransporterType(final String transporterType) {
		if (transporterType!=null) this.transporterType=transporterType;
	}

	/**
	 * Liefert die Anzahl an Transportern, die hier parken können
	 * @return	Anzahl an Transportern, die hier parken können
	 */
	public int getWaitingCapacity() {
		return waitingCapacity;
	}

	/**
	 * Stellt die Anzahl an Transportern, die hier parken können, ein.
	 * @param waitingCapacity	Anzahl an Transportern, die hier parken können
	 */
	public void setWaitingCapacity(final int waitingCapacity) {
		if (waitingCapacity>0) this.waitingCapacity=waitingCapacity;
	}

	/**
	 * Liefert die Priorität mit der verfügbare Transporter angezogen werden.
	 * @return	Priorität mit der verfügbare Transporter angezogen werden
	 */
	public String getWaitingPriority() {
		return waitingPriority;
	}

	/**
	 * Stellt die Priorität mit der verfügbare Transporter angezogen werden ein.
	 * @param waitingPriority	Priorität mit der verfügbare Transporter angezogen werden
	 */
	public void setWaitingPriority(final String waitingPriority) {
		if (waitingPriority!=null) this.waitingPriority=waitingPriority;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementTransportParking)) return false;

		if (!transporterType.equals(((ModelElementTransportParking)element).transporterType)) return false;
		if (waitingCapacity!=((ModelElementTransportParking)element).waitingCapacity) return false;
		if (!waitingPriority.equals(((ModelElementTransportParking)element).waitingPriority)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementTransportParking) {
			transporterType=((ModelElementTransportParking)element).transporterType;
			waitingCapacity=((ModelElementTransportParking)element).waitingCapacity;
			waitingPriority=((ModelElementTransportParking)element).waitingPriority;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementTransportParking clone(final EditModel model, final ModelSurface surface) {
		final ModelElementTransportParking element=new ModelElementTransportParking(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.TransportParking.Name");
	}

	/**
	 * Liefert einen Fehlertext, der unter der Box angezeigt werden soll.<br>
	 * Ist das Element in Ordnung, so soll <code>null</code> zurückgegeben werden.
	 * @return	Optionale Fehlermeldung oder <code>null</code> wenn kein Fehler vorliegt.
	 */
	@Override
	protected String getErrorMessage() {
		if (transporterType.isEmpty()) return Language.tr("Surface.ErrorInfo.NoTransporterTypeSelected");
		for (String name: getModel().transporters.getNames()) if (name.equalsIgnoreCase(transporterType)) return null;
		return Language.tr("Surface.ErrorInfo.UnknownTransporterTypeSelected");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.TransportParking.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,212,212);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementTransportParkingDialog(owner,ModelElementTransportParking.this,readOnly);
		};
	}

	/**
	 * Fügt stations-bedingte zusätzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		final RunElementTransportParkingData data=(RunElementTransportParkingData)builder.data;
		final RunElementTransportParking station=(RunElementTransportParking)builder.data.station;
		final RunDataTransporters transporters=builder.simData.runModel.transportersTemplate;
		final int index=station.getTransporterIndex();

		builder.results.append("\n"+Language.tr("Statistics.Transporter")+"\n");
		builder.results.append(Language.tr("Statistics.Transporter.InfoType")+": \""+transporters.type[index]+"\"\n");
		builder.results.append(Language.tr("Statistics.Transporter.InfoCount")+": "+NumberTools.formatLong(transporters.getTransporterCount(index))+"\n");
		builder.results.append(Language.tr("Statistics.Transporter.InfoCapacity")+": "+NumberTools.formatLong(transporters.getTransporterCapacity(index))+"\n");
		builder.results.append(Language.tr("Statistics.Transporter.InfoWaiting")+": "+NumberTools.formatLong(data.count)+"\n");
		builder.results.append(Language.tr("Statistics.Transporter.InfoMoving")+": "+NumberTools.formatLong(data.moving)+"\n");
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.TransportParking.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportParking.TransporterTyp")));
		sub.setTextContent(transporterType);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportParking.WaitingCapacity")));
		sub.setTextContent(""+waitingCapacity);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportParking.WaitingPriority")));
		sub.setTextContent(waitingPriority);
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.XML.TransportParking.TransporterTyp",name)) {
			transporterType=content;
			return null;
		}

		if (Language.trAll("Surface.XML.TransportParking.WaitingCapacity",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			waitingCapacity=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.XML.TransportParking.WaitingPriority",name)) {
			waitingPriority=content;
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station während der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return false;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_TRANSPORTER)) {
			if (transporterType.equalsIgnoreCase(oldName)) transporterType=newName;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementTransportParking";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Transportertyp */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportParking.TransporterType"),transporterType,1000);

		/* Parkplatzkapazität */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportParking.Capacity"),""+waitingCapacity,2000);

		/* Priorität zum Anfordern von Transportern */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportParking.WaitingPriority"),waitingPriority,3000);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsIn.size()>0) return false;
		if (connectionsOut.size()>0) return false;

		return false;
	}
}
