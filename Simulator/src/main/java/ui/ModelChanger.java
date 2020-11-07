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
package ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.modeleditor.ModelResource;
import ui.optimizer.OptimizerSerialKernelBase;
import ui.parameterseries.ParameterCompareRunner;

/**
 * Ermöglicht das Ändern eines Wertes innerhalb eines Modells
 * @author Alexander Herzog
 * @see OptimizerSerialKernelBase
 * @see ParameterCompareRunner
 */
public class ModelChanger {
	/**
	 * Zu ändernde Eigenschaft
	 * @author Alexander Herzog
	 */
	public enum Mode {
		/** Anzahl der Bediener in einer Ressource ändern */
		MODE_RESOURCE,
		/** Initialen Wert einer Variable ändern */
		MODE_VARIABLE,
		/** Wert eines XML-Elements oder -Attributs ändern */
		MODE_XML
	}

	/**
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur statische Methoden zur Veränderung von Modellen bereit.
	 */
	private ModelChanger() {}

	/**
	 * Ändert einen Wert in einer Verteilung
	 * @param xmlChangeMode	Art der Änderung (siehe <code>XML_ELEMENT_MODES</code>)
	 * @param oldValue	Alte Verteilung (oder alte Zahl)
	 * @param newValue	Einzutragender Zahlenwert
	 * @return	Neue Verteilung (oder neue Zahl)
	 */
	private static String changeElementValue(final int xmlChangeMode, final String oldValue, final double newValue) {
		if (xmlChangeMode==0) {
			return NumberTools.formatNumber(newValue);
		} else {
			AbstractRealDistribution dist=DistributionTools.distributionFromString(oldValue,86400);
			if (dist==null) return null;
			AbstractRealDistribution newDist=null;
			switch (xmlChangeMode) {
			case 1: newDist=DistributionTools.setMean(dist,newValue); break;
			case 2:	newDist=DistributionTools.setStandardDeviation(dist,newValue); break;
			case 3: newDist=DistributionTools.setParameter(dist,1,newValue); break;
			case 4: newDist=DistributionTools.setParameter(dist,2,newValue); break;
			case 5: newDist=DistributionTools.setParameter(dist,3,newValue); break;
			case 6: newDist=DistributionTools.setParameter(dist,4,newValue); break;
			}
			if (newDist==null) return null;
			return DistributionTools.distributionToString(newDist);
		}
	}

	/**
	 * Ändert den Wert eines XML-Objekt
	 * @param selectors	Zusammenstellung der Pfad-Komponenten
	 * @param xmlChangeMode	Art der Änderung (siehe <code>XML_ELEMENT_MODES</code>)
	 * @param parent	Übergeordnetes XML-Element
	 * @param parentTags	Namen der übergeordneten Elemente
	 * @param newValue	Neuer Wert für das XML-Objekt
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private static String changeElement(final Scanner selectors, final int xmlChangeMode, final Element parent, final List<String> parentTags, final double newValue) {
		/* Selektor dekodieren */
		String sel=selectors.next();
		String tag=sel, attr="", attrValue="";
		int attrNr=-1;
		int index=sel.indexOf('[');
		if (index>=0) {
			if (!sel.endsWith("]")) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			attr=sel.substring(index+1,sel.length()-1).trim();
			tag=sel.substring(0,index).trim();
			if (attr.isEmpty()) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			index=attr.indexOf('=');
			if (index>=0) {
				attrValue=attr.substring(index+1).trim();
				attr=attr.substring(0,index).trim();
				if (attrValue.length()>2 && attrValue.charAt(0)=='"' && attrValue.endsWith("\""))
					attrValue=attrValue.substring(1,attrValue.length()-1);
			} else {
				Integer I=NumberTools.getInteger(attr);
				if (I!=null && I>=1) attrNr=I;
			}
		}

		/* Attribut aus Parent zurückgeben */
		if (!selectors.hasNext() && tag.isEmpty()) {
			final List<String> path=new ArrayList<>(parentTags);
			path.add(attr);
			String s=changeElementValue(xmlChangeMode,parent.getAttribute(attr),newValue);
			if (s==null) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidValue"),parent.getAttribute(attr));
			parent.setAttribute(attr,s);
			return null;
		}

		/* Kindelement suchen */
		Element searchResult=null;
		NodeList list=parent.getChildNodes();
		int nr=0;
		for (int i=0; i<list.getLength();i++) {
			if (!(list.item(i) instanceof Element)) continue;
			Element node=(Element)list.item(i);
			if (node.getNodeName().equalsIgnoreCase(tag)) {
				nr++;
				if (attr.isEmpty()) {searchResult=node; break;}
				if (!selectors.hasNext() && attrValue.isEmpty() && attrNr<0) {searchResult=node; break;}
				if (attrNr>0) {
					if (nr==attrNr) {searchResult=node; break;}
				} else {
					if (node.getAttribute(attr).equalsIgnoreCase(attrValue)) {searchResult=node; break;}
					if (node.getAttribute(attr).isEmpty() && attrValue.equals("\"\"")) {searchResult=node; break;}
				}
			}
		}
		if (searchResult==null) return String.format(Language.tr("Batch.Parameter.XMLTag.NoElementFound"),sel);

		/* Elementinhalt zurückgeben */
		if (!selectors.hasNext()) {
			final List<String> path=new ArrayList<>(parentTags);
			path.add(tag);
			if (attr.isEmpty() || !attrValue.isEmpty() || attrNr>=0) {
				String s=changeElementValue(xmlChangeMode,searchResult.getTextContent(),newValue);
				if (s==null) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidValue"),searchResult.getTextContent());
				searchResult.setTextContent(s);
				return null;
			} else {
				path.add(attr);
				String s=changeElementValue(xmlChangeMode,searchResult.getAttribute(attr),newValue);
				if (s==null) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidValue"),searchResult.getAttribute(attr));
				searchResult.setAttribute(attr,s);
				return null;
			}
		}

		/* Suche fortsetzen */
		final List<String> tags=new ArrayList<>(parentTags);
		tags.add(tag);
		return changeElement(selectors,xmlChangeMode,searchResult,tags,newValue);
	}

	/**
	 * Ändert den Wert eines XML-Objekt
	 * @param selectors	Zusammenstellung der Pfad-Komponenten
	 * @param parent	Übergeordnetes XML-Element
	 * @param parentTags	Namen der übergeordneten Elemente
	 * @param newValue	Neuer Wert für das XML-Objekt
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung
	 */
	private static String changeElement(final Scanner selectors, final Element parent, final List<String> parentTags, final String newValue) {
		/* Selektor dekodieren */
		String sel=selectors.next();
		String tag=sel, attr="", attrValue="";
		int attrNr=-1;
		int index=sel.indexOf('[');
		if (index>=0) {
			if (!sel.endsWith("]")) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			attr=sel.substring(index+1,sel.length()-1).trim();
			tag=sel.substring(0,index).trim();
			if (attr.isEmpty()) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			index=attr.indexOf('=');
			if (index>=0) {
				attrValue=attr.substring(index+1).trim();
				attr=attr.substring(0,index).trim();
				if (attrValue.length()>2 && attrValue.charAt(0)=='"' && attrValue.endsWith("\""))
					attrValue=attrValue.substring(1,attrValue.length()-1);
			} else {
				Integer I=NumberTools.getInteger(attr);
				if (I!=null && I>=1) attrNr=I;
			}
		}

		/* Attribut aus Parent zurückgeben */
		if (!selectors.hasNext() && tag.isEmpty()) {
			List<String> path=new ArrayList<>(parentTags);
			path.add(attr);
			parent.setAttribute(attr,newValue);
			return null;
		}

		/* Kindelement suchen */
		Element searchResult=null;
		NodeList list=parent.getChildNodes();
		int nr=0;
		for (int i=0; i<list.getLength();i++) {
			if (!(list.item(i) instanceof Element)) continue;
			Element node=(Element)list.item(i);
			if (node.getNodeName().equalsIgnoreCase(tag)) {
				nr++;
				if (attr.isEmpty()) {searchResult=node; break;}
				if (!selectors.hasNext() && attrValue.isEmpty()) {searchResult=node; break;}
				if (attrNr>0) {
					if (nr==attrNr) {searchResult=node; break;}
				} else {
					if (node.getAttribute(attr).equalsIgnoreCase(attrValue)) {searchResult=node; break;}
					if (node.getAttribute(attr).isEmpty() && attrValue.equals("\"\"")) {searchResult=node; break;}
				}
			}
		}
		if (searchResult==null) return String.format(Language.tr("Batch.Parameter.XMLTag.NoElementFound"),sel);

		/* Elementinhalt zurückgeben */
		if (!selectors.hasNext()) {
			List<String> path=new ArrayList<>(parentTags);
			path.add(tag);
			if (attr.isEmpty() || !attrValue.isEmpty()) {
				searchResult.setTextContent(newValue);
				return null;
			} else {
				path.add(attr);
				searchResult.setAttribute(attr,newValue);
				return null;
			}
		}

		/* Suche fortsetzen */
		List<String> tags=new ArrayList<>(parentTags);
		tags.add(tag);
		return changeElement(selectors,searchResult,tags,newValue);
	}

	/**
	 * Liefert den Wert eines XML-Objekt
	 * @param selectors	Zusammenstellung der Pfad-Komponenten
	 * @param xmlChangeMode	Art der Änderung (siehe <code>XML_ELEMENT_MODES</code>)
	 * @param parent	Übergeordnetes XML-Element
	 * @param parentTags	Namen der übergeordneten Elemente
	 * @return	Liefert den Wert des Elements oder eine Fehlermeldung
	 */
	private static String getElement(final Scanner selectors, final int xmlChangeMode, final Element parent, final List<String> parentTags) {
		/* Selektor dekodieren */
		String sel=selectors.next();
		String tag=sel, attr="", attrValue="";
		int attrNr=-1;
		int index=sel.indexOf('[');
		if (index>=0) {
			if (!sel.endsWith("]")) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			attr=sel.substring(index+1,sel.length()-1).trim();
			tag=sel.substring(0,index).trim();
			if (attr.isEmpty()) return String.format(Language.tr("Batch.Parameter.XMLTag.InvalidSelector"),sel);
			index=attr.indexOf('=');
			if (index>=0) {
				attrValue=attr.substring(index+1).trim();
				attr=attr.substring(0,index).trim();
				if (attrValue.length()>2 && attrValue.charAt(0)=='"' && attrValue.endsWith("\""))
					attrValue=attrValue.substring(1,attrValue.length()-1);
			} else {
				Integer I=NumberTools.getInteger(attr);
				if (I!=null && I>=1) attrNr=I;
			}
		}

		/* Attribut aus Parent zurückgeben */
		if (!selectors.hasNext() && tag.isEmpty()) {
			return parent.getAttribute(attr);
		}

		/* Kindelement suchen */
		Element searchResult=null;
		NodeList list=parent.getChildNodes();
		int nr=0;
		for (int i=0; i<list.getLength();i++) {
			if (!(list.item(i) instanceof Element)) continue;
			Element node=(Element)list.item(i);
			if (node.getNodeName().equalsIgnoreCase(tag)) {
				nr++;
				if (attr.isEmpty()) {searchResult=node; break;}
				if (!selectors.hasNext() && attrValue.isEmpty() && attrNr<=0) {searchResult=node; break;}
				if (attrNr>0) {
					if (nr==attrNr) {searchResult=node; break;}
				} else {
					if (node.getAttribute(attr).equalsIgnoreCase(attrValue)) {searchResult=node; break;}
					if (node.getAttribute(attr).isEmpty() && attrValue.equals("\"\"")) {searchResult=node; break;}
				}
			}
		}
		if (searchResult==null) return String.format(Language.tr("Batch.Parameter.XMLTag.NoElementFound"),sel);

		/* Elementinhalt zurückgeben */
		if (!selectors.hasNext()) {
			List<String> path=new ArrayList<>(parentTags);
			path.add(tag);
			if (attr.isEmpty() || !attrValue.isEmpty() || attrNr>0) {
				return searchResult.getTextContent();
			} else {
				return searchResult.getAttribute(attr);
			}
		}

		/* Suche fortsetzen */
		List<String> tags=new ArrayList<>(parentTags);
		tags.add(tag);
		return getElement(selectors,xmlChangeMode,searchResult,tags);
	}

	/**
	 * Liste mit Bezeichnern für die XML-Modi des Modell-Changers
	 */
	public static String[] XML_ELEMENT_MODES=new String[]{ /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */
			"Zahl",
			"Erwartungswert einer Verteilung",
			"Standardabweichung einer Verteilung",
			"Parameter 1 einer Verteilung",
			"Parameter 2 einer Verteilung",
			"Parameter 3 einer Verteilung",
			"Parameter 4 einer Verteilung"
	};

	/**
	 * Verändert einen Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param mode Modus
	 * @param tag	Ressource/globale Variable/XML-Tag, dessen Inhalt geändert werden soll
	 * @param xmlChangeMode	Art der Änderung (siehe <code>XML_ELEMENT_MODES</code>)
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 */
	public static Object changeModel(final EditModel originalModel, final Mode mode, final String tag, final int xmlChangeMode, final double value) {
		switch (mode) {
		case MODE_RESOURCE: return changeModelRes(originalModel,tag,value);
		case MODE_VARIABLE: return changeModelVar(originalModel,tag,value);
		case MODE_XML: return changeModelXML(originalModel,tag,xmlChangeMode,value);
		default: return originalModel;
		}
	}

	/**
	 * Verändert einen Ressourcen-Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param tag	Ressource, deren Wert geändert werden soll
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 * @see #changeModel(EditModel, Mode, String, int, double)
	 */
	private static Object changeModelRes(final EditModel originalModel, final String tag, final double value) {
		int intValue=(int)Math.max(0,Math.round(value));

		final EditModel editModel=originalModel.clone();
		final ModelResource resource=editModel.resources.get(tag);
		if (resource==null) return String.format(Language.tr("Batch.Parameter.Changed.UnknownResource"),tag);
		resource.setCount(intValue);

		editModel.description=String.format(Language.tr("Batch.Parameter.Changed.Resource"),tag,intValue)+"\n\n"+editModel.description;
		return editModel;
	}

	/**
	 * Verändert einen Variablen-Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param tag	Variable, deren Wert geändert werden soll
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 * @see #changeModel(EditModel, Mode, String, int, double)
	 */
	private static Object changeModelVar(final EditModel originalModel, final String tag, final double value) {
		final EditModel editModel=originalModel.clone();

		boolean ok=false;
		for (int i=0;i<editModel.globalVariablesNames.size();i++) if (editModel.globalVariablesNames.get(i).equals(tag)) {
			editModel.globalVariablesExpressions.set(i,NumberTools.formatNumberMax(value));
			ok=true;
			break;
		}
		if (!ok) return String.format(Language.tr("Batch.Parameter.Changed.NoVariable"),tag);

		editModel.description=String.format(Language.tr("Batch.Parameter.Changed.Variable"),tag,NumberTools.formatNumberMax(value))+"\n\n"+editModel.description;
		return editModel;
	}

	/**
	 * Verändert einen XML-Tag-Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param tag	XML-Tag, derren Inhalt geändert werden soll
	 * @param xmlChangeMode	Art der Änderung (siehe <code>XML_ELEMENT_MODES</code>)
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 * @see #changeModel(EditModel, Mode, String, int, double)
	 */
	private static Object changeModelXML(final EditModel originalModel, final String tag, final int xmlChangeMode, final double value) {
		final Document xmlDoc=originalModel.saveToXMLDocument();
		if (xmlDoc==null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToSave");

		try (Scanner selectors=new Scanner(tag)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return null;
			String s=changeElement(selectors,xmlChangeMode,xmlDoc.getDocumentElement(),new ArrayList<>(),value);
			if (s!=null) return s;
		}

		final EditModel editModel=new EditModel();
		if (editModel.loadFromXML(xmlDoc.getDocumentElement())!=null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToLoad");
		editModel.description=String.format(Language.tr("Batch.Parameter.Changed"),tag,NumberTools.formatNumber(value))+"\n\n"+editModel.description;
		return editModel;
	}

	/**
	 * Verändert einen Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param mode (0=Ressource, 1=globale Variable, 2=xml)
	 * @param tag	Ressource/globale Variable/XML-Tag, dessen Inhalt geändert werden soll
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 */
	public static Object changeModel(final EditModel originalModel, final int mode, final String tag, final String value) {
		switch (mode) {
		case 0: return changeModelRes(originalModel,tag,value);
		case 1: return changeModelVar(originalModel,tag,value);
		case 2: return changeModelXML(originalModel,tag,value);
		default: return originalModel;
		}
	}

	/**
	 * Verändert einen Ressourcen-Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param tag	Ressource, deren Wert geändert werden soll
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 * @see #changeModel(EditModel, int, String, String)
	 */
	private static Object changeModelRes(final EditModel originalModel, final String tag, final String value) {
		final Integer I=NumberTools.getInteger(value);
		if (I==0 || I<0) return String.format(Language.tr("Batch.Parameter.Changed.InvalidValueForResource"),value,tag);
		int intValue=I.intValue();

		final EditModel editModel=originalModel.clone();
		final ModelResource resource=editModel.resources.get(tag);
		if (resource==null) return String.format(Language.tr("Batch.Parameter.Changed.UnknownResource"),tag);
		resource.setCount(intValue);

		editModel.description=String.format(Language.tr("Batch.Parameter.Changed.Resource"),tag,intValue)+"\n\n"+editModel.description;
		return editModel;
	}

	/**
	 * Verändert einen Variablen-Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param tag	Variable, deren Wert geändert werden soll
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 * @see #changeModel(EditModel, int, String, String)
	 */
	private static Object changeModelVar(final EditModel originalModel, final String tag, final String value) {
		final EditModel editModel=originalModel.clone();

		boolean ok=false;
		for (int i=0;i<editModel.globalVariablesNames.size();i++) if (editModel.globalVariablesNames.get(i).equals(tag)) {
			editModel.globalVariablesExpressions.set(i,value);
			ok=true;
			break;
		}
		if (!ok) return String.format(Language.tr("Batch.Parameter.Changed.NoVariable"),tag);

		editModel.description=String.format(Language.tr("Batch.Parameter.Changed.Variable"),tag,value)+"\n\n"+editModel.description;
		return editModel;
	}

	/**
	 * Verändert einen XML-Tag-Wert in einem Modell
	 * @param originalModel	Ausgangsmodell
	 * @param tag	XML-Tag, dessen Inhalt geändert werden soll
	 * @param value	Neuer einzutragender Wert
	 * @return	Gibt im Erfolgsfall ein neues Modell vom Typ <code>EditModel</code> zurück. Im Fehlerfall wird eine Fehlermeldung als String zurückgegeben.
	 * @see #changeModel(EditModel, int, String, String)
	 */
	private static Object changeModelXML(final EditModel originalModel, final String tag, final String value) {
		final Document xmlDoc=originalModel.saveToXMLDocument();
		if (xmlDoc==null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToSave");

		try (Scanner selectors=new Scanner(tag)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return null;
			String s=changeElement(selectors,xmlDoc.getDocumentElement(),new ArrayList<>(),value);
			if (s!=null) return s;
		}

		final EditModel editModel=new EditModel();
		if (editModel.loadFromXML(xmlDoc.getDocumentElement())!=null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToLoad");
		editModel.description=String.format(Language.tr("Batch.Parameter.Changed"),tag,value)+"\n\n"+editModel.description;
		return editModel;
	}

	/**
	 * Liefert einen Wert aus dem Modell zurück
	 * @param model	Editor-Modell
	 * @param xmlName	XML-Tag, dessen Inhalt ausgelesen werden werden soll
	 * @return	Inhalt des Elements oder Fehlermeldung oder <code>null</code>
	 */
	public static String getValue(final EditModel model, final String xmlName) {
		final Document xmlDoc=model.saveToXMLDocument();
		if (xmlDoc==null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToSave");

		try (Scanner selectors=new Scanner(xmlName)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return null;
			return getElement(selectors,0,xmlDoc.getDocumentElement(),new ArrayList<>());
		}
	}

	/**
	 * Liefert einen Wert aus dem Modell zurück
	 * @param model	Editor-Modell
	 * @param xmlName	XML-Tag, dessen Inhalt ausgelesen werden werden soll
	 * @param xmlChangeMode	Gibt an, wie der Wert zu interpretieren ist (siehe {@link ModelChanger#XML_ELEMENT_MODES})
	 * @return	Inhalt des Elements oder Fehlermeldung oder <code>null</code>
	 */
	public static String getValue(final EditModel model, final String xmlName, final int xmlChangeMode) {
		final String value=getValue(model,xmlName);
		if (value==null) return null;

		if (xmlChangeMode==0) {
			return value;
		} else {
			final AbstractRealDistribution dist=DistributionTools.distributionFromString(value,86400);
			if (dist==null) return null;
			switch (xmlChangeMode) {
			case 1: return NumberTools.formatNumberMax(DistributionTools.getMean(dist));
			case 2: return NumberTools.formatNumberMax(DistributionTools.getStandardDeviation(dist));
			case 3: return NumberTools.formatNumberMax(DistributionTools.getParameter(dist,1));
			case 4: return NumberTools.formatNumberMax(DistributionTools.getParameter(dist,2));
			case 5: return NumberTools.formatNumberMax(DistributionTools.getParameter(dist,3));
			case 6: return NumberTools.formatNumberMax(DistributionTools.getParameter(dist,4));
			default: return null;
			}
		}
	}

	/**
	 * Liefert einen Wert aus einem Statistik-Objekt zurück
	 * @param statistics	Statistik-Objekt
	 * @param xmlName	XML-Tag, dessen Inhalt ausgelesen werden werden soll
	 * @return	Inhalt des Elements oder Fehlermeldung oder <code>null</code>
	 */
	public static String getStatisticValue(final Statistics statistics, final String xmlName) {
		final Document xmlDoc=statistics.saveToXMLDocument();
		if (xmlDoc==null) return Language.tr("Batch.Parameter.XMLTag.NotAbleToSave");

		try (Scanner selectors=new Scanner(xmlName)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return null;
			return getElement(selectors,0,xmlDoc.getDocumentElement(),new ArrayList<>());
		}
	}
}
