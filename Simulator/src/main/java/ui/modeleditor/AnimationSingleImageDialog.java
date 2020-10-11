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
package ui.modeleditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.tools.ImageChooser;

/**
 * Erlaubt das Bearbeiten eines einzelnen Bildes für die Animationsicons-Liste
 * @author Alexander Herzog
 * @see AnimationImageDialog
 * @see ModelAnimationImages
 */
public class AnimationSingleImageDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 923519486870285777L;

	/** Bisheriger Name des Icons */
	private final String originalName;
	/** Liste der momentan vorhandenen (und damit blockierten) Namen. */
	private final List<String> names;
	/** Eingabefeld für den Namen des Bildes */
	private final JTextField nameEdit;
	/** Auswahlfeld für ein Bild */
	private final ImageChooser imageChooser;

	/**
	 * Konstruktor der Klasse<br>
	 * Der Dialog wird erstellt aber noch nicht angezeigt.
	 * @param owner	Übergeordnetes Element
	 * @param name	Bisheriger Name des Icons (kann <code>null</code> oder leer sein)
	 * @param image	Bisheriges Bild (kann <code>null</code> sein)
	 * @param names	Liste der momentan vorhandenen (und damit blockierten) Namen. Ist der <code>name</code>-Parameter nicht leer, so darf dieser in dieser Liste stehen.
	 * @param helpRunnable	Hilfe-Runnable
	 */
	public AnimationSingleImageDialog(final Component owner, final String name, final BufferedImage image, final String[] names, final Runnable helpRunnable) {
		super(owner,Language.tr("Animation.IconDialog.Single.Title"));

		if (name!=null) originalName=name; else originalName="";
		this.names=Arrays.asList(names);

		final JPanel content=createGUI(helpRunnable);
		content.setLayout(new BorderLayout());

		final JPanel top=new JPanel();
		content.add(top,BorderLayout.NORTH);
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("Animation.IconDialog.Single.IconName")+":",(name==null)?"":name);
		top.add((JPanel)data[0]);
		nameEdit=(JTextField)data[1];
		nameEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		content.add(imageChooser=new ImageChooser(image,null));

		checkData(false);

		setMinSizeRespectingScreensize(400,400);
		setSizeRespectingScreensize(400,400);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		final String name=nameEdit.getText();

		if (name.trim().isEmpty()) {
			ok=false;
			nameEdit.setBackground(Color.RED);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Animation.IconDialog.Single.IconName.Error"),Language.tr("Animation.IconDialog.Single.IconName.Error.NoName"));
				return false;
			}
		} else {
			if (!name.equals(originalName) && names.contains(name)) {
				ok=false;
				nameEdit.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Animation.IconDialog.Single.IconName.Error"),Language.tr("Animation.IconDialog.Single.IconName.Error.NameInUse"));
					return false;
				}
			} else {
				nameEdit.setBackground(SystemColor.text);
			}
		}

		if (imageChooser.getImage()==null) {
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Animation.IconDialog.Single.Icon.Error"),Language.tr("Animation.IconDialog.Single.Icon.Error.NoIcon"));
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
	 * Wurde der Dialog mit "Ok" geschlossen, so liefert diese Funktion den neuen Namen des Icons.
	 * @return	Neuer Name des Icons (oder <code>null</code>, wenn der Dialog abgebrochen wurde)
	 */
	public String getImageName() {
		if (getClosedBy()!=CLOSED_BY_OK) return null;

		return nameEdit.getText();
	}

	/**
	 * Wurde der Dialog mit "Ok" geschlossen, so liefert diese Funktion das neue Icon.
	 * @return	Neues Icon (oder <code>null</code>, wenn der Dialog abgebrochen wurde)
	 */
	public BufferedImage getImage() {
		if (getClosedBy()!=CLOSED_BY_OK) return null;

		return imageChooser.getImage();
	}
}
