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
import ui.modeleditor.coreelements.DataCheckResult;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Kundenquelle, die die konkreten Ank�nfte aus einer Tabelle l�dt
 * @author Alexander Herzog
 */
public class ModelElementSourceTable extends ModelElementBox implements ElementWithNewClientNames, ModelElementEdgeOut, ElementNoRemoteSimulation, ElementWithInputFile {
	private ModelElementEdge connection;

	/* Wird nur beim Laden und Clonen verwendet. */
	private int connectionId=-1;

	private String tableFileName="";

	private boolean numbersAreDistances=false;

	private final List<String> clientTypeNames=new ArrayList<>();

	/**
	 * Konstruktor der Klasse <code>ModelElementSourceTable</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementSourceTable(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ARROW_RIGHT);
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SOURCE_TABLE.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.SourceTable.Tooltip");
	}

	/**
	 * Muss aufgerufen werden, wenn sich eine Eigenschaft des Elements �ndert.
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
	 * Liefert den gew�hlten Namen der Tabelle, aus der die Ank�nfte geladen werden sollen
	 * @return	Tabelle, aus der die Ank�nfte geladen werden
	 */
	@Override
	public String getInputFile() {
		return tableFileName;
	}

	/**
	 * Stellt die Tabelle ein, aus der die Ank�nfte geladen werden sollen
	 * @param inputFile	Tabelle, aus der die Ank�nfte geladen werden
	 */
	@Override
	public void setInputFile(final String inputFile) {
		if (inputFile!=null) {
			this.tableFileName=inputFile.trim();
			fireChanged();
		}
	}

	/**
	 * Listet die Kundentypnamen auf, die in der Tabelle verwendet werden sollen
	 * @return	Kundentypennamen, die in der Tabelle vorkommen
	 */
	public List<String> getClientTypeNames() {
		return clientTypeNames;
	}

	/**
	 * Gibt an, ob die Zahlen in Spalte 1 absolute Zeitangaben oder Abst�nde
	 * vom jeweils vorherigen Wert sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Zeitangaben in Spalte 1 der Tabelle Abst�nde sind
	 */
	public boolean isNumbersAreDistances() {
		return numbersAreDistances;
	}

	/**
	 * Stellt ein, ob die Zahlen in Spalte 1 absolute Zeitangaben oder Abst�nde
	 * vom jeweils vorherigen Wert sind.
	 * @param numbersAreDistances	Wird <code>true</code> angegeben, so werden die Zeitangaben in Spalte 1 der Tabelle als Abst�nde interpretiert
	 */
	public void setNumbersAreDistances(final boolean numbersAreDistances) {
		this.numbersAreDistances=numbersAreDistances;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSourceTable)) return false;

		if (connection==null) {
			if (((ModelElementSourceTable)element).connection!=null) return false;
		} else {
			if (((ModelElementSourceTable)element).connection==null) return false;
			if (connection.getId()!=((ModelElementSourceTable)element).connection.getId()) return false;
		}

		if (!tableFileName.equals(((ModelElementSourceTable)element).tableFileName)) return false;
		if (numbersAreDistances!=((ModelElementSourceTable)element).numbersAreDistances) return false;
		if (clientTypeNames.size()!=((ModelElementSourceTable)element).clientTypeNames.size()) return false;
		for (int i=0;i<clientTypeNames.size();i++) if (!clientTypeNames.get(i).equals(((ModelElementSourceTable)element).clientTypeNames.get(i))) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSourceTable) {
			if (((ModelElementSourceTable)element).connection!=null) connectionId=((ModelElementSourceTable)element).connection.getId();
			tableFileName=((ModelElementSourceTable)element).tableFileName;
			numbersAreDistances=((ModelElementSourceTable)element).numbersAreDistances;
			clientTypeNames.clear();
			clientTypeNames.addAll(((ModelElementSourceTable)element).clientTypeNames);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSourceTable clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSourceTable element=new ModelElementSourceTable(model,surface);
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
		if (connection==null) return Language.tr("Surface.SourceTable.Name")+" - "+Language.tr("Surface.SourceTable.NotConnected");
		return Language.tr("Surface.SourceTable.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.SourceTable.Name");
	}

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
			new ModelElementSourceTableDialog(owner,ModelElementSourceTable.this,readOnly);
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
		final URL imgURL=Images.EDIT_EDGES_DELETE.getURL();

		if (connection!=null) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdge")));
			item.addActionListener(e->surface.remove(connection));
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
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.SourceTable.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceTable.XML.TableFileName")));
		sub.setTextContent(tableFileName);
		if (numbersAreDistances) sub.setAttribute(Language.trPrimary("Surface.SourceTable.XML.NumbersAre"),Language.trPrimary("Surface.SourceTable.XML.NumbersAre.Distances"));

		for (String clientTypeName : clientTypeNames) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SourceTable.XML.ClientTypeName")));
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

		if (Language.trAll("Surface.SourceTable.XML.TableFileName",name)) {
			tableFileName=content.trim();
			final String numbersAre=Language.trAllAttribute("Surface.SourceTable.XML.NumbersAre",node);
			if (Language.trAll("Surface.SourceTable.XML.NumbersAre.Distances",numbersAre)) numbersAreDistances=true;
			return null;
		}

		if (Language.trAll("Surface.SourceTable.XML.ClientTypeName",name)) {
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
		return clientTypeNames.toArray(new String[0]);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSourceTable";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		descriptionBuilder.addEdgeOut(getEdgeOut());

		if (tableFileName!=null && !tableFileName.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.SourceTable.Table"),tableFileName,1000);
		}

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
		return DataCheckResult.checkFile(this,tableFileName);
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsIn.size()>0) return false;
		if (connectionsOut.size()!=1) return false;

		this.connection=connectionsOut.get(0);

		return true;
	}
}