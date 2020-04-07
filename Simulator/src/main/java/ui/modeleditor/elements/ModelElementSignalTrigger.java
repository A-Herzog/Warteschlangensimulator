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

/**
 * Element, die Signale ausl�sen k�nnen.
 * @author Alexander Herzog
 */
public interface ModelElementSignalTrigger {
	/**
	 * Liefert die Liste der Namen der m�glichen Signale.
	 * @return	Liste der Namen der m�glichen Signale (kann leer sein und Eintr�ge k�nnen "" sein, aber sollte nicht als ganzes <code>null</code> sein)
	 */
	String[] getSignalNames();
}
