/**
 * Copyright 2021 Alexander Herzog
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
package gitconnect;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;

/**
 * Zeigt in einem Dialog die verfügbaren Branches an und ermöglicht es,
 * einen Branch auszuwählen (Checkout).
 * @author Alexander Herzog
 * @see GitListDialog
 */
public class GitBranchesListDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8998804544161019779L;

	/**
	 * Repository-Daten aus dem die Branches stammen
	 */
	private final GitSetup gitSetup;

	/**
	 * Listendarstellung der Branches
	 */
	private final JList<JLabel> list;

	/**
	 * Aktuelle Liste der Zweige in der Reihenfolge wie in {@link #list}
	 * @see #list
	 * @see #updateList()
	 */
	private String[] branches;

	/**
	 * Aktuell aktiver Zweig
	 * @see #updateList()
	 */
	private String currentBranch;

	/**
	 * Datenmodell für die Listendarstellung der Branches
	 * @see #list
	 */
	private final DefaultListModel<JLabel> listModel;

	/**
	 * Schaltfläche "Checkout"
	 */
	private final JButton buttonCheckout;


	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param gitSetup	Git-Repository dessen Branches angezeigt werden sollen
	 */
	public GitBranchesListDialog(final Component owner, final GitSetup gitSetup) {
		super(owner,Language.tr("Git.ListBranches.Title"));
		this.gitSetup=gitSetup;

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(this,"Git"));
		content.setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);
		final JButton buttonAdd;
		toolbar.add(buttonAdd=new JButton(Language.tr("Git.ListBranches.Add"),Images.EDIT_ADD.getIcon()));
		buttonAdd.setToolTipText(Language.tr("Git.ListBranches.Add.Hint"));
		buttonAdd.addActionListener(e->commandAdd());
		toolbar.add(buttonCheckout=new JButton(Language.tr("Git.ListBranches.Checkout"),Images.GENERAL_ON.getIcon()));
		buttonCheckout.setToolTipText(Language.tr("Git.ListBranches.Checkout.Hint"));
		buttonCheckout.addActionListener(e->commandCheckout());

		/* Liste */
		content.add(new JScrollPane(list=new JList<>()));
		list.setModel(listModel=new DefaultListModel<>());
		list.setCellRenderer(new JLabelRender());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) commandCheckout();
			}
		});
		list.addListSelectionListener(e->updateButtons());
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT && e.getModifiersEx()==0) {
					commandAdd();
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					commandCheckout();
					e.consume();
					return;
				}
			}
		});
		updateList();

		/* Dialog starten */
		setMinSizeRespectingScreensize(400,300);
		setSizeRespectingScreensize(600,400);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Aktualisiert den Aktivierungsstatus der Symbolleisten-Schaltflächen.
	 */
	private void updateButtons() {
		final int index=list.getSelectedIndex();

		if (branches==null || index<0) {
			buttonCheckout.setEnabled(false);
			return;
		}

		buttonCheckout.setEnabled(!branches[index].equals(currentBranch));
	}

	/**
	 * Wandelt die Zeichen "&amp;", "&lt;" und "&gt;" in ihre entsprechenden
	 * HTML-Entitäten um.
	 * @param line	Umzuwandelnder Text
	 * @return	Umgewandelter Text
	 */
	private String encodeHTMLentities(final String line) {
		if (line==null) return "";
		String result;
		result=line.replaceAll("&","&amp;");
		result=result.replaceAll("<","&lt;");
		result=result.replaceAll(">","&gt;");
		return result;
	}

	/**
	 * Generiert den Bezeichner für den Listeneintrag für einen Zweig
	 * @param branchName	Name des Zweigs
	 * @return	Text für den Listeneintrag
	 * @see #updateList()
	 */
	public String getBranchLabel(final String branchName) {
		final StringBuilder text=new StringBuilder();
		text.append("<html><body>");
		text.append(Language.tr("Git.ListBranches.BranchName"));
		text.append(": <b>");
		text.append(encodeHTMLentities(branchName));
		text.append("</b>");
		if (currentBranch.equals(branchName)) {
			text.append("  <span style='color: red'>(");
			text.append(Language.tr("Git.ListBranches.ActiveBranch"));
			text.append(")</span>");
		}
		text.append("</body></html>");
		return text.toString();
	}

	/**
	 * Aktualisiert die Liste der Zweige
	 */
	private void updateList() {
		listModel.clear();

		branches=gitSetup.listBranches();
		currentBranch=gitSetup.getCurrentBranchName();

		/* Listeneinträge erstellen */
		final Icon icon=Images.ARROW_SWITCH.getIcon();
		for (String branch: branches) {
			final JLabel label=new JLabel(getBranchLabel(branch),icon,SwingConstants.LEADING);
			listModel.addElement(label);
			label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		}

		updateButtons();
	}

	/**
	 * Befehl: Zweig hinzufügen
	 */
	private void commandAdd() {
		final String newBranchName=JOptionPane.showInputDialog(this,Language.tr("Git.ListBranches.Add.Prompt")+":");
		if (newBranchName==null) return;
		if (newBranchName.trim().isEmpty())  {
			MsgBox.error(this,Language.tr("Git.ListBranches.Add"),Language.tr("Git.ListBranches.Add.ErrorInvalidName"));
			return;
		}

		if (gitSetup.addAndCheckoutBranch(newBranchName)) {
			MsgBox.info(this,Language.tr("Git.ListBranches.Add"),String.format(Language.tr("Git.ListBranches.Add.Success"),newBranchName));
		} else {
			MsgBox.error(this,Language.tr("Git.ListBranches.Add"),String.format(Language.tr("Git.ListBranches.Add.Error"),newBranchName));
		}

		updateList();
	}

	/**
	 * Befehl: Zweig aktivieren
	 */
	private void commandCheckout() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final String branchName=branches[index];

		if (branchName.equals(currentBranch)) {
			MsgBox.error(this,Language.tr("Git.ListBranches.Checkout"),String.format(Language.tr("Git.ListBranches.Checkout.AlreadyActive"),currentBranch));
			return;
		}

		if (!MsgBox.confirm(this,Language.tr("Git.ListBranches.Checkout"),String.format(Language.tr("Git.ListBranches.Checkout.Confirm"),branchName),String.format(Language.tr("Git.ListBranches.Checkout.Confirm.InfoYes"),branchName),String.format(Language.tr("Git.ListBranches.Checkout.Confirm.InfoNo"),currentBranch))) return;

		boolean result=(Boolean)GitDialogProcessMonitor.run(this,Language.tr("Git.ListBranches.Checkout"),progressMonitor->gitSetup.checkoutBranch(branchName,progressMonitor));
		if (result) {
			MsgBox.info(this,Language.tr("Git.ListBranches.Checkout"),String.format(Language.tr("Git.ListBranches.Checkout.Success"),branchName));
		} else {
			MsgBox.error(this,Language.tr("Git.ListBranches.Checkout"),String.format(Language.tr("Git.ListBranches.Checkout.Error"),branchName));
		}

		updateList();
	}

	/**
	 * Renderer für die Einträge der Branches-Liste {@link #list()}
	 */
	private static class JLabelRender implements ListCellRenderer<JLabel> {
		@Override
		public Component getListCellRendererComponent(JList<? extends JLabel> list, JLabel value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				value.setBackground(list.getSelectionBackground());
				value.setForeground(list.getSelectionForeground());
				value.setOpaque(true);
			} else {
				value.setBackground(list.getBackground());
				value.setForeground(list.getForeground());
				value.setOpaque(false);
			}
			return value;
		}
	}
}
