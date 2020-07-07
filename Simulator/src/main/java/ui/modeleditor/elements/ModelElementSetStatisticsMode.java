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
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Stellt ein, ob ein Kunde in der Statistik erfasst werden soll.
 * @author Alexander Herzog
 */
public class ModelElementSetStatisticsMode extends ModelElementMultiInSingleOutBox {
	/**
	 * In welchen Modus soll die Statistikerfassung f�r einen Kunden geschaltet werden,
	 * wenn er diese Station passiert?
	 * @author Alexander Herzog
	 * @see ModelElementSetStatisticsMode#getMode()
	 * @see ModelElementSetStatisticsMode#setMode(Mode)
	 */
	public enum Mode {
		/**
		 * Statistikerfassung ausschalten
		 */
		OFF,

		/**
		 * Statistikerfassung einschalten
		 */
		ON,

		/**
		 * Statistikerfassung gem�� Bedingung ein- oder ausschalten
		 * @see ModelElementSetStatisticsMode#getCondition()
		 * @see ModelElementSetStatisticsMode#setCondition(String)
		 */
		CONDITION
	}

	private Mode mode;
	private String condition;

	/**
	 * Konstruktor der Klasse <code>ModelElementSetStatisticsMode</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementSetStatisticsMode(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE);
		mode=Mode.OFF;
		condition="";
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_SET_STATISTICS_MODE.getURL();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.SetStatisticsMode.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementSetStatisticsMode)) return false;

		if (mode!=((ModelElementSetStatisticsMode)element).mode) return false;
		if (mode==Mode.CONDITION) {
			if (!condition.equals(((ModelElementSetStatisticsMode)element).condition)) return false;
		}

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementSetStatisticsMode) {
			mode=((ModelElementSetStatisticsMode)element).mode;
			condition=((ModelElementSetStatisticsMode)element).condition;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementSetStatisticsMode clone(final EditModel model, final ModelSurface surface) {
		final ModelElementSetStatisticsMode element=new ModelElementSetStatisticsMode(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.SetStatisticsMode.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.SetStatisticsMode.Name");
	}

	/**
	 * Liefert optional eine zus�tzliche Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box in einer zweiten Zeile)
	 * @return	Zus�tzlicher Name des Typs (kann <code>null</code> oder leer sein)
	 */
	@Override
	public String getSubTypeName() {
		switch (mode) {
		case ON: return Language.tr("Surface.SetStatisticsMode.Info.On");
		case OFF: return Language.tr("Surface.SetStatisticsMode.Info.Off");
		case CONDITION: return Language.tr("Surface.SetStatisticsMode.Info.Condition");
		default: return null;
		}
	}

	private static final Color defaultBackgroundColor=new Color(255,255,180);

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
			new ModelElementSetStatisticsModeDialog(owner,ModelElementSetStatisticsMode.this,readOnly);
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
		NextStationHelper.nextStationsAssign(this,parentMenu,addNextStation);
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
		return Language.trAll("Surface.SetStatisticsMode.XML.Root");
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
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.SetStatisticsMode.XML.StatisticsMode")));
		switch (mode) {
		case ON:
			sub.setAttribute(Language.trPrimary("Surface.SetStatisticsMode.XML.StatisticsMode.Mode"),Language.trPrimary("Surface.SetStatisticsMode.XML.StatisticsMode.Mode.On"));
			break;
		case OFF:
			sub.setAttribute(Language.trPrimary("Surface.SetStatisticsMode.XML.StatisticsMode.Mode"),Language.trPrimary("Surface.SetStatisticsMode.XML.StatisticsMode.Mode.Off"));
			break;
		case CONDITION:
			sub.setAttribute(Language.trPrimary("Surface.SetStatisticsMode.XML.StatisticsMode.Mode"),Language.trPrimary("Surface.SetStatisticsMode.XML.StatisticsMode.Mode.Condition"));
			sub.setTextContent(condition);
			break;
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

		if (Language.trAll("Surface.SetStatisticsMode.XML.StatisticsMode",name)) {
			final String modeString=Language.trAllAttribute("Surface.SetStatisticsMode.XML.StatisticsMode.Mode",node);
			if (Language.trAll("Surface.SetStatisticsMode.XML.StatisticsMode.Mode.On",modeString)) {
				mode=Mode.ON;
				return null;
			}
			if (Language.trAll("Surface.SetStatisticsMode.XML.StatisticsMode.Mode.Off",modeString)) {
				mode=Mode.OFF;
				return null;
			}
			if (Language.trAll("Surface.SetStatisticsMode.XML.StatisticsMode.Mode.Condition",modeString)) {
				mode=Mode.CONDITION;
				condition=node.getTextContent();
				return null;
			}
		}

		return null;
	}

	/**
	 * Liefert den aktuellen Statistik-Ein/Aus-Modus
	 * @return	Aktueller Statistik-Ein/Aus-Modus
	 * @see ModelElementSetStatisticsMode.Mode
	 */
	public Mode getMode() {
		if (mode==null) return Mode.ON;
		return mode;
	}

	/**
	 * Stellt den Statistik-Ein/Aus-Modus ein
	 * @param mode	Neuer Statistik-Ein/Aus-Modus
	 * @see ModelElementSetStatisticsMode.Mode
	 */
	public void setMode(final Mode mode) {
		if (mode==null) this.mode=Mode.ON; else this.mode=mode;
		fireChanged();
	}

	/**
	 * Liefert die Bedingung im Modus <code>Mode.CONDITION</code>
	 * @return	Bedingung
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die Bedingung f�r den Modus <code>Mode.CONDITION</code> ein
	 * @param condition	Bedingung
	 */
	public void setCondition(final String condition) {
		if (condition==null) this.condition=""; else this.condition=condition;
	}

	/**
	 * Gibt an, ob Laufzeitdaten zu der Station w�hrend der Animation ausgegeben werden sollen
	 * @return Laufzeitdaten zur Station ausgeben
	 */
	@Override
	public boolean showAnimationRunData() {
		return super.showAnimationRunData(); /* statt einfach "false". Schadet ja auch an dieser Station nicht. */
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementSetStatisticsMode";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		switch (mode) {
		case ON:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.SetStatisticsMode.Mode"),Language.tr("ModelDescription.SetStatisticsMode.Mode.On"),1000);
			break;
		case OFF:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.SetStatisticsMode.Mode"),Language.tr("ModelDescription.SetStatisticsMode.Mode.Off"),1000);
			break;
		case CONDITION:
			descriptionBuilder.addProperty(Language.tr("ModelDescription.SetStatisticsMode.Mode"),Language.tr("ModelDescription.SetStatisticsMode.Mode.Condition"),1000);
			if (condition!=null) descriptionBuilder.addProperty(Language.tr("ModelDescription.SetStatisticsMode.Condition"),condition,2000);
			break;
		}
	}
}
