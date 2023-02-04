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
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementDecide;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;

/**
 * Panel zur Berechnung von Kenngrößen in einem Warteschlangensystem
 * basierend auf der Erlang-B-Formel
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabErlangB extends QueueingCalculatorTabBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6885193560880513420L;

	/** a (Arbeitslast) */
	private final QueueingCalculatorInputPanel aInput;
	/** n (Systemgröße) */
	private final QueueingCalculatorInputPanel nInput;

	/**
	 * Aktuell eingestellter Wert für a
	 * @see #calc()
	 */
	private double a;

	/**
	 * Aktuell eingestellter Wert für n
	 * @see #calc()
	 */
	private int n;

	/**
	 * Aktuell berechneter Wert für b
	 * @see #calc()
	 */
	private double b;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabErlangB() {
		super(Language.tr("LoadCalculator.Tab.ErlangB"),"B=(A^N/N!) / sum (i=0..N; A^i/i!)",Language.tr("LoadCalculator.Tab.ErlangB.Link.Info"),Language.tr("LoadCalculator.Tab.ErlangB.Link"));

		/* Arbeitslast (a) */
		aInput=getPanel(Language.tr("LoadCalculator.OfferedWorkLoad"),false);
		aInput.addDefault("&lambda;/&mu;=a=",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,900,null);
		add(aInput.get());

		/* Systemgröße (n) */
		nInput=getPanel(Language.tr("LoadCalculator.NumberOfLines"),false);
		nInput.addDefault("N=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_LONG,925,null);
		add(nInput.get());
	}

	@Override
	public void calc() {
		if (!aInput.isValueOk()) {setError(); return;}
		if (!nInput.isValueOk()) {setError(); return;}
		a=aInput.getDouble();
		n=(int)Math.round(nInput.getDouble());

		/* B=(A^N/N!) / sum (i=0..N; A^i/i!) */
		/* 1/B=sum(i=0..N; prod(j)i+1..N; j/A) [Termumformung] */
		double invB=0;
		for (int i=0;i<=n;i++) {
			double prod=1;
			for (int j=i+1;j<=n;j++) prod*=j/a;
			invB+=prod;
		}

		if (invB>0) {
			b=1/invB;
			setResult(Language.tr("LoadCalculator.ProbabilityOfBlocking")+": P(B)="+NumberTools.formatPercent(b));
		}
	}

	@Override
	protected String getHelpPageName() {
		return "erlangB";
	}

	@Override
	public EditModel buildModel() {
		final EditModel model=super.buildModel();

		final double meanInterArrivalTime=100;
		final ModelElementSource source=addSource(model,meanInterArrivalTime,1,1,50,100);

		final ModelElementCounter counterSuccess=addCounter(model,Language.tr("LoadCalculator.ModelBuilder.DecideCounter.Success"),Language.tr("LoadCalculator.ModelBuilder.DecideCounter"),450,100);
		final ModelElementCounter counterBlocked=addCounter(model,Language.tr("LoadCalculator.ModelBuilder.DecideCounter.Blocked"),Language.tr("LoadCalculator.ModelBuilder.DecideCounter"),250,200);

		final double meanServiceTime=a*100;
		final ModelElementProcess process=addProcess(model,meanServiceTime,1,1,n,Language.tr("Editor.Operator.Plural"),650,100);

		final ModelElementDecide decide=addDecide(model,"WIP("+process.getId()+")<"+n,250,100);

		final ModelElementDispose disposeSuccess=addExit(model,850,100);
		final ModelElementDispose disposeBlocked=addExit(model,850,200);

		addEdge(model,source,decide);
		addEdge(model,decide,counterSuccess);
		addEdge(model,decide,counterBlocked);
		addEdge(model,counterSuccess,process);
		addEdge(model,process,disposeSuccess);
		addEdge(model,counterBlocked,disposeBlocked);

		addText(model,"E[I]="+NumberTools.formatNumber(meanInterArrivalTime)+" "+Language.tr("LoadCalculator.Units.Seconds"),false,50,300);
		addText(model,"E[S]="+NumberTools.formatNumber(meanServiceTime)+" "+Language.tr("LoadCalculator.Units.Seconds"),false,50,320);
		addText(model,"a="+NumberTools.formatNumber(a),false,50,340);
		addText(model,"K=c="+n,false,50,360);
		addText(model,"&rho;="+NumberTools.formatPercent(meanServiceTime*(1-b)/meanInterArrivalTime/n),false,50,380);
		addText(model,Language.tr("LoadCalculator.ProbabilityOfBlocking")+": P(B)="+NumberTools.formatPercent(b),false,50,400);

		addExpression(model,Language.tr("LoadCalculator.ModelBuilder.SimRho"),"Resource_avg()/Resource_count()",50,440).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_PERCENT);
		addExpression(model,Language.tr("LoadCalculator.ModelBuilder.SimBlocked"),"part("+counterBlocked.getId()+")",50,500).setMode(ModelElementAnimationTextValue.ModeExpression.MODE_EXPRESSION_PERCENT);

		return model;
	}
}
