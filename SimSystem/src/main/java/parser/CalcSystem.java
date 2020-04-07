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
package parser;

import java.util.List;

import mathtools.NumberTools;
import parser.coresymbols.CalcSymbol;
import parser.coresymbols.CalcSymbolMiddleOperator;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.coresymbols.CalcSymbolVariable;

/**
 * Formel-Praser
 * @author Alexander Herzog
 * @version 4.0
 */
public class CalcSystem extends CalcSystemBase {
	/**
	 * Wurzelknoten in dem Baum der Rechensymbole
	 */
	protected CalcSymbol root;

	/**
	 * Cache für positive Double-Werte, um das Boxing/Unboxing zu vermeiden
	 * @see CalcSymbol
	 * @see NumberTools#fastPositiveResults
	 */
	public static final Double[] fastPositiveResults=NumberTools.fastPositiveResults;

	/**
	 * Cache für negative Double-Werte, um das Boxing/Unboxing zu vermeiden
	 * @see CalcSymbol
	 * @see NumberTools#fastNegativeResults
	 */
	public static final Double[] fastNegativeResults=NumberTools.fastNegativeResults;

	/**
	 * Cache für Double-Werte, die positive Dezimalbrüche darstellen, um das Boxing/Unboxing zu vermeiden
	 * @see CalcSymbol
	 * @see NumberTools#fastPositiveFractionalResults
	 */
	public static final Double[] fastPositiveFractionalResults=NumberTools.fastPositiveFractionalResults;

	/**
	 * Erstellt eine Liste mit allen verfügbaren Symbolen.<br>
	 * Diese Funktion wird vom Konstruktor aufgerufen und auch nur von diesem verwendet.
	 * @return	Liste mit allen Symbolen
	 */
	protected CalcSymbolList getCalcSymbolList() {
		return new CalcSymbolList(this.variables) {
			@Override
			protected List<CalcSymbolPreOperator> getUserFunctions() {return CalcSystem.this.getUserFunctions();}
		};
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 */
	public CalcSystem() {
		super();
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 * @param text	Zu berechnender Ausdruck
	 */
	public CalcSystem(final String text) {
		super(text);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystem(final String text, final String[] variables) {
		super(text,variables);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystem(final String[] variables) {
		super(variables);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 * @param values	Liste der Werte, die zu den Variablennamen gehören sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
	 */
	public CalcSystem(final List<String> variables, List<Double> values) {
		super(variables,values);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 * @param values	Liste der Werte, die zu den Variablennamen gehören sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
	 */
	public CalcSystem(final String text, final List<String> variables, List<Double> values) {
		super(text,variables,values);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystem(final List<String> variables) {
		super(variables);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystem</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystem(final String text, final List<String> variables) {
		super(text,variables);
	}

	/**
	 * Liefert eine Liste mit zusätzlichen, nicht bereits in {@link CalcSymbolList} enthaltenen Symbolen
	 * @return	Liste mit zusätzlichen Symbolen
	 */
	protected List<CalcSymbolPreOperator> getUserFunctions() {
		return null;
	}

	/**
	 * Versucht den übergebenen übergebenen Ausdruck zu interpretieren, dabei werden
	 * Variablennamen erkannt usw., es wird aber noch kein konkreter Wert berechnet.
	 * @param text	Ausdruck, der verarbeitet werden soll
	 * @return	Gibt <code>-1</code> zurück, wenn der Ausdruck verarbeitet werden konnte.
	 */
	@Override
	public int parse(String text) {
		this.text=text;
		root=null;
		unsetPlainNumber();
		if (text==null || text.isEmpty()) return 0;

		final CalcParser parser=new CalcParser(getCalcSymbolList());

		Double D=parser.isNumber(text);
		if (D!=null) {setPlainNumber(D); root=null; return -1;}

		Object obj=parser.parse(text);
		if (obj instanceof CalcSymbol) {
			root=(CalcSymbol)obj;
		} else {
			return (Integer)obj;
		}

		Object o=root.getSimplify();
		if (o instanceof CalcSymbol) root=(CalcSymbol)o;
		if (o instanceof Double) {setPlainNumber((Double)o); root=null;}

		return -1;
	}

	private Double plainNumberCache=null;

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param variableValues	Liste mit den Werten der Variablen
	 * @return	Gibt im Fehlerfall <code>null</code> zurück, sonst den Zahlenwert des Ergebnisses.
	 */
	@Override
	public Double calc(double[] variableValues) {
		if (isConstValue()) {
			final double constValue=getConstValue();
			if (plainNumberCache==null || plainNumberCache.doubleValue()!=constValue) plainNumberCache=constValue;
			return plainNumberCache;
		}
		if (root==null) return null;
		if (variableValues!=null) values=variableValues;
		return root.getValue(this);
	}

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param	variableValues	Liste mit den Werten der Variablen
	 * @param	fallbackValue	Wert, der zurückgegeben werden soll, wenn der Ausdruck nicht berechnet werden konnte
	 * @return	Berechneter Wert oder im Fehlerfall der Vorgabewert
	 */
	@Override
	public double calcOrDefault(double[] variableValues, final double fallbackValue) {
		if (isConstValue()) return getConstValue();
		if (root==null) return fallbackValue;
		if (variableValues!=null) values=variableValues;

		if (root instanceof CalcSymbolVariable) {
			if (!((CalcSymbolVariable)root).getValueDirectOk(this)) return fallbackValue;
			return ((CalcSymbolVariable)root).getValueDirect(this);
		}

		if (root instanceof CalcSymbolMiddleOperator) {
			return ((CalcSymbolMiddleOperator)root).getValueOrDefault(this,fallbackValue);
		}

		if (root instanceof CalcSymbolPreOperator) {
			return ((CalcSymbolPreOperator)root).getValueOrDefault(this,fallbackValue);
		}

		return root.getValue(this);
	}

	/**
	 * Berechnet den Wert eines Ausdrucks ohne weitere Variablen
	 * @param text	Zu berechnender Ausdruck
	 * @return	Gibt im Fehlerfall <code>null</code> zurück, sonst den Zahlenwert des Ergebnisses.
	 */
	public static Double calcSimple(String text) {
		CalcSystem calc=new CalcSystem(text);
		if (calc.parse()>=0) return null;
		return calc.calc();
	}

	/**
	 * Liefert eine Liste aller dem System bekannten Symbole (einschließlich Variablennamen)
	 * @return	Liste aller bekannten Symbole
	 */
	public String[] getAllSymbolNames() {
		return getCalcSymbolList().getAllSymbolNames();
	}

	/**
	 * Prüft, ob der Name als Symbol bekannt ist (einschließlich Variablennamen)
	 * @param name	Bezeichner, bei dem geprüft werden soll, ob er als Symbol bekannt ist
	 * @return	Gibt <code>true</code> zurück, wenn der Bezeichner ein gültiges Symbol darstellt
	 */
	public boolean isKnownSymbol(final String name) {
		return getCalcSymbolList().findSymbol(name)!=null;
	}
}
