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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import simulator.editmodel.EditModel;
import statistics.StatisticsLongRunPerformanceIndicator;
import statistics.StatisticsMultiPerformanceIndicator;
import ui.statistics.StatisticTools;
import ui.statistics.StatisticViewerOverviewText;
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
		return getTableData(addInfoRows,forceTimeAsNumber,0);
	}

	/**
	 * Formatiert eine Zahl für eine Ausgabezelle gemäß dem für die Ausgabe gewählten Format
	 * @param value	Zu formatierender Wert
	 * @param forceTimeAsNumber	Erzwingt, wenn <code>true</code>, die Ausgabe auch von Spalten, die explizit Zeitangaben oder Prozentwerze enthalten, als Zahlenwerte
	 * @param format	Gewünschtes Ausgabeformat
	 * @return	Zeichenkette, die die Zahl im entsprechenden Format enthält
	 */
	private static String formatCell(final double value, final boolean forceTimeAsNumber, final ParameterCompareSetupValueOutput.OutputFormat format) {
		switch (format) {
		case FORMAT_NUMBER:
			return NumberTools.formatNumberMax(value);
		case FORMAT_PERCENT:
			if (forceTimeAsNumber) {
				return NumberTools.formatPercent(value,7);
			} else {
				return TimeTools.formatExactTime(value);
			}
		case FORMAT_TIME:
			if (forceTimeAsNumber) {
				return NumberTools.formatNumberMax(value);
			} else {
				return TimeTools.formatExactTime(value);
			}
		default:
			return NumberTools.formatNumberMax(value);
		}
	}

	/**
	 * Trägt die Daten aus {@link #getModels()} in eine Tabelle ein
	 * @param table	Ergebnistabelle in die die Daten eingetragen werden sollen
	 * @param forceTimeAsNumber	Erzwingt, wenn <code>true</code>, die Ausgabe auch von Spalten, die explizit Zeitangaben enthalten, als Zahlenwerte
	 * @param upscaler	Sollen die Daten hochskaliert werden? Werte größer als 0 geben die Anzahl an jeweils einzufügenden Zwischenwerten an.
	 */
	private void processTableData(final Table table, final boolean forceTimeAsNumber, int upscaler) {
		upscaler=Math.max(0,Math.min(4,upscaler));

		final List<List<String>> dataRows=new ArrayList<>();
		final List<List<Double>> rawValues=new ArrayList<>();

		for (ParameterCompareSetupModel model: getModels()) {
			final List<String> dataRow=new ArrayList<>();
			final List<Double> raw=new ArrayList<>();

			/* Modellname */
			dataRow.add(model.getName());

			/* Eingabeparameter */
			for (ParameterCompareSetupValueInput input: this.input) {
				final Double D=model.getInput().get(input.getName());
				if (D==null) dataRow.add("-"); else dataRow.add(NumberTools.formatNumberMax(D));
				raw.add(D);
				if (D==null) upscaler=0;
			}

			/* Ausgabeparameter */
			for (ParameterCompareSetupValueOutput output: this.output) {
				final Double D=model.getOutput().get(output.getName());
				if (D==null) {
					dataRow.add("-");
				} else {
					dataRow.add(formatCell(D.doubleValue(),forceTimeAsNumber,output.getFormat()));
				}
				raw.add(D);
				if (D==null) upscaler=0;
			}

			/* Zeile speichern */
			dataRows.add(dataRow);
			rawValues.add(raw);
		}

		/* Splinefunktionen erstellen */
		PolynomialSplineFunction[] spline=null;
		if (rawValues.size()>0 && upscaler>0) {
			final int rows=rawValues.size();
			final int cols=rawValues.get(0).size();

			final double[][] y=new double[cols][];
			for (int i=0;i<cols;i++) y[i]=new double[rows];
			final double[] x=new double[rows];
			for (int i=0;i<rows;i++) x[i]=i;

			for (int i=0;i<rows;i++) {
				final List<Double> raw=rawValues.get(i);
				for (int j=0;j<cols;j++) y[j][i]=raw.get(j);
			}

			final SplineInterpolator interpolator=new SplineInterpolator();
			spline=new PolynomialSplineFunction[cols];
			for (int i=0;i<cols;i++) spline[i]=interpolator.interpolate(x,y[i]);
		}

		/* Tabelle erstellen */
		for (int i=0;i<dataRows.size();i++) {
			table.addLine(dataRows.get(i));
			if (spline!=null && i<dataRows.size()-1) {
				List<String> row1=dataRows.get(i);
				List<String> row2=dataRows.get(i+1);
				final int inParamCount=input.size();
				Double[] interpolatedValues=new Double[spline.length];
				for (int nr=1;nr<=upscaler;nr++) {
					final double fraction=(nr)/(upscaler+1.0);
					final List<String> scaleRow=new ArrayList<>();
					scaleRow.add(Language.tr("ParameterCompare.Table.IntermediateValue"));
					for (int j=0;j<spline.length;j++) {
						double d=spline[j].value(i+fraction);
						final int outParam=j-inParamCount;
						final Double D1;
						final Double D2;
						if (outParam>=0 && output.get(outParam).getFormat()==ParameterCompareSetupValueOutput.OutputFormat.FORMAT_TIME) {
							D1=TimeTools.getExactTime(row1.get(j+1));
							D2=TimeTools.getExactTime(row2.get(j+1));
						} else {
							/* Zahl oder Prozentwert */
							D1=NumberTools.getDouble(row1.get(j+1));
							D2=NumberTools.getDouble(row2.get(j+1));
						}
						if (D1!=null && D2!=null) {
							/* Linear skalieren, wenn der Upscaler versagt. */
							if (d<Math.min(D1,D2) || d>Math.max(D1,D2)) d=D1+(D2-D1)*fraction; /* Wenn Wert komplett außerhalb des Bereichs liegt */
							if (upscaler==1) {
								final double delta=Math.abs(D1-D2);
								if (d<Math.min(D1,D2)+delta*0.1 || d>Math.max(D1,D2)-delta*0.1) d=D1+(D2-D1)*fraction; /* Wenn Wert bei nur einem Zwischenschritt zu nah an den Grenzen liegt */
							}
							if (interpolatedValues[j]!=null) { /* Wenn der letzte Zwischenwert und dieser nicht zur generellen Steigung passen */
								if (D2>D1) {
									if (d<interpolatedValues[j]) d=(interpolatedValues[j]+D2)/2;
								} else {
									if (d>interpolatedValues[j]) d=(interpolatedValues[j]+D2)/2;
								}
							}
						}

						ParameterCompareSetupValueOutput.OutputFormat format=ParameterCompareSetupValueOutput.OutputFormat.FORMAT_NUMBER;
						if (outParam>=0) format=output.get(outParam).getFormat();
						scaleRow.add(formatCell(d,false,format));

						interpolatedValues[j]=d;
					}
					table.addLine(scaleRow);
				}
			}
		}
	}

	/**
	 * Liefert alle Tabellendaten als {@link Table}-Objekt
	 * @param addInfoRows	Fügt zusätzliche Zeilen mit Minimum/Mittelwert/Maximum an
	 * @param forceTimeAsNumber	Erzwingt, wenn <code>true</code>, die Ausgabe auch von Spalten, die explizit Zeitangaben enthalten, als Zahlenwerte
	 * @param upscaler	Sollen die Daten hochskaliert werden? Werte größer als 0 geben die Anzahl an jeweils einzufügenden Zwischenwerten an.
	 * @return	Alle Tabellendaten
	 */
	public Table getTableData(final boolean addInfoRows, final boolean forceTimeAsNumber, final int upscaler) {
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

		processTableData(table,forceTimeAsNumber,upscaler);

		/* Zusätzliche Kenngrößen */

		if (addInfoRows)  {

			/* Daten zusammenstellen */

			final double[] minValues=new double[output.size()];
			final double[] maxValues=new double[output.size()];
			final int[] countValues=new int[output.size()];
			final double[] sumValues=new double[output.size()];
			final double[] sum2Values=new double[output.size()];

			for (ParameterCompareSetupModel model: getModels()) {
				int nr=0;
				for (ParameterCompareSetupValueOutput output: this.output) {
					final Double D=model.getOutput().get(output.getName());
					if (D==null) continue;
					double d=D.doubleValue();
					if (countValues[nr]==0 || minValues[nr]>d) minValues[nr]=d;
					if (countValues[nr]==0 || maxValues[nr]<d) maxValues[nr]=d;
					countValues[nr]++;
					sumValues[nr]+=d;
					sum2Values[nr]+=d*d;
					nr++;
				}
			}

			/* Kenngrößen zu den Daten ausgeben */

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
					if (countValues[i]==0) row.add("-"); else row.add(formatCell(minValues[i],forceTimeAsNumber,output.get(i).getFormat()));
				}
				table.addLine(row);

				/* Mittelwert */
				row=new ArrayList<>();
				row.add(Language.tr("ParameterCompare.Table.Info.Mean"));
				for (int i=0;i<input.size();i++) row.add("");
				for (int i=0;i<output.size();i++) {
					if (countValues[i]==0) row.add("-"); else {
						final double e=sumValues[i]/countValues[i];
						row.add(formatCell(e,forceTimeAsNumber,output.get(i).getFormat()));
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
						row.add(formatCell(sd,forceTimeAsNumber,output.get(i).getFormat()));
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
					if (countValues[i]==0) row.add("-"); else row.add(formatCell(maxValues[i],forceTimeAsNumber,output.get(i).getFormat()));
				}
				table.addLine(row);
			}
		}

		return table;
	}

	/**
	 * Liefert die Laufzeitstatistikdaten als Tabelle als {@link Table}-Objekt
	 * @return	Laufzeitstatistikdaten als Tabelle (oder <code>null</code>, wenn keien Laufzeitstatistik aufgezeichnet wurde)
	 */
	public Table getLongRunTableData() {
		final Table table=new Table();

		final double[] quantilLevels=StatisticViewerOverviewText.getQuantilLevels();
		final Map<String,LongRunData> data=getLongRunData(quantilLevels);
		if (data==null || data.size()==0) return null;

		final String[] names=data.keySet().stream().sorted().toArray(String[]::new);

		/* Überschriften */
		final List<String> heading1=new ArrayList<>();
		final List<String> heading2=new ArrayList<>();
		heading1.add("");
		heading1.add("");
		heading2.add("");
		heading2.add("");
		for (String name: names) {
			/* Namen */
			heading1.add(name);
			for (int i=2;i<=3+quantilLevels.length;i++) heading1.add("");
			/* Eigentliche Spaltenüberschriften */
			heading2.add(Language.tr("ParameterCompare.Table.Info.Minimum"));
			heading2.add(Language.tr("ParameterCompare.Table.Info.Mean"));
			heading2.add(Language.tr("ParameterCompare.Table.Info.Maximum"));
			for (double quantilLevel: quantilLevels) heading2.add(StatisticTools.formatPercent(quantilLevel)+"-"+Language.tr("Statistics.Quantil"));
		}
		table.addLine(heading1);
		table.addLine(heading2);

		/* Daten */
		final long stepWideSec=editModel.longRunStatistics.getStepWideSec();
		final int rowCount=data.values().stream().mapToInt(indicator->indicator.getRowCount()).max().orElse(0);
		for (int interval=0;interval<rowCount;interval++) {
			final List<String> line=new ArrayList<>();
			/* Zeitbereich */
			line.add(TimeTools.formatLongTime(stepWideSec*interval));
			line.add(TimeTools.formatLongTime(stepWideSec*(interval+1)-1));
			/* Kenngrößen */
			for (String name: names) {
				final LongRunData indicator=data.get(name);
				line.add(NumberTools.formatNumberMax(indicator.getMin(interval)));
				line.add(NumberTools.formatNumberMax(indicator.getMean(interval)));
				line.add(NumberTools.formatNumberMax(indicator.getMax(interval)));
				for (int nr=0;nr<quantilLevels.length;nr++) line.add(NumberTools.formatNumberMax(indicator.getQuantil(interval,nr)));
			}
			table.addLine(line);
		}

		return table;
	}

	/**
	 * Berechnet die zusammengefassten Laufzeitstatistikdaten für die einzelnen Laufzeitstatistikindikatoren
	 * @param quantilLevels	Auszugebende Quantilwerte (darf nicht <code>null</code> sein)
	 * @return	Zuordnung von Laufzeitstatistikindikatorennamen zu den jeweiligen Daten
	 * @see LongRunData
	 */
	private Map<String,LongRunData> getLongRunData(final double[] quantilLevels) {
		final Map<String,LongRunData> data=new HashMap<>();

		/* Threading-System vorbereiten */
		final int coreCount=Runtime.getRuntime().availableProcessors();
		final ExecutorService executor=new ThreadPoolExecutor(coreCount,coreCount,5,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),new ThreadFactory() {
			private final AtomicInteger threadNumber=new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,"Long run data loader "+threadNumber.getAndIncrement());
			}
		});
		((ThreadPoolExecutor)executor).allowCoreThreadTimeOut(true);

		/* Parallele Verarbeitung starten */
		final List<Future<Map<String,LongRunData>>> results=new ArrayList<>();
		for (ParameterCompareSetupModel model: models) if (model.isStatisticsAvailable()) {
			results.add(executor.submit(()->{
				final Map<String,LongRunData> subData=new HashMap<>();
				final StatisticsMultiPerformanceIndicator longRunStatistics=model.getStatistics().longRunStatistics;
				for (String name: longRunStatistics.getNames()) {
					final LongRunData longRunData=new LongRunData(quantilLevels);
					longRunData.process((StatisticsLongRunPerformanceIndicator)longRunStatistics.get(name));
					subData.put(name,longRunData);
				}
				return subData;
			}));
		}

		/* Zu Gesamtstatistik hinzufügen */
		for (Future<Map<String,LongRunData>> result: results) {
			final Map<String,LongRunData> subData;
			try {subData=result.get();} catch (InterruptedException|ExecutionException e) {return null;}

			for (String name: subData.keySet()) {
				LongRunData longRunData=data.get(name);
				if (longRunData==null) data.put(name,longRunData=new LongRunData(quantilLevels));
				longRunData.process(subData.get(name));
			}
		}

		return data;
	}

	/**
	 * Zusammengefasste Laufzeitstatistikdaten für einen Laufzeitstatistikindikator
	 */
	private static class LongRunData {
		/**
		 * Auszugebende Quantilwerte (darf nicht <code>null</code> sein)
		 */
		private final double[] quantilLevels;

		/**
		 * Erfasste Werte pro Datenreihe und pro Intervall
		 */
		private final List<double[]> values;

		/**
		 * Wurde die Verarbeitung durch {@link #processLoadedData()} abgeschlossen?
		 * @see #processLoadedData()
		 */
		private boolean processingDone;

		/**
		 * Minimaler Wert pro Intervall
		 * @see #processLoadedData()
		 * @see #getMin(int)
		 */
		private double[] min;

		/**
		 * Mittelwert pro Intervall
		 * @see #processLoadedData()
		 * @see #getMean(int)
		 */
		private double[] mean;

		/**
		 * Maximaler Wert pro Intervall
		 * @see #processLoadedData()
		 * @see #getMax(int)
		 */
		private double[] max;

		/**
		 * Quantile pro Intervall
		 * @see #processLoadedData()
		 * @see #getQuantil(int, int)
		 * @see #quantilLevels
		 */
		private double[][] quantil;

		/**
		 * Konstruktor der Klasse
		 * @param quantilLevels	Auszugebende Quantilwerte (darf nicht <code>null</code> sein)
		 */
		public LongRunData(final double[] quantilLevels) {
			this.quantilLevels=quantilLevels;
			values=new ArrayList<>();
		}

		/**
		 * Fügt einen weiteren Datensatz hinzu.
		 * @param statistics	Neuer Datensatz
		 */
		private void process(final StatisticsLongRunPerformanceIndicator statistics) {
			values.add(statistics.getValues());
			processingDone=false;
		}

		/**
		 * Fügt weitere Datensätze aus einem anderen {@link LongRunData}-Objekt hinzu.
		 * @param otherLongRunData	Anderes {@link LongRunData}-Objekt dessen Daten hinzugefügt werden sollen
		 */
		private void process(final LongRunData otherLongRunData) {
			values.addAll(otherLongRunData.values);
			processingDone=false;
		}

		/**
		 * Liefer die Anzahl der erfassten Zeitschritte.
		 * @return	Anzahl der erfassten Zeitschritte
		 */
		public int getRowCount() {
			return values.stream().mapToInt(row->row.length).max().orElse(0);
		}

		/**
		 * Ermittelt die Kenngrößen aus den geladenen Daten.
		 * @see #values
		 */
		private void processLoadedData() {
			if (processingDone) return;
			processingDone=true;

			final int rowCount=getRowCount();

			this.min=new double[rowCount];
			this.mean=new double[rowCount];
			this.max=new double[rowCount];
			this.quantil=new double[rowCount][];

			for (int interval=0;interval<rowCount;interval++) {
				double min=Double.MAX_VALUE;
				double max=-Double.MAX_VALUE;
				int count=0;
				double sum=0;
				final List<Double> rowData=new ArrayList<>();
				for (double[] row: values) if (interval<row.length) {
					final double value=row[interval];
					if (value<min) min=value;
					if (value>max) max=value;
					count++;
					sum+=value;
					rowData.add(value);
				}
				this.min[interval]=min;
				this.mean[interval]=sum/Math.max(count,1);
				this.max[interval]=max;

				final double[] sorted=rowData.stream().mapToDouble(Double::doubleValue).sorted().toArray();
				final double[] quantil=new double[quantilLevels.length];
				for (int i=0;i<quantilLevels.length;i++) {
					quantil[i]=sorted[(int)Math.floor(quantilLevels[i]*(sorted.length-1))];
				}
				this.quantil[interval]=quantil;
			}
		}

		/**
		 * Liefert das Minimum für ein Intervall.
		 * @param interval	Nummer des Intervalls
		 * @return	Minimum
		 */
		public double getMin(final int interval) {
			processLoadedData();
			if (interval<0 || interval>=min.length) return 0;
			return min[interval];
		}

		/**
		 * Liefert den Mittelwert für ein Intervall.
		 * @param interval	Nummer des Intervalls
		 * @return	Mittelwert
		 */
		public double getMean(final int interval) {
			processLoadedData();
			if (interval<0 || interval>=mean.length) return 0;
			return mean[interval];
		}

		/**
		 * Liefert das Maximum für ein Intervall.
		 * @param interval	Nummer des Intervalls
		 * @return	Maximum
		 */
		public double getMax(final int interval) {
			processLoadedData();
			if (interval<0 || interval>=max.length) return 0;
			return max[interval];
		}

		/**
		 * Liefert einen Quantilswert für ein Intervall.
		 * @param interval	Nummer des Intervalls
		 * @param nr	Nummer des Quantilswers (zu den im Konstruktor angegebenen Quantil-Levels)
		 * @return	Quantilswert
		 */
		public double getQuantil(final int interval, final int nr) {
			processLoadedData();
			if (interval<0 || interval>=quantil.length) return 0;
			final double[] quantilData=quantil[interval];
			if (nr<0 || nr>=quantilData.length) return 0;
			return quantilData[nr];
		}
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
