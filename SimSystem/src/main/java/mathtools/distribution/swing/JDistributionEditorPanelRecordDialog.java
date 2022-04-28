/**
 * Copyright 2022 Alexander Herzog
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
package mathtools.distribution.swing;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Editordialog zum Bearbeiten der Liste der Verteilungen, die
 * hervorgehoben dargestellt werden sollen.
 * @author Alexander Herzog
 */
public class JDistributionEditorPanelRecordDialog extends JDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=3637641916895163877L;

	/**
	 * Daten für {@link #list}
	 */
	private final List<JDistributionEditorPanelRecord> records;

	/**
	 * Liste der Verteilungen
	 */
	private final JList<JDistributionEditorPanelRecord> list;

	/**
	 * Gibt an, ob die "Ok"-Schaltfläche angeklickt wurde.
	 * @see #getFilter()
	 */
	private boolean okButtonPressed=false;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param filter	Bisherige Liste der hervorzuhebenden Verteilungen
	 */
	public JDistributionEditorPanelRecordDialog(final Window owner, final String filter) {
		super(owner,JDistributionEditorPanel.SetupListTitle,Dialog.ModalityType.DOCUMENT_MODAL);

		/* GUI */
		addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent event) {setVisible(false);}
		});
		setLayout(new BorderLayout());

		/* Infozeile oben */
		final JPanel infoPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(infoPanel,BorderLayout.NORTH);
		infoPanel.add(new JLabel(JDistributionEditorPanel.SetupListInfo));

		/* Liste */
		records=JDistributionEditorPanelRecord.getList(Arrays.asList(filter.trim().split("\\n")),true,true);
		add(new JScrollPane(list=new JList<>()));
		list.setCellRenderer(new JDistributionEditorPanel.DistributionComboBoxRenderer());
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isControlDown() && list.getSelectedIndex()>=0) {
					if (e.getKeyCode()==KeyEvent.VK_UP) moveListItem(-1);
					if (e.getKeyCode()==KeyEvent.VK_DOWN) moveListItem(1);
				}
			}
		});
		updateList();

		/* Button-Zeile */
		JPanel buttonPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); add(buttonPanel,BorderLayout.SOUTH);

		/* Ok */
		final JButton okButton=new JButton(JDistributionEditorPanel.ButtonOk);
		buttonPanel.add(okButton);
		okButton.addActionListener(e->{okButtonPressed=true; setVisible(false);});
		okButton.setIcon(SimSystemsSwingImages.OK.getIcon());

		/* Abbruch */
		final JButton cancelButton=new JButton(JDistributionEditorPanel.ButtonCancel);
		buttonPanel.add(cancelButton);
		cancelButton.addActionListener(e->setVisible(false));
		cancelButton.setIcon(SimSystemsSwingImages.CANCEL.getIcon());

		/* Hotkey für "Ok" */
		getRootPane().setDefaultButton(okButton);

		/* Dialog vorbereiten */
		setResizable(true);
		setSize(520,640);
		setMinimumSize(new Dimension(520,640));
		setLocationRelativeTo(owner);
	}

	/**
	 * Trägt die Datensätze aus {@link #records} in {@link #list} ein.
	 * @see #records
	 * @see #list
	 */
	private void updateList() {
		final JDistributionEditorPanelRecord selectItem=list.getSelectedValue();
		final String selectName=(selectItem==null)?"":selectItem.getName();

		final DefaultListModel<JDistributionEditorPanelRecord> model=new DefaultListModel<>();
		for (JDistributionEditorPanelRecord record: records) model.addElement(record);
		list.setModel(model);

		for (int i=0;i<records.size();i++) if (records.get(i).getName().equals(selectName)) {
			list.setSelectedIndex(i);
			break;
		}
	}

	/**
	 * Verschiebt den aktuell in der Liste ausgewählten Eintrag nach oben
	 * oder unten (und aktualisiert dabei natürlich auch {@link #records})
	 * @param delta	Nach oben (-1) oder nach unten (1) verschieben
	 * @see #list
	 * @see #records
	 */
	private void moveListItem(final int delta) {
		final int selected=list.getSelectedIndex();

		if (delta==-1 && selected>0) {
			final JDistributionEditorPanelRecord temp=records.get(selected-1);
			records.set(selected-1,records.get(selected));
			records.set(selected,temp);
		}

		if (delta==1 && selected<records.size()-1) {
			final JDistributionEditorPanelRecord temp=records.get(selected+1);
			records.set(selected+1,records.get(selected));
			records.set(selected,temp);
		}

		boolean isHighlight=true;
		for (JDistributionEditorPanelRecord record: records) {
			if (record.isSeparator()) {isHighlight=false; continue;}
			record.highlight=isHighlight;
		}

		updateList();
	}

	/**
	 * Liefert die neue Liste der hervorzuhebenden Verteilungen
	 * @return	Neue Liste der hervorzuhebenden Verteilungen oder <code>null</code>, wenn der Dialog abgebrochen wurde
	 */
	public String getFilter() {
		if (!okButtonPressed) return null;

		final StringBuilder result=new StringBuilder();
		for (JDistributionEditorPanelRecord record: records) if (record.highlight) {
			if (result.length()>0) result.append("\n");
			result.append(record.getName());
		}
		return result.toString();
	}
}
