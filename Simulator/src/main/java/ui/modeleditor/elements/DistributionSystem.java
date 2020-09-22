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
import java.util.function.Consumer;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.distribution.tools.DistributionTools;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilder;

/**
 * Diese Klasse kapselt die Daten zu Bedien-, Nachbearbeitungs- und Abbruchzeiten für
 * ein <code>ModelElementProcess</code>-Objekt. Es wird ermöglicht, die Zeiten über eine
 * Verteilung oder über einen Ausdruck zu definieren. Außerdem können optional Daten
 * pro Kundentyp hinterlegt werden.
 * @author Alexander Herzog
 * @see ModelElementProcess
 */
public final class DistributionSystem implements Cloneable {
	private final String subLangKey;
	private final String typeLangKey;
	private final boolean canBeNull;

	private AbstractRealDistribution distribution;
	private String expression;

	private final Map<String,AbstractRealDistribution> distributionByType;
	private final Map<String,String> expressionByType;

	/**
	 * Konstruktor der Klasse<br>
	 * <b>Achtung:</b> Die Strings, die als <code>subLangKey</code> und als <code>typeLangKey</code>
	 * übergeben werden, sollten vorab einmal per <code>Language.tr(...)</code> verwendet werden,
	 * damit diese auch sicher in die Sprachdateien aufgenommen werden. Der Konstruktor von
	 * <code>DistributionSystem</code> selbst ist <b>kein</b> Scan-Schlüsselwort bei der
	 * Zusammenstellung der Sprachdateien.
	 * @param subLangKey	Language-Key für den Bezeichner für die Unterelemente (z.B. für Kundentypen)
	 * @param typeLangKey	Language-Key für den Bezeichnung der Zeiten, der beim Speichern verwendet wird (z.B. Bedienzeiten/Nachbearbeitungszeiten/...)
	 * @param canBeNull	Gibt an, ob auch der Basis-Typ (für alle Kundentypen) <code>null</code> sein darf.
	 */
	public DistributionSystem(final String subLangKey, final String typeLangKey, final boolean canBeNull) {
		this.subLangKey=subLangKey;
		this.typeLangKey=typeLangKey;
		this.canBeNull=canBeNull;

		if (!canBeNull) distribution=new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

		distributionByType=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		expressionByType=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	}

	/**
	 * Gibt an, ob der Basis-Typ (für alle Kundentypen) <code>null</code> sein darf.
	 * @return Gibt <code>true</code> zurück, wenn der Basis-Typ (für alle Kundentypen) <code>null</code> sein darf.
	 */
	public boolean canBeNull() {
		return canBeNull;
	}

	/**
	 * Liefert die Daten für den globalen Fall.
	 * @return	Liefert einen <code>String</code>, eine <code>AbstractRealDistribution</code> oder <code>null</code> zurück.
	 */
	public Object get() {
		if (expression!=null) return expression;
		return distribution;
	}

	/**
	 * Gibt an, ob überhaupt Daten in dem Element hinterlegt sind
	 * @return	Gibt <code>true</code> zurück, wenn Daten in dem Element hinterlegt sind
	 */
	public boolean hasData() {
		if (expression!=null) return true;
		if (distribution!=null) return true;
		for (Map.Entry<String,AbstractRealDistribution> entry: distributionByType.entrySet()) if (entry.getValue()!=null) return true;
		for (Map.Entry<String,String> entry: expressionByType.entrySet()) if (entry.getValue()!=null) return true;
		return false;
	}

	/**
	 * Stellt die Daten für den allgemeinen Fall.
	 * @param data	Einzustellende Daten (kann ein <code>String</code>, eine <code>AbstractRealDistribution</code> oder - wenn dies zulässig ist - <code>null</code> sein).
	 * @return	Gibt <code>true</code> zurück, wenn die Daten übernommen werden konnten.
	 */
	public boolean set(final Object data) {
		if (data==null) {
			if (!canBeNull) return false;
			distribution=null;
			expression=null;
			distributionByType.clear();
			expressionByType.clear();
			return true;
		}

		if (data instanceof String) {
			expression=(String)data;
			return true;
		}

		if (data instanceof AbstractRealDistribution) {
			distribution=DistributionTools.cloneDistribution((AbstractRealDistribution)data);
			expression=null;
			return true;
		}

		return false;
	}

	/**
	 * Liefert die Daten für einen bestimmten Subtyp.
	 * @param subType	Kundentyp o.ä., für den die Daten geliefert werden sollen. Wird <code>null</code> übergeben, so werden die Daten für den globalen Fall geliefert.
	 * @return	Liefert einen <code>String</code>, eine <code>AbstractRealDistribution</code> oder <code>null</code> zurück.
	 */
	public Object get(final String subType) {
		if (subType==null || subType.trim().isEmpty()) return get();
		final String s=expressionByType.get(subType);
		if (s!=null) return s;
		return distributionByType.get(subType);
	}

	/**
	 * Sind Daten zu mindestens einem Subtyp vorhanden?
	 * @return	Liefert <code>true</code>, wenn Daten zu mindestens einem Subtyp vorhanden sind.
	 * @see #get(String)
	 */
	public boolean hasSubTypeData() {
		return (expressionByType.size()>0) || (distributionByType.size()>0);
	}

	/**
	 * Liefert die Daten für einen bestimmten Subtyp oder als Fallback die Daten für den globalen Fall.
	 * @param subType	Kundentyp o.ä., für den die Daten geliefert werden sollen. Wird <code>null</code> übergeben, so werden die Daten für den globalen Fall geliefert.
	 * @return	Liefert einen <code>String</code>, eine <code>AbstractRealDistribution</code> oder <code>null</code> zurück.
	 */
	public Object getOrDefault(final String subType) {
		final Object obj=get(subType);
		if (obj!=null) return obj;
		return get();
	}

	/**
	 * Stellt die Daten für einen bestimmten Subtyp ein.
	 * @param subType	Kundentyp o.ä., für den die Daten eingestellt werden sollen. Wird <code>null</code> übergeben, so werden die Daten für den globalen Fall gesetzt.
	 * @param data	Einzustellende Daten (kann ein <code>String</code>, eine <code>AbstractRealDistribution</code> oder <code>null</code> sein).
	 * @return	Gibt <code>true</code> zurück, wenn die Daten übernommen werden konnten.
	 */
	public boolean set(final String subType, final Object data) {
		if (subType==null) return set(data);

		if (data==null) {
			distributionByType.remove(subType);
			expressionByType.remove(subType);
			return true;
		}

		if (data instanceof String) {
			distributionByType.remove(subType);
			expressionByType.put(subType,(String)data);
			return true;
		}

		if (data instanceof AbstractRealDistribution) {
			distributionByType.put(subType,(AbstractRealDistribution)data);
			expressionByType.remove(subType);
			return true;
		}

		return false;
	}

	/**
	 * Prüft, ob bereits Daten für einen Subtyp mit dem angegebenen Namen vorhanden sind.
	 * @param subType	Kundentyp o.ä., bei dem geprüft werden soll, ob bereits Daten vorhanden sind.
	 * @return	Gibt <code>true</code> zurück, wenn Daten für den angegebenen Subtyp vorliegen.
	 */
	public boolean nameInUse(final String subType) {
		if (distributionByType.get(subType)!=null) return true;
		if (expressionByType.get(subType)!=null) return true;
		return false;
	}

	/**
	 * Liefert eine Liste aller Subtypen zu denen Daten vorhanden sind
	 * @return	Liste aller Subtypen zu denen Daten vorhanden sind
	 */
	public String[] getNames() {
		final List<String> names=new ArrayList<>();
		for (Map.Entry<String,AbstractRealDistribution> entry: distributionByType.entrySet()) if (names.indexOf(entry.getKey())<0) names.add(entry.getKey());
		for (Map.Entry<String,String> entry: expressionByType.entrySet()) if (names.indexOf(entry.getKey())<0) names.add(entry.getKey());
		return names.toArray(new String[0]);
	}

	/**
	 * Ändert den Namen eines Subtyps in den Daten
	 * @param oldName	Alter Name
	 * @param newName	Neuer Name
	 */
	public void renameSubType(final String oldName, final String newName) {
		final Object data=get(oldName);
		set(newName,data);
		set(oldName,null);
	}

	/**
	 * Prüft, ob die in einem anderen <code>DistributionSystem</code>-Objekt gespeicherten Daten mit den Daten in diesem Objekt übereinstimmen.
	 * @param other	Anderes Objekt, das mit diesem Objekt verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsDistributionSystem(final DistributionSystem other) {
		if (other==null) return false;
		if (canBeNull!=other.canBeNull) return false;

		if (expression!=null) {
			if (other.expression==null) return false;
			if (!expression.equals(other.expression)) return false;
		} else {
			if (!DistributionTools.compare(distribution,other.distribution)) return false;
		}

		for (Map.Entry<String,String> entry: expressionByType.entrySet()) if (!entry.getValue().equals(other.expressionByType.get(entry.getKey()))) return false;
		for (Map.Entry<String,String> entry: other.expressionByType.entrySet()) if (!entry.getValue().equals(expressionByType.get(entry.getKey()))) return false;

		for (Map.Entry<String,AbstractRealDistribution> entry: distributionByType.entrySet()) if (!DistributionTools.compare(entry.getValue(),other.distributionByType.get(entry.getKey()))) return false;
		for (Map.Entry<String,AbstractRealDistribution> entry: other.distributionByType.entrySet()) if (!DistributionTools.compare(entry.getValue(),distributionByType.get(entry.getKey()))) return false;

		return true;
	}

	/**
	 * Erstellt eine Kopie des Objekts.
	 */
	@Override
	public DistributionSystem clone() {
		final DistributionSystem clone=new DistributionSystem(subLangKey,typeLangKey,canBeNull);
		clone.setData(this);

		return clone;
	}

	/**
	 * Übernimmt (abgesehen von den Daten, die nur im Konstruktor eingestellt werden können,) alle Daten von einem anderen <code>DistributionSystem</code>-Objekt.
	 * @param source	Anderes <code>DistributionSystem</code>-Objhekt, von dem die Daten übernommen werden sollen
	 */
	public void setData(final DistributionSystem source) {
		if (source==null) return;

		distribution=DistributionTools.cloneDistribution(source.distribution);
		expression=source.expression;

		expressionByType.clear();
		distributionByType.clear();

		for (Map.Entry<String,String> entry: source.expressionByType.entrySet()) expressionByType.put(entry.getKey(),entry.getValue());
		for (Map.Entry<String,AbstractRealDistribution> entry: source.distributionByType.entrySet()) distributionByType.put(entry.getKey(),entry.getValue());
	}

	/**
	 * Speichert die Daten in einer XML-Datei
	 * @param doc	XML-Dokument
	 * @param parent	Übergeordnetes XML-Element
	 * @param additionalData	Wird aufgerufen, um zusätzliche Daten in dem gerade angelegten XML-Unterelement zu hinterlegen (kann <code>null</code> sein)
	 */
	public void save(final Document doc, final Element parent, final Consumer<Element> additionalData) {
		Element sub;

		if (distribution!=null || expression!=null) {
			if (expression!=null) {
				parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DistributionSystem.XML.Expression")));
				sub.setTextContent(expression);
			} else {
				parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DistributionSystem.XML.Distribution")));
				sub.setTextContent(DistributionTools.distributionToString(distribution));
			}
			if (typeLangKey!=null && !typeLangKey.trim().isEmpty()) sub.setAttribute(Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type"),Language.tr(typeLangKey));
			if (additionalData!=null) additionalData.accept(sub);
		}
		if (subLangKey!=null && !subLangKey.trim().isEmpty()) {
			for (Map.Entry<String,AbstractRealDistribution> entry : distributionByType.entrySet()) if (entry.getValue()!=null) {
				if (expressionByType.get(entry.getKey())!=null) continue;
				parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DistributionSystem.XML.Distribution")));
				sub.setAttribute(Language.trPrimary(subLangKey),entry.getKey());
				if (typeLangKey!=null && !typeLangKey.trim().isEmpty()) sub.setAttribute(Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type"),Language.tr(typeLangKey));
				sub.setTextContent(DistributionTools.distributionToString(entry.getValue()));
			}
			for (Map.Entry<String,String> entry : expressionByType.entrySet()) if (entry.getValue()!=null) {
				parent.appendChild(sub=doc.createElement(Language.trPrimary("Surface.DistributionSystem.XML.Expression")));
				sub.setAttribute(Language.trPrimary(subLangKey),entry.getKey());
				if (typeLangKey!=null && !typeLangKey.trim().isEmpty()) sub.setAttribute(Language.trPrimary("Surface.DistributionSystem.XML.Distribution.Type"),Language.tr(typeLangKey));
				sub.setTextContent(entry.getValue());
			}
		}
	}

	/**
	 * Liefert den 0-basierten Index des Untereintrags beim Speichern der Verteilungen.
	 * @param key	Schlüssel für den Untereintrag
	 * @return	0-basierter Index oder -1, wenn für den Schlüssel keine Verteilung hinterlegt ist
	 */
	public int getSubNumber(final String key) {
		int nr=0;
		for (Map.Entry<String,AbstractRealDistribution> entry : distributionByType.entrySet()) if (entry.getValue()!=null) {
			if (entry.getKey().equals(key)) return nr;
			nr++;
		}
		return -1;
	}

	/**
	 * Speichert die Daten in einer XML-Datei
	 * @param doc	XML-Dokument
	 * @param parent	Übergeordnetes XML-Element
	 */
	public void save(final Document doc, final Element parent) {
		save(doc,parent,null);
	}

	/**
	 * Lädt Teil-Daten aus dem angegebenen XML-Element.<br>
	 * Durch den Aufruf von <code>loadDistribution</code> ist bereits festgelegt, dass es sich um eine Verteilung und nicht um einen Ausdruck handelt.
	 * Die Information Global/Kundentyp wird durch die Methode selbst aus dem XML-Element ermittelt.
	 * @param node	XML-Element, aus dem die Daten geladen werden sollen
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String loadDistribution(final Element node) {
		final String type=(subLangKey==null || subLangKey.trim().isEmpty())?"":Language.trAllAttribute(subLangKey,node);
		final AbstractRealDistribution dist=DistributionTools.distributionFromString(node.getTextContent(),3000);

		if (dist==null) return String.format(Language.tr("Surface.XML.ElementSubError"),node.getNodeName(),node.getParentNode().getNodeName());
		if (type==null || type.trim().isEmpty()) {
			distribution=dist;
		} else {
			distributionByType.put(type,dist);
		}

		return null;
	}

	/**
	 * Lädt Teil-Daten aus dem angegebenen XML-Element.<br>
	 * Durch den Aufruf von <code>loadExpression</code> ist bereits festgelegt, dass es sich um einen Ausdruck und nicht um eine Verteilung handelt.
	 * Die Information Global/Kundentyp wird durch die Methode selbst aus dem XML-Element ermittelt.
	 * @param node	XML-Element, aus dem die Daten geladen werden sollen
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String loadExpression(final Element node) {
		final String type=(subLangKey==null || subLangKey.trim().isEmpty())?"":Language.trAllAttribute(subLangKey,node);
		final String expr=node.getTextContent();

		if (type==null || type.trim().isEmpty()) {
			expression=expr;
		} else {
			expressionByType.put(type,expr);
		}
		return null;
	}

	/**
	 * Liefert den Type-Attribute des XML-Elements
	 * @param node	XML-Element für das der Typ ausgelesen werden soll
	 * @return	Typ (der Verteilung/des Ausdrucks)
	 */
	public static String getTypeAttribute(final Element node) {
		return Language.trAllAttribute("Surface.DistributionSystem.XML.Distribution.Type",node);
	}

	/**
	 * Prüft, ob das Element ein ladbares Verteilungs-Element ist
	 * @param node	Zu prüfendes Element
	 * @return	Gibt <code>true</code> zurück, wenn es sich um eine Verteilung handelt
	 */
	public static boolean isDistribution(final Element node) {
		return Language.trAll("Surface.DistributionSystem.XML.Distribution",node.getNodeName());
	}

	/**
	 * Prüft, ob das Element ein ladbares Ausdrucks-Element ist
	 * @param node	Zu prüfendes Element
	 * @return	Gibt <code>true</code> zurück, wenn es sich um einen Ausdruck handelt
	 */
	public static boolean isExpression(final Element node) {
		return Language.trAll("Surface.DistributionSystem.XML.Expression",node.getNodeName());
	}

	/**
	 * Liefert den Wert des Subtyp-Attributes des XML-Element
	 * @param node	XML-Element zu dem das Subtyp-Attribut ausgelesen werden soll
	 * @return	Wert des Subtyp-Attribues
	 */
	public String getSubAttribute(final Element node) {
		if (subLangKey==null || subLangKey.trim().isEmpty()) return "";
		return Language.trAllAttribute(subLangKey,node);
	}

	/**
	 * Gibt an, ob ein XML-Element Daten zu dem gloalen oder zu einem Subtyp-Element enthält
	 * @param node	XML-Element bei dem überprüft werden soll, ob es Daten zu dem gloalen oder zu einem Subtyp-Element enthält
	 * @return	Gibt <code>true</code> zurück, wenn das XML-Element Daten zu dem globalen Element enthält
	 */
	public boolean isGlobal(final Element node) {
		final String sub=getSubAttribute(node);
		return (sub==null || sub.trim().isEmpty());
	}

	/**
	 * Fügt die Beschreibung für die Daten dieses Objekts als Eigenschaft zu der Beschreibung hinzu
	 * @param descriptionBuilder	Description-Builder, der die Beschreibungsdaten zusammenfasst
	 * @param labelClient	Beschriftung für kundenspezifische Verteilungen (muss einen Platzhalter für den Kundentypnamen enthalten)
	 * @param labelGeneralCase	Beschriftung für die globale Verteilungen
	 * @param position	 Position in der Reihenfolge der Description-Builder Eigenschaften
	 */
	public void buildDescriptionProperty(final ModelDescriptionBuilder descriptionBuilder, final String labelClient, final String labelGeneralCase, final int position) {
		Object obj;

		for (final String clientTypeName: descriptionBuilder.getModel().surface.getClientTypes()) {
			obj=get(clientTypeName);
			if (obj instanceof AbstractRealDistribution) descriptionBuilder.addProperty(String.format(labelClient,clientTypeName),ModelDescriptionBuilder.getDistributionInfo((AbstractRealDistribution)obj),position);
			if (obj instanceof String) descriptionBuilder.addProperty(String.format(labelClient,clientTypeName),Language.tr("ModelDescription.Expression")+": "+(String)obj,position);
		}

		obj=get();
		if (obj instanceof AbstractRealDistribution) descriptionBuilder.addProperty(labelGeneralCase,ModelDescriptionBuilder.getDistributionInfo((AbstractRealDistribution)obj),position);
		if (obj instanceof String) descriptionBuilder.addProperty(labelGeneralCase,Language.tr("ModelDescription.Expression")+": "+(String)obj,position);
	}
}