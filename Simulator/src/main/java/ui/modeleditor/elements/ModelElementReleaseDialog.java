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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
 * Dialog, der Einstellungen für ein {@link ModelElementRelease}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementRelease
 */
public class ModelElementReleaseDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 7121355472159712717L;

	/** Auflistung der Namen aller Ressourcen-Belegen-Stationen */
	private String[] seizes;
	/** Auswahlbox für die Ressourcen-Belegen-Station, die das Gegenstück zu dieser Station darstellen soll */
	private JComboBox<String> seizeSelect;
	/** Option: Ressource erst verzögert freigeben */
	private JCheckBox checkBoxDelayRelease;
	/** Zeiteinheit für {@link #distributionEditor} */
	private JComboBox<String> timeBase;
	/** Verteilungseditor für verzögerte Ressourcenfreigabe */
	private DistributionBySubTypeEditor distributionEditor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementRelease}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementReleaseDialog(final Component owner, final ModelElementRelease element, final boolean readOnly) {
		super(owner,Language.tr("Surface.Release.Dialog.Title"),element,"ModelElementRelease",readOnly);
	}

	@Override
	protected void setDialogSize() {
		setSizeRespectingScreensize(750,600);
	}

	/**
	 * Liefert eine Auflistung der Namen aller Ressourcen-Belegen-Stationen.
	 * @return	Auflistung der Namen aller Ressourcen-Belegen-Stationen
	 * @see #seizes
	 */
	private String[] getSeizes() {
		List<String> names=new ArrayList<>();
		for (ModelElement e: element.getSurface().getElements()) if (e instanceof ModelElementSeize) names.add(e.getName());
		return names.toArray(new String[0]);
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationRelease;
	}

	@Override
	protected JComponent getContentPanel() {
		final JPanel content=new JPanel(new BorderLayout());

		JPanel sub, line;
		JLabel label;

		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Release.Dialog.SeizeElement")+":"));
		seizes=getSeizes();
		line.add(seizeSelect=new JComboBox<>((seizes.length==0)?new String[]{"<"+Language.tr("Surface.Release.Dialog.SeizeElement.NoSeizeAvailable")+">"}:seizes));
		seizeSelect.setEnabled(!readOnly);
		label.setLabelFor(seizeSelect);
		int nr=0;
		for (int i=0;i<seizes.length;i++) if (seizes[i].equals(((ModelElementRelease)element).getSeizeName())) {nr=i; break;}
		seizeSelect.setSelectedIndex(nr);

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(checkBoxDelayRelease=new JCheckBox("<html><body><b>"+Language.tr("Surface.Release.Dialog.UseDelayedRelease")+"</b></body></html>"));
		checkBoxDelayRelease.setEnabled(!readOnly);
		checkBoxDelayRelease.setSelected(((ModelElementRelease)element).getReleaseDelay().get(null)!=null);

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Release.Dialog.TimeBase")+":"));
		line.add(timeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		timeBase.setEnabled(!readOnly);
		timeBase.setSelectedIndex(((ModelElementRelease)element).getTimeBase().id);
		label.setLabelFor(timeBase);

		content.add(distributionEditor=new DistributionBySubTypeEditor(element.getModel(),element.getSurface(),readOnly,Language.tr("Surface.Release.Dialog.DelayedRelease"),((ModelElementRelease)element).getReleaseDelay(),DistributionBySubTypeEditor.Mode.MODE_CLIENTS));
		distributionEditor.addUserChangeListener(e->checkBoxDelayRelease.setSelected(true));

		return content;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();

		final ModelElementRelease release=(ModelElementRelease)element;

		if (seizes.length==0) {
			release.setSeizeName("");
		} else {
			release.setSeizeName(seizes[seizeSelect.getSelectedIndex()]);
		}

		release.setTimeBase(ModelSurface.TimeBase.byId(timeBase.getSelectedIndex()));

		if (checkBoxDelayRelease.isSelected()) distributionEditor.storeData(); else release.getReleaseDelay().set(null);
	}
}
