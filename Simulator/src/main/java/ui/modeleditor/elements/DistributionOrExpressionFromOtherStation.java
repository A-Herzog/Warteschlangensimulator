/**
 * Copyright 2024 Alexander Herzog
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
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import language.Language;
import simulator.editmodel.EditModel;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Hilfsklasse zum Laden von Zeitdauern-Werten von einer Station
 * in den Editor an einer anderen Station.
 */
public class DistributionOrExpressionFromOtherStation {
	/**
	 * Liste der Zeiten-Objekte, die geladen werden können
	 */
	private final List<Record> records;

	/**
	 * Liste aller Kundentypen im Modell
	 */
	private final String[] clientTypes;

	/**
	 * Konstruktor
	 * @param model	Modell dem die Daten entnommen werden sollen
	 */
	public DistributionOrExpressionFromOtherStation(final EditModel model) {
		clientTypes=model.surface.getClientTypes().toArray(String[]::new);
		records=new ArrayList<>();
		for (var element: model.surface.getElementsIncludingSubModels()) {
			if (element instanceof ModelElementDelay) loadDelay((ModelElementDelay)element);
			if (element instanceof ModelElementProcess) loadProcess((ModelElementProcess)element);
			if (element instanceof ModelElementRelease) loadRelease((ModelElementRelease)element);
		}
	}

	/**
	 * Daten aus einer Verzögerungsstation laden
	 * @param station	Verzögerungsstation
	 */
	private void loadDelay(final ModelElementDelay station) {
		Object obj;

		obj=station.getDelayTime();
		if (obj==null) obj=station.getDelayExpression();
		if (obj!=null) records.add(new Record(UseMode.DELAY,null,null,station,obj));

		for (var clientType: clientTypes) {
			obj=station.getDelayTime(clientType);
			if (obj==null) obj=station.getDelayExpression(clientType);
			if (obj!=null) records.add(new Record(UseMode.DELAY,clientType,null,station,obj));
		}
	}

	/**
	 * Daten aus einer Freigabe-Station laden
	 * @param station	Freigabe-Station
	 */
	private void loadRelease(final ModelElementRelease station) {
		Object obj;
		obj=station.getReleaseDelay().get();
		if (obj!=null) records.add(new Record(UseMode.RELEASE,null,null,station,obj));

		for (var clientType: clientTypes) {
			obj=station.getReleaseDelay().get(clientType);
			if (obj!=null) records.add(new Record(UseMode.RELEASE,clientType,null,station,obj));
		}
	}

	/**
	 * Daten aus einer Bedienstation laden
	 * @param station	Bedienstation
	 */
	private void loadProcess(final ModelElementProcess station) {
		Object obj;

		obj=station.getWorking().get();
		if (obj!=null) records.add(new Record(UseMode.PROCESS_SERVICE,null,null,station,obj));
		for (var clientType: clientTypes) {
			obj=station.getWorking().get(clientType);
			if (obj!=null) records.add(new Record(UseMode.PROCESS_SERVICE,clientType,null,station,obj));
		}

		for (var clientType1: clientTypes) for (var clientType2: clientTypes) {
			obj=station.getSetupTimes().get(clientType1,clientType2);
			if (obj!=null) records.add(new Record(UseMode.PROCESS_SETUP,clientType1,clientType2,station,obj));
		}

		obj=station.getPostProcessing().get();
		if (obj!=null) records.add(new Record(UseMode.PROCESS_POST,null,null,station,obj));
		for (var clientType: clientTypes) {
			obj=station.getPostProcessing().get(clientType);
			if (obj!=null) records.add(new Record(UseMode.PROCESS_POST,clientType,null,station,obj));
		}

		obj=station.getCancel().get();
		if (obj!=null) records.add(new Record(UseMode.PROCESS_CANCEL,null,null,station,obj));
		for (var clientType: clientTypes) {
			obj=station.getCancel().get(clientType);
			if (obj!=null) records.add(new Record(UseMode.PROCESS_CANCEL,clientType,null,station,obj));
		}
	}

	/**
	 * Erzeugt ein Callback zum Aufruf eines Popup-Menüs zur Auswahl von Datenquellen
	 * @param loadData	Callback zum Laden der über das Menü ausgewählten Daten
	 * @return	Callback zum Aufruf des Popup-Menüs
	 */
	public Consumer<JButton> getShowLoadMenu(final Consumer<Record> loadData) {
		return button->{
			final var menu=new JPopupMenu();

			for (var station: records.stream().map(record->record.station).distinct().sorted((b1,b2)->b1.getId()-b2.getId()).toArray(ModelElementBox[]::new)) {
				final var stationRecords=records.stream().filter(record->record.station==station).toArray(Record[]::new);
				if (stationRecords.length==1) {
					menu.add(stationRecords[0].getMenuItem(loadData));
				} else {
					final JMenu sub=new JMenu(stationRecords[0].getStationName());
					menu.add(sub);
					for (var record: stationRecords) sub.add(record.getMenuItem(loadData));
				}
			}

			menu.show(button,0,button.getHeight());
		};
	}

	/**
	 * Um was für ein Zeitdauern-Objekt handelt es sich?
	 */
	private enum UseMode {
		/** Verzögerung an einer Verzögerungsstation */
		DELAY(),
		/** Freigabezeitdauer an einer Freigabe-Station */
		RELEASE(),
		/** Bedienzeit an einer Bedienstation */
		PROCESS_SERVICE(()->Language.tr("Surface.LoadTimes.Mode.Service")),
		/** Rüstzeit an einer Bedienstation */
		PROCESS_SETUP(()->Language.tr("Surface.LoadTimes.Mode.Setup")),
		/** Nachbearbeitungszeit an einer Bedienstation */
		PROCESS_POST(()->Language.tr("Surface.LoadTimes.Mode.PostProcessing")),
		/** Wartezeittoleranz an einer Bedienstation */
		PROCESS_CANCEL(()->Language.tr("Surface.LoadTimes.Mode.Cancel"));

		/**
		 * Callback zum Abruf des Namens des Zeitdauern-Typs (kann <code>null</code> sein)
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Konstruktor des Enum
		 * @param nameGetter	Callback zum Abruf des Namens des Zeitdauern-Typs (kann <code>null</code> sein)
		 */
		UseMode(final Supplier<String> nameGetter) {
			this.nameGetter=nameGetter;
		}

		/**
		 * Konstruktor des Enum
		 */
		UseMode() {
			this(null);
		}

		/**
		 * Liefert den Namen des Zeitdauern-Typs
		 * @return	Name des Zeitdauern-Typs (kann <code>null</code> sein)
		 */
		public String getName() {
			if (nameGetter==null) return null;
			return nameGetter.get();
		}
	}

	/**
	 * Daten für ein einzelnes Zeitdauern-Objekt
	 */
	public static class Record {
		/**
		 * Um was für ein Zeitdauern-Objekt handelt es sich?
		 */
		public final UseMode useMode;

		/**
		 * Kundentyp (oder <code>null</code>, wenn global)
		 */
		public final String type;

		/**
		 * Optionaler zweiter Kundentyp (nur bei Rüstzeiten, sonst <code>null</code>)
		 */
		public final String type2;

		/**
		 * Station
		 */
		public final ModelElementBox station;

		/**
		 * Stations ID
		 */
		public final int id;

		/**
		 * Zeitdauern-Object
		 */
		public final Object data;

		/**
		 * Konstruktor
		 * @param useMode	Um was für ein Zeitdauern-Objekt handelt es sich?
		 * @param type	Kundentyp (oder <code>null</code>, wenn global)
		 * @param type2	Optionaler zweiter Kundentyp (nur bei Rüstzeiten, sonst <code>null</code>)
		 * @param station	Station
		 * @param data	Zeitdauern-Objekt;
		 */
		public Record(final UseMode useMode, final String type, final String type2, final ModelElementBox station, final Object data) {
			this.useMode=useMode;
			this.type=type;
			this.type2=type2;
			this.station=station;
			this.id=station.getId();
			this.data=data;
		}

		/**
		 * Liefert den Anzeigenamen der zugehörigen Station.
		 * @return	Anzeigenamen der zugehörigen Station
		 */
		public String getStationName() {
			final StringBuilder result=new StringBuilder();
			result.append(station.getTypeName());
			if (!station.getName().isBlank()) {
				result.append(" \"");
				result.append(station.getName());
				result.append("\"");
			}
			result.append(" (id=");
			result.append(station.getId());
			result.append(")");

			return result.toString();
		}

		/**
		 * Erstellt einen Kontextmenü-Eintrag für den aktuellen Datensatz.
		 * @param loadData	Callback zum Laden der Daten.
		 * @return	Kontextmenü-Eintrag für den aktuellen Datensatz
		 */
		public JMenuItem getMenuItem(final Consumer<Record> loadData) {
			final StringBuilder text=new StringBuilder();
			text.append(getStationName());
			if (type!=null) {
				text.append(" - ");
				text.append(type);
				if (type2!=null) {
					text.append(" -> ");
					text.append(type2);
				}
			}
			final var typeString=useMode.getName();
			if (typeString!=null) {
				text.append(" - ");
				text.append(typeString);
			}
			final var item=new JMenuItem(text.toString());
			item.addActionListener(e->loadData.accept(this));
			return item;
		}
	}
}
