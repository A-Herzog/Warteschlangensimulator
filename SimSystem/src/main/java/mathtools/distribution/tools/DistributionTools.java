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

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import javax.swing.ImageIcon;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.AbstractDiscreteRealDistribution;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.NeverDistributionImpl;
import mathtools.distribution.swing.JDistributionPanel;

/**
 * Liefert verschiedene Informationen zu kontinuierlichen Verteilungen
 * @author Alexander Herzog
 * @version 2.3
 * @see AbstractRealDistribution
 */
public final class DistributionTools {
	/** Basis-URL für die Verteilungsanzeige-WebApp */
	public static String WebAppBaseURL="https://a-herzog.github.io/Distributions/?distribution=";

	/** Empirische Daten */
	public static String[] DistData=new String[]{"Empirische Daten"};

	/** Wikipedia-Seite Empirische Daten */
	public static String DistDataWikipedia="https://de.wikipedia.org/wiki/Empirische_Verteilungsfunktion";

	/** Niemals-Verteilung */
	public static String[] DistNever=new String[]{"Niemals","nie"};

	/** Wikipedia-Seite Niemals-Verteilung */
	public static String DistNeverWikipedia=null;

	/** Infotext zur Niemals-Verteilung */
	public static String DistNeverInfo=null;

	/** Punkt "unendlich" */
	public static String[] DistInfinite=new String[]{"unendlich"};

	/** Ein-Punkt-Verteilung */
	public static String[] DistPoint=new String[]{"Ein-Punkt-Verteilung"};

	/** Wikipedia-Seite Ein-Punkt-Verteilung */
	public static String DistPointWikipedia=null;

	/** Infotext zur Ein-Punkt-Verteilung */
	public static String DistPointInfo="<p>Bei der Ein-Punkt-Verteilung handelt es sich um <b>keine Wahrscheinlichkeitsverteilung im eigentlichen Sinne</b>. Während normale Wahrscheinlichkeitsverteilung stets einen (verschiedenen) zufälligen Wert liefern, liefert die Ein-Punkt-Verteilung stets den einen, eingestellten Wert zurück.</p>"+
			"<p>Das bedeutet, die Ein-Punkt-Verteilung ist <b>deterministisch.</p>"+
			"<p>Der Sinn der Ein-Punkt-Verteilung besteht darin, dass mit ihr konstante Bediendauern usw. modelliert werden können, ohne dabei von der Notation der Wahrscheinlichkeitsverteilungen abweichen zu müssen.</p>";

	/** Gleichverteilung */
	public static String[] DistUniform=new String[]{"Gleichverteilung"};

	/** Wikipedia-Seite Gleichverteilung */
	public static String DistUniformWikipedia="https://de.wikipedia.org/wiki/Stetige_Gleichverteilung";

	/** Infotext zur Gleichverteilung */
	public static String DistUniformInfo="<p>Die Gleichverteilung wird über den minimal möglichen Wert und den maximal möglichen Wert, den sie annehmen kann, parametrisiert. Der Erwartungswert ist dann genau der Mittelpunkt zwischen diesen beiden Werten.</p>"+
			"<p>Die Gleichverteilung ordnet jedem Wert zwischen dem Minimum und dem Maximum dieselbe Wahrscheinlichkeit zu.</p>"+
			"<p>Auch wenn dies auf den ersten Blick relativ nützlich erscheint, so unterliegen doch <b>fast keine realen Prozesse</b> einer Gleichverteilung.</p>"+
			"<p>Ein weiterer Nachteil besteht darin, dass die ansonsten üblichen Kenngrößen Erwartungswert und Standardabweichung nur indirekt über den Bereich der Verteilung eingestellt werden können.</p>";

	/** Exponentialverteilung */
	public static String[] DistExp=new String[]{"Exponentialverteilung"};

	/** Wikipedia-Seite Exponentialverteilung */
	public static String DistExpWikipedia="https://de.wikipedia.org/wiki/Exponentialverteilung";

	/** Infotext zur Exponentialverteilung */
	public static String DistExpInfo=
			"<p>Die Exponentialverteilung besitzt <b>nur einen Parameter</b>, der zugleich Erwartungswert und Standardabweichung festlegt. Dies bringt den Vorteil mit sich, dass nur der Erwartungswert aus den historischen Daten erhoben werden muss, um die Exponentialverteilung für die Modellierung verwenden zu können; der Nachteil besteht jedoch damit zugleich darin, dass auch nur der Erwartungswert eingestellt werden kann.</p>"+
					"<p>Bei der Exponentialverteilung besitzt die Standardabweichung immer denselben Wert wie der Erwartungswert. Damit ergibt sich <b>stets ein Variationskoeffizient von 1</b>.</p>"+
					"<p>Die Besonderheit der Exponentialverteilung besteht darin, dass die Wahrscheinlichkeit dafür, dass das zu betrachtete Ereignis (z.B. eine Kundenankunft) innerhalb der nächsten Minute eintritt, unabhängig davon ist, ob das letzte Ereignis dieser Art vor einer Minute oder einer Stunde eingetreten ist. Diese Eigenschaft wird auch <b>Gedächtnislosigkeit</b> genannt. Für die Abstände der Ankünfte von unabhängigen Kunden tritt dies üblicherweise zu. Für z.B. Bediendauern hingegen praktisch nie.</p>"+
					"<p>Folglich ist die Exponentialverteilung für die Modellierung von <b>Zwischenankunftszeiten</b> meist sehr gut geeignet. Für die Modellierung von Bediendauern und ähnlichen Größen sollte jedoch auf andere Wahrscheinlichkeitsverteilungen (wie z.B. die Log-Normalverteilung, die Gamma-Verteilung oder die Dreiecksverteilung) zurückgegriffen werden.</p>";

	/** Normalverteilung */
	public static String[] DistNormal=new String[]{"Normalverteilung"};

	/** Wikipedia-Seite Normalverteilung */
	public static String DistNormalWikipedia="https://de.wikipedia.org/wiki/Normalverteilung";

	/** Infotext zur Normalverteilung */
	public static String DistNormalInfo="<p>Die Normalverteilung ergibt sich als <b>theoretische Grenzverteilung</b> bei der Hintereinanderausführung vieler jeweils unabhängiger Verteilungen.</p>"+
			"<p>Die Parameter der Normalverteilung sind zugleich deren Erwartungswert und deren Standardabweichung. Dies hat zur Folge, dass die Kenngrößen der Normalverteilung (in praktisch jeder Software) sehr einfach eingestellt werden können.</p>"+
			"<p>Die Normalverteilung kann stets auch negative Werte annehmen. Daher eignet sich diese <b>eher nicht zur Modellierung von Zeitdauern</b>. Lognormalverteilung oder Gamma-Verteilung sind für diesen Zweck meist wesentlich besser geeignet. Auch diese können im Warteschlangensimulator direkt über Erwartungswert und Standardabweichung parametrisiert werden.</p>";

	/** Log-Normalverteilung */
	public static String[] DistLogNormal=new String[]{"Lognormalverteilung"};

	/** Wikipedia-Seite Log-Normalverteilung */
	public static String DistLogNormalWikipedia="https://de.wikipedia.org/wiki/Logarithmische_Normalverteilung";

	/** Infotext zur Log-Normalverteilung */
	public static String DistLogNormalInfo="<p>Die Lognormalverteilung eignet sich sehr gut zur Modellierung von Bediendauern und ähnlichen Zeitdauern. Im Warteschlangensimulator wird die Verteilung über <b>Erwartungswert und Standardabweichung</b> parametrisiert.</p>"+
			"<p>Können diese beiden Werte aus den historischen Daten abgeleitet werden, so eigent sich die Verteilung sehr gut zur Modellierung. Stehen derartige Werte nicht zur Verfügung, so kann ggf. auf die Dreiecksverteilung zurückgegriffen werden.</p>"+
			"<p>In <b>Tabellenkalkulationen</b> ist üblicherweise auch die Lognrmalverteilung hinterlegt. Allerdings erfolgt doch die Parametrisierung meist nicht über Erwartungswert und Standardabweichung. Manuelle Umrechnungen sind hier nötig (siehe Dokumentation des jeweiligen Programms).</p>";

	/** Erlang-Verteilung */
	public static String[] DistErlang=new String[]{"Erlang-Verteilung"};

	/** Wikipedia-Seite Erlang-Verteilung */
	public static String DistErlangWikipedia="https://de.wikipedia.org/wiki/Erlang-Verteilung";

	/** Infotext zur Erlang-Verteilung */
	public static String DistErlangInfo="<p>Die Erlang-Verteilung stellt einen Spezialfall der <b>Gamma-Verteilung</b> dar. Bei der Modellierung ist es daher fast immer sinnvoller, direkt die Gamma-Verteilung zu verwenden.</p>"+
			"<p>Von mathematischen Standpunkt her stellt die Erlang-Verteilung die Hintereinanderausführung mehrerer Exponentialverteilungen dar und ist daher in der Theorie von Bedeutung.</p>";

	/** Gamma-Verteilung */
	public static String[] DistGamma=new String[]{"Gamma-Verteilung"};

	/** Wikipedia-Seite Gamma-Verteilung */
	public static String DistGammaWikipedia="https://de.wikipedia.org/wiki/Gammaverteilung";

	/** Infotext zur Gamma-Verteilung */
	public static String DistGammaInfo="<p>Die Gamma-Verteilung eignet sich sehr gut zur Modellierung von Bediendauern und ähnlichen Zeitdauern. Im Warteschlangensimulator wird die Verteilung über <b>Erwartungswert und Standardabweichung</b> parametrisiert.</p>"+
			"<p>Können diese beiden Werte aus den historischen Daten abgeleitet werden, so eigent sich die Verteilung sehr gut zur Modellierung. Stehen derartige Werte nicht zur Verfügung, so kann ggf. auf die Dreiecksverteilung zurückgegriffen werden.</p>"+
			"<p>In <b>Tabellenkalkulationen</b> ist üblicherweise auch die Gamma-Verteilung hinterlegt. Allerdings erfolgt doch die Parametrisierung meist nicht über Erwartungswert und Standardabweichung. Manuelle Umrechnungen sind hier nötig (siehe Dokumentation des jeweiligen Programms).</p>";

	/** Beta-Verteilung */
	public static String[] DistBeta=new String[]{"Beta-Verteilung"};

	/** Wikipedia-Seite Beta-Verteilung */
	public static String DistBetaWikipedia="https://de.wikipedia.org/wiki/Beta-Verteilung";

	/** Cauchy-Verteilung */
	public static String[] DistCauchy=new String[]{"Cauchy-Verteilung"};

	/** Wikipedia-Seite Cauchy-Verteilung */
	public static String DistCauchyWikipedia="https://de.wikipedia.org/wiki/Cauchy-Verteilung";

	/** Log-Cauchy-Verteilung */
	public static String[] DistLogCauchy=new String[]{"Log-Cauchy-Verteilung"};

	/** Wikipedia-Seite Cauchy-Verteilung */
	public static String DistLogCauchyWikipedia="https://en.wikipedia.org/wiki/Log-Cauchy_distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Weibull-Verteilung */
	public static String[] DistWeibull=new String[]{"Weibull-Verteilung"};

	/** Wikipedia-Seite Weibull-Verteilung */
	public static String DistWeibullWikipedia="https://de.wikipedia.org/wiki/Weibull-Verteilung";

	/** Chi-Verteilung */
	public static String[] DistChi=new String[]{"Chi-Verteilung"};

	/** Wikipedia-Seite Chi-Verteilung */
	public static String DistChiWikipedia="https://en.wikipedia.org/wiki/Chi_distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Chi^2-Verteilung */
	public static String[] DistChiSquare=new String[]{"Chi^2-Verteilung"};

	/** Wikipedia-Seite Chi^2-Verteilung */
	public static String DistChiSquareWikipedia="https://de.wikipedia.org/wiki/Chi-Quadrat-Verteilung";

	/** F-Verteilung */
	public static String[] DistF=new String[]{"F-Verteilung"};

	/** Wikipedia-Seite F-Verteilung */
	public static String DistFWikipedia="https://de.wikipedia.org/wiki/F-Verteilung";

	/** Jonhson-SU-Verteilung */
	public static String[] DistJohnson=new String[]{"Jonhson-SU-Verteilung"};

	/** Wikipedia-Seite Jonhson-SU-Verteilung */
	public static String DistJohnsonWikipedia="https://en.wikipedia.org/wiki/Johnson%27s_SU-distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Dreiecksverteilung */
	public static String[] DistTriangular=new String[]{"Dreiecksverteilung"};

	/** Wikipedia-Seite Dreiecksverteilung */
	public static String DistTriangularWikipedia="https://de.wikipedia.org/wiki/Dreiecksverteilung";

	/** Infotext zur Dreiecksverteilung */
	public static String DistTriangularInfo="<p>Die Dreiecksverteilung kommt immer dann zum Einsatz, wenn <b>Erwartungswert und Standardabweichung nicht erhoben werden können</b>, d.h. wenn keine auswertbaren historischen Daten vorliegen. Bei der Dreiecksverteilung müssen stattdessen der kleinste je aufgetretene Wert (Minimum), der am häufigsten auftretende Wert (Modus) und größete je aufgetretene Wert (Maximum) eingestellt werden.</p>"+
			"<p>Diese von der sonst bei vielen Verteilungen üblichen Parametrisierung stark abweichende Vorgehensweise macht die Dreiecksverteilung für Fälle, in denen keine historischen Daten vorhanden sind, aus denen Erwartungswert und Standardabweichung erhoben werden können, interessant.</p>"+
			"<p>Das Problem bei der Abfrage der Parameter besteht darin, dass der <b>Modus</b>, d.h. der Wert an dem die Wahrscheinlichkeitsdichte am höchsten ist, angegeben werden muss. Dieser stimmt üblicherweise nicht mit dem Mittelwert der Daten überein.</p>";

	/** Trapezverteilung */
	public static String[] DistTrapezoid=new String[]{"Trapezverteilung"};

	/** Wikipedia-Seite Dreiecksverteilung */
	public static String DistTrapezoidWikipedia="https://de.wikipedia.org/wiki/Trapezverteilung";

	/** Pert-Verteilung */
	public static String[] DistPert=new String[]{"Pert-Verteilung"};

	/** Wikipedia-Seite Pert-Verteilung */
	public static String DistPertWikipedia="https://en.wikipedia.org/wiki/PERT_distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Laplace-Verteilung */
	public static String[] DistLaplace=new String[]{"Laplace-Verteilung"};

	/** Wikipedia-Seite Laplace-Verteilung */
	public static String DistLaplaceWikipedia="https://de.wikipedia.org/wiki/Laplace-Verteilung";

	/** Pareto-Verteilung */
	public static String[] DistPareto=new String[]{"Pareto-Verteilung"};

	/** Wikipedia-Seite Pareto-Verteilung */
	public static String DistParetoWikipedia="https://de.wikipedia.org/wiki/Pareto-Verteilung";

	/** Logistische Verteilung */
	public static String[] DistLogistic=new String[]{"Logistische Verteilung"};

	/** Wikipedia-Seite Logistische Verteilung */
	public static String DistLogisticWikipedia="https://de.wikipedia.org/wiki/Logistische_Verteilung";

	/** Inverse Gauß-Verteilung */
	public static String[] DistInverseGaussian=new String[]{"Inverse Gauß-Verteilung"};

	/** Wikipedia-Seite Inverse Gauß-Verteilung */
	public static String DistInverseGaussianWikipedia="https://de.wikipedia.org/wiki/Inverse_Normalverteilung";

	/** Rayleigh-Verteilung */
	public static String[] DistRayleigh=new String[]{"Rayleigh-Verteilung"};

	/** Wikipedia-Seite Rayleigh-Verteilung */
	public static String DistRayleighWikipedia="https://de.wikipedia.org/wiki/Rayleigh-Verteilung";

	/** Log-Logistische Verteilung */
	public static String[] DistLogLogistic=new String[]{"Log-Logistische Verteilung"};

	/** Wikipedia-Seite Log-Logistische Verteilung */
	public static String DistLogLogisticWikipedia="https://en.wikipedia.org/wiki/Log-logistic_distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Potenzverteilung */
	public static String[] DistPower=new String[]{"Potenzverteilung"};

	/** Wikipedia-Seite Potenzverteilung */
	public static String DistPowerWikipedia="https://en.wikipedia.org/wiki/Pareto_distribution#Inverse-Pareto_Distribution_/_Power_Distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Gumbel-Verteilung */
	public static String[] DistGumbel=new String[]{"Gumbel-Verteilung"};

	/** Wikipedia-Seite Gumbel-Verteilung */
	public static String DistGumbelWikipedia="https://de.wikipedia.org/wiki/Gumbel-Verteilung";

	/** Fatigue-Life-Verteilung */
	public static String[] DistFatigueLife=new String[]{"Fatigue-Life-Verteilung"};

	/** Wikipedia-Seite Fatigue-Life-Verteilung */
	public static String DistFatigueLifeWikipedia="https://en.wikipedia.org/wiki/Birnbaum%E2%80%93Saunders_distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Frechet-Verteilung */
	public static String[] DistFrechet=new String[]{"Frechet-Verteilung"};

	/** Wikipedia-Seite Frechet-Verteilung */
	public static String DistFrechetWikipedia="https://de.wikipedia.org/wiki/Frechet-Verteilung";

	/** Hyperbolische Sekanten-Verteilung */
	public static String[] DistHyperbolicSecant=new String[]{"Hyperbolische Sekanten-Verteilung"};

	/** Wikipedia-Seite Hyperbolische Sekanten-Verteilung */
	public static String DistHyperbolicSecantWikipedia="https://en.wikipedia.org/wiki/Hyperbolic_secant_distribution"; /* Keine deutsche Wikipedia-Seite vorhanden */

	/** Sägezahnverteilung (links) */
	public static String[] DistSawtoothLeft=new String[]{"Linke Sägezahn-Verteilung"};

	/** Wikipedia-Seite Sägezahnverteilung (links) */
	public static String DistSawtoothLeftWikipedia="https://de.wikipedia.org/wiki/Dreiecksverteilung";

	/** Sägezahnverteilung (rechts) */
	public static String[] DistSawtoothRight=new String[]{"Rechte Sägezahn-Verteilung"};

	/** Wikipedia-Seite Sägezahnverteilung (rechts) */
	public static String DistSawtoothRightWikipedia="https://de.wikipedia.org/wiki/Dreiecksverteilung";

	/** Levy-Verteilung */
	public static String[] DistLevy=new String[]{"Levy-Verteilung"};

	/** Wikipedia-Seite Levy-Verteilung */
	public static String DistLevyWikipedia="https://de.wikipedia.org/wiki/Levy-Verteilung";

	/** Maxwell-Boltzmann-Verteilung */
	public static String[] DistMaxwellBoltzmann=new String[]{"Maxwell-Boltzmann-Verteilung"};

	/** Wikipedia-Seite Maxwell-Boltzmann-Verteilung */
	public static String DistMaxwellBoltzmannWikipedia="https://de.wikipedia.org/wiki/Maxwell-Boltzmann-Verteilung";

	/** Studentsche t-Verteilung */
	public static String[] DistStudentT=new String[] {"Studentsche t-Verteilung"};

	/** Wikipedia-Seite Studentsche t-Verteilung */
	public static String DistStudentTWikipedia="https://de.wikipedia.org/wiki/Studentsche_t-Verteilung";

	/** Hypergeometrische Verteilung */
	public static String[] DistHyperGeom=new String[]{"Hypergeometrische Verteilung"};

	/** Wikipedia-Seite Hypergeometrische Verteilung */
	public static String DistHyperGeomWikipedia="https://de.wikipedia.org/wiki/Hypergeometrische_Verteilung";

	/** Binomialverteilung */
	public static String[] DistBinomial=new String[]{"Binomialverteilung"};

	/** Wikipedia-Seite Binomialverteilung */
	public static String DistBinomialWikipedia="https://de.wikipedia.org/wiki/Binomialverteilung";

	/** Poisson-Verteilung */
	public static String[] DistPoisson=new String[]{"Poisson-Verteilung"};

	/** Wikipedia-Seite Poisson-Verteilung */
	public static String DistPoissonWikipedia="https://de.wikipedia.org/wiki/Poisson-Verteilung";

	/** Negative Binomialverteilung */
	public static String[] DistNegativeBinomial=new String[]{"Negative Binomialverteilung"};

	/** Wikipedia-Seite Negative Binomialverteilung */
	public static String DistNegativeBinomialWikipedia="https://de.wikipedia.org/wiki/Negative_Binomialverteilung";

	/** Negative Hypergeometrische Verteilung */
	public static String[] DistNegativeHyperGeom=new String[]{"Negative hypergeometrische Verteilung"};

	/** Wikipedia-Seite Negative Hypergeometrische Verteilung */
	public static String DistNegativeHyperGeomWikipedia="https://de.wikipedia.org/wiki/Negative_hypergeometrische_Verteilung";

	/** Zeta-Verteilung */
	public static String[] DistZeta=new String[]{"Zeta-Verteilung"};

	/** Wikipedia-Seite Zeta-Verteilung */
	public static String DistZetaWikipedia="https://de.wikipedia.org/wiki/Zeta-Verteilung";

	/** Diskrete Gleichverteilung */
	public static String[] DistDiscreteUniform=new String[]{"Diskrete Gleichverteilung"};

	/** Wikipedia-Seite Diskrete Gleichverteilung */
	public static String DistDiscreteUniformWikipedia="https://de.wikipedia.org/wiki/Diskrete_Gleichverteilung";

	/** Geometrische Verteilung */
	public static String[] DistGeometric=new String[]{"Geometrische Verteilung"};

	/** Wikipedia-Seite Geometrische Verteilung */
	public static String DistGeometricWikipedia="https://de.wikipedia.org/wiki/Geometrische_Verteilung";

	/** Logarithmische Verteilung */
	public static String[] DistLogarithmic=new String[]{"Logarithmische Verteilung"};

	/** Wikipedia-Seite Logarithmische Verteilung */
	public static String DistLogarithmicWikipedia="https://de.wikipedia.org/wiki/Logarithmische_Verteilung";

	/** Borel-Verteilung */
	public static String[] DistBorel=new String[]{"Borel-Verteilung"};

	/** Wikipedia-Seite Borel-Verteilung */
	public static String DistBorelWikipedia="https://en.wikipedia.org/wiki/Borel_distribution"; /* leide keine Wikipedia-Seite vorhanden */

	/** Halbe Normalverteilung */
	public static String[] DistHalfNormal=new String[]{"Halbe Normalverteilung"};

	/** Wikipedia-Seite halbe Normalverteilung */
	public static String DistHalfNormalWikipedia="https://de.wikipedia.org/wiki/Normalverteilung";

	/** U-quadratische Verteilung */
	public static String[] DistUQuadratic=new String[]{"U-quadratische Verteilung"};

	/** Wikipedia-Seite U-quadratische Verteilung */
	public static String DistUQuadraticWikipedia="https://en.wikipedia.org/wiki/U-quadratic_distribution";

	/** Reziproke Verteilung */
	public static String[] DistReciprocal=new String[]{"Reziproke Verteilung"};

	/** Wikipedia-Seite Reziproke Verteilung */
	public static String DistReciprocalWikipedia="https://en.wikipedia.org/wiki/Reciprocal_distribution";

	/** Kumaraswamy-Verteilung */
	public static String[] DistKumaraswamy=new String[]{"Kumaraswamy-Verteilung"};

	/** Wikipedia-Seite Kumaraswamy-Verteilung */
	public static String DistKumaraswamyWikipedia="https://en.wikipedia.org/wiki/Kumaraswamy_distribution";

	/** Irwin-Hall Verteilung */
	public static String[] DistIrwinHall=new String[]{"Irwin-Hall-Verteilung"};

	/** Wikipedia-Seite IrwinHall-Verteilung */
	public static String DistIrwinHallWikipedia="https://de.wikipedia.org/wiki/Irwin-Hall-Verteilung";

	/** Sinus-Verteilung */
	public static String[] DistSine=new String[]{"Sinus-Verteilung"};

	/** Wikipedia-Seite Sine-Verteilung */
	public static String DistSineWikipedia=""; /* leide keine Wikipedia-Seite vorhanden */

	/** Arcus Sinus-Verteilung */
	public static String[] DistArcsine=new String[]{"Arcus Sinus-Verteilung"};

	/** Wikipedia-Seite Arcus Sine-Verteilung */
	public static String DistArcsineWikipedia=""; /* leide keine Wikipedia-Seite vorhanden */

	/** Wigner Halbkreis-Verteilung */
	public static String[] DistWignerHalfCircle=new String[]{"Wigner Halbkreis-Verteilung"};

	/** Wikipedia-Seite  Wigner Halbkreis-Verteilung */
	public static String DistWignerHalfCircleWikipedia="https://en.wikipedia.org/wiki/Wigner_semicircle_distribution";

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

	/** Bezeichner "Inverse Skalierung" */
	public static String DistInverseScale="Inverse Skalierung";

	/** Bezeichner "Am wahrscheinlichsten" (für Dreiecksverteilung) */
	public static String DistMostLikely="Am wahrscheinlichsten";

	/** Bezeichner "Freiheitsgrade" */
	public static String DistDegreesOfFreedom="Freiheitsgrade";

	/** Bezeichner "Mittelwert" */
	public static String DistMean="Mittelwert";

	/** Wikipedia-Seite zum Mittelwert */
	public static String DistMeanWikipedia="https://de.wikipedia.org/wiki/Mittelwert";

	/** Bezeichner "Standardabweichung" */
	public static String DistStdDev="Standardabweichung";

	/** Wikipedia-Seite zur Standardabweichung*/
	public static String DistStdDevWikipedia="https://de.wikipedia.org/wiki/Standardabweichung";

	/** Bezeichner "Variationskoeffizient" */
	public static String DistCV="Variationskoeffizient";

	/** Wikipedia-Seite zum Variationskoeffizient */
	public static String DistCVWikipedia="https://de.wikipedia.org/wiki/Variationskoeffizient";

	/** Bezeichner "Schiefe" */
	public static String DistSkewness="Schiefe";

	/** Wikipedia-Seite zur Schiefe */
	public static String DistSkewnessWikipedia="https://de.wikipedia.org/wiki/Schiefe_(Statistik)";

	/** Bezeichner "Modus" */
	public static String DistMode="Modus";

	/** Wikipedia-Seite zum Modus */
	public static String DistModeWikipedia="https://de.wikipedia.org/wiki/Modus_(Statistik)";

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
				new WrapperTrapezoidDistribution(),
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
				new WrapperStudentTDistribution(),
				new WrapperHyperGeomDistribution(),
				new WrapperBinomialDistribution(),
				new WrapperPoissonDistribution(),
				new WrapperNegativeBinomialDistribution(),
				new WrapperNegativeHyperGeomDistribution(),
				new WrapperZetaDistribution(),
				new WrapperDiscreteUniformDistribution(),
				new WrapperLogarithmicDistribution(),
				new WrapperHalfNormalDistribution(),
				new WrapperGeometricDistribution(),
				new WrapperUQuadraticDistribution(),
				new WrapperReciprocalDistribution(),
				new WrapperKumaraswamyDistribution(),
				new WrapperIrwinHallDistribution(),
				new WrapperSineDistribution(),
				new WrapperArcsineDistribution(),
				new WrapperWignerHalfCircleDistribution(),
				new WrapperLogCauchyDistribution(),
				new WrapperBorelDistribution()
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
		if (distribution==null) return null;
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
	 * Liefert den Link zur Wikipedia-Seite zu einer Verteilung.
	 * @param distribution Verteilungsobjekt, zu dem der Wikipedia-Link geliefert werden soll
	 * @return	URL zur Wikipedia-Seite zu der Verteilung (oder <code>null</code>, wenn keine passende Adresse vorliegt)
	 */
	public static URI getDistributionWikipediaLink(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper==null) return null;
		String url=wrapper.getWikipediaURL();
		if (url==null || url.isBlank()) return null;
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Liefert den Link zur Wahrscheinlichkeitsverteilungen-Webapp-Seite zu einer Verteilung.
	 * @param distribution Verteilungsobjekt, zu dem der Wahrscheinlichkeitsverteilungen-Webapp-Link geliefert werden soll
	 * @return	URL zur Wahrscheinlichkeitsverteilungen-Webapp-Seite zu der Verteilung (oder <code>null</code>, wenn keine passende Adresse vorliegt)
	 */
	public static URI getDistributionWebAppLink(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper==null) return null;
		String url=wrapper.getWebAppDistributionName();
		if (url==null || url.isBlank()) return null;
		try {
			return new URI(WebAppBaseURL+url);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Liefert einen HTML-formatierten Infotext zu dem Verteilungstyp.
	 * @param distribution	Verteilungsobjekt, zu dem der Infotext geliefert werden soll
	 * @return	HTML-formatierter Infotext (oder <code>null</code>, wenn kein Infotext vorhanden ist)
	 */
	public static String getDistributionInfoHTML(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper==null) return null;
		return wrapper.getInfoHTML();
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
		if (wrapper!=null) try {
			return wrapper.setMean(distribution,value);
		} catch(Exception e) {
			return null;
		}
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
	 * Gibt an, ob die Standardabweichung ganz exakt und unabhängig vom Erwartungswert eingestellt werden kann.
	 * @param distribution	Zu prüfende Verteilung
	 * @return	Gibt <code>true</code> zurück, wenn die Standardabweichung direkt exakt eingestellt werden kann
	 * @see DistributionTools#canSetStandardDeviationExact(AbstractRealDistribution)
	 */
	public static boolean canSetStandardDeviationExactIndependent(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper==null || !wrapper.canSetStandardDeviationExact) return false;

		final double mean=distribution.getNumericalMean();
		final double sd=wrapper.getStandardDeviation(distribution);
		final AbstractRealDistribution newDistribution=wrapper.setStandardDeviation(distribution,sd+1);
		return newDistribution.getNumericalMean()==mean && wrapper.getStandardDeviation(newDistribution)==sd+1;
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
		if (wrapper!=null) try {
			return wrapper.setStandardDeviation(distribution,value);
		} catch(Exception e) {
			return null;
		}
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
	 * Liefert den zu der Verteilung passenden Rechenausdruck.
	 * @param distribution	Verteilung
	 * @return	Rechenausdruck oder <code>null</code>, wenn zu der Verteilung kein Rechenausdruck ermittelt werden konnte
	 */
	public static String getCalculationExpression(final AbstractRealDistribution distribution) {
		final AbstractDistributionWrapper wrapper=getWrapper(distribution);
		if (wrapper!=null) return wrapper.getCalcExpression(distribution);
		return null;
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

	/**
	 * Erzeugt eine Wertetabelle für eine Verteilung
	 * @param distribution	Verteilung
	 * @return	Wertetabelle
	 */
	public static Table getTableOfValues(final AbstractRealDistribution distribution) {
		final Table table=new Table();

		if (distribution instanceof AbstractDiscreteRealDistribution) {
			/* Diskrete Verteilung */
			table.addLine(new String[]{"k","P(X=k)","P(X<=k)"});
			double sumLast=0;
			for (int k=0;k<10_000;k++) {
				final double sum=((AbstractDiscreteRealDistribution)distribution).cumulativeProbability(k);
				table.addLine(new String[]{
						""+k,
						NumberTools.formatNumberMax(sum-sumLast),
						NumberTools.formatNumberMax(sum)
				});
				sumLast=sum;
				if (sum>0.999) break;
			}
		} else {
			/* Kontinuierliche Verteilung */
			table.addLine(new String[]{"x","f(x)","F(x)=P(X<=x)"});
			final double min=Math.max(-10_000,distribution.getSupportLowerBound());
			final double max=Math.min(10_000,distribution.getSupportUpperBound());
			final double step=0.1;
			double x=min;
			while (x<=max) {
				final double f=distribution.density(x);
				final double F=distribution.cumulativeProbability(x);
				if (F>=0.001) table.addLine(new String[]{
						NumberTools.formatNumberMax(x),
						NumberTools.formatNumberMax(f),
						NumberTools.formatNumberMax(F)
				});
				if (F>0.999) break;
				x+=step;
				x=Math.round(x*1000)/1000.0;
			}
		}

		return table;
	}

	/**
	 * Erzeugt eine Wertetabelle für eine Verteilung und kopiert diese in die Zwischenablage.
	 * @param distribution	Verteilung
	 * @see #getTableOfValues(AbstractRealDistribution)
	 */
	public static void copyTableOfValues(final AbstractRealDistribution distribution) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getTableOfValues(distribution).toString()),null);
	}

	/**
	 * Erzeugt und speichert eine Wertetabelle für eine Verteilung.
	 * @param parent	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param distribution	Verteilung
	 * @see #getTableOfValues(AbstractRealDistribution)
	 */
	public static void saveTableOfValues(final Component parent, final AbstractRealDistribution distribution) {
		final File file=Table.showSaveDialog(parent,JDistributionPanel.SaveButtonTable);
		if (file==null) return;
		getTableOfValues(distribution).save(file);
	}
}