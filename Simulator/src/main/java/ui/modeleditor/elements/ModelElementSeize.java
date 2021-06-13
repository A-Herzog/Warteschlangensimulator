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
import java.util.Map;
import java.util.TreeMap;
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
import ui.modeleditor.ModelDataRenameListener;
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
 * Belegt Ressourcen, wenn Kunden die Station passieren bzw. verz�gert diese, bis Ressourcen verf�gbar sind
 * @author Alexander Herzog
 * @see ModelElementRelease
 */
public class ModelElementSeize extends ModelElementMultiInSingleOutBox implements ModelDataRenameListener, ModelElementAnimationForceMove {
	/**
	 * Bedienergruppen und deren Anzahlen, die f�r die Bedienung der Kunden notwendig sind
	 * @see #getNeededResources()
	 */
	private Map<String,Integer> resources; /* Name der Ressource, ben�tigte Anzahl */

	/**
	 * Ressourcen-Priorisierungs-Formel
	 * @see #getResourcePriority()
	 * @see #setResourcePriority(String)
	 */
	private String resourcePriority;

	/**
	 * Konstruktor der Klasse <code>ModelElementSeize</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementSeize(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);

		resources=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		resourcePriority=ModelElementProcess.DEFAULT_RESOURCE_PRIORITY;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SEIZE.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Seize.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSeize)) return false;

		final ModelElementSeize seize=(ModelElementSeize)element;

		Map<String,Integer> resourcesA=resources;
		Map<String,Integer> resourcesB=seize.resources;
		for (Map.Entry<String,Integer> entry : resourcesA.entrySet()) {
			if (!entry.getValue().equals(resourcesB.get(entry.getKey()))) return false;
		}
		for (Map.Entry<String,Integer> entry : resourcesB.entrySet()) {
			if (!entry.getValue().equals(resourcesA.get(entry.getKey()))) return false;
		}
		if (!resourcePriority.equalsIgnoreCase(seize.resourcePriority)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSeize) {

			final ModelElementSeize seize=(ModelElementSeize)element;

			resources.clear();
			for (Map.Entry<String,Integer> entry: seize.resources.entrySet()) resources.put(entry.getKey(),entry.getValue());
			resourcePriority=seize.resourcePriority;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSeize clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSeize element=new ModelElementSeize(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
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
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=new Color(64,127,255);

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
			new ModelElementSeizeDialog(owner,ModelElementSeize.this,readOnly);
		};
	}

	/**
	 * F�gt optionale Men�punkte zu einem "Visualisierungen hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen zu dem aktuellen Element direkt passende Animationselemente hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Popupmen�s, welches die Eintr�ge aufnimmt
	 * @param addElement	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
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
	 * F�gt optionale Men�punkte zu einem "Folgestation hinzuf�gen"-Untermen� hinzu, welche
	 * es erm�glichen, zu dem aktuellen Element passende Folgestationen hinzuzuf�gen.
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsHold(this,parentMenu,addNextStation);
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
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Seize.XML.Root");
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

		for (Map.Entry<String,Integer> entry : resources.entrySet()) if (entry.getValue()!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Seize.XML.Operators")));
			sub.setAttribute(Language.trPrimary("Surface.Seize.XML.Operators.Group"),entry.getKey());
			sub.setAttribute(Language.trPrimary("Surface.Seize.XML.Operators.Count"),""+entry.getValue());
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Seize.XML.Priority")));
		sub.setTextContent(resourcePriority);
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
		}

		return null;
	}

	/**
	 * Liefert die Aufstellung der zur Bearbeitung von Kunden ben�tigten Ressourcen
	 * @return	Bedienergruppen und deren Anzahlen, die f�r die Bedienung der Kunden notwendig sind
	 */
	public Map<String,Integer> getNeededResources() {
		return resources;
	}

	/**
	 * Liefert die Formel zur�ck, gem�� derer die Priorisierung der Bedienstation bei der Zuweisung von verf�gbaren Ressourcen erfolgen soll.
	 * @return	Aktuelle Ressourcen-Priorisierungs-Formel
	 */
	public String getResourcePriority() {
		return resourcePriority;
	}

	/**
	 * Stellt die Formel ein, gem�� derer die Priorisierung der Bedienstation bei der Zuweisung von verf�gbaren Ressourcen erfolgen soll.
	 * @param newResourcePriority Neue Ressourcen-Priorisierungs-Formel
	 */
	public void setResourcePriority(final String newResourcePriority) {
		if (newResourcePriority==null || newResourcePriority.trim().isEmpty()) return;
		resourcePriority=newResourcePriority;
	}

	/**
	 * Gibt an, ob nur die Anzahl an Kunden, die diese Station passiert haben
	 * oder aber zus�tzlich auch die aktuelle Anzahl an Kunden an der Station
	 * w�hrend der Animation angezeigt werden sollen.
	 * @return	Nur Gesamtanzahl (<code>false</code>) oder Gesamtanzahl und aktueller Wert (<code>true</code>)
	 */
	@Override
	public boolean showFullAnimationRunData() {
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
	 * Erstellt eine Beschreibung f�r das aktuelle Element
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

		/* Ressourcenpriorit�t */
		if (resourcePriority!=null && !resourcePriority.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Seize.ResourcePriority"),resourcePriority,2000);
		} else {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Seize.ResourcePriority"),ModelElementProcess.DEFAULT_RESOURCE_PRIORITY,2000);
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		/* Ressourcenzuordnung -> keine Suche */

		searcher.testString(this,Language.tr("Surface.Seize.Dialog.ResourcePriority"),resourcePriority,newResourcePriority->{resourcePriority=newResourcePriority;});
	}
}