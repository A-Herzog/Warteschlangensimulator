/**
 * Copyright 2021 Alexander Herzog
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
package ui.modelproperties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.elements.ModelElementText;
import ui.script.ScriptEditorAreaBuilder;

/**
 * Dialogseite "Modellbeschreibung"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageDescription extends ModelPropertiesDialogPage {
	/** Eingabefeld: "Name des Modells" */
	private RSyntaxTextArea name;
	/** Eingabefeld: "Autor des Modells" */
	private JTextField author;
	/** Eingabefeld: "E-Mail-Adresse des Autors des Modells" */
	private JTextField authorEMail;
	/** Eingabefeld: "Modellbeschreibung" */
	private RSyntaxTextArea description;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageDescription(final ModelPropertiesDialog dialog, final EditModel model, final boolean readOnly, final Runnable help) {
		super(dialog,model,readOnly,help);
	}

	/**
	 * Fügt einen Text in einem eigenen Panel ein.
	 * @param parent	Übergeordnetes Element
	 * @param text	Auszugebender Text
	 * @param position	Wenn das übergeordnete Panel ein {@link BorderLayout} verwendet, kann hier die Position angegeben werden
	 * @return	Neues Label in einem eigenen Panel in dem übergeordneten Panel
	 */
	private JLabel addLabel(final JPanel parent, final String text, final String position) {
		JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		if (parent.getLayout() instanceof BorderLayout) {
			parent.add(panel,position);
		} else {
			parent.add(panel);
		}
		JLabel label=new JLabel(text);
		panel.add(label);
		return label;
	}

	/**
	 * Wurde über {@link #addNameToModel(String)} ein Beschriftungselement zu
	 * der Modell-Zeichenfläche hinzugefügt, so wird dieses auch hier gespeichert.
	 * Wird die Funktion später (während der Dialog immer noch offen ist) erneut
	 * ausgeführt, so wird der Inhalt des Beschriftungselements entsprechend
	 * geändert und nicht ein zweites Element angelegt.
	 */
	private ModelElementText nameElement=null;

	/**
	 * Fügt den Namen des Modells als Beschriftungselement in das Modell ein.
	 * @param name	Name des Modells
	 * @see #nameElement
	 */
	private void addNameToModel(final String name) {
		int minY=Integer.MAX_VALUE;

		if (nameElement==null) {
			for (ModelElement element: model.surface.getElements()) {
				/* Element, das am weitesten oben ist, finden */
				if (element instanceof ModelElementPosition) {
					final int y=((ModelElementPosition)element).getPosition(true).y;
					if (y<minY) minY=y;
				}

				/* Text-Element ganz weit oben finden */
				if (element instanceof ModelElementText) {
					final ModelElementText text=(ModelElementText)element;
					if (nameElement==null) {nameElement=text; continue;}
					final Point p1=nameElement.getPosition(true);
					final Point p2=text.getPosition(true);
					if (p2.x*p2.x+p2.y*p2.y<p1.x*p1.x+p1.y*p1.y) nameElement=text;
				}
			}
			if (nameElement!=null && nameElement.getPosition(true).y>minY) nameElement=null;
		}

		/* Name einfügen */
		if (nameElement!=null) {
			/* Text-Element gefunden? -> Direkt ändern */
			nameElement.setText(name);
		} else {
			/* Alle bisherigen Elemente nach unten verschieben */
			final int delta=100-minY;
			if (delta>0) for (ModelElement element: model.surface.getElements()) if (element instanceof ModelElementPosition) {
				final ModelElementPosition posElement=(ModelElementPosition)element;
				final Point p=posElement.getPosition(false);
				p.y+=delta;
				posElement.setPosition(p);
			}

			/* Neues Element hinzufügen */
			model.surface.add(nameElement=new ModelElementText(model,model.surface));
			nameElement.setPosition(new Point(50,50));
			nameElement.setText(name);
			nameElement.setTextSize(18);
			nameElement.setTextBold(true);
		}
	}

	@Override
	public void build(final JPanel content) {
		JPanel sub;
		JPanel line;
		JLabel label;
		Object[] data;

		final JPanel top=new JPanel();
		top.setLayout(new BoxLayout(top,BoxLayout.PAGE_AXIS));
		content.add(top,BorderLayout.NORTH);

		data=ScriptEditorAreaBuilder.getInputPanel(Language.tr("Editor.Dialog.Tab.ModelDescription.NameOfTheModel")+":",model.name);
		top.add(sub=(JPanel)data[0]);
		name=(RSyntaxTextArea)data[1];
		name.setEditable(!readOnly);
		final JButton buttonAsNameToSurface;
		sub.add(buttonAsNameToSurface=new JButton(),BorderLayout.EAST);
		buttonAsNameToSurface.setIcon(Images.MODELPROPERTIES_DESCRIPTION_ADD_TO_SURFACE.getIcon());
		buttonAsNameToSurface.setToolTipText(Language.tr("Editor.Dialog.Tab.ModelDescription.NameOfTheModel.AddNameToModel"));
		buttonAsNameToSurface.setEnabled(!readOnly && !model.name.trim().isEmpty());
		buttonAsNameToSurface.addActionListener(e->addNameToModel(name.getText().trim()));
		addKeyListener(name,()->buttonAsNameToSurface.setEnabled(!readOnly && !name.getText().trim().isEmpty()));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.ModelDescription.Author")+":",(model.author==null)?"":model.author);
		top.add((JPanel)data[0]);
		author=(JTextField)data[1];
		author.setEditable(!readOnly);
		if (!readOnly) {
			final JButton authorDefaultButton=new JButton();
			((JPanel)data[0]).add(authorDefaultButton,BorderLayout.EAST);
			authorDefaultButton.addActionListener(e->author.setText(EditModel.getDefaultAuthor()));
			authorDefaultButton.setIcon(Images.MODELPROPERTIES_DESCRIPTION_SET_AUTHOR.getIcon());
			authorDefaultButton.setToolTipText(Language.tr("Editor.Dialog.Tab.ModelDescription.Author.SetDefault"));
		}

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Editor.Dialog.Tab.ModelDescription.AuthorEMail")+":",(model.authorEMail==null)?"":model.authorEMail);
		top.add((JPanel)data[0]);
		authorEMail=(JTextField)data[1];
		authorEMail.setEditable(!readOnly);

		content.add(sub=new JPanel(new BorderLayout()),BorderLayout.CENTER);
		label=addLabel(sub,Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription")+":",BorderLayout.NORTH);
		sub.add(new ScriptEditorAreaBuilder.RScrollPane(description=ScriptEditorAreaBuilder.getPlainTextField(model.description,readOnly)),BorderLayout.CENTER);
		label.setLabelFor(description);

		content.add(sub=new JPanel(),BorderLayout.SOUTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JButton buttonAutoDescription;
		line.add(buttonAutoDescription=new JButton(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto")));
		buttonAutoDescription.setToolTipText(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.Hint"));
		final ModelDescriptionBuilderStyled builder=new ModelDescriptionBuilderStyled(model,null,true);
		builder.run();
		final String autoDescription=builder.getText();
		buttonAutoDescription.setIcon(Images.MODELPROPERTIES_DESCRIPTION_AUTO_CREATE.getIcon());
		buttonAutoDescription.setEnabled(!readOnly && !autoDescription.trim().isEmpty());
		buttonAutoDescription.addActionListener(e->{
			if (!description.getText().trim().isEmpty()) {
				if (!MsgBox.confirm(dialog,Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceTitle"),Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceInfo"),Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceYes"),Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Auto.ReplaceNo"))) return;
			}
			description.setText(autoDescription);
		});

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		if (model.surface.getElementCount()==1) {
			line.add(new JLabel(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Components.One")));
		} else {
			line.add(new JLabel(String.format(Language.tr("Editor.Dialog.Tab.ModelDescription.ModelDescription.Components.Number"),model.surface.getElementCount())));
		}
	}

	@Override
	public void storeData() {
		model.name=name.getText();
		model.author=author.getText();
		model.authorEMail=authorEMail.getText();
		model.description=description.getText();
	}
}
