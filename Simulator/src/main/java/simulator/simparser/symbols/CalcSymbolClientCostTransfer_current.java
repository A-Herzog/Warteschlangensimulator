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

import simulator.runmodel.RunDataClient;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Liefert die Transferzeitkosten des aktuellen Kunden.
 * @author Alexander Herzog
 */
public class CalcSymbolClientCostTransfer_current extends CalcSymbolSimData {

	@Override
	public String[] getNames() {
		return new String[]{"Costs_Transfer","Kosten_Transferzeit"};
	}

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length!=0) return null;
		final RunDataClient client=getCurrentClient();
		if (client==null) return null;

		return fastBoxedValue((client.transferTime/1000.0)*getSimData().runModel.clientCosts[client.type][1]+client.transferAdditionalCosts);
	}
}
