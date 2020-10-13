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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Basisklasse, die Methoden zur Verfügung stellt, um Bilder in eine temporäre Datei
 * zu schreiben, um später daraus eine MJPEG-Datei zu machen
 * @author Alexander Herzog
 */
public abstract class MJPEGBufferWriterBase {
	/** Temporäre Datei */
	private final File tempFile;
	/** Output-Stream in {@link #tempFile} */
	private final FileOutputStream tempOutputStream;

	/**
	 * Stream, der von den abgeleiteten Klassen aus mit den Videodaten befüllt wird
	 */
	protected final DataOutputStream tempOutputData;

	/**
	 * Anzahl der geschriebenen Bilder
	 * @see #getFrameCount()
	 */
	protected int frameCount;

	/**
	 * Anzahl der geschriebenen Bytes
	 * @see #getBytesCount()
	 */
	protected long bytesCount;

	/**
	 * Konstruktor der Klasse
	 */
	public MJPEGBufferWriterBase() {
		frameCount=0;
		bytesCount=0;

		File f=null;
		try {
			f=File.createTempFile("QueueAVIBuffer",null,null);
		} catch (IOException e) {
			f=null;
		}
		tempFile=f;

		if (tempFile!=null) {
			FileOutputStream stream=null;

			try {
				stream=new FileOutputStream(tempFile);
			} catch (FileNotFoundException e) {
				stream=null;
			}
			tempOutputStream=stream;

			if (tempOutputStream!=null) {
				tempOutputData=new DataOutputStream(tempOutputStream);
			} else {
				tempOutputData=null;
			}
		} else {
			tempOutputStream=null;
			tempOutputData=null;
		}
	}

	/**
	 * Gibt an, ob das System erfolgreich initialisiert werden konnte<br>
	 * (d.h. erfolgreich eine Temp-Datei angelegt werden konnte usw.).
	 * @return	Gibt <code>true</code> zurück, wenn das System genutzt werden kann.
	 */
	public boolean isReady() {
		return tempOutputData!=null;
	}

	/**
	 * Alle beteiligten Streams leeren und schließen.
	 * @return	Gibt an, ob das Schließen der Streams erfolgreich war.
	 */
	protected boolean doneStreams() {
		if (tempOutputStream==null) return true;
		try {
			tempOutputData.flush();
			tempOutputData.close();
			tempOutputStream.flush();
			tempOutputStream.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Gibt den gewählten Namen der Temp-Datei zurück
	 * @return	Name der Temp-Datei, in die die Bilder geschrieben werden sollen
	 */
	public File getFile() {
		return tempFile;
	}

	/**
	 * Anzahl der geschriebenen Bilder
	 * @return	Anzahl der geschriebenen Bilder
	 */
	public int getFrameCount() {
		return frameCount;
	}

	/**
	 * Anzahl der geschriebenen Bytes
	 * @return	Anzahl der geschriebenen Bytes
	 */
	public long getBytesCount() {
		return bytesCount;
	}

	/**
	 * Fügt ein Bild zum Ausgabepuffer hinzu
	 * @param image	Zu speicherndes Bild
	 * @param timeStamp	Zeitpunkt, zu dem dieses Bild aufgenommen wurde
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich geschrieben werden konnte
	 */
	public abstract boolean addFrame(final BufferedImage image, final long timeStamp);

	/**
	 * Beendet die Ausgabe, leert den Ausgabepuffer und schließt die Datei
	 * @return	Gibt <code>true</code> zurück, wenn das System erfolgreich abgeschlossen werden konnte.
	 */
	public abstract boolean doneFrames();

	/**
	 * Erstellt ein Bild-Objekt oder holt es aus dem Recycling-Cache
	 * @param width	Breite (wird nur beim Erstellen berücksichtigt, bei Cache-Objekten wird davon ausgegangen, dass die Maße passen bzw. sich diese nicht von Frame zu Frame ändern)
	 * @param height	Höhe (wird nur beim Erstellen berücksichtigt, bei Cache-Objekten wird davon ausgegangen, dass die Maße passen bzw. sich diese nicht von Frame zu Frame ändern)
	 * @return	Bild-Objekt zur Verwendung durch das Animationssystem, um ein Frame zum Speichern zu erstellen
	 */
	public BufferedImage getImageObjectFromCache(final int width, final int height) {
		return new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
	}
}