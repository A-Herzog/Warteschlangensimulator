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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

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

	/**
	 * Führt einen minimalen Zertifikat-Check durch.
	 * @param certificates	Zu prüfende Zertifikate
	 * @return	Liefert <code>true</code> wenn zu der Webseite, die die Zertifikate verwendet, eine Verbindung aufgebaut werden darf
	 */
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
	 * @param uri	Request-URL
	 * @param sendUserAgentString	Soll ein Benutzer-Agent-String mitgeschickt werden?
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @return	Serververbindung oder <code>null</code>, wenn die Verbindung fehlgeschlagen ist.
	 */
	public static URLConnection openConnection(URI uri, final boolean sendUserAgentString, final boolean onlySecuredURLs) {
		return openConnection(uri,sendUserAgentString?"":null,onlySecuredURLs,false);
	}

	/**
	 * Öffnet die Verbindung zum Server, um eine Datei herunterzuladen
	 * @param uri	Request-URL
	 * @param userAgentString	Benutzer-Agent-String (kann <code>null</code> sein, dann wird kein User-Agent gesendet)
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @param followCookieBasedRedirects	Weiterleitungen manuell folgen und dabei ggf. Cookies weitergeben (<code>true</code>) oder Java-Standardmechanismus verwenden (<code>false</code>)
	 * @return	Serververbindung oder <code>null</code>, wenn die Verbindung fehlgeschlagen ist.
	 */
	public static URLConnection openConnection(URI uri, final String userAgentString, final boolean onlySecuredURLs, final boolean followCookieBasedRedirects) {
		URI lastURI=null;
		final Map<String,String> cookiesSecure=new HashMap<>();
		final Map<String,String> cookiesAll=new HashMap<>();
		int redirects=0;

		while (true) try {
			/* Verbindung vorbereiten */
			final URLConnection connect=uri.toURL().openConnection(getProxy());

			/* Verbindung vorbereiten: User-Agent */
			if (userAgentString!=null) connect.setRequestProperty("User-Agent",userAgentString);

			/* Verbindung vorbereiten: Referer */
			if (lastURI!=null) connect.setRequestProperty("Referer",lastURI.toString());

			/* Verbindung vorbereiten: Cookies */
			final StringBuilder cookieBuilder=new StringBuilder();
			final Map<String,String> cookies=(connect instanceof HttpsURLConnection)?cookiesSecure:cookiesAll;
			for (String key: cookies.keySet()) {
				if (cookieBuilder.length()>0) cookieBuilder.append("; ");
				cookieBuilder.append(key+"="+cookies.get(key));
			}
			if (cookieBuilder.length()>0) connect.setRequestProperty("Cookie",cookieBuilder.toString());

			/* Verbindung vorbereiten: Weiterleitungen automatisch oder manuell folgen? */
			if (followCookieBasedRedirects && (connect instanceof HttpURLConnection)) {
				((HttpURLConnection)connect).setInstanceFollowRedirects(false);
			}

			if (onlySecuredURLs) {
				/* Nur https */
				if (!(connect instanceof HttpsURLConnection)) return null;
				final HttpsURLConnection https=(HttpsURLConnection)connect;

				/* Hostname prüfen */
				https.setHostnameVerifier((hostname, session)->true);
			}

			/* Verbindung öffnen */
			connect.connect();

			/* Ggf. Zertifikat prüfen */
			if (onlySecuredURLs) {
				if (!(connect instanceof HttpsURLConnection)) return null;
				final HttpsURLConnection https=(HttpsURLConnection)connect;
				if (!checkCertificate(https.getServerCertificates())) return null;
			}

			if (!followCookieBasedRedirects) return connect;

			/* Redirect? */
			if (!(connect instanceof HttpURLConnection)) return null;
			final HttpURLConnection http=(HttpURLConnection)connect;

			final int status=http.getResponseCode();
			if (status==HttpURLConnection.HTTP_OK) return connect;
			if (status!=HttpURLConnection.HTTP_MOVED_TEMP && status!=HttpURLConnection.HTTP_MOVED_PERM && status!=HttpURLConnection.HTTP_SEE_OTHER) return null;

			/* Maximale Anzahl an Weiterleitungen */
			redirects++;
			if (redirects>10) return null;

			/* Neue Adresse und neue Cookies bestimmen */
			if (status!=HttpURLConnection.HTTP_MOVED_PERM) lastURI=uri;
			uri=new URI(connect.getHeaderField("Location"));
			final List<String> newCookiesList=connect.getHeaderFields().get("Set-Cookie");
			if (newCookiesList!=null) for (String newCookies: newCookiesList) if (newCookies!=null && !newCookies.trim().isEmpty()) {
				final boolean secure=newCookies.contains("Secure");
				for (String record: newCookies.split(";")) {
					record=record.trim();
					final String[] parts=record.split("=");
					if (parts.length==2) {
						if (parts[0].equalsIgnoreCase("Path") || parts[0].equalsIgnoreCase("Domain") || parts[0].equalsIgnoreCase("Max-Age")) continue;
						cookiesSecure.put(parts[0],parts[1]);
						if (!secure) cookiesAll.put(parts[0],parts[1]);
					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Lädt eine Textdatei vom Server und liefert die einzelnen Zeilen
	 * @param uri	Request-URL
	 * @param sendUserAgentString	Soll ein Benutzer-Agent-String mitgeschickt werden?
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @return	Text als Zeilenarray oder <code>null</code> im Fehlerfall
	 */
	public static String[] loadTextLines(final URI uri, final boolean sendUserAgentString, final boolean onlySecuredURLs) {
		try {
			/* Verbindung öffnen */
			final URLConnection connect=NetHelper.openConnection(uri,sendUserAgentString,onlySecuredURLs);
			if (connect==null) return null;

			/* Daten laden */
			try (final BufferedReader in=new BufferedReader(new InputStreamReader(connect.getInputStream()))) {
				final List<String> lines=new ArrayList<>();
				while (lines.size()<128) {
					String inputLine=in.readLine();
					if (inputLine==null) break;
					if (inputLine.length()>65535) inputLine=inputLine.substring(0,65535);
					lines.add(inputLine);
				}
				return lines.toArray(String[]::new);
			}
		} catch (IOException e) {return null;}
	}

	/**
	 * Lädt eine Textdatei vom Server und liefert den Inhalt als String.
	 * @param uri	Request-URL
	 * @param sendUserAgentString	Soll ein Benutzer-Agent-String mitgeschickt werden?
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @return	Text als String oder <code>null</code> im Fehlerfall
	 */
	public static String loadText(final URI uri, final boolean sendUserAgentString, final boolean onlySecuredURLs) {
		final String[] lines=loadTextLines(uri,sendUserAgentString,onlySecuredURLs);
		if (lines==null) return null;
		return String.join("\n",lines);
	}

	/**
	 * Lädt eine Binärdatei vom Server und liefert den Inhalt zurück.
	 * @param uri	Request-URL
	 * @param userAgentString	Benutzer-Agent-String (kann <code>null</code> sein, dann wird kein User-Agent gesendet)
	 * @param onlySecuredURLs	Nur Verbindungen zu hinterlegter Home-URL zulassen
	 * @param followCookieBasedRedirects	Weiterleitungen manuell folgen und dabei ggf. Cookies weitergeben (<code>true</code>) oder Java-Standardmechanismus verwenden (<code>false</code>)
	 * @return	Daten oder <code>null</code> im Fehlerfall
	 */
	public static byte[] loadBinary(final URI uri, final String userAgentString, final boolean onlySecuredURLs, final boolean followCookieBasedRedirects) {
		try {
			/* Verbindung öffnen */
			final URLConnection connect=NetHelper.openConnection(uri,userAgentString,onlySecuredURLs,true);
			if (!(connect instanceof HttpURLConnection)) return null;

			/* Daten laden */
			try (InputStream input=connect.getInputStream(); final ByteArrayOutputStream output=new ByteArrayOutputStream()) {
				final byte dataBuffer[]=new byte[1024*1024];
				int bytesRead;
				while ((bytesRead=input.read(dataBuffer,0,dataBuffer.length))!=-1) {
					output.write(dataBuffer,0,bytesRead);
				}
				return output.toByteArray();
			}
		} catch (IOException e) {return null;}
	}

}
