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
package ui.optimizer;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse hält eine Liste mit allen verfügbaren Optimierern vor
 * @author Alexander Herzog
 */
public class OptimizerCatalog {
	/** Liste der verfügbaren Optimierer */
	private final List<OptimizerBase> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (kann <code>null</code> sein, wenn kein solches vorhanden ist)
	 */
	public OptimizerCatalog(final Component owner) {
		list=new ArrayList<>();

		list.add(new OptimizerSerialStepWise(owner)); /* Default */
		list.add(new OptimizerSerialStepMax(owner));
		list.add(new OptimizerParallelGenetic(owner));
	}

	/**
	 * Listet die Namen aller verfügbaren Optimiern auf
	 * @return	Liste aller verfügbaren Optimierer
	 */
	public String[] getOptimizerNames() {
		final List<String> names=new ArrayList<>();
		for (OptimizerBase optimizer: list) names.add(optimizer.getName());
		return names.toArray(String[]::new);
	}

	/**
	 * Liefert den Namen des Standard-Optimierers
	 * @return	Name des Standard-Optimierers
	 */
	public String getDetaultOptimizerName() {
		return list.get(0).getName();
	}

	/**
	 * Liefert den jeweils übersetzen Namen eines Optimierers (oder den Namen des Standardoptimierers, wenn der Name zu nichts passt)
	 * @param name	Name eines Optimierers in einer beliebigen Übersetzung
	 * @return	Name des Optimierers in der aktuellen Sprache
	 */
	public String getCanonicalOptimizerName(final String name) {
		return getOptimizer(name).getName();
	}

	/**
	 * Findet einen Optimierer zu einem Namen. Falls es keinen passenden Optimierer gibt, wird der Standardoptimierer geliefert.<br>
	 * Es wird jeweils eine Kopie des Optimierer-Objektes aus dem Katalog geliefert mit dem beliebig gearbeitet werden kann.
	 * @param name	Name des gesuchten Optimierers
	 * @return	Optimierer, der zu dem Namen passt
	 */
	public OptimizerBase getOptimizer(final String name) {
		for (OptimizerBase optimizer: list) if (optimizer.matchName(name)) return optimizer.clone();

		return list.get(0).clone();
	}
}