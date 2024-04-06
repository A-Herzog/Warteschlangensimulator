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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.CommonVariables;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.SetupData;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Dialog zur Auswahl der Einstellungen zur Aufzeichnung
 * von Animationen als Videos an.
 * @author Alexander Herzog
 */
public class AnimationRecordSetupDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=8939964843276305510L;

	/**
	 * Eingabefeld für die Videodatei
	 */
	private final JTextField editVideo;

	/**
	 * Schieberegler für den Skalierungsfaktor
	 */
	private final JSlider scaleSlider;

	/**
	 * Anzeige des Skalierungsfaktor gemäß Einstellung in {@link #scaleSlider}
	 */
	private final JLabel scaleInfo;

	/**
	 * Simulationszeit in Video einblenden?
	 */
	private final JCheckBox timeStamp;

	/**
	 * Videoaufzeichnung von Animationen sofort starten?
	 */
	private final JCheckBox startImmediately;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public AnimationRecordSetupDialog(final Component owner) {
		super(owner,Language.tr("RecordAnimation.Title"));

		final SetupData setup=SetupData.getSetup();

		/* GUI */
		final JPanel main=createGUI(null);
		main.setLayout(new BorderLayout());
		final JPanel content=new JPanel();
		main.add(content,BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line;
		Object[] data;
		JButton button;
		JLabel label;

		/* Videodatei */
		data=ModelElementBaseDialog.getInputPanel(Language.tr("RecordAnimation.Video")+":","");
		content.add(line=(JPanel)data[0]);
		editVideo=(JTextField)data[1];
		editVideo.addKeyListener(new KeyAdapter() {
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});
		line.add(button=new JButton(Images.GENERAL_SELECT_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("RecordAnimation.Video.Select"));
		button.addActionListener(e->selectVideo());

		/* Skalierung */
		content.add(line=new JPanel(new BorderLayout()));
		line.setBorder(BorderFactory.createEmptyBorder(10,0,5,0));
		line.add(label=new JLabel(Language.tr("RecordAnimation.Scale")+":"),BorderLayout.WEST);
		line.add(scaleSlider=new JSlider(1,100),BorderLayout.CENTER);
		scaleSlider.setValue(Math.max(1,Math.min(100,(int)Math.round(setup.animationFrameScale*100))));
		label.setLabelFor(scaleSlider);
		label.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		line.add(scaleInfo=new JLabel(),BorderLayout.EAST);
		scaleSlider.addChangeListener(e->updateSlideInfo());
		updateSlideInfo();

		/* Timestamps */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(timeStamp=new JCheckBox(Language.tr("RecordAnimation.TimeStamp"),setup.paintTimeStamp));

		/* Sofort starten? */
		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(startImmediately=new JCheckBox(Language.tr("RecordAnimation.StartImmediately"),setup.animationRecordStartImmediately));

		/* Dialog starten */
		checkData(false);
		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Zeigt einen Dialog zur Auswahl der Ziel-Videodatei an.
	 * @see #editVideo
	 */
	private void selectVideo() {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Window.SelectVideoFile"));
		final FileFilter avi=new FileNameExtensionFilter(Language.tr("FileType.VideoFile")+" (*.avi)","avi");
		fc.addChoosableFileFilter(avi);
		fc.setFileFilter(avi);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==avi) file=new File(file.getAbsoluteFile()+".avi");
		}

		editVideo.setText(file.toString());
		checkData(false);
	}

	/**
	 * Aktualisiert die Anzeige des Skalierungsfaktors in {@link #scaleInfo}.
	 * @see #scaleSlider
	 * @see #scaleInfo
	 */
	private void updateSlideInfo() {
		scaleInfo.setText(scaleSlider.getValue()+"%");
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		if (editVideo.getText().trim().isEmpty()) {
			editVideo.setBackground(Color.RED);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("RecordAnimation.Video.NoVideo.Title"),Language.tr("RecordAnimation.Video.NoVideo.Info"));
				return false;
			}
		} else {
			editVideo.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		final SetupData setup=SetupData.getSetup();

		final double animationFrameScale=scaleSlider.getValue()/100.0;

		if (timeStamp.isSelected()!=setup.paintTimeStamp || Math.abs(animationFrameScale-setup.animationFrameScale)>0.001) {
			setup.animationFrameScale=animationFrameScale;
			setup.paintTimeStamp=timeStamp.isSelected();
			setup.animationRecordStartImmediately=startImmediately.isSelected();
			setup.saveSetup();
		}
	}

	/**
	 * Liefert die ausgewählte Videodatei für die Aufzeichnung der Animation.
	 * @return	Ausgewählte Videodatei
	 */
	public File getVideoFile() {
		return new File(editVideo.getText());
	}
}
