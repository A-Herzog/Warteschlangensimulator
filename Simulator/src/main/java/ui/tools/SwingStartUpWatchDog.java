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
package ui.tools;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

/**
 * Der Swing-Watch-Dog legt eine Sekunde nach dem Aufruf von
 * {@link #start(int)} ein Ereignis in die AWT-Event-Queue.
 * Wird dieses dann nicht innerhalb der angegebenen Anzahl an
 * Sekunden abgearbeitet (d.h. ist die Event-Queue blockiert),
 * so wird das Programm abgebrochen.
 * @author Alexander Herzog
 */
public class SwingStartUpWatchDog {
	/**
	 * Funktioniert die AWT-Event-Queue auch noch nach einer Sekunde Laufzeit?
	 * Dann kann der Watch-Dog-Thread abgebrochen werden.
	 * @see #startOk
	 */
	private static volatile boolean startOk;

	/**
	 * Watch-Dog-Thread, der die Anwendung notwendigenfalls abbricht
	 * @see #startOk
	 */
	private static Thread thread;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur statische Hilfsroutinen zur Verfügung.
	 */
	private SwingStartUpWatchDog() {}

	/**
	 * Startet das Watch-Dog-System, das prüft, ob die AWT-Event-Queue dauerhaft funktioniert.
	 * @param seconds	Zusätzliche Sekunden die dem System zugestanden werden sollen, die Erreichbarkeit nachzuweisen
	 */
	public static void start(final int seconds) {
		startOk=false;

		/* Watch-Dog-Thread starten, der die Anwendung notwendigenfalls abbricht, wenn die AWT-Event-Queue blockiert */
		thread=new Thread(()->{
			try {
				Thread.sleep(seconds*1000+1000);
			} catch (InterruptedException e) {}

			if (!startOk) {
				Runtime.getRuntime().halt(0); /* Wenn der AWT-Thread blockiert ist, funktioniert nicht einmal mehr System.exit(0), daher dieser Umweg. */
			}

		},"Startup watch dog");
		thread.setDaemon(true);
		thread.start();

		/* Timer um den Watch-Dog-Thread wieder zu beenden, wenn die AWT-Event-Queue nach einer Sekunde immer noch funktioniert. */
		final Timer timer=new Timer("Stop watch dog",true);
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				SwingUtilities.invokeLater(()->{
					startOk=true;
					if (thread!=null) {
						thread.interrupt();
						thread=null;
					}
				});
				timer.cancel();
			}
		},1000);
	}
}
