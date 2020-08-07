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
 * Abstrakte Basisklasse für Reportgenerator-Kommandozeilenbefehle.
 * Diese Klasse stellt gemeinsame Methoden bereit, die alle Generatorbefehle benötigen.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see BaseCommandLineSystem
 */
public abstract class AbstractReportCommand extends AbstractCommand {
	private enum ExportMode {
		MODE_REPORT_INLINE(new String[] {"Inline"}),
		MODE_REPORT_FILES(new String[] {"Einzeldateien","SingleFiles"}),
		MODE_REPORT_APP(new String[] {"HTMLApp"}),
		MODE_DOCX(new String[] {"Text"}),
		MODE_PDF(new String[] {"PDF"}),
		MODE_LATEX(new String[] {"LaTeX"}),
		MODE_LIST(new String[] {"Liste","List"}),
		MODE_SINGLE_DOCUMENT(null);

		public final String[] names;

		ExportMode(final String[] names) {
			this.names=names;
		}
	}

	private File input;
	private File output;
	private ExportMode mode;
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
	 * Lädt die Statistikdaten und stellt diese über ein Klasse, die {@link AbstractReportCommandConnect} implementiert für die Auswertung zur Verfügung.
	 * @param input	Zu ladende Datei
	 * @return	Im Fehlerfall eine Fehlermeldung als String, sonst ein Objekt, das {@link AbstractReportCommandConnect} implementiert
	 */
	protected abstract Object getReportCommandConnect(File input);

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
