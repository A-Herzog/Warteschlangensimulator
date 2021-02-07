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
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;

/**
 * In diesem Dialog werden die verfügbaren Git-Konfigurationen aufgelistet
 * und es können Konfigurationen angelegt, verändert oder gelöscht werden.
 * @author Alexander Herzog
 * @see GitSetup
 */
public class GitListDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=2481596044474895961L;

	/**
	 * Liste der Git-Konfigurationen
	 */
	private final List<GitSetup> setups;

	/**
	 * Listendarstellung der Git-Konfigurationen
	 */
	private final JList<JLabel> list;

	/**
	 * Datenmodell für die Listendarstellung der Git-Konfigurationen
	 * @see #list
	 */
	private final DefaultListModel<JLabel> listModel;

	/**
	 * Schaltfläche "Hinzufügen"
	 */
	private final JButton buttonAdd;

	/**
	 * Schaltfläche "Bearbeiten"
	 */
	private final JButton buttonEdit;

	/**
	 * Schaltfläche "Löschen"
	 */
	private final JButton buttonDelete;

	/**
	 * Schaltfläche "Pull"
	 */
	private final JButton buttonPull;

	/**
	 * Schaltfläche "Push"
	 */
	private final JButton buttonPush;

	/**
	 * Schaltfläche "Pull alle"
	 */
	private final JButton buttonPullAll;

	/**
	 * Schaltfläche "Push alle"
	 */
	private final JButton buttonPushAll;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public GitListDialog(final Component owner) {
		super(owner,Language.tr("Git.List.Title"));

		/* Git-Konfigurationen laden */
		setups=SetupData.getSetup().gitSetups.stream().map(setup->new GitSetup(setup)).collect(Collectors.toList());

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"Git"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalGit);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Toolbar */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.add(buttonAdd=new JButton(Language.tr("Git.List.Add"),Images.EDIT_ADD.getIcon()));
		buttonAdd.setToolTipText(Language.tr("Git.List.Add.Hint"));
		buttonAdd.addActionListener(e->commandAdd());
		toolbar.add(buttonEdit=new JButton(Language.tr("Git.List.Edit"),Images.GENERAL_EDIT.getIcon()));
		buttonEdit.setToolTipText(Language.tr("Git.List.Edit.Hint"));
		buttonEdit.addActionListener(e->commandEdit());
		toolbar.add(buttonDelete=new JButton(Language.tr("Git.List.Delete"),Images.EDIT_DELETE.getIcon()));
		buttonDelete.setToolTipText(Language.tr("Git.List.Delete.Hint"));
		buttonDelete.addActionListener(e->commandDelete());
		toolbar.addSeparator();
		toolbar.add(buttonPull=new JButton(Language.tr("Git.List.Pull"),Images.ARROW_DOWN.getIcon()));
		buttonPull.setToolTipText(Language.tr("Git.List.Pull.Hint"));
		buttonPull.addActionListener(e->commandPull());
		toolbar.add(buttonPush=new JButton(Language.tr("Git.List.Push"),Images.ARROW_UP.getIcon()));
		buttonPush.setToolTipText(Language.tr("Git.List.Push.Hint"));
		buttonPush.addActionListener(e->commandPush());
		toolbar.add(buttonPullAll=new JButton(Language.tr("Git.List.PullAll"),Images.ARROW_DOWN_DOUBLE.getIcon()));
		buttonPullAll.setToolTipText(Language.tr("Git.List.PullAll.Hint"));
		buttonPullAll.addActionListener(e->commandPullAll());
		toolbar.add(buttonPushAll=new JButton(Language.tr("Git.List.PushAll"),Images.ARROW_UP_DOUBLE.getIcon()));
		buttonPushAll.setToolTipText(Language.tr("Git.List.PushAll.Hint"));
		buttonPushAll.addActionListener(e->commandPushAll());

		/* Liste */
		content.add(new JScrollPane(list=new JList<>()));
		list.setModel(listModel=new DefaultListModel<>());
		list.setCellRenderer(new JLabelRender());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) commandEdit();
				if (e.getClickCount()==1 && SwingUtilities.isRightMouseButton(e)) commandContextMenu(e);
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
				if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getModifiersEx()==InputEvent.SHIFT_DOWN_MASK) {
					commandEdit();
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_DELETE && e.getModifiersEx()==0) {
					commandDelete();
					e.consume();
					return;
				}
			}
		});
		updateList(-1);

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,400);
		setSizeRespectingScreensize(800,600);
		setResizable(true);
		setVisible(true);
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
	 * Erstellt den Text für den Listeneintrag für eine Git-Konfiguration.
	 * @param gitSetup Git-Konfiguration die in der Liste dargestellt werden soll
	 * @return	html-Text für den Listeneintrag
	 */
	private String buildGetSetupText(final GitSetup gitSetup) {
		final StringBuilder text=new StringBuilder();

		text.append("<html><body>");
		text.append(Language.tr("Git.List.Record.Folder"));
		text.append(": <b>");
		text.append(encodeHTMLentities(gitSetup.localFolder));
		text.append("</b><br>\n");
		if (gitSetup.useServer) {
			text.append(Language.tr("Git.List.Record.Server"));
			text.append(": <b>");
			text.append(encodeHTMLentities(gitSetup.serverURL));
			text.append("</b>");
			if (gitSetup.pullOnStart) {
				text.append("<br>\n");
				text.append("<i>");
				Language.tr("Git.List.Record.PullOnStart");
				text.append("</i>");
			}
		} else {
			text.append(Language.tr("Git.List.Record.NoServer"));
		}
		text.append("</body></html>");

		return text.toString();
	}

	/**
	 * Aktualisiert die Konfigurationenliste
	 * @param selectIndex	Nach der Aktualisierung zu selektierender Index in der Liste
	 */
	private void updateList(final int selectIndex) {
		listModel.clear();

		/* Listeneinträge erstellen */
		final Icon icon=Images.GIT.getIcon();
		for (GitSetup gitSetup: setups) {
			final JLabel label=new JLabel(buildGetSetupText(gitSetup),icon,SwingConstants.LEADING);
			listModel.addElement(label);
			label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		}

		/* Selektion wiederherstellen */
		if (selectIndex>=0 && selectIndex<listModel.size()) {
			list.setSelectedIndex(selectIndex);
		} else {
			if (listModel.size()>0) list.setSelectedIndex(0);
		}

		updateButtons();
	}

	/**
	 * Aktualisiert den Aktivierungsstatus der Symbolleisten-Schaltflächen.
	 */
	private void updateButtons() {
		/* Bearbeiten und Löschen */
		buttonEdit.setEnabled(list.getSelectedIndex()>=0);
		buttonDelete.setEnabled(list.getSelectedIndex()>=0);

		/* Pull / Push */
		buttonPull.setEnabled(list.getSelectedIndex()>=0 && setups.get(list.getSelectedIndex()).useServer);
		buttonPush.setEnabled(list.getSelectedIndex()>=0 && setups.get(list.getSelectedIndex()).useServer);

		/* Pull / Push für alle */
		boolean hasServerSetups=false;
		for (GitSetup setup: setups) if (setup.useServer) {hasServerSetups=true; break;}
		buttonPullAll.setVisible(setups.size()>=2 && hasServerSetups);
		buttonPushAll.setVisible(setups.size()>=2 && hasServerSetups);
	}

	/**
	 * Erstellt auf Basis einer Schaltfläche einen Menüpunkt
	 * @param button	Ausgangsschaltfläche
	 * @return	Neuer Menüpunkt
	 * @see #commandContextMenu(MouseEvent)
	 */
	private JMenuItem buttonToMenu(final JButton button) {
		final JMenuItem item=new JMenuItem(button.getText(),button.getIcon());
		item.setToolTipText(button.getToolTipText());
		for (ActionListener listener : button.getActionListeners()) item.addActionListener(listener);
		item.setEnabled(button.isEnabled());
		return item;
	}

	/**
	 * Zeigt das Kontextmenü zu dem gewählten Listeneintrag an.
	 * @param event	Maus-Ereignis zur Ausrichtung des Kontextmenüs
	 */
	private void commandContextMenu(final MouseEvent event) {
		final JPopupMenu menu=new JPopupMenu();

		menu.add(buttonToMenu(buttonAdd));
		menu.add(buttonToMenu(buttonEdit));
		menu.add(buttonToMenu(buttonDelete));
		menu.add(buttonToMenu(buttonPull));
		menu.add(buttonToMenu(buttonPush));

		menu.show(event.getComponent(),event.getX(),event.getY());
	}

	/**
	 * Befehl: Git-Konfiguration hinzufügen
	 */
	private void commandAdd() {
		final GitSetup gitSetup=new GitSetup();
		final GitEditDialog dialog=new GitEditDialog(this,gitSetup);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			setups.add(gitSetup);
			updateList(setups.size()-1);
		}
	}

	/**
	 * Befehl: Gewählte Git-Konfiguration bearbeiten
	 */
	private void commandEdit() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final GitEditDialog dialog=new GitEditDialog(this,setups.get(index));
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			updateList(index);
		}
	}

	/**
	 * Befehl: Gewählte Git-Konfiguration löschen
	 */
	private void commandDelete() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		if (!MsgBox.confirm(this,Language.tr("Git.List.Delete.Confirm.Title"),Language.tr("Git.List.Delete.Confirm.Info"),Language.tr("Git.List.Delete.Confirm.InfoYes"),Language.tr("Git.List.Delete.Confirm.InfoNo"))) return;

		setups.remove(index);
		updateList(Math.max(0,index-1));
	}

	/**
	 * Befehl: Pull vom Git-Server für gewählte Git-Konfiguration ausführen
	 */
	private void commandPull() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final GitSetup setup=setups.get(index);
		if (!setup.useServer) return;

		final String error=(String)GitDialogProcessMonitor.run(this,Language.tr("Git.List.Pull"),progressMonitor->setup.doPull(progressMonitor));
		if (error==null) {
			MsgBox.info(this,Language.tr("Git.List.Pull"),String.format(Language.tr("Git.List.Pull.Success"),setup.serverURL,setup.localFolder));
		} else {
			MsgBox.error(this,Language.tr("Git.List.Pull"),String.format(Language.tr("Git.List.Pull.Error"),setup.serverURL,setup.localFolder,error));
		}
	}

	/**
	 * Befehl: Push auf den Git-Server ausführen
	 */
	private void commandPush() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final GitSetup setup=setups.get(index);
		if (!setup.useServer) return;

		if (!setup.hasCommitsToPush()) {
			MsgBox.error(this,Language.tr("Git.List.Push"),Language.tr("Git.List.Push.NoCommitsToPush"));
			return;
		}

		final String error=(String)GitDialogProcessMonitor.run(this,Language.tr("Git.List.Push"),progressMonitor->setup.doPushOnly(progressMonitor));
		if (error==null) {
			MsgBox.info(this,Language.tr("Git.List.Push"),String.format(Language.tr("Git.List.Push.Success"),setup.localFolder,setup.serverURL));
		} else {
			MsgBox.error(this,Language.tr("Git.List.Push"),String.format(Language.tr("Git.List.Push.Error"),setup.localFolder,setup.serverURL,error));
		}
	}

	/**
	 * Befehl: Pull vom Git-Server für alle Repositories ausführen
	 */
	private void commandPullAll() {
		int countSuccess=0;
		int countError=0;

		for (GitSetup setup: setups) if (setup.useServer) {
			if (GitDialogProcessMonitor.run(this,Language.tr("Git.List.Pull"),progressMonitor->setup.doPull(progressMonitor))==null) countSuccess++; else countError++;
		}

		MsgBox.info(this,Language.tr("Git.List.PullAll"),"<html><body>"+String.format(Language.tr("Git.List.PullAll.Info")+"</body></html>",countSuccess,countError));
	}

	/**
	 * Befehl: Push auf den Git-Server für alle Repositories ausführen
	 */
	private void commandPushAll() {
		int countSuccess=0;
		int countError=0;

		for (GitSetup setup: setups) if (setup.useServer && setup.hasCommitsToPush()) {
			if (GitDialogProcessMonitor.run(this,Language.tr("Git.List.Push"),progressMonitor->setup.doPushOnly(progressMonitor))==null) countSuccess++; else countError++;
		}

		MsgBox.info(this,Language.tr("Git.List.PushAll"),"<html><body>"+String.format(Language.tr("Git.List.PushAll.Info")+"</body></html>",countSuccess,countError));
	}

	@Override
	protected void storeData() {
		final SetupData setupData=SetupData.getSetup();
		setupData.gitSetups.clear();
		setupData.gitSetups.addAll(setups);
		setupData.saveSetup();
	}

	/**
	 * Renderer für die Einträge der Git-Konfigurationen-Liste {@link #list()}
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