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

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Dialog zum Einstellen von verzögerten Ressourcenfreigaben.
 * Wird in der {@link TransportResourceRecord}-Klasse verwendet.
 * @author Alexander Herzog
 * @see TransportResourceRecord
 */
public class TransportResourceRecordDelayDialog extends BaseDialog {
	private static final long serialVersionUID = -8918568202461941741L;

	private final DistributionSystem data;

	private final JCheckBox active;
	private final JComboBox<String> timeBase;
	private final DistributionBySubTypeEditor distributionEditor;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param timeBase	Aktuelle Zeitbasis
	 * @param data	Aktuelle Verzögerungsdaten
	 * @param model	Editor-Modell (für den Expression Builder)
	 * @param surface	Editor-Zeichenfläche  (für den Expression Builder)
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Runnable
	 */
	public TransportResourceRecordDelayDialog(final Component owner, final ModelSurface.TimeBase timeBase, final DistributionSystem data, final EditModel model, final ModelSurface surface, final boolean readOnly, final Runnable help) {
		super(owner,Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Dialog.Title"));

		this.data=data;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JPanel sub,line;
		JLabel label;

		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		/* Aktiv */
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(active=new JCheckBox("<html><body><b>"+Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Dialog.Active")+"</b></body></html>",data.hasData()));
		active.setEnabled(!readOnly);

		/* Zeitbasis */
		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Dialog.TimeBase")+":"));
		line.add(this.timeBase=new JComboBox<>(ModelSurface.getTimeBaseStrings()));
		this.timeBase.setEnabled(!readOnly);
		this.timeBase.setSelectedIndex(timeBase.id);
		label.setLabelFor(this.timeBase);

		/* Verteilungen/Ausdrücke */
		content.add(distributionEditor=new DistributionBySubTypeEditor(model,surface,readOnly,Language.tr("Surface.TransportSource.Dialog.Ressource.DelayedRelease.Dialog.DelayTime"),data,DistributionBySubTypeEditor.Mode.MODE_TRANSPORT_DESTINATION),BorderLayout.CENTER);
		distributionEditor.addUserChangeListener(e->active.setSelected(true));

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,500);
		setSizeRespectingScreensize(600,500);
		setResizable(true);
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	/**
	 * Prüft, ob die Einstellungen in Ordnung sind und gibt im Fehlerfall eine Fehlermeldung aus.
	 * @return	Gibt <code>true</code> zurück, wenn die Einstellungen in Ordnung sind.
	 */
	@Override
	public boolean checkData() {
		return true; /* Es gibt nichts zu prüfen. */
	}

	@Override
	protected void storeData() {
		if (active.isSelected()) distributionEditor.storeData(); else data.set(null);
	}

	/**
	 * Gibt den Wert der gewählten Zeitbasis zurück.<br>
	 * (Die Verzögerungsdaten selbst werden direkt in das dem Konstruktor übergebene Objekt zurückgeschrieben.)
	 * @return	Gewählte Zeitbasis
	 * @see ui.modeleditor.ModelSurface.TimeBase#TIMEBASE_SECONDS
	 * @see ui.modeleditor.ModelSurface.TimeBase#TIMEBASE_MINUTES
	 * @see ui.modeleditor.ModelSurface.TimeBase#TIMEBASE_HOURS
	 */
	public ModelSurface.TimeBase getTimeBase() {
		return ModelSurface.TimeBase.byId(timeBase.getSelectedIndex());
	}
}
