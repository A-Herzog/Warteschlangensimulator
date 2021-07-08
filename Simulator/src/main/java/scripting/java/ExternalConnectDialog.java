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
package scripting.java;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.tools.FlatLaFHelper;

/**
 * Über diesen Dialog können die verfügbaren Klassen und Methoden
 * zur Einbindung in das System in einem Verzeichnis aufgelistet werden.
 * @author Alexander Herzog
 * @see ExternalConnect
 */
public class ExternalConnectDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-5507666377673607544L;

	/**
	 * Eingabefeld für das Verzeichnis
	 */
	private final JTextField folder;

	/**
	 * Zuletzt in {@link #updateTree(boolean)} verwendetes Verzeichnis.
	 */
	private String lastFolder;

	/**
	 * Baumstruktur zur Anzeige der gefundenen Klassen und Methoden darin.
	 */
	final JTree tree;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param initialFolder	Anfänglich im Dialog anzuzeigendes Verzeichnis
	 */
	public ExternalConnectDialog(final Component owner, final String initialFolder) {
		super(owner,Language.tr("ExternalConnect.Dialog.Title"));

		addUserButton(Language.tr("ExternalConnect.Dialog.Compile"),Images.PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVA.getIcon());
		getUserButton(0).setToolTipText(Language.tr("ExternalConnect.Dialog.Compile.Tooltip"));
		final JPanel content=createGUI(()->Help.topicModal(this,"ExternalConnect"));
		content.	setLayout(new BorderLayout());

		JPanel line;
		JPanel sub;
		JButton button;

		/* Verzeichnisauswahl */
		content.add(line=new JPanel(new BorderLayout()),BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("ExternalConnect.Dialog.Folder")+": "),BorderLayout.WEST);
		line.add(folder=new JTextField((initialFolder==null)?"":initialFolder),BorderLayout.CENTER);
		folder.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {updateTree(false);}
			@Override public void keyReleased(KeyEvent e) {updateTree(false);}
			@Override public void keyPressed(KeyEvent e) {updateTree(false);}
		});
		line.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.EAST);
		sub.add(button=new JButton(Images.GENERAL_SELECT_FOLDER.getIcon()));
		button.setToolTipText(Language.tr("ExternalConnect.Dialog.Folder.Tooltip"));
		button.addActionListener(e->{
			final JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			final String oldFolder=folder.getText().trim();
			if (!oldFolder.trim().isEmpty() && new File(oldFolder).isDirectory()) fc.setCurrentDirectory(new File(oldFolder));
			fc.setDialogTitle(Language.tr("ExternalConnect.Dialog.Folder.Tooltip"));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			folder.setText(fc.getSelectedFile().toString());
			updateTree(false);
		});
		sub.add(button=new JButton(Images.GENERAL_UPDATE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("ExternalConnect.Dialog.RereadFolder.Tooltip"));
		button.addActionListener(e->updateTree(true));

		/* Ansicht der Klassen und Methoden */
		content.add(new JScrollPane(tree=new JTree()),BorderLayout.CENTER);
		tree.setRootVisible(false);
		tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID=-6899531156092994813L;
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				setIcon(leaf?Images.SCRIPT_FILE.getIcon():Images.SCRIPT_MODE_JAVA.getIcon());
				return this;
			}
		});

		/* Infobereich */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);

		final String linkColor;
		if (FlatLaFHelper.isDark()) {
			linkColor="#589DF6";
		} else {
			linkColor="blue";
		}

		JLabel label;
		line.add(label=new JLabel("<html><body><span style=\"color: "+linkColor+"; text-decoration: underline;\">"+Language.tr("ExternalConnect.Dialog.ExamplesLink")+"</span></body></html>"));
		label.setToolTipText(Language.tr("ExternalConnect.Dialog.ExamplesLink.Hint"));
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) commandOpenExamplesFolder();
			}
		});

		/* Wenn ein Startverzeichnis übergeben wurde, dieses gleich einlesen */
		updateTree(true);

		/* Dialog starten */
		setMinSizeRespectingScreensize(500,600);
		setSizeRespectingScreensize(500,600);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Aktualisiert nach einer Veränderung in {@link #folder}
	 * die Baumstruktur in {@link #tree}.
	 * @param force	Neu einlesen erzwingen (<code>true</code>) oder nur neu einlesen, wenn seit dem letzten Aufruf ein anderes Verzeichnis gewählt wurde (<code>false</code>)
	 * @see #folder
	 * @see #tree
	 */
	private void updateTree(final boolean force) {
		final String newFolder=folder.getText().trim();
		if (lastFolder!=null && newFolder.equals(lastFolder) && !force) return;
		lastFolder=newFolder;

		final ExternalConnect connect=new ExternalConnect(new File(newFolder));
		final Map<String,List<String>> map=connect.getInformationMap();

		final DefaultMutableTreeNode root=new DefaultMutableTreeNode();

		for (Map.Entry<String,List<String>> entry: map.entrySet()) {
			final DefaultMutableTreeNode group=new DefaultMutableTreeNode(entry.getKey());
			root.add(group);
			for (String method: entry.getValue()) group.add(new DefaultMutableTreeNode(String.format("Object %s(RuntimeInterface,SystemInterface,Object)",method)));
		}

		tree.setModel(new DefaultTreeModel(root));

		/* Alle Einträge ausklappen */
		int row=0;
		while (row<tree.getRowCount()) {
			final DefaultMutableTreeNode node=(DefaultMutableTreeNode)(tree.getPathForRow(row).getLastPathComponent());
			if (!node.isLeaf()) tree.expandRow(row);
			row++;
		}
	}

	/**
	 * Liefert das gewählte Plugins-Verzeichnis
	 * @return	Ausgewähltes Plugins-Verzeichnis
	 */
	public String getFolder() {
		return folder.getText().trim();
	}

	/**
	 * Öffnet das Verzeichnis, in dem sich die Beispiel-Skripte befinden.
	 */
	private void commandOpenExamplesFolder() {
		boolean ok=false;

		final File folder1=new File(SetupData.getProgramFolder(),"userscripts");
		final File folder2=new File(new File(SetupData.getProgramFolder(),"build"),"UserScripts");

		if (folder1.isDirectory()) {
			try {Desktop.getDesktop().open(folder1); ok=true;} catch (IOException e) {ok=false;}
		} else {
			if (folder2.isDirectory()) {
				try {Desktop.getDesktop().open(folder2); ok=true;} catch (IOException e) {ok=false;}
			}
		}

		if (!ok) {
			MsgBox.error(this,Language.tr("ExternalConnect.Dialog.ExamplesLink.ErrorTitle"),String.format(Language.tr("ExternalConnect.Dialog.ExamplesLink.ErrorInfo"),folder1.toString()));
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		/* Verzeichnis mit den zu kompilierenden Java-Dateien */
		final String folderName=getFolder();
		if (folderName.isEmpty()) {
			MsgBox.error(this,Language.tr("Simulation.Java.Error.CompileError"),Language.tr("ExternalConnect.Dialog.Compile.ErrorNoFolder"));
			return;
		}
		final File folder=new File(folderName);
		if (!folder.isDirectory()) {
			MsgBox.error(this,Language.tr("Simulation.Java.Error.CompileError"),String.format(Language.tr("ExternalConnect.Dialog.Compile.ErrorNotDirectory"),folderName));
			return;
		}

		/* Java-Kompiler */
		if (!DynamicFactory.hasCompiler()) {
			MsgBox.error(this,Language.tr("Simulation.Java.Error.CompileError"),DynamicFactory.getStatusText(DynamicStatus.NO_COMPILER));
			return;
		}

		/* Verarbeitung */
		compileSuccessCount=0;
		compileErrorCount=0;
		final StringBuilder result=new StringBuilder();
		compileFolder(folder,folder,result);
		result.append("\n");
		result.append(Language.tr("ExternalConnect.Dialog.Compile.StatusDone")+"\n");
		result.append(String.format((compileSuccessCount==1)?Language.tr("ExternalConnect.Dialog.Compile.StatusSuccessOne"):Language.tr("ExternalConnect.Dialog.Compile.StatusSuccessMulti"),compileSuccessCount)+"\n");
		result.append(String.format((compileErrorCount==1)?Language.tr("ExternalConnect.Dialog.Compile.StatusErrorOne"):Language.tr("ExternalConnect.Dialog.Compile.StatusErrorMulti"),compileErrorCount)+"\n");

		updateTree(true);

		/* Ergebnisse anzeigen */
		new ExternalConnectCompileResultsDialog(this,result.toString());
	}

	/**
	 * Zähler für die erfolgreich übersetzten Dateien
	 * @see #compileFolder(File, File, StringBuilder)
	 * @see #compileFile(File, File, StringBuilder)
	 */
	private int compileSuccessCount;

	/**
	 * Zähler für die beim Übersetzten aufgetretenen Fehler
	 * @see #compileFolder(File, File, StringBuilder)
	 * @see #compileFile(File, File, StringBuilder)
	 */
	private int compileErrorCount;

	/**
	 * Übersetzt alle Dateien in einem Verzeichnis (und seinen Unterverzeichnissen).
	 * @param folder	Verzeichnis das die java-Dateien enthält
	 * @param baseFolder	Klassenpfad-Basisverzeichnis
	 * @param result	Nimmt mögliche Ausgaben zu Erfolg oder Fehlermeldungen auf
	 */
	private void compileFolder(final File folder, final File baseFolder, final StringBuilder result) {
		final String[] list=folder.list();
		if (list==null) return;

		for (String record: list) {
			final File file=new File(folder,record);
			if (file.isDirectory()) compileFolder(file,baseFolder,result);
		}

		for (String record: list) {
			if (record.toLowerCase().endsWith(".java")) compileFile(new File(folder,record),baseFolder,result);
		}
	}

	/**
	 * Übersetzt eine einzelne Dateien.
	 * @param file	Zu kompilierende Datei
	 * @param baseFolder	Klassenpfad-Basisverzeichnis
	 * @param result	Nimmt mögliche Ausgaben zu Erfolg oder Fehlermeldungen auf
	 */
	private void compileFile(final File file, final File baseFolder, final StringBuilder result) {
		result.append(file.toString()+"\n");

		final String compileResult=DynamicClassInternalCompilerFullMemory.compileJavaToClass(file,baseFolder);
		if (compileResult==null) {
			compileSuccessCount++;
		} else {
			compileErrorCount++;
			result.append(compileResult);
		}
	}
}