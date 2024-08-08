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
package simulator.simparser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parser.coresymbols.CalcSymbolPreOperator;
import simulator.simparser.ExpressionCalcUserFunctionsManager.UserFunction;

/**
 * Hält die modellspezifischen nutzerdefinierten Funktionen vor.
 * @see ExpressionCalc
 * @see ExpressionEval
 * @see ExpressionMultiEval
 */
public class ExpressionCalcModelUserFunctions {
	/**
	 * Name des XML-Elements, das die Datensätze enthält
	 */
	public static String[] XML_NODE_NAME=new String[] {"ModellNutzerdefinierteRechenfunktion"};

	/**
	 * Liste der modellspezifischen nutzerdefinierten Rechenfunktionen
	 */
	private final List<UserFunction> userFunctions;

	/**
	 * Konstruktor
	 */
	public ExpressionCalcModelUserFunctions() {
		userFunctions=new ArrayList<>();
	}

	/**
	 * Copy-Konstruktor
	 * @param copySource	Zu kopierendes Objekt
	 *
	 */
	public ExpressionCalcModelUserFunctions(final ExpressionCalcModelUserFunctions copySource) {
		this();
		copyFrom(copySource);
	}

	/**
	 * Löscht die Liste der modellspezifischen nutzerdefinierten Rechenfunktionen.
	 */
	public void clear() {
		userFunctions.clear();
	}

	/**
	 * Liefert die Liste der modellspezifischen nutzerdefinierten Rechenfunktionen.
	 * @return	Liste der modellspezifischen nutzerdefinierten Rechenfunktionen
	 */
	public List<UserFunction> getUserFunctions() {
		return userFunctions;
	}

	/**
	 * Liefert eine Liste mit den Rechenausdrücken der modellspezifischen nutzerdefinierten Rechenfunktionen.
	 * @return	Liste mit den Rechenausdrücken der modellspezifischen nutzerdefinierten Rechenfunktionen.
	 */
	public List<CalcSymbolPreOperator> getCalcFunctions() {
		return userFunctions.stream().map(userFunction->userFunction.compile()).filter(obj->(obj instanceof CalcSymbolPreOperator)).map(obj->(CalcSymbolPreOperator)obj).collect(Collectors.toList());
	}

	/**
	 * Löscht die aktuelle Liste und kopiert die Daten aus einem anderen Objekt.
	 * @param copySource	Objekt aus dem die Daten in Kopie übernommen werden sollen.
	 */
	public void copyFrom(final ExpressionCalcModelUserFunctions copySource) {
		clear();
		if (copySource==null) return;
		copySource.userFunctions.stream().map(UserFunction::new).forEach(userFunctions::add);
	}

	/**
	 * Vergleicht den Inhalt dieses Objektes mit einem anderen.
	 * @param otherModelUserFunctions	Anderes modellspezifische nutzerdefinierte Rechenfunktionen-Objekt zum Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsUserFunctions(final ExpressionCalcModelUserFunctions otherModelUserFunctions) {
		if (otherModelUserFunctions==null) return false;
		if (userFunctions.size()!=otherModelUserFunctions.userFunctions.size()) return false;
		for (int i=0;i<userFunctions.size();i++) if (!userFunctions.get(i).equalsUserFunction(otherModelUserFunctions.userFunctions.get(i))) return false;
		return true;
	}

	/**
	 * Versucht die Informationen zu den Rechenfunktionen aus einem xml-Element zu laden
	 * @param node	XML-Element, aus dem die Daten geladen werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		final Object obj=UserFunction.loadFromXML(node);
		if (obj instanceof String) return (String)obj;
		userFunctions.add((UserFunction)obj);
		return null;
	}

	/**
	 * Speichert die Informationen zu den Rechenfunktionen in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		for (var userFunction: userFunctions) {
			final Element node=doc.createElement(XML_NODE_NAME[0]);
			parent.appendChild(node);
			userFunction.saveToXML(node);
		}
	}
}
