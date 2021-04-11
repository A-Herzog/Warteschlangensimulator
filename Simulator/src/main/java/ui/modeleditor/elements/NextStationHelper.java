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
package ui.modeleditor.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import language.Language;
import simulator.editmodel.EditModelProcessor;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Diese Klasse bietet statische Methoden an,
 * um m�gliche Folgestationen per Kontextmen�
 * anbieten zu k�nnen.
 * @author Alexander Herzog
 * @see ModelElement#addNextStationContextMenuItems
 */
public class NextStationHelper {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse bietet nur statische Methoden an und kann daher nicht instanziert werden.
	 */
	private NextStationHelper() {
	}

	/**
	 * Vorschl�ge f�r Bedienstationen in das Popupmen� einf�gen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	private static void groupProcess(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!(source instanceof ModelElementProcess)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementProcess(null,null));
		if (!(source instanceof ModelElementDelay)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDelay(null,null));
		if (!(source instanceof ModelElementConveyor)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementConveyor(null,null));
		if (!(source instanceof ModelElementHold)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementHold(null,null));
		if (!(source instanceof ModelElementHoldMulti)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementHoldMulti(null,null));
	}

	/**
	 * Vorschl�ge f�r Zuweisungsstationen in das Popupmen� einf�gen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	private static void groupAssign(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!(source instanceof ModelElementAssign)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementAssign(null,null));
		if (!(source instanceof ModelElementDecide)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDecide(null,null));
		if (!(source instanceof ModelElementDuplicate)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDuplicate(null,null));
	}

	/**
	 * Vorschl�ge f�r Z�hlerstationen in das Popupmen� einf�gen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	private static void groupCounter(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!(source instanceof ModelElementCounter)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementCounter(null,null));
		if (!(source instanceof ModelElementCounterMulti)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementCounterMulti(null,null));
		if (!(source instanceof ModelElementDifferentialCounter)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDifferentialCounter(null,null));
		if (!(source instanceof ModelElementThroughput)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementThroughput(null,null));
		if (!(source instanceof ModelElementCounterBatch)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementCounterBatch(null,null));
	}

	/**
	 * Vorschl�ge f�r Ausg�nge in das Popupmen� einf�gen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	private static void groupDispose(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDispose(null,null));
		source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDisposeWithTable(null,null));
	}

	/**
	 * F�gt, wenn vorhanden, m�gliche Folgestationen gem�� Nutzerverhalten in das Men� ein.
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	private static void nextStationLearned(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		final List<Class<? extends ModelElementBox>> classes=EditModelProcessor.getInstance().getNextSuggestion(source.getClass());
		if (classes==null || classes.size()==0) return;

		final List<ModelElementBox> list=new ArrayList<>();

		for (Class<? extends ModelElementBox> cls: classes) {
			final ModelElementBox box=EditModelProcessor.getDummy(cls);
			if (box!=null) list.add(box);
		}

		if (list.size()==0) return;

		JMenuItem item;
		parentMenu.add(item=new JMenuItem("<html><body><b>"+Language.tr("Surface.Popup.AddNextStation.ByTraining")+"</b></body></html>"));
		item.setEnabled(false);
		for (ModelElementBox box: list) source.addNextStationMenuItem(parentMenu,addNextStation,box);
		parentMenu.addSeparator();
		parentMenu.add(item=new JMenuItem("<html><body><b>"+Language.tr("Surface.Popup.AddNextStation.Typical")+"</b></body></html>"));
		item.setEnabled(false);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Quell-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsSource(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Verarbeitungs-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsProcessing(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Zuweisungs-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsAssign(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Verzweigen-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsDecide(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Schranken-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsHold(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Kunden-verbinden-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsBatch(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Transport-Ziel-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsTransportTarget(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Ein-und-Ausgabe-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsData(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * M�gliche Folgestationen (in einem Popupmen�) f�r Analogwert-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermen� des Kontextmen�s, welches die Eintr�ge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfl�che hinzugef�gt werden soll
	 */
	public static void nextStationsAnalog(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		nextStationLearned(source,parentMenu,addNextStation);

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}
}
