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

import java.io.File;
import java.util.List;

import language.Language;
import mathtools.Table;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Äquivalent zu <code>ModelElementSourceTable</code>
 * @author Alexander Herzog
 * @see ModelElementSourceTable
 */
public class RunElementSourceTable extends RunElementSourceExtern {
	/**
	 * Konstruktor der Klasse
	 * @param element	Zugehöriges Editor-Element
	 */
	public RunElementSourceTable(final ModelElementSourceTable element) {
		super(element,buildName(element,Language.tr("Simulation.Element.SourceTable.Name")));
	}

	/**
	 * Lädt Ankünfte aus einer Tabelle.
	 * @param file	Zu ladende Tabellendatei
	 * @param setup	Konfiguration der Spalten (kann <code>null</code> sein, wenn eine bereits aufbereitete Tabelle verwendet werden soll)
	 * @param clientTypes	Liste der Kundentypnamen, die berücksichtigt werden sollen
	 * @param numbersAreDistances	Gibt an, ob die Zahlen Zeitpunkte (<code>false</code>) oder Zwischenankunftszeiten (<code>true</code>) sind
	 * @param bottomUp	Tabelle von unten nach oben lesen
	 * @return	Liefert im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung
	 */
	private String loadTableFile(final File file, final String setup, final List<String> clientTypes, final boolean numbersAreDistances, final boolean bottomUp) {
		final Table table=new Table();
		if (!table.load(file)) return String.format(Language.tr("Simulation.Creator.TableFile.LoadFailed"),file.toString(),id);
		return loadTable(table,setup,clientTypes,numbersAreDistances,bottomUp);
	}

	@Override
	public Object build(final EditModel editModel, final RunModel runModel, final ModelElement element, final ModelElementSub parent, final boolean testOnly) {
		if (!(element instanceof ModelElementSourceTable)) return null;

		final ModelElementSourceTable sourceElement=(ModelElementSourceTable)element;
		final RunElementSourceTable source=new RunElementSourceTable(sourceElement);

		/* Daten aus Edit-Element auslesen */
		final String tableFileName=sourceElement.getInputFile().trim();
		final boolean numbersAreDistances=sourceElement.isNumbersAreDistances();
		final List<String> clientTypes=sourceElement.getClientTypeNames();

		/* Tabellendatei auf Existenz prüfen */
		if (tableFileName.isEmpty()) return String.format(Language.tr("Simulation.Creator.TableFile.Missing"),element.getId());
		final File tableFile=new File(tableFileName);
		if (!tableFile.isFile()) return String.format(Language.tr("Simulation.Creator.TableFile.DoesNotExist"),tableFileName,element.getId());

		/* Kundentypen laden */
		String error=source.buildClientTypesList(clientTypes,runModel);
		if (error!=null) return error;

		/* On-the-fly Konvertierung */
		final String importSettings=sourceElement.getImportSettings().trim();
		final String setup=importSettings.isEmpty()?null:importSettings;

		/* Tabelle verarbeiten */
		if (!testOnly) {
			error=source.loadTableFile(tableFile,setup,clientTypes,numbersAreDistances,sourceElement.isReadBottomUp());
			if (error!=null) return error;
		}

		/* Auslaufende Kante */
		error=source.buildConnection(sourceElement);
		if (error!=null) return error;

		return source;
	}

	@Override
	public RunModelCreatorStatus test(ModelElement element) {
		if (!(element instanceof ModelElementSourceTable)) return null;

		if (findNextId(((ModelElementSourceTable)element).getEdgeOut())<0) return RunModelCreatorStatus.noEdgeOut(element);

		/* Daten aus Edit-Element auslesen */
		final String tableFileName=((ModelElementSourceTable)element).getInputFile().trim();
		final List<String> clientTypes=((ModelElementSourceTable)element).getClientTypeNames();

		/* Tabellendatei auf Existenz prüfen */
		if (tableFileName.isEmpty()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.TableFile.Missing"),element.getId()));
		final File tableFile=new File(tableFileName);
		if (!tableFile.isFile()) return new RunModelCreatorStatus(String.format(Language.tr("Simulation.Creator.TableFile.DoesNotExist"),tableFileName,element.getId()));

		/* Prüfen, ob Kundentypen definiert sind */
		final RunModelCreatorStatus error=testClientTypes(clientTypes,element);
		if (error!=null) return error;

		return RunModelCreatorStatus.ok;
	}
}