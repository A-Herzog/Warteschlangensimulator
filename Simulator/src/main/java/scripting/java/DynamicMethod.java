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

/**
 * Versucht eine java-Methode in eine Datei zu verpacken und diese dynamisch zu laden.
 * @author Alexander Herzog
 */
public final class DynamicMethod {
	/**
	 * Fortlaufender Zähler für IDs in Klassennamen
	 * @see #getNextClassID()
	 */
	private static long idCounter;

	/**
	 * Sichert die Aufrufe von {@link #getNextClassID()} ab.
	 * @see #getNextClassID()
	 */
	private static final Semaphore mutex;

	/**
	 * Einstellungen zum Laden der Methode
	 */
	private final DynamicSetup setup;

	/**
	 * Text, der als java-Methode interpretiert werden soll. Der Text muss mit dem Rückgabewert beginnen, darf also keinen Access-Modifier enthalten.
	 */
	private final String methodText;

	/**
	 * Name der zu generierenden Klasse
	 */
	private final String className;

	/**
	 * Vollständiger Text der Klassen-Datei
	 */
	private final String classText;

	/**
	 * Liefert optional eine zusätzliche Fehlermeldung, wenn das Laden nicht erfolgreich war.
	 * @see #getError()
	 */
	private String error;

	/**
	 * Instanz der geladenen Klasse
	 * @see #getDynamicObject()
	 * @see #load()
	 */
	private Object dynamicObject;

	/**
	 * Aufzurufende Methode innerhalb von {@link #dynamicObject}
	 * @see #initDynamicMethod()
	 * @see #invokeDynamicMethod(Object)
	 */
	private Method dynamicMethod;

	static {
		mutex=new Semaphore(1);
		idCounter=System.nanoTime()%1_000_000_000_000L;
	}

	/**
	 * Erstellt einen eindeutigen (einmaligen) Klassennamen
	 * @param id	Eindeutige, einmalige ID für den Klassennamen
	 * @return		Klassenname
	 * @see #getNextClassID()
	 */
	private static String buildClassID(long id) {
		final StringBuilder sb=new StringBuilder();
		while (id>0) {
			final char c=(char)('A'+(id%26));
			sb.append(c);
			id=id/26;
		}
		return sb.toString();
	}

	/**
	 * Liefert fortlaufende IDs die als Teil von {@link #className} verwendet
	 * werden können, um systemweit eindeutige Klassennamen generieren zu können.
	 * @return	Für das Gesamtsystem eindeutige fortlaufende ID für die Klassennamen
	 */
	private static String getNextClassID() {
		mutex.acquireUninterruptibly();
		try {
			return buildClassID(idCounter++);
		} finally {
			mutex.release();
		}
	}

	/**
	 * Entfernt alle möglichen Modifizierer vor dem Namen der einzubindenden Methode.
	 * @param methodText	Vollständiger Text der Methode inkl. möglichen Modifizierern vor dem Namen der Methode
	 * @return	Text der Methode ohne Modifizierer vor dem Namen
	 */
	private String removeModifiers(String methodText) {
		methodText=methodText.trim();
		if (methodText.toLowerCase().startsWith("public ")) methodText=methodText.substring(7).trim();
		if (methodText.toLowerCase().startsWith("private ")) methodText=methodText.substring(8).trim();
		if (methodText.toLowerCase().startsWith("protected ")) methodText=methodText.substring(9).trim();
		if (methodText.toLowerCase().startsWith("static ")) methodText=methodText.substring(7).trim();
		if (methodText.toLowerCase().startsWith("final ")) methodText=methodText.substring(6).trim();
		return methodText;
	}

	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 * @param methodText	Text, der als java-Methode interpretiert werden soll. Der Text muss mit dem Rückgabewert beginnen, darf also keinen Access-Modifier enthalten.
	 */
	public DynamicMethod(final DynamicSetup setup, final String methodText) {
		this.setup=setup;
		this.methodText=methodText;
		className=setup.getTempClassName()+getNextClassID();

		final StringBuilder sb=new StringBuilder();
		final String[] imports=setup.getImports();
		if (imports!=null && imports.length>0) {
			for (String line: imports) sb.append("import "+line+";\n");
			sb.append("\n");
		}
		sb.append("public class "+className+" { /* Class name is random. */ \n");
		sb.append("  /* --- User code starts here. --- */\n");
		final String method="public "+removeModifiers(methodText);
		for (String line: method.split("\\n")) {
			sb.append("  "+line+"\n");
		}
		sb.append("  /* --- User code ends here. --- */\n");
		sb.append("}\n");
		classText=sb.toString();
	}

	/**
	 * Liefert die Klasse, die zum Kompilieren und Laden der Klasse verwendet werden soll.
	 * @return	Klasse, die zum Kompilieren und Laden der Klasse verwendet werden soll.
	 * @see #test()
	 * @see #load()
	 */
	private Class<? extends DynamicClassBase> getDynamicClassClass() {
		return setup.getCompileMode().dynamicLoaderClass;
	}

	/**
	 * Prüft, ob die Methode in Ordnung ist.
	 * @return	Statuscode, der den Erfolg beschreibt.
	 * @see DynamicStatus
	 * @see DynamicMethod#getError()
	 */
	public DynamicStatus test() {
		try (final DynamicClassBase dynamicClass=getDynamicClassClass().getConstructor(DynamicSetup.class).newInstance(setup)) {
			final Object result=dynamicClass.prepare(classText);
			error=dynamicClass.getError();
			if (result instanceof DynamicStatus) return (DynamicStatus)result;
			return DynamicStatus.OK;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException	| NoSuchMethodException | SecurityException e) {
			return DynamicStatus.LOAD_ERROR;
		}
	}

	/**
	 * Versucht die Methode zu laden.
	 * @return	Statuscode, der den Erfolg beschreibt.
	 * @see DynamicStatus
	 * @see DynamicMethod#getError()
	 */
	public DynamicStatus load() {
		try (final DynamicClassBase dynamicClass=getDynamicClassClass().getConstructor(DynamicSetup.class).newInstance(setup)) {
			final DynamicStatus result=dynamicClass.prepareAndLoad(classText);
			error=dynamicClass.getError();
			if (result==DynamicStatus.OK) dynamicObject=dynamicClass.getLoadedObject();
			return result;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException	| NoSuchMethodException | SecurityException e) {
			error=e.getMessage();
			return DynamicStatus.LOAD_ERROR;
		}
	}

	/**
	 * Liefert optional eine zusätzliche Fehlermeldung, wenn das Laden nicht erfolgreich war.
	 * @return	Optionale zusätzliche Fehlermeldung
	 * @see DynamicStatus
	 * @see DynamicMethod#test()
	 * @see DynamicMethod#load()
	 */
	public String getError() {
		return error;
	}

	/**
	 * Liefert die Instanz der geladenen Klasse
	 * @return	Instanz der geladenen Klasse oder <code>null</code>, wenn dies nicht möglich ist.
	 */
	public Object getDynamicObject() {
		return dynamicObject;
	}

	/**
	 * Setzt das Feld  {@link DynamicMethod#dynamicMethod} auf die Methode innerhalb der geladenen Klasse
	 * @return	Gibt an, ob das Feld korrekt gesetzt werden konnte.
	 */
	private boolean initDynamicMethod() {
		if (dynamicMethod!=null) return true;

		if (dynamicObject==null) return false;

		for (Method method: dynamicObject.getClass().getMethods()) {
			if (method.getDeclaringClass().getName().equals(className)) {dynamicMethod=method; return true;}
		}

		return false;
	}

	/**
	 * Hält das Objekt zur Übergabe der Parameter an die benutzerdefinierte
	 * Methode vor, um so das erneute Anlegen des Arrays für die
	 * Parameter zu vermeiden.
	 * @see #invokeDynamicMethod(Object)
	 */
	private final Object[] paramsHolder=new Object[1];

	/**
	 * Führt die Methode innerhalb der dynamisch geladenen Klasse aus.
	 * @param parameter	Parameter für die Methode
	 * @return	Rückgabe der Methode
	 */
	public Object invokeDynamicMethod(final Object parameter) {
		if (dynamicMethod==null) {
			if (!initDynamicMethod()) return DynamicStatus.RUN_ERROR;
		}

		try {
			paramsHolder[0]=parameter;
			return dynamicMethod.invoke(dynamicObject,paramsHolder);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			if (e instanceof InvocationTargetException) {
				final Throwable e2=((InvocationTargetException)e).getTargetException();
				final String msg=e2.getMessage();
				if (msg==null) error=e2.getClass().getName(); else error=e2.getClass().getName()+": "+msg;
			} else {
				final String msg=e.getMessage();
				if (msg==null) error=e.getClass().getName(); else error=e.getClass().getName()+": "+msg;
			}
			return DynamicStatus.RUN_ERROR;
		}
	}

	/**
	 * Liefert den Textinhalt der Methode.
	 * @return	Textinhalt der Methode
	 */
	public String getScript() {
		return methodText;
	}

	/**
	 * Liefert den vollständigen Text der Klasse.
	 * @return	Text der Klasse
	 */
	public String getFullClass() {
		return classText;
	}
}