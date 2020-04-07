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

import java.util.function.Consumer;

import javax.swing.JMenu;

import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Diese Klasse bietet statische Methoden an,
 * um mögliche Folgestationen per Kontextmenü
 * anbieten zu können.
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

	private static void groupProcess(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!(source instanceof ModelElementProcess)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementProcess(null,null));
		if (!(source instanceof ModelElementDelay)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDelay(null,null));
		if (!(source instanceof ModelElementConveyor)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementConveyor(null,null));
		if (!(source instanceof ModelElementHold)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementHold(null,null));
		if (!(source instanceof ModelElementHoldMulti)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementHoldMulti(null,null));
	}

	private static void groupAssign(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!(source instanceof ModelElementAssign)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementAssign(null,null));
		if (!(source instanceof ModelElementDecide)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDecide(null,null));
		if (!(source instanceof ModelElementDuplicate)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDuplicate(null,null));
	}

	private static void groupCounter(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!(source instanceof ModelElementCounter)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementCounter(null,null));
		if (!(source instanceof ModelElementCounterMulti)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementCounterMulti(null,null));
		if (!(source instanceof ModelElementDifferentialCounter)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDifferentialCounter(null,null));
		if (!(source instanceof ModelElementThroughput)) source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementThroughput(null,null));
	}

	private static void groupDispose(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		source.addNextStationMenuItem(parentMenu,addNextStation,new ModelElementDispose(null,null));
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Quell-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsSource(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Verarbeitungs-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsProcessing(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Zuweisungs-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsAssign(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Verzweigen-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsDecide(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Schranken-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsHold(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Kunden-verbinden-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsBatch(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Transport-Ziel-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsTransportTarget(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Ein-und-Ausgabe-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsData(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}

	/**
	 * Mögliche Folgestationen (in einem Popupmenü) für Analogwert-Stationen
	 * @param source	Ausgangsstation
	 * @param parentMenu	Untermenü des Kontextmenüs, welches die Einträge aufnimmt
	 * @param addNextStation	Callback, das aufgerufen werden kann, wenn ein Element zur Zeichenfläche hinzugefügt werden soll
	 */
	public static void nextStationsAnalog(final ModelElementBox source, final JMenu parentMenu, final Consumer<ModelElementBox> addNextStation) {
		if (!source.canAddEdgeOut()) return;

		groupProcess(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupAssign(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupCounter(source,parentMenu,addNextStation);
		parentMenu.addSeparator();
		groupDispose(source,parentMenu,addNextStation);
	}
}
