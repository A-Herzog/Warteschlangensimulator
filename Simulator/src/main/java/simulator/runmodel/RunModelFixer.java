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
package simulator.runmodel;

import java.util.function.Consumer;

import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import ui.modeleditor.coreelements.ModelElementPosition;

/**
 * Korrekturvorschlagfunktion für einen bestimmten Fehler bei einem bestimmten Element.
 * @author Alexander Herzog
 * @see RunModelCreatorStatus#getFix(ModelElementPosition)
 */
public final class RunModelFixer {
	/**
	 * Element auf das sich die Korrektur bezieht
	 */
	public final ModelElementPosition element;

	/**
	 * Modell in dem das Element enthalten ist
	 */
	public final EditModel model;

	/**
	 * Fehlerstatus für das Element. Enthält insbesondere die Fehlermeldung
	 */
	public final RunModelCreatorStatus status;

	/**
	 * Beschreibung des Korrekturvorschlags
	 */
	public final String info;

	/**
	 * Funktion, die die eigentliche Korrektur durchführt
	 */
	private final Consumer<RunModelFixer> process;

	/**
	 * Konstruktor der Klasse
	 * @param element	Element auf das sich die Korrektur bezieht
	 * @param status	Fehlerstatus für das Element
	 * @param info	Beschreibung des Korrekturvorschlags
	 * @param process	Funktion, die die eigentliche Korrektur durchführt
	 */
	public RunModelFixer(final ModelElementPosition element, final RunModelCreatorStatus status, final String info, final Consumer<RunModelFixer> process) {
		this.element=element;
		model=element.getModel();
		this.status=status;
		this.info=info;
		this.process=process;
	}

	/**
	 * Führt die Funktion, die die eigentliche Korrektur durchführt, aus.
	 */
	public void process() {
		process.accept(this);
	}
}