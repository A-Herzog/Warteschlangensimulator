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
package ui.modeleditor;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import language.Language;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElementListGroup;
import ui.modeleditor.coreelements.ModelElementPosition;

/**
 * Ermöglicht das Zeichnen der Liste der Modell-Elemente zur Auswahl eines Elements zum Einfügen in die Liste
 * @author Alexander Herzog
 * @param <E>	Müssen von <code>ModelElementPosition</code> abgeleitete Elemente sein.
 */
public class ModelElementCatalogListCellRenderer<E extends ModelElementPosition> implements ListCellRenderer<E> {
	/**
	 * Zoomlevel
	 * @see #setZoom(double)
	 */
	private double zoom=1.0;

	/**
	 * Zoomlevel einstellen
	 * @param zoom	Neues Zoomlevel (1.0=100%)
	 */
	public void setZoom(final double zoom) {
		this.zoom=zoom;
	}

	/** Icon für "-" */
	private Icon iconMinus=null;
	/** Icon für "+" */
	private Icon iconPlus=null;

	/**
	 * Liefert den Listenzellen-Renderer für die Gruppennamen
	 * @param groupName	Gruppenname
	 * @param statusOpen	Soll die Gruppe geöffnet dargestellt werden?
	 * @return	Listenzellen-Renderer für die Gruppennamen
	 */
	private Component getGroupRenderer(final String groupName, final boolean statusOpen) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));

		/* verbraucht mehr Speicher: final JLabel label=new JLabel("<html><b>"+groupName+"</b></html>"); */
		final JLabel label=new JLabel(groupName);
		panel.add(label);
		final Font font=label.getFont();
		label.setFont(new Font(font.getName(),Font.BOLD,font.getSize()));

		final Icon icon;
		if (statusOpen) {
			if (iconMinus==null) iconMinus=Images.MODELEDITOR_GROUP_MINUS.getIcon();
			icon=iconMinus;
		} else {
			if (iconPlus==null) iconPlus=Images.MODELEDITOR_GROUP_PLUS.getIcon();
			icon=iconPlus;
		}
		if (icon!=null) label.setIcon(icon);

		panel.setToolTipText(Language.tr("Editor.TemplateFilter.CategoryTooltip"));

		return panel;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof ModelElementListGroup) {
			return getGroupRenderer(((ModelElementListGroup)value).getTypeName(),((ModelElementListGroup)value).isShowSub());
		}

		boolean groupOpen=true;
		int lastGroupIndex=index-1;
		while (lastGroupIndex>=0) {
			final ModelElementPosition element=list.getModel().getElementAt(lastGroupIndex);
			if (element instanceof ModelElementListGroup) {
				groupOpen=((ModelElementListGroup)element).isShowSub();
				break;
			}
			lastGroupIndex--;
		}

		if (groupOpen) {
			return ElementRendererTools.getElementRenderer(value,zoom,false,isSelected);
		} else {
			return ElementRendererTools.getEmptyRenderer();
		}
	}
}