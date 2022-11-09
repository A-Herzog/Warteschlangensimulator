/**
 * Copyright 2021 Alexander Herzog
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
package ui.modeleditor.elements;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Diese Klasse ermöglicht es, HTML-Entities und LaTeX-Symbole
 * in Texten zu interpretieren.
 * @author Alexander Herzog
 */
public class TextTransformer {
	/**
	 * Konstruktor der Klasse
	 */
	public TextTransformer() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Zuletzt verarbeitete Eingabezeile
	 * @see #lastSingleOutput
	 * @see #process(String)
	 */
	private String lastSingleInput;

	/**
	 * Verarbeitungsergebnis von {@link #lastSingleInput}
	 * @see #lastSingleInput
	 * @see #process(String)
	 */
	private String lastSingleOutput;

	/**
	 * Zuletzt verarbeitete mehrzeilige Eingabe
	 * @see #lastMultiOutput
	 * @see #process(String[])
	 */
	private String[] lastMultiInput;

	/**
	 * Verarbeitungsergebnis von {@link #lastMultiOutput}
	 * @see #lastMultiInput
	 * @see #process(String[])
	 */
	private String[] lastMultiOutput;

	/**
	 * Liste der HTML-Entities
	 * @see #processIntern(String)
	 */
	private static Map<Character,Map<String,String>> entitiesHTML;

	/**
	 * Liste der LaTeX-Symbole
	 * @see #processIntern(String)
	 */
	private static Map<Character,Map<String,String>> entitiesLaTeX;

	/**
	 * Länge des längsten Schlüssels
	 */
	private static int mapKeysMaxLength=0;

	/**
	 * Fügt einen Eintrag zu einer Bezeichner-Symbol-Liste hinzu.
	 * @param map	Bezeichner-Symbol-Liste
	 * @param key	Bezeichner
	 * @param value	Symbol
	 */
	private static void addEntity(final Map<Character,Map<String,String>> map, final String key, final String value) {
		if (key.length()>mapKeysMaxLength) mapKeysMaxLength=key.length();
		final Character firstChar=key.charAt(0);
		Map<String,String> subMap=map.get(firstChar);
		if (subMap==null) map.put(firstChar,subMap=new HashMap<>());
		subMap.put(key,value);
	}

	/**
	 * Fügt einen Eintrag zu einer Bezeichner-Symbol-Liste hinzu.
	 * @param map	Bezeichner-Symbol-Liste
	 * @param key	Bezeichner
	 * @param unicode	Unicode des Symbols
	 */
	private static void addEntity(final Map<Character,Map<String,String>> map, final String key, final int unicode) {
		addEntity(map,key,Character.toString((char)unicode));
	}

	static {
		entitiesHTML=new HashMap<>();
		entitiesLaTeX=new HashMap<>();

		addEntity(entitiesHTML,"amp;","&");
		addEntity(entitiesHTML,"lt;","<");
		addEntity(entitiesHTML,"gt;",">");
		addEntity(entitiesHTML,"lt;","<");
		addEntity(entitiesHTML,"Agrave;",192);
		addEntity(entitiesHTML,"Aacute;",193);
		addEntity(entitiesHTML,"Acirc;",194);
		addEntity(entitiesHTML,"Atilde;",195);
		addEntity(entitiesHTML,"Auml;",196);
		addEntity(entitiesHTML,"Aring;",197);
		addEntity(entitiesHTML,"AElig;",198);
		addEntity(entitiesHTML,"Ccedil;",199);
		addEntity(entitiesHTML,"Egrave;",200);
		addEntity(entitiesHTML,"Eacute;",201);
		addEntity(entitiesHTML,"Ecirc;",202);
		addEntity(entitiesHTML,"Euml;",203);
		addEntity(entitiesHTML,"Igrave;",204);
		addEntity(entitiesHTML,"Iacute;",205);
		addEntity(entitiesHTML,"Icirc;",206);
		addEntity(entitiesHTML,"Iuml;",207);
		addEntity(entitiesHTML,"ETH;",208);
		addEntity(entitiesHTML,"Ntilde;",209);
		addEntity(entitiesHTML,"Ograve;",210);
		addEntity(entitiesHTML,"Oacute;",211);
		addEntity(entitiesHTML,"Ocirc;",212);
		addEntity(entitiesHTML,"Otilde;",213);
		addEntity(entitiesHTML,"Ouml;",214);
		addEntity(entitiesHTML,"Oslash;",216);
		addEntity(entitiesHTML,"Ugrave;",217);
		addEntity(entitiesHTML,"Uacute;",218);
		addEntity(entitiesHTML,"Ucirc;",219);
		addEntity(entitiesHTML,"Uuml;",220);
		addEntity(entitiesHTML,"Yacute;",221);
		addEntity(entitiesHTML,"THORN;",222);
		addEntity(entitiesHTML,"szlig;",223);
		addEntity(entitiesHTML,"agrave;",224);
		addEntity(entitiesHTML,"aacute;",225);
		addEntity(entitiesHTML,"acirc;",226);
		addEntity(entitiesHTML,"atilde;",227);
		addEntity(entitiesHTML,"auml;",228);
		addEntity(entitiesHTML,"aring;",229);
		addEntity(entitiesHTML,"aelig;",230);
		addEntity(entitiesHTML,"ccedil;",231);
		addEntity(entitiesHTML,"egrave;",232);
		addEntity(entitiesHTML,"eacute;",233);
		addEntity(entitiesHTML,"ecirc;",234);
		addEntity(entitiesHTML,"euml;",235);
		addEntity(entitiesHTML,"igrave;",236);
		addEntity(entitiesHTML,"iacute;",237);
		addEntity(entitiesHTML,"icirc;",238);
		addEntity(entitiesHTML,"iuml;",239);
		addEntity(entitiesHTML,"eth;",240);
		addEntity(entitiesHTML,"ntilde;",241);
		addEntity(entitiesHTML,"ograve;",242);
		addEntity(entitiesHTML,"oacute;",243);
		addEntity(entitiesHTML,"ocirc;",244);
		addEntity(entitiesHTML,"otilde;",245);
		addEntity(entitiesHTML,"ouml;",246);
		addEntity(entitiesHTML,"oslash;",248);
		addEntity(entitiesHTML,"ugrave;",249);
		addEntity(entitiesHTML,"uacute;",250);
		addEntity(entitiesHTML,"ucirc;",251);
		addEntity(entitiesHTML,"uuml;",252);
		addEntity(entitiesHTML,"yacute;",253);
		addEntity(entitiesHTML,"thorn;",254);
		addEntity(entitiesHTML,"yuml;",255);
		addEntity(entitiesHTML,"iexcl;",161);
		addEntity(entitiesHTML,"cent;",162);
		addEntity(entitiesHTML,"pound;",163);
		addEntity(entitiesHTML,"curren;",164);
		addEntity(entitiesHTML,"yen;",165);
		addEntity(entitiesHTML,"brvbar;",166);
		addEntity(entitiesHTML,"sect;",167);
		addEntity(entitiesHTML,"uml;",168);
		addEntity(entitiesHTML,"copy;",169);
		addEntity(entitiesHTML,"ordf;",170);
		addEntity(entitiesHTML,"laquo;",171);
		addEntity(entitiesHTML,"not;",172);
		addEntity(entitiesHTML,"reg;",174);
		addEntity(entitiesHTML,"macr;",175);
		addEntity(entitiesHTML,"deg;",176);
		addEntity(entitiesHTML,"plusmn;",177);
		addEntity(entitiesHTML,"sup2;",178);
		addEntity(entitiesHTML,"sup3;",179);
		addEntity(entitiesHTML,"acute;",180);
		addEntity(entitiesHTML,"micro;",181);
		addEntity(entitiesHTML,"para;",182);
		addEntity(entitiesHTML,"cedil;",184);
		addEntity(entitiesHTML,"sup1;",185);
		addEntity(entitiesHTML,"ordm;",186);
		addEntity(entitiesHTML,"raquo;",187);
		addEntity(entitiesHTML,"frac14;",188);
		addEntity(entitiesHTML,"frac12;",189);
		addEntity(entitiesHTML,"frac34;",190);
		addEntity(entitiesHTML,"iquest;",191);
		addEntity(entitiesHTML,"times;",215);
		addEntity(entitiesHTML,"divide;",247);
		addEntity(entitiesHTML,"forall;",8704);
		addEntity(entitiesHTML,"part;",8706);
		addEntity(entitiesHTML,"exist;",8707);
		addEntity(entitiesHTML,"empty;",8709);
		addEntity(entitiesHTML,"nabla;",8711);
		addEntity(entitiesHTML,"isin;",8712);
		addEntity(entitiesHTML,"notin;",8713);
		addEntity(entitiesHTML,"ni;",8715);
		addEntity(entitiesHTML,"prod;",8719);
		addEntity(entitiesHTML,"sum;",8721);
		addEntity(entitiesHTML,"minus;",8722);
		addEntity(entitiesHTML,"lowast;",8727);
		addEntity(entitiesHTML,"radic;",8730);
		addEntity(entitiesHTML,"prop;",8733);
		addEntity(entitiesHTML,"infin;",8734);
		addEntity(entitiesHTML,"infty;",8734);
		addEntity(entitiesHTML,"ang;",8736);
		addEntity(entitiesHTML,"and;",8743);
		addEntity(entitiesHTML,"or;",8744);
		addEntity(entitiesHTML,"cap;",8745);
		addEntity(entitiesHTML,"cup;",8746);
		addEntity(entitiesHTML,"int;",8747);
		addEntity(entitiesHTML,"there4;",8756);
		addEntity(entitiesHTML,"sim;",8764);
		addEntity(entitiesHTML,"cong;",8773);
		addEntity(entitiesHTML,"asymp;",8776);
		addEntity(entitiesHTML,"ne;",8800);
		addEntity(entitiesHTML,"equiv;",8801);
		addEntity(entitiesHTML,"le;",8804);
		addEntity(entitiesHTML,"ge;",8805);
		addEntity(entitiesHTML,"sub;",8834);
		addEntity(entitiesHTML,"sup;",8835);
		addEntity(entitiesHTML,"nsub;",8836);
		addEntity(entitiesHTML,"sube;",8838);
		addEntity(entitiesHTML,"supe;",8839);
		addEntity(entitiesHTML,"oplus;",8853);
		addEntity(entitiesHTML,"otimes;",8855);
		addEntity(entitiesHTML,"perp;",8869);
		addEntity(entitiesHTML,"sdot;",8901);
		addEntity(entitiesHTML,"Alpha;",913);
		addEntity(entitiesHTML,"Beta;",914);
		addEntity(entitiesHTML,"Gamma;",915);
		addEntity(entitiesHTML,"Delta;",916);
		addEntity(entitiesHTML,"Epsilon;",917);
		addEntity(entitiesHTML,"Zeta;",918);
		addEntity(entitiesHTML,"Eta;",919);
		addEntity(entitiesHTML,"Theta;",920);
		addEntity(entitiesHTML,"Iota;",921);
		addEntity(entitiesHTML,"Kappa;",922);
		addEntity(entitiesHTML,"Lambda;",923);
		addEntity(entitiesHTML,"Mu;",924);
		addEntity(entitiesHTML,"Nu;",925);
		addEntity(entitiesHTML,"Xi;",926);
		addEntity(entitiesHTML,"Omicron;",927);
		addEntity(entitiesHTML,"Pi;",928);
		addEntity(entitiesHTML,"Rho;",929);
		addEntity(entitiesHTML,"Sigma;",931);
		addEntity(entitiesHTML,"Tau;",932);
		addEntity(entitiesHTML,"Upsilon;",933);
		addEntity(entitiesHTML,"Phi;",934);
		addEntity(entitiesHTML,"Chi;",935);
		addEntity(entitiesHTML,"Psi;",936);
		addEntity(entitiesHTML,"Omega;",937);
		addEntity(entitiesHTML,"alpha;",945);
		addEntity(entitiesHTML,"beta;",946);
		addEntity(entitiesHTML,"gamma;",947);
		addEntity(entitiesHTML,"delta;",948);
		addEntity(entitiesHTML,"epsilon;",949);
		addEntity(entitiesHTML,"zeta;",950);
		addEntity(entitiesHTML,"eta;",951);
		addEntity(entitiesHTML,"theta;",952);
		addEntity(entitiesHTML,"iota;",953);
		addEntity(entitiesHTML,"kappa;",954);
		addEntity(entitiesHTML,"lambda;",955);
		addEntity(entitiesHTML,"mu;",956);
		addEntity(entitiesHTML,"nu;",957);
		addEntity(entitiesHTML,"xi;",958);
		addEntity(entitiesHTML,"omicron;",959);
		addEntity(entitiesHTML,"pi;",960);
		addEntity(entitiesHTML,"rho;",961);
		addEntity(entitiesHTML,"sigmaf;",962);
		addEntity(entitiesHTML,"sigma;",963);
		addEntity(entitiesHTML,"tau;",964);
		addEntity(entitiesHTML,"upsilon;",965);
		addEntity(entitiesHTML,"phi;",966);
		addEntity(entitiesHTML,"chi;",967);
		addEntity(entitiesHTML,"psi;",968);
		addEntity(entitiesHTML,"omega;",969);
		addEntity(entitiesHTML,"thetasym;",977);
		addEntity(entitiesHTML,"upsih;",978);
		addEntity(entitiesHTML,"piv;",982);
		addEntity(entitiesHTML,"OElig;",338);
		addEntity(entitiesHTML,"oelig;",339);
		addEntity(entitiesHTML,"Scaron;",352);
		addEntity(entitiesHTML,"scaron;",353);
		addEntity(entitiesHTML,"Yuml;",376);
		addEntity(entitiesHTML,"fnof;",402);
		addEntity(entitiesHTML,"circ;",710);
		addEntity(entitiesHTML,"tilde;",732);
		addEntity(entitiesHTML,"ndash;",8211);
		addEntity(entitiesHTML,"mdash;",8212);
		addEntity(entitiesHTML,"lsquo;",8216);
		addEntity(entitiesHTML,"rsquo;",8217);
		addEntity(entitiesHTML,"sbquo;",8218);
		addEntity(entitiesHTML,"ldquo;",8220);
		addEntity(entitiesHTML,"rdquo;",8221);
		addEntity(entitiesHTML,"bdquo;",8222);
		addEntity(entitiesHTML,"dagger;",8224);
		addEntity(entitiesHTML,"Dagger;",8225);
		addEntity(entitiesHTML,"bull;",8226);
		addEntity(entitiesHTML,"hellip;",8230);
		addEntity(entitiesHTML,"permil;",8240);
		addEntity(entitiesHTML,"prime;",8242);
		addEntity(entitiesHTML,"Prime;",8243);
		addEntity(entitiesHTML,"lsaquo;",8249);
		addEntity(entitiesHTML,"rsaquo;",8250);
		addEntity(entitiesHTML,"oline;",8254);
		addEntity(entitiesHTML,"euro;",8364);
		addEntity(entitiesHTML,"trade;",8482);
		addEntity(entitiesHTML,"larr;",8592);
		addEntity(entitiesHTML,"uarr;",8593);
		addEntity(entitiesHTML,"rarr;",8594);
		addEntity(entitiesHTML,"darr;",8595);
		addEntity(entitiesHTML,"harr;",8596);
		addEntity(entitiesHTML,"crarr;",8629);
		addEntity(entitiesHTML,"lceil;",8968);
		addEntity(entitiesHTML,"rceil;",8969);
		addEntity(entitiesHTML,"lfloor;",8970);
		addEntity(entitiesHTML,"rfloor;",8971);
		addEntity(entitiesHTML,"loz;",9674);
		addEntity(entitiesHTML,"spades;",9824);
		addEntity(entitiesHTML,"clubs;",9827);
		addEntity(entitiesHTML,"hearts;",9829);
		addEntity(entitiesHTML,"diams;",9830);
		addEntity(entitiesHTML,"larrow;",8592);
		addEntity(entitiesHTML,"uarrow;",8593);
		addEntity(entitiesHTML,"rarrow;",8594);
		addEntity(entitiesHTML,"darrow;",8595);
		addEntity(entitiesHTML,"harrow;",8596);
		addEntity(entitiesHTML,"crarrow;",8629);
		addEntity(entitiesHTML,"exists;",8707);

		addEntity(entitiesLaTeX,"leftarrow",8592);
		addEntity(entitiesLaTeX,"gets",8592);
		addEntity(entitiesLaTeX,"uparrow",8593);
		addEntity(entitiesLaTeX,"rightarrow",8594);
		addEntity(entitiesLaTeX,"to",8594);
		addEntity(entitiesLaTeX,"downarrow",8595);
		addEntity(entitiesLaTeX,"Leftarrow",8637);
		addEntity(entitiesLaTeX,"Uparrow",8657);
		addEntity(entitiesLaTeX,"Rightarrow",8658);
		addEntity(entitiesLaTeX,"Downarrow",8659);
		addEntity(entitiesLaTeX,"leftrightarrow",8703);
		addEntity(entitiesLaTeX,"Leftrightarrow",8660);
		addEntity(entitiesLaTeX,"updownarrow",11021);
		addEntity(entitiesLaTeX,"Updownarrow",8661);
		addEntity(entitiesLaTeX,"iff",8660);
		addEntity(entitiesLaTeX,"leadsto",10547);
		addEntity(entitiesLaTeX,"mapsto",10236);
		addEntity(entitiesLaTeX,"gt",">");
		addEntity(entitiesLaTeX,"lt","<");
		addEntity(entitiesLaTeX,"ge",8805);
		addEntity(entitiesLaTeX,"geq",8805);
		addEntity(entitiesLaTeX,"le",8804);
		addEntity(entitiesLaTeX,"leq",8804);
		addEntity(entitiesLaTeX,"sim",8764);
		addEntity(entitiesLaTeX,"simeq",8771);
		addEntity(entitiesLaTeX,"cong",8773);
		addEntity(entitiesLaTeX,"asymp",8776);
		addEntity(entitiesLaTeX,"ne",8800);
		addEntity(entitiesLaTeX,"neq",8800);
		addEntity(entitiesLaTeX,"equiv",8801);
		addEntity(entitiesLaTeX,"approx",8776);
		addEntity(entitiesLaTeX,"Alpha",913);
		addEntity(entitiesLaTeX,"Beta",914);
		addEntity(entitiesLaTeX,"Gamma",915);
		addEntity(entitiesLaTeX,"Delta",916);
		addEntity(entitiesLaTeX,"Epsilon",917);
		addEntity(entitiesLaTeX,"Zeta",918);
		addEntity(entitiesLaTeX,"Eta",919);
		addEntity(entitiesLaTeX,"Theta",920);
		addEntity(entitiesLaTeX,"Iota",921);
		addEntity(entitiesLaTeX,"Kappa",922);
		addEntity(entitiesLaTeX,"Lambda",923);
		addEntity(entitiesLaTeX,"Mu",924);
		addEntity(entitiesLaTeX,"Nu",925);
		addEntity(entitiesLaTeX,"Xi",926);
		addEntity(entitiesLaTeX,"Omicron",927);
		addEntity(entitiesLaTeX,"Pi",928);
		addEntity(entitiesLaTeX,"Rho",929);
		addEntity(entitiesLaTeX,"Sigma",931);
		addEntity(entitiesLaTeX,"Tau",932);
		addEntity(entitiesLaTeX,"Upsilon",933);
		addEntity(entitiesLaTeX,"Phi",934);
		addEntity(entitiesLaTeX,"Chi",935);
		addEntity(entitiesLaTeX,"Psi",936);
		addEntity(entitiesLaTeX,"Omega",937);
		addEntity(entitiesLaTeX,"alpha",945);
		addEntity(entitiesLaTeX,"beta",946);
		addEntity(entitiesLaTeX,"gamma",947);
		addEntity(entitiesLaTeX,"delta",948);
		addEntity(entitiesLaTeX,"epsilon",949);
		addEntity(entitiesLaTeX,"zeta",950);
		addEntity(entitiesLaTeX,"eta",951);
		addEntity(entitiesLaTeX,"theta",952);
		addEntity(entitiesLaTeX,"iota",953);
		addEntity(entitiesLaTeX,"kappa",954);
		addEntity(entitiesLaTeX,"lambda",955);
		addEntity(entitiesLaTeX,"mu",956);
		addEntity(entitiesLaTeX,"nu",957);
		addEntity(entitiesLaTeX,"xi",958);
		addEntity(entitiesLaTeX,"omicron",959);
		addEntity(entitiesLaTeX,"pi",960);
		addEntity(entitiesLaTeX,"rho",961);
		addEntity(entitiesLaTeX,"sigmaf",962);
		addEntity(entitiesLaTeX,"sigma",963);
		addEntity(entitiesLaTeX,"tau",964);
		addEntity(entitiesLaTeX,"upsilon",965);
		addEntity(entitiesLaTeX,"phi",966);
		addEntity(entitiesLaTeX,"chi",967);
		addEntity(entitiesLaTeX,"psi",968);
		addEntity(entitiesLaTeX,"omega",969);
		addEntity(entitiesLaTeX,"thetasym",977);
		addEntity(entitiesLaTeX,"prod",8719);
		addEntity(entitiesLaTeX,"sum",8721);
		addEntity(entitiesLaTeX,"int",8747);
		addEntity(entitiesLaTeX,"cdot",8729);
		addEntity(entitiesLaTeX,"cup",8899);
		addEntity(entitiesLaTeX,"cap",8898);
		addEntity(entitiesLaTeX,"mathbbN",8469);
		addEntity(entitiesLaTeX,"setN",8469);
		addEntity(entitiesLaTeX,"mathbbZ",8484);
		addEntity(entitiesLaTeX,"setZ",8484);
		addEntity(entitiesLaTeX,"mathbbQ",8474);
		addEntity(entitiesLaTeX,"setQ",8474);
		addEntity(entitiesLaTeX,"mathbbR",8477);
		addEntity(entitiesLaTeX,"setR",8477);
		addEntity(entitiesLaTeX,"mathbbC",8450);
		addEntity(entitiesLaTeX,"setC",8450);
		addEntity(entitiesLaTeX,"subset",8834);
		addEntity(entitiesLaTeX,"subseteq",8838);
		addEntity(entitiesLaTeX,"supset",8835);
		addEntity(entitiesLaTeX,"supseteq",8839);
		addEntity(entitiesLaTeX,"in",8712);
		addEntity(entitiesLaTeX,"notin",8713);
		addEntity(entitiesLaTeX,"ni",8715);
		addEntity(entitiesLaTeX,"notni",8716);
		addEntity(entitiesLaTeX,"cdots",8943);
		addEntity(entitiesLaTeX,"forall",8704);
		addEntity(entitiesLaTeX,"exists",8707);
		addEntity(entitiesLaTeX,"infty",8734);
	}

	/**
	 * Verarbeitet eine einzelne Zeile
	 * @param text	Eingabe
	 * @return	Ausgabe
	 */
	public String process(final String text) {
		if (text==null) return null;
		if (lastSingleInput!=null && lastSingleOutput!=null && text.equals(lastSingleInput)) return lastSingleOutput;
		return lastSingleOutput=processIntern(lastSingleInput=text);
	}

	/**
	 * Verarbeitet eine mehrzeilige Eingabe
	 * @param text	Eingabe
	 * @return	Ausgabe
	 */
	public String[] process(final String[] text) {
		if (text==null) return null;
		if (lastMultiInput!=null && lastMultiOutput!=null && Arrays.deepEquals(text,lastMultiInput)) return lastMultiOutput;
		lastMultiInput=Arrays.copyOf(text,text.length);
		lastMultiOutput=new String[text.length];
		for (int i=0;i<text.length;i++) lastMultiOutput[i]=processIntern(text[i]);
		return lastMultiOutput;
	}

	/**
	 * Verarbeitet eine einzeilige Eingabe ohne Caching
	 * @param text	Eingabe
	 * @return	Ausgabe
	 * @see #process(String)
	 * @see #process(String[])
	 */
	private String processIntern(String text) {
		if (text==null) return null;
		if (text.contains("\\")) text=processMap(text,'\\',entitiesLaTeX);
		if (text.contains("&")) text=processMap(text,'&',entitiesHTML);
		return text;
	}

	/**
	 * Sucht und ersetzt Bezeichner in einem Text
	 * @param text	Eingabetext
	 * @param start	Startzeichen für einen Bezeichner
	 * @param map	Liste von Bezeichnern und Ersetzungstexten
	 * @return	Ausgabe
	 */
	private String processMap(final String text, final char start, final Map<Character,Map<String,String>> map) {
		int startIndex=0;
		final StringBuilder output=new StringBuilder();
		while (true) {
			final int index=text.indexOf(start,startIndex);
			if (index<0 || index==text.length()-1) {
				output.append(text.substring(startIndex));
				break;
			}
			if (index>startIndex) output.append(text.substring(startIndex,index));
			startIndex=index+1;

			boolean found=false;
			final Map<String,String> subMap=map.get(text.charAt(startIndex));
			if (subMap!=null) for (int len=mapKeysMaxLength;len>=1;len--) for (String key: subMap.keySet()) if (key.length()==len) {
				final int endIndex=startIndex+key.length();
				if (endIndex>text.length()) continue;
				if (!text.substring(startIndex,endIndex).equals(key)) continue;
				output.append(subMap.get(key));
				startIndex+=key.length();
				found=true;
				break;
			}

			if (!found) output.append(start);
		}
		return output.toString();
	}

	/**
	 * Erzeugt Zeilen für eine html-Tabelle mit den Symbolen einer Zuordnung.
	 * @param map	Zuordnung von Bezeichnern zu Symbolen
	 * @param firstChar	Zusätzliches (nicht in der Zuordnung in den Bezeichnern angegebenes) Symbol
	 * @param symbol	Funktion zur Übersetzung von Bezeichner und Symbol in einen Ausdruck für die html-Tabelle
	 * @return	Zeilen für eine html-Tabelle
	 * @see #getList()
	 */
	private static String getList(final Map<Character,Map<String,String>> map, final String firstChar, final BiFunction<String,String,String> symbol) {
		final StringBuilder result=new StringBuilder();
		for (Character c: map.keySet().stream().sorted().collect(Collectors.toList())) {
			final Map<String,String> subMap=map.get(c);
			for (String key: subMap.keySet().stream().sorted().collect(Collectors.toList())) {
				result.append("<tr><td>"+firstChar+key+"</td><td>"+symbol.apply(key,subMap.get(key))+"</td></tr>\n");
			}
		}
		return result.toString();
	}

	/**
	 * Erzeugt über {@code System.out.print(TextTransformer.getList())} eine
	 * html-Liste aller unterstützten html- und LaTeX-Symbole.
	 * @return	html-Liste aller unterstützten html- und LaTeX-Symbole
	 */
	public static String getList() {
		final StringBuilder result=new StringBuilder();

		result.append("<table>\n");
		result.append("<tr><td><b>HTML</b></td><td><b>Symbol</b></td></tr>\n");
		result.append(getList(entitiesHTML,"&amp;",(key,value)->"&"+key));
		result.append("</table>\n");

		result.append("<table>\n");
		result.append("<tr><td><b>LaTeX</b></td><td><b>Symbol</b></td></tr>\n");
		result.append(getList(entitiesLaTeX,"\\",(key,value)->{
			if (key.equals("geq")) key="ge";
			if (key.equals("leq")) key="le";
			if (key.equals("neq")) key="ne";
			if (key.equals("%")) return "%";
			if (key.equals("{")) return "{";
			if (key.equals("}")) return "}";
			return "&"+key+";";
		}));
		result.append("</table>\n");

		return result.toString();
	}

	/**
	 * Liefert eine Liste mit allen Symbolnamen in der Form, in der sie die Rechtschreibprüfung verarbeiten kann.
	 * @return	Liste mit allen Symbolnamen
	 */
	public static Set<String> getAllSymbolsPlain() {
		final Set<String> results=new HashSet<>();

		entitiesHTML.values().forEach(map->map.keySet().stream().map(s->s.substring(0,s.length()-1)).forEach(results::add));
		entitiesLaTeX.values().forEach(map->map.keySet().stream().forEach(results::add));

		return results;
	}

	/**
	 * Liefert eine Liste mit allen Symbolnamen in der Form, in der sie die Autovervollständigung verarbeiten kann.
	 * @return	Liste mit allen Symbolnamen
	 */
	public static Set<String> getAllSymbolsFull() {
		final Set<String> results=new HashSet<>();

		entitiesHTML.values().forEach(map->map.keySet().stream().map(s->"&"+s).forEach(results::add));
		entitiesLaTeX.values().forEach(map->map.keySet().stream().map(s->"\\"+s).forEach(results::add));

		return results;
	}
}