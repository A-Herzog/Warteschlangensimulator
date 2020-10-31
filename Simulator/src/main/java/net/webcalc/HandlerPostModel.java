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
import java.util.function.Consumer;

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
public class HandlerPostModel implements WebServerHandler {
	/** Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen) */
	private final String serverURL;
	/** ID des Formularfeldes für den Dateiupload (um den Originaldateinamen auszulesen) */
	private final String idOfFileNameField;
	/** Callback, welches die Daten an die Verarbeitungsklasse übergibt */
	private final Consumer<UploadInfo> fileLoader;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param idOfFileNameField	ID des Formularfeldes für den Dateiupload (um den Originaldateinamen auszulesen)
	 * @param fileLoader	Callback, welches die Daten an die Verarbeitungsklasse übergibt
	 */
	public HandlerPostModel(final String serverURL, final String idOfFileNameField, final Consumer<UploadInfo> fileLoader) {
		this.serverURL=serverURL;
		this.idOfFileNameField=idOfFileNameField;
		this.fileLoader=fileLoader;
	}

	/**
	 * Verarbeitet eine empfangene Datei
	 * @param session	Datensatz zu der empfangenen Datei
	 * @return	Ergebnis der Verarbeitung
	 */
	private String processUpload(final IHTTPSession session) {
		try {
			final Map<String,String> files=new HashMap<>();
			session.parseBody(files);
			String origFileName=null;
			if (idOfFileNameField!=null) {
				final List<String> names=session.getParameters().get(idOfFileNameField);
				if (names!=null && !names.isEmpty()) origFileName=names.get(0);
			}
			if (files.size()==0) {
				return Language.tr("WebServer.Upload.ErrorNoData");
			} else {
				final String fileName=files.get(files.keySet().toArray(new String[0])[0]);
				fileLoader.accept(new UploadInfo(new File(fileName),session.getRemoteIpAddress(),origFileName));
			}
		} catch (IOException | ResponseException e) {
			return Language.tr("WebServer.Upload.ErrorInvalidData");
		}

		return Language.tr("WebServer.Upload.Success");
	}

	@Override
	public WebServerResponse process(final IHTTPSession session) {
		if (!testPostURL(session,serverURL)) return null;

		final String status=processUpload(session);

		final WebServerResponse response=new WebServerResponse();
		response.setText(status,true);
		return response;
	}

	/**
	 * Datensatz mit Informationen zum Upload
	 * @author Alexander Herzog
	 * @see HandlerPostModel#HandlerPostModel(String, String, Consumer)
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
