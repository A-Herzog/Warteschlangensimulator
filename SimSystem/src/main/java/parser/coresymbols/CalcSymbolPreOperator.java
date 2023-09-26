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
package parser.coresymbols;

import parser.CalcSystem;
import parser.MathCalcError;

/**
 * Abstrakte Basisklasse für Funktionen (d.h. vorangestellte Operatoren)
 * @author Alexander Herzog
 */
public abstract class CalcSymbolPreOperator extends CalcSymbolFunction {
	/**
	 * Statisches Pseudo-Symbol für "leere Parametermenge"
	 * @see #symbols
	 */
	private static final CalcSymbol[] emptyParameters=new CalcSymbol[0];

	/**
	 * Gibt an, ob Parameter eingestellt wurden
	 */
	protected boolean parametersSet=false;

	/**
	 * Parameter der Funktion
	 */
	protected CalcSymbol[] symbols=emptyParameters;

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolPreOperator() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public final SymbolType getType() {
		return CalcSymbol.SymbolType.TYPE_FUNCTION;
	}

	@Override
	public final int getPriority() {
		return parametersSet?0:10;
	}

	@Override
	public final boolean setParameter(CalcSymbol[] symbols) {
		if (symbols==null) return false;

		if (symbols.length==1 && symbols[0] instanceof CalcSymbolSub) {
			this.symbols=((CalcSymbolSub)(symbols[0])).getData();
		} else {
			this.symbols=symbols;
		}
		parametersSet=true;
		return true;
	}

	/**
	 * Direkter Zugriff auf das Rechensystem.<br>
	 * Steht nach dem ersten Aufruf von {@link CalcSymbolPreOperator#getValue(CalcSystem)}
	 * oder {@link CalcSymbolPreOperator#getValueOrDefault(CalcSystem, double)} zur Verfügung.
	 */
	protected CalcSystem calcSystem;

	/**
	 * Versucht die Funktion zu berechnen, wenn die Zahlenwerte der Parameter bekannt sind
	 * @param parameters 	Zahlenwerte der Parameter
	 * @return	Ergebnis
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	protected abstract double calc(final double[] parameters) throws MathCalcError;

	/**
	 * Versucht die Funktion zu berechnen, wenn die Zahlenwerte der Parameter bekannt sind
	 * @param parameters 	Zahlenwerte der Parameter
	 * @param fallbackValue	Vorgabewert, der zurückgeliefert werden soll, wenn die Berechnung nicht ausgeführt werden konnte
	 * @return	Liefert im Erfolgsfall das Ergebnis der Berechnung, sonst den angegebenen Vorgabewert
	 */
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		try {
			return calc(parameters);
		} catch (MathCalcError e) {
			return fallbackValue;
		}
	}

	/**
	 * Array mit den Werten der Parameter<br>
	 * Die Werte werden nicht wiederverwendet, aber wenn möglich wird das Array
	 * nur einmal angelegt.
	 */
	private double[] lastValues;

	/**
	 * Gibt an, ob es sich bei allen Parametern der Funktion um Konstanten handelt.
	 * @see #getParameterValues(CalcSystem)
	 */
	protected boolean allValuesConst=false;

	/**
	 * Berechnet die Werte der Parameter der Funktion
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @return	Array mit den Werten der Parameter
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	protected final double[] getParameterValues(final CalcSystem calc) throws MathCalcError {
		double[] values;
		if (lastValues==null || lastValues.length!=symbols.length) {
			values=new double[symbols.length];
			lastValues=values;
			allValuesConst=false;
		} else {
			values=lastValues;
		}

		if (allValuesConst) return values;

		allValuesConst=true;
		for (int i=0;i<symbols.length;i++) {
			final CalcSymbol symbol=symbols[i];
			if (symbol instanceof CalcSymbolConst) {
				values[i]=((CalcSymbolConst)symbol).getValue();
			} else {
				allValuesConst=false;
				values[i]=symbol.getValue(calc);
			}
		}
		return values;
	}

	@Override
	public double getValue(final CalcSystem calc) throws MathCalcError {
		calcSystem=calc;

		if (symbols==null) throw error();

		final double[] values=getParameterValues(calc);
		return calc(values);
	}

	/**
	 * Liefert den Wert des Symbols
	 * @param calc	Rechensystem (zum Abfragen der aktuellen Werte von Variablen usw.)
	 * @param fallbackValue	Vorgabewert, der zurückgeliefert werden soll, wenn die Berechnung nicht ausgeführt werden konnte
	 * @return	Aktueller Wert des Symbols oder Vorgabewert, wenn der Wert nicht berechnet werden konnte
	 * @see CalcSymbol#getValue(CalcSystem)
	 * @see CalcSystem#calcOrDefault(double[], double)
	 */
	public double getValueOrDefault(final CalcSystem calc, final double fallbackValue) {
		calcSystem=calc;

		if (symbols==null) return fallbackValue;

		double[] values;
		if (lastValues==null || lastValues.length!=symbols.length) {
			values=new double[symbols.length];
			lastValues=values;
		} else {
			values=lastValues;
		}

		if (!allValuesConst) {
			allValuesConst=true;
			for (int i=0;i<symbols.length;i++) {
				final CalcSymbol symbol=symbols[i];
				if (symbol instanceof CalcSymbolConst) {
					values[i]=((CalcSymbolConst)symbol).getValue();
				} else {
					allValuesConst=false;
					try {
						values[i]=symbol.getValue(calc);
					} catch (MathCalcError e) {
						return fallbackValue;
					}
				}
			}
		}
		return calcOrDefault(values,fallbackValue);
	}

	@Override
	public final CalcSymbol cloneSymbol() {
		CalcSymbolPreOperator clone=(CalcSymbolPreOperator)super.cloneSymbol();
		clone.symbols=new CalcSymbol[symbols.length];
		for (int i=0;i<symbols.length;i++) if (symbols[i]!=null) clone.symbols[i]=symbols[i].cloneSymbol();
		return clone;
	}

	/**
	 * Versucht das Unterelement für die Parameter zu vereinfachen.
	 * @return	Liefert im Erfolgsfall das neue Unterelement, sonst <code>null</code>
	 * @see #getSimplify()
	 */
	private Double getSimpleConstSub() {
		if (symbols.length!=1 || symbols[0]==null) return null;
		final Object subSimple=symbols[0].getSimplify();
		if (subSimple instanceof Double) return (Double)subSimple;
		if (subSimple instanceof CalcSymbolConst) return ((CalcSymbolConst)subSimple).getValue();
		return null;
	}

	/**
	 * Gibt an, ob die Funktion als solches deterministisch ist, also bei gleichen Parametern auch
	 * stets dasselbe Ergebnis liefert (was z.B. bei "Random()" nicht der Fall ist). Wenn dies der
	 * Fall ist und die Parameter konstante Werte sind, wird die gesamte Funktion durch
	 * {@link CalcSymbolPreOperator#getSimplify()} zu einem konstanten Wert vereinfacht.
	 * @return	Gibt <code>true</code> zurück, wenn die Funktion deterministisch arbeitet. (Dies ist der Normalfall. Nur für nichtdeterministische Funktionen muss diese Methode überschrieben werden.)
	 */
	protected boolean isDeterministic() {
		return true;
	}

	@Override
	public Object getSimplify() {
		if (isDeterministic()) {
			final Double sub=getSimpleConstSub();
			if (sub!=null) {
				try {
					return calc(new double[]{sub.doubleValue()});
				} catch (MathCalcError e) {
					/* Unten weiter versuchen */
				}
			}
		}

		boolean allConst=true;
		Object[] obj=new Object[symbols.length];
		double[] consts=new double[symbols.length];
		for (int i=0;i<obj.length;i++) {
			obj[i]=symbols[i].getSimplify();
			if (obj[i] instanceof Double) {
				final CalcSymbolNumber number=new CalcSymbolNumber();
				number.setValue(((Double)obj[i]));
				consts[i]=((Double)obj[i]).doubleValue();
				obj[i]=number;
			} else {
				allConst=false;
			}
			if (!(obj[i] instanceof CalcSymbol)) return this;
		}

		try {
			final CalcSymbolPreOperator clone=(CalcSymbolPreOperator)clone();
			clone.symbols=new CalcSymbol[obj.length];
			for (int i=0;i<obj.length;i++) clone.symbols[i]=(CalcSymbol)obj[i];
			clone.parametersSet=parametersSet;
			if (isDeterministic() && allConst) { /* Wenn möglich, Wert direkt berechnen */
				try {
					return clone.calc(consts);
				} catch (MathCalcError e) {
				/* Dann nur vereinfacht und nicht ausgerechnet zurückgeben. */				}
			}
			return clone;
		} catch (CloneNotSupportedException e) {return this;}
	}
}
