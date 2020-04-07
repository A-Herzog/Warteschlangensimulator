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
package ui.modeleditor.coreelements;

import java.awt.Component;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.EditModel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Basisklasse f�r alle Logik-Stationen die eine Bedingung besitzen (If, While, ...)
 * @author Alexander Herzog
 * @see ModelElementLogic
 */
public abstract class ModelElementLogicWithCondition extends ModelElementLogic {
	private String condition;

	/**
	 * Konstruktor der Klasse <code>ModelElementLogicWithCondition</code>
	 * @param model	Modell zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 * @param surface	Zeichenfl�che zu dem dieses Element geh�ren soll (kann sp�ter nicht mehr ge�ndert werden)
	 */
	public ModelElementLogicWithCondition(EditModel model, ModelSurface surface) {
		super(model,surface);
		condition="";
	}

	/**
	 * �berpr�ft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementLogicWithCondition)) return false;

		if (!((ModelElementLogicWithCondition)element).condition.equals(condition)) return false;

		return true;
	}

	/**
	 * �bertr�gt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen �bernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementLogicWithCondition) {
			condition=((ModelElementLogicWithCondition)element).condition;
		}
	}

	/**
	 * Liefert den aktuellen Wert der Bedingung
	 * @return	Aktueller Wert der Bedingung
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Stellt einen neuen Wert f�r die Bedingung ein
	 * @param condition	Neuer Wert f�r die Bedingung ein
	 */
	public void setCondition(final String condition) {
		if (condition==null) this.condition=""; else this.condition=condition;
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
			new ModelElementLogicWithConditionDialog(owner,ModelElementLogicWithCondition.this,readOnly,getTypeName(),getHelpPageName(),getInfoPanelID());
		};
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
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Logic.XML.Condition")));
		sub.setTextContent(condition);
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

		if (Language.trAll("Surface.Logic.XML.Condition",name)) {
			condition=node.getTextContent();
			return null;
		}

		return null;
	}

	/*
	Zu langer Text unter dem Element.
	@Override
	protected String getIDInfo() {
		if (condition!=null && !condition.isEmpty()) {
			return super.getIDInfo()+", "+Language.tr("Surface.Logic.Dialog.Condition.Short")+"="+condition;
		} else {
			return super.getIDInfo();
		}
	}
	 */

	/**
	 * Erstellt eine Beschreibung f�r das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!condition.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Logic.Condition"),condition,1000);
	}
}
