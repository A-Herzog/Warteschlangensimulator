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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;

/**
 * Zeigt eine <code>DataDistributionImpl</code> an und bietet Möglichkeiten zum Bearbeiten der Veteilung an.
 * @author Alexander Herzog
 * @version 1.2
 */
public class JDataDistributionEditPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 6297587258459607410L;

	/**
	 * Beschriftung der "Kopieren"-Schaltfläche
	 */
	public static String ButtonCopy="Kopieren";

	/**
	 * Tooltip für die "Kopieren"-Schaltfläche
	 */
	public static String ButtonCopyTooltip="Kopiert die Werte der Zähldichte oder die grafische Darstellung in die Zwischenablage";

	/**
	 * Option "Als Tabelle kopieren" im Dropdown-Menü der "Kopieren"-Schaltfläche
	 */
	public static String ButtonCopyTable="Tabelle";

	/**
	 * Option "Als Grafik kopieren" im Dropdown-Menü der "Kopieren"-Schaltfläche
	 */
	public static String ButtonCopyGraphics="Grafik";

	/**
	 * Beschriftung der "Einfügen"-Schaltfläche
	 */
	public static String ButtonPaste="Einfügen";

	/**
	 * Tooltip für die "Einfügen"-Schaltfläche
	 */
	public static String ButtonPasteTooltip="Lädt die Zähldichte aus der Zwischenablage";

	/**
	 * Beschriftung der "Laden"-Schaltfläche
	 */
	public static String ButtonLoad="Laden";

	/**
	 * Tooltip für die "Laden"-Schaltfläche
	 */
	public static String ButtonLoadTooltip="Lädt die Zähldichte aus einer Datei";

	/**
	 * Titel des Dialogs zum Laden einer Verteilung
	 */
	public static String ButtonLoadDialogTitle="Verteilung laden";

	/**
	 * Beschriftung der "Speichern"-Schaltfläche
	 */
	public static String ButtonSave="Speichern";

	/**
	 * Tooltip für die "Speichern"-Schaltfläche
	 */
	public static String ButtonSaveTooltip="Speichert die Werte der Zähldichte oder die grafische Darstellung in einer Datei";

	/**
	 * Titel des Dialogs zum Speichern einer Verteilung
	 */
	public static String ButtonSaveDialogTitle="Verteilung speichern";

	/**
	 * Fehlermeldung "Laden fehlgeschlagen"
	 */
	public static String LoadError="Aus den Daten konnte keine Verteilung generiert werden.";

	/**
	 * Titel für Fehlemeldungen
	 */
	public static String LoadErrorTitle="Fehler";

	/**
	 * Überschreibwarnung - Inhalt
	 */
	public static String SaveOverwriteWarning="Die Datei %s existiert bereits. Soll die Datei jetzt überschrieben werden?";

	/**
	 * Überschreibwarnung - Titel
	 */
	public static String SaveOverwriteWarningTitle="Warnung";

	/**
	 * Bezeichner "Zähldichte"
	 */
	public static String CountDensityLabel="Zähldichte";

	/**
	 * Bezeichner "Verteilung"
	 */
	public static String CumulativeProbabilityLabel="Verteilung";

	/** Darzustellende Verteilung */
	private DataDistributionImpl distribution;

	/** Gibt an, welche Daten in dem Diagramm angezeigt werden sollen */
	private final PlotMode plotType;

	/** Gibt an, um wie viel ein Schritt über die "+" und "-" Schaltflächen jeweils verändert werden soll. Der Wert 0 schaltet die Bearbeitungsmöglichkeiten ab. */
	private final double editStep;

	/** Bietet, wenn auf <code>true</code> gesetzt, die Möglichkeit, die Verteilung als Grafik zu speichern (statt sonst nur als Tabelle). */
	private final boolean saveAsImageButtons;

	/** Gibt die Anzahl an Werten in der ursprünglich geladenen empirischen Verteilung an. */
	private int originalSteps;

	/** Zahlenformat für die Beschriftung der y-Achse */
	private LabelMode labelFormat=LabelMode.LABEL_VALUE;

	/** Funktionsplotter innerhalb des Panels */
	private final DataPlotter plotter;

	/**
	 * Drag&amp;Drop-Empfänger für Dateien
	 * (muss als Feld vorgehalten werden, um ihn zur Laufzeit aktivieren oder deaktivieren zu können)
	 * @see #setEditable(boolean)
	 */
	private FileDropper drop;

	/** Toolbar in dem Panel */
	private final JToolBar toolbar;

	/** "Einfügen"-Schaltfläche */
	private final JButton pasteButton;

	/** "Kopieren"-Schaltfläche */
	private final JButton copyButton;

	/** "Laden"-Schaltfläche */
	private final JButton loadButton;

	/** "Speichern"-Schaltfläche */
	private final JButton saveButton;

	/** Popup-Menü für die "Kopieren"-Schaltfläche */
	private final JPopupMenu copyPopup;

	/** "Kopieren als Tabelle"-Menüpunkt für das Kopieren-Popup-Menü */
	private final JMenuItem copyPopup1;

	/** "Kopieren als Grafik"-Menüpunkt für das Kopieren-Popup-Menü */
	private final JMenuItem copyPopup2;

	/** Eingabezeile für die einzelnen Zahlenwerte der empirischen Verteilung */
	private final JTextField editLine;

	/**
	 * Listener die bei einer Änderung der Verteilung benachrichtigt werden sollen.
	 * @see #notifyChangeListener()
	 */
	private final List<ActionListener> changeListener;

	/**
	 * Bildgröße beim Kopieren und Speichern
	 * @see #setImageSaveSize(int)
	 */
	private int imageSize=1000;

	/**
	 * Gibt an, welche Daten in dem Diagramm angezeigt werden sollen
	 * @author Alexander Herzog
	 */
	public enum PlotMode {
		/** Nur die Dichte anzeigen. */
		PLOT_DENSITY,

		/** Dichte und Verteilung anzeigen. */
		PLOT_BOTH
	}

	/**
	 * Zahlenformat für die Beschriftung der y-Achse
	 * @author Alexander Herzog
	 */
	public enum LabelMode {
		/** Ausgabe der Werte der Verteilung */
		LABEL_VALUE,

		/** Ausgabe der Werte der Verteilung als Prozentwerte */
		LABEL_PERCENT
	}

	/**
	 * Konstruktor der Klasse <code>JDataDistributionEditPanel</code>
	 * @param distribution	Darzustellende Verteilung. Die Verteilung wird kopiert; das Originalobjekt wird nicht verändert.
	 * @param plotType	Darstellungsart, siehe <code>PLOT_</code>-Konstanten
	 * @param editable	Gibt an, ob die Schaltflächen "Einfügen" und "Laden" angezeigt werden sollen
	 * @param editStep Gibt an, um wie viel ein Schritt über die "+" und "-" Schaltflächen jeweils verändert werden soll. Der Wert 0 schaltet die Bearbeitungsmöglichkeiten ab.
	 * @param saveAsImageButtons Bietet, wenn auf <code>true</code> gesetzt, die Möglichkeit, die Verteilung als Grafik zu speichern (statt sonst nur als Tabelle).
	 */
	public JDataDistributionEditPanel(DataDistributionImpl distribution, PlotMode plotType, boolean editable, double editStep, boolean saveAsImageButtons) {
		changeListener=new ArrayList<>();
		if (distribution==null) distribution=new DataDistributionImpl(1,1);
		this.distribution=distribution.clone();
		originalSteps=distribution.densityData.length;
		distribution.updateCumulativeDensity();
		this.plotType=plotType;
		this.editStep=editStep;
		this.saveAsImageButtons=saveAsImageButtons;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(Color.GRAY));

		if (editable) {
			drop=new FileDropper(this,new ButtonListener());
		} else {
			drop=null;
		}

		/* Kopf */
		toolbar=new JToolBar(); add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);
		pasteButton=addButton(ButtonPaste,ButtonPasteTooltip,SimSystemsSwingImages.PASTE.getIcon());
		pasteButton.setVisible(editable);
		copyButton=addButton(ButtonCopy,ButtonCopyTooltip,SimSystemsSwingImages.COPY.getIcon());
		loadButton=addButton(ButtonLoad,ButtonLoadTooltip,SimSystemsSwingImages.LOAD.getIcon());
		loadButton.setVisible(editable);
		saveButton=addButton(ButtonSave,ButtonSaveTooltip,SimSystemsSwingImages.SAVE.getIcon());

		copyPopup=new JPopupMenu();
		copyPopup.add(copyPopup1=new JMenuItem(ButtonCopyTable));
		copyPopup1.addActionListener(new ButtonListener());
		copyPopup1.setIcon(SimSystemsSwingImages.COPY_AS_TABLE.getIcon());
		copyPopup.add(copyPopup2=new JMenuItem(ButtonCopyGraphics));
		copyPopup2.setIcon(SimSystemsSwingImages.COPY_AS_IMAGE.getIcon());
		copyPopup2.addActionListener(new ButtonListener());

		/* Mitte */
		plotter=new DataPlotter(); add(plotter,BorderLayout.CENTER);

		/* Fuß */
		JPanel bottom=new JPanel(new BorderLayout()); add(bottom,BorderLayout.SOUTH);
		bottom.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		editLine=new JTextField();
		bottom.add(editLine,BorderLayout.CENTER);
		editLine.setText(distribution.getDensityString());
		editLine.addKeyListener(new EditListener());
		editLine.setEditable(editable);
	}

	/**
	 * Konstruktor der Klasse <code>JDataDistributionEditPanel</code>
	 * @param distribution	Darzustellende Verteilung. Die Verteilung wird kopiert; das Originalobjekt wird nicht verändert.
	 * @param plotType	Darstellungsart, siehe <code>PLOT_</code>-Konstanten
	 * @param editable	Gibt an, ob die Schaltflächen "Einfügen" und "Laden" angezeigt werden sollen
	 * @param editStep Gibt an, um wie viel ein Schritt über die "+" und "-" Schaltflächen jeweils verändert werden soll. Der Wert 0 schaltet die Bearbeitungsmöglichkeiten ab.
	 */
	public JDataDistributionEditPanel(DataDistributionImpl distribution, PlotMode plotType, boolean editable, double editStep) {
		this(distribution,plotType,editable,editStep,false);
	}

	/**
	 * Konstruktor der Klasse <code>JDataDistributionEditPanel</code>
	 * (Die Verteilung kann nicht über die "+" und "-" Symbole verändert werden.)
	 * @param distribution	Darzustellende Verteilung. Die Verteilung wird kopiert; das Originalobjekt wird nicht verändert.
	 * @param plotType	Darstellungsart, siehe <code>PLOT_</code>-Konstanten
	 * @param editable	Gibt an, ob die Schaltflächen "Einfügen" und "Laden" angezeigt werden sollen
	 */
	public JDataDistributionEditPanel(DataDistributionImpl distribution, PlotMode plotType, boolean editable) {
		this(distribution,plotType,editable,0,false);
	}

	/**
	 * Konstruktor der Klasse <code>JDataDistributionEditPanel</code>
	 * (Die Verteilung kann weder über die "+" und "-" Symbole noch über "Einfügen", "Laden" und eine Direkteingabe verändert werden.)
	 * @param distribution	Darzustellende Verteilung. Die Verteilung wird kopiert; das Originalobjekt wird nicht verändert.
	 * @param plotType	Darstellungsart, siehe <code>PLOT_</code>-Konstanten
	 */
	public JDataDistributionEditPanel(DataDistributionImpl distribution, PlotMode plotType) {
		this(distribution,plotType,false,0,false);
	}

	/**
	 * Stellt die Bildgröße beim Kopieren und Speichern sein.
	 * @param imageSize	Vertikale und horizontale Auflösung
	 */
	public void setImageSaveSize(int imageSize) {
		this.imageSize=imageSize;
	}

	/**
	 * Erstellt eine neue Schaltfläche und fügt sie zur Symbolleiste hinzu.
	 * @param title	Beschriftung der Schaltfläche
	 * @param hint	Tooltip für die Schaltfläche
	 * @param icon	Optionales Icon für die Schaltfläche (darf <code>null</code> sein)
	 * @return	Neue Schaltfläche (ist bereits in {@link #toolbar} eingefügt)
	 */
	private JButton addButton(final String title, final String hint, final Icon icon) {
		JButton button;
		toolbar.add(button=new JButton(title));
		button.addActionListener(new ButtonListener());
		button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	/**
	 * Liefert die (möglicherweise bearbeitete) Verteilung zurück.
	 * (Es wird nicht das interne Verteilungsobjekt geliefert, sondern eine Kopie, die beliebig verwendet werden kann.)
	 * @return	Aktuelle Verteilung
	 */
	public DataDistributionImpl getDistribution() {
		DataDistributionImpl dist=distribution.clone();
		dist.stretchToValueCount(originalSteps);
		dist.updateCumulativeDensity();
		return dist;
	}

	/**
	 * Setzt eine neue Verteilung.
	 * @param newDistribution	Darzustellende Verteilung. Die Verteilung wird kopiert; das Originalobjekt wird nicht verändert.
	 */
	public void setDistribution(DataDistributionImpl newDistribution) {
		if (newDistribution==null) newDistribution=new DataDistributionImpl(1,1);
		if (DistributionTools.compare(newDistribution,distribution)) return;
		distribution=newDistribution.clone();
		originalSteps=newDistribution.densityData.length;
		distribution.updateCumulativeDensity();
		editLine.setText((labelFormat==LabelMode.LABEL_PERCENT)?distribution.getDensityStringPercent():distribution.getDensityString());
		repaint();
		notifyChangeListener();
	}

	/**
	 * Stellt ein, ob die Werte verändert werden können.
	 * @param editable	Wird hier <code>true</code> übergeben, so können die Werte der Verteilung verändert werden.
	 */
	public void setEditable(boolean editable) {
		if (editable) {
			if (drop==null) drop=new FileDropper(this,new ButtonListener());
		} else {
			if (drop!=null) {drop.quit(); drop=null;}
		}

		pasteButton.setVisible(editable);
		loadButton.setVisible(editable);
		editLine.setEditable(editable);
	}

	/**
	 * Gibt an, wie die y-Werte angezeigt werden sollen.
	 * @param labelFormat	Eine der <code>LABEL_*</code>-Konstanten.
	 */
	public void setLabelFormat(LabelMode labelFormat) {
		this.labelFormat=labelFormat;
		editLine.setText((this.labelFormat==LabelMode.LABEL_PERCENT)?distribution.getDensityStringPercent():distribution.getDensityString());
	}

	/**
	 * Fügt einen {@link ActionListener} zu der Liste der Objekte, die bei einer Änderung der Verteilung benachrichtigt werden sollen, hinzu.
	 * @param listener	Zusätzlich zu benachrichtigender <code>ActionListener</code>
	 */
	public void addChangeListener(ActionListener listener) {
		if (changeListener.indexOf(listener)<0) changeListener.add(listener);
	}

	/**
	 * Entfernt einen {@link ActionListener} aus der Liste der Objekte, die bei einer Änderung der Verteilung benachrichtigt werden sollen.
	 * @param listener	Nicht mehr zu benachrichtigender <code>ActionListener</code>
	 * @return Gibt <code>true</code> zurück, wenn sich der Listener in der Listze befand und entfernt werden konnte.
	 */
	public boolean removeChangeListener(ActionListener listener) {
		int index=changeListener.indexOf(listener);
		if (index>=0) changeListener.remove(index);
		return (index>=0);
	}

	/**
	 * Benachrichtigt die registrierten Listen, die bei einer Änderung der Verteilung benachrichtigt werden sollen.
	 * @see #addChangeListener(ActionListener)
	 * @see #removeChangeListener(ActionListener)
	 * @see #setDistribution(DataDistributionImpl)
	 */
	private void notifyChangeListener() {
		ActionEvent action=new ActionEvent(this,ActionEvent.ACTION_FIRST,"changed");
		for (ActionListener listener : changeListener) listener.actionPerformed(action);
	}

	/**
	 * Lädt die Verteilung aus einer Zeichenkette
	 * @param line	Zeichenkette aus der die Verteilung geladen werden soll
	 * @return	Gibt an, ob das Laden erfolgreich war
	 */
	private boolean setDistributionFromString(String line) {
		/* Laden */
		final DataDistributionImpl d=DataDistributionImpl.createFromAnyString(line,distribution.upperBound);
		if (d==null) return false;

		/* Verteilung setzen */
		if (!DistributionTools.compare(distribution,d)) {
			distribution.densityData=d.densityData;
			distribution.updateCumulativeDensity();
			notifyChangeListener();
		}

		return true;
	}

	/**
	 * Aktualisiert die Verteilungsdarstellung nach dem Ende der
	 * Eingabe von Zahlen in der Eingabezeile
	 * @see #editLine
	 */
	private void updateDistributionFromEditLine() {
		if (setDistributionFromString(editLine.getText())) {
			editLine.setBackground(NumberTools.getTextFieldDefaultBackground());
			repaint();
		} else {
			editLine.setBackground(Color.red);
		}
	}

	/**
	 * Ermöglicht die Auswahl eines Dateinamens zum Speichern der Daten
	 * @return	Gewählte Datei oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 * @see #saveButton
	 */
	private File getSaveFileName() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(ButtonSaveDialogTitle);

		final FileFilter xlsx=new FileNameExtensionFilter(Table.FileTypeExcel+" (*.xlsx)","xlsx");
		final FileFilter xls=new FileNameExtensionFilter(Table.FileTypeExcelOld+" (*.xls)","xls");
		final FileFilter ods=new FileNameExtensionFilter(Table.FileTypeODS+" (*.ods)","ods");
		final FileFilter txt=new FileNameExtensionFilter(Table.FileTypeText+" (*.txt, *.tsv)","txt","tsv");
		final FileFilter csv=new FileNameExtensionFilter(Table.FileTypeCSV+" (*.csv)","csv");
		final FileFilter csvr=new FileNameExtensionFilter(Table.FileTypeCSV+" (*.csvr)","csvr");
		final FileFilter dif=new FileNameExtensionFilter(Table.FileTypeDIF+" (*.dif)","dif");
		final FileFilter sylk=new FileNameExtensionFilter(Table.FileTypeSYLK+" (*.slk, *.sylk)","slk","sylk");
		final FileFilter docx=new FileNameExtensionFilter(Table.FileTypeWord+" (*.docx)","docx");
		final FileFilter html=new FileNameExtensionFilter(Table.FileTypeHTML+" (*.html)","html");
		final FileFilter tex=new FileNameExtensionFilter(Table.FileTypeTex+" (*.tex)","tex");

		fc.addChoosableFileFilter(xlsx);
		fc.addChoosableFileFilter(xls);
		fc.addChoosableFileFilter(ods);
		fc.addChoosableFileFilter(txt);
		fc.addChoosableFileFilter(csv);
		fc.addChoosableFileFilter(csvr);
		fc.addChoosableFileFilter(dif);
		fc.addChoosableFileFilter(sylk);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(tex);

		FileFilter jpg=null, gif=null, png=null;
		if (saveAsImageButtons) {
			jpg=new FileNameExtensionFilter(JDistributionPanel.FileTypeJPEG+" (*.jpg, *.jpeg)","jpg","jpeg");
			gif=new FileNameExtensionFilter(JDistributionPanel.FileTypeGIF+" (*.gif)","gif");
			png=new FileNameExtensionFilter(JDistributionPanel.FileTypePNG+" (*.png)","png");
			fc.addChoosableFileFilter(jpg);
			fc.addChoosableFileFilter(gif);
			fc.addChoosableFileFilter(png);
		}

		fc.setFileFilter(xlsx);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==xlsx) file=new File(file.getAbsoluteFile()+".xlsx");
			if (fc.getFileFilter()==xls) file=new File(file.getAbsoluteFile()+".xls");
			if (fc.getFileFilter()==ods) file=new File(file.getAbsoluteFile()+".ods");
			if (fc.getFileFilter()==txt) file=new File(file.getAbsoluteFile()+".txt");
			if (fc.getFileFilter()==csv) file=new File(file.getAbsoluteFile()+".csv");
			if (fc.getFileFilter()==csvr) file=new File(file.getAbsoluteFile()+".csvr");
			if (fc.getFileFilter()==dif) file=new File(file.getAbsoluteFile()+".dif");
			if (fc.getFileFilter()==sylk) file=new File(file.getAbsoluteFile()+".sylk");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==tex) file=new File(file.getAbsoluteFile()+".tex");
			if (saveAsImageButtons) {
				if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
				if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
				if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			}
		}
		return file;
	}

	/**
	 * Speichert die Darstellung in einer Datei.
	 * @param file	Dateiname
	 * @param format	Dateiformat ({@link ImageIO#write(java.awt.image.RenderedImage, String, File)})
	 * @param imageSize	Bildgröße
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
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

	/**
	 * Zwischenablagen-Datenobjekt für ein Bild
	 * @see JDataDistributionEditPanel#copyImageToClipboard(Clipboard, int)
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
	 * @param imageSize	Bildgröße
	 * @see TransferableImage
	 */
	private void copyImageToClipboard(final Clipboard clipboard, final int imageSize) {
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

		clipboard.setContents(new TransferableImage(image),null);
	}

	/**
	 * Listenklasse die auf das Klicken auf die Symbolleisten-Schaltflächen reagiert.
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource()==pasteButton) {
				Transferable cont=getToolkit().getSystemClipboard().getContents(this);
				if (cont==null) return;
				String s=null;
				try {s=(String)cont.getTransferData(DataFlavor.stringFlavor);} catch (Exception ex) {s=null;}
				if (s==null) return;
				double[] newData=JDataLoader.loadNumbersFromString(JDataDistributionEditPanel.this,s,1,originalSteps);
				if (newData==null) return;
				distribution.densityData=newData;
				distribution.stretchToValueCount(originalSteps);
				distribution.updateCumulativeDensity();
				editLine.setText((labelFormat==LabelMode.LABEL_PERCENT)?distribution.getDensityStringPercent():distribution.getDensityString());
				notifyChangeListener();
				repaint();
				return;
			}

			if (e.getSource()==copyButton) {
				if (saveAsImageButtons) {copyPopup.show(copyButton,0,copyButton.getBounds().height); return;}
				final StringBuilder s=new StringBuilder();
				s.append(NumberTools.formatNumberMax(distribution.densityData[0]));
				for (int i=1;i<distribution.densityData.length;i++) s.append("\n"+NumberTools.formatNumberMax(distribution.densityData[i]));
				getToolkit().getSystemClipboard().setContents(new StringSelection(s.toString()),null);
				return;
			}

			if (e.getSource()==copyPopup1) {
				final StringBuilder s=new StringBuilder();
				s.append(NumberTools.formatNumberMax(distribution.densityData[0]));
				for (int i=1;i<distribution.densityData.length;i++) s.append("\n"+NumberTools.formatNumberMax(distribution.densityData[i]));
				getToolkit().getSystemClipboard().setContents(new StringSelection(s.toString()),null);
				return;
			}

			if (e.getSource()==copyPopup2) {
				copyImageToClipboard(getToolkit().getSystemClipboard(),imageSize);
			}

			if (e.getSource()==loadButton) {
				double[] newData=JDataLoader.loadNumbers(JDataDistributionEditPanel.this,ButtonLoadDialogTitle,1,originalSteps);
				if (newData==null) return;
				distribution.densityData=newData;
				distribution.stretchToValueCount(originalSteps);
				distribution.updateCumulativeDensity();
				editLine.setText((labelFormat==LabelMode.LABEL_PERCENT)?distribution.getDensityStringPercent():distribution.getDensityString());
				notifyChangeListener();
				repaint();
				return;
			}

			if (e.getSource()==saveButton) {
				final StringBuilder s=new StringBuilder();
				s.append(NumberTools.formatNumberMax(distribution.densityData[0]));
				for (int i=1;i<distribution.densityData.length;i++) s.append("\n"+NumberTools.formatNumberMax(distribution.densityData[i]));
				final File file=getSaveFileName();
				if (file==null) return;
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(JDataDistributionEditPanel.this,String.format(SaveOverwriteWarning,file.toString()),SaveOverwriteWarningTitle,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
				}

				String extension="";
				int i=file.toString().lastIndexOf('.');
				if (i>0) extension=file.toString().substring(i+1);

				if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
					saveImageToFile(file,extension,imageSize);
				} else {
					Table table=new Table(s.toString());
					table.save(file);
				}
				return;
			}

			if (e.getSource() instanceof FileDropperData) {
				final FileDropperData data=(FileDropperData)e.getSource();
				final double[] newData=JDataLoader.loadNumbersFromFile(JDataDistributionEditPanel.this,data.getFile(),1,originalSteps);
				if (newData==null) return;
				data.dragDropConsumed();
				distribution.densityData=newData;
				distribution.stretchToValueCount(originalSteps);
				distribution.updateCumulativeDensity();
				editLine.setText((labelFormat==LabelMode.LABEL_PERCENT)?distribution.getDensityStringPercent():distribution.getDensityString());
				notifyChangeListener();
				repaint();
				return;
			}
		}
	}

	/**
	 * Listener-Klasse für Texteingaben in die Eingabezeile
	 * @see #editLine
	 */
	private class EditListener implements KeyListener {
		/**
		 * Konstruktor der Klasse
		 */
		public EditListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void keyTyped(KeyEvent e) {updateDistributionFromEditLine();}
		@Override
		public void keyPressed(KeyEvent e) {updateDistributionFromEditLine();}
		@Override
		public void keyReleased(KeyEvent e) {updateDistributionFromEditLine();}
	}

	/**
	 * Eigentlicher Funktionsplotter innerhalb des Gesamt-Panels
	 */
	private class DataPlotter extends JPanel implements MouseListener, MouseMotionListener {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -1793441375989694737L;

		/**
		 * Befindet sich der Mauszeiger momentan innerhalb der Zeichenfläche?
		 */
		private boolean mouseIn=false;

		/**
		 * Position des Mauszeigers innerhalb der Zeichenfläche
		 */
		private Point mousePosition=new Point(0,0);

		/**
		 * Größe des Clientbereichs speichern, um Mauspositionen
		 * in konkrete Balken umrechnen zu können
		 */
		private Rectangle lastR=null;

		/**
		 * Erfolgt die Darstellung im Dark-Modus?
		 */
		public boolean isDark;

		/**
		 * Konstruktor der Klasse
		 */
		public DataPlotter() {
			addMouseListener(this);
			addMouseMotionListener(this);
			final Color textBackground=UIManager.getColor("TextField.background");
			isDark=(textBackground!=null && !textBackground.equals(Color.WHITE));
		}

		/**
		 * Berechnet die tatsächlich verfügbare Zeichenfläche
		 * @return	Verfügbare Zeichenfläche
		 */
		private Rectangle getClientRect() {
			Rectangle r=getBounds();
			Insets i=getInsets();
			return new Rectangle(i.left,i.top,r.width-i.left-i.right-1,r.height-i.top-i.bottom-1);
		}

		/**
		 * Zeichnet den Rahmen
		 * @param g	Ausgabe Ziel
		 * @param r	Rechteck-Bereich für den Rahmen
		 * @param padding	Abstände nach außen für Beschriftungen
		 * @see #paint(Graphics)
		 */
		private void paintFrame(Graphics g, Rectangle r, int padding) {
			/* Hintergrund */
			final Graphics2D g2d=(Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			final GradientPaint gp=new GradientPaint(0,0,isDark?Color.GRAY:new Color(235,235,255),0,r.height,isDark?Color.DARK_GRAY:Color.WHITE);
			g2d.setPaint(gp);
			g.fillRect(r.x,r.y,r.width,r.height);

			/* Rahmenlinien links und unten (=Koordinatenachsen) */
			g.setColor(isDark?Color.LIGHT_GRAY:Color.BLACK);
			g.drawLine(r.x,r.y,r.x,r.y+r.height);
			g.drawLine(r.x,r.y+r.height,r.x+r.width,r.y+r.height);

			/* Rahmenlinien oben und rechts */
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(r.x+r.width,r.y,r.x+r.width,r.y+r.height);
			g.drawLine(r.x,r.y,r.x+r.width,r.y);

			g.setColor(isDark?Color.WHITE:Color.BLACK);
			g.drawString("0",r.x,r.y+r.height+padding+g.getFontMetrics().getAscent());
			String s=NumberTools.formatNumber(distribution.upperBound);
			g.drawString(s,r.x+r.width-g.getFontMetrics().stringWidth(s),r.y+r.height+padding+g.getFontMetrics().getAscent());
		}

		/**
		 * Zeichnet die Verteilung
		 * @param g	Ausgabe Ziel
		 * @param r	Bereich
		 * @see #paint(Graphics)
		 */
		private void paintDistribution(Graphics g, Rectangle r) {
			double yLastDensityValue=0;
			double yLastDistributionValue=0;

			if (plotType==PlotMode.PLOT_BOTH) {
				g.setColor(Color.RED);
				g.drawString(CountDensityLabel,r.x+1+2,r.y+g.getFontMetrics().getAscent()+2);
				g.setColor(Color.BLUE);
				g.drawString(CumulativeProbabilityLabel,r.x+1+2,r.y+g.getFontMetrics().getAscent()*2+1+2);
			}

			double maxDensity=0;
			for (int i=0;i<distribution.densityData.length;i++) maxDensity=Math.max(maxDensity,distribution.densityData[i]);
			if (maxDensity==0) maxDensity=1;

			for (int xPixel=r.x;xPixel<=r.x+r.width;xPixel++) {
				double xValue=((double)(xPixel-r.x))/r.width*distribution.upperBound;

				double yDensityValue=distribution.density(xValue);
				if (xPixel>r.x) {
					g.setColor(Color.RED);
					int x1=xPixel-1;
					int x2=xPixel;
					int y1=r.y+r.height-(int)Math.round(yLastDensityValue/maxDensity*r.height);
					int y2=r.y+r.height-(int)Math.round(yDensityValue/maxDensity*r.height);
					g.drawLine(x1,y1,x2,y2);
					g.setColor(new Color(1f,0f,0f,0.05f));
					final int base=r.y+r.height-1;
					g.drawPolygon(new int[]{x1,x1,x2,x2},new int[]{base,y1,y2,base},4);
				}
				yLastDensityValue=yDensityValue;

				if (plotType==PlotMode.PLOT_BOTH) {
					double yDistributionValue;
					yDistributionValue=distribution.cumulativeProbability(xValue);
					if (xPixel>r.x) {
						g.setColor(Color.BLUE);
						g.drawLine(
								xPixel-1,
								r.y+r.height-(int)Math.round(yLastDistributionValue*r.height),
								xPixel,
								r.y+r.height-(int)Math.round(yDistributionValue*r.height)
								);
					}
					yLastDistributionValue=yDistributionValue;
				}
			}
		}

		/**
		 * Liefert die Beschriftung für einen Balken
		 * @param fromValue	Startwert
		 * @param toValue	Endwert
		 * @return	Beschriftung
		 * @see #paintMouseArea(Graphics, Rectangle, int)
		 */
		private String getIntervalText(double fromValue, double toValue) {
			if (distribution.upperBound==86399) {
				return TimeTools.formatTime((int)Math.round(fromValue))+"-"+TimeTools.formatTime((int)Math.round(toValue));
			}
			if (distribution.upperBound==48) {
				return TimeTools.formatTime((int)Math.round(fromValue*1800))+"-"+TimeTools.formatTime((int)Math.round(toValue*1800));
			}

			return NumberTools.formatNumber(fromValue)+"-"+NumberTools.formatNumber(toValue);
		}

		/**
		 * Hebt den Bereich, in dem sich die Maus befindet, hervor.
		 * @param g	Ausgabe Ziel
		 * @param r	Rechteck-Bereich für den Rahmen
		 * @param padding	Abstände nach außen für Beschriftungen
		 * @see #paint(Graphics)
		 */
		private void paintMouseArea(Graphics g, Rectangle r, int padding) {
			String s;
			int w,x;

			double maxDensity=0; for (int i=0;i<distribution.densityData.length;i++) maxDensity=Math.max(maxDensity,distribution.densityData[i]);

			/* Intervall und Intervallbreite bestimmen */
			int interval=(int)Math.floor(((double)mousePosition.x-r.x)/r.width*(distribution.densityData.length));
			interval=Math.min(distribution.densityData.length-1,Math.max(0,interval));
			double intervalWidth=(double)r.width/distribution.densityData.length;

			/* Intervall einzeichnen */
			g.setColor(Color.decode("#A0A0FF"));
			g.fillRect(
					(int)Math.round(r.x+interval*intervalWidth),
					r.y,
					(int)Math.round(intervalWidth),
					r.height
					);

			/* y-Wert anzeigen */
			switch (labelFormat) {
			case LABEL_VALUE: s=NumberTools.formatNumber(distribution.densityData[interval],3); break;
			case LABEL_PERCENT: s=NumberTools.formatNumber(distribution.densityData[interval]*100,0)+"%"; break;
			default: s="";
			}
			int y;
			if (distribution.densityData[interval]>maxDensity/2) {
				/* unten anzeigen */
				y=r.y+(int)Math.round((double)r.height*3/5);
			} else {
				y=r.y+(int)Math.round((double)r.height*2/5);
				/* oben anzeigen */
			}
			w=g.getFontMetrics().stringWidth(s);
			x=(int)Math.round(r.x+(interval+0.5)*intervalWidth)-w/2;
			x=Math.max(x,r.x);
			x=Math.min(x,r.x+r.width-w);
			g.setColor(Color.RED);
			g.drawString(s,x,y);

			/* Beschriftung auf der x-Achse */
			s=getIntervalText(
					(distribution.upperBound+1)*interval/distribution.densityData.length,
					(distribution.upperBound+1)*(interval+1)/distribution.densityData.length
					);
			w=g.getFontMetrics().stringWidth(s);
			x=(int)Math.round(r.x+(interval+0.5)*intervalWidth)-w/2;
			x=Math.max(x,r.x+g.getFontMetrics().stringWidth("0")+padding);
			x=Math.min(x,r.x+r.width-w-g.getFontMetrics().stringWidth(NumberTools.formatNumber(distribution.upperBound))-padding);
			g.setColor(Color.decode("#A0A0FF"));
			g.drawString(s,x,r.y+r.height+padding+g.getFontMetrics().getAscent());

			if (editStep!=0) {
				g.setColor(Color.BLUE);
				double middle=r.x+interval*intervalWidth+intervalWidth/2;
				g.drawString("+",(int)Math.ceil(middle-g.getFontMetrics().stringWidth("+")/2.0),r.y+g.getFontMetrics().getAscent()+padding);
				g.drawString("-",(int)Math.ceil(middle-g.getFontMetrics().stringWidth("-")/2.0),r.y+r.height-padding);

				g.setColor(Color.decode("#5050FF"));
				x=(int)Math.round(r.x+interval*intervalWidth);
				int height=g.getFontMetrics().getAscent()+2*padding;
				g.drawRect(x,r.y,(int)Math.round(intervalWidth)-1,height);
				g.drawRect(x,r.y+r.height-height,(int)Math.round(intervalWidth)-1,height);
			}
		}

		@Override
		public void paint(Graphics g) {
			final int padding=3;
			super.paint(g);

			/* Größe des Rahmens bestimmen */
			Rectangle r=getClientRect();
			r.x+=padding; r.width-=2*padding;
			r.y+=padding; r.height-=3*padding+g.getFontMetrics().getAscent();

			/* Rahmen plotten */
			paintFrame(g,r,padding);

			/* Größe des Funktionsplot-Bereichs bestimmen */
			r.x++; r.width-=2; r.y++; r.height-=2;

			/* Mausbereich hervorheben */
			if (mouseIn) paintMouseArea(g,r,padding);

			/* Dichte und Verteilung anzeigen */
			paintDistribution(g,r);

			/* Rechteck für Mausklicks speichern */
			lastR=r;
		}

		@Override
		public void mouseDragged(MouseEvent e) {}

		@Override
		public void mouseMoved(MouseEvent e) {mouseIn=true; mousePosition=e.getPoint(); repaint();}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (editStep==0 || !editLine.isEditable()) return;

			int interval=(int)Math.floor(((double)mousePosition.x-lastR.x)/lastR.width*(distribution.densityData.length));
			interval=Math.min(distribution.densityData.length-1,Math.max(0,interval));

			if (mousePosition.y-lastR.y>3*lastR.height/4) {
				distribution.densityData[interval]=Math.max(0,distribution.densityData[interval]-editStep);
				distribution.densityData[interval]=Math.round(distribution.densityData[interval]/editStep)*editStep;
				distribution.updateCumulativeDensity();
				editLine.setText((labelFormat==LabelMode.LABEL_PERCENT)?distribution.getDensityStringPercent():distribution.getDensityString());
				notifyChangeListener();
				repaint();
			}

			if (mousePosition.y-lastR.y<lastR.height/4) {
				distribution.densityData[interval]+=editStep;
				distribution.densityData[interval]=Math.round(distribution.densityData[interval]/editStep)*editStep;
				distribution.updateCumulativeDensity();
				editLine.setText((labelFormat==LabelMode.LABEL_PERCENT)?distribution.getDensityStringPercent():distribution.getDensityString());
				notifyChangeListener();
				repaint();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {mouseIn=false; repaint();}
	}
}
