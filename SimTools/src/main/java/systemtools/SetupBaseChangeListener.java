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
package systemtools;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Überwacht eine (Setup-)Datei auf Änderungen von außerhalb des Programmes
 * und löst eine Benachrichtigung aus, wenn sich die Datei verändert hat.<br>
 * Diese Klasse wird von {@link SetupBase} intern verwendet, um
 * Veränderungsnachrichten anbieten zu können.
 * @author Alexander Herzog
 * @version 1.4
 * @see SetupBase
 */
public class SetupBaseChangeListener {
	/** Pfad in dem sich die zu überwachende Datei befindet */
	private final File watchFilePath;
	/** Dateiname der zu überwachenden Datei */
	private final String watchFileName;
	/** Runnable, das aufgerufen wird, wenn die Datei verändert wurde */
	private final Runnable notify;
	/** Hintergrund-Thread, der die Verbindung zum {@link WatchService} darstellt */
	private volatile Thread worker=null;
	/** Absicherung, dass {@link #start()} und {@link #stop()} nicht mehrfach gleichzeitig aufgerufen werden. */
	private final Semaphore mutex;

	/**
	 * Konstruktor der Klasse.<br>
	 * Startet die Überwachung noch nicht direkt. Diese muss über den Aufruf
	 * von {@link SetupBaseChangeListener#start()} manuell gestartet werden.
	 * @param watchFile	Zu überwachende Datei
	 * @param notify	Runnable, das aufgerufen wird, wenn die Datei verändert wurde
	 */
	public SetupBaseChangeListener(final File watchFile, final Runnable notify) {
		watchFilePath=watchFile.getParentFile();
		watchFileName=watchFile.getName();
		this.notify=notify;
		mutex=new Semaphore(1);
	}

	/**
	 * Startet die Überwachung der Datei.
	 */
	public synchronized void start() {
		mutex.acquireUninterruptibly();
		try {
			if (worker!=null) return;
			worker=new Thread(()->work(),"SetupChangeWatcher");
			worker.setDaemon(true);
			worker.start();
		} finally {
			mutex.release();
		}
	}

	/**
	 * Stoppt die Überwachung der Datei.
	 */
	public synchronized void stop() {
		mutex.acquireUninterruptibly();
		try {
			if (worker==null) return;
			worker.interrupt();
			worker.join();
			worker=null;
		} catch (InterruptedException e) {} finally {
			mutex.release();
		}
	}

	/**
	 * Benachrichtigt {@link SetupBaseChangeListener#notify},
	 * dass sich die Datei verändert hat.
	 * @see #work()
	 */
	private synchronized void changedNotify() {
		if (notify!=null) notify.run();
	}

	/**
	 * Arbeitsmethode innerhalb von {@link SetupBaseChangeListener#worker},
	 * die in regelmäßigen Abständen den {@link WatchService} abfragt.
	 * @see SetupBaseChangeListener#worker
	 * @see SetupBaseChangeListener#start()
	 * @see SetupBaseChangeListener#stop()
	 */
	private void work() {
		if (watchFilePath==null || watchFileName==null || watchFileName.isEmpty()) return;
		final Path path=watchFilePath.toPath();
		long lastChange=0;

		try {Thread.sleep(3000);} catch (InterruptedException e) {return;}

		final Set<Thread> threadsBefore=Thread.getAllStackTraces().keySet();

		try (@SuppressWarnings("resource") final WatchService watcher=FileSystems.getDefault().newWatchService()) {
			if (watcher==null) return;

			final Set<Thread> threadsAfter=Thread.getAllStackTraces().keySet();
			threadsAfter.removeAll(threadsBefore);
			if (threadsAfter.size()==1) threadsAfter.forEach(t->t.setName("SetupChangeWatcherFileSystem"));

			final WatchKey key=path.register(watcher,StandardWatchEventKinds.ENTRY_MODIFY);
			if (!key.isValid()) return;

			while (!Thread.interrupted()) {
				try {
					if (watcher.poll()==null) Thread.sleep(500);
					if (watcher.poll()==null) continue;
				} catch (InterruptedException e) {
					return;
				}

				for (WatchEvent<?> event: key.pollEvents()) {
					if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY) && event.context() instanceof Path) {
						final Path changed=(Path)event.context();
						if (changed.toString().equals(watchFileName)) {
							if (System.currentTimeMillis()-lastChange>=10)  changedNotify();
							break;
						}
					}
				}

				lastChange=System.currentTimeMillis();
				key.reset();
			}
		} catch (IOException e) {}
	}
}