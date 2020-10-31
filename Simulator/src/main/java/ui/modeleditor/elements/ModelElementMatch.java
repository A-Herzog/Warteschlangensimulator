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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Wartet bis an jedem Eingang der Station ein Kunde wartet
 * und leitet sie dann entweder gleichzeitig einzeln oder
 * als temporären oder permanenten Batch weiter.
 * @author Alexander Herzog
 */
public class ModelElementMatch extends ModelElementBox implements ElementWithNewClientNames, ModelElementEdgeMultiIn, ModelElementEdgeOut, ModelElementAnimationForceMove {
	/**
	 * Art der Batch-Bildung
	 * @author Alexander Herzog
	 * @see ModelElementMatch#getMatchMode()
	 * @see ModelElementMatch#setMatchMode(MatchMode)
	 */
	public enum MatchMode {
		/** Sammelt die Kunden und leitet sie dann gemeinsam weiter (kein Batching im engeren Sinne). */
		MATCH_MODE_COLLECT,

		/** Fasst die Kunden zu einem temporären Batch zusammen. */
		MATCH_MODE_TEMPORARY,

		/** Fasst die Kunden zu einem neuen Kunden zusammen (permanentes Batching). */
		MATCH_MODE_PERMANENT
	}

	/**
	 * Von welcher Art ist die Eigenschaft, die zwischen
	 * den wartenden Kunden abgeglichen werden soll?
	 * @author Alexander Herzog
	 * @see ModelElementMatch#getMatchPropertyMode()
	 * @see ModelElementMatch#setMatchPropertyMode(MatchPropertyMode)
	 */
	public enum MatchPropertyMode {
		/** Kein Abgleich von Eigenschaften */
		NONE,

		/** Eine Zahlen-Eigenschaft zum Abgleich verwenden */
		NUMBER,

		/** Eine Text-Eigenschaft zum Abgleich verwenden */
		TEXT,
	}

	/**
	 * Liste der einlaufenden Kanten
	 */
	protected final List<ModelElementEdge> connectionsIn;

	/**
	 * Auslaufende Kante
	 */
	protected ModelElementEdge connectionOut;

	/**
	 * Modus wie Eigenschaften der Kunden beim Zusammenführen abgeglichen werden sollen
	 * @see #getMatchMode()
	 * @see #setMatchMode(MatchMode)
	 * @see MatchPropertyMode
	 */
	private MatchPropertyMode matchPropertyMode;

	private int matchPropertyNumber;
	private String matchPropertyString;

	/**
	 * Match-Modus
	 * @see #getMatchMode()
	 * @see #setMatchMode(MatchMode)
	 */
	private MatchMode matchMode;

	/**
	 * Neuer Kundentyp (wenn die Kunden nicht einfach einzeln weitergeleitet werden)
	 * @see #getNewClientType()
	 * @see #setNewClientType(String)
	 */
	private String newClientType;

	/** IDs der einlaufenden Kanten (Wird nur beim Laden und Clonen verwendet.) */
	private List<Integer> connectionsInIds=null;
	/** ID der auflaufenden Kante (Wird nur beim Laden und Clonen verwendet.) */
	private int connectionOutId=-1;

	/**
	 * Konstruktor der Klasse <code>ModelElementMatch</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementMatch(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_WEDGE_ARROW_RIGHT);
		connectionsIn=new ArrayList<>();

		matchPropertyMode=MatchPropertyMode.NONE;
		matchPropertyNumber=0;
		matchPropertyString="";

		matchMode=MatchMode.MATCH_MODE_COLLECT;
		newClientType="";
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_MATCH.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Match.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		updateIcon();
		super.fireChanged();
	}

	private void updateIcon() {
		if (matchMode!=null) switch (matchMode) {
		case MATCH_MODE_COLLECT:
			setAdditionalClientIconFromName(null);
			break;
		case MATCH_MODE_PERMANENT:
			setAdditionalClientIconFromName(newClientType);
			break;
		case MATCH_MODE_TEMPORARY:
			setAdditionalClientIconFromName(newClientType);
			break;
		}
	}

	/**
	 * Aktualisiert die Beschriftung der auslaufenden Kante
	 * @see #fireChanged()
	 */
	private void updateEdgeLabel() {
		final String s=newClientType;
		if (connectionOut!=null) connectionOut.setName(s.isEmpty()?"":(Language.tr("Surface.Match.NewClientType")+" \""+s+"\""));
	}

	/**
	 * Liefert den aktuellen Modus wie Eigenschaften der Kunden beim Zusammenführen abgeglichen werden sollen.
	 * @return	Aktueller Modus wie Eigenschaften der Kunden beim Zusammenführen abgeglichen werden sollen
	 */
	public MatchPropertyMode getMatchPropertyMode() {
		return matchPropertyMode;
	}

	/**
	 * Stellt einen neuen Modus wie Eigenschaften der Kunden beim Zusammenführen abgeglichen werden sollen ein.
	 * @param newMatchPropertyMode	Neuer Modus wie Eigenschaften der Kunden beim Zusammenführen abgeglichen werden sollen
	 */
	public void setMatchPropertyMode(final MatchPropertyMode newMatchPropertyMode) {
		if (newMatchPropertyMode!=null) matchPropertyMode=newMatchPropertyMode;
	}

	/**
	 * Liefert, wenn beim Zusammenführen ein Eigenschaftsabgleich auf Basis eines Zahenwertes erfolgen soll, den Index der Kundendateneigenschaft.
	 * @return	Index der Kundendateneigenschaft
	 */
	public int getMatchPropertyNumber() {
		return matchPropertyNumber;
	}

	/**
	 * Stellt für den Fall, dass beim Zusammenführen ein Eigenschaftsabgleich auf Basis eines Zahenwertes erfolgen soll, den Index der Kundendateneigenschaft ein.
	 * @param newMatchPropertyNumber	Index der Kundendateneigenschaft
	 */
	public void setMatchPropertyNumber(final int newMatchPropertyNumber) {
		matchPropertyMode=MatchPropertyMode.NUMBER;
		matchPropertyNumber=newMatchPropertyNumber;
	}

	/**
	 * Liefert, wenn beim Zusammenführen ein Eigenschaftsabgleich auf Basis einer Zeichenkette erfolgen soll, den Schlüssel der Kundendaten-Texteigenschaft.
	 * @return	Schlüssel der Kundendaten-Texteigenschaft
	 */
	public String getMatchPropertyString() {
		if (matchPropertyString==null) return ""; else return matchPropertyString;
	}

	/**
	 * Stellt für den Fall, dass beim Zusammenführen ein Eigenschaftsabgleich auf Basis einer Zeichenkette erfolgen soll, den Schlüssel der Kundendaten-Texteigenschaft ein.
	 * @param newMatchPropertyString	Schlüssel der Kundendaten-Texteigenschaft.
	 */
	public void setMatchPropertyString(final String newMatchPropertyString) {
		matchPropertyMode=MatchPropertyMode.TEXT;
		matchPropertyString=(newMatchPropertyString==null)?"":newMatchPropertyString;
	}

	/**
	 * Liefert den aktuell gewählten Match-Modus.
	 * @return	Aktueller Match-Modus
	 * @see MatchMode
	 */
	public MatchMode getMatchMode() {
		return matchMode;
	}

	/**
	 * Stellt den Match-Modus ein.
	 * @param matchMode	Neuer Match-Modus
	 * @see MatchMode
	 */
	public void setMatchMode(MatchMode matchMode) {
		switch (matchMode) {
		case MATCH_MODE_COLLECT:
			this.matchMode=MatchMode.MATCH_MODE_COLLECT;
			newClientType="";
			break;
		case MATCH_MODE_TEMPORARY:
			this.matchMode=MatchMode.MATCH_MODE_TEMPORARY;
			break;
		case MATCH_MODE_PERMANENT:
			this.matchMode=MatchMode.MATCH_MODE_PERMANENT;
			break;
		}
	}

	/**
	 * Liefert den Kundentyp, unter dem die zusammengeführten Kunden weitergeleitet werden sollen.
	 * @return	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public String getNewClientType() {
		return newClientType;
	}

	/**
	 * Stellt den Kundentyp ein, unter dem die zusammengeführten Kunden weitergeleitet werden sollen.
	 * @param newClientType	Neuer Kundentyp oder leerer String, wenn die Kunden einzeln weitergeleitet werden sollen
	 */
	public void setNewClientType(final String newClientType) {
		if (newClientType==null || newClientType.trim().isEmpty()) {
			this.newClientType="";
			matchMode=MatchMode.MATCH_MODE_COLLECT;
		} else {
			this.newClientType=newClientType;
			if (matchMode==MatchMode.MATCH_MODE_COLLECT) matchMode=MatchMode.MATCH_MODE_PERMANENT;
		}
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementMatch)) return false;

		final ModelElementMatch otherMatch=(ModelElementMatch)element;

		if (matchPropertyMode!=otherMatch.matchPropertyMode) return false;
		if (matchPropertyMode==MatchPropertyMode.NUMBER) {
			if (matchPropertyNumber!=otherMatch.matchPropertyNumber) return false;
		}
		if (matchPropertyMode==MatchPropertyMode.TEXT) {
			if (!Objects.deepEquals(matchPropertyString,otherMatch.matchPropertyString)) return false;
		}

		if (matchMode!=otherMatch.matchMode) return false;
		if (matchMode==MatchMode.MATCH_MODE_TEMPORARY || matchMode==MatchMode.MATCH_MODE_PERMANENT) {
			if (!otherMatch.newClientType.equals(newClientType)) return false;
		}

		if (connectionOut==null) {
			if (otherMatch.connectionOut!=null) return false;
		} else {
			if (otherMatch.connectionOut==null) return false;
			if (connectionOut.getId()!=otherMatch.connectionOut.getId()) return false;
		}

		final List<ModelElementEdge> connectionsIn2=otherMatch.connectionsIn;
		if (connectionsIn==null || connectionsIn2==null || connectionsIn.size()!=connectionsIn2.size()) return false;
		for (int i=0;i<connectionsIn.size();i++) if (connectionsIn.get(i).getId()!=connectionsIn2.get(i).getId()) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementMatch) {
			final ModelElementMatch source=(ModelElementMatch)element;

			matchPropertyMode=source.matchPropertyMode;
			matchPropertyNumber=source.matchPropertyNumber;
			matchPropertyString=source.matchPropertyString;

			matchMode=source.matchMode;
			if (matchMode==MatchMode.MATCH_MODE_TEMPORARY || matchMode==MatchMode.MATCH_MODE_PERMANENT) {
				newClientType=source.newClientType;
			} else {
				newClientType="";
			}

			connectionsIn.clear();
			final List<ModelElementEdge> connectionsIn2=source.connectionsIn;
			if (connectionsIn2!=null) {
				connectionsInIds=new ArrayList<>();
				for (int i=0;i<connectionsIn2.size();i++) connectionsInIds.add(connectionsIn2.get(i).getId());
			}

			if (source.connectionOut!=null) connectionOutId=source.connectionOut.getId();
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementMatch clone(final EditModel model, final ModelSurface surface) {
		final ModelElementMatch element=new ModelElementMatch(model,surface);
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

		if (connectionOutId>=0) {
			element=surface.getById(connectionOutId);
			if (element instanceof ModelElementEdge) connectionOut=(ModelElementEdge)element;
			connectionOutId=-1;
			updateEdgeLabel();
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Match.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.Match.Name.Short");
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
	 * Fügt Menüpunkte zum Hinzufügen von einlaufenden und auslaufender Kante zum Kontextmenü
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 * @return	Gibt <code>true</code> zurück, wenn Elemente in das Kontextmenü eingefügt wurden (und ggf. ein Separator vor dem nächsten Abschnitt gesetzt werden sollte)
	 */
	protected final boolean addRemoveEdgesContextMenuItems(final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();
		boolean needSeparator=false;

		if (connectionsIn!=null && connectionsIn.size()>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesIn")));
			item.addActionListener(e->{
				for (ModelElementEdge element : new ArrayList<>(connectionsIn)) surface.remove(element);
			});
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}
		if (connectionOut!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgeOut")));
			item.addActionListener(e->surface.remove(connectionOut));
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
			item.setEnabled(!readOnly);
			needSeparator=true;
		}

		return needSeparator;
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
			new ModelElementMatchDialog(owner,ModelElementMatch.this,readOnly);
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
		if (connectionsIn.size()>0) {
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_CURRENT_1);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_CURRENT_1);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_CURRENT_1);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_AVERAGE_1);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_AVERAGE_1);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_AVERAGE_1);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_CURRENT_1);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_AVERAGE_1);
		}
		if (connectionsIn.size()>1) {
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_CURRENT_2);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_CURRENT_2);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_CURRENT_2);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_AVERAGE_2);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_AVERAGE_2);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_AVERAGE_2);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_CURRENT_2);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_AVERAGE_2);
		}
		if (connectionsIn.size()>2) {
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_CURRENT_3);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_CURRENT_3);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_CURRENT_3);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.TEXT_NQ_AVERAGE_3);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.LCD_NQ_AVERAGE_3);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.SCALE_NQ_AVERAGE_3);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_CURRENT_3);
			addVisualizationMenuItem(parentMenu,addElement,VisualizationType.BAR_NQ_AVERAGE_3);
		}
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
		if (connectionOut!=null) surface.remove(connectionOut);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (connectionsIn!=null && connectionsIn.indexOf(element)>=0) {connectionsIn.remove(element); fireChanged();}
		if (connectionOut==element) {connectionOut=null; fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Match.XML.Root");
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

		if (connectionsIn!=null) for (ModelElementEdge element: connectionsIn) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		if (connectionOut!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connectionOut.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
		}

		switch (matchPropertyMode) {
		case NUMBER:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.MatchPropertyMode")));
			sub.setAttribute(Language.trPrimary("Surface.Match.XML.MatchPropertyMode.Mode"),Language.trPrimary("Surface.Match.XML.MatchPropertyMode.Mode.Number"));
			sub.setTextContent(""+matchPropertyNumber);
			break;
		case TEXT:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.MatchPropertyMode")));
			sub.setAttribute(Language.trPrimary("Surface.Match.XML.MatchPropertyMode.Mode"),Language.trPrimary("Surface.Match.XML.MatchPropertyMode.Mode.Text"));
			sub.setTextContent(matchPropertyString);
			break;
		case NONE:
			/* Hier gibt's nichts zu speichern. */
			break;
		}

		switch (matchMode) {
		case MATCH_MODE_COLLECT:
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.MatchMode")));
			sub.setTextContent(Language.trPrimary("Surface.Match.XML.MatchMode.Collect"));
			break;
		case MATCH_MODE_TEMPORARY:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.MatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Match.XML.MatchMode.Temporary"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.MatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Match.XML.MatchMode.Collect"));
			}
			break;
		case MATCH_MODE_PERMANENT:
			if (!newClientType.isEmpty()) {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.MatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Match.XML.MatchMode.Permanent"));
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.ClientType")));
				sub.setTextContent(newClientType);
			} else {
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Match.XML.MatchMode")));
				sub.setTextContent(Language.trPrimary("Surface.Match.XML.MatchMode.Collect"));
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
				connectionOutId=I;
			}
			return null;
		}

		if (Language.trAll("Surface.Match.XML.MatchPropertyMode",name)) {
			final String mode=Language.trAllAttribute("Surface.Match.XML.MatchPropertyMode.Mode",node);
			if (Language.trAll("Surface.Match.XML.MatchPropertyMode.Mode.Number",mode)) {
				matchPropertyMode=MatchPropertyMode.NUMBER;
				final Integer I=NumberTools.getNotNegativeInteger(content);
				if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
				matchPropertyNumber=I;
			}
			if (Language.trAll("Surface.Match.XML.MatchPropertyMode.Mode.Text",mode)) {
				matchPropertyMode=MatchPropertyMode.TEXT;
				matchPropertyString=content;
			}
			return null;
		}

		if (Language.trAll("Surface.Match.XML.MatchMode",name)) {
			if (Language.trAll("Surface.Match.XML.MatchMode.Collect",content)) matchMode=MatchMode.MATCH_MODE_COLLECT;
			if (Language.trAll("Surface.Match.XML.MatchMode.Temporary",content)) matchMode=MatchMode.MATCH_MODE_TEMPORARY;
			if (Language.trAll("Surface.Match.XML.MatchMode.Permanent",content)) matchMode=MatchMode.MATCH_MODE_PERMANENT;
			return null;
		}

		if (Language.trAll("Surface.Match.XML.ClientType",name)) {
			newClientType=content;
			if (!content.trim().isEmpty() && matchMode==MatchMode.MATCH_MODE_COLLECT) matchMode=MatchMode.MATCH_MODE_PERMANENT;
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
		return connectionOut==null;
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (connectionOut!=null) return false;
		if (edge==null || connectionsIn.indexOf(edge)>=0) return false;
		connectionOut=edge;
		connectionOutId=-1;
		fireChanged();
		return true;
	}

	/**
	 * Liste der einlaufenden Kanten
	 * @return	Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		if (connectionsIn==null) return new ModelElementEdge[0]; else return connectionsIn.toArray(new ModelElementEdge[0]);
	}

	/**
	 * Auslaufende Kante
	 * @return	Auslaufende Kante
	 */
	@Override
	public ModelElementEdge getEdgeOut() {
		return connectionOut;
	}

	@Override
	public String[] getNewClientTypes() {
		return new String[]{getNewClientType()};
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
		return "ModelElementMatch";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Properties abgleichen */
		switch (matchPropertyMode) {
		case NONE:
			/* Keine Ausgabe */
			break;
		case NUMBER:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Match.PropertyMode"),String.format(Language.tr("ModelDescription.Match.PropertyMode.Number"),matchPropertyNumber),1000);
			break;
		case TEXT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Match.PropertyMode"),String.format(Language.tr("ModelDescription.Match.PropertyMode.Text"),matchPropertyString),1000);
			break;
		default:
			break;
		}

		/* Batching Modus */
		switch (matchMode) {
		case MATCH_MODE_COLLECT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Match.Mode"),Language.tr("ModelDescription.Match.Mode.Collect"),2000);
			break;
		case MATCH_MODE_TEMPORARY:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Match.Mode"),Language.tr("ModelDescription.Match.Mode.Temporary"),2000);
			break;
		case MATCH_MODE_PERMANENT:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Match.Mode"),Language.tr("ModelDescription.Match.Mode.Permanent"),2000);
			break;
		}

		/* Neuer Kundentyp */
		if ((matchMode==MatchMode.MATCH_MODE_TEMPORARY || matchMode==MatchMode.MATCH_MODE_PERMANENT) && !newClientType.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Match.NewClientType"),newClientType,3000);
		}

		/* Auslaufende Kante */
		descriptionBuilder.addEdgeOut(connectionOut);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsOut.size()!=1) return false;

		this.connectionsIn.clear();
		this.connectionsIn.addAll(connectionsIn);

		this.connectionOut=connectionsOut.get(0);

		return true;
	}
}