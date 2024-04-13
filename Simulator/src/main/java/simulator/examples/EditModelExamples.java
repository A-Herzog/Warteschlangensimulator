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
package simulator.examples;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.w3c.dom.Document;

import language.Language;
import mathtools.Table;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelDark;
import systemtools.MsgBox;
import tools.SetupData;
import ui.EditorPanel;
import ui.commandline.CommandBenchmark;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementAnimationBar;
import ui.modeleditor.elements.ModelElementAnimationBarChart;
import ui.modeleditor.elements.ModelElementAnimationBarStack;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementAnimationRecord;
import ui.modeleditor.elements.ModelElementSub;
import ui.tools.FlatLaFHelper;

/**
 * Liefert Beispielmodelle
 * @author Alexander Herzog
 */
public class EditModelExamples {
	/**
	 * Kategorien für die Beispiele
	 */
	public enum ExampleType {
		/**
		 * Standardbeispiele
		 */
		TYPE_DEFAULT,

		/**
		 * Beispiele, die sich auf reale Modelle bzw. Fragen beziehen
		 */
		TYPE_REAL_MODELS,

		/**
		 * Beispiele, die bestimmte Modellierungseigenschaften verdeutlichen
		 */
		TYPE_PROPERTIES,

		/**
		 * Beispiele zum Vergleich verschiedener Steuerungsstrategien
		 */
		TYPE_COMPARE,

		/**
		 * Beispiele, die mathematische Zusammenhänge verdeutlichen
		 */
		TYPE_MATH
	}

	/**
	 * Schlüsselwörter für die Beispiele
	 */
	public enum ExampleKeyWord {
		/**
		 * Batch-Ankünfte oder -Bedienungen
		 */
		BATCH(()->Language.tr("Examples.KeyWords.Batch")),

		/**
		 * Prioritäten (inkl. FIFO/LIFO)
		 */
		PRIORITIES(()->Language.tr("Examples.KeyWords.Priorities")),

		/**
		 * Routing der Kunden
		 */
		ROUTING(()->Language.tr("Examples.KeyWords.Routing")),

		/**
		 * Zeitpläne (sowohl für Ankünfte als auch Schichtpläne)
		 */
		SCHEDULES(()->Language.tr("Examples.KeyWords.Schedules")),

		/**
		 * Push- und Pull-Strategien
		 */
		PUSH_PULL(()->Language.tr("Examples.KeyWords.PushPull")),

		/**
		 * Transporte
		 */
		TRANSPORT(()->Language.tr("Examples.KeyWords.Transport")),

		/**
		 * Darstellung mathematischer Zusammenhänge
		 */
		MATH(()->Language.tr("Examples.KeyWords.Math"));

		/**
		 * Callback zum Abruf des Namens in der aktuellen Sprache
		 */
		private final Supplier<String> nameGetter;

		/**
		 * Konstruktor des Enum
		 * @param nameGetter	Callback zum Abruf des Namens in der aktuellen Sprache
		 */
		ExampleKeyWord(final Supplier<String> nameGetter) {
			this.nameGetter=nameGetter;
		}

		/**
		 * Liefert den Namen es Enum-Eintrags.
		 * @return	Name es Enum-Eintrags
		 */
		public String getName() {
			return nameGetter.get();
		}

		/**
		 * Liefert eine Liste mit allen Schlüsselwörtern und dem Begriff "Alle" der Liste vorangestellt.
		 * @return	Liste mit allen Schlüsselwörtern zzgl. "Alle"
		 */
		public static String[] getNames() {
			final List<String> list=Stream.of(values()).map(keyWord->keyWord.getName()).collect(Collectors.toList());
			list.add(0,Language.tr("Examples.KeyWords.All"));
			return list.toArray(new String[0]);
		}
	}

	/**
	 * Instanz dieses Singleton
	 * @see #getInstance()
	 */
	private static EditModelExamples instance;

	/**
	 * Liste mit den Beispielen.
	 * @see #addExample(String[], String, ExampleType, ExampleKeyWord...)
	 */
	private final List<Example> list;

	/**
	 * Sprache für Menüeinträge in der aktuellen Instanz
	 */
	private final String language;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Hilfsroutinen zur Verfügung und kann nicht instanziert werden.
	 */
	private EditModelExamples() {
		language=Language.getCurrentLanguage();
		list=new ArrayList<>();
		addExamples();
	}

	/**
	 * Liefert die Instanz dieser Singleton-Klasse für die Verwendung in den statischen Methoden.
	 * @return	Instanz dieser Singleton-Klasse
	 * @see #instance
	 */
	private static EditModelExamples getInstance() {
		if (instance==null || !instance.language.equals(Language.getCurrentLanguage())) instance=new EditModelExamples();
		return instance;
	}

	/**
	 * Liefert den Namen des Beispielmodells das für Benchmarks verwendet werden soll.
	 * @return	Benchmark-Beispielmodell
	 * @see CommandBenchmark
	 */
	public static String getBenchmarkExampleName() {
		return Language.tr("Examples.SystemDesign");
	}

	/**
	 * Liefert den Namen einer Gruppe
	 * @param group	Gruppe deren Name bestimmt werden soll
	 * @return	Name der angegebenen Gruppe
	 */
	public static String getGroupName(final ExampleType group) {
		switch (group) {
		case TYPE_DEFAULT: return Language.tr("Examples.Type.Simple");
		case TYPE_PROPERTIES: return Language.tr("Examples.Type.Properties");
		case TYPE_COMPARE: return Language.tr("Examples.Type.Compare");
		case TYPE_REAL_MODELS: return Language.tr("Examples.Type.Real");
		case TYPE_MATH: return Language.tr("Examples.Type.Mathematics");
		default: return Language.tr("Examples.Type.Unknown");
		}
	}

	/**
	 * Liefert die Namen der Beispiele in einer bestimmten Gruppe
	 * @param group	Gruppe für die die Beispiele aufgelistet werden sollen
	 * @return	Liste der Namen der Beispiele in der gewählten Gruppe
	 */
	public static List<String> getExampleNames(final ExampleType group) {
		return getExampleNames(group,null,null);
	}

	/**
	 * Liefert die Namen der Beispiele in einer bestimmten Gruppe
	 * @param group	Gruppe für die die Beispiele aufgelistet werden sollen
	 * @param keyWord	Zusätzliches Schlüsselwort, welches ein Beispiel beinhalten muss, um zurückgeliefert zu werden (darf <code>null</code> sein, dann ist die Schlüsselwort-Filterung inaktiv)
	 * @param searchString	Suchbegriff (kann <code>null</code> oder leer sein, dann ist die Suchbegriff-Filterung inaktiv)
	 * @return	Liste der Namen der Beispiele in der gewählten Gruppe
	 */
	public static List<String> getExampleNames(final ExampleType group, final ExampleKeyWord keyWord, final String searchString) {
		final EditModelExamples examples=getInstance();
		return examples.list.stream().filter(example->example.match(group,keyWord,searchString)).map(example->example.names[0]).collect(Collectors.toList());
	}

	/**
	 * Fügt ein Beispiel zu der Liste der Beispiele hinzu
	 * @param names	Namen für das Beispiel in den verschiedenen Sprachen
	 * @param fileNameBase	Beispieldateiname (ohne Erweiterung)
	 * @param type	Gruppe in die das Beispiel fällt
	 * @param keyWords	Optionale Schlüsselwörter für das Beispiel
	 */
	private void addExample(final String[] names, final String fileNameBase, final ExampleType type, final ExampleKeyWord... keyWords) {
		list.add(new Example(names,fileNameBase+".xml",fileNameBase+".txt",type,new HashSet<>(Arrays.asList(keyWords))));
	}

	/**
	 * Fügt alle Beispiele zu der Liste der Beispiele hinzu.
	 * @see #list
	 */
	private void addExamples() {
		/* Standardbeispiele */
		addExample(Language.trAll("Examples.ErlangC"),"ErlangC1",ExampleType.TYPE_DEFAULT);
		addExample(Language.trAll("Examples.Starter"),"Starter",ExampleType.TYPE_DEFAULT);

		/* Beispiele, die sich auf reale Modelle bzw. Fragen beziehen */
		addExample(Language.trAll("Examples.Callcenter"),"Callcenter",ExampleType.TYPE_REAL_MODELS,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.Restaurant"),"Restaurant",ExampleType.TYPE_REAL_MODELS);
		addExample(Language.trAll("Examples.Baustellenampel"),"Baustellenampel",ExampleType.TYPE_REAL_MODELS);
		addExample(Language.trAll("Examples.EmergencyDepartment"),"EmergencyDepartment",ExampleType.TYPE_REAL_MODELS);
		addExample(Language.trAll("Examples.MultiStageProduction"),"MultiStageProduction",ExampleType.TYPE_REAL_MODELS);

		/* Beispiele, die bestimmte Modellierungseigenschaften verdeutlichen */
		addExample(Language.trAll("Examples.ClientTypePriorities"),"Kundentypen",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.PRIORITIES);
		addExample(Language.trAll("Examples.ImpatientClientsAndRetry"),"Warteabbrecher",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.SharedResources"),"SharedResources",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.LimitedNumberOfClientsAtAStation"),"Variable",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.OperatorsAsSimulationObjects"),"BedienerAlsSimulationsobjekte",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Transport"),"Transport",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.TRANSPORT);
		addExample(Language.trAll("Examples.Transporter"),"Transporter",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.TRANSPORT);
		addExample(Language.trAll("Examples.CombiningOrdersAndItems"),"MultiSignalBarrier",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Batch"),"Batch",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.BATCH);
		addExample(Language.trAll("Examples.Failure"),"Failure",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.SetUpTimes"),"SetUpTimes",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Rework"),"Rework",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.HoldJS"),"HoldJS",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.RestrictedBuffer"),"RestriktierterPuffer",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.Analog"),"Analog",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Jockeying"),"Jockeying",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.QueueingDiscipline"),"QueueingDiscipline",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Shiftplan"),"Shiftplan",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.SCHEDULES);
		addExample(Language.trAll("Examples.ArrivalAndServiceBatch"),"ArrivalAndServiceBatch",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.BatchTransport"),"BatchTransport",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.BATCH,ExampleKeyWord.TRANSPORT);
		addExample(Language.trAll("Examples.IntervalInterArrivalTimes"),"IntervalInterArrivalTimes",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.SCHEDULES);
		addExample(Language.trAll("Examples.ClosedQueueingNetwork"),"ClosedQueueingNetwork",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.WorkerWakeUp"),"WorkerWakeUp",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.SCHEDULES);
		addExample(Language.trAll("Examples.WaitingTimeTolerancesDistribution"),"WaitingTimeTolerancesDistribution",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.MATH);
		addExample(Language.trAll("Examples.BatchMultiClientType"),"BatchMultiClientType",ExampleType.TYPE_PROPERTIES,ExampleKeyWord.BATCH);

		/* Beispiele zum Vergleich verschiedener Steuerungsstrategien */
		addExample(Language.trAll("Examples.SystemDesign"),"Vergleiche2",ExampleType.TYPE_COMPARE,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.SystemDesignWithControl"),"Vergleiche3",ExampleType.TYPE_COMPARE,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.PushAndPullProduction"),"PushPull",ExampleType.TYPE_COMPARE,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.PushAndPullProductionMultiBarriers"),"PushPullMulti",ExampleType.TYPE_COMPARE,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.PushPullThroughput"),"PushPullThroughput",ExampleType.TYPE_COMPARE,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.ChangeResourceCountCompare"),"ChangeResourceCountCompare",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.DelayJS"),"DelayJS",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.ParallelSerial"),"ParallelSerial",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.FIFO-LIFO-Switch"),"FIFO-LIFO-Switch",ExampleType.TYPE_COMPARE,ExampleKeyWord.PRIORITIES);
		addExample(Language.trAll("Examples.SetUpTimeReduction"),"SetUpTimeReduction",ExampleType.TYPE_COMPARE,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.DurchlaufzeitenVersusDurchsatz"),"DurchlaufzeitenVersusDurchsatz",ExampleType.TYPE_COMPARE,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.EconomyOfScale"),"EconomyOfScale",ExampleType.TYPE_COMPARE,ExampleKeyWord.PRIORITIES);
		addExample(Language.trAll("Examples.LocalVersusGlobalWarehouse"),"LocalVersusGlobalWarehouse",ExampleType.TYPE_COMPARE,ExampleKeyWord.PUSH_PULL);
		addExample(Language.trAll("Examples.SplitAndJoin"),"SplitAndJoin",ExampleType.TYPE_COMPARE,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.ClientTypesAndSkills"),"ClientTypesAndSkills",ExampleType.TYPE_COMPARE,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.SJF-LJF"),"SJF-LJF",ExampleType.TYPE_COMPARE,ExampleKeyWord.ROUTING);
		addExample(Language.trAll("Examples.MultiServiceSpeed"),"MultiServiceSpeed",ExampleType.TYPE_COMPARE,ExampleKeyWord.PRIORITIES);
		addExample(Language.trAll("Examples.MinimumWaitingTimes"),"MinimumWaitingTimes",ExampleType.TYPE_COMPARE,ExampleKeyWord.PRIORITIES,ExampleKeyWord.ROUTING);

		/* Beispiele, die mathematische Zusammenhänge verdeutlichen */
		addExample(Language.trAll("Examples.LawOfLargeNumbers"),"GesetzDerGroßenZahlen",ExampleType.TYPE_MATH,ExampleKeyWord.MATH);
		addExample(Language.trAll("Examples.Galton"),"Galton",ExampleType.TYPE_MATH,ExampleKeyWord.MATH);
		addExample(Language.trAll("Examples.CoefficientOfVariation"),"CoefficientOfVariation",ExampleType.TYPE_MATH,ExampleKeyWord.MATH);
		addExample(Language.trAll("Examples.PASTA"),"PASTA",ExampleType.TYPE_MATH,ExampleKeyWord.MATH);
		addExample(Language.trAll("Examples.ZentralerGrenzwertsatz"),"ZentralerGrenzwertsatz",ExampleType.TYPE_MATH,ExampleKeyWord.MATH);
		addExample(Language.trAll("Examples.BusStoppParadoxon"),"BusStoppParadoxon",ExampleType.TYPE_MATH,ExampleKeyWord.MATH);
		addExample(Language.trAll("Examples.RandomNumberGenerators"),"RandomNumberGenerators",ExampleType.TYPE_MATH,ExampleKeyWord.MATH);
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Beispielen.
	 * @return	Liste mit allen verfügbaren Beispielen
	 */
	public static String[] getExamplesList() {
		final EditModelExamples examples=getInstance();

		return examples.list.stream().map(example->example.names[0]).toArray(String[]::new);
	}

	/**
	 * Liefert den Index des Beispiels basieren auf dem Namen
	 * @param name	Name des Beispiels zu dem der Index bestimmt werden soll
	 * @return	Index des Beispiels oder -1, wenn es kein Beispiel mit diesem Namen gibt
	 * @see #getExampleByIndex(Component, int, boolean)
	 */
	public static int getExampleIndexFromName(final String name) {
		if (name==null || name.isEmpty()) return -1;
		final EditModelExamples examples=getInstance();

		for (int i=0;i<examples.list.size();i++) {
			for (String test: examples.list.get(i).names) if (name.trim().equalsIgnoreCase(test)) return i;
		}
		return -1;
	}

	/**
	 * Liefert ein bestimmtes Beispiel über seine Nummer aus der Namesliste (<code>getExamplesList()</code>)
	 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben.
	 * @param index	Index des Beispiels, das zurückgeliefert werden soll
	 * @param dark	Modell für den Dark-Modus aufbereiten?
	 * @return	Beispiel oder <code>null</code>, wenn sich der Index außerhalb des gültigen Bereichs befindet
	 * @see EditModelExamples#getExamplesList()
	 */
	public static EditModel getExampleByIndex(final Component owner, final int index, final boolean dark) {
		final EditModelExamples examples=getInstance();

		if (index<0 || index>=examples.list.size()) return null;
		return examples.list.get(index).getModel(owner,dark);
	}

	/**
	 * Liefert den Beschreibungstext für ein bestimmtes Beispiel über seine Nummer aus der Namesliste (<code>getExamplesList()</code>)
	 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben.
	 * @param index	Index des Beispiels, das zurückgeliefert werden soll
	 * @return	Beschreibungstext für das Beispiel oder <code>null</code>, wenn sich der Index außerhalb des gültigen Bereichs befindet oder keine Beschreibung vorhanden ist
	 * @see EditModelExamples#getExamplesList()
	 */
	public static String getExampleInfoByIndex(final Component owner, final int index) {
		final EditModelExamples examples=getInstance();
		if (index<0 || index>=examples.list.size()) return null;
		return examples.list.get(index).getInfo();
	}

	/**
	 * Stellt einen Farbverlauf für die Zeichenflächendiagramme ein.
	 * @param surface	Zeichenfläche auf der die Diagramme aktualisiert werden sollen
	 */
	private static void processDiagramColors(final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementSub) processDiagramColors(((ModelElementSub)element).getSubSurface());
			if (element instanceof ModelElementAnimationLineDiagram) processLineDiagramColors((ModelElementAnimationLineDiagram)element);
			if (element instanceof ModelElementAnimationBar) processBarColors((ModelElementAnimationBar)element);
			if (element instanceof ModelElementAnimationBarStack) processBarStackColors((ModelElementAnimationBarStack)element);
			if (element instanceof ModelElementAnimationBarChart) processBarChartColors((ModelElementAnimationBarChart)element);
			if (element instanceof ModelElementAnimationRecord) processRecordColors((ModelElementAnimationRecord)element);
		}
	}

	/**
	 * Bisherige, durch einen Farbverlauf zu ersetzende Diagrammhintergrundfarbe
	 * @see #processLineDiagramColors(ModelElementAnimationLineDiagram)
	 * @see #processBarColors(ModelElementAnimationBar)
	 * @see #processBarStackColors(ModelElementAnimationBarStack)
	 * @see #processBarChartColors(ModelElementAnimationBarChart)
	 * @see #processRecordColors(ModelElementAnimationRecord)
	 */
	private static final Color DEFAULT_DIAGRAM_BACKGROUND_COLOR=new Color(240,240,240);

	/**
	 * Stellt statt der Standardhintergrundfarbe einen Farbverlauf in einem Zeichenflächen-Liniendiagramm ein.
	 * @param element	Zeichenflächen-Liniendiagramm
	 */
	private static void processLineDiagramColors(final ModelElementAnimationLineDiagram element) {
		if (DEFAULT_DIAGRAM_BACKGROUND_COLOR.equals(element.getBackgroundColor()) && element.getGradientFillColor()==null) {
			element.setBackgroundColor(Color.WHITE);
			element.setGradientFillColor(new Color(230,230,250));
		}
	}

	/**
	 * Stellt statt der Standardhintergrundfarbe einen Farbverlauf in einem Zeichenflächen-Balken ein.
	 * @param element	Zeichenflächen-Balken
	 */
	private static void processBarColors(final ModelElementAnimationBar element) {
		if (DEFAULT_DIAGRAM_BACKGROUND_COLOR.equals(element.getBackgroundColor()) && element.getGradientFillColor()==null) {
			element.setBackgroundColor(Color.WHITE);
			element.setGradientFillColor(new Color(230,230,250));
		}
	}

	/**
	 * Stellt statt der Standardhintergrundfarbe einen Farbverlauf in einem Zeichenflächen gestapeltem Balken ein.
	 * @param element	Zeichenflächen gestapeltem Balken
	 */
	private static void processBarStackColors(final ModelElementAnimationBarStack element) {
		if (DEFAULT_DIAGRAM_BACKGROUND_COLOR.equals(element.getBackgroundColor()) && element.getGradientFillColor()==null) {
			element.setBackgroundColor(Color.WHITE);
			element.setGradientFillColor(new Color(230,230,250));
		}
	}

	/**
	 * Stellt statt der Standardhintergrundfarbe einen Farbverlauf in einem Zeichenflächen-Balkendiagramm ein.
	 * @param element	Zeichenflächen-Balkendiagramm
	 */
	private static void processBarChartColors(final ModelElementAnimationBarChart element) {
		if (DEFAULT_DIAGRAM_BACKGROUND_COLOR.equals(element.getBackgroundColor()) && element.getGradientFillColor()==null) {
			element.setBackgroundColor(Color.WHITE);
			element.setGradientFillColor(new Color(230,230,250));
		}
	}

	/**
	 * Stellt statt der Standardhintergrundfarbe einen Farbverlauf in einem Zeichenflächen-Wertaufzeichungsdiagramm ein.
	 * @param element	Zeichenflächen-Wertaufzeichungsdiagramm
	 */
	private static void processRecordColors(final ModelElementAnimationRecord element) {
		if (DEFAULT_DIAGRAM_BACKGROUND_COLOR.equals(element.getBackgroundColor()) && element.getGradientFillColor()==null) {
			element.setBackgroundColor(Color.WHITE);
			element.setGradientFillColor(new Color(230,230,250));
		}
	}

	/**
	 * Prüft, ob das übergebene Modell mit einem der Beispielmodelle übereinstimmt.<br>
	 * Es werden dabei alle Sprachen für den Vergleich herangezogen.
	 * @param editModel	Model, bei dem geprüft werden soll, ob es mit einem der Beispiele übereinstimmt
	 * @return	Index des Beispielmodells oder -1, wenn das zu prüfende Modell mit keinem der Beispielmodelle übereinstimmt
	 */
	public static int equalsIndex(final EditModel editModel) {
		final EditModelExamples examples=getInstance();

		for (int i=0;i<examples.list.size();i++) for (String lang: Language.getLanguages()) {
			final EditModel testModel=examples.list.get(i).getModel(null,lang,false);
			if (testModel==null) continue;
			if (testModel.equalsEditModel(editModel)) return i;
			EditModelDark.processModel(testModel,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
			if (testModel.equalsEditModel(editModel)) return i;
		}
		return -1;
	}

	/**
	 * Fügt eine Gruppe zu dem Menü hinzu.
	 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben
	 * @param menu	Menü, in dem die Beispiele als Unterpunkte eingefügt werden sollen
	 * @param listener	Listener, der mit einem Modell aufgerufen wird, wenn dieses geladen werden soll.
	 * @param group	Beispielgruppe
	 * @see #addToMenu(Component, JMenu, Consumer)
	 */
	private void addGroupToMenu(final Component owner, final JMenu menu, final Consumer<EditModel> listener, final ExampleType group) {
		final JMenuItem caption=new JMenuItem(getGroupName(group));
		menu.add(caption);
		Font font=caption.getFont();
		font=new Font(font.getName(),Font.BOLD,font.getSize());
		caption.setFont(font);
		caption.setEnabled(false);
		caption.setForeground(Color.BLACK);

		list.stream().filter(example->example.type==group).sorted((e1,e2)->String.CASE_INSENSITIVE_ORDER.compare(e1.names[0],e2.names[0])).forEach(example->{
			final JMenuItem item=new JMenuItem(example.names[0]);
			item.addActionListener(e->{
				final EditModel editModel=example.getModel(owner,FlatLaFHelper.isDark());
				if (editModel!=null && listener!=null) listener.accept(editModel);
			});
			menu.add(item);
		});
	}

	/**
	 * Fügt eine Gruppe als Untermenü zu dem Menü hinzu.
	 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben
	 * @param menu	Menü, in dem die Beispiele über ein Untermenü als Punkte eingefügt werden sollen
	 * @param listener	Listener, der mit einem Modell aufgerufen wird, wenn dieses geladen werden soll.
	 * @param group	Beispielgruppe
	 * @see #addToMenu(Component, JMenu, Consumer)
	 */
	private void addGroupToSubMenu(final Component owner, final JMenu menu, final Consumer<EditModel> listener, final ExampleType group) {
		final JMenu sub=new JMenu(getGroupName(group));
		menu.add(sub);

		list.stream().filter(example->example.type==group).sorted((e1,e2)->String.CASE_INSENSITIVE_ORDER.compare(e1.names[0],e2.names[0])).forEach(example->{
			final JMenuItem item=new JMenuItem(example.names[0]);
			item.addActionListener(e->{
				final EditModel editModel=example.getModel(owner,FlatLaFHelper.isDark());
				if (editModel!=null && listener!=null) listener.accept(editModel);
			});
			sub.add(item);
		});
	}

	/**
	 * Fügt alle Beispiele zu einem Menü hinzu
	 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben
	 * @param menu	Menü, in dem die Beispiele als Unterpunkte eingefügt werden sollen
	 * @param listener	Listener, der mit einem Modell aufgerufen wird, wenn dieses geladen werden soll.
	 */
	public static void addToMenu(final Component owner, final JMenu menu, final Consumer<EditModel> listener) {
		final EditModelExamples examples=getInstance();

		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();

		boolean lastWasFullMenu=(menu.getItemCount()>0);
		for (ExampleType type: ExampleType.values()) {
			final long count=examples.list.stream().filter(example->example.type==type).count();
			boolean useSubMenu=true;
			if (count==1) useSubMenu=false;
			if (count<=5 && screenSize.height>1080) useSubMenu=false;
			if (count<=8 && screenSize.height>1200) useSubMenu=false;
			if (useSubMenu) {
				if (lastWasFullMenu) menu.addSeparator();
				examples.addGroupToSubMenu(owner,menu,listener,type);
			} else {
				menu.addSeparator();
				examples.addGroupToMenu(owner,menu,listener,type);
			}
			lastWasFullMenu=!useSubMenu;
		}
	}

	/**
	 * Prüft, dass sich die Modelle durch eine Änderung der Systemsprache inhaltlich nicht ändern
	 * und gibt die Ergebnisse über <code>System.out</code> aus.
	 * @param names	Namen der Sprachen
	 * @param setLanguage	Runnables, um die Sprache umzustellen
	 */
	public static void runLanguageTest(final String[] names, final Runnable[] setLanguage) {
		String error;

		for (String exampleName: EditModelExamples.getExamplesList()) {
			System.out.println("Teste Beispiel \""+exampleName+"\"");

			setLanguage[0].run();

			final EditModel example=EditModelExamples.getExampleByIndex(null,EditModelExamples.getExampleIndexFromName(exampleName),false);
			if (example==null) continue;
			Document doc=example.saveToXMLDocument();

			if (!example.equalsEditModel(example.clone())) {
				System.out.println("  Modell ist nicht mit direkter Kopie von sich identisch.");
				continue;
			}

			EditModel model;

			boolean abort=false;
			for (int index=1;index<names.length;index++) {
				setLanguage[index].run();

				model=new EditModel();
				error=model.loadFromXML(doc.getDocumentElement());
				if (error!=null) {
					System.out.println("  Fehler beim Laden des "+names[index-1]+" Modells im "+names[index]+" Setup:");
					System.out.println("  "+error);
					abort=true;
					break;
				}

				if (!model.equalsEditModel(example)) {
					System.out.println("  "+names[index]+" Modell und Ausgangsmodell sind nicht mehr identisch.");
					abort=true;
					break;
				}

				doc=model.saveToXMLDocument();
			}
			if (abort) continue;

			setLanguage[0].run();

			final EditModel finalModel=new EditModel();
			error=finalModel.loadFromXML(doc.getDocumentElement());
			if (error!=null) {
				System.out.println("  Fehler beim Laden des "+names[names.length-1]+" Modells im "+names[0]+" Setup:");
				System.out.println("  "+error);
				continue;
			}

			if (!finalModel.equalsEditModel(example)) {
				System.out.println("  Konvertiertes Modell entspricht nicht mehr dem Ausgangsmodell.");
				continue;
			}
		}
	}

	/**
	 * Prüft, ob sich die Modelle durch eine Änderung der Systemsprache inhaltlich nicht ändern
	 * und gibt die Ergebnisse über <code>System.out</code> aus.
	 */
	public static void runLanguageTestAll() {
		final SetupData setup=SetupData.getSetup();
		final String saveLanguage=setup.language;

		final String[] names=Language.getLanguages();
		final Runnable[] changers=Arrays.asList(names).stream().map(name->(Runnable)()->setup.setLanguage(name)).toArray(Runnable[]::new);
		runLanguageTest(names,changers);

		setup.setLanguage(saveLanguage);
		System.out.println("done.");
	}

	/**
	 * Liefert eine Liste aller Beispiele
	 * @return	Liste aller Beispiele
	 */
	public static List<Example> getList() {
		final EditModelExamples examples=new EditModelExamples();
		return examples.list;
	}

	/**
	 * Beispielmodell
	 * @author Alexander Herzog
	 */
	public static class Example {
		/**
		 * Namen für das Beispiel in den verschiedenen Sprachen
		 */
		public final String[] names;

		/**
		 * Beispielmodelldateiname
		 */
		private final String modelFile;

		/**
		 * Beispielinfodateiname
		 */
		private final String infoFile;

		/**
		 * Gruppe in die das Beispiel fällt
		 */
		public final ExampleType type;

		/**
		 * Menge der optionalen Schlüsselwörter für das Beispiel
		 */
		public final Set<ExampleKeyWord> keyWords;

		/**
		 * Zuordnung von Sprachen zu geladenen Modelldateien für den Light-Mode.<br>
		 * (Die gesamte Zuordnung ist bis zum Eintrag des ersten Datensatzes <code>null</code>.)
		 * @see #getModel(Component, boolean)
		 * @see #getModel(Component, String, boolean)
		 */
		private Map<String,EditModel> modelLight;

		/**
		 * Zuordnung von Sprachen zu geladenen Modelldateien für den Dark-Mode.<br>
		 * (Die gesamte Zuordnung ist bis zum Eintrag des ersten Datensatzes <code>null</code>.)
		 * @see #getModel(Component, boolean)
		 * @see #getModel(Component, String, boolean)
		 */
		private Map<String,EditModel> modelDark;

		/**
		 * Geladene zusätzliche Modellbeschreibung.<br>
		 * (Ist anfänglich <code>null</code>.)
		 * @see #getInfo()
		 */
		private String info;

		/**
		 * Konstruktor der Klasse
		 * @param names	Namen für das Beispiel in den verschiedenen Sprachen
		 * @param modelFile	Beispieldateiname
		 * @param infoFile	Beispielinfotextdateiname
		 * @param type	Gruppe in die das Beispiel fällt
		 * @param keyWords	Menge der optionalen Schlüsselwörter für das Beispiel
		 */
		private Example(final String[] names, final String modelFile, final String infoFile, final ExampleType type, final Set<ExampleKeyWord> keyWords) {
			this.names=names;
			this.modelFile=modelFile;
			this.infoFile=infoFile;
			this.type=type;
			this.keyWords=keyWords;
		}

		/**
		 * Prüft, ob das Modell zu bestimmten Filterkriterien passe.
		 * @param group	Gruppe in die das Beispiel fallen muss (<code>null</code> bedeutet: nicht prüfen)
		 * @param keyWord	Schlüsselwort welches in der Liste der Schlüsselwörter für das Modell enthalten sein muss (<code>null</code> bedeutet: nicht prüfen)
		 * @param searchString	Suchbegriff der im Modellnamen oder seiner Beschreibung auftreten muss (<code>null</code> oder leer bedeutet: nicht prüfen)
		 * @return	Liefert <code>true</code>, wenn das Modell die geforderten Kriterien erfüllt
		 */
		public boolean match(final ExampleType group, final ExampleKeyWord keyWord, final String searchString) {
			if (group!=null) {
				if (type!=group) return false;
			}

			if (keyWord!=null) {
				if (!keyWords.contains(keyWord)) return false;
			}

			if (searchString!=null && !searchString.isBlank()) {
				final String searchLower=searchString.toLowerCase();
				boolean searchOk=false;
				final EditModel model=getModel(null,false);
				if (model!=null) {
					searchOk=searchOk || model.name.toLowerCase().contains(searchLower);
					searchOk=searchOk || model.description.toLowerCase().contains(searchLower);
				}
				if (!searchOk) {
					final String info=getInfo();
					if (info!=null) {
						searchOk=info.toLowerCase().contains(searchLower);
					}
				}
				if (!searchOk) return false;
			}

			return true;
		}

		/**
		 * Lädt eines der Beispielmodelle in der aktuellen Sprache.
		 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben.
		 * @param dark	Modell für den Dark-Modus aufbereiten?
		 * @return	Beispiel oder <code>null</code>, wenn das Modell nicht geladen werden konnte
		 */
		public EditModel getModel(final Component owner, final boolean dark) {
			return getModel(owner,Language.tr("Numbers.Language"),dark);
		}

		/**
		 * Lädt eines der Beispielmodelle.
		 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben.
		 * @param lang	Sprache in der das Modell zurückgeliefert werden soll
		 * @param dark	Modell für den Dark-Modus aufbereiten?
		 * @return	Beispiel oder <code>null</code>, wenn das Modell nicht geladen werden konnte
		 */
		public EditModel getModel(final Component owner, final String lang, final boolean dark) {
			EditModel model;

			if (dark) {
				if (modelDark==null) modelDark=new HashMap<>();
				model=modelDark.get(lang);
				if (model==null) {
					model=getModelFromXML(owner,lang,dark);
					if (model!=null) modelDark.put(lang,model);
				}
			} else {
				if (modelLight==null) modelLight=new HashMap<>();
				model=modelLight.get(lang);
				if (model==null) {
					model=getModelFromXML(owner,lang,dark);
					if (model!=null) modelLight.put(lang,model);
				}
			}

			if (model!=null) model=model.clone();

			return model;
		}

		/**
		 * Lädt eines der Beispielmodelle aus einer XML-Datei.
		 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben.
		 * @param lang	Sprache in der das Modell zurückgeliefert werden soll
		 * @param dark	Modell für den Dark-Modus aufbereiten?
		 * @return	Beispiel oder <code>null</code>, wenn das Modell nicht geladen werden konnte
		 */
		private EditModel getModelFromXML(final Component owner, final String lang, final boolean dark) {
			final EditModel editModel=new EditModel();
			try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+lang+"/"+modelFile)) {
				final String error=editModel.loadFromStream(in);
				if (error!=null) {
					if (owner==null) {
						System.out.println(error);
					} else {
						MsgBox.error(owner,Language.tr("XML.LoadErrorTitle"),error);
					}
					return null;
				}
				processDiagramColors(editModel.surface);
				if (dark) {
					EditModelDark.processModel(editModel,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
				}
				return editModel;
			} catch (IOException e) {
				return null;
			}
		}

		/**
		 * Liefert die zusätzliche Modellbeschreibung in der aktuellen Sprache.
		 * @return	Modellbeschreibung aus externer Textdatei (oder <code>null</code>, wenn die Textdatei nicht geladen werden konnte)
		 */
		public String getInfo() {
			if (info==null) {

				try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+Language.tr("Numbers.Language")+"/"+infoFile)) {
					info=Table.loadTextFromInputStream(in);
				} catch (IOException e) {}
			}

			return info;
		}
	}

	/**
	 * Speichert Bilder für alle Beispielmodelle
	 * @param language	Sprache
	 * @param parentFolder	Ausgabeverzeichnis (die Dateinamen werden automatisch gewählt)
	 * @param out	Ausgabestream für Meldungen (darf nicht <code>null</code> sein)
	 * @return	Gibt an, ob die Bilder erfolgreich erstellt werden konnten
	 */

	public static boolean saveImages(final String language, final File parentFolder, final PrintStream out) {
		final File folder=new File(parentFolder,language);
		if (!folder.isDirectory()) {
			if (!folder.mkdirs()) {
				out.println("error mkdir "+folder.toString());
				return false;
			}
		}

		final SetupData setup=SetupData.getSetup();
		final String saveLanguage=setup.language;
		setup.setLanguage(language);

		try {
			final StringBuilder info=new StringBuilder();
			for (Example example: getList()) {
				final String name=example.names[0];
				final String file="ExampleModel_"+example.modelFile.replace(".xml",".png");

				if (out!=null) out.println("writing \""+name+"\"");

				final int exampleIndex=getExampleIndexFromName(name);

				final EditModel editModel=getExampleByIndex(null,exampleIndex,false);
				final ModelSurfacePanel surfacePanel=new ModelSurfacePanel();
				surfacePanel.setSurface(editModel,editModel.surface,editModel.clientData,editModel.sequences);
				final String error=EditorPanel.exportModelToFile(editModel,null,surfacePanel,new File(folder,file),null,true);
				if (error!=null && out!=null) out.println(error);

				final String modelDescription=getExampleInfoByIndex(null,exampleIndex);

				info.append("## "+name+"\n");
				info.append("!["+example.names[0]+"](Images/"+file+")\n\n");
				info.append(modelDescription);
				info.append("\n\n");
				info.append("***");
				info.append("\n\n");
			}
			Table.saveTextToFile(info.toString(),new File(folder,"info.md"));
		} finally {
			setup.setLanguage(saveLanguage);
		}

		SwingUtilities.invokeLater(()->{
			for (Frame frame: Frame.getFrames()) if (frame instanceof JFrame) {
				((JFrame)frame).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.dispose();
			}
		});

		return true;
	}
}