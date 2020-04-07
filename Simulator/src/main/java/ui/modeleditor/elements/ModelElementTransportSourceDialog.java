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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import language.Language;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementTransportSource}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementTransportSource
 */
public class ModelElementTransportSourceDialog extends ModelElementBaseDialog {
	private static final long serialVersionUID = 3463014644258916438L;

	private TransportTimeRecordPanel transportTimeRecordPanel;
	private TransportTargetSystemPanel transportTargetSystemPanel;
	private TransportResourceRecordPanel transportResourceRecordPanel;

	private JCheckBox useSectionStart;
	private JComboBox<String> sectionStart;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementTransportSource}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementTransportSourceDialog(final Component owner, final ModelElementTransportSource element, final boolean readOnly) {
		super(owner,Language.tr("Surface.TransportSource.Dialog.Title"),element,"ModelElementTransportSource",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		setMinSizeRespectingScreensize(750,650);
		pack();
		setResizable(true);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationTransportSource;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementTransportSource source=(ModelElementTransportSource)element;

		final JTabbedPane tabs=new JTabbedPane();
		JPanel tab, sub;

		/* Tab "Transportzeit" */
		tabs.addTab(Language.tr("Surface.TransportSource.Dialog.Tab.TransportTimes"),tab=new JPanel(new BorderLayout()));
		tab.add(transportTimeRecordPanel=new TransportTimeRecordPanel(source.getTransportTimeRecord(),readOnly,source.getModel(),source.getSurface()),BorderLayout.CENTER);

		/* Tab "Routing-Ziele" */
		tabs.addTab(Language.tr("Surface.TransportSource.Dialog.Tab.RoutingTargets"),tab=new JPanel(new BorderLayout()));
		tab.add(transportTargetSystemPanel=new TransportTargetSystemPanel(source.getTransportTargetSystem(),readOnly,source.getModel(),source.getSurface(),helpRunnable));

		/* Tab "Ressource" */
		tabs.addTab(Language.tr("Surface.TransportSource.Dialog.Tab.Ressource"),tab=new JPanel(new BorderLayout()));
		tab.add(transportResourceRecordPanel=new TransportResourceRecordPanel(source.getTransportResourceRecord(),readOnly,source.getModel(),source.getSurface(),helpRunnable),BorderLayout.CENTER);
		final JButton resourceButton=getOpenModelOperatorsButton();
		if (resourceButton!=null) {
			tab.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
			sub.add(resourceButton);
		}

		/* Tab "Bereich" */
		tabs.addTab(Language.tr("Surface.TransportSource.Dialog.Tab.SectionEnd"),tab=new JPanel(new BorderLayout()));
		buildSectionsTab(tab);

		/* System vorbereiten */
		tabs.setIconAt(0,Images.MODE_DISTRIBUTION.getIcon());
		tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_TRANSPORT_TARGET.getIcon());
		tabs.setIconAt(2,Images.MODELPROPERTIES_OPERATORS.getIcon());
		tabs.setIconAt(3,Images.MODELEDITOR_ELEMENT_TRANSPORT_BATCH.getIcon());

		return tabs;
	}

	private void buildSectionsTab(final JPanel tab) {
		final JPanel content=new JPanel();
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		tab.add(content,BorderLayout.NORTH);

		JPanel line;

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(useSectionStart=new JCheckBox(Language.tr("Surface.TransportSource.Dialog.SectionEnd.Use")));

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel label=new JLabel(Language.tr("Surface.TransportSource.Dialog.SectionEnd.SectionStart")+":");
		line.add(label);

		final String[] stations=getSectionStartStations();
		line.add(sectionStart=new JComboBox<>(stations));
		label.setLabelFor(sectionStart);
		sectionStart.setEnabled(!readOnly);

		int index=-1;
		final String s=((ModelElementTransportSource)element).getSectionStartName();
		if (!s.isEmpty()) for (int i=0;i<stations.length;i++) if (stations[i].equalsIgnoreCase(s)) {index=i; break;}
		if (index<0) {
			useSectionStart.setSelected(false);
			if (stations.length>0) sectionStart.setSelectedIndex(0);
		} else {
			useSectionStart.setSelected(true);
			sectionStart.setSelectedIndex(index);
		}
		useSectionStart.setEnabled(!readOnly && stations.length>0);

		sectionStart.addActionListener(e->useSectionStart.setSelected(true));
	}

	private String[] getSectionStartStations() {
		final List<String> list=new ArrayList<>();

		final ModelSurface mainSurface=(element.getSurface().getParentSurface()==null)?element.getSurface():element.getSurface().getParentSurface();
		for (ModelElement element: mainSurface.getElements()) {
			if (element instanceof ModelElementSectionStart && !element.getName().trim().isEmpty() && !list.contains(element.getName())) list.add(element.getName());
			if (element instanceof ModelElementSub) {
				for (ModelElement element2 : ((ModelElementSub)element).getSubSurface().getElements()) {
					if (element2 instanceof ModelElementSectionStart && !element2.getName().trim().isEmpty() && !list.contains(element2.getName())) list.add(element2.getName());
				}
			}
		}

		return list.toArray(new String[0]);
	}

	@Override
	protected boolean checkData() {
		if (!transportTimeRecordPanel.checkData()) return false;
		if (!transportTargetSystemPanel.checkData()) return false;
		if (!transportResourceRecordPanel.checkData()) return false;

		return true;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		transportTimeRecordPanel.storeData();
		transportTargetSystemPanel.storeData();
		transportResourceRecordPanel.storeData();

		if (!useSectionStart.isSelected()) {
			((ModelElementTransportSource)element).setSectionStartName("");
		} else {
			if (sectionStart.getSelectedIndex()<0) {
				((ModelElementTransportSource)element).setSectionStartName("");
			} else {
				((ModelElementTransportSource)element).setSectionStartName((String)sectionStart.getSelectedItem());
			}
		}
	}
}
