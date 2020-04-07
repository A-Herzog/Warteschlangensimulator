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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import mathtools.NumberTools;
import parser.CalcSystem;

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
		Double D;

		/* Plus */

		calc=new CalcSystem("1+2");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3.0,D.doubleValue());

		calc=new CalcSystem("1+a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2});
		assertNotNull(D);
		assertEquals(3.0,D.doubleValue());

		calc=new CalcSystem("1+a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3.0,calc.calcOrDefault(new double[]{2},-7));

		/* Minus */

		calc=new CalcSystem("1-2");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1.0,D.doubleValue());

		calc=new CalcSystem("1-a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2});
		assertNotNull(D);
		assertEquals(-1.0,D.doubleValue());

		calc=new CalcSystem("1-a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1.0,calc.calcOrDefault(new double[]{2},-7));

		/* Multiplizieren */

		calc=new CalcSystem("1*2");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2.0,D.doubleValue());

		calc=new CalcSystem("1*a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2});
		assertNotNull(D);
		assertEquals(2.0,D.doubleValue());

		calc=new CalcSystem("1*a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2.0,calc.calcOrDefault(new double[]{2},-7));

		/* Dividieren */

		calc=new CalcSystem("1/2");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.5,D.doubleValue());

		calc=new CalcSystem("1/a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2});
		assertNotNull(D);
		assertEquals(0.5,D.doubleValue());

		calc=new CalcSystem("1/a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0.5,calc.calcOrDefault(new double[]{2},-7));

		/* Potenzieren */

		calc=new CalcSystem("2^3");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(8.0,D.doubleValue());

		calc=new CalcSystem("2^3",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2});
		assertNotNull(D);
		assertEquals(8.0,D.doubleValue());

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
		Double D;

		calc=new CalcSystem("1+2-3");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.0,D.doubleValue());

		calc=new CalcSystem("1+2*3");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(7.0,D.doubleValue());

		calc=new CalcSystem("1+2*3-5");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2.0,D.doubleValue());

		calc=new CalcSystem("2*3^4");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(162.0,D.doubleValue());

		calc=new CalcSystem("1-2*3");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-5.0,D.doubleValue());

		calc=new CalcSystem("1+2/4");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1.5,D.doubleValue());
	}

	/**
	 * Test: Nachgestellte Operatoren
	 */
	@Test
	void testPostOperators() {
		CalcSystem calc;
		Double D;

		/* DEG -> RAD */

		calc=new CalcSystem("180°");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.PI,D.doubleValue());

		calc=new CalcSystem("1+180°");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1+Math.PI,D.doubleValue());

		/* Factorial */

		calc=new CalcSystem("10!");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3628800.0,D.doubleValue());

		/* Power2 */

		calc=new CalcSystem("3*2²");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(12.0,D.doubleValue());

		/* Power3 */

		calc=new CalcSystem("3*2³");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(24.0,D.doubleValue());
	}

	/**
	 * Test: Funktionen, Anfangsbuchstabe A
	 */
	@Test
	void testPreOperatorsA() {
		CalcSystem calc;
		Double D;

		/* Abs */

		calc=new CalcSystem("Abs(1.2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1.2,D.doubleValue());

		calc=new CalcSystem("Abs(-3.4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3.4,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(8.1,D.doubleValue(),0.01);

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1;1;-2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(9,D.doubleValue(),0.01);

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1;1;-3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(9,D.doubleValue(),0.01);

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1;1;-4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(10,D.doubleValue(),0.01);

		calc=new CalcSystem("AllenCunneen(1,8;1;1;1;2;-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(7.674,D.doubleValue(),0.01);

		calc=new CalcSystem("AllenCunneen(1,8;1,25;1;1;2;-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1.55,D.doubleValue(),0.01);

		calc=new CalcSystem("AllenCunneen(0,9;1;1,5;1;1;-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(13.163,D.doubleValue(),0.01);

		calc=new CalcSystem("AllenCunneen(0,9;1;1;1,5;1;-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(13.163,D.doubleValue(),0.01);

		/* ArcCos */

		calc=new CalcSystem("arccos(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.PI,D.doubleValue(),0.0001);

		calc=new CalcSystem("arccos(0)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.PI/2,D.doubleValue(),0.0001);

		calc=new CalcSystem("arccos(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0001);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0001);

		calc=new CalcSystem("arccosh(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0,calc.calcOrDefault(new double[]{1},-7),0.0001);

		/* ArcCot */

		calc=new CalcSystem("arccot(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<0);

		calc=new CalcSystem("arccot(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("arccot(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)<0);


		calc=new CalcSystem("arccot(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},0)>0);

		/* ArcCotH */

		calc=new CalcSystem("arccoth(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<0);

		calc=new CalcSystem("arccoth(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("arccoth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{-1},0)<0);

		calc=new CalcSystem("arccoth(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(new double[]{1},0)>0);

		/* ArcSin */

		calc=new CalcSystem("arcsin(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-Math.PI/2,D.doubleValue(),0.0001);

		calc=new CalcSystem("arcsin(0)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0001);

		calc=new CalcSystem("arcsin(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.PI/2,D.doubleValue(),0.0001);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0001);

		calc=new CalcSystem("arcsinh(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<0);

		calc=new CalcSystem("arcsinh(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0001);

		calc=new CalcSystem("arctan(10000000)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.PI/2,D.doubleValue(),0.01);

		calc=new CalcSystem("arctan(-10000000)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-Math.PI/2,D.doubleValue(),0.01);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0001);

		calc=new CalcSystem("arctanh(-0,5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<0);

		calc=new CalcSystem("arctanh(0,5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

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
		Double D;

		/* Ceil */

		calc=new CalcSystem("ceil(0,4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("ceil(0,6)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("ceil(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("ceil(-1,8)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

		calc=new CalcSystem("ceil(-1,2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

		calc=new CalcSystem("ceil(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("cos(pi/2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.000001);

		calc=new CalcSystem("cos(pi)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

		calc=new CalcSystem("cos(3*pi/2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.000001);

		calc=new CalcSystem("cos(2pi)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("cosh(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>1);

		calc=new CalcSystem("cosh(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>1);

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
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>1);

		calc=new CalcSystem("cot(pi/2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.000001);

		calc=new CalcSystem("cot(pi/2+1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<1);

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
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>1);

		calc=new CalcSystem("coth(10000000)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1.0,D.doubleValue(),0.000001);

		calc=new CalcSystem("coth(-0,1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<-1);

		calc=new CalcSystem("coth(-10000000)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1.0,D.doubleValue(),0.000001);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.527,D.doubleValue(),0.001);

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
		Double D;

		/* ErlangC */

		/* siehe https://www.mathematik.tu-clausthal.de/interaktiv/warteschlangentheorie/erlang-c/ */

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;20)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.8211,D.doubleValue(),0.0001);

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;15;200;20)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.9373,D.doubleValue(),0.0001);

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.58,D.doubleValue(),0.01);

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(10.73,D.doubleValue(),0.01);

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-3");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(9.91,D.doubleValue(),0.01);

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(189.91,D.doubleValue(),0.01);

		calc=new CalcSystem("ErlangC(3,5/60;1/3/60;1/5/60;13;200;-5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1-0.033,D.doubleValue(),0.01);

		/* Exp */

		calc=new CalcSystem("exp(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1/Math.E,D.doubleValue(),0.0001);

		calc=new CalcSystem("exp(0)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue(),0.0001);

		calc=new CalcSystem("exp(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.E,D.doubleValue(),0.0001);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("factorial(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("factorial(2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("factorial(3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(6,D.doubleValue());

		calc=new CalcSystem("factorial(4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(24,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("floor(0,6)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("floor(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("floor(-1,8)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-2,D.doubleValue());

		calc=new CalcSystem("floor(-1,2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-2,D.doubleValue());

		calc=new CalcSystem("floor(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0000001);

		calc=new CalcSystem("frac(5,2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.2,D.doubleValue(),0.0000001);

		calc=new CalcSystem("frac(-5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0000001);

		calc=new CalcSystem("frac(-5,2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-0.2,D.doubleValue(),0.0000001);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("gamma(2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("gamma(3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("gamma(4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(6,D.doubleValue());

		calc=new CalcSystem("gamma(5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(24,D.doubleValue());

		calc=new CalcSystem("gamma(0)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertTrue(D==null);

		calc=new CalcSystem("gamma(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertTrue(D==null);

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
		assertEquals(29,calc.calc(new double[]{-1,-1}));
		assertEquals(23,calc.calc(new double[]{-1,1}));
		assertEquals(17,calc.calc(new double[]{3,1}));
		assertEquals(17,calc.calc(new double[]{3,2}));

		/* Int */

		calc=new CalcSystem("int(5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(5,D.doubleValue(),0.0000001);

		calc=new CalcSystem("int(5,4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(5,D.doubleValue(),0.0000001);

		calc=new CalcSystem("int(5,6)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(5,D.doubleValue(),0.0000001);

		calc=new CalcSystem("int(-5,4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-5,D.doubleValue(),0.0000001);

		calc=new CalcSystem("int(-5,6)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-5,D.doubleValue(),0.0000001);

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
		Double D;

		/* Ld */

		calc=new CalcSystem("ld(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ld(2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("ld(4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("ld(8)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("lg(10)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("lg(100)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("lg(1000)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ln(e)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("ln(e^2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("ln(e^3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("log(2;2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("log(4;2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("log(8;2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("max(1;2;3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3,D.doubleValue());

		calc=new CalcSystem("max(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("max(a;2;3)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(3,calc.calcOrDefault(new double[]{1},-1));

		/* Mean */

		calc=new CalcSystem("mean(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("mean(1;2;3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("mean(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("mean(a;2;3)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[]{1},-1));

		/* Median */

		calc=new CalcSystem("median(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("median(1;3;2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("median(1;7;2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue());

		calc=new CalcSystem("median(1;7;2;8)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(4.5,D.doubleValue());

		calc=new CalcSystem("median(1;7;2)");
		assertTrue(calc.parse()<0);
		assertEquals(2,calc.calcOrDefault(new double[0],-1));

		/* Min */

		calc=new CalcSystem("min(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("min(1;2;3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("min(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		calc=new CalcSystem("min(a;2;3)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(1,calc.calcOrDefault(new double[]{1},-1));

		/* Modulo */

		calc=new CalcSystem("modulo(7;3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("modulo(7,7;0,5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.2,D.doubleValue(),0.0000001);

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
		Double D;

		/* Power */

		calc=new CalcSystem("power(2;3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(8,D.doubleValue());

		calc=new CalcSystem("power(0;0)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("power(0;1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("power(25;1/2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(5,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>=0);

		calc=new CalcSystem("random(5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>=0);

		calc=new CalcSystem("random()");
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(null,-7)>=0);

		calc=new CalcSystem("random(5)");
		assertTrue(calc.parse()<0);
		assertTrue(calc.calcOrDefault(null,-7)>=0);

		/* Round */

		calc=new CalcSystem("round(0,4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("round(0,6)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("round(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("round(-1,8)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-2,D.doubleValue());

		calc=new CalcSystem("round(-1,2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

		calc=new CalcSystem("round(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.277,D.doubleValue(),0.001);

		calc=new CalcSystem("scv(a;2;3;4;5)",new String[]{"a"}); /* E=3, Var=2,5, SCV=Var/E²=277 */
		assertTrue(calc.parse()<0);
		assertEquals(0.277,calc.calcOrDefault(new double[]{1},-7),0.001);

		/* Sign */

		calc=new CalcSystem("sign(3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("sign(0)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("sign(-3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.000001);

		calc=new CalcSystem("sin(pi/2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("sin(pi)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.000001);

		calc=new CalcSystem("sin(3*pi/2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1,D.doubleValue());

		calc=new CalcSystem("sin(2pi)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.000001);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("sinh(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<0);

		calc=new CalcSystem("sinh(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("sqr(3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(9,D.doubleValue());

		calc=new CalcSystem("sqr(-3)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(9,D.doubleValue());

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("sqrt(25)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(5,D.doubleValue());

		calc=new CalcSystem("sqrt(-25)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertTrue(D==null);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.sqrt(2.5),D.doubleValue(),0.001);

		calc=new CalcSystem("sd(a;2;3;4;5)",new String[]{"a"}); /* Var=2,5 */
		assertTrue(calc.parse()<0);
		assertEquals(Math.sqrt(2.5),calc.calcOrDefault(new double[]{1},-7),0.001);

		/* Sum */

		calc=new CalcSystem("sum(1;2;3;4;5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(15,D.doubleValue(),0.001);

		calc=new CalcSystem("sum(a;2;3;4;5)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(15,calc.calcOrDefault(new double[]{1},-7),0.001);

		/* Tan */

		calc=new CalcSystem("tan(-1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<0);

		calc=new CalcSystem("tan(0)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.000001);

		calc=new CalcSystem("tan(1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

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
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0.0,D.doubleValue());

		calc=new CalcSystem("tanh(0,1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("tanh(10000000)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(1.0,D.doubleValue(),0.000001);

		calc=new CalcSystem("tanh(-0,1)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertTrue(D.doubleValue()<0);

		calc=new CalcSystem("tanh(-10000000)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(-1.0,D.doubleValue(),0.000001);

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

		/* Variance */

		calc=new CalcSystem("var(1;2;3;4;5)"); /* Var=2,5 */
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2.5,D.doubleValue(),0.001);

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
		Double D;

		/* Betaverteilung */

		calc=new CalcSystem("BetaDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,3,0.5,0.5});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("BetaDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,1,3,0.5,0.5});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("BetaDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{4,1,3,0.5,0.5});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("BetaDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,3,0.5,0.5});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("BetaDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{4,1,3,0.5,0.5});
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("BetaDist(x;a;b;c;d;2)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,5});
		assertTrue(D==null);

		calc=new CalcSystem("BetaDist(a;b;c;d)",new String[]{"a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,3,0.5,0.5});
		assertNotNull(D);

		/* Cauchyverteilung */

		calc=new CalcSystem("CauchyDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,1,1});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("CauchyDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,1,1});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("CauchyDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,1,1});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("CauchyDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,1,1});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("CauchyDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,1,1});
		assertTrue(D==null);

		calc=new CalcSystem("CauchyDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,1});
		assertNotNull(D);

		/* Chi²-Verteilung */

		calc=new CalcSystem("ChiSquareDist(x;n;0)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,200});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ChiSquareDist(x;n;0)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("ChiSquareDist(x;n;1)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,200});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ChiSquareDist(x;n;1)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("ChiSquareDist(x;n;2)",new String[]{"x","n"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,200});
		assertTrue(D==null);

		calc=new CalcSystem("ChiSquareDist(n)",new String[]{"n"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200});
		assertNotNull(D);

		/* Exponentialverteilung */

		calc=new CalcSystem("ExpDist(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,5});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertEquals(0,calc.calcOrDefault(new double[]{-0.1,5},-1));

		calc=new CalcSystem("ExpDist(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,5});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertTrue(calc.calcOrDefault(new double[]{2,5},-1)>0);

		calc=new CalcSystem("ExpDist(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,5});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertEquals(0,calc.calcOrDefault(new double[]{-0.1,5},-1));

		calc=new CalcSystem("ExpDist(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,5});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertTrue(calc.calcOrDefault(new double[]{2,5},-1)>0);

		calc=new CalcSystem("ExpDist(x;a;2)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,5});
		assertTrue(D==null);
		assertEquals(-1,calc.calcOrDefault(new double[]{-0.1,5},-1));

		calc=new CalcSystem("ExpDist(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{5});
		assertNotNull(D);
		assertEquals(-1,calc.calcOrDefault(new double[0],-1));
		assertTrue(calc.calcOrDefault(new double[] {5},-1)>=0);

		/* F-Verteilung */

		calc=new CalcSystem("FDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,100,10});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("FDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,100,10});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("FDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,100,10});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("FDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,100,10});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("FDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,100,10});
		assertTrue(D==null);

		calc=new CalcSystem("FDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,10});
		assertNotNull(D);

		/* Gammaverteilung */

		calc=new CalcSystem("GammaDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,100,6});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("GammaDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{610,100,6});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("GammaDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,100,6});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("GammaDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1000,100,6});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("GammaDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1000,100,6});
		assertTrue(D==null);

		calc=new CalcSystem("GammaDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,6});
		assertNotNull(D);

		/* Inverse Gaußverteilung */

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;0)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,900,1800});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;0)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1000,900,1800});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;1)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-0.1,900,1800});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;1)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1000,900,1800});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("InverseGaussianDist(x;l;mu;2)",new String[]{"x","l","mu"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1000,900,1800});
		assertTrue(D==null);

		calc=new CalcSystem("InverseGaussianDist(l;mu)",new String[]{"l","mu"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{900,1800});
		assertNotNull(D);

		/* Johnson-SU-Verteilung */

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,2,1800,1,180});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;0)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,2,1800,1,180});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,2,1800,1,180});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;1)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,2,1800,1,180});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("JohnsonSUDist(x;a;b;c;d;2)",new String[]{"x","a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,2,1800,1,180});
		assertTrue(D==null);

		calc=new CalcSystem("JohnsonSUDist(a;b;c;d)",new String[]{"a","b","c","d"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,1800,1,180});
		assertNotNull(D);

		/* Laplace-Verteilung */

		calc=new CalcSystem("LaplaceDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,720,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LaplaceDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,720,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LaplaceDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,720,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LaplaceDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,720,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LaplaceDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,720,360});
		assertTrue(D==null);

		calc=new CalcSystem("LaplaceDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{720,360});
		assertNotNull(D);

		/* Logistische Verteilung */

		calc=new CalcSystem("LogisticDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1200,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogisticDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,1200,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogisticDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1200,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogisticDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,1200,360});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogisticDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{200,1200,360});
		assertTrue(D==null);

		calc=new CalcSystem("LogisticDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1200,360});
		assertNotNull(D);

		/* Log-Normalverteilung */

		calc=new CalcSystem("LogNormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-1,600,200});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("LogNormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,600,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogNormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-1,600,200});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("LogNormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,600,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogNormalDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,600,200});
		assertTrue(D==null);

		calc=new CalcSystem("LogNormalDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{600,200});
		assertNotNull(D);

		/* Normalverteilung */

		calc=new CalcSystem("NormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-1,600,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("NormalDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,600,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("NormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-1,600,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("NormalDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,600,200});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("NormalDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,600,200});
		assertTrue(D==null);

		calc=new CalcSystem("NormalDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{600,200});
		assertNotNull(D);

		/* Paretoverteilung */

		calc=new CalcSystem("ParetoDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{10,300,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ParetoDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,300,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("ParetoDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{10,300,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ParetoDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,300,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("ParetoDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,300,3});
		assertTrue(D==null);

		calc=new CalcSystem("ParetoDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{300,3});
		assertNotNull(D);

		/* Dreiecksverteilung */

		calc=new CalcSystem("TriangularDist(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,900,1800,2700});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("TriangularDist(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1000,900,1800,2700});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("TriangularDist(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3000,900,1800,2700});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("TriangularDist(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,900,1800,2700});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("TriangularDist(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1800,900,1800,2700});
		assertNotNull(D);
		assertEquals(0.5,D.doubleValue());

		calc=new CalcSystem("TriangularDist(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3000,900,1800,2700});
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("TriangularDist(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3000,900,1800,2700});
		assertTrue(D==null);

		calc=new CalcSystem("TriangularDist(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{900,1800,2700});
		assertNotNull(D);

		/* Gleichverteilung */

		calc=new CalcSystem("UniformDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,900,2700});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("UniformDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1000,900,2700});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("UniformDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3000,900,2700});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("UniformDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{500,900,2700});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("UniformDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1800,900,2700});
		assertNotNull(D);
		assertEquals(0.5,D.doubleValue());

		calc=new CalcSystem("UniformDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3000,900,2700});
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("UniformDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3000,900,2700});
		assertTrue(D==null);

		calc=new CalcSystem("UniformDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{900,2700});
		assertNotNull(D);

		/* Weibull-Verteilung */

		calc=new CalcSystem("WeibullDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,0.0027,2});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("WeibullDist(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{50,0.0027,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("WeibullDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,0.0027,2});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("WeibullDist(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{50,0.0027,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("WeibullDist(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{50,0.0027,2});
		assertTrue(D==null);

		calc=new CalcSystem("WeibullDist(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0.0027,2});
		assertNotNull(D);

		/* Fatigue-Life-Verteilung */

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("FatigueLifeDistribution(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2,3});
		assertTrue(D==null);

		calc=new CalcSystem("FatigueLifeDistribution(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,2,3});
		assertNotNull(D);

		/* Frechet-Verteilung */

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("FrechetDistribution(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2,3});
		assertTrue(D==null);

		calc=new CalcSystem("FatigueLifeDistribution(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,2,3});
		assertNotNull(D);

		/* Gumbel-Verteilung */

		calc=new CalcSystem("GumbelDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("GumbelDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("GumbelDistribution(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertTrue(D==null);

		calc=new CalcSystem("GumbelDistribution(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,2,3});
		assertNotNull(D);

		/* Hyperbolische Sekanten-Verteilung */

		calc=new CalcSystem("HyperbolicSecantDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("HyperbolicSecantDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("HyperbolicSecantDistribution(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertTrue(D==null);

		calc=new CalcSystem("HyperbolicSecantDistribution(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,2,3});
		assertNotNull(D);

		/* Log-Logistische Verteilung */

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;0)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;1)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("LogLogisticDistribution(x;a;b;2)",new String[]{"x","a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2});
		assertTrue(D==null);

		calc=new CalcSystem("LogLogisticDistribution(a;b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,2});
		assertNotNull(D);

		/* Potenz-Verteilung */

		calc=new CalcSystem("PowerDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("PowerDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1.5,1,2,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("PowerDistribution(x;a;b;c;0)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{5,1,2,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("PowerDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,1,2,3});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("PowerDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1.5,1,2,3});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("PowerDistribution(x;a;b;c;1)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{5,1,2,3});
		assertNotNull(D);
		assertEquals(1,D.doubleValue());

		calc=new CalcSystem("PowerDistribution(x;a;b;c;2)",new String[]{"x","a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,1,2,3});
		assertTrue(D==null);

		calc=new CalcSystem("PowerDistribution(a;b;c)",new String[]{"a","b","c"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1,2,3});
		assertNotNull(D);

		/* Rayleigh Verteilung */

		calc=new CalcSystem("RayleighDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,50});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("RayleighDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,50});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("RayleighDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,50});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("RayleighDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,50});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("RayleighDistribution(x;a;2)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,50});
		assertTrue(D==null);

		calc=new CalcSystem("RayleighDistribution(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1});
		assertNotNull(D);

		/* Chi-Verteilung */

		calc=new CalcSystem("ChiDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,50});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ChiDistribution(x;a;0)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{7,50});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("ChiDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0,50});
		assertNotNull(D);
		assertEquals(0,D.doubleValue());

		calc=new CalcSystem("ChiDistribution(x;a;1)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,50});
		assertNotNull(D);
		assertTrue(D.doubleValue()>0);

		calc=new CalcSystem("ChiDistribution(x;a;2)",new String[]{"x","a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{100,50});
		assertTrue(D==null);

		calc=new CalcSystem("ChiDistribution(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{1});
		assertNotNull(D);
	}

	/**
	 * Test: Empirische Verteilungsfunktion
	 */
	@Test
	void testEmpiricalDistribution() {
		CalcSystem calc;
		Double D;
		double d;

		/* Dichte */

		d=-1;
		while (d<=15) {
			calc=new CalcSystem("EmpirischeDichte("+NumberTools.formatNumber(d)+";2;1;3;10)");
			assertTrue(calc.parse()<0);
			D=calc.calc();
			assertNotNull(D);
			if (d<0) assertEquals(0.0,D.doubleValue(),0001);
			if (d>=0 && d<=3.3) assertEquals(2.0/(2+1+3),D.doubleValue(),0.0001);
			if (d>=3.4 && d<=6.6) assertEquals(1.0/(2+1+3),D.doubleValue(),0.0001);
			if (d>=6.7 && d<=10.0) assertEquals(3.0/(2+1+3),D.doubleValue(),0.0001);
			if (d>10) assertEquals(0.0,D.doubleValue(),0.0001);
			d+=0.1;
		}

		/* Verteilungsfunktion */

		d=-1;
		double last=-1;
		while (d<=15) {
			calc=new CalcSystem("EmpirischeVerteilung("+NumberTools.formatNumber(d)+";2;1;3;10)");
			assertTrue(calc.parse()<0);
			D=calc.calc();
			assertNotNull(D);
			if (d<=0) assertEquals(0.0,D.doubleValue(),0001);
			if (d>0 && d<10-0.001) {
				assertTrue(D.doubleValue()>0 && D.doubleValue()<1);
				assertTrue(D.doubleValue()>last);
				last=D.doubleValue();
			}
			if (Math.abs(d-3.3)<0.0001) assertEquals(2.0/(2+1+3),D.doubleValue(),0.1);
			if (Math.abs(d-6.6)<0.0001) assertEquals(3.0/(2+1+3),D.doubleValue(),0.1);
			if (d>10) assertEquals(1.0,D.doubleValue(),0.0001);
			d+=0.1;
		}

		/* Zufallszahlen */

		calc=new CalcSystem("EmpirischeZufallszahl(2;1;3;10");
		assertTrue(calc.parse()<0);
		for (int i=0;i<1_000;i++) {
			D=calc.calc();
			assertNotNull(D);
			assertTrue(D.doubleValue()>=0);
			assertTrue(D.doubleValue()<10);
		}

		/* Mittelwert */

		calc=new CalcSystem("EmpirischeVerteilungMittelwert(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(53.0/23.0,D.doubleValue(),0.0001);

		/* Median */

		calc=new CalcSystem("EmpirischeVerteilungMedian(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3,D.doubleValue(),0.0001);

		/* Quantile */

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,2)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(0,D.doubleValue(),0.0001);

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(2,D.doubleValue(),0.0001);

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,6)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(4,D.doubleValue(),0.0001);

		calc=new CalcSystem("EmpirischeVerteilungQuantil(7;2;1;3;10;5;0,8)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(4,D.doubleValue(),0.0001);

		/* Standardabweichung */

		calc=new CalcSystem("EmpirischeVerteilungStandardabweichung(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.sqrt(3.0812854442344038),D.doubleValue(),0.0001);

		/* Varianz */

		calc=new CalcSystem("EmpirischeVerteilungVarianz(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3.0812854442344038,D.doubleValue(),0.0001);

		/* Variationskoeffizient */

		calc=new CalcSystem("EmpirischeVerteilungCV(7;2;1;3;10;5)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(Math.sqrt(3.0812854442344038)/(53.0/23.0),D.doubleValue(),0.0001);
	}
}