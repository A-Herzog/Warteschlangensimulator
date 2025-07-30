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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import mathtools.NumberTools;
import parser.CalcSystem;
import parser.MathCalcError;

/**
 * Prüft die Funktionsweise der Rechensymbole von {@link CalcSystem}
 * @author Alexander Herzog
 * @see CalcSystem
 */
class SymbolsTests {
	/**
	 * Konstruktor der Klasse
	 */
	public SymbolsTests() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Test: Basis-Rechensymbole (Grundrechenarten)
	 */
	@Test
	void basicTest() {
		CalcSystem calc;
		double d;

		/* Plus */

		calc=new CalcSystem("1+2");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1+a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2});
			assertEquals(3.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1+a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3.0,calc.calcOrDefault(new double[]{2},-7));

		/* Minus */

		calc=new CalcSystem("1-2");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-1.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1-a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2});
			assertEquals(-1.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1-a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1.0,calc.calcOrDefault(new double[]{2},-7));

		/* Multiplizieren */

		calc=new CalcSystem("1*2");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1*a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2});
			assertEquals(2.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1*a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2.0,calc.calcOrDefault(new double[]{2},-7));

		/* Dividieren */

		calc=new CalcSystem("1/2");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.5,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1/a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2});
			assertEquals(0.5,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1/a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0.5,calc.calcOrDefault(new double[]{2},-7));

		/* Potenzieren */

		calc=new CalcSystem("2^3");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(8.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("2^3",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc(new double[]{2});
			assertEquals(8.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("2^3",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(8.0,calc.calcOrDefault(new double[]{2},-7));

		calc=new CalcSystem("2^a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(8.0,calc.calcOrDefault(new double[]{3},-7));
	}

	/**
	 * Test: Punkt-Vor-Strich-Rechnung
	 */
	@Test
	void priorityTest() {
		CalcSystem calc;
		double d;

		calc=new CalcSystem("1+2-3");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1+2*3");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(7.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1+2*3-5");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("2*3^4");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(162.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1-2*3");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-5.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1+2/4");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1.5,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1/2/2");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.25,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}

	/**
	 * Test: Nachgestellte Operatoren
	 */
	@Test
	void testPostOperators() {
		CalcSystem calc;
		double d;

		/* DEG -> RAD */

		calc=new CalcSystem("180°");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(Math.PI,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("1+180°");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1+Math.PI,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Factorial */

		calc=new CalcSystem("10!");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3628800.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Power2 */

		calc=new CalcSystem("3*2²");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(12.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Power3 */

		calc=new CalcSystem("3*2³");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(24.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}

	/**
	 * Test: Funktionen, Anfangsbuchstabe A
	 */
	@Test
	void testPreOperatorsA() {
		CalcSystem calc;
		double d;

		/* Abs */

		calc=new CalcSystem("Abs(1.2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1.2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("Abs(-3.4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3.4,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("Abs(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1.2,calc.calcOrDefault(new double[]{1.2},-1));

		calc=new CalcSystem("Abs(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3.4,calc.calcOrDefault(new double[]{-3.4},-1));

		/* AllenCunneen */

		/* siehe https://www.mathematik.tu-clausthal.de/interaktiv/warteschlangentheorie/warteschlangenrechner/ */

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1;1;-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(8.1,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1;1;-2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(9,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1;1;-3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(9,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1;1;-4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(10,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("AllenCunneen(1,8;1;1;1;2;-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(7.674,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("AllenCunneen(1,8;1,25;1;1;2;-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1.55,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("AllenCunneen(0,9;1;1,5;1;1;-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(13.163,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1,5;1;-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(13.163,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* ArcCos */

		calc=new CalcSystem("arccos(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(Math.PI,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccos(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(Math.PI/2,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccos(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(Math.PI,calc.calcOrDefault(new double[]{-1},-7),0.0001);

		calc=new CalcSystem("arccos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(Math.PI/2,calc.calcOrDefault(new double[]{0},-7),0.0001);

		calc=new CalcSystem("arccos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{1},-7),0.0001);

		/* ArcCosH */

		calc=new CalcSystem("arccosh(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccosh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{1},-7),0.0001);

		/* ArcCot */

		calc=new CalcSystem("arccot(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d<0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccot(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccot(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)<0);


		calc=new CalcSystem("arccot(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},0)>0);

		/* ArcCotH */

		calc=new CalcSystem("arccoth(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d<0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccoth(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arccoth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)<0);

		calc=new CalcSystem("arccoth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},0)>0);

		/* ArcSin */

		calc=new CalcSystem("arcsin(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-Math.PI/2,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arcsin(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arcsin(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(Math.PI/2,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arcsin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-Math.PI/2,calc.calcOrDefault(new double[]{-1},0.0001));

		calc=new CalcSystem("arcsin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},0.0001));

		calc=new CalcSystem("arcsin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(Math.PI/2,calc.calcOrDefault(new double[]{1},0.0001));

		/* ArcSinH */

		calc=new CalcSystem("arcsinh(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arcsinh(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d<0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arcsinh(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arcsinh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},-7),0.0001);

		calc=new CalcSystem("arcsinh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)<0);

		calc=new CalcSystem("arcsinh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},0)>0);

		/* ArcTan */

		calc=new CalcSystem("arctan(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arctan(10000000)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(Math.PI/2,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arctan(-10000000)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-Math.PI/2,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arctan(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},5),0.0001);

		calc=new CalcSystem("arctan(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(Math.PI/2,calc.calcOrDefault(new double[]{10000000},-5),0.01);

		calc=new CalcSystem("arctan(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-Math.PI/2,calc.calcOrDefault(new double[]{-10000000},5),0.01);

		/* ArcTanH */

		calc=new CalcSystem("arctanh(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arctanh(-0,5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d<0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arctanh(0,5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("arctanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},7),0.0001);

		calc=new CalcSystem("arctanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-0.5},3)<0);

		calc=new CalcSystem("arctanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{0.5},-3)>0);
	}

	/**
	 * Test: Funktionen, Anfangsbuchstabe C
	 */
	@Test
	void testPreOperatorsC() {
		CalcSystem calc;
		double d;

		/* Ceil */

		calc=new CalcSystem("ceil(0,4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ceil(0,6)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ceil(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ceil(-1,8)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ceil(-1,2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ceil(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ceil(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0.4},123));

		calc=new CalcSystem("ceil(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0.6},123));

		calc=new CalcSystem("ceil(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},123));

		calc=new CalcSystem("ceil(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-1.8},123));

		calc=new CalcSystem("ceil(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-1.2},123));

		calc=new CalcSystem("ceil(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-1},123));

		/* Cos */

		calc=new CalcSystem("cos(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cos(pi/2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cos(pi)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cos(3*pi/2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cos(2pi)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0},123));

		calc=new CalcSystem("cos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{Math.PI/2},123),0.000001);

		calc=new CalcSystem("cos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{Math.PI},123));

		calc=new CalcSystem("cos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{3*Math.PI/2},123),0.000001);

		calc=new CalcSystem("cos(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{2*Math.PI},123));

		/* CosH */

		calc=new CalcSystem("cosh(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cosh(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>1);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cosh(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>1);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cosh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0},123));

		calc=new CalcSystem("cosh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)>1);

		calc=new CalcSystem("cosh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},0)>1);

		/* Cot */

		calc=new CalcSystem("cot(pi/2-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>1);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cot(pi/2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cot(pi/2+1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d<1);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cot(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{Math.PI/2-1},0)>1);

		calc=new CalcSystem("cot(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{Math.PI/2},123),0.000001);

		calc=new CalcSystem("cot(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{Math.PI/2+1},123)<1);

		/* CotH */

		calc=new CalcSystem("coth(0,1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d>1);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("coth(10000000)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1.0,d,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("coth(-0,1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertTrue(d<-1);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("coth(-10000000)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-1.0,d,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("coth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{0.1},-5)>1);

		calc=new CalcSystem("coth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1.0,calc.calcOrDefault(new double[]{10000000},-1),0.000001);

		calc=new CalcSystem("coth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-0.1},5)<-1);

		calc=new CalcSystem("coth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1.0,calc.calcOrDefault(new double[]{-10000000},1),0.000001);

		/* CV */

		calc=new CalcSystem("cv(1;2;3;4;5)"); /* E=3, Var=2,5, CV=Sqrt(Var)/E=0,527 */
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.527,d,0.001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("cv(a;2;3;4;5)",new String[]{"a"}); /* E=3, Var=2,5, CV=Sqrt(Var)/E=0,527 */
		assertTrue(calc.parse()<0);
		assertEquals(0.527,calc.calcOrDefault(new double[]{1},-7),0.001);
	}

	/**
	 * Test: Funktionen, Anfangsbuchstaben E, F, G und I
	 */
	@Test
	void testPreOperatorsEFGI() {
		CalcSystem calc;
		double d;

		/* ErlangC */

		/* siehe https://www.mathematik.tu-clausthal.de/interaktiv/warteschlangentheorie/erlang-c/ */

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;20)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.8211,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;15;200;20)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.9373,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.58,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(10.73,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-3");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(9.91,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(183.97,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1-0.033,d,0.01);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Exp */

		calc=new CalcSystem("exp(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1/Math.E,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("exp(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("exp(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(Math.E,d,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("exp(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1/Math.E,calc.calcOrDefault(new double[]{-1},-1),0.0001);

		calc=new CalcSystem("exp(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0},-1),0.0001);

		calc=new CalcSystem("exp(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(Math.E,calc.calcOrDefault(new double[]{1},-1),0.0001);

		/* Factorial */

		calc=new CalcSystem("factorial(0)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("factorial(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("factorial(2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("factorial(3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(6,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("factorial(4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(24,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("factorial(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0},-1));

		calc=new CalcSystem("factorial(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("factorial(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{2},-1));

		calc=new CalcSystem("factorial(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(6,calc.calcOrDefault(new double[]{3},-1));

		calc=new CalcSystem("factorial(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(24,calc.calcOrDefault(new double[]{4},-1));

		/* Binom */

		calc=new CalcSystem("binom(7;0)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(1,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;1)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(7,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;2)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(21,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;3)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(35,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;4)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(35,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;5)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(21,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;6)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(7,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;7)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(1,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;1,4)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(7,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;1,6)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(21,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;-1)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(0.0,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(7;8)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(0.0,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(0;0)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(1.0,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(0;-1)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(0.0,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("binom(0;1)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(0.0,calc.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Floor */

		calc=new CalcSystem("floor(0,4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("floor(0,6)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("floor(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("floor(-1,8)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("floor(-1,2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("floor(-1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("floor(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0.4},1234));

		calc=new CalcSystem("floor(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0.6},1234));

		calc=new CalcSystem("floor(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},1234));

		calc=new CalcSystem("floor(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-2,calc.calcOrDefault(new double[]{-1.8},1234));

		calc=new CalcSystem("floor(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-2,calc.calcOrDefault(new double[]{-1.2},1234));

		calc=new CalcSystem("floor(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-1},1234));

		/* Frac */

		calc=new CalcSystem("frac(5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("frac(5,2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.2,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("frac(-5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("frac(-5,2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-0.2,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("frac(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{5},123),0.0000001);

		calc=new CalcSystem("frac(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0.2,calc.calcOrDefault(new double[]{5.2},123),0.0000001);

		calc=new CalcSystem("frac(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{-5},123),0.0000001);

		calc=new CalcSystem("frac(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-0.2,calc.calcOrDefault(new double[]{-5.2},123),0.0000001);

		/* Gamma */

		calc=new CalcSystem("gamma(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("gamma(2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("gamma(3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("gamma(4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(6,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("gamma(5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(24,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc1=new CalcSystem("gamma(0)");
		assertTrue(calc1.parse()<0);
		assertThrows(MathCalcError.class,()->calc1.calc());

		final CalcSystem calc2=new CalcSystem("gamma(-1)");
		assertTrue(calc2.parse()<0);
		assertThrows(MathCalcError.class,()->calc2.calc());

		calc=new CalcSystem("gamma(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("gamma(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{2},-1));

		calc=new CalcSystem("gamma(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{3},-1));

		calc=new CalcSystem("gamma(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(6,calc.calcOrDefault(new double[]{4},-1));

		calc=new CalcSystem("gamma(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(24,calc.calcOrDefault(new double[]{5},-1));

		calc=new CalcSystem("gamma(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{0},-1));

		calc=new CalcSystem("gamma(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-1},-1));

		/* If */

		calc=new CalcSystem("if(a;17;b;23;29)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			assertEquals(29,calc.calc(new double[]{-1,-1}));
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		try {
			assertEquals(23,calc.calc(new double[]{-1,1}));
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		try {
			assertEquals(17,calc.calc(new double[]{3,1}));
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		try {
			assertEquals(17,calc.calc(new double[]{3,2}));
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Int */

		calc=new CalcSystem("int(5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(5,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("int(5,4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(5,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("int(5,6)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(5,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("int(-5,4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-5,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("int(-5,6)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(-5,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("int(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(5,calc.calcOrDefault(new double[]{5},-1),0.0000001);

		calc=new CalcSystem("int(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(5,calc.calcOrDefault(new double[]{5.4},-1),0.0000001);

		calc=new CalcSystem("int(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(5,calc.calcOrDefault(new double[]{5.6},-1),0.0000001);

		calc=new CalcSystem("int(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-5,calc.calcOrDefault(new double[]{-5.4},-1),0.0000001);

		calc=new CalcSystem("int(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-5,calc.calcOrDefault(new double[]{-5.6},-1),0.0000001);
	}

	/**
	 * Test: Funktionen, Anfangsbuchstaben K, L und M
	 */
	@Test
	void testPreOperatorsKLM() {
		CalcSystem calc;
		double d;

		/* Kurt */

		calc=new CalcSystem("kurt(1;2;2;3;3;3;3;4;4;4;4;4;5;5)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(-0.13189417287153704,calc.calc(),0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		assertEquals(-0.13189417287153704,calc.calcOrDefault(new double[0],-123),0.0001);

		/* Ld */

		calc=new CalcSystem("ld(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ld(2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ld(4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ld(8)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ld(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("ld(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{2},-1));

		calc=new CalcSystem("ld(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{4},-1));

		calc=new CalcSystem("ld(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3,calc.calcOrDefault(new double[]{8},-1));

		/* Lg */

		calc=new CalcSystem("lg(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("lg(10)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("lg(100)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("lg(1000)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("lg(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("lg(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{10},-1));

		calc=new CalcSystem("lg(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{100},-1));

		calc=new CalcSystem("lg(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3,calc.calcOrDefault(new double[]{1000},-1));

		/* Ln */

		calc=new CalcSystem("ln(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ln(e)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ln(e^2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ln(e^3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ln(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("ln(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{Math.E},-1));

		calc=new CalcSystem("ln(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{FastMath.pow(Math.E,2)},-1));

		calc=new CalcSystem("ln(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3,calc.calcOrDefault(new double[]{FastMath.pow(Math.E,3)},-1));

		/* Log */

		calc=new CalcSystem("log(1;2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("log(2;2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("log(4;2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("log(8;2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("log(a;2)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("log(a;2)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{2},-1));

		calc=new CalcSystem("log(a;2)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{4},-1));

		calc=new CalcSystem("log(a;2)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3,calc.calcOrDefault(new double[]{8},-1));

		/* Max */

		calc=new CalcSystem("max(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("max(1;2;3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(3,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("max(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("max(a;2;3)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3,calc.calcOrDefault(new double[]{1},-1));

		/* Mean */

		calc=new CalcSystem("mean(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("mean(1;2;3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("mean(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("mean(a;2;3)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{1},-1));

		/* Median */

		calc=new CalcSystem("median(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("median(1;3;2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("median(1;7;2)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("median(1;7;2;8)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(4.5,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("median(1;7;2)");
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[0],-1));

		/* Min */

		calc=new CalcSystem("min(1)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("min(1;2;3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("min(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("min(a;2;3)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		/* Modulo */

		calc=new CalcSystem("modulo(7;3)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(1,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("modulo(7,7;0,5)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(0.2,d,0.0000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("modulo(a;3)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{7},-1));

		calc=new CalcSystem("modulo(a;0,5)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0.2,calc.calcOrDefault(new double[]{7.7},-1),0.0000001);
	}

	/**
	 * Test: Funktionen, Anfangsbuchstaben P, R, S, T und V
	 */
	@Test
	void testPreOperatorsPRSTV() {
		CalcSystem calc;
		double D;

		/* Power */

		calc=new CalcSystem("power(2;3)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(8,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("power(0;0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("power(0;1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("power(25;1/2)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(5,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("power(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		assertEquals(8,calc.calcOrDefault(new double[]{2,3},-7));

		calc=new CalcSystem("power(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0,0},-7));

		calc=new CalcSystem("power(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0,1},-7));

		calc=new CalcSystem("power(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		assertEquals(5,calc.calcOrDefault(new double[]{25,0.5},-7));

		/* Random */

		calc=new CalcSystem("random()");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D>=0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("random(5)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D>=0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("random()");
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(null,-7)>=0);

		calc=new CalcSystem("random(5)");
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(null,-7)>=0);

		/* Round */

		calc=new CalcSystem("round(0,4)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(0,6)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(-1,8)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(-2,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(-1,2)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(-1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(-1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(-1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0.4},1234));

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{0.6},1234));

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},1234));

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-2,calc.calcOrDefault(new double[]{-1.8},1234));

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-1.2},1234));

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-1},1234));

		/* SCV */

		calc=new CalcSystem("scv(1;2;3;4;5)"); /* E=3, Var=2,5, SCV=Var/E²=277 */
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0.277,D,0.001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("scv(a;2;3;4;5)",new String[]{"a"}); /* E=3, Var=2,5, SCV=Var/E²=277 */
		assertTrue(calc.parse()<0);
		assertEquals(0.277,calc.calcOrDefault(new double[]{1},-7),0.001);

		/* Sign */

		calc=new CalcSystem("sign(3)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sign(0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sign(-3)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(-1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sign(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{3},1234));

		calc=new CalcSystem("sign(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},1234));

		calc=new CalcSystem("sign(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{-3},1234));

		/* Sin */

		calc=new CalcSystem("sin(0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sin(pi/2)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sin(pi)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sin(3*pi/2)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(-1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sin(2pi)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},123),0.000001);

		calc=new CalcSystem("sin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{Math.PI/2},123));

		calc=new CalcSystem("sin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{Math.PI},123),0.000001);

		calc=new CalcSystem("sin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1,calc.calcOrDefault(new double[]{3*Math.PI/2},123));

		calc=new CalcSystem("sin(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{2*Math.PI},123),0.000001);

		/* Sinh */

		calc=new CalcSystem("sinh(0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sinh(-1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D<0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sinh(1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sinh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},123));

		calc=new CalcSystem("sinh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)<0);

		calc=new CalcSystem("sinh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},0)>0);

		/* Sk */

		calc=new CalcSystem("sk(1;2;2;3;3;3;3;4;4;4;4;4;5;5)");
		assertTrue(calc.parse()<0);
		try {
			assertEquals(-0.4759749110056202,calc.calc(),0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		assertEquals(-0.4759749110056202,calc.calcOrDefault(new double[0],-123),0.0001);

		/* Sqr */

		calc=new CalcSystem("sqr(0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqr(3)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(9,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqr(-3)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(9,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqr(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},123));

		calc=new CalcSystem("sqr(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(9,calc.calcOrDefault(new double[]{3},123));

		calc=new CalcSystem("sqr(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(9,calc.calcOrDefault(new double[]{-3},123));

		/* Sqrt */

		calc=new CalcSystem("sqrt(0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqrt(25)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(5,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc1=new CalcSystem("sqrt(-25)");
		assertTrue(calc1.parse()<0);
		assertThrows(MathCalcError.class,()->calc1.calc());

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},123));

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(5,calc.calcOrDefault(new double[]{25},123));

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(123,calc.calcOrDefault(new double[]{-25},123));

		/* StdDev */

		calc=new CalcSystem("sd(1;2;3;4;5)"); /* Var=2,5 */
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(Math.sqrt(2.5),D,0.001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sd(a;2;3;4;5)",new String[]{"a"}); /* Var=2,5 */
		assertTrue(calc.parse()<0);
		assertEquals(Math.sqrt(2.5),calc.calcOrDefault(new double[]{1},-7),0.001);

		/* Sum */

		calc=new CalcSystem("sum(1;2;3;4;5)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(15,D,0.001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sum(a;2;3;4;5)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(15,calc.calcOrDefault(new double[]{1},-7),0.001);

		/* Tan */

		calc=new CalcSystem("tan(-1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D<0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tan(0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0,D,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tan(1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tan(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)<0);

		calc=new CalcSystem("tan(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},123),0.000001);

		calc=new CalcSystem("tan(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},123)>0);

		/* TanH */

		calc=new CalcSystem("tanh(0)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(0.0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tanh(0,1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tanh(10000000)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(1.0,D,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tanh(-0,1)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertTrue(D<0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tanh(-10000000)");
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(-1.0,D,0.000001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("tanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{0},-5));

		calc=new CalcSystem("tanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{0.1},-5)>0);

		calc=new CalcSystem("tanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1.0,calc.calcOrDefault(new double[]{10000000},-1),0.000001);

		calc=new CalcSystem("tanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-0.1},5)<0);

		calc=new CalcSystem("tanh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1.0,calc.calcOrDefault(new double[]{-10000000},1),0.000001);

		/* Varianz */

		calc=new CalcSystem("var(1;2;3;4;5)"); /* Var=2,5 */
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc();
			assertEquals(2.5,D,0.001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("var(a;2;3;4;5)",new String[]{"a"}); /* Var=2,5 */
		assertTrue(calc.parse()<0);
		assertEquals(2.5,calc.calcOrDefault(new double[]{1},-7),0.001);
	}

	/**
	 * Test: Diskrete Wahrscheinlichkeitsverteilungen (Zähldichte, Zufallszahlen)
	 */
	@Test
	void testDiscreteDistributions() {
		CalcSystem calc;
		double d;

		/* Hypergeometrische Verteilung - Dichte */

		calc=new CalcSystem("HypergeometricDist(x;Num;K;n)",new String[]{"x","Num","K","n"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,10,3,2});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<=2;k++) {
			try {
				d=calc.calc(new double[]{k,10,3,2});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		try {
			d=calc.calc(new double[]{3,10,3,2});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		try {
			d=calc.calc(new double[]{2,5,5,2});
			assertEquals(1.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calcFinal1=calc;
		assertThrowsExactly(MathCalcError.class,()->{
			calcFinal1.calc(new double[]{2,-3,5,2});
		});

		calc=new CalcSystem("HypergeometricDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Hypergeometrische Verteilung - Zufallszahlen */

		calc=new CalcSystem("HypergeometricDist(Num;K;n)",new String[]{"Num","K","n"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{10,3,2});
				assertTrue(d==0.0 || d==1.0 || d==2.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Binomialverteilung - Dichte */

		calc=new CalcSystem("BinomialDist(x;n;p)",new String[]{"x","n","p"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,10,0.2});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<=10;k++) {
			try {
				d=calc.calc(new double[]{k,10,0.2});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		try {
			d=calc.calc(new double[]{11,10,0.2});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calcFinal2=calc;
		assertThrowsExactly(MathCalcError.class,()->{
			calcFinal2.calc(new double[]{3,10,-0.2});
		});

		calc=new CalcSystem("BinomialDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Binomialverteilung - Zufallszahlen */

		calc=new CalcSystem("BinomialDist(n;p)",new String[]{"n","p"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{5,0.2});
				assertTrue(d==0.0 || d==1.0 || d==2.0 || d==3.0 || d==4.0 || d==5.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Binomialverteilung direkt - Dichte */

		calc=new CalcSystem("BinomialDistDirect(x;m;s)",new String[]{"x","m","s"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,10,3});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<=10;k++) {
			try {
				d=calc.calc(new double[]{k,10,3});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		testDistributionThrows("BinomialDistDirect(x;m;s)",new String[]{"x","m","s"},new double[]{0,10,-2});

		/* Binomialverteilung direkt - Zufallszahlen */

		calc=new CalcSystem("BinomialDistDirect(m;s)",new String[]{"m","s"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{10,3});
				assertTrue(d>=0);
				assertTrue(d<=50);
				assertTrue(d%1.0==0.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Poisson-Verteilung - Dichte */

		calc=new CalcSystem("PoissonDist(x;l)",new String[]{"x","l"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,0.2});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<10;k++) {
			try {
				d=calc.calc(new double[]{k,0.2});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		final CalcSystem calcFinal3=calc;
		assertThrowsExactly(MathCalcError.class,()->{
			calcFinal3.calc(new double[]{3,-0.2});
		});

		calc=new CalcSystem("PoissonDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Poisson-Verteilung - Zufallszahlen */

		calc=new CalcSystem("PoissonDist(l)",new String[]{"l"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{0.2});
				assertTrue(d>=0.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Negative hypergeometrische Verteilung - Dichte */

		calc=new CalcSystem("NegativeHypergeometricDist(x;Num;K;n)",new String[]{"x","Num","K","n"});
		assertTrue(calc.parse()<0);

		for (int k=-1;k<=9;k++) {
			try {
				d=calc.calc(new double[]{k,50,20,10});
				assertEquals(0,d);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		for (int k=10;k<=40;k++) {
			try {
				d=calc.calc(new double[]{k,50,20,10});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		try {
			d=calc.calc(new double[]{41,50,20,10});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		testDistributionThrows("NegativeHypergeometricDist(x;Num;K;n)",new String[]{"x","Num","K","n"},new double[]{15,-3,20,10});

		/* Negative hypergeometrische Verteilung - Zufallszahlen */

		calc=new CalcSystem("NegativeHypergeometricDist(Num;K;n)",new String[]{"Num","K","n"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{50,20,10});
				assertTrue(d>=10 && d<=40);
				assertTrue(d%1.0==0.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Negative Binomialverteilung - Dichte */

		calc=new CalcSystem("NegativeBinomialDist(x;r;p)",new String[]{"x","r","p"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,10,0.2});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<=10;k++) {
			try {
				d=calc.calc(new double[]{k,10,0.2});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		final CalcSystem calcFinal4=calc;
		assertThrowsExactly(MathCalcError.class,()->{
			calcFinal4.calc(new double[]{3,10,-0.2});
		});

		calc=new CalcSystem("NegativeBinomialDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Negative Binomialverteilung - Zufallszahlen */

		calc=new CalcSystem("NegativeBinomialDist(r;p)",new String[]{"r","p"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{5,0.2});
				assertTrue(d>=0.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Negative Binomialverteilung direkt - Dichte */

		calc=new CalcSystem("NegativeBinomialDistDirect(x;m;s)",new String[]{"x","m","s"});
		assertTrue(calc.parse()<0);

		for (int k=0;k<=50;k++) {
			try {
				d=calc.calc(new double[]{k,10,5});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		testDistributionThrows("NegativeBinomialDistDirect(x;m;s)",new String[]{"x","m","s"},new double[]{3,10,-0.2});

		/* Negative Binomialverteilung direkt - Zufallszahlen */

		calc=new CalcSystem("NegativeBinomialDistDirect(m;s)",new String[]{"m","s"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<50;i++) {
			try {
				d=calc.calc(new double[]{10,5});
				assertTrue(d>=0.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Zeta-Verteilung - Dichte */

		calc=new CalcSystem("ZetaDist(x;s)",new String[]{"x","s"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{0,3});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=1;k<10;k++) {
			try {
				d=calc.calc(new double[]{k,3});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		final CalcSystem calcFinal5=calc;
		assertThrowsExactly(MathCalcError.class,()->{
			calcFinal5.calc(new double[]{3,0.5});
		});

		calc=new CalcSystem("ZetaDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Zeta-Verteilung - Zufallszahlen */

		calc=new CalcSystem("ZetaDist(s)",new String[]{"s"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{3});
				assertTrue(d>=1.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Diskrete Gleichverteilung - Dichte */

		calc=new CalcSystem("DiscreteUniformDist(x;a;b)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{0,2,5});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=1;k<10;k++) {
			try {
				d=calc.calc(new double[]{k,2,5});
				if (k<2 || k>5) {
					assertEquals(0,d);
				} else {
					assertEquals(0.25,d);
				}
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		calc=new CalcSystem("DiscreteUniformDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Diskrete Gleichverteilung - Zufallszahlen */

		calc=new CalcSystem("DiscreteUniformDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{2,5});
				assertTrue(d>=2.0 && d<=5.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Geometrische Verteilung - Dichte */

		calc=new CalcSystem("GeometricDist(x;a)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,0.5});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<10;k++) {
			try {
				d=calc.calc(new double[]{k,0.5});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		calc=new CalcSystem("GeometricDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Geometrische Verteilung - Zufallszahlen */

		calc=new CalcSystem("GeometricDist(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{0.5});
				assertTrue(d>=0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Logarithmische Verteilung - Dichte */

		calc=new CalcSystem("LogarithmicDist(x;a)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,0.5});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		try {
			d=calc.calc(new double[]{0,0.5});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=1;k<10;k++) {
			try {
				d=calc.calc(new double[]{k,0.5});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		calc=new CalcSystem("LogarithmicDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Logarithmische Verteilung - Zufallszahlen */

		calc=new CalcSystem("LogarithmicDist(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{0.5});
				assertTrue(d>=0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Borel-Verteilung - Dichte */

		calc=new CalcSystem("BorelDist(x;a)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,0.5});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		try {
			d=calc.calc(new double[]{0,0.5});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=1;k<10;k++) {
			try {
				d=calc.calc(new double[]{k,0.5});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		calc=new CalcSystem("BorelDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Borel-Verteilung - Zufallszahlen */

		calc=new CalcSystem("BorelDist(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{0.5});
				assertTrue(d>=0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Planck-Verteilung - Dichte */

		calc=new CalcSystem("PlanckDist(x;l)",new String[]{"x","l"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,0.2});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<10;k++) {
			try {
				d=calc.calc(new double[]{k,0.2});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		final CalcSystem calcFinal6=calc;
		assertThrowsExactly(MathCalcError.class,()->{
			calcFinal6.calc(new double[]{3,-0.2});
		});

		calc=new CalcSystem("PlanckDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Planck-Verteilung - Zufallszahlen */

		calc=new CalcSystem("PlanckDist(l)",new String[]{"l"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{0.2});
				assertTrue(d>=0.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Boltzmann-Verteilung - Dichte */

		calc=new CalcSystem("BoltzmannDist(x;l;N)",new String[]{"x","l","N"});
		assertTrue(calc.parse()<0);

		try {
			d=calc.calc(new double[]{-1,0.25,20});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		for (int k=0;k<=19;k++) {
			try {
				d=calc.calc(new double[]{k,0.25,20});
				assertTrue(d>0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		try {
			d=calc.calc(new double[]{20,0.25,20});
			assertEquals(0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calcFinal7=calc;
		assertThrowsExactly(MathCalcError.class,()->{
			calcFinal7.calc(new double[]{3,-0.25,20});
		});

		calc=new CalcSystem("BoltzmannDist(x;y;z;a;b)",new String[]{"x","y","z","a","b"});
		assertTrue(calc.parse()<0);
		d=calc.calcOrDefault(new double[]{1,2,3,4,5},-17);
		assertEquals(-17.0,d);

		/* Boltzmann-Verteilung - Zufallszahlen */

		calc=new CalcSystem("BoltzmannDist(l;N)",new String[]{"l","N"});
		assertTrue(calc.parse()<0);

		for (int i=0;i<100;i++) {
			try {
				d=calc.calc(new double[]{0.25,20});
				assertTrue(d>=0);
				assertTrue(d<=19);
				assertTrue(d%1.0==0.0);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}
	}

	/**
	 * Interpretiert und berechnet einen einzelnen Verteilungsausdruck.
	 * @param text Zu interpretierender Ausdruck
	 * @param variables	Namen der Variablen
	 * @param values	Werte für die Variablen
	 * @return	Rückgabewert der Berechnung
	 */
	private double testDistribution(final String text, final String[] variables, final double[] values) {
		final CalcSystem calc=new CalcSystem(text,variables);
		assertTrue(calc.parse()<0);
		try {
			return calc.calc(values);
		} catch (MathCalcError e) {
			assertTrue(false,e.getMessage());
			return -1;
		}
	}

	/**
	 * Interpretiert einen einzelnen Verteilungsausdruck und prüft, ob beim Auswerten eine Exception auftritt.
	 * @param text Zu interpretierender Ausdruck
	 * @param variables	Namen der Variablen
	 * @param values	Werte für die Variablen
	 */
	private void testDistributionThrows(final String text, final String[] variables, final double[] values) {
		final CalcSystem calc=new CalcSystem(text,variables);
		assertTrue(calc.parse()<0);
		assertThrows(MathCalcError.class,()->calc.calc(values));
	}

	/**
	 * Test: Wahrscheinlichkeitsverteilungen (Dichte, Verteilung, Zufallszahlen)
	 */
	@Test
	void testContinuousDistributions() {
		String cmd;
		String[] variables;

		/* Arcus Sinus-Verteilung */

		cmd="ArcsineDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{151,50,150}));

		cmd="ArcsineDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{151,50,150}));
		testDistributionThrows("ArcsineDist(x;a;b;2)",variables,new double[]{100,50,150});

		cmd="ArcsineDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{50,150});

		/* Betaverteilung */

		cmd="BetaDist(x;a;b;c;d;0)";
		variables=new String[]{"x","a","b","c","d"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,3,0.5,0.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,1,3,0.5,0.5})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{4,1,3,0.5,0.5}));

		cmd="BetaDist(x;a;b;c;d;1)";
		variables=new String[]{"x","a","b","c","d"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,3,0.5,0.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,1,3,0.5,0.5})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{4,1,3,0.5,0.5}));
		testDistributionThrows("BetaDist(x;a;b;c;d;2)",variables,new double[]{-0.1,5});

		cmd="BetaDist(a;b;c;d)";
		variables=new String[]{"a","b","c","d"};
		testDistribution(cmd,variables,new double[]{1,3,0.5,0.5});

		/* Betaverteilung - Direkt */

		cmd="BetaDistDirect(x;a;b;c;d;0)";
		variables=new String[]{"x","a","b","c","d"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,3,2.5,0.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,1,3,2.5,0.5})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{4,1,3,2.5,0.5}));

		cmd="BetaDistDirect(x;a;b;c;d;1)";
		variables=new String[]{"x","a","b","c","d"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,3,2.5,0.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,1,3,2.5,0.5})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{4,1,3,2.5,0.5}));
		testDistributionThrows("BetaDistDirect(x;a;b;c;d;2)",variables,new double[]{-0.1,5});

		cmd="BetaDistDirect(a;b;c;d)";
		variables=new String[]{"a","b","c","d"};
		testDistribution(cmd,variables,new double[]{1,3,2.5,0.5});

		/* Cauchyverteilung */

		cmd="CauchyDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{-0.1,1,1})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{2,1,1})>0);

		cmd="CauchyDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{-0.1,1,1})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{2,1,1})>0);
		testDistributionThrows("CauchyDist(x;a;b;2)",variables,new double[]{-0.1,1,1});

		cmd="CauchyDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{1,1});

		/* Chi-Verteilung */

		cmd="ChiDistribution(x;a;0)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,50}));
		assertTrue(testDistribution(cmd,variables,new double[]{7,50})>0);

		cmd="ChiDistribution(x;a;1)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,50}));
		assertTrue(testDistribution(cmd,variables,new double[]{7,50})>0);
		testDistributionThrows("ChiDistribution(x;a;2)",variables,new double[]{100,50});

		cmd="ChiDistribution(a)";
		variables=new String[]{"a"};
		testDistribution(cmd,variables,new double[]{1});

		/* Chi²-Verteilung */

		cmd="ChiSquareDist(x;n;0)";
		variables=new String[]{"x","n"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,200}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,200})>0);

		cmd="ChiSquareDist(x;n;1)";
		variables=new String[]{"x","n"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,200}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,200})>0);
		testDistributionThrows("ChiSquareDist(x;n;2)",variables,new double[]{200,200});

		cmd="ChiSquareDist(n)";
		variables=new String[]{"n"};
		testDistribution(cmd,variables,new double[]{200});

		/* Dreiecksverteilung */

		cmd="TriangularDist(x;a;b;c;0)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,1800,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,1800,2700})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{3000,900,1800,2700}));

		cmd="TriangularDist(x;a;b;c;1)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,1800,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,1800,2700})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{3000,900,1800,2700}));
		testDistributionThrows("TriangularDist(x;a;b;c;2)",variables,new double[]{3000,900,1800,2700});

		cmd="TriangularDist(a;b;c)";
		variables=new String[]{"a","b","c"};
		testDistribution(cmd,variables,new double[]{900,1800,2700});

		/* Exponentialverteilung */

		cmd="ExpDist(x;a;0)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,5})>0);

		cmd="ExpDist(x;a;1)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,5})>0);
		testDistributionThrows("ExpDist(x;a;2)",new String[]{"x","a"},new double[]{-0.1,5});

		cmd="ExpDist(a)";
		variables=new String[]{"a"};
		testDistribution(cmd,variables,new double[]{5});

		/* F-Verteilung */

		cmd="FDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,100,10}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,100,10})>0);

		cmd="FDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,100,10}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,100,10})>0);
		testDistributionThrows("FDist(x;a;b;2)",variables,new double[]{-0.1,100,10});

		cmd="FDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{100,10});

		/* Frechet-Verteilung */

		cmd="FrechetDistribution(x;a;b;c;0)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2,3})>0);

		cmd="FrechetDistribution(x;a;b;c;1)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2,3})>0);
		testDistributionThrows("FrechetDistribution(x;a;b;c;2)",variables,new double[]{100,1,2,3});

		cmd="FrechetDistribution(a;b;c)";
		variables=new String[]{"a","b","c"};
		testDistribution(cmd,variables,new double[]{1,2,3});

		/* Fatigue-Life-Verteilung */

		cmd="FatigueLifeDistribution(x;a;b;c;0)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2,3})>0);

		cmd="FatigueLifeDistribution(x;a;b;c;1)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2,3})>0);
		testDistributionThrows("FatigueLifeDistribution(x;a;b;c;2)",variables,new double[]{100,1,2,3});

		cmd="FatigueLifeDistribution(a;b;c)";
		variables=new String[]{"a","b","c"};
		testDistribution(cmd,variables,new double[]{1,2,3});

		/* Gammaverteilung */

		cmd="GammaDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,100,6}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,6})>0);

		cmd="GammaDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,100,6}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,100,6}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,6})>0);
		testDistributionThrows("GammaDist(x;a;b;2)",variables,new double[]{1000,100,6});

		cmd="GammaDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{100,6});

		/* Gammaverteilung - Direkt */

		cmd="GammaDistDirect(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,10,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{10,10,5})>0);

		cmd="GammaDistDirect(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,10,5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,10,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{10,10,5})>0);
		testDistributionThrows("GammaDistDirect(x;a;b;2)",variables,new double[]{10,10,5});

		cmd="GammaDistDirect(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{10,5});

		/* Gleichverteilung */

		cmd="UniformDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{3000,900,2700}));

		cmd="UniformDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{3000,900,2700}));
		testDistributionThrows("UniformDist(x;a;b;2)",variables,new double[]{1000,900,2700});

		cmd="UniformDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{900,2700});

		/* Gumbel-Verteilung */

		cmd="GumbelDistribution(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2})>0);

		cmd="GumbelDistribution(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2})>0);
		testDistributionThrows("GumbelDistribution(x;a;b;2)",variables,new double[]{100,1,2});

		cmd="GumbelDistribution(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{1,2});

		/* Halbe Cauchy-Verteilung */

		cmd="HalfCauchyDist(x;mu;sigma;0)";
		variables=new String[]{"x","mu","sigma"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{99,100,50}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,100,50})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{120,100,50})>0);

		cmd="HalfCauchyDist(x;mu;sigma;1)";
		variables=new String[]{"x","mu","sigma"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{99,100,50}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{100,100,50}));
		assertTrue(testDistribution(cmd,variables,new double[]{120,100,50})>0);
		testDistributionThrows("HalfCauchyDist(x;mu;sigma;2)",variables,new double[]{110,100,50});

		cmd="HalfCauchyDist(mu;sigma)";
		variables=new String[]{"mu","sigma"};
		testDistribution(cmd,variables,new double[]{100,50});

		/* Halbe Normalverteilung */

		cmd="HalfNormalDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-1,1,1}));
		assertTrue(testDistribution(cmd,variables,new double[]{10,1,1})>0);

		cmd="HalfNormalDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-1,1,1}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,1}));
		assertTrue(testDistribution(cmd,variables,new double[]{10,1,1})>0);
		testDistributionThrows("HalfNormalDist(x;a;b;2)",variables,new double[]{10,1,1});

		cmd="HalfNormalDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{1,1});

		/* Hyperbolische Sekanten-Verteilung */

		cmd="HyperbolicSecantDistribution(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2})>0);

		cmd="HyperbolicSecantDistribution(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{100,1,2})>0);
		testDistributionThrows("HyperbolicSecantDistribution(x;a;b;2)",variables,new double[]{100,1,2});

		cmd="HyperbolicSecantDistribution(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{1,2});

		/* Inverse Gamma-Verteilung */

		cmd="InverseGammaDistribution(x;alpha;beta;0)";
		variables=new String[]{"x","alpha","beta"};
		assertTrue(testDistribution(cmd,variables,new double[]{20,5,100})>0);

		cmd="InverseGammaDistribution(x;alpha;beta;1)";
		variables=new String[]{"x","alpha","beta"};
		assertTrue(testDistribution(cmd,variables,new double[]{20,5,100})>0);
		testDistributionThrows("HyperbolicSecantDistribution(x;alpha;beta;2)",variables,new double[]{20,5,100});

		cmd="InverseGammaDistribution(alpha;beta)";
		variables=new String[]{"alpha","beta"};
		testDistribution(cmd,variables,new double[]{5,100});

		/* Inverse Gaußverteilung */

		cmd="InverseGaussianDist(x;l;mu;0)";
		variables=new String[]{"x","l","mu"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,900,1800}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,1800})>0);

		cmd="InverseGaussianDist(x;l;mu;1)";
		variables=new String[]{"x","l","mu"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,900,1800}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,900,1800}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,1800})>0);
		testDistributionThrows("InverseGaussianDist(x;l;mu;2)",variables,new double[]{1000,900,1800});

		cmd="InverseGaussianDist(l;mu)";
		variables=new String[]{"l","mu"};
		testDistribution(cmd,variables,new double[]{900,1800});

		/* Irwin-Hall-Verteilung */

		cmd="IrwinHallDist(x;n;0)";
		variables=new String[]{"x","n"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{3,5})>0);

		cmd="IrwinHallDist(x;n;1)";
		variables=new String[]{"x","n"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{3,5})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{5,5}));
		testDistributionThrows("IrwinHallDist(x;n;2)",variables,new double[]{3,5});

		cmd="IrwinHallDist(n)";
		variables=new String[]{"n"};
		testDistribution(cmd,variables,new double[]{5});

		/* Irwin-Hall-Verteilung - Direkt */

		cmd="IrwinHallDistDirect(x;n;0)";
		variables=new String[]{"x","n"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,2.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{3,2.5})>0);

		cmd="IrwinHallDistDirect(x;n;1)";
		variables=new String[]{"x","n"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,2.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,2.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{3,2.5})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{5,2.5}));
		testDistributionThrows("IrwinHallDistDirect(x;n;2)",variables,new double[]{3,2.5});

		cmd="IrwinHallDistDirect(n)";
		variables=new String[]{"n"};
		testDistribution(cmd,variables,new double[]{2.5});

		/* Johnson-SU-Verteilung */

		cmd="JohnsonSUDist(x;a;b;c;d;0)";
		variables=new String[]{"x","a","b","c","d"};
		assertTrue(testDistribution(cmd,variables,new double[]{0,2,1800,1,180})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{2,2,1800,1,180})>0);

		cmd="JohnsonSUDist(x;a;b;c;d;1)";
		variables=new String[]{"x","a","b","c","d"};
		assertTrue(testDistribution(cmd,variables,new double[]{2,2,1800,1,180})>0);
		testDistributionThrows("JohnsonSUDist(x;a;b;c;d;2)",variables,new double[]{2,2,1800,1,180});

		cmd="JohnsonSUDist(a;b;c;d)";
		variables=new String[]{"a","b","c","d"};
		testDistribution(cmd,variables,new double[]{2,1800,1,180});

		/* Kumaraswamy-Verteilung */

		cmd="KumaraswamyDist(x;a;b;c;d;0)";
		variables=new String[]{"x","a","b","c","d"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{75,1,2,50,150})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{200,1,2,50,150}));

		cmd="KumaraswamyDist(x;a;b;c;d;1)";
		variables=new String[]{"x","a","b","c","d"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,1,2,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{75,1,2,50,150})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{150,1,2,50,150}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{200,1,2,50,150}));
		testDistributionThrows("KumaraswamyDist(x;a;b;c;d;2)",variables,new double[]{75,1,2,50,150});

		cmd="KumaraswamyDist(a;b;c;d)";
		variables=new String[]{"a","b","c","d"};
		testDistribution(cmd,variables,new double[]{1,2,50,150});

		/* Laplace-Verteilung */

		cmd="LaplaceDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{0,720,360})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{200,720,360})>0);

		cmd="LaplaceDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{200,720,360})>0);
		testDistributionThrows("LaplaceDist(x;a;b;2)",variables,new double[]{200,720,360});

		cmd="LaplaceDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{720,360});

		/* Levy-Verteilung */

		cmd="LevyDist(x;mu;c;0)";
		variables=new String[]{"x","mu","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{2,3,10}));
		assertTrue(testDistribution(cmd,variables,new double[]{7,3,10})>0);

		cmd="LevyDist(x;mu;c;1)";
		variables=new String[]{"x","mu","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{2,3,10}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{3,3,10}));
		assertTrue(testDistribution(cmd,variables,new double[]{7,3,10})>0);
		testDistributionThrows("LevyDist(x;mu;c;2)",variables,new double[]{7,3,10});

		cmd="LevyDist(mu;c)";
		variables=new String[]{"mu","c"};
		testDistribution(cmd,variables,new double[]{3,10});

		/* Linke Sägezahnverteilung */

		cmd="LeftSawtoothDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{3000,900,2700}));

		cmd="LeftSawtoothDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,2700}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{900,900,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})<1);
		assertEquals(1,testDistribution(cmd,variables,new double[]{2700,900,2700}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{3000,900,2700}));
		testDistributionThrows("LeftSawtoothDist(x;a;b;2)",variables,new double[]{1000,900,2700});

		cmd="LeftSawtoothDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{900,2700});

		/* Linke Sägezahnverteilung - Direkt */

		cmd="LeftSawtoothDistDirect(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);

		cmd="LeftSawtoothDistDirect(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})<1);
		testDistributionThrows("LeftSawtoothDist(x;a;b;2)",variables,new double[]{1000,900,2700});

		cmd="LeftSawtoothDistDirect(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{900,2700});

		/* Log-Cauchy-Verteilung */

		cmd="LogCauchyDist(x;mu;sigma;0)";
		variables=new String[]{"x","mu","sigma"};
		assertTrue(testDistribution(cmd,variables,new double[]{10,4.5,0.5})>0);

		cmd="LogCauchyDist(x;mu;sigma;1)";
		variables=new String[]{"x","mu","sigma"};
		assertTrue(testDistribution(cmd,variables,new double[]{10,4.5,0.5})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{10,4.5,0.5})<1);
		testDistributionThrows("LogCauchyDist(x;mu;sigma;2)",variables,new double[]{10,4.5,0.5});

		cmd="LogCauchyDist(mu;sigma)";
		variables=new String[]{"mu","sigma"};
		testDistribution(cmd,variables,new double[]{4.5,0.5});

		/* Log-Logistische Verteilung */

		cmd="LogLogisticDistribution(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,1,2}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2}));
		assertTrue(testDistribution(cmd,variables,new double[]{5,1,2})>0);

		cmd="LogLogisticDistribution(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-0.1,1,2}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2}));
		assertTrue(testDistribution(cmd,variables,new double[]{5,1,2})>0);
		testDistributionThrows("LogLogisticDistribution(x;a;b;2)",variables,new double[]{5,1,2});

		cmd="LogLogisticDistribution(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{1,2});

		/* Logistische Verteilung */

		cmd="LogisticDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{0,1200,360})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{200,1200,360})>0);

		cmd="LogisticDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{0,1200,360})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{200,1200,360})>0);
		testDistributionThrows("LogisticDist(x;a;b;2)",variables,new double[]{200,1200,360});

		cmd="LogisticDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{0,1200,360});

		/* Log-Normalverteilung */

		cmd="LogNormalDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-1,600,200}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,600,200}));
		assertTrue(testDistribution(cmd,variables,new double[]{500,600,200})>0);

		cmd="LogNormalDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-1,600,200}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,600,200}));
		assertTrue(testDistribution(cmd,variables,new double[]{500,600,200})>0);
		testDistributionThrows("LogNormalDist(x;a;b;2)",variables,new double[]{500,600,200});

		cmd="LogNormalDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{500,600,200});

		/* Maxwell-Boltzmann-Verteilung */

		cmd="MaxwellBoltzmannDist(x;a;0)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{-2,10}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,10})>0);

		cmd="MaxwellBoltzmannDist(x;a;1)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,10}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,10})>0);
		testDistributionThrows("MaxwellBoltzmannDist(x;a;2)",variables,new double[]{2,10});

		cmd="MaxwellBoltzmannDist(a)";
		variables=new String[]{"a"};
		testDistribution(cmd,variables,new double[]{10});

		/* Normalverteilung */

		cmd="NormalDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{-1,600,200})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{0,600,200})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{500,600,200})>0);

		cmd="NormalDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{-1,600,200})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{-1,600,200})<1);
		assertTrue(testDistribution(cmd,variables,new double[]{0,600,200})<1);
		assertTrue(testDistribution(cmd,variables,new double[]{0,600,200})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{500,600,200})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{500,600,200})<1);
		testDistributionThrows("NormalDist(x;a;b;2)",variables,new double[]{500,600,200});

		cmd="NormalDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{600,200});

		/* Paretoverteilung */

		cmd="ParetoDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{10,300,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{500,300,3})>0);

		cmd="ParetoDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{10,300,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{500,300,3})>0);
		testDistributionThrows("ParetoDist(x;a;b;2)",variables,new double[]{500,300,3});

		cmd="ParetoDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{300,3});

		/* Pert-Verteilung */

		cmd="PertDist(x;a;b;c;0)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,1800,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,1800,2700})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{3000,900,1800,2700}));

		cmd="PertDist(x;a;b;c;1)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,1800,2700}));
		assertEquals(0.5,testDistribution(cmd,variables,new double[]{1800,900,1800,2700}),0.00001);
		assertEquals(1,testDistribution(cmd,variables,new double[]{3000,900,1800,2700}));
		testDistributionThrows("PertDist(x;a;b;c;2)",variables,new double[]{3000,900,1800,2700});

		cmd="PertDist(a;b;c)";
		variables=new String[]{"a","b","c"};
		testDistribution(cmd,variables,new double[]{900,1800,2700});

		/* Potenz-Verteilung */

		cmd="PowerDistribution(x;a;b;c;0)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{1.5,1,2,3})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{5,1,2,3}));

		cmd="PowerDistribution(x;a;b;c;1)";
		variables=new String[]{"x","a","b","c"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,1,2,3}));
		assertTrue(testDistribution(cmd,variables,new double[]{1.5,1,2,3})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{5,1,2,3}));
		testDistributionThrows("PowerDistribution(x;a;b;c;2)",variables,new double[]{100,1,2,3});

		cmd="PowerDistribution(a;b;c)";
		variables=new String[]{"a","b","c"};
		testDistribution(cmd,variables,new double[]{1,2,3});

		/* Rayleigh Verteilung */

		cmd="RayleighDistribution(x;a;0)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,50}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50})>0);

		cmd="RayleighDistribution(x;a;1)";
		variables=new String[]{"x","a"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,50}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50})>0);
		testDistributionThrows("RayleighDistribution(x;a;2)",variables,new double[]{100,50});

		cmd="RayleighDistribution(a)";
		variables=new String[]{"a"};
		testDistribution(cmd,variables,new double[]{1});

		/* Rechte Sägezahnverteilung */

		cmd="RightSawtoothDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{3000,900,2700}));

		cmd="RightSawtoothDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,900,2700}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{900,900,2700}));
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})<1);
		assertEquals(1,testDistribution(cmd,variables,new double[]{2700,900,2700}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{3000,900,2700}));
		testDistributionThrows("RightSawtoothDist(x;a;b;2)",variables,new double[]{1000,900,2700});

		cmd="RightSawtoothDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{900,2700});

		/* Rechte Sägezahnverteilung - Direkt */

		cmd="RightSawtoothDistDirect(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);

		cmd="RightSawtoothDistDirect(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{1000,900,2700})<1);
		testDistributionThrows("RightSawtoothDistDirect(x;a;b;2)",variables,new double[]{1000,900,2700});

		cmd="RightSawtoothDistDirect(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{900,2700});

		/* Reziproke Verteilung */

		cmd="ReciprocalDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{50,50,150})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{150,50,150})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{151,50,150}));

		cmd="ReciprocalDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{151,50,150}));
		testDistributionThrows("ReciprocalDist(x;a;b;2)",variables,new double[]{100,50,150});

		cmd="ReciprocalDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{50,150});

		/* Sinus-Verteilung */

		cmd="SineDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{151,50,150}));

		cmd="SineDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{151,50,150}));
		testDistributionThrows("SineDist(x;a;b;2)",variables,new double[]{100,50,150});

		cmd="SineDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{50,150});

		/* Cosinus-Verteilung */

		cmd="CosineDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{151,50,150}));

		cmd="CosineDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{151,50,150}));
		testDistributionThrows("CosineDist(x;a;b;2)",variables,new double[]{100,50,150});

		cmd="CosineDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{50,150});

		/* Student-t Verteilung */

		cmd="StudentTDist(x;mu;nu;0)";
		variables=new String[]{"x","mu","nu"};
		assertTrue(testDistribution(cmd,variables,new double[]{25,50,2})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{50,50,2})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{75,50,2})>0);

		cmd="StudentTDist(x;mu;nu;1)";
		variables=new String[]{"x","mu","nu"};
		assertTrue(testDistribution(cmd,variables,new double[]{25,50,2})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{50,50,2})>0);
		assertTrue(testDistribution(cmd,variables,new double[]{75,50,2})>0);
		testDistributionThrows("StudentTDist(x;mu;nu;2)",variables,new double[]{50,50,2});

		cmd="StudentTDist(mu;nu)";
		variables=new String[]{"mu","nu"};
		testDistribution(cmd,variables,new double[]{50,2});

		/* U-quadratische Verteilung */

		cmd="UQuadraticDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{50,50,150})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{100,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{150,50,150})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{151,50,150}));

		cmd="UQuadraticDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{49,50,150}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,50,150}));
		assertTrue(testDistribution(cmd,variables,new double[]{100,50,150})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{150,50,150}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{151,50,150}));
		testDistributionThrows("UQuadraticDist(x;a;b;2)",variables,new double[]{100,50,150});

		cmd="UQuadraticDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{50,150});

		/* Weibull-Verteilung */

		cmd="WeibullDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,0.0027,2}));
		assertTrue(testDistribution(cmd,variables,new double[]{50,0.0027,2})>0);

		cmd="WeibullDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,0.0027,2}));
		assertTrue(testDistribution(cmd,variables,new double[]{50,0.0027,2})>0);
		testDistributionThrows("WeibullDist(x;a;b;2)",variables,new double[]{50,0.0027,2});

		cmd="WeibullDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{0.0027,2});

		/* Wigner Halbkreisverteilung */

		cmd="WignerHalfCircleDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,5,2}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{3,5,2}));
		assertTrue(testDistribution(cmd,variables,new double[]{4,5,2})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{7,5,2}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{8,5,2}));

		cmd="WignerHalfCircleDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,5,2}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{3,5,2}));
		assertTrue(testDistribution(cmd,variables,new double[]{4,5,2})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{7,5,2}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{8,5,2}));
		testDistributionThrows("WignerHalfCircleDist(x;a;b;2)",variables,new double[]{4,5,2});

		cmd="WignerHalfCircleDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{5,2});

		/* Log-Gamma-Verteilung */

		cmd="LogGammaDist(x;a;b;0)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,4.5,3.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{1,4.5,3.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,4.5,3.5})>0);

		cmd="LogGammaDist(x;a;b;1)";
		variables=new String[]{"x","a","b"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,4.5,3.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{1,4.5,3.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{2,4.5,3.5})>0);
		testDistributionThrows("LogGammaDist(x;a;b;2)",variables,new double[]{2,4.5,3.5});

		cmd="LogGammaDist(a;b)";
		variables=new String[]{"a","b"};
		testDistribution(cmd,variables,new double[]{4.5,3.5});

		/* Kontinuierliche Bernoulli-Verteilung */

		cmd="ContinuousBernoulliDist(x;a;b;l;0)";
		variables=new String[]{"x","a","b","l"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.3}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,400,0.3})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,100,400,0.3}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,400,0.5})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,100,400,0.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.7}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,400,0.7})>0);
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,100,400,0.7}));

		cmd="ContinuousBernoulliDist(x;a;b;l;1)";
		variables=new String[]{"x","a","b","l"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.3}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,400,0.3})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{500,100,400,0.3}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.5}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,400,0.5})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{500,100,400,0.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.7}));
		assertTrue(testDistribution(cmd,variables,new double[]{200,100,400,0.7})>0);
		assertEquals(1,testDistribution(cmd,variables,new double[]{500,100,400,0.7}));

		testDistributionThrows("ContinuousBernoulliDist(x;a;b;l;2)",variables,new double[]{1000,900,2700});

		cmd="ContinuousBernoulliDist(a;b;l)";
		variables=new String[]{"a","b","l"};
		testDistribution(cmd,variables,new double[]{100,400,0.3});
		testDistribution(cmd,variables,new double[]{100,400,0.5});
		testDistribution(cmd,variables,new double[]{100,400,0.7});

		/* Verallgemeinerte Rademacher-Verteilung */

		cmd="GeneralizedRademacherDist(x;a;b;pA;0)";
		variables=new String[]{"x","a","b","pA"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.3}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,100,400,0.3}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,100,400,0.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.7}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{500,100,400,0.7}));

		cmd="GeneralizedRademacherDist(x;a;b;pA;1)";
		variables=new String[]{"x","a","b","pA"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.3}));
		assertEquals(0.3,testDistribution(cmd,variables,new double[]{200,100,400,0.3}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{500,100,400,0.3}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.5}));
		assertEquals(0.5,testDistribution(cmd,variables,new double[]{200,100,400,0.5}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{500,100,400,0.5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{50,100,400,0.7}));
		assertEquals(0.7,testDistribution(cmd,variables,new double[]{200,100,400,0.7}));
		assertEquals(1,testDistribution(cmd,variables,new double[]{500,100,400,0.7}));

		testDistributionThrows("GeneralizedRademacherDist(x;a;b;pA;2)",variables,new double[]{100,50,150,0.5});

		cmd="GeneralizedRademacherDist(a;b;pA)";
		variables=new String[]{"a","b","pA"};
		testDistribution(cmd,variables,new double[]{100,400,0.3});
		testDistribution(cmd,variables,new double[]{100,400,0.5});
		testDistribution(cmd,variables,new double[]{100,400,0.7});

		/* Log-Laplace-Verteilung */

		cmd="LogLaplaceDist(x;c;s;0)";
		variables=new String[]{"x","c","s"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,2.5,5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{5,2.5,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{7,2.5,5})>0);

		cmd="LogLaplaceDist(x;c;s;1)";
		variables=new String[]{"x","c","s"};
		assertEquals(0,testDistribution(cmd,variables,new double[]{0,2.5,5}));
		assertEquals(0,testDistribution(cmd,variables,new double[]{5,2.5,5}));
		assertTrue(testDistribution(cmd,variables,new double[]{7,2.5,5})>0);
		testDistributionThrows("LogLaplaceDist(x;c;s;2)",variables,new double[]{10,2.5,5});

		cmd="LogLaplaceDist(c;s)";
		variables=new String[]{"c","s"};
		testDistribution(cmd,variables,new double[]{2.5,5});
	}

	/**
	 * Test: Empirische Verteilungsfunktion
	 */
	@Test
	void testEmpiricalDistribution() {
		CalcSystem calc;
		double result;
		double d;

		/* Dichte */

		for (int i=-10;i<=150;i++) {
			d=i/10.0;
			calc=new CalcSystem("EmpirischeDichte("+NumberTools.formatNumber(d)+";2;1;3;10)");
			assertTrue(calc.parse()<0);
			try {
				result=calc.calc();
				if (d<0) assertEquals(0.0,result,0001);
				if (d>=0 && d<=3.3) assertEquals(2.0/(2+1+3),result,0.0001);
				if (d>=3.4 && d<=6.6) assertEquals(1.0/(2+1+3),result,0.0001);
				if (d>=6.7 && d<=10.0) assertEquals(3.0/(2+1+3),result,0.0001);
				if (d>10) assertEquals(0.0,result,0.0001);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Verteilungsfunktion */

		double last=-1;
		for (int i=-10;i<=150;i++) {
			d=i/10.0;
			calc=new CalcSystem("EmpirischeVerteilung("+NumberTools.formatNumber(d)+";2;1;3;10)");
			assertTrue(calc.parse()<0);
			try {
				result=calc.calc();
				if (d<=0) assertEquals(0.0,result,0001);
				if (d>0 && d<10-0.001) {
					assertTrue(result>0 && result<1);
					assertTrue(result>last);
					last=result;
				}
				if (Math.abs(d-3.3)<0.0001) assertEquals(2.0/(2+1+3),result,0.1);
				if (Math.abs(d-6.6)<0.0001) assertEquals(3.0/(2+1+3),result,0.1);
				if (d>10) assertEquals(1.0,result,0.0001);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Zufallszahlen */

		calc=new CalcSystem("EmpirischeZufallszahl(2;1;3;10");
		assertTrue(calc.parse()<0);
		for (int i=0;i<1_000;i++) {
			try {
				result=calc.calc();
				assertTrue(result>=0);
				assertTrue(result<10);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
		}

		/* Mittelwert */

		calc=new CalcSystem("EmpirischeVerteilungMittelwert(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(((double)(7*0+2*1+1*2+3*3+10*4))/(7+2+1+3+10),result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Median */

		calc=new CalcSystem("EmpirischeVerteilungMedian(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(3,result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Quantile */

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,2)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(0,result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,4)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(2,result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,6)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(4,result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,8)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(4,result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Standardabweichung */

		calc=new CalcSystem("EmpirischeVerteilungStandardabweichung(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(Math.sqrt(3.0812854442344038),result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Varianz */

		calc=new CalcSystem("EmpirischeVerteilungVarianz(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(3.0812854442344038,result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Variationskoeffizient */

		calc=new CalcSystem("EmpirischeVerteilungCV(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		try {
			result=calc.calc();
			assertEquals(Math.sqrt(3.0812854442344038)/(((double)(7*0+2*1+1*2+3*3+10*4))/(7+2+1+3+10)),result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}
}