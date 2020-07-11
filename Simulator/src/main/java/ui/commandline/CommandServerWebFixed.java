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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import net.webcalc.CalcWebServer;
import simulator.editmodel.EditModel;
import systemtools.commandline.AbstractCommand;
import xml.XMLTools;

/**
 * Startet einen Simulations-Web-Server für ein festgelegtes Modell.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandServerWebFixed extends AbstractCommand {
	private int serverPort;
	private File modelFile;
	private boolean isQuit;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<String>();
		list.add(Language.tr("CommandLine.ServerWebFixedModel.Name"));
		for (String s: Language.trOther("CommandLine.ServerWebFixedModel.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ServerWebFixedModel.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ServerWebFixedModel.Description.Long").split("\n");
	}

	private final boolean isModelFile(final File file) {
		final XMLTools xml=new XMLTools(file);
		final Element root=xml.load();
		if (root==null) return false;

		for (String test: new EditModel().getRootNodeNames()) if (root.getNodeName().equalsIgnoreCase(test)) return true;

		return false;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,additionalArguments); if (s!=null) return s;

		final String portData=additionalArguments[0];
		final Integer I=NumberTools.getNotNegativeInteger(portData);
		if (I==null) return String.format(Language.tr("CommandLine.Server.InvalidPort"),portData);
		serverPort=I;

		modelFile=new File(additionalArguments[1]);
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile);
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidModelFile"),modelFile);

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		final EditModel model=new EditModel();
		final String error=model.loadFromFile(modelFile);
		if (error!=null) {
			if (out!=null) out.println(error);
			return;
		}

		final CalcWebServer server=new CalcWebServer(model);
		server.start(serverPort);
		final CloseRequestSignal quitSignal=new CloseRequestSignal(true,in);
		while (!isQuit && !quitSignal.isQuit()) try {Thread.sleep(50);} catch (InterruptedException e) {}
		server.stop();
	}

	@Override
	public final void setQuit() {
		isQuit=true;
	}
}