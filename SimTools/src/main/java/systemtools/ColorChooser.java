/**
 * Copyright 2024 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package systemtools;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Methoden, die jedes Element zur Farbauswahl implementieren muss.
 * @see SmallColorChooser
 * @see ColorChooserButton
 * @see LabeledColorChooserButton
 * @see OptionalColorChooserButton
 */
public interface ColorChooser {
	/**
	 * Liefert die momentan eingestellte Farbe.
	 * @return	Aktuell gewählte Farbe
	 */
	Color getColor();

	/**
	 * Stellt die ausgewählte Farbe ein.
	 * @param color	Auszuwählende Farbe
	 */
	void setColor(Color color);

	/**
	 * Gibt an, ob Farben per Klick ausgewählt werden können.
	 * @return	Farben per Klick anwählbar
	 */
	boolean isEnabled();

	/**
	 * Stellt ein, ob Farben per Klick gewählt werden dürfen.
	 * @param enabled	Farben per Klick anwählbar
	 */
	void setEnabled(final boolean enabled);

	/**
	 * Liefert das Objekt, in dem die Klick-Listener des konkreten Objektes gespeichert werden.
	 * (Wird in den Default-Methoden des Interface verwendet.)
	 * @return	Klick-Listener des konkreten Objektes
	 */
	List<ActionListener> getClickListeners();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn auf eine Farbe geklickt wird
	 * @param clickListener	Zu benachrichtigender Listener
	 */
	default void addClickListener(final ActionListener clickListener) {
		final List<ActionListener> clickListeners=getClickListeners();
		if (clickListeners.indexOf(clickListener)<0) clickListeners.add(clickListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle eines Klicks auf eine Farbe zu benachrichtigenden Listener
	 * @param clickListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	default boolean removeClickListener(final ActionListener clickListener) {
		final List<ActionListener> clickListeners=getClickListeners();
		return clickListeners.remove(clickListener);
	}

	/**
	 * Benachrichtigt alle registrierten Listener über eine Farbauswahl
	 */
	default void fireClickListeners() {
		final List<ActionListener> clickListeners=getClickListeners();
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"select");
		for (ActionListener listener: clickListeners) listener.actionPerformed(event);
	}
}
