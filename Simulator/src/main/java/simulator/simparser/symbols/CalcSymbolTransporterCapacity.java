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
package simulator.simparser.symbols;

import org.apache.commons.math3.util.FastMath;

import simulator.runmodel.SimulationData;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Im Falle von einem Parameter:<br>
 * Liefert die Anzahl der momentan vorhandenen Bediener in der angegebenen Ressource.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die Anzahl der momentan vorhandenen Bedienern in allen Ressourcen zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolTransporterCapacity extends CalcSymbolSimData {
	@Override
	public String[] getNames() {
		return new String[]{"resource_capacity"};
	}

	@Override
	protected Double calc(double[] parameters) {
		final SimulationData simData=getSimData();
		if (simData==null) return null;

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			return fastBoxedValue(simData.runData.transporters.getTransporterCapacity(id));
		}

		return null;
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final SimulationData simData=getSimData();
		if (simData==null) return fallbackValue;

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			return simData.runData.transporters.getTransporterCapacity(id);
		}

		return fallbackValue;
	}
}
