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
package ui.expressionbuilder;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbol;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbolType;

/**
 * Fügt die Rechenbefehle zur Abfrage von Simulationskenngrößen in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderSimulationData {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderSimulationData#build(DefaultMutableTreeNode, List, boolean, boolean, String)} zur Verfügung.
	 */
	private ExpressionBuilderSimulationData() {}

	/**
	 * Erstellt einen neuen Eintrag für die Baumstruktur (fügt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @return	Neuer Eintrag für die Baumstruktur
	 * @see #addTreeNode(DefaultMutableTreeNode, String, String, String, String)
	 */
	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_SIMDATA));
	}

	/**
	 * Fügt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugefügt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 */
	private static void addTreeNode(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(name,symbol,description));
		}
	}

	/**
	 * Erstellt einen neuen Eintrag für die Baumstruktur (fügt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @return	Neuer Eintrag für die Baumstruktur
	 * @see #addTreeNodeClient(DefaultMutableTreeNode, String, String, String, String)
	 */
	private static DefaultMutableTreeNode getTreeNodeClient(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_CLIENTDATA));
	}

	/**
	 * Fügt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugefügt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 */
	private static void addTreeNodeClient(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNodeClient(name,symbol,description));
		}
	}

	/**
	 * Fügt die Rechensymbole in die Baumstruktur eines {@link ExpressionBuilder}-Objektes ein.
	 * @param root	Wurzelelement der Baumstruktur
	 * @param pathsToOpen	Liste der initial auszuklappenden Äste
	 * @param statisticsOnly Stehen nur Statistikdaten (<code>true</code>) oder auch aktuelle Verlaufsdaten (<code>false</code>) zur Verfügung?
	 * @param hasClientData	Stehen Daten zu einem aktuellen Kunden zur Verfügung?
	 * @param filterUpper	Nur Anzeige der Elemente, die zu dem Filter passen (der Filter kann dabei <code>null</code> sein, was bedeutet "nicht filtern")
	 */
	public static void build(final DefaultMutableTreeNode root, final List<TreePath> pathsToOpen, final boolean statisticsOnly, final boolean hasClientData, final String filterUpper) {
		DefaultMutableTreeNode subgroup, sub;

		/* Simulationskenngrößen */

		final DefaultMutableTreeNode group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics"));

		if (!statisticsOnly) {

			addTreeNode(
					group,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTimeKPI")+" (TNow)",
					"TNow()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTimeKPI.Info"));
			addTreeNode(
					group,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.isWarmUp")+" (isWarmUp)",
					"isWarmUp()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.isWarmUp.Info"));
		}

		/* Simulationskenngrößen -> Wiederholungen */

		if (!statisticsOnly) {

			sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.Repeats"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.Repeats.RepeatCurrent")+" (RepeatCurrent)",
					"RepeatCurrent()",
					Language.tr("ExpressionBuilder.Repeats.RepeatCurrent.Info"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.Repeats.RepeatCount")+" (RepeatCount)",
					"RepeatCount()",
					Language.tr("ExpressionBuilder.Repeats.RepeatCount.Info"));

			if (sub.getChildCount()>0) group.add(sub);

		}

		/* Simulationskenngrößen -> Anzahlen an Kunden */

		subgroup=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.NumberOfClients"));


		/* Simulationskenngrößen -> Anzahlen an Kunden -> Kunden im System */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ClientsInSystem"));
		if (!statisticsOnly) {
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientsInSystem")+" (WIP)",
					"WIP()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientsInSystem.Info"));
		}
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" (WIP_avg)",
				"WIP_avg()",
				Language.tr("ExpressionBuilder.ClientsInSystem.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" (WIP_median)",
				"WIP_median()",
				Language.tr("ExpressionBuilder.ClientsInSystem.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" (WIP_quantil)",
				"WIP_quantil(p)",
				Language.tr("ExpressionBuilder.ClientsInSystem.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" (WIP_min)",
				"WIP_min()",
				Language.tr("ExpressionBuilder.ClientsInSystem.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" (WIP_max)",
				"WIP_max()",
				Language.tr("ExpressionBuilder.ClientsInSystem.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" (WIP_var)",
				"WIP_var()",
				Language.tr("ExpressionBuilder.ClientsInSystem.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" (WIP_std)",
				"WIP_std()",
				Language.tr("ExpressionBuilder.ClientsInSystem.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" (WIP_cv)",
				"WIP_cv()",
				Language.tr("ExpressionBuilder.ClientsInSystem.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" (WIP_scv)",
				"WIP_scv()",
				Language.tr("ExpressionBuilder.ClientsInSystem.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" (WIP_sk)",
				"WIP_sk()",
				Language.tr("ExpressionBuilder.ClientsInSystem.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" (WIP_kurt)",
				"WIP_kurt()",
				Language.tr("ExpressionBuilder.ClientsInSystem.Kurt"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Anzahlen an Kunden -> Wartende Kunden im System */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ClientsInSystemQueue"));
		if (!statisticsOnly) {
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientsInSystemQueue")+" (NQ)",
					"NQ()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientsInSystemQueue.Info"));
		}
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" (NQ_avg)",
				"NQ_avg()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" (NQ_median)",
				"NQ_median()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" (NQ_quantil)",
				"NQ_quantil(p)",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" (NQ_min)",
				"NQ_min()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" (NQ_max)",
				"NQ_max()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" (NQ_var)",
				"NQ_var()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" (NQ_std)",
				"NQ_std()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" (NQ_cv)",
				"NQ_cv()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" (NQ_scv)",
				"NQ_scv()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" (NQ_sk)",
				"NQ_sk()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" (NQ_kurt)",
				"NQ_kurt()",
				Language.tr("ExpressionBuilder.ClientsInSystemQueue.Kurt"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Anzahlen an Kunden -> Kunden im System in Bedienung */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ClientsInSystemProcess"));
		if (!statisticsOnly) {
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientsInSystemProcess")+" (Process)",
					"Process()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientsInSystemProcess.Info"));
		}
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" (Process_avg)",
				"Process_avg()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" (Process_median)",
				"Process_median()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" (Process_quantil)",
				"Process_quantil(p)",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" (Process_min)",
				"Process_min()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" (Process_max)",
				"Process_max()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" (Process_var)",
				"Process_var()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" (Process_std)",
				"Process_std()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" (Process_cv)",
				"Process_cv()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" (Process_scv)",
				"Process_scv()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" (Process_sk)",
				"Process_sk()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" (Process_kurt)",
				"Process_kurt()",
				Language.tr("ExpressionBuilder.ClientsInSystemProcess.Kurt"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Anzahlen an Kunden -> Kunden an Bedienstation / Kunden von bestimmtem Typ im System */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ClientsAtStation"));
		if (!statisticsOnly) {
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" (WIP)",
					"WIP(id)",
					Language.tr("ExpressionBuilder.ClientsAtStation.CurrentNumber"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber.ByType")+" (WIP)",
					"WIP(id1;id2)",
					Language.tr("ExpressionBuilder.ClientsAtStation.CurrentNumber.ByType"));
		}
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" (WIP_avg)",
				"WIP_avg(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" (WIP_median)",
				"WIP_median(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" (WIP_quantil)",
				"WIP_quantil(p;id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" (WIP_min)",
				"WIP_min(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" (WIP_max)",
				"WIP_max(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" (WIP_var)",
				"WIP_var(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" (WIP_std)",
				"WIP_std(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" (WIP_cv)",
				"WIP_cv(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" (WIP_scv)",
				"WIP_scv(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" (WIP_sk)",
				"WIP_sk(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" (WIP_kurt)",
				"WIP_kurt(id)",
				Language.tr("ExpressionBuilder.ClientsAtStation.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" (WIP_hist)",
				"WIP_hist(id;state)",
				Language.tr("ExpressionBuilder.ClientsAtStation.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" (WIP_hist)",
				"WIP_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.ClientsAtStation.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Anzahlen an Kunden -> Kunden in Warteschlange an Bedienstation / Wartende Kunden von bestimmtem Typ */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ClientsAtQueue"));
		if (!statisticsOnly) {
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" (NQ)",
					"NQ(id)",
					Language.tr("ExpressionBuilder.ClientsAtQueue.CurrentNumber"));
		}
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" (NQ_avg)",
				"NQ_avg(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" (NQ_median)",
				"NQ_median(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" (NQ_quantil)",
				"NQ_quantil(p;id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" (NQ_min)",
				"NQ_min(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" (NQ_max)",
				"NQ_max(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" (NQ_var)",
				"NQ_var(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" (NQ_std)",
				"NQ_std(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" (NQ_cv)",
				"NQ_cv(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" (NQ_scv)",
				"NQ_scv(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" (NQ_sk)",
				"NQ_sk(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" (NQ_kurt)",
				"NQ_kurt(id)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" (NQ_hist)",
				"NQ_hist(id;state)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" (NQ_hist)",
				"NQ_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.ClientsAtQueue.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Anzahlen an Kunden -> Kunden in Bedienung an Bedienstation / Kunden in Bedienung von bestimmtem Typ */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.ClientsAtStationService"));

		if (!statisticsOnly) {
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" (Process)",
					"Process(id)",
					Language.tr("ExpressionBuilder.ClientsAtStationService.CurrentNumber"));
		}

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" (Process_avg)",
				"Process_avg(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" (Process_median)",
				"Process_median(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" (Process_quantil)",
				"Process_quantil(p;id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" (Process_min)",
				"Process_min(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" (Process_max)",
				"Process_max(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" (Process_var)",
				"Process_var(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" (Process_std)",
				"Process_std(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" (Process_cv)",
				"Process_cv(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" (Process_scv)",
				"Process_scv(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" (Process_sk)",
				"Process_sk(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" (Process_kurt)",
				"Process_kurt(id)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" (Process_hist)",
				"Process_hist(id;state)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" (Process_hist)",
				"Process_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.ClientsInServiceProcess.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		if (subgroup.getChildCount()>0) group.add(subgroup);

		/* Simulationskenngrößen -> Zeiten an den Stationen */

		subgroup=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TimesAtStations"));

		/* Simulationskenngrößen -> Zeiten an den Stationen -> Wartezeiten an einer Station */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_hist(id;state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesAtStations.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten an den Stationen -> Transferzeiten an einer Station */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_hist(id;state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesAtStations.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten an den Stationen -> Bedienzeiten an einer Station */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_hist(id;state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesAtStations.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten an den Stationen -> Verweilzeiten an einer Station */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_hist(id;state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesAtStations.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		if (subgroup.getChildCount()>0) group.add(subgroup);

		/* Simulationskenngrößen -> Zeiten nach Kundentypen */

		subgroup=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TimesByClientTypes"));

		/* Simulationskenngrößen -> Zeiten nach Kundentypen -> Wartezeiten der Kunden eines Typs */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesByClientTypes.Kurt"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten nach Kundentypen -> Transferzeiten der Kunden eines Typs */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesByClientTypes.Kurt"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten nach Kundentypen -> Bedienzeiten der Kunden eines Typs */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesByClientTypes.Kurt"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten nach Kundentypen -> Verweilzeiten der Kunden eines Typs */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_median(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_quantil(p;id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesByClientTypes.Kurt"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		if (subgroup.getChildCount()>0) group.add(subgroup);

		/* Simulationskenngrößen -> Zeiten über alle Kundentypen und Stationen */

		subgroup=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TimesOverAll"));

		/* Simulationskenngrößen -> Zeiten über alle Kundentypen und Stationen -> Wartezeiten über alle Kundentypen und Stationen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Sum")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sum()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_median()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_quantil(p)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_min()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_max()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_var()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_std()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_cv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_scv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_sk()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_kurt()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_histAll(state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.WaitingTime")+"_histAll(stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.WaitingTimesOverAll.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten über alle Kundentypen und Stationen -> Transferzeiten über alle Kundentypen und Stationen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Sum")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sum()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_median()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_quantil(p)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_min()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_max()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_var()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_std()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_cv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_scv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_sk()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_kurt()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_histAll(state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.TransferTime")+"_histAll(stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransferTimesOverAll.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten über alle Kundentypen und Stationen -> Bedienzeiten über alle Kundentypen und Stationen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Sum")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sum()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_median()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_quantil(p)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_min()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_max()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_var()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_std()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_cv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_scv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_sk()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_kurt()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_histAll(state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.ProcessingTime")+"_histAll(stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ProcessingTimesOverAll.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Zeiten über alle Kundentypen und Stationen -> Verweilzeiten über alle Kundentypen und Stationen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Sum")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sum)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sum()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.CurrentTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.AverageTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_median()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_quantil(p)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_min()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.MinimalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_max()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.MaximalTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_var()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.VarianceOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_std()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.StandardDeviationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_cv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.CoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheTime")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_scv()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.SquaredCoefficientOfVariationOfTheTime"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_sk()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_kurt()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_histAll(state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_histAll)",
				Language.tr("ExpressionBuilder.CommandName.ResidenceTime")+"_histAll(stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResidenceTimesOverAll.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		if (subgroup.getChildCount()>0) group.add(subgroup);

		/* Simulationskenngrößen -> Ressourcen */

		subgroup=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.Resources"));

		/* Simulationskenngrößen -> Ressourcen -> Auslastung einer Ressource */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization"));

		if (!statisticsOnly) {

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.Number")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_count)",
					Language.tr("ExpressionBuilder.CommandName.Resource")+"_count(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.Number"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.DownNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_down)",
					Language.tr("ExpressionBuilder.CommandName.Resource")+"_down(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.DownNumber"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+")",
					Language.tr("ExpressionBuilder.CommandName.Resource")+"(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.CurrentNumber"));

		}

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_hist(id;state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilization.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Ressourcen -> Auslastung über alle Ressourcen */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilizationAll"));

		if (!statisticsOnly) {

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.Number")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_count)",
					Language.tr("ExpressionBuilder.CommandName.Resource")+"_count()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilizationAll.Number"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.DownNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_down)",
					Language.tr("ExpressionBuilder.CommandName.Resource")+"_down()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilizationAll.DownNumber"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+")",
					Language.tr("ExpressionBuilder.CommandName.Resource")+"()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilizationAll.CurrentNumber"));

		}

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilizationAll.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_min()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilizationAll.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Resource")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.Resource")+"_max()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.ResourceUtilizationAll.MaximalNumber"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		if (subgroup.getChildCount()>0) group.add(subgroup);

		/* Simulationskenngrößen -> Transporter */

		subgroup=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.Transporter"));

		/* Simulationskenngrößen -> Transporter -> Auslastung einer der Transporter einer Transportergruppe */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Number")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_count)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_count(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.Number"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Number")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_capacity)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_ccapacity(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.Capacity"));

		if (!statisticsOnly) {

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.DownNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_down)",
					Language.tr("ExpressionBuilder.CommandName.Transporter")+"_down(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.DownNumber"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+")",
					Language.tr("ExpressionBuilder.CommandName.Transporter")+"(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.CurrentNumber"));

		}

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_min(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_max(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_var(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_std(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_cv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_scv(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_sk(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_kurt(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_hist(id;state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_hist(id;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilization.HistogramMultiple"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		/* Simulationskenngrößen -> Transporter -> Auslastung über alle Transporter */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilizationAll"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Number")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_count)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_count()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilizationAll.Number"));

		if (!statisticsOnly) {

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.DownNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_down)",
					Language.tr("ExpressionBuilder.CommandName.Transporter")+"_down()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilizationAll.DownNumber"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+")",
					Language.tr("ExpressionBuilder.CommandName.Transporter")+"()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilizationAll.CurrentNumber"));

		}

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilizationAll.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_min()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilizationAll.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.Transporter")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.Transporter")+"_max()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.TransporterUtilizationAll.MaximalNumber"));

		if (sub.getChildCount()>0) subgroup.add(sub);

		if (subgroup.getChildCount()>0) group.add(subgroup);

		/* Simulationskenngrößen -> Ankünfte und Abgänge an Station */

		if (!statisticsOnly) {

			sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.NumberIn")+" (NumberIn)",
					"NumberIn(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.NumberIn.Info"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.NumberOut")+" (NumberOut)",
					"NumberOut(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.NumberOut.Info"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.ThroughputSystem")+" (Throughput)",
					"Throughput()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.ThroughputSystem.Info"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.Throughput")+" (Throughput)",
					"Throughput(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.Throughput.Info"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.ThroughputMax")+" (ThroughputMax)",
					"ThroughputMax(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.ThroughputMax.Info"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.ThroughputMaxInterval")+" (ThroughputMaxInterval)",
					"ThroughputMaxInterval(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.StationInputOutput.ThroughputMaxInterval.Info"));


			if (sub.getChildCount()>0) group.add(sub);
		}

		/* Simulationskenngrößen -> Zähler */

		if (!statisticsOnly) {

			sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.Counter"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.Counter.Counter")+" (Counter)",
					"Counter(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.Counter.Counter.Info"));
			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.Counter.Part")+" (Part)",
					"Part(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.Counter.Part.Info"));

			if (sub.getChildCount()>0) group.add(sub);

		}

		/* Simulationskenngrößen -> Kosten */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs"));

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingSum")+" (costs_waiting_sum)",
				"costs_waiting_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingSum.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingAvg")+" (costs_waiting_avg)",
				"costs_waiting_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingAvg.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingSumAll")+" (costs_waiting_sum)",
				"costs_waiting_sum()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingSumAll.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingAvgAll")+" (costs_waiting_avg)",
				"costs_waiting_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingAvgAll.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingCurrent")+" (costs_waiting)",
				"costs_waiting()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.WaitingCurrent.Info"));

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferSum")+" (costs_transfer_sum)",
				"costs_transfer_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferSum.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferAvg")+" (costs_transfer_avg)",
				"costs_transfer_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferAvg.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferSumAll")+" (costs_transfer_sum)",
				"costs_transfer_sum()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferSumAll.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferAvgAll")+" (costs_transfer_avg)",
				"costs_transfer_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferAvgAll.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferCurrent")+" (costs_transfer)",
				"costs_transfer()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.TransferCurrent.Info"));

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessSum")+" (costs_process_sum)",
				"costs_process_sum(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessSum.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessAvg")+" (costs_process_avg)",
				"costs_process_avg(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessAvg.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessSumAll")+" (costs_process_sum)",
				"costs_process_sum()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessSumAll.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessAvgAll")+" (costs_process_avg)",
				"costs_process_avg()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessAvgAll.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessCurrent")+" (costs_process)",
				"costs_process()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ProcessCurrent.Info"));

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.Station")+" (costs)",
				"costs(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.Station.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.StationAll")+" (costs)",
				"costs()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.StationAll.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.Resource")+" (costs_resource)",
				"costs_resource(id)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.Resource.Info"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ResourceAll")+" (costs_resource)",
				"costs_resource()",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Costs.ResourceAll.Info"));

		if (sub.getChildCount()>0) group.add(sub);

		/* Simulationskenngrößen -> Nutzerstatistik */

		sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics"));

		if (!statisticsOnly) {

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.CurrentNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+")",
					Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"(id;nr)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.CurrentNumber"));

		}

		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.AverageNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_avg)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_avg(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.AverageNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Median")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_median)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_median(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.Median"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Quantil")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_quantil)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_quantil(id;nr;p)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.Quantil"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MinimalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_min)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_min(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.MinimalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.MaximalNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_max)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_max(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.MaximalNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.VarianceOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_var)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_var(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.VarianceOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.StandardDeviationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_std)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_std(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.StandardDeviationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CoefficientOfVariationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_cv)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_cv(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.CoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.SquaredCoefficientOfVariationOfTheNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_scv)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_scv(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.SquaredCoefficientOfVariationOfTheNumber"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Skewness")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_sk)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_sk(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.Skewness"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Kurt")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_kurt)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_kurt(id;nr)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.Kurt"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramSingle")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_hist(id;nr;state)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.HistogramSingle"));
		addTreeNode(
				sub,
				filterUpper,
				Language.tr("ExpressionBuilder.SimulationCharacteristics.HistogramMultiple")+" ("+Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_hist)",
				Language.tr("ExpressionBuilder.CommandName.UserStatistics")+"_hist(id;nr;stateMin;stateMax)",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.UserStatistics.HistogramMultiple"));

		if (sub.getChildCount()>0) group.add(sub);

		/* Simulationskenngrößen -> Analoge Werte */

		if (!statisticsOnly) {

			sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.AnalogValues"));

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.AnalogCurrentNumber")+" ("+Language.tr("ExpressionBuilder.CommandName.AnalogValue")+")",
					Language.tr("ExpressionBuilder.CommandName.AnalogValue")+"(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.AnalogValues.CurrentNumber"));

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.AnalogCurrentRate")+" ("+Language.tr("ExpressionBuilder.CommandName.AnalogRate")+")",
					Language.tr("ExpressionBuilder.CommandName.AnalogRate")+"(id)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.AnalogValues.Rate"));

			addTreeNode(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.AnalogCurrentValveMaximumFlow")+" ("+Language.tr("ExpressionBuilder.CommandName.ValveMaximumFlow")+")",
					Language.tr("ExpressionBuilder.CommandName.ValveMaximumFlow")+"(id;nr)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.AnalogValues.Valve"));

		}

		if (sub.getChildCount()>0) group.add(sub);

		/* Simulationskenngrößen -> Kundenobjektdaten */

		if (hasClientData) {
			sub=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData"));

			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.isWarmUpClient")+" (isWarmUpClient)",
					"isWarmUpClient()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.isWarmUpClient.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.isClientInStatistics")+" (isClientInStatistics)",
					"isClientInStatistics()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.isClientInStatistics.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ClientNumber")+" (ClientNumber)",
					"ClientNumber()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ClientNumber.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.WaitingTime")+" (w)",
					"w",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.WaitingTime.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.TransferTime")+" (t)",
					"t",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.TransferTime.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ProcessTime")+" (p)",
					"p",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ProcessTime.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ClientData")+" (ClientData)",
					"ClientData(index)",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ClientData.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.Alternative")+" (Alternative)",
					"Alternative()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.Alternative.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.PreviousStation")+" (PreviousStation)",
					"PreviousStation()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.PreviousStation.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.WaitingTimeCurrent")+" (CurrentWaitingTime)",
					"CurrentWaitingTime()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.WaitingTimeCurrent.Info"));
			addTreeNodeClient(
					sub,
					filterUpper,
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ClientBatchSize")+" (ClientBatchSize)",
					"ClientBatchSize()",
					Language.tr("ExpressionBuilder.SimulationCharacteristics.ClientData.ClientBatchSize.Info"));

			if (sub.getChildCount()>0) group.add(sub);
		}

		if (group.getChildCount()>0) {
			root.add(group);
			pathsToOpen.add(new TreePath(group.getPath()));
		}
	}
}
