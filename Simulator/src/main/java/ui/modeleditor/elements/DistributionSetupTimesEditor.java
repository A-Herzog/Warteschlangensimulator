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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;

/**
 * Stellt einen Verteilungs- bzw. Ausdruckeditor für die Rüstzeiten dar.
 * @author Alexander Herzog
 */
public class DistributionSetupTimesEditor extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -734275539715521114L;

	/** Bezeichner für die Verteilungseditor {@link CardLayout}-Karte */
	private static final String cardDistribution="Distribution";
	/** Bezeichner für die Formeleingabe {@link CardLayout}-Karte */
	private static final String cardExpression="Expression";

	/** Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden) */
	final EditModel model;
	/** Element vom Typ <code>ModelSurface</code> (wird benötigt, um die Liste der Kundentypen oder Stationen auszulesen) */
	final ModelSurface surface;
	/** Nur-Lese-Status */
	private final boolean readOnly;

	/** Im Konstruktor übergebenes Datenobjekt in das bei {@link #storeData()} die Daten zurückgeschrieben werden */
	private final DistributionSystemSetupTimes dataOriginal;
	/** Temporäres Datenobjekt für die Arbeit in dem Panel; bei Erfolg werden die Daten am Ende in {@link #dataOriginal} zurückgeschrieben */
	final DistributionSystemSetupTimes data;
	/** Namen der Untertypen (Kundentypen) */
	final String[] clientTypes;

	/** Index des zuletzt in {@link #typeCombo} ausgewählten Eintrags */
	private int typeLast;
	/** Auswahl des aktiven Untertypen-Eintrags */
	private final JComboBox<String> typeCombo;
	/** Lokalen Wert für den aktuellen Untertyp verwenden? */
	private final JCheckBox activeCheckBox;
	/** Schaltfläche zum Laden von Kundentypdaten */
	private final JButton loadButton;
	/** Schaltfläche für Rüstzeiten-Assistent */
	private final JButton assistantButton;
	/** Zeigt den Infotext an, von welchem Kundentyp zu welchem Kundentyp die aktuelle Rüstzeit gilt */
	private final JLabel infoLabel;
	/** Auswahl: Verteilung oder Rechenausdruck */
	private final JComboBox<String> modeSelect;
	/** Panel das Verteilungseditor und Formel-Eingabefeld vorhält */
	private final JPanel cards;
	/** Verteilungseditor */
	private final JDistributionPanel distributionPanel;
	/** Panel für das Formel-Eingabefeld */
	private final JPanel expressionLines;
	/** Eingabefeld für die Formel */
	private final JTextField expressionEdit;

	/**
	 * Listener, die im Falle einer Nutzereingabe benachrichtigt werden sollen
	 * @see #fireUserChangeListener()
	 */
	private List<ActionListener> userChangeListeners;

	/**
	 * Konstruktor der Klasse <code>DistributionSetupTimesEditor</code>
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Element vom Typ <code>ModelSurface</code> (wird benötigt, um die Liste der Kundentypen oder Stationen auszulesen)
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen, oder ob sich das System im Read-Only-Modus befindet
	 * @param data	Zu bearbeitende Daten (das Objekt wird durch den Aufruf von <code>storeData</code> aktualisiert)
	 */
	public DistributionSetupTimesEditor(final EditModel model, final ModelSurface surface, final boolean readOnly, final DistributionSystemSetupTimes data) {
		super();

		userChangeListeners=new ArrayList<>();

		/* Daten vorbereiten */
		this.model=model;
		this.surface=surface;
		this.readOnly=readOnly;
		dataOriginal=data;
		this.data=data.clone();
		clientTypes=surface.getClientTypes().toArray(new String[0]);

		/* GUI aufbauen */
		setLayout(new BorderLayout());
		JPanel panel, line;
		JLabel label;

		add(panel=new JPanel(),BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

		panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(label=new JLabel(Language.tr("Surface.Process.Dialog.SetupTimes.Type")+":"));
		line.add(typeCombo=new JComboBox<>(getTypeItems()));
		final Dimension d=typeCombo.getPreferredSize();
		d.width=Math.min(450,d.width);
		typeCombo.setPreferredSize(d);
		label.setLabelFor(typeCombo);

		if (clientTypes.length>0) {

			line.add(activeCheckBox=new JCheckBox("<html><body><b>"+Language.tr("Surface.Process.Dialog.SetupTimes.Active")+"</b></body></html>"));
			activeCheckBox.setEnabled(!readOnly);
			activeCheckBox.addActionListener(e->{
				activeClientTypeChanged();
				fireUserChangeListener();
			});
			line.add(Box.createHorizontalGlue());
			line.add(loadButton=new JButton(Language.tr("ClientTypeLoader.Button"),Images.GENERAL_LOAD.getIcon()));
			loadButton.setToolTipText(Language.tr("ClientTypeLoader.Title"));
			loadButton.setEnabled(!readOnly);
			loadButton.addActionListener(e->loadClientTypeData());

			line.add(assistantButton=new JButton(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Button"),Images.MODELEDITOR_ELEMENT_PROCESS_PAGE_SETUP_ASSISTANT.getIcon()));
			assistantButton.setEnabled(!readOnly);
			assistantButton.addActionListener(e->showSetupTimesAssistant());

			panel.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
			line.add(infoLabel=new JLabel());
			line.add(modeSelect=new JComboBox<>(new String[]{
					Language.tr("Surface.DistributionByClientTypeEditor.DefineByDistribution"),
					Language.tr("Surface.DistributionByClientTypeEditor.DefineByExpression")
			}));
			modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
					Images.MODE_DISTRIBUTION,
					Images.MODE_EXPRESSION
			}));
			infoLabel.setLabelFor(modeSelect);
			modeSelect.setEnabled(!readOnly);

			add(cards=new JPanel(new CardLayout()),BorderLayout.CENTER);
			cards.add(distributionPanel=new JDistributionPanel(null,3600,!readOnly,s->toExpression(s)) {
				private static final long serialVersionUID = -8375312389773855243L;
				@Override
				protected void changedByUser() {
					activeCheckBox.setSelected(true);
					activeClientTypeChanged();
					fireUserChangeListener();
				}
			},cardDistribution);

			cards.add(panel=new JPanel(new FlowLayout(FlowLayout.LEFT)),cardExpression);
			panel.add(expressionLines=new JPanel());
			expressionLines.setLayout(new BoxLayout(expressionLines,BoxLayout.PAGE_AXIS));
			Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DistributionByClientTypeEditor.Expression")+":","");
			line=(JPanel)obj[0];
			expressionLines.add(line);
			expressionEdit=(JTextField)obj[1];
			line.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,true,model,surface),BorderLayout.EAST);
			expressionEdit.setEditable(!readOnly);
			expressionEdit.addKeyListener(new KeyListener(){
				@Override public void keyTyped(KeyEvent e) {fireUserChangeListener(); activeCheckBox.setSelected(true); checkExpression(); activeClientTypeChanged(); fireUserChangeListener();}
				@Override public void keyPressed(KeyEvent e) {fireUserChangeListener(); activeCheckBox.setSelected(true); checkExpression(); activeClientTypeChanged(); fireUserChangeListener();}
				@Override public void keyReleased(KeyEvent e) {fireUserChangeListener(); activeCheckBox.setSelected(true); checkExpression(); activeClientTypeChanged(); fireUserChangeListener();}
			});

			/* Verarbeitung starten */
			typeLast=-1;
			typeCombo.addActionListener(e->activeClientTypeChanged());
			modeSelect.addActionListener(e->{
				setCard((modeSelect.getSelectedIndex()==0)?cardDistribution:cardExpression);
				fireUserChangeListener();
			});

			typeCombo.setSelectedIndex(0);
		} else {
			activeCheckBox=null;
			loadButton=null;
			assistantButton=null;
			infoLabel=null;
			modeSelect=null;
			cards=null;
			distributionPanel=null;
			expressionLines=null;
			expressionEdit=null;
		}
	}

	/**
	 * Stellt den angegebenen Rechenausdruck ein.
	 * @param expression	Rechenausdruck
	 */
	private void toExpression(final String expression) {
		modeSelect.setSelectedIndex(1);
		setCard(cardExpression);
		expressionEdit.setText(expression);
		fireUserChangeListener(); activeCheckBox.setSelected(true); checkExpression(); activeClientTypeChanged(); fireUserChangeListener();
	}

	/**
	 * Liefert eine Auflistung aller möglichen Rüstzeittypen.
	 * @return	Auflistung aller möglichen Rüstzeittypen
	 * @see #typeCombo
	 */
	private String[] getTypeItems() {
		final String[] list=new String[clientTypes.length*clientTypes.length];

		final StringBuilder sb=new StringBuilder();
		int index=0;
		for (String type1: clientTypes) for (String type2: clientTypes) {
			sb.setLength(0);
			sb.append(type1);
			sb.append(" -> ");
			sb.append(type2);
			list[index++]=sb.toString();
		}

		return list;
	}

	/**
	 * Fügt einen Listener zu der Liste der im Falle einer Nutzereingabe zu benachrichtigenden Objekte hinzu
	 * @param actionListener	Neuer Listener
	 */
	public void addUserChangeListener(final ActionListener actionListener) {
		if (userChangeListeners.indexOf(actionListener)<0) userChangeListeners.add(actionListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer Nutzereingabe zu benachrichtigenden Objekte
	 * @param actionListener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich entfernt werden konnte.
	 */
	public boolean removeUserChangeListener(final ActionListener actionListener) {
		return userChangeListeners.remove(actionListener);
	}

	/**
	 * Löst alle Listener, die im Falle einer Nutzereingabe benachrichtigt werden sollen, aus.
	 * @see #userChangeListeners
	 */
	private void fireUserChangeListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"");
		for (ActionListener listener: userChangeListeners) listener.actionPerformed(event);
	}

	/**
	 * Schreibt die über die GUI vorgenommenen Änderungen in das dem Konstruktor übergebene Datenobjekt zurück.
	 */
	public void storeData() {
		activeClientTypeChanged();
		dataOriginal.setData(data);
	}

	/**
	 * Gibt an, ob mindestens eine Rüstzeit hinterlegt ist
	 * @return	Gibt <code>true</code> zurück, wenn mindestens eine Rüstzeit vorhanden ist
	 */
	public boolean isActive() {
		return data.isActive();
	}

	/**
	 * Stellt die aktive Karte in {@link #cards} ein,
	 * d.h. wechselt zwischen Verteilungseditor und Formeleingabe.
	 * @param cardName	Name der anzuzeigenden Karte
	 * @see #cardDistribution
	 * @see #cardExpression
	 * @see #cards
	 * @see #modeSelect
	 */
	private void setCard(final String cardName) {
		((CardLayout)cards.getLayout()).show(cards,cardName);

		SwingUtilities.invokeLater(()->{
			final int h=expressionLines.getSize().height;
			final int w=cards.getSize().width;
			if (h>0 && w>0) {
				expressionLines.setPreferredSize(new Dimension(w-15,h));
				expressionLines.setSize(new Dimension(w-15,h));
			}
		});
	}

	/**
	 * Prüft den eingegeben Rechenausdruck.
	 */
	private void checkExpression() {
		if (expressionEdit.getText().trim().isEmpty()) {
			expressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			return;
		}

		final int error=ExpressionCalc.check(expressionEdit.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true));
		if (error>=0) expressionEdit.setBackground(Color.red); else expressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
	}

	/**
	 * Wird aufgerufen, wenn ein anderer darzustellender Untertyp ausgewählt wurde.
	 * @see #typeLast
	 * @see #typeCombo
	 */
	private void activeClientTypeChanged() {
		if (clientTypes.length==0) return;

		if (typeLast>=0 && !readOnly) {
			final String type1=clientTypes[typeLast/clientTypes.length];
			final String type2=clientTypes[typeLast%clientTypes.length];
			if (activeCheckBox.isSelected()) {
				if (modeSelect.getSelectedIndex()==0) {
					data.set(type1,type2,distributionPanel.getDistribution());
				} else {
					data.set(type1,type2,expressionEdit.getText());
				}
			} else {
				data.remove(type1,type2);
			}
		}

		typeLast=typeCombo.getSelectedIndex();
		String type1=clientTypes[typeLast/clientTypes.length];
		String type2=clientTypes[typeLast%clientTypes.length];
		if (type1.length()>55) type1=type1.substring(0,50);
		if (type2.length()>55) type2=type2.substring(0,50);
		infoLabel.setText(String.format(Language.tr("Surface.Process.Dialog.SetupTimes.Info"),type1,type2));
		final Object obj=data.get(type1,type2);
		activeCheckBox.setSelected(obj!=null);
		if (obj==null) {
			setCard(cardDistribution);
			modeSelect.setSelectedIndex(0);
			distributionPanel.setDistribution(new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
			expressionEdit.setText("");
			checkExpression();
		} else {
			if (obj instanceof String) {
				setCard(cardExpression);
				modeSelect.setSelectedIndex(1);
				distributionPanel.setDistribution(new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
				expressionEdit.setText((String)obj);
				checkExpression();
			}
			if (obj instanceof AbstractRealDistribution) {
				setCard(cardDistribution);
				modeSelect.setSelectedIndex(0);
				distributionPanel.setDistribution((AbstractRealDistribution)obj);
				expressionEdit.setText("");
				checkExpression();
			}
		}
	}

	/**
	 * Schaltfläche "Kundendaten laden"
	 * @see #loadButton
	 */
	private void loadClientTypeData() {
		final File file=ClientTypeLoader.selectFile(this);
		if (file==null) return;

		final Map<String,Map<String,Object>> newClientTypes=new ClientTypeLoader(file).getSetupTimesClientTypes();

		if (newClientTypes.size()==0) {
			MsgBox.error(this,Language.tr("ClientTypeLoader.Title"),String.format(Language.tr("ClientTypeLoader.LoadError"),file.toString()));
			return;
		}

		activeClientTypeChanged();

		Map<String,Map<String,Object>> oldClientTypes=new HashMap<>();
		for (var name1: clientTypes) for (var name2: clientTypes) {
			final Object obj=data.get(name1,name2);
			if (obj!=null) {
				if (!oldClientTypes.containsKey(name1)) oldClientTypes.put(name1,new HashMap<>());
				oldClientTypes.get(name1).put(name2,obj);
			}
		}

		final ClientTypeLoaderDialog dialog=new ClientTypeLoaderDialog(this);
		dialog.initSetupTimes(Arrays.asList(clientTypes),oldClientTypes,newClientTypes);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		for (Map.Entry<String,Map<String,Object>> entry1: newClientTypes.entrySet()) {
			for (Map.Entry<String,Object> entry2: entry1.getValue().entrySet()) {
				final Object obj=entry2.getValue();
				if (obj instanceof AbstractRealDistribution) data.set(entry1.getKey(),entry2.getKey(),(AbstractRealDistribution)obj);
				if (obj instanceof String) data.set(entry1.getKey(),entry2.getKey(),(String)obj);
			}
		}

		typeLast=-1;
		activeClientTypeChanged();
	}

	/**
	 * Schaltfläche "Rüstzeiten-Assistent"
	 * @see #assistantButton
	 */
	private void showSetupTimesAssistant() {
		final DistributionSetupTimesEditorAssistantDialog dialog=new DistributionSetupTimesEditorAssistantDialog(this);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			typeLast=-1;
			activeClientTypeChanged();
		}
	}

	/**
	 * Prüft, ob für die angegebene Anzahl an Kundentypen genug Speicher zur Anzeige der Combobox verfügbar ist.
	 * @param numberOfClientTypes	Anzahl an Kundentypen im Modell
	 * @return	Liefert <code>true</code>, wenn die Combobox bzw. der Rüstzeiten-Editor angezeigt werden kann
	 */
	public static boolean showSetupTimesEditor(final int numberOfClientTypes) {
		final long maxMemoryMB=Runtime.getRuntime().maxMemory()/1024/1024;

		if (numberOfClientTypes>5000) return false;
		if (maxMemoryMB<8000 && numberOfClientTypes>2500) return false;
		if (maxMemoryMB<4000 && numberOfClientTypes>1000) return false;
		if (maxMemoryMB<2000 && numberOfClientTypes>500) return false;

		return true;
	}
}
