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
package ui.modeleditor.fastpaint;

import java.awt.Rectangle;

/**
 * Tools zum Zeichnen der Stationen auf der Zeichenoberfl‰che.<br>
 * Diese Klasse kann nicht instanziert werden und enth‰lt nur
 * ststische Methoden.
 * @author Alexander Herzog
 */
public class FastPaintTools {
	private FastPaintTools() {}

	/**
	 * ‹bertr‰gt die Maﬂe aus einem Rechteck in ein anderes ohne
	 * dabei neue Objekte anzulegen
	 * @param source	Ausgangsrechteck
	 * @param destination	Zielrechteck
	 */
	public static void copyRectangleData(final Rectangle source, final Rectangle destination) {
		if (source!=null && destination!=null) {
			destination.x=source.x;
			destination.y=source.y;
			destination.width=source.width;
			destination.height=source.height;
		}
	}
}
