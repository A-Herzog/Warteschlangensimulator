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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import simulator.runmodel.SimulationData;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementAnimationTableDialog;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.GradientFill;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeigt ein sich während der Animation aktualisierendes Tortendiagramm an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationPieChart extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Beschriftungen an den Tortensegmenten anzeigen
	 * @see ModelElementAnimationPieChart#getLabelMode()
	 * @see ModelElementAnimationPieChart#setLabelMode(LabelMode)
	 */
	public enum LabelMode {
		/** Keine Beschriftungen */
		NO_LABELS,
		/** Beschriftungen an großen Tortensegmenten */
		BIG_PARTS,
		/** Beschriftungen an allen Tortensegmenten */
		ALL_PARTS
	}

	/**
	 * Darstellungsart
	 * @see ModelElementAnimationPieChart#getDiagramDrawMode()
	 * @see ModelElementAnimationPieChart#setDiagramDrawMode(DrawMode)
	 */
	public enum DrawMode {
		/** Mit Loch in der Mitte */
		PIE,
		/** Als klassisches Tortendiagramm */
		DONUT
	}

	/**
	 * Sichert ab, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #recordedValues}
	 * zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/**
	 * Aufgezeichnete Werte zur Anzeige während der Animation
	 */
	private double[] recordedValues;

	/**
	 * Rechenausdrücke für die Segmente
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<AnimationExpression> expression=new ArrayList<>();

	/**
	 * Farben für die Segmente
	 * @see #getExpressionData()
	 * @see #setExpressionData(List)
	 */
	private final List<Color> expressionColor=new ArrayList<>();

	/**
	 * Beschriftungen an den Tortensegmenten anzeigen
	 * @see LabelMode
	 */
	private LabelMode labelMode;

	/**
	 * Darstellungsart
	 * @see DrawMode
	 */
	private DrawMode drawMode;

	/**
	 * Breite der Linie
	 * @see #getBorderWidth()
	 * @see #setBorderWidth(int)
	 */
	private int borderWidth=1;

	/**
	 * Farbe der Linie
	 * @see #getBorderColor()
	 * @see #setBorderColor(Color)
	 */
	private Color borderColor=Color.BLACK;

	/**
	 * Füllfarbe des Kastens
	 * @see #getBackgroundColor()
	 * @see #setBackgroundColor(Color)
	 */
	private Color backgroundColor=null;

	/**
	 * Cache der Bereichsfüllung-Zeichners
	 * für {@link #drawDiagramSegments(Graphics2D, Rectangle, double)}
	 * @see #drawDiagramSegments(Graphics2D, Rectangle, double)
	 */
	private GradientFill[] filler;

	/**
	 * Konstruktor der Klasse
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationPieChart(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(75,75),Shapes.ShapeType.SHAPE_NONE);
		filler=null;
		labelMode=LabelMode.BIG_PARTS;
		drawMode=DrawMode.PIE;
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public Icon getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART.getIcon();
	}

	@Override
	public boolean isVisualOnly() {
		return true;
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationPieChart.Tooltip");
	}

	/**
	 * Liefert eine Liste der vorhandenen Diagramm-Einträge.<br>
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (AnimationExpression), Balkenfarbe (Color).
	 * @return	Liste der Diagramm-Einträge
	 */
	public List<Object[]> getExpressionData() {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=expressionColor.size()) break;

			Object[] row=new Object[2];
			row[0]=expression.get(i);
			row[1]=expressionColor.get(i);

			data.add(row);
		}
		return data;
	}

	/**
	 * Ersetzt die bisherigen Diagramm-Einträge durch eine neue Liste.<br>
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (AnimationExpression), Balkenfarbe (Color).
	 * @param data	Liste der neuen Diagramm-Einträge
	 */
	public void setExpressionData(final List<Object[]> data) {
		expression.clear();
		expressionColor.clear();

		for (Object[] row: data) if (row.length==2) {
			if (!(row[0] instanceof AnimationExpression)) continue;
			if (!(row[1] instanceof Color)) continue;
			expression.add((AnimationExpression)row[0]);
			expressionColor.add((Color)row[1]);
		}
	}

	/**
	 * Liefert die eingestellte Beschriftungsart für die Tortensegmente.
	 * @return	Beschriftungsart für die Tortensegmente
	 * @see #setLabelMode(LabelMode)
	 * @see ModelElementAnimationPieChart.LabelMode
	 */
	public LabelMode getLabelMode() {
		return labelMode;
	}

	/**
	 * Stellt eine neue Beschriftungsart für die Tortensegmente ein.
	 * @param labelMode	Beschriftungsart für die Tortensegmente
	 * @see #getLabelMode()
	 * @see ModelElementAnimationPieChart.LabelMode
	 */
	public void setLabelMode(final LabelMode labelMode) {
		if (labelMode!=null) this.labelMode=labelMode;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Darstellungsart.
	 * @return	Darstellungsart
	 * @see #setDiagramDrawMode(DrawMode)
	 * @see ModelElementAnimationPieChart.DrawMode
	 */
	public DrawMode getDiagramDrawMode() {
		return drawMode;
	}

	/**
	 * Stellt die Darstellungsart ein.
	 * @param drawMode	Darstellungsart
	 * @see #getDiagramDrawMode()
	 * @see ModelElementAnimationPieChart.DrawMode
	 */
	public void setDiagramDrawMode(DrawMode drawMode) {
		if (drawMode!=null) this.drawMode=drawMode;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Breite der Linie
	 * @return	Aktuelle breite der Linie
	 */
	public int getBorderWidth() {
		return borderWidth;
	}

	/**
	 * Stellt die breite der Linie ein
	 * @param borderWidth	Neue Breite für die Linie
	 */
	public void setBorderWidth(final int borderWidth) {
		if (borderWidth>=0 && borderWidth<=50) this.borderWidth=borderWidth;
	}

	/**
	 * Liefert die aktuelle Farbe der Linie
	 * @return	Aktuelle Farbe der Linie
	 */
	public Color getBorderColor() {
		return borderColor;
	}

	/**
	 * Stellt die Farbe der Linie ein
	 * @param color	Farbe der Linie
	 */
	public void setBorderColor(final Color color) {
		if (color!=null) this.borderColor=color;
		fireChanged();
	}

	/**
	 * Liefert die aktuelle Füllfarbe des Kastens
	 * @return	Aktuelle Füllfarbe des Kastens (kann <code>null</code> sein für transparent)
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Stellt die Füllfarbe des Kastens ein
	 * @param color	Füllfarbe des Kastens (oder <code>null</code> für transparent)
	 */
	public void setBackgroundColor(final Color color) {
		this.backgroundColor=color;
		fireChanged();
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationPieChart)) return false;

		final ModelElementAnimationPieChart other=(ModelElementAnimationPieChart)element;

		if (expression.size()!=other.expression.size()) return false;
		for (int i=0;i<expression.size();i++) if (!expression.get(i).equalsAnimationExpression(other.expression.get(i))) return false;

		if (expressionColor.size()!=other.expressionColor.size()) return false;
		for (int i=0;i<expressionColor.size();i++) if (!expressionColor.get(i).equals(other.expressionColor.get(i))) return false;

		if (labelMode!=other.labelMode) return false;
		if (drawMode!=other.drawMode) return false;

		if (borderWidth!=other.borderWidth) return false;
		if (!other.borderColor.equals(borderColor)) return false;
		if (!(other.backgroundColor==null && backgroundColor==null)) {
			if (other.backgroundColor==null || backgroundColor==null) return false;
			if (!other.backgroundColor.equals(backgroundColor)) return false;
		}

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationPieChart) {
			final ModelElementAnimationPieChart source=(ModelElementAnimationPieChart)element;

			expression.addAll(source.expression.stream().map(ex->new AnimationExpression(ex)).collect(Collectors.toList()));
			expressionColor.addAll(source.expressionColor);

			labelMode=source.labelMode;
			drawMode=source.drawMode;

			borderWidth=source.borderWidth;
			borderColor=source.borderColor;
			backgroundColor=source.backgroundColor;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationPieChart clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationPieChart element=new ModelElementAnimationPieChart(model,surface);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Prüft, ob sich ein Punkt im Bereich einer Linie P-Q befindet
	 * @param p	Startpunkt der Linie
	 * @param q	Endpunkt der Linie
	 * @param point	Punkt, bei dem geprüft werden soll, ob dieser nahe der Linie P-Q ist
	 * @param zoom	Zoomfaktor gemäß dessen der Punkt skaliert wird
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt nahe der Linie befindet
	 */
	protected final boolean isNearLine(final Point p, final Point q, final Point point, final double zoom) {
		final Point v=new Point(q.x-p.x,q.y-p.y); /* Verbindungsvektor P->Q */
		final Point x=new Point((int)FastMath.round(point.x/zoom),(int)FastMath.round(point.y/zoom));

		double alpha=0;
		if (v.y!=0) {
			alpha=((double)v.y)/(v.x*v.x+v.y*v.y)*(x.y-((double)v.x)/v.y*(p.x-x.x)-p.y);
		} else {
			alpha=((double)v.x)/(v.y*v.y+v.x*v.x)*(x.x-((double)v.y)/v.x*(p.y-x.y)-p.x);
		}
		final Point y=new Point((int)FastMath.round(p.x+alpha*v.x),(int)FastMath.round(p.y+alpha*v.y)); /* Lotfußpunkt von X auf P->Q */

		if (Math.abs(x.x-y.x)>MAX_POINT_DELTA || Math.abs(x.y-y.y)>MAX_POINT_DELTA) return false; /* Abstand von Gerade zu groß? */
		final double len=FastMath.sqrt(v.x*v.x+v.y*v.y);

		if (alpha*len<-MAX_POINT_DELTA || alpha*len>len+MAX_POINT_DELTA) return false; /* Fußpunkt vor Beginn oder nach Ende der Strecke P->Q? */

		return true;
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		if (super.containsPoint(point,zoom)) return true;

		final Point p=getPosition(true);
		final Dimension s=getSize();

		if (isNearLine(new Point(p.x,p.y),new Point(p.x+s.width,p.y),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y),new Point(p.x+s.width,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x+s.width,p.y+s.height),new Point(p.x,p.y+s.height),point,zoom)) return true;
		if (isNearLine(new Point(p.x,p.y+s.height),new Point(p.x,p.y),point,zoom)) return true;

		return false;
	}

	/**
	 * Liefert die Position eines bestimmten Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @return	Position des Randpunktes oder <code>null</code>, wenn der Index außerhalb des gültigen Bereichs liegt
	 */
	@Override
	public Point getBorderPointPosition(final int index) {
		final Point p=getPosition(true);
		final Dimension s=getSize();

		switch (index) {
		case 0: return new Point(p); /* Kopie, damit diese unabhängig von Veränderungen des Wertes ist */
		case 1: return new Point(p.x+s.width,p.y);
		case 2: return new Point(p.x+s.width,p.y+s.height);
		case 3: return new Point(p.x,p.y+s.height);
		default: return null;
		}
	}

	/**
	 * Setzt die Position eines Randpunktes
	 * @param index	0-basierender Index des Randpunktes
	 * @param point	Neue Position des Randpunktes
	 */
	@Override
	public void setBorderPointPosition(final int index, final Point point) {
		final Point p1=getPosition(true);
		final Point p2=getLowerRightPosition();

		switch (index) {
		case 0:
			setPosition(point);
			setSize(new Dimension(p2.x-point.x,p2.y-point.y));
			break;
		case 1:
			setPosition(new Point(p1.x,point.y));
			setSize(new Dimension(point.x-p1.x,p2.y-point.y));
			break;
		case 2:
			setSize(new Dimension(point.x-p1.x,point.y-p1.y));
			break;
		case 3:
			setPosition(new Point(point.x,p1.y));
			setSize(new Dimension(p2.x-point.x,point.y-p1.y));
			break;
		}
	}

	/**
	 * Zeichnet eine Markierungs-Box an einer Ecke des Elements
	 * @param graphics	Grafik-Objekt, in das gezeichnet werden soll
	 * @param point	Position des Punktes
	 * @param zoom	Zoomfaktor
	 */
	protected final void drawBorderBox(final Graphics2D graphics, final Point point, final double zoom) {
		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(1));
		final Rectangle rectangle=new Rectangle((int)FastMath.round(point.x*zoom)-SELECT_BOX_SIZE/2,(int)FastMath.round(point.y*zoom)-SELECT_BOX_SIZE/2,SELECT_BOX_SIZE,SELECT_BOX_SIZE);
		graphics.draw(rectangle);
		graphics.fill(rectangle);
	}

	/**
	 * Farben für die Dummy-Segmente (die im Editor, also vor dem Start einer Animation, angezeigt werden)
	 * @see #drawDummyDiagramSegments(Graphics2D, Rectangle, double)
	 */
	private static final Color[] dummyColor={Color.BLUE,Color.RED,Color.GREEN};

	/**
	 * Werte für die Dummy-Segmente (die im Editor, also vor dem Start einer Animation, angezeigt werden)
	 * @see #drawDummyDiagramSegments(Graphics2D, Rectangle, double)
	 */
	private static final double[] dummyValue={0.6,0.4,0.8};

	/**
	 * Zeichnet eine Beschriftung für ein Tortensegment.
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 * @param angle	Winkel an dem die Beschriftung angezeigt werden soll
	 * @param value	Anzuzeigender Wert
	 * @param donutMode	 Handelt es sich bei dem Diagramm um Kreissegmente (<code>false</code>) oder Ringsemgente (<code>true</code>)
	 */
	private void drawLabel(final Graphics2D g, final Rectangle rectangle, final int angle, final double value, final boolean donutMode) {
		final String text=NumberTools.formatPercent(value,0);
		final FontMetrics metrics=g.getFontMetrics();
		final int ascent=metrics.getAscent();
		final int descent=metrics.getDescent();
		final int width=g.getFontMetrics().stringWidth(text);

		final double moveFactor=donutMode?0.38:0.33;
		final int mx=(int)Math.round(rectangle.x+rectangle.width*(0.5+moveFactor*Math.cos(angle*Math.PI/180)));
		final int my=(int)Math.round(rectangle.y+rectangle.height*(0.5-moveFactor*Math.sin(angle*Math.PI/180)));

		g.setColor(Color.BLACK);
		g.drawString(text,mx-width/2,my-(ascent+descent)/2+ascent);
	}

	/**
	 * Stiftobjekt zum Zeichnen der Kreissegmente
	 * @see #lastStrokeWidth
	 * @see #drawDiagramSegments(Graphics2D, Rectangle, double)
	 * @see #drawDummyDiagramSegments(Graphics2D, Rectangle, double)
	 */
	private BasicStroke lastStroke;

	/**
	 * Aktuelle Linienbreite für {@link #lastStroke}
	 * @see #lastStroke
	 * @see #drawDiagramSegments(Graphics2D, Rectangle, double)
	 * @see #drawDummyDiagramSegments(Graphics2D, Rectangle, double)
	 */
	private int lastStrokeWidth;

	/**
	 * Zeichnet Dummy-Segmente während der Editor aktiv ist (und noch keine Animationsdaten vorliegen)
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 * @param zoom	Zoomfaktor
	 */
	private void drawDummyDiagramSegments(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		double sum=0;
		for (double value: dummyValue) sum+=value;
		if (sum==0) return;

		if (filler==null || filler.length!=dummyValue.length) {
			filler=new GradientFill[dummyValue.length];
			for (int i=0;i<filler.length;i++) filler[i]=new GradientFill(false);
		}

		g.setFont(FontCache.getFontCache().getFont(FontCache.defaultFamily,0,11*Math.min(rectangle.width,rectangle.height)/100));

		if (drawMode==DrawMode.DONUT) {
			int strokeWidth=2*Math.min(rectangle.width,rectangle.height)/7;
			if (lastStroke==null || lastStrokeWidth!=strokeWidth) {
				lastStroke=new BasicStroke(strokeWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER);
				lastStrokeWidth=strokeWidth;
			}
			g.setStroke(lastStroke);
		}

		int startAngle=90;
		for (int i=0;i<dummyValue.length;i++) {
			final double value=dummyValue[i]/sum;
			final int angle=-(int)Math.round(value*360);

			filler[i].set(g,rectangle,dummyColor[i%dummyColor.length],true);
			switch (drawMode) {
			case PIE:
				g.fillArc(rectangle.x+1,rectangle.y+1,rectangle.width-2,rectangle.height-2,startAngle,angle);
				break;
			case DONUT:
				g.drawArc(rectangle.x+1+lastStrokeWidth/2,rectangle.y+1+lastStrokeWidth/2,rectangle.width-2-lastStrokeWidth,rectangle.height-2-lastStrokeWidth,startAngle,angle);
				break;
			}

			if (labelMode==LabelMode.ALL_PARTS || (labelMode==LabelMode.BIG_PARTS && value>=0.2)) {
				drawLabel(g,rectangle,startAngle+angle/2,value,true);
			}
			startAngle+=angle;
		}
	}

	/**
	 * Zeichnet die Segmente des Tortendiagramms
	 * @param g	Grafik-Ausgabeobjekt
	 * @param rectangle	Ausgaberechteck
	 * @param zoom	Zoomfaktor
	 */
	private void drawDiagramSegments(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		if (recordedValues==null) {
			drawDummyDiagramSegments(g,rectangle,zoom);
			return;
		}

		drawLock.acquireUninterruptibly();
		try {
			double sum=0;
			for (double value: recordedValues) sum+=value;
			if (sum==0) return;

			if (filler==null || filler.length!=recordedValues.length) {
				filler=new GradientFill[recordedValues.length];
				for (int i=0;i<filler.length;i++) filler[i]=new GradientFill(false);
			}

			g.setFont(FontCache.getFontCache().getFont(FontCache.defaultFamily,0,11*Math.min(rectangle.width,rectangle.height)/100));

			if (drawMode==DrawMode.DONUT) {
				int strokeWidth=2*Math.min(rectangle.width,rectangle.height)/7;
				if (lastStroke==null || lastStrokeWidth!=strokeWidth) {
					lastStroke=new BasicStroke(strokeWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER);
					lastStrokeWidth=strokeWidth;
				}
				g.setStroke(lastStroke);
			}

			int startAngle=90;
			for (int i=0;i<recordedValues.length;i++) {
				final double value=recordedValues[i]/sum;
				final int angle=-(int)Math.round(value*360);

				filler[i].set(g,rectangle,expressionColor.get(i),true);
				switch (drawMode) {
				case PIE:
					g.fillArc(rectangle.x+1,rectangle.y+1,rectangle.width-2,rectangle.height-2,startAngle,angle);
					break;
				case DONUT:
					g.drawArc(rectangle.x+1+lastStrokeWidth/2,rectangle.y+1+lastStrokeWidth/2,rectangle.width-2-lastStrokeWidth,rectangle.height-2-lastStrokeWidth,startAngle,angle);
					break;
				}

				if (labelMode==LabelMode.ALL_PARTS || (labelMode==LabelMode.BIG_PARTS && value>=0.2)) {
					drawLabel(g,rectangle,startAngle+angle/2,value,false);
				}
				startAngle+=angle;
			}
		} finally {
			drawLock.release();
		}
	}

	/**
	 * Zeichnet das Element in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	@Override
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showSelectionFrames) {
		setClip(graphics,drawRect,null);

		final Point p=getPosition(true);
		final Dimension s=getSize();

		final Graphics2D g2=(Graphics2D)graphics;
		final Stroke saveStroke=g2.getStroke();

		Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(Math.abs(s.width)*zoom),(int)FastMath.round(Math.abs(s.height)*zoom));
		if (backgroundColor!=null) {
			g2.setColor(backgroundColor);
			g2.fill(rectangle);
		}

		setClip(g2,drawRect,rectangle);
		drawDiagramSegments(g2,rectangle,zoom);
		setClip(g2,drawRect,null);

		boolean drawBorder=false;
		Color lineColor=borderColor;
		if (borderWidth>0) {
			g2.setStroke(new BasicStroke(borderWidth));
			drawBorder=true;
		}

		if (isSelected() && showSelectionFrames) {
			lineColor=Color.GREEN;
			g2.setStroke(new BasicStroke(FastMath.max(borderWidth,3)));
			drawBorder=true;
		} else {
			if (isSelectedArea() && showSelectionFrames) {
				lineColor=Color.BLUE;
				g2.setStroke(new BasicStroke(FastMath.max(borderWidth,3)));
				drawBorder=true;
			}
		}

		if (drawBorder) {
			g2.setColor(lineColor);
			g2.draw(rectangle);
		}

		if (isSelected() && showSelectionFrames) {
			drawBorderBox(g2,p,zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y),zoom);
			drawBorderBox(g2,new Point(p.x,p.y+s.height),zoom);
			drawBorderBox(g2,new Point(p.x+s.width,p.y+s.height),zoom);
		}

		g2.setStroke(saveStroke);
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.AnimationPieChart.Name");
	}

	/**
	 * Liefert ein <code>Runnable</code>-Objekt zurück, welches aufgerufen werden kann, wenn die Eigenschaften des Elements verändert werden sollen.
	 * @param owner	Übergeordnetes Fenster
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param clientData	Kundendaten-Objekt
	 * @param sequences	Fertigungspläne-Liste
	 * @return	<code>Runnable</code>-Objekt zur Einstellung der Eigenschaften oder <code>null</code>, wenn das Element keine Eigenschaften besitzt
	 */
	@Override
	public Runnable getProperties(final Component owner, final boolean readOnly, final ModelClientData clientData, final ModelSequences sequences) {
		return ()->{
			new ModelElementAnimationPieChartDialog(owner,ModelElementAnimationPieChart.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationPieChart.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		Element sub;

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPieChart.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+borderWidth);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPieChart.XML.LineColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(borderColor));

		if (backgroundColor!=null) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationPieChart.XML.BackgroundColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(backgroundColor));
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPieChart.XML.LabelMode"));
		node.appendChild(sub);
		switch (labelMode) {
		case NO_LABELS: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.LabelMode.NoLabels")); break;
		case BIG_PARTS: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.LabelMode.BigParts")); break;
		case ALL_PARTS: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.LabelMode.AllParts")); break;
		}

		sub=doc.createElement(Language.trPrimary("Surface.AnimationPieChart.XML.DiagramDrawMode"));
		node.appendChild(sub);
		switch (drawMode) {
		case PIE: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.DiagramDrawMode.Pie")); break;
		case DONUT: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.DiagramDrawMode.Donut")); break;
		}

		for (int i=0;i<expression.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationPieChart.XML.Set"));
			node.appendChild(sub);
			expression.get(i).storeToXML(sub);
			sub.setAttribute(Language.trPrimary("Surface.AnimationPieChart.XML.Set.PieColor"),EditModel.saveColor(expressionColor.get(i)));
		}
	}

	/**
	 * Lädt eine einzelne Einstellung des Modell-Elements aus einem einzelnen xml-Element.
	 * @param name	Name des xml-Elements
	 * @param content	Inhalt des xml-Elements als Text
	 * @param node	xml-Element, aus dem das Datum geladen werden soll
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadProperty(final String name, final String content, final Element node) {
		String error=super.loadProperty(name,content,node);
		if (error!=null) return error;

		if (Language.trAll("Surface.AnimationPieChart.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationPieChart.XML.LineColor",name) && !content.trim().isEmpty()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationPieChart.XML.BackgroundColor",name) && !content.trim().isEmpty()) {
			backgroundColor=EditModel.loadColor(content);
			if (backgroundColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationPieChart.XML.Set",name) && !content.trim().isEmpty()) {
			final String colorString=Language.trAllAttribute("Surface.AnimationPieChart.XML.Set.PieColor",node);
			final Color color;
			if (colorString.trim().isEmpty()) {
				color=Color.BLACK;
			} else {
				color=EditModel.loadColor(colorString);
				if (color==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.AnimationPieChart.XML.Set.PieColor"),name,node.getParentNode().getNodeName());
			}
			expressionColor.add(color);

			final AnimationExpression ex=new AnimationExpression();
			ex.loadFromXML(node);
			expression.add(ex);

			return null;
		}

		if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode",name)) {
			if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode.NoLabels",content)) labelMode=LabelMode.NO_LABELS;
			if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode.BigParts",content)) labelMode=LabelMode.BIG_PARTS;
			if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode.AllParts",content)) labelMode=LabelMode.ALL_PARTS;
			return null;
		}

		if (Language.trAll("Surface.AnimationPieChart.XML.DiagramDrawMode",name)) {
			if (Language.trAll("Surface.AnimationPieChart.XML.DiagramDrawMode.Pie",content)) drawMode=DrawMode.PIE;
			if (Language.trAll("Surface.AnimationPieChart.XML.DiagramDrawMode.Donut",content)) drawMode=DrawMode.DONUT;
			return null;
		}

		return null;
	}

	/**
	 * In {@link #updateSimulationData(SimulationData, boolean)} berechnete Ausgabedaten
	 * {@link #updateSimulationData(SimulationData, boolean)}
	 */
	private double[][] dataSets;

	/**
	 * Für die Ausgabe aktiver Eintrag in {@link #dataSets}
	 * @see #dataSets
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private int drawActiveDataSet;

	@Override
	public boolean updateSimulationData(final SimulationData simData, final boolean isPreview) {
		if (isPreview) return false;

		if (dataSets==null) {
			dataSets=new double[2][];
			dataSets[0]=new double[expression.size()];
			dataSets[1]=new double[expression.size()];
			drawActiveDataSet=0;
		}

		boolean needUpdate=false;
		final int nextDataSet=(drawActiveDataSet+1)%2;
		for (int i=0;i<dataSets[0].length;i++) {
			final double value=expression.get(i).getAnimationValue(this,simData);
			dataSets[nextDataSet][i]=value;
			if (!needUpdate) needUpdate=(dataSets[drawActiveDataSet][i]!=value);
		}
		if (!needUpdate) return false;

		drawLock.acquireUninterruptibly();
		try {
			drawActiveDataSet=nextDataSet;
			recordedValues=dataSets[drawActiveDataSet];
		} finally {
			drawLock.release();
		}
		return true;
	}

	@Override
	public void initAnimation(final SimulationData simData) {
		recordedValues=null;

		for (int i=0;i<expression.size();i++) {
			expression.get(i).initAnimation(this,simData);
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationPieChart";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLAnimationPieChart(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationPieChart(rect,segmentShares,segmentColors,borderColor,borderWidth,fillColor) {\n");

		sb.append("  if (typeof(fillColor)!=\"undefined\") {\n");
		sb.append("    context.fillStyle=fillColor;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  var cx=rect.x+rect.w/2;\n");
		sb.append("  var cy=rect.y+rect.h/2;\n");
		sb.append("  var r=Math.min(rect.w/2,rect.h/2);\n");
		sb.append("  var startAngle=-Math.PI/2;\n");
		sb.append("  for (var i=0;i<Math.min(segmentShares.length,segmentColors.length);i++) {\n");
		sb.append("    var angle=segmentShares[i]*2*Math.PI;\n");
		sb.append("    context.fillStyle=segmentColors[i];\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(cx,cy);\n");
		sb.append("    context.arc(cx,cy,r,startAngle,startAngle+angle);\n");
		sb.append("    context.lineTo(cx,cy);\n");
		sb.append("    startAngle+=angle;\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.fill();\n");
		sb.append("  }\n");

		sb.append("  if (borderWidth>0) {\n");
		sb.append("    context.strokeStyle=borderColor;\n");
		sb.append("    context.lineWidth=borderWidth;\n");
		sb.append("    context.beginPath();\n");
		sb.append("    context.moveTo(rect.x,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y);\n");
		sb.append("    context.lineTo(rect.x+rect.w,rect.y+rect.h);\n");
		sb.append("    context.lineTo(rect.x,rect.y+rect.h);\n");
		sb.append("    context.closePath();\n");
		sb.append("    context.stroke();\n");
		sb.append("  }\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationPieChart",builder->getHTMLAnimationPieChart(builder));

		final Point p=getPosition(true);
		final Dimension d=getSize();
		final String rect="{x: "+p.x+", y: "+p.y+", w: "+d.width+", h: "+d.height+"}";
		final String border="\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\"";

		final StringBuilder segmentShares=new StringBuilder();
		double sum=0;
		for (double value: dummyValue) sum+=value;
		for (double value: dummyValue) {
			if (segmentShares.length()>0) segmentShares.append(",");
			segmentShares.append(""+(value/sum));
		}

		final StringBuilder segmentColors=new StringBuilder();
		for (Color c: dummyColor) {
			if (segmentColors.length()>0) segmentColors.append(",");
			segmentColors.append("\""+HTMLOutputBuilder.colorToHTML(c)+"\"");
		}

		if (backgroundColor==null) {
			outputBuilder.outputBody.append("drawAnimationPieChart("+rect+",["+segmentShares.toString()+"],["+segmentColors.toString()+"],"+border+","+borderWidth+");\n");
		} else {
			final String fill="\""+HTMLOutputBuilder.colorToHTML(backgroundColor)+"\"";
			outputBuilder.outputBody.append("drawAnimationPieChart("+rect+",["+segmentShares.toString()+"],["+segmentColors.toString()+"],"+border+","+borderWidth+","+fill+");\n");
		}

	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}

	@Override
	public boolean hasAnimationStatisticsData(final SimulationData simData) {
		return simData!=null;
	}

	/**
	 * Liefert die Daten in Tabellenform für die Ausgabe einer Datentabelle während der Animation
	 * @param simData	Simulationsdatenobjekt
	 * @return	Tabelle mit den aktuellen Ausgabedaten
	 */
	private Table getAnimationRunTimeTableData(final SimulationData simData) {
		final Table table=new Table();

		table.addLine(new String[] {Language.tr("ModelDescription.Expression"),Language.tr("Statistic.Viewer.Chart.Value")});

		drawLock.acquireUninterruptibly();
		try {
			for (int i=0;i<recordedValues.length;i++) {
				final List<String> line=new ArrayList<>(2);
				final String info;
				switch (expression.get(i).getMode()) {
				case Expression: info=expression.get(i).getExpression(); break;
				case Java: info=Language.tr("ModelDescription.Expression.Java"); break;
				case Javascript: info=Language.tr("ModelDescription.Expression.Javascript"); break;
				default: info=expression.get(i).getExpression(); break;
				}
				line.add(info);
				line.add(NumberTools.formatNumber(recordedValues[i],3));
				table.addLine(line);
			}
		} finally {
			drawLock.release();
		}
		return table;
	}

	@Override
	public void showElementAnimationStatisticsData(final Component owner, final SimulationData simData) {
		if (simData==null) return;
		new ModelElementAnimationTableDialog(owner,getContextMenuElementName()+" (id="+getId()+")",()->getAnimationRunTimeTableData(simData),this);
	}

	@Override
	protected void storeElementAnimationStatisticsData(final Component owner, final JPopupMenu menu, final SimulationData simData) {
		if (simData==null) return;
		ModelElementAnimationTableDialog.buildPopupMenuItem(owner,menu,getAnimationRunTimeTableData(simData));
	}

	/**
	 * Erstellt einen Kontextmenü-Menüpunkt zur Festlegung einer Standardgröße für das Element
	 * @param x	Horizontale Größe (gemessen in 50-Pixel-Kästen)
	 * @param y	Vertikale Größe (gemessen in 50-Pixel-Kästen)
	 * @return	Neuer Menüpunkt
	 */
	private JMenuItem sizeItem(final int x, final int y) {
		final JMenuItem item=new JMenuItem(x+"x"+y);
		item.addActionListener(e->{
			setSize(new Dimension(x*50,y*50));
			fireChanged();
		});
		return item;
	}

	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		super.addContextMenuItems(owner,popupMenu,surfacePanel,point,readOnly);

		final JMenu sizesMenu=new JMenu(Language.tr("Surface.PopupMenu.DefaultSizes"));
		sizesMenu.setIcon(Images.SETUP_WINDOW_SIZE_FULL.getIcon());
		popupMenu.add(sizesMenu);

		sizesMenu.add(sizeItem(2,2));
		sizesMenu.add(sizeItem(5,5));
		sizesMenu.add(sizeItem(8,5));
		sizesMenu.add(sizeItem(12,5));
		sizesMenu.add(sizeItem(16,5));
	}

	@Override
	public void search(final FullTextSearch searcher) {
		super.search(searcher);

		for (int i=0;i<expression.size();i++) {
			final AnimationExpression ex=expression.get(i);
			if (ex.getMode()==AnimationExpression.ExpressionMode.Expression) {
				searcher.testString(this,Language.tr("Editor.DialogBase.Search.Expression"),ex.getExpression(),newExpression->ex.setExpression(newExpression));
			}
		}
		searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.BorderWidth"),borderWidth,newBorderWidth->{if (newBorderWidth>=0) borderWidth=newBorderWidth;});
	}
}