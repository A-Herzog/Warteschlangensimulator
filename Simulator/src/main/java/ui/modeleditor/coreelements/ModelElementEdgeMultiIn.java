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
package ui.modeleditor.coreelements;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import language.Language;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.elements.ModelElementEdge;

/**
 * Dieses Interface signalisiert, dass das Element mehrere einlaufende Kanten besitzen kann
 * @author Alexander Herzog
 * @see ModelElementEdge#isConnectionOk()
 */
public interface ModelElementEdgeMultiIn {
	/**
	 * Einlaufende Kanten
	 * @return Einlaufende Kanten
	 */
	ModelElementEdge[] getEdgesIn();

	/**
	 * Generiert Kontextmenü-Einträge zur Konfiguration der einlaufenden Kanten
	 * @param popupMenu	Kontextmenü zu dem die Einträge hinzugefügt werden sollen
	 * @param surface	Zeichenfläche
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so können über das Kontextmenü keine Änderungen an dem Modell vorgenommen werden
	 * @return	Gibt <code>true</code> zurück, wenn Elemente in das Kontextmenü eingefügt wurden (und ggf. ein Separator vor dem nächsten Abschnitt gesetzt werden sollte)
	 */
	default boolean addEdgesInContextMenu(final JPopupMenu popupMenu, final ModelSurface surface, final boolean readOnly) {
		JMenuItem item;
		final Icon icon=Images.EDIT_EDGES_DELETE.getIcon();
		boolean needSeparator=false;

		final ModelElementEdge[] connectionsIn=getEdgesIn();
		if (connectionsIn!=null && connectionsIn.length>0) {
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.PopupMenu.RemoveEdgesIn")));
			item.addActionListener(e->{
				for (ModelElementEdge element: connectionsIn) surface.remove(element);
			});
			if (icon!=null) item.setIcon(icon);
			item.setEnabled(!readOnly);

			final JMenu menu=new JMenu(Language.tr("Surface.Connection.LineMode.ChangeAllEdgesIn"));
			popupMenu.add(menu);

			menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Global"),Images.MODEL.getIcon()));
			item.setEnabled(!readOnly);
			item.addActionListener(e->setEdgeInLineMode(null));
			menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.Direct"),Images.EDGE_MODE_DIRECT.getIcon()));
			item.setEnabled(!readOnly);
			item.addActionListener(e->setEdgeInLineMode(ModelElementEdge.LineMode.DIRECT));
			menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLine"),Images.EDGE_MODE_MULTI_LINE.getIcon()));
			item.setEnabled(!readOnly);
			item.addActionListener(e->setEdgeInLineMode(ModelElementEdge.LineMode.MULTI_LINE));
			menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.MultiLineRounded"),Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon()));
			item.setEnabled(!readOnly);
			item.addActionListener(e->setEdgeInLineMode(ModelElementEdge.LineMode.MULTI_LINE_ROUNDED));
			menu.add(item=new JMenuItem(Language.tr("Surface.Connection.LineMode.CubicCurve"),Images.EDGE_MODE_CUBIC_CURVE.getIcon()));
			item.setEnabled(!readOnly);
			item.addActionListener(e->setEdgeInLineMode(ModelElementEdge.LineMode.CUBIC_CURVE));

			needSeparator=true;
		}

		return needSeparator;
	}

	/**
	 * Stellt den Darstellungsmodus für alle einlaufenden Kanten ein.
	 * @param lineMode	Neuer Darstellungsmodus
	 */
	default void setEdgeInLineMode(final ModelElementEdge.LineMode lineMode) {
		final ModelElementEdge[] connectionsIn=getEdgesIn();
		for (ModelElementEdge edge: connectionsIn) {
			edge.setLineMode(lineMode);
			edge.fireChanged();
		}
	}
}
