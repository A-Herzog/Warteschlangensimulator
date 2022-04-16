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

import language.Language;

/**
 * Untermen� f�r ein {@link ScriptPopup}-Men�
 * @author Alexander Herzog
 * @see ScriptPopup
 */
public class ScriptPopupItemSub extends ScriptPopupItem {
	/** Liste der Unterelemente in diesem Untermen� */
	private final List<ScriptPopupItem> children;

	/**
	 * Konstruktor der Klasse
	 * @param name	Name des Eintrags (kann <code>null</code> sein)
	 * @param hint	Tooltip des Eintrags (kann <code>null</code> sein)
	 * @param icon	Icon f�r den Eintrag (kann <code>null</code> sein)
	 */
	public ScriptPopupItemSub(final String name, final String hint, final Icon icon) {
		super(name,hint,icon);
		children=new ArrayList<>();
	}

	/**
	 * F�gt ein neues Element zu dem Untermen� hinzu
	 * @param child	Hinzuzuf�gendes Element
	 */
	public void addChild(final ScriptPopupItem child) {
		children.add(child);
	}

	/**
	 * F�gt einen Trenner zu dem Untermen� hinzu
	 */
	public void addSeparator() {
		children.add(null);
	}

	/**
	 * F�gt die Elemente des Untermen�s zu einem {@link JMenu}-Men� hinzu
	 * @param popupMenu	Popupmen� zu dem die Eintr�ge dieser Liste als Untermen� hinzugef�gt werden sollen
	 * @param clickedItem	Callback das aufgerufen werden soll, wenn einer der Men�punkte angeklickt wurde
	 * @param allowAdd	Erlaubt das Vorabpr�fen, ob der Befehl im Popupmen� angezeigt werden soll
	 * @param indexFrom	Index des ersten einzuf�genden Eintrags (inklusive)
	 * @param indexTo	Index des letzten einzuf�genden Eintrags (inklusive)
	 * @see #addChildrenToMenu(JMenu, Consumer, Predicate)
	 */
	private void addChildrenToMenu(final JMenu popupMenu, final Consumer<ScriptPopupItem> clickedItem, final Predicate<ScriptPopupItem> allowAdd, final int indexFrom, int indexTo) {
		while (children.get(indexTo)==null && indexTo>indexFrom) indexTo--;
		boolean lastObjIsSeparator=false;
		for (int i=indexFrom;i<=indexTo;i++) {
			final ScriptPopupItem child=children.get(i);

			/* Trenner */
			if (child==null) {
				if (popupMenu.getMenuComponentCount()>0 && !lastObjIsSeparator) {
					popupMenu.addSeparator();
					lastObjIsSeparator=true;
				}
				continue;
			}
			/* Untermen� */
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
	}

	/**
	 * F�gt die Elemente des Untermen�s zu einem {@link JPopupMenu}-Men� hinzu
	 * @param popupMenu	Popupmen� zu dem die Eintr�ge dieser Liste als Untermen� hinzugef�gt werden sollen
	 * @param clickedItem	Callback das aufgerufen werden soll, wenn einer der Men�punkte angeklickt wurde
	 * @param allowAdd	Erlaubt das Vorabpr�fen, ob der Befehl im Popupmen� angezeigt werden soll
	 * @param indexFrom	Index des ersten einzuf�genden Eintrags (inklusive)
	 * @param indexTo	Index des letzten einzuf�genden Eintrags (inklusive)
	 * @see #addChildrenToMenu(JPopupMenu, Consumer, Predicate)
	 */
	private void addChildrenToMenu(final JPopupMenu popupMenu, final Consumer<ScriptPopupItem> clickedItem, final Predicate<ScriptPopupItem> allowAdd, final int indexFrom, int indexTo) {
		while (children.get(indexTo)==null && indexTo>indexFrom) indexTo--;
		boolean lastObjIsSeparator=false;
		for (int i=indexFrom;i<=indexTo;i++) {
			final ScriptPopupItem child=children.get(i);

			/* Trenner */
			if (child==null) {
				if (popupMenu.getComponentCount()>0 && !lastObjIsSeparator) {
					popupMenu.addSeparator();
					lastObjIsSeparator=true;
				}
				continue;
			}
			/* Untermen� */
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
	}

	/**
	 * Maximalanzahl an Eintr�gen in einem Popupmen�
	 * auf der obersten Ebene
	 * @see #addChildrenToMenu(JPopupMenu, Consumer, Predicate, int, int)
	 */
	private static final int MAX_ITEMS_PER_POPUPMENU=60;

	/**
	 * Maximalanzahl an Eintr�gen in einem Popupmen�
	 * auf einer unteren Ebene
	 * @see #addChildrenToMenu(JMenu, Consumer, Predicate, int, int)
	 */
	private static final int MAX_ITEMS_PER_MENU=45;

	/**
	 * F�gt die Elemente des Untermen�s zu einem {@link JMenu}-Men� hinzu
	 * @param popupMenu	Popupmen� zu dem die Eintr�ge dieser Liste als Untermen� hinzugef�gt werden sollen
	 * @param clickedItem	Callback das aufgerufen werden soll, wenn einer der Men�punkte angeklickt wurde
	 * @param allowAdd	Erlaubt das Vorabpr�fen, ob der Befehl im Popupmen� angezeigt werden soll
	 */
	private void addChildrenToMenu(final JMenu popupMenu, final Consumer<ScriptPopupItem> clickedItem, final Predicate<ScriptPopupItem> allowAdd) {
		final int count=children.size();
		if (count<=MAX_ITEMS_PER_MENU) {
			addChildrenToMenu(popupMenu,clickedItem,allowAdd,0,children.size()-1);
			return;
		}

		final int parts=count/MAX_ITEMS_PER_MENU+((count%MAX_ITEMS_PER_MENU!=0)?1:0);
		for (int part=0;part<parts;part++) {
			final JMenu sub=new JMenu("");
			addChildrenToMenu(sub,clickedItem,allowAdd,part*MAX_ITEMS_PER_MENU,Math.min((part+1)*MAX_ITEMS_PER_MENU-1,count-1));
			final int subCount=sub.getMenuComponentCount();
			if (subCount>0) {
				String name1=((JMenuItem)sub.getMenuComponent(0)).getText();
				String name2=((JMenuItem)sub.getMenuComponent(subCount-1)).getText();
				if (name1.length()>10) name1=name1.substring(0,10);
				if (name2.length()>10) name2=name2.substring(0,10);
				sub.setText(String.format(Language.tr("ScriptPopup.Part"),part+1,parts,name1,name2));
				popupMenu.add(sub);
			}
		}
	}

	/**
	 * F�gt die Elemente des Untermen�s zu einem {@link JPopupMenu}-Men� hinzu
	 * @param popupMenu	Popupmen� zu dem die Eintr�ge dieser Liste als Untermen� hinzugef�gt werden sollen
	 * @param clickedItem	Callback das aufgerufen werden soll, wenn einer der Men�punkte angeklickt wurde
	 * @param allowAdd	Erlaubt das Vorabpr�fen, ob der Befehl im Popupmen� angezeigt werden soll
	 */
	public void addChildrenToMenu(final JPopupMenu popupMenu, final Consumer<ScriptPopupItem> clickedItem, final Predicate<ScriptPopupItem> allowAdd) {
		final int count=children.size();
		if (count<=MAX_ITEMS_PER_POPUPMENU) {
			addChildrenToMenu(popupMenu,clickedItem,allowAdd,0,children.size()-1);
			return;
		}

		final int parts=count/MAX_ITEMS_PER_POPUPMENU+((count%MAX_ITEMS_PER_POPUPMENU!=0)?1:0);
		for (int part=0;part<parts;part++) {
			final JMenu sub=new JMenu("");
			addChildrenToMenu(sub,clickedItem,allowAdd,part*MAX_ITEMS_PER_POPUPMENU,Math.min((part+1)*MAX_ITEMS_PER_POPUPMENU-1,count-1));
			final int subCount=sub.getMenuComponentCount();
			if (subCount>0) {
				String name1=((JMenuItem)sub.getMenuComponent(0)).getText();
				String name2=((JMenuItem)sub.getMenuComponent(subCount-1)).getText();
				if (name1.length()>10) name1=name1.substring(0,10);
				if (name2.length()>10) name2=name2.substring(0,1);
				sub.setText(String.format(Language.tr("ScriptPopup.Part"),part+1,parts,name1,name2));
				popupMenu.add(sub);
			}
		}
	}
}
