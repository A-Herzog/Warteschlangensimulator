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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.DefaultCompletionProvider;

import ui.expressionbuilder.ExpressionBuilder.ExpressionBuilderSettings;
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
	 * @param expressionBuilderSettings	Einstellungenobjekt für einen ExpressionBuilder-Dialog
	 */
	private void process(final ExpressionBuilder.ExpressionBuilderSettings expressionBuilderSettings) {
		final TreeNode root=ExpressionBuilder.buildTreeData("",expressionBuilderSettings);

		if (root instanceof DefaultMutableTreeNode) {
			final List<Completion> completionList=new ArrayList<>();
			processFolder((DefaultMutableTreeNode)root,completionList);
			autoCompleteProvider.addCompletions(completionList);
		}
	}

	/**
	 * Überträgt alle AutoComplete-Einträge eines Baumordners in die Liste.
	 * @param node	Baumknoten
	 * @param completionList	Liste in der die AutoComplete-Datensätze gesammelt werden sollen
	 */
	private void processFolder(final DefaultMutableTreeNode node, final List<Completion> completionList) {
		final int count=node.getChildCount();
		for (int i=0;i<count;i++) {
			final TreeNode sub=node.getChildAt(i);
			if (sub instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode subNode=(DefaultMutableTreeNode)sub;
				if (subNode.getChildCount()>0) {
					processFolder(subNode,completionList);
				} else {
					final Object userObject=subNode.getUserObject();
					if (userObject instanceof ExpressionBuilder.ExpressionSymbol) {
						final BasicCompletion completion=processSymbol((ExpressionBuilder.ExpressionSymbol)userObject);
						if (completion!=null) completionList.add(completion);
					}
				}
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
		case TYPE_GLOBAL_MAP: return Images.EXPRESSION_BUILDER_GLOBAL_MAP.getIcon();
		case TYPE_VARIABLE: return Images.EXPRESSION_BUILDER_VARIABLE.getIcon();
		default: return Images.EXPRESSION_BUILDER.getIcon();
		}
	}

	/**
	 * Fügt ein Symbol zu der AutoComplete-Liste hinzu.
	 * @param symbol	ExpressionBuilder-Symbol
	 * @return	Neuer AutoComplete-Datensatz (kann <code>null</code> sein, wenn kein AutoComplete-Datensatz erzeugt werden konnte)
	 */
	private BasicCompletion processSymbol(final ExpressionBuilder.ExpressionSymbol symbol) {
		return getAutoCompleteRecord(symbol.name,symbol.description,getIcon(symbol.type),symbol.symbol);
	}

	/**
	 * Fügt einen AutoComplete-Datensatz hinzu
	 * @param shortDescription	Kurzbeschreibung des Befehls
	 * @param summary	Beschreibung des Befehls
	 * @param icon	Icon für den Eintrag
	 * @param command	Einzufügendes Text
	 * @return	Neuer AutoComplete-Datensatz (kann <code>null</code> sein, wenn kein AutoComplete-Datensatz erzeugt werden konnte)
	 */
	private BasicCompletion getAutoCompleteRecord(final String shortDescription, final String summary, final Icon icon, final String command) {
		if (command==null || command.trim().isEmpty()) return null;
		final BasicCompletion completion=new BasicCompletion(autoCompleteProvider,command,shortDescription);

		if (icon!=null) completion.setIcon(icon);
		completion.setShortDescription(shortDescription);
		completion.setSummary(summary);

		return completion;
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
		autoComplete.addAutoCompletionListener(l->{
			for (KeyListener listener: textComponent.getKeyListeners()) {
				/* Listener, die sonst auf Tastatureingaben reagieren und ggf. die Eingabe prüfen, aufrufen */
				final KeyEvent e=new KeyEvent(textComponent,KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,KeyEvent.VK_F24,'\0');
				listener.keyPressed(e);
				listener.keyTyped(e);
				listener.keyReleased(e);
			}
		});
	}

	/**
	 * Fügt AutoComplete-Daten auf Basis eines {@link ExpressionBuilder}-Dialogs
	 * zu einer Eingabezeile hinzu.
	 * @param expressionBuilder	ExpressionBuilder dem die Daten für die AutoComplete-Funktion entnommen werden sollen
	 * @param textComponent	Textfeld zu dem die AutoComplete-Funktion hinzugefügt werden soll
	 */
	public static void process(final ExpressionBuilder expressionBuilder, final JTextComponent textComponent) {
		process(expressionBuilder.getSettings(),textComponent);
	}

	/**
	 * Fügt AutoComplete-Daten auf Basis eines {@link ExpressionBuilder}-Dialogs
	 * zu einer Eingabezeile hinzu.
	 * @param expressionBuilderSettings	Einstellungenobjekt für einen ExpressionBuilder dem die Daten für die AutoComplete-Funktion entnommen werden sollen
	 * @param textComponent	Textfeld zu dem die AutoComplete-Funktion hinzugefügt werden soll
	 */
	public static void process(final ExpressionBuilderSettings expressionBuilderSettings, final JTextComponent textComponent) {
		final ExpressionBuilderAutoComplete autoComplete=new ExpressionBuilderAutoComplete();
		autoComplete.process(expressionBuilderSettings);
		autoComplete.addAutoComplete(textComponent);
	}
}
