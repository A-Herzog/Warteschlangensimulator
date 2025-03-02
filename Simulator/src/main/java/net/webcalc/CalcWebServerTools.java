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
package net.webcalc;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLTools;

/**
 * Diese Klasse stellt statische Hilfsroutinen für
 * {@link CalcWebServer} zur Verfügung.
 * @author Alexander Herzog
 * @see CalcWebServer
 */
public class CalcWebServerTools {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur statische Hilfsroutinen zur Verfügung.
	 */
	private CalcWebServerTools() {}

	/**
	 * Wandelt eine json-Zeichenkette in ein xml-Objekt um.
	 * @param rootName	Root-Name für das XML-Objekt
	 * @param json	json-Eingabe-Zeichenkette
	 * @return	xml-Ausgabe-Objekt
	 */
	public static Document jsonToXml(final String rootName, final String json) {
		if (json==null) return null;
		if (json.trim().isEmpty()) return null;

		final String fullJson="{\""+rootName+"\": "+json+"}";
		final Element root=directJsonToXmlConverter(fullJson);
		if (root==null) return null;

		return root.getOwnerDocument();
	}

	/**
	 * Wandelt eine json-Zeichenkette in eine xml-Zeichenkette um.
	 * @param rootName	Root-Name für das XML-Objekt
	 * @param json	json-Eingabe-Zeichenkette
	 * @return	xml-Ausgabe-Zeichenkette
	 */
	public static String jsonToXmlString(final String rootName, final String json) {
		final Document doc=jsonToXml(rootName,json);
		if (doc==null) return null;

		final ByteArrayOutputStream bytes=new ByteArrayOutputStream();
		final XMLTools xml=new XMLTools(bytes);
		if (!xml.save(doc)) return null;
		return new String(bytes.toByteArray());
	}

	/**
	 * Wandelt eine json-Zeichenkette in eine xml-Zeichenkette um.
	 * @param json	json-Eingabe-Zeichenkette
	 * @return	xml-Ausgabe-Zeichenkette
	 * @see #jsonToXml(String, String)
	 */
	private static Element directJsonToXmlConverter(String json) {
		json=json.replace("\n"," ").replace("\r","").trim();
		if (json.length()<4) return null;
		if (json.charAt(json.length()-1)==';') json=json.substring(0,json.length()-1);
		return loadJsonObject(null,json);
	}

	/**
	 * Liefert einen Teil-String, der außerdem noch um Leerzeichen am
	 * Anfang und Ende bereinigt wird.<br>
	 * Der Funktionsaufruf ist identisch zu {@link String#substring(int, int)}
	 * gefolgt von {@link String#trim()}. Allerdings ist diese Methode
	 * speichersparsamer.
	 * @param text	Zu verarbeitender Text
	 * @param beginIndex	Startindex für den Substring (inklusive)
	 * @param endIndex	Endindex für den Substring (exklusive)
	 * @return	Teil-String
	 */
	private static String substringAndTrim(final String text, int beginIndex, int endIndex) {
		while (beginIndex<endIndex && text.charAt(beginIndex)<=' ') beginIndex++;
		while (beginIndex<endIndex && text.charAt(endIndex-1)<=' ') endIndex--;
		if (beginIndex==endIndex) return "";
		return text.substring(beginIndex,endIndex);
	}

	/**
	 * Interpretiert den Inhalt eines json-Strings und erstellt entsprechende Unterelemente unter einem vorgegebenen xml-Elternelement
	 * @param parent	xml-Elternelement
	 * @param json	Zu interpretierendes json-String
	 * @return	Liefert im Erfolgsfall <code>true</code>. Wenn der Text nicht als json-Daten interpretiert werden konnte, <code>false</code>.
	 * @see #directJsonToXmlConverter(String)
	 */
	private static Element loadJsonObject(final Element parent, String json) {
		if (json==null || json.length()<4) return null;
		if (json.charAt(0)!='{' ||  json.charAt(json.length()-1)!='}') return null;
		/* json=json.substring(1,json.length()-1); - lösen wir jetzt indirekt */

		final int i=json.indexOf(':');
		if (i<1) return null;

		/* Schlüssel */
		String name;
		if (i>2 && json.charAt(1)=='"' && json.charAt(i-1)=='"') {
			name=substringAndTrim(json,2,i-1); /* "2" für "{\"" */
		} else {
			name=substringAndTrim(json,1,i); /* "1" für "{" */
		}
		if (name.length()>2 && name.charAt(0)=='"' && name.charAt(name.length()-1)=='"') { /* Das trim() kann dazu führen, dass die Bedingung erst jetzt erfüllt ist. */
			name=substringAndTrim(name,1,name.length()-1);
		}

		/* Wert */
		json=substringAndTrim(json,i+1,json.length()-1); /* json.length()-1 um das "}" zu entfernen */

		final int dataLength=json.length();
		if (dataLength==2) {
			if (json.charAt(0)!='{' || json.charAt(dataLength-1)!='}') return null;
			json="";
		} else {
			if (dataLength<3) return null;
			if (json.charAt(0)!='{' || json.charAt(dataLength-1)!='}') return null;
			json=substringAndTrim(json,1,dataLength-1);
		}

		Element element;
		if (parent==null) {
			element=XMLTools.generateRootStatic(name,true);
		} else {
			Document doc=parent.getOwnerDocument();
			parent.appendChild(element=doc.createElement(name));
		}

		if (!json.isEmpty()) {
			if (!loadJsonContent(element,json)) return null;
		}

		return element;
	}

	/**
	 * Trennt den json-Inhalt in einem Teil eines Strings an Trennzeichen (Kommata) auf
	 * @param json	Zeichenkette, die den json-Inhalt enthälz
	 * @param beginIndex	Erstes zu berücksichtigendes Zeichen (inklusive)
	 * @param endIndex	Erstes nicht mehr zu berücksichtigendes Zeichen (d.h. bezogen auf die Bearbeitung exklusive dieses Indices)
	 * @return	An Kommata aufgetrennter json-Inhalt
	 * @see #loadJsonContent(Element, String)
	 */
	private static String[] splitData(final String json, final int beginIndex, final int endIndex) {
		final List<String> data=new ArrayList<>();

		boolean inString=false;
		boolean lastWasEscape=false;
		boolean isEmpty=true;
		int blockCount=0;

		final char[] ch;
		if (json.length()<10_000_000) {
			ch=json.toCharArray();
		} else {
			ch=null;
		}
		final StringBuilder part=new StringBuilder(Math.min(1_000_000,endIndex-beginIndex));
		for (int i=beginIndex;i<endIndex;i++) {
			char c=(ch!=null)?ch[i]:json.charAt(i);
			boolean thisIsEscape=false;
			boolean dataSeparator=false;

			if (c=='\\' && !lastWasEscape) thisIsEscape=true;

			if (inString) {
				if (c=='"' && !lastWasEscape) inString=false;
			} else {
				if (c=='"' && !lastWasEscape) inString=true;
				if ((c=='{' || c=='[') && !lastWasEscape) blockCount++;
				if ((c=='}' || c==']') && !lastWasEscape && blockCount>0) blockCount--;
				if (c==',' && !lastWasEscape && blockCount==0) dataSeparator=true;
			}

			if (dataSeparator) {
				data.add(part.toString().trim());
				part.setLength(0);
				isEmpty=true;
			} else {
				if (!isEmpty || c!=' ') {part.append(c); isEmpty=false;}
			}

			lastWasEscape=thisIsEscape;
		}
		if (!isEmpty) data.add(part.toString().trim());

		return data.toArray(String[]::new);
	}

	/**
	 * Lädt Attribute, Textinhalte und Unterelemente aus einem json-String in ein xml-Element
	 * @param element	xml-Element in das die Daten geladen werden sollen
	 * @param json	json-Text dessen Daten in das xml-Element übertragen werden sollen
	 * @return	Liefert im Erfolgsfall <code>true</code>. Wenn der Text nicht als json-Daten interpretiert werden konnte, <code>false</code>.
	 * @see #loadJsonObject(Element, String)
	 */
	private static boolean loadJsonContent(final Element element, final String json) {
		final String[] data=splitData(json,0,json.length());

		for (int i=0;i<data.length;i++) {

			int j=data[i].indexOf(':');
			if (j<1) return false;

			String name=substringAndTrim(data[i],0,j);
			if (name.length()>2 && name.charAt(0)=='"' && name.charAt(name.length()-1)=='"') name=substringAndTrim(name,1,name.length()-1);
			String param=substringAndTrim(data[i],j+1,data[i].length());
			data[i]=null; /* Speicher sparen */
			if (param.length()<2) return false;

			if (name.equalsIgnoreCase("xmlcontent")) {
				if (param.charAt(0)!='"' || param.charAt(param.length()-1)!='"') return false;
				param=param.substring(1,param.length()-1);
				element.setTextContent(param.replace("\\n","\n").replace("\\\"","\""));
				continue;
			}

			if (name.equalsIgnoreCase("xmlchildren")) {
				if (param.charAt(0)!='[' || param.charAt(param.length()-1)!=']') return false;
				final String[] children=splitData(param,1,param.length()-1);
				param=null; /* Speicher sparen */
				for (int k=0;k<children.length;k++) {
					if (loadJsonObject(element,children[k])==null) return false;
					children[k]=null; /* Speicher sparen */
				}
				continue;
			}

			if (param.charAt(0)=='[' && param.charAt(param.length()-1)==']') {
				final String[] children=splitData(param,1,param.length()-1);
				param=null; /* Speicher sparen */
				for (int k=0;k<children.length;k++) {
					if (children[k].charAt(0)=='"' && children[k].charAt(children[k].length()-1)=='"') {
						Element sub=element.getOwnerDocument().createElement(name);
						element.appendChild(sub);
						sub.setTextContent(children[k].substring(1,children[k].length()-1));
						continue;
					}

					if (children[k].charAt(0)!='{' || children[k].charAt(children[k].length()-1)!='}') return false;
					final String content=substringAndTrim(children[k],1,children[k].length()-1);
					Element sub=element.getOwnerDocument().createElement(name);
					element.appendChild(sub);
					if (!loadJsonContent(sub,content)) return false;
					children[k]=null; /* Speicher sparen */
				}
				continue;
			}

			if (param.charAt(0)=='{' && param.charAt(param.length()-1)=='}') {
				param=param.substring(1,param.length()-1);
				Element sub=element.getOwnerDocument().createElement(name);
				element.appendChild(sub);
				if (!loadJsonContent(sub,param)) return false;
				continue;
			}

			if (param.charAt(0)=='"' && param.charAt(param.length()-1)=='"') {
				param=param.substring(1,param.length()-1);
			}

			final String value=param.replace("\\n","\n").replace("\\\"","\"");
			element.setAttribute(name,value);
		}

		return true;
	}
}
