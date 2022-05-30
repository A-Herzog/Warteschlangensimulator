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
package scripting.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import language.Language;
import language.LanguageStaticLoader;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import scripting.js.JSRunDataFilter;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import xml.XMLTools;

/**
 * Implementierungsklasse für das Interface {@link StatisticsInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class StatisticsImpl implements StatisticsInterface {
	/**
	 * Abbruch-Status
	 * @see #cancel()
	 */
	private boolean canceled=false;

	/**
	 * Gibt an, ob die Statistikdaten als Datei gespeichert werden dürfen
	 */
	private final boolean allowSave;

	/** XMl-Statistik-Daten, die gefiltert werden sollen */
	private Document xml;
	/** Optional: Name der Datei aus der die XML-Statistik-Daten stammen */
	private File xmlFile;
	/** Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen */
	private final Consumer<String> errorOutput;

	/** Ausgabe als Prozentwert (<code>true</code>) oder normale Fließkommazahl (<code>false</code>)? */
	private boolean percent;
	/** Legt fest, ob Zahlen in System- (<code>true</code>) oder lokaler Notation (<code>false</code>) ausgegeben werden sollen. */
	private boolean systemNumbers;
	/** Als Zeitangabe (<code>true</code>) oder als Zahl (<code>false</code>) ausgeben? */
	private boolean time;
	/** Trennzeichen für die Ausgabe von Verteilungsdaten */
	private char separator=';';

	/**
	 * Konstruktor der Klasse
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 * @param xml	XMl-Statistik-Daten, die gefiltert werden sollen
	 * @param xmlFile	Optional: Name der Datei aus der die XML-Statistik-Daten stammen
	 * @param allowSave	Gibt an, ob die Statistikdaten als Datei gespeichert werden dürfen
	 */
	public StatisticsImpl(final Consumer<String> output, final Document xml, final File xmlFile, final boolean allowSave) {
		this.xml=xml;
		this.xmlFile=xmlFile;
		this.errorOutput=output;
		this.allowSave=allowSave;
	}

	/**
	 * Stellt das zu verwendende XML-Dokument ein
	 * @param xml	Zu verwendendes XML-Dokument
	 * @param xmlFile	Optional: Name der Datei aus der die XML-Statistik-Daten stammen
	 */
	public void setStatistics(final Document xml, final File xmlFile) {
		this.xml=xml;
		this.xmlFile=xmlFile;
	}

	/**
	 * Gibt eine Fehlermeldung über {@link #errorOutput} aus.
	 * @param text	Fehlermeldung
	 * @see #errorOutput
	 */
	private void addOutput(final String text) {
		if (errorOutput!=null) errorOutput.accept(text);
	}

	/**
	 * Selektiert ein XML-Objekt
	 * @param selectors	Zusammenstellung der Pfad-Komponenten
	 * @param parent	Übergeordnetes XML-Element
	 * @param parentTags	Namen der übergeordneten Elemente
	 * @param systemNumbers	 Legt fest, ob Zahlen in System- oder lokaler Notation ausgegeben werden sollen.
	 * @param percent	Als Prozentwert oder normale Fließkommazahl
	 * @param time	Als Zeitangabe oder als Zahl
	 * @param distributionSeparator	Trenner für die Einträge bei Verteilungen
	 * @return	Gibt ein String-Array aus zwei Elementen zurück. Im ersten Eintrag wird ein Fehler und im zweiten ein Wert zurückgegeben. Genau einer der beiden Einträge ist immer <code>null</code>.
	 */
	private String[] findElement(Scanner selectors, Element parent, List<String> parentTags, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		/* Selektor dekodieren */
		String sel=selectors.next();
		String tag=sel, attr="", attrValue="";
		int attrNr=-1;
		int index=sel.indexOf('[');
		if (index>=0) {
			if (!sel.endsWith("]")) return new String[]{Language.tr("Statistics.Filter.InvalidSelector")+" ("+sel+")",null};
			attr=sel.substring(index+1,sel.length()-1).trim();
			tag=sel.substring(0,index).trim();
			if (attr.isEmpty()) return new String[]{Language.tr("Statistics.Filter.InvalidSelector")+" ("+sel+")",null};
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
			return new String[]{null,formatNumber(parent.getAttribute(attr),path,systemNumbers,percent,time,distributionSeparator)};
		}

		/* Kindelement suchen */
		Element searchResult=null;
		NodeList list=parent.getChildNodes();
		int nr=0;
		for (int i=0;i<list.getLength();i++) {
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
		if (searchResult==null) return new String[]{String.format(Language.tr("Statistics.Filter.NoElementMatchingSelector"),sel),null};

		/* Elementinhalt zurückgeben */
		if (!selectors.hasNext()) {
			List<String> path=new ArrayList<>(parentTags);
			path.add(tag);
			if (attr.isEmpty() || !attrValue.isEmpty()) {
				return new String[]{null,formatNumber(searchResult.getTextContent(),path,systemNumbers,percent,time,distributionSeparator)};
			} else {
				path.add(attr);
				return new String[]{null,formatNumber(searchResult.getAttribute(attr),path,systemNumbers,percent,time,distributionSeparator)};
			}
		}

		/* Suche fortsetzen */
		List<String> tags=new ArrayList<>(parentTags);
		tags.add(tag);
		return findElement(selectors,searchResult,tags,systemNumbers,percent,time,distributionSeparator);
	}

	/**
	 * Einträge die nicht formatiert werden sollen.
	 * @see #doNotFormatCheck(List)
	 */
	private static String[] doNotFormat;

	static {
		updateLanguage();
	}

	/**
	 * Aktualisiert die Sprachdaten in Bezug darauf, welche XML-Elemente nicht formatiert werden dürfen.<br>
	 * Diese statische Methode muss nach einer Änderung der Sprache aufgerufen werden
	 */
	public static void updateLanguage() {
		doNotFormat=new String[]{
				Language.trPrimary("Statistics.XML.Root")+","+Language.trPrimary("Surface.XML.Model.Root")+","+Language.trPrimary("Surface.XML.ModelVersion"),
				Language.trPrimary("Statistics.XML.Root")+","+Language.trPrimary("Surface.XML.Model.Root")+","+Language.trPrimary("Surface.XML.ModelName"),
				Language.trPrimary("Statistics.XML.Root")+","+Language.trPrimary("Surface.XML.Model.Root")+","+Language.trPrimary("Surface.XML.ModelDescription"),
				Language.trPrimary("Statistics.XML.Root")+","+Language.trPrimary("Statistics.XML.Element.Simulation")+","+Language.trPrimary("Statistics.XML.RunDate"),
				Language.trPrimary("Statistics.XML.Root")+","+Language.trPrimary("Statistics.XML.Element.Simulation")+","+Language.trPrimary("Statistics.XML.RunOS"),
				Language.trPrimary("Statistics.XML.Root")+","+Language.trPrimary("Statistics.XML.Element.Simulation")+","+Language.trPrimary("Statistics.XML.RunUser")
		};
	}

	/**
	 * Prüft, ob für den Inhalt eines XML-Pfades von der Formatierung ausgenommen werden soll.
	 * @param path	XML-Pfad
	 * @return	Keine Formatierung des Inhalts?
	 */
	private static boolean doNotFormatCheck(List<String> path) {
		if (path==null || path.size()==0) return false;
		for (int i=0;i<doNotFormat.length;i++) {
			String[] s=doNotFormat[i].split(",");
			if (s.length!=path.size()) continue;
			boolean b=true;
			for (int j=0;j<FastMath.min(s.length,path.size());j++) if (!s[j].equalsIgnoreCase(path.get(j)) && !s[j].equals("*")) {b=false; break;}
			if (b) return true;
		}
		return false;
	}

	/**
	 * Formatiert ein Zahl
	 * @param value	Zu formatierender Wert
	 * @param path	XML-Pfad zu dem Wert (über <code>doNotFormatCheck</code> wird abgefragt, ob dieses Element formatiert werden darf)
	 * @param systemNumbers	Dezimalkomma (lokale Formatierung) oder Dezimalpunkt (System-Formatierung)
	 * @param percent	Formatiert den Wert als Prozentwert
	 * @param time	Formatiert den Zahlenwert als Zeit
	 * @param distributionSeparator	Zu verwendendes Trennzeichen bei Verteilungen
	 * @return	Formatierter Zahlenwert
	 */
	public static String formatNumber(String value, List<String> path, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		if (doNotFormatCheck(path)) return value;

		/* if (systemNumbers) return value; */

		if (value.indexOf(';')>=0) {
			/* Verteilung */
			DataDistributionImpl dist=DataDistributionImpl.createFromString(value,1000);
			if (dist==null) return value;
			if (systemNumbers) return dist.storeToString(Character.toString(distributionSeparator)); else return dist.storeToLocalString(Character.toString(distributionSeparator));
		}

		Double D=null;
		if (value.indexOf(':')>=0) {
			/* Zeitangabe */
			D=TimeTools.getExactTime(value);
		} else {
			/* Einzelwert */
			int index1=value.indexOf('.');
			int index2=value.lastIndexOf('.');
			/* Erkennen, ob wirklich Zahl oder z.B. Versionskennung oder Datum */
			if (index1<0 || index2<0 || index1==index2) D=NumberTools.getExtProbability(NumberTools.systemNumberToLocalNumber(value));
		}

		if (D==null) return value;

		String suffix="";
		if (time) {
			if (systemNumbers) return TimeTools.formatExactSystemTime(D); else return TimeTools.formatExactTime(D);
		} else {
			if (percent) {D=D*100; suffix="%";}
			if (systemNumbers) return NumberTools.formatSystemNumber(D)+suffix; else return NumberTools.formatNumberMax(D)+suffix;
		}
	}

	/**
	 * Selektiert ein XML-Objekt
	 * @param command	Suchstring für das XML-Objekt
	 * @param systemNumbers	 Legt fest, ob Zahlen in System- oder lokaler Notation ausgegeben werden sollen.
	 * @param percent	Als Prozentwert oder normale Fließkommazahl
	 * @param time	Als Zeitangabe oder als Zahl
	 * @param distributionSeparator	Trenner für die Einträge bei Verteilungen
	 * @return	Gibt ein String-Array aus zwei Elementen zurück. Im ersten Eintrag wird ein Fehler und im zweiten ein Wert zurückgegeben. Genau einer der beiden Einträge ist immer <code>null</code>.
	 */
	private final String[] findElement(String command, boolean systemNumbers, boolean percent, boolean time, char distributionSeparator) {
		try (Scanner selectors=new Scanner(command)) {
			selectors.useDelimiter("->");
			if (!selectors.hasNext()) return new String[]{Language.tr("Statistics.Filter.InvalidParameters")+" ("+command+")",null};
			if (xml==null) return new String[]{Language.tr("Statistics.Filter.InvalidSelector")+" ("+command+")",null};
			return findElement(selectors,xml.getDocumentElement(),new ArrayList<>(),systemNumbers,percent,time,distributionSeparator);
		}
	}

	@Override
	public void setFormat(final String format) {
		if (format==null) return;

		if (format.equalsIgnoreCase("lokal") || format.equalsIgnoreCase("local")) {systemNumbers=false; return;}
		if (format.equalsIgnoreCase("system")) {systemNumbers=true; return;}
		if (format.equalsIgnoreCase("fraction") || format.equalsIgnoreCase("bruch")) {time=false; percent=false; return;}
		if (format.equalsIgnoreCase("percent") || format.equalsIgnoreCase("prozent")) {time=false; percent=true; return;}
		if (format.equalsIgnoreCase("time") || format.equalsIgnoreCase("zeit")) {time=true; return;}
		if (format.equalsIgnoreCase("number") || format.equalsIgnoreCase("zahl")) {time=false; return;}

		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+format+")");
	}

	@Override
	public void setSeparator(final String separator) {
		if (separator==null) return;

		if (separator.equalsIgnoreCase("semikolon") || separator.equalsIgnoreCase("semicolon")) {this.separator=';'; return;}
		if (separator.equalsIgnoreCase("line") || separator.equalsIgnoreCase("lines") || separator.equalsIgnoreCase("newline") || separator.equalsIgnoreCase("zeilen") || separator.equalsIgnoreCase("zeile")) {this.separator='\n'; return;}
		if (separator.equalsIgnoreCase("tab") || separator.equalsIgnoreCase("tabs") || separator.equalsIgnoreCase("tabulator")) {this.separator='\t'; return;}

		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+separator+")");
	}

	/**
	 * Versucht eine Zeichenkette in ein Verteilungsobjekt umzuwandeln
	 * @param value	Zeichenkette
	 * @return	Liefert im Erfolgsfall ein Verteilungsobjekt und im Fehlerfall eine Fehlermeldung
	 * @see #getDistribution(String)
	 */
	private Object getDistributionFromString(final String value) {
		AbstractRealDistribution dist=DistributionTools.distributionFromString(value,86400);
		if (dist==null) return String.format(Language.tr("Statistics.Filter.InvalidDistribution"),value);

		if (dist instanceof DataDistributionImpl) {
			final double[] density=((DataDistributionImpl)dist).densityData;
			final int steps=density.length;
			DataDistributionImpl dist2=new DataDistributionImpl(steps-1,steps);
			dist2.densityData=Arrays.copyOf(density,steps);
			dist=dist2;
		}

		return dist;
	}

	/**
	 * Versucht ein Verteilungsobjekt basierend auf dem Inhalt eines XML-Pfades zu erzeugen
	 * @param path	XML-Pfad
	 * @return	Liefert im Erfolgsfall ein Verteilungsobjekt und im Fehlerfall eine Fehlermeldung
	 */
	private Object getDistribution(final String path) {
		if (path==null) return "";

		final String[] result=findElement(path,true,false,false,';');
		if (result[0]!=null) return result[0];

		return getDistributionFromString(result[1]);
	}

	@Override
	public Object xmlArray(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(path);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).densityData;
	}

	@Override
	public Object xmlSum(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(path);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).sum();
	}

	@Override
	public boolean save(final String fileName) {
		if (!allowSave || canceled) return false;

		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable")+"\n");
			return false;
		}

		if (fileName==null) return false;

		final File file=new File(fileName);
		if (file.exists()) {
			addOutput(String.format(Language.tr("Statistics.Filter.OutputAlreadyExist"),file.toString())+"\n");
			return false;
		}

		final XMLTools xmlTools=new XMLTools(file);
		if (!xmlTools.save(xml.getDocumentElement())) {
			addOutput(String.format(Language.tr("Statistics.Filter.CouldNotSaveStatistics"),file.toString())+"\n");
			return false;
		}

		return true;
	}

	@Override
	public boolean saveNext(final String folderName) {
		if (!allowSave || canceled) return false;

		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable")+"\n");
			return false;
		}

		if (folderName==null) return false;

		final File folder=new File(folderName);
		if (!folder.isDirectory()) {
			addOutput(String.format(Language.tr("Statistics.Filter.IsNotDirectory"),folder.toString())+"\n");
			return false;
		}

		int i=0;
		File file=null;
		while (file==null || file.exists()) {
			i++;
			if (i>9999) return false;
			file=new File(folder,String.format(Language.tr("Optimizer.OutputFileFormat"),i));
		}

		final XMLTools xmlTools=new XMLTools(file);
		if (!xmlTools.save(xml.getDocumentElement())) {
			addOutput(String.format(Language.tr("Statistics.Filter.CouldNotSaveStatistics"),file.toString())+"\n");
			return false;
		}

		return true;
	}

	@Override
	public Object xmlMean(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(path);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).getMean();
	}

	@Override
	public Object xmlSD(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(path);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).getStandardDeviation();
	}

	@Override
	public Object xmlCV(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(path);
		if (dist instanceof String) return dist;

		return DistributionTools.getCV((DataDistributionImpl)dist);
	}

	@Override
	public Object xmlMedian(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(path);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).getMedian();
	}

	@Override
	public Object xmlMode(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(path);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).getMode();
	}

	@Override
	public String xml(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		if (path==null) return "";

		String[] result=findElement(path,systemNumbers,percent,time,separator);
		if (result[0]!=null) return result[0];
		if (result[1]!=null) return result[1];
		return "";
	}

	@Override
	public Object xmlNumber(final String path) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		if (path==null) return "";

		String[] result=findElement(path,true,false,false,separator);
		if (result[0]!=null) return result[0];
		if (result[1]!=null) {
			final Double D=NumberTools.getDouble(result[1]);
			if (D!=null) return D; else return result[1];
		}
		return "";
	}

	/**
	 * Wendet das angegebene Javascript-Skript auf die Statistikdaten an und gibt das Ergebnis zurück.
	 * @param fileName	Skriptdateiname
	 * @return	Rückgabewert des Skriptes
	 */
	@Override
	public String filter(final String fileName) {
		if (!allowSave || canceled) return "";

		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable")+"\n");
			return "";
		}

		if (!(fileName instanceof String)) return "";

		final File file=new File(fileName);
		if (!file.exists()) {
			addOutput(String.format(Language.tr("Statistics.Filter.ScriptDoesNotExist"),file.toString())+"\n");
			return "";
		}
		String[] lines;
		try {
			lines = Files.lines(file.toPath()).toArray(String[]::new);
		} catch (IOException e) {
			addOutput(String.format(Language.tr("Statistics.Filter.CouldNotLoadScript"),file.toString())+"\n");
			return "";
		}
		final String script=String.join("\n",lines);

		final JSRunDataFilter filter=new JSRunDataFilter(xml,xmlFile);
		filter.run(script);
		return filter.getResults();
	}

	@Override
	public void cancel() {
		canceled=true;
	}

	/**
	 * Übersetzt ein XML-Dokument
	 * @param xml	Zu übersetzendes XML-Dokument
	 * @param language	Neue Sprache ("de" oder "en")
	 * @return	Liefert das neue XML-Dokument
	 */
	private Document translate(final Document xml, final String language) {
		final String currentLanguage=Language.getCurrentLanguage();

		try {
			if (!currentLanguage.equalsIgnoreCase(language)) {
				Language.init(language);
				LanguageStaticLoader.setLanguage();
			}

			if (Language.trPrimary("Statistics.XML.Root").equalsIgnoreCase(xml.getDocumentElement().getNodeName())) {
				return xml;
			}

			final Statistics statistics=new Statistics();
			if (statistics.loadFromXML(xml.getDocumentElement())!=null) return null;
			return statistics.saveToXMLDocument();
		} finally {
			if (!currentLanguage.equalsIgnoreCase(language)) {
				Language.init(currentLanguage);
				LanguageStaticLoader.setLanguage();
			}
		}
	}

	/**
	 * Übersetzt die vorliegende Statistikdatei
	 * @param language	Neue Sprache ("de" oder "en")
	 * @return	Gibt an, ob das Übersetzen erfolgreich war
	 */
	@Override
	public boolean translate(final String language) {
		if (!Language.isSupportedLanguage(language)) return false;
		final Document newXml=translate(xml,language);
		if (newXml==null) return false;
		xml=newXml;
		return true;
	}

	/**
	 * Versucht basierend auf dem Namen einer Station die zugehörige ID zu ermitteln
	 * @param surface	Zeichenfläche auf der (und deren Unterzeichenflächen) gesucht werden soll
	 * @param name	Name der Station
	 * @return	Zugehörige ID oder -1, wenn keine passende Station gefunden wurde
	 */
	private int getStationID(final ModelSurface surface, final String name) {
		for (ModelElement element1: surface.getElements()) {
			if (element1.getName().equalsIgnoreCase(name)) return element1.getId();
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2.getName().equalsIgnoreCase(name)) return element2.getId();
			}
		}

		return -1;
	}

	/**
	 * Versucht basierend auf dem Namen einer Station die zugehörige ID zu ermitteln
	 * @param name	Name der Station
	 * @return	Zugehörige ID oder -1, wenn keine passende Station gefunden wurde
	 */
	@Override
	public int getStationID(final String name) {
		if (name==null || name.trim().isEmpty()) return -1;

		final Element root=xml.getDocumentElement();

		final EditModel model=new EditModel();
		if (model.loadFromXML(root)==null) return getStationID(model.surface,name);

		final Statistics statistics=new Statistics();
		if (statistics.loadFromXML(root)==null) {
			statistics.loadedStatistics=xmlFile;
			return getStationID(statistics.editModel.surface,name);
		}

		return -1;
	}

	@Override
	public String getStatisticsFile() {
		if (xmlFile==null) return "";
		return xmlFile.toString();
	}

	@Override
	public String getStatisticsFileName() {
		if (xmlFile==null) return "";
		return xmlFile.getName();
	}
}
