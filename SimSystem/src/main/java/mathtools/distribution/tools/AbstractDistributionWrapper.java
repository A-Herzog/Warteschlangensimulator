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

import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

/**
 * Von dieser Klasse abgeleitete Klassen werden nur innerhalb von {@link DistributionTools} verwendet.
 * Jede dieser Klassen kapselt die Zusatzinformationen für eine {@link AbstractRealDistribution} Klasse.
 * @author Alexander Herzog
 * @see DistributionTools
 */
public abstract class AbstractDistributionWrapper {
	/**
	 * Klasse vom Typ {@link AbstractRealDistribution} für die diese Klasse weiter Informationen bereit hält
	 */
	public final Class<? extends AbstractRealDistribution> distributionClass;

	/**
	 * Kann bei diesen Verteilungen der Erwartungswert direkt eingestellt werden?
	 * @see #setMean(AbstractRealDistribution, double)
	 */
	public final boolean canSetMean;

	/**
	 * Gibt an, ob der Erwartungswert exakt eingestellt werden kann (<code>true</code>) oder ob er intern z.B. auf eine Ganzzahl gerundet wird (<code>false</code>)
	 */
	public final boolean canSetMeanExact;

	/**
	 * Klassen bei diesen Verteilungen die Standardabweichung direkt eingestellt werden?
	 * @see #setStandardDeviation(AbstractRealDistribution, double)
	 */
	public final boolean canSetStandardDeviation;

	/**
	 * Kann eine Verteilung direkt erstellt werden?
	 * @see #getDistribution(double, double)
	 */
	public final boolean canBuildDistributionFromMeanAndSD;

	/**
	 * Gibt an, ob die Standardabweichung exakt eingestellt werden kann (<code>true</code>) oder ob er intern z.B. auf eine Ganzzahl gerundet wird (<code>false</code>)
	 */
	public final boolean canSetStandardDeviationExact;

	/**
	 * Konstruktor der Klasse
	 * @param distributionClass	Klasse vom Typ {@link AbstractRealDistribution} für die diese Klasse weiter Informationen bereit hält
	 * @param canSetMean	Kann bei diesen Verteilungen der Erwartungswert direkt eingestellt werden?
	 * @param canSetStandardDeviation	Klassen bei diesen Verteilungen die Standardabweichung direkt eingestellt werden?
	 */
	public AbstractDistributionWrapper(final Class<? extends AbstractRealDistribution> distributionClass, final boolean canSetMean, final boolean canSetStandardDeviation) {
		this.distributionClass=distributionClass;
		this.canSetMean=canSetMean;
		this.canSetStandardDeviation=canSetStandardDeviation;
		canSetMeanExact=canSetMeanExact();
		canSetStandardDeviationExact=canSetStandardDeviationExact();
		canBuildDistributionFromMeanAndSD=canSetMean && canSetStandardDeviation && canBuildDistributionDirect();
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird davon ausgegangen, dass weder Erwartungswert noch Standardabweichung eingestellt werden können.
	 * @param distributionClass	Klasse vom Typ {@link AbstractRealDistribution} für die diese Klasse weiter Informationen bereit hält
	 */
	public AbstractDistributionWrapper(final Class<? extends AbstractRealDistribution> distributionClass) {
		this(distributionClass,false,false);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird davon ausgegangen, dass die Standardabweichung nicht eingestellt werden kann.
	 * @param distributionClass	Klasse vom Typ {@link AbstractRealDistribution} für die diese Klasse weiter Informationen bereit hält
	 * @param canSetMean	Kann bei diesen Verteilungen der Erwartungswert direkt eingestellt werden?
	 */
	public AbstractDistributionWrapper(final Class<? extends AbstractRealDistribution> distributionClass, final boolean canSetMean) {
		this(distributionClass,canSetMean,false);
	}

	/**
	 * Liefert die möglichen Namen der Verteilung (in den verschiedenen Sprachen).<br>
	 * Hieraus wird der anzuzeigende Name abgeleitet und beim Laden wird diese Liste verwendet.
	 * @return	Array mit den möglichen Namen der Verteilung
	 */
	protected abstract String[] getNames();

	/**
	 * Liefert den Dateinamen für das Thumbnail für diese Verteilung.
	 * @return	Dateiname inkl. Erweiterung aber ohne jeden Pfad
	 */
	protected abstract String getThumbnailImageName();

	/**
	 * Liefert den Link zur Wikipedia-Seite für diese Verteilung.
	 * @return	URL zur Wikipedia-Seite für diese Verteilung (oder <code>null</code>, wenn keine passende Adresse vorliegt)
	 */
	protected abstract String getWikipediaURL();

	/**
	 * Soll die Verteilung in der Auswahlliste angezeigt werden?
	 * @return	Verteilung anzeigen
	 */
	public boolean isHiddenInNamesList() {
		return false;
	}

	/**
	 * Prüft ob dieser Wrapper für das angegebene Verteilungsobjekt zuständig ist
	 * @param distribution	Verteilung bei der geprüft werden soll, ob dieser Wrapper zuständig ist
	 * @return	Gibt <code>true</code> zurück, wenn dieser Wrapper sich für zuständig erachtet.
	 */
	public final boolean isForDistribution(final AbstractRealDistribution distribution) {
		return (distribution!=null) && distributionClass.isInstance(distribution);
	}

	/**
	 * Prüft ob dieser Wrapper für Verteilung des angegebenen Namens zuständig ist
	 * @param name	Name der Verteilung bei der die Zuständigkeit geprüft werden soll
	 * @return	Gibt <code>true</code> zurück, wenn dieser Wrapper sich für zuständig erachtet.
	 */
	public final boolean isForDistribution(final String name) {
		if (name==null) return false;
		for (String testName: getNames()) if (testName.equalsIgnoreCase(name)) return true;
		return false;
	}

	/**
	 * Liefert den Anzeigenamen der Verteilung
	 * @return	Anzeigename der Verteilung
	 */
	public final String getName() {
		return getNames()[0];
	}

	/**
	 * Liefert das Thumbnail für diese Verteilung
	 * @return	Thumbnail oder <code>null</code>, wenn kein Thumbnail existiert
	 */
	public final ImageIcon getThumbnailImage() {
		final String fileName=getThumbnailImageName();
		if (fileName==null) return null;
		final URL imgURL=AbstractDistributionWrapper.class.getResource("res/"+fileName);
		if (imgURL==null) return null;
		return new ImageIcon(imgURL);
	}

	/**
	 * Liefert ein Objekt vom Typ {@link DistributionWrapperInfo}, welche weitere Daten zu der Verteilung enthält
	 * @param distribution	Verteilung deren Daten ausgegeben werden sollen (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @return	Zusatzdaten
	 */
	protected abstract DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution);

	/**
	 * Liefert ein Objekt vom Typ {@link DistributionWrapperInfo}, welche weitere Daten zu der Verteilung enthält
	 * @param distribution	Verteilung deren Daten ausgegeben werden sollen
	 * @return	Zusatzdaten oder <code>null</code>, wenn dieser Wrapper nicht zu der angegebenen Verteilung passt
	 */
	public final DistributionWrapperInfo getInfo(final AbstractRealDistribution distribution) {
		if (!isForDistribution(distribution)) return null;
		return getInfoInt(distribution);
	}

	/**
	 * Kann eine Verteilung direkt erstellt werden, sofern Erwartungswert und Standardabweichung direkt gesetzt werden können?
	 * @return	Kann eine Verteilung direkt erstellt werden, sofern Erwartungswert und Standardabweichung direkt gesetzt werden können?
	 * @see #canBuildDistributionFromMeanAndSD
	 * @see #canSetMean
	 * @see #canSetStandardDeviation
	 */
	protected boolean canBuildDistributionDirect() {
		return true;
	}

	/**
	 * Versucht eine Verteilung vom vorliegenden Typ aus angegebenem Erwartungswert und angegebener Standardabweichung zu erstellen
	 * @param mean	Erwartungswert
	 * @param sd	Standardabweichung
	 * @return	Neue Verteilung oder <code>null</code>, wenn das für den aktuellen Typ so nicht möglich ist
	 */
	public abstract AbstractRealDistribution getDistribution(final double mean, final double sd);

	/**
	 * Erstellt eine Verteilung mit auf jeden Fall gültigen Werten
	 * @return	Neue Verteilung
	 */
	public abstract AbstractRealDistribution getDefaultDistribution();

	/**
	 * Erstellt aus Erwartungswert und Standardabweichung eine Verteilung für eine Verteilungsanpassung.<br>
	 * Im Gegensatz zu {@link AbstractDistributionWrapper#getDistribution(double, double)} kann diese Funktion auch
	 * dann <code>null</code> liefern, wenn zwar eine Verteilung erstellt werden könnte, dies für einen Fit mit
	 * diesen Parametern nicht sinnvoll ist. Im Normalfall entspricht diese Funktion
	 * {@link AbstractDistributionWrapper#getDistribution(double, double)}.
	 * @param mean	Erwartungswert
	 * @param sd	Standardabweichung
	 * @param min	Minimal aufgetretener Messwert (muss vom Fitter nicht zwingend berücksichtigt werden)
	 * @param max	Maximal aufgetretener Messwert (muss vom Fitter nicht zwingend berücksichtigt werden)
	 * @return	Neue Verteilung oder <code>null</code>, wenn das so nicht möglich oder nicht sinnvoll ist
	 */
	public AbstractRealDistribution getDistributionForFit(final double mean, final double sd, final double min, final double max) {
		return getDistribution(mean,sd);
	}

	/**
	 * Liefert den Erwartungswert der Verteilung
	 * @param distribution	Verteilung
	 * @return	Erwartungswert
	 */
	public double getMean(final AbstractRealDistribution distribution) {
		return distribution.getNumericalMean();
	}

	/**
	 * Wird vom Konstruktor aufgerufen und gibt an, ob der Erwartungswert exakt eingestellt werden kann (oder ob z.B. auf Ganzzahlen gerundet wird)
	 * @return	Erwartungswert ist exakt einstellbar
	 */
	protected boolean canSetMeanExact() {
		return canSetMean;
	}

	/**
	 * Stellt den Erwartungswert der Verteilung ein
	 * @param distribution	Ausgangsverteilung bei der der Erwartungswert verändert werden soll (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @param mean	Neuer Erwartungswert
	 * @return	Neue Verteilung auf Basis der Ausgangsverteilung und dem neuen Erwartungswert
	 */
	protected abstract AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean);

	/**
	 * Stellt den Erwartungswert der Verteilung ein
	 * @param distribution	Ausgangsverteilung bei der der Erwartungswert verändert werden soll
	 * @param mean	Neuer Erwartungswert
	 * @return	Neue Verteilung auf Basis der Ausgangsverteilung und dem neuen Erwartungswert
	 */
	public final AbstractRealDistribution setMean(final AbstractRealDistribution distribution, final double mean) {
		if (!isForDistribution(distribution)) return null;
		if (!canSetMean) return null;
		return setMeanInt(distribution,mean);
	}

	/**
	 * Liefert die Standardabweichung der Verteilung
	 * @param distribution	Verteilung
	 * @return	Standardabweichung
	 */
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return Math.sqrt(distribution.getNumericalVariance());
	}

	/**
	 * Wird vom Konstruktor aufgerufen und gibt an, ob die Standardabweichung exakt eingestellt werden kann (oder ob z.B. auf Ganzzahlen gerundet wird)
	 * @return	Standardabweichung  ist exakt einstellbar
	 */
	protected boolean canSetStandardDeviationExact() {
		return canSetStandardDeviation;
	}

	/**
	 * Stellt die Standardabweichung der Verteilung ein
	 * @param distribution	Ausgangsverteilung bei der die Standardabweichung verändert werden soll (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @param sd	Neue Standardabweichung
	 * @return	Neue Verteilung auf Basis der Ausgangsverteilung und der neuen Standardabweichung
	 */
	protected abstract AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd);

	/**
	 * Stellt die Standardabweichung der Verteilung ein
	 * @param distribution	Ausgangsverteilung bei der die Standardabweichung verändert werden soll
	 * @param sd	Neue Standardabweichung
	 * @return	Neue Verteilung auf Basis der Ausgangsverteilung und der neuen Standardabweichung
	 */
	public final AbstractRealDistribution setStandardDeviation(final AbstractRealDistribution distribution, final double sd) {
		if (!isForDistribution(distribution)) return null;
		if (!canSetStandardDeviation) return null;
		return setStandardDeviationInt(distribution,sd);
	}

	/**
	 * Liefert einen Parameter der Verteilung
	 * @param distribution	Verteilung deren Daten ausgelesen werden sollen (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @param nr	Nummer des Parameters (muss eine Zahl von 1 bis 4 sein)
	 * @return	Wert
	 */
	protected abstract double getParameterInt(final AbstractRealDistribution distribution, final int nr);

	/**
	 * Liefert einen Parameter der Verteilung
	 * @param distribution	Verteilung deren Daten ausgelesen werden sollen
	 * @param nr	Nummer des Parameters (muss eine Zahl von 1 bis 4 sein)
	 * @return	Wert
	 */
	public final double getParameter(final AbstractRealDistribution distribution, final int nr) {
		if (!isForDistribution(distribution)) return 0.0;
		if (nr<1 || nr>4) return 0.0;
		return getParameterInt(distribution,nr);
	}

	/**
	 * Stellt einen Parameter der Verteilung ein
	 * @param distribution	Ausgangsverteilung bei der ein Parameter verändert werden soll (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @param nr	Nummer des Parameters (muss eine Zahl von 1 bis 4 sein, wurde bereits geprüft)
	 * @param value	Wert des Parameters
	 * @return	Liefert im Erfolgsfall ein neues Verteilungsobjekt mit den neuen Daten
	 */
	protected abstract AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value);

	/**
	 * Stellt einen Parameter der Verteilung ein
	 * @param distribution	Ausgangsverteilung bei der ein Parameter verändert werden soll
	 * @param nr	Nummer des Parameters (muss eine Zahl von 1 bis 4 sein)
	 * @param value	Wert des Parameters
	 * @return	Liefert im Erfolgsfall ein neues Verteilungsobjekt mit den neuen Daten
	 */
	public final AbstractRealDistribution setParameter(final AbstractRealDistribution distribution, final int nr, final double value) {
		if (!isForDistribution(distribution)) return null;
		if (nr<1 || nr>4) return null;
		return setParameterInt(distribution,nr,value);
	}

	/**
	 * Liefert nur die Verteilungsparameter als String zurück
	 * @param distribution	Verteilung deren Daten geliefert werden sollen (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @return	Parameter der Verteilung als String
	 */
	protected abstract String getToStringData(final AbstractRealDistribution distribution);

	/**
	 * Wandelt eine Verteilung in eine Zeichenkette um
	 * @param distribution	Umzuwandelnde / zu speichernde Verteilung
	 * @return	Verteilungsdaten (Name und Parameter) als String
	 */
	public final String toString(final AbstractRealDistribution distribution) {
		if (!isForDistribution(distribution)) return null;
		final String name=getName();
		final String data=getToStringData(distribution);
		if (data==null) return name; else return name+" ("+data+")";
	}

	/**
	 * Versucht die Verteilung aus einem String zu laden
	 * @param data	String mit den Parametern der Verteilung
	 * @param maxXValue	Maximaler x-Wert (nur für empirische Verteilungen relevant)
	 * @return	Liefert im Erfolgsfall die neue Verteilung
	 */
	public abstract AbstractRealDistribution fromString(final String data, final double maxXValue);

	/**
	 * Zerlegt einen String in eine Reihe von Zahlenwerten
	 * @param data	Zeichenkette, die an ";"-Zeichen zerlegt werden sollen
	 * @return	Array aus Zahlen (ist im Fehlerfall ein leeres Array, aber nie <code>null</code>)
	 */
	protected final double[] getDoubleArray(final String data) {
		final String[] list=data.split(";");
		final double[] values=new double[list.length];
		for (int i=0;i<list.length;i++) {
			try {
				Double d=Double.valueOf(list[i]);
				values[i]=d;
			} catch (NumberFormatException e) {return new double[0];}
		}
		return values;
	}

	/**
	 * Erstellt eine Kopie der Verteilung
	 * @param distribution	Ausgangsverteilung (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @return	Kopierte Verteilung
	 */
	protected abstract AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution);

	/**
	 * Erstellt eine Kopie der Verteilung
	 * @param distribution	Ausgangsverteilung
	 * @return	Kopierte Verteilung
	 */
	public final AbstractRealDistribution clone(final AbstractRealDistribution distribution) {
		if (!isForDistribution(distribution)) return null;
		return cloneInt(distribution);
	}

	/**
	 * Vergleicht zwei Verteilungsobjekte
	 * @param distribution1	Erste zu vergleichende Verteilung (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @param distribution2	Zweite zu vergleichende Verteilung (dass die Verteilung vom passenden Typ ist, wurde schon geprüft)
	 * @return	Gibt an, ob die Verteilungen inhaltlich identisch sind
	 */
	protected abstract boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2);

	/**
	 * Vergleicht zwei Verteilungsobjekte
	 * @param distribution1	Erste zu vergleichende Verteilung
	 * @param distribution2	Zweite zu vergleichende Verteilung
	 * @return	Gibt an, ob die Verteilungen inhaltlich identisch sind
	 */
	public final boolean compare(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		if (distribution1==null && distribution2==null) return true;
		if (distribution1==null || distribution2==null) return false;
		if (!isForDistribution(distribution1)) return false;
		if (!isForDistribution(distribution2)) return false;
		return compareInt(distribution1,distribution2);
	}
}
