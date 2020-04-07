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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import mathtools.Functions;

/**
 * Prüft die Funktionsweise von {@link Functions}
 * @author Alexander Herzog
 * @see Functions
 */
class FunctionsTest {
	/**
	 * Test: Konstruktor ist privat? - Klasse stellt nur statische Methoden zur Verfügung und soll nicht initialisierbar sein
	 * @throws NoSuchMethodException	Konstruktor konnte nicht gefunden werden
	 * @throws IllegalAccessException	Zugriff verweigert
	 * @throws InvocationTargetException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 * @throws InstantiationException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 */
	@Test
	void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		final Constructor<Functions> constructor=Functions.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * Test: Unvollständige Gamma-Funktion
	 * @see Functions#getIncompleteGamma(double, double)
	 */
	@Test
	void getIncompleteGamma() {
		final double gamma=Functions.getIncompleteGamma(10,0);
		final double gamma5=Functions.getIncompleteGamma(10,5);

		/* Gamma(x,0)=Gamma(x)=(x-1)!, 9!=362880 */
		assertEquals(362880,gamma,0.0001);

		/* Gamma(x,a)>Gamma(x,b) wenn a<b */
		assertTrue(gamma>gamma5);

		/* Gamma(1,z)=exp(-z) */
		assertEquals(Functions.getIncompleteGamma(1,7),Math.exp(-7),0.0001);
	}

	/**
	 * Test: Gamma-Funktion
	 * @see Functions#getGamma(double)
	 */
	@Test
	void getGamma() {
		/* Gamma(x)=(x-1)!, 9!=362880 */
		assertEquals(362880,Functions.getGamma(10),0.0001);

		/* Gamma(x)=(x-1)!, 1!=1 */
		assertEquals(1,Functions.getGamma(2),0.0001);

		/* Gamma(x)=(x-1)!, 0!=1 */
		assertEquals(1,Functions.getGamma(1),0.0001);
	}

	/**
	 * Test: Fakultät
	 * @see Functions#getFactorial(int)
	 */
	@Test
	void getFactorial() {
		/* 10!=362880 */
		assertEquals(3628800,Functions.getFactorial(10),0.0001);

		/* 1!=1 */
		assertEquals(1,Functions.getFactorial(1),0.0001);

		/* 0!=1 */
		assertEquals(1,Functions.getFactorial(0),0.0001);
	}
}
