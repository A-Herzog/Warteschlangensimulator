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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Zeigt eine Schaltfläche zur Auswahl der Deckkraft mit einem vorgelagerten Label an.
 * @see AlphaButton
 */
public class LabeledAlphaButton extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8879510973794135235L;

	/**
	 * Schaltfläche zur Auswahl der Deckkraft
	 */
	private final AlphaButton button;

	/**
	 * Konstruktor
	 * @param text	Vor der Schaltfläche anzuzeigender Text
	 * @param alpha	Deckkraft
	 */
	public LabeledAlphaButton(final String text, final double alpha) {
		final FlowLayout layout=new FlowLayout(FlowLayout.LEFT);
		setLayout(layout);
		final int defaultHGap=layout.getHgap();
		layout.setHgap(0);

		final JLabel label=new JLabel(text);
		add(label);

		add(Box.createHorizontalStrut(defaultHGap));

		button=new AlphaButton(alpha);
		button.addClickListener(e->fireClickListeners());
		add(button);
		label.setLabelFor(button);
	}

	/**
	 * Stellt die Deckkraft ein.
	 * @param alpha	Neue Deckkraft
	 */
	public void setAlpha(final double alpha) {
		button.setAlpha(alpha);
	}

	/**
	 * Liefert die gewählte Deckkraft
	 * @return	Gewählte Deckkraft
	 */
	public double getAlpha() {
		return button.getAlpha();
	}

	/**
	 * Klick-Listener
	 * @see #addClickListener(ActionListener)
	 * @see #removeClickListener(ActionListener)
	 * @see #fireClickListeners()
	 */
	private final List<ActionListener> clickListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn die Deckkraft geändert wird
	 * @param clickListener	Zu benachrichtigender Listener
	 */
	public void addClickListener(final ActionListener clickListener) {
		if (clickListeners.indexOf(clickListener)<0) clickListeners.add(clickListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer Änderung die Deckkraft zu benachrichtigenden Listener
	 * @param clickListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeClickListener(final ActionListener clickListener) {
		return clickListeners.remove(clickListener);
	}

	/**
	 * Benachrichtigt alle registrierten Listener über eine Deckkraftänderung
	 */
	private void fireClickListeners() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"select");
		for (ActionListener listener: clickListeners) listener.actionPerformed(event);
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		button.setEnabled(enabled);
	}
}
