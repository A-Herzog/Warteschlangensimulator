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
import java.util.ArrayList;
import java.util.List;

/**
 * Abstrakte Basisklasse für Reportgenerator-Kommandozeilenbefehle.
 * Diese Klasse stellt gemeinsame Methoden bereit, die alle Generatorbefehle benötigen.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see BaseCommandLineSystem
 */
public abstract class AbstractReportCommand extends AbstractCommand {
	private static final int MODE_REPORT_INLINE=0;
	private static final int MODE_REPORT_FILES=1;
	private static final int MODE_DOCX=2;
	private static final int MODE_PDF=3;
	private static final int MODE_LIST=4;
	private static final int MODE_SINGLE_DOCUMENT=5;

	private File input;
	private File output;
	private int mode=-1;
	private String listEntry;

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(3,additionalArguments); if (s!=null) return s;

		List<List<String>> keys=new ArrayList<List<String>>();
		List<String> list;

		list=new ArrayList<>();
		list.add("Inline");
		keys.add(list);

		list=new ArrayList<>();
		list.add("Einzeldateien");
		keys.add(list);

		list=new ArrayList<>();
		list.add("Text");
		keys.add(list);

		list=new ArrayList<>();
		list.add("PDF");
		keys.add(list);

		list=new ArrayList<>();
		list.add("Liste");
		keys.add(list);

		mode=-1;
		for (int i=0;i<keys.size();i++) {
			List<String> l=keys.get(i);
			for (int j=0;j<l.size();j++) if (additionalArguments[0].equalsIgnoreCase(l.get(j))) {mode=i; break;}
			if (mode>=0) break;
		}
		if (mode<0) {mode=MODE_SINGLE_DOCUMENT; listEntry=additionalArguments[0];}

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
		case MODE_REPORT_INLINE: return reportGeneratorConnect.runReportGeneratorHTML(output,true,false);
		case MODE_REPORT_FILES:  return reportGeneratorConnect.runReportGeneratorHTML(output,false,false);
		case MODE_DOCX: return reportGeneratorConnect.runReportGeneratorDOCX(output,false);
		case MODE_PDF: return reportGeneratorConnect.runReportGeneratorPDF(output,false);
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
