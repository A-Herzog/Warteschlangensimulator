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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
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
import simulator.simparser.symbols.CalcSymbolClientUserData;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;

/**
 * Diese Klasse stellt einen Dialog zum Auflisten von Kundenvariablen-Zuweisungen beim Routing dar.
 * @author Alexander Herzog
 * @see SequenceEditTableModel
 */
public class SequenceEditAssignmentsDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4649381127763073872L;

	private final Map<Integer,String> assignmentsOriginal;
	private final Map<Integer,String> assignments;
	private final Runnable help;
	private final EditModel model;

	private final JButton buttonAdd;
	private final JButton buttonEdit;
	private final JButton buttonDelete;
	private final JList<JLabel> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param assignments	Zuweisungen, die bearbeitet werden sollen
	 * @param help	Hilfe-Runnable
	 * @param model	Editor-Model (für den Expression-Builder-Dialog)
	 */
	public SequenceEditAssignmentsDialog(final Component owner, final Map<Integer,String> assignments, final Runnable help, final EditModel model) {
		super(owner,Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Title"));
		assignmentsOriginal=assignments;
		this.assignments=new HashMap<>();
		this.assignments.putAll(assignmentsOriginal);
		this.help=help;
		this.model=model;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		content.add(new JScrollPane(list=new JList<>(new DefaultListModel<>())),BorderLayout.CENTER);

		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.add(buttonAdd=getButton(Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Add"),Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Add.Hint"),Images.EDIT_ADD.getIcon(),()->commandAdd()));
		toolbar.add(buttonEdit=getButton(Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Edit"),Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Edit.Hint"),Images.GENERAL_SETUP.getIcon(),()->{if (list.getSelectedIndex()>=0) commandEdit(list.getSelectedIndex());}));
		toolbar.add(buttonDelete=getButton(Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Delete"),Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Delete.Hint"),Images.EDIT_DELETE.getIcon(),()->{if (list.getSelectedIndex()>=0) commandDelete(list.getSelectedIndex());}));

		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = -7787863612588403516L;
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
			}
		});
		list.addListSelectionListener(e->updateToolbar());

		updateList(-1);

		setMinSizeRespectingScreensize(500,400);
		setSizeRespectingScreensize(500,400);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private JButton getButton(final String title, final String hint, final Icon icon, final Runnable command) {
		final JButton button=new JButton(title);
		if (hint!=null && !hint.isEmpty()) button.setToolTipText(hint);
		button.addActionListener(e->command.run());
		button.setIcon(icon);
		return button;
	}

	private void updateToolbar() {
		buttonEdit.setEnabled(list.getSelectedIndex()>=0);
		buttonDelete.setEnabled(list.getSelectedIndex()>=0);
	}

	private DefaultListModel<JLabel> getListModel(final int[] sortedKeys) {
		final DefaultListModel<JLabel> model=new DefaultListModel<>();

		for (int key: sortedKeys) {
			final JLabel label=new JLabel(CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]+"("+key+"):="+assignments.get(key));
			label.setIcon(Images.MODELPROPERTIES_SEQUENCES_ASSIGNMENT.getIcon());
			model.addElement(label);
		}
		return model;
	}

	private void updateList(final int selectKey) {
		final int[] sortedKeys=assignments.keySet().stream().mapToInt(i->i).sorted().toArray();
		final DefaultListModel<JLabel> listModel=getListModel(sortedKeys);
		list.setModel(listModel);

		if (selectKey<0) {
			if (listModel.getSize()>0) list.setSelectedIndex(0);
		} else {
			int index=-1;
			for (int i=0;i<sortedKeys.length;i++) if (sortedKeys[i]==selectKey) {index=i; break;}
			if (index<0 && listModel.getSize()>0) index=0;
			if (index>=0) list.setSelectedIndex(index);
		}

		updateToolbar();
	}

	private Object[] editDialog(final int key, final String expression) {
		final SequenceEditAssignmentsEditDialog dialog=new SequenceEditAssignmentsEditDialog(this,key,expression,help,model);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return null;
		return new Object[]{Integer.valueOf(dialog.getKey()),dialog.getExpression()};
	}

	private void commandAdd() {
		int nextFreeKey=1;
		while (assignments.get(nextFreeKey)!=null) nextFreeKey++;
		final Object[] result=editDialog(nextFreeKey,"");
		if (result==null) return;
		assignments.put((Integer)result[0],(String)result[1]);
		updateList((Integer)result[0]);
	}

	private void commandEdit(final int index) {
		final int[] sortedKeys=assignments.keySet().stream().mapToInt(i->i).sorted().toArray();
		final Object[] result=editDialog(sortedKeys[index],assignments.get(sortedKeys[index]));
		if (result==null) return;
		assignments.remove(sortedKeys[index]);
		assignments.put((Integer)result[0],(String)result[1]);
		updateList((Integer)result[0]);
	}

	private void commandDelete(final int index) {
		final int[] sortedKeys=assignments.keySet().stream().mapToInt(i->i).sorted().toArray();
		final String s=CalcSymbolClientUserData.CLIENT_DATA_COMMANDS[0]+"("+sortedKeys[index]+"):="+assignments.get(sortedKeys[index]);
		if (!MsgBox.confirm(this,Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Delete.Confirm.Title"),String.format(Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Delete.Confirm.Info"),s),Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Delete.Confirm.InfoYes"),Language.tr("Editor.Dialog.Sequences.Edit.Assignments.Delete.Confirm.InfoNo"))) return;
		assignments.remove(sortedKeys[index]);
		updateList(-1);
	}

	private JMenuItem buttonToMenu(final JButton button) {
		final JMenuItem item=new JMenuItem(button.getText(),button.getIcon());
		item.setToolTipText(button.getToolTipText());
		for (ActionListener listener : button.getActionListeners()) item.addActionListener(listener);
		item.setEnabled(button.isEnabled());
		return item;
	}

	private void showContextMenu(final MouseEvent event) {
		final JPopupMenu menu=new JPopupMenu();

		menu.add(buttonToMenu(buttonAdd));
		menu.add(buttonToMenu(buttonEdit));
		menu.add(buttonToMenu(buttonDelete));

		menu.show(list,event.getX(),event.getY());
	}

	@Override
	protected void storeData() {
		assignmentsOriginal.clear();
		assignmentsOriginal.putAll(assignments);
	}
}