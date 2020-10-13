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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;

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
import systemtools.BaseDialog;
import ui.images.Images;

/**
 * Diese Klasse stellt einige Basisfunktionen zur Darstellung von Dialogen
 * zur Auflistung von Eingabeparametern und von Ausgabegrößen bereit.
 * @author Alexander Herzog
 * @see ParameterCompareSetupValueInputListDialog
 * @see ParameterCompareSetupValueOutputListDialog
 */
public abstract class ParameterCompareSetupValueBaseListDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -8145433432850373442L;

	/**
	 * Editor-Modell, welches die Basis für die Parameterstudie darstellt
	 */
	protected final EditModel model;

	/**
	 * Runnable, das aufgerufen werden kann, wenn die Hilfe zu der Programmfunktion angezeigt werden soll.
	 */
	protected final Runnable help;

	/**
	 * Toolbar des Dialogs
	 */
	protected final JToolBar toolbar;

	private final JButton buttonAdd;
	private final JButton buttonEdit;
	private final JButton buttonDelete;
	private final JButton buttonMoveUp;
	private final JButton buttonMoveDown;
	private final JList<JLabel> list;

	/**
	 * Konstruktor der Klasse.<br>
	 * @param owner	Übergeordnetes Element
	 * @param title	Fenstertitel
	 * @param model	Editor-Modell, welches die Basis für die Parameterstudie darstellt
	 * @param help	Hilfe-Runnable
	 * @see ParameterCompareSetupValueBaseListDialog#initToolbar(String, String, String, String, String, String, String, String, String, String)
	 * @see ParameterCompareSetupValueBaseListDialog#start()
	 */
	public ParameterCompareSetupValueBaseListDialog(final Component owner, final String title, final EditModel model, final Runnable help) {
		super(owner,title);

		this.model=model;
		this.help=help;

		addUserButtons();
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		content.add(new JScrollPane(list=new JList<>(new DefaultListModel<>())),BorderLayout.CENTER);

		toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.add(buttonAdd=getButton(Images.EDIT_ADD.getIcon(),()->commandAddPopup()));
		toolbar.add(buttonEdit=getButton(Images.GENERAL_SETUP.getIcon(),()->{if (list.getSelectedIndex()>=0) commandEdit(list.getSelectedIndex());}));
		toolbar.add(buttonDelete=getButton(Images.EDIT_DELETE.getIcon(),()->{if (list.getSelectedIndex()>=0) commandDelete(list.getSelectedIndex());}));
		toolbar.addSeparator();
		toolbar.add(buttonMoveUp=getButton(Images.ARROW_UP.getIcon(),()->commandMoveUp()));
		toolbar.add(buttonMoveDown=getButton(Images.ARROW_DOWN.getIcon(),()->commandMoveDown()));

		list.setCellRenderer(new DefaultListCellRenderer() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
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
				if (e.getKeyCode()==KeyEvent.VK_INSERT && !e.isControlDown() && !e.isShiftDown()) {commandAdd(0); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandEdit(list.getSelectedIndex()); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DELETE && !e.isControlDown() && !e.isShiftDown()) {if (list.getSelectedIndex()<0) return; commandDelete(list.getSelectedIndex()); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_UP && e.isControlDown() && !e.isShiftDown()) {commandMoveUp(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DOWN && e.isControlDown() && !e.isShiftDown()) {commandMoveDown(); e.consume(); return;}
			}
		});
		list.addListSelectionListener(e->updateToolbar());
	}

	/**
	 * In dieser Methode kann {@link BaseDialog#addUserButton} aufgerufen werden.
	 */
	protected void addUserButtons() {}

	/**
	 * Initialisiert den Toolbar.<br>
	 * Sollte im Konstruktor aufgerufen werden.
	 * @param add	Titel der "Hinzufügen"-Schaltfläche
	 * @param addHint	Tooltip der "Hinzufügen"-Schaltfläche
	 * @param edit	Titel der "Bearbeiten"-Schaltfläche
	 * @param editHint	Tooltip der "Bearbeiten"-Schaltfläche
	 * @param delete	Titel der "Entfernen"-Schaltfläche
	 * @param deleteHint	Tooltip der "Entfernen"-Schaltfläche
	 * @param moveUp	Titel der "Nach oben"-Schaltfläche
	 * @param moveUpHint	Tooltip der "Nach oben"-Schaltfläche
	 * @param moveDown	Titel der "Nach unten"-Schaltfläche
	 * @param moveDownHint	Tooltip der "Nach unten"-Schaltfläche
	 */
	protected final void initToolbar(final String add, final String addHint, final String edit, final String editHint, final String delete, final String deleteHint, final String moveUp, final String moveUpHint, final String moveDown, final String moveDownHint) {
		buttonAdd.setText(add);
		buttonAdd.setToolTipText(addHint);
		buttonEdit.setText(edit);
		buttonEdit.setToolTipText(editHint);
		buttonDelete.setText(delete);
		buttonDelete.setToolTipText(deleteHint);
		buttonMoveUp.setText(moveUp);
		buttonMoveUp.setToolTipText(moveUpHint);
		buttonMoveDown.setText(moveDown);
		buttonMoveDown.setToolTipText(moveDownHint);
	}

	/**
	 * Richtet die Dialoggröße ein und macht den Dialog sichtbar.<br>
	 * Sollte im Konstruktor aufgerufen werden.
	 */
	protected final void start() {
		updateList(0);

		setMinSizeRespectingScreensize(650,500);
		setSizeRespectingScreensize(650,500);
		setResizable(true);
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	private JButton getButton(final Icon icon, final Runnable command) {
		final JButton button=new JButton("");
		button.addActionListener(e->command.run());
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	private void updateToolbar() {
		buttonEdit.setEnabled(list.getSelectedIndex()>=0);
		buttonDelete.setEnabled(list.getSelectedIndex()>=0);
		buttonMoveUp.setEnabled(list.getSelectedIndex()>=1);
		buttonMoveDown.setEnabled(list.getSelectedIndex()>=0 && list.getSelectedIndex()<list.getModel().getSize()-1);
	}

	/**
	 * Liefert die Einträge des Listenmodells
	 * @return	Einträge des Listenmodells
	 */
	protected abstract DefaultListModel<JLabel> getListModel();

	/**
	 * Aktualisiert die Liste
	 * @param deltaSelectedIndex	Gibt an, um wie viele Einträge die Selektion (sofern vorhanden) nach oben oder unten verschoben werden soll
	 */
	protected final void updateList(final int deltaSelectedIndex) {
		final int selectedIndex=list.getSelectedIndex();

		final DefaultListModel<JLabel> listModel=getListModel();
		list.setModel(listModel);

		if (selectedIndex>=0 && listModel.getSize()>0) list.setSelectedIndex(Math.max(0,Math.min(listModel.getSize()-1,selectedIndex+deltaSelectedIndex)));

		updateToolbar();
	}

	/**
	 * Fügt einen einzelnen Eintrag zu dem "Hinzufügen"-Popupmenü hinzu und verknüpft diesen mit dem {@link ParameterCompareSetupValueBaseListDialog#commandAdd(int)}-Listener
	 * @param popupMenu	Popupmenü zu dem der Eintrag hinzugefügt werden soll
	 * @param title	Name des Eintrags
	 * @param hint	Tooltip für den Eintrag
	 * @param icon	Icon für den Eintrag
	 * @param nr	Nummer, die dem Listener übergeben werden soll
	 * @see ParameterCompareSetupValueBaseListDialog#commandAdd(int)
	 * @see ParameterCompareSetupValueBaseListDialog#addAddModesToMenu(JButton, JPopupMenu)
	 */
	protected final void addAddButton(final JPopupMenu popupMenu, final String title, final String hint, final Icon icon, final int nr) {
		JMenuItem item=new JMenuItem(title);
		item.setToolTipText(hint);
		if (icon!=null) item.setIcon(icon);
		item.addActionListener(e->commandAdd(nr));
		popupMenu.add(item);
	}

	/**
	 * Bietet die Möglichkeit, normale Einträge zu dem "Hinzufügen"-Popupmenü hinzuzufügen (d.h. vor dem Vorlagen-Bereich)
	 * @param anchor	Element, an dem das Popupmenü ausgerichtet werden soll
	 * @param popupMenu	Popupmenü zu dem die Einträge hinzugefügt werden können
	 * @see ParameterCompareSetupValueBaseListDialog#addTemplatesToMenu(JButton, JPopupMenu)
	 * @see ParameterCompareSetupValueBaseListDialog#addAddButton(JPopupMenu, String, String, Icon, int)
	 */
	protected void addAddModesToMenu(final JButton anchor, final JPopupMenu popupMenu) {
		addAddButton(popupMenu,Language.tr("ParameterCompare.Settings.List.AddByXML"),Language.tr("ParameterCompare.Settings.List.AddByXML.Hint"),Images.PARAMETERSERIES_SELECT_XML.getIcon(),0);
	}

	private void commandAddPopup() {
		final JPopupMenu popupMenu=new JPopupMenu();

		addAddModesToMenu(buttonAdd,popupMenu);
		popupMenu.addSeparator();
		addTemplatesToMenu(buttonAdd,popupMenu);
		if (popupMenu.getComponent(popupMenu.getComponentCount()-1) instanceof JPopupMenu.Separator) popupMenu.remove(popupMenu.getComponentCount()-1);

		popupMenu.show(buttonAdd,0,buttonAdd.getHeight());
	}

	/**
	 * Bietet die Möglichkeit, Vorlagen-Einträge zu dem "Hinzufügen"-Popupmenü hinzuzufügen
	 * @param anchor	Element, an dem das Popupmenü ausgerichtet werden soll
	 * @param popupMenu	Popupmenü zu dem die Einträge hinzugefügt werden können
	 * @see ParameterCompareSetupValueBaseListDialog#addAddModesToMenu(JButton, JPopupMenu)
	 */
	protected abstract void addTemplatesToMenu(final JButton anchor, final JPopupMenu popupMenu);

	/**
	 * Wird aufgerufen, wenn der Nutzer einen Eintrag hinzufügen will
	 * @param nr	Nummer, die vom Listener übergeben wurde
	 */
	protected abstract void commandAdd(final int nr);

	/**
	 * Wird aufgerufen, wenn der Nutzer einen Eintrag bearbeiten will
	 * @param index	Index des zu bearbeitenden Eintrags
	 */
	protected abstract void commandEdit(final int index);

	/**
	 * Wird aufgerufen, wenn der Nutzer einen Eintrag löschen will
	 * @param index	Index des zu löschenden Eintrags
	 */
	protected abstract void commandDelete(final int index);

	/**
	 * Wird aufgerufen, wenn zwei Einträge in der Liste vertauscht werden sollen
	 * @param index1	Erster Tausch-Index
	 * @param index2	Zweiter Tausch-Index
	 */
	protected abstract void commandSwap(final int index1, final int index2);

	private void commandMoveUp() {
		final int index=list.getSelectedIndex();
		if (index<1) return;
		commandSwap(index,index-1);
		updateList(-1);
	}

	private void commandMoveDown() {
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
		final JPopupMenu menu=new JPopupMenu();

		menu.add(buttonToMenu(buttonAdd));
		menu.add(buttonToMenu(buttonEdit));
		menu.add(buttonToMenu(buttonDelete));
		menu.addSeparator();
		menu.add(buttonToMenu(buttonMoveUp));
		menu.add(buttonToMenu(buttonMoveDown));

		menu.show(list,event.getX(),event.getY());
	}
}