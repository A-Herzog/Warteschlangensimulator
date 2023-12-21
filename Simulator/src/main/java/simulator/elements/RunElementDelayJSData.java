package simulator.elements;

import java.util.HashMap;
import java.util.Map;

import language.Language;
import mathtools.NumberTools;
import scripting.java.ClientImpl;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.coreelements.RunElementData;
import simulator.events.StationLeaveEvent;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.elements.ModelElementDecideJS;

/**
 * Laufzeitdaten eines {@link RunElementDelayJS}-Laufzeit-Objekts
 * @author Alexander Herzog
 * @see RunElementDelayJS
 * @see RunElementData
 */
public class RunElementDelayJSData extends RunElementData {
	/**
	 * Das auszuführende Skript.
	 */
	public final String script;

	/**
	 * JS-Skript-Ausführungsumgebung
	 */
	private final JSRunSimulationData jsRunner;

	/**
	 * Java-Ausführungsumgebung
	 */
	private final DynamicRunner javaRunner;

	/**
	 * Ausgabeobjekt das die Java-Ausgaben aufnimmt.
	 * (In der Javascript-Variante erfolgen Rückgaben direkt,
	 * so dass kein solches Objekt benötigt wird.)
	 * @see #getDelayTimeJava(SimulationData, RunDataClient)
	 */
	private final StringBuilder output;

	/**
	 * Kosten pro Bedienung
	 */
	public final ExpressionCalc costs;

	/**
	 * Liste der Kunden an der Station (kann <code>null</code> sein)
	 */
	public final Map<RunDataClient,StationLeaveEvent> clientsList;

	/**
	 * Konstruktor der Klasse
	 * @param station	Station zu diesem Datenelement
	 * @param script	Bei der Verzweigung von Kunden auszuführendes Skript
	 * @param mode	Skriptsprache
	 * @param jRunner	Im Falle von Java als Sprache kann hier optional ein bereits vorbereiteter Runner, der dann kopiert wird, angegeben werden
	 * @param simData	Simulationsdatenobjekt
	 * @param variableNames	Liste der global verfügbaren Variablennamen
	 * @param costs	Kosten pro Bedienvorgang (kann <code>null</code> sein)
	 * @param hasClientsList	Soll eine Liste der Kunden an der Station geführt werden?
	 */
	public RunElementDelayJSData(final RunElementDelayJS station, final String script, final ModelElementDecideJS.ScriptMode mode, final DynamicRunner jRunner, final SimulationData simData, final String[] variableNames, final String costs, final boolean hasClientsList) {
		super(station,simData);

		/* Skript */
		this.script=script;

		switch (mode) {
		case Java:
			jsRunner=null;
			if (jRunner==null) {
				javaRunner=DynamicFactory.getFactory().load(script,simData.runModel.javaImports);
			} else {
				javaRunner=DynamicFactory.getFactory().load(jRunner,simData.runModel.javaImports);
			}
			javaRunner.parameter.client=new ClientImpl(simData);
			javaRunner.parameter.system=new SystemImpl(simData,station.id);
			output=new StringBuilder();
			javaRunner.parameter.output=new OutputImpl(s->output.append(s),false);
			break;
		case Javascript:
			jsRunner=new JSRunSimulationData(true,false);
			jsRunner.compile(script);
			javaRunner=null;
			output=null;
			break;
		default:
			jsRunner=null;
			javaRunner=null;
			output=null;
		}

		/* Kosten pro Bedienung */
		if (costs==null || costs.trim().isEmpty()) {
			this.costs=null;
		} else {
			this.costs=new ExpressionCalc(variableNames);
			this.costs.parse(costs);
		}

		/* Liste der Kunden an der Station (kann <code>null</code> sein) */
		clientsList=hasClientsList?new HashMap<>():null;
	}

	/**
	 * Ermittelt die Verzögerungsueit auf Basis des hinterlegten Skripts.<br>
	 * (Skriptsprache: Javascript)
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde (auf seine Daten kann im Skript zugegriffen werden)
	 * @return	1-basierende Nummer des zu verwendenden Ausgangspfads
	 * @see #getDelayTime(SimulationData, RunDataClient)
	 */
	private double getDelayTimeJS(final SimulationData simData, final RunDataClient client) {
		jsRunner.setSimulationData(simData,station.id,client);
		String result=jsRunner.runCompiled();
		if (simData.loggingActive) station.logJS(simData,script,result);
		if (!jsRunner.getLastSuccess()) {
			/* Logging */
			if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DelayJS"),String.format(Language.tr("Simulation.Log.DelayJS.ErrorScript"),station.name));
			/* Evtl. Abbruch der Simulation */
			if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DelayJS.ErrorScript"),station.name)+" "+result);
			return 0;
		} else {
			if (jsRunner.isOutputDouble()) return jsRunner.getOutputDouble();
			result=result.trim();
			final Double D=NumberTools.getPlainDouble(result);
			if (D==null) {
				/* Logging */
				if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DelayJS"),String.format(Language.tr("Simulation.Log.DelayJS.ErrorNoNumber"),station.name,result));
				/* Evtl. Abbruch der Simulation */
				if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DelayJS.ErrorNoNumber"),station.name,result));
				return 0;
			}
			simData.runData.updateMapValuesForStatistics(simData);
			return D;
		}
	}

	/**
	 * Ermittelt die Verzögerungsueit auf Basis des hinterlegten Skripts.<br>
	 * (Skriptsprache: Java)
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde (auf seine Daten kann im Skript zugegriffen werden)
	 * @return	1-basierende Nummer des zu verwendenden Ausgangspfads
	 * @see #getDelayTime(SimulationData, RunDataClient)
	 */
	private double getDelayTimeJava(final SimulationData simData, final RunDataClient client) {
		javaRunner.parameter.client.setClient(client);
		javaRunner.run();
		if (javaRunner.getStatus()!=DynamicStatus.OK) {
			/* Logging */
			if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DelayJS"),String.format(Language.tr("Simulation.Log.DelayJS.ErrorScript"),station.name)+"\n"+DynamicFactory.getLongStatusText(javaRunner));
			/* Evtl. Abbruch der Simulation */
			if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DelayJS.ErrorScript"),station.name)+"\n"+DynamicFactory.getLongStatusText(javaRunner));
			return 0;
		} else {
			output.setLength(0);
			if (javaRunner.parameter.output.isOutputDouble()) return javaRunner.parameter.output.getOutputDouble();
			final String result=output.toString().trim();
			final Double D=NumberTools.getPlainDouble(result);
			if (D==null) {
				/* Logging */
				if (simData.loggingActive) station.log(simData,Language.tr("Simulation.Log.DelayJS"),String.format(Language.tr("Simulation.Log.DelayJS.ErrorNoNumber"),station.name,result));
				/* Evtl. Abbruch der Simulation */
				if (simData.runModel.cancelSimulationOnScriptError) simData.doEmergencyShutDown(String.format(Language.tr("Simulation.Log.DelayJS.ErrorNoNumber"),station.name,result));
				return 0;
			}
			simData.runData.updateMapValuesForStatistics(simData);
			return D;
		}
	}

	/**
	 * Ermittelt die Verzögerungsueit auf Basis des hinterlegten Skripts.
	 * @param simData	Simulationsdatenobjekt
	 * @param client	Aktueller Kunde (auf seine Daten kann im Skript zugegriffen werden)
	 * @return	Verzögerungszeit (in Sekunden)
	 */
	public double getDelayTime(final SimulationData simData, final RunDataClient client) {
		if (jsRunner!=null) return getDelayTimeJS(simData,client);
		if (javaRunner!=null) return getDelayTimeJava(simData,client);
		return 0;
	}
}
