package parser;

import java.io.Serializable;

/**
 * Exception, die ausgelöst wird, wenn ein Ausdruck nicht berechnet werden kann.
 * @author Alexander Herzog
 * @see MathParser#calc()
 * @see MathParser#calc(double[])
 */
public class MathCalcError extends Exception {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8709708885590784179L;

	/**
	 * Konstruktor der Klasse
	 * @param mathObject	Objekt bei dessen Bearbeitung der Fehler aufgetreten ist
	 */
	public MathCalcError(final Object mathObject) {
		super("Error executing "+mathObject.getClass().getCanonicalName());
	}

	/**
	 * Copy-Konstruktor der Klasse
	 * @param e	Exception-Objekt von dem die Meldung übernommen werden soll
	 */
	public MathCalcError(final Exception e) {
		super(e);
	}

	/**
	 * Konstruktor der Klasse
	 * @param message	Fehlermeldung
	 */
	public MathCalcError(final String message) {
		super(message);
	}
}
