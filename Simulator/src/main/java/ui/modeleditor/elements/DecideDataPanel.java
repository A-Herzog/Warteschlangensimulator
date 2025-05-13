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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionMultiEval;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Dieses Panel ermöglicht die Konfiguration der Verzweigungsbedingungen.
 * @author Alexander Herzog
 * @see ModelElementDecide
 * @see ModelElementDecideAndTeleportDialog
 */
public abstract class DecideDataPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-3769198460888882801L;

	/**
	 * Element, auf das sich die Daten beziehen
	 * (für den Expression-Builder)
	 */
	private final ModelElement element;

	/**
	 * Nur-Lese-Status
	 */
	private final boolean readOnly;

	/**
	 * Decide-artiges Element, auf das sich die Daten beziehen
	 * (zum Laden und Speichern der Einstellungen)
	 */
	private final ElementWithDecideData decide;

	/** Auswahlbox für die Verzweigungsart */
	private JComboBox<String> modeSelect;
	/** Konfigurationspanel für die gewählte Verzweigungsart */
	private JPanel contentCards;

	/** Beschriftungen für die Eingabefelder für Verzweigen nach Raten */
	private List<JLabel> labels1;
	/** Beschriftungen für die Eingabefelder für Verzweigen nach Bedingungen */
	private List<JLabel> labels2;
	/** Beschriftungen für die Eingabefelder für Verzweigen nach Kundentypen */
	private List<JLabel> labels3;
	/** Beschriftungen für die Eingabefelder für Verzweigen nach Texteigenschaften */
	private List<JLabel> labels4;

	/** Eingabefelder für die Raten (im Zufall-Modus) */
	private List<JTextField> rates;

	/** Eingabefelder für die Bedingungen */
	private List<JTextField> conditions;

	/**
	 * Äußeres Element, in das {@link #clientTypesPanel} dynamisch eingebettet wird
	 * @see #reloadClientTypeSettings()
	 * @see #clientTypesPanel
	 */
	private JPanel clientTypesPanelOuter;

	/** Auswahlboxen für die Kundentypen */
	private DecideDataPanelClientTypes clientTypesPanel;

	/** Eingabefelder für die Vielfachheiten */
	private List<JTextField> multiplicity;

	/** Eingabefeld für den Schlüssel */
	private JTextField key;

	/** Mehrere durch ";" getrennte Werte pro Wert-Feld? */
	private JCheckBox valuesMulti;

	/** Eingabefelder für die Werte */
	private List<JTextField> values;

	/** Auswahlbox 1 für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie1;

	/** Auswahlbox 2 für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie2;

	/** Auswahlbox 3 für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie3;

	/** Auswahlbox 4 für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie4;

	/** Auswahlbox 5 für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie5;

	/** Auswahlbox 6 für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie6;

	/** Auswahlbox 7 für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie7;

	/** Auswahlbox  für Verhalten bei Gleichstand */
	private JComboBox<String> comboBoxAtTie8;

	/**
	 * HTML-Vorspann zum Anzeigen der Ziele als fette Texte
	 */
	private static String HTML1="<html><body><b>";

	/**
	 * HTML-Abspann zum Anzeigen der Ziele als fette Texte
	 */
	private static String HTML2="</b></body></html>";

	/**
	 * Konstruktor der Klasse
	 * @param element	Zu bearbeitendes Element (muss das {@link ElementWithDecideData}-Interface implementieren
	 * @param oldPanel	Wird hier ein Wert ungleich <code>null</code> übergeben, so werden die Daten aus dem alten Panel anstatt aus dem Element ausgelesen
	 * @param readOnly	Nur-Lese-Status
	 */
	public DecideDataPanel(final ModelElement element, final DecideDataPanel oldPanel, final boolean readOnly) {
		this.element=element;
		this.decide=(ElementWithDecideData)element;
		this.readOnly=readOnly;

		setLayout(new BorderLayout());

		JPanel sub;
		JPanel line;
		JLabel label;
		Object[] data;

		add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		sub.add(label=new JLabel(Language.tr("Surface.Decide.Dialog.DecideBy")+":"));
		sub.add(modeSelect=new JComboBox<>(new String[]{
				Language.tr("Surface.Decide.Dialog.DecideBy.Chance"),
				Language.tr("Surface.Decide.Dialog.DecideBy.Condition"),
				Language.tr("Surface.Decide.Dialog.DecideBy.ClientType"),
				Language.tr("Surface.Decide.Dialog.DecideBy.Sequence"),
				Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextProcessStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextProcessStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.LongestQueueNextStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.LongestQueueNextProcessStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.MostClientsNextStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.MostClientsNextProcessStation"),
				Language.tr("Surface.Decide.Dialog.DecideBy.StringProperty")
		}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_CHANCE,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_CONDITION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_CLIENT_TYPE,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_SEQUENCE,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_SHORTEST_QUEUE_NEXT_PROCESS_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_LEAST_CLIENTS_NEXT_PROCESS_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_LONGEST_QUEUE_NEXT_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_LONGEST_QUEUE_NEXT_PROCESS_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_MOST_CLIENTS_NEXT_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_MOST_CLIENTS_NEXT_PROCESS_STATION,
				Images.MODELEDITOR_ELEMENT_DECIDE_BY_TEXT_PROPERTY
		}));
		modeSelect.setEnabled(!readOnly);
		label.setLabelFor(modeSelect);
		modeSelect.addActionListener(e->setActiveCard((String)modeSelect.getSelectedItem()));

		final JPanel contentCardsOuter=new JPanel(new BorderLayout());
		add(new JScrollPane(contentCardsOuter),BorderLayout.CENTER);
		contentCardsOuter.add(contentCards=new JPanel(new CardLayout()),BorderLayout.NORTH);
		JPanel content;
		JPanel contentOuter;

		final List<String> destinations=getDestinations();
		labels1=new ArrayList<>();
		labels2=new ArrayList<>();
		labels4=new ArrayList<>();

		/* Seite "Zufall" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(0));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		rates=new ArrayList<>();
		for (int i=0;i<destinations.size();i++) {
			final String name=destinations.get(i);

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(HTML1+name+HTML2); labelPanel.add(label);
			labels1.add(label);

			final String decideText;
			if (oldPanel!=null) {
				decideText=(oldPanel.rates.size()>i)?oldPanel.rates.get(i).getText():"1";
			} else {
				decideText=(decide.getRates().size()<=i)?"1":decide.getRates().get(i);
			}
			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate")+":",decideText,10);
			option.add(line=(JPanel)data[0],BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];
			input.setEnabled(!readOnly);
			input.addKeyListener(new KeyListener(){
				@Override public void keyTyped(KeyEvent e) {getRates(false);}
				@Override public void keyPressed(KeyEvent e) {getRates(false);}
				@Override public void keyReleased(KeyEvent e) {getRates(false);}
			});
			line.add(ModelElementBaseDialog.getExpressionEditButton(this,input,false,true,element.getModel(),element.getSurface()));

			rates.add(input);
		}

		/* Seite "Bedingung" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(1));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		conditions=new ArrayList<>();
		for (int i=0;i<destinations.size();i++) {
			final String name=destinations.get(i);

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(HTML1+name+HTML2); labelPanel.add(label);
			labels2.add(label);

			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition")+":","");
			final JPanel inputPanel=(JPanel)data[0];
			option.add(inputPanel,BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];
			inputPanel.add(ModelElementBaseDialog.getExpressionEditButton(this,input,true,true,element.getModel(),element.getSurface()),BorderLayout.EAST);

			if (i==destinations.size()-1) {
				input.setEditable(false);
				input.setText(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition.Else"));
			} else {
				input.setEnabled(!readOnly);
				String condition;
				if (oldPanel!=null) {
					condition=(oldPanel.conditions.size()-1>i)?oldPanel.conditions.get(i).getText():""; /* -1, da wir die letzte alte Bedingung ("Wenn keine der Bedingungnen zutrifft" nicht als echte Bedingung übernehmen wollen */
				} else {
					condition=(i>=decide.getConditions().size())?"":decide.getConditions().get(i);
				}
				if (condition==null) condition="";
				input.setText(condition);
				input.addKeyListener(new KeyListener(){
					@Override public void keyTyped(KeyEvent e) {getConditions(false);}
					@Override public void keyPressed(KeyEvent e) {getConditions(false);}
					@Override public void keyReleased(KeyEvent e) {getConditions(false);}
				});
			}

			conditions.add(input);
		}

		/* Seite "Kundentyp" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(2));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(clientTypesPanelOuter=new JPanel(new BorderLayout()));
		clientTypesPanelOuter.add(clientTypesPanel=new DecideDataPanelClientTypes(element,destinations,readOnly,()->reloadClientTypeSettings()));
		if (oldPanel!=null) {
			clientTypesPanel.loadData(oldPanel.clientTypesPanel);
		} else {
			clientTypesPanel.loadData(decide.getClientTypes());
		}
		labels3=clientTypesPanel.getLabels();

		/* Seite "Reihenfolge" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(3));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		multiplicity=new ArrayList<>();
		for (int i=0;i<destinations.size();i++) {
			final String name=destinations.get(i);

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(HTML1+name+HTML2); labelPanel.add(label);
			labels1.add(label);

			final String decideText;
			if (oldPanel!=null) {
				decideText=(oldPanel.multiplicity.size()>i)?oldPanel.multiplicity.get(i).getText():"1";
			} else {
				decideText=(decide.getMultiplicity().size()<=i)?"1":(""+decide.getMultiplicity().get(i));
			}
			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Multiplicity")+":",decideText,10);
			option.add(line=(JPanel)data[0],BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];
			input.setEnabled(!readOnly);
			input.addKeyListener(new KeyListener(){
				@Override public void keyTyped(KeyEvent e) {getMultiplicity(false);}
				@Override public void keyPressed(KeyEvent e) {getMultiplicity(false);}
				@Override public void keyReleased(KeyEvent e) {getMultiplicity(false);}
			});

			multiplicity.add(input);
		}

		/* Seite "Kürzeste Warteschlange an der nächsten Station" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(4));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie1=addAtTieComboBox(sub,decide);

		/* Seite "Kürzeste Warteschlange an der nächsten Bedienstation" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(5));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.ShortestQueueNextProcessStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie2=addAtTieComboBox(sub,decide);

		/* Seite "Geringste Anzahl an Kunden an der nächsten Station" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(6));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie3=addAtTieComboBox(sub,decide);

		/* Seite "Geringste Anzahl an Kunden an der nächsten Bedienstation" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(7));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.LeastClientsNextProcessStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie4=addAtTieComboBox(sub,decide);

		/* Seite "Längste Warteschlange an der nächsten Station" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(8));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.LongestQueueNextStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie5=addAtTieComboBox(sub,decide);

		/* Seite "Längste Warteschlange an der nächsten Bedienstation" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(9));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.LongestQueueNextProcessStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie6=addAtTieComboBox(sub,decide);

		/* Seite "Meiste Kunden an der nächsten Station" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(10));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.MostClientsNextStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie7=addAtTieComboBox(sub,decide);

		/* Seite "Meiste Kunden an der nächsten Bedienstation" */
		contentCards.add(contentOuter=new JPanel(new BorderLayout()),modeSelect.getItemAt(11));
		contentOuter.add(content=new JPanel(),BorderLayout.NORTH);
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+Language.tr("Surface.Decide.Dialog.DecideBy.MostClientsNextProcessStation.Info").replaceAll("\\n","<br>")+"</body></html>"));
		content.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		comboBoxAtTie8=addAtTieComboBox(sub,decide);

		/* Seite "Texteigenschaft" */
		contentCards.add(content=new JPanel(),modeSelect.getItemAt(12));
		content.setLayout(new BoxLayout(content,BoxLayout.PAGE_AXIS));

		final String keyString;
		if (oldPanel!=null) {
			keyString=oldPanel.key.getText();
		} else {
			keyString=decide.getKey();
		}
		data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Key")+":",keyString);
		content.add((JPanel)data[0]);
		key=(JTextField)data[1];
		key.setEnabled(!readOnly);
		key.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {getCheckKeyValues(false);}
			@Override public void keyPressed(KeyEvent e) {getCheckKeyValues(false);}
			@Override public void keyReleased(KeyEvent e) {getCheckKeyValues(false);}
		});

		content.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final boolean multiValues;
		if (oldPanel!=null) {
			multiValues=oldPanel.valuesMulti.isSelected();
		} else {
			multiValues=decide.isMultiTextValues();
		}
		line.add(valuesMulti=new JCheckBox(Language.tr("Surface.Decide.Dialog.OutgoingEdge.MultiValues"),multiValues));
		valuesMulti.setToolTipText(Language.tr("Surface.Decide.Dialog.OutgoingEdge.MultiValues.Hint"));
		valuesMulti.addActionListener(e->getCheckKeyValues(false));

		values=new ArrayList<>();
		final List<String> valuesList=decide.getValues();
		for (int i=0;i<destinations.size();i++) {
			final String name=destinations.get(i);

			final JPanel option=new JPanel(new BorderLayout()); content.add(option);

			final JPanel labelPanel=new JPanel(new FlowLayout(FlowLayout.LEFT)); option.add(labelPanel,BorderLayout.NORTH);
			label=new JLabel(HTML1+name+HTML2); labelPanel.add(label);
			labels4.add(label);

			data=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value")+":","");
			option.add((JPanel)data[0],BorderLayout.CENTER);
			final JTextField input=(JTextField)data[1];

			if (i==destinations.size()-1) {
				input.setEditable(false);
				input.setText(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value.Else"));
			} else {
				input.setEnabled(!readOnly);
				if (oldPanel!=null) {
					input.setText((i<oldPanel.values.size()-1)?oldPanel.values.get(i).getText():"");
				} else {
					input.setText((i>=valuesList.size())?"":valuesList.get(i));
				}
				input.addKeyListener(new KeyListener(){
					@Override public void keyTyped(KeyEvent e) {getCheckKeyValues(false);}
					@Override public void keyPressed(KeyEvent e) {getCheckKeyValues(false);}
					@Override public void keyReleased(KeyEvent e) {getCheckKeyValues(false);}
				});
			}

			values.add(input);
		}

		/* Aktive Karte einstellen */
		if (oldPanel==null) {
			switch (decide.getMode()) {
			case MODE_CHANCE: modeSelect.setSelectedIndex(0); break;
			case MODE_CONDITION: modeSelect.setSelectedIndex(1); break;
			case MODE_CLIENTTYPE: modeSelect.setSelectedIndex(2); break;
			case MODE_SEQUENCE: modeSelect.setSelectedIndex(3); break;
			case MODE_SHORTEST_QUEUE_NEXT_STATION: modeSelect.setSelectedIndex(4); break;
			case MODE_SHORTEST_QUEUE_PROCESS_STATION: modeSelect.setSelectedIndex(5); break;
			case MODE_MIN_CLIENTS_NEXT_STATION: modeSelect.setSelectedIndex(6); break;
			case MODE_MIN_CLIENTS_PROCESS_STATION: modeSelect.setSelectedIndex(7); break;
			case MODE_LONGEST_QUEUE_NEXT_STATION: modeSelect.setSelectedIndex(8); break;
			case MODE_LONGEST_QUEUE_PROCESS_STATION: modeSelect.setSelectedIndex(9); break;
			case MODE_MAX_CLIENTS_NEXT_STATION: modeSelect.setSelectedIndex(10); break;
			case MODE_MAX_CLIENTS_PROCESS_STATION: modeSelect.setSelectedIndex(11); break;
			case MODE_KEY_VALUE: modeSelect.setSelectedIndex(12); getCheckKeyValues(false); break;
			}
		} else {
			modeSelect.setSelectedIndex(oldPanel.modeSelect.getSelectedIndex());
			switch (oldPanel.modeSelect.getSelectedIndex()) {
			case 8: getCheckKeyValues(false); break;
			}
		}
		setActiveCard((String)modeSelect.getSelectedItem());

		getRates(false);
		getConditions(false);
		getMultiplicity(false);
		getCheckKeyValues(false);
	}

	/**
	 * Konstruktor der Klasse
	 * @param element	Zu bearbeitendes Element (muss das {@link ElementWithDecideData}-Interface implementieren
	 * @param readOnly	Nur-Lese-Status
	 */
	public DecideDataPanel(final ModelElement element, final boolean readOnly) {
		this(element,null,readOnly);
	}

	/**
	 * Aktualisiert die Beschriftungen an den Eingabefeldern.
	 */
	public void updateLabels() {
		final List<String> destinations=getDestinations();
		int min=destinations.size();
		min=Math.min(min,labels1.size());
		min=Math.min(min,labels2.size());
		min=Math.min(min,labels3.size());
		min=Math.min(min,labels4.size());
		for (int i=0;i<min;i++) {
			labels1.get(i).setText(HTML1+destinations.get(i)+HTML2);
			labels2.get(i).setText(HTML1+destinations.get(i)+HTML2);
			labels3.get(i).setText(HTML1+destinations.get(i)+HTML2);
			labels4.get(i).setText(HTML1+destinations.get(i)+HTML2);
		}
	}

	/**
	 * Legt eine Auswahlbox für den Modus der Wahl des Ausgangs bei Gleichstand zwischen den Stationen an.
	 * @param parent	Übergeordnetes Element an das die neue Zeile angehängt werden soll
	 * @param decide	Verzweigungs-Element aus dem die Daten ausgelesen werden sollen
	 * @return	Neue Auswahlbox (schon in das übergeordnete Element eingefügt)
	 */
	private JComboBox<String> addAtTieComboBox(final JPanel parent, final ElementWithDecideData decide) {
		final Object[] data=ModelElementBaseDialog.getComboBoxPanel(Language.tr("Surface.Decide.Dialog.DecideBy.AtTie")+":",new String[] {
				Language.tr("Surface.Decide.Dialog.DecideBy.AtTie.First"),
				Language.tr("Surface.Decide.Dialog.DecideBy.AtTie.Random"),
				Language.tr("Surface.Decide.Dialog.DecideBy.AtTie.Last")
		});
		parent.add((JPanel)data[0]);
		@SuppressWarnings("unchecked")
		final JComboBox<String> comboBox=(JComboBox<String>)data[1];

		comboBox.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODELEDITOR_ELEMENT_DECIDE_AT_TIE_FIRST,
				Images.MODELEDITOR_ELEMENT_DECIDE_AT_TIE_RANDOM,
				Images.MODELEDITOR_ELEMENT_DECIDE_AT_TIE_LAST
		}));

		switch (decide.getDecideByStationOnTie()) {
		case FIRST: comboBox.setSelectedIndex(0); break;
		case RANDOM: comboBox.setSelectedIndex(1); break;
		case LAST: comboBox.setSelectedIndex(2); break;
		default: comboBox.setSelectedIndex(1); break;
		}

		comboBox.addActionListener(e->{
			final Object source=e.getSource();
			if (!(source instanceof JComboBox<?>)) return;
			final int index=((JComboBox<?>)source).getSelectedIndex();
			if (comboBoxAtTie1!=null && comboBoxAtTie1!=source) comboBoxAtTie1.setSelectedIndex(index);
			if (comboBoxAtTie2!=null && comboBoxAtTie2!=source) comboBoxAtTie2.setSelectedIndex(index);
			if (comboBoxAtTie3!=null && comboBoxAtTie3!=source) comboBoxAtTie3.setSelectedIndex(index);
			if (comboBoxAtTie4!=null && comboBoxAtTie4!=source) comboBoxAtTie4.setSelectedIndex(index);
			if (comboBoxAtTie5!=null && comboBoxAtTie5!=source) comboBoxAtTie5.setSelectedIndex(index);
			if (comboBoxAtTie6!=null && comboBoxAtTie6!=source) comboBoxAtTie6.setSelectedIndex(index);
			if (comboBoxAtTie7!=null && comboBoxAtTie7!=source) comboBoxAtTie7.setSelectedIndex(index);
			if (comboBoxAtTie8!=null && comboBoxAtTie8!=source) comboBoxAtTie8.setSelectedIndex(index);
		});

		return comboBox;
	}

	/**
	 * Liefer die Liste der Namen der Ziele.
	 * @return	Liste der Namen der Ziele
	 */
	protected abstract List<String> getDestinations();

	/**
	 * Stellt den sichtbaren Bereich in {@link #contentCards} ein.
	 * @param name	Name für den anzuzeigenden Bereich
	 * @see #modeSelect
	 */
	private void setActiveCard(final String name) {
		final CardLayout cardLayout=(CardLayout)(contentCards.getLayout());
		cardLayout.show(contentCards,name);
		getCheckKeyValues(false);
	}

	/**
	 * Wird aufgerufen, wenn bei der Verzweigung nach Kundentypen eine Kundentyp-Auswahlbox für eine
	 * auslaufende Kante hinzugefügt oder entfernt wurde (und daher das Panel neu aufgebaut werden muss).
	 */
	private void reloadClientTypeSettings() {
		final DecideDataPanelClientTypes oldPanel=clientTypesPanel;
		clientTypesPanelOuter.remove(oldPanel);

		clientTypesPanelOuter.add(clientTypesPanel=new DecideDataPanelClientTypes(element,oldPanel.destinations,readOnly,()->reloadClientTypeSettings()));
		clientTypesPanel.loadData(oldPanel);

		clientTypesPanelOuter.setVisible(false);
		clientTypesPanelOuter.setVisible(true);
	}

	/**
	 * Liefert die Raten für die Verzweigungen gemäß den Einstellungen
	 * @param showErrorDialog	Im Fehlerfall eine Meldung ausgeben?
	 * @return	Liefert im Erfolgsfall die Raten und im Fehlerfall <code>null</code>
	 */
	private List<String> getRates(final boolean showErrorDialog) {
		List<String> values=new ArrayList<>();

		double sum=0;
		for (int i=0;i<rates.size();i++) {
			final JTextField field=rates.get(i);
			final Double D=NumberTools.getDouble(field,false);
			if (D==null || sum==-1) sum=-1; else sum+=Math.max(0,D);

			final int error=ExpressionCalc.check(field.getText(),element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),element.getModel().userFunctions);
			if (error>=0) field.setBackground(Color.RED); else field.setBackground(NumberTools.getTextFieldDefaultBackground());

			if (error>=0) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.InfoInvalid"),i+1,error+1));
					return null;
				}
				values=null;
			}
			if (values!=null) values.add(field.getText().trim());
		}
		if (values!=null && rates.size()>0 && sum==0) {
			if (showErrorDialog) MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.Title"),Language.tr("Surface.Decide.Dialog.OutgoingEdge.Rate.Error.InfoAllZero"));
			return null;
		}

		return values;
	}

	/**
	 * Liefert die Verzweigungsbedingungen gemäß den Einstellungen
	 * @param showErrorDialog	Im Fehlerfall eine Meldung ausgeben?
	 * @return	Liefert im Erfolgsfall die Bedingungen und im Fehlerfall <code>null</code>
	 */
	private List<String> getConditions(final boolean showErrorDialog) {
		List<String> values=new ArrayList<>();

		for (int i=0;i<conditions.size()-1;i++) {
			final JTextField field=conditions.get(i);
			final String condition=field.getText();
			final int error=ExpressionMultiEval.check(condition,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),true),element.getModel().userFunctions);
			if (error>=0) field.setBackground(Color.RED); else field.setBackground(NumberTools.getTextFieldDefaultBackground());

			if (error>=0) {
				if (showErrorDialog) {
					MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition.Error.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Condition.Error.Info"),i+1,error+1));
					return null;
				}
				values=null;
			}
			if (values!=null) values.add(condition);
		}

		return values;
	}

	/**
	 * Liefert die Vielfachheiten für die Verzweigungen gemäß den Einstellungen
	 * @param showErrorDialog	Im Fehlerfall eine Meldung ausgeben?
	 * @return	Liefert im Erfolgsfall die Vielfachheiten und im Fehlerfall <code>null</code>
	 */
	private List<Integer> getMultiplicity(final boolean showErrorDialog) {
		final List<Integer> values=new ArrayList<>();

		for (int i=0;i<multiplicity.size();i++) {
			final JTextField field=multiplicity.get(i);
			final Long L=NumberTools.getPositiveLong(field,true);
			if (L==null) {
				if (showErrorDialog) MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Multiplicity.Error.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Multiplicity.Error.InfoInvalid"),i+1,field.getText()));
				return null;
			}
			values.add(L.intValue());
		}

		return values;
	}

	/**
	 * Prüft, ob ein Wert (ggf. bestehend aus mehreren Teilwerten, wenn dies zulässig ist) gültig ist.
	 * @param value	Zu prüfender Wert einer Schlüssel-Wert-Zuweisung
	 * @return	Gibt an, ob der Wert gültig ist
	 */
	private boolean checkValue(final String value) {
		if (value.isBlank()) return false;

		if (valuesMulti.isSelected()) {
			final String[] v=value.split(";");
			if (v==null || v.length==0) return false;
			for (String s: v) if (s.isBlank()) return false;
		}

		return true;
	}

	/**
	 * Prüft die Einstellungen in {@link #key} und in {@link #values}.
	 * @param showErrorMessages	Im Fehlerfall eine Meldung ausgeben?
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean getCheckKeyValues(final boolean showErrorMessages) {
		boolean ok=true;

		/* Schlüssel */
		if (key.getText().isBlank()) {
			key.setBackground(Color.red);
			ok=false;
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Key.ErrorMissing.Title"),Language.tr("Surface.Decide.Dialog.OutgoingEdge.Key.ErrorMissing.Info"));
				return false;
			}
		} else {
			key.setBackground(NumberTools.getTextFieldDefaultBackground());
		}

		/* Werte */
		for (int i=0;i<values.size()-1;i++) {
			if (!checkValue(values.get(i).getText())) {
				values.get(i).setBackground(Color.RED);
				ok=false;
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value.ErrorMissing.Title"),String.format(Language.tr("Surface.Decide.Dialog.OutgoingEdge.Value.ErrorMissing.Info"),i+1));
					return false;
				}
			} else {
				values.get(i).setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		return ok;
	}

	/**
	 * Wird beim Klicken auf "Ok" aufgerufen, um zu prüfen, ob die Daten in der aktuellen Form
	 * in Ordnung sind und gespeichert werden können.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkDataWithErrorMessage() {
		switch (modeSelect.getSelectedIndex()) {
		case 0: return getRates(true)!=null;
		case 1: return getConditions(true)!=null;
		case 2: return clientTypesPanel.checkClientTypes();
		case 3: return getMultiplicity(true)!=null;
		case 4: return true;
		case 5: return true;
		case 6: return true;
		case 7: return true;
		case 8: return true;
		case 9: return true;
		case 10: return true;
		case 11: return true;
		case 12: return getCheckKeyValues(true);
		default: return false;
		}
	}

	/**
	 * Schreibt die Daten aus der GUI in das Element zurück
	 */
	public void storeData() {
		ModelElementDecide.DecideMode mode=ModelElementDecide.DecideMode.MODE_CHANCE;
		switch (modeSelect.getSelectedIndex()) {
		case 0: mode=ModelElementDecide.DecideMode.MODE_CHANCE; break;
		case 1: mode=ModelElementDecide.DecideMode.MODE_CONDITION; break;
		case 2: mode=ModelElementDecide.DecideMode.MODE_CLIENTTYPE; break;
		case 3: mode=ModelElementDecide.DecideMode.MODE_SEQUENCE; break;
		case 4: mode=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_NEXT_STATION; break;
		case 5: mode=ModelElementDecide.DecideMode.MODE_SHORTEST_QUEUE_PROCESS_STATION; break;
		case 6: mode=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_NEXT_STATION; break;
		case 7: mode=ModelElementDecide.DecideMode.MODE_MIN_CLIENTS_PROCESS_STATION; break;
		case 8: mode=ModelElementDecide.DecideMode.MODE_LONGEST_QUEUE_NEXT_STATION; break;
		case 9: mode=ModelElementDecide.DecideMode.MODE_LONGEST_QUEUE_PROCESS_STATION; break;
		case 10: mode=ModelElementDecide.DecideMode.MODE_MAX_CLIENTS_NEXT_STATION; break;
		case 11: mode=ModelElementDecide.DecideMode.MODE_MAX_CLIENTS_PROCESS_STATION; break;
		case 12: mode=ModelElementDecide.DecideMode.MODE_KEY_VALUE; break;
		}

		decide.setMode(mode);

		switch (mode) {
		case MODE_CHANCE:
			final List<String> ratesList=decide.getRates();
			ratesList.clear();
			ratesList.addAll(getRates(false));
			break;
		case MODE_CONDITION:
			final List<String> conditionsList=decide.getConditions();
			conditionsList.clear();
			conditionsList.addAll(getConditions(false));
			break;
		case MODE_CLIENTTYPE:
			final List<List<String>> clientTypesList=decide.getClientTypes();
			clientTypesList.clear();
			clientTypesList.addAll(clientTypesPanel.getClientTypes());
			break;
		case MODE_SEQUENCE:
			final List<Integer> multiplicityList=decide.getMultiplicity();
			multiplicityList.clear();
			multiplicityList.addAll(getMultiplicity(false));
			break;
		case MODE_SHORTEST_QUEUE_NEXT_STATION:
			switch (comboBoxAtTie1.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_SHORTEST_QUEUE_PROCESS_STATION:
			switch (comboBoxAtTie2.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_MIN_CLIENTS_NEXT_STATION:
			switch (comboBoxAtTie3.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_MIN_CLIENTS_PROCESS_STATION:
			switch (comboBoxAtTie4.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_LONGEST_QUEUE_NEXT_STATION:
			switch (comboBoxAtTie5.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_LONGEST_QUEUE_PROCESS_STATION:
			switch (comboBoxAtTie6.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_MAX_CLIENTS_NEXT_STATION:
			switch (comboBoxAtTie7.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_MAX_CLIENTS_PROCESS_STATION:
			switch (comboBoxAtTie8.getSelectedIndex()) {
			case 0: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.FIRST); break;
			case 1: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.RANDOM); break;
			case 2: decide.setDecideByStationOnTie(ElementWithDecideData.DecideByStationOnTie.LAST); break;
			}
			break;
		case MODE_KEY_VALUE:
			decide.setKey(key.getText().trim());
			decide.setMultiTextValues(valuesMulti.isSelected());
			final List<String> valuesList=decide.getValues();
			valuesList.clear();
			for (int i=0;i<values.size()-1;i++) valuesList.add(values.get(i).getText().trim());
			break;
		}
	}
}
