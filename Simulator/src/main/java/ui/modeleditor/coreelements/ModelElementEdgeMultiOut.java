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
package ui.modeleditor.coreelements;

import ui.modeleditor.elements.ModelElementEdge;

/**
 * Dieses Interface signalisiert, dass das Element mehrere auslaufende Kanten besitzen kann
 * @author Alexander Herzog
 * @see ModelElementEdge#isConnectionOk()
 */
public interface ModelElementEdgeMultiOut {
	/**
	 * Auslaufende Kanten
	 * @return	Auslaufende Kanten
	 */
	ModelElementEdge[] getEdgesOut();
}
