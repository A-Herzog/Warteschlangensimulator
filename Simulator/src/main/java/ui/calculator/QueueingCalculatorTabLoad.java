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

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
import mathtools.NumberTools;

/**
 * Panel zur Berechnung der Auslastung eines Bediensystems auf
 * Basis von Ankünften und Bedienungsdauern
 * @author Alexander Herzog
 * @see QueueingCalculatorTabBase
 * @see QueueingCalculatorDialog
 */
public class QueueingCalculatorTabLoad extends QueueingCalculatorTabBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5683337840249023703L;

	/** Register zur Auswahl der verschiedenen Modi (Ankunftsrate, Kunden pro Tag) */
	private final JTabbedPane tabs;

	/** lambda (Ankunftsrate) */
	private final QueueingCalculatorInputPanel lambdaInput;

	/** Ankünfte pro Tag */
	private final QueueingCalculatorInputPanel clientPerDayInput;
	/** Aktive Stunden pro Tag */
	private final QueueingCalculatorInputPanel officeHoursInput;
	/** Lastanteil den das betrachtete Callcenter übernehmen soll */
	private final QueueingCalculatorInputPanel loadPartInput;

	/** mu (Bedienrate) */
	private final QueueingCalculatorInputPanel muInput;
	/** rho (Auslastung) */
	private final QueueingCalculatorInputPanel rhoInput;

	/**
	 * Konstruktor der Klasse
	 */
	public QueueingCalculatorTabLoad() {
		super(Language.tr("LoadCalculator.Tab.WorkLoad"),"&rho;=&lambda;/(c&middot;&mu;)");

		tabs=addTabs(this);

		JPanel tab;

		/* Tab "Ankunftsrate" */
		tab=addTab(tabs,Language.tr("LoadCalculator.ArrivalRate"));

		/* Ankunftsrate (lambda) */
		lambdaInput=getPanel(Language.tr("LoadCalculator.ArrivalRate"));
		lambdaInput.addDefault("&lambda; ("+unitSecondsInv+")",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,3.5/60,infoRate);
		lambdaInput.addOption("&lambda; ("+unitMinutesInv+")",60,false,infoRate);
		lambdaInput.addOption("&lambda; ("+unitHoursInv+")",3600,false,infoRate);
		lambdaInput.addOption("1/&lambda; ("+unitSeconds+")",1,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitMinutes+")",60,true,infoInterarrivalTime);
		lambdaInput.addOption("1/&lambda; ("+unitHours+")",3600,true,infoInterarrivalTime);
		lambdaInput.setVisibleOptionIndex(1);
		tab.add(lambdaInput.get());

		/* Tab "Kunden pro Tag" */
		tab=addTab(tabs,Language.tr("LoadCalculator.Units.ClientsPerDay"));

		/* Kunden pro Tag */
		clientPerDayInput=getPanel(Language.tr("LoadCalculator.Units.ClientsPerDay"));
		clientPerDayInput.addDefault(Language.tr("LoadCalculator.Units.Number")+":",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_LONG,10000,null);
		tab.add(clientPerDayInput.get());

		/* Betriebsstunden */
		officeHoursInput=getPanel(Language.tr("LoadCalculator.WorkingTimePerDay"));
		officeHoursInput.addDefault(Language.tr("LoadCalculator.WorkingHours")+":",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,16,null);
		tab.add(officeHoursInput.get());

		/* Lastanteil */
		loadPartInput=getPanel(Language.tr("LoadCalculator.PartOfLoadForTheCallcenter"));
		loadPartInput.addDefault(Language.tr("LoadCalculator.Units.Part")+":",QueueingCalculatorInputPanel.NumberMode.NOT_NEGATIVE_DOUBLE,1,true,null);
		tab.add(loadPartInput.get());

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

		/* Auslastung (rho) */
		rhoInput=getPanel(Language.tr("LoadCalculator.SystemLoad")+" ("+unitPercent+")");
		rhoInput.addDefault("&rho=",QueueingCalculatorInputPanel.NumberMode.POSITIVE_DOUBLE,0.85,true,null);
		add(rhoInput.get());

		/* Listener für Tabs */
		tabs.addChangeListener(e->calc());
	}

	@Override
	public void calc() {
		final int index=tabs.getSelectedIndex();

		final double lambda;

		if (index==0) {
			if (!lambdaInput.isValueOk()) {setError(); return;}
			lambda=lambdaInput.getDouble();
		} else {
			if (!clientPerDayInput.isValueOk()) {setError(); return;}
			if (!officeHoursInput.isValueOk()) {setError(); return;}
			if (!loadPartInput.isValueOk()) {setError(); return;}
			final long clientPerDay=clientPerDayInput.getLong();
			final double officeHours=officeHoursInput.getDouble();
			final double loadPart=loadPartInput.getDouble();
			lambda=clientPerDay*loadPart/officeHours/3600;
		}

		if (!muInput.isValueOk()) {setError(); return;}
		if (!rhoInput.isValueOk()) {setError(); return;}
		final double mu=muInput.getDouble();
		final double rho=rhoInput.getDouble();

		setResult(String.format(Language.tr("LoadCalculator.MinimumNumberOfAgents"),NumberTools.formatNumber(lambda/mu/rho,2)));
	}

	@Override
	protected String getHelpPageName() {
		return "load";
	}
}
