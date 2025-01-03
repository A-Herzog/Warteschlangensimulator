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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;

/**
 * Versucht eine java-Methode in eine Datei zu verpacken und diese dynamisch zu laden.
 * @author Alexander Herzog
 */
public final class DynamicMethod {
	/**
	 * Aufruf �ber <code>MethodHandle</code> statt �ber <code>Method</code>
	 * soll schneller sein - ist er aber leider nicht.
	 */
	private static boolean USE_DYNAMIC_METHOD_HANDLE=false;

	/**
	 * Fortlaufender Z�hler f�r IDs in Klassennamen
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
	 * Text, der als java-Methode interpretiert werden soll. Der Text muss mit dem R�ckgabewert beginnen, darf also keinen Access-Modifier enthalten.
	 */
	private final String methodText;

	/**
	 * Name der zu generierenden Klasse
	 */
	private final String className;

	/**
	 * Vollst�ndiger Text der Klassen-Datei
	 */
	private final String classText;

	/**
	 * Liefert optional eine zus�tzliche Fehlermeldung, wenn das Laden nicht erfolgreich war.
	 * @see #getError()
	 */
	private String error;

	/**
	 * Geladene Klasse
	 * @see #load(String)
	 */
	private Class<?> dynamicClass;

	/**
	 * Instanz der geladenen Klasse
	 * @see #getDynamicObject()
	 * @see #load(String)
	 */
	private Object dynamicObject;

	/**
	 * Aufzurufende Methode innerhalb von {@link #dynamicObject}
	 * @see #initDynamicMethod()
	 * @see #invokeDynamicMethod(Object)
	 */
	private Method dynamicMethod;

	/**
	 * Aufzurufende Methode innerhalb von {@link #dynamicObject}
	 * als {@link MethodHandle} (soll schneller sein ein {@link Method}).
	 * @see #initDynamicMethod()
	 * @see #invokeDynamicMethod(Object)
	 */
	private MethodHandle dynamicMethodHandle;

	static {
		mutex=new Semaphore(1);
		idCounter=System.nanoTime()%1_000_000_000_000L;
	}

	/**
	 * Erstellt einen eindeutigen (einmaligen) Klassennamen
	 * @param id	Eindeutige, einmalige ID f�r den Klassennamen
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
	 * werden k�nnen, um systemweit eindeutige Klassennamen generieren zu k�nnen.
	 * @return	F�r das Gesamtsystem eindeutige fortlaufende ID f�r die Klassennamen
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
	 * Entfernt alle m�glichen Modifizierer vor dem Namen der einzubindenden Methode.
	 * @param methodText	Vollst�ndiger Text der Methode inkl. m�glichen Modifizierern vor dem Namen der Methode
	 * @return	Text der Methode ohne Modifizierer vor dem Namen
	 */
	/*
	private String removeModifiers(String methodText) {
		methodText=methodText.trim();
		if (methodText.toLowerCase().startsWith("public ")) methodText=methodText.substring(7).trim();
		if (methodText.toLowerCase().startsWith("private ")) methodText=methodText.substring(8).trim();
		if (methodText.toLowerCase().startsWith("protected ")) methodText=methodText.substring(9).trim();
		if (methodText.toLowerCase().startsWith("static ")) methodText=methodText.substring(7).trim();
		if (methodText.toLowerCase().startsWith("final ")) methodText=methodText.substring(6).trim();
		return methodText;
	}
	 */

	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 * @param methodText	Text, der als java-Methode interpretiert werden soll. Der Text muss mit dem R�ckgabewert beginnen, darf also keinen Access-Modifier enthalten.
	 * @param userImports	Optionale nutzerdefinierte Imports (kann <code>null</code> oder leer sein)
	 */
	public DynamicMethod(final DynamicSetup setup, final String methodText, final String userImports) {
		this.setup=setup;
		this.methodText=methodText;
		className=setup.getTempClassName()+getNextClassID();

		final StringBuilder sb=new StringBuilder();
		final String[] imports=setup.getImports(userImports);
		if (imports!=null && imports.length>0) {
			for (String line: imports) sb.append("import "+line+";\n");
			sb.append("\n");
		}
		sb.append("public class "+className+" { /* Class name is random. */ \n");
		sb.append("  /* --- User code starts here. --- */\n");
		final String method=/* "public "+removeModifiers(...*/ methodText;
		for (String line: method.split("\\n")) {
			sb.append("  "+line+"\n");
		}
		sb.append("  /* --- User code ends here. --- */\n");
		sb.append("}\n");
		classText=sb.toString();
	}

	/**
	 * Copy-Konstruktor (aber mit neuer Objekt-Instanz)
	 * @param prototypeMethod	Vorhandene dynamisch geladene Methode (Klasse wird �bernommen, aber eine neue Instanz des inneren Objektes erstellt)
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 */
	public DynamicMethod(final DynamicMethod prototypeMethod, final String additionalClassPath) {
		setup=prototypeMethod.setup;
		methodText=prototypeMethod.methodText;
		className=prototypeMethod.className;
		classText=prototypeMethod.classText;
		error=prototypeMethod.error;

		if (prototypeMethod.dynamicClass==null) {
			prototypeMethod.load(additionalClassPath);
		}

		if (prototypeMethod.dynamicClass!=null) {
			dynamicClass=prototypeMethod.dynamicClass;

			try {
				final Constructor<?> construct=dynamicClass.getDeclaredConstructor();
				dynamicObject=construct.newInstance();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}
	}

	/**
	 * Liefert die Klasse, die zum Kompilieren und Laden der Klasse verwendet werden soll.
	 * @return	Klasse, die zum Kompilieren und Laden der Klasse verwendet werden soll.
	 * @see #test(String)
	 * @see #load(String)
	 */
	private Class<? extends DynamicClassBase> getDynamicClassClass() {
		return setup.getCompileMode().dynamicLoaderClass;
	}

	/**
	 * Pr�ft, ob die Methode in Ordnung ist.
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @return	Statuscode, der den Erfolg beschreibt.
	 * @see DynamicStatus
	 * @see DynamicMethod#getError()
	 */
	public DynamicStatus test(final String additionalClassPath) {
		try (final DynamicClassBase dynamicClass=getDynamicClassClass().getConstructor(DynamicSetup.class).newInstance(setup,additionalClassPath)) {
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
	 * @param additionalClassPath	Optionaler zus�tzlicher �ber den Classloader bereit zu stellender Classpath (kann <code>null</code> sein)
	 * @return	Statuscode, der den Erfolg beschreibt.
	 * @see DynamicStatus
	 * @see DynamicMethod#getError()
	 */
	public DynamicStatus load(final String additionalClassPath) {
		try (final DynamicClassBase dynamicClass=getDynamicClassClass().getConstructor(DynamicSetup.class,String.class).newInstance(setup,additionalClassPath)) {
			final DynamicStatus result=dynamicClass.prepareAndLoad(classText);
			error=dynamicClass.getError();
			if (result==DynamicStatus.OK) {
				this.dynamicClass=dynamicClass.getLoadedClass();
				dynamicObject=dynamicClass.getLoadedObject();
			}
			return result;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException	| NoSuchMethodException | SecurityException e) {
			error=e.getMessage();
			return DynamicStatus.LOAD_ERROR;
		}
	}

	/**
	 * Liefert optional eine zus�tzliche Fehlermeldung, wenn das Laden nicht erfolgreich war.
	 * @return	Optionale zus�tzliche Fehlermeldung
	 * @see DynamicStatus
	 * @see DynamicMethod#test(String)
	 * @see DynamicMethod#load(String)
	 */
	public String getError() {
		return error;
	}

	/**
	 * Liefert die Instanz der geladenen Klasse
	 * @return	Instanz der geladenen Klasse oder <code>null</code>, wenn dies nicht m�glich ist.
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

		final Class<? extends Object> cls=dynamicObject.getClass();
		for (Method method: cls.getDeclaredMethods()) {
			if (method.getParameterCount()!=1) continue;
			if (method.getParameterTypes()[0]!=SimulationInterface.class) continue;

			method.setAccessible(true);
			/* Brauchen wir nicht, da wir nur die hier deklarierten Methoden betrachten: if (method.getDeclaringClass().getName().equals(className)) {dynamicMethod=method; return true;} */
			dynamicMethod=method;
			if (USE_DYNAMIC_METHOD_HANDLE) {
				final MethodHandles.Lookup publicLookup=MethodHandles.publicLookup();
				try {
					dynamicMethodHandle=publicLookup.unreflect(dynamicMethod);
					dynamicMethodHandle=dynamicMethodHandle.bindTo(dynamicObject);
				} catch (IllegalAccessException e) {}
			}
			return true;
		}
		/*
		Fragt auch die in Object deklarierten Methoden ab:
		for (Method method: cls.getMethods()) {
			System.out.println(method.toString());
			if (method.getDeclaringClass().getName().equals(className)) {dynamicMethod=method; return true;}
		}
		 */

		return false;
	}

	/**
	 * H�lt das Objekt zur �bergabe der Parameter an die benutzerdefinierte
	 * Methode vor, um so das erneute Anlegen des Arrays f�r die
	 * Parameter zu vermeiden.
	 * @see #invokeDynamicMethod(Object)
	 */
	private final Object[] paramsHolder=new Object[1];

	/**
	 * F�hrt die Methode innerhalb der dynamisch geladenen Klasse aus.
	 * @param parameter	Parameter f�r die Methode
	 * @return	R�ckgabe der Methode
	 */
	public Object invokeDynamicMethod(final Object parameter) {
		if (dynamicMethod==null) {
			if (!initDynamicMethod()) return DynamicStatus.RUN_ERROR;
		}

		try {
			paramsHolder[0]=parameter;
			if (dynamicMethodHandle!=null) {
				return dynamicMethodHandle.invoke(parameter);
			} else {
				return dynamicMethod.invoke(dynamicObject,paramsHolder);
			}
		} catch (Throwable /*| IllegalAccessException | IllegalArgumentException | InvocationTargetException*/ e) {
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
	 * Liefert den vollst�ndigen Text der Klasse.
	 * @return	Text der Klasse
	 */
	public String getFullClass() {
		return classText;
	}
}