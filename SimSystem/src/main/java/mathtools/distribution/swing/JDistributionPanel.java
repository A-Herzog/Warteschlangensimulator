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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
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
 * @version 1.6
 * @see AbstractRealDistribution
 */
public class JDistributionPanel extends JPanel implements JGetImage {
	private static final long serialVersionUID = 5152496149421849230L;

	/** Bezeichner für Fehlermeldung "Keine Verteilung angegeben" */
	public static String ErrorString="Keine Verteilung angegeben";
	/** Bezeichner für Dialogschaltfläche "Bearbeiten" */
	public static String EditButtonLabel="Bearbeiten";
	/** Bezeichner für Tooltip für Dialogschaltfläche "Bearbeiten" */
	public static String EditButtonTooltip="Öffnet den Dialog zum Bearbeiten der Verteilung";
	/** Bezeichner für Dialogschaltfläche "Bearbeiten" im Reand-Only-Modus */
	public static String EditButtonLabelDisabled="Daten anzeigen";
	/** Bezeichner für Tooltip für Dialogschaltfläche "Kopieren" */
	public static String CopyButtonLabel="Kopieren";
	/** Bezeichner für Dialogschaltfläche "Kopieren" */
	public static String CopyButtonTooltip="Kopiert die Darstellung als Grafik in die Zwischenablage";
	/** Bezeichner für Dialogschaltfläche "Speichern" */
	public static String SaveButtonLabel="Speichern";
	/** Bezeichner für Tooltip für Dialogschaltfläche "Speichern" */
	public static String SaveButtonTooltip="Speichert die Grafik als Bilddatei";
	/** Bezeichner für Dialogschaltfläche "Hilfe" */
	public static String WikiButtonLabel="Hilfe";
	/** Bezeichner für Tooltip für Dialogschaltfläche "Hilfe" */
	public static String WikiButtonTooltip="Öffnet ein Browserfenster mit weiteren Informationen zu dem gewählten Verteilungstyp";
	/** Bezeichner "Dichte" */
	public static String DensityLabel="Dichte";
	/** Bezeichner "Zähldichte" */
	public static String CountDensityLabel="Zähldichte";
	/** Bezeichner "Verteilung" */
	public static String CumulativeProbabilityLabel="Verteilung";
	/** Bezeichner für Dialogtitel "Grafik speichern" */
	public static String StoreGraphicsDialogTitle="Grafik speichern";
	/** Bezeichner für Dateiformat jpeg (im Dateiauswahldialog) */
	public static String FileTypeJPEG="jpeg-Dateien";
	/** Bezeichner für Dateiformat gif (im Dateiauswahldialog) */
	public static String FileTypeGIF="gif-Dateien";
	/** Bezeichner für Dateiformat png (im Dateiauswahldialog) */
	public static String FileTypePNG="png-Dateien";
	/** Bezeichner für Dateiformat bmp (im Dateiauswahldialog) */
	public static String FileTypeBMP="bmp-Dateien";
	/** Wikipedia-Link für Hilfe zu Verteilungwn */
	public static String DistributionWikipedia="https://de.wikipedia.org/wiki/";
	/** Überschreibwarnung für Grafiken (Text der Meldung) */
	public static String GraphicsFileOverwriteWarning="Die Datei %s existiert bereits. Soll die Datei jetzt überschrieben werden?";
	/** Überschreibwarnung für Grafiken (Titel der Meldung) */
	public static String GraphicsFileOverwriteWarningTitle="Warnung";

	private final JLabel info;
	private final JButton copy;
	private final JButton save;
	private final JButton edit;
	private final JButton wiki;
	private final JDistributionPlotter plotter;

	private int imageSize=1000;

	private double maxXValue = 1;
	private int plotType = BOTH;
	private AbstractRealDistribution distribution = null;

	private boolean allowDistributionTypeChange = true;
	private boolean allowOk = true;

	/**
	 * Parameter für <code>setPlotType</code>, es wird nur die Dichte angezeigt.
	 * @see #setPlotType(int)
	 * @see #getPlotType()
	 */
	public static final int DENSITY=1;

	/**
	 * Parameter für <code>setPlotType</code>, es wird nur die Verteilungsfunktion angezeigt.
	 * @see #setPlotType(int)
	 * @see #getPlotType()
	 */
	public static final int CUMULATIVEPROBABILITY=2;

	/**
	 * Parameter für <code>setPlotType</code>, es werden Dichte und Verteilungsfunktion angezeigt.
	 * @see #setPlotType(int)
	 * @see #getPlotType()
	 */
	public static final int BOTH=3;

	/**
	 * Konstruktor der Klasse <code>DistributionPanel</code>
	 * @param distribution Zu ladende Verteilung (vom Typ <code>AbstractContinuousDistribution</code>)
	 * @param maxXValue Maximal darzustellender x-Wert
	 * @param showEditButton Soll das "Bearbeiten"-Button angezeigt werden?
	 * @param plotType	Wählt die Darstellungsmethode (Dichte, Verteilung oder beides)
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

		copy=new JButton(CopyButtonLabel);
		copy.setToolTipText(CopyButtonTooltip);
		copy.addActionListener(new ButtonActionEvent());
		copy.setIcon(SimSystemsSwingImages.COPY.getIcon());

		save=new JButton(SaveButtonLabel);
		save.setToolTipText(SaveButtonTooltip);
		save.addActionListener(new ButtonActionEvent());
		save.setIcon(SimSystemsSwingImages.SAVE.getIcon());

		try {
			getToolkit().getSystemClipboard();
			infoPanelEast.add(copy);
			infoPanelEast.add(save);
		} catch (SecurityException e) {}

		if (showEditButton) {
			infoPanelEast.add(edit=new JButton(EditButtonLabel));
			edit.setToolTipText(EditButtonTooltip);
			edit.addActionListener(new ButtonActionEvent());
			edit.setIcon(SimSystemsSwingImages.EDIT.getIcon());
			wiki=null;
		} else {
			edit=null;
			infoPanelEast.add(wiki=new JButton(WikiButtonLabel));
			wiki.setToolTipText(WikiButtonTooltip);
			wiki.addActionListener(new ButtonActionEvent());
			wiki.setIcon(SimSystemsSwingImages.HELP.getIcon());
		}

		add(plotter=new JDistributionPlotter(),BorderLayout.CENTER);

		if (showEditButton) new FileDropper(new Component[]{plotter,infoPanel},new ButtonActionEvent());

		if (showEditButton) addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {if (e.getClickCount()==2) editButtonClicked();}
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
	 * Lädt die anzuzeigende Verteilung aus einer Zeichenkette
	 * @param data Zeichenkette, die die Verteilungsdaten enthält (mit <code>toString</code> zu erzeugen)
	 * @return Liefert <code>true</code> zurück, wenn die Verteilung erfolgreich geladen werden konnte
	 * @see #toString()
	 */
	public boolean fromString(String data) {
		AbstractRealDistribution d=DistributionTools.distributionFromString(data,maxXValue);
		if (d!=null) setDistribution(d);
		return (d!=null);
	}

	/**
	 * Gibt aus, ob im Editor der Verteilungstyp geändert werden darf.
	 * @return	Gibt <code>true</code> zurück, wenn der Verteilungstyp im Editor geändert werden darf.
	 */
	public boolean isAllowDistributionTypeChange() {return allowDistributionTypeChange;}

	/**
	 * Gibt aus, ob die Verteilung im Editor geändert werden darf.
	 * @return	Gibt <code>true</code> zurück, wenn der die Verteilung im Editor geändert werden darf.
	 */
	public boolean isAllowChangeDistributionData() {return allowOk;}

	/**
	 * Stellt ein, ob im Editor der Verteilungstyp geändert werden darf (Vorgabeeinstellung) oder nicht.
	 * @param b	Wird dieser Wert auf <code>true</code> gestellt, darf der Verteilungstyp im Editor geändert werden.
	 */
	public void setAllowDistributionTypeChange(boolean b) {allowDistributionTypeChange=b;}

	/**
	 * Gibt an, ob die Verteilung über den Verteilungseditor bearbeitet werden darf oder nicht.
	 * @param b Wird dieser Wert auf <code>false</code> gestellt, so wird die "Ok"-Schaltfläche im Verteilungseditor deaktiviert.
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
	 * Stellt die Bildgröße beim Kopieren und Speichern sein.
	 * @param imageSize	Vertikale und horizontale Auflösung
	 */
	public void setImageSaveSize(int imageSize) {
		this.imageSize=imageSize;
	}

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
	 * Wird aufgerufen, wenn auf die "Bearbeiten"-Schaltfläche geklickt wurde,
	 * um die Verteilung zu bearbeiten.
	 * @return	Gibt an, ob Änderungen an der Verteilung vorgenommen wurden.
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

	private class ButtonActionEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getSource()==copy) {
				copyImageToClipboard(getToolkit().getSystemClipboard(),imageSize);
				return;
			}
			if (event.getSource()==save) {
				File file=getSaveFileName(); if (file==null) return;
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(JDistributionPanel.this,String.format(GraphicsFileOverwriteWarning,file.toString()),GraphicsFileOverwriteWarningTitle,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
				}

				String extension="";
				int i=file.toString().lastIndexOf('.');
				if (i>0) extension=file.toString().substring(i+1);

				saveImageToFile(file,extension,imageSize);
				return;
			}
			if (event.getSource()==edit) {
				editButtonClicked();
				return;
			}
			if (event.getSource()==wiki) {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						URI uri=getDistributionWikipediaURI();
						if (uri!=null) Desktop.getDesktop().browse(uri);
					} catch (Exception e) {}
				}
			}
			if (event.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)event.getSource();
				final double[] newData=JDataLoader.loadNumbersFromFile(JDistributionPanel.this,data.getFile(),1,Integer.MAX_VALUE);
				if (newData==null) return;
				data.dragDropConsumed();
				DataDistributionImpl d=new DataDistributionImpl(maxXValue,newData);
				setDistribution(d);
				return;
			}
		}
	}
}
