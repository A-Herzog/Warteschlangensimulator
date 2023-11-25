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
package statistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Statistik-Klasse, die es ermöglicht allgemeine Informationen zur Simulation zu erfassen.<br>
 * Die Erfassung erfolgt im Gegensatz zu den anderen Statistik-Klassen nicht durch den Aufruf
 * einer Zähl-Methode, sondern durch das manuelle Setzen von öffentlichen Feldern.
 * @author Alexander Herzog
 * @version 1.2
 */
public final class StatisticsSimulationBaseData extends StatisticsPerformanceIndicator implements Cloneable {
	/** XML-Attribut für "StatistikLaufdatum" */
	public static String[] xmlNameRunDate=new String[]{"StatistikLaufdatum"};
	/** XML-Attribut für "StatistikLaufzeit" (=Dauer der Simulation) */
	public static String[] xmlNameRunTime=new String[]{"StatistikLaufzeit"};
	/** Fehlermeldung, wenn das "StatistikLaufzeit"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameRunTimeError="Der Wert in dem Element \"%s\" muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "StatistikSystem" */
	public static String[] xmlNameRunOS=new String[]{"StatistikSystem"};
	/** XML-Attribut für "StatistikNutzer" */
	public static String[] xmlNameRunUser=new String[]{"StatistikNutzer"};
	/** XML-Attribut für "StatistikThreads" */
	public static String[] xmlNameRunThreads=new String[]{"StatistikThreads"};
	/** XML-Attribut für "NUMAModus" */
	public static String[] xmlNameNUMA=new String[]{"NUMAModus"};
	/** XML-Attribut für "DynamischeThreadBalance" */
	public static String[] xmlNameDynamicBalance=new String[]{"DynamischeThreadBalance"};
	/** XML-Attribut für "DynamischeThreadBalanceDaten" */
	public static String[] xmlNameDynamicBalanceData=new String[]{"DynamischeThreadBalanceDaten"};
	/** XML-Attribut für "StatistikThreadLaufzeiten" */
	public static String[] xmlNameRunThreadTimes=new String[]{"StatistikThreadLaufzeiten"};
	/** Fehlermeldung, wenn das "StatistikThreadLaufzeiten"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameRunThreadsError="Der Wert in dem Element \"%s\" muss eine positive Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "StatistikEreignisse" */
	public static String[] xmlNameRunEvents=new String[]{"StatistikEreignisse"};
	/** Fehlermeldung, wenn das "StatistikEreignisse"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameRunEventsError="Der Wert in dem Element \"%s\" muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "StatistikWiederholungen" */
	public static String[] xmlNameRunRepeatCount=new String[]{"StatistikWiederholungen"};
	/** Fehlermeldung, wenn das "StatistikWiederholungen"-Attribut nicht gelesen werden konnte. */
	public static String xmlNameRunRepeatCountError="Der Wert in dem Element \"%s\" muss eine nicht-negative Ganzzahl sein, ist aber \"%s\".";
	/** XML-Attribut für "StatistikInternerAbbruch" */
	public static String[] xmlNameEmergencyShutDown=new String[]{"StatistikInternerAbbruch"};
	/** XML-Attribut für "StatistikModellWarnung" */
	public static String[] xmlNameWarning=new String[]{"StatistikModellWarnung"};

	/**
	 * Datum und Uhrzeit der Ausführung der Simulation
	 */
	public String runDate;

	/**
	 * Dauer der Simulation in ms.
	 */
	public long runTime;

	/**
	 * Informationen zu dem Computer auf dem die Simulation ausgeführt wurde.
	 */
	public String runOS;

	/**
	 * Informationen zu dem Nutzeraccount, der die Simulation initiiert hat.
	 */
	public String runUser;

	/**
	 * Anzahl der Threads, die für die Simulation eingesetzt wurden.
	 */
	public int runThreads;

	/**
	 * Wurde die Simulation im (speicherintensiveren) NUMA-Modus durchgeführt?
	 */
	public boolean numaAwareMode;

	/**
	 * Laufzeiten der einzelnen Threads
	 */
	public int[] threadRunTimes;

	/**
	 * Maximale relative Abweichung an simulierten Kunden pro Thread (bei der Verwendung einer dynamischen Thread-Balance)
	 */
	public double threadDynamicBalance;

	/**
	 * Anzahl an Kundenankünften pro Thread (bei der Verwendung einer dynamischen Thread-Balance)
	 */
	public long[] threadDynamicBalanceData;

	/**
	 * Anzahl der Ereignisse, die (in Summe über alle Threads) während der Simulation ausgeführt wurden.
	 */
	public long runEvents;

	/**
	 * Gibt an, wie oft die Simulation wiederholt wurde.
	 */
	public long runRepeatCount;

	/**
	 * Gibt an, ob die Simulation wegen eines internen Fehlers (z.B. explodierende Anzahl an Kunden im System) vorzeitig abgebrochen werden musste.
	 */
	public boolean emergencyShutDown;

	/**
	 * Bei der Modellerstellung aufgetretene Warnungen.
	 */
	public String[] warnings;

	/**
	 * Konstruktor der Klasse <code>StatisticsSimulationBaseData</code>
	 * @param xmlNodeName	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsSimulationBaseData(final String[] xmlNodeName) {
		super(xmlNodeName);
		reset();
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsSimulationBaseData)) return;
		StatisticsSimulationBaseData moreSimulationBaseDataStatistics=(StatisticsSimulationBaseData)moreStatistics;

		runEvents+=moreSimulationBaseDataStatistics.runEvents;
		emergencyShutDown=emergencyShutDown || moreSimulationBaseDataStatistics.emergencyShutDown;
		if (moreSimulationBaseDataStatistics.warnings!=null && moreSimulationBaseDataStatistics.warnings.length>0) {
			if (warnings==null) {
				warnings=Arrays.copyOf(moreSimulationBaseDataStatistics.warnings,moreSimulationBaseDataStatistics.warnings.length);
			} else {
				final List<String> warnings=new ArrayList<>(Arrays.asList(this.warnings));
				warnings.addAll(Arrays.asList(moreSimulationBaseDataStatistics.warnings));
				this.warnings=warnings.toArray(String[]::new);
			}
		}
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		final Calendar cal=Calendar.getInstance();
		final SimpleDateFormat sdf=new SimpleDateFormat();
		runDate=sdf.format(cal.getTime());
		runTime=0;
		runOS=System.getProperty("os.name")+" ("+System.getProperty("os.arch")+"), "+System.getProperty("java.vm.name")+" ("+System.getProperty("java.version")+")";
		runUser=System.getProperty("user.name");
		runThreads=0;
		numaAwareMode=false;
		runEvents=0;
		runRepeatCount=1;
		emergencyShutDown=false;
		warnings=null;
		threadRunTimes=new int[]{0};
		threadDynamicBalance=0.0;
		threadDynamicBalanceData=new long[]{0};
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsSimulationBaseData)) return;
		final StatisticsSimulationBaseData source=(StatisticsSimulationBaseData)indicator;
		runDate=source.runDate;
		runTime=source.runTime;
		runOS=source.runOS;
		runUser=source.runUser;
		runThreads=source.runThreads;
		numaAwareMode=numaAwareMode || source.numaAwareMode;
		runEvents=source.runEvents;
		runRepeatCount=source.runRepeatCount;
		emergencyShutDown=source.emergencyShutDown;
		if (source.warnings==null) {
			warnings=null;
		} else {
			warnings=Arrays.copyOf(source.warnings,source.warnings.length);
		}
		threadRunTimes=Arrays.copyOf(source.threadRunTimes,source.threadRunTimes.length);
		threadDynamicBalance=source.threadDynamicBalance;
		threadDynamicBalanceData=Arrays.copyOf(threadDynamicBalanceData,threadDynamicBalanceData.length);
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsSimulationBaseData clone() {
		final StatisticsSimulationBaseData indicator=new StatisticsSimulationBaseData(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsSimulationBaseData cloneEmpty() {
		return new StatisticsSimulationBaseData(xmlNodeNames);
	}

	/**
	 * Liefert die Anzahl an Ereignissen, die pro Sekunde ausgeführt wurden
	 * @return	Anzahl an Ereignissen pro Sekunde
	 */
	public double getEventsPerSec() {
		if (runTime==0) return 0;
		return ((double)runEvents)*1000/runTime;
	}

	/**
	 * Speichert eine Kenngröße, die intern aus Gesamtanzahl und Anzahl der erfolgreichen Ereignisse besteht, in einem xml-Knoten.
	 * Es wird dabei zusätzlich der Anteil an erfolgreichen Ereignissen berechnet und gespeichert
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		Element child;
		Document doc=node.getOwnerDocument();

		node.appendChild(child=doc.createElement(xmlNameRunDate[0]));
		child.setTextContent(runDate);

		node.appendChild(child=doc.createElement(xmlNameRunTime[0]));
		child.setTextContent(""+runTime);

		node.appendChild(child=doc.createElement(xmlNameRunOS[0]));
		child.setTextContent(runOS);

		node.appendChild(child=doc.createElement(xmlNameRunUser[0]));
		child.setTextContent(runUser);

		node.appendChild(child=doc.createElement(xmlNameRunThreads[0]));
		child.setTextContent(""+runThreads);
		if (numaAwareMode) child.setAttribute(xmlNameNUMA[0],"1");
		if (threadDynamicBalance>0) child.setAttribute(xmlNameDynamicBalance[0],NumberTools.formatSystemNumber(threadDynamicBalance));
		if (threadDynamicBalanceData.length>1 || threadDynamicBalanceData[0]!=0L) {
			final DataDistributionImpl dist=new DataDistributionImpl(threadDynamicBalanceData.length,threadDynamicBalanceData);
			child.setAttribute(xmlNameDynamicBalanceData[0],dist.storeToString());
		}

		if (threadRunTimes.length>1 || threadRunTimes[0]!=0) {
			node.appendChild(child=doc.createElement(xmlNameRunThreadTimes[0]));
			final DataDistributionImpl dist=new DataDistributionImpl(threadRunTimes.length,threadRunTimes);
			child.setTextContent(dist.storeToString());
		}

		node.appendChild(child=doc.createElement(xmlNameRunEvents[0]));
		child.setTextContent(""+runEvents);

		if (runRepeatCount>1) {
			node.appendChild(child=doc.createElement(xmlNameRunRepeatCount[0]));
			child.setTextContent(""+runRepeatCount);
		}

		if (emergencyShutDown) {
			node.appendChild(child=doc.createElement(xmlNameEmergencyShutDown[0]));
			child.setTextContent("1");
		}

		if (warnings!=null) for (String warning: warnings) {
			node.appendChild(child=doc.createElement(xmlNameWarning[0]));
			child.setTextContent(warning);
		}
	}

	/**
	 * Versucht eine Kenngröße, die intern durch die Gesamtanzahl die und Anzahl der erfolgreichen Ereignisse repräsentiert wird, aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(final Element node) {
		NodeList l=node.getChildNodes();
		for (int i=0; i<l.getLength();i++) {
			if (!(l.item(i) instanceof Element)) continue;
			Element e=(Element)l.item(i);
			String name=e.getNodeName();
			String text=e.getTextContent();

			if (multiCompare(name,xmlNameRunDate)) {runDate=text; continue;}
			if (multiCompare(name,xmlNameRunTime)) {
				Long L=NumberTools.getLong(text);
				if (L==null || L<0) return String.format(xmlNameRunTimeError,name,text);
				runTime=L;
				continue;
			}
			if (multiCompare(name,xmlNameRunOS)) {runOS=text; continue;}
			if (multiCompare(name,xmlNameRunUser)) {runUser=text; continue;}
			if (multiCompare(name,xmlNameRunThreads)) {
				Long L=NumberTools.getLong(text);
				if (L==null || L<=0) return String.format(xmlNameRunThreadsError,name,text);
				runThreads=(int)((long)L);
				for (String test: xmlNameNUMA) {
					final String attr=e.getAttribute(test);
					if (!attr.isEmpty() && !attr.equals("0")) {
						numaAwareMode=true;
						break;
					}
				}
				for (String test: xmlNameDynamicBalance) {
					final String attr=e.getAttribute(test);
					if (!attr.isEmpty()) {
						final Double D=NumberTools.getNotNegativeDouble(attr);
						if (D!=null) threadDynamicBalance=D.doubleValue();
						break;
					}
				}

				for (String test: xmlNameDynamicBalanceData) {
					final String attr=e.getAttribute(test);
					if (!attr.isEmpty()) {
						final DataDistributionImpl dist=DataDistributionImpl.createFromString(attr,1000);
						threadDynamicBalanceData=new long[dist.densityData.length];
						for (int j=0;j<dist.densityData.length;j++) threadDynamicBalanceData[j]=Math.round(dist.densityData[j]);
						break;
					}
				}

				continue;
			}
			if (multiCompare(name,xmlNameRunThreadTimes)) {
				final DataDistributionImpl dist=DataDistributionImpl.createFromString(text,1000);
				threadRunTimes=new int[dist.densityData.length];
				for (int j=0;j<dist.densityData.length;j++) threadRunTimes[j]=(int)Math.round(dist.densityData[j]);
				continue;
			}
			if (multiCompare(name,xmlNameRunEvents)) {
				Long L=NumberTools.getLong(text);
				if (L==null || L<0) return String.format(xmlNameRunEventsError,name,text);
				runEvents=L;
				continue;
			}
			if (multiCompare(name,xmlNameRunRepeatCount)) {
				Long L=NumberTools.getLong(text);
				if (L==null || L<1) return String.format(xmlNameRunRepeatCountError,name,text);
				runRepeatCount=L;
				continue;
			}
			if (multiCompare(name,xmlNameEmergencyShutDown)) {
				if (!text.isEmpty() && !text.equals("0")) emergencyShutDown=true;
			}
			if (multiCompare(name,xmlNameWarning)) {
				if (warnings==null || warnings.length==0) {
					warnings=new String[]{text};
				} else {
					warnings=Arrays.copyOf(warnings,warnings.length+1);
					warnings[warnings.length-1]=text;
				}
			}
		}

		return null;
	}

	/**
	 * Fügt eine Warnung zu der Liste der Warnungen hinzu.
	 * @param warning	Neue zusätzliche Warnung
	 * @see #warnings
	 */
	public void addWarning(final String warning) {
		if (warning==null || warning.isEmpty()) return;
		if (warnings==null) {
			warnings=new String[] {warning};
		} else {
			warnings=Arrays.copyOf(warnings,warnings.length+1);
			warnings[warnings.length-1]=warning;
		}
	}
}
