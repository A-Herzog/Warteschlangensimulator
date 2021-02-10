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
package swingtools;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;

/**
 * Über diese Klasse kann abgefragt werden, ob in
 * {@link ImageIO} Funktionen, die erst mit Java 9
 * eingeführt wurden, verfügbar sind. Es wird nicht
 * die Java-Version, sondern die Feature-Verfügbarkeit
 * abgefragt.
 * @author Alexander Herzog
 * @see #hasTIFF()
 */
public class ImageIOFormatCheck {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden,
	 * sie bietet nur statische Hilfsroutinen an.
	 */
	private ImageIOFormatCheck() {
	}

	/**
	 * Steht das TIFF-Dateiformat zur Verfügung?
	 * @return	Liefert <code>true</code>, wenn {@link ImageIO} mit TIFF-Dateien umgehen kann
	 */
	public static boolean hasTIFF() {
		final IIORegistry registry=IIORegistry.getDefaultInstance();
		final Iterator<ImageWriterSpi> serviceProviders=registry.getServiceProviders(ImageWriterSpi.class,false);
		while(serviceProviders.hasNext()) {
			final ImageWriterSpi next=serviceProviders.next();
			for (String format: next.getFormatNames()) if (format.equalsIgnoreCase("TIFF")) return true;
		}
		return false;
	}
}
