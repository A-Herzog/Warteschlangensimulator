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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import mathtools.Table;
import mathtools.distribution.swing.CommonVariables;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
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
	 * Ausgewählter Importmodus: Nur über Plugin-Schnittstelle oder alle Klassen für Import bereitstellen?
	 */
	private final JComboBox<String> modeSelect;

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
	 * @param initialAllowClassLoad	Sollen die Klassen im Plugings-Verzeichnis auch als normale Imports zur Verfügung gestellt werden?
	 */
	public ExternalConnectDialog(final Component owner, final String initialFolder, final boolean initialAllowClassLoad) {
		super(owner,Language.tr("ExternalConnect.Dialog.Title"));

		/* GUI */
		addUserButton(Language.tr("ExternalConnect.Dialog.Compile"),Images.PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVA.getIcon());
		getUserButton(0).setToolTipText(Language.tr("ExternalConnect.Dialog.Compile.Tooltip"));
		final JPanel content=createGUI(()->Help.topicModal(this,"ExternalConnect"));
		content.	setLayout(new BorderLayout());

		JPanel line;
		JPanel sub;
		JLabel label;
		JButton button;

		/* Setup-Bereich */
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Verzeichnisauswahl */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("ExternalConnect.Dialog.Folder")+": ",(initialFolder==null)?"":initialFolder);
		setup.add(line=(JPanel)data[0]);
		folder=(JTextField)data[1];
		folder.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {updateTree(false);}
			@Override public void keyReleased(KeyEvent e) {updateTree(false);}
			@Override public void keyPressed(KeyEvent e) {updateTree(false);}
		});

		/* Button rechts neben Verzeichnisauswahl */
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

		/* Klassen laden? */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("ExternalConnect.Dialog.Mode")+": "));
		line.add(modeSelect=new JComboBox<>(new String[] {
				Language.tr("ExternalConnect.Dialog.Mode.PluginOnly"),
				Language.tr("ExternalConnect.Dialog.Mode.Full")
		}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[] {
				Images.MODEL_PLUGINS,
				Images.SCRIPT_MODE_JAVA
		}));
		if (initialAllowClassLoad) modeSelect.setSelectedIndex(1); else modeSelect.setSelectedIndex(0);
		modeSelect.addActionListener(e->updateTree(true));

		/* Überschrift über Klassen und Methoden Darstellung */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("ExternalConnect.Dialog.TreeInfo")+": "));

		/* Ansicht der Klassen und Methoden */
		content.add(new JScrollPane(tree=new JTree()),BorderLayout.CENTER);
		tree.setRootVisible(false);
		tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID=-6899531156092994813L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree,((TreeObject)((DefaultMutableTreeNode)value).getUserObject()).label,sel,expanded,leaf,row,hasFocus);
				setIcon(((TreeObject)((DefaultMutableTreeNode)value).getUserObject()).icon);
				return this;
			}
		});

		/* Infobereich */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);

		/* Button: Beispiele in Verzeichnis kopieren */
		line.add(button=new JButton(Language.tr("ExternalConnect.Dialog.CopyExampleFiles"),Images.EDIT_COPY.getIcon()));
		button.setToolTipText(Language.tr("ExternalConnect.Dialog.CopyExampleFiles.Hint"));
		button.addActionListener(e->commandCopyExampleFiles());

		/* Link: Beispielverzeichnis öffnen */
		final String linkColor;
		if (FlatLaFHelper.isDark()) {
			linkColor="#589DF6";
		} else {
			linkColor="blue";
		}

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
		setMinSizeRespectingScreensize(600,500);
		setSizeRespectingScreensize(600,500);
		setMaxSizeRespectingScreensize(600,500);
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
		final Map<String,ExternalConnect.FileInfo> map=connect.getInformationMap();

		final DefaultMutableTreeNode root=TreeObject.buildRoot();

		for (Map.Entry<String,ExternalConnect.FileInfo> entry: map.entrySet()) {
			final DefaultMutableTreeNode group=TreeObject.buildNode(entry.getKey(),Images.SCRIPT_MODE_JAVA);
			root.add(group);
			final ExternalConnect.FileInfo fileInfo=entry.getValue();
			if (fileInfo.methods!=null) {
				if (fileInfo.methods.size()==0) {
					if (fileInfo.isInRootDir) {
						group.add(TreeObject.buildNode(Language.tr("ExternalConnect.Dialog.NoPluginMethodsRootDirInfo1"),Images.GENERAL_WARNING));
						group.add(TreeObject.buildNode(Language.tr("ExternalConnect.Dialog.NoPluginMethodsRootDirInfo2"),Images.GENERAL_WARNING));
						group.add(TreeObject.buildNode(Language.tr("ExternalConnect.Dialog.NoPluginMethodsRootDirInfo3"),Images.GENERAL_WARNING));
					} else {
						group.add(TreeObject.buildNode(Language.tr("ExternalConnect.Dialog.NoPluginMethodsInfo"),Images.GENERAL_INFO));
						if (modeSelect.getSelectedIndex()==0) {
							group.add(TreeObject.buildNode(Language.tr("ExternalConnect.Dialog.NoPluginMethodsInfo.WarningDirectImportIsOff"),Images.GENERAL_WARNING));
						}
					}
				} else {
					for (String method: fileInfo.methods) group.add(TreeObject.buildNode(String.format("Object %s(RuntimeInterface,SystemInterface,Object)",method),Images.MODEL_PLUGINS));
				}
			}
			if (fileInfo.error!=null) {
				for (String error: fileInfo.error) group.add(TreeObject.buildNode(error,Images.GENERAL_WARNING));
			}
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
	 * Sollen die Klassen im Plugings-Verzeichnis auch als normale Imports
	 * zur Verfügung gestellt werden?
	 * @return	Sollen die Klassen im Plugings-Verzeichnis auch als normale Imports zur Verfügung gestellt werden?
	 */
	public boolean getAllowClassLoad() {
		return modeSelect.getSelectedIndex()==1;
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
		compileSuccessWarningCount=0;
		compileErrorCount=0;
		final StringBuilder result=new StringBuilder();
		compileFolder(folder,folder,result);
		result.append("\n");
		result.append(Language.tr("ExternalConnect.Dialog.Compile.StatusDone")+"\n");
		result.append(String.format((compileSuccessCount==1)?Language.tr("ExternalConnect.Dialog.Compile.StatusSuccessOne"):Language.tr("ExternalConnect.Dialog.Compile.StatusSuccessMulti"),compileSuccessCount)+"\n");
		if (compileSuccessWarningCount>0) {
			result.append(String.format((compileSuccessCount==1)?Language.tr("ExternalConnect.Dialog.Compile.StatusSuccessWarningOne"):Language.tr("ExternalConnect.Dialog.Compile.StatusSuccessWarningMulti"),compileSuccessWarningCount)+"\n");
		}
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
	 * Zähler für die mit Warnung erfolgreich übersetzten Dateien
	 * @see #compileFolder(File, File, StringBuilder)
	 * @see #compileFile(File, File, StringBuilder)
	 */
	private int compileSuccessWarningCount;

	/**
	 * Zähler für die beim Übersetzten aufgetretenen Fehler
	 * @see #compileFolder(File, File, StringBuilder)
	 * @see #compileFile(File, File, StringBuilder)
	 */
	private int compileErrorCount;

	/**
	 * Basisverzeichnis für die Schnittstellenklassen, die als erstes kompiliert werden sollen.
	 * @see #compileFolder(File, File, StringBuilder)
	 */
	private static final String scriptingSubFolder="scripting";

	/**
	 * Übersetzt alle Dateien in einem Verzeichnis (und seinen Unterverzeichnissen).
	 * @param folder	Verzeichnis das die java-Dateien enthält
	 * @param baseFolder	Klassenpfad-Basisverzeichnis
	 * @param result	Nimmt mögliche Ausgaben zu Erfolg oder Fehlermeldungen auf
	 */
	private void compileFolder(final File folder, final File baseFolder, final StringBuilder result) {
		final String[] list=folder.list();
		if (list==null) return;

		final boolean isMainFolder=(folder.equals(baseFolder));

		if (isMainFolder && new File(folder,scriptingSubFolder).isDirectory()) {
			final File file=new File(folder,scriptingSubFolder);
			compileFolder(file,baseFolder,result);
		}

		for (String record: list) {
			if (isMainFolder && record.equals(scriptingSubFolder)) continue;
			final File file=new File(folder,record);
			if (file.isDirectory()) compileFolder(file,baseFolder,result);
		}

		for (String record: list) {
			if (record.toLowerCase().endsWith(".java")) compileFile(new File(folder,record),baseFolder,result);
		}
	}

	/**
	 * Prüft, ob es sich bei einer bestimmten Datei um eine Schnittstellendefinitionsdatei
	 * handelt und ersetzt diese ggf. durch ihre (aktualisierte) Vorlage
	 * @param file	Zu prüfende Datei
	 * @return	Liefert <code>true</code>, wenn die Datei ausgetauscht wurde
	 */
	private boolean checkAndReplaceInterfaceFile(final File file) {
		/* Datei im Vorlageverzeichnis ermitteln */
		final File sourceFolder=getInterfaceSourceFolder();
		if (sourceFolder==null) return false;
		final File sourceFile=new File(new File(new File(sourceFolder,scriptingSubFolder),"java"),file.getName());
		if (!sourceFile.isFile()) return false;

		/* Dateien vergleichen */
		final String fileContent=Table.loadTextFromFile(file);
		final String sourceFileContent=Table.loadTextFromFile(sourceFile);
		if (fileContent==null || sourceFileContent==null) return false;
		if (fileContent.equals(sourceFileContent)) return false;

		/* Datei ersetzen */
		try {
			Files.copy(sourceFile.toPath(),file.toPath(),StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Übersetzt eine einzelne Dateien.
	 * @param file	Zu kompilierende Datei
	 * @param baseFolder	Klassenpfad-Basisverzeichnis
	 * @param result	Nimmt mögliche Ausgaben zu Erfolg oder Fehlermeldungen auf
	 */
	private void compileFile(final File file, final File baseFolder, final StringBuilder result) {
		result.append(file.toString()+"\n");

		checkAndReplaceInterfaceFile(file);

		final DynamicClassInternalCompilerFullMemory.CompilerResult compileResult=DynamicClassInternalCompilerFullMemory.compileJavaToClass(file,baseFolder);
		switch (compileResult.type) {
		case SUCCESS:
			compileSuccessCount++;
			break;
		case SUCCESS_WARNING:
			result.append(compileResult.message);
			compileSuccessWarningCount++;
			break;
		case ERROR:
			result.append(compileResult.message);
			compileErrorCount++;
			break;
		}
	}

	/**
	 * Liefert das Verzeichnis in dem sich (in Unterverzeichnissen) die Schnittstellenklassen
	 * als Kopiervorlagen befinden.
	 * @return	Verzeichnis der Vorlagen für die Schnittstellenklassen (kann <code>null</code> sein, wenn das Verzeichnis nicht ermittelt werden konnte)
	 */
	private File getInterfaceSourceFolder() {
		final File folder1=new File(SetupData.getProgramFolder(),"userscripts");
		final File folder2=new File(new File(SetupData.getProgramFolder(),"build"),"UserScripts");
		File sourceFolder=null;
		if (folder1.isDirectory()) sourceFolder=folder1;
		if (sourceFolder==null && folder2.isDirectory()) sourceFolder=folder2;
		return sourceFolder;
	}

	/**
	 * Kopiert die Beispieldateien in ein vom Nutzer ausgewähltes Verzeichnis.
	 */
	private void commandCopyExampleFiles() {
		/* Quellverzeichnis */
		final File sourceFolder=getInterfaceSourceFolder();
		if (sourceFolder==null) {
			final File folder1=new File(SetupData.getProgramFolder(),"userscripts");
			MsgBox.error(this,Language.tr("ExternalConnect.Dialog.ExamplesLink.ErrorTitle"),String.format(Language.tr("ExternalConnect.Dialog.ExamplesLink.ErrorInfo"),folder1.toString()));
			return;
		}

		/* Zielverzeichnis auswählen */
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("ExternalConnect.Dialog.CopyExampleFiles.SelectDestinationDialogTitle"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		final File destinationFolder=fc.getSelectedFile();
		if (!destinationFolder.isDirectory()) {
			MsgBox.error(this,Language.tr("ExternalConnect.Dialog.CopyExampleFiles.NoDestinationError.Title"),String.format(Language.tr("ExternalConnect.Dialog.CopyExampleFiles.NoDestinationError.Info"),destinationFolder.toString()));
			return;
		}

		if (copyFiles(sourceFolder,destinationFolder)) {
			MsgBox.info(this,Language.tr("ExternalConnect.Dialog.CopyExampleFiles.Success.Title"),String.format(Language.tr("ExternalConnect.Dialog.CopyExampleFiles.Success.Info"),destinationFolder.toString()));
		} else {
			MsgBox.info(this,Language.tr("ExternalConnect.Dialog.CopyExampleFiles.CopyError.Title"),String.format(Language.tr("ExternalConnect.Dialog.CopyExampleFiles.CopyError.Info"),destinationFolder.toString()));
		}
	}

	/**
	 * Kopiert alle Dateien (inkl. Unterverzeichnissen) aus einem Quell- in ein Zielverzeichnis
	 * @param sourceFolder	Quellverzeichnis (muss existieren und Parameter darf nicht <code>null</code> sein)
	 * @param destinationFolder	Zielverzeichnis (muss bereits existieren und Parameter darf nicht <code>null</code> sein)
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean copyFiles(final File sourceFolder, final File destinationFolder) {
		final String[] fileNames=sourceFolder.list();
		if (fileNames==null) return false;

		for (String fileName: fileNames) {
			final File sourceFile=new File(sourceFolder,fileName);
			final File destinationFile=new File(destinationFolder,fileName);
			if (sourceFile.isFile()) {
				try {
					Files.copy(sourceFile.toPath(),destinationFile.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.COPY_ATTRIBUTES);
				} catch (IOException e) {
					return false;
				}
				continue;
			}
			if (sourceFile.isDirectory()) {
				if (destinationFile.isDirectory() || destinationFile.mkdir()) {
					if (!copyFiles(sourceFile,destinationFile)) return false;
				} else {
					return false;
				}
				continue;
			}
		}

		return true;
	}

	/**
	 * Eintrag in {@link ExternalConnectDialog#tree}
	 * @see ExternalConnectDialog#tree
	 */
	private static class TreeObject {
		/**
		 * Anzuzeigender Text in dem Baumeintrag
		 */
		public final String label;

		/**
		 * Icon zu dem Baumeintrag
		 */
		public final Icon icon;

		/**
		 * Konstruktor der Klasse<br>
		 * Diese Klasse kann nicht direkt instanziert werden.
		 * @see #buildNode(String, Images)
		 * @see #buildRoot()
		 * @param label	Anzuzeigender Text in dem Baumeintrag
		 * @param image	Icon zu dem Baumeintrag
		 */
		private TreeObject(final String label, final Images image) {
			this.label=label;
			icon=(image==null)?null:image.getIcon();
		}

		/**
		 * Erzeugt einen Baumeintrag mit Text und Icon
		 * @param label	Anzuzeigender Text in dem Baumeintrag
		 * @param image	Icon zu dem Baumeintrag
		 * @return	Neuer Baumeintrag
		 */
		public static DefaultMutableTreeNode buildNode(final String label, final Images image) {
			return new DefaultMutableTreeNode(new TreeObject(label,image));
		}

		/**
		 * Erzeugt den Wurzeleintrag für die Baumstruktur
		 * @return	Wurzeleintrag für die Baumstruktur
		 */
		public static DefaultMutableTreeNode buildRoot() {
			return new DefaultMutableTreeNode(new TreeObject(null,null));
		}
	}
}