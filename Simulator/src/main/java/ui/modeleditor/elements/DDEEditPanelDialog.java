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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import language.Language;
import net.dde.DDEConnect;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;

/**
 * Zeigt einen Dialog zur Auswahl einer über DDE erreichbaren Tabelle an.
 * @author Alexander Herzog
 * @see DDEEditPanel
 */
public class DDEEditPanelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2267758592237360567L;

	private JTree tree;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param list	Zuordnung von Arbeitsmappen und Tabellen, siehe {@link DDEConnect#listTables()}
	 * @param lastWorkbook	Zuvor gewählte Arbeitsmappe
	 * @param lastTable	Zuvor gewählte Tabelle
	 * @param helpRunnable	Hilfe-Runnable
	 */
	public DDEEditPanelDialog(final Component owner, final Map<String,List<String>> list, final String lastWorkbook, final String lastTable, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.DDE.Select.Dialog.Title"));

		/* GUI */

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());

		/* Top */

		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("Surface.DDE.Select.Dialog.Info")));

		/* Center */

		DefaultMutableTreeNode select=null;
		final DefaultMutableTreeNode root=new DefaultMutableTreeNode();
		JLabel label;
		for (final Map.Entry<String,List<String>> entry: list.entrySet()) {
			final DefaultMutableTreeNode workbook=new DefaultMutableTreeNode();
			workbook.setUserObject(label=new JLabel(entry.getKey()));
			label.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
			root.add(workbook);
			for (String s: entry.getValue()) {
				final DefaultMutableTreeNode table=new DefaultMutableTreeNode();
				table.setUserObject(label=new JLabel(s));
				label.setIcon(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon());
				workbook.add(table);
				if (select==null || (lastWorkbook!=null && lastWorkbook.equalsIgnoreCase(entry.getKey()) && lastTable!=null && lastTable.equalsIgnoreCase(s))) select=table;
			}
		}

		content.add(new JScrollPane(tree=new JTree(root)));
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = -4397512333587938583L;
			@Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)value).getUserObject() instanceof JLabel) {
					final JLabel label=((JLabel)((DefaultMutableTreeNode)value).getUserObject());
					super.getTreeCellRendererComponent(tree,label.getText(),sel,expanded,leaf,row,hasFocus);
					if (label.getIcon()!=null) setIcon(label.getIcon());
				} else {
					super.getTreeCellRendererComponent(tree,value,sel,expanded,leaf,row,hasFocus);
				}
				return this;
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
					final TreePath path=tree.getSelectionPath();
					if (path!=null && path.getPathCount()==3) close(BaseDialog.CLOSED_BY_OK);
				}
			}
		});
		for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);
		if (select!=null) tree.setSelectionPath(new TreePath(select.getPath()));

		/* Start */

		setMinSizeRespectingScreensize(400,600);
		setSizeRespectingScreensize(400,600);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected boolean checkData() {
		final TreePath path=tree.getSelectionPath();
		if (path==null || path.getPathCount()!=3) {
			MsgBox.error(this,Language.tr("Surface.DDE.Select.Dialog.ErrorTitle"),Language.tr("Surface.DDE.Select.Dialog.ErrorInfo"));
			return false;
		}

		return true;
	}

	/**
	 * Liefert, wenn der Dialog per "Ok" geschlossen wurde, die gewählte Arbeitsmappe.
	 * @return	Gewählte Arbeitsmappe
	 */
	public String getSelectedWorkbook() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return "";
		final TreePath path=tree.getSelectionPath();
		return ((JLabel)(((DefaultMutableTreeNode)path.getPathComponent(1)).getUserObject())).getText();
	}

	/**
	 * Liefert, wenn der Dialog per "Ok" geschlossen wurde, die gewählte Tabelle.
	 * @return	Gewählte Tabelle
	 */
	public String getSelectedTable() {
		if (getClosedBy()!=BaseDialog.CLOSED_BY_OK) return "";
		final TreePath path=tree.getSelectionPath();
		return ((JLabel)(((DefaultMutableTreeNode)path.getPathComponent(2)).getUserObject())).getText();
	}
}