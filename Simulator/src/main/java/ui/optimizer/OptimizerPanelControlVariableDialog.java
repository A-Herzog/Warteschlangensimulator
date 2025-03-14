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
package ui.optimizer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.w3c.dom.Document;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.ModelChanger;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.optimizer.OptimizerSetup.ControlVariable;
import ui.parameterseries.ParameterCompareInputValuesTemplates;
import ui.parameterseries.ParameterCompareSetupValueInput;
import ui.statistics.StatisticViewerFastAccessDialog;

/**
 * Diese Klasse zeigt einen Dialog zum Bearbeiten einer Kontrollvariable an
 * @author Alexander Herzog
 * @see OptimizerPanel
 */
public class OptimizerPanelControlVariableDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 8393259199855388278L;

	/**
	 * Editor-Modell, aus dem ein xml-Modell f�r die Auswahl eines xml-Elements abgeleitet werden kann
	 */
	private final EditModel model;

	/**
	 * Typ der Kontrollvariable
	 * @see #cardsPanel
	 */
	private final JComboBox<String> modeCombo;

	/**
	 * Panel in das die Editoren f�r die Kontrollvariable in Abh�ngigkeit vom Typ eingeblendet werden
	 */
	private JPanel cardsPanel;

	/**
	 * Layout f�r das Panel mit den Kontrollvariable-Editoren
	 * @see #cardsPanel
	 */
	private CardLayout cardsLayout;

	/**
	 * Anzahl an Bedienern in einer Ressource
	 */
	private final JComboBox<String> resCombo;

	/**
	 * Wert einer Variable
	 */
	private final JComboBox<String> varCombo;

	/**
	 * Wert eines Eintrags in der globalen Zuordnung
	 */
	private final JComboBox<String> mapCombo;

	/**
	 * XML-Element als Kontrollvariable
	 */
	private final JTextField xmlTagEdit;

	/**
	 * Auswahl des XML-Elements
	 */
	private final JButton xmlTagButton;

	/**
	 * Wie soll das XML-Element interpretiert werden?
	 * @see ui.ModelChanger.Mode#MODE_XML
	 */
	private final JComboBox<String> xmlMode;

	/**
	 * Minimalwert f�r Kontrollvariable
	 */
	private final JTextField rangeFrom;

	/**
	 * Maximalwert f�r Kontrollvariable
	 */
	private final JTextField rangeTo;

	/**
	 * Muss die Kontrollvariable ganzzahlig sein?
	 */
	private final JCheckBox valueIsInteger;

	/**
	 * Startwer f�r die Kontrollvariable
	 */
	private final JTextField startValue;

	/**
	 * Ausgabe von Informationen zur Kontrollvariable und ihrem Startwert
	 * @see #updateInfo()
	 */
	private final JLabel startValueInfo;

	/**
	 * Konstruktor der Klasse <code>OptimizerPanelControlVariableDialog</code>
	 * @param owner	�bergeordnetes Element
	 * @param model	Editor-Modell, aus dem ein xml-Modell f�r die Auswahl eines xml-Elements abgeleitet werden kann
	 * @param controlVariable	Bisherige Kontrollvariable (wird nicht ver�ndert); kann beim Anlegen einer neuen Kontrollvariable auch <code>null</code> sein
	 * @param help	Runnable, das aufgerufen wird, wenn der Nutzer auf "Hilfe" klickt
	 */
	public OptimizerPanelControlVariableDialog(final Component owner, final EditModel model, ControlVariable controlVariable, final Runnable help) {
		super(owner,Language.tr("Optimizer.ControlVariableEdit.Title"));
		this.model=model;

		JPanel content=createGUI(help);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		JPanel line, sub;

		/* Initialisieren */

		modeCombo=getComboBox(content,Language.tr("Optimizer.Tab.Target.Mode"),new String[]{
				Language.tr("Batch.Parameter.Type.Resource"),
				Language.tr("Batch.Parameter.Type.Variable"),
				Language.tr("Batch.Parameter.Type.Map"),
				Language.tr("Batch.Parameter.Type.XML")
		});
		modeCombo.setRenderer(new IconListCellRenderer(new Images[]{
				Images.PARAMETERSERIES_INPUT_MODE_RESOURCE,
				Images.PARAMETERSERIES_INPUT_MODE_VARIABLE,
				Images.SCRIPT_MAP,
				Images.PARAMETERSERIES_INPUT_MODE_XML
		}));

		content.add(cardsPanel=new JPanel());
		cardsPanel.setLayout(cardsLayout=new CardLayout());

		/* Ressource */

		cardsPanel.add(sub=new JPanel(),"0");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		resCombo=getComboBox(sub,Language.tr("Batch.Parameter.Resource.Label"),OptimizerSetup.getResourceNames(model));
		resCombo.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildResourceTypeIcons(OptimizerSetup.getResourceNames(model),model)));
		if (resCombo.getItemCount()>0) resCombo.setSelectedIndex(0);
		resCombo.addActionListener(e->updateInfo());

		/* Variable */

		cardsPanel.add(sub=new JPanel(),"1");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		varCombo=getComboBox(sub,Language.tr("Batch.Parameter.Variable.Label"),OptimizerSetup.getGlobalVariables(model));
		if (varCombo.getItemCount()>0) varCombo.setSelectedIndex(0);
		varCombo.addActionListener(e->updateInfo());

		/* Globale Zuordnung */

		cardsPanel.add(sub=new JPanel(),"2");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));
		mapCombo=getComboBox(sub,Language.tr("Batch.Parameter.Map.Label"),new ArrayList<>(model.globalMapInitial.keySet()).toArray(String[]::new));
		if (mapCombo.getItemCount()>0) mapCombo.setSelectedIndex(0);
		mapCombo.addActionListener(e->updateInfo());

		/* XML-Feld */

		cardsPanel.add(sub=new JPanel(),"3");
		sub.setLayout(new BoxLayout(sub,BoxLayout.PAGE_AXIS));

		sub.add(line=new JPanel(new BorderLayout()));
		line.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		line.add(new JLabel(Language.tr("Optimizer.Tab.Target.Type.XMLElement")+": "),BorderLayout.WEST);
		line.add(xmlTagEdit=new JTextField(),BorderLayout.CENTER);
		xmlTagEdit.setEditable(false);
		line.add(xmlTagButton=new JButton(Language.tr("Optimizer.Tab.Target.Type.XMLElement.Button")),BorderLayout.EAST);
		xmlTagButton.setToolTipText(Language.tr("Optimizer.Tab.Target.Type.XMLElement.Button.Hint"));
		xmlTagButton.setIcon(Images.EDIT_ADD.getIcon());
		xmlTagButton.addActionListener(e->showXMLPopup());
		xmlMode=getComboBox(sub,Language.tr("Optimizer.ControlVariableEdit.Mode"),ModelChanger.XML_ELEMENT_MODES);
		xmlMode.addActionListener(e->updateInfo());

		/* Bereich von bis */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Optimizer.ControlVariableEdit.ValidRange.From")+" "));
		line.add(rangeFrom=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(rangeFrom);
		rangeFrom.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);	}
		});
		line.add(new JLabel(" "+Language.tr("Optimizer.ControlVariableEdit.ValidRange.To")+" "));
		line.add(rangeTo=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(rangeTo);
		rangeTo.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		line.add(valueIsInteger=new JCheckBox(Language.tr("Optimizer.ControlVariableEdit.OnlyIntegerValues")));
		valueIsInteger.addActionListener(e->checkData(false));

		/* Startwert */

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(new JLabel(Language.tr("Optimizer.ControlVariableEdit.InitialValue")+":"));
		line.add(startValue=new JTextField(10));
		ModelElementBaseDialog.addUndoFeature(startValue);
		startValue.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);	}
		});
		line.add(Box.createHorizontalStrut(5));
		line.add(startValueInfo=new JLabel());

		/* Laden laden */

		if (controlVariable==null) controlVariable=new ControlVariable();

		modeCombo.addActionListener(e->{
			cardsLayout.show(cardsPanel,""+modeCombo.getSelectedIndex());
			if (modeCombo.getSelectedIndex()==0) valueIsInteger.setSelected(true);
			valueIsInteger.setEnabled(modeCombo.getSelectedIndex()!=0);
			updateInfo();
		});
		switch (controlVariable.mode) {
		case MODE_RESOURCE: modeCombo.setSelectedIndex(0); break;
		case MODE_VARIABLE: modeCombo.setSelectedIndex(1); break;
		case MODE_MAP: modeCombo.setSelectedIndex(2); break;
		case MODE_XML: modeCombo.setSelectedIndex(3); break;
		}

		xmlTagEdit.setText(controlVariable.tag);
		xmlMode.setSelectedIndex(Math.min(xmlMode.getModel().getSize()-1,Math.max(0,controlVariable.xmlMode)));
		rangeFrom.setText(NumberTools.formatNumber(controlVariable.rangeFrom));
		rangeTo.setText(NumberTools.formatNumber(controlVariable.rangeTo));
		valueIsInteger.setSelected(controlVariable.integerValue || controlVariable.mode==ModelChanger.Mode.MODE_RESOURCE);
		startValue.setText(NumberTools.formatNumber(controlVariable.start));

		checkData(false);
		setMinSizeRespectingScreensize(600,0);
		pack();
		setLocationRelativeTo(this.owner);
	}

	/**
	 * Erzeugt eine Auswahlbox.
	 * @param sub	Elternelement
	 * @param label	Beschriftung der Auswahlbox
	 * @param content	Auswahloptionen
	 * @return	Neue Auswahlbox (ist bereits in das Elternelement eingef�gt)
	 */
	private JComboBox<String> getComboBox(final JPanel sub, final String label, final String[] content) {
		final JPanel line=new JPanel(new FlowLayout(FlowLayout.LEFT));
		sub.add(line);

		final JLabel comboLabel;
		final JComboBox<String> combo;

		if (label!=null) line.add(comboLabel=new JLabel(label+":")); else comboLabel=null;
		line.add(combo=new JComboBox<>(content));
		if (comboLabel!=null) comboLabel.setLabelFor(combo);

		return combo;
	}

	/**
	 * Aktualisiert die Ausgabe von Informationen zur Kontrollvariable und ihrem Startwert.
	 * @see #startValueInfo
	 */
	private void updateInfo() {
		switch (modeCombo.getSelectedIndex()) {
		case 0: /* Ressource */
			final int resCount=OptimizerSetup.getResourceCount(model,(String)resCombo.getSelectedItem());
			if (resCount>=0) startValueInfo.setText(String.format(Language.tr("Optimizer.ControlVariableEdit.Info.Resource"),resCount)); else startValueInfo.setText("");
			break;
		case 1: /* Variable */
			final String varStart=OptimizerSetup.getGlobalVariablesStartValues(model,(String)varCombo.getSelectedItem());
			if (varStart!=null) startValueInfo.setText(String.format(Language.tr("Optimizer.ControlVariableEdit.Info.Variable"),varStart)); else startValueInfo.setText("");
			break;
		case 2: /* Zuordnung */
			final Object obj=model.globalMapInitial.get(mapCombo.getSelectedItem());
			String mapStart=null;
			if (obj instanceof Integer) mapStart=""+obj;
			if (obj instanceof Long) mapStart=""+obj;
			if (obj instanceof Double) mapStart=NumberTools.formatNumberMax((Double)obj);
			if (obj instanceof String) mapStart=(String)obj;
			if (mapStart!=null) startValueInfo.setText(String.format(Language.tr("Optimizer.ControlVariableEdit.Info.Map"),mapStart)); else startValueInfo.setText("");
			break;
		case 3: /* XML-Feld */
			final String info=ModelChanger.getValue(model,xmlTagEdit.getText(),xmlMode.getSelectedIndex());
			if (info!=null) startValueInfo.setText(String.format(Language.tr("Optimizer.ControlVariableEdit.Info.XML"),info)); else startValueInfo.setText("");
			break;
		}
	}

	/**
	 * Pr�ft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> �bergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zur�ck, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessages) {
		boolean ok=true;

		switch (modeCombo.getSelectedIndex()) {
		case 0:
			if (resCombo.getSelectedIndex()<0) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.NoResourceTitle"),Language.tr("Optimizer.ControlVariableEdit.Error.NoResourceInfo"));
					return false;
				}
			}
			break;
		case 1:
			if (varCombo.getSelectedIndex()<0) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.NoVariableTitle"),Language.tr("Optimizer.ControlVariableEdit.Error.NoVariableInfo"));
					return false;
				}
			}
			break;
		case 2:
			if (mapCombo.getSelectedIndex()<0) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.NoMapTitle"),Language.tr("Optimizer.ControlVariableEdit.Error.NoMapInfo"));
					return false;
				}
			}
			break;
		case 3:
			if (xmlTagEdit.getText().isBlank()) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.NoXMLTitle"),Language.tr("Optimizer.ControlVariableEdit.Error.NoXMLInfo"));
					return false;
				}
			}
			break;
		}

		if (valueIsInteger.isSelected()) {
			int min=0;
			int max=0;
			Integer I;
			I=NumberTools.getInteger(rangeFrom,true);
			if (I==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.LowerBoundInteger"));
					return false;
				}
			} else {
				min=I;
			}
			I=NumberTools.getInteger(rangeTo,true);
			if (I==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.UpperBoundInteger"));
					return false;
				}
			} else {
				max=I;
				if (max<min) {
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.InversLimits"));
						return false;
					}
				}
			}
			I=NumberTools.getInteger(startValue,true);
			if (I==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.InvalidInitialValue"),Language.tr("Optimizer.ControlVariableEdit.Error.InvalidInitialValue.InfoInteger"));
					return false;
				}
			} else {
				if (I<min || I>max) {
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.InvalidInitialValue"),Language.tr("Optimizer.ControlVariableEdit.Error.InitialValueNotInRange"));
						return false;
					}
				}
			}
		} else {
			double min=0;
			double max=0;
			Double D;
			D=NumberTools.getDouble(rangeFrom,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.LowerBound"));
					return false;
				}
			} else {
				min=D;
			}
			D=NumberTools.getDouble(rangeTo,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.UpperBound"));
					return false;
				}
			} else {
				max=D;
				if (max<min) {
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange"),Language.tr("Optimizer.Tab.Target.Value.Range.InvalidRange.InversLimits"));
						return false;
					}
				}
			}
			D=NumberTools.getDouble(startValue,true);
			if (D==null) {
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.InvalidInitialValue"),Language.tr("Optimizer.ControlVariableEdit.Error.InvalidInitialValue.Info"));
					return false;
				}
			} else {
				if (D<min || D>max) {
					ok=false;
					if (showErrorMessages) {
						MsgBox.error(this,Language.tr("Optimizer.ControlVariableEdit.Error.InvalidInitialValue"),Language.tr("Optimizer.ControlVariableEdit.Error.InitialValueNotInRange"));
						return false;
					}
				}
			}

		}

		return ok;
	}

	@Override
	protected boolean checkData() {
		return checkData(true);
	}

	/**
	 * Wurde der Dialog per "Ok" geschlossen, so kann �ber diese Funktion eine neue Kontrollvariable
	 * auf Basis der Einstellungen im Dialog ermittelt werden
	 * @return	Neue Kontrollvariable
	 */
	public ControlVariable getControlVariable() {
		final ControlVariable controlVariable=new ControlVariable();

		Double D;

		switch (modeCombo.getSelectedIndex()) {
		case 0:
			controlVariable.mode=ModelChanger.Mode.MODE_RESOURCE;
			if (resCombo.getSelectedIndex()<0) controlVariable.tag=""; else controlVariable.tag=(String)resCombo.getSelectedItem();
			break;
		case 1:
			controlVariable.mode=ModelChanger.Mode.MODE_VARIABLE;
			if (varCombo.getSelectedIndex()<0) controlVariable.tag=""; else controlVariable.tag=(String)varCombo.getSelectedItem();
			break;
		case 2:
			controlVariable.mode=ModelChanger.Mode.MODE_MAP;
			if (mapCombo.getSelectedIndex()<0) controlVariable.tag=""; else controlVariable.tag=(String)mapCombo.getSelectedItem();
			break;
		case 3:
			controlVariable.mode=ModelChanger.Mode.MODE_XML;
			controlVariable.tag=xmlTagEdit.getText();
			controlVariable.xmlMode=xmlMode.getSelectedIndex();
			break;
		}
		D=NumberTools.getDouble(rangeFrom,true);
		if (D!=null) controlVariable.rangeFrom=D;
		D=NumberTools.getDouble(rangeTo,true);
		if (D!=null) controlVariable.rangeTo=D;
		controlVariable.integerValue=valueIsInteger.isSelected();
		D=NumberTools.getDouble(startValue,true);
		if (D!=null) controlVariable.start=D;

		return controlVariable;
	}

	/**
	 * Zeigt das Popupmen� zur Auswalh eines XML-Eintrags an.
	 */
	private void showXMLPopup() {
		final JPopupMenu popup=new JPopupMenu();

		final JMenuItem item=new JMenuItem(Language.tr("Optimizer.Tab.Target.Type.XMLElement.Button.Direct"));
		item.setIcon(Images.EDIT_ADD.getIcon());
		item.addActionListener(e->addByDialog());
		popup.add(item);

		popup.addSeparator();

		final ParameterCompareInputValuesTemplates templates=new ParameterCompareInputValuesTemplates(model);
		final Set<ParameterCompareInputValuesTemplates.Mode> modes=new HashSet<>(Arrays.asList(ParameterCompareInputValuesTemplates.Mode.values()));
		modes.remove(ParameterCompareInputValuesTemplates.Mode.RESOURCES); /* K�nnen direkt gew�hlt werden, muss nicht �ber xml-Tag laufen. */
		templates.addToMenu(modes,popup,input->addByTemplate(input));

		popup.show(xmlTagButton,0,xmlTagButton.getHeight());
	}

	/**
	 * Zeigt einen Dialog zur Auswahl eines XML-Eintrags an.
	 * @see #showXMLPopup()
	 */
	private void addByDialog() {
		final Document xmlDoc=model.saveToXMLDocument();
		if (xmlDoc==null) return;
		final StatisticViewerFastAccessDialog dialog=new StatisticViewerFastAccessDialog(owner,xmlDoc,()->Help.topicModal(OptimizerPanelControlVariableDialog.this,"Optimizer"),true);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		xmlTagEdit.setText(dialog.getXMLSelector());
		updateInfo();
	}

	/**
	 * Konfiguriert {@link #xmlTagEdit} gem�� einer Vorlage.
	 * @param template	Zu verwendende Vorlage
	 * @see #showXMLPopup()
	 */
	private void addByTemplate(final ParameterCompareSetupValueInput template) {
		xmlTagEdit.setText(template.getTag());
		updateInfo();
	}
}