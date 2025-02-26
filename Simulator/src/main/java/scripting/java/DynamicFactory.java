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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;

import language.Language;

/**
 * Diese Factory-Klasse erm�glicht das Erstellen von Ausf�hrungsobjekten dynamische Methoden.
 * @author Alexander Herzog
 * @see DynamicRunner
 */
public final class DynamicFactory {
	/**
	 * H�lt Einstellungen zum Laden von Java-Code
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
	 * Dieser Konstruktor der Klasse kann nicht von au�en aufgerufen werden.
	 * Stattdessen kann per {@link DynamicFactory#getFactory()} eine Instanz abgerufen werden.
	 */
	private DynamicFactory() {
		setup=new SimDynamicSetup();
	}

	/**
	 * Pr�ft ein Skript auf Korrektheit.
	 * @param script	Zu pr�fendes Skript
	 * @param imports	Zu importierende Klassen (kann leer oder <code>null</code> sein, dann wird {@link SimDynamicSetup#defaultImports} verwendet)
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @return	Ergebnis der Pr�fung als {@link DynamicRunner}-Objekt
	 */
	private DynamicRunner testIntern(final String script, final String imports, final String additionalClassPath) {
		final DynamicMethod cachedDynamicMethod=JavaCodeCache.getJavaCodeCache().getCachedMethod(script,imports,additionalClassPath);
		if (cachedDynamicMethod!=null) return new DynamicRunner(cachedDynamicMethod);

		final DynamicMethod dynamicMethod=new DynamicMethod(setup,script,imports);

		final DynamicStatus status=dynamicMethod.load(additionalClassPath);
		if (status!=DynamicStatus.OK) return new DynamicRunner(script,dynamicMethod.getFullClass(),status,dynamicMethod.getError());
		JavaCodeCache.getJavaCodeCache().storeMethod(script,imports,dynamicMethod);
		return new DynamicRunner(dynamicMethod);
	}

	/**
	 * Pr�ft ein Skript auf Korrektheit.
	 * @param script	Zu pr�fendes Skript
	 * @param imports	Zu importierende Klassen (kann leer oder <code>null</code> sein, dann wird {@link SimDynamicSetup#defaultImports} verwendet)
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @return	Liefert das Skript-Objekt, welches ggf. einen Fehlerstatus besitzt, zur�ck
	 */
	public DynamicRunner test(final String script, final String imports, final String additionalClassPath) {
		return testIntern(script,imports,additionalClassPath);
	}

	/**
	 * Pr�ft ein Skript auf Korrektheit.
	 * @param script	Zu pr�fendes Skript
	 * @param importSettings	Einstellungen zu Import und Classpath f�r Skripte
	 * @return	Liefert das Skript-Objekt, welches ggf. einen Fehlerstatus besitzt, zur�ck
	 */
	public DynamicRunner test(final String script, final ImportSettingsBuilder importSettings) {
		return test(script,importSettings.getImports(),importSettings.getAdditionalClassPath());
	}

	/**
	 * Pr�ft ein Skript auf Korrektheit.
	 * @param script	Zu pr�fendes Skript
	 * @param imports	Zu importierende Klassen (kann leer oder <code>null</code> sein, dann wird {@link SimDynamicSetup#defaultImports} verwendet)
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @param longMessage	Im Falle eines Fehlers die Zeile "Java-Fehler" als erstes mit ausgeben.
	 * @return	Gibt im Erfolgsfall das Skript-Objekt zur�ck, sonst eine Fehlermeldung.
	 */
	public Object test(final String script, final String imports, final String additionalClassPath, final boolean longMessage) {
		final DynamicRunner runner=testIntern(script,imports,additionalClassPath);
		if (runner.isOk()) return runner;
		return getErrorMessage(runner,longMessage);
	}

	/**
	 * Pr�ft ein Skript auf Korrektheit.
	 * @param script	Zu pr�fendes Skript
	 * @param importSettings	Einstellungen zu Import und Classpath f�r Skripte
	 * @param longMessage	Im Falle eines Fehlers die Zeile "Java-Fehler" als erstes mit ausgeben.
	 * @return	Gibt im Erfolgsfall das Skript-Objekt zur�ck, sonst eine Fehlermeldung.
	 */
	public Object test(final String script, final ImportSettingsBuilder importSettings, final boolean longMessage) {
		return test(script,importSettings.getImports(),importSettings.getAdditionalClassPath(),longMessage);
	}

	/**
	 * Erzeugt eine Fehlermeldung basierend auf dem Fehlercode in einem Skript-Objekt
	 * @param runner	Skript-Objekt
	 * @param longMessage	Im Falle eines Fehlers die Zeile "Java-Fehler" als erstes mit ausgeben.
	 * @return	Fehlermeldung
	 */
	public static String getErrorMessage(final DynamicRunner runner, final boolean longMessage) {
		final StringBuilder sb=new StringBuilder();
		if (longMessage) sb.append(Language.tr("Simulation.Java.Error")+"\n");
		sb.append(getStatusText(runner.getStatus())+"\n");
		if (runner.getError()!=null) sb.append(runner.getError()+"\n");
		return sb.toString();
	}

	/**
	 * Pr�ft das Skript und l�dt es im Erfolgsfall.
	 * @param script	Zu ladendes Skript
	 * @param imports	Zu importierende Klassen (kann leer oder <code>null</code> sein, dann wird {@link SimDynamicSetup#defaultImports} verwendet)
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @return	{@link DynamicRunner}-Objekt welches das geladene Skript oder eine Fehlermeldung enth�lt.
	 */
	public DynamicRunner load(final String script, final String imports, final String additionalClassPath) {
		if (!hasCompiler()) return new DynamicRunner(script,script,DynamicStatus.NO_COMPILER,null);

		final DynamicMethod cachedDynamicMethod=JavaCodeCache.getJavaCodeCache().getCachedMethod(script,imports,additionalClassPath);
		if (cachedDynamicMethod!=null) return new DynamicRunner(cachedDynamicMethod);

		final DynamicMethod dynamicMethod=new DynamicMethod(setup,script,imports);
		final DynamicStatus status=dynamicMethod.load(additionalClassPath);
		if (status!=DynamicStatus.OK) return new DynamicRunner(script,dynamicMethod.getFullClass(),status,dynamicMethod.getError());
		JavaCodeCache.getJavaCodeCache().storeMethod(script,imports,dynamicMethod);
		return new DynamicRunner(dynamicMethod);
	}

	/**
	 * Pr�ft das Skript und l�dt es im Erfolgsfall.
	 * @param script	Zu ladendes Skript
	 * @param importSettings	Einstellungen zu Import und Classpath f�r Skripte
	 * @return	{@link DynamicRunner}-Objekt welches das geladene Skript oder eine Fehlermeldung enth�lt.
	 */
	public DynamicRunner load(final String script, final ImportSettingsBuilder importSettings) {
		return load(script,importSettings.getImports(),importSettings.getAdditionalClassPath());
	}

	/**
	 * Erstellt eine Kopie eines vorhandenen Runners (mit neuem Parameter-Set)
	 * @param prototypeRunner	Vorhandener Runner, vom dem die Methode �bernommen werden soll
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @return	Kopie des Runners
	 */
	public DynamicRunner load(final DynamicRunner prototypeRunner, final String additionalClassPath) {
		return new DynamicRunner(prototypeRunner,additionalClassPath);
	}

	/**
	 * Erstellt eine Kopie eines vorhandenen Runners (mit neuem Parameter-Set)
	 * @param prototypeRunner	Vorhandener Runner, vom dem die Methode �bernommen werden soll
	 * @param importSettings	Einstellungen zu Import und Classpath f�r Skripte
	 * @return	Kopie des Runners
	 */
	public DynamicRunner load(final DynamicRunner prototypeRunner, final ImportSettingsBuilder importSettings) {
		return load(prototypeRunner,importSettings.getAdditionalClassPath());
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
		case NO_COMPILER: return Language.tr("Simulation.Java.Error.NoCompiler.Internal")+" ("+System.getProperty("java.vm.name")+"; "+System.getProperty("java.version")+")";
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
	 * @param error	Optionaler zus�tzlicher Fehlerbeschreibungstext
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
	 * Pr�ft, ob es sich bei dem System, auf dem die Anwendung l�uft, um Windows handelt.
	 * @return	Gibt <code>true</code> zur�ck, wenn es sich um ein Windows-System handelt.
	 */
	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	/**
	 * Gibt an, ob die Verarbeitung vollst�ndig im Arbeitsspeicher (ohne tempor�re Dateien) stattfindet.
	 * Nur in diesem Fall kann der Java-Kompiler auf allen Plattformen verwendet werden.
	 * @return	Gibt <code>true</code> zur�ck, wenn vollst�ndig im Arbeitsspeicher (ohne tempor�re Dateien) stattfindet.
	 */
	public static boolean isInMemoryProcessing() {
		return getFactory().setup.getCompileMode().inMemoryProcessing;
	}

	/**
	 * Ist ein Java-Compiler verf�gbar?
	 * (Um in {@link #hasCompiler()} nicht immer wieder danach suchen zu m�ssen.)
	 * @see #hasCompiler()
	 */
	private static boolean hasCompiler=false;

	/**
	 * Gibt an, ob ein Java-Compiler verf�gbar ist.
	 * @return	Java-Compiler verf�gbar?
	 */
	public static boolean hasCompiler() {
		if (hasCompiler) return hasCompiler;
		try {
			final Class<?> cls=Class.forName("javax.tools.ToolProvider");
			final Method method=cls.getMethod("getSystemJavaCompiler");
			final Object compiler=method.invoke(null);
			if (compiler==null) return false;
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			hasCompiler=false;
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

		hasCompiler=true;
		return true;
	}
}
