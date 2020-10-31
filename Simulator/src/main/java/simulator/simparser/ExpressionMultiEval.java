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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import language.Language;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;

/**
 * Ermöglicht es, zusammengesetzte Vergleichsausdrücke im Kontext der Simulationsdaten zu bewerten.
 * Für die Berechnung der beiden Teilausdrücke links und rechts des Vergleichsoperators
 * wird <code>ExpressionCalc</code> verwendet.
 * @author Alexander Herzog
 * @see ExpressionCalc
 */
public class ExpressionMultiEval {
	/** Ausdruck, der in dieses Auswerteobjekt geladen werden soll */
	private String condition;
	/** Liste mit den Variablennamen, die erkannt werden sollen (kann auch <code>null</code> sein) */
	private final String[] variables;
	/** Entscheidungsbaum für die Bedingung */
	private Object[] expressionTree;

	/**
	 * Konstruktor der Klasse <code>ExpressionMultiEval</code>
	 * @param variables	Liste mit den Variablennamen, die erkannt werden sollen (kann auch <code>null</code> sein)
	 */
	public ExpressionMultiEval(final String[] variables) {
		condition="";
		if (variables==null) this.variables=null; else this.variables=Arrays.copyOf(variables,variables.length);
	}

	/** Operatoren zur Verknüpfung von Teil-Vergleichen */
	private static final String[] operators=new String[]{"||","&&"};

	/** Besteht der Gesamtausdruck nur aus Oder-verknüpften Teil-Vergleichen? */
	private boolean isOnlyOr=true;
	/** Besteht der Gesamtausdruck nur aus Und-verknüpften Teil-Vergleichen? */
	private boolean isOnlyAnd=true;

	/**
	 * Zerlegt den Gesamten Ausdruck in einzelne Makro-Tokens
	 * @param condition	Zu zerlegender Ausdruck
	 * @return	Liste aus Tokens; Tokens können Teil-Vergleiche (Strings), Verknüpfungssymbole ({@link #operators}) oder Unterlisten sein
	 * @see #parseToTree(String)
	 */
	private List<Object> tokenize(final String condition) {
		List<Object> tokens=new ArrayList<>();

		StringBuilder symbol=new StringBuilder();
		int brackets=0;
		boolean inSub=false;

		for (int i=0;i<condition.length();i++) {
			char c=condition.charAt(i);

			/* Klammer auf */
			if (c=='(' || c=='[' || c=='{') {
				final String s=symbol.toString().trim();
				if (brackets==0 && (s.isEmpty() || s.equals("!"))) {
					inSub=true;
					if (s.equals("!")) tokens.add("!");
					symbol=new StringBuilder();
				} else {
					symbol.append(c);
				}
				brackets++;
				continue;
			}

			/* Klammer zu */
			if (c==')' || c==']' || c=='}') {
				if (brackets==1 && inSub) {
					final String t=symbol.toString().trim();
					if (!t.isEmpty()) tokens.add(tokenize(t)); /* Subausdruck */
					symbol=new StringBuilder();
				} else {
					symbol.append(c);
				}
				brackets--;
				continue;
			}

			/* Zeichen zu aktuellem Symbol hinzu fügen */
			symbol.append(c);

			final String s=symbol.toString();

			if (brackets==0) for (String operator: operators) {
				/* Wir haben gerade ein besonderes Symbol gelesen. */
				if (s.endsWith(operator)) {
					if (s.length()>2) {
						final String t=s.substring(0,s.length()-2).trim();
						if (!t.isEmpty()) tokens.add(t);
					}
					tokens.add(operator);
					symbol=new StringBuilder();
					break;
				}
			}
		}

		/* Letztes Symbol aufnehmen */
		final String s=symbol.toString();
		if (!s.trim().isEmpty()) tokens.add(s.trim());

		return tokens;
	}

	/**
	 * Wandelt eine Liste aus Tokens in eine Baumstruktur um
	 * @param tokens	Liste aus Tokens
	 * @return	Baumstruktur
	 * @see #parseToTree(String)
	 */
	private List<String> getParseTree(final List<Object> tokens) {
		List<String> symbols=new ArrayList<>();
		for (Object obj: tokens) {
			if (obj instanceof String) {
				symbols.add((String)obj);
				continue;
			}
			if (obj instanceof List<?>) {
				@SuppressWarnings("unchecked")
				final List<Object> subList=(List<Object>)obj;
				final List<String> sub=getParseTree(subList);
				for (String s: sub) symbols.add("  "+s);
				continue;
			}
			symbols.add(Language.tr("Simulator.Creator.UnknownParserSymbol")+": "+obj.toString());
		}
		return symbols;
	}

	/**
	 * Wandelt (hauptsächlich für Debugging-Zwecke) einen Vergleichsausdruck in eine Baumdarstellung um.
	 * @param condition	Zu interpretierender Vergleichsausdruck
	 * @return	Baumdarstellung als mehrzeiliger Text
	 */
	public String parseToTree(final String condition) {
		List<Object> tokens=tokenize(condition);
		return String.join("\n",getParseTree(tokens).toArray(new String[0]));
	}

	/**
	 * Zählt die Anzahl an Zeichen in einer Tokens-Liste
	 * @param tokens	Tokens-Liste bei der die Anzahl der Zeichen über alle Tokens gezählt werden soll
	 * @return	Anzahl an Zeichen
	 * @see #parseTokens(List)
	 */
	@SuppressWarnings("unchecked")
	private int getSubCharCount(final List<Object> tokens) {
		int count=0;
		for (Object obj: tokens) {
			if (obj instanceof String) {count+=((String)obj).length(); continue;}
			if (obj instanceof List<?>) {count+=getSubCharCount((List<Object>)obj); continue;}
		}
		return count;
	}

	/**
	 * Wandelt eine Liste aus Tokens in eine Baumstruktur um
	 * @param tokens	Liste aus Tokens
	 * @return	Baumstruktur
	 * @see #parse(String)
	 */
	private Object parseTokens(final List<Object> tokens) {
		List<Object> result=new ArrayList<>();
		int parsedChars=0;

		for (Object obj: tokens) {
			if (obj instanceof String) {
				final String str=(String)obj;
				boolean isOperator=(str.equals("!"));
				if (!isOperator) for (String operator: operators) if (operator.equals(str)) {isOperator=true; break;}
				if (isOperator) {
					isOnlyAnd=isOnlyAnd && (str.equals("&&"));
					isOnlyOr=isOnlyOr && (str.equals("||"));
					result.add(str);
				} else {
					if (tokens.size()==1) {
						ExpressionEval eval=new ExpressionEval(variables);
						final int error=eval.parse(str);
						if (error>=0) return parsedChars+error;
						result.add(eval);
					} else {
						ExpressionMultiEval eval=new ExpressionMultiEval(variables);
						final int error=eval.parse(str);
						if (error>=0) return parsedChars+error;
						if (eval.isSingleEval()!=null) result.add(eval.isSingleEval()); else result.add(eval);
					}
				}
				parsedChars+=str.length();
				continue;
			}
			if (obj instanceof List<?>) {
				parsedChars++;
				@SuppressWarnings("unchecked")
				final List<Object> subList=(List<Object>)obj;
				final Object o=parseTokens(subList);
				if (o instanceof Integer) {
					return parsedChars+((Integer)o);
				}
				result.add(o);
				parsedChars+=getSubCharCount(subList);
				parsedChars++;
				continue;
			}
			return parsedChars; /* unbekanntes Symbol */
		}

		return result;
	}

	/**
	 * Besteht der gesamte Ausdruck nur aus einem Vergleich
	 * (also ohne Verwendung von {@link #operators})?
	 * @return	Liefert, wenn es sich um einen einzelnen Vergleich handelt, diesen zurück, sonst <code>null</code>.
	 * @see #parseTokens(List)
	 */
	private ExpressionEval isSingleEval() {
		if (expressionTree==null || expressionTree.length!=1) return null;
		if (expressionTree[0] instanceof ExpressionEval) return (ExpressionEval)expressionTree[0]; else return null;
	}

	/**
	 * Lädt einen Vergleichsausdruck und prüft ihn
	 * @param condition	Vergleichsausdruck, der in dieses Auswerteobjekt geladen werden soll
	 * @return	Liefert -1, wenn der Vergleichsausdruck erfolgreich geladen werden konnte, ansonsten die 0-basierende Fehlerstelle innerhalb des Strings.
	 */
	@SuppressWarnings("unchecked")
	public int parse(final String condition) {
		this.condition=condition;
		if (condition==null || condition.isEmpty()) return 0;
		List<Object> tokens=tokenize(condition);
		Object obj=parseTokens(tokens);
		if (obj instanceof Integer) return (Integer)obj;
		if (obj instanceof List<?>) {
			expressionTree=((List<Object>)obj).toArray(new Object[0]);
			return -1;
		}
		return 0;
	}

	/**
	 * Liefert den Text der Bedingung zurück
	 * @return	Bedingung als Text (wie <code>parse</code> übergeben)
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * Prüft die Bedingung und gibt an, ob diese erfüllt ist
	 * @param tree	Baumstruktur die die einzelnen Teil-Vergleiche inkl. ihrer Verknüpfungen enthält
	 * @param variableValues	Variablenbelegung
	 * @param simData	Aktuelles Simulationsdatenobjekt
	 * @param client	Aktueller Kunde
	 * @return	Gibt <code>true</code> zurück, wenn die in dem Ausdruck formulierte Bedingung erfüllt ist.
	 */
	private boolean evalTree(final Object[] tree, final double[] variableValues, final SimulationData simData, final RunDataClient client) {
		if (tree.length==1 && (tree[0] instanceof ExpressionEval)) return ((ExpressionEval)tree[0]).eval(variableValues,simData,client);

		boolean lastIsNot=false;
		String lastOperator=null;
		boolean result=false;

		for (int i=0;i<tree.length;i++) {
			final Object obj=tree[i];

			if (obj instanceof String) {
				final String s=(String)obj;
				if (s.equals("!")) {
					lastIsNot=true;
				} else {
					lastOperator=s;
					lastIsNot=false;
				}
				continue;
			}

			boolean b=false;
			boolean bIsSet=false;

			if (obj instanceof ExpressionEval) {
				b=(((ExpressionEval)obj).eval(variableValues,simData,client))^lastIsNot;
				lastIsNot=false;
				bIsSet=true;
			}

			if (obj instanceof ExpressionMultiEval) {
				b=(((ExpressionMultiEval)obj).eval(variableValues,simData,client))^lastIsNot;
				lastIsNot=false;
				bIsSet=true;
			}

			if (obj instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Object> subList=(List<Object>)obj;
				b=evalTree(subList.toArray(new Object[0]),variableValues,simData,client)^lastIsNot;
				lastIsNot=false;
				bIsSet=true;
			}

			if (obj instanceof Object[]) {
				b=evalTree((Object[])obj,variableValues,simData,client)^lastIsNot;
				lastIsNot=false;
				bIsSet=true;
			}

			if (!bIsSet) return false;

			if (lastOperator==null) {
				result=b;
			} else {
				switch (lastOperator) {
				case "||" : result=result || b; break;
				case "&&" : result=result && b; break;
				default: return false;
				}
				lastOperator=null;
				/* Boolsche Ausdrücke wenn möglich nicht vollständig auswerten */
				if (isOnlyOr && result) return true;
				if (isOnlyAnd && !result) return false;
			}
		}

		return result;
	}

	/**
	 * Prüft die Bedingung und gibt an, ob diese erfüllt ist
	 * @param variableValues	Werte der Variablen, deren Namen im Konstruktor übergeben wurden
	 * @param simData	Simulationsdatenobjekt; wird hier <code>null</code> übergeben, so können keine Berechnungen mit Simulationsdaten durchgeführt werden
	 * @param client	Aktueller Kunde, für den die Rechnung durchgeführt werden soll (kann <code>null</code> sein)
	 * @return	Gibt <code>true</code> zurück, wenn die in dem Ausdruck formulierte Bedingung erfüllt ist.
	 */
	public boolean eval(final double[] variableValues, final SimulationData simData, final RunDataClient client) {
		if (expressionTree==null) return false;
		return evalTree(expressionTree,variableValues,simData,client);
	}

	/**
	 * Prüft direkt, ob ein als Zeichenkette angegebener Ausdruck korrekt interpretierbar ist.
	 * @param condition	Zu prüfender Ausdruck
	 * @param variables	Liste mit den Variablennamen, die erkannt werden sollen (kann auch <code>null</code> sein)
	 * @return	Liefert -1, wenn der Ausdruck erfolgreich interpretiert werden konnte, ansonsten die 0-basierende Fehlerstelle innerhalb des Strings.
	 */
	public static int check(final String condition, final String[] variables) {
		final ExpressionMultiEval eval=new ExpressionMultiEval(variables);
		return eval.parse(condition);
	}

	/**
	 * Testet ob die Bedingung immer "falsch" liefert
	 * @return	Gibt <code>true</code> zurück, wenn die Bedingung unveränderlich und unabhängig von Variablen usw. immer "falsch" ist
	 */
	public boolean isConstFalse() {
		if (expressionTree.length==1 && (expressionTree[0] instanceof ExpressionEval)) {
			return ((ExpressionEval)expressionTree[0]).isConstFalse();
		} else {
			return false;
		}
	}
}
