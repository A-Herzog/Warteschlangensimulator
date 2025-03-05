/**
 * Copyright 2021 Alexander Herzog
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
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Kundenausgang mit Speicherung der Kundendaten
 * @author Alexander Herzog
 */
public class ModelElementDisposeWithTable extends ModelElementBox implements ModelElementEdgeMultiIn, ElementWithOutputFile, ElementNoRemoteSimulation {
	/** Liste der einlaufenden Kanten */
	private List<ModelElementEdge> connections;

	/**
	 * Liste der IDs der einlaufenden Kanten (wird nur beim Laden und Clonen verwendet, ist sonst <code>null</code>)
	 * @see #connections
	 */
	private List<Integer> connectionIds=null;

	/**
	 * Simulation abbrechen, wenn an dieser Station ein Kunde eintrifft?
	 * @see #isStoppSimulationOnClientArrival()
	 * @see #setStoppSimulationOnClientArrival(boolean)
	 */
	private boolean stoppSimulationOnClientArrival;

	/**
	 * Tabellendatei zum Speichern der Kunden
	 * @see #getOutputFile()
	 * @see #setOutputFile(String)
	 */
	public String clientsOutputTable;

	/**
	 * Ist die Ausgabe als Ganzes aktiv?
	 * @see #isOutputActive()
	 * @see #setOutputActive(boolean)
	 */
	private boolean outputActive;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementDisposeWithTable(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ARROW_LEFT);
		connections=new ArrayList<>();
		stoppSimulationOnClientArrival=false;
		clientsOutputTable="";
		outputActive=true;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DISPOSE.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		if (stoppSimulationOnClientArrival) {
			return Language.tr("Surface.DisposeWithTable.TooltipStopp");
		} else {
			return Language.tr("Surface.DisposeWithTable.Tooltip");
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
		if (!(element instanceof ModelElementDisposeWithTable)) return false;
		final ModelElementDisposeWithTable otherDispose=(ModelElementDisposeWithTable)element;

		final List<ModelElementEdge> connections2=otherDispose.connections;
		if (connections==null || connections2==null || connections.size()!=connections2.size()) return false;
		for (int i=0;i<connections.size();i++) if (connections.get(i).getId()!=connections2.get(i).getId()) return false;

		if (stoppSimulationOnClientArrival!=otherDispose.stoppSimulationOnClientArrival) return false;
		if (!clientsOutputTable.equals(otherDispose.clientsOutputTable)) return false;
		if (outputActive!=otherDispose.outputActive) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDisposeWithTable) {
			final ModelElementDisposeWithTable copySource=(ModelElementDisposeWithTable)element;

			connections.clear();
			final List<ModelElementEdge> connections2=copySource.connections;
			if (connections2!=null) {
				connectionIds=new ArrayList<>();
				for (int i=0;i<connections2.size();i++) connectionIds.add(connections2.get(i).getId());
			}

			stoppSimulationOnClientArrival=copySource.stoppSimulationOnClientArrival;
			clientsOutputTable=copySource.clientsOutputTable;
			outputActive=copySource.outputActive;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDisposeWithTable clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDisposeWithTable element=new ModelElementDisposeWithTable(model,surface);
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
		if (stoppSimulationOnClientArrival) {
			return Language.tr("Surface.DisposeWithTable.NameStopp");
		} else {
			return Language.tr("Surface.DisposeWithTable.Name");
		}
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		if (stoppSimulationOnClientArrival) {
			return Language.tr("Surface.DisposeWithTable.NameStopp");
		} else {
			return Language.tr("Surface.DisposeWithTable.Name");
		}
	}

	/**
	 * Liefert optional eine zusätzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zusätzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null || outputActive) return null;
		return Language.tr("Surface.Output.Disabled");
	}

	/**
	 * Vorgabe-Hintergrundfarbe für die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,148,148);

	/**
	 * Liefert die Vorgabe-Hintergrundfarbe für die Box
	 * @return	Vorgabe-Hintergrundfarbe für die Box
	 */
	@Override
	public Color getTypeDefaultBackgroundColor() {
		return defaultBackgroundColor;
	}

	/**
	 * Gibt an, ob die Simulation abgebrochen werden soll, wenn an dieser Station ein Kunde eintrifft.
	 * @return	Liefert <code>true</code> wenn die Simulation abgebrochen werden soll, wenn an dieser Station ein Kunde eintrifft.
	 */
	public boolean isStoppSimulationOnClientArrival() {
		return stoppSimulationOnClientArrival;
	}

	/**
	 * Stellt ein, ob die Simulation abgebrochen werden soll, wenn an dieser Station ein Kunde eintrifft.
	 * @param stoppSimulationOnClientArrival	Wird <code>true</code> angegeben, so wird die Simulation abgebrochen, wenn an dieser Station ein Kunde eintrifft.
	 */
	public void setStoppSimulationOnClientArrival(final boolean stoppSimulationOnClientArrival) {
		this.stoppSimulationOnClientArrival=stoppSimulationOnClientArrival;
		fireChanged();
	}

	/**
	 * Liefert die Tabellendatei zum Speichern der Kunden, die das System verlassen.
	 * @return	Tabellendatei zum Speichern der Kunden, die das System verlassen (leer für "keine Speicherung")
	 */
	@Override
	public String getOutputFile() {
		return clientsOutputTable;
	}

	/**
	 * Stellt die Tabellendatei zum Speichern der Kunden, die das System verlassen, ein.
	 * @param clientsOutputTable	Tabellendatei zum Speichern der Kunden, die das System verlassen (leer für "keine Speicherung")
	 */
	@Override
	public void setOutputFile(final String clientsOutputTable) {
		this.clientsOutputTable=(clientsOutputTable==null)?"":clientsOutputTable;
	}

	/**
	 * Ist die Ausgabe als Ganzes aktiv?
	 * @return	Ausgabe aktiv
	 * @see #setOutputActive(boolean)
	 */
	@Override
	public boolean isOutputActive() {
		return outputActive;
	}

	/**
	 * Stellt ein, ob die Ausgabe aktiv sein soll.
	 * @param outputActive	Ausgabe aktiv
	 * @see #isOutputActive()
	 */
	@Override
	public void setOutputActive(boolean outputActive) {
		this.outputActive=outputActive;
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
			new ModelElementDisposeWithTableDialog(owner,ModelElementDisposeWithTable.this,readOnly);
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
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_IN);
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
		addEdgesInContextMenu(popupMenu,surface,readOnly);
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
		return Language.trAll("Surface.DisposeWithTable.XML.Root");
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

		if (!outputActive) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DisposeWithTable.XML.Active")));
			sub.setTextContent("0");
		}

		if (stoppSimulationOnClientArrival) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.DisposeWithTable.Stopp")));
			sub.setTextContent("1");
		}

		if (!clientsOutputTable.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.DisposeWithTable.Table")));
			sub.setTextContent(clientsOutputTable);
		}

		if (connections!=null) for (ModelElementEdge element: connections) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
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

		if (Language.trAll("Surface.DisposeWithTable.XML.Active",name)) {
			outputActive=!content.equals("0");
			return null;
		}

		if (Language.trAll("Surface.XML.DisposeWithTable.Stopp",name)) {
			stoppSimulationOnClientArrival=content.equals("1");
			return null;
		}

		if (Language.trAll("Surface.XML.DisposeWithTable.Table",name)) {
			clientsOutputTable=content;
			return null;
		}

		if (Language.trAll("Surface.XML.Connection",name) && Language.trAll("Surface.XML.Connection.Type.In",Language.trAllAttribute("Surface.XML.Connection.Type",node))) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			if (connectionIds==null) connectionIds=new ArrayList<>();
			connectionIds.add(I);
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
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	@Override
	public ModelElementEdge[] getEdgesIn() {
		return connections.toArray(ModelElementEdge[]::new);
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
	public String getHelpPageName() {
		return "ModelElementDisposeWithTable";
	}

	@Override
	public boolean setReferenceEdges(List<ModelElementEdge> connectionsIn, List<ModelElementEdge> connectionsOut) {
		if (connectionsOut.size()>0) return false;

		this.connections.clear();
		this.connections.addAll(connectionsIn);

		return true;
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!clientsOutputTable.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Output.File"),clientsOutputTable,3000);
		}

		if (!outputActive) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Output.Active"),Language.tr("ModelDescription.Output.Active.Off"),10000);
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Tabellendateiname */
		searcher.testString(this,Language.tr("Editor.DialogBase.Search.TableFileName"),clientsOutputTable,newClientsOutputTable->{clientsOutputTable=newClientsOutputTable;});
	}
}
