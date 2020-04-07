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
 * Basisklasse für alle Logik-Stationen die eine Bedingung besitzen (If, While, ...)
 * @author Alexander Herzog
 * @see ModelElementLogic
 */
public abstract class ModelElementLogicWithCondition extends ModelElementLogic {
	private String condition;

	/**
	 * Konstruktor der Klasse <code>ModelElementLogicWithCondition</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementLogicWithCondition(EditModel model, ModelSurface surface) {
		super(model,surface);
		condition="";
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementLogicWithCondition)) return false;

		if (!((ModelElementLogicWithCondition)element).condition.equals(condition)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
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
	 * Stellt einen neuen Wert für die Bedingung ein
	 * @param condition	Neuer Wert für die Bedingung ein
	 */
	public void setCondition(final String condition) {
		if (condition==null) this.condition=""; else this.condition=condition;
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
			new ModelElementLogicWithConditionDialog(owner,ModelElementLogicWithCondition.this,readOnly,getTypeName(),getHelpPageName(),getInfoPanelID());
		};
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
		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Logic.XML.Condition")));
		sub.setTextContent(condition);
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
	 * Erstellt eine Beschreibung für das aktuelle Element
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 */
	@Override
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder) {
		super.buildDescription(descriptionBuilder);

		if (!condition.trim().isEmpty()) descriptionBuilder.addProperty(Language.tr("ModelDescription.Logic.Condition"),condition,1000);
	}
}
