/**
 * Copyright 2021 Alexander Herzog
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
package simulator.editmodel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import simulator.examples.EditModelExamples;
import tools.SetupData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementSubConnect;
import ui.modeleditor.elements.ModelElementVertex;
import ui.modeleditor.fastpaint.Shapes;

/**
 * Diese Klasse zählt wie häufig bestimmte Stationsübergänge auftreten.
 * @see EditModel
 * @author Alexander Herzog
 */
public class EditModelProcessor {
	/**
	 * Dateiname der Datei zum Speichern der Trainingsdaten
	 * @see #setupFile
	 */
	private static final String TRAINING_DATA_FILE="Training.dat";

	/**
	 * Versionskennung für {@link #TRAINING_DATA_FILE}
	 * @see #TRAINING_DATA_FILE
	 */
	private static final int FILE_VERSION=1;

	/**
	 * Datei zum Speichern der Trainingsdaten
	 * @see #TRAINING_DATA_FILE
	 * @see SetupData#getSetupFolder()
	 */
	private final File setupFile;

	/**
	 * Zählung der jeweiligen Folgestationen für alle Stationen
	 */
	private final Map<Class<? extends ModelElementBox>,Map<Class<? extends ModelElementBox>,Integer>> map;

	/**
	 * Häufigste Station-zu-Station Kombinationen
	 */
	private final Map<Class<? extends ModelElementBox>,List<Class<? extends ModelElementBox>>> connections;

	/**
	 * Referenz auf die Instanz dieses Singletons
	 * @see #getInstance()
	 */
	private static EditModelProcessor instance;

	/**
	 * Liefert die Singleton-Instanz dieser Klasse.
	 * @return	Singleton-Instanz dieser Klasse
	 * @see #instance
	 */
	public synchronized static EditModelProcessor getInstance() {
		if (instance==null) instance=new EditModelProcessor();
		return instance;
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt ein Singleton dar und kann nicht instanziert werden.
	 * @see #getInstance()
	 */
	private EditModelProcessor() {
		map=new HashMap<>();
		connections=new HashMap<>();
		setupFile=new File(SetupData.getSetupFolder(),TRAINING_DATA_FILE);
	}

	/**
	 * Lädt die Trainingsdaten aus der Konfigurationsdatei.
	 * @see #setupFile
	 */
	private void loadData() {
		/* Alte Daten löschen */
		map.clear();
		connections.clear();

		/* Gespeicherte Trainingdaten laden? */
		if (setupFile.isFile()) try (DataInputStream input=new DataInputStream(new InflaterInputStream(new FileInputStream(setupFile)))) {
			final int ver=input.read();
			if (ver==FILE_VERSION) {
				final int count1=input.read();
				for (int i1=0;i1<count1;i1++) {
					final String name1=input.readUTF();
					@SuppressWarnings("unchecked")
					final Class<? extends ModelElementBox> box1=(Class<? extends ModelElementBox>)Class.forName(name1);
					Map<Class<? extends ModelElementBox>,Integer> subMap=map.get(box1);
					if (subMap==null) map.put(box1,subMap=new HashMap<>());
					final int count2=input.read();
					for (int i2=0;i2<count2;i2++) {
						final String name2=input.readUTF();
						@SuppressWarnings("unchecked")
						final Class<? extends ModelElementBox> box2=(Class<? extends ModelElementBox>)Class.forName(name2);
						final int value=input.read();
						subMap.put(box2,value);
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			/* Im Fehlerfall gibt's keine geladenen Daten, dann werdem im Folgenden die Beispielmodelle verarbeitet. */
		}

		/* Keine Daten vorhanden? Dann Beispielmodelle als initiale Trainingsdaten verarbeiten. */
		if (map.size()==0) {
			processExampleModels();
			saveData();
		}
	}

	/**
	 * Speichert die Trainingsdaten in der Konfigurationsdatei.
	 * @return	Gibt an, ob das Speichern erfolgreich war
	 * @see #setupFile
	 */
	private boolean saveData() {
		try (DataOutputStream output=new DataOutputStream(new DeflaterOutputStream(new FileOutputStream(setupFile)))) {
			output.write(FILE_VERSION);
			output.write(map.size());
			for (Map.Entry<Class<? extends ModelElementBox>,Map<Class<? extends ModelElementBox>,Integer>> entry: map.entrySet()) {
				output.writeUTF(entry.getKey().getName());
				final Map<Class<? extends ModelElementBox>,Integer> subMap=entry.getValue();
				output.write(subMap.size());
				for (Map.Entry<Class<? extends ModelElementBox>,Integer> subEntry: subMap.entrySet()) {
					output.writeUTF(subEntry.getKey().getName());
					output.write(subEntry.getValue().intValue());
				}
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Löscht alle bisherigen Trainingsdaten.
	 */
	public void reset() {
		map.clear();
		connections.clear();
		processExampleModels();
		saveData();
	}

	/**
	 * Verarbeitet die Beispielmodelle als Trainingsdaten
	 * @see EditModelExamples
	 */
	private void processExampleModels() {
		for (EditModelExamples.Example example: EditModelExamples.getList()) {
			final int index=EditModelExamples.getExampleIndexFromName(example.names[0]);
			processSurface(EditModelExamples.getExampleByIndex(null,index,false).surface);
		}
	}

	/**
	 * Fügt ein Modell zu der Zählung hinzu.
	 * @param model	Zu erfassendes Modell
	 */
	public void processModel(final EditModel model) {
		if (model==null || model.surface.getElementCount()==0) return;
		if (map.size()==0) loadData();
		connections.clear();
		processSurface(model.surface);
		saveData();
	}

	/**
	 * Fügt die Stationen einer Zeichenfläche zu der Zählung hinzu.
	 * @param surface	Zu erfassende Zeichenfläche
	 * @see #processModel(EditModel)
	 */
	private void processSurface(final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			final ModelElementBox box=(ModelElementBox)element;

			if (box instanceof ModelElementEdgeOut) {
				final ModelElementBox next=findNext(((ModelElementEdgeOut)box).getEdgeOut());
				if (next!=null) countConnection(box,next);
			}

			if (box instanceof ModelElementEdgeMultiOut) {
				for (ModelElementEdge edge: ((ModelElementEdgeMultiOut)box).getEdgesOut()) {
					final ModelElementBox next=findNext(edge);
					if (next!=null) countConnection(box,next);
				}
			}

			if (box instanceof ModelElementSub) processSurface(((ModelElementSub)box).getSubSurface());
		}
	}

	/**
	 * Findet die nächste Station nach einer Verbindungskante.
	 * @param edge	Verbindungkante
	 * @return	Folgestation (kann <code>null</code> sein, wenn keine Folgestation gefunden wurde)
	 */
	private ModelElementBox findNext(final ModelElementEdge edge) {
		if (edge==null) return null;

		ModelElement element=edge;
		while (element!=null) {
			if (!(element instanceof ModelElementEdge)) return null;
			element=((ModelElementEdge)element).getConnectionEnd();
			if (element==null) return null;
			if (element instanceof ModelElementVertex) {
				element=((ModelElementVertex)element).getEdgeOut();
				continue;
			}
			if (element instanceof ModelElementBox) return (ModelElementBox)element;
			return null;
		}
		return null;
	}

	/**
	 * Erfasst einen Stationsübergang in {@link #map}.
	 * @param origin	Startstation
	 * @param destination	Zielstation
	 * @see #processSurface(ModelSurface)
	 * @see #map
	 */
	private void countConnection(final ModelElementBox origin, final ModelElementBox destination) {
		if (origin instanceof ModelElementSubConnect) return;
		if (destination instanceof ModelElementSubConnect) return;

		Map<Class<? extends ModelElementBox>,Integer> counter=map.get(origin.getClass());
		if (counter==null) map.put(origin.getClass(),counter=new HashMap<>());

		counter.compute(destination.getClass(),(k,v)->(v==null)?1:v+1);
	}

	/**
	 * Minimale Häufigkeit an Übergängen, damit eine Verbindung empfohlen wird
	 * @see #getConnections()
	 */
	private static final int MIN_COUNT_FOR_CONNECTION=2;

	/**
	 * Liefert eine Zuordnung aller Ausgangsstation-Folgestation-Empfehlungen.
	 * @return	Zuordnung aller Ausgangsstation-Folgestation-Empfehlungen
	 * @see #getNextSuggestion(Class)
	 * @see #MIN_COUNT_FOR_CONNECTION
	 */
	private Map<Class<? extends ModelElementBox>,List<Class<? extends ModelElementBox>>> getConnections() {
		if (connections.size()==0) for (Map.Entry<Class<? extends ModelElementBox>,Map<Class<? extends ModelElementBox>,Integer>> entry1: map.entrySet()) {

			int max1=0;
			int max2=0;
			int max3=0;
			Class<? extends ModelElementBox> key1=null;
			Class<? extends ModelElementBox> key2=null;
			Class<? extends ModelElementBox> key3=null;

			for (Map.Entry<Class<? extends ModelElementBox>,Integer> entry2: entry1.getValue().entrySet()) {
				final int v=entry2.getValue();

				if (v>max1) {
					key3=key2; max3=max2;
					key2=key1; max2=max1;
					key1=entry2.getKey(); max1=v;
				} else {
					if (v>max2) {
						key3=key2; max3=max2;
						key2=entry2.getKey(); max2=v;
					} else {
						if (v>max3) {
							key3=entry2.getKey(); max3=v;
						}
					}
				}
			}

			if (max1>=MIN_COUNT_FOR_CONNECTION) {
				final List<Class<? extends ModelElementBox>> list=new ArrayList<>();
				list.add(key1);
				if (max2>=MIN_COUNT_FOR_CONNECTION) list.add(key2);
				if (max3>=MIN_COUNT_FOR_CONNECTION) list.add(key3);
				connections.put(entry1.getKey(),list);
			}
		}

		return connections;
	}

	/**
	 * Liefert einen Vorschlag für eine Folgestation
	 * @param station	Ausgangsstation
	 * @return	Vorschlag für Folgestation (kann <code>null</code> sein, wenn keine Vorschläge vorhanden sind)
	 */
	public List<Class<? extends ModelElementBox>> getNextSuggestion(final Class<? extends ModelElementBox> station) {
		if (map.size()==0) loadData();
		return getConnections().get(station);
	}

	/**
	 * Erzeugt basierend auf der Klasse ein konkretes Stationsobjekt.
	 * @param station	Klasse für die Station
	 * @return	Stationsobjekt oder <code>null</code>, wenn die Klasse nicht instanziert werden konnte
	 */
	public static ModelElementBox getDummy(final Class<? extends ModelElementBox> station) {
		if (station==null) return null;

		try {
			@SuppressWarnings("unchecked")
			final Constructor<ModelElementBox> constructor=(Constructor<ModelElementBox>)station.getDeclaredConstructor(EditModel.class,ModelSurface.class,Shapes.ShapeType.class);
			return constructor.newInstance(null,null,null);
		} catch (NoSuchMethodException|SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}

		try {
			@SuppressWarnings("unchecked")
			final Constructor<ModelElementBox> constructor=(Constructor<ModelElementBox>)station.getDeclaredConstructor(EditModel.class,ModelSurface.class);
			return constructor.newInstance(null,null);
		} catch (NoSuchMethodException|SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}

		return null;
	}

	/**
	 * Liefert den Namen für die Stationsklasse
	 * @param station	Stationsklasse
	 * @return	Name
	 */
	private String getStationTypeName(final Class<? extends ModelElementBox> station) {
		if (station==null) return "";
		final ModelElementBox box=getDummy(station);
		if (box==null) return station.getSimpleName(); else return box.getTypeName();
	}

	/**
	 * Liefert die aktuellen Trainingsdaten als Text.
	 * @return	Trainingsdaten als Text
	 */
	public List<String> getTrainingData() {
		if (map.size()==0) loadData();

		final List<String> lines=new ArrayList<>();

		final List<Class<? extends ModelElementBox>> sortedKeys1=map.keySet().stream()
				.sorted((key1,key2)->(int)(map.get(key2).values().stream().mapToInt(Integer::intValue).count()-map.get(key1).values().stream().mapToInt(Integer::intValue).count()))
				.collect(Collectors.toList());

		for (Class<? extends ModelElementBox> key1: sortedKeys1) {
			if (lines.size()>0) lines.add("");
			lines.add(getStationTypeName(key1));
			final Map<Class<? extends ModelElementBox>,Integer> value1=map.get(key1);
			final List<Class<? extends ModelElementBox>> sortedKeys2=value1.keySet().stream().sorted((k1,k2)->(value1.get(k2)-value1.get(k1))).collect(Collectors.toList());

			for (Class<? extends ModelElementBox> key2: sortedKeys2) {
				lines.add("  "+getStationTypeName(key2)+": "+value1.get(key2));
			}
		}

		return lines;
	}
}
