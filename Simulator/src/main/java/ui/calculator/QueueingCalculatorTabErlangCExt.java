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

import org.apache.commons.math3.special.Gamma;

import language.Language;
import mathtools.ErlangC;
import mathtools.NumberTools;

/**
 * Panel zur Berechnung von Kenngrößen in einem Warteschlangensystem
 * basierend auf der erweiterten Erlang-C-Formel
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabErlangCExt extends QueueingCalculatorTabBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3700930282332096430L;

	/** lambda (Ankunftsrate) */
	private final QueueingCalculatorInputPanel lambdaInput;
	/** mu (Bedienrate) */
	private final QueueingCalculatorInputPanel muInput;
	/** nu (Abbruchrate) */
	private final QueueingCalculatorInputPanel nuInput;
	/** c (Anzahl an Bedienern) */
	private final QueueingCalculatorInputPanel cInput;
	/** t (Wartezeit zu dem der Service-Level berechnet werden soll) */
	private final QueueingCalculatorInputPanel tInput;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabErlangCExt() {
		super(Language.tr("LoadCalculator.Tab.ErlangCext"),"P(W&le;t)=1-C<sub>K</sub>&pi;<sub>0</sub>-&pi;<sub>0</sub>&Sigma;<sub>n=c..K</sub>C<sub>n</sub> Q(n-c+1;(c&mu;+&nu;)t)");

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

		/* Abbruchrate (nu) */
		nuInput=getPanel(Language.tr("LoadCalculator.AverageWaitingTimeTolerance"),true);
		nuInput.addDefault("&nu; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,1.0/60/5,infoRate);
		nuInput.addOption("&nu; ("+unitMinutesInv+")",60,false,infoRate);
		nuInput.addOption("&nu; ("+unitHoursInv+")",3600,false,infoRate);
		nuInput.addOption("1/&nu; ("+unitSeconds+")",1,true,infoTime);
		nuInput.addOption("1/&nu; ("+unitMinutes+")",60,true,infoTime);
		nuInput.addOption("1/&nu; ("+unitHours+")",3600,true,infoTime);
		nuInput.setVisibleOptionIndex(4);
		add(nuInput.get());

		/* Anzahl Bediener (c) */
		cInput=getPanel(Language.tr("LoadCalculator.Agents"),false);
		cInput.addDefault("c=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,13,null);
		add(cInput.get());

		/* Service-Level-Zeit (t) */
		tInput=getPanel(Language.tr("LoadCalculator.WaitingTime"),false);
		tInput.addDefault("t ("+unitSeconds+")",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,20,null);
		tInput.addOption("t ("+unitMinutes+")",60,false,null);
		tInput.addOption("t ("+unitHours+")",3600,false,null);
		add(tInput.get());
	}

	@Override
	public void calc() {
		if (!lambdaInput.isValueOk()) {setError(); return;}
		if (!muInput.isValueOk()) {setError(); return;}
		if (!nuInput.isValueOk()) {setError(); return;}
		if (!cInput.isValueOk()) {setError(); return;}
		if (!tInput.isValueOk()) {setError(); return;}
		final double lambda=lambdaInput.getDouble();
		final double mu=muInput.getDouble();
		final double nu=nuInput.getDouble();
		final long c=cInput.getLong();
		final double t=tInput.getDouble();

		double a=lambda/mu;

		final int K=1000;

		double[] Cn=ErlangC.extErlangCCn(lambda,mu,nu,(int)c,K);
		double pi0=0;
		for (int i=0;i<Cn.length;i++) pi0+=Cn[i];
		pi0=1/pi0;

		double Plet;
		if (pi0==0) Plet=1; else Plet=1-Cn[K]*pi0;
		for (int n=(int)c;n<=K-1;n++) {
			final Double g=Gamma.regularizedGammaQ(n-c+1,(c*mu+nu)*t);
			Plet-=pi0*Cn[n]*g;
		}
		if (Double.isNaN(Plet) || Plet<0) Plet=0;

		double Pgt0;
		if (pi0==0) Pgt0=1; else Pgt0=1-Cn[K]*pi0;
		for (int n=(int)c;n<=K-1;n++) {
			final Double g=Gamma.regularizedGammaQ(n-c+1,0);
			Pgt0-=pi0*Cn[n]*g;
		}
		if (Double.isNaN(Pgt0) || Pgt0<0) Pgt0=0;
		Pgt0=1-Pgt0;

		double ENQ=0; for (int i=(int)(c+1);i<Cn.length;i++) ENQ+=(i-c)*Cn[i]*pi0;
		double EN=0; for (int i=1;i<Cn.length;i++) EN+=i*Cn[i]*pi0;
		double EW=ENQ/lambda;
		double EV=EN/lambda;

		final StringBuilder result=new StringBuilder();

		if (!Double.isNaN(ENQ)) {
			result.append(Language.tr("LoadCalculator.OfferedWorkLoad")+" a="+NumberTools.formatNumber(a,2)+"<br>");
			result.append(Language.tr("LoadCalculator.WorkLoad")+" (rho) &rho;="+NumberTools.formatPercent((lambda-ENQ*nu)/mu/c,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageQueueLength")+" E[N<sub>Q</sub>]="+NumberTools.formatNumber(ENQ,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageNumberOfClientsInTheSystem")+" E[N]="+NumberTools.formatNumber(EN,2)+"<br>");
			result.append(Language.tr("LoadCalculator.AverageWaitingTime")+" E[W]="+NumberTools.formatNumber(EW,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");
			result.append(Language.tr("LoadCalculator.AverageResidenceTime")+" E[V]="+NumberTools.formatNumber(EV,2)+" ("+Language.tr("LoadCalculator.Units.InSeconds")+")<br>");
			result.append(Language.tr("LoadCalculator.FlowFactor")+" E[V]/E[S]="+NumberTools.formatNumber(EV*mu,2)+"<br>");
			result.append(Language.tr("LoadCalculator.CancelRate")+" P(A)="+NumberTools.formatPercent(ENQ*nu/lambda,2)+"<br>");
			result.append("P(W&le;t)="+NumberTools.formatPercent(Plet,2)+"<br>");
			result.append("P(W&gt;0)="+NumberTools.formatPercent(Pgt0,2));
		}

		setResult(result.toString());
	}

	@Override
	protected String getHelpPageName() {
		return "erlangCExt";
	}
}
