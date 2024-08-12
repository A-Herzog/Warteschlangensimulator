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
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

/**
 * Schaltfläche, die eine Farbe anzeigt und bei einem Klick einen Farbwähler aktiviert
 * @see SmallColorChooser
 */
public class ColorChooserButton extends JButton implements ColorChooser {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=4297942213354993986L;

	/**
	 * Standardfarbe, die verwendet werden soll, bis eine Farbe eingestellt oder gesetzt wird
	 */
	private static final Color DEFAULT_COLOR=Color.WHITE;

	/**
	 * Größe des Farb-Icons
	 */
	private static final int ICON_SIZE=18;

	/**
	 * Aktuell gewählte Farbe
	 */
	private Color color;

	/**
	 * Soll diese Schaltfläche als nicht aktiv (im Sinne einer vorgeschalteten Checkbox) gelten?
	 * @see #setInactiveState()
	 */
	private boolean isInactive;

	/**
	 * Popup-Menü in dem der Farbwähler angezeigt wird
	 * @see #buttonClicked()
	 */
	private JPopupMenu popup;

	/**
	 * Farbwähler
	 * @see #buttonClicked()
	 */
	private SmallColorChooser chooser;

	/**
	 * Konstruktor
	 * @param text	Auf der Schaltfläche anzuzeigender Text
	 * @param selectedColor	Aktuelle Farbe
	 */
	public ColorChooserButton(final String text, final Color selectedColor) {
		super((text==null)?"":text);
		setColor(selectedColor);
		addActionListener(e->buttonClicked());
	}

	/**
	 * Konstruktor
	 * @param text	Auf der Schaltfläche anzuzeigender Text
	 */
	public ColorChooserButton(final String text) {
		this(text,null);
	}

	/**
	 * Konstruktor
	 * @param selectedColor	Aktuelle Farbe
	 */
	public ColorChooserButton(final Color selectedColor) {
		this("",selectedColor);
	}

	/**
	 * Konstruktor
	 */
	public ColorChooserButton() {
		this("",null);
	}

	/**
	 * Soll diese Schaltfläche als nicht aktiv (im Sinne einer vorgeschalteten Checkbox) gelten?
	 */
	public void setInactiveState() {
		isInactive=true;
	}

	/**
	 * Stellt die Farbe ein.
	 * @param color	Neue Farbe
	 */
	@Override
	public void setColor(final Color color) {
		final Color newColor=(color==null)?DEFAULT_COLOR:color;
		if (!isInactive && newColor.equals(this.color)) return;
		isInactive=false;
		this.color=newColor;
		updateIcon();
		fireClickListeners();
	}

	/**
	 * Liefert die gewählte Farbe
	 * @return	Gewählte Farbe
	 */
	@Override
	public Color getColor() {
		return color;
	}

	/**
	 * Liefert eine möglichst komplementäre Rahmenfarbe zu der gewählten Farbe
	 * @param color	Gewählte Farbe
	 * @return	Rahmenfarbe, die sich von der gewählten Farbe abhebt
	 */
	private static Color getBorderColor(final Color color) {
		return (color.getRed()+color.getGreen()+color.getBlue()>3*128)?Color.BLACK:Color.WHITE;
	}

	/**
	 * Aktualisiert das Icon auf der Schaltfläche gemäß der gewählten Farbe
	 */
	private void updateIcon() {
		final BufferedImage image=new BufferedImage(ICON_SIZE,ICON_SIZE,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D graphics=(Graphics2D)image.getGraphics();
		graphics.setColor(color);
		graphics.fillRect(0,0,ICON_SIZE-1,ICON_SIZE-1);
		graphics.setColor(getBorderColor(color));
		graphics.drawRect(0,0,ICON_SIZE-2,ICON_SIZE-2);
		setIcon(new ImageIcon(image));
	}

	/**
	 * Reaktion auf Klicks auf die Schaltfläche (Aktivierung des Farbwählers)
	 */
	private void buttonClicked() {
		if (popup==null) {
			popup=new JPopupMenu();
			popup.add(chooser=new SmallColorChooser());
			chooser.addClickListener(e->setColor(chooser.getColor()));
		}
		chooser.setColor(color);
		popup.show(this,0,getHeight());
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
}
