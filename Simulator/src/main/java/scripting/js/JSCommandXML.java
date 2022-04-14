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
package scripting.js;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

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
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import xml.XMLTools;

/**
 * Stellt das "Statistics"-Objekt in Javascript-Umgebungen zur Verfügung
 * @author Alexander Herzog
 */
public class JSCommandXML extends JSBaseCommand {
	/**
	 * Abbruch-Status
	 * @see #cancel()
	 */
	private boolean canceled=false;

	/** XMl-Statistik-Daten, die gefiltert werden sollen */
	private Document xml;
	/** Optional: Name der Datei aus der die XML-Statistik-Daten stammen */
	private File xmlFile;
	/** Gibt an, ob die Statistikdaten als Datei gespeichert werden dürfen */
	private final boolean allowSave;

	/** Ausgabe als Prozentwert (<code>true</code>) oder normale Fließkommazahl (<code>false</code>)? */
	private boolean percent;
	/** Legt fest, ob Zahlen in System- (<code>true</code>) oder lokaler Notation (<code>false</code>) ausgegeben werden sollen. */
	private boolean systemNumbers;
	/** Als Zeitangabe (<code>true</code>) oder als Zahl (<code>false</code>) ausgeben? */
	private boolean time;
	/** Trennzeichen für die Ausgabe von Verteilungsdaten */
	private char separator=';';

	/**
	 * Konstruktor der Klasse <code>JSFilterCommandXML</code>
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 * @param xml	XMl-Statistik-Daten, die gefiltert werden sollen
	 * @param xmlFile	Optional: Name der Datei aus der die XML-Statistik-Daten stammen
	 * @param allowSave	Gibt an, ob die Statistikdaten als Datei gespeichert werden dürfen
	 */
	public JSCommandXML(final JSOutputWriter output, final Document xml, final File xmlFile, final boolean allowSave) {
		super(output);
		this.xml=xml;
		this.xmlFile=xmlFile;
		this.allowSave=allowSave;
	}

	/**
	 * Stellt das zu verwendende XML-Dokument ein
	 * @param xml	Zu verwendendes XML-Dokument
	 * @param xmlFile	Optional: Name der Datei aus der die XML-Statistik-Daten stammen
	 */
	public void setXML(final Document xml, final File xmlFile) {
		this.xml=xml;
		this.xmlFile=xmlFile;
	}

	/**
	 * Selektiert ein XML-Objekt
	 * @param selectors	Zusammenstellung der Pfad-Komponenten
	 * @param parent	Übergeordnetes XML-Element
	 * @param parentTags	Namen der übergeordneten Elemente
	 * @param systemNumbers	 Legt fest, ob Zahlen in System- oder lokaler Notation ausgegeben werden sollen.
	 * @param percent	Als Prozentwert oder normale Fließkommazahl
	 * @param time	Als Zeitangabe oder als Zahl
	 * @param digits	Anzahl an Nachkommastelle für Zahlen und Prozentwerte (Werte &lt;0 für maximale Anzahl)
	 * @param distributionSeparator	Trenner für die Einträge bei Verteilungen
	 * @return	Gibt ein String-Array aus zwei Elementen zurück. Im ersten Eintrag wird ein Fehler und im zweiten ein Wert zurückgegeben. Genau einer der beiden Einträge ist immer <code>null</code>.
	 */
	public static String[] findElement(final Scanner selectors, final Element parent, final List<String> parentTags, final boolean systemNumbers, final boolean percent, final boolean time, final int digits, final char distributionSeparator) {
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
			final List<String> path=new ArrayList<>(parentTags);
			path.add(attr);
			return new String[]{null,formatNumber(parent.getAttribute(attr),path,systemNumbers,percent,time,digits,distributionSeparator)};
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
			final List<String> path=new ArrayList<>(parentTags);
			path.add(tag);
			if (attr.isEmpty() || !attrValue.isEmpty()) {
				return new String[]{null,formatNumber(searchResult.getTextContent(),path,systemNumbers,percent,time,digits,distributionSeparator)};
			} else {
				path.add(attr);
				return new String[]{null,formatNumber(searchResult.getAttribute(attr),path,systemNumbers,percent,time,digits,distributionSeparator)};
			}
		}

		/* Suche fortsetzen */
		final List<String> tags=new ArrayList<>(parentTags);
		tags.add(tag);
		return findElement(selectors,searchResult,tags,systemNumbers,percent,time,digits,distributionSeparator);
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
		if (path==null || path.isEmpty()) return false;
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
	 * @param digits	Anzahl an Nachkommastelle für Zahlen und Prozentwerte (Werte &lt;0 für maximale Anzahl)
	 * @param distributionSeparator	Zu verwendendes Trennzeichen bei Verteilungen
	 * @return	Formatierter Zahlenwert
	 */
	public static String formatNumber(String value, List<String> path, boolean systemNumbers, boolean percent, boolean time, final int digits, char distributionSeparator) {
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
			if (systemNumbers) return NumberTools.formatSystemNumber(D)+suffix; else {
				if (digits<0 || digits>14) {
					return NumberTools.formatNumberMax(D)+suffix;
				} else {
					return NumberTools.formatNumber(D,digits)+suffix;
				}
			}
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
			return findElement(selectors,xml.getDocumentElement(),new ArrayList<>(),systemNumbers,percent,time,-1,distributionSeparator);
		}
	}

	/**
	 * Stellt das Ausgabeformat für Zahlen ein
	 * @param obj	Zeichenkette, über die das Format (z.B. Dezimalkomma sowie optional Prozentwert) für Zahlenausgaben festgelegt wird
	 */
	public void setFormat(final Object obj) {
		if (!(obj instanceof String)) return;
		final String parameter=(String)obj;

		if (parameter.equalsIgnoreCase("lokal") || parameter.equalsIgnoreCase("local")) {systemNumbers=false; return;}
		if (parameter.equalsIgnoreCase("system")) {systemNumbers=true; return;}
		if (parameter.equalsIgnoreCase("fraction") || parameter.equalsIgnoreCase("bruch")) {time=false; percent=false; return;}
		if (parameter.equalsIgnoreCase("percent") || parameter.equalsIgnoreCase("prozent")) {time=false; percent=true; return;}
		if (parameter.equalsIgnoreCase("time") || parameter.equalsIgnoreCase("zeit")) {time=true; return;}
		if (parameter.equalsIgnoreCase("number") || parameter.equalsIgnoreCase("zahl")) {time=false; return;}

		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+parameter+")");
	}

	/**
	 * Stellt ein, welches Trennzeichen zwischen den Werten bei der Ausgabe von Verteilungen verwendet werden soll
	 * @param obj	Bezeichner für das Trennzeichen
	 */
	public void setSeparator(final Object obj) {
		if (!(obj instanceof String)) return;
		final String parameter=(String)obj;

		if (parameter.equalsIgnoreCase("semikolon") || parameter.equalsIgnoreCase("semicolon")) {separator=';'; return;}
		if (parameter.equalsIgnoreCase("line") || parameter.equalsIgnoreCase("lines") || parameter.equalsIgnoreCase("newline") || parameter.equalsIgnoreCase("zeilen") || parameter.equalsIgnoreCase("zeile")) {separator='\n'; return;}
		if (parameter.equalsIgnoreCase("tab") || parameter.equalsIgnoreCase("tabs") || parameter.equalsIgnoreCase("tabulator")) {separator='\t'; return;}

		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+parameter+")");
	}

	/**
	 * Versucht eine Zeichenkette in ein Verteilungsobjekt umzuwandeln
	 * @param value	Zeichenkette
	 * @return	Liefert im Erfolgsfall ein Verteilungsobjekt und im Fehlerfall eine Fehlermeldung
	 * @see #getDistribution(Object)
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
	 * @param obj	XML-Pfad
	 * @return	Liefert im Erfolgsfall ein Verteilungsobjekt und im Fehlerfall eine Fehlermeldung
	 */
	private Object getDistribution(final Object obj) {
		if (!(obj instanceof String)) return "";
		final String parameter=(String)obj;

		String[] result=findElement(parameter,true,false,false,';');
		if (result[0]!=null) return result[0];

		return getDistributionFromString(result[1]);
	}

	/**
	 * Gibt die Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist, als Array aus
	 * @param obj	String, der den XML-Pfad zu der Verteilung enthält
	 * @return	Werte der Verteilung als Array
	 */
	public Object xmlArray(final Object obj) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(obj);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).densityData;
	}

	/**
	 * Summiert die Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist auf und liefert das Ergebnis als Double-Wert
	 * @param obj	String, der den XML-Pfad zu der Verteilung enthält
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	public Object xmlSum(final Object obj) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(obj);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).sum();
	}

	/**
	 * Speichert das XML-Objekt in einer Datei
	 * @param obj	Dateiname
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean save(final Object obj) {
		if (!allowSave || canceled) return false;

		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable")+"\n");
			return false;
		}

		if (!(obj instanceof String)) return false;

		final File file=new File((String)obj);
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

	/**
	 * Speichert das XML-Objekt unter dem nächsten verfügbaren Dateinamen
	 * in dem angegebenen Verzeichnis
	 * @param obj	Verzeichnis, in dem die Dateien gespeichert werden sollen
	 * @return	Gibt <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 */
	public boolean saveNext(final Object obj) {
		if (!allowSave || canceled) return false;

		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable")+"\n");
			return false;
		}

		if (!(obj instanceof String)) return false;

		final File folder=new File((String)obj);
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

	/**
	 * Wendet das angegebene Javascript-Skript auf die Statistikdaten an und gibt das Ergebnis zurück.
	 * @param obj	Skriptdateiname
	 * @return	Rückgabewert des Skriptes
	 */
	public String filter(final String obj) {
		if (!allowSave || canceled) return "";

		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable")+"\n");
			return "";
		}

		if (!(obj instanceof String)) return "";

		final File file=new File(obj);
		if (!file.exists()) {
			addOutput(String.format(Language.tr("Statistics.Filter.ScriptDoesNotExist"),file.toString())+"\n");
			return "";
		}
		String[] lines;
		try {
			lines=Files.lines(file.toPath()).toArray(String[]::new);
		} catch (IOException e) {
			addOutput(String.format(Language.tr("Statistics.Filter.CouldNotLoadScript"),file.toString())+"\n");
			return "";
		}
		final String script=String.join("\n",lines);

		final JSRunDataFilter filter=new JSRunDataFilter(xml,xmlFile);
		filter.run(script);
		return filter.getResults();
	}

	/**
	 * Bildet den Mittelwert der Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist und liefert das Ergebnis als Double-Wert
	 * @param obj	String, der den XML-Pfad zu der Verteilung enthält
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	public Object xmlMean(final Object obj) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(obj);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).getMean();
	}

	/**
	 * Bildet den Standardabweichung der Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist und liefert das Ergebnis als Double-Wert
	 * @param obj	String, der den XML-Pfad zu der Verteilung enthält
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	public Object xmlSD(final Object obj) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(obj);
		if (dist instanceof String) return dist;

		return ((DataDistributionImpl)dist).getStandardDeviation();
	}

	/**
	 * Bildet den Variationskoeffizient der Werte der Verteilung, deren XML-Pfad im Parameter angegeben ist und liefert das Ergebnis als Double-Wert
	 * @param obj	String, der den XML-Pfad zu der Verteilung enthält
	 * @return	Summe der Verteilungselemente als Double oder im Fehlerfall eine Zeichenkette
	 */
	public Object xmlCV(final Object obj) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		final Object dist=getDistribution(obj);
		if (dist instanceof String) return dist;

		return DistributionTools.getCV((DataDistributionImpl)dist);
	}

	/**
	 * Liefert das Objekt, das über den als Parameter angegebenen XML-Pfad spezifiziert wird als Zeichenkette zurück
	 * @param obj	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @return	Datenobjekt als Zeichenkette
	 */
	public String xml(final Object obj) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		if (!(obj instanceof String)) return "";
		final String parameter=(String)obj;

		String[] result=findElement(parameter,systemNumbers,percent,time,separator);
		if (result[0]!=null) return result[0];
		if (result[1]!=null) return result[1];
		return "";
	}

	/**
	 * Liefert das Objekt, das über den als Parameter angegebenen XML-Pfad spezifiziert wird als Zahl zurück
	 * @param obj	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @return	Datenobjekt als Double-Wert oder ein String mit einer Fehlermeldung
	 */
	public Object xmlNumber(final Object obj) {
		if (xml==null) {
			addOutput(Language.tr("Statistics.Filter.NoStatisticsAvailable"));
			return null;
		}

		if (!(obj instanceof String)) return "";
		final String parameter=(String)obj;

		String[] result=findElement(parameter,true,false,false,separator);
		if (result[0]!=null) return result[0];
		if (result[1]!=null) {
			final Double D=NumberTools.getDouble(result[1]);
			if (D!=null) return D; else return result[1];
		}
		return "";
	}

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden Dateiausgaben nicht mehr ausgeführt.)
	 */
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
	 * @param language	Neue Sprache (muss "de" oder "en" sein)
	 * @return	Gibt an, ob das Übersetzen erfolgreich war
	 */
	public boolean translate(final String language) {
		final String languageLower=language.toLowerCase();
		if (!languageLower.equals("de") && !languageLower.equals("en")) return false;
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

	/**
	 * Liefert den vollständigen Pfad- und Dateinamen der Statistikdatei, aus der die Daten stammen.
	 * @return	Vollständiger Pfad- und Dateinamen der Statistikdatei (kann leer, aber nicht <code>null</code> sein)
	 */
	public String getStatisticsFile() {
		if (xmlFile==null) return "";
		return xmlFile.toString();
	}

	/**
	 * Liefert den Dateinamen der Statistikdatei, aus der die Daten stammen.
	 * @return	Dateiname der Statistikdatei (kann leer, aber nicht <code>null</code> sein)
	 */
	public String getStatisticsFileName() {
		if (xmlFile==null) return "";
		return xmlFile.getName();
	}
}