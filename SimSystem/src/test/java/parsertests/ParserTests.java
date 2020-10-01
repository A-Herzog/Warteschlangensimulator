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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import parser.CalcSystem;
import parser.MathCalcError;
import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Prüft die Funktionsweise des Parsers von {@link CalcSystem}
 * @author Alexander Herzog
 * @see CalcSystem
 */
class ParserTests {
	private void constructorTest(final CalcSystem calc, final String text, final String[] variables, final List<Double> values) {
		assertNotNull(calc.getText());
		if (text==null || text.isEmpty()) {
			assertTrue(calc.getText().isEmpty());
		} else {
			assertEquals(calc.getText(),text);
		}

		assertNotNull(calc.variables);
		if (variables==null) {
			assertEquals(0,calc.variables.length);
		} else {
			assertEquals(variables.length,calc.variables.length);
			assertTrue(Objects.deepEquals(calc.variables,variables));
		}

		assertNotNull(calc.values);
		if (variables==null) {
			assertEquals(0,calc.values.length);
		} else {
			assertEquals(variables.length,calc.values.length);
			if (values==null) {
				for (double d: calc.values) assertEquals(0.0,d);
			} else {
				for (int i=0;i<calc.values.length;i++) {
					if (i>=values.size()) {
						assertEquals(0.0,calc.values[i]);
					} else {
						final double d=(values.get(i)==null)?0.0:values.get(i).doubleValue();
						assertEquals(d,calc.values[i]);
					}
				}
			}
		}
	}

	/**
	 * Test: Verschiedene Konstruktor-Varianten
	 */
	@Test
	void constructorTest() {
		final String text="1+2";
		final String textNull=null;
		final String[] variables=new String[] {"a","b","",null};
		final String[] variablesNull=null;
		final List<String> variablesList=Arrays.asList(variables);
		final List<String> variablesListNull=null;
		final List<Double> valuesList=Arrays.asList(1.0,2.0,3.0,null,4.0,5.0);
		final List<Double> valuesListNull=null;

		constructorTest(new CalcSystem(),null,null,null);
		constructorTest(new CalcSystem(textNull),textNull,null,null);
		constructorTest(new CalcSystem(""),"",null,null);
		constructorTest(new CalcSystem(text),text,null,null);

		constructorTest(new CalcSystem(text,variablesNull),text,variablesNull,null);
		constructorTest(new CalcSystem(text,variables),text,variables,null);

		constructorTest(new CalcSystem(variablesNull),null,variablesNull,null);
		constructorTest(new CalcSystem(variables),null,variables,null);

		constructorTest(new CalcSystem(variablesListNull),null,null,null);
		constructorTest(new CalcSystem(variablesList),null,variables,null);

		constructorTest(new CalcSystem(variablesListNull,valuesListNull),null,null,null);
		constructorTest(new CalcSystem(variablesList,valuesListNull),null,variables,null);

		constructorTest(new CalcSystem(variablesListNull,valuesList),null,null,null);
		constructorTest(new CalcSystem(variablesList,valuesList),null,variables,valuesList);

		constructorTest(new CalcSystem(textNull,variablesNull),null,variablesNull,null);
		constructorTest(new CalcSystem(textNull,variables),null,variables,null);

		constructorTest(new CalcSystem(textNull,variablesListNull),null,null,null);
		constructorTest(new CalcSystem(textNull,variablesList),null,variables,null);

		constructorTest(new CalcSystem(textNull,variablesListNull,valuesListNull),null,null,null);
		constructorTest(new CalcSystem(textNull,variablesList,valuesListNull),null,variables,null);

		constructorTest(new CalcSystem(textNull,variablesListNull,valuesList),null,null,null);
		constructorTest(new CalcSystem(textNull,variablesList,valuesList),null,variables,valuesList);

		constructorTest(new CalcSystem(text,variablesNull),text,variablesNull,null);
		constructorTest(new CalcSystem(text,variables),text,variables,null);

		constructorTest(new CalcSystem(text,variablesListNull),text,null,null);
		constructorTest(new CalcSystem(text,variablesList),text,variables,null);

		constructorTest(new CalcSystem(text,variablesListNull,valuesListNull),text,null,null);
		constructorTest(new CalcSystem(text,variablesList,valuesListNull),text,variables,null);

		constructorTest(new CalcSystem(text,variablesListNull,valuesList),text,null,null);
		constructorTest(new CalcSystem(text,variablesList,valuesList),text,variables,valuesList);
	}

	/**
	 * Test: Einfache Rechenausdrücke parsen
	 */
	@Test
	void parserTest() {
		CalcSystem calc;

		calc=new CalcSystem("1+2");
		assertTrue(calc.parse()<0);

		calc=new CalcSystem("1+2+a");
		assertTrue(calc.parse()>=0);

		calc=new CalcSystem();
		assertTrue(calc.parse()>=0);

		calc=new CalcSystem("");
		assertTrue(calc.parse()>=0);

		calc=new CalcSystem();
		assertTrue(calc.parse("1+2")<0);

		calc=new CalcSystem();
		assertTrue(calc.parse("1+2+a")>=0);

		calc=new CalcSystem();
		assertTrue(calc.parse(null)>=0);

		calc=new CalcSystem();
		assertTrue(calc.parse("")>=0);
	}

	/**
	 * Test: Funktionsnamen erkennen
	 */
	@Test
	void helperFunctionsTest() {
		CalcSystem calc;

		calc=new CalcSystem();
		final int count=calc.getAllSymbolNames().length;

		assertEquals(count,calc.getAllSymbolNames().length);

		calc=new CalcSystem(new String[] {"a",null,"b"});
		final String[] list=calc.getAllSymbolNames();
		assertEquals(count+2,list.length);

		for (String sym: list) {
			assertTrue(calc.isKnownSymbol(sym));
			assertTrue(calc.isKnownSymbol(sym.toLowerCase()));
			assertTrue(calc.isKnownSymbol(sym.toUpperCase()));
		}

		assertTrue(!calc.isKnownSymbol(null));
		assertTrue(!calc.isKnownSymbol(""));
		assertTrue(!calc.isKnownSymbol("äöü"));
	}

	/**
	 * Test: Ausdrücke direkt, ohne Objekt berechnen
	 * @see CalcSystem#calcSimple(String)
	 */
	@Test
	void calcSimpleTest() {
		assertNull(CalcSystem.calcSimple(null));
		assertNull(CalcSystem.calcSimple(""));
		assertNull(CalcSystem.calcSimple(" "));

		assertEquals(5.0,CalcSystem.calcSimple("5").doubleValue());
		assertEquals(5.25,CalcSystem.calcSimple("5,25").doubleValue());
		assertEquals(5.75,CalcSystem.calcSimple("5.75").doubleValue());
		assertEquals(3.0,CalcSystem.calcSimple("1+ 2 ").doubleValue());
		assertEquals(13.0,CalcSystem.calcSimple("1+3*4").doubleValue());
		assertEquals(16.0,CalcSystem.calcSimple("(1+3)*4").doubleValue());
		assertEquals(5.0,CalcSystem.calcSimple("sqrt(25)").doubleValue());
		assertNull(CalcSystem.calcSimple("sqrt(-25)"));
	}

	/**
	 * Test: Berechnungen durchführen, Konstanste Ausdrücke erkennen, Funktionsaufrufe, fehlende Multiplikationszeichen ergänzen, ...
	 */
	@Test
	void calcTest() {
		CalcSystem calc;
		double d;

		calc=new CalcSystem("5");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(5.0,calc.getConstValue());
		try {
			d=calc.calc();
			assertEquals(5,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("5,25");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(5.25,calc.getConstValue());
		try {
			d=calc.calc();
			assertEquals(5.25,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("5.75");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(5.75,calc.getConstValue());
		try {
			d=calc.calc();
			assertEquals(5.75,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("sqrt(25)");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(5.0,calc.getConstValue());
		try {
			d=calc.calc();
			assertEquals(5.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		final CalcSystem calc1=new CalcSystem();
		assertThrows(MathCalcError.class,()->calc1.calc());

		calc=new CalcSystem("a+3",new String[]{null,"a",""});
		assertTrue(calc.parse()<0);
		assertTrue(!calc.isConstValue());
		try {
			d=calc.calc(new double[]{1,2,3});
			assertEquals(5.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}

		calc=new CalcSystem("|-7|");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(7.0,calc.getConstValue());

		calc=new CalcSystem("|-7");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(7.0,calc.getConstValue());

		calc=new CalcSystem("sqr(7");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(49.0,calc.getConstValue());

		calc=new CalcSystem("sqr(7))");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(49.0,calc.getConstValue());

		calc=new CalcSystem("sqr(7]");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(49.0,calc.getConstValue());

		calc=new CalcSystem("2sqrt(25)");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(10.0,calc.getConstValue());

		calc=new CalcSystem("2(3+4)");
		assertTrue(calc.parse()<0);
		assertTrue(calc.isConstValue());
		assertEquals(14.0,calc.getConstValue());

		calc=new CalcSystem("min(2;3;4)");
		assertTrue(calc.parse()<0);
		try {
			d=calc.calc();
			assertEquals(2.0,d);
		} catch (MathCalcError e) {
			assertTrue(false);
		}
	}

	/**
	 * Test: Berechnungen mit Fallback-Vorgabewert
	 * @see CalcSystem#calcOrDefault(double[], double)
	 */
	@Test
	void calcOrDefaultTest() {
		CalcSystem calc;

		calc=new CalcSystem();
		assertTrue(calc.parse()>=0);
		assertEquals(-1.0,calc.calcOrDefault(null,-1));

		calc=new CalcSystem("sqrt(25)");
		assertTrue(calc.parse()<0);
		assertEquals(5.0,calc.calcOrDefault(null,-1));

		calc=new CalcSystem("sqrt(-25)");
		assertTrue(calc.parse()<0);
		assertEquals(-1.0,calc.calcOrDefault(null,-1));

		calc=new CalcSystem("a",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(2.0,calc.calcOrDefault(new double[]{2},-1));

		calc=new CalcSystem("a+3",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(5.0,calc.calcOrDefault(new double[]{2},-1));

		calc=new CalcSystem("5+sqrt(-7)");
		assertTrue(calc.parse()<0);
		assertEquals(-1.0,calc.calcOrDefault(null,-1));

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(5,calc.calcOrDefault(new double[]{25},-1));

		calc=new CalcSystem("sqrt(a)",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(-1.0,calc.calcOrDefault(new double[]{-25},-1));

		calc=new CalcSystem("a%",new String[]{"a"});
		assertTrue(calc.parse()<0);
		assertEquals(0.17,calc.calcOrDefault(new double[]{17},-1));
	}

	/**
	 * Test: Benutzerdefinierte Erweiterungsfunktionen
	 * @see CalcSystem#getUserFunctions()
	 */
	@Test
	void userFunctionTest() {
		CalcSystem calc=new CalcSystem() {
			@Override
			protected List<CalcSymbolPreOperator> getUserFunctions() {
				final CalcSymbolPreOperator[] userFunctions=new CalcSymbolPreOperator[]{
						new CalcSymbolPreOperator() {
							@Override
							public String[] getNames() {
								return new String[]{"TestFunction"};
							}
							@Override
							protected double calc(double[] parameters) throws MathCalcError {
								if (parameters.length!=1) throw error();
								return parameters[0]+1;
							}
						}
				};
				return Arrays.asList(userFunctions);
			}
		};

		assertTrue(calc.parse("TestFunction(7)")<0);
		assertTrue(calc.isConstValue());
		assertEquals(8,calc.getConstValue());
	}
}
