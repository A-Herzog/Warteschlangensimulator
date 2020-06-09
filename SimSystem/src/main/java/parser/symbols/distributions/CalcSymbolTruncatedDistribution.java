package parser.symbols.distributions;

import java.util.ArrayList;
import java.util.List;

import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Abstrakte Basisklasse, die Funktionen zur Erzeugung von Zufallszahlen
 * gemäß bestimmter Verteilungen und mit einem künstlich limitierten Träger
 * bereitstellt.
 * @author Alexander Herzog
 */
public class CalcSymbolTruncatedDistribution extends CalcSymbolPreOperator {
	private final static int MAX_RND=100;

	private final CalcSymbolDistribution innerDistribution;
	private final int parameterCount;
	private final double [] distParameters;

	/**
	 * Konstruktor der Klasse
	 * @param innerDistribution	Eingebettete Verteilung
	 */
	public CalcSymbolTruncatedDistribution(final CalcSymbolDistribution innerDistribution) {
		this.innerDistribution=innerDistribution;
		parameterCount=innerDistribution.getParameterCount();
		distParameters=new double[parameterCount];
	}

	@Override
	public String[] getNames() {
		final List<String> names=new ArrayList<>();
		for (String name: innerDistribution.getNames()) {
			names.add(name+"Range");
			names.add(name+"Bereich");
		}
		return names.toArray(new String[0]);
	}

	private double getRandom(final double min, final double max) throws MathCalcError {
		int count=0;
		double rnd=innerDistribution.calc(distParameters);
		while (rnd<min || rnd>max) {
			if (count>MAX_RND) return (min+max)/2;
			rnd=innerDistribution.calc(distParameters);
			count++;
		}
		return rnd;
	}

	private double getRandom(final double min, final double max, final double fallbackValue) {
		int count=0;
		double rnd=innerDistribution.calcOrDefault(distParameters,fallbackValue);
		while (rnd<min || rnd>max) {
			if (count>MAX_RND) return (min+max)/2;
			rnd=innerDistribution.calcOrDefault(distParameters,fallbackValue);
			count++;
		}
		return rnd;
	}

	@Override
	protected final double calc(double[] parameters) throws MathCalcError {
		/* Zufallszahl */
		if (parameters.length==parameterCount+2) {
			final double min=parameters[0];
			final double max=parameters[1];
			if (min>=max) throw error();
			for (int i=0;i<parameterCount;i++) distParameters[i]=parameters[i+2];
			return getRandom(min,max);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		/* Zufallszahl */
		if (parameters.length==parameterCount+2) {
			final double min=parameters[0];
			final double max=parameters[1];
			if (min>=max) return fallbackValue;
			for (int i=0;i<parameterCount;i++) distParameters[i]=parameters[i+2];
			return getRandom(min,max,fallbackValue);
		}

		return fallbackValue;
	}

	@Override
	protected boolean isDeterministic() {
		return false;
	}
}
