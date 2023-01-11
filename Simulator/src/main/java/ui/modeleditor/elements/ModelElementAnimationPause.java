/**
 * Copyright 2022 Alexander Herzog
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

import javax.swing.Icon;

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
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementMultiInSingleOutBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Pausiert die Animation, wenn ein Kunde die Station erreicht.
 * @author Alexander Herzog
 */
public class ModelElementAnimationPause extends ModelElementMultiInSingleOutBox {
	/**
	 * Soll nur einmal oder immer angehalten werden?
	 * @see #isOnlyOneActivation()
	 * @see #setOnlyOneActivation(boolean)
	 */
	private boolean onlyOneActivation;

	/**
	 * Soll bei jedem Kundentyp oder nur bei einem bestimmten Kundentyp angehalten werden?
	 * @see #getClientType()
	 * @see #setClientType(String)
	 */
	private String clientType;

	/**
	 * Bedingung, die f�r das Anhalten erf�llt sein muss
	 * @see #getCondition()
	 * @see #setCondition(String)
	 */
	private String condition;

	/**
	 * Soll bei jeder Ankunft (&le;0) oder nur bei jeder n-ten Ankunft (&gt;0) angehalten werden?
	 * @see #getCounter()
	 * @see #setCounter(long)
	 */
	private long counter;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementAnimationPause(final EditModel model, final ModelSurface surface) {
		super(model,surface,Shapes.ShapeType.SHAPE_ROUNDED_RECTANGLE_PAUSE);
		onlyOneActivation=false;
		clientType="";
		condition="";
		counter=0;
	}

	/**
	 * Icon, welches im "Element hinzuf�gen"-Dropdown-Men� angezeigt werden soll.
	 * @return	Icon f�r das Dropdown-Men�
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_PAUSE.getIcon();
	}

	/**
	 * Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�-Eintrag.
	 * @return Tooltip f�r den "Element hinzuf�gen"-Dropdown-Men�eintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationPause.Tooltip");
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationPause)) return false;
		final ModelElementAnimationPause otherPause=(ModelElementAnimationPause)element;

		if (onlyOneActivation!=otherPause.onlyOneActivation) return false;
		if (!clientType.equals(otherPause.clientType)) return false;
		if (!condition.equals(otherPause.condition)) return false;
		if (counter!=otherPause.counter) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationPause) {
			final ModelElementAnimationPause source=(ModelElementAnimationPause)element;

			onlyOneActivation=source.onlyOneActivation;
			clientType=source.clientType;
			condition=source.condition;
			counter=source.counter;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element geh�ren soll.
	 * @param surface	Zeichenfl�che zu der das kopierte Element geh�ren soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationPause clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationPause element=new ModelElementAnimationPause(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Soll nur einmal oder immer angehalten werden?
	 * @return	Soll nur einmal oder immer angehalten werden?
	 */
	public boolean isOnlyOneActivation() {
		return onlyOneActivation;
	}

	/**
	 * Stellt ein, ob nur einmal oder bei jeder Kundenankunft angehalten werden soll.
	 * @param onlyOneActivation	Soll nur einmal oder immer angehalten werden?
	 */
	public void setOnlyOneActivation(final boolean onlyOneActivation) {
		this.onlyOneActivation=onlyOneActivation;
	}

	/**
	 * Soll bei jedem Kundentyp oder nur bei einem bestimmten Kundentyp angehalten werden?
	 * @return	Kundentyp, bei dem angehalten werden soll (oder leerer String f�r alle Kundentypen)
	 */
	public String getClientType() {
		return clientType;
	}

	/**
	 * Stellt ein, ob bei jedem Kundentyp oder nur bei einem bestimmten Kundentyp angehalten werden soll.
	 * @param clientType	Kundentyp, bei dem angehalten werden soll (oder leerer String f�r alle Kundentypen)
	 */
	public void setClientType(final String clientType) {
		this.clientType=(clientType==null)?"":clientType;
	}

	/**
	 * Liefert die Bedingung, die f�r das Anhalten erf�llt sein muss.
	 * @return	Bedingung, die f�r das Anhalten erf�llt sein muss (kann <code>null</code> sein)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt die Bedingung, die f�r das Anhalten erf�llt sein muss, ein.
	 * @param condition	Bedingung, die f�r das Anhalten erf�llt sein muss (kann <code>null</code> sein)
	 */
	public void setCondition(final String condition) {
		this.condition=(condition==null)?"":condition;
	}

	/**
	 * Soll bei jeder Ankunft (&le;0) oder nur bei jeder n-ten Ankunft (&gt;0) angehalten werden?
	 * @return	Soll bei jeder Ankunft (&le;0) oder nur bei jeder n-ten Ankunft (&gt;0) angehalten werden?
	 */
	public long getCounter() {
		return counter;
	}

	/**
	 * Stellt ein, ob bei jeder Ankunft (&le;0) oder nur bei jeder n-ten Ankunft (&gt;0) angehalten werden.
	 * @param counter	Soll bei jeder Ankunft (&le;0) oder nur bei jeder n-ten Ankunft (&gt;0) angehalten werden?
	 */
	public void setCounter(final long counter) {
		this.counter=(counter<0)?0:counter;
	}

	/**
	 * Name des Elementtyps f�r die Anzeige im Kontextmen�
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationPause.Name");
	}

	/**
	 * Liefert die Bezeichnung des Typs des Elemente (zur Anzeige in der Element-Box)
	 * @return	Name des Typs
	 */
	@Override
	public String getTypeName() {
		return Language.tr("Surface.AnimationPause.Name");
	}

	/**
	 * Vorgabe-Hintergrundfarbe f�r die Box
	 * @see #getTypeDefaultBackgroundColor()
	 */
	private static final Color defaultBackgroundColor=Color.RED;

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
			new ModelElementAnimationPauseDialog(owner,ModelElementAnimationPause.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen f�r das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationPause.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereintr�ge eines xml-Knotens
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param node	�bergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		if (onlyOneActivation || !clientType.isEmpty() || !condition.isEmpty() || counter>1) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.AnimationPause.XML.Condition"));
			node.appendChild(sub);
			if (onlyOneActivation) sub.setAttribute(Language.tr("Surface.AnimationPause.XML.Condition.OnlyOnce"),"1");
			if (!clientType.isEmpty()) sub.setAttribute(Language.tr("Surface.AnimationPause.XML.Condition.ClientType"),clientType);
			if (counter>1) sub.setAttribute(Language.tr("Surface.AnimationPause.XML.Condition.Counter"),""+counter);
			if (!condition.isEmpty()) sub.setTextContent(condition);
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

		if (Language.trAll("Surface.AnimationPause.XML.Condition",name)) {
			onlyOneActivation=Language.trAllAttribute("Surface.AnimationPause.XML.Condition.OnlyOnce",node).equals("1");
			clientType=Language.trAllAttribute("Surface.AnimationPause.XML.Condition.ClientType",node);
			final String counterString=Language.trAllAttribute("Surface.AnimationPause.XML.Condition.Counter",node);
			if (!counterString.isEmpty()) {
				final Long L=NumberTools.getNotNegativeLong(counterString);
				if (L==null || L<2) counter=0; else counter=L;
			}
			condition=content;
			return null;
		}

		return null;
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
		return "ModelElementAnimationPause";
	}

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (onlyOneActivation) descriptionBuilder.addProperty(Language.tr("ModelDescription.AnimationPause.OnlyOnce"),Language.tr("ModelDescription.AnimationPause.OnlyOnce.Yes"),1000);
		if (!clientType.isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.AnimationPause.ClientType"),clientType,2000);
		if (counter>1) descriptionBuilder.addProperty(Language.tr("ModelDescription.AnimationPause.Counter"),NumberTools.formatLong(counter),3000);
		if (!condition.isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.AnimationPause.Condition"),condition,4000);
	}

	/**
	 * Sucht einen Text in den Daten dieses Datensatzes.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		if (!clientType.isEmpty()) searcher.testString(station,Language.tr("Editor.DialogBase.Search.ClientType"),clientType,newClientType->clientType=newClientType);
		if (!condition.isEmpty()) searcher.testString(station,Language.tr("Editor.DialogBase.Search.Condition"),condition,newCondition->condition=newCondition);
		if (counter>0) searcher.testLong(station,Language.tr("Editor.DialogBase.Search.TriggerDistanceCount"),counter,newCounter->counter=(newCounter>1)?newCounter:0);
	}
}