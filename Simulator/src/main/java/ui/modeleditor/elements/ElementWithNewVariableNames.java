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
 * Modell-Elemente, die dieses Interface implementieren, setzen evtl. Variablen,
 * deren Namen über die Methode in diesem Interface abgefragt werden können.
 * @author Alexander Herzog
 */
public interface ElementWithNewVariableNames {
	/**
	 * Listet alle Variablennamen auf
	 * @return	Liste aller Variablennamen
	 */
	String[] getVariables();
}
