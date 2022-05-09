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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.script.ScriptTools;

/**
 * Dieser Dialog ermöglicht es Modelle auszuwählen, deren
 * Ergebnisse gespeichert oder verglichen werden sollen.
 * @author Alexander Herzog
 */
public class ParameterCompareStatisticSelectDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4239647935934489391L;

	/**
	 * Betriebsart dieses Dialogs
	 * @author Alexander Herzog
	 * @see ParameterCompareStatisticSelectDialog#ParameterCompareStatisticSelectDialog(Component, ParameterCompareSetup, Runnable, Mode)
	 */
	public enum Mode {
		/** Modelle zum Vergleichen auswählen */
		MODE_COMPARE,
		/** Statistikdaten zum Speichern auswählen */
		MODE_STORE
	}

	/** Modus des Dialogs (entweder Vergleichen von Modellen oder Speichern von Statistikdaten) */
	private final Mode mode;
	/** Eingabefeld für den Ausgabeordner */
	private JTextField folderEdit;
	/** Liste der Modelle, die exportiert werden können */
	private final List<ParameterCompareSetupModel> models;
	/** Listendarstellung der Modell (inkl. Auswahlmöglichkeit der zu exportierenden Modelle) */
	private final JList<JCheckBox> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param setup	Sammlung der Ergebnisse, aus denen gewählt werden soll
	 * @param help	Hilfe-Runnable
	 * @param mode	Modus des Dialogs (entweder Vergleichen von Modellen oder Speichern von Statistikdaten)
	 * @see Mode#MODE_COMPARE
	 * @see Mode#MODE_STORE
	 */
	public ParameterCompareStatisticSelectDialog(final Component owner, final ParameterCompareSetup setup, final Runnable help, final Mode mode) {
		super(owner,getTitle(mode));
		this.mode=mode;

		models=new ArrayList<>();
		for (ParameterCompareSetupModel model: setup.getModels()) if (model.isStatisticsAvailable()) models.add(model);

		addUserButton(Language.tr("ParameterCompare.Select.None"),Images.EDIT_DELETE.getIcon());
		addUserButton(Language.tr("ParameterCompare.Select.All"),Images.EDIT_ADD.getIcon());

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		if (mode==Mode.MODE_STORE) {
			final JPanel line=new JPanel(new BorderLayout());
			content.add(line,BorderLayout.NORTH);
			final JLabel label=new JLabel(Language.tr("ParameterCompare.Select.SelectFolder.Label")+":");
			line.add(label,BorderLayout.WEST);
			line.add(folderEdit=new JTextField(),BorderLayout.CENTER);
			label.setLabelFor(folderEdit);
			final JButton button=new JButton("");
			line.add(button,BorderLayout.EAST);
			button.setToolTipText(Language.tr("ParameterCompare.Select.SelectFolder.Hint"));
			button.addActionListener(e->{
				final String folder=ScriptTools.selectFolder(this,Language.tr("ParameterCompare.Select.SelectFolder.Hint"));
				if (folder!=null) folderEdit.setText(folder);
			});
			button.setIcon(Images.GENERAL_SELECT_FOLDER.getIcon());
		}

		list=new JList<>(models.stream().map(model->new JCheckBox(model.getName())).toArray(JCheckBox[]::new));
		list.setCellRenderer(new JCheckBoxCellRenderer());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index=list.locationToIndex(e.getPoint());
				if (index<0) return;
				final JCheckBox checkbox=list.getModel().getElementAt(index);
				checkbox.setSelected(!checkbox.isSelected());
				repaint();

			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		content.add(new JScrollPane(list),BorderLayout.CENTER);

		if (mode==Mode.MODE_STORE) {
			for (int i=0;i<list.getModel().getSize();i++) list.getModel().getElementAt(i).setSelected(true);
			list.repaint();
		}

		setMinSizeRespectingScreensize(650,400);
		setSizeRespectingScreensize(650,400);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Liefert den Titel des Dialogs
	 * @param mode	Modus des Dialogs (entweder Vergleichen von Modellen oder Speichern von Statistikdaten)
	 * @return	Titel des Dialogs
	 */
	private static String getTitle(final Mode mode) {
		switch (mode) {
		case MODE_COMPARE: return Language.tr("ParameterCompare.Select.Title.Compare");
		case MODE_STORE: return Language.tr("ParameterCompare.Select.Title.Store");
		default: return "";
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0:
			for (int i=0;i<list.getModel().getSize();i++) list.getModel().getElementAt(i).setSelected(false);
			list.repaint();
			break;
		case 1:
			for (int i=0;i<list.getModel().getSize();i++) list.getModel().getElementAt(i).setSelected(true);
			list.repaint();
			break;
		}
	}

	@Override
	protected boolean checkData() {
		int selCount=0;
		for (int i=0;i<list.getModel().getSize();i++) if (list.getModel().getElementAt(i).isSelected()) selCount++;
		if (selCount==0) {
			MsgBox.error(this,Language.tr("ParameterCompare.Select.SelectFolder.ErrorSelection.None.Title"),Language.tr("ParameterCompare.Select.SelectFolder.ErrorSelection.None.Info"));
			return false;
		}

		switch(mode) {
		case MODE_COMPARE:
			if (selCount>5) {
				MsgBox.error(this,Language.tr("ParameterCompare.Select.SelectFolder.ErrorSelection.NoMany.Title"),String.format(Language.tr("ParameterCompare.Select.SelectFolder.ErrorSelection.NoMany.Info"),selCount,5));
				return false;
			}
			return true;
		case MODE_STORE:
			if (folderEdit!=null) {
				final String folder=folderEdit.getText().trim();
				if (folder==null) {
					MsgBox.error(this,Language.tr("ParameterCompare.Select.SelectFolder.ErrorNoFolder.Title"),Language.tr("ParameterCompare.Select.SelectFolder.ErrorNoFolder.Info"));
					return false;
				}
				final File file=new File(folder);
				if (!file.isDirectory()) {
					MsgBox.error(this,Language.tr("ParameterCompare.Select.SelectFolder.ErrorFolderDoesNotExist.Title"),String.format(Language.tr("ParameterCompare.Select.SelectFolder.ErrorFolderDoesNotExist.Info"),folder));
					return false;
				}
			}
			return true;
		default:
			return true;
		}
	}

	/**
	 * Liefert eine Liste der gewählten Modelle
	 * @return	Liste der gewählten Modelle
	 */
	public List<ParameterCompareSetupModel> getSelected() {
		final List<ParameterCompareSetupModel> selected=new ArrayList<>();
		for (int i=0;i<list.getModel().getSize();i++) if (list.getModel().getElementAt(i).isSelected()) selected.add(models.get(i));
		return selected;
	}

	/**
	 * Liefert im Falle, dass es sich um einen Dialog zur Speicherung der Statistikergebnisse handelt,
	 * den gewählten Zielordner. Es wurde bereits geprüft, dass der Ordner existiert.
	 * @return	Gewählter Zielordner für die Statistikdaten
	 */
	public File getFolder() {
		if (folderEdit==null) return null;
		return new File(folderEdit.getText().trim());
	}

	/**
	 * Renderer für die Liste {@link ParameterCompareStatisticSelectDialog#list}
	 * @see ParameterCompareStatisticSelectDialog#list
	 */
	private static class JCheckBoxCellRenderer implements ListCellRenderer<JCheckBox> {
		/**
		 * Konstruktor der Klasse
		 */
		public JCheckBoxCellRenderer() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
			value.setForeground(list.getForeground());
			value.setBackground(list.getBackground());
			return value;
		}
	}
}