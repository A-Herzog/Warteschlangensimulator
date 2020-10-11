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
package ui.modeleditor.elements;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import language.Language;

/**
 * Speichert einmal erstellte Font-Objekte für die Element-übergreifende Verwendung
 * @author Alexander Herzog
 * @see ModelElementAnimationTextValue
 * @see ModelElementAnimationTextValueJS
 * @see ModelElementText
 */
public class FontCache {
	/**
	 * Zusammenstellung der verfügbaren Schriftarten
	 * @author Alexander Herzog
	 */
	public enum FontFamily {
		/** Standard serifenlose Schriftart */
		SANS("SansSerif",()->Language.tr("FontFamily.SansSerif")),

		/** Standard serifenbehaftete Schriftart */
		SERIF("Serif",()->Language.tr("FontFamily.Serif")),

		/** Standard Schreibmaschinenschrift */
		TT("Monospaced",()->Language.tr("FontFamily.Monospaced")),

		/** Java-Standardschriftart für Dialoge */
		DIALOG("Dialog",()->Language.tr("FontFamily.Dialog")),

		/** Windows-Schriftart "Calibri" */
		WIN_CALIBRI("Calibri"),

		/** Windows-Schriftart "Cambria" */
		WIN_CAMBRIA("Cambria"),

		/** Windows-Schriftart "Lucida Console" */
		WIN_LUCIDA_CONSOLE("Lucida Console"),

		/** Windows-Schriftart "Tahoma" */
		WIN_TAHOMA("Tahoma"),

		/** Windows-Schriftart "Verdana" */
		WIN_VERDANA("Verdana");

		/** Name der Schriftart (aus Font-Objekt-Sicht) */
		public final String name;

		/** Lokalisierter Name der Schriftart (kann <code>null</code> sein) */
		private final Supplier<String> localName;

		/**
		 * Konstruktor des Enum
		 * @param name	Name der Schriftart (aus Font-Objekt-Sicht)
		 */
		FontFamily(final String name) {
			this.name=name;
			localName=null;
		}

		/**
		 * Konstruktor des Enum
		 * @param name	Name der Schriftart (aus Font-Objekt-Sicht)
		 * @param localName	Lokalisierter Name der Schriftart
		 */
		FontFamily(final String name, final Supplier<String> localName) {
			this.name=name;
			this.localName=localName;
		}

		/**
		 * Liefert den lokalisierten Namen der Schriftart (für Dropdown-Auswahllisten)
		 * @return	Lokalisierter Name der Schriftart
		 */
		public String getLocalName() {
			if (localName==null) return name;
			return localName.get();
		}
	}

	/**
	 * Standardschriftart
	 */
	public static final FontFamily defaultFamily=FontFamily.DIALOG;

	private final Map<FontFamily,Map<Integer,Map<Integer,Font>>> map;
	private final Map<String,FontFamily> nameLowerMap;

	/**
	 * Das Objekt ist ein Singleton und kann nicht direkt instanziert werden.
	 * Stattdessen muss die statische Factory-Methode <code>getFontCache()</code>
	 * verwendet werden.
	 * @see #getFontCache()
	 */
	private FontCache() {
		map=new HashMap<>();
		nameLowerMap=new HashMap<>();
		for (FontFamily family: FontFamily.values()) {
			map.put(family,new HashMap<>());
			nameLowerMap.put(family.name.toLowerCase(),family);
		}
	}

	private static FontCache fontCache;

	static {
		fontCache=new FontCache();
	}

	/**
	 * Liefert die Instanz der <code>FontCache</code>-Klasse.
	 * @return	Instanz der <code>FontCache</code>-Klasse
	 */
	public static FontCache getFontCache() {
		return fontCache;
	}

	/**
	 * Liefert das zugehörige {@link FontCache.FontFamily}-Objekt zu einem Namen
	 * @param name	Schriftartenname für das das {@link FontCache.FontFamily}-Objekt geliefert werden soll
	 * @return	Objekt welches die Schriftart repräsentiert
	 */
	public FontFamily getFamilyFromName(final String name) {
		if (name==null) return defaultFamily;
		final FontFamily family=nameLowerMap.get(name.trim().toLowerCase());
		if (family!=null) return family;
		return defaultFamily;
	}

	/**
	 * Liefert ein <code>Font</code>-Objekt in der angegebenen Form
	 * entweder aus dem Cache oder in dem es neu erstellt wird.
	 * @param family	Name der Schriftfamilie
	 * @param style	Stil des Fonts
	 * @param size	Schriftgröße für Font
	 * @return	<code>Font</code>-Objekt
	 */
	public Font getFont(final String family, final int style, final int size) {
		if (family==null) return getFont(defaultFamily,style,size);
		return getFont(nameLowerMap.get(family.trim().toLowerCase()),style,size);
	}

	/**
	 * Liefert ein <code>Font</code>-Objekt in der angegebenen Form
	 * entweder aus dem Cache oder in dem es neu erstellt wird.
	 * @param family	Schriftfamilie
	 * @param style	Stil des Fonts
	 * @param size	Schriftgröße für Font
	 * @return	<code>Font</code>-Objekt
	 */
	public Font getFont(final FontFamily family, final int style, final int size) {
		final FontFamily actualFamily=(family==null)?defaultFamily:family;

		final Map<Integer,Map<Integer,Font>> familyMap=map.get(actualFamily);
		Map<Integer,Font> styleMap=familyMap.get(style);
		if (styleMap==null) {
			styleMap=new HashMap<>();
			familyMap.put(style,styleMap);
		}
		Font font=styleMap.get(size);
		if (font==null) {
			font=new Font(actualFamily.name,style,size);
			styleMap.put(size,font);
		}
		return font;
	}
}