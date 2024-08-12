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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Zeigt eine Schaltfläche zur Farbauswahl mit einem vorgelagerten Label an.
 * @see ColorChooserButton
 */
public class LabeledColorChooserButton extends JPanel implements ColorChooser {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=9040968413797586478L;

	/**
	 * Schaltfläche zur Auswahl der Farbe
	 */
	private final ColorChooserButton button;

	/**
	 * Konstruktor
	 * @param text	Vor der Schaltfläche anzuzeigender Text
	 * @param color	Initial zu wählende Farbe
	 */
	public LabeledColorChooserButton(final String text, final Color color) {
		final FlowLayout layout=new FlowLayout(FlowLayout.LEFT);
		setLayout(layout);
		final int defaultHGap=layout.getHgap();
		layout.setHgap(0);

		final JLabel label=new JLabel(text);
		add(label);

		add(Box.createHorizontalStrut(defaultHGap));

		button=new ColorChooserButton(color);
		button.addClickListener(e->fireClickListeners());
		add(button);
		label.setLabelFor(button);
	}

	/**
	 * Stellt die Farbe ein.
	 * @param color	Neue Farbe
	 */
	@Override
	public void setColor(final Color color) {
		button.setColor(color);
	}

	/**
	 * Liefert die gewählte Farbe
	 * @return	Gewählte Farbe
	 */
	@Override
	public Color getColor() {
		return button.getColor();
	}

	/**
	 * Klick-Listener
	 * @see #getClickListeners()
	 */
	private final List<ActionListener> clickListeners=new ArrayList<>();

	@Override
	public List<ActionListener> getClickListeners() {
		return clickListeners;
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		button.setEnabled(enabled);
	}
}
