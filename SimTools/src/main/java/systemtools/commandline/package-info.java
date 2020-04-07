/**
 * Die Klassen in diesem Package stellen die Basis für ein modulares System
 * zur Verarbeitung von Kommandozeilenbefehlen dar.<br><br>
 * Den Ausgangspunkt stellt die abstrakte Klasse {@link systemtools.commandline.AbstractCommand}
 * dar, von der die konkreten Handler für bestimmte Kommandozeilenbefehle
 * abzuleiten sind. Die Handler werden in einer von {@link systemtools.commandline.BaseCommandLineSystem}
 * abgeleiteten Klasse registriert und später über diese aufgerufen. Außerdem
 * stehen sie so auch über den Dialog {@link systemtools.commandline.CommandLineDialog}
 * direkt in der GUI zur Verfügung.
 * @author Alexander Herzog
 */
package systemtools.commandline;