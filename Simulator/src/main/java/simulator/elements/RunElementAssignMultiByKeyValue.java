/**
 * Copyright 2025 Alexander Herzog
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

import java.util.List;

import language.Language;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import ui.modeleditor.elements.DecideRecord;
import ui.modeleditor.elements.ModelElementAssignMulti;

/**
 * Äquivalent zu {@link ModelElementAssignMulti} (im Modus <code>MODE_KEY_VALUE</code>)
 * @author Alexander Herzog
 * @see ModelElementAssignMulti
 */
public class RunElementAssignMultiByKeyValue extends RunElementAssignMultiBase {
	/** Für die Wahl des neuen Kundentyps auszuwertender Kundentextdaten-Schlüssel */
	private String key;
	/** Werte für {@link #key} die zu der Wahl eines jeweils neuen Kundenkeyps führen */
	private String[][] values;

	/**
	 * Konstruktor der Klasse
	 * @param element Mehrfach-Typzuweisungs-Station zu diesem Datenelement
	 */
	public RunElementAssignMultiByKeyValue(ModelElementAssignMulti element) {
		super(element,DecideRecord.DecideMode.MODE_KEY_VALUE);
	}


	@Override
	protected String buildDecideData(EditModel editModel, RunModel runModel, ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		/* Schlüssel */
		if (record.getKey().isBlank()) return String.format(Language.tr("Simulation.Creator.NoKey"),element.getId());
		key=record.getKey();

		/* Mehrere Werte pro Wert-Eintrag? */
		final boolean multiTextValues=record.isMultiTextValues();

		/* Werte */
		this.values=new String[Math.max(0,decideCount-1)][];
		final List<String> values=record.getValues();
		for (int i=0;i<decideCount-1;i++) {
			final String value=(i>=values.size())?"":values.get(i);
			if (multiTextValues) {
				final String[] v=value.split(";");
				if (v.length==0) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1);
				for (int j=0;j<v.length;j++) v[j]=v[j].trim();
				for (String s: v) if (s.isEmpty()) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1);
				this.values[i]=v;
			} else {
				if (value.isBlank()) return String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1);
				this.values[i]=new String[]{value};
			}
		}

		return null;
	}

	@Override
	protected RunModelCreatorStatus testDecideData(ModelElementAssignMulti element, DecideRecord record, int decideCount) {
		/* Schlüssel */
		if (record.getKey().isBlank()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoKey"),element.getId()));

		/* Mehrere Werte pro Wert-Eintrag? */
		final boolean multiTextValues=record.isMultiTextValues();

		/* Werte */
		final List<String> values=record.getValues();
		for (int i=0;i<decideCount-1;i++) {
			final String value=(i>=values.size())?"":values.get(i);
			if (multiTextValues) {
				final String[] v=value.split(";");
				if (v.length==0) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1));
				for (String s: v) if (s.isBlank()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1));
			} else {
				if (value.isBlank()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.NoValue"),element.getId(),i+1));
			}
		}

		return RunModelCreatorStatus.ok;
	}

	@Override
	protected int getOptionIndex(SimulationData simData, RunDataClient client, RunElementAssignMultiBaseData data, int decideCount) {
		final String value=client.getUserDataString(key);
		int nr=-1;
		for (int i=0;i<values.length;i++) {
			final String[] v=values[i];
			for (int j=0;j<v.length;j++) if (value.equals(v[j])) {nr=i; break;}
			if (nr>=0) break;
		}
		if (nr<0) nr=decideCount-1; /* Else */

		return nr;
	}
}
