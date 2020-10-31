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
package ui.script;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Untermenü für ein {@link ScriptPopup}-Menü
 * @author Alexander Herzog
 * @see ScriptPopup
 */
public class ScriptPopupItemSub extends ScriptPopupItem {
	/** Liste der Unterelemente in diesem Untermenü */
	private final List<ScriptPopupItem> children;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon für den Eintrag (kann <code>null</code> sein)
	 */
	public ScriptPopupItemSub(final String name, final String hint, final Icon icon) {
		super(name,hint,icon);
		children=new ArrayList<>();
	}

	/**
	 * Fügt ein neues Element zu dem Untermenü hinzu
	 * @param child	Hinzuzufügendes Element
	 */
	public void addChild(final ScriptPopupItem child) {
		children.add(child);
	}

	/**
	 * Fügt einen Trenner zu dem Untermenü hinzu
	 */
	public void addSeparator() {
		children.add(null);
	}

	/**
	 * Fügt die Elemente des Untermenüs zu einem {@link JPopupMenu}-Menü hinzu
	 * @param popupMenu	Popupmenü zu dem die Einträge dieser Liste als Untermenü hinzugefügt werden sollen
	 * @param clickedItem	Callback das aufgerufen werden soll, wenn einer der Menüpunkte angeklickt wurde
	 * @param allowAdd	Erlaubt das Vorabprüfen, ob der Befehl im Popupmenü angezeigt werden soll
	 */
	private void addChildrenToMenu(final JMenu popupMenu, final Consumer<ScriptPopupItem> clickedItem, final Predicate<ScriptPopupItem> allowAdd) {
		boolean lastObjIsSeparator=false;
		for (ScriptPopupItem child: children) {
			/* Trenner */
			if (child==null) {
				if (popupMenu.getMenuComponentCount()>0 && !lastObjIsSeparator) {
					popupMenu.addSeparator();
					lastObjIsSeparator=true;
				}
				continue;
			}
			/* Untermenü */
			if (child instanceof ScriptPopupItemSub) {
				final JMenu sub=child.buildMenu();
				((ScriptPopupItemSub)child).addChildrenToMenu(sub,clickedItem,allowAdd);
				if (sub.getMenuComponentCount()>0) {
					lastObjIsSeparator=false;
					popupMenu.add(sub);
				}
				continue;
			}

			/* Befehl */
			if (allowAdd.test(child)) {
				JMenuItem item=child.buildMenuItem();
				item.addActionListener(e->clickedItem.accept(child));
				lastObjIsSeparator=false;
				popupMenu.add(item);
			}
		}

		/* Trenner am Ende entfernen */
		if (lastObjIsSeparator) {
			popupMenu.remove(popupMenu.getMenuComponent(popupMenu.getMenuComponentCount()-1));
		}
	}

	/**
	 * Fügt die Elemente des Untermenüs zu einem {@link JPopupMenu}-Menü hinzu
	 * @param popupMenu	Popupmenü zu dem die Einträge dieser Liste als Untermenü hinzugefügt werden sollen
	 * @param clickedItem	Callback das aufgerufen werden soll, wenn einer der Menüpunkte angeklickt wurde
	 * @param allowAdd	Erlaubt das Vorabprüfen, ob der Befehl im Popupmenü angezeigt werden soll
	 */
	public void addChildrenToMenu(final JPopupMenu popupMenu, final Consumer<ScriptPopupItem> clickedItem, final Predicate<ScriptPopupItem> allowAdd) {
		boolean lastObjIsSeparator=false;
		for (ScriptPopupItem child: children) {
			/* Trenner */
			if (child==null) {
				if (popupMenu.getComponentCount()>0 && !lastObjIsSeparator) {
					popupMenu.addSeparator();
					lastObjIsSeparator=true;
				}
				continue;
			}
			/* Untermenü */
			if (child instanceof ScriptPopupItemSub) {
				final JMenu sub=child.buildMenu();
				((ScriptPopupItemSub)child).addChildrenToMenu(sub,clickedItem,allowAdd);
				if (sub.getMenuComponentCount()>0) {
					lastObjIsSeparator=false;
					popupMenu.add(sub);
				}
				continue;
			}

			/* Befehl */
			if (allowAdd.test(child)) {
				JMenuItem item=child.buildMenuItem();
				item.addActionListener(e->clickedItem.accept(child));
				lastObjIsSeparator=false;
				popupMenu.add(item);
			}
		}

		/* Trenner am Ende entfernen */
		if (lastObjIsSeparator) {
			popupMenu.remove(popupMenu.getComponent(popupMenu.getComponentCount()-1));
		}
	}
}
