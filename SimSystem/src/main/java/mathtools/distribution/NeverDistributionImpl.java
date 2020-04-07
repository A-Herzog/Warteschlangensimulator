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

/**
 * Die Klasse <code>NeverDistributionImpl</code> leitet sich von der Klasse <code>OnePointDistributionImpl</code>
 * ab und legt das gesamte Gewicht auf einen unendlich weit entfernten Punkt.<br>
 * Die Verteilung ist z.B. sinnvoll um Funktionen in einem Simulationsprozess zu deaktivieren
 * (wie z.B. eine Wartezeittoleranz).
 * @author Alexander Herzog
 * @version 1.0
 * @see OnePointDistributionImpl
 */
public final class NeverDistributionImpl extends OnePointDistributionImpl {
	private static final long serialVersionUID = 5707889172959565102L;

	/**
	 * Zeitpunkt, der als "nie" definiert werden soll.
	 */
	public static final double NEVER=1E18;

	/**
	 * Konstruktor der <code>NeverDistributionImpl</code>-Verteilung
	 */
	public NeverDistributionImpl() {
		super(NEVER);
	}

	@Override
	public NeverDistributionImpl clone() {return new NeverDistributionImpl();}
}
