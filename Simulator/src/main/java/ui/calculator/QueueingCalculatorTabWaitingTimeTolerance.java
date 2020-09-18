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
 * Panel zur Berechnung der Wartezeittoleranzen der Kunden
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabWaitingTimeTolerance extends QueueingCalculatorTabBase {
	private static final long serialVersionUID = 6885193560880513420L;

	/** E[W] (mittlere Wartezeit) */
	private final QueueingCalculatorInputPanel ewInput;
	/** P(A) (Anteil der Abbrecher) */
	private final QueueingCalculatorInputPanel paInput;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabWaitingTimeTolerance() {
		super(Language.tr("LoadCalculator.Tab.WaitingTimeTolerance"),"E[WT]=E[W]/P(A), "+Language.tr("LoadCalculator.ExpAssumption"));

		/* Mittlere Wartezeit (E[W]) */
		ewInput=getPanel(Language.tr("LoadCalculator.AverageWaitingTime"));
		ewInput.addDefault("E[W] ("+unitSeconds+")",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,10,null);
		ewInput.addOption("E[W] ("+unitMinutes+")",1.0/60,false,null);
		ewInput.addOption("E[W] ("+unitHours+")",1.0/3600,false,null);
		add(ewInput.get());

		/* Abbruchwahrscheinlichkeit (P(A)) */
		paInput=getPanel(Language.tr("LoadCalculator.CancelRate")+" ("+Language.tr("LoadCalculator.Units.InPercent")+")");
		paInput.addDefault("P(A)=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,0.07,true,null);
		add(paInput.get());
	}

	@Override
	public void calc() {
		if (!ewInput.isValueOk()) {setError(); return;}
		if (!paInput.isValueOk()) {setError(); return;}
		final double ew=ewInput.getDouble();
		final double pa=paInput.getDouble();

		/* E[WT]=E[W]/P(A) */
		double WT=0;
		if (pa>0) WT=ew/pa;

		setResult("E[WT]="+NumberTools.formatNumber(WT)+" "+Language.tr("LoadCalculator.Units.Seconds"));
	}

	@Override
	protected String getHelpPageName() {
		return "waitingTimeTolerance";
	}
}
