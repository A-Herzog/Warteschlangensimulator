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
package ui.modeleditor;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import statistics.StatisticsLongRunPerformanceIndicator;

/**
 * Hält einen einzelnen Datensatz für die Erfassung von Laufzeitstatistik-Daten vor
 * @author Alexander Herzog
 * @see ModelLongRunStatistics
 */
public final class ModelLongRunStatisticsElement implements Cloneable {
	/**
	 * Name des XML-Elements, das die Daten zu den einer zusätzlich aufzuzeichnenden Statistikinformation enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"ModellZusatzStatistik"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Auszuwertender Ausdruck
	 */
	public String expression;

	/**
	 * Modus für die Erfassung
	 * @see StatisticsLongRunPerformanceIndicator.Mode#MODE_MIN
	 * @see StatisticsLongRunPerformanceIndicator.Mode#MODE_AVERAGE
	 * @see StatisticsLongRunPerformanceIndicator.Mode#MODE_MAX
	 */
	public StatisticsLongRunPerformanceIndicator.Mode mode;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelLongRunStatisticsElement() {
		expression="";
		mode=StatisticsLongRunPerformanceIndicator.Mode.MODE_AVERAGE;
	}

	/**
	 * Konstruktor der Klasse
	 * @param expression	Auszuwertender Ausdruck
	 * @param mode	Modus für die Erfassung
	 */
	public ModelLongRunStatisticsElement(final String expression, final StatisticsLongRunPerformanceIndicator.Mode mode) {
		this.expression=expression;
		this.mode=mode;
	}

	@Override
	public ModelLongRunStatisticsElement clone() {
		ModelLongRunStatisticsElement clone=new ModelLongRunStatisticsElement();
		clone.expression=expression;
		clone.mode=mode;
		return clone;
	}

	/**
	 * Vergleicht den Laufzeitstatistik-Datensatz mit einem anderen Laufzeitstatistik-Datensatz
	 * @param otherElement	Laufzeitstatistik-Datensatz, der mit diesem Datensatz verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn beide Datensätze inhaltlich identisch sind
	 */
	public boolean equalsElement(final ModelLongRunStatisticsElement otherElement) {
		if (otherElement==null) return false;

		if (!Objects.equals(expression,otherElement.expression)) return false;
		if (mode!=otherElement.mode) return false;

		return true;
	}

	/**
	 * Speichert die Statistikdaten in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter Konten des Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addToXML(final Document doc, final Element parent) {
		Element node;
		parent.appendChild(node=doc.createElement(XML_NODE_NAME[0]));
		node.setTextContent(expression);
		switch (mode) {
		case MODE_MIN:
			node.setAttribute(Language.trPrimary("Surface.XML.RootName.AdditionalStatistics.Element.Mode"),Language.trPrimary("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Min"));
			break;
		case MODE_AVERAGE:
			node.setAttribute(Language.trPrimary("Surface.XML.RootName.AdditionalStatistics.Element.Mode"),Language.trPrimary("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Average"));
			break;
		case MODE_MAX:
			node.setAttribute(Language.trPrimary("Surface.XML.RootName.AdditionalStatistics.Element.Mode"),Language.trPrimary("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Max"));
			break;
		}
	}

	/**
	 * Versucht ein Statistikdaten-Element aus einem gegebenen xml-Element zu laden
	 * @param node	XML-Element, aus dem das Statistikdaten-Element geladen werden soll
	 * @return Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		expression=node.getTextContent();

		final String attr=Language.trAllAttribute("Surface.XML.RootName.AdditionalStatistics.Element.Mode",node).trim();
		if (!attr.isEmpty()) {
			boolean modeOK=false;
			if (Language.trAll("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Min",attr)) {
				mode=StatisticsLongRunPerformanceIndicator.Mode.MODE_MIN;
				modeOK=true;
			}
			if (Language.trAll("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Average",attr)) {
				mode=StatisticsLongRunPerformanceIndicator.Mode.MODE_AVERAGE;
				modeOK=true;
			}
			if (Language.trAll("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Max",attr)) {
				mode=StatisticsLongRunPerformanceIndicator.Mode.MODE_MAX;
				modeOK=true;
			}
			if (!modeOK) return String.format(Language.tr("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Error"),attr);
		}

		return null;
	}

	/**
	 * Gibt eine Beschreibung des gewählten Modus als Text zurück.
	 * @return	Beschreibung des Modus als Text
	 * @see ModelLongRunStatisticsElement#mode
	 */
	public String getModeInfo() {
		switch (mode) {
		case MODE_MIN:
			return Language.tr("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Min");
		case MODE_AVERAGE:
			return Language.tr("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Average");
		case MODE_MAX:
			return Language.tr("Surface.XML.RootName.AdditionalStatistics.Element.Mode.Max");
		default:
			return "";
		}
	}
}