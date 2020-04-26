package ui;

import java.util.HashSet;
import java.util.Set;

/**
 * �ber den Reload-Manager k�nnen mehrere Programmfenster in Bezug auf gemeinsame
 * Setup-Einstellungen synchronisiert werden.<br>
 * Beim �ffnen melden sich alle Fenster hier an. Wenn in einem Fenster eine Einstellung
 * ver�ndert wurde, die ein Update in den anderen Fenstern erfordert, kann das jeweilige
 * Fenster dies hier initiieren.
 * @author Alexander Herzog
 * @see MainFrame#reload(ui.MainFrame.ReloadMode)
 * @see MainPanel#reloadSetup()
 * @see MainFrame.ReloadMode
 */
public class ReloadManager {
	private final static Set<MainFrame> frames=new HashSet<>();

	/**
	 * Diese Klasse stellt nur statische Methoden bereit
	 * und kann nicht instanziert werden.
	 */
	private ReloadManager() {
	}

	/**
	 * Registriert ein Programmfenster
	 * @param frame	Neues Programmfenster
	 */
	public static void add(final MainFrame frame) {
		frames.add(frame);
	}

	/**
	 * Entfernt ein Programmfenster aus der Liste.<br>
	 * Diese Methode muss aufgerufen werden, wenn das Fenster geschlossen wird.
	 * @param frame	Fenster, welches zuk�nftig nicht mehr benachrichtigt werden soll
	 */
	public static void remove(final MainFrame frame) {
		frames.remove(frame);
	}

	/**
	 * Sind momentan Programmfenster registriert?
	 * @return	Sind momentan Programmfenster registriert?
	 */
	public static boolean isEmpty() {
		return frames.isEmpty();
	}

	/**
	 * Benachrichtigt alle Programmfenster,
	 * dass das Setup neu geladen werden soll.
	 * @param sender	Fenster von dem die �nderung ausgeht (dieses wird nicht benachrichtigt)
	 * @param reloadMode	Art der Setup-�nderung
	 * @see MainFrame.ReloadMode
	 */
	public static void notify(final MainFrame sender, final MainFrame.ReloadMode reloadMode) {
		for (MainFrame frame: frames) if (frame!=sender) frame.reload(reloadMode);
	}
}
