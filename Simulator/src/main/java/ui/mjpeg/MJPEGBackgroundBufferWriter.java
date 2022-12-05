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
package ui.mjpeg;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;

/**
 * Diese Klasse schreibt mit Hilfe eines Hintergrund-Threads Bilder in eine temporäre Datei,
 * um später daraus eine MJPEG-Datei zu machen.
 * @author Alexander Herzog
 */
public class MJPEGBackgroundBufferWriter extends MJPEGBufferWriterBase {
	/** Mutex zur Absicherung, dass nicht gleichzeitig neue Frames in die Warteschlange eingefügt und entnommen werden */
	private Semaphore queueMutex;
	/** Warteschlange der zu schreibenden Bilder */
	private Deque<BufferedImage> queueImages;
	/** Zeitstempel für die zu schreibenden Bilder */
	private Deque<Long> queueTimeStamps;
	/** Gibt am Ende der Aufzeichnung an, dass keine weiteren Frames mehr dazu kommen. Ist die Warteschlange leer und {@link #queueEnd} gesetzt, so beendet sich der Ausgabethread. */
	private boolean queueEnd;

	/** Benachrichtigt den {@link #writerThread}, dass neue Bilder auf Verarbeitung warten */
	private Object writerThreadNotify;
	/** Thread zur Zeitversetzten Ausgabe der Bilder */
	private WriterThread writerThread;

	/**
	 * Konstruktor der Klasse <code>MJPEGBackgroundBufferWriter</code>
	 */
	public MJPEGBackgroundBufferWriter() {
		super();

		queueMutex=new Semaphore(1);
		queueImages=new ArrayDeque<>();
		queueTimeStamps=new ArrayDeque<>();

		writerThreadNotify=new Object();
		writerThread=new WriterThread();
		writerThread.start();
	}

	/**
	 * Schreibt ein Bild in den Ausgabe-Stream für die temporären Daten
	 * @param image	Zu speicherndes Bild
	 * @param timeStamp	Zeitstempel
	 * @return	Liefert im Erfolgsfall
	 * @see #tempOutputData
	 */
	private boolean storeImageToStream(final BufferedImage image, final long timeStamp) {
		if (tempOutputData==null) return false;

		frameCount++;

		try {
			tempOutputData.writeLong(timeStamp);
			final ByteArrayOutputStream buffer=new ByteArrayOutputStream();
			ImageIO.write(image,"png",buffer);
			tempOutputData.writeInt(buffer.size());
			tempOutputData.write(buffer.toByteArray());
			bytesCount+=4+8+buffer.size();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Fügt ein Bild zum Ausgabepuffer hinzu
	 * @param image	Zu speicherndes Bild
	 * @param timeStamp	Zeitpunkt, zu dem dieses Bild aufgenommen wurde
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich geschrieben werden konnte
	 */
	@Override
	public boolean addFrame(final BufferedImage image, final long timeStamp) {
		if (!isReady()) return false;

		int queueSize=0;
		queueMutex.acquireUninterruptibly(); try {queueSize=queueImages.size();} finally {queueMutex.release();}
		while (queueSize>10) {
			try {Thread.sleep(50);} catch (InterruptedException e) {}
			queueMutex.acquireUninterruptibly(); try {queueSize=queueImages.size();} finally {queueMutex.release();}
		}

		queueMutex.acquireUninterruptibly();
		try {
			queueImages.offer(image);
			queueTimeStamps.offer(timeStamp);
			synchronized (writerThreadNotify) {writerThreadNotify.notify();}
		} finally {
			queueMutex.release();
		}

		return true;
	}

	/**
	 * Beendet die Ausgabe, leert den Ausgabepuffer und schließt die Datei
	 * @return	Gibt <code>true</code> zurück, wenn das System erfolgreich abgeschlossen werden konnte.
	 */
	@Override
	public boolean doneFrames() {
		if (!isReady()) return false;

		queueMutex.acquireUninterruptibly();
		try {
			queueEnd=true;
			synchronized (writerThreadNotify) {writerThreadNotify.notify();}
		} finally {
			queueMutex.release();
		}

		try {
			writerThread.join();
		} catch (InterruptedException e) {}

		return true;
	}

	/**
	 * Ausgabethread
	 * @see MJPEGBackgroundBufferWriter#writerThread
	 */
	private class WriterThread extends Thread {
		/**
		 * Konstruktor der Klasse
		 */
		public WriterThread() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Speichert alle momentan wartenden Frames im temporäre Daten-Stream
		 * @return	Wenn sich der Thread beenden soll (weil keine weiteren Frames mehr folgen können), wird <code>true</code> zurückgeliefert; sonst <code>false</code>
		 * @see MJPEGBackgroundBufferWriter#storeImageToStream
		 */
		private boolean storeData() {
			while (true) {
				BufferedImage image;
				long timeStamp;
				queueMutex.acquireUninterruptibly();
				try {
					if (!queueImages.isEmpty()) {
						image=queueImages.poll();
						timeStamp=queueTimeStamps.poll();
					} else {
						return queueEnd;
					}
				} finally {
					queueMutex.release();
				}
				storeImageToStream(image,timeStamp);
			}
		}

		@Override
		public void run() {
			boolean done=false;
			while (!done && !isInterrupted()) {
				synchronized (writerThreadNotify) {
					try {writerThreadNotify.wait(1000);} catch (InterruptedException e) {}
				}
				done=storeData();
			}
			doneStreams();
		}
	}
}
