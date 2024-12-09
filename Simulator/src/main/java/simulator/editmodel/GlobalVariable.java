/**
 * Copyright 2024 Alexander Herzog
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
package simulator.editmodel;

import org.w3c.dom.Element;

import language.Language;

/**
 * Objekte dieser Klasse halten Daten zu jeweils einer globalen Variablenzuweisung vor.
 * @see EditModel#globalVariables
 */
public class GlobalVariable {
	/**
	 * Name der Variable
	 */
	private String name;

	/**
	 * Zugehöriger Rechenausdruck
	 */
	private String expression;

	/**
	 * Soll beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden?
	 */
	private boolean askForValueOnStart;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name der Variable
	 * @param expression	Zugehöriger Rechenausdruck
	 * @param askForValueOnStart	Soll beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden?
	 */
	public GlobalVariable(final String name, final String expression, final boolean askForValueOnStart) {
		this.name=(name==null)?"":name;
		this.expression=(expression==null)?"":expression;
		this.askForValueOnStart=askForValueOnStart;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name der Variable
	 * @param expression	Zugehöriger Rechenausdruck
	 */
	public GlobalVariable(final String name, final String expression) {
		this(name,expression,false);
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param source	Zu kopierendes Ausgangselement
	 */
	public GlobalVariable(final GlobalVariable source) {
		this.name=(source==null)?"":source.name;
		this.expression=(source==null)?"":source.expression;
		this.askForValueOnStart=(source==null)?false:source.askForValueOnStart;
	}

	/**
	 * Konstruktor der Klasse
	 * @param xmlNode	XML-Knoten aus dem die Daten geladen werden sollen
	 */
	private GlobalVariable(final Element xmlNode) {
		name=Language.trAllAttribute("Surface.XML.GlobalVariable.Name",xmlNode).trim();
		expression=xmlNode.getTextContent().trim();
		final String askForValueOnStartString=Language.trAllAttribute("Surface.XML.GlobalVariable.AskForValueOnStart",xmlNode);
		askForValueOnStart=(askForValueOnStartString.equals("1"));
		assert(this.name!=null);
	}

	/**
	 * Lädt die Daten aus einem XML-Knoten und liefert ein {@link GlobalVariable}-Objekt zurück,
	 * wenn die Daten korrekt interpretiert werden konnten (sonst <code>null</code>).
	 * @param xmlNode	XML-Knoten aus dem die Daten geladen werden sollen
	 * @return	Im Erfolgsfall ein {@link GlobalVariable}-Objekt, sonst <code>null</code>
	 */
	public static GlobalVariable loadFromXML(final Element xmlNode) {
		final GlobalVariable globalVariable=new GlobalVariable(xmlNode);
		if (globalVariable.name.isBlank()) return null;
		return globalVariable;
	}

	/**
	 * Speichert die Daten des Objektes in einem Unterknoten des übergebenen XML-Knotens
	 * @param parent	Übergeordneter XML-Knoten
	 */
	public void saveToXML(final Element parent) {
		final Element sub;
		parent.appendChild(sub=parent.getOwnerDocument().createElement(Language.trPrimary("Surface.XML.GlobalVariable")));
		sub.setAttribute(Language.trPrimary("Surface.XML.GlobalVariable.Name"),name);
		if (askForValueOnStart) sub.setAttribute(Language.trPrimary("Surface.XML.GlobalVariable.AskForValueOnStart"),"1");
		sub.setTextContent(expression);
	}

	/**
	 * Liefert den Namen der Variable.
	 * @return	Name der Variable
	 */
	public String getName() {
		return name;
	}

	/**
	 * Stellt den Namen der Variable ein.
	 * @param name	Name der Variable
	 */
	public void setName(final String name) {
		this.name=(name==null)?"":name;
	}

	/**
	 * Liefert den zugehörigen Rechenausdruck.
	 * @return	Zugehöriger Rechenausdruck
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Stellt den zugehörigen Rechenausdruck ein.
	 * @param expression	Zugehöriger Rechenausdruck
	 */
	public void setExpression(final String expression) {
		this.expression=(expression==null)?"":expression;
	}

	/**
	 * Liefert die Information, ob beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden soll.
	 * @return	Soll beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden?
	 */
	public boolean isAskForValueOnStart() {
		return askForValueOnStart;
	}

	/**
	 * Stellt ein, ob beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden soll.
	 * @param askForValueOnStart	Soll beim Start der Simulation/Animation nach einem Update des Wertes gefragt werden?
	 */
	public void setAskForValueOnStart(final boolean askForValueOnStart) {
		this.askForValueOnStart=askForValueOnStart;
	}

	/**
	 * Vergleicht dieses Objekt mit einem anderen {@link GlobalVariable}-Objekt.
	 * @param otherGlobalVariable	Zweites {@link GlobalVariable}-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsGlobalVariable(final GlobalVariable otherGlobalVariable) {
		if (otherGlobalVariable==null) return false;
		if (!name.equals(otherGlobalVariable.name)) return false;
		if (!expression.equals(otherGlobalVariable.expression)) return false;
		if (askForValueOnStart!=otherGlobalVariable.askForValueOnStart) return false;
		return true;
	}
}
