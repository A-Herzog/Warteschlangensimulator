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
package ui.statistics.analyticcompare;

import java.util.HashSet;
import java.util.Set;

import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import ui.modeleditor.ModelResources;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.elements.ModelElementAssign;
import ui.modeleditor.elements.ModelElementAssignString;
import ui.modeleditor.elements.ModelElementClientIcon;
import ui.modeleditor.elements.ModelElementCosts;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementCounterBatch;
import ui.modeleditor.elements.ModelElementCounterMulti;
import ui.modeleditor.elements.ModelElementDifferentialCounter;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementOutput;
import ui.modeleditor.elements.ModelElementOutputDB;
import ui.modeleditor.elements.ModelElementOutputDDE;
import ui.modeleditor.elements.ModelElementOutputJS;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementRecord;
import ui.modeleditor.elements.ModelElementSectionEnd;
import ui.modeleditor.elements.ModelElementSectionStart;
import ui.modeleditor.elements.ModelElementSetStatisticsMode;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementStateStatistics;
import ui.modeleditor.elements.ModelElementThroughput;
import ui.modeleditor.elements.ModelElementUserStatistic;

/**
 * Diese Klasse extrahiert aus einem Editor-Modell die Komponenten,
 * die für einen Vergleich mit einem analytischen Modell von Bedeutung sind.
 * @author Alexander Herzog
 */
public class AnalyticModel {
	/** Kundenquelle */
	private ModelElementSource source;
	/** Bedienstation */
	private ModelElementProcess process;
	/** Ausgangselemente */
	private final Set<ModelElementDispose> dispose;
	/** Bedienergruppen */
	private ModelResources resources;

	/**
	 * Konstruktor der Klasse
	 */
	public AnalyticModel() {
		source=null;
		process=null;
		dispose=new HashSet<>();
		resources=null;
		reset();
	}

	/**
	 * Setzt das bereits geladene Modell zurück.
	 */
	public void reset() {
		source=null;
		process=null;
		dispose.clear();
		resources=null;
	}

	/**
	 * Versucht die für den Vergleich relevanten Komponenten aus
	 * einem Editor-Modell auszulesen.
	 * @param model	Editor-Modell aus dem die Komponenten ausgelesen werden sollen
	 * @return	Gibt an, ob die Komponenten erfolgreich extrahiert werden konnten
	 * @see #getSource()
	 * @see #getProcess()
	 */
	public boolean build(final EditModel model) {
		reset();

		if (model==null) return false;

		if (!findStations(model)) return false;
		if (!checkConnections()) {reset(); return false;}
		resources=model.resources;

		return true;
	}

	/**
	 * Versucht die für den Vergleich relevanten Komponenten aus
	 * einem Editor-Modell auszulesen.
	 * @param statistics	Statistik-Objekt bei dem aus dem Editor-Modell die Komponenten ausgelesen werden sollen
	 * @return	Gibt an, ob die Komponenten erfolgreich extrahiert werden könnten
	 * @see #getSource()
	 * @see #getProcess()
	 */
	public boolean build(final Statistics statistics) {
		reset();
		if (statistics==null) return false;
		return build(statistics.editModel);
	}

	/**
	 * Sucht in einem Modell nach Quelle, Bedienstation und Ausgang
	 * @param editModel	Zu betrachtendes Editor-Modell
	 * @return	Liefert <code>true</code>, wenn Stationen aller notwendigen Typen gefunden wurden
	 * @see #build(EditModel)
	 */
	private boolean findStations(final EditModel editModel) {
		for (ModelElement element: editModel.surface.getElements()) {
			if (element instanceof ModelElementSource) {
				if (source!=null) return false;
				source=(ModelElementSource)element;
				continue;
			}
			if (element instanceof ModelElementProcess) {
				if (process!=null) return false;
				process=(ModelElementProcess)element;
				continue;
			}
			if (element instanceof ModelElementDispose) {
				dispose.add((ModelElementDispose)element);
			}
		}

		if (source==null || process==null || dispose.isEmpty()) return false;
		return true;
	}

	/**
	 * Stationstypen, die bei der Bearbeitung in
	 * {@link #checkConnections()} übersprungen werden sollen.
	 * @see #checkConnections()
	 */
	private static Class<?>[] ignoreStations=new Class[] {
			ModelElementCounter.class,
			ModelElementCounterMulti.class,
			ModelElementCounterBatch.class,
			ModelElementAssign.class,
			ModelElementAssignString.class,
			ModelElementClientIcon.class,
			ModelElementCosts.class,
			ModelElementThroughput.class,
			ModelElementStateStatistics.class,
			ModelElementDifferentialCounter.class,
			ModelElementSectionStart.class,
			ModelElementSectionEnd.class,
			ModelElementSetStatisticsMode.class,
			ModelElementOutput.class,
			ModelElementOutputJS.class,
			ModelElementOutputDB.class,
			ModelElementOutputDDE.class,
			ModelElementRecord.class,
			ModelElementUserStatistic.class
	};

	/**
	 * Prüft, ob die gefundenen Stationen in der Form verknüpft sind,
	 * so dass eine analytische Auswertung möglich ist.
	 * @return	Liefert <code>true</code>, wenn die Stationen passend verknüpft sind
	 * @see #build(EditModel)
	 */
	private boolean checkConnections() {
		ModelElementEdge edge;

		edge=source.getEdgeOut();
		if (edge==null) return false;

		while (edge.getConnectionEnd()!=process) {
			final ModelElement next=edge.getConnectionEnd();
			if (!(next instanceof ModelElementEdgeOut)) return false;
			boolean ok=false;
			for (Class<?> cls: ignoreStations) if (cls.isInstance(next)) {ok=true; break;}
			if (!ok) return false;
			edge=((ModelElementEdgeOut)next).getEdgeOut();
			if (edge==null) return false;
		}

		edge=process.getEdgeOutSuccess();
		if (edge==null) return false;

		while (!(edge.getConnectionEnd() instanceof ModelElementDispose)) {
			final ModelElement next=edge.getConnectionEnd();
			if (!(next instanceof ModelElementEdgeOut)) return false;
			boolean ok=false;
			for (Class<?> cls: ignoreStations) if (cls.isInstance(next)) {ok=true; break;}
			if (!ok) return false;
			edge=((ModelElementEdgeOut)next).getEdgeOut();
			if (edge==null) return false;
		}

		if (process.getEdgeOutCancel()!=null) {
			edge=process.getEdgeOutCancel();

			while (!(edge.getConnectionEnd() instanceof ModelElementDispose)) {
				final ModelElement next=edge.getConnectionEnd();
				if (!(next instanceof ModelElementEdgeOut)) return false;
				boolean ok=false;
				for (Class<?> cls: ignoreStations) if (cls.isInstance(next)) {ok=true; break;}
				if (!ok) return false;
				edge=((ModelElementEdgeOut)next).getEdgeOut();
				if (edge==null) return false;
			}
		}

		return true;
	}

	/**
	 * Liefert die Quell-Station für den Vergleich mit dem analytischen Modell
	 * @return	Quell-Station oder <code>null</code>, wenn das Modell nicht verarbeitet werden konnte
	 */
	public ModelElementSource getSource() {
		return source;
	}

	/**
	 * Liefert die Bedienstation für den Vergleich mit dem analytischen Modell
	 * @return	Bedienstation oder <code>null</code>, wenn das Modell nicht verarbeitet werden konnte
	 */
	public ModelElementProcess getProcess() {
		return process;
	}

	/**
	 * Liefert eine Liste mit den in dem Modell vorhandenen Bedienressourcen
	 * @return	Liste mit den in dem Modell vorhandenen Bedienressourcen
	 */
	public ModelResources getResources() {
		return resources;
	}
}
