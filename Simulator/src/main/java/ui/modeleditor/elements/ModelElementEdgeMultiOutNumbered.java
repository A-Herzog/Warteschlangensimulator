/**
 * Copyright 2022 Alexander Herzog
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

import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;

/**
 * Dieses Interface signalisiert, dass das Element mehrere auslaufende Kanten besitzen kann
 * und die Reihenfolge der auslaufenden Kanten eine Rolle spielt.
 * @author Alexander Herzog
 * @see ModelElementEdge#isConnectionOk()
 */
public interface ModelElementEdgeMultiOutNumbered extends ModelElementEdgeMultiOut {
	/**
	 * Fügt eine auslaufende Kante an einer bestimmten Stelle in der Liste der auslaufenden Kanten hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @param index	Index der neuen Kante in der Liste der Kanten
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	boolean addEdgeOut(ModelElementEdge edge, int index);
}
