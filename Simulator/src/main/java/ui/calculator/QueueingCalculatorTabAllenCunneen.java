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

/**
 * Panel zur Berechnung von Kenngrößen in einem Warteschlangensystem
 * basierend auf der Allen-Cunneen-Formel
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabAllenCunneen extends QueueingCalculatorTabBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6885193560880513420L;

	/** lambda (Ankunftsrate) */
	private final QueueingCalculatorInputPanel lambdaInput;
	/** bI (Ankunfts-Batch-Größe) */
	private final QueueingCalculatorInputPanel bIInput;
	/** mu (Bedienrate) */
	private final QueueingCalculatorInputPanel muInput;
	/** c (Anzahl an Bedienern) */
	private final QueueingCalculatorInputPanel cInput;
	/** bS (Bedien-Batch-Größe) */
	private final QueueingCalculatorInputPanel bSInput;
	/** cvI (Variationskoeffizient der Zwischenankunftszeiten) */
	private final QueueingCalculatorInputPanel cvIInput;
	/** cvS (Variationskoeffizient der Bedienzeiten) */
	private final QueueingCalculatorInputPanel cvSInput;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabAllenCunneen() {
		super("Allen-Cunneen",Language.tr("LoadCalculator.Tab.AllenCunneen"),Language.tr("LoadCalculator.TUCOnlineCalculator"),"https://www.mathematik.tu-clausthal.de/interaktiv/warteschlangentheorie/warteschlangenrechner/");

		/* Ankunftsrate (lambda) */
		lambdaInput=getPanel(Language.tr("LoadCalculator.ArrivalRate"),true);
		lambdaInput.addDefault("&lambda; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,3.5/60,infoRate);
		lambdaInput.addOption("&lambda; ("+unitMinutesInv+")",60,false,infoRate);
		lambdaInput.addOption("&lambda; ("+unitHoursInv+")",3600,false,infoRate);
		lambdaInput.addOption("1/&lambda; ("+unitSeconds+")",1,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitMinutes+")",60,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitHours+")",3600,true,infoInterarrivalTime);
		lambdaInput.setVisibleOptionIndex(1);
		add(lambdaInput.get());

		/* Ankunftsbatchgröße (b(I)) */
		bIInput=getPanel(Language.tr("LoadCalculator.ArrivalBatchSize"),false);
		bIInput.addDefault("b(I)=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,1,null);
		add(bIInput.get());

		/* Bedienrate (mu) */
		muInput=getPanel(Language.tr("LoadCalculator.AverageHoldingAndPostProcessingTime"),true);
		muInput.addDefault("&mu; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,1.0/60/3,infoRate);
		muInput.addOption("&mu; ("+unitMinutesInv+")",60,false,infoRate);
		muInput.addOption("&mu; ("+unitHoursInv+")",3600,false,infoRate);
		muInput.addOption("1/&mu; ("+unitSeconds+")",1,true,infoTime);
		muInput.addOption("1/&mu; ("+unitMinutes+")",60,true,infoTime);
		muInput.addOption("1/&mu; ("+unitHours+")",3600,true,infoTime);
		muInput.setVisibleOptionIndex(4);
		add(muInput.get());

		/* Anzahl Bediener (c) */
		cInput=getPanel(Language.tr("LoadCalculator.Agents"),false);
		cInput.addDefault("c=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,13,null);
		add(cInput.get());

		/* Ankunftsbatchgröße (b(S)) */
		bSInput=getPanel(Language.tr("LoadCalculator.BatchSize"),false);
		bSInput.addDefault("b(S)=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,1,null);
		add(bSInput.get());

		/* Variationskoeffizient der Zwischenankunftszeiten (CV[I]) */
		cvIInput=getPanel(Language.tr("LoadCalculator.ArrivalRateCV"),false);
		cvIInput.addDefault("CV[I]=",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,1,null);
		add(cvIInput.get());

		/* Variationskoeffizient der Bedienzeiten (CV[S]) */
		cvSInput=getPanel(Language.tr("LoadCalculator.WorkingRateCV"),false);
		cvSInput.addDefault("CV[S]=",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,1,null);
		add(cvSInput.get());
	}

	@Override
	public void calc() {
		if (!lambdaInput.isValueOk()) {setError(); return;}
		if (!bIInput.isValueOk()) {setError(); return;}
		if (!muInput.isValueOk()) {setError(); return;}
		if (!cInput.isValueOk()) {setError(); return;}
		if (!bSInput.isValueOk()) {setError(); return;}
		if (!cvIInput.isValueOk()) {setError(); return;}
		if (!cvSInput.isValueOk()) {setError(); return;}

		double lambda=lambdaInput.getDouble();
		final long bI=bIInput.getLong();
		final double mu=muInput.getDouble();
		final long c=cInput.getLong();
		final long bS=bSInput.getLong();
		double cvI=cvIInput.getDouble();
		final double cvS=cvSInput.getDouble();

		/* Umrechnung von Arrival-Batches auf einzelne Kunden */
		lambda=lambda*bI;
		cvI=Math.sqrt(bI*cvI*cvI+bI-1);

		double a=lambda/mu;
		double rho=lambda/mu/(bS*c);

		/*
		PC1=(c*rho)^c/(c!(1-rho));
		PC=PC1/(PC1+sum(k=0...c-1; (c*rho)^k/k!))
		E[NQ]=rho/(1-rho)*PC*(SCV[I]+b*SCV[S])/2+(b-1)/2
		E[N]=E[NQ]+b*c*rho
		 */

		double PC1=powerFactorial(c*rho,c)/(1-rho);
		double PC=0; for(int i=0;i<=c-1;i++) PC+=powerFactorial(c*rho,i);
		PC=PC1/(PC1+PC);

		double ENQ=rho/(1-rho)*PC*(cvI*cvI+bS*cvS*cvS)/2+(((double)bS)-1)/2;
		double EN=ENQ+((double)bS)*((double)c)*rho;
		double EW=ENQ/lambda;
		double EV=EW+1/mu;

		final StringBuilder result=new StringBuilder();

		result.append(Language.tr("LoadCalculator.OfferedWorkLoad")+" a="+NumberTools.formatNumber(a,2)+"<br>");
		if (rho>=1) {
			result.append(Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatPercent(rho,2)+" ("+Language.tr("LoadCalculator.AllenCunneenInvalidWorkLoad")+")");
		} else {
			result.append(Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatPercent(rho,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]="+NumberTools.formatNumber(ENQ,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]="+NumberTools.formatNumber(EN,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]="+NumberTools.formatNumber(EW,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");
			result.append(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]="+NumberTools.formatNumber(EV,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");
		}

		setResult(result.toString());
	}

	@Override
	protected String getHelpPageName() {
		return "allenCunneen";
	}
}
