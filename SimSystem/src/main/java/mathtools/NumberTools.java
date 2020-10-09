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
package mathtools;

import java.awt.Color;
import java.awt.SystemColor;
import java.awt.TextField;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JTextField;

import org.apache.commons.math3.util.FastMath;

import parser.CalcSystem;
import parser.MathCalcError;

/**
 * Enthält einige statische Routinen zur Umwandlung von Zeichenketten in Zahlen
 * und umgekehrt.
 * @author Alexander Herzog
 * @version 2.7
 */
public final class NumberTools {
	/** String, der "0" enthält (um diesen nicht mehrfach anlegen zu müssen) */
	private static final String nullString="0";
	/** Ausgabeformat bei der Umwandlung von Zahlen in Texte unter Nutzung der maximal möglichen Anzahl an Nachkommastellen */
	private static final String formatFloat14Digits="%.14f";
	/** 1. Zeichen, das als Dezimaltrenner beim Umwandeln von Zeichenketten in Zahlen erkannt werden soll (unabhängig von den Spracheinstellungen) */
	private static final String separator1=",";
	/** 2. Zeichen, das als Dezimaltrenner beim Umwandeln von Zeichenketten in Zahlen erkannt werden soll (unabhängig von den Spracheinstellungen) */
	private static final String separator2=".";
	/** Prozentzeichen */
	private static final String percentString="%";

	/** Zu verwendende Spracheinstellung */
	private static Locale activeLocale=Locale.getDefault();
	/** Dezimaltrenner (als char) gemäß der aktuellen Spracheinstellung */
	private static char activeSeparator;
	/** Dezimaltrenner (als String) gemäß der aktuellen Spracheinstellung */
	private static String activeSeparatorString;
	/** 100er-Trenner gemäß der aktuellen Spracheinstellung */
	private static char activeGrouping;

	/**
	 * Cache für String-Objekte die Umwandlung von Ganzzahlen in Strings
	 * @see #formatLong(long)
	 * @see #formatLong(long, StringBuilder)
	 * @see #formatLongAndAppendToBuilder(long, StringBuilder)
	 */
	private static String[] longCache;

	/**
	 * Cache für String-Objekte die Umwandlung von Ganzzahlen in Strings (ohne 1000er-Trennzeichen)
	 * @see #formatLongNoGrouping(long)
	 * @see #formatLongNoGrouping(long, StringBuilder)
	 */
	private static String[] longCacheNoGrouping;

	/** Plus unendlich */
	private static final String plusInifityString="Infinity";
	/** Minus unendlich */
	private static final String minusInifityString="-Infinity";

	/**
	 * Diese Klasse kann nicht instanziert werden.
	 */
	private NumberTools() {
	}

	/**
	 * Liefert die aktuell eingestellte Locale
	 * @return	Aktuell eingestellte Locale
	 */
	public static Locale getLocale() {
		return activeLocale;
	}

	/**
	 * Liefert den gemäß der aktuellen Locale eingestellten Dezimalseparator
	 * @return	Aktuell eingestellter Dezimalseparator
	 */
	public static char getDecimalSeparator() {
		return activeSeparator;
	}

	/**
	 * Sprachunabhängige (d.h. sich nicht im laufenden Betrieb ändernde)
	 * Initialisierungen
	 */
	static {
		longCacheNoGrouping=null;
		final String[] cache=new String[10_000];
		final StringBuilder recycleStringBuilder=new StringBuilder();
		for (int i=0;i<cache.length;i++) cache[i]=formatLongNoGrouping(i,recycleStringBuilder);
		longCacheNoGrouping=cache;
	}

	/**
	 * Stellt eine neue Lokale für die Umwandlung von Zahlen in Strings (Dezimalkomma oder Dezimalpunkt usw.) ein
	 * @param locale	Lokale für die Umwandlung von Zahlen in Strings
	 */
	public static void setLocale(final Locale locale) {
		activeLocale=locale;

		final DecimalFormatSymbols format=new DecimalFormatSymbols(activeLocale);
		activeSeparator=format.getDecimalSeparator();
		activeSeparatorString=Character.toString(activeSeparator);
		activeGrouping=format.getGroupingSeparator();

		longCache=null;
		final String[] cache=new String[100_000];
		for (int i=0;i<cache.length;i++) cache[i]=formatLong(i);
		longCache=cache;
	}

	static {
		setLocale(activeLocale);
	}

	/**
	 * Liefert die Anzahl an Ziffern einer positiven Ganzzahl
	 * @param l	Positive Ganzzahl bei der die Anzahl an Ziffern bestimmt werden soll
	 * @return	Anzahl an Ziffern der Zahl
	 */
	private static int getDigits(final long l) {
		if (l<10) return 1;
		if (l<100) return 2;
		if (l<1_000) return 3;
		if (l<10_000) return 4;
		if (l<100_000) return 5;
		if (l<1_000_000) return 6;
		if (l<10_000_000) return 7;
		if (l<100_000_000) return 8;
		if (l<1_000_000_000) return 9;
		if (l<10_000_000_000L) return 10;
		if (l<100_000_000_000L) return 11;
		if (l<1_000_000_000_000L) return 12;
		if (l<10_000_000_000_000L) return 13;
		if (l<100_000_000_000_000L) return 14;
		if (l<1_000_000_000_000_000L) return 15;
		if (l<10_000_000_000_000_000L) return 16;
		if (l<100_000_000_000_000_000L) return 17;
		if (l<1_000_000_000_000_000_000L) return 18;
		return 19;
		/* gleichwertig zu: (int)Math.ceil(Math.log10(l+1)), aber braucht keinen Speicher */
	}

	/**
	 * Wandelt eine Ganzzahl in eine Zeichenkette um und fügt in den Ergebnisstring dabei 1000er-Punkte ein.
	 * @param l	Umzuwandelnde Zahl
	 * @return	Zahl als Zeichenkette
	 */
	public static String formatLong(long l) {
		final boolean minus=(l<0);
		if (minus) l=-l;

		/* sollte eigentlich aus dem Cache kommen, braucht aber dennoch viel Speicher: if (l<1000) return Long.toString(minus?-l:l); */
		if (!minus && longCache!=null && l<longCache.length) return longCache[(int)l]; /* Da formatLong verwendet wird, um den Cache aufzubauen, müssen wir longCache!=null prüfen. */

		int digits=getDigits(l);
		final StringBuilder sb=new StringBuilder(digits*4/3+(minus?1:0));
		if (minus) sb.append('-');
		while (digits>0) {
			long firstDigit=l;
			for (int i=0;i<digits-1;i++) firstDigit/=10;
			byte b=(byte)(firstDigit+48);
			long firstDigitNumber=firstDigit;
			for (int i=0;i<digits-1;i++) firstDigitNumber*=10;
			sb.append((char)b);
			digits--;
			if (digits%3==0 && digits>0) sb.append(activeGrouping);
			l-=firstDigitNumber;
		}
		return sb.toString();

		/* Auch ok, braucht aber mehr Arbeitsspeicher: */
		/*
		String s=Long.toString(l);
		String t;
		t="";
		while (!s.isEmpty()) {
			String u=s.substring(Math.max(0,s.length()-1-2));
			if (!t.isEmpty()) t=lastGrouping+t;
			t=u+t;
			if (s.length()-1-2>=0) s=s.substring(0,s.length()-1-2); else break;
		}
		t=(minus)?"-"+t:t;
		 */
	}

	/**
	 * Wandelt eine Ganzzahl in eine Zeichenkette um. Es werden dabei keine 1000er-Punkte eingefügt.
	 * @param l	Umzuwandelnde Zahl
	 * @return	Zahl als Zeichenkette
	 */
	public static String formatLongNoGrouping(long l) {
		final boolean minus=(l<0);
		if (minus) l=-l;

		/* sollte eigentlich aus dem Cache kommen, braucht aber dennoch viel Speicher: if (l<1000) return Long.toString(minus?-l:l); */
		if (!minus && longCacheNoGrouping!=null && l<longCacheNoGrouping.length) return longCacheNoGrouping[(int)l]; /* Da formatLongNoGrouping verwendet wird, um den Cache aufzubauen, müssen wir longCacheNoGrouping!=null prüfen. */

		int digits=getDigits(l);
		final StringBuilder sb=new StringBuilder(digits+(minus?1:0));
		if (minus) sb.append('-');
		while (digits>0) {
			long firstDigit=l;
			for (int i=0;i<digits-1;i++) firstDigit/=10;
			byte b=(byte)(firstDigit+48);
			long firstDigitNumber=firstDigit;
			for (int i=0;i<digits-1;i++) firstDigitNumber*=10;
			sb.append((char)b);
			digits--;
			l-=firstDigitNumber;
		}
		return sb.toString();
	}

	/**
	 * Wandelt eine Ganzzahl in eine Zeichenkette um. Es werden dabei keine 1000er-Punkte eingefügt.
	 * @param l	Umzuwandelnde Zahl
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return	Zahl als Zeichenkette
	 */
	public static String formatLongNoGrouping(long l, final StringBuilder recycleStringBuilder) {
		if (recycleStringBuilder==null) return formatLongNoGrouping(l);

		final boolean minus=(l<0);
		if (minus) l=-l;

		/* sollte eigentlich aus dem Cache kommen, braucht aber dennoch viel Speicher: if (l<1000) return Long.toString(minus?-l:l); */
		if (!minus && longCacheNoGrouping!=null && l<longCacheNoGrouping.length) return longCacheNoGrouping[(int)l]; /* Da formatLongNoGrouping verwendet wird, um den Cache aufzubauen, müssen wir longCacheNoGrouping!=null prüfen. */

		recycleStringBuilder.setLength(0);
		int digits=getDigits(l);
		if (minus) recycleStringBuilder.append('-');
		while (digits>0) {
			long firstDigit=l;
			for (int i=0;i<digits-1;i++) firstDigit/=10;
			byte b=(byte)(firstDigit+48);
			long firstDigitNumber=firstDigit;
			for (int i=0;i<digits-1;i++) firstDigitNumber*=10;
			recycleStringBuilder.append((char)b);
			digits--;
			l-=firstDigitNumber;
		}
		return recycleStringBuilder.toString();
	}

	/**
	 * Wandelt eine Ganzzahl in eine Zeichenkette um und fügt in den Ergebnisstring dabei 1000er-Punkte ein.
	 * @param l	Umzuwandelnde Zahl
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return	Zahl als Zeichenkette
	 */
	public static String formatLong(long l, final StringBuilder recycleStringBuilder) {
		if (recycleStringBuilder==null) return formatLong(l);

		final boolean minus=(l<0);
		if (minus) l=-l;

		/* sollte eigentlich aus dem Cache kommen, braucht aber dennoch viel Speicher: if (l<1000) return Long.toString(minus?-l:l); */
		if (!minus && l<longCache.length) return longCache[(int)l]; /* longCache!=null ist hier (im Gegensatz zu oben) garantiert. */

		recycleStringBuilder.setLength(0);
		int digits=getDigits(l);
		if (minus) recycleStringBuilder.append('-');
		while (digits>0) {
			long firstDigit=l;
			for (int i=0;i<digits-1;i++) firstDigit/=10;
			byte b=(byte)(firstDigit+48);
			long firstDigitNumber=firstDigit;
			for (int i=0;i<digits-1;i++) firstDigitNumber*=10;
			recycleStringBuilder.append((char)b);
			digits--;
			if (digits%3==0 && digits>0) recycleStringBuilder.append(activeGrouping);
			l-=firstDigitNumber;
		}
		return recycleStringBuilder.toString();
	}

	/**
	 * Wandelt eine Ganzzahl in eine Zeichenkette um und fügt in den Ergebnisstring dabei 1000er-Punkte ein.
	 * @param l	Umzuwandelnde Zahl
	 * @param stringBuilder	StringBuilder an den die Ausgabe angehängt werden soll
	 */
	public static void formatLongAndAppendToBuilder(long l, final StringBuilder stringBuilder) {
		final boolean minus=(l<0);
		if (minus) l=-l;

		/* sollte eigentlich aus dem Cache kommen, braucht aber dennoch viel Speicher: if (l<1000) return Long.toString(minus?-l:l); */
		if (!minus && l<longCache.length) {
			stringBuilder.append(longCache[(int)l]); /* longCache!=null ist hier (im Gegensatz zu oben) garantiert. */
			return;
		}

		int digits=getDigits(l);
		if (minus) stringBuilder.append('-');
		while (digits>0) {
			long firstDigit=l;
			for (int i=0;i<digits-1;i++) firstDigit/=10;
			byte b=(byte)(firstDigit+48);
			long firstDigitNumber=firstDigit;
			for (int i=0;i<digits-1;i++) firstDigitNumber*=10;
			stringBuilder.append((char)b);
			digits--;
			if (digits%3==0 && digits>0) stringBuilder.append(activeGrouping);
			l-=firstDigitNumber;
		}
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * und fügt in den Ergebnisstring dabei 1000er-Punkte ein.
	 * @param d	Umzuwandelnde Zahl
	 * @return	Zahl als Zeichenkette
	 */
	public static String formatNumberLong(double d) {
		boolean minus=(d<0);
		d=Math.abs(d);
		long l=Math.round(Math.floor(d));
		long frac=Math.round((d-Math.floor(d))*10);
		if (frac>9) {l++; frac=0;}
		String s=formatLong(l);
		if (frac!=0) {
			final DecimalFormatSymbols format=new DecimalFormatSymbols(activeLocale);
			s+=format.getDecimalSeparator()+(""+frac);
		}
		if (minus) s="-"+s;
		return s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumber(double d) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d));
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d));
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}
		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			return formatLongNoGrouping(Math.round(Math.floor(d)*(minus?-1:1)));
		} else {
			final long l=Math.round(d*10);
			if (l==0) return nullString;
			if (l%10==0) return formatLongNoGrouping((l/10)*(minus?-1:1));
			if (l/10==0) return (minus?"-0":"0")+activeSeparator+((char)(((byte)'0')+l%10));
			return formatLongNoGrouping((l/10)*(minus?-1:1))+activeSeparator+((char)(((byte)'0')+l%10));

			/*
			langsamer und speicherintensiver:
			s=String.format(ActiveLocale,formatFloat1Digit,d);
			while (((s.contains(separator1)) || (s.contains(separator2))) && (s.endsWith(nullString))) {s=s.substring(0,s.length()-1);}
			if ((s.endsWith(separator2)) || (s.endsWith(separator1))) s=s.substring(0,s.length()-1);
			 */
		}
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumber(double d, final StringBuilder recycleStringBuilder) {
		if (recycleStringBuilder==null) return formatNumber(d);

		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}
		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			return formatLongNoGrouping(Math.round(Math.floor(d))*(minus?-1:1),recycleStringBuilder);
		} else {
			final long l=Math.round(d*10);
			final String s=formatLongNoGrouping((l/10)*(minus?-1:1),recycleStringBuilder);
			recycleStringBuilder.setLength(0);
			recycleStringBuilder.append(s);
			final long digit=l%10;
			if (digit!=0) {
				recycleStringBuilder.append(activeSeparator);
				recycleStringBuilder.append((char)(((byte)'0')+digit));
			}
			return recycleStringBuilder.toString();
		}
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform).
	 * Es wird die maximal mögliche Anzahl an Nachkommastellen ausgegeben.
	 * @param d Umzuwandelnde Zahl
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumberMax(double d) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d));
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d));
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}
		String s=String.format(activeLocale,formatFloat14Digits,d);
		if (s.contains(activeSeparatorString)) {
			int len=s.length();
			while (s.charAt(len-1)=='0') len--;
			if (s.charAt(len-1)==activeSeparator) len--;
			if (len<s.length()) s=s.substring(0,len);
		}
		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform).
	 * Es wird die maximal mögliche Anzahl an Nachkommastellen ausgegeben.
	 * @param d Umzuwandelnde Zahl
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumberMax(double d, final StringBuilder recycleStringBuilder) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}
		String s=String.format(activeLocale,formatFloat14Digits,d);
		if (s.contains(activeSeparatorString)) {
			int len=s.length();
			while (s.charAt(len-1)=='0') len--;
			if (s.charAt(len-1)==activeSeparator) len--;
			if (len<s.length()) s=s.substring(0,len);
		}
		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @param n Anzahl an Nachkommastellen
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumber(double d, final int n) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d));
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d));
			}
		}

		if (n==1) return formatNumber(d);

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}

		String s;

		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			s=formatLongNoGrouping(Math.round(Math.floor(d)));
		} else {
			s=String.format(activeLocale,"%."+n+"f",d);
			if (s.contains(activeSeparatorString)) {
				int len=s.length();
				while (s.charAt(len-1)=='0') len--;
				if (s.charAt(len-1)==activeSeparator) len--;
				if (len<s.length()) s=s.substring(0,len);
			}
		}

		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @param n Anzahl an Nachkommastellen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumber(double d, final int n, final StringBuilder recycleStringBuilder) {
		if (recycleStringBuilder==null) return formatNumber(d,n);

		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			}
		}

		if (n==1) return formatNumber(d,recycleStringBuilder);

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}

		String s;

		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			s=formatLongNoGrouping(Math.round(Math.floor(d)),recycleStringBuilder);
		} else {
			s=String.format(activeLocale,"%."+n+"f",d);
			if (s.contains(activeSeparatorString)) {
				int len=s.length();
				while (s.charAt(len-1)=='0') len--;
				if (s.charAt(len-1)==activeSeparator) len--;
				if (len<s.length()) s=s.substring(0,len);
			}
		}

		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(double d) {
		return formatNumber(d*100)+percentString;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @param n Anzahl an Nachkommastellen
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(double d, final int n) {
		if (n==1) return formatNumber(d*100)+percentString;
		return formatNumber(d*100,n)+percentString;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(double d, final StringBuilder recycleStringBuilder) {
		final String s=formatNumber(d*100,recycleStringBuilder);
		if (recycleStringBuilder==null) return s+percentString;
		recycleStringBuilder.setLength(0);
		recycleStringBuilder.append(s);
		recycleStringBuilder.append(percentString);
		return recycleStringBuilder.toString();
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param d Umzuwandelnde Zahl
	 * @param n Anzahl an Nachkommastellen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(double d, final int n, final StringBuilder recycleStringBuilder) {
		final String s;
		if (n==1) s=formatNumber(d*100,recycleStringBuilder); else s=formatNumber(d*100,n,recycleStringBuilder);

		if (recycleStringBuilder==null) return s+percentString;

		recycleStringBuilder.setLength(0);
		recycleStringBuilder.append(s);
		recycleStringBuilder.append(percentString);
		return recycleStringBuilder.toString();
	}

	/**
	 * Wandelt eine Fließkommazahl in eine System-Zeichenkette um, d.h. ohne Berücksichtigung lokaler
	 * Darstellungsformen
	 * @param d Umzuwandelnde Zahl
	 * @return Zahl als Zeichenkette
	 */
	public static String formatSystemNumber(double d) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d));
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d));
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}

		String s;

		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			s=""+Math.round(Math.floor(d));
		} else {
			s=String.format(Locale.US,formatFloat14Digits,d);
			if (s.contains(separator2)) {
				int len=s.length();
				while (s.charAt(len-1)=='0') len--;
				final char c=s.charAt(len-1);
				if (c=='.') len--;
				if (len<s.length()) s=s.substring(0,len);
			}
		}

		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine System-Zeichenkette um, d.h. ohne Berücksichtigung lokaler
	 * Darstellungsformen
	 * @param d Umzuwandelnde Zahl
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Zahl als Zeichenkette
	 */
	public static String formatSystemNumber(double d, final StringBuilder recycleStringBuilder) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}

		String s;

		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			s=""+Math.round(Math.floor(d));
		} else {
			s=String.format(Locale.US,formatFloat14Digits,d);
			if (s.contains(separator2)) {
				int len=s.length();
				while (s.charAt(len-1)=='0') len--;
				final char c=s.charAt(len-1);
				if (c=='.') len--;
				if (len<s.length()) s=s.substring(0,len);
			}
		}

		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine System-Zeichenkette um, d.h. ohne Berücksichtigung lokaler
	 * Darstellungsformen
	 * @param d Umzuwandelnde Zahl
	 * @param n Anzahl an Nachkommastellen
	 * @return Zahl als Zeichenkette
	 */
	public static String formatSystemNumber(double d, final int n) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d));
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d));
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}

		String s;

		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			s=""+Math.round(Math.floor(d));
		} else {
			s=String.format(Locale.US,"%."+n+"f",d);
			if (s.contains(separator2)) {
				int len=s.length();
				while (s.charAt(len-1)=='0') len--;
				final char c=s.charAt(len-1);
				if (c=='.') len--;
				if (len<s.length()) s=s.substring(0,len);
			}
		}

		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine Fließkommazahl in eine System-Zeichenkette um, d.h. ohne Berücksichtigung lokaler
	 * Darstellungsformen
	 * @param d Umzuwandelnde Zahl
	 * @param n Anzahl an Nachkommastellen
	 * @param recycleStringBuilder	StringBuilder, der zum Erstellen der Zeichenkette wiederverwendet werden soll
	 * @return Zahl als Zeichenkette
	 */
	public static String formatSystemNumber(double d, final int n, final StringBuilder recycleStringBuilder) {
		if (Math.abs(d)<10E-16) return nullString;

		if (d%1==0.0) {
			if (d>=0) {
				if (d<=1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			} else {
				if (d>-1_000_000_000) return formatLongNoGrouping(Math.round(d),recycleStringBuilder);
			}
		}

		boolean minus=false;
		if (d<0) {minus=true; d=-d;}

		String s;

		if (Math.abs(Math.floor(d)-d)<10E-16 && d<2_000_000_000) {
			s=""+Math.round(Math.floor(d));
		} else {
			s=String.format(Locale.US,"%."+n+"f",d);
			if (s.contains(separator2)) {
				int len=s.length();
				while (s.charAt(len-1)=='0') len--;
				final char c=s.charAt(len-1);
				if (c=='.') len--;
				if (len<s.length()) s=s.substring(0,len);
			}
		}

		return minus?("-"+s):s;
	}

	/**
	 * Wandelt eine als String gegebene lokale (d.h. mit "," als Dezimaltrenner) Fließkommazahl
	 * in eine System-Fließkommazahl (mit "." als Dezimaltrenner) um und gibt diese wieder
	 * als String zurück.
	 * @param number	Umzuwandelnde Fließkommazahl als String mit "," als Dezimaltrenner
	 * @return Fließkommazahl als String mit "." als Dezimaltrenner
	 */
	public static String localNumberToSystemNumber(final String number) {
		if (number==null || number.isEmpty()) return "";

		/* Zeiten nicht verändern */
		int countCollon=0;
		for (int i=0;i<number.length();i++) if (number.charAt(i)==':') countCollon++;
		if (countCollon==2) return number;

		/* Zahlen anpassen */
		final boolean percent=number.endsWith(percentString);
		final Double D=getDouble(percent?number.substring(0,number.length()-1):number);
		if (D==null) return number;
		String s=D.toString();
		if (s.contains(separator1) || s.contains(separator2)) {
			int len=s.length();
			while (s.charAt(len-1)=='0') len--;
			if (s.charAt(len-1)==',' || s.charAt(len-1)=='.') len--;
			if (len<s.length()) s=s.substring(0,len);
		}
		return percent?(s+percentString):s;
	}

	/**
	 * Prüft, ob es sich um eine Prozentangabe handelt und wenn ja wandelt
	 * es diese in einen Bruchwert um.
	 * @param number	Zu prüfende Zahl als String
	 * @return	Zahl als String ggf. ohne Prozentangabe
	 */
	public static String percentToFraction(final String number) {
		if (number==null || number.isEmpty()) return "";
		if (!number.contains("%")) return number;

		final Double D=getDouble(number);
		if (D==null) return null;

		return formatNumberMax(D.doubleValue());
	}

	/**
	 * Wandelt eine als String gegebene System-Fließkommazahl (d.h. mit "." als Dezimaltrenner)
	 * in eine lokale Fließkommazahl (mit "," als Dezimaltrenner) um und gibt diese wieder
	 * als String zurück
	 * @param number	Umzuwandelnde Fließkommazahl als String mit "." als Dezimaltrenner
	 * @return Fließkommazahl als String mit "," als Dezimaltrenner
	 */
	public static String systemNumberToLocalNumber(final String number) {
		final boolean percent=number.endsWith(percentString);
		String s=number;
		if (percent) s=s.substring(0,number.length()-1);
		final Double D=getDouble(s); if (D==null) return number;
		s=formatNumberMax(D);
		return percent?(s+percentString):s;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Integer</code>-Zahl umzuwandeln.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getInteger(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((Math.round(d)!=d)) return null;
		return ((int)Math.round(d));
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Integer</code>-Zahl umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getInteger(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Integer i=getInteger(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return i;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Integer</code>-Zahl umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getInteger(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Integer i=getInteger(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return i;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Integer</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getNotNegativeInteger(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((Math.round(d)!=d)) return null;
		if (d<0) return null;
		return ((int)Math.round(d));
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Integer</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getNotNegativeInteger(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Integer i=getNotNegativeInteger(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return i;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Integer</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getNotNegativeInteger(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Integer i=getNotNegativeInteger(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return i;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Short</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Short</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Short getNotNegativeShort(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((Math.round(d)!=d)) return null;
		if (d<0 || d>32767) return null;
		return ((short)Math.round(d));
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Short</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Short getNotNegativeShort(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Short i=getNotNegativeShort(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return i;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Short</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Short</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Short getNotNegativeShort(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Short i=getNotNegativeShort(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return i;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Long</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getNotNegativeLong(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((Math.round(d)!=d)) return null;
		if (d<0) return null;
		return Math.round(d);
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Long</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getNotNegativeLong(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Long l=getNotNegativeLong(field.getText());
		if (setColor) {
			if (l==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return l;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Long</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getNotNegativeLong(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Long l=getNotNegativeLong(field.getText());
		if (setColor) {
			if (l==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return l;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Long</code>-Zahl umzuwandeln.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getLong(final String s) {
		if (s==null) return null;

		/* Bei zu langen Zahlen ist getDouble ungenau. */
		if (s.length()>12) {
			try {
				return Long.valueOf(s);
			} catch (NumberFormatException e) {return null;}
		}

		final Double d=getDouble(s); if (d==null) return null;
		if ((Math.round(d)!=d)) return null;
		return Math.round(d);
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Long</code>-Zahl umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getLong(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Long l=getLong(field.getText());
		if (setColor) {
			if (l==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return l;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Long</code>-Zahl umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getLong(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Long l=getLong(field.getText());
		if (setColor) {
			if (l==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return l;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur ganzzahlige Werte.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getIntDouble(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((Math.round(d)!=d)) return null;
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur ganzzahlige Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getIntDouble(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getIntDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur ganzzahlige Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getIntDouble(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getIntDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Cache für positive Double-Werte, um das Boxing/Unboxing zu vermeiden
	 */
	public static final Double[] fastPositiveResults;

	/**
	 * Cache für negative Double-Werte, um das Boxing/Unboxing zu vermeiden
	 */
	public static final Double[] fastNegativeResults;

	/**
	 * Cache für Double-Werte, die positive Dezimalbrüche darstellen, um das Boxing/Unboxing zu vermeiden
	 */
	public static final Double[] fastPositiveFractionalResults;

	static {
		fastPositiveResults=new Double[4096];
		fastNegativeResults=new Double[4096];
		for (int i=0;i<fastPositiveResults.length;i++) {
			fastPositiveResults[i]=((double)i);
			fastNegativeResults[i]=((double)(-i));
		}
		fastPositiveFractionalResults=new Double[128*1000];
		for (int i=0;i<fastPositiveFractionalResults.length;i++) fastPositiveFractionalResults[i]=i/1000.0;
	}

	/**
	 * Packt eine Zahl in ein {@link Double}-Objekt ein und versucht dabei
	 * Objekte zu recyclen.
	 * @param value	Zu verpackende Zahl
	 * @return	Zahl in einem {@link Double}-Objekt
	 */
	public static Double fastBoxedValue(final long value) {
		if (value>0) {
			if (value>=fastPositiveResults.length) return ((double)value);
			return fastPositiveResults[(int)value];
		} else {
			if ((-value)>=fastNegativeResults.length) return ((double)value);
			return fastNegativeResults[(int)(-value)];
		}
	}

	/**
	 * Packt eine Zahl in ein {@link Double}-Objekt ein und versucht dabei
	 * Objekte zu recyclen.
	 * @param value	Zu verpackende Zahl
	 * @return	Zahl in einem {@link Double}-Objekt
	 */
	public static Double fastBoxedValue(final double value) {
		if (FastMath.floor(value)!=value) {
			if (value>0) {
				final double scaled=value*1000;
				if(scaled%1==0) {
					final long scaledIndex=(long)FastMath.floor(scaled);
					if (scaledIndex<fastPositiveFractionalResults.length)
						return fastPositiveFractionalResults[(int)scaledIndex];
				}
			}
			return value;
		}

		final long l=(long)value;
		if (l>0) {
			if (l>=fastPositiveResults.length) return value;
			return fastPositiveResults[(int)l];
		} else {
			if ((-l)>=fastNegativeResults.length) return value;
			return fastNegativeResults[(int)(-l)];
		}
	}

	/**
	 * Thread-Lokale Variable für das Rechensystem, das bei der Umwandlung von Zeichenketten
	 * in Zahlen verwendet werden soll. Auf diese Weise muss nicht bei jedem Aufruf von
	 * {@link #getDouble(String)} ein neues Objekt angelegt werden.
	 * @see #getDouble(String)
	 */
	private static ThreadLocal<CalcSystem> calcSystem=new ThreadLocal<CalcSystem>() {
		@Override
		protected CalcSystem initialValue() {
			return new CalcSystem();
		}
	};

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getDouble(final String s) {
		if (s==null) return null;

		if (s.equals(plusInifityString)) return Double.POSITIVE_INFINITY;
		if (s.equals(minusInifityString)) return Double.NEGATIVE_INFINITY;

		boolean isPlain=true;
		boolean hasDecimal=false;
		final int len=s.length();
		for (int i=0;i<len;i++) {
			final char c=s.charAt(i);
			if (c>='0' && c<='9') continue;
			if (c=='-' && i>0) {isPlain=false; break;}
			if ((c=='.' || c==',') && !hasDecimal) hasDecimal=true; else {isPlain=false; break;}
		}
		if (isPlain) {
			if (hasDecimal) {
				try {
					return fastBoxedValue(Double.parseDouble(s));
				} catch (NumberFormatException e) {/* bei zu großen Zahlen: Fallback auf Double-Parser, der kommt damit klar */}
			} else {
				try {
					return fastBoxedValue(Long.parseLong(s));
				} catch (NumberFormatException e) {/* bei zu großen Zahlen: Fallback auf Double-Parser, der kommt damit klar */}
			}
		}

		final CalcSystem calc=calcSystem.get();
		if (calc.parse(s)>=0) return null;
		try {
			return fastBoxedValue(calc.calc());
		} catch (MathCalcError e) {
			return null;
		}
	}

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln.
	 * Diese Funktion verwendet <b>nicht</b> den Formelparser, sondern wandelt nur eine einfache Zahl um.
	 * Daher kann diese Funktion ohne Stack-Überlauf im Parser selbst eingesetzt werden.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getPlainDouble(String s) {
		if (s==null) return null;

		if (s.equals(plusInifityString)) return Double.POSITIVE_INFINITY;
		if (s.equals(minusInifityString)) return Double.NEGATIVE_INFINITY;

		boolean hasDecimal=false;
		final int len=s.length();
		for (int i=0;i<len;i++) {
			final char c=s.charAt(i);
			if ((c<'0' || c>'9') && c!='E' && c!='-') {
				if ((c=='.' || c==',') && !hasDecimal) hasDecimal=true; else return null;
			}
		}

		if (!hasDecimal) {
			try {
				return (double)Long.parseLong(s);
			} catch (NumberFormatException e) {/* bei zu großen Zahlen: Fallback auf Double-Parser, der kommt damit klar */}
		}

		try {
			if (','!=activeSeparator) s=s.replace(',',activeSeparator);
			if ('.'!=activeSeparator) s=s.replace('.',activeSeparator);
			return NumberFormat.getInstance(activeLocale).parse(s).doubleValue();
		} catch (ParseException e) {return null;}
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getDouble(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getDouble(JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Long</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive Zahlen.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getPositiveLong(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((d<1) || (Math.round(d)!=d)) return null;
		return Math.round(d);
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Long</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive Zahlen.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getPositiveLong(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Long l=getPositiveLong(field.getText());
		if (setColor) {
			if (l==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return l;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Long</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive Zahlen.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Long</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Long getPositiveLong(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Long l=getPositiveLong(field.getText());
		if (setColor) {
			if (l==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return l;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive ganzzahlige Werte.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getPositiveIntDouble(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((d<1) || (Math.round(d)!=d)) return null;
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive ganzzahlige Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getPositiveIntDouble(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getPositiveIntDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive ganzzahlige Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getPositiveIntDouble(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getPositiveIntDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}


	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getNotNegativeDouble(final String s) {
		if (s==null) return null;
		if (s.equals(plusInifityString)) return Double.POSITIVE_INFINITY;
		final Double d=getDouble(s); if (d==null) return null;
		if (d<0) return null;
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getNotNegativeDouble(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getNotNegativeDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur nicht negative Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getNotNegativeDouble(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getNotNegativeDouble(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive Werte.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getPositiveDouble(final String s) {
		if (s==null) return null;
		if (s.equals(plusInifityString)) return Double.POSITIVE_INFINITY;
		final Double d=getDouble(s); if (d==null) return null;
		if (d<=0) return null;
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getPositiveDouble(final TextField field, final boolean setColor) {
		if (field==null) return null;
		Double d=getNotNegativeDouble(field.getText());
		if (d!=null && d==0) d=null;
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur positive Werte.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getPositiveDouble(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		Double d=getNotNegativeDouble(field.getText());
		if (d!=null && d==0) d=null;
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur Werte im Bereich von 0 bis 1.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getProbability(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((d<0) || (d>1)) return null;
		return d;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur Werte ab 0.
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getExtProbability(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if (d<0) return null;
		return d;
	}

	/**
	 * Versucht den übergebenen String in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur Werte im Bereich von 0 bis 1 in System-Notation ("." als Dezimalkomma).
	 * @param s	String, der die umzuwandelnde Zahl enthält
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getSystemProbability(final String s) {
		if (s==null) return null;
		final Double d=getDouble(s); if (d==null) return null;
		if ((d<0) || (d>1)) return null;
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur Werte im Bereich von 0 bis 1.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getProbability(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getProbability(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur Werte im Bereich von 0 bis 1.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getProbability(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getProbability(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur Werte ab 0.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getExtProbability(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getExtProbability(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine <code>Double</code>-Zahl umzuwandeln,
	 * akzeptiert dabei jedoch nur Werte ab 0.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getExtProbability(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getExtProbability(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(SystemColor.text);
		}
		return d;
	}

	/**
	 * Reduziert die maximale Anzahl an Nachkommastellen, so dass sich eine Gesamtanzahl an Ziffern ergibt
	 * @param value	Zu verändernder Dezimalwert
	 * @param decimalDigits	Anzahl der (im 10er-System betrachteten) Ziffern
	 * @return	Auf weniger Dezimalstellen gerundeter Wert
	 */
	public static double reduceDigits(double value, final int decimalDigits) {
		if (value%1==0.0) return value;
		if (decimalDigits<1) return Math.round(value);
		if (decimalDigits>14) return value;

		final boolean minus=value<0;
		if (minus) value=-value;

		final int usedDigits=(int)Math.ceil(Math.log10(value));
		final int availableDecimalDigits=decimalDigits-usedDigits;

		if (availableDecimalDigits<=0) {
			value=Math.round(value);
		} else {
			double intPart=Math.floor(value);
			double fracPart=value-intPart;
			double mul=Math.round(FastMath.pow(10,availableDecimalDigits));
			fracPart=Math.round(fracPart*mul)/mul;
			value=intPart+fracPart;
		}

		if (minus) value=-value;
		return value;
	}
}
