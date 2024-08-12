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

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import systemtools.LabeledAlphaButton;
import systemtools.LabeledColorChooserButton;
import systemtools.OptionalColorChooserButton;
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
	private LabeledColorChooserButton colorChooserLine;
	/** Auswahl der Hintergrundfarbe */
	private OptionalColorChooserButton colorChooserBackground;
	/** Auswahl der Farbe für den Farbverlauf */
	private OptionalColorChooserButton colorChooserGradient;
	/** Schieberegler zur Auswahl des Deckkraft der Hintergrundfarbe */
	private LabeledAlphaButton alpha;

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

		JPanel line;

		/* Rahmenbreite */
		final Object[] data=getLineWidthInputPanel(Language.tr("Surface.Ellipse.Dialog.FrameWidth")+":",0,15,((ModelElementEllipse)element).getLineWidth());
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Zeile für Farben */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		/* Rahmenfarbe */
		line.add(colorChooserLine=new LabeledColorChooserButton(Language.tr("Surface.Ellipse.Dialog.FrameColor")+":",((ModelElementEllipse)element).getColor()));
		colorChooserLine.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Hintergrundfarbe */
		line.add(colorChooserBackground=new OptionalColorChooserButton(Language.tr("Surface.Ellipse.Dialog.FillBackground"),((ModelElementEllipse)element).getFillColor(),Color.WHITE));
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->{if (!colorChooserBackground.isActive()) colorChooserGradient.setActive(false);});

		line.add(Box.createHorizontalStrut(10));

		/* Farbverlauf */
		line.add(colorChooserGradient=new OptionalColorChooserButton(Language.tr("Surface.Ellipse.Dialog.BackgroundGradient"),((ModelElementEllipse)element).getGradientFillColor(),Color.WHITE));
		colorChooserGradient.setEnabled(!readOnly);
		colorChooserGradient.addClickListener(e->{if (colorChooserGradient.isActive()) colorChooserBackground.setActive(true);});

		line.add(Box.createHorizontalStrut(10));

		/* Deckkraft */
		line.add(alpha=new LabeledAlphaButton(Language.tr("Surface.Ellipse.Dialog.Alpha")+":",((ModelElementEllipse)element).getFillAlpha()));
		alpha.setEnabled(!readOnly);
		alpha.addClickListener(e->colorChooserBackground.setActive(true));

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
		ellipse.setFillColor(colorChooserBackground.getColor());
		ellipse.setGradientFillColor(colorChooserGradient.getColor());
		ellipse.setFillAlpha(alpha.getAlpha());
	}
}