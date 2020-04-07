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
package mathtools;

import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

/**
 * Enthält einige Hilfsfunktionen
 * @author Alexander Herzog
 * @version 1.0
 */
public final class Functions {
	/**
	 * Diese Klasse kann nicht instanziert werden.
	 */
	private Functions() {
	}

	/**
	 * Berechnet Gamma(x,z), d.h. den Wert der unvollständigen Gamma-Funktion bei Integration von z bis unendlich
	 * @param x	Parameter der Gammafunktion
	 * @param z	Untere Integrationsgrenze für die unvollständige Gammafunktion (z=0 liefert die vollständige Gammafunktion)
	 * @return	Gamma(x,z)
	 */
	public static double getIncompleteGamma(double x, double z) {
		return Gamma.regularizedGammaQ(x,z)*getGamma(x);
	}

	/**
	 * Berechnet Gamma(x)
	 * @param x	Parameter der (vollständigen) Gammafunktion
	 * @return	Gamma(x)
	 */
	public static double getGamma(double x) {
		return FastMath.exp(Gamma.logGamma(x));
	}

	/**
	 * Berechnet n!
	 * @param n	Wert, von dem die Fakultät berechnet werden soll
	 * @return	n!
	 */
	public static double getFactorial(int n) {
		return getGamma(n+1);
	}
}
