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
 * Führt eine Optimierungmit Hilfe von genetischen Algorithmen durch.
 * @author Alexander Herzog
 * @see OptimizerParallelKernelGenetic
 */
public final class OptimizerParallelGenetic extends OptimizerParallelBase implements Cloneable {
	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (kann <code>null</code> sein, wenn kein solches vorhanden ist)
	 */
	public OptimizerParallelGenetic(final Component owner) {
		super(owner);
	}

	@Override
	protected OptimizerParallelKernelBase getOptimizerKernel() {
		return new OptimizerParallelKernelGenetic(setup,this,model,editModelPath);
	}

	@Override
	public String getName() {
		return Language.tr("Optimizer.Tab.Optimization.Kernel.Genetic");
	}

	@Override
	protected String[] getNames() {
		return Language.trAll("Optimizer.Tab.Optimization.Kernel.Genetic");
	}

	@Override
	public OptimizerBase clone() {
		return new OptimizerParallelGenetic(owner);
	}
}