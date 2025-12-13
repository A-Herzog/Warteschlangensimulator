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
package systemtools.statistics;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument.LeafElement;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.text.Paragraph;

import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import mathtools.distribution.swing.PlugableFileChooser;
import systemtools.BaseDialog;
import systemtools.GUITools;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;

/**
 * Diese Klasse stellt Implementierung des {@link StatisticViewer}-Interfaces zur
 * Anzeige von Text dar.
 * @author Alexander Herzog
 * @version 2.5
 */
public abstract class StatisticViewerText implements StatisticViewer {
	/**
	 * Ausgabe-Text-Panel
	 * @see #initTextPane()
	 */
	private JTextPane textPane=null;

	/**
	 * Navigationsbaumstruktur
	 * @see #getViewer(boolean)
	 * @see #getSelectedNavLine()
	 */
	private JTree tree;

	/**
	 * Scroll-Komponente um die Baumstruktur {@link #tree}
	 */
	private JScrollPane treeScroller;

	/**
	 * Splitter zwischen Navigationsstruktur links
	 * und Text auf der rechten Seite
	 */
	private JSplitPane split;

	/**
	 * Standardbreite des Trenners in {@link #split}
	 */
	private int splitDividerSize;

	/**
	 * Auszugebende Zeilen
	 */
	private final List<String> lines;

	/**
	 * Linkziele oder Hinweise zu den Zeilen
	 * @see #lines
	 */
	private final List<String> hints;

	/**
	 * Bedeutung der jeweiligen Zeile:
	 * 0=Text, 1,2,3,...=Überschriften, -1=Absatzbeginn, -2=Absatzende, -3=Link(klein), -4=Link
	 * @see #lines
	 */
	private final List<Integer> lineTypes;

	/**
	 * Stärke der Einrückung der jeweiligen Zeile
	 * @see #lines
	 */
	private final List<Integer> indentLevel;

	/**
	 * html-Seite mit einer zusätzlichen Erklärung zu dieser Statistikseite
	 * @see #addDescription(URL, Consumer)
	 */
	private URL descriptionURL=null;

	/**
	 * Handler, der Themennamen (angegeben über "help:..."-Links) zum Aufruf normaler Hilfeseiten entgegen nimmt
	 * @see #addDescription(URL, Consumer)
	 */
	private Consumer<String> descriptionHelpCallback=null;

	/**
	 * Darstellung der Hilfe-Seite {@link #descriptionURL}
	 * @see #initDescriptionPane()
	 * @see #addDescription(URL, Consumer)
	 */
	private DescriptionViewer descriptionPane=null;

	/**
	 * Erfolgt die Darstellung im Dark-Modus?
	 */
	private final boolean isDark;

	/**
	 * Suchbegriff beim letzten Aufruf der Suchfunktion
	 * @see #search(Component)
	 */
	private String lastSearchString;

	/**
	 * Maximalanzahl an zurück zu liefernden Suchtreffern
	 * @see #search(Component)
	 * @see #searchInElement(Element, String, boolean, List)
	 * @see #searchInElementRegEx(Element, Pattern, List)
	 */
	private static final int MAX_SEARCH_HITS=1_000;

	/**
	 * Konstruktor der Klasse
	 */
	public StatisticViewerText() {
		lines=new ArrayList<>();
		hints=new ArrayList<>();
		lineTypes=new ArrayList<>();
		indentLevel=new ArrayList<>();

		final Color textBackground=UIManager.getColor("TextField.background");
		isDark=(textBackground!=null && !textBackground.equals(Color.WHITE));
	}

	/**
	 * Löscht alle bisherigen Ausgaben
	 * @see #saveHtml(BufferedWriter, File, int, boolean)
	 * @see #saveLaTeX(BufferedWriter, File, int)
	 */
	private void reset() {
		lines.clear();
		hints.clear();
		lineTypes.clear();
		indentLevel.clear();
	}

	/**
	 * Wurden bereits Ausgabezeilen erzeugt?
	 * @return	Liefert <code>true</code>, wenn noch keinerlei Ausgaben angelegt wurden
	 */
	protected final boolean isEmpty() {
		return lines.size()==0;
	}

	@Override
	public ViewerType getType() {
		return ViewerType.TYPE_TEXT;
	}

	@Override
	public ViewerImageType getImageType() {
		return ViewerImageType.IMAGE_TYPE_NOIMAGE;
	}

	@Override
	public boolean getCanDo(final CanDoAction canDoType) {
		switch (canDoType) {
		case CAN_DO_UNZOOM: return false;
		case CAN_DO_COPY: return true;
		case CAN_DO_PRINT: return true;
		case CAN_DO_SAVE: return true;
		case CAN_DO_SEARCH: return true;
		case CAN_DO_NAVIGATION: return true;
		default: return false;
		}
	}

	/**
	 * Überträgt den Text aus {@link #lines} usw. in {@link #textPane}.
	 * @see #lines
	 * @see #hints
	 * @see #lineTypes
	 * @see #indentLevel
	 * @see #textPane
	 */
	private void initTextPane() {
		if (textPane!=null) return;

		textPane=new JTextPane();
		textPane.setEditable(false);
		if (!isDark) textPane.setBackground(Color.WHITE);
		textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,Boolean.TRUE);

		/* Styles zusammenstellen */

		final Style oldDefaultStyle=textPane.getStyle("default");
		final String defaultFontFamily=StyleConstants.getFontFamily(oldDefaultStyle);

		final FastDefaultStyledDocument doc=new FastDefaultStyledDocument(); /* Neues Dokument erstellen; bisheriges in textPane löst bei jeder Texteinfügung ein Event aus */

		final Style defaultStyle=StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(defaultStyle,defaultFontFamily);
		Style style;

		style=doc.addStyle("default",defaultStyle);
		StyleConstants.setFontSize(style,(int)Math.round((StyleConstants.getFontSize(style)+1)*GUITools.getScaleFactor()));
		if (isDark) StyleConstants.setForeground(style,Color.LIGHT_GRAY);

		style=doc.addStyle("h1",defaultStyle);
		StyleConstants.setFontSize(style,(int)Math.round((StyleConstants.getFontSize(style)+4)*GUITools.getScaleFactor()));
		StyleConstants.setBold(style,true);
		if (isDark) StyleConstants.setForeground(style,Color.LIGHT_GRAY);

		style=doc.addStyle("h2",defaultStyle);
		StyleConstants.setBold(style,true);
		StyleConstants.setFontSize(style,(int)Math.round((StyleConstants.getFontSize(style)+2)*GUITools.getScaleFactor()));
		if (isDark) StyleConstants.setForeground(style,Color.LIGHT_GRAY);

		style=doc.addStyle("h3",defaultStyle);
		StyleConstants.setFontSize(style,(int)Math.round((StyleConstants.getFontSize(style)+1)*GUITools.getScaleFactor()));
		StyleConstants.setUnderline(style,true);
		if (isDark) StyleConstants.setForeground(style,Color.LIGHT_GRAY);

		style=doc.addStyle("linkSmall",defaultStyle);
		StyleConstants.setFontSize(style,(int)Math.round((StyleConstants.getFontSize(style)-1)*GUITools.getScaleFactor()));
		if (isDark) StyleConstants.setForeground(style,new Color(128,128,225)); else StyleConstants.setForeground(style,Color.BLUE);

		style=doc.addStyle("linkBig",defaultStyle);
		StyleConstants.setFontSize(style,(int)Math.round((StyleConstants.getFontSize(style)+1)*GUITools.getScaleFactor()));
		if (isDark) StyleConstants.setForeground(style,new Color(128,128,225)); else StyleConstants.setForeground(style,Color.BLUE);

		/* Text einfügen */

		final int size=lines.size();
		final Style defaultStyle2=doc.getStyle("default");
		for (int i=0;i<size;i++) {
			final String line=lines.get(i);
			final String hint=hints.get(i);
			final int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				doc.addText(defaultStyle2,"\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (hint!=null && !hint.isBlank()) {
					final SimpleAttributeSet attrSet=new SimpleAttributeSet(defaultStyle2);
					attrSet.addAttribute("Hint",hint);
					doc.addText(attrSet,line+"\n");
				} else {
					doc.addText(defaultStyle2,line+"\n");
				}
				continue;
			}
			if (type==-3) {
				/* Link(klein) */
				final SimpleAttributeSet attrSet=new SimpleAttributeSet(doc.getStyle("linkSmall"));
				if (hint!=null && !hint.isBlank()) attrSet.addAttribute("URL",hint);
				doc.addText(attrSet,line+"\n");
				continue;
			}
			if (type==-4) {
				/* Link */
				final SimpleAttributeSet attrSet=new SimpleAttributeSet(doc.getStyle("linkBig"));
				if (hint!=null && !hint.isBlank()) attrSet.addAttribute("URL",hint);
				doc.addText(attrSet,line+"\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (i>0 && lineTypes.get(i-1)!=-2) doc.addText(defaultStyle2,"\n");
				doc.addText(doc.getStyle("h"+type),line+"\n");
				continue;
			}
		}

		doc.finalizeText();

		textPane.setStyledDocument(doc); /* Neues Dokument setzen */

		textPane.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(java.awt.event.MouseEvent e) {
				final int cursor=(pointToLink(e.getPoint())==null)?Cursor.DEFAULT_CURSOR:Cursor.HAND_CURSOR;
				textPane.setCursor(new Cursor(cursor));
			}
		});
		textPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					final String hint=pointToHint(e.getPoint());
					final StatisticsBasePanel owner=getParentStatisticPanel(textPane);
					if (owner==null) return;
					final JPopupMenu menu=processContextClick(owner,hint);
					if (menu!=null) menu.show(e.getComponent(),e.getX(),e.getY());
				}
				if (SwingUtilities.isLeftMouseButton(e)) {
					final String url=pointToLink(e.getPoint());
					if (url!=null) processLinkClick(url);
				}
			}
		});

		textPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isControlDown() && !e.isAltDown() && !e.isShiftDown() && e.getKeyCode()==KeyEvent.VK_F) {
					search(SwingUtilities.getWindowAncestor(textPane));
					e.consume();
				}
			}
		});
	}

	/**
	 * Wurden in {@link #addLine(String, String)} oder {@link #addLine(int, String, String)} für bestimmte
	 * Zeilen Zusatztexte übergeben, so kann auf dieser Basis hier für diese ein Popup-Menü geöffnet werden.
	 * @param owner	Übergeordnetes Element
	 * @param hint	Zusätzlicher Text
	 * @return	Zu öffnendes Kontextmenü oder <code>null</code>, wenn für diesen Text kein Kontextmenü geöffnet werden soll
	 * @see #addLine(String, String)
	 * @see #addLine(int, String, String)
	 */
	protected JPopupMenu processContextClick(final StatisticsBasePanel owner, final String hint) {
		final JPopupMenu popup=new JPopupMenu();
		addOwnSettingsToPopup(owner,popup);
		return popup;
	}

	/**
	 * Maximal zu berücksichtigende Anzahl an Navigationsebenen
	 * @see #getNavigationTree()
	 */
	private static final int MAX_NAV_LEVELS=10;

	/**
	 * Baut die Navigationsbaumstruktur auf.
	 * @return	Navigationsbaumstruktur
	 * @see #getViewer(boolean)
	 */
	private DefaultMutableTreeNode getNavigationTree() {
		final DefaultMutableTreeNode root=new DefaultMutableTreeNode();

		final DefaultMutableTreeNode[] headings=new DefaultMutableTreeNode[MAX_NAV_LEVELS+1];
		for (int j=0;j<MAX_NAV_LEVELS;j++) headings[j]=root;

		final int size=lines.size();
		for (int i=0;i<size;i++) {
			final int level=lineTypes.get(i);
			if (level<=0 || level>MAX_NAV_LEVELS) continue;

			final DefaultMutableTreeNode node=new DefaultMutableTreeNode(new NavRecord(lines.get(i),i));

			headings[level-1].add(node);
			for (int j=level;j<MAX_NAV_LEVELS+1;j++) headings[j]=node;
		}

		return root;
	}

	/**
	 * Navigationsdatensatz
	 * @see StatisticViewerText#getNavigationTree()
	 *
	 */
	private static class NavRecord {
		/**
		 * Name des Eintrags
		 */
		private final String name;

		/**
		 * Zeilennummer
		 */
		private final int lineNr;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name des Eintrags
		 * @param lineNr	Zeilennummer
		 */
		public NavRecord(final String name, final int lineNr) {
			this.name=name;
			this.lineNr=lineNr;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * Aktualisiert die Breite der Navigationsbaumstruktur.
	 */
	private void updateTreeSize() {
		Dimension d=tree.getPreferredSize();
		d.width=Math.min(d.width,Math.max(250,split.getBounds().width/5));
		d=tree.getMinimumSize();
		d.width=Math.max(d.width,250);
		tree.setMinimumSize(d);
		if (d.width!=split.getDividerLocation()) split.setDividerLocation(d.width);
	}

	/**
	 * Liefert die Zeilennummer zu dem in der Baumstruktur ausgewählten Navigationsdatensatz
	 * @return	0-basierte Zeilennummer oder -1, wenn keine Datensatz gewählt ist
	 */
	public int getSelectedNavLine() {
		final TreePath path=tree.getSelectionPath();
		if (path==null) return -1;
		final Object last=path.getLastPathComponent();
		if (!(last instanceof DefaultMutableTreeNode)) return -1;
		final DefaultMutableTreeNode node=(DefaultMutableTreeNode)last;
		final Object userObject=node.getUserObject();
		if (!(userObject instanceof NavRecord)) return -1;

		return ((NavRecord)userObject).lineNr;
	}

	/**
	 * Initialisiert die Anzeige der zusätzlichen Beschreibung.
	 * @see #addDescription(URL, Consumer)
	 * @see #descriptionURL
	 * @see #descriptionHelpCallback
	 * @see #descriptionPane
	 */
	private void initDescriptionPane() {
		if (descriptionPane!=null) return;
		if (descriptionURL==null) return;

		descriptionPane=new DescriptionViewer(descriptionURL,link->{
			if (link.toLowerCase().startsWith("help:") && descriptionHelpCallback!=null) {
				descriptionHelpCallback.accept(link.substring("help:".length()));
			}
		},getDescriptionCustomStyles());
	}

	/**
	 * Konkretes Anzeigeobjekt, das üger {@link #getViewer(boolean)} geliefert wird.
	 * @see #getViewer(boolean)
	 */
	private Container viewer=null;

	@Override
	public Container getViewer(final boolean needReInit) {
		if (viewer!=null && !needReInit) return viewer;

		/* Split zwischen Navigation und Text */
		split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setResizeWeight(0.25);
		splitDividerSize=split.getDividerSize();

		/* Text */
		if (textPane==null || needReInit) {
			textPane=null;
			lines.clear();
			hints.clear();
			lineTypes.clear();
			indentLevel.clear();
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		final JScrollPane textScroller=new JScrollPane(textPane);
		textPane.setSelectionStart(0);
		textPane.setSelectionEnd(0);
		split.setRightComponent(textScroller);

		/* Navigationsstruktur */
		tree=new JTree(new DefaultTreeModel(getNavigationTree()));
		split.setLeftComponent(treeScroller=new JScrollPane(tree));
		tree.setRootVisible(false);
		tree.getParent().setMinimumSize(new Dimension(150,0));
		tree.addTreeSelectionListener(e->gotoStartOfLine(getSelectedNavLine()+1));
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(SimToolsImages.STATISTICS_TEXT.getIcon());
		for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);
		split.addPropertyChangeListener("ancestor",e->updateTreeSize());
		treeScroller.setVisible(false);

		split.setDividerLocation(0);
		split.setDividerSize(0);

		/* Hinweistext unten */
		if (descriptionPane==null) return viewer=split;
		return viewer=descriptionPane.getSplitPanel(split);
	}

	@Override
	public boolean isViewerGenerated() {
		return viewer!=null;
	}

	/**
	 * Liefert den Text des Viewers ohne Formatierungen.
	 * @return	Text des Viewers ohne Formatierungen
	 * @see #copyToClipboard(Clipboard)
	 * @see #save(Component, File)
	 */
	private String getPlainText() {
		StringBuilder result=new StringBuilder();

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);
			final int indent=indentLevel.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				result.append('\n');
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line+"\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line+"\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (i>0 && lineTypes.get(i-1)!=-2) result.append('\n');
				for (int j=0;j<type-2;j++) result.append("  ");
				result.append(line+"\n");
				continue;
			}
		}
		return result.toString();
	}

	/**
	 * Liefert den Text des Viewer mit RTF-Formatierung.
	 * @return	Text des Viewer mit RTF-Formatierung
	 * @see #copyToClipboard(Clipboard)
	 * @see #save(Component, File)
	 */
	private String getRTFText() {
		StringBuilder result=new StringBuilder();

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);
			final int indent=indentLevel.get(i);

			if (type==-1) {
				/* Absatzanfang */
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				result.append("\\par\n");
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(convertLineToRTF(line)+"\\line\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(convertLineToRTF(line)+"\\line\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				String fs="22";
				switch (type) {
				case 1: fs="34"; break;
				case 2: fs="28"; break;
				}
				if (i>0 && lineTypes.get(i-1)!=-2) result.append("\\par\n");
				result.append("\\fs"+fs+" "+convertLineToRTF(line)+"\\fs22\\par\n");
				continue;
			}
		}

		return "{\\rtf1\\ansi\\ansicpg1252\\deff0{\\fonttbl\\f0\\fswiss Helvetica;}\\f0\n"+result.toString()+"\n}\n";
	}

	/**
	 * Liefert den Text des Viewer mit HTML-Formatierung.
	 * @return	Text des Viewer mit HTML-Formatierung
	 * @see #getFullHTMLText()
	 */
	private String getHTMLText() {
		StringBuilder result=new StringBuilder();

		boolean inParagraph=false;

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);
			final int indent=indentLevel.get(i);

			if (type==-1) {
				/* Absatzanfang */
				if (inParagraph) result.append("</p>\n");
				result.append("<p>\n");
				inParagraph=true;
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				if (inParagraph) result.append("</p>\n");
				inParagraph=false;
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				if (!inParagraph) {result.append("<p>\n"); inParagraph=true;}
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line+"<br>\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (!inParagraph) {result.append("<p>\n"); inParagraph=true;}
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line+"<br>\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (inParagraph) {result.append("</p>\n"); inParagraph=false;}
				result.append("<h"+type+">"+line+"</h"+type+">\n");
				continue;
			}
		}

		return result.toString();
	}

	/**
	 * Liefert den Text des Viewer mit LaTeX-Formatierung.
	 * @return	Text des Viewer mit LaTeX-Formatierung
	 * @see #save(Component, File)
	 * @see #saveLaTeX(BufferedWriter, File, int)
	 */
	private String getLaTeXText() {
		StringBuilder result=new StringBuilder();

		boolean inParagraph=false;

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);
			final int indent=indentLevel.get(i);

			if (type==-1) {
				/* Absatzanfang */
				if (inParagraph) result.append("\n");
				result.append("\n");
				inParagraph=true;
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				if (inParagraph) result.append("\n");
				inParagraph=false;
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				if (!inParagraph) {result.append("\n"); inParagraph=true;}
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line.replace("%","\\%")+"\\\\\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (!inParagraph) {result.append("\n"); inParagraph=true;}
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line.replace("%","\\%")+"\\\\\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (inParagraph) {result.append("\n"); inParagraph=false;}
				switch (type) {
				case 1: result.append("\\section{"+line+"}\n"); break;
				case 2: result.append("\\subsection{"+line+"}\n"); break;
				case 3: result.append("\\subsubsection{"+line+"}\n"); break;
				default: result.append("\\paragraph{"+line+"}~\\\n"); break;
				}
				continue;
			}
		}

		return result.toString();
	}

	/**
	 * Liefert den Text des Viewer mit Typst-Formatierung.
	 * @return	Text des Viewer mit Typst-Formatierung
	 * @see #save(Component, File)
	 * @see #saveLaTeX(BufferedWriter, File, int)
	 */
	public String getTypstText() {
		StringBuilder result=new StringBuilder();

		boolean inParagraph=false;

		for (int i=0;i<lines.size();i++) {
			String line=lines.get(i);
			line=line.replace("*","\\*");
			final int type=lineTypes.get(i);
			final int indent=indentLevel.get(i);

			if (type==-1) {
				/* Absatzanfang */
				if (inParagraph) result.append("\n");
				result.append("\n");
				inParagraph=true;
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				if (inParagraph) result.append("\n");
				inParagraph=false;
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				if (!inParagraph) {result.append("\n"); inParagraph=true;}
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line+"\\\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (!inParagraph) {result.append("\n"); inParagraph=true;}
				for (int j=0;j<indent;j++) result.append("  ");
				result.append(line+"\\\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (inParagraph) {result.append("\n"); inParagraph=false;}
				switch (type) {
				case 1: result.append("= "+line+"\n"); break;
				case 2: result.append("== "+line+"\n"); break;
				case 3: result.append("=== "+line+"\n"); break;
				default: result.append("==== "+line+"\n"); break;
				}
				continue;
			}
		}

		return result.toString();
	}

	/**
	 * Liefert den Text des Viewer mit HTML-Formatierung und inkl. HTML-Vor- und Abspann.
	 * @return	Text des Viewer mit LaTeHTML-Formatierung
	 * @see #save(Component, File)
	 */
	private String getFullHTMLText() {
		final StringBuilder result=new StringBuilder();

		result.append("<!DOCTYPE html>\n");
		result.append("<html>\n");
		result.append("<head>\n");
		result.append("  <meta charset=\"utf-8\">");
		result.append("  <title>"+StatisticsBasePanel.program_name+"</title>\n");
		result.append("  <meta name=\"author\" content=\"Alexander Herzog\">\n");
		result.append("  <style type=\"text/css\">\n");
		result.append("    body {font-family: Verdana, Lucida, sans-serif;}\n");
		result.append("    table {border: 1px solid black; border-collapse: collapse;}\n");
		result.append("    td {border: 1px solid black; padding: 2px 5px;}\n");
		result.append("  </style>\n");
		result.append("</head>\n");
		result.append("<body>\n");
		result.append(getHTMLText());
		result.append("</body>\n</html>\n");

		return result.toString();
	}

	/**
	 * Liefert den Text des Viewer mit Markdown-Formatierung.
	 * @return	Text des Viewer mit Markdown-Formatierung
	 * @see #save(Component, File)
	 */
	private String getMarkdownText() {
		StringBuilder result=new StringBuilder();

		boolean inParagraph=false;

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);
			/* final int indent=indentLevel.get(i); - wird in Markdown nicht unterstützt */

			if (type==-1) {
				/* Absatzanfang */
				if (inParagraph) result.append("\n");
				inParagraph=true;
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				if (inParagraph) result.append("\n");
				inParagraph=false;
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				inParagraph=true;
				result.append(line+"  \n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				inParagraph=true;
				result.append(line+"  \n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				if (inParagraph) {result.append("\n"); inParagraph=false;}
				for (int j=1;j<=type;j++) result.append('#');
				result.append(' ');
				result.append(line+"  \n");
				continue;
			}
		}

		return result.toString();
	}

	/**
	 * Wird unmittelbar vor der ersten Verwendung der Textdaten aufgerufen, sofern der Text leer ist.
	 */
	protected abstract void buildText();

	@Override
	public Transferable getTransferable() {
		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		final DataFlavor RTF_FLAVOR=new DataFlavor("text/rtf", "Rich Formatted Text");
		final String transferPlain=getPlainText();
		final Object transferRTF=new ByteArrayInputStream(getRTFText().getBytes());

		final Transferable transfer=new Transferable() {
			private final DataFlavor[] flavors=new DataFlavor[]{DataFlavor.stringFlavor,RTF_FLAVOR};

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				for (DataFlavor test: flavors) if (test.equals(flavor)) return true;
				return false;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return flavors;
			}

			@Override
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if (flavor.equals(RTF_FLAVOR)) return transferRTF;
				if (flavor.equals(DataFlavor.stringFlavor)) return transferPlain;
				return null;
			}
		};

		return transfer;
	}

	@Override
	public void copyToClipboard(final Clipboard clipboard) {
		final Transferable transferable=getTransferable();
		if (transferable!=null) clipboard.setContents(transferable,null);
	}

	@Override
	public boolean print() {
		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		try {textPane.print();} catch (PrinterException e) {return false;}
		return true;
	}

	@Override
	public void save(final Component owner) {
		final var fc=new PlugableFileChooser(true);
		fc.setDialogTitle(StatisticsBasePanel.viewersSaveText);
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeDOCX+" (*.docx)","docx");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeODT+" (*.odt)","odt");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeRTF+" (*.rtf)","rtf");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeHTML+" (*.html, *.htm)","html","htm");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeTXT+" (*.txt)","txt");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypePDF+" (*.pdf)","pdf");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeMD+" (*.md)","md");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeTEX+" (*.tex)","tex");
		fc.addChoosableFileFilter(StatisticsBasePanel.fileTypeTYP+" (*.typ)","typ");
		fc.setFileFilter("docx");
		fc.setAcceptAllFileFilterUsed(false);
		final File file=fc.showSaveDialogFileWithExtension(owner);
		if (file==null) return;

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		save(owner,file);
	}

	/**
	 * Speichert den Text als PDF-Datei.
	 * @param owner	Übergeordnete Komponente für die eventuelle Anzeige von Dialogen
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll. Es darf hier <b>nicht</b> <code>null</code> übergeben werden.
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 * @see #save(Component, File)
	 */
	private boolean savePDF(final Component owner, final File file) {
		PDFWriter pdf=new PDFWriter(owner,new ReportStyle());
		if (!pdf.systemOK) return false;
		if (!savePDF(pdf)) return false;
		return pdf.save(file);
	}

	/**
	 * Speichert den Text als DOCX-Datei.
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll. Es darf hier <b>nicht</b> <code>null</code> übergeben werden.
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 * @see #save(Component, File)
	 */
	private boolean saveDOCX(final File file) {
		try(XWPFDocument doc=new XWPFDocument()) {
			if (!saveDOCX(new DOCXWriter(doc))) return false;
			try (FileOutputStream out=new FileOutputStream(file)) {doc.write(out);}
			return true;
		} catch (IOException e) {return false;}
	}

	/**
	 * Speichert den Text als ODT-Datei.
	 * @param odt	Ausgabe-Text-Dokument
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 * @see #saveODT(File)
	 */
	private boolean saveODT(final TextDocument odt) {
		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		Paragraph p=null;

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				p=odt.addParagraph(null);
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				p=null;
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				if (p==null) p=odt.addParagraph(null);
				p.appendTextContent(line);
				p.appendTextContent("\n");
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (p==null) p=odt.addParagraph(null);
				p.appendTextContent(line);
				p.appendTextContent("\n");
				continue;
			}
			if (type>0) {
				/* Überschriften */
				p=odt.addParagraph(null);
				int fs=12;
				switch (type) {
				case 1: fs=18; break;
				case 2: fs=15; break;
				}
				p.setFont(new org.odftoolkit.simple.style.Font("Arial",StyleTypeDefinitions.FontStyle.BOLD,fs));
				p.appendTextContent(line);
				p=null;
				continue;
			}
		}

		return true;
	}

	/**
	 * Speichert den Text als ODT-Datei.
	 * @param file	Datei, in der die Statistikdaten gespeichert werden soll. Es darf hier <b>nicht</b> <code>null</code> übergeben werden.
	 * @return	Liefert <code>true</code> zurück, wenn die Daten erfolgreich gespeichert werden konnten.
	 * @see #save(Component, File)
	 */
	private boolean saveODT(final File file) {
		try (TextDocument odt=TextDocument.newTextDocument()) {
			if (!saveODT(odt)) return false;
			odt.save(file);
			return true;

		} catch (Exception e) {return false;}
	}

	@Override
	public boolean save(final Component owner, final File file) {
		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		String filename=file.getName().toUpperCase();

		if (filename.endsWith(".DOCX")) return saveDOCX(file);
		if (filename.endsWith(".ODT")) return saveODT(file);
		if (filename.endsWith(".PDF")) return savePDF(owner,file);

		String text="";
		if (filename.endsWith(".RTF")) text=getRTFText();
		if (filename.endsWith(".HTML") || filename.endsWith(".HTM")) text=getFullHTMLText();
		if (filename.endsWith(".MD")) text=getMarkdownText();
		if (filename.endsWith(".TEX")) text=getLaTeXText();
		if (filename.endsWith(".TYP")) text=getTypstText();
		if (text.isEmpty()) text=getPlainText();

		return Table.saveTextToFile(text,file);
	}

	@Override
	public int saveHtml(final BufferedWriter bw, final File mainFile, final int nextImageNr, final boolean imagesInline) throws IOException {
		if (textPane==null) {
			buildText();
			bw.write(getHTMLText());
			reset();
		} else {
			bw.write(getHTMLText());
		}
		return nextImageNr;
	}

	@Override
	public int saveLaTeX(final BufferedWriter bw, final File mainFile, final int nextImageNr) throws IOException {
		if (textPane==null) {
			buildText();
			bw.write(getLaTeXText());
			reset();
		} else {
			bw.write(getLaTeXText());
		}
		return nextImageNr;
	}

	@Override
	public int saveTypst(final BufferedWriter bw, final File mainFile, final int nextImageNr) throws IOException {
		if (textPane==null) {
			buildText();
			bw.write(getTypstText());
			reset();
		} else {
			bw.write(getTypstText());
		}
		return nextImageNr;
	}

	@Override
	public void unZoom() {}

	@Override
	public JButton[] getAdditionalButton() {
		final boolean word=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.WORD);
		final boolean odt=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.ODT);
		final boolean pdf=StatisticsBasePanel.viewerPrograms.contains(StatisticsBasePanel.ViewerPrograms.PDF);
		int count=0;
		if (word) count++;
		if (odt) count++;
		if (pdf) count++;

		if (count>1) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarOpenText);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarOpenTextHint);
			button.setIcon(SimToolsImages.OPEN.getIcon());
			button.addActionListener(e->{
				final JPopupMenu menu=new JPopupMenu();
				JMenuItem item;
				if (word) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarWord));
					item.setIcon(SimToolsImages.SAVE_TEXT_WORD.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarWordHint);
					item.addActionListener(ev->openWord());
				}
				if (odt) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarODT));
					item.setIcon(SimToolsImages.SAVE_TEXT.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarODTHint);
					item.addActionListener(ev->openODT());
				}
				if (pdf) {
					menu.add(item=new JMenuItem(StatisticsBasePanel.viewersToolbarPDF));
					item.setIcon(SimToolsImages.SAVE_PDF.getIcon());
					item.setToolTipText(StatisticsBasePanel.viewersToolbarPDFHint);
					item.addActionListener(ev->openPDF(SwingUtilities.getWindowAncestor(textPane)));
				}
				menu.show(button,0,button.getHeight());

			});
			return new JButton[]{button};
		}

		if (word) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarWord);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarWordHint);
			button.setIcon(SimToolsImages.SAVE_TEXT_WORD.getIcon());
			button.addActionListener(e->openWord());
			return new JButton[]{button};
		}

		if (odt) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarODT);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarODTHint);
			button.setIcon(SimToolsImages.SAVE_TEXT.getIcon());
			button.addActionListener(e->openODT());
			return new JButton[]{button};
		}

		if (pdf) {
			final JButton button=new JButton(StatisticsBasePanel.viewersToolbarPDF);
			button.setToolTipText(StatisticsBasePanel.viewersToolbarPDFHint);
			button.setIcon(SimToolsImages.SAVE_PDF.getIcon());
			button.addActionListener(e->openPDF(SwingUtilities.getWindowAncestor(textPane)));
			return new JButton[]{button};
		}

		return null;
	}

	/**
	 * Wird aufgerufen, um eine externe Datei (mit dem Standardprogramm) zu öffnen.
	 * @param file	Zu öffnende Datei
	 * @throws IOException	Kann ausgelöst werden, wenn die Datei nicht geöffnet werden konnte
	 */
	protected void openExternalFile(final File file) throws IOException {
		Desktop.getDesktop().open(file);
	}

	/**
	 * Öffnet den Text (über eine temporäre Datei) mit Word
	 */
	private void openWord() {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".docx");
			if (saveDOCX(file)) {
				file.deleteOnExit();
				openExternalFile(file);
			}
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	/**
	 * Öffnet den Text (über eine temporäre Datei) mit OpenOffice/LibreOffice
	 */
	private void openODT() {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".odt");
			if (saveODT(file)) {
				file.deleteOnExit();
				openExternalFile(file);
			}
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	/**
	 * Öffnet den Text (über eine temporäre Datei) als pdf
	 * @param owner	Übergeordnete Komponente für die eventuelle Anzeige von Dialogen
	 */
	private void openPDF(final Component owner) {
		try {
			final File file=File.createTempFile(StatisticsBasePanel.viewersToolbarExcelPrefix+"_",".pdf");
			if (savePDF(owner,file)) {
				file.deleteOnExit();
				openExternalFile(file);
			}
		} catch (IOException e1) {
			MsgBox.error(getViewer(false),StatisticsBasePanel.viewersToolbarExcelSaveErrorTitle,StatisticsBasePanel.viewersToolbarExcelSaveErrorInfo);
		}
	}

	/**
	 * Wandelt eine Zahl im Bereich von 0 bis 15 in die entsprechende Hexadezimal-Darstellung um.
	 * @param b	Umzuwandelnde Zahl (0..15)
	 * @return	Hexadezimal-Darstellung
	 * @see #convertLineToRTF(String)
	 */
	private char hex(final int b) {
		if (b<10) return (char)(b+((short)'0')); else return (char)(b-10+((short)'a'));
	}

	@Override
	public String[] ownSettingsName() {
		return null;
	}

	@Override
	public Icon[] ownSettingsIcon() {
		return null;
	}

	@Override
	public boolean ownSettings(final StatisticsBasePanel owner, final int nr) {
		return false;
	}

	/**
	 * Wandelt eine Textzeile in eine RTF-Zeichenkette um
	 * @param line	Umzuwandelnde Zeile
	 * @return	RTF-Zeichenkette
	 * @see #getRTFText()
	 */
	private String convertLineToRTF(final String line) {
		StringBuilder result=new StringBuilder();
		for (int i=0;i<line.length();i++) {
			char c=line.charAt(i);
			if ((short)c<=127) {result.append(c); continue;}
			short b=(short)c;

			result.append("\\'");
			result.append(hex(b/16));
			result.append(hex(b%16));

		}
		return result.toString();
	}

	@Override
	public boolean saveDOCX(final DOCXWriter doc) {
		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				doc.beginParagraph();
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				doc.endParagraph();
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				doc.writeText(line);
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				doc.writeText(line);
				continue;
			}
			if (type>0) {
				/* Überschriften */
				doc.writeHeading(line,type);
				continue;
			}
		}

		return true;
	}

	@Override
	public boolean savePDF(final PDFWriter pdf) {
		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		boolean newParagraph=true;

		for (int i=0;i<lines.size();i++) {
			final String line=lines.get(i);
			final int type=lineTypes.get(i);

			if (type==-1) {
				/* Absatzanfang */
				newParagraph=true;
				continue;
			}
			if (type==-2) {
				/* Absatzende */
				newParagraph=true;
				continue;
			}
			if (type==-4) {
				/* Link (als Text, nicht kleiner Info-Link) */
				if (newParagraph) {pdf.writeStyledParSkip(); newParagraph=false;}
				if (!pdf.writeStyledText(line)) return false;
				continue;
			}
			if (type==0) {
				/* Normaler Text */
				if (newParagraph) {pdf.writeStyledParSkip(); newParagraph=false;}
				if (!pdf.writeStyledText(line)) return false;
				continue;
			}
			if (type>0) {
				/* Überschriften */
				pdf.writeStyledParSkip();
				pdf.writeStyledParSkip();
				if (!pdf.writeStyledHeading(line,type)) return false;
				newParagraph=true;
				continue;
			}
		}
		pdf.writeStyledParSkip();
		pdf.writeStyledParSkip();

		return true;
	}

	/**
	 * Berechnet aus einigem Messreihen-Kenngrößen die Standardabweichung
	 * @param x2	Quadrierte Summe der Werte
	 * @param x	Summe der Werte
	 * @param n	Anzahl der Werte
	 * @return	Standardabweichung der Messreihe
	 */
	protected static long calcStd(final long x2, final long x, final long n) {
		if (n>1) return Math.round(Math.sqrt(((double)x2)/(n-1)-((double)(x*x)/n/(n-1)))); else return 0;
	}

	/**
	 * Berechnet aus Messreihen-Kenngrößen die Standardabweichung
	 * @param x2	Quadrierte Summe der Werte
	 * @param x	Summe der Werte
	 * @param n	Anzahl der Werte
	 * @return	Standardabweichung der Messreihe
	 */
	protected static double calcStd(final double x2, final double x, final double n) {
		if (n>1) return Math.sqrt(x2/(n-1)-x*x/n/(n-1)); else return 0;
	}

	/**
	 * Berechnet aus Messreihen-Kenngrößen ein Konfidenzintervall
	 * @param x2	Quadrierte Summe der Werte
	 * @param x	Summe der Werte
	 * @param n	Anzahl der Werte
	 * @param p	Wahrscheinlichkeit, zu der das Konfidenzintervall bestimmt werden soll
	 * @return	Konfidenzintervall der Messreihe zur Wahrscheinlichkeit <code>p</code>
	 */
	protected static double[] calcConfidence(final double x2, final double x, final double n, final double p) {
		final double[] interval=new double[2];

		final double mean=x/n;
		final double sd=calcStd(x2,x,n);

		/* x +- t(n-1;1-alpha/2)*sd/sqrt(n) */
		final TDistribution dist=new TDistribution(n-1);
		final double t=dist.inverseCumulativeProbability(1-p/2);
		final double half=t*sd/Math.sqrt(n);

		interval[0]=mean-half;
		interval[1]=mean+half;

		return interval;
	}

	/**
	 * Fügt eine Überschrift zu der Ausgabe hinzu
	 * @param level	Ebene der Überschrift; gültige Werte: 1,2,3
	 * @param s	Textzeile, die ausgegeben werden soll
	 */
	protected void addHeading(final int level, final String s) {
		lines.add(s);
		hints.add(null);
		lineTypes.add(level);
		indentLevel.add(0);
	}

	/**
	 * Fügt eine Textzeile an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param line	Textzeile, die ausgegeben werden soll
	 */
	protected void addLine(final int indentLevel, final String line) {
		lines.add(line);
		hints.add(null);
		lineTypes.add(0);
		this.indentLevel.add(indentLevel);
	}

	/**
	 * Fügt eine Textzeile an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param line	Textzeile, die ausgegeben werden soll
	 * @param hint	Optionaler zusätzlicher Hinweis für das Kontextmenü
	 * @see #processContextClick(StatisticsBasePanel, String)
	 */
	protected void addLine(final int indentLevel, final String line, final String hint) {
		lines.add(line);
		hints.add(hint);
		lineTypes.add(0);
		this.indentLevel.add(indentLevel);
	}

	/**
	 * Fügt einen kleinen Info-Link an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param link	Bezeichner des Links
	 * @param text	Anzuzeigender Text
	 * @see #processLinkClick(String)
	 */
	protected void addLink(final int indentLevel, final String link, final String text) {
		lines.add(text);
		hints.add(link);
		lineTypes.add(-3);
		this.indentLevel.add(indentLevel);
	}

	/**
	 * Fügt einen Link an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param link	Bezeichner des Links
	 * @param text	Anzuzeigender Text
	 * @see #processLinkClick(String)
	 */
	protected void addLinkLine(final int indentLevel, final String link, final String text) {
		lines.add(text);
		hints.add(link);
		lineTypes.add(-4);
		this.indentLevel.add(indentLevel);
	}

	/**
	 * Beginnt einen neuen Absatz in der Ausgabe
	 */
	protected void beginParagraph() {
		lines.add("");
		hints.add(null);
		lineTypes.add((-1));
		indentLevel.add(0);
	}

	/**
	 * Beendet den aktuellen Absatz in der Ausgabe
	 */
	protected void endParagraph() {
		lines.add("");
		hints.add(null);
		lineTypes.add((-2));
		indentLevel.add(0);
	}

	/**
	 * Fügt eine Textzeile (ohne Einrückung) an die Ausgabe an
	 * @param line	Textzeile, die ausgegeben werden soll
	 */
	protected void addLine(final String line) {
		addLine(0,line);
	}

	/**
	 * Fügt eine Textzeile (ohne Einrückung) an die Ausgabe an
	 * @param line	Textzeile, die ausgegeben werden soll
	 * @param hint	Optionaler zusätzlicher Hinweis für das Kontextmenü
	 */
	protected void addLine(final String line, final String hint) {
		addLine(0,line,hint);
	}

	/**
	 * Fügt einen kleinen Info-Link (ohne Einrückung) an die Ausgabe an
	 * @param link	Bezeichner des Links
	 * @param text	Anzuzeigender Text
	 * @see StatisticViewerText#processLinkClick(String)
	 */
	protected void addLink(final String link, final String text) {
		addLink(0,link,text);
	}

	/**
	 * Fügt einen Link (ohne Einrückung) an die Ausgabe an
	 * @param link	Bezeichner des Links
	 * @param text	Anzuzeigender Text
	 * @see StatisticViewerText#processLinkClick(String)
	 */
	protected void addLinkLine(final String link, final String text) {
		addLinkLine(0,link,text);
	}

	/**
	 * Fügt mehrere Textzeilen (ohne Einrückung) an die Ausgabe an
	 * An jedem "\n" erfolgt ein Zeilenumbruch
	 * @param s	Textzeilen, die ausgegeben werden sollen
	 */
	protected void addLines(final String s) {
		String[] list=s.split("\n");
		for (int i=0;i<list.length;i++) addLine(0,list[i]);
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param data	Wert, der durch <code>days</code> geteilt ausgegeben werden soll
	 * @param days	Anzahl der Tage, durch die der summierte Wert <code>data</code> geteilt werden soll
	 * @param sum	Ist dieser Wert &gt;0, so wird zusätzlich ein Prozentwert, der sich aus dem Bruch <code>data/sum</code> ergibt, ausgegeben
	 */
	protected void addLine(final int indentLevel, final String label, final int data, final int days, final int sum) {
		String s=label+": ";
		s+=NumberTools.formatNumber((double)data/days,3);
		if (sum>0) s+=" ("+NumberTools.formatPercent((double)data/sum)+")";
		addLine(indentLevel,s);
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param data	Wert, der durch <code>div</code> geteilt ausgegeben werden soll
	 * @param div	Wert, durch den der summierte Wert <code>data</code> geteilt werden soll
	 */
	protected void addLineDiv(final int indentLevel, final String label, final double data, final double div) {
		addLine(indentLevel,label+": "+NumberTools.formatNumber(data/div,3));
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param data	Wert, der durch <code>div</code> geteilt ausgegeben werden soll
	 * @param div	Wert, durch den der summierte Wert <code>data</code> geteilt werden soll
	 */
	protected void addLineDiv2(final int indentLevel, final String label, final double data, final double div) {
		addLine(indentLevel,label+": "+NumberTools.formatNumber(data/div,2));
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param data	Wert, der ausgegeben werden soll
	 * @param digits	Anzahl an anzuzeigenden Nachkommastellen
	 */
	protected void addLine(final int indentLevel, final String label, final double data, final int digits) {
		addLine(indentLevel,label+": "+NumberTools.formatNumber(data,digits));
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert an die Ausgabe an.
	 * Es wird dabei eine Nachkommastelle ausgegeben.
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param data	Wert, der ausgegeben werden soll
	 */
	protected void addLine(final int indentLevel, final String label, final double data) {
		addLine(indentLevel,label,data,1);
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert in Prozent-Schreibweise an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param value	Wert, der in Prozentdarstellung ausgegeben werden soll
	 * @param digits	Anzahl an anzuzeigenden Nachkommastellen
	 */
	protected void addPercentLine(final int indentLevel, final String label, final double value, final int digits) {
		addLine(indentLevel,label+": "+NumberTools.formatPercent(value,digits));
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert in Prozent-Schreibweise an die Ausgabe an.
	 * Es wird dabei eine Nachkommastelle ausgegeben.
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param value	Wert, der in Prozentdarstellung ausgegeben werden soll
	 */
	protected void addPercentLine(final int indentLevel, final String label, final double value) {
		addLine(indentLevel,label,value,1);
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert in Prozent-Schreibweise an die Ausgabe an
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param zaehler	Ausgegeben wird die Zahl zaehler/nenner. Hier wird der Zähler des Bruches übergeben.
	 * @param nenner	Ausgegeben wird die Zahl zaehler/nenner. Hier wird der Nenner des Bruches übergeben.
	 * @param digits	Anzahl an anzuzeigenden Nachkommastellen
	 */
	protected void addPercentLineParts(final int indentLevel, final String label, final long zaehler, final long nenner, final int digits) {
		if (nenner==0) return;
		addPercentLine(indentLevel,label,((double)zaehler)/nenner,digits);
	}

	/**
	 * Fügt eine Textzeile mit einem Zahlenwert in Prozent-Schreibweise an die Ausgabe an.
	 * Es wird dabei eine Nachkommastelle ausgegeben.
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param zaehler	Ausgegeben wird die Zahl zaehler/nenner. Hier wird der Zähler des Bruches übergeben.
	 * @param nenner	Ausgegeben wird die Zahl zaehler/nenner. Hier wird der Nenner des Bruches übergeben.
	 */
	protected void addPercentLineParts(final int indentLevel, final String label, final long zaehler, final long nenner) {
		addPercentLineParts(indentLevel,label,zaehler,nenner,1);
	}

	/**
	 * Fügt eine Textzeile mit einer Zeitangabe in Sekunden an die Ausgabe an.
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param time	Zeitangabe (in Sekunden), die ausgegeben werden soll.
	 */
	protected void addShortTime(final int indentLevel, final String label, final double time) {
		addLine(indentLevel,label+": "+NumberTools.formatNumber(time,1)+" "+StatisticsBasePanel.viewersTextSeconds+" (="+TimeTools.formatTime((int)Math.round(time))+")");
	}

	/**
	 * Fügt eine Textzeile mit einer Zeitangabe in Sekunden an die Ausgabe an.
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param zaehler	Ausgegeben wird die Zeit zaehler/nenner. Hier wird der Zähler des Bruches übergeben.
	 * @param nenner	Ausgegeben wird die Zeit zaehler/nenner. Hier wird der Nenner des Bruches übergeben.
	 */
	protected void addShortTimeParts(final int indentLevel, final String label, final long zaehler, final long nenner) {
		if (nenner==0) return;
		addShortTime(indentLevel,label,((double)zaehler)/nenner);
	}

	/**
	 * Fügt eine Textzeile mit einer Zeitangabe an die Ausgabe an.
	 * @param indentLevel	Einrück-Level (0=keine Einrückung; 1=2 Leerzeichen, 2=4 Leerzeichen usw.)
	 * @param label	Beschriftung des Wertes, der ausgegeben werden soll
	 * @param time	Zeitangabe (in Sekunden), die ausgegeben werden soll.
	 * @param div	Wert, durch den der summierte Wert <code>time</code> geteilt werden soll
	 * @param sum	Ist dieser Wert &gt;0, so wird zusätzlich ein Prozentwert, der sich aus dem Bruch <code>time/sum</code> ergibt, ausgegeben
	 */
	protected void addPercentTime(final int indentLevel, final String label, final long time, final long div, final long sum) {
		String s=label+": ";
		s+=TimeTools.formatTime(time/div);
		if (sum>0) s+=" = "+NumberTools.formatPercent((double)time/sum);
		addLine(indentLevel,s);
	}

	/**
	 * Wandelt einen Punkt und eine Cursorposition in {@link #textPane} in einen zugehörigen Link um
	 * @param point	Angeklickter Punkt innerhalb von {@link #textPane}
	 * @return	Liefert im Erfolgsfall die URL als Text, sonst <code>null</code>
	 * @see #initTextPane()
	 */
	private String pointToLink(final Point point) {
		if (point.x>100) return null; /* Der "Details"-Link ist eher 20 Pixel breit. Bei 100 sind wir definitiv außerhalb des Textes. */

		final int offset=textPane.viewToModel2D(point);

		if (offset<0) return null;
		final Element element=textPane.getStyledDocument().getCharacterElement(offset);
		if (element==null) return null;
		final Object obj=element.getAttributes().getAttribute("URL");
		if (!(obj instanceof String)) return null;
		return (String)obj;
	}

	/**
	 * Liefert zu der aktuellen Cursorposition in {@link #textPane} den hinterlegten Hinweistext
	 * @param point	Punkt innerhalb von {@link #textPane}
	 * @return	Liefert im Erfolgsfall den hinterlegten Hinweistext, sonst <code>null</code>
	 * @see #initTextPane()
	 */
	private String pointToHint(final Point point) {
		final int offset=textPane.viewToModel2D(point);

		if (offset<0) return null;
		final Element element=textPane.getStyledDocument().getCharacterElement(offset);
		if (element==null) return null;
		final Object obj=element.getAttributes().getAttribute("Hint");
		if (!(obj instanceof String)) return null;
		return (String)obj;
	}

	/**
	 * Wird aufgerufen, wenn ein Link im Text angeklickt wurde.<br>
	 * Muss von abgeleiteten Klassen überschrieben werden, um eine Behandlung des Linkklicks durchzuführen.
	 * @param link	Bezeichner des Links
	 * @see StatisticViewerText#addLink(String, String)
	 * @see StatisticViewerText#addLink(int, String, String)
	 */
	protected void processLinkClick(final String link) {
	}

	@Override
	public void setRequestImageSize(final IntSupplier getImageSize) {}

	@Override
	public void setUpdateImageSize(final IntConsumer setImageSize) {}

	@Override
	public void setRequestChartSetup(Supplier<ChartSetup> getChartSetup) {}

	@Override
	public void setUpdateChartSetup(Consumer<ChartSetup> setChartSetup) {	}

	/**
	 * Zeigt im Fußbereich der Hilfeseite eine "Erklärung einblenden"-Schaltfläche, die,
	 * wenn sie angeklickt wird, die html-Seite der angegebenen Adresse anzeigt.
	 * @param descriptionURL	html-Seite mit einer zusätzlichen Erklärung zu dieser Statistikseite
	 * @param descriptionHelpCallback	Handler, der Themennamen (angegeben über "help:..."-Links) zum Aufruf normaler Hilfeseiten entgegen nimmt
	 */
	protected final void addDescription(final URL descriptionURL, final Consumer<String> descriptionHelpCallback) {
		this.descriptionURL=descriptionURL;
		this.descriptionHelpCallback=descriptionHelpCallback;
	}

	/**
	 * Ermöglicht das Laden zusätzlicher Styles für die Erklärungstexte.
	 * @return	Zusätzliche Stylesheets für Erklärungstexte (kann <code>null</code> oder leer sein)
	 */
	protected String getDescriptionCustomStyles() {
		return null;
	}

	/**
	 * Soll für diese Komponente der Standard-FileDrop-Listener des {@link StatisticsBasePanel} verwendet werden?
	 * @return	Übergeordneten FileDrop-Listener verwenden (<code>false</code>) oder eigenen (<code>true</code>)
	 */
	@Override
	public boolean hasOwnFileDropListener() {
		return false;
	}

	/**
	 * Speichert die Daten zu einem einzelnen Suchtreffer
	 */
	private static class Hit {
		/** Startposition */
		public final int start;
		/** Endposition */
		public final int end;

		/**
		 * Konstruktor
		 * @param start	Startposition
		 * @param end	Endposition
		 */
		public Hit(final int start, final int end) {
			this.start=start;
			this.end=end;
		}
	}

	/**
	 * Sucht einen Text in einem Element und seinen Unterelementen
	 * @param element	Element von dem die Suche ausgehen soll
	 * @param search	Bei Berücksichtigung von Groß- und Kleinschreibung: Suchtext; ohne Berücksichtigung von Groß- und Kleinschreibung: Suchtext in Kleinschreibung
	 * @param caseSensitive	Soll die Groß- und Kleinschreibung berücksichtigt werden?
	 * @param hits	Liste mit den Fundstellen (Cursorpositionen)
	 * @see #getCaretPositions(String, boolean, boolean)
	 */
	private void searchInElement(final Element element, final String search, final boolean caseSensitive, final List<Hit> hits) {
		if (hits.size()>=MAX_SEARCH_HITS) return;

		for (int i=0;i<element.getElementCount();i++) {
			searchInElement(element.getElement(i),search,caseSensitive,hits);
			if (hits.size()>=MAX_SEARCH_HITS) return;
		}

		if (element instanceof LeafElement) {
			final LeafElement leaf=(LeafElement)element;
			try {
				final int start=leaf.getStartOffset();
				String text=textPane.getText(start,leaf.getEndOffset()-start);
				if (text.isEmpty()) return;
				if (!caseSensitive) text=text.toLowerCase();

				int index=-1;
				while (true) {
					index=text.indexOf(search,index+1);
					if (index<0) break;
					hits.add(new Hit(start+index,start+index+search.length()-1));
					if (hits.size()>=MAX_SEARCH_HITS) return;
				}
			} catch (BadLocationException e) {}
		}
	}

	/**
	 * Sucht einen Text in einem Element und seinen Unterelementen
	 * @param element	Element von dem die Suche ausgehen soll
	 * @param pattern	Regulärer Ausdruck nach dem gesucht werden soll
	 * @param hits	Liste mit den Fundstellen (Cursorpositionen)
	 * @see #getCaretPositions(String, boolean, boolean)
	 */
	private void searchInElementRegEx(final Element element, final Pattern pattern, final List<Hit> hits) {
		if (hits.size()>=MAX_SEARCH_HITS) return;

		for (int i=0;i<element.getElementCount();i++) {
			searchInElementRegEx(element.getElement(i),pattern,hits);
			if (hits.size()>=MAX_SEARCH_HITS) return;
		}

		if (element instanceof LeafElement) {
			final LeafElement leaf=(LeafElement)element;
			try {
				final int start=leaf.getStartOffset();
				String text=textPane.getText(start,leaf.getEndOffset()-start);
				if (text.isEmpty()) return;

				final Matcher matcher=pattern.matcher(text);
				int index=-1;
				while (true) {
					if (!matcher.find(index+1)) break;
					index=matcher.start();
					hits.add(new Hit(start+index,start+matcher.end()-1));
					if (hits.size()>=MAX_SEARCH_HITS) return;
				}
			} catch (BadLocationException e) {}
		}
	}

	/**
	 * Sucht in dem Viewer nach einem Text und liefert die Cursorpositionen der Fundstellen
	 * @param search	Zu suchender Text
	 * @param caseSensitive	Soll die Groß- und Kleinschreibung berücksichtigt werden?
	 * @param regularExpression	Suchbegriff ist regulärer Ausdruck?
	 * @return	Liste mit den Cursorpositionen der Fundstellen (kann leer sein, ist aber nie <code>null</code>)
	 * @see #search(Component)
	 */
	private List<Hit> getCaretPositions(String search, final boolean caseSensitive, final boolean regularExpression) {
		final List<Hit> hits=new ArrayList<>();
		final Element root=textPane.getStyledDocument().getDefaultRootElement();
		if (regularExpression) {
			try {
				final Pattern pattern=Pattern.compile(search,caseSensitive?0:Pattern.CASE_INSENSITIVE);
				searchInElementRegEx(root,pattern,hits);
			} catch (PatternSyntaxException e) {}
		} else {
			if (!caseSensitive) search=search.toLowerCase();
			searchInElement(root,search,caseSensitive,hits);
		}

		return hits;
	}

	/**
	 * Markiert die Fundstellen einer Suche.
	 * @param owner	Übergeordnetes Element um optional den "Keine Treffer"-Dialog auszurichten
	 * @param search	Suchtext
	 * @param hits	Liste mit den Treffern
	 * @see #search(Component)
	 */
	private void processSearchResults(final Component owner, final String search, final List<Hit> hits) {
		textPane.getHighlighter().removeAllHighlights();

		if (hits==null || hits.isEmpty()) {
			MsgBox.info(owner,StatisticsBasePanel.viewersToolbarSearch,String.format(StatisticsBasePanel.viewersToolbarSearchNotFound,search));
			return;
		}

		final DefaultHighlighter.DefaultHighlightPainter highlightPainter=new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
		for (Hit hit: hits) {
			try {
				textPane.getHighlighter().addHighlight(hit.start,hit.end+1,highlightPainter);
			} catch (BadLocationException e) {}
		}
		textPane.setCaretPosition(hits.get(0).start);
	}

	@Override
	public void search(final Component owner) {
		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		final StatisticViewerSearchDialog dialog=new StatisticViewerSearchDialog(owner,lastSearchString);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK || dialog.getSearchString().isEmpty()) {
			textPane.getHighlighter().removeAllHighlights();
			return;
		}

		lastSearchString=dialog.getSearchString();

		final List<Hit> hits=getCaretPositions(lastSearchString,dialog.isCaseSensitive(),dialog.isRegularExpression());
		processSearchResults(owner,lastSearchString,hits);
	}

	@Override
	public void navigation(final JButton button) {
		treeScroller.setVisible(!treeScroller.isVisible());
		if (!treeScroller.isVisible()) {
			split.setDividerLocation(0);
			split.setDividerSize(0);
		} else {
			split.setDividerSize(splitDividerSize);
			updateTreeSize();
		}
	}

	/**
	 * Scrollt den Text zu einer bestimmten Zeile.
	 * @param line	1-basierte Zeilennummer (Werte &le;0 führen zu keiner Scroll-Veränderung)
	 */
	public void gotoStartOfLine(int line) {
		if (line<=0) return;

		if (textPane==null) {
			buildText();
			initTextPane();
			initDescriptionPane();
		}

		final Element root=textPane.getDocument().getDefaultRootElement();
		line=Math.max(line,1);
		line=Math.min(line,root.getElementCount());
		int startOfLineOffset=root.getElement(line-1).getStartOffset();
		textPane.setCaretPosition(startOfLineOffset);
	}

	/**
	 * Dokument für die Anzeige in {@link StatisticViewerText#textPane}
	 * @see StatisticViewerText#textPane
	 */
	private class FastDefaultStyledDocument extends DefaultStyledDocument {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=1626728203320204205L;

		/**
		 * Liste der anzuzeigenden Einzelelemente
		 */
		private List<ElementSpec> buffer;

		/**
		 * Konstruktor der Klasse
		 */
		public FastDefaultStyledDocument() {
			buffer=new ArrayList<>();
		}

		/**
		 * Standard-Element zur Darstellung eines Abschnitts-Endes
		 * @see #addText(AttributeSet, String)
		 */
		private final ElementSpec endTag=new ElementSpec(null,ElementSpec.EndTagType);

		/**
		 * Standard-Element zur Darstellung eines Abschnitts-Beginns
		 * @see #addText(AttributeSet, String)
		 */
		private final ElementSpec startTag=new ElementSpec(null,ElementSpec.StartTagType);

		/**
		 * Fügt ein neues Ausgabeelement zu der Liste der auszugebenden Elemente hinzu.
		 * @param attr	Formatierungsattribute
		 * @param text	Auszugebender Text
		 * @see #finalizeText()
		 */
		public void addText(final AttributeSet attr, final String text)  {
			buffer.add(endTag);
			buffer.add(startTag);
			buffer.add(new ElementSpec(attr,ElementSpec.ContentType,text.toCharArray(),0,text.length()));
		}

		/**
		 * Überträgt die in {@link #addText(AttributeSet, String)} gesammelten Elemente
		 * in das Dokument.
		 * @return	Liefert <code>true</code>, wenn die Elemente erfolgreich in das Dokument übertragen werden konnten
		 * @see #addText(AttributeSet, String)
		 */
		public boolean finalizeText() {
			try {
				insert(getLength(),buffer.toArray(ElementSpec[]::new));
			} catch (BadLocationException e) {
				return false;
			}
			return true;
		}
	}
}
