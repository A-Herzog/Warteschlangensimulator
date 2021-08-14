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
package simulator.runmodel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import mathtools.Table;

/**
 * Gepufferte Ausgabe von Textdaten in einer Datei
 * @author Alexander Herzog
 */
public class RunDataOutputWriter {
	/**
	 * Gibt an bei welchem Füllstand Einträge
	 * aus {@link #outputBuffer} in
	 * {@link #outputFileWriter} ausgegeben werden sollen.
	 * @see #output(String)
	 */
	private static final int BUFFER_SIZE=2^18;

	/** Datei, in die die Ausgabe erfolgen soll */
	private final File outputFile;

	/** Soll eine evtl. bestehende Ausgabedatei überschrieben werden (<code>true</code>) oder sollen die neuen Daten angehängt werden (<code>false</code>)? */
	private final boolean outputFileOverwrite;

	/** Ausgabemodus */
	private enum Mode {
		/** Ausgabe als Text */
		MODE_TEXT,
		/** Ausgabe als CSV-Tabelle */
		MODE_CSV,
		/** Ausgabe über ein Tabellenobjekt */
		MODE_TABLE
	}

	/**
	 * Ausgabemodus
	 */
	private Mode mode;

	/**
	 * Ist <code>true</code>, wenn mindestens eine Zeile nicht ausgegeben werden konnte.
	 * @see #hasOutputErrors()
	 */
	private boolean outputErrors;

	/**
	 * File-Writer-Objekt für die Ausgabe in den Modi {@link Mode#MODE_TEXT} und {@link Mode#MODE_CSV}
	 */
	private FileWriter outputFileWriter;

	/**
	 * Puffer, der die Ergebnisse zwischenspeichert bis diese in {@link #outputFileWriter} übertragen werden
	 * @see #outputFileWriter
	 * @see #BUFFER_SIZE
	 */
	private StringBuilder outputBuffer;

	/**
	 * Tabelle, die die Ergebnisse aufnimmt, wenn die Ausgabe nicht in den Text-Modi
	 * {@link Mode#MODE_TEXT} oder {@link Mode#MODE_CSV} erfolgt.
	 */
	private Table table;

	/**
	 * Konstruktor der Klasse <code>RunDataOutputWriter</code>
	 * @param outputFile	Datei, in die die Ausgabe erfolgen soll
	 * @param overwrite	Soll eine evtl. bestehende Ausgabedatei überschrieben werden (<code>true</code>) oder sollen die neuen Daten angehängt werden (<code>false</code>)?
	 */
	public RunDataOutputWriter(final File outputFile, final boolean overwrite) {
		this.outputFile=outputFile;
		this.outputFileOverwrite=overwrite;
		outputErrors=false;
		if (outputFile==null) return;

		final String nameLower=outputFile.toString().toLowerCase();
		mode=Mode.MODE_TEXT;
		if (nameLower.endsWith(".csv")) mode=Mode.MODE_CSV;

		if (Table.SaveMode.SAVEMODE_XLSX.fileNameMatch(nameLower)) mode=Mode.MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_XLS.fileNameMatch(nameLower)) mode=Mode.MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_DIF.fileNameMatch(nameLower)) mode=Mode.MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_HTML.fileNameMatch(nameLower)) mode=Mode.MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_DOCX.fileNameMatch(nameLower)) mode=Mode.MODE_TABLE;

		if (mode==Mode.MODE_TEXT || mode==Mode.MODE_CSV) {
			try {outputFileWriter=new FileWriter(outputFile,!outputFileOverwrite);} catch (IOException e) {
				outputFileWriter=null;
				return;
			}
			outputBuffer=new StringBuilder(BUFFER_SIZE);
		} else {
			table=new Table();
		}
	}

	/**
	 * Ausgabe einer Textzeile im Textdatei-Modus
	 * @param outputText	Text, der ausgegeben werden soll
	 */
	public void output(final String outputText) {
		if (outputFileWriter==null) return;

		outputBuffer.append(outputText);

		if (outputBuffer.length()>9*BUFFER_SIZE/10) {
			try {outputFileWriter.write(outputBuffer.toString());} catch (IOException e) {outputErrors=true;}
			outputBuffer=new StringBuilder(BUFFER_SIZE);
		}
	}

	/**
	 * Gibt an, ob es während der Ausgabe Fehler gab.
	 * @return	Gibt <code>true</code> zurück, wenn mindestens eine Zeile nicht ausgegeben werden konnte.
	 */
	public boolean hasOutputErrors() {
		return outputErrors;
	}

	/**
	 * Wandelt eine Tabellenzeile in CSV-Code um
	 * @param outputTableLine	Tabellenzeile in Form einzelner Zellen
	 * @return	CSV-codierte Tabellenzeile
	 */
	private String toCSV(final String[] outputTableLine) {
		final StringBuilder sb=new StringBuilder();

		for (int i=0;i<outputTableLine.length;i++) {
			String cell=outputTableLine[i];
			cell=cell.replace("\"","\"\"");
			if (cell.indexOf('"')!=-1 || cell.indexOf(';')!=-1) cell='"'+cell+'"';
			sb.append(cell);
			if (i<outputTableLine.length-1) sb.append(';');
		}

		sb.append('\n');

		return sb.toString();
	}

	/**
	 * Ausgabe einer Tabellenzeile im Tabellendatei-Modus
	 * @param outputTableLine	Zellen der Zeile, die ausgegeben werden soll
	 */
	public void output(final String[] outputTableLine) {
		/* csv */
		if (outputFileWriter!=null) output(toCSV(outputTableLine));

		/* Table */
		if (table!=null) table.addLine(outputTableLine);
	}

	/**
	 * Leert den Puffer und schließt die Datei
	 */
	public void close() {
		/* Texte */
		if (outputFileWriter!=null) {
			try {
				if (outputBuffer.length()>0) outputFileWriter.write(outputBuffer.toString());
				outputFileWriter.flush();
				outputFileWriter.close();
			} catch (IOException e) {}
			outputFileWriter=null;
		}

		/* Tabellen */
		if (table!=null) {
			table.save(outputFile);
			table=null;
		}
	}
}
