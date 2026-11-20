/**
 * Copyright 2026 Alexander Herzog
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
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurface.TimeBase;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInDoubleOutBox;
import ui.modeleditor.coreelements.QuickFixNextElements;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Leitet Kunden mit einer ID, die die Station
 * bereits durchlaufen hat, aus.
 * @author Alexander Herzog
 */
public class ModelElementDropDuplicates extends ModelElementMultiInDoubleOutBox implements ModelElementAnimationForceMove {
	/**
	 * Index des Kundendatenfeldes über das der Abgleich erfolgen soll
	 */
	private int clientDataIndex;

	/**
	 * Für wie lange sollen die IDs gespeichert werden?
	 */
	private double storageTime;

	/**
	 * Verwendete Zeitbasis (ob die Speicherdauern Sekunden-, Minuten- oder Stunden-Angaben sein sollen)
	 * @see #getTimeBase()
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Konstruktor der Klasse <code>ModelElementDelay</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementDropDuplicates(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_OCTAGON_DOUBLE_LINE);
		clientDataIndex=0;
		storageTime=1;
		timeBase=ModelSurface.TimeBase.TIMEBASE_DAYS;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_DROP_DUPLICATES.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.DropDuplicates.Tooltip");
	}

	/**
	 * Aktualisiert die Beschriftung der auslaufenden Kante
	 * @see #fireChanged()
	 */
	@Override
	protected void updateEdgeLabel() {
		if (connectionOut==null) return; /* Wenn die Methode vom Eltern-Konstruktor aufgerufen wird. */
		if (connectionOut[0]!=null) {
			connectionOut[0].setName("");
		}
		if (connectionOut[1]!=null) {
			connectionOut[1].setName(Language.tr("Surface.DropDuplicates.DuplicatesEdge"));
		}
	}

	/**
	 * Liefert den Index des Kundendatenfeldes über das der Abgleich erfolgen soll.
	 * @return	Index des Kundendatenfeldes über das der Abgleich erfolgen soll
	 * @see #setClientDataIndex(int)
	 */
	public int getClientDataIndex() {
		return clientDataIndex;
	}

	/**
	 * Stellt den Index des Kundendatenfeldes über das der Abgleich erfolgen soll ein.
	 * @param clientDataIndex	Index des Kundendatenfeldes über das der Abgleich erfolgen soll
	 * @see #getClientDataIndex()
	 */
	public void setClientDataIndex(int clientDataIndex) {
		this.clientDataIndex=Math.max(0,clientDataIndex);
	}

	/**
	 * Für wie lange sollen die IDs gespeichert werden?
	 * @return	Für wie lange sollen die IDs gespeichert werden?
	 * @see #setStorageTime(double)
	 */
	public double getStorageTime() {
		return storageTime;
	}

	/**
	 * Stellt ein für wie lange sollen die IDs gespeichert werden sollen.
	 * @param storageTime	Für wie lange sollen die IDs gespeichert werden?
	 * @see #getStorageTime()
	 */
	public void setStorageTime(double storageTime) {
		this.storageTime=Math.max(0,storageTime);
	}

	/**
	 * Liefert die verwendete Zeitbasis für die Speicherzeitdauern.
	 * @return	Verwendete Zeitbasis
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 * @see #getStorageTime()
	 * @see #setStorageTime(double)
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis für die Speicherzeitdauern ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 * @see #getTimeBase()
	 * @see #getStorageTime()
	 * @see #setStorageTime(double)
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		this.timeBase=(timeBase==null)?ModelSurface.TimeBase.TIMEBASE_DAYS:timeBase;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementDropDuplicates)) return false;
		final ModelElementDropDuplicates otherDropDuplicates=(ModelElementDropDuplicates)element;

		if (otherDropDuplicates.clientDataIndex!=clientDataIndex) return false;
		if (otherDropDuplicates.storageTime!=storageTime) return false;
		if (otherDropDuplicates.timeBase!=timeBase) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementDropDuplicates) {
			final ModelElementDropDuplicates copySource=(ModelElementDropDuplicates)element;

			clientDataIndex=copySource.clientDataIndex;
			storageTime=copySource.storageTime;
			timeBase=copySource.timeBase;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementDropDuplicates clone(final EditModel model, final ModelSurface surface) {
		final ModelElementDropDuplicates element=new ModelElementDropDuplicates(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.DropDuplicates.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.DropDuplicates.Name");
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
			new ModelElementDropDuplicatesDialog(owner,ModelElementDropDuplicates.this,readOnly);
		};
	}

	/**
	 * Fügt optionale Menüpunkte zu einem "Folgestation hinzufügen"-Untermenü hinzu, welche
	 * es ermöglichen, zu dem aktuellen Element passende Folgestationen hinzuzufügen.
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	@Override
	protected void addNextStationContextMenuItems(final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		NextStationHelper.nextStationsProcessing(this,parentMenu,addNextStation);
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
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.DropDuplicates.XML.Root");
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

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DropDuplicates.XML.ClientsDataIndex")));
		sub.setTextContent(""+clientDataIndex);

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DropDuplicates.XML.StorageTime")));
		sub.setTextContent(NumberTools.formatSystemNumber(storageTime));
		sub.setAttribute(Language.trPrimary("Surface.DropDuplicates.XML.StorageTimeTimeBase"),timeBase.getName());
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

		if (Language.trAll("Surface.DropDuplicates.XML.ClientsDataIndex",name)) {
			final Integer I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			clientDataIndex=I;
			return null;
		}

		if (Language.trAll("Surface.DropDuplicates.XML.StorageTime",name)) {
			final Double D=NumberTools.getPositiveDouble(content);
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			storageTime=D;
			timeBase=TimeBase.byName(Language.trAllAttribute("Surface.DropDuplicates.XML.StorageTimeTimeBase",node));
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementDropDuplicates";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Kundedatenfeld-Index */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.DropDuplicates.ClientDataIndex"),""+clientDataIndex,1000);

		/* Speicherzeitdauer */
		descriptionBuilder.addProperty(Language.tr("ModelDescription.DropDuplicates.StorageTime"),NumberTools.formatNumberMax(storageTime)+" "+timeBase.getName(),2000);
	}

	@Override
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
		findEdgesTo(QuickFixNextElements.process,fixer);
	}
}
