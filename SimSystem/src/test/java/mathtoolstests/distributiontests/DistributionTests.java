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
package mathtoolstests.distributiontests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.CauchyDistribution;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import mathtools.distribution.ChiDistributionImpl;
import mathtools.distribution.ErlangDistributionImpl;
import mathtools.distribution.ExtBetaDistributionImpl;
import mathtools.distribution.FatigueLifeDistributionImpl;
import mathtools.distribution.FrechetDistributionImpl;
import mathtools.distribution.HyperbolicSecantDistributionImpl;
import mathtools.distribution.InverseGaussianDistributionImpl;
import mathtools.distribution.JohnsonDistributionImpl;
import mathtools.distribution.LaplaceDistributionImpl;
import mathtools.distribution.LogLogisticDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.LogisticDistributionImpl;
import mathtools.distribution.NeverDistributionImpl;
import mathtools.distribution.OnePointDistributionImpl;
import mathtools.distribution.ParetoDistributionImpl;
import mathtools.distribution.PertDistributionImpl;
import mathtools.distribution.PowerDistributionImpl;
import mathtools.distribution.RayleighDistributionImpl;
import mathtools.distribution.SawtoothLeftDistribution;
import mathtools.distribution.SawtoothRightDistribution;
import mathtools.distribution.TriangularDistributionImpl;
import mathtools.distribution.tools.DistributionRandomNumber;
import mathtools.distribution.tools.DistributionTools;

/**
 * Prüft die Funktionsweise der verschiedenen Wahrscheinlichkeitsverteilungen
 * @author Alexander Herzog
 */
class DistributionTests {
	/**
	 * Testet die Funktionen von {@link DistributionTools}
	 * in Bezug auf eine bestimmte Verteilung
	 * @param distribution	Verteilung, die in die {@link DistributionTools}-Funktionen eingesetzt werden soll
	 */
	private void testDistributionTools(final AbstractRealDistribution distribution) {
		final AbstractRealDistribution distribution2=DistributionTools.cloneDistribution(distribution);
		assertTrue(DistributionTools.compare(distribution,distribution2));

		if (!Double.isNaN(distribution.getNumericalMean())) {
			assertEquals(distribution.getNumericalMean(),DistributionTools.getMean(distribution));
			final AbstractRealDistribution distribution3=DistributionTools.setMean(distribution,123);
			if (DistributionTools.canSetMean(distribution)) {
				assertNotNull(distribution3);
			} else {
				assertNull(distribution3);
			}
		}
		if (!Double.isNaN(distribution.getNumericalVariance())) {
			assertEquals(distribution.getNumericalVariance(),FastMath.pow(DistributionTools.getStandardDeviation(distribution),2),0.000001);
			final AbstractRealDistribution distribution3=DistributionTools.setStandardDeviation(distribution,123);
			if (DistributionTools.canSetStandardDeviation(distribution)) {
				assertNotNull(distribution3);
			} else {
				assertNull(distribution3);
			}
		}

		String text;

		text=DistributionTools.distributionToString(distribution);
		assertNotNull(text);
		assertFalse(text.isEmpty());
		final AbstractRealDistribution distribution3=DistributionTools.distributionFromString(text,1000);
		assertNotNull(distribution3);
		assertTrue(DistributionTools.compare(distribution,distribution3));

		text=DistributionTools.getDistributionInfo(distribution);
		assertNotNull(text);
		assertFalse(text.isEmpty());

		text=DistributionTools.getDistributionLongInfo(distribution);
		assertNotNull(text);
		assertFalse(text.isEmpty());
	}

	/**
	 * Testet das Einstellen der Parameter bei einer konkreten Verteilung
	 * @param distribution	Verteilung bei der das Einstellen der Parameter getestet werden soll
	 * @param values	Einzustellende Parameter
	 */
	private void testDistributionParameters(final AbstractRealDistribution distribution, final double[] values) {
		assertEquals(0,DistributionTools.getParameter(distribution,0),0.0001);
		for (int i=0;i<values.length;i++) assertEquals(values[i],DistributionTools.getParameter(distribution,i+1),0.0001);
		for (int i=values.length;i<=4;i++) assertEquals(0,DistributionTools.getParameter(distribution,i+1),0.0001);
		assertEquals(0,DistributionTools.getParameter(distribution,5),0.0001);

		assertNull(DistributionTools.setParameter(distribution,0,123));
		for (int i=0;i<values.length;i++) {
			final AbstractRealDistribution changedDistribution=DistributionTools.setParameter(distribution,i+1,values[i]*2);
			assertNotNull(changedDistribution);
			for (int j=0;j<values.length;j++) {
				assertEquals(((i==j)?2:1)*values[j],DistributionTools.getParameter(changedDistribution,j+1),0.01);
			}
		}
		for (int i=values.length;i<=4;i++) assertNull(DistributionTools.setParameter(distribution,i+1,123));
		assertNull(DistributionTools.setParameter(distribution,5,123));
	}

	/**
	 * Test: Gamma-Verteilung
	 * @see GammaDistribution
	 */
	@Test
	void testGammaDistribution() {
		GammaDistribution gamma;

		gamma=new GammaDistribution(4,5);
		assertEquals(4,gamma.getShape());
		assertEquals(5,gamma.getScale());
		testDistributionParameters(gamma,new double[]{4,5});
		DistributionRandomNumber.random(gamma);

		gamma=new GammaDistribution(1,5);
		DistributionRandomNumber.random(gamma);

		gamma=new GammaDistribution(0.75,5);
		DistributionRandomNumber.random(gamma);
	}

	/**
	 * Test: Erlang-Verteilung
	 * @see ErlangDistributionImpl
	 */
	@Test
	void testErlangDistribution() {
		final ErlangDistributionImpl erlang=new ErlangDistributionImpl(1,2);
		assertTrue(erlang instanceof GammaDistribution);
		testDistributionParameters(erlang,new double[]{1,2});
	}

	/**
	 * Test: Beta-Verteilung
	 * @see ExtBetaDistributionImpl
	 */
	@Test
	void testExtBetaDistribution() {
		final BetaDistribution defaultBeta=new BetaDistribution(2,3);
		ExtBetaDistributionImpl beta;

		beta=new ExtBetaDistributionImpl(4,7,2,3);

		assertEquals(2,beta.getAlpha());
		assertEquals(3,beta.getBeta());
		assertEquals(4,beta.getSupportLowerBound());
		assertEquals(7,beta.getSupportUpperBound());
		assertTrue(beta.isSupportLowerBoundInclusive());
		assertTrue(beta.isSupportUpperBoundInclusive());
		assertEquals(0,beta.density(3));
		assertEquals(0,beta.density(8));
		assertEquals(defaultBeta.density(0.0/3.0),beta.density(4));
		assertEquals(defaultBeta.density(1.0/3.0),beta.density(5));
		assertEquals(defaultBeta.density(2.0/3.0),beta.density(6));
		assertEquals(defaultBeta.density(3.0/3.0),beta.density(7));
		assertEquals(0,beta.cumulativeProbability(3));
		assertEquals(1,beta.cumulativeProbability(8));
		assertEquals(defaultBeta.cumulativeProbability(0.0/3.0),beta.cumulativeProbability(4));
		assertEquals(defaultBeta.cumulativeProbability(1.0/3.0),beta.cumulativeProbability(5));
		assertEquals(defaultBeta.cumulativeProbability(2.0/3.0),beta.cumulativeProbability(6));
		assertEquals(defaultBeta.cumulativeProbability(3.0/3.0),beta.cumulativeProbability(7));

		beta=beta.clone();

		assertEquals(2,beta.getAlpha());
		assertEquals(3,beta.getBeta());
		assertEquals(4,beta.getSupportLowerBound());
		assertEquals(7,beta.getSupportUpperBound());
		assertTrue(beta.isSupportLowerBoundInclusive());
		assertTrue(beta.isSupportUpperBoundInclusive());
		assertEquals(0,beta.density(3));
		assertEquals(0,beta.density(8));
		assertEquals(defaultBeta.density(0.0/3.0),beta.density(4));
		assertEquals(defaultBeta.density(1.0/3.0),beta.density(5));
		assertEquals(defaultBeta.density(2.0/3.0),beta.density(6));
		assertEquals(defaultBeta.density(3.0/3.0),beta.density(7));
		assertEquals(0,beta.cumulativeProbability(3));
		assertEquals(1,beta.cumulativeProbability(8));
		assertEquals(defaultBeta.cumulativeProbability(0.0/3.0),beta.cumulativeProbability(4));
		assertEquals(defaultBeta.cumulativeProbability(1.0/3.0),beta.cumulativeProbability(5));
		assertEquals(defaultBeta.cumulativeProbability(2.0/3.0),beta.cumulativeProbability(6));
		assertEquals(defaultBeta.cumulativeProbability(3.0/3.0),beta.cumulativeProbability(7));

		beta=new ExtBetaDistributionImpl(4,7,2,3,10E-7);

		assertEquals(2,beta.getAlpha());
		assertEquals(3,beta.getBeta());
		assertEquals(4,beta.getSupportLowerBound());
		assertEquals(7,beta.getSupportUpperBound());
		assertTrue(beta.isSupportLowerBoundInclusive());
		assertTrue(beta.isSupportUpperBoundInclusive());
		assertEquals(0,beta.density(3));
		assertEquals(0,beta.density(8));
		assertEquals(defaultBeta.density(0.0/3.0),beta.density(4));
		assertEquals(defaultBeta.density(1.0/3.0),beta.density(5));
		assertEquals(defaultBeta.density(2.0/3.0),beta.density(6));
		assertEquals(defaultBeta.density(3.0/3.0),beta.density(7));
		assertEquals(0,beta.cumulativeProbability(3));
		assertEquals(1,beta.cumulativeProbability(8));
		assertEquals(defaultBeta.cumulativeProbability(0.0/3.0),beta.cumulativeProbability(4));
		assertEquals(defaultBeta.cumulativeProbability(1.0/3.0),beta.cumulativeProbability(5));
		assertEquals(defaultBeta.cumulativeProbability(2.0/3.0),beta.cumulativeProbability(6));
		assertEquals(defaultBeta.cumulativeProbability(3.0/3.0),beta.cumulativeProbability(7));

		testDistributionTools(new ExtBetaDistributionImpl(1,200,2,3)); /* obere Grenze ist wichtig, da beim Test versucht wird, den Erwartungswert 123 einzustellen */
		testDistributionParameters(beta,new double[] {2,3,4,7});
	}

	/**
	 * Test: Inverse Gauß-Verteilung
	 * @see InverseGaussianDistributionImpl
	 */
	@Test
	void testInverseGaussianDistribution() {
		InverseGaussianDistributionImpl inverseGaussian;

		inverseGaussian=new InverseGaussianDistributionImpl(-1,-1);
		assertTrue(inverseGaussian.lambda>0);
		assertTrue(inverseGaussian.mu>0);

		inverseGaussian=new InverseGaussianDistributionImpl(2,3);

		assertEquals(2,inverseGaussian.lambda);
		assertEquals(3,inverseGaussian.mu);
		assertTrue(!inverseGaussian.isSupportLowerBoundInclusive());
		assertTrue(!inverseGaussian.isSupportUpperBoundInclusive());
		assertTrue(inverseGaussian.isSupportConnected());
		assertEquals(0,inverseGaussian.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,inverseGaussian.getSupportUpperBound());
		assertEquals(3,inverseGaussian.getNumericalMean());
		assertEquals(3*3*3/2.0,inverseGaussian.getNumericalVariance());
		assertEquals(0,inverseGaussian.density(0));
		assertEquals(0,inverseGaussian.cumulativeProbability(0));

		inverseGaussian=inverseGaussian.clone();

		assertEquals(2,inverseGaussian.lambda);
		assertEquals(3,inverseGaussian.mu);
		assertTrue(!inverseGaussian.isSupportLowerBoundInclusive());
		assertTrue(!inverseGaussian.isSupportUpperBoundInclusive());
		assertTrue(inverseGaussian.isSupportConnected());
		assertEquals(0,inverseGaussian.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,inverseGaussian.getSupportUpperBound());
		assertEquals(3,inverseGaussian.getNumericalMean());
		assertEquals(3*3*3/2.0,inverseGaussian.getNumericalVariance());
		assertEquals(0,inverseGaussian.density(0));
		assertEquals(0,inverseGaussian.cumulativeProbability(0));

		assertEquals(3,inverseGaussian.random(new DummyRandomGenerator(0.5)));
		assertEquals(3,inverseGaussian.random(new DummyRandomGenerator(new double[]{0.5,0.5})));
		assertEquals(3,inverseGaussian.random(new DummyRandomGenerator(new double[]{0.5,0})));
		assertEquals(3,inverseGaussian.random(new DummyRandomGenerator(new double[]{0.5,0.99})));

		testDistributionTools(inverseGaussian);
		testDistributionParameters(inverseGaussian,new double[] {2,3});
	}

	/**
	 * Test: Johnson-SU-Verteilung
	 * @see JohnsonDistributionImpl
	 */
	@Test
	void testJohnsonDistribution() {
		JohnsonDistributionImpl johnson;

		johnson=new JohnsonDistributionImpl(1,2,3,4);
		assertEquals(1,johnson.gamma);
		assertEquals(2,johnson.xi);
		assertEquals(3,johnson.delta);
		assertEquals(4,johnson.lambda);
		assertEquals(-Double.MAX_VALUE,johnson.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,johnson.getSupportUpperBound());
		assertTrue(!johnson.isSupportLowerBoundInclusive());
		assertTrue(!johnson.isSupportUpperBoundInclusive());
		assertTrue(johnson.isSupportConnected());
		final double mean=johnson.getNumericalMean();
		final double var=johnson.getNumericalVariance();
		assertTrue(johnson.getNumericalVariance()>=0);

		assertEquals(2,johnson.inverseCumulativeProbability(johnson.cumulativeProbability(2)),0.0001);
		assertEquals(3,johnson.inverseCumulativeProbability(johnson.cumulativeProbability(3)),0.0001);
		assertEquals(4,johnson.inverseCumulativeProbability(johnson.cumulativeProbability(4)),0.0001);

		johnson=johnson.clone();
		assertEquals(1,johnson.gamma);
		assertEquals(2,johnson.xi);
		assertEquals(3,johnson.delta);
		assertEquals(4,johnson.lambda);
		assertEquals(-Double.MAX_VALUE,johnson.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,johnson.getSupportUpperBound());
		assertTrue(!johnson.isSupportLowerBoundInclusive());
		assertTrue(!johnson.isSupportUpperBoundInclusive());
		assertTrue(johnson.isSupportConnected());
		assertEquals(mean,johnson.getNumericalMean());
		assertEquals(var,johnson.getNumericalVariance());
		assertTrue(johnson.getNumericalVariance()>=0);

		testDistributionTools(johnson);
		testDistributionParameters(johnson,new double[] {1,2,3,4});
	}

	/**
	 * Test: Laplace-Verteilung
	 * @see LaplaceDistributionImpl
	 */
	@Test
	void testLaplaceDistribution() {
		LaplaceDistributionImpl laplace;

		laplace=new LaplaceDistributionImpl(2,0);
		assertEquals(2,laplace.mu);
		assertEquals(1,laplace.b);

		laplace=new LaplaceDistributionImpl(2,3);
		assertEquals(2,laplace.mu);
		assertEquals(3,laplace.b);
		assertEquals(2,laplace.getNumericalMean());
		assertEquals(2*3*3,laplace.getNumericalVariance());
		assertEquals(-Double.MAX_VALUE,laplace.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,laplace.getSupportUpperBound());
		assertTrue(!laplace.isSupportLowerBoundInclusive());
		assertTrue(!laplace.isSupportUpperBoundInclusive());
		assertTrue(laplace.isSupportConnected());
		assertEquals(0.5/3,laplace.density(2));
		assertEquals(2,laplace.random(new DummyRandomGenerator(0.5)));

		assertEquals(0,laplace.cumulativeProbability(-1_000_000_000),0.0001);
		assertEquals(1,laplace.cumulativeProbability(1_000_000_000),0.0001);

		laplace=laplace.clone();
		assertEquals(2,laplace.mu);
		assertEquals(3,laplace.b);
		assertEquals(2,laplace.getNumericalMean());
		assertEquals(2*3*3,laplace.getNumericalVariance());
		assertEquals(-Double.MAX_VALUE,laplace.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,laplace.getSupportUpperBound());
		assertTrue(!laplace.isSupportLowerBoundInclusive());
		assertTrue(!laplace.isSupportUpperBoundInclusive());
		assertTrue(laplace.isSupportConnected());
		assertEquals(0.5/3,laplace.density(2));
		assertEquals(2,laplace.random(new DummyRandomGenerator(0.5)));

		testDistributionTools(laplace);
		testDistributionParameters(laplace,new double[] {2,3});
	}

	/**
	 * Test: Logistische Verteilung
	 * @see LogisticDistributionImpl
	 */
	@Test
	void testLogisticDistribution() {
		LogisticDistributionImpl logistic;

		logistic=new LogisticDistributionImpl(2,0);
		assertEquals(1,logistic.s);

		logistic=new LogisticDistributionImpl(2,3);
		assertEquals(2,logistic.mu);
		assertEquals(3,logistic.s);
		assertEquals(2,logistic.getNumericalMean());
		assertEquals(3*3*Math.PI*Math.PI/3,logistic.getNumericalVariance());
		assertEquals(-Double.MAX_VALUE,logistic.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,logistic.getSupportUpperBound());
		assertTrue(!logistic.isSupportLowerBoundInclusive());
		assertTrue(!logistic.isSupportUpperBoundInclusive());
		assertTrue(logistic.isSupportConnected());
		assertEquals(1.0/(3*4),logistic.density(2));
		assertEquals(2,logistic.random(new DummyRandomGenerator(0.5)));

		logistic=logistic.clone();
		assertEquals(2,logistic.mu);
		assertEquals(3,logistic.s);
		assertEquals(2,logistic.getNumericalMean());
		assertEquals(3*3*Math.PI*Math.PI/3,logistic.getNumericalVariance());
		assertEquals(-Double.MAX_VALUE,logistic.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,logistic.getSupportUpperBound());
		assertTrue(!logistic.isSupportLowerBoundInclusive());
		assertTrue(!logistic.isSupportUpperBoundInclusive());
		assertTrue(logistic.isSupportConnected());
		assertEquals(1.0/(3*4),logistic.density(2));
		assertEquals(2,logistic.random(new DummyRandomGenerator(0.5)));

		testDistributionTools(logistic);
		testDistributionParameters(logistic,new double[] {2,3});
	}

	/**
	 * Test: Lognormalverteilung
	 * @see LogNormalDistributionImpl
	 */
	@Test
	void testLogNormalDistribution() {
		LogNormalDistributionImpl logNormal;

		logNormal=new LogNormalDistributionImpl();
		assertEquals(1,logNormal.mean);
		assertEquals(1,logNormal.sd);

		final double sigma2=Math.log(FastMath.pow(3.0/2.0,2)+1);
		final double mu=Math.log(2.0)-sigma2/2;
		final double sigma=StrictMath.sqrt(sigma2);

		logNormal=new LogNormalDistributionImpl(2,3);

		assertEquals(2,logNormal.mean);
		assertEquals(3,logNormal.sd);
		assertEquals(mu,logNormal.mu);
		assertEquals(sigma,logNormal.sigma);
		assertEquals(sigma2,logNormal.sigma2);
		assertEquals(0,logNormal.density(0));
		assertEquals(2,logNormal.getNumericalMean());
		assertEquals(3*3,logNormal.getNumericalVariance());
		assertEquals(0,logNormal.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,logNormal.getSupportUpperBound());
		assertTrue(!logNormal.isSupportLowerBoundInclusive());
		assertTrue(!logNormal.isSupportUpperBoundInclusive());
		assertTrue(logNormal.isSupportConnected());
		assertEquals(1,logNormal.inverseCumulativeProbability(logNormal.cumulativeProbability(1)));

		logNormal=logNormal.clone();

		assertEquals(2,logNormal.mean);
		assertEquals(3,logNormal.sd);
		assertEquals(mu,logNormal.mu);
		assertEquals(sigma,logNormal.sigma);
		assertEquals(sigma2,logNormal.sigma2);
		assertEquals(0,logNormal.density(0));
		assertEquals(2,logNormal.getNumericalMean());
		assertEquals(3*3,logNormal.getNumericalVariance());
		assertEquals(0,logNormal.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,logNormal.getSupportUpperBound());
		assertTrue(!logNormal.isSupportLowerBoundInclusive());
		assertTrue(!logNormal.isSupportUpperBoundInclusive());
		assertTrue(logNormal.isSupportConnected());
		assertEquals(1,logNormal.inverseCumulativeProbability(logNormal.cumulativeProbability(1)));

		assertTrue(logNormal.random(new DummyRandomGenerator(new double[] {0.4,0.6}))>0);
		assertTrue(logNormal.random(new DummyRandomGenerator(new double[] {0.4,0.6}))>0); /* Es werden intern immer zwei Zufallszahlen generiert. */

		testDistributionTools(logNormal);
		testDistributionParameters(logNormal,new double[] {2,3});
	}

	/**
	 * Test: Niemals-Verteilung (Masse liegt auf unendlich)
	 * @see NeverDistributionImpl
	 */
	@Test
	void testNeverDistribution() {
		NeverDistributionImpl never;

		never=new NeverDistributionImpl();

		assertTrue(never instanceof OnePointDistributionImpl);
		assertEquals(NeverDistributionImpl.NEVER,never.point);

		never=never.clone();

		assertTrue(never instanceof OnePointDistributionImpl);
		assertEquals(NeverDistributionImpl.NEVER,never.point);

		never=(NeverDistributionImpl)DistributionTools.cloneDistribution(never);

		assertTrue(never instanceof OnePointDistributionImpl);
		assertEquals(NeverDistributionImpl.NEVER,never.point);

		testDistributionTools(never);
	}

	/**
	 * Test: Ein-Punkt-Verteilung
	 * @see OnePointDistributionImpl
	 */
	@Test
	void testOnePointDistribution() {
		OnePointDistributionImpl onePoint;

		onePoint=new OnePointDistributionImpl();
		assertEquals(0,onePoint.point);

		onePoint=new OnePointDistributionImpl(5);
		assertEquals(5,onePoint.point);

		onePoint=onePoint.clone();

		assertEquals(5,onePoint.point);
		assertTrue(onePoint.density(5)>0);
		assertEquals(0,onePoint.density(4));
		assertEquals(0,onePoint.density(6));
		assertEquals(0,onePoint.cumulativeProbability(4));
		assertEquals(1,onePoint.cumulativeProbability(5));
		assertEquals(5,onePoint.inverseCumulativeProbability(0.2));
		assertEquals(5,onePoint.inverseCumulativeProbability(0.4));
		assertEquals(5,onePoint.inverseCumulativeProbability(0.6));
		assertEquals(5,onePoint.getNumericalMean());
		assertEquals(0,onePoint.getNumericalVariance());
		assertEquals(5,onePoint.getSupportLowerBound());
		assertEquals(5,onePoint.getSupportUpperBound());
		assertTrue(onePoint.isSupportLowerBoundInclusive());
		assertTrue(!onePoint.isSupportUpperBoundInclusive());
		assertTrue(onePoint.isSupportConnected());

		assertEquals(5,onePoint.random(new DummyRandomGenerator(0.1)));
		assertEquals(5,onePoint.random(new DummyRandomGenerator(0.5)));
		assertEquals(5,onePoint.random(new DummyRandomGenerator(0.9)));

		testDistributionTools(onePoint);
		testDistributionParameters(onePoint,new double[] {5});
	}

	/**
	 * Test: Pareto-Verteilung
	 * @see ParetoDistributionImpl
	 */
	@Test
	void testParetoDistribution() {
		ParetoDistributionImpl pareto;

		pareto=new ParetoDistributionImpl(-1,3);
		assertEquals(1,pareto.xmin);
		assertEquals(3,pareto.alpha);

		pareto=new ParetoDistributionImpl(2,-1);
		assertEquals(2,pareto.xmin);
		assertEquals(1,pareto.alpha);

		pareto=new ParetoDistributionImpl(2,1);
		assertTrue(pareto.getNumericalMean()>1_000_000_000);

		pareto=new ParetoDistributionImpl(2,2);
		assertTrue(pareto.getNumericalVariance()>1_000_000_000);

		pareto=new ParetoDistributionImpl(2,3);
		assertEquals(2,pareto.xmin);
		assertEquals(3,pareto.alpha);
		assertEquals(0,pareto.density(1));
		assertEquals(3*FastMath.pow(2,3)/FastMath.pow(5,3+1),pareto.density(5));
		assertEquals(0,pareto.cumulativeProbability(1));
		assertEquals(1-FastMath.pow(2.0/5.0,3),pareto.cumulativeProbability(5));
		assertEquals(3*2/(3-1),pareto.getNumericalMean());
		assertEquals(2*2*3.0/(3-1)/(3-1)/(3-2),pareto.getNumericalVariance());
		assertEquals(2,pareto.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,pareto.getSupportUpperBound());
		assertTrue(pareto.isSupportLowerBoundInclusive());
		assertTrue(!pareto.isSupportUpperBoundInclusive());
		assertTrue(pareto.isSupportConnected());
		assertEquals(2.0/FastMath.pow(0.5,1.0/3.0),pareto.random(new DummyRandomGenerator(0.5)));

		pareto=pareto.clone();

		assertEquals(2,pareto.xmin);
		assertEquals(3,pareto.alpha);
		assertEquals(0,pareto.density(1));
		assertEquals(3*FastMath.pow(2,3)/FastMath.pow(5,3+1),pareto.density(5));
		assertEquals(0,pareto.cumulativeProbability(1));
		assertEquals(1-Math.pow(2.0/5.0,3),pareto.cumulativeProbability(5));
		assertEquals(3*2/(3-1),pareto.getNumericalMean());
		assertEquals(2*2*3.0/(3-1)/(3-1)/(3-2),pareto.getNumericalVariance());
		assertEquals(2,pareto.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,pareto.getSupportUpperBound());
		assertTrue(pareto.isSupportLowerBoundInclusive());
		assertTrue(!pareto.isSupportUpperBoundInclusive());
		assertTrue(pareto.isSupportConnected());
		assertEquals(2.0/FastMath.pow(0.5,1.0/3.0),pareto.random(new DummyRandomGenerator(0.5)));

		pareto=(ParetoDistributionImpl)DistributionTools.cloneDistribution(pareto);

		assertEquals(2,pareto.xmin);
		assertEquals(3,pareto.alpha);
		assertEquals(0,pareto.density(1));
		assertEquals(3*FastMath.pow(2,3)/FastMath.pow(5,3+1),pareto.density(5));
		assertEquals(0,pareto.cumulativeProbability(1));
		assertEquals(1-FastMath.pow(2.0/5.0,3),pareto.cumulativeProbability(5));
		assertEquals(3*2/(3-1),pareto.getNumericalMean());
		assertEquals(2*2*3.0/(3-1)/(3-1)/(3-2),pareto.getNumericalVariance());
		assertEquals(2,pareto.getSupportLowerBound());
		assertEquals(Double.MAX_VALUE,pareto.getSupportUpperBound());
		assertTrue(pareto.isSupportLowerBoundInclusive());
		assertTrue(!pareto.isSupportUpperBoundInclusive());
		assertTrue(pareto.isSupportConnected());
		assertEquals(2.0/FastMath.pow(0.5,1.0/3.0),pareto.random(new DummyRandomGenerator(0.5)));

		testDistributionTools(pareto);
		testDistributionParameters(pareto,new double[] {2,3});
	}

	/**
	 * Test: Dreiecksverteilung
	 * @see TriangularDistributionImpl
	 */
	@Test
	void testTriangularDistribution() {
		TriangularDistributionImpl triangular;

		triangular=new TriangularDistributionImpl(2,5,10);
		assertEquals(2,triangular.lowerBound);
		assertEquals(10,triangular.upperBound);
		assertEquals(5,triangular.mostLikelyX);
		assertEquals(1.0/((10-2)*0.5),triangular.mostLikelyY);
		assertEquals(0,triangular.density(1));
		assertEquals(0,triangular.density(11));
		assertEquals(triangular.mostLikelyY,triangular.density(5));
		assertTrue(triangular.density(4)>0);
		assertTrue(triangular.density(4)<triangular.mostLikelyY);
		assertTrue(triangular.density(6)<triangular.mostLikelyY);
		assertTrue(triangular.density(6)>0);
		assertEquals(0,triangular.cumulativeProbability(1));
		assertEquals(1,triangular.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,triangular.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,triangular.inverseCumulativeProbability(2));
		assertEquals(4.0,triangular.inverseCumulativeProbability(triangular.cumulativeProbability(4)));
		assertEquals(6.0,triangular.inverseCumulativeProbability(triangular.cumulativeProbability(6)));
		assertEquals((2+5+10)/3.0,triangular.getNumericalMean());
		assertEquals(2,triangular.getSupportLowerBound());
		assertEquals(10,triangular.getSupportUpperBound());
		assertTrue(triangular.isSupportLowerBoundInclusive());
		assertTrue(triangular.isSupportUpperBoundInclusive());
		assertTrue(triangular.isSupportConnected());

		triangular=triangular.clone();

		assertEquals(2,triangular.lowerBound);
		assertEquals(10,triangular.upperBound);
		assertEquals(5,triangular.mostLikelyX);
		assertEquals(1.0/((10-2)*0.5),triangular.mostLikelyY);
		assertEquals(0,triangular.density(1));
		assertEquals(0,triangular.density(11));
		assertEquals(triangular.mostLikelyY,triangular.density(5));
		assertEquals(0,triangular.cumulativeProbability(1));
		assertEquals(1,triangular.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,triangular.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,triangular.inverseCumulativeProbability(2));
		assertEquals(4,triangular.inverseCumulativeProbability(triangular.cumulativeProbability(4)));
		assertEquals((2+5+10)/3.0,triangular.getNumericalMean());
		assertEquals(2,triangular.getSupportLowerBound());
		assertEquals(10,triangular.getSupportUpperBound());
		assertTrue(triangular.isSupportLowerBoundInclusive());
		assertTrue(triangular.isSupportUpperBoundInclusive());
		assertTrue(triangular.isSupportConnected());

		triangular=(TriangularDistributionImpl)DistributionTools.cloneDistribution(triangular);

		assertEquals(2,triangular.lowerBound);
		assertEquals(10,triangular.upperBound);
		assertEquals(5,triangular.mostLikelyX);
		assertEquals(1.0/((10-2)*0.5),triangular.mostLikelyY);
		assertEquals(0,triangular.density(1));
		assertEquals(0,triangular.density(11));
		assertEquals(triangular.mostLikelyY,triangular.density(5));
		assertEquals(0,triangular.cumulativeProbability(1));
		assertEquals(1,triangular.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,triangular.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,triangular.inverseCumulativeProbability(2));
		assertEquals(4,triangular.inverseCumulativeProbability(triangular.cumulativeProbability(4)));
		assertEquals((2+5+10)/3.0,triangular.getNumericalMean());
		assertEquals(2,triangular.getSupportLowerBound());
		assertEquals(10,triangular.getSupportUpperBound());
		assertTrue(triangular.isSupportLowerBoundInclusive());
		assertTrue(triangular.isSupportUpperBoundInclusive());
		assertTrue(triangular.isSupportConnected());

		testDistributionTools(triangular);
		testDistributionParameters(triangular,new double[] {2,5,10});
	}

	/**
	 * Test: Dreiecksverteilung
	 * @see PertDistributionImpl
	 */
	@Test
	void testPertDistribution() {
		PertDistributionImpl pert;

		pert=new PertDistributionImpl(2,5,10);
		assertEquals(2,pert.lowerBound);
		assertEquals(10,pert.upperBound);
		assertEquals(5,pert.mostLikely);
		assertEquals(0,pert.density(1));
		assertEquals(0,pert.density(11));
		assertTrue(pert.density(4)>0);
		assertTrue(pert.density(6)>0);
		assertEquals(0,pert.cumulativeProbability(1));
		assertEquals(1,pert.cumulativeProbability(11));
		assertEquals((2+4*5+10)/6.0,pert.getNumericalMean());
		assertEquals(2,pert.getSupportLowerBound());
		assertEquals(10,pert.getSupportUpperBound());
		assertTrue(pert.isSupportLowerBoundInclusive());
		assertTrue(pert.isSupportUpperBoundInclusive());
		assertTrue(pert.isSupportConnected());

		pert=pert.clone();

		assertEquals(2,pert.lowerBound);
		assertEquals(10,pert.upperBound);
		assertEquals(5,pert.mostLikely);
		assertEquals(0,pert.density(1));
		assertEquals(0,pert.density(11));
		assertEquals(0,pert.cumulativeProbability(1));
		assertEquals(1,pert.cumulativeProbability(11));
		assertEquals((2+4*5+10)/6.0,pert.getNumericalMean());
		assertEquals(2,pert.getSupportLowerBound());
		assertEquals(10,pert.getSupportUpperBound());
		assertTrue(pert.isSupportLowerBoundInclusive());
		assertTrue(pert.isSupportUpperBoundInclusive());
		assertTrue(pert.isSupportConnected());

		pert=(PertDistributionImpl)DistributionTools.cloneDistribution(pert);

		assertEquals(2,pert.lowerBound);
		assertEquals(10,pert.upperBound);
		assertEquals(5,pert.mostLikely);
		assertEquals(0,pert.density(1));
		assertEquals(0,pert.density(11));
		assertEquals(0,pert.cumulativeProbability(1));
		assertEquals(1,pert.cumulativeProbability(11));
		assertEquals((2+4*5+10)/6.0,pert.getNumericalMean());
		assertEquals(2,pert.getSupportLowerBound());
		assertEquals(10,pert.getSupportUpperBound());
		assertTrue(pert.isSupportLowerBoundInclusive());
		assertTrue(pert.isSupportUpperBoundInclusive());
		assertTrue(pert.isSupportConnected());

		testDistributionTools(pert);
		testDistributionParameters(pert,new double[] {2,5,10});
	}

	/**
	 * Test: F-Verteilung
	 * @see FDistribution
	 */
	@Test
	void testFDistribution() {
		FDistribution f;

		f=new FDistribution(1,6);

		assertEquals(1,f.getNumeratorDegreesOfFreedom());
		assertEquals(6,f.getDenominatorDegreesOfFreedom());
		assertEquals(1.5,f.getNumericalMean());
		assertEquals(11.25,f.getNumericalVariance());
		assertEquals(0,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.isSupportConnected());

		f=(FDistribution)DistributionTools.cloneDistribution(f);

		assertEquals(1,f.getNumeratorDegreesOfFreedom());
		assertEquals(6,f.getDenominatorDegreesOfFreedom());
		assertEquals(1.5,f.getNumericalMean());
		assertEquals(11.25,f.getNumericalVariance());
		assertEquals(0,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.isSupportConnected());

		testDistributionTools(f);
		testDistributionParameters(f,new double[] {1,6});
	}

	/**
	 * Test: Chi-Verteilung (nicht Chi^2)
	 * @see ChiDistributionImpl
	 */
	@Test
	void testChiDistribution() {
		ChiDistributionImpl chi;

		chi=new ChiDistributionImpl(0);
		assertEquals(1,chi.degreesOfFreedom);

		chi=new ChiDistributionImpl(null);
		assertEquals(1,chi.degreesOfFreedom);

		chi=new ChiDistributionImpl(5);
		assertEquals(5,chi.degreesOfFreedom);
		final double mean=chi.getNumericalMean();
		final double var=chi.getNumericalVariance();
		assertEquals(0,chi.getSupportLowerBound());
		assertTrue(!chi.isSupportLowerBoundInclusive());
		assertTrue(chi.getSupportUpperBound()>1_000_000_000);
		assertTrue(!chi.isSupportUpperBoundInclusive());
		assertTrue(chi.isSupportConnected());

		assertEquals(0,chi.density(-1));
		assertEquals(0,chi.density(0));
		assertTrue(chi.density(1)>0);
		assertEquals(0,chi.density(1_000_000_000),0.0001);
		assertEquals(0,chi.cumulativeProbability(-1));
		assertEquals(0,chi.cumulativeProbability(0));
		assertTrue(chi.cumulativeProbability(1)>0);
		assertEquals(1,chi.cumulativeProbability(1_000_000_000),0.0001);

		chi=(ChiDistributionImpl)DistributionTools.cloneDistribution(chi);

		assertEquals(5,chi.degreesOfFreedom);
		assertEquals(mean,chi.getNumericalMean(),0.00001);
		assertEquals(var,chi.getNumericalVariance(),0.00001);
		assertEquals(0,chi.getSupportLowerBound());
		assertTrue(!chi.isSupportLowerBoundInclusive());
		assertTrue(chi.getSupportUpperBound()>1_000_000_000);
		assertTrue(!chi.isSupportUpperBoundInclusive());
		assertTrue(chi.isSupportConnected());

		chi=new ChiDistributionImpl(chi);

		assertEquals(5,chi.degreesOfFreedom);
		assertEquals(mean,chi.getNumericalMean(),0.00001);
		assertEquals(var,chi.getNumericalVariance(),0.00001);
		assertEquals(0,chi.getSupportLowerBound());
		assertTrue(!chi.isSupportLowerBoundInclusive());
		assertTrue(chi.getSupportUpperBound()>1_000_000_000);
		assertTrue(!chi.isSupportUpperBoundInclusive());
		assertTrue(chi.isSupportConnected());

		chi=chi.clone();

		assertEquals(5,chi.degreesOfFreedom);
		assertEquals(mean,chi.getNumericalMean(),0.00001);
		assertEquals(var,chi.getNumericalVariance(),0.00001);
		assertEquals(0,chi.getSupportLowerBound());
		assertTrue(!chi.isSupportLowerBoundInclusive());
		assertTrue(chi.getSupportUpperBound()>1_000_000_000);
		assertTrue(!chi.isSupportUpperBoundInclusive());
		assertTrue(chi.isSupportConnected());

		testDistributionTools(chi);
		testDistributionParameters(chi,new double[] {5});
	}

	/**
	 * Test: Fatigue-Life-Verteilung
	 * @see FatigueLifeDistributionImpl
	 */
	@Test
	void testFatigueLifeDistribution() {
		FatigueLifeDistributionImpl f;

		f=new FatigueLifeDistributionImpl(null);
		assertEquals(0,f.mu);
		assertEquals(1,f.beta);
		assertEquals(1,f.gamma);
		assertEquals(0,f.getSupportLowerBound());

		f=new FatigueLifeDistributionImpl(2,-5,-7);
		assertEquals(2,f.mu);
		assertTrue(f.beta>0);
		assertTrue(f.gamma>0);
		assertEquals(2,f.getSupportLowerBound());

		f=new FatigueLifeDistributionImpl(3,5,7);
		assertEquals(3,f.mu);
		assertEquals(5,f.beta);
		assertEquals(7,f.gamma);
		final double mean=f.getNumericalMean();
		final double var=f.getNumericalVariance();
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		assertEquals(0,f.density(2));
		assertEquals(0,f.density(3));
		assertTrue(f.density(4)>0);
		assertEquals(0,f.density(1_000_000_000),0.0001);

		assertEquals(0,f.cumulativeProbability(2));
		assertEquals(0,f.cumulativeProbability(3));
		assertTrue(f.cumulativeProbability(4)>0);
		assertEquals(1,f.cumulativeProbability(1_000_000_000),0.0001);

		f=(FatigueLifeDistributionImpl)DistributionTools.cloneDistribution(f);

		assertEquals(3,f.mu);
		assertEquals(5,f.beta);
		assertEquals(7,f.gamma);
		assertEquals(mean,f.getNumericalMean(),0.00001);
		assertEquals(var,f.getNumericalVariance(),0.00001);
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		f=new FatigueLifeDistributionImpl(f);

		assertEquals(3,f.mu);
		assertEquals(5,f.beta);
		assertEquals(7,f.gamma);
		assertEquals(mean,f.getNumericalMean(),0.00001);
		assertEquals(var,f.getNumericalVariance(),0.00001);
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		f=f.clone();

		assertEquals(3,f.mu);
		assertEquals(5,f.beta);
		assertEquals(7,f.gamma);
		assertEquals(mean,f.getNumericalMean(),0.00001);
		assertEquals(var,f.getNumericalVariance(),0.00001);
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		testDistributionTools(f);
		testDistributionParameters(f,new double[] {3,5,7});
	}

	/**
	 * Test: Frechet-Verteilung
	 * @see FrechetDistributionImpl
	 */
	@Test
	void testFrechetDistribution() {
		FrechetDistributionImpl f;

		f=new FrechetDistributionImpl(null);
		assertEquals(0,f.delta);
		assertEquals(1,f.beta);
		assertEquals(1,f.alpha);
		assertEquals(0,f.getSupportLowerBound());

		f=new FrechetDistributionImpl(2,-5,-7);
		assertEquals(2,f.delta);
		assertTrue(f.beta>0);
		assertTrue(f.alpha>0);
		assertEquals(2,f.getSupportLowerBound());

		f=new FrechetDistributionImpl(3,5,7);
		assertEquals(3,f.delta);
		assertEquals(5,f.beta);
		assertEquals(7,f.alpha);
		final double mean=f.getNumericalMean();
		final double var=f.getNumericalVariance();
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		assertEquals(0,f.density(2));
		assertEquals(0,f.density(3));
		assertTrue(f.density(10)>0);
		assertEquals(0,f.density(1_000_000_000),0.0001);

		assertEquals(0,f.cumulativeProbability(2));
		assertEquals(0,f.cumulativeProbability(3));
		assertTrue(f.cumulativeProbability(10)>0);
		assertEquals(1,f.cumulativeProbability(1_000_000_000),0.0001);

		final double median=FastMath.pow(1.0/Math.log(2),1/f.alpha)*f.beta+f.delta;
		assertEquals(median,f.random(new DummyRandomGenerator(0.5)),0.01);

		f=(FrechetDistributionImpl)DistributionTools.cloneDistribution(f);

		assertEquals(3,f.delta);
		assertEquals(5,f.beta);
		assertEquals(7,f.alpha);
		assertEquals(mean,f.getNumericalMean(),0.00001);
		assertEquals(var,f.getNumericalVariance(),0.00001);
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		f=new FrechetDistributionImpl(f);

		assertEquals(3,f.delta);
		assertEquals(5,f.beta);
		assertEquals(7,f.alpha);
		assertEquals(mean,f.getNumericalMean(),0.00001);
		assertEquals(var,f.getNumericalVariance(),0.00001);
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		f=f.clone();

		assertEquals(3,f.delta);
		assertEquals(5,f.beta);
		assertEquals(7,f.alpha);
		assertEquals(mean,f.getNumericalMean(),0.00001);
		assertEquals(var,f.getNumericalVariance(),0.00001);
		assertEquals(3,f.getSupportLowerBound());
		assertTrue(!f.isSupportLowerBoundInclusive());
		assertTrue(f.getSupportUpperBound()>1_000_000_000);
		assertTrue(!f.isSupportUpperBoundInclusive());
		assertTrue(f.isSupportConnected());

		testDistributionTools(f);
		testDistributionParameters(f,new double[] {3,5,7});
	}

	/**
	 * Test: Hyperbolische Sekantenverteilung
	 * @see HyperbolicSecantDistributionImpl
	 */
	@Test
	void testHyperbolicSecantDistribution() {
		HyperbolicSecantDistributionImpl h;

		h=new HyperbolicSecantDistributionImpl(null);
		assertEquals(0,h.mu);
		assertEquals(1,h.sigma);
		assertEquals(1,h.sigma2);

		h=new HyperbolicSecantDistributionImpl(2,-3);
		assertEquals(2,h.mu);
		assertTrue(h.sigma>0);
		assertTrue(h.sigma2>0);

		h=new HyperbolicSecantDistributionImpl(2,3);
		assertEquals(2,h.mu);
		assertEquals(3,h.sigma);
		assertEquals(9,h.sigma2);
		assertEquals(2,h.getNumericalMean());
		assertEquals(9,h.getNumericalVariance());
		assertTrue(h.getSupportLowerBound()<-1_000_000_000);
		assertTrue(!h.isSupportLowerBoundInclusive());
		assertTrue(h.getSupportUpperBound()>1_000_000_000);
		assertTrue(!h.isSupportUpperBoundInclusive());
		assertTrue(h.isSupportConnected());

		assertEquals(0,h.density(-1_000_000_000),0.0001);
		assertTrue(h.density(3)>0);
		assertEquals(0,h.density(1_000_000_000),0.0001);
		assertEquals(0,h.cumulativeProbability(-1_000_000_000));
		assertTrue(h.cumulativeProbability(3)>0);
		assertEquals(1,h.cumulativeProbability(1_000_000_000),0.0001);

		assertEquals(2,h.random(new DummyRandomGenerator(0.5)),0.0001);

		h=(HyperbolicSecantDistributionImpl)DistributionTools.cloneDistribution(h);

		assertEquals(2,h.mu);
		assertEquals(3,h.sigma);
		assertEquals(9,h.sigma2);
		assertEquals(2,h.getNumericalMean());
		assertEquals(9,h.getNumericalVariance());
		assertTrue(h.getSupportLowerBound()<-1_000_000_000);
		assertTrue(!h.isSupportLowerBoundInclusive());
		assertTrue(h.getSupportUpperBound()>1_000_000_000);
		assertTrue(!h.isSupportUpperBoundInclusive());
		assertTrue(h.isSupportConnected());

		h=new HyperbolicSecantDistributionImpl(h);

		assertEquals(2,h.mu);
		assertEquals(3,h.sigma);
		assertEquals(9,h.sigma2);
		assertEquals(2,h.getNumericalMean());
		assertEquals(9,h.getNumericalVariance());
		assertTrue(h.getSupportLowerBound()<-1_000_000_000);
		assertTrue(!h.isSupportLowerBoundInclusive());
		assertTrue(h.getSupportUpperBound()>1_000_000_000);
		assertTrue(!h.isSupportUpperBoundInclusive());
		assertTrue(h.isSupportConnected());

		h=h.clone();

		assertEquals(2,h.mu);
		assertEquals(3,h.sigma);
		assertEquals(9,h.sigma2);
		assertEquals(2,h.getNumericalMean());
		assertEquals(9,h.getNumericalVariance());
		assertTrue(h.getSupportLowerBound()<-1_000_000_000);
		assertTrue(!h.isSupportLowerBoundInclusive());
		assertTrue(h.getSupportUpperBound()>1_000_000_000);
		assertTrue(!h.isSupportUpperBoundInclusive());
		assertTrue(h.isSupportConnected());

		testDistributionTools(h);
		testDistributionParameters(h,new double[] {2,3});
	}

	/**
	 * Test: Log-Logistische Verteilung
	 * @see LogLogisticDistributionImpl
	 */
	@Test
	void testLogLogistic() {
		LogLogisticDistributionImpl logLogistic;

		logLogistic=new LogLogisticDistributionImpl(null);
		assertEquals(1,logLogistic.alpha);
		assertEquals(1,logLogistic.beta);

		logLogistic=new LogLogisticDistributionImpl(-2,-3);
		assertTrue(logLogistic.alpha>0);
		assertTrue(logLogistic.beta>0);

		logLogistic=new LogLogisticDistributionImpl(2,1);
		assertEquals(0,logLogistic.getNumericalMean());

		logLogistic=new LogLogisticDistributionImpl(2,2);
		assertEquals(0,logLogistic.getNumericalVariance());

		logLogistic=new LogLogisticDistributionImpl(2,3);
		assertEquals(2,logLogistic.alpha);
		assertEquals(3,logLogistic.beta);
		final double mean=logLogistic.getNumericalMean();
		final double var=logLogistic.getNumericalVariance();
		assertEquals(0,logLogistic.getSupportLowerBound());
		assertTrue(logLogistic.isSupportLowerBoundInclusive());
		assertTrue(logLogistic.getSupportUpperBound()>1_000_000_000);
		assertTrue(!logLogistic.isSupportUpperBoundInclusive());
		assertTrue(logLogistic.isSupportConnected());

		assertEquals(0,logLogistic.density(-1));
		assertTrue(logLogistic.density(3)>0);
		assertEquals(0,logLogistic.density(1_000_000_000),0.0001);
		assertEquals(0,logLogistic.cumulativeProbability(0));
		assertTrue(logLogistic.cumulativeProbability(3)>0);
		assertEquals(1,logLogistic.cumulativeProbability(1_000_000_000),0.0001);

		assertEquals(2,logLogistic.random(new DummyRandomGenerator(0.5)),0.0001);

		logLogistic=(LogLogisticDistributionImpl)DistributionTools.cloneDistribution(logLogistic);

		assertEquals(2,logLogistic.alpha);
		assertEquals(3,logLogistic.beta);
		assertEquals(mean,logLogistic.getNumericalMean());
		assertEquals(var,logLogistic.getNumericalVariance());
		assertEquals(0,logLogistic.getSupportLowerBound());
		assertTrue(logLogistic.isSupportLowerBoundInclusive());
		assertTrue(logLogistic.getSupportUpperBound()>1_000_000_000);
		assertTrue(!logLogistic.isSupportUpperBoundInclusive());
		assertTrue(logLogistic.isSupportConnected());

		logLogistic=new LogLogisticDistributionImpl(logLogistic);

		assertEquals(2,logLogistic.alpha);
		assertEquals(3,logLogistic.beta);
		assertEquals(mean,logLogistic.getNumericalMean());
		assertEquals(var,logLogistic.getNumericalVariance());
		assertEquals(0,logLogistic.getSupportLowerBound());
		assertTrue(logLogistic.isSupportLowerBoundInclusive());
		assertTrue(logLogistic.getSupportUpperBound()>1_000_000_000);
		assertTrue(!logLogistic.isSupportUpperBoundInclusive());
		assertTrue(logLogistic.isSupportConnected());

		logLogistic=logLogistic.clone();

		assertEquals(2,logLogistic.alpha);
		assertEquals(3,logLogistic.beta);
		assertEquals(mean,logLogistic.getNumericalMean());
		assertEquals(var,logLogistic.getNumericalVariance());
		assertEquals(0,logLogistic.getSupportLowerBound());
		assertTrue(logLogistic.isSupportLowerBoundInclusive());
		assertTrue(logLogistic.getSupportUpperBound()>1_000_000_000);
		assertTrue(!logLogistic.isSupportUpperBoundInclusive());
		assertTrue(logLogistic.isSupportConnected());

		testDistributionTools(logLogistic);
		testDistributionParameters(logLogistic,new double[] {2,3});
	}

	/**
	 * Test: Potenzverteilung
	 * @see PowerDistributionImpl
	 */
	@Test
	void testPowerDistribution() {
		PowerDistributionImpl power;

		power=new PowerDistributionImpl(null);
		assertEquals(0,power.a);
		assertEquals(1,power.b);
		assertEquals(1,power.c);

		power=new PowerDistributionImpl(-2,-3,-5);
		assertEquals(-2,power.a);
		assertTrue(power.b>power.a);
		assertTrue(power.c>0);

		power=new PowerDistributionImpl(1,2,3);
		assertEquals(1,power.a);
		assertEquals(2,power.b);
		assertEquals(3,power.c);
		assertEquals(1.0+3.0/4,power.getNumericalMean());
		assertEquals(3.0/(4*4*5),power.getNumericalVariance());
		assertEquals(1,power.getSupportLowerBound());
		assertTrue(power.isSupportLowerBoundInclusive());
		assertEquals(2,power.getSupportUpperBound());
		assertTrue(power.isSupportUpperBoundInclusive());
		assertTrue(power.isSupportConnected());

		assertEquals(0,power.density(0));
		assertTrue(power.density(1.5)>0);
		assertEquals(0,power.density(4));
		assertEquals(0,power.cumulativeProbability(0));
		assertTrue(power.cumulativeProbability(1.5)>0);
		assertEquals(1,power.cumulativeProbability(4));

		assertTrue(power.inverseCumulativeProbability(-0.1)<=1);
		assertTrue(power.inverseCumulativeProbability(0)<=1);
		assertEquals(1.25,power.inverseCumulativeProbability(power.cumulativeProbability(1.25)));
		assertEquals(1.5,power.inverseCumulativeProbability(power.cumulativeProbability(1.5)));
		assertEquals(1.75,power.inverseCumulativeProbability(power.cumulativeProbability(1.75)));
		assertTrue(power.inverseCumulativeProbability(1)>=2);
		assertTrue(power.inverseCumulativeProbability(1.1)>=2);

		assertEquals(1.0+3.0/4,power.random(new DummyRandomGenerator(0.5)),0.05);

		power=(PowerDistributionImpl)DistributionTools.cloneDistribution(power);

		assertEquals(1,power.a);
		assertEquals(2,power.b);
		assertEquals(3,power.c);
		assertEquals(1.0+3.0/4,power.getNumericalMean());
		assertEquals(3.0/(4*4*5),power.getNumericalVariance());
		assertEquals(1,power.getSupportLowerBound());
		assertTrue(power.isSupportLowerBoundInclusive());
		assertEquals(2,power.getSupportUpperBound());
		assertTrue(power.isSupportUpperBoundInclusive());
		assertTrue(power.isSupportConnected());

		power=new PowerDistributionImpl(power);

		assertEquals(1,power.a);
		assertEquals(2,power.b);
		assertEquals(3,power.c);
		assertEquals(1.0+3.0/4,power.getNumericalMean());
		assertEquals(3.0/(4*4*5),power.getNumericalVariance());
		assertEquals(1,power.getSupportLowerBound());
		assertTrue(power.isSupportLowerBoundInclusive());
		assertEquals(2,power.getSupportUpperBound());
		assertTrue(power.isSupportUpperBoundInclusive());
		assertTrue(power.isSupportConnected());

		power=power.clone();

		assertEquals(1,power.a);
		assertEquals(2,power.b);
		assertEquals(3,power.c);
		assertEquals(1.0+3.0/4,power.getNumericalMean());
		assertEquals(3.0/(4*4*5),power.getNumericalVariance());
		assertEquals(1,power.getSupportLowerBound());
		assertTrue(power.isSupportLowerBoundInclusive());
		assertEquals(2,power.getSupportUpperBound());
		assertTrue(power.isSupportUpperBoundInclusive());
		assertTrue(power.isSupportConnected());

		testDistributionTools(power);
		testDistributionParameters(power,new double[] {1,2,3});
	}

	/**
	 * Test: Rayleigh-Verteilung
	 * @see RayleighDistributionImpl
	 */
	@Test
	void testRayleighDistribution() {
		RayleighDistributionImpl rayleigh;

		rayleigh=new RayleighDistributionImpl(null);
		assertTrue(rayleigh.sigma>0);

		rayleigh=new RayleighDistributionImpl(0);
		assertTrue(rayleigh.sigma>0);

		rayleigh=new RayleighDistributionImpl(3);
		assertEquals(3*Math.sqrt(2/Math.PI),rayleigh.sigma);
		assertEquals(3,rayleigh.mean);
		assertEquals(3,rayleigh.getNumericalMean());
		assertEquals((4-Math.PI)/2*3*3*2/Math.PI,rayleigh.getNumericalVariance(),0.000001);
		assertEquals(0,rayleigh.getSupportLowerBound());
		assertTrue(rayleigh.isSupportLowerBoundInclusive());
		assertTrue(rayleigh.getSupportUpperBound()>1_000_000_000);
		assertTrue(!rayleigh.isSupportUpperBoundInclusive());
		assertTrue(rayleigh.isSupportConnected());

		assertEquals(0,rayleigh.density(-1));
		assertTrue(rayleigh.density(1.5)>0);
		assertEquals(0,rayleigh.density(1_000_000_000),0.00001);
		assertEquals(0,rayleigh.cumulativeProbability(0));
		assertTrue(rayleigh.cumulativeProbability(1.5)>0);
		assertEquals(1,rayleigh.cumulativeProbability(1_000_000_000),0.00001);

		assertTrue(rayleigh.inverseCumulativeProbability(-0.1)<=1);
		assertTrue(rayleigh.inverseCumulativeProbability(0)<=1);
		assertEquals(1,rayleigh.inverseCumulativeProbability(rayleigh.cumulativeProbability(1)),0.00001);
		assertEquals(2,rayleigh.inverseCumulativeProbability(rayleigh.cumulativeProbability(2)),0.00001);
		assertEquals(3,rayleigh.inverseCumulativeProbability(rayleigh.cumulativeProbability(3)),0.00001);
		assertEquals(4,rayleigh.inverseCumulativeProbability(rayleigh.cumulativeProbability(4)),0.00001);
		assertEquals(5,rayleigh.inverseCumulativeProbability(rayleigh.cumulativeProbability(5)),0.00001);
		assertTrue(rayleigh.inverseCumulativeProbability(1.1)>1_000_000_000);

		assertEquals(rayleigh.mean,rayleigh.random(new DummyRandomGenerator(0.5)),0.5); /* Abweichung Mittelwert<->Medien ist recht hoch, daher trifft Zufallszahl in der Mitte den Mittelwert nur bedingt. */

		rayleigh=(RayleighDistributionImpl)DistributionTools.cloneDistribution(rayleigh);

		assertEquals(3*Math.sqrt(2/Math.PI),rayleigh.sigma);
		assertEquals(3,rayleigh.mean);
		assertEquals(3,rayleigh.getNumericalMean());
		assertEquals((4-Math.PI)/2*3*3*2/Math.PI,rayleigh.getNumericalVariance(),0.000001);
		assertEquals(0,rayleigh.getSupportLowerBound());
		assertTrue(rayleigh.isSupportLowerBoundInclusive());
		assertTrue(rayleigh.getSupportUpperBound()>1_000_000_000);
		assertTrue(!rayleigh.isSupportUpperBoundInclusive());
		assertTrue(rayleigh.isSupportConnected());

		rayleigh=new RayleighDistributionImpl(rayleigh);

		assertEquals(3*Math.sqrt(2/Math.PI),rayleigh.sigma);
		assertEquals(3,rayleigh.mean);
		assertEquals(3,rayleigh.getNumericalMean());
		assertEquals((4-Math.PI)/2*3*3*2/Math.PI,rayleigh.getNumericalVariance(),0.000001);
		assertEquals(0,rayleigh.getSupportLowerBound());
		assertTrue(rayleigh.isSupportLowerBoundInclusive());
		assertTrue(rayleigh.getSupportUpperBound()>1_000_000_000);
		assertTrue(!rayleigh.isSupportUpperBoundInclusive());
		assertTrue(rayleigh.isSupportConnected());

		rayleigh=rayleigh.clone();

		assertEquals(3*Math.sqrt(2/Math.PI),rayleigh.sigma);
		assertEquals(3,rayleigh.mean);
		assertEquals(3,rayleigh.getNumericalMean());
		assertEquals((4-Math.PI)/2*3*3*2/Math.PI,rayleigh.getNumericalVariance(),0.000001);
		assertEquals(0,rayleigh.getSupportLowerBound());
		assertTrue(rayleigh.isSupportLowerBoundInclusive());
		assertTrue(rayleigh.getSupportUpperBound()>1_000_000_000);
		assertTrue(!rayleigh.isSupportUpperBoundInclusive());
		assertTrue(rayleigh.isSupportConnected());

		testDistributionTools(rayleigh);
		testDistributionParameters(rayleigh,new double[] {3});
	}

	/**
	 * Test: Sägezahnverteilung (links)
	 * @see SawtoothLeftDistribution
	 */
	@Test
	void testSawtoothLeftDistribution() {
		SawtoothLeftDistribution sawtooth;

		sawtooth=new SawtoothLeftDistribution(5,10);
		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((2*sawtooth.a+sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		sawtooth=sawtooth.clone();

		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((2*sawtooth.a+sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		sawtooth=new SawtoothLeftDistribution(sawtooth);

		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((2*sawtooth.a+sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		sawtooth=(SawtoothLeftDistribution)DistributionTools.cloneDistribution(sawtooth);

		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((2*sawtooth.a+sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		testDistributionTools(sawtooth);
		testDistributionParameters(sawtooth,new double[] {5,10});

		double rnd=sawtooth.random(new DummyRandomGenerator(0.5));
		assertTrue(rnd>=sawtooth.a);
		assertTrue(rnd<=sawtooth.b);
	}

	/**
	 * Test: Sägezahnverteilung (rechts)
	 * @see SawtoothRightDistribution
	 */
	@Test
	void testSawtoothRightDistribution() {
		SawtoothRightDistribution sawtooth;

		sawtooth=new SawtoothRightDistribution(5,10);
		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((sawtooth.a+2*sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		sawtooth=sawtooth.clone();

		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((sawtooth.a+2*sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		sawtooth=new SawtoothRightDistribution(sawtooth);

		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((sawtooth.a+2*sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		sawtooth=(SawtoothRightDistribution)DistributionTools.cloneDistribution(sawtooth);

		assertEquals(5,sawtooth.a);
		assertEquals(10,sawtooth.b);
		assertEquals(0,sawtooth.density(4));
		assertEquals(0,sawtooth.density(11));
		assertTrue(sawtooth.density(6)>0);
		assertTrue(sawtooth.density(9)>0);
		assertEquals(0,sawtooth.cumulativeProbability(4));
		assertEquals(1,sawtooth.cumulativeProbability(11));
		assertEquals(-Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(-1));
		assertEquals(Double.MAX_VALUE,sawtooth.inverseCumulativeProbability(2));
		assertEquals(6.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(6)));
		assertEquals(9.0,sawtooth.inverseCumulativeProbability(sawtooth.cumulativeProbability(9)));
		assertEquals((sawtooth.a+2*sawtooth.b)/3.0,sawtooth.getNumericalMean());
		assertEquals(5,sawtooth.getSupportLowerBound());
		assertEquals(10,sawtooth.getSupportUpperBound());
		assertTrue(sawtooth.isSupportLowerBoundInclusive());
		assertTrue(sawtooth.isSupportUpperBoundInclusive());
		assertTrue(sawtooth.isSupportConnected());

		testDistributionTools(sawtooth);
		testDistributionParameters(sawtooth,new double[] {5,10});

		double rnd=sawtooth.random(new DummyRandomGenerator(0.5));
		assertTrue(rnd>=sawtooth.a);
		assertTrue(rnd<=sawtooth.b);
	}

	/**
	 * Test: Auch in CommonMath vordefinierte Verteilungen soll der {@link DistributionTools}-Wrapper
	 * korrekt verarbeiten können.
	 */
	@Test
	void testCommonMathWrappers() {
		AbstractRealDistribution distribution;

		distribution=new NormalDistribution(2,3);
		testDistributionTools(distribution);
		testDistributionParameters(distribution,new double[] {2,3});

		distribution=new CauchyDistribution(2,3);
		testDistributionTools(distribution);
		testDistributionParameters(distribution,new double[] {2,3});

		distribution=new WeibullDistribution(2,3);
		testDistributionTools(distribution);
		testDistributionParameters(distribution,new double[] {1.0/3,2});
	}
}