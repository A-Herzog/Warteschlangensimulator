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
 * basierend auf der Erlang-B-Formel
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabErlangB extends QueueingCalculatorTabBase {
	private static final long serialVersionUID = 6885193560880513420L;

	private final QueueingCalculatorInputPanel aInput;
	private final QueueingCalculatorInputPanel nInput;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabErlangB() {
		super(Language.tr("LoadCalculator.Tab.ErlangB"),"B=(A^N/N!) / sum (i=0..N; A^i/i!)",Language.tr("LoadCalculator.Tab.ErlangB.Link.Info"),Language.tr("LoadCalculator.Tab.ErlangB.Link"));

		/* Arbeitslast (a) */
		aInput=getPanel(Language.tr("LoadCalculator.OfferedWorkLoad"));
		aInput.addDefault("&lambda;/&mu;=a=",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,900,null);
		add(aInput.get());

		/* Systemgröße (n) */
		nInput=getPanel(Language.tr("LoadCalculator.NumberOfLines"));
		nInput.addDefault("N=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,925,null);
		add(nInput.get());
	}

	@Override
	public void calc() {
		if (!aInput.isValueOk()) {setError(); return;}
		if (!nInput.isValueOk()) {setError(); return;}
		final double a=aInput.getDouble();
		final double n=nInput.getDouble();

		/* B=(A^N/N!) / sum (i=0..N; A^i/i!) */
		/* 1/B=sum(i=0..N; prod(j)i+1..N; j/A) [Termumformung] */
		double invB=0;
		for (int i=0;i<=n;i++) {
			double prod=1;
			for (int j=i+1;j<=n;j++) prod*=j/a;
			invB+=prod;
		}

		setResult(Language.tr("LoadCalculator.ProbabilityOfBlocking")+": P(B)="+NumberTools.formatPercent(1/invB));
	}

	@Override
	protected String getHelpPageName() {
		return "erlangB";
	}
}
