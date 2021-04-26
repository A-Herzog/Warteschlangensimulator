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
package ui.modeleditor.coreelements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import language.Language;
import mathtools.NumberTools;
import simulator.builder.RunModelCreator;
import simulator.builder.RunModelCreatorStatus;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModelFixer;
import ui.images.Images;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ScaledImageCache;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.fastpaint.IntersectionClipping;
import ui.modeleditor.fastpaint.Shapes;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;

/**
 * Basisklasse für alle Modell-Elemente, die eine feste Position besitzen (Box-artige Elemente, Verbindungsecken, ...)
 * @author Alexander Herzog
 */
public class ModelElementPosition extends ModelElement {
	/**
	 * Position der oberen linken Ecke
	 * @see #getPosition(boolean)
	 */
	private Point position;

	/**
	 * Größe der Box
	 * @see #getSize()
	 */
	private Dimension size;

	/**
	 * Statistikdaten für das Element erfassen?
	 * @see #isStationStatisticsActive()
	 */
	private boolean stationStatisticsActive;

	/**
	 * Helfer-Klasse zur Zeichnung der Form
	 */
	protected Shapes shape;

	/**
	 * Form {@link #shape} gespiegelt zeichnen?
	 * @see #isFlipable()
	 * @see #isFlipShape()
	 * @see #setFlipShape(boolean)
	 */
	private boolean flipShape;

	/**
	 * Soll die Größe bei Vergleichen mit herangezogen werden?
	 */
	protected boolean useSizeOnCompare=true;

	/**
	 * Konstruktor der Klasse <code>ModelElementPosition</code>
	 * @param model	Modell zu dem dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu der dieses Element gehören soll (kann später nicht mehr geändert werden)
	 * @param size	Größe der Box
	 * @param shape	Darzustellende Form ({@link Shapes})
	 */
	public ModelElementPosition(final EditModel model, final ModelSurface surface, final Dimension size, final Shapes.ShapeType shape) {
		super(model,surface);
		this.size=new Dimension(size);
		this.shape=new Shapes(shape);
		flipShape=false;
		setPosition(new Point(10,10));
		stationStatisticsActive=true;
	}

	/**
	 * Optionales Icon, welches im "Element hinzufügen"-Dropdown-Menü angezeigt werden soll.
	 * @return	Icon für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	public Icon getAddElementIcon() {
		return null;
	}

	/**
	 * Optionaler Tooltip für den "Element hinzufügen"-Dropdown-Menü-Eintrag.
	 * @return Tooltip für den "Element hinzufügen"-Dropdown-Menüeintrag
	 */
	public String getToolTip() {
		return null;
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmenü angezeigt werden soll.<br>
	 * Generiert im Falle eines Elements mit eigener Position (= einem normal darstellbaren Element) ein Icon basierend auf der Darstellung des Elements selber.
	 * @return	Icon zur Beschriftung des Elements im Kontextmenü oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	@Override
	public Icon buildIcon() {
		final BufferedImage image=new BufferedImage(size.width+15,size.height+15,BufferedImage.TYPE_4BYTE_ABGR);

		final Point savePosition=position;
		try {
			position=new Point(5,5);
			drawToGraphics(image.getGraphics(),new Rectangle(0,0,size.width+15,size.height+15),1.0,false);
		} finally {
			position=savePosition;
		}

		return getScaledElementIcon(image);
	}

	/**
	 * Skaliert ein in {@link ModelElementPosition#buildIcon()} generiertes BufferedImage
	 * auf die maximal zulässige Größe (oder belässt es, wie es ist) und erstellt ein Icon daraus.
	 * @param image	Bild, welches notwendigenfalls skaliert werden soll und dann in ein Icon verpackt werden soll
	 * @return	Icon zur Anzeige im Kontextmenü
	 */
	protected final Icon getScaledElementIcon(final BufferedImage image) {
		if (image.getWidth()>150 || image.getHeight()>150) {
			final int width=Math.min(150,image.getWidth()*150/image.getHeight());
			final int height=image.getHeight()*width/image.getWidth();
			final BufferedImage scaled=new BufferedImage(width,height,BufferedImage.TYPE_4BYTE_ABGR);
			final Graphics2D g=scaled.createGraphics();
			final AffineTransform at=AffineTransform.getScaleInstance(((double)width)/image.getWidth(),((double)height)/image.getHeight());
			g.drawRenderedImage(image,at);
			return new ImageIcon(scaled);
		}

		return new ImageIcon(image);
	}

	/**
	 * Prüft, ob sich ein Gebiet und das Elements überschneiden
	 * @param selectedArea	Gebiet, bei dem geprüft werden soll, ob es sich mit dem Element überschneidet
	 * @return	Gibt <code>true</code> zurück, wenn sich das Gebiet und das Element überschneiden
	 */
	protected boolean containedInArea(final Rectangle selectedArea) {
		/*
		final Point p1=position;
		final Point p2=new Point(position.x+size.width,position.y);
		final Point p3=new Point(position.x,position.y+size.height);
		final Point p4=new Point(position.x+size.width,position.y+size.height);
		return selectedArea.contains(p1) || selectedArea.contains(p2) || selectedArea.contains(p3) || selectedArea.contains(p4);
		 */
		return selectedArea.contains(new Rectangle(position,size));
	}

	/**
	 * Stellt ein, ob das Element momentan als bereich-selektiert dargestellt werden soll.
	 * @param selectedArea	Bereich, der ausgewählt werden soll (oder <code>null</code>, wenn nichts ausgewählt werden soll)
	 */
	@Override
	public final void setSelectedArea(final Rectangle selectedArea) {
		if (selectedArea==null) {setSelectedArea(false); return;}
		setSelectedArea(containedInArea(selectedArea));
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(final ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementPosition)) return false;
		final ModelElementPosition otherElement=(ModelElementPosition)element;

		if (!otherElement.position.equals(position)) return false;
		if (useSizeOnCompare && !otherElement.size.equals(size)) return false;

		if (flipShape!=otherElement.flipShape) return false;
		if (!ScaledImageCache.compare(shape.getCustomImage(),otherElement.shape.getCustomImage())) return false;

		if (stationStatisticsActive!=otherElement.stationStatisticsActive) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(final ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementPosition) {
			final ModelElementPosition copySource=(ModelElementPosition)element;

			position=new Point(copySource.position);
			size=new Dimension(copySource.size);
			flipShape=copySource.flipShape;
			shape.setCustomImage(copySource.shape.getCustomImage()); /* Wir verwenden dasselbe Bildobjekt; bei Änderungen wird in shape ein neues Objekt hinterlegt. */
			stationStatisticsActive=copySource.stationStatisticsActive;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElementPosition clone(final EditModel model, final ModelSurface surface) {
		final ModelElementPosition element=new ModelElementPosition(model,surface,size,shape.shapeType);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Liefert die Größe der umrandenden Box
	 * @return	Größe der Box
	 */
	public Dimension getSize() {
		return size;
	}

	/**
	 * Stellt die Größe der umrandenden Box ein
	 * @param size	Größe der Box
	 */
	protected void setSize(final Dimension size) {
		this.size=new Dimension(size);
		fireChanged();
	}

	/**
	 * x-Position beim letzten Aufruf von {@link #getRect(double)}
	 * um ggf. {@link #lastPositionRectangle} wiederverwenden zu können
	 * @see #lastPositionRectangle
	 * @see #getRect(double)
	 */
	private int lastPositionRectanglePositionX=-1;

	/**
	 * y-Position beim letzten Aufruf von {@link #getRect(double)}
	 * um ggf. {@link #lastPositionRectangle} wiederverwenden zu können
	 * @see #lastPositionRectangle
	 * @see #getRect(double)
	 */
	private int lastPositionRectanglePositionY=-1;

	/**
	 * Breite des Rechtecks beim letzten Aufruf von {@link #getRect(double)}
	 * um ggf. {@link #lastPositionRectangle} wiederverwenden zu können
	 * @see #lastPositionRectangle
	 * @see #getRect(double)
	 */
	private int lastPositionRectangleSizeW=-1;

	/**
	 * Höhe des Rechtecks beim letzten Aufruf von {@link #getRect(double)}
	 * um ggf. {@link #lastPositionRectangle} wiederverwenden zu können
	 * @see #lastPositionRectangle
	 * @see #getRect(double)
	 */
	private int lastPositionRectangleSizeH=-1;

	/**
	 * Zoomfaktor beim letzten Aufruf von {@link #getRect(double)}
	 * um ggf. {@link #lastPositionRectangle} wiederverwenden zu können
	 * @see #lastPositionRectangle
	 * @see #getRect(double)
	 */
	private double lastPositionRectangleZoom=-1;

	/**
	 * Rückgabewert des letzten Aufrufs von {@link #getRect(double)}
	 * @see #getRect(double)
	 */
	private Rectangle lastPositionRectangle;

	/**
	 * Berechnet die Größe der Ecke in Pixeln basierend auf dem Zoomfaktor
	 * @param zoom	Zoomfaktor
	 * @return	Größe des Rechtecks, das das Element umschließt, angepasst an den Zoomfaktor
	 */
	protected final Rectangle getRect(final double zoom) {
		if (lastPositionRectangle==null || lastPositionRectanglePositionX!=position.x || lastPositionRectanglePositionY!=position.y || lastPositionRectangleSizeW!=size.width || lastPositionRectangleSizeH!=size.height || lastPositionRectangleZoom!=zoom) {
			lastPositionRectangle=new Rectangle((int)FastMath.round((position.x+FastMath.min(0,size.width))*zoom),(int)FastMath.round((position.y+FastMath.min(0,size.height))*zoom),(int)FastMath.round(Math.abs(size.width)*zoom),(int)FastMath.round(Math.abs(size.height)*zoom));
			lastPositionRectanglePositionX=position.x;
			lastPositionRectanglePositionY=position.y;
			lastPositionRectangleSizeW=size.width;
			lastPositionRectangleSizeH=size.height;
			lastPositionRectangleZoom=zoom;

		}
		return lastPositionRectangle;
	}

	/**
	 * Stellt den Zeichenbereich für ein Zeichenobjekt ein
	 * @param graphics	<code>Graphics</code>-Objekt, in das gezeichnet werden soll
	 * @param drawRect	Gültiger Zeichenbereich des übergeordneten <code>JViewPort</code>-Elements
	 * @param objectRect	Optionaler Zeichenbereich (oder <code>null</code>) auf den die Zeichenfläche eingeschränkt werden soll.
	 */
	protected final void setClip(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect) {
		final IntersectionClipping clipping=shape.getClip();
		clipping.set(graphics,drawRect,objectRect);
	}

	/**
	 * Zeichnet die eigentliche Form des Elements
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Gesamter für Zeichnungen zur Verfügung stehender Bereich
	 * @param objectRect	Gemäß Zoomfaktor skalierter Rahmen des Elements
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite (in Pixeln)
	 * @param fillColor	Füllfarbe (oder <code>null</code>, wenn die Box transparent sein soll)
	 * @param zoom	Aktueller Zoomfaktor
	 * @param stage	Gibt an, ob nur der Hintergrund (stage=1) oder nur der Rahmen (stage=2) gezeichnet werden soll
	 */
	protected void drawElementShape(final Graphics graphics, final Rectangle drawRect, final Rectangle objectRect, final Color borderColor, final int borderWidth, final Color fillColor, final double zoom, final int stage) {
		shape.draw(graphics,drawRect,objectRect,borderColor,borderWidth,fillColor,zoom,flipShape,stage);
	}

	/**
	 * Zeichnet das Element inkl. optionalem Markierungsrahmen
	 * @param graphics	<code>Graphics</code>-Objekt in das das Element eingezeichnet werden soll
	 * @param drawRect	Tatsächlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite (in Pixeln)
	 * @param fillColor	Füllfarbe (oder <code>null</code>, wenn die Box transparent sein soll)
	 * @param stage	Gibt an, ob nur der Hintergrund (stage=1) oder nur der Rahmen (stage=2) gezeichnet werden soll
	 * @return	Liefert das Rechteck der Box, sofern dieses zumindest teilweise sichtbar ist, oder <code>null</code> wenn die komplette Box außerhalb des Sichtbereichs liegt und daher nicht gezeichnet werden muss.
	 * @see #drawElementShape(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)
	 */
	protected Rectangle drawRect(final Graphics graphics, final Rectangle drawRect, final double zoom, final Color borderColor, final int borderWidth, final Color fillColor, final int stage) {
		final Rectangle objectRect=getRect(zoom);

		boolean ok=false;
		ok=ok || drawRect.contains(objectRect.x,objectRect.y);
		ok=ok || drawRect.contains(objectRect.x+objectRect.width,objectRect.y);
		ok=ok || drawRect.contains(objectRect.x,objectRect.y+objectRect.height);
		ok=ok || drawRect.contains(objectRect.x+objectRect.width,objectRect.y+objectRect.height);
		if (!ok) return null;

		final Shape saveClip=graphics.getClip();
		try {
			setClip(graphics,drawRect,objectRect);
			final int w=(borderWidth==0)?0:(int)(Math.max(1,Math.round(borderWidth*zoom)));
			drawElementShape(graphics,drawRect,objectRect,borderColor,w,fillColor,zoom,stage);
		} finally {
			graphics.setClip(saveClip);
		}

		return objectRect;
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		return getRect(zoom).contains(point);
	}

	/**
	 * Gibt an, ob das Element per Drag&amp;Drop verschoben werden kann.<br>
	 * (Im Falle von <code>ModelElementBox</code>-Elementen immer <code>true</code>.)
	 * @return	Liefert <code>true</code>, wenn das Element vom Nutzer verschoben werden kann.
	 */
	@Override
	public final boolean isUserMoveable() {
		return true;
	}

	/**
	 * Wird in {@link #getPosition(boolean)} für die
	 * Read-only-Version des Punktes verwendet.
	 * @see #getPosition(boolean)
	 */
	private Point pointCache;

	/**
	 * Liefert die obere linke Ecke des Elements.
	 * @param readOnly	Gibt an, ob der Punkt selbst (<code>true</code>), der dann nicht verändert werden darf, oder eine Kopie (<code>false</code>) geliefert werden soll
	 * @return	Position der oberen linken Ecke
	 */
	@Override
	public final Point getPosition(final boolean readOnly) {
		if (!readOnly) return new Point(position);

		if (pointCache==null) pointCache=new Point();
		pointCache.setLocation(position);
		return pointCache;
	}

	/**
	 * Hält das {@link Point}-Objekt für die Mitte des Elements
	 * in der Nur-Lese-Fassung vor.
	 * @see #getMiddlePosition(boolean)
	 */
	private Point readOnlyMiddle=null;

	/**
	 * Mittelpunkt des Elements (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @param readOnly	Gibt an, ob der Punkt selbst (<code>true</code>), der dann nicht verändert werden darf, oder eine Kopie (<code>false</code>) geliefert werden soll
	 * @return	Mittelpunkt des Elements oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen können.
	 */
	@Override
	public final Point getMiddlePosition(final boolean readOnly) {
		final int x=position.x+size.width/2;
		final int y=position.y+size.height/2;
		if (readOnly) {
			if (readOnlyMiddle==null) return readOnlyMiddle=new Point(x,y);
			readOnlyMiddle.x=x;
			readOnlyMiddle.y=y;
			return readOnlyMiddle;
		} else {
			return new Point(x,y);
		}
	}

	/**
	 * Unterer rechter Rand des Elements (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @return	Unterer rechter Rand des Elements oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen können.
	 */
	@Override
	public Point getLowerRightPosition() {
		return new Point(position.x+size.width,position.y+size.height);
	}

	/**
	 * Verbindungspunkt zu anderem Objekt (sofern dieses Modell-Element eine definierte Position besitzt).
	 * @param point	Punkt zu dem die Verbindung erfolgen soll
	 * @return	Position, an der Verknüpfungen zu dem angegebenen Punkt ansetzen können oder <code>null</code>, wenn an das Element keine anderen Elemente ansetzen können.
	 */
	@Override
	public final Point getConnectionToPosition(final Point point) {
		int xStart=position.x-1;
		int yStart=position.y-1;
		int xMiddle=position.x+size.width/2;
		int yMiddle=position.y+size.height/2;
		int xEnd=position.x+size.width+1;
		int yEnd=position.y+size.height+1;

		if (yMiddle>point.y) {
			/* Ziel liegt über dem Element */
			if (point.x-xMiddle<point.y-yMiddle) return new Point(xStart,yMiddle);
			if (point.x-xMiddle>yMiddle-point.y) return new Point(xEnd,yMiddle);
			return new Point(xMiddle,yStart);
		} else {
			/* Ziel liegt unter dem Element */
			if (point.x-xMiddle>point.y-yMiddle) return new Point(xEnd,yMiddle);
			if (point.x-xMiddle<yMiddle-point.y) return new Point(xStart,yMiddle);

			return new Point(xMiddle,yEnd);
		}
	}

	/**
	 * Stellt (wenn möglich) die Position des Elements ein.
	 * @param point	Position der oberen linken Ecke. Kann von einigen Element-Typen ignoriert werden.
	 */
	@Override
	public final void setPosition(final Point point) {
		position=new Point(point);
		fireChanged();
	}

	/**
	 * Gespeicherte x-Position, um später zu dieser zurück zu kehren.
	 * @see #temporaryMoveToTop()
	 * @see #temporaryMoveRestore()
	 */
	private int saveX;

	/**
	 * Gespeicherte y-Position, um später zu dieser zurück zu kehren.
	 * @see #temporaryMoveToTop()
	 * @see #temporaryMoveRestore()
	 */
	private int saveY;

	/**
	 * Verschiebt das Element temporär an die Position (5,5) ohne dabei ein {@link ModelElementPosition#fireChanged()}
	 * auszulösen. Die bisherige Position wird gespeichert und kann per {@link ModelElementPosition#temporaryMoveRestore()}
	 * wiederhergestellt werden.
	 */
	public final void temporaryMoveToTop() {
		saveX=position.x;
		saveY=position.y;
		position.x=5;
		position.y=5;
	}

	/**
	 * Stellt die ursprüngliche Position, die in {@link ModelElementPosition#temporaryMoveToTop()}
	 * gespeichert wurde, wieder her. Auch hier wird kein {@link ModelElementPosition#fireChanged()} ausgelöst.
	 */
	public final void temporaryMoveRestore() {
		position.x=saveX;
		position.y=saveY;
	}

	/**
	 * Fügt in eine Textzeile Zeilenumbruchzeichen nach jeweils eine bestimmten Zeichenanzahl (aufgerundet zur nächsten Wortgrenze) ein.
	 * @param line	Zeile, in die Zeilenumbruchzeichen eingefügt werden sollen
	 * @param maxLength	Anzahl an Zeichen pro Zeile
	 * @return	Zeile mit Umbruchmarkierungen nach der angegebenen Anzahl an Zeichen (aufgerundet zur nächsten Wortgrenze)
	 */
	protected final String splitLine(final String line, final int maxLength) {
		final String[] words=line.trim().split("\\s+");
		final StringBuilder sb=new StringBuilder();
		int count=0;
		for (String word: words) {
			if (sb.length()>0) {
				if (count>maxLength) {
					sb.append("\n"); count=0;
				} else {
					sb.append(" ");
				}
			}
			sb.append(word);
			count+=word.length();
		}
		return sb.toString();
	}

	/**
	 * Generiert einen Menüpunkt für einen QuickFix-Eintrag
	 * @param status	QuickFix-Datensatz
	 * @return	Neuer Menüpunkt
	 */
	private JMenuItem getQuickFixInfo(final RunModelCreatorStatus status) {
		final JMenuItem item=new JMenuItem("<html><body><b>"+splitLine(status.message,40).replace("\n","<br>")+"</b></body></html>");
		item.setEnabled(false);
		return item;
	}

	/**
	 * Generiert einen Menüpunkt für einen QuickFix-Eintrag
	 * @param fix	QuickFix-Datensatz
	 * @return	Neuer Menüpunkt
	 */
	private JMenuItem getQuickFixItem(final RunModelFixer fix) {
		final JMenuItem item=new JMenuItem(fix.info);
		item.addActionListener(e->{
			fix.process();
			fireChanged();
		});
		return item;
	}

	/**
	 * Fügt mehrere QuickFix-Menüpunkte zu einem Menü hinzu
	 * @param menu	Menü
	 * @param fixer	Liste der QuickFix-Einträge
	 */
	private void buildQuickFixMenu(final JMenu menu, final List<RunModelFixer> fixer) {
		menu.add(getQuickFixInfo(fixer.get(0).status));
		menu.addSeparator();
		for (RunModelFixer fix: fixer) menu.add(getQuickFixItem(fix));
	}

	/**
	 * Fügt mehrere QuickFix-Menüpunkte zu einem Menü hinzu
	 * @param menu	Menü
	 * @param fixer	Liste der QuickFix-Einträge
	 */
	private void buildQuickFixMenu(final JPopupMenu menu, List<RunModelFixer> fixer) {
		menu.add(getQuickFixInfo(fixer.get(0).status));
		menu.addSeparator();
		for (RunModelFixer fix: fixer) menu.add(getQuickFixItem(fix));
	}

	/**
	 * Maximalanzahl an QuickFix-Einträgen zu Folgestationen
	 * @see #findEdgesTo(Class[], List)
	 */
	private static final int MAX_EDGE_FIX_OPTIONS=10;

	/**
	 * Sucht mögliche Folgeelemente und bietet als Korrekturen das Hinzufügen von Kanten zu diesen Elementen an.
	 * @param types	Typen der Elemente zu denen das hinzufügen von Kanten angeboten werden soll
	 * @param fixer	Liste der Korrektoren die erweitert werden soll
	 * @see #addEdgeOutFixes(List)
	 */
	protected final void findEdgesTo(Class<?>[] types, final List<RunModelFixer> fixer) {
		if (types==null || types.length==0) return;
		int count=0;
		for (ModelElement element: surface.getElements()) {
			for (Class<?> cls: types) {
				if (cls.isInstance(element)) {
					final String elementName;
					if (element.getName().isEmpty()) {
						elementName=String.format(Language.tr("Surface.PopupMenu.QuickFix.AddEdgeTo.ElementNoName"),((ModelElementBox)element).getTypeName(),element.getId());
					} else {
						elementName=String.format(Language.tr("Surface.PopupMenu.QuickFix.AddEdgeTo.ElementName"),((ModelElementBox)element).getTypeName(),element.getName(),element.getId());
					}
					final ModelElementPosition connectionEnd=(ModelElementPosition)element;
					final RunModelFixer fix=new RunModelFixer(this,RunModelCreatorStatus.noEdgeOut(this),String.format(Language.tr("Surface.PopupMenu.QuickFix.AddEdgeTo"),elementName),f->{
						final ModelSurface surface=f.element.getSurface();
						final ModelElementEdge edge=new ModelElementEdge(f.model,surface,f.element,connectionEnd);
						f.element.addEdgeOut(edge);
						connectionEnd.addEdgeIn(edge);
						surface.add(edge);
					});
					fixer.add(fix);
					count++;
					if (count>=MAX_EDGE_FIX_OPTIONS) return;
				}
			}
		}
	}

	/**
	 * Fügt optionale Korrekturen zum Hinzufügen von (bislang fehlenden) Auslaufenden Kanten hinzu
	 * @param fixer	Erweiterbare Liste der möglichen Korrekturen
	 * @see #findEdgesTo(Class[], List)
	 */
	protected void addEdgeOutFixes(final List<RunModelFixer> fixer) {
	}

	/**
	 * Fügt ein QuickFix-Untermenü oder QuickFix-Menüpunkte selbst zu einem Menü hinzu
	 * @param menu	Menü zu dem das Untermenü oder die Menüpunkte hinzugefügt werden sollen
	 * @param asSubMenu	Nur Menüpunkte (<code>false</code>) oder ein Menüpunkt, der dann die Punkte als Untermenü enthält, (<code>true</code>)
	 */
	@Override
	protected void addQuickFix(final JPopupMenu menu, final boolean asSubMenu) {
		final RunModelCreatorStatus status=RunModelCreator.testElementStatus(this);
		if (status!=null && !status.isOk()) {
			final List<RunModelFixer> fixer=status.getFix(this);
			if (status.status==RunModelCreatorStatus.Status.NO_EDGE_OUT) addEdgeOutFixes(fixer);
			if (fixer!=null && fixer.size()>0) {
				if (asSubMenu) {
					final JMenu sub=new JMenu(Language.tr("Surface.PopupMenu.QuickFix"));
					sub.setIcon(Images.GENERAL_WARNING_BUG.getIcon());
					menu.add(sub);
					buildQuickFixMenu(sub,fixer);
				} else {
					buildQuickFixMenu(menu,fixer);
				}
			}
		}
	}

	/**
	 * Existieren QuickFix-Korrekturen für das aktuelle Element?
	 * @return	QuickFix-Korrekturen vorhanden?
	 * @see #getQuickFixPopupMenu()
	 */
	public boolean hasQuickFix() {
		final RunModelCreatorStatus status=RunModelCreator.testElementStatus(this);
		if (status==null || status.isOk()) return false;

		final List<RunModelFixer> fixer;
		if (status.status==RunModelCreatorStatus.Status.NO_EDGE_OUT) {
			fixer=new ArrayList<>();
			addEdgeOutFixes(fixer);
		} else {
			fixer=status.getFix(this);
		}
		return fixer.size()>0;
	}

	/**
	 * Liefert ein Popupmenü mit QuickFix-Korrekturvorschlägen
	 * @return	Popupmenü oder <code>null</code>, wenn das Element in Ordnung ist oder zumindest keine QuickFix-Korrekturen vorhanden sind
	 * @see #hasQuickFix()
	 */
	public JPopupMenu getQuickFixPopupMenu() {
		final JPopupMenu popupMenu=new JPopupMenu();
		addQuickFix(popupMenu,false);
		if (popupMenu.getComponentCount()==0) return null;
		return popupMenu;
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);

		if (!stationStatisticsActive) node.setAttribute(Language.trPrimary("Surface.XML.Element.StationStatisticsActive"),"0");
		if (flipShape) node.setAttribute(Language.trPrimary("Surface.XML.Element.FlipShape"),"1");

		final Element sub=doc.createElement(Language.trPrimary("Surface.XML.Element.Size"));
		node.appendChild(sub);
		sub.setAttribute("x",""+position.x);
		sub.setAttribute("y",""+position.y);
		sub.setAttribute("w",""+size.width);
		sub.setAttribute("h",""+size.height);
	}

	/**
	 * Ermöglicht das Laden von Eigenschaften aus dem Haupt-Knoten des xml-Elements.
	 * @param node	xml-Element deren Eigenschaften verarbeitet werden sollen
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zurückgegeben. Im Erfolgsfall wird <code>null</code> zurückgegeben.
	 */
	@Override
	protected String loadPropertiesFromMainNode(final Element node) {
		String value;

		value=Language.trAllAttribute("Surface.XML.Element.StationStatisticsActive",node);
		if (value.equals("0")) stationStatisticsActive=false;

		value=Language.trAllAttribute("Surface.XML.Element.FlipShape",node);
		if (value.equals("1")) flipShape=true;

		return null;
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

		if (Language.trAll("Surface.XML.Element.Size",name)) {
			Integer I;
			I=NumberTools.getInteger(node.getAttribute("x"));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),"x",node.getNodeName(),node.getParentNode().getNodeName());
			position.x=I;
			I=NumberTools.getInteger(node.getAttribute("y"));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),"y",node.getNodeName(),node.getParentNode().getNodeName());
			position.y=I;
			I=NumberTools.getInteger(node.getAttribute("w"));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),"w",node.getNodeName(),node.getParentNode().getNodeName());
			size.width=I;
			I=NumberTools.getInteger(node.getAttribute("h"));
			if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),"h",node.getNodeName(),node.getParentNode().getNodeName());
			size.height=I;
			return null;
		}

		return null;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) einlaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) einlaufende Kante angenommen werden kann.
	 */
	public boolean canAddEdgeIn() {
		return false;
	}

	/**
	 * Gibt an, ob das Element momentan eine (weitere) auslaufende Kante annehmen kann.
	 * @return	Gibt <code>true</code> zurück, wenn eine (weitere) auslaufende Kante angenommen werden kann.
	 */
	public boolean canAddEdgeOut() {
		return false;
	}

	/**
	 * Fügt eine einlaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die einlaufende Kante hinzugefügt werden konnte.
	 */
	public boolean addEdgeIn(ModelElementEdge edge) {
		return false;
	}

	/**
	 * Fügt eine auslaufende Kante hinzu.
	 * @param edge	Hinzuzufügende Kante
	 * @return	Gibt <code>true</code> zurück, wenn die auslaufende Kante hinzugefügt werden konnte.
	 */
	public boolean addEdgeOut(ModelElementEdge edge) {
		return false;
	}

	/**
	 * Zeichnet die Form selbst. Vergleichbar mit der {@link ModelElementPosition#drawElementShape(Graphics, Rectangle, Rectangle, Color, int, Color, double, int)}-Funktion.
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 * @param borderColor	Rahmenfarbe
	 * @param borderWidth	Rahmenbreite (in Pixeln)
	 * @param fillColor	Füllfarbe (oder <code>null</code>, wenn die Box transparent sein soll)
	 */
	protected void specialOutputShape(final SpecialOutputBuilder outputBuilder, final Color borderColor, final int borderWidth, final Color fillColor) {
		shape.specialOutput(outputBuilder,getRect(1.0),borderColor,borderWidth,fillColor,flipShape);
	}

	/**
	 * Liefert die Form des Elements
	 * @return	Form des Elements
	 * @see Shapes
	 */
	public Shapes getShape() {
		return shape;
	}

	/**
	 * Kann die Form des Elements gespiegelt werden?
	 * @return	Liefert <code>true</code>, wenn die Form des Elements gespiegelt werden kann
	 * @see #isFlipShape()
	 * @see #setFlipShape(boolean)
	 */
	public boolean isFlipable() {
		return shape.shapeType.flippedName!=null;
	}

	/**
	 * Soll die Form gespiegelt gezeichnet werden?
	 * @return	Liefert <code>true</code>, wenn die Form gespiegelt gezeichnet werden soll
	 * @see #setFlipShape(boolean)
	 */
	public boolean isFlipShape() {
		return flipShape;
	}

	/**
	 * Soll die Form gespiegelt gezeichnet werden?
	 * @param flipShape	Soll die Form gespiegelt gezeichnet werden?
	 * @see #isFlipShape()
	 */
	public void setFlipShape(final boolean flipShape) {
		if (this.flipShape==flipShape) return;
		this.flipShape=flipShape;
		fireChanged();
	}

	/**
	 * Sollen die Statistikdaten für die Station erfasst werden?
	 * @return	Liefert <code>true</code>, wenn Statistikdaten erfasst werden sollen
	 * @see #setStationStatisticsActive(boolean)
	 */
	public boolean isStationStatisticsActive() {
		return stationStatisticsActive;
	}

	/**
	 * Sollen die Statistikdaten für die Station erfasst werden?
	 * @param active	Statistikdaten für die Station erfassen
	 * @see #isStationStatisticsActive()
	 */
	public void setStationStatisticsActive(final boolean active) {
		stationStatisticsActive=active;
	}
}
