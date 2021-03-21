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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Zeichnet Heatmaps um verschiedene rechteckige Bereiche.
 * @author Alexander Herzog
 */
public class HeatMapImage {
	/**
	 * Maximale Gr��e f�r den Heatmap-Bereich in Pixeln
	 * (bei einem Zoomfaktor von 100%)
	 */
	public static final double HEAT_SCALE=50;

	/**
	 * Deckkraft der Heatmap-Farbe bei einer Intensit�t von 100%
	 */
	private static final double MAX_ALPHA=200;

	/**
	 * Potenz mit der der Alpha-Wert mit dem Abstand vom Kernbereich abnimmt
	 * @see #deltaToIntensity(double, double)
	 */
	private static final double DECREASE_FACTOR=0.5;

	/**
	 * Breite des Bildes
	 */
	private int imageWidth;

	/**
	 * H�he des Bilder
	 */
	private int imageHeight;

	/**
	 * Zoomfaktor f�r die Gr��e der Heatmaps
	 */
	private double zoom;

	/**
	 * Internes Bild in das die Heatmap gezeichnet werden soll
	 */
	private BufferedImage image;

	/**
	 * Wurden seit dem letzten Reset Pixel in der Grafik ver�ndert?<br>
	 * Also muss die Grafik �berhaupt in das Ausgabeobjekt gezeichnet werden?
	 */
	private boolean heatMapHasData;

	/**
	 * Konstruktor der Klasse
	 */
	public HeatMapImage() {
	}

	/**
	 * Farbwert f�r "transparent" zum L�schen der Grafik
	 * @see #reset(int, int, double)
	 */
	private static final Color TRANSPARENT_BLACK=new Color(0,0,0,0);

	/**
	 * Stellt Gr��e und Zoomfaktor des Grafikbereichs ein.
	 * @param width	Breite des Grafikbereichs
	 * @param height	H�he des Grafikbereichs
	 * @param zoom	Zoomfaktor (wird nur zur Skalierung der Heatmap-Gr��e verwendet, die Pixelwerte f�r die Positionen �ndern sich nicht)
	 */
	public void reset(final int width, final int height, final double zoom) {
		this.imageWidth=width;
		this.imageHeight=height;
		this.zoom=zoom;

		if (imageWidth<=0 || imageHeight<=0 || zoom<=0) {
			image=null;
			return;
		}

		if (image==null || image.getWidth()!=imageWidth || image.getHeight()!=imageHeight) {
			/* Wenn n�tig neues Bild anlegen */
			image=new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_ARGB);
			return;
		}

		/* Altes Bild l�schen */
		final Graphics2D canvas=(Graphics2D)image.getGraphics();
		canvas.setBackground(TRANSPARENT_BLACK);
		canvas.clearRect(0,0,imageWidth,imageHeight);

		/* Noch keine Heatmap-Daten in neuem Bild */
		heatMapHasData=false;
	}

	/**
	 * Berechnet die Intensit�t f�r ein Pixel, welches einen bestimmten Abstand von dem rechteckigen Kerngebiet besitzt.
	 * @param delta	Abstand in Pixeln
	 * @param zoom	Zoomfaktor zur Skalierung des Pixelwertes
	 * @return	Intensit�t
	 * @see #calcIntensity(int, int, int, int, int, int)
	 */
	private double deltaToIntensity(final double delta, final double zoom) {
		return Math.max(0,1-Math.pow(delta/(zoom*HEAT_SCALE),DECREASE_FACTOR));
	}

	/**
	 * Berechnet die Intensit�t eines Pixels in Abh�ngigkeit zum Abstand von der Position eines rechteckigen Bereichs.
	 * @param x	x-Koordinate der linken oberen Ecke
	 * @param y	y-Koordinate der linken oberen Ecke
	 * @param width	Breite des Bereichs
	 * @param height	H�he des Bereichs
	 * @param i	x-Koordinate des Pixels
	 * @param j	y-Koordinate des Pixels
	 * @return	Intensit�t
	 */
	private double calcIntensity(final int x, final int y, final int width, final int height, final int i, final int j) {
		/* Punkt im Inneren des Bereichs? */
		if (i>=x && i<=x+width && j>=y && j<=y+height) return deltaToIntensity(1,zoom); /* eigentlich 1.0, das wirkt aber zu intensiv */

		/* �ber oder unter dem Bereich */
		if (i>=x && i<=x+width) {
			if (j<y) return deltaToIntensity(y-j,zoom);
			if (j>y+height) return deltaToIntensity(j-y-height,zoom);
		}

		/* Links oder rechts vom Bereich */
		if (j>=y && j<=y+height) {
			if (i<x) return deltaToIntensity(x-i,zoom);
			if (i>x+width) return deltaToIntensity(i-x-width,zoom);
		}

		/* Ecken */
		if (i<x && j<y) return deltaToIntensity(Math.sqrt((i-x)*(i-x)+(j-y)*(j-y)),zoom);
		if (i<x && j>y+height) return deltaToIntensity(Math.sqrt((i-x)*(i-x)+(j-(y+height))*(j-(y+height))),zoom);
		if (i>x+width && j<y) return deltaToIntensity(Math.sqrt((i-(x+width))*(i-(x+width))+(j-y)*(j-y)),zoom);
		if (i>x+width && j>y+height) return deltaToIntensity(Math.sqrt((i-(x+width))*(i-(x+width))+(j-(y+height))*(j-(y+height))),zoom);

		return 0.0;
	}

	/**
	 * Mischt zwei Farbwerte (inkl. Alpha-Kanal)
	 * @param c1	Farbe 1
	 * @param c2	Farbe 2
	 * @return	Kombinierte Farbe (Farbe 1 �ber Farbe 2)
	 */
	private int mixColors(final int c1, final int c2) {
		final int c1a=(c1 >> 24) & 0xFF;
		final int c1r=((c1 & 0xFF0000) >> 16) & 0xFF;
		final int c1g=((c1 & 0xFF00) >> 8) & 0xFF;
		final int c1b=c1 & 0xFF;
		final double alpha1=c1a/255.0;

		final int c2a=(c2 >> 24) & 0xFF;
		final int c2r=((c2 & 0xFF0000) >> 16) & 0xFF;
		final int c2g=((c2 & 0xFF00) >> 8) & 0xFF;
		final int c2b=c2 & 0xFF;
		final double alpha2=c2a/255.0;

		final double alpha=alpha1+(1-alpha1)*alpha2;
		int a=(int)(alpha*255);
		int r=(int)(1/alpha*(alpha1*c1r+(1-alpha1)*alpha2*c2r));
		int g=(int)(1/alpha*(alpha1*c1g+(1-alpha1)*alpha2*c2g));
		int b=(int)(1/alpha*(alpha1*c1b+(1-alpha1)*alpha2*c2b));

		return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)  | ((b & 0xFF) << 0);
	}

	/**
	 * Berechnet den Mittelwert zwischen zwei Farben
	 * @param c1	Farbe 1
	 * @param c2	Farbe 2
	 * @return	Mittlerer Farbwert
	 */
	private int meanColor(final int c1, final int c2) {
		if (c1==c2) return c1;

		final int c1a=(c1 >> 24) & 0xFF;
		final int c1r=((c1 & 0xFF0000) >> 16) & 0xFF;
		final int c1g=((c1 & 0xFF00) >> 8) & 0xFF;
		final int c1b=c1 & 0xFF;

		final int c2a=(c2 >> 24) & 0xFF;
		final int c2r=((c2 & 0xFF0000) >> 16) & 0xFF;
		final int c2g=((c2 & 0xFF00) >> 8) & 0xFF;
		final int c2b=c2 & 0xFF;

		int a=(c1a+c2a)/2;
		int r=(c1r+c2r)/2;
		int g=(c1g+c2g)/2;
		int b=(c1b+c2b)/2;

		return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)  | ((b & 0xFF) << 0);
	}

	/**
	 * Cache f�r das Array, welche die RGB-Werte des Bildes aufnimmt
	 * @see #box(int, int, int, int, int, double)
	 */
	private int[] rgbArray;

	/**
	 * Zeichnet eine Heatmap um einen rechteckigen Bereich in das interne Grafikobjekt
	 * @param x	x-Koordinate der linken oberen Ecke
	 * @param y	y-Koordinate der linken oberen Ecke
	 * @param width	Breite des Bereichs
	 * @param height	H�he des Bereichs
	 * @param rgb	Farbwert f�r das Heatmap-Objekt (die Transparenz wird ignoriert, da diese intern neu berechnet wird)
	 * @param intensity	Intensit�t der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschlie�lich)
	 */
	public void box(final int x, final int y, final int width, final int height, final int rgb, double intensity) {
		if (image==null) return;

		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		final int heat=(int)Math.round(zoom*HEAT_SCALE);
		final int minX=Math.max(0,x-heat);
		final int minY=Math.max(0,y-heat);
		final int maxX=Math.min(imageWidth-1,x+width+heat);
		final int maxY=Math.min(imageHeight-1,y+height+heat);

		final int areaW=maxX-minX+1;
		final int areaH=maxY-minY+1;
		if (rgbArray==null || rgbArray.length<areaW*areaH) rgbArray=new int[areaW*areaH];

		image.getRGB(minX,minY,maxX-minX+1,maxY-minY+1,rgbArray,0,areaW);

		for (int i=minX;i<=maxX;i++) for (int j=minY;j<=maxY;j++) {
			final double alpha=intensity*calcIntensity(x,y,width,height,i,j);
			if (alpha<=0) continue;

			final int rgbIndex=(j-minY)*areaW+(i-minX);
			final int oldColor=rgbArray[rgbIndex];
			final int newColor=(((int)(alpha*MAX_ALPHA) & 0xFF) << 24) | (rgb & 0xFFFFFF);

			heatMapHasData=true;

			if (newColor==oldColor) {
				/* Nur Deckkraft verst�rken */
				final int c=mixColors(oldColor,newColor);
				rgbArray[rgbIndex]=c;
			} else {
				final int c1=mixColors(oldColor,newColor);
				final int c2=mixColors(newColor,oldColor);
				rgbArray[rgbIndex]=meanColor(c1,c2);
			}
		}

		image.setRGB(minX,minY,maxX-minX+1,maxY-minY+1,rgbArray,0,areaW);
	}

	/**
	 * Zeichnet eine Heatmap um einen rechteckigen Bereich in das interne Grafikobjekt
	 * @param x	x-Koordinate der linken oberen Ecke
	 * @param y	y-Koordinate der linken oberen Ecke
	 * @param width	Breite des Bereichs
	 * @param height	H�he des Bereichs
	 * @param color	Farbwert f�r das Heatmap-Objekt (die Transparenz wird ignoriert, da diese intern neu berechnet wird)
	 * @param intensity	Intensit�t der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschlie�lich)
	 */
	public void box(final int x, final int y, final int width, final int height, final Color color, double intensity) {
		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		box(x,y,width,height,color.getRGB(),intensity);
	}

	/**
	 * Zeichnet eine Heatmap um einen rechteckigen Bereich in das interne Grafikobjekt
	 * @param x	x-Koordinate der linken oberen Ecke
	 * @param y	y-Koordinate der linken oberen Ecke
	 * @param width	Breite des Bereichs
	 * @param height	H�he des Bereichs
	 * @param intensity	Intensit�t der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschlie�lich)
	 */
	public void box(final int x, final int y, final int width, final int height, double intensity) {
		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		final int r=(int)Math.round(255*(1-intensity));
		final int b=(int)Math.round(255*intensity);
		final int rgb=(r << 16) | b;

		box(x,y,width,height,rgb,intensity);
	}

	/**
	 * �bertr�gt die erstellte Heatmap in ein Grafikobjekt
	 * @param graphics	Grafikobjekt in das die Heatmap eingezeichnet werden soll
	 * @param x	x-Koordinate an der die Ausgabe starten soll
	 * @param y	y-Koordinate an der die Ausgabe starten soll
	 */
	public void draw(final Graphics graphics, final int x, final int y) {
		if (image!=null && heatMapHasData) graphics.drawImage(image,x,y,null);
	}
}
