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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import language.Language;
import systemtools.BaseDialog;
import systemtools.SmallColorChooser;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.tools.ImageChooser;

/**
 * Dialog, der die Auswahl einer Hintergrundfarbe für ein {@link ModelElementBox}-Element ermöglicht.<br>
 * Dieser Dialog wird von {@link ModelElementBaseDialog} verwendet, wenn dieser mit einem Objekt, dessen Typ
 * sich von {@link ModelElementBox} ableitet, instanziert wird.
 * @author Alexander Herzog
 * @see ModelElementBaseDialog
 */
public class ModelElementBaseColorDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7425813595312162449L;

	/** Auswahloption "Standardfarbe verwenden" */
	private final JRadioButton optionDefaultColor;
	/** Auswahloption "Benutzerdefinierte Farbe verwenden" */
	private final JRadioButton optionUserColor;
	/** Auswahloption "Benutzerdefiniertes Bild verwenden" */
	private final JRadioButton optionUserImage;
	/** Auswahl der benutzerdefinierten Farbe */
	private final SmallColorChooser colorChooser;
	/** Station gespiegelt zeichnen */
	private final JCheckBox checkboxFlipped;
	/** Auswahl des benutzerdefinierten Bildes */
	private final ImageChooser imageChooser;

	/**
	 * Konstruktor der Klasse {@link ModelElementBaseColorDialog}
	 * @param owner	Übergeordnetes Element
	 * @param help	Runnable, das aufgerufen wird, wenn der Nutzer auf die Hilfe-Schaltfläche klickt
	 * @param defaultColor	Vorgabefarbe für das Element (zur Anzeige neben dem Vorgabe-Radiobutton)
	 * @param userColor	Bisher gewählte benutzerdefinierte Farbe oder <code>null</code>, wenn die Farbe automatisch festgelegt werden soll
	 * @param userImage	Bisher gewähltes benutzerdefiniertes Stationsbild oder <code>null</code>, wenn die Standardform verwendet werden soll
	 * @param flipable	Kann die Form gespiegelt werden?
	 * @param flipped	Wird die Form momentan gespiegelt gezeichnet?
	 * @param modelImages	Objekt, welches die verfügbaren allgemeinen Modell-Animationsbilder enthält
	 */
	public ModelElementBaseColorDialog(final Component owner, final Runnable help, final Color defaultColor, final Color userColor, final BufferedImage userImage, final boolean flipable, final boolean flipped, final ModelAnimationImages modelImages) {
		super(owner,Language.tr("Editor.ColorChooser.Title"));
		final JPanel contentOuter=createGUI(help);
		contentOuter.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		contentOuter.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line, defaultColorPreview;

		/* Vorgabefarbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionDefaultColor=new JRadioButton(Language.tr("Editor.ColorChooser.Default")+":"));
		line.add(defaultColorPreview=new JPanel());
		defaultColorPreview.setBackground(defaultColor);
		defaultColorPreview.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		defaultColorPreview.setPreferredSize(new Dimension(20,20));
		defaultColorPreview.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {optionDefaultColor.setSelected(true);}
		});

		/* Benutzerdefinierte Farbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionUserColor=new JRadioButton(Language.tr("Editor.ColorChooser.UserDefined")));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new SmallColorChooser(userColor));
		colorChooser.addClickListener(e->optionUserColor.setSelected(true));

		/* Spiegeln */
		if (flipable) {
			content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(checkboxFlipped=new JCheckBox(Language.tr("Editor.ColorChooser.Flipped"),flipped));
		} else {
			checkboxFlipped=null;
		}

		/* Benutzerdefiniertes Bild */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(optionUserImage=new JRadioButton(Language.tr("Editor.ColorChooser.UserDefinedImage")));

		contentOuter.add(imageChooser=new ImageChooser(userImage,modelImages),BorderLayout.CENTER);
		imageChooser.addChangeListener(e->optionUserImage.setSelected(true));

		/* Radiobuttons verbinden */
		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(optionDefaultColor);
		buttonGroup.add(optionUserColor);
		buttonGroup.add(optionUserImage);

		optionDefaultColor.setSelected(userColor==null && userImage==null);
		optionUserColor.setSelected(userColor!=null && userImage==null);
		optionUserImage.setSelected(userImage!=null);

		/* Start */
		setMinSizeRespectingScreensize(0,800);
		pack();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Konstruktor der Klasse <code>ModelElementBaseColorDialog</code>
	 * @param owner	Übergeordnetes Element
	 * @param help	Runnable, das aufgerufen wird, wenn der Nutzer auf die Hilfe-Schaltfläche klickt
	 * @param color	Farbe
	 */
	public ModelElementBaseColorDialog(final Component owner, final Runnable help, final Color color) {
		super(owner,Language.tr("Editor.ColorChooser.Title"));
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JPanel sub;

		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		optionDefaultColor=null;
		optionUserColor=null;
		optionUserImage=null;
		checkboxFlipped=null;
		imageChooser=null;

		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);
		sub.add(colorChooser=new SmallColorChooser(color));

		pack();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Liefert die eingestellte Farbe für das Element
	 * @return	Eingestellte benutzerdefinierte Farbe oder <code>null</code>, wenn die Vorgabefarbe verwendet werden soll.
	 */
	public Color getUserColor() {
		if (optionDefaultColor==null) return colorChooser.getColor();
		if (optionUserColor!=null && optionUserColor.isSelected()) return colorChooser.getColor();
		return null;
	}

	/**
	 * Liefert das eingestellt benutzerdefinerte Stationsbild
	 * @return	Benutzerdefinertes Stationsbild oder <code>null</code>, wenn die Vorgabeform verwendet werden soll.
	 */
	public BufferedImage getUserImage() {
		if (optionUserImage!=null && optionUserImage.isSelected()) return imageChooser.getImage();
		return null;
	}

	/**
	 * Soll die Form gespiegelt gezeichnet werden?
	 * @return	Liefert <code>true</code>, wenn die Form gespiegelt gezeichnet werden soll
	 */
	public boolean getFlipped() {
		return (checkboxFlipped!=null) && checkboxFlipped.isSelected();
	}
}