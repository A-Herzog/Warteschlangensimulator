/**
 * Copyright 2021 Alexander Herzog
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
package ui.statistics;

import java.net.URL;
import java.util.Arrays;

import language.Language;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsDataPerformanceIndicatorWithNegativeValues;
import statistics.StatisticsMultiPerformanceIndicator;
import statistics.StatisticsPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.statistics.StatisticViewerText;
import ui.help.Help;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Dieser Viewer analysiert das Modell und gibt Hinweise zu Kenngrößen,
 * die sich außerhalb des normalerweise erwartbarem bewegen, aus. Diese
 * Kenngrößen weisen auf Probleme oder Flaschenhälse im System hin.
 * @see StatisticViewerText
 * @author Alexander Herzog
 */
public class StatisticViewerRemarksText extends StatisticViewerText {
	/**
	 * Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	private final Statistics statistics;

	/**
	 * Warnwert für "Viele Kunden im System"
	 * @see #buildTextLargeN()
	 */
	private static final double LARGE_N_SYSTEM=20_000;

	/**
	 * Warnwert für "Viele Kunden an einer Station"
	 * @see #buildTextLargeN()
	 */
	private static final double LARGE_N_STATION=10_000;

	/**
	 * Warnwert für "Viele Kunden von einem Typ im System"
	 * @see #buildTextLargeN()
	 */
	private static final double LARGE_N_CLIENTTYPE=10_000;

	/**
	 * Warnwert für "Viele Kunden an einer Station" / "Viele Kunden von einem Typ im System"
	 * im Verhältnis zu den anderen Station bzw. Kundentypen
	 * @see #buildTextRelativeLargeN()
	 */
	private static final double LARGE_N_FACTOR=2;

	/**
	 * Warnwert für "Lange Wartezeiten an einer Station" / "Lange Wartezeiten der Kunden eines Typs"
	 * im Verhältnis zu den anderen Station bzw. Kundentypen
	 * @see #buildTextRelativeLongW()
	 */
	private static final double LARGE_W_FACTOR=2;

	/**
	 * Warnwert für "Hoher Flussgrad im System"
	 * @see #buildTextFlowFactor()
	 */
	private static final double FLOW_FACTOR_SYSTEM=20;

	/**
	 * Warnwert für "Hoher Flussgrad an einer Station"
	 * @see #buildTextFlowFactor()
	 */
	private static final double FLOW_FACTOR_STATION=20;

	/**
	 * Warnwert für "Hoher Flussgrad für einen Kundentyp"
	 * @see #buildTextFlowFactor()
	 */
	private static final double FLOW_FACTOR_CLIENTTYPE=20;

	/**
	 * Warnwert für "Hoher Flussgrad an einer Station" / "Hoher Flussgrad für einen Kundentyp"
	 * im Verhältnis zu den anderen Station bzw. Kundentypen
	 * @see #buildTextRelativeFlowFactor()
	 */
	private static final double FLOW_FACTOR_FACTOR=2;

	/**
	 * Warnwert für "Hoher Variationskoeffizient der Verweilzeiten im System"
	 * @see #buildTextCVV()
	 */
	private static final double CVV_SYSTEM=1.75;

	/**
	 * Warnwert für "Hoher Variationskoeffizient der Verweilzeiten an einer Station"
	 * @see #buildTextCVV()
	 */
	private static final double CVV_STATION=1.75;

	/**
	 * Warnwert für "Hoher Variationskoeffizient der Verweilzeiten für einen Kundentyp"
	 * @see #buildTextCVV()
	 */
	private static final double CVV_CLIENTTYPE=1.75;

	/**
	 * Warnwert für "Hohe Auslastung einer Bedienergruppe"
	 * @see #buildTextLargeRho()
	 */
	private static final double RHO=0.98;

	/**
	 * Warnwert für "Hohe Auslastung einer Bedienergruppe"
	 * im Verhältnis zu den anderen Bedienergruppen
	 * @see #buildTextRelativeLargeRho()
	 */
	private static final double RHO_FACTOR=2;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerRemarksText(final Statistics statistics) {
		super();
		this.statistics=statistics;
	}

	/**
	 * Prüfen, ob während der Simulation Fehler oder Warnungen aufgetreten sind.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 */
	private boolean buildTextError() {
		if (!statistics.simulationData.emergencyShutDown && (statistics.simulationData.warnings==null || statistics.simulationData.warnings.length==0)) return false;

		if (statistics.simulationData.emergencyShutDown) {
			addHeading(2,Language.tr("Statistics.EmergencyShutDown.Title"));
		} else {
			addHeading(2,Language.tr("Statistics.Warnings.Title"));
		}

		beginParagraph();
		if (statistics.simulationData.emergencyShutDown) {
			addLine(Language.tr("Statistics.EmergencyShutDown"));
		} else {
			if (statistics.simulationData.warnings!=null) addLine(Language.tr("Statistics.Warnings.Info"));
		}
		if (statistics.simulationData.warnings!=null) Arrays.asList(statistics.simulationData.warnings).stream().forEach(s->{if (s!=null) addLine(s);});
		endParagraph();

		return true;
	}

	/**
	 * Testet, ob ein Modell über externe Kundendatenquellen verfügt.
	 * @param surface	Zeichenfläche, deren Stationen (inkl. möglicher Untermodelle) untersucht werden sollen
	 * @return	Liefert <code>true</code>, das Modell mindestens eine externe Kundendatenquellen besitzt
	 */
	private boolean hasExternalDataSource(final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) if (hasExternalDataSource(((ModelElementSub)element).getSubSurface())) return true;

			if (element instanceof ModelElementSourceTable) return true;
			if (element instanceof ModelElementSourceDB) return true;
			if (element instanceof ModelElementSourceDDE) return true;
		}
		return false;
	}

	/**
	 * Prüft, ob es Einschwingphasen-bedingte Warnungen gibt.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 */
	private boolean buildTextWarmUpWarnings() {
		boolean headingPrinted=false;

		/* Insgesamt am System eingetroffene Kunden */
		long sum=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();

		/* Einschwingphase vorhanden? */
		final boolean hasWarmUp=(statistics.editModel.warmUpTime>0);

		/* Sind externe Datenquellen angebunden? */
		final boolean hasExternalDataSource=hasExternalDataSource(statistics.editModel.surface);

		/* Warnung: Aufgrund von Einschwingphase überhaupt keine Kunden erfasst */
		if (sum<=0 && hasWarmUp) {
			if (!headingPrinted) {addHeading(2,Language.tr("Statistics.Warnings.Title")); headingPrinted=true;}
			beginParagraph();
			addLine(Language.tr("Statistics.SimulatedClients.Zero"));
			endParagraph();
		}

		/* Warnung: Einschwingphase bei externen Datenquellen nicht sinnvoll */
		if (hasWarmUp && hasExternalDataSource) {
			if (!headingPrinted) {addHeading(2,Language.tr("Statistics.Warnings.Title")); headingPrinted=true;}
			beginParagraph();
			addLine(Language.tr("Statistics.SimulatedClients.ExternalSourceAndWarmUp"));
			endParagraph();
		}

		return headingPrinted;
	}

	/**
	 * Prüfen auf auffällig viele Kunden im System oder (absolut) auffällig viele Kunden an einer Station oder von einem Kundentyp.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #LARGE_N_SYSTEM
	 * @see #LARGE_N_STATION
	 */
	private boolean buildTextLargeN() {
		boolean headingPrinted=false;

		double n;

		/* Kunden im System insgesamt */
		n=statistics.clientsInSystem.getTimeMean();
		if (n>=LARGE_N_SYSTEM) {
			if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeN")); beginParagraph(); headingPrinted=true;}
			addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeN.System"),StatisticTools.formatNumber(n)));
		}

		/* Kunden an den Stationen (absolut) */
		for (String name: statistics.clientsAtStationByStation.getNames()) {
			n=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.get(name)).getTimeMean();
			if (n>=LARGE_N_STATION) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeN")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeN.Station"),name,StatisticTools.formatNumber(n)));
			}
		}

		/* Kunden im System pro Kundentyp (absolut) */
		for (String name: statistics.clientsInSystemByClient.getNames()) {
			n=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.get(name)).getTimeMean();
			if (n>=LARGE_N_CLIENTTYPE) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeN")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeN.ClientType"),name,StatisticTools.formatNumber(n)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Prüfen auf auffällig hohe (absolute) Flussgrade an den Stationen.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #FLOW_FACTOR_SYSTEM
	 * @see #FLOW_FACTOR_STATION
	 * @see #FLOW_FACTOR_CLIENTTYPE
	 */
	private boolean buildTextFlowFactor() {
		boolean headingPrinted=false;

		double p;
		double r;

		/* Flussgrad gesamt */
		p=statistics.clientsAllProcessingTimes.getMean();
		r=statistics.clientsAllResidenceTimes.getMean();
		if (p>0 && r/p>=FLOW_FACTOR_SYSTEM) {
			if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeFlowFactor")); beginParagraph(); headingPrinted=true;}
			addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactor.System"),StatisticTools.formatNumber(r/p)));
		}

		/* Flussgrad pro Station (absolut) */
		for (String name: statistics.stationsProcessingTimes.getNames()) {
			final StatisticsDataPerformanceIndicator pIndicator=(StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(name);
			final StatisticsDataPerformanceIndicator rIndicator=(StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(name);
			if (pIndicator==null || rIndicator==null) continue;
			p=pIndicator.getMean();
			r=rIndicator.getMean();
			if (p>0 && r/p>=FLOW_FACTOR_STATION) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeFlowFactor")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactor.Station"),name,StatisticTools.formatNumber(r/p)));
			}
		}

		/* Flussgrad pro Kundentyp (absolut) */
		for (String name: statistics.clientsProcessingTimes.getNames()) {
			final StatisticsDataPerformanceIndicator pIndicator=(StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.get(name);
			final StatisticsDataPerformanceIndicator rIndicator=(StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(name);
			if (pIndicator==null || rIndicator==null) continue;
			p=pIndicator.getMean();
			r=rIndicator.getMean();
			if (p>0 && r/p>=FLOW_FACTOR_CLIENTTYPE) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeFlowFactor")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactor.ClientType"),name,StatisticTools.formatNumber(r/p)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Prüfen auf auffällig hohe (absolute) Variationskoeffizienten der Verweilzeiten.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #CVV_SYSTEM
	 * @see #CVV_STATION
	 * @see #CVV_CLIENTTYPE
	 */
	private boolean buildTextCVV() {
		boolean headingPrinted=false;

		double cv;

		/* CV[V] gesamt */
		cv=statistics.clientsAllResidenceTimes.getCV();
		if (cv>=CVV_SYSTEM) {
			if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeCVV")); beginParagraph(); headingPrinted=true;}
			addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeCVV.System"),StatisticTools.formatNumber(cv)));
		}

		/* CV[V] pro Station (absolut) */
		for (String name: statistics.stationsResidenceTimes.getNames()) {
			cv=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(name)).getCV();
			if (cv>=CVV_STATION) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeCVV")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeCVV.Station"),name,StatisticTools.formatNumber(cv)));
			}
		}

		/* CV[V] pro Kundentyp (absolut) */
		for (String name: statistics.clientsResidenceTimes.getNames()) {
			cv=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(name)).getCV();
			if (cv>=CVV_CLIENTTYPE) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeCVV")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeCVV.ClientType"),name,StatisticTools.formatNumber(cv)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Prüfen auf auffällig hohe (absolute) Auslastungen der Bedienergruppen.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #RHO
	 */
	private boolean buildTextLargeRho() {
		boolean headingPrinted=false;

		for (String name: statistics.resourceUtilization.getNames()) {
			final double rho=((StatisticsTimePerformanceIndicator)statistics.resourceUtilization.get(name)).getTimeMean();
			if (rho>=RHO) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeRho")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeRho.Group"),name,StatisticTools.formatPercent(rho)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Berechnet den (nicht gewichteten) Mittelwert über die Mittelwerte der Teil-Statistikobjekte
	 * @param indicators	Liste der Teil-Statistikobjekte
	 * @return	Mittelwert über die Mittelwerte der Teil-Statistikobjekte
	 */
	private double getMean(final StatisticsMultiPerformanceIndicator indicators) {
		int count=0;
		double sum=0;
		for (StatisticsPerformanceIndicator indicator: indicators.getAll()) {
			double value=0;
			if (indicator instanceof StatisticsTimePerformanceIndicator) value=((StatisticsTimePerformanceIndicator)indicator).getTimeMean();
			if (indicator instanceof StatisticsDataPerformanceIndicator) value=((StatisticsDataPerformanceIndicator)indicator).getMean();
			if (indicator instanceof StatisticsDataPerformanceIndicatorWithNegativeValues) value=((StatisticsDataPerformanceIndicatorWithNegativeValues)indicator).getMean();
			if (value>0) {
				count++;
				sum+=value;
			}
		}

		return (count==0)?0:(sum/count);
	}

	/**
	 * Berechnet den (nicht gewichteten) Mittelwert über die Quotienten der Mittelwerte der Teil-Statistikobjekte
	 * @param indicators1	Liste der Teil-Statistikobjekte mit den Zählern
	 * @param indicators2	Liste der Teil-Statistikobjekte mit den Nennern
	 * @return	Mittelwert über die Quotienten über die Mittelwerte der Teil-Statistikobjekte
	 */
	private double getMeanQuotient(final StatisticsMultiPerformanceIndicator indicators1, final StatisticsMultiPerformanceIndicator indicators2) {
		int count=0;
		double sum=0;

		for (String name: indicators1.getNames()) {
			final StatisticsDataPerformanceIndicator indicator1=(StatisticsDataPerformanceIndicator)indicators1.get(name);
			final StatisticsDataPerformanceIndicator indicator2=(StatisticsDataPerformanceIndicator)indicators2.get(name);
			if (indicator1==null || indicator2==null) continue;
			final double mean1=indicator1.getMean();
			final double mean2=indicator2.getMean();
			if (mean1==0.0 || mean2==0.0) continue;

			count++;
			sum+=(mean1/mean2);
		}

		return (count==0)?0:(sum/count);
	}

	/**
	 * Prüft auf auffällig viele Kunden an einer Station bzw. pro Kundentyp (relativ zu den anderen Stationen bzw. Kundentypen)
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #LARGE_N_FACTOR
	 */
	private boolean buildTextRelativeLargeN() {
		boolean headingPrinted=false;

		String[] names;

		/* Kunden an den Stationen (relativ) */
		names=statistics.clientsAtStationByStation.getNames();
		if (names.length>=3) {
			final double mean=getMean(statistics.clientsAtStationByStation);
			for (String name: names) {
				final double n=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.get(name)).getTimeMean();
				if (n>=mean*LARGE_N_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeNRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeNRelative.Station"),name,StatisticTools.formatNumber(n)));
				}
			}
		}

		/* Kunden im System pro Kundentyp (relativ) */
		names=statistics.clientsInSystemByClient.getNames();
		if (names.length>=3) {
			final double mean=getMean(statistics.clientsInSystemByClient);
			for (String name: names) {
				final double n=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.get(name)).getTimeMean();
				if (n>=mean*LARGE_N_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeNRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeNRelative.ClientType"),name,StatisticTools.formatNumber(n)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Prüft auf auffällig lange Wartezeiten an einer Station bzw. pro Kundentyp (relativ zu den anderen Stationen bzw. Kundentypen)
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #LARGE_W_FACTOR
	 */
	private boolean buildTextRelativeLongW() {
		boolean headingPrinted=false;

		String[] names;

		/* Lange Wartezeiten an den Stationen (relativ) */
		names=statistics.stationsWaitingTimes.getNames();
		if (names.length>=3) {
			final double mean=getMean(statistics.stationsWaitingTimes);
			for (String name: names) {
				final double n=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name)).getMean();
				if (n>=mean*LARGE_W_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeWRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeWRelative.Station"),name,StatisticTools.formatNumber(n)));
				}
			}
		}

		/* Lange Wartezeiten pro Kundentyp (relativ) */
		names=statistics.clientsWaitingTimes.getNames();
		if (names.length>=3) {
			final double mean=getMean(statistics.clientsWaitingTimes);
			for (String name: names) {
				final double n=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name)).getMean();
				if (n>=mean*LARGE_W_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeWRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeWRelative.ClientType"),name,StatisticTools.formatNumber(n)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Prüft auf auffällig hohe Flussgrade an einer Station bzw. pro Kundentyp (relativ zu den anderen Stationen bzw. Kundentypen)
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #FLOW_FACTOR_FACTOR
	 */
	private boolean buildTextRelativeFlowFactor() {
		boolean headingPrinted=false;

		String[] names;


		/* Flussgrad pro Station (relativ) */
		names=statistics.stationsProcessingTimes.getNames();
		if (names.length>=3) {
			final double meanQuotient=getMeanQuotient(statistics.stationsResidenceTimes,statistics.stationsProcessingTimes);
			for (String name: names) {
				final StatisticsDataPerformanceIndicator pIndicator=(StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(name);
				final StatisticsDataPerformanceIndicator rIndicator=(StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(name);
				if (pIndicator==null || rIndicator==null) continue;
				final double p=pIndicator.getMean();
				final double r=rIndicator.getMean();
				if (p>0 && r/p>=meanQuotient*FLOW_FACTOR_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative.Station"),name,StatisticTools.formatNumber(r/p)));
				}
			}
		}

		/* Flussgrad pro Kundentyp (relativ) */
		names=statistics.clientsProcessingTimes.getNames();
		if (names.length>=3) {
			final double meanQuotient=getMeanQuotient(statistics.clientsResidenceTimes,statistics.clientsProcessingTimes);
			for (String name: names) {
				final StatisticsDataPerformanceIndicator pIndicator=(StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.get(name);
				final StatisticsDataPerformanceIndicator rIndicator=(StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(name);
				if (pIndicator==null || rIndicator==null) continue;
				final double p=pIndicator.getMean();
				final double r=rIndicator.getMean();
				if (p>0 && r/p>=meanQuotient*FLOW_FACTOR_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative.ClientType"),name,StatisticTools.formatNumber(r/p)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Prüfen auf auffällig hohe Auslastungen einer Bedienergruppen (relativ zu den anderen Bedienergruppen)
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #RHO_FACTOR
	 */
	private boolean buildTextRelativeLargeRho() {
		boolean headingPrinted=false;

		final String[] names=statistics.resourceUtilization.getNames();
		if (names.length>=3) {
			final double mean=getMean(statistics.resourceUtilization);
			for (String name: names) {
				final double rho=((StatisticsTimePerformanceIndicator)statistics.resourceUtilization.get(name)).getTimeMean();
				if (rho>=mean*RHO_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeRhoRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeRhoRelative.Group"),name,StatisticTools.formatPercent(rho)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Erzeugt die Teilausgaben.
	 * @param testOnly	Wird hier <code>true</code> übergeben, so kehrt die Methode nach der ersten Ausgabe zurück.
	 * @return	Liefert <code>true</code>, wenn Ausgaben erzeugt wurden.
	 * @see #buildText()
	 * @see #test(Statistics)
	 */
	private boolean buildTextParts(final boolean testOnly) {
		boolean output=false;

		if (buildTextError()) output=true;
		if (testOnly && output) return true;

		if (buildTextWarmUpWarnings()) output=true;
		if (testOnly && output) return true;

		if (buildTextLargeN()) output=true;
		if (testOnly && output) return true;

		if (buildTextFlowFactor()) output=true;
		if (testOnly && output) return true;

		if (buildTextCVV()) output=true;
		if (testOnly && output) return true;

		if (buildTextLargeRho()) output=true;
		if (testOnly && output) return true;

		if (buildTextRelativeLargeN()) output=true;
		if (testOnly && output) return true;

		if (buildTextRelativeLongW()) output=true;
		if (testOnly && output) return true;

		if (buildTextRelativeFlowFactor()) output=true;
		if (testOnly && output) return true;

		if (buildTextRelativeLargeRho()) output=true;
		if (testOnly && output) return true;

		return output;
	}

	@Override
	protected void buildText() {
		addHeading(1,Language.tr("Statistics.ModelRemarks"));

		/* Einzelne Teilprüfungen durchführen */
		boolean output=buildTextParts(false);

		if (output) {
			/* Erklärung, was die Anmerkungen sollen */
			beginParagraph();
			addLine(1,Language.tr("Statistics.ModelRemarks.RemarkInfo"));
			endParagraph();
		} else {
			/* Keine Anmerkungen? */
			beginParagraph();
			addLine(1,Language.tr("Statistics.ModelRemarks.NoRemarks"));
			endParagraph();
		}

		/* Infotext  */
		addDescription("Remarks");
	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerOverviewText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	/**
	 * Prüft, ob für mindestens eine der angegebenen Statistikobjekte Anmerkungen vorliegen.
	 * @param statistics	Zu prüfende Statistikobjekte
	 * @return	Liefert <code>true</code>, wenn für mindestens eines der angegebenen Statistikobjekte Anmerkungen vorliegen
	 */
	public static boolean test(final Statistics[] statistics) {
		for (Statistics statistic : statistics) if (test(statistic)) return true;
		return false;
	}

	/**
	 * Prüft, ob für das angegebene Statistikobjekt Anmerkungen vorliegen.
	 * @param statistics	Zu prüfendes Statistikobjekt
	 * @return	Liefert <code>true</code>, wenn für das angegebene Statistikobjekt Anmerkungen vorliegen
	 */
	public static boolean test(final Statistics statistics) {
		final StatisticViewerRemarksText viewer=new StatisticViewerRemarksText(statistics);
		return viewer.buildTextParts(true);
	}
}
