/**
 * Copyright 2020 Alexander Herzog
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
package tools;

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

import simulator.editmodel.EditModel;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.ModelTransporter;

/**
 * Diese Klasse ermöglicht das Verwenden von Icons in einer normalen String-basierenden Combobox.
 * @author Alexander Herzog
 * @see JComboBox
 * @see JComboBox#setRenderer(javax.swing.ListCellRenderer)
 */
public class IconListCellRenderer extends DefaultListCellRenderer {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2336966853757784114L;

	/** Ressourcennamen der Icons zu den Einträgen */
	private final Icon[] icons;

	/**
	 * Konstruktor der Klasse
	 * @param parent	Elternelement (zu dem relativ die Iconpfade interpretiert werden)
	 * @param icons	Ressourcennamen der Icons zu den Einträgen
	 */
	public IconListCellRenderer(final Container parent, final String[] icons) {
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
	public IconListCellRenderer(final Container parent, final List<String> icons) {
		this(parent,(icons==null)?new String[0]:icons.toArray(String[]::new));
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public IconListCellRenderer(final List<ImageIcon> icons) {
		this((icons==null)?new ImageIcon[0]:icons.toArray(ImageIcon[]::new));
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public IconListCellRenderer(final ImageIcon[] icons) {
		this.icons=(icons==null)?new ImageIcon[0]:icons;
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public IconListCellRenderer(final Icon[] icons) {
		this.icons=(icons==null)?new ImageIcon[0]:icons;
	}

	/**
	 * Konstruktor der Klasse
	 * @param icons	Icons zu den Einträgen
	 */
	public IconListCellRenderer(final Images[] icons) {
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
	 * Erstellt eine Iconsliste zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 * aus Iconnamen, Icons oder Bildern.
	 * @param parent	Elternelement (zu dem relativ die Iconpfade interpretiert werden)
	 * @param icons	Liste die die Iconnamen, Icons und Bilder enthält.
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
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
	 * Erstellt eine Iconsliste zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 * aus Iconnamen, Icons oder Bildern.
	 * @param parent	Elternelement (zu dem relativ die Iconpfade interpretiert werden)
	 * @param icons	Liste die die Iconnamen, Icons und Bilder enthält.
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildIconsList(final Container parent, final List<Object> icons) {
		return buildIconsList(parent,(icons==null)?new Object[0]:icons.toArray(Object[]::new));
	}

	/**
	 * Erstellt für eine Reihe von Kundentypen eine passende Iconsliste
	 * zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}.
	 * @param clientTypeNames	Kundentypennamen für die die Icons ermittelt werden sollen
	 * @param model	Model-Objekt aus dem die Icons ausgelesen werden
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildClientTypeIcons(final String[] clientTypeNames, final EditModel model) {
		final AnimationImageSource imageSource=new AnimationImageSource();
		final Icon[] result=new Icon[(clientTypeNames==null)?0:clientTypeNames.length];
		if (clientTypeNames!=null) for (int i=0;i<clientTypeNames.length;i++) if (clientTypeNames[i]!=null) {
			String icon=model.clientData.getIcon(clientTypeNames[i]);
			if (icon==null || icon.isBlank()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
			result[i]=new ImageIcon(imageSource.get(icon,model.animationImages,16));
		}
		return result;
	}

	/**
	 * Erstellt für eine Reihe von Kundentypen eine passende Iconsliste
	 * zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}.
	 * @param clientTypeNames	Kundentypennamen für die die Icons ermittelt werden sollen
	 * @param model	Model-Objekt aus dem die Icons ausgelesen werden
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildClientTypeIcons(final List<String> clientTypeNames, final EditModel model) {
		return buildClientTypeIcons((clientTypeNames==null)?new String[0]:clientTypeNames.toArray(String[]::new),model);
	}

	/**
	 * Erstellt für eine Reihe von Ressourcen eine passende Iconsliste
	 * zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}.
	 * @param resourceTypeNames	Ressourcennamen für die die Icons ermittelt werden sollen
	 * @param model	Model-Objekt aus dem die Icons ausgelesen werden
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildResourceTypeIcons(final String[] resourceTypeNames, final EditModel model) {
		final AnimationImageSource imageSource=new AnimationImageSource();
		final Icon[] result=new Icon[(resourceTypeNames==null)?0:resourceTypeNames.length];
		if (resourceTypeNames!=null) for (int i=0;i<resourceTypeNames.length;i++) if (resourceTypeNames[i]!=null) {
			final ModelResource resource=model.resources.get(resourceTypeNames[i]);
			String icon=null;
			if (resource!=null) icon=resource.getIcon();
			if (icon==null || icon.isBlank()) icon=ModelSurfaceAnimatorBase.DEFAULT_OPERATOR_ICON_NAME;
			result[i]=new ImageIcon(imageSource.get(icon,model.animationImages,16));
		}
		return result;
	}

	/**
	 * Erstellt für eine Reihe von Ressourcen eine passende Iconsliste
	 * zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}.
	 * @param resourceTypeNames	Ressourcennamen für die die Icons ermittelt werden sollen
	 * @param model	Model-Objekt aus dem die Icons ausgelesen werden
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildResourceTypeIcons(final List<String> resourceTypeNames, final EditModel model) {
		return buildResourceTypeIcons((resourceTypeNames==null)?new String[0]:resourceTypeNames.toArray(String[]::new),model);
	}

	/**
	 * Erstellt für eine Reihe von Transportern eine passende Iconsliste
	 * zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}.
	 * @param transporterTypeNames	Transporternnamen für die die Icons ermittelt werden sollen
	 * @param model	Model-Objekt aus dem die Icons ausgelesen werden
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildTransporterTypeIcons(final String[] transporterTypeNames, final EditModel model) {
		final AnimationImageSource imageSource=new AnimationImageSource();
		final Icon[] result=new Icon[(transporterTypeNames==null)?0:transporterTypeNames.length];
		if (transporterTypeNames!=null) for (int i=0;i<transporterTypeNames.length;i++) if (transporterTypeNames[i]!=null) {
			final ModelTransporter transporter=model.transporters.get(transporterTypeNames[i]);
			String icon=null;
			if (transporter!=null) icon=transporter.getEastLoadedIcon();
			if (icon==null || icon.isBlank()) icon=ModelSurfaceAnimatorBase.DEFAULT_TRANSPORTER_EAST_ICON_NAME;
			result[i]=new ImageIcon(imageSource.get(icon,model.animationImages,16));
		}
		return result;
	}

	/**
	 * Erstellt für eine Reihe von Transportern eine passende Iconsliste
	 * zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}.
	 * @param transporterTypeNames	Transporternnamen für die die Icons ermittelt werden sollen
	 * @param model	Model-Objekt aus dem die Icons ausgelesen werden
	 * @return	Liste mit Icons zur Verwendung in {@link IconListCellRenderer#IconListCellRenderer(Icon[])}
	 */
	public static Icon[] buildTransporterTypeIcons(final List<String> transporterTypeNames, final EditModel model) {
		return buildTransporterTypeIcons((transporterTypeNames==null)?new String[0]:transporterTypeNames.toArray(String[]::new),model);
	}
}