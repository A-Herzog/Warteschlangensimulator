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
package tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Diese Klasse stellt statische Funktionen zur Umwandlung
 * von Zeitangaben in Unix-Zeitstempel und umgekehrt zur Verfügung.
 * @author Alexander Herzog
 * @version 2.0
 */
public class DateTools {
	/**
	 * Diese Klasse enthält nur statische Methoden und kann nicht instanziert werden.
	 */
	private DateTools() {
	}

	/**
	 * Zu verwendendes Datums- und Zeitformat (System, lang)
	 */
	private static final String dateFormatStringSystemFull="yyyy-MM-dd HH:mm:ss";

	/**
	 * Zu verwendendes Datums- und Zeitformat (System, kurz)
	 */
	private static final String dateFormatStringSystemShort="yyyy-MM-dd";

	/**
	 * Zu verwendendes Datums- und Zeitformat (Nutzerdefiniertes Format, lang)
	 */
	private static String dateFormatStringUserFull="dd.MM.yyyy HH:mm:ss";

	/**
	 * Zu verwendendes Datums- und Zeitformat (Nutzerdefiniertes Format, kurz)
	 */
	private static String dateFormatStringUserShort="dd.MM.yyyy";

	/**
	 * Stellt eine neue Lokale für die Umwandlung von Datumsangaben in Strings ein.<br>
	 * Unterstützt werden momentan {@link Locale#GERMAN} und international (=englisches Format).
	 * @param locale	Lokale für die Umwandlung von Datumsangaben in Strings
	 */
	public static void setLocale(final Locale locale) {
		if (locale.getLanguage().equals(Locale.GERMAN.getLanguage())) {
			dateFormatStringUserFull="dd.MM.yyyy HH:mm:ss";
			dateFormatStringUserShort="dd.MM.yyyy";
		} else {
			dateFormatStringUserFull="yyyy-MM-dd HH:mm:ss";
			dateFormatStringUserShort="yyyy-MM-dd";
		}
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.
	 * @param ms	Zeitangabe in Millisekunden
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Darstellung der Zeitangabe als Zeichenkette (aus Datum und Uhrzeit) im <b>System</b>-Format
	 */
	public static String formatSystemDate(final long ms, final boolean useTimeZone) {
		final SimpleDateFormat dateFormatSystemFull=new SimpleDateFormat(dateFormatStringSystemFull);
		return dateFormatSystemFull.format(toDate(ms,useTimeZone));
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.<br>
	 * Es wird dabei davon ausgegangen, dass der Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param ms	Zeitangabe in Millisekunden
	 * @return	Darstellung der Zeitangabe als Zeichenkette (aus Datum und Uhrzeit) im <b>System</b>-Format
	 */
	public static String formatSystemDate(final long ms) {
		return formatSystemDate(ms,false);
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.<br>
	 * Es wird dabei davon ausgegangen, dass der Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param ms	Zeitangabe in Millisekunden
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Darstellung der Zeitangabe als Zeichenkette (nur Datum ohne Uhrzeit) im <b>System</b>-Format
	 */
	public static String formatSystemDateShort(final long ms, final boolean useTimeZone) {
		final SimpleDateFormat dateFormatSystemShort=new SimpleDateFormat(dateFormatStringSystemShort);
		return dateFormatSystemShort.format(toDate(ms,useTimeZone));
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.<br>
	 * Es wird dabei davon ausgegangen, dass der Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param ms	Zeitangabe in Millisekunden
	 * @return	Darstellung der Zeitangabe als Zeichenkette (nur Datum ohne Uhrzeit) im <b>System</b>-Format
	 */
	public static String formatSystemDateShort(final long ms) {
		return formatSystemDateShort(ms,false);
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.
	 * @param ms	Zeitangabe in Millisekunden
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Darstellung der Zeitangabe als Zeichenkette (aus Datum und Uhrzeit) im <b>deutschen</b> Format
	 */
	public static String formatUserDate(final long ms, final boolean useTimeZone) {
		final SimpleDateFormat dateFormatUserFull=new SimpleDateFormat(dateFormatStringUserFull);
		return dateFormatUserFull.format(toDate(ms,useTimeZone));
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.<br>
	 * Es wird dabei davon ausgegangen, dass der Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param ms	Zeitangabe in Millisekunden
	 * @return	Darstellung der Zeitangabe als Zeichenkette (aus Datum und Uhrzeit) im <b>deutschen</b> Format
	 */
	public static String formatUserDate(final long ms) {
		return formatUserDate(ms,false);
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.
	 * @param ms	Zeitangabe in Millisekunden
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Darstellung der Zeitangabe als Zeichenkette (nur Datum ohne Uhrzeit) im <b>deutschen</b> Format
	 */
	public static String formatUserDateShort(final long ms, final boolean useTimeZone) {
		final SimpleDateFormat dateFormatUserShort=new SimpleDateFormat(dateFormatStringUserShort);
		return dateFormatUserShort.format(toDate(ms,useTimeZone));
	}

	/**
	 * Wandelt einen Unix-Millisekunden-Zeitstempel in eine Zeichenkette um.<br>
	 * Es wird dabei davon ausgegangen, dass der Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param ms	Zeitangabe in Millisekunden
	 * @return	Darstellung der Zeitangabe als Zeichenkette (nur Datum ohne Uhrzeit) im <b>deutschen</b> Format
	 */
	public static String formatUserDateShort(final long ms) {
		return formatUserDateShort(ms,false);
	}

	/**
	 * Interpretiert eine Zeichenkette als System-Datumsangabe und liefert
	 * die Zeit als Millisekunden-Unix-Zeitstempel zurück.
	 * @param dateString	Zu interpretierende Zeichenkette
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Liefert die Millisekunden-Unix-Zeitstempel zurück oder -1, wenn die Zeichenkette nicht interpretiert werden konnte
	 */
	public static long getSystemDate(final String dateString, final boolean useTimeZone) {
		try {
			final SimpleDateFormat dateFormatSystemFull=new SimpleDateFormat(dateFormatStringSystemFull);
			final Date date=dateFormatSystemFull.parse(dateString);
			return toMS(date,useTimeZone);
		} catch (ParseException e) {
			return -1;
		}
	}

	/**
	 * Interpretiert eine Zeichenkette als System-Datumsangabe und liefert
	 * die Zeit als Millisekunden-Unix-Zeitstempel zurück.<br>
	 * Es wird dabei davon ausgegangen, dass der Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param dateString	Zu interpretierende Zeichenkette
	 * @return	Liefert die Millisekunden-Unix-Zeitstempel zurück oder -1, wenn die Zeichenkette nicht interpretiert werden konnte
	 */
	public static long getSystemDate(final String dateString) {
		return getSystemDate(dateString,false);
	}

	/**
	 * Interpretiert eine Zeichenkette als deutsche Datumsangabe und liefert
	 * die Zeit als Millisekunden-Unix-Zeitstempel zurück.
	 * @param dateString	Zu interpretierende Zeichenkette
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Liefert die Millisekunden-Unix-Zeitstempel zurück oder -1, wenn die Zeichenkette nicht interpretiert werden konnte
	 */
	public static long getUserDate(final String dateString, final boolean useTimeZone) {
		try {
			final SimpleDateFormat dateFormatUserFull=new SimpleDateFormat(dateFormatStringUserFull);
			final Date date=dateFormatUserFull.parse(dateString);
			return toMS(date,useTimeZone);
		} catch (ParseException e) {
			return -1;
		}
	}

	/**
	 * Interpretiert eine Zeichenkette als deutsche Datumsangabe und liefert
	 * die Zeit als Millisekunden-Unix-Zeitstempel zurück.<br>
	 * Es wird dabei davon ausgegangen, dass der Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param dateString	Zu interpretierende Zeichenkette
	 * @return	Liefert die Millisekunden-Unix-Zeitstempel zurück oder -1, wenn die Zeichenkette nicht interpretiert werden konnte
	 */
	public static long getUserDate(final String dateString) {
		return getUserDate(dateString,false);
	}

	/**
	 * Liefert die aktuelle Zeit als Unix-Millisekundenwert
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Aktuelle Zeit
	 */
	public static long getNow(final boolean useTimeZone) {
		return toMS(new Date(),useTimeZone);
	}

	/**
	 * Wandelt eine {@link Date}-Objekt wieder in einen Millisekunden-Wert um.<br>
	 * Es ist dabei immer 0 = 1.1.70 00:00:00.
	 * @param date	Umzuwandelndes Objekt
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Millisekunden seit dem 1.1.70 00:00:00
	 */
	public static long toMS(final Date date, final boolean useTimeZone) {
		if (date==null) return 0;
		final long offset=(useTimeZone)?0:TimeZone.getDefault().getRawOffset();
		return date.getTime()+offset;
	}

	/**
	 * Wandelt eine {@link Date}-Objekt wieder in einen Millisekunden-Wert um.<br>
	 * Unabhängig von der lokalen Zeitzone ist dabei immer 0 = 1.1.70 00:00:00.
	 * @param date	Umzuwandelndes Objekt
	 * @return	Millisekunden seit dem 1.1.70 00:00:00
	 */
	public static long toMS(final Date date) {
		return toMS(date,false);
	}

	/**
	 * Wandelt einen Wert in Millisekunden seit dem 1.1.70 00:00:00 in ein
	 * {@link Date}-Objekt um.
	 * @param ms	Millisekunden seit dem 1.1.70 00:00:00
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Entsprechendes <code>Date</code>-Objekt
	 */
	public static Date toDate(final long ms, final boolean useTimeZone) {
		final Date date=new Date();
		final long offset=(useTimeZone)?0:TimeZone.getDefault().getRawOffset();
		date.setTime(ms-offset);
		return date;
	}

	/**
	 * Wandelt einen Wert in Millisekunden seit dem 1.1.70 00:00:00 in ein
	 * {@link Date}-Objekt um. Es wird dabei davon ausgegangen, dass der
	 * Nutzer in der UTC-Zeitzone ist, also es findet keine Verschiebung statt.
	 * @param ms	Millisekunden seit dem 1.1.70 00:00:00
	 * @return	Entsprechendes <code>Date</code>-Objekt
	 */
	public static Date toDate(final long ms) {
		return toDate(ms,false);
	}

	/**
	 * Zerlegt ein {@link Date}-Objekt in Datum und Uhrzeit
	 * @param date	Zu zerlegendes Objekt
	 * @return	Array aus zwei Elementen: Datum, Uhrzeit
	 */
	public static Date[] split(final Date date) {
		final GregorianCalendar calendar=new GregorianCalendar();

		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY,0);
		calendar.set(Calendar.MINUTE,0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MILLISECOND,0);
		final Date day=calendar.getTime();

		calendar.setTime(date);
		calendar.set(Calendar.YEAR,1970);
		calendar.set(Calendar.MONTH,0);
		calendar.set(Calendar.DATE,1);
		final Date time=calendar.getTime();

		return new Date[]{day,time};
	}

	/**
	 * Zerlegt einen Millisekundenwert in zwei {@link Date}-Objekte, die Datum und Uhrzeit enthalten
	 * @param ms	Zu zerlegender Millisekundenwert
	 * @param useTimeZone	Lokale Zeitzone verwenden (<code>true</code>) oder UTC für den Millisekundenwert annehmen (<code>false</code>)
	 * @return	Array aus zwei Elementen: Datum, Uhrzeit
	 */
	public static Date[] split(final long ms, final boolean useTimeZone) {
		return split(toDate(ms,useTimeZone));
	}

	/**
	 * Zerlegt einen Millisekundenwert in zwei {@link Date}-Objekte, die Datum und Uhrzeit enthalten
	 * @param ms	Zu zerlegender Millisekundenwert
	 * @return	Array aus zwei Elementen: Datum, Uhrzeit
	 */
	public static Date[] split(final long ms) {
		return split(ms,false);
	}
}