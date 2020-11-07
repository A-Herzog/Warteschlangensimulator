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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionEval;
import ui.images.Images;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ScaledImageCache;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Zeigt während der Animation eines von mehreren möglichen Bildern basierend
 * auf verschiedenen Bedingungs-Rechenausdrücken an.
 * @author Alexander Herzog
 */
public class ModelElementAnimationImage extends ModelElementPosition implements ElementWithAnimationDisplay {
	/**
	 * Größe der Markierungsboxen an den Ecken des Elements
	 * @see #drawBorderBox(Graphics2D, Point, double)
	 */
	private static final int SELECT_BOX_SIZE=7;

	/**
	 * Breite der Linie
	 * @see #getBorderWidth()
	 * @see #setBorderWidth(int)
	 */
	private int borderWidth=1;

	/**
	 * Aktuelle Farbe der Linie
	 * @see #getBorderColor()
	 * @see #setBorderColor(Color)
	 */
	private Color borderColor=Color.BLACK;

	/**
	 * Sichert ab, dass Simulations- und Zeichenthread
	 * nicht gleichzeitig auf {@link #displayImage} zugreifen.
	 */
	private Semaphore drawLock=new Semaphore(1);

	/** Bedingungen zur Auswahl der Bilder */
	private final List<String> expression=new ArrayList<>();
	/** Anzuzeigende Bilder */
	private final List<BufferedImage> expressionImage=new ArrayList<>();

	/**
	 * Aktuell anzuzeigendes Bild
	 * @see #drawImage(Graphics2D, Rectangle)
	 */
	private int displayImage=-1;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationImage</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 */
	public ModelElementAnimationImage(final EditModel model, final ModelSurface surface) {
		super(model,surface,new Dimension(50,50),Shapes.ShapeType.SHAPE_NONE);
	}

	@Override
	public Icon buildIcon() {
		return null; /* Die Elemente können zu groß werden, daher hier keine Vorschau. */
	}

	/**
	 * Lädt das Standard-Vorgabe-Bild.
	 * @return	Standard-Vorgabe-Bild
	 */
	public static BufferedImage getDummyImage() {
		final URL url=Images.MODELEDITOR_ELEMENT_ANIMATION_IMAGE_EXAMPLE.getURL();
		if (url==null) {
			return new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);
		} else {
			try {
				return ImageIO.read(url);
			} catch (IOException e) {
				return new BufferedImage(100,100,BufferedImage.TYPE_4BYTE_ABGR);
			}
		}
	}

	/**
	 * Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für das Dropdown-Menü
	 */
	@Override
	public URL getAddElementIcon() {
		return Images.MODELEDITOR_ELEMENT_ANIMATION_IMAGE.getURL();
	}

	/**
	 * Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	@Override
	public String getToolTip() {
		return Language.tr("Surface.AnimationImage.Tooltip");
	}

	/**
	 * Liefert eine Liste der vorhandenen Bilder.<br>
	 * Jeder Bilddatensatz besteht aus 2 Objekten in einem Array: Ausdruck (String, für den letzten Eintrag <code>null</code>), Bild (BufferedImage).
	 * @return	Liste der Diagramm-Einträge
	 */
	public List<Object[]> getExpressionData() {
		final List<Object[]> data=new ArrayList<>();
		for (int i=0;i<expression.size();i++) {
			if (i>=expressionImage.size()) break;

			Object[] row=new Object[2];
			row[0]=expression.get(i);
			row[1]=expressionImage.get(i);

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
		expressionImage.clear();

		for (Object[] row: data) if (row.length==2) {
			if (row[0]!=null && !(row[0] instanceof String)) continue;
			if (!(row[1] instanceof BufferedImage)) continue;
			expression.add((String)row[0]);
			expressionImage.add((BufferedImage)row[1]);
		}
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
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementAnimationImage)) return false;

		if (expression.size()!=((ModelElementAnimationImage)element).expression.size()) return false;
		for (int i=0;i<expression.size();i++) {
			if (expression.get(i)==null) {
				if (((ModelElementAnimationImage)element).expression.get(i)!=null) return false;
				continue;
			}
			if (!expression.get(i).equals(((ModelElementAnimationImage)element).expression.get(i))) return false;
		}

		if (expressionImage.size()!=((ModelElementAnimationImage)element).expressionImage.size()) return false;
		for (int i=0;i<expressionImage.size();i++) {
			final BufferedImage image1=expressionImage.get(i);
			final BufferedImage image2=((ModelElementAnimationImage)element).expressionImage.get(i);
			if (!ScaledImageCache.getHash(image1).equals(ScaledImageCache.getHash(image2))) return false;
		}

		if (borderWidth!=((ModelElementAnimationImage)element).borderWidth) return false;
		if (!((ModelElementAnimationImage)element).borderColor.equals(borderColor)) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementAnimationImage) {

			expression.addAll(((ModelElementAnimationImage)element).expression);
			for (BufferedImage image: ((ModelElementAnimationImage)element).expressionImage) {
				expressionImage.add(ModelAnimationImages.copyImage(image));
			}

			borderWidth=((ModelElementAnimationImage)element).borderWidth;
			borderColor=((ModelElementAnimationImage)element).borderColor;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementAnimationImage clone(final EditModel model, final ModelSurface surface) {
		final ModelElementAnimationImage element=new ModelElementAnimationImage(model,surface);
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
	 * Zeichnet das aktuelle Bild gemäß {@link #displayImage}
	 * @param g	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param rectangle	Zeichenbereich für das Bild
	 */
	private void drawImage(final Graphics2D g, final Rectangle rectangle) {
		if (expression.size()==0) {
			expression.add(null);
			expressionImage.add(getDummyImage());
		}

		drawLock.acquireUninterruptibly();
		try {
			if (expressionImage.size()==0) return;
			final int index=(displayImage>=0)?displayImage:(expressionImage.size()-1);
			if (index>=expressionImage.size()) return;
			final BufferedImage image=expressionImage.get(index);

			final BufferedImage scaledImage=ScaledImageCache.getScaledImageCache().getScaledImage(image,rectangle.width,rectangle.height);
			g.drawImage(scaledImage,rectangle.x,rectangle.y,null);
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

		setClip(g2,drawRect,rectangle);
		drawImage(g2,rectangle);
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
		return Language.tr("Surface.AnimationImage.Name");
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
			new ModelElementAnimationImageDialog(owner,ModelElementAnimationImage.this,readOnly);
		};
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.AnimationImage.XML.Root");
	}

	@Override
	public String loadFromXML(final Element node) {
		expression.clear();
		expressionImage.clear();
		return super.loadFromXML(node);
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

		sub=doc.createElement(Language.trPrimary("Surface.AnimationImage.XML.LineWidth"));
		node.appendChild(sub);
		sub.setTextContent(""+borderWidth);

		sub=doc.createElement(Language.trPrimary("Surface.AnimationImage.XML.LineColor"));
		node.appendChild(sub);
		sub.setTextContent(EditModel.saveColor(borderColor));

		for (int i=0;i<expression.size();i++) {
			sub=doc.createElement(Language.trPrimary("Surface.AnimationImage.XML.Image"));
			node.appendChild(sub);
			if (expression.get(i)!=null) sub.setAttribute(Language.trPrimary("Surface.AnimationImage.XML.Image.Expression"),expression.get(i));
			try {
				final ByteArrayOutputStream stream=new ByteArrayOutputStream();
				ImageIO.write(expressionImage.get(i),"png",stream);
				sub.setTextContent(new String(Base64.getEncoder().encode(stream.toByteArray())));
			} catch (IOException e) {}
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

		if (Language.trAll("Surface.AnimationImage.XML.LineWidth",name)) {
			Integer I;
			I=NumberTools.getNotNegativeInteger(content);
			if (I==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			borderWidth=I;
			return null;
		}

		if (Language.trAll("Surface.AnimationImage.XML.LineColor",name) && !content.trim().isEmpty()) {
			borderColor=EditModel.loadColor(content);
			if (borderColor==null) return String.format(Language.tr("Surface.XML.ElementSubError"),name,node.getParentNode().getNodeName());
			return null;
		}

		if (Language.trAll("Surface.AnimationImage.XML.Image",name)) {
			final String exp=Language.trAllAttribute("Surface.AnimationImage.XML.Image.Expression",node).trim();
			final BufferedImage image;
			try {
				image=ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(content.getBytes())));
			} catch (IOException | IllegalArgumentException e) {return null;}

			if (exp.isEmpty()) {
				if (expression.size()>0 && expression.get(expression.size()-1)==null) {
					expression.set(expression.size()-1,null);
					expressionImage.set(expression.size()-1,image);
				} else {
					expression.add(null);
					expressionImage.add(image);
				}
			} else {
				if (expression.size()==0) {
					expression.add(null);
					expressionImage.add(getDummyImage());
				}
				expression.add(expression.size()-1,exp);
				expressionImage.add(expressionImage.size()-1,image);
			}

			return null;
		}

		return null;
	}

	/**
	 * Formelobjekte zu {@link #expression}
	 * @see #initAnimation(SimulationData)
	 * @see #updateSimulationData(SimulationData, boolean)
	 */
	private ExpressionEval[] animationExpression;

	/**
	 * Wertet {@link #animationExpression} aus und liefert
	 * den zu zeichnenden Wert zurück.
	 * @param simData	Simulationsdatenobjekt
	 * @param index	Index des Bildes für das die Bedingung ausgewertet werden soll
	 * @return	Ist die Bedingung erfüllt?
	 * @see #animationExpression
	 */
	private boolean evalExpression(final SimulationData simData, final int index) {
		final ExpressionEval calc=animationExpression[index];
		if (calc==null) return false;
		simData.runData.setClientVariableValues(null);
		return calc.eval(simData.runData.variableValues,simData,null);
	}

	@Override
	public boolean updateSimulationData(SimulationData simData, boolean isPreview) {
		if (isPreview) return false;

		int index=expression.size()-1;
		for (int i=0;i<animationExpression.length;i++) if (evalExpression(simData,i)) {index=i; break;}

		drawLock.acquireUninterruptibly();
		try {
			displayImage=index;
		} finally {
			drawLock.release();
		}

		return true;
	}

	@Override
	public void initAnimation(SimulationData simData) {
		animationExpression=new ExpressionEval[expression.size()-1];

		for (int i=0;i<expression.size()-1;i++) {
			animationExpression[i]=new ExpressionEval(simData.runModel.variableNames);
			if (animationExpression[i].parse(expression.get(i))>=0) animationExpression[i]=null;
		}
	}

	@Override
	public String getHelpPageName() {
		return "ModelElementAnimationImage";
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLDrawImage(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawAnimationImage(rect,borderColor,borderWidth,image) {\n");

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

		sb.append("  var img=new Image();\n");
		sb.append("  img.src=image;\n");
		sb.append("  img.onload=function(){context.drawImage(img,rect.x,rect.y,rect.w,rect.h);};\n");

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		outputBuilder.addJSUserFunction("drawAnimationImage",builder->getHTMLDrawImage(builder));

		final Point p=getPosition(true);
		final Dimension s=getSize();

		BufferedImage imageObj=null;
		for (int i=0;i<expression.size();i++) if (expression.get(i)==null) {imageObj=expressionImage.get(i); break;}
		if (imageObj==null) imageObj=getDummyImage();

		String image;
		try {
			final ByteArrayOutputStream out=new ByteArrayOutputStream();
			ImageIO.write(imageObj,"png",out);
			final byte[] bytes=out.toByteArray();
			final String base64bytes=Base64.getEncoder().encodeToString(bytes);
			image="data:image/png;base64,"+base64bytes;
		} catch (IOException e) {
			image="";
		}

		outputBuilder.outputBody.append("drawAnimationImage({x: "+p.x+",y: "+p.y+", w:"+s.width+",h: "+s.height+"},\""+HTMLOutputBuilder.colorToHTML(borderColor)+"\","+borderWidth+",\""+image+"\");\n");
	}

	/**
	 * Zeichnet das Element in einem {@link SpecialOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	@Override
	public void specialOutput(final SpecialOutputBuilder outputBuilder) {
		if (outputBuilder instanceof HTMLOutputBuilder) specialOutputHTML((HTMLOutputBuilder)outputBuilder);
	}
}