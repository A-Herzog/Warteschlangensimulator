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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

import mathtools.NumberTools;

/**
 * Schaltfläche über die die Deckkraft (der Hintergrundfarbe) eingestellt werden kann
 */
public class AlphaButton extends JButton {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-827641290919664382L;

	/**
	 * Aktuell gewählte Deckkraft
	 */
	private double alpha;

	/**
	 * Popup-Menü in dem der Farbwähler angezeigt wird
	 * @see #buttonClicked()
	 */
	private JPopupMenu popup;

	/**
	 * Deckkraft-Regler
	 * @see #buttonClicked()
	 */
	private JSlider slider;

	/**
	 * Konstruktor
	 * @param alpha	Deckkraft
	 */
	public AlphaButton(final double alpha) {
		this.alpha=Math.max(0,Math.min(1,alpha));
		updateText();
		addActionListener(e->buttonClicked());
	}

	/**
	 * Aktualisiert den auf der Schaltfläche anzuzeigenden Text
	 */
	private void updateText() {
		setText(Math.round(alpha*100)+"%");
	}

	/**
	 * Liefert die aktuell eingestellte Deckkraft
	 * @return	Deckkraft
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * Stellt die Deckkraft ein.
	 * @param alpha	Deckkraft
	 */
	public void setAlpha(final double alpha) {
		final double newAlpha=Math.max(0,Math.min(1,alpha));
		if (this.alpha==newAlpha) return;
		this.alpha=newAlpha;
		updateText();
		fireClickListeners();
	}

	/**
	 * Reaktion auf Klicks auf die Schaltfläche (Aktivierung des Farbwählers)
	 */
	private void buttonClicked() {
		if (popup==null) {
			popup=new JPopupMenu();
			popup.add(slider=new JSlider(0,100));
			slider.setOrientation(SwingConstants.VERTICAL);
			slider.setMinorTickSpacing(5);
			slider.setMajorTickSpacing(20);
			Hashtable<Integer,JComponent> labels=new Hashtable<>();
			for (int i=0;i<=10;i++) labels.put(i*10,new JLabel(NumberTools.formatPercent(i/10.0)));
			slider.setLabelTable(labels);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setPreferredSize(new Dimension(slider.getPreferredSize().width,300));
			slider.addChangeListener(e->setAlpha(slider.getValue()/100.0));
		}
		slider.setValue((int)Math.round(100*alpha));
		popup.show(this,0,getHeight());
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
}
