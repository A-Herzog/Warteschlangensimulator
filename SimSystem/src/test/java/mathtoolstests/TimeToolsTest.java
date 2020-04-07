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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.SystemColor;
import java.awt.TextField;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;

import javax.swing.JTextField;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mathtools.NumberTools;
import mathtools.TimeTools;

/**
 * Prüft die Funktionsweise von {@link TimeTools}
 * @author Alexander Herzog
 * @see TimeTools
 */
class TimeToolsTest {

	/**
	 * Einstellung der Sprache für {@link NumberTools} um
	 * unabhängig vom System immer vergleichbare Ergebnisse
	 * zu erhalten.
	 */
	@BeforeAll
	static void init() {
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Konstruktor ist privat? - Klasse stellt nur statische Methoden zur Verfügung und soll nicht initialisierbar sein
	 * @throws NoSuchMethodException	Konstruktor konnte nicht gefunden werden
	 * @throws IllegalAccessException	Zugriff verweigert
	 * @throws InvocationTargetException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 * @throws InstantiationException	Aufruf des Konstruktor möglich, wenn per Reflection angepasst?
	 */
	@Test
	void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		final Constructor<TimeTools> constructor=TimeTools.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * Test: Formatiert einen Zahlenwert als HH:MM:SS-Zeitangabe
	 * @see TimeTools#formatTime(long)
	 */
	@Test
	void formatTime() {
		assertEquals("00:00:00",TimeTools.formatTime(0));
		assertEquals("00:00:05",TimeTools.formatTime(5));
		assertEquals("00:00:15",TimeTools.formatTime(15));
		assertEquals("00:00:59",TimeTools.formatTime(59));
		assertEquals("00:01:00",TimeTools.formatTime(60));
		assertEquals("00:01:05",TimeTools.formatTime(65));
		assertEquals("00:01:15",TimeTools.formatTime(75));
		assertEquals("00:15:00",TimeTools.formatTime(900));
		assertEquals("00:59:59",TimeTools.formatTime(3599));
		assertEquals("01:00:00",TimeTools.formatTime(3600));
		assertEquals("05:00:00",TimeTools.formatTime(3600*5));
		assertEquals("15:00:00",TimeTools.formatTime(3600*15));
		assertEquals("30:00:00",TimeTools.formatTime(3600*30));
		assertEquals("-00:00:05",TimeTools.formatTime(-5));
	}

	/**
	 * Test: Formatiert einen Zahlenwert als HH:MM:SS-Zeitangabe (mit optionaler Abtrennung von Tagen, also D:HH:MM:SS)
	 * @see TimeTools#formatLongTime(long)
	 * @see TimeTools#formatLongTime(double)
	 */
	@Test
	void formatLongTime() {
		assertEquals("00:00:00",TimeTools.formatLongTime(0));
		assertEquals("00:00:05",TimeTools.formatLongTime(5));
		assertEquals("00:00:15",TimeTools.formatLongTime(15));
		assertEquals("00:00:59",TimeTools.formatLongTime(59));
		assertEquals("00:01:00",TimeTools.formatLongTime(60));
		assertEquals("00:01:05",TimeTools.formatLongTime(65));
		assertEquals("00:01:15",TimeTools.formatLongTime(75));
		assertEquals("00:15:00",TimeTools.formatLongTime(900));
		assertEquals("00:59:59",TimeTools.formatLongTime(3599));
		assertEquals("01:00:00",TimeTools.formatLongTime(3600));
		assertEquals("05:00:00",TimeTools.formatLongTime(3600*5));
		assertEquals("15:00:00",TimeTools.formatLongTime(3600*15));
		assertEquals("1:06:00:00",TimeTools.formatLongTime(3600*30));
		assertEquals("-00:00:05",TimeTools.formatLongTime(-5));

		assertEquals("00:00:00",TimeTools.formatLongTime(0.3));
		assertEquals("00:00:05",TimeTools.formatLongTime(5.3));
		assertEquals("00:00:06",TimeTools.formatLongTime(5.7));
		assertEquals("00:00:15",TimeTools.formatLongTime(15.3));
		assertEquals("00:00:59",TimeTools.formatLongTime(59.3));
		assertEquals("00:01:00",TimeTools.formatLongTime(60.3));
		assertEquals("00:01:05",TimeTools.formatLongTime(65.3));
		assertEquals("00:01:15",TimeTools.formatLongTime(75.3));
		assertEquals("00:15:00",TimeTools.formatLongTime(900.3));
		assertEquals("00:59:59",TimeTools.formatLongTime(3599.3));
		assertEquals("01:00:00",TimeTools.formatLongTime(3600.3));
		assertEquals("05:00:00",TimeTools.formatLongTime(3600*5+0.3));
		assertEquals("15:00:00",TimeTools.formatLongTime(3600*15+0.3));
		assertEquals("1:06:00:00",TimeTools.formatLongTime(3600*30+0.3));
		assertEquals("-00:00:05",TimeTools.formatLongTime(-5.3));
	}

	/**
	 * Test: Formatiert einen Zahlenwert als HH:MM:-Zeitangabe
	 * @see TimeTools#formatShortTime(int)
	 */
	@Test
	void formatShortTime() {
		assertEquals("00:00",TimeTools.formatShortTime(59));
		assertEquals("00:01",TimeTools.formatShortTime(61));
		assertEquals("00:59",TimeTools.formatShortTime(3599));
		assertEquals("01:00",TimeTools.formatShortTime(3600));
		assertEquals("10:00",TimeTools.formatShortTime(36000));

		assertEquals("-00:00",TimeTools.formatShortTime(-59));
		assertEquals("-00:01",TimeTools.formatShortTime(-61));
		assertEquals("-00:59",TimeTools.formatShortTime(-3599));
		assertEquals("-01:00",TimeTools.formatShortTime(-3600));
		assertEquals("-10:00",TimeTools.formatShortTime(-36000));
	}

	/**
	 * Test: Formatiert einen Zahlenwert als HH:MM:SS,s-Zeitangabe (d.h. mit Nachkomma-Sekunden)
	 * @see TimeTools#formatExactTime(double)
	 * @see TimeTools#formatExactTime(double, int)
	 */
	@Test
	void formatExactTime() {
		assertEquals("00:00:00",TimeTools.formatExactTime(0));
		assertEquals("00:00:05",TimeTools.formatExactTime(5));
		assertEquals("-00:00:05",TimeTools.formatExactTime(-5));
		assertEquals("00:00:15",TimeTools.formatExactTime(15));
		assertEquals("00:00:59",TimeTools.formatExactTime(59));
		assertEquals("00:01:00",TimeTools.formatExactTime(60));
		assertEquals("00:01:05",TimeTools.formatExactTime(65));
		assertEquals("00:01:15",TimeTools.formatExactTime(75));
		assertEquals("00:15:00",TimeTools.formatExactTime(900));
		assertEquals("00:59:59",TimeTools.formatExactTime(3599));
		assertEquals("01:00:00",TimeTools.formatExactTime(3600));
		assertEquals("05:00:00",TimeTools.formatExactTime(3600*5));
		assertEquals("15:00:00",TimeTools.formatExactTime(3600*15));
		assertEquals("30:00:00",TimeTools.formatExactTime(3600*30));
		assertEquals("-00:00:05",TimeTools.formatExactTime(-5));

		assertEquals("00:00:00,3",TimeTools.formatExactTime(0.3));
		assertEquals("00:00:00,3",TimeTools.formatExactTime(0.321));
		assertEquals("00:00:00,32",TimeTools.formatExactTime(0.321,2));
		assertEquals("00:00:00,321",TimeTools.formatExactTime(0.321,3));
		assertEquals("00:00:00,9",TimeTools.formatExactTime(0.9));
		assertEquals("00:00:01",TimeTools.formatExactTime(0.987));
		assertEquals("00:00:00,99",TimeTools.formatExactTime(0.987,2));
		assertEquals("00:00:00,987",TimeTools.formatExactTime(0.987,3));
		assertEquals("00:00:05,3",TimeTools.formatExactTime(5.3));
		assertEquals("00:00:05,7",TimeTools.formatExactTime(5.7));
		assertEquals("00:00:15,3",TimeTools.formatExactTime(15.3));
		assertEquals("00:00:59,3",TimeTools.formatExactTime(59.3));
		assertEquals("00:01:00,3",TimeTools.formatExactTime(60.3));
		assertEquals("00:01:05,3",TimeTools.formatExactTime(65.3));
		assertEquals("00:01:15,3",TimeTools.formatExactTime(75.3));
		assertEquals("00:15:00,3",TimeTools.formatExactTime(900.3));
		assertEquals("00:59:59,3",TimeTools.formatExactTime(3599.3));
		assertEquals("01:00:00,3",TimeTools.formatExactTime(3600.3));
		assertEquals("05:00:00,3",TimeTools.formatExactTime(3600*5+0.3));
		assertEquals("15:00:00,3",TimeTools.formatExactTime(3600*15+0.3));
		assertEquals("30:00:00,3",TimeTools.formatExactTime(3600*30+0.3));
		assertEquals("-00:00:05,3",TimeTools.formatExactTime(-5.3));

		assertEquals("00:00:05,3",TimeTools.formatExactTime(5.32));
		assertEquals("00:00:05,4",TimeTools.formatExactTime(5.37));

		assertEquals("00:00:05",TimeTools.formatExactTime(5.3456,0));
		assertEquals("00:00:05,3",TimeTools.formatExactTime(5.3456,1));
		assertEquals("00:00:05,35",TimeTools.formatExactTime(5.3456,2));
		assertEquals("00:00:05,346",TimeTools.formatExactTime(5.3456,3));
		assertEquals("00:00:05,3456",TimeTools.formatExactTime(5.3456,4));
		assertEquals("00:00:05,3456",TimeTools.formatExactTime(5.3456,5));
		assertEquals("00:00:05,01",TimeTools.formatExactTime(5.01,4));
	}

	/**
	 * Test: Formatiert einen Zahlenwert als HH:MM:SS.s-Zeitangabe (d.h. mit Nachkomma-Sekunden und mit Dezimalpunkt)
	 * @see TimeTools#formatExactSystemTime(double)
	 */
	@Test
	void formatExactSystemTime() {
		assertEquals("00:00:00",TimeTools.formatExactSystemTime(0));
		assertEquals("00:00:05",TimeTools.formatExactSystemTime(5));
		assertEquals("00:00:15",TimeTools.formatExactSystemTime(15));
		assertEquals("00:00:59",TimeTools.formatExactSystemTime(59));
		assertEquals("00:01:00",TimeTools.formatExactSystemTime(60));
		assertEquals("00:01:05",TimeTools.formatExactSystemTime(65));
		assertEquals("00:01:15",TimeTools.formatExactSystemTime(75));
		assertEquals("00:15:00",TimeTools.formatExactSystemTime(900));
		assertEquals("00:59:59",TimeTools.formatExactSystemTime(3599));
		assertEquals("01:00:00",TimeTools.formatExactSystemTime(3600));
		assertEquals("05:00:00",TimeTools.formatExactSystemTime(3600*5));
		assertEquals("15:00:00",TimeTools.formatExactSystemTime(3600*15));
		assertEquals("30:00:00",TimeTools.formatExactSystemTime(3600*30));
		assertEquals("-00:00:05",TimeTools.formatExactSystemTime(-5));

		assertEquals("00:00:00.3",TimeTools.formatExactSystemTime(0.3));
		assertEquals("00:00:05.3",TimeTools.formatExactSystemTime(5.3));
		assertEquals("00:00:05.7",TimeTools.formatExactSystemTime(5.7));
		assertEquals("00:00:15.3",TimeTools.formatExactSystemTime(15.3));
		assertEquals("00:00:59.3",TimeTools.formatExactSystemTime(59.3));
		assertEquals("00:01:00.3",TimeTools.formatExactSystemTime(60.3));
		assertEquals("00:01:05.3",TimeTools.formatExactSystemTime(65.3));
		assertEquals("00:01:15.3",TimeTools.formatExactSystemTime(75.3));
		assertEquals("00:15:00.3",TimeTools.formatExactSystemTime(900.3));
		assertEquals("00:59:59.3",TimeTools.formatExactSystemTime(3599.3));
		assertEquals("01:00:00.3",TimeTools.formatExactSystemTime(3600.3));
		assertEquals("05:00:00.3",TimeTools.formatExactSystemTime(3600*5+0.3));
		assertEquals("15:00:00.3",TimeTools.formatExactSystemTime(3600*15+0.3));
		assertEquals("30:00:00.3",TimeTools.formatExactSystemTime(3600*30+0.3));
		assertEquals("-00:00:05.3",TimeTools.formatExactSystemTime(-5.3));

		assertEquals("00:00:05.32",TimeTools.formatExactSystemTime(5.32));
		assertEquals("00:00:05.37",TimeTools.formatExactSystemTime(5.37));
	}

	/**
	 * Test: Versucht einen {@link String} als Zeitangabe zu parsen
	 * @see TimeTools#getTime(String)
	 * @see TimeTools#getTime(JTextField, boolean)
	 * @see TimeTools#getTime(TextField, boolean)
	 */
	@Test
	void getTime() {
		assertNull(TimeTools.getTime(null));
		assertNull(TimeTools.getTime(""));
		assertNull(TimeTools.getTime("äöü"));
		assertNull(TimeTools.getTime("0:1:2:3:4"));
		assertNull(TimeTools.getTime("0:1:2:3:4:5"));

		assertEquals(Integer.valueOf(123),TimeTools.getTime("123"));
		assertEquals(Integer.valueOf(61),TimeTools.getTime("1:1"));
		assertEquals(Integer.valueOf(3601),TimeTools.getTime("1::1"));
		assertEquals(Integer.valueOf(61),TimeTools.getTime("0:1:1"));
		assertEquals(Integer.valueOf(61),TimeTools.getTime("0:01:01"));
		assertEquals(Integer.valueOf(61),TimeTools.getTime("0:00:61"));
		assertEquals(Integer.valueOf(10*3600+61),TimeTools.getTime("10:00:61"));
		assertEquals(Integer.valueOf(30*3600+61),TimeTools.getTime("30:00:61"));
		assertEquals(Integer.valueOf(30*3600+61),TimeTools.getTime("1:06:00:61"));

		TextField f=null;

		assertNull(TimeTools.getTime(f,false));
		assertNull(TimeTools.getTime(f,true));

		assertNull(TimeTools.getTime(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Integer.valueOf(61),TimeTools.getTime(f=new TextField("1:1"),false));
		assertEquals(null,f.getBackground());

		assertNull(TimeTools.getTime(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Integer.valueOf(61),TimeTools.getTime(f=new TextField("1:1"),true));
		assertEquals(SystemColor.text,f.getBackground());

		JTextField j=null;

		assertNull(TimeTools.getTime(j,false));
		assertNull(TimeTools.getTime(j,true));

		assertNull(TimeTools.getTime(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Integer.valueOf(61),TimeTools.getTime(j=new JTextField("1:1"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertNull(TimeTools.getTime(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Integer.valueOf(61),TimeTools.getTime(j=new JTextField("1:1"),true));
		assertEquals(SystemColor.text,j.getBackground());
	}

	/**
	 * Test: Versucht einen {@link String} als Zeitangabe mit Nachkomma-Sekunden zu parsen
	 * @see TimeTools#getExactTime(String)
	 * @see TimeTools#getExactTime(JTextField, boolean)
	 * @see TimeTools#getExactTime(TextField, boolean)
	 */
	@Test
	void getExactTime() {
		assertNull(TimeTools.getExactTime(null));
		assertNull(TimeTools.getExactTime(""));
		assertNull(TimeTools.getExactTime("äöü"));
		assertNull(TimeTools.getExactTime("0:1:2:3:4"));

		assertEquals(Double.valueOf(123),TimeTools.getExactTime("123"));
		assertEquals(Double.valueOf(61),TimeTools.getExactTime("1:1"));
		assertEquals(Double.valueOf(-61),TimeTools.getExactTime("-1:1"));
		assertEquals(Double.valueOf(3601),TimeTools.getExactTime("1::1"));
		assertEquals(Double.valueOf(61),TimeTools.getExactTime("0:1:1"));
		assertEquals(Double.valueOf(61),TimeTools.getExactTime("0:01:01"));
		assertEquals(Double.valueOf(61),TimeTools.getExactTime("0:00:61"));
		assertEquals(Double.valueOf(10*3600+61),TimeTools.getExactTime("10:00:61"));
		assertEquals(Double.valueOf(30*3600+61),TimeTools.getExactTime("30:00:61"));
		assertEquals(Double.valueOf(30*3600+61),TimeTools.getExactTime("1:06:00:61"));
		assertEquals(Double.valueOf(61.3),TimeTools.getExactTime("0:1:1,3"));
		assertEquals(Double.valueOf(61.3),TimeTools.getExactTime("0:1:1.3"));
		assertEquals(null,TimeTools.getExactTime("0:1:1.3,5"));

		TextField f=null;

		assertNull(TimeTools.getExactTime(f,false));
		assertNull(TimeTools.getExactTime(f,true));

		assertNull(TimeTools.getExactTime(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(61),TimeTools.getExactTime(f=new TextField("1:1"),false));
		assertEquals(null,f.getBackground());

		assertNull(TimeTools.getExactTime(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(61),TimeTools.getExactTime(f=new TextField("1:1"),true));
		assertEquals(SystemColor.text,f.getBackground());

		JTextField j=null;

		assertNull(TimeTools.getExactTime(j,false));
		assertNull(TimeTools.getExactTime(j,true));

		assertNull(TimeTools.getExactTime(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(61),TimeTools.getExactTime(j=new JTextField("1:1"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertNull(TimeTools.getExactTime(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(61),TimeTools.getExactTime(j=new JTextField("1:1"),true));
		assertEquals(SystemColor.text,j.getBackground());
	}

	/**
	 * Test: Umwandlung eines Strings mit einem lokalen Zeitwert (z.B. Dezimalkomma) in einen System-Zeitwert (mit Dezimalpunkt)
	 */
	@Test
	void localTimeTest() {
		/* Leere Werte */
		assertEquals("",TimeTools.localTimeToSystemTime(null));
		assertEquals("",TimeTools.localTimeToSystemTime(""));

		/* Nicht-Zeitangaben nicht ändern */
		assertEquals("12:34",TimeTools.localTimeToSystemTime("12:34"));
		assertEquals("12:34,7",TimeTools.localTimeToSystemTime("12:34,7"));
		assertEquals("12:34:x6,7",TimeTools.localTimeToSystemTime("12:34:x6,7"));

		/* Zeitangaben */
		assertEquals("12:34:56",TimeTools.localTimeToSystemTime("12:34:56"));
		assertEquals("12:34:56.7",TimeTools.localTimeToSystemTime("12:34:56,7"));
		assertEquals("12:34:56.789",TimeTools.localTimeToSystemTime("12:34:56,789"));
		assertEquals("12:34:56.7",TimeTools.localTimeToSystemTime("12:34:56.7"));
		assertEquals("12:34:56.789",TimeTools.localTimeToSystemTime("12:34:56.789"));
	}
}