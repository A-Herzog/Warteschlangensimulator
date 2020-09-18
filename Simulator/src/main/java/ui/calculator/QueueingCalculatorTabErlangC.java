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

import language.Language;
import mathtools.NumberTools;

/**
 * Panel zur Berechnung von Kenngrößen in einem Warteschlangensystem
 * basierend auf der Erlang-C-Formel
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabErlangC extends QueueingCalculatorTabBase {
	private static final long serialVersionUID = 3700930282332096430L;

	/** lambda (Ankunftsrate) */
	private final QueueingCalculatorInputPanel lambdaInput;
	/** mu (Bedienrate) */
	private final QueueingCalculatorInputPanel muInput;
	/** c (Anzahl an Bedienern) */
	private final QueueingCalculatorInputPanel cInput;
	/** t (Wartezeit zu dem der Service-Level berechnet werden soll) */
	private final QueueingCalculatorInputPanel tInput;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabErlangC() {
		super(Language.tr("LoadCalculator.Tab.ErlangC"),"P(W&le;t)=1-P1+exp(-&mu;(c-a)&middot;t)",Language.tr("LoadCalculator.Tab.ErlangC.Link.Info"),Language.tr("LoadCalculator.Tab.ErlangC.Link"));

		/* Ankunftsrate (lambda) */
		lambdaInput=getPanel(Language.tr("LoadCalculator.ArrivalRate"));
		lambdaInput.addDefault("&lambda; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,3.5/60,infoRate);
		lambdaInput.addOption("&lambda; ("+unitMinutesInv+")",60,false,infoRate);
		lambdaInput.addOption("&lambda; ("+unitHoursInv+")",3600,false,infoRate);
		lambdaInput.addOption("1/&lambda; ("+unitSeconds+")",1,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitMinutes+")",60,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitHours+")",3600,true,infoInterarrivalTime);
		lambdaInput.setVisibleOptionIndex(1);
		add(lambdaInput.get());

		/* Bedienrate (mu) */
		muInput=getPanel(Language.tr("LoadCalculator.AverageHoldingAndPostProcessingTime"));
		muInput.addDefault("&mu; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,1.0/60/3,infoRate);
		muInput.addOption("&mu; ("+unitMinutesInv+")",60,false,infoRate);
		muInput.addOption("&mu; ("+unitHoursInv+")",3600,false,infoRate);
		muInput.addOption("1/&mu; ("+unitSeconds+")",1,true,infoTime);
		muInput.addOption("1/&mu; ("+unitMinutes+")",60,true,infoTime);
		muInput.addOption("1/&mu; ("+unitHours+")",3600,true,infoTime);
		muInput.setVisibleOptionIndex(4);
		add(muInput.get());

		/* Anzahl Bediener (c) */
		cInput=getPanel(Language.tr("LoadCalculator.Agents"));
		cInput.addDefault("c=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,13,null);
		add(cInput.get());

		/* Service-Level-Zeit (t) */
		tInput=getPanel(Language.tr("LoadCalculator.WaitingTime"));
		tInput.addDefault("t ("+unitSeconds+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,20,null);
		tInput.addOption("t ("+unitMinutes+")",60,false,null);
		tInput.addOption("t ("+unitHours+")",3600,false,null);
		add(tInput.get());
	}

	@Override
	public void calc() {
		if (!lambdaInput.isValueOk()) {setError(); return;}
		if (!muInput.isValueOk()) {setError(); return;}
		if (!cInput.isValueOk()) {setError(); return;}
		if (!tInput.isValueOk()) {setError(); return;}
		final double lambda=lambdaInput.getDouble();
		final double mu=muInput.getDouble();
		final long c=cInput.getLong();
		final double t=tInput.getDouble();

		double a=lambda/mu;
		double P1=0;
		for (int i=0;i<=c-1;i++)
			P1+=powerFactorial(a,i);
		double temp=powerFactorial(a,c)*c/(c-a);
		P1=temp/(P1+temp);

		double EW=P1/(c*mu-lambda);
		double EV=EW+1/mu;
		double ENQ=P1*a/(c-a);
		double EN=lambda*EV;

		final StringBuilder result=new StringBuilder();

		result.append(Language.tr("LoadCalculator.OfferedWorkLoad")+" a="+NumberTools.formatNumber(a,2)+"<br>");
		result.append(Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatPercent(a/c,2)+"<br>");
		result.append("P1="+NumberTools.formatNumber(P1,2)+"<br>");

		result.append(Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]="+NumberTools.formatNumber(ENQ,2)+"<br>");
		result.append(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]="+NumberTools.formatNumber(EN,2)+"<br>");
		result.append(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]="+NumberTools.formatNumber(EW,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");
		result.append(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]="+NumberTools.formatNumber(EV,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");

		if (P1>=1 || P1<0) {
			result.append("P(W&le;t) "+Language.tr("LoadCalculator.ErlangCNotCalculateable"));
		} else {
			result.append("P(W&le;t)="+NumberTools.formatPercent((1-P1*Math.exp(-mu*(c-a)*t)),2));
		}

		setResult(result.toString());
	}

	@Override
	protected String getHelpPageName() {
		return "erlangC";
	}
}
