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
package ui.statistics;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.Serializable;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.w3c.dom.Document;

import language.Language;
import mathtools.distribution.tools.FileDropperData;
import scripting.java.DynamicErrorInfo;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunDataFilterTools;
import simulator.Simulator;
import simulator.statistics.Statistics;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;
import ui.script.ScriptEditorAreaBuilder;
import ui.script.ScriptEditorPanel;
import ui.script.ScriptPopup;
import ui.script.ScriptTools;

/**
 * Ermöglicht die Filterung der Ergebnisse mit Hilfe von Java-Code.
 * @author Alexander Herzog
 * @see StatisticViewerFastAccessBase
 * @see StatisticViewerFastAccess
 */
public class StatisticViewerFastAccessJava extends StatisticViewerFastAccessBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2506181065659793787L;

	/** Eingabefeld für das Skript */
	private RSyntaxTextArea filter;
	/** Zuletzt gespeichertes Skript */
	private String lastSavedFilterText;
	/** Zuletzt ausgeführtes Skript */
	private String lastInterpretedFilterText;
	/** Ergebnis der Ausführung von {@link #lastInterpretedFilterText} */
	private String lastInterpretedFilterResult;
	/** XML-Dokment der Statistikdaten */
	private Document document;
	/** Wiederverwendung des {@link StringBuilder} in {@link #process()} */
	private StringBuilder results;

	/**
	 * Konstruktor der Klasse
	 * @param helpFastAccess	Hilfe für Schnellzugriff-Seite
	 * @param helpFastAccessModal	Hilfe für Schnellzugriff-Dialog
	 * @param statistics	Statistik-Objekt, dem die Daten entnommen werden sollen
	 * @param resultsChanged	Runnable das aufgerufen wird, wenn sich die Ergebnisse verändert haben
	 * @param dragDropLoad	Runnable das aufgerufen wird, wenn eine Datei auf dem Panel abgelegt wird (nur relevant, wenn das Drag&amp;drop-Verhalten des übergeordneten Panels überschrieben wird)
	 */
	public StatisticViewerFastAccessJava(final Runnable helpFastAccess, final Runnable helpFastAccessModal, final Statistics statistics, final Runnable resultsChanged, final Consumer<File> dragDropLoad) {
		super(helpFastAccess,helpFastAccessModal,statistics,resultsChanged,true);

		/* Filtertext */
		final ScriptEditorAreaBuilder builder=new ScriptEditorAreaBuilder(ScriptPopup.ScriptMode.Java,false,null);
		builder.addAutoCompleteFeatures(ScriptEditorPanel.featuresFilter);
		builder.addFileDropper(e->{
			final FileDropperData data=(FileDropperData)e.getSource();
			dragDropLoad.accept(data.getFile());
		});

		final RTextScrollPane scrollFilter;
		add(scrollFilter=new RTextScrollPane(filter=builder.get()));
		scrollFilter.setLineNumbersEnabled(true);

		/* Filtertext laden */
		String s=SetupData.getSetup().filterJava;
		if (s.trim().isEmpty()) s=ScriptEditorPanel.DEFAULT_JAVA;
		filter.setText(s);
		lastSavedFilterText="";
	}

	@Override
	protected Icon getIcon() {
		return Images.SCRIPT_MODE_JAVA.getIcon();
	}

	@Override
	protected void addXML(final String selector) {
		String oldFilter=filter.getText().trim();
		while (oldFilter.endsWith("\n") && oldFilter.length()>1) oldFilter=oldFilter.substring(0,oldFilter.length()-1);
		if (oldFilter.endsWith("}") && oldFilter.length()>1) oldFilter=oldFilter.substring(0,oldFilter.length()-1);
		while (oldFilter.endsWith("\n") && oldFilter.length()>1) oldFilter=oldFilter.substring(0,oldFilter.length()-1);
		final String newCommand=String.format("sim.getOutput().println(sim.getStatistics().xml(\"%s\"));",selector.replace("\"","\\\""));
		filter.setText(oldFilter+"\n"+newCommand+"\n}");
	}

	@Override
	protected void addCustomToolbarButtons(final JToolBar toolbar) {
		final JButton run=new JButton(Language.tr("Statistic.FastAccess.RunJava"));
		run.setToolTipText(Language.tr("Statistic.FastAccess.RunJava.Hint"));
		run.setIcon(Images.SCRIPT_RUN.getIcon());
		run.addActionListener(e->process());
		toolbar.add(run);
	}

	/**
	 * Führt das aktuelle Skript aus.
	 */
	private void process() {
		final String text=filter.getText();
		if (lastInterpretedFilterText!=null && text.equals(lastInterpretedFilterText) && lastInterpretedFilterResult!=null) {
			setResults(lastInterpretedFilterResult);
			return;
		}
		lastInterpretedFilterText=text;

		final DynamicRunner runner=DynamicFactory.getFactory().load(text,null,null);
		if (runner.getStatus()!=DynamicStatus.OK) {
			new DynamicErrorInfo(this,runner);
			return;
		}
		if (results==null) results=new StringBuilder(); else results.setLength(0);
		runner.parameter.output=new OutputImpl(s->results.append(s),false);
		if (document==null) document=statistics.saveToXMLDocument();
		runner.parameter.statistics=new StatisticsImpl(s->results.append(s),document,statistics.loadedStatistics,false);
		runner.parameter.system=new SystemImpl(Simulator.getSimulationDataFromStatistics(statistics),-1);
		runner.run();
		if (runner.getStatus()!=DynamicStatus.OK) {
			MsgBox.error(this,Language.tr("Statistic.FastAccess.JavaErrorTitle"),DynamicFactory.getLongStatusText(runner));
			return;
		}
		lastInterpretedFilterResult=results.toString();
		setResults(lastInterpretedFilterResult);

		SetupData setup=SetupData.getSetup();
		setup.filterJava=filter.getText();
		setup.saveSetupWithWarning(null);
	}

	/**
	 * Speichert das Skript oder die Ergebnisse in einer Datei.
	 * @param parentFrame	Übergeordnetes Fenster (zur Ausrichtung des Dialogs)
	 * @param text	Zu speichernder Text
	 * @param isJava	Skript (<code>true</code>) oder Ausgabetext (<code>false</code>)
	 * @return	Liefert <code>true</code>, wenn der Text erfolgreich gespeichert werden konnte
	 */
	private boolean saveTextToFile(Component parentFrame, String text, final boolean isJava) {
		final String fileName;
		if (isJava) {
			fileName=ScriptTools.selectJavaSaveFile(parentFrame,null,null);
		} else {
			fileName=ScriptTools.selectTextSaveFile(parentFrame,null,null);
		}
		if (fileName==null) return false;
		final File file=new File(fileName);

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(getParent(),file)) return false;
		}

		return JSRunDataFilterTools.saveText(text,file,false);
	}

	/**
	 * Lädt ein Skript aus einer Datei.
	 * @param parentFrame	Übergeordnetes Fenster (zur Ausrichtung des Dialogs)
	 * @param isJava	Skript (<code>true</code>) oder Ausgabetext (<code>false</code>)
	 * @return	Liefert im Erfolgsfall den geladenen Text
	 */
	private String loadTextFromFile(Container parentFrame, final boolean isJava) {
		final String fileName;
		if (isJava) {
			fileName=ScriptTools.selectJavaFile(parentFrame,null,null);
		} else {
			fileName=ScriptTools.selectTextFile(parentFrame,null,null);
		}
		if (fileName==null) return null;
		final File file=new File(fileName);

		return JSRunDataFilterTools.loadText(file);
	}

	@Override
	public boolean discardFilterOk() {
		if (filter.getText().equals(lastSavedFilterText)) return true;

		switch (MsgBox.confirmSave(getParent(),Language.tr("Filter.Save.Title"),Language.tr("Filter.Save.Info"))) {
		case JOptionPane.YES_OPTION:
			if (!saveTextToFile(getParent(),filter.getText(),true)) return false;
			return true;
		case JOptionPane.NO_OPTION: return true;
		case JOptionPane.CANCEL_OPTION: return false;
		default: return false;
		}
	}

	@Override
	protected void commandNew() {
		if (!discardFilterOk()) return;
		if (!filter.getText().equals(lastSavedFilterText)) lastSavedFilterText=filter.getText();
		filter.setText(ScriptEditorPanel.DEFAULT_JAVA);
		setResults("");
		lastInterpretedFilterText="";
		lastInterpretedFilterResult="";
	}

	@Override
	protected void commandLoad() {
		if (!discardFilterOk()) return;
		final String s=loadTextFromFile(getParent(),true);
		if (s!=null) {
			filter.setText(s);
			setResults("");
			lastInterpretedFilterText="";
			lastInterpretedFilterResult="";
		}
	}

	@Override
	protected void commandSave() {
		if (saveTextToFile(getParent(),filter.getText(),true)) lastSavedFilterText=filter.getText();
	}

	@Override
	protected void commandTools(final JButton sender) {
		final ScriptPopup popup=new ScriptPopup(sender,statistics.editModel,statistics,ScriptPopup.ScriptMode.Java,helpFastAccessModal);
		popup.addInfoText(Language.tr("Surface.ScriptEditor.PopupInfo"));
		popup.addFeatures(ScriptEditorPanel.featuresFilter);
		popup.build();
		popup.show(filter);
	}

	@Override
	public String[] preferredExtensions() {
		return new String[] {"java"};
	}

	@Override
	public boolean loadFile(final File file) {
		final String s=JSRunDataFilterTools.loadText(file);
		if (s==null) return false;
		filter.setText(s);
		setResults("");
		lastInterpretedFilterText="";
		lastInterpretedFilterResult="";
		return true;
	}
}
