package mathtools.distribution;

import java.io.Serializable;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.special.Beta;

/**
 * Implementierung einer PERT-Verteilung
 * @author Alexander Herzog
 * @see <a href="https://en.wikipedia.org/wiki/PERT_distribution">https://en.wikipedia.org/wiki/PERT_distribution</a>
 */
public final class PertDistributionImpl extends AbstractRealDistribution implements Cloneable, Serializable {
	private static final long serialVersionUID=-5002792590117661632L;

	/**
	 * Untere Grenze des Trägers
	 */
	public final double lowerBound;

	/**
	 * x-Wert des Hochpunkts
	 */
	public final double mostLikely;

	/**
	 * Obere Grenze des Trägers
	 */
	public final double upperBound;

	private final double alpha;
	private final double beta;
	private final double factorPDF;
	private final double factorCDF;

	/**
	 * Konstruktor der Klasse
	 * @param lowerBound	Untere Grenze des Trägers
	 * @param mostLikely	x-Wert des Hochpunkt
	 * @param upperBound	Obere Grenze des Trägers
	 */
	public PertDistributionImpl(final double lowerBound, final double mostLikely, final double upperBound) {
		super(null);
		this.lowerBound=lowerBound;
		this.mostLikely=Math.max(this.lowerBound+0.0001,mostLikely);
		this.upperBound=Math.max(this.mostLikely+0.0001,upperBound);

		alpha=1+4*(this.mostLikely-this.lowerBound)/(this.upperBound-this.lowerBound);
		beta=1+4*(this.upperBound-this.mostLikely)/(this.upperBound-this.lowerBound);
		factorPDF=1/Math.exp(Beta.logBeta(alpha,beta))/Math.pow(this.upperBound-this.lowerBound,alpha+beta-1);
		factorCDF=1/(this.upperBound-this.lowerBound);
	}

	@Override
	public double density(double x) {
		if (x<=lowerBound || x>=upperBound) return 0;
		return factorPDF*Math.pow(x-lowerBound,alpha-1)*Math.pow(upperBound-x,beta-1);
	}

	@Override
	public double cumulativeProbability(double x) {
		if (x<=lowerBound) return 0;
		if (x>=upperBound) return 1;

		final double z=(x-lowerBound)*factorCDF;
		return Beta.regularizedBeta(z,alpha,beta);
	}

	@Override
	public PertDistributionImpl clone() {
		return new PertDistributionImpl(lowerBound,mostLikely,upperBound);
	}

	@Override
	public double getNumericalMean() {
		return (lowerBound+4*mostLikely+upperBound)/6;
	}

	@Override
	public double getNumericalVariance() {
		final double mu=(lowerBound+4*mostLikely+upperBound)/6;
		return (mu-lowerBound)*(upperBound-mu)/7;
	}

	@Override
	public double getSupportLowerBound() {
		return lowerBound;
	}

	@Override
	public double getSupportUpperBound() {
		return upperBound;
	}

	@Override
	public boolean isSupportLowerBoundInclusive() {
		return true;
	}

	@Override
	public boolean isSupportUpperBoundInclusive() {
		return true;
	}

	@Override
	public boolean isSupportConnected() {
		return true;
	}
}
