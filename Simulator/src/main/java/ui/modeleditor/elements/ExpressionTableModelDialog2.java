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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import language.Language;
import systemtools.BaseDialog;
import systemtools.LabeledColorChooserButton;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog zum Bearbeiten einer Reihe f�r ein Animationsdiagramm
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

	/**
	 * Auswahl der Darstellungsart: Linie oder Punkte
	 */
	private final JComboBox<?> mode;

	/**
	 * Panel, welches die Punktgr��e- oder die Linienbreiteneinstellungen anzeigt
	 */
	private final JPanel modeSetupArea;

	/**
	 * Layout f�r {@link #modeSetupArea}
	 */
	private final CardLayout modeSetupAreaLayout;

	/**
	 * Auswahl der Punktgr��e
	 */
	private final JComboBox<?> pointSize;

	/**
	 * Auswahl der Linienbreite
	 */
	private final JComboBox<?> lineWidth;

	/**
	 * Auswahl der Farbe
	 */
	private final LabeledColorChooserButton colorChooser;

	/**
	 * Konstruktor der Klasse<br>
	 * Variante f�r Liniendiagramme
	 * @param owner	�bergeordnetes Element
	 * @param color	Bisherige Farbe
	 * @param width	Bisherige Linienbreite
	 * @param helpRunnable	Hilfe-Callback
	 * @see ExpressionTableModelLine
	 */
	public ExpressionTableModelDialog2(final Component owner, final Color color, final int width, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.ExpressionTableModel.Dialog"));

		JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());
		final JPanel lineArea=new JPanel();
		lineArea.setLayout(new BoxLayout(lineArea,BoxLayout.PAGE_AXIS));
		content.add(lineArea,BorderLayout.NORTH);

		Object[] data;
		JPanel line;

		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.ExpressionTableModel.Dialog.Mode")+":",new String[]{
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.Points"),
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.Line")
		});
		lineArea.add((JPanel)data[0]);
		mode=(JComboBox<?>)data[1];

		lineArea.add(modeSetupArea=new JPanel(modeSetupAreaLayout=new CardLayout()),BorderLayout.NORTH);

		data=ModelElementBaseDialog.getPointSizeInputPanel(Language.tr("Surface.ExpressionTableModel.Dialog.PointSize")+":",1,15,Math.abs(width));
		modeSetupArea.add((JPanel)data[0],"0");
		pointSize=(JComboBox<?>)data[1];

		data=ModelElementBaseDialog.getLineWidthInputPanel(Language.tr("Surface.ExpressionTableModel.Dialog.LineWidth")+":",1,15,Math.abs(width));
		modeSetupArea.add((JPanel)data[0],"1");
		lineWidth=(JComboBox<?>)data[1];

		mode.addActionListener(e->modeSetupAreaLayout.show(modeSetupArea,""+mode.getSelectedIndex()));
		mode.setSelectedIndex((width>0)?1:0);

		lineArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.ExpressionTableModel.Dialog.LineColor")+":",color));

		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Variante f�r Balkendiagramme
	 * @param owner	�bergeordnetes Element
	 * @param color	Bisherige Farbe
	 * @param helpRunnable	Hilfe-Callback
	 * @see ExpressionTableModelBar
	 */
	public ExpressionTableModelDialog2(final Component owner, final Color color, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.ExpressionTableModel.Dialog"));

		JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());

		mode=null;
		modeSetupArea=null;
		modeSetupAreaLayout=null;
		pointSize=null;
		lineWidth=null;

		JPanel sub=new JPanel(new BorderLayout()); content.add(sub,BorderLayout.CENTER);
		JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT)); sub.add(line,BorderLayout.NORTH);
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.ExpressionTableModel.Dialog.LineColor")+":",color));

		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Liefert die gew�hlte Farbe (Linien- und Balkendiagramme)
	 * @return	Neue Farbe
	 */
	public Color getColor() {
		return colorChooser.getColor();
	}

	/**
	 * Liefert die gew�hlte Linienbreite (nur f�r Liniendiagramme)
	 * @return	Neue Linienbreite
	 */
	public int getLineWidth() {
		if (lineWidth==null) return 1;
		switch (mode.getSelectedIndex()) {
		case 0: return -(pointSize.getSelectedIndex()+1);
		case 1: return lineWidth.getSelectedIndex()+1;
		default: return 1;
		}
	}
}