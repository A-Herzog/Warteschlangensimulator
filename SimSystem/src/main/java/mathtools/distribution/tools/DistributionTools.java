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
package mathtools.distribution.tools;

import java.util.stream.Stream;

import javax.swing.ImageIcon;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.NeverDistributionImpl;

/**
 * Liefert verschiedene Informationen zu kontinuierlichen Verteilungen
 * @author Alexander Herzog
 * @version 2.3
 * @see AbstractRealDistribution
 */
public final class DistributionTools {
	/** Empirische Daten */
	public static String[] DistData=new String[]{"Empirische Daten"};

	/** Niemals-Verteilung */
	public static String[] DistNever=new String[]{"Niemals","nie"};

	/** Punkt "unendlich" */
	public static String[] DistInfinite=new String[]{"unendlich"};

	/** Ein-Punkt-Verteilung */
	public static String[] DistPoint=new String[]{"Ein-Punkt-Verteilung"};

	/** Gleichverteilung */
	public static String[] DistUniform=new String[]{"Gleichverteilung"};

	/** Exponentialverteilung */
	public static String[] DistExp=new String[]{"Exponentialverteilung"};

	/** Normalverteilung */
	public static String[] DistNormal=new String[]{"Normalverteilung"};

	/** Lognormalverteilung */
	public static String[] DistLogNormal=new String[]{"Lognormalverteilung"};

	/** Erlang-Verteilung */
	public static String[] DistErlang=new String[]{"Erlang-Verteilung"};

	/** Gamma-Verteilung */
	public static String[] DistGamma=new String[]{"Gamma-Verteilung"};

	/** Beta-Verteilung */
	public static String[] DistBeta=new String[]{"Beta-Verteilung"};

	/** Cauchy-Verteilung */
	public static String[] DistCauchy=new String[]{"Cauchy-Verteilung"};

	/** Weibull-Verteilung */
	public static String[] DistWeibull=new String[]{"Weibull-Verteilung"};

	/** Chi-Verteilung */
	public static String[] DistChi=new String[]{"Chi-Verteilung"};

	/** Chi^2-Verteilung */
	public static String[] DistChiQuare=new String[]{"Chi^2-Verteilung"};

	/** F-Verteilung */
	public static String[] DistF=new String[]{"F-Verteilung"};

	/** Jonhson-SU-Verteilung */
	public static String[] DistJohnson=new String[]{"Jonhson-SU-Verteilung"};

	/** Dreiecksverteilung */
	public static String[] DistTriangular=new String[]{"Dreiecksverteilung"};

	/** Pert-Verteilung */
	public static String[] DistPert=new String[]{"Pert-Verteilung"};

	/** Laplace-Verteilung */
	public static String[] DistLaplace=new String[]{"Laplace-Verteilung"};

	/** Pareto-Verteilung */
	public static String[] DistPareto=new String[]{"Pareto-Verteilung"};

	/** Logistische Verteilung */
	public static String[] DistLogistic=new String[]{"Logistische Verteilung"};

	/** Inverse Gauß-Verteilung */
	public static String[] DistInverseGaussian=new String[]{"Inverse Gauß-Verteilung"};

	/** Rayleigh-Verteilung */
	public static String[] DistRayleigh=new String[]{"Rayleigh-Verteilung"};

	/** Log-Logistische Verteilung */
	public static String[] DistLogLogistic=new String[]{"Log-Logistische Verteilung"};

	/** Potenzverteilung */
	public static String[] DistPower=new String[]{"Potenzverteilung"};

	/** Gumbel-Verteilung */
	public static String[] DistGumbel=new String[]{"Gumbel-Verteilung"};

	/** Fatigue-Life-Verteilung */
	public static String[] DistFatigueLife=new String[]{"Fatigue-Life-Verteilung"};

	/** Frechet-Verteilung */
	public static String[] DistFrechet=new String[]{"Frechet-Verteilung"};

	/** Hyperbolische Sekanten-Verteilung */
	public static String[] DistHyperbolicSecant=new String[]{"Hyperbolische Sekanten-Verteilung"};

	/** Sägezahnverteilung (links) */
	public static String[] DistSawtoothLeft=new String[]{"Linke Sägezahn-Verteilung"};

	/** Sägezahnverteilung (rechts) */
	public static String[] DistSawtoothRight=new String[]{"Rechte Sägezahn-Verteilung"};

	/** Levy-Verteilung */
	public static String[] DistLevy=new String[]{"Levy-Verteilung"};

	/** Maxwell-Boltzmann-Verteilung */
	public static String[] DistMaxwellBoltzmann=new String[]{"Maxwell-Boltzmann-Verteilung"};

	/** Hypergeometrische Verteilung */
	public static String[] DistHyperGeom=new String[]{"Hypergeometrische Verteilung"};

	/** Binomialverteilung */
	public static String[] DistBinomial=new String[]{"Binomialverteilung"};

	/** Poisson-Verteilung */
	public static String[] DistPoisson=new String[]{"Poisson-Verteilung"};

	/** Negative Binomialverteilung */
	public static String[] DistNegativeBinomial=new String[]{"Negative Binomialverteilung"};

	/** Zeta-Verteilung */
	public static String[] DistZeta=new String[]{"Zeta-Verteilung"};

	/** Diskrete Gleichverteilung */
	public static String[] DistDiscreteUniform=new String[]{"Diskrete Gleichverteilung"};

	/** Warnung "unbekannte Verteilung" */
	public static String DistUnknown="unbekannte Verteilung";

	/** Empirische Verteilung - "Datenpunkt" */
	public static String DistDataPoint="Datenpunkt";

	/** Empirische Verteilung - "Datenpunkte" */
	public static String DistDataPoints="Datenpunkte";

	/** Bezeichner "Bereich" */
	public static String DistRange="Bereich";

	/** Bezeichner "Lage" */
	public static String DistLocation="Lage";

	/** Bezeichner "Skalierung" */
	public static String DistScale="Skalierung";

	/** Bezeichner "Am wahrscheinlichsten" (für Dreiecksverteilung) */
	public static String DistMostLikely="Am wahrscheinlichsten";

	/** Bezeichner "Freiheitsgrade" */
	public static String DistDegreesOfFreedom="Freiheitsgrade";

	/** Bezeichner "Mittelwert" */
	public static String DistMean="Mittelwert";

	/** Bezeichner "Standardabweichung" */
	public static String DistStdDev="Standardabweichung";

	/** Bezeichner "Variationskoeffizient" */
	public static String DistCV="Variationskoeffizient";

	/** Bezeichner "Schiefe" */
	public static String DistSkewness="Schiefe";

	/** Bezeichner "Modus" */
	public static String DistMode="Modus";

	/** Bezeichner "Parameter" */
	public static String DistParameter="Parameter";

	/**
	 * Diese Klasse kann nicht instanziert werden.
	 */
	private DistributionTools() {
	}

	/**
	 * Statische Liste mit den verfügbaren Verteilungs-Wrappern
	 * @see AbstractDistributionWrapper
	 */
	private static final AbstractDistributionWrapper[] wrappers;

	static {
		wrappers=new AbstractDistributionWrapper[] {
				new WrapperDataDistribution(),
				new WrapperNeverDistribution(), /* Versteckt in der Liste */
				new WrapperOnePointDistribution(),
				new WrapperUniformRealDistribution(),
				new WrapperExponentialDistribution(),
				new WrapperNormalDistribution(),
				new WrapperLogNormalDistribution(),
				new WrapperErlangDistribution(),
				new WrapperGammaDistribution(),
				new WrapperBetaDistribution(),
				new WrapperCauchyDistribution(),
				new WrapperWeibullDistribution(),
				new WrapperChiSquaredDistribution(),
				new WrapperChiDistribution(),
				new WrapperFDistribution(),
				new WrapperJohnsonDistribution(),
				new WrapperTriangularDistribution(),
				new WrapperPertDistribution(),
				new WrapperLaplaceDistribution(),
				new WrapperParetoDistribution(),
				new WrapperLogisticDistribution(),
				new WrapperInverseGaussianDistribution(),
				new WrapperRayleighDistribution(),
				new WrapperLogLogisticDistribution(),
				new WrapperPowerDistribution(),
				new WrapperGumbelDistribution(),
				new WrapperFatigueLifeDistribution(),
				new WrapperFrechetDistribution(),
				new WrapperHyperbolicSecantDistribution(),
				new WrapperSawtoothLeftDistribution(),
				new WrapperSawtoothRightDistribution(),
				new WrapperLevyDistribution(),
				new WrapperMaxwellBoltzmannDistribution(),
				new WrapperHyperGeomDistribution(),
				new WrapperBinomialDistribution(),
				new WrapperPoissonDistribution(),
				new WrapperNegativeBinomialDistribution(),
				new WrapperZetaDistribution(),
				new WrapperDiscreteUniformDistribution()
		};
	}

	/**
	 * Liefert einen Wrapper für einen gegebenen Verteilungsnamen
	 * @param name	Name für der die Wrapperklasse bestimmt werden soll
	 * @return	Passender Wrapper oder <code>null</code> wenn für den Namen kein Wrapper vorhanden ist
	 */
	public static AbstractDistributionWrapper getWrapper(final String name) {
		for (AbstractDistributionWrapper wrapper: wrappers) if (wrapper.isForDistribution(name)) return wrapper;
		return null;
	}

	/**
	 * Liefert einen Wrapper für eine gegebenen Verteilung
	 * @param distribution	Verteilung für die der zugehörige Wrapper bestimmt werden soll
	 * @return	Passender Wrapper oder <code>null</code> wenn für die Verteilungsklasse kein Wrapper vorhanden ist
	 */
	public static AbstractDistributionWrapper getWrapper(final AbstractRealDistribution distribution) {
		for (AbstractDistributionWrapper wrapper: wrappers) if (wrapper.isForDistribution(distribution)) return wrapper;
		return null;
	}

	/**
	 * Auflistung der Namen aller unterstützten Verteilungen
	 * @return Liste der unterstützten Verteilungen
	 */
	public static String[] getDistributionNames() {
		return Stream.of(wrappers).filter(wrapper->!wrapper.isHiddenInNamesList()).map(wrapper->wrapper.getName()).toArray(String[]::new);
	}

	/**
	 * Versucht aus einem angegebenen Verteilungsnamen und Mittelwert und Standardabweichung eine Verteilung zu erstellen.
	 * @param name	Name der Verteilung
	 * @param mean	Mittelwert
	 * @param sd	Standardabweichung
	 * @return	Verteilungsobjekt oder <code>null</code> wenn aus den Daten keine Verteilung erstellt werden konnte
	 */
	public static AbstractRealDistribution getDistributionFromInfo(final String name, final double mean, final double sd) {
		final AbstractDistributionWrapper wrapper=getWrapper(name);
		if (wrapper!=null) return wrapper.getDistribution(mean,sd);
		return null;
	}

	/**
	 * Liefert ein Vorschaubild für eine Verteilung
	 * @param name	Name der Verteilung (gemäß <code>getDistributionNames</code> für das ein Vorschaubild geliefert werden soll.
	 * @return	Vorschaubild in 50x25 Pixeln Auflösung für die angegebene Verteilung oder <code>null</code>, wenn kein Vorschaubild vorhanden ist.
	 * @see DistributionTools#getDistributionName(AbstractRealDistribution)
	 */
	public static ImageIcon getThumbnailImageForDistributionName(final String name) {
		final AbstractDistributionWrapper wrapper=getWrapper(name);
		if (wrapper!=null) return wrapper.getThumbnailImage();
		return null;
	}

	/**
	 * Liefert den Namen einer Verteilung
	 * @param distribution Verteilungsobjekt, von dem der Name zurück gegeben werden soll.
	 * @return Name der Verteilung als String
	 */
	public static String getDistributionName(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.getName();
		return DistUnknown;
	}

	/**
	 * Liefert die wesentlichen Kenngrößen bzw. Parameter einer Verteilung
	 * @param distribution Verteilungsobjekt, zu dem Informationen zurück gegeben werden sollen.
	 * @return Informationen zu der Verteilung als String
	 */
	public static String getDistributionShortInfo(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper==null) return "";
		return wrapper.getInfo(distribution).getVeryShortInfo();
	}

	/**
	 * Liefert die Kenngrößen bzw. Parameter einer Verteilung
	 * @param distribution Verteilungsobjekt, zu dem Informationen zurück gegeben werden sollen.
	 * @return Informationen zu der Verteilung als String
	 */
	public static String getDistributionInfo(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper==null) return "";
		return wrapper.getInfo(distribution).getShortInfo();
	}

	/**
	 * Liefert die Kenngrößen bzw. Parameter einer Verteilung (in ausgeschriebener Form)
	 * @param distribution Verteilungsobjekt, zu dem Informationen zurück gegeben werden sollen.
	 * @return Informationen zu der Verteilung als String
	 */
	public static String getDistributionLongInfo(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper==null) return "";
		return wrapper.getInfo(distribution).getLongInfo();
	}

	/**
	 * Liefert den Mittelwert der übergebenen Verteilung
	 * @param distribution Verteilungsobjekt, von dem der Mittelwert bestimmt werden soll
	 * @return Mittelwert der Verteilung
	 */
	public static double getMean(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.getMean(distribution);
		return 0.0;
	}

	/**
	 * Gibt an, ob mit der <code>setMean</code>-Funktion ein neuer Mittelwert eingestellt werden kann
	 * @param distribution	Zu prüfende Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn der Mittelwert direkt eingestellt werden kann
	 * @see DistributionTools#setMean(AbstractRealDistribution, double)
	 * @see DistributionTools#canSetMeanExact(AbstractRealDistribution)
	 */
	public static boolean canSetMean(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.canSetMean;
		return false;
	}

	/**
	 * Gibt an, ob der Mittelwert ganz exakt eingestellt werden kann.
	 * Liefert {@link DistributionTools#canSetMean(AbstractRealDistribution)} zwar <code>true</code>
	 * aber diese Methode <code>false</code>, so kann der Mittelwert zwar eingestellt werden,
	 * aber da z.B. bestimmte Verteilungsparameter Ganzzahlen sein müssen, nicht absolut exakt.
	 * @param distribution	Zu prüfende Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn der Mittelwert direkt exakt eingestellt werden kann
	 * @see DistributionTools#setMean(AbstractRealDistribution, double)
	 * @see DistributionTools#canSetMean(AbstractRealDistribution)
	 */
	public static boolean canSetMeanExact(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.canSetMeanExact;
		return false;
	}

	/**
	 * Erstellt eine neue Verteilung als Kopie der übergebenen mit angepasstem Erwartungswert
	 * @param distribution	Alte Verteilung, auf der die neue basieren soll
	 * @param value	Neuer Erwartungswert
	 * @return	Liefert im Erfolgsfall die neue Verteilung; wenn eine Anpassung nicht möglich war, <code>null</code>
	 * @see DistributionTools#canSetMean(AbstractRealDistribution)
	 */
	public static AbstractRealDistribution setMean(final AbstractRealDistribution distribution, final double value) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.setMean(distribution,value);
		return null;
	}

	/**
	 * Liefert die Standardabweichung der übergebenen Verteilung
	 * @param distribution Verteilungsobjekt, von dem die Standardabweichung bestimmt werden soll
	 * @return Standardabweichung der Verteilung
	 */
	public static double getStandardDeviation(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.getStandardDeviation(distribution);
		return 0.0;
	}

	/**
	 * Gibt an, ob mit der <code>setStandardDeviation</code>-Funktion eine neue Standardabweichung eingestellt werden kann
	 * @param distribution	Zu prüfende Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn die Standardabweichung direkt eingestellt werden kann
	 * @see DistributionTools#setStandardDeviation(AbstractRealDistribution, double)
	 */
	public static boolean canSetStandardDeviation(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.canSetStandardDeviation;
		return false;
	}

	/**
	 * Gibt an, ob die Standardabweichung ganz exakt eingestellt werden kann.
	 * Liefert {@link DistributionTools#canSetStandardDeviation(AbstractRealDistribution)} zwar <code>true</code>
	 * aber diese Methode <code>false</code>, so kann die Standardabweichung zwar eingestellt werden,
	 * aber da z.B. bestimmte Verteilungsparameter Ganzzahlen sein müssen, nicht absolut exakt.
	 * @param distribution	Zu prüfende Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn die Standardabweichung direkt exakt eingestellt werden kann
	 * @see DistributionTools#setStandardDeviation(AbstractRealDistribution, double)
	 * @see DistributionTools#canSetStandardDeviation(AbstractRealDistribution)
	 */
	public static boolean canSetStandardDeviationExact(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.canSetStandardDeviationExact;
		return false;
	}

	/**
	 * Erstellt eine neue Verteilung als Kopie der übergebenen mit angepasster Standardabweichung
	 * @param distribution	Alte Verteilung, auf der die neue basieren soll
	 * @param value	Neue Standardabweichung
	 * @return	Liefert im Erfolgsfall die neue Verteilung; wenn eine Anpassung nicht möglich war, <code>null</code>
	 * @see DistributionTools#canSetStandardDeviation(AbstractRealDistribution)
	 */
	public static AbstractRealDistribution setStandardDeviation(final AbstractRealDistribution distribution, final double value) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.setStandardDeviation(distribution,value);
		return null;
	}

	/**
	 * Liefert den Wert eines Parameters einer Wahrscheinlichkeitsverteilung
	 * @param distribution	Wahrscheinlichkeitsverteilung, von der ein Parameter ausgelesen werden soll
	 * @param nr	Nummer des Parameters (1-4)
	 * @return	Wert des Parameters
	 */
	public static double getParameter(final AbstractRealDistribution distribution, final int nr) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.getParameter(distribution,nr);
		return 0.0;
	}

	/**
	 * Stellt den Wert eines Parameters einer Wahrscheinlichkeitsverteilung ein
	 * @param distribution	Wahrscheinlichkeitsverteilung, bei der ein Parameter eingestellt werden soll
	 * @param nr	Nummer des Parameters (1-4)
	 * @param value	Neuer Wert des Parameters
	 * @return	Neue Verteilung mit verändertem Parameter
	 */
	public static AbstractRealDistribution setParameter(final AbstractRealDistribution distribution, final int nr, final double value) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.setParameter(distribution,nr,value);
		return null;
	}

	/**
	 * Liefert den Variationskoeffizient der übergebenen Verteilung
	 * @param distribution Verteilungsobjekt, von dem der Veriationskoeffizient bestimmt werden soll
	 * @return Variationskoeffizient der Verteilung
	 */
	public static double getCV(final AbstractRealDistribution distribution) {
		final double sd=getStandardDeviation(distribution);
		if (sd==0) return 0;
		final double mean=getMean(distribution);
		if (mean==0) return 0;
		return sd/Math.abs(mean);
	}

	/**
	 * Wandelt die übergebene Verteilung in eine Zeichenkette um
	 * @param distribution	Umzuwandelndes Verteilungsobjekt
	 * @return	Verteilungsname und Parameter als Zeichenkette
	 * @see DistributionTools#distributionFromString(String, double)
	 */
	public static String distributionToString(AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.toString(distribution);
		return DistributionTools.getDistributionName(distribution);
	}

	/**
	 * Maximal zulässige Abweichung zwischen zwei Parametern, bei denen zwei Werte noch als identisch angenommen werden.
	 * @see AbstractDistributionWrapper#compareInt(AbstractRealDistribution, AbstractRealDistribution)
	 */
	public static final double MAX_ERROR=10E-12;

	/**
	 * Vergleicht zu Verteilungen und liefert <code>true</code> zurück, wenn Sie inhaltlich identisch sind.
	 * @param distribution1 Erste zu vergleichende Verteilung
	 * @param distribution2 Zweite zu vergleichende Verteilung
	 * @return Liefert <code>true</code> zurück, wenn die Daten der beiden Verteilungen identisch sind.
	 */
	public static boolean compare(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution1);
		if (wrapper!=null) return wrapper.compare(distribution1,distribution2);
		return distributionToString(distribution1).equals(distributionToString(distribution2));
	}

	/**
	 * Lädt eine kontinuierliche Verteilung aus einer Zeichenkette
	 * @param data Zeichenkette, die die Verteilungsdaten enthält (mit <code>distributionToString</code> zu erzeugen)
	 * @param maxXValue	Obere Grenze für den Träger der Dichte (wenn es sich um eine empirische Verteilung handelt)
	 * @return Liefert ein Verteilungsobjekt zurück, wenn die Verteilung erfolgreich geladen werden konnte; andernfalls wird <code>null</code> zurückgegeben.
	 * @see DistributionTools#distributionToString(AbstractRealDistribution)
	 */
	public static AbstractRealDistribution distributionFromString(final String data, final double maxXValue) {
		if (data==null) return null;

		/* Just values... */
		if (data.indexOf(';')>=0) {
			DataDistributionImpl dist=DataDistributionImpl.createFromString(data,maxXValue);
			if (dist!=null) return dist;
		}

		/* NeverDistributionImpl */
		for (String s: DistInfinite) if (data.equalsIgnoreCase(s)) return new NeverDistributionImpl();
		for (String s: DistNever) if (data.equalsIgnoreCase(s)) return new NeverDistributionImpl();

		/* Split "Name (Param)" */
		final int i=data.indexOf('(');
		if (i>=0) {
			final String name=data.substring(0,i).trim();
			String param=data.substring(i+1);
			if (param.endsWith(")")) param=param.substring(0,param.length()-1);
			param=param.trim();
			if (name.isEmpty() || param.isEmpty()) return null;

			final AbstractDistributionWrapper wrapper=getWrapper(name);
			if (wrapper!=null) return wrapper.fromString(param,maxXValue);
		}

		/* Just one value */
		final Double D=NumberTools.getDouble(data);
		if (D!=null) {
			DataDistributionImpl dist=DataDistributionImpl.createFromString(data,maxXValue);
			if (dist!=null) return dist;
		}

		return null;
	}

	/**
	 * Erstellt eine Kopie eines {@link AbstractRealDistribution}-Objektes
	 * @param distribution	Zu kopierende Verteilung
	 * @return	Kopiertes Verteilungsobjekt
	 */
	public static AbstractRealDistribution cloneDistribution(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.clone(distribution);
		return null;
	}

	/**
	 * Erstellt eine Kopie eines {@link AbstractRealDistribution}-Objektes und normalisiert bei
	 * diesem die Raten zu einer Dichte, sofern es sich um ein {@link DataDistributionImpl}-Objekt handelt.
	 * @param distribution	Zu kopierende Verteilung
	 * @return	Kopiertes Verteilungsobjekt
	 */
	public static AbstractRealDistribution cloneAndNormalizeDistribution(final AbstractRealDistribution distribution) {
		final AbstractRealDistribution cloned=cloneDistribution(distribution);
		if (cloned instanceof DataDistributionImpl) ((DataDistributionImpl)cloned).normalizeDensity();
		return cloned;
	}

	/**
	 * Sofern es sich bei dem Parameter um ein {@link DataDistributionImpl}-Objekt handelt, werden
	 * bei einer Kopie von diesem die Raten zu einer Dichte normalisiert und die Kopie wird zurückgegeben.
	 * Andernfalls wird einfach das im Parameter übergebene Verteilungsobjekt selbst zurückgegeben.
	 * @param distribution	Verteilung die ggf. zu normalisieren ist
	 * @return	Kopie oder Originalobjekt
	 */
	public static AbstractRealDistribution normalizeDistribution(final AbstractRealDistribution distribution) {
		if (distribution instanceof DataDistributionImpl) {
			final DataDistributionImpl clonedData=((DataDistributionImpl)distribution).clone();
			clonedData.normalizeDensity();
			return clonedData;
		}
		return distribution;
	}
}