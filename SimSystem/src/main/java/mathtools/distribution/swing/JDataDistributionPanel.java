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
package mathtools.distribution.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.math3.distribution.AbstractRealDistribution;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;

/**
 * Zeigt den grafischen Verlauf von Daten
 * vom Typ <code>DataDistributionImpl</code> an.
 * @author Alexander Herzog
 * @version 1.1
 * @see DataDistributionImpl
 */
public class JDataDistributionPanel extends JPanel implements JGetImage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8335989669214631310L;

	/**
	 * Fehlermeldung "Keine Verteilung angegeben"
	 */
	public static String errorString="Keine Verteilung angegeben";

	/** Obere Grenze f�r den Tr�ger der Dichte */
	private static int maxXValue=48;

	/** Funktionsplotter innerhalb des Panels */
	private JDataDistributionPlotter plotter;

	/** Darzustellende Verteilung */
	private DataDistributionImpl distribution=null;

	/** Zahlenformat f�r Beschriftung der y-Achse */
	private LabelMode labelFormat=LabelMode.LABEL_VALUE;

	/** Informationstext zur aktuellen Verteilung */
	private JLabel info;

	/**
	 * Zahlenformat f�r Beschriftung der y-Achse
	 * @author Alexander Herzog
	 */
	public enum LabelMode {
		/** Ausgabe der Werte der Verteilung */
		LABEL_VALUE,

		/** Ausgabe der Werte der Verteilung als Prozentwerte */
		LABEL_PERCENT
	}

	/**
	 * Konstruktor der Klasse <code>JDataDistributionPanel</code>
	 * @param distribution Zu ladende Verteilung (vom Typ <code>AbstractContinuousDistribution</code>)
	 * @param labelFormat	Kann LABEL_VALUE oder LABEL_PERCENT lauten
	 */
	public JDataDistributionPanel(DataDistributionImpl distribution, LabelMode labelFormat) {
		this.distribution=distribution;
		this.labelFormat=labelFormat;

		setLayout(new BorderLayout(0,0));

		JPanel infoPanel=new JPanel(new BorderLayout()); add(infoPanel,BorderLayout.NORTH);

		infoPanel.add(info=new JLabel(),BorderLayout.CENTER);

		add(plotter=new JDataDistributionPlotter(),BorderLayout.CENTER);
	}

	/**
	 * Auslesen der momentan angezeigten Verteilung
	 * @return Aktuell geladene Verteilung
	 * @see #setDistribution(DataDistributionImpl)
	 */
	public DataDistributionImpl getDistribution() {return distribution;}

	/**
	 * Setzen einer neuen Verteilung
	 * @param distribution Zu ladende Verteilung (vom Typ <code>DataDistributionImpl</code>)
	 * @see #getDistribution()
	 */
	public void setDistribution(DataDistributionImpl distribution) {
		this.distribution=distribution;
		String s=DistributionTools.getDistributionName(distribution);
		String t=DistributionTools.getDistributionInfo(distribution); if (!t.isEmpty()) s=s+" ("+t+")";
		if (!info.getText().equals(s)) info.setText(s);

		repaint(); plotter.repaint();
	}

	@Override
	public String toString() {
		return DistributionTools.distributionToString(distribution);
	}

	/**
	 * L�dt die anzuzeigende Verteilung aus einer Zeichenkette
	 * @param data Zeichenkette, die die Verteilungsdaten enth�lt (mit <code>toString</code> zu erzeugen)
	 * @return Liefert <code>true</code> zur�ck, wenn die Verteilung erfolgreich geladen werden konnte
	 * @see #toString()
	 */
	public boolean fromString(String data) {
		AbstractRealDistribution d=DistributionTools.distributionFromString(data,maxXValue);
		if (d instanceof DataDistributionImpl) setDistribution((DataDistributionImpl)d);
		return (d!=null);
	}

	@Override
	public void paintToGraphics(Graphics g) {
		plotter.paintToGraphics(g);
	}

	/**
	 * Eigentlicher Funktionsplotter innerhalb des Gesamt-Panels
	 */
	private class JDataDistributionPlotter extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 4036913455488209499L;

		/**
		 * Konstruktor der Klasse
		 */
		public JDataDistributionPlotter() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Berechnet die tats�chlich verf�gbare Zeichenfl�che
		 * @return	Verf�gbare Zeichenfl�che
		 */
		private Rectangle getClientRect() {
			Rectangle r=getBounds();
			Insets i=getInsets();
			return new Rectangle(i.left,i.top,r.width-i.left-i.right-1,r.height-i.top-i.bottom-1);
		}

		/**
		 * Wird aufgerufen, wenn keine Verteilung angegeben ist.
		 * Es wird dann eine entsprechende Fehlermeldung ausgegeben.
		 * @param g	Ausgabe Ziel
		 * @param r	Bereich f�r die Ausgabe
		 * @see #paintToRectangle(Graphics, Rectangle, boolean)
		 */
		private void paintNullDistribution(Graphics g, Rectangle r) {
			g.setColor(Color.RED);
			g.drawRect(r.x,r.y,r.width,r.height);
			int w=g.getFontMetrics().stringWidth(errorString);

			g.drawString(errorString,r.x+r.width/2-w/2,r.y+r.height/2+g.getFontMetrics().getAscent()/2);
		}

		/**
		 * Zeichnet den Rahmen f�r die Darstellung der Verteilungsdaten
		 * @param g	Ausgabe Ziel
		 * @param r	Ausgbebereich
		 * @param dataRect	Innerer Bereich
		 * @see #paintToRectangle(Graphics, Rectangle, boolean)
		 */
		private void paintDistributionRect(Graphics g, Rectangle r, Rectangle dataRect) {
			g.setColor(Color.WHITE);
			g.fillRect(dataRect.x,dataRect.y,dataRect.width,dataRect.height);

			g.setColor(Color.BLACK);
			g.drawLine(dataRect.x,dataRect.y,dataRect.x,dataRect.y+dataRect.height);
			g.drawLine(dataRect.x,dataRect.y+dataRect.height,dataRect.x+dataRect.width,dataRect.y+dataRect.height);

			int barWidth=dataRect.width/maxXValue-2;
			for (int i=0;i<distribution.densityData.length;i++) {
				if (i%2!=0) continue;
				String s=NumberTools.formatNumber(i/2.0);
				g.drawString(s,dataRect.x+i*(barWidth+2)+1+barWidth/2-g.getFontMetrics().stringWidth(s)/2,r.y+r.height);
			}
		}

		/**
		 * Zeichnet die Verteilungsdaten
		 * @param g	Ausgabe Ziel
		 * @param dataRect	Zeichenbereich innerhalb des Ziels
		 * @param allLabels	Sollen alle Beschriftungen (<code>true</code>) oder nur die vom Platz her sicher passendes (<code>false</code>) angezeigt werden
		 * @see #paintToRectangle(Graphics, Rectangle, boolean)
		 */
		private void paintDistribution(Graphics g, Rectangle dataRect, boolean allLabels) {
			int barWidth=dataRect.width/maxXValue-2;
			double maxValue=0;
			for (int i=0;i<distribution.densityData.length;i++) maxValue=Math.max(maxValue,distribution.densityData[i]);
			if (maxValue==0) maxValue=1;

			for (int i=0;i<distribution.densityData.length;i++) {
				Rectangle r=new Rectangle(
						dataRect.x+i*(barWidth+2)+1,
						dataRect.y+dataRect.height,
						barWidth,
						-(int)Math.round(dataRect.height*distribution.densityData[i]/maxValue)
						);

				if (r.height<0) {r.y=r.y+r.height; r.height=-r.height;}

				g.setColor(Color.BLUE);
				g.fillRect(r.x,r.y,r.width,r.height);
				g.setColor(Color.BLACK);
				g.drawRect(r.x,r.y,r.width,r.height);

				if (i%2==0 || barWidth>3*g.getFontMetrics().stringWidth("M") || allLabels) {
					g.setColor(Color.red);
					String s;
					switch (labelFormat) {
					case LABEL_VALUE: s=NumberTools.formatNumber(distribution.densityData[i]); break;
					case LABEL_PERCENT: s=NumberTools.formatNumber(distribution.densityData[i]*100,0)+"%"; break;
					default: s="";
					}
					int w=g.getFontMetrics().stringWidth(s);
					g.drawString(s,r.x+r.width/2-w/2,Math.max(dataRect.y+g.getFontMetrics().getHeight(),r.y-3));
				}
			}
		}

		/**
		 * Zeichnet die Verteilung in ein beliebiges {@link Graphics}-Objekt
		 * @param g	Ausgabe Ziel
		 * @param r	Ausgbebereich
		 * @param allLabels	Sollen alle Beschriftungen (<code>true</code>) oder nur die vom Platz her sicher passendes (<code>false</code>) angezeigt werden
		 * @see #paint(Graphics)
		 * @see #paintToGraphics(Graphics)
		 */
		private void paintToRectangle(Graphics g, Rectangle r, boolean allLabels) {
			if (distribution==null) {paintNullDistribution(g,r); return;}

			g.setColor(Color.WHITE);
			g.fillRect(r.x,r.y,r.x+r.width,r.y+r.height);

			Dimension space=new Dimension(g.getFontMetrics().stringWidth("0"),g.getFontMetrics().getHeight());
			Rectangle dataRect=new Rectangle(r.x+space.width+1,r.y+1,r.width-space.width-2,r.height-space.height-2);

			paintDistributionRect(g,r,dataRect);
			paintDistribution(g,dataRect,allLabels);
		}

		@Override
		public void paint(Graphics g) {
			paintToRectangle(g,getClientRect(),false);
		}

		/**
		 * Zeichnet die Verteilung in ein beliebiges {@link Graphics}-Objekt
		 * (z.B. zum Speichern der Verteilungsansicht als Bild).
		 * Im Gegensatz zu der normalen Ausgabe werden dabei immer alle
		 * Balkenbeschriftungen ausgegeben.
		 * @param g	Ausgabe Ziel
		 * @see JDataDistributionPanel#paintToGraphics(Graphics)
		 */
		private void paintToGraphics(Graphics g) {
			paintToRectangle(g,g.getClipBounds(),true);
		}
	}
}
