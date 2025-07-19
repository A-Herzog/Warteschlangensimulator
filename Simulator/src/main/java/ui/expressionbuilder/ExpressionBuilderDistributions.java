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
package ui.expressionbuilder;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import mathtools.NumberTools;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbol;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbolType;

/**
 * F�gt die Rechenbefehle f�r Wahrscheinlichkeitsverteilungen in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderDistributions {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderDistributions#build(DefaultMutableTreeNode, List, String)} zur Verf�gung.
	 */
	private ExpressionBuilderDistributions() {}

	/**
	 * Erstellt einen neuen Eintrag f�r die Baumstruktur (f�gt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol f�r den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgew�hlt wird
	 * @return	Neuer Eintrag f�r die Baumstruktur
	 * @see #addTreeNode(DefaultMutableTreeNode, String, String, String, String)
	 */
	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_DISTRIBUTION));
	}

	/**
	 * F�gt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugef�gt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol f�r den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgew�hlt wird
	 */
	private static void addTreeNode(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(name,symbol,description));
		}
	}

	/**
	 * F�gt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugef�gt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param param	Parameter der Verteilung
	 * @param langName	Ausgeschriebener Name des Eintrags
	 * @param langPDF	Anzuzeigende Beschreibung f�r den Dichte-Eintrag
	 * @param langCDF	Anzuzeigende Beschreibung f�r den Verteilungsfunktion-Eintrag
	 * @param langRND	Anzuzeigende Beschreibung f�r den Zufallszahl-Eintrag
	 */
	private static void addDist(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String param, final String langName, final String langPDF, final String langCDF, final String langRND) {
		final String pdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.Density");
		final String cdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.DistributionFunction");
		final String rnd=Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomNumber");
		final String value=Language.tr("ExpressionBuilder.Value");

		final DefaultMutableTreeNode sub=new DefaultMutableTreeNode(langName);

		addTreeNode(
				sub,
				filterUpper,
				langName+", "+pdf+" ("+name+")",
				name+"("+value+";"+param+";0)",
				langPDF);
		addTreeNode(
				sub,
				filterUpper,
				langName+", "+cdf+" ("+name+")",
				name+"("+value+";"+param+";1)",
				langCDF);
		addTreeNode(
				sub,
				filterUpper,
				langName+", "+rnd+" ("+name+")",
				name+"("+param+")",
				langRND);

		if (sub.getChildCount()>0) group.add(sub);
	}

	/**
	 * F�gt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugef�gt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param param	Parameter der Verteilung
	 * @param langName	Ausgeschriebener Name des Eintrags
	 * @param langPDF	Anzuzeigende Beschreibung f�r den Dichte-Eintrag
	 * @param langCDF	Anzuzeigende Beschreibung f�r den Verteilungsfunktion-Eintrag
	 * @param langRND	Anzuzeigende Beschreibung f�r den Zufallszahl-Eintrag
	 * @param langRNDRange	Anzuzeigende Beschreibung f�r den Zufallszahl(begrenzter Bereich)-Eintrag
	 */
	private static void addDist(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String param, final String langName, final String langPDF, final String langCDF, final String langRND, final String langRNDRange) {
		final String pdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.Density");
		final String cdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.DistributionFunction");
		final String rnd=Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomNumber");
		final String rndRange=Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomNumberRange");
		final String value=Language.tr("ExpressionBuilder.Value");

		final DefaultMutableTreeNode sub=new DefaultMutableTreeNode(langName);

		addTreeNode(
				sub,
				filterUpper,
				langName+", "+pdf+" ("+name+")",
				name+"("+value+";"+param+";0)",
				langPDF);
		addTreeNode(
				sub,
				filterUpper,
				langName+", "+cdf+" ("+name+")",
				name+"("+value+";"+param+";1)",
				langCDF);
		addTreeNode(
				sub,
				filterUpper,
				langName+", "+rnd+" ("+name+")",
				name+"("+param+")",
				langRND);
		addTreeNode(
				sub,
				filterUpper,
				langName+", "+rndRange+" ("+name+"Range)",
				name+"Range(min;max;"+param+")",
				langRNDRange);

		if (sub.getChildCount()>0) group.add(sub);
	}

	/**
	 * F�gt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugef�gt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param param	Parameter der Verteilung
	 * @param langName	Ausgeschriebener Name des Eintrags
	 * @param langPDF	Anzuzeigende Beschreibung f�r den Dichte-Eintrag
	 * @param langRND	Anzuzeigende Beschreibung f�r den Zufallszahl-Eintrag
	 */
	private static void addDiscreteDist(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String param, final String langName, final String langPDF, final String langRND) {
		final String pdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.Density");
		final String rnd=Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomNumber");
		final String value=Language.tr("ExpressionBuilder.Value");

		final DefaultMutableTreeNode sub=new DefaultMutableTreeNode(langName);

		addTreeNode(
				sub,
				filterUpper,
				langName+", "+pdf+" ("+name+")",
				name+"("+value+";"+param+")",
				langPDF);
		addTreeNode(
				sub,
				filterUpper,
				langName+", "+rnd+" ("+name+")",
				name+"("+param+")",
				langRND);

		if (sub.getChildCount()>0) group.add(sub);
	}

	/**
	 * Erstellt eine Liste mit allen diskreten Verteilungen.
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @return	Allen diskreten Verteilungen
	 */
	private static DefaultMutableTreeNode buildDiscrete(final String filterUpper) {
		final DefaultMutableTreeNode group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions"));

		/* Geometrische Verteilung */

		addDiscreteDist(group,filterUpper,"GeometricDist","p",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GeometricDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GeometricDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GeometricDistribution.RandomNumberInfo"));

		/* Hypergeometrische Verteilung */

		addDiscreteDist(group,filterUpper,"HypergeometricDist","N;K;n",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HypergeometricDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HypergeometricDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HypergeometricDistribution.RandomNumberInfo"));

		/* Binomial-Verteilung */

		addDiscreteDist(group,filterUpper,"BinomialDist","n;p",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BinomialDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BinomialDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BinomialDistribution.RandomNumberInfo"));

		/* Binomial-Verteilung - Direkt */

		addDiscreteDist(group,filterUpper,"BinomialDistDirect","mean;sd",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BinomialDistributionDirect"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BinomialDistributionDirect.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BinomialDistributionDirect.RandomNumberInfo"));

		/* Poisson-Verteilung */

		addDiscreteDist(group,filterUpper,"PoissonDist","l",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PoissonDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PoissonDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PoissonDistribution.RandomNumberInfo"));

		/* Zeta-Verteilung */

		addDiscreteDist(group,filterUpper,"ZetaDist","s",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ZetaDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ZetaDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ZetaDistribution.RandomNumberInfo"));

		/* Negative Binomial-Verteilung */

		addDiscreteDist(group,filterUpper,"NegativeBinomialDist","r;p",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeBinomialDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeBinomialDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeBinomialDistribution.RandomNumberInfo"));

		/* Negative Binomial-Verteilung - Direkt */

		addDiscreteDist(group,filterUpper,"NegativeBinomialDistDirect","mean;sd",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeBinomialDistributionDirect"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeBinomialDistributionDirect.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeBinomialDistributionDirect.RandomNumberInfo"));

		/* Negative Hypergeometrische Verteilung */

		addDiscreteDist(group,filterUpper,"NegativeHypergeometricDist","N;K;n",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeHypergeometricDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeHypergeometricDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NegativeHypergeometricDistribution.RandomNumberInfo"));

		/* Diskrete Gleichverteilung */

		addDiscreteDist(group,filterUpper,"DiscreteUniformDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.DiscreteUniformDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.DiscreteUniformDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.DiscreteUniformDist.RandomNumberInfo"));

		/* Logarithmische Verteilung */

		addDiscreteDist(group,filterUpper,"LogarithmicDist","p",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogarithmicDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogarithmicDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogarithmicDist.RandomNumberInfo"));

		/* Borel-Verteilung */

		addDiscreteDist(group,filterUpper,"BorelDist","mu",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BorelDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BorelDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BorelDist.RandomNumberInfo"));

		/* Planck-Verteilung */

		addDiscreteDist(group,filterUpper,"PlanckDist","l",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PlanckDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PlanckDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PlanckDistribution.RandomNumberInfo"));

		return group;

	}

	/**
	 * Erstellt eine Liste mit allen kontinuierlichen Verteilungen.
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @return	Allen kontinuierlichen Verteilungen
	 */
	private static DefaultMutableTreeNode buildContinous(final String filterUpper) {
		final DefaultMutableTreeNode group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions"));

		/* Exponentialverteilung */

		addDist(group,filterUpper,"ExpDist","mean",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution.DensityInfo"),NumberTools.formatNumberMax(0.04104249931195)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.9179150013761)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution.RandomNumberRangeInfo"));

		/* Gleichverteilung */

		addDist(group,filterUpper,"UniformDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution.DensityInfo"),NumberTools.formatNumberMax(2.5),NumberTools.formatNumberMax(0.5)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(2.5),NumberTools.formatNumberMax(0.25)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution.RandomNumberInfo"));

		/* Normalverteilung */

		addDist(group,filterUpper,"NormalDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution.DensityInfo"),NumberTools.formatNumberMax(0.10648266850745)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.74750746245308)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution.RandomNumberRangeInfo"));

		/* Lognormalverteilung */

		addDist(group,filterUpper,"LogNormalDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution.DensityInfo"),NumberTools.formatNumberMax(3.40712431368569)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.81166409548103)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution.RandomNumberRangeInfo"));

		/* Gamma-Verteilung */

		addDist(group,filterUpper,"GammaDist","alpha;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution.DensityInfo"),NumberTools.formatNumberMax(0.03992278718149)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.087695308502)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution.RandomNumberRangeInfo"));

		/* Gamma-Verteilung - Direkt */

		addDist(group,filterUpper,"GammaDistDirect","mean;sd",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistributionDirect"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistributionDirect.DensityInfo"),NumberTools.formatNumberMax(0.07727661686311)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistributionDirect.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.78517079628442)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistributionDirect.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistributionDirect.RandomNumberRangeInfo"));

		/* Erlang-Verteilung */

		addDist(group,filterUpper,"ErlangDist","n;lambda",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution.DensityInfo"),NumberTools.formatNumberMax(0.03992278718149)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.087695308502)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution.RandomNumberRangeInfo"));

		/* Inverse Gamma-Verteilung */

		addDist(group,filterUpper,"InverseGammaDist","alpha;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGammaDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGammaDistribution.DensityInfo"),NumberTools.formatNumberMax(0.03992278718149)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGammaDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.087695308502)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGammaDistribution.RandomNumberInfo"));

		/* Beta-Verteilung */

		addDist(group,filterUpper,"BetaDist","a;b;alpha;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution.RandomNumberInfo"));

		/* Beta-Verteilung - Direkt */

		addDist(group,filterUpper,"BetaDistDirect","a;b;mean;sd",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistributionDirect"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistributionDirect.DensityInfo"),NumberTools.formatNumberMax(1.31344828230159)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistributionDirect.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.81942684533674)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistributionDirect.RandomNumberInfo"));

		/* Weibull-Verteilung */

		addDist(group,filterUpper,"WeibullDist","scale;form;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution.RandomNumberRangeInfo"));

		/* Cauchy-Verteilung */

		addDist(group,filterUpper,"CauchyDist","mean;form",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution.RandomNumberRangeInfo"));

		/* Chi�-Verteilung */

		addDist(group,filterUpper,"ChiSquareDist","n",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution.RandomNumberRangeInfo"));

		/* Chi-Verteilung */

		addDist(group,filterUpper,"ChiDist","n",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution.RandomNumberRangeInfo"));

		/* F-Verteilung */

		addDist(group,filterUpper,"FDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution.RandomNumberRangeInfo"));

		/* Johnson-SU-Verteilung */

		addDist(group,filterUpper,"JohnsonSUDist","gamma;xi;delta;lambda",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution.RandomNumberRangeInfo"));

		/* Dreiecksverteilung */

		addDist(group,filterUpper,"TriangularDist","lower;mostLikely;upper",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution.RandomNumberInfo"));

		/* Trapezverteilung */

		addDist(group,filterUpper,"TrapezoidDist","a;b;c;d",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TrapezoidDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TrapezoidDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TrapezoidDist.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TrapezoidDist.RandomNumberInfo"));

		/* Pert-Verteilung */

		addDist(group,filterUpper,"PertDist","lower;mostLikely;upper",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PertDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PertDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PertDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PertDistribution.RandomNumberInfo"));

		/* Laplace-Verteilung */

		addDist(group,filterUpper,"LaplaceDist","mu;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution.RandomNumberRangeInfo"));

		/* Pareto-Verteilung */

		addDist(group,filterUpper,"ParetoDist","xmin;alpha",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ParetoDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ParetoDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ParetoDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ParetoDistribution.RandomNumberInfo"));

		/* Logistische Verteilung */

		addDist(group,filterUpper,"LogisticDist","mu;s",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogisticDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogisticDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogisticDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogisticDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogisticDistribution.RandomNumberRangeInfo"));

		/* Inverse Gau�-Verteilung */

		addDist(group,filterUpper,"InverseGaussianDist","lambda;mu",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution.RandomNumberRangeInfo"));

		/* Rayleigh-Verteilung */

		addDist(group,filterUpper,"RayleighDist","mu",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution.RandomNumberRangeInfo"));

		/* Log-logistische Verteilung */

		addDist(group,filterUpper,"LogLogisticDist","alpha;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution.RandomNumberRangeInfo"));

		/* Potenzverteilung */

		addDist(group,filterUpper,"PowerDist","a;b;c",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution.RandomNumberInfo"));

		/* Gumbel-Verteilung */

		addDist(group,filterUpper,"GumbelDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution.RandomNumberRangeInfo"));

		/* Gumbel-Verteilung - Direkt */

		addDist(group,filterUpper,"GumbelDist","mean;sd",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistributionDirect"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistributionDirect.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistributionDirect.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistributionDirect.RandomNumberInfo"));

		/* Fatigue-Life-Verteilung */

		addDist(group,filterUpper,"FatigueLifeDist","mu;beta;gamma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution.RandomNumberRangeInfo"));

		/* Frechet-Verteilung */

		addDist(group,filterUpper,"FrechetDist","delta;beta;alpha",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution.RandomNumberRangeInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution.RandomNumberInfo"));

		/* Hyperbolische Sekanten-Verteilung */

		addDist(group,filterUpper,"HyperbolicSecantDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution.RandomNumberRangeInfo"));

		/* Linke S�gezahn-Verteilung */

		addDist(group,filterUpper,"LeftSawtoothDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistribution.RandomNumberInfo"));

		/* Linke S�gezahn-Verteilung - Direkt */

		addDist(group,filterUpper,"LeftSawtoothDistDirect","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistributionDirect"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistributionDirect.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistributionDirect.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LeftSawtoothDistributionDirect.RandomNumberInfo"));

		/* Rechte S�gezahn-Verteilung */

		addDist(group,filterUpper,"RightSawtoothDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistribution.RandomNumberInfo"));

		/* Rechte S�gezahn-Verteilung - Direkt */

		addDist(group,filterUpper,"RightSawtoothDistDirect","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistributionDirect"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistributionDirect.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistributionDirect.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RightSawtoothDistributionDirect.RandomNumberInfo"));

		/* Levy-Verteilung */

		addDist(group,filterUpper,"LevyDist","mu;c",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LevyDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LevyDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LevyDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LevyDistribution.RandomNumberInfo"));

		/* Maxwell-Boltzmann-Verteilung */

		addDist(group,filterUpper,"MaxwellBoltzmannDist","a",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.MaxwellBoltzmannDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.MaxwellBoltzmannDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.MaxwellBoltzmannDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.MaxwellBoltzmannDistribution.RandomNumberInfo"));

		/* Studentsche t-Verteilung */

		addDist(group,filterUpper,"StudentTDist","mu;nu",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.StudentTDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.StudentTDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.StudentTDist.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.StudentTDist.RandomNumberInfo"));

		/* Halbe Normalverteilung */

		addDist(group,filterUpper,"HalfNormalDist","s;mu",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfNormalDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfNormalDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfNormalDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfNormalDistribution.RandomNumberInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfNormalDistribution.RandomNumberRangeInfo"));

		/* U-quadratische Verteilung */

		addDist(group,filterUpper,"UQuadraticDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UQuadraticDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UQuadraticDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UQuadraticDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UQuadraticDistribution.RandomNumberInfo"));

		/* Reziproke Verteilung */

		addDist(group,filterUpper,"ReciprocalDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ReciprocalDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ReciprocalDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ReciprocalDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ReciprocalDistribution.RandomNumberInfo"));

		/* Kumaraswamy-Verteilung */

		addDist(group,filterUpper,"KumaraswamyDist","a;b;c;d",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.KumaraswamyDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.KumaraswamyDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.KumaraswamyDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.KumaraswamyDistribution.RandomNumberInfo"));

		/* Irwin-Hall-Verteilung */

		addDist(group,filterUpper,"IrwinHallDist","n",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistribution.RandomNumberInfo"));

		/* Irwin-Hall-Verteilung - Direkt */

		addDist(group,filterUpper,"IrwinHallDist","mean",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistributionDirect"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistributionDirect.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistributionDirect.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.IrwinHallDistributionDirect.RandomNumberInfo"));

		/* Sinus-Verteilung */

		addDist(group,filterUpper,"SineDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.SineDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.SineDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.SineDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.SineDistribution.RandomNumberInfo"));

		/* Cosinus-Verteilung */

		addDist(group,filterUpper,"CosineDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CosineDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CosineDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CosineDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CosineDistribution.RandomNumberInfo"));

		/* Arcus Sinus-Verteilung */

		addDist(group,filterUpper,"ArcsineDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ArcsineDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ArcsineDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ArcsineDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ArcsineDistribution.RandomNumberInfo"));

		/* Wigner Halbkreis-Verteilung */

		addDist(group,filterUpper,"WignerHalfCircleDist","R;m",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WignerHalfCircleDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WignerHalfCircleDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WignerHalfCircleDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WignerHalfCircleDistribution.RandomNumberInfo"));

		/* Log-Cauchy-Verteilung */

		addDist(group,filterUpper,"LogCauchyDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogCauchyDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogCauchyDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogCauchyDist.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogCauchyDist.RandomNumberInfo"));

		/* Log-Gamma-Verteilung */

		addDist(group,filterUpper,"LogGammaDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogGammaDistribution"),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.LogGammaDistribution.DensityInfo"),NumberTools.formatNumberMax(0.03992278718149)),
				String.format(Language.tr("ExpressionBuilder.ProbabilityDistributions.LogGammaDistribution.DistributionFunctionInfo"),NumberTools.formatNumberMax(0.087695308502)),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogGammaDistribution.RandomNumberInfo"));

		/* Kontinuierliche Bernoulli-Verteilung */

		addDist(group,filterUpper,"ContinuousBernoulliDist","a;b;lambda",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ContinuousBernoulliDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ContinuousBernoulliDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ContinuousBernoulliDist.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ContinuousBernoulliDist.RandomNumberInfo"));

		/* Halbe Cauchy-Verteilung */

		addDist(group,filterUpper,"HalfCauchyDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfCauchyDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfCauchyDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfCauchyDist.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HalfCauchyDist.RandomNumberInfo"));


		/* Verallgemeinerte Rademacher-Verteilung */

		addDist(group,filterUpper,"GeneralizedRademacherDist","a;b;pA",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GeneralizedRademacherDist"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GeneralizedRademacherDist.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GeneralizedRademacherDist.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GeneralizedRademacherDist.RandomNumberInfo"));

		return group;
	}

	/**
	 * Sortiert eine als Baumeintrag gegebene Liste und f�gt diese an einen anderen Baumeintrag an.
	 * @param newRecords	Baumeintrag, der die zu sortierenden Eintr�ge enth�lt
	 * @param resultGroup	Baumeintrag an den die Eintr�ge in sortierter Form angeh�ngt werden sollen
	 */
	private static void sortGroupAndAdd(final DefaultMutableTreeNode newRecords, final DefaultMutableTreeNode resultGroup) {
		final List<DefaultMutableTreeNode> records=new ArrayList<>();
		for (int i=0;i<newRecords.getChildCount();i++) records.add((DefaultMutableTreeNode)newRecords.getChildAt(i));
		records.stream().sorted((n1,n2)->String.CASE_INSENSITIVE_ORDER.compare((String)n1.getUserObject(),(String)n2.getUserObject())).forEach(n->resultGroup.add(n));
	}

	/**
	 * F�gt die Rechensymbole in die Baumstruktur eines {@link ExpressionBuilder}-Objektes ein.
	 * @param root	Wurzelelement der Baumstruktur
	 * @param pathsToOpen	Liste der initial auszuklappenden �ste
	 * @param filterUpper	Nur Anzeige der Elemente, die zu dem Filter passen (der Filter kann dabei <code>null</code> sein, was bedeutet "nicht filtern")
	 */
	public static void build(final DefaultMutableTreeNode root, final List<TreePath> pathsToOpen, final String filterUpper) {
		final String pdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.Density");
		final String cdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.DistributionFunction");
		final String rnd=Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomNumber");

		final DefaultMutableTreeNode group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions"));
		DefaultMutableTreeNode sub;

		sortGroupAndAdd(buildDiscrete(filterUpper),group);
		sortGroupAndAdd(buildContinous(filterUpper),group);

		/* Empirische Verteilung */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution"));

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+pdf+" (EmpiricalDensity)",
				"EmpiricalDensity("+Language.tr("ExpressionBuilder.Value")+";value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.DensityInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+cdf+" (EmpiricalDistribution)",
				"EmpiricalDistribution("+Language.tr("ExpressionBuilder.Value")+";value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.DistributionFunctionInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+rnd+" (EmpiricalRandom)",
				"EmpiricalRandom(value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.RandomNumberInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.Mean")+" (EmpiricalDistributionMean)",
				"EmpiricalDistributionMean(value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.MeanInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.Median")+" (EmpiricalDistributionMedian)",
				"EmpiricalDistributionMedian(value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.MedianInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.Quantil")+" (EmpiricalDistributionQuantil)",
				"EmpiricalDistributionQuantil(value1;value2;value3;...;max;p)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.QuantilInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.SD")+" (EmpiricalDistributionSD)",
				"EmpiricalDistributionSD(value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.SDInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.Var")+" (EmpiricalDistributionVar)",
				"EmpiricalDistributionVar(value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.VarInfo"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution")+", "+Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.CV")+" (EmpiricalDistributionCV)",
				"EmpiricalDistributionCV(value1;value2;value3;...;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution.CVInfo"));

		if (sub.getChildCount()>0) group.add(sub);

		/* Zuf�llige Auswahl eines von mehreren Werten */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomValues"));

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomValues")+", "+rnd+" (RandomValues)",
				"RandomValues(rate1;value1;rate2;value2;...)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomValues.RandomNumberInfo"));

		if (sub.getChildCount()>0) group.add(sub);

		/* Zufallszahlen gem�� nutzerdefinierter Verteilung */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions.CustomDistribution"));

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CustomDistribution")+", "+rnd+" (RandomGenerator)",
				"RandomGenerator(distribution(RandomGeneratorX());min;max)",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CustomDistribution.RandomNumberInfo"));

		if (sub.getChildCount()>0) group.add(sub);

		if (group.getChildCount()>0) root.add(group);
	}
}
