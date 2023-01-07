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
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import language.Language;
import mathtools.NumberTools;
import systemtools.SmallColorChooser;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementEllipse}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementEllipse
 */
public class ModelElementEllipseDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -632809253464205676L;

	/** Auswahlbox für die Breite der Linie der Ellipse */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Farbe der Linie der Ellipse */
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
	 * @param element	Zu bearbeitendes {@link ModelElementEllipse}
	 * @param readOnly	Nur-Lese-Status
	 */
	public ModelElementEllipseDialog(final Component owner, final ModelElementEllipse element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.Ellipse.Dialog.Title"),element,"ModelElementEllipse",readOnly);
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
		return InfoPanel.stationEllipse;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		JPanel line, cell;
		JLabel label;

		/* Rahmenbreite */
		final Object[] data=getLineWidthInputPanel(Language.tr("Surface.Ellipse.Dialog.FrameWidth")+":",0,15,((ModelElementEllipse)element).getLineWidth());
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Zeile für Farben */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		/* Rahmenfarbe */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.Ellipse.Dialog.FrameColor")+":"),BorderLayout.NORTH);
		cell.add(colorChooserLine=new SmallColorChooser(((ModelElementEllipse)element).getColor()),BorderLayout.CENTER);
		colorChooserLine.setEnabled(!readOnly);
		label.setLabelFor(colorChooserLine);

		/* Hintergrundfarbe */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(background=new JCheckBox(Language.tr("Surface.Ellipse.Dialog.FillBackground")),BorderLayout.NORTH);
		background.setSelected(((ModelElementEllipse)element).getFillColor()!=null);
		background.setEnabled(!readOnly);
		cell.add(colorChooserBackground=new SmallColorChooser(((ModelElementEllipse)element).getFillColor()),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		/* Farbverlauf */
		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(gradient=new JCheckBox(Language.tr("Surface.Ellipse.Dialog.BackgroundGradient")),BorderLayout.NORTH);
		gradient.setSelected(((ModelElementEllipse)element).getGradientFillColor()!=null);
		gradient.setEnabled(!readOnly);
		gradient.addActionListener(e->{if (gradient.isSelected()) background.setSelected(true);});
		cell.add(colorChooserGradient=new SmallColorChooser(((ModelElementEllipse)element).getGradientFillColor()),BorderLayout.CENTER);
		colorChooserGradient.setEnabled(!readOnly);
		colorChooserGradient.addClickListener(e->{background.setSelected(true); gradient.setSelected(true);});

		/* Deckkraft */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		JLabel alphaLabel=new JLabel(Language.tr("Surface.Ellipse.Dialog.Alpha")+":");
		line.add(alphaLabel);
		line.add(alpha=new JSlider(0,100,(int)Math.round(100*((ModelElementEllipse)element).getFillAlpha())));
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

		label.setPreferredSize(new Dimension(label.getPreferredSize().width,background.getPreferredSize().height));

		return content;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementEllipse ellipse=(ModelElementEllipse)element;

		ellipse.setLineWidth(lineWidth.getSelectedIndex());
		ellipse.setColor(colorChooserLine.getColor());

		if (background.isSelected()) {
			ellipse.setFillColor(colorChooserBackground.getColor());
		} else {
			ellipse.setFillColor(null);
		}

		if (gradient.isSelected()) {
			ellipse.setGradientFillColor(colorChooserGradient.getColor());
		} else {
			ellipse.setGradientFillColor(null);
		}

		ellipse.setFillAlpha(alpha.getValue()/100.0);
	}
}