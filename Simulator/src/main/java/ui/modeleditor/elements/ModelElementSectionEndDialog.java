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

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementSectionEnd}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementSectionEnd
 */
public class ModelElementSectionEndDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7399579014904281396L;

	/**
	 * Auswahlbox für die zu diesem Bereichs-Ende
	 * gehörenden Bereichs-Start.
	 */
	private JComboBox<String> sectionStart;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementSectionEnd}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementSectionEndDialog(final Component owner, final ModelElementSectionEnd element, final boolean readOnly) {
		super(owner,Language.tr("Surface.SectionEnd.Dialog.Title"),element,"ModelElementSectionEnd",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setSizeRespectingScreensize(600,250);
		pack();
		setMaxSizeRespectingScreensize(600,250);
	}

	@Override
	protected void setDialogSizeLater() {
	}

	/**
	 * Liefert eine Liste der Namen der Bereichs-Start-Station.
	 * @return	Liste der Namen der Bereichs-Start-Station
	 */
	private String[] getSectionStartStations() {
		final List<String> list=new ArrayList<>();

		final ModelSurface mainSurface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		for (ModelElement element: mainSurface.getElements()) {
			if (element instanceof ModelElementSectionStart && !element.getName().isBlank() && !list.contains(element.getName())) list.add(element.getName());
			if (element instanceof ModelElementSub) {
				for (ModelElement element2 : ((ModelElementSub)element).getSubSurface().getElements()) {
					if (element2 instanceof ModelElementSectionStart && !element2.getName().isBlank() && !list.contains(element2.getName())) list.add(element2.getName());
				}
			}
		}

		return list.toArray(String[]::new);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationSectionEnd;
	}

	/**
	 * Erstellt und liefert das Panel, welches im Content-Bereich des Dialogs angezeigt werden soll
	 * @return	Panel mit den Dialogelementen
	 */
	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(line);

		final JLabel label=new JLabel(Language.tr("Surface.SectionEnd.Dialog.SectionStart")+":");
		line.add(label);

		final String[] stations=getSectionStartStations();
		line.add(sectionStart=new JComboBox<>(stations));
		label.setLabelFor(sectionStart);
		sectionStart.setEnabled(!readOnly);

		int index=-1;
		for (int i=0;i<stations.length;i++) if (stations[i].equalsIgnoreCase(((ModelElementSectionEnd)element).getSectionStartName())) {index=i; break;}
		if (index<0 && stations.length>0) index=0;
		sectionStart.setSelectedIndex(index);

		return content;
	}

	@Override
	protected void storeData() {
		super.storeData();

		final int index=sectionStart.getSelectedIndex();
		if (index>=0) ((ModelElementSectionEnd)element).setSectionStartName(getSectionStartStations()[index]);
	}
}
