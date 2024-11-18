/**
 * Copyright 2024 Alexander Herzog
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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.OptionalColorChooserButton;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.elements.ModelElementText.TextAlign;

/**
 * Dialog zur Bearbeitung einer einzelnen Zelle in {@link AnimationTableModel}
 * @see AnimationTableModel
 */
public class AnimationTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=3035918345164913772L;

	/**
	 * Zu bearbeitende Tabellenzelle
	 */
	private final ModelElementAnimationTable.Cell cell;

	/**
	 * Eingabe für den auszugebenden Text bzw. den zu berechnenden Ausdruck
	 */
	private final JTextField textEdit;

	/**
	 * Expression Builder Schaltfläche für den Fall, dass es sich um einen Rechenausdruck handelt
	 */
	private final JButton textEditExpressionButton;

	/**
	 * Modus: Text oder Rechenausdruck
	 */
	private final JComboBox<?> modeSelect;

	/**
	 * Wenn es sich um einen Rechenausdruck handelt: Soll das Ergebnis als Prozentwert ausgegeben werden?
	 */
	private final JCheckBox percentCheckBox;

	/**
	 * Ausrichtung des Textes in der Zelle
	 */
	private final JComboBox<?> textAlign;

	/**
	 * Textfarbe
	 */
	private final OptionalColorChooserButton colorText;

	/**
	 * Hintergrundfarbe
	 */
	private final OptionalColorChooserButton colorBackground;

	/**
	 * Konstruktor
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param cell	Zu bearbeitende Tabellenzelle
	 * @param model	Zugehöriges Modell (für den Expression Builder)
	 * @param helpRunnable	Hilfe-Callback
	 */
	public AnimationTableModelDialog(final Component owner, final ModelElementAnimationTable.Cell cell, final EditModel model, final Runnable helpRunnable) {
		super(owner,Language.tr("Surface.AnimationTable.DialogCell.Title"));
		this.cell=cell;

		/* GUI */
		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		JPanel line;
		JLabel label;
		Object[] data;

		/* Eingabezeile */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationTable.DialogCell.TextExpression")+":",cell.text);
		content.add(line=(JPanel)data[0]);
		textEdit=(JTextField)data[1];
		line.add(textEditExpressionButton=ModelElementBaseDialog.getExpressionEditButton(this,textEdit,false,false,model,model.surface),BorderLayout.EAST);
		textEditExpressionButton.setEnabled(cell.isExpression);

		/* Expression Builder */
		data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.AnimationTable.DialogCell.Mode")+":",new String[] {
				Language.tr("Surface.AnimationTable.DialogCell.Mode.Text"),
				Language.tr("Surface.AnimationTable.DialogCell.Mode.Expression")
		});
		content.add((JPanel)data[0]);
		modeSelect=(JComboBox<?>)data[1];
		modeSelect.setSelectedIndex(cell.isExpression?1:0);
		modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_TEXT,
				Images.MODE_EXPRESSION
		}));

		/* Prozentwert? */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(percentCheckBox=new JCheckBox(Language.tr("Surface.AnimationTable.DialogCell.Percent"),cell.isExpression && cell.isExpressionIsPercent));

		/* Ausrichtung */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.AnimationTable.Dialog.Align")+":"));
		line.add(textAlign=new JComboBox<>(new String[] {
				Language.tr("Surface.AnimationTable.Dialog.Align.Left"),
				Language.tr("Surface.AnimationTable.Dialog.Align.Center"),
				Language.tr("Surface.AnimationTable.Dialog.Align.Right"),
		}));
		textAlign.setRenderer(new IconListCellRenderer(new Images[]{
				Images.TEXT_ALIGN_LEFT,
				Images.TEXT_ALIGN_CENTER,
				Images.TEXT_ALIGN_RIGHT
		}));
		label.setLabelFor(textAlign);
		switch (cell.align) {
		case LEFT: textAlign.setSelectedIndex(0); break;
		case CENTER: textAlign.setSelectedIndex(1); break;
		case RIGHT: textAlign.setSelectedIndex(2); break;
		default: textAlign.setSelectedIndex(0); break;
		}

		/* Textfarbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorText=new OptionalColorChooserButton(Language.tr("Surface.AnimationTable.Dialog.TextColor")+":",cell.textColor,Color.BLACK));
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Surface.AnimationTable.Dialog.TextColor.Info")));

		/* Hintergrundfarbe */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(colorBackground=new OptionalColorChooserButton(Language.tr("Surface.AnimationTable.Dialog.BackgroundColor")+":",cell.backgroundColor,Color.WHITE));

		/* Auf Modusänderungen reagieren */
		modeSelect.addActionListener(e->{
			textEditExpressionButton.setEnabled(modeSelect.getSelectedIndex()==1);
			percentCheckBox.setEnabled(modeSelect.getSelectedIndex()==1);
			if (modeSelect.getSelectedIndex()==0) percentCheckBox.setSelected(false);
		});

		/* Start */
		setMinSizeRespectingScreensize(800,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	@Override
	protected void storeData() {
		cell.text=textEdit.getText();
		cell.isExpression=(modeSelect.getSelectedIndex()==1);
		cell.isExpressionIsPercent=percentCheckBox.isSelected();
		switch (textAlign.getSelectedIndex()) {
		case 0: cell.align=TextAlign.LEFT; break;
		case 1: cell.align=TextAlign.CENTER; break;
		case 2: cell.align=TextAlign.RIGHT; break;
		}
		cell.textColor=colorText.getColor();
		cell.backgroundColor=colorBackground.getColor();
	}
}
