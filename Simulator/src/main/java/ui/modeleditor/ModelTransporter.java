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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;

/**
 * Daten einer Transportergruppe
 * @author Alexander Herzog
 * @see ModelTransporters
 */
public final class ModelTransporter implements Cloneable {
	/**
	 * Name des XML-Elements, das die Transporter-Daten enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"ModellTransporter"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/**
	 * Standardformel, um aus der Distanz eine Fahrtzeit zu berechnen.<br>
	 * "distance" ist dabei die Variable der Distanzen.
	 */
	public static String DEFAULT_DISTANCE="distance";

	private String name;
	private String iconEastEmpty;
	private String iconWestEmpty;
	private String iconEastLoaded;
	private String iconWestLoaded;
	private final Map<String,Integer> count;
	private int capacity;
	private final Map<String,Map<String,Double>> distances;
	private String expression;

	private final List<ModelTransporterFailure> failures;

	private AbstractRealDistribution loadDistribution;
	private String loadExpression;
	private AbstractRealDistribution unloadDistribution;
	private String unloadExpression;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelTransporter() {
		this("");
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Transporters
	 */
	public ModelTransporter(final String name) {
		this.name=name;
		iconEastEmpty="";
		iconWestEmpty="";
		iconEastLoaded="";
		iconWestLoaded="";
		count=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		capacity=1;
		distances=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		expression=DEFAULT_DISTANCE;
		failures=new ArrayList<>();
		loadDistribution=null;
		loadExpression=null;
		unloadDistribution=null;
		unloadExpression=null;
	}

	/**
	 * Liefert den Namen des Transporters
	 * @return Namen des Transporters
	 */
	public String getName() {
		return name;
	}

	/**
	 * Stellt den Namen des Transporters ein
	 * @param name	Namen des Transporters
	 */
	public void setName(final String name) {
		if (name!=null) this.name=name;
	}

	/**
	 * Liefert das Icon für die Animation des Transporters bei unbeladenen Fahrten nach rechts
	 * @return Icon für die Animation des Transporters bei unbeladenen Fahrten nach rechts
	 */
	public String getEastEmptyIcon() {
		return iconEastEmpty;
	}

	/**
	 * Stellt das Icon für die Animation des Transporters bei unbeladenen Fahrten nach rechts ein
	 * @param icon	Icon für die Animation des Transporters bei unbeladenen Fahrten nach rechts
	 */
	public void setEastEmptyIcon(final String icon) {
		if (icon!=null) this.iconEastEmpty=icon;
	}

	/**
	 * Liefert das Icon für die Animation des Transporters bei unbeladenen Fahrten nach links
	 * @return Icon für die Animation des Transporters bei unbeladenen Fahrten nach links
	 */
	public String getWestEmptyIcon() {
		return iconWestEmpty;
	}

	/**
	 * Stellt das Icon für die Animation des Transporters bei unbeladenen Fahrten nach links ein
	 * @param icon	Icon für die Animation des Transporters bei unbeladenen Fahrten nach links
	 */
	public void setWestEmptyIcon(final String icon) {
		if (icon!=null) this.iconWestEmpty=icon;
	}

	/**
	 * Liefert das Icon für die Animation des Transporters bei beladenen Fahrten nach rechts
	 * @return Icon für die Animation des Transporters bei beladenen Fahrten nach rechts
	 */
	public String getEastLoadedIcon() {
		return iconEastLoaded;
	}

	/**
	 * Stellt das Icon für die Animation des Transporters bei beladenen Fahrten nach rechts ein
	 * @param icon	Icon für die Animation des Transporters bei beladenen Fahrten nach rechts
	 */
	public void setEastLoadedIcon(final String icon) {
		if (icon!=null) this.iconEastLoaded=icon;
	}

	/**
	 * Liefert das Icon für die Animation des Transporters bei beladenen Fahrten nach links
	 * @return Icon für die Animation des Transporters bei beladenen Fahrten nach links
	 */
	public String getWestLoadedIcon() {
		return iconWestLoaded;
	}

	/**
	 * Stellt das Icon für die Animation des Transporters bei beladenen Fahrten nach links ein
	 * @param icon	Icon für die Animation des Transporters bei beladenen Fahrten nach links
	 */
	public void setWestLoadedIcon(final String icon) {
		if (icon!=null) this.iconWestLoaded=icon;
	}

	/**
	 * Liefert die Anzahl an Transportern des Typs pro Station
	 * @return	Anzahl an Transportern des Typs pro Station
	 * @see ModelTransporter#getCountAll()
	 */
	public Map<String,Integer> getCount() {
		return count;
	}

	/**
	 * Liefert die Gesamtzahl an Transportern an allen Stationen
	 * @return	Gesamtzahl an Transportern
	 * @see ModelTransporter#getCount()
	 */
	public int getCountAll() {
		int count=0;
		for (Map.Entry<String,Integer> entry: this.count.entrySet()) count+=entry.getValue().intValue();
		return count;
	}

	/**
	 * Liefert die Kapazität des eines Transporters dieses Typs
	 * @return	Kapazität des eines Transporters dieses Typs
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Stellt die Kapazität des eines Transporters dieses Typs ein.
	 * @param capacity	Kapazität des eines Transporters dieses Typs
	 */
	public void setCapacity(final int capacity) {
		if (capacity>0) this.capacity=capacity;
	}

	/**
	 * Liefert den Ausdruck zur Umrechnung der Entfernungen zu Transferzeiten
	 * @return	Ausdruck zur Umrechnung der Entfernungen zu Transferzeiten
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Stellt den Ausdruck zur Umrechnung der Entfernungen zu Transferzeiten ein.
	 * @param expression	Ausdruck zur Umrechnung der Entfernungen zu Transferzeiten
	 */
	public void setExpression(final String expression) {
		if (expression!=null) this.expression=expression;
	}

	/**
	 * Liefert die Entfernungsmatrix zwischen den Stationen
	 * @return	Entfernungsmatrix zwischen den Stationen
	 * @see ModelTransporter#getDistance(String, String)
	 * @see ModelTransporter#setDistance(String, String, double)
	 */
	public Map<String, Map<String, Double>> getDistances() {
		return distances;
	}

	/**
	 * Liefert eine einzelne Entfernung aus der Entfernungsmatrix.
	 * Nicht vorhandene Werte werden dabei automatisch als 0 interpretiert.
	 * @param stationA	Name der Startstation
	 * @param stationB	Name der Zielstation
	 * @return	Entfernung zwischen den Stationen
	 * @see ModelTransporter#getDistances()
	 */
	public double getDistance(final String stationA, final String stationB) {
		final Map<String,Double> map=distances.get(stationA);
		if (map==null) return 0;
		final Double value=map.get(stationB);
		if (value==null) return 0;
		return value.doubleValue();
	}

	/**
	 * Stellt eine einzelne Entfernung in der Entfernungsmatrix ein.
	 * @param stationA	Name der Startstation
	 * @param stationB	Name der Zielstation
	 * @param distance	Entfernung zwischen den Stationen
	 * @see ModelTransporter#getDistances()
	 */
	public void setDistance(final String stationA, final String stationB, final double distance) {
		Map<String,Double> map=distances.get(stationA);
		if (map==null) {
			map=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			distances.put(stationA,map);
		}

		map.put(stationB,distance);
	}

	/**
	 * Liefert die Liste der Ausfall-Ereignisse
	 * @return	Liste der Ausfall-Ereignisse
	 */
	public List<ModelTransporterFailure> getFailures() {
		return failures;
	}

	/**
	 * Liefert die eingestellte Beladungszeit
	 * @return	Beladungszeit (Verteilung vom Typ <code>AbstractRealDistribution</code>, Zeichenkette für Ausdruck oder <code>null</code>)
	 */
	public Object getLoadTime() {
		if (loadDistribution!=null) return loadDistribution;
		return loadExpression;
	}

	/**
	 * Liefert die eingestellte Entladungszeit
	 * @return	Entladungszeit (Verteilung vom Typ <code>AbstractRealDistribution</code>, Zeichenkette für Ausdruck oder <code>null</code>)
	 */
	public Object getUnloadTime() {
		if (unloadDistribution!=null) return unloadDistribution;
		return unloadExpression;
	}

	/**
	 * Stellt die Beladungszeit ein
	 * @param data	Beladungszeit (Verteilung vom Typ <code>AbstractRealDistribution</code>, Zeichenkette für Ausdruck oder <code>null</code>)
	 */
	public void setLoadTime(final Object data) {
		if (data instanceof AbstractRealDistribution) {
			loadDistribution=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
			loadExpression=null;
			return;
		}

		if (data instanceof String) {
			loadDistribution=null;
			loadExpression=(String)data;
			return;
		}

		loadDistribution=null;
		loadExpression=null;
	}

	/**
	 * Stellt die Entladungszeit ein
	 * @param data	Entladungszeit (Verteilung vom Typ <code>AbstractRealDistribution</code>, Zeichenkette für Ausdruck oder <code>null</code>)
	 */
	public void setUnloadTime(final Object data) {
		if (data instanceof AbstractRealDistribution) {
			unloadDistribution=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
			unloadExpression=null;
			return;
		}

		if (data instanceof String) {
			unloadDistribution=null;
			unloadExpression=(String)data;
			return;
		}

		unloadDistribution=null;
		unloadExpression=null;
	}

	/**
	 * Setzt alle Daten des Transporters zurück
	 */
	public void clear() {
		name="";
		iconEastEmpty="";
		iconWestEmpty="";
		iconEastLoaded="";
		iconWestLoaded="";
		count.clear();
		capacity=1;
		distances.clear();
		expression=DEFAULT_DISTANCE;
		failures.clear();
		loadDistribution=null;
		loadExpression=null;
		unloadDistribution=null;
		unloadExpression=null;
	}

	/**
	 * Vergleicht zwei Transporterobjekte
	 * @param otherTransporter	Anderer Transporter für den Vergleich
	 * @return	Liefert <code>true</code>, wenn die beiden Transporterobjekte inhaltlich identisch sind
	 */
	public boolean equalsModelTransporter(final ModelTransporter otherTransporter) {
		if (otherTransporter==null) return false;

		if (!name.equals(otherTransporter.name)) return false;
		if (!iconEastEmpty.equals(otherTransporter.iconEastEmpty)) return false;
		if (!iconWestEmpty.equals(otherTransporter.iconWestEmpty)) return false;
		if (!iconEastLoaded.equals(otherTransporter.iconEastLoaded)) return false;
		if (!iconWestLoaded.equals(otherTransporter.iconWestLoaded)) return false;
		if (!Objects.deepEquals(count,otherTransporter.count)) return false;
		if (capacity!=otherTransporter.capacity) return false;
		if (!Objects.deepEquals(distances,otherTransporter.distances)) return false;
		if (!expression.equals(otherTransporter.expression)) return false;

		if (failures.size()!=otherTransporter.failures.size()) return false;
		for (int i=0;i<failures.size();i++) if (!failures.get(i).equalsModelTransporterFailure(otherTransporter.failures.get(i))) return false;

		if (!DistributionTools.compare(loadDistribution,otherTransporter.loadDistribution)) return false;
		if (loadExpression==null) {
			if (otherTransporter.loadExpression!=null) return false;
		} else {
			if (otherTransporter.loadExpression==null) return false;
			if (!loadExpression.equals(otherTransporter.loadExpression)) return false;
		}

		if (!DistributionTools.compare(unloadDistribution,otherTransporter.unloadDistribution)) return false;
		if (unloadExpression==null) {
			if (otherTransporter.unloadExpression!=null) return false;
		} else {
			if (otherTransporter.unloadExpression==null) return false;
			if (!unloadExpression.equals(otherTransporter.unloadExpression)) return false;
		}

		return true;
	}

	/**
	 * Erstellt eine Kopie des Transporters
	 */
	@Override
	public ModelTransporter clone() {
		final ModelTransporter clone=new ModelTransporter();

		clone.name=name;
		clone.iconEastEmpty=iconEastEmpty;
		clone.iconWestEmpty=iconWestEmpty;
		clone.iconEastLoaded=iconEastLoaded;
		clone.iconWestLoaded=iconWestLoaded;
		clone.count.putAll(count);
		clone.capacity=capacity;
		for (Map.Entry<String,Map<String,Double>> entry: distances.entrySet()) {
			final Map<String,Double> map=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			map.putAll(entry.getValue());
			clone.distances.put(entry.getKey(),map);
		}
		clone.expression=expression;

		for (ModelTransporterFailure failure: failures) clone.failures.add(failure.clone());

		clone.loadDistribution=DistributionTools.cloneDistribution(loadDistribution);
		clone.loadExpression=loadExpression;
		clone.unloadDistribution=DistributionTools.cloneDistribution(unloadDistribution);
		clone.unloadExpression=unloadExpression;

		return clone;
	}

	/**
	 * Speichert das Transporter-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		Element sub;

		/* Name und Symbole */

		node.setAttribute(Language.trPrimary("Surface.XML.Transporter.Name"),name);
		if (iconEastEmpty!=null && !iconEastEmpty.isEmpty()) {
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.IconEmpty"),iconEastEmpty);
		}
		if (iconWestEmpty!=null && !iconWestEmpty.isEmpty()) {
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.IconLeftEmpty"),iconWestEmpty);
		}
		if (iconEastLoaded!=null && !iconEastLoaded.isEmpty()) {
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.Icon"),iconEastLoaded);
		}
		if (iconWestLoaded!=null && !iconWestLoaded.isEmpty()) {
			node.setAttribute(Language.trPrimary("Surface.XML.Transporter.IconLeft"),iconWestLoaded);
		}

		/* Anzahl pro Station */

		for (Map.Entry<String,Integer> entry: count.entrySet()) {
			if (entry.getKey().isEmpty() || entry.getValue().intValue()<=0) continue;
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.Count")));
			sub.setAttribute(Language.trPrimary("Surface.XML.Transporter.Count.Station"),entry.getKey());
			sub.setAttribute(Language.trPrimary("Surface.XML.Transporter.Count.Count"),""+entry.getValue().intValue());
		}

		/* Beförderungskapazität */

		if (capacity>0) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.Capacity")));
			sub.setTextContent(""+capacity);
		}

		/* Entfernungen */

		for (Map.Entry<String,Map<String,Double>> entryA: distances.entrySet()) {
			final String stationA=entryA.getKey();
			if (stationA.isEmpty()) continue;
			for (Map.Entry<String,Double> entryB: entryA.getValue().entrySet()) {
				final String stationB=entryB.getKey();
				if (stationB.isEmpty()) continue;
				final double d=entryB.getValue().doubleValue();
				if (d<0) continue;
				node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.Distances")));
				sub.setAttribute(Language.trPrimary("Surface.XML.Transporter.Distances.OriginStation"),stationA);
				sub.setAttribute(Language.trPrimary("Surface.XML.Transporter.Distances.DestinationStation"),stationB);
				sub.setTextContent(NumberTools.formatSystemNumber(d));
			}
		}

		node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.Expression")));
		sub.setTextContent(expression);

		/* Ausfälle */

		for (ModelTransporterFailure failure: failures) failure.addDataToXML(doc,node);

		/* Lade/Entlade Zeiten */

		if (loadDistribution!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.LoadDistribution")));
			sub.setTextContent(DistributionTools.distributionToString(loadDistribution));
		}

		if (loadExpression!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.LoadExpression")));
			sub.setTextContent(loadExpression);
		}

		if (unloadDistribution!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.UnloadDistribution")));
			sub.setTextContent(DistributionTools.distributionToString(unloadDistribution));
		}

		if (unloadExpression!=null) {
			node.appendChild(sub=doc.createElement(Language.trPrimary("Surface.XML.Transporter.UnloadExpression")));
			sub.setTextContent(unloadExpression);
		}
	}

	/**
	 * Versucht die Daten eines Transporter-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Transporter-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		/* Name und Symbole */

		name=Language.trAllAttribute("Surface.XML.Transporter.Name",node);
		iconEastEmpty=Language.trAllAttribute("Surface.XML.Transporter.IconEmpty",node);
		iconWestEmpty=Language.trAllAttribute("Surface.XML.Transporter.IconLeftEmpty",node);
		iconEastLoaded=Language.trAllAttribute("Surface.XML.Transporter.Icon",node);
		iconWestLoaded=Language.trAllAttribute("Surface.XML.Transporter.IconLeft",node);
		if (!iconEastLoaded.isEmpty() && iconEastEmpty.isEmpty()) iconEastEmpty=iconEastLoaded;
		if (!iconWestLoaded.isEmpty() && iconWestEmpty.isEmpty()) iconWestEmpty=iconWestLoaded;

		final NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);

			/* Anzahl pro Station */

			if (Language.trAll("Surface.XML.Transporter.Count",e.getNodeName())) {
				final String station=Language.trAllAttribute("Surface.XML.Transporter.Count.Station",e);
				if (!station.isEmpty()) {
					final String countString=Language.trAllAttribute("Surface.XML.Transporter.Count.Count",e);
					final Integer I=NumberTools.getNotNegativeInteger(countString);
					if (I==null || I.intValue()<1) return String.format(Language.tr("Surface.XML.AttributeError"),Language.trPrimary("Surface.XML.Transporter.Count.Count"),e.getNodeName())+" "+Language.tr("Surface.XML.ErrorInfo.NonNegativeIntegerNeeded");
					count.put(station,I);
				}
				continue;
			}

			/* Beförderungskapazität */

			if (Language.trAll("Surface.XML.Transporter.Capacity",e.getNodeName())) {
				final Integer I=NumberTools.getNotNegativeInteger(e.getTextContent());
				if (I==null || I.intValue()<1) return String.format(Language.tr("Surface.XML.ElementSubError"),e.getNodeName(),node.getNodeName())+" "+Language.tr("Surface.XML.ErrorInfo.NonNegativeIntegerNeeded");
				capacity=I.intValue();
				continue;
			}

			/* Entfernungen */

			if (Language.trAll("Surface.XML.Transporter.Distances",e.getNodeName())) {
				final String stationA=Language.trAllAttribute("Surface.XML.Transporter.Distances.OriginStation",e);
				final String stationB=Language.trAllAttribute("Surface.XML.Transporter.Distances.DestinationStation",e);
				if (!stationA.isEmpty() && !stationB.isEmpty()) {
					final Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(e.getTextContent()));
					if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),e.getNodeName(),node.getNodeName())+" "+Language.tr("Surface.XML.ErrorInfo.PositiveIntegerNeeded");
					setDistance(stationA,stationB,D.doubleValue());
				}
				continue;
			}

			if (Language.trAll("Surface.XML.Transporter.Expression",e.getNodeName())) {
				expression=e.getTextContent();
				continue;
			}

			/* Ausfälle */

			for (String test: ModelTransporterFailure.XML_NODE_NAME) if (e.getNodeName().equalsIgnoreCase(test)) {
				ModelTransporterFailure failure=new ModelTransporterFailure();
				final String error=failure.loadFromXML(e,name);
				if (error!=null) return error;
				failures.add(failure);
				break;
			}

			/* Lade/Entlade Zeiten */

			if (Language.trAll("Surface.XML.Transporter.LoadDistribution",e.getNodeName())) {
				final AbstractRealDistribution dist=DistributionTools.distributionFromString(e.getTextContent(),3600);
				if (dist==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
				loadDistribution=dist;
				loadExpression=null;
				continue;
			}

			if (Language.trAll("Surface.XML.Transporter.LoadExpression",e.getNodeName())) {
				loadDistribution=null;
				loadExpression=e.getTextContent();
				continue;
			}

			if (Language.trAll("Surface.XML.Transporter.UnloadDistribution",e.getNodeName())) {
				final AbstractRealDistribution dist=DistributionTools.distributionFromString(e.getTextContent(),3600);
				if (dist==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
				unloadDistribution=dist;
				unloadExpression=null;
				continue;
			}

			if (Language.trAll("Surface.XML.Transporter.UnloadExpression",e.getNodeName())) {
				unloadDistribution=null;
				unloadExpression=e.getTextContent();
				continue;
			}
		}

		return null;
	}
}