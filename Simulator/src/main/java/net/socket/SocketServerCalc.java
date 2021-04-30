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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mathtools.NumberTools;

/**
 * Socket-basierter Server zur Entgegennahme und Verarbeitung
 * von Simulations-Aufträgen.
 * @author Alexander Herzog
 * @see SocketServerBase
 * @see SocketServerTask
 */
public class SocketServerCalc extends SocketServerBase {
	/**
	 * Nachrichtentyp: Fehlermeldung
	 */
	private static final String MSG_TYPE_ERROR="ERROR";

	/**
	 * Nachrichtentyp: Informationen
	 */
	private static final String MSG_TYPE_MESSAGE="MESSAGE";

	/**
	 * Nachrichtentyp: ID des basierend auf den übermittelten Daten erstellten Tasks
	 */
	private static final String MSG_TYPE_ID="TASKID";

	/**
	 * Nachrichtentyp: Simulationsergebnisse
	 */
	private static final String MSG_TYPE_RESULT="RESULT";

	/**
	 * Liste der wartenden Aufgaben
	 */
	private final List<SocketServerTask> queue;

	/**
	 * Liste der abgeschlossenen Aufgaben
	 */
	private final List<SocketServerTask> results;

	/**
	 * Aktuell ausgeführte Aufgabe
	 */
	private SocketServerTask running;

	/**
	 * Verwaltungsthread, der die Aufgaben aus {@link #queue}
	 * entnimmt und startet.
	 */
	private ProcessThread processThread;

	/**
	 * Zähler für die Nummer der Anfrage
	 */
	private int runnerCounter;

	/**
	 * Konstruktor der Klasse
	 */
	public SocketServerCalc() {
		queue=new ArrayList<>();
		results=new ArrayList<>();
		runnerCounter=0;
	}

	@Override
	public boolean start(final int port) {
		if (!isRunning()) {
			synchronized(this) {
				queue.clear();
				results.clear();
				if (running!=null) running.cancel();
				running=null;
			}
		}
		return super.start(port);
	}

	@Override
	public void stop() {
		if (processThread!=null) {
			processThread.interrupt();
			processThread=null;
		}
		synchronized(this) {
			queue.clear();
			results.clear();
			if (running!=null) running.cancel();
			running=null;
		}
		super.stop();
	}

	@Override
	protected ByteArrayOutputStream process(final InputStream input) {
		final ByteArrayOutputStream output=new ByteArrayOutputStream();

		final String cmd=readString(input);
		if (cmd==null) {
			write(MSG_TYPE_ERROR,output);
			write("No Command given.",output);
			return output;
		}

		if (cmd.equalsIgnoreCase("TASK")) {
			final byte[] data=readData(input);
			if (data==null) {
				write(MSG_TYPE_ERROR,output);
				write("Invalid task data.",output);
				return output;
			}
			processTask(data,output);
			return output;
		}

		if (cmd.equalsIgnoreCase("STATUS")) {
			write(MSG_TYPE_MESSAGE,output);
			write(processInfo(),output);
			return output;
		}

		if (cmd.equalsIgnoreCase("RESULT")) {
			final String resultIdString=readString(input);
			if (resultIdString==null) {
				write(MSG_TYPE_ERROR,output);
				write("No result id given.",output);
				return output;
			}
			final Long resultId=NumberTools.getPositiveLong(resultIdString);
			if (resultId==null) {
				write(MSG_TYPE_ERROR,output);
				write("Invalid result id given.",output);
				return output;
			}
			processResult(resultId.intValue(),output);
			return output;
		}

		write(MSG_TYPE_ERROR,output);
		write("Unknown command.",output);
		return output;
	}

	/**
	 * Verarbeitet einen neue neue Aufgabe.
	 * @param data	Daten zu der neuen Aufgabe
	 * @param output	Ausgabe-Stream für Rückmeldungen
	 * @see #process(InputStream)
	 */
	private void processTask(final byte[] data, final ByteArrayOutputStream output) {
		runnerCounter++;
		final SocketServerTask task=SocketServerTask.loadData(runnerCounter,data);
		if (task==null) {
			runnerCounter--;
			write(MSG_TYPE_ERROR,output);
			write("Unknown file format.",output);
			return;
		}

		synchronized(this) {
			queue.add(task);
		}

		if (processThread==null) {
			processThread=new ProcessThread();
			processThread.start();
		}

		write(MSG_TYPE_ID,output);
		write(""+task.id,output);
	}

	/**
	 * Liefert den aktuellen Status des Servers als Stream.
	 * @return	Aktueller Status
	 * @see #process(InputStream)
	 */
	private String processInfo() {
		synchronized(this) {
			final StringBuilder result=new StringBuilder();
			result.append("Waiting:\n");
			queue.stream().map(task->task.id).forEach(id->result.append(id+"\n"));
			result.append("Running:\n");
			if (running!=null) result.append(running.id+"\n");
			result.append("Done:\n");
			results.stream().map(task->task.id).forEach(id->result.append(id+"\n"));
			return result.toString();
		}
	}

	/**
	 * Liefert die Simulationsergebnisse zu einer Aufgabe.
	 * @param id	ID derAufgabe
	 * @param output	Ausgabe-Stream für Rückmeldungen bzw. Rückgabe der Simulationsergebnisse
	 * @see #process(InputStream)
	 */
	private void processResult(final int id, final ByteArrayOutputStream output) {
		SocketServerTask task;

		synchronized(this) {
			final Optional<SocketServerTask> optional=results.stream().filter(t->t.id==id).findFirst();
			if (!optional.isPresent()) {
				write(MSG_TYPE_ERROR,output);
				write("No matching completed task.",output);
				return;
			}
			task=optional.get();
			results.remove(task);
		}

		write(MSG_TYPE_RESULT,output);
		write(task.getResult(),output);
		return;
	}

	/**
	 * Verwaltungsthread, der die Aufgaben aus {@link SocketServerCalc#queue} entnimmt und startet.
	 * @see SocketServerCalc#processThread
	 */
	private class ProcessThread extends Thread {
		/**
		 * Konstruktor der Klasse
		 */
		public ProcessThread() {
			super("SocketServerQueueProcessor");
		}

		@Override
		public void run() {
			while (!interrupted()) {

				synchronized(SocketServerCalc.this) {
					if (running==null || running.isDone()) {
						if (running!=null) {
							results.add(running);
							running=null;
						}
						if (queue.size()>0) {
							running=queue.remove(0);
							running.start();
						}
					}
				}

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {}

			}
		}
	}

	/**
	 * Singleton-Instanz dieser Klasse
	 * @see #getInstance()
	 */
	private static SocketServerCalc instance;

	/**
	 * Liefert eine Singleton-Instanz dieser Klasse
	 * @return	Instanz dieser Klasse
	 */
	public static synchronized SocketServerCalc getInstance() {
		if (instance==null) instance=new SocketServerCalc();
		return instance;
	}
}
