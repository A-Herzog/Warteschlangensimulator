/**
 * Copyright 2021 Alexander Herzog
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
package ui.help;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.AncestorEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.oxbow.swingbits.util.swing.AncestorAdapter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;
import ui.tools.WindowSizeStorage;

/**
 * Zeigt einen Dialog mit Inhaltsverzeichnis und Sachverzeichnis
 * des Buches zum Simulator an.
 * @author Alexander Herzog
 * @see BookData
 */
public class BookDataDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-1616541886150507523L;

	/**
	 * Adresse von der das Buch bezogen werden kann
	 */
	public static final String HOMEPAGE="https://Springer-URL";

	/**
	 * Timer zur zeitgesteuerten Prüfung, ob die angegeben pdf-Datei existiert
	 */
	private final Timer timer;

	/**
	 * Eingabefeld für den pdf-Dateinamen zu dem Buch
	 */
	private final JTextField pdfEdit;

	/**
	 * Schaltfläche zum direkten Aufrufen des Buches
	 */
	private final JButton openBookButton;

	/**
	 * Globales Setup-Singletoon
	 */
	private final SetupData setup;

	/**
	 * Buch-Metadaten
	 */
	private final BookData data;

	/**
	 * Sachverzeichnis
	 * @see #indexKeys
	 * @see #indexList
	 */
	private final Map<String,List<Integer>> index;

	/**
	 * Namen aus dem Sachverzeichnis in der Sortierung,
	 * in der sie auch in {@link #indexList} erscheinen.
	 * @see #indexList
	 */
	private final List<String> indexKeys;

	/**
	 * Rahmen für die Einträge in den Inhtaltsverzeichnis-
	 * und Sachverzeichnislisten.
	 * @see #tocList
	 * @see #indexList
	 */
	private final Border labelBorder;

	/**
	 * Infopanel über {@link #tocList}
	 */
	private final JPanel info1;

	/**
	 * Infopanel über {@link #indexList}
	 */
	private final JPanel info2;

	/**
	 * Inhaltsverzeichnislistendarstellung
	 */
	private final JList<JLabel> tocList;

	/**
	 * Sachverzeichnislistendarstellung
	 */
	private final JList<JLabel> indexList;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param match	Direkt beim Öffnen des Dialogs anzuzeigender Treffer (kann <code>null</code> sein)
	 */
	public BookDataDialog(final Component owner, BookData.BookMatch match) {
		super(owner,Language.tr("BookData.BookName"));

		setup=SetupData.getSetup();
		data=BookData.getInstance();
		index=data.getIndex();
		indexKeys=index.keySet().stream().sorted().collect(Collectors.toList());

		labelBorder=BorderFactory.createEmptyBorder(5,10,5,10);

		/* Beispiellabel zur Größenberechnung */
		final JLabel defaultLabel=new JLabel("Text");
		defaultLabel.setBorder(labelBorder);

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs);

		JPanel tab;
		JPanel line;
		JButton button;

		/* Tab "" */
		tabs.addTab(Language.tr("BookData.Tab.Book"),tab=new JPanel(new BorderLayout()));

		/* Links: Cover */
		final URL coverURL=BookDataDialog.class.getResource("bookinfo/Cover.png");
		if (coverURL!=null) {
			final JLabel image=new JLabel(new ImageIcon(coverURL));
			image.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			final JPanel left=new JPanel();
			tab.add(left,BorderLayout.WEST);
			left.setLayout(new BoxLayout(left,BoxLayout.PAGE_AXIS));
			left.add(image);
			left.add(Box.createVerticalGlue());
		}

		/* Rechts: Daten */
		final JPanel right=new JPanel();
		final JScrollPane scroll=new JScrollPane(right);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tab.add(scroll,BorderLayout.CENTER);
		right.setLayout(new BoxLayout(right,BoxLayout.PAGE_AXIS));

		/* Infotext */
		final StringBuilder text=new StringBuilder();
		text.append("<html><body>");
		text.append("<h2>"+Language.tr("BookData.BookName")+"</h2>");
		text.append("<p style=\"font-size: 110%;\">");
		text.append(BookData.getContentInfo());
		text.append("</p>");
		text.append("<p style=\"margin-top: 10px; font-size: 110%;\">");
		text.append(BookData.getAvailableInfo());
		text.append("</p>");
		text.append("<p style=\"margin-top: 10px; font-size: 110%;\">");
		text.append(Language.tr("BookData.AccessInfo"));
		text.append("</p>");
		text.append("</body></html>");
		right.add(new JLabel(text.toString()));

		/* Download-Link */
		right.add(new JLabel("<html><body><p style=\"font-size: 110%; margin-top: 20px;\"><b>"+Language.tr("BookData.Homepage")+":</b></p></body></html>"));
		final JLabel link=new JLabel("<html><body><p style=\"color: blue; text-decoration: underline; font-size: 110%; margin-top: 10px;\">"+HOMEPAGE+"</p></body></html>");
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (SwingUtilities.isLeftMouseButton(e)) openWebpage(HOMEPAGE);}
		});
		right.add(link);

		/* pdf-Eingabezeile */
		right.add(new JLabel("<html><body><p style=\"font-size: 110%; margin-top: 20px; margin-bottom: 10px;\"><b>"+Language.tr("BookData.BookPDF")+":</b></p></body></html>"));
		right.add(line=new JPanel(new BorderLayout()));
		line.add(pdfEdit=new JTextField(50),BorderLayout.CENTER);
		pdfEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {pdfEditUpdate();}
			@Override public void keyReleased(KeyEvent e) {pdfEditUpdate();}
			@Override public void keyPressed(KeyEvent e) {pdfEditUpdate();}
		});
		pdfEdit.setText(setup.eBook);
		line.setMaximumSize(new Dimension(10000,25));
		line.setAlignmentX(Component.LEFT_ALIGNMENT);
		line.add(button=new JButton(Images.GENERAL_SELECT_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("BookData.BookPDF.SelectToolTip"));
		button.addActionListener(e->{
			final String initial=new File(pdfEdit.getText().trim()).getParent();
			final JFileChooser fc;
			if (initial!=null) fc=new JFileChooser(initial); else {
				fc=new JFileChooser();
				CommonVariables.initialDirectoryToJFileChooser(fc);
			}
			fc.setDialogTitle(Language.tr("BookData.BookPDF.SelectToolTip"));
			final FileFilter pdf=new FileNameExtensionFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
			fc.addChoosableFileFilter(pdf);

			fc.setFileFilter(pdf);
			if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			File file=fc.getSelectedFile();
			if (file.getName().indexOf('.')<0) {
				if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			}
			pdfEdit.setText(file.toString());
			pdfEditUpdate();
		});
		new FileDropper(pdfEdit,e->{
			final FileDropperData data=(FileDropperData)e.getSource();
			pdfEdit.setText(data.getFile().toString());
			pdfEditUpdate();
			data.dragDropConsumed();
		});

		/* Buch direkt öffnen */
		right.add(Box.createVerticalStrut(20));
		right.add(openBookButton=new JButton(Language.tr("BookData.BookPDF.Open"),Images.HELP_BOOK.getIcon()));
		openBookButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		openBookButton.addActionListener(e->openPDF(getBookPDF()));
		openBookButton.addAncestorListener(new AncestorAdapter() {
			@Override public void ancestorMoved(AncestorEvent event) {right.setPreferredSize(new Dimension(0,openBookButton.getY()+openBookButton.getHeight()+10));}
		});

		right.add(Box.createVerticalGlue());

		/* Tab "Inhaltsverzeichnis" */
		tabs.addTab(Language.tr("BookData.Tab.TOC"),tab=new JPanel(new BorderLayout()));
		tab.add(info1=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		info1.add(new JLabel(Language.tr("BookData.Tab.TOC.ClickInfo")));
		tab.add(new JScrollPane(tocList=new JList<>(getTOCLabels())),BorderLayout.CENTER);
		tocList.setCellRenderer(new JLabelRender(Images.HELP_BOOK_CONTENT));
		tocList.setPrototypeCellValue(defaultLabel);
		tocList.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) openPDFtoc();}
		});
		tocList.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) openPDFtoc();}
		});

		/* Tab "Sachverzeichnis" */
		tabs.addTab(Language.tr("BookData.Tab.Index"),tab=new JPanel(new BorderLayout()));
		tab.add(info2=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		info2.add(new JLabel(Language.tr("BookData.Tab.Index.ClickInfo")));
		tab.add(new JScrollPane(indexList=new JList<>(getIndexLabels())),BorderLayout.CENTER);
		indexList.setCellRenderer(new JLabelRender(Images.HELP_BOOK_INDEX));
		indexList.setPrototypeCellValue(defaultLabel);
		indexList.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) openPDFindex();}
		});
		indexList.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) openPDFindex();}
		});

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.HELP_BOOK.getIcon());
		tabs.setIconAt(1,Images.HELP_BOOK_CONTENT.getIcon());
		tabs.setIconAt(2,Images.HELP_BOOK_INDEX.getIcon());

		/* Treffer auswählen */
		if (match instanceof BookData.BookSection) {
			tabs.setSelectedIndex(1);
			final int i=data.getTOC().indexOf(match);
			if (i>=0) {
				tocList.setSelectedIndex(i);
				tocList.ensureIndexIsVisible(i);
				SwingUtilities.invokeLater(()->tocList.requestFocus());
			}
		}
		if (match instanceof BookData.IndexMatch) {
			tabs.setSelectedIndex(2);
			final int i=indexKeys.indexOf(((BookData.IndexMatch)match).name);
			if (i>=0) {
				indexList.setSelectedIndex(i);
				indexList.ensureIndexIsVisible(i);
				SwingUtilities.invokeLater(()->indexList.requestFocus());
			}
		}

		/* pdf-Datei timer-gestützt prüfen */
		timer=new Timer(1_000,e->pdfEditUpdate());
		timer.setRepeats(true);
		timer.start();

		/* Dialog starten */
		pdfEditUpdate();
		setResizable(true);
		setMinSizeRespectingScreensize(850,650);
		setSizeRespectingScreensize(850,650);
		setLocationRelativeTo(getOwner());
		WindowSizeStorage.window(this,"book");
		setVisible(true);
	}

	@Override
	public void setVisible(boolean b) {
		if (b==false) timer.stop();
		super.setVisible(b);
	}

	/**
	 * Aktualisiert die GUI nachdem die Eingabe in {@link #pdfEdit} geändert wurde.
	 * @see #pdfEdit
	 */
	private void pdfEditUpdate() {
		if (!pdfEdit.getText().equals(setup.eBook)) {
			setup.eBook=pdfEdit.getText();
			setup.saveSetup();
		}

		final boolean bookAvailable=getBookPDF().isFile();
		openBookButton.setEnabled(bookAvailable);
		info1.setVisible(bookAvailable);
		info2.setVisible(bookAvailable);
	}

	/**
	 * Öffnet eine Webseite
	 * @param url	URL der aufzurufenden Webseite
	 */
	private void openWebpage(final String url) {
		try {
			if (!MsgBox.confirmOpenURL(this,url)) return;
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException | URISyntaxException e1) {
			MsgBox.error(this,Language.tr("Window.Info.NoInternetConnection"),String.format(Language.tr("Window.Info.NoInternetConnection.Address"),url));
		}
	}

	/**
	 * Erstellt die Einträge für das Inhaltsverzeichnis
	 * @return	Einträge für das Inhaltsverzeichnis
	 * @see #tocList
	 */
	private JLabel[] getTOCLabels() {
		return data.getTOC().stream().
				map(section->new JLabel(section.toString())).
				map(label->{label.setBorder(labelBorder); return label;}).
				toArray(JLabel[]::new);
	}

	/**
	 * Erstellt die Einträge für das Sachverzeichnis
	 * @return	Einträge für das Sachverzeichnis
	 * @see #indexList
	 */
	private JLabel[] getIndexLabels() {
		return indexKeys.stream().
				map(name->new JLabel("<html><body><b>"+name+"</b> ("+Language.tr("BookData.page")+" "+String.join(", ",index.get(name).stream().map(I->I.toString()).toArray(String[]::new))+")</body></html>")).
				map(label->{label.setBorder(labelBorder); return label;}).
				toArray(JLabel[]::new);
	}

	/**
	 * Liefert die Buch-pdf-Datei.
	 * @return	Buch-pdf-Datei (kann <code>null</code> sein, wenn das Buch nicht zur Verfügung steht)
	 */
	private File getBookPDF() {
		return new File(pdfEdit.getText().trim());
	}

	/**
	 * PDF-Anzeigeprogramm das ein direktes Aufrufen einer bestimmten Seite ermöglicht.
	 * @see #canOpenPDFpage()
	 * @see #openPDF(File, int)
	 */
	final File pdfViewer=new File("C:\\Program Files (x86)\\Adobe\\Acrobat Reader DC\\Reader\\AcroRd32.exe");

	/**
	 * Ist es möglich, eine pdf direkt auf einer bestimmten Seite zu öffnen?
	 * @return	Liefert <code>true</code>, wenn eine pdf direkt auf einer bestimmten Seite geöffnet werden kann.
	 * @see #pdfViewer
	 * @see #openPDF(File, int)
	 */
	private boolean canOpenPDFpage() {
		return pdfViewer.isFile();
	}

	/**
	 * Versucht eine pdf auf einer bestimmten Seite zu öffnen.
	 * @param file	Zu öffnende pdf
	 * @param page	Initial anzuzeigende Seite
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean openPDF(final File file, final int page) {
		if (file==null || !file.isFile()) return false;

		if (page<=0 || !canOpenPDFpage()) return openPDF(file);

		final StringBuilder cmd=new StringBuilder();
		cmd.append("\"");
		cmd.append(pdfViewer.toString());
		cmd.append("\" /A \"page=");
		cmd.append(page+data.getPageOffset());
		cmd.append("\" \"");
		cmd.append(file.toString());
		cmd.append("\"");
		try {
			Runtime.getRuntime().exec(cmd.toString());
			return true;
		} catch (IOException e) {}

		return openPDF(file);
	}

	/**
	 * Versucht eine pdf zu öffnen.
	 * @param file	Zu öffnende pdf
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean openPDF(final File file) {
		if (file==null || !file.isFile()) return false;
		try {
			Desktop.getDesktop().open(file);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Öffnet die Buch-pdf auf der Seite gemäß der Auswahl auf der Inhaltsverzeichnisliste
	 * @see #tocList
	 */
	private void openPDFtoc() {
		final int selected=tocList.getSelectedIndex();
		if (selected<0) return;

		if (!getBookPDF().isFile()) {
			MsgBox.error(this,Language.tr("BookData.NoBook.Title"),Language.tr("BookData.NoBook.Info"));
			return;
		}

		final int page=data.getTOC().get(selected).page;
		openPDF(getBookPDF(),page);
	}

	/**
	 * Öffnet die Buch-pdf auf der Seite gemäß der Auswahl auf der Sachverzeichnisliste
	 * @see #indexList
	 */
	private void openPDFindex() {
		final int selected=indexList.getSelectedIndex();
		if (selected<0) return;
		final String key=indexKeys.get(selected);
		final List<Integer> pages=index.get(key);
		if (pages==null || pages.size()==0) return;

		if (!getBookPDF().isFile()) {
			MsgBox.error(this,Language.tr("BookData.NoBook.Title"),Language.tr("BookData.NoBook.Info"));
			return;
		}

		if (pages.size()==1 || !canOpenPDFpage()) {
			openPDF(getBookPDF(),pages.get(0));
			return;
		}

		final BookDataSelectPageDialog dialog=new BookDataSelectPageDialog(this,key,pages.stream().mapToInt(Integer::intValue).toArray());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			openPDF(getBookPDF(),dialog.getSelectedPage());
		}
	}

	/**
	 * Renderer für die Einträge den Listen
	 */
	private static class JLabelRender implements ListCellRenderer<JLabel> {
		/**
		 * Vor dem Eintrag anzuzeigendes Icon
		 */
		private final Icon icon;

		/**
		 * Konstruktor der Klasse
		 * @param image	Vor dem Eintrag anzuzeigendes Icon
		 */
		public JLabelRender(final Images image) {
			icon=image.getIcon();
		}
		@Override
		public Component getListCellRendererComponent(JList<? extends JLabel> list, JLabel value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				value.setBackground(list.getSelectionBackground());
				value.setForeground(list.getSelectionForeground());
				value.setOpaque(true);
			} else {
				value.setBackground(list.getBackground());
				value.setForeground(list.getForeground());
				value.setOpaque(false);
			}
			value.setIcon(icon);
			return value;
		}
	}
}