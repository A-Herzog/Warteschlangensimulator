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
package ui.modelproperties;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import ui.tools.FlatLaFHelper;

/**
 * Stellt innerhalb eines {@link ScheduleTableModelDataDialog} Dialogs
 * einen Zeitplan dar.
 * @author Alexander Herzog
 * @see ScheduleTableModelDataDialog
 */
public class SchedulePanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6915816603375099302L;

	/**
	 * Anzahl der Beschriftungen unter den Balken an der x-Achse
	 */
	private static final int XSTEPS=10;

	/** Werte in den einzelnen Zeitslots */
	private final List<Integer> data;
	/** Nummer des ganz links dargestellten Zeitslots */
	private int startPosition;
	/** Als Maximum anzuzeigender y-Wert */
	private int editorMaxY;
	/** Zeitspanne pro Zeitslot (für die Beschriftung unter den Slots) */
	private int durationPerSlot;
	/** Breite pro darzustellendem Slot */
	private final int widthPerSlot;

	/** Schriftart für Standardtexte */
	private final Font fontDefault;
	/** Schriftart für fett darzustellende Texte */
	private final Font fontBold;

	/**
	 * Größe (von der Grundlinie nach oben)
	 * der Schrift in der Darstellung
	 */
	private int fontYDelta;

	/**
	 * Gesamtgröße (von ganz oben bis unter die Grundlinie)
	 * der Schrift in der Darstellung
	 */
	private int fontHeight;

	/** Abstand zu Diagrammbox links */
	private int marginBoxLeft;
	/** Abstand zu Diagrammbox oben */
	private int marginBoxTop;
	/** Abstand zu Diagrammbox unten */
	private int marginBoxBottom;
	/** Breite der Diagrammbox */
	private int boxWidth;
	/** Höhe der Diagrammbox */
	private int boxHeight;

	/**
	 * Letzte Position des Mauszeigers
	 * (um auch außerhalb von Maus-Ereignissen auf diese zugreifen zu können)
	 */
	private Point lastMousePoint;

	/**
	 * Erfolgt die Darstellung im Dark-Modus?
	 */
	public boolean isDark;

	/**
	 * Konstruktor der Klasse
	 * @param data	Werte in den einzelnen Zeitslots
	 * @param editorMaxY	Als Maximum anzuzeigender y-Wert
	 * @param durationPerSlot	Zeitspanne pro Zeitslot (für die Beschriftung unter den Slots)
	 * @param widthPerSlot	Breite pro darzustellendem Slot
	 */
	public SchedulePanel(final List<Integer> data, final int editorMaxY, final int durationPerSlot, final int widthPerSlot) {
		super();
		isDark=FlatLaFHelper.isDark();
		this.data=data;
		startPosition=0;
		this.editorMaxY=10;
		this.durationPerSlot=3600;
		this.widthPerSlot=widthPerSlot;
		setSetupData(editorMaxY,durationPerSlot);
		addMouseListener(new SchedulePanelMouseListener());
		addMouseMotionListener(new SchedulePanelMouseListener());

		fontDefault=new Font(Font.DIALOG,0,12);
		fontBold=new Font(Font.DIALOG,Font.BOLD,12);
		lastMousePoint=null;
	}

	/**
	 * Liefert die aktuellen Werte der einzelnen Zeitslots
	 * @return	Aktuelle Werte der Zeitslots
	 */
	public List<Integer> getData() {
		return data;
	}

	/**
	 * Liefert die aktuellen Werte der einzelnen Zeitslots als Tabelle
	 * @return	Aktuelle Werte der Zeitslots als Tabelle
	 */
	public Table getDataAsTable() {
		final Table table=new Table();
		final int max=getLastNonNullSlot();
		for (int i=0;i<=max;i++) {
			final Integer I=data.get(i);
			final int value=(I==null)?0:I.intValue();
			table.addLine(new int[] {value});
		}
		return table;
	}

	/**
	 * Lädt den Zeitplan aus einer Tabellenzeile.
	 * @param line	Tabellenzeile der der Zeitplan entnommen werden soll.
	 * @return	Liefert <code>true</code>, wenn der Zeitplan geladen werden konnte
	 */
	private boolean setDataFromTable(final List<String> line) {
		if (line==null || line.size()==0) return false;

		final int size=line.size();
		final List<Integer> newData=new ArrayList<>(size);
		for (int i=0;i<size;i++) {
			final Integer I=NumberTools.getNotNegativeInteger(line.get(i));
			if (I==null) return false;
			newData.add(I);
		}

		data.clear();
		data.addAll(newData);

		return true;
	}

	/**
	 * Lädt den Zeitplan aus einer Tabelle.
	 * @param table	Tabelle der der Zeitplan entnommen werden soll.
	 * @return	Liefert <code>true</code>, wenn der Zeitplan geladen werden konnte
	 */
	public boolean setDataFromTable(final Table table) {
		if (table==null) return false;
		final int rows=table.getSize(0);
		final int cols=table.getSize(1);

		if (rows==0 || cols==0) return false;
		if (rows>1 && cols>1) return false;

		final boolean ok;
		if (rows>1) {
			/* Daten in Zeilen */
			ok=setDataFromTable(table.transpose().getLine(0));
		} else {
			/* Daten in Spalten */
			ok=setDataFromTable(table.getLine(0));
		}

		if (ok) invalidate();
		return ok;
	}

	/**
	 * Liefert den Index des letzten Zeitslots, der einen Wert ungleich 0 enthält.
	 * @return	Index des letzten Zeitslots, der einen Wert ungleich 0 enthält (kann -1 sein, wenn der Zeitplan insgesamt leer ist)
	 */
	public int getLastNonNullSlot() {
		if (data==null) return -1;
		for (int i=data.size()-1;i>=0;i--) {
			final Integer I=data.get(i);
			if (I!=null && I.intValue()!=0) return i;
		}
		return -1;
	}

	/**
	 * Liefert die 0-basierende Nummer des ganz links dargestellten Zeitslots.
	 * @return	Nummer des ganz links dargestellten Zeitslots
	 */
	public int getStartPosition() {
		return startPosition;
	}

	/**
	 * Stellt die Nummer 0-basierende Nummer des ganz links dargestellten Zeitslots ein.
	 * @param startPosition	Nummer des ganz links dargestellten Zeitslots
	 */
	public void setStartPosition(final int startPosition) {
		this.startPosition=Math.max(0,startPosition);
		repaint();
	}

	/**
	 * Ändert die im Konstruktor eingestellten Basiswerte der Darstellung.
	 * @param editorMaxY	Als Maximum anzuzeigender y-Wert
	 * @param durationPerSlot	Zeitspanne pro Zeitslot (für die Beschriftung unter den Slots)
	 */
	public void setSetupData(final int editorMaxY, final int durationPerSlot) {
		this.editorMaxY=editorMaxY;
		this.durationPerSlot=durationPerSlot;
		repaint();
	}

	/**
	 * Liefert die in Abhängigkeit von der Breite des Panels anzuzeigende Anzahl an Zeitslots.
	 * @return	Anzuzeigende Anzahl an Zeitslots
	 */
	public int getDisplaySlots() {
		return getWidth()/widthPerSlot;
	}

	/**
	 * Zeichnet den Rahmen (Hintergrund für den gesamten Bereich)
	 * @param g	Grafikausgabeobjekt
	 * @see #paint(Graphics)
	 */
	private void paintFrame(final Graphics2D g) {
		final Rectangle2D.Double rect=new Rectangle2D.Double(0,0,getWidth(),getHeight());

		g.setColor(isDark?Color.DARK_GRAY:SystemColor.control);
		g.fill(rect);
		/* kein Rahmen - g.setColor(Color.GRAY); g.draw(rect); */
	}

	/**
	 * Zeichnet die Box für den eigentlichen Diagrammbereich
	 * @param g	Grafikausgabeobjekt
	 * @see #paint(Graphics)
	 */
	private void paintBox(final Graphics2D g) {
		final Rectangle2D.Double rect=new Rectangle2D.Double(marginBoxLeft,marginBoxTop,boxWidth,boxHeight);

		g.setColor(isDark?Color.GRAY:Color.WHITE);
		g.fill(rect);
		g.setColor(Color.BLACK);
		g.draw(rect);
	}

	/**
	 * Gibt den Titel der Darstellung aus
	 * @param g	Grafikausgabeobjekt
	 * @param title	Anzuzeigender Titel
	 * @see #paint(Graphics)
	 */
	private void paintTitle(final Graphics2D g, final String title) {
		g.setFont(fontBold);
		g.setColor(isDark?Color.LIGHT_GRAY:Color.BLACK);

		g.drawString(title,marginBoxLeft+(boxWidth-g.getFontMetrics().stringWidth(title))/2,fontHeight/2+fontYDelta);
	}

	/**
	 * Zeichnet die Achsen des Diagramms
	 * @param g	Grafikausgabeobjekt
	 * @see #paint(Graphics)
	 */
	private void paintAxis(final Graphics2D g) {
		final int displaySlots=getDisplaySlots();

		g.setFont(fontDefault);
		g.setColor(isDark?Color.LIGHT_GRAY:Color.BLACK);

		/* "Anzahl" über y-Achse */
		g.drawString(Language.tr("Schedule.Number"),fontHeight,fontHeight/2+fontYDelta);

		/* y-Achse */
		List<Integer> steps=new ArrayList<>();
		int step=0;
		while (step<=editorMaxY) {
			steps.add(step);
			step+=Math.max(1,editorMaxY/8);
		}
		if (steps.get(steps.size()-1)<editorMaxY) steps.add(editorMaxY);
		for (Integer value: steps) {
			int y=marginBoxTop+(editorMaxY-value)*boxHeight/editorMaxY;
			g.drawLine(marginBoxLeft-fontHeight/3,y,marginBoxLeft,y);
			final String text=""+value;
			g.drawString(text,marginBoxLeft-fontHeight/2-g.getFontMetrics().stringWidth(text),y-fontHeight/2+fontYDelta);
		}

		/* x-Achse */
		for (int i=0;i<XSTEPS;i++) {
			int nr=displaySlots*i/XSTEPS;
			int x=marginBoxLeft+boxWidth*nr/displaySlots;
			g.drawLine(x,marginBoxTop+boxHeight,x,marginBoxTop+boxHeight+fontHeight/3);
			final String text=getSlotInfo(nr);
			g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,marginBoxTop+boxHeight+fontHeight/2+fontYDelta);
		}
	}

	/**
	 * Zeichnet die Datenbalken.
	 * @param g	Grafikausgabeobjekt
	 * @see #paint(Graphics)
	 */
	private void paintData(final Graphics2D g) {
		final int displaySlots=getDisplaySlots();

		for (int i=0;i<displaySlots;i++) {
			final int value=getDataValue(startPosition+i);

			final int x=1+marginBoxLeft+i*boxWidth/displaySlots;
			final int y=marginBoxTop+boxHeight-1;
			final int width=(i+1)*boxWidth/displaySlots-i*boxWidth/displaySlots-2;
			final int height=Math.min(boxHeight-2,(boxHeight-2)*value/editorMaxY);

			boolean markThisBox=(lastMousePoint!=null && lastMousePoint.y>=marginBoxTop && lastMousePoint.y<=marginBoxBottom+boxHeight && lastMousePoint.x>=x && lastMousePoint.x<=x+width);

			final Rectangle2D.Double rectangle=new Rectangle2D.Double(x,y-height,width,height);

			g.setColor(markThisBox?(new Color(0,0,255,64)):(new Color(255,0,0,64)));
			g.fill(rectangle);
			g.setColor(markThisBox?Color.BLUE:Color.RED);
			g.draw(rectangle);

			g.setFont(fontDefault);
			final String text=""+value;
			final int xText=x+(width-g.getFontMetrics().stringWidth(text))/2;
			if (height>=fontHeight*3/2) {
				g.drawString(text,xText,y-height+fontHeight/4+fontYDelta);
			} else {
				g.drawString(text,xText,y-height-fontHeight-fontHeight/4+fontYDelta);
			}

			if (markThisBox) {
				g.setColor(isDark?Color.WHITE:Color.BLUE);
				g.setFont(fontBold);
				String mark=getSlotInfo(i)+" - "+getSlotInfo(i+1);
				int xMark=x+(width-g.getFontMetrics().stringWidth(mark))/2;
				xMark=Math.max(0,xMark);
				xMark=Math.min(getWidth()-g.getFontMetrics().stringWidth(mark),xMark);
				g.drawString(mark,xMark,getHeight()-3*fontHeight/2+fontYDelta);
			}

		}
	}

	/**
	 * Liefert den Wert für einen Balken
	 * @param nr	Index des Balkens bzw. des Zeitslots
	 * @return	Wert für den Balken
	 * @see #setDataValue(int, int)
	 */
	private int getDataValue(final int nr) {
		return (nr>=data.size())?0:data.get(nr);
	}

	/**
	 * Stellt den Wert für einen Balken ein
	 * @param nr	Index des Balkens bzw. des Zeitslots
	 * @param value	Neuer Wert für den Balken
	 * @see #getDataValue(int)
	 */
	private void setDataValue(final int nr, final int value) {
		while (data.size()<=nr) data.add(0);
		data.set(nr,value);
	}

	/**
	 * Liefert die Beschriftung (unter der x-Achse) für einen Balken
	 * @param nr	Index des Balkens bzw. des Zeitslots
	 * @return	Beschriftung für den Balken
	 */
	private String getSlotInfo(int nr) {
		final int slot=startPosition+nr;
		final int time=slot*durationPerSlot;
		final int timeMin=time/60;
		if (timeMin<60*24) {
			return String.format("%02d:%02d",timeMin/60,timeMin%60);
		} else {
			return String.format("%d:%02d:%02d",timeMin/1440,timeMin/60%24,timeMin%60);
		}
	}

	@Override
	public void paint(final Graphics g) {
		final int displaySlots=getDisplaySlots();

		g.setFont(fontDefault);
		fontYDelta=g.getFontMetrics().getAscent();
		fontHeight=g.getFontMetrics().getAscent()+g.getFontMetrics().getDescent();

		marginBoxLeft=fontHeight+g.getFontMetrics().stringWidth(""+editorMaxY)+fontHeight/2;
		final int marginBoxRight=fontHeight;
		marginBoxTop=2*fontHeight;
		marginBoxBottom=3*fontHeight;

		boxWidth=getWidth()-marginBoxLeft-marginBoxRight;
		boxHeight=getHeight()-marginBoxTop-marginBoxBottom;

		final StringBuilder title=new StringBuilder();
		title.append(Language.tr("Schedule.Range")+": ");
		title.append(getSlotInfo(0));
		title.append(" - ");
		title.append(getSlotInfo(displaySlots));
		title.append(", "+Language.tr("Schedule.RangePerSection")+": ");
		int time=durationPerSlot;
		if (time>=86400) {
			int days=time/86400;
			title.append(days);
			title.append(" ");
			if (days==1) title.append(Language.tr("Schedule.Day.Singular")); else title.append(Language.tr("Schedule.Day.Plural"));
			time-=days*86400;
			if (time>0) title.append(" "+Language.tr("Schedule.And")+" ");
		}
		if (time>0) title.append(String.format("%d:%02d",time/3600,time/60%60));

		paintFrame((Graphics2D)g);
		paintBox((Graphics2D)g);
		paintTitle((Graphics2D)g,title.toString());
		paintAxis((Graphics2D)g);
		paintData((Graphics2D)g);
	}

	/**
	 * Bestimmt basierend auf einer Mausposition den Index
	 * eines Zeitslots und den y-Wert der Mausposition.
	 * @param xMouse	Horizontale Mausposition
	 * @param yMouse	Vertikale Mausposition
	 * @return	Array aus Index des Zeitslots und neuem Wert für diesen Slot
	 */
	private int[] getSlotDataFromPosition(final int xMouse, final int yMouse) {
		final int displaySlots=getDisplaySlots();

		for (int i=0;i<displaySlots;i++) {
			int x=1+marginBoxLeft+i*boxWidth/displaySlots;
			int width=boxWidth/displaySlots-2;

			if (xMouse<x || xMouse>x+width) continue;

			if (yMouse<=marginBoxTop) return null;
			if (yMouse>=marginBoxTop+boxHeight) return null;

			final double value=1-(double)(yMouse-marginBoxTop)/boxHeight;

			return new int[]{startPosition+i,(int)Math.round(value*editorMaxY)};
		}

		return null;
	}

	/**
	 * Reagiert auf Mausbewegungen und Mausklicks.
	 */
	private class SchedulePanelMouseListener extends MouseAdapter implements MouseMotionListener {
		@Override
		public void mousePressed(MouseEvent e) {
			final int[] data=getSlotDataFromPosition(e.getX(),e.getY());
			if (data!=null) setDataValue(data[0],data[1]);
			repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			lastMousePoint=null;
			repaint();
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastMousePoint=new Point(e.getX(),e.getY());
			repaint();
		}
	}
}