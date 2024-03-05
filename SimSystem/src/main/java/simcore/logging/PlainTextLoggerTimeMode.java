package simcore.logging;

import java.io.File;

/**
 * Wie sollen Zeitangaben ausgegeben werden?
 * @author Alexander Herzog
 * @see PlainTextLogger#PlainTextLogger(File, boolean, boolean, PlainTextLoggerTimeMode, boolean, boolean, boolean)
 */
public enum PlainTextLoggerTimeMode {
	/** Zeitangaben als Sekunden-Zahlenwert ausgeben */
	PLAIN,
	/** Zeitangaben als HH:MM:SS,s ausgeben */
	TIME,
	/** Zeitangaben als Datum + Zeit ausgeben */
	DATETIME,
}