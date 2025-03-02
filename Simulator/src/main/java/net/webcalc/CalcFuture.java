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
import java.util.Base64;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import language.Language;
import mathtools.MultiTable;
import mathtools.Table;
import net.calc.SimulationServer;
import simulator.AnySimulator;
import simulator.Simulator;
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
		WAITING(0,false),

		/**
		 * Der Task wird gerade ausgeführt.
		 */
		PROCESSING(1,false),

		/**
		 * Die Ausführung des Tasks ist beendet, war aber nicht erfolgreich.
		 */
		DONE_ERROR(2,true),

		/**
		 * Die Ausführung des Tasks wurde erfolgreich abgeschlossen.
		 * Statistikdaten stehen zur Verfügung.
		 */
		DONE_SUCCESS(3,true);

		/**
		 * ID zur Identifikation des Status
		 */
		public final int id;

		/**
		 * Handelt es sich um einen End-Status (Erfolg oder Abbruch)?
		 */
		public final boolean done;

		/**
		 * Konstruktor des Enum
		 * @param id	ID zur Identifikation des Status
		 * @param done	Handelt es sich um einen End-Status (Erfolg oder Abbruch)?
		 */
		Status(final int id, final boolean done) {
			this.id=id;
			this.done=done;
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

	/** Sichert den Zugriff auf den Status und die Nachrichten ab */
	private final ReentrantLock lock;
	/** ID des Tasks zur späteren Identifikation in der Liste aller Tasks */
	private final long id;
	/** System-Zeitpunkt an dem die Anfrage einging (d.h. an dem der Konstruktor aufgerufen wurde) */
	private final long requestTime;
	/** Festgelegtes Modell (darf <code>null</code> sein); im Fall eines festen Modells erfolgt nur noch eine Parametrisierung */
	private final EditModel originalModel;
	/** Eingabedaten (werden bereits im Konstruktor erfasst, aber die Modell-Erstellung daraus erfolgt erst später) */
	private final byte[] input;
	/** Geladene Tabelle zum Parametrisieren des Modells */
	private MultiTable inputTable;
	/** Dateiname der geladenen Tabelle zum Parametrisieren des Modells */
	private String inputTableName;
	/** IP-Adresse des entfernten Klienten */
	private final String ip;
	/** Aktueller Ausführungsstatus */
	private Status status;
	/** Art der Simulation */
	private SimulationType simulationType;
	/** Liste der während der Verarbeitung aufgetretenen Meldungen */
	private final List<String> messages;
	/** Ausgabe-Statistikdaten */
	private Statistics statistics;
	/** Ergebnisse der Simulation in Binärform ({@link #getBytes()}) */
	private byte[] zip;
	/** Dateityp für die Ergebnis-Binärform-Daten */
	private XMLTools.FileType fileType;

	/** Benachrichtigung beim Simulationsende (Erfolg oder Abbruch) (kann <code>null</code> sein) */
	private final Consumer<CalcFuture> doneNotify;

	/** Simulator (nur während der Ausführung einer normalen Simulation ungleich <code>null</code>) */
	private volatile AnySimulator simulator=null;
	/** Parameterreihensimulator (nur während der Ausführung einer Parameterreihensimulation unlgeich <code>null</code>) */
	private volatile ParameterCompareRunner runner=null;

	/**
	 * Konstruktor der Klasse
	 * @param id	ID des Tasks zur späteren Identifikation in der Liste aller Tasks
	 * @param input	Eingabedatei (wird sofort gelesen und danach bei der eigentlichen Ausführung nicht mehr benötigt)
	 * @param ip	IP-Adresse des entfernten Klienten
	 * @param origFileName	Optional (kann also <code>null</code> sein) der Remote-Dateiname
	 * @param model	Festgelegtes Modell (darf <code>null</code> sein); im Fall eines festen Modells erfolgt nur noch eine Parametrisierung
	 */
	public CalcFuture(final long id, final File input, final String ip, final String origFileName, final EditModel model) {
		this.id=id;
		this.ip=ip;
		originalModel=model;
		if (originalModel==null) {
			this.input=loadFile(input);
			inputTable=null;
			inputTableName=null;
		} else {
			this.input=null;
			final MultiTable table=new MultiTable();
			if (origFileName==null) {
				if (table.load(input)) inputTable=table; else inputTable=null;
				inputTableName="data";
			} else {
				final Table.SaveMode mode=Table.getSaveModeFromFileName(new File(origFileName),true,false);
				if (table.load(input,mode)) inputTable=table; else inputTable=null;
				inputTableName=origFileName;
			}
		}

		doneNotify=null;

		messages=new ArrayList<>();
		lock=new ReentrantLock();
		requestTime=System.currentTimeMillis();

		initialStatusUpdate();
	}

	/**
	 * Konstruktor der Klasse
	 * @param input	Eingabedaten
	 * @param doneNotify	Benachrichtigung beim Simulationsende (Erfolg oder Abbruch) (kann <code>null</code> sein)
	 */
	public CalcFuture(final byte[] input, final Consumer<CalcFuture> doneNotify) {
		id=0;
		ip="";
		this.input=input;
		originalModel=null;
		inputTable=null;
		inputTableName=null;

		this.doneNotify=doneNotify;

		messages=new ArrayList<>();
		lock=new ReentrantLock();
		requestTime=System.currentTimeMillis();

		initialStatusUpdate();
	}

	/**
	 * Konstruktor der Klasse
	 * @param model	Festgelegtes Modell (darf <code>null</code> sein); im Fall eines festen Modells erfolgt nur noch eine Parametrisierung
	 * @param input	Eingabedaten
	 * @param doneNotify	Benachrichtigung beim Simulationsende (Erfolg oder Abbruch) (kann <code>null</code> sein)
	 */
	public CalcFuture(final EditModel model, final byte[] input, final Consumer<CalcFuture> doneNotify) {
		id=0;
		ip="";
		this.input=input;
		originalModel=model;
		inputTable=null;
		inputTableName=null;

		this.doneNotify=doneNotify;

		messages=new ArrayList<>();
		lock=new ReentrantLock();
		requestTime=System.currentTimeMillis();

		initialStatusUpdate();
	}

	/**
	 * Stellt den Status direkt nach dem Abschluss des Konstruktors ein.
	 */
	private void initialStatusUpdate() {
		if (input==null) {
			setStatus(Status.DONE_ERROR);
			addMessage(Language.tr("CalcWebServer.LoadError"));
		} else {
			setStatus(Status.WAITING);
			addMessage(Language.tr("CalcWebServer.LoadOk")+" - "+DateTools.formatUserDate(System.currentTimeMillis(),true));
		}
	}

	/**
	 * Lädt eine Datei als Binärdaten
	 * @param input	Zu ladende Datei
	 * @return	Binärdaten oder im Fehlerfall <code>null</code>
	 */
	private byte[] loadFile(final File input) {
		if (input==null) return null;

		try (FileInputStream fileInput=new FileInputStream(input)) {
			try (DataInputStream data=new DataInputStream(fileInput)) {
				int size=data.available();
				final byte[] result=new byte[size];
				int read=0;
				while (read<size) read+=data.read(result);
				return result;
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Liefert die im Konstruktor übergebene ID zurück.
	 * @return	ID des Tasks
	 */
	public long getId() {
		return id;
	}

	/**
	 * Stellt den aktuellen Status ein
	 * @param status	Neuer Status
	 * @see #status
	 * @see #getStatus()
	 */
	private void setStatus(final Status status) {
		lock.lock();
		try {
			this.status=status;
		} finally {
			lock.unlock();
		}

		if (status.done && doneNotify!=null) doneNotify.accept(this);
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

	/**
	 * Fügt eine Nachricht zu der Liste der Nachrichten hinzu
	 * @param message	Neue Nachricht
	 * @see #messages
	 * @see #getMessages()
	 */
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
			return messages.toArray(String[]::new);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Fügt eine Fehlermeldung zu der Liste der Nachrichten hinzu
	 * und setzt den Fehler-Status
	 * @param message	Fehlermeldung
	 * @see #messages
	 * @see #getMessages()
	 * @see Status#DONE_ERROR
	 */
	private void setError(final String message) {
		addMessage(message);
		setStatus(Status.DONE_ERROR);
	}

	/**
	 * Erstellt ein Ausgabe-Ergebnis bereit.
	 * @param statistics	Ausgabe-Statistikdaten
	 * @param output	Ergebnisse in Binärform
	 * @param fileType	Dateityp der Binärform-Ergebnisse
	 * @param message	Meldung bzw. Abschluss-Status
	 */
	private void setResult(final Statistics statistics, final ByteArrayOutputStream output, final XMLTools.FileType fileType, final String message) {
		this.statistics=statistics;
		setResult(output,fileType,message);
	}

	/**
	 * Erstellt ein Ausgabe-Ergebnis bereit.
	 * @param output	Ergebnisse in Binärform
	 * @param fileType	Dateityp der Binärform-Ergebnisse
	 * @param message	Meldung bzw. Abschluss-Status
	 */
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

	/**
	 * Führt die Simulation eines einzelnen Modells durch.
	 * @param model	Zu simulierendes Modell
	 */
	private void runModel(final EditModel model) {
		if (!StartAnySimulator.isRemoveSimulateable(model)) {
			setError(SimulationServer.PREPARE_NO_REMOTE_MODEL);
			return;
		}

		final StartAnySimulator starter=new StartAnySimulator(model,null,null,null,Simulator.logTypeFull);
		final StartAnySimulator.PrepareError prepareError=starter.prepare();
		if (prepareError!=null) {
			setError(prepareError.error);
			return;
		}
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

	/**
	 * Führt eine Parameterreihensimulation durch.
	 * @param setup	Parameterreihen-Konfiguration
	 */
	private void runSeries(final ParameterCompareSetup setup) {
		runner=new ParameterCompareRunner(null,null,msg->addMessage(msg));
		try {
			final String error=runner.check(setup,null);
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
	 * Versucht ein Modell aus html-Daten zu laden
	 * @param data	html-Daten als Bytes
	 * @return	Liefert im Erfolgsfall das Modell, sonst <code>null</code>
	 * @see #run()
	 */
	private EditModel tryLoadHTML(final byte[] data) {
		if (data==null || data.length==0) return null;
		final String text=new String(data);

		boolean firstLine=true;
		boolean modelDataFollow=false;

		final String[] lines=text.split("\n");
		if (lines==null) return null;

		for (String line: lines) {
			if (firstLine) {
				if (!line.trim().equalsIgnoreCase("<!doctype html>")) return null;
			} else {
				if (modelDataFollow) {
					if (!line.trim().startsWith("data:application/xml;base64,")) return null;
					final String base64data=line.trim().substring("data:application/xml;base64,".length());
					final ByteArrayInputStream inputStream=new ByteArrayInputStream(Base64.getDecoder().decode(base64data));
					final EditModel model=new EditModel();
					if (model.loadFromStream(inputStream,FileType.AUTO)==null) return model;
				} else {
					if (line.trim().equalsIgnoreCase("QSModel")) modelDataFollow=true;
				}
			}
			firstLine=false;
		}

		return null;
	}

	/**
	 * Führt die Verarbeitung aus.<br>
	 * Diese Methode kann über einen anderen Thread ausgeführt werden.
	 */
	public void run() {
		if (status!=Status.WAITING || (input==null && originalModel==null)) {
			setStatus(Status.DONE_ERROR);
			return;
		}
		setStatus(Status.PROCESSING);

		if (originalModel==null) {
			/* Normale Betriebsart, Modell oder Parameterreihen-Setup laden */

			final EditModel htmlBasedModel=tryLoadHTML(input);
			if (htmlBasedModel!=null) {
				simulationType=SimulationType.MODEL;
				runModel(htmlBasedModel);
				return;
			}

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
		} else {
			/* Festes Modell, nur Parameter laden */

			if (inputTable==null && input==null) {
				setStatus(Status.DONE_ERROR);
				return;
			}

			if (input!=null) {
				inputTable=new MultiTable();
				if (!inputTable.loadStream(new ByteArrayInputStream(input))) {
					setStatus(Status.DONE_ERROR);
					return;
				}
				inputTableName="NetworkDataTable";
			}

			final EditModel changedEditModel=originalModel.modelLoadData.changeModel(originalModel,inputTable,inputTableName,true);
			if (changedEditModel==null) {
				simulationType=SimulationType.MODEL;
				runModel(originalModel);
				return;
			}

			simulationType=SimulationType.MODEL;
			changedEditModel.modelLoadData.setActive(false);
			runModel(changedEditModel);
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

	/**
	 * html-Daten für den interaktiven Statistik-Viewers
	 * werden in diesem Objekt für weitere Abfragen über
	 * {@link #getStatisticsViewer()} vorgehalten.
	 * @see #getStatisticsViewer()
	 */
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
		final StatisticViewerReport viewer=new StatisticViewerReport(panel.getStatisticNodeRoot(),statistics,statistics.editModel.name,0,null);
		try (ByteArrayOutputStream stream=new ByteArrayOutputStream()) {
			viewer.writeReportHTMLApp(stream);
			return viewerData=stream.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Liefert einen Text zu dem aktuellen Status.
	 * @return	Text zu dem aktuellen Status
	 * @see #getStatus()
	 */
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

	/**
	 * Liefert die Meldungen in JSON-formatierter Form zurück.
	 * @param indent	Einrückung der Zeilen (kann eine leere Zeichenkette sein, darf aber nicht <code>null</code> sein)
	 * @return	JSON-formatierte Meldungen
	 * @see #getStatusJSON(String)
	 */
	private String jsonFormatMessages(final String indent) {
		final StringBuilder result=new StringBuilder();
		result.append("[");
		for (int i=0;i<messages.size();i++) {
			if (i>0) result.append(",");
			result.append("\n"+indent+"  ");
			result.append("\"");
			result.append(messages.get(i));
			result.append("\"");
		}
		result.append("\n"+indent+"]");
		return result.toString();
	}

	/**
	 * Liefert den aktuellen Status des Task in Form eines JSON-Objektes.
	 * @param versionRest	REST-Versionskennung (z.B. "v1")
	 * @return	Status als JSON-Objekt
	 */
	public String getStatusJSON(final String versionRest) {
		return getStatusJSON("",true,false,versionRest);
	}

	/**
	 * Liefert eine JSON-Auflistung der möglichen Aktionen, die für den aktuellen Task ausgeführt werden können.
	 * @param indent	Einrückung der Zeilen (kann eine leere Zeichenkette sein, darf aber nicht <code>null</code> sein)
	 * @param versionRest	REST-Versionskennung (z.B. "v1")
	 * @return	Mögliche Aktionen als JSON-Objekt
	 */
	public String getOptions(final String indent, final String versionRest) {
		final StringBuilder status=new StringBuilder();

		status.append(indent+"\"links\": [\n");
		status.append(indent+"  {\"rel\": \"metadata\", \"link\": \"/"+versionRest+"/jobs/"+id+".json\", \"method\": \"head\"},\n");
		status.append(indent+"  {\"rel\": \"methods\", \"link\": \"/"+versionRest+"/jobs/"+id+".json\", \"method\": \"options\"},\n");
		if (this.status==Status.DONE_SUCCESS) {
			status.append(indent+"  {\"rel\": \"download-json\", \"link\": \"/"+versionRest+"/jobs/"+id+".json\", \"method\": \"get\"},\n");
			status.append(indent+"  {\"rel\": \"download-xml\", \"link\": \"/"+versionRest+"/jobs/"+id+".xml\", \"method\": \"get\"},\n");
			status.append(indent+"  {\"rel\": \"download-zip-xml\", \"link\": \"/"+versionRest+"/jobs/"+id+".zip\", \"method\": \"get\"},\n");
			status.append(indent+"  {\"rel\": \"download-tar.gz-xml\", \"link\": \"/"+versionRest+"/jobs/"+id+".tar.gz\", \"method\": \"get\"},\n");
			status.append(indent+"  {\"rel\": \"filter\", \"link\": \"/"+versionRest+"/jobs/"+id+".txt\", \"method\": \"post\"},\n");
		}
		status.append(indent+"  {\"rel\": \"delete\", \"link\": \"/"+versionRest+"/jobs/"+id+".json\", \"method\": \"delete\"}\n");
		status.append(indent+"]");

		return status.toString();
	}

	/**
	 * Liefert den aktuellen Status des Task in Form eines JSON-Objektes.
	 * @param indent	Einrückung der Zeilen (kann eine leere Zeichenkette sein, darf aber nicht <code>null</code> sein)
	 * @param showViewable	Soll eine Infozeile dazu ausgegeben werden, ob die Ergebnisse im Webinterface angezeigt werden können?
	 * @param showLinks	Links für mögliche Aktionen ausgeben
	 * @param versionRest	REST-Versionskennung (z.B. "v1")
	 * @return	Status als JSON-Objekt
	 */
	public String getStatusJSON(final String indent, final boolean showViewable, final boolean showLinks, final String versionRest) {
		final String viewable=(this.status==Status.DONE_SUCCESS && simulationType==SimulationType.MODEL)?"1":"0";

		final StringBuilder status=new StringBuilder();
		status.append(indent+"{\n");
		status.append(indent+"  \"id\": \""+id+"\",\n");
		status.append(indent+"  \"time\": \""+DateTools.formatUserDate(requestTime,true)+"\",\n");
		status.append(indent+"  \"status\": \""+this.status.id+"\",\n");
		status.append(indent+"  \"statusText\": \""+getStatusText()+"\",\n");
		if (showViewable) status.append(indent+"  \"viewable\": \""+viewable+"\",\n");
		status.append(indent+"  \"client\": \""+ip+"\",\n");
		status.append(indent+"  \"messages\": "+jsonFormatMessages(indent+"  "));
		if (showLinks) {
			status.append(",\n");
			status.append(getOptions(indent+"  ",versionRest)+"\n");
		} else {
			status.append("\n");
		}

		status.append(indent+"}");
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
