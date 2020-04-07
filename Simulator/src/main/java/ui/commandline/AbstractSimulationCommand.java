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
import java.io.PrintStream;
import java.util.List;

import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelBase;
import simulator.statistics.Statistics;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;
import xml.XMLTools;

/**
 * Abstrakte Basisklasse für Simulations-Kommandozeilenbefehle.
 * Diese Klasse stellt zusätzliche geschützte Methoden bereit, die alle Simulationsbefehle benötigen.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public abstract class AbstractSimulationCommand extends AbstractCommand {
	private final Element loadXMLFile(final File file) {
		XMLTools xml=new XMLTools(file);
		return xml.load();
	}

	/**
	 * Prüft, ob die übergebene Datei eine Modell Datei ist
	 * @param file	Zu prüfende Datei
	 * @return	Gibt <code>true</code> zurück, wenn es sich um eine Modell Datei handelt
	 */
	protected final boolean isModelFile(final File file) {
		Element root=loadXMLFile(file);
		if (root==null) return false;

		for (String test: new EditModel().getRootNodeNames()) if (root.getNodeName().equalsIgnoreCase(test)) return true;

		return false;
	}

	/**
	 * Prüft, ob die übergebene Datei eine Statistik-Datei ist
	 * @param file	Zu prüfende Datei
	 * @return	Gibt <code>true</code> zurück, wenn es sich um eine Statistik-Datei handelt
	 */
	protected final boolean isStatisticFile(final File file) {
		Element root=loadXMLFile(file);
		if (root==null) return false;

		for (String test: new Statistics().getRootNodeNames()) if (root.getNodeName().equalsIgnoreCase(test)) return true;

		return false;
	}

	/**
	 * Wartet bis das als Parameter übergebene Simulator-Interface fertig ist und gibt ggf. Zwischenfortschrittsmeldungen aus.
	 * @param simulator	Interface auf das Simulator-Objekt, welches überwacht werden soll
	 * @param minimalOutput	Wird hier <code>false</code> übergeben, so werden Fortschrittsmeldungen ausgegeben.
	 * @param out Ein optionales {@link PrintStream}-Objekt, über das Texte ausgegeben werden können.
	 */
	public final static void waitForSimulationDone(final AnySimulator simulator, final boolean minimalOutput, final PrintStream out) {
		final long startTime=System.currentTimeMillis();
		long lastGesamt=Integer.MAX_VALUE;

		if (!minimalOutput && out!=null) out.println(Language.tr("Simulation.Started"));

		String s;
		int count=0;
		while (simulator.isRunning()) {
			try {Thread.sleep(25);} catch (InterruptedException e) {}
			if (minimalOutput) continue;
			count++;
			if (count%50==0) {
				final long current=simulator.getCurrentClients();
				final long sum=simulator.getCountClients();
				final long time=System.currentTimeMillis();
				if (sum<0) {
					s=String.format(Language.tr("Simulation.CommandLine.Progress1"),NumberTools.formatLong(current/1000),NumberTools.formatLong(simulator.getEventCount()/1000000),NumberTools.formatLong(simulator.getEventsPerSecond()/1000),NumberTools.formatLong((time-startTime)/1000));
					if (s.length()>78) s=String.format(Language.tr("Simulation.CommandLine.Progress1.Shorter"),NumberTools.formatLong(current/1000),NumberTools.formatLong(simulator.getEventCount()/1000000),NumberTools.formatLong(simulator.getEventsPerSecond()/1000),NumberTools.formatLong((time-startTime)/1000));
					if (out!=null) out.println(s);
				} else {
					String add="";
					if (time-startTime>2000) {
						double gesamt=(time-startTime)/(((double)current)/sum);
						gesamt-=(time-startTime);
						if (gesamt/1000<lastGesamt) {
							lastGesamt=(int) Math.round(gesamt/1000);
							add=String.format(", "+Language.tr("Simulation.CommandLine.Progress3"),NumberTools.formatLong(Math.max(0,lastGesamt)));
						}
					}
					s=String.format(Language.tr("Simulation.CommandLine.Progress2"),NumberTools.formatLong(current/1000),NumberTools.formatLong(sum/1000),NumberTools.formatLong(simulator.getEventCount()/1000000),NumberTools.formatLong(simulator.getEventsPerSecond()/1000),add);
					if (s.length()>78) s=String.format(Language.tr("Simulation.CommandLine.Progress2.Shorter"),NumberTools.formatLong(current/1000),NumberTools.formatLong(sum/1000),NumberTools.formatLong(simulator.getEventCount()/1000000),NumberTools.formatLong(simulator.getEventsPerSecond()/1000),add);
					if (out!=null) out.println(s);
				}
			}
		}
	}

	/**
	 * Versucht basierend auf einem {@link EditModel} eine Simulation vorzubereiten und zu starten
	 * @param editModel	Editor-Modell, welches verwendet werden soll (darf nicht <code>null</code> sein)
	 * @param out	Optionale Ausgabe von Meldungen
	 * @return	Liefert im Erfolgsfall ein {@link AnySimulator}-Objekt, sonst einen String mit einer Fehlermeldung
	 */
	public static final Object prepare(final EditModel editModel, final PrintStream out) {
		/* Modell vorbereiten */
		if (EditModelBase.isNewerVersionSystem(editModel.version,EditModel.systemVersion)) {
			if (out!=null) out.println(Language.tr("Dialog.Title.Warning").toUpperCase()+": "+Language.tr("Editor.NewerVersion.Info.Short"));
		} else {
			if (editModel.isUnknownElementsOnLoad()) {
				if (out!=null) out.println(Language.tr("Dialog.Title.Warning").toUpperCase()+": "+Language.tr("Editor.UnknownElements.Info.Short"));
			}
		}
		final StartAnySimulator starter=new StartAnySimulator(editModel);
		final String error=starter.prepare();
		if (error!=null) {
			if (out!=null) out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.PreparationOfModel")+": "+error);
			return error;
		}

		/* Simulation starten */
		return starter.start();
	}

	/**
	 * Gibt mögliche Warnungen, die beim Laden der externen Daten aufgetreten sein können, aus.
	 * @param warnings	Mögliche Warnungen (kann leer oder <code>null</code> sein, dann werden keine Warnungen ausgegeben)
	 * @param out	Optionale Ausgabe von Meldungen
	 */
	public static final void outputModelLoadDataWarnings(final List<String> warnings, final PrintStream out) {
		if (warnings==null || warnings.size()==0 || out==null) return;

		out.println(Language.tr("ModelLoadData.ProcessError.CommandLineTitle"));
		warnings.forEach(out::println);
	}

	private volatile AnySimulator simulator;

	/**
	 * Führt eine Simulation aus und liefert das Ergebnis-Statistik-Objekt zurück
	 * @param editModel	Zu simulierendes Modell
	 * @param minimalOutput	Wird hier <code>false</code> übergeben, so werden Fortschrittsmeldungen ausgegeben.
	 * @param maxThreads Gibt an, wie viele Threads maximal verwendet werden sollen.
	 * @param out	Ein <code>PrintStream</code>-Objekt, über das Texte ausgegeben werden können.
	 * @return	Gibt im Erfolgsfalls das Statistik-Objekt zurück, sonst <code>null</code>
	 */
	protected final Statistics singleSimulation(final EditModel editModel, final boolean minimalOutput, final int maxThreads, final PrintStream out) {
		/* Vorbereiten und starten */
		final Object obj=prepare(editModel,out);
		if (!(obj instanceof AnySimulator)) return null;
		simulator=(AnySimulator)obj;

		/* Auf Ende der Simulation warten */
		waitForSimulationDone(simulator,minimalOutput,out);

		/* Statistik zusammenstellen */
		final Statistics statistics=simulator.getStatistic();
		if (statistics==null) {
			if (out!=null) out.println(Language.tr("CommandLine.Simulation.NoResults"));
		} else {
			if (!minimalOutput) out.println(String.format(Language.tr("CommandLine.Simulation.Done"),NumberTools.formatLong(statistics.simulationData.runTime)));
		}

		simulator=null;

		return statistics;
	}

	/**
	 * Führt eine Simulation aus und liefert das Ergebnis-Statistik-Objekt zurück
	 * @param editModel	Zu simulierendes Modell
	 * @param minimalOutput	Wird hier <code>false</code> übergeben, so werden Fortschrittsmeldungen ausgegeben.
	 * @param out Ein <code>PrintStream</code>-Objekt, über das Texte ausgegeben werden können.
	 * @return	Gibt im Erfolgsfall das Statistik-Objekt zurück, sonst <code>null</code>
	 */
	protected final Statistics singleSimulation(final EditModel editModel, final boolean minimalOutput, final PrintStream out) {
		return singleSimulation(editModel,minimalOutput,Integer.MAX_VALUE,out);
	}

	/**
	 * Speichert die Statistikdaten in einer Datei und gibt im Fehlerfall eine Meldung auf der Konsole aus
	 * @param statistics	Zu speichernde Statistikdaten
	 * @param statisticsFile	Datei, in der die Statistik gespeichert werden soll
	 * @param out Ein <code>PrintStream</code>-Objekt, über das Texte ausgegeben werden können.
	 * @return	Gibt zurück, ob das Speichern erfolgreich verlief
	 */
	protected final boolean saveStatistics(final Statistics statistics, final File statisticsFile, final PrintStream out) {
		if (!statistics.saveToFile(statisticsFile)) {out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.UnableToSaveStatistic")); return false;}
		return true;
	}

	private volatile boolean canceled=false;

	/**
	 * Gibt an, ob {@link #setQuit()} aufgerufen wurde.
	 * @return	Abbruchstatus
	 */
	protected final boolean isCanceled() {
		return canceled;
	}

	@Override
	public void setQuit() {
		canceled=true;
		if (simulator!=null) simulator.cancel();
	}
}
