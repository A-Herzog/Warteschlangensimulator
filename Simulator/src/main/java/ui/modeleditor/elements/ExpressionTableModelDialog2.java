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
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import language.Language;
import systemtools.BaseDialog;
import systemtools.LabeledColorChooserButton;
import tools.IconListCellRenderer;
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

	/**
	 * Auswahl der Darstellungsart
	 */
	private final JComboBox<?> mode;

	/**
	 * Panel, welches die Punktgröße- oder die Linienbreiteneinstellungen anzeigt
	 */
	private final JPanel modeSetupArea;

	/**
	 * Layout für {@link #modeSetupArea}
	 */
	private final CardLayout modeSetupAreaLayout;

	/**
	 * Auswahl der Punktgröße
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
	 * Variante für Liniendiagramme
	 * @param owner	Übergeordnetes Element
	 * @param color	Bisherige Farbe
	 * @param width	Bisherige Linienbreite
	 * @param lineMode Bisheriger Linienmodus
	 * @param helpRunnable	Hilfe-Callback
	 * @see ExpressionTableModelLine
	 */
	public ExpressionTableModelDialog2(final Component owner, final Color color, final int width, final ModelElementAnimationLineDiagram.LineMode lineMode, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.ExpressionTableModel.Dialog"));

		JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());
		final JPanel lineArea=new JPanel();
		lineArea.setLayout(new BoxLayout(lineArea,BoxLayout.PAGE_AXIS));
		content.add(lineArea,BorderLayout.NORTH);

		Object[] data;
		JPanel line;

		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.ExpressionTableModel.Dialog.Mode")+":",new String[]{
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.Line"),
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.Points"),
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashedShort"),
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashedMedium"),
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashedLong"),
				Language.tr("Surface.ExpressionTableModel.Dialog.Mode.DashDotted"),
		});
		lineArea.add((JPanel)data[0]);
		mode=(JComboBox<?>)data[1];
		mode.setRenderer(new IconListCellRenderer(new BufferedImage[]{
				ComplexLine.getExample(0),
				ComplexLine.getExample(4),
				ComplexLine.getExample(1),
				ComplexLine.getExample(2),
				ComplexLine.getExample(3),
				ComplexLine.getExample(6)
		}));

		lineArea.add(modeSetupArea=new JPanel(modeSetupAreaLayout=new CardLayout()),BorderLayout.NORTH);

		data=ModelElementBaseDialog.getPointSizeInputPanel(Language.tr("Surface.ExpressionTableModel.Dialog.PointSize")+":",1,15,Math.abs(width));
		modeSetupArea.add((JPanel)data[0],"0");
		pointSize=(JComboBox<?>)data[1];

		data=ModelElementBaseDialog.getLineWidthInputPanel(Language.tr("Surface.ExpressionTableModel.Dialog.LineWidth")+":",1,15,Math.abs(width));
		modeSetupArea.add((JPanel)data[0],"1");
		lineWidth=(JComboBox<?>)data[1];

		mode.addActionListener(e->{
			final String card=(mode.getSelectedIndex()==1)?"0":"1";
			modeSetupAreaLayout.show(modeSetupArea,card);
		});
		switch (lineMode) {
		case LINE: mode.setSelectedIndex(0); break;
		case POINTS: mode.setSelectedIndex(1); break;
		case DASHED_SHORT: mode.setSelectedIndex(2); break;
		case DASHED_MEDIUM: mode.setSelectedIndex(3); break;
		case DASHED_LONG: mode.setSelectedIndex(4); break;
		case POINT_DASH: mode.setSelectedIndex(5); break;
		default: mode.setSelectedIndex(0); break;
		}

		lineArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorChooser=new LabeledColorChooserButton(Language.tr("Surface.ExpressionTableModel.Dialog.LineColor")+":",color));

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
		return ((mode.getSelectedIndex()==1)?pointSize.getSelectedIndex():lineWidth.getSelectedIndex())+1;
	}

	/**
	 * Liefert den gewählten Linienmodus (nur für Liniendiagramme)
	 * @return	Neuer Linienmodus
	 */
	public ModelElementAnimationLineDiagram.LineMode getLineMode() {
		if (mode==null) return ModelElementAnimationLineDiagram.LineMode.LINE;
		switch (mode.getSelectedIndex()) {
		case 0: return ModelElementAnimationLineDiagram.LineMode.LINE;
		case 1: return ModelElementAnimationLineDiagram.LineMode.POINTS;
		case 2: return ModelElementAnimationLineDiagram.LineMode.DASHED_SHORT;
		case 3: return ModelElementAnimationLineDiagram.LineMode.DASHED_MEDIUM;
		case 4: return ModelElementAnimationLineDiagram.LineMode.DASHED_LONG;
		case 5: return ModelElementAnimationLineDiagram.LineMode.POINT_DASH;
		default: return ModelElementAnimationLineDiagram.LineMode.LINE;
		}
	}
}