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

import parser.CalcSystem;

/**
 * Prüft die Funktionsweise der Basissymbole von {@link CalcSystem} (Addition, Subtraktion, ...)
 * @author Alexander Herzog
 * @see CalcSystem
 */
public class CoreSymbolTests {
	/**
	 * Test: Basis-Rechenoperationen
	 */
	@Test
	void symbolTest() {
		CalcSystem calc;
		Double D;

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3});
		assertNotNull(D);
		assertEquals(4.0,D.doubleValue());

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{10_000});
		assertNotNull(D);
		assertEquals(10_001.0,D.doubleValue());

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-3});
		assertNotNull(D);
		assertEquals(-2.0,D.doubleValue());

		calc=new CalcSystem("a+1",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-10_000});
		assertNotNull(D);
		assertEquals(-9_999.0,D.doubleValue());

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{36});
		assertNotNull(D);
		assertEquals(6.0,D.doubleValue());

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{30.25});
		assertNotNull(D);
		assertEquals(5.5,D.doubleValue());

		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{3.2});
		assertNotNull(D);
		assertEquals(3.0,D.doubleValue());


		calc=new CalcSystem("round(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{-3.2});
		assertNotNull(D);
		assertEquals(-3.0,D.doubleValue());
	}

	/**
	 * Test: Umgang mit den Konstanten pi und e
	 */
	@Test
	void constTest() {
		CalcSystem calc;
		Double D;

		calc=new CalcSystem("a+Pi",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0});
		assertNotNull(D);
		assertEquals(Math.PI,D.doubleValue());

		calc=new CalcSystem("a+e",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{0});
		assertNotNull(D);
		assertEquals(Math.E,D.doubleValue());
	}

	/**
	 * Test: Zweistellige Operationen
	 */
	@Test
	void middleOperatorTest() {
		CalcSystem calc;
		Double D;

		calc=new CalcSystem("a+b",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,3});
		assertNotNull(D);
		assertEquals(5.0,D.doubleValue());

		calc=new CalcSystem("sqr(a)+sqr(b)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2,3});
		assertNotNull(D);
		assertEquals(13.0,D.doubleValue());

		calc=new CalcSystem("sqr(a)+7",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2});
		assertNotNull(D);
		assertEquals(11.0,D.doubleValue());

		calc=new CalcSystem("7+sqr(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{2});
		assertNotNull(D);
		assertEquals(11.0,D.doubleValue());

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
		Double D;

		calc=new CalcSystem("min(1+2;3+4)");
		assertTrue(calc.parse()<0);
		D=calc.calc();
		assertNotNull(D);
		assertEquals(3.0,D.doubleValue());

		calc=new CalcSystem("min(a+1;b+2)",new String[]{"a","b"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{7,3});
		assertNotNull(D);
		assertEquals(5.0,D.doubleValue());

		calc=new CalcSystem("min(a+1)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		D=calc.calc(new double[]{5});
		assertNotNull(D);
		assertEquals(6.0,D.doubleValue());
	}
}
