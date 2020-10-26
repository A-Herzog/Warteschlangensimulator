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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.DoubleStream;

import mathtools.NumberTools;
import simulator.statistics.Statistics;
import statistics.StatisticsDataCollector;

/**
 * Prüft, wie viele Werte als Einschwingphase für die
 * Statistik verworfen werden sollten.<br>
 * Verfahren basiert auf Ripley "Stochastic Simulation" (1987) und auf Schwindt "Simulation und Analyse von Produktionssystemen" (2017)
 * @author Alexander Herzog
 */
public class TestWarmUp {
	/** Aufgezeichnete Wartezeiten */
	private final StatisticsDataCollector data;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistik-Objekt, dem die aufgezeichneten Wartezeiten entnommen werden sollen
	 */
	public TestWarmUp(final Statistics statistics) {
		data=(statistics==null)?null:statistics.clientsAllWaitingTimesCollector;
	}

	/**
	 * Konstruktor der Klasse
	 * @param dataCollector	Aufgezeichnete Wartezeiten
	 */
	public TestWarmUp(final StatisticsDataCollector dataCollector) {
		data=dataCollector;
	}

	/**
	 * Liefert ein Datenobjekt zur Berechnung von tau
	 * @param data	Aufgezeichnete Wartezeiten
	 * @return	tau
	 * @see #getTauSquared(StatisticsDataCollector)
	 * @see #getTauSquaredClassic(StatisticsDataCollector)
	 */
	private StatisticsDataCollector getTauData(final StatisticsDataCollector data) {
		int m2=data.getCount()/2;
		if (data.getCount()%2!=0) m2++;
		return data.getPart(m2,data.getCount()-1);
	}

	/**
	 * Berechnung von tau^2
	 * @param data	Aufgezeichnete Wartezeiten (es wird nur die zweite Hälfte der Daten verwendet)
	 * @return	tau^2
	 */
	public double getTauSquared(final StatisticsDataCollector data) {
		final StatisticsDataCollector x=getTauData(data);
		final double m=x.getCount();
		final double mean=x.getMean();

		double sum=0.5*(m+1)*m*mean;
		sum+=x.getWeightedSum(i->-(i+1));

		return 12.0/m/(m*m-1)*sum*sum;
	}

	/**
	 * Berechnung von tau^2 über die alte Methode (langsamer, liefert dieselben Ergebnisse)
	 * @param data	Aufgezeichnete Wartezeiten (es wird nur die zweite Hälfte der Daten verwendet)
	 * @return	tau^2
	 */
	public double getTauSquaredClassic(final StatisticsDataCollector data) {
		final StatisticsDataCollector x=getTauData(data);
		final double m=x.getCount();
		final double mean=x.getMean();

		double sum=0;
		for (int k=1;k<=m;k++) sum+=(x.getSum(0,k-1)-k*mean);

		return 12.0/m/(m*m-1)*sum*sum;
	}

	/**
	 * Berechnet den Cramer-von-Mises-Wert
	 * @param lambda	Anteil der zu verwerfenden Werte (lambda=0, 0.02, ...)
	 * @param shiftTau	Soll bei der Berechnung von tau ebenfalls die Verwerfung von Werten verwendet werden
	 * @return	Wert der Cramer-von-Mises-Statistik (für CM&lt;0.74 sind genug Werte verworfen worden)
	 */
	public double getCM(final double lambda, final boolean shiftTau) {
		final StatisticsDataCollector x;
		if (lambda==0.0) {
			x=data;
		} else {
			x=data.getPart((int)Math.round(lambda*data.getCount()),data.getCount());
		}

		final double m=x.getCount();
		final double tauSquare=getTauSquared(shiftTau?x:data);

		if (tauSquare==0) return 0.0; /* Eigentlich Fehlerbedingung */

		final double[] centered=x.getCenteredValues();

		double sum=0;
		double Z=0;
		for (int k=1;k<=m-1;k++) { /* für k=0 und k=m-1 ist Z=0 */
			Z+=centered[k-1]; /* Alternative: double Z=data.getSum(0,k-1)-k*mean; */
			sum+=Z*Z/m/m/tauSquare;
		}
		return sum;
	}

	/**
	 * Berechnet den Anteil an Werte, die verworfen werden müssen, damit keine Einschwing-Effekte in der Statistik auftreten
	 * @param shiftTau	Soll bei der Berechnung von tau ebenfalls die Verwerfung von Werten verwendet werden
	 * @return	Anteil der zu verwerfenden Werte (oder 1, wenn ein Fehler aufgetreten ist)
	 */
	public double test(final boolean shiftTau) {
		if (data==null) return 0;

		for (int i=0;i<=600;i++) {
			final double lambda=0.001*i;
			final double CM=getCM(lambda,shiftTau);
			/* System.out.println(NumberTools.formatNumber(lambda,3)+" "+NumberTools.formatNumber(CM,3)); */
			if (CM<0.74) return lambda;
		}

		return 1; /* = Fehler, zu wenig Ankünfte für stabile Daten */
	}

	/**
	 * Überprüft die Rechenroutinen mit zwei Beispielen aus
	 * Schwindt "Simulation und Analyse von Produktionssystemen" (2017)
	 */
	public static void testExamples() {
		TestWarmUp test;

		test=new TestWarmUp(new StatisticsDataCollector(null,new double[]{1,2,3,4,5,6,6,5,6,5}));
		System.out.println(String.format("CM_ist=%s, CM_soll=5,57",NumberTools.formatNumber(test.getCM(0,false),3)));

		test=new TestWarmUp(new StatisticsDataCollector(null,new double[]{5,4,6,5,5,6,6,4,5,5}));
		System.out.println(String.format("CM_ist=%s, CM_soll=0,04",NumberTools.formatNumber(test.getCM(0,false),3)));
	}

	/**
	 * Lädt eine Datei, die Messwerte enthält und wendet in regelmäßigen
	 * Abständen auf die soweit geladenen Daten den Test an und gibt
	 * die Ergebnisse aus.
	 * @param file	Zu ladende Datei mit den Messwerten (jeweils ein Messwert pro Zeile)
	 */
	public static void testFile(final String file) {
		final StatisticsDataCollector data=new StatisticsDataCollector(null);
		final TestWarmUp test=new TestWarmUp(data);

		try (DoubleStream stream=Files.lines(Paths.get(file)).mapToDouble(s->NumberTools.getPlainDouble(s))) {
			stream.forEach(d->{
				data.add(d);
				if (data.getCount()%100_000==0) {
					final double tau=StrictMath.sqrt(test.getTauSquared(data));
					final double lambda1=test.test(false);
					final double lambda2=test.test(true);
					System.out.println(NumberTools.formatLong(data.getCount())+"\t"+NumberTools.formatNumber(tau)+"\t"+NumberTools.formatPercent(lambda1)+"\t"+NumberTools.formatPercent(lambda2));
				}
			});
		} catch (Exception e) {e.printStackTrace(); return;}
	}
}
