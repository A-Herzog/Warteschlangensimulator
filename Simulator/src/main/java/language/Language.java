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
package language;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.w3c.dom.Element;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * Diese Klasse erm�glicht die Internationalisierung des Programms
 * in dem sie die Zuordnung von Bezeichnern zu jeweils landessprachlichen
 * Texten herstellt.
 * Die Klasse kann nicht direkt instanziert werden, sondern arbeitet intern
 * als Singleton. Der Zugriff erfolgt �ber statische Methoden.
 * @author Alexander Herzog
 * @version 1.1
 */
public class Language {
	/** Wenn das Programm mit eingeschr�nkten Rechten l�uft, ist kein Zugriff auf die Lokalisierungsressourcen m�glich, so dass in diesem Fall ein direkter Zugriff auf die Java-�bersetzungsklassen n�tig ist. */
	public static boolean noRightsMode=false;

	/** Daten direkt aus einer Java-Klasse statt aus Lokalisierungsressourcen laden. */
	public static boolean directLoadingMode=true;

	/** Internationalisierungsdaten f�r Deutsch */
	private final I18n i18n_de;
	/** Internationalisierungsdaten f�r Englisch */
	private final I18n i18n_en;
	/** Momentan aktive Internationalisierungsdaten */
	private I18n i18n;
	/** Ressourcendaten f�r Deutsch */
	private final ResourceBundle res_de;
	/** Ressourcendaten f�r Englisch */
	private final ResourceBundle res_en;
	/** Momentan aktive Ressourcendaten */
	private ResourceBundle res;
	/** K�rzel der momentan aktiven Sprache */
	private String languageID;

	/** Instanz des Language-Singletons */
	private static Language language=null;

	/** Cache f�r {@link #tr(String)} */
	private static Map<String,String> cache=new HashMap<>();
	/** Cache f�r {@link #trAll(String)} */
	private static Map<String,String[]> allCache=new HashMap<>();

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht direkt instanziert werden.
	 * @param loadDirect	Daten direkt aus einer Java-Klasse (<code>true</code>) statt aus Lokalisierungsressourcen (<code>false</code>) laden.
	 */
	private Language(boolean loadDirect) {
		if (loadDirect || noRightsMode) {
			res_de=new Messages_de();
			res_en=new Messages_en();
			res=res_en;
			i18n_de=null;
			i18n_en=null;
			i18n=null;
		} else {
			res_de=null;
			res_en=null;
			res=null;
			i18n_de=I18nFactory.getI18n(Language.class,"Messages",Locale.GERMAN);
			i18n_en=I18nFactory.getI18n(Language.class,"Messages",Locale.ENGLISH);
			i18n=i18n_en;
		}
	}

	/**
	 * Stellt die momentan aktive Sprache ein.
	 * @param language	K�rzel der Sprache
	 * @see #init(String)
	 */
	private void selectLanguage(String language) {
		i18n=(language.equalsIgnoreCase("de"))?i18n_de:i18n_en;
		res=(language.equalsIgnoreCase("de"))?res_de:res_en;
		languageID=language;
	}

	/**
	 * Liefert das K�rzel der momentan aktiven Sprache.
	 * @return	K�rzel der Sprache
	 */
	private String getCurrentLanguageID() {
		return languageID;
	}

	/**
	 * Listet die K�rzel der vorhandenen Sprachen auf.
	 * @return	Liste der K�rzel der vorhandenen Sprachen
	 */
	public static String[] getLanguages() {
		return new String[]{"de","en"};
	}

	/**
	 * Gibt an, ob eine Sprach-ID zu einer unterst�tzten Sprache f�hrt
	 * @param language	Sprach-ID
	 * @return	Bei "de" oder "en" f�r die Sprache wird <code>true</code> zur�ckgegeben.
	 */
	public static boolean isSupportedLanguage(final String language) {
		for (String lang: getLanguages()) if (lang.equalsIgnoreCase(language)) return true;
		return false;
	}

	/**
	 * W�hlt die zu verwendende Sprache.
	 * @param languageID Kann "de" oder "en" sein.
	 */
	public static synchronized void init(String languageID) {
		if (language==null) language=new Language(directLoadingMode);
		language.selectLanguage(languageID);
		cache.clear();
		allCache.clear();
	}

	/**
	 * Liefert die ID der aktuell gew�hlten Sprache
	 * @return	ID der aktuell gew�hlten Sprache
	 */
	public static String getCurrentLanguage() {
		if (language==null) return "";
		return language.getCurrentLanguageID();
	}

	/**
	 * Liefert den vollst�ndigen Language-String in der jeweils gew�hlten Sprache f�r einen Bezeichner.
	 * @param id	Bezeichner
	 * @return	Language-String
	 */
	public static synchronized String tr(String id) {
		String s=cache.get(id);
		if (s!=null) return s;

		if (language==null) language=new Language(directLoadingMode);
		if (language.i18n!=null) s=language.i18n.tr(id);
		if (s==null && language.res!=null) try {
			s=language.res.getString(id);
		} catch (Exception e) {s=null;}
		if (s==null || s.equals(id)) {System.out.println("Warning: Missing language string "+id); return id;}
		cache.put(id,s);
		return s;
	}

	/**
	 * Liefert den vollst�ndigen Language-String in der jeweils <b>nicht</b> gew�hlten Sprache f�r einen Bezeichner.
	 * @param id	Bezeichner
	 * @return	Language-String
	 */
	public static synchronized List<String> trOther(String id) {
		if (language==null) language=new Language(directLoadingMode);
		List<String> list=new ArrayList<>();
		if (!noRightsMode) {
			if (language.i18n_de!=null && language.i18n!=language.i18n_de) {String s=language.i18n_de.tr(id); if (s!=null) list.add(s);}
			if (language.i18n_en!=null && language.i18n!=language.i18n_en) {String s=language.i18n_en.tr(id); if (s!=null) list.add(s);}
			if (language.res_de!=null && language.res!=language.res_de) {String s=null; try{s=language.res_de.getString(id);} catch (Exception e) {s=null;} if (s!=null) list.add(s);}
			if (language.res_en!=null && language.res!=language.res_en) {String s=null; try{s=language.res_en.getString(id);} catch (Exception e) {s=null;} if (s!=null) list.add(s);}
		}
		return list;
	}

	/**
	 * Liefert den Prim�r-Anteil (d.h. den Teil vor dem ersten Semikollon) eines Language-Strings in der jeweils gew�hlten Sprache f�r einen Bezeichner.
	 * @param id	Bezeichner
	 * @return	Language-String-Anteil
	 */
	public static String trPrimary(String id) {
		String[] s=tr(id).split(";");
		if (s.length==0) {System.out.println("Warning: Invalid multi language string "+id); return id;}
		return s[0];
	}

	/**
	 * Liefert eine Liste der Anteile (die in der Datei durch Semikollons getrennt sind) eines Language-Strings in der jeweils gew�hlten Sprache f�r einen Bezeichner.
	 * @param id	Bezeichner
	 * @return	Liste der Language-String-Anteile
	 */
	public static synchronized String[] trAll(String id) {
		String[] result=allCache.get(id);
		if (result!=null) return result;

		List<String> all=new ArrayList<>();
		String value=tr(id);
		if (value.contains(";")) all.addAll(Arrays.asList(value.split(";"))); else all.add(value);
		for (String s: trOther(id)) {
			if (s.contains(";")) all.addAll(Arrays.asList(s.split(";"))); else all.add(s);
		}
		result=all.toArray(new String[0]);
		allCache.put(id,result);
		return result;
	}

	/**
	 * Pr�ft, ob ein eine Zeichenkette mit einem Anteil (die in der Datei durch Semikolons getrennt sind) eines Language-Strings in der jeweils gew�hlten Sprache �bereinstimmt.
	 * @param id	Bezeichner
	 * @param test	Zeichenkette, bei der gepr�ft werden soll, ob sie (ohne Ber�cksichtigung der Gro�- und Kleinschreibung) mit einem der Teil-Language-Strings �bereinstimmt
	 * @return	Gibt <code>true</code> zur�ck, wenn die Testzeichenkette mit einem der Teil-Language-Strings �bereinstimmt.
	 */
	public static boolean trAll(String id, String test) {
		if (test==null || test.isEmpty()) return false;
		for (String s: trAll(id)) if (s.equalsIgnoreCase(test)) return true;
		return false;
	}

	/**
	 * Pr�ft, ob ein XML-Element ein Attribut besitzt, welches vom Namen her mit einem Anteil (die in der Datei durch Semikollons getrennt sind) eines Language-Strings in der jeweils gew�hlten Sprache �bereinstimmt.
	 * @param id	Bezeichner
	 * @param node	XML-Element, dessen Attribute (ohne Ber�cksichtigung der Gro�- und Kleinschreibung) mit den Teil-Language-Strings verglichen werden sollen
	 * @return	Liefert den Inhalt des Attributes, wenn eine Attribut mit einem �bereinstimmenden Namen gefunden wurde, sonst eine leere Zeichenkette.
	 */
	public static String trAllAttribute(String id, Element node) {
		for (String name: trAll(id)) {
			String value=node.getAttribute(name);
			if (value!=null && !value.isEmpty()) return value;
		}
		return "";
	}
}