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
import net.mqtt.MQTTBrokerURL;
import net.mqtt.MQTTSimClient;
import simulator.editmodel.EditModel;
import systemtools.commandline.AbstractCommand;
import tools.SetupData;
import xml.XMLTools;

/**
 * Startet einen Simulations-MQTT-Server für ein festgelegtes Modell.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandServerMQTTFixed extends AbstractCommand {
	/** Verbindungsdaten zum MQTT-Broker */
	private MQTTBrokerURL broker;
	/** Zu verwendende Modelldatei */
	private File modelFile;
	/** Signalisiert dass der Server beendet werden soll. */
	private boolean isQuit;
	/** Anfrage-Thema auf das der Simulator antworten soll */
	private String topic;
	/** Informations-Thema über das der Simulator Statusinformationen zur Arbeitslast ausgibt (kann <code>null</code> oder leer sein) */
	private String topicInfo;
	/** Nutzername, den der Client angeben muss (kann <code>null</code> sein, wenn keine Authentifizierung stattfinden soll) */
	private String authName;
	/** Passwort, dass der Client angeben muss (kann <code>null</code> sein, wenn keine Authentifizierung stattfinden soll) */
	private String authPassword;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ServerMQTTFixed.Name"));
		for (String s: Language.trOther("CommandLine.ServerMQTTFixed.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ServerMQTTFixed.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ServerMQTTFixed.Description.Long").split("\n");
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
		String s=parameterCountCheck(3,5,additionalArguments); if (s!=null) return s;

		broker=MQTTBrokerURL.parseString(additionalArguments[0],SetupData.getSetup().mqttVerifyCertificates);
		if (broker==null) return String.format(Language.tr("CommandLine.Server.InvalidBroker"),additionalArguments[0]);
		topic=additionalArguments[1].trim();

		modelFile=new File(additionalArguments[2]);
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile);
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidModelFile"),modelFile);

		if (additionalArguments.length==4) {
			final String arg=additionalArguments[3].trim();
			final String[] parts=arg.split(":");
			if (parts.length==2) {
				authName=parts[0];
				authPassword=parts[1];
			} else {
				topicInfo=arg;
			}
		}

		if (additionalArguments.length==5) {
			topicInfo=additionalArguments[3].trim();

			final String[] parts=additionalArguments[4].split(":");
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
		final MQTTSimClient server=new MQTTSimClient();

		/* Server starten */
		error=server.start(broker,null,topic,topicInfo,model,authName,authPassword);
		if (error!=null) {
			if (out!=null) out.println(error);
			return;
		}
		if (out!=null) {
			out.println(Language.tr("CommandLine.ServerMQTT.Started"));
		}

		/* Auf Ende warten */
		final CloseRequestSignal quitSignal=new CloseRequestSignal(true,in);
		while (!isQuit && !quitSignal.isQuit()) try {Thread.sleep(50);} catch (InterruptedException e) {}
		server.stop();
		if (out!=null) out.println(Language.tr("CommandLine.ServerMQTT.Stopped"));
	}

	@Override
	public final void setQuit() {
		isQuit=true;
	}
}