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
package simulator.elements;

import java.util.List;

import simulator.runmodel.RunDataClient;

/**
 * Datenelemente, die dieses Interface implementieren stelle eine Liste mit an der zugeh�rigen Station wartenden
 * Kunden f�r andere Klassen (lesend) zur Verf�gung.
 * @author Alexander Herzog
 * @see ui.modeleditor.coreelements.ModelElementAnimationInfoDialog.ClientInfo
 */
public interface RunElementDataWithWaitingClients {
	/**
	 * Liefert die Liste der wartenden Kunden (f�r Lesezugriffe).
	 * @return	Liste der wartenden Kunden
	 */
	List<RunDataClient> getWaitingClients();
}
