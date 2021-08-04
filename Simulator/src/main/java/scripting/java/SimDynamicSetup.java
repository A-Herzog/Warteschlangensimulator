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

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tools.SetupData;

/**
 * Liefert die konkreten Einstellungen für das Laden von Java-Code
 * mit Hilfe von {@link DynamicMethod} im Simulator-Kontext.
 * @author Alexander Herzog
 * @see DynamicMethod
 * @see DynamicSetup
 */
public class SimDynamicSetup implements DynamicSetup {
	/**
	 * Zu verwendender Class-Path
	 * @see #getClassPath()
	 */
	private String classPath=null;

	@Override
	public String getTempFolderName() {
		return "QSSim";
	}

	@Override
	public String getTempClassName() {
		return "SimHelperClassRandomId";
	}

	/**
	 * Vorgabewerte für die Imports
	 */
	public static final String defaultImports=String.join("\n","scripting.java.*", "java.lang.*", "java.math.*", "java.util.*", "java.util.function.*", "java.util.stream.*");

	@Override
	public String[] getImports(final String userImports) {
		final List<String> list=new ArrayList<>();
		if (userImports==null || userImports.trim().isEmpty()) {
			list.addAll(Arrays.asList(defaultImports.split("\\n")));
		} else {
			list.addAll(Arrays.asList(userImports.split("\\n")));
		}
		list.addAll(SetupData.getSetup().dynamicImportClasses);
		return list.toArray(new String[0]);
	}

	@Override
	public String getClassPath() {
		if (classPath==null) {
			String s;

			try {
				s=new File(DynamicFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).toString();
			} catch (URISyntaxException e) {
				s=Paths.get(".").toAbsolutePath().normalize().toString();
			}
			if (s.toLowerCase().endsWith(".jar")) {
				int index=s.lastIndexOf(File.separator);
				if (index>=0) s=s.substring(0,index+1);
			}
			if (!s.endsWith(File.separator)) s=s+File.separator;

			classPath=s;
		}


		return classPath;
	}

	@Override
	public String getJDKPath() {
		final SetupData setup=SetupData.getSetup();
		String path;

		path=setup.javaJDKPath.trim();
		if (getCompilerFromJDKPath(path)!=null) return path;

		path=getDefaultJavaPath();
		if (getCompilerFromJDKPath(path)!=null) {
			if (setup.javaJDKPath.trim().isEmpty()) {setup.javaJDKPath=path; setup.saveSetup();}
			return path;
		}

		path=findJDK(setup.javaJDKPath.trim());
		if (getCompilerFromJDKPath(path)!=null) {
			if (setup.javaJDKPath.trim().isEmpty()) {setup.javaJDKPath=path; setup.saveSetup();}
			return path;
		}

		path=findJDK(getDefaultJavaPath());
		if (getCompilerFromJDKPath(path)!=null) {
			if (setup.javaJDKPath.trim().isEmpty()) {setup.javaJDKPath=path; setup.saveSetup();}
			return path;
		}

		return getDefaultJavaPath();
	}

	@Override
	public CompileMode getCompileMode() {
		/*
		 * return CompileMode.EXTERNAL_COMPILER;
		 * return CompileMode.INTERNAL_COMPILER;
		 * return CompileMode.INTERNAL_COMPILER_NO_JAVA_FILE;
		 * return CompileMode.INTERNAL_COMPILER_NO_FILES;
		 */
		return CompileMode.INTERNAL_COMPILER_NO_FILES;
	}

	/**
	 * Sucht das JDK ausgehend von einem Startpfad.
	 * @param startPath	Startpfad für die Suche (es wird in den Unterverzeichnissen gesucht)
	 * @return	Pfad zum JDK
	 * @see #getJDKPath()
	 */
	private String findJDK(String startPath) {
		if (startPath==null || startPath.trim().isEmpty()) return null;
		startPath=startPath.trim();
		if (startPath.endsWith(File.separator)) startPath=startPath.substring(0,startPath.length()-1);
		final int index=startPath.lastIndexOf(File.separator);
		if (index<0) return null;
		startPath=startPath.substring(0,index);
		final File base=new File(startPath);
		if (!base.isDirectory()) return null;
		final String[] list=base.list();
		if (list!=null) for (String rec: list) {
			final File sub=new File(base,rec);
			if (sub.isDirectory()) {
				if (getCompilerFromJDKPath(sub.toString())!=null) return sub.toString();
			}
		}
		return null;
	}

	/**
	 * Liefert den Fallback-Pfad für die Java-Umgebung (die hoffentlich auch javac enthält).
	 * @return	Standardpfad für die Java-Umgebung
	 */
	public static String getDefaultJavaPath() {
		return System.getProperty("java.home");
	}

	/**
	 * Versucht den Java-Kompiler "javac" in dem angegebenen Verzeichnis oder in einem "bin"-Unterverzeichnis des angegebenen Verzeichnisses zu finden
	 * @param path	Basispfad für die JDK-Umgebung
	 * @return	Kompiler-Datei oder <code>null</code>, wenn kein Kompiler gefunden wurde.
	 */
	public static File getCompilerFromJDKPath(final String path) {
		if (path==null || path.trim().isEmpty()) return null;

		String compilerName=path;
		if (!compilerName.endsWith(File.separator)) compilerName=compilerName+File.separator;
		compilerName=compilerName+"bin"+File.separator+"javac.exe";
		File compiler=new File(compilerName);
		if (compiler.isFile()) return compiler;

		compilerName=path;
		if (!compilerName.endsWith(File.separator)) compilerName=compilerName+File.separator;
		compilerName=compilerName+"javac.exe";
		compiler=new File(compilerName);
		if (compiler.isFile()) return compiler;

		return null;
	}
}
