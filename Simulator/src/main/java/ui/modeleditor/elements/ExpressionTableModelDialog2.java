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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import systemtools.BaseDialog;
import systemtools.SmallColorChooser;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zum Bearbeiten einer Reihe für ein Animationsdiagramm
 * (Farbe und Linienbreite)
 * @author Alexander Herzog
 * @see ExpressionTableModelBar
 * @see ExpressionTableModelLine
 * @see ExpressionTableModelDialog1
 */
public class ExpressionTableModelDialog2 extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6910980984317800965L;

	private final JComboBox<JLabel> lineWidth;
	private final SmallColorChooser colorChooser;

	/**
	 * Konstruktor der Klasse<br>
	 * Variante für Liniendiagramme
	 * @param owner	Übergeordnetes Element
	 * @param color	Bisherige Farbe
	 * @param width	Bisherige Linienbreite
	 * @param helpRunnable	Hilfe-Callback
	 * @see ExpressionTableModelLine
	 */
	@SuppressWarnings("unchecked")
	public ExpressionTableModelDialog2(final Component owner, final Color color, final int width, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.ExpressionTableModel.Dialog"));

		JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());

		final Object[] data=ModelElementBaseDialog.getLineWidthInputPanel(Language.tr("Surface.ExpressionTableModel.Dialog.LineWidth")+":",1,15,width);
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];

		JPanel sub=new JPanel(new BorderLayout()); content.add(sub,BorderLayout.CENTER);
		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT)); sub.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("Surface.ExpressionTableModel.Dialog.LineColor")+":"));
		sub.add(colorChooser=new SmallColorChooser(color),BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Variante für Balkendiagramme
	 * @param owner	Übergeordnetes Element
	 * @param color	Bisherige Farbe
	 * @param helpRunnable	Hilfe-Callback
	 * @see ExpressionTableModelBar
	 */
	public ExpressionTableModelDialog2(final Component owner, final Color color, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.ExpressionTableModel.Dialog"));

		JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());

		lineWidth=null;

		JPanel sub=new JPanel(new BorderLayout()); content.add(sub,BorderLayout.CENTER);
		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT)); sub.add(line,BorderLayout.NORTH);
		line.add(new JLabel(Language.tr("Surface.ExpressionTableModel.Dialog.LineColor")+":"));
		sub.add(colorChooser=new SmallColorChooser(color),BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Liefert die gewählte Farbe (Linien- und Balkendiagramme)
	 * @return	Neue Farbe
	 */
	public Color getColor() {
		return colorChooser.getColor();
	}

	/**
	 * Liefert die gewählte Linienbreite (nur für Liniendiagramme)
	 * @return	Neue Linienbreite
	 */
	public int getLineWidth() {
		if (lineWidth==null) return 1;
		return lineWidth.getSelectedIndex()+1;
	}
}