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

import parser.MathCalcError;
import simulator.runmodel.SimulationData;
import simulator.simparser.coresymbols.CalcSymbolSimData;

/**
 * Im Falle von einem Parameter:<br>
 * Liefert die Anzahl der momentan in Ausfallzeit befindlichen Transporter in der angegebenen Transportergruppe.<br>
 * Im Falle von keinem Parameter:<br>
 * Liefert die Anzahl der momentan in Ausfallzeit befindlichen Transporter in allen Transportergruppen zusammen.
 * @author Alexander Herzog
 */
public class CalcSymbolTransporterDown extends CalcSymbolSimData {
	/**
	 * Namen f�r das Symbol
	 * @see #getNames()
	 */
	private static final String[] names=new String[]{"transporter_down"};

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolTransporterDown() {
		/*
		 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
		 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	@Override
	public String[] getNames() {
		return names;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		final SimulationData simData=getSimData();
		if (simData==null) throw error();

		if (parameters.length==0) {
			return simData.runData.transporters.getAllDown(simData);
		}

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			return simData.runData.transporters.getDown(id,simData);
		}

		throw error();
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		final SimulationData simData=getSimData();
		if (simData==null) return fallbackValue;

		if (parameters.length==0) {
			return simData.runData.transporters.getAllDown(simData);
		}

		if (parameters.length==1) {
			final int id=(int)FastMath.round(parameters[0])-1;
			return simData.runData.transporters.getDown(id,simData);
		}

		return fallbackValue;
	}
}
