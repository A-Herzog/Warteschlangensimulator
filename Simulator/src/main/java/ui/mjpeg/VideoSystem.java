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

/**
 * Interface welches verschiedene Aufzeichnungssysteme implementieren m�ssen,
 * um w�hrend der Animation f�r die Aufzeichnung verwendet werden zu k�nnen.
 * @author Alexander Herzog
 */
public interface VideoSystem {

	/**
	 * Gibt an, ob das System korrekt initialisiert wurde und Bilder aufnehmen kann.
	 * @return	Gibt <code>true</code> zur�ck, wenn das System einsatzbereit ist.
	 */
	boolean isReady();

	/**
	 * F�gt ein Bild zum Ausgabepuffer hinzu
	 * @param image	Zu speicherndes Bild
	 * @param timeStamp	Zeitpunkt, zu dem dieses Bild aufgenommen wurde
	 * @return	Gibt <code>true</code> zur�ck, wenn das Bild erfolgreich geschrieben werden konnte
	 */
	boolean addFrame(final BufferedImage image, final long timeStamp);

	/**
	 * Erstellt ein Bild-Objekt oder holt es aus dem Recycling-Cache
	 * @param width	Breite (wird nur beim Erstellen ber�cksichtigt, bei Cache-Objekten wird davon ausgegangen, dass die Ma�e passen bzw. sich diese nicht von Frame zu Frame �ndern)
	 * @param height	H�he (wird nur beim Erstellen ber�cksichtigt, bei Cache-Objekten wird davon ausgegangen, dass die Ma�e passen bzw. sich diese nicht von Frame zu Frame �ndern)
	 * @return	Bild-Objekt zur Verwendung durch das Animationssystem, um ein Frame zum Speichern zu erstellen
	 */
	BufferedImage getImageObjectFromCache(final int width, final int height);

	/**
	 * Erstellt aus den aufgezeichneten Bildern eine Videodatei	 *
	 * @return	Gibt <code>true</code> zur�ck, wenn die Videodatei erfolgreich erstellt werden konnte
	 */
	boolean done();

	/**
	 * Anzahl der geschriebenen Bilder
	 * @return	Anzahl der geschriebenen Bilder
	 */
	int getFrameCount();

	/**
	 * Anzahl der geschriebenen Bytes
	 * @return	Anzahl der geschriebenen Bytes
	 */
	long getBytesCount();
}
