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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;

/**
 * Implementierungsklasse für das Interface {@link OutputInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class OutputImpl implements OutputInterface {
	/** Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen */
	private final Consumer<String> output;
	/** Wird hier <code>true</code> übergeben, so erfolgt die Ausgabe der eigentlichen Meldungen in eine Datei */
	private final boolean outputToFile;
	private File outputFile;
	private boolean canceled=false;
	private boolean time;
	private boolean percent;
	private boolean systemNumbers;
	private char separator=';';

	private boolean isOutputPlainDouble=true;
	private double outputPlainDouble;

	private StringBuilder outputTemp;

	/**
	 * Konstruktor der Klasse
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 * @param outputToFile	Wird hier <code>true</code> übergeben, so erfolgt die Ausgabe der eigentlichen Meldungen in eine Datei
	 * @see OutputImpl#setFile(Object)
	 */
	public OutputImpl(final Consumer<String> output, final boolean outputToFile) {
		this.output=output;
		this.outputToFile=outputToFile;
	}

	private void addOutput(final String line) {
		if (output!=null) output.accept(line);
	}

	@Override
	public void setFormat(final String format) {
		if (format==null) return;
		if (format.equalsIgnoreCase("lokal") || format.equalsIgnoreCase("local")) {systemNumbers=false; return;}
		if (format.equalsIgnoreCase("system")) {systemNumbers=true; return;}
		if (format.equalsIgnoreCase("fraction") || format.equalsIgnoreCase("bruch")) {percent=false; time=false; return;}
		if (format.equalsIgnoreCase("percent") || format.equalsIgnoreCase("prozent")) {percent=true; time=false; return;}
		if (format.equalsIgnoreCase("time") || format.equalsIgnoreCase("zeit")) {time=true; return;}
		if (format.equalsIgnoreCase("number") || format.equalsIgnoreCase("zahl")) {time=false; return;}
		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+format+")");
	}

	@Override
	public void setSeparator(final String separator) {
		if (separator==null) return;
		if (separator.equalsIgnoreCase("semikolon") || separator.equalsIgnoreCase("semicolon")) {this.separator=';'; return;}
		if (separator.equalsIgnoreCase("line") || separator.equalsIgnoreCase("lines") || separator.equalsIgnoreCase("newline") || separator.equalsIgnoreCase("zeilen") || separator.equalsIgnoreCase("zeile")) {this.separator='\n'; return;}
		if (separator.equalsIgnoreCase("tab") || separator.equalsIgnoreCase("tabs") || separator.equalsIgnoreCase("tabulator")) {this.separator='\t'; return;}
		addOutput(Language.tr("Statistics.Filter.InvalidParameters")+" ("+separator+")");
	}

	@Override
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

		if (obj instanceof Double) {
			printDouble((Double)obj);
			return;
		}

		if (obj instanceof Integer) {
			printDouble((Integer)obj);
			return;
		}

		if (obj instanceof Boolean) {
			addOutputMain(((Boolean)obj).toString());
			isOutputPlainDouble=false;
			return;
		}
	}

	@Override
	public void println(final Object obj) {
		print(obj);
		addOutputMain("\n");
	}

	@Override
	public void newLine() {
		addOutputMain("\n");
	}

	@Override
	public void tab() {
		addOutputMain("\t");
	}

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

	@Override
	public void cancel() {
		canceled=true;
	}

	/**
	 * Stellt ein, in welche Datei die Ausgabe erfolgen soll
	 * @param file	Ausgabedatei
	 */
	@Override
	public void setFile(final Object file) {
		if (!outputToFile || canceled) return;
		if (file instanceof String) {
			outputFile=new File((String)file);
			return;
		}
		if (file instanceof File) {
			outputFile=(File)file;
			return;
		}
	}

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
