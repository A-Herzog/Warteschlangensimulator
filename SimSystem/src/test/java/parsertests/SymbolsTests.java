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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import mathtools.NumberTools;
import parser.CalcSystem;
import parser.MathCalcError;

/**
 * Prüft die Funktionsweise der Rechensymbole von {@link CalcSystem}
 * @author Alexander Herzog
 * @see CalcSystem
 */
public class SymbolsTests {
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
	 * Test: Punkt-Vor-Stich-Rechnung
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
			assertEquals(189.91,d,0.01);
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
	 * Test: Funktionen, Anfangsbuchstabem L und M
	 */
	@Test
	void testPreOperatorsLM() {
		CalcSystem calc;
		double d;

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
		assertEquals(2,calc.calcOrDefault(new double[]{Math.pow(Math.E,2)},-1));

		calc=new CalcSystem("ln(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3,calc.calcOrDefault(new double[]{Math.pow(Math.E,3)},-1));

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
	 * Test: Wahrscheinlichkeitsverteilungen (Dichte, Verteilung, Zufallszahlen)
	 */
	@Test
	void testDistributions() {
		CalcSystem calc;
		double D;

		/* Betaverteilung */

		calc=new CalcSystem("BetaDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,3,0.5,0.5});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("BetaDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,1,3,0.5,0.5});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("BetaDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{4,1,3,0.5,0.5});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("BetaDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,3,0.5,0.5});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("BetaDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{4,1,3,0.5,0.5});
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc1=new CalcSystem("BetaDist(x;a;b;c;d;2)",new String[]{"x","a","b","c","d"});
		assertTrue(calc1.parse()<0);
		assertThrows(MathCalcError.class,()->calc1.calc(new double[]{-0.1,5}));

		calc=new CalcSystem("BetaDist(a;b;c;d)",new String[]{"a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1,3,0.5,0.5});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Cauchyverteilung */

		calc=new CalcSystem("CauchyDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,1,1});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("CauchyDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,1,1});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("CauchyDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,1,1});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("CauchyDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,1,1});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc2=new CalcSystem("CauchyDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc2.parse()<0);
		assertThrows(MathCalcError.class,()->calc2.calc(new double[]{-0.1,1,1}));

		calc=new CalcSystem("CauchyDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1,1});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Chi²-Verteilung */

		calc=new CalcSystem("ChiSquareDist(x;n;0)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,200});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ChiSquareDist(x;n;0)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{200,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ChiSquareDist(x;n;1)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,200});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ChiSquareDist(x;n;1)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{200,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc3=new CalcSystem("ChiSquareDist(x;n;2)",new String[]{"x","n"});
		assertTrue(calc3.parse()<0);
		assertThrows(MathCalcError.class,()->calc3.calc(new double[]{200,200}));

		calc=new CalcSystem("ChiSquareDist(n)",new String[]{"n"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{200});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Exponentialverteilung */

		calc=new CalcSystem("ExpDist(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,5});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertEquals(0,calc.calcOrDefault(new double[]{-0.1,5},-1));

		calc=new CalcSystem("ExpDist(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,5});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertTrue(calc.calcOrDefault(new double[]{2,5},-1)>0);

		calc=new CalcSystem("ExpDist(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,5});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertEquals(0,calc.calcOrDefault(new double[]{-0.1,5},-1));

		calc=new CalcSystem("ExpDist(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,5});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertTrue(calc.calcOrDefault(new double[]{2,5},-1)>0);

		final CalcSystem calc4=new CalcSystem("ExpDist(x;a;2)",new String[]{"x","a"});
		assertTrue(calc4.parse()<0);
		assertThrows(MathCalcError.class,()->calc4.calc(new double[]{-0.1,5}));
		assertEquals(-1,calc4.calcOrDefault(new double[]{-0.1,5},-1));

		calc=new CalcSystem("ExpDist(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{5});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertTrue(calc.calcOrDefault(new double[] {5},-1)>=0);

		/* F-Verteilung */

		calc=new CalcSystem("FDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,100,10});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,100,10});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,100,10});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,100,10});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc5=new CalcSystem("FDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc5.parse()<0);
		assertThrows(MathCalcError.class,()->calc5.calc(new double[]{-0.1,100,10}));

		calc=new CalcSystem("FDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{100,10});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Gammaverteilung */

		calc=new CalcSystem("GammaDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,100,6});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("GammaDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{610,100,6});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("GammaDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,100,6});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("GammaDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1000,100,6});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc6=new CalcSystem("GammaDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc6.parse()<0);
		assertThrows(MathCalcError.class,()->calc6.calc(new double[]{1000,100,6}));

		calc=new CalcSystem("GammaDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{100,6});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Inverse Gaußverteilung */

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;0)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,900,1800});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;0)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1000,900,1800});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;1)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-0.1,900,1800});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;1)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1000,900,1800});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc7=new CalcSystem("InverseGaussianDist(x;l;mu;2)",new String[]{"x","l","mu"});
		assertTrue(calc7.parse()<0);
		assertThrows(MathCalcError.class,()->calc7.calc(new double[]{1000,900,1800}));

		calc=new CalcSystem("InverseGaussianDist(l;mu)",new String[]{"l","mu"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{900,1800});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Johnson-SU-Verteilung */

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,2,1800,1,180});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,2,1800,1,180});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,2,1800,1,180});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{2,2,1800,1,180});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc8=new CalcSystem("JohnsonSUDist(x;a;b;c;d;2)",new String[]{"x","a","b","c","d"});
		assertTrue(calc8.parse()<0);
		assertThrows(MathCalcError.class,()->calc8.calc(new double[]{2,2,1800,1,180}));

		calc=new CalcSystem("JohnsonSUDist(a;b;c;d)",new String[]{"a","b","c","d"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{2,1800,1,180});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Laplace-Verteilung */

		calc=new CalcSystem("LaplaceDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,720,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LaplaceDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{200,720,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LaplaceDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,720,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LaplaceDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{200,720,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc9=new CalcSystem("LaplaceDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc9.parse()<0);
		assertThrows(MathCalcError.class,()->calc9.calc(new double[]{200,720,360}));

		calc=new CalcSystem("LaplaceDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{720,360});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Logistische Verteilung */

		calc=new CalcSystem("LogisticDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1200,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogisticDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{200,1200,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogisticDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1200,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogisticDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{200,1200,360});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc10=new CalcSystem("LogisticDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc10.parse()<0);
		assertThrows(MathCalcError.class,()->calc10.calc(new double[]{200,1200,360}));

		calc=new CalcSystem("LogisticDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1200,360});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Log-Normalverteilung */

		calc=new CalcSystem("LogNormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-1,600,200});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogNormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,600,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogNormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-1,600,200});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogNormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,600,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc11=new CalcSystem("LogNormalDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc11.parse()<0);
		assertThrows(MathCalcError.class,()->calc11.calc(new double[]{500,600,200}));

		calc=new CalcSystem("LogNormalDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{600,200});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Normalverteilung */

		calc=new CalcSystem("NormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-1,600,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("NormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,600,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("NormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{-1,600,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("NormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,600,200});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc12=new CalcSystem("NormalDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc12.parse()<0);
		assertThrows(MathCalcError.class,()->calc12.calc(new double[]{500,600,200}));

		calc=new CalcSystem("NormalDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{600,200});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Paretoverteilung */

		calc=new CalcSystem("ParetoDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{10,300,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ParetoDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,300,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ParetoDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{10,300,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ParetoDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,300,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc13=new CalcSystem("ParetoDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc13.parse()<0);
		assertThrows(MathCalcError.class,()->calc13.calc(new double[]{500,300,3}));

		calc=new CalcSystem("ParetoDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{300,3});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Dreiecksverteilung */

		calc=new CalcSystem("TriangularDist(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,900,1800,2700});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("TriangularDist(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1000,900,1800,2700});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("TriangularDist(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{3000,900,1800,2700});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("TriangularDist(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,900,1800,2700});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("TriangularDist(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1800,900,1800,2700});
			assertEquals(0.5,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("TriangularDist(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{3000,900,1800,2700});
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc14=new CalcSystem("TriangularDist(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc14.parse()<0);
		assertThrows(MathCalcError.class,()->calc14.calc(new double[]{3000,900,1800,2700}));

		calc=new CalcSystem("TriangularDist(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{900,1800,2700});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Gleichverteilung */

		calc=new CalcSystem("UniformDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,900,2700});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("UniformDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1000,900,2700});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("UniformDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{3000,900,2700});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("UniformDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{500,900,2700});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("UniformDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1800,900,2700});
			assertEquals(0.5,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("UniformDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{3000,900,2700});
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc15=new CalcSystem("UniformDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc15.parse()<0);
		assertThrows(MathCalcError.class,()->calc15.calc(new double[]{3000,900,2700}));

		calc=new CalcSystem("UniformDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{900,2700});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Weibull-Verteilung */

		calc=new CalcSystem("WeibullDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,0.0027,2});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("WeibullDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{50,0.0027,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("WeibullDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,0.0027,2});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("WeibullDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{50,0.0027,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc16=new CalcSystem("WeibullDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc16.parse()<0);
		assertThrows(MathCalcError.class,()->calc16.calc(new double[]{50,0.0027,2}));

		calc=new CalcSystem("WeibullDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{0.0027,2});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Fatigue-Life-Verteilung */

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc17=new CalcSystem("FatigueLifeDistribution(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc17.parse()<0);
		assertThrows(MathCalcError.class,()->calc17.calc(new double[]{100,1,2,3}));

		calc=new CalcSystem("FatigueLifeDistribution(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1,2,3});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Frechet-Verteilung */

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc18=new CalcSystem("FrechetDistribution(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc18.parse()<0);
		assertThrows(MathCalcError.class,()->calc18.calc(new double[]{100,1,2,3}));

		calc=new CalcSystem("FatigueLifeDistribution(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1,2,3});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Gumbel-Verteilung */

		calc=new CalcSystem("GumbelDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("GumbelDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc19=new CalcSystem("GumbelDistribution(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc19.parse()<0);
		assertThrows(MathCalcError.class,()->calc19.calc(new double[]{100,1,2}));

		calc=new CalcSystem("GumbelDistribution(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1,2,3});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Hyperbolische Sekanten-Verteilung */

		calc=new CalcSystem("HyperbolicSecantDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("HyperbolicSecantDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc20=new CalcSystem("HyperbolicSecantDistribution(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc20.parse()<0);
		assertThrows(MathCalcError.class,()->calc20.calc(new double[]{100,1,2}));

		calc=new CalcSystem("HyperbolicSecantDistribution(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1,2,3});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Log-Logistische Verteilung */

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,1,2});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc21=new CalcSystem("LogLogisticDistribution(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc21.parse()<0);
		assertThrows(MathCalcError.class,()->calc21.calc(new double[]{100,1,2}));

		calc=new CalcSystem("LogLogisticDistribution(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1,2});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Potenz-Verteilung */

		calc=new CalcSystem("PowerDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("PowerDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1.5,1,2,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("PowerDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{5,1,2,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("PowerDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,1,2,3});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("PowerDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{1.5,1,2,3});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("PowerDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{5,1,2,3});
			assertEquals(1,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc22=new CalcSystem("PowerDistribution(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc22.parse()<0);
		assertThrows(MathCalcError.class,()->calc22.calc(new double[]{100,1,2,3}));

		calc=new CalcSystem("PowerDistribution(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1,2,3});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Rayleigh Verteilung */

		calc=new CalcSystem("RayleighDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,50});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("RayleighDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,50});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("RayleighDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,50});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("RayleighDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,50});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc23=new CalcSystem("RayleighDistribution(x;a;2)",new String[]{"x","a"});
		assertTrue(calc23.parse()<0);
		assertThrows(MathCalcError.class,()->calc23.calc(new double[]{100,50}));

		calc=new CalcSystem("RayleighDistribution(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Chi-Verteilung */

		calc=new CalcSystem("ChiDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,50});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ChiDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{7,50});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ChiDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{0,50});
			assertEquals(0,D);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("ChiDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		try {
			D=calc.calc(new double[]{100,50});
			assertTrue(D>0);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc24=new CalcSystem("ChiDistribution(x;a;2)",new String[]{"x","a"});
		assertTrue(calc24.parse()<0);
		assertThrows(MathCalcError.class,()->calc24.calc(new double[]{100,50}));

		calc=new CalcSystem("ChiDistribution(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		try {
			calc.calc(new double[]{1});
			/* Keine Interpretation des Zahlenwertes */
		} catch (MathCalcError e) {
			assertTrue(false);
		}
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

		d=-1;
		while (d<=15) {
			calc=new CalcSystem("EmpirischeDichte("+NumberTools.formatNumber(d)+";2;1;3;10)");
			assertTrue(calc.parse()<0);
			try {
				result=calc.calc();
				assertNotNull(result);
				if (d<0) assertEquals(0.0,result,0001);
				if (d>=0 && d<=3.3) assertEquals(2.0/(2+1+3),result,0.0001);
				if (d>=3.4 && d<=6.6) assertEquals(1.0/(2+1+3),result,0.0001);
				if (d>=6.7 && d<=10.0) assertEquals(3.0/(2+1+3),result,0.0001);
				if (d>10) assertEquals(0.0,result,0.0001);
			} catch (MathCalcError e) {
				assertTrue(false);
			}
			d+=0.1;
		}

		/* Verteilungsfunktion */

		d=-1;
		double last=-1;
		while (d<=15) {
			calc=new CalcSystem("EmpirischeVerteilung("+NumberTools.formatNumber(d)+";2;1;3;10)");
			assertTrue(calc.parse()<0);
			try {
				result=calc.calc();
				assertNotNull(result);
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
			d+=0.1;
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
			assertEquals(53.0/23.0,result,0.0001);
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
			assertEquals(Math.sqrt(3.0812854442344038)/(53.0/23.0),result,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}
}