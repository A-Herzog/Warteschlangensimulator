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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.MathRuntimeException;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import swingtools.ImageIOFormatCheck;

/**
 * Zeigt den grafischen Verlauf von Dichte und Verteilungsfunktion einer Verteilung
 * vom Typ <code>AbstractContinuousDistribution</code> an.
 * @author Alexander Herzog
 * @version 1.9
 * @see AbstractRealDistribution
 */
public class JDistributionPanel extends JPanel implements JGetImage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5152496149421849230L;

	/** Bezeichner f�r Fehlermeldung "Keine Verteilung angegeben" */
	public static String ErrorString="Keine Verteilung angegeben";
	/** Bezeichner f�r Dialogschaltfl�che "Bearbeiten" */
	public static String EditButtonLabel="Bearbeiten";
	/** Bezeichner f�r Tooltip f�r Dialogschaltfl�che "Bearbeiten" */
	public static String EditButtonTooltip="�ffnet den Dialog zum Bearbeiten der Verteilung";
	/** Bezeichner f�r Dialogschaltfl�che "Bearbeiten" im Reand-Only-Modus */
	public static String EditButtonLabelDisabled="Daten anzeigen";
	/** Bezeichner f�r Tooltip f�r Dialogschaltfl�che "Kopieren" */
	public static String CopyButtonLabel="Kopieren";
	/** Bezeichner f�r Men�punkt "Wertetabelle kopieren" im Kopieren-Men� */
	public static String CopyButtonTable="Wertetabelle kopieren";
	/** Bezeichner f�r Men�punkt "Bild kopieren" im Kopieren-Men� */
	public static String CopyButtonImage="Bild kopieren";
	/** Bezeichner f�r Tooltip f�r Dialogschaltfl�che "Speichern" */
	/** Bezeichner f�r Dialogschaltfl�che "Speichern" */
	public static String SaveButtonLabel="Speichern";
	/** Bezeichner f�r Men�punkt "Wertetabelle speichern" im Speichern-Men� */
	public static String SaveButtonTable="Wertetabelle speichern";
	/** Bezeichner f�r Men�punkt "Bild kopieren" im Speichern-Men� */
	public static String SaveButtonImage="Bild speichern";
	/** Bezeichner f�r Dialogschaltfl�che "Hilfe" */
	public static String WikiButtonLabel="Hilfe";
	/** Bezeichner f�r Tooltip f�r Dialogschaltfl�che "Hilfe" */
	public static String WikiButtonTooltip="�ffnet ein Browserfenster mit weiteren Informationen zu dem gew�hlten Verteilungstyp";
	/** Untermen� zur Schnellauswahl des Verteilungstyps */
	public static String ChangeDistributionType="Verteilungstyp";
	/** Informationstext in der Verteilungsanzeige zur Ver�nderung des Verteilungstyps */
	public static String ChangeDistributionTypeHighlightList="Hier werden nur die hervorgehobenen Verteilungen dargestellt. Eine vollst�ndige Liste steht im Bearbeiten-Dialog zur Auswahl zur Verf�gung.";
	/** Bezeichner "Dichte" */
	public static String DensityLabel="Dichte";
	/** Bezeichner "Z�hldichte" */
	public static String CountDensityLabel="Z�hldichte";
	/** Bezeichner "Verteilung" */
	public static String CumulativeProbabilityLabel="Verteilung";
	/** Bezeichner f�r Dialogtitel "Grafik speichern" */
	public static String StoreGraphicsDialogTitle="Grafik speichern";
	/** Bezeichner f�r Dateiformat jpeg (im Dateiauswahldialog) */
	public static String FileTypeJPEG="jpeg-Dateien";
	/** Bezeichner f�r Dateiformat gif (im Dateiauswahldialog) */
	public static String FileTypeGIF="gif-Dateien";
	/** Bezeichner f�r Dateiformat png (im Dateiauswahldialog) */
	public static String FileTypePNG="png-Dateien";
	/** Bezeichner f�r Dateiformat bmp (im Dateiauswahldialog) */
	public static String FileTypeBMP="bmp-Dateien";
	/** Bezeichner f�r Dateiformat tiff (im Dateiauswahldialog) */
	public static String FileTypeTIFF="tiff-Dateien";
	/** �berschreibwarnung f�r Grafiken (Text der Meldung) */
	public static String GraphicsFileOverwriteWarning="Die Datei %s existiert bereits. Soll die Datei jetzt �berschrieben werden?";
	/** �berschreibwarnung f�r Grafiken (Titel der Meldung) */
	public static String GraphicsFileOverwriteWarningTitle="Warnung";
	/** "URL aufrufen" Warnung (Text der Meldung) */
	public static String GraphicsOpenURLWarning="M�chten Sie jetzt die externe Webseite\n%s\naufrufen?";
	/** "URL aufrufen" Warnung (Text der Meldung) */
	public static String GraphicsOpenURLWarningTitle="Warnung";

	/** Info-Text zu der Verteilung */
	private final JLabel info;

	/** "Kopieren"-Schaltfl�che */
	private final JButton copy;

	/** "Speichern"-Schaltfl�che */
	private final JButton save;

	/** "Bearbeiten"-Schaltfl�che */
	private final JButton edit;

	/** "Hilfe"-Schaltfl�che (ruft die Wikipedia auf) */
	private final JButton wiki;

	/** Funktionsplotter innerhalb des Panels */
	private final JDistributionPlotter plotter;

	/**
	 * Bildgr��e beim Kopieren und Speichern
	 * @see #setImageSaveSize(int)
	 */
	private int imageSize=1000;

	/** Maximal darzustellender x-Wert */
	private double maxXValue=1;

	/** Darstellungsmethode (Dichte, Verteilung oder beides) */
	private int plotType=BOTH;

	/** Darzustellende Verteilung */
	private AbstractRealDistribution distribution=null;

	/**
	 * Darf der Typ der Verteilung im Editor ge�ndert werden?
	 * @see #isAllowDistributionTypeChange()
	 * @see #setAllowDistributionTypeChange(boolean)
	 */
	private boolean allowDistributionTypeChange=true;

	/**
	 * D�rfen die Parameter der Verteilung im Editor ge�ndert werden?
	 * @see #isAllowChangeDistributionData()
	 * @see #setAllowChangeDistributionData(boolean)
	 */
	private boolean allowOk=true;

	/**
	 * Parameter f�r <code>setPlotType</code>, es wird nur die Dichte angezeigt.
	 * @see #setPlotType(int)
	 * @see #getPlotType()
	 */
	public static final int DENSITY=1;

	/**
	 * Parameter f�r <code>setPlotType</code>, es wird nur die Verteilungsfunktion angezeigt.
	 * @see #setPlotType(int)
	 * @see #getPlotType()
	 */
	public static final int CUMULATIVEPROBABILITY=2;

	/**
	 * Parameter f�r <code>setPlotType</code>, es werden Dichte und Verteilungsfunktion angezeigt.
	 * @see #setPlotType(int)
	 * @see #getPlotType()
	 */
	public static final int BOTH=3;

	/**
	 * Konstruktor der Klasse <code>DistributionPanel</code>
	 * @param distribution Zu ladende Verteilung (vom Typ {@link AbstractRealDistribution})
	 * @param maxXValue Maximal darzustellender x-Wert
	 * @param showEditButton Soll das "Bearbeiten"-Button angezeigt werden?
	 * @param plotType	W�hlt die Darstellungsmethode (Dichte, Verteilung oder beides)
	 */
	public JDistributionPanel(AbstractRealDistribution distribution, double maxXValue, boolean showEditButton, int plotType) {
		this.distribution=distribution;
		this.maxXValue=maxXValue;
		this.plotType=plotType;

		setLayout(new BorderLayout(0,0));

		JPanel infoPanel=new JPanel(new BorderLayout()); add(infoPanel,BorderLayout.NORTH);
		if (showEditButton) infoPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,0)); else infoPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		final JToolBar infoPanelEast;
		infoPanel.add(info=new JLabel(),BorderLayout.CENTER);
		infoPanel.add(infoPanelEast=new JToolBar(),BorderLayout.EAST);
		infoPanelEast.setFloatable(false);

		if (showEditButton) info.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) editButtonClicked();
			}
		});

		copy=new JButton(CopyButtonLabel);
		copy.addActionListener(e->actionCopy());
		copy.setIcon(SimSystemsSwingImages.COPY.getIcon());

		save=new JButton(SaveButtonLabel);
		save.addActionListener(e->actionSave());
		save.setIcon(SimSystemsSwingImages.SAVE.getIcon());

		try {
			getToolkit().getSystemClipboard();
			infoPanelEast.add(copy);
			infoPanelEast.add(save);
		} catch (SecurityException e) {}

		infoPanelEast.add(wiki=new JButton(WikiButtonLabel));
		wiki.setToolTipText(WikiButtonTooltip);
		wiki.addActionListener(e->actionWiki());
		wiki.setIcon(SimSystemsSwingImages.HELP.getIcon());

		if (showEditButton) {
			infoPanelEast.add(edit=new JButton(EditButtonLabel));
			edit.setToolTipText(EditButtonTooltip);
			edit.addActionListener(e->editButtonClicked());
			edit.setIcon(SimSystemsSwingImages.EDIT.getIcon());
		} else {
			edit=null;
		}

		add(plotter=new JDistributionPlotter(),BorderLayout.CENTER);

		if (showEditButton) new FileDropper(new Component[]{plotter,infoPanel},event->{
			final FileDropperData data=(FileDropperData)event.getSource();
			final double[] newData=JDataLoader.loadNumbersFromFile(JDistributionPanel.this,data.getFile(),1,Integer.MAX_VALUE);
			if (newData==null) return;
			data.dragDropConsumed();
			DataDistributionImpl d=new DataDistributionImpl(maxXValue,newData);
			setDistribution(d);
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2 && showEditButton) editButtonClicked();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e,showEditButton);
			}
		});
	}

	/**
	 * Konstruktor der Klasse <code>DistributionPanel</code>
	 * @param distribution Zu ladende Verteilung (vom Typ <code>AbstractContinuousDistribution</code>)
	 * @param maxXValue Maximal darzustellender x-Wert
	 * @param showEditButton Soll das "Bearbeiten"-Button angezeigt werden?
	 */
	public JDistributionPanel(AbstractRealDistribution distribution, double maxXValue, boolean showEditButton) {
		this(distribution,maxXValue,showEditButton,BOTH);
	}

	/**
	 * Auslesen der momentan angezeigten Verteilung
	 * @return Aktuell gelandene Verteilung
	 * @see #setDistribution(AbstractRealDistribution)
	 * @see #setDistribution(double)
	 */
	public AbstractRealDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Setzen einer neuen Verteilung
	 * @param distribution Zu ladende Verteilung (vom Typ <code>AbstractContinuousDistribution</code>)
	 * @see #getDistribution()
	 */
	public void setDistribution(AbstractRealDistribution distribution) {
		this.distribution=distribution;
		setInfoText();
		repaint();
		plotter.repaint();
	}

	/**
	 * Aktualisiert den Info-Text in {@link #info} zu der Verteilung.
	 * @return	Liefert <code>true</code>, wenn sich der Text gegen�ber dem bisher dargestellten Text ver�ndert hat
	 */
	private boolean setInfoText() {
		final String name=DistributionTools.getDistributionName(distribution);
		final String dataShort=DistributionTools.getDistributionInfo(distribution);
		final String infoShort="<html><b>"+name+"</b>"+((!dataShort.isEmpty())?("<br>"+dataShort):"")+"</html>";
		if (!info.getText().equals(infoShort)) {
			info.setText(infoShort);
			final String dataLong=DistributionTools.getDistributionLongInfo(distribution);
			if (dataLong.isEmpty()) {
				info.setToolTipText("");
			} else {
				info.setToolTipText("<html><body><b>"+name+"</b><br>"+dataLong.replace("; ","<br>")+"</body></html>");
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Laden einer Exponentialverteilung mit Mittelwert <code>mean</code>
	 * @param mean Mittelwert der Exponentialverteilung
	 */
	public void setDistribution(double mean) {
		setDistribution(new ExponentialDistribution(null,mean,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
	}

	/**
	 * Auslesen des aktuellen Darstellungstyps (nur Dichte, nur Verteilungsfunktion, beides)
	 * @return Aktueller Darstellungstyp
	 * @see #DENSITY
	 * @see #CUMULATIVEPROBABILITY
	 * @see #BOTH
	 */
	public int getPlotType() {return plotType;}

	/**
	 * Einstellen des Darstellungstyps (nur Dichte, nur Verteilungsfunktion, beides)
	 * @param plotType Neuer Darstellungstyp
	 * @see #DENSITY
	 * @see #CUMULATIVEPROBABILITY
	 * @see #BOTH
	 */
	public void setPlotType(int plotType) {
		if (plotType==this.plotType) return;
		this.plotType=plotType;
		repaint();
		plotter.repaint();
	}

	/**
	 * Auslesen des maximal darzustellenden x-Werts
	 * @return Maximaler x-Wert
	 */
	public double getMaxXValue() {
		return maxXValue;
	}

	/**
	 * Einstellen des maximal darzustellenden x-Werts
	 * @param maxXValue Neuer maximaler x-Wert
	 */
	public void setMaxXValue(double maxXValue) {
		if (this.maxXValue==maxXValue) return;
		this.maxXValue=maxXValue;
		repaint();
		plotter.repaint();
	}

	@Override
	public void paint(Graphics g) {
		if (setInfoText()) info.paint(g);
		super.paint(g);
	}

	@Override
	public void paintToGraphics(Graphics g) {
		plotter.paintToGraphics(g);
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
		if (d!=null) setDistribution(d);
		return (d!=null);
	}

	/**
	 * Gibt aus, ob im Editor der Verteilungstyp ge�ndert werden darf.
	 * @return	Gibt <code>true</code> zur�ck, wenn der Verteilungstyp im Editor ge�ndert werden darf.
	 */
	public boolean isAllowDistributionTypeChange() {
		return allowDistributionTypeChange;
	}

	/**
	 * Gibt aus, ob die Verteilung im Editor ge�ndert werden darf.
	 * @return	Gibt <code>true</code> zur�ck, wenn der die Verteilung im Editor ge�ndert werden darf.
	 */
	public boolean isAllowChangeDistributionData() {
		return allowOk;
	}

	/**
	 * Stellt ein, ob im Editor der Verteilungstyp ge�ndert werden darf (Vorgabeeinstellung) oder nicht.
	 * @param b	Wird dieser Wert auf <code>true</code> gestellt, darf der Verteilungstyp im Editor ge�ndert werden.
	 */
	public void setAllowDistributionTypeChange(boolean b) {
		allowDistributionTypeChange=b;
	}

	/**
	 * Gibt an, ob die Verteilung �ber den Verteilungseditor bearbeitet werden darf oder nicht.
	 * @param b Wird dieser Wert auf <code>false</code> gestellt, so wird die "Ok"-Schaltfl�che im Verteilungseditor deaktiviert.
	 */
	public void setAllowChangeDistributionData(boolean b) {
		allowOk=b;
		edit.setText(b?EditButtonLabel:EditButtonLabelDisabled);
		edit.setToolTipText(b?EditButtonTooltip:"");
	}

	/**
	 * Liefert den tats�chlich auftretenden maximalen x-Wert unter Ber�cksichtigung
	 * des Tr�gerbereichs der Verteilung.
	 * @return	Maximaler x-Wert (f�r die Anzeige der Verteilung)
	 * @see #maxXValue
	 */
	private double getRealMaxXValue() {
		if (distribution==null) return maxXValue;

		double newMaxXValue=Math.min(maxXValue,Math.min(10_000,distribution.getSupportUpperBound()));
		if (distribution.cumulativeProbability(newMaxXValue)>0.99) {
			while (newMaxXValue>10) {
				double testOld=newMaxXValue;
				newMaxXValue=Math.round(newMaxXValue/2);
				if (distribution.cumulativeProbability(newMaxXValue)<0.99) {newMaxXValue=testOld; break;}
			}
		} else {
			if (distribution.getSupportUpperBound()*1.1<newMaxXValue) newMaxXValue=distribution.getSupportUpperBound()*1.1;
		}

		/* Wenn der �bergang von F(x)<0.5 zu F(x)>0.99 sehr hart ist (und vermutlich die Ein-Punkt-Verteilung vorliegt), die obere x-Grenze etwas verschieben, so dass die (fast) vertikale Linie zu erkennen ist. */
		if (newMaxXValue*0.99<distribution.getSupportUpperBound() && distribution.cumulativeProbability(newMaxXValue*0.99)<0.5) {
			newMaxXValue=newMaxXValue*1.1;
		}

		return newMaxXValue;


	}

	/**
	 * Stellt die Bildgr��e beim Kopieren und Speichern ein.
	 * @param imageSize	Vertikale und horizontale Aufl�sung
	 */
	public void setImageSaveSize(int imageSize) {
		this.imageSize=imageSize;
	}

	/**
	 * Erzeugt eine Wertetabelle f�r die Verteilung und kopiert diese in die Zwischenablage.
	 */
	public void copyTableOfValues() {
		if (distribution==null) return;
		DistributionTools.copyTableOfValues(distribution);
	}

	/**
	 * Erzeugt und speichert eine Wertetabelle f�r die Verteilung.
	 */
	public void saveTableOfValues() {
		if (distribution==null) return;
		DistributionTools.saveTableOfValues(this,distribution);
	}

	/**
	 * Eigentlicher Funktionsplotter innerhalb des Gesamt-Panels
	 */
	private class JDistributionPlotter extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 8083886665643583864L;

		/**
		 * Erfolgt die Darstellung im Dark-Modus?
		 */
		public boolean isDark;

		/**
		 * Konstruktor der Klasse
		 */
		public JDistributionPlotter() {
			final Color textBackground=UIManager.getColor("TextField.background");
			isDark=(textBackground!=null && !textBackground.equals(Color.WHITE));
		}

		/**
		 * Berechnet die tats�chlich verf�gbare Zeichenfl�che
		 * @return	Verf�gbare Zeichenfl�che
		 */
		protected Rectangle getClientRect() {
			Rectangle r=getBounds();
			Insets i=getInsets();
			return new Rectangle(i.left,i.top,r.width-i.left-i.right-1,r.height-i.top-i.bottom-1);
		}

		/**
		 * Wird aufgerufen, wenn keine Verteilung angegeben ist.
		 * Es wird dann eine entsprechende Fehlermeldung ausgegeben.
		 * @param g	Ausgabe Ziel
		 * @param r	Bereich f�r die Ausgabe
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintNullDistribution(Graphics g, Rectangle r) {
			g.setColor(Color.red);
			g.drawRect(r.x,r.y,r.width,r.height);
			int w=g.getFontMetrics().stringWidth(ErrorString);

			g.drawString(ErrorString,r.x+r.width/2-w/2,r.y+r.height/2+g.getFontMetrics().getAscent()/2);
		}

		/**
		 * Zeichnet den Rahmen f�r die Darstellung der Verteilungsdaten
		 * @param g	Ausgabe Ziel
		 * @param r	Ausgbebereich
		 * @param dataRect	Innerer Bereich
		 * @param maxXValue	Maximal darzustellender x-Wert
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintDistributionRect(final Graphics g, final Rectangle r, final Rectangle dataRect, final double maxXValue) {
			int fontDelta=g.getFontMetrics().getAscent();

			/* Hintergrund */
			final Graphics2D g2d=(Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			final GradientPaint gp=new GradientPaint(0,0,isDark?Color.GRAY:new Color(235,235,255),0,dataRect.height,isDark?Color.DARK_GRAY:Color.WHITE);
			g2d.setPaint(gp);
			g2d.fillRect(dataRect.x,dataRect.y,dataRect.width,dataRect.height);

			/* Rahmenlinien links und unten (=Koordinatenachsen) */
			g.setColor(isDark?Color.LIGHT_GRAY:Color.BLACK);
			g.drawLine(dataRect.x,dataRect.y,dataRect.x,dataRect.y+dataRect.height);
			g.drawLine(dataRect.x,dataRect.y+dataRect.height,dataRect.x+dataRect.width,dataRect.y+dataRect.height);

			/* Rahmenlinien oben und rechts */
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(dataRect.x+dataRect.width,dataRect.y,dataRect.x+dataRect.width,dataRect.y+dataRect.height);
			g.drawLine(dataRect.x,dataRect.y,dataRect.x+dataRect.width,dataRect.y);

			g.setColor(isDark?Color.WHITE:Color.BLACK);
			if (!(distribution instanceof DataDistributionImpl)) g.drawString("1",r.x,r.y+fontDelta);
			g.drawString("0",r.x,r.y+r.height);

			String s=NumberTools.formatNumber(maxXValue);
			g.drawString(s,r.x+r.width-g.getFontMetrics().stringWidth(s),r.y+r.height);

			if ((plotType==DENSITY) || (plotType==BOTH)) {
				g.setColor(Color.red);
				g.drawString((distribution instanceof DataDistributionImpl)?CountDensityLabel:DensityLabel,dataRect.x+2+2,dataRect.y+fontDelta+2);
			}
			if ((plotType==CUMULATIVEPROBABILITY) || (plotType==BOTH)) {
				g.setColor(Color.blue);
				g.drawString(CumulativeProbabilityLabel,dataRect.x+2+2,dataRect.y+2*fontDelta+2);
			}
		}

		/**
		 * Verteilungsfunktion einzeichnen
		 * @param g	Ausgabe Ziel
		 * @param dataRect	Zeichenbereich
		 * @param maxXValue	Maximal darzustellender x-Wert
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintCumulativeProbability(final Graphics g, final Rectangle dataRect, final double maxXValue) {
			double lastY=0,y=0;

			g.setColor(Color.blue);

			for (int i=dataRect.x;i<=dataRect.x+dataRect.width;i++) {
				double x=maxXValue*(i-dataRect.x)/(dataRect.width+1);
				y=distribution.cumulativeProbability(x);
				int y1=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*lastY);
				int y2=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*y);

				if (i>dataRect.x) g.drawLine(i-1,y1,i,y2);
				lastY=y;
			}
		}

		/**
		 * Dichte einzeichnen
		 * @param g	Ausgabe Ziel
		 * @param dataRect	Zeichenbereich
		 * @param maxXValue	Maximal darzustellender x-Wert
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintDensity(final Graphics g, final Rectangle dataRect, final double maxXValue) {
			double lastY=0,y=0;

			double maxY=0.001;
			for (int i=dataRect.x;i<=dataRect.x+dataRect.width;i++) {
				double x=maxXValue*(i-dataRect.x)/(dataRect.width+1);
				try {
					final double d=distribution.density(x);
					if (!Double.isInfinite(d) && !Double.isNaN(d)) maxY=Math.max(maxY,d);
				} catch (IllegalArgumentException | MathRuntimeException e) {}
			}

			g.setColor(new Color(1f,0f,0f,0.15f));
			for (int i=dataRect.x;i<=dataRect.x+dataRect.width;i++) {
				double x=maxXValue*(i-dataRect.x)/(dataRect.width+1);
				try {y=distribution.density(x)/maxY;} catch (IllegalArgumentException | MathRuntimeException e) {}
				int y1=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*lastY);
				int y2=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*y);

				if (i>dataRect.x) {
					/* g.drawLine(i-1,y1,i,y2); */
					final int base=dataRect.y+dataRect.height-1;
					g.fillPolygon(new int[]{i-1,i-1,i,i},new int[]{base,y1,y2,base}, 4);
				}
				lastY=y;
			}

			g.setColor(Color.red);
			for (int i=dataRect.x;i<=dataRect.x+dataRect.width;i++) {
				double x=maxXValue*(i-dataRect.x)/(dataRect.width+1);
				try {y=distribution.density(x)/maxY;} catch (IllegalArgumentException | MathRuntimeException e) {}
				int y1=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*lastY);
				int y2=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*y);

				if (i>dataRect.x) g.drawLine(i-1,y1,i,y2);
				lastY=y;
			}
		}

		/**
		 * Zeichnet die Verteilung in ein beliebiges {@link Graphics}-Objekt
		 * @param g	Ausgabe Ziel
		 * @param r	Ausgbebereich
		 * @param maxXValue	Maximal darzustellender x-Wert
		 * @see #paint(Graphics)
		 * @see #paintToGraphics(Graphics)
		 */
		private void paintToRectangle(final Graphics g, Rectangle r, final double maxXValue) {
			if (distribution==null) {paintNullDistribution(g,r); return;}

			/* Hintergrund �ber alles */
			g.setColor(getBackground());
			g.fillRect(r.x,r.y,r.x+r.width,r.y+r.height);

			/* Rahmen um alles - aus */
			/*
			g.setColor(Color.GRAY);
			g.drawRect(r.x,r.y,r.x+r.width,r.y+r.height);
			 */

			/* Rechteck innerhalb der Achsen definieren */
			Dimension space=new Dimension(g.getFontMetrics().stringWidth("0"),g.getFontMetrics().getHeight());
			final int padding=3;
			r=new Rectangle(r.x+padding,r.y+padding,r.width-2*padding,r.height-2*padding);
			Rectangle dataRect=new Rectangle(r.x+space.width+1,r.y+1,r.width-space.width-2,r.height-space.height-2);

			paintDistributionRect(g,r,dataRect,maxXValue);
			if ((plotType==CUMULATIVEPROBABILITY) || (plotType==BOTH))  paintCumulativeProbability(g,dataRect,maxXValue);
			if ((plotType==DENSITY) || (plotType==BOTH)) paintDensity(g,dataRect,maxXValue);
		}

		@Override
		public void paint(Graphics g) {
			paintToRectangle(g,getClientRect(),getRealMaxXValue());
		}

		/**
		 * Zeichnet die Verteilung in ein beliebiges {@link Graphics}-Objekt
		 * (z.B. zum Speichern der Verteilungsansicht als Bild).
		 * @param g	Ausgabe Ziel
		 */
		public void paintToGraphics(Graphics g) {
			paintToRectangle(g,g.getClipBounds(),getRealMaxXValue());
		}
	}

	/**
	 * Speichert die Darstellung in einer Datei.
	 * @param file	Dateiname
	 * @param format	Dateiformat ({@link ImageIO#write(java.awt.image.RenderedImage, String, File)})
	 * @param imageSize	Bildgr��e
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean saveImageToFile(File file, String format, int imageSize) {
		BufferedImage image=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_RGB);
		Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		Dimension d=plotter.getSize();
		plotter.setSize(imageSize,imageSize);
		final boolean dark=plotter.isDark;
		plotter.isDark=false;
		plotter.paint(g);
		plotter.isDark=dark;
		plotter.setSize(d);

		try {ImageIO.write(image,format,file);} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Zwischenablagen-Datenobjekt f�r ein Bild
	 * @see JDistributionPanel#copyImageToClipboard(Clipboard, int)
	 */
	private static class TransferableImage implements Transferable{
		/**
		 * Auszugebendes Bild
		 */
		private final Image theImage;

		/**
		 * Konstruktor der Klasse
		 * @param image	Auszugebendes Bild
		 */
		public TransferableImage(Image image) {
			theImage=image;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[]{DataFlavor.imageFlavor};}
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor){return flavor.equals(DataFlavor.imageFlavor);}
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{if (flavor.equals(DataFlavor.imageFlavor)) return theImage; else throw new UnsupportedFlavorException(flavor);}
	}

	/**
	 * Kopiert die Verteilungsdarstellung in die Zwischenablage
	 * @param clipboard	System-Zwischenablage
	 * @param imageSize	Bildgr��e
	 * @see TransferableImage
	 */
	private void copyImageToClipboard(Clipboard clipboard, int imageSize) {
		final Image image=createImage(imageSize,imageSize);
		final Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		final Dimension d=plotter.getSize();
		plotter.setSize(imageSize,imageSize);
		final boolean dark=plotter.isDark;
		plotter.isDark=false;
		plotter.paint(g);
		plotter.isDark=dark;
		plotter.setSize(d);

		/* see: https://bugs.openjdk.java.net/browse/JDK-8204188 */
		final BufferedImage image2=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2=image2.createGraphics();
		g2.setBackground(Color.WHITE);
		g2.clearRect(0,0,imageSize,imageSize);
		g2.drawImage(image,0,0,null);
		g2.dispose();

		clipboard.setContents(new TransferableImage(image2),null);
	}

	/**
	 * Erm�glicht die Auswahl eines Dateinamens zum Speichern der Verteilung als Bild
	 * @return	Gew�hlte Datei oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 * @see #save
	 */
	private File getSaveFileName() {
		JFileChooser fc;
		fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StoreGraphicsDialogTitle);

		final FileFilter jpg=new FileNameExtensionFilter(FileTypeJPEG+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(FileTypeGIF+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(FileTypePNG+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(FileTypeBMP+" (*.bmp)","bmp");
		final FileFilter tiff=new FileNameExtensionFilter(FileTypeTIFF+" (*.tiff, *.tif)","tiff","tif");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		if (ImageIOFormatCheck.hasTIFF()) fc.addChoosableFileFilter(tiff);
		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==tiff) file=new File(file.getAbsoluteFile()+".tiff");
		}
		return file;
	}

	/**
	 * Wird aufgerufen, wenn auf die "Bearbeiten"-Schaltfl�che geklickt wurde,
	 * um die Verteilung zu bearbeiten.
	 * @return	Gibt an, ob �nderungen an der Verteilung vorgenommen wurden.
	 */
	protected boolean editButtonClicked() {
		Container c=getParent();
		while (!(c instanceof Window)) {c=c.getParent(); if (c==null) return false;}
		JDistributionEditorDialog dialog=new JDistributionEditorDialog((Window)c,getDistribution(),maxXValue,plotType,allowDistributionTypeChange,allowOk,imageSize);
		dialog.setVisible(true);
		AbstractRealDistribution d=dialog.getNewDistribution();
		if (d!=null) {
			boolean result=!DistributionTools.compare(getDistribution(),d);
			setDistribution(d);
			return result;
		}
		return false;
	}

	/**
	 * Kopieren-Aktion ausl�sen
	 * @see #copy
	 */
	private void actionCopy() {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(CopyButtonTable,SimSystemsSwingImages.COPY_AS_TABLE.getIcon()));
		item.addActionListener(e->copyTableOfValues());

		menu.add(item=new JMenuItem(CopyButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(e->copyImageToClipboard(getToolkit().getSystemClipboard(),imageSize));

		menu.show(copy,0,copy.getHeight());
	}

	/**
	 * Speichert die aktuelle Verteilung als Bild.
	 */
	private void saveImage() {
		final File file=getSaveFileName();
		if (file==null) return;
		if (file.exists()) {
			if (JOptionPane.showConfirmDialog(JDistributionPanel.this,String.format(GraphicsFileOverwriteWarning,file.toString()),GraphicsFileOverwriteWarningTitle,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		}

		String extension="";
		int i=file.toString().lastIndexOf('.');
		if (i>0) extension=file.toString().substring(i+1);

		saveImageToFile(file,extension,imageSize);
	}

	/**
	 * Speichern-Aktion ausl�sen
	 * @see #save
	 */
	private void actionSave() {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(SaveButtonTable,SimSystemsSwingImages.COPY_AS_TABLE.getIcon()));
		item.addActionListener(e->copyTableOfValues());

		menu.add(item=new JMenuItem(SaveButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(e->saveImage());

		menu.show(save,0,save.getHeight());
	}

	/**
	 * Wikipedia-Seite zu der gew�hlten Verteilung �ffnen
	 * @see #wiki
	 */
	private void actionWiki() {
		final URI link=DistributionTools.getDistributionWikipediaLink(distribution);
		if (link!=null) JOpenURL.open(this,link);
	}

	/**
	 * F�r ein Eingabefeld zum Kontextmen� hinzu
	 * @param popup	Kontextmen�
	 * @param record	Datensatz mit den anzuzeigenden Eingabefeldern
	 * @see #showContextMenu(MouseEvent, boolean)
	 */
	private void buildQuickEdit(final JPopupMenu popup, final JDistributionEditorPanelRecord record) {
		final String[] labels=record.getEditLabels();
		final String[] values=record.getValues(distribution);
		final JTextField[] fields=new JTextField[values.length];

		/* Typ �ndern */
		final String[] distNames=JDistributionEditorPanel.getHighlightedDistributions();
		if (distNames.length>0) {
			final List<JDistributionEditorPanelRecord> records=JDistributionEditorPanelRecord.getList(null,false,false);
			final JMenu typeItem=new JMenu(ChangeDistributionType);

			popup.add(typeItem);
			for (String distName: distNames) {
				final JDistributionEditorPanelRecord info=records.stream().filter(r->r.isForDistribution(distName)).findFirst().orElseGet(()->null);
				final JRadioButtonMenuItem item=new JRadioButtonMenuItem(distName,info!=null && info==record);
				typeItem.add(item);
				item.addActionListener(e->{
					final JRadioButtonMenuItem actionItem=(JRadioButtonMenuItem)e.getSource();
					final AbstractDistributionWrapper oldWrapper=DistributionTools.getWrapper(distribution);
					final AbstractDistributionWrapper newWrapper=DistributionTools.getWrapper(actionItem.getText());
					if (oldWrapper==newWrapper) return;
					double mean=DistributionTools.getMean(distribution);
					if (Double.isNaN(mean) || Double.isInfinite(mean) || mean<0 || mean>10E10) mean=10;
					mean=NumberTools.reduceDigits(mean,5);
					double sd=DistributionTools.getStandardDeviation(distribution);
					if (Double.isNaN(sd) || Double.isInfinite(sd) || sd<0 || sd>10E10) sd=1;
					sd=NumberTools.reduceDigits(sd,5);
					AbstractRealDistribution newDistribution=newWrapper.getDistribution(mean,sd);
					if (newDistribution==null) newDistribution=newWrapper.getDefaultDistribution();
					if (newDistribution!=null) setDistribution(newDistribution);
				});
			}
			typeItem.addSeparator();
			final StringBuilder infoText=new StringBuilder();
			int lineLength=0;
			for (String token: ChangeDistributionTypeHighlightList.split("\\s")) {
				if (lineLength+token.length()>=30) {
					infoText.append("<br>");
					lineLength=0;
				}
				infoText.append(token);
				lineLength+=token.length();
				infoText.append(" ");
				lineLength+=1;
			}
			final JMenuItem infoItem=new JMenuItem("<html><body>"+infoText.toString()+"</body></html>");
			infoItem.setEnabled(false);
			typeItem.add(infoItem);
			popup.addSeparator();
		}

		/* Werte �ndern */
		for (int i=0;i<Math.min(labels.length,values.length);i++) {
			final JMenuItem item=new JMenuItem("<html><body><b>"+labels[i]+"</b></body></html>");
			item.setEnabled(false);
			popup.add(item);

			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			popup.add(line);
			line.setOpaque(false);

			fields[i]=new JTextField(values[i],10);
			line.add(Box.createHorizontalStrut(24));
			line.add(fields[i]);

			fields[i].addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					final AbstractRealDistribution newDistribution=record.getDistribution(fields,maxXValue);
					if (newDistribution!=null) setDistribution(newDistribution);
				}
			});
		}
	}

	/**
	 * Zeigt ein Kontextmen� zu der Verteilung an
	 * @param e	Ausl�sendes Maus-Ereignis (zur Festlegung der Position des Men�s)
	 * @param showEditButton	Bearbeiten-Schaltfl�che anzeigen?
	 */
	private void showContextMenu(final MouseEvent e, final boolean showEditButton) {
		final JPopupMenu popup=new JPopupMenu();
		JMenu sub;
		JMenuItem item;

		if (showEditButton && distribution!=null) {
			final JDistributionEditorPanelRecord record=JDistributionEditorPanelRecord.getRecord(distribution);
			if (record!=null) buildQuickEdit(popup,record);
			popup.addSeparator();
		}

		popup.add(sub=new JMenu(copy.getText()));
		sub.setIcon(copy.getIcon());
		sub.add(item=new JMenuItem(CopyButtonTable,SimSystemsSwingImages.COPY_AS_TABLE.getIcon()));
		item.addActionListener(ev->copyTableOfValues());
		sub.add(item=new JMenuItem(CopyButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(ev->copyImageToClipboard(getToolkit().getSystemClipboard(),imageSize));

		popup.add(sub=new JMenu(save.getText()));
		sub.setIcon(save.getIcon());
		item.addActionListener(ev->actionSave());
		sub.add(item=new JMenuItem(SaveButtonTable,SimSystemsSwingImages.COPY_AS_TABLE.getIcon()));
		item.addActionListener(ev->copyTableOfValues());
		sub.add(item=new JMenuItem(SaveButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(ev->saveImage());

		popup.add(item=new JMenuItem(wiki.getText(),wiki.getIcon()));
		item.addActionListener(ev->actionWiki());

		if (showEditButton) {
			popup.addSeparator();
			popup.add(item=new JMenuItem(edit.getText(),edit.getIcon()));
			item.addActionListener(ev->editButtonClicked());
		}

		popup.show(JDistributionPanel.this,e.getX()+5,e.getY()+5);
	}
}