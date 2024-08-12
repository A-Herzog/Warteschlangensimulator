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
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Zeigt eine Schaltfläche zur Farbauswahl mit einer vorgelagerten Checkbox an.
 * @see ColorChooserButton
 */
public class OptionalColorChooserButton extends JPanel implements ColorChooser {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8358590640968058050L;

	/**
	 * Ist die Farbe aktiv?
	 */
	private final JCheckBox active;

	/**
	 * Schaltfläche zur Auswahl der Farbe
	 */
	private final ColorChooserButton button;

	/**
	 * Konstruktor
	 * @param text	Vor der Schaltfläche anzuzeigender Text
	 * @param color	Initial zu wählende Farbe (kann <code>null</code> sein, wenn die Auswahl deaktiviert sein soll)
	 * @param defaultColor	Farbe, die auf der Schaltfläche angezeigt wird, wenn die initiale Auswahl auf <code>null</code> steht
	 */
	public OptionalColorChooserButton(final String text, final Color color, final Color defaultColor) {
		final FlowLayout layout=new FlowLayout(FlowLayout.LEFT);
		setLayout(layout);
		final int defaultHGap=layout.getHgap();
		layout.setHgap(0);

		active=new JCheckBox(text,color!=null);
		add(active);
		active.addActionListener(e->fireClickListeners());

		add(Box.createHorizontalStrut(defaultHGap));

		button=new ColorChooserButton((color==null)?defaultColor:color);
		add(button);
		button.addClickListener(e->{
			if (!active.isSelected()) active.setSelected(true);
			fireClickListeners();
		});
		if (color==null) button.setInactiveState();
	}

	/**
	 * Stellt die Farbe ein.
	 * @param color	Neue Farbe
	 */
	@Override
	public void setColor(final Color color) {
		if (color==null) {
			if (!active.isSelected()) return;
			active.setSelected(false);
			fireClickListeners();
			return;
		}
		button.setColor(color);
	}

	/**
	 * Liefert die gewählte Farbe
	 * @return	Gewählte Farbe
	 */
	@Override
	public Color getColor() {
		return (active.isSelected())?button.getColor():null;
	}

	/**
	 * Ist die Farbauswahl aktiv?
	 * @return	Farbauswahl aktiv
	 */
	public boolean isActive() {
		return active.isSelected();
	}

	/**
	 * Stellt ein, ob die Farbauswahl aktiv sein soll.
	 * @param active	Farbauswahl aktiv
	 */
	public void setActive(final boolean active) {
		if (this.active.isSelected()==active) return;
		this.active.setSelected(active);
		fireClickListeners();
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
		active.setEnabled(enabled);
		button.setEnabled(enabled);
	}
}
