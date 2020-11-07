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
package scripting.js;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;

/**
 * Stellt das "Output"-Objekt in Javascript-Umgebungen zur Verfügung
 * @author Alexander Herzog
 */
public final class JSCommandOutput extends JSBaseCommand {
	/** Wird hier <code>true</code> übergeben, so erfolgt die Ausgabe der eigentlichen Meldungen in eine Datei */
	private final boolean outputToFile;
	/** Wird hier <code>true</code> übergeben, so erfolgt die Ausgabe der eigentlichen Meldungen in eine Datei */
	private File outputFile;
	/** Abbruch-Status ({@link #cancel()}) */
	private boolean canceled=false;
	/** Als Zeitangabe (<code>true</code>) oder als Zahl (<code>false</code>) ausgeben? */
	private boolean time;
	/** Ausgabe als Prozentwert (<code>true</code>) oder normale Fließkommazahl (<code>false</code>)? */
	private boolean percent;
	/** Legt fest, ob Zahlen in System- (<code>true</code>) oder lokaler Notation (<code>false</code>) ausgegeben werden sollen. */
	private boolean systemNumbers;
	/** Trennzeichen für die Ausgabe von Verteilungsdaten */
	private char separator=';';

	/**
	 * Ist die Ausgabe nur eine Double-Zahl?
	 * @see #isOutputDouble()
	 * @see #getOutputDouble()
	 */
	private boolean isOutputPlainDouble=true;

	/**
	 * Wenn es sich bei der Ausgabe nur um eine Double-Zahl handelt, so hält dieses Feld die Zahl vor.
	 * @see #isOutputDouble()
	 * @see #getOutputDouble()
	 */
	private double outputPlainDouble;

	/**
	 * Cache für {@link #printDouble(double)},
	 * um das {@link StringBuilder}-Objekt nicht
	 * immer wieder neu anlegen zu müssen.
	 * @see #printDouble(double)
	 */
	private StringBuilder outputTemp;

	/**
	 * Konstruktor der Klasse <code>JSCommandOutput</code>
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 * @param outputToFile	Wird hier <code>true</code> übergeben, so erfolgt die Ausgabe der eigentlichen Meldungen in eine Datei
	 * @see #setFile(Object)
	 */
	public JSCommandOutput(final JSOutputWriter output, final boolean outputToFile) {
		super(output);
		this.outputToFile=outputToFile;
	}

	/**
	 * Stellt das Ausgabeformat für Zahlen ein
	 * @param obj	Zeichenkette, über die das Format (Dezimalkomma sowie optional Prozentwert) für Zahlenausgaben festgelegt wird
	 */
	public void setFormat(final Object obj) {
		if (!(obj instanceof String)) return;
		final String parameter=(String)obj;

		if (parameter.equalsIgnoreCase("lokal") || parameter.equalsIgnoreCase("local")) {systemNumbers=false; return;}
		if (parameter.equalsIgnoreCase("system")) {systemNumbers=true; return;}
		if (parameter.equalsIgnoreCase("fraction") || parameter.equalsIgnoreCase("bruch")) {percent=false; time=false; return;}
		if (parameter.equalsIgnoreCase("percent") || parameter.equalsIgnoreCase("prozent")) {percent=true; time=false; return;}
		if (parameter.equalsIgnoreCase("time") || parameter.equalsIgnoreCase("zeit")) {time=true; return;}
		if (parameter.equalsIgnoreCase("number") || parameter.equalsIgnoreCase("zahl")) {time=false; return;}

		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+parameter+")");
	}

	/**
	 * Stellt ein, welches Trennzeichen zwischen den Werten bei der Ausgabe von Arrays verwendet werden soll
	 * @param obj	Bezeichner für das Trennzeichen
	 */
	public void setSeparator(final Object obj) {
		if (!(obj instanceof String)) return;
		final String parameter=(String)obj;

		if (parameter.equalsIgnoreCase("semikolon") || parameter.equalsIgnoreCase("semicolon")) {separator=';'; return;}
		if (parameter.equalsIgnoreCase("line") || parameter.equalsIgnoreCase("lines") || parameter.equalsIgnoreCase("newline") || parameter.equalsIgnoreCase("zeilen") || parameter.equalsIgnoreCase("zeile")) {separator='\n'; return;}
		if (parameter.equalsIgnoreCase("tab") || parameter.equalsIgnoreCase("tabs") || parameter.equalsIgnoreCase("tabulator")) {separator='\t'; return;}

		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+parameter+")");
	}

	/**
	 * Gibt einen String oder eine Zahl aus (ohne folgenden Zeilenumbruch)
	 * @param obj	Auszugebendes Objekt
	 */
	public void print(final Object obj) {
		if (obj instanceof int[]) {
			final int[] arr=(int[])obj;
			for (int i=0;i<arr.length;i++) {
				printDouble(arr[i]);
				if (i<arr.length-1) {addOutputMain(String.valueOf(separator)); isOutputPlainDouble=false;}
			}
		}

		if (obj instanceof double[]) {
			final double[] arr=(double[])obj;
			for (int i=0;i<arr.length;i++) {
				printDouble(arr[i]);
				if (i<arr.length-1) {addOutputMain(String.valueOf(separator)); isOutputPlainDouble=false;}
			}
		}

		if (obj instanceof String[]) {
			final String[] arr=(String[])obj;
			for (int i=0;i<arr.length;i++) {
				addOutputMain(arr[i]);
				if (i<arr.length-1) {addOutputMain(String.valueOf(separator)); isOutputPlainDouble=false;}
			}
		}

		if (obj instanceof String) {
			addOutputMain((String)obj);
			isOutputPlainDouble=false;
			return;
		}

		if (obj instanceof Number) {
			printDouble(((Number)obj).doubleValue());
			return;
		}

		if (obj instanceof Boolean) {
			addOutputMain(((Boolean)obj).toString());
			isOutputPlainDouble=false;
			return;
		}
	}

	/**
	 * Gibt einen String oder eine Zahl mit folgendem Zeilenumbruch aus
	 * @param obj	Auszugebendes Objekt
	 */
	public void println(final Object obj) {
		print(obj);
		addOutputMain("\n");
	}

	/**
	 * Gibt einen Zeilenumbruch aus
	 */
	public void newLine() {
		addOutputMain("\n");
	}

	/**
	 * Gibt einen Tabulator aus
	 */
	public void tab() {
		addOutputMain("\t");
	}

	/**
	 * Wird an {@link #print(Object)} eine Zahl übergeben,
	 * so wird die Ausgabe an diese Methode weitergereicht,
	 * um das unnötige Boxen der Zahl zu vermeiden und um
	 * diese später direkt über {@link #getOutputDouble()}
	 * bereitstellen zu können.
	 * @param value	Auszugebende Zahl
	 * @see #getOutputDouble()
	 * @see #print(Object)
	 */
	private void printDouble(double value) {
		if (time) {
			if (systemNumbers) {
				addOutputMain(TimeTools.formatExactSystemTime(value));
			} else {
				addOutputMain(TimeTools.formatExactTime(value));
			}
		} else {
			if (outputTemp==null) outputTemp=new StringBuilder();

			if (systemNumbers) {
				if (percent) {
					addOutputMain(NumberTools.formatSystemNumber(value*100,outputTemp)+"%");
				} else {
					addOutputMain(NumberTools.formatSystemNumber(value,outputTemp));
				}
			} else {
				if (percent) {
					addOutputMain(NumberTools.formatNumberMax(value*100,outputTemp)+"%");
				} else {
					addOutputMain(NumberTools.formatNumberMax(value,outputTemp));
				}
			}
		}

		outputPlainDouble=value;
	}

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden Dateiausgaben nicht mehr ausgeführt.)
	 */
	public void cancel() {
		canceled=true;
	}

	/**
	 * Stellt ein, in welche Datei die Ausgabe erfolgen soll
	 * @param file	Ausgabedatei
	 */
	public void setFile(final Object file) {
		if (!outputToFile || canceled) return;
		if (!(file instanceof String)) return;
		outputFile=new File((String)file);

	}

	/**
	 * Gibt eine Meldung je nach Konfiguration in eine Datei
	 * ({@link #outputFile}) oder über {@link #addOutput(String)} aus.
	 * @param line	Auszugebende Meldung
	 */
	private void addOutputMain(final String line) {
		if (canceled) return;
		if (outputToFile) {
			if (outputFile==null) {
				addOutput(Language.tr("Statistics.Filter.NoOutputFileDefined")+"\n");
				return;
			}
			try {
				if (outputFile.exists()) {
					Files.write(Paths.get(outputFile.toURI()),line.getBytes(),StandardOpenOption.APPEND);
				} else {
					Files.write(Paths.get(outputFile.toURI()),line.getBytes(),StandardOpenOption.CREATE_NEW);
				}
			} catch (IOException e) {
				addOutput(String.format(Language.tr("Statistics.Filter.CouldNotSaveText"),outputFile.toString())+"\n");
			}
		} else {
			addOutput(line);
		}
	}

	/**
	 * Gibt an, ob es sich bei der Ausgabe in Summe einfach nur um eine Double-Zahl handelt.
	 * @return	Ist die Ausgabe nur eine Double-Zahl?
	 * @see #getOutputDouble()
	 */
	public boolean isOutputDouble() {
		return isOutputPlainDouble;
	}

	/**
	 * Wenn es sich bei der Ausgabe nur um eine Double-Zahl handelt, so liefert diese Methode die Zahl zurück.
	 * @return	Ausgabe als Double-Zahl
	 * @see #isOutputDouble()
	 */
	public double getOutputDouble() {
		return outputPlainDouble;
	}
}
