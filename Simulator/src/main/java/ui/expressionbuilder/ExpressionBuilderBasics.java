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
import mathtools.NumberTools;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbol;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbolType;

/**
 * Fügt die Standard-Rechenbefehle in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderBasics {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderBasics#build(DefaultMutableTreeNode, List, String)} zur Verfügung.
	 */
	private ExpressionBuilderBasics() {}

	/**
	 * Erstellt einen neuen Eintrag für die Baumstruktur (fügt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @return	Neuer Eintrag für die Baumstruktur
	 * @see #addTreeNode(DefaultMutableTreeNode, String, String, String, String)
	 */
	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_FUNCTION));
	}

	/**
	 * Fügt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugefügt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 */
	private static void addTreeNode(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(name,symbol,description));
		}
	}

	/**
	 * Erstellt einen neuen Eintrag für eine Konstante für die Baumstruktur (fügt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @return	Neuer Eintrag für die Baumstruktur
	 * @see #addTreeNodeConst(DefaultMutableTreeNode, String, String, String, String)
	 */
	private static DefaultMutableTreeNode getTreeNodeConst(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_CONST));
	}

	/**
	 * Fügt einen Eintrag für eine Konstante zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugefügt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 */
	private static void addTreeNodeConst(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNodeConst(name,symbol,description));
		}
	}

	/**
	 * Fügt die Rechensymbole in die Baumstruktur eines {@link ExpressionBuilder}-Objektes ein.
	 * @param root	Wurzelelement der Baumstruktur
	 * @param pathsToOpen	Liste der initial auszuklappenden Äste
	 * @param filterUpper	Nur Anzeige der Elemente, die zu dem Filter passen (der Filter kann dabei <code>null</code> sein, was bedeutet "nicht filtern")
	 */
	public static void build(final DefaultMutableTreeNode root, final List<TreePath> pathsToOpen, final String filterUpper) {
		DefaultMutableTreeNode group, sub;

		final String value=Language.tr("ExpressionBuilder.Value");

		/* Grundrechenarten */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.BasicArithmetics"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Plus")+" (+)","+",Language.tr("ExpressionBuilder.BasicArithmetics.Plus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Minus")+" (-)","-",Language.tr("ExpressionBuilder.BasicArithmetics.Minus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Multiply")+" (*)","*",Language.tr("ExpressionBuilder.BasicArithmetics.Multiply.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Divide")+" (/)","/",Language.tr("ExpressionBuilder.BasicArithmetics.Divide.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Potentiate")+" (^)","^",Language.tr("ExpressionBuilder.BasicArithmetics.Potentiate.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Percent")+" (%)","%",String.format(Language.tr("ExpressionBuilder.BasicArithmetics.Percent.Info"),NumberTools.formatNumber(0.3)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Modulo")+" (mod)","mod("+value+"A;"+value+"B)",Language.tr("ExpressionBuilder.BasicArithmetics.Modulo.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.If")+" (if)","if("+Language.tr("ExpressionBuilder.BasicArithmetics.If.Parameters")+")",Language.tr("ExpressionBuilder.BasicArithmetics.If.Info"));
		if (group.getChildCount()>0) root.add(group);

		/* Runden */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.Rounding"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Rounding.round")+" (round)","round("+value+")",String.format(Language.tr("ExpressionBuilder.Rounding.round.Info"),NumberTools.formatNumberMax(1.4),NumberTools.formatNumberMax(1.5)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Rounding.floor")+" (floor)","floor("+value+")",String.format(Language.tr("ExpressionBuilder.Rounding.floor.Info"),NumberTools.formatNumberMax(1.9)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Rounding.ceil")+" (ceil)","ceil("+value+")",String.format(Language.tr("ExpressionBuilder.Rounding.ceil.Info"),NumberTools.formatNumberMax(1.1)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Rounding.trunc")+" (trunc)","trunc("+value+")",String.format(Language.tr("ExpressionBuilder.Rounding.trunc.Info"),NumberTools.formatNumberMax(1.4),NumberTools.formatNumberMax(1.5),NumberTools.formatNumberMax(-1.5),NumberTools.formatNumberMax(-1.4)));
		if (group.getChildCount()>0) root.add(group);

		/* Mathematische Funktionen */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.MathematicalFunctions"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Squareroot")+" (sqrt)","sqrt("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Squareroot.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Cuberoot")+" (cbrt)","cbrt("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Cuberoot.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Square")+" (sqr)","sqr("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Square.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Potentiate")+" (power)","power("+value+"A;"+value+"B)",Language.tr("ExpressionBuilder.MathematicalFunctions.Potentiate.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.AbsolutValue")+" (abs)","abs("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.AbsolutValue.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Sign")+" (sign)","sign("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Sign.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.IntegerPart")+" (int)","int("+value+")",String.format(Language.tr("ExpressionBuilder.MathematicalFunctions.IntegerPart.Info"),NumberTools.formatNumber(3.4)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.FractionPart")+" (frac)","frac("+value+")",String.format(Language.tr("ExpressionBuilder.MathematicalFunctions.FractionPart.info"),NumberTools.formatNumber(3.4),NumberTools.formatNumber(0.4)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Factorial")+" (!)","!",Language.tr("ExpressionBuilder.MathematicalFunctions.Factorial.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Inverse")+" (Inverse)","Inverse("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Inverse.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Binomial")+" (binom)","binom(n;k)",Language.tr("ExpressionBuilder.MathematicalFunctions.Binomial.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Beta")+" (beta)","beta(p;q)",Language.tr("ExpressionBuilder.MathematicalFunctions.Beta.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.GammaFunction")+" (gamma)","gamma("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.GammaFunction.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.GammaPFunction")+" (gammaP)","gammaP(a;x)",Language.tr("ExpressionBuilder.MathematicalFunctions.GammaPFunction.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.GammaQFunction")+" (gammaQ)","gammaQ(a;x)",Language.tr("ExpressionBuilder.MathematicalFunctions.GammaQFunction.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.ZetaFunction")+" (zeta)","zeta("+value+")",String.format(Language.tr("ExpressionBuilder.MathematicalFunctions.ZetaFunction.Info"),NumberTools.formatNumberMax(1.64493)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Random")+" (random)","random()",Language.tr("ExpressionBuilder.MathematicalFunctions.Random.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.RandomRange")+" (randomRange)","randomRange("+value+";"+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.RandomRange.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.RandomRangeInt")+" (randomIntRange)","randomIntRange("+value+";"+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.RandomRangeInt.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Gcd")+" (gcd)","gcd("+value+";"+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Gcd.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Lcm")+" (lcm)","lcm("+value+";"+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Lcm.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.IsPrime")+" (isPrime)","isPrime("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.IsPrime.Info"));

		/* Mathematische Funktionen -> Exponential- und Logarithmus-Funktionen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.ExponentialFunction")+" (exp)","exp("+value+")",String.format(Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.ExponentialFunction.Info"),NumberTools.formatNumberMax(22026.465794807)));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.NaturalLogarithm")+" (ln)","ln("+value+")",String.format(Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.NaturalLogarithm.Info"),NumberTools.formatNumberMax(2.718281828459),NumberTools.formatNumberMax(2.302585092994)));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.10Logarithm")+" (lg)","lg("+value+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.10Logarithm.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.2Logarithm")+" (ld)","ld("+value+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.2Logarithm.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.AnyBaseLogarithm")+" (log)","log("+value+";"+Language.tr("ExpressionBuilder.Base")+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.AnyBaseLogarithm.Info"));
		if (sub.getChildCount()>0) group.add(sub);

		/* Mathematische Funktionen -> Trigonometrische Funktionen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.TrigonometricFunctions"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Sinus")+" (sin)","sin("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Sinus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Cosinus")+" (cos)","cos("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Cosinus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Tangens")+" (tan)","tan("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Tangens.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Cotangens")+" (cot)","cot("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Cotangens.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinus")+" (arcsin)","arcsin("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinus")+" (arccos)","arccos("+value+")",String.format(Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinus.Info"),NumberTools.formatNumberMax(1.5707963267949)));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangens")+" (arctan)","arctan("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangens.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangens")+" (arccot)","arccot("+value+")",String.format(Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangens.Info"),NumberTools.formatNumberMax(1.5707963267949)));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.SinusHyperbolicus")+" (sinh)","sinh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.SinusHyperbolicus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.CosinusHyperbolicus")+" (cosh)","cosh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.CosinusHyperbolicus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.TangensHyperbolicus")+" (tanh)","tanh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.TangensHyperbolicus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.CotangensHyperbolicus")+" (coth)","coth("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.CotangensHyperbolicus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinusHyperbolicus")+" (arcsinh)","arcsinh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinusHyperbolicus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinusHyperbolicus")+" (arccosh)","arccosh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinusHyperbolicus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangensHyperbolicus")+" (arctanh)","arctanh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangensHyperbolicus.Info"));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangensHyperbolicus")+" (arccoth)","arccoth("+value+")",String.format(Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangensHyperbolicus.Info"),NumberTools.formatNumberMax(0.54930614433405)));
		addTreeNode(sub,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.DegreeToRadians")+" (°)","°",String.format(Language.tr("ExpressionBuilder.TrigonometricFunctions.DegreeToRadians.Info"),NumberTools.formatNumberMax(3.1415926535898)));
		if (sub.getChildCount()>0) group.add(sub);

		if (group.getChildCount()>0) root.add(group);

		/* Mathematische Konstanten */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.MathematicalConstants"));
		addTreeNodeConst(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalConstants.Pi"),"pi",String.format(Language.tr("ExpressionBuilder.MathematicalConstants.Pi.Info"),NumberTools.formatNumberMax(3.1415926535898)));
		addTreeNodeConst(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalConstants.e"),"e",String.format(Language.tr("ExpressionBuilder.MathematicalConstants.e.Info"),NumberTools.formatNumberMax(2.718281828459)));
		addTreeNodeConst(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalConstants.tau"),"tau",String.format(Language.tr("ExpressionBuilder.MathematicalConstants.tau.Info"),NumberTools.formatNumberMax(6.28318530717959)));
		if (group.getChildCount()>0) root.add(group);

		/* Logik-Funktionen */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.LogicFunctions"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.And")+" (and)","and("+value+";"+value+")",Language.tr("ExpressionBuilder.LogicFunctions.And.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.Or")+" (or)","or("+value+";"+value+")",Language.tr("ExpressionBuilder.LogicFunctions.Or.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.Xor")+" (xor)","xor("+value+";"+value+")",Language.tr("ExpressionBuilder.LogicFunctions.Xor.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.Not")+" (not)","not("+value+")",Language.tr("ExpressionBuilder.LogicFunctions.Not.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.Nand")+" (nand)","nand("+value+";"+value+")",Language.tr("ExpressionBuilder.LogicFunctions.Nand.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.Nor")+" (nor)","nor("+value+";"+value+")",Language.tr("ExpressionBuilder.LogicFunctions.Nor.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.Nxor")+" (nxor)","nxor("+value+";"+value+")",Language.tr("ExpressionBuilder.LogicFunctions.Nxor.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.LogicFunctions.Equals")+" (equals)","equals("+value+";"+value+")",Language.tr("ExpressionBuilder.LogicFunctions.Equals.Info"));
		if (group.getChildCount()>0) root.add(group);

		/* Statistik-Funktionen */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.StatisticalFunctions"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Minimum")+" (min)","min("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Minimum.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Maximum")+" (max)","max("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Maximum.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Range")+" (range)","range("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Range.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Sum")+" (sum)","sum("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Sum.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Mean")+" (mean)","mean("+value+"A;"+value+"B;"+value+"C;...)",String.format(Language.tr("ExpressionBuilder.StatisticalFunctions.Mean.Info"),NumberTools.formatNumberMax(2.5)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.MeanGeometric")+" (geomean)","geomean("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.MeanGeometric.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.MeanHarmonic")+" (harmonicmean)","harmonicmean("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.MeanHarmonic.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Median")+" (median)","median("+value+"A;"+value+"B;"+value+"C;...)",String.format(Language.tr("ExpressionBuilder.StatisticalFunctions.Median.Info"),NumberTools.formatNumberMax(2.5)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Variance")+" (var)","var("+value+"A;"+value+"B;"+value+"C;...)",String.format(Language.tr("ExpressionBuilder.StatisticalFunctions.Variance.Info"),NumberTools.formatNumberMax(1.6666666666667)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.StandardDeviation")+" (sd)","sd("+value+"A;"+value+"B;"+value+"C;...)",String.format(Language.tr("ExpressionBuilder.StatisticalFunctions.StandardDeviation.Info"),NumberTools.formatNumberMax(1.2909944487358)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.CoefficientOfVariation")+" (cv)","cv("+value+"A;"+value+"B;"+value+"C;...)",String.format(Language.tr("ExpressionBuilder.StatisticalFunctions.CoefficientOfVariation.Info"),NumberTools.formatNumberMax(0.51639777949432)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.SquaredVariationCoefficient")+" (scv)","scv("+value+"A;"+value+"B;"+value+"C;...)",String.format(Language.tr("ExpressionBuilder.StatisticalFunctions.SquaredVariationCoefficient.Info"),NumberTools.formatNumberMax(0.26666666666667)));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Sk")+" (sk)","sk("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Sk.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Kurt")+" (kurt)","kurt("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Kurt.Info"));

		if (group.getChildCount()>0) root.add(group);
	}
}