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
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * �ndert die Einstellungen in einem {@link ModelElementAnalogValue}
 * @author Alexander Herzog
 * @see ModelElementAnalogValue
 */
public class ModelElementAnalogAssign extends ModelElementMultiInSingleOutBox {
	/**
	 * Art der Zuweisung
	 * @author Alexander Herzog
	 * @see ModelElementAnalogAssign#getChangeMode()
	 */
	public enum ChangeMode {
		/** Analogen Wert zuweisen */
		CHANGE_MODE_VALUE,
		/** �nderungsrate eines analogen Wertes �ndern */
		CHANGE_MODE_RATE
	}

	/**
	 * Liste der "Analoger Wert"-Element-IDs, an denen �nderungen vorgenommen werden sollen
	 * @see #getChangeID()
	 */
	private final List<Integer> changeID;

	/**
	 * Liste der �nderungs-Modi (Wert oder Rate)
	 * @see #getChangeMode()
	 */
	private final List<ChangeMode> changeMode;

	/**
	 * Liste der Ausdr�cke, deren Werte an die Stationen zugewiesen werden sollen
	 * @see #getChangeExpression()
	 */
	private final List<String> changeExpression;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnalogAssign</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnalogAssign(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE);
		changeID=new ArrayList<>();
		changeMode=new ArrayList<>();
		changeExpression=new ArrayList<>();
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANALOG_ASSIGN.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnalogAssign.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnalogAssign)) return false;

		final ModelElementAnalogAssign other=(ModelElementAnalogAssign)element;

		if (!Objects.deepEquals(changeID,other.changeID)) return false;
		if (!Objects.deepEquals(changeMode,other.changeMode)) return false;
		if (!Objects.deepEquals(changeExpression,other.changeExpression)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnalogAssign) {
			final ModelElementAnalogAssign source=(ModelElementAnalogAssign)element;
			changeID.clear();
			changeID.addAll(source.changeID);
			changeMode.clear();
			changeMode.addAll(source.changeMode);
			changeExpression.clear();
			changeExpression.addAll(source.changeExpression);
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnalogAssign clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnalogAssign element=new ModelElementAnalogAssign(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnalogAssign.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.AnalogAssign.Name.Short");
	}

	/**
	 * Liefert die Liste der "Analoger Wert"-Element-IDs, an denen �nderungen vorgenommen werden sollen.
	 * @return	Liste der "Analoger Wert"-Element-IDs, an denen �nderungen vorgenommen werden sollen.
	 */
	public List<Integer> getChangeID() {
		return changeID;
	}

	/**
	 * Liefert die Liste der �nderungs-Modi (Wert oder Rate).
	 * @return	Liste der �nderungs-Modi
	 * @see ChangeMode
	 */
	public List<ChangeMode> getChangeMode() {
		return changeMode;
	}

	/**
	 * Liefert die Liste der Ausdr�cken, deren Werte zugewiesen werden sollen.
	 * @return	Liste der Ausdr�cke, deren Werte an die Stationen zugewiesen werden sollen
	 */
	public List<String> getChangeExpression() {
		return changeExpression;
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
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
			new ModelElementAnalogAssignDialog(owner,ModelElementAnalogAssign.this,readOnly);
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
		NextStationHelper.nextStationsAnalog(this,parentMenu,addNextStation);
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
		return Language.trAll("Surface.AnalogAssign.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		final int min=Math.min(changeID.size(),Math.min(changeMode.size(),changeExpression.size()));
		for (int i=0;i<min;i++) {
			final Element sub;
			switch (changeMode.get(i)) {
			case CHANGE_MODE_VALUE:
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.AnalogAssign.XML.ModeValue")));
				break;
			case CHANGE_MODE_RATE:
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.AnalogAssign.XML.ModeRate")));
				break;
			default:
				continue;
			}
			sub.setAttribute(Language.trPrimary("Surface.AnalogAssign.XML.ChangeID"),""+changeID.get(i).intValue());
			sub.setTextContent(changeExpression.get(i));
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

		if (Language.trAll("Surface.AnalogAssign.XML.ModeValue",name)) {
			final Integer I=NumberTools.getInteger(Language.trAllAttribute("Surface.AnalogAssign.XML.ChangeID",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnalogAssign.XML.ChangeID"),name,node.getParentNode().getNodeName());
			changeID.add(I);
			changeMode.add(ChangeMode.CHANGE_MODE_VALUE);
			changeExpression.add(content);
			return null;
		}

		if (Language.trAll("Surface.AnalogAssign.XML.ModeRate",name)) {
			final Integer I=NumberTools.getInteger(Language.trAllAttribute("Surface.AnalogAssign.XML.ChangeID",node));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnalogAssign.XML.ChangeID"),name,node.getParentNode().getNodeName());
			changeID.add(I);
			changeMode.add(ChangeMode.CHANGE_MODE_RATE);
			changeExpression.add(content);
			return null;
		}

		return null;
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnalogAssign";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		final int min=Math.min(changeID.size(),Math.min(changeMode.size(),changeExpression.size()));
		for (int i=0;i<min;i++) {
			switch (changeMode.get(i)) {
			case CHANGE_MODE_VALUE:
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.AnalogAssign.Value"),changeID.get(i).intValue()),changeExpression.get(i),1000);
				break;
			case CHANGE_MODE_RATE:
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.AnalogAssign.Rate"),changeID.get(i).intValue()),changeExpression.get(i),1000);
				break;
			}
		}
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (int i=0;i<changeExpression.size();i++) {
			final int index=i;
			if (searcher.isTestIDs()) {
				searcher.testInteger(this,String.format(Language.tr("Editor.DialogBase.Search.IDForAnalogAssign"),changeExpression.get(index)),changeID.get(index));
			}
			searcher.testString(this,String.format(Language.tr("Editor.DialogBase.Search.ExpressionForAnalogStation"),changeID.get(index).intValue()),changeExpression.get(index),newExpression->changeExpression.set(index,newExpression));
		}
	}
}