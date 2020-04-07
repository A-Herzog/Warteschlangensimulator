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

/**
 * Von der Klasse {@link ModelElementBox} abgeleitete Stationen, die dieses leere
 * Interface implementieren werden bei der Animation als Schranken für die gleichzeitige
 * Bewegung von Kunden angesehen. Trifft ein Kunde an einer solchen Station ein, so werden
 * alle bis dahin angesammelten gleichzeitigen Bewegungen ausgeführt. Stationen, an denen
 * eine Verzögerung auftreten kann, sollten dieses Interface implementieren, damit
 * Animationen im Einzelschrittmodus korrekt dargestellt werden.
 * @author Alexander Herzog
 *
 */
public interface ModelElementAnimationForceMove {
}
