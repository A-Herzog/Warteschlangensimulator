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
package net.socket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import language.Language;
import net.calc.SimulationServer;
import simulator.AnySimulator;
import simulator.Simulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import tools.SetupData;
import ui.parameterseries.ParameterCompareRunner;
import ui.parameterseries.ParameterCompareSetup;
import xml.XMLTools;
import xml.XMLTools.FileType;

/**
 * Innerhalb dieser Klasse wird eine Simulation für {@link SocketServerCalc}
 * durchgeführt und es werden die Ergebnisse vorgehalten.
 * @author Alexander Herzog
 * @see SocketServerCalc
 */
public class SocketServerTask {
	/**
	 * ID des Tasks
	 */
	public final int id;

	/**
	 * Zu simulierendes Modell (kann <code>null</code> sein)
	 */
	private final EditModel model;

	/**
	 * Zu simulierende Parameterreihe (kann <code>null</code> sein)
	 */
	private final ParameterCompareSetup series;

	/**
	 * Ergebnisse der Simulation
	 * @see #setResult(byte[])
	 * @see #getResult()
	 */
	private byte[] result;

	/**
	 * Simulator-System zur Simulation einfacher Modelle
	 * @see #startModel()
	 */
	private AnySimulator simulator;

	/**
	 * Parameterreihen-System zur Simulation ganzer Parameterreihen
	 * @see #startSeries()
	 */
	private ParameterCompareRunner runner;

	/**
	 * Konstruktor der Klasse
	 * @param id	ID des Tasks
	 * @param model	Zu simulierendes Modell
	 */
	private SocketServerTask(final int id, final EditModel model) {
		this.id=id;
		this.model=model;
		this.series=null;
	}

	/**
	 * Konstruktor der Klasse
	 * @param id	ID des Tasks
	 * @param series	Zu simulierende Parameterreihe
	 */
	private SocketServerTask(final int id, final ParameterCompareSetup series) {
		this.id=id;
		this.model=null;
		this.series=series;
	}

	/**
	 * Versucht ein Modell aus html-Daten zu laden
	 * @param data	html-Daten als Bytes
	 * @return	Liefert im Erfolgsfall das Modell, sonst <code>null</code>
	 */
	private static EditModel tryLoadHTML(final byte[] data) {
		if (data==null || data.length==0) return null;
		final String text=new String(data);

		boolean firstLine=true;
		boolean modelDataFollow=false;

		final String[] lines=text.split("\n");
		if (lines==null) return null;

		for (String line: lines) {
			if (firstLine) {
				if (!line.trim().equalsIgnoreCase("<!doctype html>")) return null;
			} else {
				if (modelDataFollow) {
					if (!line.trim().startsWith("data:application/xml;base64,")) return null;
					final String base64data=line.trim().substring("data:application/xml;base64,".length());
					final ByteArrayInputStream inputStream=new ByteArrayInputStream(Base64.getDecoder().decode(base64data));
					final EditModel model=new EditModel();
					if (model.loadFromStream(inputStream,FileType.AUTO)==null) return model;
				} else {
					if (line.trim().equalsIgnoreCase("QSModel")) modelDataFollow=true;
				}
			}
			firstLine=false;
		}

		return null;
	}

	/**
	 * Versucht ein {@link SocketServerTask}-Objekt auf Basis von zu ladenden Daten zu erstellen
	 * @param id	ID für die neue Aufgabe
	 * @param data	Zu ladende Daten (Modell oder Parameterreihe)
	 * @return	Liefert im Erfolgsfall ein neues Objekt, sonst <code>null</code>
	 */
	public static SocketServerTask loadData(final int id, final byte[] data) {
		final EditModel htmlBasedModel=tryLoadHTML(data);
		if (htmlBasedModel!=null) {
			return new SocketServerTask(id,htmlBasedModel);
		}

		final EditModel model=new EditModel();
		if (model.loadFromStream(new ByteArrayInputStream(data),FileType.AUTO)==null) {
			return new SocketServerTask(id,model);
		}

		final ParameterCompareSetup series=new ParameterCompareSetup(null);
		if (series.loadFromStream(new ByteArrayInputStream(data),FileType.AUTO)==null) {
			return new SocketServerTask(id,series);
		}

		return null;
	}

	/**
	 * Bricht eine laufende Simulation ab.
	 * @see #start()
	 */
	public void cancel() {
		synchronized(this) {
			if (simulator!=null) simulator.cancel();
			if (runner!=null) runner.cancel();
		}
	}

	/**
	 * Prüft, ob die Simulation bereits beendet wurde.
	 * @return	Liefert <code>true</code>, wenn die Simulation beendet wurde.
	 * @see #start()
	 * @see #getResult()
	 */
	public boolean isDone() {
		synchronized(this) {
			return result!=null;
		}
	}

	/**
	 * Stellt die über {@link #getResult()} abrufbaren Ergebnisse ein.
	 * @param data	Ergebnisse
	 */
	private void setResult(final byte[] data) {
		synchronized(this) {
			result=data;
		}
	}

	/**
	 * Startet die Simulation der aktuellen Aufgabe.<br>
	 * (Die eigentliche Simulation findet in einem eigenständigen Thread statt.
	 * Diese Methode kehrt sofort wieder zurück.)
	 * @see #cancel()
	 * @see #isDone()
	 */
	public void start() {
		final Thread thread=new Thread(()->{
			if (model!=null) startModel();
			if (series!=null) startSeries();
		},"SocketSimulation");
		thread.start();
	}

	/**
	 * Startet die Simulation eines einfachen Modells.
	 * @see #start()
	 * @see #model
	 * @see #simulator
	 */
	private void startModel() {
		if (!StartAnySimulator.isRemoveSimulateable(model)) {
			setResult(SimulationServer.PREPARE_NO_REMOTE_MODEL.getBytes());
			return;
		}

		final StartAnySimulator starter=new StartAnySimulator(model,null,null,Simulator.logTypeFull);
		final String prepareError=starter.prepare();
		if (prepareError!=null) {setResult(prepareError.getBytes()); return;}
		synchronized(SocketServerTask.this) {
			simulator=starter.start();
		}
		final ByteArrayOutputStream output=new ByteArrayOutputStream();
		try {
			final Statistics statistics=simulator.getStatistic();
			final XMLTools.FileType fileType=SetupData.getSetup().defaultSaveFormatStatistics.fileType;
			statistics.saveToStream(output,fileType);

		} finally {
			setResult(output.toByteArray());
			synchronized(SocketServerTask.this) {
				simulator=null;
			}
		}
	}

	/**
	 * Startet die Simulation einer Parameterreihe.
	 * @see #start()
	 * @see #series
	 * @see #runner
	 */
	private void startSeries() {
		synchronized(SocketServerTask.this) {
			runner=new ParameterCompareRunner(null,null,null);
		}

		try {
			final String error=runner.check(series);
			if (error!=null) {setResult(error.getBytes()); return;}
			runner.start();
			if (runner.waitForFinish()) {
				final ByteArrayOutputStream output=new ByteArrayOutputStream();
				final XMLTools.FileType fileType=SetupData.getSetup().defaultSaveFormatParameterSeries.fileType;
				series.saveToStream(output,fileType);
				setResult(output.toByteArray());
			} else {
				setResult(Language.tr("CalcWebServer.Simulation.Failed").getBytes());
			}
		} finally {
			synchronized(SocketServerTask.this) {
				runner=null;
			}
		}
	}

	/**
	 * Liefert die Ergebnisse der Simulation.
	 * @return	Ergebnisse der Simulation
	 * @see #result
	 */
	public byte[] getResult() {
		synchronized(this) {
			return result;
		}
	}
}
