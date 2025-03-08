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
package ui.modeleditor.elements;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.tools.DistributionTools;
import simulator.elements.RunElementSourceExtern;

/**
 * Diese Helferklasse ermöglicht es, die Daten für Zwischenankunftszeiten
 * oder Bedienzeiten für mehrere Kundentypen aus einer Tabelle zu laden.
 * @author Alexander Herzog
 *
 */
public class ClientTypeLoader {
	/**
	 * Tabellendatei aus der die Kundentypendaten geladen werden sollen
	 */
	private final Table table;

	/**
	 * Konstruktor der Klasse
	 * @param file	Tabellendatei aus der die Kundentypendaten geladen werden sollen
	 */
	public ClientTypeLoader(final File file) {
		table=new Table();
		if (file!=null) table.load(file);
	}

	/**
	 * Lädt Zwischenankuftszeiten für mehrere Kundentypen aus der angegebenen Tabelle
	 * @return	Liste der Zwischenankuftszeiten-Datensätze (kann leer sein, ist aber nie <code>null</code>)
	 */
	public List<ModelElementSourceRecord> getArrivalClientTypes() {
		final List<ModelElementSourceRecord> clientTypes=new ArrayList<>();
		final int lines=table.getSize(0);
		for (int i=0;i<lines;i++) {
			final ModelElementSourceRecord record=processArrivalLine(table.getLine(i));
			if (record!=null) clientTypes.add(record);
		}

		return clientTypes;
	}

	/**
	 * Lädt Bedienzeiten für mehrere Kundentypen aus der angegebenen Tabelle
	 * @return	Zuordnung von Kundentypnamen zu Bedienzeiten (Zeichenkette oder Verteilung). Die Zuordnung kann leer sein, ist aber nie <code>null</code>.
	 */
	public Map<String,Object> getProcessingClientTypes() {
		final Map<String,Object> clientTypes=new HashedMap<>();
		final int lines=table.getSize(0);
		for (int i=0;i<lines;i++) {
			final List<String> line=table.getLine(i);
			if (line.size()<2) return null;
			final String name=line.get(0);
			AbstractRealDistribution distribution=null;
			if (NumberTools.getDouble(line.get(1))==null) distribution=DistributionTools.distributionFromString(line.get(1),3000);
			if (distribution==null) {
				clientTypes.put(name,line.get(1));
			} else {
				clientTypes.put(name,distribution);
			}
		}
		return clientTypes;
	}

	/**
	 * Lädt Rüstzeiten für mehrere Kundentypen aus der angegebenen Tabelle
	 * @return	Zuordnung von Kundentypnamen zu Rüstzeiten (Zeichenkette oder Verteilung). Die Zuordnung kann leer sein, ist aber nie <code>null</code>.
	 */
	public Map<String,Map<String,Object>> getSetupTimesClientTypes() {
		final Map<String,Map<String,Object>> clientTypes=new HashedMap<>();
		final int lines=table.getSize(0);
		for (int i=0;i<lines;i++) {
			final List<String> line=table.getLine(i);
			if (line.size()<3) return null;
			final String name1=line.get(0);
			final String name2=line.get(1);
			AbstractRealDistribution distribution=null;
			if (NumberTools.getDouble(line.get(2))==null) distribution=DistributionTools.distributionFromString(line.get(2),3000);
			if (distribution==null) {
				Map<String,Object> sub=clientTypes.get(name1);
				if (sub==null) clientTypes.put(name1,sub=new HashMap<>());
				sub.put(name2,line.get(2));
			} else {
				Map<String,Object> sub=clientTypes.get(name1);
				if (sub==null) clientTypes.put(name1,sub=new HashMap<>());
				sub.put(name2,distribution);
			}
		}
		return clientTypes;
	}

	/**
	 * Verarbeitet eine einzelne Tabellenzeile für einen Ankunftsdatensatz.
	 * @param line	Zu verarbeitende Tabellenzeile
	 * @return	Liefert im Erfolgsfall einen Ankunftsdatensatz, sonst <code>null</code>
	 * @see #getArrivalClientTypes()
	 */
	private ModelElementSourceRecord processArrivalLine(final List<String> line) {
		if (line.size()<2) return null;
		final ModelElementSourceRecord record=new ModelElementSourceRecord(true,true,true);

		record.setName(line.get(0));

		final AbstractRealDistribution distribution=DistributionTools.distributionFromString(line.get(1),3000);
		if (distribution==null) {
			record.setInterarrivalTimeExpression(line.get(1));
		} else {
			record.setInterarrivalTimeDistribution(distribution);
		}

		for (int i=2;i<line.size();i++) processArrivalColumn(record,line.get(i));

		return record;
	}

	/**
	 * Verarbeitet eine einzelne Zuweisung innerhalb einer für einen Ankunftsdatensatz gedachten Tabellenzeile.
	 * @param record	Zu ergänzender Ankunftsdatensatz
	 * @param column	Zelle in der Tabellenzeile
	 * @see #processArrivalLine(List)
	 */
	private void processArrivalColumn(final ModelElementSourceRecord record, final String column) {
		if (column==null || column.isBlank()) return;
		final int index=column.indexOf('=');
		if (index<1 || index>=column.length()) return;
		final String name=column.substring(0,index).trim();
		final String value=column.substring(index+1).trim();

		if (name.equalsIgnoreCase("batch")) {
			processArrivalColumnBatch(record,value);
			return;
		}

		if (name.equalsIgnoreCase("count")) {
			final Long L=NumberTools.getPositiveLong(value);
			if (L!=null) record.setMaxArrivalCount(L);
			return;
		}

		if (name.equalsIgnoreCase("start")) {
			final Double D=NumberTools.getNotNegativeDouble(value);
			if (D!=null) record.setArrivalStart(D);
			return;
		}

		final Object[] data=new Object[2];
		if (RunElementSourceExtern.Arrival.processCell(column,data)) {
			if (data[0] instanceof Integer) {
				final List<String> variables=new ArrayList<>(Arrays.asList(record.getSetRecord().getVariables()));
				final List<String> expressions=new ArrayList<>(Arrays.asList(record.getSetRecord().getExpressions()));
				final int nr=(Integer)data[0];
				if (nr>=0) {
					variables.add("ClientData("+nr+")");
					expressions.add((String)data[1]);
				} else {
					switch (nr) {
					case -1:
						variables.add("w");
						expressions.add((String)data[1]);
						break;
					case -2:
						variables.add("t");
						expressions.add((String)data[1]);
						break;
					case -3:
						variables.add("p");
						expressions.add((String)data[1]);
						break;
					case -4:
						variables.add("wcosts");
						expressions.add((String)data[1]);
						break;
					case -5:
						variables.add("tcosts");
						expressions.add((String)data[1]);
						break;
					case -6:
						variables.add("pcosts");
						expressions.add((String)data[1]);
						break;
					}
				}
				record.getSetRecord().setData(variables.toArray(String[]::new),expressions.toArray(String[]::new));
			} else {
				record.getStringRecord().getKeys().add((String)data[0]);
				record.getStringRecord().getValues().add((String)data[1]);
			}
		}
	}

	/**
	 * Verarbeitet eine einzelne Batch-Ankunfts-Zuwisung
	 * @param record	Zu ergänzender Ankunftsdatensatz
	 * @param batchData	Batch-Ankunfts-Zuwisung
	 * @see #processArrivalColumn(ModelElementSourceRecord, String)
	 */
	private void processArrivalColumnBatch(final ModelElementSourceRecord record, final String batchData) {
		if (batchData==null || batchData.isBlank()) return;
		int index=batchData.indexOf('=');
		if (index<1 || index>=batchData.length()) {
			record.setBatchSize(batchData);
			return;
		}

		final List<Double> rates=new ArrayList<>();
		for (String rate: batchData.split(";")) {
			index=rate.indexOf('=');
			if (index<1 || index>=rate.length()) continue;
			Long Size=NumberTools.getPositiveLong(rate.substring(0,index).trim());
			Double SizeRate=NumberTools.getPositiveDouble(rate.substring(index+1).trim());
			if (Size!=null && SizeRate!=null) {
				while (rates.size()<Size) rates.add(0.0);
				rates.set((int)(Size-1),SizeRate);
			}
		}
		record.setMultiBatchSize(rates.stream().mapToDouble(D->D.doubleValue()).toArray());
	}

	/**
	 * Zeigt einen Dialog zur Auswahl der zu ladenden Tabellendatei an.
	 * @param parent	Elternkomponente des Dialogs
	 * @return	Liefert im Erfolgsfall den Dateinamen, sonst <code>null</code>
	 */
	public static File selectFile(final Component parent) {
		return Table.showLoadDialog(parent,Language.tr("ClientTypeLoader.SelectTitle"));
	}
}
