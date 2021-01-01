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
package ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.TimeTools;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.descriptionbuilder.StyledTextBuilder;
import ui.statistics.analyticcompare.AnalyticInfo;

/**
 * In diesem Dialog werden Daten zu von dem Editor-Model abgeleiteten analytischen Modellen angezeigt.
 * Es wird dabei angeboten, die Informationen in verschiedenen Formaten zu speichern.
 * @author Alexander Herzog
 * @see AnalyticInfo
 */
public class ModelAnalyticInfoDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -464947739154264213L;

	/**
	 * Der Text-Builder nimmt die Ausgaben der Analyse auf und
	 * überträgt diese in das Ausgabe {@link JTextPane} bzw.
	 * stellt die Daten zum Speichern in einer Datei bereit.
	 * @see #buildText(AnalyticInfo, ui.statistics.analyticcompare.AnalyticInfo.SimulationResults)
	 */
	private StyledTextBuilder textBuilder=new StyledTextBuilder();

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element (zur Ausrichtung des Dialogs)
	 * @param analyticInfo	Objekt, welches die analytischen Daten zu dem Modell bereitstellt
	 * @param simulationResults	Optional Simulationsergebnisse zum Vergleich (darf <code>null</code> sein)
	 */
	public ModelAnalyticInfoDialog(final Component owner, final AnalyticInfo analyticInfo, final AnalyticInfo.SimulationResults simulationResults) {
		super(owner,Language.tr("AnalyticModelCompare.Dialog.Title"));
		textBuilder=new StyledTextBuilder();
		buildText(analyticInfo,simulationResults);
		showCloseButton=true;

		/* GUI aufbauen */
		addUserButton(Language.tr("AnalyticModelCompare.Dialog.Copy"),Images.EDIT_COPY.getIcon());
		addUserButton(Language.tr("AnalyticModelCompare.Dialog.Save"),Images.GENERAL_SAVE.getIcon());
		final JPanel content=createGUI(()->Help.topicModal(this,"EditorAnalyticCompareDialog"));
		content.setLayout(new BorderLayout());

		final JTextPane textPane=new JTextPane();
		textPane.setEditable(false);
		textPane.setBackground(new Color(0xFF,0xFF,0xF8));
		textBuilder.writeToTextPane(textPane);

		content.add(new JScrollPane(textPane),BorderLayout.CENTER);
		textPane.setSelectionStart(0);
		textPane.setSelectionEnd(0);

		/* Dialog vorbereiten */
		setMinSizeRespectingScreensize(700,900);
		setSizeRespectingScreensize(700,900);
		setResizable(true);
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Erstellt einen Text, der analytische Informationen und evtl. Simulationsdaten enthält.
	 * @param analyticInfo	Analytische Informationen
	 * @param simulationResults	Simulationsergebnisse (kann <code>null</code> sein)
	 */
	private void buildText(final AnalyticInfo analyticInfo, final AnalyticInfo.SimulationResults simulationResults) {
		textBuilder.addHeading(1,Language.tr("Statistics.ErlangCCompare.SimModel"));

		buildTextModelInfo(analyticInfo);
		if (simulationResults!=null) buildTextSimResults(simulationResults);
		buildTextAnalytic(analyticInfo,simulationResults);
	}

	/**
	 * Gibt auf Basis der analytischen Informationen Daten zum Modell aus.
	 * @param analyticInfo	Analytische Informationen
	 * @see #buildText(AnalyticInfo, ui.statistics.analyticcompare.AnalyticInfo.SimulationResults)
	 */
	private void buildTextModelInfo(final AnalyticInfo analyticInfo) {
		textBuilder.addHeading(2,Language.tr("Statistics.ErlangCompare.Arrival"));
		textBuilder.beginParagraph();
		textBuilder.addLines(analyticInfo.getSourceInfo());
		textBuilder.endParagraph();

		textBuilder.addHeading(2,Language.tr("Statistics.ErlangCompare.Service"));
		textBuilder.beginParagraph();
		textBuilder.addLines(analyticInfo.getProcessInfo());
		textBuilder.endParagraph();
	}

	/**
	 * Gibt die Simulationsergebnisse aus.
	 * @param simulationResults	Simulationsergebnisse
	 * @see #buildText(AnalyticInfo, ui.statistics.analyticcompare.AnalyticInfo.SimulationResults)
	 */
	private void buildTextSimResults(final AnalyticInfo.SimulationResults simulationResults) {
		textBuilder.addHeading(2,Language.tr("Statistics.ErlangCCompare.SimResults"));

		textBuilder.addHeading(3,Language.tr("Statistics.ErlangCompare.NumberOfClients"));
		textBuilder.beginParagraph();
		textBuilder.addLine("E[NQ]="+NumberTools.formatNumber(simulationResults.ENQ));
		textBuilder.addLine("E[NS]="+NumberTools.formatNumber(simulationResults.ENS));
		textBuilder.addLine("E[N]="+NumberTools.formatNumber(simulationResults.EN));
		textBuilder.beginParagraph();

		textBuilder.addHeading(3,Language.tr("Statistics.ErlangCompare.Times"));
		textBuilder.beginParagraph();
		textBuilder.addLine("E[W]="+NumberTools.formatNumber(simulationResults.EW)+" "+Language.tr("Statistics.Seconds")+" ("+TimeTools.formatExactTime(simulationResults.EW,1)+")");
		textBuilder.addLine("E[V]="+NumberTools.formatNumber(simulationResults.EV)+" "+Language.tr("Statistics.Seconds")+" ("+TimeTools.formatExactTime(simulationResults.EV,1)+")");
		textBuilder.endParagraph();

		if (!simulationResults.resourceInfo.isEmpty()) {
			textBuilder.addHeading(3,Language.tr("Statistics.Utilization"));
			textBuilder.beginParagraph();
			textBuilder.addLines(simulationResults.resourceInfo);
			textBuilder.endParagraph();
		}
	}

	/**
	 * Gibt die analytischen Ergebnisse gemäß einer bestimmten Formel aus.
	 * @param results	Analytische Ergebnisse gemäß einer bestimmten Formel
	 * @see #buildTextAnalytic(AnalyticInfo, ui.statistics.analyticcompare.AnalyticInfo.SimulationResults)
	 */
	private void outputAnalyticResults(final AnalyticInfo.InfoResult results) {
		if (results==null) return;

		textBuilder.addHeading(2,Language.tr("Statistics.ErlangCCompare.Results.Input"));
		textBuilder.beginParagraph();
		textBuilder.addLines(results.getInput());
		textBuilder.endParagraph();

		final String calculated=results.getCalculated();
		if (!calculated.isEmpty()) {
			textBuilder.addHeading(2,Language.tr("Statistics.ErlangCCompare.Results.Calculated"));
			textBuilder.beginParagraph();
			textBuilder.addLines(calculated);
			textBuilder.endParagraph();
		}

		final String numbers=results.getNumbers();
		if (!numbers.isEmpty()) {
			textBuilder.addHeading(2,Language.tr("Statistics.ErlangCompare.NumberOfClients"));
			textBuilder.beginParagraph();
			textBuilder.addLines(numbers);
			textBuilder.endParagraph();
		}

		final String times=results.getTimes();
		if (!times.isEmpty()) {
			textBuilder.addHeading(2,Language.tr("Statistics.ErlangCompare.Times"));
			textBuilder.beginParagraph();
			textBuilder.addLines(times);
			textBuilder.endParagraph();
		}

		final String info=results.getInfo();
		if (!info.isEmpty()) {
			textBuilder.addHeading(2,Language.tr("Statistics.ErlangCompare.Info"));
			textBuilder.beginParagraph();
			textBuilder.addLines(info);
			textBuilder.endParagraph();
		}
	}

	/**
	 * Erstellt einen Text, der analytische Informationen Simulationsdaten enthält.
	 * @param analyticInfo	Analytische Informationen
	 * @param simulationResults	Simulationsergebnisse (kann <code>null</code> sein)
	 * @see #buildText(AnalyticInfo, ui.statistics.analyticcompare.AnalyticInfo.SimulationResults)
	 */
	private void buildTextAnalytic(final AnalyticInfo analyticInfo, final AnalyticInfo.SimulationResults simulationResults) {
		textBuilder.addHeading(1,Language.tr("Statistics.ErlangCCompare.Results.ErlangC"));
		outputAnalyticResults(analyticInfo.getErlangC(simulationResults));

		if (analyticInfo.hasCancelTimes()) {
			textBuilder.addHeading(1,Language.tr("Statistics.ErlangCCompare.Results.ErlangCExt"));
			outputAnalyticResults(analyticInfo.getErlangCExt(simulationResults));
		}

		textBuilder.addHeading(1,Language.tr("Statistics.ErlangCCompare.Results.AllenCunneen"));
		outputAnalyticResults(analyticInfo.getAllenCunneen(simulationResults));
	}

	/**
	 * Speichert die Daten in einer Datei.
	 * @param text	Auszugebender Text
	 * @param file	Ausgabedatei
	 * @see #saveToFile()
	 */
	private void saveTextToFile(final String text, final File file) {
		if (file.isFile()) {
			if (!file.delete()) {
				MsgBox.error(this,Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Title"),String.format(Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Info"),file.toString()));
				return;
			}
		}

		if (!Table.saveTextToFile(text,file)) {
			MsgBox.error(this,Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Title"),String.format(Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Info"),file.toString()));
		}
	}

	/**
	 * Speichert die Daten in einer Datei.
	 * Dazu wird zunächst ein Dateiauswahldialog angezeigt.
	 */
	private void saveToFile() {
		final File file=StyledTextBuilder.getSaveFile(this,Language.tr("AnalyticModelCompare.Dialog.Save.Title"));
		if (file==null) return;

		if (file.getName().toLowerCase().endsWith(".txt")) {
			saveTextToFile(textBuilder.getText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".rtf")) {
			saveTextToFile(textBuilder.getRTFText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".html")) {
			saveTextToFile(textBuilder.getHTMLText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".md")) {
			saveTextToFile(textBuilder.getMDText(),file);
		}

		if (file.getName().toLowerCase().endsWith(".docx")) {
			if (!textBuilder.saveDOCX(file,null)) {
				MsgBox.error(this,Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Title"),String.format(Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Info"),file.toString()));
			}
		}

		if (file.getName().toLowerCase().endsWith(".pdf")) {
			if (!textBuilder.savePDF(this,file,null)) {
				MsgBox.error(this,Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Title"),String.format(Language.tr("AnalyticModelCompare.Dialog.Save.Failed.Info"),file.toString()));
			}
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		switch (nr) {
		case 0: textBuilder.copyToClipboard(); break;
		case 1: saveToFile(); break;
		}
	}
}
