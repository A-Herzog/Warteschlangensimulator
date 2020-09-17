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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.MathRuntimeException;

import mathtools.NumberTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;

/**
 * Zeigt den grafischen Verlauf von Dichte und Verteilungsfunktion einer Verteilung
 * vom Typ <code>AbstractContinuousDistribution</code> an.
 * @author Alexander Herzog
 * @version 1.8
 * @see AbstractRealDistribution
 */
public class JDistributionPanel extends JPanel implements JGetImage {
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
	/** Bezeichner f�r Dialogschaltfl�che "Kopieren" */
	public static String CopyButtonTooltip="Kopiert die Darstellung als Grafik in die Zwischenablage";
	/** Bezeichner f�r Dialogschaltfl�che "Speichern" */
	public static String SaveButtonLabel="Speichern";
	/** Bezeichner f�r Tooltip f�r Dialogschaltfl�che "Speichern" */
	public static String SaveButtonTooltip="Speichert die Grafik als Bilddatei";
	/** Bezeichner f�r Dialogschaltfl�che "Hilfe" */
	public static String WikiButtonLabel="Hilfe";
	/** Bezeichner f�r Tooltip f�r Dialogschaltfl�che "Hilfe" */
	public static String WikiButtonTooltip="�ffnet ein Browserfenster mit weiteren Informationen zu dem gew�hlten Verteilungstyp";
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
	/** Wikipedia-Link f�r Hilfe zu Verteilungwn */
	public static String DistributionWikipedia="https://de.wikipedia.org/wiki/";
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
	private AbstractRealDistribution distribution = null;

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
		copy.setToolTipText(CopyButtonTooltip);
		copy.addActionListener(e->actionCopy());
		copy.setIcon(SimSystemsSwingImages.COPY.getIcon());

		save=new JButton(SaveButtonLabel);
		save.setToolTipText(SaveButtonTooltip);
		save.addActionListener(e->actionSave());
		save.setIcon(SimSystemsSwingImages.SAVE.getIcon());

		try {
			getToolkit().getSystemClipboard();
			infoPanelEast.add(copy);
			infoPanelEast.add(save);
		} catch (SecurityException e) {}

		if (showEditButton) {
			infoPanelEast.add(edit=new JButton(EditButtonLabel));
			edit.setToolTipText(EditButtonTooltip);
			edit.addActionListener(e->editButtonClicked());
			edit.setIcon(SimSystemsSwingImages.EDIT.getIcon());
			wiki=null;
		} else {
			edit=null;
			infoPanelEast.add(wiki=new JButton(WikiButtonLabel));
			wiki.setToolTipText(WikiButtonTooltip);
			wiki.addActionListener(e->actionWiki());
			wiki.setIcon(SimSystemsSwingImages.HELP.getIcon());
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
	public AbstractRealDistribution getDistribution() {return distribution;}

	/**
	 * Setzen einer neuen Verteilung
	 * @param distribution Zu ladende Verteilung (vom Typ <code>AbstractContinuousDistribution</code>)
	 * @see #getDistribution()
	 */
	public void setDistribution(AbstractRealDistribution distribution) {
		this.distribution=distribution;
		setInfoText();
		repaint(); plotter.repaint();
	}

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
				info.setToolTipText(name+" ("+dataLong+")");
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

	private double getRealMaxXValue() {
		double d=maxXValue;
		if (distribution!=null) {
			if (distribution.getSupportUpperBound()*1.1<d) d=distribution.getSupportUpperBound()*1.1;
		}
		return d;
	}

	/**
	 * Stellt die Bildgr��e beim Kopieren und Speichern ein.
	 * @param imageSize	Vertikale und horizontale Aufl�sung
	 */
	public void setImageSaveSize(int imageSize) {
		this.imageSize=imageSize;
	}

	/**
	 * Eigentlicher Funktionsplotter innerhalb des Gesamt-Panels
	 */
	private class JDistributionPlotter extends JPanel {
		private static final long serialVersionUID = 8083886665643583864L;

		protected Rectangle getClientRect() {
			Rectangle r=getBounds();
			Insets i=getInsets();
			return new Rectangle(i.left,i.top,r.width-i.left-i.right-1,r.height-i.top-i.bottom-1);
		}

		private void paintNullDistribution(Graphics g, Rectangle r) {
			g.setColor(Color.red);
			g.drawRect(r.x,r.y,r.width,r.height);
			int w=g.getFontMetrics().stringWidth(ErrorString);

			g.drawString(ErrorString,r.x+r.width/2-w/2,r.y+r.height/2+g.getFontMetrics().getAscent()/2);
		}

		private void paintDistributionRect(final Graphics g, final Rectangle r, final Rectangle dataRect, final double maxXValue) {
			int fontDelta=g.getFontMetrics().getAscent();

			g.setColor(Color.white);
			g.fillRect(dataRect.x,dataRect.y,dataRect.width,dataRect.height);

			g.setColor(Color.black);
			g.drawLine(dataRect.x,dataRect.y,dataRect.x,dataRect.y+dataRect.height);
			g.drawLine(dataRect.x,dataRect.y+dataRect.height,dataRect.x+dataRect.width,dataRect.y+dataRect.height);

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

		private void paintToRectangle(final Graphics g, Rectangle r, final double maxXValue) {
			if (distribution==null) {paintNullDistribution(g,r); return;}

			g.setColor(Color.WHITE);
			g.fillRect(r.x,r.y,r.x+r.width,r.y+r.height);
			g.setColor(Color.GRAY);
			g.drawRect(r.x,r.y,r.x+r.width,r.y+r.height);

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

		public void paintToGraphics(Graphics g) {
			paintToRectangle(g,g.getClipBounds(),getRealMaxXValue());
		}
	}

	private boolean saveImageToFile(File file, String format, int imageSize) {
		BufferedImage image=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_INT_RGB);
		Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		Dimension d=plotter.getSize();
		plotter.setSize(imageSize,imageSize);
		plotter.paint(g);
		plotter.setSize(d);

		try {ImageIO.write(image,format,file);} catch (IOException e) {return false;}
		return true;
	}

	private class TransferableImage implements Transferable{
		public TransferableImage(Image image) {theImage=image;}
		@Override
		public DataFlavor[] getTransferDataFlavors(){return new DataFlavor[]{DataFlavor.imageFlavor};}
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor){return flavor.equals(DataFlavor.imageFlavor);}
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException{if (flavor.equals(DataFlavor.imageFlavor)) return theImage; else throw new UnsupportedFlavorException(flavor);}
		private final Image theImage;
	}

	private void copyImageToClipboard(Clipboard clipboard, int imageSize) {
		final Image image=createImage(imageSize,imageSize);
		final Graphics g=image.getGraphics();
		g.setClip(0,0,imageSize,imageSize);
		final Dimension d=plotter.getSize();
		plotter.setSize(imageSize,imageSize);
		plotter.paint(g);
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

	private File getSaveFileName() {
		JFileChooser fc;
		fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(StoreGraphicsDialogTitle);

		FileFilter jpg=new FileNameExtensionFilter(FileTypeJPEG+" (*.jpg, *.jpeg)","jpg","jpeg");
		FileFilter gif=new FileNameExtensionFilter(FileTypeGIF+" (*.gif)","gif");
		FileFilter png=new FileNameExtensionFilter(FileTypePNG+" (*.png)","png");
		FileFilter bmp=new FileNameExtensionFilter(FileTypeBMP+" (*.bmp)","bmp");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
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
		}
		return file;
	}

	private URI getDistributionWikipediaURI() {
		String distName=DistributionTools.getDistributionName(distribution);
		URL url;
		try {
			url=new URL(DistributionWikipedia+distName.replace(' ','+'));
			try {
				return url.toURI();
			} catch (URISyntaxException e) {return null;}
		} catch (MalformedURLException e) {return null;}
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

	private void actionCopy() {
		copyImageToClipboard(getToolkit().getSystemClipboard(),imageSize);
	}

	private void actionSave() {
		File file=getSaveFileName(); if (file==null) return;
		if (file.exists()) {
			if (JOptionPane.showConfirmDialog(JDistributionPanel.this,String.format(GraphicsFileOverwriteWarning,file.toString()),GraphicsFileOverwriteWarningTitle,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		}

		String extension="";
		int i=file.toString().lastIndexOf('.');
		if (i>0) extension=file.toString().substring(i+1);

		saveImageToFile(file,extension,imageSize);
	}

	private void actionWiki() {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				URI uri=getDistributionWikipediaURI();
				if (uri!=null) {
					if (JOptionPane.showConfirmDialog(JDistributionPanel.this,String.format(GraphicsOpenURLWarning,uri.toString()),GraphicsOpenURLWarningTitle,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
					Desktop.getDesktop().browse(uri);
				}
			} catch (Exception e) {}
		}
	}

	private void buildQuickEdit(final JPopupMenu popup, final JDistributionEditorPanelRecord record) {
		final String[] labels=record.getEditLabels();
		final String[] values=record.getValues(distribution);
		final JTextField[] fields=new JTextField[values.length];

		for (int i=0;i<Math.min(labels.length,values.length);i++) {
			final JMenuItem item=new JMenuItem("<html><body><b>"+labels[i]+"</b></body></html>");
			item.setEnabled(false);
			popup.add(item);

			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			popup.add(line);

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

	private void showContextMenu(final MouseEvent e, final boolean showEditButton) {
		final JPopupMenu popup=new JPopupMenu();
		JMenuItem item;

		if (showEditButton && distribution!=null) {
			final JDistributionEditorPanelRecord record=JDistributionEditorPanelRecord.getRecord(distribution);
			if (record!=null) buildQuickEdit(popup,record);
			popup.addSeparator();
		}

		popup.add(item=new JMenuItem(copy.getText(),copy.getIcon()));
		item.addActionListener(ev->actionCopy());

		popup.add(item=new JMenuItem(save.getText(),save.getIcon()));
		item.addActionListener(ev->actionSave());

		if (showEditButton) {
			popup.add(item=new JMenuItem(edit.getText(),edit.getIcon()));
			item.addActionListener(ev->editButtonClicked());
		}

		popup.show(JDistributionPanel.this,e.getX()+5,e.getY()+5);
	}
}