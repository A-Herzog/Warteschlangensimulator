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
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Element;

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
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.js.JSRunDataFilter;
import simulator.AnySimulator;
import simulator.Simulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import tools.SetupData;
import ui.MainFrame;
import ui.MainPanel;
import ui.script.ScriptPanel;
import ui.statistics.FilterList;
import ui.statistics.FilterListFormat;
import ui.statistics.FilterListRecord;
import xml.XMLTools;
import xml.XMLTools.FileType;

/**
 * Webserver, der Rechenanfragen per Browser entgegen nimmt.
 * @author Alexander Herzog
 * @see WebServer
 */
public class CalcWebServer extends WebServer {
	/** Aktuelle Version für REST-Anfragen */
	private static final String LATEST_REST_VERSION="v1";

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
	 * Inhaltstyp für die REST-Rückmeldung
	 */
	enum ResponseMode {
		/** Rückmeldung als json-Text */
		JSON,
		/** Rückmeldung als xml-Text */
		XML
	}

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

		executor=new ThreadPoolExecutor(0,1,5000,TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>(),(ThreadFactory)r->new Thread(r,"WebSim Request Processor"));

		idCounter=0;

		final List<WebServerHandler> handlers=getHandlersList();

		/* Webinterface */
		handlers.add(new HandlerFavicon("/ui/res/Symbol.ico"));
		if (model==null) {
			handlers.add(new HandlerText("/","res/index_model_%LANG%.html",this,WebServerResponse.Mime.HTML));
		} else {
			handlers.add(new HandlerText("/","res/index_data_%LANG%.html",this,WebServerResponse.Mime.HTML));
		}
		handlers.add(new HandlerText("/rest_info","res/index_info_%LANG%.html",this,WebServerResponse.Mime.HTML));
		handlers.add(new HandlerText("/css.css","res/css.css",this,WebServerResponse.Mime.CSS));
		handlers.add(new HandlerText("/main.js","res/js_%LANG%.js",this,WebServerResponse.Mime.JS));
		handlers.add(new HandlerPost("/upload","model",info->processFile(info)));
		handlers.add(new HandlerText("/status",param->getWebStatus(),WebServerResponse.Mime.JSON,true));
		handlers.add(new HandlerProcessID("/delete/",request->deleteTask(request)));
		handlers.add(new HandlerProcessID("/download/",request->downloadResults(request)));
		handlers.add(new HandlerProcessID("/view/",request->viewResults(request)));
		handlers.add(new HandlerProcessID("/language/",request->setLanguage(request)));

		/* Direkte Parametrisierung und Simulation */
		if (model!=null) {
			handlers.add(new HandlerProcessID("/direct/",request->simDirect(request)));
		}

		/* Rest */
		addRestHandlers(handlers,LATEST_REST_VERSION);
		addRestHandlers(handlers,"latest");
		addRestHandlers(handlers,null); /* Latest version */
	}

	/**
	 * Fügt die REST-Handler zu der Liste der URL-Handler hinzu
	 * @param handlers	Liste der URL-Handler
	 * @param versionRest	Zu verwendende REST-Version (z.B. "v1" oder <code>null</code> für "jeweils neuste Version")
	 */
	private void addRestHandlers(final List<WebServerHandler> handlers, final String versionRest) {
		final String restURL=(versionRest==null || versionRest.trim().isEmpty())?"":("/"+versionRest);

		if (model==null) {
			handlers.add(new HandlerPost(restURL+"/jobs.json","model",info->processFile(info),ResponseMode.JSON));
			handlers.add(new HandlerPost(restURL+"/jobs.xml","model",info->processFile(info),ResponseMode.XML));
		} else {
			handlers.add(new HandlerPost(restURL+"/jobs.json","table",info->processFile(info),ResponseMode.JSON));
			handlers.add(new HandlerPost(restURL+"/jobs.xml","table",info->processFile(info),ResponseMode.XML));
		}
		handlers.add(new HandlerText(restURL+"/jobs.json",param->byRESTGetStatus(param,ResponseMode.JSON),WebServerResponse.Mime.JSON,true));
		handlers.add(new HandlerText(restURL+"/jobs.xml",param->byRESTGetStatus(param,ResponseMode.XML),WebServerResponse.Mime.XML,true));
		handlers.add(new HandlerHead(restURL+"/jobs/",request->byRESTTaskMetaInfo(request)));
		handlers.add(new HandlerOptions(restURL+"/jobs/",request->byRESTTaskOptions(request)));
		handlers.add(new HandlerDelete(restURL+"/jobs/",request->byRESTDeleteTask(request)));
		handlers.add(new HandlerProcessID(restURL+"/jobs/",request->byRESTDownloadResults(request)));
		handlers.add(new HandlerPut(restURL+"/language/",request->byRESTSetLanguage(request)));
		handlers.add(new HandlerPost(restURL+"/jobs/",(request,info)->byRESTFilterResults(request,info),"filter"));
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
	 * @see #getWebStatus()
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
	private String getWebStatus() {
		final StringBuilder status=new StringBuilder();

		lock.lock();
		try {
			status.append("[\n");
			for (int i=0;i<list.size();i++) {
				final CalcFuture future=list.get(i);
				status.append(future.getStatusJSON(LATEST_REST_VERSION));
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
	 * Liefert ein REST-JSON-Objekt mit Daten zum aktuellen Status.
	 * @param param	Weitere Aufrufparameter nach "?"
	 * @param responseMode	Gewünschter Inhaltstyp für die Antwort
	 * @return	REST-Antwortobjekt mit Daten zum aktuellen Status
	 */
	private String byRESTGetStatus(final String param, final ResponseMode responseMode) {
		final StringBuilder status=new StringBuilder();

		int offset=0;
		int limit=-1;
		if (param!=null) {
			for (String cmd: param.split("&")) {
				final String[] parts=cmd.split("=");
				if (parts.length!=2) continue;
				if (parts[0].toLowerCase().equals("offset")) {
					final Integer I=NumberTools.getNotNegativeInteger(parts[1]);
					if (I!=null) offset=I;
				}
				if (parts[0].toLowerCase().equals("limit")) {
					final Long L=NumberTools.getPositiveLong(parts[1]);
					if (L!=null) limit=L.intValue();
				}
			}
		}

		lock.lock();
		try {
			status.append("{\n");
			status.append("  \"jobsCount\": {\n");
			status.append("    \"available\": \""+list.size()+"\",\n");
			status.append("    \"startAt\": \""+offset+"\",\n");
			status.append("    \"outputCount\": \""+((limit>0)?Math.min(limit,Math.max(0,list.size()-offset)):list.size())+"\"\n");
			status.append("  },\n");

			status.append("  \"jobs\": [\n");
			int count=0;
			for (int i=offset;i<list.size();i++) {
				if (limit>0 && count>=limit) break;
				count++;
				final CalcFuture future=list.get(i);
				status.append(future.getStatusJSON("    ",false,true,LATEST_REST_VERSION));
				if (i<list.size()-1) status.append(",\n"); else status.append("\n");
			}
			status.append("  ],\n");

			status.append("  \"simulator\": {\n");
			status.append("    \"name\": \""+MainFrame.PROGRAM_NAME+"\",\n");
			status.append("    \"version\": \""+MainPanel.VERSION+"\",\n");
			status.append("    \"language\": \""+Language.getCurrentLanguage()+"\"\n");
			status.append("  },\n");
			final long l1=ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
			final long l2=ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
			status.append("  \"java\": {\n");
			status.append("    \"name\": \""+System.getProperty("java.vm.name")+"\",\n");
			status.append("    \"version\": \""+System.getProperty("java.version")+"\",\n");
			status.append("    \"memoryUsed\": \""+NumberTools.formatLong((l1+l2)/1024/1024)+" MB\",\n");
			status.append("    \"memoryAvailable\": \""+NumberTools.formatLong(Runtime.getRuntime().maxMemory()/1024/1024)+" MB"+"\"\n");
			status.append("  }\n");

			status.append("}\n");
		} finally {
			lock.unlock();
		}

		switch (responseMode) {
		case JSON:
			return status.toString();
		case XML:
			return CalcWebServerTools.jsonToXmlString("status",status.toString());
		default:
			return status.toString();
		}
	}

	/**
	 * Liefert ein REST-JSON-Objekt mit Daten zu einem Auftrag.
	 * @param request	ID des Auftrags zu dem Daten geliefert werden sollen
	 * @return	REST-JSON-Objekt mit Daten zu einem Auftrag
	 */
	private WebServerResponse byRESTTaskMetaInfo(String request) {
		if (request==null) return null;

		ResponseMode responseMode=null;
		if (request.toLowerCase().endsWith(".json")) {
			responseMode=ResponseMode.JSON;
			request=request.substring(0,request.length()-5);
		}
		if (request.toLowerCase().endsWith(".xml")) {
			responseMode=ResponseMode.XML;
			request=request.substring(0,request.length()-4);
		}
		if (responseMode==null) return null;

		final Long L=NumberTools.getPositiveLong(request);
		if (L==null) return null;

		for (int i=0;i<list.size();i++) {
			final CalcFuture future=list.get(i);
			if (future.getId()==L.longValue()) {
				final WebServerResponse response=new WebServerResponse();
				final String json=future.getStatusJSON("",false,true,LATEST_REST_VERSION);
				switch (responseMode) {
				case JSON:
					response.setJSON(json,true);
					break;
				case XML:
					response.setXML(CalcWebServerTools.jsonToXml("jobStatus",json),"jobStatus.xml");
					break;
				default:
					response.setJSON(json,true);
					break;
				}

				return response;
			}
		}

		return null;
	}

	/**
	 * Liefert ein REST-JSON-Objekt mit Daten zu möglichen Aktionen zu einem Auftrag.
	 * @param request	ID des Auftrags zu dem Daten geliefert werden sollen
	 * @return	REST-JSON-Objekt mit Daten zu möglichen Aktionen zu einem Auftrag
	 */
	private WebServerResponse byRESTTaskOptions(String request) {
		if (request==null) return null;

		ResponseMode responseMode=null;
		if (request.toLowerCase().endsWith(".json")) {
			responseMode=ResponseMode.JSON;
			request=request.substring(0,request.length()-5);
		}
		if (request.toLowerCase().endsWith(".xml")) {
			responseMode=ResponseMode.XML;
			request=request.substring(0,request.length()-4);
		}
		if (responseMode==null) return null;

		final Long L=NumberTools.getPositiveLong(request);
		if (L==null) return null;

		for (int i=0;i<list.size();i++) {
			final CalcFuture future=list.get(i);
			if (future.getId()==L.longValue()) {
				final WebServerResponse response=new WebServerResponse();
				final String json="{\n"+future.getOptions("  ",LATEST_REST_VERSION)+"\n}\n";
				switch (responseMode) {
				case JSON:
					response.setJSON(json,true);
					break;
				case XML:
					response.setXML(CalcWebServerTools.jsonToXmlString("options",json),"options.xml");
					break;
				default:
					response.setJSON(json,true);
					break;

				}

				return response;
			}
		}

		return null;
	}

	/**
	 * Verarbeitet eine empfangene Datei.
	 * @param info	Datensatz zu der empfangenen Datei
	 * @return	ID des neuen Auftrags
	 */
	private int processFile(final HandlerPost.UploadInfo info) {
		lock.lock();
		try {
			idCounter++;
			final CalcFuture future=new CalcFuture(idCounter,info.file,info.ip,info.origFileName,model);
			list.add(future);
			executor.submit(()->future.run());
			return idCounter;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Löscht einen Auftrag aus der Liste.
	 * @param request	ID des zu löschenden Auftrags
	 * @return	Antwort-json-Objekt
	 */
	private String deleteTaskString(final String request) {
		final Long L=NumberTools.getNotNegativeLong(request);
		boolean deleted=false;
		final int id;
		if (L!=null) {
			id=L.intValue();
			lock.lock();
			try {
				for (int i=0;i<list.size();i++) if (list.get(i).getId()==L) {
					final CalcFuture future=list.remove(i);
					future.cancel();
					deleted=true;
					break;
				}
			} finally {
				lock.unlock();
			}
		} else {
			id=-1;
		}

		final StringBuilder json=new StringBuilder();
		json.append("{\n");
		json.append("  \"job\": \""+id+"\",\n");
		json.append("  \"deleted\": \""+(deleted?"true":"false")+"\"\n");
		json.append("}\n");
		return json.toString();
	}

	/**
	 * Löscht einen Auftrag aus der Liste.
	 * @param request	ID des zu löschenden Auftrags
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse deleteTask(final String request) {
		final WebServerResponse response=new WebServerResponse();
		response.setJSON(deleteTaskString(request),true);
		return response;
	}

	/**
	 * Löscht einen Auftrag aus der Liste.
	 * @param request	ID des zu löschenden Auftrags
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse byRESTDeleteTask(String request) {
		if (request==null) return deleteTask("");

		ResponseMode responseMode=null;
		if (request.toLowerCase().endsWith(".json")) {
			responseMode=ResponseMode.JSON;
			request=request.substring(0,request.length()-5);
		}
		if (request.toLowerCase().endsWith(".xml")) {
			responseMode=ResponseMode.XML;
			request=request.substring(0,request.length()-4);
		}

		if (responseMode==null) return deleteTask("");

		switch (responseMode) {
		case JSON:
			return deleteTask(request);
		case XML:
			final WebServerResponse response=new WebServerResponse();
			response.setXML(CalcWebServerTools.jsonToXml("deleteTask",deleteTaskString(request)),"deleteTask.xml");
			return response;
		default:
			return deleteTask(request);
		}
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
	 * Liefert die Ergebnisdaten eines Auftrags.
	 * @param request	ID des Auftrags
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse byRESTDownloadResults(final String request) {
		/* Dateityp bestimmen */
		XMLTools.FileType fileType=XMLTools.FileType.XML;
		final String requestUpper=request.toUpperCase();
		if (requestUpper.endsWith(".ZIP") || requestUpper.endsWith(".XMZ") || requestUpper.endsWith(".BIN")) fileType=FileType.ZIP_XML;
		if (requestUpper.endsWith(".TAR") || requestUpper.endsWith(".TAR.GZ")  || requestUpper.endsWith(".TARGZ")) fileType=FileType.TAR_XML;
		if (requestUpper.endsWith(".JSON") || requestUpper.endsWith(".JS")) fileType=FileType.JSON;

		/* ID bestimmen */
		final int index=request.lastIndexOf('.');
		if (index<0) return null;
		String idString=request.substring(0,index);
		if (idString.toLowerCase().endsWith(".tar")) idString=idString.substring(0,idString.length()-4); /* ".tar.gz" wird über lastIndexOf('.') nicht vollständig entfernt. */

		final Long L=NumberTools.getNotNegativeLong(idString);
		if (L==null) return null;
		final int id=L.intValue();

		/* Ausgabe */
		lock.lock();
		try {
			for (int i=0;i<list.size();i++) if (list.get(i).getId()==id && list.get(i).getStatus()==CalcFuture.Status.DONE_SUCCESS) {
				return getBinaryDataResponse(list.get(i).getBytes(),list.get(i).getXMLFileType(),fileType);
			}
		} finally {
			lock.unlock();
		}
		return null;
	}

	/**
	 * Filtert die Ergebnisse einer Aufgabe mit Hilfe eines Filtersskripts.
	 * @param filterCommands	Filterbefehle
	 * @param statistic	Statistikdaten
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse filterResult(final String filterCommands, final Statistics statistic) {
		boolean error=false;
		String result=null;

		final FilterList filterList=new FilterList();
		if (filterList.load(filterCommands)) {
			final FilterListFormat format=new FilterListFormat();
			final StringBuilder sb=new StringBuilder();
			final List<FilterListRecord> list=filterList.getList();
			for (int i=0;i<list.size();i++) {
				sb.append(list.get(i).process(statistic,format));
			}
			if (sb.length()>0) result=sb.toString();
		}

		if (result==null) switch (ScriptPanel.getScriptType(filterCommands)) {
		case Javascript:
			final JSRunDataFilter dataFilter=new JSRunDataFilter(statistic.saveToXMLDocument(),statistic.loadedStatistics);
			dataFilter.run(filterCommands);
			result=dataFilter.getResults();
			break;
		case Java:
			final DynamicRunner runner=DynamicFactory.getFactory().load(filterCommands);
			if (runner.getStatus()!=DynamicStatus.OK) {
				error=true;
				result=DynamicFactory.getLongStatusText(runner);
			} else {
				final StringBuilder sb=new StringBuilder();
				runner.parameter.output=new OutputImpl(line->sb.append(line),false);
				runner.parameter.statistics=new StatisticsImpl(line->sb.append(line),statistic.saveToXMLDocument(),statistic.loadedStatistics,false);
				runner.run();
				if (runner.getStatus()!=DynamicStatus.OK) {
					error=true;
					result=DynamicFactory.getLongStatusText(runner);
				} else {
					result=sb.toString();
				}
			}
			break;
		}

		/* Ausgabe der Ergebnisse */

		final StringBuilder resultBuilder=new StringBuilder();
		if (error) {
			resultBuilder.append(Language.tr("CommandLine.Filter.Done.Error")+":\n");
			resultBuilder.append(result);
			resultBuilder.append("\n");
		} else {
			if (result==null) {
				resultBuilder.append(Language.tr("CommandLine.Filter.Done.Error")+":\n");
				resultBuilder.append(Language.tr("CommandLine.Filter.Done.Error.CouldNotProcess"));
				resultBuilder.append("\n");
			} else {
				resultBuilder.append(result.toString());
			}
		}

		final WebServerResponse response=new WebServerResponse();
		response.setText(resultBuilder.toString(),true);
		return response;
	}

	/**
	 * Filtert die Ergebnisse einer Aufgabe mit Hilfe eines Filtersskripts.
	 * @param request	Anfrage (enthält die ID der Aufgabe)
	 * @param info	Hochgeladenes Filterskript
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse byRESTFilterResults(final String request, final HandlerPost.UploadInfo info) {
		/* Filter laden */
		final String filterCommands=Table.loadTextFromFile(info.file);
		if (filterCommands==null) return null;

		/* ID bestimmen */
		final String requestUpper=request.toUpperCase();
		if (!requestUpper.endsWith(".TXT")) return null;
		final Long L=NumberTools.getNotNegativeLong(request.substring(0,request.length()-4));
		if (L==null) return null;
		final int id=L.intValue();

		/* Ausgabe */
		lock.lock();
		try {
			for (int i=0;i<list.size();i++) if (list.get(i).getId()==id && list.get(i).getStatus()==CalcFuture.Status.DONE_SUCCESS) {
				final XMLTools loader=new XMLTools(new ByteArrayInputStream(list.get(i).getBytes()),list.get(i).getXMLFileType());
				final Element root=loader.load();
				if (root==null) return null;
				final Statistics statistics=new Statistics();
				if (statistics.loadFromXML(root)!=null) return null;
				return filterResult(filterCommands,statistics);
			}
		} finally {
			lock.unlock();
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
	 * Erstellt ein Webserver-Antwort-Objekt mit den angegebenen Binärdaten als Inhalt.
	 * @param data	Binärdaten, die als Download angeboten werden sollen
	 * @param currentFileType	Typ in dem die Binärdaten momentan vorliegen
	 * @param newFileType	Für die Ausgabe gewünschtes Format für die Binärdaten
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse getBinaryDataResponse(final byte[] data, final XMLTools.FileType currentFileType, final XMLTools.FileType newFileType) {
		/* Konvertieren nicht nötig? */
		if (currentFileType==newFileType) return getBinaryDataResponse(data,currentFileType);

		/* Im alten Format laden */
		final XMLTools loader=new XMLTools(new ByteArrayInputStream(data),currentFileType);
		final Element root=loader.load();
		if (root==null) return null;

		/* Im neuen Format speichern */
		final ByteArrayOutputStream resultBytes=new ByteArrayOutputStream();
		final XMLTools saver=new XMLTools(resultBytes,newFileType);
		if (!saver.save(root)) return null;

		/* Ausgeben */
		return getBinaryDataResponse(resultBytes.toByteArray(),newFileType);
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
	 * Erstellt ein Antwortobjekt für die Spracheinstellung.
	 * @param responseMode	Gewünschter Inhaltstyp für die Antwort
	 * @param oldLanguage	Bisherige Simulatorsprache
	 * @param newLanguage	Neue	Simulatorsprache
	 * @param statusOk	War die übergebene neue Sprache gültig?
	 * @return	Liefert das Antwortobjekt für {@link #byRESTSetLanguage(String)}
	 * @see #byRESTSetLanguage(String)
	 */
	private WebServerResponse buildLanguageResponse(final ResponseMode responseMode, final String oldLanguage, final String newLanguage, final boolean statusOk) {
		final StringBuilder json=new StringBuilder();
		json.append("{\n");
		json.append("  \"languageOld\": \""+oldLanguage+"\",\n");
		json.append("  \"languageNew\": \""+newLanguage+"\",\n");
		if (statusOk) json.append("  \"status\": \"ok\"\n"); else json.append("  \"status\": \"not changed\"\n");
		json.append("}\n");

		final WebServerResponse response=new WebServerResponse();
		switch (responseMode) {
		case JSON:
			response.setJSON(json.toString(),true);
			break;
		case XML:
			response.setXML(CalcWebServerTools.jsonToXml("languageSettings",json.toString()),"languageSettings.xml");
			break;
		default:
			response.setJSON(json.toString(),true);
			break;

		}

		return response;
	}

	/**
	 * Stellt die Sprache für den Webserver ein.
	 * @param request	Neue Sprache
	 * @return	Webserver-Antwort-Objekt
	 */
	private WebServerResponse byRESTSetLanguage(String request) {
		final SetupData setup=SetupData.getSetup();
		final String languageOld=setup.language;

		if (request==null || request.trim().isEmpty()) return buildLanguageResponse(ResponseMode.JSON,languageOld,"",false);

		ResponseMode responseMode=null;
		if (request.toLowerCase().endsWith(".json")) {
			responseMode=ResponseMode.JSON;
			request=request.substring(0,request.length()-5);
		}
		if (request.toLowerCase().endsWith(".xml")) {
			responseMode=ResponseMode.XML;
			request=request.substring(0,request.length()-4);
		}

		if (responseMode==null) return buildLanguageResponse(ResponseMode.JSON,languageOld,"",false);

		final String lang=request.trim().toLowerCase();
		if (!Language.isSupportedLanguage(lang)) return buildLanguageResponse(responseMode,languageOld,lang,false);

		if (!setup.language.equals(lang)) {
			setup.language=lang;
			setup.saveSetup();
			Language.init(lang);
			LanguageStaticLoader.setLanguage();
			if (Messages_Java11.isFixNeeded()) Messages_Java11.setupMissingSwingMessages();
		}

		return buildLanguageResponse(responseMode,languageOld,lang,true);
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
		final StartAnySimulator.PrepareError prepareError=starter.prepare();
		if (prepareError!=null) {
			final WebServerResponse response=new WebServerResponse();
			response.setText(prepareError.error,WebServerResponse.Mime.TEXT,true);
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
