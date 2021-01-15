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
import net.mqtt.MQTTBrokerURL;
import net.mqtt.MQTTSimClient;
import systemtools.commandline.AbstractCommand;

/**
 * Startet einen Simulations-MQTT-Server.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public class CommandServerMQTT extends AbstractCommand {
	/** Verbindungsdaten zum MQTT-Broker */
	private MQTTBrokerURL broker;
	/** Signalisiert dass der Server beendet werden soll. */
	private boolean isQuit;
	/** Anfrage-Thema auf das der Simulator antworten soll */
	private String topic;
	/** Nutzername, den der Client angeben muss (kann <code>null</code> sein, wenn keine Authentifizierung stattfinden soll) */
	private String authName;
	/** Passwort, dass der Client angeben muss (kann <code>null</code> sein, wenn keine Authentifizierung stattfinden soll) */
	private String authPassword;

	@Override
	public String[] getKeys() {
		List<String> list=new ArrayList<>();
		list.add(Language.tr("CommandLine.ServerMQTT.Name"));
		for (String s: Language.trOther("CommandLine.ServerMQTT.Name")) if (!list.contains(s)) list.add(s);
		return list.toArray(new String[0]);
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.ServerMQTT.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.ServerMQTT.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(2,3,additionalArguments); if (s!=null) return s;

		broker=MQTTBrokerURL.parseString(additionalArguments[0]);
		if (broker==null) return String.format(Language.tr("CommandLine.Server.InvalidBroker"),additionalArguments[0]);
		topic=additionalArguments[1].trim();

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
		/* Server-Objekt */
		final MQTTSimClient server=new MQTTSimClient();

		/* Server starten */
		final String error=server.start(broker,null,topic,authName,authPassword);
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