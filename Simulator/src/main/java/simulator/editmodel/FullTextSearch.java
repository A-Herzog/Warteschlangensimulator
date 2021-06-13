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
package simulator.editmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import ui.modeleditor.ElementRendererTools;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Diese Klasse ermöglicht die Volltextsuche über ein Editor-Modell.
 * @author Alexander Herzog
 */
public class FullTextSearch {
	/**
	 * Mögliche Optionen für die Suche
	 */
	public enum SearchOption {
		/** Groß- und Kleinschreibung berücksichtigen? */
		CASE_SENSITIVE,
		/** Auch IDs von Stationen bei der Suche mit heranziehen? */
		SEARCH_IDS,
		/** Sollen Treffer gefunden werden, die dem Suchbegriff exakt entsprechen (und nicht welche, die ihn enthalten, aber nicht exakt gleich sind)? */
		FULL_MATCH_ONLY,
		/** Suchbegriff ist regulärer Ausdruck? */
		REGULAR_EXPRESSION,
	}

	/**
	 * Suchbegriff
	 */
	private final String searchText;

	/**
	 * Suchbegriff in Kleinbuchstaben<br>
	 * Ist dieses Feld mit einem Wert ungleich <code>null</code> belegt,
	 * so bedeutet dies, dass die Suche die Groß- und Kleinschreibung
	 * nicht berücksichtigen soll und dass die Suche nicht über einen
	 * regulären Ausdruck läuft.
	 */
	private final String searchTextLower;

	/**
	 * Regulärer Ausdruck nach dem gesucht werden soll.<br>
	 * Ist dieses Feld <code>null</code>, so wird nicht über einen regulären Ausdruck gesucht.
	 */
	private final Pattern regexPattern;

	/**
	 * Optionen für die Suche (ist nie <code>null</code>)
	 */
	private final Set<SearchOption> options;

	/**
	 * Liste der Suchtreffer
	 * @see #getResults()
	 */
	private final List<SearchMatch> matches;

	/**
	 * Konstruktor der Klasse
	 * @param searchText	Suchbegriff
	 * @param options	Optionen für die Suche (darf <code>null</code> oder leer sein)
	 */
	public FullTextSearch(final String searchText, final Set<SearchOption> options) {
		this.searchText=searchText.trim();
		if (options==null) this.options=new HashSet<>(); else this.options=new HashSet<>(options);
		matches=new ArrayList<>();

		/* Regulärer Ausdruck */
		if (this.options.contains(SearchOption.REGULAR_EXPRESSION)) {
			int flags=0;
			if (!this.options.contains(SearchOption.CASE_SENSITIVE)) flags+=Pattern.CASE_INSENSITIVE;
			regexPattern=Pattern.compile(searchText,flags);
		} else {
			regexPattern=null;
		}

		/* Berücksichtigung Groß- und Kleinschreibung (bei normaler Suche) */
		if (this.options.contains(SearchOption.CASE_SENSITIVE)) {
			searchTextLower=null;
		} else {
			searchTextLower=searchText.toLowerCase();
		}
	}

	/**
	 * Sollen auch IDs als mögliche Suchtreffer angesehen werden?
	 * @return	Liefert <code>true</code>, wenn auch Stations-IDs bei der Suche berücksichtigt werden sollen
	 */
	public boolean isTestIDs() {
		return options.contains(SearchOption.SEARCH_IDS);
	}

	/**
	 * Fügt einen Suchtreffer zur Liste der Treffer hinzu.
	 * @param station	Station an der der Text gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo der Suchtext gefunden wurde
	 * @param fullText	Vollständiger Text, der den Treffer enthält
	 * @param beginIndex	Erster Index der Suchtreffers im vollständigen Text
	 * @param endIndex	Erster Index im vollständigen Text, der nicht mehr zum Suchtreffer gehört
	 * @param setNewText	Funktion zur Einstellung eines neuen Textes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @param setNewInteger	Funktion zur Einstellung eines neuen Integer-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @param setNewLong	Funktion zur Einstellung eines neuen Long-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @param setNewDouble	Funktion zur Einstellung eines neuen Double-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert das bereits in die Liste eingefügte Treffer-Objekt
	 * @see #matches
	 * @see SearchMatch
	 */
	private SearchMatch addMatch(final ModelElement station, final String description, final String fullText, final int beginIndex, final int endIndex, final Consumer<String> setNewText, final IntConsumer setNewInteger, final LongConsumer setNewLong, final DoubleConsumer setNewDouble) {
		final SearchMatch match=new SearchMatch(station,description,fullText,beginIndex,endIndex,setNewText,setNewInteger,setNewLong,setNewDouble);
		matches.add(match);
		return match;
	}

	/**
	 * Überprüft, ob eine Zeichenkette zu dem Suchbegriff passt.
	 * @param element	Station an der der Text gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo der Suchtext gefunden wurde
	 * @param testString	Zu prüfende Zeichenkette
	 * @param setNewText	Funktion zur Einstellung eines neuen Textes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @param setNewInteger	Funktion zur Einstellung eines neuen Integer-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @param setNewLong	Funktion zur Einstellung eines neuen Long-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @param setNewDouble	Funktion zur Einstellung eines neuen Double-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert den Bereich des Suchtreffers zurück, wenn die Zeichenkette zu dem Suchbegriff passt, sonst <code>null</code>.
	 */
	public boolean testIntern(final ModelElement element, final String description, final String testString, final Consumer<String> setNewText, final IntConsumer setNewInteger, final LongConsumer setNewLong, final DoubleConsumer setNewDouble) {
		if (testString==null || testString.trim().isEmpty()) return false;

		if (regexPattern!=null) {
			final Matcher matcher=regexPattern.matcher(testString);
			if (options.contains(SearchOption.FULL_MATCH_ONLY)) {
				if (!matcher.matches()) return false;
				addMatch(element,description,testString,0,testString.length(),setNewText,setNewInteger,setNewLong,setNewDouble);
				return true;
			} else {
				if (!matcher.find()) return false;
				addMatch(element,description,testString,matcher.start(),matcher.end(),setNewText,setNewInteger,setNewLong,setNewDouble);
				return true;
			}
		}

		if (searchTextLower!=null) {
			if (options.contains(SearchOption.FULL_MATCH_ONLY)) {
				if (!testString.equalsIgnoreCase(searchText)) return false;
				addMatch(element,description,testString,0,testString.length(),setNewText,setNewInteger,setNewLong,setNewDouble);
				return true;
			} else {
				final int index=testString.toLowerCase().indexOf(searchTextLower);
				if (index<0) return false;
				addMatch(element,description,testString,index,index+searchText.length(),setNewText,setNewInteger,setNewLong,setNewDouble);
				return true;
			}
		}

		if (options.contains(SearchOption.FULL_MATCH_ONLY)) {
			if (!testString.equals(searchText)) return false;
			addMatch(element,description,testString,0,searchText.length(),setNewText,setNewInteger,setNewLong,setNewDouble);
			return true;
		} else {
			final int index=testString.indexOf(searchText);
			if (index<0) return false;
			addMatch(element,description,testString,index,index+searchText.length(),setNewText,setNewInteger,setNewLong,setNewDouble);
			return true;
		}
	}

	/*
	 * String
	 */

	/**
	 * Überprüft, ob eine Zeichenkette zu dem Suchbegriff passt.
	 * @param element	Station an der der Text gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo der Suchtext gefunden wurde
	 * @param testString	Zu prüfende Zeichenkette
	 * @param setNewText	Funktion zur Einstellung eines neuen Textes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert den Bereich des Suchtreffers zurück, wenn die Zeichenkette zu dem Suchbegriff passt, sonst <code>null</code>.
	 */
	public boolean testString(final ModelElement element, final String description, final String testString, final Consumer<String> setNewText) {
		return testIntern(element,description,testString,setNewText,null,null,null);
	}

	/**
	 * Überprüft, ob eine Zeichenkette zu dem Suchbegriff passt.
	 * @param element	Station an der der Text gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo der Suchtext gefunden wurde
	 * @param testString	Zu prüfende Zeichenkette
	 * @return	Liefert <code>true</code> zurück, wenn die Zeichenkette zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testString(final ModelElement element, final String description, final String testString) {
		return testIntern(element,description,testString,null,null,null,null);
	}

	/**
	 * Überprüft, ob eine Zeichenkette zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo der Suchtext gefunden wurde
	 * @param testString	Zu prüfende Zeichenkette
	 * @param setNewText	Funktion zur Einstellung eines neuen Textes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert den Bereich des Suchtreffers zurück, wenn die Zeichenkette zu dem Suchbegriff passt, sonst <code>null</code>.
	 */
	public boolean testString(final String description, final String testString, final Consumer<String> setNewText) {
		return testIntern(null,description,testString,setNewText,null,null,null);
	}

	/**
	 * Überprüft, ob eine Zeichenkette zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo der Suchtext gefunden wurde
	 * @param testString	Zu prüfende Zeichenkette
	 * @return	Liefert <code>true</code> zurück, wenn die Zeichenkette zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testString(final String description, final String testString) {
		return testIntern(null,description,testString,null,null,null,null);
	}

	/*
	 * Integer
	 */

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param element	Station an der die Zahl gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testInteger	Zu prüfende Zahl
	 * @param setNewInteger	Funktion zur Einstellung eines neuen Integer-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testInteger(final ModelElement element, final String description, final int testInteger, final IntConsumer setNewInteger) {
		return testIntern(element,description,NumberTools.formatLongNoGrouping(testInteger),null,setNewInteger,null,null);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param element	Station an der die Zahl gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testInteger	Zu prüfende Zahl
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testInteger(final ModelElement element, final String description, final int testInteger) {
		return testIntern(element,description,NumberTools.formatLongNoGrouping(testInteger),null,null,null,null);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testInteger	Zu prüfende Zahl
	 * @param setNewInteger	Funktion zur Einstellung eines neuen Integer-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testInteger(final String description, final int testInteger, final IntConsumer setNewInteger) {
		return testIntern(null,description,NumberTools.formatLongNoGrouping(testInteger),null,setNewInteger,null,null);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testInteger	Zu prüfende Zahl
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testInteger(final String description, final int testInteger) {
		return testIntern(null,description,NumberTools.formatLongNoGrouping(testInteger),null,null,null,null);
	}

	/*
	 * Long
	 */

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param element	Station an der die Zahl gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testLong	Zu prüfende Zahl
	 * @param setNewLong	Funktion zur Einstellung eines neuen Long-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testLong(final ModelElement element, final String description, final long testLong, final LongConsumer setNewLong) {
		return testIntern(element,description,NumberTools.formatLongNoGrouping(testLong),null,null,setNewLong,null);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param element	Station an der die Zahl gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testLong	Zu prüfende Zahl	 *
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testLong(final ModelElement element, final String description, final long testLong) {
		return testIntern(element,description,NumberTools.formatLongNoGrouping(testLong),null,null,null,null);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testLong	Zu prüfende Zahl
	 * @param setNewLong	Funktion zur Einstellung eines neuen Long-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testLong(final String description, final long testLong, final LongConsumer setNewLong) {
		return testIntern(null,description,NumberTools.formatLongNoGrouping(testLong),null,null,setNewLong,null);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testLong	Zu prüfende Zahl
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testLong(final String description, final long testLong) {
		return testIntern(null,description,NumberTools.formatLongNoGrouping(testLong),null,null,null,null);
	}

	/*
	 * Double
	 */

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param element	Station an der die Zahl gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testDouble	Zu prüfende Zahl
	 * @param setNewDouble	Funktion zur Einstellung eines neuen Double-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testDouble(final ModelElement element, final String description, final double testDouble, final DoubleConsumer setNewDouble) {
		return testIntern(element,description,NumberTools.formatNumberMax(testDouble),null,null,null,setNewDouble);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param element	Station an der die Zahl gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testDouble	Zu prüfende Zahl
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testDouble(final ModelElement element, final String description, final double testDouble) {
		return testIntern(element,description,NumberTools.formatNumberMax(testDouble),null,null,null,null);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testDouble	Zu prüfende Zahl
	 * @param setNewDouble	Funktion zur Einstellung eines neuen Double-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testDouble(final String description, final double testDouble, final DoubleConsumer setNewDouble) {
		return testIntern(null,description,NumberTools.formatNumberMax(testDouble),null,null,null,setNewDouble);
	}

	/**
	 * Überprüft, ob eine Zahl zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo die Zahl gefunden wurde
	 * @param testDouble	Zu prüfende Zahl
	 * @return	Liefert <code>true</code> zurück, wenn die Zahl zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testDouble(final String description, final double testDouble) {
		return testIntern(null,description,NumberTools.formatNumberMax(testDouble),null,null,null,null);
	}

	/*
	 * AbstractRealDistribution
	 */

	/**
	 * Erstellt eine Beschreibungszeichenkette (in der dann gesucht werden kann) zu einer Wahrscheinlichkeitsverteilung
	 * @param testDistribution	Wahrscheinlichkeitsverteilung
	 * @return	Beschreibung der Wahrscheinlichkeitsverteilung (kann <code>null</code> sein, wenn keine Beschreibung erstellt werden konnte)
	 */
	private String buildDistributionString(final AbstractRealDistribution testDistribution) {
		if (testDistribution==null) return null;

		final String name=DistributionTools.getDistributionName(testDistribution);
		final String info=DistributionTools.getDistributionInfo(testDistribution);
		if (name==null || name.isEmpty()) return null;

		final StringBuilder infoBuilder=new StringBuilder();
		infoBuilder.append(name);
		if (info!=null && !info.isEmpty()) {
			infoBuilder.append(" (");
			infoBuilder.append(info);
			infoBuilder.append(")");
		}

		return infoBuilder.toString();
	}

	/**
	 * Überprüft, ob eine Wahrscheinlichkeitsverteilung zu dem Suchbegriff passt.
	 * @param element	Station an der die Wahrscheinlichkeitsverteilung gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
	 * @param description	Beschreibung, wo die Wahrscheinlichkeitsverteilung gefunden wurde
	 * @param testDistribution	Zu prüfende Wahrscheinlichkeitsverteilung
	 * @return	Liefert <code>true</code> zurück, wenn die Wahrscheinlichkeitsverteilung zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testDistribution(final ModelElement element, final String description, final AbstractRealDistribution testDistribution) {
		return testIntern(element,description,buildDistributionString(testDistribution),null,null,null,null);
	}

	/**
	 * Überprüft, ob eine Wahrscheinlichkeitsverteilung zu dem Suchbegriff passt.
	 * @param description	Beschreibung, wo die Wahrscheinlichkeitsverteilung gefunden wurde
	 * @param testDistribution	Zu prüfende Wahrscheinlichkeitsverteilung
	 * @return	Liefert <code>true</code> zurück, wenn die Wahrscheinlichkeitsverteilung zu dem Suchbegriff passt (der Treffer wurde dann bereits registriert)
	 */
	public boolean testDistribution(final String description, final AbstractRealDistribution testDistribution) {
		return testIntern(null,description,buildDistributionString(testDistribution),null,null,null,null);
	}

	/**
	 * Liefert die Liste der Suchtreffer
	 * @return	Liste der Suchtreffer (kann leer sein, ist aber nie <code>null</code>)
	 */
	public List<SearchMatch> getResults() {
		return new ArrayList<>(matches);
	}

	/**
	 * Diese Klasse repräsentiert einen Suchtreffer.
	 * @see FullTextSearch#getResults()
	 */
	public static class SearchMatch {
		/**
		 * Station an der der Text gefunden wurde<br>
		 * (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
		 */
		public final ModelElement station;

		/**
		 * Beschreibung, wo der Suchtext gefunden wurde
		 */
		public final String description;

		/**
		 * Vollständiger Text, der den Treffer enthält
		 */
		public final String fullText;

		/**
		 * Erster Index der Suchtreffers im vollständigen Text
		 */
		private final int beginIndex;

		/**
		 * Erster Index im vollständigen Text, der nicht mehr zum Suchtreffer gehört
		 */
		private final int endIndex;

		/**
		 * Funktion zur Einstellung eines neuen Textes<br>
		 * (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 */
		private final Consumer<String> setNewText;

		/**
		 * Funktion zur Einstellung eines neuen Integer-Wertes<br>
		 * (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 */
		private final IntConsumer setNewInteger;

		/**
		 * Funktion zur Einstellung eines neuen Long-Wertes<br>
		 * (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 */
		private final LongConsumer setNewLong;

		/**
		 * Funktion zur Einstellung eines neuen Double-Wertes<br>
		 * (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 */
		private final DoubleConsumer setNewDouble;

		/**
		 * Konstruktor der Klasse
		 * @param station	Station an der der Text gefunden wurde (kann <code>null</code> sein, wenn sich der Treffer nicht auf eine konkrete Station bezieht)
		 * @param description	Beschreibung, wo der Suchtext gefunden wurde
		 * @param fullText	Vollständiger Text, der den Treffer enthält
		 * @param beginIndex	Erster Index der Suchtreffers im vollständigen Text
		 * @param endIndex	Erster Index im vollständigen Text, der nicht mehr zum Suchtreffer gehört
		 * @param setNewText	Funktion zur Einstellung eines neuen Textes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 * @param setNewInteger	Funktion zur Einstellung eines neuen Integer-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 * @param setNewLong	Funktion zur Einstellung eines neuen Long-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 * @param setNewDouble	Funktion zur Einstellung eines neuen Double-Wertes (kann <code>null</code> sein, wenn ein Ersetzen nicht möglich ist)
		 */
		private SearchMatch(final ModelElement station, final String description, final String fullText, final int beginIndex, final int endIndex, final Consumer<String> setNewText, final IntConsumer setNewInteger, final LongConsumer setNewLong, final DoubleConsumer setNewDouble) {
			this.station=station;
			this.description=description;
			this.fullText=fullText;
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
			this.setNewText=setNewText;
			this.setNewInteger=setNewInteger;
			this.setNewLong=setNewLong;
			this.setNewDouble=setNewDouble;
		}

		/**
		 * Kann der Text ersetzt werden?
		 * @return Liefert <code>true</code>, wenn eine Ersetzung möglich ist
		 * @see #replace(String)
		 */
		public boolean canReplace() {
			return (setNewText!=null) || (setNewInteger!=null) || (setNewLong!=null) || (setNewDouble!=null);
		}

		/**
		 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
		 * HTML-Entitäten um.
		 * @param line	Umzuwandelnder Text
		 * @return	Umgewandelter Text
		 */
		private static String encodeHTML(final String line) {
			if (line==null) return "";
			String result;
			result=line.replaceAll("&","&amp;");
			result=result.replaceAll("<","&lt;");
			result=result.replaceAll(">","&gt;");
			return result;
		}

		/**
		 * Verkürzt einen Text, falls dieser zu lang ist.
		 * @param text	Zu bearbeitender Text
		 * @param maxLength	Maximale Länge für den Text (Werte &le;0 für "keine Begrenzung")
		 * @param isPre	Soll der Text am Anfang (<code>true</code>) oder am Ende (<code>false</code>) gekürzt werden
		 * @return	Notwendigenfalls gekürzter Text
		 */
		private static String restrictLength(final String text, final int maxLength, final boolean isPre) {
			if (maxLength<=0 || text.length()<=maxLength) return text;
			if (isPre) {
				return "..."+text.substring(text.length()-maxLength);
			} else {
				return text.substring(0,maxLength)+"...";
			}
		}

		/**
		 * Liefert eine html-Fassung (ohne Präambel und Abschluss) in dem der Treffer
		 * innerhalb des gesamten Textes fett markiert ist.
		 * @param maxPrePostLength	Maximale Länge der Abschnitte vor und nach dem Treffer (&le;0 für unbegrenzt)
		 * @return	Vollständiger Text mit markiertem Treffer
		 * @see #fullText
		 */
		public String getFullTextHTML(final int maxPrePostLength) {
			final int index1=Math.min(fullText.length(),beginIndex);
			final int index2=Math.min(fullText.length(),endIndex);
			final StringBuilder html=new StringBuilder();
			html.append(encodeHTML(restrictLength(fullText.substring(0,index1),maxPrePostLength,true)));
			if (fullText.length()>index1) {
				html.append("<b>");
				html.append(encodeHTML(fullText.substring(index1,index2)));
				html.append("</b>");
			}
			if (fullText.length()>index2) html.append(encodeHTML(restrictLength(fullText.substring(index2),maxPrePostLength,false)));
			return html.toString();
		}

		/**
		 * Ersetzt einen Anteil in dem Gesamttext.
		 * @param newPart	Neuer Textanteil der anstatt des Suchbegriffs in den Gesamttext eingefügt werden soll
		 * @return	Neuer Gesamttext
		 */
		private String getFullReplaceText(final String newPart) {
			if (fullText==null || fullText.isEmpty()) return newPart;
			final StringBuilder newText=new StringBuilder();
			newText.append(fullText.substring(0,Math.min(fullText.length(),beginIndex)));
			if (newPart!=null) newText.append(newPart);
			if (fullText.length()>endIndex) newText.append(fullText.substring(endIndex));
			return newText.toString();
		}

		/**
		 * Ersetzt den Suchtreffer innerhalb des Textes durch einen neuen Anteil
		 * @param newPart	Neuer Anteil, der den Suchtreffer innerhalb des Textes ersetzen soll
		 * @return	Liefert <code>true</code>, wenn die Ersetzung ausgeführt werden konnte
		 * @see #canReplace()
		 */
		public boolean replace(final String newPart) {
			if (setNewText!=null) {
				final String newText=getFullReplaceText(newPart);
				setNewText.accept(newText);
				return true;
			}

			if (setNewInteger!=null) {
				final Integer I=NumberTools.getInteger(newPart);
				if (I==null) return false;
				setNewInteger.accept(I);
				return true;
			}

			if (setNewLong!=null) {
				final Long L=NumberTools.getLong(newPart);
				if (L==null) return false;
				setNewLong.accept(L);
				return true;
			}

			if (setNewDouble!=null) {
				final Double D=NumberTools.getDouble(newPart);
				if (D==null) return false;
				setNewDouble.accept(D);
				return true;
			}

			return false;
		}


		/**
		 * Liefert eine HTML-Beschreibung der Station
		 * @param model	Gesamtes Modell
		 * @return	HTML-Beschreibung der Station oder <code>null</code>, wenn sich der Suchtreffer nicht auf eine Station bezieht
		 */
		private String getStationName(final EditModel model) {
			if (station==null) return null;

			/* Übergeordnetes Element finden */
			ModelElementSub parent=null;
			for (ModelElement el: model.surface.getElements()) {
				if (el==station) break;
				if (el instanceof ModelElementSub) {
					for (ModelElement sub: ((ModelElementSub)el).getSubSurface().getElements()) {
						if (sub==station) {parent=(ModelElementSub)el; break;}
					}
					if (parent!=null) break;
				}
			}

			/* Text aufbauen */
			return ElementRendererTools.getElementHTMLInfo(station,parent);
		}

		/**
		 * Liefert eine HTML-Beschreibung des Suchtreffers.
		 * @param model	Gesamtes Modell
		 * @return	HTML-Beschreibung des Suchtreffers
		 */
		public String getHTML(final EditModel model) {
			final StringBuilder info=new StringBuilder();
			info.append(ElementRendererTools.htmlHead);
			info.append("\n");
			final String stationName=getStationName(model);
			if (stationName!=null) {
				info.append(stationName);
				info.append("<br>\n");
			}
			info.append("<span color=\"#00C000\">\n");
			info.append(Language.tr("FindAndReplace.Result.DescriptionOfTheLocation"));
			info.append(": ");
			info.append(description);
			info.append("<br>\n");
			info.append(Language.tr("FindAndReplace.Result.Content"));
			info.append(": ");
			info.append(getFullTextHTML(50));
			info.append("\n</span>");
			info.append(ElementRendererTools.htmlFoot);
			return info.toString();
		}

		@Override
		public String toString() {
			if (station==null) {
				return String.format("[Match: description=\"%s\", begin(inclusive)=%d, end(exclusive)=%d, text=\"%s\"]",description,beginIndex,endIndex,fullText);
			} else {
				return String.format("[Match: stationID=%d, description=\"%s\", begin(inclusive)=%d, end(exclusive)=%d, text=\"%s\"]",station.getId(),description,beginIndex,endIndex,fullText);
			}
		}
	}
}