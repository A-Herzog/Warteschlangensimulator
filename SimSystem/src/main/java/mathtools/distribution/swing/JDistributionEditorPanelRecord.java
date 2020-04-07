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
package mathtools.distribution.swing;

import java.awt.Color;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.ChiDistributionImpl;
import mathtools.distribution.DataDistributionImpl;
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
import mathtools.distribution.OnePointDistributionImpl;
import mathtools.distribution.ParetoDistributionImpl;
import mathtools.distribution.PowerDistributionImpl;
import mathtools.distribution.RayleighDistributionImpl;
import mathtools.distribution.TriangularDistributionImpl;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.WrapperBetaDistribution;
import mathtools.distribution.tools.WrapperCauchyDistribution;
import mathtools.distribution.tools.WrapperChiDistribution;
import mathtools.distribution.tools.WrapperChiSquaredDistribution;
import mathtools.distribution.tools.WrapperDataDistribution;
import mathtools.distribution.tools.WrapperErlangDistribution;
import mathtools.distribution.tools.WrapperExponentialDistribution;
import mathtools.distribution.tools.WrapperFDistribution;
import mathtools.distribution.tools.WrapperFatigueLifeDistribution;
import mathtools.distribution.tools.WrapperFrechetDistribution;
import mathtools.distribution.tools.WrapperGammaDistribution;
import mathtools.distribution.tools.WrapperGumbelDistribution;
import mathtools.distribution.tools.WrapperHyperbolicSecantDistribution;
import mathtools.distribution.tools.WrapperInverseGaussianDistribution;
import mathtools.distribution.tools.WrapperJohnsonDistribution;
import mathtools.distribution.tools.WrapperLaplaceDistribution;
import mathtools.distribution.tools.WrapperLogLogisticDistribution;
import mathtools.distribution.tools.WrapperLogNormalDistribution;
import mathtools.distribution.tools.WrapperLogisticDistribution;
import mathtools.distribution.tools.WrapperNormalDistribution;
import mathtools.distribution.tools.WrapperOnePointDistribution;
import mathtools.distribution.tools.WrapperParetoDistribution;
import mathtools.distribution.tools.WrapperPowerDistribution;
import mathtools.distribution.tools.WrapperRayleighDistribution;
import mathtools.distribution.tools.WrapperTriangularDistribution;
import mathtools.distribution.tools.WrapperUniformRealDistribution;
import mathtools.distribution.tools.WrapperWeibullDistribution;

/**
 * Diese Klasse hält Datensätze für die Anzeige von Bearbeitung von
 * Verteilungen in einem {@link JDistributionEditorPanel} vor.
 * @author Alexander Herzog
 * @see JDistributionEditorPanelRecord#getList()
 */
public abstract class JDistributionEditorPanelRecord {
	private final AbstractDistributionWrapper wrapper;
	private final String[] editLabels;

	/**
	 * Konstruktor eines Datensatzes
	 * @param wrapper	Wrapper-Klasser der Verteilung auf die sich dieser Datensatz beziehen soll
	 * @param editLabels	Namen der Eingabefelder
	 */
	private JDistributionEditorPanelRecord(final AbstractDistributionWrapper wrapper, final String[] editLabels) {
		this.wrapper=wrapper;
		this.editLabels=editLabels;
	}

	/**
	 * Ist dies die "Empirische Daten"-Verteilung?<br>
	 * Wenn ja werden die kleinen "mehr"/"weniger"-Pfeile neben dem Eingabefeld nicht angezeigt.
	 * Dafür werden dann aber Laden/Speichern/Kopieren/Einfügen-Schaltflächen angezeigt.
	 * @return	"Empirische Daten"-Verteilung?
	 */
	public boolean isDataDistribution() {
		return wrapper.isForDistribution(new DataDistributionImpl(10,10));
	}

	/**
	 * Liefert den Namen der Verteilung.
	 * @return	Name der Verteilung
	 */
	public String getName() {
		return wrapper.getName();
	}

	/**
	 * Liefert die Beschriftungen der Eingabefelder
	 * @return	Beschriftungen der Eingabefelder
	 */
	public String[] getEditLabels() {
		return editLabels;
	}

	/**
	 * Liefert die Eingabefelder inkl. Vorgabebelegung
	 * @param meanD	Erwartungswert (als Zahl)
	 * @param mean	Erwartungswert (als Zeichenkette)
	 * @param stdD	Standardabweichung (als Zahl)
	 * @param std	Standardabweichung (als Zeichenkette)
	 * @param lower	Untere Schranke
	 * @param upper	Obere Schranke
	 * @param maxXValue	Maximaler X-Wert des betrachteten Bereichs
	 * @return	Eingabefelder inkl. Vorgabebelegung
	 */
	public abstract String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue);

	/**
	 * Belegung der Eingabefelder auf Basis der Verteilung
	 * @param distribution	Verteilung (vom passenden Typ) aus der die Daten ausgelesen werden sollen
	 * @return	Belegung der Eingabefelder
	 */
	public abstract String[] getValues(final AbstractRealDistribution distribution);

	/**
	 * Stellt die Eingabefelder (evtl.) auf Basis von Erwartungswert von Standardabweichung (die von einer anderen Verteilung übernommen wurden) ein
	 * @param fields	Eingabefelder
	 * @param mean	Erwartungswert
	 * @param sd	Standardabweichung
	 */
	public void setValues(final JTextField[] fields, final double mean, final double sd) {
		final AbstractRealDistribution distribution=wrapper.getDistribution(mean,sd);
		if (distribution!=null) {
			final String[] text=getValues(distribution);
			if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
		}
	}

	/**
	 * Liefert die Verteilung auf Basis der Texten in den Eingabefeldern
	 * @param fields	Eingabefelder
	 * @param maxXValue	Maximaler X-Wert des betrachteten Bereichs
	 * @return	Neue Verteilung oder <code>null</code>, wenn keine Verteilung erstellt werden konnte.
	 */
	public abstract AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue);

	/**
	 * Liefert eine Liste aller Verteilungsdatensätze
	 * @return	Liste aller Verteilungsdatensätze
	 */
	public static List<JDistributionEditorPanelRecord> getList() {
		final List<JDistributionEditorPanelRecord> list=new ArrayList<>();
		list.add(new DataDistribution());
		list.add(new OnePointDistribution());
		list.add(new UniformDistribution());
		list.add(new ExpDistribution());
		list.add(new NormalDistribution());
		list.add(new LogNormalDistribution());
		list.add(new ErlangDistribution());
		list.add(new GammaDistribution());
		list.add(new BetaDistribution());
		list.add(new CauchyDistribution());
		list.add(new WeibullDistribution());
		list.add(new ChiSquaredDistribution());
		list.add(new ChiDistribution());
		list.add(new FDistribution());
		list.add(new JohnsonDistribution());
		list.add(new TriangularDistribution());
		list.add(new LaplaceDistribution());
		list.add(new ParetoDistribution());
		list.add(new LogisticDistribution());
		list.add(new InverseGaussianDistribution());
		list.add(new RayleighDistribution());
		list.add(new LogLogisticDistribution());
		list.add(new PowerDistribution());
		list.add(new GumbelDistribution());
		list.add(new FatigueLifeDistribution());
		list.add(new FrechetDistribution());
		list.add(new HyperbolicSecantDistribution());
		return list;
	}

	private abstract static class JDistributionEditorPanelRecordMean extends JDistributionEditorPanelRecord {
		public JDistributionEditorPanelRecordMean(final AbstractDistributionWrapper wrapper) {
			super(wrapper,new String[]{JDistributionEditorPanel.DistMean});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{mean};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(distribution.getNumericalMean())
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			fields[0].setText(NumberTools.formatNumberMax(mean));
		}
	}

	private abstract static class JDistributionEditorPanelRecordMeanStd extends JDistributionEditorPanelRecord {
		public JDistributionEditorPanelRecordMeanStd(final AbstractDistributionWrapper wrapper) {
			super(wrapper,new String[]{JDistributionEditorPanel.DistMean,JDistributionEditorPanel.DistStdDev});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{mean,std};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(distribution.getNumericalMean()),
					NumberTools.formatNumberMax(Math.sqrt(distribution.getNumericalVariance()))
			};
		}

		@Override
		public void setValues(final JTextField[] fields, double mean, double sd) {
			if (Double.isNaN(mean)) mean=100;
			if (Double.isNaN(sd)) sd=Math.abs(mean/2);
			fields[0].setText(NumberTools.formatNumberMax(mean));
			fields[1].setText(NumberTools.formatNumberMax(sd));
		}
	}

	/** Empirische Daten */
	private static class DataDistribution extends JDistributionEditorPanelRecord {
		public DataDistribution() {
			super(new WrapperDataDistribution(),new String[]{JDistributionEditorPanel.DistData});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{"1;2;3"};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					((DataDistributionImpl)distribution).getDensityString()
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final DataDistributionImpl distribution=DataDistributionImpl.createFromString(fields[0].getText(),(int)maxXValue);
			if (distribution==null) {fields[0].setBackground(Color.red); return null;}
			fields[0].setBackground(SystemColor.text);
			return distribution;
		}
	}

	/** Ein-Punkt-Verteilung */
	private static class OnePointDistribution extends JDistributionEditorPanelRecordMean {
		public OnePointDistribution() {
			super(new WrapperOnePointDistribution());
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d=NumberTools.getNotNegativeDouble(fields[0],true); if (d==null) return null;
			return new OnePointDistributionImpl(d);
		}
	}

	/** Gleichverteilung */
	private static class UniformDistribution extends JDistributionEditorPanelRecord {
		public UniformDistribution() {
			super(new WrapperUniformRealDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{lower,upper};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((UniformRealDistribution)distribution).getSupportLowerBound()),
					NumberTools.formatNumberMax(((UniformRealDistribution)distribution).getSupportUpperBound())
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null) return null;
			if (d1>d2) return null;
			return new UniformRealDistribution(d1,d2);
		}
	}

	/** Exponentialverteilung */
	private static class ExpDistribution extends JDistributionEditorPanelRecordMean {
		public ExpDistribution() {
			super(new WrapperExponentialDistribution());
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d=NumberTools.getPositiveDouble(fields[0],true); if (d==null) return null;
			return new ExponentialDistribution(null,d,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		}
	}

	/** Normalverteilung */
	private static class NormalDistribution extends JDistributionEditorPanelRecordMeanStd {
		public NormalDistribution() {
			super(new WrapperNormalDistribution());
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new org.apache.commons.math3.distribution.NormalDistribution(d1,d2);
		}
	}

	/** Log-Normalverteilung */
	private static class LogNormalDistribution extends JDistributionEditorPanelRecordMeanStd {
		public LogNormalDistribution() {
			super(new WrapperLogNormalDistribution());
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new LogNormalDistributionImpl(d1,d2);
		}
	}

	/** Erlang-Verteilung */
	private static class ErlangDistribution extends JDistributionEditorPanelRecord {
		public ErlangDistribution() {
			super(new WrapperErlangDistribution(),new String[]{"n","lambda"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, final double maxXValue) {
			return new String[]{"2",NumberTools.formatNumberMax(maxXValue/10)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((ErlangDistributionImpl)distribution).getShape()),
					NumberTools.formatNumberMax(((ErlangDistributionImpl)distribution).getScale())
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveIntDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new ErlangDistributionImpl(d1,d2);
		}
	}

	/** Gamma-Verteilung */
	private static class GammaDistribution extends JDistributionEditorPanelRecord {
		public GammaDistribution() {
			super(new WrapperGammaDistribution(),new String[]{JDistributionEditorPanel.DistMean,JDistributionEditorPanel.DistStdDev});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{mean,std};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			final double alpha=((org.apache.commons.math3.distribution.GammaDistribution)distribution).getShape();
			final double beta=((org.apache.commons.math3.distribution.GammaDistribution)distribution).getScale();
			/* E=a*b, V=a*b² */
			return new String[] {
					NumberTools.formatNumber(alpha*beta,5),
					NumberTools.formatNumber(Math.sqrt(alpha*beta*beta),5)
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			fields[0].setText(NumberTools.formatNumberMax(mean));
			fields[1].setText(NumberTools.formatNumberMax(sd));
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null || d1<=0) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null || d2<=0) return null;
			final double d3=d2*d2; /*  E=a*b, V=a*b² => a=E²/V, b=V/E */
			return new org.apache.commons.math3.distribution.GammaDistribution(d1*d1/Math.max(d3,0.000001),d3/Math.max(d1,0.000001));
		}
	}

	/** Beta-Verteilung */
	private static class BetaDistribution extends JDistributionEditorPanelRecord {
		public BetaDistribution() {
			super(new WrapperBetaDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd,"alpha","beta"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"0",NumberTools.formatNumber(maxXValue),NumberTools.formatNumberMax(1.5),NumberTools.formatNumberMax(1.5)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((ExtBetaDistributionImpl)distribution).getSupportLowerBound()),
					NumberTools.formatNumberMax(((ExtBetaDistributionImpl)distribution).getSupportUpperBound()),
					NumberTools.formatNumberMax(((ExtBetaDistributionImpl)distribution).getAlpha()),
					NumberTools.formatNumberMax(((ExtBetaDistributionImpl)distribution).getBeta())
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getPositiveDouble(fields[2],true); if (d3==null) return null;
			final Double d4=NumberTools.getPositiveDouble(fields[3],true); if (d4==null) return null;
			return new ExtBetaDistributionImpl(d1,d2,d3,d4);
		}
	}

	/** Cauchy-Verteilung */
	private static class CauchyDistribution extends JDistributionEditorPanelRecord {
		public CauchyDistribution() {
			super(new WrapperCauchyDistribution(),new String[]{JDistributionEditorPanel.DistMean,DistributionTools.DistScale});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{mean,NumberTools.formatNumberMax(maxXValue/10)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((org.apache.commons.math3.distribution.CauchyDistribution)distribution).getMedian()),
					NumberTools.formatNumberMax(((org.apache.commons.math3.distribution.CauchyDistribution)distribution).getScale())
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			fields[0].setText(NumberTools.formatNumberMax(mean));
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new org.apache.commons.math3.distribution.CauchyDistribution(d1,d2);
		}
	}

	/** Weilbull-Verteilung */
	private static class WeibullDistribution extends JDistributionEditorPanelRecord {
		public WeibullDistribution() {
			super(new WrapperWeibullDistribution(),new String[]{DistributionTools.DistScale,"Form"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(10/maxXValue),"2"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(1/((org.apache.commons.math3.distribution.WeibullDistribution)distribution).getScale()),
					NumberTools.formatNumberMax(((org.apache.commons.math3.distribution.WeibullDistribution)distribution).getShape())
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new org.apache.commons.math3.distribution.WeibullDistribution(d2,1/d1);
		}
	}

	/** Chi²-Verteilung */
	private static class ChiSquaredDistribution extends JDistributionEditorPanelRecord {
		public ChiSquaredDistribution() {
			super(new WrapperChiSquaredDistribution(),new String[]{JDistributionEditorPanel.DistDegreesOfFreedom});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(Math.ceil(maxXValue/20))};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((org.apache.commons.math3.distribution.ChiSquaredDistribution)distribution).getDegreesOfFreedom())
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveIntDouble(fields[0],true); if (d1==null) return null;
			return new org.apache.commons.math3.distribution.ChiSquaredDistribution(d1);
		}
	}

	/** Chi-Verteilung */
	private static class ChiDistribution extends JDistributionEditorPanelRecord {
		public ChiDistribution() {
			super(new WrapperChiDistribution(),new String[]{JDistributionEditorPanel.DistDegreesOfFreedom});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"20"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((ChiDistributionImpl)distribution).degreesOfFreedom)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveIntDouble(fields[0],true); if (d1==null) return null;
			return new ChiDistributionImpl((int)Math.round(d1));
		}
	}

	/** F-Verteilung */
	private static class FDistribution extends JDistributionEditorPanelRecord {
		public FDistribution() {
			super(new WrapperFDistribution(),new String[]{JDistributionEditorPanel.DistDegreesOfFreedomNumerator,JDistributionEditorPanel.DistDegreesOfFreedomDenominator});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(Math.ceil(maxXValue/20)),"5"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((org.apache.commons.math3.distribution.FDistribution)distribution).getNumeratorDegreesOfFreedom()),
					NumberTools.formatNumberMax(((org.apache.commons.math3.distribution.FDistribution)distribution).getDenominatorDegreesOfFreedom())
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveIntDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveIntDouble(fields[1],true); if (d2==null) return null;
			return new org.apache.commons.math3.distribution.FDistribution(d1,d2);
		}
	}

	/** Johnson-Verteilung */
	private static class JohnsonDistribution extends JDistributionEditorPanelRecord {
		public JohnsonDistribution() {
			super(new WrapperJohnsonDistribution(),new String[]{"gamma","xi","delta","lambda"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"2",NumberTools.formatNumberMax(maxXValue/2),"1",NumberTools.formatNumberMax(maxXValue/20)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((JohnsonDistributionImpl)distribution).gamma),
					NumberTools.formatNumberMax(((JohnsonDistributionImpl)distribution).xi),
					NumberTools.formatNumberMax(((JohnsonDistributionImpl)distribution).delta),
					NumberTools.formatNumberMax(((JohnsonDistributionImpl)distribution).lambda),
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveIntDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveIntDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getPositiveIntDouble(fields[2],true); if (d3==null) return null;
			final Double d4=NumberTools.getPositiveIntDouble(fields[3],true); if (d4==null) return null;
			return new JohnsonDistributionImpl(d1,d2,d3,d4);
		}
	}

	/** Dreiecksverteilung */
	private static class TriangularDistribution extends JDistributionEditorPanelRecord {
		public TriangularDistribution() {
			super(new WrapperTriangularDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistMostLikely,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{lower,NumberTools.formatNumberMax(maxXValue/2),upper};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((TriangularDistributionImpl)distribution).lowerBound),
					NumberTools.formatNumberMax(((TriangularDistributionImpl)distribution).mostLikelyX),
					NumberTools.formatNumberMax(((TriangularDistributionImpl)distribution).upperBound)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveIntDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveIntDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getPositiveIntDouble(fields[2],true); if (d3==null) return null;
			return new TriangularDistributionImpl(d1,d2,d3);
		}
	}

	/** Laplace-Verteilung */
	private static class LaplaceDistribution extends JDistributionEditorPanelRecord {
		public LaplaceDistribution() {
			super(new WrapperLaplaceDistribution(),new String[]{JDistributionEditorPanel.DistMean,DistributionTools.DistScale});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(maxXValue/5),NumberTools.formatNumberMax(maxXValue/10)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((LaplaceDistributionImpl)distribution).mu),
					NumberTools.formatNumberMax(((LaplaceDistributionImpl)distribution).b)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new LaplaceDistributionImpl(d1,d2);
		}
	}

	/** Pareto-Verteilung */
	private static class ParetoDistribution extends JDistributionEditorPanelRecord {
		public ParetoDistribution() {
			super(new WrapperParetoDistribution(),new String[]{DistributionTools.DistScale,"Form"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(maxXValue/50),"3"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((ParetoDistributionImpl)distribution).xmin),
					NumberTools.formatNumberMax(((ParetoDistributionImpl)distribution).alpha)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new ParetoDistributionImpl(d1,d2);

		}
	}

	/** Logistische Verteilung */
	private static class LogisticDistribution extends JDistributionEditorPanelRecord {
		public LogisticDistribution() {
			super(new WrapperLogisticDistribution(),new String[]{JDistributionEditorPanel.DistMean,DistributionTools.DistScale});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(maxXValue/3),NumberTools.formatNumberMax(maxXValue/10)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((LogisticDistributionImpl)distribution).mu),
					NumberTools.formatNumberMax(((LogisticDistributionImpl)distribution).s)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new LogisticDistributionImpl(d1,d2);
		}
	}

	/** Inverse Gauß-Verteilung */
	private static class InverseGaussianDistribution extends JDistributionEditorPanelRecord {
		public InverseGaussianDistribution() {
			super(new WrapperInverseGaussianDistribution(),new String[]{"lambda","mu"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(maxXValue/4),NumberTools.formatNumberMax(maxXValue/2)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((InverseGaussianDistributionImpl)distribution).lambda),
					NumberTools.formatNumberMax(((InverseGaussianDistributionImpl)distribution).mu)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new InverseGaussianDistributionImpl(d1,d2);
		}
	}

	/** Rayleigh-Verteilung */
	private static class RayleighDistribution extends JDistributionEditorPanelRecordMean {
		public RayleighDistribution() {
			super(new WrapperRayleighDistribution());
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			return new RayleighDistributionImpl(d1);
		}
	}

	/** Log-Logistische Verteilung */
	private static class LogLogisticDistribution extends JDistributionEditorPanelRecord {
		public LogLogisticDistribution() {
			super(new WrapperLogLogisticDistribution(),new String[]{"alpha","beta"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(500),NumberTools.formatNumberMax(3)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((LogLogisticDistributionImpl)distribution).alpha),
					NumberTools.formatNumberMax(((LogLogisticDistributionImpl)distribution).beta)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new LogLogisticDistributionImpl(d1,d2);
		}
	}

	/** Potenzverteilung */
	private static class PowerDistribution extends JDistributionEditorPanelRecord {
		public PowerDistribution() {
			super(new WrapperPowerDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd,"c"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{lower,upper,"5"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((PowerDistributionImpl)distribution).a),
					NumberTools.formatNumberMax(((PowerDistributionImpl)distribution).b),
					NumberTools.formatNumberMax(((PowerDistributionImpl)distribution).c)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null || d2.doubleValue()<=d1.doubleValue()) return null;
			final Double d3=NumberTools.getPositiveDouble(fields[2],true); if (d3==null) return null;
			return new PowerDistributionImpl(d1,d2,d3);
		}
	}

	/** Gumbel-Verteilung */
	private static class GumbelDistribution extends JDistributionEditorPanelRecord {
		public GumbelDistribution() {
			super(new WrapperGumbelDistribution(),new String[]{JDistributionEditorPanel.DistMean,JDistributionEditorPanel.DistStdDev});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{mean,std};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((org.apache.commons.math3.distribution.GumbelDistribution)distribution).getNumericalMean()),
					NumberTools.formatNumberMax(Math.sqrt(((org.apache.commons.math3.distribution.GumbelDistribution)distribution).getNumericalVariance()))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new WrapperGumbelDistribution().getDistribution(d1,d2);
		}
	}

	/** Fatigue-Life-Verteilung */
	private static class FatigueLifeDistribution extends JDistributionEditorPanelRecord {
		public FatigueLifeDistribution() {
			super(new WrapperFatigueLifeDistribution(),new String[]{DistributionTools.DistLocation+" (mu)",DistributionTools.DistScale+" (beta)","Form (gamma)"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"0",NumberTools.formatNumber(maxXValue/10),"1"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((FatigueLifeDistributionImpl)distribution).mu),
					NumberTools.formatNumberMax(((FatigueLifeDistributionImpl)distribution).beta),
					NumberTools.formatNumberMax(((FatigueLifeDistributionImpl)distribution).gamma)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getPositiveDouble(fields[2],true); if (d3==null) return null;
			return new FatigueLifeDistributionImpl(d1,d2,d3);
		}
	}

	/** Frechet-Verteilung */
	private static class FrechetDistribution extends JDistributionEditorPanelRecord {
		public FrechetDistribution() {
			super(new WrapperFrechetDistribution(),new String[]{DistributionTools.DistLocation+" (delta)",DistributionTools.DistScale+" (beta)","Form (alpha)"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"0",NumberTools.formatNumber(maxXValue/10),"1"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((FrechetDistributionImpl)distribution).delta),
					NumberTools.formatNumberMax(((FrechetDistributionImpl)distribution).beta),
					NumberTools.formatNumberMax(((FrechetDistributionImpl)distribution).alpha)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getPositiveDouble(fields[2],true); if (d3==null) return null;
			return new FrechetDistributionImpl(d1,d2,d3);
		}
	}

	/** Hyperbolische Sekanten-Verteilung */
	private static class HyperbolicSecantDistribution extends JDistributionEditorPanelRecordMeanStd {
		public HyperbolicSecantDistribution() {
			super(new WrapperHyperbolicSecantDistribution());
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new HyperbolicSecantDistributionImpl(d1,d2);
		}
	}
}