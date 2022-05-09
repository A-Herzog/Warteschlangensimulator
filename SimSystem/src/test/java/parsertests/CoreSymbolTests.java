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
package parsertests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import parser.CalcSystem;
import parser.MathCalcError;

/**
 * Prüft die Funktionsweise der Basissymbole von {@link CalcSystem} (Addition, Subtraktion, ...)
 * @author Alexander Herzog
 * @see CalcSystem
 */
class CoreSymbolTests {
	/**
	 * Konstruktor der Klasse
	 */
	public CoreSymbolTests() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Test: Basis-Rechenoperationen
	 */
	@Test
	void symbolTest() {
		CalcSystem calc;
		double d;

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{3});
			assertEquals(4.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{10_000});
			assertEquals(10_001.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{-3});
			assertEquals(-2.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{-10_000});
			assertEquals(-9_999.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{36});
			assertEquals(6.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{30.25});
			assertEquals(5.5,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{3.2});
			assertEquals(3.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{-3.2});
			assertEquals(-3.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}

	/**
	 * Test: Umgang mit den Konstanten pi und e
	 */
	@Test
	void constTest() {
		CalcSystem calc;
		double d;

		calc=new CalcSystem("a+Pi",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{0});
			assertEquals(Math.PI,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("a+e",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{0});
			assertEquals(Math.E,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}

	/**
	 * Test: Zweistellige Operationen
	 */
	@Test
	void middleOperatorTest() {
		CalcSystem calc;
		double d;

		calc=new CalcSystem("a+b",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2,3});
			assertEquals(5.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqr(a)+sqr(b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2,3});
			assertEquals(13.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqr(a)+7",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2});
			assertEquals(11.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("7+sqr(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2});
			assertEquals(11.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("a+b",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		assertEquals(5.0,calc.calcOrDefault(new double[]{2,3},-1));

		calc=new CalcSystem("sqr(a)+sqr(b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		assertEquals(13.0,calc.calcOrDefault(new double[]{2,3},-1));

		calc=new CalcSystem("sqr(a)+7",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(11.0,calc.calcOrDefault(new double[]{2},-1));

		calc=new CalcSystem("7+sqr(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(11.0,calc.calcOrDefault(new double[]{2},-1));
	}

	/**
	 * Test: Unterausdrücke (=Klammerung)
	 */
	@Test
	void subTest() {
		CalcSystem calc;
		double d;

		calc=new CalcSystem("min(1+2;3+4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("min(a+1;b+2)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{7,3});
			assertEquals(5.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("min(a+1)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{5});
			assertEquals(6.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}
}
