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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.ArcsineDistribution;
import mathtools.distribution.ChiDistributionImpl;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.DiscreteBinomialDistributionImpl;
import mathtools.distribution.DiscreteBorelDistributionImpl;
import mathtools.distribution.DiscreteGeometricDistributionImpl;
import mathtools.distribution.DiscreteHyperGeomDistributionImpl;
import mathtools.distribution.DiscreteLogarithmicDistributionImpl;
import mathtools.distribution.DiscreteNegativeBinomialDistributionImpl;
import mathtools.distribution.DiscreteNegativeHyperGeomDistributionImpl;
import mathtools.distribution.DiscretePoissonDistributionImpl;
import mathtools.distribution.DiscreteUniformDistributionImpl;
import mathtools.distribution.DiscreteZetaDistributionImpl;
import mathtools.distribution.ErlangDistributionImpl;
import mathtools.distribution.ExtBetaDistributionImpl;
import mathtools.distribution.FatigueLifeDistributionImpl;
import mathtools.distribution.FrechetDistributionImpl;
import mathtools.distribution.HalfNormalDistribution;
import mathtools.distribution.HyperbolicSecantDistributionImpl;
import mathtools.distribution.InverseGaussianDistributionImpl;
import mathtools.distribution.IrwinHallDistribution;
import mathtools.distribution.JohnsonDistributionImpl;
import mathtools.distribution.KumaraswamyDistribution;
import mathtools.distribution.LaplaceDistributionImpl;
import mathtools.distribution.LevyDistribution;
import mathtools.distribution.LogCauchyDistributionImpl;
import mathtools.distribution.LogLogisticDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.LogisticDistributionImpl;
import mathtools.distribution.MaxwellBoltzmannDistribution;
import mathtools.distribution.OnePointDistributionImpl;
import mathtools.distribution.ParetoDistributionImpl;
import mathtools.distribution.PertDistributionImpl;
import mathtools.distribution.PowerDistributionImpl;
import mathtools.distribution.RayleighDistributionImpl;
import mathtools.distribution.ReciprocalDistribution;
import mathtools.distribution.SawtoothLeftDistribution;
import mathtools.distribution.SawtoothRightDistribution;
import mathtools.distribution.SineDistribution;
import mathtools.distribution.StudentTDistributionImpl;
import mathtools.distribution.TrapezoidDistributionImpl;
import mathtools.distribution.TriangularDistributionImpl;
import mathtools.distribution.UQuadraticDistribution;
import mathtools.distribution.WignerHalfCircleDistributionImpl;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.WrapperArcsineDistribution;
import mathtools.distribution.tools.WrapperBetaDistribution;
import mathtools.distribution.tools.WrapperBinomialDistribution;
import mathtools.distribution.tools.WrapperBorelDistribution;
import mathtools.distribution.tools.WrapperCauchyDistribution;
import mathtools.distribution.tools.WrapperChiDistribution;
import mathtools.distribution.tools.WrapperChiSquaredDistribution;
import mathtools.distribution.tools.WrapperDataDistribution;
import mathtools.distribution.tools.WrapperDiscreteUniformDistribution;
import mathtools.distribution.tools.WrapperErlangDistribution;
import mathtools.distribution.tools.WrapperExponentialDistribution;
import mathtools.distribution.tools.WrapperFDistribution;
import mathtools.distribution.tools.WrapperFatigueLifeDistribution;
import mathtools.distribution.tools.WrapperFrechetDistribution;
import mathtools.distribution.tools.WrapperGammaDistribution;
import mathtools.distribution.tools.WrapperGeometricDistribution;
import mathtools.distribution.tools.WrapperGumbelDistribution;
import mathtools.distribution.tools.WrapperHalfNormalDistribution;
import mathtools.distribution.tools.WrapperHyperGeomDistribution;
import mathtools.distribution.tools.WrapperHyperbolicSecantDistribution;
import mathtools.distribution.tools.WrapperInverseGaussianDistribution;
import mathtools.distribution.tools.WrapperIrwinHallDistribution;
import mathtools.distribution.tools.WrapperJohnsonDistribution;
import mathtools.distribution.tools.WrapperKumaraswamyDistribution;
import mathtools.distribution.tools.WrapperLaplaceDistribution;
import mathtools.distribution.tools.WrapperLevyDistribution;
import mathtools.distribution.tools.WrapperLogCauchyDistribution;
import mathtools.distribution.tools.WrapperLogLogisticDistribution;
import mathtools.distribution.tools.WrapperLogNormalDistribution;
import mathtools.distribution.tools.WrapperLogarithmicDistribution;
import mathtools.distribution.tools.WrapperLogisticDistribution;
import mathtools.distribution.tools.WrapperMaxwellBoltzmannDistribution;
import mathtools.distribution.tools.WrapperNegativeBinomialDistribution;
import mathtools.distribution.tools.WrapperNegativeHyperGeomDistribution;
import mathtools.distribution.tools.WrapperNormalDistribution;
import mathtools.distribution.tools.WrapperOnePointDistribution;
import mathtools.distribution.tools.WrapperParetoDistribution;
import mathtools.distribution.tools.WrapperPertDistribution;
import mathtools.distribution.tools.WrapperPoissonDistribution;
import mathtools.distribution.tools.WrapperPowerDistribution;
import mathtools.distribution.tools.WrapperRayleighDistribution;
import mathtools.distribution.tools.WrapperReciprocalDistribution;
import mathtools.distribution.tools.WrapperSawtoothLeftDistribution;
import mathtools.distribution.tools.WrapperSawtoothRightDistribution;
import mathtools.distribution.tools.WrapperSineDistribution;
import mathtools.distribution.tools.WrapperStudentTDistribution;
import mathtools.distribution.tools.WrapperTrapezoidDistribution;
import mathtools.distribution.tools.WrapperTriangularDistribution;
import mathtools.distribution.tools.WrapperUQuadraticDistribution;
import mathtools.distribution.tools.WrapperUniformRealDistribution;
import mathtools.distribution.tools.WrapperWeibullDistribution;
import mathtools.distribution.tools.WrapperWignerHalfCircleDistribution;
import mathtools.distribution.tools.WrapperZetaDistribution;

/**
 * Diese Klasse hält Datensätze für die Anzeige von Bearbeitung von
 * Verteilungen in einem {@link JDistributionEditorPanel} vor.
 * @author Alexander Herzog
 * @see #getList(List, boolean, boolean)
 */
public abstract class JDistributionEditorPanelRecord {
	/**
	 * Wrapper-Klasse der Verteilung auf die sich dieser Datensatz beziehen soll
	 */
	protected final AbstractDistributionWrapper wrapper;

	/**
	 * Namen der Eingabefelder
	 */
	private final String[] editLabels;

	/**
	 * Soll der Datensatz hervorgehoben dargestellt werden?
	 * @see #copy(boolean)
	 */
	public boolean highlight;

	/**
	 * Konstruktor eines Datensatzes
	 * @param wrapper	Wrapper-Klasse der Verteilung auf die sich dieser Datensatz beziehen soll
	 * @param editLabels	Namen der Eingabefelder
	 */
	private JDistributionEditorPanelRecord(final AbstractDistributionWrapper wrapper, final String[] editLabels) {
		this.wrapper=wrapper;
		this.editLabels=editLabels;
	}

	/**
	 * Handelt es sich bei dem Eintrag um einen Trenner?
	 * @return	Trenner (=keine Verteilung; <code>true</code>) oder normaler Verteilungseintrag (<code>false</code>)
	 */
	public boolean isSeparator() {
		return wrapper==null;
	}

	/**
	 * Erstellt eine Kopie des Datensatzes.
	 * @param highlight	Soll der Datensatz hervorgehoben dargestellt werden?
	 * @return	Kopie des Datensatzes
	 */
	public JDistributionEditorPanelRecord copy(final boolean highlight) {
		try {
			@SuppressWarnings("unchecked")
			final Constructor<JDistributionEditorPanelRecord> constructor=(Constructor<JDistributionEditorPanelRecord>)getClass().getConstructor();
			final JDistributionEditorPanelRecord instance=constructor.newInstance();
			instance.highlight=highlight;
			return instance;
		} catch (NoSuchMethodException|SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
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
		if (wrapper==null) return JDistributionEditorPanel.SetupListDivier;
		return wrapper.getName();
	}

	/**
	 * Prüft ob dieser Datensatz für Verteilung des angegebenen Namens zuständig ist
	 * @param name	Name der Verteilung bei der die Zuständigkeit geprüft werden soll
	 * @return	Gibt <code>true</code> zurück, wenn dieser Datensatz sich für zuständig erachtet.
	 */
	public boolean isForDistribution(final String name) {
		return wrapper.isForDistribution(name);
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
	 * Vergleicht zwei Datensätze in Bezug auf ihre Namen
	 * @param record1	Datensatz 1 für Namensvergleich
	 * @param record2	Datensatz 2 für Namensvergleich
	 * @return	Entspricht dem String-Vergleich-Rückgabewert, wenn die Namen direkt verglichen würden
	 */
	public static int compare(final JDistributionEditorPanelRecord record1, final JDistributionEditorPanelRecord record2) {
		return record1.getName().compareTo(record2.getName());
	}

	/**
	 * Statische Liste mit allen Verteilungsdatensätzen
	 * @see #getList(List, boolean, boolean)
	 */
	private static final List<JDistributionEditorPanelRecord> allRecords;

	static {
		allRecords=new ArrayList<>();
		allRecords.add(new DataDistribution());
		allRecords.add(new OnePointDistribution());
		allRecords.add(new UniformDistribution());
		allRecords.add(new ExpDistribution());
		allRecords.add(new NormalDistribution());
		allRecords.add(new LogNormalDistribution());
		allRecords.add(new ErlangDistribution());
		allRecords.add(new GammaDistribution());
		allRecords.add(new BetaDistribution());
		allRecords.add(new CauchyDistribution());
		allRecords.add(new WeibullDistribution());
		allRecords.add(new ChiSquaredDistribution());
		allRecords.add(new ChiDistribution());
		allRecords.add(new FDistribution());
		allRecords.add(new JohnsonDistribution());
		allRecords.add(new TriangularDistribution());
		allRecords.add(new TrapezoidDistribution());
		allRecords.add(new PertDistribution());
		allRecords.add(new LaplaceDistribution());
		allRecords.add(new ParetoDistribution());
		allRecords.add(new LogisticDistribution());
		allRecords.add(new InverseGaussianDistribution());
		allRecords.add(new RayleighDistribution());
		allRecords.add(new LogLogisticDistribution());
		allRecords.add(new PowerDistribution());
		allRecords.add(new GumbelDistribution());
		allRecords.add(new FatigueLifeDistribution());
		allRecords.add(new FrechetDistribution());
		allRecords.add(new HyperbolicSecantDistribution());
		allRecords.add(new SawtoothLeftDistributionPanel());
		allRecords.add(new SawtoothRightDistributionPanel());
		allRecords.add(new LevyDistributionPanel());
		allRecords.add(new MaxwellBoltzmannDistributionPanel());
		allRecords.add(new StudentTDistributionPanel());
		allRecords.add(new LogCauchyDistributionPanel());
		allRecords.add(new HyperGeomDistributionPanel());
		allRecords.add(new BinomialDistributionPanel());
		allRecords.add(new PoissonDistributionPanel());
		allRecords.add(new NegativeBinomialDistributionPanel());
		allRecords.add(new NegativeHyperGeomDistributionPanel());
		allRecords.add(new ZetaDistributionPanel());
		allRecords.add(new DiscreteUniformDistributionPanel());
		allRecords.add(new GeometricDistributionPanel());
		allRecords.add(new LogarithmicDistributionPanel());
		allRecords.add(new HalfNormalDistributionPanel());
		allRecords.add(new UQuadraticDistributionPanel());
		allRecords.add(new ReciprocalDistributionPanel());
		allRecords.add(new KumaraswamyDistributionPanel());
		allRecords.add(new IrwinHallDistributionPanel());
		allRecords.add(new SineDistributionPanel());
		allRecords.add(new ArcsineDistributionPanel());
		allRecords.add(new WignerHalfCircleDistributionPanel());
		allRecords.add(new BorelDistributionPanel());
	}

	/**
	 * Liefert eine Liste aller Verteilungsdatensätze.
	 * @param highlight Namen und Reihenfolge der hervorgehoben darzustellenden Verteilungen (darf <code>null</code> oder leer sein)
	 * @param sortNonHighlightDistributions	Sollen die nicht hervorgehobenen Verteilungen alphabetisch sortiert dargestellt werden?
	 * @param addSeparator	Soll ein Trenner (für den Editor) zwischen den hervorgehobenen und den normalen Einträgen angezeigt werden?
	 * @return	Liste aller Verteilungsdatensätze
	 */
	public static List<JDistributionEditorPanelRecord> getList(final List<String> highlight, final boolean sortNonHighlightDistributions, final boolean addSeparator) {
		final List<JDistributionEditorPanelRecord> workList=new ArrayList<>();
		final List<JDistributionEditorPanelRecord> resultList=new ArrayList<>();

		workList.addAll(allRecords);

		/* Hervorzuhebende Verteilungen */
		if (highlight!=null) for (String highlightName: highlight) {
			int index=-1;
			for (int i=0;i<workList.size();i++) if (workList.get(i).isForDistribution(highlightName)) {
				index=i; break;
			}
			if (index>=0) {
				resultList.add(workList.remove(index).copy(true));
			}
		}

		/* Trenner */
		if (addSeparator) resultList.add(new SeparatorPanel());

		/* Nicht hervorzuhebende Verteilungen */
		if (sortNonHighlightDistributions) workList.sort(JDistributionEditorPanelRecord::compare);
		resultList.addAll(workList);

		return resultList;
	}

	/**
	 * Liefert die Namen (und die Reihenfolge) der standardmäßig hervorzuhebenden Verteilungsdatensätze.
	 * @return	Namen (und die Reihenfolge) der standardmäßig hervorzuhebenden Verteilungsdatensätze
	 */
	public static List<String> getDefaultHighlights() {
		final List<String> names=new ArrayList<>();

		names.add(new ExpDistribution().getName());
		names.add(new LogNormalDistribution().getName());
		names.add(new GammaDistribution().getName());
		names.add(new ErlangDistribution().getName());
		names.add(new TriangularDistribution().getName());
		names.add(new UniformDistribution().getName());
		names.add(new OnePointDistribution().getName());
		names.add(new DataDistribution().getName());

		return names;
	}

	/**
	 * Liefert den zu einer konkreten Verteilung passenden Datensatz
	 * @param distribution	Verteilung für die der Datensatz bestimmt werden soll
	 * @return	Datensatz oder <code>null</code>, wenn kein Datensatz zu der angegebenen Verteilung bestimmt werden konnte
	 */
	public static JDistributionEditorPanelRecord getRecord(final AbstractRealDistribution distribution) {
		for (JDistributionEditorPanelRecord record: allRecords) {
			if (record.wrapper.isForDistribution(distribution)) return record;
		}
		return null;
	}

	/**
	 * Diese Klasse hält Datensätze für die Anzeige von Bearbeitung von
	 * Verteilungen, die über einen einstellbaren Erwartungswert verfügen,
	 * in einem {@link JDistributionEditorPanel} vor.
	 * @see JDistributionEditorPanelRecord
	 */
	private abstract static class JDistributionEditorPanelRecordMean extends JDistributionEditorPanelRecord {
		/**
		 * Konstruktor der Klasse
		 * @param wrapper	Wrapper-Klasse der Verteilung auf die sich dieser Datensatz beziehen soll
		 */
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

	/**
	 * Diese Klasse hält Datensätze für die Anzeige von Bearbeitung von
	 * Verteilungen, die über einen einstellbaren Erwartungswert und eine
	 * einstellbare Standardabweichung verfügen,
	 * in einem {@link JDistributionEditorPanel} vor.
	 * @see JDistributionEditorPanelRecord
	 */
	private abstract static class JDistributionEditorPanelRecordMeanStd extends JDistributionEditorPanelRecord {
		/**
		 * Konstruktor der Klasse
		 * @param wrapper	Wrapper-Klasse der Verteilung auf die sich dieser Datensatz beziehen soll
		 */
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

	/** Trenner zwischen hervorgehobenen und normalen Verteilungen für den Filter-Editor (kommt in der normalen Liste nicht vor) */
	public static class SeparatorPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public SeparatorPanel() {
			super(null,new String[0]);
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[0];
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[0];
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			return null;
		}
	}

	/** Empirische Daten */
	private static class DataDistribution extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
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
			fields[0].setBackground(NumberTools.getTextFieldDefaultBackground());
			return distribution;
		}
	}

	/** Ein-Punkt-Verteilung */
	private static class OnePointDistribution extends JDistributionEditorPanelRecordMean {
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			UniformRealDistribution distribution=(UniformRealDistribution)wrapper.getDistribution(mean,sd);
			if (distribution!=null) {
				if (distribution.getSupportLowerBound()<0) distribution=new UniformRealDistribution(0,mean*2);
				final String[] text=getValues(distribution);
				if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
			}
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null) return null;
			if (d1>=d2) return null;
			return new UniformRealDistribution(d1,d2);
		}
	}

	/** Exponentialverteilung */
	private static class ExpDistribution extends JDistributionEditorPanelRecordMean {
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
					NumberTools.formatNumberMax(Math.max(0,((TriangularDistributionImpl)distribution).lowerBound)),
					NumberTools.formatNumberMax(Math.max(0,((TriangularDistributionImpl)distribution).mostLikelyX)),
					NumberTools.formatNumberMax(Math.max(0,((TriangularDistributionImpl)distribution).upperBound))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getNotNegativeDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getNotNegativeDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getNotNegativeDouble(fields[2],true); if (d3==null) return null;
			return new TriangularDistributionImpl(d1,d2,d3);
		}
	}

	/** Trapezverteilung */
	private static class TrapezoidDistribution extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public TrapezoidDistribution() {
			super(new WrapperTrapezoidDistribution(),new String[]{"a","b","c","d"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			Double lowerD=NumberTools.getDouble(lower);
			Double upperD=NumberTools.getDouble(upper);
			if (lowerD==null) lowerD=0.0;
			if (upperD==null) upperD=200.0;
			return new String[]{lower,NumberTools.formatNumberMax((3*lowerD+upperD)/4),NumberTools.formatNumberMax((lowerD+3*upperD)/4),upper};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(Math.max(0,((TrapezoidDistributionImpl)distribution).a)),
					NumberTools.formatNumberMax(Math.max(0,((TrapezoidDistributionImpl)distribution).b)),
					NumberTools.formatNumberMax(Math.max(0,((TrapezoidDistributionImpl)distribution).c)),
					NumberTools.formatNumberMax(Math.max(0,((TrapezoidDistributionImpl)distribution).d))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getNotNegativeDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getNotNegativeDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getNotNegativeDouble(fields[2],true); if (d3==null) return null;
			final Double d4=NumberTools.getNotNegativeDouble(fields[3],true); if (d4==null) return null;
			return new TrapezoidDistributionImpl(d1,d2,d3,d4);
		}
	}

	/** Pert-Verteilung */
	private static class PertDistribution extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public PertDistribution() {
			super(new WrapperPertDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistMostLikely,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{lower,NumberTools.formatNumberMax(maxXValue/2),upper};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(Math.max(0,((PertDistributionImpl)distribution).lowerBound)),
					NumberTools.formatNumberMax(Math.max(0,((PertDistributionImpl)distribution).mostLikely)),
					NumberTools.formatNumberMax(Math.max(0,((PertDistributionImpl)distribution).upperBound))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getNotNegativeDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getNotNegativeDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getNotNegativeDouble(fields[2],true); if (d3==null) return null;
			return new PertDistributionImpl(d1,d2,d3);
		}
	}

	/** Laplace-Verteilung */
	private static class LaplaceDistribution extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
					NumberTools.formatNumberMax(NumberTools.reduceDigits(Math.sqrt(((org.apache.commons.math3.distribution.GumbelDistribution)distribution).getNumericalVariance()),14))
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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
		/** Konstruktor der Klasse */
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

	/** Sägezahnverteilung (links) */
	private static class SawtoothLeftDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public SawtoothLeftDistributionPanel() {
			super(new WrapperSawtoothLeftDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{lower,upper};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(Math.max(0,((SawtoothLeftDistribution)distribution).a)),
					NumberTools.formatNumberMax(Math.max(0,((SawtoothLeftDistribution)distribution).b))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getNotNegativeDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getNotNegativeDouble(fields[1],true); if (d2==null) return null;
			return new SawtoothLeftDistribution(d1,d2);
		}
	}

	/** Sägezahnverteilung (rechts) */
	private static class SawtoothRightDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public SawtoothRightDistributionPanel() {
			super(new WrapperSawtoothRightDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{lower,upper};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(Math.max(0,((SawtoothRightDistribution)distribution).a)),
					NumberTools.formatNumberMax(Math.max(0,((SawtoothRightDistribution)distribution).b))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getNotNegativeDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getNotNegativeDouble(fields[1],true); if (d2==null) return null;
			return new SawtoothRightDistribution(d1,d2);
		}
	}

	/** Levy-Verteilung */
	private static class LevyDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public LevyDistributionPanel() {
			super(new WrapperLevyDistribution(),new String[]{DistributionTools.DistLocation+" (mu)",DistributionTools.DistScale+" (c)"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"0",mean};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(Math.max(0,((LevyDistribution)distribution).mu)),
					NumberTools.formatNumberMax(Math.max(0,((LevyDistribution)distribution).c))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double d1=NumberTools.getNotNegativeDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new LevyDistribution(d1,d2);
		}
	}

	/** Maxwell-Boltzmann-Verteilung */
	private static class MaxwellBoltzmannDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public MaxwellBoltzmannDistributionPanel() {
			super(new WrapperMaxwellBoltzmannDistribution(),new String[] {DistributionTools.DistParameter+" (a)"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			final double a=meanD/2*Math.sqrt(Math.PI/2);
			return new String[]{NumberTools.formatNumber(a)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(Math.max(0,((MaxwellBoltzmannDistribution)distribution).a))
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double a=NumberTools.getPositiveDouble(fields[0],true); if (a==null) return null;
			return new MaxwellBoltzmannDistribution(a);
		}
	}

	/** Studentsche t-Verteilung */
	private static class StudentTDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public StudentTDistributionPanel() {
			super(new WrapperStudentTDistribution(),new String[] {DistributionTools.DistMean+" (mu)",DistributionTools.DistDegreesOfFreedom+" (nu)"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			final double nu;
			if (stdD>1) {
				final double variance=stdD*stdD;
				nu=2*variance/(variance-1);
			} else {
				nu=5;
			}
			return new String[]{mean,NumberTools.formatNumberMax(nu)};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((StudentTDistributionImpl)distribution).mu),
					NumberTools.formatNumberMax(((StudentTDistributionImpl)distribution).nu)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double mu=NumberTools.getDouble(fields[0],true); if (mu==null) return null;
			final Double nu=NumberTools.getPositiveDouble(fields[1],true); if (nu==null) return null;
			return new StudentTDistributionImpl(mu,nu);
		}
	}

	/** U-quadratische Verteilung */
	private static class UQuadraticDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public UQuadraticDistributionPanel() {
			super(new WrapperUQuadraticDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{lower,upper};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((UQuadraticDistribution)distribution).getSupportLowerBound()),
					NumberTools.formatNumberMax(((UQuadraticDistribution)distribution).getSupportUpperBound())
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			UQuadraticDistribution distribution=(UQuadraticDistribution)wrapper.getDistribution(mean,sd);
			if (distribution!=null) {
				if (distribution.getSupportLowerBound()<0) distribution=new UQuadraticDistribution(0,mean*2);
				final String[] text=getValues(distribution);
				if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
			}
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null) return null;
			if (d1>=d2) return null;
			return new UQuadraticDistribution(d1,d2);
		}
	}

	/** Reziproke Verteilung */
	private static class ReciprocalDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public ReciprocalDistributionPanel() {
			super(new WrapperReciprocalDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{lower,upper};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((ReciprocalDistribution)distribution).getSupportLowerBound()),
					NumberTools.formatNumberMax(((ReciprocalDistribution)distribution).getSupportUpperBound())
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			ReciprocalDistribution distribution=(ReciprocalDistribution)wrapper.getDistribution(mean,sd);
			if (distribution!=null) {
				if (distribution.getSupportLowerBound()<0) distribution=new ReciprocalDistribution(0,mean*2);
				final String[] text=getValues(distribution);
				if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
			}
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			if (d1>=d2) return null;
			return new ReciprocalDistribution(d1,d2);
		}
	}

	/** Kumaraswamy-Verteilung */
	private static class KumaraswamyDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public KumaraswamyDistributionPanel() {
			super(new WrapperKumaraswamyDistribution(),new String[]{"a","b",JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{"1","2",lower,upper};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((KumaraswamyDistribution)distribution).a),
					NumberTools.formatNumberMax(((KumaraswamyDistribution)distribution).b),
					NumberTools.formatNumberMax(((KumaraswamyDistribution)distribution).c),
					NumberTools.formatNumberMax(((KumaraswamyDistribution)distribution).d)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getPositiveDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			final Double d3=NumberTools.getPositiveDouble(fields[2],true); if (d3==null) return null;
			final Double d4=NumberTools.getPositiveDouble(fields[3],true); if (d4==null) return null;
			if (d3>=d4) return null;
			return new KumaraswamyDistribution(d1,d2,d3,d4);
		}
	}

	/** Irwin-Hall-Verteilung */
	private static class IrwinHallDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public IrwinHallDistributionPanel() {
			super(new WrapperIrwinHallDistribution(),new String[]{"n"});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{""+(int)Math.max(1,Math.round(meanD*2))};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					""+((IrwinHallDistribution)distribution).n
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Long l=NumberTools.getPositiveLong(fields[0],true);
			if (l==null) return null;
			return new IrwinHallDistribution(l);
		}
	}

	/** Sinus-Verteilung */
	private static class SineDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public SineDistributionPanel() {
			super(new WrapperSineDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{lower,upper};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((SineDistribution)distribution).getSupportLowerBound()),
					NumberTools.formatNumberMax(((SineDistribution)distribution).getSupportUpperBound())
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			SineDistribution distribution=(SineDistribution)wrapper.getDistribution(mean,sd);
			if (distribution!=null) {
				if (distribution.getSupportLowerBound()<0) distribution=new SineDistribution(0,mean*2);
				final String[] text=getValues(distribution);
				if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
			}
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null) return null;
			if (d1>=d2) return null;
			return new SineDistribution(d1,d2);
		}
	}

	/** Arcus Sinus-Verteilung */
	private static class ArcsineDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public ArcsineDistributionPanel() {
			super(new WrapperArcsineDistribution(),new String[]{JDistributionEditorPanel.DistUniformStart,JDistributionEditorPanel.DistUniformEnd});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{lower,upper};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((ArcsineDistribution)distribution).getSupportLowerBound()),
					NumberTools.formatNumberMax(((ArcsineDistribution)distribution).getSupportUpperBound())
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			ArcsineDistribution distribution=(ArcsineDistribution)wrapper.getDistribution(mean,sd);
			if (distribution!=null) {
				if (distribution.getSupportLowerBound()<0) distribution=new ArcsineDistribution(0,mean*2);
				final String[] text=getValues(distribution);
				if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
			}
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null) return null;
			if (d1>=d2) return null;
			return new ArcsineDistribution(d1,d2);
		}
	}

	/** Wigner Halbkreis-Verteilung */
	private static class WignerHalfCircleDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public WignerHalfCircleDistributionPanel() {
			super(new WrapperWignerHalfCircleDistribution(),new String[]{JDistributionEditorPanel.DistMean,JDistributionEditorPanel.DistRadius});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{mean, NumberTools.formatNumberMax(stdD*2)};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((WignerHalfCircleDistributionImpl)distribution).m),
					NumberTools.formatNumberMax(((WignerHalfCircleDistributionImpl)distribution).R)
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			final WignerHalfCircleDistributionImpl distribution=(WignerHalfCircleDistributionImpl)wrapper.getDistribution(Math.max(0,mean),sd);
			final String[] text=getValues(distribution);
			if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getDouble(fields[1],true); if (d2==null) return null;
			return new WignerHalfCircleDistributionImpl(d1,d2);
		}
	}


	/** Halbe Normalverteilung */
	private static class HalfNormalDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public HalfNormalDistributionPanel() {
			super(new WrapperHalfNormalDistribution(),new String[]{"s","mu"});
		}

		@Override
		public String[] getEditValues(final double meanD, final String mean, final double stdD, final String std, final String lower, final String upper, final double maxXValue) {
			return new String[]{"0",mean};
		}

		@Override
		public String[] getValues(final AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((HalfNormalDistribution)distribution).s),
					NumberTools.formatNumberMax(((HalfNormalDistribution)distribution).mu)
			};
		}

		@Override
		public void setValues(final JTextField[] fields, final double mean, final double sd) {
			final HalfNormalDistribution distribution=(HalfNormalDistribution)wrapper.getDistribution(mean,0);
			final String[] text=getValues(distribution);
			if (text!=null && text.length==fields.length) for (int i=0;i<text.length;i++) fields[i].setText(text[i]);
		}

		@Override
		public AbstractRealDistribution getDistribution(final JTextField[] fields, final double maxXValue) {
			final Double d1=NumberTools.getDouble(fields[0],true); if (d1==null) return null;
			final Double d2=NumberTools.getPositiveDouble(fields[1],true); if (d2==null) return null;
			return new HalfNormalDistribution(d1,d2);
		}
	}


	/** Log-Cauchy-Verteilung */
	private static class LogCauchyDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public LogCauchyDistributionPanel() {
			super(new WrapperLogCauchyDistribution(),new String[]{"mu","sigma"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{
					NumberTools.formatNumber(Math.log(Math.max(meanD,0.001))),
					NumberTools.formatNumber(0.5),
			};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((LogCauchyDistributionImpl)distribution).mu),
					NumberTools.formatNumberMax(((LogCauchyDistributionImpl)distribution).sigma),
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double mu=NumberTools.getDouble(fields[0],true); if (mu==null) return null;
			final Double sigma=NumberTools.getPositiveDouble(fields[1],true); if (sigma==null) return null;
			return new LogCauchyDistributionImpl(mu,sigma);
		}
	}

	/** Hypergeometrische Verteilung */
	private static class HyperGeomDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public HyperGeomDistributionPanel() {
			super(new WrapperHyperGeomDistribution(),new String[]{"N","K","n"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"50","20","10"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					""+((DiscreteHyperGeomDistributionImpl)distribution).N,
					""+((DiscreteHyperGeomDistributionImpl)distribution).K,
					""+((DiscreteHyperGeomDistributionImpl)distribution).n
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Long N=NumberTools.getPositiveLong(fields[0],true); if (N==null) return null;
			final Integer K=NumberTools.getNotNegativeInteger(fields[1],true); if (K==null) return null;
			final Long n=NumberTools.getPositiveLong(fields[2],true); if (n==null) return null;
			return new DiscreteHyperGeomDistributionImpl(N.intValue(),K,n.intValue());
		}
	}

	/** Binomialverteilung */
	private static class BinomialDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public BinomialDistributionPanel() {
			super(new WrapperBinomialDistribution(),new String[]{"p","n"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			if (meanD<=0 || stdD<=0) return new String[]{NumberTools.formatNumber(0.5),"10"};
			/* E=n*p, Var=n*p*(1-p) => n=E/p, p=1-Var/E */
			final double p=1-stdD*stdD/meanD;
			if (Double.isNaN(p) || p<=0 || p>1) return new String[]{NumberTools.formatNumber(0.5),"10"};
			final int n=(int)Math.round(meanD/p);
			return new String[]{NumberTools.formatNumber(p),""+n};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((DiscreteBinomialDistributionImpl)distribution).p),
					""+((DiscreteBinomialDistributionImpl)distribution).n
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double p=NumberTools.getNotNegativeDouble(fields[0],true); if (p==null) return null;
			final Long n=NumberTools.getPositiveLong(fields[1],true); if (n==null) return null;
			return new DiscreteBinomialDistributionImpl(p,n.intValue());
		}
	}

	/** Poisson-Verteilung */
	private static class PoissonDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public PoissonDistributionPanel() {
			super(new WrapperPoissonDistribution(),new String[]{"lambda"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{mean};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((DiscretePoissonDistributionImpl)distribution).lambda)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double lambda=NumberTools.getPositiveDouble(fields[0],true); if (lambda==null) return null;
			return new DiscretePoissonDistributionImpl(lambda);
		}
	}

	/** Negative Binomialverteilung */
	private static class NegativeBinomialDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public NegativeBinomialDistributionPanel() {
			super(new WrapperNegativeBinomialDistribution(),new String[]{"p","r"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			if (meanD<=0 || stdD<=0) return new String[]{NumberTools.formatNumber(0.5),"10"};
			/* E=r(1-p)/p, Var=r(1-p)/p^2 => p=E/Var, r=E*p/(1-p) */
			final double p=meanD/(stdD*stdD);
			if (p<=0 || p>1) return new String[]{NumberTools.formatNumber(0.5),"10"};
			final int r=(int)Math.round(meanD*p/(1-p));
			return new String[]{NumberTools.formatNumber(p),""+r};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((DiscreteNegativeBinomialDistributionImpl)distribution).p),
					""+((DiscreteNegativeBinomialDistributionImpl)distribution).r
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double p=NumberTools.getNotNegativeDouble(fields[0],true); if (p==null) return null;
			final Long r=NumberTools.getPositiveLong(fields[1],true); if (r==null) return null;
			return new DiscreteNegativeBinomialDistributionImpl(p,r.intValue());
		}
	}

	/** Negative Hypergeometrische Verteilung */
	private static class NegativeHyperGeomDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public NegativeHyperGeomDistributionPanel() {
			super(new WrapperNegativeHyperGeomDistribution(),new String[]{"N","K","n"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"50","20","10"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					""+((DiscreteNegativeHyperGeomDistributionImpl)distribution).N,
					""+((DiscreteNegativeHyperGeomDistributionImpl)distribution).K,
					""+((DiscreteNegativeHyperGeomDistributionImpl)distribution).n
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Long N=NumberTools.getPositiveLong(fields[0],true); if (N==null) return null;
			final Integer K=NumberTools.getNotNegativeInteger(fields[1],true); if (K==null) return null;
			final Long n=NumberTools.getPositiveLong(fields[2],true); if (n==null) return null;
			return new DiscreteNegativeHyperGeomDistributionImpl(N.intValue(),K,n.intValue());
		}
	}

	/** Zeta-Verteilung */
	private static class ZetaDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public ZetaDistributionPanel() {
			super(new WrapperZetaDistribution(),new String[]{"s"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{"3"};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((DiscreteZetaDistributionImpl)distribution).s)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double s=NumberTools.getNotNegativeDouble(fields[0],true); if (s==null || s<=1.0) return null;
			return new DiscreteZetaDistributionImpl(s);
		}
	}

	/** Diskrete Gleichverteilung */
	private static class DiscreteUniformDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public DiscreteUniformDistributionPanel() {
			super(new WrapperDiscreteUniformDistribution(),new String[]{"a","b"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			if (meanD<=0 || stdD<=0) return new String[]{"2","5"};
			/* E=(a+b)/2, Var=((b-a+1)^2-1)/12 => b=sqrt(12Var+1)/2+E-1/2, a=2E-b */
			int b=(int)Math.round(Math.sqrt(12*stdD*stdD+1)/2+meanD-0.5);
			int a=(int)Math.round(2*meanD-b);
			if (a<0) a=0;
			if (b<a) b=a+1;
			return new String[]{""+a,""+b};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					""+((DiscreteUniformDistributionImpl)distribution).a,
					""+((DiscreteUniformDistributionImpl)distribution).b
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Long a=NumberTools.getNotNegativeLong(fields[0],true); if (a==null) return null;
			final Long b=NumberTools.getNotNegativeLong(fields[1],true); if (b==null) return null;
			return new DiscreteUniformDistributionImpl(a.intValue(),b.intValue());
		}
	}

	/** Geometrische Verteilung */
	private static class GeometricDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public GeometricDistributionPanel() {
			super(new WrapperGeometricDistribution(),new String[]{"p"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			/* (1-p)/p=mean <=> 1/(1+mean)=p */
			return new String[] {
					NumberTools.formatNumberMax(1/(1+Math.max(1,meanD)))
			};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((DiscreteGeometricDistributionImpl)distribution).p)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double p=NumberTools.getPositiveDouble(fields[0],true);
			if (p==null) return null;
			if (p>=1) {
				fields[0].setBackground(Color.RED);
				return null;
			} else {
				fields[0].setBackground(NumberTools.getTextFieldDefaultBackground());
			}
			return new DiscreteGeometricDistributionImpl(p);
		}
	}

	/** Logarithmische Verteilung */
	private static class LogarithmicDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public LogarithmicDistributionPanel() {
			super(new WrapperLogarithmicDistribution(),new String[]{"p"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(DiscreteLogarithmicDistributionImpl.getPFromMean(meanD))};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((DiscreteLogarithmicDistributionImpl)distribution).p)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double p=NumberTools.getPositiveDouble(fields[0],true); if (p==null) return null;
			return new DiscreteLogarithmicDistributionImpl(p);
		}
	}

	/** Borel-Verteilung */
	private static class BorelDistributionPanel extends JDistributionEditorPanelRecord {
		/** Konstruktor der Klasse */
		public BorelDistributionPanel() {
			super(new WrapperBorelDistribution(),new String[]{"mu"});
		}

		@Override
		public String[] getEditValues(double meanD, String mean, double stdD, String std, String lower, String upper, double maxXValue) {
			return new String[]{NumberTools.formatNumberMax(DiscreteLogarithmicDistributionImpl.getPFromMean(Math.max(1,meanD)))};
		}

		@Override
		public String[] getValues(AbstractRealDistribution distribution) {
			return new String[] {
					NumberTools.formatNumberMax(((DiscreteBorelDistributionImpl)distribution).mu)
			};
		}

		@Override
		public AbstractRealDistribution getDistribution(JTextField[] fields, double maxXValue) {
			final Double mu=NumberTools.getDouble(fields[0],true); if (mu==null || mu<0 || mu>1) return null;
			return new DiscreteBorelDistributionImpl(mu);
		}
	}
}