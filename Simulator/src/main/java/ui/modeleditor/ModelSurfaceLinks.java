/**
 * Copyright 2021 Alexander Herzog
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import language.Language;
import ui.help.BookData;
import ui.images.Images;

/**
 * Diese Klasse stellt Zeichenfl�chen-Links f�r die
 * Darstellung in einem {@link ModelSurfacePanel}
 * zur Verf�gung.
 * @author Alexander Herzog
 * @see ModelSurfacePanel
 */
public class ModelSurfaceLinks {

	/**
	 * Zeichenfl�chen-Link
	 */
	public enum Link {
		/**
		 * Zeichenfl�chen-Link: Interaktives Tutorial
		 */
		INTERACTIVE_TUTORIAL(()->Language.tr("Editor.SurfaceTooltip.InteractiveTutorial"),Images.HELP_TUTORIAL_INTERACTIVE),

		/**
		 * Zeichenfl�chen-Link: Tutorial
		 */
		TUTORIAL(()->Language.tr("Editor.SurfaceTooltip.Tutorial"),Images.HELP_TUTORIAL),

		/**
		 * Zeichenfl�chen-Link: Lehrbuch
		 */
		BOOK(()->Language.tr("Editor.SurfaceTooltip.Book"),Images.HELP_TUTORIAL),

		/**
		 * Zeichenfl�chen-Link: Modellgenerator
		 */
		GENERATOR(()->Language.tr("Editor.SurfaceTooltip.Generator"),Images.MODEL_GENERATOR),

		/**
		 * Zeichenfl�chen-Link: Beispielmodelle
		 */
		EXAMPLES(()->Language.tr("Editor.SurfaceTooltip.ExamplesByDialog"),Images.MODEL);

		/**
		 * Name f�r den Zeichenfl�chen-Link
		 */
		private final Supplier<String> name;

		/**
		 * Icon f�r den Zeichenfl�chen-Link
		 */
		private final Image image;

		/**
		 * Konstruktor des Enum
		 * @param name	Name f�r den Zeichenfl�chen-Link
		 * @param image	Icon f�r den Zeichenfl�chen-Link
		 */
		Link(final Supplier<String> name, final Images image) {
			this.name=name;
			this.image=image.getImage();
		}

		/**
		 * Liefert den Name des Zeichenfl�chen-Links
		 * @return	Name des Zeichenfl�chen-Link
		 */
		public String getName() {
			return name.get();
		}

		/**
		 * Liefert das Icon f�r den Zeichenfl�chen-Link
		 * @return	Icon f�r den Zeichenfl�chen-Link
		 */
		public Image getImage() {
			return image;
		}
	}

	/**
	 * Liste der Zeichenfl�chen-Links in der Reihenfolge,
	 * in der sie auch angezeigt werden sollen
	 */
	private final List<Link> links;

	/**
	 * Positionen der Zeichenfl�chen-Links
	 * @see #drawLinks(Graphics, int, int)
	 */
	private final Rectangle[] linkPositions;

	/**
	 * Konstruktor der Klasse
	 */
	public ModelSurfaceLinks() {
		links=new ArrayList<>();

		links.add(Link.INTERACTIVE_TUTORIAL);
		links.add(Link.TUTORIAL);
		if (BookData.getInstance().isDataAvailable()) links.add(Link.BOOK);
		links.add(Link.GENERATOR);
		links.add(Link.EXAMPLES);

		linkPositions=new Rectangle[links.size()];
	}

	/**
	 * Zeichnet die Zeichenfl�chen-Links in ein Grafikausgabeobjekt
	 * @param graphics	Grafikausgabeobjekt
	 * @param xStart	Start x-Koordinate
	 * @param yStart	Start y-Koordinate
	 * @return	Koordinaten der gezeichneten Links
	 */
	public Rectangle[] drawLinks(final Graphics graphics, final int xStart, final int yStart) {
		final int lineH=graphics.getFontMetrics().getAscent()+graphics.getFontMetrics().getDescent();
		final int lineYShift=graphics.getFontMetrics().getAscent();

		graphics.setColor(Color.BLUE.darker());
		int y=yStart;

		for (int i=0;i<linkPositions.length;i++) {
			final Link link=links.get(i);
			final String text=link.getName();
			final Image image=link.getImage();

			if (image!=null) graphics.drawImage(image,xStart,y-8,null);

			int textY=y-lineH/2;
			graphics.drawString(text,xStart+20,textY+lineYShift);
			textY+=lineH;

			linkPositions[i]=new Rectangle(20,y-8,20+graphics.getFontMetrics().stringWidth(text),lineH);

			y+=40;
		}

		return linkPositions;
	}

	/**
	 * Liefert der Link zu einer Link-Nummer
	 * @param nr	Link-Nummer
	 * @return	Link (kann <code>null</code> sein, wenn die Nummer zu keinem der gezeichneten Links passt)
	 */
	public Link getLink(final int nr) {
		if (nr<0 || nr>=links.size()) return null;
		return links.get(nr);
	}
}
