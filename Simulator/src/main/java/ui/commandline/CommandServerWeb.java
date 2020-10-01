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

import language.Language;
import mathtools.NumberTools;
import net.webcalc.CalcWebServer;
import systemtools.commandline.AbstractCommand;

/**
 * Startet einen Simulations-Web-Server.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandServerWeb extends AbstractCommand {
	private int serverPort;
	private boolean isQuit;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ServerWeb.Name"));
		for (String s: Language.trOther("CommandLine.ServerWeb.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ServerWeb.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ServerWeb.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(1,additionalArguments); if (s!=null) return s;

		final String portData=additionalArguments[0];
		final Integer I=NumberTools.getNotNegativeInteger(portData);
		if (I==null) return String.format(Language.tr("CommandLine.Server.InvalidPort"),portData);
		serverPort=I;
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final CalcWebServer server=new CalcWebServer();
		server.start(serverPort);
		if (out!=null) out.println(String.format(Language.tr("CommandLine.ServerWeb.Started"),serverPort));
		final CloseRequestSignal quitSignal=new CloseRequestSignal(true,in);
		while (!isQuit && !quitSignal.isQuit()) try {Thread.sleep(50);} catch (InterruptedException e) {}
		server.stop();
		if (out!=null) out.println(Language.tr("CommandLine.ServerWeb.Stopped"));
	}

	@Override
	public final void setQuit() {
		isQuit=true;
	}
}