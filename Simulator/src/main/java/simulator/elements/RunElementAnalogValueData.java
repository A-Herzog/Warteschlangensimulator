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
package simulator.elements;

import org.apache.commons.math3.util.FastMath;

import simulator.coreelements.RunElementAnalogProcessing;
import simulator.coreelements.RunElementAnalogProcessingData;
import simulator.runmodel.SimulationData;
import statistics.StatisticsTimeAnalogPerformanceIndicator;

/**
 * Laufzeitdaten eines <code>RunElementAnalogValue</code>-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementAnalogValue
 * @see RunElementAnalogProcessingData
 */
public class RunElementAnalogValueData extends RunElementAnalogProcessingData {
	/**
	 * Anfängliche Rate (bezogen auf eine MS)
	 */
	private final double initialRateMS;

	/**
	 * Minimalwert
	 */
	private final double valueMin;

	/**
	 * Minimalwert verwenden?
	 */
	private final boolean valueMinUse;

	/**
	 * Maximalwert
	 */
	private final double valueMax;

	/**
	 * Maximalwert verwenden?
	 */
	private final boolean valueMaxUse;

	/**
	 * Aktuelle Änderungsrate (pro MS) des Analog-Zählers
	 */
	private double rateMS;

	/**
	 * Konstruktor der Klasse
	 * @param station	Zugehöriges RunElement
	 * @param value	Initialer Wert des Analog-Zählers
	 * @param rate	Initiale Änderungsrate (pro Sekunde) des Analog-Zählers
	 * @param valueMin	Minimum, das der Wert annehmen darf
	 * @param valueMinUse	Minimum verwenden (<code>true</code>) oder ignorieren (<code>false</code>)
	 * @param valueMax	Maximum, das der Wert annehmen darf
	 * @param valueMaxUse	Maximum verwenden (<code>true</code>) oder ignorieren (<code>false</code>)
	 * @param statistics	Statistikobjekt, in dem die Größe des analogen Wertes erfasst wird
	 * @param simData	Simulationsdatenobjekt
	 */
	public RunElementAnalogValueData(final RunElementAnalogProcessing station, final double value, final double rate, final double valueMin, final boolean valueMinUse, final double valueMax, final boolean valueMaxUse, final StatisticsTimeAnalogPerformanceIndicator statistics, final SimulationData simData) {
		super(station,statistics,simData);
		/* this.value=value; - nicht nötig, das übernimmt schon der super-Konstruktor */
		initialRateMS=rate*simData.runModel.scaleToSeconds;
		this.valueMin=valueMin;
		this.valueMinUse=valueMinUse;
		this.valueMax=valueMax;
		this.valueMaxUse=valueMaxUse;
	}

	@Override
	protected void init(final SimulationData simData) {
		rateMS=initialRateMS;
	}

	@Override
	protected void updateValue(final SimulationData simData) {
		final long time=simData.currentTime;

		if (rateMS>0) {
			/* Addition */
			if (valueMaxUse) {
				/* mit Grenze */
				if (Math.abs(value-valueMax)<10E-10) {
					/* Maximum war schon vorher erreicht */
				} else {
					double newValue=value+rateMS*(time-valueTime);
					if (newValue<=valueMax) {
						/* Grenze noch nicht erreicht */
						value=newValue;
					} else {
						/* Linearer Anstieg bis Grenze, danach konstant */
						final long timeMax=FastMath.round(valueTime+(valueMax-value)/rateMS);
						setStatisticValue(timeMax,value);
						value=valueMax;
					}
				}
			} else {
				/* ohne Grenze */
				value+=rateMS*(time-valueTime);
			}
		} else {
			/* Subtraktion */
			if (valueMinUse) {
				/* mit Grenze */
				if (Math.abs(value-valueMin)<10E-10) {
					/* Minimum war schon vorher erreicht */
				} else {
					double newValue=value+rateMS*(time-valueTime);
					if (newValue>=valueMin) {
						/* Grenze noch nicht erreicht */
						value=newValue;
					} else {
						/* Linearer Abstieg bis Grenze, danach konstant */
						final long timeMin=FastMath.round(valueTime+(value-valueMin)/rateMS);
						setStatisticValue(timeMin,value);
						value=valueMin;
					}
				}
			} else {
				/* ohne Grenze */
				value+=rateMS*(time-valueTime);
			}
		}
	}

	/**
	 * Liefert die aktuelle Änderungsrate
	 * @param simData	Simulationsdatenobjekt
	 * @return	Aktuelle Änderungsrate
	 */
	public double getRate(final SimulationData simData) {
		checkInit(simData);

		return rateMS*simData.runModel.scaleToSimTime; /* Rate ist bereits in MS, daher Multiplikation mit 1000, also scaleToSimTime */
	}

	/**
	 * Liefert die aktuelle Änderungsrate, ohne dabei im Hintergrund nötigenfalls einen Update-Schritt durchzuführen
	 * @param simData	Simulationsdatenobjekt
	 * @return	Aktuelle Änderungsrate
	 * @see RunElementAnalogValueData#getRate(SimulationData)
	 */
	public double getRateNoUpdate(final SimulationData simData) {
		return rateMS*simData.runModel.scaleToSimTime; /* Rate ist bereits in MS, daher Multiplikation mit 1000, also scaleToSimTime */
	}

	@Override
	public void setValue(final SimulationData simData, double value) {
		if (valueMinUse && value<valueMin) value=valueMin;
		if (valueMaxUse && value>valueMax) value=valueMax;
		super.setValue(simData,value);
	}

	/**
	 * Stellt die aktuelle Änderungsrate ein.
	 * @param simData	Simulationsdatenobjekt
	 * @param rate	Neue Änderungsrate
	 */
	public void setRate(final SimulationData simData, final double rate) {
		getValue(simData);
		rateMS=rate*simData.runModel.scaleToSeconds; /* Rate pro Sek / 1000 = Rate pro MS */
		if (rateMS!=0.0) rateActivated(simData);
	}

	@Override
	protected boolean isRateZero() {
		return rateMS==0.0;
	}
}