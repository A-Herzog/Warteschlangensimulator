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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import simulator.simparser.ExpressionCalcUserFunctionsManager;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.images.Images;

/**
 * In diesem Dialog werden alle nutzerdefinieren Rechenfunktionen aufgelistet.
 * Neue Funktionen können angelegt und bestehende Verändert oder gelöscht werden.
 * @author Alexander Herzog
 * @see ExpressionCalcUserFunctionsManager
 */
public class ExpressionCalcUserFunctionsListDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5660928599504646548L;

	/**
	 * Referenz auf das {@link ExpressionCalcUserFunctionsManager}-Singleton
	 */
	private final ExpressionCalcUserFunctionsManager globalFunctionsManager;

	/**
	 * Datenobjekt zur Speicherung der modellspezifischen nutzerdefinierten Funktionen
	 */
	private final ExpressionCalcModelUserFunctions modelFunctionsManager;

	/**
	 * Tab zur Auflistung der globalen nutzerdefinierten Funktionen
	 */
	private final ExpressionCalcUserFunctionsListPanel globalFunctions;

	/**
	 * Tab zur Auflistung der modellspezifischen nutzerdefinierten Funktionen
	 */
	private final ExpressionCalcUserFunctionsListPanel modelFunctions;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell dessen modellspezifische nutzerdefinierte Funktionen bearbeitet werden sollen
	 */
	public ExpressionCalcUserFunctionsListDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("UserDefinedFunctions.ListTitle"));

		globalFunctionsManager=ExpressionCalcUserFunctionsManager.getInstance();
		modelFunctionsManager=model.userFunctions;

		/* GUI */
		final JPanel content=createGUI(768,480,()->Help.topicModal(this,"ExpressionsUser"));
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		/* Tabs */
		tabs.addTab(Language.tr("UserDefinedFunctions.ModeGlobal"),globalFunctions=new ExpressionCalcUserFunctionsListPanel(globalFunctionsManager.getUserFunctions()));
		tabs.addTab(Language.tr("UserDefinedFunctions.ModeModel"),modelFunctions=new ExpressionCalcUserFunctionsListPanel(modelFunctionsManager.getUserFunctions()));
		tabs.setIconAt(0,Images.GENERAL_TOOLS.getIcon());
		tabs.setIconAt(1,Images.MODEL.getIcon());

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,400);
		setSizeRespectingScreensize(768,480);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void storeData() {
		globalFunctionsManager.getUserFunctions().clear();
		globalFunctionsManager.getUserFunctions().addAll(globalFunctions.getUserFunctions());
		globalFunctionsManager.load();

		modelFunctionsManager.getUserFunctions().clear();
		modelFunctionsManager.getUserFunctions().addAll(modelFunctions.getUserFunctions());
	}
}
