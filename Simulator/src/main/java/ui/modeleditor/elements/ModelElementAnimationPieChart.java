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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.images.Images;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
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
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Beschriftungen an den Tortensegmenten anzeigen
	 * @author Alexander Herzog
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

	private Semaphore drawLock=new Semaphore(1);
	private double[] recordedValues;

	private final List<String> expression=new ArrayList<>();
	private final List<Color> expressionColor=new ArrayList<>();

	private LabelMode labelMode;

	private int borderWidth=1;
	private Color borderColor=Color.BLACK;
	private Color backgroundColor=null;

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
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_PIE_CHART.getURL();
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
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (String), Balkenfarbe (Color).
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
	 * Jeder Diagramm-Eintrag besteht aus 2 Objekten in einem Array: Ausdruck (String), Balkenfarbe (Color).
	 * @param data	Liste der neuen Diagramm-Einträge
	 */
	public void setExpressionData(final List<Object[]> data) {
		expression.clear();
		expressionColor.clear();

		for (Object[] row: data) if (row.length==2) {
			if (!(row[0] instanceof String)) continue;
			if (!(row[1] instanceof Color)) continue;
			expression.add((String)row[0]);
			expressionColor.add((Color)row[1]);
		}
	}

	/**
	 * Liefert die eingestellte Beschriftungsart für die Tortensegmente.
	 * @return	Beschriftungsart für die Tortensegmente
	 * @see ModelElementAnimationPieChart.LabelMode
	 */
	public LabelMode getLabelMode() {
		return labelMode;
	}

	/**
	 * Stellt eine neue Beschriftungsart für die Tortensegmente ein.
	 * @param labelMode	Beschriftungsart für die Tortensegmente
	 * @see ModelElementAnimationPieChart.LabelMode
	 */
	public void setLabelMode(final LabelMode labelMode) {
		if (labelMode!=null) this.labelMode=labelMode;
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
		for (int i=0;i<expression.size();i++) if (!expression.get(i).equals(other.expression.get(i))) return false;

		if (expressionColor.size()!=other.expressionColor.size()) return false;
		for (int i=0;i<expressionColor.size();i++) if (!expressionColor.get(i).equals(other.expressionColor.get(i))) return false;

		if (labelMode!=other.labelMode) return false;

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

			expression.addAll(source.expression);
			expressionColor.addAll(source.expressionColor);

			labelMode=source.labelMode;

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

		if (FastMath.abs(x.x-y.x)>MAX_POINT_DELTA || FastMath.abs(x.y-y.y)>MAX_POINT_DELTA) return false; /* Abstand von Gerade zu groß? */
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

	private static final Color[] dummyColor={Color.BLUE,Color.RED,Color.GREEN};
	private static final double[] dummyValue={0.6,0.4,0.8};


	private void drawLabel(final Graphics2D g, final Rectangle rectangle, final int angle, final double value) {
		final String text=NumberTools.formatPercent(value,0);
		final FontMetrics metrics=g.getFontMetrics();
		final int ascent=metrics.getAscent();
		final int descent=metrics.getDescent();
		final int width=g.getFontMetrics().stringWidth(text);

		final int mx=(int)Math.round(rectangle.x+rectangle.width*(0.5+0.33*Math.cos(angle*Math.PI/180)));
		final int my=(int)Math.round(rectangle.y+rectangle.height*(0.5-0.33*Math.sin(angle*Math.PI/180)));

		g.setColor(Color.BLACK);
		g.drawString(text,mx-width/2,my-(ascent+descent)/2+ascent);
	}

	private void drawDummyDiagramSegments(final Graphics2D g, final Rectangle rectangle, final double zoom) {
		double sum=0;
		for (double value: dummyValue) sum+=value;
		if (sum==0) return;

		if (filler==null || filler.length!=dummyValue.length) {
			filler=new GradientFill[dummyValue.length];
			for (int i=0;i<filler.length;i++) filler[i]=new GradientFill();
		}

		g.setFont(FontCache.getFontCache().getFont(FontCache.defaultFamily,0,(int)FastMath.round(11*zoom)));

		int startAngle=90;
		for (int i=0;i<dummyValue.length;i++) {
			final double value=dummyValue[i]/sum;
			final int angle=-(int)Math.round(value*360);
			filler[i].set(g,rectangle,dummyColor[i%dummyColor.length],true);
			g.fillArc(rectangle.x+1,rectangle.y+1,rectangle.width-2,rectangle.height-2,startAngle,angle);
			if (labelMode==LabelMode.ALL_PARTS || (labelMode==LabelMode.BIG_PARTS && value>=0.2)) {
				drawLabel(g,rectangle,startAngle+angle/2,value);
			}
			startAngle+=angle;
		}
	}

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
				for (int i=0;i<filler.length;i++) filler[i]=new GradientFill();
			}

			g.setFont(FontCache.getFontCache().getFont(FontCache.defaultFamily,0,(int)FastMath.round(11*zoom)));

			int startAngle=90;
			for (int i=0;i<recordedValues.length;i++) {
				final double value=recordedValues[i]/sum;
				final int angle=-(int)Math.round(value*360);
				filler[i].set(g,rectangle,expressionColor.get(i),true);
				g.fillArc(rectangle.x+1,rectangle.y+1,rectangle.width-2,rectangle.height-2,startAngle,angle);
				if (labelMode==LabelMode.ALL_PARTS || (labelMode==LabelMode.BIG_PARTS && value>=0.2)) {
					drawLabel(g,rectangle,startAngle+angle/2,value);
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

		Rectangle rectangle=new Rectangle((int)FastMath.round(FastMath.min(p.x,p.x+s.width)*zoom),(int)FastMath.round(FastMath.min(p.y,p.y+s.height)*zoom),(int)FastMath.round(FastMath.abs(s.width)*zoom),(int)FastMath.round(FastMath.abs(s.height)*zoom));
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
		node.appendChild(sub);
		switch (labelMode) {
		case NO_LABELS: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.LabelMode.NoLabels")); break;
		case BIG_PARTS: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.LabelMode.BigParts")); break;
		case ALL_PARTS: sub.setTextContent(Language.trPrimary("Surface.AnimationPieChart.XML.LabelMode.AllParts")); break;
		}

		for (int i=0;i<expression.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationPieChart.XML.Set"));
			node.appendChild(sub);
			sub.setTextContent(expression.get(i));
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

			expression.add(content);

			return null;
		}

		if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode",name)) {
			if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode.NoLabels",content)) labelMode=LabelMode.NO_LABELS;
			if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode.BigParts",content)) labelMode=LabelMode.BIG_PARTS;
			if (Language.trAll("Surface.AnimationPieChart.XML.LabelMode.AllParts",content)) labelMode=LabelMode.ALL_PARTS;
		}

		return null;
	}

	private ExpressionCalc[] animationExpression;

	private double calcExpression(final SimulationData simData, final int index) {
		final ExpressionCalc calc=animationExpression[index];
		if (calc==null) return 0.0;
		simData.runData.setClientVariableValues(null);
		return calc.calcOrDefault(simData.runData.variableValues,simData,null,0);
	}


	private double[][] dataSets;
	private int drawActiveDataSet;

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
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
			final double value=calcExpression(simData,i);
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
	public void initAnimation(SimulationData simData) {
		recordedValues=null;
		animationExpression=new ExpressionCalc[expression.size()];

		for (int i=0;i<expression.size();i++) {
			animationExpression[i]=new ExpressionCalc(simData.runModel.variableNames);
			if (animationExpression[i].parse(expression.get(i))>=0) animationExpression[i]=null;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationPieChart";
	}

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

	private Table getAnimationRunTimeTableData(final SimulationData simData) {
		final Table table=new Table();

		table.addLine(new String[] {Language.tr("ModelDescription.Expression"),Language.tr("Statistic.Viewer.Chart.Value")});

		drawLock.acquireUninterruptibly();
		try {
			for (int i=0;i<recordedValues.length;i++) {
				final List<String> line=new ArrayList<String>(2);
				line.add(expression.get(i));
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
		new ModelElementAnimationTableDialog(owner,getContextMenuElementName()+" (id="+getId()+")",()->getAnimationRunTimeTableData(simData));
	}

	@Override
	protected void storeElementAnimationStatisticsData(final Component owner, final JPopupMenu menu, final SimulationData simData) {
		if (simData==null) return;
		ModelElementAnimationTableDialog.buildPopupMenuItem(owner,menu,getAnimationRunTimeTableData(simData));
	}
}
