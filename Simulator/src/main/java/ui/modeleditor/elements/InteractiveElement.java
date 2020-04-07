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
package ui.modeleditor.elements;

import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;

/**
 * Wenn eine von {@link ModelElementPosition} abgeleitete Klasse
 * dieses Interface implementiert, wird es beim Testen und beim
 * Erstellen des Modells berücksichtigt, auch wenn es sich es nicht
 * von {@link ModelElementBox} ableitet oder keine einlaufenden Kanten
 * besitzt. Dies ist vor allem für interaktive Elemente, die sich
 * direkt von {@link ModelElementPosition} ableiten von Bedeutung,
 * aber auch für {@link ModelElementBox}-Elemente, die ohne
 * einlaufende Kanten auskommen (und nicht vorgeben, welche zu besitzen).
 * @author Alexander Herzog
 */
public interface InteractiveElement {
	/* Das Interface stellt lediglich einen Marker dar und enthält keine Methoden. */
}
