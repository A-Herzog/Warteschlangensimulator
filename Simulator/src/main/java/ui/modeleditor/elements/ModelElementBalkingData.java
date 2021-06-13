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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Dieses Element h�lt die globalen oder die Kundentyp-abh�ngigen
 * Zur�ckschreckdaten f�r ein {@link ModelElementBalking}-Element vor.
 * @author Alexander Herzog
 * @see ModelElementBalking
 */
public class ModelElementBalkingData {
	/**
	 * Ausdruck zur Berechnung der Zur�ckschreckwahrscheinlichkeit
	 * @see #getExpression()
	 * @see #setExpression(String)
	 */
	private String expression;

	/**
	 * Zur�ckschreckwahrscheinlichkeit
	 * @see #getProbability()
	 * @see #setProbability(double)
	 */
	private double probability;

	/**
	 * Kundentyp auf den sich diese Zur�ckschreckwahrscheinlichkeit beziehen soll
	 * @see #getClientType()
	 * @see #setClientType(String)
	 */
	private String clientType;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementBalkingData() {
		expression=null;
		probability=0;
		clientType=null;
	}

	/**
	 * Konstruktor der Klasse
	 * @param expression	Rechenausdruck der die Zur�ckschreckwahrscheinlichkeit liefert
	 * @param clientType	Kundentyp auf den sich die Zur�ckschreckwahrscheinlichkeit bezieht
	 */
	public ModelElementBalkingData(final String expression, final String clientType) {
		this.expression=expression;
		probability=0;
		this.clientType=clientType;
	}

	/**
	 * Konstruktor der Klasse
	 * @param probability	Zur�ckschreckwahrscheinlichkeit
	 * @param clientType	Kundentyp auf den sich die Zur�ckschreckwahrscheinlichkeit bezieht
	 */
	public ModelElementBalkingData(final double probability, final String clientType) {
		expression=null;
		this.probability=probability;
		this.clientType=clientType;
	}

	/**
	 * Copy-Konstruktor
	 * @param original	Ausgangsobjekt von dem die Daten kopiert werden sollen
	 */
	public ModelElementBalkingData(final ModelElementBalkingData original) {
		setDataFrom(original);
	}

	/**
	 * Kopiert die Daten von einem anderen Objekt in dieses
	 * @param original	Ausgangsobjekt von dem die Daten kopiert werden sollen
	 */
	public void setDataFrom(final ModelElementBalkingData original) {
		expression=original.expression;
		probability=original.probability;
		clientType=original.clientType;
	}

	/**
	 * Liefert den gew�hlten Ausdruck dessen Auswertung die Zur�ckschreckwahrscheinlichkeit ergeben soll.
	 * @return	Ausdruck zur Berechnung der Zur�ckschreckwahrscheinlichkeit
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Stellt den Ausdruck zur Berechnung der Zur�ckschreckwahrscheinlichkeit ein.
	 * @param expression	Ausdruck zur Berechnung der Zur�ckschreckwahrscheinlichkeit
	 */
	public void setExpression(final String expression) {
		if (expression==null) this.expression=""; else this.expression=expression;
	}

	/**
	 * Liefert die Zur�ckschreckwahrscheinlichkeit.
	 * @return	Zur�ckschreckwahrscheinlichkeit
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * Stellt die Zur�ckschreckwahrscheinlichkeit ein.<br>
	 * Der Ausdruck zur Berechnung der Zur�ckschreckwahrscheinlichkeit wird dabei gleichzeitig gel�scht.
	 * @param probability	Neue Zur�ckschreckwahrscheinlichkeit
	 */
	public void setProbability(final double probability) {
		this.probability=Math.min(1,Math.max(0,probability));
		expression=null;
	}

	/**
	 * Liefert den Ausdruck oder direkt den Wert der Zur�ckschreckwahrscheinlichkeit.
	 * @return	Zur�ckschreckwahrscheinlichkeit (entweder als {@link String}-Ausdruck zur Berechnung oder als {@link Double}-Wahrscheinlichkeit)
	 */
	public Object getObject() {
		if (expression!=null) return expression;
		return Double.valueOf(probability);
	}

	/**
	 * Liefert den eingestellten Kundentyp auf den sich diese Zur�ckschreckwahrscheinlichkeit beziehen soll.
	 * @return	Kundentyp auf den sich diese Zur�ckschreckwahrscheinlichkeit beziehen soll
	 */
	public String getClientType() {
		return (clientType==null)?"":clientType;
	}

	/**
	 * Stellt den Kundentyp auf den sich diese Zur�ckschreckwahrscheinlichkeit beziehen soll ein.
	 * @param clientType	Kundentyp auf den sich diese Zur�ckschreckwahrscheinlichkeit beziehen soll
	 */
	public void setClientType(final String clientType) {
		this.clientType=clientType;
	}

	/**
	 * �ber diese Methode wird das Zur�ckschreckwahrscheinlichkeit-Objekt dar�ber informiert,
	 * dass sich ein Kundentyp-Name ge�ndert hat. Ist dies der Name des Kundentyps, auf den
	 * sich dieses Objekt bezieht, so passt es sich entsprechend an.
	 * @param oldName	Alter Kundentyp-Namen
	 * @param newName	Neuer Kundentyp-Namen
	 */
	public void renameClientType(final String oldName, final String newName) {
		if (oldName==null || oldName.trim().isEmpty() || newName==null || newName.trim().isEmpty()) return;
		if (clientType==null) return;
		if (clientType.equalsIgnoreCase(oldName)) clientType=newName;
	}

	/**
	 * Pr�ft, ob dieses Zur�ckschreckwahrscheinlichkeit-Objekt inhaltlich einem anderen gleicht
	 * @param otherData	Andere Zur�ckschreckwahrscheinlichkeit-Objekt das mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsBalkingData(final ModelElementBalkingData otherData) {
		if (otherData==null) return false;

		if (expression==null) {
			if (otherData.expression!=null) return false;
			if (probability!=otherData.probability) return false;
		} else {
			if (otherData.expression==null) return false;
			if (!expression.equals(otherData.expression)) return false;
		}

		if (clientType==null) {
			if (otherData.clientType!=null) return false;
		} else {
			if (otherData.clientType==null) return false;
			if (!clientType.equals(otherData.clientType)) return false;
		}

		return true;
	}

	/**
	 * Speichert den Zur�ckschreck-Datensatz in einem Unterelement eines xml-Knoten
	 * @param doc	xml-Dokument
	 * @param parent	xml-Knoten in dem ein neues Unterelement f�r den Datensatz angelegt werden soll
	 * @see ModelElementBalking#addPropertiesDataToXML(Document, Element)
	 */
	public void addToXML(final Document doc, final Element parent) {
		Element node;
		parent.appendChild(node=doc.createElement(Language.trPrimary("Surface.Balking.XML.Expression")));

		if (expression!=null) {
			node.setTextContent(expression);
			node.setAttribute(Language.trPrimary("Surface.Balking.XML.Expression.Mode"),Language.trPrimary("Surface.Balking.XML.Expression.Mode.Condition"));
		} else {
			node.setTextContent(NumberTools.formatSystemNumber(probability));
			node.setAttribute(Language.trPrimary("Surface.Balking.XML.Expression.Mode"),Language.trPrimary("Surface.Balking.XML.Expression.Mode.Probability"));
		}

		if (clientType!=null) {
			node.setAttribute(Language.trPrimary("Surface.Balking.XML.Expression.ClientType"),clientType);
		}
	}

	/**
	 * Versucht einen Zur�ckschreck-Datensatz aus einem xml-Element zu laden
	 * @param node	xml-Element aus dem der Zur�ckschreck-Datensatz geladen werden soll
	 * @return	Liefert im Erfolgsfall <code>null</code> sonst eine Fehlermeldung
	 */
	public String loadFromXML(final Element node) {
		final String mode=Language.trAllAttribute("Surface.Balking.XML.Expression.Mode",node);
		boolean isProbability=false;
		for (String test: Language.trAll("Surface.Balking.XML.Expression.Mode.Probability")) if (test.equals(mode)) {
			isProbability=true;
			break;
		}

		if (isProbability) {
			final Double D=NumberTools.getProbability(NumberTools.systemNumberToLocalNumber(node.getTextContent()));
			if (D==null) return String.format(Language.tr("Surface.Balking.XML.Expression.Mode.Probability.Error"),node.getTextContent());
			probability=D.doubleValue();
		} else {
			expression=node.getTextContent();
		}

		final String type=Language.trAllAttribute("Surface.Balking.XML.Expression.ClientType",node);
		if (type!=null && !type.trim().isEmpty()) clientType=type;

		return null;
	}

	/**
	 * Erstellt eine Beschreibung f�r den Datensatz
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param level	Position in der Reihenfolge der Eigenschaften
	 * @see ModelElementBalking#buildDescription(ModelDescriptionBuilder)
	 */
	public void buildDescription(final ModelDescriptionBuilder descriptionBuilder, final int level) {
		if (expression==null) {
			if (clientType==null || clientType.trim().isEmpty()) {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Balking.Probability"),NumberTools.formatPercent(probability),level);
			} else {
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Balking.ProbabilityClientType"),clientType),NumberTools.formatPercent(probability),level);
			}
		} else {
			if (clientType==null || clientType.trim().isEmpty()) {
				descriptionBuilder.addProperty(Language.tr("ModelDescription.Balking.Expression"),expression,level);
			} else {
				descriptionBuilder.addProperty(String.format(Language.tr("ModelDescription.Balking.ExpressionClientType"),clientType),expression,level);
			}
		}
	}

	/**
	 * Sucht einen Text in den Daten des Elements.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		if (expression!=null) {
			if (clientType==null || clientType.trim().isEmpty()) {
				searcher.testString(station,Language.tr("Editor.DialogBase.Search.BalkingExpressionGlobal"),expression,newExpression->{expression=newExpression;});
			} else {
				searcher.testString(station,String.format(Language.tr("Editor.DialogBase.Search.BalkingExpressionClientType"),clientType),expression,newExpression->{expression=newExpression;});
			}
		} else {
			if (clientType==null || clientType.trim().isEmpty()) {
				searcher.testDouble(station,Language.tr("Editor.DialogBase.Search.BalkingProbabilityGlobal"),probability,newProbability->{if (newProbability>=0) probability=newProbability;});
			} else {
				searcher.testDouble(station,String.format(Language.tr("Editor.DialogBase.Search.BalkingProbabilityClientType"),clientType),probability,newProbability->{if (newProbability>=0) probability=newProbability;});
			}
		}
	}
}
