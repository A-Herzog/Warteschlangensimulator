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
package ui.modeleditor.outputbuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import language.Language;
import simulator.editmodel.EditModel;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Exportiert ein Modell in einem durch eine von dieser
 * Klasse abgeleiteten Klasse definierten Textformat.
 * @author Alexander Herzog
 */
public abstract class SpecialOutputBuilder {
	/**
	 * Zu exportierendes Modell
	 */
	public final EditModel model;

	/**
	 * Ausgabezeilen für Kopfbereich
	 */
	public final StringBuilder outputHead;

	/**
	 * Ausgabezeilen für zentralen Bereich
	 */
	public final StringBuilder outputBody;

	/**
	 * Ausgabezeilen für Fußbereich
	 */
	public final StringBuilder outputFoot;

	/**
	 * Konstruktor der Klasse
	 * @param model	Zu exportierendes Modell
	 */
	public SpecialOutputBuilder(final EditModel model) {
		this.model=model;
		outputHead=new StringBuilder();
		outputBody=new StringBuilder();
		outputFoot=new StringBuilder();
	}

	/**
	 * Datenkopf der Textausgabe
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected String processHead() {
		return null;
	}

	/**
	 * Abschluss der Textausgabe
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected String processFoot() {
		return null;
	}

	/**
	 * Fügt ein Element zu der Ausgabe hinzu
	 * @param element	Element, das in die Ausgabe aufgenommen werden soll
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	protected String processElement(final ModelElement element) {
		element.specialOutput(this);
		return null;
	}

	/**
	 * Führt die komplette Verarbeitung durch
	 * @return	Liefert im Erfolgsfall ein Array aus drei Elementen: Kopf, Inhalt, Fuß. Im Fehlerfall ein Array aus einem Element: Fehlermeldung.
	 */
	public String[] build() {
		String error;

		error=processHead();
		if (error!=null) return new String[]{error};

		for (ModelElement element: model.surface.getElements()) {
			error=processElement(element);
			if (error!=null) return new String[]{error};
		}

		error=processFoot();
		if (error!=null) return new String[]{error};

		return new String[]{outputHead.toString(),outputBody.toString(),outputFoot.toString()};
	}

	/**
	 * Führt die komplette Verarbeitung durch
	 * @param file	Zieldatei für den Export
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public final String build(final File file) {
		final String[] data=build();
		if (data.length==1) return data[0]; /* Fehler */

		final StringBuilder sb=new StringBuilder(data[0].length()+data[1].length()+data[2].length());
		sb.append(data[0]);
		sb.append(data[1]);
		sb.append(data[2]);

		try {
			Files.write(file.toPath(),sb.toString().getBytes(),StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			return String.format(Language.tr("Window.ExportSaveError"),file.toString());
		}

		return null;
	}
}