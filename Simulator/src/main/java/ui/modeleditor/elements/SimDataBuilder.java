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
package ui.modeleditor.elements;

import language.Language;
import mathtools.NumberTools;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import ui.modeleditor.coreelements.ModelElement;
import ui.statistics.StatisticTools;
import ui.statistics.StatisticViewerOverviewText;

/**
 * Diese Klasse stellt zur Laufzeit während der Animation Statistikdaten zu einer Station zusammen
 * @author Alexander Herzog
 * @see ModelElement#getAnimationRunTimeStatisticsData(SimulationData)
 */
public class SimDataBuilder {
	/**
	 * Simulationsdaten-Objekt<br>
	 * (Wird vom Konstruktor gesetzt, kann danach nicht mehr verändert werden.)
	 */
	public final SimulationData simData;

	/**
	 * ID der Station<br>
	 * (Wird vom Konstruktor gesetzt, kann danach nicht mehr verändert werden.)
	 */
	public final int id;

	/**
	 * Zu der Station gehöriges Laufzeitdaten-Objekt<br>
	 * (Im Fehlerfall <code>null</code>.)
	 */
	public final RunElementData data;

	/**
	 * Generelles Statistik-Objekt
	 */
	public final Statistics statistics;

	/**
	 * Zusammenstellung der Statistikdaten zu der Station
	 * oder im Fehlerfall <code>null</code>
	 */
	public final StringBuilder results;

	/**
	 * Konstruktor der Klasse <code>SimDataBuilder</code>
	 * @param simData	Simulationsdaten-Objekt (im dem die Stations- und die Statistikdaten enthalten sind)
	 * @param id	ID der Station, für die die Daten zusammengestellt werden sollen
	 */
	public SimDataBuilder(final SimulationData simData, final int id) {
		this.simData=simData;
		this.id=id;
		statistics=simData.statistics;

		final RunElement element=simData.runModel.elements.get(id);
		if (element==null) {
			data=null;
			results=null;
			return;
		}

		data=element.getData(simData);
		results=new StringBuilder();

		results.append(element.name+"\n\n");
		results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.Clients"),NumberTools.formatLong(data.clients))+"\n");
		results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStation"),NumberTools.formatLong(data.reportedClientsAtStation(simData)))+"\n");
		results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStationQueue"),NumberTools.formatLong(data.clientsAtStationQueue))+"\n\n");
		results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStationProcess"),NumberTools.formatLong(data.clientsAtStationProcess))+"\n\n");

		if (data.lastArrival>0) {
			results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastArrival"),simData.formatScaledSimTime(data.lastArrival))+"\n");
		}
		if (data.lastArrivalByClientType!=null) for (int i=0;i<data.lastArrivalByClientType.length;i++) if (data.lastArrivalByClientType[i]>0) {
			final String clientType=simData.runModel.clientTypes[i];
			results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastArrivalByClientTypeType"),clientType,simData.formatScaledSimTime(data.lastArrivalByClientType[i]))+"\n");
		}

		if (data.lastLeave>0) {
			results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastLeave"),simData.formatScaledSimTime(data.lastLeave))+"\n");
		}
		if (data.lastLeaveByClientType!=null) for (int i=0;i<data.lastLeaveByClientType.length;i++) if (data.lastLeaveByClientType[i]>0) {
			final String clientType=simData.runModel.clientTypes[i];
			results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastLeaveByClientType"),clientType,simData.formatScaledSimTime(data.lastLeaveByClientType[i]))+"\n");
		}

		boolean throughputHeadingPrinted=false;
		final double time=simData.statistics.clientsInSystem.getSum();
		if (time>0.0) {
			long sum=0;
			for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])simData.statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();
			throughputHeadingPrinted=true;
			results.append("\n"+Language.tr("Statistics.Throughput")+":\n");
			results.append(Language.tr("Statistics.Throughput.Average")+": "+StatisticViewerOverviewText.getMaxThroughputText(sum/time)+"\n");
		}
		if (data.maxThroughputIntervalLength>0) {
			if (!throughputHeadingPrinted) results.append("\n"+Language.tr("Statistics.Throughput")+":\n");
			results.append(Language.tr("Statistics.Throughput.Maximum")+": "+StatisticViewerOverviewText.getMaxThroughputText(data.maxThroughput/(data.maxThroughputIntervalLength*simData.runModel.scaleToSeconds))+" ("+String.format(Language.tr("Statistics.Throughput.Maximum.IntervalLength"),NumberTools.formatLong(Math.round(data.maxThroughputIntervalLength*simData.runModel.scaleToSeconds)))+")\n");
		}

		if (data.statisticWaiting!=null && data.statisticWaiting.getMean()>0) {
			results.append("\n"+Language.tr("Statistics.WaitingTimes")+":\n");
			results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticWaiting.getCount())+"\n");
			results.append(Language.tr("Statistics.AverageWaitingTime")+" E[W]="+StatisticTools.formatExactTime(data.statisticWaiting.getMean())+" ("+StatisticTools.formatNumberExt(data.statisticWaiting.getMean(),false)+")\n");
			results.append(Language.tr("Statistics.StdDevWaitingTime")+" Std[W]="+StatisticTools.formatExactTime(data.statisticWaiting.getSD())+" ("+StatisticTools.formatNumberExt(data.statisticWaiting.getSD(),false)+")\n");
			results.append(Language.tr("Statistics.VarianceWaitingTime")+" Var[W]="+StatisticTools.formatExactTime(data.statisticWaiting.getVar())+" ("+StatisticTools.formatNumberExt(data.statisticWaiting.getVar(),false)+")\n");
			results.append(Language.tr("Statistics.CVWaitingTime")+" CV[W]"+StatisticTools.formatNumberExt(data.statisticWaiting.getCV(),true)+"\n");
			results.append(Language.tr("Statistics.Skewness")+": Sk[W]"+StatisticTools.formatNumberExt(data.statisticWaiting.getSk(),true)+"\n");
			results.append(Language.tr("Statistics.Kurt")+": Kurt[W]"+StatisticTools.formatNumberExt(data.statisticWaiting.getKurt(),true)+"\n");
			results.append(Language.tr("Statistics.MinimumWaitingTime")+" Min[W]="+StatisticTools.formatExactTime(data.statisticWaiting.getMin())+" ("+StatisticTools.formatNumberExt(data.statisticWaiting.getMin(),false)+")\n");
			results.append(Language.tr("Statistics.MaximumWaitingTime")+" Max[W]="+StatisticTools.formatExactTime(data.statisticWaiting.getMax())+" ("+StatisticTools.formatNumberExt(data.statisticWaiting.getMax(),false)+")\n");
		}

		if (data.statisticWaitingByClientType!=null && data.statisticWaitingByClientType.length>1) for (int i=0;i<data.statisticWaitingByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticWaiting=data.statisticWaitingByClientType[i];
			if (statisticWaiting!=null && statisticWaiting.getMean()>0) {
				results.append("\n"+String.format(Language.tr("Statistics.WaitingTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticWaiting.getCount())+"\n");
				results.append(Language.tr("Statistics.AverageWaitingTime")+" E[W]="+StatisticTools.formatExactTime(statisticWaiting.getMean())+" ("+StatisticTools.formatNumberExt(statisticWaiting.getMean(),false)+")\n");
				results.append(Language.tr("Statistics.StdDevWaitingTime")+" Std[W]="+StatisticTools.formatExactTime(statisticWaiting.getSD())+" ("+StatisticTools.formatNumberExt(statisticWaiting.getSD(),false)+")\n");
				results.append(Language.tr("Statistics.VarianceWaitingTime")+" Var[W]="+StatisticTools.formatExactTime(statisticWaiting.getVar())+" ("+StatisticTools.formatNumberExt(statisticWaiting.getVar(),false)+")\n");
				results.append(Language.tr("Statistics.CVWaitingTime")+" CV[W]"+StatisticTools.formatNumberExt(statisticWaiting.getCV(),true)+"\n");
				results.append(Language.tr("Statistics.Skewness")+": Sk[W]"+StatisticTools.formatNumberExt(statisticWaiting.getSk(),true)+"\n");
				results.append(Language.tr("Statistics.Kurt")+": Kurt[W]"+StatisticTools.formatNumberExt(statisticWaiting.getKurt(),true)+"\n");
				results.append(Language.tr("Statistics.MinimumWaitingTime")+" Min[W]="+StatisticTools.formatExactTime(statisticWaiting.getMin())+" ("+StatisticTools.formatNumberExt(statisticWaiting.getMin(),false)+")\n");
				results.append(Language.tr("Statistics.MaximumWaitingTime")+" Max[W]="+StatisticTools.formatExactTime(statisticWaiting.getMax())+" ("+StatisticTools.formatNumberExt(statisticWaiting.getMax(),false)+")\n");
			}
		}

		if (data.statisticTransfer!=null && data.statisticTransfer.getMean()>0) {
			results.append("\n"+Language.tr("Statistics.TransferTimes")+":\n");
			results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticTransfer.getCount())+"\n");
			results.append(Language.tr("Statistics.AverageTransferTime")+" E[T]="+StatisticTools.formatExactTime(data.statisticTransfer.getMean())+" ("+StatisticTools.formatNumberExt(data.statisticTransfer.getMean(),false)+")\n");
			results.append(Language.tr("Statistics.StdDevTransferTime")+" Std[T]="+StatisticTools.formatExactTime(data.statisticTransfer.getSD())+" ("+StatisticTools.formatNumberExt(data.statisticTransfer.getSD(),false)+")\n");
			results.append(Language.tr("Statistics.VarianceTransferTime")+" Var[T]="+StatisticTools.formatExactTime(data.statisticTransfer.getVar())+" ("+StatisticTools.formatNumberExt(data.statisticTransfer.getVar(),false)+")\n");
			results.append(Language.tr("Statistics.CVTransferTime")+" CV[T]"+StatisticTools.formatNumberExt(data.statisticTransfer.getCV(),true)+"\n");
			results.append(Language.tr("Statistics.Skewness")+": Sk[T]"+StatisticTools.formatNumberExt(data.statisticTransfer.getSk(),true)+"\n");
			results.append(Language.tr("Statistics.Kurt")+": Kurt[T]"+StatisticTools.formatNumberExt(data.statisticTransfer.getKurt(),true)+"\n");
			results.append(Language.tr("Statistics.MinimumTransferTime")+" Min[T]="+StatisticTools.formatExactTime(data.statisticTransfer.getMin())+" ("+StatisticTools.formatNumberExt(data.statisticTransfer.getMin(),false)+")\n");
			results.append(Language.tr("Statistics.MaximumTransferTime")+" Max[T]="+StatisticTools.formatExactTime(data.statisticTransfer.getMax())+" ("+StatisticTools.formatNumberExt(data.statisticTransfer.getMax(),false)+")\n");
		}

		if (data.statisticTransferByClientType!=null && data.statisticTransferByClientType.length>1) for (int i=0;i<data.statisticTransferByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticTransfer=data.statisticTransferByClientType[i];
			if (statisticTransfer!=null && statisticTransfer.getMean()>0) {
				results.append("\n"+String.format(Language.tr("Statistics.TransferTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticTransfer.getCount())+"\n");
				results.append(Language.tr("Statistics.AverageTransferTime")+" E[T]="+StatisticTools.formatExactTime(statisticTransfer.getMean())+" ("+StatisticTools.formatNumberExt(statisticTransfer.getMean(),false)+")\n");
				results.append(Language.tr("Statistics.StdDevTransferTime")+" Std[T]="+StatisticTools.formatExactTime(statisticTransfer.getSD())+" ("+StatisticTools.formatNumberExt(statisticTransfer.getSD(),false)+")\n");
				results.append(Language.tr("Statistics.VarianceTransferTime")+" Var[T]="+StatisticTools.formatExactTime(statisticTransfer.getVar())+" ("+StatisticTools.formatNumberExt(statisticTransfer.getVar(),false)+")\n");
				results.append(Language.tr("Statistics.CVTransferTime")+" CV[T]"+StatisticTools.formatNumberExt(statisticTransfer.getCV(),true)+"\n");
				results.append(Language.tr("Statistics.Skewness")+": Sk[T]"+StatisticTools.formatNumberExt(statisticTransfer.getSk(),true)+"\n");
				results.append(Language.tr("Statistics.Kurt")+": Kurt[T]"+StatisticTools.formatNumberExt(statisticTransfer.getKurt(),true)+"\n");
				results.append(Language.tr("Statistics.MinimumTransferTime")+" Min[T]="+StatisticTools.formatExactTime(statisticTransfer.getMin())+" ("+StatisticTools.formatNumberExt(statisticTransfer.getMin(),false)+")\n");
				results.append(Language.tr("Statistics.MaximumTransferTime")+" Max[T]="+StatisticTools.formatExactTime(statisticTransfer.getMax())+" ("+StatisticTools.formatNumberExt(statisticTransfer.getMax(),false)+")\n");
			}
		}

		if (data.statisticProcess!=null && data.statisticProcess.getMean()>0) {
			results.append("\n"+Language.tr("Statistics.ProcessTimes")+":\n");
			results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticProcess.getCount())+"\n");
			results.append(Language.tr("Statistics.AverageProcessTime")+" E[S]="+StatisticTools.formatExactTime(data.statisticProcess.getMean())+" ("+StatisticTools.formatNumberExt(data.statisticProcess.getMean(),false)+")\n");
			results.append(Language.tr("Statistics.StdDevProcessTime")+" Std[S]="+StatisticTools.formatExactTime(data.statisticProcess.getSD())+" ("+StatisticTools.formatNumberExt(data.statisticProcess.getSD(),false)+")\n");
			results.append(Language.tr("Statistics.VarianceProcessTime")+" Var[S]="+StatisticTools.formatExactTime(data.statisticProcess.getVar())+" ("+StatisticTools.formatNumberExt(data.statisticProcess.getVar(),false)+")\n");
			results.append(Language.tr("Statistics.CVProcessTime")+" CV[S]"+StatisticTools.formatNumberExt(data.statisticProcess.getCV(),true)+"\n");
			results.append(Language.tr("Statistics.Skewness")+": Sk[S]"+StatisticTools.formatNumberExt(data.statisticProcess.getSk(),true)+"\n");
			results.append(Language.tr("Statistics.Kurt")+": Kurt[S]"+StatisticTools.formatNumberExt(data.statisticProcess.getKurt(),true)+"\n");
			results.append(Language.tr("Statistics.MinimumProcessTime")+" Min[S]="+StatisticTools.formatExactTime(data.statisticProcess.getMin())+" ("+StatisticTools.formatNumberExt(data.statisticProcess.getMin(),false)+")\n");
			results.append(Language.tr("Statistics.MaximumProcessTime")+" Max[S]="+StatisticTools.formatExactTime(data.statisticProcess.getMax())+" ("+StatisticTools.formatNumberExt(data.statisticProcess.getMax(),false)+")\n");
		}

		if (data.statisticProcessByClientType!=null && data.statisticProcessByClientType.length>1) for (int i=0;i<data.statisticProcessByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticProcess=data.statisticProcessByClientType[i];
			if (statisticProcess!=null && statisticProcess.getMean()>0) {
				results.append("\n"+String.format(Language.tr("Statistics.ProcessTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticProcess.getCount())+"\n");
				results.append(Language.tr("Statistics.AverageProcessTime")+" E[S]="+StatisticTools.formatExactTime(statisticProcess.getMean())+" ("+StatisticTools.formatNumberExt(statisticProcess.getMean(),false)+")\n");
				results.append(Language.tr("Statistics.StdDevProcessTime")+" Std[S]="+StatisticTools.formatExactTime(statisticProcess.getSD())+" ("+StatisticTools.formatNumberExt(statisticProcess.getSD(),false)+")\n");
				results.append(Language.tr("Statistics.VarianceProcessTime")+" Var[S]="+StatisticTools.formatExactTime(statisticProcess.getVar())+" ("+StatisticTools.formatNumberExt(statisticProcess.getVar(),false)+")\n");
				results.append(Language.tr("Statistics.CVProcessTime")+" CV[S]"+StatisticTools.formatNumberExt(statisticProcess.getCV(),true)+"\n");
				results.append(Language.tr("Statistics.Skewness")+": Sk[S]"+StatisticTools.formatNumberExt(statisticProcess.getSk(),true)+"\n");
				results.append(Language.tr("Statistics.Kurt")+": Kurt[S]"+StatisticTools.formatNumberExt(statisticProcess.getKurt(),true)+"\n");
				results.append(Language.tr("Statistics.MinimumProcessTime")+" Min[S]="+StatisticTools.formatExactTime(statisticProcess.getMin())+" ("+StatisticTools.formatNumberExt(statisticProcess.getMin(),false)+")\n");
				results.append(Language.tr("Statistics.MaximumProcessTime")+" Max[S]="+StatisticTools.formatExactTime(statisticProcess.getMax())+" ("+StatisticTools.formatNumberExt(statisticProcess.getMax(),false)+")\n");
			}
		}

		if (data.statisticResidence!=null && data.statisticResidence.getMean()>0) {
			results.append("\n"+Language.tr("Statistics.ResidenceTimes")+":\n");
			results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticResidence.getCount())+"\n");
			results.append(Language.tr("Statistics.AverageResidenceTime")+" E[V]="+StatisticTools.formatExactTime(data.statisticResidence.getMean())+" ("+StatisticTools.formatNumberExt(data.statisticResidence.getMean(),false)+")\n");
			results.append(Language.tr("Statistics.StdDevResidenceTime")+" Std[V]="+StatisticTools.formatExactTime(data.statisticResidence.getSD())+" ("+StatisticTools.formatNumberExt(data.statisticResidence.getSD(),false)+")\n");
			results.append(Language.tr("Statistics.VarianceResidenceTime")+" Var[V]="+StatisticTools.formatExactTime(data.statisticResidence.getVar())+" ("+StatisticTools.formatNumberExt(data.statisticResidence.getVar(),false)+")\n");
			results.append(Language.tr("Statistics.CVResidenceTime")+" CV[V]"+StatisticTools.formatNumberExt(data.statisticResidence.getCV(),true)+"\n");
			results.append(Language.tr("Statistics.Skewness")+": Sk[V]"+StatisticTools.formatNumberExt(data.statisticResidence.getSk(),true)+"\n");
			results.append(Language.tr("Statistics.Kurt")+": Kurt[V]"+StatisticTools.formatNumberExt(data.statisticResidence.getKurt(),true)+"\n");
			results.append(Language.tr("Statistics.MinimumResidenceTime")+" Min[V]="+StatisticTools.formatExactTime(data.statisticResidence.getMin())+" ("+StatisticTools.formatNumberExt(data.statisticResidence.getMin(),false)+")\n");
			results.append(Language.tr("Statistics.MaximumResidenceTime")+" Max[V]="+StatisticTools.formatExactTime(data.statisticResidence.getMax())+" ("+StatisticTools.formatNumberExt(data.statisticResidence.getMax(),false)+")\n");
		}

		if (data.statisticResidenceByClientType!=null && data.statisticResidenceByClientType.length>1) for (int i=0;i<data.statisticResidenceByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticResidence=data.statisticResidenceByClientType[i];
			if (statisticResidence!=null && statisticResidence.getMean()>0) {
				results.append("\n"+String.format(Language.tr("Statistics.ResidenceTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				results.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticResidence.getCount())+"\n");
				results.append(Language.tr("Statistics.AverageResidenceTime")+" E[V]="+StatisticTools.formatExactTime(statisticResidence.getMean())+" ("+StatisticTools.formatNumberExt(statisticResidence.getMean(),false)+")\n");
				results.append(Language.tr("Statistics.StdDevResidenceTime")+" Std[V]="+StatisticTools.formatExactTime(statisticResidence.getSD())+" ("+StatisticTools.formatNumberExt(statisticResidence.getSD(),false)+")\n");
				results.append(Language.tr("Statistics.VarianceResidenceTime")+" Var[V]="+StatisticTools.formatExactTime(statisticResidence.getVar())+" ("+StatisticTools.formatNumberExt(statisticResidence.getVar(),false)+")\n");
				results.append(Language.tr("Statistics.CVResidenceTime")+" CV[V]"+StatisticTools.formatNumberExt(statisticResidence.getCV(),true)+"\n");
				results.append(Language.tr("Statistics.Skewness")+": Sk[V]"+StatisticTools.formatNumberExt(statisticResidence.getSk(),true)+"\n");
				results.append(Language.tr("Statistics.Kurt")+": Kurt[V]"+StatisticTools.formatNumberExt(statisticResidence.getKurt(),true)+"\n");
				results.append(Language.tr("Statistics.MinimumResidenceTime")+" Min[V]="+StatisticTools.formatExactTime(statisticResidence.getMin())+" ("+StatisticTools.formatNumberExt(statisticResidence.getMin(),false)+")\n");
				results.append(Language.tr("Statistics.MaximumResidenceTime")+" Max[V]="+StatisticTools.formatExactTime(statisticResidence.getMax())+" ("+StatisticTools.formatNumberExt(statisticResidence.getMax(),false)+")\n");
			}
		}

		if (data.statisticClientsAtStation!=null && data.statisticClientsAtStation.getTimeMax()>0) {
			results.append("\n"+Language.tr("Statistics.NumberOfClientsAtStations.Singular")+":\n");
			results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStation"),NumberTools.formatLong(data.reportedClientsAtStation(simData)))+"\n");
			results.append(Language.tr("Statistics.AverageNumberOfClients")+" E[N]"+StatisticTools.formatNumberExt(data.statisticClientsAtStation.getTimeMean(),true)+"\n");
			results.append(Language.tr("Statistics.MaximumNumberOfClients")+" Max[N]"+StatisticTools.formatNumberExt(data.statisticClientsAtStation.getTimeMax(),true)+"\n");
			outputShortStateDistribution("N",data.statisticClientsAtStation);
			outputQuantil("N",data.statisticClientsAtStation);
		}

		if (data.statisticClientsAtStationQueue!=null && data.statisticClientsAtStationQueue.getTimeMax()>0) {
			results.append("\n"+Language.tr("Statistics.NumberOfClientsAtStationQueues.Singular")+":\n");
			results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStationQueue"),NumberTools.formatLong(data.clientsAtStationQueue))+"\n");
			results.append(Language.tr("Statistics.AverageNumberOfClientsInQueue")+" E[NQ]"+StatisticTools.formatNumberExt(data.statisticClientsAtStationQueue.getTimeMean(),true)+"\n");
			results.append(Language.tr("Statistics.MaximumNumberOfClientsInQueue")+" Max[NQ]"+StatisticTools.formatNumberExt(data.statisticClientsAtStationQueue.getTimeMax(),true)+"\n");
			outputShortStateDistribution("NQ",data.statisticClientsAtStationQueue);
			outputQuantil("NQ",data.statisticClientsAtStationQueue);
		}

		if (data.statisticClientsAtStationProcess!=null && data.statisticClientsAtStationProcess.getTimeMax()>0) {
			results.append("\n"+Language.tr("Statistics.NumberOfClientsAtStationProcess.Singular")+":\n");
			results.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStationProcess"),NumberTools.formatLong(data.clientsAtStationProcess))+"\n");
			results.append(Language.tr("Statistics.AverageNumberOfClientsInProcess")+" E[NS]"+StatisticTools.formatNumberExt(data.statisticClientsAtStationProcess.getTimeMean(),true)+"\n");
			results.append(Language.tr("Statistics.MaximumNumberOfClientsInProcess")+" Max[NS]"+StatisticTools.formatNumberExt(data.statisticClientsAtStationProcess.getTimeMax(),true)+"\n");
			outputShortStateDistribution("NS",data.statisticClientsAtStationProcess);
			outputQuantil("NS",data.statisticClientsAtStationProcess);
		}
	}

	/**
	 * Gibt Quantil-Informationen zu einem Zahlen-Statistikobjekt aus.
	 * @param identifier	Bezeichner für das Statistikobjekt (z.B. "W")
	 * @param indicator	Statistikobjekt
	 */
	private void outputQuantil(final String identifier, final StatisticsTimePerformanceIndicator indicator) {
		if (indicator==null) return;

		double upperBound=Double.MAX_VALUE;
		if (indicator.getReadOnlyDistribution()!=null) upperBound=indicator.getReadOnlyDistribution().upperBound-1;

		boolean hitMax=false;
		final double[] levels=StatisticViewerOverviewText.getQuantilLevels();
		for (double p: levels) {
			final String name=Language.tr("Statistics.Quantil")+"["+identifier+","+StatisticTools.formatPercent(p)+"]";
			final double value=indicator.getQuantil(p);
			if (value>=upperBound) hitMax=true;
			results.append(name+StatisticTools.formatNumberExt(value,true)+"\n");
		}

		if (hitMax && indicator.getTimeMin()!=indicator.getTimeMax()) {
			results.append(String.format(Language.tr("Statistics.Quantil.InfoMax"),StatisticTools.formatNumberExt(upperBound,false))+"\n");
		}
	}

	/**
	 * Maximalanzahl an auszugebenden Verteilungsdaten in
	 * {@link #outputShortStateDistribution(String, StatisticsTimePerformanceIndicator)}
	 * @see #outputShortStateDistribution(String, StatisticsTimePerformanceIndicator)
	 */
	private static final int MAX_SHORT_STATE_DISTRIBUTION=5;

	/**
	 * Gibt Verteilungsdaten aus, sofern bei der Zähldichte nur für wenige Einträge Werte ungleich 0 vorliegen.
	 * @param identifier	Bezeichner für das Statistikobjekt (z.B. "W")
	 * @param indicator	Statistikobjekt
	 * @see #MAX_SHORT_STATE_DISTRIBUTION
	 */
	private void outputShortStateDistribution(final String identifier, final StatisticsTimePerformanceIndicator indicator) {
		if (indicator==null) return;
		if (indicator.getReadOnlyDistribution()==null) return;

		final double[] density=indicator.getReadOnlyDistribution().densityData;
		if (density.length==0) return;

		if (density.length>MAX_SHORT_STATE_DISTRIBUTION+1) {
			for (int i=MAX_SHORT_STATE_DISTRIBUTION+1;i<density.length;i++) if (density[i]>0) return;
		}

		double sum=0;
		int maxNonZero=0;
		for (int i=0;i<Math.min(MAX_SHORT_STATE_DISTRIBUTION+1,density.length);i++) {
			sum+=density[i];
			if (density[i]>0) maxNonZero=i;
		}
		if (maxNonZero==0) return;

		final StringBuilder info=new StringBuilder();
		info.append(Language.tr("Statistics.StateDistribution"));
		info.append(": ");
		for (int i=0;i<=maxNonZero;i++) {
			if (i>0) info.append(", ");
			info.append(String.format("P(%s=%d)%s",identifier,i,StatisticTools.formatPercentExt(density[i]/sum,true)));
		}

		info.append("\n");
		results.append(info.toString());
	}
}
