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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.builder.RunModelCreatorStatus;
import simulator.db.DBConnect;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSourceDB</code>
 * @author Alexander Herzog
 * @see ModelElementSourceDB
 */
public class RunElementSourceDB extends RunElementSourceExtern {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSourceDB(final ModelElementSourceDB element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SourceDB.Name")));
	}

	/**
	 * Lädt die Kundenankünfte aus einer Datenbanktabelle.
	 * @param sourceElement	Editor-Element aus dem die Datenbankeinstellungen geladen werden
	 * @param clientTypes	Kundentypen die beim Laden der Tabelle berücksichtigt werden sollen
	 * @param runModel	Laufzeitmodell
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	private String loadDatabase(final ModelElementSourceDB sourceElement, final List<String> clientTypes, final RunModel runModel) {
		try (DBConnect connect=new DBConnect(sourceElement.getDb(),false)) {
			if (connect.getInitError()!=null) return Language.tr("Simulation.Creator.DatabaseError")+": "+connect.getInitError();

			final Table table=new Table();

			final List<String> data=new ArrayList<>();
			data.add(sourceElement.getClientTypeColumn());
			if (!sourceElement.getInfoColumn().isBlank()) data.add(sourceElement.getInfoColumn());
			final Iterator<Double> iterator=connect.readTableColumn(sourceElement.getTable(),sourceElement.getLoadColumn(),null,null,data.toArray(String[]::new));
			final List<String> row=new ArrayList<>();
			while (iterator.hasNext()) {
				row.clear();

				/* Ankunftszeit */
				final Double D=iterator.next();
				if (D==null) continue;
				row.add(NumberTools.formatSystemNumber(D));

				/* Kundentyp */
				final String name=connect.readAdditionalColumn(iterator,0);
				if (name==null) continue;
				row.add(name);

				/* Zusatzdaten */
				if (data.size()>1) {
					final String infoCell=connect.readAdditionalColumn(iterator,1);
					if (infoCell!=null) for (String info: infoCell.split("\t")) row.add(info);
				}

				table.addLine(row);
			}

			if (table.getSize(0)==0) return String.format(Language.tr("Simulation.Creator.DatabaseError.NoRows"),sourceElement.getId(),sourceElement.getTable());

			return loadTable(table,clientTypes,false,false,runModel.scaleToSimTime);
		}
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSourceDB)) return null;

		final ModelElementSourceDB sourceElement=(ModelElementSourceDB)element;
		final RunElementSourceDB source=new RunElementSourceDB(sourceElement);

		/* Daten aus Edit-Element auslesen */
		final List<String> clientTypes=sourceElement.getClientTypeNames();

		/* Kundentypen laden */
		String error=source.buildClientTypesList(clientTypes,runModel);
		if (error!=null) return error;

		/* Tabelle verarbeiten */
		if (!testOnly) {
			error=source.loadDatabase(sourceElement,clientTypes,runModel);
			if (error!=null) return error;
		}

		/* Auslaufende Kante */
		error=source.buildConnection(sourceElement);
		if (error!=null) return error;

		return source;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSourceDB)) return null;

		if (findNextId(((ModelElementSourceDB)element).getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);

		/* Daten aus Edit-Element auslesen */
		final List<String> clientTypes=((ModelElementSourceDB)element).getClientTypeNames();

		/* Prüfen, ob Kundentypen definiert sind */
		final RunModelCreatorStatus error=testClientTypes(clientTypes,element);
		if (error!=null) return error;

		return RunModelCreatorStatus.ok;
	}
}
