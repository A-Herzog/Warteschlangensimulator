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
package ui.statistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import simulator.statistics.Statistics;
import systemtools.GUITools;
import systemtools.statistics.StatisticViewerImage;
import ui.modeleditor.ModelSurfacePanel;

/**
 * Dieser Viewer stellt das simulierte Modell als Bild dar.
 * @author Alexander Herzog
 * @see StatisticViewerImage
 * @see ModelSurfacePanel#getImage(int, int)
 */
public class StatisticViewerModelImage extends StatisticViewerImage {
	/** Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen */
	private final Statistics statistics;
	/** Zeichenfläche (zur Bildgenerierung) */
	private ModelSurfacePanel surfacePanel;
	/** Erzeugtes Bild ({@link #buildImage(int, int)}) */
	private BufferedImage image;

	/**
	 * Konstruktor der Klasse
	 * @param statistics	Statistikobjekt, aus dem die anzuzeigenden Daten entnommen werden sollen
	 */
	public StatisticViewerModelImage(final Statistics statistics) {
		super();
		this.statistics=statistics;
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_PICTURE;
	}

	@Override
	protected void panelNeeded() {
		if (surfacePanel==null) {
			surfacePanel=new ModelSurfacePanel();
			surfacePanel.setSurface(statistics.editModel,statistics.editModel.surface,statistics.editModel.clientData,statistics.editModel.sequences);
		}
	}

	/**
	 * Erstellt das Bild des Modells und speichert
	 * es in {@link #image}.
	 * @param width	Breite des Bildes
	 * @param height	Höhe des Bildes
	 * @see #image
	 * @see ModelSurfacePanel#getImage(int, int)
	 */
	private void buildImage(final int width, final int height) {
		if (surfacePanel==null) panelNeeded();
		final double scaleFactor=GUITools.getOSScaleFactor();
		image=surfacePanel.getImage((int)Math.round(width*scaleFactor),(int)Math.round(height*scaleFactor));
	}

	@Override
	protected BufferedImage getImage(final int maxX, final int maxY) {
		if (surfacePanel==null) panelNeeded();
		return surfacePanel.getImage(maxX,maxY);
	}

	@Override
	protected void paintImage(Graphics g) {
		final int w=g.getClipBounds().width;
		final int h=g.getClipBounds().height;
		g.setColor(Color.WHITE);
		g.fillRect(0,0,w,h);
		if (image==null || w!=image.getWidth() || h!=image.getHeight()) buildImage(w,h);

		final double ratio=((double)image.getWidth())/image.getHeight();
		final int drawH=(int)Math.floor(w/ratio);
		if (drawH<h) {
			g.drawImage(image,0,0,w,drawH,null);
		} else {
			final int drawW=(int)Math.floor(h*ratio);
			g.drawImage(image,0,0,drawW,h,null);
		}
	}
}
