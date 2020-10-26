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
package ui.modeleditor.templates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.EditorPanel;
import ui.help.Help;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Zeigt einen Dialog zum Hinzufügen einer Elementenvorlage an
 * @author Alexander Herzog
 * @see UserTemplates
 */
public class EditTemplateDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4202359606313518050L;

	/**
	 * Neuerstelltes Template
	 * @see #getTemplate()
	 */
	private UserTemplate newTemplate;

	/**
	 * Modellbasierende Nutzervorlagen
	 */
	private UserTemplates modelTemplates;

	/**
	 * Name des Templates
	 */
	private JTextField editName;

	/**
	 * Globale Vorlage oder modellabhängige Vorlage
	 */
	private JCheckBox checkGlobal;

	/**
	 * Editor für die Vorlage
	 */
	private EditorPanel editor;

	/**
	 * Konstruktor der Klasse<br>
	 * In diesem Modus wird das neue Template beim Schließen des Dialogs per "Ok" direkt in die jeweilige Templates-Liste aufgenommen.
	 * @param owner	Übergeordnetes Element
	 * @param surface	Zeichenfläche aus der die selektierten Elemente entnommen werden sollen
	 * @param modelTemplates	Modellbasierende Nutzervorlagen
	 */
	public EditTemplateDialog(final Component owner, final ModelSurface surface, final UserTemplates modelTemplates) {
		super(owner,Language.tr("UserTemplates.AddDialog.Title"));
		this.modelTemplates=modelTemplates;

		final List<ModelElement> elements=surface.getSelectedElements();
		if (elements.isEmpty()) {
			MsgBox.error(owner,Language.tr("UserTemplates.AddDialog.NoElementsErrorTitle"),Language.tr("UserTemplates.AddDialog.NoElementsErrorInfo"));
			return;
		}
		final ByteArrayInputStream inputStream=UserTemplateTools.getElements(surface,elements);
		initGUI(inputStream,Language.tr("UserTemplates.AddDialog.Name.Default"),false);
	}

	/**
	 * Konstruktor der Klasse<br>
	 * In diesem Modus werden die Templates-Listen nicht verändert. Das veränderte Template kann per {@link EditTemplateDialog#getTemplate()} abgefragt werden
	 * @param owner	Übergeordnetes Element
	 * @param oldTemplate	Template, das bearbeitet werden soll (kann ein modellbasierendes oder ein globales Tempalte sein)
	 * @param isGlobal	Gibt an, ob es sich bei der zu bearbeitenden Vorlage um eine globale Vorlage handelt
	 */
	public EditTemplateDialog(final Component owner, final UserTemplate oldTemplate, final boolean isGlobal) {
		super(owner,Language.tr("UserTemplates.AddDialog.TitleEdit"));
		modelTemplates=null;

		initGUI(UserTemplateTools.getAllElements(oldTemplate),oldTemplate.getName(),isGlobal);
	}

	/**
	 * Baut die Zeichenfläche (mit Vorlagenelementen darin) usw. auf.
	 * @param inputStream	Vorlage
	 * @param name	Name der Vorlage
	 * @param isGlobal	Gibt an, ob es sich bei der zu bearbeitenden Vorlage um eine globale Vorlage handelt
	 */
	private void initGUI(final ByteArrayInputStream inputStream, final String name, final boolean isGlobal) {
		/* Zeichenfläche aus Stream erstellen */

		final EditModel model=new EditModel();
		UserTemplateTools.copyToSurface(inputStream,model.surface);

		/* GUI als solches */

		final JPanel content=createGUI(()->Help.topicModal(this,"UserTemplates"));
		content.setLayout(new BorderLayout());
		final JPanel config=new JPanel();
		config.setLayout(new BoxLayout(config,BoxLayout.PAGE_AXIS));
		content.add(config,BorderLayout.NORTH);

		JPanel line;

		/* Konfigurationsbereich */

		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("UserTemplates.AddDialog.Name")+":",name);
		config.add((JPanel)data[0]);
		editName=(JTextField)data[1];
		editName.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		config.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkGlobal=new JCheckBox(Language.tr("UserTemplates.AddDialog.Global"),isGlobal));

		config.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		line.add(new JLabel(getInfo(model.surface.getElements())));

		/* Zeichenfläche */

		content.add(editor=new EditorPanel(this,model,true,false,true));
		editor.setStatusBarVisible(false);
		editor.allowEditorDialogs();

		/* Start */

		setMinSizeRespectingScreensize(550,450);
		setResizable(true);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Liefert eine Informationszeile mit weiteren Angaben zu den Elementen der Vorlage
	 * @param elements	Liste der Elemente der Vorlage
	 * @return	Informationszeile
	 */
	private String getInfo(final List<ModelElement> elements) {
		final int countAll=elements.size();
		final int countStations=(int)elements.stream().filter(element->(element instanceof ModelElementBox)).count();
		return String.format(Language.tr("UserTemplates.AddDialog.Info"),countStations,countAll);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (editName.getText().trim().isEmpty()) {
			editName.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(owner,Language.tr("UserTemplates.AddDialog.Name.ErrorTitle"),Language.tr("UserTemplates.AddDialog.Name.ErrorInfo"));
				return false;
			}
		} else {
			editName.setBackground(SystemColor.text);
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		final ByteArrayInputStream inputStream=UserTemplateTools.getAllElements(editor.getModel().surface);
		newTemplate=new UserTemplate(inputStream,editName.getText().trim());

		if (modelTemplates!=null) {
			/* Neues Template direkt in Liste aufnehmen */
			final UserTemplates globalTemplates=UserTemplates.getInstance();
			final boolean newGlobal=checkGlobal.isSelected();
			if (newGlobal) globalTemplates.add(newTemplate); else modelTemplates.add(newTemplate);
			if (newGlobal) globalTemplates.saveGlobalTemplates();
		}
	}

	/**
	 * Wurde der Dialog im Modus zur Bearbeitung eines bestehenden Templates aufgerufen und per "Ok" geschlossen,
	 * so kann über diese Funktion das veränderte Template abgefragt werden.
	 * @return	Verändertes Template
	 */
	public UserTemplate getTemplate() {
		return newTemplate;
	}

	/**
	 * Gibt an, ob es sich bei der Vorlage um eine globale Vorlage handeln soll.
	 * @return	Liefert <code>true</code>, wenn es sich um eine globale Vorlage handeln soll.
	 */
	public boolean isGlobal() {
		return checkGlobal.isSelected();
	}
}