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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.distribution.tools.DistributionTools;

/**
 * Diese Klasse kapselt die Rüstzeiten für ein <code>ModelElementProcess</code>-Objekt.
 * Es wird ermöglicht, die Zeiten über eine Verteilung oder über einen Ausdruck zu definieren.
 * @author Alexander Herzog
 * @see ModelElementProcess
 */
public final class DistributionSystemSetupTimes implements Cloneable {
	private final Map<String,Map<String,Object>> data;

	/**
	 * Konstruktor der Klasse
	 */
	public DistributionSystemSetupTimes() {
		data=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * Übernimmt alle Daten von einem anderen <code>DistributionSystemSetupTimes</code>-Objekt.
	 * @param source	Anderes <code>DistributionSystemSetupTimes</code>-Objhekt, von dem die Daten übernommen werden sollen
	 */
	public void setData(final DistributionSystemSetupTimes source) {
		if (source==null) return;

		data.clear();

		for (Map.Entry<String,Map<String,Object>> entry1: source.data.entrySet()) {
			final Map<String,Object> part=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (Map.Entry<String,Object> entry2: entry1.getValue().entrySet()) {
				final Object value=entry2.getValue();
				if (value instanceof String) part.put(entry2.getKey(),value);
				if (value instanceof AbstractRealDistribution) part.put(entry2.getKey(),DistributionTools.cloneDistribution((AbstractRealDistribution)value));
			}
			if (part.size()>0) data.put(entry1.getKey(),part);
		}
	}

	/**
	 * Prüft, ob bereits Daten für einen Kundentyp mit dem angegebenen Namen vorhanden sind.
	 * @param subType	Kundentyp, bei dem geprüft werden soll, ob bereits Daten vorhanden sind.
	 * @return	Gibt <code>true</code> zurück, wenn Daten für den angegebenen Kundentyp vorliegen.
	 */
	public boolean nameInUse(final String subType) {
		for (Map.Entry<String,Map<String,Object>> entry1: data.entrySet()) {
			if (entry1.getKey().equalsIgnoreCase(subType)) return true;
			for (Map.Entry<String,Object> entry2: entry1.getValue().entrySet()) {
				if (entry2.getKey().equalsIgnoreCase(subType)) return true;
			}
		}
		return false;
	}

	/**
	 * Liefert eine Liste aller Kundentypen zu denen Daten vorhanden sind
	 * @return	Liste aller Kundentypen zu denen Daten vorhanden sind
	 */
	public String[] getNames() {
		final List<String> names=new ArrayList<>();

		String s;
		for (Map.Entry<String,Map<String,Object>> entry1: data.entrySet()) {
			s=entry1.getKey();
			if (names.indexOf(s)<0) names.add(s);
			for (Map.Entry<String,Object> entry2: entry1.getValue().entrySet()) {
				s=entry2.getKey();
				if (names.indexOf(s)<0) names.add(s);
			}
		}

		return names.toArray(new String[0]);
	}

	/**
	 * Ändert den Namen eines Kundentyps in den Daten
	 * @param oldName	Alter Name
	 * @param newName	Neuer Name
	 */
	public void renameSubType(final String oldName, final String newName) {
		final Map<String,Object> part=data.get(oldName);
		if (part!=null) {
			data.put(newName,part);
			data.remove(oldName);
		}

		for (Map.Entry<String,Map<String,Object>> entry: data.entrySet()) {
			final Object value=entry.getValue().get(oldName);
			if (value!=null) {
				entry.getValue().put(newName,value);
				entry.getValue().remove(oldName);
			}
		}
	}

	private boolean compare(final Map<String,Map<String,Object>> data1, final Map<String,Map<String,Object>> data2) {
		for (Map.Entry<String,Map<String,Object>> entry1 : data1.entrySet()) {
			final Map<String,Object> map1=entry1.getValue();
			final Map<String,Object> map2=data2.get(entry1.getKey());
			if (map2==null) return false;

			for (Map.Entry<String,Object> entry2 : map1.entrySet()) {
				final Object value1=entry2.getValue();
				final Object value2=map2.get(entry2.getKey());
				if (value2==null) return false;

				if (value1 instanceof String) {
					if (!(value2 instanceof String)) return false;
					if (!((String)value1).equalsIgnoreCase((String)value2)) return false;
				}

				if (value1 instanceof AbstractRealDistribution) {
					if (!(value2 instanceof AbstractRealDistribution)) return false;
					if (!DistributionTools.compare((AbstractRealDistribution)value1,(AbstractRealDistribution)value2)) return false;
				}
			}
		}

		return true;
	}

	/**
	 * Prüft, ob die in einem anderen <code>DistributionSystemSetupTimes</code>-Objekt gespeicherten Daten mit den Daten in diesem Objekt übereinstimmen.
	 * @param other	Anderes Objekt, das mit diesem Objekt verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsDistributionSystem(final DistributionSystemSetupTimes other) {
		if (other==null) return false;

		if (!compare(data,other.data)) return false;
		if (!compare(other.data,data)) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Objekts.
	 */
	@Override
	public DistributionSystemSetupTimes clone() {
		final DistributionSystemSetupTimes clone=new DistributionSystemSetupTimes();
		clone.setData(this);

		return clone;
	}

	/**
	 * Gibt an, ob mindestens eine Rüstzeit hinterlegt ist
	 * @return	Gibt <code>true</code> zurück, wenn mindestens eine Rüstzeit vorhanden ist
	 */
	public boolean isActive() {
		for (Map.Entry<String,Map<String,Object>> entry: data.entrySet()) if (entry.getValue().size()>0) return true;
		return false;
	}

	/**
	 * Löscht alle hinterlegten Rüstzeiten, d.h. schaltet für die Station die Rüstzeiten aus.
	 */
	public void clear() {
		data.clear();
	}

	/**
	 * Liefert die Verteilung oder den Ausdruck für type1-&gt;type2
	 * @param type1	Ausgangstyp
	 * @param type2	Zieltyp
	 * @return	Ausdruck (<code>String</code>), Verteilung (<code>AbstractRealDistribution</code>) oder <code>null</code>, wenn nicht gesetzt
	 */
	public Object get(final String type1, final String type2) {
		final Map<String,Object> part=data.get(type1);
		if (part==null) return null;
		return part.get(type2);
	}

	/**
	 * Stellt für type1-&gt;type2 eine Verteilung ein
	 * @param type1	Ausgangstyp
	 * @param type2	Zieltyp
	 * @param distribution	Verteilung, die für type1-&gt;type2 gelten soll
	 */
	public void set(final String type1, final String type2, final AbstractRealDistribution distribution) {
		Map<String,Object> part=data.get(type1);
		if (part==null) {
			part=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			data.put(type1,part);
		}

		part.put(type2,DistributionTools.cloneDistribution(distribution));
	}

	/**
	 * Stellt für type1-&gt;type2 einen Ausdruck ein
	 * @param type1	Ausgangstyp
	 * @param type2	Zieltyp
	 * @param expression	Ausdruck, der für type1-&gt;type2 gelten soll
	 */
	public void set(final String type1, final String type2, final String expression) {
		Map<String,Object> part=data.get(type1);
		if (part==null) {
			part=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			data.put(type1,part);
		}

		part.put(type2,expression);
	}

	/**
	 * Löscht die Verteilung oder den Ausdruck für type1-&gt;type2
	 * @param type1	Ausgangstyp
	 * @param type2	Zieltyp
	 */
	public void remove(final String type1, final String type2) {
		final Map<String,Object> part=data.get(type1);
		if (part==null) return;

		part.remove(type2);

		if (part.size()==0) data.remove(type1);
	}

	/**
	 * Speichert die Daten in einer XML-Datei
	 * @param doc	XML-Dokument
	 * @param parent	Übergeordnetes XML-Element
	 */
	public void save(final Document doc, final Element parent) {
		for (Map.Entry<String,Map<String,Object>> entry1: data.entrySet()) {
			final String type1=entry1.getKey();
			for (Map.Entry<String,Object> entry2: entry1.getValue().entrySet()) {
				final String type2=entry2.getKey();
				final Object value=entry2.getValue();
				Element sub=null;
				String xml=null;
				if (value instanceof String) {
					parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DistributionSystemSetupTimes.XML.Expression")));
					xml=(String)value;
				}
				if (value instanceof AbstractRealDistribution) {
					parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DistributionSystemSetupTimes.XML.Distribution")));
					xml=DistributionTools.distributionToString((AbstractRealDistribution)value);
				}
				if (sub!=null) {
					sub.setAttribute(Language.trPrimary("Surface.DistributionSystemSetupTimes.XML.Type1"),type1);
					sub.setAttribute(Language.trPrimary("Surface.DistributionSystemSetupTimes.XML.Type2"),type2);
					sub.setTextContent(xml);
				}
			}
		}
	}

	/**
	 * Lädt Teil-Daten aus dem angegebenen XML-Element.
	 * @param node	XML-Element, aus dem die Daten geladen werden sollen
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String load(final Element node) {
		final String type1=Language.trAllAttribute("Surface.DistributionSystemSetupTimes.XML.Type1",node).trim();
		final String type2=Language.trAllAttribute("Surface.DistributionSystemSetupTimes.XML.Type2",node).trim();

		if (Language.trAll("Surface.DistributionSystemSetupTimes.XML.Distribution",node.getNodeName())) {
			if (type1.isEmpty()) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.tr("Surface.DistributionSystemSetupTimes.XML.Type1"),node.getNodeName(),node.getParentNode().getNodeName());
			if (type2.isEmpty()) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.tr("Surface.DistributionSystemSetupTimes.XML.Type2"),node.getNodeName(),node.getParentNode().getNodeName());
			final AbstractRealDistribution dist=DistributionTools.distributionFromString(node.getTextContent(),3000);
			if (dist==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
			set(type1,type2,dist);
			return null;
		}

		if (Language.trAll("Surface.DistributionSystemSetupTimes.XML.Expression",node.getNodeName())) {
			if (type1.isEmpty()) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.tr("Surface.DistributionSystemSetupTimes.XML.Type1"),node.getNodeName(),node.getParentNode().getNodeName());
			if (type2.isEmpty()) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.tr("Surface.DistributionSystemSetupTimes.XML.Type2"),node.getNodeName(),node.getParentNode().getNodeName());
			set(type1,type2,node.getTextContent());
			return null;
		}

		return null;
	}

	/**
	 * Prüft, ob das Element ein ladbares Element ist
	 * @param node	Zu prüfendes Element
	 * @return	Gibt <code>true</code> zurück, wenn es sich um ein Element mit Rüstzeitdaten handelt
	 */
	public static boolean isSetupTimesNode(final Element node) {
		if (Language.trAll("Surface.DistributionSystemSetupTimes.XML.Distribution",node.getNodeName())) return true;
		if (Language.trAll("Surface.DistributionSystemSetupTimes.XML.Expression",node.getNodeName())) return true;
		return false;
	}

	/**
	 * Liefert ein zweifaches Array mit allen Rüstzeitverteilungen
	 * (mit ggf. <code>null</code>-Einträgen, wenn für die jeweilige Kombination keine Rüstzeit oder keine Rüstzeit als Verteilung hinterlegt ist)
	 * @param clientTypeNames	Liste mit allen Kundentyp-Namen
	 * @return	Zweifaches Array mit allen Rüstzeitverteilungen
	 */
	public AbstractRealDistribution[][] getAllDistributions(final String[] clientTypeNames) {
		final AbstractRealDistribution[][] dists=new AbstractRealDistribution[clientTypeNames.length][];
		for (int i=0;i<clientTypeNames.length;i++) {
			dists[i]=new AbstractRealDistribution[clientTypeNames.length];
			for (int j=0;j<clientTypeNames.length;j++) {
				final Object obj=get(clientTypeNames[i],clientTypeNames[j]);
				if (obj!=null && (obj instanceof AbstractRealDistribution)) dists[i][j]=(AbstractRealDistribution)obj;
			}
		}
		return dists;
	}

	/**
	 * Liefert ein zweifaches Array mit allen Rüstzeitausdrücken
	 * (mit ggf. <code>null</code>-Einträgen, wenn für die jeweilige Kombination keine Rüstzeit oder keine Rüstzeit als Ausdruck hinterlegt ist)
	 * @param clientTypeNames	Liste mit allen Kundentyp-Namen
	 * @return	Zweifaches Array mit allen Rüstzeitausdrücken
	 */
	public String[][] getAllExpressions(final String[] clientTypeNames) {
		final String[][] expressions=new String[clientTypeNames.length][];
		for (int i=0;i<clientTypeNames.length;i++) {
			expressions[i]=new String[clientTypeNames.length];
			for (int j=0;j<clientTypeNames.length;j++) {
				final Object obj=get(clientTypeNames[i],clientTypeNames[j]);
				if (obj!=null && (obj instanceof String)) expressions[i][j]=(String)obj;
			}
		}
		return expressions;
	}
}