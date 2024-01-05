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
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.editmodel.EditModelDark;
import simulator.examples.EditModelExamples;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.EditorPanel;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.tools.FlatLaFHelper;

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

	/** Label zur Anzeige der Anzahl an Beispielmodellen */
	private final JLabel topLabel;

	/** Baumstruktur in der die Beispielmodelle nach Themen gruppiert aufgelistet werden */
	private final JTree tree;

	/** Vorschaubereich für das ausgewählte Beispiel */
	private final EditorPanel viewer;

	/** Textbereich zur Anzeige der Beschreibung für das Beispiel */
	private final JTextPane info;

	/** Zeichenflächen-Zoomfaktor beim Aufruf des Dialogs */
	private final double lastZoom;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public SelectExampleDialog(final Component owner) {
		super(owner,Language.tr("SelectExampleWithPreview.Title"));

		lastZoom=SetupData.getSetup().lastZoom;

		/* GUI erstellen */
		final JPanel contentOuter=createGUI(()->Help.topicModal(this,"SelectExampleWithPreview"));

		contentOuter.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(contentOuter,InfoPanel.globalSelectExample);
		final JPanel content=new JPanel(new BorderLayout());
		contentOuter.add(content,BorderLayout.CENTER);

		/* Infobereich oben */
		final JPanel topArea=new JPanel();
		topArea.setLayout(new BoxLayout(topArea,BoxLayout.PAGE_AXIS));
		content.add(topArea,BorderLayout.NORTH);

		JPanel line;

		topArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("SelectExampleWithPreview.KeyWord")+":"));
		final JComboBox<String> keyWordSelect=new JComboBox<>(EditModelExamples.ExampleKeyWord.getNames());
		line.add(keyWordSelect);
		topArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(topLabel=new JLabel());

		/* Hauptbereich */
		final JSplitPane main=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		content.add(main,BorderLayout.CENTER);

		/* Rechte Seite */
		final JPanel right=new JPanel(new BorderLayout());
		main.setRightComponent(right);

		/* Vorschau */
		right.add(viewer=new EditorPanel(this,null,true,true,false,false),BorderLayout.CENTER);
		viewer.setSavedViewsButtonVisible(false);
		viewer.setMinimumSize(new Dimension(400,0));
		viewer.setZoom(0.8);

		/* Infotext */
		final JScrollPane infoScroll=new JScrollPane(info=new JTextPane());
		infoScroll.setMinimumSize(new Dimension(0,80));
		infoScroll.setMaximumSize(new Dimension(10000,80));
		infoScroll.setSize(0,150);
		infoScroll.setPreferredSize(new Dimension(0,80));
		infoScroll.setBorder(BorderFactory.createEmptyBorder());
		right.add(infoScroll,BorderLayout.SOUTH);
		info.setEditable(false);
		info.setHighlighter(null);
		info.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
		info.setBackground(right.getBackground());

		/* Baumstruktur links */
		main.setLeftComponent(new JScrollPane(tree=new JTree()));
		tree.setRootVisible(false);
		tree.getParent().setMinimumSize(new Dimension(200,0));
		tree.addTreeSelectionListener(e->{
			viewer.setModel(getExample());
			info.setText(getExampleInfo());
		});
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(Images.MODEL.getIcon());
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) && getExample()!=null) close(BaseDialog.CLOSED_BY_OK);
			}
		});
		tree.setModel(new DefaultTreeModel(buildTree(null)));
		for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);

		/* Listener zur Auswahl des Schlüsselwortes */
		keyWordSelect.addActionListener(e->{
			final int index=keyWordSelect.getSelectedIndex();
			EditModelExamples.ExampleKeyWord keyWord=(index==0)?null:EditModelExamples.ExampleKeyWord.values()[index-1];
			tree.setModel(new DefaultTreeModel(buildTree(keyWord)));
			for (int i=0;i<tree.getRowCount();i++) tree.expandRow(i);
		});

		/* Split einstellen */
		main.setDividerLocation(0.2);

		/* Dialog starten */
		setMinSizeRespectingScreensize(1440,960);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erstellt die Baumstruktur, die alle Beispiele enthält.
	 * @param keyWord	Schlüsselwort, welches ein Beispiel enthalten muss, um in die Auflistung aufgenommen zu werden (kann <code>null</code> sein für "alle Beispiele")
	 * @return	Baumstruktur, die alle Beispiele enthält
	 */
	private TreeNode buildTree(final EditModelExamples.ExampleKeyWord keyWord) {
		int exampleCount=0;
		final DefaultMutableTreeNode root=new DefaultMutableTreeNode("");

		for (EditModelExamples.ExampleType type: EditModelExamples.ExampleType.values()) {
			final List<String> names=EditModelExamples.getExampleNames(type,keyWord);
			if (names.size()==0) continue;
			names.sort(null);
			final DefaultMutableTreeNode group=new DefaultMutableTreeNode(EditModelExamples.getGroupName(type));
			root.add(group);
			for (String name: names) {
				final DefaultMutableTreeNode node=new DefaultMutableTreeNode(new ExampleData(name));
				group.add(node);
				exampleCount++;
			}
		}

		topLabel.setText(String.format(Language.tr("SelectExampleWithPreview.ExampleCountInfo"),exampleCount));

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
	 * Liefert das {@link ExampleData}-Objekt für das ausgewählte Beispiel.
	 * @return	{@link ExampleData}-Objekt für das ausgewählte Beispiel oder <code>null</code>, wenn kein Beispiel ausgewählt war
	 */
	private ExampleData getSelectedExampleData() {
		final TreePath path=tree.getSelectionPath();
		if (path==null) return null;
		final Object last=path.getLastPathComponent();
		if (!(last instanceof DefaultMutableTreeNode)) return null;
		final DefaultMutableTreeNode node=(DefaultMutableTreeNode)last;
		final Object userObject=node.getUserObject();
		if (!(userObject instanceof ExampleData)) return null;

		return (ExampleData)userObject;
	}

	/**
	 * Liefert das ausgewählte Beispiel.
	 * @return	Ausgewähltes Beispiel oder <code>null</code>, wenn kein Beispiel ausgewählt war
	 */
	public EditModel getExample() {
		final ExampleData exampleData=getSelectedExampleData();
		if (exampleData==null) return null;
		return exampleData.getModel();
	}

	/**
	 * Liefert die Beschreibung für das ausgewählte Beispiel.
	 * @return	Beschreibung für das ausgewähltes Beispiel oder <code>null</code>, wenn kein Beispiel ausgewählt war
	 */
	private String getExampleInfo() {
		final ExampleData exampleData=getSelectedExampleData();
		if (exampleData==null) return "";
		return exampleData.getInfo();

	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b==false) {
			final SetupData setup=SetupData.getSetup();
			setup.lastZoom=lastZoom;
			setup.saveSetup();
		}
	}

	/**
	 * Datensatz für ein Beispiel
	 * @see SelectExampleDialog#buildTree(simulator.examples.EditModelExamples.ExampleKeyWord)
	 */
	private class ExampleData {
		/** Name des Beispiels */
		private final String name;
		/** Modell für das Beispiel */
		private EditModel model;
		/** Infotext für das Beispiel */
		private String info;

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
		 * Liefert das Modell für dieses Beispiel.
		 * @return	Modell für dieses Beispiel
		 */
		public EditModel getModel() {
			if (model==null) {
				final int index=EditModelExamples.getExampleIndexFromName(name);
				model=EditModelExamples.getExampleByIndex(SelectExampleDialog.this,index);
				if (FlatLaFHelper.isDark()) EditModelDark.processModel(model,EditModelDark.ColorMode.LIGHT,EditModelDark.ColorMode.DARK);
			}
			return model;
		}

		/**
		 * Liefert den Beschreibungstext für dieses Beispiel.
		 * @return	Beschreibungstext für dieses Beispiel
		 */
		public String getInfo() {
			if (info==null) {
				final int index=EditModelExamples.getExampleIndexFromName(name);
				info=EditModelExamples.getExampleInfoByIndex(SelectExampleDialog.this,index);
			}
			return info;
		}
	}
}
