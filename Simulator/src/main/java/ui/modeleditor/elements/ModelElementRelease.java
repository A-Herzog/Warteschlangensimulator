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
 * Belegt Ressourcen, wenn Kunden die Station passieren bzw. verz�gert diese, bis Ressourcen verf�gbar sind
 * @author Alexander Herzog
 * @see ModelElementSeize
 */
public class ModelElementRelease extends ModelElementMultiInSingleOutBox implements ModelDataRenameListener {
	/**
	 * Zugeh�riges Ressourcen-Belegungs-Element
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
	 * Verz�gerte Ressourcenfreigabe
	 * @see #getReleaseDelay()
	 */
	private DistributionSystem releaseDelay;

	/**
	 * Konstruktor der Klasse <code>ModelElementSeize</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementRelease(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_RECTANGLE);

		seizeName="";
		timeBase=ModelSurface.TimeBase.TIMEBASE_SECONDS;

		/* Um sicher zu stellen, dass die Language-Strings auch in den Sprachdateien vorhanden sind. Der folgende DistributionSystem-Konstruktor ist kein Scan-Ziel f�r die Sprachdateien. */
		Language.tr("Surface.Release.XML.Distribution.ClientType");

		releaseDelay=new DistributionSystem("Surface.Release.XML.Distribution.ClientType",null,true);
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_RELEASE.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.Release.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
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
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
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
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementRelease clone(final EditModel model, final ModelSurface surface) {
		final ModelElementRelease element=new ModelElementRelease(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
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
			new ModelElementReleaseDialog(owner,ModelElementRelease.this,readOnly);
		};
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
		return Language.trAll("Surface.Release.XML.Root");
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
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Release.XML.SeizedResource")));
		sub.setTextContent(seizeName);

		releaseDelay.save(doc,node,e->{
			e.setAttribute(Language.trPrimary("Surface.Release.XML.TimeBase"),ModelSurface.getTimeBaseString(timeBase));
		});
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
	 * Liefert den Name des zugeh�rigen Ressourcen-Belegungs-Elements
	 * @return	Zugeh�riges Ressourcen-Belegungs-Element
	 */
	public String getSeizeName() {
		return seizeName;
	}

	/**
	 * Stellt den Name des zugeh�rigen Ressourcen-Belegungs-Elements ein
	 * @param seizeName	Zugeh�riges Ressourcen-Belegungs-Element
	 */
	public void setSeizeName(final String seizeName) {
		if (seizeName==null) this.seizeName=""; else this.seizeName=seizeName;
	}

	/**
	 * Liefert das Objekt, welches die Verteilung oder den Ausdruck f�r die verz�gerte Ressourcenfreigabe vorh�lt
	 * @return	Verz�gerte Ressourcenfreigabe
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
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		/* Zugeh�riges "Ressource belegen"-Element */
		if (seizeName!=null && !seizeName.trim().isEmpty()) {
			descriptionBuilder.addProperty(Language.tr("ModelDescription.Release.Seize"),seizeName,1000);
		}

		/* Zeitbasis */
		descriptionBuilder.addTimeBaseProperty(timeBase,2000);

		/* Verz�gerte Freigabe */
		releaseDelay.buildDescriptionProperty(descriptionBuilder,Language.tr("ModelDescription.Release.DelayedRelease.ClientType"),Language.tr("ModelDescription.Release.DelayedRelease.GeneralCase"),3000);
	}
}