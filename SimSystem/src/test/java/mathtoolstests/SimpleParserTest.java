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
package mathtoolstests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import mathtools.SimpleParser;
import parser.MathCalcError;

/**
 * Prüft die Funktionsweise von {@link SimpleParser}
 * @author Alexander Herzog
 * @see SimpleParser
 */
class SimpleParserTest {
	/**
	 * Test: Verschiedene Konstruktor-Varianten
	 */
	@Test
	void testConstructor() {
		SimpleParser sp;

		/* Leer -> Fehler */

		sp=new SimpleParser();
		assertEquals(0,sp.parse());

		/* Aufruf Parser */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+2"));

		sp=new SimpleParser("1+2");
		assertEquals(-1,sp.parse());

		/* Variablen */

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(0,sp.parse());

		sp=new SimpleParser(Arrays.asList("a"));
		assertEquals(0,sp.parse());

		sp=new SimpleParser(Arrays.asList("a"),Arrays.asList(1.5));
		assertEquals(0,sp.parse());

		sp=new SimpleParser("1+2",new String[]{"a"});
		assertEquals(-1,sp.parse());

		sp=new SimpleParser("1+2",Arrays.asList("a"));
		assertEquals(-1,sp.parse());

		sp=new SimpleParser("1+2",Arrays.asList("a"),Arrays.asList(1.5));
		assertEquals(-1,sp.parse());
	}

	/**
	 * Test: Interpretation von Ausdrücken durch den Parser
	 */
	@Test
	void testParser() {
		SimpleParser sp;

		/* Leerer Ausdruck */

		sp=new SimpleParser();
		assertEquals(0,sp.parse(null));

		sp=new SimpleParser();
		assertEquals(0,sp.parse(""));

		/* Einfacher Ausdruck */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+(2*(3-4/5))"));
		try {
			assertEquals(5.4,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Alle möglichen Zeichen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("01,2+3.4+1:2+2^3+5%+2²+2³"));
		try {
			assertEquals(25.15,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Klammern */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+{2*[3-4/5}]"));
		try {
			assertEquals(5.4,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(0,sp.parse("1+}2"));

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+2)"));
		try {
			assertEquals(3,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+2]"));
		try {
			assertEquals(3,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+2}"));
		try {
			assertEquals(3,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("(1+2"));
		try {
			assertEquals(3,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(0,sp.parse("cos(1.2.3)"));

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sgn(1+(2+3))"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sgn(1+{2+3))"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sgn(1+[2+3))"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Zahlen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1,2"));
		try {
			assertEquals(1.2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1.2"));
		try {
			assertEquals(1.2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse(",2"));
		try {
			assertEquals(0.2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse(".2"));
		try {
			assertEquals(0.2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(0,sp.parse("1,2.3"));

		/* Leerzeichen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("2 3"));
		try {
			assertEquals(6,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("2 + 3"));
		try {
			assertEquals(5,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Verknüpfung von Zahlen mit Funktionen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("2cos(0)"));
		try {
			assertEquals(2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(0,sp.parse("2.3.4cos(0)"));

		/* Variablen */

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("a+1"));

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(0,sp.parse("b+1"));

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("pi+1"));

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("e+1"));

		/* Unbekannte Symbole und merkwürdige Verknüpfungen */

		sp=new SimpleParser();
		assertEquals(0,sp.parse("cos(äöü)"));

		sp=new SimpleParser();
		assertEquals(0,sp.parse("1+-2"));

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("cos(0)²"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(0,sp.parse("sin cos(0)"));

		sp=new SimpleParser();
		assertEquals(0,sp.parse("cos ²"));

		sp=new SimpleParser();
		assertEquals(0,sp.parse("cos+3"));

		/* Minuszeichen am Beginn der Zeichenkette (die keine Zahl ist) */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("-cos(0)"));
		try {
			assertEquals(-1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Automatisch Multiplikationszeichen einfügen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("3cos(0)"));
		try {
			assertEquals(3,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("3pi"));
		assertEquals(3*Math.PI,sp.calcOrDefault(new double[0],17),0.0001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("3 4"));
		try {
			assertEquals(12,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}

	/**
	 * Test: Basisfunktionalität (Berechnung einfacher Ausdrücke)
	 */
	@Test
	void testCalc() {
		SimpleParser sp;

		/* Kein Ausdruck */

		final SimpleParser sp1=new SimpleParser();
		assertThrows(MathCalcError.class,()->sp1.calc());
		assertEquals(17,sp1.calcOrDefault(new double[0],17),0.0001);

		/* Ausdruck, der nicht plainNumber ist */

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("sqrt(a-(1+2))"));
		try {
			assertEquals(4,sp.calc(new double[]{19.0}));
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Vorgabewert für Fehlerfall */

		final SimpleParser sp2=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp2.parse("sqrt(a))"));
		assertThrows(MathCalcError.class,()->sp2.calc(new double[]{-1.0}));

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("sqrt(a))"));
		assertEquals(17,sp.calcOrDefault(new double[]{-1.0},17),0.0001);
		assertEquals(5,sp.calcOrDefault(new double[]{25},17),0.0001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("123"));
		assertEquals(123,sp.calcOrDefault(new double[0],17),0.0001);
	}

	/**
	 * Test: Abfrage und Verwendung der Konstanten für pi und e
	 */
	@Test
	void testConsts() {
		SimpleParser sp;

		/* Pi */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("pi"));
		assertEquals(Math.PI,sp.calcOrDefault(new double[0],17),0.0001);

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("pi+a"));
		assertEquals(Math.PI+1,sp.calcOrDefault(new double[]{1.0},17),0.0001);

		/* e */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("e"));
		assertEquals(Math.E,sp.calcOrDefault(new double[0],17),0.0001);

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("e+a"));
		assertEquals(Math.E+1,sp.calcOrDefault(new double[]{1.0},17),0.0001);
	}

	/**
	 * Test: Verwendung von Operatoren (zweistellige und einstellige)
	 */
	@Test
	void testOperators() {
		SimpleParser sp;
		double D;

		/* Division durch 0 */

		final SimpleParser sp1=new SimpleParser();
		assertEquals(-1,sp1.parse("1/0"));
		assertThrows(MathCalcError.class,()->sp1.calc());

		/* Negativer Exponent */

		final SimpleParser sp2=new SimpleParser();
		assertEquals(-1,sp2.parse("1^(-2)"));
		assertThrows(MathCalcError.class,()->sp2.calc());

		/* Fakultät */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("10!"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(3628800,D,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1!"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(1,D,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("0!"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(1,D,0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Nicht berechenbare Teil-Zweige */

		final SimpleParser sp3=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp3.parse("sqrt(a)+1"));
		assertThrows(MathCalcError.class,()->sp3.calc(new double[]{-1}));

		final SimpleParser sp4=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp4.parse("1+sqrt(a)"));
		assertThrows(MathCalcError.class,()->sp4.calc(new double[]{-1}));

	}

	/**
	 * Test: Berechnung der Werte von Funktionen
	 */
	@Test
	void testFunctions() {
		SimpleParser sp;
		Double D;

		/* Quadrat */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sqr(7)"));
		try {
			assertEquals(49,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Wurzel */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sqrt(625)"));
		try {
			assertEquals(25,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final SimpleParser sp1=new SimpleParser();
		assertEquals(-1,sp1.parse("sqrt(-1)"));
		assertThrows(MathCalcError.class,()->sp1.calc());

		/* Sinus */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sin(0)"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sin(pi/2)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Tangens */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(tan(0))"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e1) {
			assertTrue(false);
		}

		/* Cotangens */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(cot(pi/2))"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Exponentialfunktion */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("exp(0)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e1) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("exp(1)-e"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Natürlicher Logarithmus */

		final SimpleParser sp2=new SimpleParser();
		assertEquals(-1,sp2.parse("ln(0)"));
		assertThrows(MathCalcError.class,()->sp2.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ln(1)"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e2) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ln(e)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e1) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ln(e^2)"));
		try {
			assertEquals(2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* 10er Logarithmus */

		final SimpleParser sp3=new SimpleParser();
		assertEquals(-1,sp3.parse("lg(0)"));
		assertThrows(MathCalcError.class,()->sp3.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("lg(1)"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e2) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("lg(10)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e1) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("lg(100)"));
		try {
			assertEquals(2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* 2er Logarithmus */

		final SimpleParser sp4=new SimpleParser();
		assertEquals(-1,sp4.parse("ld(0)"));
		assertThrows(MathCalcError.class,()->sp4.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ld(1)"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e2) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ld(2)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e1) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ld(4)"));
		try {
			assertEquals(2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Betrag */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("abs(0)"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e2) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("abs(5)"));
		try {
			assertEquals(5,sp.calc());
		} catch (MathCalcError e1) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("abs(-5)"));
		try {
			assertEquals(5,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Nachkommaanteil */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("frac(123,456)"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(0.456,D.doubleValue(),0.001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("frac(-123,456)"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(-0.456,D.doubleValue(),0.001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Ganzzahlanteil */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("int(1234,567)"));
		try {
			assertEquals(1234,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("int(-1234,567)"));
		try {
			assertEquals(-1234,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Runden */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(1,4)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(1,5)"));
		try {
			assertEquals(2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(-1,5)"));
		try {
			assertEquals(-1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(-1,6)"));
		try {
			assertEquals(-2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Abrunden */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(1,0)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(1,4)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(1,5)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(-1,0)"));
		try {
			assertEquals(-1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(-1,5)"));
		try {
			assertEquals(-2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(-1,6)"));
		try {
			assertEquals(-2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Aufrunden */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(1,0)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(1,4)"));
		try {
			assertEquals(2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(1,5)"));
		try {
			assertEquals(2,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(-1,0)"));
		try {
			assertEquals(-1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(-1,5)"));
		try {
			assertEquals(-1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(-1,6)"));
		try {
			assertEquals(-1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Fakultät */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("factorial(10)"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(3628800,D.doubleValue(),0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("factorial(1)"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(1,D.doubleValue(),0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("factorial(0)"));
		try {
			assertNotNull(D=sp.calc());
			assertEquals(1,D.doubleValue(),0.0001);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Signum */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sign(5)"));
		try {
			assertEquals(1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sign(-5)"));
		try {
			assertEquals(-1,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sign(0)"));
		try {
			assertEquals(0,sp.calc());
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		/* Nicht berechenbare Teil-Zweige */

		final SimpleParser sp5=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp5.parse("cos(sqrt(a))"));
		assertThrows(MathCalcError.class,()->sp5.calc(new double[]{-1}));

		/* Unbekannte Funktionen */

		sp=new SimpleParser(new String[]{"a"});
		assertTrue(sp.parse("unknown(a)")>=0);
	}

	/**
	 * Test: Vereinfachung beim Parsen zu einfachem Wert
	 * @see SimpleParser#isConstValue()
	 * @see SimpleParser#getConstValue()
	 */
	@Test
	void testConstValue() {
		SimpleParser sp;

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("sqrt(25)"));
		assertTrue(sp.isConstValue());
		assertEquals(5,sp.getConstValue(),0.0001);

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("sqrt(a)"));
		assertTrue(!sp.isConstValue());
	}

	/**
	 * Test: Direkte Berechnung ohne Objekt
	 * @see SimpleParser#calcSimple(String)
	 */
	@Test
	void testSimpleCalc() {
		assertEquals(null,SimpleParser.calcSimple("äöü"));
		assertEquals(Double.valueOf(5.4),SimpleParser.calcSimple("1+(2*(3-4/5))"));
	}
}
