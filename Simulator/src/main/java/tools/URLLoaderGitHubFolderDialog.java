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
package tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;

/**
 * Zeigt einen Dialog zur Auswahl einer Datei aus einem von
 * {@link URLLoaderGitHubFolder} ausgelesenen Verzeichnis
 * zum Download an.
 * @author Alexander Herzog
 * @see URLLoader
 * @see URLLoaderGitHubFolder
 */
public class URLLoaderGitHubFolderDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=5306520666681203143L;

	/**
	 * Zum Download anzubietende Verzeichniseinträge
	 */
	final List<URLLoaderGitHubFolder.FileRecord> records;

	/**
	 * Liste mit den zum Download anzubietenden Verzeichniseinträgen
	 */
	private final JList<JLabel> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param records	Zum Download anzubietende Verzeichniseinträge
	 */
	public URLLoaderGitHubFolderDialog(final Component owner, final List<URLLoaderGitHubFolder.FileRecord> records) {
		super(owner,Language.tr("URLLoader.Select.Title"));
		this.records=records;

		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		final DefaultListModel<JLabel> model=new DefaultListModel<>();
		for (URLLoaderGitHubFolder.FileRecord record: records) {
			final StringBuilder text=new StringBuilder();
			text.append("<html><body>");
			text.append("<b>"+record.name+"</b><br>");
			text.append(Language.tr("URLLoader.Select.Repository"));
			text.append(": ");
			text.append(record.repo);
			text.append(" (");
			text.append(Language.tr("URLLoader.Select.User"));
			text.append(": ");
			text.append(record.user);
			text.append(", ");
			text.append(Language.tr("URLLoader.Select.Branch"));
			text.append(": ");
			text.append(record.branch);
			text.append(")<br>");
			text.append(Language.tr("URLLoader.Select.FileSize"));
			text.append(": ");
			text.append((int)Math.round(record.size/1024.0));
			text.append(" kb");
			text.append("</body></html>");
			final JLabel label=new JLabel(text.toString(),Images.HELP_HOMEPAGE.getIcon(),SwingConstants.LEADING);
			label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			model.addElement(label);
		}
		content.add(new JScrollPane(list=new JList<>(model)));
		list.setCellRenderer(new JLabelRender());
		list.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) close(BaseDialog.CLOSED_BY_OK);
			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override public void keyTyped(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) close(BaseDialog.CLOSED_BY_OK);
			}
		});

		/* Dialog starten */
		setMinSizeRespectingScreensize(800,600);
		setSizeRespectingScreensize(800,600);
		pack();
		if (getHeight()>1024) setSize(getWidth(),1024);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected boolean checkData() {
		if (list.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("URLLoader.Select.NoFileSelected.Title"),Language.tr("URLLoader.Select.NoFileSelected.Info"));
			return false;
		}
		return true;
	}

	/**
	 * Liefert die ausgewählte URL
	 * @return	Ausgewählte URL
	 */
	public String getSelectedURL() {
		return records.get(list.getSelectedIndex()).url;
	}

	/**
	 * Renderer für die Einträge von {@link URLLoaderGitHubFolderDialog#list}
	 * @see URLLoaderGitHubFolderDialog#list
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