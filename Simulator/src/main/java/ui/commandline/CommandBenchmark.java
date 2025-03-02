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
package ui.commandline;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.examples.EditModelExamples;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import systemtools.commandline.AbstractCommand;
import systemtools.commandline.BaseCommandLineSystem;

/**
 * Führt einen Benchmark-Test der Simulatorleistung durch.
 * @author Alexander Herzog
 * @see AbstractCommand
 * @see CommandLineSystem
 */
public final class CommandBenchmark extends AbstractSimulationCommand {
	/** Maximalzahl an Threads (wird natürlich zusätzlich durch den Simulator limitiert) */
	private int maxThreads=Integer.MAX_VALUE;
	/** Benchmark-Modus */
	private int speedTestMode=0;
	/** Modelldatei für Modus 1 */
	private File modelFile;


	/**
	 * Konstruktor der Klasse
	 * @param system	Referenz auf das Kommandozeilensystem
	 */
	public CommandBenchmark(final BaseCommandLineSystem system) {
		super(system);
	}

	@Override
	public String[] getKeys() {

		List<String> list=new ArrayList<>();

		list.add(Language.tr("CommandLine.Benchmark.Name1"));
		for (String s: Language.trOther("CommandLine.Benchmark.Name1")) if (!list.contains(s)) list.add(s);

		if (!list.contains(Language.tr("CommandLine.Benchmark.Name2"))) list.add(Language.tr("CommandLine.Benchmark.Name2"));
		for (String s: Language.trOther("CommandLine.Benchmark.Name2")) if (!list.contains(s)) list.add(s);

		return list.toArray(String[]::new);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	@Override
	public String getShortDescription() {
		return Language.tr("CommandLine.Benchmark.Description.Short");
	}

	@Override
	public String[] getLongDescription() {
		return Language.tr("CommandLine.Benchmark.Description.Long").split("\n");
	}

	@Override
	public String prepare(String[] additionalArguments, InputStream in, PrintStream out) {
		String s=parameterCountCheck(0,1,additionalArguments); if (s!=null) return s;

		if (additionalArguments.length==0) return null;
		String arg=additionalArguments[0];

		Integer I=NumberTools.getNotNegativeInteger(additionalArguments[0]);
		if (I!=null && I!=0) {maxThreads=I; return null;}

		speedTestMode=1;
		modelFile=new File(arg);
		if (!modelFile.isFile()) return String.format(Language.tr("CommandLine.Error.File.InputDoesNotExist"),modelFile.toString());
		if (!isModelFile(modelFile)) return String.format(Language.tr("CommandLine.Error.File.InputNoValidModelFile"),modelFile.toString());
		return null;
	}

	@Override
	public void run(AbstractCommand[] allCommands, InputStream in, PrintStream out) {
		EditModel editModel=null;
		String editModelPath=null;

		style.setColor(Color.GREEN);
		switch (speedTestMode) {
		case 0:
			final String exampleName=EditModelExamples.getBenchmarkExampleName();
			editModel=EditModelExamples.getExampleByIndex(null,EditModelExamples.getExampleIndexFromName(exampleName),false);
			out.println(Language.tr("CommandLine.Benchmark.UsedModel")+": "+exampleName);
			break;
		case 1:
			editModel=new EditModel();
			editModelPath=modelFile.getParent();
			String s=editModel.loadFromFile(modelFile);
			if (s!=null) {
				style.setErrorStyle();
				out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Error.LoadingModel")+": "+s);
				style.setNormalStyle();
				return;
			}
			out.println(Language.tr("CommandLine.Benchmark.UsedModel")+": "+modelFile.getName());
			break;
		}
		style.setColor(null);

		if (editModel==null) return;

		File parentFolder=null;
		if (modelFile!=null) parentFolder=modelFile.getParentFile();
		final EditModel changedEditModel=editModel.modelLoadData.changeModel(editModel,parentFolder);
		if (changedEditModel!=null) {
			outputModelLoadDataWarnings(editModel.modelLoadData.getChangeWarnings(),out);
			editModel=changedEditModel;
		}

		out.println(String.format(Language.tr("CommandLine.Benchmark.Info1"),NumberTools.formatLong(editModel.clientCount)));
		out.println(String.format(Language.tr("CommandLine.Benchmark.Info2"),5));

		for (int i=0;i<5;i++) {
			if (isCanceled()) break;
			if (i>0) {
				style.setBold(true);
				out.println(Language.tr("CommandLine.Benchmark.SimulaionRun")+" "+(i+1));
				style.setBold(false);
			}
			final Statistics statistics=singleSimulation(editModel,editModelPath,true,maxThreads,out,false,-1);
			if (statistics==null) {
				style.setErrorStyle();
				out.println(BaseCommandLineSystem.errorBig+": "+Language.tr("CommandLine.Benchmark.SimulaionFailed"));
				style.setNormalStyle();
				return;
			}
			if (i==0) out.println(Language.tr("CommandLine.Benchmark.Threads")+": "+NumberTools.formatLong(statistics.simulationData.runThreads));
			if (i==0) {
				style.setBold(true);
				out.println(Language.tr("CommandLine.Benchmark.SimulaionRun")+" "+(i+1));
				style.setBold(false);
			}
			out.println("  "+Language.tr("CommandLine.Benchmark.NeededCalculationTime")+": "+NumberTools.formatLong(statistics.simulationData.runTime)+" ms");
			long sum=0;
			for (StatisticsDataPerformanceIndicator indicator: (StatisticsDataPerformanceIndicator[])statistics.clientsInterarrivalTime.getAll(StatisticsDataPerformanceIndicator.class)) sum+=indicator.getCount();

			double d=((double)statistics.simulationData.runTime)*statistics.simulationData.runThreads/((sum>0)?sum:1);
			String s="ms";
			if (d<1) {d=d*1000; s="µs";}
			if (d<1) {d=d*1000; s="ns";}
			out.println("  "+Language.tr("CommandLine.Benchmark.NeededCalculationTimePerClientAndCore")+": "+NumberTools.formatNumber(d,2)+" "+s);

			out.println("  "+Language.tr("CommandLine.Benchmark.EventsPerSecond")+": "+NumberTools.formatLong(1000*statistics.simulationData.runEvents/statistics.simulationData.runTime));

			d=((double)statistics.simulationData.runTime)*statistics.simulationData.runThreads*1000/statistics.simulationData.runEvents;
			s="ms";
			if (d<1) {d=d*1000; s="µs";}
			if (d<1) {d=d*1000; s="ns";}
			out.println("  "+Language.tr("CommandLine.Benchmark.NeededCalculationTimePerEventAndCore")+": "+NumberTools.formatNumber(d,2)+" "+s);
		}
	}
}