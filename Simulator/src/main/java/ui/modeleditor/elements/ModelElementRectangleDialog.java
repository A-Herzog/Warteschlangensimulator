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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementRectangle}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementRectangle
 */
public class ModelElementRectangleDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -632809253464205676L;

	/** Auswahlbox für die Breite der Linie des Rechtecks */
	private JComboBox<JLabel> lineWidth;
	/** Auswahlbox zur Wahl der Stärke der Abrundung der Ecken */
	private JComboBox<String> rounding;
	/** Eingabefeld für die Drehung */
	private JTextField rotation;
	/** Auswahl der Farbe der Linie des Rechtecks */
	private SmallColorChooser colorChooserLine;
	/** Option: Hintergrundfarbe verwenden? */
	private JCheckBox background;
	/** Auswahl der Hintergrundfarbe */
	private SmallColorChooser colorChooserBackground;
	/** Option: Farbverlauf verwenden */
	private JCheckBox gradient;
	/** Auswahl der Farbe für den Farbverlauf */
	private SmallColorChooser colorChooserGradient;
	/** Schieberegler zur Auswahl des Deckkraft der Hintergrundfarbe */
	private JSlider alpha;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementRectangle}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementRectangleDialog(final Component owner, final ModelElementRectangle element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.Rectangle.Dialog.Title"),element,"ModelElementRectangle",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationRectangle;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final ModelElementRectangle rectangle=(ModelElementRectangle)element;

		final JPanel content=new JPanel(new BorderLayout());

		JPanel line, cell;
		JLabel label;

		/* Rahmenbreite */
		final Object[] data=getLineWidthInputPanel(Language.tr("Surface.Rectangle.Dialog.FrameWidth")+":",0,15,rectangle.getLineWidth());
		content.add(line=(JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Ecken abrunden */
		line.add(label=new JLabel(Language.tr("Surface.Rectangle.Dialog.Rounding")+":"));
		final List<String> values=new ArrayList<>();
		for (int i=0;i<=10;i++) {
			String add="";
			if (i==0) add=" ("+Language.tr("Surface.Rectangle.Dialog.Rounding.Off")+")";
			if (i==10) add=" ("+Language.tr("Surface.Rectangle.Dialog.Rounding.Max")+")";
			values.add(NumberTools.formatPercent(i/10.0,0)+add);
		}
		line.add(rounding=new JComboBox<>(values.toArray(new String[0])));
		label.setLabelFor(rounding);
		rounding.setEnabled(!readOnly);
		rounding.setSelectedIndex((int)Math.round(rectangle.getRounding()*10));

		line.add(Box.createHorizontalStrut(10));

		/* Drehung */
		line.add(label=new JLabel(Language.tr("Surface.Rectangle.Dialog.Rotation")+":"));
		line.add(rotation=new JTextField(NumberTools.formatNumber(rectangle.getRotationAlpha()),3));
		label.setLabelFor(rotation);
		line.add(new JLabel("° (0°-90°)"));
		rotation.setEnabled(!readOnly);
		rotation.addActionListener(e->checkData(false));

		/* Zeile für Farben */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		/* Rahmenfarbe */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.Rectangle.Dialog.FrameColor")+":"),BorderLayout.NORTH);
		cell.add(colorChooserLine=new SmallColorChooser(rectangle.getColor()),BorderLayout.CENTER);
		colorChooserLine.setEnabled(!readOnly);
		label.setLabelFor(colorChooserLine);

		/* Hintergrundfarbe */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(background=new JCheckBox(Language.tr("Surface.Rectangle.Dialog.FillBackground")),BorderLayout.NORTH);
		background.setSelected(rectangle.getFillColor()!=null);
		background.setEnabled(!readOnly);
		cell.add(colorChooserBackground=new SmallColorChooser(rectangle.getFillColor()),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		/* Farbverlauf */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(gradient=new JCheckBox(Language.tr("Surface.Rectangle.Dialog.BackgroundGradient")),BorderLayout.NORTH);
		gradient.setSelected(rectangle.getGradientFillColor()!=null);
		gradient.setEnabled(!readOnly);
		gradient.addActionListener(e->{if (gradient.isSelected()) background.setSelected(true);});
		cell.add(colorChooserGradient=new SmallColorChooser(rectangle.getGradientFillColor()),BorderLayout.CENTER);
		colorChooserGradient.setEnabled(!readOnly);
		colorChooserGradient.addClickListener(e->{background.setSelected(true); gradient.setSelected(true);});

		/* Deckkraft */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		JLabel alphaLabel=new JLabel(Language.tr("Surface.Rectangle.Dialog.Alpha")+":");
		line.add(alphaLabel);
		line.add(alpha=new JSlider(0,100,(int)Math.round(100*rectangle.getFillAlpha())));
		alphaLabel.setLabelFor(alpha);
		alpha.setEnabled(!readOnly);
		alpha.setMinorTickSpacing(1);
		alpha.setMajorTickSpacing(10);
		Hashtable<Integer,JComponent> labels=new Hashtable<>();
		for (int i=0;i<=10;i++) labels.put(i*10,new JLabel(NumberTools.formatPercent(i/10.0)));
		alpha.setLabelTable(labels);
		alpha.setPaintTicks(true);
		alpha.setPaintLabels(true);
		alpha.setPreferredSize(new Dimension(400,alpha.getPreferredSize().height));
		alpha.addChangeListener(e->background.setSelected(true));

		label.setPreferredSize(new Dimension(label.getPreferredSize().width,gradient.getPreferredSize().height));

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		final Double D=NumberTools.getNotNegativeDouble(rotation,true);
		if (D==null) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Rectangle.Dialog.Rotation"),Language.tr("Surface.Rectangle.Dialog.Rotation.ErrorInfo"));
				return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if (element instanceof ModelElementRectangle) {
			final ModelElementRectangle rectangle=(ModelElementRectangle)element;

			rectangle.setLineWidth(lineWidth.getSelectedIndex());
			rectangle.setColor(colorChooserLine.getColor());

			if (background.isSelected()) {
				rectangle.setFillColor(colorChooserBackground.getColor());
			} else {
				rectangle.setFillColor(null);
			}

			if (gradient.isSelected()) {
				rectangle.setGradientFillColor(colorChooserGradient.getColor());
			} else {
				rectangle.setGradientFillColor(null);
			}

			rectangle.setFillAlpha(alpha.getValue()/100.0);

			rectangle.setRounding(rounding.getSelectedIndex()/10.0);
			rectangle.setRotationAlpha(NumberTools.getNotNegativeDouble(rotation,true));
		}
	}
}