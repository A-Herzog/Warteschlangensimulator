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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.ResponseException;
import language.Language;
import net.web.WebServerHandler;
import net.web.WebServerResponse;

/**
 * Dieser Handler nimmt per http-POST ein Modell entgegen.
 * @author Alexander Herzog
 * @see CalcWebServer
 */
public class HandlerPost implements WebServerHandler {
	/** Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen) */
	private final String serverURL;
	/** ID des Formularfeldes für den Dateiupload (um den Originaldateinamen auszulesen) */
	private final String idOfFileNameField;
	/** Callback, welches die Modell- oder Tabellen-Daten an die Verarbeitungsklasse übergibt */
	private final Function<UploadInfo,Integer> fileLoaderNewModel;
	/** Callback, welches die den Request und die Filterskript-Daten an die Verarbeitungsklasse übergibt */
	private final BiFunction<String,UploadInfo,WebServerResponse> fileLoaderFilter;
	/** Soll die Ausgabe als Text (<code>null</code>) oder als json oder als xml erfolgen? */
	private final CalcWebServer.ResponseMode responseMode;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param idOfFileNameField	ID des Formularfeldes für den Dateiupload (um den Originaldateinamen auszulesen)
	 * @param fileLoaderNewModel	Callback, welches die Daten an die Verarbeitungsklasse übergibt
	 * @param responseMode	Soll die Ausgabe als Text (<code>null</code>) oder als json oder als xml erfolgen?
	 */
	public HandlerPost(final String serverURL, final String idOfFileNameField, final Function<UploadInfo,Integer> fileLoaderNewModel, final CalcWebServer.ResponseMode responseMode) {
		this.serverURL=serverURL;
		this.idOfFileNameField=idOfFileNameField;
		this.fileLoaderNewModel=fileLoaderNewModel;
		fileLoaderFilter=null;
		this.responseMode=responseMode;
	}

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param idOfFileNameField	ID des Formularfeldes für den Dateiupload (um den Originaldateinamen auszulesen)
	 * @param fileLoaderFilter	Callback, welches die Daten an die Verarbeitungsklasse übergibt
	 */
	public HandlerPost(final String serverURL, final BiFunction<String,UploadInfo,WebServerResponse> fileLoaderFilter, final String idOfFileNameField) {
		this.serverURL=serverURL;
		this.idOfFileNameField=idOfFileNameField;
		fileLoaderNewModel=null;
		this.fileLoaderFilter=fileLoaderFilter;
		this.responseMode=null;
	}

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param idOfFileNameField	ID des Formularfeldes für den Dateiupload (um den Originaldateinamen auszulesen)
	 * @param fileLoaderNewModel	Callback, welches die Daten an die Verarbeitungsklasse übergibt
	 */
	public HandlerPost(final String serverURL, final String idOfFileNameField, final Function<UploadInfo,Integer> fileLoaderNewModel) {
		this(serverURL,idOfFileNameField,fileLoaderNewModel,null);
	}

	/**
	 * Erstellt den Antworttext (als Text oder als json-Objekt)
	 * @param text	Informationstext
	 * @param id	ID des neuen Auftrags (oder &le;0 für einen Fehlerstatus)
	 * @return	Antworttext (als Text oder als json-Objekt)
	 */
	private String buildResponse(final String text, final int id) {
		if (responseMode==CalcWebServer.ResponseMode.JSON) {
			final StringBuilder json=new StringBuilder();
			json.append("{\n");
			json.append("  \"resultText\": \""+text+"\",\n");
			if (id>0) {
				json.append("  \"status\": \"success\",\n");
				json.append("  \"job\": \""+id+"\",\n");
			} else {
				json.append("  \"status\": \"error\",\n");
			}
			json.append("}\n");
			return json.toString();
		}

		if (responseMode==CalcWebServer.ResponseMode.XML) {
			final StringBuilder json=new StringBuilder();
			json.append("{\n");
			json.append("  \"resultText\": \""+text+"\",\n");
			if (id>0) {
				json.append("  \"status\": \"success\",\n");
				json.append("  \"job\": \""+id+"\",\n");
			} else {
				json.append("  \"status\": \"error\",\n");
			}
			json.append("}\n");
			return CalcWebServerTools.jsonToXmlString("upload",json.toString());
		}

		return text;

	}

	/**
	 * Verarbeitet eine empfangene Datei
	 * @param session	Datensatz zu der empfangenen Datei
	 * @return	Ergebnis der Verarbeitung
	 */
	private String processUpload(final IHTTPSession session) {
		Integer id=-1;
		try {
			final Map<String,String> files=new HashMap<>();
			session.parseBody(files);
			String origFileName=null;
			if (idOfFileNameField!=null) {
				final List<String> names=session.getParameters().get(idOfFileNameField);
				if (names!=null && !names.isEmpty()) origFileName=names.get(0);
			}
			if (files.size()==0) {
				return buildResponse(Language.tr("WebServer.Upload.ErrorNoData"),-1);
			} else {
				final String fileName=files.get(files.keySet().toArray(new String[0])[0]);
				id=fileLoaderNewModel.apply(new UploadInfo(new File(fileName),session.getRemoteIpAddress(),origFileName));
			}
		} catch (IOException | ResponseException e) {
			return buildResponse(Language.tr("WebServer.Upload.ErrorInvalidData"),-1);
		}

		return buildResponse(Language.tr("WebServer.Upload.Success"),id);
	}

	/**
	 * Verarbeitet ein hochgeladenes Modell oder eine für ein Modell hochgeladene Tabelle.
	 * @param session	Anfragedaten
	 * @return	Antwort oder <code>null</code>, wenn die Daten nicht verarbeitet werden konnten
	 */
	private WebServerResponse processModel(final IHTTPSession session) {
		final String status=processUpload(session);

		if (responseMode==CalcWebServer.ResponseMode.JSON) {
			final WebServerResponse response=new WebServerResponse();
			response.setJSON(status,true);
			response.setReturnCode(NanoHTTPD.Response.Status.CREATED);
			return response;
		}

		if (responseMode==CalcWebServer.ResponseMode.XML) {
			final WebServerResponse response=new WebServerResponse();
			response.setXML(status,"upload.xml");
			response.setReturnCode(NanoHTTPD.Response.Status.CREATED);
			return response;
		}

		final WebServerResponse response=new WebServerResponse();
		response.setText(status,true);
		return response;
	}

	/**
	 * Verarbeitet einen hochgeladenen Filter für ein ferig simuliertes Modell.
	 * @param session	Anfragedaten
	 * @return	Antwort oder <code>null</code>, wenn die Daten nicht verarbeitet werden konnten
	 */
	private WebServerResponse processFilter(final IHTTPSession session) {
		/* Request ermitteln */
		final String uri=session.getUri();
		if (uri.length()<=serverURL.length()) return null;
		if (!uri.substring(0,serverURL.length()).equalsIgnoreCase(serverURL)) return null;
		final String request=uri.substring(serverURL.length());

		/* Upload verarbeiten */
		try {
			final Map<String,String> files=new HashMap<>();
			session.parseBody(files);
			String origFileName=null;
			if (idOfFileNameField!=null) {
				final List<String> names=session.getParameters().get(idOfFileNameField);
				if (names!=null && !names.isEmpty()) origFileName=names.get(0);
			}
			if (files.size()==0) {
				return null;
			} else {
				final String fileName=files.get(files.keySet().toArray(new String[0])[0]);
				return fileLoaderFilter.apply(request,new UploadInfo(new File(fileName),session.getRemoteIpAddress(),origFileName));
			}
		} catch (IOException | ResponseException e) {
			return null;
		}
	}

	@Override
	public WebServerResponse process(final IHTTPSession session) {
		if (fileLoaderNewModel!=null) {
			if (!testPostURL(session,serverURL)) return null;
			return processModel(session);
		}

		if (fileLoaderFilter!=null) {
			if (session.getMethod()!=NanoHTTPD.Method.POST) return null;
			final String uri=session.getUri();
			if (uri.length()<=serverURL.length()) return null;
			if (!uri.substring(0,serverURL.length()).equalsIgnoreCase(serverURL)) return null;
			return processFilter(session);
		}

		return null;
	}

	/**
	 * Datensatz mit Informationen zum Upload
	 * @author Alexander Herzog
	 * @see HandlerPost#HandlerPost(String, String, Function, net.webcalc.CalcWebServer.ResponseMode)
	 * @see HandlerPost#HandlerPost(String, String, Function)
	 */
	public static class UploadInfo {
		/**
		 * Datei, die die geladenen Daten enthält (im lokalen Temp-Ordner)
		 */
		public final File file;

		/**
		 * Remote-IP-Adresse
		 */
		public final String ip;

		/**
		 * Optional (kann also <code>null</code> sein) der Remote-Dateiname
		 */
		public final String origFileName;

		/**
		 * Konstruktor der Klasse
		 * @param file	Datei, die die geladenen Daten enthält (im lokalen Temp-Ordner)
		 * @param ip	Remote-IP-Adresse
		 * @param origFileName	Optional (kann also <code>null</code> sein) der Remote-Dateiname
		 */
		public UploadInfo(final File file, final String ip, final String origFileName) {
			this.file=file;
			this.ip=ip;
			this.origFileName=origFileName;
		}
	}
}
