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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.FullTextSearch;

/**
 * Hält alle Datensätze für die Erfassung von Laufzeitstatistik-Daten vor
 * @author Alexander Herzog
 */
public final class ModelLongRunStatistics implements Cloneable {
	/**
	 * Name des XML-Elements, das die Daten zu den zusätzlich aufzuzeichnenden Statistikinformationen enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"ModellZusatzStatistik"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Name des XML-Attributes, in dem die zu verwendende Schrittweite (in Sekunden) gespeichert werden soll
	 */
	public static String[] XML_NODE_STEPWIDE_ATTR=new String[] {"Schrittweite"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Vorgabewert für die Erfassungsschrittweite in Sekunden
	 * @see #stepWideSec
	 */
	private static final long default_stepWideSec=10*3600;

	/**
	 * Liste der zu erfassenden Daten
	 * @see #getData()
	 */
	private final List<ModelLongRunStatisticsElement> data;

	/**
	 * Erfassungsschrittweite in Sekunden
	 * @see #getStepWideSec()
	 * @see #setStepWideSec(long)
	 */
	private long stepWideSec;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelLongRunStatistics() {
		data=new ArrayList<>();
		stepWideSec=default_stepWideSec;
	}

	/**
	 * Setzt alle Einstellungen zurück
	 */
	public void clear() {
		data.clear();
		stepWideSec=default_stepWideSec;
	}

	/**
	 * Erstellt eine Kopie der Statistikdatenliste
	 * @return	Kopie der Statistikdatenliste
	 */
	@Override
	public ModelLongRunStatistics clone() {
		final ModelLongRunStatistics clone=new ModelLongRunStatistics();

		for (ModelLongRunStatisticsElement record: data) clone.data.add(record.clone());
		clone.stepWideSec=stepWideSec;

		return clone;
	}

	/**
	 * Ersetzt die bisherige Liste an Statistikdaten durch eine neue Liste
	 * @param otherAdditionalStatistics	Neue Statistikdaten, die in dieses Statistikdatenliste-Objekt eingefügt werden sollen
	 */
	public void setDataFrom(final ModelLongRunStatistics otherAdditionalStatistics) {
		if (otherAdditionalStatistics==null) return;
		data.clear();
		for (ModelLongRunStatisticsElement record: otherAdditionalStatistics.data) data.add(record.clone());
		stepWideSec=otherAdditionalStatistics.stepWideSec;
	}

	/**
	 * Überprüft, ob die Statistikdatenliste inhaltlich mit einer anderen Statistikdatenliste übereinstimmt
	 * @param otherAdditionalStatistics	Statistikdatenliste, die mit der aktuellen Statistikdatenliste verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Statistikdatenlisten übereinstimmen
	 */

	public boolean equalsAdditionalStatistics(final ModelLongRunStatistics otherAdditionalStatistics) {
		if (otherAdditionalStatistics==null) return false;

		if (data.size()!=otherAdditionalStatistics.data.size()) return false;
		for (int i=0;i<data.size();i++) if (!data.get(i).equalsElement(otherAdditionalStatistics.data.get(i))) return false;
		if (stepWideSec!=otherAdditionalStatistics.stepWideSec) return false;

		return true;
	}

	/**
	 * Liefert die Liste der zu erfassenden Daten
	 * @return	Liste der zu erfassenden Daten
	 */
	public List<ModelLongRunStatisticsElement> getData() {
		return data;
	}

	/**
	 * Gibt an, ob zusätzliche Statistikdaten erfasst werden sollen
	 * @return	Gibt <code>true</code> zurück, wenn zusätzliche Laufzeitdaten erfasst werden sollen
	 */
	public boolean isActive() {
		return data.size()>0;
	}

	/**
	 * Liefert die Erfassungsschrittweite in Sekunden
	 * @return	Erfassungsschrittweite in Sekunden
	 */
	public long getStepWideSec() {
		return stepWideSec;
	}

	/**
	 * Stellt die Erfassungsschrittweite in Sekunden ein
	 * @param stepWideSec	Erfassungsschrittweite in Sekunden
	 */
	public void setStepWideSec(final long stepWideSec) {
		if (stepWideSec>0) this.stepWideSec=stepWideSec;
	}

	/**
	 * Speichert die Statistikdatenliste in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Übergeordneter Konten des Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addToXML(final Document doc, final Element parent) {
		if (data.size()==0 && stepWideSec==default_stepWideSec) return;

		Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);
		node.setAttribute(XML_NODE_STEPWIDE_ATTR[0],""+stepWideSec);

		for (ModelLongRunStatisticsElement element: data) element.addToXML(doc,node);
	}

	/**
	 * Versucht eine Statistikdatenliste aus einem gegebenen xml-Element zu laden
	 * @param node	XML-Element, aus dem die Statistikdatenliste geladen werden soll
	 * @return Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		for (String attr: XML_NODE_STEPWIDE_ATTR) {
			final String value=node.getAttribute(attr).trim();
			if (!value.isEmpty()) {
				final Long L=NumberTools.getPositiveLong(value);
				if (L==null) return String.format(Language.tr("Surface.XML.ErrorAdditionalStatistics.StepWide"),value);
				stepWideSec=L;
				break;
			}
		}

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			for (String test: ModelLongRunStatisticsElement.XML_NODE_NAME) if (e.getNodeName().equals(test)) {
				final ModelLongRunStatisticsElement element=new ModelLongRunStatisticsElement();
				final String error=element.loadFromXML(e);
				if (error!=null) return error+" ("+String.format(Language.tr("Surface.XML.ErrorAdditionalStatistics"),data.size()+1,node.getNodeName())+")";
				data.add(element);
				break;
			}

		}

		return null;
	}

	/**
	 * Sucht einen Text in den Daten der Laufzeitstatistik.
	 * @param searcher	Such-System
	 * @see FullTextSearch
	 */
	public void search(final FullTextSearch searcher) {
		for (ModelLongRunStatisticsElement record: data) record.search(searcher);
	}
}
