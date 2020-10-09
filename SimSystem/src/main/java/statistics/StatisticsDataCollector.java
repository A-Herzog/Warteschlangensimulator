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

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

import org.w3c.dom.Element;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Statistik-Klasse, die alle Werte eine Messreihe aufzeichnet.<br>
 * Die anderen Statistik-Klassen erfassen nur aggregierte Werte. Diese Klasse hingegen hält
 * alle Werte selbst vor (was ggf. sehr speicherintensiv werden kann).<br>
 * Die Zählung wird über die Funktion {@link StatisticsDataCollector#add(double)} realisiert.<br>
 * Wenn nicht einzelne Werte (wie z.B. Wartezeiten) erfasst werden sollen, sondern nur der Verlauf einer
 * Größe (wie z.B. der Warteschlangenlänge) kann die {@link StatisticsLongRunPerformanceIndicator}-Klasse
 * verwendet werden, die einen Wert stückweise aggregiert erfasst.
 * @author Alexander Herzog
 * @version 1.2
 */
public final class StatisticsDataCollector extends StatisticsPerformanceIndicator implements Cloneable {
	/**
	 * Fehlermeldung, die zurückgeliefert wird, wenn ein Element nicht die eigentlich erwartete Liste von Zahlenwerten enthält.
	 * @see #loadFromXML(Element)
	 */
	public static String xmlDistributionError="Das Element \"%s\" muss eine Liste von Zahlenwerten enthalten.";

	/**
	 * Schrittweite mit der {@link #data} bei Bedarf vergrößert wird.
	 */
	private static final int GROW_SIZE=10_000;

	/**
	 * Anzahl der tatsächlich in {@link #data} belegten Einträge.
	 */
	private int count;

	/**
	 * Erfasste Datenpunkte.
	 */
	private double[] data;

	/**
	 * Anzahl an Messwerten auf die sich die anderen Cache-Werte
	 * beziehen. Wenn {@link #count} immer noch {@link #cacheCount}
	 * entspricht, können {@link #cacheSum} usw. direkt verwendet werden.
	 */
	private int cacheCount;

	/**
	 * Berechnete Summe, um diese nicht mehrfach berechnen zu müssen.
	 * @see #getSum()
	 * @see #cacheCount
	 */
	private double cacheSum;

	/**
	 * Berechneter Mittelwert, um diesen nicht mehrfach berechnen zu müssen.
	 * @see #getMean()
	 * @see #cacheCount
	 */
	private double cacheMean;

	/**
	 * Startwert für Teilsumme, um die Berechnung der Teilsumme nicht mehrfach berechnen zu müssen.
	 * @see #getSum(int, int)
	 * @see #cacheCount
	 * @see #cachePartialSum
	 */
	private int cachePartialA;

	/**
	 * Endwert für Teilsumme, um die Berechnung der Teilsumme nicht mehrfach berechnen zu müssen.
	 * @see #getSum(int, int)
	 * @see #cacheCount
	 * @see #cachePartialSum
	 */
	private int cachePartialB;

	/**
	 * Teilsumme, um die Berechnung der Teilsumme nicht mehrfach berechnen zu müssen.
	 * @see #getSum(int, int)
	 * @see #cacheCount
	 */
	private double cachePartialSum;

	/**
	 * Konstruktor der Klasse <code>StatisticsDataCollector</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsDataCollector(String[] xmlNodeNames) {
		super(xmlNodeNames);
	}

	/**
	 * Konstruktor der Klasse <code>StatisticsDataCollector</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param data	Messwerte, die direkt in die Klasse eingetragen werden sollen
	 */
	public StatisticsDataCollector(String[] xmlNodeNames, final double[] data) {
		super(xmlNodeNames);
		this.data=data;
		count=data.length;
	}

	/**
	 * Fügt einen Wert zu der Messreihe hinzu
	 * @param value	Hinzuzufügender Wert
	 */
	public void add(final double value) {
		if (data==null) data=new double[GROW_SIZE];
		if (data.length==count) data=Arrays.copyOf(data,data.length+GROW_SIZE);
		data[count]=value;
		count++;
	}

	/**
	 * Fügt zu der Teil-Messreihe eine weitere Teilmessreihe hinzu
	 * @param moreStatistics	Statistikobjekt, dessen Daten zu diesem hinzugefügt werden sollen
	 */
	@Override
	public void add(StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsDataCollector)) return;
		final StatisticsDataCollector moreData=(StatisticsDataCollector)moreStatistics;

		if (moreData.data!=null) {
			if (data==null) {
				data=Arrays.copyOf(moreData.data,moreData.data.length);
			} else {
				data=Arrays.copyOf(data,data.length+moreData.data.length);
				for (int i=0;i<moreData.data.length;i++) data[count+i]=moreData.data[i];
			}
		}
		count+=moreData.count;

		cacheCount=-1;
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		count=0;
		data=null;
		cacheCount=-1;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsDataCollector)) return;
		final StatisticsDataCollector moreData=(StatisticsDataCollector)indicator;

		if (moreData.data!=null) data=Arrays.copyOf(moreData.data,moreData.data.length);
		count=moreData.count;

		cacheCount=-1;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataCollector clone() {
		final StatisticsDataCollector indicator=new StatisticsDataCollector(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsDataCollector cloneEmpty() {
		return clone();
	}

	/**
	 * Liefert die Anzahl der Messwerte, aus der die Messreihe besteht
	 * @return	Anzahl der erfassten Messwerte in der Messreihe
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Liefert alle aufgezeichneten Werte
	 * @return	Alle aufgezeichneten Werte
	 */
	public double[] getValues() {
		if (data==null) return new double[0];
		return Arrays.copyOf(data,count);
	}

	/**
	 * Liefert ein Array mit allen aufgezeichneten Werte, die jedoch um den Mittelwert nach unten verschoben wurden
	 * @return	Array aus verschobenen Werten
	 */
	public double[] getCenteredValues() {
		if (data==null) return new double[0];
		final double[] result=Arrays.copyOf(data,count);
		final double mean=getMean();
		for (int i=0;i<result.length;i++) result[i]-=mean;
		return result;
	}

	/**
	 *  Liefert einen einzelnen Wert
	 * @param index	Index des Wertes (0 bis <code>getCount()-1</code>
	 * @return	Einzelner Aufgezeichneter Wert
	 */
	public double getValue(int index) {
		if (data==null) return 0;
		if (index<0 || index>=count) return 0;
		return data[index];
	}

	/**
	 * Liefert alle aufgezeichneten Werte als Read-Only-Arrays
	 * @return	Alle aufgezeichneten Werte (Achtung: Array ist größer, als Anzahl an Werten. <code>getCount()</code> muss verwendet werden.)
	 */
	public double[] getValuesReadOnly() {
		if (data==null) return new double[0];
		return data;
	}

	/**
	 * Liefert die Summe alle Messwerte, aus der die Messreihe besteht
	 * @return	Summe aller Messwerte
	 */
	public double getSum() {
		if (cacheCount==count) return cacheSum;

		cacheCount=count;
		cachePartialA=-1;
		cachePartialB=-1;
		cacheSum=0;
		for (int i=0;i<count;i++) cacheSum+=data[i];
		cacheMean=cacheSum/Math.max(1,count);

		return cacheSum;
	}

	/**
	 * Liefert eine Teilsumme der Messwerte, aus der die Messreihe besteht
	 * @param from	Erster 0-basierender Index in der Summe (kann minimal 0 sein)
	 * @param to	Letzter 0-basierender Index in der Summe (kann maximal <code>getCount()-1</code> sein)
	 * @return	Teilsumme der Messwerte
	 */
	public double getSum(final int from, final int to) {
		final int a=Math.max(0,from);
		final int b=Math.min(count-1,to);

		if (cacheCount==count) {
			if (cachePartialA==a && cachePartialB==b) return cachePartialSum;
			if (cachePartialA==a && cachePartialB+1==b) {
				cachePartialSum+=data[b];
				cachePartialB=b;
				return cachePartialSum;
			}
		} else {
			getSum();
		}

		if (data==null) return 0;
		double sum=0;
		for (int i=a;i<=b;i++) sum+=data[i];

		cachePartialA=a;
		cachePartialB=b;
		cachePartialSum=sum;

		return sum;
	}

	/**
	 * Liefert eine gewichtete Summe über alle Messwerte, aus der die Messreihe besteht
	 * @param weight	Gewichtungsfunktion (Index-Parameter enthält 0-basierende Indices der Einträge der Summe)
	 * @return	Gewichtete Summe aller Messwerte
	 */
	public double getWeightedSum(final IntToDoubleFunction weight) {
		if (data==null || weight==null) return 0;
		double sum=0;
		for (int i=0;i<count;i++) {
			sum+=weight.applyAsDouble(i)*data[i];
		}
		return sum;
	}

	/**
	 * Liefert eine gewichtete Summe über einen Teil der Messwerte, aus der die Messreihe besteht
	 * @param from	Erster 0-basierender Index in der Summe (kann minimal 0 sein)
	 * @param to	Letzter 0-basierender Index in der Summe (kann maximal <code>getCount()-1</code> sein)
	 * @param weight	Gewichtungsfunktion (Index-Parameter enthält 0-basierende Indices der Einträge der Summe)
	 * @return	Gewichtete Summe der gewählten Messwerte
	 */
	public double getWeightedSum(final int from, final int to, final IntToDoubleFunction weight) {
		if (data==null || weight==null) return 0;

		final int a=Math.max(from,0);
		final int b=Math.min(to,count-1);

		double sum=0;
		for (int i=a;i<=b;i++) {
			sum+=weight.applyAsDouble(i)*data[i];
		}
		return sum;
	}

	/**
	 * Liefert eine Summe über alle Messwerte, wobei auf jeden Messwert vor der Summation noch eine Funktion angewandt wird.
	 * @param function	Anzuwendende Funktion
	 * @return	Summe der verarbeiteten Messwerte
	 * @see ValueProcessorFunction
	 */
	public double getProcessedSum(final ValueProcessorFunction function) {
		if (data==null || function==null) return 0;
		double sum=0;
		for (int i=0;i<count;i++) {
			sum+=function.process(i,data[i]);
		}
		return sum;
	}

	/**
	 * Liefert eine Summe über über einen Teil der Messwerte, wobei auf jeden Messwert vor der Summation noch eine Funktion angewandt wird.
	 * @param from	Erster 0-basierender Index in der Summe (kann minimal 0 sein)
	 * @param to	Letzter 0-basierender Index in der Summe (kann maximal <code>getCount()-1</code> sein)
	 * @param function	Anzuwendende Funktion
	 * @return	Summe der verarbeiteten Messwerte
	 * @see ValueProcessorFunction
	 */
	public double getProcessedSum(final int from, final int to, final ValueProcessorFunction function) {
		if (data==null || function==null) return 0;

		final int a=Math.max(from,0);
		final int b=Math.min(to,count-1);

		double sum=0;
		for (int i=a;i<=b;i++) {
			sum+=function.process(i,data[i]);
		}
		return sum;
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen den Mittelwert
	 * @return	Mittelwert der Messreihe
	 */
	public double getMean() {
		if (cacheCount==count) return cacheMean;

		cacheCount=count;
		cacheSum=getSum(0,count-1);
		cacheMean=cacheSum/Math.max(1,count);

		return cacheMean;
	}

	/**
	 * Berechnet aus den Messreihen-Kenngrößen einen Mittelwert über einen Teil der Werte
	 * @param from	Erster 0-basierender Index, der für die Mittelwertberechnung herangezogen werden soll (kann minimal 0 sein)
	 * @param to	Letzter 0-basierender Index, der für die Mittelwertberechnung herangezogen werden soll (kann maximal <code>getCount()-1</code> sein)
	 * @return	Mittelwert der Teilmessreihe
	 */
	public double getMean(final int from, final int to) {
		final int a=Math.max(from,0);
		final int b=Math.min(to,count-1);
		return getSum(a,b)/Math.max(1,b-a+1);
	}

	/**
	 * Liefert ein neues {@link StatisticsDataCollector}-Objekt,
	 * welches einen Teil der Messwerte enthält
	 * @param from	Erster Messwert, der in das neue Objekt übernommen werden soll (0 oder größer)
	 * @param to	Letzter Messwert, der in das neue Objekt übernommen werden soll (<code>getCount()-1</code> oder kleiner)
	 * @return	Neues {@link StatisticsDataCollector}-Objekt, welches nur einen Teil der Messwerte enthält
	 */
	public StatisticsDataCollector getPart(final int from, final int to) {
		int a=Math.max(from,0);
		int b=Math.min(to,count-1);
		if (a>b) return new StatisticsDataCollector(xmlNodeNames);

		double[] part=new double[b-a+1];
		for (int i=a;i<=b;i++) part[i-a]=data[i];
		return new StatisticsDataCollector(xmlNodeNames,part);
	}

	/**
	 * Speichert eine Kenngröße in einem xml-Knoten.
	 * @param node	Neuer xml-Knotens, in dem die Daten gespeichert werden sollen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 */
	@Override
	protected void addToXMLIntern(final Element node, final StringBuilder recycleStringBuilder) {
		if (data==null) return;
		final StringBuilder sb;
		if (recycleStringBuilder==null) {
			sb=new StringBuilder();
		} else {
			sb=recycleStringBuilder;
			sb.setLength(0);
		}
		final StringBuilder reuseSB=new StringBuilder();
		for (int i=0;i<count;i++) {
			if (i>0) sb.append(";");
			sb.append(NumberTools.formatSystemNumber(data[i],reuseSB));
		}
		node.setTextContent(sb.toString());
	}

	/**
	 * Versucht eine Kenngröße, die intern durch die Anzahl an Messwerten, deren Summe und deren quadrierte Summe repräsentiert wird, aus einem xml-Knoten zu laden
	 * @param node	xml-Knoten, aus dem die Daten geladen werden sollen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	@Override
	public String loadFromXML(Element node) {
		final DataDistributionImpl dist=DataDistributionImpl.createFromString(node.getTextContent(),1000);
		if (dist==null) return String.format(xmlDistributionError,node.getNodeName());
		data=dist.densityData;
		count=data.length;
		return null;
	}
}