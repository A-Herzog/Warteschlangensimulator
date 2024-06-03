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
package mathtools.distribution.tools;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import parser.symbols.distributions.CalcSymbolEmpiricalDistributionRandom;

/**
 * Zusätzliche Daten für ein Objekt vom Typ {@link DataDistributionImpl}
 * @author Alexander Herzog
 * @see DataDistributionImpl
 * @see DistributionTools
 */
public class WrapperDataDistribution extends AbstractDistributionWrapper {
	/**
	 * Konstruktor der Klasse
	 */
	public WrapperDataDistribution() {
		super(DataDistributionImpl.class);
	}

	@Override
	public String[] getNames() {
		return DistributionTools.DistData;
	}

	@Override
	protected String getThumbnailImageName() {
		return "data.png";
	}

	@Override
	protected String getWikipediaURL() {
		return DistributionTools.DistDataWikipedia;
	}

	@Override
	protected DistributionWrapperInfo getInfoInt(final AbstractRealDistribution distribution) {
		final DataDistributionImpl dataDist=(DataDistributionImpl)distribution;
		final int c=dataDist.densityData.length;
		final String info=c+" "+((c>1)?DistributionTools.DistDataPoints:DistributionTools.DistDataPoint);
		final double[] modeValues=dataDist.getMode();
		final Double mode=(modeValues.length==1)?modeValues[0]:null;
		return new DistributionWrapperInfo(distribution,dataDist.getSkewness(),mode,info,null);
	}

	@Override
	public AbstractRealDistribution getDistribution(final double mean, final double sd) {
		return null;
	}

	@Override
	public AbstractRealDistribution getDefaultDistribution() {
		return new DataDistributionImpl(100,new double[]{1,2,3});
	}

	@Override
	public double getMean(final AbstractRealDistribution distribution) {
		return ((DataDistributionImpl)distribution).getMean();
	}

	@Override
	protected AbstractRealDistribution setMeanInt(final AbstractRealDistribution distribution, final double mean) {
		return null;
	}

	@Override
	public double getStandardDeviation(final AbstractRealDistribution distribution) {
		return ((DataDistributionImpl)distribution).getStandardDeviation();
	}

	@Override
	protected AbstractRealDistribution setStandardDeviationInt(final AbstractRealDistribution distribution, final double sd) {
		return null;
	}

	@Override
	protected double getParameterInt(final AbstractRealDistribution distribution, final int nr) {
		return 0;
	}

	@Override
	protected AbstractRealDistribution setParameterInt(final AbstractRealDistribution distribution, final int nr, final double value) {
		return null;
	}

	@Override
	protected String getToStringData(final AbstractRealDistribution distribution) {
		return ((DataDistributionImpl)distribution).storeToString();
	}

	@Override
	public AbstractRealDistribution fromString(final String data, final double maxXValue) {
		return DataDistributionImpl.createFromString(data,maxXValue);
	}

	@Override
	protected AbstractRealDistribution cloneInt(final AbstractRealDistribution distribution) {
		return ((DataDistributionImpl)distribution).clone();
	}

	@Override
	protected boolean compareInt(final AbstractRealDistribution distribution1, final AbstractRealDistribution distribution2) {
		final DataDistributionImpl data1=(DataDistributionImpl)distribution1;
		final DataDistributionImpl data2=(DataDistributionImpl)distribution2;
		if (data1.upperBound!=data2.upperBound) return false;
		if (data1.densityData.length!=data2.densityData.length) return false;
		for (int i=0;i<data1.densityData.length;i++) if (Math.abs(data1.densityData[i]-data2.densityData[i])>DistributionTools.MAX_ERROR) return false;
		return true;
	}

	@Override
	protected String getCalcExpressionInt(final AbstractRealDistribution distribution) {
		final String name=new CalcSymbolEmpiricalDistributionRandom().getNames()[0];
		final DataDistributionImpl dataDist=(DataDistributionImpl)distribution;

		final StringBuilder result=new StringBuilder();
		result.append(name);
		result.append("(");
		for (var d: dataDist.densityData) {
			result.append(NumberTools.formatNumberMax(d));
			result.append(";");
		}
		result.append(NumberTools.formatNumberMax(dataDist.upperBound));
		result.append(")");
		return result.toString();
	}
}
