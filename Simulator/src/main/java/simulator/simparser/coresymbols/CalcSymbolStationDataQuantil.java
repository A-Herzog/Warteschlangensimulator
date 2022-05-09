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
 * Basisklasse für Funktionen, die Quantilswerte auf Basis von Simulationsdaten ausgeben.
 * @author Alexander Herzog
 * @see SimulationData
 * @see CalcSymbolSimData
 */
public abstract class CalcSymbolStationDataQuantil extends CalcSymbolSimData {
	/**
	 * Berechnet das Quantil zum Level <code>p</code> für eine Station
	 * @param p	Quantilslevel
	 * @param data	Daten zu der Station
	 * @return	Quantil
	 */
	protected abstract double calc(final double p, final RunElementData data);

	/**
	 * Konstruktor der Klasse
	 */
	public CalcSymbolStationDataQuantil() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Stehen Daten über alle Kundentypen zur Verfügung?
	 * @return	Wird <code>true</code> zurückgegeben, so muss {@link #calcAll(double)} implementiert werden.
	 */
	protected boolean hasAllData() {
		return false;
	}

	/**
	 * Stehen Daten für einzelne Kundentypen zur Verfügung?
	 * @return	Wird <code>true</code> zurückgegeben, so muss {@link #calcSingleClient(double, String)} implementiert werden.
	 */
	protected boolean hasSingleClientData() {
		return false;
	}

	/**
	 * Berechnet das Quantil zum Level <code>p</code> über alle Kundentypen.
	 * @param p	Quantilslevel
	 * @return	Quantil
	 * @see #hasAllData()
	 */
	protected double calcAll(final double p) {
		return 0.0;
	}

	/**
	 * Berechnet das Quantil zum Level <code>p</code> für einen bestimmten Kundentyp
	 * @param p	Quantilslevel
	 * @param name	Name des Kundentyps
	 * @return	Quantil
	 * @see #hasSingleClientData()
	 */
	protected double calcSingleClient(final double p, final String name) {
		return 0.0;
	}

	@Override
	protected double calc(double[] parameters) throws MathCalcError {
		if (getSimData()==null) throw error();

		/* Wert p für Quantil */
		if (parameters.length<1) throw error();
		double p=parameters[0];
		if (p<0) p=0;
		if (p>1) p=1;

		/* Kundentyp? */
		if (parameters.length==1 && hasAllData()) return calcAll(p);
		if (parameters.length!=2) throw error();

		if (hasSingleClientData()) {
			final RunElement element=getRunElementForID(parameters[1]);
			if (element==null) throw error();

			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return calcSingleClient(p,name);
			/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
		}

		/* Station? */
		final RunElementData data=getRunElementDataForID(parameters[1]);
		if (data==null) return 0.0;
		return calc(p,data);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (getSimData()==null) return fallbackValue;

		/* Wert p für Quantil */
		if (parameters.length<1) return fallbackValue;
		double p=parameters[0];
		if (p<0) p=0;
		if (p>1) p=1;

		/* Kundentyp? */
		if (parameters.length==1 && hasAllData()) return calcAll(p);
		if (parameters.length==2 && hasSingleClientData()) {
			final RunElement element=getRunElementForID(parameters[1]);
			if (element==null) return fallbackValue;

			String name=null;
			if (element instanceof RunElementSource) name=((RunElementSource)element).clientTypeName;
			if (element instanceof RunElementAssign) name=((RunElementAssign)element).clientTypeName;
			if (name!=null) return calcSingleClient(p,name);
			/* name==null: Evtl. nicht pro Kundentyp sondern pro Station */
		}

		/* Station? */
		if (parameters.length!=2) return fallbackValue;
		RunElementData data=getRunElementDataForID(parameters[1]);
		if (data==null) return 0;
		return calc(p,data);
	}
}
