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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.tools.ImageChooser;

/**
 * Dieser Dialog ermöglicht das Bearbeiten eines einzelnen Eintrags
 * aus einer {@link ModelElementAnimationImageTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see ModelElementAnimationImageTableModel
 * @see ModelElementAnimationImage
 */
public class ModelElementAnimationImageTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 5124598515121790386L;

	/** Gesamtes Editor-Modell (für den Expression-Builder) */
	private final EditModel model;
	/** Haupt-Zeichenfläche (für den Expression-Builder) */
	private final ModelSurface surface;
	/** Eingabefeld für die Bedingung, deren Erfüllung zur Anzeige des jeweiligen Bildes führen soll */
	private final JTextField expressionEdit;
	/** Auswahl des Bildes, das angezeigt werden soll, wenn die Bedingung erfüllt ist */
	private final ImageChooser imageChooser;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes element
	 * @param help	Hilfe-Callback
	 * @param expression	Bisherige Bedingung zur Anzeige des Bildes
	 * @param image	Bisheriges Bild
	 * @param model	Gesamtes Editor-Modell (für den Expression-Builder)
	 * @param surface	Haupt-Zeichenfläche (für den Expression-Builder)
	 */
	public ModelElementAnimationImageTableModelDialog(final Component owner, final Runnable help, final String expression, final BufferedImage image, final EditModel model, final ModelSurface surface) {
		super(owner,Language.tr("Surface.AnimationImage.Dialog.Images.Edit"));
		this.model=model;
		this.surface=surface;

		JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		if (expression!=null) {
			Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.AnimationImage.Dialog.Images.Expression"),expression);
			final JPanel line=(JPanel)data[0];
			expressionEdit=(JTextField)data[1];
			expressionEdit.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {checkData(false);}
				@Override public void keyReleased(KeyEvent e) {checkData(false);}
				@Override public void keyPressed(KeyEvent e) {checkData(false);}
			});
			content.add(line,BorderLayout.NORTH);
			line.add(ModelElementBaseDialog.getExpressionEditButton(this.owner,expressionEdit,true,false,model,surface),BorderLayout.EAST);
		} else {
			expressionEdit=null;
		}

		content.add(imageChooser=new ImageChooser(image,model.animationImages),BorderLayout.CENTER);

		setSizeRespectingScreensize(500,450);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		if (expressionEdit==null) return true;

		final int error=ExpressionEval.check(expressionEdit.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false));
		if (error>=0) {
			expressionEdit.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.AnimationImage.Dialog.Images.Expression.Error.Title"),String.format(Language.tr("Surface.AnimationImage.Dialog.Images.Expression.Error.Info"),expressionEdit.getText(),error+1));
			return false;
		} else {
			expressionEdit.setBackground(SystemColor.text);
			return true;
		}
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Liefert, wenn der Dialog mit "Ok" geschlossen wurde, die neue Bedingung zur Anzeige des Bildes.
	 * @return	Neue Bedingung zur Anzeige des Bildes
	 */
	public String getExpression() {
		if (expressionEdit==null) return null;
		return expressionEdit.getText().trim();
	}

	/**
	 * Liefert, wenn der Dialog mit "Ok" geschlossen wurde, das neue Bild.
	 * @return	Neues Bild
	 */
	public BufferedImage getImage() {
		return imageChooser.getImage();
	}
}
