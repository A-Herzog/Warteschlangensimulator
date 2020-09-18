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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.ModelRestore;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelSurfacePanel;

/**
 * Dialog zur Auswahl eines Modells zur Wiederherstellung beim Start
 * @author Alexander Herzog
 * @see ModelRestore
 */
public class ModelRestoreDialog extends BaseDialog {
	private static final long serialVersionUID = -1294110190784151851L;

	/**
	 * Liste mit den Modelldateien, die zur Auswahl angeboten werden sollen
	 */
	private final File[] files;

	/**
	 * Darstellung der Modelldateien, die zur Auswahl angeboten werden sollen
	 */
	private final JList<JLabel> list;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param files	Liste mit den Modelldateien, die zur Auswahl angeboten werden sollen
	 */
	public ModelRestoreDialog(final Component owner, final File[] files) {
		super(owner,Language.tr("AutoRestore.Dialog.Title"));
		this.files=files;

		addUserButton(Language.tr("AutoRestore.Dialog.ClearAll"),Images.EDIT_DELETE.getURL());

		final JPanel content=createGUI(()->Help.topicModal(this,"AutoRestoreSelect"));
		content.setLayout(new BorderLayout());
		content.add(new JScrollPane(list=new JList<>(getListData().toArray(new JLabel[0]))),BorderLayout.CENTER);
		list.setCellRenderer(new JLabelRender());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) close(BaseDialog.CLOSED_BY_OK);
			}
		});

		setMinSizeRespectingScreensize(600,800);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	private String getModelDescription(final File file) {
		final DateFormat dateFormat=DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.MEDIUM);
		final StringBuilder info=new StringBuilder();
		info.append("<html><body>");
		info.append(Language.tr("AutoRestore.Dialog.InfoSaveDate")+":");
		info.append("<br>");
		info.append("<b>");
		info.append(dateFormat.format(file.lastModified()));
		info.append("</b>");
		info.append("</body></html>");
		return info.toString();
	}

	private static Image makeColorTransparent(BufferedImage im, final Color color) {
		final ImageFilter filter=new RGBImageFilter() {
			/* the color we are looking for... Alpha bits are set to opaque */
			public int markerRGB = color.getRGB() | 0xFF000000;
			@Override
			public final int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					return 0x00FFFFFF & rgb; /* Mark the alpha bits as zero - transparent */
				} else {
					return rgb; /* nothing to do */
				}
			}
		};
		final ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	private Image getModelImage(final File file) {
		final EditModel model=new EditModel();
		if (model.loadFromFile(file)!=null) return null;
		final ModelSurfacePanel surfacePanel=new ModelSurfacePanel();
		surfacePanel.setSurface(model,model.surface,model.clientData,model.sequences);
		return makeColorTransparent(surfacePanel.getImage(550,200),Color.WHITE);
	}

	private List<JLabel> getListData() {
		final List<JLabel> list=new ArrayList<>();
		for (File file: files) {
			final JLabel label=new JLabel(getModelDescription(file));
			final Image image=getModelImage(file);
			if (image!=null) label.setIcon(new ImageIcon(image));
			label.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			list.add(label);
		}
		return list;
	}

	@Override
	protected boolean checkData() {
		if (list.getSelectedIndex()<0) {
			MsgBox.error(this,Language.tr("AutoRestore.Dialog.ErrorTitle"),Language.tr("AutoRestore.Dialog.ErrorInfo"));
			return false;
		}
		return true;
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (!MsgBox.confirm(this,Language.tr("AutoRestore.Dialog.DeleteAllTitle"),Language.tr("AutoRestore.Dialog.DeleteAllInfo"),Language.tr("AutoRestore.Dialog.DeleteAllInfoYes"),Language.tr("AutoRestore.Dialog.DeleteAllInfoNo"))) return;
		ModelRestore.clearAll();
		close(BaseDialog.CLOSED_BY_CANCEL);
	}

	/**
	 * Liefert die Ausgewählte Datei
	 * @return	ausgewählte Datei
	 */
	public File getSelectedModelFile() {
		if (list.getSelectedIndex()<0) return null;
		return files[list.getSelectedIndex()];
	}

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
