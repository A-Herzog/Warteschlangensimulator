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
package ui.commandline;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import net.socket.SocketServerCalc;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;

/**
 * Startet einen Socket-basierten Simulationsserver.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandServerSocket extends AbstractCommand {
	/** Serverport */
	private int serverPort;
	/** Signalisiert dass der Server beendet werden soll. */
	private boolean isQuit;
	/** Abbruchzeit in Sekunden (Werte &le;0 bedeuten, dass keine Abbruchzeit gesetzt ist) */
	private double timeout=-1;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandServerSocket(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ServerSocket.Name"));
		for (String s: Language.trOther("CommandLine.ServerSocket.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ServerSocket.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ServerSocket.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		final String s=parameterCountCheck(1,2,additionalArguments); if (s!=null) return s;

		Long L;

		L=NumberTools.getPositiveLong(additionalArguments[0]);
		if (L==null) return String.format(Language.tr("CommandLine.ServerSocket.InvalidPortNumber"),additionalArguments[0]);

		if (additionalArguments.length>1) {
			final Double D=NumberTools.getDouble(additionalArguments[1]);
			if (D==null) return String.format(Language.tr("CommandLine.Error.InvalidTimeout"),additionalArguments[3]);
			timeout=D.doubleValue();
		}

		serverPort=L.intValue();
		return null;
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
		final SocketServerCalc server=new SocketServerCalc(timeout);
		if (!server.start(serverPort)) {
			if (out!=null) {
				style.setErrorStyle();
				out.println(String.format(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.ServerSocket.StartError"),serverPort));
				style.setNormalStyle();
			}
			return;
		}
		if (out!=null) out.println(String.format(Language.tr("CommandLine.ServerSocket.Started"),serverPort));
		final CloseRequestSignal quitSignal=new CloseRequestSignal(true,in);
		while (!isQuit && !quitSignal.isQuit()) try {Thread.sleep(50);} catch (InterruptedException e) {}
		server.stop();
		if (out!=null) out.println(Language.tr("CommandLine.ServerSocket.Stopped"));
	}

	@Override
	public final void setQuit() {
		isQuit=true;
	}

}
