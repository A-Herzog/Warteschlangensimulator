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
package ui.modeleditor.templates;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;

/**
 * Dialog zur Bearbeitung der Vorlagen
 * @author Alexander Herzog
 * @see UserTemplates
 */
public final class TemplatesListDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -5093595007607365512L;

	/** Liste der global verfügbaren Vorlagen */
	private final UserTemplates globalTemplates;
	/** Liste der in dem Modell verfügbaren Vorlagen */
	private final UserTemplates modelTemplates;

	/** Datenmodell der Darstellung der Vorlagen in {@link #list} */
	private final DefaultListModel<ListRecord> listModel;
	/** Darstellung der Vorlagen als Liste */
	private final JList<ListRecord> list;


	/**
	 * Wurde beim Schließen des Dialogs eine Vorlage zum Einfügen in das Modell
	 * gewählt, so wird sie hier zwischengespeichert.
	 * @see #getUseTemplate()
	 */
	private UserTemplate useTemplate=null;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param modelTemplates	Modellbasierende Vorlagen
	 */
	public TemplatesListDialog(final Component owner, final UserTemplates modelTemplates) {
		super(owner,Language.tr("UserTemplates.TemplatesDialog.Title"));

		/* Daten laden */

		globalTemplates=UserTemplates.getInstance();
		this.modelTemplates=modelTemplates;
		listModel=new DefaultListModel<>();
		for (UserTemplate template: globalTemplates.getTemplates()) listModel.addElement(new ListRecord(template,true));
		for (UserTemplate template: modelTemplates.getTemplates()) listModel.addElement(new ListRecord(template,false));

		/* Zusätzliche Buttons */

		addUserButton(Language.tr("UserTemplates.TemplatesDialog.Use"),Images.EDIT_PASTE.getIcon());
		addUserButton("",Images.GENERAL_TOOLS.getIcon());
		addUserButton("",Images.EDIT_DELETE.getIcon());
		getUserButton(1).setToolTipText(Language.tr("UserTemplates.TemplatesDialog.Edit"));
		getUserButton(2).setToolTipText(Language.tr("UserTemplates.TemplatesDialog.Delete"));

		/* GUI aufbauen */

		final JPanel content=createGUI(()->Help.topicModal(this,"UserTemplates"));
		content.setLayout(new BorderLayout());

		JPanel line;
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("UserTemplates.TemplatesDialog.Info")));

		content.add(new JScrollPane(list=new JList<>(listModel)),BorderLayout.CENTER);
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {commandUse(); e.consume(); return;}
				if (SwingUtilities.isRightMouseButton(e)) {list.setSelectedIndex(list.locationToIndex(e.getPoint())); showPopup(e.getPoint()); e.consume(); return;}
			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_DELETE && !e.isControlDown() && !e.isAltDown()) {commandDelete(e.isShiftDown()); e.consume(); return;}
			}
		});
		list.setCellRenderer(new ListRecordRenderer());

		/* Start */

		setMinSizeRespectingScreensize(600,600);
		setSizeRespectingScreensize(600,600);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected JRootPane createRootPane() {
		JRootPane rootPane=super.createRootPane();
		InputMap inputMap=rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke stroke;

		stroke=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.SHIFT_DOWN_MASK);
		inputMap.put(stroke,"SHIFT+ENTER");
		rootPane.getActionMap().put("SHIFT+ENTER",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 7785168819536914745L;
			@Override public void actionPerformed(ActionEvent e) {commandUse();}

		});

		stroke=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.CTRL_DOWN_MASK);
		inputMap.put(stroke,"CTRL+ENTER");
		rootPane.getActionMap().put("CTRL+ENTER",new AbstractAction() {
			/**
			 * Serialisierungs-ID der Klasse
			 * @see Serializable
			 */
			private static final long serialVersionUID = 1999144141241921785L;
			@Override public void actionPerformed(ActionEvent e) {commandEdit();}
		});

		return rootPane;
	}

	@Override
	protected void storeData() {
		globalTemplates.clear();
		modelTemplates.clear();

		final Enumeration<ListRecord> enumerate=listModel.elements();
		while (enumerate.hasMoreElements()) {
			final ListRecord record=enumerate.nextElement();
			if (record.global) globalTemplates.add(record.template); else modelTemplates.add(record.template);
		}

		globalTemplates.saveGlobalTemplates();
	}

	/**
	 * Wurde eine Vorlage zum Einfügen in das Modell ausgewählt, so wird dieses hier in Zwischenablagen-tauglicher Form zurückgeliefert
	 * @return	Einzufügende Elemente in Zwischenablagen-tauglicher Form
	 */
	public ByteArrayInputStream getUseTemplate() {
		if (useTemplate==null) return null;
		return UserTemplateTools.getAllElements(useTemplate);
	}

	/**
	 * Befehl: Vorlage verwenden
	 */
	private void commandUse() {
		if (list.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("UserTemplates.TemplatesDialog.SelectErrorTitle"),Language.tr("UserTemplates.TemplatesDialog.SelectErrorInfo"));
			return;
		}

		useTemplate=listModel.get(list.getSelectedIndex()).template;
		close(BaseDialog.CLOSED_BY_OK);
	}

	/**
	 * Befehl: Vorlage bearbeiten
	 */
	private void commandEdit() {
		if (list.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("UserTemplates.TemplatesDialog.SelectErrorTitle"),Language.tr("UserTemplates.TemplatesDialog.SelectErrorInfo"));
			return;
		}

		final ListRecord record=listModel.get(list.getSelectedIndex());
		final UserTemplate template=record.template;
		final EditTemplateDialog dialog=new EditTemplateDialog(this,template,record.global);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			record.template=dialog.getTemplate();
			record.global=dialog.isGlobal();
			listModel.set(list.getSelectedIndex(),record);
		}
	}

	/**
	 * Befehl: Vorlage löschen
	 * @param isShiftDown	Ist die Umschalttaste gedrückt? (Wenn ja, löschen ohne Nachfrage.)
	 */
	private void commandDelete(final boolean isShiftDown) {
		if (list.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("UserTemplates.TemplatesDialog.SelectErrorTitle"),Language.tr("UserTemplates.TemplatesDialog.SelectErrorInfo"));
			return;
		}

		final UserTemplate template=listModel.get(list.getSelectedIndex()).template;
		if (!isShiftDown) {
			if (!MsgBox.confirm(this,Language.tr("UserTemplates.TemplatesDialog.Delete.ConfirmTitle"),String.format(Language.tr("UserTemplates.TemplatesDialog.Delete.ConfirmInfo"),template.getName()),Language.tr("UserTemplates.TemplatesDialog.Delete.ConfirmInfoYes"),Language.tr("UserTemplates.TemplatesDialog.Delete.ConfirmInfoNo"))) return;
		}

		listModel.remove(list.getSelectedIndex());
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0: commandUse(); break;
		case 1: commandEdit(); break;
		case 2: commandDelete(false); break;
		}
	}

	/**
	 * Zeigt das Popupmenü zu einem Eintrag in der Liste der Vorlagen an.
	 * @param point	Position an der das Popupmenü angezeigt werden soll
	 */
	private void showPopup(final Point point) {
		final boolean templateSelected=(list.getSelectedIndex()>=0);
		final JPopupMenu popup=new JPopupMenu();
		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("UserTemplates.TemplatesDialog.Use")));
		item.setIcon(Images.EDIT_PASTE.getIcon());
		item.setEnabled(templateSelected);
		item.addActionListener(e->commandUse());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.SHIFT_DOWN_MASK));

		popup.addSeparator();

		popup.add(item=new JMenuItem(Language.tr("UserTemplates.TemplatesDialog.Edit")));
		item.setIcon(Images.GENERAL_TOOLS.getIcon());
		item.setEnabled(templateSelected);
		item.addActionListener(e->commandEdit());
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,InputEvent.CTRL_DOWN_MASK));

		popup.add(item=new JMenuItem(Language.tr("UserTemplates.TemplatesDialog.Delete")));
		item.setIcon(Images.EDIT_DELETE.getIcon());
		item.setEnabled(templateSelected);
		item.addActionListener(e->commandDelete((e.getModifiers() & ActionEvent.SHIFT_MASK)!=0));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));

		popup.show(list,point.x,point.y);

	}

	/**
	 * Datensatz zu einer Vorlage
	 */
	private static class ListRecord {
		/** Vorlage */
		public UserTemplate template;
		/** Globale Vorlage (<code>true</code>) oder modellspezifisch (<code>false</code>)? */
		public boolean global;

		/**
		 * Konstruktor der Klasse
		 * @param template	Vorlage
		 * @param global	Globale Vorlage (<code>true</code>) oder modellspezifisch (<code>false</code>)?
		 */
		public ListRecord(final UserTemplate template, final boolean global) {
			this.template=template.clone();
			this.global=global;
		}
	}

	/**
	 * Renderer für die Einträge in der Vorlagenliste
	 * @see TemplatesListDialog#list
	 */
	private class ListRecordRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -1010390284185441204L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final Component result=super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			if (result instanceof JLabel && value instanceof ListRecord) {
				final ListRecord record=(ListRecord)value;
				final JLabel label=(JLabel)result;

				final StringBuilder sb=new StringBuilder();
				sb.append("<html><body>");
				sb.append("<b>");
				sb.append(record.template.getName());
				sb.append("</b> (");
				if (record.global) {
					sb.append(Language.tr("UserTemplates.TemplatesDialog.Mode.Global"));
				} else {
					sb.append(Language.tr("UserTemplates.TemplatesDialog.Mode.Model"));
				}
				sb.append(", ");
				final int[] info=record.template.getInfo();
				if (info[0]==1) {
					sb.append(Language.tr("UserTemplates.TemplatesDialog.ContentInfo.Elements.Singular"));
				} else {
					sb.append(String.format(Language.tr("UserTemplates.TemplatesDialog.ContentInfo.Elements"),info[0]));
				}
				sb.append(", ");
				if (info[1]==1) {
					sb.append(Language.tr("UserTemplates.TemplatesDialog.ContentInfo.Stations.Singular"));
				} else {
					sb.append(String.format(Language.tr("UserTemplates.TemplatesDialog.ContentInfo.Stations"),info[1]));
				}
				sb.append(")");
				sb.append("</body></html>");
				label.setText(sb.toString());

				label.setIcon(Images.MODEL_TEMPLATES.getIcon());
			}
			return result;
		}
	}
}
