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
package simulator.elements;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.coreelements.RunElementPassThrough;
import simulator.editmodel.EditModel;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementBarrier;
import ui.modeleditor.elements.ModelElementBarrierSignalOption;
import ui.modeleditor.elements.ModelElementSub;

/**
 * �quivalent zu {@link ModelElementBarrier}
 * @author Alexander Herzog
 * @see ModelElementBarrier
 */
public class RunElementBarrier extends RunElementPassThrough implements SignalListener, PickUpQueue {
	/**
	 * Namen der Signale, auf die dieses Element h�ren soll
	 */
	private String[] signalName;

	/**
	 * Anzahl an Kunden, die das Element passieren k�nnen, bevor die Schrankenwirkung einsetzt
	 */
	private int[] initialClients;

	/**
	 * Maximale Anzahl an wartenden Kunden, die freigegeben werden, wenn das zugeh�rige Signal ausgel�st wird<br>
	 * ({@link Integer#MAX_VALUE}, wenn alle momentan wartenden freigegeben werden sollen)
	 */
	private int[] clientsPerSignal;

	/**
	 * Index des Kundentyps auf den die Freigabe wirken soll<br>
	 * (-1, wenn die Freigabe f�r alle Typen gelten soll)
	 */
	private int[] clientType;

	/**
	 * Signale zwischenspeichern, wenn keine Kunden warten?
	 */
	private boolean[] storeSignals;

	/**
	 * M�ssen alle Signale vorliegen oder reicht es aus, wenn ein Signal vorliegt, um einen Kunden freizugeben?
	 */
	private boolean needAllSignalsToRelease;

	/**
	 * Konstruktor der Klasse
	 * @param element Zugeh�riges Editor-Element
	 */
	public RunElementBarrier(final ModelElementBarrier element) {
		super(element,buildName(element,Language.tr("Simulation.Element.Barrier.Name")));
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementBarrier)) return null;
		final ModelElementBarrier barrierElement=(ModelElementBarrier)element;
		final RunElementBarrier barrier=new RunElementBarrier(barrierElement);

		/* Auslaufende Kante */
		final String edgeError=barrier.buildEdgeOut(barrierElement);
		if (edgeError!=null) return edgeError;

		/* Freigaberegeln */
		final List<ModelElementBarrierSignalOption> options=barrierElement.getOptions();
		if (options.size()==0) return String.format(Language.tr("Simulation.Creator.NoSignalOptionsForBarrier"),element.getId());

		barrier.signalName=new String[options.size()];
		barrier.initialClients=new int[options.size()];
		barrier.clientsPerSignal=new int[options.size()];
		barrier.clientType=new int[options.size()];
		barrier.storeSignals=new boolean[options.size()];

		barrier.needAllSignalsToRelease=barrierElement.isNeedAllSignalsToRelease();

		for (int i=0;i<options.size();i++) {
			final ModelElementBarrierSignalOption option=options.get(i);

			barrier.signalName[i]=option.getSignalName();
			if (barrier.signalName[i].isEmpty()) return String.format(Language.tr("Simulation.Creator.NoSignalForBarrier"),element.getId());

			barrier.initialClients[i]=FastMath.max(0,option.getInitialClients());

			final int count=option.getClientsPerSignal();
			barrier.clientsPerSignal[i]=(count>0)?count:Integer.MAX_VALUE;
			if (barrier.needAllSignalsToRelease && barrier.clientsPerSignal[i]==Integer.MAX_VALUE) return String.format(Language.tr("Simulation.Creator.NeedAllSignalsToRelease.FiniteNumberNeeded"),element.getId());

			barrier.storeSignals[i]=option.isStoreSignals();
			if (barrier.needAllSignalsToRelease && !barrier.storeSignals[i]) return String.format(Language.tr("Simulation.Creator.NeedAllSignalsToRelease.StoreSignalsNeeded"),element.getId());

			final String type=option.getClientType();
			if (type==null || type.isBlank()) {
				barrier.clientType[i]=-1;
			} else {
				barrier.clientType[i]=runModel.getClientTypeNr(type);
				if (barrier.clientType[i]<0) return String.format(Language.tr("Simulation.Creator.BarrierUnknownClientType"),element.getId(),type);
				if (barrier.needAllSignalsToRelease) return String.format(Language.tr("Simulation.Creator.NeedAllSignalsToRelease.AllClientTypesNeeded"),element.getId());
			}
		}

		return barrier;
	}

	@Override
	public RunModelCreatorStatus test(final ModelElement element) {
		if (!(element instanceof ModelElementBarrier)) return null;
		final ModelElementBarrier barrierElement=(ModelElementBarrier)element;

		/* Auslaufende Kante */
		final RunModelCreatorStatus edgeError=testEdgeOut(barrierElement);
		if (edgeError!=null) return edgeError;

		/* Freigaberegeln */
		final List<ModelElementBarrierSignalOption> options=barrierElement.getOptions();
		if (options.size()==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoSignalOptionsForBarrier"),element.getId()));

		for (int i=0;i<options.size();i++) {
			final ModelElementBarrierSignalOption option=options.get(i);
			if (option.getSignalName().isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoSignalForBarrier"),element.getId()));
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	public RunElementBarrierData getData(final SimulationData simData) {
		RunElementBarrierData data;
		data=(RunElementBarrierData)(simData.runData.getStationData(this));
		if (data==null) {
			data=new RunElementBarrierData(this,initialClients,simData);
			simData.runData.setStationData(this,data);
		}
		return data;
	}

	/**
	 * Gibt es noch Kunden, die �ber die initiale Freigabe durchgereicht werden sollen? (Modus: Einzelsignale)
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 * @param index	Index des Eintrags in {@link RunElementBarrierData#initialClients} der gepr�ft werden soll
	 * @return	Liefert <code>true</code>, wenn der Kunde direkt durchgereicht werden darf
	 */
	private boolean initialProcess(final SimulationData simData, final RunElementBarrierData data, final RunDataClient client, final int index) {
		/* Keine Direktdurchleite-Kunden mehr? */
		if (data.initialClients[index]<=0) return false;

		/* Falscher Kundentyp? */
		if (clientType[index]>=0 && clientType[index]!=client.type) return false;

		/* Z�hler f�r Direktdurchleite-Kunden verringern */
		data.initialClients[index]--;

		/* Kunden freigeben */
		initialRelease(simData,data,client);

		return true;
	}

	/**
	 * Gibt es noch Kunden, die �ber die initiale Freigabe durchgereicht werden sollen? (Modus: Es m�ssen alle Signale ausgel�st sein)
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 * @return	Liefert <code>true</code>, wenn der Kunde direkt durchgereicht werden darf
	 */
	private boolean initialProcessAllSignals(final SimulationData simData, final RunElementBarrierData data, final RunDataClient client) {
		/* �berall noch mindestens eine Freigabe m�glich? */
		for (int clients: data.initialClients) if (clients==0) return false;

		/* Z�hler f�r Direktdurchleite-Kunden verringern */
		final int size=data.initialClients.length;
		for (int i=0;i<size;i++) data.initialClients[i]--;

		/* Kunden freigeben */
		initialRelease(simData,data,client);

		return true;
	}

	/**
	 * Gibt ein konkretes Kundenobjekt auf Basis der initial vorgesehenen Freigaben frei.
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 */
	private void initialRelease(final SimulationData simData, final RunElementBarrierData data, final RunDataClient client) {
		/* Wartezeit in Statistik */
		simData.runData.logStationProcess(simData,this,client,0,0,0,0);
		client.addStationTime(id,0,0,0,0);

		/* Kunden an Station in Statistik */
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.BarrierInactive"),String.format(Language.tr("Simulation.Log.BarrierInactive.Info"),client.logInfo(simData),name));

		/* Kunde zur n�chsten Station leiten */
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.announceClient(simData,client,getNext());
	}

	@Override
	public void processArrival(final SimulationData simData, final RunDataClient client) {
		final RunElementBarrierData data=getData(simData);

		data.queueLockedForPickUp=true;
		try {
			/* Sollen noch Kunden direkt durchgeleitet werden? */
			if (needAllSignalsToRelease) {
				/* Pr�fen, ob noch gem�� aller Datens�tze Freigaben m�glich sind */
				if (initialProcessAllSignals(simData,data,client)) return;
			} else {
				/* Einzelne Signal-Datens�tze pr�fen */
				for (int i=0;i<signalName.length;i++) if (initialProcess(simData,data,client,i)) return;
			}

			/* Kunden in Warteschlange einreihen */
			data.waitingClients.add(client);
			client.lastWaitingStart=simData.currentTime;

			/* Kunden an Station in Statistik */
			simData.runData.logClientEntersStationQueue(simData,this,data,client);

			/* System �ber Status-�nderung benachrichtigen */
			simData.runData.fireStateChangeNotify(simData);
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	/**
	 * Pr�ft, ob basierend auf einem ausgel�sten Signal Kunden freigegeben werden k�nnen.
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param clientType	Kundentyp des Kunden, um den es gerade geht
	 * @return	Liefert <code>true</code>, wenn der Kunde freigegeben werden kann
	 * @see #processSignal(SimulationData, RunElementBarrierData, int)
	 */
	private boolean releaseClient(final SimulationData simData, final RunElementBarrierData data, final int clientType) {
		if (data.waitingClients.size()==0) return false;

		/* Kunde aus Warteschlange entfernen und weiterleiten */
		RunDataClient client=null;
		if (clientType<0) {
			client=data.waitingClients.remove(0);
		} else {
			for (int i=0;i<data.waitingClients.size();i++) if (data.waitingClients.get(i).type==clientType) {
				client=data.waitingClients.remove(i);
				break;
			}
			if (client==null) return false;
		}

		releaseClient(simData,data,client);

		return true;
	}

	/**
	 * Gibt ein konkretes Kundenobjekt frei.
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param client	Kunde
	 */
	private void releaseClient(final SimulationData simData, final RunElementBarrierData data, final RunDataClient client) {
		StationLeaveEvent.addLeaveEvent(simData,client,this,0);
		StationLeaveEvent.announceClient(simData,client,getNext());

		/* Wartezeit in Statistik */
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		simData.runData.logStationProcess(simData,this,client,waitingTime,0,0,waitingTime);
		client.addStationTime(id,waitingTime,0,0,waitingTime);

		/* Kunden an Station in Statistik */
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		/* Logging */
		if (simData.loggingActive) log(simData,Language.tr("Simulation.Log.ClientReleasedBySignal"),String.format(Language.tr("Simulation.Log.ClientReleasedBySignal.Info"),client.logInfo(simData),name));

	}

	/**
	 * Pr�ft, ob basierend auf einem ausgel�sten Signal Kunden freigegeben werden k�nnen.
	 * @param simData	Simulationsdaten
	 * @param data	Thread-lokales Datenobjekt zu der Station
	 * @param index	Index des Eintrags in {@link RunElementBarrierData#initialClients} der gepr�ft werden soll
	 */
	private void processSignal(final SimulationData simData, final RunElementBarrierData data, final int index) {
		/* Alle Signale f�r Freigabe notwendig */
		if (needAllSignalsToRelease) {
			data.initialClients[index]+=clientsPerSignal[index];
			final int size=data.initialClients.length;
			while (true) {
				/* Alle Signale ausgel�st? */
				for (int clients: data.initialClients) if (clients==0) return;
				/* Gibt es noch wartende Kunden? */
				if (data.waitingClients.size()==0) return;
				/* Freigabez�hler reduzieren */
				for (int i=0;i<size;i++) data.initialClients[i]--;
				/* Kunden freigeben */
				final RunDataClient client=data.waitingClients.remove(0);
				releaseClient(simData,data,client);
			}
		}

		/* Normaler Modus */
		for (int i=0;i<clientsPerSignal[index];i++) {
			/* Kunde freigeben ? */
			if (releaseClient(simData,data,clientType[index])) continue;

			/* Sonst als Guthaben verbuchen */
			if (clientsPerSignal[index]==Integer.MAX_VALUE || !storeSignals[index]) break;
			data.initialClients[index]++;
		}
	}

	@Override
	public void signalNotify(final SimulationData simData, final String signalName) {
		final RunElementBarrierData data=getData(simData);
		data.queueLockedForPickUp=true;
		try {
			for (int i=0;i<this.signalName.length;i++) {
				/* Ist das unser Signal? */
				if (!this.signalName[i].equals(signalName)) continue;

				/* Freigabe ausl�sen verarbeiten */
				processSignal(simData,data,i);
			}
		} finally {
			data.queueLockedForPickUp=false;
		}
	}

	@Override
	public RunDataClient getClient(final SimulationData simData) {
		final RunElementBarrierData data=getData(simData);
		if (data.queueLockedForPickUp) return null;
		if (data.waitingClients.size()==0) return null;

		final RunDataClient client=data.waitingClients.remove(0);
		final long waitingTime=simData.currentTime-client.lastWaitingStart;
		/* Nein, da Kunde an der Station ja nicht bedient wurde: simData.runData.logStationProcess(simData,this,waitingTime,0,0); */
		client.addStationTime(id,waitingTime,0,0,waitingTime);
		simData.runData.logClientLeavesStationQueue(simData,this,data,client);

		return client;
	}
}