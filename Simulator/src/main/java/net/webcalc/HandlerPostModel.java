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
import java.util.Map;
import java.util.function.BiConsumer;

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
	private final String serverURL;
	private final BiConsumer<File,String> fileLoader;

	/**
	 * Konstruktor der Klasse
	 * @param serverURL	Pfad zu dem Dokument aus Server-Sicht (sollte mit "/" beginnen)
	 * @param fileLoader	Callback, welches die Daten an die Verarbeitungsklasse übergibt
	 */
	public HandlerPostModel(final String serverURL, final BiConsumer<File,String> fileLoader) {
		this.serverURL=serverURL;
		this.fileLoader=fileLoader;
	}

	private String processUpload(final IHTTPSession session) {
		try {
			final Map<String,String> files=new HashMap<>();
			session.parseBody(files);
			if (files.size()==0) {
				return Language.tr("WebServer.Upload.ErrorNoData");
			} else {
				final String fileName=files.get(files.keySet().toArray(new String[0])[0]);
				fileLoader.accept(new File(fileName),session.getRemoteIpAddress());
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
}
