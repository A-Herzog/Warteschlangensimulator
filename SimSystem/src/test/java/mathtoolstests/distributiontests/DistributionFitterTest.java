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
package mathtoolstests.distributiontests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.jupiter.api.Test;

import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.LogNormalDistributionImpl;
import mathtools.distribution.PertDistributionImpl;
import mathtools.distribution.tools.DistributionFitter;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.WrapperLogNormalDistribution;

/**
 * Prüft die Funktionsweise von {@link DistributionFitter}
 * @author Alexander Herzog
 * @see DistributionFitter
 */
class DistributionFitterTest {
	/**
	 * Test: Verarbeitung von ungültigen Eingabedaten
	 * @see DistributionFitter#processDensity(int[][])
	 * @see DistributionFitter#processDensity(String[][])
	 * @see DistributionFitter#processSamples(int[])
	 * @see DistributionFitter#processSamples(String[])
	 */
	@Test
	void testFitterError() {
		DistributionFitter fitter;

		int[] intArray;
		String[] strArray;
		int[][] int2Array;
		String[][] str2Array;

		fitter=new DistributionFitter();
		intArray=null;
		assertFalse(fitter.processSamples(intArray));

		fitter=new DistributionFitter();
		strArray=null;
		assertFalse(fitter.processSamples(strArray));

		fitter=new DistributionFitter();
		int2Array=null;
		assertFalse(fitter.processDensity(int2Array));

		fitter=new DistributionFitter();
		str2Array=null;
		assertFalse(fitter.processDensity(str2Array));

		fitter=new DistributionFitter();
		intArray=new int[0];
		assertFalse(fitter.processSamples(intArray));

		fitter=new DistributionFitter();
		strArray=new String[0];
		assertFalse(fitter.processSamples(strArray));
	}

	/**
	 * Test: Verteilungsanpassung (auf Basis von Lognormalverteilungs-Daten)
	 */
	@Test
	void testFitter() {
		final LogNormalDistributionImpl sourceDistribution=(LogNormalDistributionImpl)DistributionTools.getDistributionFromInfo(new WrapperLogNormalDistribution().getName(),10000,2500);
		final List<String> samples=new ArrayList<>();
		for (int p=0;p<1000;p++) {
			long sample=Math.round(sourceDistribution.inverseCumulativeProbability(p/1000.0))/100;
			samples.add(""+sample);
		}

		final DistributionFitter fitter=new DistributionFitter();
		assertTrue(fitter.processSamples(samples.toArray(new String[0])));

		assertNotNull(fitter.getSamplesDistribution());
		assertNotNull(fitter.getResult(true));
		assertNotNull(fitter.getResult(false));
		assertNotNull(fitter.getResultList());
		assertNotNull(fitter.getResultListDist());
		assertNotNull(fitter.getResultListError());
		assertNotNull(fitter.getFitDistribution());
	}

	private double[] normalDistData=new double[] {
			9.162774487,
			6.005960156,
			8.273369085,
			9.50013613,
			6.581079143,
			6.272348155,
			8.727032061,
			5.892344235,
			7.464525722,
			11.28165104,
			11.54879449,
			7.521901562,
			12.50176485,
			7.619729271,
			8.985457097,
			9.699609006,
			10.22712437,
			7.96487899,
			6.29428348,
			6.894217907,
			6.831315608,
			12.27874761,
			7.560027522,
			14.62823952,
			10.83984998,
			12.00302312,
			6.659547757,
			8.061658917,
			6.062228381,
			11.5031086,
			8.557699023,
			16.24046077,
			14.60072716,
			13.77749859,
			12.35238377,
			7.892137168,
			6.024398615,
			9.592594133,
			16.09644042,
			10.95509004,
			11.37052802,
			12.79032833,
			10.12658832,
			8.549873193,
			5.694856183,
			11.15201277,
			5.029742833,
			9.028813752,
			7.10986458,
			5.47599722,
			10.20613408,
			9.493887887,
			13.32402392,
			10.06762096,
			11.90504218,
			8.967485159,
			13.41637783,
			12.11824911,
			6.176454577,
			10.79240216,
			7.33036729,
			14.35477236,
			10.64568273,
			9.925787706,
			11.14761537,
			10.15736254,
			10.04988163,
			8.936985596,
			10.7821901,
			8.435845233,
			11.92050614,
			12.55203663,
			13.41261878,
			10.21077766,
			10.85272572,
			6.353425731,
			11.92304176,
			8.993182766,
			7.116003755,
			12.0682006,
			14.67928267,
			8.20899102,
			10.72177266,
			8.900988936,
			11.1684965,
			11.09629607,
			11.76206277,
			7.872019232,
			9.324673193,
			8.084648527,
			9.632524356,
			10.26339644,
			7.716913732,
			4.480837288,
			7.61079485,
			5.015474478,
			11.05577082,
			8.88909391,
			8.192618155,
			8.972372371
	};

	/**
	 * Test: Verteilungsanpassung (auf Basis von Normalverteilungs-Daten)
	 */
	@Test
	void testFitterNormalDistData() {
		final Object[] obj=DistributionFitter.dataDistributionFromValues(new double[][]{normalDistData});
		assertNotNull(obj);
		final DataDistributionImpl distribution=(DataDistributionImpl)obj[0];

		final DistributionFitter fitter=new DistributionFitter();
		fitter.process(distribution);

		assertNotNull(fitter.getSamplesDistribution());
		assertNotNull(fitter.getResult(true));
		assertNotNull(fitter.getResult(false));
		assertNotNull(fitter.getResultList());
		assertNotNull(fitter.getResultListDist());
		assertNotNull(fitter.getResultListError());
		final AbstractRealDistribution dist=fitter.getFitDistribution();
		assertNotNull(dist);
		assertTrue((dist instanceof NormalDistribution) || (dist instanceof PertDistributionImpl));
	}
}
