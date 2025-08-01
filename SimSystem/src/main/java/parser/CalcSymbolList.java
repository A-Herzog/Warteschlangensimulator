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
package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import parser.coresymbols.CalcSymbol;
import parser.coresymbols.CalcSymbolConst;
import parser.coresymbols.CalcSymbolMiddleOperator;
import parser.coresymbols.CalcSymbolNumber;
import parser.coresymbols.CalcSymbolPostOperator;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.coresymbols.CalcSymbolVariable;
import parser.symbols.CalcSymbolConstE;
import parser.symbols.CalcSymbolConstPi;
import parser.symbols.CalcSymbolConstTau;
import parser.symbols.CalcSymbolMiddleOperatorDivide;
import parser.symbols.CalcSymbolMiddleOperatorMinus;
import parser.symbols.CalcSymbolMiddleOperatorMultiply;
import parser.symbols.CalcSymbolMiddleOperatorPlus;
import parser.symbols.CalcSymbolMiddleOperatorPower;
import parser.symbols.CalcSymbolPostOperatorDEGtoRAD;
import parser.symbols.CalcSymbolPostOperatorFactorial;
import parser.symbols.CalcSymbolPostOperatorPercent;
import parser.symbols.CalcSymbolPostOperatorPower2;
import parser.symbols.CalcSymbolPostOperatorPower3;
import parser.symbols.CalcSymbolPreOperatorAbs;
import parser.symbols.CalcSymbolPreOperatorAllenCunneen;
import parser.symbols.CalcSymbolPreOperatorArcCos;
import parser.symbols.CalcSymbolPreOperatorArcCosh;
import parser.symbols.CalcSymbolPreOperatorArcCot;
import parser.symbols.CalcSymbolPreOperatorArcCoth;
import parser.symbols.CalcSymbolPreOperatorArcSin;
import parser.symbols.CalcSymbolPreOperatorArcSinh;
import parser.symbols.CalcSymbolPreOperatorArcTan;
import parser.symbols.CalcSymbolPreOperatorArcTanh;
import parser.symbols.CalcSymbolPreOperatorBeta;
import parser.symbols.CalcSymbolPreOperatorBinomial;
import parser.symbols.CalcSymbolPreOperatorCV;
import parser.symbols.CalcSymbolPreOperatorCbrt;
import parser.symbols.CalcSymbolPreOperatorCeil;
import parser.symbols.CalcSymbolPreOperatorCos;
import parser.symbols.CalcSymbolPreOperatorCosh;
import parser.symbols.CalcSymbolPreOperatorCot;
import parser.symbols.CalcSymbolPreOperatorCoth;
import parser.symbols.CalcSymbolPreOperatorErlangC;
import parser.symbols.CalcSymbolPreOperatorExp;
import parser.symbols.CalcSymbolPreOperatorFactorial;
import parser.symbols.CalcSymbolPreOperatorFloor;
import parser.symbols.CalcSymbolPreOperatorFrac;
import parser.symbols.CalcSymbolPreOperatorGamma;
import parser.symbols.CalcSymbolPreOperatorGammaP;
import parser.symbols.CalcSymbolPreOperatorGammaQ;
import parser.symbols.CalcSymbolPreOperatorGcd;
import parser.symbols.CalcSymbolPreOperatorIf;
import parser.symbols.CalcSymbolPreOperatorInt;
import parser.symbols.CalcSymbolPreOperatorInverse;
import parser.symbols.CalcSymbolPreOperatorIsPrime;
import parser.symbols.CalcSymbolPreOperatorKurt;
import parser.symbols.CalcSymbolPreOperatorLcm;
import parser.symbols.CalcSymbolPreOperatorLd;
import parser.symbols.CalcSymbolPreOperatorLg;
import parser.symbols.CalcSymbolPreOperatorLog;
import parser.symbols.CalcSymbolPreOperatorLogicAnd;
import parser.symbols.CalcSymbolPreOperatorLogicEquals;
import parser.symbols.CalcSymbolPreOperatorLogicNAnd;
import parser.symbols.CalcSymbolPreOperatorLogicNOr;
import parser.symbols.CalcSymbolPreOperatorLogicNXor;
import parser.symbols.CalcSymbolPreOperatorLogicNot;
import parser.symbols.CalcSymbolPreOperatorLogicOr;
import parser.symbols.CalcSymbolPreOperatorLogicXor;
import parser.symbols.CalcSymbolPreOperatorMax;
import parser.symbols.CalcSymbolPreOperatorMean;
import parser.symbols.CalcSymbolPreOperatorMeanGeometric;
import parser.symbols.CalcSymbolPreOperatorMeanHarmonic;
import parser.symbols.CalcSymbolPreOperatorMedian;
import parser.symbols.CalcSymbolPreOperatorMin;
import parser.symbols.CalcSymbolPreOperatorModulo;
import parser.symbols.CalcSymbolPreOperatorPower;
import parser.symbols.CalcSymbolPreOperatorRandom;
import parser.symbols.CalcSymbolPreOperatorRandomGeneratorInvers;
import parser.symbols.CalcSymbolPreOperatorRandomGeneratorInversX;
import parser.symbols.CalcSymbolPreOperatorRandomRange;
import parser.symbols.CalcSymbolPreOperatorRandomRangeInt;
import parser.symbols.CalcSymbolPreOperatorRandomValues;
import parser.symbols.CalcSymbolPreOperatorRange;
import parser.symbols.CalcSymbolPreOperatorRound;
import parser.symbols.CalcSymbolPreOperatorSCV;
import parser.symbols.CalcSymbolPreOperatorSign;
import parser.symbols.CalcSymbolPreOperatorSin;
import parser.symbols.CalcSymbolPreOperatorSinh;
import parser.symbols.CalcSymbolPreOperatorSk;
import parser.symbols.CalcSymbolPreOperatorSqr;
import parser.symbols.CalcSymbolPreOperatorSqrt;
import parser.symbols.CalcSymbolPreOperatorStdDev;
import parser.symbols.CalcSymbolPreOperatorSum;
import parser.symbols.CalcSymbolPreOperatorTan;
import parser.symbols.CalcSymbolPreOperatorTanh;
import parser.symbols.CalcSymbolPreOperatorTruncate;
import parser.symbols.CalcSymbolPreOperatorVariance;
import parser.symbols.CalcSymbolPreOperatorZeta;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionBinomial;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionBinomialDirect;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionBoltzmann;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionBorel;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionGeometric;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionHyperGeom;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionLogarithmic;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionNegativeBinomial;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionNegativeBinomialDirect;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionNegativeHyperGeom;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionPlanck;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionPoisson;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionUniform;
import parser.symbols.distributions.CalcSymbolDiscreteDistributionZeta;
import parser.symbols.distributions.CalcSymbolDistributionArcsine;
import parser.symbols.distributions.CalcSymbolDistributionBeta;
import parser.symbols.distributions.CalcSymbolDistributionBetaDirect;
import parser.symbols.distributions.CalcSymbolDistributionCauchy;
import parser.symbols.distributions.CalcSymbolDistributionChi;
import parser.symbols.distributions.CalcSymbolDistributionChiSquare;
import parser.symbols.distributions.CalcSymbolDistributionContinuousBernoulli;
import parser.symbols.distributions.CalcSymbolDistributionCosine;
import parser.symbols.distributions.CalcSymbolDistributionExp;
import parser.symbols.distributions.CalcSymbolDistributionF;
import parser.symbols.distributions.CalcSymbolDistributionFatigueLife;
import parser.symbols.distributions.CalcSymbolDistributionFrechet;
import parser.symbols.distributions.CalcSymbolDistributionGamma;
import parser.symbols.distributions.CalcSymbolDistributionGammaDirect;
import parser.symbols.distributions.CalcSymbolDistributionGumbel;
import parser.symbols.distributions.CalcSymbolDistributionGumbelDirect;
import parser.symbols.distributions.CalcSymbolDistributionHalfCauchy;
import parser.symbols.distributions.CalcSymbolDistributionHalfNormal;
import parser.symbols.distributions.CalcSymbolDistributionHyperbolicSecant;
import parser.symbols.distributions.CalcSymbolDistributionInverseGamma;
import parser.symbols.distributions.CalcSymbolDistributionInverseGaussian;
import parser.symbols.distributions.CalcSymbolDistributionIrwinHall;
import parser.symbols.distributions.CalcSymbolDistributionIrwinHallDirect;
import parser.symbols.distributions.CalcSymbolDistributionJohnsonSU;
import parser.symbols.distributions.CalcSymbolDistributionKumaraswamy;
import parser.symbols.distributions.CalcSymbolDistributionLaplace;
import parser.symbols.distributions.CalcSymbolDistributionLevy;
import parser.symbols.distributions.CalcSymbolDistributionLogCauchy;
import parser.symbols.distributions.CalcSymbolDistributionLogGamma;
import parser.symbols.distributions.CalcSymbolDistributionLogLaplace;
import parser.symbols.distributions.CalcSymbolDistributionLogLogistic;
import parser.symbols.distributions.CalcSymbolDistributionLogNormal;
import parser.symbols.distributions.CalcSymbolDistributionLogistic;
import parser.symbols.distributions.CalcSymbolDistributionMaxwellBoltzmann;
import parser.symbols.distributions.CalcSymbolDistributionNormal;
import parser.symbols.distributions.CalcSymbolDistributionPareto;
import parser.symbols.distributions.CalcSymbolDistributionPert;
import parser.symbols.distributions.CalcSymbolDistributionPower;
import parser.symbols.distributions.CalcSymbolDistributionRayleigh;
import parser.symbols.distributions.CalcSymbolDistributionReciprocal;
import parser.symbols.distributions.CalcSymbolDistributionSawtoothLeft;
import parser.symbols.distributions.CalcSymbolDistributionSawtoothLeftDirect;
import parser.symbols.distributions.CalcSymbolDistributionSawtoothRight;
import parser.symbols.distributions.CalcSymbolDistributionSawtoothRightDirect;
import parser.symbols.distributions.CalcSymbolDistributionSine;
import parser.symbols.distributions.CalcSymbolDistributionStudentT;
import parser.symbols.distributions.CalcSymbolDistributionTrapezoid;
import parser.symbols.distributions.CalcSymbolDistributionTriangular;
import parser.symbols.distributions.CalcSymbolDistributionUQuadratic;
import parser.symbols.distributions.CalcSymbolDistributionUniform;
import parser.symbols.distributions.CalcSymbolDistributionWeibull;
import parser.symbols.distributions.CalcSymbolDistributionWignerHalfCircle;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionCDF;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionCV;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionMean;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionMedian;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionPDF;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionQuantil;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionRandom;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionSD;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionVar;
import parser.symbols.distributions.CalcSymbolGeneralizedRademacherDistribution;
import parser.symbols.distributions.CalcSymbolTruncatedDistribution;

/**
 * Diese Klasse h�lt alle Symbole, die im Formelparser zur Verf�gung stehen sollen, vor.
 * @author Alexander Herzog
 * @see CalcParser#CalcParser(CalcSymbolList)
 * @version 1.1
 */
public class CalcSymbolList {
	/** Variablen, die zus�tzlich zu den normalen Funktionen zur Verf�gung stehen sollen */
	private final String[] variables;

	/** Liste der Konstanten-Symbolen */
	private static final List<CalcSymbolConst> listConst=new ArrayList<>();
	/** Liste der Nachgestellten-Operator-Symbolen */
	private static final List<CalcSymbolPostOperator> listPostOperator=new ArrayList<>();
	/** Liste der Zweistelligen-Operator-Symbolen */
	private static final List<CalcSymbolMiddleOperator> listMiddleOperator=new ArrayList<>();
	/** Liste der Vorangestellten-Operator-Symbolen */
	private static final List<CalcSymbolPreOperator> listPreOperator=new ArrayList<>();
	/** Liste mit den Namen aller Symbole */
	private static final List<String> listNames=new ArrayList<>();
	/** Liste mit den Namen aller Symbole in Kleinbuchstaben */
	private static List<String> listNamesLower;

	/**
	 * Lock um die Initialisierung Thread-sicher zu gestalten
	 * @see #initSymbols()
	 */
	private static final Semaphore initLock=new Semaphore(1);

	/**
	 * Liste der nutzerdefinierten Funktionen
	 * @see #getUserFunctions()
	 */
	private List<CalcSymbolPreOperator> listPreOperatorUser=null;

	/**
	 * Liste der Namen aller Symbole
	 * @see #getAllSymbolNames()
	 */
	private String[] allSymbolNames=null;

	/**
	 * Liste der Namen aller Symbole in Kleinschreibung
	 * @see #getAllSymbolNamesLower(boolean)
	 */
	private String[] allSymbolNamesLower=null;

	/**
	 * Internes Synchronisationsobjekt um eine parallele Initialisierung der Symbole zu verhindern
	 */
	private static final Object initSync=new Object();

	/**
	 * Konstruktor der Klasse <code>CalcSymbolList</code>
	 * @param variables	Variablen, die zus�tzlich zu den normalen Funktionen zur Verf�gung stehen sollen
	 */
	public CalcSymbolList(final String[] variables) {
		this.variables=(variables==null)?new String[0]:variables;
		synchronized(initSync) {
			if (listConst.isEmpty()) {
				initSymbols();
				for (int i=0;i<listPreOperator.size();i++) listNames.addAll(Arrays.asList(listPreOperator.get(i).getNames()));
				for (int i=0;i<listMiddleOperator.size();i++) listNames.addAll(Arrays.asList(listMiddleOperator.get(i).getNames()));
				for (int i=0;i<listPostOperator.size();i++) listNames.addAll(Arrays.asList(listPostOperator.get(i).getNames()));
				for (int i=0;i<listConst.size();i++) listNames.addAll(Arrays.asList(listConst.get(i).getNames()));
				final String[] arr=new String[listNames.size()];
				for (int i=0;i<listNames.size();i++) arr[i]=listNames.get(i).toLowerCase();
				listNamesLower=new ImmutableArrayList<>(arr);
			}
		}
	}

	/**
	 * Liefert die Anzahl an verschiedenen erkannten Symbolen (d.h. ein Symbol in allen seinen Schreibweisen z�hlt nur als ein Symbol).
	 * @return	Anzahl an verschiedenen erkannten Symbolen
	 * @param includeUserFunctions	Sollen auch die �ber {@link #getUserFunctions()} abrufbaren Symbole mitgez�hlt werden?
	 * @see #getAllSymbolNames()
	 */
	public int getSymbolCount(final boolean includeUserFunctions) {
		initSymbols();
		final int userFunctionCount=(includeUserFunctions && getUserFunctions()!=null)?getUserFunctions().size():0;
		return listConst.size()+listPreOperator.size()+listMiddleOperator.size()+listPostOperator.size()+userFunctionCount+variables.length;
	}

	/**
	 * Liefert eine Liste mit zus�tzlichen, nicht bereits in {@link CalcSymbolList} enthaltenen Symbolen
	 * @return	Liste mit zus�tzlichen Symbolen
	 */
	protected List<CalcSymbolPreOperator> getUserFunctions() {
		return null;
	}

	/**
	 * F�gt ein Symbol zu der Liste der bekannten Symbole hinzu.
	 * @param symbol	Neu hinzuzuf�gendes Symbol
	 * @see #initSymbols()
	 * @see #listConst
	 * @see #listPreOperator
	 * @see #listMiddleOperator
	 * @see #listPostOperator
	 */
	private static void addSymbol(final CalcSymbol symbol) {
		switch (symbol.getType()) {
		case TYPE_CONST : listConst.add((CalcSymbolConst)symbol); break;
		case TYPE_FUNCTION : listPreOperator.add((CalcSymbolPreOperator)symbol); break;
		case TYPE_MIDDLE_OPERATOR: listMiddleOperator.add((CalcSymbolMiddleOperator)symbol); break;
		case TYPE_POST_OPERATOR: listPostOperator.add((CalcSymbolPostOperator)symbol); break;
		case TYPE_SUB: /* Gibt's nicht in einer Liste. */ break;
		}
	}

	/**
	 * Initialisiert die Liste der bekannten Symbole
	 */
	private static void initSymbols() {
		initLock.acquireUninterruptibly();
		try {
			if (!listConst.isEmpty()) return;

			addSymbol(new CalcSymbolDiscreteDistributionHyperGeom());
			addSymbol(new CalcSymbolDiscreteDistributionBinomial());
			addSymbol(new CalcSymbolDiscreteDistributionBinomialDirect());
			addSymbol(new CalcSymbolDiscreteDistributionPoisson());
			addSymbol(new CalcSymbolDiscreteDistributionPlanck());
			addSymbol(new CalcSymbolDiscreteDistributionZeta());
			addSymbol(new CalcSymbolDiscreteDistributionNegativeBinomial());
			addSymbol(new CalcSymbolDiscreteDistributionNegativeBinomialDirect());
			addSymbol(new CalcSymbolDiscreteDistributionNegativeHyperGeom());
			addSymbol(new CalcSymbolDiscreteDistributionUniform());
			addSymbol(new CalcSymbolDiscreteDistributionGeometric());
			addSymbol(new CalcSymbolDiscreteDistributionLogarithmic());
			addSymbol(new CalcSymbolDiscreteDistributionBorel());
			addSymbol(new CalcSymbolDiscreteDistributionBoltzmann());

			addSymbol(new CalcSymbolDistributionExp());
			addSymbol(new CalcSymbolDistributionUniform());
			addSymbol(new CalcSymbolDistributionNormal());
			addSymbol(new CalcSymbolDistributionLogNormal());
			addSymbol(new CalcSymbolDistributionGamma());
			addSymbol(new CalcSymbolDistributionGammaDirect());
			addSymbol(new CalcSymbolDistributionBeta());
			addSymbol(new CalcSymbolDistributionBetaDirect());
			addSymbol(new CalcSymbolDistributionWeibull());
			addSymbol(new CalcSymbolDistributionCauchy());
			addSymbol(new CalcSymbolDistributionChiSquare());
			addSymbol(new CalcSymbolDistributionChi());
			addSymbol(new CalcSymbolDistributionF());
			addSymbol(new CalcSymbolDistributionJohnsonSU());
			addSymbol(new CalcSymbolDistributionTriangular());
			addSymbol(new CalcSymbolDistributionTrapezoid());
			addSymbol(new CalcSymbolDistributionPert());
			addSymbol(new CalcSymbolDistributionLaplace());
			addSymbol(new CalcSymbolDistributionPareto());
			addSymbol(new CalcSymbolDistributionLogistic());
			addSymbol(new CalcSymbolDistributionInverseGaussian());
			addSymbol(new CalcSymbolDistributionRayleigh());
			addSymbol(new CalcSymbolDistributionLogLogistic());
			addSymbol(new CalcSymbolDistributionPower());
			addSymbol(new CalcSymbolDistributionGumbel());
			addSymbol(new CalcSymbolDistributionGumbelDirect());
			addSymbol(new CalcSymbolDistributionFatigueLife());
			addSymbol(new CalcSymbolDistributionFrechet());
			addSymbol(new CalcSymbolDistributionHyperbolicSecant());
			addSymbol(new CalcSymbolDistributionSawtoothLeft());
			addSymbol(new CalcSymbolDistributionSawtoothLeftDirect());
			addSymbol(new CalcSymbolDistributionSawtoothRight());
			addSymbol(new CalcSymbolDistributionSawtoothRightDirect());
			addSymbol(new CalcSymbolDistributionLevy());
			addSymbol(new CalcSymbolDistributionMaxwellBoltzmann());
			addSymbol(new CalcSymbolDistributionStudentT());
			addSymbol(new CalcSymbolDistributionHalfNormal());
			addSymbol(new CalcSymbolDistributionUQuadratic());
			addSymbol(new CalcSymbolDistributionReciprocal());
			addSymbol(new CalcSymbolDistributionKumaraswamy());
			addSymbol(new CalcSymbolDistributionIrwinHall());
			addSymbol(new CalcSymbolDistributionIrwinHallDirect());
			addSymbol(new CalcSymbolDistributionSine());
			addSymbol(new CalcSymbolDistributionCosine());
			addSymbol(new CalcSymbolDistributionArcsine());
			addSymbol(new CalcSymbolDistributionWignerHalfCircle());
			addSymbol(new CalcSymbolDistributionLogCauchy());
			addSymbol(new CalcSymbolDistributionLogGamma());
			addSymbol(new CalcSymbolDistributionLogLaplace());
			addSymbol(new CalcSymbolDistributionInverseGamma());
			addSymbol(new CalcSymbolDistributionContinuousBernoulli());
			addSymbol(new CalcSymbolGeneralizedRademacherDistribution());
			addSymbol(new CalcSymbolDistributionHalfCauchy());

			addSymbol(new CalcSymbolEmpiricalDistributionPDF());
			addSymbol(new CalcSymbolEmpiricalDistributionCDF());
			addSymbol(new CalcSymbolEmpiricalDistributionRandom());
			addSymbol(new CalcSymbolEmpiricalDistributionMean());
			addSymbol(new CalcSymbolEmpiricalDistributionMedian());
			addSymbol(new CalcSymbolEmpiricalDistributionQuantil());
			addSymbol(new CalcSymbolEmpiricalDistributionSD());
			addSymbol(new CalcSymbolEmpiricalDistributionVar());
			addSymbol(new CalcSymbolEmpiricalDistributionCV());

			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionExp()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionNormal()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionLogNormal()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionGamma()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionGammaDirect()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionWeibull()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionCauchy()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionChiSquare()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionChi()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionF()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionJohnsonSU()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionLaplace()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionLogistic()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionInverseGaussian()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionRayleigh()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionLogLogistic()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionGumbel()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionFatigueLife()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionFrechet()));
			addSymbol(new CalcSymbolTruncatedDistribution(new CalcSymbolDistributionHyperbolicSecant()));

			addSymbol(new CalcSymbolPreOperatorSqrt());
			addSymbol(new CalcSymbolPreOperatorCbrt());
			addSymbol(new CalcSymbolPreOperatorSqr());
			addSymbol(new CalcSymbolPreOperatorPower());
			addSymbol(new CalcSymbolPreOperatorSinh());
			addSymbol(new CalcSymbolPreOperatorCosh());
			addSymbol(new CalcSymbolPreOperatorTanh());
			addSymbol(new CalcSymbolPreOperatorCoth());
			addSymbol(new CalcSymbolPreOperatorSin());
			addSymbol(new CalcSymbolPreOperatorCos());
			addSymbol(new CalcSymbolPreOperatorTan());
			addSymbol(new CalcSymbolPreOperatorCot());
			addSymbol(new CalcSymbolPreOperatorArcSin());
			addSymbol(new CalcSymbolPreOperatorArcCos());
			addSymbol(new CalcSymbolPreOperatorArcTan());
			addSymbol(new CalcSymbolPreOperatorArcCot());
			addSymbol(new CalcSymbolPreOperatorArcSinh());
			addSymbol(new CalcSymbolPreOperatorArcCosh());
			addSymbol(new CalcSymbolPreOperatorArcTanh());
			addSymbol(new CalcSymbolPreOperatorArcCoth());
			addSymbol(new CalcSymbolPreOperatorExp());
			addSymbol(new CalcSymbolPreOperatorLog());
			addSymbol(new CalcSymbolPreOperatorLg());
			addSymbol(new CalcSymbolPreOperatorLd());
			addSymbol(new CalcSymbolPreOperatorBeta());
			addSymbol(new CalcSymbolPreOperatorGamma());
			addSymbol(new CalcSymbolPreOperatorGammaP());
			addSymbol(new CalcSymbolPreOperatorGammaQ());
			addSymbol(new CalcSymbolPreOperatorZeta());
			addSymbol(new CalcSymbolPreOperatorInverse());
			addSymbol(new CalcSymbolPreOperatorAbs());
			addSymbol(new CalcSymbolPreOperatorMin());
			addSymbol(new CalcSymbolPreOperatorMax());
			addSymbol(new CalcSymbolPreOperatorRange());
			addSymbol(new CalcSymbolPreOperatorFrac());
			addSymbol(new CalcSymbolPreOperatorInt());
			addSymbol(new CalcSymbolPreOperatorSign());
			addSymbol(new CalcSymbolPreOperatorRound());
			addSymbol(new CalcSymbolPreOperatorFloor());
			addSymbol(new CalcSymbolPreOperatorCeil());
			addSymbol(new CalcSymbolPreOperatorTruncate());
			addSymbol(new CalcSymbolPreOperatorFactorial());
			addSymbol(new CalcSymbolPreOperatorBinomial());
			addSymbol(new CalcSymbolPreOperatorRandom());
			addSymbol(new CalcSymbolPreOperatorRandomRange());
			addSymbol(new CalcSymbolPreOperatorRandomRangeInt());
			addSymbol(new CalcSymbolPreOperatorIsPrime());
			addSymbol(new CalcSymbolPreOperatorSum());
			addSymbol(new CalcSymbolPreOperatorMean());
			addSymbol(new CalcSymbolPreOperatorMeanGeometric());
			addSymbol(new CalcSymbolPreOperatorMeanHarmonic());
			addSymbol(new CalcSymbolPreOperatorMedian());
			addSymbol(new CalcSymbolPreOperatorVariance());
			addSymbol(new CalcSymbolPreOperatorStdDev());
			addSymbol(new CalcSymbolPreOperatorSCV());
			addSymbol(new CalcSymbolPreOperatorCV());
			addSymbol(new CalcSymbolPreOperatorSk());
			addSymbol(new CalcSymbolPreOperatorKurt());
			addSymbol(new CalcSymbolPreOperatorErlangC());
			addSymbol(new CalcSymbolPreOperatorAllenCunneen());
			addSymbol(new CalcSymbolPreOperatorModulo());
			addSymbol(new CalcSymbolPreOperatorIf());
			addSymbol(new CalcSymbolPreOperatorLogicAnd());
			addSymbol(new CalcSymbolPreOperatorLogicOr());
			addSymbol(new CalcSymbolPreOperatorLogicXor());
			addSymbol(new CalcSymbolPreOperatorLogicNot());
			addSymbol(new CalcSymbolPreOperatorLogicNAnd());
			addSymbol(new CalcSymbolPreOperatorLogicNOr());
			addSymbol(new CalcSymbolPreOperatorLogicNXor());
			addSymbol(new CalcSymbolPreOperatorLogicEquals());
			addSymbol(new CalcSymbolPreOperatorGcd());
			addSymbol(new CalcSymbolPreOperatorLcm());

			addSymbol(new CalcSymbolMiddleOperatorPlus());
			addSymbol(new CalcSymbolMiddleOperatorMinus());
			addSymbol(new CalcSymbolMiddleOperatorMultiply());
			addSymbol(new CalcSymbolMiddleOperatorDivide());
			addSymbol(new CalcSymbolMiddleOperatorPower());

			addSymbol(new CalcSymbolPostOperatorPercent());
			addSymbol(new CalcSymbolPostOperatorDEGtoRAD());
			addSymbol(new CalcSymbolPostOperatorPower2());
			addSymbol(new CalcSymbolPostOperatorPower3());
			addSymbol(new CalcSymbolPostOperatorFactorial());

			addSymbol(new CalcSymbolConstE());
			addSymbol(new CalcSymbolConstPi());
			addSymbol(new CalcSymbolConstTau());

			addSymbol(new CalcSymbolPreOperatorRandomGeneratorInversX());
			addSymbol(new CalcSymbolPreOperatorRandomGeneratorInvers());
			addSymbol(new CalcSymbolPreOperatorRandomValues());

		} finally {
			initLock.release();
		}
	}

	/**
	 * Liefert eine Liste aller dem System bekannten Symbole (einschlie�lich Variablennamen).
	 * Verschiedene Schreibweisen werden hier als verschiedene Symbole erfasst.
	 * @return	Liste aller bekannten Symbole
	 * @see #getSymbolCount(boolean)
	 */
	public String[] getAllSymbolNames() {
		if (allSymbolNames==null) {
			if (listPreOperatorUser==null) listPreOperatorUser=getUserFunctions();

			int size=0;
			if (listPreOperatorUser!=null) size+=listPreOperatorUser.size()*3;
			size+=listNames.size();
			size+=variables.length;

			final List<String> allSymbolNamesList=new ArrayList<>(size);
			if (listPreOperatorUser!=null) for (int i=0;i<listPreOperatorUser.size();i++) allSymbolNamesList.addAll(Arrays.asList(listPreOperatorUser.get(i).getNames()));
			allSymbolNamesList.addAll(listNames);
			for (final String variable: variables) if (variable!=null) allSymbolNamesList.add(variable);

			allSymbolNames=allSymbolNamesList.toArray(String[]::new);
		}

		return allSymbolNames;
	}

	/**
	 * Wird in {@link #getAllSymbolNamesLower(boolean)} intern bei der
	 * Umwandlung einer Liste in ein Array als Platzhalter f�r
	 * den Typ verwendet. Durch das statische Vorhalten wird
	 * so das wiederholte Anlegen eines leeren Arrays vermieden.
	 */
	private static final String[] emptyArray=new String[0];

	/**
	 * Objekt zur Synchronisation von {@link #getToLowerFromCache(int, boolean)}
	 * und von {@link #putToLowerToCache(String[], int)}
	 */
	private static Object toLowerSync=new Object();

	/**
	 * Hash-Wert des Arrays mit normaler Schreibweise (f�r den Cache)
	 * @see #getToLowerFromCache(int, boolean)
	 * @see #putToLowerToCache(String[], int)
	 */
	private static int toLowerHash=0;

	/**
	 * Array mit Kleinbuchstaben-Eintr�gen (f�r den Cache)
	 * @see #getToLowerFromCache(int, boolean)
	 * @see #putToLowerToCache(String[], int)
	 */
	private static String[] toLowerArr;

	/**
	 * Versucht ein Array mit bereits fr�her erstellten Kleinbuchstaben-Eintr�gen
	 * aus dem Cache zu beziehen
	 * @param hash	Hash-Wert des Arrays mit normaler Schreibweise
	 * @param readOnlyVersion	Im Falle eines Cache-Treffers die Originaldaten aus dem Cache (<code>true</code>) oder eine Kopie davon (<code>true</code>) liefern
	 * @return	Liefert im Erfolgsfall das Array mit Eintr�gen in Kleinschreibung
	 * @see #getAllSymbolNamesLower(boolean)
	 */
	private static String[] getToLowerFromCache(final int hash, final boolean readOnlyVersion) {
		synchronized(toLowerSync) {
			if (hash!=toLowerHash || toLowerArr==null) return null;
			if (readOnlyVersion) {
				return toLowerArr;
			} else {
				return Arrays.copyOf(toLowerArr,toLowerArr.length);
			}
		}
	}

	/**
	 * Speichert ein Array mit Kleinbuchstaben-Eintr�gen zur
	 * sp�teren Verwendung im Cache
	 * @param arr	Zu speicherndes Array
	 * @param hash	Hash-Wert des Arrays mit normaler Schreibweise
	 * @see #getAllSymbolNamesLower(boolean)
	 */
	private static void putToLowerToCache(final String[] arr, final int hash) {
		synchronized(toLowerSync) {
			toLowerHash=hash;
			toLowerArr=Arrays.copyOf(arr,arr.length);
		}
	}

	/**
	 * Liefert eine Liste aller dem System bekannten Symbole (einschlie�lich Variablennamen) in Kleinschreibweise
	 * @param readOnlyVersion	Soll die Liste ver�nderlich sein (<code>false</code>) oder wird sie nur lesend verwendet (<code>true</code>)
	 * @return	Liste aller bekannten Symbole
	 */
	public String[] getAllSymbolNamesLower(final boolean readOnlyVersion) {
		if (allSymbolNamesLower==null) {
			if (listPreOperatorUser==null) listPreOperatorUser=getUserFunctions();

			int size=0;
			if (listPreOperatorUser!=null) size+=listPreOperatorUser.size()*4;
			size+=listNames.size();
			size+=variables.length;

			final List<String> allSymbolNamesList=new ArrayList<>(size);
			if (listPreOperatorUser!=null) for (int i=0;i<listPreOperatorUser.size();i++) allSymbolNamesList.addAll(new ImmutableArrayList<>(listPreOperatorUser.get(i).getNames()));
			allSymbolNamesList.addAll(listNamesLower);
			for (final String variable: variables) if (variable!=null) {
				final String s=variable.trim();
				if (!s.isEmpty()) allSymbolNamesList.add(s);
			}

			int hash=0;
			for (int i=0;i<allSymbolNamesList.size();i++) hash+=allSymbolNamesList.get(i).hashCode();
			final String[] cacheResult=getToLowerFromCache(hash,readOnlyVersion);
			if (cacheResult!=null) return cacheResult;

			allSymbolNamesLower=allSymbolNamesList.toArray(emptyArray);

			final int count=allSymbolNamesLower.length;
			for (int i=0;i<count;i++) {
				boolean needsProcessing=false;
				final int len=allSymbolNamesLower[i].length();
				for (int j=0;j<len;j++) {
					final char c=allSymbolNamesLower[i].charAt(j);
					if (c>='A' && c<='Z') {needsProcessing=true; break;}
				}
				if (needsProcessing) allSymbolNamesLower[i]=allSymbolNamesLower[i].toLowerCase();
			}

			putToLowerToCache(allSymbolNamesLower,hash);
		}
		return allSymbolNamesLower;
	}

	/**
	 * Sucht ein Symbol mit einem bestimmten Namen
	 * @param name	Name zu dem ein passendes Symbol gesucht werden soll
	 * @return	Symbol mit dem angegebenen Namen oder <code>null</code> wenn kein Symbol mit diesem Namen gefunden wurde
	 */
	public CalcSymbol findSymbol(final String name) {
		if (name==null || name.isBlank()) return null;

		CalcSymbol select=null;
		int len=0;
		if (listPreOperatorUser==null) listPreOperatorUser=getUserFunctions();

		if (listPreOperatorUser!=null) for (CalcSymbol sym: listPreOperatorUser) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}

		for (int i=0;i<variables.length;i++) if (variables[i]!=null && variables[i].equalsIgnoreCase(name) && variables[i].length()>len) {
			CalcSymbolVariable variable=new CalcSymbolVariable();
			variable.setData(i);
			select=variable;
			len=variables[i].length();
		}

		for (CalcSymbol sym: listPreOperator) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}
		for (CalcSymbol sym: listMiddleOperator) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}
		for (CalcSymbol sym: listPostOperator) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}
		for (CalcSymbol sym: listConst) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}

		if (select!=null) select=select.cloneSymbol();
		return select;
	}

	/**
	 * Erzeugt ein Symbol, welches eine Zahl repr�sentiert
	 * @param d	Zahl, die in Form eines {@link CalcSymbol}-Objekts ausgedr�ckt werden soll
	 * @return	Objekt zur Verwendung in Formel-Baumstrukturen
	 */
	public CalcSymbolNumber getNumber(final double d) {
		CalcSymbolNumber sym=new CalcSymbolNumber();
		sym.setValue(d);
		return sym;
	}
}