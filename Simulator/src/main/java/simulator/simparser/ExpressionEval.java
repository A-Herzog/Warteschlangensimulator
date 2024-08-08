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
package simulator.simparser;

import parser.MathCalcError;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Ermöglicht es, Vergleichsausdrücke im Kontext der Simulationsdaten zu bewerten.
 * Für die Berechnung der beiden Teilausdrücke links und rechts des Vergleichsoperators
 * wird <code>ExpressionCalc</code> verwendet.
 * @author Alexander Herzog
 * @see ExpressionCalc
 */
public class ExpressionEval {
	/** Ausdruck, der in dieses Auswerteobjekt geladen werden soll */
	private String condition;

	/** Linker Teilausdruck */
	private final ExpressionCalc calcLeft;
	/** Rechter Teilausdruck */
	private final ExpressionCalc calcRight;
	/** Besitzt der linke Teilausdruck einen konstanten Wert? */
	private boolean calcLeftIsConst;
	/** Besitzt der rechte Teilausdruck einen konstanten Wert? */
	private boolean calcRightIsConst;
	/** Konstanter Wert des linken Teilausdrucks (sofern er einen konstanten Wert besitzt, siehe {@link #calcLeftIsConst}) */
	private double calcLeftConst;
	/** Konstanter Wert des rechten Teilausdrucks (sofern er einen konstanten Wert besitzt, siehe {@link #calcRightIsConst}) */
	private double calcRightConst;

	/** Ist der Vergleich als erfüllt anzusehen, wenn der linke Teilausdruck kleiner als der rechte ist? */
	private boolean okWhenLess;
	/** Ist der Vergleich als erfüllt anzusehen, wenn der linke und der rechte Teilausdruck gleich groß sind? */
	private boolean okWhenEqual;
	/** Ist der Vergleich als erfüllt anzusehen, wenn der linke Teilausdruck größer als der rechte ist? */
	private boolean okWhenMore;
	/**
	 * Konstruktor der Klasse <code>ExpressionEval</code>
	 * @param variables	Liste mit den Variablennamen, die erkannt werden sollen (kann auch <code>null</code> sein)
	 * @param modelUserFunctions	Objekt mit weiteren modellspezifischen nutzerdefinierten Funktionen (kann <code>null</code> sein)
	 */
	public ExpressionEval(final String[] variables, final ExpressionCalcModelUserFunctions modelUserFunctions) {
		condition="";
		calcLeft=new ExpressionCalc(variables,modelUserFunctions);
		calcRight=new ExpressionCalc(variables,modelUserFunctions);
	}

	/**
	 * Teilt einen Ausdruck in linke Seite, Vergleichsoperator, rechte Seite auf
	 * @param condition	Aufzuteilender Ausdruck
	 * @param compareOperator	Vergleichsoperator
	 * @return	3-elementiges Array: linke Seite, Vergleichsoperator, rechte Seite
	 */
	private String[] split(final String condition, final String compareOperator) {
		int i=condition.indexOf(compareOperator);
		if (i==-1) return null;
		final String left=condition.substring(0,i);
		final String right=condition.substring(i+compareOperator.length());
		return new String[]{left,compareOperator,right};
	}

	/**
	 * Mögliche Vergleichsoperatoren
	 */
	private final static String[] compareOperators=new String[]{"<=","=<","=>",">=","!=","<>","==","<",">"};

	/**
	 * Ist der Vergleich für einen bestimmten Operator aus {@link #compareOperators} erfüllt,
	 * wenn der linke Teilausdruck kleiner als der rechte ist?
	 * @see #compareOperators
	 */
	private final static boolean[] compareLess=new boolean[]{true,true,false,false,true,true,false,true,false};

	/**
	 * Ist der Vergleich für einen bestimmten Operator aus {@link #compareOperators} erfüllt,
	 * wenn der linke und der rechte Teilausdruck gleich groß sind?
	 * @see #compareOperators
	 */
	private final static boolean[] compareEqual=new boolean[]{true,true,true,true,false,false,true,false,false};

	/**
	 * Ist der Vergleich für einen bestimmten Operator aus {@link #compareOperators} erfüllt,
	 * wenn der linke Teilausdruck größer als der rechte ist?
	 * @see #compareOperators
	 */
	private final static boolean[] compareMore=new boolean[]{false,false,true,true,true,true,false,false,true};

	/**
	 * Lädt einen Ausdruck und prüft ihn
	 * @param condition	Ausdruck, der in dieses Auswerteobjekt geladen werden soll
	 * @return	Liefert -1, wenn der Ausdruck erfolgreich geladen werden konnte, ansonsten die 0-basierende Fehlerstelle innerhalb des Strings.
	 */
	public int parse(final String condition) {
		if (condition==null || condition.isEmpty()) return 0;
		this.condition=condition;
		for (int i=0;i<compareOperators.length;i++) {
			String[] parts=split(condition,compareOperators[i]);
			if (parts!=null) {
				final int errorLeft=calcLeft.parse(parts[0]);
				if (errorLeft>=0) return errorLeft;
				final int errorRight=calcRight.parse(parts[2]);
				if (errorRight>=0) return parts[0].length()+parts[1].length()+errorRight;
				okWhenLess=compareLess[i];
				okWhenEqual=compareEqual[i];
				okWhenMore=compareMore[i];
				if (calcLeft.isConstValue()) {
					calcLeftIsConst=true;
					calcLeftConst=calcLeft.getConstValue();
				}
				if (calcRight.isConstValue()) {
					calcRightIsConst=true;
					calcRightConst=calcRight.getConstValue();
				}
				return -1;
			}
		}
		return 0;
	}

	/**
	 * Prüft die Bedingung und gibt an, ob diese erfüllt ist
	 * @param variableValues	Werte der Variablen, deren Namen im Konstruktor übergeben wurden
	 * @param simData	Simulationsdatenobjekt; wird hier <code>null</code> übergeben, so können keine Berechnungen mit Simulationsdaten durchgeführt werden
	 * @param client	Aktueller Kunde, für den die Rechnung durchgeführt werden soll (kann <code>null</code> sein)
	 * @return	Gibt <code>true</code> zurück, wenn die in dem Ausdruck formulierte Bedingung erfüllt ist.
	 */
	public boolean eval(final double[] variableValues, final SimulationData simData, final RunDataClient client) {
		double left;
		double right;

		if (calcLeftIsConst) {
			left=calcLeftConst;
		} else {
			try {
				left=calcLeft.calc(variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorEval(calcLeft);
				return false;
			}
		}
		if (calcRightIsConst) {
			right=calcRightConst;
		} else {
			try {
				right=calcRight.calc(variableValues,simData,client);
			} catch (MathCalcError e) {
				simData.calculationErrorEval(calcRight);
				return false;
			}
		}

		if (left<right) return okWhenLess;
		if (left>right) return okWhenMore;
		return okWhenEqual;
	}

	/**
	 * Liefert den Text der Bedingung zurück
	 * @return	Bedingung als Text (wie <code>parse</code> übergeben)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Prüft direkt, ob ein als Zeichenkette angegebener Ausdruck korrekt interpretierbar ist.
	 * @param condition	Zu prüferender Ausdruck
	 * @param variables	Liste mit den Variablennamen, die erkannt werden sollen (kann auch <code>null</code> sein)
	 * @param modelUserFunctions	Objekt mit weiteren modellspezifischen nutzerdefinierten Funktionen (kann <code>null</code> sein)
	 * @return	Liefert -1, wenn der Ausdruck erfolgreich interpretiert werden konnte, ansonsten die 0-basierende Fehlerstelle innerhalb des Strings.
	 */
	public static int check(final String condition, final String[] variables, final ExpressionCalcModelUserFunctions modelUserFunctions) {
		final ExpressionEval eval=new ExpressionEval(variables,modelUserFunctions);
		return eval.parse(condition);
	}

	/**
	 * Testet ob die Bedingung immer "falsch" liefert
	 * @return	Gibt <code>true</code> zurück, wenn die Bedingung unveränderlich und unabhängig von Variablen usw. immer "falsch" ist
	 */
	public boolean isConstFalse() {
		if (!calcLeftIsConst || !calcRightIsConst) return false;

		if (calcLeftConst<calcRightConst) return !okWhenLess;
		if (calcLeftConst>calcRightConst) return !okWhenMore;
		return !okWhenEqual;
	}
}
