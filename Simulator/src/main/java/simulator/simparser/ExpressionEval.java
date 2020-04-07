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
	private String condition;
	private final ExpressionCalc calcLeft;
	private final ExpressionCalc calcRight;
	private boolean calcLeftIsConst;
	private boolean calcRightIsConst;
	private double calcLeftConst;
	private double calcRightConst;
	private boolean okWhenLess, okWhenEqual, okWhenMore;

	/**
	 * Konstruktor der Klasse <code>ExpressionEval</code>
	 * @param variables	Liste mit den Variablennamen, die erkannt werden sollen (kann auch <code>null</code> sein)
	 */
	public ExpressionEval(final String[] variables) {
		condition="";
		calcLeft=new ExpressionCalc(variables);
		calcRight=new ExpressionCalc(variables);
	}

	private String[] split(final String condition, final String compareOperator) {
		int i=condition.indexOf(compareOperator);
		if (i==-1) return null;
		final String left=condition.substring(0,i);
		final String right=condition.substring(i+compareOperator.length());
		return new String[]{left,compareOperator,right};
	}

	private static final String[] compareOperators=new String[]{"<=","=<","=>",">=","!=","<>","==","<",">"};
	private static final boolean[] compareLess=new boolean[]{true,true,false,false,true,true,false,true,false};
	private static final boolean[] compareEqual=new boolean[]{true,true,true,true,false,false,true,false,false};
	private static final boolean[] compareMore=new boolean[]{false,false,true,true,true,true,false,false,true};

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
			final Double Dleft=calcLeft.calc(variableValues,simData,client);
			if (Dleft==null) {
				simData.calculationErrorEval(calcLeft);
				return false;
			}
			left=Dleft;
		}
		if (calcRightIsConst) {
			right=calcRightConst;
		} else {
			final Double Dright=calcRight.calc(variableValues,simData,client);
			if (Dright==null) {
				simData.calculationErrorEval(calcRight);
				return false;
			}
			right=Dright;
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
	 * @return	Liefert -1, wenn der Ausdruck erfolgreich interpretiert werden konnte, ansonsten die 0-basierende Fehlerstelle innerhalb des Strings.
	 */
	public static int check(final String condition, final String[] variables) {
		final ExpressionEval eval=new ExpressionEval(variables);
		return eval.parse(condition);
	}
}
