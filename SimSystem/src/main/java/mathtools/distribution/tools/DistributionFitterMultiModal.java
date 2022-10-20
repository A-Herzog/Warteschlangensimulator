/**
 * Copyright 2022 Alexander Herzog
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BisectionSolver;
import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;

/**
 * Versucht eine multimodale Verteilung in mehrere Log-Normalverteilungen zu zerlegen.
 * @author Alexander Herzog
 */
public class DistributionFitterMultiModal extends DistributionFitterBase {
	/**
	 * Bezeichner für "Für die Anpassung verwendete Verteilung";
	 * @see #process(DataDistributionImpl)
	 */
	public static String usedDistribution="Für die Anpassung verwendete Verteilung";

	/**
	 * Bezeichner für "Verteilungsanpassung Schritt %d";
	 * @see #processStep(DataDistributionImpl)
	 */
	public static String step="Verteilungsanpassung Schritt %d";

	/**
	 * Bezeichner für "Modalwert der Verteilung"
	 * @see #processStep(DataDistributionImpl)
	 */
	public static String mode="Modalwert der Verteilung";

	/**
	 * Bezeichner für "Es konnte kein Modalwert ermittelt werden. Ende der Anpassung."
	 * @see #processStep(DataDistributionImpl)
	 */
	public static String noMode="Es konnte kein Modalwert ermittelt werden. Ende der Anpassung.";

	/**
	 * Bezeichner für "Näherung"
	 * @see #processStep(DataDistributionImpl)
	 */
	public static String approximation="Näherung";

	/**
	 * Bezeichner für "Anteil"
	 * @see #processStep(DataDistributionImpl)
	 */
	public static String fraction="Anteil";

	/**
	 * Bezeichner für "Nachoptimierung der Anteile der Verteilungen"
	 * @see #optimizeFractions(DataDistributionImpl)
	 */
	public static String fractionsPostOptimization="Nachoptimierung der Anteile der Verteilungen";

	/**
	 * Bezeichner für "Bisherige Anteile"
	 * @see #optimizeFractions(DataDistributionImpl)
	 */
	public static String fractionsPostOptimizationCurrent="Bisherige Anteile";

	/**
	 * Bezeichner für "Optimierte Anteile"
	 * @see #optimizeFractions(DataDistributionImpl)
	 */
	public static String fractionsPostOptimizationNew="Optimierte Anteile";

	/**
	 * Bezeichner für "Rechenbefehl für die zusammengesetzte Verteilung";
	 * @see #process(DataDistributionImpl)
	 */
	public static String calculationCommand="Rechenbefehl für die zusammengesetzte Verteilung";

	/**
	 * Welche Verteilung soll für die Teildichten verwendet werden?
	 */
	public enum FitDistribution {
		/** Fittet gegen die Log-Normalverteilung */
		LOG_NORMAL,
		/** Fittet gegen die Gamma-Verteilung */
		GAMMA
	}

	/**
	 * Welche Verteilung soll für die Teildichten verwendet werden?
	 */
	private final FitDistribution fitDistribution;

	/**
	 * Modalwerte der Teilverteilungen
	 */
	private final List<Integer> fitMode;

	/**
	 * Erwartungswerte der Teilverteilungen
	 */
	private final List<Double> fitMean;

	/**
	 * Standardabweichungen der Teilverteilungen
	 */
	private final List<Double> fitSd;

	/**
	 * Anteile der Teilverteilungen an der Gesamtverteilung
	 */
	private final List<Double> fitFraction;

	/**
	 * Soll der Rechenbefehl, der notwendig ist, um Zufallszahlen gemäß der zusammengesetzten Verteilung zu generieren, mit ausgegeben werden?
	 */
	private boolean showCalculationCommand;

	/**
	 * Konstruktor der Klasse
	 * @param fitDistribution	Welche Verteilung soll für die Teildichten verwendet werden?
	 */
	public DistributionFitterMultiModal(final FitDistribution fitDistribution) {
		this.fitDistribution=fitDistribution;
		fitMode=new ArrayList<>();
		fitMean=new ArrayList<>();
		fitSd=new ArrayList<>();
		fitFraction=new ArrayList<>();
		showCalculationCommand=false;
		clear();
	}

	/**
	 * Setzt alle geladenen Daten und Verarbeitetungsergebnisse zurück.
	 */
	@Override
	public void clear() {
		super.clear();
		fitMode.clear();
		fitMean.clear();
		fitSd.clear();
		fitFraction.clear();
	}


	/**
	 * Gibt an, ob der Rechenbefehl, der notwendig ist, um Zufallszahlen gemäß der zusammengesetzten Verteilung zu generieren, mit ausgegeben werden soll.
	 * @return	Soll der Rechenbefehl, der notwendig ist, um Zufallszahlen gemäß der zusammengesetzten Verteilung zu generieren, mit ausgegeben werden?
	 */
	public boolean isShowCalculationCommand() {
		return showCalculationCommand;
	}

	/**
	 * Stellt ein, ob der Rechenbefehl, der notwendig ist, um Zufallszahlen gemäß der zusammengesetzten Verteilung zu generieren, mit ausgegeben werden soll.
	 * @param showCalculationCommand	Soll der Rechenbefehl, der notwendig ist, um Zufallszahlen gemäß der zusammengesetzten Verteilung zu generieren, mit ausgegeben werden?
	 */
	public void setShowCalculationCommand(boolean showCalculationCommand) {
		this.showCalculationCommand=showCalculationCommand;
	}

	/**
	 * Liefert den Typ der Verteilung gegen die gefittet werden soll.
	 * @return	Typ der Verteilung gegen die gefittet werden soll
	 */
	public FitDistribution getFitDistribution() {
		return fitDistribution;
	}

	/**
	 * Liefert eine Verteilungsfunktion vom gewählten Typ mit den angegebenen Kenngrößen.
	 * @param mean	Gewünschter Erwartungswert
	 * @param sd	Gewünschte Standardabweichung
	 * @return	Verteilungsfunktion vom für das Objekt eingestellten Typ mit den angegebenen Kenngrößen
	 * @see #fitDistribution
	 */
	private AbstractRealDistribution getDistribution(final double mean, final double sd) {
		switch (fitDistribution) {
		case LOG_NORMAL:
			return new LogNormalDistributionImpl(mean,sd);
		case GAMMA:
			return new WrapperGammaDistribution().getDistribution(mean,sd);
		default:
			return new LogNormalDistributionImpl(mean,sd);
		}
	}

	/**
	 * Berechnet bezogen auf eine Log-Normalverteilung aus Standardabweichung und Modalwert den Erwartungswert.
	 * @param mode	Modalwert der Verteilung
	 * @param mean	Startwert für die Erwartungswertschätzung
	 * @param sd	Standardabweichung der Verteilung
	 * @param max	Obere Grenze für den Suchbereich
	 * @return	Zu den anderen Kenngrößen passender Erwartungswert für die Verteilung
	 */
	private double estimateMean(final double mode, final double mean, final double sd, final int max) {
		if (fitDistribution==FitDistribution.GAMMA && mode<=0.0001) return mean;

		final UnivariateFunction f;

		switch (fitDistribution) {
		case LOG_NORMAL: f=x->Math.pow(x,4)/Math.sqrt(sd*sd+x*x)/(sd*sd+x*x)-mode; break;
		case GAMMA: f=x->(x*x-sd*sd)/x-mode; break;
		default: f=x->Math.pow(x,4)/Math.sqrt(sd*sd+x*x)/(sd*sd+x*x)-mode; break;
		}

		final BisectionSolver solver=new BisectionSolver();
		return solver.solve(100,f,0,10*max,mean);
	}

	/**
	 * Erstellt eine empirische Verteilung basierend auf einer Log-Normalverteilung mit vorgegebenen Kenngrößen.
	 * @param mean	Erwartungswert der Log-Normalverteilung
	 * @param sd	Standardabweichung der Log-Normalverteilung
	 * @param steps	Anzahl der Datenpunkte (beginnend ab 0) in der resultierenden empirischen Verteilung
	 * @return	Empirische Verteilung mit den vorgegebenen Kenngrößen
	 */
	private DataDistributionImpl fitDistribution(final double mean, final double sd, final int steps) {
		final DataDistributionImpl result=new DataDistributionImpl(steps,steps);
		final AbstractRealDistribution dist=getDistribution(mean,sd);
		for (int i=0;i<steps-1;i++) result.densityData[i]=dist.density(i);
		return result;
	}

	/**
	 * Liefert den Index des maximalen Wertes.
	 * Wenn der maximale Wert mehrfach auftritt, wird der höchste Index geliefert.
	 * @param data	Datenreihe bei der der Index des höchsten Wertes ermittelt werden soll
	 * @return	Index des höchsten Wertes in der Datenreihe
	 */
	private int argMax(final double[] data) {
		int maxIndex=-1;
		double maxValue=-Double.MAX_VALUE;
		for (int i=0;i<data.length;i++) if (data[i]>=maxValue) {
			maxIndex=i;
			maxValue=data[i];
		}
		return maxIndex;
	}

	/**
	 * Extrahiert eine einzelne Verteilung aus der Gesamtverteilung.
	 * @param dist	Gesamtverteilung
	 * @return	Neues Residuum
	 */
	private DataDistributionImpl processStep(DataDistributionImpl dist) {
		outputPlain.append(String.format(step,fitMean.size()+1)+":\n");
		outputHTML.append("<h3>"+String.format(step,fitMean.size()+1)+":</h3>");

		/* Vorverarbeitung */
		dist=dist.clone();
		if (dist.densityData[0]==0) dist.densityData[0]=0.0001;
		for (int i=1;i<dist.densityData.length;i++) if (dist.densityData[i]<0) dist.densityData[i]=0;

		/* Kenngrößen der Gesamtverteilung ermitteln */
		final double sum=dist.getSum();
		final double mean=dist.getMean();
		final double sd=dist.getStandardDeviation();
		final double[] modes=dist.getMode();
		if (modes.length==0) {
			outputPlain.append("  "+noMode+"\n");
			outputHTML.append(noMode+"<br>");
			return null;
		}
		final int mode=(int)Math.round(modes[0]);
		outputPlain.append("  "+DistributionFitterMultiModal.mode+": "+mode+"\n");
		outputHTML.append(DistributionFitterMultiModal.mode+": "+mode+"<br>");

		/* Wie hoch darf der Anteil des aktuellen Anteils an der Gesamtverteilung sein? */
		final double maxFraction=(fitMean.size()==0)?0.95:1.1;

		/* Standardabweichung schrittweise verringern und immer wieder Anpassungen erstellen */
		int step=1;
		double meanEst=mean;
		double useSd=sd;
		double fraction=0;
		DataDistributionImpl partialDist;
		while (true) {
			try {
				meanEst=estimateMean(mode,mean,useSd,dist.densityData.length);
			} catch(Exception e) {
				return null;
			}
			if (meanEst<0.001) meanEst=0.001;
			partialDist=fitDistribution(meanEst,useSd,dist.densityData.length);
			final double pdfEstAtMode=partialDist.densityData[mode];
			fraction=(pdfEstAtMode<=0)?1:(1/pdfEstAtMode*dist.densityData[mode]);
			fraction=fraction/sum;
			if (fraction<maxFraction || step>=10) break;
			useSd*=0.8;
			step++;
		}

		/* Daten speichern */
		fitMode.add(mode);
		fitMean.add(meanEst);
		fitSd.add(useSd);
		double fractionAll=1;
		for (int i=0;i<fitFraction.size();i++) fractionAll-=fitFraction.get(i);
		fractionAll=fractionAll*Math.min(1,fraction);
		fitFraction.add(fractionAll);
		outputPlain.append("  "+approximation+": "+DistributionFitterBase.Mean+"="+NumberTools.formatNumber(meanEst)+", "+DistributionFitterBase.StdDev+"="+NumberTools.formatNumber(useSd)+"\n");
		outputPlain.append("  "+fraction+": "+NumberTools.formatPercent(fractionAll)+"\n");
		outputHTML.append(approximation+": "+DistributionFitterBase.Mean+"=<b>"+NumberTools.formatNumber(meanEst)+"</b>, "+DistributionFitterBase.StdDev+"=<b>"+NumberTools.formatNumber(useSd)+"</b><br>");
		outputHTML.append(DistributionFitterMultiModal.fraction+": "+NumberTools.formatPercent(fractionAll)+"<br>");

		/* Anteilsverteilung skalieren */
		for (int i=0;i<partialDist.densityData.length;i++) {
			partialDist.densityData[i]=partialDist.densityData[i]*maxFraction*sum;
		}

		/* Residuum bestimmen */
		final DataDistributionImpl residuum=new DataDistributionImpl(dist.densityData.length,dist.densityData.length);
		for (int i=0;i<residuum.densityData.length;i++) {
			residuum.densityData[i]=dist.densityData[i]-partialDist.densityData[i];
		}

		/* Randeffekte korrigieren */
		int firstIndex=0;
		while (argMax(residuum.densityData)==firstIndex) {
			residuum.densityData[firstIndex]=0;
			firstIndex++;
		}

		return residuum;
	}

	@Override
	protected boolean process(final DataDistributionImpl dist) {
		/* Info zu Verteilungstyp ausgeben */
		final String name;
		switch (fitDistribution) {
		case LOG_NORMAL: name=new WrapperLogNormalDistribution().getName(); break;
		case GAMMA: name=new WrapperGammaDistribution().getName(); break;
		default: name=new WrapperLogNormalDistribution().getName(); break;
		}
		outputPlain.append(usedDistribution+": "+name);
		outputHTML.append("<p>"+usedDistribution+": <b>"+name+"</b></p>");

		/* Maximal 4 Verteilungen extrahieren oder Ende, wenn 99% der Daten erklärt werden können. */
		DataDistributionImpl residuum=dist;
		for (int step=0;step<4;step++) {
			residuum=processStep(residuum);
			if (residuum==null) break;
			final double fractionSum=fitFraction.stream().mapToDouble(Double::doubleValue).sum();
			if (fractionSum>0.99) break;
		}

		/* Anteile nachoptimieren */
		optimizeFractions(dist);

		/* Berechnungsfunktion ausgeben */
		if (showCalculationCommand) {
			final StringBuilder command=new StringBuilder();
			command.append("RandomValues(");
			for (int i=0;i<fitMean.size();i++) {
				if (i>0) command.append(";");
				command.append(NumberTools.formatNumber(fitFraction.get(i),2));
				command.append(";");
				switch (fitDistribution) {
				case LOG_NORMAL: command.append("LogNormalDist"); break;
				case GAMMA: command.append("GammaDistDirect"); break;
				}
				command.append("(");
				command.append(NumberTools.formatNumber(fitMean.get(i)));
				command.append(";");
				command.append(NumberTools.formatNumber(fitSd.get(i)));
				command.append(")");
			}
			command.append(")");
			outputPlain.append("\n");
			outputPlain.append(calculationCommand+":\n");
			outputPlain.append("  "+command.toString());
			outputHTML.append("<h3>"+calculationCommand+"</h3>");
			outputHTML.append("<tt>"+command.toString()+"</tt>");
		}

		return true;
	}

	/**
	 * Ändert einen Anteil und die anderen, um die Summe konstant zu halten.
	 * @param fractions	Bisherige Anteile
	 * @param changeIndex	Index, der geändert werden soll
	 * @param changeDirection	Richtung (1 oder -1) in die der Wert an dem Index verändert werden soll
	 * @return	Neue Anteile
	 * @see #optimizeFractions(DataDistributionImpl)
	 */
	private double[] changeFractions(final double[] fractions, final int changeIndex, final int changeDirection) {
		final double[] result=Arrays.copyOf(fractions,fractions.length);
		for (int i=0;i<fractions.length;i++) if (i==changeIndex) {
			result[i]+=changeDirection*0.01;
		} else {
			result[i]-=changeDirection*0.01/(fractions.length-1);
		}
		return result;
	}

	/**
	 * Nachoptimierung der Anteile.
	 * @param dist	Ausgangsverteilung (zum Abgleich)
	 */
	private void optimizeFractions(final DataDistributionImpl dist) {
		final String fractionsString=String.join(", ",fitFraction.stream().map(d->NumberTools.formatPercent(d)).toArray(String[]::new));
		outputPlain.append(fractionsPostOptimization+"\n");
		outputHTML.append("<h3>"+fractionsPostOptimization+"</h3>");
		outputPlain.append("  "+fractionsPostOptimizationCurrent+": "+fractionsString+".\n");
		outputHTML.append(fractionsPostOptimizationCurrent+": "+fractionsString+".<br>");

		/* Modalwerte der Teilverteilungen */
		final int[] modes=fitMode.stream().mapToInt(Integer::intValue).map(mode->mode>0?mode:1).toArray();

		/* Dichtewerte an den Modalwerten in der Gesamtverteilung */
		final double sum=dist.getSum();
		final double[] pdf=IntStream.of(modes).mapToDouble(mode->dist.densityData[mode]/sum).toArray();

		/* Dichtewerte der Teilverteilungen an den Modalwerten */
		final double[][] partialPdf=new double[fitMean.size()][];
		for (int i=0;i<partialPdf.length;i++) {
			partialPdf[i]=new double[fitMean.size()];
			final AbstractRealDistribution d=getDistribution(fitMean.get(i),fitSd.get(i));
			for (int j=0;j<partialPdf.length;j++) {
				partialPdf[i][j]=d.density(modes[j]);
			}
		}

		/* Bisherige Anteile */
		double[] fractions=fitFraction.stream().mapToDouble(Double::doubleValue).toArray();

		/* Abweichung vom Optimum */
		final ToDoubleFunction<double[]> f=frac->{
			for (int i=0;i<frac.length;i++) if (frac[i]<0 || frac[i]>1) return Double.MAX_VALUE;
			double delta=0;
			for (int i=0;i<frac.length;i++) {
				double y=0;
				for (int j=0;j<frac.length;j++) y+=partialPdf[j][i]*frac[j];
				if (pdf[i]>0) delta+=Math.abs(y-pdf[i])/pdf[i];
			}
			return delta;
		};

		/* Optimierung */
		int counter=0;
		while (counter<3) {
			counter++;
			for (int index=0;index<fractions.length;index++) {
				final double[] fractionsTest1=changeFractions(fractions,index,1);
				final double[] fractionsTest2=changeFractions(fractions,index,-1);
				final double current=f.applyAsDouble(fractions);
				final double test1=f.applyAsDouble(fractionsTest1);
				final double test2=f.applyAsDouble(fractionsTest2);
				if (test1<current || test2<current) {
					if (test1<test2) fractions=fractionsTest1; else fractions=fractionsTest2;
					counter=0;
				}
			}
		}

		/* Rundungsfehler wenn nötig korrigieren */
		final double fractionsSum=DoubleStream.of(fractions).sum();
		for (int i=0;i<fractions.length;i++) fractions[i]/=fractionsSum;

		/* Ergebnis speichern */
		fitFraction.clear();
		fitFraction.addAll(DoubleStream.of(fractions).mapToObj(Double::valueOf).collect(Collectors.toList()));
		final String newFractionsStringPlain=String.join(", ",fitFraction.stream().map(d->NumberTools.formatPercent(d)).toArray(String[]::new));
		final String newFractionsStringHTML=String.join("</b>, <b>",fitFraction.stream().map(d->NumberTools.formatPercent(d)).toArray(String[]::new));
		outputPlain.append("  "+fractionsPostOptimizationNew+": "+newFractionsStringPlain+".\n");
		outputHTML.append(fractionsPostOptimizationNew+": <b>"+newFractionsStringHTML+"</b>.<br>");
	}

	/**
	 * Liefert die Liste der Modalwerte der Teilverteilungen.
	 * @return	Modalwerte der Teilverteilungen
	 */
	public List<Integer> getFitMode() {
		return fitMode;
	}

	/**
	 * Liefert die Liste der Erwartungswerte der Teilverteilungen.
	 * @return	Erwartungswerte der Teilverteilungen
	 */
	public List<Double> getFitMean() {
		return fitMean;
	}

	/**
	 * Liefert die Liste der Standardabweichungen der Teilverteilungen.
	 * @return	Standardabweichungen der Teilverteilungen
	 */
	public List<Double> getFitSd() {
		return fitSd;
	}

	/**
	 * Liefert die Liste der Anteile der Teilverteilungen an der Gesamtverteilung.
	 * @return	Anteile der Teilverteilungen an der Gesamtverteilung
	 */
	public List<Double> getFitFraction() {
		return fitFraction;
	}

	/**
	 * Generiert eine Beispielverteilung für den Verteilungsfitter.
	 * @return	Beispielverteilung aus zwei Log-Normalverteilungen
	 */
	public static DataDistributionImpl buildExample() {
		final DataDistributionImpl data=new DataDistributionImpl(5000,5000);

		final double mean1=50;
		final double sd1=100;
		final int n1=1_000_000;

		final double mean2=150;
		final double sd2=30;
		final int n2=Math.round(n1/3);

		AbstractRealDistribution dist;

		dist=new LogNormalDistributionImpl(mean1,sd1);
		for (int i=0;i<n1;i++) {
			final int index=(int)Math.round(DistributionRandomNumber.random(dist));
			data.densityData[Math.max(0,Math.min(data.densityData.length-1,index))]++;
		}

		dist=new LogNormalDistributionImpl(mean2,sd2);
		for (int i=0;i<n2;i++) {
			final int index=(int)Math.round(DistributionRandomNumber.random(dist));
			data.densityData[Math.max(0,Math.min(data.densityData.length-1,index))]++;
		}

		return data;
	}

	/**
	 * Liefert die angepassten Teilverteilungen
	 * @return	Teilverteilungen
	 */
	public AbstractRealDistribution[] getDistributions() {
		final List<AbstractRealDistribution> dists=new ArrayList<>();

		for (int i=0;i<fitMean.size();i++) {
			dists.add(getDistribution(fitMean.get(i),fitSd.get(i)));
		}

		return dists.toArray(new AbstractRealDistribution[0]);
	}
}
