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

/**
 * Prüft die Funktionsweise von {@link NumberTools}
 * @author Alexander Herzog
 * @see NumberTools
 */
class NumberToolsTest {

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
		final Constructor<NumberTools> constructor=NumberTools.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * Test: Sprache einstellen und wieder auslesen
	 */
	@Test
	void getLocale() {
		NumberTools.setLocale(Locale.US);
		assertEquals(Locale.US,NumberTools.getLocale());
		NumberTools.setLocale(Locale.GERMANY);
		assertEquals(Locale.GERMANY,NumberTools.getLocale());
	}

	/**
	 * Test: Umwandlung {@link Long} zu {@link String}
	 * @see NumberTools#formatLong(long)
	 */
	@Test
	void formatLong() {
		/* Zahlen / Anzahl an Ziffern */
		assertEquals("0",NumberTools.formatLong(0));
		assertEquals("1",NumberTools.formatLong(1));
		assertEquals("9",NumberTools.formatLong(9));
		assertEquals("10",NumberTools.formatLong(10));
		assertEquals("99",NumberTools.formatLong(99));
		assertEquals("100",NumberTools.formatLong(100));
		assertEquals("999",NumberTools.formatLong(999));
		assertEquals("1.000",NumberTools.formatLong(1000));

		/* Negative Zahlen */
		assertEquals("1",NumberTools.formatLong(1));
		assertEquals("-9",NumberTools.formatLong(-9));
		assertEquals("-10",NumberTools.formatLong(-10));
		assertEquals("-99",NumberTools.formatLong(-99));
		assertEquals("-100",NumberTools.formatLong(-100));
		assertEquals("-999",NumberTools.formatLong(-999));
		assertEquals("-1.000",NumberTools.formatLong(-1000));

		/* Gruppierungspunkte */
		assertEquals("1.234",NumberTools.formatLong(1234));
		assertEquals("12.345",NumberTools.formatLong(12345));
		assertEquals("123.456",NumberTools.formatLong(123456));
		assertEquals("1.234.567",NumberTools.formatLong(1234567));
		assertEquals("12.345.678",NumberTools.formatLong(12345678));
		assertEquals("123.456.789",NumberTools.formatLong(123456789));
		assertEquals("1.234.567.890",NumberTools.formatLong(1234567890));
		assertEquals("12.345.678.901",NumberTools.formatLong(12345678901L));
		assertEquals("123.456.789.012",NumberTools.formatLong(123456789012L));
		assertEquals("1.234.567.890.123",NumberTools.formatLong(1234567890123L));
		assertEquals("12.345.678.901.234",NumberTools.formatLong(12345678901234L));
		assertEquals("123.456.789.012.345",NumberTools.formatLong(123456789012345L));

		assertEquals("1.234.567.890.123.456",NumberTools.formatLong(1234567890123456L));
		assertEquals("12.345.678.901.234.567",NumberTools.formatLong(12345678901234567L));
		assertEquals("123.456.789.012.345.678",NumberTools.formatLong(123456789012345678L));
		assertEquals("1.234.567.890.123.456.789",NumberTools.formatLong(1234567890123456789L));

		assertEquals("-10.000",NumberTools.formatLong(-10000));

		/* Caching */
		assertEquals("1.234",NumberTools.formatLong(1234));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		assertEquals("1,234",NumberTools.formatLong(1234));
		assertEquals("1,234,567",NumberTools.formatLong(1234567));
		NumberTools.setLocale(Locale.GERMANY);
	}

	private void formatLongStringBuilderInt(final String text) {
		StringBuilder sb;

		/* Zahlen / Anzahl an Ziffern */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("0",NumberTools.formatLong(0,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1",NumberTools.formatLong(1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("9",NumberTools.formatLong(9,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("10",NumberTools.formatLong(10,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("99",NumberTools.formatLong(99,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("100",NumberTools.formatLong(100,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("999",NumberTools.formatLong(999,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.000",NumberTools.formatLong(1000,sb));

		/* Negative Zahlen */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1",NumberTools.formatLong(1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-9",NumberTools.formatLong(-9,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-10",NumberTools.formatLong(-10,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-99",NumberTools.formatLong(-99,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-100",NumberTools.formatLong(-100,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-999",NumberTools.formatLong(-999,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-1.000",NumberTools.formatLong(-1000,sb));

		/* Gruppierungspunkte */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.234",NumberTools.formatLong(1234,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12.345",NumberTools.formatLong(12345,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123.456",NumberTools.formatLong(123456,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.234.567",NumberTools.formatLong(1234567,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12.345.678",NumberTools.formatLong(12345678,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123.456.789",NumberTools.formatLong(123456789,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.234.567.890",NumberTools.formatLong(1234567890,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12.345.678.901",NumberTools.formatLong(12345678901L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123.456.789.012",NumberTools.formatLong(123456789012L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.234.567.890.123",NumberTools.formatLong(1234567890123L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12.345.678.901.234",NumberTools.formatLong(12345678901234L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123.456.789.012.345",NumberTools.formatLong(123456789012345L,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.234.567.890.123.456",NumberTools.formatLong(1234567890123456L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12.345.678.901.234.567",NumberTools.formatLong(12345678901234567L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123.456.789.012.345.678",NumberTools.formatLong(123456789012345678L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.234.567.890.123.456.789",NumberTools.formatLong(1234567890123456789L,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-10.000",NumberTools.formatLong(-10000,sb));

		/* Caching */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1.234",NumberTools.formatLong(1234,sb));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1,234",NumberTools.formatLong(1234,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1,234,567",NumberTools.formatLong(1234567,sb));
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Umwandlung {@link Long} zu {@link String} mit {@link StringBuilder}-Recycling
	 * @see NumberTools#formatLong(long, StringBuilder)
	 */
	@Test
	void formatLongStringBuilder() {
		formatLongStringBuilderInt(null);
		formatLongStringBuilderInt("");
		formatLongStringBuilderInt("abc");
	}

	private void formatLongNoGroupingStringBuilderInt(final String text) {
		StringBuilder sb;

		/* Zahlen / Anzahl an Ziffern */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("0",NumberTools.formatLongNoGrouping(0,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1",NumberTools.formatLongNoGrouping(1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("9",NumberTools.formatLongNoGrouping(9,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("10",NumberTools.formatLongNoGrouping(10,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("99",NumberTools.formatLongNoGrouping(99,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("100",NumberTools.formatLongNoGrouping(100,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("999",NumberTools.formatLongNoGrouping(999,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1000",NumberTools.formatLongNoGrouping(1000,sb));

		/* Negative Zahlen */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1",NumberTools.formatLongNoGrouping(1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-9",NumberTools.formatLongNoGrouping(-9,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-10",NumberTools.formatLongNoGrouping(-10,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-99",NumberTools.formatLongNoGrouping(-99,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-100",NumberTools.formatLongNoGrouping(-100,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-999",NumberTools.formatLongNoGrouping(-999,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-1000",NumberTools.formatLongNoGrouping(-1000,sb));

		/* Gruppierungspunkte */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234",NumberTools.formatLongNoGrouping(1234,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12345",NumberTools.formatLongNoGrouping(12345,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456",NumberTools.formatLongNoGrouping(123456,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567",NumberTools.formatLongNoGrouping(1234567,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12345678",NumberTools.formatLongNoGrouping(12345678,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456789",NumberTools.formatLongNoGrouping(123456789,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567890",NumberTools.formatLongNoGrouping(1234567890,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12345678901",NumberTools.formatLongNoGrouping(12345678901L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456789012",NumberTools.formatLongNoGrouping(123456789012L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567890123",NumberTools.formatLongNoGrouping(1234567890123L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12345678901234",NumberTools.formatLongNoGrouping(12345678901234L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456789012345",NumberTools.formatLongNoGrouping(123456789012345L,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567890123456",NumberTools.formatLongNoGrouping(1234567890123456L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12345678901234567",NumberTools.formatLongNoGrouping(12345678901234567L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456789012345678",NumberTools.formatLongNoGrouping(123456789012345678L,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567890123456789",NumberTools.formatLongNoGrouping(1234567890123456789L,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-10000",NumberTools.formatLongNoGrouping(-10000,sb));

		/* Caching */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234",NumberTools.formatLongNoGrouping(1234,sb));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234",NumberTools.formatLongNoGrouping(1234,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567",NumberTools.formatLongNoGrouping(1234567,sb));
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Umwandlung {@link Long} zu {@link String} ohne 1000er-Punkte mit {@link StringBuilder}-Recycling
	 * @see NumberTools#formatLongNoGrouping(long, StringBuilder)
	 */
	@Test
	void formatLongNoGroupingStringBuilder() {
		formatLongNoGroupingStringBuilderInt(null);
		formatLongNoGroupingStringBuilderInt("");
		formatLongNoGroupingStringBuilderInt("abc");
	}

	/**
	 * Test: Umwandlung von {@link Double} zu {@link String} mit 1000er-Punkt (was sonst bei {@link Double}-Umwandlungen unüblich ist)
	 * @see NumberTools#formatNumberLong(double)
	 */
	@Test
	void formatNumberLong() {
		assertEquals("123.456,7",NumberTools.formatNumberLong(123456.74));
		assertEquals("123.456,8",NumberTools.formatNumberLong(123456.75));
		assertEquals("123.456",NumberTools.formatNumberLong(123456));

		assertEquals("-123.456,7",NumberTools.formatNumberLong(-123456.74));
		assertEquals("-123.456,8",NumberTools.formatNumberLong(-123456.75));
		assertEquals("-123.456",NumberTools.formatNumberLong(-123456));

		/* Runden mit Übertrag der Ganzzahl */
		assertEquals("100",NumberTools.formatNumberLong(99.9999));
	}

	/**
	 * Test: Umwandlung {@link Double} zu {@link String}
	 * @see NumberTools#formatNumber(double)
	 */
	@Test
	void formatNumber() {
		assertEquals("123456,7",NumberTools.formatNumber(123456.74));
		assertEquals("123456,8",NumberTools.formatNumber(123456.75));
		assertEquals("123456",NumberTools.formatNumber(123456));

		assertEquals("-123456,7",NumberTools.formatNumber(-123456.74));
		assertEquals("-123456,8",NumberTools.formatNumber(-123456.75));
		assertEquals("-123456",NumberTools.formatNumber(-123456));

		/* Null-Wert */
		assertEquals("0",NumberTools.formatNumber(10E-17));

		/* Rundung (für Werte nahe bei 0) */
		assertEquals("0",NumberTools.formatNumber(0.01));
		assertEquals("0,1",NumberTools.formatNumber(0.1));

		/* Ganzzahlen */
		assertEquals("12345",NumberTools.formatNumber(12345));
		assertEquals("-12345",NumberTools.formatNumber(-12345));
		assertEquals("1234567890",NumberTools.formatNumber(1234567890));
		assertEquals("-1234567890",NumberTools.formatNumber(-1234567890));

		/* Runden nicht nötig */
		assertEquals("2",NumberTools.formatNumber(2.0000000000000005));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		assertEquals("123456.7",NumberTools.formatNumber(123456.74));
		assertEquals("123456.8",NumberTools.formatNumber(123456.75));
		assertEquals("-123456.7",NumberTools.formatNumber(-123456.74));
		assertEquals("-123456.8",NumberTools.formatNumber(-123456.75));
		NumberTools.setLocale(Locale.GERMANY);
	}

	private void formatNumberStringBuilderInt(final String text) {
		StringBuilder sb;

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,7",NumberTools.formatNumber(123456.74,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,8",NumberTools.formatNumber(123456.75,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456",NumberTools.formatNumber(123456,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456,7",NumberTools.formatNumber(-123456.74,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456,8",NumberTools.formatNumber(-123456.75,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456",NumberTools.formatNumber(-123456,sb));

		/* Null-Wert */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("0",NumberTools.formatNumber(10E-17,sb));

		/* Ganzzahlen */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12345",NumberTools.formatNumber(12345,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-12345",NumberTools.formatNumber(-12345,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567890",NumberTools.formatNumber(1234567890,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-1234567890",NumberTools.formatNumber(-1234567890,sb));

		/* Runden nicht nötig */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("2",NumberTools.formatNumber(2.0000000000000005,sb));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456.7",NumberTools.formatNumber(123456.74,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456.8",NumberTools.formatNumber(123456.75,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456.7",NumberTools.formatNumber(-123456.74,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456.8",NumberTools.formatNumber(-123456.75,sb));
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Umwandlung {@link Double} zu {@link String} mit {@link StringBuilder}-Recycling
	 * @see NumberTools#formatNumber(double, StringBuilder)
	 */
	@Test
	void formatNumberStringBuilder() {
		formatNumberStringBuilderInt(null);
		formatNumberStringBuilderInt("");
		formatNumberStringBuilderInt("abc");
	}

	/**
	 * Test: Umwandlung {@link Double} zu {@link String} mit maximaler Genauigkeit
	 * @see NumberTools#formatNumberMax(double)
	 */
	@Test
	void formatNumberMax() {
		assertEquals("123456,789",NumberTools.formatNumberMax(123456.789));
		assertEquals("-123456,789",NumberTools.formatNumberMax(-123456.789));

		/* Null-Wert */
		assertEquals("0",NumberTools.formatNumberMax(10E-17));

		/* Ganzzahlen */
		assertEquals("12345",NumberTools.formatNumberMax(12345));
		assertEquals("-12345",NumberTools.formatNumberMax(-12345));
		assertEquals("1234567890",NumberTools.formatNumberMax(1234567890));
		assertEquals("-1234567890",NumberTools.formatNumberMax(-1234567890));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		assertEquals("123456.789",NumberTools.formatNumberMax(123456.789));
		assertEquals("-123456.789",NumberTools.formatNumberMax(-123456.789));
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Umwandlung {@link Double} zu {@link String} mit variabler Anzahl an Nachkommastellen
	 * @see NumberTools#formatNumber(double, int)
	 */
	@Test
	void formatNumberDigits() {
		assertEquals("123456,7",NumberTools.formatNumber(123456.74,1));
		assertEquals("123456,8",NumberTools.formatNumber(123456.75,1));
		assertEquals("123456",NumberTools.formatNumber(123456,1));

		assertEquals("123456,74",NumberTools.formatNumber(123456.74,2));
		assertEquals("123456,75",NumberTools.formatNumber(123456.75,2));

		assertEquals("123456,74",NumberTools.formatNumber(123456.74,3));
		assertEquals("123456,75",NumberTools.formatNumber(123456.75,3));

		assertEquals("-123456,7",NumberTools.formatNumber(-123456.74,1));
		assertEquals("-123456,8",NumberTools.formatNumber(-123456.75,1));
		assertEquals("-123456",NumberTools.formatNumber(-123456,1));

		/* Null-Wert */
		assertEquals("0",NumberTools.formatNumber(10E-17,5));

		/* Ganzzahlen */
		assertEquals("12345",NumberTools.formatNumber(12345,5));
		assertEquals("-12345",NumberTools.formatNumber(-12345,5));
		assertEquals("1234567890",NumberTools.formatNumber(1234567890,5));
		assertEquals("-1234567890",NumberTools.formatNumber(-1234567890,5));

		/* Runden nicht nötig */
		assertEquals("2",NumberTools.formatNumber(2.0000000000000005,5));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		assertEquals("123456.7",NumberTools.formatNumber(123456.74,1));
		assertEquals("123456.8",NumberTools.formatNumber(123456.75,1));
		assertEquals("123456.74",NumberTools.formatNumber(123456.74,2));
		assertEquals("123456.75",NumberTools.formatNumber(123456.75,2));
		NumberTools.setLocale(Locale.GERMANY);
	}

	private void formatNumberDigitsStringBuilderInt(final String text) {
		StringBuilder sb;

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,7",NumberTools.formatNumber(123456.74,1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,8",NumberTools.formatNumber(123456.75,1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456",NumberTools.formatNumber(123456,1,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,74",NumberTools.formatNumber(123456.74,2,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,75",NumberTools.formatNumber(123456.75,2,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,74",NumberTools.formatNumber(123456.74,3,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456,75",NumberTools.formatNumber(123456.75,3,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456,7",NumberTools.formatNumber(-123456.74,1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456,8",NumberTools.formatNumber(-123456.75,1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-123456",NumberTools.formatNumber(-123456,1,sb));

		/* Null-Wert */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("0",NumberTools.formatNumber(10E-17,5,sb));

		/* Ganzzahlen */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12345",NumberTools.formatNumber(12345,5,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-12345",NumberTools.formatNumber(-12345,5,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("1234567890",NumberTools.formatNumber(1234567890,5,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("-1234567890",NumberTools.formatNumber(-1234567890,5,sb));

		/* Runden nicht nötig */
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("2",NumberTools.formatNumber(2.0000000000000005,5,sb));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456.7",NumberTools.formatNumber(123456.74,1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456.8",NumberTools.formatNumber(123456.75,1,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456.74",NumberTools.formatNumber(123456.74,2,sb));
		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("123456.75",NumberTools.formatNumber(123456.75,2,sb));
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Umwandlung {@link Double} zu {@link String} mit variabler Anzahl an Nachkommastellen und {@link StringBuilder}-Recycling
	 * @see NumberTools#formatNumber(double, int, StringBuilder)
	 */
	@Test
	void formatNumberDigitsStringBuilder() {
		formatNumberDigitsStringBuilderInt(null);
		formatNumberDigitsStringBuilderInt("");
		formatNumberDigitsStringBuilderInt("abc");
	}

	private void formatPercentStringBuilderInt(final String text) {
		StringBuilder sb;

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12,3%",NumberTools.formatPercent(0.12345,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12,3%",NumberTools.formatPercent(0.12345,1,sb));

		sb=(text==null)?null:new StringBuilder(text);
		assertEquals("12,35%",NumberTools.formatPercent(0.12345,2,sb));
	}

	/**
	 * Test: Umwandeln {@link Double} zu Prozentangabe als {@link String} mit {@link StringBuilder}-Recycling
	 * @see NumberTools#formatPercent(double, StringBuilder)
	 */
	@Test
	void formatPercentStringBuilder() {
		formatPercentStringBuilderInt(null);
		formatPercentStringBuilderInt("");
		formatPercentStringBuilderInt("abc");
	}

	/**
	 * Test: Umwandeln {@link Double} zu Prozentangabe als {@link String}
	 * @see NumberTools#formatPercent(double)
	 */
	@Test
	void formatPercent() {
		assertEquals("12,3%",NumberTools.formatPercent(0.12345));
		assertEquals("12,3%",NumberTools.formatPercent(0.12345,1));
		assertEquals("12,35%",NumberTools.formatPercent(0.12345,2));
	}

	/**
	 * Test: Umwandeln {@link Double} zu {@link String} in System-Notation (d.h. mit Dezimalpunkt)
	 * @see NumberTools#formatSystemNumber(double)
	 */
	@Test
	void formatSystemNumber() {
		assertEquals("123456.789",NumberTools.formatSystemNumber(123456.789));
		assertEquals("-123456.789",NumberTools.formatSystemNumber(-123456.789));

		/* Null-Wert */
		assertEquals("0",NumberTools.formatSystemNumber(10E-17));

		/* Ganzzahlen */
		assertEquals("12345",NumberTools.formatSystemNumber(12345));
		assertEquals("-12345",NumberTools.formatSystemNumber(-12345));
		assertEquals("1234567890",NumberTools.formatSystemNumber(1234567890));
		assertEquals("-1234567890",NumberTools.formatSystemNumber(-1234567890));
	}

	/**
	 * Test: Umwandeln {@link Double} zu {@link String} in System-Notation (d.h. mit Dezimalpunkt) mit variabler Anzahl an Nachkommastellen
	 * @see NumberTools#formatSystemNumber(double, int)
	 */
	@Test
	void formatSystemNumberDigits() {
		assertEquals("123456.7",NumberTools.formatSystemNumber(123456.74,1));
		assertEquals("123456.8",NumberTools.formatSystemNumber(123456.75,1));
		assertEquals("123456",NumberTools.formatSystemNumber(123456,1));

		assertEquals("123456.74",NumberTools.formatSystemNumber(123456.74,2));
		assertEquals("123456.75",NumberTools.formatSystemNumber(123456.75,2));

		assertEquals("123456.74",NumberTools.formatSystemNumber(123456.74,3));
		assertEquals("123456.75",NumberTools.formatSystemNumber(123456.75,3));

		assertEquals("-123456.7",NumberTools.formatSystemNumber(-123456.74,1));
		assertEquals("-123456.8",NumberTools.formatSystemNumber(-123456.75,1));
		assertEquals("-123456",NumberTools.formatSystemNumber(-123456,1));

		/* Null-Wert */
		assertEquals("0",NumberTools.formatSystemNumber(10E-17,5));

		/* Ganzzahlen */
		assertEquals("12345",NumberTools.formatSystemNumber(12345,5));
		assertEquals("-12345",NumberTools.formatSystemNumber(-12345,5));
		assertEquals("1234567890",NumberTools.formatSystemNumber(1234567890,5));
		assertEquals("-1234567890",NumberTools.formatSystemNumber(-1234567890,5));

		/* Runden nicht nötig */
		assertEquals("2",NumberTools.formatSystemNumber(2.0000000000000005,5));
	}

	/**
	 * Test: Umwandlung einer lokalen Zahl (ggf. mit Dezimalkomma) in eine Systemzahl (immer mit Dezimalpunkt)
	 * @see NumberTools#localNumberToSystemNumber(String)
	 */
	@Test
	void localNumberToSystemNumber() {
		assertEquals("123.456",NumberTools.localNumberToSystemNumber("123,456"));
		assertEquals("123.456",NumberTools.localNumberToSystemNumber("123.456"));
		assertEquals("123.456%",NumberTools.localNumberToSystemNumber("123,456%"));
		assertEquals("123.456%",NumberTools.localNumberToSystemNumber("123.456%"));

		NumberTools.setLocale(Locale.US);
		assertEquals("123.456",NumberTools.localNumberToSystemNumber("123,456"));
		assertEquals("123.456",NumberTools.localNumberToSystemNumber("123.456"));
		assertEquals("123.456%",NumberTools.localNumberToSystemNumber("123,456%"));
		assertEquals("123.456%",NumberTools.localNumberToSystemNumber("123.456%"));
		NumberTools.setLocale(Locale.GERMANY);

		assertEquals("äöü",NumberTools.localNumberToSystemNumber("äöü"));
		assertEquals("123",NumberTools.localNumberToSystemNumber("123"));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		assertEquals("123.456",NumberTools.localNumberToSystemNumber("123,456"));
		assertEquals("123.456",NumberTools.localNumberToSystemNumber("123.456"));
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Umwandlung einer Systemzahl (immer mit Dezimalpunkt) in eine lokale Zahl (ggf. mit Dezimalkomma)
	 * @see NumberTools#systemNumberToLocalNumber(String)
	 */
	@Test
	void systemNumberToLocalNumber() {
		assertEquals("123,456",NumberTools.systemNumberToLocalNumber("123,456"));
		assertEquals("123,456",NumberTools.systemNumberToLocalNumber("123.456"));
		assertEquals("123,456%",NumberTools.systemNumberToLocalNumber("123,456%"));
		assertEquals("123,456%",NumberTools.systemNumberToLocalNumber("123.456%"));

		assertEquals("äöü",NumberTools.systemNumberToLocalNumber("äöü"));
		assertEquals("123",NumberTools.systemNumberToLocalNumber("123"));

		/* Locale Test */
		NumberTools.setLocale(Locale.US);
		assertEquals("123.456",NumberTools.systemNumberToLocalNumber("123,456"));
		assertEquals("123.456",NumberTools.systemNumberToLocalNumber("123.456"));
		NumberTools.setLocale(Locale.GERMANY);
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Integer}-Wert
	 * @see NumberTools#getInteger(String)
	 * @see NumberTools#getInteger(JTextField, boolean)
	 * @see NumberTools#getInteger(TextField, boolean)
	 */
	@Test
	void getInteger() {
		assertEquals(null,NumberTools.getInteger(null));
		assertEquals(null,NumberTools.getInteger("äöü"));
		assertEquals(Integer.valueOf(3),NumberTools.getInteger("1+2"));
		assertEquals(null,NumberTools.getInteger("1+2,5"));

		TextField f=null;

		assertEquals(null,NumberTools.getInteger(f,false));
		assertEquals(null,NumberTools.getInteger(f,true));

		assertEquals(null,NumberTools.getInteger(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getInteger(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getInteger(f=new TextField("1+2,5"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getInteger(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getInteger(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getInteger(f=new TextField("1+2,5"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getInteger(j,false));
		assertEquals(null,NumberTools.getInteger(j,true));

		assertEquals(null,NumberTools.getInteger(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getInteger(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getInteger(j=new JTextField("1+2,5"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getInteger(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getInteger(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getInteger(j=new JTextField("1+2,5"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Integer}-Wert, der nicht negativ sein darf
	 * @see NumberTools#getNotNegativeInteger(String)
	 * @see NumberTools#getNotNegativeInteger(JTextField, boolean)
	 * @see NumberTools#getNotNegativeInteger(TextField, boolean)
	 */
	@Test
	void getNotNegativeInteger() {
		assertEquals(null,NumberTools.getNotNegativeInteger(null));
		assertEquals(null,NumberTools.getNotNegativeInteger("äöü"));
		assertEquals(Integer.valueOf(3),NumberTools.getNotNegativeInteger("1+2"));
		assertEquals(null,NumberTools.getNotNegativeInteger("1+2,5"));
		assertEquals(null,NumberTools.getNotNegativeInteger("1-2"));

		TextField f=null;

		assertEquals(null,NumberTools.getNotNegativeInteger(f,false));
		assertEquals(null,NumberTools.getNotNegativeInteger(f,true));

		assertEquals(null,NumberTools.getNotNegativeInteger(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getNotNegativeInteger(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeInteger(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getNotNegativeInteger(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getNotNegativeInteger(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeInteger(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getNotNegativeInteger(j,false));
		assertEquals(null,NumberTools.getNotNegativeInteger(j,true));

		assertEquals(null,NumberTools.getNotNegativeInteger(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getNotNegativeInteger(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeInteger(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getNotNegativeInteger(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Integer.valueOf(3),NumberTools.getNotNegativeInteger(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeInteger(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Short}-Wert, der nicht negativ sein darf
	 * @see NumberTools#getNotNegativeShort(String)
	 * @see NumberTools#getNotNegativeShort(JTextField, boolean)
	 * @see NumberTools#getNotNegativeShort(TextField, boolean)
	 */
	@Test
	void getNotNegativeShort() {
		assertEquals(null,NumberTools.getNotNegativeShort(null));
		assertEquals(null,NumberTools.getNotNegativeShort("äöü"));
		assertEquals(Short.valueOf((short)3),NumberTools.getNotNegativeShort("1+2"));
		assertEquals(null,NumberTools.getNotNegativeShort("1+2,5"));
		assertEquals(null,NumberTools.getNotNegativeShort("1-2"));
		assertEquals(null,NumberTools.getNotNegativeShort("34000"));

		TextField f=null;

		assertEquals(null,NumberTools.getNotNegativeShort(f,false));
		assertEquals(null,NumberTools.getNotNegativeShort(f,true));

		assertEquals(null,NumberTools.getNotNegativeShort(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Short.valueOf((short)3),NumberTools.getNotNegativeShort(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeShort(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getNotNegativeShort(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Short.valueOf((short)3),NumberTools.getNotNegativeShort(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeShort(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getNotNegativeShort(j,false));
		assertEquals(null,NumberTools.getNotNegativeShort(j,true));

		assertEquals(null,NumberTools.getNotNegativeShort(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Short.valueOf((short)3),NumberTools.getNotNegativeShort(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeShort(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getNotNegativeShort(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Short.valueOf((short)3),NumberTools.getNotNegativeShort(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeShort(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Long}-Wert, der nicht negativ sein darf
	 * @see NumberTools#getNotNegativeLong(String)
	 * @see NumberTools#getNotNegativeLong(JTextField, boolean)
	 * @see NumberTools#getNotNegativeLong(TextField, boolean)
	 */
	@Test
	void getNotNegativeLong() {
		assertEquals(null,NumberTools.getNotNegativeLong(null));
		assertEquals(null,NumberTools.getNotNegativeLong("äöü"));
		assertEquals(Long.valueOf(3),NumberTools.getNotNegativeLong("1+2"));
		assertEquals(null,NumberTools.getNotNegativeLong("1+2,5"));
		assertEquals(null,NumberTools.getNotNegativeLong("1-2"));

		TextField f=null;

		assertEquals(null,NumberTools.getNotNegativeLong(f,false));
		assertEquals(null,NumberTools.getNotNegativeLong(f,true));

		assertEquals(null,NumberTools.getNotNegativeLong(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getNotNegativeLong(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeLong(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getNotNegativeLong(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getNotNegativeLong(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeLong(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getNotNegativeLong(j,false));
		assertEquals(null,NumberTools.getNotNegativeLong(j,true));

		assertEquals(null,NumberTools.getNotNegativeLong(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getNotNegativeLong(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeLong(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getNotNegativeLong(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getNotNegativeLong(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeLong(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Long}-Wert
	 * @see NumberTools#getLong(String)
	 * @see NumberTools#getLong(JTextField, boolean)
	 * @see NumberTools#getLong(TextField, boolean)
	 */
	@Test
	void getLong() {
		assertEquals(123,NumberTools.getLong("123"));
		assertEquals(12345678901234L,NumberTools.getLong("12345678901234"));
		assertEquals(null,NumberTools.getLong("1234567890ä1234"));

		assertEquals(null,NumberTools.getLong(null));
		assertEquals(null,NumberTools.getLong("äöü"));
		assertEquals(Long.valueOf(3),NumberTools.getLong("1+2"));
		assertEquals(null,NumberTools.getLong("1+2,5"));
		assertEquals(Long.valueOf(-1),NumberTools.getLong("1-2"));

		TextField f=null;

		assertEquals(null,NumberTools.getLong(f,false));
		assertEquals(null,NumberTools.getLong(f,true));

		assertEquals(null,NumberTools.getLong(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getLong(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Long.valueOf(-1),NumberTools.getLong(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getLong(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getLong(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Long.valueOf(-1),NumberTools.getLong(f=new TextField("1-2"),true));
		assertEquals(SystemColor.text,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getLong(j,false));
		assertEquals(null,NumberTools.getLong(j,true));

		assertEquals(null,NumberTools.getLong(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getLong(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Long.valueOf(-1),NumberTools.getLong(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getLong(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getLong(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Long.valueOf(-1),NumberTools.getLong(j=new JTextField("1-2"),true));
		assertEquals(SystemColor.text,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Double}-Wert mit der Einschränkung, dass der Text eine Ganzzahl repräsentieren muss
	 * @see NumberTools#getIntDouble(String)
	 * @see NumberTools#getIntDouble(JTextField, boolean)
	 * @see NumberTools#getIntDouble(TextField, boolean)
	 */
	@Test
	void getIntDouble() {
		assertEquals(null,NumberTools.getIntDouble(null));
		assertEquals(null,NumberTools.getIntDouble("äöü"));
		assertEquals(Double.valueOf(3),NumberTools.getIntDouble("1+2"));
		assertEquals(null,NumberTools.getIntDouble("1+2,5"));
		assertEquals(Double.valueOf(-1),NumberTools.getIntDouble("1-2"));

		TextField f=null;

		assertEquals(null,NumberTools.getIntDouble(f,false));
		assertEquals(null,NumberTools.getIntDouble(f,true));

		assertEquals(null,NumberTools.getIntDouble(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getIntDouble(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getIntDouble(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getIntDouble(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getIntDouble(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getIntDouble(f=new TextField("1-2"),true));
		assertEquals(SystemColor.text,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getIntDouble(j,false));
		assertEquals(null,NumberTools.getIntDouble(j,true));

		assertEquals(null,NumberTools.getIntDouble(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getIntDouble(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getIntDouble(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getIntDouble(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getIntDouble(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getIntDouble(j=new JTextField("1-2"),true));
		assertEquals(SystemColor.text,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Double}-Wert
	 * @see NumberTools#getDouble(String)
	 * @see NumberTools#getDouble(JTextField, boolean)
	 * @see NumberTools#getDouble(TextField, boolean)
	 */
	@Test
	void getDouble() {
		assertEquals(null,NumberTools.getDouble(null));
		assertEquals(null,NumberTools.getDouble("äöü"));
		assertEquals(Double.valueOf(3),NumberTools.getDouble("1+2"));
		assertEquals(Double.valueOf(3),NumberTools.getDouble("3."));
		assertEquals(Double.valueOf(3),NumberTools.getDouble("3,"));
		assertEquals(Double.valueOf(-3),NumberTools.getDouble("-3."));
		assertEquals(Double.valueOf(-3),NumberTools.getDouble("-3,"));
		assertEquals(Double.valueOf(3.5),NumberTools.getDouble("1+2,5"));
		assertEquals(Double.valueOf(3.5),NumberTools.getDouble("1+2.5"));
		assertEquals(null,NumberTools.getDouble("1+2.5..."));
		assertEquals(null,NumberTools.getDouble("1+2.5,2"));
		assertEquals(Double.valueOf(-1),NumberTools.getDouble("1-2"));
		assertEquals(Double.valueOf(1234),NumberTools.getDouble("1234"));
		assertEquals(Double.valueOf(12345678),NumberTools.getDouble("12345678"));
		assertEquals(Double.valueOf(-1234),NumberTools.getDouble("-1234"));
		assertEquals(Double.valueOf(-12345678),NumberTools.getDouble("-12345678"));
		assertEquals(Double.valueOf(1234.5),NumberTools.getDouble("1234.5"));
		assertEquals(Double.valueOf(12345678.5),NumberTools.getDouble("12345678.5"));
		assertEquals(Double.valueOf(-1234.5),NumberTools.getDouble("-1234.5"));
		assertEquals(Double.valueOf(-12345678.5),NumberTools.getDouble("-12345678.5"));
		assertEquals(Double.valueOf(1.2345678901234568E48),NumberTools.getDouble("01234567890123456789012345678901234567890123456789"));

		TextField f=null;

		assertEquals(null,NumberTools.getDouble(f,false));
		assertEquals(null,NumberTools.getDouble(f,true));

		assertEquals(null,NumberTools.getDouble(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getDouble(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getDouble(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getDouble(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getDouble(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getDouble(f=new TextField("1-2"),true));
		assertEquals(SystemColor.text,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getDouble(j,false));
		assertEquals(null,NumberTools.getDouble(j,true));

		assertEquals(null,NumberTools.getDouble(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getDouble(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getDouble(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getDouble(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getDouble(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(-1),NumberTools.getDouble(j=new JTextField("1-2"),true));
		assertEquals(SystemColor.text,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Double}-Wert.<br>
	 * Einschränkung: Der String muss direkt eine Zahl darstellen. Rechenausdrücke sind hier nicht zulässig.
	 * @see NumberTools#getPlainDouble(String)
	 */
	@Test
	void getPlainDouble() {
		assertEquals(null,NumberTools.getPlainDouble(null));
		assertEquals(null,NumberTools.getPlainDouble("äöü"));
		assertEquals(null,NumberTools.getPlainDouble("1+2"));
		assertEquals(null,NumberTools.getPlainDouble("1,2,3"));
		assertEquals(Double.valueOf(3),NumberTools.getPlainDouble("3"));
		assertEquals(Double.valueOf(3.5),NumberTools.getPlainDouble("3,5"));
		assertEquals(Double.valueOf(3.5),NumberTools.getPlainDouble("3.5"));
		assertEquals(Double.valueOf(-3),NumberTools.getPlainDouble("-3"));
		assertEquals(null,NumberTools.getPlainDouble("--3EEE5"));
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Long}-Wert, der positiv sein muss
	 * @see NumberTools#getPositiveLong(String)
	 * @see NumberTools#getPositiveLong(JTextField, boolean)
	 * @see NumberTools#getPositiveLong(TextField, boolean)
	 */
	@Test
	void getPositiveLong() {
		assertEquals(null,NumberTools.getPositiveLong(null));
		assertEquals(null,NumberTools.getPositiveLong("äöü"));
		assertEquals(Long.valueOf(3),NumberTools.getPositiveLong("1+2"));
		assertEquals(null,NumberTools.getPositiveLong("1+2,5"));
		assertEquals(null,NumberTools.getPositiveLong("1-2"));

		TextField f=null;

		assertEquals(null,NumberTools.getPositiveLong(f,false));
		assertEquals(null,NumberTools.getPositiveLong(f,true));

		assertEquals(null,NumberTools.getPositiveLong(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getPositiveLong(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getPositiveLong(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getPositiveLong(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getPositiveLong(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getPositiveLong(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getPositiveLong(j,false));
		assertEquals(null,NumberTools.getPositiveLong(j,true));

		assertEquals(null,NumberTools.getPositiveLong(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getPositiveLong(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getPositiveLong(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getPositiveLong(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Long.valueOf(3),NumberTools.getPositiveLong(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getPositiveLong(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Double}-Wert mit der Einschränkung, dass der Text eine natürliche Zahl repräsentieren muss
	 * @see NumberTools#getPositiveIntDouble(String)
	 * @see NumberTools#getPositiveIntDouble(JTextField, boolean)
	 * @see NumberTools#getPositiveIntDouble(TextField, boolean)
	 */
	@Test
	void getPositiveIntDouble() {
		assertEquals(null,NumberTools.getPositiveIntDouble(null));
		assertEquals(null,NumberTools.getPositiveIntDouble("äöü"));
		assertEquals(Double.valueOf(3),NumberTools.getPositiveIntDouble("1+2"));
		assertEquals(null,NumberTools.getPositiveIntDouble("1+2,5"));
		assertEquals(null,NumberTools.getPositiveIntDouble("1-2"));

		TextField f=null;

		assertEquals(null,NumberTools.getPositiveIntDouble(f,false));
		assertEquals(null,NumberTools.getPositiveIntDouble(f,true));

		assertEquals(null,NumberTools.getPositiveIntDouble(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveIntDouble(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getPositiveIntDouble(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getPositiveIntDouble(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveIntDouble(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getPositiveIntDouble(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getPositiveIntDouble(j,false));
		assertEquals(null,NumberTools.getPositiveIntDouble(j,true));

		assertEquals(null,NumberTools.getPositiveIntDouble(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveIntDouble(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getPositiveIntDouble(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getPositiveIntDouble(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveIntDouble(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getPositiveIntDouble(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Double}-Wert, der nicht negativ sein darf
	 * @see NumberTools#getNotNegativeDouble(String)
	 * @see NumberTools#getNotNegativeDouble(JTextField, boolean)
	 * @see NumberTools#getNotNegativeDouble(TextField, boolean)
	 */
	@Test
	void getNotNegativeDouble() {
		assertEquals(null,NumberTools.getNotNegativeDouble(null));
		assertEquals(null,NumberTools.getNotNegativeDouble("äöü"));
		assertEquals(Double.valueOf(3),NumberTools.getNotNegativeDouble("1+2"));
		assertEquals(Double.valueOf(3.5),NumberTools.getNotNegativeDouble("1+2,5"));
		assertEquals(null,NumberTools.getNotNegativeDouble("1-2"));
		assertEquals(Double.valueOf(0),NumberTools.getNotNegativeDouble("0"));

		TextField f=null;

		assertEquals(null,NumberTools.getNotNegativeDouble(f,false));
		assertEquals(null,NumberTools.getNotNegativeDouble(f,true));

		assertEquals(null,NumberTools.getNotNegativeDouble(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getNotNegativeDouble(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeDouble(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getNotNegativeDouble(f=new TextField("0"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getNotNegativeDouble(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getNotNegativeDouble(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getNotNegativeDouble(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getNotNegativeDouble(f=new TextField("0"),true));
		assertEquals(SystemColor.text,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getNotNegativeDouble(j,false));
		assertEquals(null,NumberTools.getNotNegativeDouble(j,true));

		assertEquals(null,NumberTools.getNotNegativeDouble(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getNotNegativeDouble(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeDouble(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getNotNegativeDouble(j=new JTextField("0"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getNotNegativeDouble(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getNotNegativeDouble(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getNotNegativeDouble(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getNotNegativeDouble(j=new JTextField("0"),true));
		assertEquals(SystemColor.text,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} in einen {@link Double}-Wert, der positiv sein muss
	 * @see NumberTools#getPositiveDouble(String)
	 * @see NumberTools#getPositiveDouble(JTextField, boolean)
	 * @see NumberTools#getPositiveDouble(TextField, boolean)
	 */
	@Test
	void getPositiveDouble() {
		assertEquals(null,NumberTools.getPositiveDouble(null));
		assertEquals(null,NumberTools.getPositiveDouble("äöü"));
		assertEquals(Double.valueOf(3),NumberTools.getPositiveDouble("1+2"));
		assertEquals(Double.valueOf(3.5),NumberTools.getPositiveDouble("1+2,5"));
		assertEquals(null,NumberTools.getPositiveDouble("1-2"));
		assertEquals(null,NumberTools.getPositiveDouble("0"));

		TextField f=null;

		assertEquals(null,NumberTools.getPositiveDouble(f,false));
		assertEquals(null,NumberTools.getPositiveDouble(f,true));

		assertEquals(null,NumberTools.getPositiveDouble(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveDouble(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(f=new TextField("0"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getPositiveDouble(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveDouble(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(f=new TextField("0"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getPositiveDouble(j,false));
		assertEquals(null,NumberTools.getPositiveDouble(j,true));

		assertEquals(null,NumberTools.getPositiveDouble(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveDouble(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(j=new JTextField("0"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getPositiveDouble(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getPositiveDouble(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(null,NumberTools.getPositiveDouble(j=new JTextField("0"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} der eine Wahrscheinlichkeit (zwischen 0 und 1) darstellt in einen {@link Double}-Wert
	 * @see NumberTools#getProbability(String)
	 * @see NumberTools#getProbability(JTextField, boolean)
	 * @see NumberTools#getProbability(TextField, boolean)
	 */
	@Test
	void getProbability() {
		assertEquals(null,NumberTools.getProbability(null));
		assertEquals(null,NumberTools.getProbability("äöü"));
		assertEquals(null,NumberTools.getProbability("1+2"));
		assertEquals(null,NumberTools.getProbability("1+2,5"));
		assertEquals(null,NumberTools.getProbability("1-2"));
		assertEquals(Double.valueOf(0),NumberTools.getProbability("0"));
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability("0.5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability("0,5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability("1-0.5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability("1-0,5"));
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability("50,2%"));
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability("50.2%"));
		assertEquals(null,NumberTools.getProbability("102%"));
		assertEquals(null,NumberTools.getProbability("-2%"));

		TextField f=null;

		assertEquals(null,NumberTools.getProbability(f,false));
		assertEquals(null,NumberTools.getProbability(f,true));

		assertEquals(null,NumberTools.getProbability(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("1+2,5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getProbability(f=new TextField("0"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("0.5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("0,5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("1-0.5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("1-0,5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(f=new TextField("50,2%"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(f=new TextField("50.2%"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("102%"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("-2%"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getProbability(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("1+2"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("1+2,5"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getProbability(f=new TextField("0"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("0.5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("0,5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("1-0.5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(f=new TextField("1-0,5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(f=new TextField("50,2%"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(f=new TextField("50.2%"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("102%"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(null,NumberTools.getProbability(f=new TextField("-2%"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getProbability(j,false));
		assertEquals(null,NumberTools.getProbability(j,true));

		assertEquals(null,NumberTools.getProbability(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("1+2,5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getProbability(j=new JTextField("0"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("0.5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("0,5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("1-0.5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("1-0,5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(j=new JTextField("50,2%"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(j=new JTextField("50.2%"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("102%"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("-2%"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getProbability(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("1+2"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("1+2,5"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getProbability(j=new JTextField("0"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("0.5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("0,5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("1-0.5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getProbability(j=new JTextField("1-0,5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(j=new JTextField("50,2%"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getProbability(j=new JTextField("50.2%"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("102%"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(null,NumberTools.getProbability(j=new JTextField("-2%"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} der im weitesten Sinne eine Wahrscheinlichkeit darstellt in einen {@link Double}-Wert
	 * @see NumberTools#getExtProbability(String)
	 * @see NumberTools#getExtProbability(JTextField, boolean)
	 * @see NumberTools#getExtProbability(TextField, boolean)
	 */
	@Test
	void getExtProbability() {
		assertEquals(null,NumberTools.getExtProbability(null));
		assertEquals(null,NumberTools.getExtProbability("äöü"));
		assertEquals(Double.valueOf(3),NumberTools.getExtProbability("1+2"));
		assertEquals(Double.valueOf(3.5),NumberTools.getExtProbability("1+2,5"));
		assertEquals(null,NumberTools.getExtProbability("1-2"));
		assertEquals(Double.valueOf(0),NumberTools.getExtProbability("0"));
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability("0.5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability("0,5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability("1-0.5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability("1-0,5"));
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability("50,2%"));
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability("50.2%"));
		assertEquals(Double.valueOf(1.02),NumberTools.getExtProbability("102%"));
		assertEquals(null,NumberTools.getExtProbability("-2%"));

		TextField f=null;

		assertEquals(null,NumberTools.getExtProbability(f,false));
		assertEquals(null,NumberTools.getExtProbability(f,true));

		assertEquals(null,NumberTools.getExtProbability(f=new TextField("äöü"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getExtProbability(f=new TextField("1+2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(3.5),NumberTools.getExtProbability(f=new TextField("1+2,5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getExtProbability(f=new TextField("1-2"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getExtProbability(f=new TextField("0"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("0.5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("0,5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("1-0.5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("1-0,5"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(f=new TextField("50,2%"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(f=new TextField("50.2%"),false));
		assertEquals(null,f.getBackground());
		assertEquals(Double.valueOf(1.02),NumberTools.getExtProbability(f=new TextField("102%"),false));
		assertEquals(null,f.getBackground());
		assertEquals(null,NumberTools.getExtProbability(f=new TextField("-2%"),false));
		assertEquals(null,f.getBackground());

		assertEquals(null,NumberTools.getExtProbability(f=new TextField("äöü"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getExtProbability(f=new TextField("1+2"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(3.5),NumberTools.getExtProbability(f=new TextField("1+2,5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getExtProbability(f=new TextField("1-2"),true));
		assertEquals(Color.RED,f.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getExtProbability(f=new TextField("0"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("0.5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("0,5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("1-0.5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(f=new TextField("1-0,5"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(f=new TextField("50,2%"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(f=new TextField("50.2%"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(Double.valueOf(1.02),NumberTools.getExtProbability(f=new TextField("102%"),true));
		assertEquals(SystemColor.text,f.getBackground());
		assertEquals(null,NumberTools.getExtProbability(f=new TextField("-2%"),true));
		assertEquals(Color.RED,f.getBackground());

		JTextField j=null;

		assertEquals(null,NumberTools.getExtProbability(j,false));
		assertEquals(null,NumberTools.getExtProbability(j,true));

		assertEquals(null,NumberTools.getExtProbability(j=new JTextField("äöü"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getExtProbability(j=new JTextField("1+2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3.5),NumberTools.getExtProbability(j=new JTextField("1+2,5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getExtProbability(j=new JTextField("1-2"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getExtProbability(j=new JTextField("0"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("0.5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("0,5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("1-0.5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("1-0,5"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(j=new JTextField("50,2%"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(j=new JTextField("50.2%"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(1.02),NumberTools.getExtProbability(j=new JTextField("102%"),false));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getExtProbability(j=new JTextField("-2%"),false));
		assertEquals(SystemColor.text,j.getBackground());

		assertEquals(null,NumberTools.getExtProbability(j=new JTextField("äöü"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(3),NumberTools.getExtProbability(j=new JTextField("1+2"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(3.5),NumberTools.getExtProbability(j=new JTextField("1+2,5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getExtProbability(j=new JTextField("1-2"),true));
		assertEquals(Color.RED,j.getBackground());
		assertEquals(Double.valueOf(0),NumberTools.getExtProbability(j=new JTextField("0"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("0.5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("0,5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("1-0.5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.5),NumberTools.getExtProbability(j=new JTextField("1-0,5"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(j=new JTextField("50,2%"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(0.502),NumberTools.getExtProbability(j=new JTextField("50.2%"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(Double.valueOf(1.02),NumberTools.getExtProbability(j=new JTextField("102%"),true));
		assertEquals(SystemColor.text,j.getBackground());
		assertEquals(null,NumberTools.getExtProbability(j=new JTextField("-2%"),true));
		assertEquals(Color.RED,j.getBackground());
	}

	/**
	 * Test: Parsen eines {@link String} der eine Wahrscheinlichkeit (zwischen 0 und 1) in System-Notation (d.h. mit Dezimalpunkt) darstellt in einen {@link Double}-Wert
	 * @see NumberTools#getSystemProbability(String)
	 */
	@Test
	void getSystemProbability() {
		assertEquals(null,NumberTools.getSystemProbability(null));
		assertEquals(null,NumberTools.getSystemProbability("äöü"));
		assertEquals(null,NumberTools.getSystemProbability("1+2"));
		assertEquals(null,NumberTools.getSystemProbability("1+2,5"));
		assertEquals(null,NumberTools.getSystemProbability("1-2"));
		assertEquals(Double.valueOf(0),NumberTools.getSystemProbability("0"));
		assertEquals(Double.valueOf(0.5),NumberTools.getSystemProbability("0.5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getSystemProbability("0,5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getSystemProbability("1-0.5"));
		assertEquals(Double.valueOf(0.5),NumberTools.getSystemProbability("1-0,5"));
		assertEquals(Double.valueOf(0.502),NumberTools.getSystemProbability("50,2%"));
		assertEquals(Double.valueOf(0.502),NumberTools.getSystemProbability("50.2%"));
		assertEquals(null,NumberTools.getSystemProbability("102%"));
		assertEquals(null,NumberTools.getSystemProbability("-2%"));
	}

	/**
	 * Test: Reduktion der Anzahl der Nachkomma-Stellen, so dass ein mehrfaches Umwandeln von
	 * Text in Zahl und zurück zu keinen Veränderungen des Wertes führt.
	 * @see NumberTools#reduceDigits(double, int)
	 */
	@Test
	void reduceDigits() {
		assertEquals(0,NumberTools.reduceDigits(0,5));

		assertEquals(123.0,NumberTools.reduceDigits(123.0,5));
		assertEquals(123.0,NumberTools.reduceDigits(123.0,15));
		assertEquals(123.456,NumberTools.reduceDigits(123.456,15));

		assertEquals(123.0,NumberTools.reduceDigits(123.456,0));
		assertEquals(123.0,NumberTools.reduceDigits(123.456,1));
		assertEquals(123.0,NumberTools.reduceDigits(123.456,2));
		assertEquals(123.0,NumberTools.reduceDigits(123.456,3));
		assertEquals(123.5,NumberTools.reduceDigits(123.456,4));
		assertEquals(123.46,NumberTools.reduceDigits(123.456,5));
		assertEquals(123.456,NumberTools.reduceDigits(123.456,6));
		assertEquals(123.456,NumberTools.reduceDigits(123.456,7));

		assertEquals(-123.0,NumberTools.reduceDigits(-123.456,0));
		assertEquals(-123.0,NumberTools.reduceDigits(-123.456,1));
		assertEquals(-123.0,NumberTools.reduceDigits(-123.456,2));
		assertEquals(-123.0,NumberTools.reduceDigits(-123.456,3));
		assertEquals(-123.5,NumberTools.reduceDigits(-123.456,4));
		assertEquals(-123.46,NumberTools.reduceDigits(-123.456,5));
		assertEquals(-123.456,NumberTools.reduceDigits(-123.456,6));
		assertEquals(-123.456,NumberTools.reduceDigits(-123.456,7));
	}

	/**
	 * Test: Korrekte Dezimaltrenner in den verschiedenen Spracheinstellungen
	 */
	@Test
	void getDecimalSeparator() {
		NumberTools.setLocale(Locale.US);
		assertEquals('.',NumberTools.getDecimalSeparator());
		NumberTools.setLocale(Locale.GERMANY);
		assertEquals(',',NumberTools.getDecimalSeparator());
	}

	/**
	 * Test: Prozent- in Bruchwert umwandeln
	 */
	@Test
	void percentToFractionTest() {
		/* Null-Werte */
		assertEquals("",NumberTools.percentToFraction(null));
		assertEquals("",NumberTools.percentToFraction(""));

		/* Nicht-Prozent-Werte */
		assertEquals("abc",NumberTools.percentToFraction("abc"));
		assertEquals("123,456",NumberTools.percentToFraction("123,456"));
		assertEquals("123.456",NumberTools.percentToFraction("123.456"));

		/* Eigentliche Verarbeitung */
		assertEquals("0,3",NumberTools.percentToFraction("30%"));
		assertEquals("-0,4",NumberTools.percentToFraction("-40%"));
	}
}