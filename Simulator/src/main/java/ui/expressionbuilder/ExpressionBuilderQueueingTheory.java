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
 * Fügt Rechenbefehle für analytische Warteschlangenmodelle in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderQueueingTheory {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderQueueingTheory#build(DefaultMutableTreeNode, List, String)} zur Verfügung.
	 */
	private ExpressionBuilderQueueingTheory() {}

	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_DISTRIBUTION));
	}

	private static void addTreeNode(final DefaultMutableTreeNode group, final String filterUpper, final String name, final String symbol, final String description) {
		if (filterUpper==null || name.toUpperCase().contains(filterUpper) || symbol.toUpperCase().contains(filterUpper)) {
			group.add(getTreeNode(name,symbol,description));
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

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.AnalyticalQueueingModels"));

		addTreeNode(
				group,
				filterUpper,
				Language.tr("ExpressionBuilder.AnalyticalQueueingModels.ErlangC")+" (ErlangC)",
				"ErlangC(lambda;mu;nu;c;K;t)",
				Language.tr("ExpressionBuilder.AnalyticalQueueingModels.ErlangC.Info"));
		addTreeNode(
				group,
				filterUpper,
				Language.tr("ExpressionBuilder.AnalyticalQueueingModels.AllenCunneen")+" (AllenCunneen)",
				"AllenCunneen(lambda;mu;cvI;cvS;c;t)",
				Language.tr("ExpressionBuilder.AnalyticalQueueingModels.AllenCunneen.Info"));

		if (group.getChildCount()>0) root.add(group);
	}
}
