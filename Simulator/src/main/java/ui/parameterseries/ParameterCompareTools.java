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
package ui.parameterseries;

import java.awt.Window;

import org.w3c.dom.Document;

import mathtools.NumberTools;
import parser.MathCalcError;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import ui.ModelChanger;
import ui.modeleditor.ModelResource;
import ui.statistics.StatisticViewerFastAccessDialog;

/**
 * Diese Klasse stellt statische Hilfsroutinen für die
 * Parameter-Variationsstudien-Funktion zur Verfügung.
 * @author Alexander Herzog
 */
public class ParameterCompareTools {

	/**
	 * Konstruktor der Klasse.<br>
	 * Diese Klasse kann nicht instanziert werden, sie enthält nur statische Methoden.
	 */
	private ParameterCompareTools() {}

	/**
	 * Zeigt einen Dialog zur Auswahl eines XML-Elements an
	 * @param ownerWindow	Übergeordnetes Fenster
	 * @param xmlDoc	XML-Dokument aus dem ein Element ausgewählt werden soll
	 * @param help	Hilfe-Runnable
	 * @return	Name des XML-Elements oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public static String selectXML(final Window ownerWindow, final Document xmlDoc, final Runnable help) {
		if (xmlDoc==null) return null;
		final StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(ownerWindow,xmlDoc,help,true);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		return dialog.getXMLSelector();
	}

	/**
	 * Berechnet den Wert eines Rechenausdrucks im Kontext eines Modells.
	 * @param model	Modell dessen Daten in dem Rechenausdruck zur Verfügung stehen sollen
	 * @param expression	Rechenausdruck
	 * @return	Liefert entweder einen Zahlenwert oder <code>null</code>, wenn der Ausdruck nicht geparst oder berechnet werden konnte
	 * @see #getModelValue(EditModel, ParameterCompareSetupValueInput)
	 */
	private static Double calcValue(final EditModel model, final String expression) {
		final ExpressionCalc calc=new ExpressionCalc(null,model.userFunctions);
		if (calc.parse(expression)>=0) return null;
		try {
			return calc.calc();
		} catch (MathCalcError e) {
			return null;
		}
	}

	/**
	 * Liefert den aktuellen Wert der Eigenschaft, die durch einen Eingabeparameter beschrieben wird
	 * @param model	Editor-Modell
	 * @param input	Eingabeparameter
	 * @return	Wert der Eigenschaft oder <code>null</code>, wenn der Wert nicht gelesen werden konnte
	 */
	public static Double getModelValue(final EditModel model, final ParameterCompareSetupValueInput input) {
		if (model==null || input==null) return null;

		switch (input.getMode()) {
		case MODE_RESOURCE:
			final ModelResource resource=model.resources.get(input.getTag());
			if (resource==null) return null;
			if (resource.getMode()!=ModelResource.Mode.MODE_NUMBER || resource.getCount()<0) return null;
			return Double.valueOf(resource.getCount());
		case MODE_VARIABLE:
			int index=-1;
			for (int i=0;i<model.globalVariablesNames.size();i++) if (model.globalVariablesNames.get(i).equalsIgnoreCase(input.getTag())) {index=i; break;}
			if (index>=0) return calcValue(model,model.globalVariablesExpressions.get(index));
			return null;
		case MODE_MAP:
			final Object obj=model.globalMapInitial.get(input.getTag());
			if (obj instanceof Integer) return ((Integer)obj).doubleValue();
			if (obj instanceof Long) return ((Long)obj).doubleValue();
			if (obj instanceof Double) return (Double)obj;
			if (obj instanceof String) return NumberTools.getDouble((String)obj);
			return null;
		case MODE_XML:
			final String value=ModelChanger.getValue(model,input.getTag(),input.getXMLMode());
			if (value==null) return null;
			return calcValue(model,value);
		default:
			return null;
		}
	}

	/**
	 * Stellt den Wert einer Eigenschaft, die durch einen Eingabeparameter beschrieben wird, ein.
	 * @param model	Editor-Modell
	 * @param input	Eingabeparameter
	 * @param newValue	Neuer Wert
	 * @return	Gibt im Erfolgsfall das veränderte Modell zurück. Im Fehlerfall entweder eine Fehlermeldung oder <code>null</code>.
	 */
	public static Object setModelValue(final EditModel model, final ParameterCompareSetupValueInput input, final Double newValue) {
		if (model==null|| input==null || newValue==null) return null;
		return ModelChanger.changeModel(model,input.getMode(),input.getTag(),input.getXMLMode(),newValue.doubleValue());
	}
}
