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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.symbols.CalcSymbolSimDataCounter;
import simulator.simparser.symbols.CalcSymbolSimDataCounterPart;
import simulator.simparser.symbols.CalcSymbolStationDataProcess_avg;
import simulator.simparser.symbols.CalcSymbolStationDataQueue;
import simulator.simparser.symbols.CalcSymbolStationDataQueue_avg;
import simulator.simparser.symbols.CalcSymbolStationDataResidence_avg;
import simulator.simparser.symbols.CalcSymbolStationDataSetup_avg;
import simulator.simparser.symbols.CalcSymbolStationDataTransfer_avg;
import simulator.simparser.symbols.CalcSymbolStationDataWIP;
import simulator.simparser.symbols.CalcSymbolStationDataWIP_avg;
import simulator.simparser.symbols.CalcSymbolStationDataWaiting_avg;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.elements.AnimationExpression.ExpressionMode;

/**
 * In diesem Panel kann ein Rechenausdruck oder ein Skript aus {@link AnimationExpression}
 * bearbeitet werden. Das Objekt wird dabei direkt im Konstruktor übergeben und die
 * Daten werden beim Aufruf von {@link #storeData()} direkt dorthin zurückgeschrieben.
 * @author Alexander Herzog
 * @see AnimationExpression
 */
public class AnimationExpressionPanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=6129350509255100815L;

	/**
	 * Modellelement in dem der Ausdruck verwendet werden soll
	 */
	private final ModelElement element;

	/**
	 * Zu bearbeitender Ausdruck (beim Speichern werden die Daten in dieses Objekt zurückgeschrieben)
	 */
	private final AnimationExpression expression;

	/**
	 * Nur-Lese-Status
	 */
	private final boolean readOnly;

	/**
	 * Callback für Klicks auf die Hilfe-Schaltfläche im Skript-Editor-Dialog
	 */
	private final Runnable helpRunnable;

	/**
	 * Auswahlbox für Modus (Ausdruck oder Skript)
	 */
	private final JComboBox<String> modeSelect;

	/**
	 * Bereich in dem der Ausdruck oder die Bearbeiten-Schaltfläche für das
	 * Skript angezeigt werden. Wird über die Auswahl in {@link #modeSelect} umgestellt.
	 * @see #modeSelect
	 * @see #updateCard()
	 */
	private final JPanel cards;

	/**
	 * Layout für {@link #cards}
	 * @see #modeSelect
	 * @see #updateCard()
	 */
	private final CardLayout cardsLayout;

	/**
	 * Eingabefeld zum Bearbeiten des Ausdrucks
	 */
	private final JTextField expressionEdit;

	/**
	 * Aktuelle Skriptsprache
	 * @see #script
	 */
	private AnimationExpression.ExpressionMode scriptMode;

	/**
	 * Aktuelles Skript
	 */
	private String script;

	/**
	 * Bereits verwendete Ausdrücke (ohne den in Bearbeitung befindlichen) in Kleinbuchstaben - um im Vorlagenpopup keine bereits verwendeten Ausdrücke anzubieten
	 */
	private Set<String> usedExpressionsLower;

	/**
	 * Konstruktor der Klasse
	 * @param element	Modellelement in dem der Ausdruck verwendet werden soll
	 * @param expression	Bisheriger Ausdruck
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Callback für Klicks auf die Hilfe-Schaltfläche im Skript-Editor-Dialog
	 * @param usedExpressions	Liste aller momentan verwendeten Ausdrücke (darf den aktuellen Ausdruck enthalten, darf leer oder <code>null</code> sein) - um im Vorlagenpopup keine bereits verwendeten Ausdrücke anzubieten
	 */
	public AnimationExpressionPanel(final ModelElement element, final AnimationExpression expression, final boolean readOnly, final Runnable helpRunnable, final List<String> usedExpressions) {
		this.element=element;
		this.expression=expression;
		this.readOnly=readOnly;
		this.helpRunnable=helpRunnable;

		setBorder(BorderFactory.createEmptyBorder(0,5,0,0));

		setLayout(new BorderLayout(5,0));

		Box box;

		/* Modus */
		add(box=new Box(BoxLayout.PAGE_AXIS),BorderLayout.WEST);
		box.add(Box.createVerticalGlue());
		box.add(modeSelect=new JComboBox<>(new String[] {
				Language.tr("AnimationExpression.Expression"),
				Language.tr("AnimationExpression.Script")
		}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[] {
				Images.MODE_EXPRESSION,
				Images.SCRIPT_MODE_JAVASCRIPT
		}));
		modeSelect.setEditable(false);
		modeSelect.setEnabled(!readOnly);
		box.add(Box.createVerticalGlue());

		/* Cards */
		add(cards=new JPanel(cardsLayout=new CardLayout()),BorderLayout.CENTER);
		JPanel card;
		cards.setBorder(BorderFactory.createEmptyBorder());

		/* Ausdruck */
		cards.add(card=new JPanel(new BorderLayout(5,0)),"0");
		card.add(box=new Box(BoxLayout.PAGE_AXIS),BorderLayout.CENTER);
		box.add(Box.createVerticalGlue());
		box.add(expressionEdit=new JTextField());
		ModelElementBaseDialog.addUndoFeature(expressionEdit);
		expressionEdit.setEnabled(!readOnly);
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
		});
		box.add(Box.createVerticalGlue());

		final JPanel rightButtons=new JPanel(new FlowLayout(FlowLayout.LEFT));
		card.add(rightButtons,BorderLayout.EAST);
		rightButtons.setBorder(BorderFactory.createEmptyBorder(0,-5,0,-5));

		rightButtons.add(box=new Box(BoxLayout.PAGE_AXIS));
		box.add(Box.createVerticalGlue());
		box.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,false,element.getModel(),element.getSurface()));
		box.add(Box.createVerticalGlue());

		if (usedExpressions!=null) {
			usedExpressionsLower=usedExpressions.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toSet());
			final String currentExpression=expression.getExpression().toLowerCase().trim();
			usedExpressionsLower.remove(currentExpression);
		} else {
			usedExpressionsLower=new HashSet<>();
		}

		if (usedExpressions!=null && !readOnly) {
			final JButton templatesButton;
			rightButtons.add(box=new Box(BoxLayout.PAGE_AXIS));
			box.add(Box.createVerticalGlue());
			box.add(templatesButton=new JButton(Images.GENERAL_INFO.getIcon()));
			box.add(Box.createVerticalGlue());
			templatesButton.setToolTipText(Language.tr("AnimationExpression.ExpressionTemplates"));
			templatesButton.addActionListener(e->commandTemplateExpression(templatesButton,element.getModel(),usedExpressionsLower,command->{expressionEdit.setText(command); checkData(false);}));
		}

		/* Skript */
		cards.add(card=new JPanel(new FlowLayout(FlowLayout.LEFT)),"1");
		final JButton button=new JButton(Language.tr("AnimationExpression.Script.Edit"),Images.GENERAL_EDIT.getIcon());
		card.add(button);
		button.addActionListener(e->editScript());
		switch (expression.getMode()) {
		case Expression:
			scriptMode=AnimationExpression.ExpressionMode.Javascript;
			break;
		case Javascript:
		case Java:
			scriptMode=expression.getMode();
			break;
		}
		script=expression.getScript();

		expressionEdit.setMaximumSize(new Dimension(10000,button.getHeight()));

		/* Starten */
		modeSelect.setSelectedIndex((expression.getMode()==AnimationExpression.ExpressionMode.Expression)?0:1);
		modeSelect.addActionListener(e->updateCard());
		updateCard();
		expressionEdit.setText(expression.getExpression());
	}

	/**
	 * Wandelt eine Liste von {@link AnimationExpression}-Objekt in eine Liste mit Zeichenketten mit Rechenausdrücken um.
	 * @param usedExpressions	Liste von {@link AnimationExpression}-Objekt (darf leer oder <code>null</code> sein)
	 * @return	Gefilterte Liste, die nur die Rechenausdrücke enthält (ist der Parameter <code>null</code>, so ist das Ergebnis <code>null</code>, sonst mindestens eine leere Liste)
	 */
	public static List<String> extractExpressionStrings(final List<AnimationExpression> usedExpressions) {
		if (usedExpressions==null) return null;
		return usedExpressions.stream().filter(animationExpression->animationExpression.getMode()==ExpressionMode.Expression).map(animationExpression->animationExpression.getExpression()).collect(Collectors.toList());
	}

	/**
	 * Konstruktor der Klasse
	 * @param element	Modellelement in dem der Ausdruck verwendet werden soll
	 * @param expression	Bisheriger Ausdruck
	 * @param readOnly	Nur-Lese-Status
	 * @param helpRunnable	Callback für Klicks auf die Hilfe-Schaltfläche im Skript-Editor-Dialog
	 */
	public AnimationExpressionPanel(final ModelElement element, final AnimationExpression expression, final boolean readOnly, final Runnable helpRunnable) {
		this(element,expression,readOnly,helpRunnable,null);
	}

	/**
	 * Aktualisiert die Darstellung in {@link #cards} nachdem
	 * in {@link #modeSelect} ein anderer Modus ausgewählt wurde.
	 * @see #modeSelect
	 * @see #cards
	 * @see #cardsLayout
	 */
	private void updateCard() {
		cardsLayout.show(cards,""+modeSelect.getSelectedIndex());
	}

	/**
	 * Befehl: Skript bearbeiten
	 * @see AnimationExpressionDialog
	 */
	private void editScript() {
		final AnimationExpressionDialog dialog=new AnimationExpressionDialog(this,scriptMode,script,element.getModel(),readOnly,helpRunnable);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			scriptMode=dialog.getMode();
			script=dialog.getScript();
		}
	}

	/**
	 * Liefert eine Liste mit allen Stationen bestimmter Typen.
	 * @param types	Liste der gesuchten Stationstypen
	 * @param model	Modell dem die Daten entnommen werden sollen
	 * @return	Liste der Stationen
	 */
	private static List<ModelElementBox> getBoxes(final Set<Class<? extends ModelElementBox>> types, final EditModel model) {
		final List<ModelElementBox> list=new ArrayList<>();
		for (var element: model.surface.getElementsIncludingSubModels()) {
			if (!(element instanceof ModelElementBox)) continue;
			final ModelElementBox box=(ModelElementBox)element;
			if (types.contains(box.getClass())) list.add(box);
		}
		return list;
	}

	/**
	 * Liefert eine Liste mit allen Kundentypennamen und den zugehörigen Stationen, an denen diese generiert werden.
	 * @param model	Modell dem die Daten entnommen werden sollen
	 * @return	Zuordnung von Kundentypennamen zu den ids, an denen diese generiert werden
	 */
	private static Map<String,int[]> getClientTypes(final EditModel model) {
		final Map<String,int[]> results=new HashMap<>();
		for (var element: model.surface.getElementsIncludingSubModels()) {
			if (!(element instanceof ModelElementBox)) continue;

			if (element instanceof ModelElementSource) {
				final String name=((ModelElementSource)element).getName();
				if (!name.isBlank()) {
					final int[] oldValue=results.get(name);
					if (oldValue!=null && oldValue.length==1) continue;
					results.put(name,new int[] {element.getId()});
				}
			}

			if (element instanceof ModelElementSourceMulti) {
				final String[] names=((ModelElementSourceMulti)element).getRecords().stream().map(record->record.getName()).toArray(String[]::new);
				for (int i=0;i<names.length;i++) if (!names[i].isBlank()) {
					results.putIfAbsent(names[i],new int[] {element.getId(),i+1});
				}
			}
		}
		return results;
	}

	/**
	 * Prüft, ob ein bestimmter Rechenausdruck für eine bestimmte Station bereits genutzt wird.
	 * @param commands	Liste der gleichwertigen Rechenausdrücke, die geprüft werden sollen
	 * @param box	Station auf die sich der Rechenausdruck beziehen soll (<code>null</code> für global)
	 * @param usedExpressionsLower	Bereits verwendete Ausdrücke (ohne den in Bearbeitung befindlichen) in Kleinbuchstaben - um im Vorlagenpopup keine bereits verwendeten Ausdrücke anzubieten
	 * @return	Liefert <code>true</code>, wenn der Ausdruck in der Liste der bereits verwendeten Ausdrücke enthalten ist
	 * @see #usedExpressionsLower
	 */
	private static boolean isInUse(final Set<String> commands, final ModelElementBox box, final Set<String> usedExpressionsLower) {
		final Set<String> commandsLower=commands.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toSet());

		for (var inUse: usedExpressionsLower) {
			final int index1=inUse.indexOf('(');
			final int index2=inUse.indexOf(')');
			if (index1<0 || index2<0 || index2<=index1) continue;

			/* Name */
			if (index1<1) continue;
			final String name=inUse.substring(0,index1).toLowerCase().trim();
			if (name.isBlank()) continue;

			/* Name ist aktueller Befehl? */
			if (!commandsLower.contains(name.toLowerCase().trim())) continue;

			/* Parameter */
			if (index2==index1+1) {
				if (box==null) return true;
			} else {
				if (box==null) continue;
				final String param=inUse.substring(index1,index2).trim();
				final Integer id=NumberTools.getNotNegativeInteger(param);
				if (id==null) continue;
				/* ID der aktuellen Box? */
				if (id.intValue()==box.getId()) return true;
			}
		}

		return false;
	}

	/**
	 * Prüft, ob ein bestimmter Rechenausdruck für einen bestimmten Kundentyp bereits genutzt wird.
	 * @param commands	Liste der gleichwertigen Rechenausdrücke, die geprüft werden sollen
	 * @param clientType	Kundentypen-ID auf den sich der Rechenausdruck beziehen soll (<code>null</code> für global)
	 * @param usedExpressionsLower	Bereits verwendete Ausdrücke (ohne den in Bearbeitung befindlichen) in Kleinbuchstaben - um im Vorlagenpopup keine bereits verwendeten Ausdrücke anzubieten
	 * @return	Liefert <code>true</code>, wenn der Ausdruck in der Liste der bereits verwendeten Ausdrücke enthalten ist
	 * @see #usedExpressionsLower
	 */
	private static boolean isInUse(final Set<String> commands, final int[] clientType, final Set<String> usedExpressionsLower) {
		final Set<String> commandsLower=commands.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toSet());

		for (var inUse: usedExpressionsLower) {
			final int index1=inUse.indexOf('(');
			final int index2=inUse.indexOf(')');
			if (index1<0 || index2<0 || index2<=index1) continue;

			/* Name */
			if (index1<1) continue;
			final String name=inUse.substring(0,index1).toLowerCase().trim();
			if (name.isBlank()) continue;

			/* Name ist aktueller Befehl? */
			if (!commandsLower.contains(name.toLowerCase().trim())) continue;

			/* Parameter */
			if (index2==index1+1) {
				if (clientType==null) return true;
			} else {
				if (clientType==null) continue;
				final Integer[] ids=Arrays.stream(inUse.substring(index1,index2).trim().split(";")).map(s->NumberTools.getNotNegativeInteger(s)).toArray(Integer[]::new);
				boolean doContinue=false;
				for (var id: ids) if (id==null) {doContinue=true; break;}
				if (doContinue) continue;
				if (ids.length!=clientType.length) continue;
				if (Arrays.equals(Arrays.stream(ids).mapToInt(Integer::intValue).toArray(),clientType)) return true;
			}
		}

		return false;
	}

	/**
	 * Erzeugt eine Anzeige-Stationsnamen zur Anzeige im Popupmenü
	 * @param box	Station
	 * @return	Anzeigename
	 */
	private static String stationName(final ModelElementBox box) {
		final StringBuilder name=new StringBuilder();
		name.append(box.getTypeName());
		if (!box.getName().isBlank()) {
			name.append(" - ");
			name.append(box.getName());
		}
		name.append(String.format(" (id=%d)",box.getId()));
		return name.toString();
	}

	/**
	 * Datensatz mit den Informationen zum übergeordneten Untermenü und zum Klick-Callback
	 * @see AnimationExpressionPanel#addItem(AddItemData, String, String)
	 */
	private static class AddItemData {
		/** Übergeordnetes Untermenü */
		private JMenu popupMenuSection;
		/** Callback für Klicks */
		private Consumer<String> setCommandCallback;
	}

	/**
	 * Fügt einen Eintrag zum aktuellen Untermenü hinzu
	 * @param addItemData Datensatz mit den Informationen zum übergeordneten Untermenü und zum Klick-Callback
	 * @param name	Name des Eintrags
	 * @param command	Befehl, der bei der Auswahl des Menüpunktes in {@link #expressionEdit} eingetragen werden soll
	 */
	private static void addItem(final AddItemData addItemData, final String name, final String command) {
		final JMenuItem item=new JMenuItem(name);
		addItemData.popupMenuSection.add(item);
		final Consumer<String> callback=addItemData.setCommandCallback;
		item.addActionListener(e->callback.accept(command));
	}

	/**
	 * Stationstypen, bei denen WIP ausgelesen werden kann
	 * @see #commandTemplateExpression
	 */
	private static final Set<Class<? extends ModelElementBox>> BOXES_WIP=Set.of(
			ModelElementProcess.class,
			ModelElementDelay.class,
			ModelElementDelayJS.class,
			ModelElementHold.class,
			ModelElementHoldJS.class,
			ModelElementHoldMulti.class,
			ModelElementSeize.class,
			ModelElementBarrier.class,
			ModelElementBarrierPull.class,
			ModelElementBatch.class,
			ModelElementBatchMulti.class,
			ModelElementMatch.class);

	/**
	 * Stationstypen, bei denen NQ ausgelesen werden kann
	 * @see #commandTemplateExpression
	 */
	private static final Set<Class<? extends ModelElementBox>> BOXES_NQ=Set.of(
			ModelElementProcess.class);

	/**
	 * Stationstypen, bei denen ein Zählerwert ausgelesen werden kann
	 * @see #commandTemplateExpression
	 */
	private static final Set<Class<? extends ModelElementBox>> BOXES_COUNTER=Set.of(
			ModelElementCounter.class,
			ModelElementDifferentialCounter.class,
			ModelElementThroughput.class);

	/**
	 * Stationstypen, bei denen ein Zähleranteil ausgelesen werden kann
	 * @see #commandTemplateExpression
	 */
	private static final Set<Class<? extends ModelElementBox>> BOXES_COUNTER_PART=Set.of(
			ModelElementCounter.class);

	/**
	 * Äquivalente zu "WIP"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_WIP=Set.of(new CalcSymbolStationDataWIP().getNames());

	/**
	 * Äquivalente zu "WIP_avg"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_WIP_AVG=Set.of(new CalcSymbolStationDataWIP_avg().getNames());

	/**
	 * Äquivalente zu "NQ"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_NQ=Set.of(new CalcSymbolStationDataQueue().getNames());

	/**
	 * Äquivalente zu "NQ_avg"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_NQ_AVG=Set.of(new CalcSymbolStationDataQueue_avg().getNames());

	/**
	 * Äquivalente zu "Counter"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_COUNTER=Set.of(new CalcSymbolSimDataCounter().getNames());

	/**
	 * Äquivalente zu "Part"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_COUNTER_PART=Set.of(new CalcSymbolSimDataCounterPart().getNames());

	/**
	 * Äquivalente zu "SetupTime_avg"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_SETUP_TIME=Set.of(new CalcSymbolStationDataSetup_avg().getNames());

	/**
	 * Äquivalente zu "WaitingTime_avg"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_WAITING_TIME=Set.of(new CalcSymbolStationDataWaiting_avg().getNames());

	/**
	 * Äquivalente zu "TransferTime_avg"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_TRANSFER_TIME=Set.of(new CalcSymbolStationDataTransfer_avg().getNames());

	/**
	 * Äquivalente zu "ProcessTime_avg"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_PROCESS_TIME=Set.of(new CalcSymbolStationDataProcess_avg().getNames());

	/**
	 * Äquivalente zu "ResidenceTime_avg"
	 * @see #commandTemplateExpression
	 */
	private static final Set<String> COMMAND_RESIDENCE_TIME=Set.of(new CalcSymbolStationDataResidence_avg().getNames());

	/**
	 * Zeigt ein Menü mit möglichen Vorlagen für den Rechenausdruck an.
	 * @param button	Schaltfläche an der das Popupmenü ausgerichtet werden soll
	 * @param model	Modell dem die Daten zu den Stationen entnommen werden sollen
	 * @param usedExpressionsLower	Bereits verwendete Ausdrücke (ohne den in Bearbeitung befindlichen) in Kleinbuchstaben - um im Vorlagenpopup keine bereits verwendeten Ausdrücke anzubieten
	 * @param setCommandCallback	Callback zum Setzen eines neuen Befehls
	 */
	private static void commandTemplateExpression(final JButton button, final EditModel model, final Set<String> usedExpressionsLower, final Consumer<String> setCommandCallback) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;
		boolean needSeparator=false;

		final List<ModelElementBox> boxesWIP=getBoxes(BOXES_WIP,model);
		final List<ModelElementBox> boxesNQ=getBoxes(BOXES_NQ,model);
		final List<ModelElementBox> boxesCounter=getBoxes(BOXES_COUNTER,model);
		final List<ModelElementBox> boxesCounterPart=getBoxes(BOXES_COUNTER_PART,model);
		final Map<String,int[]> clientTypes=getClientTypes(model);

		final AddItemData addItemData=new AddItemData();
		addItemData.setCommandCallback=setCommandCallback;

		/* Aktuelle Anzahl an Kunden */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.WIP"));
		if (!isInUse(COMMAND_WIP,(ModelElementBox)null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.inSystem"),"WIP()");
		boxesWIP.stream().filter(box->!isInUse(COMMAND_WIP,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"WIP("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Anzahl an Kunden */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.WIP_avg"));
		if (!isInUse(COMMAND_WIP_AVG,(ModelElementBox)null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.inSystem"),"WIP_avg()");
		boxesWIP.stream().filter(box->!isInUse(COMMAND_WIP_AVG,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"WIP_avg("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Aktuelle Warteschlangenlänge */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.NQ"));
		if (!isInUse(COMMAND_NQ,(ModelElementBox)null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.inSystemQueues"),"NQ()");
		boxesNQ.stream().filter(box->!isInUse(COMMAND_NQ,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"NQ("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Warteschlangenlänge */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.NQ_avg"));
		if (!isInUse(COMMAND_NQ_AVG,(ModelElementBox)null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.inSystemQueues"),"NQ_avg()");
		boxesNQ.stream().filter(box->!isInUse(COMMAND_NQ_AVG,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"NQ_avg("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		if (needSeparator) {popup.addSeparator(); needSeparator=false;}

		/* Zählerwert */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.CounterValue"));
		boxesCounter.stream().filter(box->!isInUse(COMMAND_COUNTER,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"Counter("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Zähleranteil */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.CounterPart"));
		boxesCounterPart.stream().filter(box->!isInUse(COMMAND_COUNTER_PART,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"Part("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		if (needSeparator) {popup.addSeparator(); needSeparator=false;}

		/* Mittlere Wartezeiten der Kunden */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.ClientWaitingTimes"));
		if (!isInUse(COMMAND_WAITING_TIME,(int[])null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.allClients"),"WaitingTime_avg()");
		for (var clientType: clientTypes.entrySet()) {
			if (isInUse(COMMAND_WAITING_TIME,clientType.getValue(),usedExpressionsLower)) continue;
			final String clientTypeID=String.join(";",Arrays.stream(clientType.getValue()).mapToObj(i->""+i).toArray(String[]::new));
			addItem(addItemData,clientType.getKey(),"WaitingTime_avg("+clientTypeID+")");
		}
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Transferzeiten der Kunden */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.ClientTransferTimes"));
		if (!isInUse(COMMAND_TRANSFER_TIME,(int[])null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.allClients"),"TransferTime_avg()");
		for (var clientType: clientTypes.entrySet()) {
			if (isInUse(COMMAND_TRANSFER_TIME,clientType.getValue(),usedExpressionsLower)) continue;
			final String clientTypeID=String.join(";",Arrays.stream(clientType.getValue()).mapToObj(i->""+i).toArray(String[]::new));
			addItem(addItemData,clientType.getKey(),"TransferTime_avg("+clientTypeID+")");
		}
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Bedienzeiten der Kunden */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.ClientProcessTimes"));
		if (!isInUse(COMMAND_PROCESS_TIME,(int[])null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.allClients"),"ProcessTime_avg()");
		for (var clientType: clientTypes.entrySet()) {
			if (isInUse(COMMAND_PROCESS_TIME,clientType.getValue(),usedExpressionsLower)) continue;
			final String clientTypeID=String.join(";",Arrays.stream(clientType.getValue()).mapToObj(i->""+i).toArray(String[]::new));
			addItem(addItemData,clientType.getKey(),"ProcessTime_avg("+clientTypeID+")");
		}
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Verweilzeiten der Kunden */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.ClientResidenceTimes"));
		if (!isInUse(COMMAND_RESIDENCE_TIME,(int[])null,usedExpressionsLower)) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.allClients"),"ResidenceTime_avg()");
		for (var clientType: clientTypes.entrySet()) {
			if (isInUse(COMMAND_RESIDENCE_TIME,clientType.getValue(),usedExpressionsLower)) continue;
			final String clientTypeID=String.join(";",Arrays.stream(clientType.getValue()).mapToObj(i->""+i).toArray(String[]::new));
			addItem(addItemData,clientType.getKey(),"ResidenceTime_avg("+clientTypeID+")");
		}
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		if (needSeparator) {popup.addSeparator(); needSeparator=false;}

		/* Mittlere Wartezeit an einer Station */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.StationWaitingTime"));
		boxesNQ.stream().filter(box->!isInUse(COMMAND_WAITING_TIME,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"WaitingTime_avg("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Rüstzeit an einer Station */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.StationSetupTime"));
		boxesNQ.stream().filter(box->!isInUse(COMMAND_SETUP_TIME,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"SetupTime_avg("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Bedienzeit an einer Station */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.StationProcessTime"));
		boxesNQ.stream().filter(box->!isInUse(COMMAND_PROCESS_TIME,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"ProcessTime_avg("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Verweilzeit an einer Station */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.StationProcessTime"));
		boxesNQ.stream().filter(box->!isInUse(COMMAND_RESIDENCE_TIME,box,usedExpressionsLower)).forEach(box->addItem(addItemData,stationName(box),"ResidenceTime_avg("+box.getId()+")"));
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		if (needSeparator) {popup.addSeparator(); needSeparator=false;}

		/* Aktuelle Auslastung */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.Utilization"));
		if (!usedExpressionsLower.contains("resource()/resource_count()")) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.UtilizationOverall"),"Resource()/Resource_count()");
		for (int i=0;i<model.resources.size();i++) {
			final int nr=i+1;
			if (!usedExpressionsLower.contains("resource("+nr+")/resource_count("+nr+")")) addItem(addItemData,String.format(Language.tr("AnimationExpression.ExpressionTemplates.UtilizationInGroup"),nr,model.resources.getName(i)),"Resource("+nr+")/Resource_count("+nr+")");
		}
		if (addItemData.popupMenuSection.getItemCount()>0) {popup.add(addItemData.popupMenuSection); needSeparator=true;}

		/* Mittlere Auslastung */
		addItemData.popupMenuSection=new JMenu(Language.tr("AnimationExpression.ExpressionTemplates.UtilizationAverage"));
		if (!usedExpressionsLower.contains("resource()/resource_count()")) addItem(addItemData,Language.tr("AnimationExpression.ExpressionTemplates.UtilizationAverageOverall"),"Resource_avg()/Resource_count()");
		for (int i=0;i<model.resources.size();i++) {
			final int nr=i+1;
			if (!usedExpressionsLower.contains("resource_avg("+nr+")/resource_count("+nr+")")) addItem(addItemData,String.format(Language.tr("AnimationExpression.ExpressionTemplates.UtilizationAverageInGroup"),nr,model.resources.getName(i)),"Resource_avg("+nr+")/Resource_count("+nr+")");
		}
		if (addItemData.popupMenuSection.getItemCount()>0) popup.add(addItemData.popupMenuSection);

		/* Nichts passendes gefunden? */
		if (popup.getComponentCount()==0) {
			popup.add(item=new JMenuItem(Language.tr("AnimationExpression.ExpressionTemplates.NoTemplates")));
			item.setEnabled(false);
		}

		popup.show(button,0,button.getHeight());
	}

	/**
	 * Erstellt eine Schaltfläche zur Auswahl von Vorgabe-Rechenausdrücke für Diagrammelemente und ähnliche
	 * @param model	Modell dem die Daten zu den Stationen usw. entnommen werden sollen
	 * @param setCommandCallback	Callback zum Einstellen eines neuen Rechenausdrucks
	 * @return	Button zum Aufruf des Popupmenüs
	 */
	public static JButton getTemplatesButton(final EditModel model, final Consumer<String> setCommandCallback) {
		final JButton templatesButton=new JButton(Images.GENERAL_INFO.getIcon());
		templatesButton.setToolTipText(Language.tr("AnimationExpression.ExpressionTemplates"));
		templatesButton.addActionListener(e->commandTemplateExpression(templatesButton,model,new HashSet<>(),setCommandCallback));
		return templatesButton;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessages	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	public boolean checkData(final boolean showErrorMessages) {
		if (readOnly || modeSelect.getSelectedIndex()!=0) return true;

		boolean ok=true;

		final String text=expressionEdit.getText().trim();
		if (text.isEmpty()) {
			ok=false;
			expressionEdit.setBackground(Color.RED);
			if (showErrorMessages) {
				MsgBox.error(this,Language.tr("AnimationExpression.Expression.ErrorTitle"),Language.tr("AnimationExpression.Expression.ErrorInfoNoExpression"));
				return false;
			}
		} else {
			int error=ExpressionCalc.check(text,element.getSurface().getMainSurfaceVariableNames(element.getModel().getModelVariableNames(),false),element.getModel().userFunctions);
			if (error>=0) {
				ok=false;
				expressionEdit.setBackground(Color.RED);
				if (showErrorMessages) {
					MsgBox.error(this,Language.tr("AnimationExpression.Expression.ErrorTitle"),String.format(Language.tr("AnimationExpression.Expression.ErrorInfoInvalidExpression"),text,error+1));
					return false;
				}
			} else {
				expressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}

		return ok;
	}

	/**
	 * Schreibt die Daten in das im Konstruktor übergebene
	 * {@link AnimationExpression}-Objekt zurück.
	 */
	public void storeData() {
		if (readOnly) return;

		if (modeSelect.getSelectedIndex()==0) {
			expression.setExpression(expressionEdit.getText().trim());
		} else {
			switch (scriptMode) {
			case Expression:
				/* Haben wir in diesem Fall nicht. */
				break;
			case Javascript:
				expression.setJavascript(script);
				break;
			case Java:
				expression.setJava(script);
				break;
			}
		}
	}
}
