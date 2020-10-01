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
package ui.quickaccess;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import language.Language;
import ui.images.Images;

/**
 * Erstellt Schnellzugriffeinträge basierend auf den Menüeinträgen
 * @author Alexander Herzog
 * @see JQuickAccessBuilder
 */
public class JQuickAccessBuilderMenu extends JQuickAccessBuilder {
	private static final Icon DEFAULT_ICON;

	static {
		DEFAULT_ICON=Images.GENERAL_APPLICATION.getIcon();
	}

	/**
	 * Konstruktor der Klasse
	 * @param quickAccessText	Eingegebener Text
	 */
	public JQuickAccessBuilderMenu(String quickAccessText) {
		super(Language.tr("QuickAccess.Menu"),Language.tr("QuickAccess.Menu.Hint"),quickAccessText,false);
	}

	private static final Consumer<JQuickAccessRecord> callback=record->{
		final JMenuItem clickedItem=((JMenuItem)record.data);
		final ActionEvent event=new ActionEvent(clickedItem,ActionEvent.ACTION_PERFORMED,clickedItem.getActionCommand());
		for (ActionListener listener: clickedItem.getActionListeners()) {
			listener.actionPerformed(event);
		}
	};

	/**
	 * Führt die eigentliche Verarbeitung durch.
	 * @param menu	Menü aus dem die Menüpunkte ausgelesen werden sollen
	 */
	public void work(final JMenuBar menu) {
		if (quickAccessText.length()<2) return;

		for (int i=0;i<menu.getMenuCount();i++) {
			processMenu(menu.getMenu(i),"");
		}
	}

	private enum IgnoreMode {Normal, OnlyFirst, Ignore}

	private IgnoreMode getIgnoreMode(final String menuName) {
		if (menuName.equals(Language.tr("Main.Menu.File.LoadExample"))) return IgnoreMode.OnlyFirst;
		if (menuName.equals(Language.tr("Main.Menu.File.RecentlyUsed"))) return IgnoreMode.Ignore;
		return IgnoreMode.Normal;
	}

	private void processMenu(final JMenu menu, final String parentPath) {
		if (menu==null) return;

		final IgnoreMode ignoreMode=getIgnoreMode(menu.getText());
		if (ignoreMode==IgnoreMode.Ignore) return;

		final String path=parentPath+menu.getText();
		for (int i=0;i<menu.getItemCount();i++) {
			if (ignoreMode==IgnoreMode.OnlyFirst && i>0) break;
			final JMenuItem item=menu.getItem(i);
			if (item==null) continue;
			if (item instanceof JMenu) {
				processMenu((JMenu)item,path+" - ");
				continue;
			}

			Icon icon=item.getIcon();
			if (icon==null) icon=DEFAULT_ICON;

			test(path,item.getText(),icon,callback,item);
		}
	}
}