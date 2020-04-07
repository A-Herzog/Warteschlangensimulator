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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import language.Language;

/**
 * Stellt innerhalb eines {@link ScheduleTableModelDataDialog} Dialogs
 * einen Zeitplan dar.
 * @author Alexander Herzog
 * @see ScheduleTableModelDataDialog
 */
public class SchedulePanel extends JPanel {
	private static final long serialVersionUID = 6915816603375099302L;

	private static final int XSTEPS=10;

	private final List<Integer> data;
	private int startPosition;
	private int editorMaxY;
	private int durationPerSlot;
	private final int displaySlots;

	private final Font fontDefault;
	private final Font fontBold;

	private int fontYDelta;
	private int fontHeight;
	private int marginBoxLeft;
	private int marginBoxRight;
	private int marginBoxTop;
	private int marginBoxBottom;
	private int boxWidth;
	private int boxHeight;

	private Point lastMousePoint;

	/**
	 * Konstruktor der Klasse
	 * @param data	Werte in den einzelnen Zeitslots
	 * @param editorMaxY	Als Maximum anzuzeigender y-Wert
	 * @param durationPerSlot	Zeitspanne pro Zeitslot (für die Beschriftung unter den Slots)
	 * @param displaySlots	Anzahl an gleichzeitig darzustellenden Slots
	 */
	public SchedulePanel(final List<Integer> data, final int editorMaxY, final int durationPerSlot, final int displaySlots) {
		super();
		this.data=data;
		startPosition=0;
		this.editorMaxY=10;
		this.durationPerSlot=3600;
		this.displaySlots=displaySlots;
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

	private void paintFrame(final Graphics2D g) {
		final Rectangle2D.Double rect=new Rectangle2D.Double(0,0,getWidth(),getHeight());

		g.setColor(SystemColor.control);
		g.fill(rect);
		/* kein Rahmen - g.setColor(Color.GRAY); g.draw(rect); */
	}

	private void paintBox(final Graphics2D g) {
		final Rectangle2D.Double rect=new Rectangle2D.Double(marginBoxLeft,marginBoxTop,boxWidth,boxHeight);

		g.setColor(Color.WHITE);
		g.fill(rect);
		g.setColor(Color.BLACK);
		g.draw(rect);
	}

	private void paintTitle(final Graphics2D g, final String title) {
		g.setFont(fontBold);
		g.setColor(Color.BLACK);

		g.drawString(title,marginBoxLeft+(boxWidth-g.getFontMetrics().stringWidth(title))/2,fontHeight/2+fontYDelta);
	}

	private void paintAxis(final Graphics2D g) {
		g.setFont(fontDefault);
		g.setColor(Color.BLACK);

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

	private void paintData(final Graphics2D g) {
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
				g.setFont(fontBold);
				String mark=getSlotInfo(i)+" - "+getSlotInfo(i+1);
				int xMark=x+(width-g.getFontMetrics().stringWidth(mark))/2;
				xMark=Math.max(0,xMark);
				xMark=Math.min(getWidth()-g.getFontMetrics().stringWidth(mark),xMark);
				g.drawString(mark,xMark,getHeight()-3*fontHeight/2+fontYDelta);
			}

		}
	}

	private int getDataValue(final int nr) {
		return (nr>=data.size())?0:data.get(nr);
	}

	private void setDataValue(final int nr, final int value) {
		while (data.size()<=nr) data.add(0);
		data.set(nr,value);
	}

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
		g.setFont(fontDefault);
		fontYDelta=g.getFontMetrics().getAscent();
		fontHeight=g.getFontMetrics().getAscent()+g.getFontMetrics().getDescent();

		marginBoxLeft=fontHeight+g.getFontMetrics().stringWidth(""+editorMaxY)+fontHeight/2;
		marginBoxRight=fontHeight;
		marginBoxTop=2*fontHeight;
		marginBoxBottom=3*fontHeight;

		boxWidth=getWidth()-marginBoxLeft-marginBoxRight;
		boxHeight=getHeight()-marginBoxTop-marginBoxBottom;

		final StringBuilder sb=new StringBuilder();
		sb.append(Language.tr("Schedule.Range")+": ");
		sb.append(getSlotInfo(0));
		sb.append(" - ");
		sb.append(getSlotInfo(displaySlots-1));
		sb.append(", "+Language.tr("Schedule.RangePerSection")+": ");
		int time=durationPerSlot;
		if (time>=86400) {
			int days=time/86400;
			sb.append(days);
			sb.append(" ");
			if (days==1) sb.append(Language.tr("Schedule.Day.Singular")); else sb.append(Language.tr("Schedule.Day.Plural"));
			time-=days*86400;
			if (time>0) sb.append(" "+Language.tr("Schedule.And")+" ");
		}
		if (time>0) sb.append(String.format("%d:%02d",time/3600,time/60%60));

		paintFrame((Graphics2D)g);
		paintBox((Graphics2D)g);
		paintTitle((Graphics2D)g,sb.toString());
		paintAxis((Graphics2D)g);
		paintData((Graphics2D)g);
	}

	private int[] getSlotDataFromPosition(final int xMouse, final int yMouse) {
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
		public void mouseDragged(MouseEvent e) {}

		@Override
		public void mouseMoved(MouseEvent e) {
			lastMousePoint=new Point(e.getX(),e.getY());
			repaint();
		}
	}
}