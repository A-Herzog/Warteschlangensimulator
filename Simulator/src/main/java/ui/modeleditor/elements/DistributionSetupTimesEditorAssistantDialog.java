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
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.swing.JDistributionPanel;
import simulator.simparser.ExpressionCalc;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import systemtools.images.SimToolsImages;
import tools.IconListCellRenderer;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelElementBaseDialog;

/**
 * Zeigt einen Assistenten-Dialog für die Definition von Rüstzeiten an.
 * @author Alexander Herzog
 * @see DistributionSetupTimesEditor
 */
public class DistributionSetupTimesEditorAssistantDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-8212131999919996936L;

	/**
	 * Editor-Element in das die Daten geschrieben werden sollen
	 */
	private final DistributionSetupTimesEditor editorPanel;

	/**
	 * Schaltfläche, um zur vorherigen Seite zu wechseln
	 */
	private final JButton buttonPrevious;

	/**
	 * Schaltfläche, um zur nächsten Seite zu wechseln
	 */
	private final JButton buttonNext;

	/**
	 * Gesamter Inhaltsbereich
	 */
	private final JPanel content;

	/**
	 * Layout für den Inhaltsbereich
	 * @see #content
	 */
	private final CardLayout contentLayout;

	/**
	 * Index der aktuell in {@link #content} aktiven Seite
	 */
	private int currentCardIndex;

	/**
	 * Modus "Alle Rüstzeiten deaktivieren"
	 */
	private final JRadioButton modeOff;

	/**
	 * Modus "Einheitliche Rüstzeit (unabhängig vom Kundentypwechsel) vor allen Bedienungen"
	 */
	private final JRadioButton modeSingle;

	/**
	 * Modus "Einheitliche Rüstzeit bei Kundentypwechsel, sonst keine Rüstzeit"
	 */
	private final JRadioButton modeSingleOnChange;

	/**
	 * Modus "Einheitliche Rüstzeit jeweils bei gleichem Kundentyp und bei Kundentypwechsel"
	 */
	private final JRadioButton modeDual;

	/**
	 * Infozeile auf der "einzelne Rüstzeit" Dialogseite
	 */
	private final JLabel singleInfo;

	/**
	 * Auswahlbox für Eingabemodus bei "einzelne Rüstzeit"
	 */
	private final JComboBox<?> singleMode;

	/**
	 * Verteilungseditor für "einzelne Rüstzeit"
	 */
	private final JDistributionPanel singleDistribution;

	/**
	 * Rechenausdruck-Eingabezeile für "einzelne Rüstzeit"
	 */
	private final JTextField singleEdit;

	/**
	 * Auswahlbox (links) für Eingabemodus bei "zwei Rüstzeitvarianten"
	 */
	private final JComboBox<?> dual1Mode;

	/**
	 * Verteilungseditor (links) für "zwei Rüstzeitvarianten"
	 */
	private final JDistributionPanel dual1Distribution;

	/**
	 * Rechenausdruck-Eingabezeile (links) für "zwei Rüstzeitvarianten"
	 */
	private final JTextField dual1Edit;

	/**
	 * Auswahlbox (rechts) für Eingabemodus bei "zwei Rüstzeitvarianten"
	 */
	private final JComboBox<?> dual2Mode;

	/**
	 * Verteilungseditor (rechts) für "zwei Rüstzeitvarianten"
	 */
	private final JDistributionPanel dual2Distribution;

	/**
	 * Rechenausdruck-Eingabezeile (rechts) für "zwei Rüstzeitvarianten"
	 */
	private final JTextField dual2Edit;

	/**
	 * HTML-Kopf für Infozeilen
	 */
	private static final String HTML_LABEL_HEADER="<html><body style=\"font-size: 115%\"><b>";

	/**
	 * HTML-Fuß für Infozeilen
	 */
	private static final String HTML_LABEL_FOOTER="</b></body></html>";

	/**
	 * Konstruktor der Klasse
	 * @param editorPanel	Editor-Element an dem der Dialog ausgerichtet werden soll und in das die Daten ggf. zurückgeschrieben werden
	 */
	public DistributionSetupTimesEditorAssistantDialog(final DistributionSetupTimesEditor editorPanel) {
		super(editorPanel,Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Title"));
		this.editorPanel=editorPanel;

		/* GUI */
		buttonPrevious=addUserButton(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.PagesPrevious"),SimToolsImages.ARROW_LEFT.getIcon());
		buttonNext=addUserButton(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.PagesNext"),SimToolsImages.ARROW_RIGHT.getIcon());
		final JPanel all=createGUI(null);
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,InfoPanel.stationProcessSetupTimesAssistant);
		all.add(content=new JPanel(contentLayout=new CardLayout()),BorderLayout.CENTER);

		JPanel inner;
		JPanel line;
		Object[] data;

		/* Karte: Auswahl des Modus */
		final JPanel cardMode=new JPanel(new BorderLayout());
		content.add(cardMode,"0");
		cardMode.add(inner=new JPanel(),BorderLayout.NORTH);
		inner.setLayout(new BoxLayout(inner,BoxLayout.PAGE_AXIS));
		inner.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(modeOff=new JRadioButton(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.Off")));
		inner.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(modeSingle=new JRadioButton(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.Single")));
		inner.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(modeSingleOnChange=new JRadioButton(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.SingleOnChange")));
		inner.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		line.add(modeDual=new JRadioButton(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.Dual")));
		final ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(modeOff);
		buttonGroup.add(modeSingle);
		buttonGroup.add(modeSingleOnChange);
		buttonGroup.add(modeDual);
		modeOff.setSelected(true);
		modeOff.addActionListener(e->modeSelect());
		modeSingle.addActionListener(e->modeSelect());
		modeSingleOnChange.addActionListener(e->modeSelect());
		modeDual.addActionListener(e->modeSelect());

		/* Karte: Einheitliche Rüstzeit */
		final JPanel outer=new JPanel(new BorderLayout());
		content.add(outer,"1");
		outer.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		outer.add(singleInfo=new JLabel());
		data=initSetupTimesEditor();
		outer.add((JPanel)data[0],BorderLayout.CENTER);
		singleMode=(JComboBox<?>)data[1];
		singleDistribution=(JDistributionPanel)data[2];
		singleEdit=(JTextField)data[3];

		/* Karte: Zwei Varianten */
		final JPanel cardDual=new JPanel(new GridLayout(1,2));
		content.add(cardDual,"2");

		cardDual.add(inner=new JPanel(new BorderLayout()));
		inner.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(HTML_LABEL_HEADER+Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.Dual.NoClientTypeChange")+HTML_LABEL_FOOTER));
		data=initSetupTimesEditor();
		inner.add((JPanel)data[0],BorderLayout.CENTER);
		dual1Mode=(JComboBox<?>)data[1];
		dual1Distribution=(JDistributionPanel)data[2];
		dual1Edit=(JTextField)data[3];

		cardDual.add(inner=new JPanel(new BorderLayout()));
		inner.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
		line.add(new JLabel(HTML_LABEL_HEADER+Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.Dual.ClientTypeChange")+HTML_LABEL_FOOTER));
		data=initSetupTimesEditor();
		inner.add((JPanel)data[0],BorderLayout.CENTER);
		dual2Mode=(JComboBox<?>)data[1];
		dual2Distribution=(JDistributionPanel)data[2];
		dual2Edit=(JTextField)data[3];

		/* Dialog starten */
		setCard(0);
		setSizeRespectingScreensize(1000,800);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Generiert einen Editor für Verteilungen oder Rechenausdrücke
	 * @return	Liefert ein Objekt aus 4 Elementen: Gesamt-Panel, Modusauswahl Combobox, Verteilungseditor und Eingabezeile
	 */
	private Object[] initSetupTimesEditor() {
		final JPanel panel=new JPanel(new BorderLayout());

		JPanel line;

		/* Modusauswahl */
		final JPanel selectArea=new JPanel();
		panel.add(selectArea,BorderLayout.NORTH);
		selectArea.setLayout(new BoxLayout(selectArea,BoxLayout.PAGE_AXIS));
		selectArea.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)));
		final JLabel infoLabel=new JLabel(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.SetupTimesBy"));
		line.add(infoLabel);
		final JComboBox<String> modeSelect=new JComboBox<>(new String[]{
				Language.tr("Surface.DistributionByClientTypeEditor.DefineByDistribution"),
				Language.tr("Surface.DistributionByClientTypeEditor.DefineByExpression")
		});
		line.add(modeSelect);
		modeSelect.setRenderer(new IconListCellRenderer(new Images[]{
				Images.MODE_DISTRIBUTION,
				Images.MODE_EXPRESSION
		}));
		infoLabel.setLabelFor(modeSelect);

		/* Karten */
		final CardLayout cardLayout=new CardLayout();
		final JPanel cards=new JPanel(cardLayout);
		panel.add(cards,BorderLayout.CENTER);

		final Object[] obj=ModelElementBaseDialog.getInputPanel(Language.tr("Surface.DistributionByClientTypeEditor.Expression")+":","");
		final JTextField expressionEdit=(JTextField)obj[1];

		/* Verteilungseditor */
		final JDistributionPanel distributionPanel=new JDistributionPanel(new ExponentialDistribution(300),3600,true,s->{
			modeSelect.setSelectedIndex(1);
			cardLayout.show(cards,"expression");
			expressionEdit.setText(s);
			checkData(false);
		});
		cards.add(distributionPanel,"distribution");

		/* Rechenausdruck */
		final JPanel expressionPanel=new JPanel(new BorderLayout());
		cards.add(expressionPanel,"expression");
		final JPanel expressionArea=new JPanel();
		expressionPanel.add(expressionArea,BorderLayout.NORTH);
		expressionArea.setLayout(new BoxLayout(expressionArea,BoxLayout.PAGE_AXIS));
		expressionArea.add(line=(JPanel)obj[0]);
		line.add(ModelElementBaseDialog.getExpressionEditButton(this,expressionEdit,false,true,editorPanel.model,editorPanel.surface),BorderLayout.EAST);
		expressionEdit.addKeyListener(new KeyListener(){
			@Override public void keyTyped(KeyEvent e) {checkData(false);}
			@Override public void keyPressed(KeyEvent e) {checkData(false);}
			@Override public void keyReleased(KeyEvent e) {checkData(false);}
		});

		/* Vorbereiten */
		modeSelect.addActionListener(e->{
			cardLayout.show(cards,(modeSelect.getSelectedIndex()==0)?"distribution":"expression");
			checkData(false);
		});
		modeSelect.setSelectedIndex(0);

		return new Object[] {panel, modeSelect, distributionPanel, expressionEdit};
	}

	/**
	 * Reagiert auf eine Veränderung des gewählten Modus
	 */
	private void modeSelect() {
		setEnableOk(modeOff.isSelected());
		buttonNext.setEnabled(!modeOff.isSelected());
	}

	/**
	 * Aktiviert eine bestimmte Karte in {@link #content}
	 * @param nr	Index der zu aktivierenden Karte
	 * @see #content
	 * @see #contentLayout
	 * @see #currentCardIndex
	 */
	private void setCard(final int nr) {
		currentCardIndex=nr;
		switch (nr) {
		case 0:
			contentLayout.show(content,"0");
			modeSelect();
			buttonPrevious.setEnabled(false);
			break;
		case 1:
			contentLayout.show(content,"1");
			setEnableOk(true);
			buttonNext.setEnabled(false);
			buttonPrevious.setEnabled(true);
			break;
		case 2:
			contentLayout.show(content,"2");
			setEnableOk(true);
			buttonNext.setEnabled(false);
			buttonPrevious.setEnabled(true);
			break;
		}
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		if (nr==0) {
			setCard(0);
			return;
		}
		if (nr==1) {
			if (modeDual.isSelected()) {
				setCard(2);
				return;
			}
			if (modeSingle.isSelected()) {
				setCard(1);
				singleInfo.setText(HTML_LABEL_HEADER+Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.Single")+HTML_LABEL_FOOTER);
				return;
			}
			if (modeSingleOnChange.isSelected()) {
				setCard(1);
				singleInfo.setText(HTML_LABEL_HEADER+Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.Mode.SingleOnChange")+HTML_LABEL_FOOTER);
				return;
			}
			return;
		}
	}

	/**
	 * Prüft den Inhalt eines einzelnen Eingabefelder.
	 * @param field	Zu prüfendes Feld
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 * @see #checkData(boolean)
	 */
	private boolean checkExpression(final JTextField field, final boolean showErrorMessage) {
		final int error=ExpressionCalc.check(field.getText(),editorPanel.surface.getMainSurfaceVariableNames(editorPanel.model.getModelVariableNames(),true));
		if (error>=0) {
			field.setBackground(Color.RED);
			if (showErrorMessage) MsgBox.error(this,Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.ExpressionErrorTitle"),String.format(Language.tr("Surface.Process.Dialog.SetupTimes.Assistant.ExpressionErrorInfo"),field.getText(),error+1));
		} else {
			field.setBackground(NumberTools.getTextFieldDefaultBackground());
		}
		return error<0;
	}

	/**
	 * Prüft, ob die eingegebenen Daten in Ordnung sind.
	 * @param showErrorMessage	Wird hier <code>true</code> übergeben, so wird eine Fehlermeldung ausgegeben, wenn die Daten nicht in Ordnung sind.
	 * @return	Gibt <code>true</code> zurück, wenn die Daten in Ordnung sind.
	 */
	private boolean checkData(final boolean showErrorMessage) {
		boolean ok=true;
		switch (currentCardIndex) {
		case 1:
			if (singleMode.getSelectedIndex()==1) {
				ok=checkExpression(singleEdit,showErrorMessage);
				if (!ok && showErrorMessage) return false;
			}
			break;
		case 2:
			if (dual1Mode.getSelectedIndex()==1) {
				if (!checkExpression(dual1Edit,showErrorMessage)) ok=false;
				if (!ok && showErrorMessage) return false;
			}
			if (dual2Mode.getSelectedIndex()==1) {
				if (!checkExpression(dual2Edit,showErrorMessage)) ok=false;
				if (!ok && showErrorMessage) return false;
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
		if (modeOff.isSelected()) {
			editorPanel.data.clear();
			return;
		}

		if (modeSingle.isSelected()) {
			editorPanel.data.clear();
			for (String type1: editorPanel.clientTypes) for (String type2: editorPanel.clientTypes) {
				switch (singleMode.getSelectedIndex()) {
				case 0:
					editorPanel.data.set(type1,type2,singleDistribution.getDistribution());
					break;
				case 1:
					editorPanel.data.set(type1,type2,singleEdit.getText());
					break;
				}
			}
		}

		if (modeSingleOnChange.isSelected()) {
			editorPanel.data.clear();
			for (String type1: editorPanel.clientTypes) for (String type2: editorPanel.clientTypes) {
				if (!type1.equals(type2)) switch (singleMode.getSelectedIndex()) {
				case 0:
					editorPanel.data.set(type1,type2,singleDistribution.getDistribution());
					break;
				case 1:
					editorPanel.data.set(type1,type2,singleEdit.getText());
					break;
				}
			}
		}

		if (modeDual.isSelected()) {
			editorPanel.data.clear();
			for (String type1: editorPanel.clientTypes) for (String type2: editorPanel.clientTypes) {
				if (type1.equals(type2)) {
					switch (dual1Mode.getSelectedIndex()) {
					case 0:
						editorPanel.data.set(type1,type2,dual1Distribution.getDistribution());
						break;
					case 1:
						editorPanel.data.set(type1,type2,dual1Edit.getText());
						break;
					}
				} else {
					switch (dual2Mode.getSelectedIndex()) {
					case 0:
						editorPanel.data.set(type1,type2,dual2Distribution.getDistribution());
						break;
					case 1:
						editorPanel.data.set(type1,type2,dual2Edit.getText());
						break;
					}
				}
			}
		}
	}
}
