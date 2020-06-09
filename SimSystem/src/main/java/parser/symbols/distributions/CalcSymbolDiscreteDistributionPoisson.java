package parser.symbols.distributions;

import mathtools.Functions;

/**
 * Poisson-Verteilung
 * @author Alexander Herzog
 * @version 1.0
 */
public class CalcSymbolDiscreteDistributionPoisson extends CalcSymbolDiscreteDistribution {
	@Override
	public String[] getNames() {
		return new String[]{"PoissonDistribution","PoissonDist","PoissonVerteilung"};
	}

	@Override
	protected int getParameterCount() {
		return 1;
	}

	@Override
	protected double calcProbability(double[] parameters, int k) {
		final double lambda=parameters[0];

		if (lambda<=0) return -1;

		return Math.pow(lambda,k)/Functions.getFactorial(k)*Math.exp(-lambda);
	}
}
