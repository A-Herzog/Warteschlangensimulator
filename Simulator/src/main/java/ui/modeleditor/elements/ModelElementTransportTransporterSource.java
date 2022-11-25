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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.elements.RunElementTransportTransporterSource;
import simulator.elements.RunElementTransportTransporterSourceData;
import simulator.runmodel.RunDataTransporters;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Startpunkt für Transporter-basiertes Routing
 * @author Alexander Herzog
 */
public class ModelElementTransportTransporterSource extends ModelElementBox implements ModelDataRenameListener, ModelElementEdgeMultiIn, ModelElementAnimationForceMove {
	/**
	 * Standardmäßige Priorität für Kundentypen
	 */
	public static final String DEFAULT_CLIENT_PRIORITY="w";

	/**
	 * Einlaufende Kanten
	 */
	private List<ModelElementEdge> connections;

	/**
	 * IDs der einlaufenden Kanten (wird nur beim Laden und Clonen verwendet)
	 */
	private List<Integer> connectionIds=null;

	/**
	 * Einstellungen-Objekt mit den Daten zu den Transportzielen
	 * @see #getTransportTargetSystem()
	 */
	private TransportTargetSystem transportTargetSystem;

	/**
	 * Transportertyp, der hier parken kann
	 * @see #getTransporterType()
	 * @see #setTransporterType(String)
	 */
	private String transporterType;

	/**
	 * Anzahl an wartenden Kunden ab denen ein Transporter angefordert wird
	 * @see #getRequestMinWaiting()
	 * @see #setRequestMinWaiting(int)
	 */
	private int requestMinWaiting;

	/**
	 * Priorität mit der verfügbare Transporter im Bedarfsfall angezogen werden
	 * @see #getRequestPriority()
	 * @see #setRequestPriority(String)
	 */
	private String requestPriority;

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
	 * Daten der Kundentypenprioritäten bei der Zuweisung zu Transportern
	 * @see #getClientPriority(String)
	 * @see #getClientPriorities()
	 */
	private Map<String,String> clientPriority;

	/**
	 * Namen der zugehörigen "Bereich betreten"-Station
	 * @see #getSectionStartName()
	 * @see #setSectionStartName(String)
	 */
	private String sectionStartName;

	/**
	 * Konstruktor der Klasse <code>ModelElementTransportTransporterSource</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementTransportTransporterSource(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		connections=new ArrayList<>();
		transportTargetSystem=new TransportTargetSystem();
		transporterType="";
		requestMinWaiting=1;
		requestPriority="3";
		waitingCapacity=1;
		waitingPriority="2";
		clientPriority=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		sectionStartName="";
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_TRANSPORT_TRANSPORTER_SOURCE.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.TransportTransporterSource.Tooltip");
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
	 * Liefert das Einstellungen-Objekt mit den Daten zu den Transportzielen
	 * @return	Einstellungen-Objekt mit den Daten zu den Transportzielen
	 */
	public TransportTargetSystem getTransportTargetSystem() {
		return transportTargetSystem;
	}

	/**
	 * Gibt an, ab wie viel wartenden Kunden ein Transporter angefordert wird.
	 * @return	Anzahl an wartenden Kunden ab denen ein Transporter angefordert wird
	 */
	public int getRequestMinWaiting() {
		return requestMinWaiting;
	}

	/**
	 * Stellt ein ab wie viel wartenden Kunden ein Transporter angefordert wird.
	 * @param requestMinWaiting	Anzahl an wartenden Kunden ab denen ein Transporter angefordert wird
	 */
	public void setRequestMinWaiting(final int requestMinWaiting) {
		if (requestMinWaiting>0) this.requestMinWaiting=requestMinWaiting;
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
	 * Liefert die Priorität mit der verfügbare Transporter im Bedarfsfall angezogen werden.
	 * @return	Priorität mit der verfügbare Transporter im Bedarfsfall angezogen werden
	 */
	public String getRequestPriority() {
		return requestPriority;
	}

	/**
	 * Stellt die Priorität mit der verfügbare Transporter im Bedarfsfall angezogen werden ein.
	 * @param requestPriority	Priorität mit der verfügbare Transporter im Bedarfsfall angezogen werden
	 */
	public void setRequestPriority(final String requestPriority) {
		if (requestPriority!=null) this.requestPriority=requestPriority;
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
	 * Liefert die Daten der Kundentypenprioritäten bei der Zuweisung zu Transportern
	 * @return	Daten der Kundentypenprioritäten bei der Zuweisung zu Transportern
	 */
	public Map<String,String> getClientPriorities() {
		return clientPriority;
	}

	/**
	 * Liefert die Priorität für Kunden eines bestimmten Kundentyp.
	 * @param clientType	Kundentyp für den die Priorität geliefert werden soll
	 * @return Priorität für Kunden dieses Kundentyps
	 */
	public String getClientPriority(final String clientType) {
		final String priority=clientPriority.get(clientType);
		if (priority!=null) return priority;
		return DEFAULT_CLIENT_PRIORITY;
	}

	/**
	 * Liefert den Namen der zugehörigen "Bereich betreten"-Station.
	 * @return	Namen der zugehörigen "Bereich betreten"-Station
	 */
	public String getSectionStartName() {
		return sectionStartName;
	}

	/**
	 * Stellt den Namen der zugehörigen "Bereich betreten"-Station ein.
	 * @param sectionStartName	Namen der zugehörigen "Bereich betreten"-Station
	 */
	public void setSectionStartName(final String sectionStartName) {
		if (sectionStartName==null) this.sectionStartName=""; else this.sectionStartName=sectionStartName;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementTransportTransporterSource)) return false;

		final List<ModelElementEdge> connections2=((ModelElementTransportTransporterSource)element).connections;
		if (connections==null || connections2==null || connections.size()!=connections2.size()) return false;
		for (int i=0;i<connections.size();i++) if (connections.get(i).getId()!=connections2.get(i).getId()) return false;

		if (!transportTargetSystem.equalsTransportTargetSystem(((ModelElementTransportTransporterSource)element).transportTargetSystem)) return false;

		if (!transporterType.equals(((ModelElementTransportTransporterSource)element).transporterType)) return false;

		if (requestMinWaiting!=((ModelElementTransportTransporterSource)element).requestMinWaiting) return false;
		if (!requestPriority.equals(((ModelElementTransportTransporterSource)element).requestPriority)) return false;
		if (waitingCapacity!=((ModelElementTransportTransporterSource)element).waitingCapacity) return false;
		if (!waitingPriority.equals(((ModelElementTransportTransporterSource)element).waitingPriority)) return false;

		if (!Objects.deepEquals(clientPriority,((ModelElementTransportTransporterSource)element).clientPriority)) return false;

		if (!sectionStartName.equals(((ModelElementTransportTransporterSource)element).sectionStartName)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementTransportTransporterSource) {
			connections.clear();
			final List<ModelElementEdge> connections2=((ModelElementTransportTransporterSource)element).connections;
			if (connections2==null) return;
			connectionIds=new ArrayList<>();
			for (int i=0;i<connections2.size();i++) connectionIds.add(connections2.get(i).getId());

			transportTargetSystem=((ModelElementTransportTransporterSource)element).transportTargetSystem.clone();

			transporterType=((ModelElementTransportTransporterSource)element).transporterType;
			requestMinWaiting=((ModelElementTransportTransporterSource)element).requestMinWaiting;
			requestPriority=((ModelElementTransportTransporterSource)element).requestPriority;
			waitingCapacity=((ModelElementTransportTransporterSource)element).waitingCapacity;
			waitingPriority=((ModelElementTransportTransporterSource)element).waitingPriority;

			clientPriority.putAll(((ModelElementTransportTransporterSource)element).clientPriority);

			sectionStartName=((ModelElementTransportTransporterSource)element).sectionStartName;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementTransportTransporterSource clone(final EditModel model, final ModelSurface surface) {
		final ModelElementTransportTransporterSource element=new ModelElementTransportTransporterSource(model,surface);
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
		if (connectionIds!=null) {
			for (int i=0;i<connectionIds.size();i++) {
				element=surface.getById(connectionIds.get(i));
				if (element instanceof ModelElementEdge) connections.add((ModelElementEdge)element);
			}
			connectionIds=null;
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.TransportTransporterSource.Name");
	}

	/**
	 * Liefert einen Fehlertext, der unter der Box angezeigt werden soll.<br>
	 * Ist das Element in Ordnung, so soll <code>null</code> zurückgegeben werden.
	 * @return	Optionale Fehlermeldung oder <code>null</code> wenn kein Fehler vorliegt.
	 */
	@Override
	protected String getErrorMessage() {
		final String defaultStation=transportTargetSystem.getDefaultStation();
		if (defaultStation==null || defaultStation.trim().isEmpty()) return Language.tr("Surface.ErrorInfo.NoDefaultDestination");

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
		return Language.tr("Surface.TransportTransporterSource.Name.Short");
	}

	/**
	 * Liefert optional eine zusätzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zusätzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null) return null;
		switch (transportTargetSystem.getMode()) {
		case ROUTING_MODE_EXPLICITE: return Language.tr("Surface.TransportTransporterSource.RouteBy.Settings");
		case ROUTING_MODE_SEQUENCE: return Language.tr("Surface.TransportTransporterSource.RouteBy.Sequence");
		case ROUTING_MODE_TEXT_PROPERTY: return Language.tr("Surface.TransportTransporterSource.RouteBy.Property");
		default: return null;
		}
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
			new ModelElementTransportTransporterSourceDialog(owner,ModelElementTransportTransporterSource.this,readOnly);
		};
	}

	/**
	 * Fügt stations-bedingte zusätzliche Daten zur Laufzeitstatistik hinzu
	 * @param builder	Laufzeitdaten-Builder
	 */
	@Override
	protected void addInformationToAnimationRunTimeData(final SimDataBuilder builder) {
		final RunElementTransportTransporterSourceData data=(RunElementTransportTransporterSourceData)builder.data;
		final RunElementTransportTransporterSource station=(RunElementTransportTransporterSource)builder.data.station;
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
		addVisualizationMenuItem(parentMenu,addElement,VisualizationType.HISTOGRAM_WIP);
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
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surfacePanel	Zeichenfläche
	 * @param point	Punkt auf den geklickt wurde
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		JMenuItem item;
		final Icon icon=Images.EDIT_EDGES_DELETE.getIcon();

		if (connections!=null && connections.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveAllEdges")));
			item.addActionListener(e->{
				for (ModelElementEdge element : new ArrayList<>(connections)) surface.remove(element);
			});
			if (icon!=null) item.setIcon(icon);
			item.setEnabled(!readOnly);
			popupMenu.addSeparator();
		}
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connections!=null) {
			while (connections.size()>0) {
				ModelElement element=connections.remove(0);
				surface.remove(element);
			}
		}
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connections!=null && connections.indexOf(element)>=0) {connections.remove(element); fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.TransportTransporterSource.XML.Root");
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

		if (connections!=null) for (ModelElementEdge element: connections) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		transportTargetSystem.addPropertiesToXML(doc,node);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportTransporterSource.TransporterTyp")));
		sub.setTextContent(transporterType);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportTransporterSource.RequestMinWaiting")));
		sub.setTextContent(""+requestMinWaiting);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportTransporterSource.RequestPriority")));
		sub.setTextContent(requestPriority);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportTransporterSource.WaitingCapacity")));
		sub.setTextContent(""+waitingCapacity);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportTransporterSource.WaitingPriority")));
		sub.setTextContent(waitingPriority);

		for (Map.Entry<String,String> entry: clientPriority.entrySet()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportTransporterSource.ClientPriority")));
			sub.setAttribute(Language.trPrimary("Surface.XML.TransportTransporterSource.ClientPriority.Name"),entry.getKey());
			sub.setTextContent(entry.getValue());
		}

		if (sectionStartName!=null && !sectionStartName.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.TransportTransporterSource.SectionStartName")));
			sub.setTextContent(sectionStartName);
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

		if (Language.trAll("Surface.XML.Connection",name) && Language.trAll("Surface.XML.Connection.Type.In",Language.trAllAttribute("Surface.XML.Connection.Type",node))) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			if (connectionIds==null) connectionIds=new ArrayList<>();
			connectionIds.add(I);
			return null;
		}

		error=transportTargetSystem.loadProperties(node);
		if (error!=null) return error;

		if (Language.trAll("Surface.XML.TransportTransporterSource.TransporterTyp",name)) {
			transporterType=content;
			return null;
		}

		if (Language.trAll("Surface.XML.TransportTransporterSource.RequestMinWaiting",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			requestMinWaiting=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.XML.TransportTransporterSource.RequestPriority",name)) {
			requestPriority=content;
			return null;
		}

		if (Language.trAll("Surface.XML.TransportTransporterSource.WaitingCapacity",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			waitingCapacity=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.XML.TransportTransporterSource.WaitingPriority",name)) {
			waitingPriority=content;
			return null;
		}

		if (Language.trAll("Surface.XML.TransportTransporterSource.ClientPriority",name)) {
			final String clientTypeName=Language.trAllAttribute("Surface.XML.TransportTransporterSource.ClientPriority.Name",node);
			final String clientTypePriority=content;
			if (!clientTypeName.isEmpty() && !clientTypePriority.isEmpty()) clientPriority.put(clientTypeName,clientTypePriority);
			return null;
		}

		if (Language.trAll("Surface.XML.TransportTransporterSource.SectionStartName",name)) {
			sectionStartName=content;
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
		if (edge!=null && connections.indexOf(edge)<0) {
			connections.add(edge);
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
		return connections.toArray(new ModelElementEdge[0]);
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
		return connections.size()>0;
	}

	@Override
	public boolean showFullAnimationRunData() {
		return true;
	}

	@Override
	public boolean hasQueue() {
		return true;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) {
			transportTargetSystem.clientTypeRenamed(oldName,newName);
			final String clientPriorityValue=clientPriority.get(oldName);
			if (clientPriorityValue!=null) {
				clientPriority.remove(oldName);
				clientPriority.put(newName,clientPriorityValue);
			}
		}

		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_TRANSPORT_DESTINATION)) {
			transportTargetSystem.destinationRenamed(oldName,newName);
		}

		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_TRANSPORTER)) {
			if (transporterType.equalsIgnoreCase(oldName)) transporterType=newName;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementTransportTransporterSource";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Transportertyp */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTransporterSource.TransporterType"),transporterType,1000);

		/* Mindestanzahl an wartenden Kunden damit ein Transporter angefordert wird */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTransporterSource.RequestMinWaiting"),""+requestMinWaiting,2000);

		/* Priorität zum Anfordern von Transportern (wenn dieser benötigt wird) */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTransporterSource.RequestPriority"),requestPriority,3000);

		/* Parkplatzkapazität */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTransporterSource.Capacity"),""+waitingCapacity,4000);

		/* Priorität zum Anfordern von Transportern (zum Abstellen) */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTransporterSource.WaitingPriority"),waitingPriority,5000);

		/* Prioritäten */
		for (String clientTypeName: descriptionBuilder.getClientTypes()) {
			String priority=clientPriority.get(clientTypeName);
			if (priority==null || priority.trim().isEmpty()) priority=DEFAULT_CLIENT_PRIORITY;
			descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.TransportTransporterSource.ClientPriority"),clientTypeName),priority,6000);
		}

		/* Transportziele */
		transportTargetSystem.buildDescriptionProperty(descriptionBuilder);

		/* Bereich verlassen */
		if (sectionStartName!=null && !sectionStartName.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportTransporterSource.SectionStartName"),sectionStartName,100000);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsOut.size()>0) return false;

		this.connections.clear();
		this.connections.addAll(connectionsIn);

		return true;
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Einstellungen-Objekt mit den Daten zu den Transportzielen */
		transportTargetSystem.search(searcher,this);

		/* Transportertyp, der hier parken kann */
		searcher.testString(this,Language.tr("Surface.TransportTransporterSource.Dialog.TransporterType"),transporterType,newTransporterType->{transporterType=newTransporterType;});

		/* Anzahl an wartenden Kunden ab denen ein Transporter angefordert wird */
		searcher.testInteger(this,Language.tr("Surface.TransportTransporterSource.Dialog.RequestMinWaiting"),requestMinWaiting,newRequestMinWaiting->{if (newRequestMinWaiting>0) requestMinWaiting=newRequestMinWaiting;});

		/* Priorität mit der verfügbare Transporter im Bedarfsfall angezogen werden */
		searcher.testString(this,Language.tr("Surface.TransportTransporterSource.Dialog.RequestPriority"),requestPriority,newRequestPriority->{requestPriority=newRequestPriority;});

		/* Anzahl an Transportern, die hier parken können */
		searcher.testInteger(this,Language.tr("Surface.TransportTransporterSource.Dialog.WaitingCapacity"),waitingCapacity,newWaitingCapacity->{if (newWaitingCapacity>=0) waitingCapacity=newWaitingCapacity;});

		/* Priorität mit der verfügbare Transporter angezogen werden */
		searcher.testString(this,Language.tr("Surface.TransportTransporterSource.Dialog.WaitingPriority"),waitingPriority);

		/* Daten der Kundentypenprioritäten bei der Zuweisung zu Transportern */
		for (Map.Entry<String,String> record: clientPriority.entrySet()) {
			final String clientType=record.getKey();
			searcher.testString(this,String.format(Language.tr("Editor.DialogBase.Search.PriorityForClientType"),clientType),record.getValue(),newPriority->clientPriority.put(clientType,newPriority));
		}

		/* Namen der zugehörigen "Bereich betreten"-Station */
		searcher.testString(this,Language.tr("Surface.TransportTransporterSource.Dialog.SectionEnd.SectionStart"),sectionStartName,newSectionStartName->{sectionStartName=newSectionStartName;});
	}
}