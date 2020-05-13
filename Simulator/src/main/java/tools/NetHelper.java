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
package tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Diese Klasse bietet Hilfsroutinen zur Herstellung von Netzwerkverbindungen an.
 * @author Alexander Herzog
 */
public class NetHelper {
	/**
	 * Konstruktor der Klasse<br>
	 * Kann nicht aufgerufen werden. Die Klasse bietet nur statische Methoden.
	 */
	private NetHelper() {}

	/**
	 * Liefert die aktuellen Proxy-Einstellungen, die beim Aufruf von {@link URL#openConnection(Proxy)}
	 * verwendet werden können.
	 * @return	Proxy-Einstellungen
	 */
	public static Proxy getProxy() {
		final SetupData setup=SetupData.getSetup();

		if (!setup.useProxy || setup.proxyHost.trim().isEmpty()) {
			return Proxy.NO_PROXY;
		}

		/* Authentifikator */
		Authenticator authenticator;
		if (setup.proxyUser.trim().isEmpty() && setup.proxyPassword.trim().isEmpty()) {
			/* Keine Authentifikation */
			authenticator=null;
		} else {
			/* Authentifikation */
			authenticator=new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					if (getRequestorType() == RequestorType.PROXY) {
						if (getRequestingHost().equalsIgnoreCase(setup.proxyHost) && getRequestingPort()==setup.proxyPort) {
							return new PasswordAuthentication(setup.proxyUser,setup.proxyPassword.toCharArray());
						}
					}
					return null;
				}
			};
		}
		Authenticator.setDefault(authenticator);

		/* Proxy */
		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(setup.proxyHost,setup.proxyPort));
	}

	private static boolean checkCertificate(final Certificate[] certificates) {
		if (certificates==null || certificates.length==0) return false;

		for (Certificate certificate: certificates) {
			if (!(certificate instanceof X509Certificate)) return false;
			final X509Certificate X509=(X509Certificate)certificate;

			try {
				X509.checkValidity();
			} catch (CertificateExpiredException|CertificateNotYetValidException e) {
				return false;
			}

			/*
			Optional more checking:
			final String certAuthority=X509.getIssuerDN().getName();
			System.out.println(certAuthority);
			 */
		}

		return true;
	}

	/**
	 * Öffnet die Verbindung zum Server, um eine Datei herunterzuladen
	 * @param url	Request-URL
	 * @param sendUserAgentString	Soll ein Benutzer-Agent-String mitgeschickt werden?
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @return	Serververbindung oder <code>null</code>, wenn die Verbindung fehlgeschlagen ist.
	 */
	public static URLConnection openConnection(final URL url, final boolean sendUserAgentString, final boolean onlySecuredURLs) {
		try {
			/* Verbindung vorbereiten */
			final URLConnection connect=url.openConnection(getProxy());
			if (!sendUserAgentString) connect.setRequestProperty("User-Agent","");

			if (onlySecuredURLs) {
				/* Nur https */
				if (!(connect instanceof HttpsURLConnection)) return null;
				final HttpsURLConnection https=(HttpsURLConnection)connect;

				/* Hostname prüfen */
				https.setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				});
			}

			/* Verbindung öffnen */
			connect.connect();

			if (onlySecuredURLs) {
				if (!(connect instanceof HttpsURLConnection)) return null;
				final HttpsURLConnection https=(HttpsURLConnection)connect;
				if (!checkCertificate(https.getServerCertificates())) return null;
			}

			return connect;

		} catch (IOException e) {return null;}
	}

	/**
	 * Lädt eine Textdatei vom Server und liefert die einzelnen Zeilen
	 * @param url	Request-URL
	 * @param sendUserAgentString	Soll ein Benutzer-Agent-String mitgeschickt werden?
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @return	Text als Zeilenarray oder <code>null</code> im Fehlerfall
	 */
	public static String[] loadTextLines(final URL url, final boolean sendUserAgentString, final boolean onlySecuredURLs) {
		try {
			/* Verbindung öffnen */
			final URLConnection connect=NetHelper.openConnection(url,sendUserAgentString,onlySecuredURLs);
			if (connect==null) return null;

			/* Daten laden */
			try (final BufferedReader in=new BufferedReader(new InputStreamReader(connect.getInputStream()))) {
				final List<String> lines=new ArrayList<>();
				while (lines.size()<128) {
					String inputLine=in.readLine();
					if (inputLine==null) break;
					if (inputLine.length()>8192) inputLine=inputLine.substring(0,8192);
					lines.add(inputLine);
				}
				return lines.toArray(new String[0]);
			}
		} catch (IOException e) {return null;}
	}

	/**
	 * Lädt eine Textdatei vom Server und liefert den Inhalt als String
	 * @param url	Request-URL
	 * @param sendUserAgentString	Soll ein Benutzer-Agent-String mitgeschickt werden?
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @return	Text als String oder <code>null</code> im Fehlerfall
	 */
	public static String loadText(final URL url, final boolean sendUserAgentString, final boolean onlySecuredURLs) {
		final String[] lines=loadTextLines(url,sendUserAgentString,onlySecuredURLs);
		if (lines==null) return null;
		return String.join("\n",lines);
	}
}
