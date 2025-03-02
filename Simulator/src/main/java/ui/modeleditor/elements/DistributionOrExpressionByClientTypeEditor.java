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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionPanel;
import mathtools.distribution.tools.DistributionTools;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.simparser.ExpressionCalc;
import simulator.simparser.ExpressionCalcModelUserFunctions;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;

/**
 * Stellt einen Verteilungseditor, der eine globale Verteilung und optionale kundentyp-abhängige
 * Verteilungen sowie die Möglichkeit, einen Ausdruck statt einer Verteilung zu verwenden, vorhält, bereit.
 * @author Alexander Herzog
 */
public class DistributionOrExpressionByClientTypeEditor extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 3506994764763479366L;

	/**
	 * Namen der modellweiten Variablen
	 */
	private final String[] variables;

	/**
	 * Modellspezifische nutzerdefinierte Funktionen
	 */
	private ExpressionCalcModelUserFunctions userFunctions;

	/**
	 * Namen der Kundentypen im System
	 */
	private final String[] clientTypes;

	/**
	 * Zuordnung von Kundentypen zu Indices in {@link #clientTypes},
	 * um bei einem gegebenen Namen den Index schneller ermitteln zu können.
	 * @see #clientTypes
	 * @see #setData(String, AbstractRealDistribution, String)
	 */
	private final Map<String,Integer> clientTypesMap;

	/** Globale Verteilung */
	private AbstractRealDistribution distribution;
	/** Globale Rechenausdruck */
	private String expression;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Verteilungen für die Untertypen */
	private final List<AbstractRealDistribution> clientTypeDistribution;
	/** Rechenausdrücke für die Untertypen */
	private final List<String> clientTypeExpression;
	/** Gewählter Modus für den globalen Fall */
	private int modeGlobal;
	/** Gewählte Modi für die Untertypen */
	private final int[] modes;

	/** Letzter in {@link #clientTypes} gewählter Eintrag */
	private int clientTypeLast;

	/** Auswahlbox zur Auswahl des Untertyp */
	private final JComboBox<String> clientTypeSelect;
	/** Sind Einstellung für diesen Kundentyp aktiv? */
	private final JCheckBox clientTypeActive;
	/** Schaltfläche zum Laden von Kundentypdaten */
	private final JButton loadButton;
	/** Aktueller Modus: Verteilung oder Rechenausdruck */
	private final JComboBox<String> modeSelect;
	/** Panel das den Verteilungseditor oder das Eingabefeld für den Rechenausdruck aufnimmt */
	private final JPanel editArea;
	/** Verteilungseditor */
	private final JDistributionPanel distributionPanel;
	/** Eingabefeld für den Rechenausdruck */
	private final JTextField expressionEdit;

	/** Konfigurierbare Schaltfläche (z.B. für Datenübernahme von anderen Stationen) */
	private final JButton specialButton;

	/**
	 * Konstruktor der Klasse <code>DistributionOrExpressionByClientTypeEditor</code>
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Element vom Typ <code>ModelSurface</code> (wird benötigt, um die Liste der Kundentypen auszulesen)
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen, oder ob sich das System im Read-Only-Modus befindet
	 * @param titleDist	Titel über der Verteilungsansicht (z.B. "Verteilung der Verzögerungszeiten")
	 * @param titleExpression	Titel über dem Ausdruckseditor (z.B. "Ausdruck zur Bestimmung der Verzögerungszeiten")
	 */
	public DistributionOrExpressionByClientTypeEditor(final EditModel model, final ModelSurface surface, final boolean readOnly, final String titleDist, final String titleExpression) {
		super();

		final List<String> namesList=new ArrayList<>(Arrays.asList(surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true)));
		for (String var: RunModel.additionalVariables) {
			boolean inList=false;
			for (String s: namesList) if (s.equalsIgnoreCase(var)) {inList=true; break;}
			if (!inList) namesList.add(var);
		}
		variables=namesList.toArray(String[]::new);
		userFunctions=model.userFunctions;

		/* Daten vorbereiten */
		this.readOnly=readOnly;
		clientTypes=surface.getClientTypes().toArray(String[]::new);
		clientTypeDistribution=new ArrayList<>();
		for (int i=0;i<clientTypes.length;i++) clientTypeDistribution.add(null);
		clientTypeExpression=new ArrayList<>();
		for (int i=0;i<clientTypes.length;i++) clientTypeExpression.add(null);
		modes=new int[clientTypes.length];

		clientTypesMap=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (int i=0;i<clientTypes.length;i++) clientTypesMap.put(clientTypes[i],i);

		/* GUI aufbauen */
		setLayout(new BorderLayout(0,5));
		final JPanel infoPanel=new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.PAGE_AXIS));
		add(infoPanel,BorderLayout.NORTH);

		JPanel sub;

		infoPanel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		List<String> items=new ArrayList<>();
		items.add(Language.tr("Surface.DistributionByClientTypeEditor.Global"));
		for (String typ : clientTypes) items.add(Language.tr("Surface.DistributionByClientTypeEditor.ByType")+" \""+typ+"\"");
		sub.add(clientTypeSelect=new JComboBox<>(items.toArray(String[]::new)));
		final AnimationImageSource imageSource=new AnimationImageSource();
		final List<Object> modeIcons=new ArrayList<>();
		modeIcons.add(Images.MODELPROPERTIES_CLIENTS_GROUPS.getIcon());
		for (String typ : clientTypes) {
			String icon=model.clientData.getIcon(typ);
			if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
			modeIcons.add(imageSource.get(icon,model.animationImages,16));
		}
		clientTypeSelect.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildIconsList(this,modeIcons)));

		infoPanel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(clientTypeActive=new JCheckBox(Language.tr("Surface.DistributionByClientTypeEditor.UseGlobal")));
		clientTypeActive.setEnabled(!readOnly);
		sub.add(Box.createHorizontalGlue());
		sub.add(loadButton=new JButton(Language.tr("ClientTypeLoader.Button"),Images.GENERAL_LOAD.getIcon()));
		loadButton.setToolTipText(Language.tr("ClientTypeLoader.Title"));
		loadButton.setEnabled(!readOnly);
		loadButton.addActionListener(e->loadClientTypeData());

		infoPanel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(modeSelect=new JComboBox<>(new String[]{titleDist,titleExpression}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODE_DISTRIBUTION,
				Images.MODE_EXPRESSION
		}));
		modeSelect.setEnabled(!readOnly);

		sub.add(Box.createHorizontalStrut(10));
		sub.add(specialButton=new JButton());
		specialButton.setVisible(false);

		add(editArea=new JPanel(new CardLayout()),BorderLayout.CENTER);

		editArea.add(sub=new JPanel(new BorderLayout()),"dist");
		sub.add(distributionPanel=new JDistributionPanel(null,3600,!readOnly,s->toExpression(s)),BorderLayout.CENTER);


		editArea.add(sub=new JPanel(new BorderLayout()),"expr");

		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DistributionByClientTypeEditor.Expression")+":","");
		sub.add((JPanel)obj[0],BorderLayout.NORTH);
		expressionEdit=(JTextField)obj[1];
		expressionEdit.setEnabled(!readOnly);
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {expressionEditChanged();}
			@Override public void keyReleased(KeyEvent e) {expressionEditChanged();}
			@Override public void keyPressed(KeyEvent e) {expressionEditChanged();}
		});
		((JPanel)obj[0]).add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,true,model,surface),BorderLayout.EAST);
	}

	/**
	 * Konfiguriert und aktiviert die zusätzliche Schaltfläche.
	 * @param text	Beschriftung der Schaltfläche (darf <code>null</code> sein)
	 * @param tooltip	Tooltip für die Schaltfläche (darf <code>null</code> sein)
	 * @param icon	Icon auf der Schaltfläche (darf <code>null</code> sein)
	 * @param callback	Wird beim Anklicken aufgerufen
	 */
	public void setupSpecialButton(final String text, final String tooltip, final Icon icon, Consumer<JButton> callback) {
		if (text!=null) specialButton.setText(text);
		if (tooltip!=null) specialButton.setToolTipText(tooltip);
		if (icon!=null) specialButton.setIcon(icon);
		specialButton.addActionListener(e->callback.accept(specialButton));
		specialButton.setVisible(true && !readOnly);
	}

	/**
	 * Stellt den angegebenen Rechenausdruck ein.
	 * @param expression	Rechenausdruck
	 */
	private void toExpression(final String expression) {
		modeSelect.setSelectedIndex(1);
		activeModeChanged();
		expressionEdit.setText(expression);
		expressionEditChanged();
	}

	/**
	 * Prüft den eingegebenen Rechenausdruck.
	 */
	private void checkExpression() {
		if (!readOnly) {
			String text=expressionEdit.getText().trim();
			if (text.isEmpty()) {
				expressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			} else {
				final int index=ExpressionCalc.check(text,variables,userFunctions);
				if (index>=0) expressionEdit.setBackground(Color.red); else expressionEdit.setBackground(NumberTools.getTextFieldDefaultBackground());
			}
		}
	}

	/**
	 * Wird aufgerufen, wenn sich die Eingabe in {@link #expressionEdit} ändert.
	 */
	private void expressionEditChanged() {
		if (clientTypeSelect.getSelectedIndex()>0 && !readOnly && clientTypeActive.isSelected()) {
			String expr=expression;
			if (expr==null) expression="";
			if (!expressionEdit.getText().equals(expr)) clientTypeActive.setSelected(false);
		}
		checkExpression();
	}

	/**
	 * Liste alle Kundentypen (aus dem dem Konstruktor übergebenen <code>ModelSurface</code>-Objekt ausgelesen)
	 * @return	Liste aller Kundentypen im System
	 */
	public String[] getClientTypes() {
		return Arrays.copyOf(clientTypes,clientTypes.length);
	}

	/**
	 * Übergibt dem Editor eine Verteilung und einen Ausdruck
	 * @param index	Kundentyp, für den die Verteilung/der Ausdruck eingestellt werden soll (-1 für die globale Verteilung, sonst ein Index gemäß der von <code>getClientTypes()</code> gelieferten Liste)
	 * @param distribution	Zu setzende Verteilung (kann <code>null</code> sein, dann muss <code>expression</code> ungleich <code>null</code> sein)
	 * @param expression	Zu setzender Ausdruck (wird nur berücksichtigt, wenn <code>distribution==null</code> ist; muss dann ungleich <code>null</code> sein)
	 * @see #getClientTypes()
	 * @see #start()
	 */
	public void setData(final int index, final AbstractRealDistribution distribution, final String expression) {
		if (index<0) {
			this.distribution=distribution;
			this.expression=expression;
			if (distribution!=null) modeGlobal=0; else modeGlobal=1;
			return;
		}
		if (index<clientTypeDistribution.size()) {
			clientTypeDistribution.set(index,distribution);
			clientTypeExpression.set(index,expression);
			if (distribution!=null) modes[index]=0; else {
				if (expression!=null && !expression.trim().isEmpty()) modes[index]=1; else modes[index]=0;
			}
		}
	}

	/**
	 * Übergibt dem Editor die globale Verteilung und den globalen Ausdruck
	 * @param distribution	Zu setzende Verteilung (kann <code>null</code> sein, dann muss <code>expression</code> ungleich <code>null</code> sein)
	 * @param expression	Zu setzender Ausdruck (wird nur berücksichtigt, wenn <code>distribution==null</code> ist; muss dann ungleich <code>null</code> sein)
	 * @see #start()
	 */
	public void setData(final AbstractRealDistribution distribution, final String expression) {
		setData(-1,distribution,expression);
	}

	/**
	 * Übergibt dem Editor eine Verteilung und einen Ausdruck
	 * @param name Name des Kundentyps, für den die Verteilung eingestellt werden soll, oder <code>null</code>, wenn die globale Verteilung eingestellt werden soll
	 * @param distribution	Zu setzende Verteilung (kann <code>null</code> sein, dann muss <code>expression</code> ungleich <code>null</code> sein)
	 * @param expression	Zu setzender Ausdruck (wird nur berücksichtigt, wenn <code>distribution==null</code> ist; muss dann ungleich <code>null</code> sein)
	 */
	public void setData(final String name, final AbstractRealDistribution distribution, final String expression) {
		if (name==null || name.trim().isEmpty()) {setData(-1,distribution,expression); return;}

		final Integer index=clientTypesMap.get(name);
		if (index!=null) setData(index,distribution,expression);
	}

	/**
	 * Stellt neue Daten für die aktuell angezeigte Seite ein.
	 * @param data	Neue Daten (Verteilung oder Rechenausdruck)
	 */
	public void setDataForCurrentView(final Object data) {
		if (data instanceof AbstractRealDistribution) {
			if (clientTypeSelect.getSelectedIndex()>0) clientTypeActive.setSelected(false);
			modeSelect.setSelectedIndex(0);
			activeModeChanged();
			distributionPanel.setDistribution(DistributionTools.cloneDistribution((AbstractRealDistribution)data));
			activeClientTypeChanged();
		}
		if (data instanceof String) {
			if (clientTypeSelect.getSelectedIndex()>0) clientTypeActive.setSelected(false);
			modeSelect.setSelectedIndex(1);
			activeModeChanged();
			expressionEdit.setText((String)data);
			activeClientTypeChanged();
		}
	}

	/**
	 * Initialisiert die GUI nach dem über <code>setData()</code> alle Verteilungen und Ausdrücke eingetragen wurden
	 */
	public void start() {
		clientTypeSelect.addActionListener(e->activeClientTypeChanged());
		modeSelect.addActionListener(e->activeModeChanged());
		clientTypeLast=-1;
		clientTypeSelect.setSelectedIndex(0);
	}

	/**
	 * Liefert die globale Verteilung zurück
	 * @return	Globale Verteilung
	 */
	public AbstractRealDistribution getGlobalDistribution() {
		activeClientTypeChanged();
		if (modeGlobal==0) return distribution; else return null;
	}

	/**
	 * Liefert den globalen Ausdruck zurück
	 * @return	Globaler Ausdruck
	 */
	public String getGlobalExpression() {
		activeClientTypeChanged();
		if (modeGlobal==1) return expression; else return null;
	}

	/**
	 * Liefert eine Kundentyp-Verteilung-Zuordnung aller kundentyp-spezifischen Verteilungen
	 * @return	Zuordnung der Kundentyp-Verteilung-Datensätze
	 */
	public Map<String,AbstractRealDistribution> getDistributions() {
		activeClientTypeChanged();

		Map<String,AbstractRealDistribution> map=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (int i=0;i<clientTypes.length;i++) {
			if (modes[i]!=0) continue;
			map.put(clientTypes[i],clientTypeDistribution.get(i));
		}

		return map;
	}

	/**
	 * Liefert eine Kundentyp-Ausdruck-Zuordnung aller kundentyp-spezifischen Ausdrücke
	 * @return	Zuordnung der Kundentyp-Ausdruck-Datensätze
	 */
	public Map<String,String> getExpressions() {
		activeClientTypeChanged();

		Map<String,String> map=new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (int i=0;i<clientTypes.length;i++) {
			if (modes[i]!=1) continue;
			map.put(clientTypes[i],clientTypeExpression.get(i));
		}

		return map;
	}

	/**
	 * Wird aufgerufen, wenn zwischen Verteilungseditor und
	 * Rechenausdruck-Eingabe umgeschaltet wird.
	 * @see #modeSelect
	 */
	private void activeModeChanged() {
		String card=(modeSelect.getSelectedIndex()==0)?"dist":"expr";
		((CardLayout)editArea.getLayout()).show(editArea,card);

		if (modeSelect.getSelectedIndex()==1) checkExpression();
	}

	/**
	 * Wird aufgerufen, wenn der aktuelle Kundentyp für den Editor geändert wurde.
	 * @see #clientTypeLast
	 * @see #clientTypeSelect
	 */
	private void activeClientTypeChanged() {
		if (clientTypeLast>=0 && !readOnly) {
			if (clientTypeLast==0) {
				distribution=distributionPanel.getDistribution();
				expression=expressionEdit.getText();
				modeGlobal=modeSelect.getSelectedIndex();
			} else {
				final int index=clientTypeLast-1;
				if (clientTypeActive.isSelected()) {
					clientTypeDistribution.set(index,null);
					clientTypeExpression.set(index,null);
				} else {
					modes[index]=modeSelect.getSelectedIndex();
					clientTypeDistribution.set(index,distributionPanel.getDistribution());
					clientTypeExpression.set(index, expressionEdit.getText());
				}
			}
		}

		clientTypeLast=clientTypeSelect.getSelectedIndex();

		if (clientTypeLast==0) {
			distributionPanel.setDistribution((distribution!=null)?distribution:new ExponentialDistribution(null,300,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
			expressionEdit.setText((expression!=null)?expression:"");
			modeSelect.setSelectedIndex(modeGlobal);

			clientTypeActive.setSelected(true);
			clientTypeActive.setEnabled(false);
		} else {
			AbstractRealDistribution dist=clientTypeDistribution.get(clientTypeLast-1);
			String expr=clientTypeExpression.get(clientTypeLast-1);

			clientTypeActive.setEnabled(!readOnly);
			clientTypeActive.setSelected(dist==null && (expr==null || expr.trim().isEmpty()));

			if (dist==null) dist=distribution;
			if (dist==null) dist=new ExponentialDistribution(null,300,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
			distributionPanel.setDistribution(dist);
			if (expr==null || expr.trim().isEmpty()) expr=expression;
			if (expr==null) expr="";
			expressionEdit.setText(expr);
			modeSelect.setSelectedIndex(clientTypeActive.isSelected()?modeGlobal:modes[clientTypeLast-1]);
		}
		activeModeChanged();
	}

	/**
	 * Schaltfläche "Kundendaten laden"
	 * @see #loadButton
	 */
	private void loadClientTypeData() {
		final File file=ClientTypeLoader.selectFile(this);
		if (file==null) return;

		final Map<String,Object> newClientTypes=new ClientTypeLoader(file).getProcessingClientTypes();

		if (newClientTypes.size()==0) {
			MsgBox.error(this,Language.tr("ClientTypeLoader.Title"),String.format(Language.tr("ClientTypeLoader.LoadError"),file.toString()));
			return;
		}

		activeClientTypeChanged();

		final Map<String,Object> oldClientTypes=new HashMap<>();
		for (int i=0;i<clientTypes.length;i++) {
			final AbstractRealDistribution dist=clientTypeDistribution.get(i);
			if (dist!=null) oldClientTypes.put(clientTypes[i],dist); else {
				final String expr=clientTypeExpression.get(i);
				if (expr!=null && !expr.trim().isEmpty()) oldClientTypes.put(clientTypes[i],expr);
			}
		}

		final ClientTypeLoaderDialog dialog=new ClientTypeLoaderDialog(this);
		dialog.initProcessTimes(Arrays.asList(clientTypes),oldClientTypes,newClientTypes);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;

		final List<String> activeTypesList=Arrays.asList(clientTypes);
		for (Map.Entry<String,Object> entry: newClientTypes.entrySet()) {
			final int index=activeTypesList.indexOf(entry.getKey());
			if (index>=0) {
				clientTypeDistribution.set(index,null);
				clientTypeExpression.set(index,null);
				final Object obj=entry.getValue();
				if (obj instanceof AbstractRealDistribution) clientTypeDistribution.set(index,(AbstractRealDistribution)obj);
				if (obj instanceof String) clientTypeExpression.set(index,(String)obj);
			}
		}

		clientTypeLast=-1;
		activeClientTypeChanged();
	}
}