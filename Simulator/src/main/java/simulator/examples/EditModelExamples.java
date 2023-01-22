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
import java.util.List;
import java.util.function.Consumer;

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
import ui.modeleditor.ModelSurfacePanel;
import ui.tools.FlatLaFHelper;

/**
 * Liefert Beispielmodelle
 * @author Alexander Herzog
 */
public class EditModelExamples {
	/**
	 * Kategorien für die Beispiele
	 * @author Alexander Herzog
	 */
	public enum ExampleType {
		/**
		 * Standardbeispiele
		 */
		TYPE_DEFAULT,

		/**
		 * Beispiele, die bestimmte Modellierungseigenschaften verdeutlichen
		 */
		TYPE_PROPERTIES,

		/**
		 * Beispiele zum Vergleich verschiedener Steuerungsstrategien
		 */
		TYPE_COMPARE,

		/**
		 * Beispiele, die sich auf reale Modelle bzw. Fragen beziehen
		 */
		TYPE_REAL_MODELS,

		/**
		 * Beispiele, die mathematische Zusammenhänge verdeutlichen
		 */
		TYPE_MATH
	}

	/**
	 * Liste mit den Beispielen.
	 * @see #addExample(String[], String, ExampleType)
	 */
	private final List<Example> list;

	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse stellt nur statische Hilfsroutinen zur Verfügung und kann nicht instanziert werden.
	 */
	private EditModelExamples() {
		list=new ArrayList<>();
		addExamples();
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
		final EditModelExamples examples=new EditModelExamples();
		final List<String> result=new ArrayList<>();
		for (Example example: examples.list) if (example.type==group) result.add(example.names[0]);
		return result;
	}

	/**
	 * Fügt ein Beispiel zu der Liste der Beispiele hinzu
	 * @param names	Namen für das Beispiel in den verschiedenen Sprachen
	 * @param file	Beispieldateiname
	 * @param type	Gruppe in die das Beispiel fällt
	 */
	private void addExample(final String[] names, final String file, final ExampleType type) {
		list.add(new Example(names,file,type));
	}

	/**
	 * Fügt alle Beispiele zu der Liste der Beispiele hinzu.
	 * @see #list
	 */
	private void addExamples() {
		addExample(Language.trAll("Examples.ErlangC"),"ErlangC1.xml",ExampleType.TYPE_DEFAULT);
		addExample(Language.trAll("Examples.ClientTypePriorities"),"Kundentypen.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.ImpatientClientsAndRetry"),"Warteabbrecher.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.SharedResources"),"SharedResources.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.SystemDesign"),"Vergleiche2.xml",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.SystemDesignWithControl"),"Vergleiche3.xml",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.PushAndPullProduction"),"PushPull.xml",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.PushAndPullProductionMultiBarriers"),"PushPullMulti.xml",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.PushPullThroughput"),"PushPullThroughput.xml",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.ChangeResourceCountCompare"),"ChangeResourceCountCompare.xml",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.LimitedNumberOfClientsAtAStation"),"Variable.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.LawOfLargeNumbers"),"GesetzDerGroßenZahlen.xml",ExampleType.TYPE_MATH);
		addExample(Language.trAll("Examples.Galton"),"Galton.xml",ExampleType.TYPE_MATH);
		addExample(Language.trAll("Examples.Callcenter"),"Callcenter.xml",ExampleType.TYPE_REAL_MODELS);
		addExample(Language.trAll("Examples.OperatorsAsSimulationObjects"),"BedienerAlsSimulationsobjekte.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Transport"),"Transport.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Transporter"),"Transporter.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.CombiningOrdersAndItems"),"MultiSignalBarrier.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Restaurant"),"Restaurant.xml",ExampleType.TYPE_REAL_MODELS);
		addExample(Language.trAll("Examples.Baustellenampel"),"Baustellenampel.xml",ExampleType.TYPE_REAL_MODELS);
		addExample(Language.trAll("Examples.Batch"),"Batch.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Failure"),"Failure.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.SetUpTimes"),"SetUpTimes.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Rework"),"Rework.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.HoldJS"),"HoldJS.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.DelayJS"),"DelayJS.xml",ExampleType.TYPE_COMPARE);
		addExample(Language.trAll("Examples.RestrictedBuffer"),"RestriktierterPuffer.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Analog"),"Analog.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Jockeying"),"Jockeying.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.QueueingDiscipline"),"QueueingDiscipline.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.Shiftplan"),"Shiftplan.xml",ExampleType.TYPE_PROPERTIES);
		addExample(Language.trAll("Examples.CoefficientOfVariation"),"CoefficientOfVariation.xml",ExampleType.TYPE_MATH);
		addExample(Language.trAll("Examples.PASTA"),"PASTA.xml",ExampleType.TYPE_MATH);
		addExample(Language.trAll("Examples.ZentralerGrenzwertsatz"),"ZentralerGrenzwertsatz.xml",ExampleType.TYPE_MATH);
		addExample(Language.trAll("Examples.BusStoppParadoxon"),"BusStoppParadoxon.xml",ExampleType.TYPE_MATH);
		addExample(Language.trAll("Examples.RandomNumberGenerators"),"RandomNumberGenerators.xml",ExampleType.TYPE_MATH);
	}

	/**
	 * Liefert eine Liste mit allen verfügbaren Beispielen.
	 * @return	Liste mit allen verfügbaren Beispielen
	 */
	public static String[] getExamplesList() {
		final EditModelExamples examples=new EditModelExamples();
		return examples.list.stream().map(example->example.names[0]).toArray(String[]::new);
	}

	/**
	 * Liefert den Index des Beispiels basieren auf dem Namen
	 * @param name	Name des Beispiels zu dem der Index bestimmt werden soll
	 * @return	Index des Beispiels oder -1, wenn es kein Beispiel mit diesem Namen gibt
	 * @see #getExampleByIndex(Component, int)
	 */
	public static int getExampleIndexFromName(final String name) {
		if (name==null || name.isEmpty()) return -1;
		final EditModelExamples examples=new EditModelExamples();

		for (int i=0;i<examples.list.size();i++) {
			for (String test: examples.list.get(i).names) if (name.trim().equalsIgnoreCase(test)) return i;
		}
		return -1;
	}

	/**
	 * Liefert ein bestimmtes Beispiel über seine Nummer aus der Namesliste (<code>getExamplesList()</code>)
	 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben
	 * @param index	Index des Beispiels, das zurückgeliefert werden soll
	 * @return	Beispiel oder <code>null</code>, wenn sich der Index außerhalb des gültigen Bereichs befindet
	 * @see EditModelExamples#getExamplesList()
	 */
	public static EditModel getExampleByIndex(final Component owner, final int index) {
		final EditModelExamples examples=new EditModelExamples();
		if (index<0 || index>=examples.list.size()) return null;
		final String fileName=examples.list.get(index).file;

		final EditModel editModel=new EditModel();
		try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+Language.tr("Numbers.Language")+"/"+fileName)) {
			final String error=editModel.loadFromStream(in);
			if (error!=null) {
				if (owner==null) {
					System.out.println(error);
				} else {
					MsgBox.error(owner,Language.tr("XML.LoadErrorTitle"),error);
				}
				return null;
			}
			return editModel;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Prüft, ob das übergebene Modell mit einem der Beispielmodelle übereinstimmt.<br>
	 * Es werden dabei alle Sprachen für den Vergleich herangezogen.
	 * @param editModel	Model, bei dem geprüft werden soll, ob es mit einem der Beispiele übereinstimmt
	 * @return	Index des Beispielmodells oder -1, wenn das zu prüfende Modell mit keinem der Beispielmodelle übereinstimmt
	 */
	public static int equalsIndex(final EditModel editModel) {
		final EditModelExamples examples=new EditModelExamples();

		for (int i=0;i<examples.list.size();i++) for (String lang: Language.getLanguages()) {
			final EditModel testModel=new EditModel();
			try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+lang+"/"+examples.list.get(i).file)) {
				testModel.loadFromStream(in);
				if (testModel.equalsEditModel(editModel)) return i;
				if (FlatLaFHelper.isDark()) {
					EditModelDark.processModel(testModel,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
					if (testModel.equalsEditModel(editModel)) return i;
				}
			} catch (IOException e) {return -1;}
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
		if (menu.getItemCount()>0) menu.addSeparator();

		final JMenuItem caption=new JMenuItem(getGroupName(group));
		menu.add(caption);
		Font font=caption.getFont();
		font=new Font(font.getName(),Font.BOLD,font.getSize());
		caption.setFont(font);
		caption.setEnabled(false);
		caption.setForeground(Color.BLACK);

		for (Example example: list) if (example.type==group) {
			final JMenuItem item=new JMenuItem(example.names[0]);
			item.addActionListener(e->{
				final EditModel editModel=new EditModel();
				try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+Language.tr("Numbers.Language")+"/"+example.file)) {
					final String error=editModel.loadFromStream(in);
					if (error!=null) {
						if (owner==null) {
							System.out.println(error);
						} else {
							MsgBox.error(owner,Language.tr("XML.LoadErrorTitle"),error);
						}
						return;
					}
					if (FlatLaFHelper.isDark()) EditModelDark.processModel(editModel,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
					if (listener!=null) listener.accept(editModel);
				} catch (IOException e1) {}
			});
			menu.add(item);
		}
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

		for (Example example: list) if (example.type==group) {
			final JMenuItem item=new JMenuItem(example.names[0]);
			item.addActionListener(e->{
				final EditModel editModel=new EditModel();
				try (InputStream in=EditModelExamples.class.getResourceAsStream("examples_"+Language.tr("Numbers.Language")+"/"+example.file)) {
					final String error=editModel.loadFromStream(in);
					if (error!=null) {
						if (owner==null) {
							System.out.println(error);
						} else {
							MsgBox.error(owner,Language.tr("XML.LoadErrorTitle"),error);
						}
						return;
					}
					if (FlatLaFHelper.isDark()) EditModelDark.processModel(editModel,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
					if (listener!=null) listener.accept(editModel);
				} catch (IOException e1) {}
			});
			sub.add(item);
		}
	}

	/**
	 * Fügt alle Beispiele zu einem Menü hinzu
	 * @param owner	Übergeordnetes Elementes (zum Ausrichten von Fehlermeldungen). Wird hier <code>null</code> übergeben, so werden Fehlermeldungen auf der Konsole ausgegeben
	 * @param menu	Menü, in dem die Beispiele als Unterpunkte eingefügt werden sollen
	 * @param listener	Listener, der mit einem Modell aufgerufen wird, wenn dieses geladen werden soll.
	 */
	public static void addToMenu(final Component owner, final JMenu menu, final Consumer<EditModel> listener) {
		final EditModelExamples examples=new EditModelExamples();
		final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		if (screenSize.height>1080) {
			for (ExampleType type: ExampleType.values()) examples.addGroupToMenu(owner,menu,listener,type);
		} else {
			menu.addSeparator();
			for (ExampleType type: ExampleType.values()) examples.addGroupToSubMenu(owner,menu,listener,type);
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

			final EditModel example=EditModelExamples.getExampleByIndex(null,EditModelExamples.getExampleIndexFromName(exampleName));
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
		 * Beispieldateiname
		 */
		public final String file;

		/**
		 * Gruppe in die das Beispiel fällt
		 */
		public final ExampleType type;

		/**
		 * Konstruktor der Klasse
		 * @param names	Namen für das Beispiel in den verschiedenen Sprachen
		 * @param file	Beispieldateiname
		 * @param type	Gruppe in die das Beispiel fällt
		 */
		private Example(final String[] names, final String file, final ExampleType type) {
			this.names=names;
			this.file=file;
			this.type=type;
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
				final String file="ExampleModel_"+example.file.replace(".xml",".png");

				if (out!=null) out.println("writing \""+name+"\"");

				final EditModel editModel=getExampleByIndex(null,getExampleIndexFromName(name));
				final ModelSurfacePanel surfacePanel=new ModelSurfacePanel();
				surfacePanel.setSurface(editModel,editModel.surface,editModel.clientData,editModel.sequences);
				final String error=EditorPanel.exportModelToFile(editModel,null,surfacePanel,new File(folder,file),null,true);
				if (error!=null && out!=null) out.println(error);

				info.append("## "+name+"\n");
				info.append("!["+example.names[0]+"](Images/"+file+")\n\n");
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