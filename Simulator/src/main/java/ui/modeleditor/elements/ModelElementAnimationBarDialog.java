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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import systemtools.LabeledColorChooserButton;
import systemtools.MsgBox;
import systemtools.OptionalColorChooserButton;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationBar}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationBar
 */
public class ModelElementAnimationBarDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -2546131729099509268L;

	/** Eingabefeld für den Ausdruck dessen Auswertung den Füllstand für den Balken angeben soll */
	private AnimationExpressionPanel editExpression;
	/** Auswahlbox für die Füllrichtung des Balkens */
	private JComboBox<String> selectDirection;
	/** Eingabefeld für den Minimalwert */
	private JTextField editMinimum;
	/** Eingabefeld für den Maximalwert */
	private JTextField editMaximum;
	/** Achsenbeschriftung anzeigen */
	private AxisDrawerEdit axisLabels;
	/** Auswahlbox für die Linienbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Linienfarbe */
	private LabeledColorChooserButton colorChooserLine;
	/** Auswahl der Hintergrundfarbe */
	private OptionalColorChooserButton colorChooserBackground;
	/** Auswahl der Farbe für den Farbverlauf */
	private OptionalColorChooserButton colorChooserGradient;
	/** Auswahl der Balkenfarbe */
	private LabeledColorChooserButton colorChooserBar;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationBar}
	 * @param readOnly	Nur-Lese-Modus
	 */
	public ModelElementAnimationBarDialog(final Component owner, final ModelElementAnimationBar element, final ModelElementBaseDialog.ReadOnlyMode readOnly) {
		super(owner,Language.tr("Surface.AnimationBar.Dialog.Title"),element,"ModelElementAnimationBar",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(800,0);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationBar;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent getContentPanel() {
		JPanel line;
		JLabel label;

		final JPanel content=new JPanel();

		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		content.add(editExpression=new AnimationExpressionPanel(element,((ModelElementAnimationBar)element).getExpression(),readOnly,helpRunnable,new ArrayList<>()));

		/* Richtung und Minimum / Maximum */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationBar.Dialog.Bar")+":"));
		line.add(selectDirection=new JComboBox<>(new String[]{
				Language.tr("Surface.AnimationBar.Dialog.Bar.up"),
				Language.tr("Surface.AnimationBar.Dialog.Bar.right"),
				Language.tr("Surface.AnimationBar.Dialog.Bar.down"),
				Language.tr("Surface.AnimationBar.Dialog.Bar.left")
		}));
		selectDirection.setRenderer(new IconListCellRenderer(new Images[]{
				Images.ARROW_UP,
				Images.ARROW_RIGHT,
				Images.ARROW_DOWN,
				Images.ARROW_LEFT
		}));
		selectDirection.setEnabled(!readOnly);
		label.setLabelFor(selectDirection);
		line.add(label=new JLabel(Language.tr("Surface.AnimationBar.Dialog.Minimum")+":"));
		line.add(editMinimum=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(editMinimum);
		editMinimum.setEnabled(!readOnly);
		editMinimum.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		label.setLabelFor(editMinimum);
		line.add(label=new JLabel(Language.tr("Surface.AnimationBar.Dialog.Maximum")+":"));
		line.add(editMaximum=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(editMaximum);
		editMaximum.setEnabled(!readOnly);
		editMaximum.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		label.setLabelFor(editMaximum);

		/* Achsenbeschriftung */
		content.add(axisLabels=new AxisDrawerEdit(AxisDrawer.Mode.OFF,null,"",readOnly));

		/* Rahmenbreite */
		Object[] data=getLineWidthInputPanel(Language.tr("Surface.AnimationBar.Dialog.FrameWidth")+":",1,15);
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Farben */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		/* Rahmenfarbe */
		line.add(colorChooserLine=new LabeledColorChooserButton(Language.tr("Surface.AnimationBar.Dialog.FrameColor")+":",Color.BLACK));
		colorChooserLine.setEnabled(!readOnly);

		line.add(Box.createHorizontalStrut(10));

		/* Hintergrundfarbe */
		line.add(colorChooserBackground=new OptionalColorChooserButton(Language.tr("Surface.AnimationBar.Dialog.FillBackground"),null,Color.WHITE));
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->{if (!colorChooserBackground.isActive()) colorChooserGradient.setActive(false);});

		line.add(Box.createHorizontalStrut(10));

		/* Farbverlauf */
		line.add(colorChooserGradient=new OptionalColorChooserButton(Language.tr("Surface.AnimationBar.Dialog.BackgroundGradient"),null,Color.WHITE));
		colorChooserGradient.setEnabled(!readOnly);
		colorChooserGradient.addClickListener(e->{if (colorChooserGradient.isActive()) colorChooserBackground.setActive(true);});

		line.add(Box.createHorizontalStrut(10));

		/* Balkenfarbe */
		line.add(colorChooserBar=new LabeledColorChooserButton(Language.tr("Surface.AnimationBar.Dialog.BarColor")+":",Color.BLACK));
		colorChooserBar.setEnabled(!readOnly);

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationBar) {
			final ModelElementAnimationBar diagram=(ModelElementAnimationBar)element;

			switch (diagram.getDirection()) {
			case DIRECTION_UP: selectDirection.setSelectedIndex(0); break;
			case DIRECTION_RIGHT: selectDirection.setSelectedIndex(1); break;
			case DIRECTION_DOWN: selectDirection.setSelectedIndex(2); break;
			case DIRECTION_LEFT: selectDirection.setSelectedIndex(3); break;
			}
			editMinimum.setText(NumberTools.formatNumberMax(diagram.getMinValue()));
			editMaximum.setText(NumberTools.formatNumberMax(diagram.getMaxValue()));
			axisLabels.set(diagram.getAxisLabels(),null,diagram.getAxisLabelText());
			lineWidth.setSelectedIndex(diagram.getBorderWidth()-1);
			colorChooserLine.setColor(diagram.getBorderColor());
			colorChooserBackground.setColor(diagram.getBackgroundColor());
			colorChooserGradient.setColor(diagram.getGradientFillColor());
			colorChooserBar.setColor(diagram.getBarColor());
		}

		checkData(false);

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		if (readOnly) return false;

		boolean ok=true;

		if (!editExpression.checkData(showErrorMessages)) {
			ok=false;
			if (showErrorMessages) return false;
		}

		final Double Dmin=NumberTools.getDouble(editMinimum,true);
		if (Dmin==null) {
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationBar.Dialog.Minimum.Error.Title"),String.format(Language.tr("Surface.AnimationBar.Dialog.Minimum.Error.Info"),editMinimum.getText()));
				return false;
			}
			ok=true;
		}
		final Double Dmax=NumberTools.getDouble(editMaximum,true);
		if (Dmax==null) {
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationBar.Dialog.Maximum.Error.Title"),String.format(Language.tr("Surface.AnimationBar.Dialog.Maximum.Error.Info"),editMaximum.getText()));
				return false;
			}
			ok=true;
		}
		if (Dmin!=null && Dmax!=null) {
			final double min=Dmin;
			final double max=Dmax;
			if (min>=max) {
				editMaximum.setBackground(Color.red);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationBar.Dialog.InvalidRange.Title"),String.format(Language.tr("Surface.AnimationBar.Dialog.InvalidRange.Info"),editMaximum.getText(),editMinimum.getText()));
					return false;
				}
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
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

		final ModelElementAnimationBar bar=(ModelElementAnimationBar)element;
		editExpression.storeData();

		switch (selectDirection.getSelectedIndex()) {
		case 0: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_UP); break;
		case 1: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_RIGHT); break;
		case 2: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_DOWN); break;
		case 3: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_LEFT); break;
		}
		bar.setMinValue(NumberTools.getDouble(editMinimum,true));
		bar.setMaxValue(NumberTools.getDouble(editMaximum,true));
		bar.setAxisLabels(axisLabels.getMode());
		bar.setAxisLabelText(axisLabels.getYLabel());
		bar.setBorderWidth(lineWidth.getSelectedIndex()+1);
		bar.setBorderColor(colorChooserLine.getColor());
		bar.setBackgroundColor(colorChooserBackground.getColor());
		bar.setGradientFillColor(colorChooserGradient.getColor());
		bar.setBarColor(colorChooserBar.getColor());
	}
}