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
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Zeichnet Heatmaps um verschiedene rechteckige Bereiche.
 * @author Alexander Herzog
 */
public class HeatMapImage {
	/**
	 * Standardwert für die Deckkraft der Heatmap-Farbe bei einer Intensität von 0%
	 */
	public static final double DEFAULT_INTENSITY_MIN=0;

	/**
	 * Standardwert für die Deckkraft der Heatmap-Farbe bei einer Intensität von 100%
	 */
	public static final double DEFAULT_INTENSITY_MAX=0.75;

	/**
	 * Potenz mit der der Alpha-Wert mit dem Abstand vom Kernbereich abnimmt
	 * @see #deltaToIntensity(double, double)
	 */
	private static final double DECREASE_FACTOR=0.5;

	/**
	 * Standardfarbe für "niedrigere Intensität"
	 * @see #box(int, int, int, int, double)
	 */
	public static final Color DEFAULT_COLOR_LOW_INTENSITY=Color.RED;

	/**
	 * Standardfarbe für "hohe Intensität"
	 * @see #box(int, int, int, int, double)
	 */
	public static final Color DEFAULT_COLOR_HIGH_INTENSITY=Color.BLUE;

	/**
	 * Breite des Bildes
	 */
	private int imageWidth;

	/**
	 * Höhe des Bilder
	 */
	private int imageHeight;

	/**
	 * Zoomfaktor für die Größe der Heatmaps
	 */
	private double zoom;

	/**
	 * Maximale Größe für den Heatmap-Bereich in Pixeln
	 * (bei einem Zoomfaktor von 100%)
	 */
	private int heatMapSize;

	/**
	 * Deckkraft der Heatmap-Farbe bei einer Intensität von 0%
	 */
	private double intensityMin;

	/**
	 * Deckkraft der Heatmap-Farbe bei einer Intensität von 100%
	 */
	private double intensityMax;


	/**
	 * Internes Bild in das die Heatmap gezeichnet werden soll
	 */
	private BufferedImage image;

	/**
	 * Wurden seit dem letzten Reset Pixel in der Grafik verändert?<br>
	 * Also muss die Grafik überhaupt in das Ausgabeobjekt gezeichnet werden?
	 */
	private boolean heatMapHasData;

	/**
	 * Konstruktor der Klasse
	 */
	public HeatMapImage() {
	}

	/**
	 * Farbwert für "transparent" zum Löschen der Grafik
	 * @see #reset(int, int, int, double, double, double)
	 */
	private static final Color TRANSPARENT_BLACK=new Color(0,0,0,0);

	/**
	 * Stellt Größe und Zoomfaktor des Grafikbereichs ein.
	 * @param width	Breite des Grafikbereichs
	 * @param height	Höhe des Grafikbereichs
	 * @param heatMapSize	Maximale Größe für den Heatmap-Bereich in Pixeln (bei einem Zoomfaktor von 100%)
	 * @param intensityMin	Deckkraft der Heatmap-Farbe bei einer Intensität von 0%
	 * @param intensityMax	Deckkraft der Heatmap-Farbe bei einer Intensität von 100%
	 * @param zoom	Zoomfaktor (wird nur zur Skalierung der Heatmap-Größe verwendet, die Pixelwerte für die Positionen ändern sich nicht)
	 */
	public void reset(final int width, final int height, final int heatMapSize, final double intensityMin, final double intensityMax, final double zoom) {
		this.imageWidth=width;
		this.imageHeight=height;
		this.heatMapSize=heatMapSize;
		this.intensityMin=intensityMin;
		this.intensityMax=intensityMax;
		this.zoom=zoom;

		if (imageWidth<=0 || imageHeight<=0 || heatMapSize<=0 || zoom<=0 || intensityMin<0 || intensityMax<0) {
			image=null;
			return;
		}

		if (image==null || image.getWidth()!=imageWidth || image.getHeight()!=imageHeight) {
			/* Wenn nötig neues Bild anlegen */
			image=new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_ARGB);
			return;
		}

		/* Altes Bild löschen */
		final Graphics2D canvas=(Graphics2D)image.getGraphics();
		canvas.setBackground(TRANSPARENT_BLACK);
		canvas.clearRect(0,0,imageWidth,imageHeight);

		/* Noch keine Heatmap-Daten in neuem Bild */
		heatMapHasData=false;
	}

	/**
	 * Berechnet die Intensität für ein Pixel, welches einen bestimmten Abstand von dem rechteckigen Kerngebiet besitzt.
	 * @param delta	Abstand in Pixeln
	 * @param zoom	Zoomfaktor zur Skalierung des Pixelwertes
	 * @return	Intensität
	 * @see #calcIntensity(int, int, int, int, int, int)
	 */
	private double deltaToIntensity(final double delta, final double zoom) {
		return Math.max(0,1-Math.pow(delta/(zoom*heatMapSize),DECREASE_FACTOR));
	}

	/**
	 * Berechnet die Intensität eines Pixels in Abhängigkeit zum Abstand von der Position eines rechteckigen Bereichs.
	 * @param x	x-Koordinate der linken oberen Ecke
	 * @param y	y-Koordinate der linken oberen Ecke
	 * @param width	Breite des Bereichs
	 * @param height	Höhe des Bereichs
	 * @param i	x-Koordinate des Pixels
	 * @param j	y-Koordinate des Pixels
	 * @return	Intensität
	 */
	private double calcIntensity(final int x, final int y, final int width, final int height, final int i, final int j) {
		/* Punkt im Inneren des Bereichs? */
		if (i>=x && i<=x+width && j>=y && j<=y+height) return deltaToIntensity(1,zoom); /* eigentlich 1.0, das wirkt aber zu intensiv */

		/* Über oder unter dem Bereich */
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
	 * @return	Kombinierte Farbe (Farbe 1 über Farbe 2)
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
	 * Cache für das Array, welche die RGB-Werte des Bildes aufnimmt
	 * @see #box(int, int, int, int, int, double)
	 */
	private int[] rgbArray;

	/**
	 * Zeichnet eine Heatmap um einen rechteckigen Bereich in das interne Grafikobjekt
	 * @param x	x-Koordinate der linken oberen Ecke
	 * @param y	y-Koordinate der linken oberen Ecke
	 * @param width	Breite des Bereichs
	 * @param height	Höhe des Bereichs
	 * @param rgb	Farbwert für das Heatmap-Objekt (die Transparenz wird ignoriert, da diese intern neu berechnet wird)
	 * @param intensity	Intensität der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschließlich)
	 */
	public void box(final int x, final int y, final int width, final int height, final int rgb, double intensity) {
		if (image==null) return;

		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		intensity=intensityMin+(intensityMax-intensityMin)*intensity;

		final int heat=(int)Math.round(zoom*heatMapSize);
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

			final int newColor=(((int)(alpha*255) & 0xFF) << 24) | (rgb & 0xFFFFFF);

			heatMapHasData=true;

			if (newColor==oldColor) {
				/* Nur Deckkraft verstärken */
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
	 * @param height	Höhe des Bereichs
	 * @param color	Farbwert für das Heatmap-Objekt (die Transparenz wird ignoriert, da diese intern neu berechnet wird)
	 * @param intensity	Intensität der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschließlich)
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
	 * @param height	Höhe des Bereichs
	 * @param intensity	Intensität der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschließlich)
	 */
	public void box(final int x, final int y, final int width, final int height, double intensity) {
		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		box(x,y,width,height,mixColors(DEFAULT_COLOR_LOW_INTENSITY.getRGB(),DEFAULT_COLOR_HIGH_INTENSITY.getRGB(),intensity),intensity);
	}

	/**
	 * Berechnet den Abstand eines Punktes von einer Linie.
	 * @param point	Punkt
	 * @param lineA	Startpunkt der Linie
	 * @param lineB	Endpunkt der Linie
	 * @return	Abstand
	 */
	private static double getDelta(final Point point, final Point lineA, final Point lineB) {
		final int footpointX;
		final int footpointY;

		if (lineA.x==lineB.x) {
			/* Linie verläuft senkrecht */
			final int minY=Math.min(lineA.y,lineB.y);
			final int maxY=Math.max(lineA.y,lineB.y);
			footpointX=lineA.x;
			if (point.y<minY) footpointY=minY; /* Unterhalb der senkrechten Linie */
			else if (point.y>maxY) footpointY=maxY; /* Oberhalb der senkrechten Linie */
			else footpointY=point.y; /* Punkt links oder rechts neben der Linie */
		} else if (lineA.y==lineB.y) {
			/* Linie verläuft waagerecht */
			final int minX=Math.min(lineA.x,lineB.x);
			final int maxX=Math.max(lineA.x,lineB.x);
			footpointY=lineA.y;
			if (point.x<minX) footpointX=minX; /* Links der waagerechten Linie */
			else if (point.x>maxX) footpointX=maxX; /* Rechts der waagerechten Linie */
			else footpointX=point.x; /* Punkt über oder unter der Linie */

		} else {
			/* Linie verläuft schräg */
			final double m=((double)(lineB.y-lineA.y))/(lineB.x-lineA.x);
			footpointX=(int)Math.round((lineA.x*m-lineA.y+point.x/m+point.y)/(m+1/m));
			final int minX=Math.min(lineA.x,lineB.x);
			final int maxX=Math.max(lineA.x,lineB.x);
			if (footpointX<minX) footpointY=(lineA.x<lineB.x)?lineA.y:lineB.y; /* Links neben der Linie */
			else if (footpointX>maxX) footpointY=(lineA.x>lineB.x)?lineA.y:lineB.y; /* Rechts neben der Linie */
			else footpointY=(int)Math.round(m*(footpointX-lineA.x)+lineA.y); /* y-Wert zu Lotfußpunkt-x-Wert über Geradengleichung */
		}

		final int deltaX=(point.x-footpointX);
		final int deltaY=(point.y-footpointY);
		return Math.sqrt(deltaX*deltaX+deltaY*deltaY);
	}

	/**
	 * Berechnet den minimalen Abstand eines Punktes von einem Linienzug.
	 * @param point	Punkt
	 * @param points	Punkte des Linienzugs
	 * @return	Minimaler Abstand des Punktes von einer Teillinie des Linienzugs
	 */
	private static double getDelta(final Point point, final Point[] points) {
		double minDelta=getDelta(point,points[0],points[1]);
		for (int i=1;i<points.length-1;i++) minDelta=Math.min(minDelta,getDelta(point,points[i],points[i+1]));
		return minDelta;
	}

	/**
	 * Zeichnet eine Heatmap um einen Linienzug in das interne Grafikobjekt
	 * @param points	Punkte des Linienzugs
	 * @param rgb	Farbwert für das Heatmap-Objekt (die Transparenz wird ignoriert, da diese intern neu berechnet wird)
	 * @param intensity	Intensität der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschließlich)
	 */
	public void polyline(final Point[] points, final int rgb, double intensity) {
		if (image==null) return;
		if (points==null || points.length<2) return;

		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		intensity=intensityMin+(intensityMax-intensityMin)*intensity;

		final int heat=(int)Math.round(zoom*heatMapSize);
		int minX=points[0].x;
		int maxX=points[0].x;
		int minY=points[0].y;
		int maxY=points[0].y;
		for (int i=1;i<points.length;i++) {
			minX=Math.min(minX,points[i].x);
			maxX=Math.max(maxX,points[i].x);
			minY=Math.min(minY,points[i].y);
			maxY=Math.max(maxY,points[i].y);
		}
		minX=Math.max(0,minX-heat);
		minY=Math.max(0,minY-heat);
		maxX=Math.min(imageWidth-1,maxX+heat);
		maxY=Math.min(imageHeight-1,maxY+heat);

		final int areaW=maxX-minX+1;
		final int areaH=maxY-minY+1;
		if (rgbArray==null || rgbArray.length<areaW*areaH) rgbArray=new int[areaW*areaH];

		image.getRGB(minX,minY,maxX-minX+1,maxY-minY+1,rgbArray,0,areaW);

		final Point p=new Point();
		for (int i=minX;i<=maxX;i++) for (int j=minY;j<=maxY;j++) {
			p.x=i;
			p.y=j;
			final double alpha=intensity*deltaToIntensity(getDelta(p,points),zoom);
			if (alpha<=0) continue;

			final int rgbIndex=(j-minY)*areaW+(i-minX);
			final int oldColor=rgbArray[rgbIndex];

			final int newColor=(((int)(alpha*255) & 0xFF) << 24) | (rgb & 0xFFFFFF);

			heatMapHasData=true;

			if (newColor==oldColor) {
				/* Nur Deckkraft verstärken */
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
	 * Zeichnet eine Heatmap um einen Linienzug in das interne Grafikobjekt
	 * @param points	Punkte des Linienzugs
	 * @param color	Farbwert für das Heatmap-Objekt (die Transparenz wird ignoriert, da diese intern neu berechnet wird)
	 * @param intensity	Intensität der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschließlich)
	 */
	public void polyline(final Point[] points, final Color color, double intensity) {
		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		polyline(points,color.getRGB(),intensity);
	}

	/**
	 * Zeichnet eine Heatmap um einen Linienzug in das interne Grafikobjekt
	 * @param points	Punkte des Linienzugs
	 * @param intensity	Intensität der Heatmap (Wert muss zwischen 0 und 1 liegen, jeweils einschließlich)
	 */
	public void polyline(final Point[] points, double intensity) {
		if (intensity<=0) return;
		if (intensity>1) intensity=1;

		polyline(points,mixColors(DEFAULT_COLOR_LOW_INTENSITY.getRGB(),DEFAULT_COLOR_HIGH_INTENSITY.getRGB(),intensity),intensity);
	}

	/**
	 * Überträgt die erstellte Heatmap in ein Grafikobjekt
	 * @param graphics	Grafikobjekt in das die Heatmap eingezeichnet werden soll
	 * @param x	x-Koordinate an der die Ausgabe starten soll
	 * @param y	y-Koordinate an der die Ausgabe starten soll
	 */
	public void draw(final Graphics graphics, final int x, final int y) {
		if (image!=null && heatMapHasData) graphics.drawImage(image,x,y,null);
	}

	/**
	 * Mischt zwei Farbwerte (ohne Berücksichtigung der Deckkraft).
	 * @param c1	Farbe 1
	 * @param c2	Farbe 2
	 * @param intensity	Wert zwischen 0 und 1 (0=nur Farbe 1, 1=nur Farbe 2)
	 * @return	Mischfarbe (mit 100% Deckkraft)
	 * @see #box(int, int, int, int, int, double)
	 */
	public static int mixColors(final Color c1, final Color c2, final double intensity) {
		return mixColors(c1.getRGB(),c2.getRGB(),intensity);
	}

	/**
	 * Mischt zwei Farbwerte (ohne Berücksichtigung der Deckkraft).
	 * @param c1	Farbe 1
	 * @param c2	Farbe 2
	 * @param intensity	Wert zwischen 0 und 1 (0=nur Farbe 1, 1=nur Farbe 2)
	 * @return	Mischfarbe (mit 100% Deckkraft)
	 * @see #box(int, int, int, int, int, double)
	 */
	public static int mixColors(final int c1, final int c2, double intensity) {
		if (intensity<0) return c1;
		if (intensity>1) return c2;

		final int c1r=((c1 & 0xFF0000) >> 16) & 0xFF;
		final int c1g=((c1 & 0xFF00) >> 8) & 0xFF;
		final int c1b=c1 & 0xFF;

		final int c2r=((c2 & 0xFF0000) >> 16) & 0xFF;
		final int c2g=((c2 & 0xFF00) >> 8) & 0xFF;
		final int c2b=c2 & 0xFF;

		final int r=Math.min(255,(int)Math.round(c2r*intensity+c1r*(1-intensity)));
		final int g=Math.min(255,(int)Math.round(c2g*intensity+c1g*(1-intensity)));
		final int b=Math.min(255,(int)Math.round(c2b*intensity+c1b*(1-intensity)));

		return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8)  | ((b & 0xFF) << 0);
	}
}
