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
	private static final int BUFFER_SIZE=2^18;

	/** Datei, in die die Ausgabe erfolgen soll */
	private final File outputFile;

	private static final int MODE_TEXT=0;
	private static final int MODE_CSV=1;
	private static final int MODE_TABLE=2;

	private int mode;
	private boolean outputErrors;

	private FileWriter outputFileWriter;
	private StringBuilder outputBuffer;
	private Table table;

	/**
	 * Konstruktor der Klasse <code>RunDataOutputWriter</code>
	 * @param outputFile	Datei, in die die Ausgabe erfolgen soll
	 */
	public RunDataOutputWriter(final File outputFile) {
		this.outputFile=outputFile;
		outputErrors=false;
		if (outputFile==null) return;

		final String nameLower=outputFile.toString().toLowerCase();
		mode=MODE_TEXT;
		if (nameLower.endsWith(".csv")) mode=MODE_CSV;

		if (Table.SaveMode.SAVEMODE_XLSX.fileNameMatch(nameLower)) mode=MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_XLS.fileNameMatch(nameLower)) mode=MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_DIF.fileNameMatch(nameLower)) mode=MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_HTML.fileNameMatch(nameLower)) mode=MODE_TABLE;
		if (Table.SaveMode.SAVEMODE_DOCX.fileNameMatch(nameLower)) mode=MODE_TABLE;

		if (mode==MODE_TEXT || mode==MODE_CSV) {
			try {outputFileWriter=new FileWriter(outputFile,true);} catch (IOException e) {
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

	private String toCSV(String[] outputTableLine) {
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
