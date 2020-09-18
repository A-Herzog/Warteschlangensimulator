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
package ui.parameterseries;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import xml.XMLData;

/**
 * Diese Klasse hält die Einstellungen für die
 * Parameter-Vergleichs-Funktion vor.
 * @author Alexander Herzog
 */
public final class ParameterCompareSetup extends XMLData implements Cloneable {
	/** Editor-Modell, welches die Basis für die Parameterreihe darstellen soll (kann <code>null</code> sein, dann muss ein Parameterreihen-Setup geladen werden) */
	private EditModel editModel;
	/** Liste der zu simulierenden Modelle */
	private final List<ParameterCompareSetupModel> models;
	/** Liste der Eingabeparameter-Einstellungen */
	private final List<ParameterCompareSetupValueInput> input;
	/** Liste der Ausgabeparameter-Einstellungen */
	private final List<ParameterCompareSetupValueOutput> output;

	/**
	 * Konstruktor der Klasse
	 * @param editModel	Editor-Modell, welches die Basis für die Parameterreihe darstellen soll (kann <code>null</code> sein, dann muss ein Parameterreihen-Setup geladen werden)
	 */
	public ParameterCompareSetup(final EditModel editModel) {
		this.editModel=(editModel==null)?new EditModel():editModel.clone();
		models=new ArrayList<>();
		input=new ArrayList<>();
		output=new ArrayList<>();
	}

	/**
	 * Liefert das Basismodell für die Parameterreihe.
	 * @return	Basismodell für die Parameterreihe
	 */
	public EditModel getEditModel() {
		return editModel;
	}

	/**
	 * Stellt das Basismodell für die Parameterreihe ein.
	 * @param editModel	Basismodell für die Parameterreihe
	 */
	public void setEditModel(final EditModel editModel) {
		if (editModel!=null) this.editModel=editModel;
	}

	/**
	 * Liste der zu simulierenden Modelle
	 * @return	Zu simulierenden Modelle
	 */
	public List<ParameterCompareSetupModel> getModels() {
		return models;
	}

	/**
	 * Liste der Eingabeparameter-Einstellungen
	 * @return	Eingabeparameter-Einstellungen
	 */
	public List<ParameterCompareSetupValueInput> getInput() {
		return input;
	}

	/**
	 * Liste der Ausgabeparameter-Einstellungen
	 * @return	Ausgabeparameter-Einstellungen
	 */
	public List<ParameterCompareSetupValueOutput> getOutput() {
		return output;
	}

	/**
	 * Löscht alle in allen Modellen gespeicherten Statistik-Ergebnisse
	 */
	public void clearAllOutputs() {
		for (ParameterCompareSetupModel model: models) model.clearOutputs();
	}

	/**
	 * Trägt ggf. Vorgabewerte in neue Input-Parameter bei den Modellen ein
	 */
	public void updateInputValuesInModels() {
		for (ParameterCompareSetupModel model: models) model.updateInputValuesInModel(editModel,this);
	}

	/**
	 * Vergleich die Einstellungen mit einem anderen Einstellungen-Objekt
	 * @param otherSetup	Anderes Einstellungen-Objekt
	 * @return	Liefert <code>true</code>, wenn die beiden Objekte inhaltlich identisch sind
	 */
	public boolean equalsParameterCompareSetup(final ParameterCompareSetup otherSetup) {
		if (!editModel.equalsEditModel(otherSetup.editModel)) return false;

		if (models.size()!=otherSetup.models.size()) return false;
		for (int i=0;i<models.size();i++) if (!models.get(i).equalsParameterCompareSetupRecord(otherSetup.models.get(i))) return false;

		if (input.size()!=otherSetup.input.size()) return false;
		for (int i=0;i<input.size();i++) if (!input.get(i).equalsParameterCompareSetupValueInput(otherSetup.input.get(i))) return false;

		if (output.size()!=otherSetup.output.size()) return false;
		for (int i=0;i<output.size();i++) if (!output.get(i).equalsParameterCompareSetupValueOutput(otherSetup.output.get(i))) return false;

		return true;
	}

	@Override
	public ParameterCompareSetup clone() {
		final ParameterCompareSetup clone=new ParameterCompareSetup(null);
		clone.copyFrom(this);
		return clone;
	}

	/**
	 * Löst alle Einstellungen des Einstellungen-Objektes
	 */
	public void clear() {
		models.clear();
		input.clear();
		output.clear();
	}

	/**
	 * Kopiert die Einstellungen aus einem anderen Objekt in dieses
	 * @param otherSetup	Anderes Einstellungen-Objekt aus dem die Daten übernommen werden sollen
	 */
	public void copyFrom(final ParameterCompareSetup otherSetup) {
		if (otherSetup==this) return;
		clear();
		editModel=otherSetup.editModel.clone();
		for (ParameterCompareSetupModel model: otherSetup.getModels()) models.add(model.clone());
		for (ParameterCompareSetupValueInput record: otherSetup.getInput()) input.add(record.clone());
		for (ParameterCompareSetupValueOutput record: otherSetup.getOutput()) output.add(record.clone());
	}

	/**
	 * Überträgt die Einstellungen aus einem anderen Objekt in dieses.
	 * Das andere Objekt darf danach nicht mehr verwendet werden.
	 * @param otherSetup	Anderes Einstellungen-Objekt aus dem die Daten übernommen werden sollen
	 */
	public void transferFrom(final ParameterCompareSetup otherSetup) {
		if (otherSetup==this) return;
		clear();
		editModel=otherSetup.editModel;
		for (ParameterCompareSetupModel model: otherSetup.getModels()) models.add(model);
		for (ParameterCompareSetupValueInput record: otherSetup.getInput()) input.add(record);
		for (ParameterCompareSetupValueOutput record: otherSetup.getOutput()) output.add(record);
	}

	@Override
	public String[] getRootNodeNames() {
		return Language.trAll("ParameterCompare.XML.Root");
	}

	@Override
	protected String loadProperty(String name, String text, Element node) {
		for (String test: editModel.getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			final String error=editModel.loadFromXML(node);
			if (error!=null) return error;
			return null;
		}

		for (String test: new ParameterCompareSetupModel().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			final ParameterCompareSetupModel model=new ParameterCompareSetupModel();
			final String error=model.loadFromXML(node);
			if (error!=null) return error;
			models.add(model);
			return null;
		}

		for (String test: new ParameterCompareSetupValueInput().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			final ParameterCompareSetupValueInput record=new ParameterCompareSetupValueInput();
			final String error=record.loadFromXML(node);
			if (error!=null) return error;
			input.add(record);
			return null;
		}

		for (String test: new ParameterCompareSetupValueOutput().getRootNodeNames()) if (name.equalsIgnoreCase(test)) {
			final ParameterCompareSetupValueOutput record=new ParameterCompareSetupValueOutput();
			final String error=record.loadFromXML(node);
			if (error!=null) return error;
			output.add(record);
			return null;
		}

		return null;
	}

	@Override
	protected void addDataToXML(Document doc, Element node, boolean isPartOfOtherFile, final File file) {
		editModel.saveToXML(node,true);
		for (ParameterCompareSetupModel model: models) model.addDataToXML(doc,node);
		for (ParameterCompareSetupValueInput record: input) record.addDataToXML(doc,node);
		for (ParameterCompareSetupValueOutput record: output) record.addDataToXML(doc,node);
	}

	/**
	 * Liefert alle Tabellendaten als {@link Table}-Objekt
	 * @param addInfoRows	Fügt zusätzliche Zeilen mit Minimum/Mittelwert/Maximum an
	 * @param forceTimeAsNumber	Erzwingt, wenn <code>true</code>, die Ausgabe auch von Spalten, die explizit Zeitangaben enthalten, als Zahlenwerte
	 * @return	Alle Tabellendaten
	 */
	public Table getTableData(final boolean addInfoRows, final boolean forceTimeAsNumber) {
		final Table table=new Table();

		/* Überschriften ausgeben */

		final List<String> heading=new ArrayList<>();

		heading.add(Language.tr("ParameterCompare.Table.Column.Model"));
		for (ParameterCompareSetupValueInput input: this.input) {
			heading.add(Language.tr("ParameterCompare.Table.Column.Input")+" - "+input.getName());
		}
		for (ParameterCompareSetupValueOutput output: this.output) {
			heading.add(output.getName());
		}

		table.addLine(heading);

		/* Daten ausgeben */

		double[] minValues=new double[output.size()];
		double[] maxValues=new double[output.size()];
		int[] countValues=new int[output.size()];
		double[] sumValues=new double[output.size()];
		double[] sum2Values=new double[output.size()];

		for (ParameterCompareSetupModel model: getModels()) {
			final List<String> row=new ArrayList<>();
			row.add(model.getName());
			for (ParameterCompareSetupValueInput input: this.input) {
				final Double D=model.getInput().get(input.getName());
				if (D==null) row.add("-"); else row.add(NumberTools.formatNumberMax(D));
			}
			int nr=0;
			for (ParameterCompareSetupValueOutput output: this.output) {
				final Double D=model.getOutput().get(output.getName());

				if (D==null) {
					row.add("-");
				} else {
					double d=D.doubleValue();
					if (output.getIsTime() && !forceTimeAsNumber) {
						row.add(TimeTools.formatExactTime(d));
					} else {
						row.add(NumberTools.formatNumberMax(d));
					}

					if (countValues[nr]==0 || minValues[nr]>d) minValues[nr]=d;
					if (countValues[nr]==0 || maxValues[nr]<d) maxValues[nr]=d;
					countValues[nr]++;
					sumValues[nr]+=d;
					sum2Values[nr]+=d*d;
				}
				nr++;
			}

			table.addLine(row);
		}

		/* Kenngrößen zu den Daten ausgeben */

		if (addInfoRows)  {
			boolean hasInfoData=false;
			for (int count: countValues) if (count>0) {hasInfoData=true; break;}
			if (hasInfoData) {
				List<String> row;

				/* Leerzeile */
				row=new ArrayList<>();
				for (int i=0;i<1+input.size()+output.size();i++) row.add("");
				table.addLine(row);

				/* Minimum */
				row=new ArrayList<>();
				row.add(Language.tr("ParameterCompare.Table.Info.Minimum"));
				for (int i=0;i<input.size();i++) row.add("");
				for (int i=0;i<output.size();i++) {
					if (countValues[i]==0) row.add("-"); else {
						if (output.get(i).getIsTime() && !forceTimeAsNumber) {
							row.add(TimeTools.formatExactTime(minValues[i]));
						} else {
							row.add(NumberTools.formatNumberMax(minValues[i]));
						}
					}
				}
				table.addLine(row);

				/* Mittelwert */
				row=new ArrayList<>();
				row.add(Language.tr("ParameterCompare.Table.Info.Mean"));
				for (int i=0;i<input.size();i++) row.add("");
				for (int i=0;i<output.size();i++) {
					if (countValues[i]==0) row.add("-"); else {
						final double e=sumValues[i]/countValues[i];
						if (output.get(i).getIsTime() && !forceTimeAsNumber) {
							row.add(TimeTools.formatExactTime(e));
						} else {
							row.add(NumberTools.formatNumberMax(e));
						}
					}
				}
				table.addLine(row);

				/* Standardabweichung */
				row=new ArrayList<>();
				row.add(Language.tr("ParameterCompare.Table.Info.StdDev"));
				for (int i=0;i<input.size();i++) row.add("");
				for (int i=0;i<output.size();i++) {
					if (countValues[i]==0) row.add("-"); else {
						final double sd;
						if (countValues[i]<2) {
							sd=0;
						} else {
							final double v=sum2Values[i]/(countValues[i]-1)-(sumValues[i]*sumValues[i])/countValues[i]/(countValues[i]-1);
							sd=StrictMath.sqrt(Math.max(0,v));
						}
						if (output.get(i).getIsTime() && !forceTimeAsNumber) {
							row.add(TimeTools.formatExactTime(sd));
						} else {
							row.add(NumberTools.formatNumberMax(sd));
						}
					}
				}
				table.addLine(row);

				/* Variationskoeffizient */
				row=new ArrayList<>();
				row.add(Language.tr("ParameterCompare.Table.Info.CV"));
				for (int i=0;i<input.size();i++) row.add("");
				for (int i=0;i<output.size();i++) {
					if (countValues[i]==0) row.add("-"); else {
						final double e=sumValues[i]/countValues[i];
						final double sd;
						if (countValues[i]<2) {
							sd=0;
						} else {
							final double v=sum2Values[i]/(countValues[i]-1)-(sumValues[i]*sumValues[i])/countValues[i]/(countValues[i]-1);
							sd=StrictMath.sqrt(Math.max(0,v));
						}
						row.add(NumberTools.formatPercent((e>0)?(sd/e):0.0,3));
					}
				}
				table.addLine(row);

				/* Maximum */
				row=new ArrayList<>();
				row.add(Language.tr("ParameterCompare.Table.Info.Maximum"));
				for (int i=0;i<input.size();i++) row.add("");
				for (int i=0;i<output.size();i++) {
					if (countValues[i]==0) row.add("-"); else {
						if (output.get(i).getIsTime() && !forceTimeAsNumber) {
							row.add(TimeTools.formatExactTime(maxValues[i]));
						} else {
							row.add(NumberTools.formatNumberMax(maxValues[i]));
						}
					}
				}
				table.addLine(row);
			}
		}

		return table;
	}

	/**
	 * Konfiguriert die Parameterreihe als Varianzanalyse
	 * (d.h. zur mehrfachen Wiederholung desselben Modells ohne sich verändernde Eingabeparameter)
	 * @param repeatCount	Anzahl an Wiederholungen
	 */
	public void setupVarianceAnalysis(final int repeatCount) {
		/* Eingabeparameter */
		input.clear();

		/* Ausgabegrößen */
		output.clear();
		ParameterCompareTemplatesDialog.buildDefaultOutput(editModel,output,false);
		final ParameterCompareSetupValueOutput outputRecord=new ParameterCompareSetupValueOutput();
		outputRecord.setName(Language.tr("Statistic.FastAccess.Template.RunTime"));
		outputRecord.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
		outputRecord.setTag(Language.tr("Statistics.XML.Element.Simulation")+"->"+Language.tr("Statistics.XML.RunTime"));
		output.add(outputRecord);

		/* Modelle */
		models.clear();
		for (int i=0;i<repeatCount;i++) {
			models.add(new ParameterCompareSetupModel(String.format(Language.tr("ParameterCompare.Settings.VarianceRepeatNr"),i+1)));
		}
	}
}
