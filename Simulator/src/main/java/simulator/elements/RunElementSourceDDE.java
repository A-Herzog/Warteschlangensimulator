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
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import net.dde.DDEConnect;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSourceDDE</code>
 * @author Alexander Herzog
 * @see ModelElementSourceDDE
 */
public class RunElementSourceDDE extends RunElementSourceExtern {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSourceDDE(final ModelElementSourceDDE element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SourceDDE.Name")));
	}

	/**
	 * Lädt die Kundenankünfte über eine DDE-Verbindung.
	 * @param sourceElement	Editor-Element aus dem die DDE-Einstellungen geladen werden
	 * @param clientTypes	Kundentypen die beim Laden der Daten berücksichtigt werden sollen
	 * @param numbersAreDistances	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @param runModel	Laufzeitmodell
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	private String loadTable(final ModelElementSourceDDE sourceElement, final List<String> clientTypes, final boolean numbersAreDistances, final RunModel runModel) {
		final Table table=new Table();

		final DDEConnect connect=new DDEConnect();
		final DDEConnect.DataIterator iterator=connect.getData(sourceElement.getWorkbook().trim(),sourceElement.getTable().trim(),sourceElement.getStartRow()-1,Table.numberFromColumnName(sourceElement.getColumn()));
		final List<String> row=new ArrayList<>();
		while (iterator.hasNext()) {
			row.clear();

			/* Ankunftszeit */
			final Double D=iterator.next();
			if (D==null) continue;
			row.add(NumberTools.formatSystemNumber(D));

			/* Weitere Spalten */
			final List<String> data=iterator.data();

			/* Kundentyp */
			if (data.size()<1) continue;
			final String name=data.get(0);
			if (name==null) continue;
			row.add(name);

			/* Zusatzdaten */
			for (int i=1;i<data.size();i++) row.add(data.get(i));

			table.addLine(row);
		}

		if (table.getSize(0)==0) return String.format(Language.tr("Simulation.Creator.DDEError.NoRows"),sourceElement.getId(),sourceElement.getTable());

		return loadTable(table,clientTypes,numbersAreDistances,false,runModel.scaleToSimTime);
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSourceDDE)) return null;

		final ModelElementSourceDDE sourceElement=(ModelElementSourceDDE)element;
		final RunElementSourceDDE source=new RunElementSourceDDE(sourceElement);

		/* DDE im Allgemeinen */
		if (!new DDEConnect().available()) return String.format(Language.tr("Simulation.Creator.DDENotAvailable"),element.getId());

		/* Daten aus Edit-Element auslesen */
		final List<String> clientTypes=sourceElement.getClientTypeNames();

		/* Kundentypen laden */
		String error=source.buildClientTypesList(clientTypes,runModel);
		if (error!=null) return error;

		/* Tabelle verarbeiten */
		if (!testOnly) {
			error=source.loadTable(sourceElement,clientTypes,sourceElement.isNumbersAreDistances(),runModel);
			if (error!=null) return error;
		}

		/* Auslaufende Kante */
		error=source.buildConnection(sourceElement);
		if (error!=null) return error;

		return source;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSourceDDE)) return null;

		if (findNextId(((ModelElementSourceDDE)element).getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);

		/* DDE im Allgemeinen */
		if (!new DDEConnect().available()) return RunModelCreatorStatus.noDDE(element);

		/* Daten aus Edit-Element auslesen */
		final List<String> clientTypes=((ModelElementSourceDDE)element).getClientTypeNames();

		/* Prüfen, ob Kundentypen definiert sind */
		final RunModelCreatorStatus error=testClientTypes(clientTypes,element);
		if (error!=null) return error;

		return RunModelCreatorStatus.ok;
	}
}