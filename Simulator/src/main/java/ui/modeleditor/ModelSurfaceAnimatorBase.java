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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import mathtools.NumberTools;
import scripting.java.DynamicFactory;
import scripting.java.DynamicRunner;
import scripting.java.DynamicStatus;
import scripting.java.OutputImpl;
import scripting.java.SystemImpl;
import scripting.js.JSRunSimulationData;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataResourceOperator;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.RunModel;
import simulator.runmodel.SimulationData;
import simulator.simparser.ExpressionCalc;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ElementWithAnimationDisplay;
import ui.modeleditor.elements.ModelElementConveyor;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTeleportDestination;
import ui.modeleditor.elements.ModelElementTeleportSource;
import ui.modeleditor.elements.ModelElementTransportDestination;
import ui.modeleditor.elements.ModelElementTransportParking;
import ui.modeleditor.elements.ModelElementTransportSource;
import ui.modeleditor.elements.ModelElementTransportTransporterSource;
import ui.modeleditor.elements.ModelElementVertex;
import ui.modeleditor.elements.ModelElementWayPoint;
import ui.modeleditor.elements.WayPointRecord;

/**
 * Diese Klasse stellt Basisfunktionen zur Animation eines Simulationsmodells zur Verfügung.
 * @author Alexander Herzog
 */
public class ModelSurfaceAnimatorBase {
	private final static int BOX_WIDTH=100;
	private final static int BOX_HEIGHT=50;
	private final static int ICON_SIZE=BOX_HEIGHT/2;
	/** Standardicon zur Darstellung von Kunden */
	public final static String DEFAULT_CLIENT_ICON_NAME="user";
	/** Standardicon zur Darstellung von Bedienern */
	public final static String DEFAULT_OPERATOR_ICON_NAME="status_online";
	/** Standardicon zur Darstellung von leeren Transportern in Fahrtrichtung rechts */
	public final static String DEFAULT_TRANSPORTER_EAST_EMPTY_ICON_NAME="lorry-empty";
	/** Standardicon zur Darstellung von leeren Transportern in Fahrtrichtung links */
	public final static String DEFAULT_TRANSPORTER_WEST_EMPTY_ICON_NAME="lorry-left-empty";
	/** Standardicon zur Darstellung von beladenen Transportern in Fahrtrichtung rechts */
	public final static String DEFAULT_TRANSPORTER_EAST_ICON_NAME="lorry";
	/** Standardicon zur Darstellung von beladenen Transportern in Fahrtrichtung links */
	public final static String DEFAULT_TRANSPORTER_WEST_ICON_NAME="lorry-left";
	private final static int ANIMATION_STEPS=25;
	private final static int TRANSPORTER_ANIMATION_STEPS=50;
	private final static int DEFAULT_DISTANCE=250; /* Entfernung, auf die sich die Anzahl an Schritten beziehen */

	private final boolean multiCore;
	private boolean slowMode;
	private final boolean animateResources;

	private long fpsStartTime;
	private long fpsFrameCount;
	private long fpsLastPaint;

	private boolean firstUpdateStep;

	/**
	 * Objekt welches alle verfügbaren Animations-Icons vorhält
	 */
	protected final ModelAnimationImages modelImages;

	/**
	 * Zeichenfläche für die Animation
	 */
	protected final ModelSurfacePanel surfacePanel;

	private final JFrame surfacePanelWindow;

	/**
	 * Modell-Surface-Objekt welches alle Stationen auf der aktuellen Ebene vorhält
	 */
	protected final ModelSurface surface;

	private final ElementWithAnimationDisplay[] animationElements;
	private final int[] elementBoxWidth;
	private final int[] elementBoxHeight;

	private final Semaphore drawClientsMutex;
	private final AnimationImageSource images;

	private boolean noAdditionalFrames;
	private RunDataClient[] clientsList;
	private RunDataResourceOperator[] operatorsList;
	private RunDataTransporter[] transportersList;

	private Map<Integer,Map<Integer,List<ModelElementWayPoint>>> wayPoints;

	private List<DrawIcon> drawStaticIcons;
	private List<DrawIcon> drawMovingIcons;

	private volatile boolean animationDone;

	private boolean fullRecording;
	private List<DrawIcon> fullRecordingStaticIcons;
	private List<DrawMovingIcon> fullRecordingMovingIcons;

	private SimulationData storedSimData; /* Um jeder Zeit Ausdrücke berechnen zu können. */

	/**
	 * Konstruktor der Klasse <code>ModelSurfaceAnimatorBase</code>
	 * @param surfacePanelWindow	Übergeordnetes Fenster in dem sich das Panel befindet (zur Minimiert-Erkennung, um in diesem Fall bei Fernsteuerung dennoch Animationen auszulösen)
	 * @param surfacePanel	Panel, in das die Animation eingezeichnet werden soll
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param multiCore	Gibt an, ob Simulation und Darstellung auf zwei Threads aufgeteilt werden sollen
	 * @param animateResources	Ressourcen und Transporter während der Animation anzeigen
	 */
	public ModelSurfaceAnimatorBase(final JFrame surfacePanelWindow, final ModelSurfacePanel surfacePanel, final ModelAnimationImages modelImages, final boolean multiCore, final boolean animateResources) {
		this.multiCore=multiCore;
		this.animateResources=animateResources;
		this.surfacePanel=surfacePanel;
		this.surfacePanelWindow=surfacePanelWindow;
		this.modelImages=modelImages;
		surface=surfacePanel.getSurface();
		animationElements=getAnimationElements();
		final int[][] elementBoxSizes=getElementBoxSizes();
		elementBoxWidth=elementBoxSizes[0];
		elementBoxHeight=elementBoxSizes[1];
		surfacePanel.setAdditionalUserPaint((g,zoom)->surfaceAddOnPaint(g,zoom));

		drawClientsMutex=new Semaphore(1);
		images=new AnimationImageSource();

		wayPoints=buildWayPointsList(surface);

		reset();
	}

	private ElementWithAnimationDisplay[] getAnimationElements() {
		final List<ElementWithAnimationDisplay> list=new ArrayList<>();
		for (ModelElement element: surface.getElements()) if (element instanceof ElementWithAnimationDisplay) list.add((ElementWithAnimationDisplay)element);
		return list.toArray(new ElementWithAnimationDisplay[0]);
	}

	private int[][] getElementBoxSizes() {
		final ModelSurface mainSurface=(surface.getParentSurface()==null)?surface:surface.getParentSurface();

		int maxId=mainSurface.getMaxId();
		final int[] width=new int[maxId+1];
		final int[] height=new int[maxId+1];

		for (ModelElement element1: mainSurface.getElements()) if (element1 instanceof ModelElementBox) {
			final int id1=element1.getId();
			final Dimension size1=((ModelElementBox)element1).getSize();
			width[id1]=size1.width;
			height[id1]=size1.height;
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) if (element2 instanceof ModelElementBox) {
				final int id2=element2.getId();
				final Dimension size2=((ModelElementBox)element2).getSize();
				width[id2]=size2.width;
				height[id2]=size2.height;
			}
		}

		for (int i=0;i<width.length;i++) {
			if (width[i]<=0) width[i]=BOX_WIDTH;
			if (height[i]<=0) width[i]=BOX_HEIGHT;
		}

		return new int[][]{width,height};
	}

	private ModelElement getTransportStationByName(final ModelSurface surface, final String name) {
		if (name.isEmpty()) return null;

		for (ModelElement element1: surface.getElements()) {
			if (element1.getName().equals(name)) {
				if (element1 instanceof ModelElementTransportSource) return element1;
				if (element1 instanceof ModelElementTransportTransporterSource) return element1;
				if (element1 instanceof ModelElementTransportParking) return element1;
				if (element1 instanceof ModelElementTransportDestination) return element1;
			}
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2.getName().equals(name)) {
					if (element2 instanceof ModelElementTransportSource) return element2;
					if (element2 instanceof ModelElementTransportTransporterSource) return element2;
					if (element2 instanceof ModelElementTransportParking) return element2;
					if (element2 instanceof ModelElementTransportDestination) return element2;
				}
			}
		}

		return null;
	}

	private List<ModelElement> getTransportStationsOnSurface(final ModelSurface surface) {
		final List<ModelElement> elements=new ArrayList<>();

		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementTransportSource) elements.add(element);
			if (element instanceof ModelElementTransportTransporterSource) elements.add(element);
			if (element instanceof ModelElementTransportParking) elements.add(element);
			if (element instanceof ModelElementTransportDestination) elements.add(element);
		}

		return elements;
	}

	private void addWayPoint(final ModelSurface surface, final Map<Integer,Map<Integer,Map<Integer,ModelElementWayPoint>>> wayPoints, final ModelElementWayPoint wayPoint) {
		for (WayPointRecord record: wayPoint.getRecords()) {
			final String stationNameA=record.getStationA();
			final String stationNameB=record.getStationB();

			final List<ModelElement> stationListA=new ArrayList<>();
			if (stationNameA.equals("*")) {
				stationListA.addAll(getTransportStationsOnSurface(wayPoint.getSurface()));
			} else {
				stationListA.add(getTransportStationByName(surface,stationNameA));
			}
			final List<ModelElement> stationListB=new ArrayList<>();
			if (stationNameB.equals("*")) {
				stationListB.addAll(getTransportStationsOnSurface(wayPoint.getSurface()));
			} else {
				stationListB.add(getTransportStationByName(surface,stationNameB));
			}

			for (ModelElement stationA: stationListA) for (ModelElement stationB: stationListB) {
				if (stationA==null || stationB==null) continue;

				if (stationA.getSurface()==stationB.getSurface() && stationA.getSurface()==wayPoint.getSurface()) {
					wayPoints.get(stationA.getId()).get(stationB.getId()).put(record.getIndex(),wayPoint);
				}
			}
		}
	}

	private List<ModelElementWayPoint> sortedWayPoints(final Map<Integer,ModelElementWayPoint> map) {
		final int indices[]=map.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray();
		final List<ModelElementWayPoint> list=new ArrayList<>();
		for (int index: indices) list.add(map.get(index));
		return list;
	}

	private Map<Integer,Map<Integer,List<ModelElementWayPoint>>> buildWayPointsList(ModelSurface surface) {
		if (surface.getParentSurface()!=null) surface=surface.getParentSurface();

		/* Liste mit den IDs aller Transport-Stationen */
		final List<Integer> ids=new ArrayList<>();
		for (ModelElement element1: surface.getElements()) {
			if (element1 instanceof ModelElementTransportSource) ids.add(element1.getId());
			if (element1 instanceof ModelElementTransportTransporterSource) ids.add(element1.getId());
			if (element1 instanceof ModelElementTransportParking) ids.add(element1.getId());
			if (element1 instanceof ModelElementTransportDestination) ids.add(element1.getId());
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementTransportSource) ids.add(element2.getId());
				if (element2 instanceof ModelElementTransportTransporterSource) ids.add(element2.getId());
				if (element2 instanceof ModelElementTransportParking) ids.add(element2.getId());
				if (element2 instanceof ModelElementTransportDestination) ids.add(element2.getId());
			}
		}

		/* Temporäre Map aufbauen */
		final Map<Integer,Map<Integer,Map<Integer,ModelElementWayPoint>>> wayPointsBuilder=new HashMap<>();
		for (Integer id1: ids) {
			final Map<Integer,Map<Integer,ModelElementWayPoint>> destMap=new HashMap<>();
			wayPointsBuilder.put(id1,destMap);
			for (Integer id2: ids) destMap.put(id2,new HashMap<>());
		}

		/* Zwischenziele eintragen */
		for (ModelElement element1: surface.getElements()) {
			if (element1 instanceof ModelElementWayPoint) addWayPoint(surface,wayPointsBuilder,(ModelElementWayPoint)element1);
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementWayPoint) addWayPoint(surface,wayPointsBuilder,(ModelElementWayPoint)element2);
			}
		}

		/* Finale Map aufbauen */
		final Map<Integer,Map<Integer,List<ModelElementWayPoint>>> wayPoints=new HashMap<>();
		for (Integer id1: ids) {
			final Map<Integer,List<ModelElementWayPoint>> destMap=new HashMap<>();
			wayPoints.put(id1,destMap);
			for (Integer id2: ids) destMap.put(id2,sortedWayPoints(wayPointsBuilder.get(id1).get(id2)));
		}

		return wayPoints;
	}

	/**
	 * Setzt das System als Vorbereitung eines neuen Animationslaufs zurück.<br>
	 * Wird dem <code>ModelSurfacePanel</code>-Objekt, welches im Konstruktor übergeben wurde ein neues <code>ModelSurface</code> übergeben, muss dieses Objekt über diese <code>reset</code>-Methode entsprechend aktualisiert werden.
	 */
	public void reset() {
		noAdditionalFrames=false;
		drawClientsMutex.acquireUninterruptibly();
		try {
			drawStaticIcons=null;
			drawMovingIcons=null;
			clientsList=null;
			operatorsList=null;
			transportersList=null;
			firstUpdateStep=true;

			staticDrawList=null;
			stationClients=null;
			staticIconsCache=null;
			stationByIdCache=null;
			stationAtMainLevelByIdCache=null;

			paintDone=true;
		} finally {drawClientsMutex.release();}
	}

	private void addOrReplace(final List<DrawIcon> sourceList, final List<DrawIcon> destinationListStatic, final List<DrawMovingIcon> destinationListMoving) {
		for (DrawIcon draw: sourceList) if (draw!=null) {

			boolean done=false;

			if (draw.stationA!=draw.stationB) for (int i=0;i<destinationListMoving.size();i++) {
				if (destinationListMoving.get(i).data==draw.data) {
					DrawIcon draw2=new DrawIcon(draw,-1,draw.stationB);
					destinationListMoving.get(i).add(draw2);
					done=true;
					break;
				}
			}

			for (int i=0;i<destinationListStatic.size();i++) {
				if (destinationListStatic.get(i).data==draw.data) {
					if (done) {
						destinationListStatic.remove(i);
					} else {
						destinationListStatic.set(i,new DrawIcon(draw));
					}
					done=true;
					break;
				}
			}
			if (!done) destinationListStatic.add(new DrawIcon(draw));
		}
	}

	private void addOrExtend(final List<DrawIcon> sourceList, final List<DrawMovingIcon> destinationList) {
		for (DrawIcon draw: sourceList) if (draw!=null) {
			boolean done=false;
			for (int i=0;i<destinationList.size();i++) {
				if (destinationList.get(i).data==draw.data) {
					destinationList.get(i).add(draw);
					done=true;
					break;
				}
			}
			if (!done) {
				final DrawMovingIcon drawMovingIcon=new DrawMovingIcon(draw.data);
				drawMovingIcon.add(draw);
				destinationList.add(drawMovingIcon);
			}
		}
	}

	/**
	 * Zeichnet die Kunden über die Surface-Elemente<br>
	 * Diese Methode wird dem <code>SurfacePanel</code> als <code>setAdditionalUserPaint</code> angegeben und dann über die <code>Paint</code>-Methode des <code>SurfacePanel</code>-Objekts aufgerufen.
	 * @param g	Grafik-Objekt, in das die Kunden eingezeichnet werden sollen (kann <code>null</code> sein, dann erfolgt keine Ausgabe; nur das optionale Recording wird durchgeführt)
	 * @param zoom	Aktueller Zoomfaktor (1.0=100%)
	 */
	protected void surfaceAddOnPaint(final Graphics g, final double zoom) {
		drawClientsMutex.acquireUninterruptibly();
		try {
			if (g!=null) {
				if (drawStaticIcons!=null) for (DrawIcon draw: drawStaticIcons) if (draw!=null) {
					final int x=(int)FastMath.round(draw.x*zoom);
					final int y=(int)FastMath.round(draw.y*zoom);
					g.drawImage(images.get(draw.icon,modelImages,ICON_SIZE,zoom),x,y,null);
				}
				if (drawMovingIcons!=null) for (DrawIcon draw: drawMovingIcons) if (draw!=null) {
					final int x=(int)FastMath.round(draw.x*zoom);
					final int y=(int)FastMath.round(draw.y*zoom);
					g.drawImage(images.get(draw.icon,modelImages,ICON_SIZE,zoom),x,y,null);
				}
			}

			if (fullRecording) {
				if (fullRecordingStaticIcons==null) fullRecordingStaticIcons=new ArrayList<>();
				if (fullRecordingMovingIcons==null) fullRecordingMovingIcons=new ArrayList<>();

				if (drawStaticIcons!=null) {
					addOrReplace(drawStaticIcons,fullRecordingStaticIcons,fullRecordingMovingIcons);
				}

				if (drawMovingIcons!=null) {
					addOrExtend(drawMovingIcons,fullRecordingMovingIcons);
				}
			}

		} finally {drawClientsMutex.release();}
	}

	/**
	 * Gibt an, ob der Langsam-Modus (sequenzielle Abarbeitung unabhängig von der Multi-Core-Einstellung und ohne Frame-Drop) aktiv ist.
	 * @return	Gibt <code>true</code> zurück, wenn der Langsam-Modus aktiv ist
	 */
	public boolean isSlowMode() {
		return slowMode;
	}

	/**
	 * Stellt den Langsam-Modus (sequenzielle Abarbeitung unabhängig von der Multi-Core-Einstellung und ohne Frame-Drop) ein oder aus.
	 * @param slowMode	Wird <code>true</code> übergeben, so wird der Langsam-Modus verwendet.
	 */
	public void setSlowMode(final boolean slowMode) {
		this.slowMode=slowMode;
	}

	private volatile boolean paintDone;

	private final Runnable paintRunner=new Runnable() {
		@Override
		public void run() {
			surfacePanel.repaint();
			if (fullRecording && surfacePanelWindow!=null && surfacePanelWindow.getState()==Frame.ICONIFIED) {
				surfaceAddOnPaint(null,1.0);

			}
			paintDone=true;
		}
	};

	/**
	 * Diese Methode wird aufgerufen, wenn der Animationsstatus
	 * neu auf der Zeichenfläche dargestellt werden soll.
	 * @param simData	Simulationsdatenobjekt
	 */
	protected void doPaintSurface(final SimulationData simData) {
		final long time=System.currentTimeMillis();
		storedSimData=simData;

		if (time-fpsLastPaint>=15 || slowMode || fullRecording) {
			if (multiCore && !slowMode) {
				if (!paintDone) return;
				paintDone=false;
				SwingUtilities.invokeLater(paintRunner);
			} else {
				try {
					SwingUtilities.invokeAndWait(paintRunner);
				} catch (InvocationTargetException | InterruptedException e) {Thread.currentThread().interrupt();}
			}
			fpsLastPaint=time;
		}
		fpsFrameCount++;
	}

	/**
	 * Aktualisiert die Elemente auf der Zeichenfläche, die Daten aus dem Simulationsverlauf in der Animation darstellen
	 * @param simData	Aktuelles Simulationsdatenobjekt
	 * @param isPreview	Wird auf <code>true</code> gesetzt, wenn das <code>SimulationData</code>-Objekt bereits auf einem neueren Stand ist, als es die Zeitangabe angibt
	 * @param noPaint	Führt nur die Berechnungen durch, zeichnet die Ergebnisse jedoch nicht (auch wenn die Animationselemente dies gerne hätten)
	 */
	public void updateSurfaceAnimationDisplayElements(final SimulationData simData, final boolean isPreview, final boolean noPaint) {

		if (firstUpdateStep) {
			if (animateResources) {
				operatorsList=simData.runData.resources.getOperators(simData,true);
				simData.runData.resources.addResourceCountChangeListeners(localSimData->{
					operatorsList=localSimData.runData.resources.getOperators(localSimData,false);
				});
				transportersList=simData.runData.transporters.getTransporters();
			}
			firstUpdateStep=false;
			fpsStartTime=System.currentTimeMillis();
			fpsFrameCount=0;
			fpsLastPaint=0;
			for (ElementWithAnimationDisplay element: animationElements) element.initAnimation(simData);
		}

		boolean needPaint=false;
		for (ElementWithAnimationDisplay element: animationElements) {
			if (element.updateSimulationData(simData,isPreview)) {
				needPaint=true; noAdditionalFrames=true;
			}
		}
		if (needPaint && !noPaint) doPaintSurface(simData);
	}

	/**
	 * Gibt an, ob es längere Zeitspannen ohne Systemänderung gab, die durch das Wiederholen einzelner Frames im Video abgebildet werden sollten
	 * @return	Gibt <code>true</code> zurück, wenn einzelne Frames im Video wiederholt werden sollen, um die simulierte Zeit verzerrungsfreier wiederzugeben
	 */
	public boolean useAdditionalFrames() {
		return !noAdditionalFrames;
	}

	private int[] stationAtMainLevelByIdCache;

	private int stationAtMainLevel(final int stationID) {
		if (stationID<0) return stationID;

		if (stationAtMainLevelByIdCache!=null && stationAtMainLevelByIdCache.length>stationID && stationAtMainLevelByIdCache[stationID]!=0) return stationAtMainLevelByIdCache[stationID];

		if (stationAtMainLevelByIdCache==null) stationAtMainLevelByIdCache=new int[surface.getMaxId()+1];

		if (surface.getById(stationID)!=null) {
			if (stationAtMainLevelByIdCache.length>stationID) stationAtMainLevelByIdCache[stationID]=stationID;
			return stationID;
		}
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementSub) {
			if (((ModelElementSub)element).getSubSurface().getById(stationID)!=null) {
				if (stationAtMainLevelByIdCache.length>stationID) stationAtMainLevelByIdCache[stationID]=element.getId();
				return element.getId();
			}
		}
		if (stationAtMainLevelByIdCache.length>stationID) stationAtMainLevelByIdCache[stationID]=stationID;
		return stationID;
	}

	private List<DrawIcon> staticDrawList;
	private int[] stationClients;
	private int[] stationOperators;
	private int[] stationTransporters;

	private DrawIcon[] staticIconsCache;
	private ModelElementBox[] stationByIdCache;

	private ModelElementBox getElementByIDFromCache(final int stationID) {
		final ModelElementBox el;
		if (stationID>=stationByIdCache.length) {
			final ModelElement el2=surface.getById(stationID);
			if (el2==null || !(el2 instanceof ModelElementBox)) return null;
			el=(ModelElementBox)el2;
		} else {
			if (stationByIdCache[stationID]==null) {
				final ModelElement el2=surface.getById(stationID);
				if (el2==null || !(el2 instanceof ModelElementBox)) return null;
				stationByIdCache[stationID]=(ModelElementBox)el2;
			}
			el=stationByIdCache[stationID];
		}
		return el;
	}

	private DrawIcon[] newStaticIconsCache;
	private int newStaticIconsCacheIndex;

	private void addStaticIcon(final int x, final int y, final String icon, final Object data) {
		DrawIcon drawIcon=null;
		if (staticIconsCache!=null && staticIconsCache.length>newStaticIconsCacheIndex && staticIconsCache[newStaticIconsCacheIndex]!=null) {
			if (staticIconsCache[newStaticIconsCacheIndex].x==x && staticIconsCache[newStaticIconsCacheIndex].y==y && staticIconsCache[newStaticIconsCacheIndex].icon.equals(icon)) drawIcon=staticIconsCache[newStaticIconsCacheIndex];
		}
		if (drawIcon==null) drawIcon=new DrawIcon(x,y,icon,data);
		newStaticIconsCache[newStaticIconsCacheIndex++]=drawIcon;
		staticDrawList.add(drawIcon);
	}

	@SuppressWarnings("unchecked")
	private List<DrawIcon> getStaticDrawIconsList(final RunDataClient[] clientsList, final Object skipClients, final RunDataTransporter skipTransporter, final RunDataResourceOperator[] operatorsList, final RunDataTransporter[] transporersList, final long currentTime) {
		if (staticDrawList==null) {
			staticDrawList=new ArrayList<>(10000);
			stationClients=new int[surface.getMaxId()+1];
			stationOperators=new int[surface.getMaxId()+1];
			stationTransporters=new int[surface.getMaxId()+1];
		} else {
			staticDrawList.clear();
			Arrays.fill(stationClients,0);
			Arrays.fill(stationOperators,0);
			Arrays.fill(stationTransporters,0);
		}

		if (stationByIdCache==null) stationByIdCache=new ModelElementBox[surface.getMaxId()+1];

		int cacheCount=0;
		if (clientsList!=null) cacheCount+=clientsList.length;
		if (operatorsList!=null) cacheCount+=operatorsList.length;
		if (transporersList!=null) cacheCount+=transporersList.length;
		newStaticIconsCache=new DrawIcon[cacheCount];
		newStaticIconsCacheIndex=0;

		/* Kunden hinzufügen */
		if (clientsList!=null) for (int i=0;i<clientsList.length;i++) {
			final RunDataClient client=clientsList[i];
			if (client.batched) continue; /* Kunde ist in einem Batch, also diesen Sub-Kunden nicht anzeigen. */

			boolean skip=false;
			if (skipClients!=null) {
				if (skipClients instanceof List<?>) {
					for (MoveClient skipClient: (List<MoveClient>)skipClients) if (client==skipClient.client) {skip=true; break;}
				}
				if (skipClients instanceof MoveClient) {
					if (client==((MoveClient)skipClients).client) {skip=true; break;}
				}
			}
			if (skip) continue;

			final int stationID=stationAtMainLevel(client.nextStationID);
			if (stationID<0) continue;

			final ModelElementBox element=getElementByIDFromCache(stationID);
			if (element==null) continue;

			final String icon=(client.icon==null)?DEFAULT_CLIENT_ICON_NAME:client.icon; /* hier schon das neue Icon, also nicht mehr client.iconLast, verwenden. */
			boolean done=false;
			if (element instanceof ModelElementConveyor) {addStaticClientIconConveyor(client,icon,(ModelElementConveyor)element,stationID,currentTime); done=true;}
			if (!done) addStaticClientIconDefault(client,icon,element,stationID);
		}

		/* Bediener hinzufügen */
		if (operatorsList!=null) for (int i=0;i<operatorsList.length;i++) {
			final RunDataResourceOperator operator=operatorsList[i];
			final int stationID=operator.getStation();
			if (stationID<0) continue; /* Bediener arbeitet momentan nicht */

			final ModelElementBox el=getElementByIDFromCache(stationID);
			if (el==null) continue;

			final int count=stationOperators[stationID];
			final Point p=el.getPosition(true);
			final int boxWidth=elementBoxWidth[stationID];
			final int boxHeight=elementBoxHeight[stationID];
			final int x=p.x+boxWidth-ICON_SIZE-FastMath.min(boxWidth-ICON_SIZE,count*boxWidth/10);
			final int y=p.y+boxHeight-ICON_SIZE;
			stationOperators[stationID]=count+1;
			final String icon=(operator.icon==null || operator.icon.isEmpty())?DEFAULT_OPERATOR_ICON_NAME:operator.icon;

			addStaticIcon(x,y,icon,operator);
		}

		/* Transporter hinzufügen */
		if (transportersList!=null) for (int i=0;i<transportersList.length;i++) {
			final RunDataTransporter transporter=transportersList[i];
			if (transporter==skipTransporter) continue;
			final int stationID=(transporter.inTransfer)?transporter.lastPosition:transporter.position;
			if (stationID<0) continue;

			final ModelElementBox el=getElementByIDFromCache(stationID);
			if (el==null) continue;

			final int count=stationTransporters[stationID];
			final Point p=el.getPosition(true);
			final int boxWidth=elementBoxWidth[stationID];
			final int boxHeight=elementBoxHeight[stationID];
			final int x=p.x+boxWidth-ICON_SIZE-FastMath.min(boxWidth-ICON_SIZE,count*boxWidth/10);
			final int y=p.y+boxHeight-ICON_SIZE;
			stationTransporters[stationID]=count+1;
			final String icon=(transporter.iconEastEmpty==null || transporter.iconEastEmpty.isEmpty())?DEFAULT_TRANSPORTER_EAST_EMPTY_ICON_NAME:transporter.iconEastEmpty;

			addStaticIcon(x,y,icon,transporter);
		}

		/* Ergebnis zurückliefern */
		staticIconsCache=newStaticIconsCache;
		return staticDrawList;
	}

	private void addStaticClientIconConveyor(final RunDataClient client, final String icon, final ModelElementConveyor element, final int stationID, final long currentTime) {
		final int boxWidth=elementBoxWidth[stationID];
		final int pos;

		if (client.stationInformationLong<=0 || client.arrivalProcessedStationID!=stationID) {
			/* Wartender Kunde */
			final int waiting=stationClients[stationID];
			pos=boxWidth/10-FastMath.min(boxWidth/10,waiting*boxWidth/20);
			stationClients[stationID]=waiting+1;
		} else {
			/* Kunde in Bewegung */
			final double time=element.getTransportTime();
			final double timeMS;
			switch (element.getTimeBase()) {
			case TIMEBASE_HOURS: timeMS=time*3600*1000; break;
			case TIMEBASE_MINUTES: timeMS=time*60*1000; break;
			case TIMEBASE_SECONDS: timeMS=time*1000; break;
			default: timeMS=time*1000; break;
			}
			final double percent;
			if (timeMS>0) {
				percent=FastMath.min(1,(currentTime-client.stationInformationLong)/timeMS);
			} else {
				percent=1;
			}
			pos=(int)Math.round((boxWidth-ICON_SIZE)*(1.0/10+percent*9/10));
		}

		final Point p=element.getPosition(true);
		final int x;
		if (element.getMoveDirection()==ModelElementConveyor.MoveDirection.MOVE_LEFT_TO_RIGHT) {
			x=p.x+pos;
		} else {
			x=p.x+boxWidth-ICON_SIZE-pos;
		}
		final int y=p.y-ICON_SIZE/2;
		addStaticIcon(x,y,icon,client);
	}

	private void addStaticClientIconDefault(final RunDataClient client, final String icon, final ModelElementBox element, final int stationID) {
		final int waiting=stationClients[stationID];
		final Point p=element.getPosition(true);
		final int boxWidth=elementBoxWidth[stationID];
		final int x=p.x+boxWidth-ICON_SIZE-FastMath.min(boxWidth-ICON_SIZE,waiting*boxWidth/10);
		final int y=p.y-ICON_SIZE/2;
		stationClients[stationID]=waiting+1;
		addStaticIcon(x,y,icon,client);
	}

	private void buildStaticClientsDrawList(final List<MoveClient> skipClients, final RunDataTransporter skipTransporter, final long currentTime) {
		drawClientsMutex.acquireUninterruptibly();
		try {
			if ((clientsList==null || clientsList.length==0) && (operatorsList==null || operatorsList.length==0) && (transportersList==null || transportersList.length==0)) {
				drawStaticIcons=null;
			} else {
				drawStaticIcons=getStaticDrawIconsList(clientsList,skipClients,skipTransporter,operatorsList,transportersList,currentTime);
			}
		} finally {drawClientsMutex.release();}
	}

	private void buildStaticClientsDrawListSingle(final MoveClient skipClient, final RunDataTransporter skipTransporter, final long currentTime) {
		drawClientsMutex.acquireUninterruptibly();
		try {
			if ((clientsList==null || clientsList.length==0) && (operatorsList==null || operatorsList.length==0) && (transportersList==null || transportersList.length==0)) {
				drawStaticIcons=null;
			} else {
				drawStaticIcons=getStaticDrawIconsList(clientsList,skipClient,skipTransporter,operatorsList,transportersList,currentTime);
			}
		} finally {drawClientsMutex.release();}
	}

	private AnimationPath getDirectAnimationPath(final RunDataClient client, final ModelElementBox stationFrom, final ModelElementBox stationTo) {
		/* Teleport-Übergänge zeichnen sich gerade dadurch aus, nicht in der Animation sichtbar zu sein. */
		if ((stationFrom instanceof ModelElementTeleportSource) && (stationTo instanceof ModelElementTeleportDestination)) {
			return null;
		}

		final AnimationPath path=new AnimationPath(client);
		final Point p1=stationFrom.getMiddlePosition(true);
		final Point p2=stationTo.getMiddlePosition(true);

		final int dist=(int)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
		final int steps=ANIMATION_STEPS*dist/DEFAULT_DISTANCE;

		path.buildRoute(stationFrom,stationTo,steps);

		return path;
	}

	private AnimationPath getDirectAnimationPath(final RunDataTransporter transporter, final String transporterIconEast, final String transporterIconWest, final ModelElementBox stationFrom, final ModelElementBox stationTo) {
		final AnimationPath path=new AnimationPath(transporter,transporterIconEast,transporterIconWest);
		final Point p1=stationFrom.getMiddlePosition(true);
		final Point p2=stationTo.getMiddlePosition(true);

		final int dist=(int)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
		final int steps=ANIMATION_STEPS*dist/DEFAULT_DISTANCE;

		path.buildRoute(stationFrom,stationTo,steps);

		return path;
	}

	private AnimationPath getFullAnimationPath(final RunDataClient client, final ModelElementBox stationFrom, final ModelElementBox stationTo) {
		final List<ModelElement> elements=getPathFromStationToStation(stationFrom,stationTo);
		if (elements==null || elements.size()<3) {
			return getDirectAnimationPath(client,stationFrom,stationTo);
		}

		final AnimationPath path=new AnimationPath(client);

		int partialSteps=FastMath.max(2,(ANIMATION_STEPS-1)/(elements.size()-1));
		if (partialSteps<ANIMATION_STEPS/4) partialSteps=ANIMATION_STEPS;
		if (partialSteps*elements.size()>ANIMATION_STEPS*3) partialSteps=ANIMATION_STEPS*3/elements.size();

		for (int i=0;i<elements.size()-1;i++) {
			final ModelElement e1=elements.get(i);
			final ModelElement e2=elements.get(i+1);
			if (!(e1 instanceof ModelElementPosition)) continue;
			if (!(e2 instanceof ModelElementPosition)) continue;
			final Point p1=e1.getMiddlePosition(true);
			final Point p2=e2.getMiddlePosition(true);

			final int dist=(int)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
			final int steps=partialSteps*dist/DEFAULT_DISTANCE;

			path.buildRoute((ModelElementPosition)e1,(ModelElementPosition)e2,steps);
		}

		return path;
	}

	private AnimationPath getFullTransporterAnimationPath(final RunDataTransporter transporter, final String transporterIconEast, final String transporterIconWest, final ModelElementBox stationFrom, final ModelElementBox stationTo) {
		final List<ModelElement> elements=getTransporterPathFromStationToStation(stationFrom,stationTo);
		if (elements==null || elements.size()<3) {
			return getDirectAnimationPath(transporter,transporterIconEast,transporterIconWest,stationFrom,stationTo);
		}

		final AnimationPath path=new AnimationPath(transporter,transporterIconEast,transporterIconWest);

		int partialSteps=FastMath.max(2,(TRANSPORTER_ANIMATION_STEPS-1)/(elements.size()-1));
		if (partialSteps<TRANSPORTER_ANIMATION_STEPS/4) partialSteps=TRANSPORTER_ANIMATION_STEPS;
		if (partialSteps*elements.size()>TRANSPORTER_ANIMATION_STEPS*3) partialSteps=TRANSPORTER_ANIMATION_STEPS*3/elements.size();

		for (int i=0;i<elements.size()-1;i++) {
			final ModelElement e1=elements.get(i);
			final ModelElement e2=elements.get(i+1);
			if (!(e1 instanceof ModelElementPosition)) continue;
			if (!(e2 instanceof ModelElementPosition)) continue;
			final Point p1=e1.getMiddlePosition(true);
			final Point p2=e2.getMiddlePosition(true);

			final int dist=(int)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y));
			final int steps=partialSteps*dist/DEFAULT_DISTANCE;

			path.buildRoute((ModelElementPosition)e1,(ModelElementPosition)e2,steps);
		}

		return path;
	}

	private List<ModelElement> getPathFromStationToStation(final ModelElementBox stationFrom, final ModelElementBox stationTo) {
		List<ModelElement> path=new ArrayList<>();

		/* Teleport-Übergänge zeichnen sich gerade dadurch aus, nicht in der Animation sichtbar zu sein. */
		if ((stationFrom instanceof ModelElementTeleportSource) && (stationTo instanceof ModelElementTeleportDestination)) {
			return null;
		}

		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementEdge && ((ModelElementEdge)element).getConnectionStart()==stationFrom) {
				final ModelElementEdge start=(ModelElementEdge)element;
				path.add(stationFrom);
				ModelElement target=start.getConnectionEnd();
				while (true) {
					if (target==stationTo) {path.add(target); return path;}
					if (!(target instanceof ModelElementVertex)) {path.clear(); break;}
					final ModelElementVertex vertex=(ModelElementVertex)target;
					path.add(vertex);
					if (vertex.getEdgeOut()==null) {path.clear(); break;}
					target=vertex.getEdgeOut().getConnectionEnd();
				}
			}
		}

		return null;
	}

	private List<ModelElement> getTransporterPathFromStationToStation(final ModelElementBox stationFrom, final ModelElementBox stationTo) {
		List<ModelElement> path=new ArrayList<>();

		path.add(stationFrom);

		final Map<Integer,List<ModelElementWayPoint>> destMap=wayPoints.get(stationFrom.getId());
		if (destMap!=null) {
			final List<ModelElementWayPoint> list=destMap.get(stationTo.getId());
			if (list!=null) path.addAll(list);
		}

		path.add(stationTo);

		return path;
	}

	private List<AnimationPath> getAnimationPathes(final List<MoveClient> clients) {
		if (clients==null || clients.size()==0) return new ArrayList<>();

		final List<AnimationPath> pathList=new ArrayList<>(clients.size());

		for (MoveClient move: clients) {
			final ModelElement stationFrom=surface.getById(move.from);
			final ModelElement stationTo=surface.getById(move.to);
			if (stationFrom==null || !(stationFrom instanceof ModelElementBox) || stationTo==null || !(stationTo instanceof ModelElementBox)) continue;
			final AnimationPath path=getFullAnimationPath(move.client,(ModelElementBox)stationFrom,(ModelElementBox)stationTo);
			if (path!=null) pathList.add(path);
		}

		return pathList;
	}

	private List<AnimationPath> getAnimationPath(final MoveClient move) {
		final List<AnimationPath> pathList=new ArrayList<>(1);

		final ModelElement stationFrom=surface.getById(move.from);
		final ModelElement stationTo=surface.getById(move.to);
		if (stationFrom!=null && (stationFrom instanceof ModelElementBox) && stationTo!=null && (stationTo instanceof ModelElementBox)) {
			final AnimationPath path=getFullAnimationPath(move.client,(ModelElementBox)stationFrom,(ModelElementBox)stationTo);
			if (path!=null) pathList.add(path);
		}

		return pathList;
	}

	private List<AnimationPath> getAnimationPath(final RunDataTransporter transporter, final String transporterIconEast, final String transporterIconWest, final int transporterStationID1, final int transporterStationID2) {
		final List<AnimationPath> pathList=new ArrayList<>(1);

		final ModelElement stationFrom=surface.getById(transporterStationID1);
		final ModelElement stationTo=surface.getById(transporterStationID2);
		if (stationFrom!=null && (stationFrom instanceof ModelElementBox) && stationTo!=null && (stationTo instanceof ModelElementBox)) {
			final AnimationPath path=getFullTransporterAnimationPath(transporter,transporterIconEast,transporterIconWest,(ModelElementBox)stationFrom,(ModelElementBox)stationTo);
			if (path!=null) pathList.add(path);
		}

		return pathList;
	}

	private void animatePathList(final List<AnimationPath> pathList, final int delay, final SimulationData simData) {
		if (pathList==null || pathList.size()==0) return;
		int points=0;
		for (AnimationPath path: pathList) points=FastMath.max(points,path.getPointCount());

		drawClientsMutex.acquireUninterruptibly();
		try {
			drawMovingIcons=new ArrayList<>(pathList.size());
			for (int i=0;i<pathList.size();i++) drawMovingIcons.add(null);
		} finally {drawClientsMutex.release();}

		int delayIntegrator=0;
		for (int i=0;i<points-1;i++) {
			drawClientsMutex.acquireUninterruptibly();
			try {
				for (int j=0;j<pathList.size();j++) {
					drawMovingIcons.set(j,pathList.get(j).getDrawClient(i));
				}
			} finally {drawClientsMutex.release();}
			doPaintSurface(simData);

			while (delayIntegrator>10) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {Thread.currentThread().interrupt(); break;}
				delayIntegrator-=10;
			}
			delayIntegrator+=delay;
		}

		drawClientsMutex.acquireUninterruptibly();
		try {drawMovingIcons=null;} finally {drawClientsMutex.release();}
	}

	/**
	 * Vorbereitung eines Animationsschritts<br>
	 * Bestimmte die Liste mit allen aktiven Kunden und aktualisiert auf der Zeichenfläche enthaltene Zähler usw.<br>
	 * Diese Methode sollte vor der eigentlichen Animation aufgerufen werden.
	 * @param simData	Simulationsdaten-Objekt
	 */
	protected void preProcess(final SimulationData simData) {
		final List<RunDataClient> list=simData.runData.clients.requestClientsInUseList();
		if (clientsList==null || clientsList.length!=list.size()) {
			clientsList=list.toArray(new RunDataClient[0]);
		} else {
			clientsList=list.toArray(clientsList);
		}
		updateSurfaceAnimationDisplayElements(simData,true,false);
	}

	/**
	 * Erstellt eine Liste mit zu animierenden Kunden bestehend aus einem einzigen Kunden.<br>
	 * Solch ein Liste wird für die <code>animate</code>-Funktion benötigt.
	 * @param client	Zu animierender Kunde
	 * @return	Liste mit zu animierenden Kunden
	 * @see #animate(MoveClient, int, SimulationData)
	 */
	protected List<MoveClient> getMoveClientList(final RunDataClient client) {
		List<MoveClient> list=new ArrayList<>(1);
		list.add(new MoveClient(client));
		return list;
	}

	/**
	 * Bewegt die angegebenen Kunden gemäß der angegebenen Start- und Zielstationen durch das System
	 * @param clients	Liste mit zu bewegenden Kunden und Start- und Zielstationen
	 * @param delay	Verzögerung pro Animationsschritt
	 * @param simData	Simulationsdatenobjekt
	 * @param lastStep	Handelt es sich um den letzten Schritt einer mehrteiligen Animation (wenn ja, werden die Endzustände der Kunden eingezeichnet)
	 */
	protected void animate(final List<MoveClient> clients, final int delay, final SimulationData simData, final boolean lastStep) {
		animationDone=false;
		buildStaticClientsDrawList(clients,null,simData.currentTime);
		final List<AnimationPath> pathList=getAnimationPathes(clients);
		animatePathList(pathList,fullRecording?1:delay,simData);
		buildStaticClientsDrawList(null,null,simData.currentTime);
		if (lastStep) updateSurfaceAnimationDisplayElements(simData,false,false);
		doPaintSurface(simData);
		animationDone=true;
	}

	/**
	 * Bewegt den angegebenen Kunden gemäß der angegebenen Start- und Zielstationen durch das System
	 * @param client	Zu bewegender Kunde
	 * @param delay	Verzögerung pro Animationsschritt
	 * @param simData	Simulationsdatenobjekt
	 */
	protected void animate(final MoveClient client, final int delay, final SimulationData simData) {
		animationDone=false;
		buildStaticClientsDrawListSingle(client,null,simData.currentTime);
		final List<AnimationPath> pathList=getAnimationPath(client);
		animatePathList(pathList,fullRecording?1:delay,simData);
		buildStaticClientsDrawList(null,null,simData.currentTime);
		updateSurfaceAnimationDisplayElements(simData,false,false);
		doPaintSurface(simData);
		animationDone=true;
	}

	/**
	 * Bewegt einen Transporter gemäß der angegebenen Start- und Zielstationen durch das System
	 * @param transporterIconEast	Icon für den Transporter für Fahrten nach rechts
	 * @param transporterIconWest	Icon für den Transporter für Fahrten nach links
	 * @param transporter	Transporterobjekt
	 * @param delay	Verzögerung pro Animationsschritt
	 * @param simData	Simulationsdatenobjekt
	 */
	protected void animate(final String transporterIconEast, final String transporterIconWest, final RunDataTransporter transporter, final int delay, final SimulationData simData) {
		animationDone=false;
		buildStaticClientsDrawListSingle(null,transporter,simData.currentTime);
		final List<AnimationPath> pathList=getAnimationPath(transporter,transporterIconEast,transporterIconWest,transporter.lastPosition,transporter.position);
		animatePathList(pathList,fullRecording?1:delay,simData);
		buildStaticClientsDrawList(null,null,simData.currentTime);
		updateSurfaceAnimationDisplayElements(simData,false,false);
		doPaintSurface(simData);
		animationDone=true;
	}

	private class DrawIcon {
		public final int x;
		public final int y;
		public final String icon;
		public final int stationA;
		public final int stationB;
		public final Object data;

		public DrawIcon(final DrawIcon drawIcon) {
			this.x=drawIcon.x;
			this.y=drawIcon.y;
			this.icon=drawIcon.icon;
			this.data=drawIcon.data;
			this.stationA=drawIcon.stationA;
			this.stationB=drawIcon.stationB;
		}

		public DrawIcon(final DrawIcon drawIcon, final int newSource, final int newDestination) {
			this.x=drawIcon.x;
			this.y=drawIcon.y;
			this.icon=drawIcon.icon;
			this.data=drawIcon.data;
			this.stationA=newSource;
			this.stationB=newDestination;
		}

		public DrawIcon(final int x, final int y, final String icon, final Object data) {
			this.x=x;
			this.y=y;
			this.icon=icon;
			this.data=data;

			int a=-1;
			int b=-1;
			if (data instanceof RunDataClient) {
				a=((RunDataClient)data).lastStationID;
				b=((RunDataClient)data).nextStationID;
			}
			if (data instanceof RunDataTransporter) {
				a=((RunDataTransporter)data).lastPosition;
				b=((RunDataTransporter)data).position;
			}
			if (data instanceof RunDataResourceOperator) {
				a=((RunDataResourceOperator)data).getStation();
				b=a;
			}
			stationA=a;
			stationB=b;
		}
	}

	private class DrawMovingIcon { /* Für die Netzwerk-Übertragung, nicht für die lokale Animation selbst */
		public final Object data;
		public final List<DrawIcon> list;

		public DrawMovingIcon(final Object data) {
			this.data=data;
			this.list=new ArrayList<>();
		}

		public void add(final DrawIcon drawIcon) {
			if (list.size()>0) {
				final DrawIcon lastIcon=list.get(list.size()-1);
				if (lastIcon.x==drawIcon.x && lastIcon.y==drawIcon.y && lastIcon.icon.equals(drawIcon.icon) && lastIcon.data==drawIcon.data) return;
			}
			list.add(new DrawIcon(drawIcon));
		}
	}

	/**
	 * Frames pro Sekunde über die gesamte Laufzeit der Animation
	 * @return	Liefert die Frames per second (fps) im Mittel über die gesamte Laufzeit der Animation
	 */
	public long getFPS() {
		return fpsFrameCount*1000/(System.currentTimeMillis()-fpsStartTime);
	}

	/**
	 * Gibt für einen einzelnen Kunden eine Bewegung von einer Station zu einer anderen an.<br>
	 * Wenn der Kunde nach dem Erstellen dieses Ereignisses aber vor dem Ausführen der Animation
	 * bereits ein zweites Mal verschoben wurde, so enthält dieses Objekt nach wie vor die bei
	 * der Erstellung gültige Verschiebung (die noch abgearbeitet werden muss) und noch nicht
	 * die neue Verschiebung.
	 * @author Alexander Herzog
	 * @see ModelSurfaceAnimatorBase#animate(MoveClient, int, SimulationData)
	 * @see ModelSurfaceAnimatorBase#getMoveClientList(RunDataClient)
	 */
	protected class MoveClient {
		/** Kunde, dessen Bewegung animiert werden soll */
		public final RunDataClient client;
		/** ID der Ausgangsstation */
		public int from;
		/** ID der Zielstation */
		public int to;

		/**
		 * Konstruktor der Klasse <code>MoveClient</code>
		 * @param client	Kunde, dessen Bewegung animiert werden soll
		 */
		public MoveClient(final RunDataClient client) {
			this.client=client;
			this.from=client.lastStationID;
			this.to=client.nextStationID;
		}
	}

	private Map<ModelElementPosition,Map<ModelElementPosition,Map<Integer,Point[]>>> routeCache=new HashMap<>();

	private class AnimationPath {
		private final String iconEast;
		private final String iconWest;
		private int[] x;
		private int[] y;
		private int count;
		private final Object data;

		public AnimationPath(final RunDataClient client) {
			data=client;
			iconEast=(client.icon==null)?DEFAULT_CLIENT_ICON_NAME:client.iconLast;
			iconWest=iconEast;
			x=new int[ANIMATION_STEPS+1];
			y=new int[ANIMATION_STEPS+1];
			count=0;
		}

		public AnimationPath(final Object data, final String iconEast, final String iconWest) {
			this.data=data;
			this.iconEast=(iconEast==null)?DEFAULT_CLIENT_ICON_NAME:iconEast;
			this.iconWest=(iconWest==null)?DEFAULT_CLIENT_ICON_NAME:iconWest;
			x=new int[ANIMATION_STEPS+1];
			y=new int[ANIMATION_STEPS+1];
			count=0;
		}

		public void addPoint(final int x, final int y) {
			if (count>0 && this.x[count-1]==x && this.y[count-1]==y) return;

			if (count==this.x.length) {
				this.x=Arrays.copyOf(this.x,this.x.length*2);
				this.y=Arrays.copyOf(this.y,this.y.length*2);
			}
			this.x[count]=x;
			this.y[count]=y;
			count++;
		}

		public int getPointCount() {
			return count;
		}

		public DrawIcon getDrawClient(int pointNr) {
			if (count==0) return new DrawIcon(0,0,iconEast,data);

			if (pointNr<0) pointNr=0;
			if (pointNr>=count) pointNr=count-1;

			final String icon=(pointNr>0 && x[pointNr-1]>x[pointNr])?iconWest:iconEast;

			return new DrawIcon(x[pointNr],y[pointNr],icon,data);
		}

		private ModelElementEdge.LineMode getLineMode(final ModelElementPosition source, final ModelElementPosition destination) {
			for (ModelElement element: surface.getElements()) if (element instanceof ModelElementEdge) {
				final ModelElementEdge edge=(ModelElementEdge)element;
				if (edge.getConnectionStart()==source && edge.getConnectionEnd()==destination) {
					final ModelElementEdge.LineMode lineMode=edge.getLineMode();
					if (lineMode==null) return edge.getModel().edgeLineMode; else return lineMode;
				}
			}

			return ModelElementEdge.LineMode.DIRECT;
		}

		private int sign(final int value) {
			if (value>0) return 1;
			if (value<0) return -1;
			return 0;
		}

		private Point[] calcRoute(final ModelElementPosition source, final ModelElementPosition destination, final int steps) {
			final Point p1=source.getMiddlePosition(false);
			final Point p2=destination.getMiddlePosition(false);
			final Point[] route=new Point[steps+1];
			route[0]=p1;
			route[steps]=p2;
			if (getLineMode(source,destination)==ModelElementEdge.LineMode.DIRECT) {
				final int deltaX=p2.x-p1.x;
				final int deltaY=p2.y-p1.y;
				for (int i=1;i<steps;i++) {
					int x=p1.x+i*deltaX/steps;
					int y=p1.y+i*deltaY/steps-ICON_SIZE/2;
					route[i]=new Point(x,y);
				}
			} else {
				final Point p1a=source.getConnectionToPosition(p2);
				final int middleX=(p1.x+p2.x)/2;
				final int middleY=(p1.y+p2.y)/2;
				if (p1a.x!=p1.x) {
					/* Nach rechts oder links */
					final int deltaX1=middleX-p1.x;
					final int deltaY=p2.y-p1.y;
					final int deltaX2=p2.x-middleX;
					final int deltaX1sign=sign(deltaX1);
					final int deltaYsign=sign(deltaY);
					final int deltaX2sign=sign(deltaX2);
					final int deltaSum1=Math.abs(deltaX1);
					final int deltaSum2=deltaSum1+Math.abs(deltaY);
					final int deltaSum3=deltaSum2+Math.abs(deltaX2);
					for (int i=1;i<steps;i++) {
						final int pos=i*deltaSum3/steps;
						final int x;
						final int y;
						if (pos<deltaSum1) {
							/* Im 1. Abschnitt */
							x=p1.x+pos*deltaX1sign;
							y=p1.y-ICON_SIZE/2;
						} else {
							if (pos<deltaSum2) {
								/* Im 2. Abschnitt */
								x=middleX-ICON_SIZE/2;
								y=p1.y+(pos-deltaSum1)*deltaYsign;
							} else {
								/* Im 3. Abschnitt */
								x=middleX+(pos-deltaSum2)*deltaX2sign;
								y=p2.y-ICON_SIZE/2;
							}
						}
						route[i]=new Point(x,y);
					}
				} else {
					/* Nach oben oder unten */
					final int deltaY1=middleY-p1.y;
					final int deltaX=p2.x-p1.x;
					final int deltaY2=p2.y-middleY;
					final int deltaY1sign=sign(deltaY1);
					final int deltaXsign=sign(deltaX);
					final int deltaY2sign=sign(deltaY2);
					final int deltaSum1=Math.abs(deltaY1);
					final int deltaSum2=deltaSum1+Math.abs(deltaX);
					final int deltaSum3=deltaSum2+Math.abs(deltaY2);
					for (int i=1;i<steps;i++) {
						final int pos=i*deltaSum3/steps;
						final int x;
						final int y;
						if (pos<deltaSum1) {
							/* Im 1. Abschnitt */
							x=p1.x-ICON_SIZE/2;
							y=p1.y+pos*deltaY1sign;
						} else {
							if (pos<deltaSum2) {
								/* Im 2. Abschnitt */
								x=p1.x+(pos-deltaSum1)*deltaXsign;
								y=middleY-ICON_SIZE/2;
							} else {
								/* Im 3. Abschnitt */
								x=p2.x-ICON_SIZE/2;
								y=middleY+(pos-deltaSum2)*deltaY2sign;
							}
						}
						route[i]=new Point(x,y);
					}
				}
			}
			return route;
		}

		public void buildRoute(final ModelElementPosition source, final ModelElementPosition destination, final int steps) {
			final Point[] route;
			Map<ModelElementPosition,Map<Integer,Point[]>> destCache=routeCache.get(source);
			if (destCache==null) {
				route=calcRoute(source,destination,steps);
				routeCache.put(source,destCache=new HashMap<>());
				final Map<Integer,Point[]> stepsCache=new HashMap<>();
				destCache.put(destination,stepsCache);
				stepsCache.put(steps,route);
			} else {
				Map<Integer,Point[]> stepsCache=destCache.get(destination);
				if (stepsCache==null) {
					route=calcRoute(source,destination,steps);
					destCache.put(destination,stepsCache=new HashMap<>());
					stepsCache.put(steps,route);
				} else {
					final Point[] r=stepsCache.get(steps);
					if (r==null) stepsCache.put(steps,route=calcRoute(source,destination,steps)); else route=r;
				}
			}

			for (Point p: route) addPoint(p.x,p.y);
		}
	}

	private String base64Icon(final String iconName) {
		final BufferedImage image=images.get(iconName,modelImages,ICON_SIZE,1.0);
		try (final ByteArrayOutputStream output=new ByteArrayOutputStream()) {
			if (!ImageIO.write(image,"png",output)) return "";
			final String base64bytes=Base64.getEncoder().encodeToString(output.toByteArray());
			return "data:image/png;base64,"+base64bytes;
		} catch (IOException e) {
			return "";
		}
	}

	private long lastFullRecordingTimeStep=-1;
	private List<DrawIcon> oldFullRecordingStaticIcons;
	private List<DrawMovingIcon> oldFullRecordingMovingIcons;

	/**
	 * Stellt ein, dass während der Animation Daten für die Netzwerkausgabe aufgezeichnet werden sollen.
	 * Wird die Animation im Play-Modus gestartet, so wird diese Ausgabe automatisch zurückgesetzt.
	 * Diese Funktion sollte vor jedem Einzelschritt, der erfasst werden soll, aufgerufen werden.
	 * @param fullRecording	Vollständige Erfassung ein- oder ausschalten
	 * @see ModelSurfaceAnimatorBase#getAnimationStepInfo(long, RunModel, List, List)
	 */
	public void setFullRecording(final boolean fullRecording) {
		this.fullRecording=fullRecording;
		fullRecordingStaticIcons=null;
		fullRecordingMovingIcons=null;
	}

	private Map<String,String> drawIconToMap(final RunModel runModel, final DrawIcon drawIcon) {
		final Map<String,String> map=new HashMap<>();
		map.put("x",""+drawIcon.x);
		map.put("y",""+drawIcon.y);
		map.put("stationID1",""+drawIcon.stationA);
		map.put("stationID2",""+drawIcon.stationB);
		final String iconSize=NumberTools.formatLongNoGrouping(ICON_SIZE);
		map.put("w",iconSize);
		map.put("h",iconSize);
		map.put("icon",""+base64Icon(drawIcon.icon));
		if (drawIcon.data!=null) {
			if (drawIcon.data instanceof RunDataClient) {
				final RunDataClient client=(RunDataClient)drawIcon.data;
				map.put("type","client");
				map.put("id",""+client.hashCode());
				map.put("typeName",runModel.clientTypes[client.typeLast]);
			}
			if (drawIcon.data instanceof RunDataResourceOperator) {
				final RunDataResourceOperator operator=(RunDataResourceOperator)drawIcon.data;
				map.put("type","operator");
				map.put("id",""+operator.hashCode());
				map.put("typeName",operator.name);
			}
			if (drawIcon.data instanceof RunDataTransporter) {
				final RunDataTransporter transporter=(RunDataTransporter)drawIcon.data;
				map.put("type","transporter");
				map.put("id",""+transporter.hashCode());
			}
		}
		return map;
	}

	/**
	 * Füllt zwei Listen mit Daten zu den statischen Icons und den bewegten Icons aus dem
	 * aktuellen Animationsschritt
	 * @param time	Aktuelle Systemzeit
	 * @param runModel	Laufzeitmodell
	 * @param staticElements	Statische Icons
	 * @param movingElements	Bewerte Icons
	 * @see ModelSurfaceAnimatorBase#setFullRecording(boolean)
	 */
	public void getAnimationStepInfo(final long time, final RunModel runModel, final List<Map<String,String>> staticElements, final List<Map<String,Object>> movingElements) {
		while (!animationDone) {
			try {Thread.sleep(50);} catch (InterruptedException e) {}
		}

		drawClientsMutex.acquireUninterruptibly();
		try {

			if (time==lastFullRecordingTimeStep) {
				if (oldFullRecordingStaticIcons!=null) {
					final List<DrawIcon> list=new ArrayList<>();
					for (DrawIcon icon: oldFullRecordingStaticIcons) {
						boolean needAdd=true;
						if (fullRecordingStaticIcons!=null) for (DrawIcon icon2: fullRecordingStaticIcons) if (icon.data==icon2.data) {needAdd=false; break;}
						if (needAdd) list.add(icon);
					}
					if (fullRecordingStaticIcons!=null) list.addAll(fullRecordingStaticIcons);
					oldFullRecordingStaticIcons=null;
					if (list.size()==0) fullRecordingStaticIcons=null; else fullRecordingStaticIcons=list;
				}
				if (oldFullRecordingMovingIcons!=null) {
					final List<DrawMovingIcon> list=new ArrayList<>();
					for (DrawMovingIcon icon: oldFullRecordingMovingIcons) {
						boolean needAdd=true;
						if (fullRecordingMovingIcons!=null) for (DrawMovingIcon icon2 : fullRecordingMovingIcons) if (icon.data==icon2.data) {needAdd=false; break;}
						if (needAdd) list.add(icon);
					}
					if (fullRecordingMovingIcons!=null) list.addAll(fullRecordingMovingIcons);
					oldFullRecordingMovingIcons=null;
					if (list.size()==0) fullRecordingMovingIcons=null; else fullRecordingMovingIcons=list;
				}
			}
			lastFullRecordingTimeStep=time;

			if (fullRecordingStaticIcons!=null) for (DrawIcon icon: fullRecordingStaticIcons) {
				staticElements.add(drawIconToMap(runModel,icon));
			}

			if (fullRecordingMovingIcons!=null) for (DrawMovingIcon move: fullRecordingMovingIcons) {
				final Map<String,Object> map=new HashMap<>();
				movingElements.add(map);

				int source=-1;
				int dest=-1;
				for (int i=0;i<move.list.size();i++) {
					final DrawIcon draw=move.list.get(i);
					if (draw.stationA>=0 && draw.stationB>=0) {
						source=draw.stationA;
						dest=draw.stationB;
						break;
					}
				}
				int nr=0;

				for (int i=0;i<move.list.size();i++) {
					final DrawIcon draw=move.list.get(i);
					if (draw.stationA<0 && draw.stationB==source) {
						map.put(NumberTools.formatLongNoGrouping(nr),drawIconToMap(runModel,new DrawIcon(draw,source,dest)));
						nr++;
						break;
					}
				}

				for (int i=0;i<move.list.size();i++) {
					final DrawIcon draw=move.list.get(i);
					if (draw.stationA>=0 && draw.stationB>=0) {
						map.put(NumberTools.formatLongNoGrouping(nr),drawIconToMap(runModel,draw));
						nr++;
					}
				}

				for (int i=0;i<move.list.size();i++) {
					final DrawIcon draw=move.list.get(i);
					if (draw.stationA<0 && draw.stationB==dest) {
						map.put(NumberTools.formatLongNoGrouping(nr),drawIconToMap(runModel,new DrawIcon(draw,source,dest)));
						nr++;
						break;
					}
				}

				map.put("count",""+nr);
			}

			oldFullRecordingStaticIcons=fullRecordingStaticIcons;
			oldFullRecordingMovingIcons=fullRecordingMovingIcons;

			fullRecordingStaticIcons=null;
			fullRecordingMovingIcons=null;

		} finally {drawClientsMutex.release();}
	}

	/**
	 * Versucht den als Zeichenkette übergebenen Ausdruck im Kontext der Simulationsdaten zu berechnen
	 * @param expression	Zu berechnender Ausdruck
	 * @return	Ergebnis oder im Fehlerfall <code>null</code>
	 */
	public Double calculateExpression(final String expression) {
		if (storedSimData==null) return null;
		final ExpressionCalc calc=new ExpressionCalc(storedSimData.runModel.variableNames);
		if (calc.parse(expression)>=0) return null;
		storedSimData.runData.setClientVariableValues(null);
		return calc.calc(storedSimData.runData.variableValues,storedSimData,null);
	}

	/**
	 * Versucht das als Zeichenkette übergebene Javascript im Kontext der Simulationsdaten auszuführen
	 * @param script	Auszuführendes Javascript
	 * @return	Rückgabewert (Text oder Fehlermeldung oder leere Zeichenkette)
	 */
	public String runJavaScript(final String script) {
		if (storedSimData==null) return Language.tr("Animation.NoSimulationDataAvailable");
		final JSRunSimulationData jsRunner=new JSRunSimulationData(true,false);
		jsRunner.compile(script);
		jsRunner.setSimulationDataNoClient(storedSimData);
		return jsRunner.runCompiled();
	}

	/**
	 * Versucht den als Zeichenkette übergebenen Java-Code im Kontext der Simulationsdaten auszuführen
	 * @param script	Auszuführender Java-Code
	 * @return	Rückgabewert (Text oder Fehlermeldung oder leere Zeichenkette)
	 */
	public String runJava(final String script) {
		if (storedSimData==null) return Language.tr("Animation.NoSimulationDataAvailable");
		final DynamicRunner runner=DynamicFactory.getFactory().load(script);
		if (runner.getStatus()!=DynamicStatus.OK) return DynamicFactory.getLongStatusText(runner);
		runner.parameter.system=new SystemImpl(storedSimData);
		final StringBuilder sb=new StringBuilder();
		runner.parameter.output=new OutputImpl(s->sb.append(s),false);
		final Object result=runner.run();
		if (runner.getStatus()==DynamicStatus.RUN_ERROR) return runner.getError();
		if (result!=null) {
			if (sb.length()>0) sb.append("\n");
			sb.append(result.toString());
		}
		return sb.toString();
	}
}