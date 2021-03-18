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

import org.apache.commons.math3.random.RandomGenerator;

import mathtools.distribution.tools.DistributionRandomNumber;

/**
 * Verteilungsklassen, die dieses Interface implementieren,
 * besitzen eine Methode, um Zufallszahlen gem‰ﬂ der
 * Verteilung zu generieren.
 * @author Alexander Herzog
 * @see DistributionRandomNumber
 */
public interface DistributionWithRandom {
	/**
	 * Erzeugt eine Zufallszahl gem‰ﬂ der Verteilung
	 * @param generator	Generator f¸r auf [0;1] gleichverteilte Zufallszahlen
	 * @return	Zufallszahl gem‰ﬂ der Verteilung
	 */
	double random(final RandomGenerator generator);
}
