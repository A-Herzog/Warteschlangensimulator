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
import java.util.List;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Element;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;

/**
 * Statistik-Klasse, die einen Wert wie z.B. eine Warteschlangenlänge (aber nicht notwendig diskret),
 * der sich über die Zeit ändert, stückweise aggregiert erfasst.<br>
 * Die Zählung wird über die Methode {@link StatisticsLongRunPerformanceIndicator#init(long, Mode)} initialisiert.
 * Danach werden über die Methode {@link StatisticsLongRunPerformanceIndicator#set(long, double)} Änderungen
 * des Wertes mitgeteilt.<br>
 * Sollen alle Werte (z.B. einzelne Wartezeiten) einer Messreihe erfasst werden, so kann dafür die
 * {@link StatisticsDataCollector}-Klasse verwendet werden, die allerdings sehr speicherintensiv ist.
 * @author Alexander Herzog
 * @version 2.0
 */
public final class StatisticsLongRunPerformanceIndicator extends StatisticsPerformanceIndicator implements Cloneable {
	/** Fehlermeldung, wenn der Inhalt des XML-Elements nicht gelesen werden konnte. */
	public static String xmlLoadError="Die in dem Element \"%s\" angegebene Verteilung ist ungültig.";
	/** XML-Attribut für "Schrittweite" */
	public static String[] xmlNameStep=new String[] {"Schrittweite"};
	/** Fehlermeldung, wenn das "Schrittweite"-Attribut nicht gelesen werden konnte. */
	public static String xmlLoadStepWideError="Die in dem Element \"%s\" angegebene Schrittweite ist ungültig.";

	/**
	 * Initiale Größe des Erfassungs-Arrays und gleichzeitig auch Wert, um den
	 * das Array bei jeder notwendigen Vergrößerung verlängert wird.
	 */
	private static final int initialValues=1_000;

	/**
	 * Maximale Anzahl an Werten, die erfasst werden.
	 * Dies stellt sicher, dass es nicht zu Out-of-memory-Problemen kommt.
	 */
	private static final int maxValues=2_000_000;

	/**
	 * @see StatisticsLongRunPerformanceIndicator#init(long, Mode)
	 */
	public enum Mode {
		/** Erfassung des Mittelwerts pro Intervall */
		MODE_AVERAGE,

		/** Erfassung des Minimums pro Intervall */
		MODE_MIN,

		/** Erfassung des Maximums pro Intervall */
		MODE_MAX
	}

	private double[] data;
	private int dataUsed;
	private long step;
	private Mode mode;
	private long lastTime;
	private double lastSum;
	private double lastValue;
	private long timeDelta;

	/**
	 * Konstruktor der Klasse <code>StatisticsLongRunPerformanceIndicator</code>
	 * @param xmlNodeNames	Name des xml-Knotens, in dem die Daten gespeichert werden sollen
	 */
	public StatisticsLongRunPerformanceIndicator(final String[] xmlNodeNames) {
		super(xmlNodeNames);
		data=new double[initialValues];
		dataUsed=0;
		reset();
	}

	@Override
	public void add(final StatisticsPerformanceIndicator moreStatistics) {
		if (!(moreStatistics instanceof StatisticsLongRunPerformanceIndicator)) return;
		StatisticsLongRunPerformanceIndicator moreLongRunStatistics=(StatisticsLongRunPerformanceIndicator)moreStatistics;

		if (moreLongRunStatistics.dataUsed>dataUsed) {
			data=Arrays.copyOf(moreLongRunStatistics.data,moreLongRunStatistics.data.length);
			dataUsed=moreLongRunStatistics.dataUsed;
			step=moreLongRunStatistics.step;
			mode=moreLongRunStatistics.mode;
		}
	}

	/**
	 * Trägt direkt Daten in das Statistikobjekt ein (und überschreibt dabei ggf. vorhandene Daten)
	 * @param data	Neue Daten
	 */
	public void setData(final double[] data) {
		if (data==null || data.length==0) {
			reset();
		} else {
			this.data=Arrays.copyOf(data,data.length);
			dataUsed=data.length;
		}
	}

	/**
	 * Setzt alle Teil-Kenngrößen auf 0 zurück.
	 */
	@Override
	public void reset() {
		dataUsed=0;
		lastTime=-1;
	}

	/**
	 * Stellt den Startwert ein, ab dem die Zeit gezählt werden soll.
	 * Sollte direkt nach {@link StatisticsLongRunPerformanceIndicator#reset()} aufgerufen werden.
	 * @param time	Startzeitpunkt
	 */
	public void setTime(final long time) {
		timeDelta=time;
	}

	/**
	 * Initialisiert das Statistik-Objekt
	 * @param step	Schrittweite für ein Datenfeld
	 * @param mode	Gibt ab, ob der Minimal-, Maximal- oder Durchschnittswert in dem Intervall erfasst werden soll
	 * @see Mode#MODE_MIN
	 * @see Mode#MODE_MAX
	 * @see Mode#MODE_AVERAGE
	 */
	public void init(final long step, final Mode mode) {
		reset();
		this.step=step;
		this.mode=mode;
	}

	private void dataAdd(final double value) {
		if (dataUsed==data.length) {
			if (dataUsed>=maxValues) return; /* Ende, wenn zu viel Speicher belegt wird */
			data=Arrays.copyOf(data,data.length+initialValues);
		}
		data[dataUsed++]=value;
	}

	private void processEndOfStep() {
		if (lastTime<0) {
			/* noch überhaupt kein Wert gesetzt */
			dataAdd(0.0);
			return;
		}

		final long stepStart=step*dataUsed;
		final long stepEnd=step*(dataUsed+1);

		if (lastTime<=stepStart) {
			/* Wert wurde in dem Intervall überhaupt nicht geändert */
			dataAdd(lastValue);
		} else {
			double value=0;
			switch (mode) {
			case MODE_AVERAGE:
				value=lastSum+(lastValue*(stepEnd-lastTime))/step;
				break;
			case MODE_MIN:
			case MODE_MAX:
				value=lastSum;
				break;
			}
			dataAdd(value);
		}
	}

	/**
	 * Gibt an, dass zu einem bestimmten Zeitpunkt ein bestimmter Wert vorherrscht
	 * @param time	Zeitpunkt
	 * @param value	Wert
	 */
	public void set(final long time, final double value) {
		if (dataUsed>=maxValues) return; /* Ende, wenn zu viel Speicher belegt wird */

		final long realTime=time-timeDelta;

		final long stepNr=realTime/step;

		while (stepNr>dataUsed) {
			if (dataUsed>=maxValues) return; /* Ende, wenn zu viel Speicher belegt wird */
			processEndOfStep();
		}

		long stepStart=step*dataUsed;

		switch (mode) {
		case MODE_AVERAGE:
			if (lastTime<stepStart) {
				lastSum=(lastValue*(realTime-stepStart))/step;
			} else {
				lastSum=lastSum+(lastValue*(realTime-lastTime))/step;
			}
			lastValue=value;
			break;
		case MODE_MIN:
			if (lastTime<stepStart) {
				lastSum=FastMath.min(lastValue,value);
			} else {
				lastSum=FastMath.min(lastSum,value);
			}
			lastValue=value;
			break;
		case MODE_MAX:
			if (lastTime<stepStart) {
				lastSum=FastMath.max(lastValue,value);
			} else {
				lastSum=FastMath.max(lastSum,value);
			}
			lastValue=value;
			break;
		}

		lastTime=realTime;
	}

	/**
	 * Liefert die Verteilung zurück, wie lange sich das System in welchem Zustand befinden hat
	 * @return	Zeit-Verteilung über die Systemzustände
	 */
	public DataDistributionImpl getDistribution() {
		if (data==null) return new DataDistributionImpl(0,new double[0]);
		return new DataDistributionImpl(dataUsed*step/1000.0,Arrays.copyOf(data,dataUsed),true);
	}

	/**
	 * Gibt an, wie viele Werte bisher aufgezeichnet wurden.
	 * @return	Anzahl an aufgezeichneten Werten
	 * @see StatisticsLongRunPerformanceIndicator#getValue(int)
	 */
	public int getValueCount() {
		return dataUsed;
	}

	/**
	 * Liefert einen der aufgezeichneten Werte
	 * @param index	0-basierender Index des Wertes
	 * @return	Wert in dem Intervall
	 * @see StatisticsLongRunPerformanceIndicator#getValueCount()
	 */
	public double getValue(final int index) {
		if (index<0 || index>=dataUsed) return 0.0;
		return data[index];
	}

	/**
	 * Liefert die aufgezeichneten Werte.<br>
	 * Es wird eine Kopie der internen Daten geliefert.
	 * @return	Aufgezeichnete Werte
	 */
	public double[] getValues() {
		if (data==null) return new double[0];
		return Arrays.copyOf(data,dataUsed);
	}

	/**
	 * Liefert den maximalen aufgezeichneten Wert
	 * @return	Maximaler aufgezeichneter Wert
	 */
	public double getMax() {
		if (dataUsed==0) return 0;
		double max=data[0];
		for (int i=1;i<dataUsed;i++) max=Math.max(max,data[i]);
		return max;
	}

	/**
	 * Liefert den minimalen aufgezeichneten Wert
	 * @return	minimaler aufgezeichneter Wert
	 */
	public double getMin() {
		if (dataUsed==0) return 0;
		double min=data[0];
		for (int i=1;i<dataUsed;i++) min=Math.min(min,data[i]);
		return min;
	}

	/**
	 * Kopiert die Daten eines anderen Statistik-Objektes in dieses
	 * @param indicator	Objekt, aus dem die Daten kopiert werden sollen
	 */
	@Override
	protected void copyDataFrom(final StatisticsPerformanceIndicator indicator) {
		if (!(indicator instanceof StatisticsLongRunPerformanceIndicator)) return;
		data=Arrays.copyOf(((StatisticsLongRunPerformanceIndicator)indicator).data,((StatisticsLongRunPerformanceIndicator)indicator).data.length);
		dataUsed=((StatisticsLongRunPerformanceIndicator)indicator).dataUsed;
		step=((StatisticsLongRunPerformanceIndicator)indicator).step;
		mode=((StatisticsLongRunPerformanceIndicator)indicator).mode;
		lastTime=-1;
	}

	/**
	 * Legt eine Kopie des Objekts an.
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsLongRunPerformanceIndicator clone() {
		final StatisticsLongRunPerformanceIndicator indicator=new StatisticsLongRunPerformanceIndicator(xmlNodeNames);
		indicator.copyDataFrom(this);
		return indicator;
	}

	/**
	 * Legt eine Kopie des Objekts an, übernimmt aber keine Daten
	 * (da das Ausgangsobjekt noch leer ist).
	 * @return Kopie des Statistik-Objektes
	 */
	@Override
	public StatisticsLongRunPerformanceIndicator cloneEmpty() {
		return new StatisticsLongRunPerformanceIndicator(xmlNodeNames);
	}

	@Override
	protected void addToXMLIntern(Element node) {
		node.setTextContent(getDistribution().storeToString());
		if (step>0) node.setAttribute(xmlNameStep[0],NumberTools.formatSystemNumber(step/1000.0));
	}

	@Override
	public String loadFromXML(Element node) {
		DataDistributionImpl dist=DataDistributionImpl.createFromString(node.getTextContent(),1000);
		if (dist==null) return String.format(xmlLoadError,node.getNodeName());
		dataUsed=0;
		for (double d: dist.densityData) dataAdd(d);

		for (int i=0;i<xmlNameStep.length;i++) {
			String s=node.getAttribute(xmlNameStep[i]);
			if (!s.isEmpty()) {
				Double D=NumberTools.getNotNegativeDouble(NumberTools.systemNumberToLocalNumber(s));
				if (D==null) return String.format(xmlLoadStepWideError,node.getNodeName());
				step=Math.round(D*1000);
				break;
			}
		}

		return null;
	}

	/**
	 * Summiert die Werte der verschiedenen übergebenen Zeitreihen auf und liefert die Gesamtzeitreihe zurück.
	 * @param indicators	Teil-Zeitreihen
	 * @return	Werte der elementweise addierten Zeitenreihen
	 */
	public static double[] sum(final List<StatisticsLongRunPerformanceIndicator> indicators) {
		double[] result=new double[0];
		if (indicators!=null) for (StatisticsLongRunPerformanceIndicator indicator: indicators) {
			final double[] values=indicator.getValues();
			if (values.length>result.length) result=Arrays.copyOf(result,values.length);
			for (int i=0;i<values.length;i++) result[i]+=values[i];
		}

		return result;
	}

	/**
	 * Summiert die Werte der verschiedenen übergebenen Zeitreihen auf und liefert die Gesamtzeitreihe zurück.
	 * @param indicators	Teil-Zeitreihen
	 * @return	Werte der elementweise addierten Zeitenreihen
	 */
	public static double[] sum(final StatisticsLongRunPerformanceIndicator[] indicators) {
		double[] result=new double[0];
		if (indicators!=null) for (StatisticsLongRunPerformanceIndicator indicator: indicators) {
			final double[] values=indicator.getValues();
			if (values.length>result.length) result=Arrays.copyOf(result,values.length);
			for (int i=0;i<values.length;i++) result[i]+=values[i];
		}

		return result;
	}

	/**
	 * Summiert die Werte der verschiedenen übergebenen Zeitreihen auf und liefert die Gesamtzeitreihe zurück.
	 * @param multi	Teil-Zeitreihen
	 * @return	Werte der elementweise addierten Zeitenreihen
	 */
	public static double[] sum(final StatisticsMultiPerformanceIndicator multi) {
		return sum(multi.getAll(StatisticsLongRunPerformanceIndicator.class));
	}
}
