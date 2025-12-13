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
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
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

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.exception.MathRuntimeException;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.AbstractDiscreteRealDistribution;
import mathtools.distribution.DataDistributionImpl;
import mathtools.distribution.tools.AbstractDistributionWrapper;
import mathtools.distribution.tools.DistributionRandomNumberThreadLocal;
import mathtools.distribution.tools.DistributionTools;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import mathtools.distribution.tools.RandomGeneratorMode;

/**
 * Zeigt den grafischen Verlauf von Dichte und Verteilungsfunktion einer Verteilung
 * vom Typ <code>AbstractContinuousDistribution</code> an.
 * @author Alexander Herzog
 * @version 2.1
 * @see AbstractRealDistribution
 */
public class JDistributionPanel extends JPanel implements JGetImage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
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
	/** Bezeichner für Menüpunkt "Wertetabelle kopieren" im Kopieren-Menü */
	public static String CopyButtonTable="Wertetabelle kopieren";
	/** Bezeichner für Menüpunkt "Zufallszahlen erzeugen und kopieren" im Kopieren-Menü */
	public static String CopyButtonRandomNumbers="Zufallszahlen erzeugen und kopieren";
	/** Bezeichner für Menüpunkt "Bild kopieren" im Kopieren-Menü */
	public static String CopyButtonImage="Bild kopieren";
	/** Bezeichner für Tooltip für Dialogschaltfläche "Speichern" */
	/** Bezeichner für Dialogschaltfläche "Speichern" */
	public static String SaveButtonLabel="Speichern";
	/** Bezeichner für Menüpunkt "Wertetabelle speichern" im Speichern-Menü */
	public static String SaveButtonTable="Zufallszahlen erzeugen und speichern";
	/** Bezeichner für Menüpunkt "Wertetabelle speichern" im Speichern-Menü */
	public static String SaveButtonRandomNumbers="Zufallszahlen erzeugen und speichern";
	/** Bezeichner für Menüpunkt "Bild kopieren" im Speichern-Menü */
	public static String SaveButtonImage="Bild speichern";
	/** Bezeichner für Dialogschaltfläche "Info" */
	public static String InfoButtonLabel="Info";
	/** Bezeichner für Tooltip für Dialogschaltfläche "Info" */
	public static String InfoButtonTooltip="Zeigt weitere Informationen zu dem gewählten Verteilungstyp an";
	/** Zwischenüberschrift "Aktuell gewählte Parameter" im Info-Fenster */
	public static String InfoWindowParameters="Aktuell gewählte Parameter";
	/** Zwischenüberschrift "Weitere Informationen" im Info-Fenster */
	public static String InfoWindowMore="Weitere Informationen";
	/** Bezeichner für den Link zur Wahrscheinlichkeitsverteilungsanzeige-WebApp */
	public static String WebAppButtonLabel="Wahrscheinlichkeitsverteilungsanzeige-WebApp";
	/** Bezeichner für Dialogschaltfläche "Wikipedia" */
	public static String WikiButtonLabel="Wikipedia";
	/** Bezeichner für Tooltip für Dialogschaltfläche "Wikipedia" */
	public static String WikiButtonTooltip="Öffnet ein Browserfenster mit weiteren Informationen zu dem gewählten Verteilungstyp";
	/** Untermenü zur Schnellauswahl des Verteilungstyps */
	public static String ChangeDistributionType="Verteilungstyp";
	/** Informationstext in der Verteilungsanzeige zur Veränderung des Verteilungstyps */
	public static String ChangeDistributionTypeHighlightList="Hier werden nur die hervorgehobenen Verteilungen dargestellt. Eine vollständige Liste steht im Bearbeiten-Dialog zur Auswahl zur Verfügung.";
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
	/** Bezeichner für Dateiformat tiff (im Dateiauswahldialog) */
	public static String FileTypeTIFF="tiff-Dateien";
	/** Überschreibwarnung für Grafiken (Text der Meldung) */
	public static String GraphicsFileOverwriteWarning="Die Datei %s existiert bereits. Soll die Datei jetzt überschrieben werden?";
	/** Überschreibwarnung für Grafiken (Titel der Meldung) */
	public static String GraphicsFileOverwriteWarningTitle="Warnung";
	/** "URL aufrufen" Warnung (Text der Meldung) */
	public static String GraphicsOpenURLWarning="Möchten Sie jetzt die externe Webseite\n%s\naufrufen?";
	/** "URL aufrufen" Warnung (Text der Meldung) */
	public static String GraphicsOpenURLWarningTitle="Warnung";
	/** Eingabeprompt für die Anzahl an zu erzeugenden Zufallszahlen */
	public static String RandomNumbersCount="Anzahl an zu erzeugenden Zufallszahlen";
	/** Fehlermeldung wenn die angegebene Anzahl an zu erzeugenden Zufallszahlen ungültig ist */
	public static String RandomNumbersError="Die Anzahl an Zufallszahlen muss eine positive Ganzzahl sein.";
	/** Kontextmenü-Eintrag "In Rechenausdruck umwandeln" */
	public static String ToCalculationExpression="In Rechenausdruck umwandeln";
	/** Kontextmenü-Eintrag "Generator" */
	public static String Generator="Generator";

	/** Info-Text zu der Verteilung */
	private final JLabel info;

	/** "Kopieren"-Schaltfläche */
	private final JButton copy;

	/** "Speichern"-Schaltfläche */
	private final JButton save;

	/** "Bearbeiten"-Schaltfläche */
	private final JButton edit;

	/** "Info"-Schaltfläche */
	private final JButton help;

	/** "Wikipedia"-Schaltfläche */
	private final JButton wiki;

	/** Funktionsplotter innerhalb des Panels */
	private final JDistributionPlotter plotter;

	/**
	 * Bildgröße beim Kopieren und Speichern
	 * @see #setImageSaveSize(int)
	 */
	private int imageSize=1000;

	/** Maximal darzustellender x-Wert */
	private double maxXValue=1;

	/** Darstellungsmethode (Dichte, Verteilung oder beides) */
	private int plotType=BOTH;

	/** Optionales Callback, welches aufgerufen wird, wenn die Verteilung in einen Rechenausdruck umgewandelt werden soll */
	private final Consumer<String> toExpression;

	/** Darzustellende Verteilung */
	protected AbstractRealDistribution distribution=null;

	/**
	 * Darf der Typ der Verteilung im Editor geändert werden?
	 * @see #isAllowDistributionTypeChange()
	 * @see #setAllowDistributionTypeChange(boolean)
	 */
	private boolean allowDistributionTypeChange=true;

	/**
	 * Dürfen die Parameter der Verteilung im Editor geändert werden?
	 * @see #isAllowChangeDistributionData()
	 * @see #setAllowChangeDistributionData(boolean)
	 */
	private boolean allowOk=true;

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
	 * Zu verwendender Pseudo-Zufalllszahlengenerator
	 */
	private RandomGeneratorMode randomMode;

	/**
	 * Konstruktor der Klasse <code>DistributionPanel</code>
	 * @param distribution Zu ladende Verteilung (vom Typ {@link AbstractRealDistribution})
	 * @param maxXValue Maximal darzustellender x-Wert
	 * @param showEditButton Soll das "Bearbeiten"-Button angezeigt werden?
	 * @param toExpression	Optionales Callback, welches aufgerufen wird, wenn die Verteilung in einen Rechenausdruck umgewandelt werden soll
	 * @param plotType	Wählt die Darstellungsmethode (Dichte, Verteilung oder beides)
	 */
	public JDistributionPanel(AbstractRealDistribution distribution, double maxXValue, boolean showEditButton, Consumer<String> toExpression, int plotType) {
		this.distribution=distribution;
		this.maxXValue=maxXValue;
		this.plotType=plotType;
		this.toExpression=toExpression;

		randomMode=RandomGeneratorMode.defaultRandomGeneratorMode;

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

		infoPanelEast.add(help=new JButton(InfoButtonLabel));
		help.setToolTipText(InfoButtonTooltip);
		help.addActionListener(e->actionInfo());
		help.setIcon(SimSystemsSwingImages.HELP.getIcon());

		infoPanelEast.add(wiki=new JButton(WikiButtonLabel));
		wiki.setToolTipText(WikiButtonTooltip);
		wiki.addActionListener(e->actionWiki());
		wiki.setIcon(SimSystemsSwingImages.WEB.getIcon());

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
	 * @param distribution Zu ladende Verteilung (vom Typ {@link AbstractRealDistribution})
	 * @param maxXValue Maximal darzustellender x-Wert
	 * @param showEditButton Soll das "Bearbeiten"-Button angezeigt werden?
	 * @param plotType	Wählt die Darstellungsmethode (Dichte, Verteilung oder beides)
	 */
	public JDistributionPanel(AbstractRealDistribution distribution, double maxXValue, boolean showEditButton, int plotType) {
		this(distribution,maxXValue,showEditButton,null,plotType);
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
	 * Konstruktor der Klasse <code>DistributionPanel</code>
	 * @param distribution Zu ladende Verteilung (vom Typ <code>AbstractContinuousDistribution</code>)
	 * @param maxXValue Maximal darzustellender x-Wert
	 * @param showEditButton Soll das "Bearbeiten"-Button angezeigt werden?
	 * @param toExpression	Optionales Callback, welches aufgerufen wird, wenn die Verteilung in einen Rechenausdruck umgewandelt werden soll
	 */
	public JDistributionPanel(AbstractRealDistribution distribution, double maxXValue, boolean showEditButton, Consumer<String> toExpression) {
		this(distribution,maxXValue,showEditButton,toExpression,BOTH);
	}

	/**
	 * Auslesen der momentan angezeigten Verteilung
	 * @return Aktuell geladene Verteilung
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
		wiki.setVisible(DistributionTools.getDistributionWikipediaLink(distribution)!=null);
		help.setVisible(DistributionTools.getDistributionInfoHTML(distribution)!=null);
	}

	/**
	 * Aktualisiert den Info-Text in {@link #info} zu der Verteilung.
	 * @return	Liefert <code>true</code>, wenn sich der Text gegenüber dem bisher dargestellten Text verändert hat
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
	public boolean isAllowDistributionTypeChange() {
		return allowDistributionTypeChange;
	}

	/**
	 * Gibt aus, ob die Verteilung im Editor geändert werden darf.
	 * @return	Gibt <code>true</code> zurück, wenn der die Verteilung im Editor geändert werden darf.
	 */
	public boolean isAllowChangeDistributionData() {
		return allowOk;
	}

	/**
	 * Stellt ein, ob im Editor der Verteilungstyp geändert werden darf (Vorgabeeinstellung) oder nicht.
	 * @param b	Wird dieser Wert auf <code>true</code> gestellt, darf der Verteilungstyp im Editor geändert werden.
	 */
	public void setAllowDistributionTypeChange(boolean b) {
		allowDistributionTypeChange=b;
	}

	/**
	 * Gibt an, ob die Verteilung über den Verteilungseditor bearbeitet werden darf oder nicht.
	 * @param b Wird dieser Wert auf <code>false</code> gestellt, so wird die "Ok"-Schaltfläche im Verteilungseditor deaktiviert.
	 */
	public void setAllowChangeDistributionData(boolean b) {
		allowOk=b;
		edit.setText(b?EditButtonLabel:EditButtonLabelDisabled);
		edit.setToolTipText(b?EditButtonTooltip:"");
	}

	/**
	 * Liefert den tatsächlich auftretenden maximalen x-Wert unter Berücksichtigung
	 * des Trägerbereichs der Verteilung.
	 * @return	Maximaler x-Wert (für die Anzeige der Verteilung)
	 * @see #maxXValue
	 */
	protected double getRealMaxXValue() {
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

		/* Wenn der Übergang von F(x)<0.5 zu F(x)>0.99 sehr hart ist (und vermutlich die Ein-Punkt-Verteilung vorliegt), die obere x-Grenze etwas verschieben, so dass die (fast) vertikale Linie zu erkennen ist. */
		if (newMaxXValue*0.99<distribution.getSupportUpperBound() && distribution.cumulativeProbability(newMaxXValue*0.99)<0.5) {
			newMaxXValue=newMaxXValue*1.1;
		}

		return newMaxXValue;


	}

	/**
	 * Stellt die Bildgröße beim Kopieren und Speichern ein.
	 * @param imageSize	Vertikale und horizontale Auflösung
	 */
	public void setImageSaveSize(int imageSize) {
		this.imageSize=imageSize;
	}

	/**
	 * Erzeugt eine Wertetabelle für die Verteilung und kopiert diese in die Zwischenablage.
	 */
	public void copyTableOfValues() {
		if (distribution==null) return;
		DistributionTools.copyTableOfValues(distribution);
	}

	/**
	 * Erzeugt und speichert eine Wertetabelle für die Verteilung.
	 */
	public void saveTableOfValues() {
		if (distribution==null) return;
		DistributionTools.saveTableOfValues(this,distribution);
	}

	/**
	 * Erzeugt eine Reihe von Zufallszahlen basierend auf der Verteilung
	 * und liefert diese als Zeilenumbruch-getrennte Zeichenkette.
	 * @param mode	Überschrift für mögliche Fehlermeldungen
	 * @return	Liefert im Erfolgsfall eine mehrzeilige Zeichenkette. Im Fehlerfall oder bei Nutzerabbruch <code>null</code>.
	 */
	private String getRandomNumbers(final String mode) {
		if (distribution==null) return null;

		final String result=JOptionPane.showInputDialog(this,RandomNumbersCount,"10000");
		if (result==null) return null;
		final Long count=NumberTools.getPositiveLong(result);
		if (count==null) {
			JOptionPane.showMessageDialog(this,RandomNumbersError,mode,JOptionPane.ERROR_MESSAGE);
			return null;
		}

		final double[] arr=new double[(int)Math.min(count.longValue(),1_000_000)];
		final var generator=new DistributionRandomNumberThreadLocal(randomMode);
		generator.init();
		for (int i=0;i<arr.length;i++) arr[i]=generator.random(distribution);

		return DoubleStream.of(arr).mapToObj(NumberTools::formatNumberMax).collect(Collectors.joining("\n"));

	}

	/**
	 * Erzeugt Zufallszahlen basierend auf der Verteilung und kopiert diese in die Zwischenablage.
	 */
	public void copyRandomNumbers() {
		final String numbers=getRandomNumbers(CopyButtonRandomNumbers);
		if (numbers==null) return;
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(numbers),null);
	}

	/**
	 * Erzeugt und speichert Zufallszahlen basierend auf der Verteilung.
	 */
	public void saveRandomNumbers() {
		final String numbers=getRandomNumbers(SaveButtonRandomNumbers);
		if (numbers==null) return;

		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(SaveButtonRandomNumbers);
		fc.addChoosableFileFilter(Table.FileTypeText+" (*.txt)","txt");
		fc.setFileFilter("txt");
		fc.setAcceptAllFileFilterUsed(false);

		final File file=fc.showSaveDialogFileWithExtension(this);
		if (file==null) return;

		if (file.exists()) {
			if (JOptionPane.showConfirmDialog(JDistributionPanel.this,String.format(GraphicsFileOverwriteWarning,file.toString()),GraphicsFileOverwriteWarningTitle,JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		}

		Table.saveTextToFile(numbers,file);
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
		 * Berechnet die tatsächlich verfügbare Zeichenfläche
		 * @return	Verfügbare Zeichenfläche
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
		 * @param r	Bereich für die Ausgabe
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintNullDistribution(Graphics g, Rectangle r) {
			g.setColor(Color.red);
			g.drawRect(r.x,r.y,r.width,r.height);
			int w=g.getFontMetrics().stringWidth(ErrorString);

			g.drawString(ErrorString,r.x+r.width/2-w/2,r.y+r.height/2+g.getFontMetrics().getAscent()/2);
		}

		/**
		 * Gradienten-Hintergrundfarbe im Falle der hellen Darstellung
		 * @see #paintDistributionRect(Graphics, Rectangle, Rectangle, double)
		 */
		private final Color COLOR_BACKGROUND_GRADIENT_LIGHT=new Color(235,235,255);

		/**
		 * Farbe für die Dichte (Füllbereich)
		 * @see #paintDensityDiscrete(Graphics, Rectangle, double)
		 * @see #paintDensityContinuous(Graphics, Rectangle, double)
		 */
		private final Color COLOR_DENSITY=new Color(1f,0f,0f,0.15f);

		/**
		 * Farbe für die Dichte (Linie)
		 * @see #paintDensityDiscrete(Graphics, Rectangle, double)
		 * @see #paintDensityContinuous(Graphics, Rectangle, double)
		 */
		private final Color COLOR_DENSITY_LINE=Color.RED;

		/**
		 * Farbe für die Darstellung des Erwartungswertes
		 * @see #paintExpectedValue(Graphics, Rectangle, double)
		 */
		private final Color COLOR_EXPECTED_VALUE=Color.GREEN.darker();

		/**
		 * Zeichnet den Rahmen für die Darstellung der Verteilungsdaten
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
			final GradientPaint gp=new GradientPaint(0,0,isDark?Color.GRAY:COLOR_BACKGROUND_GRADIENT_LIGHT,0,dataRect.height,isDark?Color.DARK_GRAY:Color.WHITE);
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
				g.setColor(Color.RED);
				g.drawString((distribution instanceof DataDistributionImpl)?CountDensityLabel:DensityLabel,dataRect.x+2+2,dataRect.y+fontDelta+2);
			}
			if ((plotType==CUMULATIVEPROBABILITY) || (plotType==BOTH)) {
				g.setColor(Color.BLUE);
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

			g.setColor(Color.BLUE);

			final double distMaxX=distribution.getSupportUpperBound();
			for (int i=dataRect.x;i<=dataRect.x+dataRect.width;i++) {
				final double x=maxXValue*(i-dataRect.x)/(dataRect.width+1);
				y=(x>=distMaxX)?1:distribution.cumulativeProbability(x);
				final int y1=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*lastY);
				final int y2=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*y);

				if (i>dataRect.x) g.drawLine(i-1,y1,i,y2);
				lastY=y;
			}
		}

		/**
		 * Dichte einer kontinuierlichen Verteilung einzeichnen
		 * @param g	Ausgabe Ziel
		 * @param dataRect	Zeichenbereich
		 * @param maxXValue	Maximal darzustellender x-Wert
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintDensityDiscrete(final Graphics g, final Rectangle dataRect, final double maxXValue) {
			final double distMaxX=distribution.getSupportUpperBound();
			final AbstractDiscreteRealDistribution dist=(AbstractDiscreteRealDistribution)distribution;

			/* Maximal auftretenden y-Wert der Dichte bestimmen */
			double maxY=0.001;
			for (int x=0;x<=Math.min(distMaxX,maxXValue);x++) try {
				final double d=(x>distMaxX)?0:dist.countDensity(x);
				if (!Double.isInfinite(d) && !Double.isNaN(d)) maxY=Math.max(maxY,d);
			} catch (IllegalArgumentException | MathRuntimeException e) {}

			final int yBase=dataRect.y+dataRect.height-1;
			final int halfXBarWidth=Math.max(1,(int)Math.round(dataRect.width/maxXValue/4));

			for (int x=0;x<=Math.min(distMaxX,maxXValue);x++) {
				try {
					final double y=dist.countDensity(x);
					final int x2=dataRect.x+(int)Math.round(dataRect.width*x/maxXValue);
					final int y2=Math.min((int)Math.round(dataRect.height*y/maxY),dataRect.height-2);

					final int barX=Math.max(dataRect.x+1,x2-halfXBarWidth);
					final int barY=yBase-y2;
					final int barWidth=Math.min(2*halfXBarWidth,dataRect.x+dataRect.width-barX-1);
					final int barHeight=y2;

					/* Balken zeichnen */
					g.setColor(COLOR_DENSITY);
					if (barWidth>2) {
						g.fillRect(barX,barY,barWidth,barHeight);
						g.setColor(COLOR_DENSITY_LINE);
					}

					/* Rahmen zeichnen */
					g.drawRect(barX,barY,barWidth,barHeight);
				} catch (IllegalArgumentException | MathRuntimeException e) {}
			}
		}

		/**
		 * Dichte einer kontinuierlichen Verteilung einzeichnen
		 * @param g	Ausgabe Ziel
		 * @param dataRect	Zeichenbereich
		 * @param maxXValue	Maximal darzustellender x-Wert
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintDensityContinuous(final Graphics g, final Rectangle dataRect, final double maxXValue) {
			final double distMaxX=distribution.getSupportUpperBound();

			/* Maximal auftretenden y-Wert der Dichte bestimmen */
			double maxY=0.001;
			for (int i=dataRect.x;i<=dataRect.x+dataRect.width;i++) {
				double x=maxXValue*(i-dataRect.x)/(dataRect.width+1);
				try {
					final double d=(x>distMaxX)?0:distribution.density(x);
					if (!Double.isInfinite(d) && !Double.isNaN(d)) maxY=Math.max(maxY,d);
				} catch (IllegalArgumentException | MathRuntimeException e) {}
			}

			double lastY=0;
			double y=0;

			/* Füllbereich zeichnen */
			final int base=dataRect.y+dataRect.height-1;
			g.setColor(COLOR_DENSITY);
			for (int i=dataRect.x;i<=dataRect.x+dataRect.width;i++) {
				final double x=maxXValue*(i-dataRect.x)/(dataRect.width+1);
				try {y=(x>distMaxX)?0:distribution.density(x)/maxY;} catch (IllegalArgumentException | MathRuntimeException e) {}
				final int y1=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*lastY);
				final int y2=dataRect.y+dataRect.height-(int)Math.round(dataRect.height*y);

				if (i>dataRect.x) {
					/* g.drawLine(i-1,y1,i,y2); */
					g.fillPolygon(new int[]{i-1,i-1,i,i},new int[]{base,y1,y2,base}, 4);
				}
				lastY=y;
			}

			/* Linie zeichnen */
			g.setColor(COLOR_DENSITY_LINE);
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
		 * Senkrechte Linie für Erwartungswert einzeichnen
		 * @param g	Ausgabe Ziel
		 * @param dataRect	Zeichenbereich
		 * @param maxXValue	Maximal darzustellender x-Wert
		 * @see #paintToRectangle(Graphics, Rectangle, double)
		 */
		private void paintExpectedValue(final Graphics g, final Rectangle dataRect, final double maxXValue) {
			final double expectedValue=distribution.getNumericalMean();
			if (Double.isNaN(expectedValue) || Double.isInfinite(expectedValue)) return;

			final double d=expectedValue/maxXValue;
			if (d<=0 || d>=1) return;
			final int x=(int)Math.round(d*dataRect.width);
			final int xPos=x+dataRect.x;
			g.setColor(COLOR_EXPECTED_VALUE);
			g.drawLine(xPos,dataRect.y+1,xPos,dataRect.y+dataRect.height-1);

			final String s="E="+NumberTools.formatNumber(expectedValue);
			final FontMetrics metrics=g.getFontMetrics();

			final int xDrawPosition;
			if (d<=0.5) {
				xDrawPosition=xPos+2;
			} else {
				xDrawPosition=xPos-2-metrics.stringWidth(s);
			}

			final int yShift;
			if (x<=metrics.stringWidth(CumulativeProbabilityLabel)+5) {
				yShift=3*metrics.getAscent()+2*metrics.getDescent();
			} else {
				yShift=metrics.getAscent();
			}

			g.drawString(s,xDrawPosition,dataRect.y+yShift+2);
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

			/* Hintergrund über alles */
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
			if ((plotType==DENSITY) || (plotType==BOTH)) {
				if (distribution instanceof AbstractDiscreteRealDistribution) {
					paintDensityDiscrete(g,dataRect,maxXValue);
				} else {
					paintDensityContinuous(g,dataRect,maxXValue);
				}
			}
			paintExpectedValue(g,dataRect,maxXValue);
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
	 * @param imageSize	Bildgröße
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
	 * Zwischenablagen-Datenobjekt für ein Bild
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
	 * @param imageSize	Bildgröße
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
	 * Ermöglicht die Auswahl eines Dateinamens zum Speichern der Verteilung als Bild
	 * @return	Gewählte Datei oder <code>null</code>, wenn die Auswahl abgebrochen wurde
	 * @see #save
	 */
	private File getSaveFileName() {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(StoreGraphicsDialogTitle);

		fc.addChoosableFileFilter(FileTypeJPEG+" (*.jpg, *.jpeg)","jpg","jpeg");
		fc.addChoosableFileFilter(FileTypeGIF+" (*.gif)","gif");
		fc.addChoosableFileFilter(FileTypePNG+" (*.png)","png");
		fc.addChoosableFileFilter(FileTypeBMP+" (*.bmp)","bmp");
		fc.addChoosableFileFilter(FileTypeTIFF+" (*.tiff, *.tif)","tiff","tif");
		fc.setFileFilter("png");
		fc.setAcceptAllFileFilterUsed(false);
		return fc.showSaveDialogFileWithExtension(this);
	}

	/**
	 * Wird aufgerufen, wenn auf die "Bearbeiten"-Schaltfläche geklickt wurde,
	 * um die Verteilung zu bearbeiten.
	 * @return	Gibt an, ob Änderungen an der Verteilung vorgenommen wurden.
	 */
	private boolean editButtonClicked() {
		Container c=getParent();
		while (!(c instanceof Window)) {c=c.getParent(); if (c==null) return false;}
		JDistributionEditorDialog dialog=new JDistributionEditorDialog((Window)c,getDistribution(),maxXValue,plotType,allowDistributionTypeChange,allowOk,imageSize);
		dialog.setVisible(true);
		AbstractRealDistribution d=dialog.getNewDistribution();
		if (d!=null) {
			boolean result=!DistributionTools.compare(getDistribution(),d);
			setDistribution(d);
			if (result) changedByUser();
			return result;
		}
		return false;
	}

	/**
	 * Wird aufgerufen, wenn die Verteilung über GUI-Interaktionen verändert wurde.
	 */
	protected void changedByUser() {
	}

	/**
	 * Kopieren-Aktion auslösen
	 * @see #copy
	 */
	private void actionCopy() {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(CopyButtonTable,SimSystemsSwingImages.COPY_AS_TABLE.getIcon()));
		item.addActionListener(e->copyTableOfValues());
		menu.add(item=new JMenuItem(CopyButtonRandomNumbers,SimSystemsSwingImages.COPY_RANDOM_NUMBERS.getIcon()));
		item.addActionListener(e->copyRandomNumbers());
		menu.add(item=new JMenuItem(CopyButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(e->copyImageToClipboard(getToolkit().getSystemClipboard(),imageSize));

		menu.addSeparator();

		final JMenu sub=new JMenu(Generator);
		menu.add(sub);
		for (var mode: RandomGeneratorMode.values()) {
			final JRadioButtonMenuItem generatorItem=new JRadioButtonMenuItem(mode.name,mode==randomMode);
			sub.add(generatorItem);
			generatorItem.addActionListener(e2->randomMode=mode);
		}

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
	 * Speichern-Aktion auslösen
	 * @see #save
	 */
	private void actionSave() {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(SaveButtonTable,SimSystemsSwingImages.COPY_AS_TABLE.getIcon()));
		item.addActionListener(e->saveTableOfValues());
		menu.add(item=new JMenuItem(SaveButtonRandomNumbers,SimSystemsSwingImages.COPY_RANDOM_NUMBERS.getIcon()));
		item.addActionListener(e->saveRandomNumbers());
		menu.add(item=new JMenuItem(SaveButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(e->saveImage());

		menu.addSeparator();

		final JMenu sub=new JMenu(Generator);
		menu.add(sub);
		for (var mode: RandomGeneratorMode.values()) {
			final JRadioButtonMenuItem generatorItem=new JRadioButtonMenuItem(mode.name,mode==randomMode);
			sub.add(generatorItem);
			generatorItem.addActionListener(e2->randomMode=mode);
		}

		menu.show(save,0,save.getHeight());
	}

	/**
	 * Info-Dialog zu der gewählten Verteilung öffnen
	 * @see #help
	 */
	private void actionInfo() {
		new JDistributionPanelInfo(this,distribution);
	}

	/**
	 * Wikipedia-Seite zu der gewählten Verteilung öffnen
	 * @see #wiki
	 */
	private void actionWiki() {
		final URI link=DistributionTools.getDistributionWikipediaLink(distribution);
		if (link!=null) JOpenURL.open(this,link);
	}

	/**
	 * Für ein Eingabefeld zum Kontextmenü hinzu
	 * @param popup	Kontextmenü
	 * @param record	Datensatz mit den anzuzeigenden Eingabefeldern
	 * @see #showContextMenu(MouseEvent, boolean)
	 */
	private void buildQuickEdit(final JPopupMenu popup, final JDistributionEditorPanelRecord record) {
		final String[] labels=record.getEditLabels();
		final String[] values=record.getValues(distribution);
		final JTextField[] fields=new JTextField[values.length];

		/* Typ ändern */
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
					if (newDistribution!=null)
					{
						setDistribution(newDistribution);
						changedByUser();
					}
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

		/* Werte ändern */
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
					if (newDistribution!=null) {
						setDistribution(newDistribution);
						changedByUser();
					}
				}
			});
		}
	}

	/**
	 * Zeigt ein Kontextmenü zu der Verteilung an
	 * @param e	Auslösendes Maus-Ereignis (zur Festlegung der Position des Menüs)
	 * @param showEditButton	Bearbeiten-Schaltfläche anzeigen?
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
		sub.add(item=new JMenuItem(CopyButtonRandomNumbers,SimSystemsSwingImages.COPY_RANDOM_NUMBERS.getIcon()));
		item.addActionListener(ev->copyRandomNumbers());
		sub.add(item=new JMenuItem(CopyButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(ev->copyImageToClipboard(getToolkit().getSystemClipboard(),imageSize));

		popup.add(sub=new JMenu(save.getText()));
		sub.setIcon(save.getIcon());
		item.addActionListener(ev->actionSave());
		sub.add(item=new JMenuItem(SaveButtonTable,SimSystemsSwingImages.COPY_AS_TABLE.getIcon()));
		item.addActionListener(ev->saveTableOfValues());
		sub.add(item=new JMenuItem(SaveButtonRandomNumbers,SimSystemsSwingImages.COPY_RANDOM_NUMBERS.getIcon()));
		item.addActionListener(ev->saveRandomNumbers());
		sub.add(item=new JMenuItem(SaveButtonImage,SimSystemsSwingImages.COPY_AS_IMAGE.getIcon()));
		item.addActionListener(ev->saveImage());

		popup.add(sub=new JMenu(Generator));
		for (var mode: RandomGeneratorMode.values()) {
			final JRadioButtonMenuItem generatorItem=new JRadioButtonMenuItem(mode.name,mode==randomMode);
			sub.add(generatorItem);
			generatorItem.addActionListener(e2->randomMode=mode);
		}

		if (DistributionTools.getDistributionInfoHTML(distribution)!=null) {
			popup.add(item=new JMenuItem(help.getText(),help.getIcon()));
			item.addActionListener(ev->actionInfo());
		}

		if (DistributionTools.getDistributionWikipediaLink(distribution)!=null) {
			popup.add(item=new JMenuItem(wiki.getText(),wiki.getIcon()));
			item.addActionListener(ev->actionWiki());
		}

		if (showEditButton) {
			popup.addSeparator();
			popup.add(item=new JMenuItem(edit.getText(),edit.getIcon()));
			item.addActionListener(ev->editButtonClicked());
			if (toExpression!=null) {
				popup.add(item=new JMenuItem(ToCalculationExpression,SimSystemsSwingImages.EXPRESSION.getIcon()));
				item.addActionListener(ev->{
					final String expression=DistributionTools.getCalculationExpression(getDistribution());
					if (expression!=null) toExpression.accept(expression);
				});
			}
		}

		popup.show(JDistributionPanel.this,e.getX()+5,e.getY()+5);
	}
}