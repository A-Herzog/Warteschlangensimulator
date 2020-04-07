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

/**
 * Einfacher Formel-Praser
 * @author Alexander Herzog
 * @version 2.3
 */
public class SimpleParser extends CalcSystemBase {
	private Symbol root;

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 */
	public SimpleParser() {
		super();
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
	 * @param values	Liste der Werte, die zu den Variablennamen geh�ren sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
	 */
	public SimpleParser(final List<String> variables, final List<Double> values) {
		super(variables,values);
	}

	/**
	 * Konstruktor der Klasse <code>SimpleParser</code>
	 * @param text	Zu berechnender Ausdruck
	 * @param variables	Liste der Variablennamen, die erkannt werden sollen
	 * @param values	Liste der Werte, die zu den Variablennamen geh�ren sollen (kann allerdings bei der Berechnung durch andere Werte ersetzt werden)
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

			/* N�chstes Zeichen holen und vorsortieren */
			char c=(i>=text.length())?0:text.charAt(i);
			lastMode=mode;
			mode=mode_text;
			if (i>=text.length()) mode=mode_done; else {
				if ((c>='0' && c<='9') || c==',' || c=='.') mode=mode_number;
				if (c=='+' || c=='-' || c=='*' || c=='/' || c==':' || c=='^' || c=='%' || c=='�' || c=='�') mode=mode_symbol;
				if (c=='(' || c=='[' || c=='{' || c==')' || c==']' || c=='}') mode=mode_sub;
				if (c==' ') mode=mode_empty;
			}

			/* Ggf. Verarbeitung des letzten Token abschlie�en */
			if (lastMode==mode_number && mode!=mode_number) {
				final Double D=NumberTools.getPlainDouble(token);
				if (D==null) return null;
				tokens.add(D); inNumberFracPart=false; token="";
			}
			if (lastMode==mode_symbol) {tokens.add(token); token="";}
			if (lastMode==mode_text && mode!=mode_text) {tokens.add(token); token="";}

			/* Wenn alle Zeichen verarbeitet sind, Ende */
			if (mode==mode_done) break;

			/* N�chstes Zeichen verarbeiten */

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

	private int isVariable(final String symbol) {
		if (symbol.equalsIgnoreCase("pi")) return -2;
		if (symbol.equalsIgnoreCase("e")) return -3;
		for (int i=0;i<variables.length;i++) if (variables[i].equalsIgnoreCase(symbol)) return i;
		return -1;
	}

	private boolean isFunction(final String symbol) {
		if (symbol==null || symbol.isEmpty()) return false;
		for (String sym: FUNCTION_NAMES) if (symbol.equalsIgnoreCase(sym)) return true;
		return false;
	}

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

	@SuppressWarnings("unchecked")
	private Symbol tokenToSymbol(final Object o) {
		if (o instanceof Double) return new NumberSymbol((Double)o);
		if (o instanceof String) {int i=isVariable((String)o); return (i!=-1)?new VariableSymbol(i):null;}
		if (o instanceof Symbol) return (Symbol)o;
		if (o instanceof ArrayList) return buildTree((ArrayList<Object>)o);
		return null;
	}

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
	 * Versucht den �bergebenen �bergebenen Ausdruck zu interpretieren, dabei werden
	 * Variablennamen erkannt usw., es wird aber noch kein konkreter Wert berechnet.
	 * @param text	Ausdruck, der verarbeitet werden soll
	 * @return	Gibt <code>-1</code> zur�ck, wenn der Ausdruck verarbeitet werden konnte.
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

	private Double plainNumberCache=null;

	/**
	 * Berechnet den bereits geparsten Ausdruck auf Basis der bekannten Variablennamen und der hier angegebenen Werte.
	 * @param variableValues	Liste mit den Werten der Variablen
	 * @return	Gibt im Fehlerfall <code>null</code> zur�ck, sonst den Zahlenwert des Ergebnisses.
	 */
	@Override
	public Double calc(double[] variableValues) {
		if (isConstValue()) {
			final double constValue=getConstValue();
			if (plainNumberCache==null || plainNumberCache.doubleValue()!=constValue) plainNumberCache=constValue;
			return plainNumberCache;
		}
		if (root==null) return null;
		return root.calc(variableValues);
	}

	@Override
	public double calcOrDefault(double[] variableValues, double fallbackValue) {
		if (isConstValue()) return getConstValue();
		if (root==null) return fallbackValue;
		final Double D=root.calc(variableValues);
		return (D==null)?fallbackValue:D.doubleValue();
	}

	private abstract class Symbol {
		public abstract Double calc(final double[] variableValues);
		public Symbol simplify() {return this;}
	}

	private class NumberSymbol extends Symbol {
		public final double value;
		public NumberSymbol(final double value) {this.value=value;}
		@Override
		public Double calc(final double[] variableValues) {return value;}
	}

	private class VariableSymbol extends Symbol {
		public final int variableIndex;
		public VariableSymbol(final int variableIndex) {this.variableIndex=variableIndex;}
		@Override
		public Double calc(final double[] variableValues) {
			if (variableIndex==-2) return Math.PI;
			if (variableIndex==-3) return Math.E;
			return (variableIndex<0 || variableIndex>=variableValues.length)?null:variableValues[variableIndex];
		}
		@Override
		public Symbol simplify() {
			if (variableIndex==-2) return new NumberSymbol(Math.PI);
			if (variableIndex==-3) return new NumberSymbol(Math.E);
			return this;
		}
	}

	private static final String[] OPERATOR_NAMES=new String[]{"^",":","/","*","-","+"};
	private static final char[] POST_OPERATOR_NAMES=new char[]{'%','�','�','!'};

	private class OperatorSymbol extends Symbol {
		public final char operator;
		public final Symbol left, right;
		public OperatorSymbol(final char operator, final Symbol left, final Symbol right) {this.operator=operator; this.left=left; this.right=right;}
		@Override
		public Double calc(final double[] variableValues) {
			Double l=null, r=null;
			if (left!=null) {l=left.calc(variableValues); if (l==null) return null;}
			if (right!=null) {r=right.calc(variableValues); if (r==null) return null;}
			final double l2=(l!=null)?l.doubleValue():0.0;
			final double r2=(r!=null)?r.doubleValue():0.0;
			switch (operator) {
			case '+': return l2+r2;
			case '-': return l2-r2;
			case '*': return l2*r2;
			case '/':
			case ':': return (r2==0)?null:(l2/r2);
			case '^': return (r2<0)?null:Math.pow(l2,r2);
			case '%': return l2/100;
			case '�': return l2*l2;
			case '�': return l2*l2*l2;
			case '!': if (l2==0.0) return 1.0; else return Math.signum(l2)*Functions.getFactorial((int)Math.round(Math.abs(l2)));
			default: return null;
			}
		}
		@Override
		public Symbol simplify() {
			final Symbol l=(left==null)?null:left.simplify();
			final Symbol r=(right==null)?null:right.simplify();
			if ((l==null || l instanceof NumberSymbol) && (r==null || r instanceof NumberSymbol)) {
				final OperatorSymbol o=new OperatorSymbol(operator,l,r);
				final Double d=o.calc(new double[0]);
				if (d!=null) return new NumberSymbol(d);
			}
			return this;
		}
	}

	private static final String[] FUNCTION_NAMES=new String[]{"sqr","wurzel","sqrt","quadratwurzel","\\","sin","cos","tan","cot","exp","log","ln","lg","ld","abs","absolutbetrag","betrag","frac","int","round","rnd","runden","abrunden","floor","aufrunden","ceil","factorial","fakult�t","sign","signum","sgn"};

	private class FunctionSymbol extends Symbol {
		public final String name;
		public final Symbol sub;
		public FunctionSymbol(final String name, final Symbol sub) {this.name=name.toLowerCase(); this.sub=sub;}
		@Override
		public Double calc(final double[] variableValues) {
			if (sub==null) return null;
			final Double subValue=sub.calc(variableValues); if (subValue==null) return null;
			double d;
			switch (name) {
			case "sqr" : return subValue*subValue;
			case "wurzel":
			case "quadratwurzel":
			case "\\":
			case "sqrt" : return (subValue>=0)?Math.sqrt(subValue):null;
			case "sin" : return Math.sin(subValue);
			case "cos" : return Math.cos(subValue);
			case "tan" : d=Math.tan(subValue); return (Double.isNaN(d))?null:d;
			case "cot" : d=Math.tan(subValue); return (Double.isNaN(d) || Math.abs(d)<0.000001)?null:(1/d);
			case "exp" : return FastMath.exp(subValue);
			case "log" :
			case "ln" : return (subValue>0)?Math.log(subValue):null;
			case "lg" : return (subValue>0)?Math.log10(subValue):null;
			case "ld" : return (subValue>0)?(Math.log(subValue)/Math.log(2)):null;
			case "absolutbetrag":
			case "betrag":
			case "abs" : return StrictMath.abs(subValue);
			case "frac": return subValue%1;
			case "int": return subValue-subValue%1;
			case "round" :
			case "rnd":
			case "runden" : return (double)Math.round(subValue);
			case "abrunden":
			case "floor" : return Math.floor(subValue);
			case "aufrunden":
			case "ceil" : return Math.ceil(subValue);
			case "factorial":
			case "fakult�t": if (subValue==0.0) return 1.0; else return Math.signum(subValue)*Functions.getFactorial((int)Math.round(Math.abs(subValue)));
			case "sign":
			case "signum":
			case "sgn": return Math.signum(subValue);
			default: return null;
			}
		}
		@Override
		public Symbol simplify() {
			final Symbol s=sub.simplify();
			if (s instanceof NumberSymbol) {
				final FunctionSymbol f=new FunctionSymbol(name,s);
				final Double d=f.calc(new double[0]);
				if (d!=null) return new NumberSymbol(d);
			}
			return this;
		}
	}

	/**
	 * Berechnet den Wert eines Ausdrucks ohne weitere Variablen
	 * @param text	Zu berechnender Ausdruck
	 * @return	Gibt im Fehlerfall <code>null</code> zur�ck, sonst den Zahlenwert des Ergebnisses.
	 */
	public static Double calcSimple(final String text) {
		final SimpleParser parser=new SimpleParser(text);
		if (parser.parse()!=-1) return null;
		return parser.calc();
	}
}