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
package systemtools.commandline;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Basisklasse zur Bearbeitung von Kommandozeilen-Aufrufen
 * @author Alexander Herzog
 * @see AbstractCommand
 * @version 1.1
 */
public class BaseCommandLineSystem {
	/** Bezeichner "Fehler" (in Großbuchstaben) */
	public static String errorBig="FEHLER";

	/** Fehlermeldung "Unbekannter Parameter" */
	public static String unknownCommand="Unbekannte Parameter. Rufen Sie den Simulation mit \"Hilfe\" als Parameter auf, um eine Liste der gültigen Befehle zu erhalten.";

	/** Teil-Fehlermeldung "Wenn... (Anzahl)" */
	public static String commandCountIf="Wenn als erster Parameter der Befehl \"%s\" übergeben wird,";
	/** Teil-Fehlermeldung "... keine weiteren Parameter" */
	public static String commandCountThen0="darf kein weiterer Parameter folgen";
	/** Teil-Fehlermeldung "... genau ein weiter Parameter" */
	public static String commandCountThen1="muss genau ein weiterer Parameter folgen";
	/** Teil-Fehlermeldung "... genau N weitere Parameter" */
	public static String commandCountThenN="müssen genau %d weitere Parameter folgen";
	/** Teil-Fehlermeldung "... mindestens ein weiter Parameter" */
	public static String commandCountThenAtLeast1="muss mindestens ein weiterer Parameter folgen";
	/** Teil-Fehlermeldung "... mindestens N weitere Parameter" */
	public static String commandCountThenAtLeastN="müssen mindestens %d weitere Parameter folgen";
	/** Teil-Fehlermeldung "... maximal ein weiter Parameter" */
	public static String commandCountThenMaximum1="darf maximal ein weiterer Parameter folgen";
	/** Teil-Fehlermeldung "... maximal N weitere Parameter" */
	public static String commandCountThenMaximumN="dürfen maximal %d weitere Parameter folgen";
	/** Teil-Fehlermeldung "... Aber keine übergeben." */
	public static String commandCountThenBut0="Es wurden jedoch keine weiteren Parameter übergeben.";
	/** Teil-Fehlermeldung "... Aber einer übergeben." */
	public static String commandCountThenBut1="Es wurde jedoch ein weiterer Parameter übergeben.";
	/** Teil-Fehlermeldung "... Aber N übergeben." */
	public static String commandCountThenButN="Es wurden jedoch %d weitere Parameter übergeben.";

	/** Hilfe für den "Report"-Befehl */
	public static String commandReportHelp=
			"Dieser Befehl erwartet genau drei weitere Parameter:\n"+
					"1. \"Inline\", \"Einzeldateien\", \"Liste\", \"Text\", \"PDF\", \"LaTeX\", \"HTMLApp\" oder ein Listeneintrag je nach dem, ob\n"+
					"a) ein HTML-Report mit eingebetteten Bildern,\n"+
					"b) ein HTML-Report mit Bildern in separaten Dateien,\n"+
					"c) eine Übersicht über alle verfügbaren Einzeldokumente\n"+
					"d) ein DOCX-Report,\n"+
					"e) ein PDF-Report oder\n"+
					"f) ein LaTeX-Report,\n"+
					"g) ein HTML-Web-App-Report oder\n"+
					"h) ein bestimmtes Einzeldokument ausgegeben werden soll.\n"+
					"2. Dateiname der Eingabedatei\n"+
					"3. Dateiname der Ausgabedatei";
	/** Fehlermeldung für den "Report"-Befehl */
	public static String commandReportError="Der Report konnte nicht erstellt werden.";
	/** Fehler beim "Report"-Befehl: Eingabedatei existiert nicht. */
	public static String commandReportErrorInputDoesNotExists="Die Eingabedatei %s existiert nicht.";
	/** Fehler beim "Report"-Befehl: Ausgabedatei existiert bereits. */
	public static String commandReportErrorOutputExists="Die Ausgabedatei %s existiert bereits.";
	/** "Report"-Befehl-Rückmeldung: Erfolg */
	public static String commandReportDone="Der Report wurde erfolgreich erstellt.";

	/** Name des "Hilfe"-Befehls */
	public static String commandHelpName="Hilfe";
	/** Name des "Hilfe"-Befehls optional in weiteren Sprachen */
	public static String[] commandHelpNamesOtherLanguages=new String[0];
	/** Kurzhilfe für den "Hilfe"-Befehl */
	public static String commandHelpHelpShort="Zeigt diese Hilfe an.";
	/** Hilfe für den "Hilfe"-Befehl */
	public static String commandHelpHelpLong="Dieser Befehl erwartet einen oder keine weiteren Parameter.\n"+
			"Wird ein Befehl als zusätzlicher Parameter angegeben, so wird die Hilfe zu diesem Befehl angezeigt.\n"+
			"Ansonsten wird die Hilfe zu allen Befehlen angezeigt.";
	/** Erklärung des allgemeinen Aufrufschemas und Liste aller Befehle (Einleitung) */
	public static String commandHelpInfo1=
			"Allgemeines Aufrufschema:\n"+
					"<SimulatorProgrammdatei> <Simulationsmodell>\n"+
					"oder\n"+
					"<SimulatorProgrammdatei> <Statistikdaten>\n"+
					"oder\n"+
					"<SimulatorProgrammdatei> <Befehl> <Parameter>\n"+
					"\n"+
					"In den ersten zwei Fällen wird das angegebene Simulationsmodell bzw. die angegebene Statistikdatei\n"+
					"in den Simulator geladen.\n"+
					"\n"+
					"<Befehl> kann einer der folgenden Begriffe sein:";
	/** Erklärung des allgemeinen Aufrufschemas und Liste aller Befehle (Abschluss) */
	public static String commandHelpInfo2=
			"(Die Groß- und Kleinschreibung der als <Befehl> angegebenen Betriebsart wird nicht berücksichtigt.)\n"+
					"\n"+
					"Die im Bereich <Parameter> anzugebenden zusätzlichen Parameter hängen vom <Befehl> ab:";
	/** Fehler bei "Hilfe"-Befehl: Kein Befehl mit dem Namen bekannt zu dem Hilfe angezeigt werden könnte." */
	public static String commandHelpError="Es existiert kein Kommandozeilen-Befehl \"%s\".";

	private final String programName;
	private final String version;
	private final String author;
	private final AbstractCommand[] commands;
	private final InputStream in;
	private final PrintStream out;
	private AbstractCommand command;

	/**
	 * Konstruktor der Klasse <code>BaseCommandLineSystem</code>
	 * @param programName Name des Programms
	 * @param version Version des Programms
	 * @param author Autor(en) des Programms
	 * @param in	Ein {@link InputStream}-Objekt oder <code>null</code>, über das Zeichen von der Konsole gelesen werden können (<code>null</code>, wenn keine Konsole verfügbar ist)
	 * @param out	Ein {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 */
	public BaseCommandLineSystem(final String programName, final String version, final String author, InputStream in, PrintStream out) {
		this.programName=programName;
		this.version=version;
		this.author=author;
		this.commands=getCommands().toArray(new AbstractCommand[0]);
		this.in=in;
		this.out=out;
	}

	/**
	 * Liefert eine Liste der verfügbaren Befehle
	 * @return	Liste der verfügbaren Befehle
	 */
	protected List<AbstractCommand> getCommands() {
		List<AbstractCommand> list=new ArrayList<>();

		list.add(new CommandHelp());

		return list;
	}

	private AbstractCommand findCommand(String arg0) {
		for (int i=0;i<commands.length;i++) {
			String[] keys=commands[i].getKeys();
			for (int j=0;j<keys.length;j++) if (keys[j].equalsIgnoreCase(arg0)) return commands[i];
		}
		return null;
	}

	/**
	 * Prüft, ob der übergebene Parameter ein Dateiname ist.
	 * @param args Kommandozeilenparameter, die an <code>main</code> übergeben wurden.
	 * @return Gibt ein <code>File</code>-Objekt zurück, wenn es eine Datei gibt, die geladen werden soll, sonst <code>null</code>
	 */
	public File checkLoadFile(String[] args) {
		if (args.length!=1) return null;
		File file=new File(args[0]);
		if (file.isFile()) return file; else return null;
	}

	/**
	 * Prüft, ob der Befehl ausgeführt werden kann.
	 * @param command	Auszuführender Befehl
	 * @return	Gibt an, ob der Befehl ausgeführt werden kann.
	 */
	protected boolean canRun(final AbstractCommand command) {
		return true;
	}

	/**
	 * Startet die Kommandozeilen-Variante des Simulators
	 * @param arguments Kommandozeilenparameter, die an <code>main</code> übergeben wurden.
	 * @return Gibt <code>true</code> zurück, wenn Kommandozeilenparameter übergeben wurden und folglich die Kommandozeilen-Variante aktiviert wurde.
	 */
	public boolean run(String[] arguments) {
		if (arguments.length==0) return false;
		out.println(programName+" "+version+", (c) "+author);
		command=findCommand(arguments[0]);
		if (command==null) {
			out.println(errorBig+": "+unknownCommand);
		} else {
			List<String> argsAsList=new ArrayList<>(Arrays.asList(arguments));
			argsAsList.remove(0);
			String s=command.prepare(argsAsList.toArray(new String[0]),in,out);
			if (s!=null) out.println(errorBig+": "+s); else {
				if (canRun(command)) command.run(commands,in,out);
			}
		}
		return true;
	}

	/**
	 * Teil dem laufenden Befehl mit, sich beenden zu sollen.
	 */
	public void setQuit() {
		if (command!=null) command.setQuit();
	}
}
