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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelDataResourceUsage;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Belegt Ressourcen, wenn Kunden die Station passieren bzw. verzögert diese, bis Ressourcen verfügbar sind
 * @author Alexander Herzog
 * @see ModelElementRelease
 */
public class ModelElementSeize extends ModelElementBox implements ModelElementEdgeMultiIn, ModelElementEdgeMultiOut, ModelDataRenameListener, ModelElementAnimationForceMove, ModelDataResourceUsage {
	/**
	 * Liste der einlaufenden Kanten
	 */
	protected final List<ModelElementEdge> connectionsIn;

	/**
	 * Auslaufende Kante (Ressource wurde belegt)
	 */
	protected ModelElementEdge connectionOutSuccess;

	/**
	 * Auslaufende Kante (Timeout für Ressourcenbelegung)
	 */
	protected ModelElementEdge connectionOutCancel;

	/**
	 * IDs der einlaufenden Kanten (Wird nur beim Laden und Clonen verwendet.)
	 */
	private List<Integer> connectionsInIds=null;

	/**
	 * IDs der auslaufenden Kante für erfolgreiche Kunden (wird nur beim Laden und Clonen verwendet)
	 * @see #connectionOutSuccess
	 */
	private int connectionOutSuccessId=-1;

	/**
	 * IDs der auslaufenden Kante für Warteabbrecher (wird nur beim Laden und Clonen verwendet)
	 * @see #connectionOutCancel
	 */
	private int connectionOutCancelId=-1;

	/**
	 * Bedienergruppen und deren Anzahlen, die für die Bedienung der Kunden notwendig sind
	 * @see #getNeededResources()
	 */
	private Map<String,Integer> resources; /* Name der Ressource, benötigte Anzahl */

	/**
	 * Ressourcen-Priorisierungs-Formel
	 * @see #getResourcePriority()
	 * @see #setResourcePriority(String)
	 */
	private String resourcePriority;

	/**
	 * Wartezeit (in Sekunden) nach der ein wartender Kunde aufgibt
	 */
	private double timeOut;

	/**
	 * Konstruktor der Klasse <code>ModelElementSeize</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementSeize(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);

		connectionsIn=new ArrayList<>();

		resources=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		resourcePriority=ModelElementProcess.DEFAULT_RESOURCE_PRIORITY;

		timeOut=-1;
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	/**
	 * Gibt die Möglichkeit, das Label an den auslaufenden Kanten zu aktualisieren, nachdem sich im Element Veränderungen ergeben haben.
	 */
	protected void updateEdgeLabel() {
		if (connectionOutCancel==null) {
			if (connectionOutSuccess!=null) connectionOutSuccess.setName("");
		} else {
			if (connectionOutSuccess!=null) connectionOutSuccess.setName(Language.tr("Surface.Process.Label.Success"));
			connectionOutCancel.setName(Language.tr("Surface.Process.Label.WaitingCancelation"));
		}
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SEIZE.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Seize.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSeize)) return false;

		final ModelElementSeize seize=(ModelElementSeize)element;

		/* Einlaufende Kanten */
		final List<ModelElementEdge> connectionsIn2=seize.connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		/* Auslaufende Kante (Erfolg) */
		if (connectionOutSuccess==null) {
			if (seize.connectionOutSuccess!=null) return false;
		} else {
			if (seize.connectionOutSuccess==null) return false;
			if (connectionOutSuccess.getId()!=seize.connectionOutSuccess.getId()) return false;
		}

		/* Auslaufende Kante (Warteabbrecher) */
		if (connectionOutCancel==null) {
			if (seize.connectionOutCancel!=null) return false;
		} else {
			if (seize.connectionOutCancel==null) return false;
			if (connectionOutCancel.getId()!=seize.connectionOutCancel.getId()) return false;
		}

		/* Ressourcen */
		Map<String,Integer> resourcesA=resources;
		Map<String,Integer> resourcesB=seize.resources;
		for (Map.Entry<String,Integer> entry : resourcesA.entrySet()) {
			if (!entry.getValue().equals(resourcesB.get(entry.getKey()))) return false;
		}
		for (Map.Entry<String,Integer> entry : resourcesB.entrySet()) {
			if (!entry.getValue().equals(resourcesA.get(entry.getKey()))) return false;
		}
		if (!resourcePriority.equalsIgnoreCase(seize.resourcePriority)) return false;

		/* Timeout */
		if (timeOut!=seize.timeOut) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSeize) {

			final ModelElementSeize seize=(ModelElementSeize)element;

			/* Einlaufende Kanten */
			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=seize.connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			/* Auslaufende Kanten */
			if (seize.connectionOutSuccess!=null) connectionOutSuccessId=seize.connectionOutSuccess.getId();
			if (seize.connectionOutCancel!=null) connectionOutCancelId=seize.connectionOutCancel.getId();

			/* Ressourcendaten */
			resources.clear();
			for (Map.Entry<String,Integer> entry: seize.resources.entrySet()) resources.put(entry.getKey(),entry.getValue());
			resourcePriority=seize.resourcePriority;

			/* Timeout */
			timeOut=seize.timeOut;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSeize clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSeize element=new ModelElementSeize(model,surface);
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

		if (connectionOutSuccessId>=0) {
			element=surface.getById(connectionOutSuccessId);
			if (element instanceof ModelElementEdge) connectionOutSuccess=(ModelElementEdge)element;
			connectionOutSuccessId=-1;
			updateEdgeLabel();
		}

		if (connectionOutCancelId>=0) {
			if (connectionOutSuccess!=null) { /* Ohne Erfolgskante keine Abbruchkante */
				element=surface.getById(connectionOutCancelId);
				if (element instanceof ModelElementEdge) connectionOutCancel=(ModelElementEdge)element;
			}
			connectionOutCancelId=-1;
			updateEdgeLabel();
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Seize.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		if (surface==null) return Language.tr("Surface.Seize.Name");
		return Language.tr("Surface.Seize.Name.Short");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(64,127,255);

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
			new ModelElementSeizeDialog(owner,ModelElementSeize.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Visualisierungen hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen zu dem aktuellen Element direkt passende Animationselemente hinzuzufügen.
	 * @param parentMenu	Untermenü des Popupmenüs, welches die Einträge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfläche hinzugefügt werden sollen
	 */
	@Override
	protected void addVisualizationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementPosition[]> addElements) {
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.TEXT_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.LCD_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.SCALE_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_CURRENT);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.BAR_WIP_AVERAGE);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.CHART_WIP);
		addVisualizationMenuItem(parentMenu,addElements,VisualizationType.HISTOGRAM_WIP);
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
		NextStationHelper.nextStationsHold(this,parentMenu,addNextStation);
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
		boolean needSeparator=false;

		needSeparator=needSeparator || addEdgesInContextMenu(popupMenu,surface,readOnly);

		if (connectionOutSuccess!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesOut")));
			item.addActionListener(e->{
				if (connectionOutCancel!=null) surface.remove(connectionOutCancel);
				surface.remove(connectionOutSuccess);
			});
			if (icon!=null) item.setIcon(icon);
			item.setEnabled(!readOnly);
			needSeparator=true;

			if (connectionOutCancel!=null) {
				final JMenu menu=new JMenu(Language.tr("Surface.Connection.LineMode.ChangeAllEdgesOut"));
				popupMenu.add(menu);

				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Global"),Images.MODEL.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(null));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Direct"),Images.EDGE_MODE_DIRECT.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.DIRECT));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLine"),Images.EDGE_MODE_MULTI_LINE.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.MULTI_LINE));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLineRounded"),Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.MULTI_LINE_ROUNDED));
				menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.CubicCurve"),Images.EDGE_MODE_CUBIC_CURVE.getIcon()));
				item.setEnabled(!readOnly);
				item.addActionListener(e->setEdgeOutLineMode(ModelElementEdge.LineMode.CUBIC_CURVE));
			}
		}

		if (needSeparator) popupMenu.addSeparator();
	}

	/**
	 * Stellt den Darstellungsmodus für alle auslaufenden Kanten ein.
	 * @param lineMode	Neuer Darstellungsmodus
	 */
	private void setEdgeOutLineMode(final ModelElementEdge.LineMode lineMode) {
		connectionOutSuccess.setLineMode(lineMode);
		connectionOutSuccess.fireChanged();
		connectionOutCancel.setLineMode(lineMode);
		connectionOutCancel.fireChanged();
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
		if (connectionOutSuccess!=null) surface.remove(connectionOutSuccess);
		if (connectionOutCancel!=null) surface.remove(connectionOutCancel);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
		if (connectionOutSuccess==element) {connectionOutSuccess=null; fireChanged();}
		if (connectionOutCancel==element) {connectionOutCancel=null; fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Seize.XML.Root");
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

		/* Kanten */

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		if (connectionOutSuccess!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connectionOutSuccess.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.tr("Surface.XML.Connection.Type.Out"));
		}
		if (connectionOutCancel!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connectionOutCancel.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.tr("Surface.XML.Connection.Type.Out"));
			sub.setAttribute(Language.trPrimary("Surface.Process.XML.Connection.Status"),Language.tr("Surface.Process.XML.Connection.Status.WaitingCancelation"));
		}

		/* Ressourcen */

		for (Map.Entry<String,Integer> entry : resources.entrySet()) if (entry.getValue()!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Seize.XML.Operators")));
			sub.setAttribute(Language.trPrimary("Surface.Seize.XML.Operators.Group"),entry.getKey());
			sub.setAttribute(Language.trPrimary("Surface.Seize.XML.Operators.Count"),""+entry.getValue());
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Seize.XML.Priority")));
		sub.setTextContent(resourcePriority);

		/* Timeout */
		if (timeOut>=0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Seize.XML.TimeOut")));
			sub.setTextContent(NumberTools.formatSystemNumber(timeOut));
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

		/* Kanten */

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			final String s=Language.trAllAttribute("Surface.XML.Connection.Type",node);
			if (Language.trAll("Surface.XML.Connection.Type.In",s)) {
				if (connectionsInIds==null) connectionsInIds=new ArrayList<>();
				connectionsInIds.add(I);
			}
			if (Language.trAll("Surface.XML.Connection.Type.Out",s)) {
				final String status=Language.trAllAttribute("Surface.Process.XML.Connection.Status",node);
				if (Language.trAll("Surface.Process.XML.Connection.Status.WaitingCancelation",status)) {
					connectionOutCancelId=I;
				} else {
					connectionOutSuccessId=I;
				}
			}
			return null;
		}

		/* Ressourcen */

		if (Language.trAll("Surface.Seize.XML.Operators",name)) {
			final String typ=Language.trAllAttribute("Surface.Seize.XML.Operators.Group",node);
			final Long L=NumberTools.getPositiveLong(Language.trAllAttribute("Surface.Seize.XML.Operators.Count",node));
			if (L==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.Seize.XML.Operators.Count"),name,node.getParentNode().getNodeName());
			final int i=(int)((long)L);
			if (!typ.trim().isEmpty()) resources.put(typ,i);
			return null;
		}

		if (Language.trAll("Surface.Seize.XML.Priority",name)) {
			resourcePriority=node.getTextContent();
			return null;
		}

		/* Timeout */

		if (Language.trAll("Surface.Seize.XML.TimeOut",name)) {
			final Double D=NumberTools.getDouble(node.getTextContent());
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			timeOut=D;
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
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return (connectionOutSuccess==null) || (connectionOutCancel==null);
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (connectionOutSuccess!=null && connectionOutCancel!=null) return false;
		if (edge==null || connectionsIn.indexOf(edge)>=0) return false;

		if (connectionOutSuccess==null) {
			connectionOutSuccess=edge;
			connectionOutSuccessId=-1;
		} else {
			connectionOutCancel=edge;
			connectionOutCancelId=-1;
		}
		fireChanged();
		return true;
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
	 * Auslaufende Kanten
	 * @return	Auslaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesOut() {
		if (connectionOutSuccess==null) {
			if (connectionOutCancel==null) return new ModelElementEdge[0];
			return new ModelElementEdge[]{connectionOutCancel};
		} else {
			if (connectionOutCancel==null) return new ModelElementEdge[]{connectionOutSuccess};
			return new ModelElementEdge[]{connectionOutSuccess, connectionOutCancel};
		}
	}

	/**
	 * Liefert die auslaufende Kante für erfolgreiche Kunden.
	 * @return	Auslaufende Kante (erfolgreiche Kunden)
	 */
	public ModelElementEdge getEdgeOutSuccess() {
		return connectionOutSuccess;
	}

	/**
	 * Liefert die auslaufende Kante für Warteabbrecher.
	 * @return	Auslaufende Kante (Warteabbrecher)
	 */
	public ModelElementEdge getEdgeOutCancel() {
		return connectionOutCancel;
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

	/**
	 * Liefert die Aufstellung der zur Bearbeitung von Kunden benötigten Ressourcen
	 * @return	Bedienergruppen und deren Anzahlen, die für die Bedienung der Kunden notwendig sind
	 */
	public Map<String,Integer> getNeededResources() {
		return resources;
	}

	/**
	 * Liefert die Formel zurück, gemäß derer die Priorisierung der Bedienstation bei der Zuweisung von verfügbaren Ressourcen erfolgen soll.
	 * @return	Aktuelle Ressourcen-Priorisierungs-Formel
	 */
	public String getResourcePriority() {
		return resourcePriority;
	}

	/**
	 * Stellt die Formel ein, gemäß derer die Priorisierung der Bedienstation bei der Zuweisung von verfügbaren Ressourcen erfolgen soll.
	 * @param newResourcePriority Neue Ressourcen-Priorisierungs-Formel
	 */
	public void setResourcePriority(final String newResourcePriority) {
		if (newResourcePriority==null || newResourcePriority.trim().isEmpty()) return;
		resourcePriority=newResourcePriority;
	}

	/**
	 * Liefert die maximale Wartezeit (in Sekunden) nach der ein wartender Kunde aufgibt.
	 * @return	Wartezeit (in Sekunden) nach der ein wartender Kunde aufgibt
	 * @see #setTimeOut(double)
	 */
	public double getTimeOut() {
		return timeOut;
	}

	/**
	 * Stellt die maximale Wartezeit (in Sekunden) nach der ein wartender Kunde aufgibt ein.
	 * @param timeOut	Wartezeit (in Sekunden) nach der ein wartender Kunde aufgibt
	 * @see #getTimeOut()
	 */
	public void setTimeOut(final double timeOut) {
		this.timeOut=(timeOut<0)?-1:timeOut;
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
	public boolean hasQueue() {
		return true;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_RESOURCE)) {
			final Map<String,Integer> map=getNeededResources();
			final Integer neededNumber=map.get(oldName);
			if (neededNumber!=null) {map.remove(oldName); map.put(newName,neededNumber);}
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSeize";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Ressourcen */
		final StringBuilder sb=new StringBuilder();
		for (Map.Entry<String,Integer> entry: resources.entrySet()) {
			if (entry.getValue().intValue()>0) {
				if (sb.length()>0) sb.append("\n");
				sb.append(String.format(Language.tr("ModelDescription.Seize.Resources.Resource"),entry.getKey(),entry.getValue().intValue()));
			}
		}
		descriptionBuilder.addProperty(Language.tr("ModelDescription.Seize.Resources"),sb.toString(),1000);

		/* Ressourcenpriorität */
		if (resourcePriority!=null && !resourcePriority.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Seize.ResourcePriority"),resourcePriority,2000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Seize.ResourcePriority"),ModelElementProcess.DEFAULT_RESOURCE_PRIORITY,2000);
		}

		/* Timeout */
		if (timeOut>=0 && connectionOutCancel!=null) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Seize.Timeout"),NumberTools.formatNumber(timeOut)+" "+Language.tr("Statistic.Seconds"),3000);
		}

		/* Nächste Stationen */
		descriptionBuilder.addEdgeOut(connectionOutSuccess);
		if (connectionOutCancel!=null) descriptionBuilder.addConditionalEdgeOut(Language.tr("ModelDescription.Process.EdgeOutCancel"),connectionOutCancel);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsOut.size()>2) return false;

		this.connectionsIn.clear();
		this.connectionsIn.addAll(connectionsIn);

		this.connectionOutSuccess=null;
		if (connectionsOut.size()>0) this.connectionOutSuccess=connectionsOut.get(0);
		this.connectionOutCancel=null;
		if (connectionsOut.size()>1) this.connectionOutCancel=connectionsOut.get(0);

		return true;
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Ressourcenzuordnung -> keine Suche */

		searcher.testString(this,Language.tr("Surface.Seize.Dialog.ResourcePriority"),resourcePriority,newResourcePriority->{resourcePriority=newResourcePriority;});

		if (timeOut>=0) searcher.testDouble(Language.tr("Surface.Seize.Dialog.Timeout"),timeOut);
	}

	@Override
	public Map<String,Integer> getUsedResourcesInfo() {
		final Map<String,Integer> map=new HashMap<>();
		map.putAll(resources);
		return map;
	}

	@Override
	public void addResourceUsage(final String resourceName, final int neededNumber) {
		final int currentUsage=resources.getOrDefault(resourceName,0);
		resources.put(resourceName,currentUsage+neededNumber);
	}
}