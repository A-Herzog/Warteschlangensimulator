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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * Textfeld mit einem optionalen Platzhalter, der in Grau angezeigt wird,
 * wenn das Element leer ist.
 * @author Alexander Herzog
 */
public class JPlaceholderTextField extends JTextField {
	private static final long serialVersionUID = 7305679732549597115L;

	private String placeholder;

	/**
	 * Konstruktor der Klasse
	 */
	public JPlaceholderTextField() {
		super();
	}

	/**
	 * Konstruktor der Klasse
	 * @param doc	Objekt zur Speicherung des Textes; wird <code>null</code> übergeben, so wird das Vorgabeobjekt, welches über <code>createDefaultModel</code> abgerufen wird, verwendet.
	 * @param text	Initialer Text (darf <code>null</code> sein)
	 * @param columns	Anzahl an Spalten zur Berechnung der bevorzugten Breite
	 */
	public JPlaceholderTextField(final Document doc, final String text, final int columns) {
		super(doc,text,columns);
	}

	/**
	 * Konstruktor der Klasse
	 * @param columns	Anzahl an Spalten zur Berechnung der bevorzugten Breite
	 */
	public JPlaceholderTextField(final int columns) {
		super(columns);
	}

	/**
	 * Konstruktor der Klasse
	 * @param text Initialer Text (darf <code>null</code> sein)
	 */
	public JPlaceholderTextField(final String text) {
		super(text);
	}

	/**
	 * Konstruktor der Klasse
	 * @param text	Initialer Text (darf <code>null</code> sein)
	 * @param columns	Anzahl an Spalten zur Berechnung der bevorzugten Breite
	 */
	public JPlaceholderTextField(final String text, final int columns) {
		super(text,columns);
	}

	/**
	 * Liefert den aktuellen Platzhalter
	 * @return	Aktueller Platzhalter (nie <code>null</code>; im Falle eines leeren Platzhalters "")
	 */
	public String getPlaceholder() {
		return (placeholder==null)?"":placeholder;
	}

	/**
	 * Stellt einen neuen Platzhalter ein, der angezeigt wird, wenn das Textelement leer ist.
	 * @param placeholder	Neuer Platzhalter (<code>null</code> wird als "" interpretiert)
	 */
	public void setPlaceholder(final String placeholder) {
		this.placeholder=(placeholder==null)?"":placeholder;
		invalidate();
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);

		if (placeholder==null || placeholder.trim().length()==0 || getText().length()>0) return;

		final Graphics2D g=(Graphics2D)graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getDisabledTextColor());
		g.drawString(placeholder,getInsets().left,graphics.getFontMetrics().getMaxAscent()+getInsets().top);
	}
}