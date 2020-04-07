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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Test;

import mathtools.ErlangC;

/**
 * Prüft die Funktionsweise von {@link ErlangC}
 * @author Alexander Herzog
 * @see ErlangC
 */
class ErlangCTest {
	/**
	 * Test: Konstruktor ist privat? - Klasse stellt nur statische Methoden zur Verfügung und soll nicht initialisierbar sein
	 * @throws NoSuchMethodException	Konstruktor konnte nicht gefunden werden
	 * @throws IllegalAccessException	Zugriff verweigert
	 * @throws InvocationTargetException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 * @throws InstantiationException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 */
	@Test
	void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		final Constructor<ErlangC> constructor=ErlangC.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * Test: Berechnung der Wartezeiten
	 */
	@Test
	void waitingTime() {
		/* lambda=3,5 (1/min), 1/mu=3 (min), c=13 */
		assertEquals(26.56,ErlangC.waitingTime(3.5/60,1.0/3/60,13),0.01);

		assertEquals(Double.POSITIVE_INFINITY,ErlangC.waitingTime(3.5/60,0,13),0.01);

		/* Zahlenwerte siehe https://www.mathematik.tu-clausthal.de/interaktiv/warteschlangentheorie/erlang-c/ */
	}

	/**
	 * Test: Berechnung der Cn-Werte
	 */
	@Test
	void ErlangCCn() {
		double[] cn;

		cn=ErlangC.extErlangCCn(10,5,0.1,3,100);
		assertNotNull(cn);
		assertEquals(101,cn.length);
		assertEquals(1.0,cn[0]);
		assertEquals(2.0,cn[1]);
		assertEquals(2.0,cn[2]);
		assertEquals(4.0/3.0,cn[3]);

		cn=ErlangC.extErlangCCn(0,0,0.1,3,100);
		assertNotNull(cn);
		assertEquals(101,cn.length);
		assertEquals(1.0,cn[0]);
		for (int i=1;i<cn.length;i++) assertEquals(0,cn[i]);

		cn=ErlangC.extErlangCCn(1,0,0.1,3,100);
		assertNotNull(cn);
		assertEquals(101,cn.length);
		assertEquals(1.0,cn[0]);
		for (int i=1;i<cn.length;i++) assertEquals(0,cn[i]);

		cn=ErlangC.extErlangCCn(0,1,0.1,3,100);
		assertNotNull(cn);
		assertEquals(101,cn.length);
		assertEquals(1.0,cn[0]);
		for (int i=1;i<cn.length;i++) assertEquals(0,cn[i]);
	}

	/**
	 * Berechnung der Wartezeiten bei Verwendung von Wartezeittoleranzen
	 */
	@Test
	void waitingTimeExt() {
		/* lambda=3,5 (1/min), 1/mu=3 (min), c=13, 1/v=5 (min), K=100 Leitungen */
		assertEquals(9.91,ErlangC.waitingTimeExt(3.5/60,1.0/3/60,1.0/5/60,13,100),0.01);

		/* lambda=3,5 (1/min), 1/mu=3 (min), c=13, 1/v=5 (min), K=infty Leitungen */
		assertEquals(9.91,ErlangC.waitingTimeExt(3.5/60,1.0/3/60,1.0/5/60,13,Integer.MAX_VALUE),0.01);

		/* lambda=3,5 (1/min), 1/mu=3 (min), c=9, 1/v=5 (min), K=100 Leitungen */
		assertEquals(9.91,ErlangC.waitingTimeExt(3.5/60,1.0/3/60,1.0/5/60,9,100),56.62);

		/* lambda=3,5 (1/min), 1/mu=3 (min), c=9, 1/v=5 (min), K=infty Leitungen */
		assertEquals(9.91,ErlangC.waitingTimeExt(3.5/60,1.0/3/60,1.0/5/60,9,Integer.MAX_VALUE),56.62);

		/* lambda=0 */
		assertEquals(0,ErlangC.waitingTimeExt(0,0,1.0/5/60,9,Integer.MAX_VALUE),56.62);

		/* Zahlenwerte siehe https://www.mathematik.tu-clausthal.de/interaktiv/warteschlangentheorie/erlang-c/ */
	}
}
