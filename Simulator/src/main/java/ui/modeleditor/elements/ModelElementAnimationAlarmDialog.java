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

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JComponent;

import language.Language;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.tools.SoundSystemPanel;

/**
 * Dialog, der Einstellungen für ein {@link ModelElementAnimationAlarm}-Element anbietet
 * @author Alexander Herzog
 * @see ModelElementAnimationAlarm
 */
public class ModelElementAnimationAlarmDialog extends ModelElementBaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8756803822304491805L;

	/**
	 * Panel in dem die eigentliche Konfiguration erfolgt
	 */
	private SoundSystemPanel soundPanel;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Fenster
	 * @param element	Zu bearbeitendes {@link ModelElementAnalogValue}
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfläche deaktiviert
	 */
	public ModelElementAnimationAlarmDialog(final Component owner, final ModelElementAnimationAlarm element, final boolean readOnly) {
		super(owner,Language.tr("Surface.AnimationAlarm.Dialog.Title"),element,"ModelElementAnimationAlarm",readOnly);
	}

	/**
	 * Stellt die Größe des Dialogfensters ein.
	 */
	@Override
	protected void setDialogSize() {
		pack();
		final int h=getSize().height;
		setMinSizeRespectingScreensize(800,h);
		pack();
	}

	@Override
	protected String getInfoPanelID() {
		return InfoPanel.stationAnimationAlarm;
	}

	@Override
	protected JComponent getContentPanel() {
		final ModelElementAnimationAlarm alarm=(ModelElementAnimationAlarm)element;
		soundPanel=new SoundSystemPanel(alarm.getSound(),alarm.getSoundMaxSeconds(),readOnly);
		return soundPanel;
	}

	/**
	 * Speichert die Dialog-Daten in dem zugehörigen Daten-Objekt.<br>
	 * (Diese Routine wird beim Klicken auf "Ok" nach <code>checkData</code> aufgerufen.
	 * @see #checkData()
	 */
	@Override
	protected void storeData() {
		super.storeData();
		final ModelElementAnimationAlarm alarm=(ModelElementAnimationAlarm)element;
		alarm.setSound(soundPanel.getSound(),soundPanel.getMaxSeconds());
	}
}
