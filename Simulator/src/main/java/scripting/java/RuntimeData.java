/**
 * Copyright 2021 Alexander Herzog
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
package scripting.java;

import java.util.HashMap;
import java.util.Map;

import simulator.runmodel.SimulationData;

/**
 * Klasse zur Speicherung von stationsbezogenen und modellweiten Skript-Daten
 * @author Alexander Herzog
 * @see SimulationData#runtimeData
 * @see RuntimeImpl#mapGlobal
 * @see RuntimeImpl#mapLocal
 */
public class RuntimeData extends ThreadLocal<Map<String,Object>> {
	@Override
	protected Map<String,Object> initialValue() {
		return new HashMap<>();
	}
}
