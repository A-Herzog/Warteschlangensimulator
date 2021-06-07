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

import java.io.Closeable;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basisklasse, die es abgeleiteten Klassen erlaubt, dynamisch Klassen aus Texten oder aus java- oder aus class-Dateien zu laden.
 * @author Alexander Herzog
 */
public abstract class DynamicClassBase implements Closeable, AutoCloseable {
	/**
	 * Liste der Dateien, die beim Schließen dieses Objektes gelöscht werden sollen
	 * @see #addFileToDeleteList(File)
	 * @see #close()
	 */
	private final List<File> deleteFiles=new ArrayList<>();

	/**
	 * Liefert im Falle einiger bestimmter Fehler optional eine zusätzliche Fehlermeldung.
	 * @see #getError()
	 * @see #setError(String)
	 */
	private String error=null;

	/**
	 * Instanz der geladenen Klasse
	 * @see #setLoadedClass(Class)
	 * @see #getLoadedObject()
	 */
	private Class<?> loadedClass=null;

	/**
	 * Konstruktor der Klasse
	 * @param setup	Einstellungen zum Laden der Methode
	 */
	public DynamicClassBase(final DynamicSetup setup) {
	}

	/**
	 * Nimmt eine Datei in die Liste der Dateien, die beim Schließen dieses Objektes gelöscht werden sollen, auf.
	 * @param deleteMeLater	Später zu löschende Datei.
	 */
	protected final void addFileToDeleteList(final File deleteMeLater) {
		deleteFiles.add(deleteMeLater);
	}

	/**
	 * Stellt eine über {@link DynamicClassBase#getError()} abrufbare Fehlermeldung ein.
	 * @param error	Neue Fehlermeldung
	 * @see DynamicClassBase#getError()
	 */
	protected final void setError(final String error) {
		this.error=error;
	}

	/**
	 * Stellt die geladene Klasse ein.
	 * @param loadedClass	Geladene Klasse
	 * @see DynamicClassBase#getLoadedObject()
	 */
	protected final void setLoadedClass(final Class<?> loadedClass) {
		this.loadedClass=loadedClass;
	}

	@Override
	public final void close() {
		int index=0;
		while (index<deleteFiles.size())
			if (deleteFiles.get(index).delete()) deleteFiles.remove(index); else index++;
	}

	/**
	 * Liefert im Falle einiger bestimmter Fehler optional eine zusätzliche Fehlermeldung.
	 * @return	Optionale zusätzliche Fehlermeldung
	 * @see DynamicStatus
	 */
	public final String getError() {
		return error;
	}

	/**
	 * Liefert die geladene Klasse.
	 * @return	Geladenen Klasse oder <code>null</code>, wenn diese nicht verfügbar ist.
	 */
	public final Class<?> getLoadedClass() {
		return loadedClass;
	}

	/**
	 * Liefert eine Instanz der geladenen Klasse.
	 * @return	Instanz der geladenen Klasse oder <code>null</code>, wenn dies nicht möglich ist.
	 */
	public final Object getLoadedObject() {
		/* Keine Klasse geladen? */
		if (loadedClass==null) return null;

		/* Klasse instanzieren */
		try {
			final Constructor<?> construct=loadedClass.getDeclaredConstructor();
			return construct.newInstance();
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}

	/**
	 * Versucht den Text als java-Klasse anzusehen und zu kompilieren.
	 * @param text	Als java-Klasse anzusehender Text
	 * @return	Liefert im Erfolgsfall ein Objekt vom Typ {@link File}, das die Klasse repräsentiert, oder einen Klassennamen als String zurück. Ansonsten eine Fehlermeldung als {@link DynamicStatus}-Objekt.
	 */
	public abstract Object prepare(final String text);

	/**
	 * Lädt die Klasse aus dem übergebenen Parameter (File oder String, je nach dem, was {@link DynamicClassBase#prepare(String)} liefert)
	 * und setzt diese per {@link DynamicClassBase#setLoadedClass(Class)}.
	 * @param classData	Angaben zu der zu ladenden Klasse (Rückgabewert von {@link DynamicClassBase#prepare(String)})
	 * @return	Statusmeldung, die angibt, ob der Ladevorgang erfolgreich war
	 */
	protected abstract DynamicStatus loadClass(final Object classData);

	/**
	 * Lädt eine Klasse aus einer Zeichenkette
	 * @param text	Zeichenkette, die als java-Datei interpretiert werden soll
	 * @return	Statusmeldung, die angibt, ob der Ladevorgang erfolgreich war
	 */
	public final DynamicStatus prepareAndLoad(final String text) {
		/* Klasse vorbereiten */
		final Object result=prepare(text);
		if (result instanceof DynamicStatus) return (DynamicStatus)result;

		/* Klasse laden */
		return loadClass(result);
	}

	/**
	 * Interpretiert einen Text als java-Datei und extrahiert aus dieser den Namen der Klasse
	 * @param text	Text, der eine java-Datei darstellen soll
	 * @return	Name der Klasse oder <code>null</code>, wenn der Name nicht ermittelt werden konnte
	 */
	protected static final String getClassName(String text) {
		int index;
		index=text.indexOf(" class ");
		if (index<0) return null;
		text=text.substring(index+7);
		index=text.indexOf(" ");
		if (index<0) return null;
		text=text.substring(0,index).trim();
		if (text.isEmpty()) return null;
		return text;
	}
}
