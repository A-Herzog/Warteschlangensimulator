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
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataListener;

import org.apache.commons.math3.util.FastMath;

import language.Language;
import systemtools.MsgBox;
import tools.JTableExt;
import ui.images.Images;

/**
 * Dieses Panel hält eine Tabelle für die Auswahl von Ressourcn und
 * Ressourcen-Alternativen für ein {@link ModelElementProcess} vor.
 * @author Alexander Herzog
 * @see ModelElementProcess
 * @see ResourceTableModel
 */
public class MultiResourceTable extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 4821242036162357877L;

	/** Zugehörige Bedienstation aus der die Daten geladen werden und in die sie auch wieder zurückgeschrieben werden */
	private final ModelElementProcess process;
	/** Hilfe-Callback */
	private final Runnable helpRunnable;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Wird aufgerufen (sofern ungleich <code>null</code>), wenn sich die definierten Ressourcen verändern */
	private final Runnable statusChanged;

	/** Daten der Bedienergruppen zum Bearbeiten im Dialog */
	private final List<Map<String,Integer>> data;
	/** Datenmodell für {@link #table} */
	private ResourceTableModel model;
	/** Zuletzt ausgewählte Bedienalternative */
	private int lastSelected;

	/**
	 * Auswahlbox für die Bediener-Bedarfs-Alternativen
	 */
	private final JComboBox<String> alternativesList;

	/**
	 * Schaltfläche "Alternative früher prüfen"
	 */
	private final JButton alternativeUp;

	/**
	 * Schaltfläche "Alternative später prüfen"
	 */
	private final JButton alternativeDown;

	/**
	 * Schaltfläche "Alternative hinzufügen"
	 */
	private final JButton alternativeAdd;

	/**
	 * Schaltfläche "Alternative löschen"
	 */
	private final JButton alternativeDelete;

	/**
	 * Info zur Anzeige der Anzahl an verfügbaren Bedien-Alternativen
	 */
	private final JLabel alternativeInfo;

	/**
	 * Tabelle zur Konfiguration der notwendigen
	 * Bediener innerhalb der aktuellen Alternative.
	 */
	private final JTableExt table;

	/**
	 * Konstruktor der Klasse
	 * @param process	Zugehörige Bedienstation aus der die Daten geladen werden und in die sie auch wieder zurückgeschrieben werden
	 * @param helpRunnable	Hilfe-Callback
	 * @param readOnly	Nur-Lese-Status
	 * @param statusChanged	Wird aufgerufen (sofern ungleich <code>null</code>), wenn sich die definierten Ressourcen verändern
	 */
	public MultiResourceTable(final ModelElementProcess process, final Runnable helpRunnable, final boolean readOnly, final Runnable statusChanged) {
		super();

		this.process=process;
		this.helpRunnable=helpRunnable;
		this.readOnly=readOnly;
		this.statusChanged=statusChanged;

		data=new ArrayList<>();
		copyData(process.getNeededResources(),data);

		setLayout(new BorderLayout());

		final JPanel line;
		add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(alternativesList=new JComboBox<>());
		alternativesList.setEditable(false);
		alternativesList.addActionListener(e->selectAlternative());
		line.add(alternativeUp=addButton(e->alternativeMoveUp(),Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Up"),Images.ARROW_UP.getIcon()));
		line.add(alternativeDown=addButton(e->alternativeMoveDown(),Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Down"),Images.ARROW_DOWN.getIcon()));
		line.add(alternativeAdd=addButton(e->alternativeAdd(),Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Add"),Images.MODELPROPERTIES_OPERATORS_ADD.getIcon()));
		line.add(alternativeDelete=addButton(e->alternativeDelete(),Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Delete"),Images.MODELPROPERTIES_OPERATORS_DELETE.getIcon()));
		line.add(Box.createHorizontalStrut(5));
		line.add(alternativeInfo=new JLabel());

		add(new JScrollPane(table=new JTableExt()),BorderLayout.CENTER);

		updateAlternativesList(0);
	}

	/**
	 * Kopiert die Daten aus einer Bediener-Bedarfs-Alternativen-Liste in eine andere
	 * @param source	Ausgangsliste
	 * @param dest	Zielliste (wird vor dem Übertragen geleert)
	 */
	private void copyData(final List<Map<String,Integer>> source, final List<Map<String,Integer>> dest) {
		dest.clear();
		for (Map<String,Integer> map: source) {
			final Map<String,Integer> copy=process.createNewResourceMap();
			copy.putAll(map);
			dest.add(copy);
		}

		if (dest.size()==0) dest.add(process.createNewResourceMap());
	}

	/**
	 * Erzeugt eine Schaltfläche
	 * @param listener	Beim Anklicken auszuführende Aktion
	 * @param tooltip	Tooltip für die Schaltfläche
	 * @param icon	Icon für die Schaltfläche (kann <code>null</code> sein)
	 * @return	Neue Schaltfläche
	 */
	private JButton addButton(final ActionListener listener, final String tooltip, final Icon icon) {
		final JButton button=new JButton("");
		button.addActionListener(listener);
		button.setToolTipText(tooltip);
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	/**
	 * Aktualisiert die Listendarstellung der Bediener-Bedarfs-Alternativen.
	 * @param select	Index des nach der Aktualisierung auszuwählenden Eintrags
	 * @see #alternativesList
	 * @see #lastSelected
	 */
	private void updateAlternativesList(final int select) {
		final int count=data.size();
		final ComboBoxModel<String> model=new ComboBoxModel<String>() {
			private Object sel;
			@Override public int getSize() {return count;}
			@Override public String getElementAt(int index) {return Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative")+" "+(index+1);}
			@Override public void addListDataListener(ListDataListener l) {}
			@Override public void removeListDataListener(ListDataListener l) {}
			@Override public void setSelectedItem(Object anItem) {sel=anItem;}
			@Override public Object getSelectedItem() {return sel;}
		};
		alternativesList.setModel(model);

		if (select>=0) {
			lastSelected=-1;
			alternativesList.setSelectedIndex(select);
		}
	}

	/**
	 * Wird aufgerufen wenn in der Liste der Bediener-Bedarfs-Alternativen
	 * {@link #alternativesList} ein neuer Eintrag ausgewählt wurde
	 * und folglich die Tabellendarstellung aktualisiert werden muss.
	 * @see #alternativesList
	 * @see #lastSelected
	 * @see #data
	 * @see #table
	 */
	private void selectAlternative() {
		if (lastSelected>=0 && model!=null) {
			model.storeData(data.get(lastSelected));
		}

		lastSelected=alternativesList.getSelectedIndex();
		model=null;

		if (lastSelected>=0) {
			table.setModel(model=new ResourceTableModel(table,data.get(lastSelected),process.getModel(),process.getSurface().getResources(),readOnly,helpRunnable));
			table.getColumnModel().getColumn(1).setMaxWidth(275);
			table.getColumnModel().getColumn(1).setMinWidth(275);
			table.setIsPanelCellTable(0);
			table.setIsPanelCellTable(1);
			table.setEnabled(!readOnly);
			if (statusChanged!=null) model.addTableChangeListener(e->statusChanged.run());
		}

		alternativeUp.setEnabled(!readOnly && lastSelected>0);
		alternativeDown.setEnabled(!readOnly && lastSelected<alternativesList.getItemCount()-1);
		alternativeAdd.setEnabled(!readOnly);
		alternativeDelete.setEnabled(!readOnly && alternativesList.getItemCount()>1);

		alternativeInfo.setVisible(data.size()>1);
		alternativeInfo.setText(String.format(Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.CountInfo"),data.size()));

		if (statusChanged!=null) statusChanged.run();
	}

	/**
	 * Befehl: Alternative in der Liste nach oben verschieben
	 */
	private void alternativeMoveUp() {
		selectAlternative();
		final int nr=lastSelected;
		if (nr<1) return;
		final Map<String,Integer> map=data.get(nr);
		data.set(nr,data.get(nr-1));
		data.set(nr-1,map);
		lastSelected=-1;
		alternativesList.setSelectedIndex(nr-1);
		alternativesList.repaint();
	}

	/**
	 * Befehl: Alternative in der Liste nach unten verschieben
	 */
	private void alternativeMoveDown() {
		selectAlternative();
		final int nr=lastSelected;
		if (nr==data.size()-1) return;
		final Map<String,Integer> map=data.get(nr);
		data.set(nr,data.get(nr+1));
		data.set(nr+1,map);
		lastSelected=-1;
		alternativesList.setSelectedIndex(nr+1);
		alternativesList.repaint();
	}

	/**
	 * Befehl: Alternative hinzufügen
	 */
	private void alternativeAdd() {
		selectAlternative();
		data.add(process.createNewResourceMap());
		updateAlternativesList(data.size()-1);
	}

	/**
	 * Befehl: Alternative löschen
	 */
	private void alternativeDelete() {
		if (!MsgBox.confirm(this,Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Delete.Confirm.Title"),Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Delete.Confirm.Info"),Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Delete.Confirm.InfoYes"),Language.tr("Surface.Process.Dialog.Tab.Operators.Alternative.Delete.Confirm.InfoNo"))) return;
		data.remove(lastSelected);
		lastSelected=FastMath.max(0,lastSelected-1);
		updateAlternativesList(lastSelected);
	}

	/**
	 * Wird aufgerufen, um die Daten aus dem Panel in die im
	 * Konstruktor angegebene Bedienstation zurückzuschreiben.
	 */
	public void store() {
		selectAlternative();
		copyData(data,process.getNeededResources());
	}

	/**
	 * Gibt an, ob momentan benötigte Bediener in
	 * diesem Panel definiert sind.
	 * @return	Liefert <code>true</code>, wenn notwendige Bediener angegeben sind
	 */
	public boolean isResourceDefined() {
		for (int i=0;i<data.size();i++) {
			if (i==lastSelected && model!=null) {
				if (model.size()>0) return true;
			} else {
				if (data.get(i).size()>0) return true;
			}
		}
		return false;
	}

	/**
	 * Zeigt einen Dialog zum Anlegen und Hinzufügen einer neuen Bedienergruppe an.
	 * @see ResourceTableModel#addNewGroup()
	 */
	public void addNewGroup() {
		model.addNewGroup();
	}

	/**
	 * Fügt eine bestehende Bedienergruppe zu der Liste der notwendigen Bediener hinzu.
	 * @param name	Name der bestehenden Bedienergruppe
	 * @see ResourceTableModel#addExistingGroup(String)
	 */
	public void addExistingGroup(final String name) {
		model.addExistingGroup(name);
	}
}