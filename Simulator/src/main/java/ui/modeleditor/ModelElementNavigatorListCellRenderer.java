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

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Ermöglicht das Zeichnen der Liste der aktiven Modell-Elemente (Navigator)
 * @author Alexander Herzog
 * @param <E>	Müssen von <code>ModelElementBox</code> abgeleitete Elemente sein.
 */
public class ModelElementNavigatorListCellRenderer<E extends ModelElementBox> implements ListCellRenderer<E> {
	/**
	 * Sollen Farbverläufe im Hintergrund verwendet werden?
	 */
	private final ElementRendererTools.GradientStyle useGradient;

	/**
	 * Zoomlevel
	 * @see #setZoom(double)
	 */
	private double zoom=1.0;

	/**
	 * Konstruktor der Klasse
	 * @param useGradient	Sollen Farbverläufe im Hintergrund verwendet werden?
	 */
	public ModelElementNavigatorListCellRenderer(final ElementRendererTools.GradientStyle useGradient) {
		this.useGradient=useGradient;
	}

	/**
	 * Stellt den Zoomlevel ein
	 * @param zoom	Neuer Zoomlevel (1.0=100%)
	 */
	public void setZoom(final double zoom) {
		this.zoom=zoom;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
		return ElementRendererTools.getElementRenderer(value,zoom,true,isSelected,useGradient);
	}
}
