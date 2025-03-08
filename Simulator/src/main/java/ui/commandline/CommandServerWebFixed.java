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
import systemtools.commandline.BaseCommandLineSystem;
import tools.SetupData;
import xml.XMLTools;

/**
 * Startet einen Simulations-Web-Server für ein festgelegtes Modell.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandServerWebFixed extends AbstractCommand {
	/** Serverport */
	private int serverPort;
	/** Zu verwendende Modelldatei */
	private File modelFile;
	/** Signalisiert dass der Server beendet werden soll. */
	private boolean isQuit;
	/** Nutzername, den der Client angeben muss (kann <code>null</code> sein, wenn keine Authentifizierung stattfinden soll) */
	private String authName;
	/** Passwort, dass der Client angeben muss (kann <code>null</code> sein, wenn keine Authentifizierung stattfinden soll) */
	private String authPassword;

	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandServerWebFixed(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ServerWebFixedModel.Name"));
		for (String s: Language.trOther("CommandLine.ServerWebFixedModel.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(String[]::new);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ServerWebFixedModel.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ServerWebFixedModel.Description.Long").split("\n");
	}

	/**
	 * Prüft, ob die übergebene Datei eine Modell Datei ist
	 * @param file	Zu prüfende Datei
	 * @return	Gibt <code>true</code> zurück, wenn es sich um eine Modell Datei handelt
	 */
	private final boolean isModelFile(final File file) {
		final XMLTools xml=new XMLTools(file);
		final Element root=xml.load();
		if (root==null) return false;

		for (String test: new EditModel().getRootNodeNames()) if (root.getNodeName().equalsIgnoreCase(test)) return true;

		return false;
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,3,additionalArguments); if (s!=null) return s;

		final String portData=additionalArguments[0];
		final Integer I=NumberTools.getNotNegativeInteger(portData);
		if (I==null) return String.format(Language.tr("CommandLine.Server.InvalidPort"),portData);
		serverPort=I;

		modelFile=new File(additionalArguments[1]);
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile);
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidModelFile"),modelFile);

		if (additionalArguments.length==3) {
			final String[] parts=additionalArguments[2].split(":");
			if (parts.length==2) {
				authName=parts[0];
				authPassword=parts[1];
			} else {
				return Language.tr("CommandLine.Server.AuthNamePasswordInvalid");
			}
		}

		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		/* Modell laden */
		final EditModel model=new EditModel();
		String error=model.loadFromFile(modelFile);
		if (error!=null) {
			if (out!=null) out.println(error);
			return;
		}

		/* Server-Objekt */
		final CalcWebServer server=new CalcWebServer(model);

		/* Zugangsdaten */
		if (authName!=null && !authName.isBlank() && authPassword!=null && !authPassword.isBlank()) {
			server.setAuthData(Language.tr("SimulationServer.AuthRequestInfo"),authName,authPassword);
		} else {
			server.setAuthData(null,null,null);
		}

		/* TLS */
		final SetupData setup=SetupData.getSetup();
		server.setTLSData(setup.serverTLSKeyStoreFile,setup.serverTLSKeyStorePassword);

		/* Server starten */
		error=server.start(serverPort);
		if (error!=null) {
			if (out!=null) {
				style.setErrorStyle();
				out.println(error);
				style.setNormalStyle();
			}
			return;
		}
		if (out!=null) out.println(String.format(Language.tr("CommandLine.ServerWebFixedModel.Started"),serverPort));

		/* Auf Ende warten */
		final CloseRequestSignal quitSignal=new CloseRequestSignal(true,in);
		while (!isQuit && !quitSignal.isQuit()) try {Thread.sleep(50);} catch (InterruptedException e) {}
		server.stop();
		if (out!=null) out.println(Language.tr("CommandLine.ServerWebFixedModel.Stopped"));
	}

	@Override
	public final void setQuit() {
		isQuit=true;
	}
}