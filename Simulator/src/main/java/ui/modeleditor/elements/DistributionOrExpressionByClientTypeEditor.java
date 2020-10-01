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
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.runmodel.RunModel;
import simulator.simparser.ExpressionCalc;
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
	private static final long serialVersionUID = 3506994764763479366L;

	private final String[] variables;
	private final String[] clientTypes;

	private AbstractRealDistribution distribution;
	private String expression;
	private final boolean readOnly;
	private final List<AbstractRealDistribution> clientTypeDistribution;
	private final List<String> clientTypeExpression;
	private int modeGlobal;
	private final int[] modes;

	private int clientTypeLast;

	private final JComboBox<String> clientTypeSelect;
	private final JCheckBox clientTypeActive;
	private final JComboBox<String> modeSelect;
	private final JPanel editArea;
	private final JDistributionPanel distributionPanel;
	private final JTextField expressionEdit;

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
		variables=namesList.toArray(new String[0]);

		/* Daten vorbereiten */
		this.readOnly=readOnly;
		clientTypes=surface.getClientTypes().toArray(new String[0]);
		clientTypeDistribution=new ArrayList<>();
		for (int i=0;i<clientTypes.length;i++) clientTypeDistribution.add(null);
		clientTypeExpression=new ArrayList<>();
		for (int i=0;i<clientTypes.length;i++) clientTypeExpression.add(null);
		modes=new int[clientTypes.length];

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
		sub.add(clientTypeSelect=new JComboBox<>(items.toArray(new String[0])));
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

		infoPanel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(modeSelect=new JComboBox<>(new String[]{titleDist,titleExpression}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODE_DISTRIBUTION,
				Images.MODE_EXPRESSION
		}));
		modeSelect.setEnabled(!readOnly);

		add(editArea=new JPanel(new CardLayout()),BorderLayout.CENTER);

		editArea.add(sub=new JPanel(new BorderLayout()),"dist");
		sub.add(distributionPanel=new JDistributionPanel(null,3600,!readOnly),BorderLayout.CENTER);


		editArea.add(sub=new JPanel(new BorderLayout()),"expr");

		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DistributionByClientTypeEditor.Expression")+":","");
		sub.add((JPanel)obj[0],BorderLayout.NORTH);
		expressionEdit=(JTextField)obj[1];
		expressionEdit.setEditable(!readOnly);
		expressionEdit.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {expressionEditChanged();}
			@Override public void keyReleased(KeyEvent e) {expressionEditChanged();}
			@Override public void keyPressed(KeyEvent e) {expressionEditChanged();}
		});
		((JPanel)obj[0]).add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,true,model,surface),BorderLayout.EAST);
	}

	private void checkExpression() {
		if (!readOnly) {
			String text=expressionEdit.getText().trim();
			if (text.isEmpty()) {
				expressionEdit.setBackground(SystemColor.text);
			} else {
				final int index=ExpressionCalc.check(text,variables);
				if (index>=0) expressionEdit.setBackground(Color.red); else expressionEdit.setBackground(SystemColor.text);
			}
		}
	}

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

		int index=-1;
		for (int i=0;i<clientTypes.length;i++) if (clientTypes[i].equalsIgnoreCase(name)) {index=i; break;}
		setData(index,distribution,expression);
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

	private void activeModeChanged() {
		String card=(modeSelect.getSelectedIndex()==0)?"dist":"expr";
		((CardLayout)editArea.getLayout()).show(editArea,card);

		if (modeSelect.getSelectedIndex()==1) checkExpression();
	}

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
}
