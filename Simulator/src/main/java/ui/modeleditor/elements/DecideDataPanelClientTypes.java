/**
 * Copyright 2022 Alexander Herzog
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
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dieses Panel ermöglicht die Verzweigung nach Kundentypen.
 * @author Alexander Herzog
 * @see DecideDataPanel
 */
public class DecideDataPanelClientTypes extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6144372310393417333L;

	/**
	 * HTML-Vorspann zum Anzeigen der Ziele als fette Texte
	 */
	private static String HTML1="<html><body><b>";

	/**
	 * HTML-Abspann zum Anzeigen der Ziele als fette Texte
	 */
	private static String HTML2="</b></body></html>";

	/**
	 * Liste der Beschriftungslabels für die verschiedenen Ausgänge
	 * @see #getLabels()
	 */
	private final List<JLabel> labels;

	/**
	 * Liste mit allen im Modell vorhandenen Kundentypennamen
	 */
	private final List<String> allClientTypesList;

	/**
	 * Liste der Namen der Ziele
	 */
	public final List<String> destinations;

	/**
	 * Gesamtes Modell (zur Ermittlung der Icons für die Kundentypen)
	 */
	private final EditModel model;

	/**
	 * Nur-Lese-Status
	 */
	private final boolean readOnly;

	/**
	 * Callback, welches aufgerufen wird, wenn das Element neu aufgebaut werden muss
	 */
	private final Runnable needUpdate;

	/**
	 * Auswahlboxen für die Kundentypen
	 */
	private final List<List<JComboBox<?>>> clientTypeCombos;

	/**
	 * Konstruktor der Klasse
	 * @param element	Element (zum Auslesen der Kundentypennamen und der Icons)
	 * @param destinations	Liste der Namen der Ziele
	 * @param readOnly	Nur-Lese-Status
	 * @param needUpdate	Callback, welches aufgerufen wird, wenn das Element neu aufgebaut werden muss
	 */
	public DecideDataPanelClientTypes(final ModelElement element, final List<String> destinations, final boolean readOnly, final Runnable needUpdate) {
		allClientTypesList=element.getSurface().getClientTypes();
		model=element.getModel();

		labels=new ArrayList<>();
		this.destinations=destinations;
		clientTypeCombos=new ArrayList<>();
		for (int i=0;i<destinations.size();i++) clientTypeCombos.add(new ArrayList<>());

		this.readOnly=readOnly;

		this.needUpdate=needUpdate;

		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
	}

	/**
	 * Fügt einen Abschnitt für eine auslaufende Kante hinzu.
	 * @param title	Name des Abschnitts
	 * @return	Neuer Abschnitt
	 */
	private JPanel addSection(final String title) {
		final JPanel section=new JPanel(new BorderLayout());
		add(section);

		final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
		section.add(labelPanel,BorderLayout.NORTH);
		final JLabel label=new JLabel(HTML1+title+HTML2);
		labels.add(label);
		labelPanel.add(label);

		final JPanel lines=new JPanel();
		lines.setLayout(new BoxLayout(lines,BoxLayout.PAGE_AXIS));
		section.add(lines,BorderLayout.CENTER);

		return lines;
	}

	/**
	 * Erzeugt eine Auswahlbox für einen Kundentyp
	 * @param clientType	Bisheriger Kundentyp (wird <code>null</code> übergeben, so wird eine Box ohne Auswahlmöglichkeiten erzeugt)
	 * @return	Neue Auswahlbox
	 */
	private JComboBox<String> buildCombo(final String clientType) {
		final String[] items;
		if (clientType==null) {
			items=new String[]{Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.Else")};
		} else {
			items=allClientTypesList.toArray(new String[0]);
		}
		final JComboBox<String> input=new JComboBox<>(items);
		if (clientType==null) {
			input.setEnabled(false);
			input.setSelectedIndex(0);
		} else {
			input.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildClientTypeIcons(items,model)));
			input.setEnabled(!readOnly);
			final int index=allClientTypesList.indexOf(clientType);
			if (index>=0) input.setSelectedIndex(index); else {
				if (allClientTypesList.size()>0) input.setSelectedIndex(0);
			}
		}
		return input;
	}

	/**
	 * Erzeugt eine Zeile mit einer Auswahlbox
	 * @param parent	Übergeordnetes Element an das die Zeile angefügt werden soll
	 * @param clientType	Bisheriger Kundentyp (wird <code>null</code> übergeben, so wird eine Box ohne Auswahlmöglichkeiten erzeugt)
	 * @return	2-elementiges Array aus Zeile (<code>JPanel</code>) und Auswahlbox (<code>JComboBox&lt;String&gt;</code>)
	 */
	private Object[] buildComboLine(final JPanel parent, final String clientType) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		parent.add(line);

		final JLabel label=new JLabel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType")+":");
		line.add(label);

		final JComboBox<String> combo=buildCombo(clientType);
		line.add(combo);
		label.setLabelFor(combo);

		return new Object[] {line,combo};
	}

	/**
	 * Fügt Bearbeiten-Schaltflächen an eine Zeile an.
	 * @param parent	Zeile an die die Schaltflächen angefügt werden sollen
	 * @param destination	Index der auslaufenden Kante
	 * @param allowRemove	Soll nur eine Hinzufügen- (<code>false</code>) oder auch eine Löschen- (<code>true</code>) Schaltfläche hinzugefügt werden?
	 */
	private void addButtons(final JPanel parent, final int destination, final boolean allowRemove) {
		if (needUpdate==null || readOnly) return;
		final JButton buttonAdd=new JButton(Images.EDIT_ADD.getIcon());
		buttonAdd.setToolTipText(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.Add"));
		parent.add(buttonAdd);
		buttonAdd.addActionListener(e->{
			clientTypeCombos.get(destination).add(buildCombo(""));
			needUpdate.run();
		});

		if (allowRemove) {
			final JButton buttonRemove=new JButton(Images.EDIT_DELETE.getIcon());
			buttonRemove.setToolTipText(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.Remove"));
			parent.add(buttonRemove);
			buttonRemove.addActionListener(e->{
				final List<JComboBox<?>> combos=clientTypeCombos.get(destination);
				combos.remove(combos.size()-1);
				needUpdate.run();
			});
		}
	}

	/**
	 * Lädt die Daten in das Panel.
	 * @param clientTypes	Kundentypen für die verschiedenen Richtungen
	 */
	public void loadData(final List<List<String>> clientTypes) {
		for (int i=0;i<destinations.size();i++) {
			final JPanel section=addSection(destinations.get(i));
			if (i==destinations.size()-1) {
				buildComboLine(section,null);
			} else {
				final int size=(clientTypes.size()>i)?Math.max(1,clientTypes.get(i).size()):1;
				for (int j=0;j<size;j++) {
					final String clientType=(i>=clientTypes.size() || j>=clientTypes.get(i).size())?"":clientTypes.get(i).get(j);
					final Object[] obj=buildComboLine(section,clientType);
					final JPanel line=(JPanel)obj[0];
					clientTypeCombos.get(i).add((JComboBox<?>)obj[1]);
					if (j==size-1) addButtons(line,i,size>1);
				}
			}
		}
	}

	/**
	 * Lädt die Daten in das Panel.
	 * @param oldPanel	Ausgangspanel dem die Daten entnommen werden sollen
	 */
	public void loadData(final DecideDataPanelClientTypes oldPanel) {
		labels.clear();
		for (int i=0;i<destinations.size();i++) {
			final JPanel section=addSection(destinations.get(i));
			if (i==destinations.size()-1) {
				buildComboLine(section,null);
			} else {
				final int size=(oldPanel.clientTypeCombos.size()>i)?Math.max(1,oldPanel.clientTypeCombos.get(i).size()):1;
				for (int j=0;j<size;j++) {
					final String clientType=(i>=oldPanel.clientTypeCombos.size() || j>=oldPanel.clientTypeCombos.get(i).size())?"":(String)oldPanel.clientTypeCombos.get(i).get(j).getSelectedItem();
					final Object[] obj=buildComboLine(section,clientType);
					final JPanel line=(JPanel)obj[0];
					clientTypeCombos.get(i).add((JComboBox<?>)obj[1]);
					if (j==size-1) addButtons(line,i,size>1);
				}

			}
		}
	}

	/**
	 * Prüft die Einstellungen.
	 * @return	Liefert im Erfolgsfall <code>true</code> (im Fehlerfall wird außerdem eine Fehlermeldung ausgegeben)
	 */
	public boolean checkClientTypes() {
		final List<String> usedClientTypes=new ArrayList<>();

		for (int i=0;i<clientTypeCombos.size()-1;i++) for (int j=0;j<clientTypeCombos.get(i).size();j++) {
			final JComboBox<?> comboBox=clientTypeCombos.get(i).get(j);
			if (comboBox.getSelectedIndex()<0) {
				MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorMissing.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorMissing.Info"),i+1));
				return false;
			}
			final String name=(String)comboBox.getSelectedItem();
			if (usedClientTypes.indexOf(name)>=0) {
				MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorDouble.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.ClientType.ErrorDouble.Info"),i+1,name));
				return false;
			}
			usedClientTypes.add(name);
		}

		return true;
	}

	/**
	 * Liefert die Einstellungen aus dem Panel.
	 * @return	Namen der Kundentypen pro auslaufender Kante
	 */
	public List<List<String>> getClientTypes() {
		final List<List<String>> result=new ArrayList<>();
		for (int i=0;i<clientTypeCombos.size()-1;i++) {
			final List<String> list=new ArrayList<>();
			for (int j=0;j<clientTypeCombos.get(i).size();j++) list.add((String)clientTypeCombos.get(i).get(j).getSelectedItem());
			result.add(list);
		}
		return result;
	}

	/**
	 * Liefert die Liste der Beschriftungslabels für die verschiedenen Ausgänge.
	 * @return	Liste der Beschriftungslabels für die verschiedenen Ausgänge
	 */
	public List<JLabel> getLabels() {
		return labels;
	}
}
