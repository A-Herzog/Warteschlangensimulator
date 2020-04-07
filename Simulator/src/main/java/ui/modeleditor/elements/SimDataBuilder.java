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
import mathtools.TimeTools;
import simcore.SimData;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.runmodel.SimulationData;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import ui.modeleditor.coreelements.ModelElement;

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
	public final StringBuilder sb;

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
			sb=null;
			return;
		}

		data=element.getData(simData);
		sb=new StringBuilder();

		sb.append(element.name+"\n\n");
		sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.Clients"),NumberTools.formatLong(data.clients))+"\n");
		sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStation"),NumberTools.formatLong(data.reportedClientsAtStation(simData)))+"\n");
		sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStationQueue"),NumberTools.formatLong(data.clientsAtStationQueue))+"\n\n");

		if (data.lastArrival>0) {
			sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastArrival"),SimData.formatSimTime(data.lastArrival))+"\n");
		}
		if (data.lastArrivalByClientType!=null) for (int i=0;i<data.lastArrivalByClientType.length;i++) if (data.lastArrivalByClientType[i]>0) {
			final String clientType=simData.runModel.clientTypes[i];
			sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastArrivalByClientTypeType"),clientType,SimData.formatSimTime(data.lastArrivalByClientType[i]))+"\n");
		}

		if (data.lastLeave>0) {
			sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastLeave"),SimData.formatSimTime(data.lastLeave))+"\n");
		}
		if (data.lastLeaveByClientType!=null) for (int i=0;i<data.lastLeaveByClientType.length;i++) if (data.lastLeaveByClientType[i]>0) {
			final String clientType=simData.runModel.clientTypes[i];
			sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.LastLeaveByClientType"),clientType,SimData.formatSimTime(data.lastLeaveByClientType[i]))+"\n");
		}

		if (data.statisticWaiting!=null && data.statisticWaiting.getMean()>0) {
			sb.append("\n"+Language.tr("Statistics.WaitingTimes")+":\n");
			sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticWaiting.getCount())+"\n");
			sb.append(Language.tr("Statistics.AverageWaitingTime")+" E[W]="+TimeTools.formatExactTime(data.statisticWaiting.getMean())+" ("+NumberTools.formatNumber(data.statisticWaiting.getMean())+")\n");
			sb.append(Language.tr("Statistics.StdDevWaitingTime")+" Std[W]="+TimeTools.formatExactTime(data.statisticWaiting.getSD())+" ("+NumberTools.formatNumber(data.statisticWaiting.getSD())+")\n");
			sb.append(Language.tr("Statistics.VarianceWaitingTime")+" Var[W]="+TimeTools.formatExactTime(data.statisticWaiting.getVar())+" ("+NumberTools.formatNumber(data.statisticWaiting.getVar())+")\n");
			sb.append(Language.tr("Statistics.CVWaitingTime")+" CV[W]="+NumberTools.formatNumber(data.statisticWaiting.getCV())+"\n");
			sb.append(Language.tr("Statistics.MinimumWaitingTime")+" Min[W]="+TimeTools.formatExactTime(data.statisticWaiting.getMin())+" ("+NumberTools.formatNumber(data.statisticWaiting.getMin())+")\n");
			sb.append(Language.tr("Statistics.MaximumWaitingTime")+" Max[W]="+TimeTools.formatExactTime(data.statisticWaiting.getMax())+" ("+NumberTools.formatNumber(data.statisticWaiting.getMax())+")\n");
		}

		if (data.statisticWaitingByClientType!=null && data.statisticWaitingByClientType.length>1) for (int i=0;i<data.statisticWaitingByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticWaiting=data.statisticWaitingByClientType[i];
			if (statisticWaiting!=null && statisticWaiting.getMean()>0) {
				sb.append("\n"+String.format(Language.tr("Statistics.WaitingTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticWaiting.getCount())+"\n");
				sb.append(Language.tr("Statistics.AverageWaitingTime")+" E[W]="+TimeTools.formatExactTime(statisticWaiting.getMean())+" ("+NumberTools.formatNumber(statisticWaiting.getMean())+")\n");
				sb.append(Language.tr("Statistics.StdDevWaitingTime")+" Std[W]="+TimeTools.formatExactTime(statisticWaiting.getSD())+" ("+NumberTools.formatNumber(statisticWaiting.getSD())+")\n");
				sb.append(Language.tr("Statistics.VarianceWaitingTime")+" Var[W]="+TimeTools.formatExactTime(statisticWaiting.getVar())+" ("+NumberTools.formatNumber(statisticWaiting.getVar())+")\n");
				sb.append(Language.tr("Statistics.CVWaitingTime")+" CV[W]="+NumberTools.formatNumber(statisticWaiting.getCV())+"\n");
				sb.append(Language.tr("Statistics.MinimumWaitingTime")+" Min[W]="+TimeTools.formatExactTime(statisticWaiting.getMin())+" ("+NumberTools.formatNumber(statisticWaiting.getMin())+")\n");
				sb.append(Language.tr("Statistics.MaximumWaitingTime")+" Max[W]="+TimeTools.formatExactTime(statisticWaiting.getMax())+" ("+NumberTools.formatNumber(statisticWaiting.getMax())+")\n");
			}
		}

		if (data.statisticTransfer!=null && data.statisticTransfer.getMean()>0) {
			sb.append("\n"+Language.tr("Statistics.TransferTimes")+":\n");
			sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticTransfer.getCount())+"\n");
			sb.append(Language.tr("Statistics.AverageTransferTime")+" E[T]="+TimeTools.formatExactTime(data.statisticTransfer.getMean())+" ("+NumberTools.formatNumber(data.statisticTransfer.getMean())+")\n");
			sb.append(Language.tr("Statistics.StdDevTransferTime")+" Std[T]="+TimeTools.formatExactTime(data.statisticTransfer.getSD())+" ("+NumberTools.formatNumber(data.statisticTransfer.getSD())+")\n");
			sb.append(Language.tr("Statistics.VarianceTransferTime")+" Var[T]="+TimeTools.formatExactTime(data.statisticTransfer.getVar())+" ("+NumberTools.formatNumber(data.statisticTransfer.getVar())+")\n");
			sb.append(Language.tr("Statistics.CVTransferTime")+" CV[T]="+NumberTools.formatNumber(data.statisticTransfer.getCV())+"\n");
			sb.append(Language.tr("Statistics.MinimumTransferTime")+" Min[T]="+TimeTools.formatExactTime(data.statisticTransfer.getMin())+" ("+NumberTools.formatNumber(data.statisticTransfer.getMin())+")\n");
			sb.append(Language.tr("Statistics.MaximumTransferTime")+" Max[T]="+TimeTools.formatExactTime(data.statisticTransfer.getMax())+" ("+NumberTools.formatNumber(data.statisticTransfer.getMax())+")\n");
		}

		if (data.statisticTransferByClientType!=null && data.statisticTransferByClientType.length>1) for (int i=0;i<data.statisticTransferByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticTransfer=data.statisticTransferByClientType[i];
			if (statisticTransfer!=null && statisticTransfer.getMean()>0) {
				sb.append("\n"+String.format(Language.tr("Statistics.TransferTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticTransfer.getCount())+"\n");
				sb.append(Language.tr("Statistics.AverageTransferTime")+" E[T]="+TimeTools.formatExactTime(statisticTransfer.getMean())+" ("+NumberTools.formatNumber(statisticTransfer.getMean())+")\n");
				sb.append(Language.tr("Statistics.StdDevTransferTime")+" Std[T]="+TimeTools.formatExactTime(statisticTransfer.getSD())+" ("+NumberTools.formatNumber(statisticTransfer.getSD())+")\n");
				sb.append(Language.tr("Statistics.VarianceTransferTime")+" Var[T]="+TimeTools.formatExactTime(statisticTransfer.getVar())+" ("+NumberTools.formatNumber(statisticTransfer.getVar())+")\n");
				sb.append(Language.tr("Statistics.CVTransferTime")+" CV[T]="+NumberTools.formatNumber(statisticTransfer.getCV())+"\n");
				sb.append(Language.tr("Statistics.MinimumTransferTime")+" Min[T]="+TimeTools.formatExactTime(statisticTransfer.getMin())+" ("+NumberTools.formatNumber(statisticTransfer.getMin())+")\n");
				sb.append(Language.tr("Statistics.MaximumTransferTime")+" Max[T]="+TimeTools.formatExactTime(statisticTransfer.getMax())+" ("+NumberTools.formatNumber(statisticTransfer.getMax())+")\n");
			}
		}

		if (data.statisticProcess!=null && data.statisticProcess.getMean()>0) {
			sb.append("\n"+Language.tr("Statistics.ProcessTimes")+":\n");
			sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticProcess.getCount())+"\n");
			sb.append(Language.tr("Statistics.AverageProcessTime")+" E[S]="+TimeTools.formatExactTime(data.statisticProcess.getMean())+" ("+NumberTools.formatNumber(data.statisticProcess.getMean())+")\n");
			sb.append(Language.tr("Statistics.StdDevProcessTime")+" Std[S]="+TimeTools.formatExactTime(data.statisticProcess.getSD())+" ("+NumberTools.formatNumber(data.statisticProcess.getSD())+")\n");
			sb.append(Language.tr("Statistics.VarianceProcessTime")+" Var[S]="+TimeTools.formatExactTime(data.statisticProcess.getVar())+" ("+NumberTools.formatNumber(data.statisticProcess.getVar())+")\n");
			sb.append(Language.tr("Statistics.CVProcessTime")+" CV[S]="+NumberTools.formatNumber(data.statisticProcess.getCV())+"\n");
			sb.append(Language.tr("Statistics.MinimumProcessTime")+" Min[S]="+TimeTools.formatExactTime(data.statisticProcess.getMin())+" ("+NumberTools.formatNumber(data.statisticProcess.getMin())+")\n");
			sb.append(Language.tr("Statistics.MaximumProcessTime")+" Max[S]="+TimeTools.formatExactTime(data.statisticProcess.getMax())+" ("+NumberTools.formatNumber(data.statisticProcess.getMax())+")\n");
		}

		if (data.statisticProcessByClientType!=null && data.statisticProcessByClientType.length>1) for (int i=0;i<data.statisticProcessByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticProcess=data.statisticProcessByClientType[i];
			if (statisticProcess!=null && statisticProcess.getMean()>0) {
				sb.append("\n"+String.format(Language.tr("Statistics.ProcessTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticProcess.getCount())+"\n");
				sb.append(Language.tr("Statistics.AverageProcessTime")+" E[S]="+TimeTools.formatExactTime(statisticProcess.getMean())+" ("+NumberTools.formatNumber(statisticProcess.getMean())+")\n");
				sb.append(Language.tr("Statistics.StdDevProcessTime")+" Std[S]="+TimeTools.formatExactTime(statisticProcess.getSD())+" ("+NumberTools.formatNumber(statisticProcess.getSD())+")\n");
				sb.append(Language.tr("Statistics.VarianceProcessTime")+" Var[S]="+TimeTools.formatExactTime(statisticProcess.getVar())+" ("+NumberTools.formatNumber(statisticProcess.getVar())+")\n");
				sb.append(Language.tr("Statistics.CVProcessTime")+" CV[S]="+NumberTools.formatNumber(statisticProcess.getCV())+"\n");
				sb.append(Language.tr("Statistics.MinimumProcessTime")+" Min[S]="+TimeTools.formatExactTime(statisticProcess.getMin())+" ("+NumberTools.formatNumber(statisticProcess.getMin())+")\n");
				sb.append(Language.tr("Statistics.MaximumProcessTime")+" Max[S]="+TimeTools.formatExactTime(statisticProcess.getMax())+" ("+NumberTools.formatNumber(statisticProcess.getMax())+")\n");
			}
		}

		if (data.statisticResidence!=null && data.statisticResidence.getMean()>0) {
			sb.append("\n"+Language.tr("Statistics.ResidenceTimes")+":\n");
			sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(data.statisticResidence.getCount())+"\n");
			sb.append(Language.tr("Statistics.AverageResidenceTime")+" E[V]="+TimeTools.formatExactTime(data.statisticResidence.getMean())+" ("+NumberTools.formatNumber(data.statisticResidence.getMean())+")\n");
			sb.append(Language.tr("Statistics.StdDevResidenceTime")+" Std[V]="+TimeTools.formatExactTime(data.statisticResidence.getSD())+" ("+NumberTools.formatNumber(data.statisticResidence.getSD())+")\n");
			sb.append(Language.tr("Statistics.VarianceResidenceTime")+" Var[V]="+TimeTools.formatExactTime(data.statisticResidence.getVar())+" ("+NumberTools.formatNumber(data.statisticResidence.getVar())+")\n");
			sb.append(Language.tr("Statistics.CVResidenceTime")+" CV[V]="+NumberTools.formatNumber(data.statisticResidence.getCV())+"\n");
			sb.append(Language.tr("Statistics.MinimumResidenceTime")+" Min[V]="+TimeTools.formatExactTime(data.statisticResidence.getMin())+" ("+NumberTools.formatNumber(data.statisticResidence.getMin())+")\n");
			sb.append(Language.tr("Statistics.MaximumResidenceTime")+" Max[V]="+TimeTools.formatExactTime(data.statisticResidence.getMax())+" ("+NumberTools.formatNumber(data.statisticResidence.getMax())+")\n");
		}

		if (data.statisticResidenceByClientType!=null && data.statisticResidenceByClientType.length>1) for (int i=0;i<data.statisticResidenceByClientType.length;i++) {
			final StatisticsDataPerformanceIndicator statisticResidence=data.statisticResidenceByClientType[i];
			if (statisticResidence!=null && statisticResidence.getMean()>0) {
				sb.append("\n"+String.format(Language.tr("Statistics.ResidenceTimesByClientType"),simData.runModel.clientTypes[i])+":\n");
				sb.append(Language.tr("Statistics.NumberOfClients")+": "+NumberTools.formatLong(statisticResidence.getCount())+"\n");
				sb.append(Language.tr("Statistics.AverageResidenceTime")+" E[V]="+TimeTools.formatExactTime(statisticResidence.getMean())+" ("+NumberTools.formatNumber(statisticResidence.getMean())+")\n");
				sb.append(Language.tr("Statistics.StdDevResidenceTime")+" Std[V]="+TimeTools.formatExactTime(statisticResidence.getSD())+" ("+NumberTools.formatNumber(statisticResidence.getSD())+")\n");
				sb.append(Language.tr("Statistics.VarianceResidenceTime")+" Var[V]="+TimeTools.formatExactTime(statisticResidence.getVar())+" ("+NumberTools.formatNumber(statisticResidence.getVar())+")\n");
				sb.append(Language.tr("Statistics.CVResidenceTime")+" CV[V]="+NumberTools.formatNumber(statisticResidence.getCV())+"\n");
				sb.append(Language.tr("Statistics.MinimumResidenceTime")+" Min[V]="+TimeTools.formatExactTime(statisticResidence.getMin())+" ("+NumberTools.formatNumber(statisticResidence.getMin())+")\n");
				sb.append(Language.tr("Statistics.MaximumResidenceTime")+" Max[V]="+TimeTools.formatExactTime(statisticResidence.getMax())+" ("+NumberTools.formatNumber(statisticResidence.getMax())+")\n");
			}
		}

		if (data.statisticClientsAtStation!=null && data.statisticClientsAtStation.getTimeMax()>0) {
			sb.append("\n"+Language.tr("Statistics.NumberOfClientsAtStations.Singular")+":\n");
			sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStation"),NumberTools.formatLong(data.reportedClientsAtStation(simData)))+"\n");
			sb.append(Language.tr("Statistics.AverageNumberOfClients")+" E[N]="+NumberTools.formatNumber(data.statisticClientsAtStation.getTimeMean())+"\n");
			sb.append(Language.tr("Statistics.MaximumNumberOfClients")+" Max[N]="+NumberTools.formatNumber(data.statisticClientsAtStation.getTimeMax())+"\n");
		}

		if (data.statisticClientsAtStationQueue!=null && data.statisticClientsAtStationQueue.getTimeMax()>0) {
			sb.append("\n"+Language.tr("Statistics.NumberOfClientsAtStationQueues.Singular")+":\n");
			sb.append(String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Data.ClientsAtStationQueue"),NumberTools.formatLong(data.clientsAtStationQueue))+"\n");
			sb.append(Language.tr("Statistics.AverageNumberOfClientsInQueue")+" E[NQ]="+NumberTools.formatNumber(data.statisticClientsAtStationQueue.getTimeMean())+"\n");
			sb.append(Language.tr("Statistics.MaximumNumberOfClientsInQueue")+" Max[NQ]="+NumberTools.formatNumber(data.statisticClientsAtStationQueue.getTimeMax())+"\n");
		}
	}
}
