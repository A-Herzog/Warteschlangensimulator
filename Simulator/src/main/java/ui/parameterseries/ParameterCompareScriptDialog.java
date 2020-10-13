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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JPanel;

import language.Language;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.StatisticsImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunDataFilter;
import simulator.Simulator;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.script.ScriptEditorPanel;
import ui.script.ScriptPanel;

/**
 * Dieser Dialog ermöglicht das Anwenden von Skripten auf
 * die Statistikausgaben der Parameter-Vergleichs-Simulationen.
 * @author Alexander Herzog
 */
public class ParameterCompareScriptDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 9064720881495989992L;

	private final ScriptPanel scriptPanel;
	private final ParameterCompareSetupModel[] models;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param setup	Parameterreihen-Setup (inkl. der Modelle)
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareScriptDialog(final Component owner, final ParameterCompareSetup setup, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.ScriptRunner.Title"));

		final String[] exampleScript=getExamples(setup);
		final Map<ScriptEditorPanel.ScriptMode,String> examples=new HashMap<>();
		examples.put(ScriptEditorPanel.ScriptMode.Javascript,exampleScript[0]);
		examples.put(ScriptEditorPanel.ScriptMode.Java,exampleScript[1]);

		models=setup.getModels().stream().filter(m->m.isStatisticsAvailable()).toArray(ParameterCompareSetupModel[]::new);
		if (models.length==0) {
			scriptPanel=null;
			return;
		}

		showCloseButton=true;
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());
		content.add(scriptPanel=new ScriptPanel(null,true,ScriptEditorPanel.featuresFilter,examples) {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = -202431138635835392L;
			@Override protected EditModel getModel() {return null;}
			@Override protected Statistics getMiniStatistics() {return models[0].getStatistics();}
			@Override protected Runnable getHelpRunnable() {return help;}
			@Override protected boolean run(final ScriptEditorPanel.ScriptMode mode, final String script) {
				for (ParameterCompareSetupModel model: models) if (!runScriptOnModel(mode,script,model,line->addOutput(line))) return false;
				return true;
			}
		},BorderLayout.CENTER);

		setMinSizeRespectingScreensize(800,600);
		setSizeRespectingScreensize(800,600);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private boolean runScriptOnModel(final ScriptEditorPanel.ScriptMode mode, final String script, final ParameterCompareSetupModel model, final Consumer<String> addOutput) {
		switch (mode) {
		case Javascript:
			final JSRunDataFilter filter=new JSRunDataFilter(model.getStatisticsDocument());
			filter.run(script);
			if (!filter.getLastSuccess()) {
				MsgBox.error(this,Language.tr("ParameterCompare.ScriptRunner.Script.Run.Error.Title"),String.format(Language.tr("ParameterCompare.ScriptRunner.Script.Run.Error.Info"),filter.getResults()));
				return false;
			}
			addOutput.accept(filter.getResults());
			break;
		case Java:
			final DynamicRunner runner=DynamicFactory.getFactory().load(script);
			if (runner.getStatus()!=DynamicStatus.OK) {
				MsgBox.error(this,Language.tr("ParameterCompare.ScriptRunner.Script.Run.Error.Title"),String.format(Language.tr("ParameterCompare.ScriptRunner.Script.Run.Error.Info"),DynamicFactory.getLongStatusText(runner)));
				return false;
			}
			runner.parameter.system=new SystemImpl(Simulator.getSimulationDataFromStatistics(model.getStatistics()));
			runner.parameter.output=new OutputImpl(addOutput,false);
			runner.parameter.statistics=new StatisticsImpl(addOutput,model.getStatisticsDocument(),false);
			runner.run();
			if (runner.getStatus()!=DynamicStatus.OK) {
				MsgBox.error(this,Language.tr("ParameterCompare.ScriptRunner.Script.Run.Error.Title"),String.format(Language.tr("ParameterCompare.ScriptRunner.Script.Run.Error.Info"),DynamicFactory.getLongStatusText(runner)));
				return false;
			}
			break;
		}
		return true;
	}

	private String[] getExamples(final ParameterCompareSetup setup) {
		final StringBuilder js1=new StringBuilder();
		final StringBuilder js2=new StringBuilder();
		final StringBuilder java1=new StringBuilder();
		final StringBuilder java2=new StringBuilder();

		boolean needTab=false;
		boolean isTime=false;
		int nr=1;
		for (ParameterCompareSetupValueOutput output: setup.getOutput()) {
			final ParameterCompareSetupValueOutput.OutputMode mode=output.getMode();
			if (mode==ParameterCompareSetupValueOutput.OutputMode.MODE_XML) {
				/* JS */
				js1.append("var value"+nr+"=Statistics.xmlNumber('"+output.getTag()+"');\n");
				if (needTab) js2.append("Output.tab();\n");
				if (isTime!=output.getIsTime()) {
					if (output.getIsTime()) js2.append("Output.setFormat(\"Time\");\n"); else js2.append("Output.setFormat(\"Number\");\n");
				}
				js2.append("Output.print(value"+nr+");\n");

				/* Java */
				java1.append("  Object value"+nr+"=sim.getStatistics().xmlNumber(\""+output.getTag().replace("\"","\\\"")+"\");\n");
				if (needTab) java2.append("  sim.getOutput().tab();\n");
				if (isTime!=output.getIsTime()) {
					if (output.getIsTime()) java2.append("  sim.getOutput().setFormat(\"Time\");\n"); else java2.append("  sim.getOutput().setFormat(\"Number\");\n");
				}
				java2.append("  sim.getOutput().print(value"+nr+");\n");

				needTab=true;
				isTime=output.getIsTime();
			}
			if (mode==ParameterCompareSetupValueOutput.OutputMode.MODE_COMMAND) {
				/* JS */
				js1.append("var value"+nr+"=System.calc('"+output.getTag()+"');\n");
				if (needTab) js2.append("Output.tab();\n");
				if (isTime!=output.getIsTime()) {
					if (output.getIsTime()) js2.append("Output.setFormat(\"Time\");\n"); else js2.append("Output.setFormat(\"Number\");\n");
				}
				js2.append("Output.print(value"+nr+");\n");

				/* Java */
				java1.append("  Object value"+nr+"=sim.getSystem().calc(\""+output.getTag()+"\");\n");
				if (needTab) java2.append("  sim.getOutput().tab();\n");
				if (isTime!=output.getIsTime()) {
					if (output.getIsTime()) java2.append("  sim.getOutput().setFormat(\"Time\");\n"); else java2.append("  sim.getOutput().setFormat(\"Number\");\n");
				}
				java2.append("  sim.getOutput().print(value"+nr+");\n");

				needTab=true;
				isTime=output.getIsTime();
			}
			nr++;
		}
		if (needTab) {
			js2.append("Output.newLine();\n");
			java2.append("  sim.getOutput().newLine();\n");
		}

		return new String[] {js1.toString()+"\n"+js2.toString(),"void function(SimulationInterface sim) {\n"+java1.toString()+"\n"+java2.toString()+"}\n"};
	}

	@Override
	protected boolean closeButtonOK() {
		return scriptPanel.allowDiscard();
	}
}
