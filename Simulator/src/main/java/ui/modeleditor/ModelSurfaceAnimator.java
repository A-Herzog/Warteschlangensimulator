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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.math3.util.FastMath;

import mathtools.TimeTools;
import simulator.coreelements.RunElement;
import simulator.elements.RunElementTransportDestination;
import simulator.elements.RunElementTransportSource;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunDataTransporter;
import simulator.runmodel.SimulationData;
import ui.mjpeg.VideoSystem;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementAnimationForceMove;


/**
 * Diese Klasse erm�glicht die Animation eines Simulationsmodells
 * @author Alexander Herzog
 */
public class ModelSurfaceAnimator extends ModelSurfaceAnimatorBase {
	/**
	 * Bewegungsmodus w�hrend der Animation
	 * @author Alexander Herzog
	 * @see ModelSurfaceAnimator#ModelSurfaceAnimator(JFrame, ModelSurfacePanel, ModelAnimationImages, AnimationMoveMode, boolean, boolean)
	 */
	public enum AnimationMoveMode {
		/** Kunden einzeln bewegen */
		MODE_SINGLE,

		/** Versucht, zeitgleiche Kundenbewegungen auch zeitgleich auszuf�hren */
		MODE_MULTI
	}

	/** Animationsmodus */
	private final AnimationMoveMode mode;
	/** Wird hier ein Wert ungleich <code>null</code> angegeben, so werden die gezeichneten Animationsschritte in dem �bergebenen System aufgezeichnet */
	private VideoSystem recordSystem;
	/** Skalierung der Bilder vor dem �bernehmen ins Video (0.01..1) */
	private double scaleFrame;
	/** F�gt in das Video den jeweils aktuellen Simulationszeit-Wert ein */
	private boolean paintTimeStamp;
	/** Gr��e der Zeichenfl�che (f�r die Aufzeichnung von Videos, {@link #recordStep(SimulationData)}) */
	private Dimension surfaceSize;

	/**
	 * Aktuelle Simulationszeit
	 * @see #process(SimulationData, RunDataClient, int)
	 */
	private long currentTime;

	/**
	 * Konstruktor der Klasse <code>ModelSurfaceAnimator</code>
	 * @param window	�bergeordnetes Fenster in dem sich das Panel befindet (zur Minimiert-Erkennung, um in diesem Fall bei Fernsteuerung dennoch Animationen auszul�sen)
	 * @param surfacePanel	Panel, in das die Animation eingezeichnet werden soll
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param mode	Animationsmodus
	 * @param multiCore	Gibt an, ob Simulation und Darstellung auf zwei Threads aufgeteilt werden sollen
	 * @param animateResources	Ressourcen und Transporter w�hrend der Animation anzeigen
	 */
	public ModelSurfaceAnimator(final JFrame window, final ModelSurfacePanel surfacePanel, final ModelAnimationImages modelImages, final AnimationMoveMode mode, final boolean multiCore, final boolean animateResources) {
		super(window,surfacePanel,modelImages,multiCore,animateResources);
		this.mode=mode;
	}

	/**
	 * Simulationszeit an der die letzte Bewegung stattgefunden hat
	 * @see #processMulti(SimulationData, RunDataClient, int)
	 */
	private long lastMultiMoveTime;

	/**
	 * Liste der sich bewegenden Kunden-Icons
	 * @see #processMulti(SimulationData, RunDataClient, int)
	 */
	private List<MoveClient> multiMovedClients;


	/**
	 * Setzt das System als Vorbereitung eines neuen Animationslaufs zur�ck.<br>
	 * Wird dem <code>ModelSurfacePanel</code>-Objekt, welches im Konstruktor �bergeben wurde ein neues <code>ModelSurface</code> �bergeben, muss dieses Objekt �ber diese <code>reset</code>-Methode entsprechend aktualisiert werden.
	 */
	@Override
	public void reset() {
		super.reset();
		lastMultiMoveTime=-1;
		multiMovedClients=new ArrayList<>();
	}

	/**
	 * Stellt ein, ob und wenn ja in welchem Objekt die Animation aufgezeichnet werden soll
	 * @param recordSystem	Wird hier ein Wert ungleich <code>null</code> �bergeben, so werden die gezeichneten Animationsschritte in dem �bergebenen System aufgezeichnet
	 * @param scaleFrame	Skalierung der Bilder vor dem �bernehmen ins Video (0.01..1)
	 * @param paintTimeStamp	F�gt in das Video den jeweils aktuellen Simulationszeit-Wert ein
	 */
	public void setRecordSystem(final VideoSystem recordSystem, final double scaleFrame, final boolean paintTimeStamp) {
		this.recordSystem=recordSystem;
		this.scaleFrame=Math.max(0.01,Math.min(1.0,scaleFrame));
		this.paintTimeStamp=paintTimeStamp;
	}

	/**
	 * Berechnet die Gr��e der gesamten Zeichenfl�che.<br>
	 * (F�r die Aufzeichnung von Animationen notwendig.)
	 */
	public void calcSurfaceSize() {
		int minX=Integer.MAX_VALUE;
		int minY=Integer.MAX_VALUE;
		int maxX=0;
		int maxY=0;
		final double zoom=surfacePanel.getZoom();
		for (ModelElement element: surfacePanel.getSurface().getElements()) if (!(element instanceof ModelElementAnimationConnect)) {
			final Point p1=element.getPosition(true);
			final Point p2=element.getLowerRightPosition();
			if (p1!=null) {
				if (p1.x>=0) minX=FastMath.min(minX,(int)Math.round(p1.x*zoom));
				if (p1.y>=0) minY=FastMath.min(minY,(int)Math.round(p1.y*zoom));
			}
			if (p2!=null) {
				maxX=FastMath.max(maxX,(int)Math.round(p2.x*zoom));
				maxY=FastMath.max(maxY,(int)Math.round(p2.y*zoom));
			}
		}

		surfaceSize=new Dimension((int)FastMath.ceil((maxX+minX)/8.0)*8,(int)FastMath.ceil((maxY+minY)/8.0)*8);
	}

	/**
	 * Tr�gt die aktuelle Simulationszeit in ein Ausgabe-Grafikobjekt
	 * ein, welches dann als Video aufgezeichnet wird.
	 * @param graphics	Ausgabe-Grafikobjekt
	 * @param area	Ausgabefl�chengr��e
	 * @param zoom	Aktueller Zoomfaktor
	 * @param time	Aktueller Zeitpunkt (in Simulationszeit)
	 * @see #recordStep(SimulationData)
	 */
	private void surfacePaintTime(final Graphics graphics, final Rectangle area, final double zoom, final long time) {
		final String timeCode=TimeTools.formatLongTime(time);

		graphics.setFont(new Font(ModelElementBox.DEFAULT_FONT_TYPE,Font.PLAIN,(int)FastMath.round(10*zoom)));
		final FontMetrics metrics=graphics.getFontMetrics();

		graphics.setColor(new Color(220,220,220,128));
		int h=metrics.getDescent()+metrics.getAscent();
		graphics.fillRect(5,area.height-5-h,metrics.stringWidth(timeCode),h);

		graphics.setColor(Color.BLACK);
		graphics.drawString(timeCode,5,area.height-metrics.getDescent()-5);
	}

	/**
	 * Cache f�r Zeichenfl�chen-Bilder in Originalgr��e,
	 * wenn skaliert aufgezeichnet werden soll.
	 * @see #recordStep(SimulationData)
	 */
	private BufferedImage recordBigImage;

	/**
	 * Zeichnet einen Simulationsschritt auf
	 * @param simData	Simulationsdatenobjekt
	 */
	private void recordStep(final SimulationData simData) {
		final BufferedImage image;

		if (scaleFrame==1.0) {
			/* Direkt in das Ergebnisbild zeichnen */
			image=recordSystem.getImageObjectFromCache(surfaceSize.width,surfaceSize.height);

			final Graphics graphics=image.getGraphics();
			final Rectangle viewArea=new Rectangle(0,0,surfaceSize.width,surfaceSize.height);
			surfacePanel.paintElements(graphics,viewArea,surfacePanel.getBackgroundImageMode(),true,surfacePanel.getRaster(),surfacePanel.getColors(),surfacePanel.getBackgroundImage(),surfacePanel.getBackgroundImageHash(),surfacePanel.getBackgroundImageScale(),false);
			surfaceAddOnPaint(graphics,surfacePanel.getZoom());
			if (paintTimeStamp) surfacePaintTime(graphics,viewArea,surfacePanel.getZoom(),(long)Math.floor(simData.currentTime*simData.runModel.scaleToSeconds));
		} else {
			final int w=(int)Math.round(surfaceSize.width*scaleFrame);
			final int h=(int)Math.round(surfaceSize.height*scaleFrame);
			image=recordSystem.getImageObjectFromCache(w,h);

			if (recordBigImage==null) recordBigImage=new BufferedImage(surfaceSize.width,surfaceSize.height,BufferedImage.TYPE_INT_RGB);

			final Graphics graphics=recordBigImage.getGraphics();
			final Rectangle viewArea=new Rectangle(0,0,surfaceSize.width,surfaceSize.height);
			surfacePanel.paintElements(graphics,viewArea,surfacePanel.getBackgroundImageMode(),true,surfacePanel.getRaster(),surfacePanel.getColors(),surfacePanel.getBackgroundImage(),surfacePanel.getBackgroundImageHash(),surfacePanel.getBackgroundImageScale(),false);
			surfaceAddOnPaint(graphics,surfacePanel.getZoom());
			if (paintTimeStamp) surfacePaintTime(graphics,viewArea,surfacePanel.getZoom(),(long)Math.floor(simData.currentTime*simData.runModel.scaleToSeconds));

			image.getGraphics().drawImage(recordBigImage,0,0,w,h,null);
		}

		recordSystem.addFrame(image,currentTime);
	}

	@Override
	protected void doPaintSurface(final SimulationData simData) {
		super.doPaintSurface(simData);
		if (recordSystem!=null) recordStep(simData);
	}

	/**
	 * L�uft gerade eine Verarbeitung in {@link #process(SimulationData, RunDataClient, int)}?<br>
	 * Wird die process-Methode synchronized, so kann es einen Deadlock geben. Mit Hilfe dieser
	 * Abfrage hingegen werden doppelte Zeichenbefehle notfalls einfach verworfen.
	 */
	private boolean inProcessClient=false;

	/**
	 * Teilt dem Animationssystem mit, dass sich ein Kunde bewegt hat oder es sonst Ver�nderungen im System gab.
	 * @param simData	Simulationsdaten-Objekt
	 * @param client	Kunde, der sich bewegt hat (kann auch <code>null</code> sein)
	 * @param delay	Verz�gerung pro Animationsschritt
	 */
	public void process(final SimulationData simData, final RunDataClient client, int delay) {
		if (inProcessClient) return;
		inProcessClient=true;
		try {
			currentTime=simData.currentTime;
			preProcess(simData);

			if (client!=null) {
				final RunElement lastStation=(client.lastStationID>=0)?simData.runModel.elementsFast[client.lastStationID]:null;
				if (lastStation instanceof RunElementTransportSource) {
					final RunElement nextStation=(client.nextStationID>=0)?simData.runModel.elementsFast[client.nextStationID]:null;
					if (nextStation instanceof RunElementTransportDestination) {
						/* Kunde wird �ber Transport-Elemente bewegt, nicht �ber eine Kante */
						processSingle(simData,null,delay);
						return;
					}
				}
			}

			switch (mode) {
			case MODE_SINGLE: processSingle(simData,client,delay); break;
			case MODE_MULTI: processMulti(simData,client,delay); break;
			}
		} finally {
			inProcessClient=false;
		}
	}

	/**
	 * Teilt dem Animationssystem mit, dass sich ein Transporter bewegt hat.
	 * @param simData	Simulationsdaten-Objekt
	 * @param transporter	Transporter, der sich bewegt hat (darf nicht <code>null</code> sein)
	 * @param delay	Verz�gerung pro Animationsschritt
	 */
	public void process(final SimulationData simData, final RunDataTransporter transporter, final int delay) {
		currentTime=simData.currentTime;
		preProcess(simData);

		if (transporter==null) return;

		final int id1=transporter.lastPosition;
		final int id2=transporter.position;
		if (id1==id2) return; /* kein echter Transport, sondern Transporter steht bei Simulationsbeginn an dieser Station */
		final String iconEast;
		final String iconWest;
		if (transporter.clients.size()==0) {
			iconEast=(transporter.iconEastEmpty==null || transporter.iconEastEmpty.isEmpty())?DEFAULT_TRANSPORTER_EAST_EMPTY_ICON_NAME:transporter.iconEastEmpty;
			iconWest=(transporter.iconWestEmpty==null || transporter.iconWestEmpty.isEmpty())?DEFAULT_TRANSPORTER_WEST_EMPTY_ICON_NAME:transporter.iconWestEmpty;
		} else {
			iconEast=(transporter.iconEastLoaded==null || transporter.iconEastLoaded.isEmpty())?DEFAULT_TRANSPORTER_EAST_ICON_NAME:transporter.iconEastLoaded;
			iconWest=(transporter.iconWestLoaded==null || transporter.iconWestLoaded.isEmpty())?DEFAULT_TRANSPORTER_WEST_ICON_NAME:transporter.iconWestLoaded;
		}

		animate(iconEast,iconWest,transporter,delay,simData);
	}

	/**
	 * Teilt dem Animationssystem mit, dass sich ein Kunde bewegt hat oder es sonst Ver�nderungen im System gab
	 * (Einzelbewegung).
	 * @param simData	Simulationsdaten-Objekt
	 * @param client	Kunde, der sich bewegt hat (kann auch <code>null</code> sein)
	 * @param delay	Verz�gerung pro Animationsschritt
	 * @see #process(SimulationData, RunDataClient, int)
	 */
	private void processSingle(final SimulationData simData, final RunDataClient client, final int delay) {
		if (client==null || client.lastStationID==-1 || client.nextStationID==-1) return;

		animate(new MoveClient(client),delay,simData);
	}

	/**
	 * Teilt dem Animationssystem mit, dass sich ein Kunde bewegt hat oder es sonst Ver�nderungen im System gab
	 * (Mehrfachbewegung).
	 * @param simData	Simulationsdaten-Objekt
	 * @param client	Kunde, der sich bewegt hat (kann auch <code>null</code> sein)
	 * @param delay	Verz�gerung pro Animationsschritt
	 * @see #process(SimulationData, RunDataClient, int)
	 */
	private void processMulti(final SimulationData simData, RunDataClient client, final int delay) {
		if (client!=null && (client.lastStationID==-1 || client.nextStationID==-1)) client=null;
		boolean forceMove=false;
		if (client!=null) {
			final ModelElement next=surface.getById(client.nextStationID);
			if (next instanceof ModelElementAnimationForceMove) forceMove=true;
		}

		boolean alreadyInMoveList=false;
		if (client!=null) for (MoveClient move: multiMovedClients) if (move.client.clientNumber==client.clientNumber) {
			alreadyInMoveList=true;
			break;
		}

		if (alreadyInMoveList) {
			animate(multiMovedClients,delay,simData,false);
			multiMovedClients.clear();
		}

		if (client==null || lastMultiMoveTime!=simData.currentTime || forceMove) {

			if (client!=null) {
				multiMovedClients.add(new MoveClient(client));
				client=null;
			}

			animate(multiMovedClients,delay,simData,true);

			lastMultiMoveTime=simData.currentTime;
			multiMovedClients.clear();
		}

		if (client!=null) {
			multiMovedClients.add(new MoveClient(client));
		}
	}

	/*
	private void processMulti(final SimulationData simData, RunDataClient client, final int delay) {
		if (client!=null && (client.lastStationID==-1 || client.nextStationID==-1)) client=null;

		boolean alreadyInMoveList=false;
		if (client!=null) for (MoveClient move: multiMovedClients) if (move.client.clientNumber==client.clientNumber) {
			alreadyInMoveList=true;
			move.to=client.nextStationID;
			break;
		}
		if (client==null || lastMultiMoveTime!=simData.currentTime || alreadyInMoveList) {

			if (client!=null && lastMultiMoveTime!=simData.currentTime && !alreadyInMoveList) {

				multiMovedClients.add(new MoveClient(client));
				client=null;
			}

			animate(multiMovedClients,delay,simData,true);

			lastMultiMoveTime=simData.currentTime;
			multiMovedClients.clear();
		}

		if (client!=null)
			multiMovedClients.add(new MoveClient(client));
	}
	 */
}
