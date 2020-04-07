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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import mathtools.SimpleParser;

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

		sp=new SimpleParser(Arrays.asList(new String[]{"a"}));
		assertEquals(0,sp.parse());

		sp=new SimpleParser(Arrays.asList(new String[]{"a"}),Arrays.asList(new Double[]{1.5}));
		assertEquals(0,sp.parse());

		sp=new SimpleParser("1+2",new String[]{"a"});
		assertEquals(-1,sp.parse());

		sp=new SimpleParser("1+2",Arrays.asList(new String[]{"a"}));
		assertEquals(-1,sp.parse());

		sp=new SimpleParser("1+2",Arrays.asList(new String[]{"a"}),Arrays.asList(new Double[]{1.5}));
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
		assertEquals(Double.valueOf(5.4),sp.calc());

		/* Alle möglichen Zeichen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("01,2+3.4+1:2+2^3+5%+2²+2³"));
		assertEquals(Double.valueOf(25.15),sp.calc());

		/* Klammern */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+{2*[3-4/5}]"));
		assertEquals(Double.valueOf(5.4),sp.calc());

		sp=new SimpleParser();
		assertEquals(0,sp.parse("1+}2"));

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+2)"));
		assertEquals(Double.valueOf(3),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+2]"));
		assertEquals(Double.valueOf(3),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1+2}"));
		assertEquals(Double.valueOf(3),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("(1+2"));
		assertEquals(Double.valueOf(3),sp.calc());

		sp=new SimpleParser();
		assertEquals(0,sp.parse("cos(1.2.3)"));

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sgn(1+(2+3))"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sgn(1+{2+3))"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sgn(1+[2+3))"));
		assertEquals(Double.valueOf(1),sp.calc());

		/* Zahlen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1,2"));
		assertEquals(Double.valueOf(1.2),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1.2"));
		assertEquals(Double.valueOf(1.2),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse(",2"));
		assertEquals(Double.valueOf(0.2),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse(".2"));
		assertEquals(Double.valueOf(0.2),sp.calc());

		sp=new SimpleParser();
		assertEquals(0,sp.parse("1,2.3"));

		/* Leerzeichen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("2 3"));
		assertEquals(Double.valueOf(6),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("2 + 3"));
		assertEquals(Double.valueOf(5),sp.calc());

		/* Verknüpfung von Zahlen mit Funktionen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("2cos(0)"));
		assertEquals(Double.valueOf(2),sp.calc());

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
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(0,sp.parse("sin cos(0)"));

		sp=new SimpleParser();
		assertEquals(0,sp.parse("cos ²"));

		sp=new SimpleParser();
		assertEquals(0,sp.parse("cos+3"));

		/* Minuszeichen am Beginn der Zeichenkette (die keine Zahl ist) */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("-cos(0)"));
		assertEquals(Double.valueOf(-1),sp.calc());

		/* Automatisch Multiplikationszeichen einfügen */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("3cos(0)"));
		assertEquals(Double.valueOf(3),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("3pi"));
		assertEquals(3*Math.PI,sp.calcOrDefault(new double[0],17),0.0001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("3 4"));
		assertEquals(Double.valueOf(12),sp.calc());
	}

	/**
	 * Test: Basisfunktionalität (Berechnung einfacher Ausdrücke)
	 */
	@Test
	void testCalc() {
		SimpleParser sp;

		/* Kein Ausdruck */

		sp=new SimpleParser();
		assertEquals(null,sp.calc());
		assertEquals(17,sp.calcOrDefault(new double[0],17),0.0001);

		/* Ausdruck, der nicht plainNumber ist */

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("sqrt(a-(1+2))"));
		assertEquals(Double.valueOf(4),sp.calc(new double[]{19.0}));

		/* Vorgabewert für Fehlerfall */

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("sqrt(a))"));
		assertEquals(null,sp.calc(new double[]{-1.0}));

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
		Double D;

		/* Division durch 0 */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1/0"));
		assertEquals(null,sp.calc());

		/* Negativer Exponent */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1^(-2)"));
		assertEquals(null,sp.calc());

		/* Fakultät */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("10!"));
		assertNotNull(D=sp.calc());
		assertEquals(3628800,D.doubleValue(),0.0001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("1!"));
		assertNotNull(D=sp.calc());
		assertEquals(1,D.doubleValue(),0.0001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("0!"));
		assertNotNull(D=sp.calc());
		assertEquals(1,D.doubleValue(),0.0001);

		/* Nicht berechenbare Teil-Zweige */

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("sqrt(a)+1"));
		assertEquals(null,sp.calc(new double[]{-1}));

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("1+sqrt(a)"));
		assertEquals(null,sp.calc(new double[]{-1}));

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
		assertEquals(Double.valueOf(49),sp.calc());

		/* Wurzel */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sqrt(625)"));
		assertEquals(Double.valueOf(25),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sqrt(-1)"));
		assertEquals(null,sp.calc());

		/* Sinus */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sin(0)"));
		assertEquals(Double.valueOf(0),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sin(pi/2)"));
		assertEquals(Double.valueOf(1),sp.calc());

		/* Tangens */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(tan(0))"));
		assertEquals(Double.valueOf(0),sp.calc());

		/* Cotangens */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(cot(pi/2))"));
		assertEquals(Double.valueOf(0),sp.calc());

		/* Exponentialfunktion */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("exp(0)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("exp(1)-e"));
		assertEquals(Double.valueOf(0),sp.calc());

		/* Natürlicher Logarithmus */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ln(0)"));
		assertEquals(null,sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ln(1)"));
		assertEquals(Double.valueOf(0),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ln(e)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ln(e^2)"));
		assertEquals(Double.valueOf(2),sp.calc());

		/* 10er Logarithmus */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("lg(0)"));
		assertEquals(null,sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("lg(1)"));
		assertEquals(Double.valueOf(0),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("lg(10)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("lg(100)"));
		assertEquals(Double.valueOf(2),sp.calc());

		/* 2er Logarithmus */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ld(0)"));
		assertEquals(null,sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ld(1)"));
		assertEquals(Double.valueOf(0),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ld(2)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ld(4)"));
		assertEquals(Double.valueOf(2),sp.calc());

		/* Betrag */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("abs(0)"));
		assertEquals(Double.valueOf(0),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("abs(5)"));
		assertEquals(Double.valueOf(5),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("abs(-5)"));
		assertEquals(Double.valueOf(5),sp.calc());

		/* Nachkommaanteil */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("frac(123,456)"));
		assertNotNull(D=sp.calc());
		assertEquals(0.456,D.doubleValue(),0.001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("frac(-123,456)"));
		assertNotNull(D=sp.calc());
		assertEquals(-0.456,D.doubleValue(),0.001);

		/* Ganzzahlanteil */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("int(1234,567)"));
		assertEquals(Double.valueOf(1234),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("int(-1234,567)"));
		assertEquals(Double.valueOf(-1234),sp.calc());

		/* Runden */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(1,4)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(1,5)"));
		assertEquals(Double.valueOf(2),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(-1,5)"));
		assertEquals(Double.valueOf(-1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("round(-1,6)"));
		assertEquals(Double.valueOf(-2),sp.calc());

		/* Abrunden */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(1,0)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(1,4)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(1,5)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(-1,0)"));
		assertEquals(Double.valueOf(-1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(-1,5)"));
		assertEquals(Double.valueOf(-2),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("floor(-1,6)"));
		assertEquals(Double.valueOf(-2),sp.calc());

		/* Aufrunden */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(1,0)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(1,4)"));
		assertEquals(Double.valueOf(2),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(1,5)"));
		assertEquals(Double.valueOf(2),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(-1,0)"));
		assertEquals(Double.valueOf(-1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(-1,5)"));
		assertEquals(Double.valueOf(-1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("ceil(-1,6)"));
		assertEquals(Double.valueOf(-1),sp.calc());

		/* Fakultät */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("factorial(10)"));
		assertNotNull(D=sp.calc());
		assertEquals(3628800,D.doubleValue(),0.0001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("factorial(1)"));
		assertNotNull(D=sp.calc());
		assertEquals(1,D.doubleValue(),0.0001);

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("factorial(0)"));
		assertNotNull(D=sp.calc());
		assertEquals(1,D.doubleValue(),0.0001);

		/* Signum */

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sign(5)"));
		assertEquals(Double.valueOf(1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sign(-5)"));
		assertEquals(Double.valueOf(-1),sp.calc());

		sp=new SimpleParser();
		assertEquals(-1,sp.parse("sign(0)"));
		assertEquals(Double.valueOf(0),sp.calc());

		/* Nicht berechenbare Teil-Zweige */

		sp=new SimpleParser(new String[]{"a"});
		assertEquals(-1,sp.parse("cos(sqrt(a))"));
		assertEquals(null,sp.calc(new double[]{-1}));

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
