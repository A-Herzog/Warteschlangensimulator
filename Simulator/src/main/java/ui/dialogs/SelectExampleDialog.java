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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.examples.EditModelExamples;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.EditorPanel;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;

/**
 * Ermöglicht die Auswahl eines zu ladenden Beispiels über einen Dialog mit Vorschau
 * @author Alexander Herzog
 * @see EditModelExamples
 */
public final class SelectExampleDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4553266650939654301L;

	/** Baumstruktur in der die Beispielmodelle nach Themen gruppiert aufgelistet werden */
	private final JTree tree;
	/** Vorschaubereich für das ausgewählte Beispiel */
	private final EditorPanel viewer;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public SelectExampleDialog(final Component owner) {
		super(owner,Language.tr("SelectExampleWithPreview.Title"));

		/* GUI erstellen */
		final JPanel content=createGUI(()->Help.topicModal(this,"SelectExampleWithPreview"));
		content.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(content,InfoPanel.globalSelectExample);
		final JSplitPane main=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		content.add(main,BorderLayout.CENTER);

		/* Vorschau rechts */
		main.setRightComponent(viewer=new EditorPanel(this,null,true,false,false));
		viewer.setSavedViewsButtonVisible(false);
		viewer.setMinimumSize(new Dimension(400,0));

		/* Baumstruktur links */
		main.setLeftComponent(new JScrollPane(tree=new JTree(new DefaultTreeModel(buildTree()))));
		tree.setRootVisible(false);
		tree.getParent().setMinimumSize(new Dimension(200,0));
		tree.addTreeSelectionListener(e->viewer.setModel(getExample()));
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(Images.MODEL.getIcon());
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) && getExample()!=null) close(BaseDialog.CLOSED_BY_OK);
			}
		});
		for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);

		/* Split einstellen */
		main.setDividerLocation(0.2);

		/* Dialog starten */
		setMinSizeRespectingScreensize(1200,800);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erstellt die Baumstruktur, die alle Beispiele enthält.
	 * @return	Baumstruktur, die alle Beispiele enthält
	 */
	private TreeNode buildTree() {
		final DefaultMutableTreeNode root=new DefaultMutableTreeNode("");

		for (EditModelExamples.ExampleType type: EditModelExamples.ExampleType.values()) {
			final DefaultMutableTreeNode group=new DefaultMutableTreeNode(EditModelExamples.getGroupName(type));
			root.add(group);
			final List<String> names=EditModelExamples.getExampleNames(type);
			names.sort(null);
			for (String name: names) {
				final DefaultMutableTreeNode node=new DefaultMutableTreeNode(new ExampleData(name));
				group.add(node);
			}
		}

		return root;
	}

	@Override
	protected boolean checkData() {
		if (getExample()==null) {
			MsgBox.error(this,Language.tr("SelectExampleWithPreview.ErrorNoExampleSelected.Title"),Language.tr("SelectExampleWithPreview.ErrorNoExampleSelected.Info"));
			return false;
		}
		return true;
	}

	/**
	 * Liefert das ausgewählte Beispiel
	 * @return	Ausgewähltes Beispiel oder <code>null</code>, wenn kein Beispiel ausgewählt war
	 */
	public EditModel getExample() {
		final TreePath path=tree.getSelectionPath();
		if (path==null) return null;
		final Object last=path.getLastPathComponent();
		if (!(last instanceof DefaultMutableTreeNode)) return null;
		final DefaultMutableTreeNode node=(DefaultMutableTreeNode)last;
		final Object userObject=node.getUserObject();
		if (!(userObject instanceof ExampleData)) return null;

		return ((ExampleData)userObject).getModel();
	}

	/**
	 * Datensatz für ein Beispiel
	 * @see SelectExampleDialog#buildTree()
	 */
	private class ExampleData {
		/** Name des Beispiels */
		private final String name;
		/** Modell für das Beispiel */
		private EditModel model;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name des Beispiels
		 */
		public ExampleData(final String name) {
			this.name=name;
		}

		@Override
		public String toString() {
			return name;
		}

		/**
		 * Liefert das Modell für dieses Beispiel
		 * @return	Modell für dieses Beispiel
		 */
		public EditModel getModel() {
			if (model==null) {
				final int index=EditModelExamples.getExampleIndexFromName(name);
				model=EditModelExamples.getExampleByIndex(SelectExampleDialog.this,index);
			}
			return model;
		}
	}
}
