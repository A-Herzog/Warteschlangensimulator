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
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.DataCheckResult;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Kundenquelle, die die konkreten Ankünfte per DDE lädt
 * @author Alexander Herzog
 */
public class ModelElementSourceDDE extends ModelElementBox implements ElementWithNewClientNames, ModelElementEdgeOut, ElementWithDDEInputOutput, ElementNoRemoteSimulation {
	private ModelElementEdge connection;

	/* Wird nur beim Laden und Clonen verwendet. */
	private int connectionId=-1;

	private String workbook;
	private String table;
	private int startRow;
	private String column;

	private final List<String> clientTypeNames=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceDDE</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementSourceDDE(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ARROW_RIGHT);

		workbook="";
		table="";
		startRow=1;
		column="";
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SOURCE_DDE.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.SourceDDE.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements ändert.
	 */
	@Override
	public void fireChanged() {
		updateEdgeLabel();
		super.fireChanged();
	}

	private void updateEdgeLabel() {
		if (connection!=null) connection.setName(Language.tr("Surface.Source.LabelArrivals"));
	}

	/**
	 * Liefert den Namen der Arbeitsmappe aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Arbeitsmappe aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public String getWorkbook() {
		if (workbook==null) return ""; else return workbook;
	}

	/**
	 * Stellt den Namen der Arbeitsmappe aus der die Daten ausgelesen werden sollen ein.
	 * @param workbook	Name der Arbeitsmappe aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public void setWorkbook(final String workbook) {
		if (workbook==null) this.workbook=""; else this.workbook=workbook;
	}

	/**
	 * Liefert den Namen der Tabelle aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public String getTable() {
		if (table==null) return ""; else return table;
	}

	/**
	 * Stellt den Namen der Tabelle aus der die Daten ausgelesen werden sollen ein.
	 * @param table	Name der Tabelle aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public void setTable(final String table) {
		if (table==null) this.table=""; else this.table=table;
	}

	/**
	 * Liefert die 1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen.
	 * @return	1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen
	 */
	@Override
	public int getStartRow() {
		if (startRow<1) return 1;
		return startRow;
	}

	/**
	 * Stellt die 1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen ein.
	 * @param startRow	1-basierende Nummer der ersten Zeile aus der Daten ausgelesen werden sollen
	 */
	@Override
	public void setStartRow(final int startRow) {
		if (startRow>=1) this.startRow=startRow;
	}

	/**
	 * Liefert den Namen der Spalte aus der die Daten ausgelesen werden sollen.
	 * @return	Name der Spalte aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public String getColumn() {
		if (column==null || column.trim().isEmpty()) return "A"; else return column;
	}

	/**
	 * Stellt den Namen der Spalte aus der die Daten ausgelesen werden sollen ein.
	 * @param column	Name der Spalte aus der die Daten ausgelesen werden sollen
	 */
	@Override
	public void setColumn(final String column) {
		if (column==null) this.column="A"; else this.column=column;
	}

	/**
	 * Listet die Kundentypnamen auf, die in der Tabelle verwendet werden sollen
	 * @return	Kundentypennamen, die in der Tabelle vorkommen
	 */
	public List<String> getClientTypeNames() {
		return clientTypeNames;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSourceDDE)) return false;
		final ModelElementSourceDDE otherSource=(ModelElementSourceDDE)element;

		if (connection==null) {
			if (otherSource.connection!=null) return false;
		} else {
			if (otherSource.connection==null) return false;
			if (connection.getId()!=otherSource.connection.getId()) return false;
		}

		if (!Objects.equals(workbook,otherSource.workbook)) return false;
		if (!Objects.equals(table,otherSource.table)) return false;
		if (startRow!=otherSource.startRow) return false;
		if (!Objects.equals(column,otherSource.column)) return false;

		if (clientTypeNames.size()!=((ModelElementSourceDDE)element).clientTypeNames.size()) return false;
		for (int i=0;i<clientTypeNames.size();i++) if (!clientTypeNames.get(i).equals(((ModelElementSourceDDE)element).clientTypeNames.get(i))) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSourceDDE) {
			final ModelElementSourceDDE source=(ModelElementSourceDDE)element;
			if (source.connection!=null) connectionId=source.connection.getId();

			workbook=source.workbook;
			table=source.table;
			startRow=source.startRow;
			column=source.column;

			clientTypeNames.clear();
			clientTypeNames.addAll(source.clientTypeNames);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSourceDDE clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSourceDDE element=new ModelElementSourceDDE(model,surface);
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
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		if (connection==null) return Language.tr("Surface.SourceDDE.Name")+" - "+Language.tr("Surface.SourceDDE.NotConnected");
		return Language.tr("Surface.SourceDDE.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.SourceDDE.Name");
	}

	private static final Color defaultBackgroundColor=new Color(180,255,180);

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
			new ModelElementSourceDDEDialog(owner,ModelElementSourceDDE.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Laufzeitstatistik hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugefügt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
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
		NextStationHelper.nextStationsSource(this,parentMenu,addNextStation);
	}

	/**
	 * Fügt optional weitere Einträge zum Kontextmenü hinzu
	 * @param owner	Übergeordnetes Element
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 */
	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final boolean readOnly) {
		JMenuItem item;
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();

		if (connection!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdge")));
			item.addActionListener((e)->surface.remove(connection));
			if (imgURL!=null) item.setIcon(new ImageIcon(imgURL));
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
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.SourceDDE.XML.Root");
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

		if (connection!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+connection.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Out"));
		}

		if (workbook!=null && !workbook.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDDE.XML.Workbook")));
			sub.setTextContent(workbook);
		}

		if (table!=null && !table.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDDE.XML.Table")));
			sub.setTextContent(table);
		}

		if (startRow>=1) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDDE.XML.StartRow")));
			sub.setTextContent(""+startRow);
		}

		if (column!=null && !column.trim().isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDDE.XML.Column")));
			sub.setTextContent(column);
		}

		for (String clientTypeName : clientTypeNames) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceDDE.XML.ClientTypeName")));
			sub.setTextContent(clientTypeName);
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

		if (Language.trAll("Surface.XML.Connection",name) && Language.trAll("Surface.XML.Connection.Type.Out",Language.trAllAttribute("Surface.XML.Connection.Type", node))) {
			final String s=Language.trAllAttribute("Surface.XML.Connection.Element",node);
			if (!s.isEmpty()) {
				final Integer I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
				connectionId=I;
			}
			return null;
		}

		if (Language.trAll("Surface.SourceDDE.XML.Workbook",name)) {
			workbook=content;
			return null;
		}

		if (Language.trAll("Surface.SourceDDE.XML.Table",name)) {
			table=content;
			return null;
		}

		if (Language.trAll("Surface.SourceDDE.XML.StartRow",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null || I<1) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			startRow=I.intValue();
			return null;
		}

		if (Language.trAll("Surface.SourceDDE.XML.Column",name)) {
			column=content;
			return null;
		}

		if (Language.trAll("Surface.SourceDDE.XML.ClientTypeName",name)) {
			clientTypeNames.add(content);
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeOut() {
		return connection==null;
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
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
		return clientTypeNames.toArray(new String[0]);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSourceDDE";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		descriptionBuilder.addEdgeOut(getEdgeOut());

		if (workbook!=null && !workbook.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDDE.Workbook"),table,1000);
		if (table!=null && !table.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDDE.Table"),table,2000);
		if (startRow>0) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDDE.StartRow"),""+startRow,3000);
		if (column!=null && !column.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceDDE.Column"),table,4000);

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
		return DataCheckResult.checkDDE(this,workbook,table);
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
		findEdgesTo(new Class[]{ModelElementProcess.class,ModelElementDelay.class},fixer);
	}
}
