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

import javax.swing.Icon;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import ui.images.Images;

/**
 * Fügt AutoComplete-Daten auf Basis eines {@link ExpressionBuilder}-Dialogs
 * zu einer Eingabezeile hinzu.
 * @author Alexander Herzog
 * @see ExpressionBuilderAutoComplete#process(ExpressionBuilder, JTextComponent)
 */
public class ExpressionBuilderAutoComplete {
	/**
	 * System für AutoComplete-Vorschläge
	 */
	private final DefaultCompletionProvider autoCompleteProvider;

	/**
	 * Konstruktor der Klasse
	 */
	private ExpressionBuilderAutoComplete() {
		autoCompleteProvider=new DefaultCompletionProvider();
	}

	/**
	 * Überträgt alle Einträge aus einem ExpressionBuilder-Dialog in eine AutoComplete-Liste.
	 * @param expressionBuilder	ExpressionBuilder-Dialog
	 */
	private void process(final ExpressionBuilder expressionBuilder) {
		final TreeNode root=expressionBuilder.buildTreeData("");
		if (root instanceof DefaultMutableTreeNode) processFolder((DefaultMutableTreeNode)root);
	}

	/**
	 * Überträgt alle AutoComplete-Einträge eines Baumordners in die Liste.
	 * @param node	Baumknoten
	 */
	private void processFolder(final DefaultMutableTreeNode node) {
		for (int i=0;i<node.getChildCount();i++) {
			final TreeNode sub=node.getChildAt(i);
			if (sub instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode subNode=(DefaultMutableTreeNode)sub;
				if (subNode.getChildCount()>0) processFolder(subNode);
				if (subNode.getUserObject() instanceof ExpressionBuilder.ExpressionSymbol) processSymbol((ExpressionBuilder.ExpressionSymbol)subNode.getUserObject());
			}
		}
	}

	/**
	 * Ermittelt das Icon für einen AutoComplete-Datensatz.
	 * @param symbolType	Symboltyp
	 * @return	Icon
	 */
	private Icon getIcon(final ExpressionBuilder.ExpressionSymbolType symbolType) {
		switch (symbolType) {
		case TYPE_CLIENTDATA: return Images.EXPRESSION_BUILDER_CLIENT_DATA.getIcon();
		case TYPE_CONST: return Images.EXPRESSION_BUILDER_CONST.getIcon();
		case TYPE_DISTRIBUTION: return Images.EXPRESSION_BUILDER_DISTRIBUTION.getIcon();
		case TYPE_FUNCTION: return Images.EXPRESSION_BUILDER_FUNCTION.getIcon();
		case TYPE_SIMDATA: return Images.EXPRESSION_BUILDER_SIMDATA.getIcon();
		case TYPE_STATION_ID: return Images.EXPRESSION_BUILDER_STATION_ID.getIcon();
		case TYPE_VARIABLE: return Images.EXPRESSION_BUILDER_VARIABLE.getIcon();
		default: return Images.EXPRESSION_BUILDER.getIcon();
		}
	}

	/**
	 * Erstellt das einzufügende Symbol aus dem Symboltext.
	 * @param symbolText	Symboltext
	 * @return	Einzufügende Zeichenkette
	 */
	private String getCommand(final String symbolText) {
		/*
		final int index=symbolText.indexOf('(');
		if (index<0) return symbolText;
		return symbolText.substring(0,index+1);
		 */
		return symbolText;
	}

	/**
	 * Fügt ein Symbol zu der AutoComplete-Liste hinzu.
	 * @param symbol	ExpressionBuilder-Symbol
	 */
	private void processSymbol(final ExpressionBuilder.ExpressionSymbol symbol) {
		addAutoCompleteRecord(symbol.toString(),symbol.description,getIcon(symbol.type),getCommand(symbol.symbol));
	}

	/**
	 * Fügt einen AutoComplete-Datensatz hinzu
	 * @param shortDescription	Kurzbeschreibung des Befehls
	 * @param summary	Beschreibung des Befehls
	 * @param icon	Icon für den Eintrag
	 * @param command	Einzufügendes Text
	 */
	private void addAutoCompleteRecord(final String shortDescription, final String summary, final Icon icon, final String command) {
		if (command==null || command.trim().isEmpty()) return;
		final BasicCompletion completion=new BasicCompletion(autoCompleteProvider,command,shortDescription);

		if (icon!=null) completion.setIcon(icon);
		completion.setShortDescription(shortDescription);
		completion.setSummary(summary);
		autoCompleteProvider.addCompletion(completion);
	}

	/**
	 * Fügt die AutoComplete-Funktion zu einem Textfeld hinzu.
	 * @param textComponent	Textfeld das AutoComplete-Daten erhalten soll
	 */
	private void addAutoComplete(final JTextComponent textComponent) {
		final AutoCompletion autoComplete=new AutoCompletion(autoCompleteProvider);
		autoComplete.setListCellRenderer(new CompletionCellRenderer());
		autoComplete.setShowDescWindow(true);
		autoComplete.install(textComponent);
	}

	/**
	 * Fügt AutoComplete-Daten auf Basis eines {@link ExpressionBuilder}-Dialogs
	 * zu einer Eingabezeile hinzu.
	 * @param expressionBuilder	ExpressionBuilder dem die Daten für die AutoComplete-Funktion entnommen werden sollen
	 * @param textComponent	Textfeld zu dem die AutoComplete-Funktion hinzugefügt werden soll
	 */
	public static void process(final ExpressionBuilder expressionBuilder, final JTextComponent textComponent) {
		final ExpressionBuilderAutoComplete autoComplete=new ExpressionBuilderAutoComplete();
		autoComplete.process(expressionBuilder);
		autoComplete.addAutoComplete(textComponent);
	}
}
