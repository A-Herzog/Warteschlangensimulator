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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
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
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementNote;
import ui.modeleditor.elements.ModelElementNoteDialog;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Zeigt einen Dialog zum Bearbeiten der Notizen im Modell an.
 * @author Alexander Herzog
 * @see ModelElementNote
 */
public class NotesDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3572446091807711886L;

	/**
	 * Objekt das die verfügbaren Animations-Icons vorhält
	 */
	private final AnimationImageSource imageSource;

	/**
	 * Modell dem die Notizen entnommen werden sollen (und das beim Schließen mit "Ok" direkt verändert wird)
	 */
	private final EditModel model;

	/**
	 * Liste der Notizen
	 */
	private final List<ModelElementNote> notesList;

	/**
	 * Notizen-Liste
	 */
	private final JList<JLabel> list;

	/**
	 * Listenmodell welches die Daten für die Notizen-Liste vorhält
	 * @see #list
	 */
	private final DefaultListModel<JLabel> listModel;

	/**
	 * Schaltfläche "Bearbeiten" (der gewählten Notiz)
	 */
	private final JButton buttonEdit;

	/**
	 * Schaltfläche "Löschen" (der gewählten Notiz)
	 */
	private final JButton buttonDelete;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell dem die Notizen entnommen werden sollen (und das beim Schließen mit "Ok" direkt verändert wird)
	 */
	public NotesDialog(final Component owner, final EditModel model) {
		super(owner,Language.tr("NotesDialog.Title"));
		imageSource=new AnimationImageSource();
		this.model=model;
		notesList=new ArrayList<>();

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"Notes"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalNotes);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		/* Toolbar */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);
		toolbar.add(buttonEdit=new JButton(Language.tr("NotesDialog.Edit"),Images.MODEL_NOTES.getIcon()));
		buttonEdit.setToolTipText(Language.tr("NotesDialog.Edit.Hint"));
		buttonEdit.addActionListener(e->commandEdit());
		toolbar.add(buttonDelete=new JButton(Language.tr("NotesDialog.Delete"),Images.EDIT_DELETE.getIcon()));
		buttonDelete.setToolTipText(Language.tr("NotesDialog.Delete.Hint"));
		buttonDelete.addActionListener(e->commandDelete((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0));

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
				if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getModifiersEx()==InputEvent.SHIFT_DOWN_MASK) {
					commandEdit();
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_DELETE) {
					commandDelete(e.getModifiersEx()==InputEvent.SHIFT_DOWN_MASK);
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
		setLocationRelativeTo(getOwner());
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
	 * Ermittelt das Untermodell-Element, das zu einer Unterzeichenfläche gehört.
	 * @param surface	Unterzeichenfläche
	 * @return	Untermodell-Element (oder <code>null</code>, wenn kein passendes Untermodell-Element gefunden wurde)
	 */
	private ModelElementSub getSubElement(final ModelSurface surface) {
		for (ModelElement element: model.surface.getElements()) if (element instanceof ModelElementSub) {
			if (((ModelElementSub)element).getSubSurface()==surface) return (ModelElementSub)element;
		}
		return null;
	}

	/**
	 * Erstellt den Text für den Listeneintrag für eine Notiz.
	 * @param note	Notiz deren Text in der Liste dargestellt werden soll
	 * @return	html-Text für den Listeneintrag
	 */
	private String buildNotesText(final ModelElementNote note) {
		final StringBuilder text=new StringBuilder();

		text.append("<html><body>");
		text.append("<span style=\"color: blue\">");
		if (note.getSurface().getParentSurface()==null) {
			text.append(Language.tr("NotesDialog.Status.Main"));
		} else {
			final ModelElementSub sub=getSubElement(note.getSurface());
			if (sub!=null) text.append(String.format(Language.tr("NotesDialog.Status.Sub"),sub.getId()));
		}
		text.append("</span>");
		text.append("<br>");
		text.append("<b>");
		boolean first=true;
		for (String line: note.getNote().split("\\\n")) if (!line.trim().isEmpty()) {
			if (first) first=false; else text.append("<br>");
			text.append(encodeHTMLentities(line));
		}
		text.append("</b>");
		text.append("</body></html>");

		return text.toString();
	}

	/**
	 * Aktualisiert die Notizenliste
	 * @param selectIndex	Nach der Aktualisierung zu selektierender Index in der Liste
	 */
	private void updateList(final int selectIndex) {
		listModel.clear();

		/* Listeneinträge erstellen */
		notesList.clear();
		notesList.addAll(getNotes(model));
		for (ModelElementNote note: notesList) {
			final BufferedImage image=imageSource.get(note.getIcon(),model.animationImages,ModelElementNote.ICON_SIZE);
			final JLabel label;
			if (image==null) {
				listModel.addElement(label=new JLabel(buildNotesText(note)));
			} else {
				listModel.addElement(label=new JLabel(buildNotesText(note),new ImageIcon(image),SwingConstants.LEADING));
			}
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
		buttonEdit.setEnabled(list.getSelectedIndex()>=0);
		buttonDelete.setEnabled(list.getSelectedIndex()>=0);
	}

	/**
	 * Befehl: Bearbeiten
	 */
	private void commandEdit() {
		final int index=list.getSelectedIndex();
		if (index<0) return;
		final ModelElementNote note=notesList.get(list.getSelectedIndex());

		new ModelElementNoteDialog(this,note,false);

		updateList(index);
	}

	/**
	 * Befehl: Löschen
	 *  @param isShiftDown	Ist die Umschalttaste gedrückt? (Wenn ja, löschen ohne Nachfrage.)
	 */
	private void commandDelete(final boolean isShiftDown) {
		final int index=list.getSelectedIndex();
		if (index<0) return;
		final ModelElementNote note=notesList.get(list.getSelectedIndex());
		if (!isShiftDown) {
			if (!MsgBox.confirm(this,Language.tr("NotesDialog.Delete.Confirm.Title"),Language.tr("NotesDialog.Delete.Confirm.Info"),Language.tr("NotesDialog.Delete.Confirm.InfoYes"),Language.tr("NotesDialog.Delete.Confirm.InfoNo"))) return;
		}

		note.getSurface().remove(note);

		updateList(index-1);
	}

	/**
	 * Zeigt das Kontextmenü zu dem gewählten Listeneintrag an.
	 * @param event	Maus-Ereignis zur Ausrichtung des Kontextmenüs
	 */
	private void commandContextMenu(final MouseEvent event) {
		if (list.getSelectedIndex()<0) return;

		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(Language.tr("NotesDialog.Edit"),Images.MODEL_NOTES.getIcon()));
		item.setToolTipText(Language.tr("NotesDialog.Edit.Hint"));
		item.addActionListener(e->commandEdit());
		menu.add(item=new JMenuItem(Language.tr("NotesDialog.Delete"),Images.EDIT_DELETE.getIcon()));
		item.setToolTipText(Language.tr("NotesDialog.Delete.Hint"));
		item.addActionListener(e->commandDelete((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0));

		menu.show(event.getComponent(),event.getX(),event.getY());
	}

	/**
	 * Fügt alle Notizen einer Zeichenfläche (und ihrer Unterzeichenfläche) zu einer Liste hinzu.
	 * @param notes	Zu ergänzende Liste mit Notizen
	 * @param surface	Zeichenfläche deren Notizen in die Liste aufgenommen werden sollen
	 */
	private static void findNotes(final List<ModelElementNote> notes, final ModelSurface surface) {
		for (ModelElement element: surface.getElements()) {
			if (element instanceof ModelElementNote) notes.add((ModelElementNote)element);
			if (element instanceof ModelElementSub) findNotes(notes,((ModelElementSub)element).getSubSurface());
		}
	}

	/**
	 * Liefert eine Liste mit allen Notizen im Modell.
	 * @param model	Modell dem die Notizen entnommen werden sollen
	 * @return	Liste mit allen Notizen im Modell
	 */
	public static List<ModelElementNote> getNotes(final EditModel model) {
		final List<ModelElementNote> notes=new ArrayList<>();
		findNotes(notes,model.surface);
		return notes;
	}

	/**
	 * Renderer für die Einträge der Notizenliste {@link #list()}
	 */
	private static class JLabelRender implements ListCellRenderer<JLabel> {
		/**
		 * Konstruktor der Klasse
		 */
		public JLabelRender() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

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
