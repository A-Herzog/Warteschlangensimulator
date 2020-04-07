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
 * Fügt die Standard-Rechenbefehle in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderBasics {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderBasics#build(DefaultMutableTreeNode, List)} zur Verfügung.
	 */
	private ExpressionBuilderBasics() {}

	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_FUNCTION));
	}

	private static void addTreeNode(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(name,symbol,description));
		}
	}

	private static DefaultMutableTreeNode getTreeNodeConst(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_CONST));
	}

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
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Percent")+" (%)","%",Language.tr("ExpressionBuilder.BasicArithmetics.Percent.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.Modulo")+" (mod)","mod("+value+"A;"+value+"B)",Language.tr("ExpressionBuilder.BasicArithmetics.Modulo.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.BasicArithmetics.If")+" (if)","if("+Language.tr("ExpressionBuilder.BasicArithmetics.If.Parameters")+")",Language.tr("ExpressionBuilder.BasicArithmetics.If.Info"));
		if (group.getChildCount()>0) root.add(group);

		/* Runden */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.Rounding"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Rounding.round")+" (round)","round("+value+")",Language.tr("ExpressionBuilder.Rounding.round.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Rounding.floor")+" (floor)","floor("+value+")",Language.tr("ExpressionBuilder.Rounding.floor.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.Rounding.ceil")+" (ceil)","ceil("+value+")",Language.tr("ExpressionBuilder.Rounding.ceil.Info"));
		if (group.getChildCount()>0) root.add(group);

		/* Mathematische Funktionen */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.MathematicalFunctions"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Squareroot")+" (sqrt)","sqrt("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Squareroot.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Square")+" (sqr)","sqr("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Square.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Potentiate")+" (power)","power("+value+"A;"+value+"B)",Language.tr("ExpressionBuilder.MathematicalFunctions.Potentiate.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.AbsolutValue")+" (abs)","abs("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.AbsolutValue.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Sign")+" (sign)","sign("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.Sign.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.IntegerPart")+" (int)","int("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.IntegerPart.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.FractionPart")+" (frac)","frac("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.FractionPart.info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Factorial")+" (!)","!",Language.tr("ExpressionBuilder.MathematicalFunctions.Factorial.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.GammaFunction")+" (gamma)","gamma("+value+")",Language.tr("ExpressionBuilder.MathematicalFunctions.GammaFunction.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalFunctions.Random")+" (random)","random()",Language.tr("ExpressionBuilder.MathematicalFunctions.Random.Info"));

		/* Mathematische Funktionen -> Exponential- und Logarithmus-Funktionen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.ExponentialFunction")+" (exp)","exp("+value+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.ExponentialFunction.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.NaturalLogarithm")+" (ln)","ln("+value+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.NaturalLogarithm.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.10Logarithm")+" (lg)","lg("+value+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.10Logarithm.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.2Logarithm")+" (ld)","ld("+value+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.2Logarithm.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.AnyBaseLogarithm")+" (log)","log("+value+";"+Language.tr("ExpressionBuilder.Base")+")",Language.tr("ExpressionBuilder.ExponentialAndLogarithmFunctions.AnyBaseLogarithm.Info"));
		if (sub.getChildCount()>0) group.add(sub);

		/* Mathematische Funktionen -> Trigonometrische Funktionen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.TrigonometricFunctions"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Sinus")+" (sin)","sin("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Sinus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Cosinus")+" (cos)","cos("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Cosinus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Tangens")+" (tan)","tan("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Tangens.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.Cotangens")+" (cot)","cot("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.Cotangens.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinus")+" (arcsin)","arcsin("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinus")+" (arccos)","arccos("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangens")+" (arctan)","arctan("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangens.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangens")+" (arccot)","arccot("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangens.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.SinusHyperbolicus")+" (sinh)","sinh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.SinusHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.CosinusHyperbolicus")+" (cosh)","cosh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.CosinusHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.TangensHyperbolicus")+" (tanh)","tanh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.TangensHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.CotangensHyperbolicus")+" (coth)","coth("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.CotangensHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinusHyperbolicus")+" (arcsinh)","arcsinh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusSinusHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinusHyperbolicus")+" (arccosh)","arccosh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCosinusHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangensHyperbolicus")+" (arctanh)","arctanh("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusTangensHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangensHyperbolicus")+" (arccoth)","arccoth("+value+")",Language.tr("ExpressionBuilder.TrigonometricFunctions.ArcusCotangensHyperbolicus.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.TrigonometricFunctions.DegreeToRadians")+" (°)","°",Language.tr("ExpressionBuilder.TrigonometricFunctions.DegreeToRadians.Info"));
		if (sub.getChildCount()>0) group.add(sub);

		if (group.getChildCount()>0) root.add(group);

		/* Mathematische Konstanten */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.MathematicalConstants"));
		addTreeNodeConst(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalConstants.Pi"),"pi",Language.tr("ExpressionBuilder.MathematicalConstants.Pi.Info"));
		addTreeNodeConst(group,filterUpper,Language.tr("ExpressionBuilder.MathematicalConstants.e"),"e",Language.tr("ExpressionBuilder.MathematicalConstants.e.Info"));
		if (group.getChildCount()>0) root.add(group);

		/* Statistik-Funktionen */

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.StatisticalFunctions"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Minimum")+" (min)","min("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Minimum.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Maximum")+" (max)","max("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Maximum.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Sum")+" (sum)","sum("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Sum.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Mean")+" (mean)","mean("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Mean.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Median")+" (median)","median("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Median.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.Variance")+" (var)","var("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.Variance.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.StandardDeviation")+" (sd)","sd("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.StandardDeviation.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.CoefficientOfVariation")+" (cv)","cv("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.CoefficientOfVariation.Info"));
		addTreeNode(group,filterUpper,Language.tr("ExpressionBuilder.StatisticalFunctions.SquaredVariationCoefficient")+" (scv)","scv("+value+"A;"+value+"B;"+value+"C;...)",Language.tr("ExpressionBuilder.StatisticalFunctions.SquaredVariationCoefficient.Info"));
		if (group.getChildCount()>0) root.add(group);
	}
}
