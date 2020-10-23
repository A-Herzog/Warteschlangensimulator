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
package ui.commandline;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import language.Language;
import mathtools.NumberTools;
import net.calc.SimulationServer;
import systemtools.commandline.AbstractCommand;

/**
 * Startet einen Simulationsserver mit unbegrenzter Anzahl an gleichzeitig zulässigen Simulationsthreads.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandServer extends AbstractCommand {
	/** Serverport */
	private int serverPort;
	/** Serverpasswort */
	private String serverPassword;
	/** Signalisiert dass der Server beendet werden soll. */
	private boolean isQuit;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.Server.Name"));
		for (String s: Language.trOther("CommandLine.Server.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Server.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Server.Description.Long").split("\n");
	}

	/**
	 * Prüft die Parameter für den Server-Betrieb
	 * @param additionalArguments	Zusätzlich übergebene Parameter (siehe {@link AbstractCommand#prepare(String[], InputStream, PrintStream)})
	 * @param in	Tastatur-Input-Stream (über den wenn ungleich <code>null</code> und notwendig die Port-Nummer abgefragt wird)
	 * @param out	Optionaler Ausgabe-Stream über den, wenn nötig, der Infotext vor der Port-Nummern-Abfrage ausgegeben wird
	 * @return	Liefert im Erfolgsfall <code>null</code>, sonst eine Fehlermeldung (die Fehlermeldung wird nicht an <code>out</code> ausgegeben)
	 */
	protected final String prepareServerParameter(final String[] additionalArguments, final InputStream in, final PrintStream out) {
		final Integer I;
		if (additionalArguments.length>=1) {
			String portData=additionalArguments[0];
			I=NumberTools.getNotNegativeInteger(portData);
			if (I==null) return String.format(Language.tr("CommandLine.Server.InvalidPort"),portData);
		} else {
			if (out!=System.out || in==null) {
				return String.format(Language.tr("CommandLine.Server.InvalidPort"),"");
			}
			out.println(Language.tr("CommandLine.Server.EnterPortNumber"));
			@SuppressWarnings("resource") /* Wir dürfen scanner.close(); nicht aufrufen, sonst später nicht mehr auf Eingaben (zum Beenden des Servers) reagiert werden. */
			final Scanner scanner=new Scanner(in);
			I=NumberTools.getNotNegativeInteger(scanner.next());
			if (I==null || I==0) return Language.tr("CommandLine.Server.InvalidPort.Short");
		}
		serverPort=I;

		if (additionalArguments.length>=2) {
			serverPassword=additionalArguments[1];
		}

		return null;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(0,2,additionalArguments); if (s!=null) return s;
		return prepareServerParameter(additionalArguments,in,out);
	}

	/**
	 * Gibt an, ob nur dann Anfragen angenommen werden sollen, wenn unbelegte CPU-Kerne verfügbar sind
	 * @return	Wird <code>true</code> zurückgegeben, so werden Anfragen abgewiesen, wenn bereits alle CPU-Kerne arbeiten.
	 */
	protected boolean isThreadLimited() {
		return false;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final SimulationServer server=new SimulationServer(s->out.println(s),serverPort,serverPassword,isThreadLimited());
		server.start();
		if (out!=null) out.println(String.format(Language.tr("CommandLine.Server.Started"),serverPort));
		final CloseRequestSignal quitSignal=new CloseRequestSignal(true,in);
		while (!isQuit && !quitSignal.isQuit()) try {Thread.sleep(50);} catch (InterruptedException e) {}
		server.stop();
		if (out!=null) out.println(Language.tr("CommandLine.Server.Stopped"));
	}

	@Override
	public final void setQuit() {
		isQuit=true;
	}
}