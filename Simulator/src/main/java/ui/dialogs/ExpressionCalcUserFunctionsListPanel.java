/**
 * Copyright 2024 Alexander Herzog
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
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.simparser.ExpressionCalcUserFunctionsManager;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;

/**
 * In diesem Panel werden alle nutzerdefinieren Rechenfunktionen aufgelistet.
 * Neue Funktionen können angelegt und bestehende Verändert oder gelöscht werden.
 * @see ExpressionCalcUserFunctionsListDialog
 */
public class ExpressionCalcUserFunctionsListPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-4238818953612406803L;

	/**
	 * Aktuelle Liste der nutzerdefinierten Funktionen
	 */
	private final List<ExpressionCalcUserFunctionsManager.UserFunction> userFunctions;

	/**
	 * Listendarstellung der nutzerdefinierten Funktionen
	 */
	private final JList<JLabel> list;

	/**
	 * Datenmodell für die Listendarstellung in {@link #list}
	 * @see #list
	 */
	private final DefaultListModel<JLabel> listModel;

	/**
	 * Liste der Elemente, die deaktiviert werden müssen, wenn kein Eintrag in {@link #list} ausgewählt ist
	 */
	private final List<Component> allowEdit;

	/**
	 * Konstruktor der Klasse
	 * @param userFunctions	Liste der in dem Panel aufzulistenden nutzerdefinierten Funktionen
	 */
	public ExpressionCalcUserFunctionsListPanel(final List<ExpressionCalcUserFunctionsManager.UserFunction> userFunctions) {
		this.userFunctions=new ArrayList<>(userFunctions.stream().map(userFunction->new ExpressionCalcUserFunctionsManager.UserFunction(userFunction)).collect(Collectors.toList()));
		allowEdit=new ArrayList<>();

		setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolBar=new JToolBar();
		toolBar.setFloatable(false);
		add(toolBar,BorderLayout.NORTH);
		JButton button;
		toolBar.add(button=new JButton(Language.tr("UserDefinedFunctions.List.Add"),Images.EDIT_ADD.getIcon()));
		button.setToolTipText(Language.tr("UserDefinedFunctions.List.AddHint")+ "("+getHotkeyText(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,0))+")");
		button.addActionListener(e->commandAdd());
		toolBar.add(button=new JButton(Language.tr("UserDefinedFunctions.List.Edit"),Images.GENERAL_EDIT.getIcon()));
		button.setToolTipText(Language.tr("UserDefinedFunctions.List.EditHint")+ "("+getHotkeyText(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0))+")");
		button.addActionListener(e->commandEdit());
		allowEdit.add(button);
		toolBar.add(button=new JButton(Language.tr("UserDefinedFunctions.List.Delete"),Images.EDIT_DELETE.getIcon()));
		button.setToolTipText(Language.tr("UserDefinedFunctions.List.DeleteHint")+ "("+getHotkeyText(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0))+")");
		button.addActionListener(e->commandDelete());
		allowEdit.add(button);

		/* Liste */
		add(new JScrollPane(list=new JList<>()));
		list.setModel(listModel=new DefaultListModel<>());
		list.setCellRenderer(new JLabelRender());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {commandEdit(); e.consume(); return;}
				if (SwingUtilities.isRightMouseButton(e)) {showContextMenu(e); e.consume(); return;}
			}
		});
		list.addListSelectionListener(e->updateButtons());
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT && e.getModifiersEx()==0) {commandAdd(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER && e.getModifiersEx()==0) {commandEdit(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DELETE && e.getModifiersEx()==0) {commandDelete(); e.consume(); return;}
			}
		});

		/* Dialog starten */
		buildList(0);
	}

	/**
	 * Aktualisiert den Aktivierungsstatus verschiedener Schaltflächen
	 * nachdem ein anderer Eintrag in {@link #list} ausgewählt wurde.
	 * @see #list
	 */
	private void updateButtons() {
		final boolean isItemSelected=list.getSelectedIndex()>=0;
		allowEdit.forEach(component->component.setEnabled(isItemSelected));
	}

	/**
	 * Aktualisiert die Daten in {@link #listModel} basierend auf {@link #userFunctions}.
	 * @param moveDelta	Relative Verschiebung der Selektionsmarkierung
	 */
	private void buildList(final int moveDelta) {
		final int selectedIndex=(list.getSelectedIndex()>=0)?Math.max(0,list.getSelectedIndex()+moveDelta):0;
		listModel.clear();

		for (ExpressionCalcUserFunctionsManager.UserFunction userFunction: userFunctions) {
			final StringBuilder labelText=new StringBuilder();
			labelText.append("<html><body>");
			labelText.append("<b>"+userFunction.name+"</b>(");
			switch (userFunction.mode) {
			case EXPRESSION:
				for (int i=0;i<userFunction.parameterCount;i++) {
					if (i>0) labelText.append(";");
					labelText.append("Parameter"+(i+1));
				}
				break;
			case JAVASCRIPT:
				labelText.append("...");
				break;
			}

			labelText.append("):=<br>");
			switch (userFunction.mode) {
			case EXPRESSION:
				labelText.append("<b>"+userFunction.content+"</b>");
				break;
			case JAVASCRIPT:
				labelText.append(Language.tr("UserDefinedFunctions.List.JavascriptCode"));
				break;
			}
			labelText.append("</body></html>");
			final JLabel label=new JLabel(labelText.toString(),Images.EXPRESSION_BUILDER_FUNCTION.getIcon(),SwingConstants.LEADING);
			listModel.addElement(label);
			label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
		}

		if (listModel.getSize()>0) list.setSelectedIndex(Math.min(selectedIndex,listModel.getSize()-1));
		updateButtons();
	}

	/**
	 * Bestimmt einen Hotkey-Text aus einem Hotkey-Objekt
	 * @param hotkey	Hotkey-Objekt für das ein Text ermittelt werden soll
	 * @return	Hotkey-Text
	 */
	private static String getHotkeyText(final KeyStroke hotkey) {
		final int modifiers=hotkey.getModifiers();
		String acceleratorText=(modifiers==0)?"":InputEvent.getModifiersExText(modifiers)+"+";
		acceleratorText+=KeyEvent.getKeyText(hotkey.getKeyCode());
		return acceleratorText;
	}

	/**
	 * Zeigt das Kontextmenü zu einem Listeneintrag an.
	 * @param event	Auslösendes Mausereignis
	 */
	private void showContextMenu(final MouseEvent event) {
		final JPopupMenu menu=new JPopupMenu();
		JMenuItem item;

		menu.add(item=new JMenuItem(Language.tr("UserDefinedFunctions.List.Add"),Images.EDIT_ADD.getIcon()));
		item.addActionListener(e->commandAdd());
		if (list.getSelectedIndex()>=0) {
			menu.add(item=new JMenuItem(Language.tr("UserDefinedFunctions.List.Edit"),Images.GENERAL_EDIT.getIcon()));
			item.addActionListener(e->commandEdit());
			menu.add(item=new JMenuItem(Language.tr("UserDefinedFunctions.List.Delete"),Images.EDIT_DELETE.getIcon()));
			item.addActionListener(e->commandDelete());
		}

		menu.show(list,event.getX(),event.getY());
	}

	/**
	 * Befehl: Funktion hinzufügen
	 */
	private void commandAdd() {
		final ExpressionCalcUserFunctionsEditDialog dialog=new ExpressionCalcUserFunctionsEditDialog(this,userFunctions,null);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			userFunctions.add(dialog.getUserFunction());
			buildList(0);
			list.setSelectedIndex(listModel.getSize()-1);
			updateButtons();
		}
	}

	/**
	 * Befehl: Funktion bearbeiten
	 */
	private void commandEdit() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final ExpressionCalcUserFunctionsEditDialog dialog=new ExpressionCalcUserFunctionsEditDialog(this,userFunctions,userFunctions.get(index));
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			userFunctions.set(index,dialog.getUserFunction());
			buildList(0);
		}
	}

	/**
	 * Befehl: Funktion löschen
	 */
	private void commandDelete() {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		if (!MsgBox.confirm(this,Language.tr("UserDefinedFunctions.List.DeleteConfirmTitle"),String.format(Language.tr("UserDefinedFunctions.List.DeleteConfirmInfo"),userFunctions.get(index).name),Language.tr("UserDefinedFunctions.List.DeleteConfirmInfoYes"),Language.tr("UserDefinedFunctions.List.DeleteConfirmInfoNo"))) return;

		userFunctions.remove(index);
		buildList(-1);
	}

	/**
	 * Liefert die aktuelle Liste der nutzerdefinierten Funktionen
	 * @return	Liste der nutzerdefinierten Funktionen
	 */
	public List<ExpressionCalcUserFunctionsManager.UserFunction> getUserFunctions() {
		return userFunctions;
	}

	/**
	 * Renderer für die Einträge der Branches-Liste {@link #list()}
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
