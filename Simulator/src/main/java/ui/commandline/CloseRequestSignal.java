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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

/**
 * Prüft, ob der Nutzer den Server beenden will
 * (in dem ein TERM-Signal gesendet wurde oder per Tastendruck).
 * @author Alexander Herzog
 * @version 1.2
 */
public class CloseRequestSignal {
	/* private final CloseSignalHandler signal; */
	private boolean quit=false;
	private final InputStream listenKeys;
	private final Semaphore mutex;

	/**
	 * Konstruktor der Klasse <code>CloseRequestSignal</code>
	 * @param listenSignal	Prüft auf TERM-Signale (funktionslos, wird nicht mehr unterstützt)
	 * @param listenKeys	Prüft auf Tastendrücke, wenn hier ein {@link InputStream} übergeben wird
	 */
	public CloseRequestSignal(boolean listenSignal, InputStream listenKeys) {
		mutex=new Semaphore(1);
		/* if (listenSignal) signal=new CloseSignalHandler("TERM",false); else signal=null; */
		this.listenKeys=listenKeys;
	}

	/**
	 * Prüft, ob der Nutzer den Server beenden will.
	 * @return	Liefert <code>true</code> zurück, wenn das Server-System beendet werden soll.
	 */
	public boolean isQuit() {
		try {
			mutex.acquire();
		} catch (InterruptedException e1) {return false;}
		try {
			if (quit) return true;

			/* if (signal!=null && signal.isSignal()) {quit=true; return true;} */

			if (listenKeys!=null)	{
				try {quit=(listenKeys.available()>0);} catch (IOException e) {}
				if (quit) return true;
			}

			return false;
		} finally {
			mutex.release();
		}
	}

	/**
	 * Setzt das Quit-Flag von Benutzerseite.
	 */
	public void setQuit() {
		quit=true;
	}

	/*
	private class CloseSignalHandler implements SignalHandler {
		private final SignalHandler oldHandler;
		private boolean sig=false;
		public CloseSignalHandler(String signalName, boolean keepDefaultHandler) {
			SignalHandler handler=Signal.handle(new Signal(signalName),this);
			oldHandler=(keepDefaultHandler)?handler:SIG_DFL;
		}
		public boolean isSignal() {return sig;}
		@Override public void handle(Signal signal) {sig=true; if (oldHandler!=SIG_DFL && oldHandler!=SIG_IGN) oldHandler.handle(signal);}
	}
	 */
}
