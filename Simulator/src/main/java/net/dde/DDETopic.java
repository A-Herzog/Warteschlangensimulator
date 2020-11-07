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
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Diese Klasse repräsentiert ein Thema innerhalb eines durch ein
 * {@link DDEServerSystem}-Objekt erzeugten DDE-Servers.<br>
 * Jedes Thema besitzt automatisch die beiden Datenfelder "Formats"
 * und "TopicItemList", die auch automatisch die richtigen Werte liefern.
 * @author Alexander Herzog
 * @see DDEServerSystem
 */
public abstract class DDETopic {
	/** Vorgabe-DDE-Item "TopicItemList" */
	private final DDEItem itemTopicItemList;
	/** Vorgabe-DDE-Item "Formats" */
	private final DDEItem itemFormats;
	/** Liste der Verfügbaren DDE-Items */
	private final List<DDEItem> items;

	/**
	 * Sichert parallele Zugriffe auf {@link #getItemsStream()} ab.
	 * @see #getItemsStream()
	 */
	private final Semaphore lock;

	/**
	 * Name des Themas.<br>
	 * Wird vom Konstruktor gesetzt.
	 */
	public final String name;

	/**
	 * Zugehöriger DDE-Server.<br>
	 * Wird vom Konstruktor gesetzt.
	 */
	protected final DDEServerSystem server;

	/**
	 * Konstruktor der Klasse.
	 * @param server	DDE-Server
	 * @param topicName	Name des Themas
	 */
	public DDETopic(final DDEServerSystem server, final String topicName) {
		this.server=server;
		name=topicName;
		items=new ArrayList<>();
		lock=new Semaphore(1);

		itemTopicItemList=new DDEItem("TopicItemList",()->String.join("\t",getItemsStream().map(item->item.name).toArray(String[]::new)));
		itemFormats=new DDEItem("Formats","TEXT");
	}

	/**
	 * Gibt an, ob ein bestimmtes Datenfeld vorhanden ist.
	 * @param itemName	Zu prüfendes Datenfeld
	 * @return	Gibt <code>true</code> zurück, wenn das Datenfeld existiert.
	 */
	public final boolean isItemSupported(final String itemName) {
		if (getItemsStream().anyMatch(item->item.name.equalsIgnoreCase(itemName))) return true;
		return isNotListedItemSupported(itemName);
	}

	/**
	 * Wenn kein regulär erfasstes Datenfeld zu dem Bezeichner passt,
	 * so wird diese Methode, die von abgeleiteten Klassen überschrieben
	 * werden kann, aufgerufen, um evtl. weitere Felder abzuprüfen, die
	 * nicht über fixe Datenfeldnamen zugänglich sind.
	 * @param itemName	Zu prüfendes Datenfeld
	 * @return	Gibt <code>true</code> zurück, wenn das Datenfeld existiert.
	 */
	protected boolean isNotListedItemSupported(final String itemName) {
		return false;
	}

	/**
	 * Liefert den Inhalt eines Datenfeldes.
	 * @param itemName	Abzufragendes	Datenfeldes
	 * @return	Inhalt des Datenfeldes oder eine leere Zeichenkette, wenn das Datenfeld nicht existiert.
	 */
	public final String getItem(final String itemName) {
		lock.acquireUninterruptibly();
		try {
			final DDEItem item=getItemsStream().filter(i->i.name.equalsIgnoreCase(itemName)).findFirst().orElse(null);
			if (item==null) return getNotListedItem(itemName);
			return item.get();
		} finally {
			lock.release();
		}
	}

	/**
	 * Wenn kein regulär erfasstes Datenfeld zu dem Bezeichner passt,
	 * so wird diese Methode, die von abgeleiteten Klassen überschrieben
	 * werden kann, aufgerufen, um evtl. Verarbeitungen, die nicht über
	 * fixe Datenfeldnamen möglich sind, durchzuführen.
	 * @param itemName	Abzufragendes	Datenfeldes
	 * @return	Inhalt des Datenfeldes oder eine leere Zeichenkette, wenn das Datenfeld nicht existiert.
	 */
	protected String getNotListedItem(final String itemName) {
		return "";
	}

	/**
	 * Liefert alle in dem Thema registrierten Datenfelder als Stream.
	 * @return	Stream aller Datenfelder des Themas
	 */
	protected final Stream<DDEItem> getItemsStream() {
		final List<DDEItem> list=new ArrayList<>();
		list.add(itemTopicItemList);
		list.add(itemFormats);
		list.addAll(items);
		return list.stream();
	}

	/**
	 * Fügt ein neues statisches Datenfeld zu dem Thema hinzu.
	 * @param itemName	Name des neuen Datenfeldes
	 * @param staticContent	Statischer Wert des Datenfeldes
	 */
	protected final void addItemStatic(final String itemName, final String staticContent) {
		items.add(new DDEItem(itemName,staticContent));
	}

	/**
	 * Fügt ein neues Datenfeld, welches seinen Inhalt ändern kann, zu dem Thema hinzu.
	 * @param itemName	Name des neuen Datenfeldes
	 * @param getter	Lambda-Ausdruck zur Ermittlung des aktuellen Wertes des Datenfeldes
	 */
	protected final void addItemDynamic(final String itemName, final Supplier<String> getter) {
		items.add(new DDEItem(itemName,getter));
	}

	/**
	 * Fügt ein neues Datenfeld, welches seinen Inhalt ändern kann, zu dem Thema hinzu.<br>
	 * Das Datenfeld kann außerdem, wenn es sich ändert, den Server darüber benachrichtigen.
	 * @param itemName	Name des neuen Datenfeldes
	 * @param getter	Lambda-Ausdruck zur Ermittlung des aktuellen Wertes des Datenfeldes
	 */
	protected final void addItemUpdateable(final String itemName, final Supplier<String> getter) {
		items.add(new DDEItem(itemName,getter,true));
	}

	/**
	 * Liefert eine Liste der Datenfelder, die geändert wurden.<br>
	 * Wird von {@link DDETopic#updateTest()} aufgerufen.
	 * @return	Liste der Datenfelder, die geändert wurden. Darf auch insgesamt <code>null</code> sein.
	 */
	protected List<String> getItemsToUpdate() {
		List<String> list=null;
		for (DDEItem item: items) if (item.updateTest()) {
			if (list==null) list=new ArrayList<>();
			list.add(item.name);
		}
		return list;
	}

	/**
	 * Prüft, ob sich ein Datenfeld dieses Themas geändert hat
	 * und benachrichtigt ggf. den Server.
	 */
	public final void updateTest() {
		List<String> updateList=null;

		lock.acquireUninterruptibly();
		try {
			updateList=getItemsToUpdate();
		} finally {
			lock.release();
		}

		if (updateList!=null) server.updateNotify(name,updateList);
	}
}
