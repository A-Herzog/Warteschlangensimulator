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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;

/**
 * Diese Klasse hält die Informationen für das Editor-Modell vor
 * wie die Kunden bestimmter Typen in den Diagrammen in Bezug auf
 * die Farben dargestellt werden sollen und welche Icons für die
 * Kunden in der Animation verwendet werden sollen.
 * @author Alexander Herzog
 * @see EditModel
 */
public final class ModelClientData implements Cloneable {
	/** Vorgabefarben für die Kundentypen */
	private static final Color[] DEFAULT_COLORS=new Color[]{Color.RED,Color.BLUE,Color.GREEN,Color.BLACK};
	/** Standards-Icon für eine Benutzergruppe */
	private static final String DEFAULT_ICON="user";
	/** Standardkosten pro Wartesekunde für eine Benutzergruppe */
	private static final double DEFAULT_COSTS_WAITING=0.0;
	/** Standardkosten pro Transfersekunde für eine Benutzergruppe */
	private static final double DEFAULT_COSTS_TRANSFER=0.0;
	/** Standardkosten pro Bediensekunde für eine Benutzergruppe */
	private static final double DEFAULT_COSTS_PROCESS=0.0;

	/**
	 * Name des XML-Elements, das die Ressourcen-Elemente enthält
	 */
	public static String[] XML_NODE_NAME=new String[]{"KundentypenDaten"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	/** Zuordnung von Kundentypen zu Farben */
	private Map<String,Color> colorMap;
	/** Zuordnung von Kundentypen zu Icons */
	private Map<String,String> iconMap;

	/** Zuordnung von Kundentypen zu Wartezeitkosten */
	private Map<String,Double> costsWaitingMap;
	/** Zuordnung von Kundentypen zu Transferzeitkosten */
	private Map<String,Double> costsTransferMap;
	/** Zuordnung von Kundentypen zu Bedienzeitkosten */
	private Map<String,Double> costsProcessMap;

	/**
	 * Konstruktor der Klasse <code>ModelClientData</code>
	 */
	public ModelClientData() {
		colorMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		iconMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		costsWaitingMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		costsTransferMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		costsProcessMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * Liefert die Farbe für die Kunden eines bestimmten Typs
	 * @param name	Name des Kundentyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @return	Farbe oder <code>null</code> wenn keine Farbe gesetzt ist
	 */
	public Color getColor(final String name) {
		return colorMap.get(name);
	}

	/**
	 * Liefert das Icon für die Kunden eines bestimmten Typs
	 * @param name	Name des Kundentyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @return	Icon für den Kundentyp
	 */
	public String getIcon(final String name) {
		return iconMap.getOrDefault(name,DEFAULT_ICON);
	}

	/**
	 * Liefert die Wartezeit-, Transferzeit- und Bedienzeitkosten für die Kunden eines bestimmten Typs
	 * @param name	Name des Kundentyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @return 3-elementiges Array aus Wartezeit-, Transferzeit- und Bedienzeitkosten
	 */
	public double[] getCosts(final String name) {
		final double d1=costsWaitingMap.getOrDefault(name,DEFAULT_COSTS_WAITING);
		final double d2=costsTransferMap.getOrDefault(name,DEFAULT_COSTS_TRANSFER);
		final double d3=costsProcessMap.getOrDefault(name,DEFAULT_COSTS_PROCESS);
		return new double[]{d1,d2,d3};
	}

	/**
	 * Stellt ein, in welche Farbe die Kunden eines bestimmten Typs in den Statistik-Diagrammen angezeigt werden sollen
	 * @param name	Name des Kundentyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @param color	Farbe oder <code>null</code>, wenn die Farbe automatisch gewählt werden soll
	 */
	public void setColor(final String name, final Color color) {
		colorMap.put(name,color);
	}

	/**
	 * Stellt ein, welches Icon für die Kunden eines bestimmten Typs in der Animation verwendet werden soll
	 * @param name	Name des Kundentyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @param icon	Icon, welches in der Animation verwendet werden soll
	 */
	public void setIcon(final String name, final String icon) {
		iconMap.put(name,(icon==null)?DEFAULT_ICON:icon);
	}

	/**
	 * Stellt ein, welche Kosten durch Warte-, Transfer- und Bedienzeit für einem Kunden eines bestimmten Typs entstehen
	 * @param name	Name des Kundentyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden)
	 * @param costs	3-elementiges Array aus Wartezeit-, Transferzeit- und Bedienzeitkosten
	 */
	public void setCosts(final String name, final double[] costs) {
		if (costs==null || costs.length!=3) return;

		costsWaitingMap.put(name,costs[0]);
		costsTransferMap.put(name,costs[1]);
		costsProcessMap.put(name,costs[2]);
	}

	/**
	 * Löscht den Datensatz zu einem Kundentyps
	 * @param name	Name des Kundentyps (es wird nicht zwischen Groß- und Kleinschreibung unterschieden), der gelöscht werden soll
	 */
	public void delete(final String name) {
		colorMap.remove(name);
		iconMap.remove(name);
		costsWaitingMap.remove(name);
		costsTransferMap.remove(name);
		costsProcessMap.remove(name);
	}

	/**
	 * Liefert den Namen einer Kundengruppe basierend auf dem Index in der Liste der Farben
	 * @param index	Index des Eintrags
	 * @return	Name der Kundengruppe
	 * @see #sizeColor()
	 */
	public String getNameFromColorList(final int index) {
		int nr=0;
		for (Map.Entry<String,Color> entry: colorMap.entrySet()) {
			if (nr==index) return entry.getKey();
			nr++;
		}
		return null;
	}

	/**
	 * Liefert den Namen einer Kundengruppe basierend auf dem Index in der Liste der Icons
	 * @param index	Index des Eintrags
	 * @return	Name der Kundengruppe
	 * @see #sizeColor()
	 */
	public String getNameFromIconList(final int index) {
		int nr=0;
		for (Map.Entry<String,String> entry: iconMap.entrySet()) {
			if (nr==index) return entry.getKey();
			nr++;
		}
		return null;
	}

	/**
	 * Liefert den Namen einer Kundengruppe basierend auf dem Index in der Liste der Kosten
	 * @param index	Index des Eintrags
	 * @return	Name der Kundengruppe
	 * @see #sizeColor()
	 */
	public String getNameFromCostsList(final int index) {
		int nr=0;
		for (Map.Entry<String,Double> entry: costsWaitingMap.entrySet()) {
			if (nr==index) return entry.getKey();
			nr++;
		}
		return null;
	}

	/**
	 * Ändert den Namen eines Kundentyp-Eintrags in der Farbenliste
	 * @param index	Index des Eintrags in der Liste
	 * @param name	Neuer Name
	 */
	public void changeNameInColorList(final int index, final String name) {
		if (index<0) return;

		final String oldName=getNameFromColorList(index);
		if (oldName==null || oldName.equals(name)) return;
		final Color color=colorMap.get(oldName);
		colorMap.remove(oldName);
		colorMap.put(name,color);
	}

	/**
	 * Ändert den Namen eines Kundentyp-Eintrags in der Iconsliste
	 * @param index	Index des Eintrags in der Liste
	 * @param name	Neuer Name
	 */
	public void changeNameInIconsList(final int index, final String name) {
		if (index<0) return;

		final String oldName=getNameFromIconList(index);
		if (oldName==null || oldName.equals(name)) return;
		final String icon=iconMap.get(oldName);
		iconMap.remove(oldName);
		iconMap.put(name,icon);
	}

	/**
	 * Ändert den Namen eines Kundentyp-Eintrags in der Kostenliste
	 * @param index	Index des Eintrags in der Liste
	 * @param name	Neuer Name
	 */
	public void changeNameInCostsList(final int index, final String name) {
		if (index<0) return;

		final String oldName=getNameFromCostsList(index);
		if (oldName==null || oldName.equals(name)) return;
		final double d1=costsWaitingMap.get(oldName);
		final double d2=costsTransferMap.get(oldName);
		final double d3=costsProcessMap.get(oldName);
		costsWaitingMap.remove(oldName);
		costsTransferMap.remove(oldName);
		costsProcessMap.remove(oldName);
		costsWaitingMap.put(name,d1);
		costsTransferMap.put(name,d2);
		costsProcessMap.put(name,d3);
	}

	/**
	 * Liefert eine Liste aller vorhandenen Kundentypen
	 * @return	Liste aller registrierten Kundentypen
	 */
	public String[] list() {
		final List<String> list=new ArrayList<>(colorMap.keySet());
		for (String s: iconMap.keySet()) if (list.indexOf(s)<0) list.add(s);
		return list.toArray(new String[0]);
	}

	/**
	 * Liefert die Anzahl an vorhandenen Kundentypen in der Farbenliste
	 * @return	Anzahl an Kundentypen
	 */
	public int sizeColor() {
		return colorMap.size();
	}

	/**
	 * Liefert die Anzahl an vorhandenen Kundentypen in der Iconliste
	 * @return	Anzahl an Kundentypen
	 */
	public int sizeIcon() {
		return iconMap.size();
	}

	/**
	 * Liefert die Anzahl an vorhandenen Kundentypen in der Kostenliste
	 * @return	Anzahl an Kundentypen
	 */
	public int sizeCosts() {
		return costsWaitingMap.size();
	}

	/**
	 * Löscht alle Kundentypen
	 */
	public void clear() {
		colorMap.clear();
		iconMap.clear();
		costsWaitingMap.clear();
		costsTransferMap.clear();
		costsProcessMap.clear();
	}

	/**
	 * Speichert das Ressourcen-Element in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param parent	Knoten, in dem die Daten des Objekts gespeichert werden sollen
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		for (String name: list()) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.ClientData"));
			node.appendChild(sub);

			/* Name */
			sub.setAttribute(Language.trPrimary("Surface.XML.ClientData.Name"),name);

			/* Farbe */
			final Color color=colorMap.get(name);
			if (color!=null) sub.setAttribute(Language.trPrimary("Surface.XML.ClientData.Color"),EditModel.saveColor(color));

			/* Icon */
			final String icon=iconMap.getOrDefault(name,DEFAULT_ICON);
			if (!icon.equals(DEFAULT_ICON)) sub.setAttribute(Language.trPrimary("Surface.XML.ClientData.Icon"),icon);

			/* Kosten */
			final double[] costs=getCosts(name);
			if (costs[0]!=0 || costs[1]!=0 || costs[2]!=0) sub.setAttribute(Language.trPrimary("Surface.XML.ClientData.Costs"),NumberTools.formatSystemNumber(costs[0])+";"+NumberTools.formatSystemNumber(costs[1])+";"+NumberTools.formatSystemNumber(costs[2]));
		}
	}

	/**
	 * Versucht die Daten des Ressourcen-Elements aus einem xml-Element zu laden
	 * @param node	XML-Element, das das Ressourcen-Objekt beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			final Element e=(Element)l.item(i);
			if (Language.trAll("Surface.XML.ClientData",e.getNodeName())) {
				/* Name */
				final String name=Language.trAllAttribute("Surface.XML.ClientData.Name",e);
				if (name.trim().isEmpty()) continue;

				/* Farbe */
				String color=Language.trAllAttribute("Surface.XML.ClientData.Color",e);
				if (color==null || color.isEmpty()) color=e.getTextContent();
				if (!color.trim().isEmpty()) {
					final Color c=EditModel.loadColor(color);
					if (c==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.ClientData.Color"),e.getNodeName(),e.getParentNode().getNodeName());
					colorMap.put(name,c);
				}

				/* Icon */
				final String icon=Language.trAllAttribute("Surface.XML.ClientData.Icon",e);
				if (!icon.isEmpty()) iconMap.put(name,icon);

				/* Kosten */
				final String costs=Language.trAllAttribute("Surface.XML.ClientData.Costs",e);
				if (costs!=null && !costs.isEmpty()) {
					final String[] partialCosts=costs.split(";");
					if (partialCosts==null || partialCosts.length!=3) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.ClientData.Costs"),e.getNodeName(),e.getParentNode().getNodeName());
					final double[] doubleCosts=new double[3];
					for (int j=0;j<3;j++) {
						final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(partialCosts[j]));
						if (D==null) return String.format(Language.tr("Surface.XML.NrAttributeSubError"),Language.trPrimary("Surface.XML.ClientData.Costs"),j+1,e.getNodeName(),e.getParentNode().getNodeName());
						doubleCosts[j]=D;
					}
					setCosts(name,doubleCosts);
				}
			}
		}

		return null;
	}

	/**
	 * Erstellt eine Kopie des <code>ModelClientColors</code>-Objekts
	 */
	@Override
	public ModelClientData clone() {
		ModelClientData clone=new ModelClientData();

		for (Map.Entry<String,Color> entry : colorMap.entrySet()) clone.setColor(entry.getKey(),entry.getValue());
		for (Map.Entry<String,String> entry : iconMap.entrySet()) clone.setIcon(entry.getKey(),entry.getValue());

		for (Map.Entry<String,Double> entry : costsWaitingMap.entrySet()) {
			final double[] costs=new double[3];
			costs[0]=entry.getValue();
			costs[1]=costsTransferMap.getOrDefault(entry.getKey(),DEFAULT_COSTS_TRANSFER);
			costs[2]=costsProcessMap.getOrDefault(entry.getKey(),DEFAULT_COSTS_PROCESS);
			clone.setCosts(entry.getKey(),costs);
		}

		return clone;
	}

	/**
	 * Löscht alle Einträge in der Kundenfarben-Liste und übernimmt die Daten aus einer anderen Liste
	 * @param otherClientColors	Kundenfarben-Liste aus der die Daten in diese Liste übernommen werden sollen.
	 */
	public void setDataFrom(final ModelClientData otherClientColors) {
		clear();
		for (Map.Entry<String,Color> entry : otherClientColors.colorMap.entrySet()) colorMap.put(entry.getKey(),entry.getValue());
		for (Map.Entry<String,String> entry : otherClientColors.iconMap.entrySet()) iconMap.put(entry.getKey(),entry.getValue());
		for (Map.Entry<String,Double> entry : otherClientColors.costsWaitingMap.entrySet()) costsWaitingMap.put(entry.getKey(),entry.getValue());
		for (Map.Entry<String,Double> entry : otherClientColors.costsTransferMap.entrySet()) costsTransferMap.put(entry.getKey(),entry.getValue());
		for (Map.Entry<String,Double> entry : otherClientColors.costsProcessMap.entrySet()) costsProcessMap.put(entry.getKey(),entry.getValue());
	}

	/**
	 * Prüft, ob zwei <code>ModelClientColors</code>-Objekte inhaltlich identisch sind
	 * @param otherClientColors	Das zweite <code>ModelClientColors</code>-Objekt, welches mit diesem Objekt verglichen werden soll
	 * @return	Gibt <code>true</code> zurück, wenn beide Objekte die gleichen Kundentypenfarben enthalten
	 */
	public boolean equalsModelClientData(final ModelClientData otherClientColors) {
		for (String name : list()) {
			final Color c1=getColor(name);
			final Color c2=otherClientColors.getColor(name);
			if (!(c1==null && c2==null)) {
				if (c1==null || c2==null) return false;
				if (!c1.equals(c2)) return false;
			}
			final String i1=getIcon(name);
			final String i2=otherClientColors.getIcon(name);
			if (!(i1==null && i2==null)) {
				if (i1==null || i2==null) return false;
				if (!i1.equals(i2)) return false;
			}
			final double[] d1=getCosts(name);
			final double[] d2=otherClientColors.getCosts(name);
			if (d1.length!=3 || d2.length!=3) return false;
			for (int i=0;i<d1.length;i++) if (d1[i]!=d2[i]) return false;
		}
		for (String name : otherClientColors.list()) {
			final Color c1=getColor(name);
			final Color c2=otherClientColors.getColor(name);
			if (!(c1==null && c2==null)) {
				if (c1==null || c2==null) return false;
				if (!c1.equals(c2)) return false;
			}
			final String i1=getIcon(name);
			final String i2=otherClientColors.getIcon(name);
			if (!(i1==null && i2==null)) {
				if (i1==null || i2==null) return false;
				if (!i1.equals(i2)) return false;
			}
			final double[] d1=getCosts(name);
			final double[] d2=otherClientColors.getCosts(name);
			if (d1.length!=3 || d2.length!=3) return false;
			for (int i=0;i<d1.length;i++) if (d1[i]!=d2[i]) return false;
		}

		return true;
	}

	/**
	 * Liefert eine vollständige Map von Farben für alle Kundentypen und verwendet dabei Vorgabefarben für alle nicht definierten Kundentypen
	 * @param clientTypes	Liste mit allen Kundentypen, zu denen Farben in der Map geliefert werden sollen
	 * @return	Map mit Zuordnung von allen Kundentypen zu Farben
	 */
	public Map<String,Color> getStatisticColors(List<String> clientTypes) {
		Map<String,Color> fullMap=new HashMap<>();

		int nextColor=0;
		for (String clientType: clientTypes) {
			final Color color=colorMap.get(clientType);
			if (color!=null) {fullMap.put(clientType,color); continue;}
			fullMap.put(clientType,DEFAULT_COLORS[nextColor%DEFAULT_COLORS.length]);
			nextColor++;
		}

		return fullMap;
	}

	/**
	 * Fügt ggf. Daten für den neuen Kundentyp zu den Listen hinzu
	 * in dem die Daten von dem alten Kundentyp kopiert werden.<br>
	 * Existieren berets Daten zu dem neuen Namen, so erfolgt keine Übertragung.
	 * @param oldName	Alter Name des Kundentyps
	 * @param newName	Neuer Name des Kundentyps
	 */
	public void copyDataIfNotExistent(final String oldName, final String newName) {
		/* Wurde der Name überhaupt geändert? */
		if (oldName==null || oldName.isEmpty() || newName==null || newName.isEmpty() || oldName.equals(newName)) return;

		/* Keine Kopie anlegen, wenn es bereits Daten für den neuen Namen gibt. */
		if (colorMap.get(newName)!=null) return;
		if (iconMap.get(newName)!=null) return;
		if (costsWaitingMap.get(newName)!=null) return;
		if (costsTransferMap.get(newName)!=null) return;
		if (costsProcessMap.get(newName)!=null) return;

		/* Daten übertragen */

		final Color color=colorMap.get(oldName);
		if (color!=null) colorMap.put(newName,color);

		final String icon=iconMap.get(oldName);
		if (icon!=null) iconMap.put(newName,icon);

		Double costs;

		costs=costsWaitingMap.get(oldName);
		if (costs!=null) costsWaitingMap.put(newName,costs);

		costs=costsTransferMap.get(oldName);
		if (costs!=null) costsTransferMap.put(newName,costs);

		costs=costsProcessMap.get(oldName);
		if (costs!=null) costsProcessMap.put(newName,costs);
	}
}