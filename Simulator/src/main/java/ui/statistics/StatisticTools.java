package ui.statistics;

import mathtools.NumberTools;
import tools.SetupData;

/**
 * Diese Klasse stellt ähnlich wie {@link NumberTools} Methoden zur Umwandlung von
 * Zahlen in Zeichenketten zur Verfügung. Dabei wird die per {@link SetupData}
 * konfigurierte Mindestanzahl an Nachkommastellen berücksichtigt.
 * @author Alexander Herzog
 * @see NumberTools
 */
public class StatisticTools {
	private StatisticTools() {
	}

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
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * und fügt in den Ergebnisstring dabei 1000er-Punkte ein.
	 * @param number	Umzuwandelnde Zahl
	 * @return	Zahl als Zeichenkette
	 */
	public static String formatNumberLong(final double number)  {
		return NumberTools.formatNumberLong(number,Math.max(1,setup.statisticsNumberDigits));
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param number Umzuwandelnde Zahl
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(final double number)  {
		return NumberTools.formatPercent(number,Math.max(1,setup.statisticsPercentDigits));
	}

	/**
	 * Wandelt eine Fließkommazahl in eine Zeichenkette um (unter Beachtung der lokalen Darstellungsform)
	 * @param number Umzuwandelnde Zahl
	 * @param minDigits Minimale Anzahl an Nachkommastellen
	 * @return Zahl als Zeichenkette
	 */
	public static String formatPercent(final double number, final int minDigits)  {
		return NumberTools.formatPercent(number,Math.max(minDigits,Math.max(1,setup.statisticsPercentDigits)));
	}
}
