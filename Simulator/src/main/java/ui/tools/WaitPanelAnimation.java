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
package ui.tools;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;

import org.oxbow.swingbits.util.swing.AncestorAdapter;

import tools.SetupData;

/**
 * Zeigt in einem Panel eine Warteanimation während einer laufenden Simulation an.
 * @see WaitPanel
 */
public class WaitPanelAnimation extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=3366182977093782762L;

	/**
	 * Liste der im "res"-Ordner verfügbaren Bilder
	 */
	private static final List<String> imageNames=List.of("user.png","user-female.png","user-green.png","user-orange.png","user-red.png");

	/**
	 * Zuordnung von Namen zu geladenen Bildern
	 * @see #getImage(String)
	 */
	private Map<String,BufferedImage> images;

	/**
	 * Bildobjekt innerhalb des Panels
	 */
	private static class DrawImage {
		/**
		 * Anzahl an Pixeln, um die das Bild pro {@link #move()}-Aufruf
		 * verschoben werden soll.
		 * @see #move()
		 */
		private static final int STEP_SIZE=7;

		/**
		 * Bilddatei, die diese Klasse kapselt
		 */
		private final BufferedImage image;

		/**
		 * Aktuell x-Position auf der Zeichenfläche
		 */
		private int x;

		/**
		 * Konstruktor der Klasse
		 * @param image	Bilddatei, die diese Klasse kapselt
		 */
		public DrawImage(final BufferedImage image) {
			this.image=image;
			x=-(image.getWidth()+(int)(Math.random()*40));
		}

		/**
		 * Zeichnet das Bild
		 * @param g	Grafikkontext, in den das Bild gezeichnet werden soll
		 */
		public void draw(final Graphics g) {
			g.drawImage(image,x,0,null);
		}

		/**
		 * Verschiebt das Bild nach rechts.
		 */
		public void move() {
			x+=STEP_SIZE;
		}

		/**
		 * Ist das Bild von links aus vollständig in den sichtbaren Bereich gerutscht?
		 * @return	Bild (bezogen auf den linken Rand) vollständig sichtbar?
		 */
		public boolean isFullyVisible() {
			return x>=0;
		}

		/**
		 * Ist das Bild nach rechts vollständig aus dem sichtbaren Bereich gerutscht?
		 * @param width	Breite des Zeichenbereichs
		 * @return	Bild (bezogen auf den rechten Rand) nicht mehr sichtbar?
		 */
		public boolean canRemove(final int width) {
			return x>width;
		}
	}

	/**
	 * Liste der aktuell angezeigten Bilder
	 */
	private final List<DrawImage> drawImages;

	/**
	 * Timer zur Aktualisierung der Anzeige
	 * (ist <code>null</code>, wenn nicht aktiv)
	 */
	private Timer timer;

	/**
	 * Konstruktor der Klasse
	 */
	public WaitPanelAnimation() {
		final Dimension size=new Dimension(200,48+20);
		setSize(size);
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		drawImages=new ArrayList<>();
		addNextImage();
		addAncestorListener(new AncestorAdapter() {
			@Override
			public void ancestorAdded (AncestorEvent event) {
				if (!SetupData.getSetup().simulationProgressAnimation) return;
				timer=new Timer("SimProgressAnimation",false);
				timer.schedule(new TimerTask() {
					@Override public void run() {step();}
				},2500,100);
			}
			@Override
			public void ancestorRemoved (AncestorEvent event) {
				if (timer!=null) {
					timer.cancel();
					timer=null;
					drawImages.clear();
					addNextImage();
				}
			}
		});
	}

	/**
	 * Lädt ein einzelnes Bild aus den Ressourcen.
	 * @param name	Dateiname des Bildes
	 * @return	Bildobjekt
	 */
	private BufferedImage loadImage(final String name) {
		final URL url=getClass().getResource("res/"+name);
		try {
			return ImageIO.read(url);
		} catch (IOException e) {
			assert(false);
			return null;
		}
	}

	/**
	 * Liefert ein Bild entweder aus der Ressource oder aus dem Cache
	 * @param name	Dateiname des Bildes
	 * @return	Bildobjekt
	 */
	private BufferedImage getImage(final String name) {
		if (images==null) images=new HashMap<>();
		return images.computeIfAbsent(name,key->loadImage(key));
	}

	/**
	 * Liefert ein zufälliges Bild aus der Liste der verfügbaren Bilder.
	 * @return	Bildobjekt
	 * @see #imageNames
	 */
	private DrawImage getRandomDrawImage() {
		final int nr=(int)(Math.random()*imageNames.size());
		return new DrawImage(getImage(imageNames.get(nr)));
	}

	@Override
	public void paint(final Graphics g) {
		super.paint(g);
		drawImages.forEach(drawImage->drawImage.draw(g));
	}

	/**
	 * Fügt ein zufälliges neues Bild zu {@link #drawImages} hinzu.
	 * @see #drawImages
	 */
	private void addNextImage() {
		drawImages.add(getRandomDrawImage());
	}

	/**
	 * Verschiebt alle Bilder um {@link DrawImage#STEP_SIZE} nach rechts,
	 * entfernt gegebenenfalls nicht mehr benötigte Bilder und fügt
	 * ggf. auch neue Bilder hinzu.
	 */
	private void step() {
		final int width=getWidth();
		int index=0;
		boolean needNext=true;
		while (index<drawImages.size()) {
			final DrawImage drawImage=drawImages.get(index);
			drawImage.move();
			if (drawImage.canRemove(width)) {
				drawImages.remove(index);
				continue;
			}
			if (!drawImage.isFullyVisible()) needNext=false;
			index++;
		}
		if (needNext) drawImages.add(getRandomDrawImage());
		repaint();
	}
}
