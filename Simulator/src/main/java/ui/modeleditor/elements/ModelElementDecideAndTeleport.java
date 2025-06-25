/**
 * Copyright 2022 Alexander Herzog
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
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Verzweigt die eintreffenden Kunden in verschiedene Richtungen und teleportiert sie zu den Zielstationen
 * @author Alexander Herzog
 */
public class ModelElementDecideAndTeleport extends ModelElementBox implements ModelDataRenameListener, ModelElementEdgeMultiIn, ElementWithDecideData {
	/** Liste der einlaufenden Kanten */
	private final List<ModelElementEdge> connectionsIn;

	/**
	 * Liste der IDs der einlaufenden Kanten (wird nur beim Laden und Clonen verwendet, ist sonst <code>null</code>)
	 * @see #connectionsIn
	 */
	private List<Integer> connectionsInIds=null;

	/**
	 * Einstellungen zur Verzweigung
	 */
	private final DecideRecord decideRecord=new DecideRecord(true);

	/**
	 * Namen der Zielstationen
	 * @see #getDestinations()
	 */
	private final List<String> destinations;

	/**
	 * Konstruktor der Klasse <code>ModelElementDecide</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementDecideAndTeleport(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE_TWO_LINES_INSIDE);
		setSize(new Dimension(30,30));
		setDrawText(false);
		connectionsIn=new ArrayList<>();
		decideRecord.addChangeListener(()->fireChanged());
		destinations=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_TELEPORT_SOURCE.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.DecideAndTeleport.Tooltip");
	}

	/**
	 * Gibt die Namen der Zielstationen an.
	 * @return	Namen der Zielstationen
	 */
	public List<String> getDestinations() {
		return destinations;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDecideAndTeleport)) return false;

		final ModelElementDecideAndTeleport otherTeleportSource=(ModelElementDecideAndTeleport)element;

		final List<ModelElementEdge> connectionsIn2=otherTeleportSource.connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		if (!Objects.deepEquals(destinations,otherTeleportSource.destinations)) return false;

		if (!decideRecord.equalsDecideRecord(otherTeleportSource.decideRecord,destinations.size())) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDecideAndTeleport) {
			final ModelElementDecideAndTeleport source=(ModelElementDecideAndTeleport)element;

			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=source.connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			destinations.clear();
			destinations.addAll(source.destinations);

			decideRecord.copyDataFrom(source.decideRecord);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDecideAndTeleport clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDecideAndTeleport element=new ModelElementDecideAndTeleport(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		ModelElement element;

		if (connectionsInIds!=null) {
			for (int i=0;i<connectionsInIds.size();i++) {
				element=surface.getById(connectionsInIds.get(i));
				if (element instanceof ModelElementEdge) connectionsIn.add((ModelElementEdge)element);
			}
			connectionsInIds=null;
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.DecideAndTeleport.Name");
	}

	/**
	 * Liefert einen Fehlertext, der unter der Box angezeigt werden soll.<br>
	 * Ist das Element in Ordnung, so soll <code>null</code> zurückgegeben werden.
	 * @return	Optionale Fehlermeldung oder <code>null</code> wenn kein Fehler vorliegt.
	 */
	@Override
	protected String getErrorMessage() {
		if (destinations.size()==0) return Language.tr("Surface.ErrorInfo.NoTeleportDestination");

		return null;
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.DecideAndTeleport.Name");
	}

	/**
	 * Liefert optional eine zusätzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zusätzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null) return null; /* keinen Untertitel in der Templates-Liste anzeigen */
		return decideRecord.getMode().getName();
	}

	@Override
	public DecideRecord getDecideRecord() {
		return decideRecord;
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(204,99,255);

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
			new ModelElementDecideAndTeleportDialog(owner,ModelElementDecideAndTeleport.this,readOnly);
		};
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
		boolean needSeparator=false;

		needSeparator=needSeparator || addEdgesInContextMenu(popupMenu,surface,readOnly);

		if (needSeparator) popupMenu.addSeparator();
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionsIn!=null) {
			while (connectionsIn.size()>0) {
				ModelElement element=connectionsIn.remove(0);
				surface.remove(element);
			}
		}
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.DecideAndTeleport.XML.Root");
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

		decideRecord.saveToXml(doc,node);

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		for (int i=0;i<destinations.size();i++) {
			final String destination=destinations.get(i);
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TeleportSourceMulti.XML.Destination")));
			sub.setTextContent(destination);

			decideRecord.saveToXmlOutput(doc,sub,i,i==destinations.size()-1);
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

		error=decideRecord.loadFromXml(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.In",s)) {
				if (connectionsInIds==null) connectionsInIds=new ArrayList<>();
				connectionsInIds.add(I);
			}
		}

		if (Language.trAll("Surface.TeleportSourceMulti.XML.Destination",name)) {
			if (!content.isBlank()) destinations.add(content.trim());

			error=decideRecord.loadOptionFromXml(name,node);
			if (error!=null) return error;

			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * Fügt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die einlaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeIn(ModelElementEdge edge) {
		if (edge!=null && connectionsIn.indexOf(edge)<0) {
			connectionsIn.add(edge);
			fireChanged();
			return true;
		}
		return false;
	}

	/**
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		return connectionsIn.toArray(ModelElementEdge[]::new);
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation überhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung übersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation berücksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden müssten).
	 * @return	Gibt <code>true</code> zurück, wenn es mindestens eine in das Element einlaufende
	 * Kante gibt.
	 */
	@Override
	public boolean inputConnected() {
		return connectionsIn.size()>0;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_TELEPORT_DESTINATION)) {
			for (int i=0;i<destinations.size();i++) if (destinations.get(i).equals(oldName)) destinations.set(i,newName);
		}

		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) {
			decideRecord.renameClientType(oldName,newName);
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementDecideAndTeleport";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		switch (decideRecord.getMode()) {
		case MODE_CHANCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Rate"),1000);
			final List<String> rates=decideRecord.getRates();
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				final String destinationDescription=String.format(Language.tr("ModelDescription.Decide.Rate"),(i>=rates.size())?"1":rates.get(i));
				descriptionBuilder.addConditionalTeleportDestination(destinationDescription,destinationName);
			}
			break;
		case MODE_CONDITION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Condition"),1000);
			final List<String> conditions=decideRecord.getConditions();
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				final String destinationDescription;
				if (i<destinations.size()-1) {
					destinationDescription=String.format(Language.tr("ModelDescription.Decide.Condition"),(i>=conditions.size())?"":conditions.get(i));
				} else {
					destinationDescription=Language.tr("ModelDescription.Decide.Condition.Else");
				}
				descriptionBuilder.addConditionalTeleportDestination(destinationDescription,destinationName);
			}
			break;
		case MODE_CLIENTTYPE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ClientType"),1000);
			final List<List<String>> clientTypes=decideRecord.getClientTypes();
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				final String destinationDescription;
				if (i<destinations.size()-1) {
					final String s=(i<clientTypes.size() && clientTypes.get(i).size()>0)?String.join(", ",clientTypes.get(i)):"";
					destinationDescription=String.format(Language.tr("ModelDescription.Decide.ClientType"),s);
				} else {
					destinationDescription=Language.tr("ModelDescription.Decide.ClientType.Else");
				}
				descriptionBuilder.addConditionalTeleportDestination(destinationDescription,destinationName);
			}
			break;
		case MODE_SEQUENCE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.Sequence"),1000);
			final List<Integer> multiplicity=decideRecord.getMultiplicity();
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				final String info=(i>=multiplicity.size() || multiplicity.get(i).intValue()==1)?"":(" ("+multiplicity.get(i).intValue()+"x)");
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement")+info,destinationName);
			}
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ShortestQueueNextStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.ShortestQueueNextProcessStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LeastClientsNextStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LeastClientsNextProcessStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_LONGEST_QUEUE_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LongestQueueNextStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_LONGEST_QUEUE_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.LongestQueueNextProcessStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_MAX_CLIENTS_NEXT_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.MostClientsNextStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_MAX_CLIENTS_PROCESS_STATION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.MostClientsNextProcessStation"),1000);
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				descriptionBuilder.addConditionalTeleportDestination(Language.tr("ModelDescription.NextElement"),destinationName);
			}
			break;
		case MODE_KEY_VALUE:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Decide.Mode"),Language.tr("ModelDescription.Decide.Mode.StringProperty"),1000);
			final List<String> values=decideRecord.getValues();
			for (int i=0;i<destinations.size();i++) {
				final String destinationName=destinations.get(i);
				final String destinationDescription;
				if (i<destinations.size()-1) {
					final String s=(i<values.size())?values.get(i):"";
					destinationDescription=String.format(Language.tr("ModelDescription.Decide.StringProperty"),decideRecord.getKey(),s);
				} else {
					destinationDescription=Language.tr("ModelDescription.Decide.StringProperty.Else");
				}
				descriptionBuilder.addConditionalTeleportDestination(destinationDescription,destinationName);
			}
			break;
		}
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		this.connectionsIn.clear();
		this.connectionsIn.addAll(connectionsIn);

		return true;
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (int i=0;i<destinations.size();i++) {
			final int index=i;
			searcher.testString(this,Language.tr("Surface.TeleportSourceMulti.Dialog.Target"),destinations.get(index),newDestination->{destinations.set(index,newDestination);});
		}

		decideRecord.search(this,searcher);
	}
}
