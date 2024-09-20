/**
 * Copyright 2023 Alexander Herzog
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
package ui.generator;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import simulator.editmodel.EditModel;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementText;

/**
 * Basisklasse für ein Panel mit Einstellungen zur Erzeugung von Beispielmodellen
 * @see ModelGeneratorDialog
 */
public abstract class ModelGeneratorPanelBase extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6096523095394137763L;


	/**
	 * Listener, die benachrichtigt werden, wenn die Einstellungen verändert wurden.
	 * @see #fireModelChanged()
	 */
	private Set<Runnable> modelChangeListeners=new HashSet<>();

	/**
	 * Konstruktor der Klasse
	 */
	public ModelGeneratorPanelBase() {
		/*
		 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
		 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
		 */
	}

	/**
	 * Registriert einen Listener, der benachrichtigt werden soll, wenn die Einstellungen verändert wurden.
	 * @param modelChangeListener	Zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener in die Liste neu aufgenommen wurde (und nicht bereits enthalten ist)
	 */
	public boolean addModelChangeListener(final Runnable modelChangeListener) {
		return modelChangeListeners.add(modelChangeListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste  der zu benachrichtigen Listener, wenn die Einstellungen verändert wurden.
	 * @param modelChangeListener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener in der Liste enthalten war und entfernt werden konnte
	 */
	public boolean removeModelChangeListener(final Runnable modelChangeListener) {
		return modelChangeListeners.remove(modelChangeListener);
	}

	/**
	 * Benachrichtigt die Listener über Änderungen an den Einstellungen.
	 * @see #addModelChangeListener(Runnable)
	 * @see #removeModelChangeListener(Runnable)
	 */
	protected void fireModelChanged() {
		modelChangeListeners.stream().forEach(listener->listener.run());
	}

	/**
	 * Zwischenüberschrift anzeigen
	 * @param panel	Panel in das die Überschrift eingefügt werden soll
	 * @param text	Text der Überschrift
	 * @param spaceBefore	Vertikalen Abstand vor der Überschrift einfügen?
	 */
	protected void addHeading(final JPanel panel, final String text, final boolean spaceBefore) {
		if (spaceBefore) panel.add(Box.createVerticalStrut(15));

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		line.add(new JLabel("<html><body><b>"+text+"</b></body></html>"));
	}

	/**
	 * Auswahlfeld einfügen
	 * @param panel	Panel in das das Auswahlfeld eingefügt werden soll
	 * @param text	Beschriftung des Auswahlfeldes
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param value	Initialer Wert
	 * @return	Neues Auswahlfeld
	 */
	protected SpinnerNumberModel addSpinner(final JPanel panel, final String text, final int min, final int max, final int value) {
		return addSpinner(panel,text,min,max,value,null);
	}

	/**
	 * Auswahlfeld einfügen
	 * @param panel	Panel in das das Auswahlfeld eingefügt werden soll
	 * @param text	Beschriftung des Auswahlfeldes
	 * @param min	Minimaler Wert
	 * @param max	Maximaler Wert
	 * @param value	Initialer Wert
	 * @param info	Optionaler Infotext nach dem Feld (kann <code>null</code> sein)
	 * @return	Neues Auswahlfeld
	 */
	protected SpinnerNumberModel addSpinner(final JPanel panel, final String text, final int min, final int max, final int value, final String info) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JLabel label=new JLabel(text+":");
		line.add(label);
		final JSpinner spinner=new JSpinner(new SpinnerNumberModel(value,min,max,1));
		line.add(spinner);
		label.setLabelFor(spinner);
		if (info!=null) line.add(new JLabel(info));
		spinner.addChangeListener(e->fireModelChanged());
		return (SpinnerNumberModel)spinner.getModel();
	}

	/**
	 * Checkbox einfügen
	 * @param panel	Panel in das die Checkbox eingefügt werden soll
	 * @param text	Beschriftung der Checkbox
	 * @param selected	Soll die Checkbox anfänglich markiert sein?
	 * @return	Neue Checkbox
	 */
	protected JCheckBox addCheckBox(final JPanel panel, final String text, final boolean selected) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JCheckBox checkBox=new JCheckBox(text,selected);
		line.add(checkBox);
		checkBox.addActionListener(e->fireModelChanged());
		return checkBox;
	}

	/**
	 * Combobox einfügen
	 * @param panel	Panel in das die Combobox eingefügt werden soll
	 * @param text	Beschriftung der Combobox
	 * @param options	Auswahloptionen in der Combobox
	 * @return	Neue Combobox
	 */
	protected JComboBox<String> addCombo(final JPanel panel, final String text, String[] options) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(line);
		final JLabel label=new JLabel(text+":");
		line.add(label);
		if (options==null || options.length==0) options=new String[]{""};
		final JComboBox<String> combo=new JComboBox<>(options);
		combo.setSelectedIndex(0);
		line.add(combo);
		label.setLabelFor(combo);
		combo.addActionListener(e->fireModelChanged());
		return combo;
	}

	/**
	 * Erzeugt eine Reihe von Auswahlboxen.
	 * @param panel	Panel in das die Auswahlboxen eingefügt werden sollen
	 * @param options	Namen der Optionen
	 * @param select	Initialer Auswahlstatus der Optionen
	 * @return	Array mit den {@link JRadioButton}-Elementen
	 */
	protected JRadioButton[] addRadioButtons(final JPanel panel, final String[] options, final boolean[] select) {
		final int count=Math.min(options.length,select.length);
		final JRadioButton[] buttons=new JRadioButton[count];
		final ButtonGroup buttonGroup=new ButtonGroup();

		for (int i=0;i<count;i++) {
			final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(line);
			buttons[i]=new JRadioButton(options[i]);
			buttons[i].setSelected(select[i]);
			buttons[i].addActionListener(e->fireModelChanged());
			line.add(buttons[i]);
			buttonGroup.add(buttons[i]);
		}

		return buttons;
	}

	/**
	 * Erzeugt eine Reihe von Auswahlboxen.
	 * @param panel	Panel in das die Auswahlboxen eingefügt werden sollen
	 * @param options	Namen der Optionen
	 * @param selectIndex	Index der initial auszuwähenden Option
	 * @return	Array mit den {@link JRadioButton}-Elementen
	 */
	protected JRadioButton[] addRadioButtons(final JPanel panel, final String[] options, final int selectIndex) {
		final boolean[] select=new boolean[options.length];
		if (selectIndex>=0 && selectIndex<=select.length) select[selectIndex]=true;
		return addRadioButtons(panel,options,select);
	}

	/**
	 * Legt ein neues Modell an.
	 * @param name	Name für das Modell
	 * @param description	Beschreibung für das Modell
	 * @return	Neues Modell
	 */
	protected static EditModel buildModel(final String name, final String description) {
		final EditModel model=new EditModel();
		model.name=name;
		model.description=description;
		return model;
	}

	/**
	 * Erzeugt ein Überschrift-Textelement.
	 * @param model	Modell in das die Überschrift eingefügt werden soll
	 * @param text	Anzuzeigender Text
	 */
	protected static void addHeading(final EditModel model, final String text) {
		final ModelElementText label=new ModelElementText(model,model.surface);
		model.surface.add(label);
		label.setPosition(new Point(50,50));
		label.setText(text);
		label.setTextBold(true);
		label.setTextSize(label.getTextSize()+2);
	}

	/**
	 * Fügt einen kleinen Infotext in das modell ein.
	 * @param model	Modell in das der Infotext eingefügt werden soll
	 * @param text	Anzuzeigender Text
	 * @param xPosition	X-Position auf der Zeichenfläche
	 * @param yPosition	Y-Position auf der Zeichenfläche
	 */
	protected static void addSmallInfo(final EditModel model, final String text, final int xPosition, final int yPosition) {
		final ModelElementText label=new ModelElementText(model,model.surface);
		model.surface.add(label);
		label.setPosition(new Point(xPosition,yPosition));
		label.setTextItalic(true);
		label.setTextSize(label.getTextSize()-2);
		label.setText(text);
	}

	/**
	 * Fügt in dem Modell eine Verbindungskante zwischen zwei Stationen ein
	 * @param model	Modell bei dem die Kante auf der Hauptzeichenfläche eingefügt werden soll
	 * @param station1	Ausgangsstation
	 * @param station2	Zielstation
	 */
	protected static void addEdge(final EditModel model, final ModelElementPosition station1, final ModelElementPosition station2) {
		addEdge(model,station1,station2,false);
	}

	/**
	 * Fügt in dem Modell eine Verbindungskante zwischen zwei Stationen ein
	 * @param model	Modell bei dem die Kante auf der Hauptzeichenfläche eingefügt werden soll
	 * @param station1	Ausgangsstation
	 * @param station2	Zielstation
	 * @param direct	 Soll die Verbindungskante normal (<code>false</code>) oder zwingend als gerade Linie (<code>true</code>) gezeichnet werden?
	 */
	protected static void addEdge(final EditModel model, final ModelElementPosition station1, final ModelElementPosition station2, final boolean direct) {
		final ModelElementEdge edge=new ModelElementEdge(model,model.surface,station1,station2);
		station1.addEdgeOut(edge);
		station2.addEdgeIn(edge);
		if (direct) edge.setLineMode(ModelElementEdge.LineMode.DIRECT);
		model.surface.add(edge);
	}

	/**
	 * Fügt ein Simulationsdatenausgabetextfeld in das Modell ein
	 * @param model	Modell in das das Textfeld eingefügt werden soll
	 * @param xPosition	X-Position auf der Zeichenfläche
	 * @param yPosition	Y-Position auf der Zeichenfläche
	 * @param labelText	Beschriftungstext
	 * @param labelColor	Farbe für den Beschriftungstext
	 * @param dataText	Rechenausdruck dessen Ergebnis angezeigt werden soll
	 */
	protected static void addSimDataText(final EditModel model, final int xPosition, final int yPosition, final String labelText, final Color labelColor, final String dataText) {
		final ModelElementText label;
		model.surface.add(label=new ModelElementText(model,model.surface));
		label.setPosition(new Point(xPosition,yPosition-20));
		label.setText(labelText);
		label.setTextItalic(true);

		final ModelElementAnimationTextValue textValue=new ModelElementAnimationTextValue(model,model.surface);
		textValue.setPosition(new Point(xPosition,yPosition));
		textValue.setTextSize(14);
		textValue.setTextBold(true);
		textValue.setColor(labelColor);
		textValue.setDigits(1);
		textValue.setExpression(dataText);
		model.surface.add(textValue);
	}

	/**
	 * Liefert den Namen des Modelltyps für die Auswahlbox im Dialog.
	 * @return	Name des Modelltyps
	 * @see ModelGeneratorDialog
	 */
	public abstract String getTypeName();

	/**
	 * Liefert das neu erstellte Modell
	 * @return	Neu erstelltes Modell
	 */
	public abstract EditModel getModel();
}
