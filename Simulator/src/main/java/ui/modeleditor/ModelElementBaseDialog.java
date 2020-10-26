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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.expressionbuilder.ExpressionBuilder;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ComplexLine;
import ui.modeleditor.elements.FontCache;
import ui.quickaccess.JPlaceholderTextField;

/**
 * Basisklasse zur Erstellung von Dialogen zur Bearbeitung der Eigenschaften eines <code>ModellElement</code>-Objekts
 * @author Alexander Herzog
 */
public abstract class ModelElementBaseDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6340641625398515847L;

	/**
	 * Objekt vom Typ <code>ModelElement</code>, welches in diesem Dialog bearbeitet werden soll
	 */
	protected final ModelElement element;

	/**
	 * <code>Runnable</code>-Objekt, welches ausgelöst wird, wenn auf die Hilfe-Schaltfläche geklickt wird.<br>
	 * Dieses <code>Runnable</code> wird durch den Konstruktor angelegt und basiert auf dem dort als Parameter übergebenen <code>helpTopic</code>-Wert
	 */
	protected final Runnable helpRunnable;

	/**
	 * Das zu dem {@link ModelSurface}, auf dem sich dieses Element befindet, gehörige {@link ModelSurfacePanel}.
	 * Es kann sein, dass dieses Feld <code>null</code> ist, wenn kein Panel ermittelt werden konnte.
	 */
	protected final ModelSurfacePanel surfacePanel;

	/** Beschreibung des Elements */
	private String description;
	/** Titel des Fensters (ohne Ergänzungen) */
	private final String plainTitle;
	/** Eingabefeld für den Namen des Elements */
	private final JTextField nameField;
	/** Standard-Farbe für das Element */
	private final Color defaultColor;
	/** Benutzerdefinierte Farbe für das Element */
	private Color userColor;
	/** Benutzerdefiniertes Bild für das Element */
	private BufferedImage userImage;
	/** Schaltfläche zum Ändern der ID des Elements */
	private final JButton idButton;
	/** Schaltfläche zum Aktivieren/Deaktivieren des Schreibschutzes für das Element */
	private final JButton protectedButton;
	/** Schaltfläche zum Ändern der Farbe des Elements */
	private final JButton colorButton;
	/** Schaltfläche zum Bearbeiten der Beschreibung des Elements */
	private final JButton descriptionButton;
	/** ID für einen Infotext oben im Dialog */
	private final String infoPanelID;

	/**
	 * Konstruktor der Klasse <code>ModelElementBaseDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param element	Zu bearbeitendes <code>ModelElement</code>
	 * @param helpTopic	Name des Hilfethemas mit dem die Hilfeschaltfläche verknüpft werden soll
	 * @param infoPanelID	ID für einen Infotext oben im Dialog
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param makeDialogVisible	Gibt an, ob der Dialog direkt durch den Konstruktur sichtbar geschaltet werden soll
	 */
	protected ModelElementBaseDialog(final Component owner, final String title, final ModelElement element, final String helpTopic, final String infoPanelID, final boolean readOnly, final boolean makeDialogVisible) {
		super(owner,title+" (id="+element.getId()+")",readOnly);
		if (owner instanceof ModelSurfacePanel) surfacePanel=(ModelSurfacePanel)owner; else surfacePanel=null;
		plainTitle=title;
		this.element=element;
		this.infoPanelID=infoPanelID;

		final Container finalContainer=this.owner;
		helpRunnable=()->Help.topicModal(finalContainer,helpTopic);
		initUserButtons();
		final JPanel fullContentPanel=createGUI(helpRunnable);
		fullContentPanel.setLayout(new BorderLayout());

		final JPanel infoPanel;
		final String infoPanelId=getInfoPanelID();
		if (infoPanelId!=null && !infoPanelId.isEmpty()) {
			infoPanel=InfoPanel.addTopPanel(fullContentPanel,infoPanelId);
		} else {
			infoPanel=null;
		}

		final JPanel contentPanel=new JPanel(new BorderLayout(0,5));
		fullContentPanel.add(contentPanel,BorderLayout.CENTER);

		if (hasNameField()) {
			final Object[] data=getInputPanel(Language.tr("Editor.DialogBase.NameLabel")+":",element.getName());
			nameField=(JTextField)data[1];
			nameField.setEditable(!readOnly);
			contentPanel.add((JPanel)data[0],BorderLayout.NORTH);

			final JPanel sub=new JPanel(new FlowLayout(FlowLayout.LEFT));
			((JPanel)data[0]).add(sub,BorderLayout.EAST);

			initUserNameFieldButtons(sub);

			if (element instanceof ModelElementBox) {
				sub.add(idButton=new JButton("id="+element.getId()));
				idButton.setToolTipText(Language.tr("Editor.DialogBase.ID.Tooltip"));
				idButton.addActionListener(e->changeID());
				idButton.setEnabled(!readOnly);
			} else {
				idButton=null;
			}

			sub.add(protectedButton=new JButton());
			protectedButton.setPreferredSize(new Dimension(26,26));
			protectedButton.setToolTipText(Language.tr("Editor.DialogBase.Protected.Tooltip"));
			protectedButton.addActionListener(e->protectedButton.setSelected(!protectedButton.isSelected()));
			protectedButton.setEnabled(!readOnly);
			protectedButton.setIcon(Images.GENERAL_LOCK_CLOSED.getIcon());
			protectedButton.setSelected(element.isDeleteProtected());

			if (element instanceof ModelElementBox) {
				final ModelElementBox boxElement=(ModelElementBox)element;
				defaultColor=boxElement.getTypeDefaultBackgroundColor();
				userColor=boxElement.getUserBackgroundColor();
				userImage=boxElement.getUserBackgroundImage();
				sub.add(colorButton=new JButton());
				colorButton.setPreferredSize(new Dimension(26,26));
				colorButton.setToolTipText(Language.tr("Editor.DialogBase.ColorTooltip"));
				setupColorButton();
				colorButton.addActionListener(e->showColorSelectDialog());
			} else {
				defaultColor=null;
				userColor=null;
				userImage=null;
				colorButton=null;
			}
			description=element.getDescription();
			sub.add(descriptionButton=new JButton());
			descriptionButton.setPreferredSize(new Dimension(26,26));
			descriptionButton.setToolTipText(Language.tr("Editor.DialogBase.Description.Tooltip"));
			descriptionButton.addActionListener(e->showDescriptionEditDialog());
			descriptionButton.setIcon(Images.MODELEDITOR_COMMENT.getIcon());
		} else {
			nameField=null;
			defaultColor=null;
			userColor=null;
			idButton=null;
			protectedButton=null;
			colorButton=null;
			descriptionButton=null;
		}

		JComponent content=getContentPanel();
		if (content!=null) contentPanel.add(content,BorderLayout.CENTER);

		setDialogSize();
		pack();
		SwingUtilities.invokeLater(()->{
			if (infoPanel!=null) infoPanel.setPreferredSize(infoPanel.getSize());
			pack();
			setLocationRelativeTo(this.owner);
			if (makeDialogVisible) {
				setVisible(true);
			}
		});
	}

	/**
	 * Konstruktor der Klasse <code>ModelElementBaseDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param element	Zu bearbeitendes <code>ModelElement</code>
	 * @param helpTopic	Name des Hilfethemas mit dem die Hilfeschaltfläche verknüpft werden soll
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 * @param makeDialogVisible	Gibt an, ob der Dialog direkt durch den Konstruktur sichtbar geschaltet werden soll
	 */
	protected ModelElementBaseDialog(final Component owner, final String title, final ModelElement element, final String helpTopic, final boolean readOnly, final boolean makeDialogVisible) {
		this(owner,title,element,helpTopic,null,readOnly,makeDialogVisible);
	}

	/**
	 * Konstruktor der Klasse <code>ModelElementBaseDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param element	Zu bearbeitendes <code>ModelElement</code>
	 * @param helpTopic	Name des Hilfethemas mit dem die Hilfeschaltfläche verknüpft werden soll
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	protected ModelElementBaseDialog(final Component owner, final String title, final ModelElement element, final String helpTopic, final boolean readOnly) {
		this(owner,title,element,helpTopic,readOnly,true);
	}

	/**
	 * Konstruktor der Klasse <code>ModelElementBaseDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Titel des Fensters
	 * @param element	Zu bearbeitendes <code>ModelElement</code>
	 * @param helpTopic	Name des Hilfethemas mit dem die Hilfeschaltfläche verknüpft werden soll
	 * @param infoPanelID	ID für einen Infotext oben im Dialog zurück
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	protected ModelElementBaseDialog(final Component owner, final String title, final ModelElement element, final String helpTopic, final String infoPanelID, final boolean readOnly) {
		this(owner,title,element,helpTopic,infoPanelID,readOnly,true);
	}

	/**
	 * Gibt optional die ID für einen Infotext oben im Dialog zurück.
	 * Kann überschrieben werden, um einen Wert zu liefern oder über den Konstruktor mit einem Wert belegt werden
	 * @return	ID für einen Infotext oben im Dialog (kann <code>null</code> sein)
	 * @see InfoPanel
	 */
	protected String getInfoPanelID() {
		return infoPanelID;
	}

	/**
	 * Gibt abgeleiteten Klassen die Möglichkeit, benutzerdefinierte Buttons hinzuzufügen
	 * @see BaseDialog#addUserButton
	 */
	protected void initUserButtons() {
	}

	/**
	 * Gibt abgeleiteten Klassen die Möglichkeit, benutzerdefinierte Buttons hinter dem Name-Eingabefeld hinzuzufügen
	 * @param panel	Panel, in das die Buttons eingefügt werden können
	 */
	protected void initUserNameFieldButtons(final JPanel panel) {
	}

	/**
	 * Gibt an, ob der Dialog eine Zeile zur Eingabe eines Namens für das Element anzeigen soll.<br>
	 * Diese Methode liefert standardmäßig <code>true</code> und muss nur überschrieben werden,
	 * wenn kein Namen geliefert werden soll.
	 * @return	Gibt <code>true</code> zurück, wenn eine Name-Zeile angezeigt werden soll.
	 */
	protected boolean hasNameField() {
		return true;
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	protected void setDialogSize() {
		pack();
	}

	/**
	 * Erstellt ein Textfeld mit einem Label links davor.
	 * @param labelText	Text des Labels
	 * @param value	Initialer Wert des Textfeldes
	 * @return	Liefert ein Objekt aus zwei Elementen: das <code>JPanel</code> das beide Elemente enthält und als zweites das <code>JTextField</code>
	 */
	public static final Object[] getInputPanel(final String labelText, final String value) {
		return getInputPanel(labelText,value,-1);
	}

	/**
	 * Erstellt ein Textfeld mit einem Label links davor.
	 * @param labelText	Text des Labels
	 * @param value	Initialer Wert des Textfeldes
	 * @param size	Breite des Textfeldes
	 * @return	Liefert ein Objekt aus zwei Elementen: das <code>JPanel</code> das beide Elemente enthält und als zweites das <code>JTextField</code>
	 */
	public static final Object[] getInputPanel(final String labelText, final String value, final int size) {
		JPanel panel;
		JLabel label;
		JTextField field;

		if (size>0) {
			panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(label=new JLabel(labelText));
			panel.add(field=new JTextField(size));
		} else {
			panel=new JPanel(new BorderLayout(5,0));

			Box box;

			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			final JPanel panelLeft=new JPanel(new FlowLayout());
			panelLeft.add(label=new JLabel(labelText));
			box.add(panelLeft);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.WEST);

			field=new JTextField();
			field.setMaximumSize(new Dimension(field.getMaximumSize().width,field.getPreferredSize().height));
			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			box.add(field);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.CENTER);
		}

		label.setLabelFor(field);
		if (value!=null) field.setText(value);
		return new Object[]{panel,field};
	}

	/**
	 * Erstellt ein Textfeld mit einem Label links davor.
	 * @param labelText	Text des Labels
	 * @param placeholder	Platzhaltertext (darf <code>null</code> oder leer sein)
	 * @param value	Initialer Wert des Textfeldes
	 * @return	Liefert ein Objekt aus zwei Elementen: das <code>JPanel</code> das beide Elemente enthält und als zweites das <code>JTextField</code>
	 */
	public static final Object[] getPlaceholderInputPanel(final String labelText, final String placeholder, final String value) {
		return getPlaceholderInputPanel(labelText,placeholder,value,-1);
	}

	/**
	 * Erstellt ein Textfeld mit einem Label links davor.
	 * @param labelText	Text des Labels
	 * @param placeholder	Platzhaltertext (darf <code>null</code> oder leer sein)
	 * @param value	Initialer Wert des Textfeldes
	 * @param size	Breite des Textfeldes
	 * @return	Liefert ein Objekt aus zwei Elementen: das <code>JPanel</code> das beide Elemente enthält und als zweites das <code>JTextField</code>
	 */
	public static final Object[] getPlaceholderInputPanel(final String labelText, final String placeholder, final String value, final int size) {
		JPanel panel;
		JLabel label;
		JPlaceholderTextField field;

		if (size>0) {
			panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(label=new JLabel(labelText));
			panel.add(field=new JPlaceholderTextField(size));
		} else {
			panel=new JPanel(new BorderLayout(5,0));

			Box box;

			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			final JPanel panelLeft=new JPanel(new FlowLayout());
			panelLeft.add(label=new JLabel(labelText));
			box.add(panelLeft);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.WEST);

			field=new JPlaceholderTextField();
			field.setMaximumSize(new Dimension(field.getMaximumSize().width,field.getPreferredSize().height));
			box=Box.createVerticalBox();
			box.add(Box.createVerticalGlue());
			box.add(field);
			box.add(Box.createVerticalGlue());
			panel.add(box,BorderLayout.CENTER);
		}

		if (placeholder!=null && !placeholder.trim().isEmpty()) field.setPlaceholder(placeholder);

		label.setLabelFor(field);
		if (value!=null) field.setText(value);
		return new Object[]{panel,field};
	}

	/**
	 * Erstellt eine Hilfe-Schaltfläche, die die Hilfe-Seite zur Berechnung von Ausdrücken öffnet
	 * @param owner	Übergeordnetes Element
	 * @return	Hilfe-Schaltfläche
	 */
	public static final JButton getExpressionHelpButton(final Container owner) {
		final JButton button=new JButton();
		button.setToolTipText(Language.tr("Editor.DialogBase.ExpressionHelpTooltip"));
		button.setIcon(Images.HELP.getIcon());
		button.addActionListener(e->Help.topicModal(owner,"Expressions"));
		final Dimension size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		return button;
	}

	/**
	 * Erstellt eine Bearbeiten-Schaltfläche, die das Bearbeiten eines Ausdrucks (Vergleich oder Rechengröße)
	 * in einem Expression-Builder-Dialog erlaubt.
	 * @param owner	Übergeordnetes Element
	 * @param inputLine	Eingabezeile, aus der der Ausdruck entnommen werden soll und in den der Ausdruck zurückgeschrieben werden soll
	 * @param isCompare	Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt
	 * @param hasClientData	Gibt an, ob Funktionen zum Zugriff auf Kundenobjekt-spezifische Datenfelder angeboten werden sollen
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Zeichenfläche, die alle Elemente enthält
	 * @return	Bearbeiten-Schaltfläche
	 */
	public static final JButton getExpressionEditButton(final Container owner, final JTextField inputLine, final boolean isCompare, final boolean hasClientData, final EditModel model, final ModelSurface surface) {
		ModelSurface mainSurface=(surface==null)?null:surface.getParentSurface();
		if (mainSurface==null) mainSurface=surface;

		final String[] variableNames;
		if (surface!=null && model!=null) {
			variableNames=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),hasClientData);
		} else {
			variableNames=new String[0];
		}

		final Map<String,String> initialVariableValues;
		if (model!=null) {
			initialVariableValues=model.getInitialVariablesWithValues();
		} else {
			initialVariableValues=new HashMap<>();
		}

		return getExpressionEditButton(owner,inputLine,isCompare,variableNames,initialVariableValues,ExpressionBuilder.getStationIDs(mainSurface),ExpressionBuilder.getStationNameIDs(mainSurface),hasClientData,false);
	}

	/**
	 * Erstellt eine Bearbeiten-Schaltfläche, die das Bearbeiten eines Ausdrucks (Vergleich oder Rechengröße)
	 * in einem Expression-Builder-Dialog erlaubt.
	 * @param owner	Übergeordnetes Element
	 * @param inputLine	Eingabezeile, aus der der Ausdruck entnommen werden soll und in den der Ausdruck zurückgeschrieben werden soll
	 * @param isCompare	Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt
	 * @param hasClientData	Gibt an, ob Funktionen zum Zugriff auf Kundenobjekt-spezifische Datenfelder angeboten werden sollen
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Zeichenfläche, die alle Elemente enthält
	 * @param additionalVariableNames	Liste mit zusätzlichen Variablennamen, die sich nicht aus den vorherigen Parametern ergeben
	 * @return	Bearbeiten-Schaltfläche
	 */
	public static final JButton getExpressionEditButton(final Container owner, final JTextField inputLine, final boolean isCompare, final boolean hasClientData, final EditModel model, final ModelSurface surface, final String[] additionalVariableNames) {
		ModelSurface mainSurface=surface.getParentSurface();
		if (mainSurface==null) mainSurface=surface;

		final List<String> variables=new ArrayList<>(Arrays.asList(surface.getMainSurfaceVariableNames(model.getModelVariableNames(),hasClientData)));
		if (additionalVariableNames!=null) variables.addAll(Arrays.asList(additionalVariableNames));
		return getExpressionEditButton(owner,inputLine,isCompare,variables.toArray(new String[0]),model.getInitialVariablesWithValues(),ExpressionBuilder.getStationIDs(mainSurface),ExpressionBuilder.getStationNameIDs(mainSurface),hasClientData,false);
	}

	/**
	 * Erstellt eine Bearbeiten-Schaltfläche, die das Bearbeiten eines Ausdrucks (Vergleich oder Rechengröße)
	 * in einem Expression-Builder-Dialog erlaubt.
	 * @param owner	Übergeordnetes Element
	 * @param inputLine	Eingabezeile, aus der der Ausdruck entnommen werden soll und in den der Ausdruck zurückgeschrieben werden soll
	 * @param isCompare	Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt
	 * @param variableNames	Liste mit allen bekannten Variablennamen
	 * @param initialVariableValues	Liste der initialen Variablen mit Werten
	 * @param stationIDs	Liste mit den Zuordnungen von Stations-IDs zu Stationsnamen
	 * @param stationNameIDs	Liste mit den Zuordnungen von Stations-IDs zu nutzerdefinierten Stationsnamen
	 * @param hasClientData	Gibt an, ob Funktionen zum Zugriff auf Kundenobjekt-spezifische Datenfelder angeboten werden sollen
	 * @return	Bearbeiten-Schaltfläche
	 */
	public static final JButton getExpressionEditButton(final Container owner, final JTextField inputLine, final boolean isCompare, final String[] variableNames, final Map<String,String> initialVariableValues, final Map<Integer,String> stationIDs, final Map<Integer,String> stationNameIDs, final boolean hasClientData) {
		return getExpressionEditButton(owner,inputLine,isCompare,variableNames,initialVariableValues,stationIDs,stationNameIDs,hasClientData,false);
	}

	/**
	 * Erstellt eine Bearbeiten-Schaltfläche, die das Bearbeiten eines Ausdrucks (Vergleich oder Rechengröße)
	 * in einem Expression-Builder-Dialog erlaubt.
	 * @param owner	Übergeordnetes Element
	 * @param inputLine	Eingabezeile, aus der der Ausdruck entnommen werden soll und in den der Ausdruck zurückgeschrieben werden soll
	 * @param isCompare	Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt
	 * @param variableNames	Liste mit allen bekannten Variablennamen
	 * @param initialVariableValues	Liste der initialen Variablen mit Werten
	 * @param stationIDs	Liste mit den Zuordnungen von Stations-IDs zu Stationsnamen
	 * @param stationNameIDs	Liste mit den Zuordnungen von Stations-IDs zu nutzerdefinierten Stationsnamen
	 * @param hasClientData	Gibt an, ob Funktionen zum Zugriff auf Kundenobjekt-spezifische Datenfelder angeboten werden sollen
	 * @param statisticsOnly	Gibt an, dass nur Funktionen angeboten werden sollen, deren Ergebnisse aus Statistikdaten gewonnen werden können (keine reinen Runtime-Daten)
	 * @return	Bearbeiten-Schaltfläche
	 */
	public static final JButton getExpressionEditButton(final Container owner, final JTextField inputLine, final boolean isCompare, final String[] variableNames, final Map<String,String> initialVariableValues, final Map<Integer,String> stationIDs, final Map<Integer,String> stationNameIDs, final boolean hasClientData, final boolean statisticsOnly) {
		final JButton button=new JButton();
		button.setToolTipText(Language.tr("Editor.DialogBase.ExpressionEditTooltip"));
		button.setIcon(Images.EXPRESSION_BUILDER.getIcon());
		button.addActionListener(e->{
			if (!inputLine.isEditable() || !inputLine.isEnabled()) return;
			final ExpressionBuilder dialog=new ExpressionBuilder(owner,inputLine.getText(),isCompare,variableNames,initialVariableValues,stationIDs,stationNameIDs,hasClientData,statisticsOnly,false);
			dialog.setVisible(true);
			if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
				inputLine.setText(dialog.getExpression());
				/* Prüfung des Feldes veranlassen, damit es z.B. nun als ok markiert wird, falls es vorher fehlerhaft war */
				for (KeyListener listener: inputLine.getKeyListeners()) listener.keyPressed(new KeyEvent(inputLine,0,0,0,KeyEvent.VK_SPACE,' '));
			}
		});
		final Dimension size=button.getPreferredSize();
		button.setPreferredSize(new Dimension(size.height,size.height));
		return button;
	}

	/**
	 * Erstellt eine Bearbeiten-Schaltfläche, die das Bearbeiten eines Ausdrucks (Vergleich oder Rechengröße)
	 * in einem Expression-Builder-Dialog erlaubt. Es werden dabei nur die Funktionen, deren Daten auch aus reinen Statistikdaten gewonnen werden können, angeboten.
	 * @param owner	Übergeordnetes Element
	 * @param inputLine	Eingabezeile, aus der der Ausdruck entnommen werden soll und in den der Ausdruck zurückgeschrieben werden soll
	 * @param isCompare	Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt
	 * @param stationIDs	Liste mit den Zuordnungen von Stations-IDs zu Stationsnamen
	 * @param stationNameIDs	Liste mit den Zuordnungen von Stations-IDs zu nutzerdefinierten Stationsnamen
	 * @return	Bearbeiten-Schaltfläche
	 */
	public static final JButton getStatisticsExpressionEditButton(final Container owner, final JTextField inputLine, final boolean isCompare, final Map<Integer,String> stationIDs, final Map<Integer,String> stationNameIDs) {
		return getExpressionEditButton(owner,inputLine,isCompare,null,null,stationIDs,stationNameIDs,false,true);
	}

	/**
	 * Erstellt eine Bearbeiten-Schaltfläche, die das Bearbeiten eines Ausdrucks (Vergleich oder Rechengröße)
	 * in einem Expression-Builder-Dialog erlaubt. Es werden dabei nur die Funktionen, deren Daten auch aus reinen Statistikdaten gewonnen werden können, angeboten.
	 * @param owner	Übergeordnetes Element
	 * @param inputLine	Eingabezeile, aus der der Ausdruck entnommen werden soll und in den der Ausdruck zurückgeschrieben werden soll
	 * @param isCompare	Gibt an, ob es sich bei dem Ausdruck um einen Vergleich (<code>true</code>) oder um einen zu einer Zahl auszurechnenden Ausdruck (<code>false</code>) handelt
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Zeichenfläche, die alle Elemente enthält
	 * @return	Bearbeiten-Schaltfläche
	 */
	public static final JButton getStatisticsExpressionEditButton(final Container owner, final JTextField inputLine, final boolean isCompare, final EditModel model, final ModelSurface surface) {
		ModelSurface mainSurface=surface.getParentSurface();
		if (mainSurface==null) mainSurface=surface;

		return getStatisticsExpressionEditButton(owner,inputLine,isCompare,ExpressionBuilder.getStationIDs(mainSurface),ExpressionBuilder.getStationNameIDs(mainSurface));
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	protected abstract JComponent getContentPanel();

	/**
	 * Liefert den im Dialog eingestellten Namen des Elements
	 * @return	Neuer Name für das Element
	 */
	protected final String getElementName() {
		if (nameField!=null) {
			String s=nameField.getText();
			/*
			s=s.replaceAll("<","");
			s=s.replaceAll(">","");
			 */
			return s.trim();
		} else {
			return element.getName();
		}
	}

	/**
	 * Liefert die im Dialog eingestellte Hintergrundfarbe für das Element
	 * @return	Hintergrundfarbe für das Element oder <code>null</code>, wenn die Vorgabe-Hintergrundfarbe verwendet werden soll
	 */
	protected final Color getElementColor() {
		return userColor;
	}

	/**
	 * Liefert das im Dialog eingestellte Bild für das Element
	 * @return	Bild für das Element oder <code>null</code>, wenn die normale Form verwendet werden soll
	 */
	protected final BufferedImage getElementImage() {
		return userImage;
	}

	/**
	 * Konfiguriert die Darstellung der Farbe auf {@link #colorButton}.
	 * @see #colorButton
	 */
	private void setupColorButton() {
		final BufferedImage image;
		if (userImage==null) {
			image=new BufferedImage(16,16,BufferedImage.TYPE_4BYTE_ABGR);
			final Color color=(userColor==null)?defaultColor:userColor;
			final Graphics g=image.getGraphics();
			g.setColor(color);
			g.fillRect(0,0,15,15);
			g.setColor(Color.DARK_GRAY);
			g.drawRect(0,0,15,15);
		} else {
			image=ScaledImageCache.getScaledImageCache().getScaledImage(userImage,16,16);
		}
		colorButton.setIcon(new ImageIcon(image));
	}

	/**
	 * Zeigt den Dialog zur Auswahl einer Farbe an.
	 * @see #colorButton
	 * @see ModelElementBaseColorDialog
	 */
	private void showColorSelectDialog() {
		if (readOnly) return;
		final ModelElementBaseColorDialog dialog=new ModelElementBaseColorDialog(this,helpRunnable,defaultColor,userColor,userImage,element.getModel().animationImages);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			userColor=dialog.getUserColor();
			userImage=dialog.getUserImage();
			setupColorButton();
		}
	}

	/**
	 * Zeit den Dialog zum Bearbeiten der Stationsbeschreibung an.
	 * @see ModelElementDescriptionDialog
	 */
	private void showDescriptionEditDialog() {
		final ModelElementDescriptionDialog dialog=new ModelElementDescriptionDialog(this,description,readOnly,element.getHelpPageName());
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			description=dialog.getDescription();
		}
	}

	/**
	 * Zeigt den Dialog zum Bearbeiten der Stations-ID an.
	 * @see ModelElementBaseIDDialog
	 */
	private void changeID() {
		final ModelSurface mainSurface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		final ModelElementBaseIDDialog dialog=new ModelElementBaseIDDialog(this,helpRunnable,element.getId(),mainSurface);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		final int newID=dialog.getNewID();
		if (newID<=0) return;

		final ModelElement otherElement=mainSurface.getByIdIncludingSubModels(newID);
		if (otherElement!=null) otherElement.setId(element.getId());
		element.setId(newID);
		idButton.setText("id="+newID);
		setTitle(plainTitle+" (id="+element.getId()+")");
	}

	/**
	 * Schreibt Name und ggf. Farbe in das dem Konstruktor übergebene Element zurück
	 */
	public void storeBaseProperties() {
		if (hasNameField()) {
			element.setName(getElementName());
			element.setDescription(description);
		}
		if (element instanceof ModelElementBox) {
			final ModelElementBox boxElement=(ModelElementBox)element;
			boxElement.setUserBackgroundColor(getElementColor());
			boxElement.setUserBackgroundImage(getElementImage());
		}
		if (protectedButton!=null) element.setDeleteProtection(protectedButton.isSelected());
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		storeBaseProperties();
		if (element instanceof ModelElementBox) SwingUtilities.invokeLater(()->{
			((ModelElementBox)element).fireChanged();
			element.getSurface().updateAdditionalIcons();
		});
	}

	private static BufferedImage getLineWidthImage(final int width, final int imageSize) {
		final BufferedImage image=new BufferedImage(imageSize,imageSize,BufferedImage.TYPE_4BYTE_ABGR);

		Graphics g=image.getGraphics();
		g.setColor(Color.BLACK);
		final int w=Math.min(imageSize-1,width);
		for (int i=1;i<=w;i++) g.drawLine(0,i+(imageSize-w)/2,imageSize-1,i+(imageSize-w)/2);

		return image;
	}

	/**
	 * Liefert ein Button, welches beim Anklicken den Dialog (nach Rückfrage) per Ok schließt und die Bediener-Seite im Modelleigenschaften-Dialog öffnet.
	 * @return	Button oder <code>null</code>, wenn kein Callback zum Aufruf des Modelleigenschaften-Dialogs verfügbar ist
	 */
	protected final JButton getOpenModelOperatorsButton() {
		if (surfacePanel==null) return null;

		final JButton resourceButton=new JButton(Language.tr("Editor.DialogBase.OpenModelResources"));
		resourceButton.setIcon(Images.MODELPROPERTIES_OPERATORS.getIcon());
		resourceButton.addActionListener(e->{
			if (!MsgBox.confirm(ModelElementBaseDialog.this,Language.tr("Editor.DialogBase.OpenModelResources"),Language.tr("Editor.DialogBase.OpenModelResources.Info"),Language.tr("Editor.DialogBase.OpenModelResources.InfoYes"),Language.tr("Editor.DialogBase.OpenModelResources.InfoNo"))) return;
			if (close(BaseDialog.CLOSED_BY_OK)) SwingUtilities.invokeLater(()->{
				surfacePanel.fireShowPropertiesDialog(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_OPERATORS);
			});
		});

		return resourceButton;
	}

	/**
	 * Liefert ein Button, welches beim Anklicken den Dialog (nach Rückfrage) per Ok schließt und die Transporter-Seite im Modelleigenschaften-Dialog öffnet.
	 * @return	Button oder <code>null</code>, wenn kein Callback zum Aufruf des Modelleigenschaften-Dialogs verfügbar ist
	 */
	protected final JButton getOpenModelTransportersButton() {
		if (surfacePanel==null) return null;

		final JButton resourceButton=new JButton(Language.tr("Editor.DialogBase.OpenModelTranporters"));
		resourceButton.setIcon(Images.MODELPROPERTIES_TRANSPORTERS.getIcon());
		resourceButton.addActionListener(e->{
			if (!MsgBox.confirm(ModelElementBaseDialog.this,Language.tr("Editor.DialogBase.OpenModelTranporters"),Language.tr("Editor.DialogBase.OpenModelTranporters.Info"),Language.tr("Editor.DialogBase.OpenModelTranporters.InfoYes"),Language.tr("Editor.DialogBase.OpenModelTranporters.InfoNo"))) return;
			if (close(BaseDialog.CLOSED_BY_OK)) SwingUtilities.invokeLater(()->{
				surfacePanel.fireShowPropertiesDialog(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_TRANSPORTERS);
			});
		});

		return resourceButton;
	}

	/**
	 * Erstellt ein ComboBox-Modell, welches verschiedene Linienbreiten darstellt
	 * @param min	Minimale Linienbreite in Pixeln
	 * @param max	Maximale Linienbreite in Pixeln
	 * @return	ComboBox-Modell mit Einträgen für verschiedene Linienbreiten
	 * @see LineWidthComboBoxCellRenderer
	 */
	public static DefaultComboBoxModel<JLabel> getLineWidthComboBoxModel(final int min, final int max) {
		final DefaultComboBoxModel<JLabel> lineWidthListModel=new DefaultComboBoxModel<>();

		for (int i=min;i<=max;i++) {
			final JLabel label=new JLabel((i==0)?Language.tr("Editor.DialogBase.LineWidth.No"):(""+i+" "+Language.tr("Editor.DialogBase.LineWidth.Pixel")));
			label.setIcon(new ImageIcon(getLineWidthImage(i,Math.max(16,max+2))));
			lineWidthListModel.addElement(label);
		}

		return lineWidthListModel;
	}

	/**
	 * ComboBox-Element-Renderer, der es ermöglicht, Vorschauwerte für die Linienbreiten in der ComboBox darzustellen.
	 * @author Alexander Herzog
	 */
	public static class LineWidthComboBoxCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 6448329627127036343L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			final Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((LineWidthComboBoxCellRenderer)renderer).setText(((JLabel)value).getText());
				((LineWidthComboBoxCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			return renderer;
		}
	}

	/**
	 * Erstellt eine ComboBox zur Auswahl einer Linienbreite
	 * @param min	Minimale Linienbreite in Pixeln
	 * @param max	Maximale Linienbreite in Pixeln
	 * @param value	Zu Anfang ausgewählte Linienbreite (nicht notwendig Index des Eintrags)
	 * @return	ComboBox zur Auswahl einer Linienbreite
	 */
	public static JComboBox<JLabel> getLineWidthComboBox(final int min, final int max, final int value) {
		final JComboBox<JLabel> lineWidthComboBox=getLineWidthComboBox(min,max);
		lineWidthComboBox.setSelectedIndex(value-min);
		return lineWidthComboBox;
	}

	/**
	 * Erstellt eine ComboBox zur Auswahl einer Linienbreite
	 * @param min	Minimale Linienbreite in Pixeln
	 * @param max	Maximale Linienbreite in Pixeln
	 * @return	ComboBox zur Auswahl einer Linienbreite
	 */
	public static JComboBox<JLabel> getLineWidthComboBox(final int min, final int max) {
		final JComboBox<JLabel> lineWidthComboBox=new JComboBox<>();
		lineWidthComboBox.setModel(getLineWidthComboBoxModel(min,max));
		lineWidthComboBox.setRenderer(new LineWidthComboBoxCellRenderer());
		return lineWidthComboBox;
	}

	/**
	 * Erstellt ein Panel, in dem sich ein Label und eine ComboBox zur Auswahl einer Linienbreite befinden
	 * @param labelText	Beschriftungstext, der vor der ComboBox stehen soll
	 * @param min	Minimale Linienbreite in Pixeln
	 * @param max	Maximale Linienbreite in Pixeln
	 * @return	Liefert ein 2-elementiges Array: <code>JPanel</code>-Objekt, in dem sich Label und ComboBox befinden, und <code>JComboBox</code> vom Typ <code>JLabel</code>
	 */
	public static Object[] getLineWidthInputPanel(final String labelText, final int min, final int max) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(labelText);
		panel.add(label);
		final JComboBox<JLabel> lineWidthComboBox=getLineWidthComboBox(min,max);
		panel.add(lineWidthComboBox);
		label.setLabelFor(lineWidthComboBox);

		return new Object[]{panel,lineWidthComboBox};
	}

	/**
	 * Erstellt ein Panel, in dem sich ein Label und eine ComboBox zur Auswahl einer Linienbreite befinden
	 * @param labelText	Beschriftungstext, der vor der ComboBox stehen soll
	 * @param min	Minimale Linienbreite in Pixeln
	 * @param max	Maximale Linienbreite in Pixeln
	 * @param value	Zu Anfang ausgewählte Linienbreite (nicht notwendig Index des Eintrags)
	 * @return	Liefert ein 2-elementiges Array: <code>JPanel</code>-Objekt, in dem sich Label und ComboBox befinden, und <code>JComboBox</code> vom Typ <code>JLabel</code>
	 */
	public static Object[] getLineWidthInputPanel(final String labelText, final int min, final int max, final int value) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(labelText);
		panel.add(label);
		final JComboBox<JLabel> lineWidthComboBox=getLineWidthComboBox(min,max,value);
		panel.add(lineWidthComboBox);
		label.setLabelFor(lineWidthComboBox);

		return new Object[]{panel,lineWidthComboBox};
	}

	/**
	 * Erstellt ein ComboBox-Modell, welches verschiedene Linientypen darstellt
	 * @return	ComboBox-Modell mit Einträgen für verschiedene Linientypen
	 * @see LineWidthComboBoxCellRenderer
	 */
	public static DefaultComboBoxModel<JLabel> getLineTypeComboBoxModel() {
		final DefaultComboBoxModel<JLabel> lineTypeListModel=new DefaultComboBoxModel<>();

		for (int i=0;i<=ComplexLine.MAX_TYPE;i++) {
			final JLabel label=new JLabel(ComplexLine.LINE_TYPE_NAMES[i]);
			label.setIcon(new ImageIcon(ComplexLine.getExample(i)));
			lineTypeListModel.addElement(label);
		}

		return lineTypeListModel;
	}

	/**
	 * ComboBox-Element-Renderer, der es ermöglicht, Vorschauwerte für den Linientyp in der ComboBox darzustellen.
	 * @author Alexander Herzog
	 */
	public static class LineTypeComboBoxCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -1510471598523775390L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((LineTypeComboBoxCellRenderer)renderer).setText(((JLabel)value).getText());
				((LineTypeComboBoxCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			return renderer;
		}
	}

	/**
	 * Erstellt eine ComboBox zur Auswahl eines Linientyps
	 * @param value	Initial ausgewählter Linientyp
	 * @return	ComboBox zur Auswahl eines Linientyps
	 */
	public static JComboBox<JLabel> getLineTypeComboBox(final int value) {
		final JComboBox<JLabel> lineTypeComboBox=getLineTypeComboBox();
		lineTypeComboBox.setSelectedIndex(value);
		return lineTypeComboBox;
	}

	/**
	 * Erstellt eine ComboBox zur Auswahl eines Linientyps
	 * @return	ComboBox zur Auswahl eines Linientyps
	 */
	public static JComboBox<JLabel> getLineTypeComboBox() {
		final JComboBox<JLabel> lineTypeComboBox=new JComboBox<>();
		lineTypeComboBox.setModel(getLineTypeComboBoxModel());
		lineTypeComboBox.setRenderer(new LineTypeComboBoxCellRenderer());
		return lineTypeComboBox;
	}

	/**
	 * Erstellt ein Panel, in dem sich ein Label und eine ComboBox zur Auswahl eines Linientyps befinden
	 * @param labelText	Beschriftungstext, der vor der ComboBox stehen soll
	 * @return	Liefert ein 2-elementiges Array: <code>JPanel</code>-Objekt, in dem sich Label und ComboBox befinden, und <code>JComboBox</code> vom Typ <code>JLabel</code>
	 */
	public static Object[] getLineWidthTypePanel(final String labelText) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(labelText);
		panel.add(label);
		final JComboBox<JLabel> lineTypeComboBox=getLineTypeComboBox();
		panel.add(lineTypeComboBox);
		label.setLabelFor(lineTypeComboBox);

		return new Object[]{panel,lineTypeComboBox};
	}

	/**
	 * Erstellt ein Panel, in dem sich ein Label und eine ComboBox zur Auswahl eines Linientyps befinden
	 * @param labelText	Beschriftungstext, der vor der ComboBox stehen soll
	 * @param value	Zu Anfang ausgewählte Linienbreite (nicht notwendig Index des Eintrags)
	 * @return	Liefert ein 2-elementiges Array: <code>JPanel</code>-Objekt, in dem sich Label und ComboBox befinden, und <code>JComboBox</code> vom Typ <code>JLabel</code>
	 */
	public static Object[] getLineWidthTypePanel(final String labelText, final int value) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(labelText);
		panel.add(label);
		final JComboBox<JLabel> lineTypeComboBox=getLineTypeComboBox(value);
		panel.add(lineTypeComboBox);
		label.setLabelFor(lineTypeComboBox);

		return new Object[]{panel,lineTypeComboBox};
	}

	/**
	 * Erstellt ein Panel, in dem sich ein Label und eine ComboBox mit aktivem Editor befinden
	 * @param labelText	Beschriftungstext, der vor der ComboBox stehen soll
	 * @param value	Text, der im ComboBox-Editor angezeigt werden soll
	 * @param values	Mögliche Auswahlwerte für die ComboBox
	 * @return	Liefert ein 2-elementiges Array: <code>JPanel</code>-Objekt, in dem sich Label und ComboBox befinden, und <code>JComboBox</code> vom Typ <code>String</code>
	 */
	public static Object[] getComboBoxPanel(final String labelText, final String value, final Collection<String> values) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(labelText);
		panel.add(label);
		final JComboBox<String> comboBox=new JComboBox<>(values.toArray(new String[0]));
		comboBox.setEditable(true);
		if (value!=null) comboBox.getEditor().setItem(value);
		panel.add(comboBox);
		label.setLabelFor(comboBox);

		return new Object[]{panel,comboBox};
	}

	/**
	 * Erstellt eine ComboBox zur Auswahl einer Schriftart
	 * @param initialValue	Initial auszuwählende Schriftart (darf <code>null</code> sein)
	 * @return	ComboBox zur Auswahl einer Schriftart
	 */
	public static JComboBox<FontCache.FontFamily> getFontFamilyComboBox(final FontCache.FontFamily initialValue) {
		List<FontCache.FontFamily> fontFamilies=Arrays.stream(FontCache.FontFamily.values()).sorted((f1,f2)->f1.getLocalName().compareToIgnoreCase(f2.getLocalName())).collect(Collectors.toList());

		int index=-1;
		if (initialValue!=null) index=fontFamilies.indexOf(initialValue);
		if (index<0 && fontFamilies.size()>0) index=0;

		final JComboBox<FontCache.FontFamily> comboBox=new JComboBox<>(fontFamilies.toArray(new FontCache.FontFamily[0]));
		if (index>=0) comboBox.setSelectedIndex(index);
		comboBox.setRenderer(new FontFamilyComboBoxCellRenderer());

		return comboBox;
	}

	/**
	 * Erstellt ein Panel, in dem sich ein Label und eine ComboBox zur Auswahl einer Schriftart befinden
	 * @param labelText	Beschriftungstext, der vor der ComboBox stehen soll
	 * @param initialValue	Initial auszuwählende Schriftart (darf <code>null</code> sein)
	 * @return	Liefert ein 2-elementiges Array: <code>JPanel</code>-Objekt, in dem sich Label und ComboBox befinden, und <code>JComboBox</code> vom Typ <code>FontCache.FontFamily</code>
	 */
	public static Object[] getFontFamilyComboBoxPanel(final String labelText, final FontCache.FontFamily initialValue) {
		final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JLabel label=new JLabel(labelText);
		panel.add(label);
		final JComboBox<FontCache.FontFamily> comboBox=getFontFamilyComboBox(initialValue);
		panel.add(comboBox);
		label.setLabelFor(comboBox);

		return new Object[]{panel,comboBox};
	}

	/**
	 * Renderer für die Auswahlbox für die verschiedenen Schriftarten
	 * @see ModelElementBaseDialog#getFontFamilyComboBox(ui.modeleditor.elements.FontCache.FontFamily)
	 */
	private static class FontFamilyComboBoxCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = -929195057808593785L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			final Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof FontCache.FontFamily) {
				final FontCache.FontFamily fontFamily=(FontCache.FontFamily)value;
				((FontFamilyComboBoxCellRenderer)renderer).setFont(FontCache.getFontCache().getFont(fontFamily,0,12));
				((FontFamilyComboBoxCellRenderer)renderer).setText(fontFamily.getLocalName());
			}
			return renderer;
		}
	}

	/**
	 * Aktiviert die Undo/Redo-Funktionen für ein Textfeld
	 * @param textArea	Textfeld, bei dem die Funktionen aktiviert werden sollen
	 */
	public static void addUndoFeature(final JTextArea textArea) {
		final UndoManager manager=new UndoManager();
		textArea.getDocument().addUndoableEditListener(manager);

		textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_Z && e.isControlDown()) {
					try {manager.undo();} catch (CannotUndoException e2) {}
					e.consume();
					return;
				}
				if (e.getKeyCode()==KeyEvent.VK_Y && e.isControlDown()) {
					try {manager.redo();} catch (CannotRedoException e2) {}
					e.consume();
					return;
				}
			}
		});
	}

	/**
	 * Zeichnet das Animationsicon eines Kundentyps auf einer Schaltfläche ein
	 * @param clientTypeName	Name des Kundentyps dessen Animationsicon verwendet werden soll
	 * @param button	Schaltfläche auf der das Icon eingezeichnet werden soll
	 * @param model	Modell dem die notwendigen Daten entnommen werden sollen
	 */
	public static void setClientIcon(final String clientTypeName, final JButton button, final EditModel model) {
		AnimationImageSource imageSource=new AnimationImageSource();
		final BufferedImage image=imageSource.get(model.clientData.getIcon(clientTypeName),model.animationImages,16);
		button.setIcon(new ImageIcon(image));
		button.setPreferredSize(new Dimension(26,26));
	}
}