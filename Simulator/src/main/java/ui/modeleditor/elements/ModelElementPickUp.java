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
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Passiert ein Kunde dieses Element, so wird aus der Warteschlange eines anderen Elements
 * ebenfalls ein Kunde entnommen und gemeinsam mit dem aktuellen Kunden auf dem neuen Weg
 * weitergeleitet oder wird mit dem aktuellen Kunden zu einen temporären oder dauerhaften
 * Batch zusammengefasst.
 * @author Alexander Herzog
 */
public class ModelElementPickUp extends ModelElementMultiInSingleOutBox implements ElementWithNewClientNames, ModelElementAnimationForceMove {
	/**
	 * Art der Batch-Bildung
	 * @author Alexander Herzog
	 * @see ModelElementPickUp#getBatchMode()
	 * @see ModelElementPickUp#setBatchMode(BatchMode)
	 */
	public enum BatchMode {
		/** Sammelt die Kunden und leitet sie dann gemeinsam weiter (kein Batching im engeren Sinne). */
		BATCH_MODE_COLLECT,

		/** Fasst die Kunden zu einem temporären Batch zusammen. */
		BATCH_MODE_TEMPORARY,

		/** Fasst die Kunden zu einem neuen Kunden zusammen (permanentes Batching). */
		BATCH_MODE_PERMANENT
	}

	/**
	 * ID der Warteschlange, aus der der jeweils andere Kunde entnommen werden soll
	 * @see #getQueueID()
	 * @see #setQueueID(int)
	 */
	private int queueID;

	/**
	 * Batch-Modus
	 * @see #getBatchMode()
	 * @see #setBatchMode(BatchMode)
	 * @see BatchMode
	 */
	private BatchMode batchMode;

	/**
	 * Neuer Kundentyp (wenn die Kunden nicht einzeln weitergeleitet werden)
	 * @see #getNewClientType()
	 * @see #setNewClientType(String)
	 */
	private String newClientType;

	/**
	 * Soll der aktuelle Kunde alleine weiter geleitet werden, wenn in der anderen Warteschlange keine Kunden warten?
	 * @see #isSendAloneIfQueueEmpty()
	 * @see #setSendAloneIfQueueEmpty(boolean)
	 */
	private boolean sendAloneIfQueueEmpty;

	/**
	 * Konstruktor der Klasse <code>ModelElementPickUp</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementPickUp(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		queueID=-1;
		batchMode=BatchMode.BATCH_MODE_COLLECT;
		newClientType="";
		sendAloneIfQueueEmpty=false;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_PICK_UP.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.PickUp.Tooltip");
	}

	@Override
	protected void updateEdgeLabel() {
		final String s=newClientType;
		if (connectionOut!=null) connectionOut.setName(s.isEmpty()?"":(Language.tr("Surface.PickUp.NewClientType")+" \""+s+"\""));
	}

	/**
	 * Liefert die ID der Warteschlange, aus der der jeweils andere Kunde entnommen werden soll.
	 * @return	ID der Warteschlange
	 */
	public int getQueueID() {
		return queueID;
	}

	/**
	 * Stellt die ID der Warteschlange, aus der der jeweils andere Kunde entnommen werden soll, ein.
	 * @param queueID ID der Warteschlange
	 */
	public void setQueueID(final int queueID) {
		this.queueID=queueID;
	}

	/**
	 * Liefert den aktuell gewählten Batch-Modus.
	 * @return	Aktueller Batch-Modus
	 * @see BatchMode
	 */
	public BatchMode getBatchMode() {
		return batchMode;
	}

	/**
	 * Stellt den Batch-Modus ein.
	 * @param batchMode	Neuer Batch-Modus
	 * @see BatchMode
	 */
	public void setBatchMode(BatchMode batchMode) {
		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			this.batchMode=BatchMode.BATCH_MODE_COLLECT;
			newClientType="";
			break;
		case BATCH_MODE_TEMPORARY:
			this.batchMode=BatchMode.BATCH_MODE_TEMPORARY;
			break;
		case BATCH_MODE_PERMANENT:
			this.batchMode=BatchMode.BATCH_MODE_PERMANENT;
			break;
		}
	}

	/**
	 * Liefert den Kundentyp, unter dem die zusammengefassten Kunden weitergeleitet werden sollen.
	 * @return	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public String getNewClientType() {
		return newClientType;
	}

	/**
	 * Stellt den Kundentyp ein, unter dem die zusammengefassten Kunden weitergeleitet werden sollen.
	 * @param newClientType	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public void setNewClientType(final String newClientType) {
		if (newClientType==null || newClientType.trim().isEmpty()) {
			this.newClientType="";
			batchMode=BatchMode.BATCH_MODE_COLLECT;
		} else {
			this.newClientType=newClientType;
			if (batchMode==BatchMode.BATCH_MODE_COLLECT) batchMode=BatchMode.BATCH_MODE_PERMANENT;
		}
	}

	/**
	 * Soll der aktuelle Kunde alleine weiter geleitet werden, wenn in der anderen Warteschlange keine Kunden warten?
	 * @return	Kunden notfalls alleine weiterleiten
	 */
	public boolean isSendAloneIfQueueEmpty() {
		return sendAloneIfQueueEmpty;
	}

	/**
	 * Stellt ein, ob der aktuelle Kunde notfalls alleine weiter geleitet werden soll, wenn in der anderen Warteschlange keine Kunden warten.
	 * @param sendAloneIfQueueEmpty	Kunden notfalls alleine weiterleiten
	 */
	public void setSendAloneIfQueueEmpty(final boolean sendAloneIfQueueEmpty) {
		this.sendAloneIfQueueEmpty=sendAloneIfQueueEmpty;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementPickUp)) return false;

		if (((ModelElementPickUp)element).queueID!=queueID) return false;
		if (batchMode!=((ModelElementPickUp)element).batchMode) return false;
		if (batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) {
			if (!((ModelElementPickUp)element).newClientType.equals(newClientType)) return false;
		}
		if (((ModelElementPickUp)element).sendAloneIfQueueEmpty!=sendAloneIfQueueEmpty) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementPickUp) {
			queueID=((ModelElementPickUp)element).queueID;
			batchMode=((ModelElementPickUp)element).batchMode;
			if (batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) {
				newClientType=((ModelElementPickUp)element).newClientType;
			} else {
				newClientType="";
			}
			sendAloneIfQueueEmpty=((ModelElementPickUp)element).sendAloneIfQueueEmpty;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementPickUp clone(final EditModel model, final ModelSurface surface) {
		final ModelElementPickUp element=new ModelElementPickUp(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.PickUp.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.PickUp.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(180,225,255);

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
			new ModelElementPickUpDialog(owner,ModelElementPickUp.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Visualisierungen hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen zu dem aktuellen Element direkt passende Animationselemente hinzuzufügen.
	 * @param parentMenu	Untermenü des Popupmenüs, welches die Einträge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition> addElement) {
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.CHART_WIP);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Laufzeitstatistik hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugefügt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.WIP);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_IN);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_OUT);
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsBatch(this,parentMenu,addNextStation);
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.PickUp.XML.Root");
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

		if (queueID>=0 || sendAloneIfQueueEmpty) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.Queue")));
			sub.setTextContent(""+queueID);
			if (sendAloneIfQueueEmpty) sub.setAttribute(Language.trPrimary("Surface.PickUp.XML.Queue.IgnoreIfEmpty"),"1");
		}

		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.BatchMode")));
			sub.setTextContent(Language.trPrimary("Surface.PickUp.XML.BatchMode.Collect"));
			break;
		case BATCH_MODE_TEMPORARY:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.PickUp.XML.BatchMode.Temporary"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.PickUp.XML.BatchMode.Collect"));
			}
			break;
		case BATCH_MODE_PERMANENT:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.PickUp.XML.BatchMode.Permanent"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.PickUp.XML.BatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.PickUp.XML.BatchMode.Collect"));
			}
			break;
		}
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

		if (Language.trAll("Surface.PickUp.XML.Queue",name)) {
			Integer I=NumberTools.getInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			queueID=I;
			final String s=Language.trAllAttribute("Surface.PickUp.XML.Queue.IgnoreIfEmpty",node);
			sendAloneIfQueueEmpty=s.equals("1");
			return null;
		}

		if (Language.trAll("Surface.PickUp.XML.BatchMode",name)) {
			if (Language.trAll("Surface.PickUp.XML.BatchMode.Collect",content)) batchMode=BatchMode.BATCH_MODE_COLLECT;
			if (Language.trAll("Surface.PickUp.XML.BatchMode.Temporary",content)) batchMode=BatchMode.BATCH_MODE_TEMPORARY;
			if (Language.trAll("Surface.PickUp.XML.BatchMode.Permanent",content)) batchMode=BatchMode.BATCH_MODE_PERMANENT;
			return null;
		}

		if (Language.trAll("Surface.PickUp.XML.ClientType",name)) {
			newClientType=content;
			if (!content.trim().isEmpty() && batchMode==BatchMode.BATCH_MODE_COLLECT) batchMode=BatchMode.BATCH_MODE_PERMANENT;
			return null;
		}

		return null;
	}

	@Override
	public String[] getNewClientTypes() {
		return new String[]{getNewClientType()};
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zusätzlich auch die aktuelle Anzahl an Kunden an der Station
	 * während der Animation angezeigt werden sollen.
	 * @return	Nur Gesamtanzahl (<code>false</code>) oder Gesamtanzahl und aktueller Wert (<code>true</code>)
	 */
	@Override
	public boolean showFullAnimationRunData() {
		return true;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementPickUp";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Batching Modus */
		switch (batchMode) {
		case BATCH_MODE_COLLECT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.PickUp.Mode"),Language.tr("ModelDescription.PickUp.Mode.Collect"),1000);
			break;
		case BATCH_MODE_TEMPORARY:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.PickUp.Mode"),Language.tr("ModelDescription.PickUp.Mode.Temporary"),1000);
			break;
		case BATCH_MODE_PERMANENT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.PickUp.Mode"),Language.tr("ModelDescription.PickUp.Mode.Permanent"),1000);
			break;
		}

		/* Andere Warteschlange zum Ausleiten von Kunden */
		final String queueName=ModelDescriptionBuilder.getStationName(descriptionBuilder.getModel().surface.getByIdIncludingSubModels(queueID));
		if (queueName==null) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.PickUp.Queue"),"id="+queueID,2000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.PickUp.Queue"),queueName,2000);
		}

		/* Kunde ggf. alleine weitersenden ? */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.PickUp.SendAlone"),sendAloneIfQueueEmpty?Language.tr("Dialog.Button.Yes"):Language.tr("Dialog.Button.No"),2500);

		/* Neuer Kundentyp */
		if ((batchMode==BatchMode.BATCH_MODE_TEMPORARY || batchMode==BatchMode.BATCH_MODE_PERMANENT) && !newClientType.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.PickUp.NewClientType"),newClientType,3000);
		}
	}
}