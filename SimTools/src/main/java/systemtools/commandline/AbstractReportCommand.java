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
package systemtools.commandline;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Abstrakte Basisklasse f�r Reportgenerator-Kommandozeilenbefehle.
 * Diese Klasse stellt gemeinsame Methoden bereit, die alle Generatorbefehle ben�tigen.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see BaseCommandLineSystem
 */
public abstract class AbstractReportCommand extends AbstractCommand {
	/**
	 * Dateiformat f�r die Ausgabe
	 */
	private enum ExportMode {
		/** Dateiformat: html, Bilder inline */
		MODE_REPORT_INLINE(new String[] {"Inline"}),
		/** Dateiformat: html, Bilder als externe Dateien */
		MODE_REPORT_FILES(new String[] {"Einzeldateien","SingleFiles"}),
		/** Dateiformat: html als interaktive Web-App */
		MODE_REPORT_APP(new String[] {"HTMLApp"}),
		/** Dateiformat: docx */
		MODE_DOCX(new String[] {"Text"}),
		/** Dateiformat: pdf */
		MODE_PDF(new String[] {"PDF"}),
		/** Dateiformat: tex */
		MODE_LATEX(new String[] {"LaTeX"}),
		/** Ausgabe einer Liste (als Text) der verf�gbaren Einzeldokumente */
		MODE_LIST(new String[] {"Liste","List"}),
		/** Einzeldokument f�r eine Statistikseite */
		MODE_SINGLE_DOCUMENT(null);

		/**
		 * Liste mit m�glichen Namen, �ber die das entsprechende Dateiformat
		 * von der Kommandozeile aus angesprochen werden kann.
		 */
		private final String[] names;

		/**
		 * Konstruktor der Klasse
		 * @param names	Liste mit m�glichen Namen, �ber die das entsprechende Dateiformat von der Kommandozeile aus angesprochen werden kann.
		 */
		ExportMode(final String[] names) {
			this.names=names;
		}
	}

	/**
	 * Statistikdatei, der die Daten entnommen werden sollen
	 * @see #prepare(String[], InputStream, PrintStream)
	 */
	private File input;

	/**
	 * Ausgabedatei
	 * @see #prepare(String[], InputStream, PrintStream)
	 */
	private File output;

	/**
	 * Ausgabedateiformat
	 * @see AbstractReportCommand.ExportMode
	 */
	private ExportMode mode;

	/**
	 * Gew�hltes Einzeldokument im Modus {@link AbstractReportCommand.ExportMode#MODE_SINGLE_DOCUMENT}
	 * @see #prepare(String[], InputStream, PrintStream)
	 */
	private String listEntry;

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		mode=null;
		for (ExportMode test: ExportMode.values()) {
			if (test.names==null) continue;
			for (String name: test.names) {
				if (additionalArguments[0].equalsIgnoreCase(name)) {mode=test; break;}
			}
			if (mode!=null) break;
		}
		if (mode==null) {mode=ExportMode.MODE_SINGLE_DOCUMENT; listEntry=additionalArguments[0];}

		input=new File(additionalArguments[1]);
		if (!input.exists()) return  String.format(BaseCommandLineSystem.commandReportErrorInputDoesNotExists,input.toString());
		output=new File(additionalArguments[2]);
		if (output.exists()) return String.format(BaseCommandLineSystem.commandReportErrorOutputExists,output.toString());

		return null;
	}

	@Override
	public String[] getLongDescription() {
		return BaseCommandLineSystem.commandReportHelp.split("\\n");
	}

	/**
	 * L�dt die Statistikdaten und stellt diese �ber ein Klasse, die {@link AbstractReportCommandConnect} implementiert f�r die Auswertung zur Verf�gung.
	 * @param input	Zu ladende Datei
	 * @return	Im Fehlerfall eine Fehlermeldung als String, sonst ein Objekt, das {@link AbstractReportCommandConnect} implementiert
	 */
	protected abstract Object getReportCommandConnect(File input);

	/**
	 * F�hrt die eigentliche Report-Ausgabe durch
	 * @param reportGeneratorConnect	Verbindung zu der Klasse, die die Dateien erzeugen kann
	 * @return	Gibt an, ob die Ausgabe erfolgreich war
	 */
	private boolean process(AbstractReportCommandConnect reportGeneratorConnect) {
		switch (mode) {
		case MODE_REPORT_INLINE: return reportGeneratorConnect.runReportGeneratorHTML(output,true,true);
		case MODE_REPORT_FILES:  return reportGeneratorConnect.runReportGeneratorHTML(output,false,true);
		case MODE_REPORT_APP:  return reportGeneratorConnect.runReportGeneratorHTMLApp(output,true);
		case MODE_DOCX: return reportGeneratorConnect.runReportGeneratorDOCX(output,true);
		case MODE_PDF: return reportGeneratorConnect.runReportGeneratorPDF(output,true);
		case MODE_LATEX: return reportGeneratorConnect.runReportGeneratorLaTeX(output,true);
		case MODE_LIST: return reportGeneratorConnect.getReportList(output);
		case MODE_SINGLE_DOCUMENT: return reportGeneratorConnect.getReportListEntry(output,listEntry);
		}
		return true;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final Object obj=getReportCommandConnect(input);

		if (obj instanceof String) {
			out.println(BaseCommandLineSystem.errorBig+": "+((String)obj));
			return;
		}

		if (obj instanceof AbstractReportCommandConnect) {
			if (!process((AbstractReportCommandConnect)obj)) out.println(BaseCommandLineSystem.errorBig+": "+BaseCommandLineSystem.commandReportError);
			return;
		}

		out.println(BaseCommandLineSystem.commandReportDone);
	}
}
