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

import java.util.Objects;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.OnePointDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelResource.Mode;
import ui.modeleditor.ModelResources;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurface.TimeBase;
import ui.modeleditor.elements.DistributionSystem;
import ui.modeleditor.elements.ModelElementProcess;

/**
 * Diese Klasse stellt alle Daten über eine Bedienstation bereit,
 * die notwendig sind, um ein analytisches Vergleichsmodell aufzustellen.
 * @author Alexander Herzog
 */
public class AnalyticProcess {

	/**
	 * Bedienreihenfolge
	 * @author Alexander Herzog
	 * @see #priority
	 */
	public enum Priority {
		/** Bedienreihenfolge: FIFO */
		FIFO,
		/** Bedienreihenfolge: LIFO */
		LIFO,
		/** andere Bedienreihenfolge (nicht FIFO oder LIFO) */
		OTHER
	}

	/**
	 * Verteilung der Bedienzeiten (kann <code>null</code> sein, wenn die Bedienzeiten nicht über eine einfache Verteilung definiert sind)
	 */
	public final AbstractRealDistribution distribution;

	/**
	 * Gibt an, ob {@link #distribution} die exakte Bedienzeitenverteilung ist (<code>true</code>),
	 * oder ob in Wirklichkeit noch weitere Faktoren Einfluss auf die Bedienzeiten besitzen (<code>false</code>).
	 * Im zweiten Fall liefern {@link #mean}, {@link #cv} und {@link #mu} möglichst gute Näherungen für die
	 * tatsächlichen Bedienzeiten.
	 */
	public boolean distributionIsExact;

	/**
	 * Mittlere Bediendauer (ist <code>distribution==null</code>, so steht hier nur ein Dummy-Wert)
	 * @see #timeBase
	 */
	public final double mean;

	/**
	 * Variationskoeffizient der Bediendauern (ist <code>distribution==null</code>, so steht hier nur ein Dummy-Wert)
	 */
	public final double cv;

	/**
	 * Bedienrate (ist <code>distribution==null</code>, so steht hier nur ein Dummy-Wert)<br>
	 * ({@link #timeBase} ist hier bereits berücksichtigt)
	 */
	public final double mu;

	/**
	 * Verteilung der Wartezeittoleranzen (kann <code>null</code> sein, wenn die Wartezeittoleranzen nicht über eine einfache Verteilung definiert sind oder die Kunden unendlich geduldig sind)
	 */
	public final AbstractRealDistribution cancelDistribution;

	/**
	 * Gibt an, ob {@link #cancelDistribution} die exakte Wartezeittoleranzenverteilung ist (<code>true</code>),
	 * oder ob in Wirklichkeit noch weitere Faktoren Einfluss auf die Wartezeittoleranzen besitzen (<code>false</code>).
	 * Im zweiten Fall liefern {@link #cancelMean}, {@link #cancelCv} und {@link #nu} möglichst gute Näherungen für die
	 * tatsächlichen Wartezeittoleranzen.
	 */
	public final boolean cancelDistributionIsExact;

	/**
	 * Mittlere Wartezeittoleranz (ist <code>cancelDistribution==null</code>, so steht hier nur ein Dummy-Wert)
	 * @see #timeBase
	 */
	public final double cancelMean;

	/**
	 * Variationskoeffizient der Wartezeittoleranzen (ist <code>cancelDistribution==null</code>, so steht hier nur ein Dummy-Wert)
	 */
	public final double cancelCv;

	/**
	 * Abbruchrate (ist <code>cancelDistribution==null</code>, so steht hier nur ein Dummy-Wert)
	 * ({@link #timeBase} ist hier bereits berücksichtigt)
	 */
	public final double nu;

	/**
	 * Zeiteinheit für {@link #mean} bzw. {@link #distribution} und ggf. {@link #cancelMean} bzw. {@link #cancelDistribution}
	 */
	public final TimeBase timeBase;

	/**
	 * Benötigte Anzahl an Bedienern, um eine Bedienung durchzuführen.<br>
	 * Kann dieser Wert nicht ermittelt werden, so wird hier -1 angegeben.
	 */
	public final int cNeeded;

	/**
	 * Vorhandene Anzahl an Bedienern.<br>
	 * Kann dieser Wert nicht ermittelt werden, so wird hier -1 angegeben.
	 */
	public final int cAvailable;

	/**
	 * Minimale Bedien-Batch-Größe
	 */
	public final int batchMin;

	/**
	 * Maximale Bedien-Batch-Größe
	 */
	public final int batchMax;

	/**
	 * Mittlere Batch-Größe
	 */
	public final int batchMean;

	/**
	 * Bedienreihenfolge der wartenden Kunden
	 * @see AnalyticProcess.Priority
	 */
	public final Priority priority;

	/**
	 * Konstruktor der Klasse
	 * @param process	Editor-Modell Bedienstation aus der die Daten ausgelesen werden sollen
	 * @param clientTypeName	Name des an der Quelle erzeugten Kundentyps
	 * @param resources	Liste mit den in dem Modell vorhandenen Bedienressourcen
	 */
	public AnalyticProcess(final ModelElementProcess process, final String clientTypeName, final ModelResources resources) {
		/* Bedienzeiten */
		distribution=getDistribution(process.getWorking());
		distributionIsExact=true;
		if (process.getWorking().getNames().length>0) distributionIsExact=false;

		/* Wartezeittoleranzen */
		cancelDistribution=getDistribution(process.getCancel());
		cancelDistributionIsExact=(process.getCancel().getNames().length==0);

		/* Rüstzeiten */
		if (process.getSetupTimes().isActive()) {
			distributionIsExact=false;
		}

		/* Nachbearbeitungszeiten */
		double postProcessingMean=0;
		if (process.getPostProcessing().get()!=null) {
			distributionIsExact=false;
			final AbstractRealDistribution dist=getDistribution(process.getPostProcessing());
			postProcessingMean=DistributionTools.getMean(dist);
		}

		/* Zeitbasis */
		timeBase=process.getTimeBase();

		/* Kenngrößen */
		if (distribution==null) {
			mean=0;
			cv=1;
			mu=0;
		} else {
			mean=DistributionTools.getMean(distribution)+postProcessingMean;
			cv=DistributionTools.getCV(distribution);
			mu=1/(mean*timeBase.multiply);
		}
		if (cancelDistribution==null) {
			cancelMean=Double.MAX_VALUE;
			cancelCv=0;
			nu=0;
		} else {
			cancelMean=DistributionTools.getMean(cancelDistribution);
			cancelCv=DistributionTools.getCV(cancelDistribution);
			nu=1/(cancelMean*timeBase.multiply);
		}

		/* Batch */
		batchMin=process.getBatchMinimum();
		batchMax=process.getBatchMaximum();
		batchMean=(int)Math.round((batchMin+batchMax)/2.0);

		/* Prioritäten */
		priority=getPriority(process.getPriority(clientTypeName));

		/* Ressourcen */
		if (process.getNeededResources().size()!=1 || process.getNeededResources().get(0).size()!=1) {
			cNeeded=-1;
			cAvailable=-1;
		} else {
			final String[] keys=process.getNeededResources().get(0).keySet().toArray(new String[0]);
			cNeeded=process.getNeededResources().get(0).get(keys[0]);
			cAvailable=getAvailableOperators(keys[0],resources);
		}
	}

	/**
	 * Ermittelt eine Verteilung basierend auf einem allgemeineren Zeiten-System
	 * @param distributionSystem	Zeiten-System
	 * @return	Verteilung oder <code>null</code>, wenn die Zeiten nicht auf einer Verteilung basieren
	 */
	private AbstractRealDistribution getDistribution(final DistributionSystem distributionSystem) {
		if (distributionSystem==null) return null;
		final Object data=distributionSystem.get();

		if (data instanceof AbstractRealDistribution) {
			return (AbstractRealDistribution)data;
		}

		if (data instanceof String) {
			final Double D=ExpressionCalc.calcDirect((String)data);
			if (D!=null) return new OnePointDistributionImpl(D);
		}

		return null;
	}

	/**
	 * Ermittelt die Art der Priorisierung basierend auf einer Prioritätsformel
	 * @param priorityString	Prioritätsformel
	 * @return	Art der Priorisierung
	 * @see Priority
	 */
	private Priority getPriority(final String priorityString) {
		Priority priority=Priority.OTHER;
		if (Objects.equals(priorityString,"w")) priority=Priority.FIFO;
		if (Objects.equals(priorityString,"-w")) priority=Priority.LIFO;
		return priority;
	}

	/**
	 * Ermittelt die Anzahl an Bedienern in einer Gruppe
	 * @param resourceName	Name der Bedienergruppe
	 * @param resources	Listenobjekt über alle Bedienergruppen
	 * @return	Anzahl an Bedienern in der Bedienergruppe oder -1, wenn keine Anzahl ermittelt werden konnte
	 */
	private int getAvailableOperators(final String resourceName, final ModelResources resources) {
		final ModelResource resource=resources.get(resourceName);
		if (resource==null) return -1;

		if (resource.getMode()!=Mode.MODE_NUMBER) return -1;
		return resource.getCount();
	}

	/**
	 * Liefert die Parameter der Bedienstation, die für die folgenden Berechnungen verwendet werden, in Textform.
	 * @return	Parameter der Bedienstation in Textform
	 */
	public String getInfo() {
		final StringBuilder result=new StringBuilder();

		/* Verteilung */
		if (distribution==null) {
			result.append(Language.tr("Statistics.ErlangCompare.Distribution")+": "+Language.tr("Statistics.ErlangCompare.Distribution.Other")+"\n");
		} else {
			result.append(Language.tr("Statistics.ErlangCompare.Distribution")+": "+DistributionTools.getDistributionName(distribution)+"\n");
			if (!distributionIsExact) result.append(Language.tr("Statistics.ErlangCompare.Distribution.Other.Approx")+":\n");
			if (mean>=0) result.append("E[S]="+NumberTools.formatNumber(mean)+" "+ModelSurface.getTimeBaseString(timeBase)+"\n");
			if (cv>=0) result.append("CV[S]="+NumberTools.formatNumber(cv)+"\n");
		}

		/* Warteabbrüche */
		if (cancelDistribution!=null) {
			result.append(Language.tr("Statistics.ErlangCompare.Distribution.WaitingTimeTolerance")+": "+DistributionTools.getDistributionName(cancelDistribution)+"\n");
			if (!cancelDistributionIsExact) result.append(Language.tr("Statistics.ErlangCompare.Distribution.WaitingTimeTolerance.Approx")+":\n");
			if (cancelMean>=0) result.append("E[WT]="+NumberTools.formatNumber(cancelMean)+" "+ModelSurface.getTimeBaseString(timeBase)+"\n");
			if (cancelCv>=0) result.append("CV[WT]="+NumberTools.formatNumber(cancelCv)+"\n");
		}

		/* Batch-Größe */
		if (batchMin==batchMax) {
			result.append(Language.tr("Statistics.ErlangCompare.BatchSize")+": "+batchMin+"\n");
		} else {
			result.append(Language.tr("Statistics.ErlangCompare.BatchSize.Min")+": "+batchMin+"\n");
			result.append(Language.tr("Statistics.ErlangCompare.BatchSize.Max")+": "+batchMin+"\n");
		}

		/* Bediener */
		if (cAvailable>0) result.append(Language.tr("Statistics.ErlangCompare.Operators.Available")+": c="+cAvailable+"\n");
		if (cNeeded>1) result.append(Language.tr("Statistics.ErlangCompare.Operators.Needed")+": "+cNeeded+"\n");

		/* Bedienreihenfolge */
		switch (priority) {
		case FIFO:
			result.append(Language.tr("Statistics.ErlangCompare.Priority")+": "+Language.tr("Statistics.ErlangCompare.Priority.FIFO"));
			break;
		case LIFO:
			result.append(Language.tr("Statistics.ErlangCompare.Priority")+": "+Language.tr("Statistics.ErlangCompare.Priority.LIFO"));
			break;
		case OTHER:
			result.append(Language.tr("Statistics.ErlangCompare.Priority")+": "+Language.tr("Statistics.ErlangCompare.Priority.OTHER"));
			break;

		}

		return result.toString();
	}
}
