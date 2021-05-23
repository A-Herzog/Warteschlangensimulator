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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.script.ScriptEditorPanel;

/**
 * Dieser Dialog ermöglicht das Bearbeiten eines einzelnen Eintrags
 * einer {@link ModelElementActionRecordTableModel}-Tabelle.
 * @author Alexander Herzog
 * @see ModelElementActionRecordTableModel
 */
public class ModelElementActionRecordTableModelDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4694694293692841647L;

	/**
	 * Namen der modellweiten Variablen
	 */
	private final String[] variables;

	/**
	 * Zu bearbeitender Datensatz
	 */
	private final ModelElementActionRecord record;

	/** HTML-Kopf für die Ausgabe von fett dargestelltem Text */
	private static final String bold1="<html><body><b>";
	/** HTML-Fuß für die Ausgabe von fett dargestelltem Text */
	private static final String bold2="</b></body></html>";

	/** Ist die Teilaktion aktiv? */
	private final JCheckBox activeCheckBox;

	/** Art der Bedingung: Rechenausdruck-Bedingung */
	private final JRadioButton triggerCondition;
	/** Art der Bedingung: Schwellenwert */
	private final JRadioButton triggerThreshold;
	/** Art der Bedingung: Signal */
	private final JRadioButton triggerSignal;

	/** Art der auszulösenden Aktion: Variablenzuweisung */
	private final JRadioButton actionAssign;
	/** Art der auszulösenden Aktion: Analogwertzuweisung */
	private final JRadioButton actionAnalog;
	/** Art der auszulösenden Aktion: Signalauslöseung */
	private final JRadioButton actionSignal;
	/** Art der auszulösenden Aktion: Skriptausführung */
	private final JRadioButton actionScript;

	/** Eingabefeld für die Bedingung im Fall {@link #triggerCondition} */
	private final JTextField conditionEdit;
	/** Eingabefeld für den minimalen zeitlichen Abstand für zwei bedingungs-ausgelöste Aktionen im Fall {@link #triggerCondition} */
	private final JTextField conditionMinDistanceEdit;

	/** Eingabefeld für den Schwellenwert-Ausdruck im Fall {@link #triggerThreshold} */
	private final JTextField thresholdExpressionEdit;
	/** Eingabefeld für den Schwellenwert-Zahlenwert im Fall {@link #triggerThreshold} */
	private final JTextField thresholdValueEdit;
	/** Option: Schwellenwert muss für Signalauslösung überschritten werden (im Fall {@link #triggerThreshold}) */
	private final JRadioButton thresholdDirectionUp;
	/** Option: Schwellenwert muss für Signalauslösung unterschritten werden (im Fall {@link #triggerThreshold}) */
	private final JRadioButton thresholdDirectionDown;

	/** Signal das die Aktion auslöst im Fall {@link #triggerSignal} */
	private final JComboBox<String> triggerSignalName;

	/** Textfeld für den Variablennamen bei einer {@link #actionAssign} Aktion */
	private final JTextField assignVariableEdit;
	/** Textfeld für den Ausdruck für die Variablenzuweisung bei einer {@link #actionAssign} Aktion */
	private final JTextField assignExpressionEdit;

	/** Namen aller Analogwert-Stationen */
	private String[] analogIDNames;
	/** Stations-IDs aller Analogwert-Stationen */
	private int[] analogIDs;
	/** Auswahlfeld für das analoge Element bei einer {@link #actionAnalog} Aktion */
	private final JComboBox<String> analogElementCombo;
	/** Textfeld für den Wert für eine analoger Wert Zuweisung bei einer {@link #actionAnalog} Aktion */
	private final JTextField analogExpressionEdit;

	/** Textfeld für das auszulösende Signal bei einer {@link #actionSignal} Aktion */
	private final JTextField signalNameEdit;

	/** Textfeld für das auszuführende Skript bei einer {@link #actionScript} Aktion */
	private final ScriptEditorPanel scriptEdit;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param record	Zu bearbeitender Datensatz
	 * @param surface	Haupt-Zeichenfläche (für Expression-Builder)
	 * @param model	Vollständiges Modell (für Expression-Builder)
	 * @param help	Hilfe-Callback
	 */
	public ModelElementActionRecordTableModelDialog(final Component owner, final ModelElementActionRecord record, final ModelSurface surface, final EditModel model, final Runnable help) {
		super(owner,Language.tr("Surface.Action.Dialog.Edit"));
		this.record=record;
		variables=surface.getMainSurfaceVariableNames(model.getModelVariableNames(),false);
		buildAnalogIDNames(surface);

		/* GUI */
		final JPanel content=createGUI(help);
		content.setLayout(new BorderLayout());

		/* Aktivierung */
		final JPanel setup=new JPanel(new FlowLayout(FlowLayout.LEFT));
		content.add(setup,BorderLayout.NORTH);
		setup.add(activeCheckBox=new JCheckBox(Language.tr("Surface.Action.Dialog.Edit.Active"),record.isActive()));

		/* Tabs */
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tab, tabOuter;
		JPanel line;
		JLabel label;
		Object[] data;
		ButtonGroup buttonGroup;

		if (record.getActionMode()==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {

			/* Tab "Auslöser" */

			tabs.add(tabOuter=new JPanel(new BorderLayout()),Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger"));
			tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
			tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
			tab.setBorder(BorderFactory.createEmptyBorder(0,5,10,5));

			/* Bedingung */

			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(triggerCondition=new JRadioButton(bold1+Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition")+bold2));
			triggerCondition.addActionListener(e->checkData(false));

			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition")+":",record.getCondition());
			tab.add(line=(JPanel)data[0]);
			conditionEdit=(JTextField)data[1];
			line.add(ModelElementBaseDialog.getExpressionEditButton(this,conditionEdit,true,false,model,surface),BorderLayout.EAST);
			addKeyListener(conditionEdit,()->triggerCondition.setSelected(true));

			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition.MinDistance")+":",NumberTools.formatNumber(record.getConditionMinDistance()),10);
			tab.add(line=(JPanel)data[0]);
			conditionMinDistanceEdit=(JTextField)data[1];
			line.add(new JLabel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition.MinDistance.Seconds")));
			addKeyListener(conditionEdit,()->triggerCondition.setSelected(true));

			/* Schwellenwert */

			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(triggerThreshold=new JRadioButton(bold1+Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Threshold")+bold2));
			triggerThreshold.addActionListener(e->checkData(false));

			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdExpression")+":",record.getThresholdExpression());
			tab.add(line=(JPanel)data[0]);
			thresholdExpressionEdit=(JTextField)data[1];
			line.add(ModelElementBaseDialog.getExpressionEditButton(this,thresholdExpressionEdit,false,false,model,surface),BorderLayout.EAST);
			addKeyListener(thresholdExpressionEdit,()->triggerThreshold.setSelected(true));

			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdValue")+":",NumberTools.formatNumber(record.getThresholdValue()),10);
			tab.add(line=(JPanel)data[0]);
			thresholdValueEdit=(JTextField)data[1];
			addKeyListener(thresholdValueEdit,()->triggerThreshold.setSelected(true));

			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(thresholdDirectionUp=new JRadioButton(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdDirection.Up")));
			thresholdDirectionUp.addActionListener(e->{triggerThreshold.setSelected(true); checkData(false);});
			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(thresholdDirectionDown=new JRadioButton(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdDirection.Down")));
			thresholdDirectionDown.addActionListener(e->{triggerThreshold.setSelected(true); checkData(false);});
			buttonGroup=new ButtonGroup();
			buttonGroup.add(thresholdDirectionUp);
			buttonGroup.add(thresholdDirectionDown);
			switch (record.getThresholdDirection()) {
			case THRESHOLD_DOWN: thresholdDirectionDown.setSelected(true); break;
			case THRESHOLD_UP: thresholdDirectionUp.setSelected(true); break;
			}

			/* Signal */

			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(triggerSignal=new JRadioButton(bold1+Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Signal")+bold2));
			triggerSignal.addActionListener(e->checkData(false));
			final String[] triggerSignalNames=model.surface.getAllSignalNames().toArray(new String[0]);
			line.add(triggerSignalName=new JComboBox<>(triggerSignalNames));
			final String triggerSignalNameCurrent=record.getConditionSignal();
			int index=-1;
			if (!triggerSignalNameCurrent.trim().isEmpty()) for (int i=0;i<triggerSignalNames.length;i++) if (triggerSignalNameCurrent.equalsIgnoreCase(triggerSignalNames[i])) {index=i; break;}
			if (index<0 && triggerSignalNames.length>0) index=0;
			if (index>=0) triggerSignalName.setSelectedIndex(0);
			triggerSignalName.addActionListener(e->{triggerSignal.setSelected(true); checkData(false);});

			/* Auslöser-Radiobuttons zusammenfassen */

			buttonGroup=new ButtonGroup();
			buttonGroup.add(triggerCondition);
			buttonGroup.add(triggerThreshold);
			buttonGroup.add(triggerSignal);

			switch (record.getConditionType()) {
			case CONDITION_CONDITION: triggerCondition.setSelected(true); break;
			case CONDITION_THRESHOLD: triggerThreshold.setSelected(true); break;
			case CONDITION_SIGNAL: triggerSignal.setSelected(true); break;
			}
		} else {
			triggerCondition=null;
			triggerThreshold=null;
			triggerSignal=null;
			triggerSignalName=null;

			conditionEdit=null;
			conditionMinDistanceEdit=null;

			thresholdExpressionEdit=null;
			thresholdValueEdit=null;
			thresholdDirectionUp=null;
			thresholdDirectionDown=null;
		}

		/* Tab "Aktion" */

		tabs.add(tabOuter=new JPanel(new BorderLayout()),Language.tr("Surface.Action.Dialog.Edit.Tabs.Action"));
		tabOuter.add(tab=new JPanel(),BorderLayout.NORTH);
		tab.setLayout(new BoxLayout(tab,BoxLayout.PAGE_AXIS));
		tab.setBorder(BorderFactory.createEmptyBorder(0,5,10,5));

		/* Variablenzuweisung */

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(actionAssign=new JRadioButton(bold1+Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign")+bold2));
		actionAssign.addActionListener(e->checkData(false));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Variable")+":",record.getAssignVariable(),15);
		tab.add((JPanel)data[0]);
		assignVariableEdit=(JTextField)data[1];
		addKeyListener(assignVariableEdit,()->actionAssign.setSelected(true));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Expression")+":",record.getAssignExpression());
		tab.add(line=(JPanel)data[0]);
		assignExpressionEdit=(JTextField)data[1];
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,assignExpressionEdit,false,false,model,surface),BorderLayout.EAST);
		addKeyListener(assignExpressionEdit,()->actionAssign.setSelected(true));

		/* Analoger Wert */

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(actionAnalog=new JRadioButton(bold1+Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog")+bold2));
		actionAnalog.addActionListener(e->checkData(false));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Element")+":"));
		line.add(analogElementCombo=new JComboBox<>(analogIDNames));
		label.setLabelFor(analogElementCombo);
		int index=-1;
		for (int i=0;i<analogIDs.length;i++) if (analogIDs[i]==record.getAnalogID()) {index=i; break;}
		if (index<0 && analogIDs.length>0) index=0;
		if (index>=0) analogElementCombo.setSelectedIndex(index);
		analogElementCombo.addActionListener(e->{actionAnalog.setSelected(true); checkData(false);});

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Expression")+":",record.getAnalogValue());
		tab.add(line=(JPanel)data[0]);
		analogExpressionEdit=(JTextField)data[1];
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,analogExpressionEdit,false,false,model,surface),BorderLayout.EAST);
		addKeyListener(analogExpressionEdit,()->actionAnalog.setSelected(true));

		/* Signal auslösen */

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(actionSignal=new JRadioButton(bold1+Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Signal")+bold2));
		actionSignal.addActionListener(e->checkData(false));

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));

		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Signal.Name")+":",record.getSignalName(),25);
		tab.add((JPanel)data[0]);
		signalNameEdit=(JTextField)data[1];
		addKeyListener(signalNameEdit,()->actionSignal.setSelected(true));

		/* Javascript ausführen */

		tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(actionScript=new JRadioButton(bold1+Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.JS")+bold2));
		actionScript.addActionListener(e->checkData(false));

		final ScriptEditorPanel.ScriptMode mode;
		switch (record.getScriptMode()) {
		case Javascript: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
		case Java: mode=ScriptEditorPanel.ScriptMode.Java; break;
		default: mode=ScriptEditorPanel.ScriptMode.Javascript; break;
		}
		tabOuter.add(scriptEdit=new ScriptEditorPanel(record.getScript(),mode,false,null,model,help,ScriptEditorPanel.featuresPlainStation),BorderLayout.CENTER);
		scriptEdit.addKeyActionListener(()->{actionScript.setSelected(true); checkData(false);});

		switch (record.getActionType()) {
		case ACTION_ASSIGN: actionAssign.setSelected(true); break;
		case ACTION_ANALOG_VALUE: actionAnalog.setSelected(true); break;
		case ACTION_SIGNAL: actionSignal.setSelected(true); break;
		case ACTION_SCRIPT: actionScript.setSelected(true); break;
		}

		buttonGroup=new ButtonGroup();
		buttonGroup.add(actionAssign);
		buttonGroup.add(actionAnalog);
		buttonGroup.add(actionSignal);
		buttonGroup.add(actionScript);

		/* Icons */

		if (record.getActionMode()==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_ANIMATION_TRAFFIC_LIGHTS.getIcon());
			tabs.setIconAt(1,Images.MODELEDITOR_ELEMENT_PROPERTIES.getIcon());
		} else {
			tabs.setIconAt(0,Images.MODELEDITOR_ELEMENT_PROPERTIES.getIcon());
		}

		/* Dialog starten */

		checkData(false);

		setMinSizeRespectingScreensize(600,500);
		pack();

		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Fügt einen Tasten-Listener zu einem Feld hinzu
	 * @param field	Feld das einen neuen Tasten-Listener erhalten soll
	 * @param work	Wird aufgerufen, wenn in dem Feld eine Taste gedrückt oder losgelassen wird
	 */
	private void addKeyListener(final Component field, final Runnable work) {
		field.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {if (work!=null) work.run(); checkData(false);}
			@Override public void keyReleased(KeyEvent e) {if (work!=null) work.run(); checkData(false);}
			@Override public void keyPressed(KeyEvent e) {if (work!=null) work.run(); checkData(false);}
		});
	}

	/**
	 * Erstellt Listen mit Namen und IDs der Analogwert-Stationen
	 * @param mainSurface	Hauptzeichenfläche
	 * @see #analogIDNames
	 * @see #analogIDs
	 */
	private void buildAnalogIDNames(final ModelSurface mainSurface) {
		final List<String> names=new ArrayList<>();
		final List<Integer> ids=new ArrayList<>();

		for (ModelElement element1: mainSurface.getElements()) {
			if (element1 instanceof ModelElementAnalogValue) {names.add(element1.getName()); ids.add(element1.getId());}
			if (element1 instanceof ModelElementTank) {names.add(element1.getName()); ids.add(element1.getId());}
			if (element1 instanceof ModelElementSub) for (ModelElement element2: ((ModelElementSub)element1).getSubSurface().getElements()) {
				if (element2 instanceof ModelElementAnalogValue) {names.add(element2.getName()); ids.add(element2.getId());}
				if (element2 instanceof ModelElementTank) {names.add(element2.getName()); ids.add(element2.getId());}
			}
		}

		for (int i=0;i<names.size();i++) {
			if (names.get(i).isEmpty()) {
				names.set(i,String.format(Language.tr("Surface.AnalogAssign.Dialog.ID.NoName"),ids.get(i)));
			} else {
				names.set(i,String.format(Language.tr("Surface.AnalogAssign.Dialog.ID.Name"),ids.get(i),names.get(i)));
			}
		}

		analogIDNames=names.toArray(new String[0]);
		analogIDs=ids.stream().mapToInt(Integer::intValue).toArray();
	}

	/**
	 * Fügt eine Variablen zu einer Variablenliste hinzu (wenn die neue Variable nicht sowieso schon enthalten ist)
	 * @param defaultVariablesList	Ausgangs-Variablenliste
	 * @param add	Hinzuzufügender Variablennamen
	 * @return	Neue Variablenliste die den zusätzlichen Variablennamen auf jeden Fall enthält
	 */
	private String[] getExtVariablesList(final String[] defaultVariablesList, final String add) {
		if (add==null || add.trim().isEmpty()) return defaultVariablesList;
		for (String s: defaultVariablesList) if (s.equalsIgnoreCase(add)) return defaultVariablesList;
		final List<String> list=new ArrayList<>(Arrays.asList(defaultVariablesList));
		list.add(add);
		return list.toArray(new String[0]);
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		if (record.getActionMode()==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			/* Auslöser */

			if (triggerCondition.isSelected()) {
				final int error=ExpressionMultiEval.check(conditionEdit.getText(),variables);
				if (error<0) {
					conditionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
				} else {
					conditionEdit.setBackground(Color.RED);
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition.ErrorTitle"),String.format(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition.ErrorInfo"),conditionEdit.getText(),error+1));
						return false;
					}
				}
				final Double D=NumberTools.getPositiveDouble(conditionMinDistanceEdit,true);
				if (D==null) {
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition.MinDistance.ErrorTitle"),String.format(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Condition.MinDistance.ErrorInfo"),conditionMinDistanceEdit.getText()));
						return false;
					}
				}
			} else {
				conditionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
				conditionMinDistanceEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}

			if (triggerThreshold.isSelected()) {
				final int error=ExpressionCalc.check(thresholdExpressionEdit.getText(),variables);
				if (error<0) {
					thresholdExpressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
				} else {
					thresholdExpressionEdit.setBackground(Color.RED);
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdExpression.ErrorTitle"),String.format(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdExpression.ErrorInfo"),thresholdExpressionEdit.getText(),error+1));
						return false;
					}
				}
				final Double D=NumberTools.getDouble(thresholdValueEdit,true);
				if (D==null) {
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdValue.ErrorTitle"),String.format(Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.ThresholdValue.ErrorInfo"),thresholdValueEdit.getText()));
						return false;
					}
				}
			} else {
				thresholdExpressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
				thresholdValueEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}

			if (triggerSignal.isSelected()) {
				if (triggerSignalName.getSelectedIndex()<0) {
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Signal.ErrorTitle"),Language.tr("Surface.Action.Dialog.Edit.Tabs.Trigger.Signal.ErrorInfo"));
						return false;
					}
				}
			}
		}

		/* Aktion */

		if (actionAssign.isSelected()) {
			final String varName=assignVariableEdit.getText().trim();
			boolean varOk=ExpressionCalc.checkVariableName(varName);
			for (String add: RunModel.additionalVariables) varOk=varOk && !varName.equalsIgnoreCase(add);
			if (varOk) {
				assignVariableEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				ok=false;
				assignVariableEdit.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Variable.ErrorTitle"),String.format(String.format(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Variable.ErrorInfo"),varName)));
					return false;
				}
			}
			final int error=ExpressionCalc.check(assignExpressionEdit.getText(),getExtVariablesList(variables,varName));
			if (error<0) {
				assignExpressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				assignExpressionEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Expression.ErrorTitle"),String.format(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Assign.Expression.ErrorInfo"),assignExpressionEdit.getText(),error+1));
					return false;
				}
			}
		} else {
			assignVariableEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			assignExpressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (actionAnalog.isSelected()) {
			if (analogElementCombo.getSelectedIndex()<0) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Element.ErrorTitle"),Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Element.ErrorInfo"));
					return false;
				}
			}
			final int error=ExpressionCalc.check(analogExpressionEdit.getText(),variables);
			if (error<0) {
				analogExpressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				analogExpressionEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Expression.ErrorTitle"),String.format(Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Analog.Expression.ErrorInfo"),analogExpressionEdit.getText(),error+1));
					return false;
				}
			}
		} else {
			analogExpressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		if (actionSignal.isSelected()) {
			if (signalNameEdit.getText().isEmpty()) {
				signalNameEdit.setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Signal.ErrorTitle"),Language.tr("Surface.Action.Dialog.Edit.Tabs.Action.Signal.ErrorInfo"));
					return false;
				}
			} else {
				signalNameEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		if (actionScript.isSelected()) {
			if (showErrorMessages) {
				if (!scriptEdit.checkData()) return false;
			}
		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	@Override
	protected void storeData() {
		/* Aktiv */
		record.setActive(activeCheckBox.isSelected());

		if (record.getActionMode()==ModelElementActionRecord.ActionMode.TRIGGER_AND_ACTION) {
			/* Auslöser */

			if (triggerCondition.isSelected()) {
				record.setCondition(conditionEdit.getText());
				record.setConditionMinDistance(NumberTools.getPositiveDouble(conditionMinDistanceEdit,true));
			}

			if (triggerThreshold.isSelected()) {
				record.setThresholdExpression(thresholdExpressionEdit.getText());
				record.setThresholdValue(NumberTools.getDouble(thresholdValueEdit,true));
				if (thresholdDirectionUp.isSelected()) record.setThresholdDirection(ModelElementActionRecord.ThresholdDirection.THRESHOLD_UP); else record.setThresholdDirection(ModelElementActionRecord.ThresholdDirection.THRESHOLD_DOWN);
			}

			if (triggerSignal.isSelected()) {
				record.setConditionSignal((String)triggerSignalName.getSelectedItem());
			}
		}

		/* Aktion */

		if (actionAssign.isSelected()) {
			record.setAssignVariable(assignVariableEdit.getText().trim());
			record.setAssignExpression(assignExpressionEdit.getText());
		}

		if (actionAnalog.isSelected()) {
			record.setAnalogID(analogIDs[analogElementCombo.getSelectedIndex()]);
			record.setAnalogValue(analogExpressionEdit.getText());
		}

		if (actionSignal.isSelected()) {
			record.setSignalName(signalNameEdit.getText());
		}

		if (actionScript.isSelected()) {
			record.setScript(scriptEdit.getScript());
			switch (scriptEdit.getMode()) {
			case Javascript: record.setScriptMode(ModelElementActionRecord.ScriptMode.Javascript); break;
			case Java: record.setScriptMode(ModelElementActionRecord.ScriptMode.Java); break;
			}
		}
	}
}