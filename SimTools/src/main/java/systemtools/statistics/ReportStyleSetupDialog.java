/**
 * Copyright 2022 Alexander Herzog
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
package systemtools.statistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.TooManyListenersException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import mathtools.distribution.swing.SimSystemsSwingImages;
import systemtools.BaseDialog;
import systemtools.ImageTools;
import systemtools.SimToolsIconListCellRenderer;
import systemtools.images.SimToolsImages;

/**
 * Zeigt einen Dialog zum Bearbeiten eines {@link ReportStyle} an.
 * @author Alexander Herzog
 * @see StatisticsBasePanel
 * @see StatisticViewerReport
 * @see ReportStyle
 */
public class ReportStyleSetupDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1627297987350588918L;

	/**
	 * Tabs zur Aufteilung des Dialogs
	 */
	private final JTabbedPane tabs;

	/* Seitenränder */

	/**
	 * Seitenrand oben (in mm)
	 */
	private final SpinnerNumberModel borderTopMM;

	/**
	 * Seitenrand rechts (in mm)
	 */
	private final SpinnerNumberModel borderRightMM;

	/**
	 * Seitenrand unten (in mm)
	 */
	private final SpinnerNumberModel borderBottomMM;

	/**
	 * Seitenrand links (in mm)
	 */
	private final SpinnerNumberModel borderLeftMM;

	/* Kopfzeile */

	/**
	 * Anzeige-Panel für das in der Kopfzeile anzuzeigende Logo
	 */
	private final ImagePanel logo;

	/**
	 * Horizontale Position des Logos in der Kopfzeile
	 */
	private final JComboBox<String> logoPosition;

	/**
	 * Maximalbreite des Logos in MM
	 */
	private final SpinnerNumberModel logoMaxWidthMM;

	/**
	 * Maximalhöhe des Logos in MM
	 */
	private final SpinnerNumberModel logoMaxHeightMM;

	/**
	 * Soll das Logo auf jeder Seite wiederholt werden?
	 */
	private final JCheckBox logoRepeat;

	/* Fußzeile */

	/**
	 * Ausgabe der Seitennummer in der Fußzeile?
	 */
	private final JCheckBox footerPageNumbers;

	/**
	 * Ausgabe des aktuellen Datums in der Fußzeile?
	 */
	private final JCheckBox footerDate;

	/* Schriftarten */

	/**
	 * Schriftarten für die Überschriften
	 */
	public final FontEditor[] headingFont;

	/**
	 * Schriftart für normalen Text
	 */
	public final FontEditor textFont;

	/**
	 * Schriftart für die Tabellenkopfzeile
	 */
	public final FontEditor tableHeadingFont;

	/**
	 * Schriftart für den Tabelleninhalt
	 */
	public final FontEditor tableTextFont;

	/**
	 * Schriftart für die Fußzeile
	 */
	public final FontEditor footerFont;

	/**
	 * Absatz-Abstand
	 */
	public final SpinnerNumberModel parSkip;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param reportStyle	Zu bearbeitender Report-Style
	 */
	public ReportStyleSetupDialog(final Component owner, final ReportStyle reportStyle) {
		super(owner,StatisticsBasePanel.viewersReportCustomizeTitle);

		/* GUI */
		addUserButton(StatisticsBasePanel.viewersReportCustomizeReset,SimToolsImages.UNDO.getIcon());
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		JPanel tabOuter;
		JPanel tab;
		JPanel line;
		Object[] data;
		JButton button;
		JLabel label;

		/* Seitenränder */
		tabs.addTab(StatisticsBasePanel.viewersReportCustomizeTabPageMargins,tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		data=getValueInputLine(StatisticsBasePanel.viewersReportCustomizeTabPageMarginsTop,reportStyle.borderTopMM);
		tab.add((JPanel)data[0]);
		borderTopMM=(SpinnerNumberModel)data[1];

		data=getValueInputLine(StatisticsBasePanel.viewersReportCustomizeTabPageMarginsRight,reportStyle.borderRightMM);
		tab.add((JPanel)data[0]);
		borderRightMM=(SpinnerNumberModel)data[1];

		data=getValueInputLine(StatisticsBasePanel.viewersReportCustomizeTabPageMarginsBottom,reportStyle.borderBottomMM);
		tab.add((JPanel)data[0]);
		borderBottomMM=(SpinnerNumberModel)data[1];

		data=getValueInputLine(StatisticsBasePanel.viewersReportCustomizeTabPageMarginsLeft,reportStyle.borderLeftMM);
		tab.add((JPanel)data[0]);
		borderLeftMM=(SpinnerNumberModel)data[1];

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(StatisticsBasePanel.viewersReportCustomizePDFandDOCX));

		/* Kopfzeile */
		tabs.addTab(StatisticsBasePanel.viewersReportCustomizeTabHeader,tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogo+":"));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(logo=new ImagePanel(reportStyle.logo));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(button=new JButton(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoLoad,SimSystemsSwingImages.LOAD.getIcon()));
		button.setToolTipText(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoLoadHint);
		button.addActionListener(e->loadLogo());
		line.add(button=new JButton(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoPaste,SimSystemsSwingImages.PASTE.getIcon()));
		button.setToolTipText(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoPasteHint);
		button.addActionListener(e->pasteLogo());
		line.add(button=new JButton(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRemove,SimSystemsSwingImages.CANCEL.getIcon()));
		button.setToolTipText(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRemoveHint);
		button.addActionListener(e->logo.setImage(null));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignment+":"));
		line.add(logoPosition=new JComboBox<>(new String[] {
				StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignmentLeft,
				StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignmentCenter,
				StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoAlignmentRight
		}));
		logoPosition.setRenderer(new SimToolsIconListCellRenderer(new SimToolsImages[] {
				SimToolsImages.TEXT_ALIGN_LEFT,
				SimToolsImages.TEXT_ALIGN_CENTER,
				SimToolsImages.TEXT_ALIGN_RIGHT
		}));
		label.setLabelFor(logoPosition);
		switch (reportStyle.logoPosition) {
		case LEFT: logoPosition.setSelectedIndex(0); break;
		case CENTER: logoPosition.setSelectedIndex(1); break;
		case RIGHT: logoPosition.setSelectedIndex(2); break;
		default: logoPosition.setSelectedIndex(0); break;
		}

		data=getValueInputLine(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoMaxWidth,reportStyle.logoMaxWidthMM);
		tab.add((JPanel)data[0]);
		logoMaxWidthMM=(SpinnerNumberModel)data[1];

		data=getValueInputLine(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoMaxHeight,reportStyle.logoMaxHeightMM);
		tab.add((JPanel)data[0]);
		logoMaxHeightMM=(SpinnerNumberModel)data[1];

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(logoRepeat=new JCheckBox(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRepeat,reportStyle.logoRepeat));
		logoRepeat.setToolTipText(StatisticsBasePanel.viewersReportCustomizeTabHeaderLogoRepeatHint);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(StatisticsBasePanel.viewersReportCustomizePDFonly));

		/* Fußzeile */
		tabs.addTab(StatisticsBasePanel.viewersReportCustomizeTabFooter,tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(footerPageNumbers=new JCheckBox(StatisticsBasePanel.viewersReportCustomizeTabFooterPageNumber,reportStyle.footerPageNumbers));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(footerDate=new JCheckBox(StatisticsBasePanel.viewersReportCustomizeTabFooterDate,reportStyle.footerDate));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(StatisticsBasePanel.viewersReportCustomizePDFonly));

		/* Schriftarten */
		tabs.addTab(StatisticsBasePanel.viewersReportCustomizeTabFonts,tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		headingFont=new FontEditor[reportStyle.headingFont.length];
		for (int i=0;i<reportStyle.headingFont.length;i++) tab.add(headingFont[i]=new FontEditor(String.format(StatisticsBasePanel.viewersReportCustomizeTabFontsHeader,i+1),reportStyle.headingFont[i]));
		tab.add(textFont=new FontEditor(StatisticsBasePanel.viewersReportCustomizeTabFontsText,reportStyle.textFont));
		tab.add(tableHeadingFont=new FontEditor(StatisticsBasePanel.viewersReportCustomizeTabFontsTableHeader,reportStyle.tableHeadingFont));
		tab.add(tableTextFont=new FontEditor(StatisticsBasePanel.viewersReportCustomizeTabFontsTableText,reportStyle.tableTextFont));
		tab.add(footerFont=new FontEditor(StatisticsBasePanel.viewersReportCustomizeTabFontsFooter,reportStyle.footerFont));

		data=getValueInputLine(StatisticsBasePanel.viewersReportCustomizeTabFontsParSkip,reportStyle.parSkip);
		tab.add((JPanel)data[0]);
		parSkip=(SpinnerNumberModel)data[1];

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(StatisticsBasePanel.viewersReportCustomizePDFandDOCX));

		/* Icons auf den Tabs */
		tabs.setIconAt(0,SimToolsImages.REPORT_MARGINS.getIcon());
		tabs.setIconAt(1,SimToolsImages.REPORT_HEADER.getIcon());
		tabs.setIconAt(2,SimToolsImages.REPORT_FOOTER.getIcon());
		tabs.setIconAt(3,SimToolsImages.REPORT_FONTS.getIcon());

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erzeugt eine Zeile mit einem Spin-Edit-Feld
	 * @param name	Beschriftung des Eingabefeldes
	 * @param value	Initialer Wert
	 * @return	Liefert ein zwei-elementiges Array aus Panel und Spin-Modell zurück
	 */
	private Object[] getValueInputLine(final String name, final int value) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));

		final JLabel label=new JLabel(name+":");
		line.add(label);

		final SpinnerNumberModel model=new SpinnerNumberModel(value,0,999,1);
		final JSpinner spinner=new JSpinner(model);
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(spinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(3);
		spinner.setEditor(editor);
		line.add(spinner);
		label.setLabelFor(spinner);

		return new Object[] {line, model};
	}

	/**
	 * Befehl: Bild aus Datei laden
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean loadLogo() {
		final File file=ImageTools.showLoadDialog(this);
		if (file==null) return false;
		try {
			final BufferedImage image=ImageIO.read(file);
			if (image==null) return false;
			logo.setImage(image);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Befehl: Bild aus Zwischenablage einfügen
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean pasteLogo() {
		Transferable transferable=Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

		try {
			if (transferable!=null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				Image tempImage=(Image)transferable.getTransferData(DataFlavor.imageFlavor);
				if (tempImage!=null) {
					final BufferedImage image=new BufferedImage(tempImage.getWidth(null),tempImage.getHeight(null),BufferedImage.TYPE_4BYTE_ABGR);
					image.getGraphics().drawImage(tempImage,0,0,null);
					logo.setImage(image);
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	/**
	 * Befehl: Einstellungen auf einer Seite zurücksetzen
	 * @param nr	Index der Seite
	 */
	private void resetPage(final int nr) {
		final ReportStyle defaultValues=new ReportStyle();

		switch (nr) {
		case 0: /* Seitenränder */
			borderTopMM.setValue(defaultValues.borderTopMM);
			borderRightMM.setValue(defaultValues.borderRightMM);
			borderBottomMM.setValue(defaultValues.borderBottomMM);
			borderLeftMM.setValue(defaultValues.borderLeftMM);
			break;
		case 1: /* Kopfzeile */
			logo.setImage(defaultValues.logo);
			switch (defaultValues.logoPosition) {
			case LEFT: logoPosition.setSelectedIndex(0); break;
			case CENTER: logoPosition.setSelectedIndex(1); break;
			case RIGHT: logoPosition.setSelectedIndex(2); break;
			default: logoPosition.setSelectedIndex(0); break;
			}
			logoMaxWidthMM.setValue(defaultValues.logoMaxWidthMM);
			logoMaxHeightMM.setValue(defaultValues.logoMaxHeightMM);
			logoRepeat.setSelected(defaultValues.logoRepeat);
			break;
		case 2: /* Fußzeile */
			footerPageNumbers.setSelected(defaultValues.footerPageNumbers);
			footerDate.setSelected(defaultValues.footerDate);
			break;
		case 3: /* Schriftarten */
			for (int i=0;i<headingFont.length;i++) headingFont[i].loadFromFont(defaultValues.headingFont[i]);
			textFont.loadFromFont(defaultValues.textFont);
			tableHeadingFont.loadFromFont(defaultValues.tableHeadingFont);
			tableTextFont.loadFromFont(defaultValues.tableTextFont);
			footerFont.loadFromFont(defaultValues.footerFont);
			parSkip.setValue(defaultValues.parSkip);
			break;
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(StatisticsBasePanel.viewersReportCustomizeResetThisPage));
		item.addActionListener(e->resetPage(tabs.getSelectedIndex()));

		popup.add(item=new JMenuItem(StatisticsBasePanel.viewersReportCustomizeResetAllPages));
		item.addActionListener(e->{for (int i=0;i<tabs.getTabCount();i++) resetPage(i);});

		popup.show(button,0,button.getHeight());
	}

	/**
	 * Liefert den neuen Report-Style, wenn der Dialog per "Ok" geschlossen wurde.
	 * @return	Neuer Report-Style
	 */
	public ReportStyle getReportStyle() {
		final ReportStyle reportStyle=new ReportStyle();

		/* Seitenränder */
		reportStyle.borderTopMM=(Integer)borderTopMM.getValue();
		reportStyle.borderRightMM=(Integer)borderRightMM.getValue();
		reportStyle.borderBottomMM=(Integer)borderBottomMM.getValue();
		reportStyle.borderLeftMM=(Integer)borderLeftMM.getValue();

		/* Kopfzeile */
		reportStyle.logo=logo.getImage();
		switch (logoPosition.getSelectedIndex()) {
		case 0: reportStyle.logoPosition=ReportStyle.ReportPosition.LEFT; break;
		case 1: reportStyle.logoPosition=ReportStyle.ReportPosition.CENTER; break;
		case 2: reportStyle.logoPosition=ReportStyle.ReportPosition.RIGHT; break;
		}
		reportStyle.logoMaxWidthMM=(Integer)logoMaxWidthMM.getValue();
		reportStyle.logoMaxHeightMM=(Integer)logoMaxHeightMM.getValue();
		reportStyle.logoRepeat=logoRepeat.isSelected();

		/* Fußzeile */
		reportStyle.footerPageNumbers=footerPageNumbers.isSelected();
		reportStyle.footerDate=footerDate.isSelected();

		/* Schriftarten */
		for (int i=0;i<reportStyle.headingFont.length;i++) headingFont[i].writeToFont(reportStyle.headingFont[i]);
		textFont.writeToFont(reportStyle.textFont);
		tableHeadingFont.writeToFont(reportStyle.tableHeadingFont);
		tableTextFont.writeToFont(reportStyle.tableTextFont);
		tableTextFont.writeToFont(reportStyle.tableLastLineTextFont);
		footerFont.writeToFont(reportStyle.footerFont);
		reportStyle.parSkip=(Integer)parSkip.getValue();

		return reportStyle;
	}

	/**
	 * Editor-Panel für Schriftfamilie, Schriftgröße und fett/nicht fett-Status
	 */
	private static class FontEditor extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-8108646321315554629L;

		/**
		 * Schriftart
		 */
		private final JComboBox<String> family;

		/**
		 * Schriftgröße (in pt)
		 */
		private final SpinnerNumberModel size;

		/**
		 * Soll der Text fett dargestellt werden?
		 */
		private final JCheckBox bold;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name der Stelle, an der die einzustellende Schriftart verwendet wird
		 * @param font	Bisherige Schriftart
		 */
		public FontEditor(final String name, final ReportStyle.ReportFont font) {
			setLayout(new FlowLayout(FlowLayout.LEFT));

			JLabel label;

			add(label=new JLabel(name+":"));

			add(family=new JComboBox<>(FontFamilyComboBoxCellRenderer.fontNames));
			label.setLabelFor(family);
			switch (font.family) {
			case SANS_SERIF: family.setSelectedIndex(0); break;
			case SERIF: family.setSelectedIndex(1); break;
			case TYPEWRITER: family.setSelectedIndex(2); break;
			default: family.setSelectedIndex(0); break;
			}
			family.setRenderer(new FontFamilyComboBoxCellRenderer());
			add(label=new JLabel(StatisticsBasePanel.viewersReportCustomizeTabFontsSize+":"));

			final JSpinner spinner=new JSpinner(size=new SpinnerNumberModel(font.size,1,99,1));
			final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(spinner);
			editor.getFormat().setGroupingUsed(false);
			editor.getTextField().setColumns(2);
			spinner.setEditor(editor);
			add(spinner);
			label.setLabelFor(spinner);

			add(bold=new JCheckBox("<html><body><b>"+StatisticsBasePanel.viewersReportCustomizeTabFontsBold+"</b></body></html>",font.bold));
		}

		/**
		 * Lädt die Einstellungen aus einem Schriftarten-Objekt.
		 * @param font	Schriftarten-Objekt dessen Daten in die GUI geladen werden sollen
		 */
		public void loadFromFont(final ReportStyle.ReportFont font) {
			switch (font.family) {
			case SANS_SERIF: family.setSelectedIndex(0); break;
			case SERIF: family.setSelectedIndex(1); break;
			case TYPEWRITER: family.setSelectedIndex(2); break;
			default: family.setSelectedIndex(0); break;
			}

			size.setValue(font.size);

			bold.setSelected(font.bold);
		}

		/**
		 * Schreibt die Einstellungen in ein Schriftart-Objekt zurück.
		 * @param font	Schriftart-Objekt in das die Einstellungen aus der GUI zurückgeschrieben werden sollen
		 */
		public void writeToFont(final ReportStyle.ReportFont font) {
			switch (family.getSelectedIndex()) {
			case 0: font.family=ReportStyle.ReportFontFamily.SANS_SERIF; break;
			case 1: font.family=ReportStyle.ReportFontFamily.SERIF; break;
			case 2: font.family=ReportStyle.ReportFontFamily.TYPEWRITER; break;
			}
			font.size=Math.max(6,Math.min(72,(Integer)size.getValue()));
			font.bold=bold.isSelected();
		}
	}

	/**
	 * Renderer für die Schriftart-Combobox in {@link FontEditor}
	 * @see FontEditor
	 */
	private static class FontFamilyComboBoxCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=1864985033200932989L;

		/**
		 * Namen der Schriftarten
		 */
		public static String[] fontNames;

		/**
		 * Schriftart-Objekte zu {@link #fontNames}
		 */
		private static Font[] fonts;

		static {
			fontNames=new String[] {"SansSerif", "Serif", "Monospaced"};
			fonts=Arrays.asList(fontNames).stream().map(name->new Font(name,0,12)).toArray(Font[]::new);
		}

		/**
		 * Konstruktor der Klasse
		 */
		public FontFamilyComboBoxCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			final DefaultListCellRenderer renderer=(DefaultListCellRenderer)super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (index>=0 && index<fonts.length) {
				renderer.setFont(fonts[index]);
				renderer.setText(fontNames[index]);
			}
			return renderer;
		}
	}

	/**
	 * Anzeige-Panel für ein Bild
	 */
	private static class ImagePanel extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-8699110113662925482L;

		/**
		 * Anzuzeigendes Bild (kann <code>null</code> sein)
		 */
		private BufferedImage image;

		/**
		 * Konstruktor der Klasse
		 * @param image	Initial zu verwendendes Bild (darf <code>null</code> sein)
		 */
		public ImagePanel(final BufferedImage image) {
			setBorder(BorderFactory.createLineBorder(Color.GRAY));
			setBackground(Color.WHITE);
			final Dimension size=new Dimension(350,200);
			setSize(size);
			setPreferredSize(size);
			setMinimumSize(size);
			this.image=image;
			initDropTarget();
		}

		/**
		 * Liefert das aktuelle Bild.
		 * @return	Aktuelles Bild (kann <code>null</code> sein)
		 */
		public BufferedImage getImage() {
			return image;
		}

		/**
		 * Stellt das aktuelle Bild ein.
		 * @param image	Aktuelles Bild (darf <code>null</code> sein)
		 */
		public void setImage(BufferedImage image) {
			this.image=image;
			repaint();
		}

		/**
		 * Richtet die Komponente als Drag&amp;drop-Empfänger ein.
		 * @return	Liefert <code>true</code>, wenn die Drag&amp;drop-Einstellungen vorgenommen werden konnten
		 */
		private boolean initDropTarget() {
			final DropTarget dt=new DropTarget();
			setDropTarget(dt);
			try {
				dt.addDropTargetListener(new DropTargetAdapter() {
					@Override
					public void drop(DropTargetDropEvent dtde) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE); /* Sonst können keine Dateien abgelegt werden */
						final Transferable transfer=dtde.getTransferable();

						/* Datei(en) abgelegt */
						if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
							try {
								final Object obj=transfer.getTransferData(DataFlavor.javaFileListFlavor);
								if (obj instanceof List) for (Object entry: ((List<?>)obj)) if (entry instanceof File) {
									if (dropFile((File)entry)) break;
								}
								return;
							} catch (UnsupportedFlavorException | IOException e) {return;}
						}
					}
				});
			} catch (TooManyListenersException e1) {return false;}

			return true;
		}

		/**
		 * Reagiert auf Drag&amp;Drop einer Datei auf das Panel
		 * @param file	Datei, die übermittelt wurde
		 * @return	Gibt an, ob die Datei erfolgreich geladen werden konnte
		 */
		private boolean dropFile(final File file) {
			try {
				BufferedImage newImage=ImageIO.read(file);
				if(newImage!=null) {
					setImage(newImage);
					return true;
				}
			} catch (IOException e) {return false;}
			return false;
		}

		@Override
		public void paint(final Graphics g) {
			super.paint(g);
			if (image!=null) {
				final Dimension dest=getSize();
				dest.width-=2;
				dest.height-=2;

				final int imageW=image.getWidth();
				final int imageH=image.getHeight();

				int destX2=imageW;
				int destY2=imageH;
				if (destX2>dest.width) {
					destY2=destY2*dest.width/destX2;
					destX2=dest.width;
				}
				if (destY2>dest.height) {
					destX2=destX2*dest.height/destY2;
					destY2=dest.height;
				}
				g.drawImage(image,1,1,destX2,destY2,0,0,imageW,imageH,null);
			}
		}
	}
}
