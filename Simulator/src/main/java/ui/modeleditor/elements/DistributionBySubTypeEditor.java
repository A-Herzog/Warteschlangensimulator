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
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.editmodel.EditModel;
import simulator.simparser.ExpressionCalc;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelElementBaseDialog;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfaceAnimatorBase;
import ui.modeleditor.coreelements.ModelElement;

/**
 * Stellt einen Verteilungseditor, der eine globale Verteilung und optionale kundentyp- oder stations-abhängige
 * Verteilungen vorhält, bereit.
 * @author Alexander Herzog
 */
public class DistributionBySubTypeEditor extends JPanel {
	private static final long serialVersionUID = 3506994764763479366L;

	/**
	 * Sind die Subtypen Kundentypen oder Zielstationen?
	 * @author Alexander Herzog
	 * @see DistributionBySubTypeEditor#DistributionBySubTypeEditor(EditModel, ModelSurface, boolean, String, DistributionSystem, Mode)
	 */
	public enum Mode {
		/** Subtypen sind Kundentypen */
		MODE_CLIENTS,
		/** Subtypen sind Zielstationen */
		MODE_TRANSPORT_DESTINATION
	}

	private static final String cardDistribution="Distribution";
	private static final String cardExpression="Expression";

	private final EditModel model;
	private final ModelSurface surface;
	private final boolean readOnly;
	private final String[] subTypes;
	private final DistributionSystem sourceData;
	private final DistributionSystem data;

	private int subTypeLast;
	private final JComboBox<String> subTypeSelect;
	private final JLabel localIsActive;
	private final JCheckBox useGlobal;
	private final JComboBox<String> modeSelect;
	private final JPanel cards;
	private final JDistributionPanel distributionPanel;
	private final JPanel expressionLines;
	private final JTextField expressionEdit;

	private List<ActionListener> userChangeListeners;

	/**
	 * Konstruktor der Klasse <code>DistributionBySubTypeEditor</code>
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Element vom Typ <code>ModelSurface</code> (wird benötigt, um die Liste der Kundentypen oder Stationen auszulesen)
	 * @param readOnly	Gibt an, ob die Daten bearbeitet werden dürfen, oder ob sich das System im Read-Only-Modus befindet
	 * @param title	Titel über der Verteilungsansicht (z.B. "Verteilung der Verzögerungszeiten")
	 * @param data	Zu bearbeitende Daten (das Objekt wird durch den Aufruf von <code>storeData</code> aktualisiert)
	 * @param mode	Sind die Subtypen Kundentypen oder Zielstationen?
	 * @see Mode
	 */
	public DistributionBySubTypeEditor(final EditModel model, final ModelSurface surface, final boolean readOnly, final String title, final DistributionSystem data, final Mode mode) {
		super();

		userChangeListeners=new ArrayList<>();

		/* Daten vorbereiten */
		this.model=model;
		this.surface=surface;
		this.readOnly=readOnly;
		switch(mode) {
		case MODE_CLIENTS:
			subTypes=surface.getClientTypes().toArray(new String[0]);
			break;
		case MODE_TRANSPORT_DESTINATION:
			List<String> destinations=new ArrayList<>();
			ModelSurface mainSurface=surface;
			if (mainSurface.getParentSurface()!=null) mainSurface=mainSurface.getParentSurface();
			for (ModelElement element: mainSurface.getElements()) {
				if (element instanceof ModelElementTransportDestination && !element.getName().isEmpty() && destinations.indexOf(element.getName())<0) destinations.add(element.getName());
				if (element instanceof ModelElementSub) {
					for (ModelElement subelement: ((ModelElementSub)element).getSubSurface().getElements()) {
						if (subelement instanceof ModelElementTransportDestination && !subelement.getName().isEmpty() && destinations.indexOf(subelement.getName())<0) destinations.add(subelement.getName());
					}
				}
			}
			subTypes=destinations.toArray(new String[0]);
			break;
		default:
			subTypes=surface.getClientTypes().toArray(new String[0]);
		}
		sourceData=data;
		this.data=data.clone();

		/* GUI aufbauen */
		setLayout(new BorderLayout(0,5));
		final JPanel infoPanel=new JPanel();
		infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.PAGE_AXIS));
		add(infoPanel,BorderLayout.NORTH);

		JPanel sub, linesParent;

		infoPanel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		List<String> items=new ArrayList<>();
		String typeLabel="";
		switch (mode) {
		case MODE_CLIENTS:
			items.add(Language.tr("Surface.DistributionByClientTypeEditor.Global"));
			typeLabel=Language.tr("Surface.DistributionByClientTypeEditor.ByType");
			break;
		case MODE_TRANSPORT_DESTINATION:
			items.add(Language.tr("Surface.DistributionByClientTypeEditor.GlobalStation"));
			typeLabel=Language.tr("Surface.DistributionByClientTypeEditor.ByTypeStation");
			break;
		}
		for (String typ : subTypes) items.add(typeLabel+" \""+typ+"\"");
		sub.add(subTypeSelect=new JComboBox<>(items.toArray(new String[0])));
		if (mode==Mode.MODE_CLIENTS) {
			final AnimationImageSource imageSource=new AnimationImageSource();
			final List<Object> modeIcons=new ArrayList<>();
			modeIcons.add(Images.MODELPROPERTIES_CLIENTS_GROUPS.getIcon());
			for (String typ : subTypes) {
				String icon=model.clientData.getIcon(typ);
				if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
				modeIcons.add(imageSource.get(icon,model.animationImages,16));
			}
			subTypeSelect.setRenderer(new IconListCellRenderer(IconListCellRenderer.buildIconsList(this,modeIcons)));
		}
		sub.add(useGlobal=new JCheckBox(Language.tr("Surface.DistributionByClientTypeEditor.UseGlobal")));
		useGlobal.setEnabled(!readOnly);

		final String localInfo;
		switch (mode) {
		case MODE_CLIENTS:
			localInfo=Language.tr("Surface.DistributionByClientTypeEditor.LocalIsActive.ClientType");
			break;
		case MODE_TRANSPORT_DESTINATION:
			localInfo=Language.tr("Surface.DistributionByClientTypeEditor.LocalIsActive.Station");
			break;
		default:
			localInfo="";
			break;
		}
		sub.add(localIsActive=new JLabel(localInfo));
		localIsActive.setVisible(false);

		infoPanel.add(sub=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		sub.add(new JLabel("<html><body>"+title+"&nbsp;</body></html>"));
		sub.add(modeSelect=new JComboBox<>(new String[]{
				Language.tr("Surface.DistributionByClientTypeEditor.DefineByDistribution"),
				Language.tr("Surface.DistributionByClientTypeEditor.DefineByExpression")
		}));
		modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODE_DISTRIBUTION,
				Images.MODE_EXPRESSION
		}));
		modeSelect.setEnabled(!readOnly);

		add(cards=new JPanel(new CardLayout()),BorderLayout.CENTER);
		cards.add(distributionPanel=new JDistributionPanel(null,3600,!readOnly) {
			private static final long serialVersionUID = -8375312389773855243L;
			@Override
			protected boolean editButtonClicked() {
				fireUserChangeListener();
				if (subTypeSelect.getSelectedIndex()>0 && !readOnly) useGlobal.setSelected(false);
				return super.editButtonClicked();
			}
		},cardDistribution);

		cards.add(linesParent=new JPanel(new FlowLayout(FlowLayout.LEFT)),cardExpression);
		linesParent.add(expressionLines=new JPanel());
		expressionLines.setLayout(new BoxLayout(expressionLines,BoxLayout.PAGE_AXIS));
		Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DistributionByClientTypeEditor.Expression")+":","");
		sub=(JPanel)obj[0];
		expressionLines.add(sub);
		expressionEdit=(JTextField)obj[1];
		sub.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,true,model,surface),BorderLayout.EAST);
		expressionEdit.setEditable(!readOnly);
		expressionEdit.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {fireUserChangeListener(); checkExpression(); if (subTypeSelect.getSelectedIndex()>0 && !readOnly) useGlobal.setSelected(false);}
			@Override public void keyPressed(KeyEvent e) {fireUserChangeListener(); checkExpression(); if (subTypeSelect.getSelectedIndex()>0 && !readOnly) useGlobal.setSelected(false);}
			@Override public void keyReleased(KeyEvent e) {fireUserChangeListener(); checkExpression(); if (subTypeSelect.getSelectedIndex()>0 && !readOnly) useGlobal.setSelected(false);}
		});

		/* Verarbeitung starten */
		subTypeLast=-1;
		subTypeSelect.addActionListener(e->activeClientTypeChanged());
		modeSelect.addActionListener(e->{
			setCard((modeSelect.getSelectedIndex()==0)?cardDistribution:cardExpression);
			if (subTypeSelect.getSelectedIndex()>0 && !readOnly) useGlobal.setSelected(false);
			checkExpression();
		});
		subTypeSelect.setSelectedIndex(0);
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

	private void fireUserChangeListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"");
		for (ActionListener listener: userChangeListeners) listener.actionPerformed(event);
	}

	/**
	 * Schreibt die über die GUI vorgenommenen Änderungen in das dem Konstruktor übergebene Datenobjekt zurück.
	 */
	public void storeData() {
		activeClientTypeChanged();
		sourceData.setData(data);
	}

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

	private void checkExpression() {
		if (expressionEdit.getText().trim().isEmpty()) {
			expressionEdit.setBackground(Color.red);
			return;
		}

		final int error=ExpressionCalc.check(expressionEdit.getText(),surface.getMainSurfaceVariableNames(model.getModelVariableNames(),true));
		if (error>=0) expressionEdit.setBackground(Color.red); else expressionEdit.setBackground(SystemColor.text);
	}

	private void activeClientTypeChanged() {
		if (subTypeLast>=0 && !readOnly) {
			final String clientTypeName=(subTypeLast==0)?null:subTypes[subTypeLast-1];
			Object obj;
			if (modeSelect.getSelectedIndex()==0) {
				obj=distributionPanel.getDistribution();
			} else {
				obj=expressionEdit.getText();
			}
			if (subTypeLast>0 && useGlobal.isSelected()) {
				data.set(clientTypeName,null);
			} else {
				data.set(clientTypeName,obj);
			}
		}

		subTypeLast=subTypeSelect.getSelectedIndex();
		final String clientTypeName=(subTypeLast==0)?null:subTypes[subTypeLast-1];
		useGlobal.setVisible(subTypeLast>0);
		localIsActive.setVisible(subTypeLast==0 && data.hasSubTypeData()); // XXX + nur wenn Untereinträge aktiv
		useGlobal.setEnabled(subTypeLast>0 && !readOnly);
		Object obj=data.get(clientTypeName);
		boolean isFallBack=false;
		if (obj==null && subTypeLast>0) {
			obj=data.get();
			isFallBack=true;
		}
		if (obj==null) {
			setCard(cardDistribution);
			modeSelect.setSelectedIndex(0);
			distributionPanel.setDistribution(new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
			expressionEdit.setText("");
			checkExpression();
			useGlobal.setSelected(true);
		} else {
			if (obj instanceof String) {
				setCard(cardExpression);
				modeSelect.setSelectedIndex(1);
				distributionPanel.setDistribution(new ExponentialDistribution(null,50,ExponentialDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY));
				expressionEdit.setText((String)obj);
				checkExpression();
				useGlobal.setSelected(subTypeLast==0 || isFallBack);
			}
			if (obj instanceof AbstractRealDistribution) {
				setCard(cardDistribution);
				modeSelect.setSelectedIndex(0);
				distributionPanel.setDistribution((AbstractRealDistribution)obj);
				expressionEdit.setText("");
				checkExpression();
				useGlobal.setSelected(subTypeLast==0 || isFallBack);
			}
		}
	}
}
