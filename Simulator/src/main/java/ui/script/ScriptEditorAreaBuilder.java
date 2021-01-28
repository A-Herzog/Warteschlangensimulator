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
package ui.script;

import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.Icon;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionCellRenderer;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import language.Language;
import mathtools.distribution.tools.FileDropper;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptPopup.ScriptFeature;
import ui.script.ScriptPopup.ScriptMode;

/**
 * Diese Klasse erstellt und konfiguriert ein
 * {@link RSyntaxTextArea}-Objekt.
 * @author Alexander Herzog
 * @see RSyntaxTextArea
 */
public class ScriptEditorAreaBuilder {
	/** In dem Editor zu bearbeitende Sprache */
	private final ScriptPopup.ScriptMode language;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Listener, der bei Tastendruck benachrichtigt werden soll (darf <code>null</code> sein) */
	private final Consumer<KeyEvent> keyListener;
	/** Initialer Text für das Eingabefeld */
	private String initialText;
	/** Datei-Drag&amp;Drop-Funktionalität für das Eingabefeld ({@link #addFileDropper(ActionListener)}) */
	private ActionListener fileDropListener;
	/** Aktive AutoComplete-Funktionen */
	private Set<ScriptPopup.ScriptFeature> features;
	/** System für AutoComplete-Vorschläge */
	private DefaultCompletionProvider autoCompleteProvider;

	/**
	 * Konstruktor der Klasse
	 * @param language	In dem Editor zu bearbeitende Sprache
	 * @param readOnly	Nur-Lese-Status
	 * @param keyListener	Listener, der bei Tastendruck benachrichtigt werden soll (darf <code>null</code> sein)
	 */
	public ScriptEditorAreaBuilder(final ScriptPopup.ScriptMode language, final boolean readOnly, final Consumer<KeyEvent> keyListener) {
		this.language=language;
		this.readOnly=readOnly;
		this.keyListener=keyListener;
		features=new HashSet<>();
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Es wird davon ausgegangen, dass das Eingabefeld bearbeitbar sein soll.
	 * @param language	In dem Editor zu bearbeitende Sprache
	 */
	public ScriptEditorAreaBuilder(final ScriptPopup.ScriptMode language) {
		this(language,false,null);
	}

	/**
	 * Stellt den initialen Text in dem Eingabefeld ein.
	 * @param initialText	Initialer Text für das Eingabefeld
	 */
	public void setText(final String initialText) {
		this.initialText=(initialText==null)?"":initialText;
	}

	/**
	 * Fügt eine Datei-Drag&amp;Drop-Funktionalität zu dem Eingabefeld hinzu
	 * @param fileDropListener	Listener, der benachrichtigt werden soll, wenn eine Datei auf dem Feld abgelegt wurde
	 */
	public void addFileDropper(final ActionListener fileDropListener) {
		this.fileDropListener=fileDropListener;
	}

	/**
	 * Fügt eine Reihe von AutoComplete-Funktionen zu dem Eingabefeld hinzu
	 * @param autoCompleteFeatures	Befehle, die die AutoComplete-Funktion des Eingabefeldes unterstützen soll
	 */
	public void addAutoCompleteFeatures(final ScriptPopup.ScriptFeature[] autoCompleteFeatures) {
		if (autoCompleteFeatures!=null) features.addAll(Arrays.asList(autoCompleteFeatures));
	}

	/**
	 * Fügt einen AutoComplete-Funktionsbereich zu dem Eingabefeld hinzu
	 * @param autoCompleteFeature	Befehle, die die AutoComplete-Funktion des Eingabefeldes unterstützen soll
	 */
	public void addAutoCompleteFeature(final ScriptPopup.ScriptFeature autoCompleteFeature) {
		if (autoCompleteFeature!=null) features.add(autoCompleteFeature);
	}

	/**
	 * Liefert das konfigurierte Eingabefeld
	 * @return	Neues Eingabefeld
	 */
	public RSyntaxTextArea get() {
		/* Konstruktor */
		final RSyntaxTextArea editor=new RSyntaxTextArea();

		/* Text setzen */
		if (initialText!=null && !initialText.isEmpty()) {
			editor.setText(initialText);
		}

		/* Sprache einstellen */
		switch (language) {
		case Javascript: editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT); break;
		case Java: editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA); break;
		}

		/* Undo */
		ModelElementBaseDialog.addUndoFeature(editor);

		/* Read-only */
		editor.setEditable(!readOnly);

		/* Auf Tastendrücke reagieren */
		if (keyListener!=null) editor.addKeyListener(new KeyAdapter() {
			/* @Override public void keyTyped(KeyEvent e) {keyListener.accept(e);} */
			@Override public void keyReleased(KeyEvent e) {keyListener.accept(e);}
			/* @Override public void keyPressed(KeyEvent e) {keyListener.accept(e);} */
		});

		/* Auf Drag&Drop reagieren */
		if (!readOnly && fileDropListener!=null) {
			new FileDropper(editor,fileDropListener);
		}

		/* AutoComplete */
		autoCompleteProvider=new DefaultCompletionProvider();
		if (language==ScriptMode.Javascript) {
			addAutoComplete(Language.tr("Statistic.FastAccess.Template.JSEngineName"),Language.tr("Statistic.FastAccess.Template.JSEngineName.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),"JS_ENGINE_NAME");
		}
		buildRuntime();
		buildSystem();
		buildClient();
		buildClients();
		buildInput();
		buildOutput(false,false);
		buildOutput(true,false);
		if (features.contains(ScriptFeature.Model)) {
			buildModel();
			buildStatisticsTools(false);
		} else {
			buildStatisticsTools(true);
		}
		buildStatistics();
		final AutoCompletion autoComplete=new AutoCompletion(autoCompleteProvider);
		autoComplete.setListCellRenderer(new CompletionCellRenderer());
		autoComplete.setShowDescWindow(true);
		autoComplete.install(editor);

		/* Ergebnis liefern */
		return editor;
	}

	/**
	 * Fügt einen AutoComplete-Datensatz hinzu
	 * @param shortDescription	Kurzbeschreibung des Befehls
	 * @param summary	Beschreibung des Befehls
	 * @param icon	Icon für den Eintrag
	 * @param command	Einzufügendes Text
	 */
	private void addAutoComplete(final String shortDescription, final String summary, final Icon icon, final String command) {
		if (command==null || command.trim().isEmpty()) return;
		final BasicCompletion completion=new BasicCompletion(autoCompleteProvider,command);

		if (icon!=null) completion.setIcon(icon);
		completion.setShortDescription(shortDescription);
		completion.setSummary(summary);
		autoCompleteProvider.addCompletion(completion);
	}

	/**
	 * Erstellt die AutoComplete-Einträge für das
	 * Javascript-System-Objekt (steht nur zur Verfügung, wenn das Simulation-Objekt nicht vorhanden ist).
	 * @see ScriptFeature#JSSystem
	 */
	private void buildRuntime() {
		if (language==ScriptMode.Javascript && !features.contains(ScriptFeature.JSSystem)) return;

		String runtimeCalc="";
		String runtimeTime="";
		String runtimeLoad="";
		String runtimeExecute1="";
		String runtimeExecute2="";
		String runtimeExecute3="";

		if (language==ScriptMode.Javascript && features.contains(ScriptFeature.JSSystem)) {
			runtimeCalc="System.calc(\"1+2\");";
			runtimeTime="System.time();";
			runtimeLoad="System.getInput(\"https://www.valuegetter\",-1);";
			runtimeExecute1="System.execute(\"program.exe\");";
			runtimeExecute2="System.executeAndReturnOutput(\"program.exe\");";
			runtimeExecute3="System.executeAndWait(\"program.exe\");";
		}

		if (language==ScriptMode.Java) {
			runtimeCalc="sim.getRuntime().calc(\"1+2\");";
			runtimeTime="sim.getRuntime().getTime();";
			runtimeLoad="sim.getRuntime().getInput(\"https://www.valuegetter\",-1);";
			runtimeExecute1="sim.getRuntime().execute(\"program.exe\");";
			runtimeExecute2="sim.getRuntime().executeAndReturnOutput(\"program.exe\");";
			runtimeExecute3="sim.getRuntime().executeAndWait(\"program.exe\");";
		}

		addAutoComplete(Language.tr("ScriptPopup.Runtime.Calc"),Language.tr("ScriptPopup.Runtime.Calc.Hint"),Images.SCRIPT_RECORD_EXPRESSION.getIcon(),runtimeCalc);
		addAutoComplete(Language.tr("ScriptPopup.Runtime.Time"),Language.tr("ScriptPopup.Runtime.Time.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),runtimeTime);
		addAutoComplete(Language.tr("ScriptPopup.Runtime.LoadValue"),Language.tr("ScriptPopup.Runtime.LoadValue.Hint"),Images.SCRIPT_RECORD_INPUT.getIcon(),runtimeLoad);
		if (SetupData.getSetup().modelSecurityAllowExecuteExternal) {
			addAutoComplete(Language.tr("ScriptPopup.Runtime.Execute"),Language.tr("ScriptPopup.Runtime.Execute.Hint"),Images.SCRIPT_RECORD_EXECUTE_PROGRAM.getIcon(),runtimeExecute1);
			addAutoComplete(Language.tr("ScriptPopup.Runtime.ExecuteAndReturnOutput"),Language.tr("ScriptPopup.Runtime.ExecuteAndReturnOutput.Hint"),Images.SCRIPT_RECORD_EXECUTE_PROGRAM.getIcon(),runtimeExecute2);
			addAutoComplete(Language.tr("ScriptPopup.Runtime.ExecuteAndWait"),Language.tr("ScriptPopup.Runtime.ExecuteAndWait.Hint"),Images.SCRIPT_RECORD_EXECUTE_PROGRAM.getIcon(),runtimeExecute3);
		}
	}

	/**
	 * Erstellt die AutoComplete-Einträge zur
	 * Anzeige der Befehle für den Zugriff auf die Simulationseigenschaften.
	 * @see ScriptFeature#Simulation
	 */
	private void buildSystem() {
		if (!features.contains(ScriptFeature.Simulation)) return;

		String systemCalc="";
		String systemTime="";
		String systemWarmUp="";
		String systemWIP="";
		String systemNQ="";
		String systemWIPAll="";
		String systemNQAll="";
		String systemVar="";
		String systemSetAnalogValue="";
		String systemSetAnalogRate="";
		String systemSetAnalogMaxFlow="";
		String systemResourceGetAll="";
		String systemResourceGet="";
		String systemResourceSet="";
		String systemResourceDown="";
		String systemAllResourceDown="";
		String systemSignal="";
		String systemLog="";
		String systemRunPlugin="";

		if (language==ScriptMode.Javascript) {
			systemCalc="Simulation.calc(\"1+2\");";
			systemTime="Simulation.time();";
			systemWarmUp="Simulation.isWarmUp();";
			systemWIP="Simulation.getWIP(id);";
			systemNQ="Simulation.getNQ(id);";
			systemWIPAll="Simulation.getWIP();";
			systemNQAll="Simulation.getNQ();";
			systemVar="Simulation.set(\"variable\",123);";
			systemSetAnalogValue="Simulation.setValue(id,123);";
			systemSetAnalogRate="Simulation.setRate(id,Wert);";
			systemSetAnalogMaxFlow="Simulation.setValveMaxFlow(id,1,123);";
			systemResourceGetAll="Simulation.getAllResourceCount();";
			systemResourceGet="Simulation.getResourceCount(resourceId);";
			systemResourceSet="Simulation.setResourceCount(resourceId,123);";
			systemResourceDown="Simulation.getResourceDown(resourceId);";
			systemAllResourceDown="Simulation.getAllResourceDown();";
			systemSignal="Simulation.signal(\"signalName\");";
			systemLog="Simulation.log(\""+Language.tr("ScriptPopup.Simulation.Log.ExampleMessage")+"\");";
		}

		if (language==ScriptMode.Java) {
			systemCalc="sim.getSystem().calc(\"1+2\");";
			systemTime="sim.getSystem().getTime();";
			systemWarmUp="sim.getSystem().isWarmUp();";
			systemWIP="sim.getSystem().getWIP(id);";
			systemNQ="sim.getSystem().getNQ(id);";
			systemWIPAll="sim.getSystem().getWIP();";
			systemNQAll="sim.getSystem().getNQ();";
			systemVar="sim.getSystem().set(\"variable\",123);";
			systemSetAnalogValue="sim.getSystem().setAnalogValue(id,123);";
			systemSetAnalogRate="sim.getSystem().setAnalogRate(id,123);";
			systemSetAnalogMaxFlow="sim.getSystem().setAnalogValveMaxFlow(id,1,123);";
			systemResourceGetAll="sim.getSystem().getAllResourceCount();";
			systemResourceGet="sim.getSystem().getResourceCount(resourceId);";
			systemResourceSet="sim.getSystem().setResourceCount(resourceId,123);";
			systemResourceDown="sim.getSystem().getResourceDown(resourceId);";
			systemAllResourceDown="sim.getSystem().getAllResourceDown();";
			systemSignal="sim.getSystem().signal(\"signalName\");";
			systemLog="sim.getSystem().log(\""+Language.tr("ScriptPopup.Simulation.Log.ExampleMessage")+"\");";
			systemRunPlugin="sim.getSystem().runPlugin(\"className\",\"methodName\",userData);";
		}

		addAutoComplete(Language.tr("ScriptPopup.Simulation.Calc"),Language.tr("ScriptPopup.Simulation.Calc.Hint"),Images.SCRIPT_RECORD_EXPRESSION.getIcon(),systemCalc);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.Time"),Language.tr("ScriptPopup.Simulation.Time.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),systemTime);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.IsWarmUp"),Language.tr("ScriptPopup.Simulation.IsWarmUp.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),systemWarmUp);

		addAutoComplete(Language.tr("ScriptPopup.Simulation.getWIP"),Language.tr("ScriptPopup.Simulation.getWIP.Hint"),Images.SCRIPT_RECORD_DATA_STATION.getIcon(),systemWIP);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.getNQ"),Language.tr("ScriptPopup.Simulation.getNQ.Hint"),Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon(),systemNQ);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.getWIPAll"),Language.tr("ScriptPopup.Simulation.getWIPAll.Hint"),Images.SCRIPT_RECORD_DATA_STATION.getIcon(),systemWIPAll);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.getNQAll"),Language.tr("ScriptPopup.Simulation.getNQAll.Hint"),Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon(),systemNQAll);

		addAutoComplete(Language.tr("ScriptPopup.Simulation.setVariable"),Language.tr("ScriptPopup.Simulation.setVariable.Hint"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),systemVar);

		addAutoComplete(Language.tr("ScriptPopup.Simulation.setAnalogValue"),Language.tr("ScriptPopup.Simulation.setAnalogValue.Hint"),Images.SCRIPT_RECORD_ANALOG_VALUE.getIcon(),systemSetAnalogValue);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.setAnalogRate"),Language.tr("ScriptPopup.Simulation.setAnalogRate.Hint"),Images.SCRIPT_RECORD_ANALOG_VALUE.getIcon(),systemSetAnalogRate);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.setAnalogValveMaxFlow"),Language.tr("ScriptPopup.Simulation.setAnalogValveMaxFlow.Hint"),Images.SCRIPT_RECORD_ANALOG_VALUE.getIcon(),systemSetAnalogMaxFlow);

		addAutoComplete(Language.tr("ScriptPopup.Simulation.getAllResourceCount"),Language.tr("ScriptPopup.Simulation.getAllResourceCount.Hint"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),systemResourceGetAll);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.getResourceCount"),Language.tr("ScriptPopup.Simulation.getResourceCount.Hint"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),systemResourceGet);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.setResourceCount"),Language.tr("ScriptPopup.Simulation.setResourceCount.Hint"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),systemResourceSet);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.getResourceDown"),Language.tr("ScriptPopup.Simulation.getResourceDown.Hint"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),systemResourceDown);
		addAutoComplete(Language.tr("ScriptPopup.Simulation.getAllResourceDown"),Language.tr("ScriptPopup.Simulation.getAllResourceDown.Hint"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),systemAllResourceDown);

		addAutoComplete(Language.tr("ScriptPopup.Simulation.Signal"),Language.tr("ScriptPopup.Simulation.Signal.Hint"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),systemSignal);

		addAutoComplete(Language.tr("ScriptPopup.Simulation.Log"),Language.tr("ScriptPopup.Simulation.Log.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),systemLog);



		if (language==ScriptMode.Java) {
			addAutoComplete(Language.tr("ScriptPopup.Simulation.runPlugin"),Language.tr("ScriptPopup.Simulation.runPlugin.Hint"),Images.SCRIPT_FILE.getIcon(),systemRunPlugin);
		}
	}

	/**
	 * Erstellt die AutoComplete-Einträge zur
	 * Anzeige der Befehle für den Zugriff auf die Eigenschaften einzelner Kunden.
	 * @see ScriptFeature#Client
	 */
	private void buildClient() {
		if (!features.contains(ScriptFeature.Client)) return;

		String clientCalc="";
		String clientTypeName="";
		String clientWarmUp="";
		String clientInStatistics="";
		String clientSetInStatistics="";
		String clientNumber="";
		String clientWaitingSeconds="";
		String clientWaitingTime="";
		String clientWaitingSecondsSet="";
		String clientTransferSeconds="";
		String clientTransferTime="";
		String clientTransferSecondsSet="";
		String clientProcessSeconds="";
		String clientProcessTime="";
		String clientProcessSecondsSet="";
		String clientGetValue="";
		String clientSetValue="";
		String clientGetText="";
		String clientSetText="";

		String batchClientSize="";
		String batchClientTypeName="";
		String batchClientWaitingSeconds="";
		String batchClientWaitingTime="";
		String batchClientTransferSeconds="";
		String batchClientTransferTime="";
		String batchClientProcessSeconds="";
		String batchClientProcessTime="";
		String batchClientGetValue="";
		String batchClientGetText="";

		if (language==ScriptMode.Javascript) {
			clientCalc="Simulation.calc(\"1+2\");";
			clientTypeName="Simulation.clientTypeName();";
			clientWarmUp="Simulation.isWarmUpClient();";
			clientInStatistics="Simulation.isClientInStatistics();";
			clientSetInStatistics="Simulation.setClientInStatistics(true);";
			clientNumber="Simulation.clientNumber();";

			clientWaitingSeconds="Simulation.clientWaitingSeconds();";
			clientWaitingTime="Simulation.clientWaitingTime();";
			clientWaitingSecondsSet="Simulation.clientWaitingSecondsSet(123.456);";
			clientTransferSeconds="Simulation.clientTransferSeconds();";
			clientTransferTime="Simulation.clientTransferTime();";
			clientTransferSecondsSet="Simulation.clientTransferSecondsSet(123.456);";
			clientProcessSeconds="Simulation.clientProcessSeconds();";
			clientProcessTime="Simulation.clientProcessTime();";
			clientProcessSecondsSet="Simulation.clientProcessSecondsSet(123.456);";

			clientGetValue="Simulation.getClientValue(index);";
			clientSetValue="Simulation.setClientValue(index,123);";
			clientGetText="Simulation.getClientText(\"key\");";
			clientSetText="Simulation.setClientText(\"key\",\"value\");";

			batchClientSize="Simulation.batchSize();";
			batchClientTypeName="Simulation.batchClientTypeName(batchIndex);";

			batchClientWaitingSeconds="Simulation.batchClientWaitingSeconds(batchIndex);";
			batchClientWaitingTime="Simulation.batchClientWaitingTime(batchIndex);";
			batchClientTransferSeconds="Simulation.batchClientTransferSeconds(batchIndex);";
			batchClientTransferTime="Simulation.batchClientTransferTime(batchIndex);";
			batchClientProcessSeconds="Simulation.batchClientProcessSeconds(batchIndex);";
			batchClientProcessTime="Simulation.batchClientProcessTime(batchIndex);";

			batchClientGetValue="Simulation.getBatchClientValue(batchIndex,index);";
			batchClientGetText="Simulation.getBatchClientText(batchIndex,\"key\");";
		}

		if (language==ScriptMode.Java) {
			clientCalc="sim.getClient().calc(\"1+2\");";
			clientTypeName="sim.getClient().getTypeName();";
			clientWarmUp="sim.getClient().isWarmUp();";
			clientInStatistics="sim.getClient().isInStatistics();";
			clientSetInStatistics="sim.getClient().setInStatistics(true);";
			clientNumber="sim.getClient().getNumber();";

			clientWaitingSeconds="sim.getClient().getWaitingSeconds();";
			clientWaitingTime="sim.getClient().getWaitingTime();";
			clientWaitingSecondsSet="sim.getClient().setWaitingSeconds(123.456);";
			clientTransferSeconds="sim.getClient().getTransferSeconds();";
			clientTransferTime="sim.getClient().getTransferTime();";
			clientTransferSecondsSet="sim.getClient().setTransferSeconds(123.456);";
			clientProcessSeconds="sim.getClient().getProcessSeconds();";
			clientProcessTime="sim.getClient().getProcessTime();";
			clientProcessSecondsSet="sim.getClient().setProcessSeconds(123.456);";

			clientGetValue="sim.getClient().getValue(index);";
			clientSetValue="sim.getClient().setValue(index,123);";
			clientGetText="sim.getClient().getText(\"key\");";
			clientSetText="sim.getClient().setText(\"key\",\"value\");";

			batchClientSize="sim.getClient().batchSize();";
			batchClientTypeName="sim.getClient().getBatchTypeName(batchIndex);";

			batchClientWaitingSeconds="sim.getClient().getBatchWaitingSeconds(batchIndex);";
			batchClientWaitingTime="sim.getClient().getBatchWaitingTime(batchIndex);";
			batchClientTransferSeconds="sim.getClient().getBatchTransferSeconds(batchIndex);";
			batchClientTransferTime="sim.getClient().getBatchTransferTime(batchIndex);";
			batchClientProcessSeconds="sim.getClient().getBatchProcessSeconds(batchIndex);";
			batchClientProcessTime="sim.getClient().getBatchProcessTime(batchIndex);";

			batchClientGetValue="sim.getClient().getBatchValue(batchIndex,index);";
			batchClientGetText="sim.getClient().getBatchText(batchIndex,\"key\");";
		}

		addAutoComplete(Language.tr("ScriptPopup.Client.Calc"),Language.tr("ScriptPopup.Client.Calc.Hint"),Images.SCRIPT_RECORD_EXPRESSION.getIcon(),clientCalc);

		addAutoComplete(Language.tr("ScriptPopup.Client.getTypeName"),Language.tr("ScriptPopup.Client.getTypeName.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientTypeName);
		addAutoComplete(Language.tr("ScriptPopup.Client.getNumber"),Language.tr("ScriptPopup.Client.getNumber.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientNumber);
		addAutoComplete(Language.tr("ScriptPopup.Client.isWarmUp"),Language.tr("ScriptPopup.Client.isWarmUp.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientWarmUp);
		addAutoComplete(Language.tr("ScriptPopup.Client.isInStatistics"),Language.tr("ScriptPopup.Client.isInStatistics.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientInStatistics);
		addAutoComplete(Language.tr("ScriptPopup.Client.setInStatistics"),Language.tr("ScriptPopup.Client.setInStatistics.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientSetInStatistics);

		addAutoComplete(Language.tr("ScriptPopup.Client.WaitingTime")+" - "+Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.WaitingTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Get.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientWaitingSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Client.WaitingTime")+" - "+Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.WaitingTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.GetText.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientWaitingTime);
		addAutoComplete(Language.tr("ScriptPopup.Client.WaitingTime")+" - "+Language.tr("ScriptPopup.Client.Time.Set"),Language.tr("ScriptPopup.Client.WaitingTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Set.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientWaitingSecondsSet);

		addAutoComplete(Language.tr("ScriptPopup.Client.TransferTime")+" - "+Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.TransferTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Get.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientTransferSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Client.TransferTime")+" - "+Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.TransferTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.GetText.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientTransferTime);
		addAutoComplete(Language.tr("ScriptPopup.Client.TransferTime")+" - "+Language.tr("ScriptPopup.Client.Time.Set"),Language.tr("ScriptPopup.Client.TransferTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Set.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientTransferSecondsSet);

		addAutoComplete(Language.tr("ScriptPopup.Client.ProcessTime")+" - "+Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.ProcessTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Get.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientProcessSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Client.ProcessTime")+" - "+Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.ProcessTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.GetText.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientProcessTime);
		addAutoComplete(Language.tr("ScriptPopup.Client.ProcessTime")+" - "+Language.tr("ScriptPopup.Client.Time.Set"),Language.tr("ScriptPopup.Client.ProcessTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Set.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientProcessSecondsSet);

		addAutoComplete(Language.tr("ScriptPopup.Client.ValueNumber")+" - "+Language.tr("ScriptPopup.Client.ValueNumber.Get"),Language.tr("ScriptPopup.Client.ValueNumber.Hint")+" - "+Language.tr("ScriptPopup.Client.ValueNumber.Get.Hint"),Images.SCRIPT_RECORD_DATA_COUNTER.getIcon(),clientGetValue);
		addAutoComplete(Language.tr("ScriptPopup.Client.ValueNumber")+" - "+Language.tr("ScriptPopup.Client.ValueNumber.Set"),Language.tr("ScriptPopup.Client.ValueNumber.Hint")+" - "+Language.tr("ScriptPopup.Client.ValueNumber.Set.Hint"),Images.SCRIPT_RECORD_DATA_COUNTER.getIcon(),clientSetValue);

		addAutoComplete(Language.tr("ScriptPopup.Client.ValueText")+" - "+Language.tr("ScriptPopup.Client.ValueText.Get"),Language.tr("ScriptPopup.Client.ValueText.Hint")+" - "+Language.tr("ScriptPopup.Client.ValueText.Get.Hint"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),clientGetText);
		addAutoComplete(Language.tr("ScriptPopup.Client.ValueText")+" - "+Language.tr("ScriptPopup.Client.ValueText.Set"),Language.tr("ScriptPopup.Client.ValueText.Hint")+" - "+Language.tr("ScriptPopup.Client.ValueText.Set.Hint"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),clientSetText);

		addAutoComplete(Language.tr("ScriptPopup.Client.getBatchSize"),Language.tr("ScriptPopup.Client.getBatchSize.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),batchClientSize);
		addAutoComplete(Language.tr("ScriptPopup.Client.getTypeName.Batch"),Language.tr("ScriptPopup.Client.getTypeName.Batch.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),batchClientTypeName);

		addAutoComplete(Language.tr("ScriptPopup.Client.WaitingTime.Batch")+" - "+Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.WaitingTime.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Get.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),batchClientWaitingSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Client.WaitingTime.Batch")+" - "+Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.WaitingTime.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.GetText.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),batchClientWaitingTime);

		addAutoComplete(Language.tr("ScriptPopup.Client.TransferTime.Batch")+" - "+Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.TransferTime.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Get.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),batchClientTransferSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Client.TransferTime.Batch")+" - "+Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.TransferTime.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.GetText.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),batchClientTransferTime);

		addAutoComplete(Language.tr("ScriptPopup.Client.ProcessTime.Batch")+" - "+Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.ProcessTime.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Get.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),batchClientProcessSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Client.ProcessTime.Batch")+" - "+Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.ProcessTime.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.GetText.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),batchClientProcessTime);

		addAutoComplete(Language.tr("ScriptPopup.Client.ValueNumber.Batch")+" - "+Language.tr("ScriptPopup.Client.ValueNumber.Get"),Language.tr("ScriptPopup.Client.ValueNumber.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.ValueNumber.Get.Hint"),Images.SCRIPT_RECORD_DATA_COUNTER.getIcon(),batchClientGetValue);

		addAutoComplete(Language.tr("ScriptPopup.Client.ValueText.Batch")+" - "+Language.tr("ScriptPopup.Client.ValueText.Get"),Language.tr("ScriptPopup.Client.ValueText.Batch.Hint")+" - "+Language.tr("ScriptPopup.Client.ValueText.Get.Hint"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),batchClientGetText);
	}

	/**
	 * Erstellt die AutoComplete-Einträge zur
	 * Anzeige der Befehle für den Zugriff auf die Liste der wartenden Kunden.
	 * @see ScriptFeature#ClientsList
	 */
	private void buildClients() {
		if (!features.contains(ScriptFeature.ClientsList)) return;

		String clientsCount="";
		String clientsRelease="";
		String clientsTypeName="";
		String clientsDataGet="";
		String clientsDataSet="";
		String clientsTextDataGet="";
		String clientsTextDataSet="";
		String clientsWaitingSeconds="";
		String clientsWaitingTime="";
		String clientsTransferSeconds="";
		String clientsTransferTime="";
		String clientsProcessSeconds="";
		String clientsProcessTime="";

		if (language==ScriptMode.Javascript) {
			clientsCount="Clients.count();";
			clientsRelease="Clients.release(index);";
			clientsTypeName="Clients.clientTypeName(index);";
			clientsDataGet="Clients.clientData(index,data);";
			clientsDataSet="Clients.clientData(index,data,value);";
			clientsTextDataGet="Clients.clientTextData(index,key);";
			clientsTextDataSet="Clients.clientTextData(index,key,value);";
			clientsWaitingSeconds="Clients.clientWaitingSeconds(index);";
			clientsWaitingTime="Clients.clientWaitingTime(index);";
			clientsTransferSeconds="Clients.clientTransferSeconds(index);";
			clientsTransferTime="Clients.clientTransferTime(index);";
			clientsProcessSeconds="Clients.clientProcessSeconds(index);";
			clientsProcessTime="Clients.clientProcessTime(index);";
		}

		if (language==ScriptMode.Java) {
			clientsCount="sim.getClients().count();";
			clientsRelease="sim.getClients().release(index);";
			clientsTypeName="sim.getClients().clientTypeName(index);";
			clientsDataGet="sim.getClients().clientData(index,data);";
			clientsDataSet="sim.getClients().clientData(index,data,value);";
			clientsTextDataGet="sim.getClients().clientTextData(index,key);";
			clientsTextDataSet="sim.getClients().clientTextData(index,key,value);";
			clientsWaitingSeconds="sim.getClients().clientWaitingSeconds(index);";
			clientsWaitingTime="sim.getClients().clientWaitingTime(index);";
			clientsTransferSeconds="sim.getClients().clientTransferSeconds(index);";
			clientsTransferTime="sim.getClients().clientTransferTime(index);";
			clientsProcessSeconds="sim.getClients().clientProcessSeconds(index);";
			clientsProcessTime="sim.getClients().clientProcessTime(index);";
		}

		addAutoComplete(Language.tr("ScriptPopup.Clients.count"),Language.tr("ScriptPopup.Clients.count.Hint"),Images.SCRIPT_RECORD_DATA_COUNTER.getIcon(),clientsCount);

		addAutoComplete(Language.tr("ScriptPopup.Clients.release"),Language.tr("ScriptPopup.Clients.release.Hint"),Images.SCRIPT_RECORD_RELEASE.getIcon(),clientsRelease);
		addAutoComplete(Language.tr("ScriptPopup.Clients.clientTypeName"),Language.tr("ScriptPopup.Clients.clientTypeName.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientsTypeName);
		addAutoComplete(Language.tr("ScriptPopup.Clients.clientData"),Language.tr("ScriptPopup.Clients.clientData.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientsDataGet);
		addAutoComplete(Language.tr("ScriptPopup.Clients.clientDataSet"),Language.tr("ScriptPopup.Clients.clientDataSet.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientsDataSet);
		addAutoComplete(Language.tr("ScriptPopup.Clients.clientTextData"),Language.tr("ScriptPopup.Clients.clientTextData.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientsTextDataGet);
		addAutoComplete(Language.tr("ScriptPopup.Clients.clientTextDataSet"),Language.tr("ScriptPopup.Clients.clientTextDataSet.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),clientsTextDataSet);

		addAutoComplete(Language.tr("ScriptPopup.Clients.WaitingTime")+" - "+Language.tr("ScriptPopup.Client.Time.Number"),Language.tr("ScriptPopup.Clients.WaitingTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Number.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientsWaitingSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Clients.WaitingTime")+" - "+Language.tr("ScriptPopup.Client.Time.Text"),Language.tr("ScriptPopup.Clients.WaitingTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Text.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientsWaitingTime);

		addAutoComplete(Language.tr("ScriptPopup.Clients.TransferTime")+" - "+Language.tr("ScriptPopup.Client.Time.Number"),Language.tr("ScriptPopup.Clients.TransferTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Number.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientsTransferSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Clients.TransferTime")+" - "+Language.tr("ScriptPopup.Client.Time.Text"),Language.tr("ScriptPopup.Clients.TransferTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Text.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientsTransferTime);

		addAutoComplete(Language.tr("ScriptPopup.Clients.ProcessTime")+" - "+Language.tr("ScriptPopup.Client.Time.Number"),Language.tr("ScriptPopup.Clients.ProcessTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Number.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientsProcessSeconds);
		addAutoComplete(Language.tr("ScriptPopup.Clients.ProcessTime")+" - "+Language.tr("ScriptPopup.Client.Time.Text"),Language.tr("ScriptPopup.Clients.ProcessTime.Hint")+" - "+Language.tr("ScriptPopup.Client.Time.Text.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),clientsProcessTime);
	}

	/**
	 * Erstellt die AutoComplete-Einträge für
	 * "Anzeige der Befehle zur Ausgabe" und "Anzeige der Befehle zur Dateiausgabe".
	 * @param fileMode	Dateiausgabe (<code>true</code>) oder direkte Ausgabe (<code>false</code>)
	 * @param force	Ausgabe im {@link ScriptFeature#Output}-Modus auch dann erzwingen, wenn das Feature nicht aktiviert ist
	 * @see ScriptFeature#FileOutput
	 * @see ScriptFeature#Output
	 */
	private void buildOutput(final boolean fileMode, final boolean force) {
		if (fileMode) {
			if (!features.contains(ScriptFeature.FileOutput)) return;
		} else {
			if (!features.contains(ScriptFeature.Output) && !force) return;
		}

		String outputSetFile="";
		String outputFormatSystem="";
		String outputFormatLocal="";
		String outputFormatFraction="";
		String outputFormatPercent="";
		String outputFormatTime="";
		String outputFormatNumber="";
		String outputSeparatorSemicolon="";
		String outputSeparatorLine="";
		String outputSeparatorTabs="";
		String outputDigits="";
		String outputPrint="";
		String outputPrintln="";
		String outputNewLine="";
		String outputTab="";
		String outputCancel="";

		if (language==ScriptMode.Javascript) {
			final String obj=fileMode?"FileOutput":"Output";
			outputSetFile=obj+".setFile(\"FileName\");";
			outputFormatSystem=obj+".setFormat(\"System\");";
			outputFormatLocal=obj+".setFormat(\"Local\");";
			outputFormatFraction=obj+".setFormat(\"Fraction\");";
			outputFormatPercent=obj+".setFormat(\"Percent\");";
			outputFormatTime=obj+".setFormat(\"Time\");";
			outputFormatNumber=obj+".setFormat(\"Number\");";
			outputSeparatorSemicolon=obj+".setSeparator(\"Semicolon\");";
			outputSeparatorLine=obj+".setSeparator(\"Line\");";
			outputSeparatorTabs=obj+".setSeparator(\"Tabs\");";
			outputDigits=obj+".setDigits(digits);";
			outputPrint=obj+".print(\"Text\");";
			outputPrintln=obj+".println(\"Text\");";
			outputNewLine=obj+".newLine();";
			outputTab=obj+".tab();";
			outputCancel=obj+".cancel();";
		}

		if (language==ScriptMode.Java) {
			final String obj=fileMode?"sim.getFileOutput()":"sim.getOutput()";
			outputSetFile=obj+".setFile(\"FileName\");";
			outputFormatSystem=obj+".setFormat(\"System\");";
			outputFormatLocal=obj+".setFormat(\"Local\");";
			outputFormatFraction=obj+".setFormat(\"Fraction\");";
			outputFormatPercent=obj+".setFormat(\"Percent\");";
			outputFormatTime=obj+".setFormat(\"Time\");";
			outputFormatNumber=obj+".setFormat(\"Number\");";
			outputSeparatorSemicolon=obj+".setSeparator(\"Semicolon\");";
			outputSeparatorLine=obj+".setSeparator(\"Line\");";
			outputSeparatorTabs=obj+".setSeparator(\"Tabs\");";
			outputDigits=obj+".setDigits(digits);";
			outputPrint=obj+".print(\"Text\");";
			outputPrintln=obj+".println(\"Text\");";
			outputNewLine=obj+".newLine();";
			outputTab=obj+".tab();";
			outputCancel=obj+".cancel();";
		}

		if (fileMode) {
			addAutoComplete(Language.tr("ScriptPopup.Output.SetFile"),Language.tr("ScriptPopup.Output.SetFile.Hint"),Images.GENERAL_SAVE.getIcon(),outputSetFile);
		}

		addAutoComplete(Language.tr("ScriptPopup.Output.Print"),Language.tr("ScriptPopup.Output.Print.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),outputPrint);
		addAutoComplete(Language.tr("ScriptPopup.Output.Println"),Language.tr("ScriptPopup.Output.Println.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),outputPrintln);

		addAutoComplete(Language.tr("ScriptPopup.Output.NewLine"),Language.tr("ScriptPopup.Output.NewLine.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),outputNewLine);
		addAutoComplete(Language.tr("ScriptPopup.Output.Tab"),Language.tr("ScriptPopup.Output.Tab.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),outputTab);
		if ((fileMode && features.contains(ScriptFeature.FileOutput)) || (!fileMode && features.contains(ScriptFeature.Output))) {
			addAutoComplete(Language.tr("ScriptPopup.Output.Cancel"),Language.tr("ScriptPopup.Output.Cancel.Hint"),Images.SCRIPT_CANCEL.getIcon(),outputCancel);
		}

		addAutoComplete(Language.tr("ScriptPopup.Output.Format.System"),Language.tr("ScriptPopup.Output.Format.System.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputFormatSystem);
		addAutoComplete(Language.tr("ScriptPopup.Output.Format.Local"),Language.tr("ScriptPopup.Output.Format.Local.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputFormatLocal);

		addAutoComplete(Language.tr("ScriptPopup.Output.Format.Fraction"),Language.tr("ScriptPopup.Output.Format.Fraction.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputFormatFraction);
		addAutoComplete(Language.tr("ScriptPopup.Output.Format.Percent"),Language.tr("ScriptPopup.Output.Format.Percent.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputFormatPercent);

		addAutoComplete(Language.tr("ScriptPopup.Output.Format.Time"),Language.tr("ScriptPopup.Output.Format.Time.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputFormatTime);
		addAutoComplete(Language.tr("ScriptPopup.Output.Format.Number"),Language.tr("ScriptPopup.Output.Format.Number.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputFormatNumber);

		addAutoComplete(Language.tr("ScriptPopup.Output.Separator.Semicolon"),Language.tr("ScriptPopup.Output.Separator.Semicolon.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputSeparatorSemicolon);
		addAutoComplete(Language.tr("ScriptPopup.Output.Separator.Line"),Language.tr("ScriptPopup.Output.Separator.Line.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputSeparatorLine);
		addAutoComplete(Language.tr("ScriptPopup.Output.Separator.Tabs"),Language.tr("ScriptPopup.Output.Separator.Tabs.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputSeparatorTabs);

		addAutoComplete(Language.tr("ScriptPopup.Output.Digits"),Language.tr("ScriptPopup.Output.Digits.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon(),outputDigits);
	}

	/**
	 * Erstellt die AutoComplete-Einträge zur
	 * Anzeige der Befehle zum Ändern des Modells.
	 * @see ScriptFeature#Model
	 * @see ScriptFeature#Statistics
	 */
	private void buildModel() {
		if (!features.contains(ScriptFeature.Model) || !features.contains(ScriptFeature.Statistics)) return;

		final String path=Language.tr("Statistic.FastAccess.Template.Parameter.Path");
		final String number=Language.tr("Statistic.FastAccess.Template.Parameter.Number");
		final String resource=Language.tr("Statistic.FastAccess.Template.Parameter.ResourceName");
		final String variable=Language.tr("Statistic.FastAccess.Template.Parameter.VariableName");
		final String expression=Language.tr("Statistic.FastAccess.Template.Parameter.Expression");

		String modelReset="";
		String modelRun="";
		String modelSetValue="";
		String modelSetMean="";
		String modelSetSD="";
		String modelGetRes="";
		String modelSetRes="";
		String modelGetVar="";
		String modelSetVar="";
		String modelGetID="";

		if (language==ScriptMode.Javascript) {
			modelReset="Model.reset();";
			modelRun="Model.run();";
			modelSetValue="Model.setValue(\""+path+"\","+number+");";
			modelSetMean="Model.setMean(\""+path+"\","+number+");";
			modelSetSD="Model.setSD(\""+path+"\","+number+");";
			modelGetRes="Model.getResourceCount(\""+resource+"\");";
			modelSetRes="Model.setResourceCount(\""+resource+"\","+number+");";
			modelGetVar="Model.getGlobalVariableInitialValue(\""+variable+"\");";
			modelSetVar="Model.setGlobalVariableInitialValue(\""+variable+"\",\""+expression+"\");";
			modelGetID="Model.getStationID(\"StationName\");";
		}

		if (language==ScriptMode.Java) {
			modelReset="sim.getModel().reset();";
			modelRun="sim.getModel().run();";
			modelSetValue="sim.getModel().setValue(\""+path+"\","+number+");";
			modelSetMean="sim.getModel().setMean(\""+path+"\","+number+");";
			modelSetSD="sim.getModel().setSD(\""+path+"\","+number+");";
			modelGetRes="sim.getModel().getResourceCount(\""+resource+"\");";
			modelSetRes="sim.getModel().setResourceCount(\""+resource+"\","+number+");";
			modelGetVar="sim.getModel().getGlobalVariableInitialValue(\""+variable+"\");";
			modelSetVar="sim.getModel().setGlobalVariableInitialValue(\""+variable+"\",\""+expression+"\");";
			modelGetID="sim.getModel().getStationID(\"StationName\");";
		}

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.Reset"),Language.tr("Statistic.FastAccess.Template.Reset.Tooltip"),Images.SCRIPT_RECORD_MODEL.getIcon(),modelReset);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.Run"),Language.tr("Statistic.FastAccess.Template.Run.Tooltip"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon(),modelRun);

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.SetValue"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.SetValue.Tooltip"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon(),modelSetValue);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.SetMean"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.SetMean.Tooltip"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon(),modelSetMean);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.SetSD"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.SetSD.Tooltip"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon(),modelSetSD);

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.Resource.Get"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.Resource.Get.Tooltip"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),modelGetRes);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.Resource.Set"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.Resource.Set.Tooltip"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),modelSetRes);

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.Variable.Get"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.Variable.Get.Tooltip"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),modelGetVar);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.Variable.Set"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.Variable.Set.Tooltip"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),modelSetVar);

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.ChangeModel")+" - "+Language.tr("Statistic.FastAccess.Template.StationID.Get"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint")+" - "+Language.tr("Statistic.FastAccess.Template.StationID.Get.Tooltip"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon(),modelGetID);
	}

	/**
	 * Erstellt die AutoComplete-Einträge zur
	 * Anzeige der Befehle für den Abruf des Eingabewertes.
	 * @see ScriptFeature#InputValue
	 */
	private void buildInput() {
		if (!features.contains(ScriptFeature.InputValue)) return;

		String inputGet="";

		if (language==ScriptMode.Javascript) {
			inputGet="Simulation.getInput();";
		}

		if (language==ScriptMode.Java) {
			inputGet="sim.getInputValue().get();";
		}

		addAutoComplete(Language.tr("ScriptPopup.InputValue"),Language.tr("ScriptPopup.InputValue.Hint"),Images.SCRIPT_RECORD_INPUT.getIcon(),inputGet);
	}

	/**
	 * Erstellt die AutoComplete-Einträge zur
	 * Anzeige der Befehle zum Zugriff auf die Statistik.
	 * @param addOutputCommands	Zusätzliche Ausgabebefehle hinzufügen
	 * @see ScriptFeature#Statistics
	 */
	private void buildStatisticsTools(final boolean addOutputCommands) {
		if (!features.contains(ScriptFeature.Statistics)) return;

		/* Allgemeine Funktionen zur Konfiguration der Ausgabe */

		if (addOutputCommands) buildOutput(false,true);

		/* XML-Elemente wählen */

		String statisticsSave="";
		String statisticsSaveNext="";
		String statisticsFilter="";
		String statisticsFileFull="";
		String statisticsFileName="";

		if (language==ScriptMode.Javascript) {
			statisticsSave="Statistics.save(\"FileName\");";
			statisticsSaveNext="Statistics.saveNext(\"Path\");";
			statisticsFilter="Statistics.filter(\"FileName\");";
			statisticsFileFull="Statistics.getStatisticsFile();";
			statisticsFileName="Statistics.getStatisticsFileName();";
		}

		if (language==ScriptMode.Java) {
			statisticsSave="sim.getStatistics().save(\"FileName\");";
			statisticsSaveNext="sim.getStatistics().saveNext(\"Path\");";
			statisticsFilter=""; /* Diese Option gibt's nur im JS-Modus. */
			statisticsFileFull="sim.getStatistics().getStatisticsFile();";
			statisticsFileName="sim.getStatistics().getStatisticsFileName();";
		}

		/* Speichern der Ergebnisse */

		if (features.contains(ScriptFeature.Save)) {
			addAutoComplete(Language.tr("Statistic.FastAccess.Template.SaveStatistics"),Language.tr("Statistic.FastAccess.Template.SaveStatistics.Tooltip"),Images.SCRIPT_RECORD_STATISTICS_SAVE.getIcon(),statisticsSave);
			addAutoComplete(Language.tr("Statistic.FastAccess.Template.SaveStatisticsNext"),Language.tr("Statistic.FastAccess.Template.SaveStatisticsNext.Tooltip"),Images.GENERAL_SAVE.getIcon(),statisticsSaveNext);
			if (!statisticsFilter.isEmpty()) {
				addAutoComplete(Language.tr("Statistic.FastAccess.Template.SaveStatisticsFilter"),Language.tr("Statistic.FastAccess.Template.SaveStatisticsFilter.Tooltip"),Images.GENERAL_SAVE.getIcon(),statisticsFilter);
			}
		}

		/* Übersetzen */

		if (language==ScriptMode.Javascript) {
			addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.de"),Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.de.Tooltip"),Images.LANGUAGE_DE.getIcon(),"Statistics.translate(\"de\");");
			addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.en"),Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.en.Tooltip"),Images.LANGUAGE_EN.getIcon(),"Statistics.translate(\"en\");");
		}

		if (language==ScriptMode.Java) {
			addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.de"),Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.de.Tooltip"),Images.LANGUAGE_DE.getIcon(),"sim.getStatistics().translate(\"de\");");
			addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.en"),Language.tr("Statistic.FastAccess.Template.StatisticsTranslate.en.Tooltip"),Images.LANGUAGE_EN.getIcon(),"sim.getStatistics().translate(\"en\");");
		}

		/* Dateiname der geladenen Statistikdatei */

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsFileFull"),Language.tr("Statistic.FastAccess.Template.StatisticsFileFull.Tooltip"),Images.STATISTICS.getIcon(),statisticsFileFull);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsFileName"),Language.tr("Statistic.FastAccess.Template.StatisticsFileName.Tooltip"),Images.STATISTICS.getIcon(),statisticsFileName);
	}

	/**
	 * Erstellt die AutoComplete-Einträge zur
	 * Anzeige der Befehle zum Zugriff auf die Statistik.
	 * @see ScriptFeature#Statistics
	 */
	private void buildStatistics() {
		if (!features.contains(ScriptFeature.Statistics)) return;

		String statisticsXml="";
		String statisticsXmlNumber="";
		String statisticsXmlArray="";
		String statisticsXmlSum="";
		String statisticsXmlMean="";
		String statisticsXmlSD="";
		String statisticsXmlCV="";
		String statisticsGetID="";

		final String path="\"Path\"";

		if (language==ScriptMode.Javascript) {
			statisticsXml="Statistics.xml("+path+")";
			statisticsXml="Statistics.xmlNumber("+path+")";
			statisticsXmlArray="Statistics.xmlArray("+path+")";
			statisticsXmlSum="Statistics.xmlSum("+path+")";
			statisticsXmlMean="Statistics.xmlMean("+path+")";
			statisticsXmlSD="Statistics.xmlSD("+path+")";
			statisticsXmlCV="Statistics.xmlCV("+path+")";
			statisticsGetID="Statistics.getStationID(\"StationName\");";
		}

		if (language==ScriptMode.Java) {
			statisticsXml="sim.getStatistics().xml("+path+")";
			statisticsXml="sim.getStatistics().xmlNumber("+path+")";
			statisticsXmlArray="sim.getStatistics().xmlArray("+path+")";
			statisticsXmlSum="sim.getStatistics().xmlSum("+path+")";
			statisticsXmlMean="sim.getStatistics().xmlMean("+path+")";
			statisticsXmlSD="sim.getStatistics().xmlSD("+path+")";
			statisticsXmlCV="sim.getStatistics().xmlCV("+path+")";
			statisticsGetID="sim.getStatistics().getStationID(\"StationName\");";
		}

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsXML"),Language.tr("Statistic.FastAccess.Template.StatisticsXML.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),statisticsXml);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsXMLNumber"),Language.tr("Statistic.FastAccess.Template.StatisticsXMLNumber.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),statisticsXmlNumber);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsXMLArray"),Language.tr("Statistic.FastAccess.Template.StatisticsXMLArray.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),statisticsXmlArray);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsXMLSum"),Language.tr("Statistic.FastAccess.Template.StatisticsXMLSum.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),statisticsXmlSum);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsXMLMean"),Language.tr("Statistic.FastAccess.Template.StatisticsXMLMean.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),statisticsXmlMean);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsXMLSD"),Language.tr("Statistic.FastAccess.Template.StatisticsXMLSD.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),statisticsXmlSD);
		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StatisticsXMLCV"),Language.tr("Statistic.FastAccess.Template.StatisticsXMLCV.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon(),statisticsXmlCV);

		addAutoComplete(Language.tr("Statistic.FastAccess.Template.StationID.Get"),Language.tr("Statistic.FastAccess.Template.StationID.Get.Tooltip"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon(),statisticsGetID);
	}
}