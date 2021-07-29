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
package mathtools.distribution;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import mathtools.NumberTools;

/**
 * Klasse zur Modellierung von Verteilungen, die in Form einer empirischen Dichte gegeben sind
 * @author Alexander Herzog
 * @version 1.5
 */
public final class DataDistributionImpl extends AbstractRealDistribution implements Serializable, Cloneable, DistributionWithRandom  {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2650001189317761476L;

	/**
	 * Gibt die Obergrenze des Trägerbereichs der Dichte an.
	 */
	public final double upperBound;

	/**
	 * Umrechnungsfaktor zwischen den Verteilungsschritten und dem Trägerbereich
	 * @see #upperBound
	 * @see #densityData
	 */
	private final double argumentScaleFactor;

	/**
	 * In diesem Array werden die einzelnen Werte der Dichte gespeichert.
	 * Die Summe der Werte muss dabei nicht 1 ergeben. Bei der Berechnung der
	 * Verteilungsfunktion per <code>updateCumulativeDensity</code> wird
	 * automatisch berücksichtigt, wenn sich die einzelnen Werte der Dichte nicht
	 * zu 1 aufsummieren.
	 * @see DataDistributionImpl#updateCumulativeDensity()
	 */
	public double[] densityData;

	/**
	 * Werte der Verteilungsfunktion. Diese Daten werden durch den Aufruf von
	 * <code>updateCumulativeDensity</code> aus <code>densityData</code> berechnet.
	 * @see DataDistributionImpl#updateCumulativeDensity()
	 * @see DataDistributionImpl#densityData
	 */
	public double[] cumulativeDensity;

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param steps	Gibt an, wie viele einzelne Werte für die Dichte vorgehalten werden sollen (Dimension von <code>densityData</code> und <code>cumulativeDensity</code>)
	 * @see DataDistributionImpl#densityData
	 * @see DataDistributionImpl#cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final int steps) {
		super(null);
		this.upperBound=upperBound;
		argumentScaleFactor=steps/((upperBound==86399)?86400:upperBound);
		densityData=new double[steps];
	}

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param data	Verwendet diese Daten als Dichte
	 * @see DataDistributionImpl#densityData
	 * @see DataDistributionImpl#cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final double[] data) {
		super(null);
		this.upperBound=upperBound;
		argumentScaleFactor=data.length/((upperBound==86399)?86400:upperBound);
		densityData=data.clone();
		updateCumulativeDensity();
	}

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param data	Verwendet diese Daten als Dichte
	 * @param readOnly	Wird hier <code>true</code> übergeben, so werden die in <code>data</code> übergebenen Daten direkt und nicht in Kopie verwendet
	 * @see #densityData
	 * @see #cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final double[] data, final boolean readOnly) {
		super(null);
		this.upperBound=upperBound;
		argumentScaleFactor=data.length/((upperBound==86399)?86400:upperBound);
		densityData=readOnly?data:data.clone();
		if (!readOnly) updateCumulativeDensity();
	}

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param data	Verwendet diese Daten als Dichte
	 * @see #densityData
	 * @see #cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final int[] data) {
		super(null);
		this.upperBound=upperBound;
		argumentScaleFactor=data.length/((upperBound==86399)?86400:upperBound);
		densityData=new double[data.length];
		for (int i=0;i<data.length;i++) densityData[i]=data[i];
		updateCumulativeDensity();
	}

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param data	Verwendet diese Daten als Dichte
	 * @see #densityData
	 * @see #cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final long[] data) {
		super(null);
		this.upperBound=upperBound;
		argumentScaleFactor=data.length/((upperBound==86399)?86400:upperBound);
		densityData=new double[data.length];
		for (int i=0;i<data.length;i++) densityData[i]=data[i];
		updateCumulativeDensity();
	}

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param data	Verwendet diese Daten als Dichte
	 * @see #densityData
	 * @see #cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final Double[] data) {
		super(null);
		this.upperBound=upperBound;
		argumentScaleFactor=data.length/((upperBound==86399)?86400:upperBound);
		densityData=new double[data.length];
		for (int i=0;i<data.length;i++) densityData[i]=data[i];
		updateCumulativeDensity();
	}

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param data	Verwendet diese Daten als Dichte
	 * @see #densityData
	 * @see #cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final Integer[] data) {
		super(null);
		this.upperBound=upperBound;
		argumentScaleFactor=data.length/((upperBound==86399)?86400:upperBound);
		densityData=new double[data.length];
		for (int i=0;i<data.length;i++) densityData[i]=data[i];
		updateCumulativeDensity();
	}

	/**
	 * Konstruktor der Klasse <code>DataDistributionImpl</code>
	 * @param upperBound	Gibt die Obergrenze des Trägers der Dichte an
	 * @param data	Verwendet diese Daten als Dichte
	 * @see #densityData
	 * @see #cumulativeDensity
	 */
	public DataDistributionImpl(final double upperBound, final List<Double> data) {
		this(upperBound,data.toArray(new Double[0]));
	}

	/**
	 * Aktualisiert die Verteilungsfunktion <code>cumulativeDensity</code> nach dem Ändern
	 * der Dichtedaten in <code>densityData</code>
	 * @see #cumulativeDensity
	 * @see #densityData
	 */
	public void updateCumulativeDensity() {
		double sum=0;
		if (cumulativeDensity==null || cumulativeDensity.length!=densityData.length) cumulativeDensity=new double[densityData.length];
		for (int i=0;i<densityData.length;i++) cumulativeDensity[i]=(sum+=densityData[i]);
		if (sum>0 && sum!=1) for (int i=0;i<densityData.length;i++) cumulativeDensity[i]/=sum;
	}

	/**
	 * Skaliert die Dichte so, dass die Summe über alle Dichte-Werte 1 ergibt.
	 * Außerdem wird bei Bedarf die Verteilungsfunktion angelegt oder aktualisiert.
	 */
	public void normalizeDensity() {
		final int size=densityData.length;
		double sum=0;
		if (cumulativeDensity==null || cumulativeDensity.length!=size) cumulativeDensity=new double[size];
		for (int i=0;i<size;i++) cumulativeDensity[i]=(sum+=densityData[i]);
		if (sum>0 && Math.abs(sum-1)>1E-8) for (int i=0;i<size;i++) {
			densityData[i]/=sum;
			cumulativeDensity[i]/=sum;
		}
	}

	/**
	 * Skaliert die Dichte so, dass die Summe über alle Dichte-Werte 1 ergibt.
	 * Wurde noch keine Verteilungsfunktion angelegt, so erfolgt dies auch hier
	 * nicht. Existiert bereits eine Verteilungsfunktion, so wird auch diese aktualisiert.
	 */
	public void normalizeDensityOnly() {
		if (cumulativeDensity!=null) {
			normalizeDensity();
			return;
		}

		final int size=densityData.length;
		double sum=0;
		for (int i=0;i<size;i++) sum+=densityData[i];
		if (sum>0 && Math.abs(sum-1)>1E-8) for (int i=0;i<size;i++) densityData[i]/=sum;
	}

	/**
	 * Setzt die Dichte auf 0 zurück.
	 */
	public void clearDensityData() {
		Arrays.fill(densityData,0);
	}

	/**
	 * Setzt die Dichte durchgängig auf einen bestimmten Wert.
	 * @param d	Wert, auf den die Dichte eingestellt werden soll.
	 */
	public void setToValue(final double d) {
		Arrays.fill(densityData,d);
	}

	/**
	 * Bildet die Summe über die Werte der Dichte.
	 * @return Summe über die Werte der Dichte.
	 */
	public double sum() {
		double s=0;
		for (int i=0;i<densityData.length;i++) s+=densityData[i];
		return s;
	}

	/**
	 * Bildet die Summe über die Werte der Dichte.
	 * Die Werte werden dabei mit der Genauigkeit, wie sie auch beim Speichern der Daten als String verwendet wird, summiert.
	 * @return Summe über die Werte der Dichte.
	 */
	public double sumAsStoredAsString() {
		double s=0;
		for (int i=0;i<densityData.length;i++) {
			s+=NumberTools.getPlainDouble(NumberTools.formatNumberMax(densityData[i]));
		}
		return s;
	}

	/**
	 * Prüft, ob die Summe über alle Werte 0 ist.
	 * (Es wird dabei angenommen, dass keine negativen Werte in der Summe stehen, d.h. die Partialsumme durch das
	 * Hinzufügen weiterer Summanden nur wachsen kann und nicht wieder fallen kann.)
	 * @return	Gibt <code>true</code> zurück, wenn die Summe ==0 ist.
	 */
	public boolean sumIsZero() {
		for (double d : densityData) if (Math.abs(d)>1E-8) return false;
		return true;
	}

	/**
	 * Gleicht die aktuelle und eine weitere Verteilung in Bezug auf die
	 * Anzahl an Datenpunkten an.
	 * @param d	Zweite Verteilung, die angepasst werden soll, um zu der aktuellen Verteilung zu passen
	 * @return	Array aus einer jeweils evtl. angepassten Kopie der aktuellen Verteilung und der zweiten Verteilung (wobei die zweite Verteilung im Original übergeben wird, wenn keine Anpassungen notwendig sind)
	 */
	private DataDistributionImpl[] prepare(final DataDistributionImpl d) {
		final DataDistributionImpl dist1=clone();
		final DataDistributionImpl dist2;

		if (densityData.length==d.densityData.length) {
			dist2=d;
		} else {
			if (densityData.length<d.densityData.length) {
				dist1.stretchToValueCount(d.densityData.length);
				dist2=d;
			} else {
				dist2=d.clone();
				dist2.stretchToValueCount(densityData.length);
			}
		}

		return new DataDistributionImpl[]{dist1,dist2};
	}

	/**
	 * Berechnet elementweise das Minimum der Dichte und der in dem Parameter übergebenen Wert und liefert das Ergebnis als neue Verteilung zurück
	 * @param d	Wert, mit dem das Minimum gebildet werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl min(final double d) {
		DataDistributionImpl dist=clone();
		double[] data=dist.densityData;
		for (int i=0;i<data.length;i++) data[i]=Math.min(data[i],d);
		return dist;
	}

	/**
	 * Berechnet elementweise das Minimum der Dichte und der Dichte der durch den Parameter angegebenen Verteilung und liefert das Ergebnis als neue Verteilung zurück
	 * Es dürfen hier Verteilungen mit verschieden vielen Werten verwendet werden; die jeweils kürzere wird dann hochskaliert.
	 * @param d	Verteilung, mit deren Dichte das Minimum bestimmt werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl min(final DataDistributionImpl d) {
		DataDistributionImpl[] dist=prepare(d);
		double[] data=dist[0].densityData;
		double[] data2=dist[1].densityData;
		for (int i=0;i<data.length;i++) data[i]=Math.min(data[i],data2[i]);
		return dist[0];
	}

	/**
	 * Berechnet elementweise das Maximum der Dichte und der in dem Parameter übergebenen Wert und liefert das Ergebnis als neue Verteilung zurück
	 * @param d	Wert, mit dem das Maximum gebildet werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl max(final double d) {
		DataDistributionImpl dist=clone();
		double[] data=dist.densityData;
		for (int i=0;i<data.length;i++) data[i]=Math.max(data[i],d);
		return dist;
	}

	/**
	 * Berechnet elementweise das Maximum der Dichte und der Dichte der durch den Parameter angegebenen Verteilung und liefert das Ergebnis als neue Verteilung zurück
	 * Es dürfen hier Verteilungen mit verschieden vielen Werten verwendet werden; die jeweils kürzere wird dann hochskaliert.
	 * @param d	Verteilung, mit deren Dichte das Maximum bestimmt werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl max(final DataDistributionImpl d) {
		DataDistributionImpl[] dist=prepare(d);
		double[] data=dist[0].densityData;
		double[] data2=dist[1].densityData;
		for (int i=0;i<data.length;i++) data[i]=Math.max(data[i],data2[i]);
		return dist[0];
	}

	/**
	 * Addiert den durch den Parameter angegebenen Wert zu der Dichte und liefert das Ergebnis als neue Verteilung zurück
	 * @param d	Wert, der zur Dichte addiert werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl add(final double d) {
		DataDistributionImpl dist=clone();
		if (d!=0) {
			double[] data=dist.densityData;
			for (int i=0;i<data.length;i++) data[i]+=d;
		}
		return dist;
	}

	/**
	 * Addiert die Dichte der durch den Parameter angegebenen Verteilung zu der Dichte und liefert das Ergebnis als neue Verteilung zurück
	 * Es dürfen hier Verteilungen mit verschieden vielen Werten verwendet werden; die jeweils kürzere wird dann hochskaliert.
	 * @param d	Verteilung, deren Dichte zu der Dichte addiert werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl add(final DataDistributionImpl d) {
		DataDistributionImpl[] dist=prepare(d);
		double[] data=dist[0].densityData;
		double[] add=dist[1].densityData;
		for (int i=0;i<data.length;i++) data[i]+=add[i];
		return dist[0];
	}

	/**
	 * Vergleichbar der Funktion {@link #add(DataDistributionImpl)}, aber
	 * es wird keine neue Verteilung erstellt, sondern die Daten werden direkt
	 * zu dieser Verteilung addiert.
	 * @param d	Verteilung, deren Dichte zu der Dichte addiert werden soll
	 * @see #add(DataDistributionImpl)
	 */
	public void addToThis(final DataDistributionImpl d) {
		if (densityData.length==d.densityData.length) {
			for (int i=0;i<densityData.length;i++) densityData[i]+=d.densityData[i];
		} else {
			if (densityData.length<d.densityData.length) {
				stretchToValueCount(d.densityData.length);
				for (int i=0;i<densityData.length;i++) densityData[i]+=d.densityData[i];
			} else {
				final DataDistributionImpl dist2=d.clone();
				dist2.stretchToValueCount(densityData.length);
				for (int i=0;i<densityData.length;i++) densityData[i]+=dist2.densityData[i];
			}
		}
	}

	/**
	 * Subtrahiert den durch den Parameter angegebenen Wert von der Dichte und liefert das Ergebnis als neue Verteilung zurück
	 * @param d	Wert, der von der Dichte subtrahiert werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl sub(final double d) {
		DataDistributionImpl dist=clone();
		if (d!=0) {
			double[] data=dist.densityData;
			for (int i=0;i<data.length;i++) data[i]-=d;
		}
		return dist;
	}

	/**
	 * Subtrahiert die Dichte der durch den Parameter angegebenen Verteilung von der Dichte und liefert das Ergebnis als neue Verteilung zurück
	 * Es dürfen hier Verteilungen mit verschieden vielen Werten verwendet werden; die jeweils kürzere wird dann hochskaliert.
	 * @param d	Verteilung, deren Dichte von der Dichte subtrahiert werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl sub(final DataDistributionImpl d) {
		DataDistributionImpl[] dist=prepare(d);
		double[] data=dist[0].densityData;
		double[] sub=dist[1].densityData;
		for (int i=0;i<data.length;i++) data[i]-=sub[i];
		return dist[0];
	}

	/**
	 * Multipliziert die Werte der Dichte mit dem angegebenen Parameter und liefert das Ergebnis als neue Verteilung zurück
	 * @param d	Wert, mit dem die Werte der Dichte multipliziert werden sollen
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl multiply(final double d) {
		DataDistributionImpl dist=clone();
		double[] data=dist.densityData;
		for (int i=0;i<data.length;i++) data[i]*=d;
		return dist;
	}

	/**
	 * Multipliziert die Dichte der durch den Parameter angegebenen Verteilung mit der Dichte und liefert das Ergebnis als neue Verteilung zurück
	 * Es dürfen hier Verteilungen mit verschieden vielen Werten verwendet werden; die jeweils kürzere wird dann hochskaliert.
	 * @param d	Verteilung, deren Dichte mit der Dichte multipliziert werden soll
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl multiply(final DataDistributionImpl d) {
		DataDistributionImpl[] dist=prepare(d);
		double[] data=dist[0].densityData;
		double[] mul=dist[1].densityData;
		for (int i=0;i<data.length;i++) data[i]*=mul[i];
		return dist[0];
	}

	/**
	 * Teilt die Werte der Dichte durch dem angegebenen Parameter und liefert das Ergebnis als neue Verteilung zurück
	 * @param d	Wert, durch den die Werte der Dichte geteilt werden sollen
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl divide(final double d) {
		DataDistributionImpl dist=clone();
		if (d!=0) {
			double[] data=dist.densityData;
			for (int i=0;i<data.length;i++) data[i]/=d;
		}
		return dist;
	}

	/**
	 * Teilt die Werte der Dichte durch die Werte der Dichte der im Parameter angegebenen Verteilung und liefert das Ergebnis als neue Verteilung zurück
	 * Es dürfen hier Verteilungen mit verschieden vielen Werten verwendet werden; die jeweils kürzere wird dann hochskaliert.
	 * @param d	Verteilung durch deren Dichte die Dichte dieser Verteilung geteilt werden sollen
	 * @return	Neue Verteilung mit neuer Dichte
	 */
	public DataDistributionImpl divide(final DataDistributionImpl d) {
		final DataDistributionImpl[] dist=prepare(d);
		double[] data=dist[0].densityData;
		double[] div=dist[1].densityData;
		for (int i=0;i<data.length;i++) if (Math.abs(div[i])>10E-10) data[i]/=div[i]; else data[i]=0;
		return dist[0];
	}

	/**
	 * Liefert eine neue Verteilung zurück, die sich durch die Rundung der Werte der Dichte ergibt.
	 * @return Neue Verteilung mit gerundeten Werten für die Dichte
	 */
	public DataDistributionImpl round() {
		DataDistributionImpl dist=clone();
		double[] data=dist.densityData;
		for (int i=0;i<data.length;i++) data[i]=Math.round(data[i]);
		return dist;
	}

	/**
	 * Liefert eine neue Verteilung zurück, die sich durch die Rundung der Werte nach unten der Dichte ergibt.
	 * @return Neue Verteilung mit nach unten gerundeten Werten für die Dichte
	 */
	public DataDistributionImpl floor() {
		DataDistributionImpl dist=clone();
		double[] data=dist.densityData;
		for (int i=0;i<data.length;i++) data[i]=Math.floor(data[i]);
		return dist;
	}

	/**
	 * Liefert eine neue Verteilung zurück, die sich durch die Rundung der Werte nach oben der Dichte ergibt.
	 * @return Neue Verteilung mit nach oben gerundeten Werten für die Dichte
	 */
	public DataDistributionImpl ceil() {
		DataDistributionImpl dist=clone();
		double[] data=dist.densityData;
		for (int i=0;i<data.length;i++) data[i]=Math.ceil(data[i]);
		return dist;
	}

	@Override
	public double density(double x) {
		if (densityData.length==0) return 0;
		x=x*argumentScaleFactor;
		final int i=(int)Math.round(Math.floor(x));
		if (i==densityData.length) return densityData[densityData.length-1];
		if (i<0 || i>=densityData.length) return 0;
		return densityData[i];
	}

	@Override
	public double cumulativeProbability(double x) {
		if (densityData.length==0) return 0;
		if (cumulativeDensity==null) updateCumulativeDensity();

		x=x*argumentScaleFactor;
		if (x<0) return 0;
		if (x>=densityData.length) return 1;

		final int i=(int)Math.round(Math.floor(x));

		final double a=(i==0)?0:cumulativeDensity[i-1];
		final double b=cumulativeDensity[i];

		final double xi=x-Math.floor(x);
		return a*(1-xi)+b*xi;
	}

	@Override
	public double getNumericalMean() {
		return getMean();
	}

	@Override
	public double getNumericalVariance() {
		final double sd=getStandardDeviation();
		return sd*sd;
	}

	/**
	 * Liefert die Schiefe der Verteilung.
	 * @return	Schiefe der Verteilung
	 */
	public double getSkewness() {
		final double mu=getMean();
		final double sigma=getStandardDeviation();
		/* (E[X^3]-3*mu*sigma^2-mu^3)/sigma^3 */
		return (getXPow3()-3*mu*sigma*sigma-mu*mu*mu)/(sigma*sigma*sigma);
	}

	@Override
	public double getSupportLowerBound() {
		return 0;
	}

	@Override
	public double getSupportUpperBound() {
		return densityData.length/argumentScaleFactor;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {return true;}

	@Override
	public boolean isSupportUpperBoundInclusive() {return true;}

	@Override
	public boolean isSupportConnected() {return true;}

	@Override
	public double inverseCumulativeProbability(double p) {
		if (densityData.length==0) return 0;
		if (cumulativeDensity==null) updateCumulativeDensity();

		p=Math.min(1,Math.max(p,0));

		int nr=densityData.length-1;
		/* alt: for (int i=0;i<densityData.length;i++) if (p<cumulativeDensity[i]) {nr=i; break;} */
		int a=0, b=densityData.length-1;
		while (b-a>2) {
			int m=(a+b)/2;
			if (p<cumulativeDensity[m]) b=m; else a=m;
		}
		for (int i=a;i<=b;i++) if (p<cumulativeDensity[i]) {nr=i; break;}

		double diff;
		if (nr>0) {p-=cumulativeDensity[nr-1]; diff=cumulativeDensity[nr]-cumulativeDensity[nr-1];} else {diff=cumulativeDensity[nr];}

		/* wenn P(nr) und P(nr-1) unterschiedlich: Zwischen den Werten linear interpolieren */
		final double add=(diff==0)?0:p/diff;

		return (nr+add)/argumentScaleFactor;
	}

	/**
	 * Entspricht {@link #inverseCumulativeProbability(double)}, aber verzichtet darauf,
	 * zu prüfen, ob der Parameter zwischen 0 und 1 liegt.
	 * @param p	Wahrscheinlichkeit, zu der die Inverse berechnet werden soll
	 * @return	F^-1(p) Wert zu der gegebenen Wahrscheinlichkeit
	 * @see #inverseCumulativeProbability(double)
	 */
	public double inverseCumulativeProbabilityWithOutThrowsAndChecks(double p) {
		if (densityData.length==0) return 0;
		if (cumulativeDensity==null) updateCumulativeDensity();

		int nr=densityData.length-1;
		/* alt: for (int i=0;i<densityData.length;i++) if (p<cumulativeDensity[i]) {nr=i; break;} */
		int a=0, b=densityData.length-1;
		while (b-a>2) {
			int m=(a+b)/2;
			if (p<cumulativeDensity[m]) b=m; else a=m;
		}
		for (int i=a;i<=b;i++) if (p<cumulativeDensity[i]) {nr=i; break;}

		double diff;
		if (nr>0) {p-=cumulativeDensity[nr-1]; diff=cumulativeDensity[nr]-cumulativeDensity[nr-1];} else {diff=cumulativeDensity[nr];}

		/* wenn P(nr) und P(nr-1) unterschiedlich: Zwischen den Werten linear interpolieren */
		final double add=(diff==0)?0:p/diff;

		return (nr+add)/argumentScaleFactor;
	}

	/**
	 * Erzeugt direkt eine Zufallszahl gemäß der Verteilung.
	 * @return	Zufallswert gemäß der Verteilung
	 */
	@Override
	public double random(final RandomGenerator generator) {
		/* return inverseCumulativeProbabilityWithOutThrowsAndChecks(rnd.nextDouble()); */

		double p=generator.nextDouble();

		if (cumulativeDensity==null) updateCumulativeDensity();

		int nr=densityData.length-1;
		/* alt: for (int i=0;i<densityData.length;i++) if (p<cumulativeDensity[i]) {nr=i; break;} */
		int a=0, b=densityData.length-1;
		while (b-a>2) {
			int m=(a+b)/2;
			if (p<cumulativeDensity[m]) b=m; else a=m;
		}
		for (int i=a;i<=b;i++) if (p<cumulativeDensity[i]) {nr=i; break;}

		double diff;
		if (nr>0) {
			p-=cumulativeDensity[nr-1];
			diff=cumulativeDensity[nr]-cumulativeDensity[nr-1];
		} else {
			diff=cumulativeDensity[nr];
		}

		/* wenn P(nr) und P(nr-1) unterschiedlich: Zwischen den Werten linear interpolieren */
		final double add=(diff==0)?0:p/diff;

		return (nr+add)/argumentScaleFactor;
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in lokalisierter Form ausgegeben.
	 * @return Dichte-Array als ";"-getrennte Zeichenkette
	 * @see #densityData
	 */
	public String getDensityString() {
		if (densityData.length==0) return "";
		final StringBuilder s=new StringBuilder(NumberTools.formatNumberMax(densityData[0]));
		for (int i=1;i<densityData.length;i++) {s.append(";"); s.append(NumberTools.formatNumberMax(densityData[i]));}
		return s.toString();
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in lokalisierter Form als Prozentwerte ausgegeben.
	 * @return Dichte-Array als ";"-getrennte Zeichenkette
	 * @see #densityData
	 */
	public String getDensityStringPercent() {
		if (densityData.length==0) return "";
		final StringBuilder s=new StringBuilder(NumberTools.formatPercent(densityData[0],3));
		for (int i=1;i<densityData.length;i++) {s.append(";"); s.append(NumberTools.formatPercent(densityData[i],3));}
		return s.toString();
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergebenen Dichtedaten.
	 * @param valueStrings	Zeichenketten, die die Dichtewerten enthalten
	 * @param upperBound	Obere Grenze für den Träger der Dichte
	 * @param allowAllDecimalSeparators	Gibt an, ob nur der gemäß Lokalcode gültige oder alle denkbaren Dezimaltrenner als solche akzeptiert werden sollen.
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromArray(final String[] valueStrings, final double upperBound, final boolean allowAllDecimalSeparators) {
		if (valueStrings==null) return new DataDistributionImpl(upperBound,0);

		final double[] values=new double[valueStrings.length];
		Double d;
		for (int i=0;i<valueStrings.length;i++) {
			try {
				if (valueStrings[i].endsWith("%")) {
					d=NumberTools.getExtProbability(valueStrings[i]);
					if (d!=null) {values[i]=d; continue;}
				}
				values[i]=Double.parseDouble(valueStrings[i]);
			} catch (NumberFormatException e) {
				if (!allowAllDecimalSeparators) return null;
				d=NumberTools.getDouble(valueStrings[i]);
				if (d==null || d<0) {
					d=NumberTools.getExtProbability(valueStrings[i]);
					if (d==null) return null;
				}
				values[i]=d;
			}
		}
		final DataDistributionImpl dist=new DataDistributionImpl(upperBound,values.length);
		dist.densityData=values;
		dist.updateCumulativeDensity();
		return dist;
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergebenen Dichtedaten.
	 * @param s	Zeichenkette aus per ";" getrennten Dichtewerten
	 * @param upperBound	Obere Grenze für den Träger der Dichte
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromString(final String s, final double upperBound) {
		if (s==null || s.isEmpty()) return new DataDistributionImpl(upperBound,0);
		return createFromArray(s.split(";"),upperBound,true);
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergebenen Messwerten (nicht Dichtewerten).
	 * @param samples	Array, dass die Messwerte enthält
	 * @param normalize	Soll die Verteilung nach dem Laden normalisiert werden?
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromSamplesArray(final String[] samples, final boolean normalize) {
		if (samples==null || samples.length==0) return new DataDistributionImpl(1,0);

		final int[] numbers=new int[samples.length];
		for (int i=0;i<samples.length;i++) {
			final Integer I=NumberTools.getNotNegativeInteger(samples[i]);
			if (I==null) return null;
			numbers[i]=I;
		}
		return createFromSamplesArray(numbers,normalize);
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergeben Messwerten (nicht Dichtewerten).
	 * @param samples	Array, dass die Messwerte enthält
	 * @param normalize	Soll die Verteilung nach dem Laden normalisiert werden?
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromSamplesArray(final int[] samples, final boolean normalize) {
		if (samples==null || samples.length==0) return new DataDistributionImpl(1,0);

		int max=0;
		for (int i=0;i<samples.length;i++) max=Math.max(samples[i],max);
		final DataDistributionImpl dist=new DataDistributionImpl(max+1,max+1);

		for (int i=0;i<samples.length;i++) dist.densityData[samples[i]]++;
		if (normalize) {
			dist.normalizeDensity();
			dist.updateCumulativeDensity();
		}

		return dist;
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergebenen Messwerten (eine Zeile) oder Dichtewerten (zwei Zeilen).
	 * @param samples	Array, dass die Messwerte enthält
	 * @param normalize	Soll die Verteilung nach dem Laden normalisiert werden?
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromSamplesArray(final String[][] samples, final boolean normalize) {
		if (samples==null || samples.length==0) return new DataDistributionImpl(1,0);

		final int[][] numbers=new int[samples.length][];
		for (int i=0;i<samples.length;i++) {
			final int[] row=new int[samples[i].length];
			for (int j=0;j<samples[i].length;j++) {
				final Integer I=NumberTools.getNotNegativeInteger(samples[i][j]);
				if (I==null) return null;
				row[j]=I;
			}
			numbers[i]=row;
		}
		return createFromSamplesArray(numbers,normalize);
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergebenen Messwerten (eine Zeile) oder Dichtewerten (zwei Zeilen).
	 * @param samples	Array, dass die Messwerte enthält
	 * @param normalize	Soll die Verteilung nach dem Laden normalisiert werden?
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromSamplesArray(final int[][] samples, final boolean normalize) {
		if (samples==null || samples.length==0) return new DataDistributionImpl(1,0);

		if (samples.length==1) return createFromSamplesArray(samples[0],normalize);

		int max=0;
		for (int i=0;i<Math.min(samples[0].length,samples[1].length);i++) max=Math.max(samples[0][i],max);
		final DataDistributionImpl dist=new DataDistributionImpl(max+1,max+1);

		for (int i=0;i<samples[0].length;i++) dist.densityData[Math.max(0,samples[0][i])]=samples[1][i];
		dist.normalizeDensity();
		dist.updateCumulativeDensity();

		return dist;
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergebenen Messwerten (nicht Dichtewerten).
	 * @param samples	Zeichenketten, die die Messwerte enthält
	 * @param normalize	Soll die Verteilung nach dem Laden normalisiert werden?
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromAnySamplesString(final String samples, final boolean normalize) {
		if (samples==null || samples.isEmpty()) return new DataDistributionImpl(1,0);
		final String splitChars="\n\t;";
		String[] strings=new String[]{samples};
		for (int i=0;i<splitChars.length();i++) strings=splitArray(strings,Character.toString(splitChars.charAt(i)));
		return createFromSamplesArray(strings,normalize);
	}

	/**
	 * Trennt eine oder mehrere Zeichenketten gemäß einem oder mehreren Trennzeichen
	 * @param strings	Zeichenketten, die getrennt werden sollen
	 * @param splitChar	Zu berücksichtigende Trennzeichen
	 * @return	Einzel-Zeichenketten
	 */
	private static String[] splitArray(final String[] strings, final String splitChar) {
		final String[][] temp=new String[strings.length][];
		for (int i=0; i<strings.length;i++) temp[i]=strings[i].split(splitChar);
		int size=0; for (int i=0; i<temp.length;i++) size+=temp[i].length;
		final String[] result=new String[size];
		int pos=0; for (int i=0; i<temp.length;i++) for (int j=0;j<temp[i].length;j++) {result[pos]=temp[i][j]; pos++;}
		return result;
	}

	/**
	 * Erstellt ein <code>DataDistributionImpl</code>-Objekt auf Basis der übergebenen Dichtedaten.
	 * @param s	Zeichenkette aus per ";", Tabulatoren oder Zeilentrennern getrennten Dichtewerten
	 * @param upperBound	Obere Grenze für den Träger der Dichte
	 * @return	Neues Objekt vom Typ <code>DataDistributionImpl</code>
	 */
	public static DataDistributionImpl createFromAnyString(final String s, final double upperBound) {
		if (s==null || s.isEmpty()) return new DataDistributionImpl(upperBound,0);
		final String splitChars="\n\t;";
		String[] strings=new String[]{s};
		for (int i=0;i<splitChars.length();i++) strings=splitArray(strings,Character.toString(splitChars.charAt(i)));
		return createFromArray(strings,upperBound,true);
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in System-Form ausgegeben.
	 * @param separator	Trennzeichen für die Werte der Verteilung
	 * @return Dichte-Array als durch den Separator getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToString(final String separator) {
		return storeToString(separator,null);
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in System-Form ausgegeben.
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Dichte-Array als durch den Separator getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToString(final StringBuilder recycleStringBuilder) {
		return storeToString(";",recycleStringBuilder);
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in System-Form ausgegeben.
	 * @param separator	Trennzeichen für die Werte der Verteilung
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Dichte-Array als durch den Separator getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToString(final String separator, final StringBuilder recycleStringBuilder) {
		if (densityData.length==0) return "";

		final StringBuilder sb;
		if (recycleStringBuilder==null) {
			sb=new StringBuilder(densityData.length*(separator.length()+2));
		} else {
			sb=recycleStringBuilder;
			sb.setLength(0);
		}

		final StringBuilder reuseSB=new StringBuilder();
		sb.append(NumberTools.formatSystemNumber(densityData[0],reuseSB));
		for (int i=1;i<densityData.length;i++) {
			sb.append(separator);
			sb.append(NumberTools.formatSystemNumber(densityData[i],reuseSB));
		}

		return sb.toString();
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in lokaler Form ausgegeben.
	 * @param separator	Trennzeichen für die Werte der Verteilung
	 * @return Dichte-Array als durch den Separator getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToLocalString(final String separator) {
		if (densityData.length==0) return "";
		final StringBuilder s=new StringBuilder(NumberTools.formatNumberMax(densityData[0]));
		for (int i=1;i<densityData.length;i++) {s.append(separator); s.append(NumberTools.formatNumberMax(densityData[i]));}
		return s.toString();
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in System-Form ausgegeben.
	 * @return Dichte-Array als ";"-getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToString() {
		return storeToString(";",null);
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Nullen am Ende werden entfernt.
	 * Dabei werden Zahlen in System-Form ausgegeben.
	 * @param separator	Trennzeichen für die Werte der Verteilung
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Dichte-Array als durch den Separator getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToStringShort(final String separator, final StringBuilder recycleStringBuilder) {
		if (densityData.length==0) return "";

		final StringBuilder sb;
		if (recycleStringBuilder==null) {
			sb=new StringBuilder(densityData.length*(separator.length()+2));
		} else {
			sb=recycleStringBuilder;
			sb.setLength(0);
		}

		final StringBuilder reuseSB=new StringBuilder();
		sb.append(NumberTools.formatSystemNumber(densityData[0],reuseSB));
		int last=1;
		for (int i=densityData.length-1;i>=1;i--) if (densityData[i]!=0) {last=i; break;}
		for (int i=1;i<=last;i++) {
			sb.append(separator);
			sb.append(NumberTools.formatSystemNumber(densityData[i],reuseSB));
		}

		return sb.toString();
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Nullen am Ende werden entfernt.
	 * Dabei werden Zahlen in System-Form ausgegeben.
	 * @return Dichte-Array als ";"-getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToStringShort() {
		return storeToStringShort(";",null);
	}

	/**
	 * Wandelt das in <code>densityData</code> gespeicherte Array aus Dichtewerten in eine Zeichenkette um.
	 * Dabei werden Zahlen in lokaler Form ausgegeben.
	 * @return Dichte-Array als ";"-getrennte Zeichenkette
	 * @see #densityData
	 */
	public String storeToLocalString() {
		return storeToLocalString(";");
	}

	/**
	 * Liefert den minimalen Dichte-Wert.
	 * @return	Minimum über die Dichte-Werte.
	 */
	public double getMin() {
		if (densityData.length==0) return 0;
		double min=densityData[0];
		for (int i=1;i<densityData.length;i++) min=Math.min(min,densityData[i]);
		return min;
	}

	/**
	 * Liefert den maximalen Dichte-Wert.
	 * @return	Maximum über die Dichte-Werte.
	 */
	public double getMax() {
		if (densityData.length==0) return 0;
		double max=densityData[0];
		for (int i=1;i<densityData.length;i++) max=Math.max(max,densityData[i]);
		return max;
	}

	/**
	 * Berechnet den Mittelwert der empirischen Verteilungsfunktion
	 * @return Mittelwert
	 */
	public double getMean() {
		if (densityData.length==0) return 0;
		double densitySum=0, sum=0;
		double scale=1/argumentScaleFactor;
		for (int i=0;i<densityData.length;i++) {
			densitySum+=densityData[i];
			sum+=densityData[i]*(scale*i);
		}
		if (densitySum==0.0) return 0.0;
		sum/=densitySum;

		return sum;
	}

	/**
	 * Berechnet ein Quantil der empirischen Verteilungsfunktion
	 * @param p	Wert für das Quantil
	 * @return	Quantil der Dichte
	 */
	public double getQuantil(final double p) {
		final double quantilSum=getSum()*Math.min(1.0,Math.max(0.0,p));
		int index=-1;
		double partialSum=0;
		for (int i=0;i<densityData.length;i++) {
			partialSum+=densityData[i];
			if (partialSum>=quantilSum) {index=i; break;}
		}
		if (index<0) return 0.0;

		if (argumentScaleFactor==0.0) return 0.0;
		return index/argumentScaleFactor;
	}

	/**
	 * Berechnet den Median der empirischen Verteilungsfunktion
	 * @return Median
	 */
	public double getMedian() {
		return getQuantil(0.5);
	}

	/**
	 * Liefert die Summe über alle Werte innerhalb der Dichte.<br>
	 * (Sollte bei normierten Verteilungen 1 sein.)
	 * @return	Summe über alle Werte innerhalb der Dichte
	 */
	public double getSum() {
		double sum=0.0;
		for (double value: densityData) sum+=value;
		return sum;
	}

	/**
	 * Liefert das zweite Moment der Werte (X^2)
	 * @return	Zweites Moment der Werte
	 * @see #getStandardDeviation()
	 */
	private double getXSqr() {
		if (densityData.length==0) return 0;
		double densitySum=0, sum=0;
		double scale=1/argumentScaleFactor;
		for (int i=0;i<densityData.length;i++) {
			densitySum+=densityData[i];
			sum+=densityData[i]*(scale*i)*(scale*i);
		}
		if (densitySum==0.0) return 0.0;
		sum/=densitySum;

		return sum;
	}

	/**
	 * Liefert das dritte Moment der Werte (X^3)
	 * @return	Drittes Moment der Werte
	 * @see #getSkewness()
	 */
	private double getXPow3() {
		if (densityData.length==0) return 0;
		double densitySum=0, sum=0;
		double scale=1/argumentScaleFactor;
		for (int i=0;i<densityData.length;i++) {
			densitySum+=densityData[i];
			sum+=densityData[i]*(scale*i)*(scale*i)*(scale*i);
		}
		if (densitySum==0.0) return 0.0;
		sum/=densitySum;

		return sum;
	}

	/**
	 * Berechnet die Standardabweichung der empirischen Verteilungsfunktion
	 * @return Standardabweichung
	 */
	public double getStandardDeviation() {
		return Math.sqrt(getXSqr()-FastMath.pow(getMean(),2));
	}

	@Override
	public DataDistributionImpl clone() {
		final DataDistributionImpl d=new DataDistributionImpl(upperBound,densityData.length);
		System.arraycopy(densityData,0,d.densityData,0,densityData.length);
		if (cumulativeDensity!=null) d.cumulativeDensity=Arrays.copyOf(cumulativeDensity,cumulativeDensity.length);
		return d;
	}

	/**
	 * Skaliert die Werte der Verteilung so, dass sich die angegebene Anzahl an Werten ergibt
	 * @param count	Neue Anzahl an Werten in der Verteilung
	 */
	public void stretchToValueCount(final int count) {
		if (densityData.length==count) return;

		final double[] d=densityData;
		densityData=new double[count];

		if (d.length!=0) {
			for (int i=0;i<densityData.length;i++) {
				final double x=(double)i/(densityData.length)*(d.length);
				final int index=(int)Math.max(0,Math.min(d.length-1,Math.floor(x)));
				densityData[i]=d[index];
			}
		} else {
			/* Wir hatten bislang überhaupt keine Werte. Also können wir das nur auf 0-Werte aufziehen. */
		}

		updateCumulativeDensity();
	}
}