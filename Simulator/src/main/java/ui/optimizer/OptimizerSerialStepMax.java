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

import language.Language;

/**
 * Führt eine Optimierung durch, in der bei allen Kontrollvariablen der
 * Reihe nach geprüft wird, ob eine Veränderung in die eine oder andere Richtung
 * zu einer Verbesserung führt. Pro Schritt wird jede Kontrollvariable dabei
 * jeweils so weit bewegt, wie dies sinnvoll ist.
 * @author Alexander Herzog
 * @see OptimizerSerialKernelStepMax
 */
public final class OptimizerSerialStepMax extends OptimizerSerialBase implements Cloneable {
	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (kann <code>null</code> sein, wenn kein solches vorhanden ist)
	 */
	public OptimizerSerialStepMax(final Component owner) {
		super(owner);
	}

	@Override
	protected OptimizerSerialKernelBase getOptimizerKernel() {
		return new OptimizerSerialKernelStepMax(setup,model);
	}

	@Override
	public String getName() {
		return Language.tr("Optimizer.Tab.Optimization.Kernel.Simple");
	}

	@Override
	protected String[] getNames() {
		return Language.trAll("Optimizer.Tab.Optimization.Kernel.Simple");
	}

	@Override
	public OptimizerBase clone() {
		return new OptimizerSerialStepMax(owner);
	}
}
