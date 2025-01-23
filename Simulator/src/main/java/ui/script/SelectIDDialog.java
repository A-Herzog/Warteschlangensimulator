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
package ui.script;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.ModelElementAnimationConnect;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDispose;
import ui.modeleditor.elements.ModelElementDisposeWithTable;
import ui.modeleditor.elements.ModelElementInput;
import ui.modeleditor.elements.ModelElementInputDB;
import ui.modeleditor.elements.ModelElementInputDDE;
import ui.modeleditor.elements.ModelElementInputJS;
import ui.modeleditor.elements.ModelElementOutput;
import ui.modeleditor.elements.ModelElementOutputDB;
import ui.modeleditor.elements.ModelElementOutputDDE;
import ui.modeleditor.elements.ModelElementOutputJS;
import ui.modeleditor.elements.ModelElementOutputLog;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementRecord;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementSourceDB;
import ui.modeleditor.elements.ModelElementSourceDDE;
import ui.modeleditor.elements.ModelElementSourceMulti;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;

/**
 * Dialog zur Auswahl einer Station aus einem Modell
 * @author Alexander Herzog
 * @see ScriptPopupItemCommandID
 */
public class SelectIDDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -3524092476641852821L;

	/**
	 * ID der gewählten Station
	 * @see #getSelectedID()
	 */
	private int selectedID;

	/**
	 * Name der gewählten Station
	 * @see #getSelectedID()
	 */
	private String selectedName;

	/**
	 * Initial zu wählender Eintrag in {@link #combo}
	 * @see #getIDs(EditModel)
	 */
	private int selectedIndex;

	/**
	 * IDs der Stationen in {@link #combo}
	 */
	private int[] ids;

	/**
	 * Namen der Stationen in {@link #combo}
	 */
	private String[] names;

	/**
	 * Namen aller Stationen im Modell
	 */
	private List<String> allNames;

	/**
	 * Auswahlbox zur Auswahl einer Station
	 */
	private final JComboBox<String> combo;

	/**
	 * Option: Stationen wenn möglich über ihre Namen identifizieren?
	 */
	private final JCheckBox byNameCheckBox;

	/**
	 * Soll auch ein leerer Parameter zulässig sein?
	 */
	private final boolean allowEmpty;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param ids	Zuordnung von vorhandenen IDs zu Stationen
	 * @param allNames	Namen aller Stationen im Modell
	 * @param help	Hilfe-Runnable
	 * @param stationRestrictions	Gibt an, ob es sich bei der Zuordnung um eine Liste aller Stationen (<code>false</code>) oder nur um eine bestimmte Auswahl (<code>true</code>) handelt
	 * @param preferProcessStations	Soll wenn möglich in der Liste eine Bedienstation oder Verzögerungsstation initial ausgewählt werden?
	 * @param allowEmpty	Soll auch ein leerer Parameter zulässig sein?
	 * @param allowNames	Soll es zulässig sein, Stationen über ihre Namen zu identifizieren?
	 */
	public SelectIDDialog(final Component owner, final Map<Integer,ModelElementBox> ids, final List<String> allNames, final Runnable help, final boolean stationRestrictions, final boolean preferProcessStations, final boolean allowEmpty, final boolean allowNames) {
		super(owner,Language.tr("ScriptPopup.SelectIDDialog.Title"));
		this.allowEmpty=allowEmpty;
		selectedID=-1;
		selectedIndex=-1;
		this.allNames=allNames;
		if (ids.size()==0) {
			if (stationRestrictions) {
				MsgBox.error(owner,Language.tr("ScriptPopup.SelectIDDialog.ErrorMatchingNoStations.Title"),Language.tr("ScriptPopup.SelectIDDialog.ErrorMatchingNoStations.Info"));
			} else {
				MsgBox.error(owner,Language.tr("ScriptPopup.SelectIDDialog.ErrorNoStations.Title"),Language.tr("ScriptPopup.SelectIDDialog.ErrorNoStations.Info"));
			}
			combo=null;
			byNameCheckBox=null;
			return;
		}

		/* GUI */
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JPanel line;

		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		/* Auswahl der Station */
		setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel label=new JLabel(Language.tr("ScriptPopup.SelectIDDialog.Station")+":");
		line.add(label);
		line.add(combo=new JComboBox<>(getIDNames(ids,preferProcessStations,allowEmpty)));
		if (selectedIndex<0) selectedIndex=0;
		combo.setSelectedIndex(selectedIndex);
		label.setLabelFor(combo);

		/* Wenn möglich über Name adressieren? */
		if (allowNames) {
			setup.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(byNameCheckBox=new JCheckBox(Language.tr("ScriptPopup.SelectIDDialog.byName")));
		} else {
			byNameCheckBox=null;
		}

		setMinSizeRespectingScreensize(500,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell aus dem die Liste der Stationen ausgelesen werden soll
	 * @param help	Hilfe-Runnable
	 * @param stationTypes	Optionale Liste der Klassennamen der Stationen, die in die Auswahl aufgenommen werden sollen (wird hier <code>null</code> oder eine leere Liste übergeben, so erfolgt keine Einschränkung)
	 * @param preferProcessStations	Soll wenn möglich in der Liste eine Bedienstation oder Verzögerungsstation initial ausgewählt werden?
	 * @param allowEmpty	Soll auch ein leerer Parameter zulässig sein?
	 * @param allowNames	Soll es zulässig sein, Stationen über ihre Namen zu identifizieren?
	 */
	public SelectIDDialog(final Component owner, final EditModel model, final Runnable help, final Class<?>[] stationTypes, final boolean preferProcessStations, final boolean allowEmpty, final boolean allowNames) {
		this(owner,getIDs(model,stationTypes),getAllNames(model),help,(stationTypes!=null && stationTypes.length>0),true,allowEmpty,allowNames);
	}

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param model	Editor-Modell aus dem die Liste der Stationen ausgelesen werden soll
	 * @param help	Hilfe-Runnable
	 */
	public SelectIDDialog(final Component owner, final EditModel model, final Runnable help) {
		this(owner,model,help,null,false,false,false);
	}

	/**
	 * Erstellt eine Zuordnung von IDs zu Stationen
	 * @param surface	Zeichenfläche der die Stationen entnommen werden sollen
	 * @param ids	Zuordnung von IDs zu Stationen
	 * @param stationTypes	Zu betrachtende Stationstypen (ist die Liste <code>null</code> oder leer, so werden alle Stationen übernommen)
	 */
	private static void addIDsToMap(final ModelSurface surface, final Map<Integer,ModelElementBox> ids, final Class<?>[] stationTypes) {
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {

			if (element instanceof ModelElementSource) continue;
			if (element instanceof ModelElementSourceDB) continue;
			if (element instanceof ModelElementSourceDDE) continue;
			if (element instanceof ModelElementSourceMulti) continue;
			if (element instanceof ModelElementSourceTable) continue;
			if (element instanceof ModelElementDispose) continue;
			if (element instanceof ModelElementDisposeWithTable) continue;
			if (element instanceof ModelElementInput) continue;
			if (element instanceof ModelElementInputDB) continue;
			if (element instanceof ModelElementInputDDE) continue;
			if (element instanceof ModelElementInputJS) continue;
			if (element instanceof ModelElementOutput) continue;
			if (element instanceof ModelElementOutputDB) continue;
			if (element instanceof ModelElementOutputDDE) continue;
			if (element instanceof ModelElementOutputJS) continue;
			if (element instanceof ModelElementOutputLog) continue;
			if (element instanceof ModelElementRecord) continue;
			if (element instanceof ModelElementAnimationConnect) continue;

			if (stationTypes!=null && stationTypes.length>0) {
				boolean ok=false;
				for (Class<?> cls: stationTypes) if (cls.isInstance(element)) {ok=true; break;}
				if (!ok) continue;
			}

			if (element instanceof ModelElementSub) {
				addIDsToMap(((ModelElementSub)element).getSubSurface(),ids,stationTypes);
				continue;
			}

			ids.put(element.getId(),(ModelElementBox)element);
		}
	}

	/**
	 * Erstellt eine Zuordnung, die alle IDs und die zugehörigen Namen der Stationen eines Modells enthält
	 * @param model	Modell aus dem die Stationen ausgelesen werden sollen
	 * @return	Zuordnung aus IDs und Stationen
	 */
	public static Map<Integer,ModelElementBox> getIDs(final EditModel model) {
		return getIDs(model,null);
	}

	/**
	 * Erstellt eine Zuordnung, die alle IDs und die zugehörigen Namen der Stationen eines Modells enthält
	 * @param model	Modell aus dem die Stationen ausgelesen werden sollen
	 * @param stationTypes	Optionale Liste der Klassennamen der Stationen, die in die Zuordnung aufgenommen werden sollen (wird hier <code>null</code> oder eine leere Liste übergeben, so erfolgt keine Einschränkung)
	 * @return	Zuordnung aus IDs und Stationen
	 */
	public static Map<Integer,ModelElementBox> getIDs(final EditModel model, final Class<?>[] stationTypes) {
		final Map<Integer,ModelElementBox> ids=new HashMap<>();
		addIDsToMap(model.surface,ids,stationTypes);
		return ids;
	}

	/**
	 * Liefert eine Liste der Namen aller Stationen im Modell.
	 * @param model	Modell aus dem die Daten ausgelesen werden sollen
	 * @return Namen aller Stationen im Modell
	 */
	private static List<String> getAllNames(final EditModel model) {
		final List<String> result=new ArrayList<>();
		getAllNames(model.surface,result);
		return result;
	}

	/**
	 * Fügt die Namen der Stationen auf einer Zeichenfläche zu der Gesamtliste der Stationsnamen hinzu.
	 * @param surface	Zeichenfläche deren Stationen betrachtet werden sollen
	 * @param allNames	Liste zu der die Stationsnamen hinzugefügt werden sollen
	 * @see #getAllNames(EditModel)
	 */
	private static void getAllNames(final ModelSurface surface, final List<String> allNames) {
		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementBox) {
			allNames.add(element.getName());
			if (element instanceof ModelElementSub) getAllNames(((ModelElementSub)element).getSubSurface(),allNames);
		}
	}

	/**
	 * Liefert eine Liste mit Stationsnamen (und ihren IDs)
	 * @param ids	Zuordnung von IDs zu Stationselementen
	 * @param preferProcessStations	Bedienstationen an den Anfang stellen
	 * @param allowEmpty	Soll auch ein leerer Parameter zulässig sein?
	 * @return	Liste mit Stationsnamen (und ihren IDs)
	 */
	private String[] getIDNames(final Map<Integer,ModelElementBox> ids, final boolean preferProcessStations, final boolean allowEmpty) {
		this.ids=ids.keySet().stream().mapToInt(i->i).sorted().toArray();
		this.names=IntStream.of(this.ids).mapToObj(id->ids.get(id).getName()).toArray(String[]::new);

		final List<String> names=new ArrayList<>();
		if (allowEmpty) names.add(Language.tr("ScriptPopup.SelectIDDialog.Station.NoID"));
		for (int i=0;i<this.ids.length;i++) {
			final ModelElementBox element=ids.get(this.ids[i]);
			final StringBuilder sb=new StringBuilder();
			sb.append(element.getTypeName());
			if (!element.getName().trim().isEmpty()) sb.append(String.format(" \"%s\"",element.getName()));
			sb.append(String.format(" (id=%d)",element.getId()));
			names.add(sb.toString());

			if (preferProcessStations && selectedIndex<0 && ((element instanceof ModelElementProcess) || (element instanceof ModelElementDelay))) selectedIndex=names.size()-1;
		}

		return names.toArray(new String[0]);
	}

	@Override
	protected void storeData() {
		final int index=combo.getSelectedIndex();
		if (index<0) {
			selectedID=-1;
			selectedName=null;
		} else {
			if (allowEmpty) {
				if (index==0) selectedID=-2; else {
					selectedID=ids[index-1];
					selectedName=names[index-1];
					if (allNames.stream().filter(name->name.equalsIgnoreCase(selectedName)).count()>1) selectedName=null;
				}
			} else {
				selectedID=ids[index];
				selectedName=names[index];
				if (allNames.stream().filter(name->name.equalsIgnoreCase(selectedName)).count()>1) selectedName=null;
			}
		}
	}

	/**
	 * Liefert die ID der gewählten Station.
	 * @return	ID der gewählten Station oder -1, wenn keine Auswahl erfolgt ist
	 */
	public int getSelectedID() {
		return selectedID;
	}

	/**
	 * Liefert den Namen der gewählten Station sofern eine Identifikation über den Namen möglich und gewünscht ist.
	 * @return Name der gewählten Station oder <code>null</code>
	 */
	public String getSelectedName() {
		if (byNameCheckBox!=null && byNameCheckBox.isSelected() && selectedName!=null) return selectedName;
		return null;
	}
}