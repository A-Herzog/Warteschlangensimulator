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

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Erweiterte Version von {@link JScrollPane}
 * in dem mit Touch-Ereignissen gescrollt werden kann.
 * @see JScrollPaneTouchHelper
 */
public class JScrollPaneTouch extends JScrollPane {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1974929737736828579L;

	/**
	 * Aktiviert oder deaktiviert die Touch-Unterstützung
	 * für alle Objekte dieser Klasse, die im Folgenden
	 * erzeugt werden.
	 * @see #installTouchSupport()
	 */
	public static boolean TOUCH_SCROLL_ACTIVE=false;

	/**
	 * Konstruktor der Klasse
	 * @param view	Innere Komponente
	 * @param vsbPolicy	Steuerung des vertikalen Scrollens
	 * @param hsbPolicy	Steuerung des horizontalen Scrollens
	 */
	public JScrollPaneTouch(Component view, int vsbPolicy, int hsbPolicy) {
		super(view,vsbPolicy,hsbPolicy);
		installTouchSupport();
	}

	/**
	 * Konstruktor der Klasse
	 * @param view	Innere Komponente
	 */
	public JScrollPaneTouch(Component view) {
		super(view);
		installTouchSupport();
	}

	/**
	 * Konstruktor der Klasse
	 * @param vsbPolicy	Steuerung des vertikalen Scrollens
	 * @param hsbPolicy	Steuerung des horizontalen Scrollens
	 */
	public JScrollPaneTouch(int vsbPolicy, int hsbPolicy) {
		super(vsbPolicy,hsbPolicy);
		installTouchSupport();
	}

	/**
	 * Konstruktor der Klasse
	 */
	public JScrollPaneTouch() {
		super();
		installTouchSupport();
	}

	/**
	 * Aktiviert die Touch-Unterstützung
	 * @see #TOUCH_SCROLL_ACTIVE
	 */
	private void installTouchSupport() {
		if (TOUCH_SCROLL_ACTIVE) SwingUtilities.invokeLater(()->new JScrollPaneTouchHelper(this));
	}
}
