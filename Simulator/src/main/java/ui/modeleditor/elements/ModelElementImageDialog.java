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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import systemtools.LabeledColorChooserButton;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.tools.ImageChooser;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementImage}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementImage
 */
public class ModelElementImageDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7824874736501593918L;

	/** Auswahlbox für die Linienbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Hintergrundfarbe */
	private LabeledColorChooserButton colorChooser;
	/** Auswahl des Bildes */
	private ImageChooser imageChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementImage}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementImageDialog(final Component owner, final ModelElementImage element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.Image.Dialog.Title"),element,"ModelElementImage",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationImage;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		JPanel line;

		final JPanel content=new JPanel(new BorderLayout());
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Rahmenbreite */
		final Object[] data=getLineWidthInputPanel(Language.tr("Surface.Image.Dialog.FrameWidth")+":",0,15,((ModelElementImage)element).getLineWidth());
		setup.add(line=(JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Rahmenfarbe */
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.Image.Dialog.FrameColor")+":",((ModelElementImage)element).getColor()));
		colorChooser.setEnabled(!readOnly);

		/* Bild */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.Image.Dialog.Image")+":"));
		content.add(imageChooser=new ImageChooser(((ModelElementImage)element).getImage(),element.getModel().animationImages),BorderLayout.CENTER);
		imageChooser.setEnabled(!readOnly);

		return content;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,500);
		pack();
		setResizable(true);
		setMaxSizeRespectingScreensize(1024,768);
	}

	/**
	 * Stellt die Größe des Dialogfensters unmittelbar vor dem Sicherbarmachen ein.
	 */
	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementImage image=(ModelElementImage)element;

		image.setLineWidth(lineWidth.getSelectedIndex());
		image.setColor(colorChooser.getColor());

		if (imageChooser.getImage()!=null) image.setImage(imageChooser.getImage());
	}
}
