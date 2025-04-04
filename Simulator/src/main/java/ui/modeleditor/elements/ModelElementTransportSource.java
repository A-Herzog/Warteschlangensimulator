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
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
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
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Routing Startpunkt
 * @author Alexander Herzog
 */
public class ModelElementTransportSource extends ModelElementBox implements ModelDataRenameListener, ModelElementEdgeMultiIn, ModelElementAnimationForceMove, ModelDataResourceUsage {
	/**
	 * Einlaufende Kanten
	 */
	private List<ModelElementEdge> connections;

	/**
	 * IDs der einlaufenden Kanten (wird nur beim Laden und Clonen verwendet)
	 */
	private List<Integer> connectionIds=null;

	/**
	 * Datensatz zu den Transportzeiten
	 * @see #getTransportTimeRecord()
	 */
	private TransportTimeRecord transportTimeRecord;

	/**
	 * Einstellungen-Objekt mit den Daten zu den Transportzielen
	 * @see #getTransportTargetSystem()
	 */
	private TransportTargetSystem transportTargetSystem;

	/**
	 * Objekt mit den Einstellungen zur Ressourcennutzung f�r den Transport
	 * @see #getTransportResourceRecord()
	 */
	private TransportResourceRecord transportResourceRecord;

	/**
	 * Namen der zugeh�rigen "Bereich betreten"-Station
	 * (der jeweilige Bereich wird dann verlassen, wenn ein Kunde �ber diese Station transportiert wird)
	 * @see #getSectionStartName()
	 */
	private String sectionStartName;

	/**
	 * Konstruktor der Klasse <code>ModelElementTransportSource</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementTransportSource(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);
		connections=new ArrayList<>();
		transportTimeRecord=new TransportTimeRecord();
		transportTargetSystem=new TransportTargetSystem();
		transportResourceRecord=new TransportResourceRecord();
		sectionStartName="";
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_TRANSPORT_SOURCE.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.TransportSource.Tooltip");
	}

	/**
	 * Liefert den Datensatz zu den Transportzeiten
	 * @return	Datensatz zu den Transportzeiten
	 */
	public TransportTimeRecord getTransportTimeRecord() {
		return transportTimeRecord;
	}

	/**
	 * Liefert das Einstellungen-Objekt mit den Daten zu den Transportzielen
	 * @return	Einstellungen-Objekt mit den Daten zu den Transportzielen
	 */
	public TransportTargetSystem getTransportTargetSystem() {
		return transportTargetSystem;
	}

	/**
	 * Liefert das Objekt mit den Einstellungen zur Ressourcennutzung f�r den Transport
	 * @return	Objekt mit den Einstellungen zur Ressourcennutzung f�r den Transport
	 */
	public TransportResourceRecord getTransportResourceRecord() {
		return transportResourceRecord;
	}

	/**
	 * Liefert den Namen der zugeh�rigen "Bereich betreten"-Station.
	 * @return	Namen der zugeh�rigen "Bereich betreten"-Station
	 */
	public String getSectionStartName() {
		return sectionStartName;
	}

	/**
	 * Stellt den Namen der zugeh�rigen "Bereich betreten"-Station ein.
	 * @param sectionStartName	Namen der zugeh�rigen "Bereich betreten"-Station
	 */
	public void setSectionStartName(final String sectionStartName) {
		if (sectionStartName==null) this.sectionStartName=""; else this.sectionStartName=sectionStartName;
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementTransportSource)) return false;

		final List<ModelElementEdge> connections2=((ModelElementTransportSource)element).connections;
		if (connections==null || connections2==null || connections.size()!=connections2.size()) return false;
		for (int i=0;i<connections.size();i++) if (connections.get(i).getId()!=connections2.get(i).getId()) return false;

		if (!transportTimeRecord.equalsTransportTimeRecord(((ModelElementTransportSource)element).transportTimeRecord)) return false;

		if (!transportTargetSystem.equalsTransportTargetSystem(((ModelElementTransportSource)element).transportTargetSystem)) return false;

		if (!transportResourceRecord.equalsTransportResourceRecord(((ModelElementTransportSource)element).transportResourceRecord)) return false;

		if (!sectionStartName.equals(((ModelElementTransportSource)element).sectionStartName)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementTransportSource) {
			connections.clear();
			final List<ModelElementEdge> connections2=((ModelElementTransportSource)element).connections;
			if (connections2==null) return;
			connectionIds=new ArrayList<>();
			for (int i=0;i<connections2.size();i++) connectionIds.add(connections2.get(i).getId());

			transportTimeRecord=((ModelElementTransportSource)element).transportTimeRecord.clone();
			transportTargetSystem=((ModelElementTransportSource)element).transportTargetSystem.clone();
			transportResourceRecord=((ModelElementTransportSource)element).transportResourceRecord.clone();

			sectionStartName=((ModelElementTransportSource)element).sectionStartName;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementTransportSource clone(final EditModel model, final ModelSurface surface) {
		final ModelElementTransportSource element=new ModelElementTransportSource(model,surface);
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
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.TransportSource.Name");
	}

	/**
	 * Liefert einen Fehlertext, der unter der Box angezeigt werden soll.<br>
	 * Ist das Element in Ordnung, so soll <code>null</code> zur�ckgegeben werden.
	 * @return	Optionale Fehlermeldung oder <code>null</code> wenn kein Fehler vorliegt.
	 */
	@Override
	protected String getErrorMessage() {
		final String defaultStation=transportTargetSystem.getDefaultStation();
		if (defaultStation==null || defaultStation.isBlank()) return Language.tr("Surface.ErrorInfo.NoDefaultDestination");

		return null;
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.TransportSource.Name");
	}

	/**
	 * Liefert optional eine zus�tzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zus�tzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		if (surface==null) return null;
		switch (transportTargetSystem.getMode()) {
		case ROUTING_MODE_EXPLICITE: return Language.tr("Surface.TransportSource.RouteBy.Settings");
		case ROUTING_MODE_SEQUENCE: return Language.tr("Surface.TransportSource.RouteBy.Sequence");
		case ROUTING_MODE_TEXT_PROPERTY: return Language.tr("Surface.TransportSource.RouteBy.Property");
		default: return null;
		}
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(255,212,212);

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
			new ModelElementTransportSourceDialog(owner,ModelElementTransportSource.this,readOnly);
		};
	}

	/**
	 * Erstellte optional weitere Men�punkte (in Form von Panels),
	 * die das direkte Bearbeiten von Einstellungen aus dem
	 * Kontextmen� heraus erlauben.
	 * @return	Array mit Panels (Array kann leer oder <code>null</code> sein; auch Eintr�ge d�rfen <code>null</code> sein)
	 */
	@Override
	protected JPanel[] addCustomSettingsToContextMenu() {
		final List<JPanel> panels=new ArrayList<>();

		if (transportTimeRecord.getTransportTime().get() instanceof AbstractRealDistribution) {
			final AbstractRealDistribution distribution=(AbstractRealDistribution)transportTimeRecord.getTransportTime().get();
			final Consumer<AbstractRealDistribution> distributionChanger=newDistribution->transportTimeRecord.getTransportTime().set(newDistribution);
			panels.add(createContextMenuSliderDistributionMean(Language.tr("Surface.TransportSource.AverageTransportTimeGlobal"),transportTimeRecord.getTimeBase(),distribution,300,distributionChanger));
		}

		return panels.toArray(JPanel[]::new);
	}


	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElements	Callback, das aufgerufen werden kann, wenn Elemente zur Zeichenfl�che hinzugef�gt werden sollen
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
	 * F�gt optionale Men�punkte zu einem "Laufzeitstatistik hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element direkt passende Statistikdaten im Modell hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addLongRunStatistics	Callback, das aufgerufen werden kann, wenn ein Eintrag hinzugef�gt werden soll
	 */
	@Override
	protected void addLongRunStatisticsContextMenuItems(final JMenu parentMenu, final Consumer<String> addLongRunStatistics) {
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.WIP);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_IN);
		addLongRunStatisticsMenuItem(parentMenu,addLongRunStatistics,LongRunStatisticsType.NUMBER_OUT);
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
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.TransportSource.XML.Root");
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

		if (connections!=null) for (ModelElementEdge element: connections) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Connection")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element"),""+element.getId());
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.In"));
		}

		transportTimeRecord.addPropertiesToXML(doc,node);
		transportTargetSystem.addPropertiesToXML(doc,node);
		transportResourceRecord.addPropertiesToXML(doc,node);

		if (sectionStartName!=null && !sectionStartName.isEmpty()) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.TransportSource.XML.SectionStartName")));
			sub.setTextContent(sectionStartName);
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

		if (Language.trAll("Surface.XML.Connection",name) && Language.trAll("Surface.XML.Connection.Type.In",Language.trAllAttribute("Surface.XML.Connection.Type",node))) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(Language.trAllAttribute("Surface.XML.Connection.Element",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element"),name,node.getParentNode().getNodeName());
			if (connectionIds==null) connectionIds=new ArrayList<>();
			connectionIds.add(I);
			return null;
		}

		error=transportTimeRecord.loadProperties(node);
		if (error!=null) return error;

		error=transportTargetSystem.loadProperties(node);
		if (error!=null) return error;

		error=transportResourceRecord.loadProperties(node);
		if (error!=null) return error;

		if (Language.trAll("Surface.TransportSource.XML.SectionStartName",name)) {
			sectionStartName=content;
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	@Override
	public boolean canAddEdgeIn() {
		return true;
	}

	/**
	 * F�gt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzuf�gende Kante
	 * @return	Gibt <code>true</code> zur�ck, wenn die einlaufende Kante hinzugef�gt werden konnte.
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
		return connections.toArray(ModelElementEdge[]::new);
	}

	/**
	 * Gibt an, ob es in das Element einlaufende Kanten gibt.<br><br>
	 * Wenn nicht, kann es in der Simulation �berhaupt nicht erreicht werden und kann daher
	 * bei der Initialisierung �bersprungen werden, d.h. in diesem Fall ist es dann egal,
	 * ob das Element in Bezug auf die Konfiguration fehlerhaft ist, z.B. keine auslaufenden
	 * Kanten hat.<br><br>
	 * Bei Variablenzuweisungen wird die Liste der Zuweisungen dennoch bei der Initialisierung
	 * der Simulation ber�cksichtigt: Es wird so ermittelt, welche Variablennamen in im Modell
	 * vorkommen (d.h. auf diese Variablen kann an anderer Stelle zugegriffen werden, ohne dass
	 * sie noch einmal deklariert werden m�ssten).
	 * @return	Gibt <code>true</code> zur�ck, wenn es mindestens eine in das Element einlaufende
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
		}

		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_TRANSPORT_DESTINATION)) {
			transportTimeRecord.destinationRenamed(oldName,newName);
			transportTargetSystem.destinationRenamed(oldName,newName);
			transportResourceRecord.destinationRenamed(oldName,newName);
		}

		if (isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_RESOURCE)) {
			transportResourceRecord.resourceRenamed(oldName,newName);
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementTransportSource";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Routingzeiten */
		transportTimeRecord.buildDescriptionProperty(descriptionBuilder);

		/* Ressourcen f�r Routing */
		transportResourceRecord.buildDescriptionProperty(descriptionBuilder);

		/* Transportziele */
		transportTargetSystem.buildDescriptionProperty(descriptionBuilder);

		/* Bereich verlassen */
		if (sectionStartName!=null && !sectionStartName.isBlank()) descriptionBuilder.addProperty(Language.tr("ModelDescription.TransportSource.SectionStartName"),sectionStartName,100000);
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

		/* Datensatz zu den Transportzeiten */
		transportTimeRecord.search(searcher,this);

		/* Einstellungen-Objekt mit den Daten zu den Transportzielen */
		transportTargetSystem.search(searcher,this);

		/* Objekt mit den Einstellungen zur Ressourcennutzung f�r den Transport */
		transportResourceRecord.search(searcher,this);

		/* Namen der zugeh�rigen "Bereich betreten"-Station */
		searcher.testString(this,Language.tr("Surface.TransportSource.Dialog.SectionEnd.SectionStart"),sectionStartName,newSectionStartName->{sectionStartName=newSectionStartName;});
	}

	@Override
	public Map<String,Integer> getUsedResourcesInfo() {
		final Map<String,Integer> map=new HashMap<>();
		map.putAll(transportResourceRecord.getResources());
		return map;
	}

	@Override
	public void addResourceUsage(final String resourceName, final int neededNumber) {
		final int currentUsage=transportResourceRecord.getResources().getOrDefault(resourceName,0);
		transportResourceRecord.getResources().put(resourceName,currentUsage+neededNumber);
	}
}