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
import java.awt.TextField;

import javax.swing.JTextField;

import org.apache.commons.math3.util.FastMath;

/**
 * Enthält einige statische Routinen zur Umwandlung von Zeichenketten in Uhrzeiten
 * und umgekehrt.
 * @author Alexander Herzog
 * @version 1.5
 */
public final class TimeTools {
	/**
	 * Diese Klasse kann nicht instanziert werden.
	 */
	private TimeTools() {
	}

	/**
	 * Wandelt eine als Sekunden-Integer-Wert gegebene Uhrzeit in eine Zeichenkette um
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 */
	public static String formatTime(long time) {
		final StringBuilder sb=new StringBuilder();

		if (time<0) {
			sb.append('-');
			time=-time;
		}

		final long h=time/60/60;
		if (h<10) sb.append("0");
		sb.append(h);

		sb.append(":");

		final long m=(time/60)%60;
		if (m<10) sb.append("0");
		sb.append(m);

		sb.append(":");

		final long s=time%60;
		if (s<10) sb.append("0");
		sb.append(s);

		return sb.toString();

		/* langsamer: return String.format("%02d:%02d:%02d",time/3600,time/60%60,time%60); */
	}

	/**
	 * Wandelt eine als Sekunden-Integer-Wert gegebene Uhrzeit in eine Zeichenkette um und unterteilt dabei bei Bedarf in mehrere Tage
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 */
	public static String formatLongTime(long time) {
		final StringBuilder sb=new StringBuilder();

		if (time<0) {
			sb.append('-');
			time=-time;
		}

		final long d=time/60/60/24;
		if (d>0) {sb.append(d); sb.append(":");}

		final long h=time/60/60%24;
		if (h<10) sb.append("0");
		sb.append(h);

		sb.append(":");

		final long m=(time/60)%60;
		if (m<10) sb.append("0");
		sb.append(m);

		sb.append(":");

		final long s=time%60;
		if (s<10) sb.append("0");
		sb.append(s);

		return sb.toString();


		/*
		langsamer:
		if (time<86400) {
			return String.format("%02d:%02d:%02d",time/3600,time/60%60,time%60);
		} else {
			return String.format("%d:%02d:%02d:%02d",time/86400,time/3600%24,time/60%60,time%60);
		}
		 */
	}

	/**
	 * Wandelt eine als Sekunden-Integer-Wert gegebene Uhrzeit in eine Zeichenkette um und unterteilt dabei bei Bedarf in mehrere Tage
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 */
	public static String formatLongTime(final double time) {
		return formatLongTime(Math.round(time));
	}

	/**
	 * Wandelt eine als Sekunden-Integer-Wert gegebene Uhrzeit in eine Zeichenkette um
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 */
	public static String formatShortTime(final int time) {
		if (time>=0) {
			return String.format("%02d:%02d",time/3600,time/60%60);
		} else {
			return String.format("-%02d:%02d",(-time)/3600,(-time)/60%60);
		}
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * @param time Umzuwandelnde Uhrzeit
	 * @param digits	Anzahl an Nachkommastellen des Sekundenwerts
	 * @param separator	Dezimaltrenner
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactSystemTime(double)
	 * @see #formatExactTime(double)
	 * @see #formatExactTime(double, int)
	 */
	private static String formatExactTimeInt(double time, final int digits, final char separator) {
		final StringBuilder sb=new StringBuilder();
		final boolean minus;
		if (time<0) {
			minus=true;
			time=-time;
		} else {
			minus=false;
		}
		if (minus) sb.append("-");

		sb.append(formatTime(FastMath.round(FastMath.floor(time))));

		final double fraction=time-FastMath.floor(time);

		double level=1;
		for (int i=1;i<=digits;i++) level*=10;

		if (fraction>=1.0/level) {
			String t=""+FastMath.round(fraction*level);
			if (t.length()>digits) {
				sb.setLength(0);
				if (minus) sb.append("-");
				sb.append(formatTime(FastMath.round(FastMath.floor(time))+1));
				return sb.toString();
			}
			while (t.length()<digits) t='0'+t;
			while (t.length()>0 && t.charAt(t.length()-1)=='0') t=t.substring(0,t.length()-1);
			if (t.length()>0) {
				sb.append(separator);
				sb.append(t);
			}
		}

		return sb.toString();
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * @param time Umzuwandelnde Uhrzeit
	 * @param digits	Anzahl an Nachkommastellen des Sekundenwerts
	 * @param separator	Dezimaltrenner
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactSystemTime(double)
	 * @see #formatExactTime(double)
	 * @see #formatExactTime(double, int)
	 */
	private static String formatExactLongTimeInt(double time, final int digits, final char separator) {
		final StringBuilder sb=new StringBuilder();
		final boolean minus;
		if (time<0) {
			minus=true;
			time=-time;
		} else {
			minus=false;
		}
		if (minus) sb.append("-");

		sb.append(formatLongTime(FastMath.round(FastMath.floor(time))));

		final double fraction=time-FastMath.floor(time);

		double level=1;
		for (int i=1;i<=digits;i++) level*=10;

		if (fraction>=1.0/level) {
			String t=""+FastMath.round(fraction*level);
			if (t.length()>digits) {
				sb.setLength(0);
				if (minus) sb.append("-");
				sb.append(formatLongTime(FastMath.round(FastMath.floor(time))+1));
				return sb.toString();
			}
			while (t.length()<digits) t='0'+t;
			while (t.length()>0 && t.charAt(t.length()-1)=='0') t=t.substring(0,t.length()-1);
			if (t.length()>0) {
				sb.append(separator);
				sb.append(t);
			}
		}

		return sb.toString();
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um.
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * (Als Dezimaltrenner wird ein Komma verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactSystemTime(double)
	 */
	public static String formatExactTime(final double time) {
		return formatExactTimeInt(time,1,NumberTools.getDecimalSeparator());
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um.
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * (Als Dezimaltrenner wird ein Komma verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @param digits	Anzahl an Nachkommastellen des Sekundenwerts
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactSystemTime(double)
	 */
	public static String formatExactTime(final double time, final int digits) {
		return formatExactTimeInt(time,digits,NumberTools.getDecimalSeparator());
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um.
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * (Als Dezimaltrenner wird ein Punkt verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactTime(double)
	 * @see #formatExactTime(double, int)
	 */
	public static String formatExactSystemTime(final double time) {
		return formatExactTimeInt(time,3,'.');
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um.
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * Die Ausgabe erfolgt, wenn nötig, mit separater Angabe von Tagen.
	 * (Als Dezimaltrenner wird ein Komma verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactSystemTime(double)
	 */
	public static String formatExactLongTime(final double time) {
		return formatExactLongTimeInt(time,1,NumberTools.getDecimalSeparator());
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um.
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * Die Ausgabe erfolgt, wenn nötig, mit separater Angabe von Tagen.
	 * (Als Dezimaltrenner wird ein Komma verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @param digits	Anzahl an Nachkommastellen des Sekundenwerts
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactSystemTime(double)
	 */
	public static String formatExactLongTime(final double time, final int digits) {
		return formatExactLongTimeInt(time,digits,NumberTools.getDecimalSeparator());
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um.
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * Die Ausgabe erfolgt, wenn nötig, mit separater Angabe von Tagen.
	 * (Als Dezimaltrenner wird ein Punkt verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 * @see #formatExactTime(double)
	 * @see #formatExactTime(double, int)
	 */
	public static String formatExactLongSystemTime(final double time) {
		return formatExactLongTimeInt(time,3,'.');
	}

	/**
	 * Versucht den übergebenen String in eine sekundenbasierende Zeitangabe umzuwandeln.
	 * @param s	String, der den umzuwandelnden Zeitwert enthält
	 * @return	Zeit in Sekunden als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getTime(final String s) {
		if (s==null || s.isEmpty()) return null;
		final String[] l=s.split(":");

		if (l.length<1 || l.length>4) return null;

		final int[] multiply=new int[]{1,60,3600,86400};

		int result=0;
		for (int i=0;i<l.length;i++) {
			final int value;
			if (l[i].equals("0") || l[i].equals("00")) {
				value=0;
			} else {
				if (l[i].length()>0 && l[i].charAt(0)=='0') l[i]=l[i].substring(1);
				if (l[i].trim().isEmpty()) {
					value=0;
				} else {
					final Integer val=NumberTools.getInteger(l[i]);
					if (val==null) return null;
					value=val.intValue();
				}
			}
			result+=multiply[l.length-1-i]*value;
		}

		return result;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine sekundenbasierende Zeitangabe umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getTime(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Integer i=getTime(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(NumberTools.getTextFieldDefaultBackground());
		}
		return i;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine sekundenbasierende Zeitangabe umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Integer</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Integer getTime(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Integer i=getTime(field.getText());
		if (setColor) {
			if (i==null) field.setBackground(Color.red); else field.setBackground(NumberTools.getTextFieldDefaultBackground());
		}
		return i;
	}

	/**
	 * Versucht den übergebenen String in eine sekundenbasierende Zeitangabe inkl. Vorzeichen und Nachkommastellen umzuwandeln.
	 * @param s	String, der den umzuwandelnden Zeitwert enthält
	 * @return	Zeit in Sekunden als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getExactTime(String s) {
		if (s==null || s.isEmpty()) return null;
		boolean negative=false;

		if (s.substring(0,1).equals("-")) {negative=true; s=s.substring(1);}

		int i1=s.indexOf('.'), i2=s.indexOf(','), i=-1;
		if (i1>=0 || i2>=0) {if (i1<0) i=i2; else {if (i2<0) i=i1; else i=FastMath.min(i1,i2);}}
		double frac=0;
		if (i>=0) {
			final Double f=NumberTools.getDouble("0"+NumberTools.getDecimalSeparator()+s.substring(i+1)); if (f==null) return null;
			frac=f.doubleValue(); s=s.substring(0,i);
		}

		Integer time=getTime(s);
		if (time==null) return null;

		double t=time+frac;
		if (negative) t=-t;
		return t;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine sekundenbasierende Zeitangabe inkl. Vorzeichen und Nachkommastellen umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getExactTime(final TextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getExactTime(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(NumberTools.getTextFieldDefaultBackground());
		}
		return d;
	}

	/**
	 * Versucht den in dem Textfeld angegebenen Wert in eine sekundenbasierende Zeitangabe inkl. Vorzeichen und Nachkommastellen umzuwandeln.<br>
	 * Wenn dies fehlschlägt, kann das Textfeld optional rot eingefärbt werden.
	 * @param field	Textfeld, dass die umzuwandelnde Zeichenkette als Text enthält.
	 * @param setColor	Ist dieser Parameter auf <code>true</code> gesetzt, so wird das Textfeld rot eingefärbt, wenn die Umwandlung fehltschlägt und wieder normal eingefärbt, wenn die Umwandlung erfolgreich ist.
	 * @return	Zahl als <code>Double</code>; wenn die Umwandlung fehlschlägt, wird <code>null</code> zurückgegeben.
	 */
	public static Double getExactTime(final JTextField field, final boolean setColor) {
		if (field==null) return null;
		final Double d=getExactTime(field.getText());
		if (setColor) {
			if (d==null) field.setBackground(Color.red); else field.setBackground(NumberTools.getTextFieldDefaultBackground());
		}
		return d;
	}

	/**
	 * Wandelt eine als String gegebene lokale (d.h. mit "," als Dezimaltrenner) Zeitangabe
	 * in eine System-Zeitangabe (mit "." als Dezimaltrenner) um und gibt diese wieder
	 * als String zurück.
	 * @param time	Umzuwandelnde Zeitangabe als String mit "," als Dezimaltrenner
	 * @return Zeitangabe als String mit "." als Dezimaltrenner
	 */
	public static String localTimeToSystemTime(final String time) {
		if (time==null || time.isEmpty()) return "";

		/* Dinge, die keine Zeiten sind, nicht verändern */
		int countCollon=0;
		for (int i=0;i<time.length();i++) if (time.charAt(i)==':') countCollon++;
		if (countCollon!=2) return time;

		/* Zeiten anpassen */
		final Double D=getExactTime(time);
		if (D==null) return time;
		return formatExactSystemTime(D.doubleValue());
	}
}
