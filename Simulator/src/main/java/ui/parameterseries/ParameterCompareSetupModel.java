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
package ui.parameterseries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;

/**
 * Hält ein Modell für die Parameter-Vergleichs-Funktion vor.
 * @author Alexander Herzog
 * @see ParameterCompareSetup
 */
public final class ParameterCompareSetupModel extends ParameterCompareSetupBase implements Cloneable {
	/** Liste der Eingangsgrößen mit ihren Zahlwertbelegungen */
	private final Map<String,Double> input;
	/** Ausgabegrößen mit ihren Zahlenwertbelegungen */
	private final Map<String,Double> output;
	/** In-Arbeit-Status des Modells (-1 für "nicht in Arbeit, sonst Prozentwert) */
	private int inProcess; /* wird nicht gespeichert */
	/** Statistik-Ergebnisse für dieses Modell */
	private Document statisticsDocument;

	/**
	 * Konstruktor der Klasse
	 */
	public ParameterCompareSetupModel() {
		super();
		input=new HashMap<>();
		output=new HashMap<>();
		inProcess=-1;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name für das Element
	 */
	public ParameterCompareSetupModel(final String name) {
		super(name);
		input=new HashMap<>();
		output=new HashMap<>();
		inProcess=-1;
	}

	/**
	 * Liefert die Liste der Eingangsgrößen mit ihren Zahlwertbelegungen.
	 * @return	Liste der Eingangsgrößen mit ihren Zahlwertbelegungen
	 */
	public Map<String,Double> getInput() {
		return input;
	}

	/**
	 * Liefert die Liste der Ausgabegrößen mit ihren Zahlenwertbelegungen.
	 * @return	Liste der Ausgabegrößen mit ihren Zahlenwertbelegungen
	 */
	public Map<String,Double> getOutput() {
		return output;
	}

	/**
	 * Gibt an, ob das Modell gerade simuliert wird
	 * @return	In-Arbeit-Status des Modells (-1 für "nicht in Arbeit, sonst Prozentwert)
	 */
	public int isInProcess() {
		return inProcess;
	}

	/**
	 * Stellt ein, ob das Modell gerade simuliert wird (Wert zwischen 0 und 100) oder nicht (-1)
	 * @param inProcess	In-Arbeit-Status des Modells
	 */
	public void setInProcess(final int inProcess) {
		this.inProcess=inProcess;
	}

	/**
	 * Gibt an, ob Statistik-Ergebnisse vorliegen
	 * @return	Sind Statistik-Ergebnisse vorhanden?
	 * @see ParameterCompareSetupModel#getStatistics()
	 */
	public boolean isStatisticsAvailable() {
		return statisticsDocument!=null;
	}

	/**
	 * Liefert die Statistik-Ergebnisse für dieses Modell
	 * @return	Statistik-Ergebnisse oder <code>null</code>, wenn keine Statistik-Ergebnisse vorliegen
	 * @see ParameterCompareSetupModel#isStatisticsAvailable()
	 */
	public Statistics getStatistics() {
		if (statisticsDocument==null) return null;
		final Statistics statistics=new Statistics();
		if (statistics.loadFromXML(statisticsDocument.getDocumentElement())!=null) return null;
		return statistics;
	}

	/**
	 * Liefert die Statistik-Ergebnisse für dieses Modell als XML-Dokument
	 * @return	Statistik-Ergebnisse als XML-Dokument oder <code>null</code>, wenn keine Statistik-Ergebnisse vorliegen
	 * @see ParameterCompareSetupModel#isStatisticsAvailable()
	 */
	public Document getStatisticsDocument() {
		return statisticsDocument;
	}

	/**
	 * Speichert die Statistik-Ergebnisse in diesem Objekt
	 * @param statistics	Statistik-Ergebnisse oder <code>null</code>, wenn die Ergebnisse gelöscht werden sollen
	 * @see ParameterCompareSetupModel#getStatistics()
	 */
	public void setStatistics(final Statistics statistics) {
		if (statistics==null) {
			statisticsDocument=null;
			return;
		}

		statisticsDocument=statistics.saveToXMLDocument();
	}

	/**
	 * Löscht alle in diesem Modell gespeicherten Statistik-Ergebnisse
	 */
	public void clearOutputs() {
		output.clear();
		statisticsDocument=null;
	}

	/**
	 * Trägt ggf. Vorgabewerte in neue Input-Parameter bei dem Modell ein
	 * @param model	Editor-Model aus dem die Daten ausgelesen werden sollen
	 * @param setup	Gesamt-Setup aus dem u.a. die Input-Parameter ausgelesen werden
	 */
	public void updateInputValuesInModel(final EditModel model, final ParameterCompareSetup setup) {
		for (ParameterCompareSetupValueInput inputName:	setup.getInput()) {
			if (input.get(inputName.getName())!=null) continue;
			final Double defaultValue=ParameterCompareTools.getModelValue(model,inputName);
			if (defaultValue!=null) input.put(inputName.getName(),defaultValue);
		}
	}

	/**
	 * Vergleich den Modell-Einstellungen-Datensatz mit einem anderen Einstellungen-Objekt
	 * @param otherRecord	Anderes Einstellungen-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsParameterCompareSetupRecord(final ParameterCompareSetupModel otherRecord) {
		if (!getName().equals(otherRecord.getName())) return false;
		if (!Objects.deepEquals(input,otherRecord.input)) return false;
		if (!Objects.deepEquals(output,otherRecord.output)) return false;
		if (statisticsDocument==null) {
			if (otherRecord.statisticsDocument!=null) return false;
		} else {
			if (otherRecord.statisticsDocument==null) return false;
		}
		return true;
	}

	private boolean initStatisticsDocument() {
		DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db=dbf.newDocumentBuilder();
			statisticsDocument=db.newDocument();
		} catch (ParserConfigurationException e) {return false;}
		return true;
	}

	@Override
	public ParameterCompareSetupModel clone() {
		final ParameterCompareSetupModel clone=new ParameterCompareSetupModel();
		clone.setName(getName());
		clone.getInput().putAll(input);
		clone.getOutput().putAll(output);
		clone.setInProcess(inProcess);

		if (statisticsDocument!=null) {
			if (clone.initStatisticsDocument()) {
				clone.statisticsDocument.appendChild(clone.statisticsDocument.importNode(statisticsDocument.getDocumentElement(),true));
			}
		}

		return clone;
	}

	@Override
	public String[] getRootNodeNames() {
		return Language.trAll("ParameterCompare.XML.Models.Root");
	}

	@Override
	protected String loadPropertyFromXML(final String name, final String content, final Element node) {
		final String error=super.loadPropertyFromXML(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("ParameterCompare.XML.Models.InputValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			final String n=Language.trAllAttribute("ParameterCompare.XML.Models.ValueName",node);
			if (n.trim().isEmpty()) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("ParameterCompare.XML.Models.ValueName"),name,node.getParentNode().getNodeName());
			input.put(n,D);
			return null;
		}

		if (Language.trAll("ParameterCompare.XML.Models.OutputValue",name)) {
			final Double D=NumberTools.getDouble(NumberTools.systemNumberToLocalNumber(content));
			if (D==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			final String n=Language.trAllAttribute("ParameterCompare.XML.Models.ValueName",node);
			if (n.trim().isEmpty()) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("ParameterCompare.XML.Models.ValueName"),name,node.getParentNode().getNodeName());
			output.put(n,D);
			return null;
		}

		for (String test: new Statistics().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			if (initStatisticsDocument()) {
				statisticsDocument.appendChild(statisticsDocument.importNode(node,true));
			}
			return null;
		}

		return null;
	}

	@Override
	protected void addPropertiesToXML(Document doc, Element node) {
		super.addPropertiesToXML(doc,node);

		for (Map.Entry<String,Double> entry: input.entrySet()) if (entry.getValue()!=null) {
			final Element sub=doc.createElement(Language.tr("ParameterCompare.XML.Models.InputValue"));
			node.appendChild(sub);
			sub.setAttribute(Language.tr("ParameterCompare.XML.Models.ValueName"),entry.getKey());
			final Double D=entry.getValue();
			sub.setTextContent(NumberTools.formatSystemNumber(D.doubleValue()));
		}

		for (Map.Entry<String,Double> entry: output.entrySet()) if (entry.getValue()!=null) {
			final Element sub=doc.createElement(Language.tr("ParameterCompare.XML.Models.OutputValue"));
			node.appendChild(sub);
			sub.setAttribute(Language.tr("ParameterCompare.XML.Models.ValueName"),entry.getKey());
			final Double D=entry.getValue();
			sub.setTextContent(NumberTools.formatSystemNumber(D.doubleValue()));
		}

		if (statisticsDocument!=null) {
			node.appendChild(doc.importNode(statisticsDocument.getDocumentElement(),true));
			/* Beim Einfügen werden Zeilenumbrüche zu neuen #Text-Elementen, die dann einen eigenen Zeichenumbruch bekommen. Diese #Text-Elemente müssen wir folglich loswerden. */
			try {
				final XPathFactory xpathFactory=XPathFactory.newInstance();
				final XPathExpression xpathExp=xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
				final NodeList emptyTextNodes=(NodeList)xpathExp.evaluate(doc,XPathConstants.NODESET);
				for (int i=0;i<emptyTextNodes.getLength();i++) {
					final Node emptyTextNode=emptyTextNodes.item(i);
					emptyTextNode.getParentNode().removeChild(emptyTextNode);
				}
			} catch (XPathExpressionException e) {}
		}
	}
}
