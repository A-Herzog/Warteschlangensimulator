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
package scripting.java;

import java.util.concurrent.Semaphore;

import language.Language;

/**
 * Diese Factory-Klasse ermöglicht das Erstellen von Ausführungsobjekten dynamische Methoden.
 * @author Alexander Herzog
 * @see DynamicRunner
 */
public final class DynamicFactory {
	/**
	 * Hält Einstellungen zum Laden von Java-Code
	 * @see DynamicSetup
	 * @see SimDynamicSetup
	 */
	private final DynamicSetup setup;

	/**
	 * Singleton-Instanz dieser Klasse
	 * @see #getFactory()
	 */
	private static volatile DynamicFactory factory=null;

	/**
	 * Sichert ab, dass nicht mehrere parallele Aufrufe
	 * von {@link #getFactory()} erfolgen und so am Ende
	 * zwei Singleton-Instanzen entstehen.
	 * @see #getFactory()
	 */
	private static final Semaphore mutex=new Semaphore(1);

	/**
	 * Factory-Methode die die Singleton-Instanz dieser Klasse liefert
	 * @return	Singleton-Instanz dieser Klasse
	 */
	public static DynamicFactory getFactory() {
		mutex.acquireUninterruptibly();
		try {
			if (factory==null) factory=new DynamicFactory();
			return factory;
		} finally {
			mutex.release();
		}
	}

	/**
	 * Dieser Konstruktor der Klasse kann nicht von außen aufgerufen werden.
	 * Stattdessen kann per {@link DynamicFactory#getFactory()} eine Instanz abgerufen werden.
	 */
	private DynamicFactory() {
		setup=new SimDynamicSetup();
		DynamicSecurity.getInstance();
	}

	/**
	 * Prüft ein Skript auf Korrektheit.
	 * @param script	Zu prüfendes Skript
	 * @return	Ergebnis der Prüfung als {@link DynamicRunner}-Objekt
	 */
	public DynamicRunner test(final String script) {
		final DynamicMethod dynamicMethod=new DynamicMethod(setup,script);
		final DynamicStatus status=dynamicMethod.test();
		return new DynamicRunner(script,status,dynamicMethod.getError());
	}

	/**
	 * Prüft ein Skript auf Korrektheit.
	 * @param script	Zu prüfendes Skript
	 * @param longMessage	Im Falle eines Fehlers die Zeile "Java-Fehler" als erstes mit ausgeben.
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String test(final String script, final boolean longMessage) {
		final DynamicRunner runner=test(script);
		if (runner.getStatus()==DynamicStatus.OK) return null;

		final StringBuilder sb=new StringBuilder();
		if (longMessage) sb.append(Language.tr("Simulation.Java.Error")+"\n");
		sb.append(getStatusText(runner.getStatus())+"\n");
		if (runner.getError()!=null) sb.append(runner.getError()+"\n");
		return sb.toString();
	}

	/**
	 * Prüft das Skript und lädt es im Erfolgsfall.
	 * @param script	Zu ladendes Skript
	 * @return	{@link DynamicRunner}-Objekt welches das geladene Skript oder eine Fehlermeldung enthält.
	 */
	public DynamicRunner load(final String script) {
		if (!hasCompiler()) return new DynamicRunner(script,DynamicStatus.NO_COMPILER,null);

		final DynamicMethod dynamicMethod=new DynamicMethod(setup,script);
		final DynamicStatus status=dynamicMethod.load();
		if (status!=DynamicStatus.OK) return new DynamicRunner(script,status,dynamicMethod.getError());
		return new DynamicRunner(dynamicMethod);
	}

	/**
	 * Liefert einen Beschreibungstext zu einem {@link DynamicStatus}
	 * @param status	{@link DynamicStatus} zu dem eine Beschreibung geliefert werden soll
	 * @return	Beschreibung als Text
	 */
	public static String getStatusText(final DynamicStatus status) {
		switch (status) {
		case COMPILE_ERROR: return Language.tr("Simulation.Java.Error.CompileError");
		case LOAD_ERROR: return Language.tr("Simulation.Java.Error.LoadError");
		case NO_COMPILER: return Language.tr("Simulation.Java.Error.NoCompiler.Internal");
		case NO_INPUT_FILE_OR_DATA: return Language.tr("Simulation.Java.Error.NoInputFileOrData");
		case NO_TEMP_FOLDER: return Language.tr("Simulation.Java.Error.NoTempFolder");
		case OK: return Language.tr("Simulation.Java.Error.Ok");
		case RUN_ERROR: return Language.tr("Simulation.Java.Error.RunError");
		case UNKNOWN_INPUT_FORMAT: return Language.tr("Simulation.Java.Error.UnkownInputFormat");
		case UNSUPPORTED_OS: return Language.tr("Simulation.Java.Error.UnsupportedOS");
		default: return "";
		}
	}

	/**
	 * Liefert eine Beschreibung aus Statuscode und ggf. einer erweiterten Fehlermeldung.
	 * @param status	{@link DynamicStatus} zu dem eine Beschreibung geliefert werden soll
	 * @param error	Optionaler zusätzlicher Fehlerbeschreibungstext
	 * @return	Beschreibung als Text
	 */
	public static String getLongStatusText(final DynamicStatus status, final String error) {
		if (error==null) return getStatusText(status); else return getStatusText(status)+":\n"+error;
	}

	/**
	 * Liefert eine Beschreibung aus Statuscode und ggf. einer erweiterten Fehlermeldung.
	 * @param runner	{@link DynamicRunner} bei dem Status und Fehlermeldungstext ausgelesen werden sollen
	 * @return	Beschreibung als Text
	 */
	public static String getLongStatusText(final DynamicRunner runner) {
		return getLongStatusText(runner.getStatus(),runner.getError());
	}

	/**
	 * Prüft, ob es sich bei dem System, auf dem die Anwendung läuft, um Windows handelt.
	 * @return	Gibt <code>true</code> zurück, wenn es sich um ein Windows-System handelt.
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	/**
	 * Gibt an, ob die Verarbeitung vollständig im Arbeitsspeicher (ohne temporäre Dateien) stattfindet.
	 * Nur in diesem Fall kann der Java-Kompiler auf allen Plattformen verwendet werden.
	 * @return	Gibt <code>true</code> zurück, wenn vollständig im Arbeitsspeicher (ohne temporäre Dateien) stattfindet.
	 */
	public static boolean isInMemoryProcessing() {
		return getFactory().setup.getCompileMode().inMemoryProcessing;
	}

	/**
	 * Gibt an, ob ein Java-Compiler verfügbar ist.
	 * @return	Java-Compiler verfügbar?
	 */
	public static boolean hasCompiler() {
		try {
			Class.forName("javax.tools.ToolProvider");
		} catch (ClassNotFoundException e) {
			return false;
		}

		/*
		try {
			final JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
			return compiler!=null;
		} catch (NoClassDefFoundError e) {
			return false;
		}
		 */

		return true;
	}
}
