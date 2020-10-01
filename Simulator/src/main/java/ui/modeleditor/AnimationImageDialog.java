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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.help.Help;
import ui.images.Images;

/**
 * Dialog zur Konfiguration von benutzerdefinierten Animationsicons
 * @author Alexander Herzog
 * @see EditModel
 * @see ModelAnimationImages
 */
public class AnimationImageDialog extends BaseDialog {
	private static final long serialVersionUID = 9216923146541994445L;

	private final EditModel model;
	private final ModelAnimationImages animationImages;

	private final Runnable help;

	private final JButton add;
	private final JButton edit;
	private final JButton delete;

	private final DefaultListModel<JLabel> listModel;
	private final JList<JLabel> list;

	/**
	 * Konstruktor der Klasse<br>
	 * Der Dialog wird erstellt aber noch nicht angezeigt.
	 * @param owner	Übergeordnetes Element
	 * @param model	Modell aus dem die Icons ausgelesen und auch wieder zurückgeschrieben werden
	 * @param readOnly	Nur-Lese-Status
	 */
	public AnimationImageDialog(final Component owner, final EditModel model, final boolean readOnly) {
		super(owner,Language.tr("Animation.IconDialog.Title"),readOnly);

		/* Daten vorbereiten */
		this.model=model;
		animationImages=model.animationImages.clone();

		/* GUI vorbereiten */
		help=()->Help.topicModal(AnimationImageDialog.this,"EditorAnimationUserIcons");
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		content.add(toolbar,BorderLayout.NORTH);
		toolbar.setFloatable(false);

		add=new JButton(Language.tr("Animation.IconDialog.Add"));
		add.setToolTipText(Language.tr("Animation.IconDialog.Add.Hint"));
		add.addActionListener(e->commandAdd(null));
		add.setIcon(Images.EDIT_ADD.getIcon());
		add.setEnabled(!readOnly);
		toolbar.add(add);

		edit=new JButton(Language.tr("Animation.IconDialog.Edit"));
		edit.setToolTipText(Language.tr("Animation.IconDialog.Edit.Hint"));
		edit.addActionListener(e->commandEdit());
		edit.setIcon(Images.GENERAL_SETUP.getIcon());
		edit.setEnabled(false);
		toolbar.add(edit);

		delete=new JButton(Language.tr("Animation.IconDialog.Delete"));
		delete.setToolTipText(Language.tr("Animation.IconDialog.Delete.Hint"));
		delete.addActionListener(e->commandDelete());
		delete.setIcon(Images.EDIT_DELETE.getIcon());
		delete.setEnabled(false);
		toolbar.add(delete);

		/* Liste */
		this.listModel=new DefaultListModel<>();
		list=new JList<>(this.listModel);
		list.setCellRenderer(new ImageRenderer());
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_INSERT) {commandAdd(null); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {commandEdit(); e.consume(); return;}
				if (e.getKeyCode()==KeyEvent.VK_DELETE) {commandDelete(); e.consume(); return;}
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {commandEdit(); e.consume(); return;}
				if (SwingUtilities.isRightMouseButton(e) && e.getClickCount()==1) {commandContextMenu(e); e.consume(); return;}
			}
		});
		list.addListSelectionListener(e->{
			edit.setEnabled(!readOnly && list.getSelectedIndex()>=0);
			delete.setEnabled(!readOnly && list.getSelectedIndex()>=0);
		});
		updateList();
		content.add(new JScrollPane(list),BorderLayout.CENTER);

		/* Filedropper */
		new FileDropper(new Component[] {content,toolbar,list},e->fileDropped((FileDropperData)e.getSource()));

		/* Dialoggröße einstellen */
		setMinSizeRespectingScreensize(600,500);
		setSizeRespectingScreensize(600,500);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	private void updateList() {
		listModel.clear();

		for (String name: animationImages.getLocalNames()) {
			JLabel label=new JLabel(name);
			final BufferedImage image=animationImages.getLocalSize(name,32);
			if (image!=null) label.setIcon(new ImageIcon(image));
			listModel.addElement(label);
		}
	}

	private void commandAdd(final BufferedImage image) {
		if (readOnly) return;

		final List<String> inUse=new ArrayList<>();
		inUse.addAll(Arrays.asList(animationImages.getGlobalNames()));
		inUse.addAll(Arrays.asList(animationImages.getLocalNames()));

		final AnimationSingleImageDialog dialog=new AnimationSingleImageDialog(this,null,image,inUse.toArray(new String[0]),help);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			animationImages.set(dialog.getImageName(),dialog.getImage());
			updateList();
			list.setSelectedIndex(animationImages.size()-1);
		}
	}

	private void commandEdit() {
		if (readOnly) return;
		if (list.getSelectedIndex()<0) return;

		final List<String> inUse=new ArrayList<>();
		inUse.addAll(Arrays.asList(animationImages.getGlobalNames()));
		inUse.addAll(Arrays.asList(animationImages.getLocalNames()));

		int index=list.getSelectedIndex();
		final String name=animationImages.getLocalNames()[index];
		final AnimationSingleImageDialog dialog=new AnimationSingleImageDialog(this,name,animationImages.getLocal(name),inUse.toArray(new String[0]),help);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (dialog.getImageName().equals(name)) {
				animationImages.set(name,dialog.getImage());
			} else {
				animationImages.remove(name);
				animationImages.set(dialog.getImageName(),dialog.getImage());
				index=animationImages.size()-1;
			}
			updateList();
			list.setSelectedIndex(index);
		}
	}

	private void commandDelete() {
		if (readOnly) return;
		if (list.getSelectedIndex()<0) return;

		animationImages.remove(list.getSelectedIndex());
		updateList();
	}

	private void commandContextMenu(final MouseEvent event) {
		final JPopupMenu menu=new JPopupMenu();

		for (JButton button: new JButton[] {add,edit,delete}) {
			final JMenuItem item=new JMenuItem(button.getText());
			if (button.getIcon()!=null) item.setIcon(button.getIcon());
			item.setEnabled(button.isEnabled());
			item.addActionListener(e->{
				for (ActionListener listener: button.getActionListeners()) {
					listener.actionPerformed(e);
				}
			});
			menu.add(item);
		}

		menu.show(event.getComponent(),event.getX(),event.getY());
	}

	private boolean fileDropped(final FileDropperData data) {
		try {
			final BufferedImage image=ImageIO.read(data.getFile());
			if (image==null) return false;
			data.dragDropConsumed();
			SwingUtilities.invokeLater(()->commandAdd(image));
		} catch (IOException e) {return false;}
		return true;
	}

	@Override
	protected void storeData() {
		model.animationImages.setDataFrom(animationImages);
	}

	private class ImageRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 8633392935026934066L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				setText(((JLabel)value).getText());
				setIcon(((JLabel)value).getIcon());
			}
			return this;
		}
	}
}
