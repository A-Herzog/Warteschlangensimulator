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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import org.oxbow.swingbits.util.swing.AncestorAdapter;

import language.Language;
import mathtools.distribution.swing.JOpenURL;
import mathtools.distribution.swing.PlugableFileChooser;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.NetHelper;
import tools.SetupData;
import ui.dialogs.WaitDialog;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
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
	public static final String HOMEPAGE="https://www.springer.com/gp/book/9783658346676";

	/**
	 * Timer zur zeitgesteuerten Prüfung, ob die angegebene pdf-Datei existiert
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
	 * Schaltfläche zum direkten Hinterladen des Buches
	 */
	private final JButton downloadBookButton;

	/**
	 * Globales Setup-Singleton
	 */
	private final SetupData setup;

	/**
	 * Buch-Metadaten
	 */
	private final BookData data;

	/**
	 * Dürfen Beispielmodelle direkt geöffnet werden?
	 */
	private final boolean allowOpenExample;

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
	 * Infopanel über {@link #examplesList}
	 */
	private final JPanel info3;

	/**
	 * Inhaltsverzeichnislistendarstellung
	 */
	private final JList<JLabel> tocList;

	/**
	 * Sachverzeichnislistendarstellung
	 */
	private final JList<JLabel> indexList;

	/**
	 * Liste der Beispielmodelle aus dem Buch
	 */
	private final JList<JLabel> examplesList;

	/**
	 * Zum Laden ausgewähltes Beispielmodell (kann <code>null</code> sein)
	 * @see #openExample()
	 * @see #getSelectedExampleModel()
	 */
	private EditModel selectedExampleModel;

	/**
	 * Liste aller verfügbaren Beispielmodelle
	 */
	private static final BookExampleModel[] exampleModels;

	static {
		final List<BookExampleModel> exampleModelsList=new ArrayList<>();
		exampleModelsList.add(new BookExampleModel("Modell-5.2.xml","5.2","Erstes Beispiel zum Ausprobieren der wesentlichen Programmfunktionen"));
		exampleModelsList.add(new BookExampleModel("Modell-7.1.xml","7.1","Auswirkungen von CV[S] auf die Kenngrößen"));
		exampleModelsList.add(new BookExampleModel("Modell-7.2a.xml","7.2","Auswirkungen der Auslastung auf die Erfolgsquote"));
		exampleModelsList.add(new BookExampleModel("Modell-7.2b.xml","7.2","Effekte duruch Warteabbrecher und Wiederholer"));
		exampleModelsList.add(new BookExampleModel("Modell-7.3.xml","7.3","Economy of scale"));
		exampleModelsList.add(new BookExampleModel("Modell-7.4a.xml","7.4","Bedienreihenfolge"));
		exampleModelsList.add(new BookExampleModel("Modell-7.4b.xml","7.4","Nicht-lineare Skalierung der Interpretation der Wartezeiten"));
		exampleModelsList.add(new BookExampleModel("Modell-7.4c.xml","7.4","Priorisierung nach Liefertermin"));
		exampleModelsList.add(new BookExampleModel("Modell-7.4d.xml","7.4","Priorisierung bestimmter Kundentypen"));
		exampleModelsList.add(new BookExampleModel("Modell-7.5.xml","7.5","System-Design"));
		exampleModelsList.add(new BookExampleModel("Modell-7.6a.xml","7.6","Pull-Produktion"));
		exampleModelsList.add(new BookExampleModel("Modell-7.6b.xml","7.6","Auswirkung der Pull-Produktion auf den Durchsatz"));
		exampleModelsList.add(new BookExampleModel("Modell-7.6c.xml","7.6","Gemeinsames Lager an zwei Stationen"));
		exampleModelsList.add(new BookExampleModel("Modell-8.1a.xml","8.1","Ungeduld bei verschiedenen Kundentypen"));
		exampleModelsList.add(new BookExampleModel("Modell-8.1b.xml","8.1","Von zwei Stationen gemeinsam genutzte Ressource"));
		exampleModelsList.add(new BookExampleModel("Modell-8.2a.xml","8.2","Verschiedene Arten der Batch-Bildung"));
		exampleModelsList.add(new BookExampleModel("Modell-8.2b.xml","8.2","Zusammenführen von Bauteilen"));
		exampleModelsList.add(new BookExampleModel("Modell-8.3.xml","8.3","Steuerung des Transportziels über Texteigenschaften"));
		exampleModelsList.add(new BookExampleModel("Modell-8.4.xml","8.4","Schichtpläne"));
		exampleModelsList.add(new BookExampleModel("Modell-8.6.xml","8.6","Bediener mit Ausfallzeiten"));
		exampleModels=exampleModelsList.toArray(BookExampleModel[]::new);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param match	Direkt beim Öffnen des Dialogs anzuzeigender Treffer (kann <code>null</code> sein)
	 * @param allowOpenExample	Dürfen Beispielmodelle direkt geöffnet werden?
	 */
	public BookDataDialog(final Component owner, final BookData.BookMatch match, final boolean allowOpenExample) {
		this(owner,match,false,allowOpenExample);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param openExamplesTab	Soll direkt nach dem Aufruf des Dialogs die Seite mit den Beispielmodellen angezeigt werden?
	 * @param allowOpenExample	Dürfen Beispielmodelle direkt geöffnet werden?
	 */
	public BookDataDialog(final Component owner, final boolean openExamplesTab, final boolean allowOpenExample) {
		this(owner,null,openExamplesTab,allowOpenExample);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param match	Direkt beim Öffnen des Dialogs anzuzeigender Treffer (kann <code>null</code> sein)
	 * @param openExamplesTab	Soll direkt nach dem Aufruf des Dialogs die Seite mit den Beispielmodellen angezeigt werden?
	 * @param allowOpenExample	Dürfen Beispielmodelle direkt geöffnet werden?
	 */
	public BookDataDialog(final Component owner, final BookData.BookMatch match, final boolean openExamplesTab, final boolean allowOpenExample) {
		super(owner,Language.tr("BookData.BookName"));

		setup=SetupData.getSetup();
		data=BookData.getInstance();
		index=data.getIndex();
		indexKeys=index.keySet().stream().sorted().collect(Collectors.toList());
		this.allowOpenExample=allowOpenExample;

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

		/* Homepage-Link */
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
		ModelElementBaseDialog.addUndoFeature(pdfEdit);
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
			final var fc=new PlugableFileChooser(initial,true);
			fc.setDialogTitle(Language.tr("BookData.BookPDF.SelectToolTip"));
			fc.addChoosableFileFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
			fc.setFileFilter("pdf");
			final File file=fc.showOpenDialogFileWithExtension(this);
			if (file==null) return;
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

		/* Buch herunterladen */
		right.add(Box.createVerticalStrut(20));
		right.add(downloadBookButton=new JButton(Language.tr("BookData.BookPDF.Download"),Images.HELP_HOMEPAGE.getIcon()));
		downloadBookButton.setToolTipText(Language.tr("BookData.BookPDF.Download.Tooltip"));
		downloadBookButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		downloadBookButton.addActionListener(e->downloadPDF());
		downloadBookButton.addAncestorListener(new AncestorAdapter() {
			@Override public void ancestorMoved(AncestorEvent event) {right.setPreferredSize(new Dimension(0,downloadBookButton.getY()+downloadBookButton.getHeight()+10));}
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

		/* Tab "Beispielmodelle" */
		tabs.addTab(Language.tr("BookData.Tab.Examples"),tab=new JPanel(new BorderLayout()));
		tab.add(info3=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		info3.add(new JLabel(Language.tr("BookData.Tab.Examples.ClickInfo")));
		tab.add(new JScrollPane(examplesList=new JList<>(getExampleLabels())),BorderLayout.CENTER);
		examplesList.setCellRenderer(new JLabelRender(Images.MODEL));
		examplesList.setPrototypeCellValue(defaultLabel);
		examplesList.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) openExample();}
		});
		examplesList.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) openExample();}
		});
		info3.setVisible(allowOpenExample);

		/* Icons auf den Tabs */
		tabs.setIconAt(0,Images.HELP_BOOK.getIcon());
		tabs.setIconAt(1,Images.HELP_BOOK_CONTENT.getIcon());
		tabs.setIconAt(2,Images.HELP_BOOK_INDEX.getIcon());
		tabs.setIconAt(3,Images.MODEL.getIcon());

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

		/* Direkte Anzeige der Beispiele */
		if (openExamplesTab) {
			tabs.setSelectedIndex(3);
		}

		/* pdf-Datei timer-gestützt prüfen */
		timer=new Timer(1_000,e->pdfEditUpdate());
		timer.setRepeats(true);
		timer.start();

		/* Dialog starten */
		pdfEditUpdate();
		setResizable(true);
		setMinSizeRespectingScreensize(1024,800);
		setSizeRespectingScreensize(1024,800);
		setLocationRelativeTo(getOwner());
		WindowSizeStorage.window(this,"book");
		setVisible(true);
	}

	@Override
	public void setVisible(boolean b) {
		if (!b) timer.stop();
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
		downloadBookButton.setEnabled(!bookAvailable);
		info1.setVisible(bookAvailable);
		info2.setVisible(bookAvailable);
	}

	/**
	 * Öffnet eine Webseite
	 * @param url	URL der aufzurufenden Webseite
	 */
	private void openWebpage(final String url) {
		JOpenURL.open(this,url);
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
	 * Erstellt die Einträge für die Beispielmodellliste
	 * @return	Einträge für die Beispielmodellliste
	 * @see #examplesList
	 */
	private JLabel[] getExampleLabels() {
		return Stream.of(exampleModels).
				map(exampleModel->new JLabel(exampleModel.getDescription())).
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
	 * 32-Bit Variante des PDF-Anzeigeprogramm das ein direktes Aufrufen einer bestimmten Seite ermöglicht.
	 * @see #canOpenPDFpage()
	 * @see #openPDF(File, int)
	 */
	final File pdfViewer32=new File("C:\\Program Files (x86)\\Adobe\\Acrobat Reader DC\\Reader\\AcroRd32.exe");

	/**
	 * 64-Bit Variante des PDF-Anzeigeprogramm das ein direktes Aufrufen einer bestimmten Seite ermöglicht.
	 * @see #canOpenPDFpage()
	 * @see #openPDF(File, int)
	 */
	final File pdfViewer64=new File("C:\\Program Files\\Adobe\\Acrobat DC\\Acrobat\\Acrobat.exe");

	/**
	 * Ist es möglich, eine pdf direkt auf einer bestimmten Seite zu öffnen?
	 * @return	Liefert <code>true</code>, wenn eine pdf direkt auf einer bestimmten Seite geöffnet werden kann.
	 * @see #pdfViewer32
	 * @see #pdfViewer64
	 * @see #openPDF(File, int)
	 */
	private boolean canOpenPDFpage() {
		return pdfViewer64.isFile() || pdfViewer32.isFile();
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

		final List<String> cmd=new ArrayList<>();
		if (pdfViewer64.isFile()) {
			cmd.add(pdfViewer64.toString());
		} else {
			if (pdfViewer32.isFile()) cmd.add(pdfViewer32.toString());
		}
		cmd.add("/A");
		cmd.add("page="+(page+data.getPageOffset(page)));
		cmd.add(file.toString());

		try {
			Runtime.getRuntime().exec(cmd.toArray(String[]::new));
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
	 * SpringerLink-URL zu dem Lehrbuch<br>
	 * (Kann nur aufgerufen werden, wenn die aktuelle Nutzer-IP von SpringerLink als berechtigt
	 * angesehen wird. Andernfalls wird nur eine html-Datei geliefert.)
	 * @see #downloadPDF()
	 */
	private static final String bookURL="https://link.springer.com/content/pdf/10.1007%2F978-3-658-34668-3.pdf";

	/**
	 * Standardmäßiger Dateiname für das Buch
	 * @see #downloadPDF()
	 */
	private static final String bookDefaultFileName="Herzog2021_Book_SimulationMitDemWarteschlangen.pdf";

	/**
	 * Versucht das Buch von SpringerLink herunterzuladen und es lokal zu speichern.
	 */
	private void downloadPDF() {
		/* Buch schon vorhanden? */
		final File outputFile=new File(SetupData.getSetupFolder(),bookDefaultFileName);
		if (outputFile.isFile()) {
			setBookFile(outputFile);
			return;
		}

		/* Datei herunterladen */
		byte[] data=null;
		try {
			final URI uri=new URI(bookURL);
			data=WaitDialog.workBytes(this,()->NetHelper.loadBinary(uri,null,false,true),WaitDialog.Mode.DOWNLOAD_FILE); /* onlySecuredURLs=false ist leider nötig */
		} catch (URISyntaxException e1) {
			data=null;
		}

		if (data==null || data.length==0) {
			MsgBox.error(this,Language.tr("BookData.BookPDF.Download.ErrorTitle"),Language.tr("BookData.BookPDF.Download.ErrorInfo"));
			return;
		}

		/* Prüfen, ob wir eine pdf erhalten haben */
		if (data.length<5 || data[0]!='%' || data[1]!='P' || data[2]!='D' || data[3]!='F' || data[4]!='-') {
			MsgBox.error(this,Language.tr("BookData.BookPDF.Download.ErrorTitle"),Language.tr("BookData.BookPDF.Download.ErrorInfoLink"));
			return;
		}

		/* Speichern */
		try (FileOutputStream output=new FileOutputStream(outputFile)) {
			output.write(data);
		} catch (IOException e) {
			MsgBox.error(this,Language.tr("BookData.BookPDF.Download.ErrorSaveTitle"),String.format(Language.tr("BookData.BookPDF.Download.ErrorSaveInfo"),outputFile.toString()));
			return;
		}

		/* Eintragen */
		setBookFile(outputFile);
	}

	/**
	 * Stellt einen neuen Pfad zur Buch-pdf ein.
	 * @param file	Buch-pdf Pfad
	 * @see #downloadPDF()
	 */
	private void setBookFile(final File file) {
		pdfEdit.setText(file.toString());
		pdfEditUpdate();
	}

	/**
	 * Öffnet die Buch-pdf auf der Seite gemäß der Auswahl auf der Inhaltsverzeichnisliste
	 * @see #tocList
	 */
	private void openPDFtoc() {
		final int selected=tocList.getSelectedIndex();
		if (selected<0) return;

		if (!getBookPDF().isFile()) {
			JOpenURL.open(this,data.getTOC().get(selected).getChapterURL());
			/* MsgBox.error(this,Language.tr("BookData.NoBook.Title"),Language.tr("BookData.NoBook.Info")); */
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

		final int pageToShow;

		if (pages.size()==1 || (!canOpenPDFpage() && getBookPDF().isFile())) {
			pageToShow=pages.get(0);
		} else {
			final BookDataSelectPageDialog dialog=new BookDataSelectPageDialog(this,key,pages.stream().mapToInt(Integer::intValue).toArray());
			if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
			pageToShow=dialog.getSelectedPage();
		}


		if (!getBookPDF().isFile()) {
			JOpenURL.open(this,data.getSection(pageToShow).getChapterURL());
			/* MsgBox.error(this,Language.tr("BookData.NoBook.Title"),Language.tr("BookData.NoBook.Info")); */
			return;
		}

		openPDF(getBookPDF(),pageToShow);
	}

	/**
	 * Lädt das gewählte Beispielmodell
	 * @see #examplesList
	 * @see #getSelectedExampleModel()
	 */
	private void openExample() {
		final int selected=examplesList.getSelectedIndex();
		if (selected<0) return;
		if (!allowOpenExample) {
			MsgBox.error(this,Language.tr("BookData.BookExample.ErrorTitle"),Language.tr("BookData.BookExample.ErrorInfo"));
			return;
		}
		selectedExampleModel=exampleModels[selected].getModel(this);
		if (selectedExampleModel!=null) close(BaseDialog.CLOSED_BY_OK);
	}

	/**
	 * Liefert das zum Laden ausgewählte Beispielmodell.
	 * @return	Zum Laden ausgewählte Beispielmodell (kann <code>null</code> sein)
	 */
	public EditModel getSelectedExampleModel() {
		return selectedExampleModel;
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

	/**
	 * Eintrag für ein Beispielmodell
	 * @see BookDataDialog#exampleModels
	 */
	private static class BookExampleModel {
		/**
		 * Dateiname (inkl. Erweiterung, ohne Pfad)
		 */
		private final String fileName;

		/**
		 * Kapitel in dem das Beispiel vorkommt
		 */
		private final String chapter;

		/**
		 * Name des Beispielmodells
		 */
		private final String name;

		/**
		 * Konstruktor der Klasse
		 * @param fileName	Dateiname (inkl. Erweiterung, ohne Pfad)
		 * @param chapter	Kapitel in dem das Beispiel vorkommt
		 * @param name	Name des Beispielmodells
		 */
		public BookExampleModel(final String fileName, final String chapter, final String name) {
			this.chapter=chapter;
			this.fileName=fileName;
			this.name=name;
		}

		/**
		 * Liefert eine html-Beschreibung für das Beispiel
		 * @return	html-Beschreibung für das Beispiel
		 * @see BookDataDialog#examplesList
		 */
		public String getDescription() {
			return String.format("<html><body>Kapitel %s: <b>%s</b></body></html>",chapter,name);
		}

		/**
		 * Liefert das Beispielmodell als Objekt
		 * @param owner	Übergeordnetes Element (zur Anzeige von Fehlermeldungen; darf <code>null</code> sein)
		 * @return	Beispielmodell als Objekt
		 */
		public EditModel getModel(final Component owner) {
			final EditModel editModel=new EditModel();
			try (InputStream in=BookDataDialog.class.getResourceAsStream("bookexamples/"+fileName)) {
				final String error=editModel.loadFromStream(in);
				if (error!=null) {
					if (owner==null) {
						System.out.println(error);
					} else {
						MsgBox.error(owner,Language.tr("XML.LoadErrorTitle"),error);
					}
					return null;
				}
				return editModel;
			} catch (IOException e) {
				return null;
			}
		}
	}
}