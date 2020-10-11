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
 * Fügt die Vergleichsoperatoren in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderCompare {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderCompare#build(DefaultMutableTreeNode, List, String)} zur Verfügung.
	 */
	private ExpressionBuilderCompare() {}


	/**
	 * Erstellt einen neuen Eintrag für die Baumstruktur (fügt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @return	Neuer Eintrag für die Baumstruktur
	 * @see #addCompare(DefaultMutableTreeNode, String, String, String, String)
	 */
	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_FUNCTION));
	}

	/**
	 * Fügt einen Eintrag zur Baumstruktur hinzu
	 * @param group	Gruppe zu der der Eintrag hinzugefügt werden soll
	 * @param filterUpper	Filtertext (kann <code>null</code> sein); ist ein Filtertext angegeben, so wird der Eintrag nur in die Baumstruktur aufgenommen, wenn er zum Filtertext passt
	 * @param name	Name des Eintrags
	 * @param langName	Ausgeschriebener Name des Eintrags
	 * @param langInfo	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 */
	private static void addCompare(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String langName, final String langInfo) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || langName.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(
					langName+" ("+name+")",
					name,
					langInfo));
		}
	}

	/**
	 * Fügt die Rechensymbole in die Baumstruktur eines {@link ExpressionBuilder}-Objektes ein.
	 * @param root	Wurzelelement der Baumstruktur
	 * @param pathsToOpen	Liste der initial auszuklappenden Äste
	 * @param filterUpper	Nur Anzeige der Elemente, die zu dem Filter passen (der Filter kann dabei <code>null</code> sein, was bedeutet "nicht filtern")
	 */
	public static void build(final DefaultMutableTreeNode root, final List<TreePath> pathsToOpen, final String filterUpper) {
		DefaultMutableTreeNode group;

		root.add(group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons")));

		addCompare(group,filterUpper,"<=",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.LessOrEqual"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.LessOrEqual.Info"));

		addCompare(group,filterUpper,">=",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.GreaterOrEqual"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.GreaterOrEqual.Info"));

		addCompare(group,filterUpper,"!=",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.NotEqual"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.NotEqual.Info"));

		addCompare(group,filterUpper,"==",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.Equal"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.Equal.Info"));

		addCompare(group,filterUpper,"<",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.Less"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.Less.Info"));

		addCompare(group,filterUpper,">",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.Greater"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.Comparisons.Greater.Info"));

		/* Zusammengesetzte Vergleiche */

		root.add(group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.SimulationCharacteristics.CompositeComparisons")));

		addCompare(group,filterUpper,"||",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CompositeComparisons.Or"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CompositeComparisons.Or.Info"));

		addCompare(group,filterUpper,"&&",
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CompositeComparisons.And"),
				Language.tr("ExpressionBuilder.SimulationCharacteristics.CompositeComparisons.And.Info"));
	}
}
