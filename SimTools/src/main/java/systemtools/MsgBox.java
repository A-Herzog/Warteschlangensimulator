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
import java.util.Locale;

import javax.swing.JOptionPane;

/**
 * Diese Klasse stellt einen Ersatz f�r {@link JOptionPane} zur Verf�gung.<br>
 * Sie stellt einige weitere statische Methoden ("Jetzt speichern?"-Abfrage, �berschreib-Warnung)
 * zur Verf�gung. Au�erdem kann sie �ber die statische Methode {@link #setBackend(MsgBoxBackend)}
 * mit alternativen Backends (statt dem Vorgabe-Backend, welches {@link JOptionPane} verwendet)
 * verbunden werden.
 * @author Alexander Herzog
 * @see MsgBoxBackend
 * @version 1.2
 */
public class MsgBox {
	/*
	 * Diese Konstanten k�nnen von den Anzeige-Backends verwendet werden.<br>
	 * Auf diese Weise ist eine einfache Internationalisierung m�glich.
	 */

	/** Bezeichner f�r den Dialogtitel "Information" */
	public static String TitleInformation="Information";
	/** Bezeichner f�r den Dialogtitel "Warnung" */
	public static String TitleWarning="Warnung";
	/** Bezeichner f�r den Dialogtitel "Fehler" */
	public static String TitleError="Fehler";
	/** Bezeichner f�r den Dialogtitel "Frage" */
	public static String TitleConfirmation="Frage";
	/** Bezeichner f�r den Dialogtitel "Auswahl" */
	public static String TitleAlternatives="Auswahl";
	/** Bezeichner f�r die Dialogschaltfl�che "Ja" */
	public static String OptionYes="Ja";
	/** Bezeichner f�r die Dialogschaltfl�che "Nein" */
	public static String OptionNo="Nein";
	/** Bezeichner f�r die Dialogschaltfl�che "Abbrechen" */
	public static String OptionCancel="Abbrechen";
	/** Bezeichner f�r die Dialogschaltfl�che "Jetzt speichern" */
	public static String OptionSaveYes="Jetzt speichern";
	/** Bezeichner f�r den Erkl�rungstext f�r die Dialogschaltfl�che "Jetzt speichern" */
	public static String OptionSaveYesInfo="Speichert die aktuellen Daten bevor sie verworfen werden.";
	/** Bezeichner f�r die Dialogschaltfl�che "Nicht speichern" */
	public static String OptionSaveNo="Nicht speichern";
	/** Bezeichner f�r den Erkl�rungstext f�r die Dialogschaltfl�che "Nicht speichern" */
	public static String OptionSaveNoInfo="Verwirft die aktuellen Daten ohne sie vorher zu speichern.";
	/** Bezeichner f�r den Erkl�rungstext f�r die Dialogschaltfl�che "Abbrechen" (in Bezug auf eine Speichern ja/nein/abbrechen Frage) */
	public static String OptionSaveCancelInfo="F�hrt den gew�hlten Befehl nicht aus. Die Daten bleiben unver�ndert bestehen.";
	/** Bezeichner f�r den Dialogtitel "�berschreiben" */
	public static String OverwriteTitle="Vorhendene Datei �berschreiben?";
	/** Bezeichner f�r die Erkl�rung zur �berschreib-Frage */
	public static String OverwriteInfo="Die Datei\n%s\nexistiert bereits.\nSoll die Datei jetzt �berschrieben werden?";
	/** Bezeichner f�r die Dialogschaltfl�che "Datei �berschreiben" */
	public static String OverwriteYes="Datei �berschreiben";
	/** Bezeichner f�r den Erkl�rungstext f�r die Dialogschaltfl�che "Datei nicht �berschreiben" */
	public static String OverwriteYesInfo="L�scht die bisher unter diesem Namen existierende Datei und speichert die neuen Daten unter diesem Dateinamen.";
	/** Bezeichner f�r die Dialogschaltfl�che "Datei nicht �berschreiben" */
	public static String OverwriteNo="Datei nicht �berschreiben";
	/** Bezeichner f�r den Erkl�rungstext f�r die Dialogschaltfl�che "Datei nicht �berschreiben" */
	public static String OverwriteNoInfo="Beh�lt die bestehende Datei unver�ndert bei. Die neuen Daten werden nicht gespeichert.";
	/** F�r die Dialoge aktuell g�ltige Landeseinstellung */
	public static Locale ActiveLocale=Locale.getDefault();

	/**
	 * Die Klasse {@link MsgBox} besitzt nur statische Methoden und kann
	 * nicht instanziert werden.
	 */
	private MsgBox() {}

	private static MsgBoxBackend backend;

	static {
		backend=new MsgBoxBackendJOptionPane();
	}

	/**
	 * Setzt ein neues Backend, welches die Meldungs-Dialoge anzeigt
	 * @param newBackend	Neues Backend
	 * @see MsgBoxBackend
	 */
	public static void setBackend(final MsgBoxBackend newBackend) {
		if (newBackend!=null) backend=newBackend;
	}

	/**
	 * Zeigt einen Informationsdialog an.
	 * @param parentComponent	�bergeordnetes Element
	 * @param title	Titel der Meldung; kann <code>null</code> sein, dann wird "Information" als Titel verwendet.
	 * @param message	Anzuzeigende Meldung
	 */
	public static void info(Component parentComponent, String title, String message) {
		if (title==null || title.isEmpty()) title=TitleInformation;
		backend.info(parentComponent,title,message);
	}

	/**
	 * Zeigt einen Warnungdialog an.
	 * @param parentComponent	�bergeordnetes Element
	 * @param title	Titel der Meldung; kann <code>null</code> sein, dann wird "Warnung" als Titel verwendet.
	 * @param message	Anzuzeigende Meldung
	 */
	public static void warning(Component parentComponent, String title, String message) {
		if (title==null || title.isEmpty()) title=TitleWarning;
		backend.warning(parentComponent,title,message);
	}

	/**
	 * Zeigt eine Fehlermeldung an.
	 * @param parentComponent	�bergeordnetes Element
	 * @param title	Titel der Meldung; kann <code>null</code> sein, dann wird "Fehler" als Titel verwendet.
	 * @param message	Anzuzeigende Meldung
	 */
	public static void error(Component parentComponent, String title, String message) {
		if (title==null || title.isEmpty()) title=TitleError;
		backend.error(parentComponent,title,message);
	}

	/**
	 * Zeigt einen Ja/Nein/Abbrechen-Dialog an.
	 * @param parentComponent	�bergeordnetes Element
	 * @param title	Titel der Anfrage; kann <code>null</code> sein, dann wird "Frage" als Titel verwendet.
	 * @param message	Anzuzeigende Meldung
	 * @param infoYes	Infotext f�r die "Ja"-Option (muss nicht von allen Backends verwendet werden)
	 * @param infoNo	Infotext f�r die "Nein"-Option (muss nicht von allen Backends verwendet werden)
	 * @param infoCancel	Infotext f�r die "Abbrechen"-Option (muss nicht von allen Backends verwendet werden)
	 * @return	Gibt eine der Ja/Nein/Abbrechen-Konstanten aus <code>JOptionPane</code> zur�ck.
	 */
	public static int confirm(Component parentComponent, String title, String message, String infoYes, String infoNo, String infoCancel) {
		if (title==null || title.isEmpty()) title=TitleConfirmation;
		return backend.confirm(parentComponent,title,message,infoYes,infoNo,infoCancel);
	}

	/**
	 * Zeigt einen "Sollen die nicht gespeicherten Daten jetzt gespeichert werden"-Dialog an; verwendet daf�r
	 * die Ja/Nein/Abbrechen-Schaltfl�chen.
	 * @param parentComponent	�bergeordnetes Element
	 * @param title	Titel der Anfrage; kann <code>null</code> sein, dann wird "Frage" als Titel verwendet.
	 * @param message	Anzuzeigende Meldung
	 * @return Gibt eine der Ja/Nein/Abbrechen-Konstanten aus <code>JOptionPane</code> zur�ck.
	 */
	public static int confirmSave(Component parentComponent, String title, String message) {
		if (title==null || title.isEmpty()) title=TitleConfirmation;
		return backend.confirmSave(parentComponent,title,message);
	}

	/**
	 * Zeigt einen Ja/Nein-Dialog an.
	 * @param parentComponent	�bergeordnetes Element
	 * @param title	Titel der Anfrage; kann <code>null</code> sein, dann wird "Frage" als Titel verwendet.
	 * @param message	Anzuzeigende Meldung
	 * @param infoYes	Infotext f�r die "Ja"-Option (muss nicht von allen Backends verwendet werden)
	 * @param infoNo	Infotext f�r die "Nein"-Option (muss nicht von allen Backends verwendet werden)
	 * @return	Gibt <code>true</code> zur�ck, wenn der Dialog mit "Ja" geschlossen wurde.
	 */
	public static boolean confirm(Component parentComponent, String title, String message, String infoYes, String infoNo) {
		if (title==null || title.isEmpty()) title=TitleConfirmation;
		return backend.confirm(parentComponent,title,message,infoYes,infoNo);
	}

	/**
	 * Zeigt eine Abfrage an, ob die angegebene Datei �berschrieben werden soll
	 * @param parentComponent	�bergeordnetes Element
	 * @param file	Name der Datei, f�r die die Abfrage durchgef�hrt werden soll
	 * @return	Gibt <code>true</code> zur�ck, wenn der Nutzer dem �berschreiben zugestimmt hat.
	 */
	public static boolean confirmOverwrite(Component parentComponent, File file) {
		return backend.confirmOverwrite(parentComponent,file);
	}

	/**
	 * Zeigt einen Auswahldialog mit verschiedenen Optionen an.
	 * @param parentComponent	�bergeordnetes Element
	 * @param title	Titel der Anfrage
	 * @param message	Anzuzeigende Meldung
	 * @param options	Texte zu den Optionen
	 * @param info	Erkl�rungen zu den Texten zu den Optionen
	 * @return	Gew�hlte Option (0-basierend); -1 f�r Abbruch
	 */
	public static int options(Component parentComponent, String title, String message, String[] options, String[] info) {
		return backend.options(parentComponent,title,message,options,info);
	}

}