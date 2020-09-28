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
package scripting.js;

import language.Language;
import simulator.AnySimulator;
import simulator.StartAnySimulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.ModelChanger;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Stellt das "Model"-Objekt in Javascript-Umgebungen zur Verfügung
 * @author Alexander Herzog
 * @see JSRunComplexScript
 */
public final class JSRunComplexScriptModel extends JSBaseCommand {
	private final JSRunComplexScript runner;

	private boolean canceled=false;

	/**
	 * Konstruktor der Klasse
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 * @param runner	Skript-Ausführungsumgebung, welche auch das Modell vorhält
	 */
	public JSRunComplexScriptModel(final JSOutputWriter output, final JSRunComplexScript runner) {
		super(output);
		this.runner=runner;
	}

	/**
	 * Stellt das Modell auf den Ausgangszustand zurück.
	 */
	public void reset() {
		runner.setChangedModel(null);
	}

	/**
	 * Liefert den Wert eines XML-Objekts
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @return	Bisheriger Wert des XML-Objekts
	 */
	public String xml(final Object xmlName) {
		if (!(xmlName instanceof String)) return "";
		return ModelChanger.getValue(runner.getChangedModel(),(String)xmlName);
	}

	private boolean set(final Object xmlName, final int xmlChangeMode, final double value) {
		if (!(xmlName instanceof String)) return false;

		final Object obj=ModelChanger.changeModel(runner.getChangedModel(),ModelChanger.Mode.MODE_XML,(String)xmlName,xmlChangeMode,value);
		if (obj instanceof String) {
			addOutput((String)obj);
			return false;
		} else {
			runner.setChangedModel((EditModel)obj);
			return true;
		}
	}

	/**
	 * Stellt den Wert eines XML-Objektes ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neuer Wert des XML-Objektes
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	public boolean setString(final Object xmlName, final String value) {
		if (!(xmlName instanceof String)) return false;

		final Object obj=ModelChanger.changeModel(runner.getChangedModel(),0,(String)xmlName,value);
		if (obj instanceof String) {
			addOutput((String)obj);
			return false;
		} else {
			runner.setChangedModel((EditModel)obj);
			return true;
		}
	}

	/**
	 * Stellt den Zahlenwert eines XML-Objektes ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neuer Zahlenwert des XML-Objektes
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	public boolean setValue(final Object xmlName, final double value) {
		return set(xmlName,0,value);
	}

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt deren Mittelwert ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neuer Mittelwert
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	public boolean setMean(final Object xmlName, final double value) {
		return set(xmlName,1,value);
	}

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt deren Standardabweichung ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param value	Neue Standardabweichung
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	public boolean setSD(final Object xmlName, final double value) {
		return set(xmlName,2,value);
	}

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt einen Verteilungsparameter ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enthält
	 * @param number	1-basierende Nummer des Verteilungsparameters (1-4)
	 * @param value	Neuer Wert für den Parameter
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	public boolean setDistributionParameter(final Object xmlName, final int number, final double value) {
		if (number<1 || number>4) {
			addOutput(String.format(Language.tr("Batch.Parameter.XMLTag.InvalidDistributionParameterNumber"),number));
			return false;
		}
		return set(xmlName,2+number,value);
	}

	/**
	 * Liefert die Anzahl an Bedienern in einer Ressource
	 * @param resourceName	Name der Ressource
	 * @return	Anzahl an Bedienern in einer Ressource oder -1, wenn die Ressource nicht existiert oder nicht über eine feste Anzahl an Bedienern definiert ist.
	 */
	public int getResourceCount(final Object resourceName) {
		if (!(resourceName instanceof String)) return -1;

		final EditModel model=runner.getChangedModel();
		final ModelResource resource=model.resources.get((String)resourceName);
		if (resource==null || resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return -1;
		return resource.getCount();
	}

	/**
	 * Stellt die Anzahl an Bedienern in einer Ressource ein
	 * @param resourceName	Name der Ressource
	 * @param count	Anzahl an Bedienern
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	public boolean setResourceCount(final Object resourceName, final int count) {
		if (!(resourceName instanceof String)) return false;

		final EditModel model=runner.getChangedModel().clone();
		final ModelResource resource=model.resources.get((String)resourceName);
		if (resource==null || resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return false;

		resource.setCount(count);
		runner.setChangedModel(model);
		return true;
	}

	/**
	 * Liefert den initialen Ausdruck für eine globale Variable
	 * @param variableName	Name der globalen Variable
	 * @return	Initialer Ausdruck für die globale Variable oder eine leere Zeichenkette, wenn die Variable nicht existiert.
	 */
	public String getGlobalVariableInitialValue(final Object variableName) {
		if (!(variableName instanceof String)) return "";

		final EditModel model=runner.getChangedModel();
		final int index=model.globalVariablesNames.indexOf(variableName);
		if (index<0) return "";
		return model.globalVariablesExpressions.get(index);
	}

	/**
	 * Stellt den initialen Ausdruck für eine globale Variable ein
	 * @param variableName	Name der globalen Variable
	 * @param expression Neuer initialer Wert für die globale Variable
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich verändert werden konnte.
	 */
	public boolean setGlobalVariableInitialValue(final Object variableName, final String expression) {
		if (expression==null || expression.trim().isEmpty()) return false;
		if (!(variableName instanceof String)) return false;

		final EditModel model=runner.getChangedModel().clone();
		final int index=model.globalVariablesNames.indexOf(variableName);
		if (index<0) return false;

		model.globalVariablesExpressions.set(index,expression);
		runner.setChangedModel(model);
		return true;
	}

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden keine Simulationsläufe mehr ausgeführt.)
	 */
	public void cancel() {
		canceled=true;
	}

	/**
	 * Simuliert das aktuelle Modell.
	 * @return	Gibt <code>true</code> zurück, wenn das Modell erfolgreich simuliert werden konnte
	 */
	public boolean run() {
		if (canceled) return false;

		final EditModel editModel=runner.getChangedModel();
		if (editModel.modelLoadData.willChangeModel()) addOutput(Language.tr("ModelLoadData.IncompatibleWarning.ScriptRunner"));
		final StartAnySimulator starter=new StartAnySimulator(editModel);
		final String error=starter.prepare();
		if (error!=null) {
			addOutput(error);
			return false;
		}

		final AnySimulator simulator=starter.start();

		while (simulator.isRunning()) {
			try {Thread.sleep(500);} catch (InterruptedException e) {}
			if (canceled) {
				simulator.cancel();
				break;
			}
		}

		if (!canceled) {
			simulator.finalizeRun();
			final Statistics statistics=simulator.getStatistic();
			if (statistics!=null) {
				runner.setStatistics(statistics);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private int getStationID(final ModelSurface surface, final String name) {
		for (ModelElement element1: surface.getElements()) {
			if (element1.getName().equalsIgnoreCase(name)) return element1.getId();
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2.getName().equalsIgnoreCase(name)) return element2.getId();
			}
		}

		return -1;
	}

	/**
	 * Versucht basierend auf dem Namen einer Station die zugehörige ID zu ermitteln
	 * @param name	Name der Station
	 * @return	Zugehörige ID oder -1, wenn keine passende Station gefunden wurde
	 */
	public int getStationID(final String name) {
		if (name==null || name.trim().isEmpty()) return -1;
		final EditModel editModel=runner.getChangedModel();
		return getStationID(editModel.surface,name);
	}
}
