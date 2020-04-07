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

import java.util.ArrayList;
import java.util.List;

import mathtools.NumberTools;
import parser.coresymbols.CalcSymbol;
import parser.coresymbols.CalcSymbolConst;
import parser.coresymbols.CalcSymbolFunction;
import parser.coresymbols.CalcSymbolPostOperator;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.coresymbols.CalcSymbolSub;
import parser.coresymbols.CalcSymbolVariable;
import parser.symbols.CalcSymbolMiddleOperatorMultiply;
import parser.symbols.CalcSymbolPreOperatorAbs;

/**
 * Diese Klasse wandelt Zeichenketten, die Formelausdrücke beinhalten,
 * in Objektstrukturen um. Diese Klasse wird in {@link CalcSystem} verwendet.
 * @author Alexander Herzog
 * @see CalcSystem
 */
public final class CalcParser {
	private static final char[] simpleStrings=new char[]{'(','{','[',')',']','}','|',';'};
	private static final char[] simpleSymbols=new char[]{'(','(','(',')',')',')','|',';'};

	private final CalcSymbolList symbolList;
	private final String[] allSymbols;

	/**
	 * Konstruktor der Klasse <code>CalcParser</code>
	 * @param symbolList	Liste der bekannten Symbole
	 * @see CalcSymbolList
	 */
	public CalcParser(final CalcSymbolList symbolList) {
		this.symbolList=symbolList;
		allSymbols=symbolList.getAllSymbolNamesLower();
	}

	private StringBuilder numberParser;

	private Object[] getNumber(String text) {
		if (numberParser==null) numberParser=new StringBuilder(); else numberParser.setLength(0);

		int i=0;
		boolean separator=false;

		while (i<text.length()) {
			char c=text.charAt(i);
			if (c>='0' && c<='9') {
				numberParser.append(c);
				i++;
				continue;
			}
			if (c==',' || c=='.') {
				if (separator) return null;
				separator=true;
				if (numberParser.length()==0) numberParser.append("0");
				numberParser.append(c);
				i++;
				continue;
			}

			break;
		}

		if (numberParser.length()==0) return null;

		Double D=NumberTools.getPlainDouble(numberParser.toString());
		if (D==null) return null;

		if (i==text.length()) text=""; else text=text.substring(i);

		return new Object[]{symbolList.getNumber(D),text,i,D};
	}

	/**
	 * Überprüft, ob es sich bei der Zeichenkette einfach nur um eine Zahl handelt.
	 * @param text	Zu prüfende Zeichenkette
	 * @return	Gibt im Erfolgsfall direkt die Zahl zurück.
	 */
	public Double isNumber(final String text) {
		/*
		Belegt unnötig Speicher:
		Object[] obj=getNumber(text.trim());
		if (obj==null || obj.length<4) return null;
		if (((String)obj[1]).length()>0) return null;
		return (Double)obj[3];
		 */

		int i=0;
		boolean separator=false;
		boolean startsWithSeparator=false;
		final int len=text.length();

		while (i<len) {
			char c=text.charAt(i);
			if (c>='0' && c<='9') {i++; continue;}
			if (c==',' || c=='.') {
				if (separator) return null;
				separator=true;
				if (i==0) startsWithSeparator=true;
				i++;
				continue;
			}
			return null;
		}

		if (startsWithSeparator) return NumberTools.getPlainDouble("0"+text);
		return NumberTools.getPlainDouble(text);

		/*
		if (numberParser==null) numberParser=new StringBuilder(); else numberParser.setLength(0);

		int i=0;
		boolean separator=false;
		final int len=text.length();

		while (i<len) {
			char c=text.charAt(i);
			if (c>='0' && c<='9') {
				numberParser.append(c);
				i++;
				continue;
			}
			if (c==',' || c=='.') {
				if (separator) return null;
				separator=true;
				if (numberParser.length()==0) numberParser.append("0");
				numberParser.append(c);
				i++;
				continue;
			}

			break;
		}

		if (numberParser.length()==0) return null;

		Double D=NumberTools.getPlainDouble(numberParser.toString());
		if (D==null) return null;

		if (i!=len) return null;
		return D;
		 */
	}

	private Object splitString(String text) {
		int position=0;
		List<Object> tokens=new ArrayList<>();
		text=text.toLowerCase();
		boolean ok;

		while (!text.isEmpty()) {
			char c=text.charAt(0);

			/* Zahlen */
			if ((c>='0' && c<='9') || c==',' || c=='.') {
				Object[] obj=getNumber(text);
				if (obj==null) return position;
				if (!(obj[0] instanceof CalcSymbol)) return position;
				CalcSymbol sym=(CalcSymbol)obj[0];
				sym.position=position;
				tokens.add(sym);
				text=(String)obj[1];
				position+=(Integer)obj[2];
				continue;
			}

			/* Einfache Symbole */
			ok=false;
			for (int i=0;i<simpleStrings.length;i++) if (simpleStrings[i]==c) {
				tokens.add(""+simpleSymbols[i]);
				if (text.length()==1) text=""; else text=text.substring(1);
				position++;
				ok=true;
				break;
			}
			if (ok) continue;

			/* Leerzeichen */
			if (c==' ') {
				if (text.length()==1) text=""; else text=text.substring(1);
				position++;
				continue;
			}

			/* Symbole */
			String bestMatch=null;
			for (String s: allSymbols) if (text.startsWith(s)) {
				if (bestMatch==null || s.length()>bestMatch.length()) bestMatch=s;
			}
			if (bestMatch!=null) {
				CalcSymbol sym=symbolList.findSymbol(bestMatch);
				if (sym!=null) {
					sym.position=position;
					tokens.add(sym);
					if (bestMatch.length()==text.length()) text=""; else text=text.substring(bestMatch.length());
					position+=bestMatch.length();
					continue;
				}
			}

			/* Unbekannte Zeichen */
			return position;
		}

		return tokens;
	}

	/**
	 * Versucht die übergebene Zeichenkette als Formel bestehend aus den im Konstruktor
	 * übergebenen Symbolen zu interpretieren.
	 * @param text	Zu interpretierende Zeichenkette
	 * @return	Liefert im Erfolgsfall das Wurzelelement (vom Typ {@link CalcSymbol}) des Formelbaums zurück. Ansonsten ein Objekt vom Typ {@link Integer}, welche die 0-basierende Position des Fehlers in der Zeichenkette angibt.
	 */
	public Object parse(final String text) {
		/* In Tokens zerlegen */
		final Object obj=splitString(text);
		if (obj instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<Object> list=(List<Object>)obj;
			return parse(list);
		} else {
			return obj;
		}
	}

	private Object buildSub(final List<Object> list) {
		final List<CalcSymbol> parts=new ArrayList<>();
		final List<Object> buffer=new ArrayList<>();
		int subcount=0;
		for (Object o: list) {
			if (o instanceof String) {
				boolean ok=false;
				if (((String)o).equals("(")) {ok=true; subcount++;}
				if (((String)o).equals(")")) {ok=true; subcount=Math.max(0,subcount-1);}
				if (subcount==0 && ((String)o).equals(";")) {
					Object obj=parse(buffer);
					if (!(obj instanceof CalcSymbol)) return obj;
					parts.add((CalcSymbol)obj);
					buffer.clear();
					continue;
				}
				if (subcount==0 && !ok) return 0;
			}
			buffer.add(o);
		}
		if (!buffer.isEmpty()) {
			Object obj=parse(buffer);
			if (!(obj instanceof CalcSymbol)) return obj;
			parts.add((CalcSymbol)obj);
		}

		final CalcSymbolSub sub=new CalcSymbolSub();
		sub.setData(parts.toArray(new CalcSymbol[0]));
		return sub;
	}

	private Object parse(List<Object> list) {
		List<Object> temp, buffer;

		/* Klammern verarbeiten */
		temp=new ArrayList<>();
		buffer=null;
		int count=0;
		for (Object o: list) {
			if (o instanceof String && ((String)o).equals("(")) {
				count++;
				if (count==1) {buffer=new ArrayList<>(); continue;}
			}
			if (o instanceof String && ((String)o).equals(")")) {
				count--;
				if (count<0) {
					/* Eine Klammer zu viel geschlossen */
					count=0;
					continue;
				}
				if (count==0) {
					Object obj=buildSub(buffer);
					if (!(obj instanceof CalcSymbolSub)) return obj;
					temp.add(obj);
					buffer=null;
					continue;
				}
			}
			if (buffer!=null) buffer.add(o); else temp.add(o);
		}
		if (buffer!=null) {
			/* Klammer nicht geschlossen */
			Object obj=buildSub(buffer);
			if (!(obj instanceof CalcSymbolSub)) return obj;
			temp.add(obj);
		}
		list=temp;

		/* Betragsstriche verarbeiten */
		temp=new ArrayList<>();
		buffer=null;
		boolean inBlock=false;
		for (Object o: list) {
			if (o instanceof String && ((String)o).equals("|")) {
				if (inBlock) {
					Object obj=buildSub(buffer);
					if (!(obj instanceof CalcSymbolSub)) return obj;
					CalcSymbolPreOperatorAbs abs=new CalcSymbolPreOperatorAbs();
					abs.setParameter(new CalcSymbol[]{(CalcSymbolSub)obj});
					temp.add(abs);
					buffer=null;
				} else {
					buffer=new ArrayList<>();
				}
				inBlock=!inBlock;
				continue;
			}
			if (buffer!=null) buffer.add(o); else temp.add(o);
		}
		if (buffer!=null) {
			/* Betragsblock nicht geschlossen */
			Object obj=buildSub(buffer);
			if (!(obj instanceof CalcSymbolSub)) return obj;
			CalcSymbolPreOperatorAbs abs=new CalcSymbolPreOperatorAbs();
			abs.setParameter(new CalcSymbol[]{(CalcSymbolSub)obj});
			temp.add(abs);
		}
		list=temp;

		/* Malpunkte einfügen wenn nötig */
		temp=new ArrayList<>();
		Object last=null;
		if (!list.isEmpty()) {last=list.get(0); temp.add(last);}
		for (int i=1;i<list.size();i++) {
			Object sym=list.get(i);
			boolean needMul=false;

			if (last instanceof CalcSymbolConst && sym instanceof CalcSymbolConst) needMul=true;
			if (last instanceof CalcSymbolVariable && sym instanceof CalcSymbolConst) needMul=true;
			if (last instanceof CalcSymbolConst && sym instanceof CalcSymbolVariable) needMul=true;

			if (last instanceof CalcSymbolConst && sym instanceof CalcSymbolPreOperator) needMul=true;
			if (last instanceof CalcSymbolPostOperator && sym instanceof CalcSymbolConst) needMul=true;
			if (last instanceof CalcSymbolConst && sym instanceof CalcSymbolSub) needMul=true;
			if (last instanceof CalcSymbolSub && sym instanceof CalcSymbolConst) needMul=true;

			if (last instanceof CalcSymbolVariable && sym instanceof CalcSymbolPreOperator) needMul=true;
			if (last instanceof CalcSymbolPostOperator && sym instanceof CalcSymbolVariable) needMul=true;
			if (last instanceof CalcSymbolVariable && sym instanceof CalcSymbolSub) needMul=true;
			if (last instanceof CalcSymbolSub && sym instanceof CalcSymbolVariable) needMul=true;

			if (needMul) temp.add(new CalcSymbolMiddleOperatorMultiply());
			temp.add(sym);
			last=sym;
		}
		list=temp;

		/* Baum aufbauen */
		return buildTree(list);
	}

	private Object buildTree(final List<Object> list) {
		CalcSymbolFunction prioSym;
		int prio, prioIndex;
		while (list.size()>1) {
			prioSym=null;
			prio=-1;
			prioIndex=-1;
			for (int i=0;i<list.size();i++) {
				if (!(list.get(i) instanceof CalcSymbolFunction)) continue;
				CalcSymbolFunction sym=(CalcSymbolFunction)list.get(i);
				if (sym.getPriority()>prio) {prioSym=sym; prioIndex=i; prio=sym.getPriority();}
			}
			if (prioSym==null) return 0;

			if (prioSym.getType()==CalcSymbol.SymbolType.TYPE_FUNCTION) {
				if (prioIndex==list.size()-1) return prioSym.position;
				if (list.get(prioIndex+1) instanceof String) return prioSym.position;
				CalcSymbol right=(CalcSymbol)(list.get(prioIndex+1));
				if (right instanceof CalcSymbolFunction && ((CalcSymbolFunction)right).getPriority()>0) return right.position;
				prioSym.setParameter(new CalcSymbol[]{right});
				list.remove(prioIndex+1);
				continue;
			}

			if (prioSym.getType()==CalcSymbol.SymbolType.TYPE_MIDDLE_OPERATOR) {
				if (prioIndex==list.size()-1) return prioSym.position;
				if (list.get(prioIndex+1) instanceof String) return prioSym.position;
				CalcSymbol right=(CalcSymbol)(list.get(prioIndex+1));
				if (right instanceof CalcSymbolFunction && ((CalcSymbolFunction)right).getPriority()>0) return right.position;
				if (prioIndex==0) {
					prioSym.setParameter(new CalcSymbol[]{symbolList.getNumber(0),right});
					list.remove(prioIndex+1);
				} else {
					if (list.get(prioIndex-1) instanceof String) return prioSym.position;
					CalcSymbol left=(CalcSymbol)(list.get(prioIndex-1));
					if (left instanceof CalcSymbolFunction && ((CalcSymbolFunction)left).getPriority()>0) return left.position;
					prioSym.setParameter(new CalcSymbol[]{left,right});
					list.remove(prioIndex+1);
					list.remove(prioIndex-1);
				}
				continue;
			}

			if (prioSym.getType()==CalcSymbol.SymbolType.TYPE_POST_OPERATOR) {
				if (prioIndex==0) return prioSym.position;
				if (list.get(prioIndex-1) instanceof String) return prioSym.position;
				CalcSymbol left=(CalcSymbol)(list.get(prioIndex-1));
				if (left instanceof CalcSymbolFunction && ((CalcSymbolFunction)left).getPriority()>0) return left.position;
				prioSym.setParameter(new CalcSymbol[]{left});
				list.remove(prioIndex-1);
				continue;
			}
		}

		if (list.isEmpty()) return 0;
		Object o=list.get(0);
		if (!(o instanceof CalcSymbol)) return 0;
		return o;
	}
}
