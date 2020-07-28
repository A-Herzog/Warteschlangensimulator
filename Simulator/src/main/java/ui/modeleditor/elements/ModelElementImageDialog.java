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

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.tools.ImageChooser;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementImage}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementImage
 */
public class ModelElementImageDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 7824874736501593918L;

	private JComboBox<JLabel> lineWidth;
	private SmallColorChooser colorChooser;
	private ImageChooser imageChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementImage}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementImageDialog(final Component owner, final ModelElementImage element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Image.Dialog.Title"),element,"ModelElementImage",readOnly);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationImage;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout(5,5));

		JPanel line,sub;

		/* Linke Spalte */
		final JPanel left=new JPanel(new BorderLayout());
		content.add(left,BorderLayout.WEST);

		/* Links: Rahmenbreite */
		final Object[] data=getLineWidthInputPanel(Language.tr("Surface.Image.Dialog.FrameWidth")+":",0,15,((ModelElementImage)element).getLineWidth());
		left.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Links: Rahmenfarbe */
		left.add(sub=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("Surface.Image.Dialog.FrameColor")+":"));
		sub.add(line=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		line.add(colorChooser=new SmallColorChooser(((ModelElementImage)element).getColor()),BorderLayout.NORTH);
		colorChooser.setEnabled(!readOnly);

		/* Center */
		final JPanel center=new JPanel(new BorderLayout());
		content.add(center,BorderLayout.CENTER);
		center.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("Surface.Image.Dialog.Image")+":"));
		center.add(imageChooser=new ImageChooser(((ModelElementImage)element).getImage(),element.getModel().animationImages),BorderLayout.CENTER);
		imageChooser.setEnabled(!readOnly);

		return content;
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,500);
		pack();
		setResizable(true);
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
