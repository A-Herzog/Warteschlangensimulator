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
import mathtools.distribution.DataDistributionImpl;
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
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;
import ui.tools.FlatLaFHelper;

/**
 * Dieser Viewer analysiert das Modell und gibt Hinweise zu Kenngr��en,
 * die sich au�erhalb des normalerweise erwartbarem bewegen, aus. Diese
 * Kenngr��en weisen auf Probleme oder Flaschenh�lse im System hin.
 * @see StatisticViewerText
 * @author Alexander Herzog
 */
public class StatisticViewerRemarksText extends StatisticViewerText {
	/**
	 * Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	private final Statistics statistics;

	/**
	 * Warnwert f�r "Viele Kunden im System"
	 * @see #buildTextLargeN()
	 */
	private static final double LARGE_N_SYSTEM=20_000;

	/**
	 * Warnwert f�r "Viele Kunden an einer Station"
	 * @see #buildTextLargeN()
	 */
	private static final double LARGE_N_STATION=10_000;

	/**
	 * Warnwert f�r "Viele Kunden von einem Typ im System"
	 * @see #buildTextLargeN()
	 */
	private static final double LARGE_N_CLIENTTYPE=10_000;

	/**
	 * Warnwert f�r "Viele Kunden an einer Station" / "Viele Kunden von einem Typ im System"
	 * im Verh�ltnis zu den anderen Station bzw. Kundentypen
	 * @see #buildTextRelativeLargeN()
	 */
	private static final double LARGE_N_FACTOR=2;

	/**
	 * Warnwert f�r "Lange Wartezeiten an einer Station" / "Lange Wartezeiten der Kunden eines Typs"
	 * im Verh�ltnis zu den anderen Station bzw. Kundentypen
	 * @see #buildTextRelativeLongW()
	 */
	private static final double LARGE_W_FACTOR=2;

	/**
	 * Warnwert f�r "Hoher Flussgrad im System"
	 * @see #buildTextFlowFactor()
	 */
	private static final double FLOW_FACTOR_SYSTEM=20;

	/**
	 * Warnwert f�r "Hoher Flussgrad an einer Station"
	 * @see #buildTextFlowFactor()
	 */
	private static final double FLOW_FACTOR_STATION=20;

	/**
	 * Warnwert f�r "Hoher Flussgrad f�r einen Kundentyp"
	 * @see #buildTextFlowFactor()
	 */
	private static final double FLOW_FACTOR_CLIENTTYPE=20;

	/**
	 * Warnwert f�r "Hoher Flussgrad an einer Station" / "Hoher Flussgrad f�r einen Kundentyp"
	 * im Verh�ltnis zu den anderen Station bzw. Kundentypen
	 * @see #buildTextRelativeFlowFactor()
	 */
	private static final double FLOW_FACTOR_FACTOR=2;

	/**
	 * Warnwert f�r "Hoher Variationskoeffizient der Verweilzeiten im System"
	 * @see #buildTextCVV()
	 */
	private static final double CVV_SYSTEM=1.75;

	/**
	 * Warnwert f�r "Hoher Variationskoeffizient der Verweilzeiten an einer Station"
	 * @see #buildTextCVV()
	 */
	private static final double CVV_STATION=1.75;

	/**
	 * Warnwert f�r "Hoher Variationskoeffizient der Verweilzeiten f�r einen Kundentyp"
	 * @see #buildTextCVV()
	 */
	private static final double CVV_CLIENTTYPE=1.75;

	/**
	 * Warnwert f�r "Hohe Auslastung einer Bedienergruppe"
	 * @see #buildTextLargeRho()
	 */
	private static final double RHO=0.98;

	/**
	 * Warnwert f�r "Hohe Auslastung einer Bedienergruppe"
	 * im Verh�ltnis zu den anderen Bedienergruppen
	 * @see #buildTextRelativeLargeRho()
	 */
	private static final double RHO_FACTOR=2;

	/**
	 * Warnanteil f�r abgeschnittene Werte in H�ufigkeitsverteilungen
	 * @see StatisticViewerRemarksText#buildTextFrequencyDistributionClipPart()
	 */
	private static final double FREQUENCY_DIST_CLIP_PART=0.2;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerRemarksText(final Statistics statistics) {
		super();
		this.statistics=statistics;
	}

	/**
	 * Pr�fen, ob w�hrend der Simulation Fehler oder Warnungen aufgetreten sind.
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
	 * Testet, ob ein Modell �ber externe Kundendatenquellen verf�gt.
	 * @param surface	Zeichenfl�che, deren Stationen (inkl. m�glicher Untermodelle) untersucht werden sollen
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
	 * Testet, ob ein Modell �ber einen Ausgang verf�gt.
	 * @param surface	Zeichenfl�che, deren Stationen (inkl. m�glicher Untermodelle) untersucht werden sollen
	 * @return	Liefert <code>true</code>, das Modell mindestens einen Ausgang besitzt
	 */
	private boolean hasDispose(final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) if (hasDispose(((ModelElementSub)element).getSubSurface())) return true;

			if (element instanceof ModelElementDispose) return true;
		}
		return false;
	}

	/**
	 * Pr�ft, ob es Einschwingphasen-bedingte Warnungen gibt.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 */
	private boolean buildTextWarmUpWarnings() {
		boolean headingPrinted=false;

		/* Insgesamt am System eingetroffene Kunden */
		long sum=0;
		for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();

		/* Einschwingphase vorhanden? */
		final boolean hasWarmUp=(statistics.editModel.warmUpTime>0.0 || statistics.editModel.warmUpTimeTime>0);

		/* Sind externe Datenquellen angebunden? */
		final boolean hasExternalDataSource=hasExternalDataSource(statistics.editModel.surface);

		/* Warnung: Aufgrund von Einschwingphase �berhaupt keine Kunden erfasst */
		if (sum<=0 && hasWarmUp) {
			if (!headingPrinted) {
				addHeading(2,Language.tr("Statistics.Warnings.Title"));
				beginParagraph();
				headingPrinted=true;
			}
			addLine(Language.tr("Statistics.SimulatedClients.Zero"));
		}

		/* Warnung: Einschwingphase bei externen Datenquellen nicht sinnvoll */
		if (hasWarmUp && hasExternalDataSource) {
			if (!headingPrinted) {
				addHeading(2,Language.tr("Statistics.Warnings.Title"));
				beginParagraph();
				headingPrinted=true;
			}
			addLine(Language.tr("Statistics.SimulatedClients.ExternalSourceAndWarmUp"));
		}

		/* Geschlossenes Netzwerk */
		final boolean hasDispose=hasDispose(statistics.editModel.surface);

		/* Warnung: Einschwingphase bei geschlossenen Netzen nicht sinnvoll */
		if (hasWarmUp && !hasDispose) {
			if (!headingPrinted) {
				addHeading(2,Language.tr("Statistics.Warnings.Title"));
				beginParagraph();
				headingPrinted=true;
			}
			addLine(Language.tr("Statistics.SimulatedClients.ClosedNetworkAndWarmUp"));
		}

		if (headingPrinted) endParagraph();

		return headingPrinted;
	}

	/**
	 * Pr�fen auf auff�llig viele Kunden im System oder (absolut) auff�llig viele Kunden an einer Station oder von einem Kundentyp.
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
			addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeN.System"),StatisticTools.formatNumberExt(n,true)));
		}

		/* Kunden an den Stationen (absolut) */
		for (String name: statistics.clientsAtStationByStation.getNames()) {
			n=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.get(name)).getTimeMean();
			if (n>=LARGE_N_STATION) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeN")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeN.Station"),name,StatisticTools.formatNumberExt(n,true)));
			}
		}

		/* Kunden im System pro Kundentyp (absolut) */
		for (String name: statistics.clientsInSystemByClient.getNames()) {
			n=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.get(name)).getTimeMean();
			if (n>=LARGE_N_CLIENTTYPE) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeN")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeN.ClientType"),name,StatisticTools.formatNumberExt(n,true)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Pr�fen auf auff�llig hohe (absolute) Flussgrade an den Stationen.
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
			addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactor.System"),StatisticTools.formatNumberExt(r/p,false)));
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
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactor.Station"),name,StatisticTools.formatNumberExt(r/p,false)));
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
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactor.ClientType"),name,StatisticTools.formatNumberExt(r/p,false)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Pr�fen auf auff�llig hohe (absolute) Variationskoeffizienten der Verweilzeiten.
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
			addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeCVV.System"),StatisticTools.formatNumberExt(cv,true)));
		}

		/* CV[V] pro Station (absolut) */
		for (String name: statistics.stationsResidenceTimes.getNames()) {
			cv=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(name)).getCV();
			if (cv>=CVV_STATION) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeCVV")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeCVV.Station"),name,StatisticTools.formatNumberExt(cv,true)));
			}
		}

		/* CV[V] pro Kundentyp (absolut) */
		for (String name: statistics.clientsResidenceTimes.getNames()) {
			cv=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(name)).getCV();
			if (cv>=CVV_CLIENTTYPE) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeCVV")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeCVV.ClientType"),name,StatisticTools.formatNumberExt(cv,true)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Pr�fen auf auff�llig hohe (absolute) Auslastungen der Bedienergruppen.
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #RHO
	 */
	private boolean buildTextLargeRho() {
		boolean headingPrinted=false;

		for (String name: statistics.resourceUtilization.getNames()) {
			final StatisticsTimePerformanceIndicator i1=((StatisticsTimePerformanceIndicator)statistics.resourceCount.get(name));
			final StatisticsTimePerformanceIndicator i2=((StatisticsTimePerformanceIndicator)statistics.resourceUtilization.get(name));
			if (i1==null || i2==null) continue;
			final double count=i1.getTimeMean();
			final double load=i2.getTimeMean();
			final double rho=load/count;
			if (rho>=RHO) {
				if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeRho")); beginParagraph(); headingPrinted=true;}
				addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeRho.Group"),name,StatisticTools.formatPercentExt(rho,true)));
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Berechnet den (nicht gewichteten) Mittelwert �ber die Mittelwerte der Teil-Statistikobjekte
	 * @param indicators	Liste der Teil-Statistikobjekte
	 * @return	Mittelwert �ber die Mittelwerte der Teil-Statistikobjekte
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
	 * Berechnet den (nicht gewichteten) Mittelwert �ber die Quotienten der Mittelwerte der Teil-Statistikobjekte
	 * @param indicators1	Liste der Teil-Statistikobjekte mit den Z�hlern
	 * @param indicators2	Liste der Teil-Statistikobjekte mit den Nennern
	 * @return	Mittelwert �ber die Quotienten �ber die Mittelwerte der Teil-Statistikobjekte
	 */
	private double getMeanQuotient(final StatisticsMultiPerformanceIndicator indicators1, final StatisticsMultiPerformanceIndicator indicators2) {
		int count=0;
		double sum=0;

		for (String name: indicators1.getNames()) {
			final StatisticsPerformanceIndicator i1=indicators1.get(name);
			final StatisticsPerformanceIndicator i2=indicators2.get(name);
			if (i1==null || i2==null) continue;

			if ((i1 instanceof StatisticsDataPerformanceIndicator) && (i2 instanceof StatisticsDataPerformanceIndicator)) {
				final StatisticsDataPerformanceIndicator indicator1=(StatisticsDataPerformanceIndicator)i1;
				final StatisticsDataPerformanceIndicator indicator2=(StatisticsDataPerformanceIndicator)i2;
				final double mean1=indicator1.getMean();
				final double mean2=indicator2.getMean();
				if (mean1==0.0 || mean2==0.0) continue;
				count++;
				sum+=(mean1/mean2);
			}

			if ((i1 instanceof StatisticsTimePerformanceIndicator) && (i2 instanceof StatisticsTimePerformanceIndicator)) {
				final StatisticsTimePerformanceIndicator indicator1=(StatisticsTimePerformanceIndicator)i1;
				final StatisticsTimePerformanceIndicator indicator2=(StatisticsTimePerformanceIndicator)i2;
				final double mean1=indicator1.getTimeMean();
				final double mean2=indicator2.getTimeMean();
				if (mean1==0.0 || mean2==0.0) continue;
				count++;
				sum+=(mean1/mean2);
			}
		}

		return (count==0)?0:(sum/count);
	}

	/**
	 * Pr�ft auf auff�llig viele Kunden an einer Station bzw. pro Kundentyp (relativ zu den anderen Stationen bzw. Kundentypen)
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
			if (mean>0) for (String name: names) {
				final double n=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.get(name)).getTimeMean();
				if (n>=mean*LARGE_N_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeNRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeNRelative.Station"),name,StatisticTools.formatNumberExt(n,true)));
				}
			}
		}

		/* Kunden im System pro Kundentyp (relativ) */
		names=statistics.clientsInSystemByClient.getNames();
		if (names.length>=3) {
			final double mean=getMean(statistics.clientsInSystemByClient);
			if (mean>0) for (String name: names) {
				final double n=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.get(name)).getTimeMean();
				if (n>=mean*LARGE_N_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeNRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeNRelative.ClientType"),name,StatisticTools.formatNumberExt(n,true)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Pr�ft auf auff�llig lange Wartezeiten an einer Station bzw. pro Kundentyp (relativ zu den anderen Stationen bzw. Kundentypen)
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
			if (mean>0) for (String name: names) {
				final double n=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.get(name)).getMean();
				if (n>=mean*LARGE_W_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeWRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeWRelative.Station"),name,StatisticTools.formatNumberExt(n,true)));
				}
			}
		}

		/* Lange Wartezeiten pro Kundentyp (relativ) */
		names=statistics.clientsWaitingTimes.getNames();
		if (names.length>=3) {
			final double mean=getMean(statistics.clientsWaitingTimes);
			if (mean>0) for (String name: names) {
				final double n=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.get(name)).getMean();
				if (n>=mean*LARGE_W_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeWRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeWRelative.ClientType"),name,StatisticTools.formatNumberExt(n,true)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Pr�ft auf auff�llig hohe Flussgrade an einer Station bzw. pro Kundentyp (relativ zu den anderen Stationen bzw. Kundentypen)
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
			if (meanQuotient>1) for (String name: names) {
				final StatisticsDataPerformanceIndicator pIndicator=(StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.get(name);
				final StatisticsDataPerformanceIndicator rIndicator=(StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.get(name);
				if (pIndicator==null || rIndicator==null) continue;
				final double p=pIndicator.getMean();
				final double r=rIndicator.getMean();
				if (p>0 && r/p>=meanQuotient*FLOW_FACTOR_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative.Station"),name,StatisticTools.formatNumberExt(r/p,false)));
				}
			}
		}

		/* Flussgrad pro Kundentyp (relativ) */
		names=statistics.clientsProcessingTimes.getNames();
		if (names.length>=3) {
			final double meanQuotient=getMeanQuotient(statistics.clientsResidenceTimes,statistics.clientsProcessingTimes);
			if (meanQuotient>1) for (String name: names) {
				final StatisticsDataPerformanceIndicator pIndicator=(StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.get(name);
				final StatisticsDataPerformanceIndicator rIndicator=(StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.get(name);
				if (pIndicator==null || rIndicator==null) continue;
				final double p=pIndicator.getMean();
				final double r=rIndicator.getMean();
				if (p>0 && r/p>=meanQuotient*FLOW_FACTOR_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeFlowFactorRelative.ClientType"),name,StatisticTools.formatNumberExt(r/p,false)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Pr�fen auf auff�llig hohe Auslastungen einer Bedienergruppen (relativ zu den anderen Bedienergruppen)
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #RHO_FACTOR
	 */
	private boolean buildTextRelativeLargeRho() {
		boolean headingPrinted=false;

		final String[] names=statistics.resourceUtilization.getNames();
		if (names.length>=3) {
			final double mean=getMeanQuotient(statistics.resourceUtilization,statistics.resourceCount); /* Reihenfolge der Argumente ist so richtig */
			if (mean>0) for (String name: names) {
				final StatisticsTimePerformanceIndicator i1=((StatisticsTimePerformanceIndicator)statistics.resourceCount.get(name));
				final StatisticsTimePerformanceIndicator i2=((StatisticsTimePerformanceIndicator)statistics.resourceUtilization.get(name));
				if (i1==null || i2==null) continue;
				final double count=i1.getTimeMean();
				final double load=i2.getTimeMean();
				final double rho=load/count;
				if (rho>=mean*RHO_FACTOR) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.LargeRhoRelative")); beginParagraph(); headingPrinted=true;}
					addLine(String.format(Language.tr("Statistics.ModelRemarks.LargeRhoRelative.Group"),name,StatisticTools.formatPercentExt(rho,true)));
				}
			}
		}

		if (headingPrinted) endParagraph();
		return headingPrinted;
	}

	/**
	 * Pr�ft, ob ein Anteil von {@value #FREQUENCY_DIST_CLIP_PART} oder mehr der Werte
	 * im obersten Bereich der Verteilung liegen.
	 * @param count	Gesamtanzahl an Werten
	 * @param dist	Verteilung
	 * @return	Liefert den Anteil der Werte oder -1, wenn der Grenzwert nicht �berschritten wurde
	 */
	private double clippedValues(final long count, final DataDistributionImpl dist) {
		if (count==0 || dist==null || dist.densityData==null || dist.densityData.length<2) return -1;
		final double max=dist.densityData[dist.densityData.length-1];

		if (max>=count*FREQUENCY_DIST_CLIP_PART) return max/count;
		return -1;
	}

	/**
	 * Pr�fen auf viele abgeschnittene Werte in H�ufigkeitsverteilungen
	 * @return	Liefert <code>true</code>, wenn die Methode Ausgaben erzeugt hat
	 * @see #FREQUENCY_DIST_CLIP_PART
	 */
	private boolean buildTextFrequencyDistributionClipPart() {
		boolean headingPrinted=false;

		for (StatisticsPerformanceIndicator indicator1: statistics.getAllPerformanceIndicators()) {
			if (indicator1 instanceof StatisticsMultiPerformanceIndicator && indicator1.xmlNodeNames!=null && indicator1.xmlNodeNames.length>0) for (StatisticsPerformanceIndicator indicator2 : ((StatisticsMultiPerformanceIndicator)indicator1).getAll()) {
				if (indicator2 instanceof StatisticsDataPerformanceIndicator) {
					final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)indicator2;
					final double p=clippedValues(indicator.getCount(),indicator.getDistribution());
					if (p>0 && indicator.xmlNodeNames!=null && indicator.xmlNodeNames.length>0) {
						if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.TruncatedValues")); beginParagraph(); headingPrinted=true;}
						final String name=indicator1.xmlNodeNames[0]+"->"+indicator.xmlNodeNames[0]+"["+Language.trAll("Statistics.XML.Type")[0]+"="+((StatisticsMultiPerformanceIndicator)indicator1).getName(indicator)+"]";
						addLine(String.format(Language.tr("Statistics.ModelRemarks.TruncatedValues.DistributionInfo"),name,StatisticTools.formatPercentExt(p,false)));
					}
				}
				if (indicator2 instanceof StatisticsDataPerformanceIndicatorWithNegativeValues) {
					final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)indicator2;
					final double p=clippedValues(indicator.getCount(),indicator.getDistribution());
					if (p>0 && indicator.xmlNodeNames!=null && indicator.xmlNodeNames.length>0) {
						if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.TruncatedValues")); beginParagraph(); headingPrinted=true;}
						final String name=indicator1.xmlNodeNames[0]+"->"+indicator.xmlNodeNames[0]+"["+Language.trAll("Statistics.XML.Type")[0]+"="+((StatisticsMultiPerformanceIndicator)indicator1).getName(indicator)+"]";
						addLine(String.format(Language.tr("Statistics.ModelRemarks.TruncatedValues.DistributionInfo"),name,StatisticTools.formatPercentExt(p,false)));
					}
				}
			}
			if (indicator1 instanceof StatisticsDataPerformanceIndicator) {
				final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)indicator1;
				final double p=clippedValues(indicator.getCount(),indicator.getDistribution());
				if (p>0 && indicator.xmlNodeNames!=null && indicator.xmlNodeNames.length>0) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.TruncatedValues")); beginParagraph(); headingPrinted=true;}
					final String name=indicator.xmlNodeNames[0];
					addLine(String.format(Language.tr("Statistics.ModelRemarks.TruncatedValues.DistributionInfo"),name,StatisticTools.formatPercentExt(p,false)));
				}
			}
			if (indicator1 instanceof StatisticsDataPerformanceIndicatorWithNegativeValues) {
				final StatisticsDataPerformanceIndicatorWithNegativeValues indicator=(StatisticsDataPerformanceIndicatorWithNegativeValues)indicator1;
				final double p=clippedValues(indicator.getCount(),indicator.getDistribution());
				if (p>0 && indicator.xmlNodeNames!=null && indicator.xmlNodeNames.length>0) {
					if (!headingPrinted) {addHeading(2,Language.tr("Statistics.ModelRemarks.TruncatedValues")); beginParagraph(); headingPrinted=true;}
					final String name=indicator.xmlNodeNames[0];
					addLine(String.format(Language.tr("Statistics.ModelRemarks.TruncatedValues.DistributionInfo"),name,StatisticTools.formatPercentExt(p,false)));
				}
			}
		}

		if (headingPrinted) {
			endParagraph();
			beginParagraph();
			addLine(String.format(Language.tr("Statistics.ModelRemarks.TruncatedValues.Info1"),StatisticTools.formatPercent(FREQUENCY_DIST_CLIP_PART)));
			addLine(Language.tr("Statistics.ModelRemarks.TruncatedValues.Info2"));
			endParagraph();
		}
		return headingPrinted;
	}

	/**
	 * Erzeugt die Teilausgaben.
	 * @param testOnly	Wird hier <code>true</code> �bergeben, so kehrt die Methode nach der ersten Ausgabe zur�ck.
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

		if (buildTextFrequencyDistributionClipPart()) output=true;
		if (testOnly && output) return true;

		return output;
	}

	@Override
	protected void buildText() {
		addHeading(1,Language.tr("Statistics.ModelRemarks"));

		/* Einzelne Teilpr�fungen durchf�hren */
		boolean output=buildTextParts(false);

		if (output) {
			/* Erkl�rung, was die Anmerkungen sollen */
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
	 * Zeigt im Fu�bereich der Hilfeseite eine "Erkl�rung einblenden"-Schaltfl�che, die,
	 * wenn sie angeklickt wird, eine html-Hilfeseite anzeigt.
	 * @param topic	Hilfe-Thema (wird als Datei in den "description_*"-Ordern gesucht)
	 */
	private void addDescription(final String topic) {
		final URL url=StatisticViewerOverviewText.class.getResource("description_"+Language.getCurrentLanguage()+"/"+topic+".html");
		addDescription(url,helpTopic->Help.topic(getViewer(false),helpTopic));
	}

	@Override
	protected String getDescriptionCustomStyles() {
		if (FlatLaFHelper.isDark()) return StatisticsPanel.DARK_MODE_DESACRIPTION_STYLE;
		return null;
	}

	/**
	 * Pr�ft, ob f�r mindestens eine der angegebenen Statistikobjekte Anmerkungen vorliegen.
	 * @param statistics	Zu pr�fende Statistikobjekte
	 * @return	Liefert <code>true</code>, wenn f�r mindestens eines der angegebenen Statistikobjekte Anmerkungen vorliegen
	 */
	public static boolean test(final Statistics[] statistics) {
		for (Statistics statistic : statistics) if (test(statistic)) return true;
		return false;
	}

	/**
	 * Pr�ft, ob f�r das angegebene Statistikobjekt Anmerkungen vorliegen.
	 * @param statistics	Zu pr�fendes Statistikobjekt
	 * @return	Liefert <code>true</code>, wenn f�r das angegebene Statistikobjekt Anmerkungen vorliegen
	 */
	public static boolean test(final Statistics statistics) {
		final StatisticViewerRemarksText viewer=new StatisticViewerRemarksText(statistics);
		return viewer.buildTextParts(true);
	}
}
