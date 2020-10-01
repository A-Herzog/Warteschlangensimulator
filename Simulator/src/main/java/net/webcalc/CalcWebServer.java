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
import mathtools.NumberTools;
import net.web.HandlerFavicon;
import net.web.HandlerText;
import net.web.WebServer;
import net.web.WebServerHandler;
import net.web.WebServerResponse;
import simulator.editmodel.EditModel;
import tools.SetupData;

/**
 * Webserver, der Rechenanfragen per Browser entgegen nimmt.
 * @author Alexander Herzog
 * @see WebServer
 */
public class CalcWebServer extends WebServer {
	private final ReentrantLock lock;
	private final List<CalcFuture> list;
	private final ExecutorService executor;
	private int idCounter;
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
		}
		handlers.add(new HandlerText("/css.css","res/css.css",this,WebServerResponse.Mime.CSS));
		handlers.add(new HandlerText("/main.js","res/js_%LANG%.js",this,WebServerResponse.Mime.JS));
		handlers.add(new HandlerPostModel("/upload","model",info->processFile(info)));
		handlers.add(new HandlerText("/status",()->getStatus(),WebServerResponse.Mime.JSON,true));
		handlers.add(new HandlerProcessID("/delete/",request->deleteTask(request)));
		handlers.add(new HandlerProcessID("/download/",request->downloadResults(request)));
		handlers.add(new HandlerProcessID("/view/",request->viewResults(request)));
		handlers.add(new HandlerProcessID("/language/",request->setLanguage(request)));
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Die Konfiguration (Start/Stop des Server, Statusabfrage usw.) erfolgt über die Basisklasse {@link WebServer}.
	 */
	public CalcWebServer() {
		this(null);
	}

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

	private WebServerResponse downloadResults(final String request) {
		final Long L=NumberTools.getNotNegativeLong(request);
		if (L!=null) {
			lock.lock();
			try {
				for (int i=0;i<list.size();i++) if (list.get(i).getId()==L && list.get(i).getStatus()==CalcFuture.Status.DONE_SUCCESS) {
					final WebServerResponse response=new WebServerResponse();
					switch (list.get(i).getXMLFileType()) {
					case CRYPT_XML:
						response.setXML(list.get(i).getBytes(),"results.cs");
						break;
					case JSON:
						response.setJSON(list.get(i).getBytes(),"results.json");
						break;
					case TAR_XML:
						response.setTARGZ(list.get(i).getBytes(),"results.tar.gz");
						break;
					case XML:
						response.setXML(list.get(i).getBytes(),"results.xml");
						break;
					case ZIP_XML:
						response.setZIP(list.get(i).getBytes(),"results.zip");
						break;
					default:
						response.setXML(list.get(i).getBytes(),"results.xml");
						break;
					}
					return response;
				}
			} finally {
				lock.unlock();
			}
		}
		return null;
	}

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
