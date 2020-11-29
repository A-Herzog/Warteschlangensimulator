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
package ui.modeleditor.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

/**
 * Diese Klasse ermöglicht die Darstellung einer Linie
 * in verschiedenen Breiten, Farben und Darstellungsarten.
 * @author Alexander Herzog
 * @see ModelElementEdge
 * @see ModelElementLine
 */
public final class ComplexLine implements Cloneable {
	/**
	 * Bezeichner für die Namen der Linientypen
	 */
	public static String[] LINE_TYPE_NAMES=new String[]{ /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */
			"durchgezogen",
			"gestrichelt (kurz)",
			"gestrichelt (mittel)",
			"gestrichelt (lang)",
			"gepunktet",
			"gepunktet (lang)",
			"strichpunktiert",
			"Straße 1",
			"Straße 2",
			"Bahnschienen",
			"Warteschlange"
	};

	/**
	 * Anzahl an verschiedenen verfügbaren Linientypen
	 */
	public static final int MAX_TYPE=LINE_TYPE_NAMES.length-1;

	/**
	 * Linienbreite
	 * @see #getWidth()
	 * @see #setWidth(int)
	 */
	private int width;

	/**
	 * Linienfarbe
	 * @see #getColor()
	 * @see #setColor(Color)
	 */
	private Color color;

	/**
	 * Linientyp
	 * @see #getType()
	 * @see #setType(int)
	 * @see #LINE_TYPE_NAMES
	 */
	private int type;

	/**
	 * Konstruktor der Klasse
	 * @param width	Linienbreite
	 * @param color	Linienfarbe
	 * @param type	Linientyp (siehe {@link #LINE_TYPE_NAMES})
	 */
	public ComplexLine(final int width, final Color color, final int type) {
		setWidth(width);
		if (color==null) setColor(Color.BLACK); else setColor(color);
		setType(type);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Erzeugt eine 1 Pixel breite, schwarze, durchgezogene Linie
	 */
	public ComplexLine() {
		this(1,null,0);
	}

	/**
	 * Berechnet den Zeichenbereich einer zu einem Rechteck vergrößerten Linie
	 * @param p1	Startpunkt der Linie
	 * @param p2	Endpunkt der Linie
	 * @param zoom	Zoomfaktor
	 * @return	Eckpunkte des Rechtecks
	 */
	private Point2D.Double[] calcArea(final Point p1, final Point p2, final double zoom) {
		final double lenA=FastMath.sqrt((p2.x-p1.x)*(p2.x-p1.x)+(p2.y-p1.y)*(p2.y-p1.y));
		final double lenB=width*zoom;
		final Point2D.Double dirA=new Point2D.Double((p2.x-p1.x)/lenA,(p2.y-p1.y)/lenA);
		final Point2D.Double dirB=new Point2D.Double(dirA.y,-dirA.x);

		double x=p1.x;
		double y=p1.y;

		List<Point2D.Double> list=new ArrayList<>();

		x+=dirB.x*lenB/2;
		y+=dirB.y*lenB/2;
		list.add(new Point2D.Double(x,y));

		x+=dirA.x*lenA;
		y+=dirA.y*lenA;
		list.add(new Point2D.Double(x,y));

		x+=-dirB.x*lenB;
		y+=-dirB.y*lenB;
		list.add(new Point2D.Double(x,y));

		x+=-dirA.x*lenA;
		y+=-dirA.y*lenA;
		list.add(new Point2D.Double(x,y));

		return list.toArray(new Point2D.Double[0]);
	}

	/**
	 * Erstellt ein Polygon basierend auf einer Reihe von Punkten
	 * @param points	Punkte aus denen das Polygon geformt werden soll
	 * @return	Polygon
	 */
	private Polygon polygonFromPoints(final Point2D.Double[] points) {
		final Polygon polygon=new Polygon();
		for (Point2D.Double point: points) polygon.addPoint((int)FastMath.round(point.x),(int)FastMath.round(point.y));
		return polygon;
	}

	/**
	 * Letzter Linienstil
	 * @see #setStroke(Graphics2D, double)
	 */
	private BasicStroke cacheStroke;

	/**
	 * Linienbreite für letzten Linienstil
	 * @see #cacheStroke
	 * @see #setStroke(Graphics2D, double)
	 */
	private float cacheStrokeWidth;

	/**
	 * Linientyp für letzten Linienstil
	 * @see #cacheStroke
	 * @see #setStroke(Graphics2D, double)
	 */
	private int cacheStrokeType;

	/**
	 * Stellt den Linienstil ein
	 * @param g2	Grafikausgabeobjekt
	 * @param zoom	Zoomfaktor
	 */
	private void setStroke(final Graphics2D g2, final double zoom) {
		float strokeWide=width*(float)zoom;
		if (cacheStroke==null || Math.abs(cacheStrokeWidth-strokeWide)>0.0001 || cacheStrokeType!=type) {

			float[] dash=null;
			switch (type) {
			case 0: dash=null; break; /* durchgezogen */
			case 1: dash=new float[]{3*width*(float)zoom}; break; /* gestrichelt (kurz) */
			case 2: dash=new float[]{5*width*(float)zoom}; break; /* gestrichelt (mittel) */
			case 3: dash=new float[]{7*width*(float)zoom}; break; /* gestrichelt (lang) */
			case 4: dash=new float[]{width*(float)zoom}; break; /* gepunktet */
			case 5: dash=new float[]{width*(float)zoom,2*width*(float)zoom}; break; /* gepunktet (lang) */
			case 6: dash=new float[]{5*width*(float)zoom,3*width*(float)zoom,width*(float)zoom,3*width*(float)zoom}; break; /* strichpunktiert */
			case 7: dash=null; break; /* Straße 1 */
			case 8: dash=null; break; /* Straße 2 */
			case 9: dash=null; break; /* Bahnschienen */
			case 10: dash=null; break; /* Warteschlange */
			}

			cacheStroke=new BasicStroke(strokeWide,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash,0.0f);
			cacheStrokeWidth=strokeWide;
			cacheStrokeType=type;
		}

		g2.setStroke(cacheStroke);
	}

	/**
	 * Speichert das Linienobjekt für weitere
	 * Aufrufe von {@link #getLine(Point, Point)}
	 * zwischen. (Es wird nur das Objekt im
	 * Speicher aufgehoben, die Inhalte sind nicht
	 * von Bedeutung.)
	 * @see #getLine(Point, Point)
	 */
	private final Line2D.Double cacheLine=new Line2D.Double(0,0,0,0);

	/**
	 * Erstellt eine Linie zwischen zwei Punkten
	 * @param p1	Startpunkt der Linie
	 * @param p2	Endpunkt der Linie
	 * @return	Neue Linie
	 */
	private Shape getLine(final Point p1, final Point p2) {
		cacheLine.x1=p1.x;
		cacheLine.y1=p1.y;
		cacheLine.x2=p2.x;
		cacheLine.y2=p2.y;

		return cacheLine;
	}

	/**
	 * Zeichnet die Linie
	 * @param graphics	{@link Graphics}-Objekt in das die Linie eingezeichnet werden soll
	 * @param p1	Startpunkt der Linie
	 * @param p2	Zielpunkt der Linie
	 * @param zoom	Zoomfaktor (1.0==100%)
	 */
	public void draw(final Graphics graphics, final Point p1, final Point p2, final double zoom) {
		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		g2.setColor(color);
		setStroke(g2,zoom);

		switch (type) {
		case 0: /* durchgezogen */
		case 1: /* gestrichelt (kurz) */
		case 2: /* gestrichelt (mittel) */
		case 3: /* gestrichelt (lang) */
		case 4: /* gepunktet */
		case 5: /* gepunktet (lang) */
		case 6: /* strichpunktiert */
			g2.draw(getLine(p1,p2));
			break;
		case 7: /* Straße 1 */
			g2.draw(getLine(p1,p2));
			g2.setColor(new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue()));
			g2.setStroke(new BasicStroke(FastMath.max(1,width/10*(float)zoom),BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,new float[]{3*width*(float)zoom},0.0f));
			g2.draw(getLine(p1,p2));
			break;
		case 8: /* Straße 2 */
			g2.draw(getLine(p1,p2));
			g2.setColor(new Color(255-color.getRed(),255-color.getGreen(),255-color.getBlue()));
			g2.setStroke(new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,null,0.0f));
			g2.draw(getLine(p1,p2));
			break;
		case 9: /* Bahnschienen */
		case 10: /* Warteschlange */
			final Point2D.Double[] area=calcArea(p1,p2,zoom);
			final Polygon polygon=polygonFromPoints(area);

			final double p1x=area[0].x;
			final double p1y=area[0].y;
			final double p2x=area[3].x;
			final double p2y=area[3].y;
			final double dx=area[1].x-area[0].x;
			final double dy=area[1].y-area[0].y;

			final double len=FastMath.sqrt(dx*dx+dy*dy);

			if (type==9) {
				/* Schienen: Schienen */
				g2.setColor(color);
				g2.setStroke(new BasicStroke(3*(float)zoom));
				final Point2D.Double[] smallArea=calcArea(p1,p2,zoom*0.7);
				g2.drawLine((int)FastMath.round(smallArea[0].x),(int)FastMath.round(smallArea[0].y),(int)FastMath.round(smallArea[1].x),(int)FastMath.round(smallArea[1].y));
				g2.drawLine((int)FastMath.round(smallArea[3].x),(int)FastMath.round(smallArea[3].y),(int)FastMath.round(smallArea[2].x),(int)FastMath.round(smallArea[2].y));
				/* Schienen: Querbalken */
				g2.setColor(Color.BLACK);
				g2.setStroke(new BasicStroke(3*(float)zoom));
				for (double d=0;d<len;d+=width*(float)zoom) {
					g2.drawLine((int)FastMath.round(p1x+d/len*dx),(int)FastMath.round(p1y+d/len*dy),(int)FastMath.round(p2x+d/len*dx),(int)FastMath.round(p2y+d/len*dy));
				}
			}

			if (type==10) {
				/* Warteschlange: Boxfüllung */
				g2.setColor(Color.LIGHT_GRAY);
				g2.fill(polygon);
				/* Warteschlange: Box */
				g2.setStroke(new BasicStroke(1));
				g2.setColor(color);
				g2.draw(polygon);
				/* Warteschlange: Querlinien */
				for (double d=width*(float)zoom;d<len;d+=width*(float)zoom) {
					g2.drawLine((int)FastMath.round(p1x+d/len*dx),(int)FastMath.round(p1y+d/len*dy),(int)FastMath.round(p2x+d/len*dx),(int)FastMath.round(p2y+d/len*dy));
				}
			}
			break;
		}

		g2.setStroke(saveStroke);
	}

	/**
	 * Zeichnet einen Bogen mit den Einstellungen der Linie
	 * @param graphics	{@link Graphics}-Objekt in das der Bogen eingezeichnet werden soll
	 * @param p1	Startpunkt des Bogens
	 * @param p2	Zielpunkt des Bogens
	 * @param zoom	Zoomfaktor (1.0==100%)
	 */
	public void drawArc(final Graphics graphics, final Point p1, final Point p2, final double zoom) {
		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		g2.setColor(color);
		setStroke(g2,zoom);

		final int wHalf=Math.abs(p2.x-p1.x);
		final int hHalf=Math.abs(p2.y-p1.y);

		/* 1. Viertel */
		if (p2.y>p1.y && p2.x>p1.x) g2.drawArc(p1.x-wHalf,p1.y,2*wHalf,2*hHalf,0,90);
		/* 2. Viertel */
		if (p2.y>p1.y && p2.x<p1.x) g2.drawArc(p2.x-wHalf,p1.y-hHalf,2*wHalf,2*hHalf,270,90);
		/* 3. Viertel */
		if (p2.y<p1.y && p2.x<p1.x) g2.drawArc(p2.x,p2.y-hHalf,2*wHalf,2*hHalf,180,90);
		/* 4. Viertel */
		if (p2.y<p1.y && p2.x>p1.x) g2.drawArc(p1.x,p2.y,2*wHalf,2*hHalf,90,90);

		g2.setStroke(saveStroke);
	}

	/**
	 * Cache für das Bezierkurven-Objekt
	 * @see #drawBezier(Graphics, Point, double, double, double, double, Point, double)
	 */
	private CubicCurve2D.Double cubicCurve;

	/**
	 * Zeichner eine Bezierkurve
	 * @param graphics	{@link Graphics}-Objekt in das der Bogen eingezeichnet werden soll
	 * @param p1	Startpunkt der Kurve
	 * @param ctrlx1	x-Koordinate des Kontrollpunkts für den Startpunkt
	 * @param ctrly1	y-Koordinate des Kontrollpunkts für den Startpunkt
	 * @param ctrlx2	x-Koordinate des Kontrollpunkts für den Endpunkt
	 * @param ctrly2	y-Koordinate des Kontrollpunkts für den Endpunkt
	 * @param p2	Endpunkt der Kurve
	 * @param zoom	Zoomfaktor (1.0==100%)
	 */
	public void drawBezier(final Graphics graphics, final Point p1, final double ctrlx1, final double ctrly1, final double ctrlx2, final double ctrly2, final Point p2, final double zoom) {
		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		g2.setColor(color);
		setStroke(g2,zoom);

		if (cubicCurve==null) cubicCurve=new CubicCurve2D.Double();
		cubicCurve.x1=p1.x;
		cubicCurve.y1=p1.y;
		cubicCurve.ctrlx1=ctrlx1;
		cubicCurve.ctrly1=ctrly1;
		cubicCurve.ctrlx2=ctrlx2;
		cubicCurve.ctrly2=ctrly2;
		cubicCurve.x2=p2.x;
		cubicCurve.y2=p2.y;
		g2.draw(cubicCurve);

		g2.setStroke(saveStroke);
	}

	/**
	 * Liefert die eingestellte Linienbreite
	 * @return	Linienbreite
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Stellt die Linienbreite ein.
	 * @param width	Linienbreite
	 */
	public void setWidth(final int width) {
		this.width=FastMath.max(1,width);
	}

	/**
	 * Liefert die Linienfarbe.
	 * @return	Linienfarbe
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Stellt die Linienfarbe ein.
	 * @param color	Linienfarbe
	 */
	public void setColor(final Color color) {
		if (color!=null) this.color=color;
	}

	/**
	 * Liefert den Linientyp.
	 * @return	Linientyp
	 * @see #LINE_TYPE_NAMES
	 */
	public int getType() {
		return type;
	}

	/**
	 * Stellt den Linientyp ein.
	 * @param type	Linientyp
	 * @see #LINE_TYPE_NAMES
	 */
	public void setType(final int type) {
		this.type=FastMath.max(0,FastMath.min(MAX_TYPE,type));
	}

	/**
	 * Stellt Linienbreite, Farbe und Typ gleichzeitig ein.
	 * @param width	Neue Linienbreite
	 * @param color	Neue Linienfarbe
	 * @param type	Neuer Linientyp
	 * @see #LINE_TYPE_NAMES
	 */
	public void set(final int width, final Color color, final int type) {
		setWidth(width);
		if (color==null) setColor(Color.BLACK); else setColor(color);
		setType(type);
	}

	/**
	 * Überträgt die Einstellung aus einem anderen {@link ComplexLine}-Objekt in dieses
	 * @param otherLine	Objekt aus dem die Daten kopiert werden sollen
	 */
	public void set(final ComplexLine otherLine) {
		if (otherLine!=null) set(otherLine.getWidth(),otherLine.getColor(),otherLine.getType());
	}

	/**
	 * Vergleicht zwei Linientypen
	 * @param otherLine	Anderes Objekt, welches mit diesem verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Objekte inhaltlich identisch sind.
	 */
	public boolean equalsLine(final ComplexLine otherLine) {
		if (otherLine==null) return false;

		if (otherLine.width!=width) return false;
		if (!otherLine.color.equals(color)) return false;
		if (otherLine.type!=type) return false;

		return true;
	}

	@Override
	public ComplexLine clone() {
		final ComplexLine clone=new ComplexLine();
		clone.width=width;
		clone.color=color;
		clone.type=type;
		return clone;
	}

	/**
	 * Erstellt ein Bild, welches ein Beispiel für den Linientyp darstellt
	 * @param type	Linientyp für den ein Beispielbild erzeugt werden soll
	 * @return	Beispielbild zur Verdeutlichung des Linientyps
	 */
	public static BufferedImage getExample(final int type) {
		final BufferedImage image=new BufferedImage(96,16,BufferedImage.TYPE_4BYTE_ABGR);

		final ComplexLine line=new ComplexLine((type<7)?3:12,null,type);

		final Graphics g=image.getGraphics();
		line.draw(g,new Point(8,8),new Point(88,8),1);

		return image;
	}
}
