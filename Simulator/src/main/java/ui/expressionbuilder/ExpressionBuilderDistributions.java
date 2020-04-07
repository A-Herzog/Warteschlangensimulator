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

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbol;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbolType;

/**
 * Fügt die Rechenbefehle für Wahrscheinlichkeitsverteilungen in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderDistributions {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderDistributions#build(DefaultMutableTreeNode, List)} zur Verfügung.
	 */
	private ExpressionBuilderDistributions() {}

	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_DISTRIBUTION));
	}

	private static void addTreeNode(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(name,symbol,description));
		}
	}

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
	 * Fügt die Rechensymbole in die Baumstruktur eines {@link ExpressionBuilder}-Objektes ein.
	 * @param root	Wurzelelement der Baumstruktur
	 * @param pathsToOpen	Liste der initial auszuklappenden Äste
	 * @param filterUpper	Nur Anzeige der Elemente, die zu dem Filter passen (der Filter kann dabei <code>null</code> sein, was bedeutet "nicht filtern")
	 */
	public static void build(final DefaultMutableTreeNode root, final List<TreePath> pathsToOpen, final String filterUpper) {
		final String pdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.Density");
		final String cdf=Language.tr("ExpressionBuilder.ProbabilityDistributions.DistributionFunction");
		final String rnd=Language.tr("ExpressionBuilder.ProbabilityDistributions.RandomNumber");

		final DefaultMutableTreeNode group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions"));

		/* Exponentialverteilung */

		addDist(group,filterUpper,"ExpDist","mean",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ExponentialDistribution.RandomNumberInfo"));

		/* Gleichverteilung */

		addDist(group,filterUpper,"UniformDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution,DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.UniformDistribution.RandomNumberInfo"));

		/* Normalverteilung */

		addDist(group,filterUpper,"NormalDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.NormalDistribution.RandomNumberInfo"));

		/* Lognormalverteilung */

		addDist(group,filterUpper,"LogNormalDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogNormalDistribution.RandomNumberInfo"));

		/* Gamma-Verteilung */

		addDist(group,filterUpper,"GammaDist","alpha;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GammaDistribution.RandomNumberInfo"));

		/* Erlang-Verteilung */

		addDist(group,filterUpper,"ErlangDist","n;lambda",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ErlangDistribution.RandomNumberInfo"));

		/* Beta-Verteilung */

		addDist(group,filterUpper,"BetaDist","a;b;alpha;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.BetaDistribution.RandomNumberInfo"));

		/* Weibull-Verteilung */

		addDist(group,filterUpper,"WeibullDist","scale;form;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.WeibullDistribution.RandomNumberInfo"));

		/* Cauchy-Verteilung */

		addDist(group,filterUpper,"CauchyDist","mean;form",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.CauchyDistribution.RandomNumberInfo"));

		/* Chi²-Verteilung */

		addDist(group,filterUpper,"ChiSquareDist","n",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiSquareDistribution.RandomNumberInfo"));

		/* Chi-Verteilung */

		addDist(group,filterUpper,"ChiDist","n",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.ChiDistribution.RandomNumberInfo"));

		/* F-Verteilung */

		addDist(group,filterUpper,"FDist","a;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FDistribution.RandomNumberInfo"));

		/* Johnson-SU-Verteilung */

		addDist(group,filterUpper,"JohnsonSUDist","gamma;xi;delta;lambda",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.JohnsonSUDistribution.RandomNumberInfo"));

		/* Dreiecksverteilung */

		addDist(group,filterUpper,"TriangularDist","lower;mostLikely;upper",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.TriangularDistribution.RandomNumberInfo"));

		/* Laplace-Verteilung */

		addDist(group,filterUpper,"LaplaceDist","mu;b",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LaplaceDistribution.RandomNumberInfo"));

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
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogisticDistribution.RandomNumberInfo"));

		/* Inverse Gauß-Verteilung */

		addDist(group,filterUpper,"InverseGaussianDist","lambda;mu",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.InverseGaussianDistribution.RandomNumberInfo"));

		/* Rayleigh-Verteilung */

		addDist(group,filterUpper,"RayleighDist","mu",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.RayleighDistribution.RandomNumberInfo"));

		/* Log-logistische Verteilung */

		addDist(group,filterUpper,"LogLogisticDist","alpha;beta",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.LogLogisticDistribution.RandomNumberInfo"));

		/* Potenzverteilung */

		addDist(group,filterUpper,"PowerDist","a;b;c",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.PowerDistribution.RandomNumberInfo"));

		/* Gumbel-Verteilung */

		addDist(group,filterUpper,"GumbelDist","mean;sd",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.GumbelDistribution.RandomNumberInfo"));

		/* Fatigue-Life-Verteilung */

		addDist(group,filterUpper,"FatigueLifeDist","mu;beta;gamma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FatigueLifeDistribution.RandomNumberInfo"));

		/* Frechet-Verteilung */

		addDist(group,filterUpper,"FrechetDist","delta;beta;alpha",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.FrechetDistribution.RandomNumberInfo"));

		/* Hyperbolische Sekanten-Verteilung */

		addDist(group,filterUpper,"HyperbolicSecantDist","mu;sigma",
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution.DensityInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution.DistributionFunctionInfo"),
				Language.tr("ExpressionBuilder.ProbabilityDistributions.HyperbolicSecantDistribution.RandomNumberInfo"));

		/* Empirische Verteilung */

		final DefaultMutableTreeNode sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ProbabilityDistributions.EmpiricalDistribution"));

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
		if (group.getChildCount()>0) root.add(group);
	}
}
