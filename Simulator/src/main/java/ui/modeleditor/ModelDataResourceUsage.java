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
package ui.modeleditor;

import java.util.Map;

/**
 * Elemente, die dieses Interface implementieren, können Ressourcen belegen und geben über
 * die Methode in diesem Interface Auskunft über die Ressourcentypen, die sie nutzen.
 * @author Alexander Herzog
 */
public interface ModelDataResourceUsage {
	/**
	 * Liefert eine Aufstellung, wie viele Bediener welchen Typs genutzt werden
	 * @return	Zuordnung von Ressourcennamen und Anzahl an (belegten) Bedienern
	 */
	Map<String,Integer> getUsedResourcesInfo();

	/**
	 * Stellt ein, dass eine angegebene Ressource in der angegebenen Vielfachheit
	 * benötigt wird. Ob die Ressource dabei zu einer Liste hinzugefügt wird oder
	 * eine neue Alternative zur Ressourcennutzung angelegt wird, entscheidet
	 * dabei die konkrete Station.
	 * @param resourceName	Name der Ressource
	 * @param neededNumber	Vielfachheit mit der die Ressource benötigt werden soll
	 */
	void addResourceUsage(final String resourceName, final int neededNumber);
}
