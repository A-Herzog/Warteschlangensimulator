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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelSequence;
import ui.modeleditor.ModelSequences;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementTransportDestination;

/**
 * Zeigt eine Liste der Fertigungspläne an und ermöglicht es, diese zu bearbeiten.
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 */
public class SequencesEditPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3732693280948871122L;

	private final ModelSequences sequences;
	private final List<ModelSequence> sequencesList;
	private final String[] destinations;
	private final boolean readOnly;
	private final Runnable help;
	private final EditModel model;

	private final JButton buttonAdd;
	private final JButton buttonEdit;
	private final JButton buttonDelete;
	private final JButton buttonCopy;
	private final JButton buttonMoveUp;
	private final JButton buttonMoveDown;

	private final JList<JLabel> list;

	/**
	 * Konstruktor der Klasse
	 * @param sequences	Objekt, welches die Fertigungspläne enthält
	 * @param surface	Zeichenoberfläche (aus der die Namen der möglichen Zielstationen ausgelesen werden)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Runnable
	 * @param model	Editor-Model (für den Expression-Builder-Dialog)
	 */
	public SequencesEditPanel(final ModelSequences sequences, final ModelSurface surface, final boolean readOnly, final Runnable help, final EditModel model) {
		super();
		this.sequences=sequences;
		destinations=getDestinationStations(surface);
		sequencesList=new ArrayList<>();
		for (ModelSequence sequence: sequences.getSequences()) sequencesList.add(sequence.clone());
		this.readOnly=readOnly;
		this.help=help;
		this.model=model;

		setLayout(new BorderLayout());

		add(new JScrollPane(list=new JList<>(new DefaultListModel<>())),BorderLayout.CENTER);

		final JToolBar toolBar=new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		add(toolBar,BorderLayout.NORTH);
		buttonAdd=addButton(toolBar,Language.tr("Editor.Dialog.Sequences.Add"),Language.tr("Editor.Dialog.Sequences.Add.Hint"),Images.EDIT_ADD.getIcon(),e->commandAdd());
		buttonEdit=addButton(toolBar,Language.tr("Editor.Dialog.Sequences.Edit"),Language.tr("Editor.Dialog.Sequences.Edit.Hint"),Images.GENERAL_SETUP.getIcon(),e->commandEdit(list.getSelectedIndex()));
		buttonDelete=addButton(toolBar,Language.tr("Editor.Dialog.Sequences.Delete"),Language.tr("Editor.Dialog.Sequences.Delete.Hint"),Images.EDIT_DELETE.getIcon(),e->commandDelete(list.getSelectedIndex()));
		buttonCopy=addButton(toolBar,Language.tr("Editor.Dialog.Sequences.Copy"),Language.tr("Editor.Dialog.Sequences.Copy.Hint"),Images.EDIT_COPY.getIcon(),e->commandCopy(list.getSelectedIndex()));
		toolBar.addSeparator();
		buttonMoveUp=addButton(toolBar,Language.tr("Editor.Dialog.Sequences.MoveUp"),Language.tr("Editor.Dialog.Sequences.MoveUp.Hint"),Images.ARROW_UP.getIcon(),e->commandMoveUp());
		buttonMoveDown=addButton(toolBar,Language.tr("Editor.Dialog.Sequences.MoveDown"),Language.tr("Editor.Dialog.Sequences.MoveDown.Hint"),Images.ARROW_DOWN.getIcon(),e->commandMoveDown());

		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 555672352480312004L;
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel l=(JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				l.setText(((JLabel)value).getText());
				l.setIcon(((JLabel)value).getIcon());
				return l;
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					if (list.getSelectedIndex()>=0) commandEdit(list.getSelectedIndex());
					e.consume();
					return;
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					showContextMenu(e);
					e.consume();
					return;
				}
			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT && !e.isControlDown() && !e.isShiftDown()) {commandAdd(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandEdit(list.getSelectedIndex()); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DELETE && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandDelete(list.getSelectedIndex()); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_UP && e.isControlDown() && !e.isShiftDown()) {commandMoveUp(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DOWN && e.isControlDown() && !e.isShiftDown()) {commandMoveDown(); e.consume(); return;}
			}
		});
		list.addListSelectionListener(e->updateToolbar());

		updateList(0);
		if (list.getModel().getSize()>0) {
			list.setSelectedIndex(0);
			updateToolbar();
		}
	}

	private JButton addButton(final JToolBar toolbar, final String title, final String hint, final Icon icon, final ActionListener listener) {
		final JButton button=new JButton(title);
		button.setToolTipText(hint);
		button.setIcon(icon);
		button.addActionListener(listener);
		button.setEnabled(!readOnly);
		toolbar.add(button);
		return button;
	}

	/**
	 * Liefert eine Liste mit den Namen aller Zielstationen
	 * @param surface	Zeichenoberfläche (aus der die Namen der möglichen Zielstationen ausgelesen werden)
	 * @return	Liste mit den Namen aller Zielstationen
	 */
	public static String[] getDestinationStations(ModelSurface surface) {
		final List<String> list=new ArrayList<>();

		if (surface.getParentSurface()!=null) surface=surface.getParentSurface();

		for (ModelElement e: surface.getElements()) {
			if (e instanceof ModelElementTransportDestination && !e.getName().trim().isEmpty()) list.add(e.getName());
			if (e instanceof ModelElementSub) {
				for (ModelElement e2: ((ModelElementSub)e).getSubSurface().getElements()) {
					if (e2 instanceof ModelElementTransportDestination && !e2.getName().trim().isEmpty()) list.add(e2.getName());
				}
			}
		}

		return list.toArray(new String[0]);
	}

	private void updateToolbar() {
		buttonEdit.setEnabled(!readOnly && list.getSelectedIndex()>=0);
		buttonDelete.setEnabled(!readOnly && list.getSelectedIndex()>=0);
		buttonCopy.setEnabled(!readOnly && list.getSelectedIndex()>=0);
		buttonMoveUp.setEnabled(!readOnly && list.getSelectedIndex()>=1);
		buttonMoveDown.setEnabled(!readOnly && list.getSelectedIndex()>=0 && list.getSelectedIndex()<list.getModel().getSize()-1);
	}

	private void updateList(final int deltaSelectedIndex) {
		final int selectedIndex=list.getSelectedIndex();

		final DefaultListModel<JLabel> listModel=new DefaultListModel<>();
		for (ModelSequence sequence: sequencesList) {
			final StringBuilder sb=new StringBuilder();
			sb.append("<html><body>");
			sb.append("<b>");
			sb.append(sequence.getName());
			sb.append("</b><br>");
			if (sequence.getSteps().size()==1) {
				sb.append(Language.tr("Editor.Dialog.Sequences.ListInfo.One"));
			} else {
				sb.append(String.format(Language.tr("Editor.Dialog.Sequences.ListInfo"),sequence.getSteps().size()));
			}
			sb.append("</body></html>");
			final JLabel label=new JLabel(sb.toString());
			label.setIcon(Images.MODELPROPERTIES_SEQUENCES.getIcon());
			listModel.addElement(label);
		}
		list.setModel(listModel);

		if (selectedIndex>=0 && listModel.getSize()>0) list.setSelectedIndex(Math.max(0,Math.min(listModel.getSize()-1,selectedIndex+deltaSelectedIndex)));

		updateToolbar();
	}

	private boolean editDialog(final ModelSequence sequence, final int index) {
		final SequenceEditDialog dialog=new SequenceEditDialog(this,sequence,sequencesList.toArray(new ModelSequence[0]),index,destinations,help,model);
		return dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK;
	}

	private void commandAdd() {
		if (readOnly) return;
		ModelSequence newSequence=new ModelSequence();
		if (!editDialog(newSequence,-1)) return;
		sequencesList.add(newSequence);
		updateList(Integer.MAX_VALUE);
	}

	private void commandEdit(final int index) {
		if (readOnly) return;
		if (!editDialog(sequencesList.get(index),index)) return;
		updateList(0);
	}

	private void commandDelete(final int index) {
		if (readOnly) return;
		if (index<0) return;
		if (!MsgBox.confirm(this,Language.tr("Editor.Dialog.Sequences.Delete.Confirm.Title"),String.format(Language.tr("Editor.Dialog.Sequences.Delete.Confirm.Info"),sequencesList.get(index).getName()),Language.tr("Editor.Dialog.Sequences.Delete.Confirm.InfoYes"),Language.tr("Editor.Dialog.Sequences.Delete.Confirm.InfoNo"))) return;
		sequencesList.remove(index);
		updateList(-1);
	}

	private void commandCopy(final int index) {
		if (readOnly) return;

		final ModelSequence sequence=sequencesList.get(index).clone();
		final SequenceCopyDialog dialog=new SequenceCopyDialog(this,sequence,sequencesList.toArray(new ModelSequence[0]),help);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			sequencesList.add(sequence);
			updateList(Integer.MAX_VALUE);
		}
	}

	private void commandSwap(final int index1, final int index2) {
		final ModelSequence temp=sequencesList.get(index1);
		sequencesList.set(index1,sequencesList.get(index2));
		sequencesList.set(index2,temp);
	}

	private void commandMoveUp() {
		if (readOnly) return;
		final int index=list.getSelectedIndex();
		if (index<1) return;
		commandSwap(index,index-1);
		updateList(-1);
	}

	private void commandMoveDown() {
		if (readOnly) return;
		final int index=list.getSelectedIndex();
		if (index<0 || index>=list.getModel().getSize()-1) return;
		commandSwap(index,index+1);
		updateList(1);
	}

	private JMenuItem buttonToMenu(final JButton button) {
		final JMenuItem item=new JMenuItem(button.getText(),button.getIcon());
		item.setToolTipText(button.getToolTipText());
		for (ActionListener listener : button.getActionListeners()) item.addActionListener(listener);
		item.setEnabled(button.isEnabled());
		return item;
	}

	private void showContextMenu(final MouseEvent event) {
		if (readOnly) return;
		final JPopupMenu menu=new JPopupMenu();

		menu.add(buttonToMenu(buttonAdd));
		menu.add(buttonToMenu(buttonEdit));
		menu.add(buttonToMenu(buttonDelete));
		menu.add(buttonToMenu(buttonCopy));
		menu.addSeparator();
		menu.add(buttonToMenu(buttonMoveUp));
		menu.add(buttonToMenu(buttonMoveDown));

		menu.show(list,event.getX(),event.getY());
	}

	/**
	 * Schreibt die geänderten Einstellungen in das im Konstruktur übergebene Fertigungspläne-Objekt zurück
	 */
	public void storeData() {
		sequences.getSequences().clear();
		for (ModelSequence sequence: sequencesList) sequences.getSequences().add(sequence);
	}
}
