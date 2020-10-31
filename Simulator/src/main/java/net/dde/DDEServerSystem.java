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
package net.dde;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.pretty_tools.dde.ClipboardFormat;
import com.pretty_tools.dde.DDEException;
import com.pretty_tools.dde.client.DDEClientConversation;
import com.pretty_tools.dde.server.DDEServer;

/**
 * Diese Klasse kapselt einen DDE-Server über den andere Anwendungen
 * Daten aus diesem Programm abrufen können.
 * @author Alexander Herzog
 */
public class DDEServerSystem {
	/** DDE-Server */
	private DDEServer server;
	/** Name des Services über den der DDE-Server angesprochen werden können soll */
	private String service;
	/** Sichert parallele Zugriffe auf {@link #server} ab */
	private Semaphore lock;

	/**
	 * Liste der Themen.<br>
	 * Hier können durch abgeleitete Klassen weitere Themen
	 * vom Typ {@link DDETopic} registriert werden.<br>
	 * Das "System"-Thema ({@link DDETopicSystem}) wird
	 * bereits durch den Konstruktor zu dieser Liste hinzugefügt.
	 */
	protected final List<DDETopic> topics;

	/**
	 * Konstruktor der Klasse.<br>
	 * Der Konstruktor startet den Server nicht, dies muss über die Methode {@link DDEServerSystem#start()} erfolgen.
	 * @param service	Name des Services über den der DDE-Server angesprochen werden können soll
	 */
	public DDEServerSystem(final String service) {
		this.service=service;
		topics=new ArrayList<>();
		topics.add(new DDETopicSystem(this));
		lock=new Semaphore(1);
	}

	/**
	 * Gibt an, ob der Server momentan läuft.
	 * @return	Gibt <code>true</code> zurück, wenn der Server momentan läuft.
	 */
	public boolean isRunning() {
		return server!=null;
	}

	private boolean otherServerRunning() {
		DDEClientConversation conversation=null;
		try {
			conversation=new DDEClientConversation();
		} catch (UnsatisfiedLinkError | NoClassDefFoundError e) {return false;} /* DLL nicht gefunden */

		try {
			conversation.connect(service,"System");
			conversation.disconnect();
		} catch (DDEException e) {
			return false;
		}
		return true;
	}

	/**
	 * Startet den Server. Dies passiert nicht bereits automatisch im Konstruktor.
	 * @return	Gibt <code>true</code> zurück, wenn der Server erfolgreich gestartet werden konnte. (Wenn der Server bereits lief, liefert die Funktion ebenfalls <code>true</code>.)
	 * @see DDEServerSystem#stop()
	 */
	public boolean start() {
		if (server!=null) return true;
		if (otherServerRunning()) return false;

		lock.acquireUninterruptibly();
		try {
			try {
				server=new DDEServerExt(service);
			} catch (UnsatisfiedLinkError | NoClassDefFoundError e) {server=null;} /* DLL nicht gefunden */
			if (server!=null) try {server.start();} catch (DDEException e) {server=null;}
			return server!=null;
		} finally {
			lock.release();
		}
	}

	/**
	 * Hält den Server an, falls er momentan läuft.
	 * @return	Gibt <code>true</code> zurück, wenn der Server gestoppt werden konnte oder überhaupt nicht lief.
	 * @see DDEServerSystem#start()
	 */
	public boolean stop() {
		if (server==null) return true;
		lock.acquireUninterruptibly();
		try {
			try {server.stop();} catch (DDEException e) {server=null; return false;}
			server=null;
			return true;
		} finally {
			lock.release();
		}
	}

	/**
	 * Erfasst eine Statusmeldung.<br>
	 * Muss von abgeleiteten Klassen überschrieben werden, wenn die Meldungen
	 * verarbeitet werden sollen.
	 * @param message	Neue Statusmeldung
	 */
	protected void log(final String message) {
	}

	/**
	 * Benachrichtigt alle verbundenen DDE-Clienten, dass sich ein bestimmte Datenfeld geändert hat.
	 * @param topic	Thema in dem sich Datenfeld geändert hat
	 * @param items	Datenfelder, die sich geändert haben. Darf auch insgesamt <code>null</code> sein.
	 * @return	Liefert <code>true</code> zurück, wenn die Benachrichtigung abgesendet werden konnte.
	 */
	public boolean updateNotify(final String topic, final List<String> items) {
		if (server==null) return false;
		if (items==null || items.isEmpty()) return true;
		lock.acquireUninterruptibly();
		try {
			try {
				for (String item: items) {
					log("Update: "+topic+","+item);
					server.notifyClients(topic,item);
				}
			} catch (DDEException e) {return false;}
			return true;
		} finally {
			lock.release();
		}
	}

	/**
	 * Prüft alle Themen und Datenfelder, ob sich Daten geändert haben.
	 */
	public void updateTest() {
		topics.stream().forEach(topic->topic.updateTest());
	}

	/**
	 * Liefert eine Liste aller registrierten Themen.
	 * @return	Liste aller registrierten Themen
	 */
	public String[] getTopicNames() {
		return topics.stream().map(topic->topic.name).toArray(String[]::new);
	}

	/**
	 * Wird aufgerufen, wenn per DDE eine Anweisung übergeben wurde.
	 * @param command	DDE-Anweisung (dass dieser Parameter nicht <code>null</code> und nicht leer ist, wurde bereits geprüft).
	 * @return	Gibt <code>true</code> zurück, wenn der Befehl verarbeitet werden konnte.
	 */
	protected boolean processCommand(final String command) {
		return false;
	}

	/**
	 * Konkrete Implementierung von {@link DDEServer}
	 */
	private class DDEServerExt extends DDEServer {
		/**
		 * Konstruktor der Klasse
		 * @param service	Name des angebotenen Service
		 */
		public DDEServerExt(final String service) {
			super(service);
		}

		@Override
		protected boolean isTopicSupported(final String topicName) {
			return topics.stream().anyMatch(topic->topic.name.equalsIgnoreCase(topicName));
		}

		@Override
		protected boolean isItemSupported(final String topic, final String item, final int uFmt) {
			if (uFmt!=ClipboardFormat.CF_TEXT.getNativeCode() && uFmt!=ClipboardFormat.CF_UNICODETEXT.getNativeCode()) return false;
			final DDETopic ddeTopic=topics.stream().filter(t->t.name.equalsIgnoreCase(topic)).findFirst().orElse(null);
			if (ddeTopic==null) return false;
			return ddeTopic.isItemSupported(item);
		}

		@Override
		protected String onRequest(final String topic, final String item) {
			final DDETopic ddeTopic=topics.stream().filter(t->t.name.equalsIgnoreCase(topic)).findFirst().orElse(null);
			if (ddeTopic==null) return "";
			final String s=ddeTopic.getItem(item);
			log("Request: "+topic+","+item+"="+s);
			return s;
		}

		@Override
		protected byte[] onRequest(final String topic, final String item, final int uFmt) {
			if (uFmt!=ClipboardFormat.CF_TEXT.getNativeCode() && uFmt!=ClipboardFormat.CF_UNICODETEXT.getNativeCode()) return new byte[0];
			return onRequest(topic,item).getBytes();
		}

		@Override
		protected boolean onExecute(final String command) {
			if (command==null || command.trim().isEmpty()) return false;
			return processCommand(command);
		}
	}
}