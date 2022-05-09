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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import systemtools.images.SimToolsImages;
import systemtools.statistics.StatisticViewer.ViewerType;

/**
 * Baumansicht f�r die Statistikanzeige
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
public class StatisticTree extends JTree {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1819707611627082842L;

	/** Kommandozeilenbefehl, �ber den einzelne Statistikergebnisse abgerufen werden k�nnen (zur Anzeige eines Kontextmen�s, welche den jeweiligen Befehl benennt; wird hier <code>null</code> �bergeben, so erh�lt die Baumansicht kein Kontextmen�) */
	private final String commandLineCommand;

	/** Dateiname, der in dem Beispielkommandozeilen-Befehl f�r die Statistikdatei verwendet werden soll (wird <code>null</code> �bergeben, so wird "data.xml" verwendet) */
	private String commandLineDataFileName;

	/** Liefert die Liste der Bookmarks (kann <code>null</code> sein, dann wird das Bookmarks-System deaktiviert) */
	private final Supplier<List<String>> getBookmarks;

	/** Speichert die ver�nderte Liste der Bookmarks */
	private final Consumer<List<String>> setBookmarks;

	/**
	 * Konstruktor der Klasse <code>StatisticTree</code>
	 * @param commandLineCommand	Kommandozeilenbefehl, �ber den einzelne Statistikergebnisse abgerufen werden k�nnen (zur Anzeige eines Kontextmen�s, welche den jeweiligen Befehl benennt; wird hier <code>null</code> �bergeben, so erh�lt die Baumansicht kein Kontextmen�)
	 * @param commandLineDataFileName	Dateiname, der in dem Beispielkommandozeilen-Befehl f�r die Statistikdatei verwendet werden soll (wird <code>null</code> �bergeben, so wird "data.xml" verwendet)
	 * @param bookmarkColor	Farbe f�r als Bookmark hervorgehobene Eintr�ge
	 * @param getBookmarks	Liefert die Liste der Bookmarks (kann <code>null</code> sein, dann wird das Bookmarks-System deaktiviert)
	 * @param setBookmarks	Speichert die ver�nderte Liste der Bookmarks
	 */
	public StatisticTree(final String commandLineCommand, final String commandLineDataFileName, final Color bookmarkColor, final Supplier<List<String>> getBookmarks, final Consumer<List<String>> setBookmarks) {
		super(new DefaultTreeModel(new DefaultMutableTreeNode()));
		this.commandLineCommand=commandLineCommand;
		this.commandLineDataFileName=commandLineDataFileName;
		this.getBookmarks=getBookmarks;
		this.setBookmarks=setBookmarks;

		setRootVisible(false);
		setCellRenderer(new StatisticTreeCellRenderer(bookmarkColor,getBookmarks));
		addTreeSelectionListener(new TreeSelectionChanged());
		if (commandLineCommand!=null && !commandLineCommand.isEmpty()) addMouseListener(new TreeMouseListener());
		setDragEnabled(true);
		setTransferHandler(new StatisticTreeTransferHandler());
	}

	/**
	 * Aktuell gew�hlter {@link StatisticNode}
	 * @see TreeSelectionChanged
	 * @see #fireNodeSelected()
	 */
	private StatisticNode lastSelectedNode;

	/**
	 * Aktuell gew�hlter {@link DefaultMutableTreeNode}
	 * @see TreeSelectionChanged
	 * @see #fireNodeSelected()
	 */
	private DefaultMutableTreeNode lastSelectedTreeNode;

	/**
	 * Reagiert darauf, wenn in der Baumstruktur ein anderer Eintrag ausgew�hlt wurde.
	 * @see StatisticTree#nodeSelected(StatisticNode, DefaultMutableTreeNode)
	 */
	private final class TreeSelectionChanged implements TreeSelectionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public TreeSelectionChanged() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node;
			if (e==null || e.getNewLeadSelectionPath()==null) node=null; else node=(DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent();

			if (node==null || node.getUserObject()==null) {
				nodeSelected(null,node);
				lastSelectedNode=null;
				lastSelectedTreeNode=node;
				return;
			}

			if (!(node.getUserObject() instanceof StatisticNode)) {
				nodeSelected(null,node);
				lastSelectedNode=null;
				lastSelectedTreeNode=node;
			} else {
				nodeSelected((StatisticNode)(node.getUserObject()),node);
				lastSelectedNode=(StatisticNode)(node.getUserObject());
				lastSelectedTreeNode=node;
			}
		}
	}

	/**
	 * L�st die Aktionen vergleichbar mit dem Klicken auf einen Baumeintrag aus.
	 * @see #nodeSelected
	 */
	public void fireNodeSelected() {
		nodeSelected(lastSelectedNode,lastSelectedTreeNode);
	}

	/**
	 * Wird aufgerufen, wenn ein Eintrag im Baum ausgew�hlt wurde
	 * @param node	Objekt vom Typ <code>StatisticNode</code>, welches zu dem Eintrag im Baum geh�rt
	 * @param treeNode	Auswah�lter Eintrag in der Baumstruktur
	 */
	protected void nodeSelected(StatisticNode node, DefaultMutableTreeNode treeNode) {}

	/**
	 * W�hlt einen bestimmten Knoten im Baum aus
	 * @param node	Auszuw�hlender Knoten
	 * @return	Liefert <code>true</code> zur�ck, wenn der Eintrag selektiert wurde.
	 */
	public final boolean selectNode(DefaultMutableTreeNode node) {
		if (node==null) return false;
		final TreePath path=new TreePath(node.getPath());
		setSelectionPath(path);
		scrollPathToVisible(path);
		return true;
	}

	/**
	 * Wenn im Kontextmen� der Baumstruktur Befehle f�r die Kommandozeile angeboten werden
	 * sollen, �ber die die jeweilige Information �ber die Kommandozeile abgerufen werden
	 * kann, so muss hier ein Beispieldateiname f�r die zu verwendende Statistikdatei
	 * angegeben werden.
	 * @param commandLineDataFileName	Dateiname f�r die Statistikdatei der in Beispiel-Kommandozeilen-Befehlen angezeigt werden soll
	 */
	public void setDataFileName(final String commandLineDataFileName) {
		this.commandLineDataFileName=commandLineDataFileName;
	}

	/**
	 * Zeigt ein Popup-Men� zu einem Baumeintrag an
	 * @param x	x-Position des Men�s (relativ zum Baum)
	 * @param y	y-Position des Men�s (relativ zum Baum)
	 * @param bookmarkName	Bezeichnung des Baumeintrags f�r die Bookmarksliste
	 * @param objName	Textbezeichnung des Baumeintrags
	 * @param type	Art des gew�hlten Eintrags
	 */
	private void buildPopup(final int x, final int y, final String bookmarkName, final String objName, final ViewerType type) {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		if (commandLineCommand!=null) {
			item=new JMenuItem(StatisticsBasePanel.treeCopyParameter);
			item.setToolTipText(StatisticsBasePanel.treeCopyParameterHint);
			item.setIcon(SimToolsImages.COMMAND_LINE.getIcon());
			item.addActionListener(e->{
				String text=commandLineCommand;
				text+=" \""+objName+"\"";
				if (commandLineDataFileName!=null && !commandLineDataFileName.trim().isEmpty()) text+=" \""+commandLineDataFileName+"\""; else text+=" \"data.xml\"";
				if (type==StatisticViewer.ViewerType.TYPE_TEXT) text+=" \"data.docx\"";
				if (type==StatisticViewer.ViewerType.TYPE_TABLE) text+=" \"data.xlsx\"";
				if (type==StatisticViewer.ViewerType.TYPE_IMAGE) text+=" \"data.png\"";
				getToolkit().getSystemClipboard().setContents(new StringSelection(text),null);
			});
			menu.add(item);
		}

		if (getBookmarks!=null && setBookmarks!=null && getBookmarks.get()!=null) {
			final List<String> bookmarks=getBookmarks.get();
			final boolean isBookmarked=bookmarks.contains(bookmarkName);
			item=new JMenuItem(isBookmarked?StatisticsBasePanel.treeBookmarkSetOff:StatisticsBasePanel.treeBookmarkSetOn);
			item.setToolTipText(isBookmarked?StatisticsBasePanel.treeBookmarkSetOffHint:StatisticsBasePanel.treeBookmarkSetOnHint);
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
			item.setIcon(SimToolsImages.STATISTICS_BOOKMARK.getIcon());
			item.addActionListener(e->toggleBookmark());
			menu.add(item);

			if (bookmarks.size()>0) {
				item=new JMenuItem(StatisticsBasePanel.treeBookmarkJump);
				item.setToolTipText(	StatisticsBasePanel.treeBookmarkJumpHint);
				item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,InputEvent.CTRL_DOWN_MASK));
				item.setIcon(SimToolsImages.HELP_NEXT.getIcon());
				item.addActionListener(e->jumpToNextBookmark());
				menu.add(item);
			}
		}


		menu.show(StatisticTree.this,x,y);
	}

	/**
	 * Liefert die vollst�ndige Bezeichnung f�r einen Baumeintrag
	 * @param node	Baumeintrag
	 * @return	Vollst�ndige Bezeichnung
	 */
	private String getNodeFullName(StatisticNode node) {
		String name="";
		while (node!=null && node.name!=null) {
			String part=node.name;
			if (node.viewer.length>0) {
				switch (node.viewer[0].getType()) {
				case TYPE_TEXT : part+=" ("+StatisticsBasePanel.typeText+")"; break;
				case TYPE_TABLE : part+=" ("+StatisticsBasePanel.typeTable+")"; break;
				case TYPE_IMAGE : part+=" ("+StatisticsBasePanel.typeImage+")"; break;
				default: break; /* Report, Sub */
				}
			}
			if (name.isEmpty()) name=part; else name=part+" - "+name;
			node=node.getParent();
		}
		return name;
	}

	/**
	 * Bestimmt den Baumeintrag an einer bestimmten Stelle
	 * @param x	x-Position des Men�s (relativ zum Baum)
	 * @param y	y-Position des Men�s (relativ zum Baum)
	 * @return	3-elementiges Array: Baumeintrag (StatisticNode), Textbezeichnung des Baumeintrags, Art des gew�hlten Eintrags
	 * @see #buildPopup(int, int, String, String, ViewerType)
	 */
	private Object[] dataAtPosition(int x, int y) {
		int selRow=getRowForLocation(x,y);
		if (selRow<0) return null;
		final TreePath selPath=getPathForLocation(x,y);
		Object obj=selPath.getLastPathComponent();
		if (!(obj instanceof DefaultMutableTreeNode)) return null;
		DefaultMutableTreeNode node=(DefaultMutableTreeNode)obj;
		selectNode(node);
		obj=node.getUserObject();
		if (!(obj instanceof StatisticNode)) return null;
		final StatisticNode statNode=(StatisticNode)obj;
		if (statNode.viewer.length==0) return null;
		StatisticViewer viewer=((StatisticNode)obj).viewer[0];
		final ViewerType type=viewer.getType();
		if (type!=StatisticViewer.ViewerType.TYPE_TEXT && type!=StatisticViewer.ViewerType.TYPE_TABLE && type!=StatisticViewer.ViewerType.TYPE_IMAGE) return null;
		return new Object[]{statNode,getNodeFullName(statNode),type};
	}

	/**
	 * Liefert den aktuell gew�hlten Baumeintrag.
	 * @return	Gew�hlter Baumeintrag oder <code>null</code>, wenn kein Eintrag gew�hlt ist
	 */
	private DefaultMutableTreeNode getSelectedTreeNode() {
		final TreePath path=getSelectionPath();
		if (path==null) return null;
		final Object treeNode=path.getLastPathComponent();
		if (!(treeNode instanceof DefaultMutableTreeNode)) return null;
		return (DefaultMutableTreeNode)treeNode;
	}

	/**
	 * Geht von einem Baumeintrag zum n�chsten oder beginnt wieder oben.
	 * @param currentTreeNode	Aktueller Eintrag
	 * @return	N�chster Eintrag
	 */
	private DefaultMutableTreeNode getNextTreeNode(final DefaultMutableTreeNode currentTreeNode) {
		/* Erstes Unterelement des aktuellen Eintrags */
		if (currentTreeNode.getChildCount()>0) {
			return (DefaultMutableTreeNode)currentTreeNode.getChildAt(0);
		}

		/* N�chster Eintrag auf derselben Ebene */
		DefaultMutableTreeNode next=currentTreeNode.getNextSibling();
		if (next!=null) return next;

		/* N�chster Eintrag auf einer der h�heren Ebenen */
		DefaultMutableTreeNode parent=(DefaultMutableTreeNode)currentTreeNode.getParent();
		while (parent!=null) {
			next=parent.getNextSibling();
			if (next!=null) return next;
			parent=(DefaultMutableTreeNode)parent.getParent();
		}

		/* Neustart am Anfang */
		return (DefaultMutableTreeNode)getModel().getRoot();
	}

	/**
	 * W�hlt vom aktuellen Baumeintrag aus den n�chsten markierten Eintrag
	 */
	public void jumpToNextBookmark() {
		final List<String> bookmarks=getBookmarks.get();

		DefaultMutableTreeNode selectedTreeNode=getSelectedTreeNode();
		if (selectedTreeNode==null) selectedTreeNode=(DefaultMutableTreeNode)getModel().getRoot();

		DefaultMutableTreeNode node=getNextTreeNode(selectedTreeNode);
		while (node!=selectedTreeNode) {
			if (node.getUserObject() instanceof StatisticNode) {
				final StatisticNode statisticNode=(StatisticNode)node.getUserObject();
				if (statisticNode.isBookmark(bookmarks)) {
					setSelectionPath(new TreePath(node.getPath()));
					fireNodeSelected();
					return;
				}
			}
			node=getNextTreeNode(node);
		}
	}

	/**
	 * Schaltet beim aktuellen Eintrag zwischen markiert und nicht markiert um.
	 */
	public void toggleBookmark() {
		final DefaultMutableTreeNode treeNode=getSelectedTreeNode();
		if (treeNode==null) return;
		if (!(treeNode.getUserObject() instanceof StatisticNode)) return;
		final StatisticNode statisticNode=(StatisticNode)treeNode.getUserObject();
		if (statisticNode.viewer==null || statisticNode.viewer.length==0) return;
		final String bookmarkName=statisticNode.getBookmarkName();

		final List<String> bookmarks=new ArrayList<>(getBookmarks.get());
		final boolean isBookmarked=bookmarks.contains(bookmarkName);

		if (isBookmarked) {
			final int index=bookmarks.indexOf(bookmarkName);
			bookmarks.remove(index);
		} else {
			bookmarks.add(bookmarkName);
		}
		setBookmarks.accept(bookmarks);
		invalidate();
	}

	/**
	 * Reagiert auf Mausklicks auf Baumeintr�ge und zeigt ggf. ein Kontextmen� an.
	 * @see StatisticTree#dataAtPosition(int, int)
	 * @see StatisticTree#buildPopup(int, int, String, String, ViewerType)
	 */
	private class TreeMouseListener extends MouseAdapter {
		/**
		 * Konstruktor der Klasse
		 */
		public TreeMouseListener() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				final Object[] data=dataAtPosition(e.getX(),e.getY());
				if (data!=null) {
					if (commandLineCommand!=null || (getBookmarks!=null && setBookmarks!=null && getBookmarks.get()!=null)) {
						buildPopup(e.getX(),e.getY(),((StatisticNode)data[0]).getBookmarkName(),(String)data[1],(ViewerType)data[2]);
						e.consume();
						return;
					}
				}
			}
		}
	}

	/**
	 * Erm�glicht es, Daten von einzelnen Viewern per Drag&amp;Drop
	 * aus der Baumstruktur in andere Anwendungen zu �bertragen.
	 */
	private class StatisticTreeTransferHandler extends TransferHandler {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID=-1581460374664214749L;

		/**
		 * Konstruktor der Klasse
		 */
		public StatisticTreeTransferHandler() {
			/*
			 * Wird nur ben�tigt, um einen JavaDoc-Kommentar f�r diesen (impliziten) Konstruktor
			 * setzen zu k�nnen, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			/* Viewer finden */
			final TreePath path=getSelectionPath();
			if (path==null || path.getPathCount()==0) return null;
			final Object obj=path.getLastPathComponent();
			if (!(obj instanceof DefaultMutableTreeNode)) return null;
			final Object userObj=((DefaultMutableTreeNode)obj).getUserObject();
			if (!(userObj instanceof StatisticNode)) return null;
			final StatisticViewer[] viewers=((StatisticNode)userObj).viewer;
			if (viewers==null || viewers.length!=1) return null;
			final StatisticViewer viewer=viewers[0];

			/* Transferobjekt generieren */
			if (!viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_COPY)) return null;
			final Transferable transferable=viewer.getTransferable();
			if (transferable==null) return null;

			return transferable;
		}
	}
}