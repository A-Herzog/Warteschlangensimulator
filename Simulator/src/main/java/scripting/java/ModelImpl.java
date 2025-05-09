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
package scripting.java;

import java.util.function.Consumer;

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
 * Implementierungsklasse f�r das Interface {@link ModelInterface}
 * @author Alexander Herzog
 * @see SimulationInterface
 * @see SimulationImpl
 */
public class ModelImpl implements ModelInterface {
	/** Abbruch-Status. (Nach einem Abbruch werden keine Simulationsl�ufe mehr ausgef�hrt.) */
	private boolean canceled;
	/** Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen */
	private final Consumer<String> output;
	/** Ausgangsmodell */
	private final EditModel original;
	/** Arbeitskopie des Ausgangsmodells */
	private EditModel model;
	/** Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen) */
	private String editModelPath;
	/** Statistik-Objekt, welches die Simulationsergebnisse sp�ter bereitstellen soll */
	private final StatisticsImpl statisticsConnect;

	/**
	 * Konstruktor der Klasse
	 * @param output	Wird aufgerufen, wenn Meldungen usw. ausgegeben werden sollen
	 * @param original	Ausgangsmodell
	 * @param editModelPath	Pfad zur zugeh�rigen Modelldatei (als Basis f�r relative Pfade in Ausgabeelementen)
	 * @param statisticsConnect	Statistik-Objekt, welches die Simulationsergebnisse sp�ter bereitstellen soll
	 */
	public ModelImpl(final Consumer<String> output, final EditModel original, final String editModelPath, final StatisticsImpl statisticsConnect) {
		canceled=false;
		this.output=output;
		this.original=original;
		this.model=(original==null)?null:original.clone();
		this.editModelPath=editModelPath;
		this.statisticsConnect=statisticsConnect;
	}

	/**
	 * Gibt eine Meldung �ber {@link #output} aus.
	 * @param line	Meldung
	 * @see #output
	 */
	private void addOutput(final String line) {
		if (canceled) return;
		if (output!=null) output.accept(line);
	}

	/**
	 * Stellt das Modell auf den Ausgangszustand zur�ck.
	 */
	@Override
	public void reset() {
		model=original.clone();
	}

	/**
	 * Liefert den Wert eines XML-Objekts
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @return	Bisheriger Wert des XML-Objekts
	 */
	@Override
	public String xml(final String xmlName) {
		if (xmlName==null) return "";
		return ModelChanger.getValue(model,xmlName);
	}

	/**
	 * �ndert einen Wert im Modell
	 * @param xmlName	XML-Bezeichner des zu �ndernden Eintrags
	 * @param xmlChangeMode Art der �nderung (0: Wert, 1: Mittelwert, 2: Standardabweichung, 4-6: Verteilungsparameter 1-4)
	 * @param value	Neuer Wert
	 * @return Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	private boolean set(final String xmlName, final int xmlChangeMode, final double value) {
		if (xmlName==null) return false;

		final Object obj=ModelChanger.changeModel(model,ModelChanger.Mode.MODE_XML,xmlName,xmlChangeMode,value);
		if (obj instanceof String) {
			addOutput((String)obj);
			return false;
		} else {
			model=(EditModel)obj;
			return true;
		}
	}

	/**
	 * Stellt den Wert eines XML-Objektes ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @param value	Neuer Wert des XML-Objektes
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setString(final String xmlName, final String value) {
		if (xmlName==null) return false;

		final Object obj=ModelChanger.changeModel(model,0,xmlName,value);
		if (obj instanceof String) {
			addOutput((String)obj);
			return false;
		} else {
			model=(EditModel)obj;
			return true;
		}
	}

	/**
	 * Stellt den Zahlenwert eines XML-Objektes ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @param value	Neuer Zahlenwert des XML-Objektes
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setValue(final String xmlName, final double value) {
		return set(xmlName,0,value);
	}

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt deren Mittelwert ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @param value	Neuer Mittelwert
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setMean(final String xmlName, final double value) {
		return set(xmlName,1,value);
	}

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt deren Standardabweichung ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @param value	Neue Standardabweichung
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setSD(final String xmlName, final double value) {
		return set(xmlName,2,value);
	}

	/**
	 * Nimmt an, dass ein XML-Objekt eine Verteilung beinhaltet und stellt einen Verteilungsparameter ein
	 * @param xmlName	String, der den XML-Pfad zu dem Datenobjekt enth�lt
	 * @param number	1-basierende Nummer des Verteilungsparameters (1-4)
	 * @param value	Neuer Wert f�r den Parameter
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setDistributionParameter(final String xmlName, final int number, final double value) {
		if (number<1 || number>4) {
			addOutput(String.format(Language.tr("Batch.Parameter.XMLTag.InvalidDistributionParameterNumber"),number));
			return false;
		}
		return set(xmlName,2+number,value);
	}

	/**
	 * Liefert die Anzahl an Bedienern in einer Ressource
	 * @param resourceName	Name der Ressource
	 * @return	Anzahl an Bedienern in einer Ressource oder -1, wenn die Ressource nicht existiert oder nicht �ber eine feste Anzahl an Bedienern definiert ist.
	 */
	@Override
	public int getResourceCount(final String resourceName) {
		if (resourceName==null) return -1;

		final ModelResource resource=model.resources.get(resourceName);
		if (resource==null || resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return -1;
		return resource.getCount();
	}

	/**
	 * Stellt die Anzahl an Bedienern in einer Ressource ein
	 * @param resourceName	Name der Ressource
	 * @param count	Anzahl an Bedienern
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setResourceCount(final String resourceName, final int count) {
		if (resourceName==null) return false;

		final ModelResource resource=model.resources.get(resourceName);
		if (resource==null || resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return false;

		resource.setCount(count);
		return true;
	}

	/**
	 * Liefert den initialen Ausdruck f�r eine globale Variable
	 * @param variableName	Name der globalen Variable
	 * @return	Initialer Ausdruck f�r die globale Variable oder eine leere Zeichenkette, wenn die Variable nicht existiert.
	 */
	@Override
	public String getGlobalVariableInitialValue(final String variableName) {
		if (variableName==null) return "";

		final var globalVariable=model.getGlobalVariableByName(variableName);
		if (globalVariable==null) return "";
		return globalVariable.getExpression();
	}

	/**
	 * Stellt den initialen Ausdruck f�r eine globale Variable ein
	 * @param variableName	Name der globalen Variable
	 * @param expression Neuer initialer Wert f�r die globale Variable
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setGlobalVariableInitialValue(final String variableName, final String expression) {
		if (expression==null || expression.isBlank()) return false;
		if (variableName==null) return false;

		final var globalVariable=model.getGlobalVariableByName(variableName);
		if (globalVariable==null) return false;

		globalVariable.setExpression(expression);
		return true;
	}

	/**
	 * Liefert den initialen Wert f�r einen Eintrag in der globalen Zuordnung
	 * @param variableName	Name des Eintrags
	 * @return	Initialer Wert f�r den Eintrag in der globalen Zuordnung
	 */
	@Override
	public Object getGlobalMapInitialValue(final Object variableName) {
		if (!(variableName instanceof String)) return null;

		return model.globalMapInitial.get(variableName);
	}

	/**
	 * Stellt den initialen Ausdruck f�r einen Eintrag in der globalen Zuordnung ein
	 * @param variableName	Name des Eintrags
	 * @param value Neuer initialer Wert
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich ver�ndert werden konnte.
	 */
	@Override
	public boolean setGlobalMapInitialValue(final Object variableName, final Object value) {
		if (value==null) return false;
		if (!(variableName instanceof String)) return false;

		model.globalMapInitial.put((String)variableName,value);

		return true;
	}

	/**
	 * Setzt den Abbruch-Status. (Nach einem Abbruch werden keine Simulationsl�ufe mehr ausgef�hrt.)
	 */
	@Override
	public void cancel() {
		canceled=true;
	}

	/**
	 * Simuliert das aktuelle Modell.
	 * @return	Gibt <code>true</code> zur�ck, wenn das Modell erfolgreich simuliert werden konnte
	 */
	@Override
	public boolean run() {
		if (canceled) return false;

		if (model.modelLoadData.willChangeModel()) addOutput(Language.tr("ModelLoadData.IncompatibleWarning.ScriptRunner"));
		final StartAnySimulator starter=new StartAnySimulator(model,editModelPath);
		final StartAnySimulator.PrepareError error=starter.prepare();
		if (error!=null) {
			addOutput(error.error);
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
				statisticsConnect.setStatistics(statistics.saveToXMLDocument(),null);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Versucht basierend auf dem Namen einer Station die zugeh�rige ID zu ermitteln
	 * @param surface	Zeichenfl�che auf der (und deren Unterzeichenfl�chen) gesucht werden soll
	 * @param name	Name der Station
	 * @return	Zugeh�rige ID oder -1, wenn keine passende Station gefunden wurde
	 */
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
	 * Versucht basierend auf dem Namen einer Station die zugeh�rige ID zu ermitteln
	 * @param name	Name der Station
	 * @return	Zugeh�rige ID oder -1, wenn keine passende Station gefunden wurde
	 */
	@Override
	public int getStationID(final String name) {
		if (name==null || name.isBlank()) return -1;
		return getStationID(model.surface,name);
	}
}
