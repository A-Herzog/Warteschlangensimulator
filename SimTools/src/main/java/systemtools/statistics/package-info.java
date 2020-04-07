/**
 * Dieses Package stellt eine ganze Reihe von Klassen zur Verf�gung, die
 * die Anzeige von Statistikergebnissen in Form von Texten, Tabellen
 * und Grafiken (jeweils inkl. Export-Funktionen) erm�glichen.
 * <br><br>
 * �ber {@link systemtools.statistics.StatisticsBasePanel} steht
 * eine Basisklasse f�r die Statistikansicht (links Baumstruktur, rechts Viewer)
 * zur Verf�gung, die nur noch um Viewer angereichert werden muss.
 * Basis-Viewer f�r Texte, Tabellen und Grafiken stehen ebenfalls
 * zur Verf�gung. In abgeleiteten Klassen m�ssen lediglich die
 * jeweils gew�nschten Daten aus der Statistik entnommen und an
 * die vordefinierten Ausgabefunktionen �bergeben werden. Die
 * verschiedenen Ausgabe- und Exportformate werden dann
 * automatisch bereitgestellt.
 * @author Alexander Herzog
 */
package systemtools.statistics;