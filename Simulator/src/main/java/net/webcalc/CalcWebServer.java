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

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import language.Language;
import language.LanguageStaticLoader;
import language.Messages_Java11;
import mathtools.MultiTable;
import mathtools.NumberTools;
import mathtools.Table;
import net.web.HandlerFavicon;
import net.web.HandlerText;
import net.web.WebServer;
import net.web.WebServerHandler;
import net.web.WebServerResponse;
import simulator.AnySimulator;
import simulator.Simulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import tools.SetupData;
import xml.XMLTools;

/**
 * Webserver, der Rechenanfragen per Browser entgegen nimmt.
 * @author Alexander Herzog
 * @see WebServer
 */
public class CalcWebServer extends WebServer {
	/** Sichert den Zugriff auf die Liste der aktiven Aufgaben ab */
	private final ReentrantLock lock;
	/** Liste der aktiven Aufgaben */
	private final List<CalcFuture> list;
	/** System zur Ausführung von Aufgaben */
	private final ExecutorService executor;
	/** Zähler für die Aufgaben */
	private int idCounter;
	/** Festgelegtes Modell (wird <code>null</code> übergeben, so können beliebige Modelle geladen werden) */
	private final EditModel model;

	/**
	 * Konstruktor der Klasse<br>
	 * Die Konfiguration (Start/Stop des Server, Statusabfrage usw.) erfolgt über die Basisklasse {@link WebServer}.
	 * @param model	Festgelegtes Modell (wird <code>null</code> übergeben, so können beliebige Modelle geladen werden)
	 */
	public CalcWebServer(final EditModel model) {
		super();
		lock=new ReentrantLock();
		list=new ArrayList<>();
		this.model=model;

		executor=new ThreadPoolExecutor(0,1,5000,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(),new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,"WebSim Request Processor");
			}
		});

		idCounter=0;

		final List<WebServerHandler> handlers=getHandlersList();
		handlers.add(new HandlerFavicon("/ui/res/Symbol.ico"));
		if (model==null) {
			handlers.add(new HandlerText("/","res/index_model_%LANG%.html",this,WebServerResponse.Mime.HTML));
		} else {
			handlers.add(new HandlerText("/","res/index_data_%LANG%.html",this,WebServerResponse.Mime.HTML));
			handlers.add(new HandlerText("/direct_info","res/index_data_info_%LANG%.html",this,WebServerResponse.Mime.HTML));
		}
		handlers.add(new HandlerText("/css.css","res/css.css",this,WebServerResponse.Mime.CSS));
		handlers.add(new HandlerText("/main.js","res/js_%LANG%.js",this,WebServerResponse.Mime.JS));
		handlers.add(new HandlerPostModel("/upload","model",info->processFile(info)));
		handlers.add(new HandlerText("/status",()->getStatus(),WebServerResponse.Mime.JSON,true));
		handlers.add(new HandlerProcessID("/delete/",request->deleteTask(request)));
		handlers.add(new HandlerProcessID("/download/",request->downloadResults(request)));
		handlers.add(new HandlerProcessID("/view/",request->viewResults(request)));
		handlers.add(new HandlerProcessID("/language/",request->setLanguage(request)));

		if (model!=null) {
			handlers.add(new HandlerProcessID("/direct/",request->simDirect(request)));
		}
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Die Konfiguration (Start/Stop des Server, Statusabfrage usw.) erfolgt über die Basisklasse {@link WebServer}.
	 */
	public CalcWebServer() {
		this(null);
	}

	/**
	 * Liefert eine Textbeschreibung mit Daten zum Simulationsrechner.
	 * @return	Textbeschreibung mit Daten zum Simulationsrechner
	 * @see #getStatus()
	 */
	private String getSystemStatus() {
		final StringBuilder status=new StringBuilder();

		status.append(Language.tr("InfoDialog.JavaVersion")+": "+System.getProperty("java.version")+" ("+System.getProperty("java.vm.name")+")");
		status.append(", ");
		final long l1=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		final long l2=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
		status.append(Language.tr("InfoDialog.MemoryUsage")+": "+NumberTools.formatLong((l1+l2)/1024/1024)+" MB");
		status.append(", ");
		status.append(Language.tr("InfoDialog.MemoryAvailable")+": "+NumberTools.formatLong(Runtime.getRuntime().maxMemory()/1024/1024)+" MB");

		return status.toString();
	}


	/**
	 * Liefert ein JSON-Objekt mit Daten zum aktuellen Status (System-Status und gewählte Sprache).
	 * @return	JSON-Objekt mit Daten zum aktuellen Status
	 */
	private String getStatus() {
		final StringBuilder status=new StringBuilder();

		lock.lock();
		try {
			status.append("[\n");
			for (int i=0;i<list.size();i++) {
				final CalcFuture future=list.get(i);
				status.append(future.getStatusJSON());
				status.append(",\n");
			}
			status.append("{\"system\": \""+getSystemStatus()+"\"},\n");
			status.append("{\"language\": \""+Language.getCurrentLanguage()+"\"}\n");

			status.append("]\n");
		} finally {
			lock.unlock();
		}

		return status.toString();
	}

	/**
	 * Verarbeitet eine empfangene Datei.
	 * @param info	Datensatz zu der empfangenen Datei
	 */
	private void processFile(final HandlerPostModel.UploadInfo info) {
		lock.lock();
		try {
			idCounter++;
			final CalcFuture future=new CalcFuture(idCounter,info.file,info.ip,info.origFileName,model);
			list.add(future);
			executor.submit(()->future.run());
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Löscht einen Auftrag aus der Liste.
	 * @param request	ID des zu löschenden Auftrags
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse deleteTask(final String request) {
		final Long L=NumberTools.getNotNegativeLong(request);
		if (L!=null) {
			lock.lock();
			try {
				for (int i=0;i<list.size();i++) if (list.get(i).getId()==L) {
					final CalcFuture future=list.remove(i);
					future.cancel();
					break;
				}
			} finally {
				lock.unlock();
			}
		}

		final WebServerResponse response=new WebServerResponse();
		response.setText("",WebServerResponse.Mime.TEXT,false);
		return response;
	}

	/**
	 * Liefert die Ergebnisdaten eines Auftrags.
	 * @param request	ID des Auftrags
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse downloadResults(final String request) {
		final Long L=NumberTools.getNotNegativeLong(request);
		if (L!=null) {
			lock.lock();
			try {
				for (int i=0;i<list.size();i++) if (list.get(i).getId()==L && list.get(i).getStatus()==CalcFuture.Status.DONE_SUCCESS) {
					return getBinaryDataResponse(list.get(i).getBytes(),list.get(i).getXMLFileType());
				}
			} finally {
				lock.unlock();
			}
		}
		return null;
	}

	/**
	 * Erstellt ein Webserver-Antwort-Objekt mit den angegebenen Binärdaten als Inhalt.
	 * @param data	Binärdaten, die als Download angeboten werden sollen
	 * @param fileType	Typ der Binärdaten
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse getBinaryDataResponse(final byte[] data, final XMLTools.FileType fileType) {
		final WebServerResponse response=new WebServerResponse();
		switch (fileType) {
		case CRYPT_XML:
			response.setXML(data,"results.cs");
			break;
		case JSON:
			response.setJSON(data,"results.json");
			break;
		case TAR_XML:
			response.setTARGZ(data,"results.tar.gz");
			break;
		case XML:
			response.setXML(data,"results.xml");
			break;
		case ZIP_XML:
			response.setZIP(data,"results.zip");
			break;
		default:
			response.setXML(data,"results.xml");
			break;
		}
		return response;
	}

	/**
	 * Zeigt die Ergebnisse einer Simulation im Online-Viewer an
	 * @param request	ID des Auftrags zu dem die Ergebnisse angezeigt werden sollen
	 * @return	Webserver-Rückgabe (Online-Viewer)
	 */
	private WebServerResponse viewResults(final String request) {
		final Long L=NumberTools.getNotNegativeLong(request);
		if (L!=null) {
			lock.lock();
			try {
				for (int i=0;i<list.size();i++) if (list.get(i).getId()==L && list.get(i).getStatus()==CalcFuture.Status.DONE_SUCCESS) {
					final byte[] data=list.get(i).getStatisticsViewer();
					if (data!=null) {
						final WebServerResponse response=new WebServerResponse();
						response.setHTML(data);
						return response;
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return null;
	}

	/**
	 * Stellt die Sprache für den Webserver ein.
	 * @param request	Neue Sprache
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse setLanguage(final String request) {
		if (request==null || request.trim().isEmpty()) return null;
		final String lang=request.trim().toLowerCase();
		if (!Language.isSupportedLanguage(lang)) return null;

		SetupData setup=SetupData.getSetup();
		if (!setup.language.equals(lang)) {
			setup.language=lang;
			setup.saveSetup();
			Language.init(lang);
			LanguageStaticLoader.setLanguage();
			if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();
		}

		final WebServerResponse response=new WebServerResponse();
		response.setText(lang,false);
		return response;
	}

	/**
	 * Interpretiert die URL-Parameter als Zuweisungen für eine Tabelle
	 * die dann als externe Daten in ein Modell geladen werden kann.
	 * @param request	URL-Parameter die mehrere durch "&amp;" getrennte Zuweisungen der Form "A1=123" enthalten
	 * @return	Tabelle in die die Zuweisungen eingetragen sind
	 */
	private Table getAssignmentsTable(final String request) {
		final String[] parts=request.split("&");
		final Table table=new Table();
		for (String part: parts) {
			final String[] s=part.split("=");
			if (s.length!=2) continue;
			final int[] cell=Table.cellIDToNumbers(s[0]);
			if (cell==null) continue;
			final Double D=NumberTools.getDouble(s[1]);
			if (D==null) continue;
			table.setValue(cell[0],cell[1],NumberTools.formatNumberMax(D));
		}

		return table;
	}

	/**
	 * Simuliert das festvorgegebene Modell mit den in den Parametern übergebenen Werten
	 * und liefert das Ergebnis unmittelbar zurück.
	 * @param request	Werte die in das Modell geladen werden sollen
	 * @return	Liefert im Erfolgsfall die Statistikergebnisse, sonst eine Fehlermeldung
	 */
	private WebServerResponse simDirect(final String request) {
		/* Existiert ein Modell? */
		if (model==null) {
			final WebServerResponse response=new WebServerResponse();
			response.setText(Language.tr("CalcWebServer.NoModel"),WebServerResponse.Mime.TEXT,true);
			return response;
		}

		/* Anfrage aufbereiten */
		if (request==null || request.trim().isEmpty()) return null;
		final Table table=getAssignmentsTable(request);

		/* Externe Daten laden */
		final EditModel changedEditModel;
		final MultiTable multi=new MultiTable();
		multi.add("",table);
		changedEditModel=model.modelLoadData.changeModel(model,multi,"URLdata",true);

		/* Simulation durchführen */
		final StartAnySimulator starter=new StartAnySimulator(changedEditModel,null,null,Simulator.logTypeFull);
		final String prepareError=starter.prepare();
		if (prepareError!=null) {
			final WebServerResponse response=new WebServerResponse();
			response.setText(prepareError,WebServerResponse.Mime.TEXT,true);
			return response;
		}
		final AnySimulator simulator=starter.start();
		simulator.finalizeRun();

		/* Ergebnisse bereitstellen */
		final Statistics statistics=simulator.getStatistic();
		final ByteArrayOutputStream output=new ByteArrayOutputStream();
		final XMLTools.FileType fileType=SetupData.getSetup().defaultSaveFormatStatistics.fileType;
		statistics.saveToStream(output,fileType);
		return getBinaryDataResponse(output.toByteArray(),fileType);
	}

	/**
	 * Singleton-Instanz dieser Klasse
	 * @see #getInstance()
	 */
	private static CalcWebServer instance;

	/**
	 * Liefert eine Singleton-Instanz dieser Klasse
	 * @return	Instanz dieser Klasse
	 */
	public static synchronized CalcWebServer getInstance() {
		if (instance==null) instance=new CalcWebServer();
		return instance;
	}
}
