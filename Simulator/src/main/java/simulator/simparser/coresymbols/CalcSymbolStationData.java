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
package simulator.simparser.coresymbols;

import parser.MathCalcError;
import simulator.coreelements.RunElement;
import simulator.coreelements.RunElementData;
import simulator.elements.RunElementAssign;
import simulator.elements.RunElementSource;
import simulator.runmodel.SimulationData;

/**
 * Basisklasse f�r Funktionen, die Stationsdaten aus den Simulationsdaten auslesen.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolStationData extends CalcSymbolSimData {
	/**
	 * Berechnung der Daten auf Basis des Laufzeit-Datenobjekts
	 * @param data	Laufzeit-Datenobjekt
	 * @return	Berechneter Wert
	 */
	protected abstract double calc(final RunElementData data);

	/**
	 * Kann der Wert �ber alle Stationen hinweg berechnet werden?
	 * @return	Wird hier <code>true</code> zur�ckgegeben, so muss {@link #calcAll()} implementiert werden.
	 */
	protected boolean hasAllData() {
		return false;
	}

	/**
	 * K�nnen Daten f�r einen einzelnen Kundentyp (identifiziert �ber eine Quelle- oder Zuweisungsstation) berechnet werden?
	 * @return	Wird hier <code>true</code> zur�ckgegeben, so muss {@link #calcSingleClient(String)} implementiert werden.
	 */
	protected boolean hasSingleClientData() {
		return false;
	}

	/**
	 * Berechnet die Daten �ber alle Stationen hinweg
	 * @return	Wert berechnet �ber alle Stationen hinweg
	 * @see #hasAllData()
	 */
	protected double calcAll() {
		return 0.0;
	}

	/**
	 * Berechnet den Wert f�r einen Kundentyp
	 * @param name	Kundentyp f�r den der Wert berechnet werden soll
	 * @return	Wert berechnet f�r den angegebenen Kundentyp
	 */
	protected double calcSingleClient(final String name) {
		return 0.0;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (getSimData()==null) throw error();

		/* Kundentyp? */
		if (parameters.length==0 && hasAllData()) return calcAll();
		if (parameters.length!=1) throw error();

		if (hasSingleClientData()) {
			final RunElement element=getRunElementForID(parameters[0]);
			if (element==null) throw error();

			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return calcSingleClient(name);
			/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
		}

		/* Station? */
		final RunElementData data=getRunElementDataForID(parameters[0]);
		if (data==null) return 0.0;
		return calc(data);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (getSimData()==null) return fallbackValue;

		/* Kundentyp? */
		if (parameters.length==0 && hasAllData()) return calcAll();
		if (parameters.length==1 && hasSingleClientData()) {
			final RunElement element=getRunElementForID(parameters[0]);
			if (element==null) return fallbackValue;

			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return calcSingleClient(name);
			/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
		}

		/* Station? */
		if (parameters.length!=1) return fallbackValue;
		RunElementData data=getRunElementDataForID(parameters[0]);
		if (data==null) return 0;
		return calc(data);
	}
}
