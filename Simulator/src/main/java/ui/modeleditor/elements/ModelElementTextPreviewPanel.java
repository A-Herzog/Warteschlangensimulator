/**
 * Copyright 2024 Alexander Herzog
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
package ui.modeleditor.elements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.JPanel;

import mathtools.NumberTools;

/**
 * Zeigt eine Vorschau eines formatierten Texten in der Form,
 * wie er auch auf der Zeichenfläche gezeichnet werden würde, an.
 */
public class ModelElementTextPreviewPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=7533755111593941063L;

	/**
	 * Renderer für einfache Texte
	 */
	private final ModelElementTextRendererPlain rendererPlain;

	/**
	 * Renderer für Texte mit Markdown- und/oder LaTeX-Formatierungen
	 */
	private final ModelElementTextRendererMarkDownLaTeX rendererExt;

	/**
	 * Aktuell gewählter Renderer (kann <code>null</code> sein)
	 */
	private ModelElementTextRenderer activeRenderer;

	/**
	 * Textfarbe
	 */
	private Color textColor=Color.BLACK;

	/**
	 * Muss die Größe des Bereiches beim nächsten Zeichen neu berechnet werden?
	 */
	private boolean needRecalc;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelElementTextPreviewPanel() {
		rendererPlain=new ModelElementTextRendererPlain();
		rendererExt=new ModelElementTextRendererMarkDownLaTeX();
		activeRenderer=null;
	}

	/**
	 * Stellt den darzustelltenden Text inkl. der Konfigurationsdaten ein.
	 * @param interpretMarkdown	Markdown interpretieren?
	 * @param interpretLaTeX	LaTeX interpretieren?
	 * @param interpretSymbols	HTML- und LaTeX-Symbole interpretieren?
	 * @param text	Anzuzeigender Text
	 * @param color	Textfarbe
	 * @param fillColor	Hintergrundfarbe (darf <code>null</code> sein)
	 * @param fillAlpha	Deckkraft für den Hintergrund (0..1)
	 * @param shadowColor	Schattenfarbe (darf <code>null</code> sein)
	 * @param shadowAlpha	Deckkraft für den Schatten (0..1)
	 * @param textSize	Schriftgröße
	 * @param bold	Text fett drucken?
	 * @param italic	Text kursiv drucken?
	 * @param fontFamily	Schriftart
	 * @param textAlign	Ausrichtung
	 */
	public void set(final boolean interpretMarkdown, final boolean interpretLaTeX, final boolean interpretSymbols, final String text, final Color color, final Color fillColor, final double fillAlpha, final Color shadowColor, final double shadowAlpha, final int textSize, final boolean bold, final boolean italic, final FontCache.FontFamily fontFamily, final ModelElementText.TextAlign textAlign) {
		if (interpretMarkdown || interpretLaTeX) {
			activeRenderer=rendererExt;
			rendererExt.setRenderMode(interpretMarkdown,interpretLaTeX);
		} else {
			activeRenderer=rendererPlain;
		}

		activeRenderer.setText(text,interpretSymbols);
		activeRenderer.setBackgroundColor(fillColor,fillAlpha);
		activeRenderer.setShadowColor(shadowColor,shadowAlpha);
		activeRenderer.setStyle(textSize,bold,italic,fontFamily.name,textAlign);
		textColor=color;
		needRecalc=true;

		invalidate();
		repaint();
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);

		g.setColor(NumberTools.getTextFieldDefaultBackground());
		g.fillRect(0,0,getWidth(),getHeight());

		if (activeRenderer==null) return;

		if (needRecalc) activeRenderer.calc(g,1.0);

		final int canvasW=activeRenderer.getWidth();
		final int canvasH=activeRenderer.getHeight();
		if ((getWidth()!=canvasW || getHeight()!=canvasH) && needRecalc) {
			final Dimension d=new Dimension(canvasW,canvasH);
			setMinimumSize(d);
			setSize(d);
			setPreferredSize(d);
			setMaximumSize(d);
		}

		needRecalc=false;
		activeRenderer.draw(g,1,1,textColor);
	}
}
