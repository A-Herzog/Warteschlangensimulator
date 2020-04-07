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
	private static long idCounter;
	private static final Semaphore mutex;

	private final DynamicSetup setup;
	private final String methodText;
	private final String className;
	private final String classText;
	private String error;
	private Object dynamicObject;
	private Method dynamicMethod;

	static {
		mutex=new Semaphore(1);
		idCounter=System.nanoTime()%1_000_000_000_000L;
	}

	private static String buildClassID(long id) {
		final StringBuilder sb=new StringBuilder();
		while (id>0) {
			final char c=(char)('A'+(id%26));
			sb.append(c);
			id=id/26;
		}
		return sb.toString();
	}

	private static String getNextClassID() {
		mutex.acquireUninterruptibly();
		try {
			return buildClassID(idCounter++);
		} finally {
			mutex.release();
		}
	}

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
		sb.append("public class "+className+" {\n");
		sb.append("public "+removeModifiers(methodText));
		sb.append("\n}\n");
		classText=sb.toString();
	}

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
				error=e2.getClass().getName()+": "+e2.getMessage();
			} else {
				error=e.getClass().getName()+": "+e.getMessage();
			}
			return DynamicStatus.RUN_ERROR;
		}
	}

	/**
	 * Liefert den Textinhalt der Methode
	 * @return	Textinhalt der Methode
	 */
	public String getScript() {
		return methodText;
	}
}