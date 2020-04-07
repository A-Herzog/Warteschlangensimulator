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
package ui.statistics.analyticcompare;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.OnePointDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurface.TimeBase;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceRecord;

/**
 * Diese Klasse stellt alle Daten über eine Kundenquelle bereit,
 * die notwendig sind, um ein analytisches Vergleichsmodell aufzustellen.
 * @author Alexander Herzog
 */
public class AnalyticSource {
	/**
	 * Name der Kunden an dieser Quelle
	 */
	public final String name;

	/**
	 * Verteilung der Zwischenankunftszeiten (kann <code>null</code> sein, wenn die Zwischenankunftszeiten nicht über eine einfache Verteilung definiert sind)
	 */
	public final AbstractRealDistribution distribution;

	/**
	 * Mittlere Zwischenankunftszeit (ist <code>distribution==null</code>, so steht hier nur ein Dummy-Wert)
	 * @see #timeBase
	 */
	public final double mean;

	/**
	 * Variationskoeffizient der Zwischenankunftszeiten (ist <code>distribution==null</code>, so steht hier nur ein Dummy-Wert)
	 */
	public final double cv;

	/**
	 * Ankunftsrate (ist <code>distribution==null</code>, so steht hier nur ein Dummy-Wert)<br>
	 * ({@link #timeBase} ist hier bereits berücksichtigt)
	 */
	public final double lambda;

	/**
	 * Zeiteinheit für {@link #mean} bzw. {@link #distribution}
	 */
	public final TimeBase timeBase;

	/**
	 * Batchgröße (entweder &ge;1 oder, wenn keine feste Größe ermittelt werden konnte, -1)
	 */
	public final int batch;

	/**
	 * Konstruktor der Klasse
	 * @param source	Editor-Modell Quelle aus der die Daten ausgelesen werden sollen
	 */
	public AnalyticSource(final ModelElementSource source) {
		final ModelElementSourceRecord record=source.getRecord();

		/* Name des Kundentyps */
		name=source.getName();

		/* Zwischenankunkftszeiten */
		this.distribution=getDistribution(record);

		/* Zeitbasis */
		timeBase=record.getTimeBase();

		/* Kenngrößen */
		if (distribution==null) {
			mean=0;
			cv=1;
			lambda=0;
		} else {
			mean=DistributionTools.getMean(distribution);
			cv=DistributionTools.getCV(distribution);
			lambda=1/(mean*timeBase.multiply);
		}


		/* Batch-Größe */
		final String batchSize=record.getBatchSize();
		if (batchSize==null) {
			batch=-1; /* Verschiedene Batch-Größen */
		} else {
			final Double D=ExpressionCalc.calcDirect(batchSize);
			if (D==null) batch=-1; else {
				final long l=Math.round(D.doubleValue());
				if (l>=1) batch=(int)l; else batch=-1;
			}
		}

	}

	private AbstractRealDistribution getDistribution(final ModelElementSourceRecord record) {
		switch (record.getNextMode()) {
		case NEXT_DISTRIBUTION:
			return record.getInterarrivalTimeDistribution();
		case NEXT_EXPRESSION:
			final Double iD=ExpressionCalc.calcDirect(record.getInterarrivalTimeExpression());
			if (iD!=null) return new OnePointDistributionImpl(iD);
			break;
		default:
			/* Nichts */
		}
		return null;
	}

	/**
	 * Liefert die Parameter der Quelle, die für die folgenden Berechnungen verwendet werden, in Textform.
	 * @return	Parameter der Quelle in Textform
	 */
	public String getInfo() {
		final StringBuilder result=new StringBuilder();

		/* Verteilung */
		if (distribution==null) {
			result.append(Language.tr("Statistics.ErlangCompare.Distribution")+": "+Language.tr("Statistics.ErlangCompare.Distribution.Other")+"\n");
		} else {
			result.append(Language.tr("Statistics.ErlangCompare.Distribution")+": "+DistributionTools.getDistributionName(distribution)+"\n");
			if (mean>=0) result.append("E[I]="+NumberTools.formatNumber(mean)+" "+ModelSurface.getTimeBaseString(timeBase)+"\n");
			if (cv>=0) result.append("CV[I]="+NumberTools.formatNumber(cv)+"\n");
		}

		/* Batch-Größe */
		if (batch<0) {
			result.append(Language.tr("Statistics.ErlangCompare.BatchSize")+": "+Language.tr("Statistics.ErlangCompare.BatchSize.Other"));
		} else {
			result.append(Language.tr("Statistics.ErlangCompare.BatchSize")+": "+batch);
		}

		return result.toString();
	}
}
