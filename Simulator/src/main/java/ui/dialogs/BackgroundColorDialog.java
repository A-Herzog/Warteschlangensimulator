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
import java.awt.FlowLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import language.Language;
import systemtools.BaseDialog;
import systemtools.SmallColorChooser;
import ui.help.Help;
import ui.modeleditor.ModelSurface;

/**
 * Dialog, der das Einstellen von Hintergrund- un Rasterfarbe ermöglicht.
 * @author Alexander Herzog
 */
public class BackgroundColorDialog extends BaseDialog {
	private static final long serialVersionUID = 7680431280280416543L;

	private final JCheckBox backgroundCheck;
	private final SmallColorChooser backgroundColor;
	private final JCheckBox rasterCheck;
	private final SmallColorChooser rasterColor;
	private final JCheckBox gradientCheck;
	private final SmallColorChooser gradientColor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param colors	Bisherige Farben (2-elementiges Array aus Hintergrund- und Rasterfarbe)
	 * @param readOnly	Gibt an, ob die Einstellungen verändert werden dürfen
	 */
	public BackgroundColorDialog(final Component owner, final Color[] colors, final boolean readOnly) {
		super(owner,Language.tr("Window.BackgroundColor.Title"),readOnly);

		final JPanel content=createGUI(()->Help.topicModal(BackgroundColorDialog.this,"EditorColorDialog"));

		final Color c1=(colors!=null && colors.length>=2 && colors[0]!=null)?colors[0]:ModelSurface.DEFAULT_BACKGROUND_COLOR;
		final Color c2=(colors!=null && colors.length>=2 && colors[1]!=null)?colors[1]:ModelSurface.DEFAULT_RASTER_COLOR;
		final Color c3=(colors!=null && colors.length>=3 && colors[2]!=null)?colors[2]:null;

		JPanel line, cell;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		/* Hintergrundfarbe */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(backgroundCheck=new JCheckBox(Language.tr("Window.BackgroundColor.UserBackground"),!c1.equals(ModelSurface.DEFAULT_BACKGROUND_COLOR)),BorderLayout.NORTH);
		backgroundCheck.setEnabled(!readOnly);
		cell.add(backgroundColor=new SmallColorChooser(c1),BorderLayout.CENTER);
		backgroundColor.setEnabled(!readOnly);
		backgroundColor.addClickListener(e->backgroundCheck.setSelected(true));

		/* Rasterfarbe */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(rasterCheck=new JCheckBox(Language.tr("Window.BackgroundColor.UserRaster"),!c2.equals(ModelSurface.DEFAULT_RASTER_COLOR)),BorderLayout.NORTH);
		rasterCheck.setEnabled(!readOnly);
		cell.add(rasterColor=new SmallColorChooser(c2),BorderLayout.CENTER);
		rasterColor.setEnabled(!readOnly);
		rasterColor.addClickListener(e->rasterCheck.setSelected(true));

		/* Farbverlauf */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(gradientCheck=new JCheckBox(Language.tr("Window.BackgroundColor.UseGradient"),c3!=null),BorderLayout.NORTH);
		gradientCheck.setEnabled(!readOnly);
		cell.add(gradientColor=new SmallColorChooser(c3==null?Color.WHITE:c3),BorderLayout.CENTER);
		gradientColor.setEnabled(!readOnly);
		gradientColor.addClickListener(e->gradientCheck.setSelected(true));

		pack();
		setLocationRelativeTo(getOwner());
	}

	/**
	 * Liefert im Falle, dass der Dialog per "Ok" geschlossen wurde die neueingestellten Farben
	 * @return	3-elementiges Array aus Hintergrund-, Raster und gradienten Hintergrundfarbe (welche <code>null</code> sein kann) oder <code>null</code>, wenn der Dialog abgebrochen wurde.
	 */
	public Color[] getColors() {
		if (getClosedBy()!=CLOSED_BY_OK) return null;
		Color c1=ModelSurface.DEFAULT_BACKGROUND_COLOR;
		if (backgroundCheck.isSelected()) c1=backgroundColor.getColor();
		Color c2=ModelSurface.DEFAULT_RASTER_COLOR;
		if (rasterCheck.isSelected()) c2=rasterColor.getColor();
		Color c3=(gradientCheck.isSelected())?gradientColor.getColor():null;
		return new Color[] {c1,c2,c3};
	}
}
