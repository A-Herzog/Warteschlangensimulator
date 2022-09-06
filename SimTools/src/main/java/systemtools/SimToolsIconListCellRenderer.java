/**
 * Copyright 2022 Alexander Herzog
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
import java.awt.Container;
import java.awt.Image;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JList;

import systemtools.images.SimToolsImages;

/**
 * Diese Klasse ermöglicht das Verwenden von Icons in einer normalen String-basierenden Combobox.
 * @author Alexander Herzog
 * @see JComboBox
 * @see JComboBox#setRenderer(javax.swing.ListCellRenderer)
 */
public class SimToolsIconListCellRenderer extends DefaultListCellRenderer {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1841587407411409549L;

	/** Ressourcennamen der Icons zu den Einträgen */
	private final Icon[] icons;

	/**
	 * Konstruktor der Klasse
	 * @param parent	Elternelement (zu dem relativ die Iconpfade interpretiert werden)
	 * @param icons	Ressourcennamen der Icons zu den Einträgen
	 */
	public SimToolsIconListCellRenderer(final Container parent, final String[] icons) {
		super();
		this.icons=new Icon[(icons==null)?0:icons.length];
		if (icons!=null) for (int i=0;i<icons.length;i++) if (icons[i]!=null) {
			final URL url=parent.getClass().getResource(icons[i]);
			if (url!=null) this.icons[i]=new ImageIcon(url);
		}
	}

	/**
	 * Konstruktor der Klasse
	 * @param parent	Elternelement (zu dem relativ die Iconpfade interpretiert werden)
	 * @param icons	Ressourcennamen der Icons zu den Einträgen
	 */
	public SimToolsIconListCellRenderer(final Container parent, final List<String> icons) {
		this(parent,(icons==null)?new String[0]:icons.toArray(new String[0]));
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public SimToolsIconListCellRenderer(final List<ImageIcon> icons) {
		this((icons==null)?new ImageIcon[0]:icons.toArray(new ImageIcon[0]));
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public SimToolsIconListCellRenderer(final ImageIcon[] icons) {
		this.icons=(icons==null)?new ImageIcon[0]:icons;
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public SimToolsIconListCellRenderer(final Icon[] icons) {
		this.icons=(icons==null)?new ImageIcon[0]:icons;
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public SimToolsIconListCellRenderer(final SimToolsImages[] icons) {
		if (icons==null) {
			this.icons=new ImageIcon[0];
		} else {
			this.icons=new ImageIcon[icons.length];
			for (int i=0;i<icons.length;i++) this.icons[i]=icons[i].getIcon();
		}
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		int realIndex=-1;
		for (int i=0;i<list.getModel().getSize();i++) {
			final Object obj=list.getModel().getElementAt(i);
			if (Objects.equals(obj,value)) {realIndex=i; break;}
		}

		final Component c=super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		final Icon icon=(realIndex>=0 && realIndex<icons.length)?icons[realIndex]:null;
		if (icon==null) setIcon(null); else setIcon(icon);

		return c;
	}

	/**
	 * Erstellt eine Iconsliste zur Verwendung in {@link SimToolsIconListCellRenderer#SimToolsIconListCellRenderer(Icon[])}
	 * aus Iconnamen, Icons oder Bildern.
	 * @param parent	Elternelement (zu dem relativ die Iconpfade interpretiert werden)
	 * @param icons	Liste die die Iconnamen, Icons und Bilder enthält.
	 * @return	Liste mit Icons zur Verwendung in {@link SimToolsIconListCellRenderer#SimToolsIconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildIconsList(final Container parent, final Object[] icons) {
		final Icon[] result=new Icon[(icons==null)?0:icons.length];
		if (icons!=null) for (int i=0;i<icons.length;i++) {
			if (icons[i] instanceof Icon) {
				result[i]=(Icon)icons[i];
				continue;
			}
			if (icons[i] instanceof String) {
				final URL url=parent.getClass().getResource((String)icons[i]);
				if (url!=null) result[i]=new ImageIcon(url);
				continue;
			}
			if (icons[i] instanceof Image) {
				result[i]=new ImageIcon((Image)icons[i]);
				continue;
			}
			if (icons[i] instanceof URL) {
				result[i]=new ImageIcon((URL)icons[i]);
				continue;
			}
		}
		return result;
	}

	/**
	 * Erstellt eine Iconsliste zur Verwendung in {@link SimToolsIconListCellRenderer#SimToolsIconListCellRenderer(Icon[])}
	 * aus Iconnamen, Icons oder Bildern.
	 * @param parent	Elternelement (zu dem relativ die Iconpfade interpretiert werden)
	 * @param icons	Liste die die Iconnamen, Icons und Bilder enthält.
	 * @return	Liste mit Icons zur Verwendung in {@link SimToolsIconListCellRenderer#SimToolsIconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildIconsList(final Container parent, final List<Object> icons) {
		return buildIconsList(parent,(icons==null)?new Object[0]:icons.toArray(new Object[0]));
	}
}