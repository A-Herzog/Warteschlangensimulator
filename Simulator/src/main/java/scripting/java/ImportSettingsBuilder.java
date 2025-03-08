/**
 * Copyright 2021 Alexander Herzog
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import simulator.editmodel.EditModel;

/**
 * Diese Klasse bereitet die Einstellungen aus einem
 * {@link EditModel} zu Java-Imports und Classpath
 * für {@link DynamicFactory} auf.
 * @author Alexander Herzog
 * @see EditModel
 * @see DynamicFactory
 */
public class ImportSettingsBuilder {
	/**
	 * Ausgangs-Editor-Modell
	 */
	private final EditModel model;

	/**
	 * Ist das Laden von allgemeinen nutzerdefinierten Klassen zulässig?
	 */
	private boolean allowLoadClasses;

	/**
	 * Speichert die per {@link #buildImports()} generierten
	 * Imports für weitere Abrufe.
	 * @see #buildImports()
	 * @see #getImports()
	 */
	private String imports;

	/**
	 * Packages, die nicht als zusätzliche Imports angeboten werden müssen
	 */
	private static final String[] IGNORE_PACKAGES=new String[] {
			"scripting.java.*"
	};

	/**
	 * Konstruktor der Klasse
	 * @param model	Ausgangs-Editor-Modell
	 */
	public ImportSettingsBuilder(final EditModel model) {
		this.model=model;
		allowLoadClasses=(model!=null && model.pluginsFolderAllowClassLoad && model.pluginsFolder!=null && !model.pluginsFolder.isBlank());
	}

	/**
	 * Erstellt eine Liste von allen zu importierenden Packages ausgehend von einem Basisverzeichnis aus.
	 * @param list	Liste der zu importierenden Packages
	 * @param folder	Ausgangsverzeichnis
	 * @param packageName	Packagebezeichner für das aktuelle Verzeichnis (kann <code>null</code> sein für das Basisverzeichnis)
	 */
	private void getPluginFolderImports(final List<String> list, final File folder, final String packageName) {
		if (folder==null || !folder.isDirectory()) return;
		final String[] fileNames=folder.list();
		if (fileNames==null) return;
		boolean added=false;
		for (String fileName: fileNames) {
			final String fileNameLower=fileName.toLowerCase();
			if ((fileNameLower.endsWith(".java") || fileNameLower.endsWith(".class")) && !added && packageName!=null) {
				final String importPackage=packageName+".*";
				boolean ignore=false;
				for (String ignorePackage: IGNORE_PACKAGES) if (importPackage.equals(ignorePackage)) {ignore=true; break;}
				if (!ignore) list.add(importPackage);
				added=true;
				continue;
			}
			final File file=new File(folder,fileName);
			if (file.isDirectory()) getPluginFolderImports(list,file,(packageName==null)?fileName:(packageName+"."+fileName));
		}
	}

	/**
	 * Erstellt die Liste der zu importierenden Klassen als Enter-Zeichen getrennte Einträge in einer Zeichenkette.
	 * @return	Zu importierende Klassen
	 * @see #getImports()
	 */
	private String buildImports() {
		final List<String> imports=new ArrayList<>();

		/* Standard-Imports oder Definition gemäß Modell */
		if (model==null || model.javaImports==null || model.javaImports.isBlank()) {
			imports.addAll(Arrays.asList(SimDynamicSetup.defaultImports.split("\\n")));
		} else {
			imports.addAll(Arrays.asList(model.javaImports.split("\\n")));
		}

		/* Import der Plugins-Verzeichnis-Packages */
		if (allowLoadClasses) {
			if (model!=null && model.pluginsFolder!=null) getPluginFolderImports(imports,new File(model.pluginsFolder),null);
		}

		return String.join("\n",imports);

	}

	/**
	 * Liefert die zu importierenden Klassen als Enter-Zeichen getrennte Einträge in einer Zeichenkette.
	 * @return	Zu importierende Klassen
	 */
	public synchronized String getImports() {
		if (model==null) return null;
		if (imports==null) imports=buildImports();
		return imports;
	}

	/**
	 * Liefert den zusätzlich zu verwendenden Classpath.
	 * @return	Zusätzlich zu verwendender Classpath
	 */
	public synchronized String getAdditionalClassPath() {
		if (model==null) return null;
		if (allowLoadClasses) {
			return model.pluginsFolder;
		} else {
			return null;
		}
	}

	/**
	 * Ist das Laden von allgemeinen nutzerdefinierten Klassen zulässig?
	 * @return	Ist das Laden von allgemeinen nutzerdefinierten Klassen zulässig?
	 */
	public boolean isAllowLoadClasses() {
		return allowLoadClasses;
	}
}
