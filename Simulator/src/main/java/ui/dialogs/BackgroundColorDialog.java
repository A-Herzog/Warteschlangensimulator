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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.OptionalColorChooserButton;
import tools.IconListCellRenderer;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.tools.ImageChooser;

/**
 * Dialog, der das Einstellen von Hintergrund- und Rasterfarbe ermöglicht.
 * @author Alexander Herzog
 */
public class BackgroundColorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7680431280280416543L;

	/** Nutzerdefinierte Hintergrundfarbe auswählen */
	private final OptionalColorChooserButton backgroundColor;
	/** Nutzerdefinierte Rasterfarbe auswählen */
	private final OptionalColorChooserButton rasterColor;
	/** Nutzerdefinierte Gradientenfarbe auswählen */
	private final OptionalColorChooserButton gradientColor;

	/** Hintergrundbild */
	private final ImageChooser backgroundImage;
	/** Skalierung für Hintergrundbild */
	private final JTextField backgroundImageScale;
	/** Hintergrundbild auch in Untermodellen anzeigen? */
	private final JCheckBox optionImageInSubModels;
	/** Reihenfolge von Raster und Hintergrundbild */
	private final JComboBox<String> orderComboBox;
	/** Schaltfläche zum Entfernen des Hintergrundbildes */
	private final JButton removeButton;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param colors	Bisherige Farben (2-elementiges Array aus Hintergrund- und Rasterfarbe)
	 * @param image	Hintergrundbild (kann <code>null</code> sein)
	 * @param scale	Skalierung für das Hintergrundbild (muss größer als 0 sein)
	 * @param useImageInSubModels	Hintergrundbild auch in Untermodellen anzeigen?
	 * @param mode	Reihenfolge von Raster und Hintergrundbild
	 * @param readOnly	Gibt an, ob die Einstellungen verändert werden dürfen
	 */
	public BackgroundColorDialog(final Component owner, final Color[] colors, final BufferedImage image, final double scale, final boolean useImageInSubModels, final ModelSurface.BackgroundImageMode mode, final boolean readOnly) {
		super(owner,Language.tr("Window.BackgroundColor.Title"),readOnly);

		final JPanel content=createGUI(()->Help.topicModal(BackgroundColorDialog.this,"EditorColorDialog"));
		content.setLayout(new BorderLayout());
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tabOuter;
		JPanel tab;
		JPanel line;
		JLabel label;

		/* Tab "Farben" */
		tabs.addTab(Language.tr("Window.BackgroundColor.Tab.Color"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));

		final Color c1=(colors!=null && colors.length>=2 && !ModelSurface.DEFAULT_BACKGROUND_COLOR.equals(colors[0]))?colors[0]:null;
		final Color c2=(colors!=null && colors.length>=2 && !ModelSurface.DEFAULT_RASTER_COLOR.equals(colors[1]))?colors[1]:null;
		final Color c3=(colors!=null && colors.length>=3)?colors[2]:null;

		/* Hintergrundfarbe */
		tab.add(backgroundColor=new OptionalColorChooserButton(Language.tr("Window.BackgroundColor.UserBackground")+":",c1,ModelSurface.DEFAULT_BACKGROUND_COLOR));
		backgroundColor.setEnabled(!readOnly);

		/* Rasterfarbe */
		tab.add(rasterColor=new OptionalColorChooserButton(Language.tr("Window.BackgroundColor.UserRaster")+":",c2,ModelSurface.DEFAULT_RASTER_COLOR));
		rasterColor.setEnabled(!readOnly);

		/* Farbverlauf */
		tab.add(gradientColor=new OptionalColorChooserButton(Language.tr("Window.BackgroundColor.UseGradient")+":",c3,Color.WHITE));
		gradientColor.setEnabled(!readOnly);

		/* Tab "Hintergrundbild" */
		tabs.addTab(Language.tr("Window.BackgroundColor.Tab.Image"),tabOuter=new JPanel(new BorderLayout()));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		tab.add(backgroundImage=new ImageChooser(image,null));
		backgroundImage.setPreferredSize(new Dimension(500,350));
		backgroundImage.setEnabled(!readOnly);

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Window.BackgroundColor.ImageScale")+":",NumberTools.formatNumberMax(scale),7);
		tab.add((JPanel)data[0]);
		backgroundImageScale=(JTextField)data[1];
		backgroundImageScale.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		backgroundImageScale.setEnabled(!readOnly);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionImageInSubModels=new JCheckBox(Language.tr("Window.BackgroundColor.ImageInSubModels"),useImageInSubModels));
		optionImageInSubModels.setEnabled(!readOnly);

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Window.BackgroundColor.ImageRasterOrder")+":"));
		line.add(orderComboBox=new JComboBox<>(new String[] {
				Language.tr("Window.BackgroundColor.ImageRasterOrder.RasterInFrontOfImage"),
				Language.tr("Window.BackgroundColor.ImageRasterOrder.ImageInFrontOfRaster")
		}));
		orderComboBox.setEnabled(!readOnly);
		label.setLabelFor(orderComboBox);
		switch (mode) {
		case BEHIND_RASTER: orderComboBox.setSelectedIndex(0); break;
		case IN_FRONT_OF_RASTER: orderComboBox.setSelectedIndex(1); break;
		default: orderComboBox.setSelectedIndex(0); break;
		}
		orderComboBox.setRenderer(new IconListCellRenderer(new Images[]{
				Images.EDIT_VIEW_RASTER,
				Images.EDIT_BACKGROUND_IMAGE
		}));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(removeButton=new JButton(Language.tr("Window.BackgroundColor.RemoveImage"),Images.EDIT_DELETE.getIcon()));
		removeButton.setEnabled(!readOnly && image!=null);
		removeButton.addActionListener(e->{
			backgroundImage.setImage(null);
			removeButton.setEnabled(true);
		});

		/* Icons auf Tabs */
		tabs.setIconAt(0,Images.EDIT_BACKGROUND_COLOR.getIcon());
		tabs.setIconAt(1,Images.EDIT_BACKGROUND_IMAGE.getIcon());

		/* Hintergrundbild mit Entfernen-Schaltfläche verbinden */
		backgroundImage.addChangeListener(e->removeButton.setEnabled(!readOnly && backgroundImage.getImage()!=null));

		/* Dialog starten */
		pack();
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final Double D=NumberTools.getPositiveDouble(backgroundImageScale,true);
		if (D==null) {
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Window.BackgroundColor.ImageScale.ErrorTitle"),String.format(Language.tr("Window.BackgroundColor.ImageScale.ErrorInfo"),backgroundImageScale.getText()));
				return false;
			}
			ok=false;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wurde die neueingestellten Farben
	 * @return	3-elementiges Array aus Hintergrund-, Raster und gradienten Hintergrundfarbe (welche <code>null</code> sein kann) oder <code>null</code>, wenn der Dialog abgebrochen wurde.
	 */
	public Color[] getColors() {
		if (getClosedBy()!=CLOSED_BY_OK) return null;

		Color c1=backgroundColor.getColor();
		if (c1==null) c1=ModelSurface.DEFAULT_BACKGROUND_COLOR;

		Color c2=rasterColor.getColor();
		if (c2==null) c2=ModelSurface.DEFAULT_RASTER_COLOR;

		final Color c3=gradientColor.getColor();

		return new Color[] {c1,c2,c3};
	}

	/**
	 * Liefert das eingestellte Hintergrundbild.
	 * @return	Hintergrundbild (kann <code>null</code> sein)
	 */
	public BufferedImage getImage() {
		if (getClosedBy()!=CLOSED_BY_OK) return null;
		return backgroundImage.getImage();
	}

	/**
	 * Liefert den Skalierungsfaktor für das Hintergrundbild.
	 * @return	Skalierungsfaktor für das Hintergrundbild
	 */
	public double getScale() {
		return NumberTools.getPositiveDouble(backgroundImageScale,true);
	}

	/**
	 * Ist die Option "Hintergrundbild auch in Untermodellen anzeigen" ausgewählt?
	 * @return	Option "Hintergrundbild auch in Untermodellen anzeigen" ausgewählt
	 */
	public boolean isImageInSubModels() {
		return optionImageInSubModels.isSelected();
	}

	/**
	 * Liefert die Reihenfolge von Raster und Hintergrundbild.
	 * @return	Reihenfolge von Raster und Hintergrundbild
	 */
	public ModelSurface.BackgroundImageMode getMode() {
		if (orderComboBox.getSelectedIndex()==0) return ModelSurface.BackgroundImageMode.BEHIND_RASTER; else return ModelSurface.BackgroundImageMode.IN_FRONT_OF_RASTER;
	}
}
