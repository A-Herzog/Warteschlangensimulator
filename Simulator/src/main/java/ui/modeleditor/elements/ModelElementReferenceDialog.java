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
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.collections4.map.HashedMap;

import language.Language;
import systemtools.MsgBox;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementReference}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementReference
 */
public class ModelElementReferenceDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6987289177662484589L;

	private int[] ids;
	private String[] names;
	private JComboBox<String> combo;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementReference}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementReferenceDialog(final Component owner, final ModelElementReference element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Reference.Dialog.Title"),element,"ModelElementReference",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(600,0);
		pack();
	}

	private String getName(final ModelElementBox element) {
		return String.format(Language.tr("Surface.Reference.Dialog.Name"),element.getTypeName(),element.getName(),element.getId());
	}

	private void buildList() {
		/* Daten zusammenstellen */
		final Map<Integer,String> map=new HashedMap<>();
		for (ModelElement element1: element.getModel().surface.getElements()) {
			if (ModelElementReference.canUseForReference(element1)) map.put(element1.getId(),getName((ModelElementBox)element1));
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (ModelElementReference.canUseForReference(element2)) map.put(element2.getId(),getName((ModelElementBox)element2));
			}
		}

		/* IDs sortieren */
		ids=map.keySet().stream().mapToInt(I->I.intValue()).sorted().toArray();
		names=new String[ids.length];

		/* Namen */
		for (int i=0;i<ids.length;i++) names[i]=map.get(ids[i]);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationReference;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		buildList();

		final JPanel content=new JPanel(new BorderLayout());

		JPanel sub;
		JLabel label;

		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Reference.Dialog.Element")+":"));
		sub.add(combo=new JComboBox<>(names));
		label.setLabelFor(combo);
		combo.setEnabled(!readOnly);

		if (element instanceof ModelElementReference) {
			final ModelElementReference reference=(ModelElementReference)element;
			int id=-1;
			final ModelElement element=reference.getReferenceElement();
			if (element!=null) id=element.getId();
			int index=-1;
			if (id>=0) for (int i=0;i<ids.length;i++) if (ids[i]==id) {index=i; break;}
			if (index<0 && ids.length>0) index=0;
			if (index>=0) combo.setSelectedIndex(index);
		}

		return content;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (combo.getSelectedIndex()<0) {
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("Surface.Reference.Dialog.Element.ErrorTitle"),Language.tr("Surface.Reference.Dialog.Element.ErrorInfo"));
				return false;
			}
		}
		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		if ((element instanceof ModelElementReference) && combo.getSelectedIndex()>=0) {
			final ModelElementReference reference=(ModelElementReference)element;
			reference.setReferenceElement(ids[combo.getSelectedIndex()]);
		}
	}
}
