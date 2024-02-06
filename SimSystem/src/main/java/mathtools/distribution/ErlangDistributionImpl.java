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
package mathtools.distribution;

import java.io.Serializable;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

/**
 * Die Klasse <code>ErlangDistributionImpl</code> stellt eine Erweiterung der Klasse
 * <code>GammaDistribution</code> dar. Sie enthält keine neuen Funktionen, sondern
 * dient nur zur internen Unterscheidung der beiden Verteilungen.
 * @author Alexander Herzog
 * @version 1.0
 * @see GammaDistribution
 */
public class ErlangDistributionImpl extends GammaDistribution {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7370072894248279874L;

	/**
	 * Konstruktor der Klasse <code>ErlangDistributionImpl</code>
	 * @param shape	Parameter n der Erlang-Verteilung
	 * @param scale	Parameter lambda der Erlang-Verteilung
	 * @throws NotStrictlyPositiveException	Wird ausgelöst, wenn der <code>shape</code>- oder der <code>scale</code>-Parameter keinen positiven Wert enthält
	 */
	public ErlangDistributionImpl(double shape, double scale) throws NotStrictlyPositiveException {
		super(null,shape,Math.max(scale,0.00001),GammaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
	}
}
