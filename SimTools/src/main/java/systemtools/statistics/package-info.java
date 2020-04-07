/**
 * Dieses Package stellt eine ganze Reihe von Klassen zur Verfügung, die
 * die Anzeige von Statistikergebnissen in Form von Texten, Tabellen
 * und Grafiken (jeweils inkl. Export-Funktionen) ermöglichen.
 * <br><br>
 * Über {@link systemtools.statistics.StatisticsBasePanel} steht
 * eine Basisklasse für die Statistikansicht (links Baumstruktur, rechts Viewer)
 * zur Verfügung, die nur noch um Viewer angereichert werden muss.
 * Basis-Viewer für Texte, Tabellen und Grafiken stehen ebenfalls
 * zur Verfügung. In abgeleiteten Klassen müssen lediglich die
 * jeweils gewünschten Daten aus der Statistik entnommen und an
 * die vordefinierten Ausgabefunktionen übergeben werden. Die
 * verschiedenen Ausgabe- und Exportformate werden dann
 * automatisch bereitgestellt.
 * @author Alexander Herzog
 */
package systemtools.statistics;