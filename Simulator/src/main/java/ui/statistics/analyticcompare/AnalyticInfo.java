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
package ui.statistics.analyticcompare;

import language.Language;
import mathtools.ErlangC;
import mathtools.NumberTools;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import statistics.StatisticsTimePerformanceIndicator;
import ui.modeleditor.ModelResource;
import ui.statistics.StatisticTools;

/**
 * Diese Klasse liefert Informationen auf Basis von analytischen Vergleichsmodellen
 * zu einem Editor-Modell. Optional werden die Ergebnisse mit Simulationsergebnissen
 * zum Vergleich angereichert.
 * @author Alexander Herzog
 */
public class AnalyticInfo {
	/**
	 * Daten zur Kundenquelle
	 */
	private AnalyticSource source;

	/**
	 * Daten zur Bedienstation
	 */
	private AnalyticProcess process;

	/**
	 * Konstruktor der Klasse
	 */
	public AnalyticInfo() {
		reset();
	}

	/**
	 * Setzt das bereits geladene Modell zurück.
	 */
	public void reset() {
		source=null;
		process=null;
	}

	/**
	 * Versucht die für den Vergleich relevanten Komponenten aus
	 * einem Editor-Modell auszulesen.
	 * @param editModel	Editor-Modell aus dem die Komponenten ausgelesen werden sollen
	 * @return	Gibt an, ob die Komponenten erfolgreich extrahiert werden konnten
	 * @see AnalyticModel#getSource()
	 * @see AnalyticModel#getProcess()
	 * @see #getSourceInfo()
	 * @see #getProcessInfo()
	 */
	public boolean build(final EditModel editModel) {
		reset();

		final AnalyticModel model=new AnalyticModel();
		if (!model.build(editModel)) return false;

		source=new AnalyticSource(model.getSource());
		process=new AnalyticProcess(model.getProcess(),source.name,model.getResources());

		return true;
	}

	/**
	 * Liefert die Parameter der Quelle, die für die folgenden Berechnungen verwendet werden, in Textform.
	 * @return	Parameter der Quelle in Textform oder ein leerer String, wenn noch kein Modell geladen wurde
	 */
	public String getSourceInfo() {
		return (source==null)?"":source.getInfo();
	}

	/**
	 * Liefert die Parameter der Bedienstation, die für die folgenden Berechnungen verwendet werden, in Textform.
	 * @return	Parameter der Bedienstation in Textform oder ein leerer String, wenn noch kein Modell geladen wurde
	 */
	public String getProcessInfo() {
		return (process==null)?"":process.getInfo();
	}

	/**
	 * Gibt an, ob in dem Modell ungeduldige Kunden auftreten, d.h. ob neben der Erlang-C-Formel auch die erweiterte Erlang-C-Formel für dieses Modell von Interesse ist.
	 * @return	Liefert <code>true</code>, wenn die Kunden in dem Modell eine endliche Wartezeittoleranz besitzen
	 */
	public boolean hasCancelTimes() {
		if (process==null) return false;
		return process.cancelDistribution!=null;
	}

	/**
	 * Prüft, ob eine analytische Bewertung des Modells möglich ist.
	 * @param info	Trägt in dieses Objekt (wenn ein Wert ungleich <code>null</code> angegeben wurde) mögliche Grunde, warum eine analytische Untersuchung nicht möglich ist, ein.
	 * @return	Liefert <code>true</code>, wenn das Modell analytisch bewertet werden kann.
	 * @see InfoResult
	 */
	private boolean analyticTests(final InfoResult info) {
		if (source.distribution==null) {
			if (info!=null) info.input.append(Language.tr("Statistics.ErlangCompare.NoSourceDistribution"));
			return false;
		}
		if (process.distribution==null) {
			if (info!=null) info.input.append(Language.tr("Statistics.ErlangCompare.NoProcessDistribution"));
			return false;
		}
		if (process.cAvailable<=0) {
			if (info!=null) info.input.append(Language.tr("Statistics.ErlangCompare.NoFixedNumberOfOperators"));
			return false;
		}

		if (source.batch<1) {
			if (info!=null) info.input.append(Language.tr("Statistics.ErlangCompare.UnknownSourceBatchSize"));
			return false;
		}

		if (process.batchMin<1 || process.batchMax<1) {
			if (info!=null) info.input.append(Language.tr("Statistics.ErlangCompare.UnknownProcessBatchSize"));
			return false;
		}

		return true;
	}

	/**
	 * Berechnet a^c/c! unter Vermeidung von numerischen Auslöschungen
	 * @param a	Parameter a für a^c/c!
	 * @param c	Parameter c für a^c/c!
	 * @return	Ergebnis der Berechnung von a^c/c!
	 */
	private double powerFactorial(final double a, final long c) {
		/* a^c/c! */
		double result=1;
		for (int i=1;i<=c;i++) result*=(a/i);
		return result;
	}

	/**
	 * Liefert einen String, der ein analytisches Ergebnis einem Simulationsergebnis gegenüberstellt (Format: Zahl)
	 * @param info	Voran zu stellender Info-Text
	 * @param analytic	Analytischer Wert
	 * @param simulation	Simulationswert (kann 0 sein, dann wird kein Wert ausgegeben)
	 * @return	Zusammengesetzter String
	 */
	private static String buildInfoNumber(final String info, final double analytic, final double simulation) {
		final StringBuilder result=new StringBuilder();
		result.append(info);
		result.append('=');
		result.append(StatisticTools.formatNumber(analytic));
		if (simulation!=0) {
			result.append(", ");
			result.append(Language.tr("Statistics.ErlangCompare.SimulationDelta"));
			result.append(": ");
			result.append(StatisticTools.formatPercent(((analytic-simulation)/simulation),2));
		}
		result.append("\n");
		return result.toString();
	}

	/**
	 * Liefert einen String, der ein analytisches Ergebnis einem Simulationsergebnis gegenüberstellt (Format: Zeitangabe)
	 * @param info	Voran zu stellender Info-Text
	 * @param analytic	Analytischer Wert
	 * @param simulation	Simulationswert (kann 0 sein, dann wird kein Wert ausgegeben)
	 * @return	Zusammengesetzter String
	 */
	private static String buildInfoTime(final String info, final double analytic, final double simulation) {
		final StringBuilder result=new StringBuilder();
		result.append(info);
		result.append('=');
		result.append(StatisticTools.formatNumber(analytic));
		result.append(" ");
		result.append(Language.tr("Statistics.Seconds"));
		result.append(" (");
		result.append(TimeTools.formatExactTime(analytic,1));
		result.append(")");
		if (simulation!=0) {
			result.append(", ");
			result.append(Language.tr("Statistics.ErlangCompare.SimulationDelta"));
			result.append(": ");
			result.append(StatisticTools.formatPercent(((analytic-simulation)/simulation),2));
		}
		result.append("\n");
		return result.toString();
	}

	/**
	 * Versucht die Auslastung rho zu berechnen.
	 * @return	Liefert im Erfolgsfall die Auslastung, sonst <code>null</code>.
	 */
	public Double getRho() {
		if (!analyticTests(null)) return null;
		final double lambda=source.lambda;
		final double mu=process.mu;

		final long c=process.cAvailable*process.batchMean;
		final double a=lambda/mu;
		return a/c;
	}

	/**
	 * Liefert analytische Daten auf Basis eines Erlang-C-Modells.
	 * @param simulationResults	Wird ein Wert ungleich <code>null</code> übergeben, so werden die Simulationsergebnisse in die Ergebnisausgabe eingebunden.
	 * @return	Resultate auf Basis der Erlang-C-Formel
	 * @see InfoResult
	 */
	public InfoResult getErlangC(final SimulationResults simulationResults) {
		final InfoResult info=new InfoResult();
		if (!analyticTests(info)) return info;

		final double lambda=source.lambda;
		final double mu=process.mu;
		final long c=process.cAvailable*process.batchMean;
		final double a=lambda/mu;

		info.input.append("lambda="+NumberTools.formatNumberMax(lambda)+" (1/"+Language.tr("LoadCalculator.Units.Seconds")+")\n");
		info.input.append("mu="+NumberTools.formatNumberMax(mu)+" (1/"+Language.tr("LoadCalculator.Units.Seconds")+")\n");
		info.input.append("c="+c+"\n");

		info.calculated.append("a="+StatisticTools.formatNumber(a,2)+"\n");
		info.calculated.append("rho="+StatisticTools.formatPercent(a/c,2)+"\n");

		if (a/c>=1) return info;

		double P1=0;
		for (int i=0;i<=c-1;i++)
			P1+=powerFactorial(a,i);
		double temp=powerFactorial(a,c)*c/(c-a);
		P1=temp/(P1+temp);

		double EW=P1/(c*mu-lambda);
		double EV=EW+1/mu;
		double ENQ=P1*a/(c-a);
		double EN=lambda*EV;

		info.calculated.append("P1="+StatisticTools.formatNumber(P1,2)+"\n");

		info.setResults(ENQ,EN,EW,EV,simulationResults);
		if (source.cv!=1.0) info.addInfo(String.format(Language.tr("Statistics.ErlangCompare.Info.Source.CV"),StatisticTools.formatNumber(source.cv)));
		if (source.batch!=1) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Source.Batch"));
		if (process.cv!=1.0) info.addInfo(String.format(Language.tr("Statistics.ErlangCompare.Info.Process.CV"),StatisticTools.formatNumber(process.cv)));
		if (process.batchMean!=1.0) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.Batch"));
		if (process.cancelDistribution!=null) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.CancelDistribution"));
		if (!process.distributionIsExact) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.DistributionNotExact"));

		return info;
	}

	/**
	 * Liefert analytische Daten auf Basis eines erweiterten Erlang-C-Modells (d.h. inkl. Abbrechern).
	 * @param simulationResults	Wird ein Wert ungleich <code>null</code> übergeben, so werden die Simulationsergebnisse in die Ergebnisausgabe eingebunden.
	 * @return	Resultate auf Basis der erweiterten Erlang-C-Formel
	 * @see InfoResult
	 */
	public InfoResult getErlangCExt(final SimulationResults simulationResults) {
		final InfoResult info=new InfoResult();
		if (!analyticTests(info)) return info;

		final double lambda=source.lambda;
		final double mu=process.mu;
		final double nu=process.nu;
		final long c=process.cAvailable*process.batchMean;
		final double a=lambda/mu;

		info.input.append("lambda="+NumberTools.formatNumberMax(lambda)+" (1/"+Language.tr("LoadCalculator.Units.Seconds")+")\n");
		info.input.append("mu="+NumberTools.formatNumberMax(mu)+" (1/"+Language.tr("LoadCalculator.Units.Seconds")+")\n");
		info.input.append("nu="+NumberTools.formatNumberMax(nu)+" (1/"+Language.tr("LoadCalculator.Units.Seconds")+")\n");
		info.input.append("c="+c+"\n");

		info.calculated.append("a="+StatisticTools.formatNumber(a,2)+"\n");
		info.calculated.append("rho("+Language.tr("Statistics.ErlangCompare.rho.noCancelations")+")="+StatisticTools.formatPercent(a/c,2)+"\n");

		final int K=1000;

		double[] Cn=ErlangC.extErlangCCn(lambda,mu,nu,(int)c,K);
		double pi0=0;
		for (int i=0;i<Cn.length;i++) pi0+=Cn[i];
		pi0=1/pi0;

		double ENQ=0; for (int i=(int)(c+1);i<Cn.length;i++) ENQ+=(i-c)*Cn[i]*pi0;
		double EN=0; for (int i=1;i<Cn.length;i++) EN+=i*Cn[i]*pi0;
		double EW=ENQ/lambda;
		double EV=EW+1/mu;
		double PA=ENQ*nu/lambda;

		info.calculated.append("rho("+Language.tr("Statistics.ErlangCompare.rho.actual")+")="+StatisticTools.formatPercent((lambda-ENQ*nu)/mu/c,2)+"\n");

		info.setResults(ENQ,EN,EW,EV,simulationResults);
		info.times.append("P(A)="+StatisticTools.formatPercent(PA,2));

		if (source.cv!=1.0) info.addInfo(String.format(Language.tr("Statistics.ErlangCompare.Info.Source.CV"),StatisticTools.formatNumber(source.cv)));
		if (source.batch!=1) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Source.Batch"));
		if (process.cv!=1.0) info.addInfo(String.format(Language.tr("Statistics.ErlangCompare.Info.Process.CV"),StatisticTools.formatNumber(process.cv)));
		if (process.batchMean!=1.0) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.Batch"));
		if (process.cancelDistribution!=null) {
			if (process.cancelCv!=1.0) info.addInfo(String.format(Language.tr("Statistics.ErlangCompare.Info.Process.CancelCV"),StatisticTools.formatNumber(process.cv)));
		}
		if (!process.distributionIsExact) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.DistributionNotExact"));

		return info;
	}

	/**
	 * Liefert analytische Daten auf Basis eines Allen-Cunneen-Modells.
	 * @param simulationResults	Wird ein Wert ungleich <code>null</code> übergeben, so werden die Simulationsergebnisse in die Ergebnisausgabe eingebunden.
	 * @return	Resultate auf Basis der Allen-Cunneen-Formel
	 * @see InfoResult
	 */
	public InfoResult getAllenCunneen(final SimulationResults simulationResults) {
		final InfoResult info=new InfoResult();
		if (!analyticTests(info)) return info;

		double lambda=source.lambda;
		final double mu=process.mu;
		final long c=process.cAvailable;

		final long bI=source.batch;
		final long bS=process.batchMean;
		double cvI=source.cv;
		final double cvS=process.cv;

		/* Umrechnung von Arrival-Batches auf einzelne Kunden */
		lambda=lambda*bI;
		cvI=Math.sqrt(bI*cvI*cvI+bI-1);

		double a=lambda/mu;
		double rho=lambda/mu/(bS*c);

		info.input.append("lambda="+NumberTools.formatNumberMax(lambda)+" (1/"+Language.tr("LoadCalculator.Units.Seconds")+")\n");
		info.input.append("CV[I]="+StatisticTools.formatNumber(cvI)+"\n");
		info.input.append("mu="+NumberTools.formatNumberMax(mu)+" (1/"+Language.tr("LoadCalculator.Units.Seconds")+")\n");
		info.input.append("CV[S]="+StatisticTools.formatNumber(cvS)+"\n");
		info.input.append("c="+c+"\n");
		info.input.append("b(I)="+bI+"\n");
		info.input.append("b(S)="+bS+"\n");

		info.calculated.append("a="+StatisticTools.formatNumber(a,2)+"\n");
		info.calculated.append("rho="+StatisticTools.formatPercent(rho,2)+"\n");
		if (rho>=1) return info;

		/*
		PC1=(c*rho)^c/(c!(1-rho));
		PC=PC1/(PC1+sum(k=0...c-1; (c*rho)^k/k!))
		E[NQ]=rho/(1-rho)*PC*(SCV[I]+b*SCV[S])/2+(b-1)/2
		E[N]=E[NQ]+b*c*rho
		 */

		double PC1=powerFactorial(c*rho,c)/(1-rho);
		double PC=0; for(int i=0;i<=c-1;i++) PC+=powerFactorial(c*rho,i);
		PC=PC1/(PC1+PC);

		double ENQ=rho/(1-rho)*PC*(cvI*cvI+bS*cvS*cvS)/2+(((double)bS)-1)/2;
		double EN=ENQ+((double)bS)*((double)c)*rho;
		double EW=ENQ/lambda;
		double EV=EW+1/mu;

		info.setResults(ENQ,EN,EW,EV,simulationResults);
		if (source.batch!=1) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Source.Batch"));
		if (process.batchMean!=1.0) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.Batch"));
		if (process.cancelDistribution!=null) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.CancelDistribution"));
		if (!process.distributionIsExact) info.addInfo(Language.tr("Statistics.ErlangCompare.Info.Process.DistributionNotExact"));

		return info;
	}

	/**
	 * Prüft, ob analytische Vergleichsdaten für ein Modell berechnet werden können,
	 * also ob die Parameter, die für die Berechnung der Modelle notwendig sind,
	 * ermittelt werden können.
	 * @param editModel	Zu prüfendes Editor-Modell
	 * @return	Gibt an, ob von dem Modell ein analytisches Modell abgeleitet werden kann
	 */
	public static boolean canCompare(final EditModel editModel) {
		final AnalyticModel model=new AnalyticModel();
		return model.build(editModel);
	}

	/**
	 * Prüft, ob analytische Vergleichsdaten für ein Modell berechnet werden können,
	 * also ob die Parameter, die für die Berechnung der Modelle notwendig sind,
	 * ermittelt werden können.
	 * @param statistics	Statistikdaten aus denen das zu prüfendes Editor-Modell ausgelesen werden soll
	 * @return	Gibt an, ob von dem Modell ein analytisches Modell abgeleitet werden kann
	 */
	public static boolean canCompare(final Statistics statistics) {
		final AnalyticModel model=new AnalyticModel();
		return model.build(statistics);
	}

	/**
	 * Diese Klasse liest aus einem {@link Statistics}-Objekt die wesentlichen Kenngrößen aus,
	 * die dann als Vergleichswerte in den Ergebnissen der analytischen Modelle angegeben werden.
	 * @author Alexander Herzog
	 * @see AnalyticInfo#getErlangC(SimulationResults)
	 * @see AnalyticInfo#getErlangCExt(SimulationResults)
	 * @see AnalyticInfo#getAllenCunneen(SimulationResults)
	 */
	public static class SimulationResults {
		/**
		 * Mittlere Warteschlangenlänge E[NQ]
		 */
		public final double ENQ;

		/**
		 * Mittlere Anzahl an Kunden in Bedienung E[NS]
		 */
		public final double ENS;

		/**
		 * Mittlere Anzahl an Kunden im System E[S]
		 */
		public final double EN;

		/**
		 * Mittlere Wartezeit E[W]
		 */
		public final double EW;

		/**
		 * Mittlere Verweilzeit E[V]
		 */
		public final double EV;

		/**
		 * Liefert einen evtl. mehrzeiligen Infotext, der Informationen
		 * über die in dem Modell vorhandenen Ressourcen enthält.
		 */
		public final String resourceInfo;

		/**
		 * Konstruktor der Klasse
		 * @param statistics	Statistikobjekt aus dem die Kenngrößen ausgelesen werden sollen
		 */
		public SimulationResults(final Statistics statistics) {
			ENQ=statistics.clientsInSystemQueues.getTimeMean();
			EN=statistics.clientsInSystem.getTimeMean();
			ENS=EN-ENQ;
			EW=statistics.clientsAllWaitingTimes.getMean();
			EV=statistics.clientsAllResidenceTimes.getMean();

			final StringBuilder sb=new StringBuilder();
			for (String resource: statistics.resourceUtilization.getNames()) {
				final StatisticsTimePerformanceIndicator indicator=((StatisticsTimePerformanceIndicator)statistics.resourceUtilization.get(resource));
				final double meanState=indicator.getTimeMean();
				final ModelResource resourceObj=statistics.editModel.resources.get(resource);
				if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_NUMBER) {
					int count=resourceObj.getCount();
					if (count>0) {
						final StatisticsTimePerformanceIndicator countIndicator=(StatisticsTimePerformanceIndicator)(statistics.resourceCount.getOrNull(resource));
						if (sb.length()>0) sb.append("\n");
						if (countIndicator==null || countIndicator.getTimeMean()<0.0001) {
							sb.append(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState,2)+" (rho="+StatisticTools.formatPercent(meanState/count)+")");
						} else {
							sb.append(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState,2)+" (rho="+StatisticTools.formatPercent(meanState/countIndicator.getTimeMean())+")");
						}
						if (!resourceObj.getFailures().isEmpty()) {
							final StatisticsTimePerformanceIndicator indicator2=(StatisticsTimePerformanceIndicator)(statistics.resourceInDownTime.get(resource));
							if (sb.length()>0) sb.append("\n");
							sb.append(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.FailureTime.AveragePartOfDownTimeOperators")+": "+StatisticTools.formatPercent(indicator2.getTimeMean()/count));
						}
					} else {
						if (sb.length()>0) sb.append("\n");
						sb.append(Language.tr("Statistics.Resource")+" "+resource+": "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState));
					}
				}
				if (resourceObj!=null && resourceObj.getMode()==ModelResource.Mode.MODE_SCHEDULE) {
					if (sb.length()>0) sb.append("\n");
					sb.append(Language.tr("Statistics.Resource")+" "+resource+" ("+Language.tr("Statistics.bySchedule")+" "+resourceObj.getSchedule()+"): "+Language.tr("Statistics.AverageNumberOfBusyOperators")+"="+StatisticTools.formatNumber(meanState));
				}
			}
			resourceInfo=sb.toString();
		}
	}

	/**
	 * Diese Klasse kapselt Informationen zu analytischen Vergleichsmodellen.<br>
	 * Enthalten sind die jeweils verwendeten Eingangsgrößen sowie die Ergebnisse
	 * gruppiert nach verschiedenen Themen.
	 * @author Alexander Herzog
	 * @see AnalyticInfo#getErlangC(SimulationResults)
	 * @see AnalyticInfo#getErlangCExt(SimulationResults)
	 * @see AnalyticInfo#getAllenCunneen(SimulationResults)
	 */
	public static class InfoResult {
		/** Für das analytische Modell verwendete Eingabeparameter */
		private final StringBuilder input;
		/** Allgemeine berechnete Daten auf Basis des analytischen Modells */
		private final StringBuilder calculated;
		/** Berechnete Anzahlwerte auf Basis des analytischen Modells */
		private final StringBuilder numbers;
		/** Berechnete Zeiten auf Basis des analytischen Modells */
		private final StringBuilder times;
		/** Zusätzliche Informationen zu den analytischen Ergebnissen (z.B. Erklärungen für Abweichungen) */
		private final StringBuilder info;

		/**
		 * Konstruktor der Klasse<br>
		 * Kann von außerhalb von {@link AnalyticInfo} nicht aufgerufen werden.
		 */
		private InfoResult() {
			input=new StringBuilder();
			calculated=new StringBuilder();
			numbers=new StringBuilder();
			times=new StringBuilder();
			info=new StringBuilder();
		}

		private void setResults(final double ENQ, final double EN, final double EW, final double EV, final SimulationResults simulationResults) {
			if (simulationResults==null) {
				numbers.append(buildInfoNumber("E[NQ]",ENQ,0));
				numbers.append(buildInfoNumber("E[NS]",EN-ENQ,0));
				numbers.append(buildInfoNumber("E[N]",EN,0));
				times.append(buildInfoTime("E[W]",EW,0));
				times.append(buildInfoTime("E[V]",EV,0));
			} else {
				numbers.append(buildInfoNumber("E[NQ]",ENQ,simulationResults.ENQ));
				numbers.append(buildInfoNumber("E[NS]",EN-ENQ,simulationResults.ENS));
				numbers.append(buildInfoNumber("E[N]",EN,simulationResults.EN));
				times.append(buildInfoTime("E[W]",EW,simulationResults.EW));
				times.append(buildInfoTime("E[V]",EV,simulationResults.EV));
			}
		}

		private void addInfo(final String info) {
			if (this.info.length()>0) this.info.append('\n');
			this.info.append(info);
		}

		/**
		 * Liefert die für das analytische Modell verwendeten Eingabeparameter
		 * @return	Eingabeparameter (mehrzeilig mit Zeilenumbrüchen)
		 */
		public String getInput() {
			return input.toString();
		}

		/**
		 * Liefert allgemeine berechnete Daten auf Basis des analytischen Modells
		 * @return	Berechnete Größen (mehrzeilig mit Zeilenumbrüchen)
		 */
		public String getCalculated() {
			return calculated.toString();
		}

		/**
		 * Liefert berechnete Anzahlwerte auf Basis des analytischen Modells
		 * @return	Berechnete Größen (mehrzeilig mit Zeilenumbrüchen)
		 */
		public String getNumbers() {
			return numbers.toString();
		}

		/**
		 * Liefert berechnete Zeiten auf Basis des analytischen Modells
		 * @return	Berechnete Größen (mehrzeilig mit Zeilenumbrüchen)
		 */
		public String getTimes() {
			return times.toString();
		}

		/**
		 * Liefert zusätzliche Informationen zu den analytischen Ergebnissen (z.B. Erklärungen für Abweichungen)
		 * @return	Zusätzliche Informationen zu den analytischen Ergebnissen
		 */
		public String getInfo() {
			return info.toString();
		}
	}
}