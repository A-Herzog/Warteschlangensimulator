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
package ui.script;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextArea;

import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import ui.expressionbuilder.ExpressionBuilder;
import ui.images.Images;
import ui.modeleditor.ModelSurface;

/**
 * Popupmenü-Eintrag zur Ausführung eines Rechenausdruck-Befehls
 * @author Alexander Herzog
 * @see ScriptPopupItemCommandModel
 */
public class ScriptPopupItemExpressionBuilder extends ScriptPopupItem {
	/** Modell, dem die Daten entnommen werden sollen */
	private final EditModel model;
	/** Zeichenfläche aus der die Daten entnommen werden sollen */
	private final ModelSurface mainSurface;
	/** Sind Laufzeitdaten vorhanden? */
	private final boolean hasModelData;
	/** Sind Kunden-Laufzeitdaten vorhanden? */
	private final boolean hasClientData;
	/** Ausgabe als Java- oder als Javascript-Befehl */
	private final ScriptPopup.ScriptMode scriptMode;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param statistics	Statistik-Objekt aus dessen XML-Repräsentation die Daten ausgewählt werden sollen
	 * @param scriptMode	Ausgabe als Java- oder als Javascript-Befehl
	 */
	public ScriptPopupItemExpressionBuilder(final String name, final String hint, final Statistics statistics, final ScriptPopup.ScriptMode scriptMode) {
		super(name,hint,Images.EXPRESSION_BUILDER.getIcon());
		model=statistics.editModel;
		this.mainSurface=statistics.editModel.surface;
		hasModelData=false;
		hasClientData=false;
		this.scriptMode=scriptMode;
	}

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param model	Editor-Modell für das die Daten ausgewählt werden sollen
	 * @param hasModelData	Sind Kunden-Laufzeitdaten vorhanden?
	 * @param hasClientData	Sind Kunden-Laufzeitdaten vorhanden?
	 * @param scriptMode	Ausgabe als Java- oder als Javascript-Befehl
	 */
	public ScriptPopupItemExpressionBuilder(final String name, final String hint, final EditModel model, final boolean hasModelData, final boolean hasClientData, final ScriptPopup.ScriptMode scriptMode) {
		super(name,hint,Images.EXPRESSION_BUILDER.getIcon());
		this.model=model;
		this.mainSurface=model.surface;
		this.hasModelData=hasModelData;
		this.hasClientData=hasClientData;
		this.scriptMode=scriptMode;
	}

	/**
	 * Liefert den Basisbefehl zur Berechnung eines Ausdrucks.
	 * @return	Basisbefehl zur Berechnung eines Ausdrucks
	 */
	private String getBaseCommand() {
		switch (scriptMode) {
		case Javascript:
			return "System.calc(\"%s\")";
		case Java:
			return "sim.getSystem().calc(\"%s\")";
		default:
			return "";
		}
	}

	@Override
	public void insertIntoTextArea(final JTextArea textArea, final Runnable update) {
		final String[] variables;
		final Map<String, String> initialVariables;
		if (hasModelData) {
			variables=mainSurface.getMainSurfaceVariableNames(model.getModelVariableNames(),hasClientData);
			initialVariables=model.getInitialVariablesWithValues();
		} else {
			variables=new String[0];
			initialVariables=new HashMap<>();
		}

		final ExpressionBuilder builder=new ExpressionBuilder(null,"",false,variables,initialVariables,ExpressionBuilder.getStationIDs(mainSurface),ExpressionBuilder.getStationNameIDs(mainSurface),false,!hasModelData,false);
		builder.setVisible(true);
		if (builder.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		final String expression=builder.getExpression();
		insertTextIntoTextArea(textArea,String.format(getBaseCommand(),expression.replace("\"","\\\"")));

		if (update!=null) update.run();
	}

}
