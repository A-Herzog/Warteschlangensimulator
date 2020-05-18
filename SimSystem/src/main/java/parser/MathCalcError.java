package parser;

/**
 * Exception, die ausgelöst wird, wenn ein Ausdruck nicht berechnet werden kann.
 * @author Alexander Herzog
 * @see MathParser#calc()
 * @see MathParser#calc(double[])
 */
public class MathCalcError extends Exception {
	private static final long serialVersionUID=8709708885590784179L;

	/**
	 * Konstruktor der Klasse
	 * @param mathObject	Objekt bei dessen Bearbeitung der Fehler aufgetreten ist
	 */
	public MathCalcError(final Object mathObject) {
		super("Error executing "+mathObject.getClass().getCanonicalName());
	}
}
