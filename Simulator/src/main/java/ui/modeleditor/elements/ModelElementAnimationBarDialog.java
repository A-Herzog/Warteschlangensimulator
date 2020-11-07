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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import systemtools.MsgBox;
import systemtools.SmallColorChooser;
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
	private JTextField editExpression;
	/** Auswahlbox für die Füllrichtung des Balkens */
	private JComboBox<String> selectDirection;
	/** Eingabefeld für den Minimalwert */
	private JTextField editMinimum;
	/** Eingabefeld für den Maximalwert */
	private JTextField editMaximum;
	/** Auswahlbox für ide Linienbreite */
	private JComboBox<JLabel> lineWidth;
	/** Auswahl der Linienfarbe */
	private SmallColorChooser colorChooserLine;
	/** Option: Hintergrundfarbe verwenden? */
	private JCheckBox background;
	/** Auswahl der Hintergrundfarbe */
	private SmallColorChooser colorChooserBackground;
	/** Auswahl der Balkenfarbe */
	private SmallColorChooser colorChooserBar;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnimationBar}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationBarDialog(final Component owner, final ModelElementAnimationBar element, final boolean readOnly) {
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
		JPanel line, cell;
		JLabel label;

		final JPanel content=new JPanel();

		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		/* Ausdruck */
		Object[] data=getInputPanel(Language.tr("Surface.AnimationBar.Dialog.Expression")+":","");
		content.add((JPanel)data[0]);
		editExpression=(JTextField)data[1];
		editExpression.setEditable(!readOnly);
		((JPanel)data[0]).add(getExpressionEditButton(this,editExpression,false,false,element.getModel(),element.getSurface()),BorderLayout.EAST);
		editExpression.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

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
		editMinimum.setEditable(!readOnly);
		editMinimum.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		label.setLabelFor(editMinimum);
		line.add(label=new JLabel(Language.tr("Surface.AnimationBar.Dialog.Maximum")+":"));
		line.add(editMaximum=new JTextField(10));
		editMaximum.setEditable(!readOnly);
		editMaximum.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		label.setLabelFor(editMaximum);

		/* Rahmenbreite */
		data=getLineWidthInputPanel(Language.tr("Surface.AnimationBar.Dialog.FrameWidth")+":",1,15);
		content.add((JPanel)data[0],BorderLayout.NORTH);
		lineWidth=(JComboBox<JLabel>)data[1];
		lineWidth.setEnabled(!readOnly);

		/* Farben */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.CENTER);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationBar.Dialog.FrameColor")+":"),BorderLayout.NORTH);
		cell.add(colorChooserLine=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserLine.setEnabled(!readOnly);
		label.setLabelFor(colorChooserLine);

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(background=new JCheckBox(Language.tr("Surface.AnimationBar.Dialog.FillBackground")),BorderLayout.NORTH);
		background.setEnabled(!readOnly);
		cell.add(colorChooserBackground=new SmallColorChooser(Color.WHITE),BorderLayout.CENTER);
		colorChooserBackground.setEnabled(!readOnly);
		colorChooserBackground.addClickListener(e->background.setSelected(true));

		label.setPreferredSize(new Dimension(label.getPreferredSize().width,background.getPreferredSize().height));

		line.add(cell=new JPanel(new BorderLayout()));
		cell.add(label=new JLabel(Language.tr("Surface.AnimationBar.Dialog.BarColor")+":"),BorderLayout.NORTH);
		cell.add(colorChooserBar=new SmallColorChooser(Color.BLACK),BorderLayout.CENTER);
		colorChooserBar.setEnabled(!readOnly);
		label.setLabelFor(colorChooserBar);

		label.setPreferredSize(new Dimension(label.getPreferredSize().width,background.getPreferredSize().height));

		/* Daten eintragen */
		if (element instanceof ModelElementAnimationBar) {
			editExpression.setText(((ModelElementAnimationBar)element).getExpression());
			switch (((ModelElementAnimationBar)element).getDirection()) {
			case DIRECTION_UP: selectDirection.setSelectedIndex(0); break;
			case DIRECTION_RIGHT: selectDirection.setSelectedIndex(1); break;
			case DIRECTION_DOWN: selectDirection.setSelectedIndex(2); break;
			case DIRECTION_LEFT: selectDirection.setSelectedIndex(3); break;
			}
			editMinimum.setText(NumberTools.formatNumber(((ModelElementAnimationBar)element).getMinValue()));
			editMaximum.setText(NumberTools.formatNumber(((ModelElementAnimationBar)element).getMaxValue()));
			lineWidth.setSelectedIndex(((ModelElementAnimationBar)element).getBorderWidth()-1);
			colorChooserLine.setColor(((ModelElementAnimationBar)element).getBorderColor());
			background.setSelected(((ModelElementAnimationBar)element).getBackgroundColor()!=null);
			if (((ModelElementAnimationBar)element).getBackgroundColor()!=null) colorChooserBackground.setColor(((ModelElementAnimationBar)element).getBackgroundColor());
			colorChooserBar.setColor(((ModelElementAnimationBar)element).getBarColor());
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

		final String text=editExpression.getText().trim();
		if (text.isEmpty()) {
			ok=false;
			editExpression.setBackground(Color.red);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.AnimationBar.Dialog.Expression.Error.Title"),Language.tr("Surface.AnimationBar.Dialog.Expression.ErrorNoExpression.Info"));
				return false;
			}
		} else {
			int error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false));
			if (error>=0) {
				ok=false;
				editExpression.setBackground(Color.red);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.AnimationBar.Dialog.Expression.Error.Title"),String.format(Language.tr("Surface.AnimationBar.Dialog.Expression.ErrorInvalidExpression.Info"),text,error+1));
					return false;
				}
			} else {
				editExpression.setBackground(SystemColor.text);
			}
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
		bar.setExpression(editExpression.getText().trim());

		switch (selectDirection.getSelectedIndex()) {
		case 0: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_UP); break;
		case 1: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_RIGHT); break;
		case 2: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_DOWN); break;
		case 3: bar.setDirection(ModelElementAnimationBar.FillDirection.DIRECTION_LEFT); break;
		}
		bar.setMinValue(NumberTools.getDouble(editMinimum,true));
		bar.setMaxValue(NumberTools.getDouble(editMaximum,true));
		bar.setBorderWidth(lineWidth.getSelectedIndex()+1);
		bar.setBorderColor(colorChooserLine.getColor());
		bar.setBackgroundColor((background.isSelected())?colorChooserBackground.getColor():null);
		bar.setBarColor(colorChooserBar.getColor());
	}
}