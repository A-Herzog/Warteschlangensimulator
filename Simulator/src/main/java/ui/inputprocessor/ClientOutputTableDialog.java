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
package ui.inputprocessor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import mathtools.Table;
import mathtools.distribution.tools.FileDropper;
import simulator.elements.RunElementDisposeWithTable;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.elements.ModelElementDisposeWithTable;

/**
 * Dieser Dialog erlaubt es Tabellen, die Speichern+Ausgang-Stationen generieren,
 * in normale Tabellen mit Spaltenüberschriften umzuwandeln.
 * @author Alexander Herzog
 * @see ModelElementDisposeWithTable
 * @see RunElementDisposeWithTable
 */
public class ClientOutputTableDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=1331534513995045832L;

	/**
	 * Eingabefeld für die Eingabetabelle
	 */
	private final JTextField editInput;

	/**
	 * Eingabefeld für die Ausgabetabelle
	 */
	private final JTextField editOutput;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 */
	public ClientOutputTableDialog(final Component owner) {
		super(owner,Language.tr("ProcessClientOutputTable.Title"));

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(this,"ProcessClientOutputTable"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.globalProcessClientOutputTable);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		Object[] data;
		JPanel line;
		JButton button;

		/* Konfigurationsbereich */
		final JPanel setup=new JPanel();
		content.add(setup,BorderLayout.NORTH);
		setup.setLayout(new BoxLayout(setup,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ProcessClientOutputTable.InputTable")+":","");
		setup.add(line=(JPanel)data[0]);
		editInput=(JTextField)data[1];
		line.add(button=new JButton(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("ProcessClientOutputTable.InputTable.Select"));
		button.addActionListener(e->selectInputTable());
		FileDropper.addFileDropper(line,editInput);

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ProcessClientOutputTable.OutputTable")+":","");
		setup.add(line=(JPanel)data[0]);
		editOutput=(JTextField)data[1];
		line.add(button=new JButton(Images.GENERAL_SELECT_TABLE_IN_FILE.getIcon()),BorderLayout.EAST);
		button.setToolTipText(Language.tr("ProcessClientOutputTable.OutputTable.Select"));
		button.addActionListener(e->selectOutputTable());
		FileDropper.addFileDropper(line,editOutput);

		/* Dialog starten */
		setMinSizeRespectingScreensize(600,0);
		setMaxSizeRespectingScreensize(600,600);
		SwingUtilities.invokeLater(()->{
			pack();
			setMaxSizeRespectingScreensize(600,600);
		});
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Zeigt den Dialog zur Auswahl der Eingabetabelle an.
	 * @see #editInput
	 */
	private void selectInputTable() {
		final File file=Table.showLoadDialog(this,Language.tr("ProcessClientOutputTable.InputTable.Select"));
		if (file!=null) {
			editInput.setText(file.toString());
		}
	}

	/**
	 * Zeigt den Dialog zur Auswahl der Ausgabetabelle an.
	 * @see #editOutput
	 */
	private void selectOutputTable() {
		final File file=Table.showSaveDialog(this,Language.tr("ProcessClientOutputTable.OutputTable.Select"));
		if (file!=null) {
			editOutput.setText(file.toString());
		}
	}

	@Override
	protected boolean checkData() {
		final String input=editInput.getText().trim();
		if (input.isEmpty()) {
			MsgBox.error(this,Language.tr("ProcessClientOutputTable.InputTable.Error.NoFile.Title"),Language.tr("ProcessClientOutputTable.InputTable.Error.NoFile.Info"));
			return false;
		}
		final File inputFile=new File(input);
		if (!inputFile.isFile()) {
			MsgBox.error(this,Language.tr("ProcessClientOutputTable.InputTable.Error.DoesNotExist.Title"),String.format(Language.tr("ProcessClientOutputTable.InputTable.Error.DoesNotExist.Info"),input));
			return false;
		}

		final String output=editOutput.getText().trim();
		if (output.isEmpty()) {
			MsgBox.error(this,Language.tr("ProcessClientOutputTable.OutputTable.Error.NoFile.Title"),Language.tr("ProcessClientOutputTable.OutputTable.Error.NoFile.Info"));
			return false;
		}
		final File outputFile=new File(output);
		if (outputFile.isFile()) {
			if (!MsgBox.confirmOverwrite(this,outputFile)) return false;
		}

		return true;
	}

	@Override
	protected void storeData() {
		final String input=editInput.getText().trim();
		final File inputFile=new File(input);
		final String output=editOutput.getText().trim();
		final File outputFile=new File(output);

		final Table inputTable=new Table();
		if (!inputTable.load(inputFile)) {
			MsgBox.error(this,Language.tr("ProcessClientOutputTable.InputTable.Error.Load.Title"),String.format(Language.tr("ProcessClientOutputTable.InputTable.Error.Load.Info"),input));
			return;
		}
		final Table outputTable=new Table();
		process(inputTable,outputTable);
		if (!outputTable.save(outputFile)) {
			MsgBox.error(this,Language.tr("ProcessClientOutputTable.OutputTable.Error.Save.Title"),String.format(Language.tr("ProcessClientOutputTable.OutputTable.Error.Save.Info"),output));
		}
	}

	/**
	 * Führt die Aufbereitung der Tabelle durch.
	 * @param inputTable	Eingabetabelle
	 * @param outputTable	Ausgabetabelle
	 */
	public static void process(final Table inputTable, final Table outputTable) {
		/* Erfassung, welche Daten vorhanden sind */
		boolean hasW=false;
		boolean hasT=false;
		boolean hasP=false;
		boolean hasWCosts=false;
		boolean hasTCosts=false;
		boolean hasPCosts=false;
		final Set<Integer> clientDataIndices=new HashSet<>();
		final Set<String> clientDataKeys=new HashSet<>();
		final int inputRows=inputTable.getSize(0);
		for (int i=0;i<inputRows;i++) {
			final List<String> line=inputTable.getLine(i);
			for (int j=2;j<line.size();j++) {
				final String cell=line.get(j);
				if (cell.startsWith("w=")) {hasW=true; continue;}
				if (cell.startsWith("t=")) {hasT=true; continue;}
				if (cell.startsWith("p=")) {hasP=true; continue;}
				if (cell.startsWith("wCosts=")) {hasWCosts=true; continue;}
				if (cell.startsWith("tCosts=")) {hasTCosts=true; continue;}
				if (cell.startsWith("pCosts=")) {hasPCosts=true; continue;}
				if (cell.startsWith("ClientData('") && cell.length()>12+1+3+1) {
					final String s=cell.substring(12);
					final int index=s.indexOf("')=");
					if (index>0) clientDataKeys.add(s.substring(0,index));
					continue;
				}
				if (cell.startsWith("ClientData(") && cell.length()>11+1+2+1) {
					final String s=cell.substring(11);
					final int index=s.indexOf(")=");
					if (index>0) {
						final Integer I=NumberTools.getNotNegativeInteger(s.substring(0,index));
						if (I!=null) clientDataIndices.add(I);
					}
					continue;
				}
			}
		}

		/* Spalten bestimmen */
		final int[] clientDataIndicesList=clientDataIndices.stream().mapToInt(Integer::intValue).sorted().toArray();
		final String[] clientDataKeysList=clientDataKeys.stream().sorted().toArray(String[]::new);
		final Map<Integer,Integer> clientDataIndicesMap=new HashMap<>();
		for (int i=0;i<clientDataIndicesList.length;i++) clientDataIndicesMap.put(clientDataIndicesList[i],i);
		final Map<String,Integer> clientDataKeysMap=new HashMap<>();
		for (int i=0;i<clientDataKeysList.length;i++) clientDataKeysMap.put(clientDataKeysList[i],i);
		int baseDataColCount=2;
		int colW=-1;
		int colT=-1;
		int colP=-1;
		int colWCosts=-1;
		int colTCosts=-1;
		int colPCosts=-1;
		if (hasW) {colW=baseDataColCount; baseDataColCount++;}
		if (hasT) {colT=baseDataColCount; baseDataColCount++;}
		if (hasP) {colP=baseDataColCount; baseDataColCount++;}
		if (hasWCosts) {colWCosts=baseDataColCount; baseDataColCount++;}
		if (hasTCosts) {colTCosts=baseDataColCount; baseDataColCount++;}
		if (hasPCosts) {colPCosts=baseDataColCount; baseDataColCount++;}

		/* Ausgabe der Überschriftzeile */
		final List<String> heading=new ArrayList<>();
		heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.ArrivalTime"));
		heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.ClientType"));
		if (hasW) heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.WaitingTime"));
		if (hasT) heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.TransferTime"));
		if (hasP) heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.ProcessTime"));
		if (hasWCosts) heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.WaitingTimeCosts"));
		if (hasTCosts) heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.TransferTimeCosts"));
		if (hasPCosts) heading.add(Language.tr("ProcessClientOutputTable.ColumnHeader.ProcessTimeCosts"));
		for (int index: clientDataIndicesList) heading.add("ClientData("+index+")");
		for (String key: clientDataKeysList)  heading.add("ClientData('"+key+"')");
		outputTable.addLine(heading);

		/* Aufbereitung und Ausgabe der Zeilen */
		for (int i=0;i<inputRows;i++) {
			final List<String> inputLine=inputTable.getLine(i);
			if (inputLine.size()<2) continue;
			final List<String> outputLine=new ArrayList<>();
			outputLine.add(inputLine.get(0));
			outputLine.add(inputLine.get(1));
			for (int j=0;j<baseDataColCount-2;j++) outputLine.add("0");
			for (int j=0;j<clientDataIndicesList.length+clientDataKeysList.length;j++) outputLine.add("");
			for (int j=2;j<inputLine.size();j++) {
				final String cell=inputLine.get(j);
				if (cell.startsWith("w=") && cell.length()>2) {outputLine.set(colW,cell.substring(2)); continue;}
				if (cell.startsWith("t=") && cell.length()>2) {outputLine.set(colT,cell.substring(2)); continue;}
				if (cell.startsWith("p=") && cell.length()>2) {outputLine.set(colP,cell.substring(2)); continue;}
				if (cell.startsWith("wCosts=") && cell.length()>2) {outputLine.set(colWCosts,cell.substring(7)); continue;}
				if (cell.startsWith("tCosts=") && cell.length()>2) {outputLine.set(colTCosts,cell.substring(7)); continue;}
				if (cell.startsWith("pCosts=") && cell.length()>2) {outputLine.set(colPCosts,cell.substring(7)); continue;}
				if (cell.startsWith("ClientData('") && cell.length()>12+1+3+1) {
					final String s=cell.substring(12);
					final int index=s.indexOf("')=");
					if (index>0) {
						final int col=clientDataKeysMap.get(s.substring(0,index));
						outputLine.set(baseDataColCount+clientDataIndicesList.length+col,s.substring(index+3));
					}
					continue;
				}
				if (cell.startsWith("ClientData(") && cell.length()>11+1+2+1) {
					final String s=cell.substring(11);
					final int index=s.indexOf(")=");
					if (index>0) {
						final Integer I=NumberTools.getNotNegativeInteger(s.substring(0,index));
						if (I!=null) {
							final int col=clientDataIndicesMap.get(I);
							outputLine.set(baseDataColCount+col,s.substring(index+2));
						}
					}
					continue;
				}
			}
			outputTable.addLine(outputLine);
		}
	}
}
