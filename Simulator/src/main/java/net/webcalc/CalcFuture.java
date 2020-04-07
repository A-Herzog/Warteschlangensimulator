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
package net.webcalc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import language.Language;
import net.calc.SimulationServer;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.statistics.StatisticViewerReport;
import tools.DateTools;
import tools.SetupData;
import ui.parameterseries.ParameterCompareRunner;
import ui.parameterseries.ParameterCompareSetup;
import ui.statistics.StatisticsPanel;
import xml.XMLTools;
import xml.XMLTools.FileType;

/**
 * Diese Klasse bildet einen einzelnen Rechen-Task innerhalb
 * eines {@link CalcWebServer}-Objekts ab.
 * @author Alexander Herzog
 * @see CalcWebServer
 */
public class CalcFuture {
	/**
	 * Status des Rechen-Tasks
	 * @author Alexander Herzog
	 * @see CalcFuture#getStatus()
	 */
	public enum Status {
		/**
		 * Der Task wartet noch.
		 */
		WAITING(0),

		/**
		 * Der Task wird gerade ausgeführt.
		 */
		PROCESSING(1),

		/**
		 * Die Ausführung des Tasks ist beendet, war aber nicht erfolgreich.
		 */
		DONE_ERROR(2),

		/**
		 * Die Ausführung des Tasks wurde erfolgreich abgeschlossen.
		 * Statistikdaten stehen zur Verfügung.
		 */
		DONE_SUCCESS(3);

		/**
		 * ID zur Identifikation des Status
		 */
		public final int id;

		Status(final int id) {
			this.id=id;
		}
	}

	/**
	 * Art der Simulation
	 * @author Alexander Herzog
	 *
	 */
	public enum SimulationType {
		/** Einfaches Modell */
		MODEL,
		/** Parameterreihe */
		PARAMETER_SERIES
	}

	private final ReentrantLock lock;
	private final long id;
	private final long requestTime;
	private final byte[] input;
	private final String ip;
	private Status status;
	private SimulationType simulationType;
	private final List<String> messages;
	private Statistics statistics;
	private byte[] zip;
	private XMLTools.FileType fileType;

	private volatile AnySimulator simulator=null;
	private volatile ParameterCompareRunner runner=null;

	/**
	 * Konstruktor der Klasse
	 * @param id	ID des Tasks zur späteren Identifikation in der Liste aller Tasks
	 * @param input	Eingabedatei (wird sofort gelesen und danach bei der eigentlichen Ausführung nicht mehr benötigt)
	 * @param ip	IP-Adresse des entfernten Klienten
	 */
	public CalcFuture(final long id, final File input, final String ip) {
		this.id=id;
		this.ip=ip;
		this.input=loadFile(input);

		messages=new ArrayList<>();
		lock=new ReentrantLock();
		requestTime=System.currentTimeMillis();

		if (input==null) {
			setStatus(Status.DONE_ERROR);
			addMessage(Language.tr("CalcWebServer.LoadError"));
		} else {
			setStatus(Status.WAITING);
			addMessage(Language.tr("CalcWebServer.LoadOk")+" - "+DateTools.formatUserDate(System.currentTimeMillis(),true));
		}
	}

	private byte[] loadFile(final File input) {
		if (input==null) return null;

		try (FileInputStream fileInput=new FileInputStream(input)) {
			try (DataInputStream data=new DataInputStream(fileInput)) {
				int size=data.available();
				final byte[] result=new byte[size];
				if (data.read(result)<size) return null;
				return result;
			}
		} catch (IOException e) {return null;}
	}

	/**
	 * Liefert die im Konstruktor übergebene ID zurück.
	 * @return	ID des Tasks
	 */
	public long getId() {
		return id;
	}

	private void setStatus(final Status status) {
		lock.lock();
		try {
			this.status=status;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Liefert den aktuellen Status des Tasks.
	 * @return	Aktueller Status
	 * @see Status
	 */
	public Status getStatus() {
		lock.lock();
		try {
			return status;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Liefert den Zeitstempel an dem der Task angelegt wurde
	 * @return	Zeitstempel des Anlegens des Tasks
	 */
	public long getRequestTime() {
		return requestTime;
	}

	/**
	 * Liefert die im Konstruktor übergebene IP des entfernten Clienten zurück.
	 * @return	IP des entfernten Clienten
	 */
	public String getIP() {
		return ip;
	}

	private void addMessage(final String message) {
		lock.lock();
		try {
			messages.add(message.replace("\n"," "));
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Liefert die Liste der Meldungen die sich bislang ergeben haben.
	 * @return	Liste der Meldungen (kann leer sein, aber nie <code>null</code>)
	 */
	public String[] getMessages() {
		lock.lock();
		try {
			return messages.toArray(new String[0]);
		} finally {
			lock.unlock();
		}
	}

	private void setError(final String message) {
		addMessage(message);
		setStatus(Status.DONE_ERROR);
	}

	private void setResult(final Statistics statistics, final ByteArrayOutputStream output, final XMLTools.FileType fileType, final String message) {
		this.statistics=statistics;
		setResult(output,fileType,message);
	}

	private void setResult(final ByteArrayOutputStream output, final XMLTools.FileType fileType, final String message) {
		addMessage(message);

		final ByteArrayOutputStream result=new ByteArrayOutputStream();
		try (GZIPOutputStream compressor=new GZIPOutputStream(result)) {
			compressor.write(output.toByteArray());
		} catch (IOException e) {
			addMessage(e.getMessage());
			setStatus(Status.DONE_ERROR);
			return;
		}
		zip=result.toByteArray();
		this.fileType=fileType;
		setStatus(Status.DONE_SUCCESS);
	}

	/**
	 * Liefert das Ergebnis der Simulation in Binärform
	 * @return	Bytes oder <code>null</code>, wenn noch keine Ergebnisse vorliegen
	 */
	public byte[] getBytes() {
		if (zip==null) return null;

		final ByteArrayOutputStream output=new ByteArrayOutputStream();
		try (GZIPInputStream decompressor=new GZIPInputStream(new ByteArrayInputStream(zip))) {
			while (decompressor.available()>0) {
				byte[] data=new byte[decompressor.available()];
				decompressor.read(data);
				output.write(data);
			}
		} catch (IOException e) {
			return null;
		}

		return output.toByteArray();
	}

	/**
	 * Liefert den Dateityp der Binärform-Daten
	 * @return	Dateityp der Binärform-Daten
	 * @see #getBytes()
	 */
	public XMLTools.FileType getXMLFileType() {
		return fileType;
	}

	private void runModel(final EditModel model) {
		if (!StartAnySimulator.isRemoveSimulateable(model)) {
			setError(SimulationServer.PREPARE_NO_REMOTE_MODEL);
			return;
		}

		final StartAnySimulator starter=new StartAnySimulator(model,null);
		final String prepareError=starter.prepare();
		if (prepareError!=null) {setError(prepareError); return;}
		addMessage(Language.tr("CalcWebServer.Simulation.Start")+" - "+DateTools.formatUserDate(System.currentTimeMillis(),true));
		simulator=starter.start();
		try {
			final Statistics statistics=simulator.getStatistic();
			final ByteArrayOutputStream output=new ByteArrayOutputStream();
			final XMLTools.FileType fileType=SetupData.getSetup().defaultSaveFormatStatistics.fileType;
			statistics.saveToStream(output,fileType);
			setResult(statistics,output,fileType,Language.tr("CalcWebServer.Simulation.Finished")+" - "+DateTools.formatUserDate(System.currentTimeMillis(),true));
		} finally {
			simulator=null;
		}
	}

	private void runSeries(final ParameterCompareSetup setup) {
		runner=new ParameterCompareRunner(null,null,msg->addMessage(msg));
		try {
			final String error=runner.check(setup);
			if (error!=null) {setError(error); return;}
			runner.start();
			if (runner.waitForFinish()) {
				final ByteArrayOutputStream output=new ByteArrayOutputStream();
				final XMLTools.FileType fileType=SetupData.getSetup().defaultSaveFormatParameterSeries.fileType;
				setup.saveToStream(output,fileType);
				setResult(output,fileType,Language.tr("CalcWebServer.Simulation.Finished")+" - "+DateTools.formatUserDate(System.currentTimeMillis(),true));
			} else {
				setError(Language.tr("CalcWebServer.Simulation.Failed"));
			}
		} finally {
			runner=null;
		}
	}

	/**
	 * Führt die Verarbeitung aus.<br>
	 * Diese Methode kann über einen anderen Thread ausgeführt werden.
	 */
	public void run() {
		if (status!=Status.WAITING || input==null) {
			setStatus(Status.DONE_ERROR);
			return;
		}
		setStatus(Status.PROCESSING);

		final EditModel model=new EditModel();
		if (model.loadFromStream(new ByteArrayInputStream(input),FileType.AUTO)==null) {
			simulationType=SimulationType.MODEL;
			runModel(model);
			return;
		}

		final ParameterCompareSetup series=new ParameterCompareSetup(null);
		if (series.loadFromStream(new ByteArrayInputStream(input),FileType.AUTO)==null) {
			simulationType=SimulationType.PARAMETER_SERIES;
			runSeries(series);
			return;
		}

		setError(Language.tr("CalcWebServer.Simulation.UnknownFormat"));
	}

	/**
	 * Handelt es sich bei dem Rechen-Task um eine normale Simulation und wurde
	 * diese erfolgreich abgeschlossen, so kann über diese Methode das zugehörige
	 * Statistikobjekt abgefragt werden.
	 * @return	Statistik-Ergebnis-Objekt für den Rechen-Task
	 */
	public Statistics getStatistics() {
		return statistics;
	}

	private byte[] viewerData=null;

	/**
	 * Erstellt einen html-js-basierenden Statistik-Viewer (sofern es sich um eine
	 * normale Simulation handelt und diese erfolgreich abgeschlossen wurde) und liefert
	 * diesen als html-Daten zurück
	 * @return	html-Daten eines interaktiven Statistik-Viewers
	 * @see #getStatistics()
	 */
	public byte[] getStatisticsViewer() {
		if (viewerData!=null) return viewerData;
		if (statistics==null) return null;
		final StatisticsPanel panel=new StatisticsPanel(1);
		panel.setStatistics(statistics,false);
		final StatisticViewerReport viewer=new StatisticViewerReport(panel.getStatisticNodeRoot(),statistics.editModel.name,0,null);
		try (ByteArrayOutputStream stream=new ByteArrayOutputStream()) {
			viewer.writeReportHTMLApp(stream);
			return viewerData=stream.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	private String getStatusText() {
		if (status==null) return Language.tr("CalcWebServer.Status.Unknown");
		switch (status) {
		case DONE_ERROR: return Language.tr("CalcWebServer.Status.Done_error");
		case DONE_SUCCESS: return Language.tr("CalcWebServer.Status.Done_success");
		case PROCESSING: return Language.tr("CalcWebServer.Status.Processing");
		case WAITING: return Language.tr("CalcWebServer.Status.Waiting");
		default: return Language.tr("CalcWebServer.Status.Unknown");
		}
	}

	private String jsonFormatMessages() {
		final StringBuilder result=new StringBuilder();
		result.append("[");
		for (int i=0;i<messages.size();i++) {
			if (i>0) result.append(",");
			result.append("\"");
			result.append(messages.get(i));
			result.append("\"");
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * Liefert den aktuellen Status des Task in Form eines JSON-Objektes.
	 * @return	Status als JSON-Objekt
	 */
	public String getStatusJSON() {
		final String viewable=(this.status==Status.DONE_SUCCESS && simulationType==SimulationType.MODEL)?"1":"0";

		final StringBuilder status=new StringBuilder();
		status.append("{\n");
		status.append("  \"id\": \""+id+"\",\n");
		status.append("  \"time\": \""+DateTools.formatUserDate(requestTime,true)+"\",\n");
		status.append("  \"status\": \""+this.status.id+"\",\n");
		status.append("  \"statusText\": \""+getStatusText()+"\",\n");
		status.append("  \"viewable\": \""+viewable+"\",\n");
		status.append("  \"client\": \""+ip+"\",\n");
		status.append("  \"messages\": "+jsonFormatMessages()+"\n");
		status.append("}");
		return status.toString();
	}

	/**
	 * Bricht die Verarbeitung des Tasks ab bzw. stellt ein,
	 * dass beim späteren Aufruf von {@link #run()} keine
	 * Verarbeitung mehr stattfindet.
	 */
	public void cancel() {
		setError(Language.tr("CalcWebServer.Simulation.Canceled")+" - "+DateTools.formatUserDate(System.currentTimeMillis(),true));

		if (simulator!=null) simulator.cancel();
		if (runner!=null) runner.cancel();
	}
}
