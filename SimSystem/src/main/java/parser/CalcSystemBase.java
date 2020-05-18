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

/**
 * Abstrakte Basisklasse für einen Parser mathematischer Ausdrücke.<br>
 * Diese Klasse stellt einige allgemeine Hilfsroutinen zur Verfügung.
 * In den abgeleiteten Klassen müssen dann die beiden Methoden zum
 * Parsen von Ausdrücken und zum späteren Berechnen der vorverarbeiteten
 * Daten implementiert werden.
 * @author Alexander Herzog
 * @version 1.1
 */
public abstract class CalcSystemBase implements MathParser {
	/**
	 * Übergebener, auszurechnender Ausdruck
	 */
	protected String text;

	/**
	 * Liste der Variablennamen
	 */
	public final String[] variables;

	/**
	 * Liste der Werte für die Variablen
	 * (Kann beim Erstellen der Klasse definiert werden, aber auch von der Rechenroutine überschrieben werden.)
	 */
	public double[] values;

	/**
	 * Gibt an, ob es sich bei dem Rechenobjekt um einen konstanten Ausdruck handelt.
	 * @see CalcSystemBase#isConstValue()
	 * @see CalcSystemBase#getConstValue()
	 * @see CalcSystemBase#setPlainNumber(double)
	 * @see CalcSystemBase#unsetPlainNumber()
	 */
	private boolean isPlainNumber;

	/**
	 * Wenn es sich bei dem Ausdruck letztendlich um einen
	 * fixen Zahlenwert handelt, so kann dieser hier hinterlegt werden.
	 * @see CalcSystemBase#isConstValue()
	 * @see CalcSystemBase#getConstValue()
	 * @see CalcSystemBase#setPlainNumber(double)
	 * @see CalcSystemBase#unsetPlainNumber()
	 */
	private double plainNumber;

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 */
	public CalcSystemBase() {
		this("",new String[0]);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 * @param text	Zu berechnender Ausdruck
	 */
	public CalcSystemBase(final String text) {
		this(text,new String[0]);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystemBase(final String text, final String[] variables) {
		this.text=(text!=null)?text:"";
		this.variables=(variables!=null)?variables:new String[0];
		this.values=new double[this.variables.length];
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystemBase(final String[] variables) {
		this("",variables);
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 * @param values	Liste der Werte, die zu den Variablennamen gehören sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
	 */
	public CalcSystemBase(final List<String> variables, List<Double> values) {
		this("",variables);
		if (values!=null) for (int i=0;i<Math.min(this.values.length,values.size());i++) this.values[i]=(values.get(i)==null)?0.0:values.get(i).doubleValue();
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 * @param values	Liste der Werte, die zu den Variablennamen gehören sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
	 */
	public CalcSystemBase(final String text, final List<String> variables, List<Double> values) {
		this(text,variables);
		if (values!=null) for (int i=0;i<Math.min(this.values.length,values.size());i++) this.values[i]=(values.get(i)==null)?0.0:values.get(i).doubleValue();
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystemBase(final List<String> variables) {
		this("",(variables==null)?new String[0]:variables.toArray(new String[0]));
	}

	/**
	 * Konstruktor der Klasse <code>CalcSystemBase</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public CalcSystemBase(final String text, final List<String> variables) {
		this(text,(variables==null)?new String[0]:variables.toArray(new String[0]));
	}

	/**
	 * Versucht den übergebenen übergebenen Ausdruck zu interpretieren, dabei werden
	 * Variablennamen erkannt usw., es wird aber noch kein konkreter Wert berechnet.
	 * @param text	Ausdruck, der verarbeitet werden soll
	 * @return	Gibt <code>-1</code> zurück, wenn der Ausdruck verarbeitet werden konnte.
	 */
	@Override
	public abstract int parse(String text);

	/**
	 * Versucht den im Konstruktor übergebenen Ausdruck zu interpretieren, dabei werden
	 * Variablennamen erkannt usw., es wird aber noch kein konkreter Wert berechnet.
	 * @return	Gibt <code>-1</code> zurück, wenn der Ausdruck verarbeitet werden konnte.
	 */
	@Override
	public int parse() {
		return parse(text);
	}

	/**
	 * Initalisierung des Rechenobjektes mit einer einfachen, konstanten Zahl
	 * @param number	Konstante Zahl, die die Calc-Routine als Ergebnis liefern soll
	 * @see CalcSystemBase#unsetPlainNumber()
	 */
	public void setPlainNumber(final double number) {
		isPlainNumber=true;
		plainNumber=number;
	}

	/**
	 * Stellt ein, dass das Rechenobjekt nicht länger eine einfache, konstante Zahl repräsentieren soll.
	 * @see CalcSystemBase#setPlainNumber(double)
	 */
	public void unsetPlainNumber() {
		isPlainNumber=false;
	}

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param variableValues	Liste mit den Werten der Variablen
	 * @return	Zahlenwert des Ergebnisses.
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	@Override
	public abstract double calc(double[] variableValues) throws MathCalcError;

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und Werte.
	 * @return	Zahlenwert des Ergebnisses.
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	@Override
	public double calc() throws MathCalcError {
		return calc(null);
	}

	/**
	 * Gibt an, ob sich die Formel auf eine konstante Zahl reduzieren lässt (also keine Variablen enthält).
	 * @return Gibt <code>true</code> zurück, wenn es sich um eine konstante Zahl handelt.
	 * @see CalcSystemBase#getConstValue()
	 */
	@Override
	public boolean isConstValue() {
		return isPlainNumber;
	}

	/**
	 * Handelt es sich bei der Formel um eine konstante Zahl, so kann diese über diese Methode abgefragt werden.
	 * @return	Konstanter Wert der Formel
	 * @see CalcSystemBase#isConstValue()
	 */
	@Override
	public double getConstValue() {
		return plainNumber;
	}

	/**
	 * Übergebener auszurechnender Ausdruck
	 * @return	Auszurechnender Ausdruck
	 */
	public String getText() {
		return text;
	}
}
