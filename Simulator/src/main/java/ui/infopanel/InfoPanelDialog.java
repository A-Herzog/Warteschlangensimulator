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
package ui.infopanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import systemtools.BaseDialog;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;

/**
 * Der von dieser Klasse zur Verfügung gestellte Dialog ermöglicht es einzustellen,
 * in welchen Dialogen {@link InfoPanel}-basierende Hinweise angezeigt werden sollen.
 * @author Alexander Herzog
 * @see InfoPanel
 */
public final class InfoPanelDialog extends BaseDialog {
	private static final long serialVersionUID = 1356255618875549024L;

	/** Darstellung der verfügbaren Themen */
	private final JTree tree;
	/** Checkboxen zur Aktivierung oder Deaktivierung der Hilfe-Einblendungen */
	private List<JCheckBox> checkBoxes;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param setup	Initiale Einstellungen für die ein- und auszublendenden Hinweise
	 * @see InfoPanel
	 * @see SetupData#hintDialogs
	 */
	public InfoPanelDialog(final Component owner, final String setup) {
		super(owner,Language.tr("HintsDialog.Title"));

		/* Inhaltsbereich vorbereiten */
		addUserButton(Language.tr("HintsDialog.Button.HideAll"),Images.INFO_PANEL_SETUP_HIDE.getURL());
		addUserButton(Language.tr("HintsDialog.Button.ShowAll"),Images.INFO_PANEL_SETUP_SHOW.getURL());
		getUserButton(0).setToolTipText(Language.tr("HintsDialog.Button.HideAll.Hint"));
		getUserButton(1).setToolTipText(Language.tr("HintsDialog.Button.ShowAll.Hint"));
		final JPanel content=createGUI(()->Help.topicModal(this,"Setup"));
		content.setLayout(new BorderLayout());

		/* Baumstruktur */
		content.add(new JScrollPane(tree=new JTree(new DefaultTreeModel(buildTree(setup)))),BorderLayout.CENTER);
		tree.setRootVisible(false);
		tree.setCellRenderer(new CheckBoxCellRenderer());

		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent event) {
				if (SwingUtilities.isRightMouseButton(event)) showGroupContextMenu(tree.getPathForLocation(event.getX(),event.getY()),event);
				if (SwingUtilities.isLeftMouseButton(event)) toggleCheckBox(tree.getPathForLocation(event.getX(),event.getY()));
			}
		});
		tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_SPACE || e.getKeyCode()==KeyEvent.VK_ENTER) toggleCheckBox(tree.getSelectionPath());
			}
		});
		for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,700);
		setResizable(true);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private void toggleCheckBox(final TreePath path) {
		if (path==null) return;
		final Object obj1=path.getLastPathComponent();
		if (!(obj1 instanceof DefaultMutableTreeNode)) return;
		final Object obj2=((DefaultMutableTreeNode)obj1).getUserObject();
		if (!(obj2 instanceof JCheckBox)) return;
		final JCheckBox checkBox=(JCheckBox)obj2;
		checkBox.setSelected(!checkBox.isSelected());
		tree.repaint();
	}

	private void showGroupContextMenu(final TreePath path, final MouseEvent event) {
		if (path==null) return;
		final Object obj1=path.getLastPathComponent();
		if (!(obj1 instanceof DefaultMutableTreeNode)) return;
		final Object obj2=((DefaultMutableTreeNode)obj1).getUserObject();
		if (!(obj2 instanceof String)) return;

		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;
		menu.add(item=new JMenuItem(Language.tr("HintsDialog.Button.HideAll"),Images.INFO_PANEL_SETUP_HIDE.getIcon()));
		item.addActionListener(l->changeGroup((DefaultMutableTreeNode)obj1,false));
		menu.add(item=new JMenuItem(Language.tr("HintsDialog.Button.ShowAll"),Images.INFO_PANEL_SETUP_SHOW.getIcon()));
		item.addActionListener(l->changeGroup((DefaultMutableTreeNode)obj1,true));

		menu.show((Component)event.getSource(),event.getX(),event.getY());
	}

	private void changeGroup(final DefaultMutableTreeNode group, final boolean select) {
		for (int i=0;i<group.getChildCount();i++) {
			final TreeNode child=group.getChildAt(i);
			if (!(child instanceof DefaultMutableTreeNode)) continue;
			final DefaultMutableTreeNode node=(DefaultMutableTreeNode)child;
			final Object obj=node.getUserObject();
			if (obj instanceof String) changeGroup(node,select);
			if (obj instanceof JCheckBox) ((JCheckBox)obj).setSelected(select);
		}
		tree.repaint();
	}

	private DefaultMutableTreeNode getParent(final DefaultMutableTreeNode root, final Map<String,DefaultMutableTreeNode> groups, final List<String> parts) {
		DefaultMutableTreeNode parent=root;

		for (int i=0;i<parts.size();i++) {
			final StringBuilder sb=new StringBuilder();
			sb.append(parts.get(0));
			for (int j=1;j<=i;j++) {sb.append("|"); sb.append(parts.get(j));}
			final String groupName=sb.toString();
			DefaultMutableTreeNode next=groups.get(groupName);
			if (next==null) groups.put(groupName,next=new DefaultMutableTreeNode(parts.get(i)));
			parent.add(next);
			parent=next;
		}

		return parent;

	}

	private TreeNode buildTree(final String setup) {
		final InfoPanel instance=InfoPanel.getInstance();
		final String saveSetup=instance.getSetup();
		instance.loadSetup(setup);

		checkBoxes=new ArrayList<>();

		final DefaultMutableTreeNode root=new DefaultMutableTreeNode();
		final Map<String,DefaultMutableTreeNode> groups=new HashMap<>();

		for (InfoPanel.Item item : instance.getItems()) {
			final List<String> parts=new ArrayList<>(Arrays.asList(item.getName().split("\\|")));
			final String checkBoxName=parts.remove(parts.size()-1).trim();

			DefaultMutableTreeNode parent=root;
			parent=getParent(root,groups,parts);

			final JCheckBox checkBox=new JCheckBox(checkBoxName,item.visible);
			parent.add(new DefaultMutableTreeNode(checkBox));
			checkBoxes.add(checkBox);
		}

		instance.loadSetup(saveSetup);

		return root;
	}

	@Override
	protected void userButtonClick(final int index, final JButton button) {
		if (checkBoxes!=null) for (JCheckBox checkBox: checkBoxes) checkBox.setSelected(index==1);
		tree.repaint();
	}

	/**
	 * Liefert nach dem Schließen des Dialogs die neuen Einstellungen für die ein- und auszublendenden Hinweise
	 * @return	Neue Einstellungen für die ein- und auszublendenden Hinweise
	 * @see SetupData#hintDialogs
	 */
	public String getData() {
		final InfoPanel instance=InfoPanel.getInstance();
		final String saveSetup=instance.getSetup();

		final List<InfoPanel.Item> items=instance.getItems();
		if (checkBoxes!=null) for (int i=0;i<Math.min(checkBoxes.size(),items.size());i++) items.get(i).visible=checkBoxes.get(i).isSelected();
		final String newSetup=instance.getSetup();

		instance.loadSetup(saveSetup);

		return newSetup;
	}

	/**
	 * Darstellung der Checkboxen in der Baumstruktur
	 * @see InfoPanelDialog#tree
	 * @see InfoPanelDialog#checkBoxes
	 */
	private class CheckBoxCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
		private static final long serialVersionUID = -5361367157516885457L;

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			final DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
			final Object obj=node.getUserObject();
			if (!(obj instanceof JCheckBox)) return super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
			final JCheckBox checkBox=(JCheckBox)obj;

			if (selected) {
				checkBox.setForeground(getTextSelectionColor());
				checkBox.setBackground(getBackgroundSelectionColor());
			} else {
				checkBox.setForeground(getTextNonSelectionColor());
				checkBox.setBackground(getBackgroundNonSelectionColor());
			}

			return checkBox;
		}
	}
}
