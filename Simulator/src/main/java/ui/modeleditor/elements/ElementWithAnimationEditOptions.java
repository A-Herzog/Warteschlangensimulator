/**
 * Copyright 2023 Alexander Herzog
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

import ui.AnimationPanel;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Bei Elementen, die dieses Interface implementieren, wird am Ende
 * der Animation geprüft, ob diese eine Semi-Editable-Funktion
 * bieten und wenn ja, werden die (möglicherweise während der
 * Animation veränderten) Einstellungen in das Editor-Modell zurück
 * übertragen.
 * @author Alexander Herzog
 * @see AnimationPanel#updateEditorModel(ModelSurface, ModelSurface)
 * @see ModelElement#getPropertiesSemiEditable(java.awt.Component, ui.modeleditor.ModelClientData, ui.modeleditor.ModelSequences)
 */
public interface ElementWithAnimationEditOptions {
}
