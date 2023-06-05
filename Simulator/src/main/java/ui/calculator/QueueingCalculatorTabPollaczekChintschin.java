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
package ui.calculator;

import java.io.Serializable;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;

/**
 * Panel zur Berechnung von Kenngrößen in einem Warteschlangensystem
 * basierend auf der Pollaczek-Chintschin-Formel
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabPollaczekChintschin extends QueueingCalculatorTabBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3576010005571034381L;

	/** lambda (Ankunftsrate) */
	private final QueueingCalculatorInputPanel lambdaInput;
	/** mu (Bedienrate) */
	private final QueueingCalculatorInputPanel muInput;
	/** cvS (Variationskoeffizient der Bedienzeiten) */
	private final QueueingCalculatorInputPanel cvSInput;

	/**
	 * Aktuell eingestellter Wert für lambda
	 * @see #calc()
	 */
	private double lambda;

	/**
	 * Aktuell eingestellter Wert für mu
	 * @see #calc()
	 */
	private double mu;

	/**
	 * Aktuell eingestellter Wert für CV[S]
	 * @see #calc()
	 */
	private double cvS;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabPollaczekChintschin() {
		super(Language.tr("LoadCalculator.Tab.PollaczekChintschinShort"),Language.tr("LoadCalculator.Tab.PollaczekChintschin"),Language.tr("LoadCalculator.Tab.PollaczekChintschin.Link.Info"),Language.tr("LoadCalculator.Tab.PollaczekChintschin.Link"));

		/* Ankunftsrate (lambda) */
		lambdaInput=getPanel(Language.tr("LoadCalculator.ArrivalRate"),true);
		lambdaInput.addDefault("&lambda; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,1.0/100.0,infoRate);
		lambdaInput.addOption("&lambda; ("+unitMinutesInv+")",60,false,infoRate);
		lambdaInput.addOption("&lambda; ("+unitHoursInv+")",3600,false,infoRate);
		lambdaInput.addOption("1/&lambda; ("+unitSeconds+")",1,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitMinutes+")",60,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitHours+")",3600,true,infoInterarrivalTime);
		lambdaInput.setVisibleOptionIndex(3);
		add(lambdaInput.get());

		/* Bedienrate (mu) */
		muInput=getPanel(Language.tr("LoadCalculator.AverageHoldingAndPostProcessingTime"),true);
		muInput.addDefault("&mu; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,1.0/95.0,infoRate);
		muInput.addOption("&mu; ("+unitMinutesInv+")",60,false,infoRate);
		muInput.addOption("&mu; ("+unitHoursInv+")",3600,false,infoRate);
		muInput.addOption("1/&mu; ("+unitSeconds+")",1,true,infoTime);
		muInput.addOption("1/&mu; ("+unitMinutes+")",60,true,infoTime);
		muInput.addOption("1/&mu; ("+unitHours+")",3600,true,infoTime);
		muInput.setVisibleOptionIndex(3);
		add(muInput.get());

		/* Variationskoeffizient der Bedienzeiten (CV[S]) */
		cvSInput=getPanel(Language.tr("LoadCalculator.WorkingRateCV"),false);
		cvSInput.addDefault("CV[S]=",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,1,null);
		add(cvSInput.get());
	}

	@Override
	public void calc() {
		if (!lambdaInput.isValueOk()) {setError(); return;}
		if (!muInput.isValueOk()) {setError(); return;}
		if (!cvSInput.isValueOk()) {setError(); return;}
		lambda=lambdaInput.getDouble();
		mu=muInput.getDouble();
		cvS=cvSInput.getDouble();

		final double a=lambda/mu;
		final double rho=a;
		final double ES=1/mu;
		final double VarS=Math.pow(cvS*ES,2);

		final double ENQ=(rho*rho+lambda*lambda*VarS)/(2*(1-rho));
		final double EN=(rho*rho+lambda*lambda*VarS)/(2*(1-rho))+rho;
		final double EW=(rho*rho+lambda*lambda*VarS)/(2*lambda*(1-rho));
		final double EV=(rho*rho+lambda*lambda*VarS)/(2*lambda*(1-rho))+1/mu;

		final StringBuilder result=new StringBuilder();

		result.append(Language.tr("LoadCalculator.OfferedWorkLoad")+" a="+NumberTools.formatNumber(a,2)+"<br>");
		if (rho+0.000001<1.0) {
			result.append(Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatPercent(rho,2)+"<br>");
		} else {
			result.append(Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatPercent(rho,2)+" ("+Language.tr("LoadCalculator.PollaczekChintschinInvalidWorkLoad")+")<br>");
		}

		if (rho+0.000001<1.0) {
			result.append(Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]="+NumberTools.formatNumber(ENQ,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]="+NumberTools.formatNumber(EN,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]="+NumberTools.formatNumber(EW,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");
			result.append(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]="+NumberTools.formatNumber(EV,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");
			result.append(Language.tr("LoadCalculator.FlowFactor")+" E[V]/E[S]="+NumberTools.formatNumber(EV*mu,2)+"<br>");

		}

		setResult(result.toString());
	}

	@Override
	protected String getHelpPageName() {
		return "pollaczekChintschin";
	}

	@Override
	public EditModel buildModel() {
		final EditModel model=super.buildModel();

		final double meanInterArrivalTime=1/lambda;
		final ModelElementSource source=addSource(model,meanInterArrivalTime,1,1,50,100);

		final double meanServiceTime=1/mu;
		final ModelElementProcess process=addProcess(model,meanServiceTime,cvS,1,1,Language.tr("Editor.Operator.Singular"),250,100);

		final ModelElementDispose dispose=addExit(model,450,100);

		addEdge(model,source,process);
		addEdge(model,process,dispose);

		addText(model,"E[I]="+NumberTools.formatNumber(meanInterArrivalTime)+" "+Language.tr("LoadCalculator.Units.Seconds"),false,50,200);
		addText(model,"E[S]="+NumberTools.formatNumber(meanServiceTime)+" "+Language.tr("LoadCalculator.Units.Seconds"),false,50,220);
		addText(model,"c=1",false,50,240);
		addText(model,"&rho;="+NumberTools.formatPercent(meanServiceTime/meanInterArrivalTime),false,50,260);
		addText(model,"CV[S]="+NumberTools.formatNumber(cvS),false,50,280);

		addExpression(model,Language.tr("LoadCalculator.ModelBuilder.SimRho"),"Resource_avg()/Resource_count()",50,320).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_PERCENT);
		addExpression(model,Language.tr("LoadCalculator.ModelBuilder.SimEW"),"waitingTime_avg()",50,360);
		addExpression(model,Language.tr("LoadCalculator.ModelBuilder.SimEV"),"residenceTime_avg()",50,400);
		addExpression(model,Language.tr("LoadCalculator.ModelBuilder.SimENQ"),"NQ_avg()",50,440);
		addExpression(model,Language.tr("LoadCalculator.ModelBuilder.SimEN"),"WIP_avg()",50,480);

		return model;
	}
}
