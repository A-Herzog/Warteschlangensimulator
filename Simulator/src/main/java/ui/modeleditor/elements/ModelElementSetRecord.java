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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import simulator.editmodel.FullTextSearch;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Diese Klasse hält die konkreten Daten für eine Variablenzuweisung vor.
 * @author Alexander Herzog
 * @see ModelElementSet
 * @see ModelElementSourceRecord
 */
public final class ModelElementSetRecord implements Cloneable {
	/* Die folgenden Texte müssen nicht übersetzt werden, da sie nur intern verwendet werden. */
	/** Besonderer Wert für "Wartezeit zuweisen" (wird statt eines Rechenausdrucks intern verwendet; ist nie sichtbar) */
	public static final String SPECIAL_WAITING="<<Wartezeit>>";
	/** Besonderer Wert für "Transportzeit zuweisen" (wird statt eines Rechenausdrucks intern verwendet; ist nie sichtbar) */
	public static final String SPECIAL_TRANSFER="<<Transferzeit>>";
	/** Besonderer Wert für "Bedienzeit zuweisen" (wird statt eines Rechenausdrucks intern verwendet; ist nie sichtbar) */
	public static final String SPECIAL_PROCESS="<<Bedienzeit>>";
	/** Besonderer Wert für "Verweilzeit zuweisen" (wird statt eines Rechenausdrucks intern verwendet; ist nie sichtbar) */
	public static final String SPECIAL_RESIDENCE="<<Verweilzeit>>";

	/** Variablen an die Werte zugewiesen werden sollen */
	private final List<String> variables;
	/** Rechenausdrücke deren Ergebnisse an {@link #variables} zugewiesen werden sollen */
	private final List<String> expressions;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementSetRecord() {
		variables=new ArrayList<>();
		expressions=new ArrayList<>();
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param copySource	Ausgangselement dessen Daten kopiert werden sollen
	 */
	public ModelElementSetRecord(final ModelElementSetRecord copySource) {
		this();
		copyDataFrom(copySource);
	}

	/**
	 * Vergleicht diesen Datensatz mit einem weiteren
	 * @param otherRecord	Zweiter Datensatz, der mit diesem verglichen werden soll
	 * @return	Liefert <code>true</code>, wenn beide Datensätze inhaltlich übereinstimmen
	 */
	public boolean equalsModelElementSetRecord(final ModelElementSetRecord otherRecord) {
		if (otherRecord==null) return false;

		if (otherRecord.variables.size()!=variables.size()) return false;
		for (int i=0;i<variables.size();i++) {
			if (!otherRecord.variables.get(i).equals(variables.get(i))) return false;
			if (!otherRecord.expressions.get(i).equals(expressions.get(i))) return false;
		}

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param otherRecord	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	public void copyDataFrom(final ModelElementSetRecord otherRecord) {
		if (otherRecord==null) return;

		variables.clear();
		variables.addAll(otherRecord.variables);
		expressions.clear();
		expressions.addAll(otherRecord.expressions);
	}

	@Override
	public ModelElementSetRecord clone() {
		return new ModelElementSetRecord(this);
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	public void saveToXML(final Document doc, final Element parent) {
		Element sub;

		for (int i=0;i<variables.size();i++) {
			parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.Set.XML.Assignment")));
			sub.setAttribute(Language.trPrimary("Surface.Set.XML.Assignment.Variable"),variables.get(i));
			final String expression=expressions.get(i);
			switch (expression) {
			case SPECIAL_WAITING:
				sub.setAttribute(Language.trPrimary("Surface.Set.XML.Assignment.Value"),Language.trPrimary("Surface.Set.XML.Assignment.Value.WaitingTime"));
				break;
			case SPECIAL_TRANSFER:
				sub.setAttribute(Language.trPrimary("Surface.Set.XML.Assignment.Value"),Language.trPrimary("Surface.Set.XML.Assignment.Value.TransferTime"));
				break;
			case SPECIAL_PROCESS:
				sub.setAttribute(Language.trPrimary("Surface.Set.XML.Assignment.Value"),Language.trPrimary("Surface.Set.XML.Assignment.Value.ProcessTime"));
				break;
			case SPECIAL_RESIDENCE:
				sub.setAttribute(Language.trPrimary("Surface.Set.XML.Assignment.Value"),Language.trPrimary("Surface.Set.XML.Assignment.Value.ResidenceTime"));
				break;
			default:
				sub.setTextContent(expression);
				break;
			}
		}
	}

	/**
	 * Gibt an, ob ein XML-Element eines zum Laden eines Datenelements ist
	 * @param node	XML-Element bei dem geprüft werden soll, ob es sich um einen Zuweisungsdatensatz handelt
	 * @return	Gibt <code>true</code> zurück, wenn es sich um einen Zuweisungsdatensatz handelt
	 */
	public static boolean isSetNode(final Element node) {
		return Language.trAll("Surface.Set.XML.Assignment",node.getNodeName());
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadXMLNode(final Element node) {
		if (!isSetNode(node)) return null;

		final String variable=Language.trAllAttribute("Surface.Set.XML.Assignment.Variable",node);
		final String value=Language.trAllAttribute("Surface.Set.XML.Assignment.Value",node);
		if (!variable.isEmpty()) {
			if (value.isEmpty()) {
				final String expression=node.getTextContent();
				if (!expression.isEmpty()) {variables.add(variable); expressions.add(expression);}
			} else {
				if (Language.trAll("Surface.Set.XML.Assignment.Value.WaitingTime",value)) {variables.add(variable); expressions.add(SPECIAL_WAITING);}
				if (Language.trAll("Surface.Set.XML.Assignment.Value.TransferTime",value))  {variables.add(variable); expressions.add(SPECIAL_TRANSFER);}
				if (Language.trAll("Surface.Set.XML.Assignment.Value.ProcessTime",value))  {variables.add(variable); expressions.add(SPECIAL_PROCESS);}
				if (Language.trAll("Surface.Set.XML.Assignment.Value.ResidenceTime",value))  {variables.add(variable); expressions.add(SPECIAL_RESIDENCE);}
			}
		}
		return null;
	}

	/**
	 * Liefert die Anzahl an momentan vorhandenen Variablenzuweisungen
	 * @return	Anzahl an vorhandenen Variablenzuweisungen
	 */
	public int getExpressionCount() {
		return variables.size();
	}

	/**
	 * Listet alle Variablennamen auf
	 * @return	Liste aller Variablennamen
	 */
	public String[] getVariables() {
		final String[] arr=variables.toArray(new String[0]);
		return Arrays.copyOf(arr,arr.length);
	}

	/**
	 * Listet alle Ausdrücke auf (rechte Seiten)
	 * @return	Liste aller Ausdrücke
	 */
	public String[] getExpressions() {
		final String[] arr=expressions.toArray(new String[0]);
		return Arrays.copyOf(arr,arr.length);
	}

	/**
	 * Stellt eine neue Liste mit Variablen und Ausdrücken ein.
	 * @param variables	Neue Variablenliste
	 * @param expressions	Neue Ausdrucksliste
	 */
	public void setData(final String[] variables, final String[] expressions) {
		this.variables.clear();
		this.expressions.clear();

		for (int i=0;i<Math.min(variables.length,expressions.length);i++) {
			this.variables.add(variables[i]);
			this.expressions.add(expressions[i]);
		}
	}

	/**
	 * Liefert die Liste der Zuweisungen für die Ausgabe in Form einer Beschreibung
	 * @return	Liste der Zuweisungen
	 */
	public List<String> getDescription() {
		final List<String> list=new ArrayList<>();

		for (int i=0;i<variables.size();i++) {
			final StringBuilder sb=new StringBuilder();
			sb.append(variables.get(i));
			sb.append(":=");
			switch (expressions.get(i)) {
			case ModelElementSetRecord.SPECIAL_WAITING:
				sb.append(Language.tr("ModelDescription.Set.WaitingTime"));
				break;
			case ModelElementSetRecord.SPECIAL_TRANSFER:
				sb.append(Language.tr("ModelDescription.Set.TransferTime"));
				break;
			case ModelElementSetRecord.SPECIAL_PROCESS:
				sb.append(Language.tr("ModelDescription.Set.ProcessTime"));
				break;
			case ModelElementSetRecord.SPECIAL_RESIDENCE:
				sb.append(Language.tr("ModelDescription.Set.ResidenceTime"));
				break;
			default:
				sb.append(expressions.get(i));
				break;
			}
			list.add(sb.toString());
		}

		return list;
	}

	/**
	 * Sucht einen Text in den Daten der Variablenzuweisung.
	 * @param searcher	Such-System
	 * @param station	Station an der dieser Datensatz verwendet wird
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher, final ModelElementBox station) {
		for (int i=0;i<variables.size();i++) {
			final int index=i;
			final String name=variables.get(index);
			final String expression=expressions.get(index);
			searcher.testString(station,Language.tr("Editor.DialogBase.Search.Set.Variable.Name"),name,newVariableName->variables.set(index,newVariableName));
			if (expression.equals(SPECIAL_WAITING)) continue;
			if (expression.equals(SPECIAL_TRANSFER)) continue;
			if (expression.equals(SPECIAL_PROCESS)) continue;
			if (expression.equals(SPECIAL_RESIDENCE)) continue;
			searcher.testString(station,String.format(Language.tr("Editor.DialogBase.Search.Set.Variable.Expression"),name),expression,newExpression->expressions.set(index,newExpression));
		}
	}
}
