/**
 * Copyright 2022 Alexander Herzog
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

import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import simulator.simparser.ExpressionCalcUserFunctionsManager;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbol;
import ui.expressionbuilder.ExpressionBuilder.ExpressionSymbolType;

/**
 * Fügt die nutzerdefinierten Rechenbefehle in eine {@link ExpressionBuilder}-Baumstruktur ein.
 * @author Alexander Herzog
 * @see ExpressionBuilder
 */
public class ExpressionBuilderUserFunctions {
	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden.
	 * Sie stellt nur die statische Methode {@link ExpressionBuilderUserFunctions#build(DefaultMutableTreeNode, List, String, ExpressionCalcModelUserFunctions)} zur Verfügung.
	 */
	private ExpressionBuilderUserFunctions() {}

	/**
	 * Erstellt einen neuen Eintrag für die Baumstruktur (fügt diesen aber noch nicht ein)
	 * @param name	Name des Eintrags
	 * @param symbol	Symbol für den Eintrag
	 * @param description	Anzuzeigende Beschreibung wenn der Eintrag ausgewählt wird
	 * @return	Neuer Eintrag für die Baumstruktur
	 * @see #addTreeNode(DefaultMutableTreeNode, String, String, String, String)
	 */
	private static DefaultMutableTreeNode getTreeNode(final String name, final String symbol, final String description) {
		return new DefaultMutableTreeNode(new ExpressionSymbol(name,symbol,description,ExpressionSymbolType.TYPE_USER_FUNCTION));
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
	 * Fügt die Rechensymbole in die Baumstruktur eines {@link ExpressionBuilder}-Objektes ein.
	 * @param root	Wurzelelement der Baumstruktur
	 * @param pathsToOpen	Liste der initial auszuklappenden Äste
	 * @param filterUpper	Nur Anzeige der Elemente, die zu dem Filter passen (der Filter kann dabei <code>null</code> sein, was bedeutet "nicht filtern")
	 * @param modelUserFunctionsManager	Modellspezifische nutzerdefinierte Funktionen (kann <code>null</code> sein)
	 */
	public static void build(final DefaultMutableTreeNode root, final List<TreePath> pathsToOpen, final String filterUpper, final ExpressionCalcModelUserFunctions modelUserFunctionsManager) {
		final List<ExpressionCalcUserFunctionsManager.UserFunction> globalUserFunctions=ExpressionCalcUserFunctionsManager.getInstance().getUserFunctions();
		final List<ExpressionCalcUserFunctionsManager.UserFunction> modelUserFunctions=(modelUserFunctionsManager==null)?List.of():modelUserFunctionsManager.getUserFunctions();
		if (globalUserFunctions.size()==0 && modelUserFunctions.size()==0) return;

		DefaultMutableTreeNode group;

		final String value=Language.tr("ExpressionBuilder.Value");

		group=new DefaultMutableTreeNode(Language.tr("ExpressionBuilder.UserFunctions"));

		/* Globale nutzerdefinierte Funktionen */
		final String globalInfo="<p>"+Language.tr("ExpressionBuilder.UserFunctions.Global")+"</p>";
		for (ExpressionCalcUserFunctionsManager.UserFunction userFunction: globalUserFunctions) {
			final String[] parameters=new String[userFunction.parameterCount];
			for (int i=0;i<parameters.length;i++) parameters[i]="Parameter"+(i+1);
			final String content;
			if (userFunction.mode==ExpressionCalcUserFunctionsManager.UserFunctionMode.EXPRESSION) {
				content="<b>"+userFunction.content+"</b>";
			} else {
				content="<tt><pre>"+userFunction.content+"</pre></tt>";
			}
			final String info=globalInfo+"<p><b>"+userFunction.name+"</b>("+String.join(";",parameters)+"):=<br>"+content+"</p>";
			final String[] values=new String[userFunction.parameterCount];
			Arrays.fill(values,value);
			addTreeNode(group,filterUpper,userFunction.name,userFunction.name+"("+String.join(";",values)+")",info);
		}

		/* Modellspezifische nutzerdefinierte Funktionen */
		final String modelInfo="<p>"+Language.tr("ExpressionBuilder.UserFunctions.Model")+"</p>";
		for (ExpressionCalcUserFunctionsManager.UserFunction userFunction: modelUserFunctions) {
			final String[] parameters=new String[userFunction.parameterCount];
			for (int i=0;i<parameters.length;i++) parameters[i]="Parameter"+(i+1);
			final String content;
			if (userFunction.mode==ExpressionCalcUserFunctionsManager.UserFunctionMode.EXPRESSION) {
				content="<b>"+userFunction.content+"</b>";
			} else {
				content="<tt><pre>"+userFunction.content+"</pre></tt>";
			}
			final String info=modelInfo+"<p><b>"+userFunction.name+"</b>("+String.join(";",parameters)+"):=<br>"+content+"</p>";
			final String[] values=new String[userFunction.parameterCount];
			Arrays.fill(values,value);
			addTreeNode(group,filterUpper,userFunction.name,userFunction.name+"("+String.join(";",values)+")",info);
		}

		if (group.getChildCount()>0) root.add(group);
	}
}