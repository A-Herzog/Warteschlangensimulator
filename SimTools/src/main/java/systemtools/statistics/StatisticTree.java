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

import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import systemtools.images.SimToolsImages;
import systemtools.statistics.StatisticViewer.ViewerType;

/**
 * Baumansicht für die Statistikanzeige
 * Diese Klasse kann nur innerhalb dieses Package verwendet werden.
 * @author Alexander Herzog
 */
public class StatisticTree extends JTree {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 1819707611627082842L;

	/** Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (zur Anzeige eines Kontextmenüs, welche den jeweiligen Befehl benennt; wird hier <code>null</code> übergeben, so erhält die Baumansicht kein Kontextmenü) */
	private final String commandLineCommand;

	/** Dateiname, der in dem Beispielkommandozeilen-Befehl für die Statistikdatei verwendet werden soll (wird <code>null</code> übergeben, so wird "data.xml" verwendet) */
	private String commandLineDataFileName;

	/**
	 * Konstruktor der Klasse <code>StatisticTree</code>
	 * @param commandLineCommand	Kommandozeilenbefehl, über den einzelne Statistikergebnisse abgerufen werden können (zur Anzeige eines Kontextmenüs, welche den jeweiligen Befehl benennt; wird hier <code>null</code> übergeben, so erhält die Baumansicht kein Kontextmenü)
	 * @param commandLineDataFileName	Dateiname, der in dem Beispielkommandozeilen-Befehl für die Statistikdatei verwendet werden soll (wird <code>null</code> übergeben, so wird "data.xml" verwendet)
	 */
	public StatisticTree(final String commandLineCommand, final String commandLineDataFileName) {
		super(new DefaultTreeModel(new DefaultMutableTreeNode()));
		this.commandLineCommand=commandLineCommand;
		this.commandLineDataFileName=commandLineDataFileName;
		setRootVisible(false);
		setCellRenderer(new StatisticTreeCellRenderer());
		addTreeSelectionListener(new TreeSelectionChanged());
		if (commandLineCommand!=null && !commandLineCommand.isEmpty()) addMouseListener(new TreeMouseListener());
	}

	/**
	 * Reagiert darauf, wenn in der Baumstruktur ein anderer Eintrag ausgewählt wurde.
	 * @see StatisticTree#nodeSelected(StatisticNode, DefaultMutableTreeNode)
	 */
	private final class TreeSelectionChanged implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			DefaultMutableTreeNode node;
			if (e==null || e.getNewLeadSelectionPath()==null) node=null; else node=(DefaultMutableTreeNode)e.getNewLeadSelectionPath().getLastPathComponent();

			if (node==null || node.getUserObject()==null) {nodeSelected(null,node); return;}

			if (!(node.getUserObject() instanceof StatisticNode)) nodeSelected(null,node); else nodeSelected((StatisticNode)(node.getUserObject()),node);
		}
	}


	/**
	 * Wird aufgerufen, wenn ein Eintrag im Baum ausgewählt wurde
	 * @param node	Objekt vom Typ <code>StatisticNode</code>, welches zu dem Eintrag im Baum gehört
	 * @param treeNode	Auswahälter Eintrag in der Baumstruktur
	 */
	protected void nodeSelected(StatisticNode node, DefaultMutableTreeNode treeNode) {}

	/**
	 * Wählt einen bestimmten Knoten im Baum aus
	 * @param node	Auszuwählender Knoten
	 * @return	Liefert <code>true</code> zurück, wenn der Eintrag selektiert wurde.
	 */
	public final boolean selectNode(DefaultMutableTreeNode node) {
		if (node==null) return false;
		TreePath path=new TreePath(node.getPath());
		setSelectionPath(path);
		scrollPathToVisible(path);
		return true;
	}

	/**
	 * Wenn im Kontextmenü der Baumstruktur Befehle für die Kommandozeile angeboten werden
	 * sollen, über die die jeweilige Information über die Kommandozeile abgerufen werden
	 * kann, so muss hier ein Beispieldateiname für die zu verwendende Statistikdatei
	 * angegeben werden.
	 * @param commandLineDataFileName	Dateiname für die Statistikdatei der in Beispiel-Kommandozeilen-Befehlen angezeigt werden soll
	 */
	public void setDataFileName(final String commandLineDataFileName) {
		this.commandLineDataFileName=commandLineDataFileName;
	}

	/**
	 * Zeigt ein Popup-Menü zu einem Baumeintrag an
	 * @param x	x-Position des Menüs (relativ zum Baum)
	 * @param y	y-Position des Menüs (relativ zum Baum)
	 * @param objName	Textbezeichnung des Baumeintrags
	 * @param type	Art des gewählten Eintrags
	 */
	private void buildPopup(final int x, final int y, final String objName, final ViewerType type) {
		final JPopupMenu menu=new JPopupMenu();
		final JMenuItem item=new JMenuItem(StatisticsBasePanel.treeCopyParameter);
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
		menu.show(StatisticTree.this,x,y);
	}

	/**
	 * Liefert die vollständige Bezeichnung für einen Baumeintrag
	 * @param node	Baumeintrag
	 * @return	Vollständige Bezeichnung
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
	 * @param x	x-Position des Menüs (relativ zum Baum)
	 * @param y	y-Position des Menüs (relativ zum Baum)
	 * @return	Zweielementiges Array: Textbezeichnung des Baumeintrags, Art des gewählten Eintrags
	 * @see #buildPopup(int, int, String, ViewerType)
	 */
	private Object[] dataAtPosition(int x, int y) {
		int selRow=getRowForLocation(x,y);
		if (selRow<0) return null;
		TreePath selPath=getPathForLocation(x,y);
		Object obj=selPath.getLastPathComponent();
		if (!(obj instanceof DefaultMutableTreeNode)) return null;
		DefaultMutableTreeNode node=(DefaultMutableTreeNode)obj;
		selectNode(node);
		obj=node.getUserObject();
		if (!(obj instanceof StatisticNode)) return null;
		StatisticNode statNode=(StatisticNode)obj;
		if (statNode.viewer.length==0) return null;
		StatisticViewer viewer=((StatisticNode)obj).viewer[0];
		final ViewerType type=viewer.getType();
		if (type!=StatisticViewer.ViewerType.TYPE_TEXT && type!=StatisticViewer.ViewerType.TYPE_TABLE && type!=StatisticViewer.ViewerType.TYPE_IMAGE) return null;
		return new Object[]{getNodeFullName(statNode),type};
	}

	/**
	 * Reagiert auf Mausklicks auf Baumeinträge und zeigt ggf. ein Kontextmenü an.
	 * @see StatisticTree#dataAtPosition(int, int)
	 * @see StatisticTree#buildPopup(int, int, String, ViewerType)
	 */
	private class TreeMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				Object[] data=dataAtPosition(e.getX(),e.getY());
				if (data!=null && commandLineCommand!=null) {
					buildPopup(e.getX(),e.getY(),(String)data[0],(ViewerType)data[1]);
					e.consume();
					return;
				}
			}
		}
	}
}