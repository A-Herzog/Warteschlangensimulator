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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import javax.swing.JFileChooser;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.junit.jupiter.api.Test;

import mathtools.MultiTable;
import mathtools.Table;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.swing.JDataDistributionEditPanel;
import mathtools.distribution.swing.JDataDistributionPanel;
import mathtools.distribution.swing.JDataLoader;
import mathtools.distribution.swing.JDistributionEditorDialog;
import mathtools.distribution.swing.JDistributionEditorPanel;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.swing.SimSystemsSwingImages;
import mathtools.distribution.tools.DistributionTools;

/**
 * Prüft, so weit per Unittest möglich, die Funktionsweise der Dialoge,
 * die für den Umgang mit den Wahrscheinlichkeitsverteilungen vorgesehen sind.
 * @author Alexander Herzog
 */
class DistributionSwingTests {
	/**
	 * Test: Konstruktor von {@link CommonVariables} ist privat? - Klasse stellt nur statische Methoden zur Verfügung und soll nicht initialisierbar sein
	 * @throws NoSuchMethodException	Konstruktor konnte nicht gefunden werden
	 * @throws IllegalAccessException	Zugriff verweigert
	 * @throws InvocationTargetException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 * @throws InstantiationException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 */
	@Test
	void testConstructor() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		final Constructor<CommonVariables> constructor=CommonVariables.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * Test: Abfragen des {@link CommonVariables}-Singletons
	 */
	@Test
	void testGetInstance() {
		final CommonVariables obj1=CommonVariables.getCommonVariables();
		final CommonVariables obj2=CommonVariables.getCommonVariables();
		assertNotNull(obj1);
		assertNotNull(obj2);
		assertSame(obj1,obj2);
	}

	/**
	 * Test: Lesen/Schreiben der Daten in {@link CommonVariables}
	 */
	@Test
	void testData() {
		/* Wurde der Test von einem Service (außerhalb eines Benutzerkontos) ausgelöst? Wenn ja schlägt JFileChooser(home) fehlt, weil z.B. kein "Desktop"-Ordner ermittelt werden kann. */
		if (System.getProperty("user.home").toLowerCase().contains("\\system32\\config\\systemprofile")) return;

		JFileChooser chooser;

		final File home=new File(System.getProperty("user.home"));
		chooser=new JFileChooser(home);
		CommonVariables.initialDirectoryFromJFileChooser(chooser);

		chooser=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(chooser);
		assertEquals(home.toString(),chooser.getCurrentDirectory().toString());

		CommonVariables.setInitialDirectoryFromFile(new File(""));
		CommonVariables.setInitialDirectoryFromFile(new File(System.getProperty("user.home")+"\\file.txt"));

		chooser=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(chooser);
		assertEquals(home.toString(),chooser.getCurrentDirectory().toString());
	}

	/**
	 * Test: Aufbau des {@link JDataDistributionPanel}
	 * @see JDataDistributionPanel
	 */
	@Test
	void testMinimalJDataDistributionPanel() {
		final DataDistributionImpl dataDist=new DataDistributionImpl(100,new double[] {1,2,3});

		JDataDistributionPanel dataPanel;

		dataPanel=new JDataDistributionPanel(null,JDataDistributionPanel.LabelMode.LABEL_PERCENT);
		assertNull(dataPanel.getDistribution());
		assertNotNull(dataPanel.toString());
		dataPanel.setDistribution(dataDist);
		assertEquals(dataDist,dataPanel.getDistribution());
		assertNotNull(dataPanel.toString());

		dataPanel=new JDataDistributionPanel(dataDist,JDataDistributionPanel.LabelMode.LABEL_PERCENT);
		assertEquals(dataDist,dataPanel.getDistribution());
		assertNotNull(dataPanel.toString());

		dataPanel=new JDataDistributionPanel(dataDist,JDataDistributionPanel.LabelMode.LABEL_PERCENT);
		dataPanel.setDistribution(null);

		dataPanel=new JDataDistributionPanel(dataDist,JDataDistributionPanel.LabelMode.LABEL_PERCENT);
		assertFalse(dataPanel.fromString(null));

		dataPanel=new JDataDistributionPanel(dataDist,JDataDistributionPanel.LabelMode.LABEL_PERCENT);
		assertFalse(dataPanel.fromString(""));

		dataPanel=new JDataDistributionPanel(dataDist,JDataDistributionPanel.LabelMode.LABEL_PERCENT);
		assertFalse(dataPanel.fromString("äöü"));

		dataPanel=new JDataDistributionPanel(dataDist,JDataDistributionPanel.LabelMode.LABEL_PERCENT);
		assertTrue(dataPanel.fromString("1;2;3"));
	}

	/**
	 * Test: Aufbau des {@link JDataDistributionEditPanel}
	 * @see JDataDistributionEditPanel
	 */
	@Test
	void testMinimalJDataDistributionEditPanel() {
		final DataDistributionImpl dataDist=new DataDistributionImpl(100,new double[] {1,2,3});

		JDataDistributionEditPanel dataEditPanel;

		dataEditPanel=new JDataDistributionEditPanel(null,JDataDistributionEditPanel.PlotMode.PLOT_BOTH);
		assertNotNull(dataEditPanel.getDistribution()); /* Wird null übergeben, so wird eine Minimalverteilung angelegt. */
		dataEditPanel.setDistribution(dataDist);
		assertTrue(DistributionTools.compare(dataDist,dataEditPanel.getDistribution()));
		dataEditPanel.setDistribution(null);
		assertNotNull(dataEditPanel.getDistribution());
		assertFalse(DistributionTools.compare(dataDist,dataEditPanel.getDistribution())); /* Wird null übergeben, so wird eine Minimalverteilung angelegt. */

		dataEditPanel=new JDataDistributionEditPanel(dataDist,JDataDistributionEditPanel.PlotMode.PLOT_BOTH);
		assertTrue(DistributionTools.compare(dataDist,dataEditPanel.getDistribution()));

		dataEditPanel=new JDataDistributionEditPanel(dataDist,JDataDistributionEditPanel.PlotMode.PLOT_BOTH,true);
		dataEditPanel.setEditable(false);
		dataEditPanel.setEditable(true);

		new JDataDistributionEditPanel(dataDist,JDataDistributionEditPanel.PlotMode.PLOT_BOTH,true,10);

		dataEditPanel.setLabelFormat(JDataDistributionEditPanel.LabelMode.LABEL_PERCENT);
		dataEditPanel.setLabelFormat(JDataDistributionEditPanel.LabelMode.LABEL_VALUE);
	}

	/**
	 * Test: Aufbau des {@link JDistributionPanel}
	 * @see JDistributionPanel
	 */
	@Test
	void testMinimalJDistributionPanel() {
		final DataDistributionImpl dataDist=new DataDistributionImpl(100,new double[] {1,2,3});

		JDistributionPanel distPanel;
		AbstractRealDistribution dist1;
		AbstractRealDistribution dist2;

		/* Panel erstellen 1 */
		distPanel=new JDistributionPanel(null,1000,true);
		assertNull(distPanel.getDistribution());
		assertNotNull(distPanel.toString());
		distPanel.setDistribution(dataDist);
		assertEquals(dataDist,distPanel.getDistribution());
		assertNotNull(distPanel.toString());

		/* Panel erstellen 2 */
		distPanel=new JDistributionPanel(dataDist,1000,true);
		assertEquals(dataDist,distPanel.getDistribution());
		assertNotNull(distPanel.toString());

		/* Verteilung einstellen/auslesen 1 */
		distPanel.setDistribution(100);
		dist1=distPanel.getDistribution();
		assertTrue(dist1 instanceof ExponentialDistribution);
		assertEquals(((ExponentialDistribution)dist1).getMean(),100);

		/* Verteilung einstellen/auslesen 2 */
		dist1=new LogNormalDistribution(10,5);
		distPanel.setDistribution(dist1);
		dist2=distPanel.getDistribution();
		assertTrue(DistributionTools.compare(dist1,dist2));

		/* PlotType */
		distPanel.setPlotType(JDistributionPanel.DENSITY);
		assertEquals(distPanel.getPlotType(),JDistributionPanel.DENSITY);
		distPanel.setPlotType(JDistributionPanel.CUMULATIVEPROBABILITY);
		assertEquals(distPanel.getPlotType(),JDistributionPanel.CUMULATIVEPROBABILITY);
		distPanel.setPlotType(JDistributionPanel.BOTH);
		assertEquals(distPanel.getPlotType(),JDistributionPanel.BOTH);

		/* MaxValue */
		distPanel.setMaxXValue(123);
		assertEquals(distPanel.getMaxXValue(),123);

		/* Änderungsmöglichkeiten */
		distPanel.setAllowDistributionTypeChange(false);
		assertFalse(distPanel.isAllowDistributionTypeChange());
		distPanel.setAllowDistributionTypeChange(true);
		assertTrue(distPanel.isAllowDistributionTypeChange());
		distPanel.setAllowChangeDistributionData(false);
		assertFalse(distPanel.isAllowChangeDistributionData());
		distPanel.setAllowChangeDistributionData(true);
		assertTrue(distPanel.isAllowChangeDistributionData());
	}

	/**
	 * Test: Aufbau des {@link JDistributionEditorPanel}
	 * @see JDistributionEditorPanel
	 */
	@Test
	void testMinimalJDistributionEditorPanel() {
		final DataDistributionImpl dataDist=new DataDistributionImpl(100,new double[] {1,2,3});

		JDistributionEditorPanel editPanel;

		editPanel=new JDistributionEditorPanel(null,1000,null,true);
		assertNull(editPanel.getDistribution());

		editPanel=new JDistributionEditorPanel(dataDist,1000,null,true);
		assertEquals(dataDist,editPanel.getDistribution());
	}

	/**
	 * Test: Aufbau des {@link JDataLoader}
	 * @see JDataLoader
	 */
	@Test
	void testMinimalJDataLoader() {
		final MultiTable multi=new MultiTable();
		multi.add("Table",new Table());

		new JDataLoader(null,multi,0,10,true);
		new JDataLoader(null,multi,5,true);

		assertNull(JDataLoader.loadDataFromFile(null,null,0,10));
		assertNull(JDataLoader.loadDataFromFile(null,null,5));
	}

	/**
	 * Test: Aufbau des {@link JDistributionEditorDialog}
	 * @see JDistributionEditorDialog
	 */
	@Test
	void testMinimalJDistributionEditorDialog() {
		assertNotNull(new JDistributionEditorDialog(null,null,100,JDistributionPanel.BOTH,true,true,1000));
	}

	/**
	 * Test: Alle Icons vorhanden
	 * @see SimSystemsSwingImages
	 */
	@Test
	void testSimSystemsSwingImages() {
		assertTrue(SimSystemsSwingImages.checkAll(false));
	}
}