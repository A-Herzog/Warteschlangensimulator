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
package ui.parameterseries;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import language.Language;
import mathtools.distribution.tools.FileDropper;
import mathtools.distribution.tools.FileDropperData;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.statistics.Statistics;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.script.ScriptTools;

/**
 * Dialog zum Bearbeiten von einem Ausgabewert
 * für die Parameter-Vergleichs-Funktion
 * @author Alexander Herzog
 * @see ParameterCompareSetupValueOutputListDialog
 */
public class ParameterCompareSetupValueOutputDialog extends BaseDialog {
	private static final long serialVersionUID = 2916018873433384277L;

	private final ParameterCompareSetupValueOutput output;

	private final JTextField nameEdit;
	private final JComboBox<String> modeCombo;
	private final JPanel cardPanel;
	private final CardLayout cardLayout;

	private final JTextField xmlTagEdit;
	private final JButton xmlTagButton;

	private final JTextField expressionEdit;

	private final JTextField scriptEditJS;
	private final JButton scriptButtonJS;

	private final JTextField scriptEditJava;
	private final JButton scriptButtonJava;

	private final JCheckBox isTimeCheck;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param output	Ausgabewert-Datensatz
	 * @param model	Editor-Modell, welches die Basis für die Parameterstudie darstellt
	 * @param miniStatistics	Statistikdaten bezogen auf einen kurzen Lauf über das angegebene Editor-Modell (zur Auswahl von XML-Elementen als Zielwerte)
	 * @param help	Hilfe-Runnable
	 */
	public ParameterCompareSetupValueOutputDialog(final Component owner, final ParameterCompareSetupValueOutput output, final EditModel model, final Statistics miniStatistics, final Runnable help) {
		super(owner,Language.tr("ParameterCompare.Settings.Output.Title"));
		this.output=output;

		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		JPanel sub, line;
		JLabel label;
		Object[] data;

		/* Oben */

		content.add(sub=new JPanel(),BorderLayout.NORTH);
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Settings.Output.Name")+":",output.getName());
		sub.add((JPanel)data[0]);
		nameEdit=(JTextField)data[1];
		nameEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});

		sub.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("ParameterCompare.Settings.Output.Mode.Label")+":"));
		line.add(modeCombo=new JComboBox<>(new String[]{
				Language.tr("ParameterCompare.Settings.Output.Mode.XML"),
				Language.tr("ParameterCompare.Settings.Output.Mode.Script"),
				Language.tr("ParameterCompare.Settings.Output.Mode.ScriptJava"),
				Language.tr("ParameterCompare.Settings.Output.Mode.Command")
		}));
		modeCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.PARAMETERSERIES_OUTPUT_MODE_XML,
				Images.PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVASCRIPT,
				Images.PARAMETERSERIES_OUTPUT_MODE_SCRIPT_JAVA,
				Images.PARAMETERSERIES_OUTPUT_MODE_COMMAND
		}));
		label.setLabelFor(modeCombo);

		/* Mitte */

		content.add(cardPanel=new JPanel(),BorderLayout.CENTER);
		cardPanel.setLayout(cardLayout=new CardLayout());

		cardPanel.add(sub=new JPanel(),"0");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.add(line=new JPanel(new BorderLayout()));
		line.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		line.add(new JLabel(Language.tr("ParameterCompare.Settings.Output.Mode.XML.Label")+": "),BorderLayout.WEST);
		line.add(xmlTagEdit=new JTextField(),BorderLayout.CENTER);
		xmlTagEdit.setEditable(false);
		line.add(xmlTagButton=new JButton(Language.tr("ParameterCompare.Settings.Output.Mode.XML.Button")),BorderLayout.EAST);
		xmlTagButton.setToolTipText(Language.tr("ParameterCompare.Settings.Output.Mode.XML.Button.Hint"));
		xmlTagButton.setIcon(Images.PARAMETERSERIES_OUTPUT_MODE_XML.getIcon());
		xmlTagButton.addActionListener(e->{
			final String xml=ParameterCompareTools.selectXML(getOwner(),miniStatistics.saveToXMLDocument(),help);
			if (xml==null) return;
			xmlTagEdit.setText(xml);
			checkData(false);
		});

		cardPanel.add(sub=new JPanel(),"1");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.add(line=new JPanel(new BorderLayout()));
		line.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		line.add(new JLabel(Language.tr("ParameterCompare.Settings.Output.Mode.Script.Label")+": "),BorderLayout.WEST);
		line.add(scriptEditJS=new JTextField(),BorderLayout.CENTER);
		scriptEditJS.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(scriptButtonJS=new JButton(),BorderLayout.EAST);
		scriptButtonJS.setToolTipText(Language.tr("ParameterCompare.Settings.Output.Mode.Script.Button.Hint"));
		scriptButtonJS.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		scriptButtonJS.addActionListener(e->{
			final String newFileName=ScriptTools.selectJSFile(this,Language.tr("ParameterCompare.Settings.Output.Mode.Script.Button.DialogTitle"),scriptEditJS.getText());
			if (newFileName!=null) {
				scriptEditJS.setText(newFileName);
				checkData(false);
			}
		});
		new FileDropper(scriptEditJS,e->{
			final FileDropperData dropdata=(FileDropperData)e.getSource();
			final File file=dropdata.getFile();
			if (file.isFile()) {
				scriptEditJS.setText(file.toString());
				checkData(false);
				dropdata.dragDropConsumed();
			}
		});

		cardPanel.add(sub=new JPanel(),"2");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		sub.add(line=new JPanel(new BorderLayout()));
		line.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		line.add(new JLabel(Language.tr("ParameterCompare.Settings.Output.Mode.Script.Label")+": "),BorderLayout.WEST);
		line.add(scriptEditJava=new JTextField(),BorderLayout.CENTER);
		scriptEditJava.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(scriptButtonJava=new JButton(),BorderLayout.EAST);
		scriptButtonJava.setToolTipText(Language.tr("ParameterCompare.Settings.Output.Mode.Script.Button.Hint"));
		scriptButtonJava.setIcon(Images.GENERAL_SELECT_FILE.getIcon());
		scriptButtonJava.addActionListener(e->{
			final String newFileName=ScriptTools.selectJavaFile(this,Language.tr("ParameterCompare.Settings.Output.Mode.Script.Button.DialogTitle"),scriptEditJS.getText());
			if (newFileName!=null) {
				scriptEditJava.setText(newFileName);
				checkData(false);
			}
		});
		new FileDropper(scriptEditJava,e->{
			final FileDropperData dropdata=(FileDropperData)e.getSource();
			final File file=dropdata.getFile();
			if (file.isFile()) {
				scriptEditJava.setText(file.toString());
				checkData(false);
				dropdata.dragDropConsumed();
			}
		});

		cardPanel.add(sub=new JPanel(),"3");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("ParameterCompare.Settings.Output.Mode.Command.Command"),"");
		sub.add(line=(JPanel)data[0]);
		expressionEdit=(JTextField)data[1];
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(ModelElementBaseDialog.getStatisticsExpressionEditButton(this,expressionEdit,false,model,model.surface),BorderLayout.EAST);

		/* Unten */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(isTimeCheck=new JCheckBox(Language.tr("ParameterCompare.Settings.Output.IsTime")));

		/* Daten laden */

		modeCombo.addActionListener(e->{cardLayout.show(cardPanel,""+modeCombo.getSelectedIndex()); checkData(false);});

		switch (output.getMode()) {
		case MODE_XML:
			modeCombo.setSelectedIndex(0);
			xmlTagEdit.setText(output.getTag());
			break;
		case MODE_SCRIPT_JS:
			modeCombo.setSelectedIndex(1);
			scriptEditJS.setText(output.getTag());
			break;
		case MODE_SCRIPT_JAVA:
			modeCombo.setSelectedIndex(2);
			scriptEditJS.setText(output.getTag());
			break;
		case MODE_COMMAND:
			modeCombo.setSelectedIndex(3);
			expressionEdit.setText(output.getTag());
			break;
		}

		isTimeCheck.setSelected(output.getIsTime());

		checkData(false);

		/* Dialog starten */

		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(this.owner);
		setVisible(true);
	}

	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;

		if (nameEdit.getText().trim().isEmpty()) {
			nameEdit.setBackground(Color.RED);
			ok=false;
			if (showErrorMessage) {
				MsgBox.error(this,Language.tr("ParameterCompare.Settings.Output.Name.ErrorTitle"),Language.tr("ParameterCompare.Settings.Output.Name.ErrorInfo"));
				return false;
			}
		} else {
			nameEdit.setBackground(SystemColor.text);
		}

		switch (modeCombo.getSelectedIndex()) {
		case 0:
			if (xmlTagEdit.getText().isEmpty()) {
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Output.Mode.XML.ErrorTitle"),Language.tr("ParameterCompare.Settings.Output.Mode.XML.ErrorInfo"));
					return false;
				}
			}
			break;
		case 1:
			if (scriptEditJS.getText().isEmpty()) {
				scriptEditJS.setBackground(Color.RED);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Output.Mode.Script.ErrorTitle"),Language.tr("ParameterCompare.Settings.Output.Mode.Script.ErrorInfo"));
					return false;
				}
			} else {
				scriptEditJS.setBackground(SystemColor.text);
			}
			break;
		case 2:
			if (scriptEditJava.getText().isEmpty()) {
				scriptEditJava.setBackground(Color.RED);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Output.Mode.Script.ErrorTitle"),Language.tr("ParameterCompare.Settings.Output.Mode.Script.ErrorInfo"));
					return false;
				}
			} else {
				scriptEditJava.setBackground(SystemColor.text);
			}
			break;
		case 3:
			final int error=ExpressionCalc.check(expressionEdit.getText(),null);
			if (error>=0) {
				expressionEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessage) {
					MsgBox.error(this,Language.tr("ParameterCompare.Settings.Output.Mode.Command.ErrorTitle"),String.format(Language.tr("ParameterCompare.Settings.Output.Mode.Command.ErrorInfo"),expressionEdit.getText(),error+1));
					return false;
				}
			} else {
				expressionEdit.setBackground(SystemColor.text);
			}
			break;
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		output.setName(nameEdit.getText().trim());

		switch (modeCombo.getSelectedIndex()) {
		case 0:
			output.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_XML);
			output.setTag(xmlTagEdit.getText());
			break;
		case 1:
			output.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_SCRIPT_JS);
			output.setTag(scriptEditJS.getText());
			break;
		case 2:
			output.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_SCRIPT_JAVA);
			output.setTag(scriptEditJava.getText());
			break;
		case 3:
			output.setMode(ParameterCompareSetupValueOutput.OutputMode.MODE_COMMAND);
			output.setTag(expressionEdit.getText());
			break;
		}

		output.setIsTime(isTimeCheck.isSelected());
	}
}