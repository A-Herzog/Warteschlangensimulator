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

import java.awt.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.elements.RunElementTank;
import simulator.statistics.Statistics;
import ui.ModelChanger;
import ui.images.Images;
import ui.modeleditor.ModelResource;
import ui.modeleditor.elements.ModelElementAnalogValue;
import ui.parameterseries.ParameterCompareInputValuesTemplates;
import ui.parameterseries.ParameterCompareInputValuesTemplates.Sub;
import ui.parameterseries.ParameterCompareSetupValueInput;

/**
 * Ermöglicht das Anzeigen eines Popupmenüs zum Einfügen von Befehlen
 * in ein Eingabefeld.
 * @author Alexander Herzog
 * @see ScriptEditorPanel
 */
public class ScriptPopup {
	/**
	 * Verwendete Sprache
	 * @author Alexander Herzog
	 */
	public enum ScriptMode {
		/**
		 * Javascript
		 */
		Javascript,

		/**
		 * Java
		 */
		Java
	}

	/**
	 * Art der Ausgabe eines aus einem XML-Knoten ausgelesenen Eintrags
	 * @author Alexander Herzog
	 * @see ScriptPopupItemStatistics
	 */
	public enum XMLMode {
		/**
		 * Ausgabe als Text
		 */
		XML_TEXT,

		/**
		 * Wert ist eine Zeitangabe; Ausgabe als Zahl
		 */
		XML_NUMBER_TIME,

		/**
		 * Ausgabe als Zahl
		 */
		XML_NUMBER
	}

	/**
	 * Ermöglicht die Konfiguration der Einträge, die im Popupmenü angezeigt werden sollen.
	 * @author Alexander Herzog
	 */
	public enum ScriptFeature {
		/**
		 * Javascript-System-Objekt (steht nur zur Verfügung, wenn das Simulation-Objekt nicht vorhanden ist).<br>
		 * Hat auf die Darstellung der Befehle für Java keine Auswirkung.
		 */
		JSSystem,

		/**
		 * Anzeige der Befehle für den Zugriff auf die Simulationseigenschaften.
		 */
		Simulation,

		/**
		 * Anzeige der Befehle für den Zugriff auf die Eigenschaften einzelner Kunden.
		 */
		Client,

		/**
		 * Anzeige der Befehle für den Zugriff auf die Liste der wartenden Kunden.
		 */
		ClientsList,

		/**
		 * Anzeige der Befehle für den Abruf des Eingabewertes.
		 */
		InputValue,

		/**
		 * Anzeige der Befehle zur Ausgabe.
		 */
		Output,

		/**
		 * Anzeige der Befehle zur Dateiausgabe.
		 */
		FileOutput,

		/**
		 * Anzeige der Befehle zum ändern des Modells (muss gemeinsam mit <code>Statistics</code> verwendet werden).
		 */
		Model,

		/**
		 * Anzeige der Befehle zum Zugriff auf die Statistik.
		 */
		Statistics,

		/**
		 * Anzeige von Befehlen zum Speichern der Statistikdaten.
		 */
		Save,
	}

	private final Component owner;
	private EditModel model;
	private Statistics statistics;
	private final ScriptMode scriptMode;
	private final Runnable help;

	private final Set<ScriptFeature> features;

	private final ScriptPopupItemSub root;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Popupmenüs)
	 * @param model	Editor-Modell (welches Daten wie Ids bereitstellt, um die Befehle mit Parametern zu versorgen)
	 * @param statistics	Statistik-Objekt (welches Daten bereitstellt, um die Befehle mit Parametern zu versorgen)
	 * @param scriptMode	Skriptsprache
	 * @param help	Hilfe-Runnable
	 */
	public ScriptPopup(final Component owner, final EditModel model, final Statistics statistics, final ScriptMode scriptMode, final Runnable help) {
		this.owner=owner;
		this.model=model;
		this.statistics=statistics;
		this.scriptMode=scriptMode;
		this.help=help;

		this.features=new HashSet<>();

		root=new ScriptPopupItemSub(null,null,null);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Popupmenüs)
	 * @param model	Editor-Modell (welches Daten wie Ids bereitstellt, um die Befehle mit Parametern zu versorgen)
	 * @param scriptMode	Skriptsprache
	 * @param help	Hilfe-Runnable
	 */
	public ScriptPopup(final Component owner, final EditModel model, final ScriptMode scriptMode, final Runnable help) {
		this(owner,model,null,scriptMode,help);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Popupmenüs)
	 * @param statistics	Statistik-Objekt (welches Daten bereitstellt, um die Befehle mit Parametern zu versorgen)
	 * @param scriptMode	Skriptsprache
	 * @param help	Hilfe-Runnable
	 */
	public ScriptPopup(final Component owner, final Statistics statistics, final ScriptMode scriptMode, final Runnable help) {
		this(owner,statistics.editModel,statistics,scriptMode,help);
	}

	/**
	 * Fügt eine Eigenschaft zu dem Popupmenü hinzu
	 * @param feature	Hinzuzufügende Eigenschaft
	 * @see ScriptPopup.ScriptFeature
	 */
	public void addFeature(final ScriptFeature feature) {
		features.add(feature);
	}

	/**
	 * Fügt eine oder mehrere Eigenschaften zu dem Popupmenü hinzu
	 * @param features	Hinzuzufügende Eigenschaften
	 * @see ScriptPopup.ScriptFeature
	 */
	public void addFeatures(final ScriptFeature[] features) {
		if (features!=null) this.features.addAll(Arrays.asList(features));
	}

	/**
	 * Stellt das Popupmenü zusammen.<br>
	 * Diese Methode muss vor der Anzeige aufgerufen werden.
	 */
	public void build() {
		buildRuntime(root);
		buildSystem(root);
		buildClient(root);
		buildClients(root);
		buildInput(root);
		buildOutput(root,false,false,false);
		buildOutput(root,false,true,false);

		if (features.contains(ScriptFeature.Model)) {
			buildModel(root);
			final ScriptPopupItemSub group=new ScriptPopupItemSub(Language.tr("ScriptPopup.Statistics"),Language.tr("ScriptPopup.Statistics.Hint"),Images.SCRIPT_RECORD_STATISTICS.getIcon());
			root.addChild(group);
			buildAdd(group);
			buildStatisticsTools(group,false);
			buildStatistics(group);
		} else {
			buildAdd(root);
			buildStatisticsTools(root,true);
			buildStatistics(root);
		}
	}

	private void buildRuntime(final ScriptPopupItemSub parent) {
		if (scriptMode==ScriptMode.Javascript && !features.contains(ScriptFeature.JSSystem)) return;

		String runtimeCalc="";
		String runtimeTime="";
		String runtimeLoad="";

		if (scriptMode==ScriptMode.Javascript && features.contains(ScriptFeature.JSSystem)) {
			runtimeCalc="System.calc(\"1+2\");";
			runtimeTime="System.time();";
			runtimeLoad="System.getInput(\"https://www.valuegetter\",-1);";
		}

		if (scriptMode==ScriptMode.Java) {
			runtimeCalc="sim.getRuntime().calc(\"1+2\");";
			runtimeTime="sim.getRuntime().getTime();";
			runtimeLoad="sim.getRuntime().getInput(\"https://www.valuegetter\",-1);";
		}

		final ScriptPopupItemSub group=new ScriptPopupItemSub(Language.tr("ScriptPopup.Runtime"),Language.tr("ScriptPopup.Runtime.Hint"),Images.SCRIPT_RECORD_RUNTIME.getIcon());

		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Runtime.Calc"),Language.tr("ScriptPopup.Runtime.Calc.Hint"),Images.SCRIPT_RECORD_EXPRESSION.getIcon(),runtimeCalc));
		group.addChild(new ScriptPopupItemExpressionBuilder(Language.tr("ScriptPopup.Runtime.CalcByExpressionBuilder"),Language.tr("ScriptPopup.Runtime.CalcByExpressionBuilder.Hint"),statistics,scriptMode));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Runtime.Time"),Language.tr("ScriptPopup.Runtime.Time.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),runtimeTime));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Runtime.LoadValue"),Language.tr("ScriptPopup.Runtime.LoadValue.Hint"),Images.SCRIPT_RECORD_INPUT.getIcon(),runtimeLoad));

		parent.addChild(group);
	}

	private void buildSystem(final ScriptPopupItemSub parent) {
		if (!features.contains(ScriptFeature.Simulation)) return;
		if (model==null) return;

		String systemCalc="";
		String systemTime="";
		String systemWarmUp="";
		String systemWIP="";
		String systemNQ="";
		String systemVar="";
		String systemSetAnalogValue="";
		String systemSetAnalogRate="";
		String systemSetAnalogMaxFlow="";
		String systemResourceGetAll="";
		String systemResourceGet="";
		String systemResourceSet="";

		if (scriptMode==ScriptMode.Javascript) {
			systemCalc="Simulation.calc(\"1+2\");";
			systemTime="Simulation.time();";
			systemWarmUp="Simulation.isWarmUp();";
			systemWIP="Simulation.getWIP(%s)";
			systemNQ="Simulation.getNQ(%s)";
			systemVar="Simulation.set(\"%s\",123)";
			systemSetAnalogValue="Simulation.setValue(%s,123)";
			systemSetAnalogRate="Simulation.setRate(%s,Wert)";
			systemSetAnalogMaxFlow="Simulation.setValveMaxFlow(%s,1,123)";
			systemResourceGetAll="Simulation.getAllResourceCount()";
			systemResourceGet="Simulation.getResourceCount(resourceId)";
			systemResourceSet="Simulation.setResourceCount(resourceId,123)";
		}

		if (scriptMode==ScriptMode.Java) {
			systemCalc="sim.getSystem().calc(\"1+2\");";
			systemTime="sim.getSystem().getTime();";
			systemWarmUp="sim.getSystem().isWarmUp();";
			systemWIP="sim.getSystem().getWIP(%s);";
			systemNQ="sim.getSystem().getNQ(%s);";
			systemVar="sim.getSystem().set(\"%s\",123);";
			systemSetAnalogValue="sim.getSystem().setAnalogValue(%s,123);";
			systemSetAnalogRate="sim.getSystem().setAnalogRate(%s,123);";
			systemSetAnalogMaxFlow="sim.getSystem().setAnalogValveMaxFlow(%s,1,123);";
			systemResourceGetAll="sim.getSystem().getAllResourceCount();";
			systemResourceGet="sim.getSystem().getResourceCount(resourceId);";
			systemResourceSet="sim.getSystem().setResourceCount(resourceId,123);";
		}

		final ScriptPopupItemSub group=new ScriptPopupItemSub(Language.tr("ScriptPopup.Simulation"),Language.tr("ScriptPopup.Simulation.Hint"),Images.SIMULATION.getIcon());
		ScriptPopupItemSub sub;
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Simulation.Calc"),Language.tr("ScriptPopup.Simulation.Calc.Hint"),Images.SCRIPT_RECORD_EXPRESSION.getIcon(),systemCalc));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Simulation.Time"),Language.tr("ScriptPopup.Simulation.Time.Hint"),Images.SCRIPT_RECORD_TIME.getIcon(),systemTime));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Simulation.IsWarmUp"),Language.tr("ScriptPopup.Simulation.IsWarmUp.Hint"),null,systemWarmUp));
		group.addSeparator();
		group.addChild(new ScriptPopupItemCommandID(Language.tr("ScriptPopup.Simulation.getWIP"),Language.tr("ScriptPopup.Simulation.getWIP.Hint"),Images.SCRIPT_RECORD_DATA_STATION.getIcon(),systemWIP,owner,model,help,true));
		group.addChild(new ScriptPopupItemCommandID(Language.tr("ScriptPopup.Simulation.getNQ"),Language.tr("ScriptPopup.Simulation.getNQ.Hint"),Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon(),systemNQ,owner,model,help,true));
		group.addSeparator();
		group.addChild(new ScriptPopupItemCommandSetVariable(Language.tr("ScriptPopup.Simulation.setVariable"),Language.tr("ScriptPopup.Simulation.setVariable.Hint"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),systemVar,owner,model,help));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Simulation.AnalogValue"),Language.tr("ScriptPopup.Simulation.AnalogValue.Hint"),Images.SCRIPT_RECORD_ANALOG_VALUE.getIcon()));
		sub.addChild(new ScriptPopupItemCommandID(Language.tr("ScriptPopup.Simulation.setAnalogValue"),Language.tr("ScriptPopup.Simulation.setAnalogValue.Hint"),null,systemSetAnalogValue,owner,model,help,new Class<?>[]{
			ModelElementAnalogValue.class,
			RunElementTank.class
		}));
		sub.addChild(new ScriptPopupItemCommandID(Language.tr("ScriptPopup.Simulation.setAnalogRate"),Language.tr("ScriptPopup.Simulation.setAnalogRate.Hint"),null,systemSetAnalogRate,owner,model,help,new Class<?>[]{
			ModelElementAnalogValue.class
		}));
		sub.addChild(new ScriptPopupItemCommandID(Language.tr("ScriptPopup.Simulation.setAnalogValveMaxFlow"),Language.tr("ScriptPopup.Simulation.setAnalogValveMaxFlow.Hint"),null,systemSetAnalogMaxFlow,owner,model,help,new Class<?>[]{
			RunElementTank.class
		}));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Simulation.Resources"),Language.tr("ScriptPopup.Simulation.Resources.Hint"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Simulation.getAllResourceCount"),Language.tr("ScriptPopup.Simulation.getAllResourceCount.Hint"),null,systemResourceGetAll));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Simulation.getResourceCount"),Language.tr("ScriptPopup.Simulation.getResourceCount.Hint"),null,systemResourceGet));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Simulation.setResourceCount"),Language.tr("ScriptPopup.Simulation.setResourceCount.Hint"),null,systemResourceSet));

		parent.addChild(group);
	}

	private void buildClient(final ScriptPopupItemSub parent) {
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

		if (scriptMode==ScriptMode.Javascript) {
			clientCalc="Simulation.calc(\"1+2\");";
			clientTypeName="Simulation.clientTypeName()";
			clientWarmUp="Simulation.isWarmUpClient()";
			clientInStatistics="Simulation.isClientInStatistics()";
			clientSetInStatistics="Simulation.setClientInStatistics(true)";
			clientNumber="Simulation.clientNumber()";
			clientWaitingSeconds="Simulation.clientWaitingSeconds()";
			clientWaitingTime="Simulation.clientWaitingTime()";
			clientWaitingSecondsSet="Simulation.clientWaitingSecondsSet(123.456);";
			clientTransferSeconds="Simulation.clientTransferSeconds()";
			clientTransferTime="Simulation.clientTransferTime()";
			clientTransferSecondsSet="Simulation.clientTransferSecondsSet(123.456);";
			clientProcessSeconds="Simulation.clientProcessSeconds()";
			clientProcessTime="Simulation.clientProcessTime()";
			clientProcessSecondsSet="Simulation.clientProcessSecondsSet(123.456);";
			clientGetValue="Simulation.getClientValue(index)";
			clientSetValue="Simulation.setClientValue(index,123)";
			clientGetText="Simulation.getClientText(\"key\")";
			clientSetText="Simulation.setClientText(\"key\",\"value\")";
		}

		if (scriptMode==ScriptMode.Java) {
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
		}

		final ScriptPopupItemSub group=new ScriptPopupItemSub(Language.tr("ScriptPopup.Client"),Language.tr("ScriptPopup.Client.Hint"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon());
		ScriptPopupItemSub sub;
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Calc"),Language.tr("ScriptPopup.Client.Calc.Hint"),Images.SCRIPT_RECORD_EXPRESSION.getIcon(),clientCalc));
		group.addSeparator();
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.getTypeName"),Language.tr("ScriptPopup.Client.getTypeName.Hint"),null,clientTypeName));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.getNumber"),Language.tr("ScriptPopup.Client.getNumber.Hint"),null,clientNumber));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.isWarmUp"),Language.tr("ScriptPopup.Client.isWarmUp.Hint"),null,clientWarmUp));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.isInStatistics"),Language.tr("ScriptPopup.Client.isInStatistics.Hint"),null,clientInStatistics));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.setInStatistics"),Language.tr("ScriptPopup.Client.setInStatistics.Hint"),null,clientSetInStatistics));
		group.addSeparator();
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Client.WaitingTime"),Language.tr("ScriptPopup.Client.WaitingTime.Hint"),Images.SCRIPT_RECORD_TIME.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.Time.Get.Hint"),null,clientWaitingSeconds));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.Time.GetText.Hint"),null,clientWaitingTime));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Set"),Language.tr("ScriptPopup.Client.Time.Set.Hint"),null,clientWaitingSecondsSet));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Client.TransferTime"),Language.tr("ScriptPopup.Client.TransferTime.Hint"),Images.SCRIPT_RECORD_TIME.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.Time.Get.Hint"),null,clientTransferSeconds));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.Time.GetText.Hint"),null,clientTransferTime));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Set"),Language.tr("ScriptPopup.Client.Time.Set.Hint"),null,clientTransferSecondsSet));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Client.ProcessTime"),Language.tr("ScriptPopup.Client.ProcessTime.Hint"),Images.SCRIPT_RECORD_TIME.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Get"),Language.tr("ScriptPopup.Client.Time.Get.Hint"),null,clientProcessSeconds));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.GetText"),Language.tr("ScriptPopup.Client.Time.GetText.Hint"),null,clientProcessTime));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Set"),Language.tr("ScriptPopup.Client.Time.Set.Hint"),null,clientProcessSecondsSet));
		group.addSeparator();
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Client.ValueNumber"),Language.tr("ScriptPopup.Client.ValueNumber.Hint"),Images.SCRIPT_RECORD_DATA_COUNTER.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.ValueNumber.Get"),Language.tr("ScriptPopup.Client.ValueNumber.Get.Hint"),null,clientGetValue));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.ValueNumber.Set"),Language.tr("ScriptPopup.Client.ValueNumber.Set.Hint"),null,clientSetValue));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Client.ValueText"),Language.tr("ScriptPopup.Client.ValueText.Hint"),Images.SCRIPT_RECORD_VARIABLE.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.ValueText.Get"),Language.tr("ScriptPopup.Client.ValueText.Get.Hint"),null,clientGetText));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.ValueText.Set"),Language.tr("ScriptPopup.Client.ValueText.Set.Hint"),null,clientSetText));

		parent.addChild(group);
	}

	private void buildClients(final ScriptPopupItemSub parent) {
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

		if (scriptMode==ScriptMode.Javascript) {
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

		if (scriptMode==ScriptMode.Java) {
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

		final ScriptPopupItemSub group=new ScriptPopupItemSub(Language.tr("ScriptPopup.ClientsList"),Language.tr("ScriptPopup.ClientsList.Hint"),Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon());
		ScriptPopupItemSub sub;
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Clients.count"),Language.tr("ScriptPopup.Clients.count.Hint"),Images.SCRIPT_RECORD_DATA_COUNTER.getIcon(),clientsCount));
		group.addSeparator();
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Clients.release"),Language.tr("ScriptPopup.Clients.release.Hint"),Images.SCRIPT_RECORD_RELEASE.getIcon(),clientsRelease));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Clients.clientTypeName"),Language.tr("ScriptPopup.Clients.clientTypeName.Hint"),null,clientsTypeName));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Clients.clientData"),Language.tr("ScriptPopup.Clients.clientData.Hint"),null,clientsDataGet));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Clients.clientDataSet"),Language.tr("ScriptPopup.Clients.clientDataSet.Hint"),null,clientsDataSet));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Clients.clientTextData"),Language.tr("ScriptPopup.Clients.clientTextData.Hint"),null,clientsTextDataGet));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Clients.clientTextDataSet"),Language.tr("ScriptPopup.Clients.clientTextDataSet.Hint"),null,clientsTextDataSet));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Clients.WaitingTime"),Language.tr("ScriptPopup.Clients.WaitingTime.Hint"),Images.SCRIPT_RECORD_TIME.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Number"),Language.tr("ScriptPopup.Client.Time.Number.Hint"),null,clientsWaitingSeconds));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Text"),Language.tr("ScriptPopup.Client.Time.Text.Hint"),null,clientsWaitingTime));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Clients.TransferTime"),Language.tr("ScriptPopup.Clients.TransferTime.Hint"),Images.SCRIPT_RECORD_TIME.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Number"),Language.tr("ScriptPopup.Client.Time.Number.Hint"),null,clientsTransferSeconds));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Text"),Language.tr("ScriptPopup.Client.Time.Text.Hint"),null,clientsTransferTime));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Clients.ProcessTime"),Language.tr("ScriptPopup.Clients.ProcessTime.Hint"),Images.SCRIPT_RECORD_TIME.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Number"),Language.tr("ScriptPopup.Client.Time.Number.Hint"),null,clientsProcessSeconds));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Client.Time.Text"),Language.tr("ScriptPopup.Client.Time.Text.Hint"),null,clientsProcessTime));

		parent.addChild(group);
	}

	private void buildInput(final ScriptPopupItemSub parent) {
		if (!features.contains(ScriptFeature.InputValue)) return;

		String inputGet="";

		if (scriptMode==ScriptMode.Javascript) {
			inputGet="Simulation.getInput();";
		}

		if (scriptMode==ScriptMode.Java) {
			inputGet="sim.getInputValue().get();";
		}

		final ScriptPopupItemCommand cmd=new ScriptPopupItemCommand(Language.tr("ScriptPopup.InputValue"),Language.tr("ScriptPopup.InputValue.Hint"),Images.SCRIPT_RECORD_INPUT.getIcon(),inputGet);

		parent.addChild(cmd);
	}

	private void buildOutput(final ScriptPopupItemSub parent, final boolean leanMode, final boolean fileMode, final boolean force) {
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
		String outputPrint="";
		String outputPrintln="";
		String outputNewLine="";
		String outputTab="";
		String outputCancel="";

		if (scriptMode==ScriptMode.Javascript) {
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
			outputPrint=obj+".print(\"Text\");";
			outputPrintln=obj+".println(\"Text\");";
			outputNewLine=obj+".newLine();";
			outputTab=obj+".tab();";
			outputCancel=obj+".cancel();";
		}

		if (scriptMode==ScriptMode.Java) {
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
			outputPrint=obj+".print(\"Text\");";
			outputPrintln=obj+".println(\"Text\");";
			outputNewLine=obj+".newLine();";
			outputTab=obj+".tab();";
			outputCancel=obj+".cancel();";
		}

		final ScriptPopupItemSub group;
		if (leanMode) {
			group=parent;
		} else {
			if (fileMode) {
				group=new ScriptPopupItemSub(Language.tr("ScriptPopup.FileOutput"),Language.tr("ScriptPopup.FileOutput.Hint"),Images.GENERAL_SAVE.getIcon());
			} else {
				group=new ScriptPopupItemSub(Language.tr("ScriptPopup.Output"),Language.tr("ScriptPopup.Output.Hint"),Images.SCRIPT_RECORD_OUTPUT.getIcon());
			}
		}

		ScriptPopupItemSub sub;
		if (fileMode) {
			group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.SetFile"),Language.tr("ScriptPopup.Output.SetFile.Hint"),Images.GENERAL_SAVE.getIcon(),outputSetFile));
			group.addSeparator();
		}
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Print"),Language.tr("ScriptPopup.Output.Print.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),outputPrint));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Println"),Language.tr("ScriptPopup.Output.Println.Hint"),Images.SCRIPT_RECORD_TEXT.getIcon(),outputPrintln));
		group.addSeparator();
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.NewLine"),Language.tr("ScriptPopup.Output.NewLine.Hint"),null,outputNewLine));
		group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Tab"),Language.tr("ScriptPopup.Output.Tab.Hint"),null,outputTab));
		if ((fileMode && features.contains(ScriptFeature.FileOutput)) || (!fileMode && features.contains(ScriptFeature.Output))) {
			group.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Cancel"),Language.tr("ScriptPopup.Output.Cancel.Hint"),Images.SCRIPT_CANCEL.getIcon(),outputCancel));
		}
		group.addSeparator();
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Output.Format"),Language.tr("ScriptPopup.Output.Format.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Format.System"),Language.tr("ScriptPopup.Output.Format.System.Hint"),null,outputFormatSystem));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Format.Local"),Language.tr("ScriptPopup.Output.Format.Local.Hint"),null,outputFormatLocal));
		sub.addSeparator();
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Format.Fraction"),Language.tr("ScriptPopup.Output.Format.Fraction.Hint"),null,outputFormatFraction));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Format.Percent"),Language.tr("ScriptPopup.Output.Format.Percent.Hint"),null,outputFormatPercent));
		sub.addSeparator();
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Format.Time"),Language.tr("ScriptPopup.Output.Format.Time.Hint"),null,outputFormatTime));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Format.Number"),Language.tr("ScriptPopup.Output.Format.Number.Hint"),null,outputFormatNumber));
		group.addChild(sub=new ScriptPopupItemSub(Language.tr("ScriptPopup.Output.Separator"),Language.tr("ScriptPopup.Output.Separator.Hint"),Images.SCRIPT_RECORD_FORMAT.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Separator.Semicolon"),Language.tr("ScriptPopup.Output.Separator.Semicolon.Hint"),null,outputSeparatorSemicolon));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Separator.Line"),Language.tr("ScriptPopup.Output.Separator.Line.Hint"),null,outputSeparatorLine));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("ScriptPopup.Output.Separator.Tabs"),Language.tr("ScriptPopup.Output.Separator.Tabs.Hint"),null,outputSeparatorTabs));

		if (group!=parent) parent.addChild(group);
	}

	private void buildAdd(final ScriptPopupItemSub parent) {
		if (!features.contains(ScriptFeature.Statistics)) return;
		if (statistics==null) return;

		parent.addSeparator();

		if (features.contains(ScriptFeature.Model) && model!=null) {
			parent.addChild(new ScriptPopupItemXML(Language.tr("Statistic.FastAccess.SelectXMLTag.Statistics"),Language.tr("Statistic.FastAccess.SelectXMLTag.Statistics.Tooltip"),statistics,help,scriptMode));
		} else {
			parent.addChild(new ScriptPopupItemXML(Language.tr("Statistic.FastAccess.SelectXMLTag"),Language.tr("Statistic.FastAccess.SelectXMLTag.Tooltip"),statistics,help,scriptMode));
		}
	}

	private void buildModel(final ScriptPopupItemSub parent) {
		if (!features.contains(ScriptFeature.Model) || !features.contains(ScriptFeature.Statistics)) return;
		if (statistics==null || model==null) return;

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

		if (scriptMode==ScriptMode.Javascript) {
			modelReset="Model.reset();";
			modelRun="Model.run();";
			modelSetValue="Model.setValue(\""+path+"\","+number+");";
			modelSetMean="Model.setMean(\""+path+"\","+number+");";
			modelSetSD="Model.setSD(\""+path+"\","+number+");";
			modelGetRes="Model.getResourceCount(\""+resource+"\");";
			modelSetRes="Model.setResourceCount(\""+resource+"\","+number+");";
			modelGetVar="Model.getGlobalVariableInitialValue(\""+variable+"\");";
			modelSetVar="Model.setGlobalVariableInitialValue(\""+variable+"\",\""+expression+"\");";
		}

		if (scriptMode==ScriptMode.Java) {
			modelReset="sim.getModel().reset();";
			modelRun="sim.getModel().run();";
			modelSetValue="sim.getModel().setValue(\""+path+"\","+number+");";
			modelSetMean="sim.getModel().setMean(\""+path+"\","+number+");";
			modelSetSD="sim.getModel().setSD(\""+path+"\","+number+");";
			modelGetRes="sim.getModel().getResourceCount(\""+resource+"\");";
			modelSetRes="sim.getModel().setResourceCount(\""+resource+"\","+number+");";
			modelGetVar="sim.getModel().getGlobalVariableInitialValue(\""+variable+"\");";
			modelSetVar="sim.getModel().setGlobalVariableInitialValue(\""+variable+"\",\""+expression+"\");";
		}

		ScriptPopupItemSub sub;

		parent.addSeparator();

		parent.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.Reset"),Language.tr("Statistic.FastAccess.Template.Reset.Tooltip"),Images.SCRIPT_RECORD_MODEL.getIcon(),modelReset));
		parent.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.Run"),Language.tr("Statistic.FastAccess.Template.Run.Tooltip"),Images.SIMULATION.getIcon(),modelRun));

		parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ChangeModel"),Language.tr("Statistic.FastAccess.Template.ChangeModel.Hint"),Images.SCRIPT_RECORD_MODEL_EDIT.getIcon()));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.SetValue"),Language.tr("Statistic.FastAccess.Template.SetValue.Tooltip"),null,modelSetValue));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.SetMean"),Language.tr("Statistic.FastAccess.Template.SetMean.Tooltip"),null,modelSetMean));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.SetSD"),Language.tr("Statistic.FastAccess.Template.SetSD.Tooltip"),null,modelSetSD));
		sub.addSeparator();
		sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.Resource.Get"),Language.tr("Statistic.FastAccess.Template.Resource.Get.Tooltip"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),modelGetRes));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.Resource.Set"),Language.tr("Statistic.FastAccess.Template.Resource.Set.Tooltip"),Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon(),modelSetRes));
		sub.addSeparator();
		sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.Variable.Get"),Language.tr("Statistic.FastAccess.Template.Variable.Get.Tooltip"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),modelGetVar));
		sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.Variable.Set"),Language.tr("Statistic.FastAccess.Template.Variable.Set.Tooltip"),Images.SCRIPT_RECORD_VARIABLE.getIcon(),modelSetVar));

		sub.addSeparator();
		final List<Object> list=new ParameterCompareInputValuesTemplates(model).getList();
		sub.addChild(new ScriptPopupItemTitle(Language.tr("Statistic.FastAccess.ChangeModelParameters")));
		sub.addChild(new ScriptPopupItemXML(Language.tr("Statistic.FastAccess.SelectXMLTag.Model"),Language.tr("Statistic.FastAccess.SelectXMLTag.Model.Tooltip"),model,help,scriptMode));
		addInputValuesToScriptPopup(sub,list);

		parent.addSeparator();
	}

	private void addInputValuesToScriptPopup(final ScriptPopupItemSub parent, final List<Object> list) {
		for (Object obj: list) {
			if (obj instanceof ParameterCompareSetupValueInput) {
				final ParameterCompareSetupValueInput input=(ParameterCompareSetupValueInput)obj;
				final ScriptPopupItemCommand command=new ScriptPopupItemCommand(input.getName(),null,ParameterCompareInputValuesTemplates.getIcon(input.getMode()),inputToScript(input));
				parent.addChild(command);
				continue;
			}
			if (obj instanceof Sub) {
				final Sub sub=(Sub)obj;
				if (sub.list.size()>0) {
					final ScriptPopupItemSub menu=new ScriptPopupItemSub(sub.title,null,ParameterCompareInputValuesTemplates.getIcon(sub.iconMode));
					parent.addChild(menu);
					addInputValuesToScriptPopup(menu,sub.list);
				}
				continue;
			}
		}
	}

	private String escape(final String text) {
		return text.replace("\"","\\\"");
	}

	private String inputToScript(final ParameterCompareSetupValueInput input) {
		final String number=Language.tr("Statistic.FastAccess.Template.Parameter.Number");
		final String tag=escape(input.getTag());

		if (input.getMode()==ModelChanger.Mode.MODE_RESOURCE) switch (scriptMode) {
		case Javascript: return "Model.setResourceCount(\""+tag+"\","+number+");";
		case Java: return "sim.getModel().setResourceCount(\""+tag+"\","+number+");";
		default: return "";
		}

		if (input.getMode()==ModelChanger.Mode.MODE_XML) switch (input.getXMLMode()) {
		case 0: /* Zahl */
			switch (scriptMode) {
			case Javascript: return "Model.setValue(\""+tag+"\","+number+");";
			case Java: return "sim.getModel().setValue(\""+tag+"\","+number+");";
			default: return "";
			}
		case 1: /* Erwartungswert einer Verteilung */
			switch (scriptMode) {
			case Javascript: return "Model.setMean(\""+tag+"\","+number+");";
			case Java: return "sim.getModel().setMean(\""+tag+"\","+number+");";
			default: return "";
			}
		case 2: /* Standardabweichung einer Verteilung */
			switch (scriptMode) {
			case Javascript: return "Model.setSD(\""+tag+"\","+number+");";
			case Java: return "sim.getModel().setSD(\""+tag+"\","+number+");";
			default: return "";
			}
		case 3: /* Parameter 1 einer Verteilung */
			switch (scriptMode) {
			case Javascript: return "Model.setDistributionParameter(\""+tag+"\",1,"+number+")";
			case Java: return "sim.getModel().setDistributionParameter(\""+tag+"\",1,"+number+");";
			default: return "";
			}
		case 4: /* Parameter 2 einer Verteilung */
			switch (scriptMode) {
			case Javascript: return "Model.setDistributionParameter(\""+tag+"\",2,"+number+")";
			case Java: return "sim.getModel().setDistributionParameter(\""+tag+"\",2,"+number+");";
			default: return "";
			}
		case 5: /* Parameter 3 einer Verteilung */
			switch (scriptMode) {
			case Javascript: return "Model.setDistributionParameter(\""+tag+"\",3,"+number+")";
			case Java: return "sim.getModel().setDistributionParameter(\""+tag+"\",3,"+number+");";
			default: return "";
			}
		case 6: /* Parameter 4 einer Verteilung */
			switch (scriptMode) {
			case Javascript: return "Model.setDistributionParameter(\""+tag+"\",4,"+number+")";
			case Java: return "sim.getModel().setDistributionParameter(\""+tag+"\",4,"+number+");";
			default: return "";
			}
		default:
			return "";
		}

		return "";
	}

	private void buildStatisticsTools(final ScriptPopupItemSub parent, final boolean addOutputCommands) {
		if (!features.contains(ScriptFeature.Statistics)) return;
		if (statistics==null) return;

		/* Allgemeine Funktionen zur Konfiguration der Ausgabe */

		if (addOutputCommands) buildOutput(parent,true,false,true);

		/* XML-Elemente wählen */

		String statisticsSave="";
		String statisticsSaveNext="";
		String statisticsFilter="";

		if (scriptMode==ScriptMode.Javascript) {
			statisticsSave="Statistics.save(\"FileName\");";
			statisticsSaveNext="Statistics.saveNext(\"Path\");";
			statisticsFilter="Statistics.filter(\"FileName\");";
		}

		if (scriptMode==ScriptMode.Java) {
			statisticsSave="sim.getStatistics().save(\"FileName\");";
			statisticsSaveNext="sim.getStatistics().saveNext(\"Path\");";
			statisticsFilter=""; /* Diese Option gibt's nur im JS-Modus. */
		}

		/* Trenner */

		parent.addSeparator();

		/* Speichern der Ergebnisse */

		if (features.contains(ScriptFeature.Save)) {
			ScriptPopupItemSub sub;
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.Save"),Language.tr("Statistic.FastAccess.Template.Save.Tooltip"),Images.GENERAL_SAVE.getIcon()));
			sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.SaveStatistics"),Language.tr("Statistic.FastAccess.Template.SaveStatistics.Tooltip"),Images.SCRIPT_RECORD_STATISTICS_SAVE.getIcon(),statisticsSave));
			sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.SaveStatisticsNext"),Language.tr("Statistic.FastAccess.Template.SaveStatisticsNext.Tooltip"),null,statisticsSaveNext));
			if (!statisticsFilter.isEmpty()) {
				sub.addChild(new ScriptPopupItemCommand(Language.tr("Statistic.FastAccess.Template.SaveStatisticsFilter"),Language.tr("Statistic.FastAccess.Template.SaveStatisticsFilter.Tooltip"),null,statisticsFilter));
			}
		}
	}

	private void buildStatistics(final ScriptPopupItemSub parent) {
		if (!features.contains(ScriptFeature.Statistics)) return;
		if (statistics==null) return;

		final String mean="["+Language.tr("Statistics.XML.Mean")+"]";
		final String Std="["+Language.tr("Statistics.XML.StdDev")+"]";
		final String CV="["+Language.tr("Statistics.XML.CV")+"]";
		final String Min="["+Language.tr("Statistics.XML.Minimum")+"]";
		final String Max="["+Language.tr("Statistics.XML.Maximum")+"]";
		final String count="["+Language.tr("Statistics.XML.Count")+"]";
		final String value="["+Language.tr("Statistics.XML.Value")+"]";
		final String quotient="["+Language.tr("Statistics.XML.Quotient")+"]";

		String xmlSub;
		ScriptPopupItemSub sub, sub2, main;

		/* Trenner */

		parent.addSeparator();

		/* Wartezeiten */

		parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.WaitingTime"),Language.tr("Statistic.FastAccess.Template.WaitingTime.Tooltip"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
		xmlSub=Language.tr("Statistics.XML.Element.WaitingAllClients");
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));

		if (statistics.clientsWaitingTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.WaitingTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.WaitingClients");
			for (String name: statistics.clientsWaitingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsWaitingTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.WaitingTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.WaitingStations");
			for (String name: statistics.stationsWaitingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsWaitingTimesByClientType.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.WaitingTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.WaitingStationsByClientType");
			for (String name: statistics.stationsWaitingTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		/* Transferzeiten */

		parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.TransferTime"),Language.tr("Statistic.FastAccess.Template.TransferTime.Tooltip"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
		xmlSub=Language.tr("Statistics.XML.Element.TransferAllClients");
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));

		if (statistics.clientsTransferTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.TransferTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.TransferClients");
			for (String name: statistics.clientsWaitingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsTransferTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.TransferTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.TransferStations");
			for (String name: statistics.stationsTransferTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER_TIME,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsTransferTimesByClientType.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.TransferTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.TransferStationsByClientType");
			for (String name: statistics.stationsTransferTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER_TIME,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		/* Bedienzeiten */

		parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ProcessTime"),Language.tr("Statistic.FastAccess.Template.ProcessTime.Tooltip"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
		xmlSub=Language.tr("Statistics.XML.Element.ProcessAllClients");
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));

		if (statistics.clientsProcessingTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ProcessTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ProcessClients");
			for (String name: statistics.clientsProcessingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsProcessingTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ProcessTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ProcessStations");
			for (String name: statistics.stationsProcessingTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER_TIME,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsProcessingTimesByClientType.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ProcessTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ProcessStationsByClientType");
			for (String name: statistics.stationsProcessingTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER_TIME,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		/* Verweilzeiten */

		parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResidenceTime"),Language.tr("Statistic.FastAccess.Template.ResidenceTime.Tooltip"),Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
		xmlSub=Language.tr("Statistics.XML.Element.ResidenceAllClients");
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
		sub.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));

		if (statistics.clientsResidenceTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResidenceTime.ByClientType"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ResidenceClients");
			for (String name: statistics.clientsResidenceTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsResidenceTimes.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResidenceTime.ByStation"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ResidenceStations");
			for (String name: statistics.stationsResidenceTimes.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER_TIME,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		if (statistics.stationsResidenceTimesByClientType.getNames().length>1) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResidenceTime.ByStationClient"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ResidenceStationsByClientType");
			for (String name: statistics.stationsResidenceTimesByClientType.getNames()) {
				xmlSub=xmlMain+"->"+Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]->";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER_TIME,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
				sub.addChild(sub2=new ScriptPopupItemSub(name,null,null));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Average"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+mean,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.StdDev"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Std,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.CV"),null,null,XMLMode.XML_NUMBER,xmlSub+CV,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Minimum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Min,scriptMode));
				sub2.addChild(new ScriptPopupItemStatistics(Language.tr("Statistics.Maximum"),null,null,XMLMode.XML_NUMBER_TIME,xmlSub+Max,scriptMode));
			}
		}

		/* Trenner */

		parent.addSeparator();

		/* Kunden im System */

		parent.addChild(new ScriptPopupItemStatistics(
				Language.tr("Statistic.FastAccess.Template.ClientsInSystem"),
				Language.tr("Statistic.FastAccess.Template.ClientsInSystem.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),
				XMLMode.XML_NUMBER,
				Language.tr("Statistics.XML.Element.ClientsInSystem")+mean,
				scriptMode));

		parent.addChild(new ScriptPopupItemStatistics(
				Language.tr("Statistic.FastAccess.Template.ClientsInSystemQueue"),
				Language.tr("Statistic.FastAccess.Template.ClientsInSystemQueue.Tooltip"),
				Images.SCRIPT_RECORD_DATA_CLIENT.getIcon(),
				XMLMode.XML_NUMBER,
				Language.tr("Statistics.XML.Element.ClientsInSystemQueue")+mean,
				scriptMode));

		/* Kunden an den Stationen */

		if (statistics.clientsInSystemByClient.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsAtStationByStation"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationByType");
			for (String name: statistics.clientsInSystemByClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		if (statistics.clientsAtStationByStation.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsAtStation"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStation");
			for (String name: statistics.clientsAtStationByStation.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		if (statistics.clientsAtStationByStationAndClient.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsAtStationByStationAndType"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationByClientType");
			for (String name: statistics.clientsAtStationByStationAndClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		if (statistics.clientsAtStationQueueByClient.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsInSystemQueue"),null,Images.SCRIPT_RECORD_DATA_CLIENT.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsInSystemQueue");
			for (String name: statistics.clientsAtStationQueueByClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.ClientType")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		if (statistics.clientsAtStationQueueByStation.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsAtStationQueue"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationQueue");
			for (String name: statistics.clientsAtStationQueueByStation.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		if (statistics.clientsAtStationQueueByStationAndClient.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsAtStationQueueByClientType"),null,Images.SCRIPT_RECORD_DATA_STATION_QUEUE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.ClientsAtStationQueueByClientType");
			for (String name: statistics.clientsAtStationQueueByStationAndClient.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Station")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		/* Trenner */

		parent.addSeparator();

		/* Auslastung */

		if (statistics.editModel.resources.getResources().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResourceUtilization"),null,Images.SCRIPT_RECORD_DATA_RESOURCE.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.Utilization");
			for (ModelResource resource: statistics.editModel.resources.getResources()) {
				final String name=resource.getName();
				xmlSub=Language.tr("Statistics.XML.Element.UtilizationResource")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		/* Trenner */

		parent.addSeparator();

		/* Zähler & Nutzerstatistik */

		if (statistics.counter.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.Counter"),null,Images.SCRIPT_RECORD_DATA_COUNTER.getIcon()));
			final String xmlMain=Language.tr("Statistics.XML.Element.Counter");
			for (String name: statistics.counter.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Element.CounterName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+count,scriptMode));
			}
		}

		if (statistics.differentialCounter.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.DifferentialCounter"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.DifferenceCounter");
			for (String name: statistics.differentialCounter.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Element.DifferenceCounterName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		if (statistics.throughputStatistics.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ThroughputCounter"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.ThroughputStatistics");
			for (String name: statistics.throughputStatistics.getNames()) {
				xmlSub=Language.tr("Statistics.XML.ThroughputStatisticsName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+quotient,scriptMode));
			}
		}

		if (statistics.userStatistics.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.UserStatistics"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.UserStatistics");
			for (String name: statistics.userStatistics.getNames()) {
				xmlSub=Language.tr("Statistics.XML.UserStatisticsKey")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		/* Analoge Werte */

		if (statistics.analogStatistics.getNames().length>0) {
			parent.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.AnalogValues"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.AnalogStatistics");
			for (String name: statistics.analogStatistics.getNames()) {
				xmlSub=Language.tr("Statistics.XML.AnalogStatisticsName")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+mean,scriptMode));
			}
		}

		/* Trenner */

		parent.addSeparator();

		/* Kosten */

		parent.addChild(main=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.Costs"),null,Images.SCRIPT_RECORD_DATA_COSTS.getIcon()));

		/* Kosten - Kunden */

		if (statistics.clientsCostsWaiting.getNames().length>0) {
			main.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsCostsWaiting"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsWaiting");
			for (String name: statistics.clientsCostsWaiting.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value,scriptMode));
			}
		}

		if (statistics.clientsCostsTransfer.getNames().length>0) {
			main.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsCostsTransfer"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsTransfer");
			for (String name: statistics.clientsCostsTransfer.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value,scriptMode));
			}
		}

		if (statistics.clientsCostsProcess.getNames().length>0) {
			main.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ClientsCostsProcess"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsProcess");
			for (String name: statistics.clientsCostsProcess.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value,scriptMode));
			}
		}

		/* Kosten - Stationen */

		if (statistics.stationCosts.getNames().length>0) {
			main.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.StationCosts"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.CostsStations");
			for (String name: statistics.stationCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value,scriptMode));
			}
		}

		/* Kosten - Ressourcen */

		if (statistics.resourceTimeCosts.getNames().length>0) {
			main.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResourceTimeCosts"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.ResourceTimeCosts");
			for (String name: statistics.resourceTimeCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value,scriptMode));
			}
		}

		if (statistics.resourceIdleCosts.getNames().length>0) {
			main.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResourceIdleCosts"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.ResourceIdleCosts");
			for (String name: statistics.resourceIdleCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value,scriptMode));
			}
		}

		if (statistics.resourceWorkCosts.getNames().length>0) {
			main.addChild(sub=new ScriptPopupItemSub(Language.tr("Statistic.FastAccess.Template.ResourceWorkCosts"),null,null));
			final String xmlMain=Language.tr("Statistics.XML.Element.ResourceWorkCosts");
			for (String name: statistics.resourceIdleCosts.getNames()) {
				xmlSub=Language.tr("Statistics.XML.Costs")+"["+Language.tr("Statistics.XML.Type")+"=\""+name+"\"]";
				sub.addChild(new ScriptPopupItemStatistics(name,null,null,XMLMode.XML_NUMBER,xmlMain+"->"+xmlSub+"->"+value,scriptMode));
			}
		}
	}

	/**
	 * Zeigt das Popupmenü an.
	 * @param clickedItem	Wird aufgerufen als Reaktion auf den Klick auf einen Eintrag
	 * @param allowAdd	Erlaubt das Vorabprüfen, ob der Befehl im Popupmenü angezeigt werden soll
	 */
	public void show(final Consumer<ScriptPopupItem> clickedItem, final Predicate<ScriptPopupItem> allowAdd) {
		final JPopupMenu popupMenu=new JPopupMenu();
		root.addChildrenToMenu(popupMenu,clickedItem,allowAdd);
		popupMenu.show(owner,0,owner.getHeight());
	}

	/**
	 * Zeigt das Popupmenü an.
	 * @param clickedItem	Wird aufgerufen als Reaktion auf den Klick auf einen Eintrag
	 */
	public void show(final Consumer<ScriptPopupItem> clickedItem) {
		show(clickedItem,allowAdd->true);
	}

	/**
	 * Zeigt das Popupmenü an.
	 * @param textArea	Textfeld in das der Befehl eingefügt werden soll
	 * @param allowAdd	Erlaubt das Vorabprüfen, ob der Befehl im Popupmenü angezeigt werden soll
	 * @param update	Wird benachrichtigt, wenn es Änderungen an dem Textfeld gab
	 */
	public void show(final JTextArea textArea, final Predicate<ScriptPopupItem> allowAdd, final Runnable update) {
		show(item->item.insertIntoTextArea(textArea,update),allowAdd);
	}

	/**
	 * Zeigt das Popupmenü an.
	 * @param textArea	Textfeld in das der Befehl eingefügt werden soll
	 * @param allowAdd	Erlaubt das Vorabprüfen, ob der Befehl im Popupmenü angezeigt werden soll
	 */
	public void show(final JTextArea textArea, final Predicate<ScriptPopupItem> allowAdd) {
		show(item->item.insertIntoTextArea(textArea,null));
	}

	/**
	 * Zeigt das Popupmenü an.
	 * @param textArea	Textfeld in das der Befehl eingefügt werden soll
	 * @param update	Wird benachrichtigt, wenn es Änderungen an dem Textfeld gab
	 */
	public void show(final JTextArea textArea, final Runnable update) {
		show(textArea,allowAdd->true,update);
	}

	/**
	 * Zeigt das Popupmenü an.
	 * @param textArea	Textfeld in das der Befehl eingefügt werden soll
	 */
	public void show(final JTextArea textArea) {
		show(textArea,allowAdd->true,null);
	}
}