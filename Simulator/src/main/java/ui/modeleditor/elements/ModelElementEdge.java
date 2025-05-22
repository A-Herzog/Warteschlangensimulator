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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.editmodel.FullTextSearch;
import systemtools.SmallColorChooser;
import ui.images.Images;
import ui.modeleditor.ModelElementCatalog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementEdgeMultiIn;
import ui.modeleditor.coreelements.ModelElementEdgeMultiOut;
import ui.modeleditor.coreelements.ModelElementEdgeOut;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modeleditor.outputbuilder.SpecialOutputBuilder;
import ui.tools.FlatLaFHelper;

/**
 * Verbindungskante zwischen zwei Modell-Elementen
 * @author Alexander Herzog
 */
public final class ModelElementEdge extends ModelElement {
	/** Element im Katalog registrieren **/
	static {
		ModelElementCatalog.getCatalog().addElement(new ModelElementEdge(null,null,null,null));
	}

	/**
	 * Größe des Pfeils
	 */
	private static final int ARROW_SIZE=10;

	/**
	 * Krümmungsradius bei abgerundeten abgewinkelten Linien
	 */
	private static final int ARC_RADIUS=10;

	/**
	 * Art der Verknüpfungslinie
	 * @see ModelElementEdge#getLineMode()
	 * @see ModelElementEdge#setLineMode(LineMode)
	 */
	public enum LineMode {
		/** Gerade Verbindung von Start zu Ziel */
		DIRECT,
		/** Abgewinkelte Linie */
		MULTI_LINE,
		/** Abgewinkelte Linie mit abgerundeten Ecken */
		MULTI_LINE_ROUNDED,
		/** Kubische Kurve */
		CUBIC_CURVE,
	}

	/**
	 * Größe des Pfeils am Ende der Verknüpfungslinie
	 * @see ModelElementEdge#getArrowMode()
	 * @see ModelElementEdge#setArrowMode(ArrowMode)
	 */
	public enum ArrowMode {
		/** Keinen Pfeil zeichnen */
		OFF,
		/** Kleiner Pfeil */
		SMALL,
		/** Mittlerer Pfeil (Vorgabewert) */
		MEDIUM,
		/** Größer Pfeil */
		LARGE,
	}

	/**
	 * Art der Verknüpfungslinien
	 * @see EditModel#edgeLineMode
	 */
	private LineMode lineMode=null;

	/**
	 * Größe des Pfeils am Ende der Verknüpfungslinie
	 * @see EditModel#edgeArrowMode
	 */
	private ArrowMode arrowMode=null;

	/**
	 * Beschriftung (wenn vorhanden) an Kante anzeigen?
	 */
	private boolean drawName=true;

	/**
	 * Element von dem die Kante ausgeht
	 */
	private ModelElement connectionStart;

	/**
	 * Element zu dem die Kante führt
	 */
	private ModelElement connectionEnd;

	/**
	 * ID des Elements von dem die Kante ausgeht (wird nur beim Laden und Clonen verwendet, ist sonst -1)
	 * @see #connectionStart
	 */
	private int connectionStartId=-1;

	/**
	 * ID des Elements zu dem die Kante führt (wird nur beim Laden und Clonen verwendet, ist sonst -1)
	 * @see #connectionEnd
	 */
	private int connectionEndId=-1;

	/**
	 * Konstruktor der Klasse <code>ModelElementEdge</code>
	 * @param model	Modell zu dem diese Element gehören soll (kann später nicht mehr geändert werden)
	 * @param surface	Zeichenfläche zu der diese Element gehören soll (kann später nicht mehr geändert werden)
	 * @param connectionStart	Startpunkt der herzustellenden Verbindung (kann später nicht mehr geändert werden)
	 * @param connectionEnd	Endpunkt der herzustellenden Verbindung (kann später nicht mehr geändert werden)
	 */
	public ModelElementEdge(final EditModel model, final ModelSurface surface, final ModelElement connectionStart, final ModelElement connectionEnd) {
		super(model,surface);
		this.connectionStart=connectionStart;
		this.connectionEnd=connectionEnd;
	}

	/**
	 * Gibt für die Klasse an, ob der Name bei Vergleichen mit einbezogen werden soll.<br>
	 * (Für normale Elemente sollte hier <code>true</code> zurückgegeben werden. Nur für Kanten usw. ist <code>false</code> sinnvoll.)
	 * @return	Gibt an, ob der Name des Elements bei Vergleichen mit einbezogen werden soll.
	 */
	@Override
	protected boolean getEqualsIncludesName() {
		return false;
	}

	/**
	 * Überprüft, ob das Element mit dem angegebenen Element inhaltlich identisch ist.
	 * @param element	Element mit dem dieses Element verglichen werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn die beiden Elemente identisch sind.
	 */
	@Override
	public boolean equalsModelElement(ModelElement element) {
		if (!super.equalsModelElement(element)) return false;
		if (!(element instanceof ModelElementEdge)) return false;
		final ModelElementEdge otherEdge=(ModelElementEdge)element;

		if (connectionStart==null || otherEdge.connectionStart==null) return false;
		if (connectionEnd==null || otherEdge.connectionEnd==null) return false;
		if (connectionStart.getId()!=otherEdge.connectionStart.getId()) return false;
		if (connectionEnd.getId()!=otherEdge.connectionEnd.getId()) return false;
		if (lineMode!=otherEdge.lineMode) return false;
		if (arrowMode!=otherEdge.arrowMode) return false;
		if (!Objects.equals(lineColor,otherEdge.lineColor)) return false;
		if (drawName!=otherEdge.drawName) return false;

		return true;
	}

	/**
	 * Überträgt die Einstellungen von dem angegebenen Element auf dieses.
	 * @param element	Element, von dem alle Einstellungen übernommen werden sollen
	 */
	@Override
	public void copyDataFrom(ModelElement element) {
		super.copyDataFrom(element);
		if (element instanceof ModelElementEdge) {
			final ModelElementEdge otherEdge=(ModelElementEdge)element;
			if (otherEdge.connectionStart!=null) connectionStartId=((ModelElementEdge)element).connectionStart.getId();
			if (otherEdge.connectionEnd!=null) connectionEndId=((ModelElementEdge)element).connectionEnd.getId();
			lineMode=otherEdge.lineMode;
			arrowMode=otherEdge.arrowMode;
			setLineColor(otherEdge.lineColor);
			drawName=otherEdge.drawName;
		}
	}

	/**
	 * Erstellt eine Kopie des Elements
	 * @param model	Modell zu dem das kopierte Element gehören soll.
	 * @param surface	Zeichenfläche zu der das kopierte Element gehören soll.
	 * @return	Kopiertes Element
	 */
	@Override
	public ModelElement clone(final EditModel model, final ModelSurface surface) {
		ModelElement element=new ModelElementEdge(model,surface,null,null);
		element.copyDataFrom(this);
		return element;
	}

	/**
	 * Optionale Initialisierungen nach dem Laden bzw. Clonen.
	 */
	@Override
	public void initAfterLoadOrClone() {
		super.initAfterLoadOrClone();

		if (connectionStartId>=0) {
			connectionStart=surface.getById(connectionStartId);
			connectionStartId=-1;
		}
		if (connectionEndId>=0) {
			connectionEnd=surface.getById(connectionEndId);
			connectionEndId=-1;
		}
		if ((connectionStart==null || connectionEnd==null) && surface!=null) surface.remove(this);

		setLineColor(lineColor);
	}

	/**
	 * Liefert die Art der Verknüpfungslinie.
	 * @return	Art der Verknüpfungslinie (<code>null</code> bedeutet "Vorgabe Stil aus Modell verwenden)
	 * @see EditModel#edgeLineMode
	 * @see ModelElementEdge.LineMode
	 */
	public LineMode getLineMode() {
		return lineMode;
	}

	/**
	 * Stellt die Art der Verknüpfungslinie ein.
	 * @param lineMode	Art der Verknüpfungslinie (<code>null</code> bedeutet "Vorgabe Stil aus Modell verwenden)
	 * @see EditModel#edgeLineMode
	 * @see ModelElementEdge.LineMode
	 */
	public void setLineMode(final LineMode lineMode) {
		this.lineMode=lineMode;
	}

	/**
	 * Liefert die Größe des Pfeils am Ende der Verknüpfungslinie.
	 * @return	Größe des Pfeils am Ende der Verknüpfungslinie (<code>null</code> bedeutet "Vorgabe Stil aus Modell verwenden)
	 * @see EditModel#edgeArrowMode
	 * @see ModelElementEdge.ArrowMode
	 */
	public ArrowMode getArrowMode() {
		return arrowMode;
	}

	/**
	 * Stellt die Größe des Pfeils am Ende der Verknüpfungslinie ein.
	 * @param arrowMode	Größe des Pfeils am Ende der Verknüpfungslinie (<code>null</code> bedeutet "Vorgabe Stil aus Modell verwenden)
	 * @see EditModel#edgeArrowMode
	 * @see ModelElementEdge.ArrowMode
	 */
	public void setArrowMode(final ArrowMode arrowMode) {
		this.arrowMode=arrowMode;
	}

	@Override
	public boolean canCopy() {
		return false;
	}

	@Override
	public boolean canArrange() {
		return false;
	}

	@Override
	protected void addContextMenuItems(final Component owner, final JPopupMenu popupMenu, final ModelSurfacePanel surfacePanel, final Point point, final boolean readOnly) {
		if (readOnly) return;

		JMenu menu;
		JCheckBoxMenuItem check;
		JMenuItem item;

		/* Kantenführen */

		popupMenu.add(menu=new JMenu(Language.tr("Surface.Connection.LineMode")));

		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.Global"),Images.MODEL.getIcon(),lineMode==null));
		check.addActionListener(e->{lineMode=null; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.Direct"),Images.EDGE_MODE_DIRECT.getIcon(),lineMode==LineMode.DIRECT));
		check.addActionListener(e->{lineMode=LineMode.DIRECT; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.MultiLine"),Images.EDGE_MODE_MULTI_LINE.getIcon(),lineMode==LineMode.MULTI_LINE));
		check.addActionListener(e->{lineMode=LineMode.MULTI_LINE; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.MultiLineRounded"),Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon(),lineMode==LineMode.MULTI_LINE_ROUNDED));
		check.addActionListener(e->{lineMode=LineMode.MULTI_LINE_ROUNDED; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.LineMode.CubicCurve"),Images.EDGE_MODE_CUBIC_CURVE.getIcon(),lineMode==LineMode.CUBIC_CURVE));
		check.addActionListener(e->{lineMode=LineMode.CUBIC_CURVE; fireChanged();});

		/* Pfeile */

		popupMenu.add(menu=new JMenu(Language.tr("Surface.Connection.ArrowMode")));

		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.ArrowMode.Global"),Images.MODEL.getIcon(),arrowMode==null));
		check.addActionListener(e->{arrowMode=null; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.ArrowMode.Off"),Images.ARROW_SIZE_OFF.getIcon(),arrowMode==ArrowMode.OFF));
		check.addActionListener(e->{arrowMode=ArrowMode.OFF; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.ArrowMode.Small"),Images.ARROW_SIZE_SMALL.getIcon(),arrowMode==ArrowMode.SMALL));
		check.addActionListener(e->{arrowMode=ArrowMode.SMALL; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.ArrowMode.Medium"),Images.ARROW_SIZE_MEDIUM.getIcon(),arrowMode==ArrowMode.MEDIUM));
		check.addActionListener(e->{arrowMode=ArrowMode.MEDIUM; fireChanged();});
		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.ArrowMode.Large"),Images.ARROW_SIZE_LARGE.getIcon(),arrowMode==ArrowMode.LARGE));
		check.addActionListener(e->{arrowMode=ArrowMode.LARGE; fireChanged();});

		/* Farbe */

		popupMenu.add(menu=new JMenu(Language.tr("Surface.Connection.Color")));

		menu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.Color.Default"),lineColor==null));
		check.addActionListener(e->setLineColor(null));
		final SmallColorChooser colorChooser=new SmallColorChooser(lineColor);
		menu.add(colorChooser);
		colorChooser.addClickListener(e->setLineColor(colorChooser.getColor()));

		/* Beschriftung anzeigen */

		final String name=getName();
		if (name!=null && !name.isBlank()) {
			popupMenu.add(check=new JCheckBoxMenuItem(Language.tr("Surface.Connection.ShowLabel"),drawName));
			check.addActionListener(e->{drawName=!drawName; fireChanged();});
		}

		/* Kante aufsplitten */

		if ((connectionStart instanceof ModelElementPosition) && (connectionEnd instanceof ModelElementPosition)) {
			/* Ecke auf Pfad einfügen */
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.Connection.AddVertex"),Images.MODELEDITOR_ELEMENT_VERTEX.getIcon()));
			item.addActionListener(e->{
				final double zoom=surfacePanel.getZoom();
				contextAddVertex(surfacePanel,(ModelElementPosition)connectionStart,(ModelElementPosition)connectionEnd,new Point((int)Math.round(point.x/zoom),(int)Math.round(point.y/zoom)));
			});

			/* Teleport auf Pfad einfügen */
			popupMenu.add(item=new JMenuItem(Language.tr("Surface.Connection.AddTeleport"),Images.MODELEDITOR_ELEMENT_TELEPORT.getIcon()));
			item.addActionListener(e->{
				final double zoom=surfacePanel.getZoom();
				contextAddTeleport(surfacePanel,(ModelElementPosition)connectionStart,(ModelElementPosition)connectionEnd,new Point((int)Math.round(point.x/zoom),(int)Math.round(point.y/zoom)));
			});
		}
	}

	/**
	 * Legt die zentrale Position eines Elements fest.
	 * @param element	Element
	 * @param point	Mittelpunkt des Elements
	 */
	private static void setMiddlePositon(final ModelElementPosition element, final Point point) {
		final Dimension dimension=element.getSize();
		element.setPosition(new Point((int)Math.round(point.x-dimension.width/2.0),(int)Math.round(point.y-dimension.height/2.0)));
	}

	/**
	 * Fügt eine Verbindungskante zwischen zwei Elementen ein
	 * @param element1	Ausgangselement
	 * @param element2	Zielelement
	 */
	private void addEdge(final ModelElementPosition element1, final ModelElementPosition element2) {
		final ModelElementEdge edge=new ModelElementEdge(getModel(),surface,element1,element2);
		surface.add(edge);
		element1.addEdgeOut(edge);
		element2.addEdgeIn(edge);
	}

	/**
	 * Fügt eine Ecke hinzu
	 * @param surfacePanel	Zeichenfläche
	 * @param element1	Ausgangselement
	 * @param element2	Zielelement
	 * @param point	Position der zwischen Ausgangs- und Zielelement einzufügenden Ecke
	 */
	private void contextAddVertex(final ModelSurfacePanel surfacePanel, final ModelElementPosition element1, final ModelElementPosition element2, final Point point) {
		/* Ecke hinzufügen */
		final ModelElementVertex vertex=new ModelElementVertex(getModel(),surface);
		setMiddlePositon(vertex,point);
		surface.add(vertex);

		/* Diese Kante umbauen */
		element2.removeConnectionNotify(this);
		connectionEnd=vertex;
		vertex.addEdgeIn(this);

		/* Neue Kante einfügen */
		addEdge(vertex,element2);
	}

	/**
	 * Fügt eine Teleport-Strecke hinzu
	 * @param surfacePanel	Zeichenfläche
	 * @param element1	Ausgangselement
	 * @param element2	Zielelement
	 * @param point	Position der zwischen Ausgangs- und Zielelement einzufügenden Teleport-Strecke
	 */
	private void contextAddTeleport(final ModelSurfacePanel surfacePanel, final ModelElementPosition element1, final ModelElementPosition element2, final Point point) {
		/* Namen abfragen */
		final String name=JOptionPane.showInputDialog(surfacePanel.getParent(),Language.tr("Surface.Connection.AddTeleport.TargetName"),String.format("->id=%d",element2.getId()));
		if (name==null || name.isBlank()) return;

		/* Positionen berechnen */
		Point p;
		p=element1.getMiddlePosition(true);
		final Point p1=new Point((int)Math.round((p.x+3*point.x)/4.0),(int)Math.round((p.y+3*point.y)/4.0));
		p=element2.getMiddlePosition(true);
		final Point p2=new Point((int)Math.round((p.x+3*point.x)/4.0),(int)Math.round((p.y+3*point.y)/4.0));

		/* Teleportstationen hinzufügen */
		final ModelElementTeleportSource teleport1=new ModelElementTeleportSource(getModel(),surface);
		setMiddlePositon(teleport1,p1);
		surface.add(teleport1);
		teleport1.setDestination(name);
		final ModelElementTeleportDestination teleport2=new ModelElementTeleportDestination(getModel(),surface);
		setMiddlePositon(teleport2,p2);
		surface.add(teleport2);
		teleport2.setName(name);

		/* Diese Kante umbauen */
		element2.removeConnectionNotify(this);
		connectionEnd=teleport1;
		teleport1.addEdgeIn(this);

		/* Neue Kante einfügen */
		addEdge(teleport2,element2);
	}

	/**
	 * Gibt ein Icon an, welches neben dem Beschriftungslabel im Kontextmenü angezeigt werden soll.
	 * @return	Icon zur Beschriftung des Elements im Kontextmenü oder <code>null</code>, wenn kein Icon angezeigt werden soll.
	 */
	@Override
	public Icon buildIcon() {
		switch (getDrawLineMode(null)) {
		case DIRECT: return Images.EDGE_MODE_DIRECT.getIcon();
		case MULTI_LINE: return Images.EDGE_MODE_MULTI_LINE.getIcon();
		case MULTI_LINE_ROUNDED: return Images.EDGE_MODE_MULTI_LINE_ROUNDED.getIcon();
		case CUBIC_CURVE: return Images.EDGE_MODE_CUBIC_CURVE.getIcon();
		default: return null;
		}
	}

	/**
	 * Wo erfolgt die Anbindung an eine Station?
	 * @see Connect
	 */
	private enum Side {
		/** Oben */
		TOP,
		/** Links */
		LEFT,
		/** Unten */
		BOTTOM,
		/** Rechts */
		RIGHT
	}

	/**
	 * Beschreibt die Anbindung der Kante an eine Station
	 * @see ModelElementEdge#getLine(double)
	 */
	private static class Connect {
		/** x-Koordinate des Punktes der Anbindung */
		public final int x;
		/** y-Koordinate des Punktes der Anbindung */
		public final int y;
		/** Punkt der Anbindung */
		public final Point p;
		/** Wo erfolgt die Anbindung an eine Station? */
		public final Side side;

		/**
		 * Konstruktor der Klasse
		 * @param x	x-Koordinate des Punktes der Anbindung
		 * @param y	y-Koordinate des Punktes der Anbindung
		 * @param side	Wo erfolgt die Anbindung an eine Station?
		 */
		public Connect(final int x, final int y, final Side side) {
			this.x=x;
			this.y=y;
			p=new Point(x,y);
			this.side=side;
		}
	}

	/**
	 * Startpunkt beim letzten Aufruf von {@link #getLine(double)}
	 * @see #getLine(double)
	 */
	private Point lastLineP1;

	/**
	 * Startpunkt beim letzten Aufruf von {@link #getLine(double)}
	 * @see #getLine(double)
	 */
	private Point lastLineP2;

	/**
	 * Zoomfaktor  beim letzten Aufruf von {@link #getLine(double)}
	 * @see #getLine(double)
	 */
	private double lastZoom;

	/**
	 * Start- und Zielpunkt beim letzten Aufruf von {@link #getLine(double)}
	 * @see #getLine(double)
	 */
	private Connect[] lastLine;

	/**
	 * Ermittelt Start- und Zielpunkt der Linie
	 * @param zoom	Zoomfaktor
	 * @return	Start- und Zielpunkt
	 */
	private Connect[] getLine(final double zoom) {
		if (connectionStart==null || connectionEnd==null) return null;

		final Point p1=connectionStart.getMiddlePosition(true);
		final Point p2=connectionEnd.getMiddlePosition(true);
		if (p1==null || p2==null) return null;

		if (lastLine==null || lastLineP1==null || lastLineP2==null || lastLineP1.x!=p1.x || lastLineP1.y!=p1.y || lastLineP2.x!=p2.x || lastLineP2.y!=p2.y || lastZoom!=zoom) {
			if (lastLineP1==null) lastLineP1=new Point();
			lastLineP1.x=p1.x;
			lastLineP1.y=p1.y;
			if (lastLineP2==null) lastLineP2=new Point();
			lastLineP2.x=p2.x;
			lastLineP2.y=p2.y;
			lastZoom=zoom;

			final Point point1=connectionStart.getConnectionToPosition(p2);
			final Point point2=connectionEnd.getConnectionToPosition(p1);
			if (point1==null || point2==null) return null;

			final Side side1;
			if (p1.y>point1.y) side1=Side.TOP; else {
				if (p1.y<point1.y) side1=Side.BOTTOM; else {
					if (p1.x>point1.x) side1=Side.LEFT; else side1=Side.RIGHT;
				}
			}
			final Side side2;
			if (p2.y>point2.y) side2=Side.TOP; else {
				if (p2.y<point2.y) side2=Side.BOTTOM; else {
					if (p2.x>point2.x) side2=Side.LEFT; else side2=Side.RIGHT;
				}
			}

			lastLine=new Connect[]{
					new Connect((int)FastMath.round(point1.x*zoom),(int)FastMath.round(point1.y*zoom),side1),
					new Connect((int)FastMath.round(point2.x*zoom),(int)FastMath.round(point2.y*zoom),side2)
			};
		}

		return lastLine;
	}

	/**
	 * Liefert die Art der Verknüpfungslinien.
	 * @param points	Zu verbindende Punkte
	 * @return	Art der Verknüpfungslinien
	 */
	private LineMode getDrawLineMode(final Connect[] points) {
		LineMode drawLineMode=lineMode;
		final EditModel model=getModel();
		if (drawLineMode==null && model!=null) drawLineMode=model.edgeLineMode;
		if (drawLineMode==null) drawLineMode=LineMode.DIRECT;

		if (drawLineMode==LineMode.DIRECT) return drawLineMode;
		if (points!=null) {
			if (points[0].x==points[1].x || points[0].y==points[1].y) return LineMode.DIRECT;
			if ((points[0].side==Side.TOP || points[0].side==Side.BOTTOM) && (points[1].side!=Side.TOP && points[1].side!=Side.BOTTOM)) return LineMode.DIRECT;
			if ((points[0].side==Side.LEFT || points[0].side==Side.RIGHT) && (points[1].side!=Side.LEFT && points[1].side!=Side.RIGHT)) return LineMode.DIRECT;
		}
		return drawLineMode;
	}

	/**
	 * Cache für den ersten Punkt des Pfeils
	 * @see #drawArrow(Graphics, ComplexLine, Point, Point, double, boolean)
	 */
	private Point arrow1=new Point();

	/**
	 * Cache für den zweiten Punkt des Pfeils
	 * @see #drawArrow(Graphics, ComplexLine, Point, Point, double, boolean)
	 */
	private Point arrow2=new Point();

	/**
	 * Cache für den Linienmitte-Punkt
	 * @see #drawText(Graphics, Point, double)
	 */
	private Point middle=new Point();

	/**
	 * Wie stark sollen die Kontrollpunkte auf einer Bezierkurven-Kante wirken?
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 * @see LineMode#CUBIC_CURVE
	 */
	private static final double CUBIC_CURVE_CTRL_FACTOR=0.5;

	/**
	 * Zeichensystem, welches in den Modi {@link ui.modeleditor.coreelements.ModelElement.DrawMode#HIGHLIGHTED}
	 * und {@link ui.modeleditor.coreelements.ModelElement.DrawMode#GRAYED_OUT} verwendet wird.
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private final ComplexLine specialPainter=new ComplexLine();

	/**
	 * Linienfarbe im Standardfall<br>
	 * (Kann <code>null</code> sein, dann wird {@link EditModel#edgePainterNormal} verwendet.)
	 * @see #getLineColor()
	 * @see #setLineColor(Color)
	 */
	private Color lineColor;

	/**
	 * Zeichenobjekt zur Umsetzung der Farbdaten aus {@link #lineColor}<br>
	 * (Ist {@link #lineColor} <code>null</code>, dann auch dieses Objekt)
	 * @see #setLineColor(Color)
	 * @see #drawToGraphics(Graphics, Rectangle, double, boolean)
	 */
	private ComplexLine customNormalPainter=null;

	/**
	 * Liefert die eingestellte individuelle Linienfarbe.
	 * @return	Linienfarbe oder <code>null</code>, wenn die Modellvorgabe verwendet werden soll
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * Stellt eine individuelle Farbe für die Verbindungslinie ein.
	 * @param lineColor	Linienfarbe oder <code>null</code>, wenn die Modellvorgabe verwendet werden soll
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor=lineColor;
		if (lineColor==null) {
			customNormalPainter=null;
		} else {
			customNormalPainter=new ComplexLine(1,lineColor,0);
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
		/* Zeichenfläche vorbereiten */
		graphics.setClip(drawRect.x,drawRect.y,drawRect.width,drawRect.height);

		/* Punkte berechnen */
		final Connect[] points=getLine(zoom);
		if (points==null) return;
		final Point p1=points[0].p;
		final Point p2=points[1].p;

		/* Mitte bestimmen */
		middle.x=(p1.x+p2.x)/2;
		middle.y=(p1.y+p2.y)/2;

		/* Linienstil */
		final ComplexLine painter;
		switch (drawMode) {
		case NORMAL:
			if (isSelected() && showSelectionFrames) {
				painter=getModel().edgePainterSelected;
			} else {
				if (customNormalPainter!=null) painter=customNormalPainter; else painter=getModel().edgePainterNormal;
			}
			break;
		case GRAYED_OUT:
			specialPainter.set(1,Color.GRAY,getModel().edgePainterNormal.getType());
			painter=specialPainter;
			break;
		case HIGHLIGHTED:
			specialPainter.set(3*getModel().edgePainterNormal.getWidth(),Color.RED,getModel().edgePainterNormal.getType());
			painter=specialPainter;
			break;
		default:
			painter=(isSelected() && showSelectionFrames)?(getModel().edgePainterSelected):(getModel().edgePainterNormal);
			break;
		}

		/* Linie(n) zeichnen */
		switch (getDrawLineMode(points)) {
		case DIRECT:
			drawArrow(graphics,painter,p1,p2,zoom,false);
			break;
		case MULTI_LINE:
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				/* vertikal, horizontal, vertikal */
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				drawLine(graphics,painter,p1,m1,zoom);
				drawLine(graphics,painter,m1,m2,zoom);
				drawArrow(graphics,painter,m2,p2,zoom,false);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				drawLine(graphics,painter,p1,m1,zoom);
				drawLine(graphics,painter,m1,m2,zoom);
				drawArrow(graphics,painter,m2,p2,zoom,false);
			}
			break;
		case MULTI_LINE_ROUNDED:
			final int radius=(int)FastMath.round(ARC_RADIUS*zoom);
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				/* vertikal, horizontal, vertikal */
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.y-m1.y)<=radius || Math.abs(m1.x-m2.x)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(p1.x,m1.y+radius*((m1.y>p1.y)?-1:1));
					m1b=new Point(m1.x+radius*((m2.x>m1.x)?1:-1),m1.y);
					if (m1b.x<m1a.x ^ m1b.y<m1a.y) drawArcClockWise(graphics,painter,m1a,m1b,zoom); else drawArcClockWise(graphics,painter,m1b,m1a,zoom);
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.x-m2.x)<=2*radius || Math.abs(p2.y-m2.y)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x+radius*((m2.x>m1.x)?-1:1),m2.y);
					m2b=new Point(p2.x,m2.y+radius*((m2.y>p2.y)?-1:1));
					if (m2b.x>m2a.x ^ m2b.y<m2a.y) drawArcClockWise(graphics,painter,m2a,m2b,zoom); else drawArcClockWise(graphics,painter,m2b,m2a,zoom);
				}
				drawLine(graphics,painter,p1,m1a,zoom);
				drawLine(graphics,painter,m1b,m2a,zoom);
				drawArrow(graphics,painter,m2b,p2,zoom,false);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.x-m1.x)<=radius || Math.abs(m1.y-m2.y)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(m1.x+radius*((m1.x>p1.x)?-1:1),p1.y);
					m1b=new Point(m1.x,m1.y+radius*((m2.y>m1.y)?1:-1));
					if (m1b.y>m1a.y ^ m1b.x<m1a.x) drawArcClockWise(graphics,painter,m1a,m1b,zoom); else drawArcClockWise(graphics,painter,m1b,m1a,zoom);
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.y-m2.y)<=2*radius || Math.abs(p2.x-m2.x)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x,m2.y+radius*((m2.y>m1.y)?-1:1));
					m2b=new Point(m2.x+radius*((m2.x>p2.x)?-1:1),p2.y);
					if (m2b.y<m2a.y ^ m2b.x<m2a.x) drawArcClockWise(graphics,painter,m2a,m2b,zoom); else drawArcClockWise(graphics,painter,m2b,m2a,zoom);
				}
				drawLine(graphics,painter,p1,m1a,zoom);
				drawLine(graphics,painter,m1b,m2a,zoom);
				drawArrow(graphics,painter,m2b,p2,zoom,false);
			}
			break;
		case CUBIC_CURVE:
			final int deltaX=Math.max(20,Math.abs(p1.x-p2.x));
			final int deltaY=Math.max(20,Math.abs(p1.y-p2.y));
			double ctrlx1=p1.x;
			double ctrly1=p1.y;
			switch (points[0].side) {
			case BOTTOM: ctrly1+=deltaY*CUBIC_CURVE_CTRL_FACTOR; break;
			case LEFT:ctrlx1-=deltaX*CUBIC_CURVE_CTRL_FACTOR; break;
			case RIGHT: ctrlx1+=deltaX*CUBIC_CURVE_CTRL_FACTOR; break;
			case TOP: ctrly1-=deltaY*CUBIC_CURVE_CTRL_FACTOR; break;
			}

			double ctrlx2=p2.x;
			double ctrly2=p2.y;
			switch (points[1].side) {
			case BOTTOM: ctrly2+=deltaY*CUBIC_CURVE_CTRL_FACTOR; break;
			case LEFT:ctrlx2-=deltaX*CUBIC_CURVE_CTRL_FACTOR; break;
			case RIGHT: ctrlx2+=deltaX*CUBIC_CURVE_CTRL_FACTOR; break;
			case TOP: ctrly2-=deltaY*CUBIC_CURVE_CTRL_FACTOR; break;
			}

			painter.	drawBezier(graphics,p1,ctrlx1,ctrly1,ctrlx2,ctrly2,p2,zoom);
			drawArrow(graphics,painter,new Point((int)FastMath.round(ctrlx2),(int)FastMath.round(ctrly2)),p2,zoom,true);
			break;
		}

		/* Text ausgeben */
		if (drawName) drawText(graphics,middle,zoom);
	}

	/**
	 * Liefert eine Liste aller Punkte, aus denen die Gesamtlinie besteht.
	 * @param zoom	Aktueller Zoomfaktor
	 * @return	Liste aller Punkte, aus denen die Gesamtlinie besteht
	 */
	public List<Point> getPolylinePoints(final double zoom) {
		/* Punkte berechnen */
		final Connect[] points=getLine(zoom);
		if (points==null) return null;
		final Point p1=points[0].p;
		final Point p2=points[1].p;

		/* Mitte bestimmen */
		middle.x=(p1.x+p2.x)/2;
		middle.y=(p1.y+p2.y)/2;

		final List<Point> polyline=new ArrayList<>();

		/* Linie(n) zeichnen */
		switch (getDrawLineMode(points)) {
		case DIRECT:
			polyline.add(p1);
			polyline.add(p2);
			break;
		case MULTI_LINE:
		case MULTI_LINE_ROUNDED:
		case CUBIC_CURVE:
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				/* vertikal, horizontal, vertikal */
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				polyline.add(p1);
				polyline.add(m1);
				polyline.add(m2);
				polyline.add(p2);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				polyline.add(p1);
				polyline.add(m1);
				polyline.add(m2);
				polyline.add(p2);
			}
			break;
		}

		return polyline;
	}

	/**
	 * Zeichnet eine Linie
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param painter	Linienzeichner
	 * @param point1	Startpunkt
	 * @param point2	Zielpunkt
	 * @param zoom	Zoomfaktor
	 */
	private void drawLine(final Graphics graphics, final ComplexLine painter, final Point point1, final Point point2, final double zoom) {
		painter.draw(graphics,point1,point2,zoom);
	}

	/**
	 * Zeichnet eine Pfeilspitze
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param painter	Linienzeichner
	 * @param point1	Startpunkt der Linie
	 * @param point2	Zielpunkt der Linie
	 * @param zoom	Zoomfaktor
	 * @param arrowHeadOnly	Vollständigen Pfeil zeichnen (<code>false</code>) oder nur die Pfeilspitze (<code>true</code>)
	 */
	private void drawArrow(final Graphics graphics, final ComplexLine painter, final Point point1, final Point point2, final double zoom, final boolean arrowHeadOnly) {
		ArrowMode drawArrowMode=arrowMode;
		final EditModel model=getModel();
		if (drawArrowMode==null && model!=null) drawArrowMode=model.edgeArrowMode;
		if (drawArrowMode==null) drawArrowMode=ArrowMode.LARGE;

		final int drawArrowSize;
		switch (drawArrowMode) {
		case OFF: drawArrowSize=0; break;
		case SMALL: drawArrowSize=(2*ARROW_SIZE)/3; break;
		case MEDIUM: drawArrowSize=ARROW_SIZE; break;
		case LARGE: drawArrowSize=(ARROW_SIZE*3)/2; break;
		default: drawArrowSize=ARROW_SIZE; break;
		}

		/* Linien zeichnen */
		if (!arrowHeadOnly) painter.draw(graphics,point1,point2,zoom);

		if (drawArrowSize>0) {
			/* Pfeil berechnen */
			double v0=point2.x-point1.x, v1=point2.y-point1.y;
			final double length=Math.sqrt(v0*v0+v1*v1);
			v0=v0/length; v1=v1/length;
			final double w0=v1, w1=-v0;
			arrow1.x=(int)FastMath.round(point2.x-drawArrowSize*zoom*v0+drawArrowSize*zoom*w0);
			arrow1.y=(int)FastMath.round(point2.y-drawArrowSize*zoom*v1+drawArrowSize*zoom*w1);
			arrow2.x=(int)FastMath.round(point2.x-drawArrowSize*zoom*v0-drawArrowSize*zoom*w0);
			arrow2.y=(int)FastMath.round(point2.y-drawArrowSize*zoom*v1-drawArrowSize*zoom*w1);

			/* Pfeil zeichnen */
			painter.draw(graphics,point2,arrow1,zoom);
			painter.draw(graphics,point2,arrow2,zoom);
		}
	}

	/**
	 * Zeichnet einen Bogen mit den Einstellungen der Linie
	 * @param graphics	{@link Graphics}-Objekt in das der Bogen eingezeichnet werden soll
	 * @param painter	Linienzeichner
	 * @param point1	Startpunkt des Bogens
	 * @param point2	Zielpunkt des Bogens
	 * @param zoom	Zoomfaktor (1.0==100%)
	 */
	private void drawArcClockWise(final Graphics graphics, final ComplexLine painter, final Point point1, final Point point2, final double zoom) {
		painter.drawArc(graphics,point1,point2,zoom);
	}

	/**
	 * Zoomfaktor beim letzten Aufruf von {@link #drawText(Graphics, Point, double)}
	 * @see #drawText(Graphics, Point, double)
	 */
	private double lastZoomFont=-1;

	/**
	 * Schriftartenobjekt beim letzten Aufruf von {@link #drawText(Graphics, Point, double)}
	 * @see #drawText(Graphics, Point, double)
	 */
	private Font lastFont;

	/**
	 * Farbe für Kantenbeschriftungen
	 * @see #drawText(Graphics, Point, double)
	 */
	private Color textColor;

	/**
	 * Bezog sich der letzte Aufruf von {@link #drawText(Graphics, Point, double)}
	 * auf einen Export als Vektorgrafik?
	 * @see #drawText(Graphics, Point, double)
	 */
	private boolean lastWasExport=false;

	/**
	 * Gibt {@link #getName()} aus.
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param middle	Mittelpunkt des Textes
	 * @param zoom	Zoomfaktor
	 */
	private void drawText(final Graphics graphics, final Point middle, final double zoom) {
		final String text=getName();
		if (text==null || text.isBlank()) return;

		if (lastWasExport) {
			lastFont=null;
			lastWasExport=false;
		}
		final boolean isExport=(graphics instanceof SVGGraphics2D || graphics instanceof VectorGraphics2D);
		if (isExport) {
			lastFont=null;
			lastWasExport=true;
		}
		if (zoom!=lastZoomFont || lastFont==null) {
			/*
			 * "Sans" und "Serif" werden von jedem SVG-Renderer anders umgesetzt,
			 * daher machen wir hier konkrete Vorgaben, so dass die Laufweite
			 * vorab korrekt berechnet werden kann und Texte und Sub- und Subskripte
			 * zusammen passen.
			 */
			final String fontName=isExport?FontCache.FontFamily.WIN_VERDANA.name:ModelElementBox.DEFAULT_FONT_TYPE;
			lastFont=new Font(fontName,Font.PLAIN,(int)Math.round(11*zoom));
			lastZoomFont=zoom;
		}

		graphics.setFont(lastFont);
		if (textColor==null) textColor=FlatLaFHelper.isDark()?EditModel.BLACK_COLOR_IN_DARK_MODE:Color.BLACK;
		graphics.setColor(textColor);
		graphics.drawString(text,middle.x-graphics.getFontMetrics().stringWidth(text)/2,middle.y+graphics.getFontMetrics().getAscent());
	}

	/**
	 * Prüft, ob ein Punkt auf oder zumindest in unmittelbarer Nähe einer Linie liegt
	 * @param point	Zu prüfender Punkt
	 * @param p1	Startpunkt der Linie
	 * @param p2	Endpunkt der Linie
	 * @param zoom	Zoomfaktor
	 * @return	Liefert <code>true</code>, wenn sich der Punkt in unmittelbarer Nähe der Linie befindet
	 */
	private boolean lineContainsPoint(final Point point, final Point p1, final Point p2, final double zoom) {
		/* Gerade: u1+t1*v1 */
		final double[] u1=new double[]{p1.x,p1.y};
		final double[] v1=new double[]{p2.x-p1.x,p2.y-p1.y};

		/* Gerade von Punkt senkrecht zu Ausgangsgerade: u2+t2*v2 */
		final double[] u2=new double[]{point.x,point.y};
		final double[] v2=new double[]{v1[1]-point.x,-v1[0]-point.y};

		/* Schnitt der beiden Geraden: u1+t1*v1=u2+t2*v2*/
		double t1;
		if (v2[0]!=0) {
			t1=(u1[1]-u2[1]-v2[1]*(u1[0]-u2[0])/v2[0])/(v1[0]*v2[1]/v2[0]-v1[1]);
		} else {
			t1=(u1[0]-u2[0]-v2[0]*(u1[1]-u2[1])/v2[1])/(v1[1]*v2[0]/v2[1]-v1[0]);
		}

		if (t1<0 || t1>1) return false; /* Lotfußpunkt außerhalb der Strecke */
		final double[] p=new double[]{u1[0]+t1*v1[0],u1[1]+t1*v1[1]}; /* Lotfußpunkt */

		/*
		Debug-Informationen:
		System.out.println("P=("+points[0].x+";"+points[0].y+") Q=("+points[1].x+";"+points[1].y+")");
		System.out.println("u1=("+Math.round(u1[0])+";"+Math.round(u1[1])+") v1=("+Math.round(v1[0])+";"+Math.round(v1[1])+")");
		System.out.println("u2=("+Math.round(u2[0])+";"+Math.round(u2[1])+") v2=("+Math.round(v2[0])+";"+Math.round(v2[1])+")");
		System.out.println("p=("+Math.round(p[0])+";"+Math.round(p[1])+")");
		System.out.println(delta);
		 */

		final double deltaX=point.x-p[0];
		final double deltaY=point.y-p[1];
		double delta=Math.sqrt(deltaX*deltaX+deltaY*deltaY);
		if (t1<0.1 || t1>0.9) {
			return (delta<3*zoom);
		} else {
			return (delta<20*zoom);
		}
	}

	/**
	 * Liefert den Start- und den Endpunkt der Verbindungskante.
	 * @param zoom	Zoomfaktor
	 * @return	Im Erfolgsfall ein Array aus zwei Elementen; im Fehlerfall <code>null</code>.
	 */
	public Point[] getConnectionLine(final double zoom) {
		final Connect[] points=getLine(zoom);
		if (points==null) return null;
		return new Point[] {points[0].p,points[1].p};
	}

	/**
	 * Prüft, ob sich ein Punkt innerhalb des Elements befindet
	 * @param point	Punkt, bei dem geprüft werden soll, ob er sich innerhalb des Elements befindet
	 * @param zoom	Zoomfaktor
	 * @return	Gibt <code>true</code> zurück, wenn sich der Punkt innerhalb des Elements befindet
	 */
	@Override
	public boolean containsPoint(final Point point, final double zoom) {
		/* Punkte berechnen */
		final Connect[] points=getLine(zoom);
		if (points==null) return false;
		final Point p1=points[0].p;
		final Point p2=points[1].p;

		/* Mitte bestimmen */
		middle.x=(p1.x+p2.x)/2;
		middle.y=(p1.y+p2.y)/2;

		switch (getDrawLineMode(points)) {
		case DIRECT:
		case CUBIC_CURVE: /* Die Bezierkurve behandeln wir näherungsweise wie gerade Linien. */
			return lineContainsPoint(point,p1,p2,zoom);
		case MULTI_LINE:
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				/* vertikal, horizontal, vertikal */
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				return lineContainsPoint(point,p1,m1,zoom) || lineContainsPoint(point,m1,m2,zoom) || lineContainsPoint(point,m2,p2,zoom);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				return lineContainsPoint(point,p1,m1,zoom) || lineContainsPoint(point,m1,m2,zoom) || lineContainsPoint(point,m2,p2,zoom);
			}
		case MULTI_LINE_ROUNDED:
			final int radius=(int)FastMath.round(ARC_RADIUS*zoom);
			if (points[0].side==Side.TOP || points[0].side==Side.BOTTOM) {
				final Point m1=new Point(p1.x,middle.y);
				final Point m2=new Point(p2.x,middle.y);
				/* vertikal, horizontal, vertikal */
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.y-m1.y)<=radius || Math.abs(m1.x-m2.x)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(p1.x,m1.y+radius*((m1.y>p1.y)?-1:1));
					m1b=new Point(m1.x+radius*((m2.x>m1.x)?1:-1),m1.y);
					if (lineContainsPoint(point,m1a,m1b,zoom)) return true;
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.x-m2.x)<=2*radius || Math.abs(p2.y-m2.y)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x+radius*((m2.x>m1.x)?-1:1),m2.y);
					m2b=new Point(p2.x,m2.y+radius*((m2.y>p2.y)?-1:1));
					if (lineContainsPoint(point,m2a,m2b,zoom)) return true;
				}
				return lineContainsPoint(point,p1,m1a,zoom) || lineContainsPoint(point,m1b,m2a,zoom) || lineContainsPoint(point,m2b,p2,zoom);
			} else {
				/* horizontal, vertikal, horizontal */
				final Point m1=new Point(middle.x,p1.y);
				final Point m2=new Point(middle.x,p2.y);
				final Point m1a;
				final Point m1b;
				if (Math.abs(p1.x-m1.x)<=radius || Math.abs(m1.y-m2.y)<=2*radius) {
					m1a=m1;
					m1b=m1;
				} else {
					m1a=new Point(m1.x+radius*((m1.x>p1.x)?-1:1),p1.y);
					m1b=new Point(m1.x,m1.y+radius*((m2.y>m1.y)?1:-1));
					if (lineContainsPoint(point,m1a,m1b,zoom)) return true;
				}
				final Point m2a;
				final Point m2b;
				if (Math.abs(m1.y-m2.y)<=2*radius || Math.abs(p2.x-m2.x)<=radius) {
					m2a=m2;
					m2b=m2;
				} else {
					m2a=new Point(m2.x,m2.y+radius*((m2.y>m1.y)?-1:1));
					m2b=new Point(m2.x+radius*((m2.x>p2.x)?-1:1),p2.y);
					if (lineContainsPoint(point,m2a,m2b,zoom)) return true;
				}
				return lineContainsPoint(point,p1,m1a,zoom) || lineContainsPoint(point,m1b,m2a,zoom) || lineContainsPoint(point,m2b,p2,zoom);
			}
		default:
			return false;
		}
	}

	/**
	 * Name des Elementtyps für die Anzeige im Kontextmenü
	 * @return	Name des Elementtyps
	 */
	@Override
	public String getContextMenuElementName() {
		return Language.tr("Surface.Connection.Name");
	}

	/**
	 * Benachrichtigt das Element, dass es aus der Surface-Liste ausgetragen wurde.
	 */
	@Override
	public void removeNotify() {
		if (connectionStart!=null) connectionStart.removeConnectionNotify(this);
		if (connectionEnd!=null) connectionEnd.removeConnectionNotify(this);
	}

	/**
	 * Benachrichtigt das Element, dass ein mit ihm in Verbindung stehendes Element entfernt wurde.
	 */
	@Override
	public void removeConnectionNotify(final ModelElement element) {
		if (element==connectionStart || element==connectionEnd) surface.remove(this);
	}

	/**
	 * Liefert den jeweiligen xml-Element-Namen für das Modell-Element
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	@Override
	public String[] getXMLNodeNames() {
		return Language.trAll("Surface.Edge.XML.Root");
	}

	/**
	 * Ermöglicht die Abfrage der Liste der Namen ohne dafür ein Objekt anlegen zu müssen.
	 * @return	xml-Element-Namen, der diesem Modell-Element zugeordnet werden soll
	 */
	public static String[] getXMLNodeNamesStatic() {
		return Language.trAll("Surface.Edge.XML.Root");
	}

	/**
	 * Speichert die Eigenschaften des Modell-Elements als Untereinträge eines xml-Knotens
	 * @param doc	Übergeordnetes xml-Dokument
	 * @param node	Übergeordneter xml-Knoten, in dessen Kindelementen die Daten des Objekts gespeichert werden sollen
	 */
	@Override
	protected void addPropertiesDataToXML(final Document doc, final Element node) {
		super.addPropertiesDataToXML(doc,node);
		if (connectionStart!=null || connectionEnd!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.Connection"));
			node.appendChild(sub);
			sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Type"),Language.trPrimary("Surface.XML.Connection.Type.Edge"));
			if (connectionStart!=null) sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element1"),""+connectionStart.getId());
			if (connectionEnd!=null) sub.setAttribute(Language.trPrimary("Surface.XML.Connection.Element2"),""+connectionEnd.getId());
		}
		if (lineMode!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.LineMode"));
			node.appendChild(sub);
			switch (lineMode) {
			case DIRECT: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.Direct")); break;
			case MULTI_LINE: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.MultiLine")); break;
			case MULTI_LINE_ROUNDED: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.MultiLineRounded")); break;
			case CUBIC_CURVE: sub.setTextContent(Language.trPrimary("Surface.XML.LineMode.CubicCurve")); break;
			}
		}
		if (arrowMode!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.ArrowMode"));
			node.appendChild(sub);
			switch (arrowMode) {
			case OFF: sub.setTextContent(Language.trPrimary("Surface.XML.ArrowMode.Off")); break;
			case SMALL: sub.setTextContent(Language.trPrimary("Surface.XML.ArrowMode.Small")); break;
			case MEDIUM: sub.setTextContent(Language.trPrimary("Surface.XML.ArrowMode.Medium")); break;
			case LARGE: sub.setTextContent(Language.trPrimary("Surface.XML.ArrowMode.Large")); break;
			}
		}
		if (lineColor!=null) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.LineColor"));
			node.appendChild(sub);
			sub.setTextContent(EditModel.saveColor(lineColor));
		}
		if (!drawName) {
			final Element sub=doc.createElement(Language.trPrimary("Surface.XML.LineLabel"));
			node.appendChild(sub);
			sub.setTextContent("0");
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

		if (Language.trAll("Surface.XML.Connection",name)) {
			Integer I;
			String s;
			s=Language.trAllAttribute("Surface.XML.Connection.Element1",node);
			if (!s.isEmpty()) {
				I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element1"),name,node.getParentNode().getNodeName());
				connectionStartId=I;
			}
			s=Language.trAllAttribute("Surface.XML.Connection.Element2",node);
			if (!s.isEmpty()) {
				I=NumberTools.getNotNegativeInteger(s);
				if (I==null) return String.format(Language.tr("Surface.XML.AttributeSubError"),Language.trPrimary("Surface.XML.Connection.Element2"),name,node.getParentNode().getNodeName());
				connectionEndId=I;
			}
			return null;
		}

		if (Language.trAll("Surface.XML.LineMode",name)) {
			if (Language.trAll("Surface.XML.LineMode.Direct",content)) lineMode=ModelElementEdge.LineMode.DIRECT;
			if (Language.trAll("Surface.XML.LineMode.MultiLine",content)) lineMode=ModelElementEdge.LineMode.MULTI_LINE;
			if (Language.trAll("Surface.XML.LineMode.MultiLineRounded",content)) lineMode=ModelElementEdge.LineMode.MULTI_LINE_ROUNDED;
			if (Language.trAll("Surface.XML.LineMode.CubicCurve",content)) lineMode=ModelElementEdge.LineMode.CUBIC_CURVE;
			return null;
		}

		if (Language.trAll("Surface.XML.ArrowMode",name)) {
			if (Language.trAll("Surface.XML.ArrowMode.Off",content)) arrowMode=ModelElementEdge.ArrowMode.OFF;
			if (Language.trAll("Surface.XML.ArrowMode.Small",content)) arrowMode=ModelElementEdge.ArrowMode.SMALL;
			if (Language.trAll("Surface.XML.ArrowMode.Medium",content)) arrowMode=ModelElementEdge.ArrowMode.MEDIUM;
			if (Language.trAll("Surface.XML.ArrowMode.Large",content)) arrowMode=ModelElementEdge.ArrowMode.LARGE;
			return null;
		}

		if (Language.trAll("Surface.XML.LineColor",name)) {
			lineColor=EditModel.loadColor(content);
			return null;
		}

		if (Language.trAll("Surface.XML.LineLabel",name)) {
			if (content.equals("0")) drawName=false;
			return null;
		}

		return null;
	}

	/**
	 * Startpunkt der Verknüpfung
	 * @return	Startpunkt der Verknüpfung
	 */
	public ModelElement getConnectionStart() {
		return connectionStart;
	}

	/**
	 * Endpunkt der Verknüpfung
	 * @return	Endpunkt der Verknüpfung
	 */
	public ModelElement getConnectionEnd() {
		return connectionEnd;
	}

	/**
	 * Stellt den Endpunkt der Verknüpfung ein
	 * @param element	Neuer Endpunkt der Verknüpfung
	 */
	public void setConnectionEnd(final ModelElement element) {
		connectionEnd=element;
		fireChanged();
	}

	/**
	 * Prüft, ob sich diese Kante in einer Liste von Kanten befindet
	 * @param list	Liste der Kanten
	 * @return	Liefert <code>true</code>, wenn sich diese Kante in der Liste der Kanten befindet
	 * @see #isConnectionOk()
	 */
	private boolean edgeInList(final ModelElementEdge[] list) {
		for (ModelElementEdge edge: list) if (edge==this) return true;
		return false;
	}

	/**
	 * Prüft, ob die Elemente, die die Kante verknüpft auch von der Kante wissen.
	 * @return	Gibt <code>true</code> zurück, wenn die Kante korrekt verankert ist.
	 */
	public boolean isConnectionOk() {
		if (surface==null) return true;
		if (connectionStart==null || connectionEnd==null) return false;

		/* Startelement */

		if (connectionStart instanceof ModelElementEdgeOut) {
			final ModelElementEdgeOut edgeOutElement=(ModelElementEdgeOut)connectionStart;
			if (edgeOutElement.getEdgeOut()!=this) return false;
		}

		if (connectionStart instanceof ModelElementEdgeMultiOut) {
			final ModelElementEdgeMultiOut edgesOutElement=(ModelElementEdgeMultiOut)connectionStart;
			if (!edgeInList(edgesOutElement.getEdgesOut())) return false;
		}

		if (connectionStart instanceof ModelElementProcess) {
			final ModelElementProcess process=(ModelElementProcess)connectionStart;
			if (process.getEdgeOutSuccess()!=this && process.getEdgeOutCancel()!=this) return false;
		}

		/* Zielelement */

		if (connectionEnd instanceof ModelElementEdgeMultiIn) {
			final ModelElementEdgeMultiIn edgesInElement=(ModelElementEdgeMultiIn)connectionEnd;
			if (!edgeInList(edgesInElement.getEdgesIn())) return false;
		}

		return true;
	}

	/**
	 * Liefert die Javascript-Daten für die Station zur Ausgabe des Modells als HTML-Datei
	 * @param outputBuilder	Builder, der die Gesamtdaten aufnehmen soll
	 * @return	Javascript-Daten für die Station
	 */
	private String getHTMLDrawEdge(final HTMLOutputBuilder outputBuilder) {
		final StringBuilder sb=new StringBuilder();

		sb.append("function drawEdge(p1,p2,text) {\n");

		sb.append("  context.strokeStyle=\"Black\";\n");
		sb.append("  context.lineWidth=1;\n");
		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(p1.x,p1.y);\n");
		sb.append("  context.lineTo(p2.x,p2.y);\n");
		sb.append("  context.stroke();\n");

		sb.append("  var v={x: p2.x-p1.x,y: p2.y-p1.y};\n");
		sb.append("  var length=Math.sqrt(v.x*v.x+v.y*v.y);\n");
		sb.append("  v={x: v.x/length,y: v.y/length};\n");
		sb.append("  var w={x: v.y, y: -v.x};\n");
		sb.append("  var p1a=Math.round(p2.x-"+ARROW_SIZE+"*v.x+"+ARROW_SIZE+"*w.x);\n");
		sb.append("  var p2a=Math.round(p2.y-"+ARROW_SIZE+"*v.y+"+ARROW_SIZE+"*w.y);\n");
		sb.append("  var p1b=Math.round(p2.x-"+ARROW_SIZE+"*v.x-"+ARROW_SIZE+"*w.x);\n");
		sb.append("  var p2b=Math.round(p2.y-"+ARROW_SIZE+"*v.y-"+ARROW_SIZE+"*w.y);\n");

		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(p2.x,p2.y);\n");
		sb.append("  context.lineTo(p1a,p2a);\n");
		sb.append("  context.stroke();\n");

		sb.append("  context.beginPath();\n");
		sb.append("  context.moveTo(p2.x,p2.y);\n");
		sb.append("  context.lineTo(p1b,p2b);\n");
		sb.append("  context.stroke();\n");

		if (drawName) {
			sb.append("  if (typeof(text)!=\"undefined\") {\n");
			sb.append("    var middle={x: Math.round((p1.x+p2.x)/2), y: Math.round((p1.y+p2.y)/2)};\n");
			sb.append("    context.font=\"11px Verdana,Lucida,sans-serif\";\n");
			sb.append("    context.textAlign=\"center\";\n");
			sb.append("    context.textBaseline=\"bottom\";\n");
			sb.append("    context.fillStyle=\"Black\";\n");
			sb.append("    context.fillText(text,middle.x,middle.y);\n");
			sb.append("  }\n");
		}

		sb.append("}\n");

		return sb.toString();
	}

	/**
	 * Zeichnet das Element in einem {@link HTMLOutputBuilder}
	 * @param outputBuilder	Builder, der die Daten aufnehmen soll
	 */
	private void specialOutputHTML(final HTMLOutputBuilder outputBuilder) {
		final Connect[] points=getLine(1.0);
		if (points==null) return;

		outputBuilder.addJSUserFunction("drawEdge",builder->getHTMLDrawEdge(builder));

		final String text=getName();
		if (text!=null && !text.isEmpty()) {
			outputBuilder.outputBody.append("drawEdge({x: "+points[0].x+", y: "+points[0].y+"},{x: "+points[1].x+", y: "+points[1].y+"},\""+HTMLOutputBuilder.encodeHTML(text,true)+"\");\n");
		} else {
			outputBuilder.outputBody.append("drawEdge({x: "+points[0].x+", y: "+points[0].y+"},{x: "+points[1].x+", y: "+points[1].y+"});\n");
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

	/**
	 * Cache für das Objekt zur Speicherung des Linienmittelpunktes
	 * im Nur-Lese-Rückgabemodus von {@link #getMiddlePosition(boolean)}
	 * @see #getMiddlePosition(boolean)
	 */
	private Point readOnlyMiddle=null;

	@Override
	public Point getMiddlePosition(final boolean readOnly) {
		if (connectionStart==null || connectionEnd==null) return null;
		final Point p1=connectionStart.getPosition(true);
		final Point p2=connectionEnd.getPosition(true);
		if (p1==null || p2==null) return null;

		final int x=(p1.x+p2.x)/2;
		final int y=(p1.y+p2.y)/2;
		if (readOnly) {
			if (readOnlyMiddle==null) return readOnlyMiddle=new Point(x,y);
			readOnlyMiddle.x=x;
			readOnlyMiddle.y=y;
			return readOnlyMiddle;
		} else {
			return new Point(x,y);
		}
	}

	@Override
	protected boolean canSetDeleteProtection() {
		return false;
	}

	@Override
	public void search(final FullTextSearch searcher) {
		if (searcher.isTestIDs()) {
			searcher.testInteger(this,Language.tr("Editor.DialogBase.Search.ID"),getId());
		}
		/* Nicht nach Name und Beschreibung suchen, daher kein super-Aufruf. */
	}
}