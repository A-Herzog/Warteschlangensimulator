/**
 * Die Klassen in diesem Package stellen die Basis f�r ein modulares System
 * zur Verarbeitung von Kommandozeilenbefehlen dar.<br><br>
 * Den Ausgangspunkt stellt die abstrakte Klasse {@link systemtools.commandline.AbstractCommand}
 * dar, von der die konkreten Handler f�r bestimmte Kommandozeilenbefehle
 * abzuleiten sind. Die Handler werden in einer von {@link systemtools.commandline.BaseCommandLineSystem}
 * abgeleiteten Klasse registriert und sp�ter �ber diese aufgerufen. Au�erdem
 * stehen sie so auch �ber den Dialog {@link systemtools.commandline.CommandLineDialog}
 * direkt in der GUI zur Verf�gung.
 * @author Alexander Herzog
 */
package systemtools.commandline;