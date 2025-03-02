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
package ui.tools;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Konfigurations-Panel zur Auswahl und Konfiguration eines auszugebenden Sounds.
 * @author Alexander Herzog
 * @see SoundSystem
 */
public class SoundSystemPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-6396322067170282522L;

	/**
	 * Referenz auf das {@link SoundSystem}-Singleton
	 */
	private final SoundSystem soundSystem;

	/**
	 * Existieren auf dem System Windows-Sounds
	 * @see SoundSystem#getSoundFiles()
	 */
	private final boolean hasWindowsSounds;

	/**
	 * Auswahlbox für den Modus (Systemereignis-Sounds, Windows-Sounds, Sound-Dateien)
	 */
	private final JComboBox<String> modeSelect;

	/**
	 * Cards-Bereich der die Eingabe- und Auswahlfelder für die konkrete Konfiguration enthält
	 */
	private final JPanel cards;

	/**
	 * Layout für {@link #cards}
	 * @see #cards
	 */
	private final CardLayout cardLayout;

	/**
	 * Auswahlfeld für einen Systemereignis-Sound
	 */
	private final JComboBox<String> systemEventSoundSelect;

	/**
	 * Auswahlfeld für einen Windows-Sound
	 */
	private final JComboBox<String> systemSoundFileSelect;

	/**
	 * Eingabefeld zur Definition einer abzuspielenden Datei
	 */
	private final JTextField soundFileEdit;

	/**
	 * Auswahlfeld für die maximale  Abspieldauer
	 */
	private final SpinnerModel soundFileMaxSeconds;

	/**
	 * Auswahlfeld für einen Ton
	 */
	private final JComboBox<String> toneSelect;

	/**
	 * Eingabefeld für eine Frequenz
	 */
	private final JTextField frequencyEdit;

	/**
	 * Konstruktor der Klasse
	 * @param oldSound	Bisheriger Sound (kann leer oder <code>null</code> sein)
	 * @param maxSeconds	Bei Sound-Dateien maximal abzuspielende Sekunden (oder ein Wert &le;0 für keine Einschränkung)
	 * @param readOnly	Nur-Lese-Status
	 */
	public SoundSystemPanel(String oldSound, final int maxSeconds, final boolean readOnly) {
		soundSystem=SoundSystem.getInstance();
		hasWindowsSounds=soundSystem.getSoundFiles().length>0;

		JPanel line, subArea;
		JButton button;
		List<Images> icons;

		setLayout(new BorderLayout());

		/* Konfigurationsbereich */
		final JPanel main=new JPanel();
		main.setLayout(new BoxLayout(main,BoxLayout.PAGE_AXIS));
		add(main,BorderLayout.CENTER);

		/* Auswahl des Arts des Sounds */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		main.add(line);
		final List<String> modes=new ArrayList<>();
		final List<Images> modesImages=new ArrayList<>();

		modes.add(Language.tr("SoundSelectPanel.ModeSystem"));
		modesImages.add(Images.SOUND_EVENT);

		if (hasWindowsSounds) {
			modes.add(Language.tr("SoundSelectPanel.ModeWindows"));
			modesImages.add(Images.SOUND_WINDOWS);
		}

		modes.add(Language.tr("SoundSelectPanel.ModeFile"));
		modesImages.add(Images.SOUND_FILE);

		modes.add(Language.tr("SoundSelectPanel.Tone"));
		modesImages.add(Images.SOUND);

		modes.add(Language.tr("SoundSelectPanel.Frequency"));
		modesImages.add(Images.SOUND);

		line.add(modeSelect=new JComboBox<>(modes.toArray(String[]::new)));
		modeSelect.setEnabled(!readOnly);
		modeSelect.setSelectedIndex(0);
		modeSelect.setRenderer(new IconListCellRenderer(modesImages.toArray(Images[]::new)));

		/* Cards */
		cards=new JPanel(cardLayout=new CardLayout());
		main.add(cards);

		/* Card: Systemereignis-Sounds */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		cards.add(line,"1");
		line.add(systemEventSoundSelect=new JComboBox<>(soundSystem.getSystemSounds()));
		icons=new ArrayList<>();
		for (int i=0;i<systemEventSoundSelect.getItemCount();i++) icons.add(Images.SOUND);
		systemEventSoundSelect.setRenderer(new IconListCellRenderer(icons.toArray(Images[]::new)));
		systemEventSoundSelect.setEnabled(!readOnly);
		systemEventSoundSelect.setSelectedIndex(0);

		/* Card: Windows-Sounds */
		if (hasWindowsSounds) {
			line=new JPanel(new FlowLayout(FlowLayout.LEFT));
			cards.add(line,"2");
			line.add(systemSoundFileSelect=new JComboBox<>(Stream.of(soundSystem.getSoundFiles()).map(file->file.toString()).toArray(String[]::new)));
			icons=new ArrayList<>();
			for (int i=0;i<systemSoundFileSelect.getItemCount();i++) icons.add(Images.SOUND);
			systemSoundFileSelect.setRenderer(new IconListCellRenderer(icons.toArray(Images[]::new)));
			systemSoundFileSelect.setEnabled(!readOnly);
			systemSoundFileSelect.setSelectedIndex(0);
		} else {
			systemSoundFileSelect=null;
		}

		/* Card: Sound-Dateien */
		line=new JPanel(new BorderLayout());
		cards.add(line,hasWindowsSounds?"3":"2");
		Box box=Box.createVerticalBox();
		box.add(Box.createVerticalGlue());
		box.add(soundFileEdit=new JTextField(""));
		ModelElementBaseDialog.addUndoFeature(soundFileEdit);
		box.add(Box.createVerticalGlue());
		soundFileEdit.setMaximumSize(new Dimension(soundFileEdit.getMaximumSize().width,soundFileEdit.getPreferredSize().height));
		line.add(box,BorderLayout.CENTER);
		soundFileEdit.setEnabled(!readOnly);
		line.add(subArea=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.EAST);
		subArea.add(button=new JButton(Images.GENERAL_SELECT_FILE.getIcon()));
		button.setEnabled(!readOnly);
		button.setToolTipText(Language.tr("SoundSelectDialog.Title"));
		button.addActionListener(e->selectFile());
		subArea.add(Box.createHorizontalStrut(5));
		final JLabel label=new JLabel(Language.tr("SoundSelectPanel.MaxSeconds")+":");
		subArea.add(label);
		final JSpinner spinner=new JSpinner(soundFileMaxSeconds=new SpinnerNumberModel(1,1,99,1));
		final JSpinner.NumberEditor editor=new JSpinner.NumberEditor(spinner);
		editor.getFormat().setGroupingUsed(false);
		editor.getTextField().setColumns(3);
		spinner.setEditor(editor);
		editor.setEnabled(!readOnly);
		spinner.setEnabled(!readOnly);
		soundFileMaxSeconds.setValue(10);
		subArea.add(spinner);

		/* Card: Ton */
		line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		cards.add(line,hasWindowsSounds?"4":"3");
		final List<String> toneList=new ArrayList<>();
		for (int i=0;i<SoundSystem.fullToneNameList.size();i++) toneList.add(SoundSystem.fullToneNameList.get(i)+" ("+NumberTools.formatNumber(SoundSystem.fullToneFrequencyList.get(i),2)+Language.tr("SoundSelectPanel.Hz")+")");
		line.add(toneSelect=new JComboBox<>(toneList.toArray(String[]::new)));
		icons=new ArrayList<>();
		for (int i=0;i<toneSelect.getItemCount();i++) icons.add(Images.SOUND);
		toneSelect.setRenderer(new IconListCellRenderer(icons.toArray(Images[]::new)));
		toneSelect.setEnabled(!readOnly);
		toneSelect.setSelectedIndex(57);

		/* Card: Frequenz */
		final Object[] data=ModelElementBaseDialog.getInputPanel(Language.tr("SoundSelectPanel.Frequency")+":","440",10);
		cards.add((JPanel)data[0],hasWindowsSounds?"5":"4");
		((JPanel)data[0]).add(new JLabel(Language.tr("SoundSelectPanel.Hz")));
		frequencyEdit=(JTextField)data[1];
		frequencyEdit.setEnabled(!readOnly);
		frequencyEdit.addKeyListener(new KeyAdapter() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		/* Daten laden */
		if (oldSound==null || oldSound.trim().isEmpty()) oldSound=SoundSystem.BEEP_SOUND;
		setSound(oldSound,maxSeconds);

		modeChanged();
		modeSelect.addActionListener(e->modeChanged());

		/* Abspiel-Schaltfläche */
		add(line=new JPanel(new FlowLayout(FlowLayout.CENTER)),BorderLayout.EAST);
		line.add(button=new JButton(Language.tr("SoundSelectPanel.Play")));
		button.setIcon(Images.ANIMATION_PLAY.getIcon());
		button.setToolTipText(Language.tr("SoundSelectPanel.Play.Info"));
		button.addActionListener(e->play(getSound()));
		final JButton playButton=button;

		/* Größen der Abspiel-Schaltfläche korrigieren */
		SwingUtilities.invokeLater(()->{
			final int w=playButton.getSize().width;
			final Dimension parentSize=playButton.getParent().getSize();
			final int wOuter=parentSize.width;
			final int hOuter=parentSize.height;
			final int delta=wOuter-w;
			playButton.setSize(new Dimension(w,hOuter-delta));
			playButton.setPreferredSize(new Dimension(w,hOuter-delta));
		});
	}

	/**
	 * Stellt den in der GUI anzuzeigenden Sound ein.
	 * @param sound	Bisheriger Sound
	 * @param maxSeconds	Bei Sound-Dateien maximal abzuspielende Sekunden (oder ein Wert &le;0 für keine Einschränkung)
	 */
	private void setSound(final String sound, final int maxSeconds) {
		int index;

		/* Systemereignis-Sounds */
		final List<String> systemSounds=Arrays.asList(soundSystem.getSystemSounds());
		index=systemSounds.indexOf(sound);
		if (index>=0) {
			modeSelect.setSelectedIndex(0);
			systemEventSoundSelect.setSelectedIndex(index);
			return;
		}

		/* Windows-Sounds */
		final List<String> systemFiles=Stream.of(soundSystem.getSoundFiles()).map(file->file.toString()).collect(Collectors.toList());
		index=systemFiles.indexOf(sound);
		if (index>=0) {
			modeSelect.setSelectedIndex(1);
			systemSoundFileSelect.setSelectedIndex(index);
			return;
		}

		/* Ton */
		if (SoundSystem.fullToneNameSet.contains(sound)) {
			modeSelect.setSelectedIndex(hasWindowsSounds?3:2);
			toneSelect.setSelectedIndex(SoundSystem.fullToneNameList.indexOf(sound));
			return;
		}

		/* Frequenz */
		final Double D=NumberTools.getPositiveDouble(sound);
		if (D!=null) {
			modeSelect.setSelectedIndex(hasWindowsSounds?4:3);
			frequencyEdit.setText(sound);
			return;
		}

		/* Sound-Dateien */
		modeSelect.setSelectedIndex(hasWindowsSounds?2:1);
		soundFileEdit.setText(sound);
		if (maxSeconds<=0) soundFileMaxSeconds.setValue(999); else soundFileMaxSeconds.setValue(maxSeconds);
	}

	/**
	 * Wählt einen anderen Inhalt für die Cards aus,
	 * wenn ein anderer Modus eingestellt wurde.
	 * @see #modeSelect
	 * @see #cards
	 */
	private void modeChanged() {
		final int mode=modeSelect.getSelectedIndex();
		cardLayout.show(cards,""+(mode+1));
		checkData(false);
	}

	/**
	 * Wählt eine Sound-Datei per Dateiauswahldialog aus.
	 * @see #soundFileEdit
	 */
	private void selectFile() {
		final String oldSound=soundFileEdit.getText().trim();
		final File initialDirectory;
		if (oldSound.isEmpty()) {
			initialDirectory=null;
		} else {
			final File file=new File(oldSound);
			initialDirectory=file.getParentFile();
		}
		final File newSound=soundSystem.selectFile(this,initialDirectory);
		if (newSound!=null) soundFileEdit.setText(newSound.toString());
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData(final boolean showErrorMessage) {
		final int mode=modeSelect.getSelectedIndex();

		/* Frequenz */
		if ((mode==4 && hasWindowsSounds) || (mode==3 && !hasWindowsSounds)) {
			if (NumberTools.getPositiveDouble(frequencyEdit,true)==null) {
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("SoundSelectPanel.Frequency.InvalidTitle"),Language.tr("SoundSelectPanel.Frequency.InvalidInfo"));
				}
				return false;
			}
			return true;
		}

		return true;
	}

	/**
	 * Liefert den gewählten neuen Sound.
	 * @return	Gewählter Sound
	 */
	public String getSound() {
		final int mode=modeSelect.getSelectedIndex();

		/* Systemereignis-Sounds */
		if (mode==0) {
			return (String)systemEventSoundSelect.getSelectedItem();
		}

		/* Windows-Sounds */
		if (mode==1 && hasWindowsSounds) {
			return (String)systemSoundFileSelect.getSelectedItem();
		}

		/* Sound-Dateien */
		if ((mode==2 && hasWindowsSounds) || (mode==1 && !hasWindowsSounds)) {
			return soundFileEdit.getText().trim();
		}

		/* Ton */
		if ((mode==3 && hasWindowsSounds) || (mode==2 && !hasWindowsSounds)) {
			return SoundSystem.fullToneNameList.get(toneSelect.getSelectedIndex());
		}

		/* Frequenz */
		if ((mode==4 && hasWindowsSounds) || (mode==3 && !hasWindowsSounds)) {
			final Double D=NumberTools.getPositiveDouble(frequencyEdit,true);
			return NumberTools.formatNumberMax((D==null)?440:D.doubleValue());
		}

		return SoundSystem.BEEP_SOUND;
	}

	/**
	 * Liefert die bei Sound-Dateien maximal abzuspielenden Sekunden.<br>
	 * (Ist ein System- oder Windows-Sound gewählt, so wird immer -1 zurückgegeben.)
	 * @return	Maximal abzuspielende Sekunden (oder &le;0 für unbegrenzt)
	 */
	public int getMaxSeconds() {
		final int mode=modeSelect.getSelectedIndex();

		if ((mode==2 && hasWindowsSounds) || (mode==1 && !hasWindowsSounds)) {
			final int maxSeconds=(Integer)soundFileMaxSeconds.getValue();
			if (maxSeconds==999) return -1; else return maxSeconds;
		} else {
			return -1;
		}
	}

	/**
	 * Spielt max. 10 Sekunden eines Sounds ab und gibt eine
	 * Fehlermeldung aus, wenn keine Ausgabe möglich ist.
	 * @param sound	Abzuspielender Sound
	 */
	private void play(final String sound) {
		if (!soundSystem.playAll(sound,10)) {
			MsgBox.error(this,Language.tr("SoundSelectPanel.Play.ErrorTitle"),String.format(Language.tr("SoundSelectPanel.Play.ErrorInfo"),sound));
		}
	}
}
