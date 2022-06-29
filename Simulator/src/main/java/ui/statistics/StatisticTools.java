package ui.statistics;

import mathtools.NumberTools;
import mathtools.TimeTools;
import tools.SetupData;

/**
 * Diese Klasse stellt ähnlich wie {@link NumberTools} Methoden zur Umwandlung von
 * Zahlen in Zeichenketten zur Verfügung. Dabei wird die per {@link SetupData}
 * konfigurierte Mindestanzahl an Nachkommastellen berücksichtigt.
 * @author Alexander Herzog
 * @see NumberTools
 */
public class StatisticTools {
	/**
	 * Konstruktor der Klasse<br>
	 * Diese Klasse kann nicht instanziert werden. Sie stellt nur statische Hilfsmethoden zur Verfügung.
	 */
	private StatisticTools() {
	}

	/** Referenz auf das Setup-Singleton */
	private static final SetupData setup=SetupData.getSetup();


	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param number Umzuwandelnde Zahl
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumber(final double number)  {
		return NumberTools.formatNumber(number,Math.max(1,setup.statisticsNumberDigits));
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param number Umzuwandelnde Zahl
	 * @param minDigits Minimale Anzahl an Nachkommastellen
	 * @return Zahl als Zeichenkette
	 */
	public static String formatNumber(final double number, final int minDigits)  {
		return NumberTools.formatNumber(number,Math.max(minDigits,Math.max(1,setup.statisticsNumberDigits)));
	}

	/**
	 * Wird in {@link #formatPercent(double)} und {@link #formatPercent(double, int)}
	 * verwendet und hier gecacht.<br>
	 * Ist nicht Thread-save, brauchen wir hier aber auch nicht, ist für die GUI.
	 * @see #formatPercent(double)
	 * @see #formatPercent(double, int)
	 */
	private static StringBuilder sb1=new StringBuilder();

	/**
	 * Wird in {@link #formatPercent(double)} und {@link #formatPercent(double, int)}
	 * verwendet und hier gecacht.<br>
	 * Ist nicht Thread-save, brauchen wir hier aber auch nicht, ist für die GUI.
	 * @see #formatPercent(double)
	 * @see #formatPercent(double, int)
	 */
	private static StringBuilder sb2=new StringBuilder();

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param number Umzuwandelnde Zahl
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(final double number)  {
		synchronized(sb1) {
			sb1.setLength(0);
			sb2.setLength(0);
			sb2.append(NumberTools.formatNumber(number*100,Math.max(1,setup.statisticsPercentDigits),sb1));
			sb2.append('%');
			return sb2.toString();
		}
		/* return NumberTools.formatPercent(number,Math.max(1,setup.statisticsPercentDigits)); */
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param number Umzuwandelnde Zahl
	 * @param minDigits Minimale Anzahl an Nachkommastellen
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(final double number, final int minDigits)  {
		synchronized(sb1) {
			sb1.setLength(0);
			sb2.setLength(0);
			sb2.append(NumberTools.formatNumber(number*100,Math.max(minDigits,Math.max(1,setup.statisticsPercentDigits)),sb1));
			sb2.append('%');
			return sb2.toString();
		}
		/* return NumberTools.formatPercent(number,Math.max(minDigits,Math.max(1,setup.statisticsPercentDigits))); */
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * (Als Dezimaltrenner wird ein Komma verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @return Uhrzeit als Zeichenkette
	 */
	public static String formatExactTime(final double time) {
		return TimeTools.formatExactLongTime(time,setup.statisticsNumberDigits);
	}

	/**
	 * Wandelt eine als Sekunden-Double-Wert gegebene Uhrzeit in eine Zeichenkette um
	 * Die Zeitangabe kann dabei auch negativ sein und Nachkommastellen enthalten.
	 * (Als Dezimaltrenner wird ein Komma verwendet.)
	 * @param time Umzuwandelnde Uhrzeit
	 * @param digits	Anzahl an Nachkommastellen des Sekundenwerts
	 * @return Uhrzeit als Zeichenkette
	 */
	public static String formatExactTime(final double time, final int digits) {
		return TimeTools.formatExactLongTime(time,Math.max(digits,setup.statisticsNumberDigits));
	}
}
