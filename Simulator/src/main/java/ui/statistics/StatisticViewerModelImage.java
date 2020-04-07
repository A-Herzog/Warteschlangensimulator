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
import systemtools.statistics.StatisticViewerImage;
import ui.modeleditor.ModelSurfacePanel;

/**
 * Dieser Viewer stellt das simulierte Modell als Bild dar.
 * @author Alexander Herzog
 * @see StatisticViewerImage
 * @see ModelSurfacePanel#getImage(int, int)
 */
public class StatisticViewerModelImage extends StatisticViewerImage {
	private final Statistics statistics;
	private ModelSurfacePanel surfacePanel;
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

	private void buildImage(final int width, final int height) {
		if (surfacePanel==null) panelNeeded();
		image=surfacePanel.getImage(width,height);
	}

	@Override
	protected void paintImage(Graphics g) {
		final int w=g.getClipBounds().width;
		final int h=g.getClipBounds().height;
		g.setColor(Color.WHITE);
		g.fillRect(0,0,w,h);
		if (image==null || w!=image.getWidth() || h!=image.getHeight()) buildImage(w,h);
		g.drawImage(image,0,0,null);
	}
}
