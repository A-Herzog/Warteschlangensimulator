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
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelDataRenameListener;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Belegt Ressourcen, wenn Kunden die Station passieren bzw. verzögert diese, bis Ressourcen verfügbar sind
 * @author Alexander Herzog
 * @see ModelElementSeize
 */
public class ModelElementRelease extends ModelElementMultiInSingleOutBox implements ModelDataRenameListener {
	/**
	 * Zugehöriges Ressourcen-Belegungs-Element
	 * @see #getSeizeName()
	 * @see #setSeizeName(String)
	 */
	private String seizeName;

	/**
	 * Verwendete Zeitbasis
	 * @see #getTimeBase()
	 * @see #setTimeBase(ui.modeleditor.ModelSurface.TimeBase)
	 */
	private ModelSurface.TimeBase timeBase;

	/**
	 * Verzögerte Ressourcenfreigabe
	 * @see #getReleaseDelay()
	 */
	private DistributionSystem releaseDelay;

	/**
	 * Konstruktor der Klasse <code>ModelElementSeize</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementRelease(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);

		seizeName="";
		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;

		/* Um sicher zu stellen, dass die Language-Strings auch in den Sprachdateien vorhanden sind. Der folgende DistributionSystem-Konstruktor ist kein Scan-Ziel für die Sprachdateien. */
		Language.tr("Surface.Release.XML.Distribution.ClientType");

		releaseDelay=new DistributionSystem("Surface.Release.XML.Distribution.ClientType",null,true);
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_RELEASE.getIcon();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Release.Tooltip");
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementRelease)) return false;

		final ModelElementRelease release=(ModelElementRelease)element;

		if (!seizeName.equalsIgnoreCase(release.seizeName)) return false;
		if (timeBase!=release.timeBase) return false;
		if (!releaseDelay.equalsDistributionSystem(release.releaseDelay)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementRelease) {

			final ModelElementRelease release=(ModelElementRelease)element;

			seizeName=release.seizeName;
			timeBase=release.timeBase;
			releaseDelay=release.releaseDelay.clone();
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementRelease clone(final EditModel model, final ModelSurface surface) {
		final ModelElementRelease element=new ModelElementRelease(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Release.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		if (surface==null) return Language.tr("Surface.Release.Name");
		return Language.tr("Surface.Release.Name.Short");
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
			new ModelElementReleaseDialog(owner,ModelElementRelease.this,readOnly);
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
		if (addRemoveEdgesContextMenuItems(popupMenu,readOnly)) popupMenu.addSeparator();
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Release.XML.Root");
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
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Release.XML.SeizedResource")));
		sub.setTextContent(seizeName);

		releaseDelay.save(doc,node,e->{
			e.setAttribute(Language.trPrimary("Surface.Release.XML.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
		});
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

		if (Language.trAll("Surface.Release.XML.SeizedResource",name)) {
			seizeName=node.getTextContent();
		}

		if (DistributionSystem.isDistribution(node)) {
			if (releaseDelay.isGlobal(node)) timeBase=ModelSurface.getTimeBaseInteger(Language.trAllAttribute("Surface.Release.XML.TimeBase",node));
			return releaseDelay.loadDistribution(node);
		}
		if (DistributionSystem.isExpression(node)) {
			if (releaseDelay.isGlobal(node)) timeBase=ModelSurface.getTimeBaseInteger(Language.trAllAttribute("Surface.Release.XML.TimeBase",node));
			return releaseDelay.loadExpression(node);
		}

		return null;
	}

	/**
	 * Liefert den Name des zugehörigen Ressourcen-Belegungs-Elements
	 * @return	Zugehöriges Ressourcen-Belegungs-Element
	 */
	public String getSeizeName() {
		return seizeName;
	}

	/**
	 * Stellt den Name des zugehörigen Ressourcen-Belegungs-Elements ein
	 * @param seizeName	Zugehöriges Ressourcen-Belegungs-Element
	 */
	public void setSeizeName(final String seizeName) {
		if (seizeName==null) this.seizeName=""; else this.seizeName=seizeName;
	}

	/**
	 * Liefert das Objekt, welches die Verteilung oder den Ausdruck für die verzögerte Ressourcenfreigabe vorhält
	 * @return	Verzögerte Ressourcenfreigabe
	 */
	public DistributionSystem getReleaseDelay() {
		return releaseDelay;
	}

	/**
	 * Liefert die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen)
	 * @return	Verwendete Zeitbasis
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return timeBase;
	}

	/**
	 * Stellt die verwendete Zeitbasis (ob die Verteilungswerte Sekunden-, Minuten- oder Stunden-Angaben darstellen sollen) ein.
	 * @param timeBase	Neue zu verwendende Zeitbasis
	 */
	public void setTimeBase(final ModelSurface.TimeBase timeBase) {
		this.timeBase=timeBase;
	}

	@Override
	public void objectRenamed(String oldName, String newName, ModelDataRenameListener.RenameType type) {
		if (!isRenameType(oldName,newName,type,ModelDataRenameListener.RenameType.RENAME_TYPE_CLIENT_TYPE)) return;

		if (!releaseDelay.nameInUse(newName)) releaseDelay.renameSubType(oldName,newName);
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementRelease";
	}

	/**
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Zugehöriges "Ressource belegen"-Element */
		if (seizeName!=null && !seizeName.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Release.Seize"),seizeName,1000);
		}

		/* Zeitbasis */
		descriptionBuilder.addTimeBaseProperty(timeBase,2000);

		/* Verzögerte Freigabe */
		releaseDelay.buildDescriptionProperty(descriptionBuilder,Language.tr("ModelDescription.Release.DelayedRelease.ClientType"),Language.tr("ModelDescription.Release.DelayedRelease.GeneralCase"),3000);
	}
}