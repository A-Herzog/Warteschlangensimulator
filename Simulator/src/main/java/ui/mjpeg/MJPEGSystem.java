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
import java.io.File;

/**
 * Erzeugt eine MJPEG-Videodatei aus einzelnen Animationsbildern
 * @author Alexander Herzog
 */
public class MJPEGSystem implements VideoSystem {
	/** Gibt an, ob einzelne Frames wiederholt werden sollen, um l�ngere Zeitdauern zwischen �nderungen am System im Video abzubilden. */
	private final boolean useAdditionalFrames;
	/** System zur Zwischenspeicherung der Bilder */
	private final MJPEGBufferWriterBase buffer;
	/** Ausgabe in die finale Datei */
	private final MJPEGBuilder storage;

	/** Bilder direkt beim Puffern als JPEG speichern, so dass die Erstellung der avi-Datei schneller geht? */
	private static final boolean useDirectPath=true;

	/**
	 * Konstruktor der Klasse <code>MJPEGSystem</code>
	 * @param outputFile	Video-Ausgabedatei
	 * @param useAdditionalFrames	Gibt an, ob einzelne Frames wiederholt werden sollen, um l�ngere Zeitdauern zwischen �nderungen am System im Video abzubilden.
	 */
	public MJPEGSystem(final File outputFile, final boolean useAdditionalFrames) {
		/* altes System: buffer=new MJPEGBackgroundBufferWriter(); */
		buffer=new MJPEGMultiThreadBufferWriter(useDirectPath,0.8f);
		storage=new MJPEGBuilder(buffer.getFile(),outputFile);
		this.useAdditionalFrames=useAdditionalFrames;
	}

	/**
	 * Gibt an, ob das System korrekt initialisiert wurde und Bilder aufnehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn das System einsatzbereit ist.
	 */
	@Override
	public boolean isReady() {
		return buffer.isReady();
	}

	/**
	 * Anzahl der geschriebenen Bilder
	 * @return	Anzahl der geschriebenen Bilder
	 */
	@Override
	public int getFrameCount() {
		return buffer.getFrameCount();
	}

	/**
	 * Anzahl der geschriebenen Bytes
	 * @return	Anzahl der geschriebenen Bytes
	 */
	@Override
	public long getBytesCount() {
		return buffer.getBytesCount();
	}

	/**
	 * Erstellt ein Bild-Objekt oder holt es aus dem Recycling-Cache
	 * @param width	Breite (wird nur beim Erstellen ber�cksichtigt, bei Cache-Objekten wird davon ausgegangen, dass die Ma�e passen bzw. sich diese nicht von Frame zu Frame �ndern)
	 * @param height	H�he (wird nur beim Erstellen ber�cksichtigt, bei Cache-Objekten wird davon ausgegangen, dass die Ma�e passen bzw. sich diese nicht von Frame zu Frame �ndern)
	 * @return	Bild-Objekt zur Verwendung durch das Animationssystem, um ein Frame zum Speichern zu erstellen
	 */
	@Override
	public BufferedImage getImageObjectFromCache(final int width, final int height) {
		return buffer.getImageObjectFromCache(width,height);
	}

	/**
	 * F�gt ein Bild zum Ausgabepuffer hinzu
	 * @param image	Zu speicherndes Bild
	 * @param timeStamp	Zeitpunkt, zu dem dieses Bild aufgenommen wurde
	 * @return	Gibt <code>true</code> zur�ck, wenn das Bild erfolgreich geschrieben werden konnte
	 */
	@Override
	public boolean addFrame(final BufferedImage image, final long timeStamp) {
		if (!buffer.isReady()) return false;
		return buffer.addFrame(image,timeStamp);
	}

	/**
	 * Erstellt aus den aufgezeichneten Bildern eine Videodatei
	 * @return	Gibt <code>true</code> zur�ck, wenn die Videodatei erfolgreich erstellt werden konnte
	 */
	@Override
	public boolean done() {
		if (!buffer.isReady()) return false;
		if (!buffer.doneFrames()) return false;

		new Thread(()->storage.process(buffer.getBytesCount(),useAdditionalFrames,useDirectPath)).start();

		return true;
	}

	/**
	 * Liefert die Anzahl an zu speichernden Bildern
	 * @return	Anzahl an zu speichernden Bildern
	 */
	public int getFramesToWrite() {
		return storage.getFramesToWrite();
	}

	/**
	 * Liefert die Anzahl an geschriebenen Bildern
	 * @return Anzahl an geschriebenen Bildern
	 */
	public int getFramesWritten() {
		return storage.getFramesWritten();
	}
}