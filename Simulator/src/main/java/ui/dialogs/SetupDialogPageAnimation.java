/**
 * Copyright 2021 Alexander Herzog
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
package ui.dialogs;

import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import language.Language;
import tools.IconListCellRenderer;
import tools.SetupData;
import ui.images.Images;

/**
 * Dialogseite "Animation" im Programmeinstellungen-Dialog
 * @author Alexander Herzog
 * @see SetupData
 */
public class SetupDialogPageAnimation extends SetupDialogPage {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8947820731556954918L;

	/** Wie soll bei Animationen mit der Warm-up-Phase verfahren werden? */
	private final JComboBox<String> animationWarmUpMode;
	/** Lautzeitdaten zu den Stationen anzeigen */
	private final JCheckBox showStationData;
	/** Logging-Daten im Einzelschrittmodus anzeigen */
	private final JCheckBox showSingleStepLogData;
	/** Animation im Einzelschrittmodus starten */
	private final JCheckBox animationStartPaused;
	/** Ressourcen und Transporter in der Animation anzeigen */
	private final JCheckBox animateResources;
	/** Animation verlangsamen um die Veränderung analoger Werte besser abzubilden */
	private final JCheckBox useSlowModeAnimation;

	/**
	 * Konstruktor der Klasse
	 */
	public SetupDialogPageAnimation() {
		JPanel line;
		JLabel label;

		/* Wie soll bei Animationen mit der Warm-up-Phase verfahren werden? */
		line=addLine();
		line.add(label=new JLabel(Language.tr("SettingsDialog.AnimationWarmUp")+":"));
		line.add(animationWarmUpMode=new JComboBox<>(new String[]{
				Language.tr("SettingsDialog.AnimationWarmUp.Normal"),
				Language.tr("SettingsDialog.AnimationWarmUp.Ask"),
				Language.tr("SettingsDialog.AnimationWarmUp.SkipIfNeeded"),
				Language.tr("SettingsDialog.AnimationWarmUp.FastForward")
		}));
		animationWarmUpMode.setRenderer(new IconListCellRenderer(new Images[]{
				Images.SETUP_ANIMATION_START_NORMAL,
				Images.SETUP_ANIMATION_START_ASK,
				Images.SETUP_ANIMATION_START_SKIP,
				Images.SETUP_ANIMATION_START_FAST
		}));
		label.setLabelFor(animationWarmUpMode);

		/* Lautzeitdaten zu den Stationen anzeigen */
		addLine().add(showStationData=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ShowStationData")));

		/* Logging-Daten im Einzelschrittmodus anzeigen */
		addLine().add(showSingleStepLogData=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.ShowSingleStepLogData")));

		/* Animation im Einzelschrittmodus starten */
		addLine().add(animationStartPaused=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.AnimationStartPaused")));

		/* Ressourcen und Transporter in der Animation anzeigen */
		addLine().add(animateResources=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.AnimateResources")));

		/* Animation verlangsamen um die Veränderung analoger Werte besser abzubilden */
		addLine().add(useSlowModeAnimation=new JCheckBox(Language.tr("SettingsDialog.Tabs.Simulation.UseSlowModeAnimation")));
	}

	@Override
	public String getTitle() {
		return Language.tr("SettingsDialog.Tabs.Animation");
	}

	@Override
	public Icon getIcon() {
		return Images.SETUP_PAGE_ANIMATION.getIcon();
	}

	@Override
	public void loadData() {
		switch (setup.animationWarmUpMode) {
		case ANIMATION_WARMUP_NORMAL: animationWarmUpMode.setSelectedIndex(0); break;
		case ANIMATION_WARMUP_ASK: animationWarmUpMode.setSelectedIndex(1); break;
		case ANIMATION_WARMUP_SKIP: animationWarmUpMode.setSelectedIndex(2); break;
		case ANIMATION_WARMUP_FAST: animationWarmUpMode.setSelectedIndex(3); break;
		}
		showStationData.setSelected(setup.showStationRunTimeData);
		showSingleStepLogData.setSelected(setup.showSingleStepLogData);
		animationStartPaused.setSelected(setup.animationStartPaused);
		animateResources.setSelected(setup.animateResources);
		useSlowModeAnimation.setSelected(setup.useSlowModeAnimation);
	}

	@Override
	public void storeData() {
		switch (animationWarmUpMode.getSelectedIndex()) {
		case 0: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_NORMAL; break;
		case 1: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_ASK; break;
		case 2: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_SKIP; break;
		case 3: setup.animationWarmUpMode=SetupData.AnimationMode.ANIMATION_WARMUP_FAST; break;
		}
		setup.showStationRunTimeData=showStationData.isSelected();
		setup.showSingleStepLogData=showSingleStepLogData.isSelected();
		setup.animationStartPaused=animationStartPaused.isSelected();
		setup.animateResources=animateResources.isSelected();
		setup.useSlowModeAnimation=useSlowModeAnimation.isSelected();
	}

	@Override
	public void resetSettings() {
		animationWarmUpMode.setSelectedIndex(2);
		showStationData.setSelected(true);
		showSingleStepLogData.setSelected(true);
		animationStartPaused.setSelected(false);
		animateResources.setSelected(true);
		useSlowModeAnimation.setSelected(true);
	}
}