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
package ui.modeleditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.simparser.symbols.CalcSymbolClientUserData;
import tools.SetupData;
import ui.AnimationPanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.elements.ElementWithNewClientNames;
import ui.modeleditor.elements.ElementWithNewVariableNames;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSignalTrigger;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.fastpaint.GradientFill;
import xml.XMLData;

/**
 * Diese Klasse enth�lt die Elemente des Modells.
 * @author Alexander Herzog
 */
public final class ModelSurface {
	/**
	 * Standardhintergrundfarbe (unten)
	 */
	public final static Color DEFAULT_BACKGROUND_COLOR=new Color(255,255,250);

	/**
	 * Standardhintergrundfarbe (oben)
	 */
	public final static Color DEFAULT_BACKGROUND_GRADIENT_COLOR=new Color(253,245,230);

	/**
	 * Standardfarbe des Rasters
	 */
	public final static Color DEFAULT_RASTER_COLOR=new Color(240,240,240);

	/**
	 * Zeitbasis f�r Bedien-, Transport-, Verz�gerungs- usw. Zeiten.
	 * @author Alexander Herzog
	 */
	public enum TimeBase {
		/** Zeitbasis: Sekunden */
		TIMEBASE_SECONDS(0),

		/** Zeitbasis: Minuten */
		TIMEBASE_MINUTES(1),

		/** Zeitbasis: Stunden */
		TIMEBASE_HOURS(2);

		/**
		 * Interne ID des Zeitbasis-Wertes
		 * @see TimeBase#byId(int)
		 */
		public final int id;

		/**
		 * Multiplikator-Faktor um auf einen Sekundenwert zu kommen.
		 * @see ModelSurface#TIMEBASE_MULTIPLY
		 */
		public final long multiply;

		TimeBase(final int id) {this.id=id; multiply=TIMEBASE_MULTIPLY[id];}

		/**
		 * Liefert eine Zeitbasis auf Basis einer ID
		 * @param id	ID f�r die das Zeitbasis-Objekt ermittelt werden solle
		 * @return	Zeitbasis-Objekt, das zu der ID passt. Es wird immer ein Objekt geliefert (Fallback: Sekunden), nie <code>null</code>.
		 */
		public static TimeBase byId(final int id) {
			switch (id) {
			case 0: return TIMEBASE_SECONDS;
			case 1: return TIMEBASE_MINUTES;
			case 2: return TIMEBASE_HOURS;
			default: return TimeBase.TIMEBASE_SECONDS;
			}
		}
	}

	/**
	 * Rasteranzeige auf der Zeichenfl�che
	 * @author Alexander Herzog
	 * @see ModelSurface#drawToGraphics(Graphics, Rectangle, double, boolean, boolean, Grid, Color[], boolean)
	 */
	public enum Grid {
		/** Keine Rasteranzeige */
		OFF("off"),
		/** Punktraster anzeigen */
		DOTS("dots"),
		/** Linienraster anzeigen */
		LINES("raster");

		/** ID der Raster-Anzeige-Art f�r das Setup */
		public final String id;

		Grid(final String id) {
			this.id=id;
		}
	}

	/**
	 * Multiplikatoren (relativ zu einer Sekunde) f�r die Zeitbasis-Werte
	 */
	private final static long[] TIMEBASE_MULTIPLY=new long[]{1,60,3600};

	/**
	 * Name des XML-Elements, das die Surface-Elemente enth�lt
	 */
	public static String[] XML_NODE_NAME=new String[]{"ModellElemente"}; /* wird dynamisch mit Sprachdaten geladen, siehe LanguageStaticLoader */

	private final List<ModelElement> elements;
	private final List<Runnable> redrawListeners;
	private final List<Runnable> requestCopyListeners;
	private final List<Runnable> requestCutListeners;
	private final List<Runnable> stateChangeListener;
	private final EditModel model;
	private final ModelSurface parentSurface;
	private final ModelResources resources;
	private final ModelSchedules schedules;

	private ModelElement saveLastSelected;
	private ModelElementPosition saveLastSelectedPosition;

	private AnimationPanel animationPanel;

	private final List<String> layers;
	private final List<String> visibleLayers;
	private String activeLayer;

	/**
	 * Konstruktor der Klasse <code>ModelSurface</code>
	 * @param model	Element vom Typ <code>EditModel</code> (wird ben�tigt, um die Liste der globalen Variablen zu laden)
	 * @param resources Zu verwendendes Ressourcen-Objekt (kann �ber <code>getResources()</code> wieder abgerufen werden, wird sonst von diesem Objekt nicht verwendet)
	 * @param schedules Zu verwendendes Zeitpl�ne-Objekt (kann �ber <code>getSchedules()</code> wieder abgerufen werden, wird sonst von diesem Objekt nicht verwendet)
	 * @param parentSurface	�bergeordnetes <code>ModelSurface</code>-Element (zur Bestimmung der n�chsten freien id), kann <code>null</code> sein, wenn dies das prim�re Surface ist.
	 */
	public ModelSurface(final EditModel model, final ModelResources resources, final ModelSchedules schedules, final ModelSurface parentSurface) {
		elements=new ArrayList<>();
		redrawListeners=new ArrayList<>();
		requestCopyListeners=new ArrayList<>();
		requestCutListeners=new ArrayList<>();
		stateChangeListener=new ArrayList<>();
		this.model=model;
		this.resources=resources;
		this.schedules=schedules;
		this.parentSurface=parentSurface;

		layers=new ArrayList<>();
		visibleLayers=new ArrayList<>();
		activeLayer=null;
	}

	/**
	 * L�scht die komplette Zeichenfl�che.
	 */
	public void clear() {
		elements.clear();
		fireStateChangeListener();

		layers.clear();
		visibleLayers.clear();
		activeLayer=null;
	}

	/**
	 * Namen (f�r die xml-Dateien und die Dialoge) f�r die Zeitbasis-Werte
	 */

	/**
	 * Liefert eine Liste mit den Zeitbasis-Namen in der aktuellen Sprache
	 * (f�r Comboboxen)
	 * @return	Liste mit den Namen der Zeitbasis-Optionen
	 */
	public static String[] getTimeBaseStrings() {
		return new String[] {
				Language.trPrimary("Surface.XML.TimeBase.Seconds"),
				Language.trPrimary("Surface.XML.TimeBase.Minutes"),
				Language.trPrimary("Surface.XML.TimeBase.Hours")
		};
	}

	/**
	 * Wandelt einen Zeitbasis-Wert (siehe {@link TimeBase}) in einen Namen in der aktuellen Sprache um
	 * @param timeBase	Zeitbasis-Wert (siehe {@link TimeBase})
	 * @return	Name der Zeitbasis in der aktuellen Sprache
	 */
	public static String getTimeBaseString(final TimeBase timeBase) {
		switch (timeBase) {
		case TIMEBASE_SECONDS: return Language.trPrimary("Surface.XML.TimeBase.Seconds");
		case TIMEBASE_MINUTES: return Language.trPrimary("Surface.XML.TimeBase.Minutes");
		case TIMEBASE_HOURS: return Language.trPrimary("Surface.XML.TimeBase.Hours");
		default: return Language.trPrimary("Surface.XML.TimeBase.Seconds");
		}
	}

	/**
	 * Wandelt einen Zeitbasis-Namen (egal in welcher Sprache) in einen Zeitbasis-Wert (siehe {@link TimeBase}) um
	 * @param timeBaseName	Zeitbasis-Name
	 * @return	Zeitbasis-Wert (siehe {@link TimeBase})
	 */
	public static TimeBase getTimeBaseInteger(final String timeBaseName) {
		if (timeBaseName==null || timeBaseName.isEmpty()) return TimeBase.TIMEBASE_SECONDS;
		if (Language.trAll("Surface.XML.TimeBase.Seconds",timeBaseName)) return TimeBase.TIMEBASE_SECONDS;
		if (Language.trAll("Surface.XML.TimeBase.Minutes",timeBaseName)) return TimeBase.TIMEBASE_MINUTES;
		if (Language.trAll("Surface.XML.TimeBase.Hours",timeBaseName)) return TimeBase.TIMEBASE_HOURS;
		return TimeBase.TIMEBASE_SECONDS;
	}

	/**
	 * F�gt ein <code>Runnable</code>-Objekt zu der Liste der Listener, die beim �ndern des
	 * Modells aufgerufen werden sollen, hinzu.
	 * @param listener	Bei �nderungen am Modell aufzurufender Listener
	 */
	public void addRedrawListener(final Runnable listener) {
		if (redrawListeners.indexOf(listener)<0) redrawListeners.add(listener);
	}

	/**
	 * Entfernt ein <code>Runnable</code>-Objekt aus der Liste der Listener, die beim �ndern
	 * des Modell aufgerufen werden sollen.
	 * @param listener	Listener, der nicht mehr �ber �nderungen an dem Modell benachrichtigt werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte.
	 */
	public boolean removeRedrawListener(final Runnable listener) {
		final int index=redrawListeners.indexOf(listener);
		if (index<0) return false;
		redrawListeners.remove(listener);
		return true;
	}

	/**
	 * Benachrichtigt alle Listener, dass sich das Model ver�ndert hat.<br>
	 * Beim Hinzuf�gen und Entfernen von Elementen wird diese Methode automatisch aufgerufen. �ndert sich ein
	 * Element, so muss es diese Methode aufrufen um das <code>ModelSurface</code>-Element �ber die �nderungen
	 * zu benachrichtigen.
	 */
	public void fireRedraw() {
		for (Runnable listener: redrawListeners) listener.run();
	}

	/**
	 * F�gt ein Element zu dem Model hinzu.
	 * @param element	Hinzuzuf�gendes Element
	 */
	public void add(final ModelElement element) {
		elements.add(element);
		element.addedToSurface();
		if (activeLayer!=null && !activeLayer.trim().isEmpty()) element.getLayers().add(activeLayer);
		fireRedraw();
		fireStateChangeListener();
	}

	/**
	 * Liefert die Anzahl an Elementen, aus denen das Modell besteht.
	 * @return	Anzahl der Elemente in dem Modell
	 * @see #getElementCount()
	 */
	public int count() {
		return elements.size();
	}

	/**
	 * Liefert ein bestimmte Element basierend auf dem Index zur�ck.
	 * @param index	Index des Elements, welches geliefert werden soll
	 * @return	Gibt im Erfolgsfall das Element zur�ck oder <code>null</code>, wenn der Index au�erhalb des g�ltigen Bereichs liegt
	 * @see count
	 */
	/*
	public ModelElement get(final int index) {
		if (index<0 || index>=elements.size()) return null;
		return elements.get(index);
	}
	 */

	/**
	 * Liefert ein bestimmte Element basierend auf der ID des Elements zur�ck.
	 * @param id	ID des Elements, welches geliefert werden soll
	 * @return	Gibt im Erfolgsfall das Element zur�ck oder <code>null</code>, wenn es kein Element mit der angegebenen ID gibt.
	 */
	public ModelElement getById(final int id) {
		if (id<0) return null;
		for (ModelElement element: elements) if (element.getId()==id) return element;
		return null;
	}

	/**
	 * Liefert ein bestimmte Element basierend auf der ID des Elements zur�ck.
	 * @param id	ID des Elements, welches geliefert werden soll
	 * @return	Gibt im Erfolgsfall das Element zur�ck oder <code>null</code>, wenn es kein Element mit der angegebenen ID gibt.
	 */
	public ModelElement getByIdIncludingSubModels(final int id) {
		if (id<0) return null;
		for (ModelElement element: elements) {
			if (element.getId()==id) return element;
			if (element instanceof ModelElementSub) {
				final ModelElement element2=((ModelElementSub)element).getSubSurface().getById(id);
				if (element2!=null) return element2;
			}
		}
		return null;
	}

	/**
	 * Liefert den Index des angegebenen Elements im Modell.
	 * @param element	Element dessen Index abgefragt werden soll
	 * @return	Index des Elements in dem Modell oder -1, wenn das Element nicht im Modell vorhanden ist
	 */
	public int indexOf(final ModelElement element) {
		if (element==null) return -1;
		return elements.indexOf(element);
	}

	/**
	 * Entfernt ein bestimmtes Element basierend auf dem Index aus dem Modell.
	 * @param index	Index des Elements, welches entfernt werden soll
	 * @return	Gibt im Erfolgfall <code>true</code> zur�ck. Wenn der Index au�erhalb des g�ltigen Bereichs liegt <code>false</code>.
	 * @see #count()
	 */
	public boolean remove(final int index) {
		if (index<0 || index>=elements.size()) return false;
		if (elements.get(index).isDeleteProtected()) return false;
		ModelElement removedElement=elements.remove(index);
		removedElement.removeNotify();
		fireRedraw();
		fireStateChangeListener();
		return true;
	}

	/**
	 * Entfernt ein bestimmte Element aus dem Modell.
	 * @param element	Zu entfernendes Element
	 * @return	Gibt im Erfolgsfall <code>true</code> zur�ck. Wenn das angegebene Element nicht in dem Modell enthalten war <code>false</code>.
	 */
	public boolean remove(final ModelElement element) {
		final int index=elements.indexOf(element);
		if (index<0) return false;
		if (element.isDeleteProtected()) return false;
		elements.remove(element);
		element.removeNotify();
		fireRedraw();
		fireStateChangeListener();
		return true;
	}

	/**
	 * Liefert eine Kopie des kompletten Modells.
	 * @param copyRedrawListeners	Gibt an, ob die in dem Ausgangsmodell hinterlegten Redraw-Listener auch in der Kopie eingetragen werden sollen
	 * @param resources	Resourcenliste, die f�r die Kopie verwendet werden soll
	 * @param schedules	Zeitpl�ne, die f�r die Kopie verwendet werden sollen
	 * @param parentSurface	�bergeordnete Zeichenfl�che
	 * @param model	Editor-Modell
	 * @return	Kopie des Ausgangsmodells
	 */
	public ModelSurface clone(final boolean copyRedrawListeners, final ModelResources resources, final ModelSchedules schedules, final ModelSurface parentSurface, final EditModel model) {
		final ModelSurface clonedModelSurface=new ModelSurface((model==null)?this.model:model,resources,schedules,parentSurface);

		final EditModel m=(model==null)?this.model:model;
		clonedModelSurface.turnOffIDScanner=true;
		try {
			for (ModelElement element : elements) {
				final ModelElement clone=element.clone(m,clonedModelSurface);
				clonedModelSurface.elements.add(clone);
				clone.addedToSurface();
				/* langsamer: clonedModelSurface.add(element.clone(m,clonedModelSurface)); */
			}
		} finally {
			clonedModelSurface.turnOffIDScanner=false;
		}

		clonedModelSurface.layers.addAll(layers);
		clonedModelSurface.visibleLayers.addAll(visibleLayers);
		clonedModelSurface.activeLayer=activeLayer;

		clonedModelSurface.initAfterLoad();
		if (copyRedrawListeners) {
			for (Runnable listener : redrawListeners) clonedModelSurface.addRedrawListener(listener);
			for (Runnable listener: requestCopyListeners) clonedModelSurface.addRequestCopyListener(listener);
			for (Runnable listener: requestCutListeners) clonedModelSurface.addRequestCutListener(listener);
		}
		return clonedModelSurface;
	}

	/**
	 * Optionale Initialisierungen der Elemente nach dem Laden bzw. Clonen.<br>
	 * Von <code>clone</code> wird diese Methode bereits aufgerufen.
	 */
	public void initAfterLoad() {
		int i;

		/* Elemente initialisieren */
		i=0;
		while (i<elements.size()) {
			final ModelElement element=elements.get(i);
			element.initAfterLoadOrClone();
			if (elements.indexOf(element)>=0) i++;
		}

		/* Wenn Kanten mit Elementen verkn�pft sind, die davon nichts wissen, die jeweilige Kante entfernen */
		i=0;
		while (i<elements.size()) {
			final ModelElement element=elements.get(i);
			if (!(element instanceof ModelElementEdge)) {i++; continue;}
			final ModelElementEdge edge=(ModelElementEdge)element;
			if (edge.isConnectionOk()) {i++; continue;}
			remove(edge);
		}
	}

	/**
	 * Pr�ft, ob das Modell mit dem angegebenen Modell �bereinstimmt.
	 * @param surface	Modell, welches mit dem Ausgangsmodell verglichen werden soll
	 * @return	Gibt <code>true</code> zur�ck, wenn beide Modell exakt �bereinstimmen.
	 */
	public boolean equalsModelSurface(final ModelSurface surface) {
		return equalsModelSurface(surface,false);
	}

	/**
	 * Pr�ft, ob das Modell mit dem angegebenen Modell �bereinstimmt.
	 * @param surface	Modell, welches mit dem Ausgangsmodell verglichen werden soll
	 * @param ignoreAnimationConnect	Sollen am Ende angef�gte {@link ModelElementAnimationConnect}-Elemente ignoriert werden?
	 * @return	Gibt <code>true</code> zur�ck, wenn beide Modell exakt �bereinstimmen.
	 */
	public boolean equalsModelSurface(final ModelSurface surface, final boolean ignoreAnimationConnect) {
		final int size1=elements.size();
		final int size2=surface.elements.size();
		final int size;
		if (ignoreAnimationConnect) {
			final int delta=Math.abs(size1-size2);
			if (delta>1) return false;
			if (delta==1) {
				if (size1<size2) {
					if (!(surface.elements.get(size2-1) instanceof ModelElementAnimationConnect)) return false;
				} else {
					if (!(elements.get(size1-1) instanceof ModelElementAnimationConnect)) return false;
				}
			}
			size=Math.min(size1,size2);
		} else {
			if (size1!=size2) return false;
			size=size1;
		}
		for (int i=0;i<size;i++) {
			if (!elements.get(i).equalsModelElement(surface.elements.get(i))) return false;
		}

		if (!Objects.deepEquals(layers,surface.layers)) return false;
		if (!Objects.deepEquals(visibleLayers,surface.visibleLayers)) return false;
		if (!Objects.equals(activeLayer,surface.activeLayer)) return false;

		return true;
	}

	private final GradientFill gradient=new GradientFill();

	private void drawBackgroundToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showBackground, final boolean showBoundingBox, final Grid raster, final Color[] colors) {
		Color backgroundColor=DEFAULT_BACKGROUND_COLOR;
		Color backgroundColorGradient=null;
		Color rasterColor=DEFAULT_RASTER_COLOR;
		if (colors!=null && colors.length>=2) {
			if (colors[0]!=null) backgroundColor=colors[0];
			if (colors[1]!=null) rasterColor=colors[1];
			if (colors.length==3) backgroundColorGradient=colors[2];
		}

		/* Hintergrund */
		if (showBackground) {
			if (backgroundColorGradient==null || SetupData.getSetup().useHighContrasts) {
				graphics.setColor(backgroundColor);
			} else {
				gradient.set(graphics,drawRect,backgroundColor,backgroundColorGradient,true);
			}
			graphics.fillRect(drawRect.x,drawRect.y,drawRect.width,drawRect.height);
		} else {
			if (showBoundingBox) {
				graphics.setColor(Color.WHITE);
				graphics.fillRect(drawRect.x,drawRect.y,drawRect.width,drawRect.height);
			}
		}

		/* Raster */
		graphics.setColor(rasterColor);
		final int step=(int)FastMath.round(50*zoom);
		final int pointSize=(int)FastMath.round(3*zoom);
		final int xStart=(int)FastMath.round(FastMath.ceil(((double)drawRect.x)/step)*step);
		final int xEnd=(int)FastMath.round(FastMath.floor(((double)(drawRect.x+drawRect.width))/step)*step);
		final int yStart=(int)FastMath.round(FastMath.ceil(((double)drawRect.y)/step)*step);
		final int yEnd=(int)FastMath.round(FastMath.floor(((double)(drawRect.y+drawRect.height))/step)*step);
		switch (raster) {
		case OFF:
			/* Kein Raster */
			break;
		case DOTS:
			for (int x=xStart;x<=xEnd;x+=step) for (int y=yStart;y<=yEnd;y+=step) {
				graphics.drawLine(x-pointSize,y,x+pointSize,y);
				graphics.drawLine(x,y-pointSize,x,y+pointSize);
			}
			break;
		case LINES:
			for (int x=xStart;x<=xEnd;x+=step) graphics.drawLine(x,drawRect.y,x,drawRect.y+drawRect.height);
			for (int y=yStart;y<=yEnd;y+=step) graphics.drawLine(drawRect.x,y,drawRect.x+drawRect.width,y);
			break;
		}
	}

	/**
	 * Gibt an, ob eine bestimmte Id in diesem oder einem untergeordneten Element vergeben ist
	 * @param id	Zu pr�fende Id
	 * @return	Gibt <code>true</code> zur�ck, wenn die ID in dem aktuellen und den untergeordneten Elementen noch nicht vergeben ist
	 */
	public boolean isFreeId(final int id) {
		for (ModelElement element: elements) {
			if (element.getId()==id) return false;
			if (element instanceof ModelElementSub) if (!((ModelElementSub)element).isFreeId(id)) return false;
		}
		return true;
	}

	private boolean turnOffIDScanner=false;

	/**
	 * Liefert die n�chste freie ID.<br>
	 * Die Pr�fung erfolgt dabei global, d.h. es wird zun�chst die Kette zum prim�ren Surface verfolgt und von diesem aus gepr�ft, welche IDs bereits vergeben sind
	 * @return	N�chste globale freie ID
	 */
	public int getNextFreeId() {
		if (turnOffIDScanner) return 1; /* Wenn das Element nur tempor�r initialisiert wird und gleich sowieso alles biem Laden �berschrieben wird, brauchen wir auch keine ID zu suchen. */
		int nextFreeId=1;
		while (true) {
			boolean ok=true;
			ModelSurface surface=this;
			while (surface!=null) {
				if (!surface.isFreeId(nextFreeId)) {ok=false; break;}
				surface=surface.getParentSurface();
			}
			if (ok) return nextFreeId;
			nextFreeId++;
		}
	}

	/**
	 * Liefert das �bergeordnete <code>ModelSurface</code>-Elemente
	 * @return	�bergeordnetes <code>ModelSurface</code>-Elemente oder <code>null</code>, wenn dies das prim�re Surface ist
	 */
	public ModelSurface getParentSurface() {
		return parentSurface;
	}

	/**
	 * Pr�ft, ob ein Element auf der Zeichenfl�che sichtbar ist.
	 * @param element	Zu pr�fendes Element (Kanten werden relativ zu ihren zugeh�rigen Ecken bewertet)
	 * @return	Gibt an, ob das Element sichtbar sein soll.
	 * @see ModelElement#isVisibleOnLayer(List, List)
	 * @see #getLayers()
	 * @see #getVisibleLayers()
	 */
	public boolean isVisibleOnLayer(final ModelElement element) {
		if (element==null) return false;
		if (element instanceof ModelElementEdge) {
			final ModelElementEdge edge=(ModelElementEdge)element;
			final ModelElement e1=edge.getConnectionStart();
			final ModelElement e2=edge.getConnectionEnd();
			if (e1==null || e2==null) return true;
			return e1.isVisibleOnLayer(layers,visibleLayers) && e2.isVisibleOnLayer(layers,visibleLayers);
		} else {
			return element.isVisibleOnLayer(layers,visibleLayers);
		}
	}

	/**
	 * Zeichnet die Elemente in ein <code>Graphics</code>-Objekt
	 * @param graphics	<code>Graphics</code>-Objekt, in das die Elemente gezeichnet werden sollen
	 * @param drawRect	Tats�chlich sichtbarer Ausschnitt
	 * @param zoom	Zoomfaktor
	 * @param showBackground	Hintergrund anzeigen?
	 * @param showBoundingBox	Wenn Hintergrund aus: Wenigstens wei�en Kasten? (bei svg-Export: aus, sonst: an)
	 * @param raster	Raster anzeigen?
	 * @param colors	2- oder 3-elementiges Array aus Hintergrund-, Raster- und optional oberer Gradienthintergrundfarbe
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgew�hlt ist
	 */
	public void drawToGraphics(final Graphics graphics, final Rectangle drawRect, final double zoom, final boolean showBackground, final boolean showBoundingBox, final Grid raster, final Color[] colors, final boolean showSelectionFrames) {
		if (graphics==null) return;

		delayFireStateChangeListener=true;
		needToFireStateChangeListener=false;
		try {
			drawBackgroundToGraphics(graphics,drawRect,zoom,showBackground,showBoundingBox,raster,colors);
			/* final Rectangle smallerDrawRect=new Rectangle(drawRect.x,drawRect.y,drawRect.width-1,drawRect.height-1); */ /* sonst gibt's beim initialen Zeichnen evtl. eine Pixelzeile unter dem Scrollbalken */
			drawRect.width--;
			drawRect.height--;
			if (graphics!=null) for (ModelElement element : elements) if (isVisibleOnLayer(element)) {
				element.drawToGraphics(graphics,/*smallerDrawRect*/drawRect,zoom,showSelectionFrames);
			}
			drawRect.width++;
			drawRect.height++;
		} finally {
			delayFireStateChangeListener=false;
			if (needToFireStateChangeListener) {
				needToFireStateChangeListener=false;
				fireStateChangeListener();
			}
		}
	}

	/**
	 * Liefert das aktuelle selektierte Element
	 * @return	Aktuell selektiertes Element oder <code>null</code>, falls nichts selektiert ist.
	 */
	public ModelElement getSelectedElement() {
		for (ModelElement element : elements) if (element.isSelected()) return element;
		return null;
	}

	/**
	 * Liefert das zuletzt selektierte Element zur�ck (dieses muss momentan nicht mehr selektiert sein)
	 * @return	Zuletzt selektiertes Element
	 */
	public ModelElement getLastSelectedElement() {
		if (!elements.contains(saveLastSelected)) saveLastSelected=null;
		return saveLastSelected;
	}

	/**
	 * Liefert das zuletzt selektierte Element vom Typ <code>ModelElementBox</code> zur�ck (dieses muss momentan nicht mehr selektiert sein)
	 * @return	Zuletzt selektiertes Element vom Typ <code>ModelElementBox</code>
	 */
	public ModelElement getLastSelectedPositionElement() {
		if (!elements.contains(saveLastSelectedPosition)) saveLastSelectedPosition=null;
		return saveLastSelectedPosition;
	}

	/**
	 * Stellt das aktuell selektierte Element ein.<br>
	 * Die bisherige Selektion (sofern vorhanden) wird damit aufgehoben.
	 * @param element	Neues selektiertes Element (kann auch <code>null</code> sein, wenn nichts selektiert werden soll).
	 */
	public void setSelectedElement(final ModelElement element) {
		ModelElement selectedElement=getSelectedElement();
		if (element==selectedElement) return;
		if (selectedElement!=null) selectedElement.setSelected(false);

		if (element==null) return;

		if (isVisibleOnLayer(element)) {
			if (indexOf(element)>=0) element.setSelected(true);
			saveLastSelected=element;
			if (element instanceof ModelElementPosition) saveLastSelectedPosition=(ModelElementPosition)element;
		}
	}

	/**
	 * W�hlt mehrere Elemente basierend auf einem Zeichenbereich aus.
	 * @param area	Auszuw�hlender Bereich (oder <code>null</code>, wenn nichts ausgew�hlt werden soll)
	 * @param zoom	Zoomfaktor
	 */
	public void setSelectedArea(final Rectangle area, final double zoom) {
		Rectangle zoomArea;
		if (area==null) {
			zoomArea=null;
		} else {
			zoomArea=new Rectangle((int)FastMath.round(area.x/zoom),(int)FastMath.round(area.y/zoom),(int)FastMath.round(area.width/zoom),(int)FastMath.round(area.height/zoom));
		}

		for (ModelElement element : elements) if (isVisibleOnLayer(element)) element.setSelectedArea(zoomArea);
	}

	/**
	 * W�hlt alle Elemente aus.
	 */
	public void setSelectedAreaAll() {
		for (ModelElement element : elements) if (isVisibleOnLayer(element)) element.setSelectedArea(true);
	}

	/**
	 * Liefert eine Liste der Elemente, die �ber eine Bereichsselektion markiert sind.
	 * @param addRegularSelectedElement	Auch die normal ausgew�hlten Objekte hinzuf�gen?
	 * @return	Liste der markierten Elemente (ist nie <code>null</code>, kann aber leer sein)
	 */
	public List<ModelElement> getSelectedArea(final boolean addRegularSelectedElement) {
		final List<ModelElement> list=new ArrayList<>();
		for (ModelElement element : elements) if (element.isSelectedArea() || (addRegularSelectedElement && element.isSelected())) list.add(element);
		return list;
	}

	/**
	 * Liefert das Element an der angegebenen Position (in Pixeln) zur�ck.
	 * @param point	Pixel-Position, die abgefragt werden soll.
	 * @param zoom	Zoomfaktor
	 * @return	Liefert das Element an dieser Position oder <code>null</code>, wenn sich nichts an dieser Position befindet.
	 */
	public ModelElement getElementAtPosition(final Point point, final double zoom) {
		/* Umgekehrte Reihenfolge, um das optisch oberste Element (=am Ende der Liste) zuerst zu testen */
		for (int i=elements.size()-1;i>=0;i--) if (elements.get(i).containsPoint(point,zoom)) return elements.get(i);
		return null;
	}

	/**
	 * Liefert den Mittelpunkt des Modells
	 * @return	Mittelpunkt des Modells
	 * @see #getUpperLeftModelCorner()
	 * @see #getLowerRightModelCorner()
	 */
	public Point getModelCenter() {
		Point sum=new Point(0,0);
		int count=0;
		for (ModelElement element : elements) {
			final Point point=element.getMiddlePosition(true);
			if (point!=null) {sum.translate(point.x,point.y); count++;}
		}
		if (count==0) return new Point(100,100); else return new Point(sum.x/count,sum.y/count);
	}

	/**
	 * Liefert den unteren rechten Rand des Modells
	 * @return Unterer rechter Rand des Modells
	 * @see #getModelCenter()
	 * @see #getLowerRightModelCorner()
	 */
	public Point getUpperLeftModelCorner() {
		Point upperLeft=new Point(ModelSurfacePanel.SIZE,ModelSurfacePanel.SIZE);
		for (ModelElement element : elements) {
			final Point point=element.getPosition(true);
			if (point!=null) {
				upperLeft.x=FastMath.min(upperLeft.x,point.x);
				upperLeft.y=FastMath.min(upperLeft.y,point.y);
			}
		}

		if (upperLeft.x==ModelSurfacePanel.SIZE) upperLeft.x=0;
		if (upperLeft.y==ModelSurfacePanel.SIZE) upperLeft.y=0;

		return upperLeft;
	}

	/**
	 * Liefert den unteren rechten Rand des Modells
	 * @return Unterer rechter Rand des Modells
	 * @see #getModelCenter()
	 * @see #getUpperLeftModelCorner()
	 */
	public Point getLowerRightModelCorner() {
		Point lowerRight=new Point(0,0);
		for (ModelElement element : elements) {
			final Point point=element.getLowerRightPosition();
			if (point!=null) {
				lowerRight.x=FastMath.max(lowerRight.x,point.x);
				lowerRight.y=FastMath.max(lowerRight.y,point.y);
			}
		}
		return lowerRight;
	}

	/**
	 * Speichert das gesamte Surface in einem xml-Knoten
	 * @param doc	�bergeordnetes xml-Dokument
	 * @param parent	�bergeordneter Knoten
	 */
	public void addDataToXML(final Document doc, final Element parent) {
		final Element node=doc.createElement(XML_NODE_NAME[0]);
		parent.appendChild(node);

		for (ModelElement element : elements) element.addDataToXML(doc,node);

		for (String layer: layers) {
			final Element sub=doc.createElement(Language.tr("Surface.XML.Layer"));
			node.appendChild(sub);
			sub.setTextContent(layer);
			if (visibleLayers.contains(layer)) sub.setAttribute(Language.tr("Surface.XML.Layer.Visible"),"1");
			if (Objects.equals(layer,activeLayer)) sub.setAttribute(Language.tr("Surface.XML.Layer.NewElements"),"1");
		}
	}

	private ModelElementCatalog catalog=null;
	private String[] edgeLoadNames=null;

	private ModelElement findElementType(final String name) {
		/* Kante */
		if (edgeLoadNames==null) edgeLoadNames=ModelElementEdge.getXMLNodeNamesStatic();
		for (String langname: edgeLoadNames) if (name.equalsIgnoreCase(langname)) return new ModelElementEdge(model,this,null,null);

		/* Element */
		if (catalog==null) catalog=ModelElementCatalog.getCatalog(); /* Nicht beim Feld initialisieren, sonst Stack Overflow. */
		final ModelElement template=catalog.getXMLElement(name);
		if (template!=null) return template;

		/* Unbekannt */
		return null; /* unbekannte Elemente ignorieren wir - return String.format("Die XML-Element-Bezeichnung \"%s\" ist unbekannt.",name); */
	}

	/**
	 * Versucht ein Modell-Element aus einem gegebenen xml-Element zu laden
	 * @param name	Name des XML-Elements
	 * @param node	XML-Element, aus dem das Modell-Element geladen werden soll
	 * @return Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	private String loadElementFromXML(final String name, final Element node) {
		ModelElement element=null;

		if (Language.trAll("Surface.XML.Layer",name)) {
			final String layer=node.getTextContent();
			if (!layer.trim().isEmpty()) {
				layers.add(layer);
				final String visible=Language.trAllAttribute("Surface.XML.Layer.Visible",node);
				if (!visible.isEmpty() && !visible.equals("0")) visibleLayers.add(layer);
				final String newElements=Language.trAllAttribute("Surface.XML.Layer.NewElements",node);
				if (!newElements.isEmpty() && !newElements.equals("0")) activeLayer=layer;
			}
			return null;
		}

		turnOffIDScanner=true;
		try {
			final ModelElement template=findElementType(name);
			if (template==null) {
				unknownElementsOnLoad=true;
				if (parentSurface!=null) parentSurface.unknownElementsOnLoad=true;
				return null;
			}

			element=template.clone(model,this);
			if (parentSurface!=null && !element.canAddToSub()) return null; /* Pr�fen, ob wir in Sub-Element sind und ggf. Hinzuf�gen von nicht-sub-tauglichen Elementen verbieten. */
		} finally {
			turnOffIDScanner=false;
		}

		String error=element.loadFromXML(node);
		if (error!=null) return error;


		if (parentSurface==null) {
			if (!isFreeId(element.getId())) return null;
		} else {
			if (!parentSurface.isFreeId(element.getId())) return null;
		}

		elements.add(element);
		element.addedToSurface();

		return null;
	}

	private boolean unknownElementsOnLoad=false;

	/**
	 * Gibt nach dem Laden eines Modell an, ob alle Elemente vollst�ndig geladen werden konnten,
	 * oder ob es unbekannte Elemente gab. Im Fall von unbekannten Elementen gibt
	 * {@link ModelSurface#loadElementFromXML(String, Element)} trotzdem <code>null</code> zur�ck,
	 * so dass diese Methode ben�tigt wird, um abzufragen, ob XML-Elemente beim Laden �bersprungen
	 * wurden. Der "unbekannte Elemente"-Marker wird vor dem Laden zur�ckgesetzt und beim
	 * Kopieren dieses Elements nicht mitkopiert.
	 * @return	Gibt <code>true</code> zur�ck, wenn beim Laden Elemente �bersprungen wurden.
	 * @see ModelSurface#loadElementFromXML(String, Element)
	 */
	public boolean isUnknownElementsOnLoad() {
		return unknownElementsOnLoad;
	}

	/**
	 * Versucht die Elemente des Surface aus einem xml-Element zu laden
	 * @param node	XML-Element, das die Modell-Elemente als Kind-Elemente beinhaltet
	 * @return	Tritt ein Fehler auf, so wird die Fehlermeldung als String zur�ckgegeben. Im Erfolgsfall wird <code>null</code> zur�ckgegeben.
	 */
	public String loadFromXML(final Element node) {
		clear();

		unknownElementsOnLoad=false;

		/* Laden */
		final NodeList l=node.getChildNodes();
		final int length=l.getLength();
		for (int i=0;i<length;i++) {
			final Node sub=l.item(i);
			if (!(sub instanceof Element)) continue;
			final Element e=(Element)sub;
			final String error=loadElementFromXML(e.getNodeName(),e);
			if (error!=null) return error;
		}

		/* Verkn�pfen (�bergang von IDs zu Referenzen) */
		delayFireStateChangeListener=true;
		try {
			boolean needReRun=true;
			while (needReRun) {
				final int size=elements.size();
				needReRun=false;
				for (int i=0;i<size;i++) {
					elements.get(i).initAfterLoadOrClone();
					if (elements.size()!=size) {needReRun=true; break;}
				}
			}
			/* Kann zu Fehlern f�hren, da initAfterLoadOrClone evtl. Element entfernt: for (ModelElement element : elements) element.initAfterLoadOrClone(); */
		} finally {
			delayFireStateChangeListener=false;
		}

		fireRedraw();
		fireStateChangeListener();

		return null;
	}

	private boolean getClientTypesRunning=false;

	/**
	 * Liefert eine Liste aller nicht-leeren Kundentypnamen, die von <code>ModelElementSource</code>- oder
	 * <code>ModelElementAssign</code>-Elementen generiert werden. Namen, die sich nur in der Gro�- und Kleinschreibung
	 * unterscheiden, werden dabei zusammengefasst.
	 * @return	Liste mit allen Kundentypnamen
	 */
	public List<String> getClientTypes() {
		if (parentSurface!=null && !getClientTypesRunning) {
			getClientTypesRunning=true;
			try {
				return parentSurface.getClientTypes();
			} finally {
				getClientTypesRunning=false;
			}
		}

		final List<String> clientTypes=new ArrayList<>();
		for (ModelElement element : elements) {
			String names[]=null;

			if (element instanceof ElementWithNewClientNames) names=((ElementWithNewClientNames)element).getNewClientTypes();

			if (names!=null) for (String name: names) if (name!=null && !name.isEmpty()) {
				boolean isNew=true;
				for (String s : clientTypes) if (s.equalsIgnoreCase(name)) {isNew=false; break;}
				if (isNew) clientTypes.add(name);
			}
		}
		return clientTypes;
	}

	/**
	 * Liefert eine Liste aller Modell-Elemente.
	 * @return	Liste mit allen Modell-Elementen
	 */
	public List<ModelElement> getElements() {
		return elements;
	}

	/**
	 * Liefert eine Liste aller Modell-Elemente auf dieser Zeichenfl�che und auch auf m�glichen Unter-Zeichenfl�chen.
	 * @return	Liste mit allen Modell-Elementen auf dieser und auf Unter-Zeichenfl�chen
	 */
	public List<ModelElement> getElementsIncludingSubModels() {
		final List<ModelElement> elements=new ArrayList<>();
		elements.addAll(this.elements);
		for (ModelElement element: this.elements) if (element instanceof ModelElementSub) {
			elements.addAll(((ModelElementSub)element).getSubSurface().getElements());
		}
		return elements;
	}

	/**
	 * Liefert das Ressourcen-Objekt f�r das Modell
	 * @return	Ressourcen-Objekt
	 */
	public ModelResources getResources() {
		return resources;
	}

	/**
	 * Liefert das Zeitpl�ne-Objekt f�r das Modell
	 * @return	Zeitpl�ne-Objekt
	 */
	public ModelSchedules getSchedules() {
		return schedules;
	}

	/**
	 * Liefert die Anzahl an Elementen (einschlie�lich der Elemente in Unterelementen)
	 * @return	Anzahl an Elementen insgesamt
	 * @see #count()
	 */
	public int getElementCount() {
		int count=0;
		for (ModelElement element: elements) {
			count++;
			if (element instanceof ModelElementSub) count+=((ModelElementSub)element).getSubSurface().getElementCount();
		}
		return count;
	}

	/**
	 * Gibt an, wie viele Elemente und Stationen auf der Zeichenfl�che enthalten sind.
	 * @return	2-elementiges Array aus Anzahl an Elementen insgesamt und Anzahl an Stationen
	 */
	public int[] getElementAndStationCount() {
		int countElements=0;
		int countStations=0;
		for (ModelElement element: elements) {
			countElements++;
			if (element instanceof ModelElementBox) countStations++;
			if (element instanceof ModelElementSub) {
				final int[] sub=((ModelElementSub)element).getSubSurface().getElementAndStationCount();
				countElements+=sub[0];
				countStations+=sub[1];
			}
		}
		return new int[]{countElements,countStations};
	}

	/**
	 * Liefert die gew�hlten Objekte in einer Zwischenablagen-tauglichen Form
	 * @param elements	Objekte, die in den Stream kopiert werden sollen
	 * @return	Stream, der alle angegebenen Objekte enth�lt
	 */
	public ByteArrayOutputStream getTransferData(final List<ModelElement> elements) {
		final ModelSurface temp=new ModelSurface(model,resources,schedules,null);
		for (ModelElement element : elements) {
			temp.add(element.clone(model,temp));
		}

		temp.initAfterLoad();

		final ByteArrayOutputStream stream=new ByteArrayOutputStream();

		final XMLData xml=new XMLData() {
			@Override protected String loadProperty(String name, String text, Element node) {return null;}
			@Override public String[] getRootNodeNames() {return new String[]{"Transfer"};}
			@Override protected void addDataToXML(Document doc, Element node, boolean isPartOfOtherFile, final File file) {
				temp.addDataToXML(doc,node);
			}
		};
		xml.saveToStream(stream);

		return stream;
	}

	/**
	 * Liefert eine Liste der gew�hlten Elemente (inkl. der Kanten, die diese evtl. verbinden)
	 * @return	Liste der gew�hlten Elemente (kann leer sein, ist aber nie <code>null</code>)
	 */
	public List<ModelElement> getSelectedElements() {
		final List<ModelElement> list=new ArrayList<>();
		for (ModelElement element : elements) {
			if (element.isSelected() || element.isSelectedArea()) list.add(element);
			if (element instanceof ModelElementEdge) {
				ModelElementEdge edge=(ModelElementEdge)element;
				if (edge.getConnectionStart()!=null && edge.getConnectionEnd()!=null && (edge.getConnectionStart().isSelected() || edge.getConnectionStart().isSelectedArea()) && (edge.getConnectionEnd().isSelected() || edge.getConnectionEnd().isSelectedArea())) list.add(edge);
			}
		}
		return list;
	}

	/**
	 * Liefert alle markierten Objekte in einer Zwischenablagen-tauglichen Form
	 * @return	Stream, der alle markierten Objekte enth�lt
	 */
	public ByteArrayOutputStream getTransferData() {
		return getTransferData(getSelectedElements());
	}

	private ModelDescriptionBuilderStyled getDescriptionBuilder() {
		final List<ModelElement> descriptionElements=new ArrayList<>();

		for (ModelElement element : elements) if (element.isSelected() || element.isSelectedArea()) {
			descriptionElements.add(element);
		}


		final ModelDescriptionBuilderStyled descriptionBuilder;
		if (descriptionElements.size()>0) {
			/* Gew�hlte Elemente */
			descriptionBuilder=new ModelDescriptionBuilderStyled(model,descriptionElements,true);
		} else {
			/* Alles */
			descriptionBuilder=new ModelDescriptionBuilderStyled(model);
		}

		descriptionBuilder.run();

		return descriptionBuilder;
	}

	/**
	 * Liefert alle markierten Objekte in einer Zwischenablagen-tauglichen Textform
	 * @return	Text, der Beschreibungen zu allen markierten Objekte enth�lt
	 */
	public String getTransferTxt() {
		return getDescriptionBuilder().getText();
	}

	/**
	 * Liefert alle markierten Objekte in einer Zwischenablagen-tauglichen RTF-Textform
	 * @return	Text, der Beschreibungen zu allen markierten Objekte enth�lt
	 */
	public String getTransferRtf() {
		return getDescriptionBuilder().getRTFText();
	}

	/**
	 * Liefert alle markierten Objekte in einer Zwischenablagen-tauglichen Bildform
	 * @return	Bild, das die markierten Objekte enth�lt
	 */
	public BufferedImage getTransferImage() {
		final Point p1=new Point(Integer.MAX_VALUE,Integer.MAX_VALUE);
		final Point p2=new Point(0,0);

		for (ModelElement element : elements) if (element.isSelected() || element.isSelectedArea()) {
			final Point a=element.getPosition(true);
			final Point b=element.getLowerRightPosition();
			if (a!=null && b!=null) {
				if (a.x<p1.x) p1.x=a.x;
				if (a.y<p1.y) p1.y=a.y;
				if (b.x>p2.x) p2.x=b.x;
				if (b.y>p2.y) p2.y=b.y;
			}
		}

		if (p1.x>=p2.x || p1.y>=p2.y) {
			/* Nichts ausgew�hlt */
			for (ModelElement element : elements) {
				final Point a=element.getPosition(true);
				final Point b=element.getLowerRightPosition();
				if (a!=null && b!=null) {
					if (a.x<p1.x) p1.x=a.x;
					if (a.y<p1.y) p1.y=a.y;
					if (b.x>p2.x) p2.x=b.x;
					if (b.y>p2.y) p2.y=b.y;
				}
			}
		}

		p1.x=Math.max(0,p1.x-5);
		p1.y=Math.max(0,p1.y-5);
		p2.x+=5;
		p2.y+=5;

		final BufferedImage image=new BufferedImage(p2.x,p2.y,BufferedImage.TYPE_INT_RGB);
		Graphics g=image.getGraphics();
		g.setClip(p1.x,p1.y,p2.x-p1.x,p2.y-p1.y);
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
		drawToGraphics(g,new Rectangle(p1.x,p1.y,p2.x-p1.x,p2.y-p1.y),1.0,false,true,Grid.OFF,null,false);

		return image.getSubimage(p1.x,p1.y,p2.x-p1.x,p2.y-p1.y);
	}

	private ModelSurface getSurfaceFromTransferData(final ByteArrayInputStream stream) {
		final ModelSurface temp=new ModelSurface(model,resources,schedules,null);

		final XMLData xml=new XMLData() {
			@Override public String[] getRootNodeNames() {return new String[]{"Transfer"};}
			@Override
			protected String loadProperty(String name, String text, Element node) {
				for (String test: XML_NODE_NAME) if (name.equals(test)) return temp.loadFromXML(node);
				return null;
			}
			@Override protected void addDataToXML(Document doc, Element node, boolean isPartOfOtherFile, final File file) {}
		};
		xml.loadFromStream(stream);

		return temp;
	}

	/**
	 * Liefert die h�chste im Modell auftretenden id
	 * @return	H�chste verwendete id
	 */
	public int getMaxId() {
		int maxId=0;
		for (ModelElement element: elements) {
			maxId=FastMath.max(maxId,element.getId());
			if (element instanceof ModelElementSub) maxId=FastMath.max(maxId,((ModelElementSub)element).getSubSurface().getMaxId());
		}
		return maxId;
	}

	/**
	 * F�gt die Daten aus einem in die Zwischenablage kopierten Stream in das Modell ein
	 * @param stream	Stream, der die Daten enth�lt
	 * @param point	Punkt (Mauskoordinaten), an dem die Elemente eingef�gt werden sollen
	 * @param zoom	Zoomfaktor
	 */
	public void setTransferData(final ByteArrayInputStream stream, final Point point, final double zoom) {
		final ModelSurface temp=getSurfaceFromTransferData(stream);

		/* IDs der einzuf�genden Elemente anpassen */
		int maxId=getMaxId();
		for (ModelElement element: temp.elements) element.setId(element.getId()+maxId+1);

		/* Obere linke Ecke der einzuf�genden Elemente bestimmen */
		Point upperLeft=new Point(ModelSurfacePanel.SIZE,ModelSurfacePanel.SIZE);
		for (ModelElement element : temp.elements) {
			final Point p=element.getPosition(true);
			if (p!=null) {
				upperLeft.x=FastMath.min(upperLeft.x,p.x);
				upperLeft.y=FastMath.min(upperLeft.y,p.y);
			}
		}
		if (upperLeft.x==ModelSurfacePanel.SIZE) upperLeft.x=0;
		if (upperLeft.y==ModelSurfacePanel.SIZE) upperLeft.y=0;

		/* Obere linke Ecke an Mausposition verschieben */
		final Point delta=new Point((int)FastMath.round(point.x/zoom)-upperLeft.x,(int)FastMath.round(point.y/zoom)-upperLeft.y);
		for (ModelElement element : temp.elements) {
			final Point p=element.getPosition(false);
			if (p!=null) {
				p.translate(delta.x,delta.y);
				element.setPosition(p);
			}
		}

		/* In Ziel einf�gen */
		List<ModelElement> newElements=new ArrayList<>();
		for (ModelElement element: temp.elements) {
			ModelElement newElement=element.clone(model,this);
			if (parentSurface!=null && !newElement.canAddToSub()) continue;
			elements.add(newElement);
			newElement.addedToSurface();
			newElements.add(newElement);
		}

		/* Nur ein Element eingef�gt? */
		if (newElements.size()==1) smartRename(newElements.get(0));

		for (ModelElement element: newElements) element.initAfterLoadOrClone();
	}

	/**
	 * Listet alle momentan im System vorhandenen Variablennamen auf
	 * @param globalVariableNames	Liste der globalen (im Modell selbst definierten) Variablennamen
	 * @return	Liste aller vorhandenen Variablennamen (ohne Doppelungen)
	 */
	public String[] getVariableNames(final String[] globalVariableNames) {
		List<String> variables=new ArrayList<>();

		if (globalVariableNames!=null) variables.addAll(Arrays.asList(globalVariableNames));

		for (ModelElement element: elements) if ((element instanceof ElementWithNewVariableNames)) {
			for (String var: ((ElementWithNewVariableNames)element).getVariables()) {
				if (CalcSymbolClientUserData.testClientData(var)>=0) continue;
				if (CalcSymbolClientUserData.testClientDataString(var)!=null) continue;
				boolean inList=false;
				for (String s: variables) if (s.equalsIgnoreCase(var)) {inList=true; break;}
				if (!inList) variables.add(var);
			}
		}

		return variables.toArray(new String[0]);
	}

	/**
	 * Listet alle momentan im System vorhandenen Variablennamen auf.<br>
	 * Es wird dabei ggf. auf das �bergeordnete Surface-Element zur�ckgegriffen.
	 * @param globalVariableNames	Liste der globalen (im Modell selbst definierten) Variablennamen
	 * @param addClientVariables	F�gt optional die Nutzerdaten-Pseudo-Variablen "w", "t" und "p" hinzu.
	 * @return	Liste aller vorhandenen Variablennamen (ohne Doppelungen)
	 */
	public String[] getMainSurfaceVariableNames(final String[] globalVariableNames, final boolean addClientVariables) {
		final String[] variables=(getParentSurface()==null)?getVariableNames(globalVariableNames):getParentSurface().getVariableNames(globalVariableNames);
		if (!addClientVariables) return variables;

		final List<String> namesList=new ArrayList<>(Arrays.asList(variables));
		for (String var: RunModel.additionalVariables) {
			boolean inList=false;
			for (String s: namesList) if (s.equalsIgnoreCase(var)) {inList=true; break;}
			if (!inList) namesList.add(var);
		}
		return namesList.toArray(new String[0]);
	}

	private boolean isSignalListEmpty(final String[] signalList) {
		if (signalList==null || signalList.length==0) return true;
		for (String signal: signalList) if (signal!=null && !signal.trim().isEmpty()) return false;
		return true;
	}

	private void listSignals(final List<ModelElementSignalTrigger> list, final boolean namedOnly) {
		for (ModelElement element: elements) {
			if (!(element instanceof ModelElementBox)) continue;
			if (!(element instanceof ModelElementSignalTrigger)) continue;
			if (namedOnly && isSignalListEmpty(((ModelElementSignalTrigger)element).getSignalNames())) continue;
			list.add((ModelElementSignalTrigger)element);
		}
	}

	/**
	 * Liefert eine Liste mit allen Elementen, die Signale ausl�sen k�nnen, auf allen Modellebenen.<br>
	 * (Es ist dabei unerheblich, ob diese Funktion f�r das Haupt-Surface oder f�r ein untergeordnetes
	 * Surface aufgerufen wird. Es wird immer die gesamte Liste geliefert.)
	 * @param namedOnly	Gibt an, ob alle oder nur benannte Signale geliefert werden sollen.
	 * @return	Liste der Elemente, die Signale ausl�sen k�nnen (d.h. die {@link ModelElementSignalTrigger} implementieren)
	 * @see ModelElementSignalTrigger
	 */
	public List<ModelElementSignalTrigger> getAllSignals(final boolean namedOnly) {
		if (parentSurface!=null) return parentSurface.getAllSignals(namedOnly);

		final List<ModelElementSignalTrigger> list=new ArrayList<>();

		listSignals(list,namedOnly);
		for (ModelElement element: elements) if (element instanceof ModelElementSub) ((ModelElementSub)element).getSubSurface().listSignals(list,namedOnly);

		return list;
	}

	/**
	 * Liefert eine Liste der Namen aller Signale auf allen Modellebenen.<br>
	 * (Es ist dabei unerheblich, ob diese Funktion f�r das Haupt-Surface oder f�r ein untergeordnetes
	 * Surface aufgerufen wird. Es wird immer die gesamte Liste geliefert.)
	 * @return	Namen der Elemente, die Signale ausl�sen k�nnen (d.h. die {@link ModelElementSignalTrigger} implementieren)
	 */
	public List<String> getAllSignalNames() {
		List<String> names=new ArrayList<>();

		for (ModelElementSignalTrigger signalTrigger: getAllSignals(true)) {
			final String[] list=signalTrigger.getSignalNames();
			if (list!=null) for (String name: list) if (!names.contains(name)) names.add(name);
		}

		names.sort(String.CASE_INSENSITIVE_ORDER);

		return names;
	}

	private void buildCheckMap(final Map<Integer,Integer> ids, final boolean repair) {
		for (ModelElement element: elements) {
			int id=element.getId();
			Integer I=ids.get(id);
			if (I==null) {
				ids.put(id,1);
			} else {
				if (repair) {
					final int newId=getNextFreeId();
					element.setId(newId);
					ids.put(newId,1);
					fireStateChangeListener();
				} else {
					ids.put(id,I+1);
				}
			}
			if (element instanceof ModelElementSub) ((ModelElementSub)element).getSubSurface().buildCheckMap(ids,repair);
		}
	}

	/**
	 * �berpr�ft das Modell auf doppelt auftretende Stations-IDs
	 * @param repair	Modell automatisch reparieren?
	 * @return	Liste mit den doppelt auftretenden IDs (im Erfolgsfall eine leere Liste); wird das Modell repariert, so bezieht sich die Liste auf den Stand nach der Reparatur
	 */
	public int[] checkDoubleIDs(final boolean repair) {
		ModelSurface root=this;
		while (root.getParentSurface()!=null) root=root.getParentSurface();

		final Map<Integer,Integer> ids=new HashMap<>();
		root.buildCheckMap(ids,repair);

		final List<Integer> damagedIDsList=new ArrayList<>();
		for (Map.Entry<Integer,Integer> entry: ids.entrySet()) {
			int count=entry.getValue();
			if (count>1) damagedIDsList.add(entry.getKey());
		}

		return damagedIDsList.stream().mapToInt(I->I.intValue()).toArray();
	}

	private void swapElementsInList(final int index1, final int index2) {
		final ModelElement element1=elements.get(index1);
		elements.set(index1,elements.get(index2));
		elements.set(index2,element1);
	}

	/**
	 * Bewegt ein Element um eine oder mehrere Stufen in der Zeichenfl�chen-Hierarchie nach vorne
	 * @param element	Das zu bewegende Element
	 * @param max	Wird hier <code>true</code> �bergeben, so wird das Element ganz nach vorne verschoben; ansonsten nur um eine Stufe nach vorne
	 */
	public void moveElementToFront(final ModelElement element, final boolean max) {
		if (element==null) return;
		final int index=elements.indexOf(element);
		if (index<0) return;

		if (index==elements.size()-1) return;
		if (max) swapElementsInList(index,elements.size()-1); else swapElementsInList(index,index+1);

		fireRedraw();
	}

	/**
	 * Bewegt ein Element um eine oder mehrere Stufen in der Zeichenfl�chen-Hierarchie nach hinten
	 * @param element	Das zu bewegende Element
	 * @param max	Wird hier <code>true</code> �bergeben, so wird das Element ganz nach hinten verschoben; ansonsten nur um eine Stufe nach hinten
	 */
	public void moveElementToBack(final ModelElement element, final boolean max) {
		if (element==null) return;
		final int index=elements.indexOf(element);
		if (index<0) return;

		if (index==0) return;
		if (max) swapElementsInList(index,0); else swapElementsInList(index,index-1);

		fireRedraw();
	}

	/**
	 * Bewegt das momentan ausgew�hlte Element um eine oder mehrere Stufen in der Zeichenfl�chen-Hierarchie nach vorne
	 * @param max	Wird hier <code>true</code> �bergeben, so wird das Element ganz nach vorne verschoben; ansonsten nur um eine Stufe nach vorne
	 */
	public void moveSelectedElementToFront(final boolean max) {
		moveElementToFront(getSelectedElement(),max);
	}

	/**
	 * Bewegt das momentan ausgew�hlte Element um eine oder mehrere Stufen in der Zeichenfl�chen-Hierarchie nach hinten
	 * @param max	Wird hier <code>true</code> �bergeben, so wird das Element ganz nach hinten verschoben; ansonsten nur um eine Stufe nach hinten
	 */
	public void moveSelectedElementToBack(final boolean max) {
		moveElementToBack(getSelectedElement(),max);
	}

	private List<ModelElementPosition> getSelectedPositionElements() {
		return getSelectedElements().stream().filter(element->element instanceof ModelElementPosition).map(element->(ModelElementPosition)element).collect(Collectors.toList());
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen Oberkante aus.
	 */
	public void alignSelectedElementsTop() {
		final List<ModelElementPosition> elements=getSelectedPositionElements();
		if (elements.size()<2) return;
		final int y=elements.stream().mapToInt(element->element.getPosition(true).y).sum()/elements.size();
		for (ModelElementPosition element: elements) {
			final Point p=element.getPosition(false);
			p.y=y;
			element.setPosition(p);
		}
	}

	/**
	 * Richtet die ausgew�hlten Element so aus, dass sich ihre vertikale Mitte auf derselben H�he befindet.
	 */
	public void alignSelectedElementsMiddle() {
		final List<ModelElementPosition> elements=getSelectedPositionElements();
		if (elements.size()<2) return;
		final int y=elements.stream().mapToInt(element->element.getMiddlePosition(true).y).sum()/elements.size();
		for (ModelElementPosition element: elements) {
			final Point p=element.getPosition(false);
			p.y=y-element.getSize().height/2;
			element.setPosition(p);
		}
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen Unterkante aus.
	 */
	public void alignSelectedElementsBottom() {
		final List<ModelElementPosition> elements=getSelectedPositionElements();
		if (elements.size()<2) return;
		final int y=elements.stream().mapToInt(element->element.getLowerRightPosition().y).sum()/elements.size();
		for (ModelElementPosition element: elements) {
			final Point p=element.getPosition(false);
			p.y=y-element.getSize().height;
			element.setPosition(p);
		}
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen linken Kante aus.
	 */
	public void alignSelectedElementsLeft() {
		final List<ModelElementPosition> elements=getSelectedPositionElements();
		if (elements.size()<2) return;
		final int x=elements.stream().mapToInt(element->element.getPosition(true).x).sum()/elements.size();
		for (ModelElementPosition element: elements) {
			final Point p=element.getPosition(false);
			p.x=x;
			element.setPosition(p);
		}
	}

	/**
	 * Richtet die ausgew�hlten Element so aus, dass sich ihre horizontale Mitte auf derselben Linie befindet.
	 */
	public void alignSelectedElementsCenter() {
		final List<ModelElementPosition> elements=getSelectedPositionElements();
		if (elements.size()<2) return;
		final int x=elements.stream().mapToInt(element->element.getMiddlePosition(true).x).sum()/elements.size();
		for (ModelElementPosition element: elements) {
			final Point p=element.getPosition(false);
			p.x=x-element.getSize().width/2;
			element.setPosition(p);
		}
	}

	/**
	 * Richtet die ausgew�hlten Element an einer gemeinsamen rechten Kante aus.
	 */
	public void alignSelectedElementsRight() {
		final List<ModelElementPosition> elements=getSelectedPositionElements();
		if (elements.size()<2) return;
		final int x=elements.stream().mapToInt(element->element.getLowerRightPosition().x).sum()/elements.size();
		for (ModelElementPosition element: elements) {
			final Point p=element.getPosition(false);
			p.x=x-element.getSize().width;
			element.setPosition(p);
		}
	}

	/**
	 * F�gt ein <code>Runnable</code>-Objekt zu der Liste der Listener, die aufgerufen werden,
	 * wenn das aktuelle Element in die Zwischenablage kopiert werden soll.
	 * @param listener	Zu benachrichtigende Listener
	 */
	public void addRequestCopyListener(final Runnable listener) {
		if (requestCopyListeners.indexOf(listener)<0) requestCopyListeners.add(listener);
	}

	/**
	 * Entfernt ein <code>Runnable</code>-Objekt aus der Liste der Listener, die aufgerufen werden,
	 * wenn das aktuelle Element in die Zwischenablage kopiert werden soll.
	 * @param listener	Listener, der nicht mehr benachrichtigt werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte.
	 */
	public boolean removeRequestCopyListener(final Runnable listener) {
		final int index=requestCopyListeners.indexOf(listener);
		if (index<0) return false;
		requestCopyListeners.remove(listener);
		return true;
	}

	/**
	 * Benachrichtigt alle Listener, dass das aktuelle Element kopiert werden m�chte.<br>
	 */
	public void fireRequestCopy() {
		for (Runnable listener: requestCopyListeners) listener.run();
	}

	/**
	 * F�gt ein <code>Runnable</code>-Objekt zu der Liste der Listener, die aufgerufen werden,
	 * wenn das aktuelle Element in die Zwischenablage hinein ausgeschnitten werden soll.
	 * @param listener	Zu benachrichtigende Listener
	 */
	public void addRequestCutListener(final Runnable listener) {
		if (requestCutListeners.indexOf(listener)<0) requestCutListeners.add(listener);
	}

	/**
	 * Entfernt ein <code>Runnable</code>-Objekt aus der Liste der Listener, die aufgerufen werden,
	 * wenn das aktuelle Element in die Zwischenablage hinein ausgeschnitten werden soll.
	 * @param listener	Listener, der nicht mehr benachrichtigt werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte.
	 */
	public boolean removeRequestCutListener(final Runnable listener) {
		final int index=requestCutListeners.indexOf(listener);
		if (index<0) return false;
		requestCutListeners.remove(listener);
		return true;
	}

	/**
	 * Benachrichtigt alle Listener, dass das aktuelle Element kopiert werden m�chte.
	 */
	public void fireRequestCut() {
		for (Runnable listener: requestCutListeners) listener.run();
	}

	/**
	 * F�gt ein <code>Runnable</code>-Objekt zu der Liste der Listener, die aufgerufen werden,
	 * wenn es eine �nderung im Modell gibt.
	 * @param listener	Zu benachrichtigende Listener
	 */
	public void addStateChangeListener(final Runnable listener) {
		if (stateChangeListener.indexOf(listener)<0) stateChangeListener.add(listener);
	}

	/**
	 * Entfernt ein <code>Runnable</code>-Objekt aus der Liste der Listener, die aufgerufen werden,
	 * wenn es eine �nderung im Modell gibt.
	 * @param listener	Listener, der nicht mehr benachrichtigt werden soll.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Listener erfolgreich aus der Liste entfernt werden konnte.
	 */
	public boolean removeStateChangeListener(final Runnable listener) {
		final int index=stateChangeListener.indexOf(listener);
		if (index<0) return false;
		stateChangeListener.remove(listener);
		return true;
	}

	private boolean delayFireStateChangeListener=false;
	private boolean needToFireStateChangeListener=false;

	/**
	 * Benachrichtigt alle Listener, dass es eine �nderung an dem Modell gab,
	 */
	public void fireStateChangeListener() {
		if (delayFireStateChangeListener) {needToFireStateChangeListener=true; return;}
		for (Runnable listener: stateChangeListener) listener.run();
	}

	/**
	 * Informiert alle Elemente (die sich daf�r interessieren), dass ein Objekt
	 * (z.B. ein Kundentyp) umbenannt wurde, so dass diese ggf. Daten kopieren
	 * k�nnen (sofern f�r den neuen Namen nicht bereits Daten vorliegen).
	 * @param oldName	Alter Name
	 * @param newName	Neuer Name
	 * @param type	Gibt an, was umbenannt wurde
	 * @param checkParent	Wird hier <code>true</code> �bergeben, so reicht die Funktion die Benachrichtigung an ein optionales Eltern-Surface durch.
	 * @see ModelDataRenameListener
	 */
	public void objectRenamed(final String oldName, final String newName, final ModelDataRenameListener.RenameType type, boolean checkParent) {
		if (checkParent && parentSurface!=null) {
			parentSurface.objectRenamed(oldName,newName,type,false);
			return;
		}

		/* Wurde der Name �berhaupt ge�ndert? */
		if (oldName==null || oldName.isEmpty() || newName==null || newName.isEmpty() || oldName.equals(newName)) return;

		/* Einstellungen in Stationen kopieren */
		for (ModelElement element: elements) {
			if (element instanceof ModelDataRenameListener) ((ModelDataRenameListener)element).objectRenamed(oldName,newName,type);
		}
	}

	/**
	 * Definiert ein <code>AnimationPanel</code>, welches f�r die Animation des aktuellen Surface-Elements
	 * verwendet wird. Dieser Wert wird nicht von dem Objekt selbst verwendet, sondern kann �ber die
	 * <code>getAnimationPanel</code>-Methode abgefragt werden.
	 * @param animationPanel	Animations-Panel f�r dieses Element
	 * @see ModelSurface#getAnimationPanel()
	 */
	public void setAnimatorPanel(final AnimationPanel animationPanel) {
		this.animationPanel=animationPanel;
	}

	/**
	 * Liefert das aktuelle f�r dieses Element eingestellte <code>AnimationPanel</code>
	 * @return Aktuelles <code>AnimationPanel</code>
	 * @see ModelSurface#setAnimatorPanel(AnimationPanel)
	 */
	public AnimationPanel getAnimationPanel() {
		return animationPanel;
	}

	/**
	 * Tr�gt manuell ein neues Editor-Modell in die Elemente ein (inkl. Untermodelle)
	 * @param model	Neues Editor-Modell
	 */
	public void updateElements(final EditModel model) {
		for (ModelElement element: elements) {
			element.setModel(model);
		}
	}

	/**
	 * Aktualisiert die kleinen Icons auf den Stationen, wenn zuvor z.B. das Icon f�r einen Kundentypen ver�ndert wurde.
	 * @see ModelElementBox#updateAdditionalIcon()
	 */
	public void updateAdditionalIcons() {
		for (ModelElement element: elements) if (element instanceof ModelElementBox) {
			((ModelElementBox)element).updateAdditionalIcon();
		}
	}

	private boolean isNameInUse(final String name, final ModelElement ignoreElement) {
		return elements.stream().filter(e->(e instanceof ModelElementBox) && e!=ignoreElement).map(e->((ModelElementBox)e).getName()).filter(s->s.equals(name)).findFirst().isPresent();
	}

	private Object[] splitName(final String name) {
		final int len=name.length();
		if (len<2) return null;

		int i=len-1;
		while (i>=0) {
			final char c=name.charAt(i);
			if (c<'0' || c>'9') {i++; break;}
			i--;
		}
		if (i<0 || i==len) return null;

		final Integer I=NumberTools.getInteger(name.substring(i));
		if (I==null) return null;
		return new Object[] {name.subSequence(0,i),I};
	}

	/**
	 * Benennt ein Element evtl. nach dem Kopieren um
	 * @param element	Neues Element, das durch Kopieren entstanden ist
	 * @see SetupData#renameOnCopy
	 */
	public void smartRename(final ModelElement element) {
		final SetupData.RenameOnCopyMode mode=SetupData.getSetup().renameOnCopy;

		if (mode==SetupData.RenameOnCopyMode.OFF) return;

		if (element==null || !elements.contains(element) || !(element instanceof ModelElementBox)) return;
		final ModelElementBox box=(ModelElementBox)element;
		if (box.getName().trim().isEmpty()) return;

		final Object[] parts=splitName(box.getName());
		final String str;
		int nr;
		if (parts==null) {
			if (mode==SetupData.RenameOnCopyMode.SMART) return;
			str=box.getName();
			nr=0;
		} else {
			str=(String)parts[0];
			nr=(Integer)parts[1];
		}

		nr++;
		while (isNameInUse(str+nr,element)) nr++;
		box.setName(str+nr);
	}

	/**
	 * Liefert eine Liste mit allen verf�gbaren Ebenen.<br>
	 * Es wird die Originalliste geliefert, d.h. es k�nnen direkt Ver�nderungen vorgenommen werden.
	 * @return	Liste mit allen verf�gbaren Ebenen (kann leer sein, aber ist nie <code>null</code>)
	 */
	public List<String> getLayers() {
		return layers;
	}

	/**
	 * Liefert eine Liste mit allen sichtbaren Ebenen.<br>
	 * Diese Liste sollte ein Teilliste von {@link #getLayers()} sein.<br>
	 * Es wird die Originalliste geliefert, d.h. es k�nnen direkt Ver�nderungen vorgenommen werden.
	 * @return	Liste mit allen sichtbaren Ebenen (kann leer sein, aber ist nie <code>null</code>)
	 */
	public List<String> getVisibleLayers() {
		return visibleLayers;
	}

	/**
	 * Name der aktuellen Ebene, auf die neue Elemente eingef�gt werden.
	 * @return	Name der Ebene f�r neue Elemente (kann <code>null</code> sein)
	 */
	public String getActiveLayer() {
		if (activeLayer!=null && activeLayer.trim().isEmpty()) return null;
		return activeLayer;
	}

	/**
	 * Stellt den Namen der aktuellen Ebene, auf die neue Elemente eingef�gt werden soll, ein.
	 * @param layer	Name der Ebene f�r neue Elemente (kann <code>null</code> sein)
	 */
	public void setActiveLayer(final String layer) {
		if (layer!=null && layer.trim().isEmpty()) activeLayer=null; else activeLayer=(layer==null)?null:layer.trim();
	}
}