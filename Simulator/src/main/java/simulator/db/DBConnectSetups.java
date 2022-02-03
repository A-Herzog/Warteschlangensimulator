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
package simulator.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tools.SetupData;
import xml.XMLTools;

/**
 * Diese Klasse hält alle Datenbank-Verbindungsdatensätze vor.
 * @author Alexander Herzog
 * @see DBConnect
 * @see DBConnectSetup
 */
public class DBConnectSetups {
	/**
	 * Dateiname der Konfigurationsdatei
	 */
	private static final String SETUP_FILE="JDBC.cfg";

	/**
	 * Datenbank-Verbindungsdatensätze
	 */
	private final List<DBConnectSetup> setups;

	/**
	 * Referenz auf die Instanz dieses Singletons
	 * @see #getInstance()
	 */
	private static DBConnectSetups instance;

	/**
	 * Liefert die Singleton-Instanz dieser Klasse.
	 * @return	Singleton-Instanz dieser Klasse
	 * @see #instance
	 */
	public synchronized static DBConnectSetups getInstance() {
		if (instance==null) instance=new DBConnectSetups();
		return instance;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt ein Singleton dar und kann nicht instanziert werden.
	 * @see #getInstance()
	 */
	private DBConnectSetups() {
		setups=new ArrayList<>();

		final File file1=SetupData.getProgramFolder();
		final File file2=SetupData.getSetupFolder();
		loadFromFile(new File(file1,SETUP_FILE),setups);
		if (!file1.equals(file2)) loadFromFile(new File(file2,SETUP_FILE),setups);

		DBConntectSetupTemplates.addToList(setups);
	}

	/**
	 * Lädt die Einstellungen aus einer Datei.
	 * @param file	Zu ladende Datei
	 * @param list	Zu ergänzende Liste
	 */
	private static void loadFromFile(final File file, final List<DBConnectSetup> list) {
		if (file==null || !file.isFile()) return;
		final XMLTools xml=new XMLTools(file);
		final Element root=xml.load();
		if (root!=null) loadFromXML(root,list);
	}

	/**
	 * Liefert den Dateinamen der primären Konfigurationsdatei.
	 * @return	Primäre Konfigurationsdatei (kann <code>null</code> sein, wenn keine Konfigurationsdatei existiert)
	 */
	public static File getConfigurationFile() {
		File file;

		file=new File(SetupData.getSetupFolder(),SETUP_FILE);
		if (file.isFile()) return file;

		file=new File(SetupData.getProgramFolder(),SETUP_FILE);
		if (file.isFile()) return file;

		return null;
	}

	/**
	 * Lädt die Einstellungen aus einem XML-Knoten und seinen Unterknoten.
	 * @param root	Ausgangs-XML-Knoten
	 * @param list	Zu ergänzende Liste
	 */
	private static void loadFromXML(final Element root, final List<DBConnectSetup> list) {
		final NodeList l=root.getChildNodes();
		final int length=l.getLength();
		for (int i=0; i<length;i++) {
			final Node sub=l.item(i);
			if (!(sub instanceof Element)) continue;
			final Element e=(Element)sub;

			if (e.getNodeName().equals("Database")) {
				final DBConnectSetup setup=new DBConnectSetup();
				if (setup.loadFromXML(e)) {
					int index=-1;
					for (int j=0;i<list.size();j++) if (list.get(j).name.equals(setup.name)) {
						index=j;
						break;
					}
					if (index<0) list.add(setup); else list.set(index,setup);
				}
			}
		}
	}

	/**
	 * Liefert einen Datensatz auf Basis seines Namens.
	 * @param name	Name des Datensatz
	 * @return	Datensatz oder <code>null</code>, wenn kein Datensatz mit dem angegebenen Namen existiert.
	 */
	public static DBConnectSetup getByType(final String name) {
		return getInstance().setups.stream().filter(setup->setup.name.equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	/**
	 * Liefert eine Liste aller vorhandenen Datensatznamen.
	 * @return	Liste aller vorhandenen Datensatznamen
	 */
	public static String[] getNames() {
		return getInstance().setups.stream().map(setup->setup.name).toArray(String[]::new);
	}

	/**
	 * Liefert den Index eines Datensatzes in der Liste aller vorhandenen Datensatznamen.
	 * @param name	Name des Datensatzes
	 * @return	Index eines Datensatzes in der Liste aller vorhandenen Datensatznamen (-1, wenn kein Datensatz mit dem angegebenen Namen in der Liste existiert)
	 */
	public static int index(final String name) {
		final List<DBConnectSetup> setups=getInstance().setups;
		for (int i=0;i<setups.size();i++) if (setups.get(i).name.equalsIgnoreCase(name)) return i;
		return -1;
	}

	/**
	 * Liefert einen Datensatz auf Basis seines Indexes in {@link #getNames()}.
	 * @param index	Index des Datensatzes
	 * @return	Datensatz oder <code>null</code>, wenn der Index außerhalb des gültigen Bereiches liegt
	 */
	public static DBConnectSetup byIndex(int index) {
		final List<DBConnectSetup> setups=getInstance().setups;
		if (index<0 || index>=setups.size()) return null;
		return setups.get(index);
	}
}
