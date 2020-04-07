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

import java.awt.Font;

import javax.swing.JMenuItem;

/**
 * Popupmenü-Eintrag der eine nichtanklickbare Überschrift anzeigt
 * @author Alexander Herzog
 * @see ScriptPopup
 * @see ScriptPopupItem
 */
public class ScriptPopupItemTitle extends ScriptPopupItem {
	private final String title;

	/**
	 * Konstruktor der Klasse
	 * @param title	Name des Eintrags (kann <code>null</code> sein)
	 */
	public ScriptPopupItemTitle(final String title) {
		super(title,null,null);
		this.title=title;
	}

	@Override
	protected JMenuItem buildMenuItem() {
		final JMenuItem item=(title==null || title.trim().isEmpty())?new JMenuItem():new JMenuItem(title.trim());
		item.setEnabled(false);
		final Font oldFont=item.getFont();
		item.setFont(new Font(oldFont.getFontName(),Font.BOLD,oldFont.getSize()));

		return item;
	}
}
