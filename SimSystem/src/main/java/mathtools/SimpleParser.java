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
package mathtools;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import parser.CalcSystemBase;
import parser.MathCalcError;

/**
 * Einfacher Formel-Praser
 * @author Alexander Herzog
 * @version 2.3
 */
public class SimpleParser extends CalcSystemBase {
	/**
	 * Wurzelelement des geparsten Ausdrucks
	 * @see #parse(String)
	 */
	private Symbol root;

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 */
	public SimpleParser() {
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param text	Zu berechnender Ausdruck
	 */
	public SimpleParser(final String text) {
		super(text);
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public SimpleParser(final String text, final String[] variables) {
		super(text,variables);
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public SimpleParser(final String[] variables) {
		super(variables);
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 * @param values	Liste der Werte, die zu den Variablennamen gehören sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
	 */
	public SimpleParser(final List<String> variables, final List<Double> values) {
		super(variables,values);
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 * @param values	Liste der Werte, die zu den Variablennamen gehören sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
	 */
	public SimpleParser(final String text, final List<String> variables, final List<Double> values) {
		super(text,variables,values);
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public SimpleParser(final List<String> variables) {
		super(variables);
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 */
	public SimpleParser(final String text, final List<String> variables) {
		super(text,variables);
	}

	/**
	 * Zerlegt den gesamten Text in einzelne Tokens
	 * @param text	Text
	 * @return	Liste mit Tokens
	 * @see #parse(String)
	 */
	private List<Object> getTokens(final String text) {
		boolean inNumberFracPart=false;

		String token="";
		final List<Object> tokens=new ArrayList<>();

		int i=0;

		final int mode_symbol=0;
		final int mode_number=1;
		final int mode_text=2;
		final int mode_sub=3;
		final int mode_done=4;
		final int mode_empty=5;

		int mode=-1,lastMode=-1;
		while (true) {

			/* Nächstes Zeichen holen und vorsortieren */
			char c=(i>=text.length())?0:text.charAt(i);
			lastMode=mode;
			mode=mode_text;
			if (i>=text.length()) mode=mode_done; else {
				if ((c>='0' && c<='9') || c==',' || c=='.') mode=mode_number;
				if (c=='+' || c=='-' || c=='*' || c=='/' || c==':' || c=='^' || c=='%' || c=='²' || c=='³') mode=mode_symbol;
				if (c=='(' || c=='[' || c=='{' || c==')' || c==']' || c=='}') mode=mode_sub;
				if (c==' ') mode=mode_empty;
			}

			/* Ggf. Verarbeitung des letzten Token abschließen */
			if (lastMode==mode_number && mode!=mode_number) {
				final Double D=NumberTools.getPlainDouble(token);
				if (D==null) return null;
				tokens.add(D); inNumberFracPart=false; token="";
			}
			if (lastMode==mode_symbol) {tokens.add(token); token="";}
			if (lastMode==mode_text && mode!=mode_text) {tokens.add(token); token="";}

			/* Wenn alle Zeichen verarbeitet sind, Ende */
			if (mode==mode_done) break;

			/* Nächstes Zeichen verarbeiten */

			/* Leerzeichen */
			if (mode==mode_empty) {i++;	continue;}

			/* Ziffer */
			if (mode==mode_number) {
				if (c==',' || c=='.') {
					if (token.isEmpty()) token="0";
					if (inNumberFracPart) return null;
					inNumberFracPart=true;
				}
				token+=c;
				i++;
				continue;
			}

			/* Klammer zu (die nie aufgemacht wurde) */
			if (mode==mode_sub) {
				if (c==')' || c==']' || c=='}') {
					if (i<text.length()-1) return null;
					i++;
				} else {
					int count=1;
					int last=text.length()-1;
					for (int j=i+1;j<text.length();j++) {
						char d=text.charAt(j);
						if (d=='(' || d=='[' || d=='{') count++;
						if (d==')' || d==']' || d=='}') count--;
						if (count==0) {last=j-1; break;}
					}
					final List<Object> l=getTokens(text.substring(i+1,last+1));
					if (l==null) return null;
					tokens.add(l);
					i=last+2;
				}
				continue;
			}

			/* Symbol oder Text */
			if (mode==mode_symbol || mode==mode_text) {
				token+=c;
				i++;
				continue;
			}
		}

		return tokens;
	}

	/**
	 * Handelt es sich bei dem Symbol um eine Variable?
	 * @param symbol	Zu prüfendes Symbol
	 * @return	Liefert <code>true</code>, wenn es sich um eine Variable handelt
	 */
	private int isVariable(final String symbol) {
		if (symbol.equalsIgnoreCase("pi")) return -2;
		if (symbol.equalsIgnoreCase("e")) return -3;
		for (int i=0;i<variables.length;i++) if (variables[i].equalsIgnoreCase(symbol)) return i;
		return -1;
	}

	/**
	 * Handelt es sich bei dem Symbol um den Namen einer Funktion?
	 * @param symbol	Zu prüfendes Symbol
	 * @return	Liefert <code>true</code>, wenn es sich um den Namen einer Funktion handelt
	 */

	private boolean isFunction(final String symbol) {
		if (symbol==null || symbol.isEmpty()) return false;
		for (String sym: FUNCTION_NAMES) if (symbol.equalsIgnoreCase(sym)) return true;
		return false;
	}

	/**
	 * Fügt in die Liste der Tokens implizite gegebene, aber nicht explizit vorhandene Multiplikationen ein
	 * (z.B. wird aus "3","pi" dann "3","*","pi".
	 * @param list	Liste der Tokens, die ggf. ergänzt werden soll
	 */
	private void insertMultiplySymbols(final List<Object> list) {
		int i=1;
		while (i<list.size()) {
			Object o1=list.get(i-1);
			Object o2=list.get(i);
			boolean needMul=false;
			needMul=/*needMul || */(o1 instanceof Double && o2 instanceof Double);
			needMul=needMul || (o1 instanceof Double && o2 instanceof ArrayList);
			needMul=needMul || (o1 instanceof ArrayList && o2 instanceof Double);
			needMul=needMul || (o1 instanceof String && isVariable((String)o1)!=-1 && o2 instanceof Double);
			needMul=needMul || (o1 instanceof Double && o2 instanceof String && (isVariable((String)o2)!=-1));
			needMul=needMul || (o1 instanceof Double && o2 instanceof String && isFunction((String)o2));
			if (needMul) list.add(i,"*"); else i++;
		}
	}

	/**
	 * Wandelt ein einzelnes Token in ein Symbol um
	 * @param o	Umzuwandelndes Token
	 * @return	Rechensymbol oder <code>null</code>, wenn die Umwandlung fehlgeschlagen ist
	 * @see #buildTree(List)
	 */
	@SuppressWarnings("unchecked")
	private Symbol tokenToSymbol(final Object o) {
		if (o instanceof Double) return new NumberSymbol((Double)o);
		if (o instanceof String) {int i=isVariable((String)o); return (i!=-1)?new VariableSymbol(i):null;}
		if (o instanceof Symbol) return (Symbol)o;
		if (o instanceof ArrayList) return buildTree((ArrayList<Object>)o);
		return null;
	}

	/**
	 * Erstellt auf Basis der Tokens eine Baumstruktur des Rechenausdrucks
	 * @param list	Liste der Tokens
	 * @return	Wurzelelement der Baumstruktur oder <code>null</code>, wenn die Umwandlung fehlgeschlagen ist
	 */
	private Symbol buildTree(final List<Object> list) {
		if (list.size()>=2 && list.get(0) instanceof String && ((String)list.get(0)).equals("-")) list.add(0,0.0);
		if (list.size()==1) return tokenToSymbol(list.get(0));

		int i;

		/* Sub-Listen berechnen, Zahlen und Variablen zu Symbolen */
		for (i=0;i<list.size();i++) {Symbol s=tokenToSymbol(list.get(i)); if (s!=null) list.set(i,s);}

		/* Nachgestellte Operatoren */
		for (char sym: POST_OPERATOR_NAMES) {
			i=1;
			while (i<list.size()) {
				if (list.get(i) instanceof String && ((String)list.get(i)).equals(""+sym)) {
					if (!(list.get(i-1) instanceof Symbol)) return null;
					list.set(i-1,new OperatorSymbol(sym,(Symbol)list.get(i-1),null));
					list.remove(i);
					continue;
				}
				i++;
			}
		}

		/* Funktionen */
		for (String sym: FUNCTION_NAMES) {
			i=0;
			while (i<list.size()-1) {
				if (list.get(i) instanceof String && ((String)list.get(i)).equalsIgnoreCase(sym)) {
					if (!(list.get(i+1) instanceof Symbol)) return null;
					list.set(i,new FunctionSymbol(sym,(Symbol)list.get(i+1)));
					list.remove(i+1);
				}
				i++;
			}
		}

		/* Die zweistelligen Operatoren der Reihe nach */
		for (String sym: OPERATOR_NAMES) {
			i=list.size()-2;
			while (i>0) {
				if (list.get(i) instanceof String && ((String)list.get(i)).equals(sym)) {
					if (!(list.get(i-1) instanceof Symbol) || !(list.get(i+1) instanceof Symbol)) return null;
					list.set(i-1,new OperatorSymbol(sym.charAt(0),(Symbol)list.get(i-1),(Symbol)list.get(i+1)));
					list.remove(i+1); list.remove(i);
				}
				i--;
			}
		}

		if (list.size()!=1) return null;
		return (Symbol)list.get(0);
	}

	/**
	 * Versucht den übergebenen übergebenen Ausdruck zu interpretieren, dabei werden
	 * Variablennamen erkannt usw., es wird aber noch kein konkreter Wert berechnet.
	 * @param text	Ausdruck, der verarbeitet werden soll
	 * @return	Gibt <code>-1</code> zurück, wenn der Ausdruck verarbeitet werden konnte.
	 */
	@Override
	public int parse(String text) {
		if (text==null || text.isEmpty()) return 0;
		final List<Object> tokens=getTokens(text);
		if (tokens==null) return 0;
		insertMultiplySymbols(tokens);
		root=buildTree(tokens);
		if (root==null) return 0;
		root=root.simplify();
		if (root instanceof NumberSymbol) {setPlainNumber(((NumberSymbol)root).value); root=null;}
		return -1;
	}

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param variableValues	Liste mit den Werten der Variablen
	 * @return	Zahlenwert des Ergebnisses.
	 * @throws	MathCalcError	Fehler während der Berechnung
	 */
	@Override
	public double calc(double[] variableValues) throws MathCalcError {
		if (isConstValue()) {
			return getConstValue();
		}
		if (root==null) throw new MathCalcError(this);
		try {
			return root.calc(variableValues);
		} catch (Exception e) {
			if (e instanceof MathCalcError) throw e;
			throw new MathCalcError(e);
		}
	}

	@Override
	public double calcOrDefault(double[] variableValues, double fallbackValue) {
		if (isConstValue()) return getConstValue();
		if (root==null) return fallbackValue;
		try {
			return root.calc(variableValues);
		} catch (Exception e) {
			return fallbackValue;
		}
	}

	/**
	 * Basisklasse für alle Rechensymbole
	 * @see NumberSymbol
	 * @see VariableSymbol
	 * @see FunctionSymbol
	 */
	private abstract static class Symbol {
		/**
		 * Führt die eigentliche Berechnung durch.
		 * @param variableValues	Liste mit den Werten der Variablen.
		 * @return	Berechneter Ausdruck
		 * @throws MathCalcError	Wird ausgelöst, wenn die Berechnung fehlgeschlagen ist.
		 */
		public abstract double calc(final double[] variableValues) throws MathCalcError;
		/**
		 * Versucht den Ausdruck zu vereinfachen.
		 * @return	Vereinfachter Ausdruck oder auch das Objekt selbst, wenn keine Vereinfachung möglich war.
		 */
		public Symbol simplify() {return this;}

		/**
		 * Wird von {@link #calc(double[])} aufgerufen, um ein Fehler-Objekt zu erzeugen.
		 * @return	Fehler-Objekt zum aktuellen Symbol
		 */
		protected final MathCalcError error() {return new MathCalcError(this);}
	}

	/**
	 * Rechensymbol, das eine Zahl repräsentiert
	 */
	private class NumberSymbol extends Symbol {
		/** Zahl */
		public final double value;

		/**
		 * Konstruktor der Klasse
		 * @param value	Zahl
		 */
		public NumberSymbol(final double value) {
			this.value=value;
		}

		@Override
		public double calc(final double[] variableValues) {
			return value;
		}
	}

	/**
	 * Rechensymbol, das eine Variable repräsentiert
	 */
	private class VariableSymbol extends Symbol {
		/**
		 * Index der Variable in der Liste aller Variablen,
		 * dabei steht -2 für pi und -3 für e.
		 * @see #calc(double[])
		 */
		public final int variableIndex;

		/**
		 * Konstruktor der Klasse
		 * @param variableIndex	Index der Variable in der Liste aller Variablen
		 */
		public VariableSymbol(final int variableIndex) {
			this.variableIndex=variableIndex;
		}

		@Override
		public double calc(final double[] variableValues) throws MathCalcError {
			if (variableIndex==-2) return Math.PI;
			if (variableIndex==-3) return Math.E;
			if (variableIndex<0 || variableIndex>=variableValues.length) throw error();
			return variableValues[variableIndex];
		}

		@Override
		public Symbol simplify() {
			if (variableIndex==-2) return new NumberSymbol(Math.PI);
			if (variableIndex==-3) return new NumberSymbol(Math.E);
			return this;
		}
	}

	/**
	 * Liste der unterstützten zweistelligen Operatoren
	 * @see OperatorSymbol
	 */
	private static final String[] OPERATOR_NAMES=new String[]{"^",":","/","*","-","+"};

	/**
	 * Liste der unterstützten nachgestellten Operatoren
	 * @see OperatorSymbol
	 */
	private static final char[] POST_OPERATOR_NAMES=new char[]{'%','²','³','!'};

	/** Faktor zur Interpretation einer Zahl als Prozentwert */
	private static final double toPercent=1.0/100.0;

	/** Enthält den 1/log(2); wird bei der Berechnung von Logarithmen zur Basis 2 verwendet */
	private static final double inverseLog2=1.0/Math.log(2.0);

	/**
	 * Rechensymbol, das einen ein- oder zweistelligen Operator repräsentiert
	 */
	private class OperatorSymbol extends Symbol {
		/**
		 * Bezeichner des Operators
		 */
		public final char operator;

		/**
		 * Linkes Unter-Symbol
		 */
		public final Symbol left;

		/** Rechtes Unter-Symbol (oder <code>null</code> bei einem einstelligen Operator) */
		public final Symbol  right;

		/**
		 * Konstruktor der Klasse
		 * @param operator	Bezeichner des Operators
		 * @param left	Linkes Unter-Symbol
		 * @param right	Rechtes Unter-Symbol (oder <code>null</code> bei einem einstelligen Operator)
		 */
		public OperatorSymbol(final char operator, final Symbol left, final Symbol right) {this.operator=operator; this.left=left; this.right=right;}

		@Override
		public double calc(final double[] variableValues) throws MathCalcError {
			double l2=0;
			if (left!=null) l2=left.calc(variableValues);
			double r2=0;
			if (right!=null) r2=right.calc(variableValues);

			switch (operator) {
			case '+': return l2+r2;
			case '-': return l2-r2;
			case '*': return l2*r2;
			case '/':
			case ':': if (r2==0) throw error(); else return (l2/r2);
			case '^': if (r2<0) throw error(); else return FastMath.pow(l2,r2);
			case '%': return l2*toPercent;
			case '²': return l2*l2;
			case '³': return l2*l2*l2;
			case '!': if (l2==0.0) return 1.0; else return Math.signum(l2)*Functions.getFactorial((int)Math.round(Math.abs(l2)));
			default: throw error();
			}
		}

		@Override
		public Symbol simplify() {
			final Symbol l=(left==null)?null:left.simplify();
			final Symbol r=(right==null)?null:right.simplify();
			if ((l==null || l instanceof NumberSymbol) && (r==null || r instanceof NumberSymbol)) {
				final OperatorSymbol o=new OperatorSymbol(operator,l,r);
				try {
					return new NumberSymbol(o.calc(new double[0]));
				} catch (MathCalcError e) {
					return this;
				}
			}
			return this;
		}
	}

	/**
	 * Liste der unterstützten Funktionen
	 * @see FunctionSymbol
	 */
	private static final String[] FUNCTION_NAMES=new String[]{"sqr","wurzel","sqrt","quadratwurzel","\\","sin","cos","tan","cot","exp","log","ln","lg","ld","abs","absolutbetrag","betrag","frac","int","round","rnd","runden","abrunden","floor","aufrunden","ceil","factorial","fakultät","sign","signum","sgn"};

	/**
	 * Rechensymbol, das eine Funktion repräsentiert
	 */
	private class FunctionSymbol extends Symbol {
		/**
		 * Name der Funktion
		 */
		public final String name;

		/**
		 * Parameter der Funktion
		 */
		public final Symbol sub;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der Funktion
		 * @param sub	Parameter der Funktion
		 */
		public FunctionSymbol(final String name, final Symbol sub) {
			this.name=name.toLowerCase();
			this.sub=sub;
		}

		@Override
		public double calc(final double[] variableValues) throws MathCalcError {
			if (sub==null) throw error();
			final double subValue=sub.calc(variableValues);
			double d;
			switch (name) {
			case "sqr" : return subValue*subValue;
			case "wurzel":
			case "quadratwurzel":
			case "\\":
			case "sqrt" : if (subValue<0) throw error(); else return Math.sqrt(subValue);
			case "sin" : return Math.sin(subValue);
			case "cos" : return Math.cos(subValue);
			case "tan" : d=Math.tan(subValue); if (Double.isNaN(d)) throw error(); else return d;
			case "cot" : d=Math.tan(subValue); if (Double.isNaN(d) || Math.abs(d)<0.000001) throw error(); else return 1/d;
			case "exp" : return FastMath.exp(subValue);
			case "log" :
			case "ln" : if (subValue<=0) throw error(); else return Math.log(subValue);
			case "lg" : if (subValue<=0) throw error(); else return Math.log10(subValue);
			case "ld" : if (subValue<=0) throw error(); else return Math.log(subValue)*inverseLog2;
			case "absolutbetrag":
			case "betrag":
			case "abs" : return Math.abs(subValue);
			case "frac": return subValue%1;
			case "int": return subValue-subValue%1;
			case "round" :
			case "rnd":
			case "runden" : return Math.round(subValue);
			case "abrunden":
			case "floor" : return Math.floor(subValue);
			case "aufrunden":
			case "ceil" : return Math.ceil(subValue);
			case "factorial":
			case "fakultät": if (subValue==0.0) return 1.0; else return Math.signum(subValue)*Functions.getFactorial((int)Math.round(Math.abs(subValue)));
			case "sign":
			case "signum":
			case "sgn": return Math.signum(subValue);
			default: throw error();
			}
		}

		@Override
		public Symbol simplify() {
			final Symbol s=sub.simplify();
			if (s instanceof NumberSymbol) {
				final FunctionSymbol f=new FunctionSymbol(name,s);
				try {
					return new NumberSymbol(f.calc(new double[0]));
				} catch (MathCalcError e) {
					return this;
				}

			}
			return this;
		}
	}

	/**
	 * Berechnet den Wert eines Ausdrucks ohne weitere Variablen
	 * @param text	Zu berechnender Ausdruck
	 * @return	Gibt im Fehlerfall <code>null</code> zurück, sonst den Zahlenwert des Ergebnisses.
	 */
	public static Double calcSimple(final String text) {
		final SimpleParser parser=new SimpleParser(text);
		if (parser.parse()!=-1) return null;
		try {
			return parser.calc();
		} catch (MathCalcError e) {
			return null;
		}
	}
}