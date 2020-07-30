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
package systemtools;

import java.awt.Component;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xml.XMLTools;

/**
 * Basisklasse zum Verwalten von Setup-Daten
 * @author Alexander Herzog
 * @version 2.4
 */
public abstract class SetupBase {
	/**
	 * Titel der Fehlermeldung "Konfiguration konnte nicht gespeichert werden"
	 */
	public static String errorSaveTitle="Konfiguration konnte nicht gespeichert werden";

	/**
	 * Inhalt der Fehlermeldung "Konfiguration konnte nicht gespeichert werden"
	 */
	public static String errorSaveMessage="Die Konfiguration konnte nicht in\n%s\ngespeichert werden.\nDie geänderten Einstellungen gehen beim Programmende verloren.";

	/**
	 * Kein Laden/Speichern von Setupeinstellungen
	 */
	public static boolean memoryOnly=false;

	/**
	 * Wird hier eine Datei angegeben, so wird die Konfiguration in dieser Datei gespeichert.
	 * Beim Laden hingegen wird zunächst die standardmäßige Setup-Datei geladen und dann werden
	 * die Daten durch die in dieser Datei ergänzt, d.h. die Standard-Setup-Datei wird in gewisser
	 * Weise als Default-Werte-Datei verwendet.
	 */
	public static File userConfigFile=null;

	private final List<SetupBaseChangeListener> changeListeners;
	private SetupBaseChangeListener watcher;
	private boolean lastFileSaveWasSuccessful;

	/**
	 * Konstruktor der Klasse <code>SetupBase</code>
	 * Das Setup sollte als Singleton eingesetzt werden, daher sollte der Konstruktor nur
	 * durch eine statische Methode aufgerufen werden.
	 */
	protected SetupBase() {
		changeListeners=new ArrayList<>();
		resetDataToDefaults();
		lastFileSaveWasSuccessful=true;
	}

	private void loadOrReloadSetupFromFile() {
		loadSetup(getSetupFile(),true);
		if (userConfigFile!=null) loadSetup(userConfigFile,false);
	}

	/**
	 * Lädt die Setup-Daten aus der Setup-Datei
	 */
	protected final void loadSetupFromFile() {
		loadOrReloadSetupFromFile();
		startChangeListener();
	}

	/**
	 * Liefert den Dateinamen der Standard-Setup-Datei
	 * @return	Dateiname der Standard-Setup-Datei
	 */
	protected abstract File getSetupFile();

	/**
	 * Stellt die Standardwerte ein.
	 */
	protected abstract void resetDataToDefaults();

	/**
	 * Versucht eine Zeichenfolge als boolschen Wert zu interpretieren
	 * @param data	Zu interpretierende Zeichenkette
	 * @param defaultValue	Vorgabewert, der Verwendet werden soll, wenn die Daten nicht interpretiert werden können bzw. leer sind
	 * @return	Gibt an, ob die Zeichenkette als wahr oder falsch interpretiert werden konnte.
	 */
	protected final boolean loadBoolean(final String data, final boolean defaultValue) {
		if (defaultValue) {
			return !(data!=null && !data.isEmpty() && (data.equals("0") || data.equalsIgnoreCase("false") || data.equalsIgnoreCase("falsch") || data.equalsIgnoreCase("nein") || data.equalsIgnoreCase("aus") || data.equalsIgnoreCase("no") || data.equalsIgnoreCase("off")));
		} else {
			return data!=null && !data.isEmpty() && (data.equals("1") || data.equalsIgnoreCase("true") || data.equalsIgnoreCase("wahr") || data.equalsIgnoreCase("ja") || data.equalsIgnoreCase("ein") || data.equalsIgnoreCase("yes") || data.equalsIgnoreCase("on"));
		}
	}

	/**
	 * Prüft nacheinander mehrere Zeichenketten.
	 * Die erste nichtleere Zeichenkette wird versucht als boolschen Werten zu interpretieren.
	 * @param values	Zu interpretierende Zeichenketten
	 * @param defaultValue	Vorgabewert, der Verwendet werden soll, wenn die Daten nicht interpretiert werden können bzw. leer sind
	 * @return	Gibt an, ob eine der Zeichenketten als wahr oder falsch interpretiert werden konnte.
	 */
	protected final boolean loadMultiBoolean(final String[] values, final boolean defaultValue) {
		for (String value: values) if (value!=null && !value.isEmpty()) return loadBoolean(value,defaultValue);
		return defaultValue;
	}

	/**
	 * Fügt eine Liste aus Zeichenketten an ein Array aus Zeichenketten an
	 * @param oldArray	Bestehendes Array aus Zeichenketten
	 * @param list	Anzufügende Liste aus Zeichenketten
	 * @return	Neues Array aus Zeichenketten, in dem das alte Array mit der Liste zusammengeführt wurde (ohne doppelte Einträge)
	 */
	protected final String[] addToArray(final String[] oldArray, final List<String> list) {
		if (oldArray==null) return list.toArray(new String[0]);
		final List<String> oldList=new ArrayList<>(Arrays.asList(oldArray));
		for (int i=0;i<list.size();i++) if (oldList.indexOf(list.get(i))<0) oldList.add(list.get(i));
		return oldList.toArray(new String[0]);
	}

	private Element getXMLRoot(final File setupFile) {
		int count=0;
		while (count<5) {
			if (count>0) try {Thread.sleep(500);} catch (InterruptedException e) {return null;}
			count++;

			final XMLTools xml=new XMLTools(setupFile);
			final Element root=xml.load();
			if (root==null) continue;

			if (!root.getNodeName().equalsIgnoreCase("Setup")) continue;
			return root;
		}
		return null;
	}

	private void loadSetup(final File setupFile, final boolean resetBeforeLoad) {
		if (watcher==null && !memoryOnly) {
			watcher=new SetupBaseChangeListener(setupFile,()->changeNotify());
			changeListeners.add(watcher);
		}

		if (memoryOnly) {
			if (resetBeforeLoad) resetDataToDefaults();
		} else {
			final Element root=getXMLRoot(setupFile);
			if (root!=null) {
				if (resetBeforeLoad) resetDataToDefaults();
				loadSetupFromXML(root);
			}
		}
	}

	private synchronized void startChangeListener() {
		for (SetupBaseChangeListener changeListener: changeListeners) changeListener.start();
	}

	private synchronized void stopChangeListener() {
		for (SetupBaseChangeListener changeListener: changeListeners) changeListener.stop();
	}

	private List<Runnable> changeNotifyListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn die Setup-Datei
	 * von außerhalb des Programmes verändert wurde.<br>
	 * Das Setup wird vor dem Aufruf der Listener bereits neu geladen.
	 * @param changeNotifyListener	Listener, der benachrichtigt wird, wenn die Setup-Datei von außerhalb des Programmes verändert wurde
	 */
	public synchronized void addChangeNotifyListener(final Runnable changeNotifyListener) {
		if (!changeNotifyListeners.contains(changeNotifyListener)) changeNotifyListeners.add(changeNotifyListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die benachrichtigt werden, wenn die Setup-Datei
	 * von außerhalb des Programmes verändert wurde.
	 * @param changeNotifyListener	Listener, der nicht mehr benachrichtigt werden soll, wenn die Setup-Datei von außerhalb des Programmes verändert wurde
	 */
	public synchronized void removeChangeNotifyListener(final Runnable changeNotifyListener) {
		if (changeNotifyListener!=null) changeNotifyListeners.remove(changeNotifyListener);
	}

	private void changeNotify() {
		loadOrReloadSetupFromFile();
		for (Runnable changeNotifyListener: changeNotifyListeners) changeNotifyListener.run();
	}

	/**
	 * Lädt die Setup-Daten aus einem xml-Knoten
	 * @param root	xml-Basisknoten, aus dem die Setup-Daten geladen werden soll
	 */
	protected abstract void loadSetupFromXML(final Element root);

	/**
	 * Speichert das Setup
	 * Gibt im Fehlerfall keine Warnung aus
	 * @return	Liefert <code>true</code>, wenn die Setup-Daten erfolgreich gespeichert werden konnten.
	 */
	public final boolean saveSetup() {
		if (memoryOnly) return lastFileSaveWasSuccessful=true;

		stopChangeListener();
		try {
			final File setupFile=(userConfigFile==null)?getSetupFile():userConfigFile;
			final XMLTools xml=new XMLTools(setupFile);
			final Element root=xml.generateRoot("Setup",true);
			final Document doc=root.getOwnerDocument();

			saveSetupToXML(doc,root);

			return lastFileSaveWasSuccessful=xml.save(root,true);
		} finally {
			startChangeListener();
		}
	}

	/**
	 * Speichert das Setup in einem Stream
	 * @param output	Stream in den die xml-Daten  geschrieben werden sollen
	 * @return	Liefert <code>true</code>, wenn die Setup-Daten erfolgreich gespeichert werden konnten.
	 */
	public final boolean saveToStream(final OutputStream output) {
		final XMLTools xml=new XMLTools(output);
		final Element root=xml.generateRoot("Setup",true);
		final Document doc=root.getOwnerDocument();

		saveSetupToXML(doc,root);

		return xml.save(doc);
	}

	/**
	 * Speichert das Setup
	 * Gibt im Fehlerfall eine Warnung aus
	 * @param parentComponent	Übergeordnete Komponente für einen möglichen Fehlermeldungsdialog
	 * @return	Liefert <code>true</code>, wenn die Setup-Daten erfolgreich gespeichert werden konnen.
	 */
	public final boolean saveSetupWithWarning(final Component parentComponent) {
		final boolean b=saveSetup();
		if (!b && parentComponent!=null) MsgBox.error(parentComponent,errorSaveTitle,String.format(errorSaveMessage,getSetupFile()));
		return b;
	}

	/**
	 * Speichert die Setup-Daten in einem xml-Knoten
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param root	xml-Basisknoten, in dem die Setup-Daten gespeichert werden sollen
	 */
	abstract protected void saveSetupToXML(final Document doc, final Element root);

	/**
	 * War der letzte Speichervorgang des Setups in einer Datei erfolgreich?
	 * @return	Erfolg des letzten Speichervorgangs
	 * @see #saveSetup()
	 */
	public boolean isLastFileSaveSuccessful() {
		return lastFileSaveWasSuccessful;
	}
}