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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import language.Language;
import simcore.Event;
import simcore.SimData;
import simulator.coreelements.RunElement;
import simulator.events.ProcessReleaseResources;
import simulator.events.ReleaseReleaseResources;
import simulator.events.StationLeaveEvent;
import simulator.events.SystemArrivalEvent;
import simulator.events.TransporterArrivalEvent;
import simulator.events.TransporterPauseEndEvent;
import simulator.events.WaitingCancelEvent;
import simulator.runmodel.SimulationData;
import systemtools.BaseDialog;
import ui.AnimationPanel;
import ui.images.Images;

/**
 * Zeigt eine Liste der nächsten anstehenden Ereignisse an
 * @author Alexander Herzog
 * @see AnimationPanel
 */
public class NextEventsViewerDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7426782186422397312L;

	/** Sortierte Liste der nächsten Ereignisse */
	private final List<Event> events;
	/** Simulationsdatenobjekt */
	private final SimulationData simData;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param events	Sortierte Liste der nächsten Ereignisse
	 * @param simData	Simulationsdatenobjekt
	 */
	public NextEventsViewerDialog(final Component owner, final List<Event> events, final SimulationData simData) {
		super(owner,Language.tr("Animation.NextEventsViewer.Title"));
		this.events=events;
		this.simData=simData;

		showCloseButton=true;
		addUserButton(Language.tr("Animation.NextEventsViewer.Copy"),Images.EDIT_COPY.getIcon());
		getUserButton(0).setToolTipText(Language.tr("Animation.NextEventsViewer.Copy.Hint"));
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		final JList<JLabel> viewer=new JList<>(getListContent(events,simData));
		viewer.setCellRenderer(new ListCellRenderer());
		content.add(new JScrollPane(viewer));

		setMinSizeRespectingScreensize(600,400);
		setResizable(true);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		final StringBuilder result=new StringBuilder();

		for (Event event: events) {
			if (result.length()>0) result.append("\n\n");

			if (event instanceof ProcessReleaseResources) {
				final ProcessReleaseResources e=(ProcessReleaseResources)event;
				result.append(SimData.formatSimTime(event.time));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.ReleaseResource"));
				result.append('\n');
				result.append(String.format(Language.tr("Simulation.Log.ReleaseResource.Info"),e.station.name));
			}

			if (event instanceof ReleaseReleaseResources) {
				final ReleaseReleaseResources e=(ReleaseReleaseResources)event;
				result.append(SimData.formatSimTime(event.time));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.Release"));
				result.append('\n');
				result.append(String.format(Language.tr("Simulation.Log.Release.InfoDelay2"),e.station.name));
			}

			if (event instanceof StationLeaveEvent) {
				final StationLeaveEvent e=(StationLeaveEvent)event;
				result.append(SimData.formatSimTime(event.time));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.LeaveStation"));
				result.append('\n');
				result.append(String.format(Language.tr("Simulation.Log.LeaveStation.Info"),e.client.logInfo(simData),e.station.name));
			}

			if (event instanceof SystemArrivalEvent) {
				final SystemArrivalEvent e=(SystemArrivalEvent)event;
				result.append(SimData.formatSimTime(event.time));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.SourceArrival"));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.Station")+": "+e.source.name);
			}

			if (event instanceof TransporterArrivalEvent) {
				final TransporterArrivalEvent e=(TransporterArrivalEvent)event;
				result.append(SimData.formatSimTime(event.time));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.TransporterArrival"));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.Station")+": "+((RunElement)(e.station)).name+", "+Language.tr("Simulation.Log.Transporter")+": "+simData.runData.transporters.type[e.transporter.type]);
			}

			if (event instanceof TransporterPauseEndEvent) {
				final TransporterPauseEndEvent e=(TransporterPauseEndEvent)event;
				result.append(SimData.formatSimTime(event.time));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.TransporterArrival"));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.Transporter")+": "+simData.runData.transporters.type[e.transporter.type]);
			}

			if (event instanceof WaitingCancelEvent) {
				final WaitingCancelEvent e=(WaitingCancelEvent)event;
				result.append(SimData.formatSimTime(event.time));
				result.append('\n');
				result.append(Language.tr("Simulation.Log.WaitingCancelation"));
				result.append('\n');
				result.append(String.format(Language.tr("Simulation.Log.WaitingCancelation.Info"),e.client.logInfo(simData),e.station.name));
			}
		}
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(result.toString()),null);
	}

	/**
	 * Erstellt Labels zur Darstellung der nächsten Ereignisse
	 * @param events	Liste der nächsten Ereignisse
	 * @param simData	Simulationsdatenobjekt
	 * @return	Array mit Labels zur Darstellung der nächsten Ereignisse
	 */
	private JLabel[] getListContent(final List<Event> events, final SimulationData simData) {
		final List<JLabel> result=new ArrayList<>();

		for (Event event: events) {
			final String text=processEvent(event,simData);
			if (text!=null) result.add(new JLabel("<html><body>"+SimData.formatSimTime(event.time)+":<br>"+text+"<br>&nbsp;</body></html>"));
		}

		return result.toArray(new JLabel[0]);
	}

	/**
	 * Erstellt einen html-Beschreibungstext für eine Ereignis.
	 * @param event	Ereignis
	 * @param simData	Simulationsdatenobjekt
	 * @return	html-Beschreibungstext für das Ereignis
	 * @see #getListContent(List, SimulationData)
	 */
	private String processEvent(final Event event, final SimulationData simData) {
		if (event instanceof ProcessReleaseResources) {
			final ProcessReleaseResources e=(ProcessReleaseResources)event;
			return "<b>"+Language.tr("Simulation.Log.ReleaseResource")+"</b><br>"+String.format(Language.tr("Simulation.Log.ReleaseResource.Info"),e.station.name);
		}

		if (event instanceof ReleaseReleaseResources) {
			final ReleaseReleaseResources e=(ReleaseReleaseResources)event;
			return "<b>"+Language.tr("Simulation.Log.Release")+"</b><br>"+String.format(Language.tr("Simulation.Log.Release.InfoDelay2"),e.station.name);
		}

		if (event instanceof StationLeaveEvent) {
			final StationLeaveEvent e=(StationLeaveEvent)event;
			return "<b>"+Language.tr("Simulation.Log.LeaveStation")+"</b><br>"+String.format(Language.tr("Simulation.Log.LeaveStation.Info"),e.client.logInfo(simData),e.station.name);
		}

		if (event instanceof SystemArrivalEvent) {
			final SystemArrivalEvent e=(SystemArrivalEvent)event;
			return "<b>"+Language.tr("Simulation.Log.SourceArrival")+"</b><br>"+Language.tr("Simulation.Log.Station")+": "+e.source.name;
		}

		if (event instanceof TransporterArrivalEvent) {
			final TransporterArrivalEvent e=(TransporterArrivalEvent)event;
			return "<b>"+Language.tr("Simulation.Log.TransporterArrival")+"</b><br>"+Language.tr("Simulation.Log.Station")+": "+((RunElement)(e.station)).name+", "+Language.tr("Simulation.Log.Transporter")+": "+simData.runData.transporters.type[e.transporter.type];
		}

		if (event instanceof TransporterPauseEndEvent) {
			final TransporterPauseEndEvent e=(TransporterPauseEndEvent)event;
			return "<b>"+Language.tr("Simulation.Log.TransporterArrival")+"</b><br>"+Language.tr("Simulation.Log.Transporter")+": "+simData.runData.transporters.type[e.transporter.type];

		}

		if (event instanceof WaitingCancelEvent) {
			final WaitingCancelEvent e=(WaitingCancelEvent)event;
			return "<b>"+Language.tr("Simulation.Log.WaitingCancelation")+"</b><br>"+String.format(Language.tr("Simulation.Log.WaitingCancelation.Info"),e.client.logInfo(simData),e.station.name);
		}

		return null;
	}

	/**
	 * Renderer für die Liste der nächsten Ereignisse
	 */
	private class ListCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -9219657834226171890L;

		/**
		 * Konstruktor der Klasse
		 */
		public ListCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((ListCellRenderer)renderer).setText(((JLabel)value).getText());
				((ListCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			return renderer;
		}
	}
}
