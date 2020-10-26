package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Über den Reload-Manager können mehrere Programmfenster in Bezug auf gemeinsame
 * Setup-Einstellungen synchronisiert werden.<br>
 * Beim Öffnen melden sich alle Fenster hier an. Wenn in einem Fenster eine Einstellung
 * verändert wurde, die ein Update in den anderen Fenstern erfordert, kann das jeweilige
 * Fenster dies hier initiieren.
 * @author Alexander Herzog
 * @see MainFrame#reload(ui.MainFrame.ReloadMode)
 * @see MainPanel#reloadSetup()
 * @see MainFrame.ReloadMode
 */
public class ReloadManager {
	/**
	 * Menge aller aktuell geöffneten Fenster
	 */
	private static final Set<MainFrame> frames=new HashSet<>();

	/**
	 * Benachrichtigungsgruppen
	 * @see #addBroadcastReceiver(String, Runnable)
	 * @see #removeBroadcastReceiver(Runnable)
	 * @see #notify(Runnable)
	 */
	private static final Map<String,Set<Runnable>> broadcastReceivers=new HashMap<>();

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
	 * @param frame	Fenster, welches zukünftig nicht mehr benachrichtigt werden soll
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
	 * @param sender	Fenster von dem die Änderung ausgeht (dieses wird nicht benachrichtigt)
	 * @param reloadMode	Art der Setup-Änderung
	 * @see MainFrame.ReloadMode
	 */
	public static void notify(final MainFrame sender, final MainFrame.ReloadMode reloadMode) {
		for (MainFrame frame: frames) if (frame!=sender) frame.reload(reloadMode);
	}

	/**
	 * Listet alle aktiven Programmfenster auf.
	 * @return	Liste aller Programmfenster
	 */
	public static List<MainFrame> getMainFrames() {
		return new ArrayList<>(frames);
	}

	/**
	 * Fügt einen neuen Empfänger zu einer Benachrichtigungsgruppe hinzu
	 * @param id	Benachrichtigungsgruppe
	 * @param broadcastReceiver	Empfänger für Benachrichtigungen
	 */
	public static void addBroadcastReceiver(final String id, final Runnable broadcastReceiver) {
		Set<Runnable> set=broadcastReceivers.get(id);
		if (set==null) broadcastReceivers.put(id,set=new HashSet<>());
		set.add(broadcastReceiver);
	}

	/**
	 * Entfernt einen Empfänger aus den Listen aller Benachrichtigungsgruppe
	 * @param broadcastReceiver	Nicht mehr zu benachrichtigender Empfänger
	 */
	public static void removeBroadcastReceiver(final Runnable broadcastReceiver) {
		final Set<String> ids=new HashSet<>();
		for (Map.Entry<String,Set<Runnable>> entry: broadcastReceivers.entrySet()) {
			if (entry.getValue().contains(broadcastReceiver)) ids.add(entry.getKey());
		}
		for (String id: ids) {
			final Set<Runnable> set=broadcastReceivers.get(id);
			set.remove(broadcastReceiver);
			if (set.isEmpty()) broadcastReceivers.remove(id);
		}
	}

	/**
	 * Löst die Benachrichtigungen in einer oder mehreren Benachrichtigungsgruppe
	 * @param sender	Sender; alle Benachrichtigungsgruppen, in denen dieser Sender enthalten ist, werden aktiviert. Der Sender selbst wird nicht benachrichtigt.
	 */
	public static void notify(final Runnable sender) {
		for (Set<Runnable> set: broadcastReceivers.values()) if (set.contains(sender)) {
			set.stream().filter(r->r!=sender).forEach(r->r.run());
		}
	}
}
