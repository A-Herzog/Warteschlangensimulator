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
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.db.DBSettings;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.DataCheckResult;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Kundenquelle, die die konkreten Ank�nfte aus einer Datenbank l�dt
 * @author Alexander Herzog
 */
public class ModelElementSourceDB extends ModelElementBox implements ElementWithNewClientNames, ModelElementEdgeOut, ElementNoRemoteSimulation, ElementWithDB {
	/**
	 * Auslaufende Kante
	 */
	private ModelElementEdge connection;

	/**
	 * ID der auslaufenden Kante (wird nur beim Laden und Clonen verwendet)
	 */
	private int connectionId=-1;

	/**
	 * Einstellungen zur Verbindung zur Datenbank
	 * @see #getDb()
	 */
	private DBSettings db;

	/**
	 * Name der Tabelle aus der die Daten ausgelesen werden sollen
	 * @see #getTable()
	 * @see #setTable(String)
	 */
	private String table;

	/**
	 * Name der zu ladenden Tabellenspalte
	 * @see #getLoadColumn()
	 * @see #setLoadColumn(String)
	 */
	private String loadColumn;

	/**
	 * Name Tabellenspalte, die die Kundentypnamen enth�lt
	 * @see #getClientTypeColumn()
	 * @see #setClientTypeColumn(String)
	 */
	private String clientTypeColumn;

	/**
	 * Name der Tabellenspalte mit Zusatzdaten (oder ein leerer String, wenn keine Zusatzdaten-Tabellenspalte definiert ist)
	 * @see #getInfoColumn()
	 * @see #setInfoColumn(String)
	 */
	private String infoColumn;

	/**
	 * Kundentypennamen, die in der Tabelle vorkommen
	 * @see #getClientTypeNames()
	 */
	private final List<String> clientTypeNames=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceDB</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementSourceDB(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ARROW_RIGHT);

		db=new DBSettings();
		table="";
		loadColumn="";
		clientTypeColumn="";
		infoColumn="";
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SOURCE_DB.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.SourceDB.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements �ndert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	/**
	 * Aktualisiert die Beschriftung der auslaufenden Kante
	 * @see #fireChanged()
	 */
	private void updateEdgeLabel() {
		if (connection!=null) connection.setName(Language.tr("Surface.Source.LabelArrivals"));
	}

	/**
	 * Liefert die Einstellungen zur Verbindung zur Datenbank
	 * @return	Einstellungen zur Verbindung zur Datenbank
	 */
	@Override
	public DBSettings getDb() {
		return db;
	}

	/**
	 * Liefert den Namen der Tabelle aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	public String getTable() {
		if (table==null) return ""; else return table;
	}

	/**
	 * Stellt den Namen der Tabelle aus der die Daten ausgelesen werden sollen ein.
	 * @param table	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	public void setTable(final String table) {
		if (table==null) this.table=""; else this.table=table;
	}

	/**
	 * Liefert den Namen der zu ladenden Tabellenspalte.
	 * @return	Name der zu ladenden Tabellenspalte
	 * @see ModelElementSourceDB#setLoadColumn(String)
	 */
	public String getLoadColumn() {
		if (loadColumn==null) return ""; else return loadColumn;
	}

	/**
	 * Stellt den Namen der zu ladenden Tabellenspalte ein.
	 * @param loadColumn	Name der zu ladenden Tabellenspalte
	 * @see ModelElementSourceDB#getLoadColumn()
	 */
	public void setLoadColumn(final String loadColumn) {
		if (loadColumn==null) this.loadColumn=""; else this.loadColumn=loadColumn;
	}

	/**
	 * Liefert den Namen der Tabellenspalte, die die Kundentypnamen enth�lt.
	 * @return	Name Tabellenspalte, die die Kundentypnamen enth�lt
	 * @see ModelElementSourceDB#setClientTypeColumn(String)
	 */
	public String getClientTypeColumn() {
		if (clientTypeColumn==null) return ""; else return clientTypeColumn;
	}

	/**
	 * Stellt den Namen der Tabellenspalte, die die Kundentypnamen enth�lt ein.
	 * @param clientTypeColumn	Name der Tabellenspalte, die die Kundentypnamen enth�lt
	 * @see ModelElementSourceDB#getClientTypeColumn()
	 */
	public void setClientTypeColumn(final String clientTypeColumn) {
		if (clientTypeColumn==null) this.clientTypeColumn=""; else this.clientTypeColumn=clientTypeColumn;
	}

	/**
	 * Liefert den Namen der Tabellenspalte, die zus�tzliche Daten f�r die Kundentypen enth�lt.
	 * @return	Name der Tabellenspalte mit Zusatzdaten (oder ein leerer String, wenn keine Zusatzdaten-Tabellenspalte definiert ist)
	 * @see ModelElementSourceDB#setInfoColumn(String)
	 */
	public String getInfoColumn() {
		if (infoColumn==null) return ""; else return infoColumn;
	}

	/**
	 * Stellt den Namen der Tabellenspalte, die zus�tzliche Daten f�r die Kundentypen enth�lt, ein.
	 * @param infoColumn	Name der Tabellenspalte mit Zusatzdaten (oder ein leerer String, wenn keine Zusatzdaten-Tabellenspalte definiert sein soll)
	 * @see ModelElementSourceDB#getInfoColumn()
	 */
	public void setInfoColumn(final String infoColumn) {
		if (infoColumn==null) this.infoColumn=""; else this.infoColumn=infoColumn;
	}

	/**
	 * Listet die Kundentypnamen auf, die in der Tabelle verwendet werden sollen
	 * @return	Kundentypennamen, die in der Tabelle vorkommen
	 */
	public List<String> getClientTypeNames() {
		return clientTypeNames;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSourceDB)) return false;
		final ModelElementSourceDB otherSource=(ModelElementSourceDB)element;

		if (connection==null) {
			if (otherSource.connection!=null) return false;
		} else {
			if (otherSource.connection==null) return false;
			if (connection.getId()!=otherSource.connection.getId()) return false;
		}

		if (!db.equalsDBSettings(otherSource.db)) return false;
		if (!Objects.equals(table,otherSource.table)) return false;
		if (!Objects.equals(loadColumn,otherSource.loadColumn)) return false;
		if (!Objects.equals(clientTypeColumn,otherSource.clientTypeColumn)) return false;
		if (!Objects.equals(infoColumn,otherSource.infoColumn)) return false;

		if (clientTypeNames.size()!=((ModelElementSourceDB)element).clientTypeNames.size()) return false;
		for (int i=0;i<clientTypeNames.size();i++) if (!clientTypeNames.get(i).equals(((ModelElementSourceDB)element).clientTypeNames.get(i))) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSourceDB) {
			final ModelElementSourceDB source=(ModelElementSourceDB)element;
			if (source.connection!=null) connectionId=source.connection.getId();

			db=new DBSettings(source.db);
			table=source.table;
			loadColumn=source.loadColumn;
			clientTypeColumn=source.clientTypeColumn;
			infoColumn=source.infoColumn;

			clientTypeNames.clear();
			clientTypeNames.addAll(source.clientTypeNames);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSourceDB clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSourceDB element=new ModelElementSourceDB(model,surface);
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
		if (connectionId>=0) {
			element=surface.getById(connectionId);
			if (element instanceof ModelElementEdge) connection=(ModelElementEdge)element;
			connectionId=-1;
			updateEdgeLabel();
		}
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		if (connection==null) return Language.tr("Surface.SourceDB.Name")+" - "+Language.tr("Surface.SourceDB.NotConnected");
		return Language.tr("Surface.SourceDB.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.SourceDB.Name");
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
			new ModelElementSourceDBDialog(owner,ModelElementSourceDB.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_OUT);
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsSource(this,parentMenu,addNextStation);
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
		JMenuItem item;
		final Icon icon=Images.EDIT_EDGES_DELETE.getIcon();

		if (connection!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdge")));
			item.addActionListener(e->surface.remove(connection));
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
		if (connection!=null) surface.remove(connection);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (element==connection) {connection=null; fireChanged();}
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.SourceDB.XML.Root");
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

		if (connection!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connection.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
		}

		db.saveToXML(doc,node);

		if (table!=null && !table.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDB.XML.Table")));
			sub.setTextContent(table);
		}

		if (loadColumn!=null && !loadColumn.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDB.XML.LoadColumn")));
			sub.setTextContent(loadColumn);
		}

		if (clientTypeColumn!=null && !clientTypeColumn.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDB.XML.ClientTypeColumn")));
			sub.setTextContent(clientTypeColumn);
		}

		if (infoColumn!=null && !infoColumn.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDB.XML.InfoColumn")));
			sub.setTextContent(infoColumn);
		}


		for (String clientTypeName : clientTypeNames) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDB.XML.ClientTypeName")));
			sub.setTextContent(clientTypeName);
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

		if (Language.trAll("Surface.XML.Connection",name) && Language.trAll("Surface.XML.Connection.Type.Out",Language.trAllAttribute("Surface.XML.Connection.Type", node))) {
			final String s=Language.trAllAttribute("Surface.XML.Connection.Element",node);
			if (!s.isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
				connectionId=I;
			}
			return null;
		}

		if (DBSettings.isDBSettingsNode(node)) {
			return db.loadFromXML(node);
		}

		if (Language.trAll("Surface.SourceDB.XML.Table",name)) {
			table=content;
			return null;
		}

		if (Language.trAll("Surface.SourceDB.XML.LoadColumn",name)) {
			loadColumn=content;
			return null;
		}

		if (Language.trAll("Surface.SourceDB.XML.ClientTypeColumn",name)) {
			clientTypeColumn=content;
			return null;
		}

		if (Language.trAll("Surface.SourceDB.XML.InfoColumn",name)) {
			infoColumn=content;
			return null;
		}

		if (Language.trAll("Surface.SourceDB.XML.ClientTypeName",name)) {
			clientTypeNames.add(content);
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return connection==null;
	}

	/**
	 * F�gt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die auslaufende Kante hinzugef�gt werden konnte.
	 */
	@Override
	public boolean addEdgeOut(ModelElementEdge edge) {
		if (connection!=null) return false;
		connection=edge;
		connectionId=-1;
		fireChanged();
		return true;
	}

	/**
	 * Auslaufende Kante
	 * @return	Auslaufende Kante
	 */
	@Override
	public ModelElementEdge getEdgeOut() {
		return connection;
	}

	@Override
	public String[] getNewClientTypes() {
		return clientTypeNames.toArray(String[]::new);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSourceDB";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		descriptionBuilder.addEdgeOut(getEdgeOut());

		db.buildDescription(descriptionBuilder,1000);

		if (table!=null && !table.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDB.Table"),table,2000);
		if (loadColumn!=null && !loadColumn.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDB.ColumnLoad"),loadColumn,3000);
		if (clientTypeColumn!=null && !clientTypeColumn.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDB.ColumnClientType"),clientTypeColumn,4000);
		if (infoColumn!=null && !infoColumn.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDB.ColumnInfo"),infoColumn,5000);

		final StringBuilder sb=new StringBuilder();
		if (clientTypeNames!=null) for (String clientType: clientTypeNames) {
			if (sb.length()>0) sb.append("\n");
			sb.append(clientType);
		}
		if (sb.length()>0) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceTable.ClientTypes"),sb.toString(),2000);
		}
	}

	@Override
	public DataCheckResult checkExternalData() {
		return DataCheckResult.checkDB(this,db);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsIn.size()>0) return false;
		if (connectionsOut.size()!=1) return false;

		this.connection=connectionsOut.get(0);

		return true;
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.source,fixer);
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Tabelleneinstellungen */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.Table"),table,newTable->{table=newTable;});
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.TableLoadColumn"),loadColumn,newLoadColumn->{loadColumn=newLoadColumn;});
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.TableClientTypeColumn"),clientTypeColumn,newClientTypeColumn->{clientTypeColumn=newClientTypeColumn;});
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.TableInfoColumn"),infoColumn,newInfoColumn->{infoColumn=newInfoColumn;});

		/* Kundentypen */
		for (int i=0;i<clientTypeNames.size();i++) {
			final int index=i;
			searcher.testString(this,Language.tr("Editor.DialogBase.Search.ClientTypeName"),clientTypeNames.get(index),newClientTypeName->clientTypeNames.set(index,newClientTypeName));
		}
	}

	@Override
	public boolean isOutputActive() {
		/* Keine Remote-Simulation */
		return true;
	}
}