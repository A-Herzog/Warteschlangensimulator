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
	 * Zuletzt in {@link #updateTree()} verwendetes Verzeichnis.
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

		final JPanel content=createGUI(()->Help.topicModal(this,"ExternalConnect"));
		content.	setLayout(new BorderLayout());

		/* Verzeichnisauswahl */
		JPanel line=new JPanel(new BorderLayout());
		content.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("ExternalConnect.Dialog.Folder")+": "),BorderLayout.WEST);
		line.add(folder=new JTextField((initialFolder==null)?"":initialFolder),BorderLayout.CENTER);
		folder.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {updateTree();	}
			@Override public void keyReleased(KeyEvent e) {updateTree();}
			@Override public void keyPressed(KeyEvent e) {updateTree();}
		});
		final JButton button=new JButton(Images.GENERAL_SELECT_FOLDER.getIcon());
		button.setToolTipText(Language.tr("ExternalConnect.Dialog.Folder.Tooltip"));
		line.add(button,BorderLayout.EAST);
		button.addActionListener(e->{
			final JFileChooser fc=new JFileChooser();
			CommonVariables.initialDirectoryToJFileChooser(fc);
			final String oldFolder=folder.getText().trim();
			if (!oldFolder.trim().isEmpty() && new File(oldFolder).isDirectory()) fc.setCurrentDirectory(new File(oldFolder));
			fc.setDialogTitle(Language.tr("ExternalConnect.Dialog.Folder.Tooltip"));
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
			CommonVariables.initialDirectoryFromJFileChooser(fc);
			folder.setText(fc.getSelectedFile().toString());
			updateTree();
		});

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

		JLabel label;
		line.add(label=new JLabel("<html><body><span style=\"color: blue; text-decoration: underline;\">"+Language.tr("ExternalConnect.Dialog.ExamplesLink")+"</span></body></html>"));
		label.setToolTipText(Language.tr("ExternalConnect.Dialog.ExamplesLink.Hint"));
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) commandOpenExamplesFolder();
			}
		});

		/* Wenn ein Startverzeichnis übergeben wurde, dieses gleich einlesen */
		updateTree();

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
	 * @see #folder
	 * @see #tree
	 */
	private void updateTree() {
		final String newFolder=folder.getText().trim();
		if (lastFolder!=null && newFolder.equals(lastFolder)) return;
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
}
